/**
 * 
 */
package com.ibm.cmr.create.batch.service;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;

import com.ibm.cio.cmr.create.entity.NotifyReq;
import com.ibm.cio.cmr.create.entity.NotifyReqPK;
import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.AdminPK;
import com.ibm.cio.cmr.request.entity.CompoundEntity;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.entity.MassCreate;
import com.ibm.cio.cmr.request.entity.MassCreateData;
import com.ibm.cio.cmr.request.entity.ReqCmtLog;
import com.ibm.cio.cmr.request.entity.ReqCmtLogPK;
import com.ibm.cio.cmr.request.entity.WfHist;
import com.ibm.cio.cmr.request.entity.WfHistPK;
import com.ibm.cio.cmr.request.entity.listeners.ChangeLogListener;
import com.ibm.cio.cmr.request.model.requestentry.MassCreateBatchEmailModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.util.RequestUtils;
import com.ibm.cio.cmr.request.util.SystemLocation;
import com.ibm.cio.cmr.request.util.SystemParameters;
import com.ibm.cio.cmr.request.util.SystemUtil;
import com.ibm.cmr.create.batch.model.CmrServiceInput;
import com.ibm.cmr.create.batch.model.NotifyReqModel;
import com.ibm.cmr.create.batch.util.BatchUtil;
import com.ibm.cmr.create.batch.util.USCMRNumGen;
import com.ibm.cmr.create.batch.util.masscreate.WorkerThreadFactory;
import com.ibm.cmr.create.batch.util.worker.impl.USMassCreateMultiWorker;

/**
 * @author JeffZAMORA
 *
 */
public class MassCreateProcessMultiService extends MultiThreadedBatchService<String> {

  private static final Logger LOG = Logger.getLogger(MassCreateProcessMultiService.class);

  @Override
  protected Queue<String> getRequestsToProcess(EntityManager entityManager) {
    LinkedList<String> toProcess = new LinkedList<>();

    // search the notify requests from transconn
    String sql = ExternalizedQuery.getSql("BATCH.MONITOR_TRANSCONN_MASSPROCESS");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("NOTIFIED_IND", CmrConstants.NOTIFY_IND_YES);

    List<NotifyReq> notifyReqList = query.getResults(NotifyReq.class);
    LOG.debug("Size of notify list : " + notifyReqList.size());

    for (NotifyReq notifyReq : notifyReqList) {
      toProcess.add("N" + notifyReq.getId().getNotifyId());
    }

    sql = ExternalizedQuery.getSql("BATCH.MONITOR_ABORTED_REC");
    query = new PreparedQuery(entityManager, sql);
    query.append("and a.REQ_TYPE = 'N'");
    List<Admin> abortedReqList = query.getResults(Admin.class);
    LOG.debug("Size of abortedRecList : " + abortedReqList.size());

    for (Admin admin : abortedReqList) {
      toProcess.add("R" + admin.getId().getReqId());
    }

    return toProcess;
  }

  @Override
  public Boolean executeBatchForRequests(EntityManager entityManager, List<String> requests) throws Exception {
    List<Long> notifyReqList = new ArrayList<Long>();
    List<Long> abortedReqList = new ArrayList<Long>();

    for (String id : requests) {
      if (id.startsWith("N")) {
        notifyReqList.add(Long.parseLong(id.substring(1)));
      } else if (id.startsWith("R")) {
        abortedReqList.add(Long.parseLong(id.substring(1)));
      }
    }

    ChangeLogListener.setUser(BATCH_USER_ID);

    monitorTransconn(entityManager, notifyReqList);

    monitorAbortedRecords(entityManager, abortedReqList);

    return true;
  }

