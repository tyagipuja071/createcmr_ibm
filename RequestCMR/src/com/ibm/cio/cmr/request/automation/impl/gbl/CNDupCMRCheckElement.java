package com.ibm.cio.cmr.request.automation.impl.gbl;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.automation.ActionOnError;
import com.ibm.cio.cmr.request.automation.AutomationElementRegistry;
import com.ibm.cio.cmr.request.automation.AutomationEngineData;
import com.ibm.cio.cmr.request.automation.RequestData;
import com.ibm.cio.cmr.request.automation.impl.DuplicateCheckElement;
import com.ibm.cio.cmr.request.automation.out.AutomationResult;
import com.ibm.cio.cmr.request.automation.out.MatchingOutput;
import com.ibm.cio.cmr.request.automation.util.AutomationUtil;
import com.ibm.cio.cmr.request.automation.util.DuplicateChecksUtil;
import com.ibm.cio.cmr.request.automation.util.ScenarioExceptionsUtil;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.AutomationMatching;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.entity.IntlAddr;
import com.ibm.cio.cmr.request.model.CompanyRecordModel;
import com.ibm.cio.cmr.request.model.requestentry.FindCMRRecordModel;
import com.ibm.cio.cmr.request.model.requestentry.FindCMRResultModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.util.BluePagesHelper;
import com.ibm.cio.cmr.request.util.CompanyFinder;
import com.ibm.cio.cmr.request.util.Person;
import com.ibm.cio.cmr.request.util.RequestUtils;
import com.ibm.cio.cmr.request.util.SystemUtil;
import com.ibm.cio.cmr.request.util.geo.GEOHandler;
import com.ibm.cio.cmr.request.util.mail.Email;
import com.ibm.cio.cmr.request.util.mail.MessageType;
import com.ibm.cmr.services.client.CmrServicesFactory;
import com.ibm.cmr.services.client.MatchingServiceClient;
import com.ibm.cmr.services.client.QueryClient;
import com.ibm.cmr.services.client.automation.AutomationResponse;
import com.ibm.cmr.services.client.automation.cn.CNResponse;
import com.ibm.cmr.services.client.matching.MatchingResponse;
import com.ibm.cmr.services.client.matching.cmr.DuplicateCMRCheckRequest;
import com.ibm.cmr.services.client.matching.cmr.DuplicateCMRCheckResponse;
import com.ibm.cmr.services.client.query.QueryRequest;
import com.ibm.cmr.services.client.query.QueryResponse;

public class CNDupCMRCheckElement extends DuplicateCheckElement {

  private static final Logger log = Logger.getLogger(CNDupCMRCheckElement.class);

  public CNDupCMRCheckElement(String requestTypes, String actionOnError, boolean overrideData, boolean stopOnError) {
    super(requestTypes, actionOnError, overrideData, stopOnError);
  }

  @Override
  public boolean importMatch(EntityManager entityManager, RequestData requestData, AutomationMatching match) {

    return false;
  }

