/**
 * 
 */
package com.ibm.cmr.create.batch.service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.persistence.EntityManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.AdminPK;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.entity.DataPK;
import com.ibm.cio.cmr.request.entity.MassUpdt;
import com.ibm.cio.cmr.request.entity.ReqCmtLog;
import com.ibm.cio.cmr.request.entity.WfHist;
import com.ibm.cio.cmr.request.entity.listeners.ChangeLogListener;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.util.RequestUtils;
import com.ibm.cio.cmr.request.util.SystemUtil;
import com.ibm.cmr.create.batch.util.BatchUtil;
import com.ibm.cmr.create.batch.util.CMRRequestContainer;
import com.ibm.cmr.create.batch.util.masscreate.WorkerThreadFactory;
import com.ibm.cmr.create.batch.util.mq.LandedCountryMap;
import com.ibm.cmr.create.batch.util.worker.impl.AustriaMassUpdtMultiWorker;
import com.ibm.cmr.services.client.process.ProcessRequest;
import com.ibm.cmr.services.client.process.ProcessResponse;

/**
 * @author 000S8A744
 *
 */
public class ATMassProcessMultiLegacyService extends MultiThreadedBatchService<Long> {

  private static final Logger LOG = Logger.getLogger(ATMassProcessMultiLegacyService.class);
  private static final String[] ADDRESS_ORDER = { "ZS01", "ZP01", "ZI01", "ZD01", "ZS02", "ZP02", "ZD02" };

  @Override
  public Boolean executeBatchForRequests(EntityManager entityManager, List<Long> requests) throws Exception {
    ChangeLogListener.setManager(entityManager);
    for (Long reqId : requests) {
      trackRequestLogs(reqId);
      AdminPK pk = new AdminPK();
      pk.setReqId(reqId);
      Admin admin = entityManager.find(Admin.class, pk);
      try {
        CMRRequestContainer cmrObjects = prepareRequest(entityManager, admin);
        Data data = cmrObjects.getData();
        ProcessRequest request = new ProcessRequest();
        request.setCmrNo(data.getCmrNo());
        request.setMandt(SystemConfiguration.getValue("MANDT"));
        request.setReqId(admin.getId().getReqId());
        request.setReqType(admin.getReqType());
        request.setUserId(BATCH_USER_ID);

        String histMessage = "";
        processMassUpdate(entityManager, request, admin, data, histMessage);

        if (CmrConstants.RDC_STATUS_ABORTED.equalsIgnoreCase(admin.getRdcProcessingStatus())
            || CmrConstants.RDC_STATUS_NOT_COMPLETED.equalsIgnoreCase(admin.getRdcProcessingStatus())) {
          admin.setReqStatus("PPN");
          admin.setProcessedFlag("E"); // set request status to error.
          RequestUtils.clearClaimDetails(admin);
          admin.setLockInd("N");

          if (StringUtils.isEmpty(histMessage)) {
            histMessage = "Sending back to processor due to error on RDC processing";
            histMessage += "<br>" + admin.getRdcProcessingMsg();
          }
          createHistory(entityManager, histMessage, "PPN", "RDC Processing", admin.getId().getReqId());
        } else if ((CmrConstants.RDC_STATUS_COMPLETED.equalsIgnoreCase(admin.getRdcProcessingStatus())
            || CmrConstants.RDC_STATUS_COMPLETED_WITH_WARNINGS.equalsIgnoreCase(admin.getRdcProcessingStatus()))
            && CmrConstants.REQ_TYPE_CREATE.equals(admin.getReqType())) {
          admin.setReqStatus("COM");
          admin.setProcessedFlag("Y"); // set request status to processed
          createHistory(entityManager, "Request processing Completed Successfully", "COM", "RDC Processing", admin.getId().getReqId());
        }
        partialCommit(entityManager);

      } catch (Exception e) {
        partialRollback(entityManager);
        LOG.error("Unexpected error occurred during processing of Request " + admin.getId().getReqId(), e);
        processError(entityManager, admin, e.getMessage());
      }
    }
    resetThreadName();
    return null;
  }

