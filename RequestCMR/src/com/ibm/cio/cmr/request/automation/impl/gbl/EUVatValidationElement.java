package com.ibm.cio.cmr.request.automation.impl.gbl;

import java.util.Arrays;
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
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.ui.PageManager;
import com.ibm.cmr.services.client.AutomationServiceClient;
import com.ibm.cmr.services.client.CmrServicesFactory;
import com.ibm.cmr.services.client.ServiceClient.Method;
import com.ibm.cmr.services.client.automation.AutomationResponse;
import com.ibm.cmr.services.client.automation.eu.VatLayerRequest;
import com.ibm.cmr.services.client.automation.eu.VatLayerResponse;

public class EUVatValidationElement extends ValidatingElement implements CompanyVerifier {

  private static final Logger LOG = Logger.getLogger(EUVatValidationElement.class);
  private static final List<String> EU_COUNTRIES = Arrays.asList("BE", "AT", "BG", "CY", "CZ", "DE", "DK", "EE", "EL", "ES", "FI", "FR", "GB", "HR",
      "HU", "IE", "IT", "LT", "LU", "LV", "MT", "NL", "PL", "PT", "RO", "SE", "SI", "SK");

  public EUVatValidationElement(String requestTypes, String actionOnError, boolean overrideData, boolean stopOnError) {
    super(requestTypes, actionOnError, overrideData, stopOnError);
  }

