package com.ibm.cio.cmr.request.automation.impl.ca;

import javax.persistence.EntityManager;

import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.automation.ActionOnError;
import com.ibm.cio.cmr.request.automation.AutomationElementRegistry;
import com.ibm.cio.cmr.request.automation.AutomationEngineData;
import com.ibm.cio.cmr.request.automation.RequestData;
import com.ibm.cio.cmr.request.automation.impl.ValidatingElement;
import com.ibm.cio.cmr.request.automation.out.AutomationResult;
import com.ibm.cio.cmr.request.automation.out.ValidationOutput;
import com.ibm.cio.cmr.request.automation.util.AutomationUtil;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;

public class CACMDERequesterCheck extends ValidatingElement {

  private static final Logger log = Logger.getLogger(CACMDERequesterCheck.class);

  public CACMDERequesterCheck(String requestTypes, String actionOnError, boolean overrideData, boolean stopOnError) {
    super(requestTypes, actionOnError, overrideData, stopOnError);
  }

  @Override
  public AutomationResult<ValidationOutput> executeElement(EntityManager entityManager, RequestData requestData, AutomationEngineData engineData)
      throws Exception {
    Admin admin = requestData.getAdmin();
    Data data = requestData.getData();

    AutomationResult<ValidationOutput> output = buildResult(admin.getId().getReqId());
    ValidationOutput validation = new ValidationOutput();
    AutomationUtil countryUtil = AutomationUtil.getNewCountryUtil(data.getCmrIssuingCntry());
    log.debug("Automation Util for " + data.getCmrIssuingCntry() + " = " + (countryUtil != null ? countryUtil.getClass().getSimpleName() : "none"));
    if (countryUtil != null && countryUtil.getSkipChecksRequestTypesforCMDE().contains(admin.getReqType())) {
      String sqlKey = ExternalizedQuery.getSql("AUTO.CHECK_CMDE");
      PreparedQuery query = new PreparedQuery(entityManager, sqlKey);
      query.setParameter("REQUESTER_ID", admin.getRequesterId());
      query.setParameter("CNTRY", data.getCmrIssuingCntry());
      query.setForReadOnly(true);
      if (query.exists()) {
        // skip checks if requester is from CMDE team
        // countryUtil.skipAllChecks(engineData);
        // admin.setScenarioVerifiedIndc("Y");
        // engineData.addPositiveCheckStatus("SKIP_APPROVALS");
        // engineData.addPositiveCheckStatus("SKIP_DPL_CHECK");
        // engineData.addPositiveCheckStatus("SKIP_UPDATE_SWITCH");
        log.debug("Requester is from CMDE team.");
        output.setDetails("Requester is from CMDE team.\n");
        validation.setMessage("Requester is from CMDE team");
        validation.setSuccess(true);
      } else if (!"C".equals(admin.getReqType()) && !"U".equals(admin.getReqType())) {
        String message = "Requester not from CMDE team.";
        if (ActionOnError.Proceed.equals(getActionOnError())) {
          message += "Further processing validation will be required before proceeding.";
        }
        output.setDetails(message);
        engineData.addRejectionComment("OTH", message, "", "");
        output.setOnError(true);
        validation.setMessage("Validation Failed");
        validation.setSuccess(false);
      } else {
        output.setDetails("Requester is not from CMDE team.");
        output.setOnError(false);
        validation.setMessage("Automation checks required.");
        validation.setSuccess(true);
      }
    } else {
      output.setDetails("Element execution skipped for current request type based on country configurations.");
      validation.setMessage("Skipped");
      validation.setSuccess(true);
    }
    output.setResults(validation.getMessage());
    output.setProcessOutput(validation);
    return output;
  }

  @Override
  public String getProcessCode() {
    // TODO Auto-generated method stub
    return AutomationElementRegistry.CA_CMDE_CHECK;
  }

  @Override
  public String getProcessDesc() {
    // TODO Auto-generated method stub
    return "Canada - CMDE Requester Check";
  }

}