  /**
   * Monitor records from the TRANSCONN.NOTIFY_REQ
   * 
   * @param entityManager
   * @throws JsonGenerationException
   * @throws JsonMappingException
   * @throws IOException
   * @throws Exception
   */
  public void monitorTransconn(EntityManager em, List<Long> idList) throws JsonGenerationException, JsonMappingException, IOException, Exception {

    NotifyReqPK pk = null;
    NotifyReq notifyReq = null;
    for (Long id : idList) {
      try {
        trackRequestLogs(id);
        pk = new NotifyReqPK();
        pk.setNotifyId(id);
        notifyReq = em.find(NotifyReq.class, pk);

        LOG.info("Processing Notify Req " + notifyReq.getId().getNotifyId() + " [Request ID: " + notifyReq.getReqId() + "]");
        NotifyReqModel notifyReqModel = new NotifyReqModel();
        copyValuesFromEntity(notifyReq, notifyReqModel);

        // create a entry in request's comment log re_cmt_log table
        if (null != notifyReq.getCmtLogMsg() && !notifyReq.getCmtLogMsg().isEmpty()) {
          createCommentLog(em, notifyReqModel);
        }
        // update the NOTIFIED_IND of NOTIFY_REQ Table for each record processed
        notifyReq.setNotifiedInd(CmrConstants.NOTIFY_IND_YES);
        updateEntity(notifyReq, em);

        // send mail and get the input params for create cmr service
        String sqlMail = "BATCH.GET_MAIL_CONTENTS";
        String mapping = Admin.BATCH_SERVICE_MAPPING;
        PreparedQuery queryMail = new PreparedQuery(em, ExternalizedQuery.getSql(sqlMail));
        queryMail.setParameter("REQ_ID", notifyReq.getReqId());
        queryMail.setParameter("REQ_STATUS", notifyReq.getReqStatus());
        queryMail.setParameter("CHANGED_BY_ID", notifyReq.getChangedById());
        List<CompoundEntity> rs = queryMail.getCompundResults(1, Admin.class, mapping);
        Admin admin = null;
        Data data = null;
        WfHist wfHist = null;

        for (CompoundEntity entity : rs) {
          admin = entity.getEntity(Admin.class);
          data = entity.getEntity(Data.class);
          wfHist = entity.getEntity(WfHist.class);
        }
        RequestUtils.sendEmailNotifications(em, admin, wfHist);
        partialCommit(em);
        CmrServiceInput cmrServiceInput = getReqParam(em, notifyReqModel.getReqId(), admin.getReqType(), data.getCmrNo());
        cmrServiceInput.setCmrIssuingCntry(data.getCmrIssuingCntry());
        boolean isMassCreateRecord = false;

        if (null != admin.getReqType() && !"".equalsIgnoreCase(admin.getReqType())
            && CmrConstants.REQ_TYPE_MASS_CREATE.equalsIgnoreCase(admin.getReqType())) {
          isMassCreateRecord = true;
        }

        String batchAction = "MonitorTransconn";
        if (isMassCreateRecord) {
          LOG.debug("Processing Mass Create Service for Request ID " + notifyReq.getReqId());
          String result = processMassCreateService(em, admin, cmrServiceInput, notifyReq.getIterationId(), batchAction,
              admin.getRdcProcessingStatus(), wfHist);
          LOG.debug("Result from Create Service: '" + result + "'");
        }

        partialCommit(em);

      } catch (Exception e) {
        LOG.error("Error in processing TransConn Record " + notifyReq.getId().getNotifyId() + " for Request ID " + notifyReq.getReqId() + " ["
            + e.getMessage() + "]");
      }
    }
    resetThreadName();
  }