  @Override
  protected Queue<Long> getRequestsToProcess(EntityManager entityManager) {
    LOG.info("Initializing Country Map..");
    LandedCountryMap.init(entityManager);
    String sql = ExternalizedQuery.getSql("AT.GET_MASS_PROCESS_PENDING.RDC");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    List<Admin> pending = query.getResults(Admin.class);

    LinkedList<Long> toProcess = new LinkedList<>();
    LOG.debug((pending != null ? pending.size() : 0) + " records to process.");
    if (pending != null) {
      for (Admin admin : pending) {
        toProcess.add(admin.getId().getReqId());
      }
    }
    return toProcess;
  }

  /**
   * This is the batch process for mass update requests
   * 
   * @param entityManager
   * @param admin
   */
  public void processMassUpdate(EntityManager entityManager, ProcessRequest request, Admin admin, Data data, String histMessage) throws Exception {
    if (admin == null) {
      throw new Exception("Cannot process mass update request. Admin information is null or empty.");
    }

    long reqId = admin.getId().getReqId();
    String processingStatus = admin.getRdcProcessingStatus() != null ? admin.getRdcProcessingStatus() : "";
    try {
      // 1. Get request to process
      PreparedQuery query = new PreparedQuery(entityManager, ExternalizedQuery.getSql("BATCH.MA.GET.MASS_UPDT"));
      query.setParameter("REQ_ID", admin.getId().getReqId());
      query.setParameter("ITER_ID", admin.getIterationId());

      List<MassUpdt> resultsMain = query.getResults(MassUpdt.class);
      List<String> statusCodes = new ArrayList<String>();
      StringBuilder comment = new StringBuilder();
      boolean isIndexNotUpdated = false;

      List<String> rdcProcessStatusMsgs = new ArrayList<String>();
      HashMap<String, String> overallStatus = new HashMap<String, String>();
      if (resultsMain != null && resultsMain.size() > 0) {
        // 2. If results are not empty, lock the admin record
        lockRecord(entityManager, admin);

        int threads = 5;
        String threadCount = BatchUtil.getProperty("multithreaded.threadCount");
        if (threadCount != null && StringUtils.isNumeric(threadCount)) {
          threads = Integer.parseInt(threadCount);
        }

        LOG.debug("Worker threads to use: " + threads);
        LOG.debug("Starting processing AUSTRIA mass update lines at " + new Date());
        LOG.debug("Number of records found: " + resultsMain.size());

        List<AustriaMassUpdtMultiWorker> workers = new ArrayList<AustriaMassUpdtMultiWorker>();

        if (!CmrConstants.REQUEST_STATUS.PCO.toString().equals(admin.getReqStatus())) {
          admin.setReqStatus(CmrConstants.REQUEST_STATUS.PCO.toString());
          entityManager.merge(admin);
        }

        while (resultsMain.size() > 0) {

          List<MassUpdt> results = new LinkedList<MassUpdt>();
          for (int i = 0; i < 50; i++) {
            if (resultsMain.size() > 0) {
              results.add(resultsMain.remove(0));
            } else {
              break;
            }
          }

          ScheduledExecutorService executor = Executors.newScheduledThreadPool(threads, new WorkerThreadFactory(getThreadName() + reqId));
          for (MassUpdt sMassUpdt : results) {
            AustriaMassUpdtMultiWorker worker = new AustriaMassUpdtMultiWorker(this, admin, sMassUpdt);
            executor.schedule(worker, 5, TimeUnit.SECONDS);
            workers.add(worker);
          }

          executor.shutdown();
          while (!executor.isTerminated()) {
            try {
              Thread.sleep(5000);
            } catch (InterruptedException e) {
              // noop
            }
          }

          // execute flush every 50
          entityManager.flush();
        }

        LOG.debug("Finished processing mass update lines at " + new Date());

        Throwable processError = null;
        for (AustriaMassUpdtMultiWorker worker : workers) {
          if (worker != null) {
            if (worker.isError()) {
              LOG.error("Error in processing mass update rdc for Request ID " + admin.getId().getReqId() + ": " + worker.getErrorMsg());
              if (processError == null && worker.getErrorMsg() != null) {
                processError = worker.getErrorMsg();
              }
            } else {
              statusCodes.addAll(worker.getStatusCodes());
              rdcProcessStatusMsgs.addAll(worker.getRdcProcessStatusMsgs());
              isIndexNotUpdated = isIndexNotUpdated || worker.isIndexNotUpdated();
              comment.append(worker.getComments());
            }
          }
        }
        LOG.debug("**** Status CODES --> " + statusCodes);
        LOG.debug("Worker Process Message" + comment);

        if (processError != null) {
          throw new Exception(processError);
        }

        // *** START OF FIX
        LOG.debug("**** Placing comment on success --> " + comment);
        comment = new StringBuilder();
        if (rdcProcessStatusMsgs.size() > 0) {
          if (rdcProcessStatusMsgs.contains(CmrConstants.RDC_STATUS_ABORTED)) {
            if (isIndexNotUpdated) {
              overallStatus.put("overallStatus", CmrConstants.RDC_STATUS_COMPLETED);
            } else {
              overallStatus.put("overallStatus", CmrConstants.RDC_STATUS_ABORTED);
            }
          } else if (rdcProcessStatusMsgs.contains(CmrConstants.RDC_STATUS_NOT_COMPLETED)) {
            overallStatus.put("overallStatus", CmrConstants.RDC_STATUS_NOT_COMPLETED);
          } else if (rdcProcessStatusMsgs.contains(CmrConstants.RDC_STATUS_COMPLETED)) {
            overallStatus.put("overallStatus", CmrConstants.RDC_STATUS_COMPLETED);
          }
        } else {
          LOG.error("Response statuses is empty for request ID: " + admin.getId().getReqId());
          ProcessResponse customResponse = new ProcessResponse();
          customResponse.setMessage("No data was updated on RDc for this request. Please contact Ops for assistance.");
          overallStatus.put("overallStatus", CmrConstants.RDC_STATUS_ABORTED);
        }

        if (comment != null && !StringUtils.isEmpty(comment.toString())) {
          RequestUtils.createCommentLogFromBatch(entityManager, BATCH_USER_ID, reqId, comment.toString().trim());
        } else {
          String strOverallStatus = overallStatus.get("overallStatus");

          if (strOverallStatus != null && CmrConstants.RDC_STATUS_COMPLETED.equals(strOverallStatus)) {
            comment.append("Successfully completed RDc processing for mass update request.");
          } else {
            comment.append("Issues happened generating a processing comment. Please contact your Administrator.");
          }
          RequestUtils.createCommentLogFromBatch(entityManager, BATCH_USER_ID, reqId, comment.toString().trim());
        }

        LOG.debug("Updating Admin record for Request ID " + admin.getId().getReqId());

        if (statusCodes.contains(CmrConstants.RDC_STATUS_NOT_COMPLETED)) {
          admin.setRdcProcessingStatus(CmrConstants.RDC_STATUS_NOT_COMPLETED);
          admin.setReqStatus(CmrConstants.REQUEST_STATUS.PPN.toString());
        } else if (statusCodes.contains(CmrConstants.RDC_STATUS_ABORTED)) {
          admin.setReqStatus(CmrConstants.REQUEST_STATUS.PPN.toString());
          admin.setRdcProcessingStatus(
              processingStatus.equals(CmrConstants.RDC_STATUS_ABORTED) ? CmrConstants.RDC_STATUS_NOT_COMPLETED : CmrConstants.RDC_STATUS_ABORTED);
        } else if (statusCodes.contains(CmrConstants.RDC_STATUS_COMPLETED_WITH_WARNINGS)) {
          admin.setRdcProcessingStatus(CmrConstants.RDC_STATUS_COMPLETED_WITH_WARNINGS);
        } else {
          admin.setRdcProcessingStatus(CmrConstants.RDC_STATUS_COMPLETED);
          admin.setProcessedFlag("Y"); // set request status to processed
        }

        String rdcProcessingMsg = null;
        if ("N".equals(admin.getRdcProcessingStatus()) || "A".equals(admin.getRdcProcessingStatus())) {
          rdcProcessingMsg = "Some errors occurred during processing. Please check request's comment log for details.";
        } else {
          rdcProcessingMsg = "RDc Processing has been completed. Please check request's comment log for details.";
        }

        admin.setRdcProcessingTs(SystemUtil.getCurrentTimestamp());
        admin.setRdcProcessingMsg(rdcProcessingMsg.toString().trim());

        LOG.debug("Request Processing status" + admin.getRdcProcessingStatus());

        if (!"A".equals(admin.getRdcProcessingStatus()) && !"N".equals(admin.getRdcProcessingStatus())) {
          admin.setReqStatus("COM");
        }
        updateEntity(admin, entityManager);

        if ("N".equals(admin.getRdcProcessingStatus()) || "A".equals(admin.getRdcProcessingStatus())) {
          RequestUtils.createWorkflowHistoryFromBatch(entityManager, BATCH_USER_ID, admin,
              "Some errors occurred during RDc processing. Please check request's comment log for details.", TransConnService.ACTION_RDC_UPDATE, null,
              null, "COM".equals(admin.getReqStatus()));
        } else {
          RequestUtils.createWorkflowHistoryFromBatch(entityManager, BATCH_USER_ID, admin,
              "RDc  Processing has been completed. Please check request's comment log for details.", TransConnService.ACTION_RDC_UPDATE, null, null,
              "COM".equals(admin.getReqStatus()));
        }

        partialCommit(entityManager);
        LOG.debug(
            "Request ID " + admin.getId().getReqId() + " Status: " + admin.getRdcProcessingStatus() + " Message: " + admin.getRdcProcessingMsg());
        // *** END OF FIX

      } else {
        LOG.error("*****There are no mass update requests for RDC processing.*****");
        admin.setRdcProcessingStatus(CmrConstants.RDC_STATUS_ABORTED);
        admin.setRdcProcessingMsg(
            "There are no Mass Update records completed on Legacy that can be processed on RDc. " + "This request is being sent back in error.");
        histMessage = "There are no Mass Update records completed on Legacy that can be processed on RDc. "
            + "This request is being sent back in error.";
        RequestUtils.createCommentLogFromBatch(entityManager, BATCH_USER_ID, reqId, histMessage);
      }
      partialCommit(entityManager);
    } catch (Exception e) {
      LOG.error("Error in processing Update Request " + admin.getId().getReqId(), e);
      addError("Update Request " + admin.getId().getReqId() + " Error: " + e.getMessage());
    }
  }

