package com.ibm.cmr.create.batch.service;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.persistence.EntityManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;

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
import com.ibm.cmr.create.batch.util.worker.impl.USMassUpdtMultiWorker;
import com.ibm.cmr.services.client.process.ProcessRequest;
import com.ibm.cmr.services.client.process.ProcessResponse;

public class USMassProcessMultiService extends MultiThreadedBatchService<Long> {

  private static final Logger LOG = Logger.getLogger(USMassProcessMultiService.class);
  public static final String MASS_UDPATE_FAIL_MSG = "Errors occured while processing mass updates. Please check request summary for details.";
  public static final String ACTION_RDC_UPDATE = "System Action:RDc Update";
  private static final String[] ADDRESS_ORDER = { "ZS01", "ZP01", "ZI01", "ZD01", "ZS02", "ZP02", "ZD02" };

  private CMRRequestContainer cmrObjects;

  @Override
  public boolean isTransactional() {
    return true;
  }

  @Override
  public Boolean executeBatchForRequests(EntityManager entityManager, List<Long> requests) throws Exception {
    ChangeLogListener.setManager(entityManager);
    monitorCreqcmrMassUpd(entityManager, requests);
    ChangeLogListener.clean();
    return true;

  }

  @Override
  protected Queue<Long> getRequestsToProcess(EntityManager entityManager) {
    LinkedList<Long> toProcess = new LinkedList<>();
    List<Admin> pending = getPendingRecordsMassUpd(entityManager);

    LOG.debug((pending != null ? pending.size() : 0) + " records to process to RDc.");
    for (Admin admin : pending) {
      toProcess.add(admin.getId().getReqId());
    }
    return toProcess;
  }

  @Override
  protected String getThreadName() {
    return "USMassProcessMulti";
  }

  /**
   * Gets the Admin records with status = 'PCP' and country has processing type
   * = 'US' and req type = 'M'
   * 
   * @param entityManager
   * @return
   */
  private List<Admin> getPendingRecordsMassUpd(EntityManager entityManager) {
    String sql = ExternalizedQuery.getSql("US.GET_MASS_PROCESS_PENDING.RDC");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    return query.getResults(Admin.class);
  }

