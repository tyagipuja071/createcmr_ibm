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
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.entity.ScenarioExceptions;
import com.ibm.cmr.services.client.matching.gbg.GBGResponse;

public class SingaporeUtil extends AutomationUtil {

  private static final Logger LOG = Logger.getLogger(SingaporeUtil.class);

  private static final List<String> ALLOW_DEFAULT_SCENARIOS = Arrays.asList("PRIV", "XPRIV", "BLUMX", "MKTPC", "XBLUM", "XMKTP");

  @Override
  public AutomationResult<OverrideOutput> doCountryFieldComputations(EntityManager entityManager, AutomationResult<OverrideOutput> results,
      StringBuilder details, OverrideOutput overrides, RequestData requestData, AutomationEngineData engineData) throws Exception {
    long reqId = requestData.getAdmin().getId().getReqId();
    LOG.debug("Executing doCountryFieldComputations() for reqId=" + reqId);
    Data data = requestData.getData();
    boolean ifDefaultCluster = false;
    String cluster = data.getApCustClusterId();
    String govType = data.getGovType();
    String duns = data.getDunsNo();
    StringBuilder eleResults = new StringBuilder();
    String defaultCluster = getDefaultCluster(entityManager, requestData, engineData);
    String[] isicScenarioList = { "NRML", "ASLOM", "BUSPR", "SPOFF", "CROSS", "AQSTN", "XAQST", "SOFT" };
    boolean isIsicInvalid = false;

    if (StringUtils.isNotBlank(defaultCluster)) {
      ifDefaultCluster = cluster.equals(defaultCluster);
    }

    if (ifDefaultCluster) {
      if ("9500".equals(data.getIsicCd()) || ALLOW_DEFAULT_SCENARIOS.contains(data.getCustSubGrp())) {
        LOG.debug("Default Cluster used but allowed: ISIC=" + data.getIsicCd() + " Scenario=" + data.getCustSubGrp());
        results.setOnError(false);
        // eleResults.append("Default Cluster used.\n");
        details.append("Default Cluster used but allowed for the request (Private Person/BlueMix/Marketplace).\n");
      } else {
        details.append("Cluster should not be the default for the scenario.\n");
        engineData.addRejectionComment("OTH", "Cluster should not be the default for the scenario.", "", "");
        // eleResults.append("Default cluster found.\n");
        results.setOnError(true);
      }
    } else {
      details.append("Default cluster not used.\n");
    }

    String validRes = checkIfClusterSalesmanIsValid(entityManager, requestData);
    LOG.debug("IfClusterSalesmanIsValid" + validRes);

    if (validRes != null && validRes.equals("INVALID")) {
      details.append("The combination of Salesman No. and Cluster is not valid.\n");
      engineData.addRejectionComment("OTH", "The combination of Salesman No. and Cluster is not valid.", "", "");
      // eleResults.append("Invalid Salesman No.\n");
      results.setOnError(true);
    } else if (validRes != null && validRes.equals("VALID")) {
      details.append("The combination of Salesman No. and Cluster is valid.\n");
    } else if (validRes != null && validRes.equals("NO_RESULTS")) {
      details.append("The combination of Salesman No. and Cluster doesn't exist for country.\n");
    }

    /*
     * if (StringUtils.isNotEmpty(duns)) { GOEDeterminationElement
     * goeDetermination = new
     * GOEDeterminationElement(requestData.getAdmin().getReqType(), null, false,
     * false); JSONArray matches = new JSONArray(); try { matches =
     * goeDetermination.findGOE(duns, null, null, null); if (!matches.isEmpty())
     * { for (Object dunsMatch : matches) { JSONObject match = (JSONObject)
     * dunsMatch; String overallIndicator = (String)
     * match.get("OVERALL_GOE_CD"); LOG.debug("overallIndicator" +
     * overallIndicator); if (StringUtils.isNotEmpty(overallIndicator) &&
     * (overallIndicator.equals("Y") || overallIndicator.equals("S"))) {
     * details.append("Government= Y" + "\n");
     * overrides.addOverride(AutomationElementRegistry.GBL_FIELD_COMPUTE,
     * "DATA", "GOVERNMENT", data.getGovType(), "Y"); } else {
     * eleResults.append("NOT a GOE." + "\n"); results.setOnError(true);
     * details.append("GOE Determination = NOT a GOE. \n"); } } } else {
     * eleResults.append("Cannot determine GOE" + "\n");
     * details.append("GOE status cannot be determined via DUNS No." + "\n");
     * results.setOnError(true); } } catch (IOException e) {
     * LOG.debug(e.getMessage()); e.printStackTrace();
     * eleResults.append("Cannot determine GOE" + "\n");
     * details.append("Error occured while determining GOE status." + "\n");
     * results.setOnError(true); } } else { eleResults.append("Duns not found" +
     * "\n"); details.append(
     * "GOE status cannot be determined via DUNS No. as duns No doesn't exists on request."
     * + "\n"); results.setOnError(true); }
     */

    if (govType != null && govType.equals("Y")) {
      // eleResults.append("Government Organization" + "\n");
      details.append("Processor review is needed as customer is a Government Organization" + "\n");
      engineData.addNegativeCheckStatus("ISGOV", "Customer is a Government Organization");
    } else {
      // eleResults.append("Non Government Customer" + "\n");
      details.append("Customer is a non government organization" + "\n");
    }

    // CMR-2034 fix
    if ("34".equals(data.getIsuCd()) || "04".equals(data.getIsuCd())) {
      GBGResponse computedGbg = (GBGResponse) engineData.get(AutomationEngineData.GBG_MATCH);
      if (computedGbg == null) {
        engineData.setScenarioVerifiedIndc("N");
      }
    }

    isIsicInvalid = isISICValidForScenario(requestData, Arrays.asList(isicScenarioList));

    if (isIsicInvalid) {
      details.append("Invalid ISIC code, please choose another one based on industry.\n");
      // eleResults.append("Invalid ISIC code.\n");
      engineData.addRejectionComment("OTH", "Invalid ISIC code, please choose another one based on industry.", "", "");
      results.setOnError(true);
    } else {
      details.append("ISIC is valid" + "\n");
    }

    if (!results.isOnError()) {
      // eleResults.append("Successful Exceution");
      details.append("Field Computation completed successfully." + "\n");
    } else {
      eleResults.append("Error On Field Calculation.");
    }

    results.setDetails(details.toString());
    results.setResults(eleResults.toString());
    results.setProcessOutput(overrides);
    LOG.debug(details.toString());
    return results;
  }

