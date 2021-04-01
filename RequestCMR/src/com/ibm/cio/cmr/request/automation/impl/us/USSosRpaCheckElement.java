package com.ibm.cio.cmr.request.automation.impl.us;

import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import com.ibm.cio.cmr.request.automation.AutomationElementRegistry;
import com.ibm.cio.cmr.request.automation.AutomationEngineData;
import com.ibm.cio.cmr.request.automation.CompanyVerifier;
import com.ibm.cio.cmr.request.automation.RequestData;
import com.ibm.cio.cmr.request.automation.impl.ValidatingElement;
import com.ibm.cio.cmr.request.automation.out.AutomationResult;
import com.ibm.cio.cmr.request.automation.out.ValidationOutput;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.entity.Scorecard;
import com.ibm.cmr.services.client.AutomationServiceClient;
import com.ibm.cmr.services.client.CmrServicesFactory;
import com.ibm.cmr.services.client.ServiceClient.Method;
import com.ibm.cmr.services.client.automation.AutomationResponse;
import com.ibm.cmr.services.client.automation.us.SosRequest;
import com.ibm.cmr.services.client.automation.us.SosResponse;

public class USSosRpaCheckElement extends ValidatingElement implements CompanyVerifier {

  // Business Partner
  public static final String SC_BP_END_USER = "END USER";
  public static final String SC_BP_POOL = "POOL";
  public static final String SC_BP_DEVELOP = "DEVELOP";
  public static final String SC_BP_E_HOST = "E-HOST";
  private static final Logger log = Logger.getLogger(USSosRpaCheckElement.class);

  public USSosRpaCheckElement(String requestTypes, String actionOnError, boolean overrideData, boolean stopOnError) {
    super(requestTypes, actionOnError, overrideData, stopOnError);
  }

  @Override
  public AutomationResult<ValidationOutput> executeElement(EntityManager entityManager, RequestData requestData, AutomationEngineData engineData)
      throws Exception {
    log.debug("Performing US SOS - RPA Check Element");

    Admin admin = requestData.getAdmin();
    Data data = requestData.getData();
    String scenario = data.getCustSubGrp();
    AutomationResult<ValidationOutput> output = buildResult(admin.getId().getReqId());
    ValidationOutput validation = new ValidationOutput();
    Scorecard scorecard = requestData.getScorecard();
    // skip sos matching if matching records are found in SOS-RPA
    if (engineData.isDnbVerified()) {
      validation.setSuccess(true);
      validation.setMessage("Skipped");
      output.setDetails("Sos-RPA Matching is skipped as matching records are found in DnB");
      engineData.addPositiveCheckStatus(AutomationEngineData.DNB_MATCH);
      return output;
    }
    if (SC_BP_END_USER.equals(scenario) && SC_BP_POOL.equals(scenario) && SC_BP_DEVELOP.equals(scenario) && SC_BP_E_HOST.equals(scenario)) {
      validation.setSuccess(true);
      validation.setMessage("Skipped");
      output.setDetails("Skipping SOS-RPA Check Element for US BP Scenario");
      log.debug("Skipping SOS-RPA Check Element for US BP Scenario");
      return output;
    }

    Addr zs01 = requestData.getAddress("ZS01");
    StringBuilder details = new StringBuilder();

    if (zs01 != null) {
      AutomationResponse<SosResponse> response = getSosMatches(admin.getId().getReqId(), zs01, admin);
      scorecard.setRpaMatchingResult("");
      if (response != null && response.isSuccess() && response.getRecord() != null) {
        admin.setCompVerifiedIndc("Y");
        scorecard.setRpaMatchingResult("Y");
        validation.setSuccess(true);
        validation.setMessage("Successful Execution");
        log.debug(response.getMessage());
        details.append("Record found in SOS.");
        details.append("\nCompany Id = " + (StringUtils.isBlank(response.getRecord().getCompanyId()) ? "" : response.getRecord().getCompanyId()));
        details.append("\nCustomer Name = " + (StringUtils.isBlank(response.getRecord().getLegalName()) ? "" : response.getRecord().getLegalName()));
        details.append("\nAddress = " + (StringUtils.isBlank(response.getRecord().getAddress1()) ? "" : response.getRecord().getAddress1()));
        details.append("\nState = " + (StringUtils.isBlank(response.getRecord().getState()) ? "" : response.getRecord().getState()));
        details.append("\nZip = " + (StringUtils.isBlank(response.getRecord().getZip()) ? "" : response.getRecord().getZip()));
        output.setDetails(details.toString());
        engineData.addPositiveCheckStatus(AutomationEngineData.SOS_MATCH);
      } else {
        scorecard.setRpaMatchingResult("N");
        validation.setSuccess(true);
        validation.setMessage("No Matches found");
        output.setDetails(response.getMessage());
        log.debug(response.getMessage());
      }
    }
    output.setResults(validation.getMessage());
    output.setProcessOutput(validation);
    return output;
  }

  private AutomationResponse<SosResponse> getSosMatches(long reqId, Addr zs01, Admin admin) throws Exception {
    AutomationServiceClient autoClient = CmrServicesFactory.getInstance().createClient(SystemConfiguration.getValue("BATCH_SERVICES_URL"),
        AutomationServiceClient.class);
    autoClient.setReadTimeout(1000 * 60 * 5);
    autoClient.setRequestMethod(Method.Post);

    // calling SOS-RPA Service
    log.debug("Calling SOS-RPA Service for Install - At (ZS01) address for Req_id : " + reqId);
    SosRequest requestInstallAt = new SosRequest();
    requestInstallAt.setName((StringUtils.isNotBlank(admin.getMainCustNm1()) ? admin.getMainCustNm1() : "")
        + (StringUtils.isNotBlank(admin.getMainCustNm2()) ? " " + admin.getMainCustNm2() : ""));
    requestInstallAt.setCity1(zs01.getCity1());
    requestInstallAt.setAddrTxt((StringUtils.isNotBlank(zs01.getAddrTxt()) ? zs01.getAddrTxt() : "")
        + (StringUtils.isNotBlank(zs01.getAddrTxt2()) ? " " + zs01.getAddrTxt2() : ""));
    requestInstallAt.setState(zs01.getStateProv());

    log.debug("Connecting to the SOS - RPA Service at " + SystemConfiguration.getValue("BATCH_SERVICES_URL"));
    AutomationResponse<?> rawResponseInstallAt = autoClient.executeAndWrap(AutomationServiceClient.US_SOS_RPA_SERVICE_ID, requestInstallAt,
        AutomationResponse.class);

    ObjectMapper mapper = new ObjectMapper();
    String json = mapper.writeValueAsString(rawResponseInstallAt);
    log.trace("SOS-RPA Service Response : " + json);
    TypeReference<AutomationResponse<SosResponse>> ref = new TypeReference<AutomationResponse<SosResponse>>() {
    };
    return mapper.readValue(json, ref);
  }

  @Override
  public String getProcessCode() {
    return AutomationElementRegistry.US_SOS_RPA_CHECK;
  }

  @Override
  public String getProcessDesc() {
    return "US - SOS-RPA CHECK";
  }

}
