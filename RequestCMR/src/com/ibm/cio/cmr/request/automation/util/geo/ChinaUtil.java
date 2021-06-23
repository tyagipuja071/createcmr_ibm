package com.ibm.cio.cmr.request.automation.util.geo;

import java.util.Arrays;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.automation.AutomationEngineData;
import com.ibm.cio.cmr.request.automation.RequestData;
import com.ibm.cio.cmr.request.automation.out.AutomationResult;
import com.ibm.cio.cmr.request.automation.out.OverrideOutput;
import com.ibm.cio.cmr.request.automation.out.ValidationOutput;
import com.ibm.cio.cmr.request.automation.util.AutomationUtil;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.Data;

public class ChinaUtil extends AutomationUtil {

  private static final Logger LOG = Logger.getLogger(ChinaUtil.class);
  public static final String SCENARIO_LOCAL_NRML = "NRML";
  public static final String SCENARIO_LOCAL_EMBSA = "EMBSA";
  public static final String SCENARIO_CROSS_CROSS = "CROSS";
  public static final String SCENARIO_LOCAL_AQSTN = "AQSTN";
  public static final String SCENARIO_LOCAL_BLUMX = "BLUMX";
  public static final String SCENARIO_LOCAL_MRKT = "MRKT";
  public static final String SCENARIO_LOCAL_BUSPR = "BUSPR";
  public static final String SCENARIO_LOCAL_INTER = "INTER";
  public static final String SCENARIO_LOCAL_PRIV = "PRIV";

