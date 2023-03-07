/**
 * 
 */
package com.ibm.cmr.create.batch.service;

import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.persistence.EntityManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;

import com.ibm.ci.search.client.IndexServiceClient;
import com.ibm.ci.search.client.impl.index.DeleteFromIndexRequest;
import com.ibm.ci.search.client.impl.index.IndexResponse;
import com.ibm.cio.cmr.create.entity.NotifyReq;
import com.ibm.cio.cmr.create.entity.NotifyReqPK;
import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.automation.util.DummyServletRequest;
import com.ibm.cio.cmr.request.automation.util.RequestChangeContainer;
import com.ibm.cio.cmr.request.automation.util.geo.USUtil;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.AddrPK;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.AdminPK;
import com.ibm.cio.cmr.request.entity.CompoundEntity;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.entity.DataPK;
import com.ibm.cio.cmr.request.entity.Kna1;
import com.ibm.cio.cmr.request.entity.MassUpdt;
import com.ibm.cio.cmr.request.entity.MassUpdtAddr;
import com.ibm.cio.cmr.request.entity.MassUpdtData;
import com.ibm.cio.cmr.request.entity.MqIntfReqQueue;
import com.ibm.cio.cmr.request.entity.MqIntfReqQueuePK;
import com.ibm.cio.cmr.request.entity.ReqCmtLog;
import com.ibm.cio.cmr.request.entity.ReqCmtLogPK;
import com.ibm.cio.cmr.request.entity.ReservedCMRNos;
import com.ibm.cio.cmr.request.entity.ReservedCMRNosPK;
import com.ibm.cio.cmr.request.entity.WfHist;
import com.ibm.cio.cmr.request.entity.listeners.ChangeLogListener;
import com.ibm.cio.cmr.request.model.CompanyRecordModel;
import com.ibm.cio.cmr.request.model.ParamContainer;
import com.ibm.cio.cmr.request.model.requestentry.RequestEntryModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.QuickSearchService;
import com.ibm.cio.cmr.request.user.AppUser;
import com.ibm.cio.cmr.request.util.CompanyFinder;
import com.ibm.cio.cmr.request.util.RequestUtils;
import com.ibm.cio.cmr.request.util.SlackAlertsUtil;
import com.ibm.cio.cmr.request.util.SystemLocation;
import com.ibm.cio.cmr.request.util.SystemParameters;
import com.ibm.cio.cmr.request.util.SystemUtil;
import com.ibm.cio.cmr.request.util.geo.GEOHandler;
import com.ibm.cio.cmr.request.util.geo.impl.LAHandler;
import com.ibm.cmr.create.batch.model.CmrServiceInput;
import com.ibm.cmr.create.batch.model.MQIntfReqQueueModel;
import com.ibm.cmr.create.batch.model.MassUpdateServiceInput;
import com.ibm.cmr.create.batch.model.NotifyReqModel;
import com.ibm.cmr.create.batch.util.BatchUtil;
import com.ibm.cmr.create.batch.util.DebugUtil;
import com.ibm.cmr.create.batch.util.ProfilerLogger;
import com.ibm.cmr.create.batch.util.masscreate.WorkerThreadFactory;
import com.ibm.cmr.create.batch.util.masscreate.handler.impl.USMassUpdateWorker;
import com.ibm.cmr.create.batch.util.mq.MQMsgConstants;
import com.ibm.cmr.create.batch.util.mq.exception.MQErrorLine;
import com.ibm.cmr.create.batch.util.mq.exception.MQProcessingException;
import com.ibm.cmr.services.client.CmrServicesFactory;
import com.ibm.cmr.services.client.MassProcessClient;
import com.ibm.cmr.services.client.MassServicesFactory;
import com.ibm.cmr.services.client.ProcessClient;
import com.ibm.cmr.services.client.UpdateByEntClient;
import com.ibm.cmr.services.client.process.ProcessRequest;
import com.ibm.cmr.services.client.process.ProcessResponse;
import com.ibm.cmr.services.client.process.RDcRecord;
import com.ibm.cmr.services.client.process.ent.EnterpriseUpdtRequest;
import com.ibm.cmr.services.client.process.ent.EnterpriseUpdtResponse;
import com.ibm.cmr.services.client.process.mass.MassProcessRequest;
import com.ibm.cmr.services.client.process.mass.MassProcessResponse;
import com.ibm.cmr.services.client.process.mass.MassUpdateRecord;
import com.ibm.cmr.services.client.process.mass.RequestValueRecord;
import com.ibm.cmr.services.client.process.mass.ResponseRecord;

/**
 * Updated version of {@link NotifyReqService}
 * 
 * @author Jeffrey Zamora
 * @since b1309
 * 
 */
public class TransConnService extends BaseBatchService {

  protected static final String BATCH_SERVICES_URL = SystemConfiguration.getValue("BATCH_SERVICES_URL");
  public static final String ACTION_RDC_UPDATE = "System Action:RDc Update";
  public static final String ACTION_RDC_CREATE = "System Action:RDc Create";
  public static final String ACTION_RDC_DELETE = "System Action:RDc Mass Delete";
  public static final String ACTION_RDC_REACTIVATE = "System Action:RDc Mass Reactivate";
  public static final String ACTION_RDC_SINGLE_REACTIVATE = "System Action:RDc Single Reactivate";
  protected static final String ACTION_RDC_MASS_UPDATE = "System Action:RDc Mass Update";
  protected static final String FORCED_CHANGE_ACTION = "System Action: Forced Changes.";
  protected static final String ACTION_RDC_UPDT_BY_ENT = "System Action:RDc Enterprise update";
  protected static final String FORCED_UPDATE_COMMENT = "System Action:  Status changed to 'Processing Create/Updt Pending', Request Type changed to 'Update' for further automatic processing";
  protected static final String REVERT_TO_CREATE_COMMENT = "System Action:  Request Type reverted to 'Create' (original request type)";

  protected ProcessClient serviceClient;
  protected MassProcessClient massServiceClient;
  protected UpdateByEntClient updtByEntClient;

  protected SimpleDateFormat ERDAT_FORMATTER = new SimpleDateFormat("yyyyMMdd");

  protected static final List<String> SINGLE_REQUEST_TYPES = Arrays.asList(CmrConstants.REQ_TYPE_CREATE, CmrConstants.REQ_TYPE_UPDATE);
  protected static final List<String> MASS_REQUEST_TYPES = Arrays.asList(CmrConstants.REQ_TYPE_MASS_UPDATE, CmrConstants.REQ_TYPE_REACTIVATE,
      CmrConstants.REQ_TYPE_DELETE);

  protected boolean deleteRDcTargets;

  protected boolean multiMode;

  public TransConnService() {
    super();
  }

  @Override
  protected Boolean executeBatch(EntityManager entityManager) throws Exception {
    try {
      initClient();

      LOG.info("Multi Mode: " + this.multiMode);

      ChangeLogListener.setUser(BATCH_USER_ID);

      List<Long> records = null;

      LOG.info("Processing Aborted records (retry)...");
      records = gatherAbortedRecords(entityManager);
      monitorAbortedRecords(entityManager, records);

      LOG.info("Processing TransConn records...");
      records = gatherTransConnRecords(entityManager);
      monitorTransconn(entityManager, records);

      LOG.info("Processing MQ Interface records...");
      records = gatherMQInterfaceRequests(entityManager);
      monitorMQInterfaceRequests(entityManager, records);

      LOG.info("Processing Completed Manual records...");
      records = gatherDisAutoProcRecords(entityManager);
      monitorDisAutoProcRec(entityManager, records);

      LOG.info("Updation Of Sap Numbers For Completed Request...");
      monitorUpdateSapNumber(entityManager);

      if ("Y".equals(SystemParameters.getString("POOL.CMR.STATUS"))) {
        LOG.info("Processing Pool records..");
        records = gatherLegacyPending(entityManager);
        monitorLegacyPending(entityManager, records);
      }

      LOG.info("Processing LA Reprocess RDC records...");
      records = gatherLAReprocessRdcRecords(entityManager);
      monitorLAReprocessRdcRecords(entityManager, records);

      return true;
    } catch (Exception e) {
      addError(e);
      return false;
    }
  }

  protected List<Long> gatherLAReprocessRdcRecords(EntityManager entityManager) {
    // search for LA records to be reprocessed in RDC
    String sql = ExternalizedQuery.getSql("BATCH.MONITOR.LA_RDC_REPROCESS");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    List<Admin> reprocessRecords = query.getResults(Admin.class);
    LOG.debug("Size of LA Reprocess Rdc Records : " + reprocessRecords.size());
    List<Long> queue = new ArrayList<>();
    for (Admin admin : reprocessRecords) {
      queue.add(admin.getId().getReqId());
    }
    return queue;
  }

  /**
   * Prepares a list of request IDs to process for the aborted flow
   * 
   * @param entityManager
   * @return
   */
  protected List<Long> gatherAbortedRecords(EntityManager entityManager) {
    long start = new Date().getTime();
    // search the aborted records from Admin table
    String sql = ExternalizedQuery.getSql("BATCH.MONITOR_ABORTED_REC");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    List<Admin> abortedRecords = query.getResults(Admin.class);
    LOG.debug("Size of Aborted Records : " + abortedRecords.size());
    List<Long> queue = new ArrayList<>();
    for (Admin admin : abortedRecords) {
      queue.add(admin.getId().getReqId());
    }
    ProfilerLogger.LOG.trace("After gatherAbortedRecords " + DurationFormatUtils.formatDuration(new Date().getTime() - start, "m 'm' s 's'"));
    return queue;
  }

  /**
   * Retrieves {@link Admin} records with RDC_PROCESSING_STATUS = 'A' (Aborted)
   * 
   * @param entityManager
   * @throws JsonGenerationException
   * @throws JsonMappingException
   * @throws IOException
   * @throws Exception
   */
  public void monitorAbortedRecords(EntityManager entityManager, List<Long> abortedRecords)
      throws JsonGenerationException, JsonMappingException, IOException, Exception {

    // lockAdminRecordsForProcessing(abortedRecords, entityManager);

    for (Long id : abortedRecords) {
      try {
        Thread.currentThread().setName("REQ-" + id);

        long start = new Date().getTime();
        AdminPK pk = new AdminPK();
        pk.setReqId(id);
        Admin admin = entityManager.find(Admin.class, pk);

        LOG.info("Processing Aborted Record " + admin.getId().getReqId() + " [Request ID: " + admin.getId().getReqId() + "]");

        // get the data
        String sql = ExternalizedQuery.getSql("BATCH.GET_DATA");
        PreparedQuery query = new PreparedQuery(entityManager, sql);
        query.setParameter("REQ_ID", admin.getId().getReqId());

        Data data = query.getSingleResult(Data.class);
        entityManager.detach(data);

        if (SINGLE_REQUEST_TYPES.contains(admin.getReqType()) && CmrConstants.REQUEST_STATUS.COM.toString().equals(admin.getReqStatus())) {
          processSingleRequest(entityManager, admin, data);

        } else if (MASS_REQUEST_TYPES.contains(admin.getReqType())) {
          processMassChanges(entityManager, admin, data);

        } else if (CmrConstants.REQ_TYPE_UPDT_BY_ENT.equals(admin.getReqType())) {
          processUpdateByEnterprise(entityManager, admin, data);

        } else {
          LOG.warn("Request ID " + admin.getId().getReqId() + " cannot be processed. Improper Type or not completed.");
        }

        partialCommit(entityManager);
        ProfilerLogger.LOG.trace("After monitorAbortedRecords for Request ID: " + id + " "
            + DurationFormatUtils.formatDuration(new Date().getTime() - start, "m 'm' s 's'"));
      } catch (Exception e) {
        SlackAlertsUtil.recordException("TransConn", "Request " + id, e);
        LOG.error("Error in processing Aborted Record wit Request ID " + id + " [" + e.getMessage() + "]", e);
      }
    }
    Thread.currentThread().setName("TransConn-" + Thread.currentThread().getId());

  }

