/**
 * 
 */
package com.ibm.cio.cmr.request.util;

import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.springframework.ui.ModelMap;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.model.CompanyRecordModel;
import com.ibm.cio.cmr.request.model.requestentry.FindCMRRecordModel;
import com.ibm.cio.cmr.request.model.requestentry.FindCMRResultModel;
import com.ibm.cio.cmr.request.service.CmrClientService;
import com.ibm.cio.cmr.request.util.dnb.DnBUtil;
import com.ibm.cmr.services.client.AutomationServiceClient;
import com.ibm.cmr.services.client.CmrServicesFactory;
import com.ibm.cmr.services.client.MatchingServiceClient;
import com.ibm.cmr.services.client.ServiceClient.Method;
import com.ibm.cmr.services.client.automation.AutomationResponse;
import com.ibm.cmr.services.client.automation.cn.CNRequest;
import com.ibm.cmr.services.client.automation.cn.CNResponse;
import com.ibm.cmr.services.client.dnb.DnbData;
import com.ibm.cmr.services.client.dnb.DnbOrganizationId;
import com.ibm.cmr.services.client.matching.MatchingResponse;
import com.ibm.cmr.services.client.matching.cmr.DuplicateCMRCheckRequest;
import com.ibm.cmr.services.client.matching.cmr.DuplicateCMRCheckResponse;
import com.ibm.cmr.services.client.matching.dnb.DnBMatchingResponse;
import com.ibm.cmr.services.client.matching.gbg.GBGFinderRequest;
import com.ibm.cmr.services.client.matching.request.ReqCheckRequest;
import com.ibm.cmr.services.client.matching.request.ReqCheckResponse;
import com.ibm.json.java.JSONObject;

/**
 * Utility to find company records via FindCMR or D&B. Also connects to FindGOE
 * to determine GOE status of the company
 * 
 * @author JeffZAMORA
 *
 */
public class CompanyFinder {

  private static final Logger LOG = Logger.getLogger(CompanyFinder.class);

  private static final CharsetEncoder asciiEncoder = Charset.forName("ISO-8859-1").newEncoder();

  private static final List<String> LOCAL_LANG_COUNTRIES = Arrays.asList("229", "358", "359", "363", "602", "607", "608", "610", "613", "614", "616",
      "618", "621", "624", "626", "631", "635", "637", "638", "641", "642", "644", "645", "647", "649", "651", "661", "666", "667", "668", "677",
      "678", "683", "692", "693", "694", "695", "699", "702", "704", "705", "706", "707", "708", "711", "718", "724", "725", "726", "729", "736",
      "738", "740", "741", "744", "749", "754", "755", "756", "758", "759", "760", "762", "764", "766", "767", "768", "772", "778", "780", "781",
      "782", "787", "788", "796", "804", "805", "806", "808", "815", "818", "820", "821", "822", "823", "826", "831", "832", "834", "838", "840",
      "846", "848", "849", "852", "856", "857", "858", "862", "864", "865", "866", "881", "889", "897");

