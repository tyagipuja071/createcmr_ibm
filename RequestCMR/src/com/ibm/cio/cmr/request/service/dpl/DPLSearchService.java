/**
 * 
 */
package com.ibm.cio.cmr.request.service.dpl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.activation.MimetypesFileTypeMap;
import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.ibm.cio.cmr.request.automation.RequestData;
import com.ibm.cio.cmr.request.automation.dpl.DPLSearchResult;
import com.ibm.cio.cmr.request.automation.util.CommonWordsUtil;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.entity.MassUpdtAddr;
import com.ibm.cio.cmr.request.entity.Scorecard;
import com.ibm.cio.cmr.request.entity.ScorecardPK;
import com.ibm.cio.cmr.request.model.ParamContainer;
import com.ibm.cio.cmr.request.service.BaseSimpleService;
import com.ibm.cio.cmr.request.service.requestentry.AttachmentService;
import com.ibm.cio.cmr.request.user.AppUser;
import com.ibm.cio.cmr.request.util.RequestUtils;
import com.ibm.cio.cmr.request.util.SystemLocation;
import com.ibm.cio.cmr.request.util.SystemUtil;
import com.ibm.cio.cmr.request.util.dpl.DPLResultCompany;
import com.ibm.cio.cmr.request.util.dpl.DPLResultsItemizer;
import com.ibm.cio.cmr.request.util.geo.GEOHandler;
import com.ibm.cio.cmr.request.util.pdf.impl.DPLSearchPDFConverter;
import com.ibm.cmr.services.client.CmrServicesFactory;
import com.ibm.cmr.services.client.DPLCheckClient;
import com.ibm.cmr.services.client.dpl.DPLCheckRequest;
import com.ibm.cmr.services.client.dpl.DPLRecord;
import com.ibm.cmr.services.client.dpl.DPLSearchResponse;
import com.ibm.cmr.services.client.dpl.DPLSearchResults;
import com.ibm.cmr.services.client.dpl.KycScreeningResponse;

/**
 * @author JeffZAMORA
 *
 */
@Component
public class DPLSearchService extends BaseSimpleService<Object> {

  private static final Logger LOG = Logger.getLogger(DPLSearchService.class);
  private static final MimetypesFileTypeMap MIME_TYPES = new MimetypesFileTypeMap();

  @Override
  public Object doProcess(EntityManager entityManager, HttpServletRequest request, ParamContainer params) throws Exception {
    String processType = (String) params.getParam("processType");
    if (StringUtils.isBlank(processType)) {
      throw new Exception("Process Type is not defined.");
    }
    switch (processType) {
    case "REQ":
      return processRequest(entityManager, params);
    case "SEARCH":
      return getItemizedDPLSearchResults(entityManager, params);
    case "ATTACH":
      return attachResultsToRequest(entityManager, params);
    case "ASSESS":
      return assessDPL(entityManager, params);
    case "ATTACHMASSUPDT":
      return attachResultsToMassUpdateRequest(entityManager, params);
    }
    return null;
  }

  /**
   * Retrieves the details of the current request and performs DPL check
   * 
   * @param entityManager
   * @param params
   * @return
   */
  private RequestData processRequest(EntityManager entityManager, ParamContainer params) throws Exception {
    Long reqId = (Long) params.getParam("reqId");
    LOG.debug("Retreiving Request data for Request ID " + reqId);
    RequestData reqData = new RequestData(entityManager, reqId);
    if (reqData.getAdmin() == null) {
      throw new Exception("Request " + reqId + " does not exist.");
    }
    return reqData;
  }