  /**
   * Prepares a list of request IDs to process for the transconn flow
   * 
   * @param entityManager
   * @return
   */
  protected List<Long> gatherTransConnRecords(EntityManager entityManager) {
    long start = new Date().getTime();
    // search the notify requests from transconn where NOTIFIED_IND <> 'Y'
    String sql = ExternalizedQuery.getSql("BATCH.MONITOR_TRANSCONN");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("NOTIFIED_IND", CmrConstants.NOTIFY_IND_YES);

    List<NotifyReq> notifyList = query.getResults(NotifyReq.class);
    LOG.debug("Size of Notify List : " + notifyList.size());
    List<Long> queue = new ArrayList<>();
    for (NotifyReq notif : notifyList) {
      queue.add(notif.getId().getNotifyId());
    }
    ProfilerLogger.LOG.trace("After gatherTransConnRecords " + DurationFormatUtils.formatDuration(new Date().getTime() - start, "m 'm' s 's'"));
    return queue;

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
  public void monitorTransconn(EntityManager entityManager, List<Long> notifyList) throws Exception {

    for (Long id : notifyList) {
      try {
        Thread.currentThread().setName("REQ-" + id);
        long start = new Date().getTime();
        NotifyReqPK pk = new NotifyReqPK();
        pk.setNotifyId(id);
        NotifyReq notify = entityManager.find(NotifyReq.class, pk);
        if (BatchUtil.excludeForEnvironment(this.context, entityManager, notify.getReqId())) {
          continue;
        }
        LOG.info("Processing Notify Req " + notify.getId().getNotifyId() + " [Request ID: " + notify.getReqId() + "]");
        NotifyReqModel notifyReqModel = new NotifyReqModel();
        copyValuesFromEntity(notify, notifyReqModel);

        // create a entry in request's comment log re_cmt_log table
        if (!StringUtils.isBlank(notify.getCmtLogMsg())) {
          createNotifyReqCommentLog(entityManager, notifyReqModel);
        }
        // update the NOTIFIED_IND of NOTIFY_REQ Table for each record
        // processed
        notify.setNotifiedInd(CmrConstants.NOTIFY_IND_YES);
        updateEntity(notify, entityManager);

        partialCommit(entityManager); // commit the transconn changes so
        // that
        // they won't be on the next run

        // retrieve the mail contents and needed entities
        String sqlMail = "BATCH.GET_MAIL_CONTENTS";
        String mapping = Admin.BATCH_SERVICE_MAPPING;
        PreparedQuery requestQuery = new PreparedQuery(entityManager, ExternalizedQuery.getSql(sqlMail));
        requestQuery.setParameter("REQ_ID", notify.getReqId());
        requestQuery.setParameter("REQ_STATUS", notify.getReqStatus());
        requestQuery.setParameter("CHANGED_BY_ID", notify.getChangedById());
        List<CompoundEntity> records = requestQuery.getCompundResults(1, Admin.class, mapping);

        CompoundEntity entities = records.get(0);
        Admin admin = entities.getEntity(Admin.class);
        Data data = entities.getEntity(Data.class);
        WfHist wfHist = entities.getEntity(WfHist.class);
        entityManager.detach(data);
        entityManager.detach(wfHist);

        if (wfHist != null) {
          RequestUtils.sendEmailNotifications(entityManager, admin, wfHist);
        } else {
          LOG.warn("Cannot create Workflow History, missing WF_HIST record.");
        }

        if (SINGLE_REQUEST_TYPES.contains(admin.getReqType()) && CmrConstants.REQUEST_STATUS.COM.toString().equals(admin.getReqStatus())) {
          processSingleRequest(entityManager, admin, data);

        } else if (MASS_REQUEST_TYPES.contains(admin.getReqType())) {
          processMassChanges(entityManager, admin, data);

        } else if (CmrConstants.REQ_TYPE_UPDT_BY_ENT.equals(admin.getReqType())) {
          processUpdateByEnterprise(entityManager, admin, data);

        } else {
          LOG.warn("Request ID " + admin.getId().getReqId() + " cannot be processed. Improper Type or not completed.");
        }

        partialCommit(entityManager);
        ProfilerLogger.LOG.trace(
            "After monitorTransconn for Request ID: " + id + " " + DurationFormatUtils.formatDuration(new Date().getTime() - start, "m 'm' s 's'"));
      } catch (Exception e) {
        SlackAlertsUtil.recordException("TransConn", "Notify ID " + id, e);
        LOG.error("Error in processing TransConn Record with Notify ID " + id + " [" + e.getMessage() + "]", e);
      }
    }
    Thread.currentThread().setName("TransConn-" + Thread.currentThread().getId());

  }

  /**
   * Prepares a list of request IDs to process for the mq flow
   * 
   * @param entityManager
   * @return
   */
  protected List<Long> gatherMQInterfaceRequests(EntityManager entityManager) {
    long start = new Date().getTime();
    String sql = ExternalizedQuery.getSql("BATCH.MONITOR_MQ_INTF_REQ_QUEUE");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("MQ_IND", CmrConstants.MQ_IND_YES);

    List<MqIntfReqQueue> mqIntfList = query.getResults(MqIntfReqQueue.class);
    LOG.debug("Size of MQ Intf List : " + mqIntfList.size());

    List<Long> queue = new ArrayList<>();
    for (MqIntfReqQueue mq : mqIntfList) {
      queue.add(mq.getId().getQueryReqId());
    }
    ProfilerLogger.LOG.trace("After gatherMQInterfaceRequests " + DurationFormatUtils.formatDuration(new Date().getTime() - start, "m 'm' s 's'"));
    return queue;

  }

  /**
   * Monitor records from the CREQCMR.MQ_INTF_REQ_QUEUE
   * 
   * @param entityManager
   * @throws JsonGenerationException
   * @throws JsonMappingException
   * @throws IOException
   * @throws Exception
   */
  public void monitorMQInterfaceRequests(EntityManager entityManager, List<Long> mqIntfList)
      throws JsonGenerationException, JsonMappingException, IOException, Exception {

    boolean isError = false;
    for (Long id : mqIntfList) {
      Thread.currentThread().setName("REQ-" + id);

      long start = new Date().getTime();
      MqIntfReqQueuePK pk = new MqIntfReqQueuePK();
      pk.setQueryReqId(id);
      MqIntfReqQueue mqIntfReq = entityManager.find(MqIntfReqQueue.class, pk);

      isError = mqIntfReq.getReqStatus() != null && mqIntfReq.getReqStatus().contains(MQMsgConstants.REQ_STATUS_SER);
      try {
        LOG.info("Processing MQ Intf Req " + mqIntfReq.getId().getQueryReqId() + " [Request ID: " + mqIntfReq.getReqId() + "]");
        MQIntfReqQueueModel mqIntfReqModel = new MQIntfReqQueueModel();
        copyValuesFromEntity(mqIntfReq, mqIntfReqModel);

        // create a entry in request's comment log re_cmt_log table
        if (isError) {
          createMQIntfReqCommentLog(entityManager, mqIntfReqModel);
        }
        // update the MQ_IND of NOTIFY_REQ Table for each record
        // processed
        mqIntfReq.setMqInd(CmrConstants.MQ_IND_YES);
        updateEntity(mqIntfReq, entityManager);

        partialCommit(entityManager); // commit the MQ_INTF_REQ_QUEUE
        // changes so
        // that
        // they won't be on the next run

        // retrieve the mail contents and needed entities
        String sqlMail = "BATCH.GET_MAIL_CONTENTS";
        String mapping = Admin.BATCH_SERVICE_MAPPING;
        PreparedQuery requestQuery = new PreparedQuery(entityManager, ExternalizedQuery.getSql(sqlMail));
        requestQuery.setParameter("REQ_ID", mqIntfReq.getReqId());
        requestQuery.setParameter("REQ_STATUS", isError ? CmrConstants.REQUEST_STATUS.PPN.toString() : CmrConstants.REQUEST_STATUS.COM.toString());
        requestQuery.setParameter("CHANGED_BY_ID", mqIntfReq.getLastUpdtBy());
        List<CompoundEntity> records = requestQuery.getCompundResults(1, Admin.class, mapping);

        CompoundEntity entities = records.get(0);
        Admin admin = entities.getEntity(Admin.class);
        Data data = entities.getEntity(Data.class);
        WfHist wfHist = entities.getEntity(WfHist.class);

        if (data != null) {
          entityManager.detach(data);
        }
        if (wfHist != null) {
          entityManager.detach(wfHist);
        } else {
          // create generic wf hist, will be removed once all
          // functions e2e are
          // tested
          wfHist = new WfHist();
          wfHist.setReqId(mqIntfReq.getReqId());
          wfHist.setCmt(isError ? MQMsgConstants.WH_COMMENT_REJECT : MQMsgConstants.WH_HIST_CMT_COM + data.getCmrNo());
          wfHist.setCreateById(MQMsgConstants.MQ_APP_USER);
          wfHist.setCreateTs(SystemUtil.getCurrentTimestamp());
          wfHist.setCreateByNm(MQMsgConstants.MQ_APP_USER);
          wfHist.setReqStatus(isError ? CmrConstants.REQUEST_STATUS.PPN.toString() : CmrConstants.REQUEST_STATUS.COM.toString());
          wfHist.setRejReason("");
        }

        if (wfHist != null) {
          RequestUtils.sendEmailNotifications(entityManager, admin, wfHist);
        }
        // else {
        // LOG.warn("Cannot create Workflow History, missing WF_HIST
        // record.");
        // }

        if (SINGLE_REQUEST_TYPES.contains(admin.getReqType()) && CmrConstants.REQUEST_STATUS.COM.toString().equals(admin.getReqStatus())) {
          processSingleRequest(entityManager, admin, data);

        } else if (MASS_REQUEST_TYPES.contains(admin.getReqType())) {
          processMassChanges(entityManager, admin, data);

        } else if (CmrConstants.REQ_TYPE_UPDT_BY_ENT.equals(admin.getReqType())) {
          processUpdateByEnterprise(entityManager, admin, data);

        } else {
          LOG.warn("Request ID " + admin.getId().getReqId() + " cannot be processed. Improper Type or not completed.");
        }

        partialCommit(entityManager);
        ProfilerLogger.LOG.trace("After monitorMQInterfaceRequests for Request ID: " + id + " "
            + DurationFormatUtils.formatDuration(new Date().getTime() - start, "m 'm' s 's'"));
      } catch (Exception e) {
        SlackAlertsUtil.recordException("TransConn", "Query ID " + mqIntfReq.getId().getQueryReqId(), e);
        LOG.error("Error in processing TransConn Record " + mqIntfReq.getId().getQueryReqId() + " for Request ID " + mqIntfReq.getReqId() + " ["
            + e.getMessage() + "]", e);
      }
    }
    Thread.currentThread().setName("TransConn-" + Thread.currentThread().getId());

  }

  /**
   * Prepares a list of request IDs to process for the manual flow
   * 
   * @param entityManager
   * @return
   */
  protected List<Long> gatherDisAutoProcRecords(EntityManager entityManager) {
    long start = new Date().getTime();
    // search the manual processed records from Admin table
    String sql = ExternalizedQuery.getSql("BATCH.MONITOR_DISABLE_AUTO_PROC");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    List<Admin> manualRecList = query.getResults(Admin.class);
    LOG.debug("Size of Manually Completed Requests : " + manualRecList.size());
    List<Long> queue = new ArrayList<>();
    for (Admin admin : manualRecList) {
      queue.add(admin.getId().getReqId());
    }
    ProfilerLogger.LOG.trace("After gatherDisAutoProcRecords " + DurationFormatUtils.formatDuration(new Date().getTime() - start, "m 'm' s 's'"));
    return queue;
  }

  /**
   * Monitor manually processed records
   * 
   * @param entityManager
   * @throws JsonGenerationException
   * @throws JsonMappingException
   * @throws IOException
   * @throws Exception
   */
  public void monitorDisAutoProcRec(EntityManager entityManager, List<Long> manualRecList) throws Exception {

    for (Long id : manualRecList) {
      try {
        Thread.currentThread().setName("REQ-" + id);

        long start = new Date().getTime();
        AdminPK pk = new AdminPK();
        pk.setReqId(id);
        Admin admin = entityManager.find(Admin.class, pk);
        LOG.info("Processing Manually Completed Record [Request ID: " + admin.getId().getReqId() + "]");

        // get the data
        String sql = ExternalizedQuery.getSql("BATCH.GET_DATA");
        PreparedQuery query = new PreparedQuery(entityManager, sql);
        query.setParameter("REQ_ID", admin.getId().getReqId());

        Data data = query.getSingleResult(Data.class);
        entityManager.detach(data);

        if (SINGLE_REQUEST_TYPES.contains(admin.getReqType()) && CmrConstants.REQUEST_STATUS.COM.toString().equals(admin.getReqStatus())) {
          processSingleRequest(entityManager, admin, data);
        } else {
          LOG.warn("Request ID " + admin.getId().getReqId() + " cannot be processed. Improper Type or not completed.");
        }

        partialCommit(entityManager);
        ProfilerLogger.LOG.trace("After monitorDisAutoProcRec for Request ID: " + id + " "
            + DurationFormatUtils.formatDuration(new Date().getTime() - start, "m 'm' s 's'"));
      } catch (Exception e) {
        SlackAlertsUtil.recordException("TransConn", "Request " + id, e);
        LOG.error("Error in processing Manual Record with Request ID " + id + " [" + e.getMessage() + "]", e);
      }
    }
    Thread.currentThread().setName("TransConn-" + Thread.currentThread().getId());
  }

  /**
   * Prepares a list of request IDs to process for the pool flow
   * 
   * @param entityManager
   * @return
   */
  protected List<Long> gatherLegacyPending(EntityManager entityManager) {
    long start = new Date().getTime();
    String sql = ExternalizedQuery.getSql("BATCH.MONITOR_LEGACY_PENDING");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("PROC_TYPE", SystemConfiguration.getValue("BATCH_CMR_POOL_PROCESSING_TYPE"));
    query.setParameter("ISSU_CNTRY", SystemConfiguration.getValue("BATCH_CMR_POOL_ISSUING_CNTRY"));
    // jz: temporary, so that only Commercial REGULAR will be done for now
    // until
    // cm: scenario will be hardcoded for now for REGULAR and PRIV
    // CMR-5564 is implemented
    // query.setParameter("SCENARIO", "REGULAR");
    List<Admin> pvcRecords = query.getResults(Admin.class);
    LOG.debug("Size of PVC Records : " + pvcRecords.size());
    List<Long> queue = new ArrayList<>();
    for (Admin admin : pvcRecords) {
      queue.add(admin.getId().getReqId());
    }
    ProfilerLogger.LOG.trace("After gatherLegacyPending " + DurationFormatUtils.formatDuration(new Date().getTime() - start, "m 'm' s 's'"));
    return queue;
  }

  protected void monitorLegacyPending(EntityManager entityManager, List<Long> pvcRecords) {
    // Search the records with Status PCP and check if current timestamp
    // falls
    // within host down outage

    // Update CREQCMR.ADMIN set STATUS to 'PCR' (Processing Validation),
    // Lock
    // info CreateCMR, DISABLE_AUTO_PROC = 'Y'
    for (Long id : pvcRecords) {
      try {
        Thread.currentThread().setName("REQ-" + id);

        long start = new Date().getTime();
        AdminPK pk = new AdminPK();
        pk.setReqId(id);
        Admin admin = entityManager.find(Admin.class, pk);

        LOG.info("Processing PVC Record " + admin.getId().getReqId() + " [Request ID: " + admin.getId().getReqId() + "]");
        admin.setReqStatus("PCR");
        admin.setLockInd("Y");
        admin.setLockBy(BATCH_USER_ID);
        admin.setLockByNm(BATCH_USER_ID);
        admin.setLockTs(SystemUtil.getCurrentTimestamp());

        String sql = ExternalizedQuery.getSql("BATCH.GET_DATA");
        PreparedQuery query = new PreparedQuery(entityManager, sql);
        query.setParameter("REQ_ID", admin.getId().getReqId());

        Data data = query.getSingleResult(Data.class);
        entityManager.detach(data);

        // Query FindCMR using filter on configuration file
        CompanyRecordModel search = new CompanyRecordModel();
        search.setName(SystemConfiguration.getValue("BATCH_CMR_POOL_CUST_NAME"));
        search.setIssuingCntry(SystemConfiguration.getValue("BATCH_CMR_POOL_ISSUING_CNTRY"));
        search.setCountryCd(SystemConfiguration.getValue("BATCH_CMR_POOL_LANDED_CNTRY_CD"));
        search.setStreetAddress1(SystemConfiguration.getValue("BATCH_CMR_POOL_STREET"));
        search.setCity(SystemConfiguration.getValue("BATCH_CMR_POOL_CITY"));
        search.setStateProv(SystemConfiguration.getValue("BATCH_CMR_POOL_STATE"));
        search.setPostCd(SystemConfiguration.getValue("BATCH_CMR_POOL_POSTAL"));
        search.setPoolRecord(true);
        // Select first record from FindCMR which is not in
        // CREQCMR.RESERVED_CMR_NOS

        LOG.debug("Setting pool record to TRUE for PVC..");
        List<CompanyRecordModel> records = CompanyFinder.findCompanies(search);
        LOG.info("Number of CMRs in Pool: " + records.size());

        if (records.size() == 0) {
          LOG.error("CMR Pool depleted. Cannot proceed with automatic CMR number assignment");
          return;
        }

        synchronized (this) {
          for (CompanyRecordModel record : records) {
            // check if CMR is in reserved
            String sqlReservedCMR = ExternalizedQuery.getSql("LD.REACDEL_RESERVED_CMR_CHECK");
            PreparedQuery queryReservedCMR = new PreparedQuery(entityManager, sqlReservedCMR);

            queryReservedCMR.setParameter("CMR_NO", record.getCmrNo());
            queryReservedCMR.setParameter("COUNTRY", record.getIssuingCntry());
            queryReservedCMR.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
            queryReservedCMR.setForReadOnly(true);
            if (queryReservedCMR.exists() && (records.indexOf(record) == (records.size() - 1))) {
              LOG.error("All CMR numbers in the Pool are existing in reserved. Cannot proceed with automatic CMR number assignment");
              return;
            } else if (queryReservedCMR.exists()) {
              continue;
            }
            updateEntity(admin, entityManager);
            // partialCommit(entityManager);
            LOG.info("CMR no does not exist on reserved. Continuing...");
            // Update CREQCMR.DATA set CMR_NO = from pool CMR, set
            // CREQCMR.ADMIN
            // REQ_STATUS to 'COM', put CMR_NO in RESERVED_CMR_NOS
            data.setCmrNo(record.getCmrNo());
            updateEntity(data, entityManager);

            admin.setLockInd(CmrConstants.YES_NO.N.toString());
            admin.setLockTs(null);
            admin.setLockBy(null);
            admin.setLockByNm(null);
            admin.setDisableAutoProc("Y"); // added to send it to
            // processing
            // service 1020
            admin.setRdcProcessingStatus(null);
            admin.setReqStatus("COM");
            admin.setPoolCmrIndc(CmrConstants.YES_NO.Y.toString());
            // set to aborted so the details can be sent to
            // processing service
            // on
            // next run
            admin.setRdcProcessingStatus("A");
            updateEntity(admin, entityManager);

            ReservedCMRNos reservedCMRNo = new ReservedCMRNos();
            ReservedCMRNosPK reservedCMRNoPK = new ReservedCMRNosPK();

            reservedCMRNoPK.setCmrIssuingCntry(record.getIssuingCntry());
            reservedCMRNoPK.setCmrNo(record.getCmrNo());
            reservedCMRNoPK.setMandt(SystemConfiguration.getValue("MANDT"));
            reservedCMRNo.setId(reservedCMRNoPK);

            reservedCMRNo.setStatus("A");
            reservedCMRNo.setCreateTs(SystemUtil.getCurrentTimestamp());
            reservedCMRNo.setLastUpdtTs(SystemUtil.getCurrentTimestamp());
            reservedCMRNo.setCreateBy(BATCH_USER_ID);
            reservedCMRNo.setLastUpdtBy(BATCH_USER_ID);

            createEntity(reservedCMRNo, entityManager);

            String knaSql = ExternalizedQuery.getSql("BATCH.GET.KNA1_MANDT_CMRNO");
            PreparedQuery knaQuery = new PreparedQuery(entityManager, knaSql);
            knaQuery.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
            knaQuery.setParameter("CMR_NO", record.getCmrNo());
            knaQuery.setParameter("KATR6", data.getCmrIssuingCntry());
            List<Kna1> kna1List = knaQuery.getResults(Kna1.class);
            Map<String, String> kna1KunnrMap = new HashMap<String, String>();
            for (Kna1 kna1 : kna1List) {
              if (!StringUtils.isBlank(admin.getSourceSystId())) {
                String source = admin.getSourceSystId();
                if (source.length() > 12) {
                  source = source.substring(0, 12);
                }
                kna1.setErnam(source);
              } else {
                kna1.setErnam(BATCH_USER_ID);
              }
              kna1.setErdat(ERDAT_FORMATTER.format(SystemUtil.getCurrentTimestamp()));
              if ("ZS01".equals(kna1.getKtokd()) || ("ZP01".equals(kna1.getKtokd()) && !"PG".equals(kna1.getAufsd()))) {
                // track only CMR KUNNRs, not paygos
                kna1KunnrMap.put(kna1.getKtokd(), kna1.getId().getKunnr());
              }
              updateEntity(kna1, entityManager);
            }

            // partialCommit(entityManager);

            // Workflow history creation
            RequestUtils.createWorkflowHistoryFromBatch(entityManager, BATCH_USER_ID, admin, "Completed using Pool CMR assignment", "Pool Assignment",
                null, null, true);
            RequestUtils.createCommentLogFromBatch(entityManager, BATCH_USER_ID, admin.getId().getReqId(), "Completed using Pool CMR assignment");

            // Create a new Update request using the CMR_NO from
            // Pool and
            // overwrite with data from original request
            QuickSearchService qs = new QuickSearchService();
            ParamContainer params = new ParamContainer();

            record.setReqType("C");
            record.setPoolRecord(true);
            params.addParam("model", record);

            AppUser user = new AppUser();
            user.setIntranetId(BATCH_USER_ID);
            user.setBluePagesName(BATCH_USER_ID);
            DummyServletRequest dummyReq = new DummyServletRequest();
            if (dummyReq.getSession() != null) {
              LOG.trace("Session found for dummy req");
              dummyReq.getSession().setAttribute(CmrConstants.SESSION_APPUSER_KEY, user);
            } else {
              LOG.warn("Session not found for dummy req");
            }
            RequestEntryModel reqModel = qs.process(dummyReq, params);
            // get a request id, get the request and update data
            // from the
            // original
            // request
            AdminPK adminPk = new AdminPK();
            long reqId = reqModel.getReqId();
            adminPk.setReqId(reqId);
            Admin newAdmin = entityManager.find(Admin.class, adminPk);
            copyValuesToEntity(admin, newAdmin);
            newAdmin.setId(adminPk);
            newAdmin.setCreateTs(SystemUtil.getCurrentTimestamp());
            newAdmin.setLastUpdtTs(SystemUtil.getCurrentTimestamp());
            newAdmin.setInternalTyp("UPD_SIMPLE_AUTO");
            newAdmin.setLockInd(CmrConstants.YES_NO.N.toString());
            newAdmin.setLockTs(null);
            newAdmin.setLockBy(null);
            newAdmin.setLockByNm(null);
            newAdmin.setModelCmrNo(null);
            newAdmin.setReqType("U");
            newAdmin.setReqReason("POOL");
            newAdmin.setCustType(null);
            newAdmin.setLastUpdtBy(BATCH_USER_ID);
            newAdmin.setProcessedFlag(CmrConstants.YES_NO.N.toString());
            newAdmin.setProcessedTs(null);
            newAdmin.setDisableAutoProc(CmrConstants.YES_NO.N.toString());
            newAdmin.setRdcProcessingStatus(null);
            newAdmin.setRdcProcessingTs(null);
            newAdmin.setRdcProcessingMsg(null);

            updateEntity(newAdmin, entityManager);

            DataPK dataPk = new DataPK();
            dataPk.setReqId(reqId);
            Data newData = entityManager.find(Data.class, dataPk);

            copyValuesToEntity(data, newData);

            newData.setId(dataPk);
            newData.setCustGrp(null);
            newData.setCustSubGrp(null);
            if (data.getAffiliate() == null || data.getAffiliate().equals(""))
              newData.setAffiliate(record.getCmrNo());
            if (data.getEnterprise() == null || data.getEnterprise().equals(""))
              newData.setEnterprise(record.getCmrNo());

            updateEntity(newData, entityManager);

            PreparedQuery addrQuery = new PreparedQuery(entityManager, ExternalizedQuery.getSql("BATCH.GET_ADDR_ENTITY_CREATE_REQ"));
            addrQuery.setParameter("REQ_ID", admin.getId().getReqId());
            addrQuery.setParameter("ADDR_TYPE", "ZS01");
            Addr addr = addrQuery.getSingleResult(Addr.class);
            addr.setSapNo(kna1KunnrMap.get("ZS01"));
            updateEntity(addr, entityManager);

            PreparedQuery zi01AddrQuery = new PreparedQuery(entityManager, ExternalizedQuery.getSql("BATCH.GET_ADDR_ENTITY_CREATE_REQ"));
            zi01AddrQuery.setParameter("REQ_ID", admin.getId().getReqId());
            zi01AddrQuery.setParameter("ADDR_TYPE", "ZI01");
            Addr zi01Addr = zi01AddrQuery.getSingleResult(Addr.class);
            if (zi01Addr != null) {
              zi01Addr.setSapNo(kna1KunnrMap.get("ZP01"));
              if (!StringUtils.isBlank(zi01Addr.getSapNo())) {
                updateEntity(zi01Addr, entityManager);
              }
            }

            PreparedQuery zp01AddrQuery = new PreparedQuery(entityManager, ExternalizedQuery.getSql("BATCH.GET_ADDR_ENTITY_CREATE_REQ"));
            zp01AddrQuery.setParameter("REQ_ID", admin.getId().getReqId());
            zp01AddrQuery.setParameter("ADDR_TYPE", "PG01");
            List<Addr> zp01Addrs = zp01AddrQuery.getResults(Addr.class);

            PreparedQuery newAddrQuery = new PreparedQuery(entityManager, ExternalizedQuery.getSql("BATCH.GET_ADDR_FOR_SAP_NO"));
            newAddrQuery.setParameter("REQ_ID", reqId);
            List<Addr> newAddresses = newAddrQuery.getResults(Addr.class);

            if (newAddresses != null) {
              for (Addr newAddr : newAddresses) {
                AddrPK addrPK = newAddr.getId();
                if ("ZI01".equals(addrPK.getAddrType())) {
                  if (zi01Addr != null) {
                    // there is an actual ZI01 on original request
                    copyValuesToEntity(zi01Addr, newAddr);
                    newAddr.setImportInd(CmrConstants.YES_NO.N.toString());
                  } else {
                    // ZI01 was dummy copy
                    newAddr.setImportInd(CmrConstants.YES_NO.Y.toString());
                  }
                  newAddr.setSapNo(null);
                  newAddr.setChangedIndc(null);
                } else {
                  copyValuesToEntity(addr, newAddr);
                  newAddr.setSapNo(kna1KunnrMap.get("ZS01"));
                  newAddr.setImportInd(CmrConstants.YES_NO.Y.toString());
                  newAddr.setChangedIndc(CmrConstants.YES_NO.Y.toString());
                }
                newAddr.setId(addrPK);
                newAddr.setAddrStdResult("X");
                newAddr.setAddrStdAcceptInd(null);
                newAddr.setAddrStdRejReason(null);
                newAddr.setAddrStdRejCmt(null);
                newAddr.setAddrStdTs(null);
                newAddr.setRdcCreateDt(ERDAT_FORMATTER.format(SystemUtil.getCurrentTimestamp()));
                newAddr.setRdcLastUpdtDt(SystemUtil.getCurrentTimestamp());
                updateEntity(newAddr, entityManager);
              }

              if (zp01Addrs != null && !zp01Addrs.isEmpty()) {
                // copy zp01 addresses
                for (Addr zp01 : zp01Addrs) {
                  Addr newAddr = new Addr();
                  AddrPK addrPK = new AddrPK();
                  addrPK.setAddrSeq(zp01.getId().getAddrSeq());
                  addrPK.setReqId(reqId);
                  addrPK.setAddrType("PG01");
                  copyValuesToEntity(zp01, newAddr);
                  newAddr.setSapNo(null);
                  newAddr.setImportInd(CmrConstants.YES_NO.N.toString());
                  newAddr.setChangedIndc(null);
                  newAddr.setId(addrPK);
                  newAddr.setAddrStdResult("X");
                  newAddr.setAddrStdAcceptInd(null);
                  newAddr.setAddrStdRejReason(null);
                  newAddr.setAddrStdRejCmt(null);
                  newAddr.setAddrStdTs(null);
                  newAddr.setRdcCreateDt(ERDAT_FORMATTER.format(SystemUtil.getCurrentTimestamp()));
                  newAddr.setRdcLastUpdtDt(SystemUtil.getCurrentTimestamp());
                  updateEntity(newAddr, entityManager);
                  break;
                }
              }
            }
            newAdmin.setReqStatus("PCP");
            newAdmin.setPoolCmrIndc(CmrConstants.YES_NO.Y.toString());
            admin.setChildReqId(newAdmin.getId().getReqId());
            updateEntity(newAdmin, entityManager);
            updateEntity(admin, entityManager);

            RequestUtils.createCommentLogFromBatch(entityManager, BATCH_USER_ID, admin.getId().getReqId(),
                "Child Update Request " + reqId + " created.");

            partialCommit(entityManager);
            break;
          }
        }

        partialCommit(entityManager);
        ProfilerLogger.LOG.trace("After monitorLegacyPending for Request ID: " + id + " "
            + DurationFormatUtils.formatDuration(new Date().getTime() - start, "m 'm' s 's'"));
      } catch (Exception e) {
        LOG.error("Error in processing PVC Record with Request ID " + id + " [" + e.getMessage() + "]", e);
      }
    }
    Thread.currentThread().setName("TransConn-" + Thread.currentThread().getId());
  }

  /**
   * Calls the createCMRService and process the response returned
   * 
   * @param entityManager
   * @param reqId
   * @param input
   * @param batchAction
   * @param currentRDCProcStat
   * @throws JsonGenerationException
   * @throws JsonMappingException
   * @throws IOException
   * @throws Exception
   */
  public void processSingleRequest(EntityManager entityManager, Admin admin, Data data)
      throws JsonGenerationException, JsonMappingException, IOException, Exception {

    CmrServiceInput input = prepareSingleRequestInput(entityManager, admin, data);

    ProcessRequest request = new ProcessRequest();
    request.setCmrNo(input.getInputCmrNo());
    request.setMandt(input.getInputMandt());
    request.setReqId(input.getInputReqId());
    request.setReqType(input.getInputReqType());
    request.setUserId(input.getInputUserId());

    if (CmrConstants.REQ_TYPE_UPDATE.equals(input.getInputReqType())) {
      processUpdateRequest(entityManager, request, admin, data);
    } else if (CmrConstants.REQ_TYPE_CREATE.equals(input.getInputReqType())) {
      processCreateRequest(entityManager, request, admin, data);
    }
  }

  /**
   * Processes Request Type 'M', 'D', 'R'
   * 
   * @param entityManager
   * @param reqId
   * @param cmrServiceInput
   * @param batchAction
   * @param currentRDCProcStat
   * @throws JsonGenerationException
   * @throws JsonMappingException
   * @throws IOException
   * @throws Exception
   */
  public void processMassChanges(EntityManager entityManager, Admin admin, Data data)
      throws JsonGenerationException, JsonMappingException, IOException, Exception {
    // Create the request

    if (this.multiMode) {
      LOG.debug("Processing in Multi Mode..");
      processMassChangesMulti(entityManager, admin, data);
      return;
    }

    long reqId = admin.getId().getReqId();
    long iterationId = admin.getIterationId();
    String processingStatus = admin.getRdcProcessingStatus();

    MassUpdateServiceInput input = prepareMassChangeInput(entityManager, admin);

    MassProcessRequest request = new MassProcessRequest();
    // set the update mass record in request
    if (input.getInputReqType() != null && (input.getInputReqType().equalsIgnoreCase("D") || input.getInputReqType().equalsIgnoreCase("R"))) {
      request = prepareReactivateDelRequest(entityManager, admin, data, input);
    } else {
      request = prepareMassUpdateRequest(entityManager, admin, data, input);
    }

    // call the Mass Update service
    LOG.info("Sending request to Mass Update Service [Request ID: " + request.getReqId() + "  Type: " + request.getReqType() + "]");

    LOG.trace("Request JSON:");
    if (LOG.isTraceEnabled()) {
      DebugUtil.printObjectAsJson(LOG, request);
    }
    // actual service call here
    MassProcessResponse response = null;
    String applicationId = BatchUtil.getAppId(data.getCmrIssuingCntry());
    if (applicationId == null) {
      LOG.debug("No Application ID mapped to " + data.getCmrIssuingCntry());
      response = new MassProcessResponse();
      response.setReqId(request.getReqId());
      response.setMandt(request.getMandt());
      response.setStatus(CmrConstants.RDC_STATUS_NOT_COMPLETED);
      response.setMsg("No application ID defined for Country: " + data.getCmrIssuingCntry() + ". Cannot process RDc records.");
    } else {
      try {
        this.massServiceClient.setReadTimeout(60 * 15 * 1000); // 15
        // mins
        response = this.massServiceClient.executeAndWrap(applicationId, request, MassProcessResponse.class);
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

    // try this
    // get the results from the service and process jason response
    try {
      // update MASS_UPDT table with the error txt and row status cd
      for (ResponseRecord record : response.getRecords()) {
        PreparedQuery updtQuery = new PreparedQuery(entityManager, ExternalizedQuery.getSql("BATCH.GET_MASS_UPDT_ENTITY"));
        updtQuery.setParameter("REQ_ID", reqId);
        updtQuery.setParameter("ITERATION_ID", iterationId);
        updtQuery.setParameter("CMR_NO", record.getCmrNo());
        List<MassUpdt> updateList = updtQuery.getResults(MassUpdt.class);

        for (MassUpdt massUpdt : updateList) {
          massUpdt.setErrorTxt(record.getMessage());
          if (CmrConstants.RDC_STATUS_NOT_COMPLETED.equals(record.getStatus())) {
            massUpdt.setRowStatusCd("RDCER");
          } else if (CmrConstants.RDC_STATUS_COMPLETED.equals(record.getStatus())) {
            massUpdt.setRowStatusCd("DONE");
          }
          LOG.info("Mass Update Record Updated [Request ID: " + massUpdt.getId().getParReqId() + " CMR_NO: " + massUpdt.getCmrNo() + " SEQ No: "
              + massUpdt.getId().getSeqNo() + "]");
          updateEntity(massUpdt, entityManager);
        }
      }
      String resultCode = response.getStatus();

      String requestType = "";
      String action = "";
      switch (admin.getReqType()) {
      case "D":
        action = ACTION_RDC_DELETE;
        requestType = "Mass Delete";
        break;
      case "R":
        action = ACTION_RDC_REACTIVATE;
        requestType = "Mass Reactivate";
        break;
      case "X":
        action = ACTION_RDC_SINGLE_REACTIVATE;
        requestType = "Single Reactivate";
        break;
      case "M":
        action = ACTION_RDC_MASS_UPDATE;
        requestType = "Mass Update";
        break;
      }

      // create comment log and workflow history entries for update type
      // of
      // request
      StringBuilder comment = new StringBuilder();
      if (isCompletedSuccessfully(resultCode)) {
        comment = comment.append(requestType + " in RDc successfully completed.");
      } else {
        if (CmrConstants.RDC_STATUS_ABORTED.equals(resultCode) && CmrConstants.RDC_STATUS_ABORTED.equals(processingStatus)) {
          comment = comment.append(requestType + " in RDc failed: " + response.getMsg());
        } else if (CmrConstants.RDC_STATUS_ABORTED.equalsIgnoreCase(resultCode)) {
          comment = comment.append(requestType + " in RDc aborted: " + response.getMsg() + "\nSystem will retry once.");
        } else if (CmrConstants.RDC_STATUS_NOT_COMPLETED.equalsIgnoreCase(resultCode)) {
          comment = comment.append(requestType + " in RDc failed: " + response.getMsg());
        } else if (CmrConstants.RDC_STATUS_IGNORED.equalsIgnoreCase(resultCode)) {
          comment = comment.append(requestType + " in RDc skipped: " + response.getMsg());
        }
      }

      if (!CmrConstants.RDC_STATUS_IGNORED.equals(resultCode)) {
        RequestUtils.createWorkflowHistoryFromBatch(entityManager, BATCH_USER_ID, admin, comment.toString().trim(), action, null, null,
            "COM".equals(admin.getReqStatus()));
      }
      RequestUtils.createCommentLogFromBatch(entityManager, BATCH_USER_ID, admin.getId().getReqId(), comment.toString().trim());

      // only update Admin record once depending on the overall status of
      // the
      // request
      LOG.debug("Updating Admin record for Request ID " + admin.getId().getReqId());
      if (CmrConstants.RDC_STATUS_ABORTED.equals(resultCode) && CmrConstants.RDC_STATUS_ABORTED.equals(admin.getRdcProcessingStatus())) {
        admin.setRdcProcessingStatus(CmrConstants.RDC_STATUS_NOT_COMPLETED);
      } else {
        admin.setRdcProcessingStatus(resultCode);
      }
      admin.setRdcProcessingTs(SystemUtil.getCurrentTimestamp());
      admin.setRdcProcessingMsg(response.getMsg());
      updateEntity(admin, entityManager);
      LOG.debug("Request ID " + admin.getId().getReqId() + " Status: " + admin.getRdcProcessingStatus() + " Message: " + admin.getRdcProcessingMsg());

      partialCommit(entityManager);

    } catch (Exception e) {
      LOG.error("Error in processing Mass Change Request  " + admin.getId().getReqId(), e);
      addError("Mass Change Request " + admin.getId().getReqId() + " Error: " + e.getMessage());
    }

  }

  /**
   * Processes Request Type 'M', 'D', 'R'
   * 
   * @param entityManager
   * @param reqId
   * @param cmrServiceInput
   * @param batchAction
   * @param currentRDCProcStat
   * @throws JsonGenerationException
   * @throws JsonMappingException
   * @throws IOException
   * @throws Exception
   */
  public void processMassChangesMulti(EntityManager entityManager, Admin admin, Data data)
      throws JsonGenerationException, JsonMappingException, IOException, Exception {
    // Create the request

    long reqId = admin.getId().getReqId();
    long iterationId = admin.getIterationId();
    String processingStatus = admin.getRdcProcessingStatus();

    MassUpdateServiceInput input = prepareMassChangeInput(entityManager, admin);

    MassProcessRequest request = new MassProcessRequest();
    // set the update mass record in request
    if (input.getInputReqType() != null && (input.getInputReqType().equalsIgnoreCase("D") || input.getInputReqType().equalsIgnoreCase("R"))) {
      request = prepareReactivateDelRequest(entityManager, admin, data, input);
    } else {
      request = prepareMassUpdateRequest(entityManager, admin, data, input);
    }

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
    LOG.debug("Starting processing mass update at " + new Date());
    LOG.debug("Number of records found: " + request.getRecords().size());
    List<USMassUpdateWorker> workers = new ArrayList<USMassUpdateWorker>();

    LinkedList<MassUpdateRecord> origList = new LinkedList<>();
    origList.addAll(request.getRecords());

    List<List<MassUpdateRecord>> allocated = allocateRequests(origList, threads);

    LOG.debug("Allocations: ");
    for (List<MassUpdateRecord> list : allocated) {
      LOG.debug(" - " + list.size() + " records");
    }
    ExecutorService executor = Executors.newFixedThreadPool(threads, new WorkerThreadFactory("UpdateMulti"));
    for (List<MassUpdateRecord> list : allocated) {
      USMassUpdateWorker worker = new USMassUpdateWorker(list, reqId, request, data.getCmrIssuingCntry());
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
    LOG.debug("Mass update processing finished at " + new Date());

    MassProcessResponse response = new MassProcessResponse();
    response.setMandt(request.getMandt());
    response.setReqId(reqId);
    response.setReqType(request.getReqType());
    response.setRecords(new ArrayList<ResponseRecord>());

    for (USMassUpdateWorker worker : workers) {
      if (worker != null) {
        if (worker.getResponse().getRecords() != null) {
          response.getRecords().addAll(worker.getResponse().getRecords());
        }
        String status = worker.getResponse().getStatus();
        if (CmrConstants.RDC_STATUS_ABORTED.equals(status) || CmrConstants.RDC_STATUS_NOT_COMPLETED.equals(status)) {
          response.setStatus(CmrConstants.RDC_STATUS_ABORTED);
          response.setMsg(worker.getResponse().getMsg());
        }
      }
    }
    if (StringUtils.isBlank(response.getStatus())) {
      response.setStatus(CmrConstants.RDC_STATUS_COMPLETED);
    }

    LOG.debug("Total records from response: " + response.getRecords().size());
    // try this
    // get the results from the service and process jason response
    try {
      // update MASS_UPDT table with the error txt and row status cd
      for (ResponseRecord record : response.getRecords()) {
        PreparedQuery updtQuery = new PreparedQuery(entityManager, ExternalizedQuery.getSql("BATCH.GET_MASS_UPDT_ENTITY"));
        updtQuery.setParameter("REQ_ID", reqId);
        updtQuery.setParameter("ITERATION_ID", iterationId);
        updtQuery.setParameter("CMR_NO", record.getCmrNo());
        List<MassUpdt> updateList = updtQuery.getResults(MassUpdt.class);

        for (MassUpdt massUpdt : updateList) {
          massUpdt.setErrorTxt(record.getMessage());
          if (CmrConstants.RDC_STATUS_NOT_COMPLETED.equals(record.getStatus())) {
            massUpdt.setRowStatusCd("RDCER");
          } else if (CmrConstants.RDC_STATUS_COMPLETED.equals(record.getStatus())) {
            massUpdt.setRowStatusCd("DONE");
          }
          LOG.info("Mass Update Record Updated [Request ID: " + massUpdt.getId().getParReqId() + " CMR_NO: " + massUpdt.getCmrNo() + " SEQ No: "
              + massUpdt.getId().getSeqNo() + "]");
          updateEntity(massUpdt, entityManager);
        }
      }
      String resultCode = response.getStatus();

      String requestType = "";
      String action = "";
      switch (admin.getReqType()) {
      case "D":
        action = ACTION_RDC_DELETE;
        requestType = "Mass Delete";
        break;
      case "R":
        action = ACTION_RDC_REACTIVATE;
        requestType = "Mass Reactivate";
        break;
      case "X":
        action = ACTION_RDC_SINGLE_REACTIVATE;
        requestType = "Single Reactivate";
        break;
      case "M":
        action = ACTION_RDC_MASS_UPDATE;
        requestType = "Mass Update";
        break;
      }

      // create comment log and workflow history entries for update type
      // of
      // request
      StringBuilder comment = new StringBuilder();
      if (isCompletedSuccessfully(resultCode)) {
        comment = comment.append(requestType + " in RDc successfully completed.");
      } else {
        if (CmrConstants.RDC_STATUS_ABORTED.equals(resultCode) && CmrConstants.RDC_STATUS_ABORTED.equals(processingStatus)) {
          comment = comment.append(requestType + " in RDc failed: " + response.getMsg());
        } else if (CmrConstants.RDC_STATUS_ABORTED.equalsIgnoreCase(resultCode)) {
          comment = comment.append(requestType + " in RDc aborted: " + response.getMsg() + "\nSystem will retry once.");
        } else if (CmrConstants.RDC_STATUS_NOT_COMPLETED.equalsIgnoreCase(resultCode)) {
          comment = comment.append(requestType + " in RDc failed: " + response.getMsg());
        } else if (CmrConstants.RDC_STATUS_IGNORED.equalsIgnoreCase(resultCode)) {
          comment = comment.append(requestType + " in RDc skipped: " + response.getMsg());
        }
      }

      if (!CmrConstants.RDC_STATUS_IGNORED.equals(resultCode)) {
        RequestUtils.createWorkflowHistoryFromBatch(entityManager, BATCH_USER_ID, admin, comment.toString().trim(), action, null, null,
            "COM".equals(admin.getReqStatus()));
      }
      RequestUtils.createCommentLogFromBatch(entityManager, BATCH_USER_ID, admin.getId().getReqId(), comment.toString().trim());

      // only update Admin record once depending on the overall status of
      // the
      // request
      LOG.debug("Updating Admin record for Request ID " + admin.getId().getReqId());
      if (CmrConstants.RDC_STATUS_ABORTED.equals(resultCode) && CmrConstants.RDC_STATUS_ABORTED.equals(admin.getRdcProcessingStatus())) {
        admin.setRdcProcessingStatus(CmrConstants.RDC_STATUS_NOT_COMPLETED);
      } else {
        admin.setRdcProcessingStatus(resultCode);
      }
      admin.setRdcProcessingTs(SystemUtil.getCurrentTimestamp());
      admin.setRdcProcessingMsg(response.getMsg());
      updateEntity(admin, entityManager);
      LOG.debug("Request ID " + admin.getId().getReqId() + " Status: " + admin.getRdcProcessingStatus() + " Message: " + admin.getRdcProcessingMsg());

      partialCommit(entityManager);

    } catch (Exception e) {
      LOG.error("Error in processing Mass Change Request  " + admin.getId().getReqId(), e);
      addError("Mass Change Request " + admin.getId().getReqId() + " Error: " + e.getMessage());
    }

  }

  /**
   * Processes Request Type 'E'
   * 
   * @param entityManager
   * @param admin
   * @param data
   * @throws JsonGenerationException
   * @throws JsonMappingException
   * @throws IOException
   * @throws Exception
   */
  public void processUpdateByEnterprise(EntityManager entityManager, Admin admin, Data data)
      throws JsonGenerationException, JsonMappingException, IOException, Exception {
    try {
      String resultCode = null;
      String currentProcessingStatus = admin.getRdcProcessingStatus();

      LOG.info("Processing Enterprise Record " + admin.getId().getReqId() + " [Request ID: " + admin.getId().getReqId() + "]");

      PreparedQuery query = new PreparedQuery(entityManager, ExternalizedQuery.getSql("BATCH.MASS_UPDATE_DATA_RECORDS"));
      query.setParameter("REQ_ID", admin.getId().getReqId());
      query.setParameter("ITERATION_ID", admin.getIterationId());
      query.setParameter("STATUS", "PASS");

      List<CompoundEntity> results = query.getCompundResults(Admin.class, Admin.BATCH_UPDATE_DATA_MAPPING);

      MassUpdtData enterpriseData = results != null && results.size() > 0 ? results.get(0).getEntity(MassUpdtData.class) : null;
      if (enterpriseData == null) {
        LOG.warn("Mass Update Data cannot be retrieved for Request ID " + admin.getId().getReqId() + " Iteration " + admin.getIterationId());
      } else {
        EnterpriseUpdtRequest enterpriseUpdtRequest = prepareUpdtByEnterpriseRequest(entityManager, admin, data, enterpriseData);

        LOG.trace("Request JSON:");
        if (LOG.isTraceEnabled()) {
          DebugUtil.printObjectAsJson(LOG, enterpriseUpdtRequest);
        }

        EnterpriseUpdtResponse response = null;

        long startTime = new Date().getTime();
        try {
          this.updtByEntClient.setConnectTimeout(60 * 1000); // 1 min
          // to
          // connect
          this.updtByEntClient.setReadTimeout(60 * 30 * 1000); // 30
          // min
          // to
          // finish
          response = this.updtByEntClient.executeAndWrap(enterpriseUpdtRequest, EnterpriseUpdtResponse.class);
        } catch (Exception e) {
          LOG.error("Error when connecting to the enterprise service.", e);
          response = new EnterpriseUpdtResponse();
          response.setReqId(admin.getId().getReqId());
          long endTime = new Date().getTime();
          if (endTime - startTime > 1000 * 60 * 25) {
            LOG.debug("Wait time is more than 25 mins, setting status to completed.");
            response.setStatus(CmrConstants.RDC_STATUS_COMPLETED_WITH_WARNINGS);
            response.setMessage("Bulk process. Processing will complete asynchronously");
            // this thread has been waiting more than 50 mins, means
            // backend
            // processing is being completed
          } else {
            response.setStatus(CmrConstants.RDC_STATUS_ABORTED);
            response.setMessage("A system error has occured. Setting to aborted.");
          }
        }

        resultCode = response.getStatus();

        LOG.trace("Response JSON:");
        if (LOG.isTraceEnabled()) {
          DebugUtil.printObjectAsJson(LOG, response);
        }
        LOG.info("Response received from Enterprise Service [Request ID: " + response.getReqId() + " Status: " + response.getStatus() + " Message: "
            + (response.getMessage() != null ? response.getMessage() : "-") + "]");

        StringBuilder comment = new StringBuilder();

        // create comment log and workflow history entries for update
        // type of
        // request
        if (isCompletedSuccessfully(resultCode)) {

          comment = comment.append("Update by Enterprise in RDc successfully completed.");
          if (CmrConstants.RDC_STATUS_COMPLETED_WITH_WARNINGS.equals(resultCode)) {
            comment = comment.append("\nWarning Message: " + response.getMessage());
          }

        } else {
          if (CmrConstants.RDC_STATUS_ABORTED.equals(resultCode) && CmrConstants.RDC_STATUS_ABORTED.equals(currentProcessingStatus)) {
            comment = comment.append("Update by Enterprise in RDc failed. Error: " + response.getMessage());
          } else if (CmrConstants.RDC_STATUS_ABORTED.equalsIgnoreCase(resultCode)) {
            comment = comment.append("Update by Enterprise in RDc failed. Error: " + response.getMessage() + " System will retry processing once.");
          } else if (CmrConstants.RDC_STATUS_NOT_COMPLETED.equalsIgnoreCase(resultCode)) {
            comment = comment.append("Update by Enterprise in RDc failed. Error: " + response.getMessage());
          } else if (CmrConstants.RDC_STATUS_IGNORED.equalsIgnoreCase(resultCode)) {
            comment = comment.append("Update by Enterprise in RDc skipped: " + response.getMessage());
          }
        }

        updateMassUpdateRowStatus(resultCode, entityManager, response.getReqId());

        if (!CmrConstants.RDC_STATUS_IGNORED.equals(resultCode)) {
          RequestUtils.createWorkflowHistoryFromBatch(entityManager, BATCH_USER_ID, admin, comment.toString().trim(), ACTION_RDC_UPDT_BY_ENT, null,
              null, "COM".equals(admin.getReqStatus()));
        }
        RequestUtils.createCommentLogFromBatch(entityManager, BATCH_USER_ID, admin.getId().getReqId(), comment.toString().trim());

        // only update Admin record once depending on the overall status
        // of the
        // request
        LOG.debug("Updating Admin record for Request ID " + admin.getId().getReqId());
        if (CmrConstants.RDC_STATUS_ABORTED.equals(resultCode) && CmrConstants.RDC_STATUS_ABORTED.equals(admin.getRdcProcessingStatus())) {
          admin.setRdcProcessingStatus(CmrConstants.RDC_STATUS_NOT_COMPLETED);
        } else {
          admin.setRdcProcessingStatus(resultCode);
        }
        admin.setRdcProcessingTs(SystemUtil.getCurrentTimestamp());
        admin.setRdcProcessingMsg(response.getMessage());
        updateEntity(admin, entityManager);
        LOG.debug(
            "Request ID " + admin.getId().getReqId() + " Status: " + admin.getRdcProcessingStatus() + " Message: " + admin.getRdcProcessingMsg());

        partialCommit(entityManager);

      }
    } catch (Exception e) {
      LOG.error("Error in processing Enterprise Record " + admin.getId().getReqId() + " for Request ID " + admin.getId().getReqId(), e);
      addError("Update By Enterprise Request " + admin.getId().getReqId() + " Error: " + e.getMessage());
    }
  }

  /**
   * Processes Request Type 'C'
   * 
   * @param entityManager
   * @param request
   * @param admin
   * @param data
   * @throws Exception
   */
  protected void processCreateRequest(EntityManager entityManager, ProcessRequest request, Admin admin, Data data) throws Exception {
    // try to convert the request to Legal CMR conversion

    if (this.deleteRDcTargets) {
      deleteRDcTargetRecords(entityManager, data.getCmrNo(), data.getCmrIssuingCntry());
    }
    convertToProspectToLegalCMRInput(request, entityManager, request.getReqId());

    boolean prospectConversion = !StringUtils.isBlank(request.getProspectCMRNo());
    LOG.info("Sending request to Process Service [Request ID: " + request.getReqId() + " CMR No: " + request.getCmrNo() + " Type: "
        + request.getReqType() + "]");

    String processingStatus = admin.getRdcProcessingStatus();
    String resultCode = null;

    // call the create cmr service
    LOG.trace("Request JSON:");
    if (LOG.isTraceEnabled()) {
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
        this.serviceClient.setReadTimeout(60 * 20 * 1000); // 20 mins
        response = this.serviceClient.executeAndWrap(applicationId, request, ProcessResponse.class);
      } catch (Exception e) {
        LOG.error("Error when connecting to the service.", e);
        response = new ProcessResponse();
        response.setReqId(admin.getId().getReqId());
        response.setStatus(CmrConstants.RDC_STATUS_ABORTED);
        response.setMessage("Cannot connect to the service at the moment.");
      }
    }

    if (response.getReqId() <= 0) {
      response.setReqId(request.getReqId());
    }
    resultCode = response.getStatus();
    LOG.trace("Response JSON:");
    if (LOG.isTraceEnabled()) {
      DebugUtil.printObjectAsJson(LOG, response);
    }

    LOG.info("Response received from Process Service [Request ID: " + response.getReqId() + " CMR No: " + response.getCmrNo() + " Status: "
        + response.getStatus() + " Message: " + (response.getMessage() != null ? response.getMessage() : "-") + "]");

    // get the results from the service and process jason response
    try {

      if (isCompletedSuccessfully(resultCode)) {

        if (response.getRecords() != null) {
          for (RDcRecord record : response.getRecords()) {

            // update the ADDR Table for each rdc record returned by
            // the
            // create cmr service for create request

            updateRequestAddress(entityManager, admin, data, record);

          }
        }
      }

      StringBuilder comment = new StringBuilder();

      // create comment log and workflow history entries for update type
      // of
      // request
      if (isCompletedSuccessfully(resultCode)) {

        setLAAdminToCompleted(admin, data);
        if (response.isCmrNoGenerated() && !StringUtils.isEmpty(response.getCmrNo())) {
          LOG.debug("CMR No. " + response.getCmrNo() + " generated. Updating DATA for Request " + data.getId().getReqId());
          data.setCmrNo(response.getCmrNo());
          updateEntity(data, entityManager);
          String cmrNoCmt = "CMR No. " + response.getCmrNo() + " generated for this request.";
          RequestUtils.createCommentLogFromBatch(entityManager, BATCH_USER_ID, admin.getId().getReqId(), cmrNoCmt);
        }

        if (response.getRecords() != null) {

          if (prospectConversion) {
            comment = comment.append("RDc processing converted prospect " + request.getProspectCMRNo() + " to KUNNR(s): ");
          } else {
            comment = comment.append("RDc processing successfully created KUNNR(s): ");
          }
          if (response.getRecords() != null && response.getRecords().size() != 0) {
            for (int i = 0; i < response.getRecords().size(); i++) {
              comment = comment.append(response.getRecords().get(i).getSapNo() + " ");
            }
          }
          if (CmrConstants.RDC_STATUS_COMPLETED_WITH_WARNINGS.equals(resultCode)) {
            comment = comment.append("\nWarning Message: " + response.getMessage());
          }
        } else {
          comment.append("RDc records were not processed.");
          if (CmrConstants.RDC_STATUS_COMPLETED_WITH_WARNINGS.equals(resultCode)) {
            comment = comment.append("\nWarning Message: " + response.getMessage());
          }
        }

      } else {
        if (CmrConstants.RDC_STATUS_ABORTED.equals(resultCode) && CmrConstants.RDC_STATUS_ABORTED.equals(processingStatus)) {
          comment = comment.append("RDc create processing for CMR No " + request.getCmrNo() + " failed. Error: " + response.getMessage());
        } else if (CmrConstants.RDC_STATUS_ABORTED.equalsIgnoreCase(resultCode)) {
          comment = comment.append("RDc create processing for CMR No " + request.getCmrNo() + " failed. Error: " + response.getMessage()
              + " System will retry processing once.");
        } else if (CmrConstants.RDC_STATUS_NOT_COMPLETED.equalsIgnoreCase(resultCode)) {
          setLAAdminToPending(admin, data);
          comment = comment.append("RDc create processing for CMR No " + request.getCmrNo() + " failed. Error: " + response.getMessage());
        } else if (CmrConstants.RDC_STATUS_IGNORED.equalsIgnoreCase(resultCode)) {
          comment = comment.append("Create processing in RDc skipped: " + response.getMessage());
        }
        if (!CmrConstants.RDC_STATUS_IGNORED.equals(resultCode)) {
          SlackAlertsUtil.recordGenericAlert("TransConn", "Request " + admin.getId().getReqId(),
              SlackAlertsUtil.bold("Processing Result: " + resultCode + ", Message: " + response.getMessage()));
        }

      }

      // only update Admin record once depending on the overall status of
      // the
      // request
      LOG.debug("Updating Admin record for Request ID " + admin.getId().getReqId());

      if (CmrConstants.RDC_STATUS_ABORTED.equals(resultCode) && CmrConstants.RDC_STATUS_ABORTED.equals(admin.getRdcProcessingStatus())) {
        admin.setRdcProcessingStatus(CmrConstants.RDC_STATUS_NOT_COMPLETED);
      } else {
        admin.setRdcProcessingStatus(resultCode);
      }
      admin.setRdcProcessingTs(SystemUtil.getCurrentTimestamp());
      admin.setRdcProcessingMsg(response.getMessage());
      if (!CmrConstants.RDC_STATUS_IGNORED.equals(resultCode)) {
        handleMailsForResult(entityManager, resultCode, admin, comment.toString());
      }
      RequestUtils.createCommentLogFromBatch(entityManager, BATCH_USER_ID, admin.getId().getReqId(), comment.toString().trim());

      updateEntity(admin, entityManager);
      LOG.debug("Request ID " + admin.getId().getReqId() + " Status: " + admin.getRdcProcessingStatus() + " Message: " + admin.getRdcProcessingMsg());

      partialCommit(entityManager);

      /*
       * Defect 1450224 - TransCon batch issue when writing Tax Separation Indc
       * on CROS
       */
      // create a flag to note if at least one tax separation indc is not
      // empty
      boolean oneTaxInfoNotEmpty = false;
      if (SystemLocation.CHILE.equals(data.getCmrIssuingCntry())) {
        // query all tax separation indicators on GEO_TAX_INFO for the
        // request
        LOG.debug("***QUERYING DB FOR TAX INFO!!!");
        List<Object[]> geoTaxInfo = getGeoTaxInfo(entityManager, String.valueOf(data.getId().getReqId()));
        LOG.debug("***GOT RESULTS >> " + geoTaxInfo != null ? geoTaxInfo.size() : geoTaxInfo);
        if (geoTaxInfo != null && geoTaxInfo.size() != 0) {
          for (int i = 0; i < geoTaxInfo.size(); i++) {
            Object[] singleInfo = geoTaxInfo.get(i);
            LOG.debug("***GOT RESULTS AT INDEX " + i + ", TAX SEPARATION INDC >> " + singleInfo[1]);
            if (singleInfo != null && !StringUtils.isEmpty(String.valueOf(singleInfo[1]))) {
              oneTaxInfoNotEmpty = true;
              break;
            }
          }
        } else {
          oneTaxInfoNotEmpty = false;
        }
        LOG.debug("***oneTaxInfoNotEmpty is finally >> " + oneTaxInfoNotEmpty);
      }

      // process force updates if needed
      // changes to force the type to be update and send back to tc
      if ((SystemLocation.UNITED_STATES.equals(data.getCmrIssuingCntry()) || isNotInternalAndChile(data, admin) || isInternalAndSSAMX(data, admin))
          && !CmrConstants.RDC_STATUS_ABORTED.equals(admin.getRdcProcessingStatus())
          && CmrConstants.REQUEST_STATUS.COM.toString().equals(admin.getReqStatus()) && !"Y".equals(admin.getDisableAutoProc())) {
        forceChangeToUpdate(entityManager, admin, data, oneTaxInfoNotEmpty);
      }

    } catch (Exception e) {
      LOG.error("Error in processing Create Request " + admin.getId().getReqId(), e);
      addError("Create Request " + admin.getId().getReqId() + " Error: " + e.getMessage());
    }
  }

  /**
   * Based on the resulting {@link RDcRecord}, updates the address on the
   * request and sets sapNo and ierpSitePrtyId
   * 
   * @param entityManager
   * @param admin
   * @param data
   * @param record
   */
  protected void updateRequestAddress(EntityManager entityManager, Admin admin, Data data, RDcRecord record) {
    PreparedQuery addrQuery = new PreparedQuery(entityManager, ExternalizedQuery.getSql("BATCH.GET_ADDR_ENTITY_CREATE_REQ"));
    addrQuery.setParameter("REQ_ID", admin.getId().getReqId());

    if ("897".equals(data.getCmrIssuingCntry())) {
      if ("ZP01".equals(record.getAddressType()) && record.getSeqNo() != null && Integer.parseInt(record.getSeqNo()) >= 200) {
        // If additional bill to handle accordingly
        addrQuery = new PreparedQuery(entityManager, ExternalizedQuery.getSql("BATCH.GET_ADDR_ENTITY_CREATE_REQ_SEQ"));
        addrQuery.setParameter("REQ_ID", admin.getId().getReqId());
        addrQuery.setParameter("ADDR_TYPE", "PG01");
        addrQuery.setParameter("ADDR_SEQ", record.getSeqNo());
      } else {
        // if returned is ZS01/ZI01, update the ZS01 address. Else,
        // Update
        // the ZI01 address
        addrQuery.setParameter("ADDR_TYPE", "ZS01".equals(record.getAddressType()) || "ZI01".equals(record.getAddressType()) ? "ZS01" : "ZI01");
      }
    } else if ("616".equals(data.getCmrIssuingCntry()) || "796".equals(data.getCmrIssuingCntry())) {
      if ("ZP01".equals(record.getAddressType()) && record.getSeqNo() != null && Integer.parseInt(record.getSeqNo()) = 200) {
        // If additional bill to handle accordingly
        addrQuery = new PreparedQuery(entityManager, ExternalizedQuery.getSql("BATCH.GET_ADDR_ENTITY_CREATE_REQ_SEQ"));
        addrQuery.setParameter("REQ_ID", admin.getId().getReqId());
        addrQuery.setParameter("ADDR_TYPE", "PG01");
        addrQuery.setParameter("ADDR_SEQ", record.getSeqNo());
      } else {
        // if returned is ZS01/ZI01, update the ZS01 address. Else,
        // Update
        // the ZI01 address
        addrQuery.setParameter("ADDR_TYPE", "ZS01".equals(record.getAddressType()) || "ZI01".equals(record.getAddressType()) ? "ZS01" : "ZI01");
      }
    } else {
      addrQuery.setParameter("ADDR_TYPE", record.getAddressType());
    }

    List<Addr> addrList = addrQuery.getResults(Addr.class);
    for (Addr addr : addrList) {
      addr.setSapNo(record.getSapNo());
      addr.setIerpSitePrtyId(record.getIerpSitePartyId());
      addr.setRdcCreateDt(record.getCreateDate());
      addr.setRdcLastUpdtDt(SystemUtil.getCurrentTimestamp());
      LOG.info("Address Record Updated [Request ID: " + addr.getId().getReqId() + " Type: " + addr.getId().getAddrType() + " SAP No: "
          + record.getSapNo() + "]");
      updateEntity(addr, entityManager);
    }
  }

  /**
   * Handles the action to be done when a non-I status is returned
   * 
   * @param entityManager
   * @param admin
   * @param comment
   * @throws SQLException
   * @throws CmrException
   */
  protected void handleMailsForResult(EntityManager entityManager, String resultCode, Admin admin, String comment) throws CmrException, SQLException {
    RequestUtils.createWorkflowHistoryFromBatch(entityManager, BATCH_USER_ID, admin, comment, ACTION_RDC_CREATE, null, null,
        "COM".equals(admin.getReqStatus()));
  }

  /**
   * Validates whether request is NOT "INTER","INTOU","INTUS","INIBM" and is a
   * CHILE request
   * 
   * @param data
   * @param admin
   */

  protected boolean isNotInternalAndChile(Data data, Admin admin) {
    boolean ret = false;

    if (SystemLocation.CHILE.equals(data.getCmrIssuingCntry()) && !CmrConstants.SSAMX_INTERNAL_TYPES.contains(admin.getCustType())) {
      ret = true;
    }

    return ret;
  }

  /**
   * Validates whether request is "INTER","INTOU","INTUS","INIBM" and is an
   * SSA/MX country
   * 
   * @param data
   * @param admin
   */
  protected boolean isInternalAndSSAMX(Data data, Admin admin) {
    boolean ret = false;

    if ((LAHandler.isSSAIssuingCountry(data.getCmrIssuingCntry()) || LAHandler.isMXIssuingCountry(data.getCmrIssuingCntry()))
        && CmrConstants.SSAMX_INTERNAL_TYPES.contains(admin.getCustType())) {
      ret = true;
    }

    return ret;
  }

  /**
   * Processes Request Type 'U'
   * 
   * @param entityManager
   * @param request
   * @param admin
   * @param data
   * @throws Exception
   */
  protected void processUpdateRequest(EntityManager entityManager, ProcessRequest request, Admin admin, Data data) throws Exception {

    String resultCode = null;
    String processingStatus = admin.getRdcProcessingStatus() != null ? admin.getRdcProcessingStatus() : "";
    long reqId = admin.getId().getReqId();
    RequestChangeContainer changes = new RequestChangeContainer(entityManager, data.getCmrIssuingCntry(), admin, reqId);

    try {

      // code to retrieve the sap no list for a req id so that sap no can
      // be
      // passed as input
      String sql = ExternalizedQuery.getSql("BATCH.GET_ADDR_FOR_SAP_NO");
      PreparedQuery query = new PreparedQuery(entityManager, sql);
      query.setParameter("REQ_ID", admin.getId().getReqId());
      List<Addr> addresses = query.getResults(Addr.class);
      // for each address on the request, execute

      StringBuilder rdcProcessingMsg = new StringBuilder();
      List<String> statusCodes = new ArrayList<String>();

      boolean isDataUpdated = false;
      boolean continueUpdate = false;
      if (changes != null && changes.hasDataChanges()) {
        isDataUpdated = true;
      }
      StringBuilder comment = new StringBuilder();

      boolean ignoreWfhistory = false;
      for (Addr addr : addresses) {

        if (isDataUpdated || "Y".equals(addr.getChangedIndc()) || "N".equals(addr.getImportInd()) || "Y".equals(addr.getImportInd())
            || "D".equals(addr.getImportInd()) || StringUtils.isNotBlank(addr.getId().getAddrType())) {
          continueUpdate = true;
        }

        if (continueUpdate) {
          request.setSapNo(addr.getSapNo());

          request.setAddrType(addr.getId().getAddrType());

          request.setSeqNo(addr.getId().getAddrSeq());

          // call the create cmr service
          LOG.info("Sending request to Process Service [Request ID: " + request.getReqId() + " CMR No: " + request.getCmrNo() + " Type: "
              + request.getReqType() + " SAP No: " + request.getSapNo() + "]");

          LOG.trace("Request JSON:");
          if (LOG.isTraceEnabled()) {
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
              this.serviceClient.setReadTimeout(60 * 20 * 1000); // 20
              // mins
              response = this.serviceClient.executeAndWrap(applicationId, request, ProcessResponse.class);
            } catch (Exception e) {
              LOG.error("Error when connecting to the service.", e);
              response = new ProcessResponse();
              response.setReqId(admin.getId().getReqId());
              response.setStatus(CmrConstants.RDC_STATUS_ABORTED);
              response.setMessage("Cannot connect to the service at the moment.");
            }
          }

          if (response.getReqId() <= 0) {
            response.setReqId(request.getReqId());
          }

          resultCode = response.getStatus();
          if (StringUtils.isBlank(resultCode)) {
            statusCodes.add(CmrConstants.RDC_STATUS_NOT_COMPLETED);
          } else {
            statusCodes.add(resultCode);
          }

          LOG.trace("Response JSON:");
          if (LOG.isTraceEnabled()) {
            DebugUtil.printObjectAsJson(LOG, response);
          }

          LOG.info("Response received from Process Service [Request ID: " + response.getReqId() + " CMR No: " + response.getCmrNo() + " KUNNR: "
              + addr.getSapNo() + " Status: " + response.getStatus() + " Message: " + (response.getMessage() != null ? response.getMessage() : "-")
              + "]");

          // get the results from the service and process jason
          // response

          // create comment log and workflow history entries for
          // update type of
          // request
          if (isCompletedSuccessfully(resultCode)) {

            addr.setRdcLastUpdtDt(SystemUtil.getCurrentTimestamp());

            if (response.getRecords() != null) {
              comment = comment.append("\nRDc processing successfully updated KUNNR(s): ");
              if (response.getRecords() != null && response.getRecords().size() != 0) {
                for (int i = 0; i < response.getRecords().size(); i++) {
                  comment = comment.append(response.getRecords().get(i).getSapNo() + " ");
                  if (StringUtils.isBlank(addr.getSapNo())) {
                    addr.setSapNo(response.getRecords().get(i).getSapNo());
                  }
                  if (StringUtils.isBlank(addr.getIerpSitePrtyId())) {
                    addr.setIerpSitePrtyId(response.getRecords().get(i).getIerpSitePartyId());
                  }
                }
              }
              if (CmrConstants.RDC_STATUS_COMPLETED_WITH_WARNINGS.equals(resultCode)) {
                comment = comment.append("\nWarning Message: " + response.getMessage());
                rdcProcessingMsg.append("Warning: " + response.getMessage() + "[KUNNR:" + addr.getSapNo() + "]\n");
              }
            } else {
              comment.append("RDc records were not processed.");
              if (CmrConstants.RDC_STATUS_COMPLETED_WITH_WARNINGS.equals(resultCode)) {
                comment = comment.append("\nWarning Message: " + response.getMessage());
              }
            }

            updateEntity(addr, entityManager);
          } else {
            if (CmrConstants.RDC_STATUS_ABORTED.equals(resultCode) && CmrConstants.RDC_STATUS_ABORTED.equals(processingStatus)) {
              comment = comment.append("\nRDc update processing for KUNNR " + (request.getSapNo() != null ? request.getSapNo() : "(not generated)")
                  + " failed. Error: " + response.getMessage() + "\n");
            } else if (CmrConstants.RDC_STATUS_ABORTED.equalsIgnoreCase(resultCode)) {
              comment = comment.append("\nRDc update processing for KUNNR " + (request.getSapNo() != null ? request.getSapNo() : "(not generated)")
                  + " failed. Error: " + response.getMessage() + " System will retry processing once.\n");
            } else if (CmrConstants.RDC_STATUS_NOT_COMPLETED.equalsIgnoreCase(resultCode)) {
              comment = comment.append("\nRDc update processing for KUNNR " + (request.getSapNo() != null ? request.getSapNo() : "(not generated)")
                  + " failed. Error: " + response.getMessage() + "\n");
            } else if (CmrConstants.RDC_STATUS_IGNORED.equalsIgnoreCase(resultCode)) {
              comment = comment.append("\nUpdate processing in RDc skipped: " + response.getMessage() + "\n");
            }
            rdcProcessingMsg.append("Error: " + response.getMessage() + "[KUNNR:" + addr.getSapNo() + "]\n");
            if (!CmrConstants.RDC_STATUS_IGNORED.equals(resultCode)) {
              SlackAlertsUtil.recordGenericAlert("TransConn", "Request " + admin.getId().getReqId(),
                  SlackAlertsUtil.bold("Processing Result: " + resultCode + ", Message: " + response.getMessage()));
            }
          }

          if (!CmrConstants.RDC_STATUS_IGNORED.equals(resultCode)) {
            // RequestUtils.createWorkflowHistoryFromBatch(entityManager,
            // BATCH_USER_ID, admin, comment.toString().trim(),
            // ACTION_RDC_UPDATE, null,
            // null, "COM".equals(admin.getReqStatus()));
          } else {
            continueUpdate = false;
            ignoreWfhistory = true;
          }
          keepAlive();
        }
      }

      RequestUtils.createCommentLogFromBatch(entityManager, BATCH_USER_ID, reqId, comment.toString().trim());

      LOG.debug("Updating Admin record for Request ID " + admin.getId().getReqId());

      // only update Admin record once depending on the overall status of
      // the
      // request
      if (statusCodes.contains(CmrConstants.RDC_STATUS_NOT_COMPLETED)) {
        admin.setRdcProcessingStatus(CmrConstants.RDC_STATUS_NOT_COMPLETED);
      } else if (statusCodes.contains(CmrConstants.RDC_STATUS_ABORTED)) {
        admin.setRdcProcessingStatus(
            processingStatus.equals(CmrConstants.RDC_STATUS_ABORTED) ? CmrConstants.RDC_STATUS_NOT_COMPLETED : CmrConstants.RDC_STATUS_ABORTED);
      } else if (statusCodes.contains(CmrConstants.RDC_STATUS_COMPLETED_WITH_WARNINGS)) {
        admin.setRdcProcessingStatus(CmrConstants.RDC_STATUS_COMPLETED_WITH_WARNINGS);
      } else {
        admin.setRdcProcessingStatus(CmrConstants.RDC_STATUS_COMPLETED);
      }

      if ("N".equals(admin.getRdcProcessingStatus()) || "A".equals(admin.getRdcProcessingStatus())) {
        rdcProcessingMsg = new StringBuilder("Some errors occurred during processing. Please check request's comment log for details.");
      } else {
        rdcProcessingMsg = new StringBuilder("Processing has been completed. Please check request's comment log for details.");
      }

      admin.setRdcProcessingTs(SystemUtil.getCurrentTimestamp());
      admin.setRdcProcessingMsg(rdcProcessingMsg.toString().trim());
      updateEntity(admin, entityManager);

      if ("N".equals(admin.getRdcProcessingStatus()) || "A".equals(admin.getRdcProcessingStatus())) {
        RequestUtils.createWorkflowHistoryFromBatch(entityManager, BATCH_USER_ID, admin,
            "Some errors occurred during processing. Please check request's comment log for details.", ACTION_RDC_UPDATE, null, null,
            "COM".equals(admin.getReqStatus()));
      } else {
        if (!ignoreWfhistory) {
          RequestUtils.createWorkflowHistoryFromBatch(entityManager, BATCH_USER_ID, admin,
              "Processing has been completed. Please check request's comment log for details.", ACTION_RDC_UPDATE, null, null,
              "COM".equals(admin.getReqStatus()));
        }
      }

      partialCommit(entityManager);
      LOG.debug("Request ID " + admin.getId().getReqId() + " Status: " + admin.getRdcProcessingStatus() + " Message: " + admin.getRdcProcessingMsg());

      // process revert to Create if needed
      if ((SystemLocation.UNITED_STATES.equals(data.getCmrIssuingCntry()) || isNotInternalAndChile(data, admin) || isInternalAndSSAMX(data, admin))
          && !CmrConstants.RDC_STATUS_ABORTED.equals(admin.getRdcProcessingStatus())
          && CmrConstants.REQUEST_STATUS.COM.toString().equals(admin.getReqStatus())) {
        revertToCreate(entityManager, admin);
      }

    } catch (Exception e) {
      LOG.error("Error in processing Update Request " + admin.getId().getReqId(), e);
      addError("Update Request " + admin.getId().getReqId() + " Error: " + e.getMessage());
    }

  }

  /**
   * Forces a Create request to be changed to update
   * 
   * @param entityManager
   * @param reqId
   * @throws CmrException
   * @throws SQLException
   */
  protected void forceChangeToUpdate(EntityManager entityManager, Admin admin, Data data, boolean oneTaxInfoNotEmpty)
      throws CmrException, SQLException {
    LOG.info("Forced Update : Checking type of completed request...");

    long reqId = admin.getId().getReqId();
    String user = BATCH_USER_ID;
    AppUser dummyuser = new AppUser();
    dummyuser.setIntranetId(user);
    dummyuser.setBluePagesName(user);
    String issuingCntry = data.getCmrIssuingCntry();

    if (SystemLocation.UNITED_STATES.equals(issuingCntry) && !StringUtils.isBlank(data.getCmrNo())
        && (!StringUtils.isBlank(data.getAffiliate()) || !StringUtils.isBlank(data.getEnterprise()) || !StringUtils.isBlank(data.getIccTaxClass())
            || !StringUtils.isBlank(data.getIccTaxExemptStatus()) || !StringUtils.isBlank(data.getNonIbmCompanyInd())
            || !StringUtils.isBlank(data.getUsSicmen()) || !StringUtils.isBlank(data.getSpecialTaxCd()) || isT2PoolCMRRequest(admin, data))) {
      LOG.info("Required update needed for Request " + reqId + " - Force to Update");

      admin.setReqStatus(CmrConstants.REQUEST_STATUS.PCP.toString());
      admin.setInternalTyp("UPD_SIMPLE_AUTO");
      admin.setLockInd(CmrConstants.YES_NO.N.toString());
      admin.setLockBy(null);
      admin.setLockByNm(null);
      admin.setLockTs(null);
      admin.setReqType(CmrConstants.REQ_TYPE_UPDATE);
      admin.setProcessedFlag(CmrConstants.YES_NO.N.toString());
      admin.setProcessedTs(null);
      updateEntity(admin, entityManager);

      if (isT2PoolCMRRequest(admin, data)) {
        LOG.info("Blanking out BP Acc Type and BP Name after CMR creation for BP T2 Pool request");
        data.setBpAcctTyp("");
        data.setBpName("");
        updateEntity(data, entityManager);
      }

      RequestUtils.createWorkflowHistoryFromBatch(entityManager, BATCH_USER_ID, admin, FORCED_UPDATE_COMMENT, FORCED_CHANGE_ACTION, null, null,
          "COM".equals(admin.getReqStatus()));
      RequestUtils.createCommentLogFromBatch(entityManager, BATCH_USER_ID, reqId, FORCED_UPDATE_COMMENT);

      partialCommit(entityManager);

    } else if (isNotInternalAndChile(data, admin) && oneTaxInfoNotEmpty) {
      LOG.debug("Forced Update : EXECUTING FORCE UPDATE CHANGE FOR CHILE...");
      // if this is Chile we need to get the tax info from GEO_TAX_INFO
      admin.setReqStatus(CmrConstants.REQUEST_STATUS.PCP.toString());
      admin.setInternalTyp("UPDT_AUTO");
      admin.setLockInd(CmrConstants.YES_NO.N.toString());
      admin.setLockBy(null);
      admin.setLockByNm(null);
      admin.setLockTs(null);
      admin.setReqType(CmrConstants.REQ_TYPE_UPDATE);
      admin.setProcessedFlag(CmrConstants.YES_NO.N.toString());
      admin.setProcessedTs(null);
      updateEntity(admin, entityManager);

      RequestUtils.createWorkflowHistoryFromBatch(entityManager, BATCH_USER_ID, admin, FORCED_UPDATE_COMMENT, FORCED_CHANGE_ACTION, null, null,
          "COM".equals(admin.getReqStatus()));
      RequestUtils.createCommentLogFromBatch(entityManager, BATCH_USER_ID, reqId, FORCED_UPDATE_COMMENT);

      partialCommit(entityManager);
    } else if (isInternalAndSSAMX(data, admin)) {
      LOG.debug("Forced Update : EXECUTING FORCE UPDATE CHANGE FOR SSA AND INTERNALS...");
      // if this is SSA and Internal we need to force update to set the
      // other
      // screens
      admin.setReqStatus(CmrConstants.REQUEST_STATUS.PCP.toString());
      admin.setInternalTyp("UPDT_AUTO");
      admin.setLockInd(CmrConstants.YES_NO.N.toString());
      admin.setLockBy(null);
      admin.setLockByNm(null);
      admin.setLockTs(null);
      admin.setReqType(CmrConstants.REQ_TYPE_UPDATE);
      admin.setProcessedFlag(CmrConstants.YES_NO.N.toString());
      admin.setProcessedTs(null);
      updateEntity(admin, entityManager);

      RequestUtils.createWorkflowHistoryFromBatch(entityManager, BATCH_USER_ID, admin, FORCED_UPDATE_COMMENT, FORCED_CHANGE_ACTION, null, null,
          "COM".equals(admin.getReqStatus()));
      RequestUtils.createCommentLogFromBatch(entityManager, BATCH_USER_ID, reqId, FORCED_UPDATE_COMMENT);

      partialCommit(entityManager);
    }
  }

  protected boolean isT2PoolCMRRequest(Admin admin, Data data) {
    if (USUtil.BUSINESS_PARTNER.equals(admin.getCustType()) && "TT2".equals(data.getCsoSite()) && "P".equals(data.getBpAcctTyp())
        && "IRMR".equals(data.getBpName())) {
      return true;
    } else {
      return false;
    }
  }

  /**
   * Reverts the request to a 'Create' if it detects that the system forced the
   * update
   * 
   * @param entityManager
   * @param reqId
   * @throws CmrException
   * @throws SQLException
   */
  protected void revertToCreate(EntityManager entityManager, Admin admin) throws CmrException, SQLException {
    String sql = ExternalizedQuery.getSql("BATCH.CHECK_FORCED_UPDATE");
    long reqId = admin.getId().getReqId();
    PreparedQuery q = new PreparedQuery(entityManager, sql);
    q.setParameter("REQ_ID", reqId);
    q.setParameter("ACT", FORCED_CHANGE_ACTION);
    if (q.exists()) {
      LOG.info("Reverting Request " + reqId + " to Create");
      admin.setReqType(CmrConstants.REQ_TYPE_CREATE);
      admin.setInternalTyp("CREATE_AUTO");
      updateEntity(admin, entityManager);
      RequestUtils.createCommentLogFromBatch(entityManager, BATCH_USER_ID, reqId, REVERT_TO_CREATE_COMMENT);
      partialCommit(entityManager);
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
  public static CmrServiceInput prepareSingleRequestInput(EntityManager entityManager, Admin admin, Data data) {
    CmrServiceInput input = new CmrServiceInput();

    input.setInputMandt(SystemConfiguration.getValue("MANDT"));
    input.setInputReqId(admin.getId().getReqId());
    input.setInputReqType(admin.getReqType());
    input.setInputCmrNo(data.getCmrNo());
    input.setInputUserId(BATCH_USER_ID);

    return input;
  }

  /**
   * Converts the input to Legal CMR Input
   * 
   * @param request
   * @param entityManager
   * @param reqId
   * @throws Exception
   */
  protected void convertToProspectToLegalCMRInput(ProcessRequest request, EntityManager entityManager, long reqId) throws Exception {
    String sql = ExternalizedQuery.getSql("BATCH.GET_PROSPECT");
    PreparedQuery q = new PreparedQuery(entityManager, sql);
    q.setParameter("REQ_ID", reqId);
    List<Object[]> results = q.getResults();
    if (results != null && results.size() > 0) {
      Object[] result = results.get(0);
      if (CmrConstants.YES_NO.Y.toString().equals(result[0])) {
        String prospectCMR = (String) result[1];
        if (StringUtils.isEmpty(prospectCMR)) {
          throw new Exception("Cannot process Prospect to Legal CMR conversion for Request ID " + reqId + ". Prospect CMR No. is missing.");
        }
        request.setProspectCMRNo(prospectCMR);
        request.setSeqNo((String) result[2]);
        // request.set
      }
    }
  }

  /**
   * Create the comment log entry for the request in Notify_Req
   * 
   * @param service
   * @param entityManager
   * @param model
   * @throws CmrException
   * @throws SQLException
   */
  public void createNotifyReqCommentLog(EntityManager entityManager, NotifyReqModel model) throws CmrException, SQLException {
    LOG.info("Creating Comment Log for  Notify Req " + model.getNotifyId() + " [Request ID: " + model.getReqId() + "]");
    ReqCmtLog log = new ReqCmtLog();
    ReqCmtLogPK logPk = new ReqCmtLogPK();
    logPk.setCmtId(SystemUtil.getNextID(entityManager, SystemConfiguration.getValue("MANDT"), "CMT_ID"));
    log.setId(logPk);
    log.setReqId(model.getReqId());
    log.setCmt(model.getCmtLogMsg());
    // save cmtlockedIn as Y default for current realese
    log.setCmtLockedIn(CmrConstants.CMT_LOCK_IND_YES);
    log.setCreateById(model.getChangedById());
    log.setCreateByNm(model.getChangedById());
    // set createTs as current timestamp and updateTs same as CreateTs
    log.setCreateTs(SystemUtil.getCurrentTimestamp());
    log.setUpdateTs(log.getCreateTs());
    createEntity(log, entityManager);
  }

  /**
   * Create the comment log entry for the request in MQ_INTF_REQ_QUEUE
   * 
   * @param service
   * @param entityManager
   * @param model
   * @throws CmrException
   * @throws SQLException
   */
  public void createMQIntfReqCommentLog(EntityManager entityManager, MQIntfReqQueueModel model) throws CmrException, SQLException {
    LOG.info("Creating Comment Log for  MQ INTF REQ " + model.getQueryReqId() + " [Request ID: " + model.getReqId() + "]");
    ReqCmtLog log = new ReqCmtLog();
    ReqCmtLogPK logPk = new ReqCmtLogPK();
    logPk.setCmtId(SystemUtil.getNextID(entityManager, SystemConfiguration.getValue("MANDT"), "CMT_ID"));
    log.setId(logPk);
    log.setReqId(model.getReqId());
    if (!StringUtils.isBlank(model.getExecpMessage())) {
      MQProcessingException exception = MQProcessingException.parse(model.getTargetSys(), model.getExecpMessage());
      log.setCmt(exception.toString(2000, MQErrorLine.TYPE_MANDATORY_ERROR, MQErrorLine.TYPE_GENERAL_ERROR, MQErrorLine.TYPE_ANY_ERROR));
    } else {
      log.setCmt("");
    }
    // save cmtlockedIn as Y default for current realese
    log.setCmtLockedIn(CmrConstants.CMT_LOCK_IND_YES);
    log.setCreateById(model.getLastUpdtBy());
    log.setCreateByNm(model.getLastUpdtBy());
    // set createTs as current timestamp and updateTs same as CreateTs
    log.setCreateTs(SystemUtil.getCurrentTimestamp());
    log.setUpdateTs(log.getCreateTs());
    createEntity(log, entityManager);
  }

  /**
   * Initializes the ProcessClient
   * 
   * @throws Exception
   */
  protected void initClient() throws Exception {
    if (this.serviceClient == null) {
      this.serviceClient = CmrServicesFactory.getInstance().createClient(BATCH_SERVICES_URL, ProcessClient.class);
    }
    if (this.massServiceClient == null) {
      this.massServiceClient = MassServicesFactory.getInstance().createClient(BATCH_SERVICES_URL, MassProcessClient.class);
    }
    if (this.updtByEntClient == null) {
      this.updtByEntClient = CmrServicesFactory.getInstance().createClient(BATCH_SERVICES_URL, UpdateByEntClient.class);
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
  public static MassUpdateServiceInput prepareMassChangeInput(EntityManager entityManager, Admin admin) {
    MassUpdateServiceInput input = new MassUpdateServiceInput();
    input.setInputMandt(SystemConfiguration.getValue("MANDT"));
    input.setInputReqId(admin.getId().getReqId());
    input.setInputReqType(admin.getReqType());
    input.setInputUserId(BATCH_USER_ID);

    return input;
  }

  /**
   * Calls the MassUpdateService and process rdc tagged records only
   * 
   * @param entityManager
   * @param reqId
   * @param cmrServiceInput
   * @param batchAction
   * @param currentRDCProcStat
   * @throws JsonGenerationException
   * @throws JsonMappingException
   * @throws IOException
   * @throws Exception
   */

  public MassProcessRequest prepareMassUpdateRequest(EntityManager entityManager, Admin admin, Data data,
      MassUpdateServiceInput massUpdateServiceInput) throws JsonGenerationException, JsonMappingException, IOException, Exception {
    MassProcessRequest request = new MassProcessRequest();
    request.setMandt(massUpdateServiceInput.getInputMandt());
    request.setReqId(massUpdateServiceInput.getInputReqId());
    request.setReqType(massUpdateServiceInput.getInputReqType());
    request.setUserId(massUpdateServiceInput.getInputUserId());

    List<MassUpdateRecord> records = new ArrayList<MassUpdateRecord>();
    fetchMassUpdateAddressDetails(entityManager, admin, data, records);
    fetchMassUpdateDataDetails(entityManager, admin, data, records);
    request.setRecords(records);
    return request;

  }

  /**
   * Retrieves the {@link MassUpdtData} details
   * 
   * @param entityManager
   * @param reqId
   * @param itrId
   * @param sysLoc
   * @param updtRecordsList
   */
  protected void fetchMassUpdateDataDetails(EntityManager entityManager, Admin admin, Data data, List<MassUpdateRecord> updtRecordsList) {
    long reqId = admin.getId().getReqId();
    int itrId = admin.getIterationId();
    String sysLoc = data.getCmrIssuingCntry();

    String addrTypeAll = "ALL";
    PreparedQuery query = new PreparedQuery(entityManager, ExternalizedQuery.getSql("BATCH.MASS_UPDATE_DATA_RECORDS"));
    query.setParameter("REQ_ID", reqId);
    query.setParameter("ITERATION_ID", itrId);
    query.setParameter("STATUS", "PASS");
    List<CompoundEntity> results = query.getCompundResults(Admin.class, Admin.BATCH_UPDATE_DATA_MAPPING);
    MassUpdt massUpdt = null;
    MassUpdtData massUpdtData = null;
    LOG.debug("Size of Mass Update data Record list : " + results.size());
    for (CompoundEntity entity : results) {
      massUpdt = entity.getEntity(MassUpdt.class);
      massUpdtData = entity.getEntity(MassUpdtData.class);
      // set all the data value with addretype =ALL
      List<RequestValueRecord> requestDataValueRecords = new ArrayList<RequestValueRecord>();
      MassUpdateRecord updtDataRec = new MassUpdateRecord();

      // set the values in the update record
      updtDataRec.setCmrNo(massUpdt.getCmrNo());
      updtDataRec.setSysLoc(sysLoc);
      updtDataRec.setAddrType(addrTypeAll);

      // add NAME1_Record
      // if (!StringUtils.isBlank(mass_updt_data.getCustNm1())) {
      if ((null != massUpdtData.getCustNm1()) && (massUpdtData.getCustNm1().length() > 0)) {
        RequestValueRecord NAME1_Record = new RequestValueRecord();
        NAME1_Record.setField("NAME1");
        NAME1_Record.setValue(massUpdtData.getCustNm1());
        requestDataValueRecords.add(NAME1_Record);
      }

      // add NAME2_Record
      // if (!StringUtils.isBlank(mass_updt_data.getCustNm2())) {
      if ((null != massUpdtData.getCustNm2()) && (massUpdtData.getCustNm2().length() > 0)) {
        RequestValueRecord NAME2_Record = new RequestValueRecord();
        NAME2_Record.setField("NAME2");
        NAME2_Record.setValue(massUpdtData.getCustNm2());
        requestDataValueRecords.add(NAME2_Record);
      }

      // add ZZKV_SIC_Record
      // if (!StringUtils.isBlank(mass_updt_data.getIsicCd())) {
      if ((null != massUpdtData.getIsicCd()) && (massUpdtData.getIsicCd().length() > 0)) {
        RequestValueRecord ZZKV_SIC_Record = new RequestValueRecord();
        ZZKV_SIC_Record.setField("ZZKV_SIC");
        ZZKV_SIC_Record.setValue(massUpdtData.getIsicCd());
        requestDataValueRecords.add(ZZKV_SIC_Record);
      }

      // add ISU_CD_Record
      // if (!StringUtils.isBlank(mass_updt_data.getIsuCd())) {
      if ((null != massUpdtData.getIsuCd()) && (massUpdtData.getIsuCd().length() > 0)) {
        RequestValueRecord ISU_CD_Record = new RequestValueRecord();
        ISU_CD_Record.setField("BRSCH");
        ISU_CD_Record.setValue(massUpdtData.getIsuCd());
        requestDataValueRecords.add(ISU_CD_Record);
      }

      // add INAC_CD_Record
      // if (!StringUtils.isBlank(mass_updt_data.getInacCd())) {
      if ((null != massUpdtData.getInacCd()) && (massUpdtData.getInacCd().length() > 0)) {
        RequestValueRecord INAC_CD_Record = new RequestValueRecord();
        INAC_CD_Record.setField("ZZKV_INAC");
        INAC_CD_Record.setValue(massUpdtData.getInacCd());
        requestDataValueRecords.add(INAC_CD_Record);
      }

      // add CLIENT_TIER_Record
      // if (!StringUtils.isBlank(mass_updt_data.getClientTier())) {
      if ((null != massUpdtData.getClientTier()) && (massUpdtData.getClientTier().length() > 0)) {
        RequestValueRecord CLIENT_TIER_Record = new RequestValueRecord();
        CLIENT_TIER_Record.setField("KATR3");
        CLIENT_TIER_Record.setValue(massUpdtData.getClientTier());
        requestDataValueRecords.add(CLIENT_TIER_Record);
      }

      // add TAX_CD1_Record
      // if (!StringUtils.isBlank(mass_updt_data.getTaxCd1())) {
      if ((null != massUpdtData.getTaxCd1()) && (massUpdtData.getTaxCd1().length() > 0)) {
        RequestValueRecord TAX_CD1_Record = new RequestValueRecord();
        TAX_CD1_Record.setField("STCD2");
        TAX_CD1_Record.setValue(massUpdtData.getTaxCd1());
        requestDataValueRecords.add(TAX_CD1_Record);
      }

      // add ENTERPRISE_Record
      // if (!StringUtils.isBlank(mass_updt_data.getEnterprise())) {
      if ((null != massUpdtData.getEnterprise()) && (massUpdtData.getEnterprise().length() > 0)) {
        RequestValueRecord ENTERPRISE_Record = new RequestValueRecord();
        ENTERPRISE_Record.setField("ZZKV_NODE2");
        ENTERPRISE_Record.setValue(massUpdtData.getEnterprise());
        requestDataValueRecords.add(ENTERPRISE_Record);
      }

      if (!StringUtils.isBlank(massUpdtData.getAbbrevNm())) {
        RequestValueRecord telx1 = new RequestValueRecord();
        telx1.setField("TELX1");
        telx1.setValue(massUpdtData.getAbbrevNm());
        requestDataValueRecords.add(telx1);
      }

      if ("897".equals(data.getCmrIssuingCntry())) {
        RequestValueRecord telx1 = new RequestValueRecord();
        telx1.setField("TELX1");
        telx1.setValue("Update");
        requestDataValueRecords.add(telx1);
      }

      // CREATCMR-2990 - add affiliate
      if (!StringUtils.isBlank(massUpdtData.getAffiliate())) {
        RequestValueRecord affiliate = new RequestValueRecord();
        affiliate.setField("KONZS");
        affiliate.setValue(massUpdtData.getAffiliate());
        requestDataValueRecords.add(affiliate);
      }

      // add KUKLA
      if (!StringUtils.isBlank(massUpdtData.getCustClass())) {
        RequestValueRecord kukla = new RequestValueRecord();
        kukla.setField("KUKLA");
        kukla.setValue(massUpdtData.getCustClass());
        requestDataValueRecords.add(kukla);
      }

      // add Order Block
      if (!StringUtils.isBlank(massUpdtData.getOrdBlk())) {
        RequestValueRecord ordblk = new RequestValueRecord();
        ordblk.setField("AUFSD");
        ordblk.setValue(massUpdtData.getOrdBlk());
        requestDataValueRecords.add(ordblk);
      }

      // Misc Bill Code
      if (!StringUtils.isBlank(massUpdtData.getMiscBillCd())) {
        RequestValueRecord miscbill = new RequestValueRecord();
        miscbill.setField("MISCBILLCD");
        miscbill.setValue(massUpdtData.getMiscBillCd());
        requestDataValueRecords.add(miscbill);
      }
      // CREATCMR-5828
      // PCC AR Dept.
      if (!StringUtils.isBlank(massUpdtData.getPccArDept())) {
        RequestValueRecord PCCARBO = new RequestValueRecord();
        PCCARBO.setField("PCC_AR_BO");
        PCCARBO.setValue(massUpdtData.getPccArDept());
        requestDataValueRecords.add(PCCARBO);
      }
      // Restrict To
      if (!StringUtils.isBlank(massUpdtData.getRestrictTo())) {
        RequestValueRecord USRESTRICTTO = new RequestValueRecord();
        USRESTRICTTO.setField("US_RESTRICT_TO");
        USRESTRICTTO.setValue(massUpdtData.getRestrictTo());
        requestDataValueRecords.add(USRESTRICTTO);
      }
      // SVC AR Office
      if (!StringUtils.isBlank(massUpdtData.getSvcArOffice())) {
        RequestValueRecord SVCAROFC = new RequestValueRecord();
        SVCAROFC.setField("SVC_AR_OFC");
        SVCAROFC.setValue(massUpdtData.getSvcArOffice());
        requestDataValueRecords.add(SVCAROFC);
      }
      // Marketing AR Dept.
      if (!StringUtils.isBlank(massUpdtData.getMktgArDept())) {
        RequestValueRecord MKTGARDEPT = new RequestValueRecord();
        MKTGARDEPT.setField("MKTG_AR_DEPT");
        MKTGARDEPT.setValue(massUpdtData.getMktgArDept());
        requestDataValueRecords.add(MKTGARDEPT);
      }
      // CSO Site
      if (!StringUtils.isBlank(massUpdtData.getCsoSite())) {
        RequestValueRecord CSOSITE = new RequestValueRecord();
        CSOSITE.setField("CSO_SITE");
        CSOSITE.setValue(massUpdtData.getCsoSite());
        requestDataValueRecords.add(CSOSITE);
      }
      // Marketing Dept.
      if (!StringUtils.isBlank(massUpdtData.getMktgDept())) {
        RequestValueRecord MKTGDEPT = new RequestValueRecord();
        MKTGDEPT.setField("MKTG_DEPT");
        MKTGDEPT.setValue(massUpdtData.getMktgDept());
        requestDataValueRecords.add(MKTGDEPT);
      }
      // Misc Bill Code
      if (!StringUtils.isBlank(massUpdtData.getMiscBillCd())) {
        RequestValueRecord MISCBILLING = new RequestValueRecord();
        MISCBILLING.setField("MISC_BILLING");
        MISCBILLING.setValue(massUpdtData.getMiscBillCd());
        requestDataValueRecords.add(MISCBILLING);
      }
      // ICC Tax Class
      if (!StringUtils.isBlank(massUpdtData.getIccTaxClass())) {
        RequestValueRecord CICCTAXCLASS = new RequestValueRecord();
        CICCTAXCLASS.setField("C_ICC_TAX_CLASS");
        CICCTAXCLASS.setValue(massUpdtData.getIccTaxClass());
        requestDataValueRecords.add(CICCTAXCLASS);
      }
      // ICC Tax Exempt Status
      if (!StringUtils.isBlank(massUpdtData.getIccTaxExemptStatus())) {
        RequestValueRecord CICCTE = new RequestValueRecord();
        CICCTE.setField("C_ICC_TE");
        CICCTE.setValue(massUpdtData.getIccTaxExemptStatus());
        requestDataValueRecords.add(CICCTE);
      }
      // Tax Class / Code 2
      if (!StringUtils.isBlank(massUpdtData.getTaxCd2()) && massUpdtData.getTaxCd2().length() == 4) {
        RequestValueRecord ITAXCLASS2 = new RequestValueRecord();
        ITAXCLASS2.setField("I_TAX_CLASS_2");
        ITAXCLASS2.setValue(massUpdtData.getTaxCd2().substring(1, 4));
        requestDataValueRecords.add(ITAXCLASS2);
      }

      // I_TYPE_CUST_2
      if (!StringUtils.isBlank(massUpdtData.getTaxCd2()) && massUpdtData.getTaxCd2().length() == 4) {
        RequestValueRecord ITYPECUST2 = new RequestValueRecord();
        ITYPECUST2.setField("I_TYPE_CUST_2");
        ITYPECUST2.setValue(massUpdtData.getTaxCd2().substring(0, 1));
        requestDataValueRecords.add(ITYPECUST2);
      }
      // Tax Class / Code 3
      if (!StringUtils.isBlank(massUpdtData.getTaxCd3()) && massUpdtData.getTaxCd3().length() == 4) {
        RequestValueRecord ITAXCLASS3 = new RequestValueRecord();
        ITAXCLASS3.setField("I_TAX_CLASS_3");
        ITAXCLASS3.setValue(massUpdtData.getTaxCd3().substring(1, 4));
        requestDataValueRecords.add(ITAXCLASS3);
      }

      // I_TYPE_CUST_3
      if (!StringUtils.isBlank(massUpdtData.getTaxCd3()) && massUpdtData.getTaxCd3().length() == 4) {
        RequestValueRecord ITYPECUST3 = new RequestValueRecord();
        ITYPECUST3.setField("I_TYPE_CUST_3");
        ITYPECUST3.setValue(massUpdtData.getTaxCd3().substring(0, 1));
        requestDataValueRecords.add(ITYPECUST3);
      }

      // Tax Exempt Status
      if (!StringUtils.isBlank(massUpdtData.getTaxExemptStatus())) {
        RequestValueRecord CTECERTST1 = new RequestValueRecord();
        CTECERTST1.setField("C_TE_CERT_ST_1");
        CTECERTST1.setValue(massUpdtData.getTaxExemptStatus());
        requestDataValueRecords.add(CTECERTST1);
      }
      // Tax Exempt Status2
      if (!StringUtils.isBlank(massUpdtData.getTaxExemptStatus2())) {
        RequestValueRecord CTECERTST2 = new RequestValueRecord();
        CTECERTST2.setField("C_TE_CERT_ST_2");
        CTECERTST2.setValue(massUpdtData.getTaxExemptStatus2());
        requestDataValueRecords.add(CTECERTST2);
      }
      // Tax Exempt Status3
      if (!StringUtils.isBlank(massUpdtData.getTaxExemptStatus3())) {
        RequestValueRecord CTECERTST3 = new RequestValueRecord();
        CTECERTST3.setField("C_TE_CERT_ST_3");
        CTECERTST3.setValue(massUpdtData.getTaxExemptStatus3());
        requestDataValueRecords.add(CTECERTST3);
      }
      // Education Allowance
      if (!StringUtils.isBlank(massUpdtData.getEducAllowCd())) {
        RequestValueRecord EASTATUS = new RequestValueRecord();
        EASTATUS.setField("EA_STATUS");
        EASTATUS.setValue(massUpdtData.getEducAllowCd());
        requestDataValueRecords.add(EASTATUS);
      }
      // Out of City Limits
      if (!StringUtils.isBlank(massUpdtData.getOutCityLimit())) {
        RequestValueRecord FOCL = new RequestValueRecord();
        FOCL.setField("F_OCL");
        FOCL.setValue(massUpdtData.getOutCityLimit());
        requestDataValueRecords.add(FOCL);
      }
      // CREATCMR-5828

      // set requestDataValueRecords list updatDataRec
      updtDataRec.setValues(requestDataValueRecords);
      int recordSize = updtDataRec.getValues().size();
      // set record in request only if not empty
      if (recordSize > 0) {
        // set updatDataRec in updtRecordsList
        updtRecordsList.add(updtDataRec);
      }
    }
  }

  /**
   * Retrieves the {@link MassUpdtAddr} details
   * 
   * @param entityManager
   * @param reqId
   * @param itrId
   * @param sysLoc
   * @param updtRecordsList
   */
  protected void fetchMassUpdateAddressDetails(EntityManager entityManager, Admin admin, Data data, List<MassUpdateRecord> updtRecordsList) {
    // code to retrieve the cmrNo,addrType and other fields

    long reqId = admin.getId().getReqId();
    int itrId = admin.getIterationId();
    String sysLoc = data.getCmrIssuingCntry();

    PreparedQuery query = new PreparedQuery(entityManager, ExternalizedQuery.getSql("BATCH.MASS_UPDATE_ADDR_RECORDS"));
    query.setParameter("REQ_ID", reqId);
    query.setParameter("ITERATION_ID", itrId);
    query.setParameter("STATUS", "PASS");
    List<CompoundEntity> results = query.getCompundResults(Admin.class, Admin.BATCH_UPDATE_ADDR_MAPPING);
    MassUpdt massUpdt = null;
    MassUpdtAddr addr = null;
    LOG.debug("Size of Mass Upadte Addr Record list : " + results.size());
    for (CompoundEntity entity : results) {
      massUpdt = entity.getEntity(MassUpdt.class);
      addr = entity.getEntity(MassUpdtAddr.class);

      List<RequestValueRecord> requestAddrValueRecords = new ArrayList<RequestValueRecord>();
      MassUpdateRecord updtAddrRec = new MassUpdateRecord();

      // set the values in the update record
      updtAddrRec.setCmrNo(massUpdt.getCmrNo());
      updtAddrRec.setSysLoc(sysLoc);
      updtAddrRec.setAddrType(addr.getId().getAddrType());

      // add ADDR_TXT_Record
      // if (!StringUtils.isBlank(mass_updt_addr.getAddrTxt())) {
      if ((null != addr.getAddrTxt()) && (addr.getAddrTxt().length() > 0)) {
        RequestValueRecord ADDR_TXT_Record = new RequestValueRecord();
        ADDR_TXT_Record.setField("STRAS");
        ADDR_TXT_Record.setValue(addr.getAddrTxt());
        requestAddrValueRecords.add(ADDR_TXT_Record);
      }

      // add CITY1_Record
      // if (!StringUtils.isBlank(mass_updt_addr.getCity1())) {
      if ((null != addr.getCity1()) && (addr.getCity1().length() > 0)) {
        RequestValueRecord CITY1_Record = new RequestValueRecord();
        CITY1_Record.setField("ORT01");
        CITY1_Record.setValue(addr.getCity1());
        requestAddrValueRecords.add(CITY1_Record);
      }

      // add POST_CD_Record
      // if (!StringUtils.isBlank(mass_updt_addr.getPostCd())) {
      if ((null != addr.getPostCd()) && (addr.getPostCd().length() > 0)) {
        RequestValueRecord POST_CD_Record = new RequestValueRecord();
        POST_CD_Record.setField("PSTLZ");
        POST_CD_Record.setValue(addr.getPostCd());
        requestAddrValueRecords.add(POST_CD_Record);
      }

      // add STATE_PROV_Record
      // if (!StringUtils.isBlank(mass_updt_addr.getStateProv())) {
      if ((null != addr.getStateProv()) && (addr.getStateProv().length() > 0)) {
        RequestValueRecord STATE_PROV_Record = new RequestValueRecord();
        STATE_PROV_Record.setField("REGIO");
        STATE_PROV_Record.setValue(addr.getStateProv());
        requestAddrValueRecords.add(STATE_PROV_Record);
      }

      // add DIVN_Record
      // if (!StringUtils.isBlank(mass_updt_addr.getDivn())) {
      if ((null != addr.getDivn()) && (addr.getDivn().length() > 0)) {
        RequestValueRecord DIVN_Record = new RequestValueRecord();
        DIVN_Record.setField("NAME3");
        DIVN_Record.setValue(addr.getDivn());
        requestAddrValueRecords.add(DIVN_Record);
      }

      // add DEPT_Record
      // if (!StringUtils.isBlank(mass_updt_addr.getDept())) {
      if ((null != addr.getDept()) && (addr.getDept().length() > 0)) {
        RequestValueRecord DEPT_Record = new RequestValueRecord();
        DEPT_Record.setField("NAME4");
        DEPT_Record.setValue(addr.getDept());
        requestAddrValueRecords.add(DEPT_Record);
      }

      // add COUNTY_Record
      // if (!StringUtils.isBlank(mass_updt_addr.getCounty())) {
      if ((null != addr.getCounty()) && (addr.getCounty().length() > 0)) {
        RequestValueRecord COUNTY_Record = new RequestValueRecord();
        COUNTY_Record.setField("COUNC");
        COUNTY_Record.setValue(addr.getCounty());
        requestAddrValueRecords.add(COUNTY_Record);
      }

      // set requestAddrValueRecords in updtAddrRec

      updtAddrRec.setValues(requestAddrValueRecords);
      int recordSize = updtAddrRec.getValues().size();
      if (recordSize > 0) {
        // set updtAddrRec in updtRecordsList
        updtRecordsList.add(updtAddrRec);
      }
    }

  }

  /**
   * Prepares the request for Reactivate/Delete
   * 
   * @param entityManager
   * @param admin
   * @param data
   * @param input
   * @return
   * @throws JsonGenerationException
   * @throws JsonMappingException
   * @throws IOException
   * @throws Exception
   */
  public MassProcessRequest prepareReactivateDelRequest(EntityManager entityManager, Admin admin, Data data, MassUpdateServiceInput input)
      throws JsonGenerationException, JsonMappingException, IOException, Exception {

    MassProcessRequest request = new MassProcessRequest();
    request.setMandt(input.getInputMandt());
    request.setReqId(input.getInputReqId());
    request.setReqType(input.getInputReqType());
    request.setUserId(input.getInputUserId());

    long reqId = admin.getId().getReqId();
    long iterationId = admin.getIterationId();
    String sysLoc = data.getCmrIssuingCntry();

    // get PASS records for the current iteration
    List<MassUpdateRecord> updtRecordsList = new ArrayList<MassUpdateRecord>();
    PreparedQuery massquery = new PreparedQuery(entityManager, ExternalizedQuery.getSql("BATCH.REACT_DEL_RECORDS"));
    massquery.setParameter("REQ_ID", reqId);
    massquery.setParameter("ITERATION_ID", iterationId);
    massquery.setParameter("STATUS", "PASS");
    List<CompoundEntity> results = massquery.getCompundResults(Admin.class, Admin.BATCH_REACT_DEL_SERVICE_MAPPING);

    MassUpdt massUpdt = null;
    MassUpdateRecord record = null;
    LOG.debug("Size of Mass Update Record list for Iteration " + iterationId + " : " + results.size());
    for (CompoundEntity entity : results) {
      record = new MassUpdateRecord();
      massUpdt = entity.getEntity(MassUpdt.class);

      // set the values in the update record
      record.setCmrNo(massUpdt.getCmrNo());
      record.setSysLoc(sysLoc);
      record.setAddrType("ZS01");

      // set all the name and values pair the document here
      List<RequestValueRecord> requestValueRecords = new ArrayList<RequestValueRecord>();

      // add LOEVM_Record
      if (CmrConstants.REQ_TYPE_DELETE.equals(input.getInputReqType())) {
        RequestValueRecord loevm = new RequestValueRecord();
        loevm.setField("LOEVM");
        loevm.setValue("X");
        requestValueRecords.add(loevm);
      } else if (CmrConstants.REQ_TYPE_REACTIVATE.equals(input.getInputReqType())) {
        RequestValueRecord loevm = new RequestValueRecord();
        loevm.setField("LOEVM");
        loevm.setValue("");
        requestValueRecords.add(loevm);
      }

      // set requestValueRecords list updatRec

      record.setValues(requestValueRecords);
      // set updtRec in updtRecordsList
      updtRecordsList.add(record);
      // request.getRecords()

    }
    // set updtRecordsList in request
    request.setRecords(updtRecordsList);

    return request;

  }

  /**
   * Prepares the request for Update by Enterprise
   * 
   * @param em
   * @param reqId
   * @param sysLoc
   * @param admin
   * @param massUpdtData
   * @return
   * @throws JsonGenerationException
   * @throws JsonMappingException
   * @throws IOException
   * @throws Exception
   */
  public EnterpriseUpdtRequest prepareUpdtByEnterpriseRequest(EntityManager em, Admin admin, Data data, MassUpdtData massUpdtData)
      throws JsonGenerationException, JsonMappingException, IOException, Exception {
    EnterpriseUpdtRequest request = new EnterpriseUpdtRequest();

    request.setMandt(SystemConfiguration.getValue("MANDT"));
    request.setReqId(admin.getId().getReqId());
    request.setReqType(admin.getReqType());
    request.setUserId(SystemConfiguration.getValue("BATCH_USERID"));
    request.setEntUpdtType(massUpdtData.getEntpUpdtTyp());
    request.setEnterprise(massUpdtData.getEnterprise());
    request.setCompany(massUpdtData.getCompany());
    request.setCompanyName(massUpdtData.getCustNm1() + massUpdtData.getCustNm2());
    request.setNewEnterprise(massUpdtData.getNewEntp());
    request.setNewEnterpriseName1(massUpdtData.getNewEntpName1());
    request.setNewEnterpriseName2(massUpdtData.getNewEntpName2());
    request.setSysLoc(data.getCmrIssuingCntry());

    return request;
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

  /**
   * Updates MASS_UPDT.ROW_STATUS_CD
   * 
   * @param statusCode
   * @param entityManager
   * @param parReqId
   */
  protected void updateMassUpdateRowStatus(String statusCode, EntityManager entityManager, long parReqId) {
    String sql = null;
    if (statusCode.equalsIgnoreCase("C") || statusCode.equalsIgnoreCase("W")) {
      sql = ExternalizedQuery.getSql("ENTERPRISE.MASS.UPDATE.DATA.DONE");
    } else if (statusCode.equalsIgnoreCase("N") || statusCode.equalsIgnoreCase("A")) {
      sql = ExternalizedQuery.getSql("ENTERPRISE.MASS.UPDATE.DATA.RDCER");
    } else {
      return;
    }

    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("PAR_REQ_ID", parReqId);
    query.executeSql();
  }

  @Override
  protected boolean isTransactional() {
    return true;
  }

  protected void lockAdminRecordsForProcessing(List<Admin> forProcessing, EntityManager entityManager) {
    for (Admin admin : forProcessing) {
      if (!CmrConstants.REQ_TYPE_MASS_CREATE.equals(admin.getReqType())) {
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

  protected List<Object[]> getGeoTaxInfo(EntityManager em, String reqid) {
    String sql1 = ExternalizedQuery.getSql("QUERY.TAXINFORECORDSBYREQID");
    PreparedQuery query = new PreparedQuery(em, sql1);
    query.setParameter("REQ_ID", reqid);
    List<Object[]> taxInfoList = query.getResults();
    return taxInfoList;
  }

  /**
   * Utility class that deletes RDc records. <strong>This is a work-around for
   * the environment issues on test/pp and should not be called in
   * prod</strong>.
   * 
   * @param entityManager
   * @param cmrNo
   * @param country
   */
  protected void deleteRDcTargetRecords(EntityManager entityManager, String cmrNo, String country) {
    try {

      // get target kunnrs
      LOG.debug("Getting target KUNNR for KATR6: " + country + " and CMR No.: " + cmrNo);
      String sql = "select KUNNR from SAPR3.KNA1 where MANDT = :MANDT and ZZKV_CUSNO = :CMR_NO and KATR6 = :COUNTRY";
      PreparedQuery query = new PreparedQuery(entityManager, sql);
      query.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
      query.setParameter("COUNTRY", country);
      query.setParameter("CMR_NO", cmrNo);
      query.setForReadOnly(true);
      List<String> kunnrs = query.getResults(String.class);
      if (!kunnrs.isEmpty()) {

        IndexServiceClient client = new IndexServiceClient(SystemConfiguration.getValue("BATCH_CI_SERVICES_URL"));
        for (String kunnr : kunnrs) {
          LOG.debug("Got KUNNR: " + kunnr);
          LOG.debug("Deleting RDc records for KATR6: " + country + " and CMR No.: " + cmrNo);
          String[] tables = { "KNEX", "KNVK", "KNVP", "KNVI", "KNVV", "KNB1", "KUNNR_EXT", "KNA1" };
          for (String table : tables) {
            LOG.debug("Deleting KUNNR: " + kunnr + " from " + table);
            sql = "delete from SAPR3." + table + " where MANDT = :MANDT and KUNNR = :KUNNR";
            query = new PreparedQuery(entityManager, sql);
            query.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
            query.setParameter("KUNNR", kunnr);
            query.executeSql();

            try {
              DeleteFromIndexRequest deleteReq = new DeleteFromIndexRequest("cmr");
              deleteReq.setDelete(true);
              deleteReq.setKunnr(kunnr);
              deleteReq.setMandt(SystemConfiguration.getValue("MANDT"));

              IndexResponse response = client.sendRequest(deleteReq);
              if (response.isSuccess()) {
                LOG.debug("KUNNR " + kunnr + " deleted from Index");
              } else {
                LOG.warn("Error when deleting KUNNR " + kunnr + " from Index: " + response.getMsg());
              }
            } catch (Exception e) {
              LOG.warn("Cannot delete from KUNNR " + kunnr + " from Index", e);
            }
          }
        }
        partialCommit(entityManager);
      } else {
        LOG.debug("No KUNNR found. Proceeding with CREATE.");
      }
    } catch (Exception e) {
      partialRollback(entityManager);
    }
  }

  public boolean isDeleteRDcTargets() {
    return deleteRDcTargets;
  }

  public void setDeleteRDcTargets(boolean deleteRDcTargets) {
    this.deleteRDcTargets = deleteRDcTargets;
  }

  /**
   * Distributes the requests evenly on lists that equate to the thread count
   * 
   * @param requests
   * @param threadCount
   * @return
   */
  protected <T> List<List<T>> allocateRequests(Queue<T> requests, int threadCount) {
    List<List<T>> lists = new ArrayList<List<T>>();
    if (requests.size() < threadCount) {
      for (int i = 0; i < threadCount; i++) {
        T reqId = requests.poll();
        if (reqId != null) {
          lists.add(Collections.singletonList(reqId));
        }
      }
    } else {
      while (requests.peek() != null) {
        for (int i = 0; i < threadCount; i++) {
          if (lists.size() < threadCount) {
            lists.add(new ArrayList<T>());
          }
          T reqId = requests.poll();
          if (reqId != null) {
            List<T> queueForThread = lists.get(i);
            queueForThread.add(reqId);
          }
        }
      }
    }

    return lists;
  }

  /**
   * 
   * @param entityManager
   * @param cmrNo
   * @param cmrIssuingCntry
   * @return
   */
  public boolean isOwnerCorrect(EntityManager entityManager, String cmrNo, String cmrIssuingCntry) {
    String sql = "select KATR10 from SAPR3.KNA1 where MANDT = :MANDT and KATR6 = :COUNTRY and ZZKV_CUSNO = :CMR_NO and KTOKD = 'ZS01'";
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
    query.setParameter("COUNTRY", cmrIssuingCntry);
    query.setParameter("CMR_NO", cmrNo);
    String katr10 = query.getSingleResult(String.class);
    if (katr10 == null) {
      // non existent, return true;
      return true;
    } else {
      return "".equals(katr10.trim());
    }
  }

  public boolean isMultiMode() {
    return multiMode;
  }

  public void setMultiMode(boolean multiMode) {
    this.multiMode = multiMode;
  }

  @Override
  protected boolean terminateOnLongExecution() {
    return !this.multiMode;
  }

  public void monitorUpdateSapNumber(EntityManager entityManager) throws Exception {
    String sql = ExternalizedQuery.getSql("BATCH.FIND_MISSING_SAP_NUMBER");
    PreparedQuery results = new PreparedQuery(entityManager, sql);
    List<Object[]> missedSapNumberRec = results.getResults();

    if (missedSapNumberRec != null && !missedSapNumberRec.isEmpty()) {
      for (Object[] obj : missedSapNumberRec) {
        Optional<Object> checkNull = Optional.ofNullable(obj);
        if (checkNull.isPresent()) {
          String addrType = (String) obj[3];
          String seqNo = (String) obj[2];

          GEOHandler handler = RequestUtils.getGEOHandler((String) obj[6]);
          String mappedAddrType = handler.getEquivalentAddressType(addrType, seqNo);
          String sql1 = ExternalizedQuery.getSql("BATCH.FIND_KUNNR");
          PreparedQuery findKunnr = new PreparedQuery(entityManager, sql1);
          Long reqId = (Long) obj[0];
          Thread.currentThread().setName("REQ-" + reqId);
          findKunnr.setParameter("ZZKV_CUSNO", obj[4]);
          findKunnr.setParameter("KTOKD", mappedAddrType);
          findKunnr.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
          List<Kna1> knaList = findKunnr.getResults(Kna1.class);

          if (knaList != null && !knaList.isEmpty()) {
            Kna1 kna1 = null;
            for (Kna1 kna1Item : knaList) {
              // only process for US now, //TODO adjust for LA/JP
              if ("ZS01".equals(mappedAddrType) && kna1Item.getKtokd().equals(mappedAddrType)) {
                kna1 = kna1Item;
                break;
              }
              // sure ZP01
              if ("ZI01".equals(addrType) && "002".equals(kna1Item.getZzkvSeqno())) {
                kna1 = kna1Item;
                break;
              }
              // sure PG match
              if ("PG01".equals(addrType) && seqNo.equals(kna1Item.getZzkvSeqno())) {
                kna1 = kna1Item;
                break;
              }
              // last try to match seqno, padded
              String paddedSeq = seqNo;
              if (seqNo.length() < kna1Item.getZzkvSeqno().length()) {
                paddedSeq = StringUtils.leftPad(seqNo, kna1Item.getZzkvSeqno().length(), '0');
              }
              if (paddedSeq.equals(kna1Item.getZzkvSeqno())) {
                kna1 = kna1Item;
                break;
              }
            }
            if (kna1 != null) {
              LOG.info("kunnr=" + kna1.getId().getKunnr());
              String kunnr = kna1.getId().getKunnr();

              String sql2 = ExternalizedQuery.getSql("BATCH.GET_ADDR_RECORDS");
              PreparedQuery query2 = new PreparedQuery(entityManager, sql2);
              query2.setParameter("REQ_ID", reqId);
              query2.setParameter("ADDR_TYPE", addrType);
              query2.setParameter("ADDR_SEQ", seqNo);

              List<Addr> addrList = query2.getResults(Addr.class);
              LOG.debug("Size of Upadte Addr Record list : " + addrList.size());
              for (Addr addr : addrList) {
                addr.setSapNo(kunnr);
                addr.setIerpSitePrtyId(kna1.getBran5());
                updateEntity(addr, entityManager);
              }
            }

          }
          Thread.currentThread().setName("TransConn-" + Thread.currentThread().getId());
        } else {
          LOG.debug("NO RECORD Fetch For Update KUNNR in Kna1 Table");
        }
      }
    } else {
      LOG.debug("NO RECORD Fetch For UPDATE SAP NUMBER IN ADDR TABLE");
    }
  }

  public void monitorLAReprocessRdcRecords(EntityManager entityManager, List<Long> reprocessRecords)
      throws JsonGenerationException, JsonMappingException, IOException, Exception {

    for (Long id : reprocessRecords) {
      try {
        Thread.currentThread().setName("REQ-" + id);
        AdminPK pk = new AdminPK();
        pk.setReqId(id);
        Admin admin = entityManager.find(Admin.class, pk);

        LOG.info("Processing LA RDC Record " + admin.getId().getReqId() + " [Request ID: " + admin.getId().getReqId() + "]");

        // get the data
        String sql = ExternalizedQuery.getSql("BATCH.GET_DATA");
        PreparedQuery query = new PreparedQuery(entityManager, sql);
        query.setParameter("REQ_ID", admin.getId().getReqId());

        Data data = query.getSingleResult(Data.class);
        entityManager.detach(data);

        if (SINGLE_REQUEST_TYPES.contains(admin.getReqType()) && CmrConstants.REQUEST_STATUS.PCO.toString().equals(admin.getReqStatus())) {
          processSingleRequest(entityManager, admin, data);
        } else {
          LOG.warn("Request ID " + admin.getId().getReqId() + " cannot be processed. Improper Type or not completed.");
        }

        partialCommit(entityManager);

      } catch (Exception e) {
        SlackAlertsUtil.recordException("TransConn", "Request " + id, e);
        LOG.error("Error in processing Reprocess RDC Record with Request ID " + id + " [" + e.getMessage() + "]", e);
      }
    }
    Thread.currentThread().setName("TransConn-" + Thread.currentThread().getId());
  }

  private void setLAAdminToCompleted(Admin admin, Data data) {
    if (CmrConstants.LA_COUNTRIES.contains(data.getCmrIssuingCntry())) {
      admin.setReqStatus("COM");
    }
  }

  private void setLAAdminToPending(Admin admin, Data data) {
    if (CmrConstants.LA_COUNTRIES.contains(data.getCmrIssuingCntry())) {
      admin.setReqStatus("PPN");
      admin.setProcessedFlag("E"); // set request status to error.
    }
  }
}