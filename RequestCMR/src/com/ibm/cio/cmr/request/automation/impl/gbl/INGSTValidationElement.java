/**
 *@author Shivangi
 */
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
import com.ibm.cio.cmr.request.automation.CompanyVerifier;
import com.ibm.cio.cmr.request.automation.RequestData;
import com.ibm.cio.cmr.request.automation.impl.ValidatingElement;
import com.ibm.cio.cmr.request.automation.out.AutomationResult;
import com.ibm.cio.cmr.request.automation.out.ValidationOutput;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.util.dnb.DnBUtil;
import com.ibm.cmr.services.client.AutomationServiceClient;
import com.ibm.cmr.services.client.CmrServicesFactory;
import com.ibm.cmr.services.client.ServiceClient.Method;
import com.ibm.cmr.services.client.automation.AutomationResponse;
import com.ibm.cmr.services.client.automation.in.GstLayerRequest;
import com.ibm.cmr.services.client.automation.in.GstLayerResponse;

public class INGSTValidationElement extends ValidatingElement implements CompanyVerifier {

  public static final String SCENARIO_BLUEMIX = "BLUMX";
  public static final String SCENARIO_MARKETPLACE = "MKTPC";
  public static final String SCENARIO_ACQUISITION = "AQSTN";
  public static final String SCENARIO_PRIVATE_CUSTOMER = "PRIV";
  public static final String SCENARIO_FOREIGN = "CROSS";
  private static final Logger log = Logger.getLogger(INGSTValidationElement.class);

  public INGSTValidationElement(String requestTypes, String actionOnError, boolean overrideData, boolean stopOnError) {
    super(requestTypes, actionOnError, overrideData, stopOnError);
  }

  @Override
  public AutomationResult<ValidationOutput> executeElement(EntityManager entityManager, RequestData requestData, AutomationEngineData engineData)
      throws Exception {
    log.debug("Performing IN GST - Validation Element");

    Admin admin = requestData.getAdmin();
    Data data = requestData.getData();
    String scenario = data.getCustSubGrp();
    AutomationResult<ValidationOutput> output = buildResult(admin.getId().getReqId());
    ValidationOutput validation = new ValidationOutput();

    if (StringUtils.isBlank(data.getVat()) || SCENARIO_FOREIGN.equals(scenario)) {
      validation.setSuccess(true);
      validation.setMessage("Skipped");
      output.setResults("Skipped");
      output.setDetails("Skipping IN - GST Validation Element for " + scenario + " Scenario");
      log.debug("Skipping IN - GST Validation Element for " + scenario + " Scenario");
      return output;
    }

    // skip sos matching if matching records are found in SOS-RPA

    if (engineData.isDnbVerified() || "Y".equals(admin.getCompVerifiedIndc())) {
      validation.setSuccess(true);
      validation.setMessage("Skipped");
      output.setResults("Skipped");
      output.setDetails("Name and Address Matching is skipped as matching records are found in DnB");
      engineData.addPositiveCheckStatus(AutomationEngineData.DNB_MATCH);
      return output;
    }

    Addr zs01 = requestData.getAddress("ZS01");
    StringBuilder details = new StringBuilder();

    if (zs01 != null) {
      AutomationResponse<GstLayerResponse> response = getGstMatches(entityManager, admin.getId().getReqId(), zs01, data.getVat());
      String msg1 = "Valid GST and Company Name entered on the Request";
      String msg2 = "Valid Address and Company Name entered on the Request";
      List<String> messageList = new ArrayList<String>();
      messageList.add(msg1);
      messageList.add(msg2);
      if (response != null && response.isSuccess() && messageList.contains(response.getMessage())) {
        admin.setCompVerifiedIndc("Y");
        validation.setSuccess(true);
        validation.setMessage("Successful Execution");
        log.debug(response.getMessage());
        details.append("Record found in GST service : Address and Company Name Validated on API");

        details.append("\nCustomer Name = " + (StringUtils.isBlank(response.getRecord().getName()) ? "" : response.getRecord().getName()));
        details.append("\nGST = " + (StringUtils.isBlank(response.getRecord().getGst()) ? "" : response.getRecord().getGst()));
        details.append("\nAddress = " + (StringUtils.isBlank(response.getRecord().getAddress()) ? "" : response.getRecord().getAddress()));
        details.append("\nCity = " + (StringUtils.isBlank(response.getRecord().getCity()) ? "" : response.getRecord().getCity()));
        details.append("\nState = " + (StringUtils.isBlank(response.getRecord().getState()) ? "" : response.getRecord().getState()));
        details.append("\nZip = " + (StringUtils.isBlank(response.getRecord().getPostal()) ? "" : response.getRecord().getPostal()));

      } else {
        // company proof
        if (DnBUtil.isDnbOverrideAttachmentProvided(entityManager, admin.getId().getReqId())) {
          details.append("No High Quality API Matches were found for name and address.").append("\n");
          details.append("Supporting documentation(Company Proof) is provided by the requester as attachment").append("\n");
        } else {
          details.append("No High Quality API Matches were found for name and address.").append("\n");
          details.append("\nNo supporting documentation is provided by the requester for address.").append("\n");
        }
        output.setOnError(false);
        validation.setMessage("No Matches");
        validation.setSuccess(false);
        engineData.addNegativeCheckStatus("OTH", "Matches against API were found but no record matched the request data.");
      }
    }
    output.setDetails(details.toString());
    output.setResults(validation.getMessage());
    output.setProcessOutput(validation);
    return output;
  }

