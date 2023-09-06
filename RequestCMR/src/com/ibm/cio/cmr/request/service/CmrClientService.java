package com.ibm.cio.cmr.request.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.ui.ModelMap;

import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.model.ParamContainer;
import com.ibm.cio.cmr.request.model.requestentry.AddressModel;
import com.ibm.cio.cmr.request.model.requestentry.DNBData;
import com.ibm.cio.cmr.request.model.requestentry.ImportDNBData;
import com.ibm.cio.cmr.request.model.requestentry.RequestEntryModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.util.JpaManager;
import com.ibm.cio.cmr.request.util.RequestUtils;
import com.ibm.cio.cmr.request.util.SystemUtil;
import com.ibm.cio.cmr.request.util.geo.GEOHandler;
import com.ibm.cmr.services.client.CmrServicesFactory;
import com.ibm.cmr.services.client.DecisionServiceClient;
import com.ibm.cmr.services.client.DnBServiceClient;
import com.ibm.cmr.services.client.StandardCityServiceClient;
import com.ibm.cmr.services.client.TgmeClient;
import com.ibm.cmr.services.client.WtaasClient;
import com.ibm.cmr.services.client.dnb.DnBCompany;
import com.ibm.cmr.services.client.dnb.DnBDetailsRequest;
import com.ibm.cmr.services.client.dnb.DnBResponse;
import com.ibm.cmr.services.client.stdcity.StandardCityRequest;
import com.ibm.cmr.services.client.stdcity.StandardCityResponse;
import com.ibm.cmr.services.client.tgme.DunsData;
import com.ibm.cmr.services.client.tgme.DunsRequest;
import com.ibm.cmr.services.client.tgme.DunsResponse;
import com.ibm.cmr.services.client.wodm.bg.BuyingGroupInput;
import com.ibm.cmr.services.client.wodm.bg.BuyingGroupInputList;
import com.ibm.cmr.services.client.wodm.bg.BuyingGroupOutput;
import com.ibm.cmr.services.client.wodm.bg.BuyingGroupRequest;
import com.ibm.cmr.services.client.wodm.bg.BuyingGroupResponse;
import com.ibm.cmr.services.client.wodm.coverage.Coverage;
import com.ibm.cmr.services.client.wodm.coverage.CoverageInput;
import com.ibm.cmr.services.client.wodm.coverage.CoverageInputList;
import com.ibm.cmr.services.client.wodm.coverage.CoverageOutput;
import com.ibm.cmr.services.client.wodm.coverage.CoverageRequest;
import com.ibm.cmr.services.client.wodm.coverage.CoverageResponse;
import com.ibm.cmr.services.client.wodm.coverage.IBM;
import com.ibm.cmr.services.client.wodm.glc.GlcCoverageInput;
import com.ibm.cmr.services.client.wodm.glc.GlcCoverageInputList;
import com.ibm.cmr.services.client.wodm.glc.GlcRequest;
import com.ibm.cmr.services.client.wodm.glc.GlcResponse;
import com.ibm.cmr.services.client.wtaas.WtaasQueryRequest;
import com.ibm.cmr.services.client.wtaas.WtaasQueryResponse;

@Component
public class CmrClientService extends BaseSimpleService<Object> {

  private static final Logger LOG = Logger.getLogger(CmrClientService.class);

