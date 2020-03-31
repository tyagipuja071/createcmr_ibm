package com.ibm.cio.cmr.request.automation.util.geo;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.automation.AutomationEngineData;
import com.ibm.cio.cmr.request.automation.RequestData;
import com.ibm.cio.cmr.request.automation.out.AutomationResult;
import com.ibm.cio.cmr.request.automation.out.OverrideOutput;
import com.ibm.cio.cmr.request.automation.out.ValidationOutput;
import com.ibm.cio.cmr.request.automation.util.AutomationUtil;
import com.ibm.cio.cmr.request.automation.util.RequestChangeContainer;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cmr.services.client.CmrServicesFactory;
import com.ibm.cmr.services.client.QueryClient;
import com.ibm.cmr.services.client.query.QueryRequest;
import com.ibm.cmr.services.client.query.QueryResponse;

/**
 * 
 * @author RoopakChugh
 *
 */

public class USUtil extends AutomationUtil {

  private static final Logger LOG = Logger.getLogger(USUtil.class);
  private static final String COMMERCIAL = "1";
  private static final String POWER_OF_ATTORNEY = "6";
  private static final String FEDERAL = "4";
  private static final String INTERNAL = "5";
  private static final String LEASING = "3";
  private static final String BUSINESS_PARTNER = "7";
  private static final String STATE_LOCAL = "2";

  @Override
  public AutomationResult<OverrideOutput> doCountryFieldComputations(EntityManager entityManager, AutomationResult<OverrideOutput> results,
      StringBuilder details, OverrideOutput overrides, RequestData requestData, AutomationEngineData engineData) throws Exception {
    return null;
  }

  @Override
  public boolean performScenarioValidation(EntityManager entityManager, RequestData requestData, AutomationEngineData engineData,
      AutomationResult<ValidationOutput> result, StringBuilder details, ValidationOutput output) {
    // get request admin and data
    Admin admin = requestData.getAdmin();
    Data data = requestData.getData();
    String[] scnarioList = { "HOSPITALS", "SCHOOL PUBLIC", "SCHOOL CHARTER", "SCHOOL PRIV", "SCHOOL PAROCHL", "SCHOOL COLLEGE", "STATE", "SPEC DIST",
        "COUNTY", "CITY", "32C", "TPPS", "3CC", "SVR CONT" };
    String scenarioSubType = "";
    if ("C".equals(admin.getReqType()) && data != null) {
      scenarioSubType = StringUtils.isBlank(data.getCustSubGrp()) ? "" : data.getCustSubGrp();
    }

    if (Arrays.asList(scnarioList).contains(scenarioSubType)) {
      engineData.addNegativeCheckStatus("US_SCENARIO_CHK", "Automated checks cannot be performed for this scenario.");
    }
    return true;
  }

  @Override
  public boolean runUpdateChecksForData(EntityManager entityManager, AutomationEngineData engineData, RequestData requestData,
      RequestChangeContainer changes, ValidationOutput validation) throws Exception {
    // TODO Auto-generated method stub
    return super.runUpdateChecksForData(entityManager, engineData, requestData, changes, validation);
  }

  @Override
  public boolean runUpdateChecksForAddress(EntityManager entityManager, AutomationEngineData engineData, RequestData requestData,
      RequestChangeContainer changes, ValidationOutput validation) throws Exception {

    return true;
  }

  public HashMap<String, String> determineUSCMRDetails(EntityManager entityManager, RequestData requestData, AutomationEngineData engineData)
      throws Exception {
    // get request admin and data
    HashMap<String, String> mapUSCMR = new HashMap<>();
    Admin admin = requestData.getAdmin();
    String custTypCd = "NA";
    String entType = "";
    String leasingCo = "";
    String bpAccTyp = "";
    String cGem = "";
    String usRestricTo = "";
    String companyNo = "";
    String cmrNo = StringUtils.isBlank(admin.getModelCmrNo()) ? "" : admin.getModelCmrNo();
    String url = SystemConfiguration.getValue("BATCH_SERVICES_URL");
    String usSchema = SystemConfiguration.getValue("US_CMR_SCHEMA");
    String sql = ExternalizedQuery.getSql("AUTO.US.GET_SCENARIO_INFO", usSchema);
    sql = StringUtils.replace(sql, ":CMR_NO", "'" + cmrNo + "'");
    QueryRequest query = new QueryRequest();
    query.setSql(sql);
    query.setRows(1);
    query.addField("I_ENT_TYPE");
    query.addField("I_BP_ACCOUNT_TYPE");
    query.addField("C_LEASING_CO");
    query.addField("C_GEM");
    query.addField("C_COM_RESTRCT_CODE");
    query.addField("I_CO");
    QueryClient client = CmrServicesFactory.getInstance().createClient(url, QueryClient.class);
    QueryResponse response = client.executeAndWrap(QueryClient.USCMR_APP_ID, query, QueryResponse.class);
    if (!response.isSuccess()) {
      custTypCd = "NA";
    } else if (response.getRecords() == null || response.getRecords().size() == 0) {
      custTypCd = "NA";
    } else {
      Map<String, Object> record = response.getRecords().get(0);
      entType = (String) record.get("I_ENT_TYPE");
      leasingCo = (String) record.get("C_LEASING_CO");
      bpAccTyp = (String) record.get("I_BP_ACCOUNT_TYPE");
      cGem = (String) record.get("C_GEM");
      usRestricTo = (String) record.get("C_COM_RESTRCT_CODE");
      companyNo = String.valueOf(record.get("I_CO"));
      if ("P".equals(entType)) {
        custTypCd = "6";
      } else if ("F".equals(entType)) {
        custTypCd = "4";
      } else if ("I".equals(entType)) {
        custTypCd = "5";
      } else if ("C".equals(entType) || StringUtils.isNotBlank(leasingCo)) {
        custTypCd = "3";
      } else if (StringUtils.isNotBlank(bpAccTyp)) {
        custTypCd = "7";
      } else if ("2".equals(cGem) || "3".equals(cGem)) {
        custTypCd = "2";
      } else {
        custTypCd = "1";
      }
    }
    mapUSCMR.put("custTypCd", custTypCd);
    mapUSCMR.put("entType", entType);
    mapUSCMR.put("leasingCo", leasingCo);
    mapUSCMR.put("bpAccTyp", bpAccTyp);
    mapUSCMR.put("cGem", cGem);
    mapUSCMR.put("usRestricTo", usRestricTo);
    mapUSCMR.put("companyNo", companyNo);
    System.out.println(mapUSCMR);
    return mapUSCMR;
  }

}