  /**
   * Retrieves the {@link Admin}, {@link Data}, and {@link Addr} records based
   * on the request
   * 
   * @param cmmaMgr
   * @param reqId
   * @return
   * @throws Exception
   */
  private CMRRequestContainer prepareRequest(EntityManager entityManager, Admin admin) throws Exception {
    LOG.debug("Preparing Request Objects... ");
    CMRRequestContainer container = new CMRRequestContainer();

    DataPK dataPk = new DataPK();
    dataPk.setReqId(admin.getId().getReqId());
    Data data = entityManager.find(Data.class, dataPk);
    if (data == null) {
      throw new Exception("Cannot locate DATA record");
    }
    LOG.debug("Preparing Data Objects... " + data.getCmrNo());

    String sql = ExternalizedQuery.getSql("AT.GET.ADDR");
    // get the address order for swizz
    if (getIerpAddressOrder() != null) {
      String[] order = getIerpAddressOrder();
      StringBuilder types = new StringBuilder();
      if (order != null && order.length > 0) {

        for (String type : order) {
          LOG.trace("Looking for Address Types " + type);
          types.append(types.length() > 0 ? ", " : "");
          types.append("'" + type + "'");
        }
      }

      if (types.length() > 0) {
        sql += " and ADDR_TYPE in ( " + types.toString() + ") ";
      }
      StringBuilder orderBy = new StringBuilder();
      int orderIndex = 0;
      for (String type : order) {
        orderBy.append(" when ADDR_TYPE = '").append(type).append("' then ").append(orderIndex);
        orderIndex++;
      }
      orderBy.append(" else 25 end, ADDR_TYPE, case when IMPORT_IND = 'Y' then 0 else 1 end, ADDR_SEQ ");
      sql += " order by case " + orderBy.toString();
    }
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    // query.setForReadOnly(true);
    query.setParameter("REQ_ID", admin.getId().getReqId());
    List<Addr> addresses = query.getResults(Addr.class);

    container.setAdmin(admin);
    container.setData(data);
    if (addresses != null) {
      for (Addr addr : addresses) {
        container.addAddress(addr);
      }
    }
    return container;
  }