  @Override
  protected Object doProcess(EntityManager entityManager, HttpServletRequest request, ParamContainer params) throws Exception {
    String serviceType = (String) params.getParam("serviceType");
    ModelMap response = (ModelMap) params.getParam("model");

    if ("CITY".equals(serviceType)) {
      AddressModel address = (AddressModel) params.getParam("addr");
      RequestEntryModel data = (RequestEntryModel) params.getParam("data");
      getStandardCity(address, data.getCmrIssuingCntry(), response);
      return "";
    } else if ("DNB".equals(serviceType)) {
      String dunsNo = (String) params.getParam("dunsNo");
      if (StringUtils.isEmpty(dunsNo)) {
        response.put("success", "false");
        response.put("msg", "No DUNS No. specified.");
      } else {
        getDnBDetails(response, dunsNo);
      }
      return "";
    } else if ("WTAAS".equals(serviceType)) {
      String cmrNo = (String) params.getParam("cmrNo");
      String country = (String) params.getParam("country");
      if (StringUtils.isEmpty(cmrNo) || StringUtils.isEmpty(country)) {
        response.put("success", "false");
        response.put("msg", "No CMR No. or Country specified.");
      } else {
        getWTAASDetails(response, cmrNo, country);
      }
      return "";
    } else {
      Object reqId = params.getParam("reqId");
      String sql = ExternalizedQuery.getSql("CMRSERVICES.GET_ADDRESS");
      PreparedQuery query = new PreparedQuery(entityManager, sql);
      query.setParameter("REQ_ID", reqId);

      Addr mainAddr = new Addr();
      List<Addr> results = query.getResults(1, Addr.class);
      if (results != null && results.size() > 0) {
        mainAddr = results.get(0);
      }

      RequestEntryModel data = (RequestEntryModel) params.getParam("data");

      if ("COV".equals(serviceType)) {
        getCoverage(entityManager, mainAddr, data, response);
      } else if ("GBG".equals(serviceType)) {
        getBuyingGroup(entityManager, mainAddr, data, response);
      } else if ("GLC".equals(serviceType)) {
        getGlc(entityManager, mainAddr, data, response);
      } else if ("DUNS".equals(serviceType)) {
        getLHDuns(entityManager, mainAddr, data, response);
      } else if ("ALL".equals(serviceType)) {
        getGlc(entityManager, mainAddr, data, response);
        getBuyingGroup(entityManager, mainAddr, data, response);
        // getLHDuns(entityManager, mainAddr, data, response);
        getCoverage(entityManager, mainAddr, data, response);
      }

      return mainAddr;
    }
  }

  public void getAutoDnBDetails(ModelMap map, String dunsNo) throws Exception {
    String baseUrl = SystemConfiguration.getValue("CMR_SERVICES_URL");
    DnBServiceClient client = CmrServicesFactory.getInstance().createClient(baseUrl, DnBServiceClient.class);
    ImportDNBData dnbData = new ImportDNBData();
    DNBData dnbRecord = null;
    List<DNBData> itemsList = new ArrayList<DNBData>();

    DnBDetailsRequest details = new DnBDetailsRequest();
    details.setDunsNo(dunsNo);
    DnBResponse response = client.executeAndWrap(DnBServiceClient.DETAILS_APP_ID, details, DnBResponse.class);
    if (response.isSuccess()) {
      map.put("success", true);
      map.put("msg", null);
      // map.put("data", response.getData());
      if (response.getData() != null && response.getData().getResults() != null && response.getData().getResults().size() > 0) {
        for (DnBCompany dnbcompany : response.getData().getResults()) {
          dnbRecord = new DNBData();
          dnbRecord.setCmrAddrTypeCode("ZS01");
          dnbRecord.setCmrCity(StringUtils.isNotBlank(dnbcompany.getPrimaryCity()) ? dnbcompany.getPrimaryCity() : "");

          if (StringUtils.isNotBlank(dnbcompany.getPrimaryCountry())) {
            if (dnbcompany.getPrimaryCountry().length() > 2) {
              dnbRecord.setCmrCountryLanded(dnbcompany.getPrimaryCountry().substring(0, 2));
            } else {
              dnbRecord.setCmrCountryLanded(dnbcompany.getPrimaryCountry());
            }
          } else {
            dnbRecord.setCmrCountryLanded("");
          }

          dnbRecord.setCmrCounty(StringUtils.isNotBlank(dnbcompany.getPrimaryCounty()) ? dnbcompany.getPrimaryCounty() : "");
          dnbRecord.setCmrCustFax(StringUtils.isNotBlank(dnbcompany.getFaxNo()) ? dnbcompany.getFaxNo() : "");
          dnbRecord.setCmrCustPhone(StringUtils.isNotBlank(dnbcompany.getPhoneNo()) ? dnbcompany.getPhoneNo() : "");
          dnbRecord.setCmrDuns(StringUtils.isNotBlank(dnbcompany.getDunsNo()) ? dnbcompany.getDunsNo() : "");
          dnbRecord.setCmrIsic(StringUtils.isNotBlank(dnbcompany.getIbmIsic()) ? dnbcompany.getIbmIsic() : "");
          dnbRecord.setCmrName1Plain(StringUtils.isNotBlank(dnbcompany.getCompanyName()) ? dnbcompany.getCompanyName() : "");
          dnbRecord.setCmrPostalCode(StringUtils.isNotBlank(dnbcompany.getPrimaryPostalCode()) ? dnbcompany.getPrimaryPostalCode() : "");
          dnbRecord.setCmrState(StringUtils.isNotBlank(dnbcompany.getPrimaryStateCode()) ? dnbcompany.getPrimaryStateCode() : "");
          if ("CN".equalsIgnoreCase(dnbcompany.getPrimaryCountry())) {
            dnbRecord.setCmrState(StringUtils.isNotBlank(dnbcompany.getPrimaryStateName()) ? dnbcompany.getPrimaryStateName() : "");
          }
          dnbRecord.setCmrStreet(StringUtils.isNotBlank(dnbcompany.getPrimaryAddress()) ? dnbcompany.getPrimaryAddress() : "");
          dnbRecord.setCmrStreetAddressCont(StringUtils.isNotBlank(dnbcompany.getPrimaryAddressCont()) ? dnbcompany.getPrimaryAddressCont() : "");
          itemsList.add(dnbRecord);
        }
        dnbData.setItems(itemsList);
      }
      map.put("data", dnbData);
    } else {
      map.put("success", false);
      map.put("msg", response.getMsg());
      map.put("data", null);
    }
  }

