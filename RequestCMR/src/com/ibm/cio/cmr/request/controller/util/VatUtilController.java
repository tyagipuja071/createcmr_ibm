/**
 * 
 */
package com.ibm.cio.cmr.request.controller.util;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.util.JpaManager;
import com.ibm.cmr.services.client.AutomationServiceClient;
import com.ibm.cmr.services.client.CmrServicesFactory;
import com.ibm.cmr.services.client.ServiceClient.Method;
import com.ibm.cmr.services.client.ValidatorClient;
import com.ibm.cmr.services.client.automation.AutomationResponse;
import com.ibm.cmr.services.client.automation.ap.anz.BNValidationRequest;
import com.ibm.cmr.services.client.automation.ap.anz.BNValidationResponse;
import com.ibm.cmr.services.client.automation.cn.CNRequest;
import com.ibm.cmr.services.client.automation.cn.CNResponse;
import com.ibm.cmr.services.client.automation.eu.VatLayerRequest;
import com.ibm.cmr.services.client.automation.eu.VatLayerResponse;
import com.ibm.cmr.services.client.automation.in.GstLayerRequest;
import com.ibm.cmr.services.client.automation.in.GstLayerResponse;
import com.ibm.cmr.services.client.validator.PostalCodeValidateRequest;
import com.ibm.cmr.services.client.validator.ValidationResult;
import com.ibm.cmr.services.client.validator.VatValidateRequest;

/**
 * @author Jeffrey Zamora
 * 
 */
@Controller
public class VatUtilController {

  private static final Logger LOG = Logger.getLogger(VatUtilController.class);

  @RequestMapping(
      value = "/vat")
  public ModelMap checkVat(HttpServletRequest request, HttpServletResponse response) throws Exception {
    ModelMap map = new ModelMap();

    ValidationResult validation = null;
    String country = request.getParameter("country");
    if (StringUtils.isEmpty(country)) {
      validation = ValidationResult.error("'country' param is required.");
    }
    String vat = request.getParameter("vat");
    if (StringUtils.isEmpty(vat)) {
      validation = ValidationResult.error("'vat' param is required.");
    }
    if (validation == null || validation.isSuccess()) {

      LOG.debug("Validating VAT " + vat + " for country " + country);

      String baseUrl = SystemConfiguration.getValue("CMR_SERVICES_URL");
      ValidatorClient client = CmrServicesFactory.getInstance().createClient(baseUrl, ValidatorClient.class);
      VatValidateRequest vatRequest = new VatValidateRequest();
      vatRequest.setCountry(country);
      vatRequest.setVat(vat);

      validation = client.executeAndWrap(ValidatorClient.VAT_APP_ID, vatRequest, ValidationResult.class);
    }
    map.addAttribute("result", validation);
    return map;
  }

  @RequestMapping(
      value = "/vat/vies")
  public ModelMap validateVATUsingVies(HttpServletRequest request, HttpServletResponse response) throws Exception {
    ModelMap map = new ModelMap();

    ValidationResult validation = null;
    String country = request.getParameter("country");
    if (StringUtils.isEmpty(country)) {
      validation = ValidationResult.error("'country' param is required.");
    }
    String vat = request.getParameter("vat");
    if (StringUtils.isEmpty(vat)) {
      validation = ValidationResult.error("'vat' param is required.");
    }
    if (validation == null || validation.isSuccess()) {

      LOG.debug("Validating VAT " + vat + " for country " + country);

      String baseUrl = SystemConfiguration.getValue("CMR_SERVICES_URL");
      AutomationServiceClient autoClient = CmrServicesFactory.getInstance().createClient(baseUrl, AutomationServiceClient.class);
      autoClient.setReadTimeout(1000 * 60 * 5);
      autoClient.setRequestMethod(Method.Get);

      VatLayerRequest vatLayerRequest = new VatLayerRequest();
      vatLayerRequest.setVat(vat);
      vatLayerRequest.setCountry(country);

      LOG.debug("Connecting to the EU VAT Layer Service at " + baseUrl);
      AutomationResponse<?> rawResponse = autoClient.executeAndWrap(AutomationServiceClient.EU_VAT_SERVICE_ID, vatLayerRequest,
          AutomationResponse.class);
      ObjectMapper mapper = new ObjectMapper();
      String json = mapper.writeValueAsString(rawResponse);
      TypeReference<AutomationResponse<VatLayerResponse>> ref = new TypeReference<AutomationResponse<VatLayerResponse>>() {
      };
      AutomationResponse<VatLayerResponse> vatResponse = mapper.readValue(json, ref);
      if (vatResponse != null && vatResponse.isSuccess()) {
        if (vatResponse.getRecord().isValid()) {
          validation = ValidationResult.success();
        } else {
          validation = ValidationResult.error("VAT provided on the request is not valid as per VIES Validation. Please verify the VAT provided.");
        }
      } else {
        validation = ValidationResult
            .error("VAT Validation with VIES Failed." + (StringUtils.isNotBlank(vatResponse.getMessage()) ? vatResponse.getMessage() : ""));
      }
    }
    map.addAttribute("result", validation);
    return map;
  }