  @Override
  public AutomationResult<ValidationOutput> executeElement(EntityManager entityManager, RequestData requestData, AutomationEngineData engineData)
      throws Exception {

    LOG.debug("Entering EU Vat Validation Element");
    // get request admin and data
    Admin admin = requestData.getAdmin();
    Data data = requestData.getData();
    long reqId = requestData.getAdmin().getId().getReqId();

    AutomationResult<ValidationOutput> output = buildResult(reqId);
    ValidationOutput validation = new ValidationOutput();

    Addr zs01 = requestData.getAddress("ZS01");
    Addr zp01 = requestData.getAddress("ZP01");
    StringBuilder details = new StringBuilder();
    try {
    	String landCntry;
        if (data.getCmrIssuingCntry() == "624" || data.getCmrIssuingCntry() == "788") {
          landCntry = zp01.getLandCntry();
        } else {
          landCntry = zs01.getLandCntry();
        }
      String landCntryForVies = getLandedCountryForVies(data.getCmrIssuingCntry(), landCntry, data.getCountryUse());
      if (landCntryForVies == null) {
        validation.setSuccess(true);
        validation.setMessage("No Landed Country");
        String msg = "Cannot verify VAT because no Landed Country was found on the main address. Further validation is needed.";
        output.setDetails(msg);
        output.setOnError(false);
        engineData.addNegativeCheckStatus("_vatLandCntry", msg);
        LOG.debug("Landed Country not found. Need review.");
      } else {
        if (engineData.isVatVerified()) {
          validation.setSuccess(true);
          validation.setMessage("Skipped.");
          output.setDetails("VAT has been marked verified through previous process executions. Skipping VIES verification.");
          LOG.debug("VAT has been marked verified through previous process executions. Skipping VIES verification.");
        } else if (!EU_COUNTRIES.contains(landCntryForVies)) {
          validation.setSuccess(true);
          validation.setMessage("Skipped.");
          output.setDetails("Landed Country does not belong to the European Union. Skipping VAT Validation.");
          LOG.debug("Landed Country does not belong to the European Union. Skipping VAT Validation.");
        } else if ("U".equals(admin.getReqType()) && !isVatChanged(entityManager, requestData)) {
          validation.setSuccess(true);
          validation.setMessage("VAT not updated");
          output.setDetails("Skipping VAT validation as VAT was not updated on the request.");
          LOG.debug("VAT not updated.");
        } else if (StringUtils.isBlank(data.getVat())) {
          validation.setSuccess(true);
          validation.setMessage("VAT not found");
          output.setDetails("No VAT specified on the request.");
          LOG.debug("No VAT specified on the request.");
        } else if (engineData.hasPositiveCheckStatus(AutomationEngineData.SKIP_VAT_CHECKS)) {
          validation.setSuccess(true);
          validation.setMessage("Skipped");
          output.setDetails("VAT checks not required for this record.");
          LOG.debug("VAT checks not required.");
        } else {
          AutomationResponse<VatLayerResponse> response = getVatLayerInfo(admin, data, landCntryForVies);
          if (response != null && response.isSuccess()) {
            if (response.getRecord().isValid()) {
              validation.setSuccess(true);
              validation.setMessage("Execution done.");
              LOG.debug("VAT and company information verified through VIES.");
              engineData.setVatVerified(true, "VAT Verified");

              details.append("VAT and company information verified through VIES.");
              details.append("\nCompany details from VIES :");
              details.append(
                  "\nCompany Name = " + (StringUtils.isBlank(response.getRecord().getCompanyName()) ? "" : response.getRecord().getCompanyName()));
              details
                  .append("\nVAT number = " + (StringUtils.isBlank(response.getRecord().getVatNumber()) ? "" : response.getRecord().getVatNumber()));
              output.setDetails(details.toString());
              engineData.setCompanySource("VIES");
              updateEntity(admin, entityManager);
            } else {
              validation.setSuccess(false);
              validation.setMessage("Review needed.");
              output.setDetails("VAT is invalid. Need review.");
              output.setOnError(true);
              engineData.addRejectionComment("OTH", "VAT is invalid.", "", "");
              LOG.debug("VAT is invalid.Need review.");
            }
          } else {
            validation.setSuccess(false);
            validation.setMessage("Execution failed.");
            output.setDetails(response.getMessage());
            output.setOnError(true);
            engineData.addRejectionComment("OTH", response.getMessage(), null, null);
            LOG.debug(response.getMessage());
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

  private String getLandedCountryForVies(String cmrIssuingCntry, String landCntry, String countryUse) {
	    String defaultLandedCountry = PageManager.getDefaultLandedCountry(cmrIssuingCntry);
	    String subRegion = !StringUtils.isBlank(countryUse) && countryUse.length() > 3 ? countryUse.substring(3) : "";
	    if (StringUtils.isNotBlank(subRegion) && EU_COUNTRIES.contains(subRegion)) {
	      // if subregion is part of EU countries eligible for VAT matching, use
	      // subregion as default country
	      defaultLandedCountry = subRegion;
	    }
	    if (!landCntry.equals(defaultLandedCountry) && !landCntry.equals(subRegion)) {
	      // if landed country is crossborder and not part of subregions
	      return landCntry;
	    }
	    return defaultLandedCountry;
	  }

  private AutomationResponse<VatLayerResponse> getVatLayerInfo(Admin admin, Data data, String landCntryForVies) throws Exception {
    AutomationServiceClient autoClient = CmrServicesFactory.getInstance().createClient(SystemConfiguration.getValue("BATCH_SERVICES_URL"),
        AutomationServiceClient.class);
    autoClient.setReadTimeout(1000 * 60 * 5);
    autoClient.setRequestMethod(Method.Get);

    VatLayerRequest request = new VatLayerRequest();
    request.setVat(data.getVat());
    request.setCountry(landCntryForVies);

    LOG.debug("Connecting to the EU VAT Layer Service at " + SystemConfiguration.getValue("BATCH_SERVICES_URL"));
    AutomationResponse<?> rawResponse = autoClient.executeAndWrap(AutomationServiceClient.EU_VAT_SERVICE_ID, request, AutomationResponse.class);
    ObjectMapper mapper = new ObjectMapper();
    String json = mapper.writeValueAsString(rawResponse);

    TypeReference<AutomationResponse<VatLayerResponse>> ref = new TypeReference<AutomationResponse<VatLayerResponse>>() {
    };
    return mapper.readValue(json, ref);
  }

  private boolean isVatChanged(EntityManager entityManager, RequestData requestData) {
    boolean isVATChanged = false;
    String vatImported = "";
    String vatCurrent = !StringUtils.isEmpty(requestData.getData().getVat()) ? requestData.getData().getVat() : "";
    String sql = ExternalizedQuery.getSql("AUTO.GET_EXISTING_VAT");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", requestData.getAdmin().getId().getReqId());
    query.setForReadOnly(true);
    vatImported = query.getSingleResult(String.class);
    vatImported = !StringUtils.isEmpty(vatImported) ? vatImported : "";
    if (!vatCurrent.trim().equalsIgnoreCase(vatImported.trim())) {
      isVATChanged = true;
    }
    return isVATChanged;
  }

  @Override
  public String getProcessCode() {
    return AutomationElementRegistry.EU_VAT_VALIDATION;
  }

  @Override
  public String getProcessDesc() {
    return "EU - VAT Validation";
  }

}