  public void getDnBDetails(ModelMap map, String dunsNo) throws Exception {
    String baseUrl = SystemConfiguration.getValue("CMR_SERVICES_URL");
    DnBServiceClient client = CmrServicesFactory.getInstance().createClient(baseUrl, DnBServiceClient.class);
    DnBDetailsRequest details = new DnBDetailsRequest();
    details.setDunsNo(dunsNo);
    DnBResponse response = client.executeAndWrap(DnBServiceClient.DETAILS_APP_ID, details, DnBResponse.class);
    if (response.isSuccess()) {
      map.put("success", true);
      map.put("msg", null);
      map.put("data", response.getData());
    } else {
      map.put("success", false);
      map.put("msg", response.getMsg());
      map.put("data", null);
    }
  }

  public void getStandardCity(AddressModel address, String cmrIssuingCntry, ModelMap response) throws Exception {
    LOG.debug("Performing standard city service for Request " + address.getReqId());
    String baseUrl = SystemConfiguration.getValue("CMR_SERVICES_URL");
    LOG.debug(" - connecting to " + baseUrl);
    StandardCityServiceClient stdCityClient = CmrServicesFactory.getInstance().createClient(baseUrl, StandardCityServiceClient.class);
    StandardCityRequest stdCityRequest = new StandardCityRequest();
    stdCityRequest.setCountry(address.getLandCntry());
    stdCityRequest.setCity(address.getCity1());
    stdCityRequest.setState(address.getStateProv());
    stdCityRequest.setSysLoc(cmrIssuingCntry);
    stdCityRequest.setStreet1(address.getAddrTxt());
    stdCityRequest.setStreet2(address.getAddrTxt2());
    if ("US".equals(address.getLandCntry())) {
      stdCityRequest.setCountyName(address.getCountyName());
      stdCityRequest.setPostalCode(address.getPostCd());
    }
    stdCityClient.setStandardCityRequest(stdCityRequest);

    String req = new ObjectMapper().writeValueAsString(stdCityClient);
    LOG.debug(" - " + address.getReqId() + "std city request " + req);
    StandardCityResponse resp = stdCityClient.executeAndWrap(StandardCityResponse.class);
    if (resp != null) {
      String res = new ObjectMapper().writeValueAsString(resp);
      LOG.debug(" - " + address.getReqId() + " std city response " + res);
      response.put("result", resp);
    }
  }

