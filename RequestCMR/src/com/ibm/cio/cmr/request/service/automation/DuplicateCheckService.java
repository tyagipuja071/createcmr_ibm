package com.ibm.cio.cmr.request.service.automation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.ibm.cio.cmr.request.automation.RequestData;
import com.ibm.cio.cmr.request.automation.impl.gbl.DupReqCheckElement;
import com.ibm.cio.cmr.request.automation.impl.la.br.BrazilDupCMRCheckElement;
import com.ibm.cio.cmr.request.automation.util.DuplicateContainer;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.model.ParamContainer;
import com.ibm.cio.cmr.request.model.auto.BaseV2RequestModel;
import com.ibm.cio.cmr.request.model.automation.DuplicateCheckModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.service.BaseSimpleService;
import com.ibm.cio.cmr.request.user.AppUser;
import com.ibm.cio.cmr.request.util.BluePagesHelper;
import com.ibm.cio.cmr.request.util.SystemLocation;
import com.ibm.cmr.services.client.CmrServicesFactory;
import com.ibm.cmr.services.client.QueryClient;
import com.ibm.cmr.services.client.matching.MatchingResponse;
import com.ibm.cmr.services.client.matching.request.ReqCheckResponse;
import com.ibm.cmr.services.client.query.QueryRequest;
import com.ibm.cmr.services.client.query.QueryResponse;

/**
 * @author PoojaTyagi
 * 
 */
@Component
public class DuplicateCheckService extends BaseSimpleService<DuplicateCheckModel> {

  private static final Logger LOG = Logger.getLogger(DuplicateCheckService.class);
  List<DuplicateCheckModel> resultList = new ArrayList<DuplicateCheckModel>();
  List<DuplicateCheckModel> cmrResultList = new ArrayList<DuplicateCheckModel>();

