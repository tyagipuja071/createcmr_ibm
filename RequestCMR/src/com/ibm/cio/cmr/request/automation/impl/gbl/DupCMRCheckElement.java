package com.ibm.cio.cmr.request.automation.impl.gbl;

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
import com.ibm.cio.cmr.request.util.RequestUtils;
import com.ibm.cio.cmr.request.util.SystemLocation;
import com.ibm.cio.cmr.request.util.geo.GEOHandler;
import com.ibm.cmr.services.client.CmrServicesFactory;
import com.ibm.cmr.services.client.MatchingServiceClient;
import com.ibm.cmr.services.client.QueryClient;
import com.ibm.cmr.services.client.matching.MatchingResponse;
import com.ibm.cmr.services.client.matching.cmr.DuplicateCMRCheckRequest;
import com.ibm.cmr.services.client.matching.cmr.DuplicateCMRCheckResponse;
import com.ibm.cmr.services.client.query.QueryRequest;
import com.ibm.cmr.services.client.query.QueryResponse;

public class DupCMRCheckElement extends DuplicateCheckElement {

  private static final Logger log = Logger.getLogger(DupCMRCheckElement.class);
  private static final List<String> AP_COUNTRIES = Arrays.asList("744");
  private static final List<String> DACH_COUNTRIES = Arrays.asList("618", "724", "848");

  public DupCMRCheckElement(String requestTypes, String actionOnError, boolean overrideData, boolean stopOnError) {
    super(requestTypes, actionOnError, overrideData, stopOnError);
  }

  @Override
  public boolean importMatch(EntityManager entityManager, RequestData requestData, AutomationMatching match) {

    return false;
  }

