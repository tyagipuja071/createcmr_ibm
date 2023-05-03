package com.ibm.cio.cmr.request.automation.util.geo;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import com.ibm.cio.cmr.request.automation.AutomationElementRegistry;
import com.ibm.cio.cmr.request.automation.AutomationEngineData;
import com.ibm.cio.cmr.request.automation.RequestData;
import com.ibm.cio.cmr.request.automation.impl.gbl.RetrieveIBMValuesElement;
import com.ibm.cio.cmr.request.automation.out.AutomationResult;
import com.ibm.cio.cmr.request.automation.out.OverrideOutput;
import com.ibm.cio.cmr.request.automation.out.ValidationOutput;
import com.ibm.cio.cmr.request.automation.util.AutomationUtil;
import com.ibm.cio.cmr.request.automation.util.BrazilFieldsContainer;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
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

  @Override
  public boolean fillCoverageAttributes(RetrieveIBMValuesElement retrieveElement, EntityManager entityManager,
      AutomationResult<OverrideOutput> results, StringBuilder details, OverrideOutput overrides, RequestData requestData,
      AutomationEngineData engineData, String covType, String covId, String covDesc, String gbgId) throws Exception {
    Data data = requestData.getData();
    String isu = "";
    String ctc = "";
    String mrc = "";
    String salesBoCd = "";
    LOG.debug("BR performing fillCoverageAttributes based on GBG");
    LOG.debug("GBG Id: " + gbgId);

    if (gbgId != null && !"BGNONE".equals(gbgId.trim())) {
      BrazilFieldsContainer sortl = getGbgSortlMapping(entityManager, requestData, gbgId.trim());
      String comment = StringUtils.isNotBlank(sortl.getComment()) ? sortl.getComment() : "";
      if (StringUtils.isNotBlank(sortl.getText()) && !comment.equals("Signature-Strategic")) {
        List<BrazilFieldsContainer> isuCtcResults = computeIsuCtcFromSbo(entityManager, data, sortl.getText());
        if (isuCtcResults != null && !isuCtcResults.isEmpty()) {
          for (BrazilFieldsContainer field : isuCtcResults) {
            if (field.getIsuCode() != null && field.getClientTier() != null && field.getMrcCode() != null) {
              isu = field.getIsuCode();
              ctc = field.getClientTier();
              mrc = field.getMrcCode();
              salesBoCd = field.getSalesBoCde();
              break;
            }
          }
          LOG.debug("Setting SORTL ISU CTC MRC based on GBG. (GBG Found)");
          details.append("Setting SORTL ISU CTC MRC based on GBG.");
          overrides.addOverride(AutomationElementRegistry.GBL_FIELD_COMPUTE, "DATA", "SALES_BO_CD", data.getSalesBusOffCd(), salesBoCd);
          overrides.addOverride(AutomationElementRegistry.GBL_FIELD_COMPUTE, "DATA", "ISU_CD", data.getIsuCd(), isu);
          overrides.addOverride(AutomationElementRegistry.GBL_FIELD_COMPUTE, "DATA", "CLIENT_TIER", data.getClientTier(), ctc);
          overrides.addOverride(AutomationElementRegistry.GBL_FIELD_COMPUTE, "DATA", "MRC_CD", data.getMrcCd(), mrc);
          LOG.debug("SBO : " + salesBoCd + " " + "ISU : " + isu + " " + "CTC : " + ctc + " " + "MRC : " + mrc);
        }
      } else {
        if (StringUtils.isNotBlank(sortl.getText()) && comment.equals("Signature-Strategic")) {
          LOG.debug("Setting SORTL ISU CTC MRC based on GBG. (Signature-Strategic)");
          details.append("Setting SORTL ISU CTC MRC based on GBG.");
          overrides.addOverride(AutomationElementRegistry.GBL_FIELD_COMPUTE, "DATA", "SALES_BO_CD", data.getSalesBusOffCd(), sortl.getText());
          overrides.addOverride(AutomationElementRegistry.GBL_FIELD_COMPUTE, "DATA", "MRC_CD", data.getMrcCd(), "A");
          overrides.addOverride(AutomationElementRegistry.GBL_FIELD_COMPUTE, "DATA", "CLIENT_TIER", data.getClientTier(), "");
          LOG.debug("SBO : " + sortl.getText() + " " + "ISU : " + data.getIsuCd() + " " + "CTC : " + "" + " " + "MRC : " + "A");
        }
      }
    } else {
      LOG.debug("GBG is BGNONE...");
      LOG.debug("SBO : " + requestData.getData().getSalesBusOffCd());
      if (covId != null) {
        String coverageId = covType.trim() + covId.trim();
        LOG.debug("Coverage Id: " + coverageId);
        BrazilFieldsContainer sortl = getGbgSortlMapping(entityManager, requestData, coverageId);
        if (StringUtils.isNotBlank(sortl.getText())) {
          List<BrazilFieldsContainer> isuCtcResults = computeIsuCtcFromSbo(entityManager, data, requestData.getData().getSalesBusOffCd());
          if (isuCtcResults != null && !isuCtcResults.isEmpty()) {
            for (BrazilFieldsContainer field : isuCtcResults) {
              if (field.getIsuCode() != null && field.getClientTier() != null && field.getMrcCode() != null) {
                isu = field.getIsuCode();
                ctc = field.getClientTier();
                mrc = field.getMrcCode();
                salesBoCd = field.getSalesBoCde();
                break;
              }
            }
            details.append("Setting SORTL ISU CTC MRC based on GBG.");
            overrides.addOverride(AutomationElementRegistry.GBL_FIELD_COMPUTE, "DATA", "ISU_CD", data.getIsuCd(), isu);
            overrides.addOverride(AutomationElementRegistry.GBL_FIELD_COMPUTE, "DATA", "CLIENT_TIER", data.getClientTier(), ctc);
            overrides.addOverride(AutomationElementRegistry.GBL_FIELD_COMPUTE, "DATA", "MRC_CD", data.getMrcCd(), mrc);
            LOG.debug("SBO : " + requestData.getData().getSalesBusOffCd() + " " + "ISU : " + isu + " " + "CTC : " + ctc + " " + "MRC : " + mrc);
          }
        }
      }
    }
    return true;

  }

  private List<BrazilFieldsContainer> computeIsuCtcFromSbo(EntityManager entityManager, Data data, String sortl) {
    List<BrazilFieldsContainer> salesBoFields = new ArrayList<>();
    String sql = ExternalizedQuery.getSql("QUERY.GET.ISU.CTC.BY_SBO");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("ISSUING_CNTRY", data.getCmrIssuingCntry());
    query.setParameter("SALES_BO_CD", sortl);
    query.setForReadOnly(true);
    List<Object[]> results = query.getResults();
    if (results != null && !results.isEmpty()) {
      for (Object[] result : results) {
        BrazilFieldsContainer fieldValues = new BrazilFieldsContainer();
        fieldValues.setSalesBoCde((String) result[0]);
        fieldValues.setIsuCode((String) result[1]);
        fieldValues.setClientTier((String) result[2]);
        fieldValues.setMrcCode((String) result[3]);
        fieldValues.setRepTeamCode((String) result[4]);
        salesBoFields.add(fieldValues);
      }
    }
    return salesBoFields;
  }

  private BrazilFieldsContainer getGbgSortlMapping(EntityManager entityManager, RequestData requestData, String code) {
    BrazilFieldsContainer lov = new BrazilFieldsContainer();
    String sql = ExternalizedQuery.getSql("GET.SBO.CD.BY.GBG");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("CMR_ISSUING_CNTRY", requestData.getData().getCmrIssuingCntry());
    query.setParameter("FIELD_ID", "##Sortl");
    query.setParameter("CD", code);
    List<Object[]> codes = query.getResults(1);
    if (codes != null && codes.size() > 0) {
      lov.setCode((String) codes.get(0)[0]);
      lov.setText((String) codes.get(0)[1]);
      lov.setComment((String) codes.get(0)[2]);
    }
    return lov;
  }

}