  /**
   * Monitor aborted records
   * 
   * @param entityManager
   * @throws JsonGenerationException
   * @throws JsonMappingException
   * @throws IOException
   * @throws Exception
   */
  public void monitorAbortedRecords(EntityManager em, List<Long> idList)
      throws JsonGenerationException, JsonMappingException, IOException, Exception {

    String cmrno = "";
    String cmrIssuingCountry = "";
    LOG.debug("Size of abortedRecList : " + idList.size());

    boolean isMassRecordCreate = false;

    AdminPK pk = null;
    Admin manualRec1 = null;
    List<Admin> manualRecList = new ArrayList<Admin>();
    for (Long id : idList) {
      pk = new AdminPK();
      pk.setReqId(id);
      manualRec1 = em.find(Admin.class, pk);
      if (manualRec1 != null) {
        manualRecList.add(manualRec1);
      }
    }
    lockAdminRecordsForProcessing(manualRecList, em);

    for (Admin manualRec : manualRecList) {
      trackRequestLogs(manualRec.getId().getReqId());
      try {
        String batchAction = "MonitorAbortedRecords";
        LOG.info("Processing Aborted Record " + manualRec.getId().getReqId() + " [Request ID: " + manualRec.getId().getReqId() + "]");
        String sql = ExternalizedQuery.getSql("BATCH.GET_DATA");
        PreparedQuery query = new PreparedQuery(em, sql);
        query.setParameter("REQ_ID", manualRec.getId().getReqId());
        List<Data> manualRecDataList = query.getResults(Data.class);

        for (Data manualRecData : manualRecDataList) {
          cmrno = manualRecData.getCmrNo();
          cmrIssuingCountry = manualRecData.getCmrIssuingCntry();
        }

        if (null != manualRec.getReqType() && !"".equalsIgnoreCase(manualRec.getReqType())
            && CmrConstants.REQ_TYPE_MASS_CREATE.equalsIgnoreCase(manualRec.getReqType())) {
          isMassRecordCreate = true;
        }

        if (isMassRecordCreate) {
          // get input params for create cmr service
          CmrServiceInput cmrServiceInput = getReqParam(em, manualRec.getId().getReqId(), manualRec.getReqType(), cmrno);
          cmrServiceInput.setCmrIssuingCntry(cmrIssuingCountry);

          String result = processMassCreateService(em, manualRec, cmrServiceInput, manualRec.getIterationId(), batchAction,
              manualRec.getRdcProcessingStatus(), null);
          LOG.debug("Result from Create Service: '" + result + "'");

        }

      } catch (Exception e) {
        LOG.error("Error in processing Aborted Record " + manualRec.getId().getReqId() + " for Request ID " + manualRec.getId().getReqId() + " ["
            + e.getMessage() + "]");
      }

    }
    resetThreadName();

  }

  private void lockAdminRecordsForProcessing(List<Admin> forProcessing, EntityManager entityManager) {
    for (Admin admin : forProcessing) {
      if (CmrConstants.REQ_TYPE_MASS_CREATE.equals(admin.getReqType())) {
        admin.setRdcProcessingStatus(CmrConstants.RDC_STATUS_WAITING);
        admin.setRdcProcessingMsg("Waiting for completion.");
        admin.setRdcProcessingTs(SystemUtil.getCurrentTimestamp());
        updateEntity(admin, entityManager);
        LOG.debug("Locking Request ID " + admin.getId().getReqId() + " for processing.");
      }
    }
    partialCommit(entityManager);

  }

  /**
   * Create the comment log entry for the request in Notify_Req
   * 
   * @param service
   * @param entityManager
   * @param notifyReqModel
   * @throws CmrException
   * @throws SQLException
   */
  public void createCommentLog(EntityManager em, NotifyReqModel notifyReqModel) throws CmrException, SQLException {
    LOG.info("Creating Comment Log for  Notify Req " + notifyReqModel.getNotifyId() + " [Request ID: " + notifyReqModel.getReqId() + "]");
    ReqCmtLog reqCmtLog = new ReqCmtLog();
    ReqCmtLogPK reqCmtLogpk = new ReqCmtLogPK();
    reqCmtLogpk.setCmtId(SystemUtil.getNextID(em, SystemConfiguration.getValue("MANDT"), "CMT_ID"));
    reqCmtLog.setId(reqCmtLogpk);
    reqCmtLog.setReqId(notifyReqModel.getReqId());
    reqCmtLog.setCmt(notifyReqModel.getCmtLogMsg());
    // save cmtlockedIn as Y default for current realese
    reqCmtLog.setCmtLockedIn(CmrConstants.CMT_LOCK_IND_YES);
    reqCmtLog.setCreateById(notifyReqModel.getChangedById());
    reqCmtLog.setCreateByNm(notifyReqModel.getChangedById());
    // set createTs as current timestamp and updateTs same as CreateTs
    reqCmtLog.setCreateTs(SystemUtil.getCurrentTimestamp());
    reqCmtLog.setUpdateTs(reqCmtLog.getCreateTs());
    createEntity(reqCmtLog, em);
  }