  /**
   * Searches for the records matching the criteria on the
   * {@link CompanyRecordModel}
   * 
   * @param searchModel
   * @return
   * @throws Exception
   */
  public static List<CompanyRecordModel> findCompanies(CompanyRecordModel searchModel) throws Exception {
    List<CompanyRecordModel> matches = new ArrayList<CompanyRecordModel>();
    if (!StringUtils.isBlank(searchModel.getCmrNo())) {
      matches.addAll(findCMRsViaService(searchModel.getIssuingCntry(), searchModel.getCmrNo(), 10, null));
    } else if (isLatin(searchModel.getName())) {
      matches.addAll(findCMRs(searchModel));
      // CREATCMR-7388 - always append D&B results
      boolean searchDnb = true;

      // List<String> lowLevelMatches = Arrays.asList("F3", "F4", "F5", "VAT",
      // "DUNS");
      // if (!matches.isEmpty()) {
      // for (CompanyRecordModel cmrMatch : matches) {
      // if (lowLevelMatches.contains(cmrMatch.getMatchGrade())) {
      // searchDnb = true;
      // break;
      // }
      // }
      // }
      matches.addAll(findRequests(searchModel));

      if (matches.isEmpty() || searchDnb || "Y".equals(searchModel.getAddDnBMatches())) {
        matches.addAll(searchDnB(searchModel));
      }

      if (matches.isEmpty() && !StringUtils.isBlank(searchModel.getName()) && LOCAL_LANG_COUNTRIES.contains(searchModel.getIssuingCntry())) {
        // try non latin if country has local lang data
        LOG.debug("Trying non-latin search on inputs..");
        matches.addAll(findCMRsNonLatin(searchModel));
      }

    } else {
      LOG.debug("Finding CMRs using local language..");
      matches.addAll(findCMRsNonLatin(searchModel));
      if ("Y".equals(searchModel.getAddDnBMatches())) {
        matches.addAll(searchDnB(searchModel));
      }
    }

    Collections.sort(matches);
    List<String> keys = new ArrayList<String>();
    List<CompanyRecordModel> cleaned = new ArrayList<CompanyRecordModel>();
    for (CompanyRecordModel rec : matches) {
      String key = rec.getRecType() + (CompanyRecordModel.REC_TYPE_CMR.equals(rec.getRecType()) ? rec.getCmrNo()
          : (CompanyRecordModel.REC_TYPE_DNB.equals(rec.getRecType()) ? rec.getDunsNo() : rec.getCmrNo()));
      if (!keys.contains(key)) {
        cleaned.add(rec);
        keys.add(key);
      }
    }

    return cleaned;
  }

  private static boolean isLowLevelMatched(List<CompanyRecordModel> matches) {
    List<String> lowLevelMatches = Arrays.asList("F3", "F4", "F5", "VAT", "DUNS");

    return Optional.ofNullable(matches).orElseGet(Collections::emptyList).parallelStream()
        .anyMatch(cmrMatch -> lowLevelMatches.contains(cmrMatch.getMatchGrade()));
  }

