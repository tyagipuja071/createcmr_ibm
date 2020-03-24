package com.ibm.cio.cmr.request.automation.util.geo;

import java.util.Arrays;

import javax.persistence.EntityManager;

import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import com.ibm.cio.cmr.request.automation.AutomationEngineData;
import com.ibm.cio.cmr.request.automation.RequestData;
import com.ibm.cio.cmr.request.automation.out.AutomationResult;
import com.ibm.cio.cmr.request.automation.out.OverrideOutput;
import com.ibm.cio.cmr.request.automation.out.ValidationOutput;
import com.ibm.cio.cmr.request.automation.util.AutomationUtil;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cmr.services.client.AutomationServiceClient;
import com.ibm.cmr.services.client.CmrServicesFactory;
import com.ibm.cmr.services.client.ServiceClient.Method;
import com.ibm.cmr.services.client.automation.AutomationResponse;
import com.ibm.cmr.services.client.automation.ap.anz.BNValidationRequest;
import com.ibm.cmr.services.client.automation.ap.anz.BNValidationResponse;

public class AustraliaUtil extends AutomationUtil {

  private static final Logger LOG = Logger.getLogger(AustraliaUtil.class);

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
      LOG.debug("Default Cluster used.");
      engineData.addRejectionComment("Cluster should not be the default cluster for the scenario.");
      results.setOnError(true);
      // eleResults.append("Default Cluster used.\n");
      details.append("Cluster should not be the default cluster for the scenario.\n");
    } else {
      LOG.debug("Default Cluster NOT used.");
      // eleResults.append("Default Cluster not used.\n");
      details.append("Cluster used is not default.\n");
    }
    // b. check cluster and salesman no. combination
    if ("VALID".equalsIgnoreCase(validCode)) {
      LOG.debug("The combination of Salesman No. and Cluster is valid.");
      // eleResults.append("\nValid cluster and Salesman No. used.\n");
      details.append("\nThe combination of Salesman No. and Cluster is valid.\n");
    } else if ("INVALID".equalsIgnoreCase(validCode)) {
      LOG.debug("The combination of Salesman No. and Cluster is INVALID.");
      engineData.addRejectionComment("The combination of Salesman No. and Cluster is invalid.");
      results.setOnError(true);
      // eleResults.append("Invalid Cluster and Salesman No. combination.\n");
      details.append("\nThe combination of Salesman No. and Cluster is invalid.\n");
    } else {
      LOG.debug("Salesman No.-Cluster combination not present.");
      engineData.addRejectionComment("No combination of Salesman No. and Cluster is present.");
      results.setOnError(true);
      // eleResults.append("No combination of Salesman No. and Cluster
      // present.\n");
      details.append("\nNo combination of Salesman No. and Cluster is present.\n");
    }
    // defect CMR-1972 fix
    // try {
    // // c. check entity type
    // AutomationResponse<BNValidationResponse> response = getBNInfo(admin,
    // data);
    // if (response != null && response.isSuccess()) {
    // LOG.debug("Response received from ABN Service.");
    // if (response.getRecord().isValid()) {
    // LOG.debug("Business No. verified succesfully from ABN Service.");
    // if ((StringUtils.isNotBlank(response.getRecord().getEntityTypeCode())
    // &&
    // StringUtils.containsIgnoreCase(response.getRecord().getEntityTypeCode(),
    // "GOV"))
    // || (StringUtils.isNotBlank(response.getRecord().getEntityTypeDesc())
    // &&
    // StringUtils.containsIgnoreCase(response.getRecord().getEntityTypeDesc(),
    // "GOV"))) {
    // LOG.debug("Entity Type is GOV.");
    // details.append("\nSetting Fields based on ABN Service:\n");
    // details.append("Government Indicator = " + "Yes\n");
    // overrides.addOverride(AutomationElementRegistry.GBL_FIELD_COMPUTE,
    // "DATA", "GOVERNMENT", data.getGovType(), "Y");
    // // code for calculating gov type to be placed here
    //
    // if ((StringUtils.isNotBlank(response.getRecord().getEntityTypeCode())
    // &&
    // (StringUtils.containsIgnoreCase(response.getRecord().getEntityTypeCode(),
    // "Federal")
    // ||
    // StringUtils.containsIgnoreCase(response.getRecord().getEntityTypeCode(),
    // "Commonwealth")))
    // || (StringUtils.isNotBlank(response.getRecord().getEntityTypeDesc())
    // &&
    // (StringUtils.containsIgnoreCase(response.getRecord().getEntityTypeDesc(),
    // "Federal")
    // ||
    // StringUtils.containsIgnoreCase(response.getRecord().getEntityTypeDesc(),
    // "Commonwealth")))) {
    // details.append("Government Customer Type = " + "012\n");
    // overrides.addOverride(AutomationElementRegistry.GBL_FIELD_COMPUTE,
    // "DATA", "TAX_CD2", data.getTaxCd2(), "012");
    // } else if
    // ((StringUtils.isNotBlank(response.getRecord().getEntityTypeCode())
    // &&
    // StringUtils.containsIgnoreCase(response.getRecord().getEntityTypeCode(),
    // "State"))
    // || (StringUtils.isNotBlank(response.getRecord().getEntityTypeDesc())
    // &&
    // StringUtils.containsIgnoreCase(response.getRecord().getEntityTypeDesc(),
    // "State"))) {
    // details.append("Government Customer Type = " + "013\n");
    // overrides.addOverride(AutomationElementRegistry.GBL_FIELD_COMPUTE,
    // "DATA", "TAX_CD2", data.getTaxCd2(), "013");
    // } else {
    // details.append("Government Customer Type = " + "014\n");
    // overrides.addOverride(AutomationElementRegistry.GBL_FIELD_COMPUTE,
    // "DATA", "TAX_CD2", data.getTaxCd2(), "014");
    // }
    //
    // // code end
    // // engineData.addRejectionComment("The entity is a Government entity
    // // but type cannot be determined.");
    // // results.setOnError(true);
    // // eleResults.append("\nGov Typ undefined.\n");
    // // details.append("\nThe entity is a Government entity but type
    // // cannot be determined.\n");
    // } else {
    // LOG.debug("The entity is not a Government entity.");
    // // eleResults.append("\nEntity is non-government.\n");
    // details.append("\nThe entity is not a Government entity.\n");
    // }
    // } else {
    // LOG.debug("Buisness Number is not Valid.");
    // engineData.addRejectionComment("Buisness Number is not Valid.");
    // results.setOnError(true);
    // // eleResults.append("\nBuisness no. invalid.\n");
    // details.append("\nThe information on the request does not match the information from the ABN service.\n");
    // }
    // } else {
    // LOG.debug("No Response from ABN Service.");
    // engineData.addRejectionComment(response.getMessage());
    // results.setOnError(true);
    // // eleResults.append("\nNo Response from ABN Service.\n");
    // details.append("\n" + response.getMessage() + "\n");
    // }
    // } finally {
    // }

    isIsicInvalid = isISICValidForScenario(requestData, Arrays.asList(isicScenarioList));

    if (isIsicInvalid) {
      details.append("Invalid ISIC code, please choose another one based on industry.\n");
      engineData.addRejectionComment("Invalid ISIC code, please choose another one based on industry.");
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

  @Override
  public boolean performScenarioValidation(EntityManager entityManager, RequestData requestData, AutomationEngineData engineData,
      AutomationResult<ValidationOutput> result, StringBuilder details, ValidationOutput output) {
    //String[] scnarioList = { "XIGF", "CROSS", "MKTPC", "BLUMX", "DUMMY", "XDUMM", "INTER", "XINT", "AQSTN", "XAQST", "SOFT" };
    //skipCompanyCheckForScenario(requestData, engineData, Arrays.asList(scnarioList), true);
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
    AutomationResponse<?> rawResponse = client
        .executeAndWrap(AutomationServiceClient.AU_ABN_VALIDATION_SERVICE_ID, request, AutomationResponse.class);
    ObjectMapper mapper = new ObjectMapper();
    String json = mapper.writeValueAsString(rawResponse);
    TypeReference<AutomationResponse<BNValidationResponse>> ref = new TypeReference<AutomationResponse<BNValidationResponse>>() {
    };
    return mapper.readValue(json, ref);
  }
}
