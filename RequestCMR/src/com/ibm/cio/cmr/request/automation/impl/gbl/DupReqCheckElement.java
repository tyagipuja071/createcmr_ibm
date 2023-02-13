package com.ibm.cio.cmr.request.automation.impl.gbl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

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
import com.ibm.cio.cmr.request.util.RequestUtils;
import com.ibm.cio.cmr.request.util.geo.GEOHandler;
import com.ibm.cmr.services.client.CmrServicesFactory;
import com.ibm.cmr.services.client.MatchingServiceClient;
import com.ibm.cmr.services.client.matching.MatchingResponse;
import com.ibm.cmr.services.client.matching.request.ReqCheckRequest;
import com.ibm.cmr.services.client.matching.request.ReqCheckResponse;

/**
 * 
 * @author RoopakChugh
 *
 */
public class DupReqCheckElement extends DuplicateCheckElement {

  private static final Logger LOG = Logger.getLogger(DupReqCheckElement.class);
  private static final List<String> NORDX_SUB_COUNTRIES = Arrays.asList("678", "702");

  public DupReqCheckElement(String requestTypes, String actionOnError, boolean overrideData, boolean stopOnError) {
    super(requestTypes, actionOnError, overrideData, stopOnError);
  }

  @Override
  public boolean importMatch(EntityManager entityManager, RequestData requestData, AutomationMatching match) {
    return false;
  }

  @Override
  public AutomationResult<MatchingOutput> executeElement(EntityManager entityManager, RequestData requestData, AutomationEngineData engineData)
      throws Exception {
    String matchType = "NA";
    Admin admin = requestData.getAdmin();
    Data data = requestData.getData();
    ScenarioExceptionsUtil scenarioExceptions = getScenarioExceptions(entityManager, requestData, engineData);

    AutomationResult<MatchingOutput> result = buildResult(admin.getId().getReqId());

    MatchingOutput output = new MatchingOutput();
    Addr soldTo = requestData.getAddress("ZS01");
    if (soldTo != null && !scenarioExceptions.isSkipDuplicateChecks()) {

      // check if eligible for vat matching
      if (AutomationUtil.isCheckVatForDuplicates(data.getCmrIssuingCntry())) {
        matchType = "V";
      }

      // get Matches
      MatchingResponse<ReqCheckResponse> response = getMatches(entityManager, requestData, engineData);
      if (response != null) {
        if (response.getSuccess()) {
          if (response.getMatched()) {
            List<String> dupReqIds = new ArrayList<>();
            StringBuilder details = new StringBuilder();
            List<ReqCheckResponse> reqCheckMatches = response.getMatches();
            details.append(reqCheckMatches.size() + " record(s) found.");
            if (reqCheckMatches.size() > 5) {
              details.append("Showing top 5 matches only.");
              reqCheckMatches = reqCheckMatches.subList(0, 5);
            }

            int itemNo = 1;
            for (ReqCheckResponse reqCheckRecord : reqCheckMatches) {
              details.append("\n");
              LOG.debug("Duplicate Requests Found, Req Id: " + reqCheckRecord.getReqId());
              output.addMatch(getProcessCode(), "REQ_ID", reqCheckRecord.getReqId() + "", matchType, reqCheckRecord.getMatchGrade() + "", "REQ",
                  itemNo);
              logDuplicateRequest(details, reqCheckRecord, matchType);
              dupReqIds.add(Long.toString(reqCheckRecord.getReqId()));
            }
            engineData.put("reqCheckMatches", reqCheckMatches);
            result.setResults("Found Duplicate Requests.");
            engineData.addRejectionComment("DUPR", reqCheckMatches.size() + " possible duplicate request(s) found with the same data.",
                StringUtils.join(dupReqIds, ", "), "");
            result.setOnError(true);
            result.setProcessOutput(output);
            result.setDetails(details.toString().trim());
          } else {
            result.setDetails("No Duplicate Requests were found.");
            result.setResults("No Matches");
            result.setOnError(false);
          }
        } else {
          result.setDetails(response.getMessage());
          engineData.addRejectionComment("OTH", response.getMessage(), "", "");
          result.setOnError(true);
          result.setResults("Duplicate Request Check Encountered an error.");
        }
      } else {
        result.setDetails("Duplicate Request Check Encountered an error.");
        engineData.addRejectionComment("OTH", "Duplicate Request Check Encountered an error.", "", "");
        result.setOnError(true);
        result.setResults("Duplicate Request Check Encountered an error.");
      }

    } else if (scenarioExceptions.isSkipDuplicateChecks()) {
      result.setDetails("The request's scenario is configured to skip duplicate checks.");
      result.setResults("Skipped Duplicate Check");
      result.setOnError(false);
    } else if (soldTo == null) {
      result.setDetails("Missing main address on the request.");
      engineData.addRejectionComment("OTH", "Missing main address on the request.", "", "");
      result.setResults("No Matches");
      result.setOnError(true);
    } else {
      result.setDetails("Duplicate Request Check Encountered an error.");
      engineData.addRejectionComment("OTH", "Duplicate Request Check Encountered an error.", "", "");
      result.setOnError(true);
      result.setResults("Duplicate Request Check Encountered an error.");
    }

    return result;

  }

