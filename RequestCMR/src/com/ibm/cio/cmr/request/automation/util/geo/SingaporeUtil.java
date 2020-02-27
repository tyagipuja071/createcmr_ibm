package com.ibm.cio.cmr.request.automation.util.geo;

import java.io.IOException;

import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.automation.AutomationElementRegistry;
import com.ibm.cio.cmr.request.automation.AutomationEngineData;
import com.ibm.cio.cmr.request.automation.RequestData;
import com.ibm.cio.cmr.request.automation.impl.gbl.GOEDeterminationElement;
import com.ibm.cio.cmr.request.automation.out.AutomationResult;
import com.ibm.cio.cmr.request.automation.out.OverrideOutput;
import com.ibm.cio.cmr.request.automation.util.AutomationUtil;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.json.java.JSONArray;
import com.ibm.json.java.JSONObject;

public class SingaporeUtil extends AutomationUtil {

  private static final Logger LOG = Logger.getLogger(SingaporeUtil.class);

  @Override
  public AutomationResult<OverrideOutput> doCountryFieldComputations(EntityManager entityManager, AutomationResult<OverrideOutput> results,
      StringBuilder details, OverrideOutput overrides, RequestData requestData, AutomationEngineData engineData) throws Exception {
    long reqId = requestData.getAdmin().getId().getReqId();
    LOG.debug("Executing doCountryFieldComputations() for reqId=" + reqId);
    Data data = requestData.getData();
    boolean ifDefaultCluster = false;
    String cluster = data.getApCustClusterId();
    String duns = data.getDunsNo();
    StringBuilder eleResults = new StringBuilder();
    String defaultCluster = getDefaultCluster(entityManager, requestData, engineData);

    if (StringUtils.isNotBlank(defaultCluster)) {
      ifDefaultCluster = cluster.equals(defaultCluster);
    }

    if (ifDefaultCluster) {
      details.append("Cluster should not be the default for the scenario.\n");
      engineData.addRejectionComment("Cluster should not be the default for the scenario.");
      eleResults.append("Default cluster found.\n");
      results.setOnError(true);
    } else {
      details.append("Default cluster not used.\n");
    }

    String validRes = checkIfClusterSalesmanIsValid(entityManager, requestData);
    LOG.debug("IfClusterSalesmanIsValid" + validRes);

    if (validRes != null && validRes.equals("INVALID")) {
      details.append("The combination of Salesman No. and Cluster is not valid.\n");
      engineData.addRejectionComment("The combination of Salesman No. and Cluster is not valid.");
      eleResults.append("Invalid Salesman No.\n");
      results.setOnError(true);
    } else if (validRes != null && validRes.equals("VALID")) {
      details.append("The combination of Salesman No. and Cluster is valid.\n");
    } else if (validRes != null && validRes.equals("NO_RESULTS")) {
      details.append("The combination of Salesman No. and Cluster doesn't exist for country.\n");
    }

    if (StringUtils.isNotEmpty(duns)) {
      GOEDeterminationElement goeDetermination = new GOEDeterminationElement(requestData.getAdmin().getReqType(), null, false, false);
      JSONArray matches = new JSONArray();
      try {
        matches = goeDetermination.findGOE(duns, null, null, null);
        if (!matches.isEmpty()) {
          for (Object dunsMatch : matches) {
            JSONObject match = (JSONObject) dunsMatch;
            String overallIndicator = (String) match.get("OVERALL_GOE_CD");
            LOG.debug("overallIndicator" + overallIndicator);
            if (StringUtils.isNotEmpty(overallIndicator) && (overallIndicator.equals("Y") || overallIndicator.equals("S"))) {
              details.append("Government= Y" + "\n");
              overrides.addOverride(AutomationElementRegistry.GBL_FIELD_COMPUTE, "DATA", "GOVERNMENT", data.getGovType(), "Y");
            } else {
              eleResults.append("NOT a GOE." + "\n");
              results.setOnError(true);
              details.append("GOE Determination = NOT a GOE. \n");
            }
          }
        } else {
          eleResults.append("Cannot determine GOE" + "\n");
          details.append("GOE status cannot be determined via DUNS No." + "\n");
          results.setOnError(true);
        }
      } catch (IOException e) {
        LOG.debug(e.getMessage());
        e.printStackTrace();
        eleResults.append("Cannot determine GOE" + "\n");
        details.append("Error occured while determining GOE status." + "\n");
        results.setOnError(true);
      }
    } else {
      eleResults.append("Duns not found" + "\n");
      details.append("GOE status cannot be determined via DUNS No. as duns No doesn't exists on request." + "\n");
      results.setOnError(true);
    }

    if (!results.isOnError()) {
      eleResults.append("Successful Exceution");
      details.append("Field Computation completed successfully." + "\n");
    }

    results.setDetails(details.toString());
    results.setResults(eleResults.toString());
    results.setProcessOutput(overrides);
    LOG.debug(details.toString());
    return results;
  }

  @Override
  public boolean performScenarioValidation(EntityManager entityManager, RequestData requestData, AutomationEngineData engineData) {

    return true;
  }

}
