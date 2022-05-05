package com.ibm.cmr.create.batch.util.masscreate.handler.impl;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.entity.MassUpdt;
import com.ibm.cmr.create.batch.util.BatchUtil;
import com.ibm.cmr.create.batch.util.DebugUtil;
import com.ibm.cmr.services.client.CmrServicesFactory;
import com.ibm.cmr.services.client.ProcessClient;
import com.ibm.cmr.services.client.ServiceClient;
import com.ibm.cmr.services.client.process.ProcessRequest;
import com.ibm.cmr.services.client.process.ProcessResponse;
import com.ibm.cmr.services.client.process.RDcRecord;

/**
 * 
 * @author 136786PH1
 *
 */
public class IERPMassWorker implements Runnable {

  private static final Logger LOG = Logger.getLogger(IERPMassWorker.class);
  private static final String MASS_UPDATE_DONE = "DONE";
  private static final String MASS_UPDATE_FAIL = "FAIL";

  private EntityManager entityManager;
  private MassUpdt sMassUpdt;
  private Admin admin;
  private String userId;
  private Data data;
  private List<String> statusCodes = new ArrayList<String>();
  private List<String> rdcProcessStatusMsgs = new ArrayList<String>();
  private StringBuilder comment;
  private boolean isIndexNotUpdated;
  private boolean error;
  private Exception errorMsg;

  public IERPMassWorker(EntityManager entityManager, MassUpdt massUpdt, Admin admin, Data data, String userId) {
    this.entityManager = entityManager;
    this.sMassUpdt = massUpdt;
    this.admin = admin;
    this.userId = userId;
    this.data = data;
  }

  @Override
  public void run() {
    processIERPRequest();
  }