  /**
   * Connects to FindCMR and checks for existing CMRs matching the criteria
   * 
   * @param searchModel
   * @return
   * @throws Exception
   */
  private static List<CompanyRecordModel> findCMRs(CompanyRecordModel searchModel) throws Exception {

    List<CompanyRecordModel> cmrMatches = new ArrayList<CompanyRecordModel>();

    List<String> searchAddrTypes = new ArrayList<String>();
    searchAddrTypes.add(CmrConstants.ADDR_TYPE.ZS01.toString());
    if (SystemLocation.UNITED_STATES.equals(searchModel.getIssuingCntry())) {
      searchAddrTypes.add(CmrConstants.ADDR_TYPE.ZI01.toString());
    }
    for (String addrType : searchAddrTypes) {
      // build the criteria
      DuplicateCMRCheckRequest request = new DuplicateCMRCheckRequest();
      request.setIssuingCountry(searchModel.getIssuingCntry());
      request.setLandedCountry(searchModel.getCountryCd());
      if (!StringUtils.isBlank(searchModel.getName())) {
        request.setCustomerName(searchModel.getName());
      } else if (!StringUtils.isBlank(searchModel.getVat()) || !StringUtils.isBlank(searchModel.getTaxCd1())) {
        request.setCustomerName("xxxx");
      }
      if (!StringUtils.isBlank(searchModel.getStreetAddress1())) {
        request.setStreetLine1(searchModel.getStreetAddress1());
      } else if (!StringUtils.isBlank(searchModel.getVat()) || !StringUtils.isBlank(searchModel.getTaxCd1())) {
        request.setStreetLine1("xxxx");
      }
      request.setStreetLine2(StringUtils.isBlank(searchModel.getStreetAddress2()) ? "" : searchModel.getStreetAddress2());
      if (!StringUtils.isBlank(searchModel.getCity())) {
        request.setCity(searchModel.getCity());
      } else if (!StringUtils.isBlank(searchModel.getVat()) || !StringUtils.isBlank(searchModel.getTaxCd1())) {
        request.setCity("xxxx");
      }
      request.setStateProv(searchModel.getStateProv());
      request.setPostalCode(searchModel.getPostCd());
      if (StringUtils.isNotBlank(searchModel.getVat())) {
        request.setVat(searchModel.getVat());
      } else if (StringUtils.isNotBlank(searchModel.getTaxCd1())) {
        request.setVat(searchModel.getTaxCd1());
      }
      request.setAddrType(addrType);
      request.setUsRestrictTo(searchModel.getRestrictTo());
      if (searchModel.isPoolRecord()) {
        request.setIncludeDeleted("Y");
      }

      if (StringUtils.isNotBlank(searchModel.getCied())) {
        request.setPpsCeId(searchModel.getCied());
      }

      LOG.debug("Connecting to CMR matching service for " + request.getIssuingCountry() + " - " + request.getCustomerName());
      // connect to the duplicate CMR check service
      MatchingServiceClient client = CmrServicesFactory.getInstance().createClient(SystemConfiguration.getValue("BATCH_SERVICES_URL"),
          MatchingServiceClient.class);
      client.setReadTimeout(1000 * 60 * 5);

      if (searchModel.isPoolRecord()) {
        LOG.debug("Pool CMR search criteria:");
        LOG.debug(new ObjectMapper().writeValueAsString(request));
      }

      JSONObject retObj = client.execute(MatchingServiceClient.CMR_SERVICE_ID, request);
      ObjectMapper mapper = new ObjectMapper();
      String rawJson = mapper.writeValueAsString(retObj);
      TypeReference<MatchingResponse<DuplicateCMRCheckResponse>> typeRef = new TypeReference<MatchingResponse<DuplicateCMRCheckResponse>>() {
      };
      MatchingResponse<DuplicateCMRCheckResponse> res = mapper.readValue(rawJson, typeRef);
      if (res != null && res.getSuccess() && res.getMatched()) {
        for (DuplicateCMRCheckResponse record : res.getMatches()) {
          CompanyRecordModel match = new CompanyRecordModel();
          match.setCity(record.getCity());
          match.setCmrNo(record.getCmrNo());
          match.setCountryCd(record.getLandedCountry());
          match.setDunsNo(record.getDunsNo());
          match.setIssuingCntry(record.getIssuingCntry());
          match.setName(record.getCustomerName().replaceAll("@", " "));
          match.setPostCd(record.getPostalCode());
          match.setRecType(CompanyRecordModel.REC_TYPE_CMR);
          match.setStateProv(record.getStateProv());
          match.setStreetAddress1(record.getStreetLine1());
          match.setStreetAddress2(record.getStreetLine2());
          match.setVat(record.getVat());
          match.setMatchGrade("V".equals(record.getMatchGrade()) ? "VAT" : record.getMatchGrade());

          match.setAltName(record.getAltName());
          match.setAltCity(record.getAltCity());
          match.setAltStreet(record.getAltStreet());

          match.setRestrictTo(record.getUsRestrictTo());
          match.setRevenue(record.getRevenue());
          if (!StringUtils.isBlank(searchModel.getVat()) || !StringUtils.isBlank(searchModel.getTaxCd1())) {
            match.setOrgIdMatch(searchModel.getVat().equals(match.getVat()) || searchModel.getTaxCd1().equals(match.getTaxCd1()));
          }
          cmrMatches.add(match);
        }

        CompanyRecordModel highest = null;
        double high = 0;
        for (CompanyRecordModel match : cmrMatches) {
          if (match.getRevenue() > high) {
            highest = match;
            high = match.getRevenue();
          }
        }
        if (highest != null) {
          highest.setHighestRevenue(true);
        }
        break;
      }

    }

    return cmrMatches;
  }

