package com.ibm.cio.cmr.request.automation.impl.gbl;

import java.util.ArrayList;
import java.util.List;

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
import com.ibm.cio.cmr.request.util.dnb.DnBUtil;
import com.ibm.cmr.services.client.AutomationServiceClient;
import com.ibm.cmr.services.client.CmrServicesFactory;
import com.ibm.cmr.services.client.MatchingServiceClient;
import com.ibm.cmr.services.client.ServiceClient.Method;
import com.ibm.cmr.services.client.automation.AutomationResponse;
import com.ibm.cmr.services.client.automation.ap.anz.BNValidationRequest;
import com.ibm.cmr.services.client.automation.ap.anz.BNValidationResponse;
import com.ibm.cmr.services.client.matching.MatchingResponse;
import com.ibm.cmr.services.client.matching.dnb.DnBMatchingResponse;
import com.ibm.cmr.services.client.matching.gbg.GBGFinderRequest;

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
    AutomationResponse<BNValidationResponse> response = null;
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
      try {
        response = getVatLayerInfo(admin, data);
      } catch (Exception e) {
        if (response == null || !response.isSuccess()) {
          log.debug("Failed to connect to ABN Service. Checking ABN and Company Name with D&B.");
          details.append("Failed to Connect to ABN Service.");
          details.append("\nD&B Matching Results are:");
          List<DnBMatchingResponse> matches = getMatches(requestData, engineData, zs01, false);
          if (data.getVat() != null && !StringUtils.isBlank(data.getVat())) {
            boolean orgIdFound = false;
            boolean isOrgIdMatched = false;
            if (!matches.isEmpty()) {
              for (DnBMatchingResponse dnbRecord : matches) {
                if (dnbRecord.getConfidenceCode() > 7) {
                  // check if the record closely matches D&B. This really
                  // validates
                  // input against record
                  boolean closelyMatches = DnBUtil.closelyMatchesDnb(data.getCmrIssuingCntry(), zs01, admin, dnbRecord);
                  if (closelyMatches) {
                    log.debug("DUNS " + dnbRecord.getDunsNo() + " matches the request data.");
                    isOrgIdMatched = "Y".equals(dnbRecord.getOrgIdMatch());
                    orgIdFound = StringUtils.isNotBlank(DnBUtil.getVAT(dnbRecord.getDnbCountry(), dnbRecord.getOrgIdDetails()));
                    if (orgIdFound && isOrgIdMatched) {
                      engineData.setVatVerified(true, "VAT Verified");
                      validation.setSuccess(true);
                      validation.setMessage("Execution done.");
                      details.append("\nBusiness Number is Valid");
                      details.append("\nBusiness Number and Legal Name verified using D&B");
                      output.setDetails(details.toString());
                      engineData.addPositiveCheckStatus(AutomationEngineData.VAT_VERIFIED);
                      break;
                    } else {
                      validation.setSuccess(false);
                      validation.setMessage("\nBusiness no. invalid");
                      details.append("\nThe information on the request does not match the information from the service");
                      output.setDetails(details.toString());
                      output.setOnError(true);
                      engineData.addRejectionComment("OTH", "Business Number is not Valid.", "", "");
                      log.debug("The Company business number is not the same as the one on the request.");
                      break;
                    }
                  } else {
                    validation.setMessage("\nLegal Name not same as D&B's");
                    details.append("\nThe Customer Legal Name on the request does not match the information from the service.");
                    output.setDetails(details.toString());
                    engineData.addNegativeCheckStatus("ABNLegalName", "Legal Name on request and D&B doesn't match.");
                    log.debug("The Customer Legal Name on the request does not match the information from the service.");
                  }
                }
              }
            } else {
              validation.setSuccess(false);
              validation.setMessage("\nNo Matches Found in D&B");
              details.append("\nThe information on the request does not match the information from the D&B");
              output.setDetails(details.toString());
              output.setOnError(true);
              engineData.addRejectionComment("OTH", "No Matches found in D&B.", "", "");
              log.debug("No Matches found in D&B.");
            }
          }
        }
      }
      if (StringUtils.isBlank(data.getVat())) {
        validation.setSuccess(true);
        validation.setMessage("Execution done.");
        output.setDetails("No ABN# specified on the request.");
        log.debug("No ABN# specified on the request.");
      } else {
        if (response != null && response.isSuccess()) {
          if (response.getRecord().isValid() && customerName
              .equalsIgnoreCase(StringUtils.isBlank(response.getRecord().getCompanyName()) ? "" : response.getRecord().getCompanyName())) {
            validation.setSuccess(true);
            validation.setMessage("Execution done.");
            log.debug("Business Number and Legal Name verified using ABN/NBN lookup service");
            details.append("Business Number is Valid");
            details.append("\nBusiness Number and Legal Name verified using ABN/NBN lookup service");
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
            engineData.addPositiveCheckStatus(AutomationEngineData.VAT_VERIFIED);
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
            validation.setMessage("Business no. invalid");
            output.setDetails("The information on the request does not match the information from the service");
            output.setOnError(true);
            engineData.addRejectionComment("OTH", "Buisness Number is not Valid.", "", "");
            log.debug("The Company business number is not the same as the one on the request.");
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
    AutomationResponse<?> rawResponse = client.executeAndWrap(AutomationServiceClient.AU_ABN_VALIDATION_SERVICE_ID, request,
        AutomationResponse.class);
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

  public List<DnBMatchingResponse> getMatches(RequestData requestData, AutomationEngineData engineData, Addr addr, boolean isOrgIdMatchOnly)
      throws Exception {
    Admin admin = requestData.getAdmin();
    Data data = requestData.getData();
    List<DnBMatchingResponse> dnbMatches = new ArrayList<DnBMatchingResponse>();
    if (addr == null) {
      addr = requestData.getAddress("ZS01");
    }
    GBGFinderRequest request = createRequest(admin, data, addr, isOrgIdMatchOnly);
    MatchingServiceClient client = CmrServicesFactory.getInstance().createClient(SystemConfiguration.getValue("BATCH_SERVICES_URL"),
        MatchingServiceClient.class);
    client.setReadTimeout(1000 * 60 * 5);
    log.debug("Connecting to the Advanced D&B Matching Service at " + SystemConfiguration.getValue("BATCH_SERVICES_URL"));
    MatchingResponse<?> rawResponse = client.executeAndWrap(MatchingServiceClient.DNB_SERVICE_ID, request, MatchingResponse.class);
    ObjectMapper mapper = new ObjectMapper();
    String json = mapper.writeValueAsString(rawResponse);

    TypeReference<MatchingResponse<DnBMatchingResponse>> ref = new TypeReference<MatchingResponse<DnBMatchingResponse>>() {
    };

    MatchingResponse<DnBMatchingResponse> response = mapper.readValue(json, ref);
    if (response != null) {
      dnbMatches = response.getMatches();
    }

    return dnbMatches;
  }

  public GBGFinderRequest createRequest(Admin admin, Data data, Addr addr, Boolean isOrgIdMatchOnly) {
    GBGFinderRequest request = new GBGFinderRequest();
    request.setMandt(SystemConfiguration.getValue("MANDT"));
    if (StringUtils.isNotBlank(data.getVat())) {
      request.setOrgId(data.getVat());
    } else if (StringUtils.isNotBlank(addr.getVat())) {
      request.setOrgId(addr.getVat());
    }

    if (addr != null) {
      if (isOrgIdMatchOnly) {
        request.setLandedCountry(addr.getLandCntry());
      } else {
        request.setCity(addr.getCity1());
        request.setCustomerName(addr.getCustNm1() + (StringUtils.isBlank(addr.getCustNm2()) ? "" : " " + addr.getCustNm2()));
        request.setStreetLine1(addr.getAddrTxt());
        request.setStreetLine2(addr.getAddrTxt2());
        request.setLandedCountry(addr.getLandCntry());
        request.setPostalCode(addr.getPostCd());
        request.setStateProv(addr.getStateProv());
        // request.setMinConfidence("8");
      }
    }

    return request;
  }
}