  private AutomationResponse<GstLayerResponse> getGstMatches(EntityManager entityManager, long reqId, Addr zs01, String vat) throws Exception {

    log.debug("Validating GST# " + vat + " for India");

    String baseUrl = SystemConfiguration.getValue("BATCH_SERVICES_URL");
    AutomationServiceClient autoClient = CmrServicesFactory.getInstance().createClient(baseUrl, AutomationServiceClient.class);
    autoClient.setReadTimeout(1000 * 60 * 5);
    autoClient.setRequestMethod(Method.Get);

    // calling India GST Validation Service
    log.debug("Calling GST Layer Service for Req_id : " + reqId);

    GstLayerRequest gstLayerRequest = new GstLayerRequest();
    String state = zs01.getStateProv();
    if (StringUtils.isNotBlank(state)) {
      String sql = ExternalizedQuery.getSql("AUTO.GET_STATE_DESCRIP");
      PreparedQuery query = new PreparedQuery(entityManager, sql);
      query.setParameter("STATE_PROV_CD", state);
      String stateDesc = query.getSingleResult(String.class);
      if (StringUtils.isNotBlank(stateDesc)) {
        state = stateDesc;
      }
    }
    gstLayerRequest.setGst(vat);
    gstLayerRequest.setCountry(state);
    gstLayerRequest.setName((StringUtils.isNotBlank(zs01.getCustNm1()) ? zs01.getCustNm1() : ""));
    gstLayerRequest.setAddress((StringUtils.isNotBlank(zs01.getAddrTxt()) ? zs01.getAddrTxt() : "") + " "
        + (StringUtils.isNotBlank(zs01.getAddrTxt2()) ? " " + zs01.getAddrTxt2() : "") + " "
        + (StringUtils.isNotBlank(zs01.getDept()) ? " " + zs01.getDept() : ""));
    gstLayerRequest.setCity(zs01.getCity1());
    gstLayerRequest.setPostal(zs01.getPostCd());

    log.debug("Connecting to the GST Layer Service at " + baseUrl);
    AutomationResponse<?> rawResponse = autoClient.executeAndWrap(AutomationServiceClient.IN_GST_SERVICE_ID, gstLayerRequest,
        AutomationResponse.class);
    ObjectMapper mapper = new ObjectMapper();
    String json = mapper.writeValueAsString(rawResponse);
    TypeReference<AutomationResponse<GstLayerResponse>> ref = new TypeReference<AutomationResponse<GstLayerResponse>>() {
    };
    return mapper.readValue(json, ref);
  }

  @Override
  public String getProcessCode() {
    return AutomationElementRegistry.IN_GST_VALIDATION;
  }

  @Override
  public String getProcessDesc() {
    return "IN - GST VALIDATION";
  }

}