  public void logDuplicateRequest(StringBuilder details, ReqCheckResponse reqCheckRecord, String matchType) {
    details.append("Request ID = " + reqCheckRecord.getReqId()).append("\n");
    details.append("Match Type = " + matchType).append("\n");
    details.append("Match Score = " + reqCheckRecord.getMatchGrade()).append("\n");
    details.append("Issuing Country =  " + reqCheckRecord.getIssuingCntry()).append("\n");
    details.append("Customer Name =  " + reqCheckRecord.getCustomerName()).append("\n");
    details.append("Address =  " + reqCheckRecord.getStreetLine1()).append("\n");
    if (!StringUtils.isBlank(reqCheckRecord.getStreetLine2())) {
      details.append("Address (cont)=  " + reqCheckRecord.getStreetLine2()).append("\n");
    }
    if (!StringUtils.isBlank(reqCheckRecord.getCity())) {
      details.append("City =  " + reqCheckRecord.getCity()).append("\n");
    }
    if (!StringUtils.isBlank(reqCheckRecord.getStateProv())) {
      details.append("State =  " + reqCheckRecord.getStateProv()).append("\n");
    }
    if (!StringUtils.isBlank(reqCheckRecord.getPostalCode())) {
      details.append("Postal Code =  " + reqCheckRecord.getPostalCode()).append("\n");
    }
    if (!StringUtils.isBlank(reqCheckRecord.getLandedCountry())) {
      details.append("Landed Country =  " + reqCheckRecord.getLandedCountry()).append("\n");
    }
  }

  public MatchingResponse<ReqCheckResponse> getMatches(EntityManager entityManager, RequestData requestData, AutomationEngineData engineData)
      throws Exception {
    Admin admin = requestData.getAdmin();
    Data data = requestData.getData();
    ScenarioExceptionsUtil scenarioExceptions = getScenarioExceptions(entityManager, requestData, engineData);
    MatchingResponse<ReqCheckResponse> response = new MatchingResponse<ReqCheckResponse>();
    MatchingServiceClient client = CmrServicesFactory.getInstance().createClient(SystemConfiguration.getValue("BATCH_SERVICES_URL"),
        MatchingServiceClient.class);
    client.setReadTimeout(1000 * 60 * 5);
    boolean first = true;
    for (String addrType : scenarioExceptions.getAddressTypesForDuplicateRequestCheck()) {
      Addr addr = requestData.getAddress(addrType);
      if (addr != null) {
        ReqCheckRequest request = getRequest(entityManager, data, admin, addr, scenarioExceptions, engineData);
        LOG.debug("Executing Duplicate Request Check "
            + (admin.getId().getReqId() > 0 ? " for Request ID: " + admin.getId().getReqId() : " through UI") + " for AddrType: " + addrType);
        MatchingResponse<?> rawResponse = client.executeAndWrap(MatchingServiceClient.REQ_SERVICE_ID, request, MatchingResponse.class);
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(rawResponse);
        TypeReference<MatchingResponse<ReqCheckResponse>> ref = new TypeReference<MatchingResponse<ReqCheckResponse>>() {
        };
        MatchingResponse<ReqCheckResponse> res = mapper.readValue(json, ref);

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

      } else {
        LOG.debug("No '" + addrType + "' address on the request. Skipping duplicate check.");
        continue;
      }
    }
    
    // Filer requests based on sub-region
    if (NORDX_SUB_COUNTRIES.contains(data.getCmrIssuingCntry()) && response.getSuccess() && response.getMatches().size() > 0) {
      LOG.debug("Matches found for the given search criteria.");
      AutomationUtil countryUtil = AutomationUtil.getNewCountryUtil(data.getCmrIssuingCntry());
      if (countryUtil != null) {
        countryUtil.filterDuplicateReqMatches(entityManager, requestData, engineData, response);
      }
    }

    // reverify
    if (response.getMatches().size() == 0) {
      response.setMatched(false);
      response.setMessage("No matches found for the given search criteria.");
    }

    return response;
  }

