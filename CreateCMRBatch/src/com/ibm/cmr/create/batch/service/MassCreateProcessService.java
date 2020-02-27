package com.ibm.cmr.create.batch.service;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;

import com.ibm.cio.cmr.create.entity.NotifyReq;
import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.CompoundEntity;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.entity.MassCreate;
import com.ibm.cio.cmr.request.entity.MassCreateAddr;
import com.ibm.cio.cmr.request.entity.MassCreateData;
import com.ibm.cio.cmr.request.entity.ReqCmtLog;
import com.ibm.cio.cmr.request.entity.ReqCmtLogPK;
import com.ibm.cio.cmr.request.entity.WfHist;
import com.ibm.cio.cmr.request.entity.WfHistPK;
import com.ibm.cio.cmr.request.model.requestentry.MassCreateBatchEmailModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.util.RequestUtils;
import com.ibm.cio.cmr.request.util.SystemUtil;
import com.ibm.cmr.create.batch.model.CmrServiceInput;
import com.ibm.cmr.create.batch.model.NotifyReqModel;
import com.ibm.cmr.create.batch.util.DebugUtil;
import com.ibm.cmr.services.client.CmrServicesFactory;
import com.ibm.cmr.services.client.ProcessClient;
import com.ibm.cmr.services.client.process.ProcessRequest;
import com.ibm.cmr.services.client.process.ProcessResponse;
import com.ibm.cmr.services.client.process.RDcRecord;

/**
 * @author Rochelle Salazar
 * 
 */
public class MassCreateProcessService extends BaseBatchService {

  private static final String BATCH_SERVICES_URL = SystemConfiguration.getValue("BATCH_SERVICES_URL");
  private ProcessClient serviceClient;

  public MassCreateProcessService() {
    super();
  }

  @Override
  protected Boolean executeBatch(EntityManager entityManager) throws Exception {
    try {

      initClient();

      LOG.info("Processing TransConn records...");
      monitorTransconn(entityManager);

      LOG.info("Processing Aborted records (retry)...");
      monitorAbortedRecords(entityManager);

      return true;

    } catch (Exception e) {
      addError(e);
      return false;
    }
  }

  @Override
  protected boolean isTransactional() {
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
  public void monitorTransconn(EntityManager em) throws JsonGenerationException, JsonMappingException, IOException, Exception {

    // search the notify requests from transconn
    String sql = ExternalizedQuery.getSql("BATCH.MONITOR_TRANSCONN_MASSPROCESS");
    PreparedQuery query = new PreparedQuery(em, sql);
    query.setParameter("NOTIFIED_IND", CmrConstants.NOTIFY_IND_YES);

    List<NotifyReq> notifyList = query.getResults(NotifyReq.class);
    LOG.debug("Size of notify list : " + notifyList.size());

    for (NotifyReq notifyReq : notifyList) {
      try {
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
          String result = processMassCreateService(em, notifyReq.getReqId(), cmrServiceInput, notifyReq.getIterationId(), batchAction,
              admin.getRdcProcessingStatus(), wfHist);
          LOG.debug("Result from Create Service: '" + result + "'");
        }

        partialCommit(em);

      } catch (Exception e) {
        LOG.error("Error in processing TransConn Record " + notifyReq.getId().getNotifyId() + " for Request ID " + notifyReq.getReqId() + " ["
            + e.getMessage() + "]");
      }
    }
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
  public void monitorAbortedRecords(EntityManager em) throws JsonGenerationException, JsonMappingException, IOException, Exception {

    String cmrno = "";
    String cmrIssuingCountry = "";
    // search the manual processed records from Admin table
    String sql = ExternalizedQuery.getSql("BATCH.MONITOR_ABORTED_REC");
    PreparedQuery query = new PreparedQuery(em, sql);
    List<Admin> manualRecList = query.getResults(Admin.class);
    LOG.debug("Size of abortedRecList : " + manualRecList.size());

    boolean isMassRecordCreate = false;

    lockAdminRecordsForProcessing(manualRecList, em);

    for (Admin manualRec : manualRecList) {

      try {
        String batchAction = "MonitorAbortedRecords";
        LOG.info("Processing Aborted Record " + manualRec.getId().getReqId() + " [Request ID: " + manualRec.getId().getReqId() + "]");
        sql = ExternalizedQuery.getSql("BATCH.GET_DATA");
        query = new PreparedQuery(em, sql);
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

          String result = processMassCreateService(em, manualRec.getId().getReqId(), cmrServiceInput, manualRec.getIterationId(), batchAction,
              manualRec.getRdcProcessingStatus(), null);
          LOG.debug("Result from Create Service: '" + result + "'");

        }

      } catch (Exception e) {
        LOG.error("Error in processing Aborted Record " + manualRec.getId().getReqId() + " for Request ID " + manualRec.getId().getReqId() + " ["
            + e.getMessage() + "]");
      }

    }

  }