  public boolean getCoverage(EntityManager entityManager, Addr addr, RequestEntryModel data, ModelMap response) throws Exception {
    try {
      CoverageRequest request = new CoverageRequest();
      request.setDecisionID(UUID.randomUUID().toString());

      CoverageInputList list = new CoverageInputList();

      CoverageInput coverage = new CoverageInput();

      coverage.setAffiliateGroup(data.getAffiliate());
      coverage.setClassification(data.getCustClass());
      coverage.setCompanyNumber(data.getCompany());
      coverage.setSegmentation("IBM".equals(data.getCmrOwner()) ? null : data.getCmrOwner());
      String isoCntryCd = SystemUtil.getISOCountryCode(data.getCmrIssuingCntry());
      LOG.debug("System Location: " + data.getCmrIssuingCntry() + " ISO Code: " + isoCntryCd);
      coverage.setCountryCode(isoCntryCd != null ? isoCntryCd : data.getCmrIssuingCntry());
      coverage.setDb2ID(data.getCmrNo());
      coverage.setEnterprise(data.getEnterprise());
      coverage.setGbQuadSectorTier(data.getClientTier());
      coverage.setINAC(data.getInacCd());
      coverage.setIndustryClass(data.getSubIndustryCd());
      coverage
          .setIndustryCode(data.getSubIndustryCd() != null && data.getSubIndustryCd().length() > 0 ? data.getSubIndustryCd().substring(0, 1) : null);
      coverage.setIndustrySolutionUnit(data.getIsuCd());
      coverage.setNationalTaxID(data.getTaxCd1());
      coverage.setSORTL(data.getSearchTerm());
      coverage.setUnISIC(data.getIsicCd());
      coverage.setSitePartyID(data.getSitePartyId());
      coverage.setCity(addr.getCity1());
      coverage.setCounty(addr.getCounty());
      coverage.setPostalCode(addr.getPostCd());
      if (!"''".equals(addr.getStateProv())) {
        coverage.setStatePrefectureCode(addr.getStateProv());
      }
      coverage.setPhysicalAddressCountry(addr.getLandCntry());

      if (response.get("buyingGroupID") != null) {
        String bg = (String) response.get("buyingGroupID");
        String gbg = (String) response.get("globalBuyingGroupID");
        IBM ibm = new IBM();
        ibm.setDomesticBuyingGroupID(bg);
        ibm.setGlobalBuyingGroupID(gbg);
        coverage.setIbm(ibm);
      }
      if (response.get("glcCode") != null) {
        coverage.setGeoLocationCode((String) response.get("glcCode"));
      }

      GEOHandler geoHandler = RequestUtils.getGEOHandler(data.getCmrIssuingCntry());
      if (geoHandler != null) {
        geoHandler.convertCoverageInput(entityManager, coverage, addr, data);
      }

      list.getCoverageInput().add(coverage);
      request.setCoverageInputList(list);

      DecisionServiceClient client = CmrServicesFactory.getInstance().createClient(SystemConfiguration.getValue("CMR_SERVICES_URL"),
          DecisionServiceClient.class);

      CoverageResponse responseObj = client.executeAndWrap(DecisionServiceClient.COVERAGE_APP_ID, request, CoverageResponse.class);
      if (responseObj.getCoverageOutputList() != null && responseObj.getCoverageOutputList().getCoverageOutput() != null
          && responseObj.getCoverageOutputList().getCoverageOutput().size() > 0) {
        CoverageOutput output = responseObj.getCoverageOutputList().getCoverageOutput().get(0);
        Coverage coverageOutput = null;

        if (output.getCoverage().size() == 1) {
          coverageOutput = output.getCoverage().get(0);
        } else {
          for (Coverage out : output.getCoverage()) {
            if ("Delegate".equals(out.getCoverageMode())) {
              coverageOutput = out;
              break;
            }
          }
        }

        if (coverageOutput == null) {
          if (output.getCoverage().size() > 0) {
            coverageOutput = output.getCoverage().get(0);
          } else {
            coverageOutput = new Coverage();
          }
        }

        LOG.debug("Coverage Found: " + coverageOutput.getCoverageType() + "-" + coverageOutput.getCoverageID() + " ("
            + coverageOutput.getCoverageMode() + ")");

        response.put("coverageType", coverageOutput.getCoverageType());
        response.put("coverageCountryCode", coverageOutput.getCoverageCountryCode());
        response.put("coverageMode", coverageOutput.getCoverageMode());
        response.put("coverageID", coverageOutput.getCoverageID());
        response.put("coverageDesc", coverageOutput.getCoverageDesc());
        response.put("clientTier", coverageOutput.getCoverageGbQuadSectorTier());
      } else {
        response.put("coverageType", null);
        response.put("coverageMode", null);
        response.put("coverageID", null);
        response.put("coverageDesc", null);
        response.put("coverageCountryCode", null);
        response.put("clientTier", null);
      }
      return true;
    } catch (Exception e) {
      response.put("coverageError", true);
      return false;
    }
  }

