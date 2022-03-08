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
import com.ibm.cio.cmr.request.entity.MassUpdtData;
import com.ibm.cio.cmr.request.entity.MassUpdtDataPK;
import com.ibm.cio.cmr.request.entity.ReqCmtLog;
import com.ibm.cio.cmr.request.entity.listeners.ChangeLogListener;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.util.SystemLocation;
import com.ibm.cmr.create.batch.util.BatchUtil;
import com.ibm.cmr.create.batch.util.DebugUtil;
import com.ibm.cmr.services.client.CmrServicesFactory;
import com.ibm.cmr.services.client.ProcessClient;
import com.ibm.cmr.services.client.ServiceClient;
import com.ibm.cmr.services.client.process.ProcessRequest;
import com.ibm.cmr.services.client.process.ProcessResponse;
import com.ibm.cmr.services.client.process.RDcRecord;

/**
 * @author JeffZAMORA
 *
 */
public class LDMassProcessRdcWorker implements Runnable {

  private static final Logger LOG = Logger.getLogger(LDMassProcessRdcWorker.class);
  private static final String MASS_UPDATE_FAIL = "FAIL";
  private static final String MASS_UPDATE_DONE = "DONE";

  private EntityManager entityManager;
  private Admin admin;
  private Data data;
  private MassUpdt massUpdt;
  private String userId;

  private List<String> statusCodes = new ArrayList<String>();
  private List<String> rdcProcessStatusMsgs = new ArrayList<String>();

  private StringBuilder comment;

  private boolean error;
  private Exception errorMsg;
  private boolean isIndexNotUpdated;

