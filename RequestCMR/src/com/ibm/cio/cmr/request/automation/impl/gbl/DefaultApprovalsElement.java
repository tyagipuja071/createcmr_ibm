/**
 * 
 */
package com.ibm.cio.cmr.request.automation.impl.gbl;

import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.automation.AutomationElementRegistry;
import com.ibm.cio.cmr.request.automation.AutomationEngineData;
import com.ibm.cio.cmr.request.automation.RequestData;
import com.ibm.cio.cmr.request.automation.impl.ApprovalsElement;
import com.ibm.cio.cmr.request.automation.out.AutomationResult;
import com.ibm.cio.cmr.request.automation.out.EmptyOutput;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.model.requestentry.RequestEntryModel;
import com.ibm.cio.cmr.request.service.approval.ApprovalService;
import com.ibm.cio.cmr.request.user.AppUser;
import com.ibm.cio.cmr.request.util.BluePagesHelper;

/**
 * {@link ApprovalsElement} implementing the default approvals workflow
 * 
 * @author JeffZAMORA
 * 
 */
public class DefaultApprovalsElement extends ApprovalsElement {

  private static final Logger LOG = Logger.getLogger(DefaultApprovalsElement.class);

  public DefaultApprovalsElement(String requestTypes, String actionOnError, boolean overrideData, boolean stopOnError) {
    super(requestTypes, actionOnError, overrideData, stopOnError);

  }

  @Override
  public AutomationResult<EmptyOutput> executeElement(EntityManager entityManager, RequestData requestData, AutomationEngineData engineData)
      throws Exception {
    long reqId = requestData.getAdmin().getId().getReqId();
    Admin admin = requestData.getAdmin();
    RequestEntryModel model = requestData.createModelFromRequest();

    AppUser requester = new AppUser();
    requester.setIntranetId(admin.getRequesterId());
    requester.setBluePagesName(admin.getRequesterNm());
    String cnum = BluePagesHelper.getCNUMByIntranetAddr(admin.getRequesterId());
    requester.setUserCnum(cnum);

    AutomationResult<EmptyOutput> result = buildResult(reqId);
    result.setProcessOutput(new EmptyOutput());

    LOG.info("Checking and generating required approvals for Request " + reqId);

    ApprovalService service = new ApprovalService();
    String approvalsResult = service.processDefaultApproval(entityManager, reqId, admin.getReqType(), requester, model);
    LOG.trace("Approvals result: " + approvalsResult);
    if (StringUtils.isBlank(approvalsResult) || "NONE".equalsIgnoreCase(approvalsResult)) {
      result.setResults("None");
      result.setDetails("No approvals are required.");
    } else {
      result.setOnError(true);
      result.setResults("Generated");
      result.setDetails("Required approvals have been generated.");
    }

    return result;
  }

  @Override
  public String getProcessCode() {
    return AutomationElementRegistry.GBL_APPROVALS;
  }

  @Override
  public String getProcessDesc() {
    return "Required Approvals";
  }

}
