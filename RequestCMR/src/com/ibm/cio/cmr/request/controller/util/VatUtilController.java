/**
 * 
 */
package com.ibm.cio.cmr.request.controller.util;

import java.util.ArrayList;
import java.util.List;

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

import com.ibm.cio.cmr.request.automation.RequestData;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.util.JpaManager;
import com.ibm.cio.cmr.request.util.dnb.DnBUtil;
import com.ibm.cio.cmr.request.util.geo.impl.CNHandler;
import com.ibm.cmr.services.client.AutomationServiceClient;
import com.ibm.cmr.services.client.CmrServicesFactory;
import com.ibm.cmr.services.client.ServiceClient.Method;
import com.ibm.cmr.services.client.ValidatorClient;
import com.ibm.cmr.services.client.automation.AutomationResponse;
import com.ibm.cmr.services.client.automation.ap.anz.BNValidationRequest;
import com.ibm.cmr.services.client.automation.ap.anz.BNValidationResponse;
import com.ibm.cmr.services.client.automation.ap.anz.NMValidationRequest;
import com.ibm.cmr.services.client.automation.ap.nz.NZBNValidationRequest;
import com.ibm.cmr.services.client.automation.ap.nz.NZBNValidationResponse;
import com.ibm.cmr.services.client.automation.cn.CNRequest;
import com.ibm.cmr.services.client.automation.cn.CNResponse;
import com.ibm.cmr.services.client.automation.eu.VatLayerRequest;
import com.ibm.cmr.services.client.automation.eu.VatLayerResponse;
import com.ibm.cmr.services.client.automation.in.GstLayerRequest;
import com.ibm.cmr.services.client.automation.in.GstLayerResponse;
import com.ibm.cmr.services.client.matching.dnb.DnBMatchingResponse;
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

  @RequestMapping(value = "/vat")
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

  @RequestMapping(value = "/vat/vies")
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

  @RequestMapping(value = "/in/gst")
  public ModelMap validateGST(HttpServletRequest request, HttpServletResponse response) throws Exception {
    ModelMap map = new ModelMap();

    ValidationResult validation = null;
    String country = "";
    if (!StringUtils.isEmpty(request.getParameter("country"))) {
      country = request.getParameter("country");
    }
    String vat = request.getParameter("vat");
    if (StringUtils.isEmpty(vat)) {
      validation = ValidationResult.error("'Gst#' param is required.");
    }
    String custNm1 = request.getParameter("custNm1");
    if (StringUtils.isEmpty(custNm1)) {
      validation = ValidationResult.error("'name' param is required.");
    }
    String custNm2 = request.getParameter("custNm2");

    String addrTxt = request.getParameter("addrTxt");
    if (StringUtils.isEmpty(addrTxt)) {
      validation = ValidationResult.error("'addrTxt' param is required.");
    }
    String postal = request.getParameter("postal");
    if (StringUtils.isEmpty(postal)) {
      validation = ValidationResult.error("'postal' param is required.");
    }
    String city = request.getParameter("city");
    if (StringUtils.isEmpty(city)) {
      validation = ValidationResult.error("'city' param is required.");
    }
    String stateProv = request.getParameter("stateProv");
    if (StringUtils.isEmpty(stateProv)) {
      validation = ValidationResult.error("'stateProv' param is required.");
    }
    String landCntry = request.getParameter("landCntry");
    if (StringUtils.isEmpty(landCntry)) {
      validation = ValidationResult.error("'landCntry' param is required.");
    }

    if (validation == null || validation.isSuccess()) {

      LOG.debug("Validating GST# " + vat + " for India");

      String baseUrl = SystemConfiguration.getValue("CMR_SERVICES_URL");
      AutomationServiceClient autoClient = CmrServicesFactory.getInstance().createClient(baseUrl, AutomationServiceClient.class);
      autoClient.setReadTimeout(1000 * 60 * 5);
      autoClient.setRequestMethod(Method.Get);

      GstLayerRequest gstLayerRequest = new GstLayerRequest();
      gstLayerRequest.setGst(vat);
      gstLayerRequest.setAddrTxt(addrTxt);
      gstLayerRequest.setCustName1(custNm1);
      gstLayerRequest.setCustName2(custNm2);
      gstLayerRequest.setCity(city);
      gstLayerRequest.setPostal(postal);
      gstLayerRequest.setStateProv(stateProv);
      gstLayerRequest.setLandCntry(landCntry);

      LOG.debug("Connecting to the GST Layer Service at " + baseUrl);
      AutomationResponse<?> rawResponse = autoClient.executeAndWrap(AutomationServiceClient.IN_GST_SERVICE_ID, gstLayerRequest,
          AutomationResponse.class);
      ObjectMapper mapper = new ObjectMapper();
      String json = mapper.writeValueAsString(rawResponse);
      TypeReference<AutomationResponse<GstLayerResponse>> ref = new TypeReference<AutomationResponse<GstLayerResponse>>() {
      };
      AutomationResponse<GstLayerResponse> gstResponse = mapper.readValue(json, ref);
      if (gstResponse != null && gstResponse.isSuccess()) {
        if (gstResponse.getMessage().equals("GST provided is verified with the company details.")) {
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

  @RequestMapping(value = "/zip")
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

  @RequestMapping(value = "/au/abn")
  public ModelMap validateABN(HttpServletRequest request, HttpServletResponse response) throws Exception {
    ModelMap map = new ModelMap();

    ValidationResult validation = null;
    Boolean comp_proof_Abn = false;

    String formerAbn = request.getParameter("formerAbn");
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
        if (abnResponse.getRecord().isValid()) {
          if (StringUtils.isBlank(formerAbn)) {
            validation = ValidationResult.success();
          } else {
            LOG.debug("Validating former ABN " + abn + " for Australia");
            BNValidationRequest requestToAPIForFormer = new BNValidationRequest();
            requestToAPIForFormer.setBusinessNumber(formerAbn);
            System.out.println(requestToAPIForFormer + requestToAPIForFormer.getBusinessNumber());

            LOG.debug("Connecting to the ABNValidation service at " + SystemConfiguration.getValue("BATCH_SERVICES_URL"));
            AutomationResponse<?> rawResponseFormerAbn = autoClient.executeAndWrap(AutomationServiceClient.AU_ABN_VALIDATION_SERVICE_ID,
                requestToAPIForFormer, AutomationResponse.class);
            String json2 = mapper.writeValueAsString(rawResponseFormerAbn);
            AutomationResponse<BNValidationResponse> formerAbnResponse = mapper.readValue(json2, ref);
            if (formerAbnResponse != null && formerAbnResponse.isSuccess()) {
              if (formerAbnResponse.getRecord().isValid() && (formerAbnResponse.getRecord().getStatus().equalsIgnoreCase("Cancelled"))) {
                validation = ValidationResult.success();
              } else if (formerAbnResponse.getRecord().isValid() && (!formerAbnResponse.getRecord().getStatus().equals(null)
                  && !formerAbnResponse.getRecord().getStatus().equalsIgnoreCase("Cancelled")) && !comp_proof_Abn) {
                validation = ValidationResult.error(
                    "ABN provided on Matches API But Former ABN isn't cancelled. Please provide Supporting documentation(Company Proof) as attachment");
              } else {
                if (!comp_proof_Abn)
                  validation = ValidationResult.error(
                      "ABN provided on Matches API But Former ABN mismatches with API.Please provide Supporting documentation(Company Proof) as attachment");
                else
                  validation = ValidationResult.success();
              }
            } else {
              validation = ValidationResult.error("ABN Validation Failed.Failed to connect to ABN Service. Checking ABN and Company Name with D&B.");
            }
          }
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

  @RequestMapping(value = "/au/custNm")
  public ModelMap validateCustNmFromVat(HttpServletRequest request, HttpServletResponse response) throws Exception {
    ModelMap map = new ModelMap();
    String regex = "\\s+$";
    String abn = request.getParameter("abn");
    String reqId = request.getParameter("reqId");
    String formerCustNm = request.getParameter("formerCustNm").replaceAll(regex, "");
    String custNm = request.getParameter("custNm").replaceAll(regex, "");
    Boolean success = false;
    Boolean custNmMatch = false;
    Boolean formerCustNmMatch = false;
    if (map.isEmpty() && !StringUtils.isBlank(abn) && !StringUtils.isBlank(reqId)) {

      LOG.debug("Validating Customer Name  " + custNm + " and Former Customer Name " + formerCustNm + " for Australia");

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
        map.put("success", true);
      } else if (abnResponse != null && abnResponse.isSuccess()) {
        if (abnResponse.getRecord().isValid()) {

          String responseCustNm = StringUtils.isBlank(abnResponse.getRecord().getCompanyName()) ? ""
              : abnResponse.getRecord().getCompanyName().replaceAll(regex, "");
          String responseTradingNm = StringUtils.isBlank(abnResponse.getRecord().getTradingName()) ? ""
              : abnResponse.getRecord().getTradingName().replaceAll(regex, "");
          String responseOthTradingNm = StringUtils.isBlank(abnResponse.getRecord().getOtherTradingName()) ? ""
              : abnResponse.getRecord().getOtherTradingName().replaceAll(regex, "");
          String responseBusinessNm = StringUtils.isBlank(abnResponse.getRecord().getBusinessName()) ? ""
              : abnResponse.getRecord().getBusinessName().replaceAll(regex, "");
          List<String> historicalNameList = new ArrayList<String>();
          historicalNameList = abnResponse.getRecord().getHistoricalNameList();
          for (String historicalNm : historicalNameList) {
            historicalNm = historicalNm.replaceAll(regex, "");
            if (abnResponse.getRecord().isValid() && custNm.equalsIgnoreCase(responseCustNm) && formerCustNm.equalsIgnoreCase(historicalNm)) {
              success = true;
              custNmMatch = true;
              formerCustNmMatch = true;
            }
          }
          if (!(success && custNmMatch && formerCustNmMatch)) {
            if (abnResponse.getRecord().isValid() && (custNm.equalsIgnoreCase(responseCustNm)) && ((formerCustNm.equalsIgnoreCase(responseTradingNm))
                || (formerCustNm.equalsIgnoreCase(responseOthTradingNm)) || (formerCustNm.equalsIgnoreCase(responseBusinessNm)))) {
              success = true;
              custNmMatch = true;
              formerCustNmMatch = true;
            } else if (abnResponse.getRecord().isValid() && (custNm.equalsIgnoreCase(responseCustNm))
                && !((formerCustNm.equalsIgnoreCase(responseTradingNm)) || (formerCustNm.equalsIgnoreCase(responseOthTradingNm))
                    || (formerCustNm.equalsIgnoreCase(responseBusinessNm)))) {
              success = true;
              custNmMatch = true;
              formerCustNmMatch = false;
            } else if (abnResponse.getRecord().isValid() && !(custNm.equalsIgnoreCase(responseCustNm))
                && ((formerCustNm.equalsIgnoreCase(responseTradingNm)) || (formerCustNm.equalsIgnoreCase(responseOthTradingNm))
                    || (formerCustNm.equalsIgnoreCase(responseBusinessNm)))) {
              success = true;
              custNmMatch = false;
              formerCustNmMatch = true;
            } else {
              success = true;
              custNmMatch = false;
              formerCustNmMatch = false;
            }
          }
        }
      } else {
        success = false;
        custNmMatch = false;
        formerCustNmMatch = false;
        String message = "ABN Validation Failed.Failed to connect to ABN Service. Checking ABN and Company Name with D&B.";
        if (abnResponse != null) {
          message = abnResponse.getMessage();
        }
        map.put("message", message);
      }
    }
    map.put("success", success);
    map.put("custNmMatch", custNmMatch);
    map.put("formerCustNmMatch", formerCustNmMatch);
    return map;
  }

  @RequestMapping(value = "/au/custNmFromAPI")
  public ModelMap validateCustNmFromAPI(HttpServletRequest request, HttpServletResponse response) throws Exception {
    ModelMap map = new ModelMap();
    String regex = "\\s+$";
    String reqId = request.getParameter("reqId");
    String formerCustNm = request.getParameter("formerCustNm").replaceAll(regex, "");
    String custNm = request.getParameter("custNm").replaceAll(regex, "");
    Boolean success = false;
    Boolean custNmMatch = false;
    Boolean formerCustNmMatch = false;
    if (map.isEmpty() && !StringUtils.isBlank(reqId) && !StringUtils.isBlank(custNm)) {

      LOG.debug("Validating Customer Name  " + custNm + " and Former Customer Name " + formerCustNm + " for Australia");

      String baseUrl = SystemConfiguration.getValue("CMR_SERVICES_URL");
      AutomationServiceClient autoClient = CmrServicesFactory.getInstance().createClient(baseUrl, AutomationServiceClient.class);
      autoClient.setReadTimeout(1000 * 60 * 5);
      autoClient.setRequestMethod(Method.Get);

      NMValidationRequest requestToAPI = new NMValidationRequest();
      requestToAPI.setCustNm(custNm);
      System.out.println(requestToAPI + requestToAPI.getCustNm());

      LOG.debug("Connecting to the ABNValidation service at " + SystemConfiguration.getValue("BATCH_SERVICES_URL"));
      AutomationResponse<?> rawResponse = autoClient.executeAndWrap(AutomationServiceClient.AU_NM_VALIDATION_SERVICE_ID, requestToAPI,
          AutomationResponse.class);
      ObjectMapper mapper = new ObjectMapper();
      String json = mapper.writeValueAsString(rawResponse);

      TypeReference<AutomationResponse<BNValidationResponse>> ref = new TypeReference<AutomationResponse<BNValidationResponse>>() {
      };
      AutomationResponse<BNValidationResponse> abnResponse = mapper.readValue(json, ref);
      if (abnResponse != null && abnResponse.isSuccess()) {
        if (abnResponse.getRecord().isValid()) {

          String responseCustNm = StringUtils.isBlank(abnResponse.getRecord().getCompanyName()) ? ""
              : abnResponse.getRecord().getCompanyName().replaceAll(regex, "");
          String responseTradingNm = StringUtils.isBlank(abnResponse.getRecord().getTradingName()) ? ""
              : abnResponse.getRecord().getTradingName().replaceAll(regex, "");
          String responseOthTradingNm = StringUtils.isBlank(abnResponse.getRecord().getOtherTradingName()) ? ""
              : abnResponse.getRecord().getOtherTradingName().replaceAll(regex, "");
          String responseBusinessNm = StringUtils.isBlank(abnResponse.getRecord().getBusinessName()) ? ""
              : abnResponse.getRecord().getBusinessName().replaceAll(regex, "");
          if (abnResponse.getRecord().isValid() && (custNm.equalsIgnoreCase(responseCustNm)) && ((formerCustNm.equalsIgnoreCase(responseTradingNm))
              || (formerCustNm.equalsIgnoreCase(responseOthTradingNm)) || (formerCustNm.equalsIgnoreCase(responseBusinessNm)))) {
            success = true;
            custNmMatch = true;
            formerCustNmMatch = true;
          } else if (abnResponse.getRecord().isValid() && (custNm.equalsIgnoreCase(responseCustNm))
              && !((formerCustNm.equalsIgnoreCase(responseTradingNm)) || (formerCustNm.equalsIgnoreCase(responseOthTradingNm))
                  || (formerCustNm.equalsIgnoreCase(responseBusinessNm)))) {
            success = true;
            custNmMatch = true;
            formerCustNmMatch = false;
          } else if (abnResponse.getRecord().isValid() && !(custNm.equalsIgnoreCase(responseCustNm))
              && ((formerCustNm.equalsIgnoreCase(responseTradingNm)) || (formerCustNm.equalsIgnoreCase(responseOthTradingNm))
                  || (formerCustNm.equalsIgnoreCase(responseBusinessNm)))) {
            success = true;
            custNmMatch = false;
            formerCustNmMatch = true;
          } else {
            success = true;
            custNmMatch = false;
            formerCustNmMatch = false;
          }
        }
      } else {
        success = false;
        custNmMatch = false;
        formerCustNmMatch = false;
        String message = "ABN Validation Failed.Failed to connect to ABN Service. Checking ABN and Company Name with D&B.";
        if (abnResponse != null) {
          message = abnResponse.getMessage();
        }
        map.put("message", message);
      }
    }
    map.put("success", success);
    map.put("custNmMatch", custNmMatch);
    map.put("formerCustNmMatch", formerCustNmMatch);
    return map;
  }

  /**
   * 
   * @param request
   * @param response
   * @return
   * @throws Exception
   * @deprecated - TYC discontinued; Use
   *             {@link #checkCnAddrViaDNB(HttpServletRequest, HttpServletResponse)}
   */
  @Deprecated
  @RequestMapping(value = "/cn/tyc")
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

  /**
   * Connects to D&B and tries to do a local language retrieval using D&B
   * 
   * @param request
   * @param response
   * @return
   * @throws Exception
   */
  @RequestMapping(value = "/cn/dnb")
  public ModelMap checkCnAddrViaDNB(HttpServletRequest request, HttpServletResponse response) throws Exception {
    ModelMap map = new ModelMap();
    map.put("result", null);

    // ValidationResult validation = null;
    String busnType = request.getParameter("busnType");
    String cnName = request.getParameter("cnName");
    String cnAddress = request.getParameter("cnAddress");
    String cnCity = request.getParameter("cnCity");

    // dummyfy street and city when not supplied
    if (StringUtils.isBlank(cnAddress)) {
      cnAddress = "dummy";
    }
    if (StringUtils.isBlank(cnCity)) {
      cnCity = "dummy";
    }

    String trackedDuns = null;
    if (!StringUtils.isBlank(busnType)) {
      LOG.debug("Finding D&B records with Org ID " + busnType);
      List<DnBMatchingResponse> orgIdMatches = DnBUtil.findByOrgId(busnType, "CN");
      // use only the top match here
      if (!orgIdMatches.isEmpty()) {
        DnBMatchingResponse dnb = orgIdMatches.get(0);
        LOG.debug("DUNS " + dnb.getDunsNo() + " found for Org ID " + busnType);
        trackedDuns = dnb.getDunsNo();
      }
    }

    List<DnBMatchingResponse> nameMatches = DnBUtil.findByAddress("CN", cnName, cnAddress, cnCity);
    if (!nameMatches.isEmpty()) {
      // only add if no orgId specified, or orgId specified and matches duns
      CNHandler handler = new CNHandler();
      for (DnBMatchingResponse match : nameMatches) {
        LOG.debug("Got " + match.getDunsNo() + " / " + match.getDnbName() + " / " + match.getDnbStreetLine1() + " / " + match.getDnbCity());

        if (trackedDuns != null && trackedDuns.equals(match.getDunsNo())) {
          LOG.debug(" - matched with DUNS " + match.getDunsNo() + ". Comparing name and address");
          // repurpose CNResponse here
          CNResponse cnResponse = new CNResponse();
          cnResponse.setCreditCode(!StringUtils.isBlank(busnType) ? busnType : DnBUtil.getVAT("CN", match.getOrgIdDetails()));
          cnResponse.setName(match.getDnbName());
          cnResponse.setRegLocation(match.getDnbStreetLine1());
          cnResponse.setCity(match.getDnbCity());
          map.put("result", cnResponse);
          break;
        } else if (trackedDuns == null) {
          // only do name matching, if needed check only street
          // note: account for dummy address and city
          String dbcsInputName = handler.convert2DBCS(cnName);
          String dbcsDnBName = handler.convert2DBCS(match.getDnbName());
          if (dbcsInputName.equals(dbcsDnBName)) {
            // repurpose CNResponse here
            CNResponse cnResponse = new CNResponse();
            cnResponse.setCreditCode(!StringUtils.isBlank(busnType) ? busnType : DnBUtil.getVAT("CN", match.getOrgIdDetails()));
            cnResponse.setName(match.getDnbName());
            cnResponse.setRegLocation(match.getDnbStreetLine1());
            cnResponse.setCity(match.getDnbCity());
            map.put("result", cnResponse);
            break;
          }
        }
      }
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

  @RequestMapping(value = "/nz/nzbnFromAPI")
  public ModelMap validateNZBNFromAPI(HttpServletRequest request, HttpServletResponse response) throws Exception {
    ModelMap map = new ModelMap();
    boolean apiSuccess = false;
    boolean apiCustNmMatch = false;
    boolean apiAddressMatch = false;
    String errorMsg = "";

    String regex = "\\s+$";
    String reqIdStr = request.getParameter("reqId").replaceAll(regex, "");
    String businessNumber = request.getParameter("businessNumber").replaceAll(regex, "");
    String custNm = request.getParameter("custNm").replaceAll(regex, "");

    EntityManager entityManager = null;
    try {
      if (reqIdStr != null) {
        long reqId = Long.parseLong(reqIdStr);
        entityManager = JpaManager.getEntityManager();
        RequestData requestData = new RequestData(entityManager, reqId);

        if (requestData != null && (!StringUtils.isBlank(businessNumber) || !StringUtils.isBlank(custNm))) {
          LOG.debug("Validating Customer Name  " + custNm + " and Business Number " + businessNumber + " for New Zealand");
          Addr addr = requestData.getAddress("ZS01");

          String baseUrl = SystemConfiguration.getValue("CMR_SERVICES_URL");
          AutomationServiceClient autoClient = CmrServicesFactory.getInstance().createClient(baseUrl, AutomationServiceClient.class);
          autoClient.setReadTimeout(1000 * 60 * 5);
          autoClient.setRequestMethod(Method.Get);

          NZBNValidationRequest requestToAPI = new NZBNValidationRequest();
          requestToAPI.setBusinessNumber(businessNumber);
          requestToAPI.setName(custNm);
          LOG.debug("requestToAPI: businessNumber = " + requestToAPI.getBusinessNumber() + ", custNm = " + custNm);

          LOG.debug("Connecting to the NZBNValidation service at " + SystemConfiguration.getValue("CMR_SERVICES_URL"));
          AutomationResponse<?> rawResponse = autoClient.executeAndWrap(AutomationServiceClient.NZ_BN_VALIDATION_SERVICE_ID, requestToAPI,
              AutomationResponse.class);
          ObjectMapper mapper = new ObjectMapper();
          String json = mapper.writeValueAsString(rawResponse);

          TypeReference<AutomationResponse<NZBNValidationResponse>> ref = new TypeReference<AutomationResponse<NZBNValidationResponse>>() {
          };
          AutomationResponse<NZBNValidationResponse> nzbnResponse = mapper.readValue(json, ref);
          if (nzbnResponse != null && nzbnResponse.isSuccess()) {
            apiSuccess = true;
            NZBNValidationResponse nzbnResRec = nzbnResponse.getRecord();
            if (nzbnResRec != null) {
              String responseCustNm = StringUtils.isBlank(nzbnResRec.getName()) ? "" : nzbnResRec.getName().replaceAll(regex, "");
              if (custNm.equalsIgnoreCase(responseCustNm)) {
                apiCustNmMatch = true;

                // Address matching - BEGIN
                String regexForAddr = "\\s+|$";
                String addressAll = addr.getCustNm1() + (addr.getCustNm2() == null ? "" : addr.getCustNm2())
                    + (addr.getAddrTxt() != null ? addr.getAddrTxt() : "") + (addr.getAddrTxt2() == null ? "" : addr.getAddrTxt2())
                    + (addr.getStateProv() == null ? "" : addr.getStateProv()) + (addr.getCity1() == null ? "" : addr.getCity1())
                    + (addr.getPostCd() == null ? "" : addr.getPostCd());
                LOG.debug("****** addressAll: " + addressAll);
                addressAll = addressAll.toUpperCase();
                LOG.debug("Address used for NZ API matching: " + addressAll + " VS " + nzbnResRec.getAddress());
                if (StringUtils.isNotEmpty(nzbnResRec.getAddress())
                    && addressAll.replaceAll(regexForAddr, "").contains(nzbnResRec.getAddress().replaceAll(regexForAddr, "").toUpperCase())
                    && StringUtils.isNotEmpty(nzbnResRec.getCity())
                    && addressAll.replaceAll(regexForAddr, "").contains(nzbnResRec.getCity().replaceAll(regexForAddr, "").toUpperCase())
                    && StringUtils.isNotEmpty(nzbnResRec.getPostal())
                    && addressAll.replaceAll(regexForAddr, "").contains(nzbnResRec.getPostal().replaceAll(regexForAddr, "").toUpperCase())) {
                  apiAddressMatch = true;
                }

                // CREATCMR-8430: checking if the address matches with service
                // type address from API
                LOG.debug("REGISTERED Address matched ?  " + apiAddressMatch);

                String serviceAddr = nzbnResRec.getServiceAddressDetail();
                if (!apiAddressMatch && StringUtils.isNotEmpty(serviceAddr)) {
                  LOG.debug("****** addressOfRequest: " + addressAll);
                  LOG.debug("****** serviceAddr: " + serviceAddr);
                  String[] serviceAddrArr = serviceAddr.split("\\^");
                  boolean serviceFlag = false;
                  for (String partAddr : serviceAddrArr) {
                    serviceFlag = (addressAll.replaceAll(regexForAddr, "").contains(partAddr.replaceAll(regexForAddr, "").toUpperCase()));
                    if (!serviceFlag) {
                      break;
                    }
                  }
                  apiAddressMatch = serviceFlag;
                  LOG.debug("SERVICE Address matched ?  " + apiAddressMatch);
                }
              }
            }

            if (!apiAddressMatch) {
              errorMsg = "NZBN Validation Failed - Address does not match with NZ API.";
            }
            if (!apiCustNmMatch) {
              errorMsg = "NZBN Validation Failed - Customer Name does not match with NZ API.";
            }
          } else {
            String respMsg = StringUtils.isNotBlank(nzbnResponse.getMessage()) ? nzbnResponse.getMessage() : "";
            errorMsg = "NZBN Validation Failed - ";
            if (respMsg.replaceAll(regex, "").contains("No Response on the Request".replaceAll(regex, ""))) {
              errorMsg += "Invalid NZBN.";
            } else {
              errorMsg += respMsg;
            }
          }
        }
      } else {
        errorMsg = "Invalid request Id";
      }
    } catch (Exception e) {
      LOG.debug("Error occurred while checking NZ API Matches." + e);
      errorMsg = "An error occurred while matching with NZ API.";
    } finally {
      if (entityManager != null) {
        entityManager.close();
      }
    }

    map.put("success", apiSuccess);
    map.put("custNmMatch", apiCustNmMatch);
    map.put("addressMatch", apiAddressMatch);
    map.put("message", errorMsg);
    return map;
  }
}