  @RequestMapping(
      value = "/in/gst")
  public ModelMap validateGST(HttpServletRequest request, HttpServletResponse response) throws Exception {
    ModelMap map = new ModelMap();

    ValidationResult validation = null;
    String state = "";
    if (!StringUtils.isEmpty(request.getParameter("country"))) {
      state = request.getParameter("country");
    }
    String vat = request.getParameter("vat");
    if (StringUtils.isEmpty(vat)) {
      validation = ValidationResult.error("'Gst#' param is required.");
    }
    String name = request.getParameter("name");
    if (StringUtils.isEmpty(name)) {
      validation = ValidationResult.error("'name' param is required.");
    }
    String address = request.getParameter("address");
    if (StringUtils.isEmpty(address)) {
      validation = ValidationResult.error("'address' param is required.");
    }
    String postal = request.getParameter("postal");
    if (StringUtils.isEmpty(postal)) {
      validation = ValidationResult.error("'postal' param is required.");
    }
    String city = request.getParameter("city");
    if (StringUtils.isEmpty(city)) {
      validation = ValidationResult.error("'city' param is required.");
    }
    if (validation == null || validation.isSuccess()) {

      LOG.debug("Validating GST# " + vat + " for India");

      String baseUrl = SystemConfiguration.getValue("CMR_SERVICES_URL");
      AutomationServiceClient autoClient = CmrServicesFactory.getInstance().createClient(baseUrl, AutomationServiceClient.class);
      autoClient.setReadTimeout(1000 * 60 * 5);
      autoClient.setRequestMethod(Method.Get);

      GstLayerRequest gstLayerRequest = new GstLayerRequest();
      gstLayerRequest.setGst(vat);
      gstLayerRequest.setCountry(state);
      gstLayerRequest.setName(name);
      gstLayerRequest.setAddress(address);
      gstLayerRequest.setCity(city);
      gstLayerRequest.setPostal(postal);

      LOG.debug("Connecting to the GST Layer Service at " + baseUrl);
      AutomationResponse<?> rawResponse = autoClient.executeAndWrap(AutomationServiceClient.IN_GST_SERVICE_ID, gstLayerRequest,
          AutomationResponse.class);
      ObjectMapper mapper = new ObjectMapper();
      String json = mapper.writeValueAsString(rawResponse);
      TypeReference<AutomationResponse<GstLayerResponse>> ref = new TypeReference<AutomationResponse<GstLayerResponse>>() {
      };
      AutomationResponse<GstLayerResponse> gstResponse = mapper.readValue(json, ref);
      if (gstResponse != null && gstResponse.isSuccess()) {
        if (gstResponse.getMessage().equals("Valid GST and Company Name entered on the Request")
            || gstResponse.getMessage().equals("Valid Address and Company Name entered on the Request")) {
          validation = ValidationResult.success();
        } else {
          validation = ValidationResult.error("GST# provided on the request is not valid as per GST Validation. Please verify the GST# provided.");
        }
      } else {
        validation = ValidationResult
            .error("GST Validation Failed." + (StringUtils.isNotBlank(gstResponse.getMessage()) ? gstResponse.getMessage() : ""));
      }
    }
    map.addAttribute("result", validation);
    return map;
  }

  @RequestMapping(
      value = "/zip")
  public ModelMap checkPostalCode(HttpServletRequest request, HttpServletResponse response) throws Exception {
    ModelMap map = new ModelMap();

    ValidationResult validation = null;
    String country = request.getParameter("country");
    if (StringUtils.isEmpty(country)) {
      validation = ValidationResult.error("'country' param is required.");
    }
    String postalCode = request.getParameter("postalCode");
    if (StringUtils.isEmpty(postalCode)) {
      postalCode = "";
      // validation = ValidationResult.error("'postalCode' param is required.");
    }

    if (validation == null || validation.isSuccess()) {
      String loc = request.getParameter("sysLoc");

      String baseUrl = SystemConfiguration.getValue("CMR_SERVICES_URL");
      String mandt = SystemConfiguration.getValue("MANDT");

      PostalCodeValidateRequest zipRequest = new PostalCodeValidateRequest();
      zipRequest.setMandt(mandt);
      zipRequest.setPostalCode(postalCode);
      zipRequest.setSysLoc(loc);
      zipRequest.setCountry(country);

      LOG.debug("Validating Postal Code " + postalCode + " for country " + country + " (mandt: " + mandt + " sysloc: " + loc + ")");

      ValidatorClient client = CmrServicesFactory.getInstance().createClient(baseUrl, ValidatorClient.class);
      validation = client.executeAndWrap(ValidatorClient.POSTAL_CODE_APP_ID, zipRequest, ValidationResult.class);
    }
    map.addAttribute("result", validation);
    return map;
  }

