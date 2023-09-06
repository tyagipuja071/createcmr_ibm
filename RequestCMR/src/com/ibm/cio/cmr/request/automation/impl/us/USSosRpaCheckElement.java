/**
 *@author Shivangi
 */
package com.ibm.cio.cmr.request.automation.impl.us;

import java.util.Arrays;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import com.ibm.cio.cmr.request.CmrConstants;
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
import com.ibm.cio.cmr.request.util.RequestUtils;
import com.ibm.cmr.services.client.AutomationServiceClient;
import com.ibm.cmr.services.client.CmrServicesFactory;
import com.ibm.cmr.services.client.ServiceClient.Method;
import com.ibm.cmr.services.client.automation.AutomationResponse;
import com.ibm.cmr.services.client.automation.us.SosRequest;
import com.ibm.cmr.services.client.automation.us.SosResponse;

/**
 *
 * @author Shivangi
 *
 */

public class USSosRpaCheckElement extends ValidatingElement implements CompanyVerifier {

  // Business Partner
  public static final String SC_BP_END_USER = "END USER";
  public static final String SC_BP_POOL = "POOL";
  public static final String SC_BP_DEVELOP = "DEVELOP";
  public static final String SC_BP_E_HOST = "E-HOST";
  private static final List<String> RELEVANT_ADDRESSES = Arrays.asList(CmrConstants.RDC_SOLD_TO, CmrConstants.RDC_INSTALL_AT);
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
    StringBuilder details = new StringBuilder();
    boolean payGoAddredited = RequestUtils.isPayGoAccredited(entityManager, admin.getSourceSystId());
    // skip sos matching if matching records are found in SOS-RPA
    /*
     * if (engineData.isDnbVerified() ||
     * "Y".equals(admin.getCompVerifiedIndc())) { validation.setSuccess(true);
     * validation.setMessage("Skipped"); output.setResults("Skipped"); output.
     * setDetails("Sos-RPA Matching is skipped as matching records are found in DnB"
     * ); engineData.addPositiveCheckStatus(AutomationEngineData.DNB_MATCH);
     * return output; }
     */
    if (SC_BP_END_USER.equals(scenario) && SC_BP_POOL.equals(scenario) && SC_BP_DEVELOP.equals(scenario) && SC_BP_E_HOST.equals(scenario)) {
      validation.setSuccess(true);
      validation.setMessage("Skipped");
      output.setResults("Skipped");
      output.setDetails("Skipping SOS-RPA Check Element for US BP Scenario");
      log.debug("Skipping SOS-RPA Check Element for US BP Scenario");
      return output;
    }
    String matchRPA = "";
    int itr = 0;
    for (String addrType : RELEVANT_ADDRESSES) {
      Addr address = requestData.getAddress(addrType);
      Boolean containsInvoice = true;
      Addr invoice = requestData.getAddress("ZI01");
      if (invoice == null) {
        containsInvoice = false;
      }
      if (address != null) {
        AutomationResponse<SosResponse> response = getSosMatches(admin.getId().getReqId(), address, admin);
        scorecard.setRpaMatchingResult("");
        log.debug("Scorecard Updated for SOS-RPA to Not Done");
        details.append("SOS-RPA Matching Results for " + addrType + "\n");
        if (response != null && response.isSuccess() && response.getRecord() != null) {
          admin.setCompVerifiedIndc("Y");
          scorecard.setRpaMatchingResult("Y");
          log.debug("Scorecard Updated for SOS-RPA to" + scorecard.getRpaMatchingResult());
          validation.setSuccess(true);
          if ((matchRPA == "Y" && itr == 1) || !containsInvoice) {
            validation.setMessage("Successful Execution");
          } else if (matchRPA != "Y" && itr == 1) {
            validation.setMessage("Partial Matches found");
          }
          matchRPA = "Y";
          log.debug(response.getMessage());
          details.append("Record found in SOS.");
          details.append("\nCompany Id = " + (StringUtils.isBlank(response.getRecord().getCompanyId()) ? "" : response.getRecord().getCompanyId()));
          details
              .append("\nCustomer Name = " + (StringUtils.isBlank(response.getRecord().getLegalName()) ? "" : response.getRecord().getLegalName()));
          details.append("\nAddress = " + (StringUtils.isBlank(response.getRecord().getAddress1()) ? "" : response.getRecord().getAddress1()));
          details.append("\nState = " + (StringUtils.isBlank(response.getRecord().getState()) ? "" : response.getRecord().getState()));
          details.append("\nZip = " + (StringUtils.isBlank(response.getRecord().getZip()) ? "" : response.getRecord().getZip()) + "\n\n");
          output.setDetails(details.toString());
          engineData.addPositiveCheckStatus(AutomationEngineData.SOS_MATCH);
          engineData.clearNegativeCheckStatus("DnBMatch");
          engineData.clearNegativeCheckStatus("DNB_VAT_MATCH_CHECK_FAIL");
          engineData.clearNegativeCheckStatus("DnBSoSMatch");
        } else {
          if ((itr == 1 || !containsInvoice) && ("N".equals(scorecard.getRpaMatchingResult()) || "".equals(scorecard.getRpaMatchingResult()))) {
            scorecard.setRpaMatchingResult("N");
          }
          log.debug("Scorecard Updated for SOS-RPA to" + scorecard.getRpaMatchingResult());
          validation.setSuccess(true);
          if ("C".equals(admin.getReqType()) && payGoAddredited && ("ZI01".equals(addrType) || ("ZS01".equals(addrType)))) {
            output.setOnError(false);
         //   admin.setPaygoProcessIndc("Y");
            details.append("Skipping checks for PayGo Addredited Customers.");
          }
          if ("O".equals(admin.getCompVerifiedIndc()) || "Y".equals(admin.getCompVerifiedIndc())) {
            output.setOnError(false);
            admin.setCompVerifiedIndc("Y");
          } else {
            if ((itr == 1 || !containsInvoice) && !payGoAddredited) {
              output.setOnError(true);
              engineData.addNegativeCheckStatus("DnBSoSMatch", "No high quality matches with D&B and SOS-RPA records.");
            }
          }
          if (matchRPA == "Y" && itr == 1) {
            validation.setMessage("Partial Matches found");
          } else if ((matchRPA != "Y" && itr == 1) || !containsInvoice) {
            validation.setMessage("No Matches found");
          }
          details.append("No Matches Found\n\n");
          output.setDetails(details.toString());
          log.debug(response.getMessage());
        }
        itr = 1;
      }
    }
    output.setResults(validation.getMessage());
    output.setProcessOutput(validation);
    return output;
  }

  public static AutomationResponse<SosResponse> getSosMatches(long reqId, Addr addr, Admin admin) throws Exception {
    AutomationServiceClient autoClient = CmrServicesFactory.getInstance().createClient(SystemConfiguration.getValue("BATCH_SERVICES_URL"),
        AutomationServiceClient.class);
    autoClient.setReadTimeout(1000 * 60 * 5);
    autoClient.setRequestMethod(Method.Post);
    // calling SOS-RPA Service
    log.debug("Calling SOS-RPA Service for Req_id : " + reqId);
    SosRequest requestAddr = new SosRequest();
    requestAddr.setName((StringUtils.isNotBlank(admin.getMainCustNm1()) ? admin.getMainCustNm1() : "")
        + (StringUtils.isNotBlank(admin.getMainCustNm2()) ? " " + admin.getMainCustNm2() : ""));
    requestAddr.setCity1(addr.getCity1());
    requestAddr.setAddrTxt((StringUtils.isNotBlank(addr.getAddrTxt()) ? addr.getAddrTxt() : "")
        + (StringUtils.isNotBlank(addr.getAddrTxt2()) ? " " + addr.getAddrTxt2() : ""));
    requestAddr.setState(addr.getStateProv());
    log.debug("Connecting to the SOS - RPA Service at " + SystemConfiguration.getValue("BATCH_SERVICES_URL"));
    AutomationResponse<?> rawResponseInstallAt = autoClient.executeAndWrap(AutomationServiceClient.US_SOS_RPA_SERVICE_ID, requestAddr,
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