  /**
   * Connects to FindCMR web service and reformats the results into a list of
   * {@link CompanyRecordModel} objects
   * 
   * @param issuingCountry
   * @param cmrNo
   * @return
   * @throws CmrException
   */
  private static List<CompanyRecordModel> findCMRsViaService(String issuingCountry, String cmrNo, int rows, String dunsList) throws CmrException {
    List<CompanyRecordModel> cmrRecords = new ArrayList<CompanyRecordModel>();
    FindCMRResultModel result = getCMRDetails(issuingCountry, cmrNo, rows, null, dunsList);
    if (result != null && result.getItems() != null && !result.getItems().isEmpty()) {
      Collections.sort(result.getItems());
      for (FindCMRRecordModel record : result.getItems()) {
        CompanyRecordModel cmr = new CompanyRecordModel();
        cmr.setCity(record.getCmrCity());
        cmr.setCmrNo(record.getCmrNum());
        cmr.setCountryCd(record.getCmrCountryLanded());
        cmr.setDunsNo(record.getCmrDuns());
        cmr.setGoeStatus(record.getCmrGOEIndicator());
        cmr.setIssuingCntry(issuingCountry);
        cmr.setMatchGrade("A");
        cmr.setName(record.getCmrName());
        cmr.setPostCd(record.getCmrPostalCode());
        cmr.setRecType(CompanyRecordModel.REC_TYPE_CMR);
        cmr.setStateProv(record.getCmrState());
        cmr.setStreetAddress1(record.getCmrStreetAddress());
        cmr.setStreetAddress2(record.getCmrStreetAddressCont());
        cmr.setVat(record.getCmrVat());
        cmr.setAltName(record.getCmrIntlName1() + (record.getCmrIntlName2() != null ? record.getCmrIntlName2() : ""));
        cmr.setAltStreet(record.getCmrIntlAddress() + (record.getCmrIntlName3() != null ? record.getCmrIntlName3() : ""));
        cmr.setAltCity(record.getCmrIntlCity1());
        cmr.setRestrictTo(record.getUsCmrRestrictTo());
        cmrRecords.add(cmr);
        if (dunsList == null) {
          // only import 1 record for non duns matching
          break;
        }
      }
    }
    return cmrRecords;
  }

