/**
 * 
 */
package com.ibm.cio.cmr.request.controller.util;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cmr.services.client.CmrServicesFactory;
import com.ibm.cmr.services.client.ValidatorClient;
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

}