  public LDMassProcessRdcWorker(EntityManagerFactory emf, Admin admin, Data data, MassUpdt massUpdt, String userId) {
    // SystemUtil.setManager(entityManager);
    EntityManager entityManager = emf.createEntityManager();
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
      processLDRdc();
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

  private void processLDRdc() {
    try {

      ServiceClient serviceClient = CmrServicesFactory.getInstance().createClient(SystemConfiguration.getValue("BATCH_SERVICES_URL"),
          ProcessClient.class);

      String processingStatus = admin.getRdcProcessingStatus() != null ? admin.getRdcProcessingStatus() : "";

      comment = new StringBuilder();

      if (!CmrConstants.REQUEST_STATUS.PCO.toString().equals(admin.getReqStatus())) {
        admin.setReqStatus(CmrConstants.REQUEST_STATUS.PCO.toString());
        entityManager.merge(admin);
      }
      // CMR-2279: ISR update in massUpdtData for Turkey
      if (SystemLocation.TURKEY.equals(data.getCmrIssuingCntry())) {
        MassUpdtDataPK muDataPK = new MassUpdtDataPK();
        muDataPK.setIterationId(massUpdt.getId().getIterationId());
        muDataPK.setParReqId(massUpdt.getId().getParReqId());
        muDataPK.setSeqNo(massUpdt.getId().getSeqNo());
        MassUpdtData muData = entityManager.find(MassUpdtData.class, muDataPK);

        if (!StringUtils.isBlank(muData.getCustNm1())) {
          String sql = ExternalizedQuery.getSql("LEGACY.GET_ISR_BYSBO");
          PreparedQuery q = new PreparedQuery(entityManager, sql);
          q.setParameter("SBO", muData.getCustNm1());
          q.setParameter("CNTRY", SystemLocation.TURKEY);
          String isr = q.getSingleResult(String.class);
          if (!StringUtils.isBlank(isr)) {
            muData.setRepTeamMemberNo(isr);
            entityManager.merge(muData);
          } else {
            muData.setRepTeamMemberNo("");
            entityManager.merge(muData);
          }
        }
      }

      ProcessRequest request = new ProcessRequest();
      request.setCmrNo(massUpdt.getCmrNo());
      request.setMandt(SystemConfiguration.getValue("MANDT"));
      request.setReqId(admin.getId().getReqId());
      request.setReqType(admin.getReqType());
      request.setUserId(userId);
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
          if (isForErrorTests(entityManager, admin)) {
            response = processMassUpdateError(admin, request.getCmrNo());
          } else {
            serviceClient.setReadTimeout(60 * 30 * 1000); // 30 mins
            response = serviceClient.executeAndWrap(applicationId, request, ProcessResponse.class);
          }

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
        if (response.getRecords() != null && response.getRecords().size() > 0) {
          if (response != null && response.getRecords() != null && response.getRecords().size() > 0) {
            comment.append("Record with the following Kunnr, Address sequence and address types on request ID " + admin.getId().getReqId()
                + " was SUCCESSFULLY processed:\n");
            LOG.debug(comment.toString());
            for (RDcRecord pRecord : response.getRecords()) {
              comment.append("Kunnr: " + pRecord.getSapNo() + ", sequence number: " + pRecord.getSeqNo() + ", ");
              comment.append(" address type: " + pRecord.getAddressType() + "\n");
              LOG.debug(comment.toString());
            }
          }
        } else {
          comment.append("\n\nRDc records were not processed.");
          if (CmrConstants.RDC_STATUS_COMPLETED_WITH_WARNINGS.equals(resultCode)) {
            comment = comment.append("\nWarning Message: " + response.getMessage());
          }
          LOG.debug(comment.toString());
        }

        if (StringUtils.isEmpty(massUpdt.getErrorTxt())) {
          massUpdt.setErrorTxt(comment.toString());
        } else {
          massUpdt.setErrorTxt(massUpdt.getErrorTxt() + comment.toString());
        }

        massUpdt.setRowStatusCd(MASS_UPDATE_DONE);
        // admin.setReqStatus(CmrConstants.REQUEST_STATUS.COM.toString());
        rdcProcessStatusMsgs.add(CmrConstants.RDC_STATUS_COMPLETED);
      } else {
        if (CmrConstants.RDC_STATUS_ABORTED.equals(resultCode) && CmrConstants.RDC_STATUS_ABORTED.equals(processingStatus)) {
          comment = comment.append("\nRDc mass update processing for REQ ID " + request.getReqId() + " was ABORTED.");
          massUpdt.setRowStatusCd(CmrConstants.MASS_CREATE_ROW_STATUS_FAIL);
          massUpdt.setErrorTxt(massUpdt.getErrorTxt() + comment.toString());
          rdcProcessStatusMsgs.add(resultCode);
        } else if (CmrConstants.RDC_STATUS_ABORTED.equalsIgnoreCase(resultCode)) {
          comment = comment.append("\nRDc mass update processing for REQ ID " + request.getReqId() + " was ABORTED.");
          massUpdt.setRowStatusCd(CmrConstants.MASS_CREATE_ROW_STATUS_FAIL);
          massUpdt.setErrorTxt(massUpdt.getErrorTxt() + comment.toString());
          rdcProcessStatusMsgs.add(resultCode);
        } else if (CmrConstants.RDC_STATUS_NOT_COMPLETED.equalsIgnoreCase(resultCode)) {
          comment = comment.append("\nRDc mass update processing for REQ ID " + request.getReqId() + " is NOT COMPLETED.");
          massUpdt.setRowStatusCd(CmrConstants.MASS_CREATE_ROW_STATUS_FAIL);
          rdcProcessStatusMsgs.add(resultCode);
          massUpdt.setErrorTxt(massUpdt.getErrorTxt() + comment.toString());
        } else if (CmrConstants.RDC_STATUS_IGNORED.equalsIgnoreCase(resultCode)) {
          comment = comment.append("\nRDc mass update processing for REQ ID " + request.getReqId() + " is IGNORED.");
          massUpdt.setRowStatusCd(CmrConstants.MASS_CREATE_ROW_STATUS_UPDATE_FAILE);
          massUpdt.setErrorTxt(massUpdt.getErrorTxt() + comment.toString());
          rdcProcessStatusMsgs.add(resultCode);
        } else {
          // DTN: Do nothing because completes should be addressed by first
          // If.
        }
        // DTN: Note that there is no need to update the req status on admin
        // table. Process should keep on trying to update what failed
        // previously. Thereby if not complete, then should remain
        // Completing in request status
        LOG.debug(comment.toString());
      }
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

  private boolean isForErrorTests(EntityManager entityManager, Admin admin) {
    boolean isForErrorTests = false;
    // 1. Check if request reason is "Other"
    if ("OTH".equals(admin.getReqReason())) {
      // 2. We need to get the bottom most comment
      // REQUESTENTRY.REQ_CMT_LOG.SEARCH_BY_REQID
      String sql = ExternalizedQuery.getSql("REQUESTENTRY.REQ_CMT_LOG.SEARCH_BY_REQID");
      PreparedQuery query = new PreparedQuery(entityManager, sql);
      query.setParameter("REQ_ID", admin.getId().getReqId());
      List<ReqCmtLog> comments = query.getResults(ReqCmtLog.class);

      if (comments != null && comments.size() > 0) {
        int size = comments.size();
        ReqCmtLog rcLog = comments.get(size - 1);
        // 3.If bottom most comment is "ERROR TEST" then we return true
        if (rcLog != null && "ERROR TEST".equals(rcLog.getCmt() != null ? rcLog.getCmt().toUpperCase() : "")) {
          isForErrorTests = true;
        }
      }
    }
    return isForErrorTests;
  }

  /**
   * This is a test method to test error returns from the service.
   * 
   * @param cmmaMgr
   * @param rdcMgr
   * @param reqContainer
   * @param mandt
   * @param katr6
   * @param requestObj
   * @return
   * @throws Exception
   */
  protected ProcessResponse processMassUpdateError(Admin admin, String cmrNo) throws Exception {
    long reqId = admin.getId().getReqId();
    String cmr = cmrNo;
    LOG.debug("**** START Mass Update operation for reqId >> " + reqId + ", cmrNo >> " + cmr + " ****");
    ProcessResponse response = new ProcessResponse();
    response.setReqId(reqId);
    response.setCmrNo(cmr);
    response.setStatus("N");
    response.setMessage("At least 1 error occurred during the mass update");
    return response;
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

  public String getComments() {
    return this.comment.toString();
  }

  public List<String> getStatusCodes() {
    return statusCodes;
  }

  public List<String> getRdcProcessStatusMsgs() {
    return rdcProcessStatusMsgs;
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

}