  private String[] getIerpAddressOrder() {
    return ADDRESS_ORDER;
  }

  @Override
  protected String getThreadName() {
    return "ATRdcMulti";
  }

  /**
   * Processes errors that happened during execution. Updates the status of the
   * {@link Admin} record and creates relevant {@link WfHist} and
   * {@link ReqCmtLog} records
   * 
   * @param entityManager
   * @param admin
   * @param errorMsg
   * @throws CmrException
   * @throws SQLException
   */
  private void processError(EntityManager entityManager, Admin admin, String errorMsg) throws CmrException, SQLException {
    if (CmrConstants.REQ_TYPE_DELETE.equals(admin.getReqType()) || CmrConstants.REQ_TYPE_REACTIVATE.equals(admin.getReqType())) {
      admin.setDisableAutoProc("Y");// disable auto processing if error on
                                    // processing
    }
    // processing pending
    LOG.info("Processing error for Request ID " + admin.getId().getReqId() + ": " + errorMsg);
    admin.setReqStatus("PPN");
    admin.setLockBy(null);
    admin.setLockByNm(null);
    admin.setLockInd("N");
    // error
    admin.setProcessedFlag("E");
    admin.setLastUpdtBy(BATCH_USER_ID);
    updateEntity(admin, entityManager);

    WfHist hist = createHistory(entityManager, "An error occurred during processing: " + errorMsg, "PPN", "Processing Error",
        admin.getId().getReqId());
    createComment(entityManager, "An error occurred during processing:\n" + errorMsg, admin.getId().getReqId());

    RequestUtils.sendEmailNotifications(entityManager, admin, hist);
  }

  /**
   * Locks the admin record
   * 
   * @param entityManager
   * @param admin
   * @throws Exception
   */
  private void lockRecord(EntityManager entityManager, Admin admin) throws Exception {
    LOG.info("Locking Request " + admin.getId().getReqId());
    admin.setLockBy(BATCH_USER_ID);
    admin.setLockByNm(BATCH_USER_ID);
    admin.setLockInd("Y");
    // error
    admin.setProcessedFlag("Wx");
    admin.setReqStatus("PCR");
    admin.setLastUpdtBy(BATCH_USER_ID);
    updateEntity(admin, entityManager);

    createHistory(entityManager, "Legacy database processing started.", "PCR", "Claim", admin.getId().getReqId());
    createComment(entityManager, "Legacy database processing started.", admin.getId().getReqId());

    partialCommit(entityManager);
  }

  @Override
  public boolean flushOnCommitOnly() {
    return true;
  }

  @Override
  public boolean isTransactional() {
    return true;
  }

  @Override
  protected boolean useServicesConnections() {
    return true;
  }

}