  public boolean getBuyingGroup(EntityManager entityManager, Addr addr, RequestEntryModel data, ModelMap response) throws Exception {
    try {
      BuyingGroupRequest request = new BuyingGroupRequest();
      request.setDecisionID(UUID.randomUUID().toString());
      BuyingGroupInputList list = new BuyingGroupInputList();
      BuyingGroupInput input = new BuyingGroupInput();

      input.setAffiliateGroup(data.getAffiliate());
      input.setCompanyNumber(data.getCompany());
      input.setCountryCode(addr.getLandCntry());
      input.setCustomerNumber(data.getCmrNo());
      input.setDb2ID(data.getCmrNo());
      input.setEnterprise(data.getEnterprise());
      input.setINAC(data.getInacCd());
      input.setIndustryClass(data.getSubIndustryCd());
      input.setNationalTaxID(data.getTaxCd1());
      input.setUnISIC(data.getIsicCd());
      input.setVatRegistrationNumber(data.getVat());
      list.getBuyingGroupInput().add(input);
      request.setBuyingGroupInputList(list);
      DecisionServiceClient client = CmrServicesFactory.getInstance().createClient(SystemConfiguration.getValue("CMR_SERVICES_URL"),
          DecisionServiceClient.class);

      BuyingGroupResponse responseObj = client.executeAndWrap(DecisionServiceClient.BUYING_GROUP_APP_ID, request, BuyingGroupResponse.class);
      if (responseObj.getBuyingGroupOutputList() != null && responseObj.getBuyingGroupOutputList().getBuyingGroupOutput() != null
          && responseObj.getBuyingGroupOutputList().getBuyingGroupOutput().size() > 0) {
        BuyingGroupOutput output = responseObj.getBuyingGroupOutputList().getBuyingGroupOutput().get(0);
        response.put("buyingGroupID", output.getBuyingGroupID());
        response.put("globalBuyingGroupID", output.getGlobalBuyingGroupID());
        response.put("buyingGroupDesc", output.getBuyingGroupDesc());
        response.put("globalBuyingGroupDesc", output.getGlobalBuyingGroupDesc());
        response.put("odmRuleID", output.getOdmRuleID());
      } else {
        response.put("buyingGroupID", null);
        response.put("globalBuyingGroupID", null);
        response.put("buyingGroupDesc", null);
        response.put("globalBuyingGroupDesc", null);
        response.put("odmRuleID", null);
      }
      return true;
    } catch (Exception e) {
      response.put("buyingGroupError", true);
      return false;
    }
  }

  public boolean getGlc(EntityManager entityManager, Addr addr, RequestEntryModel data, ModelMap response) throws Exception {
    try {
      GlcRequest request = new GlcRequest();
      GlcCoverageInputList list = new GlcCoverageInputList();
      GlcCoverageInput input = new GlcCoverageInput();

      input.setCountryCode(addr.getLandCntry());
      input.setPostalCode(addr.getPostCd());
      input.setCity(addr.getCity1());
      input.setStateProvinceCode("641".equals(data.getCmrIssuingCntry()) ? switchCNState(addr.getStateProv()) : addr.getStateProv());
      input.setSitePartyID(data.getSitePartyId());
      list.getCoverageInput().add(input);
      request.setCoverageInputList(list);

      DecisionServiceClient client = CmrServicesFactory.getInstance().createClient(SystemConfiguration.getValue("CMR_SERVICES_URL"),
          DecisionServiceClient.class);

      GlcResponse responseObj = client.executeAndWrap(DecisionServiceClient.GLC_APP_ID, request, GlcResponse.class);
      if (responseObj.getCoverageInputList() != null && responseObj.getCoverageInputList().getCoverageInput() != null
          && responseObj.getCoverageInputList().getCoverageInput().size() > 0) {
        GlcCoverageInput output = responseObj.getCoverageInputList().getCoverageInput().get(0);
        response.put("glcID", output.getGlcID());
        response.put("glcDesc", output.getGlcDesc());
        response.put("glcCode", output.getGlcCode());
        response.put("glcCountryCode", output.getCountryCode());
      } else {
        response.put("glcID", null);
        response.put("glcDesc", null);
        response.put("glcCode", null);
        response.put("glcCountryCode", null);
      }
      return true;
    } catch (Exception e) {
      response.put("glcError", true);
      return false;
    }

  }