  private void updateMatches(MatchingResponse<ReqCheckResponse> global, MatchingResponse<ReqCheckResponse> iteration) {
    List<ReqCheckResponse> updated = new ArrayList<ReqCheckResponse>();
    for (ReqCheckResponse i : global.getMatches()) {
      for (ReqCheckResponse j : iteration.getMatches()) {
        if (i.getReqId() == j.getReqId()) {
          updated.add(i);
          break;
        }
      }
    }

    global.setSuccess(true);
    global.setMatches(updated);
    global.setMatched(updated.size() > 0);
  }

  private ReqCheckRequest getRequest(EntityManager entityManager, Data data, Admin admin, Addr addr, ScenarioExceptionsUtil scenarioExceptions,
      AutomationEngineData engineData) {
    ReqCheckRequest request = new ReqCheckRequest();
    request.setReqId(admin.getId().getReqId());
    boolean payGoAddredited = RequestUtils.isPayGoAccredited(entityManager, admin.getSourceSystId());
    if (addr != null) {
      request.setAddrType(addr.getId().getAddrType());
      request.setCity(addr.getCity1());
      GEOHandler handler = RequestUtils.getGEOHandler(data.getCmrIssuingCntry());
      if (handler != null && !handler.customerNamesOnAddress()) {
        request.setCustomerName(admin.getMainCustNm1() + (StringUtils.isBlank(admin.getMainCustNm2()) ? "" : " " + admin.getMainCustNm2()));
      } else {
        request.setCustomerName(addr.getCustNm1() + (StringUtils.isBlank(addr.getCustNm2()) ? "" : " " + addr.getCustNm2()));
      }
      request.setStreetLine1(addr.getAddrTxt());
      request.setStreetLine2(addr.getAddrTxt2());
      request.setLandedCountry(addr.getLandCntry());
      request.setIssuingCountry(data.getCmrIssuingCntry());
      request.setPostalCode(addr.getPostCd());
      request.setStateProv(addr.getStateProv());
      // extWallet
      if (payGoAddredited) {
        if ("PG01".equals(addr.getId().getAddrType()) && StringUtils.isNotBlank(addr.getExtWalletId())) {
          request.setExtWalletId(addr.getExtWalletId());
        }
      }
      if (StringUtils.isNotBlank(data.getVat())) {
        request.setVat(data.getVat());
      } else if (StringUtils.isNotBlank(addr.getVat())) {
        request.setVat(addr.getVat());
      }
      if (AutomationUtil.isCheckVatForDuplicates(data.getCmrIssuingCntry())) {
        request.setMatchType("V");
      }
      request.setScenario(data.getCustSubGrp());

      DuplicateChecksUtil.setCountrySpecificsForRequestChecks(entityManager, admin, data, addr, request, engineData);

    }
    return request;
  }

  @Override
  public String getProcessCode() {
    // TODO Auto-generated method stub
    return AutomationElementRegistry.GBL_REQ_CHECK;
  }

  @Override
  public String getProcessDesc() {
    return "Duplicate Request Check";
  }

  @Override
  public boolean isNonImportable() {
    return true;
  }

}