  @Override
  public AutomationResult<MatchingOutput> executeElement(EntityManager entityManager, RequestData requestData, AutomationEngineData engineData)
      throws Exception {
    Addr soldTo = requestData.getAddress("ZS01");
    Admin admin = requestData.getAdmin();
    Data data = requestData.getData();

    AutomationResult<MatchingOutput> result = buildResult(admin.getId().getReqId());

    MatchingOutput output = new MatchingOutput();

    if (StringUtils.isNotBlank(admin.getDupCmrReason())) {
      StringBuilder details = new StringBuilder();
      details.append("User requested to proceed with Duplicate CMR Creation.").append("\n\n");
      details.append("Reason provided - ").append("\n");
      details.append(admin.getDupCmrReason()).append("\n");
      result.setDetails(details.toString());
      result.setResults("Overridden");
      result.setOnError(false);
    } else if (soldTo != null) {

      String cnNameSingleByte = null;
      String cnHistoryNameSingleByte = null;
      String cnAddrSingleByte = null;
      String cnCreditCd = data.getBusnType();
      String cnCeid = data.getPpsceid();

      String cnNameDoubleByte = null;
      String cnHistoryNameDoubleByte = null;
      String cnAddrDoubleByte = null;

      IntlAddr iAddr = new IntlAddr();
      CompanyRecordModel searchModelCNAPI = new CompanyRecordModel();

      AutomationResponse<CNResponse> resultCNApi = null;
      List<CompanyRecordModel> resultFindCmrCN = null;
      FindCMRResultModel findCMRResult = null;
      GEOHandler handler = RequestUtils.getGEOHandler(data.getCmrIssuingCntry());
      CompanyRecordModel cmrData = new CompanyRecordModel(); // nonLatin result
      MatchingResponse<DuplicateCMRCheckResponse> response = null; // Latin
                                                                   // result
      boolean nameMatched = false;
      boolean historyNmMatched = false;
      boolean ceidMatched = false;
      String nameFindCmrCnResult = null;
      String addrFindCmrCnResult = null;
      String ceidFindCmrCnResult = null;
      List<String> matchedCMRs = new ArrayList<>();
      StringBuilder details = new StringBuilder();

      boolean shouldBeRejected = false;
      String[] searchTerm08036 = getSearchTerm08036(entityManager);
      String[] searchTerm04182 = { "04182", "00075" };
      String[] cnSpecialKukla = { "81", "85", "43", "44", "45", "46" };
      String kukla = null;

      String pcmrs = "";
      boolean prospectCMRFlag = false;
      boolean convertPCMRFlag = false;
      boolean shouldGoToCMDE = false;
      boolean skipAndPpn = false;

      if (data.getCustSubGrp() != null) {
        switch (data.getCustSubGrp()) {
        case "NRMLC": // SCENARIO_LOCAL_NRMLC
        case "NRMLD": // SCENARIO_LOCAL_NRMLD

          // logic:
          // a) Duplication means CMRs with same Chinese name or Historical
          // Chinese name.
          // b) If the existing CMR have CEID,and Search Term not as "08036"
          // don't define as duplication.
          // c) If the existing CMR don't have CEID and Search Term is
          // "08036",then defined as Duplication.
          // d) If the existing CMR don't have CEID and Search Term not as
          // "08036",and Kukla not in ('81','85','43','44','45','46') .then
          // defined as Duplication.
          // e) If the existing CMR don't have CEID and Search Term not as
          // "08036",and Kukla in ('81','85','43','44','45','46') .then don't
          // defined as Duplication.
          // f) check China API with single byte string value
          // g) check findcmr twice with single byte and double byte value, if
          // they are not equals

          // CREATCMR-5461
          // check if should skip CNDupCMRCheck and set req status PPN
          skipAndPpn = getSkipAndPpnInd(entityManager, data);
          if (skipAndPpn) {
            log.debug("skip CN Dup CMR Check for req " + data.getId().getReqId());
            doSkipAndPpn(result, engineData);
            return result;
          }

          iAddr = handler.getIntlAddrById(soldTo, entityManager);
          if (iAddr != null) {

            try {

              // 1, Check CN API
              if (cnCreditCd != null && cnCreditCd.length() > 0) {
                searchModelCNAPI.setIssuingCntry(data.getCmrIssuingCntry());
                searchModelCNAPI.setCountryCd(soldTo.getLandCntry());
                searchModelCNAPI.setTaxCd1(cnCreditCd);
                resultCNApi = CompanyFinder.getCNApiInfo(searchModelCNAPI, "TAXCD");
                if (resultCNApi.getMessage() != null && resultCNApi.getMessage().contains("无数据")) {
                  boolean hasCNSpecialAttach = checkCNSpecialAttach(entityManager, data.getId().getReqId());
                  if (hasCNSpecialAttach) {
                    doSkipAndPpn4NoData(result, engineData);
                    return result;
                  }
                }
              } else {
                cnNameSingleByte = iAddr.getIntlCustNm1() + (!StringUtils.isBlank(iAddr.getIntlCustNm2()) ? (" " + iAddr.getIntlCustNm2()) : "");
                cnNameSingleByte = convert2SingleByte(cnNameSingleByte);
                searchModelCNAPI.setIssuingCntry(data.getCmrIssuingCntry());
                searchModelCNAPI.setCountryCd(soldTo.getLandCntry());
                searchModelCNAPI.setAltName(cnNameSingleByte);
                resultCNApi = CompanyFinder.getCNApiInfo(searchModelCNAPI, "ALTNAME");
              }

              if (resultCNApi != null && resultCNApi.isSuccess()) {
                cnNameSingleByte = resultCNApi.getRecord().getName();
                cnHistoryNameSingleByte = resultCNApi.getRecord().getHistoryNames();
                cnAddrSingleByte = resultCNApi.getRecord().getRegLocation();

                cnNameDoubleByte = convert2DoubleByte(cnNameSingleByte);
                cnHistoryNameDoubleByte = convert2DoubleByte(cnHistoryNameSingleByte);
                cnAddrDoubleByte = convert2DoubleByte(cnAddrSingleByte);

                try {
                  // 2, Check FindCMR NON Latin with Chinese name - single byte
                  CompanyRecordModel searchModelFindCmrCN = new CompanyRecordModel();
                  searchModelFindCmrCN.setIssuingCntry(data.getCmrIssuingCntry());
                  searchModelFindCmrCN.setCountryCd(soldTo.getLandCntry());
                  searchModelFindCmrCN.setName(cnNameSingleByte);
                  findCMRResult = searchFindCMR(searchModelFindCmrCN);

                  if (findCMRResult != null && findCMRResult.getItems() != null && !findCMRResult.getItems().isEmpty()) {
                    List<FindCMRRecordModel> cmrs = findCMRResult.getItems();
                    log.debug("There are " + cmrs.size() + " cmrs retrieved from FINDCMR.");
                    for (FindCMRRecordModel cmrsMods : cmrs) {

                      nameFindCmrCnResult = cmrsMods.getCmrIntlName1();
                      if (!StringUtils.isBlank(cmrsMods.getCmrIntlName2())) {
                        nameFindCmrCnResult = cmrsMods.getCmrIntlName1() + cmrsMods.getCmrIntlName2();
                      }

                      log.debug("CNAPI retrieved name is <" + cnNameSingleByte + "> after trim Chinese Space is <"
                          + trimChineseSpace(cnNameSingleByte) + ">");
                      log.debug("FINDCMR retrieved name is <" + nameFindCmrCnResult + "> after trim Chinese Space is <"
                          + trimChineseSpace(nameFindCmrCnResult) + ">");

                      if (nameFindCmrCnResult != null && cnNameSingleByte.equals(nameFindCmrCnResult)) {

                        kukla = getKukla(entityManager, cmrsMods.getCmrNum(), data.getCmrIssuingCntry());

                        log.debug("CNDupCMRCheckElement: request " + data.getId().getReqId() + ", CmrNo " + cmrsMods.getCmrNum() + ", ceid "
                            + cmrsMods.getCmrPpsceid() + ", search term " + cmrsMods.getCmrSortl() + ", kukla " + kukla);

                        if (cmrsMods.getCmrNum() != null && cmrsMods.getCmrNum().startsWith("P")) {
                          // CREATCMR-6302
                          if (StringUtils.isBlank(data.getCmrNo()) || !data.getCmrNo().equals(cmrsMods.getCmrNum())) {
                            log.debug("Found a duplicate prospect CMR.");
                            pcmrs += cmrsMods.getCmrNum() + ", ";
                            prospectCMRFlag = true;
                            handleLogDetails(cmrsMods, cmrData, matchedCMRs, details);
                          } else if (StringUtils.isNotBlank(data.getCmrNo()) && data.getCmrNo().equals(cmrsMods.getCmrNum())) {
                            convertPCMRFlag = true;
                            log.debug("Skip Duplicate CMR check for Convert to Legal CMR for prospect CmrNo " + cmrsMods.getCmrNum());
                          } else {
                            log.debug("Skip Duplicate CMR check for prospect CmrNo " + cmrsMods.getCmrNum());
                          }
                          continue;
                        }

                        nameMatched = true;

                        if (cmrsMods.getCmrPpsceid() == null || StringUtils.isBlank(cmrsMods.getCmrPpsceid())) {
                          if (incloud(cmrsMods.getCmrSortl(), searchTerm04182) || "00462".equals(cmrsMods.getCmrSortl())
                              || (cmrsMods.getCmrSortl() == null || StringUtils.isBlank(cmrsMods.getCmrSortl())
                                  || (cmrsMods.getCmrSortl() != null && (cmrsMods.getCmrSortl().trim().equalsIgnoreCase("000000")
                                      || cmrsMods.getCmrSortl().trim().equalsIgnoreCase("00000") || cmrsMods.getCmrSortl().matches("[^0-9]+"))))) {
                            if (cmrsMods.getCmrNum() != null && (cmrsMods.getCmrNum().startsWith("0") || cmrsMods.getCmrNum().startsWith("1")
                                || cmrsMods.getCmrNum().startsWith("2") || cmrsMods.getCmrNum().startsWith("9"))) {
                              // should not be rejected
                              log.debug("Not a duplicate CMR.");
                            } else {
                              // should be rejected
                              log.debug("Duplicate CMR. Request should be rejected.");
                              shouldBeRejected = true;
                              handleLogDetails(cmrsMods, cmrData, matchedCMRs, details);
                            }
                          } else {
                            // should be rejected
                            log.debug("Duplicate CMR. Request should be rejected.");
                            shouldBeRejected = true;
                            handleLogDetails(cmrsMods, cmrData, matchedCMRs, details);
                          }
                        } else {
                          if (incloud(cmrsMods.getCmrSortl(), searchTerm04182)
                              || (cmrsMods.getCmrSortl() == null || StringUtils.isBlank(cmrsMods.getCmrSortl())
                                  || (cmrsMods.getCmrSortl() != null && (cmrsMods.getCmrSortl().trim().equalsIgnoreCase("000000")
                                      || cmrsMods.getCmrSortl().trim().equalsIgnoreCase("00000") || cmrsMods.getCmrSortl().matches("[^0-9]+"))))
                                  && cmrsMods.getCmrNum() != null && (cmrsMods.getCmrNum().startsWith("0") || cmrsMods.getCmrNum().startsWith("1")
                                      || cmrsMods.getCmrNum().startsWith("2"))) {
                            // should not be rejected
                            log.debug("Not a duplicate CMR.");
                          } else {
                            if (!incloud(cmrsMods.getCmrSortl(), searchTerm08036) && incloud(kukla, cnSpecialKukla)) {
                              // should not be rejected
                              log.debug("Not a duplicate CMR.");
                            } else {
                              // should be rejected
                              log.debug("Duplicate CMR. Request should be rejected.");
                              shouldBeRejected = true;
                              handleLogDetails(cmrsMods, cmrData, matchedCMRs, details);
                            }
                          }
                        }

                      }
                    }
                  }
                } catch (Exception e) {
                  e.printStackTrace();
                  result.setDetails("Error on checking findCMR on Duplicate CMR Check of Chinese, name: " + cnNameSingleByte);
                  engineData.addRejectionComment("OTH", "Error on checking findCMR on Duplicate CMR Check of Chinese, name: " + cnNameSingleByte, "",
                      "");
                  result.setOnError(true);
                  result.setResults("Error on checking findCMR on Duplicate CMR Check of Chinese, name: " + cnNameSingleByte);
                }

                if (cnNameDoubleByte != null && !cnNameDoubleByte.equals(cnNameSingleByte)) {
                  // 3, Check FindCMR NON Latin with Chinese name - double byte
                  try {
                    CompanyRecordModel searchModelFindCmrCN = new CompanyRecordModel();
                    searchModelFindCmrCN.setIssuingCntry(data.getCmrIssuingCntry());
                    searchModelFindCmrCN.setCountryCd(soldTo.getLandCntry());
                    searchModelFindCmrCN.setName(cnNameDoubleByte);
                    findCMRResult = searchFindCMR(searchModelFindCmrCN);

                    if (findCMRResult != null && findCMRResult.getItems() != null && !findCMRResult.getItems().isEmpty()) {
                      List<FindCMRRecordModel> cmrs = findCMRResult.getItems();
                      for (FindCMRRecordModel cmrsMods : cmrs) {

                        nameFindCmrCnResult = cmrsMods.getCmrIntlName1();
                        if (!StringUtils.isBlank(cmrsMods.getCmrIntlName2())) {
                          nameFindCmrCnResult = cmrsMods.getCmrIntlName1() + cmrsMods.getCmrIntlName2();
                        }

                        if (nameFindCmrCnResult != null && trimChineseSpace(cnNameDoubleByte).equals(trimChineseSpace(nameFindCmrCnResult))) {

                          kukla = getKukla(entityManager, cmrsMods.getCmrNum(), data.getCmrIssuingCntry());

                          log.debug("CNDupCMRCheckElement: request " + data.getId().getReqId() + ", CmrNo " + cmrsMods.getCmrNum() + ", ceid "
                              + cmrsMods.getCmrPpsceid() + ", search term " + cmrsMods.getCmrSortl() + ", kukla " + kukla);

                          if (cmrsMods.getCmrNum() != null && cmrsMods.getCmrNum().startsWith("P")) {
                            // CREATCMR-6302
                            // log.debug("Skip Duplicate CMR check for prospect
                            // CmrNo " + cmrsMods.getCmrNum());
                            // continue;
                            if (StringUtils.isBlank(data.getCmrNo()) || !data.getCmrNo().equals(cmrsMods.getCmrNum())) {
                              log.debug("Found a duplicate prospect CMR.");
                              pcmrs += cmrsMods.getCmrNum() + ", ";
                              prospectCMRFlag = true;
                              handleLogDetails(cmrsMods, cmrData, matchedCMRs, details);
                            } else if (StringUtils.isNotBlank(data.getCmrNo()) && data.getCmrNo().equals(cmrsMods.getCmrNum())) {
                              convertPCMRFlag = true;
                              log.debug("Skip Duplicate CMR check for Convert to Legal CMR for prospect CmrNo " + cmrsMods.getCmrNum());
                            } else {
                              log.debug("Skip Duplicate CMR check for prospect CmrNo " + cmrsMods.getCmrNum());
                            }
                            continue;

                          }

                          nameMatched = true;

                          if (cmrsMods.getCmrPpsceid() == null || StringUtils.isBlank(cmrsMods.getCmrPpsceid())) {
                            if (incloud(cmrsMods.getCmrSortl(), searchTerm04182) || "00462".equals(cmrsMods.getCmrSortl())
                                || (cmrsMods.getCmrSortl() == null || StringUtils.isBlank(cmrsMods.getCmrSortl())
                                    || (cmrsMods.getCmrSortl() != null && (cmrsMods.getCmrSortl().trim().equalsIgnoreCase("000000")
                                        || cmrsMods.getCmrSortl().trim().equalsIgnoreCase("00000") || cmrsMods.getCmrSortl().matches("[^0-9]+"))))) {
                              if (cmrsMods.getCmrNum() != null && (cmrsMods.getCmrNum().startsWith("0") || cmrsMods.getCmrNum().startsWith("1")
                                  || cmrsMods.getCmrNum().startsWith("2") || cmrsMods.getCmrNum().startsWith("9"))) {
                                // should not be rejected
                                log.debug("Not a duplicate CMR.");
                              } else {
                                // should be rejected
                                log.debug("Duplicate CMR. Request should be rejected.");
                                shouldBeRejected = true;
                                handleLogDetails(cmrsMods, cmrData, matchedCMRs, details);
                              }
                            } else {
                              // should be rejected
                              log.debug("Duplicate CMR. Request should be rejected.");
                              shouldBeRejected = true;
                              handleLogDetails(cmrsMods, cmrData, matchedCMRs, details);
                            }
                          } else {
                            if (incloud(cmrsMods.getCmrSortl(), searchTerm04182)
                                || (cmrsMods.getCmrSortl() == null || StringUtils.isBlank(cmrsMods.getCmrSortl())
                                    || (cmrsMods.getCmrSortl() != null && (cmrsMods.getCmrSortl().trim().equalsIgnoreCase("000000")
                                        || cmrsMods.getCmrSortl().trim().equalsIgnoreCase("00000") || cmrsMods.getCmrSortl().matches("[^0-9]+"))))
                                    && cmrsMods.getCmrNum() != null && (cmrsMods.getCmrNum().startsWith("0") || cmrsMods.getCmrNum().startsWith("1")
                                        || cmrsMods.getCmrNum().startsWith("2"))) {
                              // should not be rejected
                              log.debug("Not a duplicate CMR.");
                            } else {
                              if (!incloud(cmrsMods.getCmrSortl(), searchTerm08036) && incloud(kukla, cnSpecialKukla)) {
                                // should not be rejected
                                log.debug("Not a duplicate CMR.");
                              } else {
                                // should be rejected
                                log.debug("Duplicate CMR. Request should be rejected.");
                                shouldBeRejected = true;
                                handleLogDetails(cmrsMods, cmrData, matchedCMRs, details);
                              }
                            }
                          }

                        }
                      }
                    }
                  } catch (Exception e) {
                    e.printStackTrace();
                    result.setDetails("Error on checking findCMR on Duplicate CMR Check of Chinese, name: " + cnNameDoubleByte);
                    engineData.addRejectionComment("OTH", "Error on checking findCMR on Duplicate CMR Check of Chinese, name: " + cnNameDoubleByte,
                        "", "");
                    result.setOnError(true);
                    result.setResults("Error on checking findCMR on Duplicate CMR Check of Chinese, name: " + cnNameDoubleByte);
                  }
                }

                // 4, Check FindCMR Non Latin with historical Chinese name -
                // single byte
                if (cnHistoryNameSingleByte != null && !"".equals(cnHistoryNameSingleByte)) {
                  List<String> cnHistoryNameList = Arrays.asList(cnHistoryNameSingleByte.split(";"));
                  CompanyRecordModel searchModelFindCmrCN = new CompanyRecordModel();
                  searchModelFindCmrCN.setIssuingCntry(data.getCmrIssuingCntry());
                  try {
                    for (String historyName : cnHistoryNameList) {
                      searchModelFindCmrCN.setName(historyName);
                      findCMRResult = searchFindCMR(searchModelFindCmrCN);

                      if (findCMRResult != null && findCMRResult.getItems() != null && !findCMRResult.getItems().isEmpty()) {
                        List<FindCMRRecordModel> cmrs = findCMRResult.getItems();
                        for (FindCMRRecordModel cmrsMods : cmrs) {

                          nameFindCmrCnResult = cmrsMods.getCmrIntlName1();
                          if (!StringUtils.isBlank(cmrsMods.getCmrIntlName2())) {
                            nameFindCmrCnResult = cmrsMods.getCmrIntlName1() + cmrsMods.getCmrIntlName2();
                          }

                          if (nameFindCmrCnResult != null && trimChineseSpace(nameFindCmrCnResult).equals(trimChineseSpace(historyName))) {

                            kukla = getKukla(entityManager, cmrsMods.getCmrNum(), data.getCmrIssuingCntry());

                            log.debug("CNDupCMRCheckElement: request " + data.getId().getReqId() + ", CmrNo " + cmrsMods.getCmrNum() + ", ceid "
                                + cmrsMods.getCmrPpsceid() + ", search term " + cmrsMods.getCmrSortl() + ", kukla " + kukla);

                            if (cmrsMods.getCmrNum() != null && cmrsMods.getCmrNum().startsWith("P")) {
                              // CREATCMR-6302
                              // log.debug("Skip Duplicate CMR check for
                              // prospect CmrNo " + cmrsMods.getCmrNum());
                              // continue;
                              if (StringUtils.isBlank(data.getCmrNo()) || !data.getCmrNo().equals(cmrsMods.getCmrNum())) {
                                log.debug("Found a duplicate prospect CMR.");
                                pcmrs += cmrsMods.getCmrNum() + ", ";
                                prospectCMRFlag = true;
                                handleLogDetails(cmrsMods, cmrData, matchedCMRs, details);
                              } else if (StringUtils.isNotBlank(data.getCmrNo()) && data.getCmrNo().equals(cmrsMods.getCmrNum())) {
                                convertPCMRFlag = true;
                                log.debug("Skip Duplicate CMR check for Convert to Legal CMR for prospect CmrNo " + cmrsMods.getCmrNum());
                              } else {
                                log.debug("Skip Duplicate CMR check for prospect CmrNo " + cmrsMods.getCmrNum());
                              }
                              continue;

                            }

                            historyNmMatched = true;

                            if (cmrsMods.getCmrPpsceid() == null || StringUtils.isBlank(cmrsMods.getCmrPpsceid())) {
                              if (incloud(cmrsMods.getCmrSortl(), searchTerm04182) || "00462".equals(cmrsMods.getCmrSortl())
                                  || (cmrsMods.getCmrSortl() == null || StringUtils.isBlank(cmrsMods.getCmrSortl())
                                      || (cmrsMods.getCmrSortl() != null && (cmrsMods.getCmrSortl().trim().equalsIgnoreCase("000000")
                                          || cmrsMods.getCmrSortl().trim().equalsIgnoreCase("00000")
                                          || cmrsMods.getCmrSortl().matches("[^0-9]+"))))) {
                                if (cmrsMods.getCmrNum() != null && (cmrsMods.getCmrNum().startsWith("0") || cmrsMods.getCmrNum().startsWith("1")
                                    || cmrsMods.getCmrNum().startsWith("2") || cmrsMods.getCmrNum().startsWith("9"))) {
                                  // should not be rejected
                                  log.debug("Not a duplicate CMR.");
                                } else {
                                  // should be rejected
                                  log.debug("Duplicate CMR. Request should be rejected.");
                                  shouldBeRejected = true;
                                  handleLogDetails(cmrsMods, cmrData, matchedCMRs, details);
                                }
                              } else {
                                // should be rejected
                                log.debug("Duplicate CMR. Request should be rejected.");
                                shouldBeRejected = true;
                                handleLogDetails(cmrsMods, cmrData, matchedCMRs, details);
                              }
                            } else {
                              if (incloud(cmrsMods.getCmrSortl(), searchTerm04182)
                                  || (cmrsMods.getCmrSortl() == null || StringUtils.isBlank(cmrsMods.getCmrSortl())
                                      || (cmrsMods.getCmrSortl() != null && (cmrsMods.getCmrSortl().trim().equalsIgnoreCase("000000")
                                          || cmrsMods.getCmrSortl().trim().equalsIgnoreCase("00000") || cmrsMods.getCmrSortl().matches("[^0-9]+"))))
                                      && cmrsMods.getCmrNum() != null && (cmrsMods.getCmrNum().startsWith("0") || cmrsMods.getCmrNum().startsWith("1")
                                          || cmrsMods.getCmrNum().startsWith("2"))) {
                                // should not be rejected
                                log.debug("Not a duplicate CMR.");
                              } else {
                                if (!incloud(cmrsMods.getCmrSortl(), searchTerm08036) && incloud(kukla, cnSpecialKukla)) {
                                  // should not be rejected
                                  log.debug("Not a duplicate CMR.");
                                } else {
                                  // should be rejected
                                  log.debug("Duplicate CMR. Request should be rejected.");
                                  shouldBeRejected = true;
                                  handleLogDetails(cmrsMods, cmrData, matchedCMRs, details);
                                }
                              }
                            }

                          }
                        }
                      }
                    }

                  } catch (Exception e) {
                    e.printStackTrace();
                    result
                        .setDetails("Error on getting findCMR data when Duplicate CMR Check of historical Chinese, name: " + cnHistoryNameSingleByte);
                    engineData.addRejectionComment("OTH",
                        "Error on getting findCMR data when Duplicate CMR Check of historical Chinese, name: " + cnHistoryNameSingleByte, "", "");
                    result.setOnError(true);
                    result
                        .setResults("Error on getting findCMR data when Duplicate CMR Check of historical Chinese, name: " + cnHistoryNameSingleByte);
                  }

                }

                // 5, Check FindCMR Non Latin with historical Chinese name -
                // double byte
                if (cnHistoryNameDoubleByte != null && !"".equals(cnHistoryNameDoubleByte)
                    && !cnHistoryNameDoubleByte.equals(cnHistoryNameSingleByte)) {
                  List<String> cnHistoryNameList = Arrays.asList(cnHistoryNameDoubleByte.split(";"));
                  CompanyRecordModel searchModelFindCmrCN = new CompanyRecordModel();
                  searchModelFindCmrCN.setIssuingCntry(data.getCmrIssuingCntry());
                  try {
                    for (String historyName : cnHistoryNameList) {
                      searchModelFindCmrCN.setName(historyName);
                      findCMRResult = searchFindCMR(searchModelFindCmrCN);

                      if (findCMRResult != null && findCMRResult.getItems() != null && !findCMRResult.getItems().isEmpty()) {
                        List<FindCMRRecordModel> cmrs = findCMRResult.getItems();
                        for (FindCMRRecordModel cmrsMods : cmrs) {

                          nameFindCmrCnResult = cmrsMods.getCmrIntlName1();
                          if (!StringUtils.isBlank(cmrsMods.getCmrIntlName2())) {
                            nameFindCmrCnResult = cmrsMods.getCmrIntlName1() + cmrsMods.getCmrIntlName2();
                          }

                          if (nameFindCmrCnResult != null && trimChineseSpace(nameFindCmrCnResult).equals(trimChineseSpace(historyName))) {

                            kukla = getKukla(entityManager, cmrsMods.getCmrNum(), data.getCmrIssuingCntry());

                            log.debug("CNDupCMRCheckElement: request " + data.getId().getReqId() + ", CmrNo " + cmrsMods.getCmrNum() + ", ceid "
                                + cmrsMods.getCmrPpsceid() + ", search term " + cmrsMods.getCmrSortl() + ", kukla " + kukla);

                            if (cmrsMods.getCmrNum() != null && cmrsMods.getCmrNum().startsWith("P")) {
                              // CREATCMR-6302
                              // log.debug("Skip Duplicate CMR check for
                              // prospect CmrNo " + cmrsMods.getCmrNum());
                              // continue;
                              if (StringUtils.isBlank(data.getCmrNo()) || !data.getCmrNo().equals(cmrsMods.getCmrNum())) {
                                log.debug("Found a duplicate prospect CMR.");
                                pcmrs += cmrsMods.getCmrNum() + ", ";
                                prospectCMRFlag = true;
                                handleLogDetails(cmrsMods, cmrData, matchedCMRs, details);
                              } else if (StringUtils.isNotBlank(data.getCmrNo()) && data.getCmrNo().equals(cmrsMods.getCmrNum())) {
                                convertPCMRFlag = true;
                                log.debug("Skip Duplicate CMR check for Convert to Legal CMR for prospect CmrNo " + cmrsMods.getCmrNum());
                              } else {
                                log.debug("Skip Duplicate CMR check for prospect CmrNo " + cmrsMods.getCmrNum());
                              }
                              continue;

                            }

                            historyNmMatched = true;

                            if (cmrsMods.getCmrPpsceid() == null || StringUtils.isBlank(cmrsMods.getCmrPpsceid())) {
                              if (incloud(cmrsMods.getCmrSortl(), searchTerm04182) || "00462".equals(cmrsMods.getCmrSortl())
                                  || (cmrsMods.getCmrSortl() == null || StringUtils.isBlank(cmrsMods.getCmrSortl())
                                      || (cmrsMods.getCmrSortl() != null && (cmrsMods.getCmrSortl().trim().equalsIgnoreCase("000000")
                                          || cmrsMods.getCmrSortl().trim().equalsIgnoreCase("00000")
                                          || cmrsMods.getCmrSortl().matches("[^0-9]+"))))) {
                                if (cmrsMods.getCmrNum() != null && (cmrsMods.getCmrNum().startsWith("0") || cmrsMods.getCmrNum().startsWith("1")
                                    || cmrsMods.getCmrNum().startsWith("2") || cmrsMods.getCmrNum().startsWith("9"))) {
                                  // should not be rejected
                                  log.debug("Not a duplicate CMR.");
                                } else {
                                  // should be rejected
                                  log.debug("Duplicate CMR. Request should be rejected.");
                                  shouldBeRejected = true;
                                  handleLogDetails(cmrsMods, cmrData, matchedCMRs, details);
                                }
                              } else {
                                // should be rejected
                                log.debug("Duplicate CMR. Request should be rejected.");
                                shouldBeRejected = true;
                                handleLogDetails(cmrsMods, cmrData, matchedCMRs, details);
                              }
                            } else {
                              if (incloud(cmrsMods.getCmrSortl(), searchTerm04182)
                                  || (cmrsMods.getCmrSortl() == null || StringUtils.isBlank(cmrsMods.getCmrSortl())
                                      || (cmrsMods.getCmrSortl() != null && (cmrsMods.getCmrSortl().trim().equalsIgnoreCase("000000")
                                          || cmrsMods.getCmrSortl().trim().equalsIgnoreCase("00000") || cmrsMods.getCmrSortl().matches("[^0-9]+"))))
                                      && cmrsMods.getCmrNum() != null && (cmrsMods.getCmrNum().startsWith("0") || cmrsMods.getCmrNum().startsWith("1")
                                          || cmrsMods.getCmrNum().startsWith("2"))) {
                                // should not be rejected
                                log.debug("Not a duplicate CMR.");
                              } else {
                                if (!incloud(cmrsMods.getCmrSortl(), searchTerm08036) && incloud(kukla, cnSpecialKukla)) {
                                  // should not be rejected
                                  log.debug("Not a duplicate CMR.");
                                } else {
                                  // should be rejected
                                  log.debug("Duplicate CMR. Request should be rejected.");
                                  shouldBeRejected = true;
                                  handleLogDetails(cmrsMods, cmrData, matchedCMRs, details);
                                }
                              }
                            }

                          }
                        }
                      }
                    }

                  } catch (Exception e) {
                    e.printStackTrace();
                    result
                        .setDetails("Error on getting findCMR data when Duplicate CMR Check of historical Chinese, name: " + cnHistoryNameDoubleByte);
                    engineData.addRejectionComment("OTH",
                        "Error on getting findCMR data when Duplicate CMR Check of historical Chinese, name: " + cnHistoryNameDoubleByte, "", "");
                    result.setOnError(true);
                    result
                        .setResults("Error on getting findCMR data when Duplicate CMR Check of historical Chinese, name: " + cnHistoryNameDoubleByte);
                  }

                }

                // output
                if (shouldBeRejected) {
                  result.setResults("Matches Found");
                  result.setResults("Found Duplicate CMRs.");
                  Collections.sort(matchedCMRs);
                  engineData.addRejectionComment("DUPC", "Customer already exists / duplicate CMR", StringUtils.join(matchedCMRs, ", "), "");
                  // to allow overides later
                  requestData.getAdmin().setMatchIndc("C");
                  result.setOnError(true);
                  result.setProcessOutput(output);
                  result.setDetails(details.toString().trim());
                  sendManagerEmail(entityManager, admin, data, soldTo, details);
                } else if (prospectCMRFlag && !convertPCMRFlag && !shouldBeRejected) { // CREATCMR-6302
                  result.setResults("Matches Found");
                  result.setResults("Found Duplicate Prospect CMRs only.");
                  pcmrs = pcmrs.substring(0, pcmrs.length() - 2);
                  Collections.sort(matchedCMRs);
                  engineData.addRejectionComment("DUPC",
                      "please import the existing Prospect CMR(" + pcmrs + ") into CreateCMR to convert into Legal CMR.",
                      StringUtils.join(matchedCMRs, ", "), "");
                  // to allow overrides later
                  requestData.getAdmin().setMatchIndc("C");
                  result.setOnError(true);
                  result.setProcessOutput(output);
                  result.setDetails(details.toString().trim());
                  // sendManagerEmail(entityManager, admin, data, soldTo,
                  // details);
                } else {
                  result.setDetails("No Duplicate CMRs were found.");
                  result.setResults("No Matches");
                  result.setOnError(false);
                }
              } else if (resultCNApi != null && !resultCNApi.isSuccess()) {
                result.setDetails("Error on getting China API Data when Duplicate CMR Check of Chinese - " + resultCNApi.getMessage());
                engineData.addRejectionComment("OTH",
                    "Error on getting China API Data when Duplicate CMR Check of Chinese - " + resultCNApi.getMessage(), "", "");
                result.setOnError(true);
                result.setResults("Error on getting China API Data when Duplicate CMR Check of Chinese - " + resultCNApi.getMessage());
              }

            } catch (Exception e) {
              e.printStackTrace();
              result.setDetails("Error on getting China API Data when Duplicate CMR Check of Chinese.");
              engineData.addRejectionComment("OTH", "Error on getting China API Data when Duplicate CMR Check of Chinese.", "", "");
              result.setOnError(true);
              result.setResults("Error on getting China API Data when Duplicate CMR Check of Chinese.");
            }
          } else {
            result.setDetails("Error on getting Chinese name when Duplicate CMR Check.");
            engineData.addRejectionComment("OTH", "Error on getting Chinese name when Duplicate CMR Check.", "", "");
            result.setOnError(true);
            result.setResults("Error on getting Chinese name when Duplicate CMR Check.");
          }

          break;

        case "KYND": // SCENARIO_LOCAL_KYND

          // logic:
          // a) Duplication means CMRs with same Chinese name or Historical
          // Chinese name.
          // b) If the existing cmr Search Term is "09058" then define as
          // duplication.
          // c) check China API with single byte string value
          // d) check findcmr twice with single byte and double byte value, if
          // they are not equals

          // CREATCMR-5461
          // check if should skip CNDupCMRCheck and set req status PPN
          skipAndPpn = getSkipAndPpnInd(entityManager, data);
          if (skipAndPpn) {
            log.debug("skip CN Dup CMR Check for req " + data.getId().getReqId());
            doSkipAndPpn(result, engineData);
            return result;
          }

          iAddr = handler.getIntlAddrById(soldTo, entityManager);
          if (iAddr != null) {

            try {

              // 1, Check CN API
              if (cnCreditCd != null && cnCreditCd.length() > 0) {
                searchModelCNAPI.setIssuingCntry(data.getCmrIssuingCntry());
                searchModelCNAPI.setCountryCd(soldTo.getLandCntry());
                searchModelCNAPI.setTaxCd1(cnCreditCd);
                resultCNApi = CompanyFinder.getCNApiInfo(searchModelCNAPI, "TAXCD");
                if (resultCNApi.getMessage() != null && resultCNApi.getMessage().contains("无数据")) {
                  boolean hasCNSpecialAttach = checkCNSpecialAttach(entityManager, data.getId().getReqId());
                  if (hasCNSpecialAttach) {
                    doSkipAndPpn4NoData(result, engineData);
                    return result;
                  }
                }
              } else {
                cnNameSingleByte = iAddr.getIntlCustNm1() + (!StringUtils.isBlank(iAddr.getIntlCustNm2()) ? (" " + iAddr.getIntlCustNm2()) : "");
                cnNameSingleByte = convert2SingleByte(cnNameSingleByte);
                searchModelCNAPI.setIssuingCntry(data.getCmrIssuingCntry());
                searchModelCNAPI.setCountryCd(soldTo.getLandCntry());
                searchModelCNAPI.setAltName(cnNameSingleByte);
                resultCNApi = CompanyFinder.getCNApiInfo(searchModelCNAPI, "ALTNAME");
              }

              if (resultCNApi != null && resultCNApi.isSuccess()) {
                cnNameSingleByte = resultCNApi.getRecord().getName();
                cnHistoryNameSingleByte = resultCNApi.getRecord().getHistoryNames();
                cnAddrSingleByte = resultCNApi.getRecord().getRegLocation();

                cnNameDoubleByte = convert2DoubleByte(cnNameSingleByte);
                cnHistoryNameDoubleByte = convert2DoubleByte(cnHistoryNameSingleByte);
                cnAddrDoubleByte = convert2DoubleByte(cnAddrSingleByte);

                try {
                  // 2, Check FindCMR NON Latin with Chinese name - single byte
                  CompanyRecordModel searchModelFindCmrCN = new CompanyRecordModel();
                  searchModelFindCmrCN.setIssuingCntry(data.getCmrIssuingCntry());
                  searchModelFindCmrCN.setCountryCd(soldTo.getLandCntry());
                  searchModelFindCmrCN.setName(cnNameSingleByte);
                  findCMRResult = searchFindCMR(searchModelFindCmrCN);

                  if (findCMRResult != null && findCMRResult.getItems() != null && !findCMRResult.getItems().isEmpty()) {
                    List<FindCMRRecordModel> cmrs = findCMRResult.getItems();
                    log.debug("There are " + cmrs.size() + " cmrs retrieved from FINDCMR.");
                    for (FindCMRRecordModel cmrsMods : cmrs) {

                      nameFindCmrCnResult = cmrsMods.getCmrIntlName1();
                      if (!StringUtils.isBlank(cmrsMods.getCmrIntlName2())) {
                        nameFindCmrCnResult = cmrsMods.getCmrIntlName1() + cmrsMods.getCmrIntlName2();
                      }

                      log.debug("CNAPI retrieved name is <" + cnNameSingleByte + "> after trim Chinese Space is <"
                          + trimChineseSpace(cnNameSingleByte) + ">");
                      log.debug("FINDCMR retrieved name is <" + nameFindCmrCnResult + "> after trim Chinese Space is <"
                          + trimChineseSpace(nameFindCmrCnResult) + ">");

                      if (nameFindCmrCnResult != null && cnNameSingleByte.equals(nameFindCmrCnResult)) {

                        kukla = getKukla(entityManager, cmrsMods.getCmrNum(), data.getCmrIssuingCntry());

                        log.debug("CNDupCMRCheckElement: request " + data.getId().getReqId() + ", CmrNo " + cmrsMods.getCmrNum() + ", ceid "
                            + cmrsMods.getCmrPpsceid() + ", search term " + cmrsMods.getCmrSortl() + ", kukla " + kukla);

                        if (cmrsMods.getCmrNum() != null && cmrsMods.getCmrNum().startsWith("P")) {
                          // CREATCMR-6302
                          if (StringUtils.isBlank(data.getCmrNo()) || !data.getCmrNo().equals(cmrsMods.getCmrNum())) {
                            log.debug("Found a duplicate prospect CMR.");
                            pcmrs += cmrsMods.getCmrNum() + ", ";
                            prospectCMRFlag = true;
                            handleLogDetails(cmrsMods, cmrData, matchedCMRs, details);
                          } else if (StringUtils.isNotBlank(data.getCmrNo()) && data.getCmrNo().equals(cmrsMods.getCmrNum())) {
                            convertPCMRFlag = true;
                            log.debug("Skip Duplicate CMR check for Convert to Legal CMR for prospect CmrNo " + cmrsMods.getCmrNum());
                          } else {
                            log.debug("Skip Duplicate CMR check for prospect CmrNo " + cmrsMods.getCmrNum());
                          }
                          continue;
                        }

                        nameMatched = true;

                        if ("09058".equals(cmrsMods.getCmrSortl())) {
                          // should be rejected
                          log.debug("Duplicate CMR. Request should be rejected.");
                          shouldBeRejected = true;
                          handleLogDetails(cmrsMods, cmrData, matchedCMRs, details);
                        } else {
                          // should not be rejected
                          log.debug("Not a duplicate CMR.");
                        }
                      }
                    }
                  }
                } catch (Exception e) {
                  e.printStackTrace();
                  result.setDetails("Error on checking findCMR on Duplicate CMR Check of Chinese, name: " + cnNameSingleByte);
                  engineData.addRejectionComment("OTH", "Error on checking findCMR on Duplicate CMR Check of Chinese, name: " + cnNameSingleByte, "",
                      "");
                  result.setOnError(true);
                  result.setResults("Error on checking findCMR on Duplicate CMR Check of Chinese, name: " + cnNameSingleByte);
                }

                if (cnNameDoubleByte != null && !cnNameDoubleByte.equals(cnNameSingleByte)) {
                  // 3, Check FindCMR NON Latin with Chinese name - double byte
                  try {
                    CompanyRecordModel searchModelFindCmrCN = new CompanyRecordModel();
                    searchModelFindCmrCN.setIssuingCntry(data.getCmrIssuingCntry());
                    searchModelFindCmrCN.setCountryCd(soldTo.getLandCntry());
                    searchModelFindCmrCN.setName(cnNameDoubleByte);
                    findCMRResult = searchFindCMR(searchModelFindCmrCN);

                    if (findCMRResult != null && findCMRResult.getItems() != null && !findCMRResult.getItems().isEmpty()) {
                      List<FindCMRRecordModel> cmrs = findCMRResult.getItems();
                      for (FindCMRRecordModel cmrsMods : cmrs) {

                        nameFindCmrCnResult = cmrsMods.getCmrIntlName1();
                        if (!StringUtils.isBlank(cmrsMods.getCmrIntlName2())) {
                          nameFindCmrCnResult = cmrsMods.getCmrIntlName1() + cmrsMods.getCmrIntlName2();
                        }

                        if (nameFindCmrCnResult != null && trimChineseSpace(cnNameDoubleByte).equals(trimChineseSpace(nameFindCmrCnResult))) {

                          kukla = getKukla(entityManager, cmrsMods.getCmrNum(), data.getCmrIssuingCntry());

                          log.debug("CNDupCMRCheckElement: request " + data.getId().getReqId() + ", CmrNo " + cmrsMods.getCmrNum() + ", ceid "
                              + cmrsMods.getCmrPpsceid() + ", search term " + cmrsMods.getCmrSortl() + ", kukla " + kukla);

                          if (cmrsMods.getCmrNum() != null && cmrsMods.getCmrNum().startsWith("P")) {
                            // CREATCMR-6302
                            // log.debug("Skip Duplicate CMR check for prospect
                            // CmrNo " + cmrsMods.getCmrNum());
                            // continue;
                            if (StringUtils.isBlank(data.getCmrNo()) || !data.getCmrNo().equals(cmrsMods.getCmrNum())) {
                              log.debug("Found a duplicate prospect CMR.");
                              pcmrs += cmrsMods.getCmrNum() + ", ";
                              prospectCMRFlag = true;
                              handleLogDetails(cmrsMods, cmrData, matchedCMRs, details);
                            } else if (StringUtils.isNotBlank(data.getCmrNo()) && data.getCmrNo().equals(cmrsMods.getCmrNum())) {
                              convertPCMRFlag = true;
                              log.debug("Skip Duplicate CMR check for Convert to Legal CMR for prospect CmrNo " + cmrsMods.getCmrNum());
                            } else {
                              log.debug("Skip Duplicate CMR check for prospect CmrNo " + cmrsMods.getCmrNum());
                            }
                            continue;

                          }

                          nameMatched = true;

                          if ("09058".equals(cmrsMods.getCmrSortl())) {
                            // should be rejected
                            log.debug("Duplicate CMR. Request should be rejected.");
                            shouldBeRejected = true;
                            handleLogDetails(cmrsMods, cmrData, matchedCMRs, details);
                          } else {
                            // should not be rejected
                            log.debug("Not a duplicate CMR.");
                          }

                        }
                      }
                    }
                  } catch (Exception e) {
                    e.printStackTrace();
                    result.setDetails("Error on checking findCMR on Duplicate CMR Check of Chinese, name: " + cnNameDoubleByte);
                    engineData.addRejectionComment("OTH", "Error on checking findCMR on Duplicate CMR Check of Chinese, name: " + cnNameDoubleByte,
                        "", "");
                    result.setOnError(true);
                    result.setResults("Error on checking findCMR on Duplicate CMR Check of Chinese, name: " + cnNameDoubleByte);
                  }
                }

                // 4, Check FindCMR Non Latin with historical Chinese name -
                // single byte
                if (cnHistoryNameSingleByte != null && !"".equals(cnHistoryNameSingleByte)) {
                  List<String> cnHistoryNameList = Arrays.asList(cnHistoryNameSingleByte.split(";"));
                  CompanyRecordModel searchModelFindCmrCN = new CompanyRecordModel();
                  searchModelFindCmrCN.setIssuingCntry(data.getCmrIssuingCntry());
                  try {
                    for (String historyName : cnHistoryNameList) {
                      searchModelFindCmrCN.setName(historyName);
                      findCMRResult = searchFindCMR(searchModelFindCmrCN);

                      if (findCMRResult != null && findCMRResult.getItems() != null && !findCMRResult.getItems().isEmpty()) {
                        List<FindCMRRecordModel> cmrs = findCMRResult.getItems();
                        for (FindCMRRecordModel cmrsMods : cmrs) {

                          nameFindCmrCnResult = cmrsMods.getCmrIntlName1();
                          if (!StringUtils.isBlank(cmrsMods.getCmrIntlName2())) {
                            nameFindCmrCnResult = cmrsMods.getCmrIntlName1() + cmrsMods.getCmrIntlName2();
                          }

                          if (nameFindCmrCnResult != null && trimChineseSpace(nameFindCmrCnResult).equals(trimChineseSpace(historyName))) {

                            kukla = getKukla(entityManager, cmrsMods.getCmrNum(), data.getCmrIssuingCntry());

                            log.debug("CNDupCMRCheckElement: request " + data.getId().getReqId() + ", CmrNo " + cmrsMods.getCmrNum() + ", ceid "
                                + cmrsMods.getCmrPpsceid() + ", search term " + cmrsMods.getCmrSortl() + ", kukla " + kukla);

                            if (cmrsMods.getCmrNum() != null && cmrsMods.getCmrNum().startsWith("P")) {
                              // CREATCMR-6302
                              // log.debug("Skip Duplicate CMR check for
                              // prospect CmrNo " + cmrsMods.getCmrNum());
                              // continue;
                              if (StringUtils.isBlank(data.getCmrNo()) || !data.getCmrNo().equals(cmrsMods.getCmrNum())) {
                                log.debug("Found a duplicate prospect CMR.");
                                pcmrs += cmrsMods.getCmrNum() + ", ";
                                prospectCMRFlag = true;
                                handleLogDetails(cmrsMods, cmrData, matchedCMRs, details);
                              } else if (StringUtils.isNotBlank(data.getCmrNo()) && data.getCmrNo().equals(cmrsMods.getCmrNum())) {
                                convertPCMRFlag = true;
                                log.debug("Skip Duplicate CMR check for Convert to Legal CMR for prospect CmrNo " + cmrsMods.getCmrNum());
                              } else {
                                log.debug("Skip Duplicate CMR check for prospect CmrNo " + cmrsMods.getCmrNum());
                              }
                              continue;

                            }

                            historyNmMatched = true;

                            if ("09058".equals(cmrsMods.getCmrSortl())) {
                              // should be rejected
                              log.debug("Duplicate CMR. Request should be rejected.");
                              shouldBeRejected = true;
                              handleLogDetails(cmrsMods, cmrData, matchedCMRs, details);
                            } else {
                              // should not be rejected
                              log.debug("Not a duplicate CMR.");
                            }

                          }
                        }
                      }
                    }

                  } catch (Exception e) {
                    e.printStackTrace();
                    result
                        .setDetails("Error on getting findCMR data when Duplicate CMR Check of historical Chinese, name: " + cnHistoryNameSingleByte);
                    engineData.addRejectionComment("OTH",
                        "Error on getting findCMR data when Duplicate CMR Check of historical Chinese, name: " + cnHistoryNameSingleByte, "", "");
                    result.setOnError(true);
                    result
                        .setResults("Error on getting findCMR data when Duplicate CMR Check of historical Chinese, name: " + cnHistoryNameSingleByte);
                  }

                }

                // 5, Check FindCMR Non Latin with historical Chinese name -
                // double byte
                if (cnHistoryNameDoubleByte != null && !"".equals(cnHistoryNameDoubleByte)
                    && !cnHistoryNameDoubleByte.equals(cnHistoryNameSingleByte)) {
                  List<String> cnHistoryNameList = Arrays.asList(cnHistoryNameDoubleByte.split(";"));
                  CompanyRecordModel searchModelFindCmrCN = new CompanyRecordModel();
                  searchModelFindCmrCN.setIssuingCntry(data.getCmrIssuingCntry());
                  try {
                    for (String historyName : cnHistoryNameList) {
                      searchModelFindCmrCN.setName(historyName);
                      findCMRResult = searchFindCMR(searchModelFindCmrCN);

                      if (findCMRResult != null && findCMRResult.getItems() != null && !findCMRResult.getItems().isEmpty()) {
                        List<FindCMRRecordModel> cmrs = findCMRResult.getItems();
                        for (FindCMRRecordModel cmrsMods : cmrs) {

                          nameFindCmrCnResult = cmrsMods.getCmrIntlName1();
                          if (!StringUtils.isBlank(cmrsMods.getCmrIntlName2())) {
                            nameFindCmrCnResult = cmrsMods.getCmrIntlName1() + cmrsMods.getCmrIntlName2();
                          }

                          if (nameFindCmrCnResult != null && trimChineseSpace(nameFindCmrCnResult).equals(trimChineseSpace(historyName))) {

                            kukla = getKukla(entityManager, cmrsMods.getCmrNum(), data.getCmrIssuingCntry());

                            log.debug("CNDupCMRCheckElement: request " + data.getId().getReqId() + ", CmrNo " + cmrsMods.getCmrNum() + ", ceid "
                                + cmrsMods.getCmrPpsceid() + ", search term " + cmrsMods.getCmrSortl() + ", kukla " + kukla);

                            if (cmrsMods.getCmrNum() != null && cmrsMods.getCmrNum().startsWith("P")) {
                              // CREATCMR-6302
                              // log.debug("Skip Duplicate CMR check for
                              // prospect CmrNo " + cmrsMods.getCmrNum());
                              // continue;
                              if (StringUtils.isBlank(data.getCmrNo()) || !data.getCmrNo().equals(cmrsMods.getCmrNum())) {
                                log.debug("Found a duplicate prospect CMR.");
                                pcmrs += cmrsMods.getCmrNum() + ", ";
                                prospectCMRFlag = true;
                                handleLogDetails(cmrsMods, cmrData, matchedCMRs, details);
                              } else if (StringUtils.isNotBlank(data.getCmrNo()) && data.getCmrNo().equals(cmrsMods.getCmrNum())) {
                                convertPCMRFlag = true;
                                log.debug("Skip Duplicate CMR check for Convert to Legal CMR for prospect CmrNo " + cmrsMods.getCmrNum());
                              } else {
                                log.debug("Skip Duplicate CMR check for prospect CmrNo " + cmrsMods.getCmrNum());
                              }
                              continue;

                            }

                            historyNmMatched = true;

                            if ("09058".equals(cmrsMods.getCmrSortl())) {
                              // should be rejected
                              log.debug("Duplicate CMR. Request should be rejected.");
                              shouldBeRejected = true;
                              handleLogDetails(cmrsMods, cmrData, matchedCMRs, details);
                            } else {
                              // should not be rejected
                              log.debug("Not a duplicate CMR.");
                            }

                          }
                        }
                      }
                    }

                  } catch (Exception e) {
                    e.printStackTrace();
                    result
                        .setDetails("Error on getting findCMR data when Duplicate CMR Check of historical Chinese, name: " + cnHistoryNameDoubleByte);
                    engineData.addRejectionComment("OTH",
                        "Error on getting findCMR data when Duplicate CMR Check of historical Chinese, name: " + cnHistoryNameDoubleByte, "", "");
                    result.setOnError(true);
                    result
                        .setResults("Error on getting findCMR data when Duplicate CMR Check of historical Chinese, name: " + cnHistoryNameDoubleByte);
                  }

                }

                // output
                if (shouldBeRejected) {
                  result.setResults("Matches Found");
                  result.setResults("Found Duplicate CMRs.");
                  Collections.sort(matchedCMRs);
                  engineData.addRejectionComment("DUPC", "Customer already exists / duplicate CMR", StringUtils.join(matchedCMRs, ", "), "");
                  // to allow overides later
                  requestData.getAdmin().setMatchIndc("C");
                  result.setOnError(true);
                  result.setProcessOutput(output);
                  result.setDetails(details.toString().trim());
                  sendManagerEmail(entityManager, admin, data, soldTo, details);
                } else if (prospectCMRFlag && !convertPCMRFlag && !shouldBeRejected) { // CREATCMR-6302
                  result.setResults("Matches Found");
                  result.setResults("Found Duplicate Prospect CMRs only.");
                  pcmrs = pcmrs.substring(0, pcmrs.length() - 2);
                  Collections.sort(matchedCMRs);
                  engineData.addRejectionComment("DUPC",
                      "please import the existing Prospect CMR(" + pcmrs + ") into CreateCMR to convert into Legal CMR.",
                      StringUtils.join(matchedCMRs, ", "), "");
                  // to allow overrides later
                  requestData.getAdmin().setMatchIndc("C");
                  result.setOnError(true);
                  result.setProcessOutput(output);
                  result.setDetails(details.toString().trim());
                  // sendManagerEmail(entityManager, admin, data, soldTo,
                  // details);
                } else {
                  result.setDetails("No Duplicate CMRs were found.");
                  result.setResults("No Matches");
                  result.setOnError(false);
                }
              } else if (resultCNApi != null && !resultCNApi.isSuccess()) {
                result.setDetails("Error on getting China API Data when Duplicate CMR Check of Chinese - " + resultCNApi.getMessage());
                engineData.addRejectionComment("OTH",
                    "Error on getting China API Data when Duplicate CMR Check of Chinese - " + resultCNApi.getMessage(), "", "");
                result.setOnError(true);
                result.setResults("Error on getting China API Data when Duplicate CMR Check of Chinese - " + resultCNApi.getMessage());
              }

            } catch (Exception e) {
              e.printStackTrace();
              result.setDetails("Error on getting China API Data when Duplicate CMR Check of Chinese.");
              engineData.addRejectionComment("OTH", "Error on getting China API Data when Duplicate CMR Check of Chinese.", "", "");
              result.setOnError(true);
              result.setResults("Error on getting China API Data when Duplicate CMR Check of Chinese.");
            }
          } else {
            result.setDetails("Error on getting Chinese name when Duplicate CMR Check.");
            engineData.addRejectionComment("OTH", "Error on getting Chinese name when Duplicate CMR Check.", "", "");
            result.setOnError(true);
            result.setResults("Error on getting Chinese name when Duplicate CMR Check.");
          }

          break;

        case "ECOSY": // SCENARIO_LOCAL_ECOSY

          // logic:
          // a) Duplication means CMRs with same Chinese name or historical
          // Chinese name
          // b) check China API with single byte string value
          // c) check findcmr twice with single byte and double byte value, if
          // they are not equals
          // d) check if findcmr result's Search Term is in a special list. yes
          // - dup.
          // e) If the existing CMR tag under Search term 04182, then auto
          // processing
          // f) if findcmr result's Search Term is "00000" and CEID has value -
          // not dup.
          // g) when findcmr result's Search Term is "00000" and CEID is empty,
          // if Kukla (classification code) in ('81','85') - not dup

          // CREATCMR-5461
          // check if should skip CNDupCMRCheck and set req status PPN
          skipAndPpn = getSkipAndPpnInd(entityManager, data);
          if (skipAndPpn) {
            log.debug("skip CN Dup CMR Check for req " + data.getId().getReqId());
            doSkipAndPpn(result, engineData);
            return result;
          }

          iAddr = handler.getIntlAddrById(soldTo, entityManager);
          if (iAddr != null) {

            try {

              // 1, Check CN API
              if (cnCreditCd != null && cnCreditCd.length() > 0) {
                searchModelCNAPI.setIssuingCntry(data.getCmrIssuingCntry());
                searchModelCNAPI.setCountryCd(soldTo.getLandCntry());
                searchModelCNAPI.setTaxCd1(cnCreditCd);
                resultCNApi = CompanyFinder.getCNApiInfo(searchModelCNAPI, "TAXCD");
                if (resultCNApi.getMessage() != null && resultCNApi.getMessage().contains("无数据")) {
                  boolean hasCNSpecialAttach = checkCNSpecialAttach(entityManager, data.getId().getReqId());
                  if (hasCNSpecialAttach) {
                    doSkipAndPpn4NoData(result, engineData);
                    return result;
                  }
                }
              } else {
                cnNameSingleByte = iAddr.getIntlCustNm1() + (!StringUtils.isBlank(iAddr.getIntlCustNm2()) ? (" " + iAddr.getIntlCustNm2()) : "");
                cnNameSingleByte = convert2SingleByte(cnNameSingleByte);
                searchModelCNAPI.setIssuingCntry(data.getCmrIssuingCntry());
                searchModelCNAPI.setCountryCd(soldTo.getLandCntry());
                searchModelCNAPI.setAltName(cnNameSingleByte);
                resultCNApi = CompanyFinder.getCNApiInfo(searchModelCNAPI, "ALTNAME");
              }

              if (resultCNApi != null && resultCNApi.isSuccess()) {
                cnNameSingleByte = resultCNApi.getRecord().getName();
                cnHistoryNameSingleByte = resultCNApi.getRecord().getHistoryNames();
                cnAddrSingleByte = resultCNApi.getRecord().getRegLocation();

                cnNameDoubleByte = convert2DoubleByte(cnNameSingleByte);
                cnHistoryNameDoubleByte = convert2DoubleByte(cnHistoryNameSingleByte);
                cnAddrDoubleByte = convert2DoubleByte(cnAddrSingleByte);

                boolean isSpecailSearchTerm = false;

                List<String> specialSearchTermList = getSpecialSearchTermList(entityManager, data.getCmrIssuingCntry());

                try {
                  // 2, Check FindCMR NON Latin with Chinese name - single byte
                  CompanyRecordModel searchModelFindCmrCN = new CompanyRecordModel();
                  searchModelFindCmrCN.setIssuingCntry(data.getCmrIssuingCntry());
                  searchModelFindCmrCN.setCountryCd(soldTo.getLandCntry());
                  searchModelFindCmrCN.setName(cnNameSingleByte);
                  findCMRResult = searchFindCMR(searchModelFindCmrCN);

                  if (findCMRResult != null && findCMRResult.getItems() != null && !findCMRResult.getItems().isEmpty()) {
                    List<FindCMRRecordModel> cmrs = findCMRResult.getItems();
                    log.debug("There are " + cmrs.size() + " cmrs retrieved from FINDCMR.");
                    for (FindCMRRecordModel cmrsMods : cmrs) {

                      nameFindCmrCnResult = cmrsMods.getCmrIntlName1();
                      if (!StringUtils.isBlank(cmrsMods.getCmrIntlName2())) {
                        nameFindCmrCnResult = cmrsMods.getCmrIntlName1() + cmrsMods.getCmrIntlName2();
                      }

                      log.debug("CNAPI retrieved name is <" + cnNameSingleByte + "> after trim Chinese Space is <"
                          + trimChineseSpace(cnNameSingleByte) + ">");
                      log.debug("FINDCMR retrieved name is <" + nameFindCmrCnResult + "> after trim Chinese Space is <"
                          + trimChineseSpace(nameFindCmrCnResult) + ">");

                      if (nameFindCmrCnResult != null && trimChineseSpace(cnNameSingleByte).equals(trimChineseSpace(nameFindCmrCnResult))) {

                        kukla = getKukla(entityManager, cmrsMods.getCmrNum(), data.getCmrIssuingCntry());

                        log.debug("CNDupCMRCheckElement: request " + data.getId().getReqId() + ", CmrNo " + cmrsMods.getCmrNum() + ", ceid "
                            + cmrsMods.getCmrPpsceid() + ", search term " + cmrsMods.getCmrSortl() + ", kukla " + kukla);

                        if (cmrsMods.getCmrNum() != null && cmrsMods.getCmrNum().startsWith("P")) {
                          // CREATCMR-6302
                          // log.debug("Skip Duplicate CMR check for
                          // prospect CmrNo " + cmrsMods.getCmrNum());
                          // continue;
                          if (StringUtils.isBlank(data.getCmrNo()) || !data.getCmrNo().equals(cmrsMods.getCmrNum())) {
                            log.debug("Found a duplicate prospect CMR.");
                            pcmrs += cmrsMods.getCmrNum() + ", ";
                            prospectCMRFlag = true;
                            handleLogDetails(cmrsMods, cmrData, matchedCMRs, details);
                          } else if (StringUtils.isNotBlank(data.getCmrNo()) && data.getCmrNo().equals(cmrsMods.getCmrNum())) {
                            convertPCMRFlag = true;
                            log.debug("Skip Duplicate CMR check for Convert to Legal CMR for prospect CmrNo " + cmrsMods.getCmrNum());
                          } else {
                            log.debug("Skip Duplicate CMR check for prospect CmrNo " + cmrsMods.getCmrNum());
                          }
                          continue;

                        }

                        nameMatched = true;

                        if (nameMatched) {

                          if (specialSearchTermList != null && specialSearchTermList.contains(cmrsMods.getCmrSortl())) {
                            isSpecailSearchTerm = true;
                            log.debug("Search Term is in special list.");
                            // should be rejected
                            log.debug("Duplicate CMR. Request should be rejected.");
                            shouldBeRejected = true;
                            handleLogDetails(cmrsMods, cmrData, matchedCMRs, details);
                          } else if (incloud(cmrsMods.getCmrSortl(), searchTerm04182)) {
                            // should not be rejected
                            log.debug("Not a duplicate CMR.");
                          } else if (cmrsMods.getCmrSortl() != null
                              && (cmrsMods.getCmrSortl().trim().equalsIgnoreCase("000000") || cmrsMods.getCmrSortl().trim().equalsIgnoreCase("00000")
                                  || cmrsMods.getCmrSortl().matches("[^0-9]+"))
                              && cmrsMods.getCmrNum() != null && (cmrsMods.getCmrNum().startsWith("1") || cmrsMods.getCmrNum().startsWith("2"))) {
                            if (cmrsMods.getCmrPpsceid() != null && cmrsMods.getCmrPpsceid().length() > 0) {
                              // should not be rejected
                              log.debug("Not a duplicate CMR.");
                            } else {
                              // Kukla (classification code) in ('81','85') -
                              // not dup
                              if ("81".equals(kukla) || "85".equals(kukla)) {
                                // should not be rejected
                                log.debug("Not a duplicate CMR.");
                              } else {
                                // should be rejected
                                log.debug("Duplicate CMR. Request should be rejected.");
                                shouldBeRejected = true;
                                handleLogDetails(cmrsMods, cmrData, matchedCMRs, details);
                              }
                            }
                          }

                        }
                      }
                    }
                  } else {
                    log.debug("There are 0 cmrs retrieved from FINDCMR.");
                  }
                } catch (Exception e) {
                  e.printStackTrace();
                  result.setDetails("Error on checking findCMR on Duplicate CMR Check of Chinese, name: " + cnNameSingleByte);
                  engineData.addRejectionComment("OTH", "Error on checking findCMR on Duplicate CMR Check of Chinese, name: " + cnNameSingleByte, "",
                      "");
                  result.setOnError(true);
                  result.setResults("Error on checking findCMR on Duplicate CMR Check of Chinese, name: " + cnNameSingleByte);
                }

                if (cnNameDoubleByte != null && !cnNameDoubleByte.equals(cnNameSingleByte)) {
                  // 3, Check FindCMR NON Latin with Chinese name - double byte
                  try {
                    CompanyRecordModel searchModelFindCmrCN = new CompanyRecordModel();
                    searchModelFindCmrCN.setIssuingCntry(data.getCmrIssuingCntry());
                    searchModelFindCmrCN.setCountryCd(soldTo.getLandCntry());
                    searchModelFindCmrCN.setName(cnNameDoubleByte);
                    findCMRResult = searchFindCMR(searchModelFindCmrCN);

                    if (findCMRResult != null && findCMRResult.getItems() != null && !findCMRResult.getItems().isEmpty()) {
                      List<FindCMRRecordModel> cmrs = findCMRResult.getItems();
                      for (FindCMRRecordModel cmrsMods : cmrs) {

                        nameFindCmrCnResult = cmrsMods.getCmrIntlName1();
                        if (!StringUtils.isBlank(cmrsMods.getCmrIntlName2())) {
                          nameFindCmrCnResult = cmrsMods.getCmrIntlName1() + cmrsMods.getCmrIntlName2();
                        }

                        if (nameFindCmrCnResult != null && trimChineseSpace(cnNameDoubleByte).equals(trimChineseSpace(nameFindCmrCnResult))) {

                          kukla = getKukla(entityManager, cmrsMods.getCmrNum(), data.getCmrIssuingCntry());

                          log.debug("CNDupCMRCheckElement: request " + data.getId().getReqId() + ", CmrNo " + cmrsMods.getCmrNum() + ", ceid "
                              + cmrsMods.getCmrPpsceid() + ", search term " + cmrsMods.getCmrSortl() + ", kukla " + kukla);

                          if (cmrsMods.getCmrNum() != null && cmrsMods.getCmrNum().startsWith("P")) {
                            // CREATCMR-6302
                            // log.debug("Skip Duplicate CMR check for
                            // prospect CmrNo " + cmrsMods.getCmrNum());
                            // continue;
                            if (StringUtils.isBlank(data.getCmrNo()) || !data.getCmrNo().equals(cmrsMods.getCmrNum())) {
                              log.debug("Found a duplicate prospect CMR.");
                              pcmrs += cmrsMods.getCmrNum() + ", ";
                              prospectCMRFlag = true;
                              handleLogDetails(cmrsMods, cmrData, matchedCMRs, details);
                            } else if (StringUtils.isNotBlank(data.getCmrNo()) && data.getCmrNo().equals(cmrsMods.getCmrNum())) {
                              convertPCMRFlag = true;
                              log.debug("Skip Duplicate CMR check for Convert to Legal CMR for prospect CmrNo " + cmrsMods.getCmrNum());
                            } else {
                              log.debug("Skip Duplicate CMR check for prospect CmrNo " + cmrsMods.getCmrNum());
                            }
                            continue;

                          }

                          nameMatched = true;

                          if (nameMatched) {

                            if (specialSearchTermList != null && specialSearchTermList.contains(cmrsMods.getCmrSortl())) {
                              isSpecailSearchTerm = true;
                              log.debug("Search Term is in special list.");
                              // should be rejected
                              log.debug("Duplicate CMR. Request should be rejected.");
                              shouldBeRejected = true;
                              handleLogDetails(cmrsMods, cmrData, matchedCMRs, details);
                            } else if (incloud(cmrsMods.getCmrSortl(), searchTerm04182)) {
                              // should not be rejected
                              log.debug("Not a duplicate CMR.");
                            } else if (cmrsMods.getCmrSortl() != null
                                && (cmrsMods.getCmrSortl().trim().equalsIgnoreCase("000000")
                                    || cmrsMods.getCmrSortl().trim().equalsIgnoreCase("00000") || cmrsMods.getCmrSortl().matches("[^0-9]+"))
                                && cmrsMods.getCmrNum() != null && (cmrsMods.getCmrNum().startsWith("1") || cmrsMods.getCmrNum().startsWith("2"))) {
                              if (cmrsMods.getCmrPpsceid() != null && cmrsMods.getCmrPpsceid().length() > 0) {
                                // should not be rejected
                                log.debug("Not a duplicate CMR.");
                              } else {
                                // Kukla (classification code) in ('81','85') -
                                // not dup
                                if ("81".equals(kukla) || "85".equals(kukla)) {
                                  // should not be rejected
                                  log.debug("Not a duplicate CMR.");
                                } else {
                                  // should be rejected
                                  log.debug("Duplicate CMR. Request should be rejected.");
                                  shouldBeRejected = true;
                                  handleLogDetails(cmrsMods, cmrData, matchedCMRs, details);
                                }
                              }
                            }

                          }
                        }
                      }
                    }
                  } catch (Exception e) {
                    e.printStackTrace();
                    result.setDetails("Error on checking findCMR on Duplicate CMR Check of Chinese, name: " + cnNameDoubleByte);
                    engineData.addRejectionComment("OTH", "Error on checking findCMR on Duplicate CMR Check of Chinese, name: " + cnNameDoubleByte,
                        "", "");
                    result.setOnError(true);
                    result.setResults("Error on checking findCMR on Duplicate CMR Check of Chinese, name: " + cnNameDoubleByte);
                  }
                }

                // 4, Check FindCMR Non Latin with historical Chinese name -
                // single byte
                if (cnHistoryNameSingleByte != null && !"".equals(cnHistoryNameSingleByte)) {
                  List<String> cnHistoryNameList = Arrays.asList(cnHistoryNameSingleByte.split(";"));
                  CompanyRecordModel searchModelFindCmrCN = new CompanyRecordModel();
                  searchModelFindCmrCN.setIssuingCntry(data.getCmrIssuingCntry());
                  try {
                    for (String historyName : cnHistoryNameList) {
                      searchModelFindCmrCN.setName(historyName);
                      findCMRResult = searchFindCMR(searchModelFindCmrCN);

                      if (findCMRResult != null && findCMRResult.getItems() != null && !findCMRResult.getItems().isEmpty()) {
                        List<FindCMRRecordModel> cmrs = findCMRResult.getItems();
                        for (FindCMRRecordModel cmrsMods : cmrs) {

                          nameFindCmrCnResult = cmrsMods.getCmrIntlName1();
                          if (!StringUtils.isBlank(cmrsMods.getCmrIntlName2())) {
                            nameFindCmrCnResult = cmrsMods.getCmrIntlName1() + cmrsMods.getCmrIntlName2();
                          }

                          if (nameFindCmrCnResult != null && trimChineseSpace(nameFindCmrCnResult).equals(trimChineseSpace(historyName))) {

                            kukla = getKukla(entityManager, cmrsMods.getCmrNum(), data.getCmrIssuingCntry());

                            log.debug("CNDupCMRCheckElement: request " + data.getId().getReqId() + ", CmrNo " + cmrsMods.getCmrNum() + ", ceid "
                                + cmrsMods.getCmrPpsceid() + ", search term " + cmrsMods.getCmrSortl() + ", kukla " + kukla);

                            if (cmrsMods.getCmrNum() != null && cmrsMods.getCmrNum().startsWith("P")) {
                              // CREATCMR-6302
                              // log.debug("Skip Duplicate CMR check for
                              // prospect CmrNo " + cmrsMods.getCmrNum());
                              // continue;
                              if (StringUtils.isBlank(data.getCmrNo()) || !data.getCmrNo().equals(cmrsMods.getCmrNum())) {
                                log.debug("Found a duplicate prospect CMR.");
                                pcmrs += cmrsMods.getCmrNum() + ", ";
                                prospectCMRFlag = true;
                                handleLogDetails(cmrsMods, cmrData, matchedCMRs, details);
                              } else if (StringUtils.isNotBlank(data.getCmrNo()) && data.getCmrNo().equals(cmrsMods.getCmrNum())) {
                                convertPCMRFlag = true;
                                log.debug("Skip Duplicate CMR check for Convert to Legal CMR for prospect CmrNo " + cmrsMods.getCmrNum());
                              } else {
                                log.debug("Skip Duplicate CMR check for prospect CmrNo " + cmrsMods.getCmrNum());
                              }
                              continue;

                            }

                            historyNmMatched = true;

                            if (historyNmMatched) {

                              if (specialSearchTermList != null && specialSearchTermList.contains(cmrsMods.getCmrSortl())) {
                                isSpecailSearchTerm = true;
                                log.debug("Search Term is in special list.");
                                // should be rejected
                                log.debug("Duplicate CMR. Request should be rejected.");
                                shouldBeRejected = true;
                                handleLogDetails(cmrsMods, cmrData, matchedCMRs, details);
                              } else if (incloud(cmrsMods.getCmrSortl(), searchTerm04182)) {
                                // should not be rejected
                                log.debug("Not a duplicate CMR.");
                              } else if (cmrsMods.getCmrSortl() != null
                                  && (cmrsMods.getCmrSortl().trim().equalsIgnoreCase("000000")
                                      || cmrsMods.getCmrSortl().trim().equalsIgnoreCase("00000") || cmrsMods.getCmrSortl().matches("[^0-9]+"))
                                  && cmrsMods.getCmrNum() != null && (cmrsMods.getCmrNum().startsWith("1") || cmrsMods.getCmrNum().startsWith("2"))) {
                                if (cmrsMods.getCmrPpsceid() != null && cmrsMods.getCmrPpsceid().length() > 0) {
                                  // should not be rejected
                                  log.debug("Not a duplicate CMR.");
                                } else {
                                  // Kukla (classification code) in ('81','85')
                                  // - not dup
                                  if ("81".equals(kukla) || "85".equals(kukla)) {
                                    // should not be rejected
                                    log.debug("Not a duplicate CMR.");
                                  } else {
                                    // should be rejected
                                    log.debug("Duplicate CMR. Request should be rejected.");
                                    shouldBeRejected = true;
                                    handleLogDetails(cmrsMods, cmrData, matchedCMRs, details);
                                  }
                                }
                              }

                            }
                          }
                        }
                      }
                    }
                  } catch (Exception e) {
                    e.printStackTrace();
                    result
                        .setDetails("Error on getting findCMR data when Duplicate CMR Check of historical Chinese, name: " + cnHistoryNameSingleByte);
                    engineData.addRejectionComment("OTH",
                        "Error on getting findCMR data when Duplicate CMR Check of historical Chinese, name: " + cnHistoryNameSingleByte, "", "");
                    result.setOnError(true);
                    result
                        .setResults("Error on getting findCMR data when Duplicate CMR Check of historical Chinese, name: " + cnHistoryNameSingleByte);
                  }

                }

                // 5, Check FindCMR Non Latin with historical Chinese name -
                // double byte
                if (cnHistoryNameDoubleByte != null && !"".equals(cnHistoryNameDoubleByte)
                    && !cnHistoryNameDoubleByte.equals(cnHistoryNameSingleByte)) {
                  List<String> cnHistoryNameList = Arrays.asList(cnHistoryNameDoubleByte.split(";"));
                  CompanyRecordModel searchModelFindCmrCN = new CompanyRecordModel();
                  searchModelFindCmrCN.setIssuingCntry(data.getCmrIssuingCntry());
                  try {
                    for (String historyName : cnHistoryNameList) {
                      searchModelFindCmrCN.setName(historyName);
                      findCMRResult = searchFindCMR(searchModelFindCmrCN);

                      if (findCMRResult != null && findCMRResult.getItems() != null && !findCMRResult.getItems().isEmpty()) {
                        List<FindCMRRecordModel> cmrs = findCMRResult.getItems();
                        for (FindCMRRecordModel cmrsMods : cmrs) {

                          nameFindCmrCnResult = cmrsMods.getCmrIntlName1();
                          if (!StringUtils.isBlank(cmrsMods.getCmrIntlName2())) {
                            nameFindCmrCnResult = cmrsMods.getCmrIntlName1() + cmrsMods.getCmrIntlName2();
                          }

                          if (nameFindCmrCnResult != null && trimChineseSpace(nameFindCmrCnResult).equals(trimChineseSpace(historyName))) {

                            kukla = getKukla(entityManager, cmrsMods.getCmrNum(), data.getCmrIssuingCntry());

                            log.debug("CNDupCMRCheckElement: request " + data.getId().getReqId() + ", CmrNo " + cmrsMods.getCmrNum() + ", ceid "
                                + cmrsMods.getCmrPpsceid() + ", search term " + cmrsMods.getCmrSortl() + ", kukla " + kukla);

                            if (cmrsMods.getCmrNum() != null && cmrsMods.getCmrNum().startsWith("P")) {
                              // CREATCMR-6302
                              // log.debug("Skip Duplicate CMR check for
                              // prospect CmrNo " + cmrsMods.getCmrNum());
                              // continue;
                              if (StringUtils.isBlank(data.getCmrNo()) || !data.getCmrNo().equals(cmrsMods.getCmrNum())) {
                                log.debug("Found a duplicate prospect CMR.");
                                pcmrs += cmrsMods.getCmrNum() + ", ";
                                prospectCMRFlag = true;
                                handleLogDetails(cmrsMods, cmrData, matchedCMRs, details);
                              } else if (StringUtils.isNotBlank(data.getCmrNo()) && data.getCmrNo().equals(cmrsMods.getCmrNum())) {
                                convertPCMRFlag = true;
                                log.debug("Skip Duplicate CMR check for Convert to Legal CMR for prospect CmrNo " + cmrsMods.getCmrNum());
                              } else {
                                log.debug("Skip Duplicate CMR check for prospect CmrNo " + cmrsMods.getCmrNum());
                              }
                              continue;

                            }

                            historyNmMatched = true;

                            if (historyNmMatched) {

                              if (specialSearchTermList != null && specialSearchTermList.contains(cmrsMods.getCmrSortl())) {
                                isSpecailSearchTerm = true;
                                log.debug("Search Term is in special list.");
                                // should be rejected
                                log.debug("Duplicate CMR. Request should be rejected.");
                                shouldBeRejected = true;
                                handleLogDetails(cmrsMods, cmrData, matchedCMRs, details);
                              } else if (incloud(cmrsMods.getCmrSortl(), searchTerm04182)) {
                                // should not be rejected
                                log.debug("Not a duplicate CMR.");
                              } else if (cmrsMods.getCmrSortl() != null
                                  && (cmrsMods.getCmrSortl().trim().equalsIgnoreCase("000000")
                                      || cmrsMods.getCmrSortl().trim().equalsIgnoreCase("00000") || cmrsMods.getCmrSortl().matches("[^0-9]+"))
                                  && cmrsMods.getCmrNum() != null && (cmrsMods.getCmrNum().startsWith("1") || cmrsMods.getCmrNum().startsWith("2"))) {
                                if (cmrsMods.getCmrPpsceid() != null && cmrsMods.getCmrPpsceid().length() > 0) {
                                  // should not be rejected
                                  log.debug("Not a duplicate CMR.");
                                } else {
                                  // Kukla (classification code) in ('81','85')
                                  // - not dup
                                  if ("81".equals(kukla) || "85".equals(kukla)) {
                                    // should not be rejected
                                  } else {
                                    // should be rejected
                                    log.debug("Duplicate CMR. Request should be rejected.");
                                    shouldBeRejected = true;
                                    handleLogDetails(cmrsMods, cmrData, matchedCMRs, details);
                                  }
                                }
                              }

                            }
                          }
                        }
                      }
                    }
                  } catch (Exception e) {
                    e.printStackTrace();
                    result
                        .setDetails("Error on getting findCMR data when Duplicate CMR Check of historical Chinese, name: " + cnHistoryNameDoubleByte);
                    engineData.addRejectionComment("OTH",
                        "Error on getting findCMR data when Duplicate CMR Check of historical Chinese, name: " + cnHistoryNameDoubleByte, "", "");
                    result.setOnError(true);
                    result
                        .setResults("Error on getting findCMR data when Duplicate CMR Check of historical Chinese, name: " + cnHistoryNameDoubleByte);
                  }

                }

                // output
                if (shouldBeRejected) {
                  result.setResults("Matches Found");
                  result.setResults("Found Duplicate CMRs.");
                  Collections.sort(matchedCMRs);
                  engineData.addRejectionComment("DUPC", "Customer already exists / duplicate CMR", StringUtils.join(matchedCMRs, ", "), "");
                  // to allow overides later
                  requestData.getAdmin().setMatchIndc("C");
                  result.setOnError(true);
                  result.setProcessOutput(output);
                  result.setDetails(details.toString().trim());
                  sendManagerEmail(entityManager, admin, data, soldTo, details);
                } else if (prospectCMRFlag && !convertPCMRFlag && !shouldBeRejected) { // CREATCMR-6302
                  result.setResults("Matches Found");
                  result.setResults("Found Duplicate Prospect CMRs only.");
                  pcmrs = pcmrs.substring(0, pcmrs.length() - 2);
                  Collections.sort(matchedCMRs);
                  engineData.addRejectionComment("DUPC",
                      "please import the existing Prospect CMR(" + pcmrs + ") into CreateCMR to convert into Legal CMR.",
                      StringUtils.join(matchedCMRs, ", "), "");
                  // to allow overrides later
                  requestData.getAdmin().setMatchIndc("C");
                  result.setOnError(true);
                  result.setProcessOutput(output);
                  result.setDetails(details.toString().trim());
                  // sendManagerEmail(entityManager, admin, data, soldTo,
                  // details);
                } else {
                  result.setDetails("No Duplicate CMRs were found.");
                  result.setResults("No Matches");
                  result.setOnError(false);
                }
              } else if (resultCNApi != null && !resultCNApi.isSuccess()) {
                result.setDetails("Error on getting China API Data when Duplicate CMR Check of Chinese - " + resultCNApi.getMessage());
                engineData.addRejectionComment("OTH",
                    "Error on getting China API Data when Duplicate CMR Check of Chinese - " + resultCNApi.getMessage(), "", "");
                result.setOnError(true);
                result.setResults("Error on getting China API Data when Duplicate CMR Check of Chinese - " + resultCNApi.getMessage());
              }

            } catch (Exception e) {
              e.printStackTrace();
              result.setDetails("Error on getting China API Data when Duplicate CMR Check of Chinese.");
              engineData.addRejectionComment("OTH", "Error on getting China API Data when Duplicate CMR Check of Chinese.", "", "");
              result.setOnError(true);
              result.setResults("Error on getting China API Data when Duplicate CMR Check of Chinese.");
            }
          } else {
            result.setDetails("Error on getting Chinese name when Duplicate CMR Check.");
            engineData.addRejectionComment("OTH", "Error on getting Chinese name when Duplicate CMR Check.", "", "");
            result.setOnError(true);
            result.setResults("Error on getting Chinese name when Duplicate CMR Check.");
          }

          break;

        case "BUSPR": // SCENARIO_LOCAL_BUSPR

          // logic:
          // a) Duplication means CMRs with same Chinese name or Historical
          // Chinese name,or same CEID.
          // b) If the existing CMR have same CEID and Search Term is "08036",do
          // not defined as Duplication.
          // c) If the existing CMR don't have CEID,then don't define it as
          // duplication.
          // d) check China API with single byte string value
          // e) check findcmr twice with single byte and double byte value, if
          // they are not equals

          // CREATCMR-5461
          // check if should skip CNDupCMRCheck and set req status PPN
          skipAndPpn = getSkipAndPpnInd(entityManager, data);
          if (skipAndPpn) {
            log.debug("skip CN Dup CMR Check for req " + data.getId().getReqId());
            doSkipAndPpn(result, engineData);
            return result;
          }

          iAddr = handler.getIntlAddrById(soldTo, entityManager);
          if (iAddr != null) {

            try {

              // 1, Check CN API
              if (cnCreditCd != null && cnCreditCd.length() > 0) {
                searchModelCNAPI.setIssuingCntry(data.getCmrIssuingCntry());
                searchModelCNAPI.setCountryCd(soldTo.getLandCntry());
                searchModelCNAPI.setTaxCd1(cnCreditCd);
                resultCNApi = CompanyFinder.getCNApiInfo(searchModelCNAPI, "TAXCD");
                if (resultCNApi.getMessage() != null && resultCNApi.getMessage().contains("无数据")) {
                  boolean hasCNSpecialAttach = checkCNSpecialAttach(entityManager, data.getId().getReqId());
                  if (hasCNSpecialAttach) {
                    doSkipAndPpn4NoData(result, engineData);
                    return result;
                  }
                }
              } else {
                cnNameSingleByte = iAddr.getIntlCustNm1() + (!StringUtils.isBlank(iAddr.getIntlCustNm2()) ? (" " + iAddr.getIntlCustNm2()) : "");
                cnNameSingleByte = convert2SingleByte(cnNameSingleByte);
                searchModelCNAPI.setIssuingCntry(data.getCmrIssuingCntry());
                searchModelCNAPI.setCountryCd(soldTo.getLandCntry());
                searchModelCNAPI.setAltName(cnNameSingleByte);
                resultCNApi = CompanyFinder.getCNApiInfo(searchModelCNAPI, "ALTNAME");
              }

              if (resultCNApi != null && resultCNApi.isSuccess()) {
                cnNameSingleByte = resultCNApi.getRecord().getName();
                cnHistoryNameSingleByte = resultCNApi.getRecord().getHistoryNames();
                cnAddrSingleByte = resultCNApi.getRecord().getRegLocation();

                cnNameDoubleByte = convert2DoubleByte(cnNameSingleByte);
                cnHistoryNameDoubleByte = convert2DoubleByte(cnHistoryNameSingleByte);
                cnAddrDoubleByte = convert2DoubleByte(cnAddrSingleByte);

                try {
                  // 2, Check FindCMR NON Latin with Chinese name - single byte
                  CompanyRecordModel searchModelFindCmrCN = new CompanyRecordModel();
                  searchModelFindCmrCN.setIssuingCntry(data.getCmrIssuingCntry());
                  searchModelFindCmrCN.setCountryCd(soldTo.getLandCntry());
                  searchModelFindCmrCN.setName(cnNameSingleByte);
                  findCMRResult = searchFindCMR(searchModelFindCmrCN);

                  if (findCMRResult != null && findCMRResult.getItems() != null && !findCMRResult.getItems().isEmpty()) {
                    List<FindCMRRecordModel> cmrs = findCMRResult.getItems();
                    log.debug("There are " + cmrs.size() + " cmrs retrieved from FINDCMR.");
                    for (FindCMRRecordModel cmrsMods : cmrs) {

                      nameFindCmrCnResult = cmrsMods.getCmrIntlName1();
                      if (!StringUtils.isBlank(cmrsMods.getCmrIntlName2())) {
                        nameFindCmrCnResult = cmrsMods.getCmrIntlName1() + cmrsMods.getCmrIntlName2();
                      }

                      log.debug("CNAPI retrieved name is <" + cnNameSingleByte + "> after trim Chinese Space is <"
                          + trimChineseSpace(cnNameSingleByte) + ">");
                      log.debug("FINDCMR retrieved name is <" + nameFindCmrCnResult + "> after trim Chinese Space is <"
                          + trimChineseSpace(nameFindCmrCnResult) + ">");

                      if (nameFindCmrCnResult != null && trimChineseSpace(cnNameSingleByte).equals(trimChineseSpace(nameFindCmrCnResult))) {

                        log.debug("CNDupCMRCheckElement: request " + data.getId().getReqId() + ", CmrNo " + cmrsMods.getCmrNum() + ", ceid "
                            + cmrsMods.getCmrPpsceid() + ", search term " + cmrsMods.getCmrSortl());

                        if (cmrsMods.getCmrNum() != null && cmrsMods.getCmrNum().startsWith("P")) {
                          log.debug("Skip Duplicate CMR check for prospect CmrNo " + cmrsMods.getCmrNum());
                          continue;
                        }

                        nameMatched = true;

                        if (cmrsMods.getCmrPpsceid() != null && StringUtils.isNotBlank(cmrsMods.getCmrPpsceid())) {
                          if (incloud(cmrsMods.getCmrSortl(), searchTerm08036)) {
                            // should not be rejected
                            log.debug("Not a duplicate CMR.");
                          } else if (incloud(cmrsMods.getCmrSortl(), searchTerm04182)
                              || (cmrsMods.getCmrSortl() == null || StringUtils.isBlank(cmrsMods.getCmrSortl())
                                  || (cmrsMods.getCmrSortl() != null && (cmrsMods.getCmrSortl().trim().equalsIgnoreCase("000000")
                                      || cmrsMods.getCmrSortl().trim().equalsIgnoreCase("00000") || cmrsMods.getCmrSortl().matches("[^0-9]+"))))
                                  && cmrsMods.getCmrNum() != null && (cmrsMods.getCmrNum().startsWith("0") || cmrsMods.getCmrNum().startsWith("1")
                                      || cmrsMods.getCmrNum().startsWith("2"))) {
                            // should be rejected
                            log.debug("Duplicate CMR. Request should be rejected.");
                            shouldBeRejected = true;
                            handleLogDetails(cmrsMods, cmrData, matchedCMRs, details);
                          } else {
                            // should not be rejected
                            log.debug("Not a duplicate CMR.");
                          }
                        } else {
                          if (incloud(cmrsMods.getCmrSortl(), searchTerm04182)
                              || (cmrsMods.getCmrSortl() == null || StringUtils.isBlank(cmrsMods.getCmrSortl())
                                  || (cmrsMods.getCmrSortl() != null && (cmrsMods.getCmrSortl().trim().equalsIgnoreCase("000000")
                                      || cmrsMods.getCmrSortl().trim().equalsIgnoreCase("00000") || cmrsMods.getCmrSortl().matches("[^0-9]+"))))
                                  && cmrsMods.getCmrNum() != null && (cmrsMods.getCmrNum().startsWith("1") || cmrsMods.getCmrNum().startsWith("2"))) {
                            // should be rejected
                            log.debug("Duplicate CMR. Request should be rejected.");
                            shouldBeRejected = true;
                            handleLogDetails(cmrsMods, cmrData, matchedCMRs, details);
                          } else {
                            // should not be rejected
                            log.debug("Not a duplicate CMR.");
                          }
                        }
                      }
                    }
                  }
                } catch (Exception e) {
                  e.printStackTrace();
                  result.setDetails("Error on checking findCMR on Duplicate CMR Check of Chinese, name: " + cnNameSingleByte);
                  engineData.addRejectionComment("OTH", "Error on checking findCMR on Duplicate CMR Check of Chinese, name: " + cnNameSingleByte, "",
                      "");
                  result.setOnError(true);
                  result.setResults("Error on checking findCMR on Duplicate CMR Check of Chinese, name: " + cnNameSingleByte);
                }

                if (cnNameDoubleByte != null && !cnNameDoubleByte.equals(cnNameSingleByte)) {
                  try {
                    // 3, Check FindCMR NON Latin with Chinese name - double
                    // byte
                    CompanyRecordModel searchModelFindCmrCN = new CompanyRecordModel();
                    searchModelFindCmrCN.setIssuingCntry(data.getCmrIssuingCntry());
                    searchModelFindCmrCN.setCountryCd(soldTo.getLandCntry());
                    searchModelFindCmrCN.setName(cnNameDoubleByte);
                    findCMRResult = searchFindCMR(searchModelFindCmrCN);

                    if (findCMRResult != null && findCMRResult.getItems() != null && !findCMRResult.getItems().isEmpty()) {
                      List<FindCMRRecordModel> cmrs = findCMRResult.getItems();
                      for (FindCMRRecordModel cmrsMods : cmrs) {

                        nameFindCmrCnResult = cmrsMods.getCmrIntlName1();
                        if (!StringUtils.isBlank(cmrsMods.getCmrIntlName2())) {
                          nameFindCmrCnResult = cmrsMods.getCmrIntlName1() + cmrsMods.getCmrIntlName2();
                        }

                        if (nameFindCmrCnResult != null && trimChineseSpace(cnNameDoubleByte).equals(trimChineseSpace(nameFindCmrCnResult))) {

                          log.debug("CNDupCMRCheckElement: request " + data.getId().getReqId() + ", CmrNo " + cmrsMods.getCmrNum() + ", ceid "
                              + cmrsMods.getCmrPpsceid() + ", search term " + cmrsMods.getCmrSortl());

                          if (cmrsMods.getCmrNum() != null && cmrsMods.getCmrNum().startsWith("P")) {
                            log.debug("Skip Duplicate CMR check for prospect CmrNo " + cmrsMods.getCmrNum());
                            continue;
                          }

                          nameMatched = true;

                          if (cmrsMods.getCmrPpsceid() != null && StringUtils.isNotBlank(cmrsMods.getCmrPpsceid())) {
                            if (incloud(cmrsMods.getCmrSortl(), searchTerm08036)) {
                              // should not be rejected
                              log.debug("Not a duplicate CMR.");
                            } else if (incloud(cmrsMods.getCmrSortl(), searchTerm04182)
                                || (cmrsMods.getCmrSortl() == null || StringUtils.isBlank(cmrsMods.getCmrSortl())
                                    || (cmrsMods.getCmrSortl() != null && (cmrsMods.getCmrSortl().trim().equalsIgnoreCase("000000")
                                        || cmrsMods.getCmrSortl().trim().equalsIgnoreCase("00000") || cmrsMods.getCmrSortl().matches("[^0-9]+"))))
                                    && cmrsMods.getCmrNum() != null && (cmrsMods.getCmrNum().startsWith("0") || cmrsMods.getCmrNum().startsWith("1")
                                        || cmrsMods.getCmrNum().startsWith("2"))) {
                              // should be rejected
                              log.debug("Duplicate CMR. Request should be rejected.");
                              shouldBeRejected = true;
                              handleLogDetails(cmrsMods, cmrData, matchedCMRs, details);
                            } else {
                              // should not be rejected
                              log.debug("Not a duplicate CMR.");
                            }
                          } else {
                            if (incloud(cmrsMods.getCmrSortl(), searchTerm04182)
                                || (cmrsMods.getCmrSortl() == null || StringUtils.isBlank(cmrsMods.getCmrSortl())
                                    || (cmrsMods.getCmrSortl() != null && (cmrsMods.getCmrSortl().trim().equalsIgnoreCase("000000")
                                        || cmrsMods.getCmrSortl().trim().equalsIgnoreCase("00000") || cmrsMods.getCmrSortl().matches("[^0-9]+"))))
                                    && cmrsMods.getCmrNum() != null
                                    && (cmrsMods.getCmrNum().startsWith("1") || cmrsMods.getCmrNum().startsWith("2"))) {
                              // should be rejected
                              log.debug("Duplicate CMR. Request should be rejected.");
                              shouldBeRejected = true;
                              handleLogDetails(cmrsMods, cmrData, matchedCMRs, details);
                            } else {
                              // should not be rejected
                              log.debug("Not a duplicate CMR.");
                            }
                          }

                        }
                      }
                    }
                  } catch (Exception e) {
                    e.printStackTrace();
                    result.setDetails("Error on checking findCMR on Duplicate CMR Check of Chinese, name: " + cnNameDoubleByte);
                    engineData.addRejectionComment("OTH", "Error on checking findCMR on Duplicate CMR Check of Chinese, name: " + cnNameDoubleByte,
                        "", "");
                    result.setOnError(true);
                    result.setResults("Error on checking findCMR on Duplicate CMR Check of Chinese, name: " + cnNameDoubleByte);
                  }
                }

                // 4, Check FindCMR Non Latin with historical Chinese name -
                // single byte
                if (cnHistoryNameSingleByte != null && !"".equals(cnHistoryNameSingleByte)) {
                  List<String> cnHistoryNameList = Arrays.asList(cnHistoryNameSingleByte.split(";"));
                  try {
                    CompanyRecordModel searchModelFindCmrCN = new CompanyRecordModel();
                    searchModelFindCmrCN.setIssuingCntry(data.getCmrIssuingCntry());
                    for (String historyName : cnHistoryNameList) {
                      searchModelFindCmrCN.setName(historyName);
                      findCMRResult = searchFindCMR(searchModelFindCmrCN);

                      if (findCMRResult != null && findCMRResult.getItems() != null && !findCMRResult.getItems().isEmpty()) {
                        List<FindCMRRecordModel> cmrs = findCMRResult.getItems();
                        for (FindCMRRecordModel cmrsMods : cmrs) {

                          nameFindCmrCnResult = cmrsMods.getCmrIntlName1();
                          if (!StringUtils.isBlank(cmrsMods.getCmrIntlName2())) {
                            nameFindCmrCnResult = cmrsMods.getCmrIntlName1() + cmrsMods.getCmrIntlName2();
                          }

                          if (nameFindCmrCnResult != null && trimChineseSpace(nameFindCmrCnResult).equals(trimChineseSpace(historyName))) {

                            log.debug("CNDupCMRCheckElement: request " + data.getId().getReqId() + ", CmrNo " + cmrsMods.getCmrNum() + ", ceid "
                                + cmrsMods.getCmrPpsceid() + ", search term " + cmrsMods.getCmrSortl());

                            if (cmrsMods.getCmrNum() != null && cmrsMods.getCmrNum().startsWith("P")) {
                              log.debug("Skip Duplicate CMR check for prospect CmrNo " + cmrsMods.getCmrNum());
                              continue;
                            }

                            historyNmMatched = true;

                            if (cmrsMods.getCmrPpsceid() != null && StringUtils.isNotBlank(cmrsMods.getCmrPpsceid())) {
                              if (incloud(cmrsMods.getCmrSortl(), searchTerm08036)) {
                                // should not be rejected
                                log.debug("Not a duplicate CMR.");
                              } else if (incloud(cmrsMods.getCmrSortl(), searchTerm04182)
                                  || (cmrsMods.getCmrSortl() == null || StringUtils.isBlank(cmrsMods.getCmrSortl())
                                      || (cmrsMods.getCmrSortl() != null && (cmrsMods.getCmrSortl().trim().equalsIgnoreCase("000000")
                                          || cmrsMods.getCmrSortl().trim().equalsIgnoreCase("00000") || cmrsMods.getCmrSortl().matches("[^0-9]+"))))
                                      && cmrsMods.getCmrNum() != null && (cmrsMods.getCmrNum().startsWith("0") || cmrsMods.getCmrNum().startsWith("1")
                                          || cmrsMods.getCmrNum().startsWith("2"))) {
                                // should be rejected
                                log.debug("Duplicate CMR. Request should be rejected.");
                                shouldBeRejected = true;
                                handleLogDetails(cmrsMods, cmrData, matchedCMRs, details);
                              } else {
                                // should not be rejected
                                log.debug("Not a duplicate CMR.");
                              }
                            } else {
                              if (incloud(cmrsMods.getCmrSortl(), searchTerm04182)
                                  || (cmrsMods.getCmrSortl() == null || StringUtils.isBlank(cmrsMods.getCmrSortl())
                                      || (cmrsMods.getCmrSortl() != null && (cmrsMods.getCmrSortl().trim().equalsIgnoreCase("000000")
                                          || cmrsMods.getCmrSortl().trim().equalsIgnoreCase("00000") || cmrsMods.getCmrSortl().matches("[^0-9]+"))))
                                      && cmrsMods.getCmrNum() != null
                                      && (cmrsMods.getCmrNum().startsWith("1") || cmrsMods.getCmrNum().startsWith("2"))) {
                                // should be rejected
                                log.debug("Duplicate CMR. Request should be rejected.");
                                shouldBeRejected = true;
                                handleLogDetails(cmrsMods, cmrData, matchedCMRs, details);
                              } else {
                                // should not be rejected
                                log.debug("Not a duplicate CMR.");
                              }
                            }

                          }
                        }
                      }
                    }

                  } catch (Exception e) {
                    e.printStackTrace();
                    result
                        .setDetails("Error on getting findCMR data when Duplicate CMR Check of historical Chinese, name: " + cnHistoryNameSingleByte);
                    engineData.addRejectionComment("OTH",
                        "Error on getting findCMR data when Duplicate CMR Check of historical Chinese, name: " + cnHistoryNameSingleByte, "", "");
                    result.setOnError(true);
                    result
                        .setResults("Error on getting findCMR data when Duplicate CMR Check of historical Chinese, name: " + cnHistoryNameSingleByte);
                  }

                }

                // 5, Check FindCMR Non Latin with historical Chinese name -
                // double byte
                if (cnHistoryNameDoubleByte != null && !"".equals(cnHistoryNameDoubleByte)
                    && !cnHistoryNameDoubleByte.equals(cnHistoryNameSingleByte)) {
                  List<String> cnHistoryNameList = Arrays.asList(cnHistoryNameDoubleByte.split(";"));
                  try {
                    CompanyRecordModel searchModelFindCmrCN = new CompanyRecordModel();
                    searchModelFindCmrCN.setIssuingCntry(data.getCmrIssuingCntry());
                    for (String historyName : cnHistoryNameList) {
                      searchModelFindCmrCN.setName(historyName);
                      findCMRResult = searchFindCMR(searchModelFindCmrCN);

                      if (findCMRResult != null && findCMRResult.getItems() != null && !findCMRResult.getItems().isEmpty()) {
                        List<FindCMRRecordModel> cmrs = findCMRResult.getItems();
                        for (FindCMRRecordModel cmrsMods : cmrs) {

                          nameFindCmrCnResult = cmrsMods.getCmrIntlName1();
                          if (!StringUtils.isBlank(cmrsMods.getCmrIntlName2())) {
                            nameFindCmrCnResult = cmrsMods.getCmrIntlName1() + cmrsMods.getCmrIntlName2();
                          }

                          if (nameFindCmrCnResult != null && trimChineseSpace(nameFindCmrCnResult).equals(trimChineseSpace(historyName))) {

                            log.debug("CNDupCMRCheckElement: request " + data.getId().getReqId() + ", CmrNo " + cmrsMods.getCmrNum() + ", ceid "
                                + cmrsMods.getCmrPpsceid() + ", search term " + cmrsMods.getCmrSortl());

                            if (cmrsMods.getCmrNum() != null && cmrsMods.getCmrNum().startsWith("P")) {
                              log.debug("Skip Duplicate CMR check for prospect CmrNo " + cmrsMods.getCmrNum());
                              continue;
                            }

                            historyNmMatched = true;

                            if (cmrsMods.getCmrPpsceid() != null && StringUtils.isNotBlank(cmrsMods.getCmrPpsceid())) {
                              if (incloud(cmrsMods.getCmrSortl(), searchTerm08036)) {
                                // should not be rejected
                                log.debug("Not a duplicate CMR.");
                              } else if (incloud(cmrsMods.getCmrSortl(), searchTerm04182)
                                  || (cmrsMods.getCmrSortl() == null || StringUtils.isBlank(cmrsMods.getCmrSortl())
                                      || (cmrsMods.getCmrSortl() != null && (cmrsMods.getCmrSortl().trim().equalsIgnoreCase("000000")
                                          || cmrsMods.getCmrSortl().trim().equalsIgnoreCase("00000") || cmrsMods.getCmrSortl().matches("[^0-9]+"))))
                                      && cmrsMods.getCmrNum() != null && (cmrsMods.getCmrNum().startsWith("0") || cmrsMods.getCmrNum().startsWith("1")
                                          || cmrsMods.getCmrNum().startsWith("2"))) {
                                // should be rejected
                                log.debug("Duplicate CMR. Request should be rejected.");
                                shouldBeRejected = true;
                                handleLogDetails(cmrsMods, cmrData, matchedCMRs, details);
                              } else {
                                // should not be rejected
                                log.debug("Not a duplicate CMR.");
                              }
                            } else {
                              if (incloud(cmrsMods.getCmrSortl(), searchTerm04182)
                                  || (cmrsMods.getCmrSortl() == null || StringUtils.isBlank(cmrsMods.getCmrSortl())
                                      || (cmrsMods.getCmrSortl() != null && (cmrsMods.getCmrSortl().trim().equalsIgnoreCase("000000")
                                          || cmrsMods.getCmrSortl().trim().equalsIgnoreCase("00000") || cmrsMods.getCmrSortl().matches("[^0-9]+"))))
                                      && cmrsMods.getCmrNum() != null
                                      && (cmrsMods.getCmrNum().startsWith("1") || cmrsMods.getCmrNum().startsWith("2"))) {
                                // should be rejected
                                log.debug("Duplicate CMR. Request should be rejected.");
                                shouldBeRejected = true;
                                handleLogDetails(cmrsMods, cmrData, matchedCMRs, details);
                              } else {
                                // should not be rejected
                                log.debug("Not a duplicate CMR.");
                              }
                            }

                          }
                        }
                      }
                    }

                  } catch (Exception e) {
                    e.printStackTrace();
                    result
                        .setDetails("Error on getting findCMR data when Duplicate CMR Check of historical Chinese, name: " + cnHistoryNameDoubleByte);
                    engineData.addRejectionComment("OTH",
                        "Error on getting findCMR data when Duplicate CMR Check of historical Chinese, name: " + cnHistoryNameDoubleByte, "", "");
                    result.setOnError(true);
                    result
                        .setResults("Error on getting findCMR data when Duplicate CMR Check of historical Chinese, name: " + cnHistoryNameDoubleByte);
                  }

                }

                if (cnCeid != null && !"".equals(cnCeid)) {
                  try {
                    // 6, Check FindCMR with CEID
                    CompanyRecordModel searchModelFindCmrCN = new CompanyRecordModel();
                    searchModelFindCmrCN.setIssuingCntry(data.getCmrIssuingCntry());
                    searchModelFindCmrCN.setCied(cnCeid);
                    findCMRResult = searchFindCMR(searchModelFindCmrCN);
                    if (findCMRResult != null && findCMRResult.getItems() != null && !findCMRResult.getItems().isEmpty()) {
                      ceidMatched = true;
                      List<FindCMRRecordModel> cmrs = findCMRResult.getItems();
                      for (FindCMRRecordModel cmrsMods : cmrs) {

                        log.debug("CNDupCMRCheckElement: request " + data.getId().getReqId() + ", CmrNo " + cmrsMods.getCmrNum() + ", ceid "
                            + cmrsMods.getCmrPpsceid() + ", search term " + cmrsMods.getCmrSortl());

                        if (cmrsMods.getCmrNum() != null && cmrsMods.getCmrNum().startsWith("P")) {
                          log.debug("Skip Duplicate CMR check for prospect CmrNo " + cmrsMods.getCmrNum());
                          continue;
                        }

                        if (incloud(cmrsMods.getCmrSortl(), searchTerm08036)) {
                          // should not be rejected
                          log.debug("Not a duplicate CMR.");
                        } else if (incloud(cmrsMods.getCmrSortl(), searchTerm04182)
                            || (cmrsMods.getCmrSortl() == null || StringUtils.isBlank(cmrsMods.getCmrSortl())
                                || (cmrsMods.getCmrSortl() != null && (cmrsMods.getCmrSortl().trim().equalsIgnoreCase("000000")
                                    || cmrsMods.getCmrSortl().trim().equalsIgnoreCase("00000") || cmrsMods.getCmrSortl().matches("[^0-9]+"))))
                                && cmrsMods.getCmrNum() != null && (cmrsMods.getCmrNum().startsWith("0") || cmrsMods.getCmrNum().startsWith("1")
                                    || cmrsMods.getCmrNum().startsWith("2"))) {
                          // should be rejected
                          log.debug("Duplicate CMR. Request should be rejected.");
                          shouldBeRejected = true;
                          handleLogDetails(cmrsMods, cmrData, matchedCMRs, details);
                        } else {
                          // should not be rejected
                          log.debug("Not a duplicate CMR.");
                        }
                      }

                    }
                  } catch (Exception e) {
                    e.printStackTrace();
                    result.setDetails("Error on checking findCMR on Duplicate CMR Check of Chinese, ceid: " + cnCeid);
                    engineData.addRejectionComment("OTH", "Error on checking findCMR on Duplicate CMR Check of Chinese, ceid: " + cnCeid, "", "");
                    result.setOnError(true);
                    result.setResults("Error on checking findCMR on Duplicate CMR Check of Chinese, ceid: " + cnCeid);
                  }
                }

                // output
                if (shouldBeRejected) {
                  result.setResults("Matches Found");
                  result.setResults("Found Duplicate CMRs.");
                  engineData.addRejectionComment("DUPC", "Customer already exists / duplicate CMR", StringUtils.join(matchedCMRs, ", "), "");
                  // to allow overides later
                  requestData.getAdmin().setMatchIndc("C");
                  result.setOnError(true);
                  result.setProcessOutput(output);
                  result.setDetails(details.toString().trim());
                  sendManagerEmail(entityManager, admin, data, soldTo, details);
                } else {
                  result.setDetails("No Duplicate CMRs were found.");
                  result.setResults("No Matches");
                  result.setOnError(false);
                }
              } else if (resultCNApi != null && !resultCNApi.isSuccess()) {
                result.setDetails("Error on getting China API Data when Duplicate CMR Check of Chinese - " + resultCNApi.getMessage());
                engineData.addRejectionComment("OTH",
                    "Error on getting China API Data when Duplicate CMR Check of Chinese - " + resultCNApi.getMessage(), "", "");
                result.setOnError(true);
                result.setResults("Error on getting China API Data when Duplicate CMR Check of Chinese - " + resultCNApi.getMessage());
              }

            } catch (Exception e) {
              e.printStackTrace();
              result.setDetails("Error on getting China API Data when Duplicate CMR Check of Chinese.");
              engineData.addRejectionComment("OTH", "Error on getting China API Data when Duplicate CMR Check of Chinese.", "", "");
              result.setOnError(true);
              result.setResults("Error on getting China API Data when Duplicate CMR Check of Chinese.");
            }
          } else {
            result.setDetails("Error on getting Chinese name when Duplicate CMR Check.");
            engineData.addRejectionComment("OTH", "Error on getting Chinese name when Duplicate CMR Check.", "", "");
            result.setOnError(true);
            result.setResults("Error on getting Chinese name when Duplicate CMR Check.");
          }
          break;

        case "EMBSA": // SCENARIO_LOCAL_EMBSA

          // logic:
          // a) Duplication means CMRs with same Chinese name or Historical
          // Chinese name.
          // b) If the existing CMR have CEID,and Search Term not as "08036"
          // don't define as duplication.
          // c) If the existing CMR don't have CEID and Search Term is
          // "08036",then defined as Duplication.
          // d) If the existing CMR don't have CEID and Search Term not as
          // "08036",and Kukla not in ('81','85','43','44','45','46') .then
          // defined as Duplication.
          // e) If the existing CMR don't have CEID and Search Term not as
          // "08036",and Kukla in ('81','85','43','44','45','46') .then don't
          // defined as Duplication.
          // f) check China API with single byte string value
          // g) check findcmr twice with single byte and double byte value, if
          // they are not equals
          // h) fastpass - TBD
          // i) scenario ESA would not reject dupliate cmr, just send to CMDE.

          // CREATCMR-5461
          // check if should skip CNDupCMRCheck and set req status PPN
          skipAndPpn = getSkipAndPpnInd(entityManager, data);
          if (skipAndPpn) {
            log.debug("skip CN Dup CMR Check for req " + data.getId().getReqId());
            doSkipAndPpn(result, engineData);
            return result;
          }

          iAddr = handler.getIntlAddrById(soldTo, entityManager);
          // searchModelFindCMR.setCmrNo(cmrNo);
          if (iAddr != null) {

            try {

              // 1, Check CN API
              if (cnCreditCd != null && cnCreditCd.length() > 0) {
                searchModelCNAPI.setIssuingCntry(data.getCmrIssuingCntry());
                searchModelCNAPI.setCountryCd(soldTo.getLandCntry());
                searchModelCNAPI.setTaxCd1(cnCreditCd);
                resultCNApi = CompanyFinder.getCNApiInfo(searchModelCNAPI, "TAXCD");
                if (resultCNApi.getMessage() != null && resultCNApi.getMessage().contains("无数据")) {
                  boolean hasCNSpecialAttach = checkCNSpecialAttach(entityManager, data.getId().getReqId());
                  if (hasCNSpecialAttach) {
                    doSkipAndPpn4NoData(result, engineData);
                    return result;
                  }
                }
              } else {
                cnNameSingleByte = iAddr.getIntlCustNm1() + (!StringUtils.isBlank(iAddr.getIntlCustNm2()) ? (" " + iAddr.getIntlCustNm2()) : "");
                cnNameSingleByte = convert2SingleByte(cnNameSingleByte);
                searchModelCNAPI.setIssuingCntry(data.getCmrIssuingCntry());
                searchModelCNAPI.setCountryCd(soldTo.getLandCntry());
                searchModelCNAPI.setAltName(cnNameSingleByte);
                resultCNApi = CompanyFinder.getCNApiInfo(searchModelCNAPI, "ALTNAME");
              }

              if (resultCNApi != null && resultCNApi.isSuccess()) {
                cnNameSingleByte = resultCNApi.getRecord().getName();
                cnHistoryNameSingleByte = resultCNApi.getRecord().getHistoryNames();
                cnAddrSingleByte = resultCNApi.getRecord().getRegLocation();

                cnNameDoubleByte = convert2DoubleByte(cnNameSingleByte);
                cnHistoryNameDoubleByte = convert2DoubleByte(cnHistoryNameSingleByte);
                cnAddrDoubleByte = convert2DoubleByte(cnAddrSingleByte);

                try {
                  // 2, Check FindCMR NON Latin with Chinese name - single byte
                  CompanyRecordModel searchModelFindCmrCN = new CompanyRecordModel();
                  searchModelFindCmrCN.setIssuingCntry(data.getCmrIssuingCntry());
                  searchModelFindCmrCN.setCountryCd(soldTo.getLandCntry());
                  searchModelFindCmrCN.setName(cnNameSingleByte);
                  findCMRResult = searchFindCMR(searchModelFindCmrCN);

                  if (findCMRResult != null && findCMRResult.getItems() != null && !findCMRResult.getItems().isEmpty()) {
                    List<FindCMRRecordModel> cmrs = findCMRResult.getItems();
                    log.debug("There are " + cmrs.size() + " cmrs retrieved from FINDCMR.");
                    for (FindCMRRecordModel cmrsMods : cmrs) {

                      nameFindCmrCnResult = cmrsMods.getCmrIntlName1();
                      if (!StringUtils.isBlank(cmrsMods.getCmrIntlName2())) {
                        nameFindCmrCnResult = cmrsMods.getCmrIntlName1() + cmrsMods.getCmrIntlName2();
                      }

                      log.debug("CNAPI retrieved name is <" + cnNameSingleByte + "> after trim Chinese Space is <"
                          + trimChineseSpace(cnNameSingleByte) + ">");
                      log.debug("FINDCMR retrieved name is <" + nameFindCmrCnResult + "> after trim Chinese Space is <"
                          + trimChineseSpace(nameFindCmrCnResult) + ">");

                      if (nameFindCmrCnResult != null && trimChineseSpace(cnNameSingleByte).equals(trimChineseSpace(nameFindCmrCnResult))) {

                        kukla = getKukla(entityManager, cmrsMods.getCmrNum(), data.getCmrIssuingCntry());

                        log.debug("CNDupCMRCheckElement: request " + data.getId().getReqId() + ", CmrNo " + cmrsMods.getCmrNum() + ", ceid "
                            + cmrsMods.getCmrPpsceid() + ", search term " + cmrsMods.getCmrSortl() + ", kukla " + kukla);

                        if (cmrsMods.getCmrNum() != null && cmrsMods.getCmrNum().startsWith("P")) {
                          // CREATCMR-6302
                          // log.debug("Skip Duplicate CMR check for
                          // prospect CmrNo " + cmrsMods.getCmrNum());
                          // continue;
                          if (StringUtils.isBlank(data.getCmrNo()) || !data.getCmrNo().equals(cmrsMods.getCmrNum())) {
                            log.debug("Found a duplicate prospect CMR.");
                            pcmrs += cmrsMods.getCmrNum() + ", ";
                            prospectCMRFlag = true;
                            handleLogDetails(cmrsMods, cmrData, matchedCMRs, details);
                          } else if (StringUtils.isNotBlank(data.getCmrNo()) && data.getCmrNo().equals(cmrsMods.getCmrNum())) {
                            convertPCMRFlag = true;
                            log.debug("Skip Duplicate CMR check for Convert to Legal CMR for prospect CmrNo " + cmrsMods.getCmrNum());
                          } else {
                            log.debug("Skip Duplicate CMR check for prospect CmrNo " + cmrsMods.getCmrNum());
                          }
                          continue;

                        }

                        nameMatched = true;

                        if (cmrsMods.getCmrPpsceid() == null || StringUtils.isBlank(cmrsMods.getCmrPpsceid())) {
                          if (incloud(cmrsMods.getCmrSortl(), searchTerm04182) || "00462".equals(cmrsMods.getCmrSortl())
                              || (cmrsMods.getCmrSortl() == null || StringUtils.isBlank(cmrsMods.getCmrSortl())
                                  || (cmrsMods.getCmrSortl() != null && (cmrsMods.getCmrSortl().trim().equalsIgnoreCase("000000")
                                      || cmrsMods.getCmrSortl().trim().equalsIgnoreCase("00000") || cmrsMods.getCmrSortl().matches("[^0-9]+"))))) {
                            if (cmrsMods.getCmrNum() != null && (cmrsMods.getCmrNum().startsWith("0") || cmrsMods.getCmrNum().startsWith("1")
                                || cmrsMods.getCmrNum().startsWith("2") || cmrsMods.getCmrNum().startsWith("9"))) {
                              // should not be rejected
                              log.debug("Not a duplicate CMR.");
                            } else {
                              // should be rejected
                              log.debug("Duplicate CMR. Request should be rejected.");
                              shouldBeRejected = true;
                              handleLogDetails(cmrsMods, cmrData, matchedCMRs, details);
                            }
                          } else {
                            // should be rejected
                            log.debug("Duplicate CMR. Request should be rejected.");
                            shouldBeRejected = true;
                            handleLogDetails(cmrsMods, cmrData, matchedCMRs, details);
                          }
                        } else {
                          if (incloud(cmrsMods.getCmrSortl(), searchTerm04182)
                              || (cmrsMods.getCmrSortl() == null || StringUtils.isBlank(cmrsMods.getCmrSortl())
                                  || (cmrsMods.getCmrSortl() != null && (cmrsMods.getCmrSortl().trim().equalsIgnoreCase("000000")
                                      || cmrsMods.getCmrSortl().trim().equalsIgnoreCase("00000") || cmrsMods.getCmrSortl().matches("[^0-9]+"))))
                                  && cmrsMods.getCmrNum() != null && (cmrsMods.getCmrNum().startsWith("0") || cmrsMods.getCmrNum().startsWith("1")
                                      || cmrsMods.getCmrNum().startsWith("2"))) {
                            // should not be rejected
                            log.debug("Not a duplicate CMR.");
                          } else {
                            if (!incloud(cmrsMods.getCmrSortl(), searchTerm08036) && incloud(kukla, cnSpecialKukla)) {
                              // should not be rejected
                              log.debug("Not a duplicate CMR.");
                            } else {
                              // should be rejected
                              log.debug("Duplicate CMR. Request should be rejected.");
                              shouldBeRejected = true;
                              handleLogDetails(cmrsMods, cmrData, matchedCMRs, details);
                            }
                          }
                        }

                      }
                    }
                  }
                } catch (Exception e) {
                  e.printStackTrace();
                  result.setDetails("Error on checking findCMR on Duplicate CMR Check of Chinese, name: " + cnNameSingleByte);
                  engineData.addRejectionComment("OTH", "Error on checking findCMR on Duplicate CMR Check of Chinese, name: " + cnNameSingleByte, "",
                      "");
                  result.setOnError(true);
                  result.setResults("Error on checking findCMR on Duplicate CMR Check of Chinese, name: " + cnNameSingleByte);
                }

                if (cnNameDoubleByte != null && !cnNameDoubleByte.equals(cnNameSingleByte)) {
                  // 3, Check FindCMR NON Latin with Chinese name - double byte
                  try {
                    CompanyRecordModel searchModelFindCmrCN = new CompanyRecordModel();
                    searchModelFindCmrCN.setIssuingCntry(data.getCmrIssuingCntry());
                    searchModelFindCmrCN.setCountryCd(soldTo.getLandCntry());
                    searchModelFindCmrCN.setName(cnNameDoubleByte);
                    findCMRResult = searchFindCMR(searchModelFindCmrCN);

                    if (findCMRResult != null && findCMRResult.getItems() != null && !findCMRResult.getItems().isEmpty()) {
                      List<FindCMRRecordModel> cmrs = findCMRResult.getItems();
                      for (FindCMRRecordModel cmrsMods : cmrs) {

                        nameFindCmrCnResult = cmrsMods.getCmrIntlName1();
                        if (!StringUtils.isBlank(cmrsMods.getCmrIntlName2())) {
                          nameFindCmrCnResult = cmrsMods.getCmrIntlName1() + cmrsMods.getCmrIntlName2();
                        }

                        if (nameFindCmrCnResult != null && trimChineseSpace(cnNameDoubleByte).equals(trimChineseSpace(nameFindCmrCnResult))) {

                          kukla = getKukla(entityManager, cmrsMods.getCmrNum(), data.getCmrIssuingCntry());

                          log.debug("CNDupCMRCheckElement: request " + data.getId().getReqId() + ", CmrNo " + cmrsMods.getCmrNum() + ", ceid "
                              + cmrsMods.getCmrPpsceid() + ", search term " + cmrsMods.getCmrSortl() + ", kukla " + kukla);

                          if (cmrsMods.getCmrNum() != null && cmrsMods.getCmrNum().startsWith("P")) {
                            // CREATCMR-6302
                            // log.debug("Skip Duplicate CMR check for
                            // prospect CmrNo " + cmrsMods.getCmrNum());
                            // continue;
                            if (StringUtils.isBlank(data.getCmrNo()) || !data.getCmrNo().equals(cmrsMods.getCmrNum())) {
                              log.debug("Found a duplicate prospect CMR.");
                              pcmrs += cmrsMods.getCmrNum() + ", ";
                              prospectCMRFlag = true;
                              handleLogDetails(cmrsMods, cmrData, matchedCMRs, details);
                            } else if (StringUtils.isNotBlank(data.getCmrNo()) && data.getCmrNo().equals(cmrsMods.getCmrNum())) {
                              convertPCMRFlag = true;
                              log.debug("Skip Duplicate CMR check for Convert to Legal CMR for prospect CmrNo " + cmrsMods.getCmrNum());
                            } else {
                              log.debug("Skip Duplicate CMR check for prospect CmrNo " + cmrsMods.getCmrNum());
                            }
                            continue;

                          }

                          nameMatched = true;

                          if (cmrsMods.getCmrPpsceid() == null || StringUtils.isBlank(cmrsMods.getCmrPpsceid())) {
                            if (incloud(cmrsMods.getCmrSortl(), searchTerm04182) || "00462".equals(cmrsMods.getCmrSortl())
                                || (cmrsMods.getCmrSortl() == null || StringUtils.isBlank(cmrsMods.getCmrSortl())
                                    || (cmrsMods.getCmrSortl() != null && (cmrsMods.getCmrSortl().trim().equalsIgnoreCase("000000")
                                        || cmrsMods.getCmrSortl().trim().equalsIgnoreCase("00000") || cmrsMods.getCmrSortl().matches("[^0-9]+"))))) {
                              if (cmrsMods.getCmrNum() != null && (cmrsMods.getCmrNum().startsWith("0") || cmrsMods.getCmrNum().startsWith("1")
                                  || cmrsMods.getCmrNum().startsWith("2") || cmrsMods.getCmrNum().startsWith("9"))) {
                                // should not be rejected
                                log.debug("Not a duplicate CMR.");
                              } else {
                                // should be rejected
                                log.debug("Duplicate CMR. Request should be rejected.");
                                shouldBeRejected = true;
                                handleLogDetails(cmrsMods, cmrData, matchedCMRs, details);
                              }
                            } else {
                              // should be rejected
                              log.debug("Duplicate CMR. Request should be rejected.");
                              shouldBeRejected = true;
                              handleLogDetails(cmrsMods, cmrData, matchedCMRs, details);
                            }
                          } else {
                            if (incloud(cmrsMods.getCmrSortl(), searchTerm04182)
                                || (cmrsMods.getCmrSortl() == null || StringUtils.isBlank(cmrsMods.getCmrSortl())
                                    || (cmrsMods.getCmrSortl() != null && (cmrsMods.getCmrSortl().trim().equalsIgnoreCase("000000")
                                        || cmrsMods.getCmrSortl().trim().equalsIgnoreCase("00000") || cmrsMods.getCmrSortl().matches("[^0-9]+"))))
                                    && cmrsMods.getCmrNum() != null && (cmrsMods.getCmrNum().startsWith("0") || cmrsMods.getCmrNum().startsWith("1")
                                        || cmrsMods.getCmrNum().startsWith("2"))) {
                              // should not be rejected
                              log.debug("Not a duplicate CMR.");
                            } else {
                              if (!incloud(cmrsMods.getCmrSortl(), searchTerm08036) && incloud(kukla, cnSpecialKukla)) {
                                // should not be rejected
                                log.debug("Not a duplicate CMR.");
                              } else {
                                // should be rejected
                                log.debug("Duplicate CMR. Request should be rejected.");
                                shouldBeRejected = true;
                                handleLogDetails(cmrsMods, cmrData, matchedCMRs, details);
                              }
                            }
                          }

                        }
                      }
                    }
                  } catch (Exception e) {
                    e.printStackTrace();
                    result.setDetails("Error on checking findCMR on Duplicate CMR Check of Chinese, name: " + cnNameDoubleByte);
                    engineData.addRejectionComment("OTH", "Error on checking findCMR on Duplicate CMR Check of Chinese, name: " + cnNameDoubleByte,
                        "", "");
                    result.setOnError(true);
                    result.setResults("Error on checking findCMR on Duplicate CMR Check of Chinese, name: " + cnNameDoubleByte);
                  }
                }

                // 4, Check FindCMR Non Latin with historical Chinese name -
                // single byte
                if (cnHistoryNameSingleByte != null && !"".equals(cnHistoryNameSingleByte)) {
                  List<String> cnHistoryNameList = Arrays.asList(cnHistoryNameSingleByte.split(";"));
                  CompanyRecordModel searchModelFindCmrCN = new CompanyRecordModel();
                  searchModelFindCmrCN.setIssuingCntry(data.getCmrIssuingCntry());
                  try {
                    for (String historyName : cnHistoryNameList) {
                      searchModelFindCmrCN.setName(historyName);
                      findCMRResult = searchFindCMR(searchModelFindCmrCN);

                      if (findCMRResult != null && findCMRResult.getItems() != null && !findCMRResult.getItems().isEmpty()) {
                        List<FindCMRRecordModel> cmrs = findCMRResult.getItems();
                        for (FindCMRRecordModel cmrsMods : cmrs) {

                          nameFindCmrCnResult = cmrsMods.getCmrIntlName1();
                          if (!StringUtils.isBlank(cmrsMods.getCmrIntlName2())) {
                            nameFindCmrCnResult = cmrsMods.getCmrIntlName1() + cmrsMods.getCmrIntlName2();
                          }

                          if (nameFindCmrCnResult != null && trimChineseSpace(nameFindCmrCnResult).equals(trimChineseSpace(historyName))) {

                            kukla = getKukla(entityManager, cmrsMods.getCmrNum(), data.getCmrIssuingCntry());

                            log.debug("CNDupCMRCheckElement: request " + data.getId().getReqId() + ", CmrNo " + cmrsMods.getCmrNum() + ", ceid "
                                + cmrsMods.getCmrPpsceid() + ", search term " + cmrsMods.getCmrSortl() + ", kukla " + kukla);

                            if (cmrsMods.getCmrNum() != null && cmrsMods.getCmrNum().startsWith("P")) {
                              // CREATCMR-6302
                              // log.debug("Skip Duplicate CMR check for
                              // prospect CmrNo " + cmrsMods.getCmrNum());
                              // continue;
                              if (StringUtils.isBlank(data.getCmrNo()) || !data.getCmrNo().equals(cmrsMods.getCmrNum())) {
                                log.debug("Found a duplicate prospect CMR.");
                                pcmrs += cmrsMods.getCmrNum() + ", ";
                                prospectCMRFlag = true;
                                handleLogDetails(cmrsMods, cmrData, matchedCMRs, details);
                              } else if (StringUtils.isNotBlank(data.getCmrNo()) && data.getCmrNo().equals(cmrsMods.getCmrNum())) {
                                convertPCMRFlag = true;
                                log.debug("Skip Duplicate CMR check for Convert to Legal CMR for prospect CmrNo " + cmrsMods.getCmrNum());
                              } else {
                                log.debug("Skip Duplicate CMR check for prospect CmrNo " + cmrsMods.getCmrNum());
                              }
                              continue;

                            }

                            historyNmMatched = true;

                            if (cmrsMods.getCmrPpsceid() == null || StringUtils.isBlank(cmrsMods.getCmrPpsceid())) {
                              if (incloud(cmrsMods.getCmrSortl(), searchTerm04182) || "00462".equals(cmrsMods.getCmrSortl())
                                  || (cmrsMods.getCmrSortl() == null || StringUtils.isBlank(cmrsMods.getCmrSortl())
                                      || (cmrsMods.getCmrSortl() != null && (cmrsMods.getCmrSortl().trim().equalsIgnoreCase("000000")
                                          || cmrsMods.getCmrSortl().trim().equalsIgnoreCase("00000")
                                          || cmrsMods.getCmrSortl().matches("[^0-9]+"))))) {
                                if (cmrsMods.getCmrNum() != null && (cmrsMods.getCmrNum().startsWith("0") || cmrsMods.getCmrNum().startsWith("1")
                                    || cmrsMods.getCmrNum().startsWith("2") || cmrsMods.getCmrNum().startsWith("9"))) {
                                  // should not be rejected
                                  log.debug("Not a duplicate CMR.");
                                } else {
                                  // should be rejected
                                  log.debug("Duplicate CMR. Request should be rejected.");
                                  shouldBeRejected = true;
                                  handleLogDetails(cmrsMods, cmrData, matchedCMRs, details);
                                }
                              } else {
                                // should be rejected
                                log.debug("Duplicate CMR. Request should be rejected.");
                                shouldBeRejected = true;
                                handleLogDetails(cmrsMods, cmrData, matchedCMRs, details);
                              }
                            } else {
                              if (incloud(cmrsMods.getCmrSortl(), searchTerm04182)
                                  || (cmrsMods.getCmrSortl() == null || StringUtils.isBlank(cmrsMods.getCmrSortl())
                                      || (cmrsMods.getCmrSortl() != null && (cmrsMods.getCmrSortl().trim().equalsIgnoreCase("000000")
                                          || cmrsMods.getCmrSortl().trim().equalsIgnoreCase("00000") || cmrsMods.getCmrSortl().matches("[^0-9]+"))))
                                      && cmrsMods.getCmrNum() != null && (cmrsMods.getCmrNum().startsWith("0") || cmrsMods.getCmrNum().startsWith("1")
                                          || cmrsMods.getCmrNum().startsWith("2"))) {
                                // should not be rejected
                                log.debug("Not a duplicate CMR.");
                              } else {
                                if (!incloud(cmrsMods.getCmrSortl(), searchTerm08036) && incloud(kukla, cnSpecialKukla)) {
                                  // should not be rejected
                                  log.debug("Not a duplicate CMR.");
                                } else {
                                  // should be rejected
                                  log.debug("Duplicate CMR. Request should be rejected.");
                                  shouldBeRejected = true;
                                  handleLogDetails(cmrsMods, cmrData, matchedCMRs, details);
                                }
                              }
                            }

                          }
                        }
                      }
                    }

                  } catch (Exception e) {
                    e.printStackTrace();
                    result
                        .setDetails("Error on getting findCMR data when Duplicate CMR Check of historical Chinese, name: " + cnHistoryNameSingleByte);
                    engineData.addRejectionComment("OTH",
                        "Error on getting findCMR data when Duplicate CMR Check of historical Chinese, name: " + cnHistoryNameSingleByte, "", "");
                    result.setOnError(true);
                    result
                        .setResults("Error on getting findCMR data when Duplicate CMR Check of historical Chinese, name: " + cnHistoryNameSingleByte);
                  }

                }

                // 5, Check FindCMR Non Latin with historical Chinese name -
                // double byte
                if (cnHistoryNameDoubleByte != null && !"".equals(cnHistoryNameDoubleByte)
                    && !cnHistoryNameDoubleByte.equals(cnHistoryNameSingleByte)) {
                  List<String> cnHistoryNameList = Arrays.asList(cnHistoryNameDoubleByte.split(";"));
                  CompanyRecordModel searchModelFindCmrCN = new CompanyRecordModel();
                  searchModelFindCmrCN.setIssuingCntry(data.getCmrIssuingCntry());
                  try {
                    for (String historyName : cnHistoryNameList) {
                      searchModelFindCmrCN.setName(historyName);
                      findCMRResult = searchFindCMR(searchModelFindCmrCN);

                      if (findCMRResult != null && findCMRResult.getItems() != null && !findCMRResult.getItems().isEmpty()) {
                        List<FindCMRRecordModel> cmrs = findCMRResult.getItems();
                        for (FindCMRRecordModel cmrsMods : cmrs) {

                          nameFindCmrCnResult = cmrsMods.getCmrIntlName1();
                          if (!StringUtils.isBlank(cmrsMods.getCmrIntlName2())) {
                            nameFindCmrCnResult = cmrsMods.getCmrIntlName1() + cmrsMods.getCmrIntlName2();
                          }

                          if (nameFindCmrCnResult != null && trimChineseSpace(nameFindCmrCnResult).equals(trimChineseSpace(historyName))) {

                            kukla = getKukla(entityManager, cmrsMods.getCmrNum(), data.getCmrIssuingCntry());

                            log.debug("CNDupCMRCheckElement: request " + data.getId().getReqId() + ", CmrNo " + cmrsMods.getCmrNum() + ", ceid "
                                + cmrsMods.getCmrPpsceid() + ", search term " + cmrsMods.getCmrSortl() + ", kukla " + kukla);

                            if (cmrsMods.getCmrNum() != null && cmrsMods.getCmrNum().startsWith("P")) {
                              // CREATCMR-6302
                              // log.debug("Skip Duplicate CMR check for
                              // prospect CmrNo " + cmrsMods.getCmrNum());
                              // continue;
                              if (StringUtils.isBlank(data.getCmrNo()) || !data.getCmrNo().equals(cmrsMods.getCmrNum())) {
                                log.debug("Found a duplicate prospect CMR.");
                                pcmrs += cmrsMods.getCmrNum() + ", ";
                                prospectCMRFlag = true;
                                handleLogDetails(cmrsMods, cmrData, matchedCMRs, details);
                              } else if (StringUtils.isNotBlank(data.getCmrNo()) && data.getCmrNo().equals(cmrsMods.getCmrNum())) {
                                convertPCMRFlag = true;
                                log.debug("Skip Duplicate CMR check for Convert to Legal CMR for prospect CmrNo " + cmrsMods.getCmrNum());
                              } else {
                                log.debug("Skip Duplicate CMR check for prospect CmrNo " + cmrsMods.getCmrNum());
                              }
                              continue;

                            }

                            historyNmMatched = true;

                            if (cmrsMods.getCmrPpsceid() == null || StringUtils.isBlank(cmrsMods.getCmrPpsceid())) {
                              if (incloud(cmrsMods.getCmrSortl(), searchTerm04182) || "00462".equals(cmrsMods.getCmrSortl())
                                  || (cmrsMods.getCmrSortl() == null || StringUtils.isBlank(cmrsMods.getCmrSortl())
                                      || (cmrsMods.getCmrSortl() != null && (cmrsMods.getCmrSortl().trim().equalsIgnoreCase("000000")
                                          || cmrsMods.getCmrSortl().trim().equalsIgnoreCase("00000")
                                          || cmrsMods.getCmrSortl().matches("[^0-9]+"))))) {
                                if (cmrsMods.getCmrNum() != null && (cmrsMods.getCmrNum().startsWith("0") || cmrsMods.getCmrNum().startsWith("1")
                                    || cmrsMods.getCmrNum().startsWith("2") || cmrsMods.getCmrNum().startsWith("9"))) {
                                  // should not be rejected
                                  log.debug("Not a duplicate CMR.");
                                } else {
                                  // should be rejected
                                  log.debug("Duplicate CMR. Request should be rejected.");
                                  shouldBeRejected = true;
                                  handleLogDetails(cmrsMods, cmrData, matchedCMRs, details);
                                }
                              } else {
                                // should be rejected
                                log.debug("Duplicate CMR. Request should be rejected.");
                                shouldBeRejected = true;
                                handleLogDetails(cmrsMods, cmrData, matchedCMRs, details);
                              }
                            } else {
                              if (incloud(cmrsMods.getCmrSortl(), searchTerm04182)
                                  || (cmrsMods.getCmrSortl() == null || StringUtils.isBlank(cmrsMods.getCmrSortl())
                                      || (cmrsMods.getCmrSortl() != null && (cmrsMods.getCmrSortl().trim().equalsIgnoreCase("000000")
                                          || cmrsMods.getCmrSortl().trim().equalsIgnoreCase("00000") || cmrsMods.getCmrSortl().matches("[^0-9]+"))))
                                      && cmrsMods.getCmrNum() != null && (cmrsMods.getCmrNum().startsWith("0") || cmrsMods.getCmrNum().startsWith("1")
                                          || cmrsMods.getCmrNum().startsWith("2"))) {
                                // should not be rejected
                                log.debug("Not a duplicate CMR.");
                              } else {
                                if (!incloud(cmrsMods.getCmrSortl(), searchTerm08036) && incloud(kukla, cnSpecialKukla)) {
                                  // should not be rejected
                                  log.debug("Not a duplicate CMR.");
                                } else {
                                  // should be rejected
                                  log.debug("Duplicate CMR. Request should be rejected.");
                                  shouldBeRejected = true;
                                  handleLogDetails(cmrsMods, cmrData, matchedCMRs, details);
                                }
                              }
                            }

                          }
                        }
                      }
                    }

                  } catch (Exception e) {
                    e.printStackTrace();
                    result
                        .setDetails("Error on getting findCMR data when Duplicate CMR Check of historical Chinese, name: " + cnHistoryNameDoubleByte);
                    engineData.addRejectionComment("OTH",
                        "Error on getting findCMR data when Duplicate CMR Check of historical Chinese, name: " + cnHistoryNameDoubleByte, "", "");
                    result.setOnError(true);
                    result
                        .setResults("Error on getting findCMR data when Duplicate CMR Check of historical Chinese, name: " + cnHistoryNameDoubleByte);
                  }

                }

                shouldGoToCMDE = true;

                // output
                if (shouldBeRejected && !shouldGoToCMDE) {
                  result.setResults("Matches Found");
                  result.setResults("Found Duplicate CMRs.");
                  Collections.sort(matchedCMRs);
                  engineData.addRejectionComment("DUPC", "Customer already exists / duplicate CMR", StringUtils.join(matchedCMRs, ", "), "");
                  // to allow overides later
                  requestData.getAdmin().setMatchIndc("C");
                  result.setOnError(true);
                  result.setProcessOutput(output);
                  result.setDetails(details.toString().trim());
                  sendManagerEmail(entityManager, admin, data, soldTo, details);

                } else if (shouldBeRejected && shouldGoToCMDE) {

                  log.debug("send request " + admin.getId().getReqId() + " to CMDE...");
                  super.setStopOnError(false);
                  super.setActionOnError(ActionOnError.Proceed);
                  result.setOnError(true);
                  result.setResults("Matches Found. Go to CMDE");
                  result.setDetails(details.toString().trim());
                  requestData.getAdmin().setMatchIndc("C");

                } else if (prospectCMRFlag && !convertPCMRFlag && !shouldBeRejected) { // CREATCMR-6302
                  result.setResults("Matches Found");
                  result.setResults("Found Duplicate Prospect CMRs only.");
                  pcmrs = pcmrs.substring(0, pcmrs.length() - 2);
                  Collections.sort(matchedCMRs);
                  engineData.addRejectionComment("DUPC",
                      "please import the existing Prospect CMR(" + pcmrs + ") into CreateCMR to convert into Legal CMR.",
                      StringUtils.join(matchedCMRs, ", "), "");
                  // to allow overrides later
                  requestData.getAdmin().setMatchIndc("C");
                  result.setOnError(true);
                  result.setProcessOutput(output);
                  result.setDetails(details.toString().trim());
                  // sendManagerEmail(entityManager, admin, data, soldTo,
                  // details);
                } else {
                  result.setDetails("No Duplicate CMRs were found.");
                  result.setResults("No Matches");
                  result.setOnError(false);
                }
              } else if (resultCNApi != null && !resultCNApi.isSuccess()) {
                result.setDetails("Error on getting China API Data when Duplicate CMR Check of Chinese - " + resultCNApi.getMessage());
                engineData.addRejectionComment("OTH",
                    "Error on getting China API Data when Duplicate CMR Check of Chinese - " + resultCNApi.getMessage(), "", "");
                result.setOnError(true);
                result.setResults("Error on getting China API Data when Duplicate CMR Check of Chinese - " + resultCNApi.getMessage());
              }

            } catch (Exception e) {
              e.printStackTrace();
              result.setDetails("Error on getting China API Data when Duplicate CMR Check of Chinese.");
              engineData.addRejectionComment("OTH", "Error on getting China API Data when Duplicate CMR Check of Chinese.", "", "");
              result.setOnError(true);
              result.setResults("Error on getting China API Data when Duplicate CMR Check of Chinese.");
            }
          } else {
            result.setDetails("Error on getting Chinese name when Duplicate CMR Check.");
            engineData.addRejectionComment("OTH", "Error on getting Chinese name when Duplicate CMR Check.", "", "");
            result.setOnError(true);
            result.setResults("Error on getting Chinese name when Duplicate CMR Check.");
          }
          break;

        case "AQSTN": // SCENARIO_LOCAL_AQSTN

          // logic:
          // a) if the return result has same Chinese name, then it is a
          // duplicate request.
          // b) if no Chinese information, then rely on English Name

          // CREATCMR-5461
          // check if should skip CNDupCMRCheck and set req status PPN
          skipAndPpn = getSkipAndPpnInd(entityManager, data);
          if (skipAndPpn) {
            log.debug("skip CN Dup CMR Check for req " + data.getId().getReqId());
            doSkipAndPpn(result, engineData);
            return result;
          }

          boolean ifAQSTNHasCN = true;
          iAddr = handler.getIntlAddrById(soldTo, entityManager);
          if (iAddr != null) {
            cnNameSingleByte = iAddr.getIntlCustNm1() + (!StringUtils.isBlank(iAddr.getIntlCustNm2()) ? (" " + iAddr.getIntlCustNm2()) : "");
          }
          // George, fix dup cmr not found CREATCMR-3133 on 20210802
          if (validCNName(cnNameSingleByte)) {
            try {

              // 1, Check CN API
              if (cnCreditCd != null && cnCreditCd.length() > 0) {
                searchModelCNAPI.setIssuingCntry(data.getCmrIssuingCntry());
                searchModelCNAPI.setCountryCd(soldTo.getLandCntry());
                searchModelCNAPI.setTaxCd1(cnCreditCd);
                resultCNApi = CompanyFinder.getCNApiInfo(searchModelCNAPI, "TAXCD");
                if (resultCNApi.getMessage() != null && resultCNApi.getMessage().contains("无数据")) {
                  boolean hasCNSpecialAttach = checkCNSpecialAttach(entityManager, data.getId().getReqId());
                  if (hasCNSpecialAttach) {
                    doSkipAndPpn4NoData(result, engineData);
                    return result;
                  }
                }
              } else {

                cnNameSingleByte = convert2SingleByte(cnNameSingleByte);
                searchModelCNAPI.setIssuingCntry(data.getCmrIssuingCntry());
                searchModelCNAPI.setCountryCd(soldTo.getLandCntry());
                searchModelCNAPI.setAltName(cnNameSingleByte);
                resultCNApi = CompanyFinder.getCNApiInfo(searchModelCNAPI, "ALTNAME");
              }

              if (resultCNApi != null && resultCNApi.isSuccess()) {
                cnNameSingleByte = resultCNApi.getRecord().getName();
                cnHistoryNameSingleByte = resultCNApi.getRecord().getHistoryNames();
                cnAddrSingleByte = resultCNApi.getRecord().getRegLocation();

                cnNameDoubleByte = convert2DoubleByte(cnNameSingleByte);
                cnHistoryNameDoubleByte = convert2DoubleByte(cnHistoryNameSingleByte);
                cnAddrDoubleByte = convert2DoubleByte(cnAddrSingleByte);

                try {
                  // 2, Check FindCMR NON Latin with Chinese name - single byte
                  CompanyRecordModel searchModelFindCmrCN = new CompanyRecordModel();
                  searchModelFindCmrCN.setIssuingCntry(data.getCmrIssuingCntry());
                  searchModelFindCmrCN.setName(cnNameSingleByte);
                  resultFindCmrCN = CompanyFinder.findCompanies(searchModelFindCmrCN);
                  if (!resultFindCmrCN.isEmpty() && resultFindCmrCN.size() > 0) {
                    for (int i = 0; i < resultFindCmrCN.size(); i++) {
                      nameFindCmrCnResult = resultFindCmrCN.get(i).getAltName() != null ? resultFindCmrCN.get(i).getAltName() : "";
                      if (nameFindCmrCnResult != null && trimChineseSpace(cnNameSingleByte).equals(trimChineseSpace(nameFindCmrCnResult))) {

                        if (resultFindCmrCN.get(i).getCmrNo() != null && resultFindCmrCN.get(i).getCmrNo().startsWith("P")) {
                          // CREATCMR-6302
                          // log.debug("Skip Duplicate CMR check for prospect
                          // CmrNo " + resultFindCmrCN.get(i).getCmrNo());
                          // continue;
                          if (StringUtils.isBlank(data.getCmrNo()) || !data.getCmrNo().equals(resultFindCmrCN.get(i).getCmrNo())) {
                            log.debug("Found a duplicate prospect CMR.");
                            pcmrs += resultFindCmrCN.get(i).getCmrNo() + ", ";
                            prospectCMRFlag = true;
                            cmrData = resultFindCmrCN.get(i);
                            handleLogDetails(cmrData, matchedCMRs, details);
                          } else if (StringUtils.isNotBlank(data.getCmrNo()) && data.getCmrNo().equals(resultFindCmrCN.get(i).getCmrNo())) {
                            convertPCMRFlag = true;
                            log.debug("Skip Duplicate CMR check for Convert to Legal CMR for prospect CmrNo " + resultFindCmrCN.get(i).getCmrNo());
                          } else {
                            log.debug("Skip Duplicate CMR check for prospect CmrNo " + resultFindCmrCN.get(i).getCmrNo());
                          }
                          continue;

                        }

                        nameMatched = true;

                        if (nameMatched) {
                          cmrData = resultFindCmrCN.get(i);
                          handleLogDetails(cmrData, matchedCMRs, details);
                        }
                      }
                    }
                  }
                } catch (Exception e) {
                  e.printStackTrace();
                  result.setDetails("Error on checking findCMR on Duplicate CMR Check of Chinese, name: " + cnNameSingleByte);
                  engineData.addRejectionComment("OTH", "Error on checking findCMR on Duplicate CMR Check of Chinese, name: " + cnNameSingleByte, "",
                      "");
                  result.setOnError(true);
                  result.setResults("Error on checking findCMR on Duplicate CMR Check of Chinese, name: " + cnNameSingleByte);
                }

                if (cnNameDoubleByte != null && !cnNameDoubleByte.equals(cnNameSingleByte)) {
                  try {
                    // 3, Check FindCMR NON Latin with Chinese name - double
                    // byte
                    CompanyRecordModel searchModelFindCmrCN = new CompanyRecordModel();
                    searchModelFindCmrCN.setIssuingCntry(data.getCmrIssuingCntry());
                    searchModelFindCmrCN.setName(cnNameDoubleByte);
                    resultFindCmrCN = CompanyFinder.findCompanies(searchModelFindCmrCN);
                    if (!resultFindCmrCN.isEmpty() && resultFindCmrCN.size() > 0) {
                      for (int i = 0; i < resultFindCmrCN.size(); i++) {
                        nameFindCmrCnResult = resultFindCmrCN.get(i).getAltName() != null ? resultFindCmrCN.get(i).getAltName() : "";
                        if (nameFindCmrCnResult != null && trimChineseSpace(cnNameDoubleByte).equals(trimChineseSpace(nameFindCmrCnResult))) {

                          if (resultFindCmrCN.get(i).getCmrNo() != null && resultFindCmrCN.get(i).getCmrNo().startsWith("P")) {
                            // CREATCMR-6302
                            // log.debug("Skip Duplicate CMR check for prospect
                            // CmrNo " + resultFindCmrCN.get(i).getCmrNo());
                            // continue;
                            if (StringUtils.isBlank(data.getCmrNo()) || !data.getCmrNo().equals(resultFindCmrCN.get(i).getCmrNo())) {
                              log.debug("Found a duplicate prospect CMR.");
                              pcmrs += resultFindCmrCN.get(i).getCmrNo() + ", ";
                              prospectCMRFlag = true;
                              cmrData = resultFindCmrCN.get(i);
                              handleLogDetails(cmrData, matchedCMRs, details);
                            } else if (StringUtils.isNotBlank(data.getCmrNo()) && data.getCmrNo().equals(resultFindCmrCN.get(i).getCmrNo())) {
                              convertPCMRFlag = true;
                              log.debug("Skip Duplicate CMR check for Convert to Legal CMR for prospect CmrNo " + resultFindCmrCN.get(i).getCmrNo());
                            } else {
                              log.debug("Skip Duplicate CMR check for prospect CmrNo " + resultFindCmrCN.get(i).getCmrNo());
                            }
                            continue;

                          }

                          nameMatched = true;

                          if (nameMatched) {
                            cmrData = resultFindCmrCN.get(i);
                            handleLogDetails(cmrData, matchedCMRs, details);
                          }
                        }
                      }
                    }
                  } catch (Exception e) {
                    e.printStackTrace();
                    result.setDetails("Error on checking findCMR on Duplicate CMR Check of Chinese, name: " + cnNameDoubleByte);
                    engineData.addRejectionComment("OTH", "Error on checking findCMR on Duplicate CMR Check of Chinese, name: " + cnNameDoubleByte,
                        "", "");
                    result.setOnError(true);
                    result.setResults("Error on checking findCMR on Duplicate CMR Check of Chinese, name: " + cnNameDoubleByte);
                  }
                }

                // 4, Check FindCMR Non Latin with historical Chinese name -
                // single byte
                if (cnHistoryNameSingleByte != null && !"".equals(cnHistoryNameSingleByte)) {
                  List<String> cnHistoryNameList = Arrays.asList(cnHistoryNameSingleByte.split(";"));
                  try {
                    CompanyRecordModel searchModelFindCmrCN = new CompanyRecordModel();
                    searchModelFindCmrCN.setIssuingCntry(data.getCmrIssuingCntry());
                    for (String historyName : cnHistoryNameList) {
                      searchModelFindCmrCN.setName(historyName);
                      resultFindCmrCN = CompanyFinder.findCompanies(searchModelFindCmrCN);
                      if (!resultFindCmrCN.isEmpty() && resultFindCmrCN.size() > 0) {
                        for (int i = 0; i < resultFindCmrCN.size(); i++) {
                          nameFindCmrCnResult = resultFindCmrCN.get(i).getAltName() != null ? resultFindCmrCN.get(i).getAltName() : "";
                          if (nameFindCmrCnResult != null && trimChineseSpace(nameFindCmrCnResult).equals(trimChineseSpace(historyName))) {

                            if (resultFindCmrCN.get(i).getCmrNo() != null && resultFindCmrCN.get(i).getCmrNo().startsWith("P")) {
                              // CREATCMR-6302
                              // log.debug("Skip Duplicate CMR check for
                              // prospect
                              // CmrNo " + resultFindCmrCN.get(i).getCmrNo());
                              // continue;
                              if (StringUtils.isBlank(data.getCmrNo()) || !data.getCmrNo().equals(resultFindCmrCN.get(i).getCmrNo())) {
                                log.debug("Found a duplicate prospect CMR.");
                                pcmrs += resultFindCmrCN.get(i).getCmrNo() + ", ";
                                prospectCMRFlag = true;
                                cmrData = resultFindCmrCN.get(i);
                                handleLogDetails(cmrData, matchedCMRs, details);
                              } else if (StringUtils.isNotBlank(data.getCmrNo()) && data.getCmrNo().equals(resultFindCmrCN.get(i).getCmrNo())) {
                                convertPCMRFlag = true;
                                log.debug(
                                    "Skip Duplicate CMR check for Convert to Legal CMR for prospect CmrNo " + resultFindCmrCN.get(i).getCmrNo());
                              } else {
                                log.debug("Skip Duplicate CMR check for prospect CmrNo " + resultFindCmrCN.get(i).getCmrNo());
                              }
                              continue;

                            }

                            historyNmMatched = true;

                            if (historyNmMatched) {
                              cmrData = resultFindCmrCN.get(i);
                              handleLogDetails(cmrData, matchedCMRs, details);
                            }
                          }
                        }
                      }
                    }

                  } catch (Exception e) {
                    e.printStackTrace();
                    result
                        .setDetails("Error on getting findCMR data when Duplicate CMR Check of historical Chinese, name: " + cnHistoryNameSingleByte);
                    engineData.addRejectionComment("OTH",
                        "Error on getting findCMR data when Duplicate CMR Check of historical Chinese, name: " + cnHistoryNameSingleByte, "", "");
                    result.setOnError(true);
                    result
                        .setResults("Error on getting findCMR data when Duplicate CMR Check of historical Chinese, name: " + cnHistoryNameSingleByte);
                  }
                }

                // 5, Check FindCMR Non Latin with historical Chinese name -
                // double byte
                if (cnHistoryNameDoubleByte != null && !"".equals(cnHistoryNameDoubleByte)
                    && !cnHistoryNameDoubleByte.equals(cnHistoryNameSingleByte)) {
                  List<String> cnHistoryNameList = Arrays.asList(cnHistoryNameDoubleByte.split(";"));
                  try {
                    CompanyRecordModel searchModelFindCmrCN = new CompanyRecordModel();
                    searchModelFindCmrCN.setIssuingCntry(data.getCmrIssuingCntry());
                    for (String historyName : cnHistoryNameList) {
                      searchModelFindCmrCN.setName(historyName);
                      resultFindCmrCN = CompanyFinder.findCompanies(searchModelFindCmrCN);
                      if (!resultFindCmrCN.isEmpty() && resultFindCmrCN.size() > 0) {
                        for (int i = 0; i < resultFindCmrCN.size(); i++) {
                          nameFindCmrCnResult = resultFindCmrCN.get(i).getAltName() != null ? resultFindCmrCN.get(i).getAltName() : "";
                          if (nameFindCmrCnResult != null && trimChineseSpace(nameFindCmrCnResult).equals(trimChineseSpace(historyName))) {

                            if (resultFindCmrCN.get(i).getCmrNo() != null && resultFindCmrCN.get(i).getCmrNo().startsWith("P")) {
                              // CREATCMR-6302
                              // log.debug("Skip Duplicate CMR check for
                              // prospect
                              // CmrNo " + resultFindCmrCN.get(i).getCmrNo());
                              // continue;
                              if (StringUtils.isBlank(data.getCmrNo()) || !data.getCmrNo().equals(resultFindCmrCN.get(i).getCmrNo())) {
                                log.debug("Found a duplicate prospect CMR.");
                                pcmrs += resultFindCmrCN.get(i).getCmrNo() + ", ";
                                prospectCMRFlag = true;
                                cmrData = resultFindCmrCN.get(i);
                                handleLogDetails(cmrData, matchedCMRs, details);
                              } else if (StringUtils.isNotBlank(data.getCmrNo()) && data.getCmrNo().equals(resultFindCmrCN.get(i).getCmrNo())) {
                                convertPCMRFlag = true;
                                log.debug(
                                    "Skip Duplicate CMR check for Convert to Legal CMR for prospect CmrNo " + resultFindCmrCN.get(i).getCmrNo());
                              } else {
                                log.debug("Skip Duplicate CMR check for prospect CmrNo " + resultFindCmrCN.get(i).getCmrNo());
                              }
                              continue;

                            }

                            historyNmMatched = true;

                            if (historyNmMatched) {
                              cmrData = resultFindCmrCN.get(i);
                              handleLogDetails(cmrData, matchedCMRs, details);
                            }
                          }
                        }
                      }
                    }

                  } catch (Exception e) {
                    e.printStackTrace();
                    result
                        .setDetails("Error on getting findCMR data when Duplicate CMR Check of historical Chinese, name: " + cnHistoryNameDoubleByte);
                    engineData.addRejectionComment("OTH",
                        "Error on getting findCMR data when Duplicate CMR Check of historical Chinese, name: " + cnHistoryNameDoubleByte, "", "");
                    result.setOnError(true);
                    result
                        .setResults("Error on getting findCMR data when Duplicate CMR Check of historical Chinese, name: " + cnHistoryNameDoubleByte);
                  }
                }

                // output
                if (nameMatched || historyNmMatched) {
                  result.setResults("Matches Found");
                  result.setResults("Found Duplicate CMRs.");
                  Collections.sort(matchedCMRs);
                  engineData.addRejectionComment("DUPC", "Customer already exists / duplicate CMR", StringUtils.join(matchedCMRs, ", "), "");
                  // to allow overides later
                  requestData.getAdmin().setMatchIndc("C");
                  result.setOnError(true);
                  result.setProcessOutput(output);
                  result.setDetails(details.toString().trim());
                  sendManagerEmail(entityManager, admin, data, soldTo, details);
                } else if (prospectCMRFlag && !convertPCMRFlag && !nameMatched && !historyNmMatched) { // CREATCMR-6302
                  result.setResults("Matches Found");
                  result.setResults("Found Duplicate Prospect CMRs only.");
                  pcmrs = pcmrs.substring(0, pcmrs.length() - 2);
                  Collections.sort(matchedCMRs);
                  engineData.addRejectionComment("DUPC",
                      "please import the existing Prospect CMR(" + pcmrs + ") into CreateCMR to convert into Legal CMR.",
                      StringUtils.join(matchedCMRs, ", "), "");
                  // to allow overrides later
                  requestData.getAdmin().setMatchIndc("C");
                  result.setOnError(true);
                  result.setProcessOutput(output);
                  result.setDetails(details.toString().trim());
                  // sendManagerEmail(entityManager, admin, data, soldTo,
                  // details);
                } else if (!nameMatched && !historyNmMatched) {
                  result.setDetails("No Duplicate CMRs were found.");
                  result.setResults("No Matches");
                  result.setOnError(false);
                }
              } else if (resultCNApi != null && !resultCNApi.isSuccess()) {
                result.setDetails("Error on getting China API Data when Duplicate CMR Check of Chinese - " + resultCNApi.getMessage());
                engineData.addRejectionComment("OTH",
                    "Error on getting China API Data when Duplicate CMR Check of Chinese - " + resultCNApi.getMessage(), "", "");
                result.setOnError(true);
                result.setResults("Error on getting China API Data when Duplicate CMR Check of Chinese - " + resultCNApi.getMessage());
              }

            } catch (Exception e) {
              e.printStackTrace();
              result.setDetails("Error on getting China API Data when Duplicate CMR Check of Chinese.");
              engineData.addRejectionComment("OTH", "Error on getting China API Data when Duplicate CMR Check of Chinese.", "", "");
              result.setOnError(true);
              result.setResults("Error on getting China API Data when Duplicate CMR Check of Chinese.");
            }

          } else {

            // check with English name and get matched result, then it is dup
            // req
            ifAQSTNHasCN = false;

            CompanyRecordModel searchModelFindCmr = new CompanyRecordModel();
            searchModelFindCmr.setIssuingCntry(data.getCmrIssuingCntry());
            searchModelFindCmr.setName(soldTo.getCustNm1() + (!StringUtils.isBlank(soldTo.getCustNm2()) ? (" " + soldTo.getCustNm2()) : ""));
            findCMRResult = searchFindCMR(searchModelFindCmr);
            if (findCMRResult != null && findCMRResult.getItems() != null && !findCMRResult.getItems().isEmpty()) {
              ceidMatched = true;
              List<FindCMRRecordModel> cmrs = findCMRResult.getItems();
              for (FindCMRRecordModel cmrsMods : cmrs) {

                if (cmrsMods.getCmrNum() != null && cmrsMods.getCmrNum().startsWith("P")) {
                  // CREATCMR-6302
                  // log.debug("Skip Duplicate CMR check for
                  // prospect CmrNo " + cmrsMods.getCmrNum());
                  // continue;
                  if (StringUtils.isBlank(data.getCmrNo()) || !data.getCmrNo().equals(cmrsMods.getCmrNum())) {
                    log.debug("Found a duplicate prospect CMR.");
                    pcmrs += cmrsMods.getCmrNum() + ", ";
                    prospectCMRFlag = true;
                    handleLogDetails(cmrsMods, cmrData, matchedCMRs, details);
                  } else if (StringUtils.isNotBlank(data.getCmrNo()) && data.getCmrNo().equals(cmrsMods.getCmrNum())) {
                    convertPCMRFlag = true;
                    log.debug("Skip Duplicate CMR check for Convert to Legal CMR for prospect CmrNo " + cmrsMods.getCmrNum());
                  } else {
                    log.debug("Skip Duplicate CMR check for prospect CmrNo " + cmrsMods.getCmrNum());
                  }
                  continue;
                }

                cmrData.setCmrNo(cmrsMods.getCmrNum());
                cmrData.setName(
                    cmrsMods.getCmrName1Plain() + (!StringUtils.isBlank(cmrsMods.getCmrName2Plain()) ? (" " + cmrsMods.getCmrName2Plain()) : ""));
                cmrData.setIssuingCntry(cmrsMods.getCmrIssuedBy());
                cmrData.setCied(cmrsMods.getCmrPpsceid());
                cmrData.setCountryCd(cmrsMods.getCmrCountryLanded());
                cmrData.setStreetAddress1(cmrsMods.getCmrStreetAddress());
                cmrData.setStreetAddress2(cmrsMods.getCmrStreetAddressCont());
                cmrData.setPostCd(cmrsMods.getCmrPostalCode());
                cmrData.setCity(cmrsMods.getCmrCity());
                shouldBeRejected = true;
                handleLogDetails(cmrData, matchedCMRs, details);
              }

              if (shouldBeRejected) {
                result.setResults("Matches Found");
                result.setResults("Found Duplicate CMRs.");
                Collections.sort(matchedCMRs);
                engineData.addRejectionComment("DUPC", "Customer already exists / duplicate CMR", StringUtils.join(matchedCMRs, ", "), "");
                // to allow overides later
                requestData.getAdmin().setMatchIndc("C");
                result.setOnError(true);
                result.setProcessOutput(output);
                result.setDetails(details.toString().trim());
                sendManagerEmail(entityManager, admin, data, soldTo, details);
              } else if (prospectCMRFlag && !convertPCMRFlag && !shouldBeRejected) { // CREATCMR-6302
                result.setResults("Matches Found");
                result.setResults("Found Duplicate Prospect CMRs only.");
                pcmrs = pcmrs.substring(0, pcmrs.length() - 2);
                Collections.sort(matchedCMRs);
                engineData.addRejectionComment("DUPC",
                    "please import the existing Prospect CMR(" + pcmrs + ") into CreateCMR to convert into Legal CMR.",
                    StringUtils.join(matchedCMRs, ", "), "");
                // to allow overrides later
                requestData.getAdmin().setMatchIndc("C");
                result.setOnError(true);
                result.setProcessOutput(output);
                result.setDetails(details.toString().trim());
                // sendManagerEmail(entityManager, admin, data, soldTo,
                // details);
              }
            } else {
              result.setDetails("No Duplicate CMRs were found.");
              result.setResults("No Matches");
              result.setOnError(false);
            }

          }
          break;
        case "BLUMX": // SCENARIO_LOCAL_BLUMX
        case "MRKT": // SCENARIO_LOCAL_MRKT

          // logic:
          // a) if the return result has same Chinese name, then it is a
          // duplicate request

          // CREATCMR-5461
          // check if should skip CNDupCMRCheck and set req status PPN
          skipAndPpn = getSkipAndPpnInd(entityManager, data);
          if (skipAndPpn) {
            log.debug("skip CN Dup CMR Check for req " + data.getId().getReqId());
            doSkipAndPpn(result, engineData);
            return result;
          }

          iAddr = handler.getIntlAddrById(soldTo, entityManager);
          if (iAddr != null) {

            try {

              // 1, Check CN API
              if (cnCreditCd != null && cnCreditCd.length() > 0) {
                searchModelCNAPI.setIssuingCntry(data.getCmrIssuingCntry());
                searchModelCNAPI.setCountryCd(soldTo.getLandCntry());
                searchModelCNAPI.setTaxCd1(cnCreditCd);
                resultCNApi = CompanyFinder.getCNApiInfo(searchModelCNAPI, "TAXCD");
                if (resultCNApi.getMessage() != null && resultCNApi.getMessage().contains("无数据")) {
                  boolean hasCNSpecialAttach = checkCNSpecialAttach(entityManager, data.getId().getReqId());
                  if (hasCNSpecialAttach) {
                    doSkipAndPpn4NoData(result, engineData);
                    return result;
                  }
                }
              } else {
                cnNameSingleByte = iAddr.getIntlCustNm1() + (!StringUtils.isBlank(iAddr.getIntlCustNm2()) ? (" " + iAddr.getIntlCustNm2()) : "");
                cnNameSingleByte = convert2SingleByte(cnNameSingleByte);
                searchModelCNAPI.setIssuingCntry(data.getCmrIssuingCntry());
                searchModelCNAPI.setCountryCd(soldTo.getLandCntry());
                searchModelCNAPI.setAltName(cnNameSingleByte);
                resultCNApi = CompanyFinder.getCNApiInfo(searchModelCNAPI, "ALTNAME");
              }

              if (resultCNApi != null && resultCNApi.isSuccess()) {
                cnNameSingleByte = resultCNApi.getRecord().getName();
                cnHistoryNameSingleByte = resultCNApi.getRecord().getHistoryNames();
                cnAddrSingleByte = resultCNApi.getRecord().getRegLocation();

                cnNameDoubleByte = convert2DoubleByte(cnNameSingleByte);
                cnHistoryNameDoubleByte = convert2DoubleByte(cnHistoryNameSingleByte);
                cnAddrDoubleByte = convert2DoubleByte(cnAddrSingleByte);

                try {
                  // 2, Check FindCMR NON Latin with Chinese name - single byte
                  CompanyRecordModel searchModelFindCmrCN = new CompanyRecordModel();
                  searchModelFindCmrCN.setIssuingCntry(data.getCmrIssuingCntry());
                  searchModelFindCmrCN.setName(cnNameSingleByte);
                  resultFindCmrCN = CompanyFinder.findCompanies(searchModelFindCmrCN);
                  if (!resultFindCmrCN.isEmpty() && resultFindCmrCN.size() > 0) {
                    for (int i = 0; i < resultFindCmrCN.size(); i++) {
                      nameFindCmrCnResult = resultFindCmrCN.get(i).getAltName() != null ? resultFindCmrCN.get(i).getAltName() : "";
                      if (nameFindCmrCnResult != null && trimChineseSpace(cnNameSingleByte).equals(trimChineseSpace(nameFindCmrCnResult))) {

                        if (resultFindCmrCN.get(i).getCmrNo() != null && resultFindCmrCN.get(i).getCmrNo().startsWith("P")) {
                          log.debug("Skip Duplicate CMR check for prospect CmrNo " + resultFindCmrCN.get(i).getCmrNo());
                          continue;
                        }

                        nameMatched = true;

                        if (nameMatched) {
                          cmrData = resultFindCmrCN.get(i);
                          handleLogDetails(cmrData, matchedCMRs, details);
                        }
                      }
                    }
                  }
                } catch (Exception e) {
                  e.printStackTrace();
                  result.setDetails("Error on checking findCMR on Duplicate CMR Check of Chinese, name: " + cnNameSingleByte);
                  engineData.addRejectionComment("OTH", "Error on checking findCMR on Duplicate CMR Check of Chinese, name: " + cnNameSingleByte, "",
                      "");
                  result.setOnError(true);
                  result.setResults("Error on checking findCMR on Duplicate CMR Check of Chinese, name: " + cnNameSingleByte);
                }

                if (cnNameDoubleByte != null && !cnNameDoubleByte.equals(cnNameSingleByte)) {
                  try {
                    // 3, Check FindCMR NON Latin with Chinese name - double
                    // byte
                    CompanyRecordModel searchModelFindCmrCN = new CompanyRecordModel();
                    searchModelFindCmrCN.setIssuingCntry(data.getCmrIssuingCntry());
                    searchModelFindCmrCN.setName(cnNameDoubleByte);
                    resultFindCmrCN = CompanyFinder.findCompanies(searchModelFindCmrCN);
                    if (!resultFindCmrCN.isEmpty() && resultFindCmrCN.size() > 0) {
                      for (int i = 0; i < resultFindCmrCN.size(); i++) {
                        nameFindCmrCnResult = resultFindCmrCN.get(i).getAltName() != null ? resultFindCmrCN.get(i).getAltName() : "";
                        if (nameFindCmrCnResult != null && trimChineseSpace(cnNameDoubleByte).equals(trimChineseSpace(nameFindCmrCnResult))) {

                          if (resultFindCmrCN.get(i).getCmrNo() != null && resultFindCmrCN.get(i).getCmrNo().startsWith("P")) {
                            log.debug("Skip Duplicate CMR check for prospect CmrNo " + resultFindCmrCN.get(i).getCmrNo());
                            continue;
                          }

                          nameMatched = true;

                          if (nameMatched) {
                            cmrData = resultFindCmrCN.get(i);
                            handleLogDetails(cmrData, matchedCMRs, details);
                          }
                        }
                      }
                    }
                  } catch (Exception e) {
                    e.printStackTrace();
                    result.setDetails("Error on checking findCMR on Duplicate CMR Check of Chinese, name: " + cnNameDoubleByte);
                    engineData.addRejectionComment("OTH", "Error on checking findCMR on Duplicate CMR Check of Chinese, name: " + cnNameDoubleByte,
                        "", "");
                    result.setOnError(true);
                    result.setResults("Error on checking findCMR on Duplicate CMR Check of Chinese, name: " + cnNameDoubleByte);
                  }
                }

                // 4, Check FindCMR Non Latin with historical Chinese name -
                // single byte
                if (cnHistoryNameSingleByte != null && !"".equals(cnHistoryNameSingleByte)) {
                  List<String> cnHistoryNameList = Arrays.asList(cnHistoryNameSingleByte.split(";"));
                  try {
                    CompanyRecordModel searchModelFindCmrCN = new CompanyRecordModel();
                    searchModelFindCmrCN.setIssuingCntry(data.getCmrIssuingCntry());
                    for (String historyName : cnHistoryNameList) {
                      searchModelFindCmrCN.setName(historyName);
                      resultFindCmrCN = CompanyFinder.findCompanies(searchModelFindCmrCN);
                      if (!resultFindCmrCN.isEmpty() && resultFindCmrCN.size() > 0) {
                        for (int i = 0; i < resultFindCmrCN.size(); i++) {
                          nameFindCmrCnResult = resultFindCmrCN.get(i).getAltName() != null ? resultFindCmrCN.get(i).getAltName() : "";
                          if (nameFindCmrCnResult != null && trimChineseSpace(nameFindCmrCnResult).equals(trimChineseSpace(historyName))) {

                            if (resultFindCmrCN.get(i).getCmrNo() != null && resultFindCmrCN.get(i).getCmrNo().startsWith("P")) {
                              log.debug("Skip Duplicate CMR check for prospect CmrNo " + resultFindCmrCN.get(i).getCmrNo());
                              continue;
                            }

                            historyNmMatched = true;

                            if (historyNmMatched) {
                              cmrData = resultFindCmrCN.get(i);
                              handleLogDetails(cmrData, matchedCMRs, details);
                            }
                          }
                        }
                      }
                    }

                  } catch (Exception e) {
                    e.printStackTrace();
                    result
                        .setDetails("Error on getting findCMR data when Duplicate CMR Check of historical Chinese, name: " + cnHistoryNameSingleByte);
                    engineData.addRejectionComment("OTH",
                        "Error on getting findCMR data when Duplicate CMR Check of historical Chinese, name: " + cnHistoryNameSingleByte, "", "");
                    result.setOnError(true);
                    result
                        .setResults("Error on getting findCMR data when Duplicate CMR Check of historical Chinese, name: " + cnHistoryNameSingleByte);
                  }
                }

                // 5, Check FindCMR Non Latin with historical Chinese name -
                // double byte
                if (cnHistoryNameDoubleByte != null && !"".equals(cnHistoryNameDoubleByte)
                    && !cnHistoryNameDoubleByte.equals(cnHistoryNameSingleByte)) {
                  List<String> cnHistoryNameList = Arrays.asList(cnHistoryNameDoubleByte.split(";"));
                  try {
                    CompanyRecordModel searchModelFindCmrCN = new CompanyRecordModel();
                    searchModelFindCmrCN.setIssuingCntry(data.getCmrIssuingCntry());
                    for (String historyName : cnHistoryNameList) {
                      searchModelFindCmrCN.setName(historyName);
                      resultFindCmrCN = CompanyFinder.findCompanies(searchModelFindCmrCN);
                      if (!resultFindCmrCN.isEmpty() && resultFindCmrCN.size() > 0) {
                        for (int i = 0; i < resultFindCmrCN.size(); i++) {
                          nameFindCmrCnResult = resultFindCmrCN.get(i).getAltName() != null ? resultFindCmrCN.get(i).getAltName() : "";
                          if (nameFindCmrCnResult != null && trimChineseSpace(nameFindCmrCnResult).equals(trimChineseSpace(historyName))) {

                            if (resultFindCmrCN.get(i).getCmrNo() != null && resultFindCmrCN.get(i).getCmrNo().startsWith("P")) {
                              log.debug("Skip Duplicate CMR check for prospect CmrNo " + resultFindCmrCN.get(i).getCmrNo());
                              continue;
                            }

                            historyNmMatched = true;

                            if (historyNmMatched) {
                              cmrData = resultFindCmrCN.get(i);
                              handleLogDetails(cmrData, matchedCMRs, details);
                            }
                          }
                        }
                      }
                    }

                  } catch (Exception e) {
                    e.printStackTrace();
                    result
                        .setDetails("Error on getting findCMR data when Duplicate CMR Check of historical Chinese, name: " + cnHistoryNameDoubleByte);
                    engineData.addRejectionComment("OTH",
                        "Error on getting findCMR data when Duplicate CMR Check of historical Chinese, name: " + cnHistoryNameDoubleByte, "", "");
                    result.setOnError(true);
                    result
                        .setResults("Error on getting findCMR data when Duplicate CMR Check of historical Chinese, name: " + cnHistoryNameDoubleByte);
                  }
                }

                // output
                if (nameMatched || historyNmMatched) {
                  result.setResults("Matches Found");
                  result.setResults("Found Duplicate CMRs.");
                  engineData.addRejectionComment("DUPC", "Customer already exists / duplicate CMR", StringUtils.join(matchedCMRs, ", "), "");
                  // to allow overides later
                  requestData.getAdmin().setMatchIndc("C");
                  result.setOnError(true);
                  result.setProcessOutput(output);
                  result.setDetails(details.toString().trim());
                  sendManagerEmail(entityManager, admin, data, soldTo, details);
                } else if (!nameMatched && !historyNmMatched) {
                  result.setDetails("No Duplicate CMRs were found.");
                  result.setResults("No Matches");
                  result.setOnError(false);
                }
              } else if (resultCNApi != null && !resultCNApi.isSuccess()) {
                result.setDetails("Error on getting China API Data when Duplicate CMR Check of Chinese - " + resultCNApi.getMessage());
                engineData.addRejectionComment("OTH",
                    "Error on getting China API Data when Duplicate CMR Check of Chinese - " + resultCNApi.getMessage(), "", "");
                result.setOnError(true);
                result.setResults("Error on getting China API Data when Duplicate CMR Check of Chinese - " + resultCNApi.getMessage());
              }

            } catch (Exception e) {
              e.printStackTrace();
              result.setDetails("Error on getting China API Data when Duplicate CMR Check of Chinese.");
              engineData.addRejectionComment("OTH", "Error on getting China API Data when Duplicate CMR Check of Chinese.", "", "");
              result.setOnError(true);
              result.setResults("Error on getting China API Data when Duplicate CMR Check of Chinese.");
            }
          } else {
            result.setDetails("Error on getting Chinese name when Duplicate CMR Check.");
            engineData.addRejectionComment("OTH", "Error on getting Chinese name when Duplicate CMR Check.", "", "");
            result.setOnError(true);
            result.setResults("Error on getting Chinese name when Duplicate CMR Check.");
          }
          break;

        case "INTER": // SCENARIO_LOCAL_INTER

          // logic:
          // a) if the return result has same Chinese name and address, then it
          // is a duplicate request
          // b) if there is no Chinese name, then check English name

          iAddr = handler.getIntlAddrById(soldTo, entityManager);
          if (iAddr != null) {
            cnNameSingleByte = iAddr.getIntlCustNm1() + (!StringUtils.isBlank(iAddr.getIntlCustNm2()) ? (" " + iAddr.getIntlCustNm2()) : "");
          }
          // George, fix dup cmr not found CREATCMR-3133 on 20210802
          if (validCNName(cnNameSingleByte)) {
            try {

              // 1, Check CN API
              if (cnCreditCd != null && cnCreditCd.length() > 0) {
                searchModelCNAPI.setIssuingCntry(data.getCmrIssuingCntry());
                searchModelCNAPI.setCountryCd(soldTo.getLandCntry());
                searchModelCNAPI.setTaxCd1(cnCreditCd);
                resultCNApi = CompanyFinder.getCNApiInfo(searchModelCNAPI, "TAXCD");
              } else {
                cnNameSingleByte = convert2SingleByte(cnNameSingleByte);
                searchModelCNAPI.setIssuingCntry(data.getCmrIssuingCntry());
                searchModelCNAPI.setCountryCd(soldTo.getLandCntry());
                searchModelCNAPI.setAltName(cnNameSingleByte);
                resultCNApi = CompanyFinder.getCNApiInfo(searchModelCNAPI, "ALTNAME");
              }

              if (resultCNApi != null && resultCNApi.isSuccess()) {
                cnNameSingleByte = resultCNApi.getRecord().getName();
                cnHistoryNameSingleByte = resultCNApi.getRecord().getHistoryNames();
                cnAddrSingleByte = resultCNApi.getRecord().getRegLocation();

                cnNameDoubleByte = convert2DoubleByte(cnNameSingleByte);
                cnHistoryNameDoubleByte = convert2DoubleByte(cnHistoryNameSingleByte);
                cnAddrDoubleByte = convert2DoubleByte(cnAddrSingleByte);

                try {
                  // 2, Check FindCMR NON Latin with Chinese name - single byte
                  CompanyRecordModel searchModelFindCmrCN = new CompanyRecordModel();
                  searchModelFindCmrCN.setIssuingCntry(data.getCmrIssuingCntry());
                  searchModelFindCmrCN.setName(cnNameSingleByte);
                  resultFindCmrCN = CompanyFinder.findCompanies(searchModelFindCmrCN);
                  if (!resultFindCmrCN.isEmpty() && resultFindCmrCN.size() > 0) {
                    for (int i = 0; i < resultFindCmrCN.size(); i++) {
                      nameFindCmrCnResult = resultFindCmrCN.get(i).getAltName() != null ? resultFindCmrCN.get(i).getAltName() : "";
                      if (nameFindCmrCnResult != null && trimChineseSpace(cnNameSingleByte).equals(trimChineseSpace(nameFindCmrCnResult))) {

                        if (resultFindCmrCN.get(i).getCmrNo() != null && resultFindCmrCN.get(i).getCmrNo().startsWith("P")) {
                          log.debug("Skip Duplicate CMR check for prospect CmrNo " + resultFindCmrCN.get(i).getCmrNo());
                          continue;
                        }

                        nameMatched = true;

                        // check Chinese address
                        addrFindCmrCnResult = resultFindCmrCN.get(i).getAltStreet() != null ? resultFindCmrCN.get(i).getAltStreet() : "";
                        if (addrFindCmrCnResult.equals(cnAddrSingleByte)) {
                          nameMatched = true;
                        } else {
                          nameMatched = false;
                        }

                        if (nameMatched) {
                          cmrData = resultFindCmrCN.get(i);
                          handleLogDetails(cmrData, matchedCMRs, details);
                        }
                      }
                    }
                  }
                } catch (Exception e) {
                  e.printStackTrace();
                  result.setDetails("Error on checking findCMR on Duplicate CMR Check of Chinese, name: " + cnNameSingleByte);
                  engineData.addRejectionComment("OTH", "Error on checking findCMR on Duplicate CMR Check of Chinese, name: " + cnNameSingleByte, "",
                      "");
                  result.setOnError(true);
                  result.setResults("Error on checking findCMR on Duplicate CMR Check of Chinese, name: " + cnNameSingleByte);
                }

                if (cnNameDoubleByte != null && !cnNameDoubleByte.equals(cnNameSingleByte)) {
                  try {
                    // 3, Check FindCMR NON Latin with Chinese name - double
                    // byte
                    CompanyRecordModel searchModelFindCmrCN = new CompanyRecordModel();
                    searchModelFindCmrCN.setIssuingCntry(data.getCmrIssuingCntry());
                    searchModelFindCmrCN.setName(cnNameDoubleByte);
                    resultFindCmrCN = CompanyFinder.findCompanies(searchModelFindCmrCN);
                    if (!resultFindCmrCN.isEmpty() && resultFindCmrCN.size() > 0) {
                      for (int i = 0; i < resultFindCmrCN.size(); i++) {
                        nameFindCmrCnResult = resultFindCmrCN.get(i).getAltName() != null ? resultFindCmrCN.get(i).getAltName() : "";
                        if (nameFindCmrCnResult != null && trimChineseSpace(cnNameDoubleByte).equals(trimChineseSpace(nameFindCmrCnResult))) {

                          if (resultFindCmrCN.get(i).getCmrNo() != null && resultFindCmrCN.get(i).getCmrNo().startsWith("P")) {
                            log.debug("Skip Duplicate CMR check for prospect CmrNo " + resultFindCmrCN.get(i).getCmrNo());
                            continue;
                          }

                          nameMatched = true;

                          // check Chinese address
                          addrFindCmrCnResult = resultFindCmrCN.get(i).getAltStreet() != null ? resultFindCmrCN.get(i).getAltStreet() : "";
                          if (addrFindCmrCnResult.equals(cnAddrSingleByte)) {
                            nameMatched = true;
                          } else {
                            nameMatched = false;
                          }

                          if (nameMatched) {
                            cmrData = resultFindCmrCN.get(i);
                            handleLogDetails(cmrData, matchedCMRs, details);
                          }
                        }
                      }
                    }
                  } catch (Exception e) {
                    e.printStackTrace();
                    result.setDetails("Error on checking findCMR on Duplicate CMR Check of Chinese, name: " + cnNameDoubleByte);
                    engineData.addRejectionComment("OTH", "Error on checking findCMR on Duplicate CMR Check of Chinese, name: " + cnNameDoubleByte,
                        "", "");
                    result.setOnError(true);
                    result.setResults("Error on checking findCMR on Duplicate CMR Check of Chinese, name: " + cnNameDoubleByte);
                  }
                }

                // 4, Check FindCMR Non Latin with historical Chinese name -
                // single byte
                if (cnHistoryNameSingleByte != null && !"".equals(cnHistoryNameSingleByte)) {
                  List<String> cnHistoryNameList = Arrays.asList(cnHistoryNameSingleByte.split(";"));
                  try {
                    CompanyRecordModel searchModelFindCmrCN = new CompanyRecordModel();
                    searchModelFindCmrCN.setIssuingCntry(data.getCmrIssuingCntry());
                    for (String historyName : cnHistoryNameList) {
                      searchModelFindCmrCN.setName(historyName);
                      resultFindCmrCN = CompanyFinder.findCompanies(searchModelFindCmrCN);
                      if (!resultFindCmrCN.isEmpty() && resultFindCmrCN.size() > 0) {
                        for (int i = 0; i < resultFindCmrCN.size(); i++) {
                          nameFindCmrCnResult = resultFindCmrCN.get(i).getAltName() != null ? resultFindCmrCN.get(i).getAltName() : "";
                          if (nameFindCmrCnResult != null && trimChineseSpace(nameFindCmrCnResult).equals(trimChineseSpace(historyName))) {

                            if (resultFindCmrCN.get(i).getCmrNo() != null && resultFindCmrCN.get(i).getCmrNo().startsWith("P")) {
                              log.debug("Skip Duplicate CMR check for prospect CmrNo " + resultFindCmrCN.get(i).getCmrNo());
                              continue;
                            }

                            historyNmMatched = true;

                            // check Chinese address
                            addrFindCmrCnResult = resultFindCmrCN.get(i).getAltStreet() != null ? resultFindCmrCN.get(i).getAltStreet() : "";
                            if (addrFindCmrCnResult.equals(cnAddrSingleByte)) {
                              historyNmMatched = true;
                            } else {
                              historyNmMatched = false;
                            }

                            if (historyNmMatched) {
                              cmrData = resultFindCmrCN.get(i);
                              handleLogDetails(cmrData, matchedCMRs, details);
                            }
                          }
                        }
                      }
                    }

                  } catch (Exception e) {
                    e.printStackTrace();
                    result
                        .setDetails("Error on getting findCMR data when Duplicate CMR Check of historical Chinese, name: " + cnHistoryNameSingleByte);
                    engineData.addRejectionComment("OTH",
                        "Error on getting findCMR data when Duplicate CMR Check of historical Chinese, name: " + cnHistoryNameSingleByte, "", "");
                    result.setOnError(true);
                    result
                        .setResults("Error on getting findCMR data when Duplicate CMR Check of historical Chinese, name: " + cnHistoryNameSingleByte);
                  }

                }

                // 5, Check FindCMR Non Latin with historical Chinese name -
                // double byte
                if (cnHistoryNameDoubleByte != null && !"".equals(cnHistoryNameDoubleByte)
                    && !cnHistoryNameDoubleByte.equals(cnHistoryNameSingleByte)) {
                  List<String> cnHistoryNameList = Arrays.asList(cnHistoryNameDoubleByte.split(";"));
                  try {
                    CompanyRecordModel searchModelFindCmrCN = new CompanyRecordModel();
                    searchModelFindCmrCN.setIssuingCntry(data.getCmrIssuingCntry());
                    for (String historyName : cnHistoryNameList) {
                      searchModelFindCmrCN.setName(historyName);
                      resultFindCmrCN = CompanyFinder.findCompanies(searchModelFindCmrCN);
                      if (!resultFindCmrCN.isEmpty() && resultFindCmrCN.size() > 0) {
                        for (int i = 0; i < resultFindCmrCN.size(); i++) {
                          nameFindCmrCnResult = resultFindCmrCN.get(i).getAltName() != null ? resultFindCmrCN.get(i).getAltName() : "";
                          if (nameFindCmrCnResult != null && trimChineseSpace(nameFindCmrCnResult).equals(trimChineseSpace(historyName))) {

                            if (resultFindCmrCN.get(i).getCmrNo() != null && resultFindCmrCN.get(i).getCmrNo().startsWith("P")) {
                              log.debug("Skip Duplicate CMR check for prospect CmrNo " + resultFindCmrCN.get(i).getCmrNo());
                              continue;
                            }

                            historyNmMatched = true;

                            // check Chinese address
                            addrFindCmrCnResult = resultFindCmrCN.get(i).getAltStreet() != null ? resultFindCmrCN.get(i).getAltStreet() : "";
                            if (addrFindCmrCnResult.equals(cnAddrSingleByte)) {
                              historyNmMatched = true;
                            } else {
                              historyNmMatched = false;
                            }

                            if (historyNmMatched) {
                              cmrData = resultFindCmrCN.get(i);
                              handleLogDetails(cmrData, matchedCMRs, details);
                            }
                          }
                        }
                      }
                    }

                  } catch (Exception e) {
                    e.printStackTrace();
                    result
                        .setDetails("Error on getting findCMR data when Duplicate CMR Check of historical Chinese, name: " + cnHistoryNameDoubleByte);
                    engineData.addRejectionComment("OTH",
                        "Error on getting findCMR data when Duplicate CMR Check of historical Chinese, name: " + cnHistoryNameDoubleByte, "", "");
                    result.setOnError(true);
                    result
                        .setResults("Error on getting findCMR data when Duplicate CMR Check of historical Chinese, name: " + cnHistoryNameDoubleByte);
                  }

                }

                // output
                if (nameMatched || historyNmMatched) {
                  result.setResults("Matches Found");
                  result.setResults("Found Duplicate CMRs.");
                  engineData.addRejectionComment("DUPC", "Customer already exists / duplicate CMR", StringUtils.join(matchedCMRs, ", "), "");
                  // to allow overides later
                  requestData.getAdmin().setMatchIndc("C");
                  result.setOnError(true);
                  result.setProcessOutput(output);
                  result.setDetails(details.toString().trim());
                  sendManagerEmail(entityManager, admin, data, soldTo, details);
                } else if (!nameMatched && !historyNmMatched) {
                  result.setDetails("No Duplicate CMRs were found.");
                  result.setResults("No Matches");
                  result.setOnError(false);
                }
              } else if (resultCNApi != null && !resultCNApi.isSuccess()) {
                result.setDetails("Error on getting China API Data when Duplicate CMR Check of Chinese - " + resultCNApi.getMessage());
                engineData.addRejectionComment("OTH",
                    "Error on getting China API Data when Duplicate CMR Check of Chinese - " + resultCNApi.getMessage(), "", "");
                result.setOnError(true);
                result.setResults("Error on getting China API Data when Duplicate CMR Check of Chinese - " + resultCNApi.getMessage());
              }

            } catch (Exception e) {
              e.printStackTrace();
              result.setDetails("Error on getting China API Data when Duplicate CMR Check of Chinese.");
              engineData.addRejectionComment("OTH", "Error on getting China API Data when Duplicate CMR Check of Chinese.", "", "");
              result.setOnError(true);
              result.setResults("Error on getting China API Data when Duplicate CMR Check of Chinese.");
            }

          } else {

            // check with English name and get matched result, then it is dup
            // req
            CompanyRecordModel searchModelFindCmr = new CompanyRecordModel();
            searchModelFindCmr.setIssuingCntry(data.getCmrIssuingCntry());
            searchModelFindCmr.setName(soldTo.getCustNm1() + (!StringUtils.isBlank(soldTo.getCustNm2()) ? (" " + soldTo.getCustNm2()) : ""));
            findCMRResult = searchFindCMR(searchModelFindCmr);
            if (findCMRResult != null && findCMRResult.getItems() != null && !findCMRResult.getItems().isEmpty()) {
              ceidMatched = true;
              List<FindCMRRecordModel> cmrs = findCMRResult.getItems();
              for (FindCMRRecordModel cmrsMods : cmrs) {

                if (cmrsMods.getCmrNum() != null && cmrsMods.getCmrNum().startsWith("P")) {
                  log.debug("Skip Duplicate CMR check for prospect CmrNo " + cmrsMods.getCmrNum());
                  continue;
                }

                cmrData.setCmrNo(cmrsMods.getCmrNum());
                cmrData.setName(
                    cmrsMods.getCmrName1Plain() + (!StringUtils.isBlank(cmrsMods.getCmrName2Plain()) ? (" " + cmrsMods.getCmrName2Plain()) : ""));
                cmrData.setIssuingCntry(cmrsMods.getCmrIssuedBy());
                cmrData.setCied(cmrsMods.getCmrPpsceid());
                cmrData.setCountryCd(cmrsMods.getCmrCountryLanded());
                cmrData.setStreetAddress1(cmrsMods.getCmrStreetAddress());
                cmrData.setStreetAddress2(cmrsMods.getCmrStreetAddressCont());
                cmrData.setPostCd(cmrsMods.getCmrPostalCode());
                cmrData.setCity(cmrsMods.getCmrCity());

                handleLogDetails(cmrData, matchedCMRs, details);
              }

              result.setResults("Matches Found");
              result.setResults("Found Duplicate CMRs.");
              engineData.addRejectionComment("DUPC", "Customer already exists / duplicate CMR", StringUtils.join(matchedCMRs, ", "), "");
              // to allow overides later
              requestData.getAdmin().setMatchIndc("C");
              result.setOnError(true);
              result.setProcessOutput(output);
              result.setDetails(details.toString().trim());
              sendManagerEmail(entityManager, admin, data, soldTo, details);
            } else {
              result.setDetails("No Duplicate CMRs were found.");
              result.setResults("No Matches");
              result.setOnError(false);
            }

          }
          break;

        case "PRIV": // SCENARIO_LOCAL_PRIV

          // logic:
          // a) check with English name and address, if get matched result, then
          // it is dup req

          CompanyRecordModel searchModelFindCmr = new CompanyRecordModel();
          searchModelFindCmr.setIssuingCntry(data.getCmrIssuingCntry());
          searchModelFindCmr.setName(soldTo.getCustNm1() + (!StringUtils.isBlank(soldTo.getCustNm2()) ? (" " + soldTo.getCustNm2()) : ""));
          searchModelFindCmr.setStreetAddress1(soldTo.getAddrTxt());
          searchModelFindCmr.setStreetAddress2(soldTo.getAddrTxt2());
          findCMRResult = searchFindCMR(searchModelFindCmr);
          if (findCMRResult != null && findCMRResult.getItems() != null && !findCMRResult.getItems().isEmpty()) {
            ceidMatched = true;
            List<FindCMRRecordModel> cmrs = findCMRResult.getItems();
            for (FindCMRRecordModel cmrsMods : cmrs) {

              if (cmrsMods.getCmrNum() != null && cmrsMods.getCmrNum().startsWith("P")) {
                log.debug("Skip Duplicate CMR check for prospect CmrNo " + cmrsMods.getCmrNum());
                continue;
              }

              cmrData.setCmrNo(cmrsMods.getCmrNum());
              cmrData.setName(
                  cmrsMods.getCmrName1Plain() + (!StringUtils.isBlank(cmrsMods.getCmrName2Plain()) ? (" " + cmrsMods.getCmrName2Plain()) : ""));
              cmrData.setIssuingCntry(cmrsMods.getCmrIssuedBy());
              cmrData.setCied(cmrsMods.getCmrPpsceid());
              cmrData.setCountryCd(cmrsMods.getCmrCountryLanded());
              cmrData.setStreetAddress1(cmrsMods.getCmrStreetAddress());
              cmrData.setStreetAddress2(cmrsMods.getCmrStreetAddressCont());
              cmrData.setPostCd(cmrsMods.getCmrPostalCode());
              cmrData.setCity(cmrsMods.getCmrCity());

              handleLogDetails(cmrData, matchedCMRs, details);
            }

            result.setResults("Matches Found");
            result.setResults("Found Duplicate CMRs.");
            engineData.addRejectionComment("DUPC", "Customer already exists / duplicate CMR", StringUtils.join(matchedCMRs, ", "), "");
            // to allow overides later
            requestData.getAdmin().setMatchIndc("C");
            result.setOnError(true);
            result.setProcessOutput(output);
            result.setDetails(details.toString().trim());
            sendManagerEmail(entityManager, admin, data, soldTo, details);
          } else {
            result.setDetails("No Duplicate CMRs were found.");
            result.setResults("No Matches");
            result.setOnError(false);
          }

          break;
        case "CROSS": // SCENARIO_CROSS_CROSS

          // logic:
          // a) check with English name and get matched result, then it is dup
          // req

          CompanyRecordModel searchModel = new CompanyRecordModel();
          searchModel.setIssuingCntry(data.getCmrIssuingCntry());
          searchModel.setName(soldTo.getCustNm1() + (!StringUtils.isBlank(soldTo.getCustNm2()) ? (" " + soldTo.getCustNm2()) : ""));
          searchModel.setCountryCd(soldTo.getLandCntry());
          findCMRResult = searchFindCMR(searchModel);
          if (findCMRResult != null && findCMRResult.getItems() != null && !findCMRResult.getItems().isEmpty()) {
            ceidMatched = true;
            List<FindCMRRecordModel> cmrs = findCMRResult.getItems();
            for (FindCMRRecordModel cmrsMods : cmrs) {

              if (cmrsMods.getCmrNum() != null && cmrsMods.getCmrNum().startsWith("P")) {
                // CREATCMR-6302
                // log.debug("Skip Duplicate CMR check for
                // prospect CmrNo " + cmrsMods.getCmrNum());
                // continue;
                if (StringUtils.isBlank(data.getCmrNo()) || !data.getCmrNo().equals(cmrsMods.getCmrNum())) {
                  log.debug("Found a duplicate prospect CMR.");
                  pcmrs += cmrsMods.getCmrNum() + ", ";
                  prospectCMRFlag = true;
                  handleLogDetails(cmrsMods, cmrData, matchedCMRs, details);
                } else if (StringUtils.isNotBlank(data.getCmrNo()) && data.getCmrNo().equals(cmrsMods.getCmrNum())) {
                  convertPCMRFlag = true;
                  log.debug("Skip Duplicate CMR check for Convert to Legal CMR for prospect CmrNo " + cmrsMods.getCmrNum());
                } else {
                  log.debug("Skip Duplicate CMR check for prospect CmrNo " + cmrsMods.getCmrNum());
                }
                continue;
              }

              cmrData.setCmrNo(cmrsMods.getCmrNum());
              cmrData.setName(
                  cmrsMods.getCmrName1Plain() + (!StringUtils.isBlank(cmrsMods.getCmrName2Plain()) ? (" " + cmrsMods.getCmrName2Plain()) : ""));
              cmrData.setIssuingCntry(cmrsMods.getCmrIssuedBy());
              cmrData.setCied(cmrsMods.getCmrPpsceid());
              cmrData.setCountryCd(cmrsMods.getCmrCountryLanded());
              cmrData.setStreetAddress1(cmrsMods.getCmrStreetAddress());
              cmrData.setStreetAddress2(cmrsMods.getCmrStreetAddressCont());
              cmrData.setPostCd(cmrsMods.getCmrPostalCode());
              cmrData.setCity(cmrsMods.getCmrCity());
              shouldBeRejected = true;
              handleLogDetails(cmrData, matchedCMRs, details);
            }

            if (shouldBeRejected) {
              result.setResults("Matches Found");
              result.setResults("Found Duplicate CMRs.");
              Collections.sort(matchedCMRs);
              engineData.addRejectionComment("DUPC", "Customer already exists / duplicate CMR", StringUtils.join(matchedCMRs, ", "), "");
              // to allow overides later
              requestData.getAdmin().setMatchIndc("C");
              result.setOnError(true);
              result.setProcessOutput(output);
              result.setDetails(details.toString().trim());
              sendManagerEmail(entityManager, admin, data, soldTo, details);
            } else if (prospectCMRFlag && !convertPCMRFlag && !shouldBeRejected) { // CREATCMR-6302
              result.setResults("Matches Found");
              result.setResults("Found Duplicate Prospect CMRs only.");
              pcmrs = pcmrs.substring(0, pcmrs.length() - 2);
              Collections.sort(matchedCMRs);
              engineData.addRejectionComment("DUPC",
                  "please import the existing Prospect CMR(" + pcmrs + ") into CreateCMR to convert into Legal CMR.",
                  StringUtils.join(matchedCMRs, ", "), "");
              // to allow overrides later
              requestData.getAdmin().setMatchIndc("C");
              result.setOnError(true);
              result.setProcessOutput(output);
              result.setDetails(details.toString().trim());
              // sendManagerEmail(entityManager, admin, data, soldTo,
              // details);
            }
          } else {
            result.setDetails("No Duplicate CMRs were found.");
            result.setResults("No Matches");
            result.setOnError(false);
          }
          break;
        default:
          result.setDetails("Invalid Scenario Sub-type on Duplicate CMR Check." + data.getCustSubGrp());
          engineData.addRejectionComment("OTH", "Invalid Scenario Sub-type is missing on Duplicate CMR Check." + data.getCustSubGrp(), "", "");
          result.setOnError(true);
          result.setResults("Invalid Scenario Sub-type is missing on Duplicate CMR Check");
          break;
        }
      } else {
        result.setDetails("Scenario Sub-type is missing on Duplicate CMR Check.");
        engineData.addRejectionComment("OTH", "Scenario Sub-type is missing on Duplicate CMR Check.", "", "");
        result.setOnError(true);
        result.setResults("Scenario Sub-type is missing on Duplicate CMR Check");
      }

    } else {
      result.setDetails("Missing main address on the request.");
      engineData.addRejectionComment("OTH", "Missing main address on the request.", "", "");
      result.setResults("No Matches");
      result.setOnError(true);
    }
    return result;
  }

