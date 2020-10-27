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
import com.ibm.cio.cmr.request.automation.util.geo.us.USBPEndUserHandler;
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
  public static final String RESTRICT_TO_END_USER = "BPQS";
  public static final String RESTRICT_TO_MAINTENANCE = "IRCSO";

  public static final String BP_MANAGING_IR = "MIR";
  public static final String BP_INDIRECT_REMARKETER = "IRMR";

  public static final String TYPE_STATE_AND_LOCAL = "7";
  public static final String TYPE_FEDERAL = "9";
  public static final String TYPE_COMMERCIAL = "1";
  public static final String TYPE_BUSINESS_PARTNER = "5";

  public static final String SUB_TYPE_STATE_AND_LOCAL_STATE = "STATE";
  public static final String SUB_TYPE_STATE_AND_LOCAL_DISTRICT = "SPEC DIST";
  public static final String SUB_TYPE_STATE_AND_LOCAL_COUNTY = "COUNTY";
  public static final String SUB_TYPE_STATE_AND_LOCAL_CITY = "CITY";
  public static final String SUB_TYPE_FEDERAL_POA = "POA";
  public static final String SUB_TYPE_FEDERAL_REGULAR_GOVT = "FEDERAL";
  public static final String SUB_TYPE_COMMERCIAL_REGULAR = "REGULAR";
  public static final String SUB_TYPE_BUSINESS_PARTNER_END_USER = "END USER";

  public static final String AFFILIATE_FEDERAL = "9200000";
  private boolean waiting;

  public USBusinessPartnerElement(String requestTypes, String actionOnError, boolean overrideData, boolean stopOnError) {
    super(requestTypes, actionOnError, overrideData, stopOnError);
  }

  @Override
  public AutomationResult<OverrideOutput> executeElement(EntityManager entityManager, RequestData requestData, AutomationEngineData engineData)
      throws Exception {

    Admin admin = requestData.getAdmin();
    long reqId = admin.getId().getReqId();
    Data data = requestData.getData();
    AutomationResult<OverrideOutput> output = buildResult(reqId);
    Addr addr = requestData.getAddress("ZS01");
    USBPHandler bpHandler = new USBPEndUserHandler();

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
    if (childReqId > 0 && bpHandler.isChildRequestSupported()) {
      childCompleted = bpHandler.checkIfChildRequestCompleted(entityManager, engineData, requestData, childRequest, details, output, ibmCmr, handler);
      if (!childCompleted) {
        this.waiting = bpHandler.isWaiting();
        return output;
      }
    }

    boolean processed = bpHandler.processRequest(entityManager, requestData, engineData, output, details, childCompleted, ibmCmr, childRequest,
        handler, overrides);
    // check if request should wait
    this.waiting = bpHandler.isWaiting();
    if (processed && !this.waiting) {
      // copy from IBM Direct if found, and fill the rest of BO codes
      bpHandler.copyAndFillIBMData(entityManager, handler, ibmCmr, requestData, engineData, details, overrides, childRequest);

      bpHandler.doFinalValidations(engineData, requestData, details, overrides, output, ibmCmr);
    }

    output.setProcessOutput(overrides);
    output.setDetails(details.toString());
    output.setResults("Success");
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

}