  private void processIERPRequest() {

    String processingStatus = admin.getRdcProcessingStatus() != null ? admin.getRdcProcessingStatus() : "";

    comment = new StringBuilder();
    ProcessRequest request = new ProcessRequest();
    request.setCmrNo(sMassUpdt.getCmrNo());
    request.setMandt(SystemConfiguration.getValue("MANDT"));
    request.setReqId(admin.getId().getReqId());
    request.setReqType(admin.getReqType());
    request.setUserId(this.userId);
    request.setSapNo("");
    request.setAddrType("");
    request.setSeqNo("");

    // call the create cmr service
    LOG.info("Sending request to Process Service [Request ID: " + request.getReqId() + " Type: " + request.getReqType() + "]");

    if (LOG.isTraceEnabled()) {
      LOG.trace("Request JSON:");
      DebugUtil.printObjectAsJson(LOG, request);
    }

    ProcessResponse response = null;
    String applicationId = BatchUtil.getAppId(data.getCmrIssuingCntry());
    if (applicationId == null) {
      LOG.debug("No Application ID mapped to " + data.getCmrIssuingCntry());
      response = new ProcessResponse();
      response.setReqId(request.getReqId());
      response.setCmrNo(request.getCmrNo());
      response.setMandt(request.getMandt());
      response.setStatus(CmrConstants.RDC_STATUS_NOT_COMPLETED);
      response.setMessage("No application ID defined for Country: " + data.getCmrIssuingCntry() + ". Cannot process RDc records.");
    } else {
      try {
        ServiceClient serviceClient = CmrServicesFactory.getInstance().createClient(SystemConfiguration.getValue("BATCH_SERVICES_URL"),
            ProcessClient.class);
        serviceClient.setReadTimeout(60 * 30 * 1000); // 30 mins
        response = serviceClient.executeAndWrap(applicationId, request, ProcessResponse.class);

        if (response != null && response.getStatus().equals("A") && response.getMessage().contains("was not successfully updated on the index.")) {
          isIndexNotUpdated = true;
          response.setStatus("C");
          response.setMessage("");
        }
      } catch (Exception e) {
        sMassUpdt.setRowStatusCd(MASS_UPDATE_FAIL);
        LOG.error("Error when connecting to the service.", e);
        response = new ProcessResponse();
        response.setReqId(admin.getId().getReqId());
        response.setCmrNo(request.getCmrNo());
        response.setMandt(request.getMandt());
        response.setStatus(CmrConstants.RDC_STATUS_ABORTED);
        response.setMessage("Cannot connect to the service at the moment.");
      }
    }

    //
    if (response.getReqId() <= 0) {
      response.setReqId(request.getReqId());
    }

    String resultCode = response.getStatus();

    if (StringUtils.isBlank(resultCode)) {
      statusCodes.add(CmrConstants.RDC_STATUS_NOT_COMPLETED);
    } else {
      statusCodes.add(resultCode);
    }

    if (LOG.isTraceEnabled()) {
      LOG.trace("Response JSON:");
      DebugUtil.printObjectAsJson(LOG, response);
    }

    if (isCompletedSuccessfully(resultCode)) {
      if (response.getRecords() != null) {
        if (response != null && response.getRecords() != null && response.getRecords().size() > 0) {

          if (CmrConstants.RDC_STATUS_COMPLETED_WITH_WARNINGS.equals(resultCode)) {
            comment.append("RDc records were not processed.");
            comment = comment.append("Warning Message: " + response.getMessage());
          } else {
            comment.append("Record with the following Kunnr, Address sequence and address types on request ID " + admin.getId().getReqId()
                + " was SUCCESSFULLY processed:\n");
            for (RDcRecord pRecord : response.getRecords()) {
              comment.append("Kunnr: " + pRecord.getSapNo() + ", sequence number: " + pRecord.getSeqNo() + ", ");
              comment.append(" address type: " + pRecord.getAddressType() + "\n");
            }
          }
        }

      } else {
        comment.append("RDc records were not processed.");
        if (CmrConstants.RDC_STATUS_COMPLETED_WITH_WARNINGS.equals(resultCode)) {
          comment = comment.append("Warning Message: " + response.getMessage());
        }
      }

      if (StringUtils.isEmpty(sMassUpdt.getErrorTxt())) {
        sMassUpdt.setErrorTxt(comment.toString());
      } else {
        sMassUpdt.setErrorTxt(sMassUpdt.getErrorTxt() + comment.toString());
      }

      sMassUpdt.setRowStatusCd(MASS_UPDATE_DONE);

      rdcProcessStatusMsgs.add(CmrConstants.RDC_STATUS_COMPLETED);
    } else {
      if (CmrConstants.RDC_STATUS_ABORTED.equals(resultCode) && CmrConstants.RDC_STATUS_ABORTED.equals(processingStatus)) {
        comment = comment.append("\nRDc mass update processing for REQ ID " + request.getReqId() + " was ABORTED.");
        sMassUpdt.setRowStatusCd(CmrConstants.MASS_CREATE_ROW_STATUS_FAIL);
        sMassUpdt.setErrorTxt(comment.toString());
        rdcProcessStatusMsgs.add(resultCode);
      } else if (CmrConstants.RDC_STATUS_ABORTED.equalsIgnoreCase(resultCode)) {
        comment = comment.append("\nRDc mass update processing for REQ ID " + request.getReqId() + " was ABORTED.");
        sMassUpdt.setRowStatusCd(CmrConstants.MASS_CREATE_ROW_STATUS_FAIL);
        sMassUpdt.setErrorTxt(comment.toString());
        rdcProcessStatusMsgs.add(resultCode);
      } else if (CmrConstants.RDC_STATUS_NOT_COMPLETED.equalsIgnoreCase(resultCode)) {
        comment = comment.append("\nRDc mass update processing for REQ ID " + request.getReqId() + " is NOT COMPLETED.");
        sMassUpdt.setRowStatusCd(CmrConstants.MASS_CREATE_ROW_STATUS_FAIL);
        rdcProcessStatusMsgs.add(resultCode);
        sMassUpdt.setErrorTxt(comment.toString());
      } else if (CmrConstants.RDC_STATUS_IGNORED.equalsIgnoreCase(resultCode)) {
        comment = comment.append("\nRDc mass update processing for REQ ID " + request.getReqId() + " is IGNORED.");
        sMassUpdt.setRowStatusCd(CmrConstants.MASS_CREATE_ROW_STATUS_UPDATE_FAILE);
        sMassUpdt.setErrorTxt(comment.toString());
        rdcProcessStatusMsgs.add(resultCode);
      } else {
        sMassUpdt.setRowStatusCd(CmrConstants.MASS_CREATE_ROW_STATUS_DONE);
        sMassUpdt.setErrorTxt("");
      }
    }
    entityManager.merge(sMassUpdt);
    entityManager.flush();

  }

  /**
   * Checks if the status is a completed status
   * 
   * @param status
   * @return
   */
  private boolean isCompletedSuccessfully(String status) {
    return CmrConstants.RDC_STATUS_COMPLETED.equals(status) || CmrConstants.RDC_STATUS_COMPLETED_WITH_WARNINGS.equals(status);
  }

  public List<String> getStatusCodes() {
    return statusCodes;
  }

  public void setStatusCodes(List<String> statusCodes) {
    this.statusCodes = statusCodes;
  }

  public List<String> getRdcProcessStatusMsgs() {
    return rdcProcessStatusMsgs;
  }

  public void setRdcProcessStatusMsgs(List<String> rdcProcessStatusMsgs) {
    this.rdcProcessStatusMsgs = rdcProcessStatusMsgs;
  }

  public boolean getIndexNotUpdated() {
    return isIndexNotUpdated;
  }

  public boolean isError() {
    return error;
  }

  public Exception getErrorMsg() {
    return errorMsg;
  }

  public String getComments() {
    return this.comment.toString();
  }
}