  /**
   * Connects to FindCMR and checks for existing CMRs matching the criteria
   * 
   * @param searchModel
   * @return
   * @throws Exception
   */
  private static List<CompanyRecordModel> findCMRsNonLatin(CompanyRecordModel searchModel) throws Exception {

    List<CompanyRecordModel> cmrMatches = new ArrayList<CompanyRecordModel>();

    String extraParams = "addressType=ZS01&showCmrType=R,P";

    FindCMRResultModel results = SystemUtil.findCMRsAltLang(searchModel.getIssuingCntry(), 500, searchModel.getName(),
        searchModel.getStreetAddress1(), searchModel.getCity(), extraParams);

    // do a secondary search switching city/street
    if (results == null || results.getItems() == null || results.getItems().isEmpty()) {
      results = SystemUtil.findCMRsAltLang(searchModel.getIssuingCntry(), 500, searchModel.getName(), searchModel.getCity(),
          searchModel.getStreetAddress1(), extraParams);
    }
    // try no street
    if (results == null || results.getItems() == null || results.getItems().isEmpty()) {
      results = SystemUtil.findCMRsAltLang(searchModel.getIssuingCntry(), 500, searchModel.getName(), searchModel.getCity(), null, extraParams);
    }
    if (results == null || results.getItems() == null || results.getItems().isEmpty()) {
      results = SystemUtil.findCMRsAltLang(searchModel.getIssuingCntry(), 500, searchModel.getName(), null, searchModel.getCity(), extraParams);
    }

    // try no city
    if (results == null || results.getItems() == null || results.getItems().isEmpty()) {
      results = SystemUtil.findCMRsAltLang(searchModel.getIssuingCntry(), 500, searchModel.getName(), null, searchModel.getStreetAddress1(),
          extraParams);
    }
    if (results == null || results.getItems() == null || results.getItems().isEmpty()) {
      results = SystemUtil.findCMRsAltLang(searchModel.getIssuingCntry(), 500, searchModel.getName(), searchModel.getStreetAddress1(), null,
          extraParams);
    }

    if (results != null && !results.getItems().isEmpty()) {
      for (FindCMRRecordModel record : results.getItems()) {
        CompanyRecordModel cmr = new CompanyRecordModel();
        cmr.setCity(record.getCmrCity());
        cmr.setCmrNo(record.getCmrNum());
        cmr.setCountryCd(record.getCmrCountryLanded());
        cmr.setDunsNo(record.getCmrDuns());
        cmr.setGoeStatus(record.getCmrGOEIndicator());
        cmr.setIssuingCntry(record.getCmrIssuedBy());
        cmr.setMatchGrade("LANG");
        cmr.setName(record.getCmrName());
        cmr.setPostCd(record.getCmrPostalCode());
        cmr.setRecType(CompanyRecordModel.REC_TYPE_CMR);
        cmr.setStateProv(record.getCmrState());
        cmr.setStreetAddress1(record.getCmrStreetAddress());
        cmr.setStreetAddress2(record.getCmrStreetAddressCont());
        cmr.setVat(record.getCmrVat());

        cmr.setAltName(record.getCmrIntlName1() + (record.getCmrIntlName2() != null ? record.getCmrIntlName2() : ""));
        cmr.setAltStreet(record.getCmrIntlAddress() + (record.getCmrIntlName3() != null ? record.getCmrIntlName3() : ""));
        cmr.setAltCity(record.getCmrIntlCity1());

        cmr.setCied(record.getCmrPpsceid());

        if (!StringUtils.isBlank(searchModel.getVat()) || !StringUtils.isBlank(searchModel.getTaxCd1())) {
          cmr.setOrgIdMatch(searchModel.getVat().equals(cmr.getVat()) || searchModel.getTaxCd1().equals(cmr.getTaxCd1()));
        }

        cmrMatches.add(cmr);
      }
    }
    return cmrMatches;
  }

  /**
   * Checks any existing requests similar to the search criteria
   * 
   * @param searchModel
   * @return
   * @throws InvocationTargetException
   * @throws IllegalArgumentException
   * @throws IllegalAccessException
   * @throws InstantiationException
   * @throws SecurityException
   * @throws NoSuchMethodException
   */
  private static List<CompanyRecordModel> findRequests(CompanyRecordModel searchModel) throws Exception {
    List<CompanyRecordModel> reqMatches = new ArrayList<CompanyRecordModel>();

    // connect to the service
    MatchingResponse<ReqCheckResponse> response = new MatchingResponse<ReqCheckResponse>();
    MatchingServiceClient client = CmrServicesFactory.getInstance().createClient(SystemConfiguration.getValue("BATCH_SERVICES_URL"),
        MatchingServiceClient.class);
    client.setReadTimeout(1000 * 60 * 5);

    ReqCheckRequest request = new ReqCheckRequest();
    request.setReqId(0);
    request.setAddrType("ZS01");
    request.setCity(searchModel.getCity());
    request.setCustomerName(searchModel.getName());
    request.setIssuingCountry(searchModel.getIssuingCntry());
    request.setLandedCountry(searchModel.getCountryCd());
    request.setPostalCode(searchModel.getPostCd());
    request.setStateProv(searchModel.getStateProv());
    request.setStreetLine1(searchModel.getStreetAddress1());
    request.setStreetLine2(searchModel.getStreetAddress2());
    request.setVat(searchModel.getVat());

    LOG.debug("Executing Duplicate Request Check for quick search..");
    MatchingResponse<?> rawResponse = client.executeAndWrap(MatchingServiceClient.REQ_SERVICE_ID, request, MatchingResponse.class);
    ObjectMapper mapper = new ObjectMapper();
    String json = mapper.writeValueAsString(rawResponse);
    TypeReference<MatchingResponse<ReqCheckResponse>> ref = new TypeReference<MatchingResponse<ReqCheckResponse>>() {
    };
    MatchingResponse<ReqCheckResponse> res = mapper.readValue(json, ref);
    if (res != null && res.getMatched()) {
      LOG.debug("Found " + res.getMatches().size() + " requests");
      CompanyRecordModel match = null;
      for (ReqCheckResponse duplicateReq : res.getMatches()) {
        if (reqMatches.size() < 5) {
          match = new CompanyRecordModel();
          match.setCmrNo(duplicateReq.getReqId() + "");
          match.setCity(duplicateReq.getCity());
          match.setCountryCd(duplicateReq.getLandedCountry());
          match.setIssuingCntry(duplicateReq.getIssuingCntry());
          match.setMatchGrade(duplicateReq.getMatchGrade() + "");
          match.setName(duplicateReq.getCustomerName());
          match.setPostCd(duplicateReq.getPostalCode());
          match.setRecType(CompanyRecordModel.REC_TYPE_REQUEST);
          match.setStateProv(duplicateReq.getStateProv());
          match.setStreetAddress1(duplicateReq.getStreetLine1());
          match.setStreetAddress2(duplicateReq.getStreetLine2());
          match.setRestrictTo(duplicateReq.getUsRestrictTo());
          match.setVat(duplicateReq.getVat());
          match.setTaxCd1(duplicateReq.getTaxCd1());

          reqMatches.add(match);
        } else {
          break;
        }
      }
    }
    return reqMatches;
  }

