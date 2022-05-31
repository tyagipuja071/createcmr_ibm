package com.ibm.cmr.create.batch.util.masscreate.handler.impl;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.FlushModeType;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.entity.MassUpdt;
import com.ibm.cio.cmr.request.entity.listeners.ChangeLogListener;
import com.ibm.cmr.create.batch.util.BatchUtil;
import com.ibm.cmr.create.batch.util.DebugUtil;
import com.ibm.cmr.services.client.CmrServicesFactory;
import com.ibm.cmr.services.client.ProcessClient;
import com.ibm.cmr.services.client.ServiceClient;
import com.ibm.cmr.services.client.process.ProcessRequest;
import com.ibm.cmr.services.client.process.ProcessResponse;
import com.ibm.cmr.services.client.process.RDcRecord;

public class USMassProcessWorker implements Runnable {

  private static final Logger LOG = Logger.getLogger(USMassProcessWorker.class);

  private EntityManager entityManager;
  private Admin admin;
  private Data data;
  private MassUpdt massUpdt;
  private String userId;
  private boolean error;
  private Exception errorMsg;

  private StringBuilder comment;
  ProcessResponse response = null;
  protected static String BATCH_USER_ID = "CreateCMR";
  private static final String MASS_UPDATE_FAIL = "FAIL";
  private static final String MASS_UPDATE_DONE = "DONE";
  private List<String> rdcProcessStatusMsgs = new ArrayList<String>();
  private List<String> statusCodes = new ArrayList<String>();

  private boolean isIndexNotUpdated;

  public USMassProcessWorker(EntityManagerFactory emf, Admin admin, Data data, MassUpdt massUpdt, String userId) {
    EntityManager entityManager = emf.createEntityManager();
    entityManager.setFlushMode(FlushModeType.COMMIT);
    // SystemUtil.setManager(entityManager);
    this.entityManager = entityManager;
    this.admin = admin;
    this.data = data;
    this.massUpdt = massUpdt;
    this.userId = userId;
  }

  @Override
  public void run() {
    EntityTransaction transaction = null;
    try {
      ChangeLogListener.setManager(entityManager);
      transaction = entityManager.getTransaction();
      transaction.begin();
      processMassUpdate(entityManager);
      if (transaction != null && transaction.isActive() && !transaction.getRollbackOnly()) {
        transaction.commit();
      }
    } catch (Throwable e) {
      LOG.error("An error was encountered during processing. Transaction will be rolled back.", e);
      if (transaction != null && transaction.isActive()) {
        transaction.rollback();
      }
      throw e;
    } finally {
      if (transaction != null && transaction.isActive()) {
        transaction.rollback();
      }
      // empty the manager
      entityManager.clear();
      entityManager.close();
    }

  }

  private void processMassUpdate(EntityManager entityManager) {
    try {

      ServiceClient serviceClient = CmrServicesFactory.getInstance().createClient(SystemConfiguration.getValue("BATCH_SERVICES_URL"),
          ProcessClient.class);
      String resultCode = null;
      String processingStatus = admin.getRdcProcessingStatus() != null ? admin.getRdcProcessingStatus() : "";
      List<String> statusCodes = new ArrayList<String>();

      comment = new StringBuilder();
      ProcessRequest request = new ProcessRequest();
      request.setCmrNo(massUpdt.getCmrNo());
      request.setMandt(SystemConfiguration.getValue("MANDT"));
      request.setReqId(admin.getId().getReqId());
      request.setReqType(admin.getReqType());
      request.setUserId(BATCH_USER_ID);
      request.setSapNo("");
      request.setAddrType("");
      request.setSeqNo("");

      // call the create cmr service
      LOG.info("Sending request to Process Service [Request ID: " + request.getReqId() + " Type: " + request.getReqType() + "]");

      if (LOG.isTraceEnabled()) {
        LOG.trace("Request JSON:");
        DebugUtil.printObjectAsJson(LOG, request);
      }

      response = null;
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
          serviceClient.setReadTimeout(60 * 30 * 1000); // 30 mins
          response = serviceClient.executeAndWrap(applicationId, request, ProcessResponse.class);

          if (response != null && response.getStatus().equals("A") && response.getMessage().contains("was not successfully updated on the index.")) {
            isIndexNotUpdated = true;
            response.setStatus("C");
            response.setMessage("");
          }
        } catch (Exception e) {
          massUpdt.setRowStatusCd(MASS_UPDATE_FAIL);
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

      resultCode = response.getStatus();
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
            comment.append("Record with the following Kunnr, Address sequence and address types on request ID " + admin.getId().getReqId()
                + " was SUCCESSFULLY processed:\n");
            for (RDcRecord pRecord : response.getRecords()) {
              comment.append("Kunnr: " + pRecord.getSapNo() + ", sequence number: " + pRecord.getSeqNo() + ", ");
              comment.append(" address type: " + pRecord.getAddressType() + "\n");
            }
          }
        } else {
          comment.append("RDc records were not processed.");
          if (CmrConstants.RDC_STATUS_COMPLETED_WITH_WARNINGS.equals(resultCode)) {
            comment = comment.append("Warning Message: " + response.getMessage());
          }
        }

