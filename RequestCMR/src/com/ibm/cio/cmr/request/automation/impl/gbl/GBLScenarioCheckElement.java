package com.ibm.cio.cmr.request.automation.impl.gbl;

import java.util.List;

import javax.persistence.EntityManager;

import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.automation.AutomationElementRegistry;
import com.ibm.cio.cmr.request.automation.AutomationEngineData;
import com.ibm.cio.cmr.request.automation.RequestData;
import com.ibm.cio.cmr.request.automation.impl.ValidatingElement;
import com.ibm.cio.cmr.request.automation.out.AutomationResult;
import com.ibm.cio.cmr.request.automation.out.ValidationOutput;
import com.ibm.cio.cmr.request.automation.util.AutomationUtil;
import com.ibm.cio.cmr.request.automation.util.ScenarioExceptionsUtil;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.entity.listeners.ChangeLogListener;
import com.ibm.cio.cmr.request.util.dnb.DnBUtil;
import com.ibm.cmr.services.client.matching.MatchingResponse;
import com.ibm.cmr.services.client.matching.dnb.DnBMatchingResponse;

/**
 * 
 * @author RoopakChugh
 *
 */
public class GBLScenarioCheckElement extends ValidatingElement {

  private static final Logger log = Logger.getLogger(GBLScenarioCheckElement.class);

  public GBLScenarioCheckElement(String requestTypes, String actionOnError, boolean overrideData, boolean stopOnError) {
    super(requestTypes, actionOnError, overrideData, stopOnError);
    // TODO Auto-generated constructor stub
  }

  @Override
  public AutomationResult<ValidationOutput> executeElement(EntityManager entityManager, RequestData requestData, AutomationEngineData engineData)
      throws Exception {

    long reqId = requestData.getAdmin().getId().getReqId();

    AutomationResult<ValidationOutput> result = buildResult(reqId);
    ValidationOutput output = new ValidationOutput();
    StringBuilder details = new StringBuilder();
    log.debug("Entering global performScenarioCheck()");
    ChangeLogListener.setManager(entityManager);
    Data data = requestData.getData();
    Admin admin = requestData.getAdmin();
    String cmrIssuingCntry = data.getCmrIssuingCntry();

    if ("Y".equals(admin.getScenarioVerifiedIndc())) {
      log.debug("Skip processing of element");
      result.setDetails("Skip processing of element");
      result.setOnError(false);
      output.setSuccess(true);
      output.setMessage("Skip scenario check.");
    } else {
      ScenarioExceptionsUtil scenarioExceptions = getScenarioExceptions(entityManager, requestData, engineData);
      log.debug("Skip check is " + scenarioExceptions.isSkipChecks());
      // method that will perform country specific check
      AutomationUtil countryUtil = AutomationUtil.getNewCountryUtil(cmrIssuingCntry);
      log.debug("Automation Util for " + data.getCmrIssuingCntry() + " = " + (countryUtil != null ? countryUtil.getClass().getSimpleName() : "none"));
      boolean isPrivateSubScenario = scenarioExceptions != null ? scenarioExceptions.isSkipFindGbgForPrivates() : false;
      boolean checkDunsMatchOnPrivates = checkDunsMatchOnPrivates(requestData, engineData, "ZS01", isPrivateSubScenario);
      if (checkDunsMatchOnPrivates) {
        output.setSuccess(false);
        output.setMessage("DUNS closely matching name and address in 'Private Household CMR' leads to automatic rejection.");
        result.setDetails("DUNS closely matching name and address in 'Private Household CMR' leads to automatic rejection.");
        result.setOnError(true);
        result.setResults("DUNS closely matching name and address in 'Private Household CMR' leads to automatic rejection.");
        engineData.addRejectionComment("OTH", "DUNS closely matching name and address in 'Private Household CMR' leads to automatic rejection.", "",
            "");
        log.debug("DUNS closely matching name and address in 'Private Household CMR' leads to automatic rejection.");
        return result;
      }
      if (countryUtil != null) {
        boolean countryCheck = false;
        countryCheck = countryUtil.performScenarioValidation(entityManager, requestData, engineData, result, details, output);
        if (countryCheck) {
          output.setSuccess(true);
          output.setMessage("Scenario Valid");
          details.insert(0, "Scenario Checks Performed Successfully.\n" + (details.length() > 0 ? "Details:\n" : ""));
          result.setDetails(details.toString());
          result.setOnError(false);
          log.debug("Scenario chosen is correct");
        } else {
          output.setSuccess(false);
          output.setMessage("Scenario Invalid");
          details.insert(0, "Scenario Checks were not successful.\n" + (details.length() > 0 ? "Details:\n" : ""));
          result.setDetails(details.toString());
          result.setOnError(true);
          log.debug("Scenario chosen is not correct");
        }
      } else {
        engineData.addNegativeCheckStatus("SCENARIO_CHECK_FAIL", "Country Scenario check logic was not found and needs confirmation.");
        output.setSuccess(false);
        output.setMessage("No CntryCheck found");
        result.setDetails("Country Scenario check logic was not found\nScenario needs to be manually verified by processor");
        log.debug("Scenario needs to be manually verified by processor");
      }

    }

    result.setResults(output.getMessage());
    result.setProcessOutput(output);
    return result;
  }

  public boolean checkDunsMatchOnPrivates(RequestData requestData, AutomationEngineData engineData, String addrType, boolean hasCloselyMatch)
      throws Exception {
    boolean hasValidMatches = false;
    MatchingResponse<DnBMatchingResponse> response = DnBUtil.getMatches(requestData, engineData, "ZS01");
    if (response != null && response.getMatched()) {
      hasValidMatches = DnBUtil.hasValidMatches(response);
      List<DnBMatchingResponse> dnbMatches = response.getMatches();

      if (hasValidMatches) {
        for (DnBMatchingResponse dnbRecord : dnbMatches) {

          if (hasCloselyMatch) {
            Addr soldTo = requestData.getAddress("ZS01");
            Admin admin = requestData.getAdmin();
            Data data = requestData.getData();
            // call a method to check customer name and address
            boolean closelyMatches = DnBUtil.closelyMatchesDnb(data.getCmrIssuingCntry(), soldTo, admin, dnbRecord);
            if (closelyMatches) {
              return true;
            }
          }
        }
      }
    }
    return false;
  }

  @Override
  public String getProcessCode() {
    return AutomationElementRegistry.GBL_SCENARIO_CHECK;
  }

  @Override
  public String getProcessDesc() {
    return "Global-Scenario Check";
  }

}