  /**
   * Connects to the extended D&B matching service to get high quality D&B
   * matches
   * 
   * @param searchModel
   * @return
   * @throws Exception
   */
  private static List<CompanyRecordModel> searchDnB(CompanyRecordModel searchModel) throws Exception {

    List<CompanyRecordModel> dnbMatches = new ArrayList<CompanyRecordModel>();

    GBGFinderRequest request = new GBGFinderRequest();
    request.setMandt(SystemConfiguration.getValue("MANDT"));
    if (StringUtils.isNotBlank(searchModel.getVat())) {
      request.setOrgId(searchModel.getVat());
    }
    if (StringUtils.isBlank(searchModel.getVat()) && !StringUtils.isBlank(searchModel.getTaxCd1())) {
      request.setOrgId(searchModel.getTaxCd1());
    }
    request.setCity(searchModel.getCity());
    request.setCustomerName(searchModel.getName());
    request.setStreetLine1(searchModel.getStreetAddress1());
    request.setStreetLine2(searchModel.getStreetAddress2());
    request.setLandedCountry(searchModel.getCountryCd());
    request.setPostalCode(searchModel.getPostCd());
    request.setStateProv(searchModel.getStateProv());
    request.setOrgId(searchModel.getVat());
    if (StringUtils.isBlank(request.getOrgId())) {
      request.setOrgId(searchModel.getTaxCd1());
    }
    request.setMinConfidence("7");
    request.setIncludeOob(true);

    LOG.debug("Connecting to D&B matching service for " + request.getLandedCountry() + " - " + request.getCustomerName());
    // connect to the duplicate CMR check service
    MatchingServiceClient client = CmrServicesFactory.getInstance().createClient(SystemConfiguration.getValue("BATCH_SERVICES_URL"),
        MatchingServiceClient.class);
    client.setReadTimeout(1000 * 60 * 5);

    JSONObject retObj = client.execute(MatchingServiceClient.DNB_SERVICE_ID, request);
    ObjectMapper mapper = new ObjectMapper();
    String rawJson = mapper.writeValueAsString(retObj);
    TypeReference<MatchingResponse<DnBMatchingResponse>> typeRef = new TypeReference<MatchingResponse<DnBMatchingResponse>>() {
    };
    MatchingResponse<DnBMatchingResponse> res = mapper.readValue(rawJson, typeRef);

    StringBuilder sbDuns = new StringBuilder();
    if (res != null && res.getSuccess() && res.getMatched()) {
      for (DnBMatchingResponse record : res.getMatches()) {
        CompanyRecordModel match = new CompanyRecordModel();
        match.setCity(record.getDnbCity());
        match.setCountryCd(record.getDnbCountry());
        match.setDunsNo(record.getDunsNo());
        sbDuns.append(sbDuns.length() > 0 ? "," : "");
        sbDuns.append(record.getDunsNo());
        match.setIssuingCntry(searchModel.getIssuingCntry());
        match.setName(record.getDnbName());
        match.setPostCd(record.getDnbPostalCode());
        match.setRecType(CompanyRecordModel.REC_TYPE_DNB);
        match.setStateProv(record.getDnbStateProv());
        match.setStreetAddress1(record.getDnbStreetLine1());
        match.setStreetAddress2(record.getDnbStreetLine2());
        match.setMatchGrade(StringUtils.leftPad("" + record.getConfidenceCode(), 2, '0'));
        if (record.getOrgIdDetails() != null && !record.getOrgIdDetails().isEmpty()) {
          StringBuilder sb = new StringBuilder();
          for (DnbOrganizationId orgId : record.getOrgIdDetails()) {
            if (DnBUtil.isRelevant(searchModel.getCountryCd(), orgId)) {
              sb.append(sb.length() > 0 ? ", " : "");
              sb.append(orgId.getOrganizationIdCode() + " (" + orgId.getOrganizationIdType() + ")");
            }
          }
          match.setVat(sb.toString());
        }
        match.setOperStatusCode(record.getOperStatusCode());
        match.setOrgIdMatch("Y".equals(record.getOrgIdMatch()));
        dnbMatches.add(match);
      }
    }
    if (sbDuns.length() > 0 && !"Y".equals(searchModel.getAddDnBMatches())) {
      // try to do a secondary matching against FindCMR using DUNS Information
      LOG.debug("Trying to find CMRs with DUNS: " + sbDuns.toString());
      String params = "showCmrType=R,P&addressType=" + ("897".equals(searchModel.getIssuingCntry()) ? "ZS01,ZI01" : "ZS01") + "&dunsNumberList="
          + sbDuns.toString();
      if (!StringUtils.isBlank(searchModel.getRestrictTo())) {
        params += "&usRestrictTo=" + searchModel.getRestrictTo();
      }
      List<CompanyRecordModel> cmrs = findCMRsViaService(searchModel.getIssuingCntry(), null, 3, params);
      if (cmrs != null) {
        for (CompanyRecordModel cmr : cmrs) {
          cmr.setMatchGrade("DUNS");
        }
        dnbMatches.addAll(cmrs);
      }
    }

    return dnbMatches;
  }

