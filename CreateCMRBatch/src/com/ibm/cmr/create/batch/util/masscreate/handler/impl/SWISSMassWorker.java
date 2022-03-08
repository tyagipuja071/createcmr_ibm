/**
 * 
 */
package com.ibm.cmr.create.batch.util.masscreate.handler.impl;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.entity.MassUpdt;
import com.ibm.cio.cmr.request.entity.listeners.ChangeLogListener;
import com.ibm.cmr.create.batch.util.BatchUtil;
import com.ibm.cmr.create.batch.util.CMRRequestContainer;
import com.ibm.cmr.create.batch.util.DebugUtil;
import com.ibm.cmr.services.client.CmrServicesFactory;
import com.ibm.cmr.services.client.ProcessClient;
import com.ibm.cmr.services.client.ServiceClient;
import com.ibm.cmr.services.client.process.ProcessRequest;
import com.ibm.cmr.services.client.process.ProcessResponse;
import com.ibm.cmr.services.client.process.RDcRecord;

/**
 * @author 05634V744
 *
 */
public class SWISSMassWorker implements Runnable {

  // private ProcessClient serviceClient;
  private static final String COMMENT_LOGGER = "Swiss Service";
  public static final String CMR_REQUEST_REASON_TEMP_REACT_EMBARGO = "TREC";
  private boolean massServiceMode;
  private CMRRequestContainer cmrObjects;
  private static final String[] ADDRESS_ORDER = { "ZS01", "ZP01", "ZI01", "ZD01", "ZD02", "ZP02" };
  private long reQId;
  private static final String MASS_UPDATE_FAIL = "FAIL";
  private static final String MASS_UPDATE_DONE = "DONE";
  public static final String CMR_REQUEST_STATUS_CPR = "CPR";
  public static final String CMR_REQUEST_STATUS_PCP = "PCP";

  private static final Logger LOG = Logger.getLogger(SWISSMassWorker.class);

  private EntityManager entityManager;
  private MassUpdt sMassUpdt;
  private Admin admin;
  private String userId;
  private Data data;
  private List<String> statusCodes = new ArrayList<String>();
  private List<String> rdcProcessStatusMsgs = new ArrayList<String>();
  private boolean isIndexNotUpdated;
  private StringBuilder comment;
  private boolean error;
  private Exception errorMsg;

  @Override
  public void run() {
    // TODO Auto-generated method stub
    EntityTransaction transaction = null;
    try {

      ChangeLogListener.setManager(entityManager);
      transaction = entityManager.getTransaction();
      transaction.begin();

      processSWissMassRequest(entityManager);

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

  private void processSWissMassRequest(EntityManager entityManager) {

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
    LOG.info("Sending request to Process Service [Request ID: " + request.getReqId() + " Type: " + request.getReqType() + " CMRNo: "
        + request.getCmrNo() + "]");

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

      if (StringUtils.isEmpty(sMassUpdt.getErrorTxt())) {
        sMassUpdt.setErrorTxt(comment.toString());
      } else {
        sMassUpdt.setErrorTxt(sMassUpdt.getErrorTxt() + comment.toString());
      }

      sMassUpdt.setRowStatusCd(MASS_UPDATE_DONE);

      rdcProcessStatusMsgs.add(CmrConstants.RDC_STATUS_COMPLETED);
    } else {
      if (CmrConstants.RDC_STATUS_ABORTED.equals(resultCode) && CmrConstants.RDC_STATUS_ABORTED.equals(processingStatus)) {
        comment = comment
            .append("\nRDc mass update processing for REQ ID " + request.getReqId() + " for CMRNo" + request.getCmrNo() + " was ABORTED.");
        sMassUpdt.setRowStatusCd(CmrConstants.MASS_CREATE_ROW_STATUS_FAIL);
        sMassUpdt.setErrorTxt(comment.toString());
        rdcProcessStatusMsgs.add(resultCode);
      } else if (CmrConstants.RDC_STATUS_ABORTED.equalsIgnoreCase(resultCode)) {
        comment = comment
            .append("\nRDc mass update processing for REQ ID " + request.getReqId() + " for CMRNo" + request.getCmrNo() + " was ABORTED.");
        sMassUpdt.setRowStatusCd(CmrConstants.MASS_CREATE_ROW_STATUS_FAIL);
        sMassUpdt.setErrorTxt(comment.toString());
        rdcProcessStatusMsgs.add(resultCode);
      } else if (CmrConstants.RDC_STATUS_NOT_COMPLETED.equalsIgnoreCase(resultCode)) {
        comment = comment
            .append("\nRDc mass update processing for REQ ID " + request.getReqId() + " for CMRNo" + request.getCmrNo() + " is NOT COMPLETED.");
        sMassUpdt.setRowStatusCd(CmrConstants.MASS_CREATE_ROW_STATUS_FAIL);
        rdcProcessStatusMsgs.add(resultCode);
        sMassUpdt.setErrorTxt(comment.toString());
      } else if (CmrConstants.RDC_STATUS_IGNORED.equalsIgnoreCase(resultCode)) {
        comment = comment
            .append("\nRDc mass update processing for REQ ID " + request.getReqId() + " for CMRNo" + request.getCmrNo() + " is IGNORED.");
        sMassUpdt.setRowStatusCd(CmrConstants.MASS_CREATE_ROW_STATUS_UPDATE_FAILE);
        sMassUpdt.setErrorTxt(comment.toString());
        rdcProcessStatusMsgs.add(resultCode);
      } else {
        sMassUpdt.setRowStatusCd(CmrConstants.MASS_CREATE_ROW_STATUS_DONE);
        sMassUpdt.setErrorTxt("");
      }
    }

    entityManager.merge(sMassUpdt);
    entityManager.merge(admin);
    entityManager.flush();
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

  /**
   * Checks if the status is a completed status
   * 
   * @param status
   * @return
   */
  private boolean isCompletedSuccessfully(String status) {
    return CmrConstants.RDC_STATUS_COMPLETED.equals(status) || CmrConstants.RDC_STATUS_COMPLETED_WITH_WARNINGS.equals(status);
  }

  public SWISSMassWorker(EntityManagerFactory emf, MassUpdt massUpdt, Admin admin, Data data, String userId) {
    EntityManager entityManager = emf.createEntityManager();
    this.entityManager = entityManager;
    this.sMassUpdt = massUpdt;
    this.admin = admin;
    this.userId = userId;
    this.data = data;
  }

  public boolean isError() {
    return error;
  }

  public Exception getErrorMsg() {
    return errorMsg;
  }

  public boolean getIndexNotUpdated() {
    return isIndexNotUpdated;
  }

  public String getComments() {
    return this.comment.toString();
  }
}
