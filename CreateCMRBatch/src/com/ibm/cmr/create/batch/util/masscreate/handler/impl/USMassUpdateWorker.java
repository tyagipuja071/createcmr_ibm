/**
 * 
 */
package com.ibm.cmr.create.batch.util.masscreate.handler.impl;

import java.util.List;

import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cmr.create.batch.util.BatchUtil;
import com.ibm.cmr.create.batch.util.DebugUtil;
import com.ibm.cmr.services.client.MassProcessClient;
import com.ibm.cmr.services.client.MassServicesFactory;
import com.ibm.cmr.services.client.ServiceClient;
import com.ibm.cmr.services.client.process.mass.MassProcessRequest;
import com.ibm.cmr.services.client.process.mass.MassProcessResponse;
import com.ibm.cmr.services.client.process.mass.MassUpdateRecord;

/**
 * @author 136786PH1
 *
 */
public class USMassUpdateWorker implements Runnable {

  private static final Logger LOG = Logger.getLogger(USMassUpdateWorker.class);

  private MassProcessRequest request;
  private MassProcessResponse response;
  private long reqId;
  private String issuingCntry;

  public USMassUpdateWorker(List<MassUpdateRecord> records, long reqId, MassProcessRequest origRequest, String issuingCntry) {
    this.reqId = reqId;
    this.issuingCntry = issuingCntry;
    this.request = new MassProcessRequest();
    this.request.setMandt(origRequest.getMandt());
    this.request.setReqId(reqId);
    this.request.setReqType(origRequest.getReqType());
    this.request.setUserId(origRequest.getUserId());
    this.request.setRecords(records);
  }

  @Override
  public void run() {
    runMassUpdates();
  }

  private void runMassUpdates() {
    // call the Mass Update service
    LOG.info("Sending request to Mass Update Service [Request ID: " + this.reqId + "  Type: M]");

    LOG.trace("Request JSON:");
    if (LOG.isTraceEnabled()) {
      DebugUtil.printObjectAsJson(LOG, request);
    }
    // actual service call here
    response = null;
    String applicationId = BatchUtil.getAppId(this.issuingCntry);
    if (applicationId == null) {
      LOG.debug("No Application ID mapped to " + this.issuingCntry);
      response = new MassProcessResponse();
      response.setReqId(request.getReqId());
      response.setMandt(request.getMandt());
      response.setStatus(CmrConstants.RDC_STATUS_NOT_COMPLETED);
      response.setMsg("No application ID defined for Country: " + this.issuingCntry + ". Cannot process RDc records.");
    } else {
      try {
        ServiceClient massServiceClient = MassServicesFactory.getInstance().createClient(SystemConfiguration.getValue("BATCH_SERVICES_URL"),
            MassProcessClient.class);
        massServiceClient.setReadTimeout(60 * 15 * 1000); // 15 mins
        response = massServiceClient.executeAndWrap(applicationId, request, MassProcessResponse.class);
      } catch (Exception e) {
        LOG.error("Error when connecting to the mass change service.", e);
        response = new MassProcessResponse();
        response.setReqId(request.getReqId());
        response.setStatus(CmrConstants.RDC_STATUS_ABORTED);
        response.setMsg("A system error has occured. Setting to aborted.");
      }
    }

    LOG.trace("Response JSON:");
    if (LOG.isTraceEnabled()) {
      DebugUtil.printObjectAsJson(LOG, response);
    }
    LOG.info("Response received from Process Service [Request ID: " + response.getReqId() + " Status: " + response.getStatus() + " Message: "
        + (response.getMsg() != null ? response.getMsg() : "-") + "]");

    if (response.getReqId() <= 0) {
      response.setReqId(request.getReqId());
    }
  }

  public MassProcessResponse getResponse() {
    return response;
  }

}
