package com.ibm.cio.cmr.request.automation.impl.gbl;

import java.util.ArrayList;
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
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.util.RequestUtils;
import com.ibm.cio.cmr.request.util.geo.GEOHandler;
import com.ibm.cmr.services.client.CmrServicesFactory;
import com.ibm.cmr.services.client.MatchingServiceClient;
import com.ibm.cmr.services.client.matching.MatchingResponse;
import com.ibm.cmr.services.client.matching.request.ReqCheckRequest;
import com.ibm.cmr.services.client.matching.request.ReqCheckResponse;

public class CNDupReqCheckElement extends DuplicateCheckElement {

  private static final Logger LOG = Logger.getLogger(CNDupReqCheckElement.class);

  public CNDupReqCheckElement(String requestTypes, String actionOnError, boolean overrideData, boolean stopOnError) {
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

    LOG.debug("China duplicate request check start... request " + data.getId().getReqId());

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
            LOG.debug("CNDupReqCheckElement... " + reqCheckMatches.size() + " record(s) returned.");
            details.append(reqCheckMatches.size() + " record(s) returned.");

            List<ReqCheckResponse> reqCheckMatchesCopy = new ArrayList<ReqCheckResponse>();
            for (ReqCheckResponse reqCheckRecord : reqCheckMatches) {
              if (isValidDupReq(entityManager, reqCheckRecord, data.getCustSubGrp())) {
                reqCheckMatchesCopy.add(reqCheckRecord);
              }
            }

            if (reqCheckMatchesCopy != null && reqCheckMatchesCopy.size() > 0) {
              LOG.debug("CNDupReqCheckElement... " + reqCheckMatchesCopy.size() + " record(s) found.");
              details.append(reqCheckMatchesCopy.size() + " record(s) found.");
              if (reqCheckMatchesCopy.size() > 5) {
                details.append("Showing top 5 matches only.");
                reqCheckMatchesCopy = reqCheckMatchesCopy.subList(0, 5);
              }
              int itemNo = 1;
              for (ReqCheckResponse reqCheckRecord : reqCheckMatchesCopy) {
                details.append("\n");
                LOG.debug("Duplicate Requests Found, Req Id: " + reqCheckRecord.getReqId());
                output.addMatch(getProcessCode(), "REQ_ID", reqCheckRecord.getReqId() + "", matchType, reqCheckRecord.getMatchGrade() + "", "REQ",
                    itemNo);
                logDuplicateRequest(details, reqCheckRecord, matchType);
                dupReqIds.add(Long.toString(reqCheckRecord.getReqId()));
              }
              engineData.put("reqCheckMatches", reqCheckMatchesCopy);
              result.setResults("Found Duplicate Requests.");
              engineData.addRejectionComment("DUPR", reqCheckMatchesCopy.size() + " possible duplicate request(s) found with the same data.",
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

    LOG.debug("China duplicate request check end... request " + data.getId().getReqId());

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
      if (StringUtils.isNotBlank(data.getVat())) {
        request.setVat(data.getVat());
      } else if (StringUtils.isNotBlank(addr.getVat())) {
        request.setVat(addr.getVat());
      }
      if (AutomationUtil.isCheckVatForDuplicates(data.getCmrIssuingCntry())) {
        request.setMatchType("V");
      }

      DuplicateChecksUtil.setCountrySpecificsForRequestChecks(entityManager, admin, data, addr, request, engineData);

    }
    return request;
  }

  @Override
  public String getProcessCode() {
    // TODO Auto-generated method stub
    return AutomationElementRegistry.CN_DUP_REQ_CHECK;
  }

  @Override
  public String getProcessDesc() {
    return "China Duplicate Request Check";
  }

  @Override
  public boolean isNonImportable() {
    return true;
  }

  private boolean isValidDupReq(EntityManager entityManager, ReqCheckResponse reqCheckRecord, String scenario) {
    boolean output = false;
    if (reqCheckRecord == null || StringUtils.isEmpty(scenario)) {
      return false;
    }
    String reqCheckRecordScenario = getScenario(entityManager, reqCheckRecord.getReqId());
    if (scenario != null && scenario.equals(reqCheckRecordScenario)) {
      output = true;
    }
    String outputStr = String.valueOf(output);
    LOG.debug("CNDupReqCheckElement... reqCheckRecord " + reqCheckRecord.getReqId() + " matches = " + outputStr);
    return output;
  }
  
  private boolean isValidDupReqOld(EntityManager entityManager, ReqCheckResponse reqCheckRecord, String scenario) {
    boolean output = true;
    if (reqCheckRecord == null || StringUtils.isBlank(scenario)) {
      return false;
    }
    String reqCheckRecordScenario = getScenario(entityManager, reqCheckRecord.getReqId());
    if ("NRMLC".equals(scenario)) {
      switch (reqCheckRecordScenario) {
      case "BUSPR":
      case "INTER":
      case "BLUMX":
      case "MRKT":
        output = false;
        break;
      case "EMBSA":
      case "AQSTN":
      case "NRMLD":
      case "KYND":
      case "ECOSY":
      default:
        output = true;
        break;
      }
    } else if ("NRMLD".equals(scenario)) {
      switch (reqCheckRecordScenario) {
      case "BUSPR":
      case "INTER":
      case "BLUMX":
      case "MRKT":
        output = false;
        break;
      case "EMBSA":
      case "AQSTN":
      case "NRMLC":
      case "KYND":
      case "ECOSY":
      default:
        output = true;
        break;
      }
    } else if ("ECOSY".equals(scenario)) {
      switch (reqCheckRecordScenario) {
      case "BUSPR":
      case "INTER":
      case "BLUMX":
      case "MRKT":
        output = false;
        break;
      case "EMBSA":
      case "AQSTN":
      case "NRMLC":
      case "NRMLD":
      case "KYND":
      default:
        output = true;
        break;
      }
    } else if ("BUSPR".equals(scenario)) {
      switch (reqCheckRecordScenario) {
      case "ECOSY":
      case "INTER":
      case "BLUMX":
      case "MRKT":
      case "EMBSA":
      case "AQSTN":
      case "NRMLC":
        output = false;
        break;
      case "NRMLD":
        output = false;
        break;
      case "KYND":
      default:
        output = true;
        break;
      }
    } else if ("INTER".equals(scenario)) {
      switch (reqCheckRecordScenario) {
      case "ECOSY":
      case "BUSPR":
      case "BLUMX":
      case "MRKT":
      case "EMBSA":
      case "AQSTN":
      case "NRMLC":
        output = false;
        break;
      case "NRMLD":
        output = false;
        break;
      case "KYND":
      default:
        output = true;
        break;
      }
    } else if ("BLUMX".equals(scenario)) {
      switch (reqCheckRecordScenario) {
      case "ECOSY":
      case "INTER":
      case "BUSPR":
      case "MRKT":
      case "EMBSA":
      case "AQSTN":
      case "NRMLC":
        output = false;
        break;
      case "NRMLD":
        output = false;
        break;
      case "KYND":
      default:
        output = true;
        break;
      }
    } else if ("MRKT".equals(scenario)) {
      switch (reqCheckRecordScenario) {
      case "ECOSY":
      case "INTER":
      case "BLUMX":
      case "BUSPR":
      case "EMBSA":
      case "AQSTN":
      case "NRMLC":
        output = false;
        break;
      case "NRMLD":
        output = false;
        break;
      case "KYND":
      default:
        output = true;
        break;
      }
    } else {
      output = true;
    }
    String outputStr = String.valueOf(output);
    LOG.debug("CNDupReqCheckElement... reqCheckRecord " + reqCheckRecord.getReqId() + " matches = " + outputStr);
    return output;
  }

  private String getScenario(EntityManager entityManager, long reqId) {
    String output = null;
    List<Object[]> results = new ArrayList<Object[]>();
    String sql = ExternalizedQuery.getSql("QUERY.DATA.GET.CUSTSUBGRP.BY_REQID");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", reqId);
    results = query.getResults();
    if (results != null && !results.isEmpty()) {
      Object[] sResult = results.get(0);
      output = sResult[0] != null ? sResult[0].toString() : "";
    }
    return output;
  }

}
