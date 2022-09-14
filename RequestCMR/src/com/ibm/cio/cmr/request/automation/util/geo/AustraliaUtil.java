package com.ibm.cio.cmr.request.automation.util.geo;

import java.util.Arrays;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import com.ibm.cio.cmr.request.automation.AutomationEngineData;
import com.ibm.cio.cmr.request.automation.RequestData;
import com.ibm.cio.cmr.request.automation.out.AutomationResult;
import com.ibm.cio.cmr.request.automation.out.OverrideOutput;
import com.ibm.cio.cmr.request.automation.out.ValidationOutput;
import com.ibm.cio.cmr.request.automation.util.AutomationUtil;
import com.ibm.cio.cmr.request.automation.util.ScenarioExceptionsUtil;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.entity.NotifList;
import com.ibm.cio.cmr.request.entity.NotifListPK;
import com.ibm.cio.cmr.request.util.SystemLocation;
import com.ibm.cio.cmr.request.util.SystemParameters;
import com.ibm.cmr.services.client.AutomationServiceClient;
import com.ibm.cmr.services.client.CmrServicesFactory;
import com.ibm.cmr.services.client.ServiceClient.Method;
import com.ibm.cmr.services.client.automation.AutomationResponse;
import com.ibm.cmr.services.client.automation.ap.anz.BNValidationRequest;
import com.ibm.cmr.services.client.automation.ap.anz.BNValidationResponse;

public class AustraliaUtil extends AutomationUtil {

  private static final Logger LOG = Logger.getLogger(AustraliaUtil.class);

  private static final List<String> ALLOW_DEFAULT_SCENARIOS = Arrays.asList("PRIV", "XPRIV", "BLUMX", "MKTPC", "XBLUM", "XMKTP");

  public static final String SCENARIO_BLUEMIX = "BLUMX";
  public static final String SCENARIO_MARKETPLACE = "MKTPC";
  private static final String SCENARIO_PRIVATE_CUSTOMER = "PRIV";
  private static final String SCENARIO_INTERNAL = "INTER";
  private static final String SCENARIO_DUMMY = "DUMMY";
  private static final String SCENARIO_ECOSYS = "ECSYS";
  private static final String SCENARIO_CROSS_ECOSYS = "XECO";