  public static void logDuplicateCMR(StringBuilder details, CompanyRecordModel companyRecordModel) {
    if (!StringUtils.isBlank(companyRecordModel.getCmrNo())) {
      details.append("CMR Number = " + companyRecordModel.getCmrNo()).append("\n");
    }
    if (!StringUtils.isBlank(companyRecordModel.getIssuingCntry())) {
      details.append("Issuing Country =  " + companyRecordModel.getIssuingCntry()).append("\n");
    }
    if (!StringUtils.isBlank(companyRecordModel.getAltName())) {
      details.append("Chinese Customer Name =  " + companyRecordModel.getAltName()).append("\n");
    }
    if (!StringUtils.isBlank(companyRecordModel.getAltStreet())) {
      details.append("Chinese Address =  " + companyRecordModel.getAltStreet()).append("\n");
    }
    if (!StringUtils.isBlank(companyRecordModel.getName())) {
      details.append("Customer Name=  " + companyRecordModel.getName()).append("\n");
    }
    if (!StringUtils.isBlank(companyRecordModel.getDunsNo())) {
      details.append("Duns No =  " + companyRecordModel.getDunsNo()).append("\n");
    }
    details.append("<br>");
  }

  public static void logDuplicateCMR(StringBuilder details, DuplicateCMRCheckResponse cmrCheckRecord) {
    if (!StringUtils.isBlank(cmrCheckRecord.getCmrNo())) {
      details.append("CMR Number = " + cmrCheckRecord.getCmrNo()).append("\n");
    }
    if (!StringUtils.isBlank(cmrCheckRecord.getIssuingCntry())) {
      details.append("Issuing Country =  " + cmrCheckRecord.getIssuingCntry()).append("\n");
    }
    if (!StringUtils.isBlank(cmrCheckRecord.getCustomerName())) {
      details.append("Customer Name =  " + cmrCheckRecord.getCustomerName()).append("\n");
    }
    if (!StringUtils.isBlank(cmrCheckRecord.getLandedCountry())) {
      details.append("Landed Country =  " + cmrCheckRecord.getLandedCountry()).append("\n");
    }
    if (!StringUtils.isBlank(cmrCheckRecord.getStreetLine1())) {
      details.append("Address =  " + cmrCheckRecord.getStreetLine1()).append("\n");
    }
    if (!StringUtils.isBlank(cmrCheckRecord.getStreetLine2())) {
      details.append("Address (cont)=  " + cmrCheckRecord.getStreetLine2()).append("\n");
    }
    if (!StringUtils.isBlank(cmrCheckRecord.getDunsNo())) {
      details.append("Duns No =  " + cmrCheckRecord.getDunsNo()).append("\n");
    }
  }