  @Override
  public AutomationResult<MatchingOutput> executeElement(EntityManager entityManager, RequestData requestData, AutomationEngineData engineData)
      throws Exception {
    log.debug("DupcCmrCheckElement");
    Addr soldTo = requestData.getAddress("ZS01");
    Admin admin = requestData.getAdmin();
    Data data = requestData.getData();
    String isProspectCmr = admin.getProspLegalInd();
    String issuingCntry = data.getCmrIssuingCntry();
    ScenarioExceptionsUtil scenarioExceptions = getScenarioExceptions(entityManager, requestData, engineData);
    AutomationResult<MatchingOutput> result = buildResult(admin.getId().getReqId());
    boolean matchDepartment = false;
    if (engineData.get(AutomationEngineData.MATCH_DEPARTMENT) != null) {
      matchDepartment = (boolean) engineData.get(AutomationEngineData.MATCH_DEPARTMENT);
    }

    MatchingOutput output = new MatchingOutput();
    if (!scenarioExceptions.isSkipDuplicateChecks()) {
      if (StringUtils.isNotBlank(admin.getDupCmrReason())) {
        StringBuilder details = new StringBuilder();
        details.append("User requested to proceed with Duplicate CMR Creation.").append("\n\n");
        details.append("Reason provided - ").append("\n");
        details.append(admin.getDupCmrReason()).append("\n");
        result.setDetails(details.toString());
        result.setResults("Overridden");
        result.setOnError(false);
      } else if (soldTo != null) {
        String department = StringUtils.isBlank(soldTo.getDept()) ? "" : soldTo.getDept();
        // get CMR Matches
        MatchingResponse<DuplicateCMRCheckResponse> response = getMatches(entityManager, requestData, engineData);
        if (response != null) {
          if (response.getSuccess()) {
            if (response.getMatched()) {
              StringBuilder details = new StringBuilder();
              List<DuplicateCMRCheckResponse> cmrCheckMatches = response.getMatches();
              // cmr-2067 fix start
              if (matchDepartment) {
                List<DuplicateCMRCheckResponse> cmrCheckMatchesDept = new ArrayList<DuplicateCMRCheckResponse>();
                for (DuplicateCMRCheckResponse cmrCheckRecord : cmrCheckMatches) {
                  if (department.equalsIgnoreCase(cmrCheckRecord.getDept()) || department.equalsIgnoreCase(cmrCheckRecord.getCust_name3())
                      || department.equalsIgnoreCase(cmrCheckRecord.getCust_name4())) {
                    cmrCheckMatchesDept.add(cmrCheckRecord);
                  }
                }
                Collections.copy(cmrCheckMatches, cmrCheckMatchesDept);
              }
              result = checkDupcProspectCmr(cmrCheckMatches, soldTo, isProspectCmr, engineData, result, issuingCntry);
              if (result.isOnError()) {
                return result;
              }
              if (cmrCheckMatches.size() != 0) {
                result.setResults("Matches Found");
                details.append(cmrCheckMatches.size() + " record(s) found.");
                List<String> dupCMRNos = new ArrayList<>();
                if (cmrCheckMatches.size() > 5) {
                  cmrCheckMatches = cmrCheckMatches.subList(0, 5);
                  details.append("Showing top 5 matches only.");
                }

                int itemNo = 1;
                for (DuplicateCMRCheckResponse cmrCheckRecord : cmrCheckMatches) {
                  details.append("\n");

                  log.debug("Duplicate CMRs Found..");
                  output.addMatch(getProcessCode(), "CMR_NO", cmrCheckRecord.getCmrNo(), "Matching Logic", cmrCheckRecord.getMatchGrade() + "", "CMR",
                      itemNo++);
                  dupCMRNos.add(cmrCheckRecord.getCmrNo());
                  logDuplicateCMR(details, cmrCheckRecord);
                }
                engineData.put("cmrCheckMatches", cmrCheckMatches);
                result.setResults("Found Duplicate CMRs.");
                if (engineData.hasPositiveCheckStatus("allowDuplicates")) {
                  engineData.addNegativeCheckStatus("dupAllowed",
                      cmrCheckMatches.size()
                          + " possible duplicate CMR(s) found with the same data but allowed for the scenario.\n Duplicate CMR(s) found: "
                          + StringUtils.join(dupCMRNos, ", "));
                } else {
                  List<String> zs01KunnrsList = new ArrayList<String>();
                  for (String dupCMR : dupCMRNos) {
                    zs01KunnrsList.add(getZS01Kunnr(dupCMR, requestData.getData().getCmrIssuingCntry()));
                  }
                  engineData.addRejectionComment("DUPC", "Customer already exists / duplicate CMR", StringUtils.join(dupCMRNos, ", "),
                      StringUtils.join(zs01KunnrsList, ", "));
                  // to allow overides later
                  requestData.getAdmin().setMatchIndc("C");
                  result.setOnError(true);
                }
                result.setProcessOutput(output);
                result.setDetails(details.toString().trim());
              } else {
                result.setDetails("No Duplicate CMRs were found.");
                result.setResults("No Matches");
                result.setOnError(false);
              }
            } else {
              result.setDetails("No Duplicate CMRs were found.");
              result.setResults("No Matches");
              result.setOnError(false);
            }
          } else {
            result.setDetails(response.getMessage());
            engineData.addRejectionComment("OTH", response.getMessage(), "", "");
            result.setOnError(true);
            result.setResults("Error on Duplicate CMR Check.");
          }
        } else {
          result.setDetails("Duplicate CMR Check Encountered an error.");
          engineData.addRejectionComment("OTH", "Duplicate CMR Check Encountered an error.", "", "");
          result.setOnError(true);
          result.setResults("Error on Duplicate CMR Check");
        }
      } else {
        result.setDetails("Missing main address on the request.");
        engineData.addRejectionComment("OTH", "Missing main address on the request.", "", "");
        result.setResults("No Matches");
        result.setOnError(true);
      }
    } else {
      result.setDetails("Skipping Duplicate CMR checks for scenario");
      log.debug("Skipping Duplicate CMR checks for scenario");
      result.setResults("Skipped");
      result.setOnError(false);
    }
    return result;
  }