  private String switchCNState(String stateProv) {
    if ("BJ".equals(stateProv)) {
      return "11";
    } else if ("TJ".equals(stateProv)) {
      return "12";
    } else if ("HE".equals(stateProv)) {
      return "13";
    } else if ("SX".equals(stateProv)) {
      return "14";
    } else if ("NM".equals(stateProv)) {
      return "15";
    } else if ("LN".equals(stateProv)) {
      return "21";
    } else if ("JL".equals(stateProv)) {
      return "22";
    } else if ("HL".equals(stateProv)) {
      return "23";
    } else if ("SH".equals(stateProv)) {
      return "31";
    } else if ("JS".equals(stateProv)) {
      return "32";
    } else if ("ZJ".equals(stateProv)) {
      return "33";
    } else if ("AH".equals(stateProv)) {
      return "34";
    } else if ("FJ".equals(stateProv)) {
      return "35";
    } else if ("JX".equals(stateProv)) {
      return "36";
    } else if ("SD".equals(stateProv)) {
      return "37";
    } else if ("HA".equals(stateProv)) {
      return "41";
    } else if ("HB".equals(stateProv)) {
      return "42";
    } else if ("HN".equals(stateProv)) {
      return "43";
    } else if ("GD".equals(stateProv)) {
      return "44";
    } else if ("GX".equals(stateProv)) {
      return "45";
    } else if ("HI".equals(stateProv)) {
      return "46";
    } else if ("CQ".equals(stateProv)) {
      return "50";
    } else if ("SC".equals(stateProv)) {
      return "51";
    } else if ("GZ".equals(stateProv)) {
      return "52";
    } else if ("YN".equals(stateProv)) {
      return "53";
    } else if ("XZ".equals(stateProv)) {
      return "54";
    } else if ("SN".equals(stateProv)) {
      return "61";
    } else if ("GS".equals(stateProv)) {
      return "62";
    } else if ("QH".equals(stateProv)) {
      return "63";
    } else if ("NX".equals(stateProv)) {
      return "64";
    } else if ("XJ".equals(stateProv)) {
      return "65";
    } else if ("TW".equals(stateProv)) {
      return "71";
    } else if ("HK".equals(stateProv)) {
      return "91";
    } else if ("MO".equals(stateProv)) {
      return "92";
    }
    return stateProv;
  }

  public boolean getLHDuns(EntityManager entityManager, Addr addr, RequestEntryModel data, ModelMap response) throws Exception {
    try {

      TgmeClient tgme = CmrServicesFactory.getInstance().createClient(SystemConfiguration.getValue("CMR_SERVICES_URL"), TgmeClient.class);
      DunsRequest duns = new DunsRequest();
      duns.setCustomerName(addr.getCustNm1());
      duns.setCountryCode(addr.getLandCntry());
      duns.setCity(addr.getCity1());
      duns.setPostalCode(addr.getPostCd());
      duns.setStreetAddress(addr.getAddrTxt());

      DunsResponse dunsResponse = tgme.executeAndWrap(TgmeClient.DUNS_APP_ID, duns, DunsResponse.class);
      if (dunsResponse.getStatus() == TgmeClient.STATUS_FAILED) {
        response.put("dunsError", true);
      } else {
        DunsData dunsData = dunsResponse.getData();
        if (dunsData != null) {
          response.put("dunsNo", dunsData.getDunsNo());
          response.put("duBusinessName", dunsData.getDuBusinessName());
          response.put("guBusinessName", dunsData.getGuBusinessName());
          response.put("dnbBusinessName", dunsData.getDnbBusinessName());
          response.put("dnbTradestyleName", dunsData.getDnbTradestyleName());
        } else {
          response.put("dunsNo", null);
          response.put("duBusinessName", null);
          response.put("guBusinessName", null);
          response.put("dnbBusinessName", null);
          response.put("dnbTradestyleName", null);
        }
      }
      return true;
    } catch (Exception e) {
      LOG.error("Error in processing DUNS", e);
      response.put("dunsError", true);
      return false;
    }
  }