  @Override
  public String getProcessCode() {
    return AutomationElementRegistry.CN_DUP_CMR_CHECK;
  }

  @Override
  public String getProcessDesc() {
    return "China Duplicate CMR Check";
  }

  public MatchingResponse<DuplicateCMRCheckResponse> getMatches(EntityManager entityManager, RequestData requestData, AutomationEngineData engineData)
      throws Exception {
    Admin admin = requestData.getAdmin();
    Data data = requestData.getData();
    ScenarioExceptionsUtil scenarioExceptions = getScenarioExceptions(entityManager, requestData, engineData);
    MatchingResponse<DuplicateCMRCheckResponse> response = new MatchingResponse<DuplicateCMRCheckResponse>();
    MatchingServiceClient client = CmrServicesFactory.getInstance().createClient(SystemConfiguration.getValue("BATCH_SERVICES_URL"),
        MatchingServiceClient.class);
    client.setReadTimeout(1000 * 60 * 5);
    Map<String, List<String>> addrTypes = scenarioExceptions.getAddressTypesForDuplicateCMRCheck();
    boolean vatMatchRequired = AutomationUtil.isCheckVatForDuplicates(data.getCmrIssuingCntry());
    if (addrTypes.isEmpty()) {
      addrTypes.put("ZS01", Arrays.asList("ZS01"));
    }
    boolean first = true;
    List<String> rdcAddrTypes = null;
    for (String cmrAddrType : addrTypes.keySet()) {
      log.debug("CmrAddrType= " + cmrAddrType);
      rdcAddrTypes = addrTypes.get(cmrAddrType);
      Addr addr = requestData.getAddress(cmrAddrType);
      if (addr != null) {
        for (String rdcAddrType : rdcAddrTypes) {

          DuplicateCMRCheckRequest request = getRequest(entityManager, data, admin, addr, engineData, rdcAddrType, vatMatchRequired);
          log.debug("Executing Duplicate CMR Check " + (admin.getId().getReqId() > 0 ? " for Request ID: " + admin.getId().getReqId() : " through UI")
              + " for AddrType: " + cmrAddrType + "-" + rdcAddrType);
          MatchingResponse<?> rawResponse = client.executeAndWrap(MatchingServiceClient.CMR_SERVICE_ID, request, MatchingResponse.class);
          ObjectMapper mapper = new ObjectMapper();
          String json = mapper.writeValueAsString(rawResponse);
          TypeReference<MatchingResponse<DuplicateCMRCheckResponse>> ref = new TypeReference<MatchingResponse<DuplicateCMRCheckResponse>>() {
          };
          MatchingResponse<DuplicateCMRCheckResponse> res = mapper.readValue(json, ref);

          if (first) {
            if (res.getSuccess()) {
              response = res;
              first = false;
            } else {
              return res;
            }
          } else if (res.getSuccess()) {
            updateMatches(response, res);
          }
        }
      } else {
        log.debug("No '" + cmrAddrType + "' address on the request. Skipping duplicate check.");
        continue;
      }
    }

    if (response.getSuccess() && response.getMatches().size() > 0) {
      AutomationUtil countryUtil = AutomationUtil.getNewCountryUtil(data.getCmrIssuingCntry());
      if (countryUtil != null) {
        countryUtil.filterDuplicateCMRMatches(entityManager, requestData, engineData, response);
      }
    }

    // reverify
    if (response.getSuccess() && response.getMatches().size() == 0) {
      response.setMatched(false);
      response.setMessage("No matches found for the given search criteria.");
    }

    return response;
  }