  @Override
  public AutomationResult<OverrideOutput> doCountryFieldComputations(EntityManager entityManager, AutomationResult<OverrideOutput> results,
      StringBuilder details, OverrideOutput overrides, RequestData requestData, AutomationEngineData engineData) throws Exception {
    // get request admin and data
    long reqId = requestData.getAdmin().getId().getReqId();
    Admin admin = requestData.getAdmin();
    Data data = requestData.getData();
    Addr soldTo = requestData.getAddress("ZS01");
    StringBuilder eleResults = new StringBuilder();
    String[] isicScenarioList = { "NRML", "ESOSW", "IGF", "XIGF", "CROSS", "AQSTN", "XAQST", "SOFT" };
    boolean isIsicInvalid = false;
    LOG.debug("Australia : Performing field computations for req_id : " + reqId);
    String defaultClusterCd = getDefaultCluster(entityManager, requestData, engineData);
    String validCode = checkIfClusterSalesmanIsValid(entityManager, requestData);

    // a. check cluster
    if (defaultClusterCd.equalsIgnoreCase(data.getApCustClusterId())) {

      if ("9500".equals(data.getIsicCd()) || ALLOW_DEFAULT_SCENARIOS.contains(data.getCustSubGrp())) {
        LOG.debug("Default Cluster used but allowed: ISIC=" + data.getIsicCd() + " Scenario=" + data.getCustSubGrp());
        results.setOnError(false);
        // eleResults.append("Default Cluster used.\n");
        details.append("Default Cluster used but allowed for the request (Private Person/BlueMix/Marketplace).\n");
      } else {
        LOG.debug("Default Cluster used.");
        engineData.addRejectionComment("OTH", "Cluster should not be the default cluster for the scenario.", "", "");
        results.setOnError(true);
        // eleResults.append("Default Cluster used.\n");
        details.append("Cluster should not be the default cluster for the scenario.\n");
      }
    } else {
      LOG.debug("Default Cluster NOT used.");
      // eleResults.append("Default Cluster not used.\n");
      details.append("Cluster used is not default.\n");
    }

    if ("9500".equals(data.getIsicCd()) || ALLOW_DEFAULT_SCENARIOS.contains(data.getCustSubGrp())) {
      LOG.debug("Salesman check skipped for private and allowed scenarios.");
      // eleResults.append("\nValid cluster and Salesman No. used.\n");
      details.append("\nSalesman check skipped for this scenario (Private/Marketplace/Bluemix).\n");
    } else {

      // b. check cluster and salesman no. combination
      if ("VALID".equalsIgnoreCase(validCode)) {
        LOG.debug("The combination of Salesman No. and Cluster is valid.");
        // eleResults.append("\nValid cluster and Salesman No. used.\n");
        details.append("\nThe combination of Salesman No. and Cluster is valid.\n");
      } else if ("INVALID".equalsIgnoreCase(validCode)) {
        LOG.debug("The combination of Salesman No. and Cluster is INVALID.");
        engineData.addRejectionComment("OTH", "The combination of Salesman No. and Cluster is invalid.", "", "");
        results.setOnError(true);
        // eleResults.append("Invalid Cluster and Salesman No. combination.\n");
        details.append("\nThe combination of Salesman No. and Cluster is invalid.\n");
      } else {
        LOG.debug("Salesman No.-Cluster combination not present.");
        /*
         * engineData.addRejectionComment("OTH",
         * "No combination of Salesman No. and Cluster is present.", "", "");
         * results.setOnError(true);
         */
        // eleResults.append("No combination of Salesman No. and Cluster
        // present.\n");
        details.append("\nNo combination of Salesman No. and Cluster is present.\n");
      }
    }
    isIsicInvalid = isISICValidForScenario(requestData, Arrays.asList(isicScenarioList));

    if (isIsicInvalid) {
      details.append("Invalid ISIC code, please choose another one based on industry.\n");
      engineData.addRejectionComment("OTH", "Invalid ISIC code, please choose another one based on industry.", "", "");
      results.setOnError(true);
    } else {
      details.append("ISIC is valid" + "\n");
    }

    if (results != null && !results.isOnError()) {

    } else {
      eleResults.append("Error On Field Calculation.");
    }
    results.setResults(eleResults.toString());
    results.setDetails(details.toString());
    results.setProcessOutput(overrides);

    return results;
  }

  /**
   * CHecks if the record is a private customer / bluemix / marketplace
   * 
   * @param engineData
   * @param requestData
   * @param details
   * @return
   */
  private void processSkipCompanyChecks(AutomationEngineData engineData, RequestData requestData, StringBuilder details) {
    Data data = requestData.getData();

    boolean skipCompanyChecks = "9500".equals(data.getIsicCd()) || (data.getCustSubGrp() != null && data.getCustSubGrp().contains("PRIV"));
    if (skipCompanyChecks) {
      details.append("Private Person request - company checks will be skipped.\n");
      ScenarioExceptionsUtil exc = (ScenarioExceptionsUtil) engineData.get("SCENARIO_EXCEPTIONS");
      if (exc != null) {
        exc.setSkipCompanyVerification(true);
        engineData.put("SCENARIO_EXCEPTIONS", exc);
      }
    }
  }