  @RequestMapping(
      value = "/au/abn")
  public ModelMap validateABN(HttpServletRequest request, HttpServletResponse response) throws Exception {
    ModelMap map = new ModelMap();

    ValidationResult validation = null;
    Boolean comp_proof_Abn = false;

    String abn = request.getParameter("abn");
    if (StringUtils.isEmpty(abn)) {
      validation = ValidationResult.error("'ABN' param is required.");
    }
    String reqId = request.getParameter("reqId");
    if (StringUtils.isEmpty(reqId)) {
      validation = ValidationResult.error("'reqId' param is required.");
    }
    comp_proof_Abn = isDnbOverrideAttachmentProvided(reqId);

    if (validation == null || validation.isSuccess()) {

      LOG.debug("Validating ABN " + abn + " for Australia");

      String baseUrl = SystemConfiguration.getValue("CMR_SERVICES_URL");
      AutomationServiceClient autoClient = CmrServicesFactory.getInstance().createClient(baseUrl, AutomationServiceClient.class);
      autoClient.setReadTimeout(1000 * 60 * 5);
      autoClient.setRequestMethod(Method.Get);

      BNValidationRequest requestToAPI = new BNValidationRequest();
      requestToAPI.setBusinessNumber(abn);
      System.out.println(requestToAPI + requestToAPI.getBusinessNumber());

      LOG.debug("Connecting to the ABNValidation service at " + SystemConfiguration.getValue("BATCH_SERVICES_URL"));
      AutomationResponse<?> rawResponse = autoClient.executeAndWrap(AutomationServiceClient.AU_ABN_VALIDATION_SERVICE_ID, requestToAPI,
          AutomationResponse.class);
      ObjectMapper mapper = new ObjectMapper();
      String json = mapper.writeValueAsString(rawResponse);

      TypeReference<AutomationResponse<BNValidationResponse>> ref = new TypeReference<AutomationResponse<BNValidationResponse>>() {
      };
      AutomationResponse<BNValidationResponse> abnResponse = mapper.readValue(json, ref);
      if (StringUtils.isBlank(abn)) {
        validation = ValidationResult.success();
      } else if (abnResponse != null && abnResponse.isSuccess()) {
        if (abnResponse.getRecord().isValid() && !(abnResponse.getRecord().getStatus().equalsIgnoreCase("Cancelled"))) {
          validation = ValidationResult.success();
        } else if (abnResponse.getRecord().isValid()
            && (!abnResponse.getRecord().getStatus().equals(null) && abnResponse.getRecord().getStatus().equalsIgnoreCase("Cancelled"))
            && !comp_proof_Abn) {
          validation = ValidationResult.error(
              "ABN provided on the request cancelled as per API Validation. Please provide Supporting documentation(Company Proof) as attachment");
        } else {
          validation = ValidationResult.error("ABN provided on the request mismatches with API.Please provide correct ABN#");
        }
      } else {
        validation = ValidationResult.error("ABN Validation Failed.Failed to connect to ABN Service. Checking ABN and Company Name with D&B.");
      }
    }
    map.addAttribute("result", validation);
    return map;
  }

  @RequestMapping(
      value = "/cn/tyc")
  public ModelMap checkCnAddr(HttpServletRequest request, HttpServletResponse response) throws Exception {
    ModelMap map = new ModelMap();
    // ValidationResult validation = null;
    String busnType = request.getParameter("busnType");
    String cnName = request.getParameter("cnName");
    String keyword = "";
    CNRequest cnRequest = new CNRequest();
    if (StringUtils.isEmpty(busnType)) {
      keyword = cnName;
    } else {
      keyword = busnType;
    }
    cnRequest.setKeyword(keyword);

    AutomationServiceClient client = CmrServicesFactory.getInstance().createClient(SystemConfiguration.getValue("BATCH_SERVICES_URL"),
        AutomationServiceClient.class);
    client.setReadTimeout(1000 * 60 * 5);
    client.setRequestMethod(Method.Get);

    LOG.debug("Connecting to the CNValidation service at " + SystemConfiguration.getValue("BATCH_SERVICES_URL"));
    AutomationResponse<?> rawResponse = client.executeAndWrap(AutomationServiceClient.CN_TYC_SERVICE_ID, cnRequest, AutomationResponse.class);
    ObjectMapper mapper = new ObjectMapper();
    String json = mapper.writeValueAsString(rawResponse);
    TypeReference<AutomationResponse<CNResponse>> ref = new TypeReference<AutomationResponse<CNResponse>>() {
    };
    AutomationResponse<CNResponse> tycResponse = mapper.readValue(json, ref);

    if (tycResponse != null && tycResponse.isSuccess()) {
      map.addAttribute("result", tycResponse.getRecord());
    }
    return map;
  }

  // CREATCMR 3176 company Proof
  public static boolean isDnbOverrideAttachmentProvided(String reqId) {
    EntityManager entityManager = JpaManager.getEntityManager();
    String sql = ExternalizedQuery.getSql("QUERY.CHECK_DNB_MATCH_ATTACHMENT");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("ID", reqId);

    return query.exists();
  }
}
