package com.ibm.cio.cmr.request.automation.util.geo;

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
import com.ibm.cmr.services.client.AutomationServiceClient;
import com.ibm.cmr.services.client.CmrServicesFactory;
import com.ibm.cmr.services.client.ServiceClient.Method;
import com.ibm.cmr.services.client.automation.AutomationResponse;
import com.ibm.cmr.services.client.automation.la.br.ConsultaCCCResponse;
import com.ibm.cmr.services.client.automation.la.br.MidasRequest;
import com.ibm.cmr.services.client.automation.la.br.SintegraResponse;

public class BrazilUtil extends AutomationUtil {

  private static final Logger LOG = Logger.getLogger(BrazilUtil.class);

  @Override
  public AutomationResult<OverrideOutput> doCountryFieldComputations(EntityManager entityManager, AutomationResult<OverrideOutput> results,
      StringBuilder details, OverrideOutput overrides, RequestData requestData, AutomationEngineData engineData) throws Exception {
    return null;
  }

  @Override
  public boolean performScenarioValidation(EntityManager entityManager, RequestData requestData, AutomationEngineData engineData,
      AutomationResult<ValidationOutput> result, StringBuilder details, ValidationOutput output) {
    return true;
  }

  /**
   * Calls the sintegra service
   * 
   * @param vat
   * @param state
   * @return
   * @throws Exception
   */
  public static AutomationResponse<SintegraResponse> querySintegra(String vat, String state) throws Exception {
    AutomationServiceClient client = CmrServicesFactory.getInstance().createClient(SystemConfiguration.getValue("BATCH_SERVICES_URL"),
        AutomationServiceClient.class);
    client.setReadTimeout(1000 * 60 * 5);
    client.setRequestMethod(Method.Get);

    // calling SINTEGRA
    LOG.debug("Calling Sintegra Service for VAT " + vat);
    MidasRequest requestSoldTo = new MidasRequest();
    requestSoldTo.setCnpj(vat);
    requestSoldTo.setUf(state);

    LOG.debug("Connecting to the Sintegra Service at " + SystemConfiguration.getValue("BATCH_SERVICES_URL"));
    AutomationResponse<?> rawResponseSoldTo = client.executeAndWrap(AutomationServiceClient.BR_SINTEGRA_SERVICE_ID, requestSoldTo,
        AutomationResponse.class);
    ObjectMapper mapperSoldTo = new ObjectMapper();
    String jsonSoldTo = mapperSoldTo.writeValueAsString(rawResponseSoldTo);
    LOG.trace("Sintegra Service Response for VAT " + vat + ": " + jsonSoldTo);

    TypeReference<AutomationResponse<SintegraResponse>> refSoldTo = new TypeReference<AutomationResponse<SintegraResponse>>() {
    };
    AutomationResponse<SintegraResponse> response = mapperSoldTo.readValue(jsonSoldTo, refSoldTo);

    return response;
  }

  /**
   * Calls the ConsultaCCC service
   * 
   * @param vat
   * @param state
   * @return
   * @throws Exception
   */
  public static AutomationResponse<ConsultaCCCResponse> querySintegraByConsulata(String vat, String state) throws Exception {
    AutomationServiceClient client = CmrServicesFactory.getInstance().createClient(SystemConfiguration.getValue("BATCH_SERVICES_URL"),
        AutomationServiceClient.class);
    client.setReadTimeout(1000 * 60 * 3);
    client.setRequestMethod(Method.Get);

    // calling SINTEGRA
    LOG.debug("Calling ConsultaCCC Service for VAT " + vat);
    MidasRequest requestSoldTo = new MidasRequest();
    requestSoldTo.setCnpj(vat);
    requestSoldTo.setUf(state);

    LOG.debug("Connecting to the ConsultaCCC Service at " + SystemConfiguration.getValue("BATCH_SERVICES_URL"));
    AutomationResponse<?> rawResponseSoldTo = client.executeAndWrap(AutomationServiceClient.BR_CONSULTA_SERVICE_ID, requestSoldTo,
        AutomationResponse.class);
    ObjectMapper mapperSoldTo = new ObjectMapper();
    String jsonSoldTo = mapperSoldTo.writeValueAsString(rawResponseSoldTo);
    LOG.trace("ConsultaCCC Service Response for VAT " + vat + ": " + jsonSoldTo);

    TypeReference<AutomationResponse<ConsultaCCCResponse>> refSoldTo = new TypeReference<AutomationResponse<ConsultaCCCResponse>>() {
    };
    AutomationResponse<ConsultaCCCResponse> response = mapperSoldTo.readValue(jsonSoldTo, refSoldTo);

    return response;
  }

  public static boolean hasScenarioCheck(String issuingCntry) {
    return true;
  }

}
