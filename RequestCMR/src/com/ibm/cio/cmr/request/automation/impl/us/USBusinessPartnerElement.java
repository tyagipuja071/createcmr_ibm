package com.ibm.cio.cmr.request.automation.impl.us;

import javax.persistence.EntityManager;

import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.automation.AutomationElement;
import com.ibm.cio.cmr.request.automation.AutomationElementRegistry;
import com.ibm.cio.cmr.request.automation.AutomationEngineData;
import com.ibm.cio.cmr.request.automation.RequestData;
import com.ibm.cio.cmr.request.automation.impl.OverridingElement;
import com.ibm.cio.cmr.request.automation.impl.ProcessWaitingElement;
import com.ibm.cio.cmr.request.automation.out.AutomationResult;
import com.ibm.cio.cmr.request.automation.out.OverrideOutput;
import com.ibm.cio.cmr.request.automation.util.AutomationUtil;
import com.ibm.cio.cmr.request.automation.util.geo.us.USBPHandler;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.model.requestentry.FindCMRRecordModel;
import com.ibm.cio.cmr.request.util.geo.impl.USHandler;

/**
 * {@link AutomationElement} handling the US - Business Partner End User
 * Scenario
 * 
 * @author JeffZAMORA
 *
 */
public class USBusinessPartnerElement extends OverridingElement implements ProcessWaitingElement {

  private static final Logger LOG = Logger.getLogger(USBusinessPartnerElement.class);
  private boolean waiting;

  public USBusinessPartnerElement(String requestTypes, String actionOnError, boolean overrideData, boolean stopOnError) {
    super(requestTypes, actionOnError, overrideData, stopOnError);
  }

  @Override
  public AutomationResult<OverrideOutput> executeElement(EntityManager entityManager, RequestData requestData, AutomationEngineData engineData)
      throws Exception {
    LOG.debug(">>>> Executing USBusinessPartnerElement <<<<");
    Admin admin = requestData.getAdmin();
    long reqId = admin.getId().getReqId();
    Data data = requestData.getData();
    AutomationResult<OverrideOutput> output = buildResult(reqId);
    Addr addr = requestData.getAddress("ZS01");

    // CMR - 3999
    boolean shouldSendToCMDE = AutomationUtil.checkCommentSection(entityManager, admin, data);
    if (shouldSendToCMDE) {
      String cmt = "The model CMR provided isn't consistent with the CMR type requested, please cancel this request and choose a compatible model CMR.";
      output.setDetails(cmt);
      output.setResults("Review Required.");
      engineData.addNegativeCheckStatus("INCOMP_MODEL_CMR", cmt);
      return output;
    }

    if ("U".equals(admin.getReqType())) {
      LOG.debug("Request Type update is not supported by USBusinessPartnerElement - Skipping ");
      output.setResults("Skipped");
      output.setDetails("Update types not supported.");
      return output;
    }

    USBPHandler bpHandler = USBPHandler.getBPHandler(entityManager, requestData, engineData);

    if (bpHandler == null) {
      LOG.debug("No BP Handler could be determined for the request");
      output.setResults("Skipped");
      output.setDetails("Request is either of a non-BP scenario or non-leasing scenario or is not currently supported by the element.");
      return output;
    }

    // validate first
    if (bpHandler.doInitialValidations(admin, data, addr, output, engineData)) {
      return output;
    }

    USHandler handler = new USHandler();
    OverrideOutput overrides = new OverrideOutput(false);
    StringBuilder details = new StringBuilder();
    FindCMRRecordModel ibmCmr = null;

    long childReqId = admin.getChildReqId();
    RequestData childRequest = null;
    boolean childCompleted = false;
    if (bpHandler.isEndUserSupported()) {
      if (childReqId > 0) {
        childRequest = new RequestData(entityManager, childReqId);
        childCompleted = bpHandler.checkIfChildRequestCompleted(entityManager, engineData, requestData, childRequest, details, output, handler);
        if (!childCompleted) {
          this.waiting = bpHandler.isWaiting();
          return output;
        }
      }
      ibmCmr = bpHandler.getIbmCmr(entityManager, handler, requestData, details, addr, engineData, childRequest, childCompleted);
    }

    boolean processed = bpHandler.processRequest(entityManager, requestData, engineData, output, details, childCompleted, childRequest, handler,
        ibmCmr, overrides);
    // check if request should wait
    this.waiting = bpHandler.isWaiting();
    if (processed && !this.waiting) {
      // copy from IBM Direct if found, and fill the rest of BO codes
      bpHandler.copyAndFillIBMData(entityManager, handler, requestData, engineData, details, overrides, childRequest, ibmCmr);

      bpHandler.doFinalValidations(engineData, requestData, details, overrides, ibmCmr, output);
      output.setResults("Successful Execution.");
    } else if (this.waiting) {
      output.setResults("Waiting for Child request completion.");
    }
    output.setProcessOutput(overrides);
    output.setDetails(details.toString());
    return output;
  }

  @Override
  public boolean isWaiting() {
    return this.waiting;
  }

  @Override
  public String getProcessCode() {
    return AutomationElementRegistry.US_BP_PROCESS;
  }

  @Override
  public String getProcessDesc() {
    return "US - Business Partner Process";
  }

  @Override
  public void resetWaitingStatus() {
    this.waiting = false;
  }

}