  public static void logDuplicateCMR(StringBuilder details, DuplicateCMRCheckResponse cmrCheckRecord) {
    if (!StringUtils.isBlank(cmrCheckRecord.getCmrNo())) {
      details.append("CMR Number = " + cmrCheckRecord.getCmrNo()).append("\n");
    }
    if (!StringUtils.isBlank(cmrCheckRecord.getMatchGrade())) {
      details.append("Match Grade = " + cmrCheckRecord.getMatchGrade()).append("\n");
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
    if (!StringUtils.isBlank(cmrCheckRecord.getCity())) {
      details.append("City =  " + cmrCheckRecord.getCity()).append("\n");
    }
    if (!StringUtils.isBlank(cmrCheckRecord.getStateProv())) {
      details.append("State =  " + cmrCheckRecord.getStateProv()).append("\n");
    }
    if (!StringUtils.isBlank(cmrCheckRecord.getPostalCode())) {
      details.append("Postal Code =  " + cmrCheckRecord.getPostalCode()).append("\n");
    }
    if (!StringUtils.isBlank(cmrCheckRecord.getVat())) {
      details.append("VAT =  " + cmrCheckRecord.getVat()).append("\n");
    }
    if (!StringUtils.isBlank(cmrCheckRecord.getDunsNo())) {
      details.append("Duns No =  " + cmrCheckRecord.getDunsNo()).append("\n");
    }
    if (!StringUtils.isBlank(cmrCheckRecord.getParentDunsNo())) {
      details.append("Parent Duns No =  " + cmrCheckRecord.getParentDunsNo()).append("\n");
    }
    if (!StringUtils.isBlank(cmrCheckRecord.getGuDunsNo())) {
      details.append("Global Duns No =  " + cmrCheckRecord.getGuDunsNo()).append("\n");
    }
    if (!StringUtils.isBlank(cmrCheckRecord.getCoverageId())) {
      details.append("Coverage Id=  " + cmrCheckRecord.getCoverageId()).append("\n");
    }
    if (!StringUtils.isBlank(cmrCheckRecord.getIbmClientId())) {
      details.append("IBM Client Id =  " + cmrCheckRecord.getIbmClientId()).append("\n");
    }
    if (!StringUtils.isBlank(cmrCheckRecord.getCapInd())) {
      details.append("Cap Indicator =  " + cmrCheckRecord.getCapInd()).append("\n");
    }
  }

  @Override
  public String getProcessCode() {
    return AutomationElementRegistry.GBL_DUP_CMR_CHECK;
  }

  @Override
  public String getProcessDesc() {
    return "Duplicate CMR Check";
  }

  public static AutomationResult<MatchingOutput> checkDupcProspectCmr(List<DuplicateCMRCheckResponse> cmrCheckMatches, Addr soldTo,
      String isProspectCmr, AutomationEngineData engineData, AutomationResult<MatchingOutput> result, String issuingCntry) {
    // cmr - 4512 match dupc prospect cmr
    log.debug("checkDupcProspectCmr");
    StringBuilder details = new StringBuilder();
    MatchingOutput output = new MatchingOutput();
    int itemNo = 1;
    for (DuplicateCMRCheckResponse cmrCheckRecord : cmrCheckMatches) {
      String regex = "\\s+$";
      String custName = soldTo.getCustNm1() + (StringUtils.isBlank(soldTo.getCustNm2()) ? "" : " " + soldTo.getCustNm2());
      String cmrRecCustName = StringUtils.isBlank(cmrCheckRecord.getCustomerName()) ? "" : cmrCheckRecord.getCustomerName();
      custName = custName.replaceAll(regex, "");
      cmrRecCustName = cmrRecCustName.replaceAll(regex, "");
      if (!"Y".equals(isProspectCmr) && cmrCheckRecord.getCmrNo() != null && cmrCheckRecord.getCmrNo().startsWith("P")
          && "75".equals(cmrCheckRecord.getOrderBlk())) {
        log.debug("Duplicate Prospect CMR Found..");
        details.append(cmrCheckMatches.size() + " record(s) found. \n");
        if (issuingCntry.equals(SystemLocation.UNITED_STATES)) {
          output.addMatch("US_DUP_CHK", "CMR_NO", cmrCheckRecord.getCmrNo(), "Matching Logic", cmrCheckRecord.getMatchGrade() + "", "CMR", itemNo++);
        } else {
          output.addMatch("GBL_DUP_CMR_CHECK", "CMR_NO", cmrCheckRecord.getCmrNo(), "Matching Logic", cmrCheckRecord.getMatchGrade() + "", "CMR",
              itemNo++);
        }
        logDuplicateCMR(details, cmrCheckRecord);
        engineData.put("cmrCheckMatches", cmrCheckMatches);
        engineData.addRejectionComment("DUPC", "There is an existing CMR " + cmrCheckRecord.getCmrNo()
            + " , please convert this Prospect CMR to Legal CMR instead of creating a new Legal CMR", "", "");
        result.setOnError(true);
        result.setResults("Found Duplicate CMRs.");
        result.setProcessOutput(output);
        result.setDetails(details.toString().trim());
        return result;
      }
    }
    return result;
  }

  public void filterProspectCMRMatches(EntityManager entityManager, RequestData requestData, AutomationEngineData engineData,
      MatchingResponse<DuplicateCMRCheckResponse> response, String isProspectCmr) {
    if ("Y".equals(isProspectCmr)) {
      List<DuplicateCMRCheckResponse> cmrCheckMatches = response.getMatches();
      List<DuplicateCMRCheckResponse> filteredMatches = new ArrayList<DuplicateCMRCheckResponse>();
      for (DuplicateCMRCheckResponse cmrCheckRecord : cmrCheckMatches) {
        if (cmrCheckRecord.getCmrNo() != null && !cmrCheckRecord.getCmrNo().startsWith("P") && !"75".equals(cmrCheckRecord.getOrderBlk())) {
          filteredMatches.add(cmrCheckRecord);
        }
      }
      response.setMatches(filteredMatches);
    }
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
      log.debug("Matches found for the given search criteria.");
      String isProspectCmr = admin.getProspLegalInd();
      AutomationUtil countryUtil = AutomationUtil.getNewCountryUtil(data.getCmrIssuingCntry());
      if (countryUtil != null) {
        countryUtil.filterDuplicateCMRMatches(entityManager, requestData, engineData, response);
      }
      if ("Y".equals(isProspectCmr)) {
        // remove all prospect cmr found in DUPC matches if it's an prospect cmr
        filterProspectCMRMatches(entityManager, requestData, engineData, response, isProspectCmr);
      }
    }

    // reverify
    if (response.getSuccess() && response.getMatches().size() == 0) {
      log.debug("No matches found for the given search criteria.");
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
      request.setStreetLine1(addr.getAddrTxt());
      request.setStreetLine2(StringUtils.isEmpty(addr.getAddrTxt2()) ? "" : addr.getAddrTxt2());
      log.debug("Issuing country:-" + data.getCmrIssuingCntry());
      if (AP_COUNTRIES.contains(data.getCmrIssuingCntry())) {
        request.setStreetLine3(StringUtils.isEmpty(addr.getDept()) ? "" : addr.getDept());
      } else {
        request.setStreetLine3("");
      }
      if (DACH_COUNTRIES.contains(data.getCmrIssuingCntry())) {
        request.setCustClass(StringUtils.isEmpty(data.getCustClass()) ? "" : data.getCustClass());
      }
      request.setCity(addr.getCity1());

      request.setStateProv(addr.getStateProv());
      request.setPostalCode(addr.getPostCd());

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
    for (DuplicateCMRCheckResponse match : global.getMatches()) {
      if (match.getCmrNo() != null && match.getCmrNo().startsWith("P") && "75".equals(match.getOrderBlk())) {
        updated.add(match);
      }
    }
    for (DuplicateCMRCheckResponse match2 : iteration.getMatches()) {
      if (match2.getCmrNo() != null && match2.getCmrNo().startsWith("P") && "75".equals(match2.getOrderBlk())) {
        updated.add(match2);
      }
    }
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
}