  private DuplicateCMRCheckRequest getRequest(EntityManager entityManager, Data data, Admin admin, Addr addr, AutomationEngineData engineData,
      String rdcAddrType, boolean vatMatchRequired) {
    DuplicateCMRCheckRequest request = new DuplicateCMRCheckRequest();
    if (addr != null) {
      request.setIssuingCountry(data.getCmrIssuingCntry());
      request.setLandedCountry(addr.getLandCntry());
      GEOHandler handler = RequestUtils.getGEOHandler(data.getCmrIssuingCntry());
      if (handler != null && !handler.customerNamesOnAddress()) {
        request.setCustomerName(admin.getMainCustNm1() + (StringUtils.isBlank(admin.getMainCustNm2()) ? "" : " " + admin.getMainCustNm2()));
      } else {
        request.setCustomerName(addr.getCustNm1() + (StringUtils.isBlank(addr.getCustNm2()) ? "" : " " + addr.getCustNm2()));
      }

      if (data.getCustSubGrp() != null) {
        if ("PRIV".equals(data.getCustSubGrp()) || "INTER".equals(data.getCustSubGrp())) {
          request.setStreetLine1(addr.getAddrTxt());
          request.setStreetLine2(StringUtils.isEmpty(addr.getAddrTxt2()) ? "" : addr.getAddrTxt2());
        } else if ("CROSS".equals(data.getCustSubGrp()) || "AQSTN".equals(data.getCustSubGrp())) {
          request.setNameMatch("Y");
        }

      }

      if (vatMatchRequired) {
        if (StringUtils.isNotBlank(data.getVat())) {
          request.setVat(data.getVat());
        } else if (StringUtils.isNotBlank(addr.getVat())) {
          request.setVat(addr.getVat());
        }
      }
      request.setAddrType(rdcAddrType);

      DuplicateChecksUtil.setCountrySpecificsForCMRChecks(entityManager, admin, data, addr, request, engineData);
    }
    return request;
  }