  @Override
  public boolean performScenarioValidation(EntityManager entityManager, RequestData requestData, AutomationEngineData engineData,
      AutomationResult<ValidationOutput> result, StringBuilder details, ValidationOutput output) {
    // String[] scnarioList = { "XIGF", "CROSS", "MKTPC", "BLUMX", "DUMMY",
    // "XDUMM", "INTER", "XINT", "AQSTN", "XAQST", "SOFT" };
    // skipCompanyCheckForScenario(requestData, engineData,
    // Arrays.asList(scnarioList), true);
    // scenario check for normal and ESOSW
    Data data = requestData.getData();
    String scenario = data.getCustSubGrp();
    String scenarioList[] = { "NRML", "ESOSW" };
    Addr soldTo = requestData.getAddress("ZS01");
    String custNm1 = soldTo.getCustNm1();
    String custNm2 = StringUtils.isNotBlank(soldTo.getCustNm2()) ? " " + soldTo.getCustNm2() : "";
    String customerName = custNm1 + custNm2;

    // allowDuplicatesForScenario(engineData, requestData,
    // Arrays.asList(scenarioList));
    processSkipCompanyChecks(engineData, requestData, details);
    switch (scenario) {
    // CREATCMR - 2031
    // case SCENARIO_BLUEMIX:
    // case SCENARIO_MARKETPLACE:
    // engineData.addPositiveCheckStatus(AutomationEngineData.SKIP_GBG);
    // break;
    // CREATCMR - 5772
    case SCENARIO_BLUEMIX:
    case SCENARIO_MARKETPLACE:
    case SCENARIO_DUMMY:
    case SCENARIO_INTERNAL:
      engineData.addPositiveCheckStatus(AutomationEngineData.SKIP_COVERAGE);
      break;

    case SCENARIO_PRIVATE_CUSTOMER:
      engineData.addPositiveCheckStatus(AutomationEngineData.SKIP_COVERAGE);
      return doPrivatePersonChecks(engineData, SystemLocation.AUSTRALIA, soldTo.getLandCntry(), customerName, details, false, requestData);
    case SCENARIO_ECOSYS:
    case SCENARIO_CROSS_ECOSYS:
      addToNotifyListANZ(entityManager, data.getId().getReqId());
    }
    return true;
  }

  private AutomationResponse<BNValidationResponse> getBNInfo(Admin admin, Data data) throws Exception {
    AutomationServiceClient client = CmrServicesFactory.getInstance().createClient(SystemConfiguration.getValue("BATCH_SERVICES_URL"),
        AutomationServiceClient.class);
    client.setReadTimeout(1000 * 60 * 5);
    client.setRequestMethod(Method.Get);

    BNValidationRequest request = new BNValidationRequest();
    request.setBusinessNumber(data.getVat());
    System.out.println(request + request.getBusinessNumber());

    LOG.debug("Connecting to the BNValidation service at " + SystemConfiguration.getValue("BATCH_SERVICES_URL"));
    AutomationResponse<?> rawResponse = client.executeAndWrap(AutomationServiceClient.AU_ABN_VALIDATION_SERVICE_ID, request,
        AutomationResponse.class);
    ObjectMapper mapper = new ObjectMapper();
    String json = mapper.writeValueAsString(rawResponse);
    TypeReference<AutomationResponse<BNValidationResponse>> ref = new TypeReference<AutomationResponse<BNValidationResponse>>() {
    };
    return mapper.readValue(json, ref);
  }

  public static void addToNotifyListANZ(EntityManager entityManager, long reqId) {
    StringBuilder anzEcoNotifyList = getANZEcoNotifyList();
    List<String> users = Arrays.asList(anzEcoNotifyList.toString().split("\\s*,\\s*"));
    for (String user : users) {
      NotifList notif = new NotifList();
      NotifListPK pk = new NotifListPK();
      pk.setReqId(reqId);
      pk.setNotifId(user);
      notif.setId(pk);
      notif.setNotifNm(user);
      entityManager.merge(notif);
    }
  }

  private static StringBuilder getANZEcoNotifyList() {
    StringBuilder anzEcoNotifyList = new StringBuilder();
    anzEcoNotifyList.append(SystemParameters.getString("ANZ_ECSYS_NOTIFY"));
    return anzEcoNotifyList;
  }

  @Override
  protected List<String> getCountryLegalEndings() {
    return Arrays.asList("PTY LTD", "LTD", "company", "limited", "PT", "SDN BHD", "berhad", "CO. LTD", "company limited", "JSC", "JOINT STOCK",
        "INC.", "PTE LTD", "PVT LTD", "private limited", "CORPORATION", "hospital", "university");
  }
}
