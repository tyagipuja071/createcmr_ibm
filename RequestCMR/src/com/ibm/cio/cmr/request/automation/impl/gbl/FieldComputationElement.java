package com.ibm.cio.cmr.request.automation.impl.gbl;

import javax.persistence.EntityManager;

import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.automation.ActionOnError;
import com.ibm.cio.cmr.request.automation.AutomationElementRegistry;
import com.ibm.cio.cmr.request.automation.AutomationEngineData;
import com.ibm.cio.cmr.request.automation.RequestData;
import com.ibm.cio.cmr.request.automation.impl.OverridingElement;
import com.ibm.cio.cmr.request.automation.out.AutomationResult;
import com.ibm.cio.cmr.request.automation.out.OverrideOutput;
import com.ibm.cio.cmr.request.automation.util.AutomationUtil;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.Data;

public class FieldComputationElement extends OverridingElement {

  private static final Logger log = Logger.getLogger(FieldComputationElement.class);

  public FieldComputationElement(String requestTypes, String actionOnError, boolean overrideData, boolean stopOnError) {
    super(requestTypes, actionOnError, overrideData, stopOnError);
    // TODO Auto-generated constructor stub
  }

  @Override
  public AutomationResult<OverrideOutput> executeElement(EntityManager entityManager, RequestData requestData, AutomationEngineData engineData)
      throws Exception {
    long reqId = requestData.getAdmin().getId().getReqId();
    AutomationResult<OverrideOutput> results = buildResult(reqId);
    String issuingCntry = requestData.getData().getCmrIssuingCntry();
    StringBuilder details = new StringBuilder();
    OverrideOutput overrides = new OverrideOutput(false);
    log.debug("Entering FieldComputationElement()");
    AutomationResult<OverrideOutput> result = null;
    AutomationUtil countryUtil = AutomationUtil.getNewCountryUtil(issuingCntry);
    log.debug("Automation Util for " + issuingCntry + " = " + (countryUtil != null ? countryUtil.getClass().getSimpleName() : "none"));
    if (engineData.hasPositiveCheckStatus(AutomationEngineData.SKIP_FIELD_COMPUTATION)) {
      results.setDetails("Coverage calculation skipped due to previous element execution results.");
      results.setResults("Skipped");
      results.setProcessOutput(results.getProcessOutput());
      log.debug("Skipping field compuation..");
      return results;
    }
    if (countryUtil != null) {
      result = countryUtil.doCountryFieldComputations(entityManager, results, details, overrides, requestData, engineData);
    }
    if (result == null) {
      details
          .append(" Field computations logics not defined for the country " + issuingCntry + ". Sending back to processor for final calculations.");
      engineData.addRejectionComment("OTH", "Field computations logics not defined for the country and needs to manually be completed.", "", "");
      results.setResults("Error On Field Calculation");
      results.setOnError(true);
    } else if (result != null && !result.isOnError()) {
      if ("Skipped".equals(result.getResults())) {
        results = result;
        results.setResults("Skip processing of element");
        results.setOnError(false);
        results.setProcessOutput(result.getProcessOutput());
      } else {
        results = result;
        results.setResults("Successful Execution");
        results.setOnError(false);
        results.setProcessOutput(result.getProcessOutput());
      }

    } else {
      log.debug("Error On Field Calculation");
      // engineData.addRejectionComment(result.getResults());
      results = result;

      // CREATCMR-8430: don't set PRJ status for NZBN Check.
      Data data = requestData.getData();
      if ("796".equals(data.getCmrIssuingCntry()) && result.getResults() != null && "Request check failed".equals(result.getResults())) {
        log.debug("For NZ -- to move to next step");
        super.setStopOnError(false);
        super.setActionOnError(ActionOnError.fromCode(""));
      } else if (result.getResults() != null && "Requester check fail".equals(result.getResults())) {
        super.setStopOnError(true);
        super.setActionOnError(ActionOnError.fromCode("R"));
      }
      results.setResults(result.getResults());
      results.setProcessOutput(result.getProcessOutput());
    }

    results.setDetails(details.toString());

    log.debug(details.toString());

    // CREATCMR-6342
    Data data = requestData.getData();
    Admin admin = requestData.getAdmin();

    if ("897".equals(data.getCmrIssuingCntry())) {
      if ("C".equals(admin.getReqType()) || "U".equals(admin.getReqType())) {
        if ("TT2".equals(data.getCsoSite()) && "P".equals(data.getBpAcctTyp())) {
          try {
            data.setBpAcctTyp("");
            data.setBpName("");
            entityManager.merge(data);
            entityManager.flush();
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
      }
    }
    // CREATCMR-6342

    return results;
  }

  @Override
  public String getProcessCode() {
    return AutomationElementRegistry.GBL_FIELD_COMPUTE;
  }

  @Override
  public String getProcessDesc() {
    return "Global Field Computation";
  }
}