  private void updateMatches(MatchingResponse<DuplicateCMRCheckResponse> global, MatchingResponse<DuplicateCMRCheckResponse> iteration) {
    List<DuplicateCMRCheckResponse> updated = new ArrayList<DuplicateCMRCheckResponse>();
    for (DuplicateCMRCheckResponse resp1 : global.getMatches()) {
      for (DuplicateCMRCheckResponse resp2 : iteration.getMatches()) {
        if (resp1.getCmrNo().equals(resp2.getCmrNo())) {
          updated.add(resp1);
          break;
        }
      }
    }

    global.setSuccess(true);
    global.setMatches(updated);
    global.setMatched(updated.size() > 0);
  }

  protected String getZS01Kunnr(String cmrNo, String cntry) throws Exception {
    String kunnr = "";

    String url = SystemConfiguration.getValue("CMR_SERVICES_URL");
    String mandt = SystemConfiguration.getValue("MANDT");
    String sql = ExternalizedQuery.getSql("GET.ZS01.KUNNR");
    sql = StringUtils.replace(sql, ":MANDT", "'" + mandt + "'");
    sql = StringUtils.replace(sql, ":ZZKV_CUSNO", "'" + cmrNo + "'");
    sql = StringUtils.replace(sql, ":KATR6", "'" + cntry + "'");

    String dbId = QueryClient.RDC_APP_ID;

    QueryRequest query = new QueryRequest();
    query.setSql(sql);
    query.addField("KUNNR");
    query.addField("ZZKV_CUSNO");

    QueryClient client = CmrServicesFactory.getInstance().createClient(url, QueryClient.class);
    QueryResponse response = client.executeAndWrap(dbId, query, QueryResponse.class);

    if (response.isSuccess() && response.getRecords() != null && response.getRecords().size() != 0) {
      List<Map<String, Object>> records = response.getRecords();
      Map<String, Object> record = records.get(0);
      kunnr = record.get("KUNNR") != null ? record.get("KUNNR").toString() : "";
    }
    return kunnr;
  }