  // STORY 1180239
  public Object getMRCFromSalesBranchOff(String issuingCountry, String sORTL) {
    List<String> queryResults;
    String oneResult = null;
    PreparedQuery query;
    EntityManager entityManager = JpaManager.getEntityManager();
    LOG.info("getMRCFromSalesBranchOff : starting process data values for import. . .");
    try {
      String prepQuery = ExternalizedQuery.getSql("IMPORT.LA.SALESBRANCHOFFICE.CODE");
      query = new PreparedQuery(entityManager, prepQuery);
      query.setParameter("SORTL", sORTL);
      query.setParameter("CNTRY", issuingCountry);
      queryResults = query.getResults(String.class);
      if (queryResults != null && !queryResults.isEmpty()) {
        if (queryResults.size() > 1) {
          // return blank if too many results
          oneResult = "";
        } else {
          // return only one record
          oneResult = queryResults.get(0);
        }
      } else {
        // return empty if results is null or empty
        oneResult = "";
      }
    } catch (Exception ex) {
      if (ex instanceof NullPointerException) {
        oneResult = "";
        LOG.debug("NULL IS RETURNED!");
      }
      LOG.error(ex.getMessage(), ex);
    } finally {
      entityManager.clear();
      entityManager.close();
    }
    return oneResult;
  }

  // STORY 1180239
  public Object getISUCode(String issuingCountry, String mrcCode) {
    List<String> queryResults;
    String oneResult = null;
    PreparedQuery query;
    EntityManager entityManager = JpaManager.getEntityManager();
    LOG.info("getISUCode : starting process data values for import. . .");
    try {
      String prepQuery = ExternalizedQuery.getSql("IMPORT.LA.MRC.ISU");
      query = new PreparedQuery(entityManager, prepQuery);
      query.setParameter("MRCCD", mrcCode);
      query.setParameter("CNTRY", issuingCountry);
      queryResults = query.getResults(String.class);
      if ((!queryResults.isEmpty() || queryResults != null) && queryResults.size() > 1) {
        // return blank if there are more than one results
        oneResult = "";
      } else {
        // return only one record
        oneResult = queryResults.get(0);
      }
    } catch (Exception ex) {
      if (ex instanceof NullPointerException) {
        oneResult = "";
        LOG.debug("NULL IS RETURNED. . .");
      }
      LOG.error(ex.getMessage(), ex);
    } finally {
      entityManager.clear();
      entityManager.close();
    }
    return oneResult;
  }

  // STORY 1164429
  public Object getClientTierCode(String mrcCode, String isuCode) {
    List<String> queryResults;
    String oneResult = null;
    PreparedQuery query;
    EntityManager entityManager = JpaManager.getEntityManager();
    LOG.info("getClientTierCode : starting process data values for import. . .");
    try {
      String prepQuery = ExternalizedQuery.getSql("IMPORT.LA.CLIENTTIER_CODE");
      query = new PreparedQuery(entityManager, prepQuery);
      query.setParameter("MRCCD", mrcCode);
      query.setParameter("ISUCD", isuCode);
      queryResults = query.getResults(String.class);
      if ((!queryResults.isEmpty() || queryResults != null) && queryResults.size() > 1) {
        // return blank if there are more than one results
        oneResult = "";
        if ("Q".equals(mrcCode) && "34".equals(isuCode)) {
          oneResult = queryResults.get(0);
        }
      } else if (queryResults.isEmpty() || queryResults == null) {
        oneResult = "";
      } else {
        // return only one record
        oneResult = queryResults.get(0);
      }
    } catch (Exception ex) {
      if (ex instanceof NullPointerException) {
        oneResult = "";
        LOG.debug("NULL IS RETURNED. . .");
      }
      LOG.error(ex.getMessage(), ex);
    } finally {
      entityManager.clear();
      entityManager.close();
    }
    return oneResult;
  }

  public Map<String, Object> getCrosTypesByCustType(String issingCntry, String custType) {
    HashMap<String, Object> crosTypeSubType = new HashMap<String, Object>();
    List<Object[]> queryResults;
    Object[] oneResult = null;
    PreparedQuery query;
    EntityManager entityManager = JpaManager.getEntityManager();
    LOG.info("getCrosTypesByCustType : starting process data values for import. . .");
    try {
      String prepQuery = ExternalizedQuery.getSql("GET_CUST_SCENARIO_BY_CUST_TYPE");
      query = new PreparedQuery(entityManager, prepQuery);
      query.setParameter("CUST_TYP", custType);
      query.setParameter("ISS_NUM", issingCntry);

      queryResults = query.getResults();

      if (queryResults != null && !queryResults.isEmpty() && queryResults.size() > 0) {
        oneResult = queryResults.get(0);
        crosTypeSubType.put("crosType", oneResult[0]);
        crosTypeSubType.put("crosSubTyp", oneResult[1]);
      }
    } catch (Exception ex) {
      if (ex instanceof NullPointerException) {
        LOG.debug("NULL IS RETURNED. . .");
      }
      LOG.error(ex.getMessage(), ex);
    } finally {
      entityManager.clear();
      entityManager.close();
    }
    return crosTypeSubType;
  }