  /**
   * Processes Request Type 'M'
   * 
   * @param entityManager
   * @param request
   * @param admin
   * @param data
   * @throws Exception
   */
  protected void processMassUpdateRequest(EntityManager entityManager, ProcessRequest request, Admin admin, Data data) throws Exception {

    String processingStatus = admin.getRdcProcessingStatus() != null ? admin.getRdcProcessingStatus() : "";
    long reqId = admin.getId().getReqId();
    boolean isIndexNotUpdated = false;
    try {
      // 1. Get request to process
      PreparedQuery query = new PreparedQuery(entityManager, ExternalizedQuery.getSql("BATCH.US.GET.MASS_UPDT"));
      query.setParameter("REQ_ID", admin.getId().getReqId());
      query.setParameter("ITER_ID", admin.getIterationId());

      List<MassUpdt> resultsMain = query.getResults(MassUpdt.class);
      List<String> statusCodes = new ArrayList<String>();
      StringBuilder comment = new StringBuilder();

      List<String> rdcProcessStatusMsgs = new ArrayList<String>();
      HashMap<String, String> overallStatus = new HashMap<String, String>();
      if (resultsMain != null && resultsMain.size() > 0) {
        // 2. If results are not empty, lock the admin record
        lockRecord(entityManager, admin);
        int threads = 10;
        String threadCount = BatchUtil.getProperty("multithreaded.threadCount");
        if (threadCount != null && StringUtils.isNumeric(threadCount)) {
          threads = Integer.parseInt(threadCount);
        }

        LOG.debug("Worker threads to use: " + threads);
        LOG.debug("Number of records found: " + resultsMain.size());
        LOG.debug("Starting processing mass update lines at " + new Date());

        List<USMassUpdtMultiWorker> workers = new ArrayList<USMassUpdtMultiWorker>();

        LOG.debug(" - Processing " + resultsMain.size() + " subrecords...");
        ExecutorService executor = Executors.newFixedThreadPool(threads, new WorkerThreadFactory(getThreadName() + reqId));
        for (MassUpdt sMassUpdt : resultsMain) {
          USMassUpdtMultiWorker worker = new USMassUpdtMultiWorker(this, admin, sMassUpdt);
          executor.execute(worker);
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

        LOG.debug("Finished processing mass update lines at " + new Date());

        Throwable processError = null;
        for (USMassUpdtMultiWorker worker : workers) {
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
        if (processError != null) {
          throw new Exception(processError);
        }

        // *** START OF FIX
        LOG.debug("**** Placing comment on success --> " + comment);
        LOG.debug("Status Codes: " + statusCodes);
        LOG.debug("RDC PRocessings msgs " + rdcProcessStatusMsgs);
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
          LOG.debug("Overall Status =" + strOverallStatus);

          if (strOverallStatus != null && CmrConstants.RDC_STATUS_COMPLETED.equals(strOverallStatus)) {
            comment.append("Successfully completed RDc processing for mass update request.");
          } else if (strOverallStatus != null && !CmrConstants.RDC_STATUS_COMPLETED.equals(strOverallStatus)) {
            comment.append("Some errors occurred during processing. Please check request's summary for details.");
          } else {
            comment.append("Issues happened generating a processing comment. Please contact your Administrator.");
          }
          RequestUtils.createCommentLogFromBatch(entityManager, BATCH_USER_ID, reqId, comment.toString().trim());
        }

        LOG.debug("Updating Admin record for Request ID " + admin.getId().getReqId());

        if (rdcProcessStatusMsgs.contains(CmrConstants.RDC_STATUS_NOT_COMPLETED)) {
          admin.setRdcProcessingStatus(CmrConstants.RDC_STATUS_NOT_COMPLETED);
          admin.setReqStatus(CmrConstants.REQUEST_STATUS.PPN.toString());
          RequestUtils.clearClaimDetails(admin);
          admin.setLockInd("N");
        } else if (rdcProcessStatusMsgs.contains(CmrConstants.RDC_STATUS_ABORTED)) {
          admin.setReqStatus(CmrConstants.REQUEST_STATUS.PPN.toString());
          admin.setRdcProcessingStatus(
              processingStatus.equals(CmrConstants.RDC_STATUS_ABORTED) ? CmrConstants.RDC_STATUS_NOT_COMPLETED : CmrConstants.RDC_STATUS_ABORTED);
          RequestUtils.clearClaimDetails(admin);
          admin.setLockInd("N");
        } else if (rdcProcessStatusMsgs.contains(CmrConstants.RDC_STATUS_COMPLETED_WITH_WARNINGS)) {
          admin.setRdcProcessingStatus(CmrConstants.RDC_STATUS_COMPLETED_WITH_WARNINGS);
          admin.setLockInd("N");
        } else {
          admin.setRdcProcessingStatus(CmrConstants.RDC_STATUS_COMPLETED);
          admin.setProcessedFlag("Y"); // set request status to processed
          admin.setLockInd("N");
        }

        String rdcProcessingMsg = null;
        if ("N".equals(admin.getRdcProcessingStatus()) || "A".equals(admin.getRdcProcessingStatus())) {
          rdcProcessingMsg = "Some errors occurred during processing. Please check request's comment log for details.";
        } else {
          rdcProcessingMsg = "RDc Processing has been completed. Please check request's comment log for details.";
        }

        admin.setRdcProcessingTs(SystemUtil.getCurrentTimestamp());
        admin.setRdcProcessingMsg(rdcProcessingMsg.toString().trim());

        if (!"A".equals(admin.getRdcProcessingStatus()) && !"N".equals(admin.getRdcProcessingStatus())) {
          admin.setReqStatus("COM");
        }
        updateEntity(admin, entityManager);

        if ("N".equals(admin.getRdcProcessingStatus()) || "A".equals(admin.getRdcProcessingStatus())) {
          RequestUtils.createWorkflowHistoryFromBatch(entityManager, BATCH_USER_ID, admin,
              "Some errors occurred during RDc processing. Please check request's comment log for details.", ACTION_RDC_UPDATE, null, null,
              "COM".equals(admin.getReqStatus()));
        } else {
          RequestUtils.createWorkflowHistoryFromBatch(entityManager, BATCH_USER_ID, admin,
              "RDc  Processing has been completed. Please check request's comment log for details.", ACTION_RDC_UPDATE, null, null,
              "COM".equals(admin.getReqStatus()));
        }

        partialCommit(entityManager);
        LOG.debug(
            "Request ID " + admin.getId().getReqId() + " Status: " + admin.getRdcProcessingStatus() + " Message: " + admin.getRdcProcessingMsg());
        // *** END OF FIX
      } else {
        LOG.error("*****There are no mass update requests for RDC processing.*****");
      }
    } catch (Exception e) {
      LOG.error("Error in processing Mass Update Request " + admin.getId().getReqId(), e);
      addError("Mass Update Request " + admin.getId().getReqId() + " Error: " + e.getMessage());
    }
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

  public void monitorCreqcmrMassUpd(EntityManager entityManager, List<Long> idList)
      throws JsonGenerationException, JsonMappingException, IOException, Exception {
    LOG.info("Initializing Country Map..");
    LandedCountryMap.init(entityManager);
    Data data = null;
    ProcessRequest request = null;
    for (Long reqId : idList) {
      AdminPK pk = new AdminPK();
      pk.setReqId(reqId);
      Admin admin = entityManager.find(Admin.class, pk);

      try {

        this.cmrObjects = prepareRequest(entityManager, admin);
        data = this.cmrObjects.getData();

        request = new ProcessRequest();
        request.setCmrNo(data.getCmrNo());
        request.setMandt(SystemConfiguration.getValue("MANDT"));
        request.setReqId(admin.getId().getReqId());
        request.setReqType(admin.getReqType());
        request.setUserId(BATCH_USER_ID);

        processMassUpdateRequest(entityManager, request, admin, data);

        if (CmrConstants.RDC_STATUS_ABORTED.equalsIgnoreCase(admin.getRdcProcessingStatus())
            || CmrConstants.RDC_STATUS_NOT_COMPLETED.equalsIgnoreCase(admin.getRdcProcessingStatus())) {
          admin.setReqStatus("PPN");
          RequestUtils.clearClaimDetails(admin);
          admin.setLockInd("N");
          admin.setProcessedFlag("E"); // set request status to error.
          createHistory(entityManager, "Sending back to processor due to error on RDC processing", "PPN", "RDC Processing", admin.getId().getReqId());
          LOG.debug("Sending back to processor due to error on RDC processing , request status=" + admin.getReqStatus());
        } else if ((CmrConstants.RDC_STATUS_COMPLETED.equalsIgnoreCase(admin.getRdcProcessingStatus())
            || CmrConstants.RDC_STATUS_COMPLETED_WITH_WARNINGS.equalsIgnoreCase(admin.getRdcProcessingStatus()))
            && CmrConstants.REQ_TYPE_CREATE.equals(admin.getReqType())) {
          admin.setReqStatus("COM");
          admin.setProcessedFlag("Y"); // set request status to processed
          createHistory(entityManager, "Request processing Completed Successfully", "COM", "RDC Processing", admin.getId().getReqId());
          LOG.debug("Request processing Completed Successfully , request status=" + admin.getReqStatus());

        }
        partialCommit(entityManager);
      } catch (Exception e) {
        partialRollback(entityManager);
        LOG.error("Unexpected error occurred during processing of Request " + reqId, e);
        processError(entityManager, admin, e.getMessage());
      }
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

    String sql = ExternalizedQuery.getSql("AT.GET.ADDR");
    // get the address order for US
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

  public String[] getIerpAddressOrder() {
    return ADDRESS_ORDER;
  }

  @Override
  protected boolean useServicesConnections() {
    return true;
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

    createHistory(entityManager, "RDC processing started.", "PCR", "Claim", admin.getId().getReqId());
    createComment(entityManager, "RDC processing started.", admin.getId().getReqId());

    partialCommit(entityManager);
  }

  @Override
  public boolean flushOnCommitOnly() {
    return true;
  }
}