  @Override
  public boolean performScenarioValidation(EntityManager entityManager, RequestData requestData, AutomationEngineData engineData,
      AutomationResult<ValidationOutput> result, StringBuilder details, ValidationOutput output) {
    /*
     * String[] scnarioList = { "XBLUM", "BLUMX", "MKTPC", "XMKTP", "DUMMY",
     * "XDUMM", "INTER", "XINT", "AQSTN", "XAQST", "SOFT" };
     * skipCompanyCheckForScenario(requestData, engineData,
     * Arrays.asList(scnarioList), false);
     */

    String[] scnarioList = { "ASLOM", "NRML" };

    allowDuplicatesForScenario(engineData, requestData, Arrays.asList(scnarioList));

    processSkipCompanyChecks(engineData, requestData, details);
    return true;
  }

  /**
   * CHecks if the record is a private customer / bluemix / marketplace
   * 
   * @param engineData
   * @param requestData
   * @param details
   * @return
   */
  private void processSkipCompanyChecks(AutomationEngineData engineData, RequestData requestData, StringBuilder details) {
    Data data = requestData.getData();

    boolean skipCompanyChecks = "9500".equals(data.getIsicCd()) || (data.getCustSubGrp() != null && data.getCustSubGrp().contains("PRIV"));
    if (skipCompanyChecks) {
      details.append("Private Person request - company checks will be skipped.\n");
      ScenarioExceptions exc = (ScenarioExceptions) engineData.get("SCENARIO_EXCEPTIONS");
      if (exc != null) {
        exc.setSkipVerificationIndc("Y");
        engineData.put("SCENARIO_EXCEPTIONS", exc);
      }
    }
  }
}