  public String processMassCreateService(EntityManager em, long reqId, CmrServiceInput cmrServiceInput, int itrId, String batchAction,
      String currentRDCProcStat, WfHist wfHist) throws JsonGenerationException, JsonMappingException, IOException, Exception {

    String resultCode = "";
    // get the params for input to the create cmr service
    List<MassCreateBatchEmailModel> ufailList = new ArrayList<MassCreateBatchEmailModel>();

    String massCreteSql = "BATCH.MASS_CREATE_DATA_RECORDS";
    String massUfailsql = "BATCH.MASS_CREATE_DATA_UFAIL_RECORDS";
    String map = Admin.BATCH_CREATE_DATA_MAPPING;
    PreparedQuery massCreatequery = new PreparedQuery(em, ExternalizedQuery.getSql(massCreteSql));
    massCreatequery.setParameter("REQ_ID", reqId);
    massCreatequery.setParameter("ITERATION_ID", itrId);
    List<CompoundEntity> results = massCreatequery.getCompundResults(Admin.class, map);
    MassCreate mass_create = null;

    List<String> resultCodes = new ArrayList<String>();
    Map<String, List<String>> cmrNoSapNoMap = new HashMap<String, List<String>>();

    for (CompoundEntity entity : results) {
      mass_create = entity.getEntity(MassCreate.class);

      ProcessRequest request = new ProcessRequest();
      request.setCmrNo(mass_create.getCmrNo());
      request.setMandt(cmrServiceInput.getInputMandt());
      request.setReqId(cmrServiceInput.getInputReqId());
      request.setReqType(CmrConstants.REQ_TYPE_CREATE);
      request.setUserId(cmrServiceInput.getInputUserId());

      if (!cmrNoSapNoMap.containsKey(mass_create.getCmrNo())) {
        cmrNoSapNoMap.put(mass_create.getCmrNo(), new ArrayList<String>());
      }

      LOG.info("Sending request to Process Service [Request ID: " + request.getReqId() + " CMR No: " + request.getCmrNo() + " Type: "
          + request.getReqType() + "]");

      // call the create cmr service
      LOG.trace("Request JSON:");
      if (LOG.isTraceEnabled()) {
        DebugUtil.printObjectAsJson(LOG, request);
      }

      ProcessResponse response = null;
      try {
        this.serviceClient.setReadTimeout(60 * 10 * 1000); // 10 mins
        response = this.serviceClient.executeAndWrap(ProcessClient.US_APP_ID, request, ProcessResponse.class);
      } catch (Exception e) {
        LOG.error("Error when connecting to the service.", e);
        response = new ProcessResponse();
        response.setStatus(CmrConstants.RDC_STATUS_ABORTED);
        response.setReqId(request.getReqId());
        response.setCmrNo(mass_create.getCmrNo());
        response.setMandt(request.getMandt());
        response.setMessage("Cannot connect to the service at the moment.");
      }

      resultCode = response.getStatus();
      LOG.trace("Response JSON:");
      if (LOG.isTraceEnabled()) {
        DebugUtil.printObjectAsJson(LOG, response);
      }

      if (!StringUtils.isBlank(resultCode) && !resultCodes.contains(resultCode)) {
        resultCodes.add(resultCode);
      }
      LOG.info("Response received from Process Service [Request ID: " + response.getReqId() + " CMR No: " + response.getCmrNo() + " Status: "
          + response.getStatus() + " Message: " + (response.getMessage() != null ? response.getMessage() : "-") + "]");

      // get the results from the service and process json response
      try {

        if (CmrConstants.RDC_STATUS_COMPLETED.equalsIgnoreCase(response.getStatus())
            || CmrConstants.RDC_STATUS_COMPLETED_WITH_WARNINGS.equalsIgnoreCase(response.getStatus())) {
          // get the update date from the RDC Records returned from the
          // service
          for (RDcRecord record : response.getRecords()) {
            // update MASS_CREATE_ADDR Table for the update requests
            PreparedQuery createAddrQry = new PreparedQuery(em, ExternalizedQuery.getSql("BATCH.GET_MASS_ADDR_ENTITY_CREATE_REQ"));
            createAddrQry.setParameter("REQ_ID", reqId);
            createAddrQry.setParameter("ITERATION_ID", itrId);
            createAddrQry.setParameter("ADDR_TYPE", "ZS01".equals(record.getAddressType()) || "ZI01".equals(record.getAddressType()) ? "ZS01"
                : "ZI01");
            createAddrQry.setParameter("SEQ_NO", mass_create.getId().getSeqNo());
            List<MassCreateAddr> cretAddrList = createAddrQry.getResults(MassCreateAddr.class);
            for (MassCreateAddr cretAddrEntity : cretAddrList) {
              cretAddrEntity.setSapNo(record.getSapNo());
              updateEntity(cretAddrEntity, em);
            }
            cmrNoSapNoMap.get(mass_create.getCmrNo()).add(record.getSapNo());
          }
        }

        // update MASS_CREATE table with the error txt and row status cd
        PreparedQuery cretMassAddrQry = new PreparedQuery(em, ExternalizedQuery.getSql("BATCH.GET_MASS_CREATE_ENTITY"));
        cretMassAddrQry.setParameter("REQ_ID", reqId);
        cretMassAddrQry.setParameter("ITERATION_ID", itrId);
        cretMassAddrQry.setParameter("CMR_NO", response.getCmrNo());
        List<MassCreate> createList = cretMassAddrQry.getResults(MassCreate.class);

        for (MassCreate massCretEntity : createList) {
          if (null != response.getStatus() && CmrConstants.RDC_STATUS_NOT_COMPLETED.equalsIgnoreCase((response.getStatus()))) {
            if (massCretEntity.getRowStatusCd().equalsIgnoreCase(CmrConstants.MASS_CREATE_ROW_STATUS_UPDATE_FAILE)) {
              massCretEntity.setErrorTxt(response.getMessage() + " The automatic update also failed.");
            } else {
              massCretEntity.setErrorTxt(response.getMessage());
              massCretEntity.setRowStatusCd("RDCER");
            }
          } else if (null != response.getStatus()
              && (CmrConstants.RDC_STATUS_COMPLETED.equalsIgnoreCase(response.getStatus()) || CmrConstants.RDC_STATUS_COMPLETED_WITH_WARNINGS
                  .equalsIgnoreCase(response.getStatus()))) {
            if (massCretEntity.getRowStatusCd().equalsIgnoreCase(CmrConstants.MASS_CREATE_ROW_STATUS_UPDATE_FAILE)) {
              massCretEntity.setErrorTxt("The automatic update failed.");
            } else {
              massCretEntity.setRowStatusCd("DONE");
            }
          }
          LOG.info("Mass Create Record Updated [Request ID: " + massCretEntity.getId().getParReqId() + " CMR_NO: " + mass_create.getCmrNo()
              + " SEQ No: " + massCretEntity.getId().getSeqNo() + "]");
          updateEntity(massCretEntity, em);
        }

        partialCommit(em);

      } catch (Exception e) {
        LOG.error("Error in processing Mass Create and Mass Create Addr Updates for Create Mass Request " + " [" + e.getMessage() + "]", e);
        throw e;
      }
    }

    // update the Admin table for all the update responses
    PreparedQuery adminQuery = new PreparedQuery(em, ExternalizedQuery.getSql("BATCH.GET_ADMIN_ENTITY"));
    adminQuery.setParameter("REQ_ID", reqId);
    Admin adminEntity = adminQuery.getSingleResult(Admin.class);
    String currProcStatus = adminEntity.getRdcProcessingStatus();
    Admin adminEntityWf = new Admin();
    LOG.debug("Updating Admin table. Current Status: " + currProcStatus);

    String overallStatus = null;
    String statusMessage = null;
    if ((resultCodes.contains(CmrConstants.RDC_STATUS_NOT_COMPLETED) || resultCodes.contains(CmrConstants.RDC_STATUS_ABORTED))
        && (resultCodes.contains(CmrConstants.RDC_STATUS_COMPLETED) || resultCodes.contains(CmrConstants.RDC_STATUS_COMPLETED))) {
      overallStatus = CmrConstants.RDC_STATUS_COMPLETED_WITH_WARNINGS;
      statusMessage = "One or more records failed in RDc processing.";
    } else if ((!resultCodes.contains(CmrConstants.RDC_STATUS_NOT_COMPLETED) && !resultCodes.contains(CmrConstants.RDC_STATUS_ABORTED))
        && (resultCodes.contains(CmrConstants.RDC_STATUS_COMPLETED) || resultCodes.contains(CmrConstants.RDC_STATUS_COMPLETED))) {
      overallStatus = CmrConstants.RDC_STATUS_COMPLETED;
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
   * Initializes the ProcessClient
   * 
   * @throws Exception
   */
  private void initClient() throws Exception {
    if (this.serviceClient == null) {
      this.serviceClient = CmrServicesFactory.getInstance().createClient(BATCH_SERVICES_URL, ProcessClient.class);
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

  /**
   * Creates a basic Comment log record after batch receives the result
   * 
   * @param service
   * @param entityManager
   * 
   * @param user
   * @param req
   *          id
   * @param cmt
   * @throws CmrException
   * @throws SQLException
   */
  public void createCommentLogAfterProcess(EntityManager entityManager, String user, long reqId, String cmt) throws CmrException, SQLException {
    ReqCmtLog reqCmtLog = new ReqCmtLog();
    ReqCmtLogPK reqCmtLogpk = new ReqCmtLogPK();
    reqCmtLogpk.setCmtId(SystemUtil.getNextID(entityManager, SystemConfiguration.getValue("MANDT"), "CMT_ID"));
    reqCmtLog.setId(reqCmtLogpk);
    reqCmtLog.setReqId(reqId);
    reqCmtLog.setCmt(cmt);
    // save cmtlockedIn as Y default for current realese
    reqCmtLog.setCmtLockedIn(CmrConstants.CMT_LOCK_IND_YES);
    reqCmtLog.setCreateById(user);
    reqCmtLog.setCreateByNm(user);
    // set createTs as current timestamp and updateTs same as CreateTs
    reqCmtLog.setCreateTs(SystemUtil.getCurrentTimestamp());
    reqCmtLog.setUpdateTs(reqCmtLog.getCreateTs());
    createEntity(reqCmtLog, entityManager);

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

  @Override
  protected boolean useServicesConnections() {
    return true;
  }
}