  protected static FindCMRResultModel searchFindCMR(CompanyRecordModel searchModel) throws Exception {
    String cmrNo = searchModel.getCmrNo();
    String issuingCntry = searchModel.getIssuingCntry();
    String params = null;
    if (!StringUtils.isBlank(searchModel.getCied())) {
      params = "&ppsCeId=" + searchModel.getCied();
    }
    if (!StringUtils.isBlank(searchModel.getName())) {
      String name = searchModel.getName();
      name = StringUtils.replace(name, " ", "%20");
      // params = "&customerName=" + name;
      params = "&customerName=" + URLEncoder.encode(name, "UTF-8");
    }
    if (!StringUtils.isBlank(searchModel.getStreetAddress1())) {
      String street = searchModel.getStreetAddress1();
      street = StringUtils.replace(street, " ", "%20");
      if (!StringUtils.isBlank(searchModel.getStreetAddress2())) {
        String street2 = searchModel.getStreetAddress2();
        street2 = StringUtils.replace(street2, " ", "%20");
        street += street2;
      }
      params += "&streetAddress=" + street;
    }
    params += "&addressType=ZS01";
    FindCMRResultModel results = SystemUtil.findCMRs(cmrNo, issuingCntry, 50, null, params);
    return results;
  }

  private void sendManagerEmail(EntityManager entityManager, Admin admin, Data data, Addr soldTo, StringBuilder details) {
    boolean skip = true;
    if (skip) {
      return;
    }
    Person target = null;
    String manager = null;
    try {
      target = BluePagesHelper.getPerson(admin.getRequesterId());
      manager = BluePagesHelper.getManagerEmail(target.getId());
    } catch (CmrException e) {
      log.debug("Failed in getting requester manager. ReqID = " + data.getId().getReqId() + ", requester = " + admin.getRequesterId());
      e.printStackTrace();
    }
    if (manager != null) {
      log.debug("Sending duplicate CMR manager email. ReqID = " + data.getId().getReqId() + ", requester = " + admin.getRequesterId() + ". Manager = "
          + manager);
      String host = SystemConfiguration.getValue("MAIL_HOST");
      String subject = "Duplicate CMR Found for Request " + data.getId().getReqId() + " From " + admin.getRequesterId();
      // String from = SystemConfiguration.getValue("MAIL_FROM");
      String from = "CreateCMR_Automation_China";
      String email = "ReqID " + data.getId().getReqId() + "status is changed to REJECT due to duplicate CMR found. <br><br> Found records: "
          + details;

      Email mail = new Email();
      mail.setSubject(subject);
      mail.setTo(manager);
      mail.setFrom(from);
      mail.setMessage(email);
      mail.setType(MessageType.HTML);
      mail.send(host);
    }
  }