  /**
   * Attaches the results of the automated DPL search to the request
   * 
   * @param entityManager
   * @param params
   * @return
   * @throws Exception
   */
  private Boolean attachResultsToRequest(EntityManager entityManager, ParamContainer params) throws Exception {
    Long reqId = (Long) params.getParam("reqId");
    if (reqId == null) {
      throw new Exception("Request ID is required.");
    }
    AppUser user = (AppUser) params.getParam("user");

    RequestData reqData = processRequest(entityManager, params);
    String companyName = extractMainCompanyName(reqData, params);
    boolean success = true;
    try {
    List<DPLSearchResults> results = getPlainDPLSearchResults(entityManager, params);

    int resultCount = 0;
    for (DPLSearchResults result : results) {
      if (result.getDeniedPartyRecords() != null) {
        resultCount += result.getDeniedPartyRecords().size();
      }
    }
    ScorecardPK scorecardPk = new ScorecardPK();
    scorecardPk.setReqId(reqId);
    Scorecard scorecard = entityManager.find(Scorecard.class, scorecardPk);
    if (scorecard != null && resultCount == 0) {
      LOG.debug("Auto assessinging DPL check results.");
      scorecard.setDplAssessmentBy("CreateCMR");
      scorecard.setDplAssessmentCmt("No actual results found during the search.");
      scorecard.setDplAssessmentResult("N");
      scorecard.setDplAssessmentDate(SystemUtil.getActualTimestamp());
      entityManager.merge(scorecard);
      entityManager.flush();
    }

    AttachmentService attachmentService = new AttachmentService();
    Timestamp ts = SystemUtil.getActualTimestamp();
    DPLSearchPDFConverter pdf = new DPLSearchPDFConverter(user.getIntranetId(), ts.getTime(), companyName, results);
    pdf.setScorecard(scorecard);

    String type = "";
    try {
      type = MIME_TYPES.getContentType("temp.pdf");
    } catch (Exception e) {
    }
    if (StringUtils.isEmpty(type)) {
      type = "application/octet-stream";
    }

    String prefix = (String) params.getParam("filePrefix");
    if (prefix == null) {
      prefix = "DPLSearch_";
    }

    SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmm");
    String fileName = prefix + formatter.format(ts) + ".pdf";

    LOG.debug("Attaching " + fileName + " to Request " + reqId);

    try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
      pdf.exportToPdf(null, null, null, bos, null);

      byte[] pdfBytes = bos.toByteArray();

      try (ByteArrayInputStream bis = new ByteArrayInputStream(pdfBytes)) {
        try {
          attachmentService.removeAttachmentsOfType(entityManager, reqId, "DPL", prefix);
          attachmentService.addExternalAttachment(entityManager, user, reqId, "DPL", fileName, "DPL Search Results", bis);
        } catch (Exception e) {
          LOG.warn("Unable to save DPL attachment.", e);
          success = false;
        }
      }
    }
    } catch (Exception e){
      success = false;
    }
    return success;

  }

  /**
   * Extracts the company name to use
   * 
   * @param reqData
   * @return
   */
  private String extractMainCompanyName(RequestData reqData, ParamContainer params) {
    Data data = reqData.getData();

    String companyName = null;
    switch (data.getCmrIssuingCntry()) {
    case SystemLocation.ISRAEL:
      for (Addr addr : reqData.getAddresses()) {
        if ("CTYA".equals(addr.getId().getAddrType())) {
          companyName = addr.getCustNm1() + (StringUtils.isBlank(addr.getCustNm2()) ? " " + addr.getCustNm2() : "");
        }
      }
      break;
    case SystemLocation.JAPAN:
      for (Addr addr : reqData.getAddresses()) {
        if ("ZS01".equals(addr.getId().getAddrType())) {
          companyName = addr.getCustNm3();
        }
      }
      break;
    case SystemLocation.BRAZIL:
      String mainCustNam1 = (String) params.getParam("mainCustNam1") != null ? (String) params.getParam("mainCustNam1") : "";
      String mainCustNam2 = (String) params.getParam("mainCustNam2") != null ? (String) params.getParam("mainCustNam2") : "";
      if (StringUtils.isBlank(reqData.getAdmin().getMainCustNm1()) && StringUtils.isNotBlank(mainCustNam1)) {
        companyName = mainCustNam1;
      }
      if (StringUtils.isBlank(reqData.getAdmin().getMainCustNm2()) && StringUtils.isNotBlank(mainCustNam2)) {
        companyName += " " + mainCustNam2;
      }
      break;
    }
    if (StringUtils.isBlank(companyName)) {
      companyName = reqData.getAdmin().getMainCustNm1();
      if (!StringUtils.isBlank(reqData.getAdmin().getMainCustNm2())) {
        companyName += " " + reqData.getAdmin().getMainCustNm2();
      }
    }
    return companyName;
  }

  /**
   * Queries the database and does a dpl search
   * 
   * @param entityManager
   * @param params
   * @return
   * @throws Exception
   */
  private List<DPLResultsItemizer> getItemizedDPLSearchResults(EntityManager entityManager, ParamContainer params) throws Exception {

    List<DPLSearchResults> results = getPlainDPLSearchResults(entityManager, params);

    List<DPLResultsItemizer> list = new ArrayList<DPLResultsItemizer>();

    List<String> entityIds = new ArrayList<String>();
    for (DPLSearchResults result : results) {
      DPLResultsItemizer itemizer = new DPLResultsItemizer();
      itemizer.setSearchArgument(result.getSearchArgument());

      int itemNo = 1;
      for (DPLRecord record : result.getDeniedPartyRecords()) {
        String dplName = record.getCompanyName();
        if (StringUtils.isBlank(dplName) && !StringUtils.isBlank(record.getCustomerLastName())) {
          dplName = record.getCustomerFirstName() + " " + record.getCustomerLastName();
        }
        if (dplName == null) {
          dplName = "";
        }

        if (!entityIds.contains(record.getEntityId())) {
          DPLResultCompany company = itemizer.get(dplName);
          if (company == null) {
            company = new DPLResultCompany();
            company.setCompanyName(dplName);
            company.setItemNo(itemNo++);
            company.getRecords().add(record);

            itemizer.getRecords().add(company);
          } else {
            company.getRecords().add(record);
          }
          entityIds.add(record.getEntityId());
        }
      }
      List<DPLRecord> topMatches = DPLSearchResult.getTopMatches(result);
      itemizer.getTopMatches().addAll(topMatches);
      list.add(itemizer);
    }
    return list;
  }

  /**
   * Executes a DPL search and returns a list of results based on name
   * variations
   * 
   * @param entityManager
   * @param params
   * @return
   * @throws Exception
   */
  private List<DPLSearchResults> getPlainDPLSearchResults(EntityManager entityManager, ParamContainer params) throws Exception {
    List<String> names = new ArrayList<String>();
    List<DPLSearchResults> results = new ArrayList<DPLSearchResults>();

    Long reqId = (Long) params.getParam("reqId");
    RequestData reqData = null;
    if (reqId == null || reqId == 0) {
      String searchString = (String) params.getParam("searchString");
      if (searchString != null) {
        names.add(searchString.toUpperCase().trim());
      }
    } else {
      reqData = processRequest(entityManager, params);
      GEOHandler handler = RequestUtils.getGEOHandler(reqData.getData().getCmrIssuingCntry());
      if (handler != null && !handler.customerNamesOnAddress()) {
        String cntry = reqData.getData().getCmrIssuingCntry();

        if (SystemLocation.BRAZIL.equals(cntry)) {
          String cname1 = "";
          String cname2 = "";
          String cname = "";
          String mainCustNam1 = (String) params.getParam("mainCustNam1") != null ? (String) params.getParam("mainCustNam1") : "";
          String mainCustNam2 = (String) params.getParam("mainCustNam2") != null ? (String) params.getParam("mainCustNam2") : "";
          if (StringUtils.isBlank(reqData.getAdmin().getMainCustNm1()) && StringUtils.isNotBlank(mainCustNam1)) {
            cname1 = mainCustNam1;
          }
          if (StringUtils.isBlank(reqData.getAdmin().getMainCustNm2()) && StringUtils.isNotBlank(mainCustNam2)) {
            cname2 = mainCustNam2;
          }
          if (StringUtils.isNotBlank(cname1)) {
            cname = cname1 + (StringUtils.isBlank(cname2) ? " " + cname2 : "");
          } else {
            cname = reqData.getAdmin().getMainCustNm1().toUpperCase();
            if (!StringUtils.isBlank(reqData.getAdmin().getMainCustNm2())) {
              cname += " " + reqData.getAdmin().getMainCustNm2().toUpperCase();
            }
          }
          names.add(cname.toUpperCase());
        } else {
          String name = reqData.getAdmin().getMainCustNm1().toUpperCase();
          if (!StringUtils.isBlank(reqData.getAdmin().getMainCustNm2())) {
            name += " " + reqData.getAdmin().getMainCustNm2().toUpperCase();
          }
          names.add(name);
        }
      } else {
        String cntry = reqData.getData().getCmrIssuingCntry();
        for (Addr addr : reqData.getAddresses()) {
          if (addr.getId().getAddrType().contains("X")) {
            // no DPL check for uwanted addresses
            continue;
          }
          if (!"P".equals(addr.getDplChkResult()) && !"X".equals(addr.getDplChkResult()) && !"N".equals(addr.getDplChkResult())) {
            String name = "";
            // for japan, name is on cust nm3, CMR-7419
            if (SystemLocation.JAPAN.equals(cntry)) {
              name = addr.getCustNm3();
            } else {
              name = addr.getCustNm1() != null ? addr.getCustNm1().toUpperCase() : "";
              if (!StringUtils.isBlank(addr.getCustNm2())) {
                name += " " + addr.getCustNm2().toUpperCase();
              }
            }
            if (!StringUtils.isBlank(name) && !names.contains(name)) {
              names.add(name);
            }
          }
        }
      }
    }

    if (names.isEmpty()) {
      LOG.debug("No name specified to search.");
      return null;
    }

    // get a minimized name from search name
    List<String> minimizedList = new ArrayList<String>();
    for (String name : names) {
      String minimized = CommonWordsUtil.minimize(name).toUpperCase();
      if (!names.contains(minimized) && !minimizedList.contains(minimized)) {
        minimizedList.add(minimized);
      }
    }
    names.addAll(minimizedList);

    boolean isPrivate = reqData != null ? isPrivate(reqData) : false;
    String baseUrl = SystemConfiguration.getValue("CMR_SERVICES_URL");
    DPLCheckClient client = CmrServicesFactory.getInstance().createClient(baseUrl, DPLCheckClient.class);
    for (String searchString : names) {
      DPLCheckRequest request = new DPLCheckRequest();
      request.setId(new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
      request.setCompanyName(searchString);
      request.setIncludeScreening(true);
      request.setPrivate(isPrivate);
      try {
        LOG.debug("Performing DPL Search on " + searchString);
        DPLSearchResponse resp = null;
        if (SystemUtil.useKYCForDPLChecks()) {
          KycScreeningResponse kycResponse = client.executeAndWrap(DPLCheckClient.KYC_APP_ID, request, KycScreeningResponse.class);
          resp = RequestUtils.convertToLegacySearchResults("CreateCMR", kycResponse);
        } else {
          resp = client.executeAndWrap(DPLCheckClient.DPL_SEARCH_APP_ID, request, DPLSearchResponse.class);
        }
        if (resp.isSuccess()) {
          DPLSearchResults result = resp.getResults();
          result.setSearchArgument(searchString);
          results.add(result);
        } else {
          LOG.debug("An error was encountered when trying to search: " + resp.getMsg());
          throw new Exception("DPL search cannot be performed at the moment.");
        }
      } catch (Exception e) {
        LOG.warn("DPL Search encountered an error for " + searchString, e);
        throw new Exception("DPL search cannot be performed at the moment.");
      }

    }

    return results;
  }

  /**
   * Generates a PDF and writes it to the {@link HttpServletResponse} object
   * 
   * @param entityManager
   * @param params
   * @throws Exception
   */
  public void generatePDF(AppUser user, HttpServletResponse response, ParamContainer params) throws Exception {
    List<DPLSearchResults> results = getPlainDPLSearchResults(null, params);
    String searchString = (String) params.getParam("searchString");
    Timestamp ts = SystemUtil.getActualTimestamp();
    DPLSearchPDFConverter pdf = new DPLSearchPDFConverter(user.getIntranetId(), ts.getTime(), searchString, results);

    String type = "";
    try {
      type = MIME_TYPES.getContentType("temp.pdf");
    } catch (Exception e) {
    }
    if (StringUtils.isEmpty(type)) {
      type = "application/octet-stream";
    }

    SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmm");
    String fileName = "ManualDPLSearch_" + formatter.format(ts) + ".pdf";
    response.setCharacterEncoding("UTF-8");
    response.setContentType("application/pdf");
    response.addHeader("Content-Type", type);
    response.addHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");

    pdf.exportToPdf(null, null, null, response.getOutputStream(), null);
  }

  /**
   * Records the DPL Assessment
   * 
   * @param entityManager
   * @param params
   * @return
   * @throws Exception
   */
  private RequestData assessDPL(EntityManager entityManager, ParamContainer params) throws Exception {
    Long reqId = (Long) params.getParam("reqId");
    LOG.debug("Retreiving Request data for Request ID " + reqId);
    RequestData reqData = new RequestData(entityManager, reqId);
    if (reqData.getAdmin() == null) {
      throw new Exception("Request " + reqId + " does not exist.");
    }
    String assessment = (String) params.getParam("assessment");
    String cmt = (String) params.getParam("assessmentCmt");
    AppUser user = (AppUser) params.getParam("user");

    Scorecard scorecard = reqData.getScorecard();
    scorecard.setDplAssessmentResult(assessment);
    scorecard.setDplAssessmentBy(user.getIntranetId());
    scorecard.setDplAssessmentDate(SystemUtil.getActualTimestamp());
    scorecard.setDplAssessmentCmt(cmt);

    entityManager.merge(scorecard);
    entityManager.flush();

    return reqData;
  }

  /**
   * Attaches the results of the automated DPL search to the request
   * 
   * @param entityManager
   * @param params
   * @return
   * @throws Exception
   */
  private Boolean attachResultsToMassUpdateRequest(EntityManager entityManager, ParamContainer params) throws Exception {
    Long reqId = (Long) params.getParam("reqId");
    if (reqId == null) {
      throw new Exception("Request ID is required.");
    }
    AppUser user = (AppUser) params.getParam("user");

    RequestData reqData = processRequest(entityManager, params);
    String companyName = extractMainCompanyMassUpdtName(reqData, params);
    List<DPLSearchResults> results = getPlainDPLMassUpdateSearchResults(entityManager, params);

    int resultCount = 0;
    for (DPLSearchResults result : results) {
      if (result.getDeniedPartyRecords() != null) {
        resultCount += result.getDeniedPartyRecords().size();
      }
    }
    ScorecardPK scorecardPk = new ScorecardPK();
    scorecardPk.setReqId(reqId);
    Scorecard scorecard = entityManager.find(Scorecard.class, scorecardPk);
    if (scorecard != null && resultCount == 0) {
      LOG.debug("Auto assessinging DPL check results.");
      scorecard.setDplAssessmentBy("CreateCMR");
      scorecard.setDplAssessmentCmt("No actual results found during the search.");
      scorecard.setDplAssessmentResult("N");
      scorecard.setDplAssessmentDate(SystemUtil.getActualTimestamp());
      entityManager.merge(scorecard);
    }

    AttachmentService attachmentService = new AttachmentService();
    Timestamp ts = SystemUtil.getActualTimestamp();
    DPLSearchPDFConverter pdf = new DPLSearchPDFConverter(user.getIntranetId(), ts.getTime(), companyName, results);
    pdf.setScorecard(scorecard);

    String type = "";
    try {
      type = MIME_TYPES.getContentType("temp.pdf");
    } catch (Exception e) {
    }
    if (StringUtils.isEmpty(type)) {
      type = "application/octet-stream";
    }

    String prefix = (String) params.getParam("filePrefix");
    if (prefix == null) {
      prefix = "DPLSearch_";
    }

    SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmm");
    String fileName = prefix + formatter.format(ts) + ".pdf";

    LOG.debug("Attaching " + fileName + " to Request " + reqId);

    boolean success = true;
    try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
      pdf.exportToPdf(null, null, null, bos, null);

      byte[] pdfBytes = bos.toByteArray();

      try (ByteArrayInputStream bis = new ByteArrayInputStream(pdfBytes)) {
        try {
          attachmentService.removeAttachmentsOfType(entityManager, reqId, "DPL", prefix);
          attachmentService.addExternalAttachment(entityManager, user, reqId, "DPL", fileName, "DPL Search Results", bis);
        } catch (Exception e) {
          LOG.warn("Unable to save DPL attachment.", e);
          success = false;
        }
      }
    }
    return success;

  }

  /**
   * Extracts the company name to use
   * 
   * @param reqData
   * @return
   */
  private String extractMainCompanyMassUpdtName(RequestData reqData, ParamContainer params) {
    Data data = reqData.getData();
    String addrType = (String) params.getParam("addrType");
    String custname1 = (String) params.getParam("custname1");
    String custname2 = (String) params.getParam("custname2");

    String companyName = null;
    switch (data.getCmrIssuingCntry()) {
    case SystemLocation.ISRAEL:
      if ("CTYA".equals(addrType) || "CTYB".equals(addrType) || "CTYC".equals(addrType) || "ZI01".equals(addrType) || "ZS02".equals(addrType)) {
        companyName = custname1 + (StringUtils.isBlank(custname2) ? " " + custname2 : "");
      }
      break;
    case SystemLocation.JAPAN:
      for (MassUpdtAddr muAddr : reqData.getMuAddr()) {
        if ("ZS01".equals(muAddr.getId().getAddrType())) {
          companyName = muAddr.getCustNm3();
        }
      }
      break;

    }
    if (StringUtils.isBlank(companyName)) {
      companyName = reqData.getAdmin().getMainCustNm1();
      if (!StringUtils.isBlank(reqData.getAdmin().getMainCustNm2())) {
        companyName += " " + reqData.getAdmin().getMainCustNm2();
      }
    }
    return companyName;
  }

  /**
   * Executes a DPL search and returns a list of results based on name
   * variations
   * 
   * @param entityManager
   * @param params
   * @return
   * @throws Exception
   */
  private List<DPLSearchResults> getPlainDPLMassUpdateSearchResults(EntityManager entityManager, ParamContainer params) throws Exception {
    List<String> names = new ArrayList<String>();
    List<DPLSearchResults> results = new ArrayList<DPLSearchResults>();

    Long reqId = (Long) params.getParam("reqId");
    if (reqId == null || reqId == 0) {
      String searchString = (String) params.getParam("searchString");
      if (searchString != null) {
        names.add(searchString.toUpperCase().trim());
      }
    } else {
      RequestData reqData = processRequest(entityManager, params);
      GEOHandler handler = RequestUtils.getGEOHandler(reqData.getData().getCmrIssuingCntry());
      if (handler != null && !handler.customerNamesOnAddress()) {
        String name = reqData.getAdmin().getMainCustNm1().toUpperCase();
        if (!StringUtils.isBlank(reqData.getAdmin().getMainCustNm2())) {
          name += " " + reqData.getAdmin().getMainCustNm2().toUpperCase();
        }
        names.add(name);
      } else {
        String cntry = reqData.getData().getCmrIssuingCntry();
        String addrType = (String) params.getParam("addrType");
        String dplChkResult = (String) params.getParam("dplChkResult");
        String custname1 = (String) params.getParam("custname1");
        String custname2 = (String) params.getParam("custname2");
        if (addrType.contains("X")) {
          // no DPL check for uwanted addresses
          // continue;
        }
        if (!"P".equals(dplChkResult) && !"X".equals(dplChkResult) && !"N".equals(dplChkResult)) {
          String name = "";
          // for japan, name is on cust nm3, CMR-7419
          if (SystemLocation.JAPAN.equals(cntry)) {
            // name = muAddr.getCustNm3();
          } else {
            name = custname1 != null ? custname1.toUpperCase() : "";
            if (!StringUtils.isBlank(custname2)) {
              name += " " + custname2.toUpperCase();
            }
          }
          if (!StringUtils.isBlank(name) && !names.contains(name)) {
            names.add(name);
          }
        }
      }
    }

    if (names.isEmpty()) {
      LOG.debug("No name specified to search.");
      return null;
    }

    // get a minimized name from search name
    List<String> minimizedList = new ArrayList<String>();
    for (String name : names) {
      String minimized = CommonWordsUtil.minimize(name).toUpperCase();
      if (!names.contains(minimized) && !minimizedList.contains(minimized)) {
        minimizedList.add(minimized);
      }
    }
    names.addAll(minimizedList);

    String baseUrl = SystemConfiguration.getValue("CMR_SERVICES_URL");
    DPLCheckClient client = CmrServicesFactory.getInstance().createClient(baseUrl, DPLCheckClient.class);
    for (String searchString : names) {
      DPLCheckRequest request = new DPLCheckRequest();
      request.setId(new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
      request.setCompanyName(searchString);
      request.setIncludeScreening(true);
      try {
        LOG.debug("Performing DPL Search on " + searchString);
        DPLSearchResponse resp = null;
        if (SystemUtil.useKYCForDPLChecks()) {
          KycScreeningResponse kycResponse = client.executeAndWrap(DPLCheckClient.KYC_APP_ID, request, KycScreeningResponse.class);
          resp = RequestUtils.convertToLegacySearchResults("CreateCMR", kycResponse);
        } else {
          resp = client.executeAndWrap(DPLCheckClient.DPL_SEARCH_APP_ID, request, DPLSearchResponse.class);
        }
        if (resp.isSuccess()) {
          DPLSearchResults result = resp.getResults();
          result.setSearchArgument(searchString);
          results.add(result);
        } else {
          LOG.debug("An error was encountered when trying to search: " + resp.getMsg());
          break;
        }
      } catch (Exception e) {
        LOG.warn("DPL Search encountered an error for " + searchString, e);
      }
    }

    return results;
  }

  @Override
  protected boolean isTransactional() {
    return true;
  }

  /**
   * Returns the number of actual dpl search results for a particular search
   * performed.
   * 
   * @param entityManager
   * @param reqId
   * @param user
   * @return
   * @throws Exception
   */
  public int getResultCount(EntityManager entityManager, long reqId, AppUser user) throws Exception {
    ParamContainer params = new ParamContainer();
    params.addParam("processType", "SEARCH");
    params.addParam("reqId", reqId);
    params.addParam("user", user);
    List<DPLSearchResults> results = getPlainDPLSearchResults(entityManager, params);

    int resultCount = 0;
    for (DPLSearchResults result : results) {
      if (result.getDeniedPartyRecords() != null) {
        resultCount += result.getDeniedPartyRecords().size();
      }
    }
    return resultCount;
  }

  private boolean isPrivate(RequestData reqData) {
    Data data = reqData.getData();
    String subGrp = data.getCustSubGrp();
    if (subGrp != null) {
      if (subGrp.toUpperCase().contains("PRIV") || subGrp.toUpperCase().contains("PRIPE")) {
        return true;
      }
    }
    return "60".equals(data.getCustClass()) || "9500".equals(data.getIsicCd());
  }

}