  @Override
  protected DuplicateCheckModel doProcess(EntityManager entityManager, HttpServletRequest request, ParamContainer params) throws Exception {
    List<ReqCheckResponse> matches = null;
    List<DuplicateContainer> cmrMatches = null;
    BaseV2RequestModel dupRsnChkModel = null;
    DuplicateCheckModel dupChkModel = null;
    BrazilDupCMRCheckElement dupElement = new BrazilDupCMRCheckElement(null, null, false, false);
    LOG.debug("Processing doProcess() method of  DuplicateCheckService");

    if (!StringUtils.isBlank((String) request.getAttribute("duplicacyReason"))) {
      dupRsnChkModel = (BaseV2RequestModel) params.getParam("model");
    } else {
      dupChkModel = (DuplicateCheckModel) params.getParam("model");
    }
    // to check if duplicate CMRs exist
    if (dupChkModel != null && "CHECK_FR_DUP_CMRS".equals(dupChkModel.getAction())) {
      // duplicate cmr check function
      cmrResultList = new ArrayList<DuplicateCheckModel>();
      cmrMatches = dupElement.checkCMRDuplicacy(entityManager, dupChkModel.getVat(), dupChkModel.getVatzi01(), dupChkModel.getSubscenario());

      if (cmrMatches != null && cmrMatches.size() > 0) {
        // to assign model values to be displayed on grid
        for (DuplicateContainer matchedCMR : cmrMatches) {
          DuplicateCheckModel resultModel = new DuplicateCheckModel();

          resultModel.setIssuingCountry(dupChkModel.getIssuingCountry());
          resultModel.setVat(matchedCMR.getVat());
          resultModel.setCustomerName(matchedCMR.getName1() + (StringUtils.isBlank(matchedCMR.getName2()) ? "" : " " + matchedCMR.getName2()));
          resultModel.setCmrNo(matchedCMR.getCmrNo());
          cmrResultList.add(resultModel);
        }
        dupChkModel.setSuccess(true);
        dupChkModel.setMessage("CMR Matches are found !");
      }
    } else if (dupChkModel != null && "GET_DUP_CMRS_LISTS".equals(dupChkModel.getAction())) {
      // sending the cmrs result list as param back to controller to populate
      // gird
      if (!cmrResultList.isEmpty()) {
        params.addParam("cmrResultList", cmrResultList);
      } else {
        params.addParam("cmrResultList", new ArrayList<DuplicateCheckModel>());
      }
    }

    // to check if duplicate requests exist
    else if (dupChkModel != null && "CHECK_FR_DUP_REQS".equals(dupChkModel.getAction())) {
      // hitting duplicate request chk service to check if match exists and
      // populate resultList with matches
      resultList = new ArrayList<DuplicateCheckModel>();
      MatchingResponse<ReqCheckResponse> response = getDuplicateRequests(entityManager, dupChkModel);
      if (response != null && !response.getMatches().isEmpty()) {
        matches = response.getMatches();
        // to assign model values to be displayed on grid
        for (ReqCheckResponse match : matches) {
          DuplicateCheckModel resultModel = new DuplicateCheckModel();
          resultModel.setIssuingCountry(match.getIssuingCntry());
          String name1 = match.getCustomerName() != null ? match.getCustomerName() : "";
          String streetLine1 = match.getStreetLine1() != null ? match.getStreetLine1() : "";
          String streetLine2 = match.getStreetLine2() != null ? match.getStreetLine2() : "";
          String vat = dupChkModel.getVat();

          resultModel.setCustomerName(name1 + (StringUtils.isNotBlank(streetLine1) ? "\\n " + streetLine1 : "")
              + (StringUtils.isNotBlank(streetLine2) ? "\\n " + streetLine2 : "") + (StringUtils.isNotBlank(vat) ? "\\nVAT: " + vat : ""));
          resultModel.setReqId(Long.toString(match.getReqId()));
          resultModel.setMatchType(Long.toString(match.getMatchGrade()));
          resultList.add(resultModel);
        }
        dupChkModel.setSuccess(true);
        dupChkModel.setMessage("Matches are found !");
      }
    } else if (dupChkModel != null && "GET_DUP_REQS_LISTS".equals(dupChkModel.getAction())) {
      if (!resultList.isEmpty()) {
        params.addParam("resultList", resultList);
      } else {
        params.addParam("resultList", new ArrayList<DuplicateCheckModel>());
      }
    }

    else if (dupChkModel != null && "CHECK_FR_BLGRP".equals(dupChkModel.getAction()) && "BUSPR".equals(dupChkModel.getSubscenario())) {
      LOG.debug("Executing check for BR_BP_BLUEGROUP, for  business partner ..");
      AppUser user = AppUser.getUser(request);
      boolean isUserInBP_GRP = BluePagesHelper.isUserInBRBPBlueGroup(user.getIntranetId());
      if (isUserInBP_GRP) {
        dupChkModel.setSuccess(true);
      } else {
        dupChkModel.setSuccess(false);
      }

    }

    // else if (dupRsnChkModel != null &&
    // "DUP_CMR_RSN".equals(dupRsnChkModel.getAction())) {
    // LOG.debug("Executing Duplicte cmr reason save..");
    // RequestData requestData = new RequestData(entityManager,
    // dupRsnChkModel.getReqId());
    // Admin admin = requestData.getAdmin();
    // admin.setDupCmrReason(dupRsnChkModel.getDupCmrRsn());
    // EntityTransaction transaction = entityManager.getTransaction();
    // try {
    // transaction.begin();
    // entityManager.merge(admin);
    // transaction.commit();
    // } catch (Exception e) {
    // if (transaction.isActive()) {
    // transaction.rollback();
    // }
    // }
    // }
    return dupChkModel;
  }

  private MatchingResponse<ReqCheckResponse> getDuplicateRequests(EntityManager entityManager, DuplicateCheckModel dupChkModel) throws Exception {
    RequestData requestData = new RequestData(entityManager);
    DupReqCheckElement reqCheckElement = new DupReqCheckElement(null, null, false, false);
    MatchingResponse<ReqCheckResponse> response = null;
    assignRequestDataFromModel(entityManager, requestData, dupChkModel);
    if (dupChkModel.getIssuingCountry().equals(SystemLocation.BRAZIL) && (dupChkModel.getSubscenario().equals("GOVDI")
        || dupChkModel.getSubscenario().equals("GOVIN") || dupChkModel.getSubscenario().equals("COMME"))) {
      List<ReqCheckResponse> matches = new ArrayList<ReqCheckResponse>();
      List<Long> reqIds = new ArrayList<Long>();
      response = new MatchingResponse<ReqCheckResponse>();
      List<String> commonScenario = Arrays.asList("GOVDI", "GOVIN", "COMME");
      for (String scenario : commonScenario) {
        requestData.getData().setCustSubGrp(scenario);
        MatchingResponse<ReqCheckResponse> iterationResponse = reqCheckElement.getMatches(entityManager, requestData, null);
        if (iterationResponse != null && iterationResponse.getMatched()) {
          for (ReqCheckResponse res : iterationResponse.getMatches()) {
            if (!reqIds.contains(res.getReqId())) {
              matches.add(res);
              reqIds.add(res.getReqId());
            }
          }
        } else if (iterationResponse != null && !iterationResponse.getSuccess()) {
          return iterationResponse;
        } else if (iterationResponse == null) {
          response.setMatched(false);
          response.setSuccess(false);
          response.setMessage("Service Unavailable at the moment.");
        }
      }

      if (matches.size() > 0) {
        response.setMatched(true);
        response.setMatches(matches);
        response.setSuccess(true);
        response.setMessage(matches.size() + " matches found for the given search criteria.");
      } else {
        response.setMatched(false);
        response.setSuccess(false);
        response.setMessage("No matches for given search criteria.");
      }

    } else {
      assignRequestDataFromModel(entityManager, requestData, dupChkModel);
      response = reqCheckElement.getMatches(entityManager, requestData, null);
    }

    return response;
  }