  /**
   * Connects to the FindCMR web service to get all records under the issuing
   * country and CMR No.
   * 
   * @param issuingCountry
   * @param cmrNo
   * @return
   * @throws CmrException
   */
  public static FindCMRResultModel getCMRDetails(String issuingCountry, String cmrNo, int maxRows, String searchCountry, String overrideParams)
      throws CmrException {
    LOG.debug("Getting CMR details for " + issuingCountry + " - " + cmrNo);
    try {
      FindCMRResultModel results = SystemUtil.findCMRs(cmrNo, issuingCountry, maxRows, searchCountry, overrideParams);
      if (results != null && results.getItems() != null) {
        Collections.sort(results.getItems());
      }
      return results;
    } catch (Exception e) {
      LOG.error("Error in connecting to FindCMR", e);
      return null;
    }
  }

  /**
   * Connects to the D&B details service to get the details of the DUNS No.
   * 
   * @param dunsNo
   * @return
   * @throws Exception
   */
  public static DnbData getDnBDetails(String dunsNo) throws Exception {
    LOG.debug("Getting D&B details for " + dunsNo);
    ModelMap map = new ModelMap();
    CmrClientService service = new CmrClientService();
    try {
      service.getDnBDetails(map, dunsNo);
      Boolean success = (Boolean) map.get("success");
      if (success != null && success) {
        return (DnbData) map.get("data");
      }
    } catch (Exception e) {
      LOG.error("Error in connecting to D&B details service", e);
    }
    return null;
  }