  public String getDataEmail1(long reqId, boolean isServiceCall, EntityManager em) {
    List<String> queryResults;
    String oneResult = null;
    PreparedQuery query = null;
    EntityManager newEm = null;
    if (!isServiceCall && em == null) {
      newEm = JpaManager.getEntityManager();
    }
    LOG.info("getDataEmail1 : processing. . . .");
    try {
      String prepQuery = ExternalizedQuery.getSql("CONTACTINFO.GET_EMAIL1_FROM_DATA");
      if (!isServiceCall) {
        query = new PreparedQuery(newEm, prepQuery);
      } else {
        if (em != null) {
          query = new PreparedQuery(em, prepQuery);
        }
      }
      query.setParameter("REQ_ID", reqId);
      queryResults = query.getResults(String.class);
      if ((!queryResults.isEmpty() || queryResults != null) && queryResults.size() > 1) {
        // return blank if there are more than one results
        oneResult = "";
      } else if ((!queryResults.isEmpty() || queryResults != null) && queryResults.size() == 1) {
        // return only one record
        oneResult = queryResults.get(0);
      } else {
        oneResult = "";
      }
    } catch (Exception ex) {
      oneResult = "";
      LOG.error("getDataEmail1 : " + ex.getMessage(), ex);
    } finally {
      if (!isServiceCall) {
        newEm.clear();
        newEm.close();
      }
    }
    return oneResult;
  }

  public void doActualDeleteOnFirstSaveAfterTempLoad(long reqId, List<String> addrTypesToDelete, String prepSql, EntityManager entMan) {
    LOG.info("doActualDeleteOnFirstSaveAfterTempLoad : processing . . .");
    int loopCount = 0;
    String actualScript = "";
    int deleteResult = 0;
    String filterParam = "ADDR_FILT";
    try {
      actualScript = ExternalizedQuery.getSql(prepSql);
      PreparedQuery delete_addr = new PreparedQuery(entMan, actualScript);
      for (String param : addrTypesToDelete) {
        loopCount++;
        delete_addr.setParameter(filterParam + loopCount, param);
      }
      delete_addr.setParameter("REQ_ID", reqId);
      deleteResult = delete_addr.executeSql();
      loopCount = 0;
      LOG.debug("Deleted " + deleteResult + " from ADDR" + " for Request ID " + reqId);
      actualScript = ExternalizedQuery.getSql(prepSql + "_RDC");
      PreparedQuery delete_addrRdc = new PreparedQuery(entMan, actualScript);
      for (String param : addrTypesToDelete) {
        loopCount++;
        delete_addrRdc.setParameter(filterParam + loopCount, param);
      }
      delete_addrRdc.setParameter("REQ_ID", reqId);
      deleteResult = delete_addrRdc.executeSql();
      loopCount = 0;
      LOG.debug("Deleted " + deleteResult + " from ADDR_RDC" + " for Request ID " + reqId);
    } catch (Exception ex) {
      LOG.error("doActualDeleteOnFirstSaveAfterTempLoad : delete failed " + ex.getMessage());
    }
  }

  public void getWTAASDetails(ModelMap map, String cmrNo, String country) throws Exception {

    WtaasClient client = CmrServicesFactory.getInstance().createClient(SystemConfiguration.getValue("CMR_SERVICES_URL"), WtaasClient.class);

    WtaasQueryRequest request = new WtaasQueryRequest();
    request.setCmrNo(cmrNo);
    request.setCountry(country);

    WtaasQueryResponse response = client.executeAndWrap(WtaasClient.QUERY_ID, request, WtaasQueryResponse.class);
    if (response.isSuccess()) {
      map.put("success", true);
      map.put("msg", null);
      map.put("data", response.getData());
      if (response.getData() != null) {
        String status = (String) response.getData().get("Status");
        map.put("Status", status);
        map.put("Error_Msg", response.getData().get("Error_Msg"));
      }
    } else {
      map.put("success", false);
      map.put("msg", response.getMsg());
      map.put("data", null);
    }
  }
}