  private void assignRequestDataFromModel(EntityManager entityManager, RequestData requestData, DuplicateCheckModel model) {
    Data data = requestData.getData();
    List<Addr> addresses = requestData.getAddresses();
    if (SystemLocation.BRAZIL.equals(model.getIssuingCountry())) {
      data.setCmrIssuingCntry(model.getIssuingCountry());
      if (StringUtils.isNotBlank(model.getSubscenario())) {
        data.setCustSubGrp(model.getSubscenario());
      }
      data.setCustGrp("LOCAL");
      Addr zs01 = requestData.createDummyAddress(entityManager, "ZS01", "1");
      zs01.setCustNm1(model.getCustomerName());
      zs01.setAddrTxt(model.getStreetLine1());
      zs01.setAddrTxt2(model.getStreetLine2());
      zs01.setCity1(model.getCity());
      zs01.setVat(model.getVat());
      zs01.setStateProv(model.getStateProv());
      zs01.setPostCd(model.getPostalCode());
      zs01.setLandCntry(model.getLandedCountry());
      addresses.add(zs01);

      if (StringUtils.isNotBlank(model.getVatzi01())) {
        Addr zi01 = requestData.createDummyAddress(entityManager, "ZI01", "1");
        zi01.setCustNm1(model.getCustomerName());
        zi01.setAddrTxt(model.getStreetLine1());
        zi01.setAddrTxt2(model.getStreetLine2());
        zi01.setCity1(model.getCity());
        zi01.setVat(model.getVatzi01());
        zi01.setStateProv(model.getStateProv());
        zi01.setPostCd(model.getPostalCode());
        zi01.setLandCntry(model.getLandedCountry());
        addresses.add(zi01);
      }
    }
  }

  protected String getKna1Details(String cmr, String cntry) throws Exception {
    String url = SystemConfiguration.getValue("CMR_SERVICES_URL");
    String mandt = SystemConfiguration.getValue("MANDT");
    String sql = ExternalizedQuery.getSql("AUTOMATION.GET.KNA1.BY_CMR");
    String name1 = "";
    String name2 = "";
    String name4 = "";
    sql = StringUtils.replace(sql, ":MANDT", "'" + mandt + "'");
    sql = StringUtils.replace(sql, ":ZZKV_CUSNO", "'" + cmr + "'");
    sql = StringUtils.replace(sql, ":KATR6", "'" + cntry + "'");

    String dbId = QueryClient.RDC_APP_ID;

    QueryRequest query = new QueryRequest();
    query.setSql(sql);
    query.addField("NAME1");
    query.addField("NAME2");
    query.addField("NAME4");

    LOG.debug("Getting existing KUNNNR_EXT details from RDc DB..");
    QueryClient client = CmrServicesFactory.getInstance().createClient(url, QueryClient.class);
    QueryResponse response = client.executeAndWrap(dbId, query, QueryResponse.class);
    if (response.isSuccess() && response.getRecords() != null && response.getRecords().size() != 0) {
      List<Map<String, Object>> records = response.getRecords();
      Map<String, Object> record = records.get(0);
      name1 = record.get("NAME1") != null ? record.get("NAME1").toString() : "";
      name2 = record.get("NAME2") != null ? record.get("NAME2").toString() : "";
      name4 = record.get("NAME4") != null ? record.get("NAME4").toString() : "";

    }
    return name1 + "\\n" + name2 + "\\n" + name4;
  }
}