  /**
   * Checks if the string can be encoded in pure ISO Latin Characters
   * 
   * @param text
   * @return
   */
  public static boolean isLatin(String text) {
    if (text == null || "".equals(text)) {
      // by default, if the input is empty we consider it as Latin
      return true;
    }
    // check if the number of bytes that encodes the text equals the number of
    // characters
    boolean isLatinResult = asciiEncoder.canEncode(text);
    return isLatinResult;
  }

  /**
   * Get chinese name and address from China's TianYanCha Api
   * 
   * @param searchModel
   * @return
   * @throws Exception
   */
  public static AutomationResponse<CNResponse> getCNApiInfo(CompanyRecordModel searchModel, String key) throws Exception {
    AutomationServiceClient client = CmrServicesFactory.getInstance().createClient(SystemConfiguration.getValue("BATCH_SERVICES_URL"),
        AutomationServiceClient.class);
    client.setReadTimeout(1000 * 60 * 5);
    client.setRequestMethod(Method.Get);

    CNRequest request = new CNRequest();
    switch (key) {
    case "TAXCD":
      request.setKeyword(searchModel.getTaxCd1());
      break;
    case "ALTNAME":
      request.setKeyword(searchModel.getAltName());
      break;
    case "DEFAULT":
      request.setKeyword(searchModel.getTaxCd1());
      break;
    }

    LOG.debug(request + request.getKeyword());

    LOG.debug("Connecting to the CNValidation service at " + SystemConfiguration.getValue("BATCH_SERVICES_URL"));
    AutomationResponse<?> rawResponse = client.executeAndWrap(AutomationServiceClient.CN_TYC_SERVICE_ID, request, AutomationResponse.class);
    ObjectMapper mapper = new ObjectMapper();
    String json = mapper.writeValueAsString(rawResponse);
    TypeReference<AutomationResponse<CNResponse>> ref = new TypeReference<AutomationResponse<CNResponse>>() {
    };
    return mapper.readValue(json, ref);
  }

  /**
   * Get chinese name and address from China's TianYanCha Api
   * 
   * @param searchModel
   * @return
   * @throws Exception
   */
  public static AutomationResponse<CNResponse> getCNApiInfo4GBG(CompanyRecordModel searchModel, String key) throws Exception {
    AutomationServiceClient client = CmrServicesFactory.getInstance().createClient(SystemConfiguration.getValue("BATCH_SERVICES_URL"),
        AutomationServiceClient.class);
    client.setReadTimeout(1000 * 60 * 5);
    client.setRequestMethod(Method.Get);

    CNRequest request = new CNRequest();
    switch (key) {
    case "TAXCD":
      request.setKeyword(searchModel.getTaxCd1());
      break;
    case "ALTNAME":
      request.setKeyword(searchModel.getAltName());
      break;
    case "DEFAULT":
      request.setKeyword(searchModel.getTaxCd1());
      break;
    }
    request.setQueryType("2");

    System.out.println(request + request.getKeyword());

    LOG.debug("Connecting to the CNValidation service at " + SystemConfiguration.getValue("BATCH_SERVICES_URL"));
    AutomationResponse<?> rawResponse = client.executeAndWrap(AutomationServiceClient.CN_TYC_SERVICE_ID, request, AutomationResponse.class);
    ObjectMapper mapper = new ObjectMapper();
    String json = mapper.writeValueAsString(rawResponse);
    TypeReference<AutomationResponse<CNResponse>> ref = new TypeReference<AutomationResponse<CNResponse>>() {
    };
    return mapper.readValue(json, ref);
  }
}