  public String processMassCreateService(EntityManager em, Admin admin, CmrServiceInput cmrServiceInput, int itrId, String batchAction,
      String currentRDCProcStat, WfHist wfHist) throws JsonGenerationException, JsonMappingException, IOException, Exception {

    long reqId = admin.getId().getReqId();
    String resultCode = "";
    // get the params for input to the create cmr service
    List<MassCreateBatchEmailModel> ufailList = new ArrayList<MassCreateBatchEmailModel>();

    String massCreteSql = "BATCH.MASS_CREATE_DATA_RECORDS";
    String massUfailsql = "BATCH.MASS_CREATE_DATA_UFAIL_RECORDS";
    String map = Admin.BATCH_CREATE_DATA_MAPPING;
    PreparedQuery massCreatequery = new PreparedQuery(em, ExternalizedQuery.getSql(massCreteSql));
    massCreatequery.setParameter("REQ_ID", reqId);
    massCreatequery.setParameter("ITERATION_ID", itrId);
    List<CompoundEntity> resultsMain = massCreatequery.getCompundResults(Admin.class, map);
    MassCreate mass_create = null;
    MassCreateData massCrtData = null;

    List<String> resultCodes = new ArrayList<String>();
    Map<String, List<String>> backing = new HashMap<String, List<String>>();
    Map<String, List<String>> cmrNoSapNoMap = Collections.synchronizedMap(backing);

    int threads = 5;
    String mcThreads = SystemParameters.getString("PROCESS.THREAD.COUNT");
    if (!StringUtils.isBlank(mcThreads) && StringUtils.isNumeric(mcThreads)) {
      threads = Integer.parseInt(mcThreads);
    } else {
      String threadCount = BatchUtil.getProperty("multithreaded.threadCount");
      if (threadCount != null && StringUtils.isNumeric(threadCount)) {
        threads = Integer.parseInt(threadCount);
      }
    }
    LOG.debug("Worker threads to use: " + threads);
    LOG.debug("Starting processing mass create at " + new Date());
    LOG.debug("Number of records found: " + resultsMain.size());

    List<USMassCreateMultiWorker> workers = new ArrayList<USMassCreateMultiWorker>();

    LOG.debug(" - Processing " + resultsMain.size() + " subrecords...");
    ExecutorService executor = Executors.newFixedThreadPool(threads, new WorkerThreadFactory("MCWorker-" + reqId));
    for (CompoundEntity entity : resultsMain) {
      mass_create = entity.getEntity(MassCreate.class);
      massCrtData = entity.getEntity(MassCreateData.class);
      // if (USCMRNumGen.cmrNumMapMassCrt == null ||
      // USCMRNumGen.cmrNumMapMassCrt.isEmpty()) {
      // LOG.info("there is no CMR number stored for Mass Create in cache, so
      // init...");
      // USCMRNumGen.initMassCrt(em);
      // }

      String issuingCntrySql = "BATCH.GET_DATA";
      PreparedQuery dataQuery = new PreparedQuery(em, ExternalizedQuery.getSql(issuingCntrySql));
      dataQuery.setParameter("REQ_ID", reqId);
      Data data = dataQuery.getSingleResult(Data.class);
      // temp switch for US masscreate
      String procType = null;
      if (SystemLocation.UNITED_STATES.equals(data.getCmrIssuingCntry())) {

        String sql = "select PROCESSING_TYP from creqcmr.SUPP_CNTRY where CNTRY_CD='897'";
        Query q = em.createNativeQuery(sql);
        procType = (String) q.getSingleResult();
        if (procType.equals("US") && CmrConstants.REQ_TYPE_MASS_CREATE.equalsIgnoreCase(cmrServiceInput.getInputReqType())) {
          boolean isPoaFed = "4".equals(massCrtData.getCustTyp()) || "6".equals(massCrtData.getCustTyp());
          boolean isMainCustNm1 = false;

          if (massCrtData.getCustNm1() != null) {
            isMainCustNm1 = massCrtData.getCustNm1().trim().toUpperCase().startsWith("A");
          }

          String cmrType = "COMM";
          if (isPoaFed) {
            cmrType = "POA";
          } else if (isMainCustNm1) {
            cmrType = "MAIN";
          }
          // temp try get number, will remove when generate method done
          String cmrNum = USCMRNumGen.genCMRNumMassCrt(em, cmrType);
          massCrtData.setCmrNo(cmrNum);
          mass_create.setCmrNo(cmrNum);
          // CREATCMR-6987
          if ("KYN".equalsIgnoreCase(massCrtData.getCustSubGrp())
              || ("BYMODEL".equalsIgnoreCase(massCrtData.getCustSubGrp()) && "KYN".equalsIgnoreCase(massCrtData.getRestrictTo()))) {
            massCrtData.setCustNm1("KYNDRYL INC");
            massCrtData.setCustNm2("");
            // CREATCMR-7173
            massCrtData.setIsuCd("5K");
          }
          updateEntity(mass_create, em);
          updateEntity(massCrtData, em);
          partialCommit(em);
        } else if (procType.equals("TC")) {
          setCmrNoOnMassCrtData(em, admin.getId().getReqId(), mass_create.getCmrNo(), mass_create.getId().getSeqNo());
        }

      }

      USMassCreateMultiWorker worker = new USMassCreateMultiWorker(this, admin, mass_create, cmrServiceInput, cmrNoSapNoMap,
          data.getCmrIssuingCntry());
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

    LOG.debug("Mass create processing finished at " + new Date());

    for (USMassCreateMultiWorker worker : workers) {
      if (worker != null) {
        if (worker.isError()) {
          LOG.error("Error in processing mass create ID " + reqId + ": " + worker.getErrorMsg());
          throw new Exception(worker.getErrorMsg());
        } else {
          resultCodes.addAll(worker.getStatusCodes());
        }
      }
    }

    // update the Admin table for all the update responses
    Admin adminEntity = admin;
    String currProcStatus = adminEntity.getRdcProcessingStatus();
    Admin adminEntityWf = new Admin();
    LOG.debug("Updating Admin table. Current Status: " + currProcStatus);

    String overallStatus = null;
    String statusMessage = null;
    adminEntity.setReqStatus("PPN");
    if ((resultCodes.contains(CmrConstants.RDC_STATUS_NOT_COMPLETED) || resultCodes.contains(CmrConstants.RDC_STATUS_ABORTED))
        && (resultCodes.contains(CmrConstants.RDC_STATUS_COMPLETED) || resultCodes.contains(CmrConstants.RDC_STATUS_COMPLETED))) {
      overallStatus = CmrConstants.RDC_STATUS_COMPLETED_WITH_WARNINGS;
      statusMessage = "One or more records failed in RDc processing.";
    } else if ((!resultCodes.contains(CmrConstants.RDC_STATUS_NOT_COMPLETED) && !resultCodes.contains(CmrConstants.RDC_STATUS_ABORTED))
        && (resultCodes.contains(CmrConstants.RDC_STATUS_COMPLETED) || resultCodes.contains(CmrConstants.RDC_STATUS_COMPLETED)
            || resultCodes.contains(CmrConstants.RDC_STATUS_COMPLETED_WITH_WARNINGS))) {
      overallStatus = CmrConstants.RDC_STATUS_COMPLETED;
      adminEntity.setReqStatus("COM");
      statusMessage = "All records processed successfully.";
    } else if ((!resultCodes.contains(CmrConstants.RDC_STATUS_COMPLETED) && !resultCodes.contains(CmrConstants.RDC_STATUS_COMPLETED_WITH_WARNINGS))) {
      overallStatus = CmrConstants.RDC_STATUS_NOT_COMPLETED;
      statusMessage = "All records failed processing.";
    } else if ((resultCodes.contains(CmrConstants.RDC_STATUS_NOT_COMPLETED) || resultCodes.contains(CmrConstants.RDC_STATUS_ABORTED))) {
      overallStatus = CmrConstants.RDC_STATUS_NOT_COMPLETED;
      statusMessage = "All records failed processing.";
    }
    LOG.debug("Overall Processing Status: " + overallStatus);

    adminEntity.setRdcProcessingStatus(overallStatus);
    adminEntity.setRdcProcessingMsg(statusMessage);
    adminEntity.setRdcProcessingTs(SystemUtil.getCurrentTimestamp());
    updateEntity(adminEntity, em);
    adminEntityWf = adminEntity;

    // create comment log and workflow history entries for create type of
    // request
    String actionWf = "System Action:RDc Mass Create";
    String comment = statusMessage + "\n" + "Please check summary notification mail and/or request summary for details.";
    // createCommentLogAfterProcess(em,
    // SystemConfiguration.getValue("BATCH_USERID"), reqId, comment);
    RequestUtils.createCommentLogFromBatch(em, BATCH_USER_ID, adminEntity.getId().getReqId(), comment);
    RequestUtils.createWorkflowHistoryFromBatch(em, BATCH_USER_ID, adminEntityWf, comment, actionWf, null, null, true);
    createWfHistory(em, reqId, adminEntityWf.getReqStatus(), SystemConfiguration.getValue("BATCH_USERID"), comment, actionWf, adminEntityWf);

    partialCommit(em);

    if (resultCode != CmrConstants.RDC_STATUS_ABORTED) {

      sendMasscreateCMRNos(em, reqId, itrId, cmrNoSapNoMap);

      MassCreateBatchEmailModel ufailrecords = null;
      MassCreateData mass_create_data = null;
      String statusfail = "UFAIL";
      PreparedQuery ufailquery = new PreparedQuery(em, ExternalizedQuery.getSql(massUfailsql));
      ufailquery.setParameter("REQ_ID", reqId);
      ufailquery.setParameter("ITERATION_ID", itrId);
      ufailquery.setParameter("STATUS", statusfail);
      List<CompoundEntity> resultfail = ufailquery.getCompundResults(Admin.class, map);

      for (CompoundEntity entity : resultfail) {
        ufailrecords = new MassCreateBatchEmailModel();
        mass_create = entity.getEntity(MassCreate.class);
        mass_create_data = entity.getEntity(MassCreateData.class);
        ufailrecords.setCmrNo(mass_create.getCmrNo() != null ? mass_create.getCmrNo() : "");
        ufailrecords.setErrorMsg(mass_create.getErrorTxt() != null ? mass_create.getErrorTxt() : "");
        ufailrecords.setCustName(mass_create_data.getCustNm1() + (mass_create_data.getCustNm2() != null ? mass_create_data.getCustNm2() : ""));
        ufailList.add(ufailrecords);
      }

      LOG.info("UFAIL records size : " + ufailList.size());
      if (ufailList.size() > 0) {
        RequestUtils.sendBatchEmailNotifications(em, ufailList, wfHist);
      }
    }

    return resultCode;
  }

  private void sendMasscreateCMRNos(EntityManager em, long reqId, int itrId, Map<String, List<String>> cmrNoSapNoMap) throws Exception {
    String statusfail = "UFAIL";
    PreparedQuery ufailquery = new PreparedQuery(em, ExternalizedQuery.getSql("BATCH.MASS_CREATE_DATA_CMR_RECORDS"));
    ufailquery.setParameter("REQ_ID", reqId);
    ufailquery.setParameter("ITERATION_ID", itrId);
    ufailquery.setParameter("STATUS", statusfail);
    String map = Admin.BATCH_CMR_DATA_MAPPING;
    List<CompoundEntity> resultfail = ufailquery.getCompundResults(Admin.class, map);

    MassCreate massCreate = null;
    MassCreateData massCreateData = null;
    Admin adminData = null;
    List<MassCreateBatchEmailModel> cmrList = new ArrayList<MassCreateBatchEmailModel>();
    MassCreateBatchEmailModel record = null;
    for (CompoundEntity entity : resultfail) {
      massCreate = entity.getEntity(MassCreate.class);
      record = new MassCreateBatchEmailModel();
      adminData = entity.getEntity(Admin.class);
      massCreateData = entity.getEntity(MassCreateData.class);

      record.setCmrNo(massCreate.getCmrNo());
      record.setCustName(massCreateData.getCustNm1() + (massCreateData.getCustNm2() != null ? massCreateData.getCustNm2() : ""));
      record.setRowNo(massCreate.getId().getSeqNo());
      record.setErrorMsg(massCreate.getErrorTxt());
      StringBuilder sapNoAll = new StringBuilder();
      List<String> sapNos = cmrNoSapNoMap.get(record.getCmrNo());
      if (sapNos != null) {
        for (String sapNo : sapNos) {
          sapNoAll.append(sapNoAll.length() > 0 ? "<br>" : "");
          sapNoAll.append(sapNo);
        }
      }
      record.setSapNo(sapNoAll.toString());
      cmrList.add(record);
    }

    LOG.info("CMRs records size : " + cmrList.size());
    if (cmrList.size() > 0) {
      RequestUtils.sendMassCreateCMRNotifications(em, cmrList, adminData);
    }
  }

  /**
   * Get values of the input parameters for the createCMRService Request
   * 
   * @param entityManager
   * @param reqId
   * @param reqType
   * @param cmrno
   */
  public static CmrServiceInput getReqParam(EntityManager em, long reqId, String reqType, String cmrno) {
    String cmrNo = ((cmrno) != null && (cmrno).trim().length() > 0) ? (cmrno) : "";
    String requestType = ((reqType) != null && (reqType).trim().length() > 0) ? (reqType) : "";
    long requestId = reqId;
    CmrServiceInput cmrServiceInput = new CmrServiceInput();

    cmrServiceInput.setInputMandt(SystemConfiguration.getValue("MANDT"));
    cmrServiceInput.setInputReqId(requestId);
    cmrServiceInput.setInputReqType(requestType);
    cmrServiceInput.setInputCmrNo(cmrNo);
    cmrServiceInput.setInputUserId(SystemConfiguration.getValue("BATCH_USERID"));

    return cmrServiceInput;
  }

  private void setCmrNoOnMassCrtData(EntityManager entityManager, long reqId, String cmrNo, int seqNo) {
    PreparedQuery query = new PreparedQuery(entityManager, ExternalizedQuery.getSql("US.SETCMRNO"));
    query.setParameter("REQ_ID", reqId);
    query.setParameter("CMR_NO", cmrNo);
    query.setParameter("SEQ_NO", seqNo);
    query.executeSql();
  }

  /**
   * Create the createWfHist record
   * 
   * @param service
   * @param entityManager
   * @param reqId
   * @param reqStatus
   * @param lockBy
   * @param cmt
   * @param action
   * @param action
   * @param adminEntityWf
   * @throws CmrException
   * @throws SQLException
   */
  public void createWfHistory(EntityManager em, long reqId, String reqStatus, String lockBy, String cmt, String action, Admin adminEntityWf)
      throws CmrException, SQLException {
    LOG.info("Creating wf_hist record  Req  for [Request ID: " + reqId + "]");
    WfHist hist = new WfHist();
    WfHistPK histpk = new WfHistPK();
    histpk.setWfId(SystemUtil.getNextID(em, SystemConfiguration.getValue("MANDT"), "WF_ID"));
    hist.setId(histpk);
    hist.setCmt(cmt);
    hist.setReqStatus(reqStatus);
    hist.setCreateById(lockBy);
    hist.setCreateByNm(lockBy);
    hist.setCreateTs(SystemUtil.getCurrentTimestamp());
    hist.setCompleteTs(SystemUtil.getCurrentTimestamp());
    hist.setReqStatusAct(action);
    hist.setReqId(reqId);
    createEntity(hist, em);

    RequestUtils.sendEmailNotifications(em, adminEntityWf, hist);

  }

  @Override
  public boolean flushOnCommitOnly() {
    return true;
  }

  @Override
  protected String getThreadName() {
    return "MassCreateMulti";
  }

  @Override
  public boolean isTransactional() {
    return true;
  }

  @Override
  protected boolean useServicesConnections() {
    return true;
  }

  @Override
  protected boolean terminateOnLongExecution() {
    return true;
  }

  @Override
  protected int getTerminatorWaitTime() {
    return 180;
  }

  @Override
  protected boolean hasPreProcess() {
    return true;
  }

  @Override
  protected void preProcess(EntityManager entityManager) {
    USCMRNumGen.initMassCrt(entityManager);
  }

}
