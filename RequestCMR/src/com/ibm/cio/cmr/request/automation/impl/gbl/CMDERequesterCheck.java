package com.ibm.cio.cmr.request.automation.impl.gbl;

import javax.persistence.EntityManager;

import org.apache.log4j.Logger;

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

public class CMDERequesterCheck extends ValidatingElement {

  private static final Logger log = Logger.getLogger(CMDERequesterCheck.class);

  public CMDERequesterCheck(String requestTypes, String actionOnError, boolean overrideData, boolean stopOnError) {
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

    String sqlKey = ExternalizedQuery.getSql("AUTO.CHECK_CMDE");
    PreparedQuery query = new PreparedQuery(entityManager, sqlKey);
    query.setParameter("REQUESTER_ID", admin.getRequesterId());
    query.setParameter("CMR_ISSUING_CNTRY", data.getCmrIssuingCntry());
    query.setForReadOnly(true);
    if (query.exists()) {
      // skip checks if requester is from CMDE team
      countryUtil.skipAllChecks(engineData);
      log.debug("Requester is from CMDE team, skipping Automation checks.");
      output.setDetails("Requester is from CMDE team, skipping Automation checks.\n");
      validation.setMessage("Automation checks Skipped");
      validation.setSuccess(true);
    } else {
      output.setDetails("Requester is not from CMDE team");
      validation.setMessage("Approval Required");
      validation.setSuccess(false);
    }
    output.setResults(validation.getMessage());
    output.setProcessOutput(validation);
    return output;
  }

  @Override
  public String getProcessCode() {
    // TODO Auto-generated method stub
    return AutomationElementRegistry.GBL_CMDE_CHECK;
  }

  @Override
  public String getProcessDesc() {
    // TODO Auto-generated method stub
    return "Global - CMDE Requester Check";
  }

}