        if (StringUtils.isEmpty(massUpdt.getErrorTxt())) {
          massUpdt.setErrorTxt(comment.toString());
        } else {
          massUpdt.setErrorTxt(massUpdt.getErrorTxt() + comment.toString());
        }

        massUpdt.setRowStatusCd(MASS_UPDATE_DONE);

        rdcProcessStatusMsgs.add(CmrConstants.RDC_STATUS_COMPLETED);
      } else {
        if (CmrConstants.RDC_STATUS_ABORTED.equals(resultCode) && CmrConstants.RDC_STATUS_ABORTED.equals(processingStatus)) {
          comment = comment
              .append("\nRDc mass update processing for REQ ID " + request.getReqId() + ", CMR NO = " + massUpdt.getCmrNo() + " was ABORTED.");
          massUpdt.setRowStatusCd(CmrConstants.MASS_CREATE_ROW_STATUS_FAIL);
          massUpdt.setErrorTxt(comment.toString());
          rdcProcessStatusMsgs.add(resultCode);
        } else if (CmrConstants.RDC_STATUS_ABORTED.equalsIgnoreCase(resultCode)) {
          comment = comment
              .append("\nRDc mass update processing for REQ ID " + request.getReqId() + ", CMR NO = " + massUpdt.getCmrNo() + " was ABORTED.");
          massUpdt.setRowStatusCd(CmrConstants.MASS_CREATE_ROW_STATUS_FAIL);
          massUpdt.setErrorTxt(comment.toString());
          rdcProcessStatusMsgs.add(resultCode);
        } else if (CmrConstants.RDC_STATUS_NOT_COMPLETED.equalsIgnoreCase(resultCode)) {
          comment = comment
              .append("\nRDc mass update processing for REQ ID " + request.getReqId() + ", CMR NO = " + massUpdt.getCmrNo() + " is NOT COMPLETED.");
          massUpdt.setRowStatusCd(CmrConstants.MASS_CREATE_ROW_STATUS_FAIL);
          rdcProcessStatusMsgs.add(resultCode);
          massUpdt.setErrorTxt(comment.toString());
        } else if (CmrConstants.RDC_STATUS_IGNORED.equalsIgnoreCase(resultCode)) {
          comment = comment
              .append("\nRDc mass update processing for REQ ID " + request.getReqId() + ", CMR NO = " + massUpdt.getCmrNo() + " is IGNORED.");
          massUpdt.setRowStatusCd(CmrConstants.MASS_CREATE_ROW_STATUS_UPDATE_FAILE);
          massUpdt.setErrorTxt(comment.toString());
          rdcProcessStatusMsgs.add(resultCode);
        } else {
          massUpdt.setRowStatusCd(CmrConstants.MASS_CREATE_ROW_STATUS_DONE);
          massUpdt.setErrorTxt("");
        }
      }
      // updateEntity(massUpdt, entityManager);
      // admin.setReqStatus(CmrConstants.REQUEST_STATUS.COM.toString());
      // admin.setProcessedFlag("Y");
      // updateEntity(admin, entityManager);
      // partialCommit(entityManager);
      entityManager.merge(massUpdt);
      // entityManager.merge(admin);
      entityManager.flush();
    } catch (Exception e) {
      LOG.debug("Error in processing Mass Update: Request " + massUpdt.getId().getParReqId() + " Iter " + massUpdt.getId().getIterationId() + " Seq "
          + massUpdt.getId().getSeqNo() + " CMR No. " + massUpdt.getCmrNo(), e);
      this.error = true;
      this.errorMsg = e;
    }
  }

  /**
   * Checks if the status is a completed status
   * 
   * @param status
   * @return
   */
  protected boolean isCompletedSuccessfully(String status) {
    return CmrConstants.RDC_STATUS_COMPLETED.equals(status) || CmrConstants.RDC_STATUS_COMPLETED_WITH_WARNINGS.equals(status);
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

  public List<String> getRdcProcessStatusMsgs() {
    return rdcProcessStatusMsgs;
  }

  public List<String> getStatusCodes() {
    return statusCodes;
  }

  public boolean getIndexNotUpdated() {
    return isIndexNotUpdated;
  }

}