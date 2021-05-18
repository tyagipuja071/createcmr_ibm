/**
 * 
 */
package com.ibm.cmr.create.batch.util.masscreate.handler.impl;

import java.util.ArrayList;
import java.util.LinkedList;
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
import com.ibm.cmr.services.client.process.mass.ResponseRecord;

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
  private List<MassUpdateRecord> records;

  public USMassUpdateWorker(List<MassUpdateRecord> records, long reqId, MassProcessRequest origRequest, String issuingCntry) {
    this.reqId = reqId;
    this.issuingCntry = issuingCntry;
    this.request = new MassProcessRequest();
    this.request.setMandt(origRequest.getMandt());
    this.request.setReqId(reqId);
    this.request.setReqType(origRequest.getReqType());
    this.request.setUserId(origRequest.getUserId());
    this.records = records;
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

      LinkedList<MassUpdateRecord> queue = new LinkedList<>();
      queue.addAll(this.records);

      List<ResponseRecord> responseRecords = new ArrayList<ResponseRecord>();
      List<MassUpdateRecord> sub = new ArrayList<MassUpdateRecord>();
      List<String> statusCodes = new ArrayList<String>();
      int finished = 0;
      while (queue.peek() != null) {
        sub.add(queue.pop());
        if (sub.size() % 50 == 0 && sub.size() > 0) {
          try {
            LOG.debug("Sending " + sub.size() + " records to process services. (Finished: " + finished + ")");
            request.setRecords(sub);
            ServiceClient massServiceClient = MassServicesFactory.getInstance().createClient(SystemConfiguration.getValue("BATCH_SERVICES_URL"),
                MassProcessClient.class);
            massServiceClient.setReadTimeout(60 * 30 * 1000); // 15 mins
            response = massServiceClient.executeAndWrap(applicationId, request, MassProcessResponse.class);
            if (response.getRecords() != null) {
              responseRecords.addAll(response.getRecords());
            }
            LOG.debug(" - Response received. Status: " + response.getStatus() + (response.getMsg() != null ? " (" + response.getMsg() + ")" : ""));
            statusCodes.add(response.getStatus());
            sub.clear();
          } catch (Exception e) {
            LOG.error("Error when connecting to the mass change service.", e);
            statusCodes.add(CmrConstants.RDC_STATUS_ABORTED);
          }
        }
        finished++;
      }

      // another round, clearout
      if (sub.size() > 0) {
        try {
          LOG.debug("Sending final " + sub.size() + " records to process services. (Finished: " + finished + ")");
          request.setRecords(sub);
          ServiceClient massServiceClient = MassServicesFactory.getInstance().createClient(SystemConfiguration.getValue("BATCH_SERVICES_URL"),
              MassProcessClient.class);
          massServiceClient.setReadTimeout(60 * 30 * 1000); // 15 mins
          response = massServiceClient.executeAndWrap(applicationId, request, MassProcessResponse.class);
          if (response.getRecords() != null) {
            responseRecords.addAll(response.getRecords());
          }
          LOG.debug(" - Response received. Status: " + response.getStatus() + (response.getMsg() != null ? " (" + response.getMsg() + ")" : ""));
          statusCodes.add(response.getStatus());
          sub.clear();
        } catch (Exception e) {
          LOG.error("Error when connecting to the mass change service.", e);
          statusCodes.add(CmrConstants.RDC_STATUS_ABORTED);
        }
      }

      response = new MassProcessResponse();
      response.setReqId(request.getReqId());
      if (statusCodes.contains(CmrConstants.RDC_STATUS_ABORTED) || statusCodes.contains(CmrConstants.RDC_STATUS_NOT_COMPLETED)) {
        response.setStatus(CmrConstants.RDC_STATUS_ABORTED);
        response.setMsg("A system error has occured in one or more records. Setting to aborted.");
      } else if (statusCodes.contains(CmrConstants.RDC_STATUS_COMPLETED_WITH_WARNINGS)) {
        response.setStatus(CmrConstants.RDC_STATUS_COMPLETED_WITH_WARNINGS);
        response.setMsg("Mass update successfully executed in RDC with warnings.");
      } else {
        response.setStatus(CmrConstants.RDC_STATUS_COMPLETED);
        response.setMsg("Mass update successfully executed in RDC.");
      }
      response.setMandt(request.getMandt());
      response.setReqType(request.getReqType());
      response.setRecords(new ArrayList<ResponseRecord>());
      response.getRecords().addAll(responseRecords);
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