  private String convert2DoubleByte(String value) {
    String modifiedVal = null;
    if (value != null && value.length() > 0) {
      modifiedVal = value;
      modifiedVal = modifiedVal.replace("1", "１");
      modifiedVal = modifiedVal.replace("2", "２");
      modifiedVal = modifiedVal.replace("3", "３");
      modifiedVal = modifiedVal.replace("4", "４");
      modifiedVal = modifiedVal.replace("5", "５");
      modifiedVal = modifiedVal.replace("6", "６");
      modifiedVal = modifiedVal.replace("7", "７");
      modifiedVal = modifiedVal.replace("8", "８");
      modifiedVal = modifiedVal.replace("9", "９");
      modifiedVal = modifiedVal.replace("0", "０");
      modifiedVal = modifiedVal.replace("a", "ａ");
      modifiedVal = modifiedVal.replace("b", "ｂ");
      modifiedVal = modifiedVal.replace("c", "ｃ");
      modifiedVal = modifiedVal.replace("d", "ｄ");
      modifiedVal = modifiedVal.replace("e", "ｅ");
      modifiedVal = modifiedVal.replace("f", "ｆ");
      modifiedVal = modifiedVal.replace("g", "ｇ");
      modifiedVal = modifiedVal.replace("h", "ｈ");
      modifiedVal = modifiedVal.replace("i", "ｉ");
      modifiedVal = modifiedVal.replace("j", "ｊ");
      modifiedVal = modifiedVal.replace("k", "ｋ");
      modifiedVal = modifiedVal.replace("l", "ｌ");
      modifiedVal = modifiedVal.replace("m", "ｍ");
      modifiedVal = modifiedVal.replace("n", "ｎ");
      modifiedVal = modifiedVal.replace("o", "ｏ");
      modifiedVal = modifiedVal.replace("p", "ｐ");
      modifiedVal = modifiedVal.replace("q", "ｑ");
      modifiedVal = modifiedVal.replace("r", "ｒ");
      modifiedVal = modifiedVal.replace("s", "ｓ");
      modifiedVal = modifiedVal.replace("t", "ｔ");
      modifiedVal = modifiedVal.replace("u", "ｕ");
      modifiedVal = modifiedVal.replace("v", "ｖ");
      modifiedVal = modifiedVal.replace("w", "ｗ");
      modifiedVal = modifiedVal.replace("x", "ｘ");
      modifiedVal = modifiedVal.replace("y", "ｙ");
      modifiedVal = modifiedVal.replace("z", "ｚ");
      modifiedVal = modifiedVal.replace("A", "Ａ");
      modifiedVal = modifiedVal.replace("B", "Ｂ");
      modifiedVal = modifiedVal.replace("C", "Ｃ");
      modifiedVal = modifiedVal.replace("D", "Ｄ");
      modifiedVal = modifiedVal.replace("E", "Ｅ");
      modifiedVal = modifiedVal.replace("F", "Ｆ");
      modifiedVal = modifiedVal.replace("G", "Ｇ");
      modifiedVal = modifiedVal.replace("H", "Ｈ");
      modifiedVal = modifiedVal.replace("I", "Ｉ");
      modifiedVal = modifiedVal.replace("J", "Ｊ");
      modifiedVal = modifiedVal.replace("K", "Ｋ");
      modifiedVal = modifiedVal.replace("L", "Ｌ");
      modifiedVal = modifiedVal.replace("M", "Ｍ");
      modifiedVal = modifiedVal.replace("N", "Ｎ");
      modifiedVal = modifiedVal.replace("O", "Ｏ");
      modifiedVal = modifiedVal.replace("P", "Ｐ");
      modifiedVal = modifiedVal.replace("Q", "Ｑ");
      modifiedVal = modifiedVal.replace("R", "Ｒ");
      modifiedVal = modifiedVal.replace("S", "Ｓ");
      modifiedVal = modifiedVal.replace("T", "Ｔ");
      modifiedVal = modifiedVal.replace("U", "Ｕ");
      modifiedVal = modifiedVal.replace("V", "Ｖ");
      modifiedVal = modifiedVal.replace("W", "Ｗ");
      modifiedVal = modifiedVal.replace("X", "Ｘ");
      modifiedVal = modifiedVal.replace("Y", "Ｙ");
      modifiedVal = modifiedVal.replace("Z", "Ｚ");
      modifiedVal = modifiedVal.replace(" ", "　");
      modifiedVal = modifiedVal.replace("&", "＆");
      modifiedVal = modifiedVal.replace("-", "－");
      modifiedVal = modifiedVal.replace(".", "．");
      modifiedVal = modifiedVal.replace(",", "，");
      modifiedVal = modifiedVal.replace(":", "：");
      modifiedVal = modifiedVal.replace("_", "＿");
      modifiedVal = modifiedVal.replace("(", "（");
      modifiedVal = modifiedVal.replace(")", "）");
    }
    return modifiedVal;
  }

  private String convert2SingleByte(String value) {
    String modifiedVal = null;
    if (value != null && value.length() > 0) {
      modifiedVal = value;
      modifiedVal = modifiedVal.replace("１", "1");
      modifiedVal = modifiedVal.replace("２", "2");
      modifiedVal = modifiedVal.replace("３", "3");
      modifiedVal = modifiedVal.replace("４", "4");
      modifiedVal = modifiedVal.replace("５", "5");
      modifiedVal = modifiedVal.replace("６", "6");
      modifiedVal = modifiedVal.replace("７", "7");
      modifiedVal = modifiedVal.replace("８", "8");
      modifiedVal = modifiedVal.replace("９", "9");
      modifiedVal = modifiedVal.replace("０", "0");
      modifiedVal = modifiedVal.replace("ａ", "a");
      modifiedVal = modifiedVal.replace("ｂ", "b");
      modifiedVal = modifiedVal.replace("ｃ", "c");
      modifiedVal = modifiedVal.replace("ｄ", "d");
      modifiedVal = modifiedVal.replace("ｅ", "e");
      modifiedVal = modifiedVal.replace("ｆ", "f");
      modifiedVal = modifiedVal.replace("ｇ", "g");
      modifiedVal = modifiedVal.replace("ｈ", "h");
      modifiedVal = modifiedVal.replace("ｉ", "i");
      modifiedVal = modifiedVal.replace("ｊ", "j");
      modifiedVal = modifiedVal.replace("ｋ", "k");
      modifiedVal = modifiedVal.replace("ｌ", "l");
      modifiedVal = modifiedVal.replace("ｍ", "m");
      modifiedVal = modifiedVal.replace("ｎ", "n");
      modifiedVal = modifiedVal.replace("ｏ", "o");
      modifiedVal = modifiedVal.replace("ｐ", "p");
      modifiedVal = modifiedVal.replace("ｑ", "q");
      modifiedVal = modifiedVal.replace("ｒ", "r");
      modifiedVal = modifiedVal.replace("ｓ", "s");
      modifiedVal = modifiedVal.replace("ｔ", "t");
      modifiedVal = modifiedVal.replace("ｕ", "u");
      modifiedVal = modifiedVal.replace("ｖ", "v");
      modifiedVal = modifiedVal.replace("ｗ", "w");
      modifiedVal = modifiedVal.replace("ｘ", "x");
      modifiedVal = modifiedVal.replace("ｙ", "y");
      modifiedVal = modifiedVal.replace("ｚ", "z");
      modifiedVal = modifiedVal.replace("Ａ", "A");
      modifiedVal = modifiedVal.replace("Ｂ", "B");
      modifiedVal = modifiedVal.replace("Ｃ", "C");
      modifiedVal = modifiedVal.replace("Ｄ", "D");
      modifiedVal = modifiedVal.replace("Ｅ", "E");
      modifiedVal = modifiedVal.replace("Ｆ", "F");
      modifiedVal = modifiedVal.replace("Ｇ", "G");
      modifiedVal = modifiedVal.replace("Ｈ", "H");
      modifiedVal = modifiedVal.replace("Ｉ", "I");
      modifiedVal = modifiedVal.replace("Ｊ", "J");
      modifiedVal = modifiedVal.replace("Ｋ", "K");
      modifiedVal = modifiedVal.replace("Ｌ", "L");
      modifiedVal = modifiedVal.replace("Ｍ", "M");
      modifiedVal = modifiedVal.replace("Ｎ", "N");
      modifiedVal = modifiedVal.replace("Ｏ", "O");
      modifiedVal = modifiedVal.replace("Ｐ", "P");
      modifiedVal = modifiedVal.replace("Ｑ", "Q");
      modifiedVal = modifiedVal.replace("Ｒ", "R");
      modifiedVal = modifiedVal.replace("Ｓ", "S");
      modifiedVal = modifiedVal.replace("Ｔ", "T");
      modifiedVal = modifiedVal.replace("Ｕ", "U");
      modifiedVal = modifiedVal.replace("Ｖ", "V");
      modifiedVal = modifiedVal.replace("Ｗ", "W");
      modifiedVal = modifiedVal.replace("Ｘ", "X");
      modifiedVal = modifiedVal.replace("Ｙ", "Y");
      modifiedVal = modifiedVal.replace("Ｚ", "Z");
      modifiedVal = modifiedVal.replace("　", " ");
      modifiedVal = modifiedVal.replace("＆", "&");
      modifiedVal = modifiedVal.replace("－", "-");
      modifiedVal = modifiedVal.replace("．", ".");
      modifiedVal = modifiedVal.replace("，", ",");
      modifiedVal = modifiedVal.replace("：", ":");
      modifiedVal = modifiedVal.replace("＿", "_");
      modifiedVal = modifiedVal.replace("（", "(");
      modifiedVal = modifiedVal.replace("）", ")");
    }
    return modifiedVal;
  }

  private String[] getSearchTerm08036(EntityManager entityManager) {
    String[] list = null;
    String sql = ExternalizedQuery.getSql("QUERY.GET.SPECIAL_SEARCH_TERM");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("FIELD_ID", "##CNSearchTerm08036");
    query.setParameter("CMR_ISSUING_CNTRY", "641");
    List<String> results = query.getResults(String.class);
    if (results != null && !results.isEmpty()) {
      list = results.toArray(new String[results.size()]);
    }
    return list;
  }

  private List<String> getSpecialSearchTermList(EntityManager entityManager, String cmrIssuingCntry) {
    List<String> list = null;
    String sql = ExternalizedQuery.getSql("QUERY.GET.SPECIAL_SEARCH_TERM");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("FIELD_ID", "##SpecialSearchTerm");
    query.setParameter("CMR_ISSUING_CNTRY", cmrIssuingCntry);
    List<String> results = query.getResults(String.class);
    if (results != null && !results.isEmpty()) {
      list = results;
    }
    return list;
  }

  private String getKukla(EntityManager entityManager, String cmrNo, String issuingCountry) {
    String result = null;
    String sql = ExternalizedQuery.getSql("QUERY.GET.KNA1_BY_CMRNO");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("ZZKV_CUSNO", cmrNo);
    query.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
    query.setParameter("KATR6", issuingCountry);
    List<Object[]> sqlResults = query.getResults();
    if (sqlResults != null && !sqlResults.isEmpty()) {
      if (sqlResults.size() == 1) {
        result = (String) sqlResults.get(0)[0];
      } else {
        log.debug("function getKukla returned " + sqlResults.size() + "result(s). It could be a data issue. Please check.");
      }
    }
    return result;
  }

  private void convertMatchData(FindCMRRecordModel cmrsMods, CompanyRecordModel cmrData) {
    if (!StringUtils.isBlank(cmrsMods.getCmrNum())) {
      cmrData.setCmrNo(cmrsMods.getCmrNum());
    }
    if (!StringUtils.isBlank(Float.toString(cmrsMods.getSearchScore()))) {
      cmrData.setMatchGrade(Float.toString(cmrsMods.getSearchScore()));
    }
    if (!StringUtils.isBlank(cmrsMods.getCmrIssuedBy())) {
      cmrData.setIssuingCntry(cmrsMods.getCmrIssuedBy());
    }
    String intlName = cmrsMods.getCmrIntlName1() + (!StringUtils.isBlank(cmrsMods.getCmrIntlName2()) ? (" " + cmrsMods.getCmrIntlName2()) : "");
    if (!StringUtils.isBlank(intlName)) {
      cmrData.setAltName(intlName);
    }
    if (!StringUtils.isBlank(cmrsMods.getCmrIntlCity1())) {
      cmrData.setAltCity(cmrsMods.getCmrIntlCity1());
    }
    if (!StringUtils.isBlank(cmrsMods.getCmrIntlAddress())) {
      cmrData.setAltStreet(cmrsMods.getCmrIntlAddress());
    }
    String name = cmrsMods.getCmrName1Plain() + (!StringUtils.isBlank(cmrsMods.getCmrName2Plain()) ? (" " + cmrsMods.getCmrName2Plain()) : "");
    if (!StringUtils.isBlank(name)) {
      cmrData.setName(name);
    }
    if (!StringUtils.isBlank(cmrsMods.getCmrCity())) {
      cmrData.setCity(cmrsMods.getCmrCity());
    }
    if (!StringUtils.isBlank(cmrsMods.getCmrStateDesc())) {
      cmrData.setStateProv(cmrsMods.getCmrStateDesc());
    }
    if (!StringUtils.isBlank(cmrsMods.getCmrPostalCode())) {
      cmrData.setPostCd(cmrsMods.getCmrPostalCode());
    }
    if (!StringUtils.isBlank(cmrsMods.getCmrVat())) {
      cmrData.setVat(cmrsMods.getCmrVat());
    }
    if (!StringUtils.isBlank(cmrsMods.getCmrDuns())) {
      cmrData.setDunsNo(cmrsMods.getCmrDuns());
    }
    if (!StringUtils.isBlank(cmrsMods.getCmrPpsceid())) {
      cmrData.setCied(cmrsMods.getCmrPpsceid());
    }
  }

  private boolean incloud(String str, String[] strList) {
    boolean result = false;
    if (StringUtils.isNotBlank(str)) {
      for (String temp : strList) {
        if (str.equals(temp)) {
          result = true;
        }
      }
    }
    return result;
  }

  private boolean validCNName(String input) {
    if (input != null && !StringUtils.isBlank(input) && !"null".equals(input) && !"*".equals(input)) {
      return true;
    } else {
      return false;
    }
  }

  // The trim doesn't work if input param is Chinese Space.
  // so first replace the Chiense Space with English Space.
  private String trimChineseSpace(String nameFindCmrCnResult) {
    String ret = "";
    if (nameFindCmrCnResult != null) {
      nameFindCmrCnResult = nameFindCmrCnResult.replace((char) 12288, ' ');
      nameFindCmrCnResult = nameFindCmrCnResult.trim();
      ret = nameFindCmrCnResult;
    }
    return ret;
  }

  private void handleLogDetails(CompanyRecordModel cmrData, List<String> matchedCMRs, StringBuilder details) {
    if (included(matchedCMRs, cmrData.getCmrNo())) {
      log.debug("CmrNo " + cmrData.getCmrNo() + " is included. skip logging...");
      return;
    }
    matchedCMRs.add(cmrData.getCmrNo());
    details.append("\n");
    logDuplicateCMR(details, cmrData);
  }

  private void handleLogDetails(FindCMRRecordModel cmrsMods, CompanyRecordModel cmrData, List<String> matchedCMRs, StringBuilder details) {
    if (included(matchedCMRs, cmrsMods.getCmrNum())) {
      log.debug("CmrNo " + cmrsMods.getCmrNum() + " is included. skip logging...");
      return;
    }
    matchedCMRs.add(cmrsMods.getCmrNum());
    convertMatchData(cmrsMods, cmrData);
    details.append("\n");
    logDuplicateCMR(details, cmrData);
  }

  private boolean included(List<String> strList, String str) {
    boolean result = false;
    if (StringUtils.isNotBlank(str)) {
      for (String temp : strList) {
        if (str.equals(temp)) {
          result = true;
        }
      }
    }
    return result;
  }

  public static boolean getSkipAndPpnInd(EntityManager entityManager, Data data) {
    boolean result = false;
    boolean hasCNSpecialAttach = checkCNSpecialAttach(entityManager, data.getId().getReqId());
    if (hasCNSpecialAttach && "Y".equals(data.getVatExempt())) {
      log.debug("VatExempt is selected. CN specific attachment is found. Should skip and set PPN for req " + data.getId().getReqId());
      return true;
    }
    return result;
  }

  private static boolean checkCNSpecialAttach(EntityManager entityManager, long reqId) {
    boolean result = false;
    String sql = ExternalizedQuery.getSql("QUERY.CHECK_CN_API_ATTACHMENT");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("ID", reqId);
    List<String> records = query.getResults(String.class);
    if (records != null && records.size() > 0) {
      result = true;
      log.debug("found CN specific attachment for req " + reqId);
    }
    return result;
  }

  private void doSkipAndPpn(AutomationResult<MatchingOutput> result, AutomationEngineData engineData) {
    result.setDetails("Skip CN Duplicate CMR Check and forword request to CMDE, due to VAT Exempt and CN Specific attachment.");
    engineData.addPositiveCheckStatus("Skip CN Duplicate CMR Check and forword request to CMDE, due to VAT Exempt and CN Specific attachment.");
    engineData.setSkipChecks();
    result.setResults("Skip");
    result.setOnError(false);
  }

  private void doSkipAndPpn4NoData(AutomationResult<MatchingOutput> result, AutomationEngineData engineData) {
    result.setDetails("Forword request to CMDE, due to CNService API responds error: 无数据 and CN Specific attachment.");
    engineData.addPositiveCheckStatus("Forword request to CMDE, due to CNService API responds error: 无数据 and CN Specific attachment.");
    // engineData.setSkipChecks();
    result.setResults("Skip");
    result.setOnError(false);
  }

}