  @Override
  public boolean performScenarioValidation(EntityManager entityManager, RequestData requestData, AutomationEngineData engineData,
      AutomationResult<ValidationOutput> result, StringBuilder details, ValidationOutput output) {

    // ScenarioExceptionsUtil scenarioExceptions = (ScenarioExceptionsUtil)
    // engineData.get("SCENARIO_EXCEPTIONS");
    Data data = requestData.getData();
    String scenario = data.getCustSubGrp();
    Admin admin = requestData.getAdmin();
    if ("U".equals(admin.getReqType())) {

    }
    Addr soldTo = requestData.getAddress("ZS01");
    String custNm1 = soldTo.getCustNm1();
    String custNm2 = StringUtils.isNotBlank(soldTo.getCustNm2()) ? " " + soldTo.getCustNm2() : "";
    String customerName = custNm1 + custNm2;

    switch (scenario) {

    case SCENARIO_LOCAL_NRML:
      if (!("00000".equals(data.getSearchTerm()) || "04182".equals(data.getSearchTerm()))) {
        LOG.debug("Cluster allowed: Cluster=" + data.getSearchTerm() + " Scenario=" + data.getCustSubGrp());
        result.setOnError(false);
        details.append("Cluster allowed:Cluster=" + data.getSearchTerm() + " Scenario=" + data.getCustSubGrp() + " for the request.\n");
      } else {
        details.append("Cluster=" + data.getSearchTerm() + " should not be allowed for Scenario=" + data.getCustSubGrp() + " for the request.\n");
        engineData.addRejectionComment("OTH", "Cluster=" + data.getSearchTerm() + " should not be allowed for this scenario", "", "");
        result.setOnError(true);
      }
      break;
    case SCENARIO_LOCAL_EMBSA:
      if (!("00000".equals(data.getSearchTerm()) || "04182".equals(data.getSearchTerm()))) {
        LOG.debug("Cluster allowed: Cluster=" + data.getSearchTerm() + " Scenario=" + data.getCustSubGrp());
        result.setOnError(false);
        details.append("Cluster allowed:Cluster=" + data.getSearchTerm() + " Scenario=" + data.getCustSubGrp() + " for the request.\n");
      } else {
        details.append("Cluster=" + data.getSearchTerm() + " should not be allowed for Scenario=" + data.getCustSubGrp() + " for the request.\n");
        engineData.addRejectionComment("OTH", "Cluster=" + data.getSearchTerm() + " should not be allowed for this scenario", "", "");
        result.setOnError(true);
      }
      break;
    case SCENARIO_CROSS_CROSS:
      if (!("00000".equals(data.getSearchTerm()) || "04182".equals(data.getSearchTerm()))) {
        LOG.debug("Cluster allowed: Cluster=" + data.getSearchTerm() + " Scenario=" + data.getCustSubGrp());
        result.setOnError(false);
        details.append("Cluster allowed:Cluster=" + data.getSearchTerm() + " Scenario=" + data.getCustSubGrp() + " for the request.\n");
      } else {
        details.append("Cluster=" + data.getSearchTerm() + " should not be allowed for Scenario=" + data.getCustSubGrp() + " for the request.\n");
        engineData.addRejectionComment("OTH", "Cluster=" + data.getSearchTerm() + " should not be allowed for this scenario", "", "");
        result.setOnError(true);
      }
      break;
    case SCENARIO_LOCAL_AQSTN:
      if (!("00000".equals(data.getSearchTerm()) || "04182".equals(data.getSearchTerm()))) {
        LOG.debug("Cluster allowed: Cluster=" + data.getSearchTerm() + " Scenario=" + data.getCustSubGrp());
        result.setOnError(false);
        details.append("Cluster allowed:Cluster=" + data.getSearchTerm() + " Scenario=" + data.getCustSubGrp() + " for the request.\n");
      } else {
        details.append("Cluster=" + data.getSearchTerm() + " should not be allowed for Scenario=" + data.getCustSubGrp() + " for the request.\n");
        engineData.addRejectionComment("OTH", "Cluster=" + data.getSearchTerm() + " should not be allowed for this scenario", "", "");
        result.setOnError(true);
      }
      break;
    case SCENARIO_LOCAL_BLUMX:
      engineData.addPositiveCheckStatus(AutomationEngineData.SKIP_COVERAGE);
      break;
    case SCENARIO_LOCAL_MRKT:
      engineData.addPositiveCheckStatus(AutomationEngineData.SKIP_COVERAGE);
      break;
    case SCENARIO_LOCAL_BUSPR:
      if ("04182".equals(data.getSearchTerm())) {
        LOG.debug("Cluster allowed: Cluster=" + data.getSearchTerm() + " Scenario=" + data.getCustSubGrp());
        result.setOnError(false);
        details.append("Cluster allowed:Cluster=" + data.getSearchTerm() + " Scenario=" + data.getCustSubGrp() + " for the request.\n");
      } else {
        details
            .append("Cluster=" + data.getSearchTerm() + " should be default (04182)  for Scenario=" + data.getCustSubGrp() + " for the request.\n");
        engineData.addRejectionComment("OTH", "Cluster=" + data.getSearchTerm() + " should default (04182)  for this scenario", "", "");
        result.setOnError(true);
      }

      if ("7230".equals(data.getIsicCd())) {
        LOG.debug("ISIC allowed: ISIC=" + data.getIsicCd() + " Scenario=" + data.getCustSubGrp());
        result.setOnError(false);
        details.append("ISIC ISIC=" + data.getIsicCd() + " Scenario=" + data.getCustSubGrp() + " for the request.\n");
      } else {
        details.append("ISIC=" + data.getIsicCd() + " should be default (7230)  for Scenario=" + data.getCustSubGrp() + " for the request.\n");
        engineData.addRejectionComment("OTH", "ISIC=" + data.getIsicCd() + " should default (7230)  for this scenario", "", "");
        result.setOnError(true);
      }
      engineData.addPositiveCheckStatus(AutomationEngineData.SKIP_GBG);
      engineData.addPositiveCheckStatus(AutomationEngineData.SKIP_COVERAGE);
      engineData.addPositiveCheckStatus(AutomationEngineData.SKIP_RETRIEVE_VALUES);
      doBusinessPartnerChecks(engineData, data.getPpsceid(), details);
      break;
    case SCENARIO_LOCAL_INTER:

      if (StringUtils.isNotBlank(customerName) && customerName.indexOf("IBM China") >= 0) {
        LOG.debug("English name=" + customerName + " for Scenario=" + data.getCustSubGrp());
        result.setOnError(false);
        details.append("English name=" + customerName + " for Scenario=" + data.getCustSubGrp() + " for the request.\n");
      } else {
        details.append("English name should include 'IBM China' for Scenario=" + data.getCustSubGrp() + " for the request.\n");
        engineData.addRejectionComment("OTH", "English name should include 'IBM China' for this scenario", "", "");
        result.setOnError(true);
      }
      engineData.addPositiveCheckStatus(AutomationEngineData.SKIP_GBG);
      engineData.addPositiveCheckStatus(AutomationEngineData.SKIP_COVERAGE);
      engineData.addPositiveCheckStatus(AutomationEngineData.SKIP_RETRIEVE_VALUES);
      break;
    case SCENARIO_LOCAL_PRIV:
      if ("00000".equals(data.getSearchTerm())) {
        LOG.debug("Cluster allowed: Cluster=" + data.getSearchTerm() + " Scenario=" + data.getCustSubGrp());
        result.setOnError(false);
        details.append("Cluster allowed:Cluster=" + data.getSearchTerm() + " Scenario=" + data.getCustSubGrp() + " for the request.\n");
      } else {
        details
            .append("Cluster=" + data.getSearchTerm() + " should be default (00000)  for Scenario=" + data.getCustSubGrp() + " for the request.\n");
        engineData.addRejectionComment("OTH", "Cluster=" + data.getSearchTerm() + " should default (00000)  for this scenario", "", "");
        result.setOnError(true);
      }
      if ("9500".equals(data.getIsicCd())) {
        LOG.debug("ISIC allowed: ISIC=" + data.getIsicCd() + " Scenario=" + data.getCustSubGrp());
        result.setOnError(false);
        details.append("ISIC ISIC=" + data.getIsicCd() + " Scenario=" + data.getCustSubGrp() + " for the request.\n");
      } else {
        details.append("ISIC=" + data.getIsicCd() + " should be default (9500)  for Scenario=" + data.getCustSubGrp() + " for the request.\n");
        engineData.addRejectionComment("OTH", "ISIC=" + data.getIsicCd() + " should default (9500)  for this scenario", "", "");
        result.setOnError(true);
      }
      engineData.addPositiveCheckStatus(AutomationEngineData.SKIP_GBG);
      engineData.addPositiveCheckStatus(AutomationEngineData.SKIP_COVERAGE);
      engineData.addPositiveCheckStatus(AutomationEngineData.SKIP_RETRIEVE_VALUES);
      break;
    }

    return true;
  }

