package com.ibm.cio.cmr.request.automation.impl.gbl;

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
import com.ibm.cio.cmr.request.entity.listeners.ChangeLogListener;
import com.ibm.cio.cmr.request.util.SystemLocation;
import com.ibm.cmr.services.client.AutomationServiceClient;
import com.ibm.cmr.services.client.CmrServicesFactory;
import com.ibm.cmr.services.client.ServiceClient.Method;
import com.ibm.cmr.services.client.automation.AutomationResponse;
import com.ibm.cmr.services.client.automation.ap.anz.BNValidationRequest;
import com.ibm.cmr.services.client.automation.ap.anz.BNValidationResponse;

public class ANZBNValidationElement extends ValidatingElement implements CompanyVerifier {

  private static final Logger log = Logger.getLogger(ANZBNValidationElement.class);

  public ANZBNValidationElement(String requestTypes, String actionOnError, boolean overrideData, boolean stopOnError) {
    super(requestTypes, actionOnError, overrideData, stopOnError);
    // TODO Auto-generated constructor stub
  }

  @Override
  public AutomationResult<ValidationOutput> executeElement(EntityManager entityManager, RequestData requestData, AutomationEngineData engineData)
      throws Exception {
    long reqId = requestData.getAdmin().getId().getReqId();

    AutomationResult<ValidationOutput> output = buildResult(reqId);
    ValidationOutput validation = new ValidationOutput();
    // Log.debug("Entering global performScenarioCheck()");
    ChangeLogListener.setManager(entityManager);
    Data data = requestData.getData();
    Admin admin = requestData.getAdmin();
    StringBuilder details = new StringBuilder();
    Addr zs01 = requestData.getAddress("ZS01");
    String customerName = zs01.getCustNm1() + (StringUtils.isBlank(zs01.getCustNm2()) ? "" : " " + zs01.getCustNm2());

    log.debug("Calling BNValidation Service for Req_id : " + reqId);
    try {
      AutomationResponse<BNValidationResponse> response = getVatLayerInfo(admin, data);
      if (StringUtils.isBlank(data.getVat())) {
        validation.setSuccess(true);
        validation.setMessage("Execution done.");
        output.setDetails("No ABN# specified on the request.");
        log.debug("No ABN# specified on the request.");
      } else {
        if (response != null && response.isSuccess()) {
          if (response.getRecord().isValid()
              && customerName.equalsIgnoreCase(StringUtils.isBlank(response.getRecord().getCompanyName()) ? "" : response.getRecord()
                  .getCompanyName())) {
            validation.setSuccess(true);
            validation.setMessage("Execution done.");
            log.debug("Buisness Number and Legal Name verified using ABN/NBN lookup service");
            details.append("Buisness Number is Valid");
            details.append("\nBuisness Number and Legal Name verified using ABN/NBN lookup service");
            details.append("\nCompany details from VAT Layer :");
            details.append("\nCountry Code =" + response.getRecord().getCountryCode());
            details.append("\nBuisness Number =" + response.getRecord().getBusinessNumber());
            details.append("\nCompany Name =" + response.getRecord().getCompanyName());
            details.append("\nStreet =" + response.getRecord().getStreet());
            details.append("\nCity =" + response.getRecord().getCity());
            details.append("\nPostal Code =" + response.getRecord().getPostalCode());
            details.append("\nCountry Name =" + response.getRecord().getCountryName());
            details.append("\nStatus =" + response.getRecord().getStatus());
            details.append("\nState =" + response.getRecord().getState());
            output.setDetails(details.toString());
            output.setOnError(false);
            engineData.hasPositiveCheckStatus(AutomationEngineData.VAT_VERIFIED);
            // Defect CMR - 1375
            // admin.setCompVerifiedIndc("Y");
            // admin.setCompInfoSrc("Business Number Validation");
            if (SystemLocation.AUSTRALIA.equals(data.getCmrIssuingCntry())) {
              engineData.setCompanySource("ABN");
            } else if (SystemLocation.NEW_ZEALAND.equals(data.getCmrIssuingCntry())) {
              engineData.setCompanySource("NZBN");
            }
          } else if (!response.getRecord().isValid()) {
            validation.setSuccess(false);
            validation.setMessage("Buisness no. invalid");
            output.setDetails("The information on the request does not match the information from the service");
            output.setOnError(true);
            engineData.addRejectionComment("OTH", "Buisness Number is not Valid.", "", "");
            log.debug("The Company buisness number is not the same as the one on the request.");
          } else {
            // validation.setSuccess(false);
            validation.setMessage("Legal Name not same as API's");
            output.setDetails("The Customer Legal Name on the request does not match the information from the service.");
            // output.setOnError(true);
            // engineData.addRejectionComment("Legal Name on request and API
            // doesn't match.");
            engineData.addNegativeCheckStatus("ABNLegalName", "Legal Name on request and API doesn't match.");
            log.debug("The Customer Legal Name on the request does not match the information from the service.");
          }
        } else {
          validation.setSuccess(false);
          validation.setMessage("Execution failed");
          output.setDetails(response.getMessage());
          output.setOnError(true);
          engineData.addRejectionComment("OTH", response.getMessage(), "", "");
          log.debug(response.getMessage());
        }
      }
    } finally {
      ChangeLogListener.clearManager();
    }
    output.setResults(validation.getMessage());
    output.setProcessOutput(validation);
    return output;

  }

  private AutomationResponse<BNValidationResponse> getVatLayerInfo(Admin admin, Data data) throws Exception {
    AutomationServiceClient client = CmrServicesFactory.getInstance().createClient(SystemConfiguration.getValue("BATCH_SERVICES_URL"),
        AutomationServiceClient.class);
    client.setReadTimeout(1000 * 60 * 5);
    client.setRequestMethod(Method.Get);

    BNValidationRequest request = new BNValidationRequest();
    request.setBusinessNumber(data.getVat());
    System.out.println(request + request.getBusinessNumber());
    log.debug("Connecting to the ABNValidation service at " + SystemConfiguration.getValue("BATCH_SERVICES_URL"));
    AutomationResponse<?> rawResponse = client
        .executeAndWrap(AutomationServiceClient.AU_ABN_VALIDATION_SERVICE_ID, request, AutomationResponse.class);
    ObjectMapper mapper = new ObjectMapper();
    String json = mapper.writeValueAsString(rawResponse);
    TypeReference<AutomationResponse<BNValidationResponse>> ref = new TypeReference<AutomationResponse<BNValidationResponse>>() {
    };
    return mapper.readValue(json, ref);

  }

  @Override
  public String getProcessCode() {
    // TODO Auto-generated method stub
    return AutomationElementRegistry.ANZ_BN_VALIDATION;
  }

  @Override
  public String getProcessDesc() {
    // TODO Auto-generated method stub
    return "ANZ - Business Number Validation";
  }
}