  @Override
  public AutomationResult<OverrideOutput> doCountryFieldComputations(EntityManager entityManager, AutomationResult<OverrideOutput> results,
      StringBuilder details, OverrideOutput overrides, RequestData requestData, AutomationEngineData engineData) throws Exception {
    Data data = requestData.getData();
    Admin admin = requestData.getAdmin();
    if ("C".equals(admin.getReqType())) {
      String scenario = data.getCustSubGrp();
      LOG.info("Starting Field Computations for Request ID " + data.getId().getReqId());
      if (StringUtils.isNotBlank(scenario) && (SCENARIO_LOCAL_BLUMX.equals(scenario) || SCENARIO_LOCAL_MRKT.equals(scenario)
          || SCENARIO_LOCAL_BUSPR.equals(scenario) || SCENARIO_LOCAL_INTER.equals(scenario) || SCENARIO_LOCAL_PRIV.equals(scenario))) {
        details.append("Skip processing of element.\n");
        results.setResults("Skipped");
        results.setDetails("Skip processing of element.");
        results.setOnError(false);
      } else {
        details.append("Successful Execution.\n");
        results.setResults("Computed");
      }

    } else {
      details.append("No specific fields to compute.\n");
      results.setResults("Skipped");
    }
    results.setDetails(details.toString());

    return results;
  }

  @Override
  public List<String> getSkipChecksRequestTypesforCMDE() {
    return Arrays.asList("C", "U", "M", "D", "R");
  }
}
