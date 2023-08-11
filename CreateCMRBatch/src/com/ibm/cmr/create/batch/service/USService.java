package com.ibm.cmr.create.batch.service;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.AddrPK;
import com.ibm.cio.cmr.request.entity.AddrRdc;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.AdminPK;
import com.ibm.cio.cmr.request.entity.CompoundEntity;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.entity.DataPK;
import com.ibm.cio.cmr.request.entity.DataRdc;
import com.ibm.cio.cmr.request.entity.MassUpdt;
import com.ibm.cio.cmr.request.entity.MassUpdtData;
import com.ibm.cio.cmr.request.entity.ReqCmtLog;
import com.ibm.cio.cmr.request.entity.ReqCmtLogPK;
import com.ibm.cio.cmr.request.entity.SuppCntry;
import com.ibm.cio.cmr.request.entity.WfHist;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.util.IERPRequestUtils;
import com.ibm.cio.cmr.request.util.RequestUtils;
import com.ibm.cio.cmr.request.util.SlackAlertsUtil;
import com.ibm.cio.cmr.request.util.SystemUtil;
import com.ibm.cio.cmr.request.util.geo.impl.USHandler;
import com.ibm.cmr.create.batch.model.CmrServiceInput;
import com.ibm.cmr.create.batch.model.MassUpdateServiceInput;
import com.ibm.cmr.create.batch.util.BatchUtil;
import com.ibm.cmr.create.batch.util.CMRRequestContainer;
import com.ibm.cmr.create.batch.util.DebugUtil;
import com.ibm.cmr.create.batch.util.ProfilerLogger;
import com.ibm.cmr.create.batch.util.USCMRNumGen;
import com.ibm.cmr.create.batch.util.mq.LandedCountryMap;
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

public class USService extends TransConnService {
  // private static final String BATCH_SERVICES_URL =
  // SystemConfiguration.getValue("BATCH_SERVICES_URL");
  private ProcessClient serviceClient;
  private MassProcessClient massServiceClient;
  private UpdateByEntClient updtByEntClient;
  private static final String COMMENT_LOGGER = "US Service";
  public static final String CMR_REQUEST_REASON_TEMP_REACT_EMBARGO = "TREC";
  private boolean massServiceMode;
  private CMRRequestContainer cmrObjects;
  private static final String[] ADDRESS_ORDER = { "ZS01", "ZI01", "ZD01", "PG01" };
  private long reQId;
  private static final String MASS_UPDATE_FAIL = "FAIL";
  private static final String MASS_UPDATE_DONE = "DONE";
  public static final String CMR_REQUEST_STATUS_CPR = "CPR";
  public static final String CMR_REQUEST_STATUS_PCP = "PCP";
  private static final String ACTION_RDC_UPDT_BY_ENT = "System Action:RDc Enterprise update";
  protected boolean multiMode;

  public boolean isMassServiceMode() {
    return massServiceMode;
  }

  public void setMassServiceMode(boolean massServiceMode) {
    this.massServiceMode = massServiceMode;
  }

  public long getReQId() {
    return reQId;
  }

  public void setReQId(long reQId) {
    this.reQId = reQId;
  }

  public String[] getIerpAddressOrder() {
    return ADDRESS_ORDER;
  }

  @Override
  protected boolean isTransactional() {
    return true;
  }

  @Override
  protected Boolean executeBatch(EntityManager entityManager) throws Exception {
    try {
      initClientUS();
      LOG.info("Multi Mode: " + this.multiMode);
      List<Long> ids = null;
      if (isMassServiceMode()) {
        ids = gatherMassUpdateRequests(entityManager);
        monitorCreqcmrMassUpd(entityManager, ids);
      } else {
        // LOG.info("Processing Aborted records (retry)...");
        // monitorAbortedRecords(entityManager);

        LOG.info("Processing New records...");
        ids = gatherSingleRequests(entityManager);
        monitorCreqcmr(entityManager, ids);
      }

    } catch (Exception e) {
      e.printStackTrace();
      addError(e);
      return false;
    }
    return true;
  }

  public void initClientUS() throws Exception {
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

  @SuppressWarnings("unused")
  public void monitorCreqcmr(EntityManager entityManager, List<Long> requests)
      throws JsonGenerationException, JsonMappingException, IOException, Exception {

    LOG.info("Initializing Country Map..");
    long start = new Date().getTime();
    LandedCountryMap.init(entityManager);
    // Retrieve the PCP records
    LOG.info("Retreiving pending records for processing..");
    // List<Admin> pending = getPendingRecords(entityManager);
    // LOG.debug((pending != null ? pending.size() : 0) + " records to process
    // to RDc.");
    Data data = null;
    ProcessRequest request = null;
    for (Long id : requests) {
      Thread.currentThread().setName("REQ-" + id);
      AdminPK pk = new AdminPK();
      pk.setReqId(id);
      Admin admin = entityManager.find(Admin.class, pk);
      if ("M".equals(admin.getReqType())) {
        continue;
      }
      if (BatchUtil.excludeForEnvironment(this.context, entityManager, admin)) {
        continue;
      }
      // System.out.println(">>>> Req ID >>> " + admin.getId().getReqId());
      // hard coding the req. id below
      // if (admin.getId().getReqId() != reQId) {
      // continue;
      // }
      try {
        this.cmrObjects = prepareRequest(entityManager, admin);
        data = this.cmrObjects.getData();

        request = new ProcessRequest();
        request.setCmrNo(data.getCmrNo());
        request.setMandt(SystemConfiguration.getValue("MANDT"));
        request.setReqId(admin.getId().getReqId());
        request.setReqType(admin.getReqType());
        request.setUserId(BATCH_USER_ID);

        // send all as create process so that it will be sent as 1 request even
        // for updates

        switch (admin.getReqType()) {
        case CmrConstants.REQ_TYPE_CREATE:
          processCreateRequest(entityManager, request, admin, data);
          break;
        case CmrConstants.REQ_TYPE_UPDATE:
          processUpdateRequest(entityManager, request, admin, data);
          break;
        case CmrConstants.REQ_TYPE_REACTIVATE:
        case CmrConstants.REQ_TYPE_DELETE:
          processReactivateDeleteRequest(entityManager, admin, data);
          break;
        case CmrConstants.REQ_TYPE_UPDT_BY_ENT:
          processUpdateByEnterprise(entityManager, admin, data);
          break;
        }

        if (!(CmrConstants.REQ_TYPE_DELETE.equalsIgnoreCase(admin.getReqType())
            && CmrConstants.REQ_TYPE_REACTIVATE.equalsIgnoreCase(admin.getReqType()))) {
          if (CmrConstants.RDC_STATUS_ABORTED.equalsIgnoreCase(admin.getRdcProcessingStatus())
              || CmrConstants.RDC_STATUS_NOT_COMPLETED.equalsIgnoreCase(admin.getRdcProcessingStatus())) {
            admin.setReqStatus("PPN");
            admin.setProcessedFlag("E"); // set request status to error.
            WfHist hist = createHistory(entityManager, "Sending back to processor due to error on RDC processing", "PPN", "RDC Processing",
                admin.getId().getReqId());
          } else if ((CmrConstants.RDC_STATUS_COMPLETED.equalsIgnoreCase(admin.getRdcProcessingStatus())
              || CmrConstants.RDC_STATUS_COMPLETED_WITH_WARNINGS.equalsIgnoreCase(admin.getRdcProcessingStatus()))
              && !"TREC".equalsIgnoreCase(admin.getReqReason())) {
            admin.setReqStatus("COM");
            admin.setProcessedFlag("Y"); // set request status to processed
            WfHist hist = createHistory(entityManager, "Request processing Completed Successfully", "COM", "RDC Processing",
                admin.getId().getReqId());
          }
        }
        partialCommit(entityManager);
        ProfilerLogger.LOG.trace(
            "After monitorCreqcmr for Request ID: " + id + " " + DurationFormatUtils.formatDuration(new Date().getTime() - start, "m 'm' s 's'"));
      } catch (Exception e) {
        SlackAlertsUtil.recordException("USService", "Request " + admin.getId().getReqId(), e);
        partialRollback(entityManager);
        LOG.error("Unexpected error occurred during processing of Request " + admin.getId().getReqId(), e);
        processError(entityManager, admin, e.getMessage());
      }
    }
    Thread.currentThread().setName("USService-" + Thread.currentThread().getId());
  }

  @Override
  public void monitorAbortedRecords(EntityManager entityManager, List<Long> abortedsRecords) {

    // search the aborted records from Admin table
    String sql = ExternalizedQuery.getSql("BATCH.MONITOR_ABORTED_REC");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    List<Admin> abortedRecords = query.getResults(Admin.class);
    LOG.debug("Size of Aborted Records : " + abortedRecords.size());

    // lockAdminRecordsForProcessing(abortedRecords, entityManager);

    for (Admin admin : abortedRecords) {
      LOG.info("Processing Aborted Record " + admin.getId().getReqId() + " [Request ID: " + admin.getId().getReqId() + "]");
      Thread.currentThread().setName("REQ-" + admin.getId().getReqId());
      // get the data
      sql = ExternalizedQuery.getSql("BATCH.GET_DATA");
      query = new PreparedQuery(entityManager, sql);
      query.setParameter("REQ_ID", admin.getId().getReqId());

      Data data = query.getSingleResult(Data.class);
      entityManager.detach(data);
      try {
        CmrServiceInput input = prepareSingleRequestInput(entityManager, admin, data);

        ProcessRequest request = new ProcessRequest();
        request.setCmrNo(input.getInputCmrNo());
        request.setMandt(input.getInputMandt());
        request.setReqId(input.getInputReqId());
        request.setReqType(input.getInputReqType());
        request.setUserId(input.getInputUserId());

        // send all as create process so that it will be sent as 1 request even
        // for updates

        switch (admin.getReqType()) {
        case CmrConstants.REQ_TYPE_CREATE:
          processCreateRequest(entityManager, request, admin, data);
          break;
        case CmrConstants.REQ_TYPE_UPDATE:
          processUpdateRequest(entityManager, request, admin, data);
          break;
        case CmrConstants.REQ_TYPE_REACTIVATE:
        case CmrConstants.REQ_TYPE_DELETE:
          processReactivateDeleteRequest(entityManager, admin, data);
          break;
        case CmrConstants.REQ_TYPE_UPDT_BY_ENT:
          processUpdateByEnterprise(entityManager, admin, data);
          break;
        default:
          LOG.warn("Request ID " + admin.getId().getReqId() + " cannot be processed. Improper Type or not completed.");
        }

        if (CmrConstants.RDC_STATUS_ABORTED.equalsIgnoreCase(admin.getRdcProcessingStatus())
            || CmrConstants.RDC_STATUS_NOT_COMPLETED.equalsIgnoreCase(admin.getRdcProcessingStatus())) {
          admin.setReqStatus("PPN");
          admin.setProcessedFlag("E"); // set request status to error.
          WfHist hist = createHistory(entityManager, "Sending back to processor due to error on RDC processing", "PPN", "RDC Processing",
              admin.getId().getReqId());
        } else if ((CmrConstants.RDC_STATUS_COMPLETED.equalsIgnoreCase(admin.getRdcProcessingStatus())
            || CmrConstants.RDC_STATUS_COMPLETED_WITH_WARNINGS.equalsIgnoreCase(admin.getRdcProcessingStatus()))
            && CmrConstants.REQ_TYPE_CREATE.equals(admin.getReqType())) {
          admin.setReqStatus("COM");
          admin.setProcessedFlag("Y"); // set request status to processed
          WfHist hist = createHistory(entityManager, "Request processing Completed Successfully", "COM", "RDC Processing", admin.getId().getReqId());
        }
        partialCommit(entityManager);
      } catch (Exception e) {
        SlackAlertsUtil.recordException("USService", "Request " + admin.getId().getReqId(), e);
        LOG.error("Error in processing Aborted Record " + admin.getId().getReqId() + " for Request ID " + admin.getId().getReqId() + " ["
            + e.getMessage() + "]", e);
      }
    }
    Thread.currentThread().setName("USService-" + Thread.currentThread().getId());

  }

  public void monitorCreqcmrMassUpd(EntityManager entityManager, List<Long> requests)
      throws JsonGenerationException, JsonMappingException, IOException, Exception {
    LOG.info("Initializing Country Map..");
    long start = new Date().getTime();
    LandedCountryMap.init(entityManager);
    // List<Admin> pending = getPendingRecordsMassUpd(entityManager);

    // LOG.debug((pending != null ? pending.size() : 0) + " records to process
    // to RDc.");

    Data data = null;
    ProcessRequest request = null;
    for (Long id : requests) {
      Thread.currentThread().setName("REQ-" + id);

      AdminPK pk = new AdminPK();
      pk.setReqId(id);
      Admin admin = entityManager.find(Admin.class, pk);
      try {
        // hard-coding to debug specific request
        // if (admin.getId().getReqId() != reQId) {
        // continue;
        // }
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
          admin.setProcessedFlag("E"); // set request status to error.
          createHistory(entityManager, "Sending back to processor due to error on RDC processing", "PPN", "RDC Processing", admin.getId().getReqId());
        } else if ((CmrConstants.RDC_STATUS_COMPLETED.equalsIgnoreCase(admin.getRdcProcessingStatus())
            || CmrConstants.RDC_STATUS_COMPLETED_WITH_WARNINGS.equalsIgnoreCase(admin.getRdcProcessingStatus()))
            && CmrConstants.REQ_TYPE_MASS_UPDATE.equals(admin.getReqType())) {
          admin.setReqStatus("COM");
          admin.setProcessedFlag("Y"); // set request status to processed
          createHistory(entityManager, "Request processing Completed Successfully", "COM", "RDC Processing", admin.getId().getReqId());
        }
        partialCommit(entityManager);
        ProfilerLogger.LOG.trace("After monitorCreqcmrMassUpd for Request ID: " + id + " "
            + DurationFormatUtils.formatDuration(new Date().getTime() - start, "m 'm' s 's'"));
      } catch (Exception e) {
        partialRollback(entityManager);
        LOG.error("Unexpected error occurred during processing of Request " + admin.getId().getReqId(), e);
        processError(entityManager, admin, e.getMessage());
      }
    }
    Thread.currentThread().setName("USService-" + Thread.currentThread().getId());
  }

  /**
   * Gets the Admin records with status = 'PCP' and country has processing type
   * = 'US'
   * 
   * @param entityManager
   * @return
   */
  public List<Admin> getPendingRecords(EntityManager entityManager) {
    String sql = ExternalizedQuery.getSql("US.GET_PENDING.RDC");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    return query.getResults(Admin.class);
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

    // generateUSCmr
    if ("C".equals(admin.getReqType())) {
      String cmrNoGen = generateUSCmr(entityManager, admin);
      data.setCmrNo(cmrNoGen);
      LOG.debug(" ---- Generate CMR No. is --- " + cmrNoGen);
    }

    String sql = ExternalizedQuery.getSql("US.GET.ADDR");
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

    List<Addr> addresses = null;
    if ("R".equals(admin.getReqType()) || "D".equals(admin.getReqType())) {
      addresses = null;
    } else {
      addresses = query.getResults(Addr.class);
    }
    // List<Addr> addresses = query.getResults(Addr.class);

    container.setAdmin(admin);
    container.setData(data);
    if (addresses != null) {
      for (Addr addr : addresses) {
        container.addAddress(addr);
      }
    }
    return container;
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
   * Processes Request Type 'C'
   * 
   * @param entityManager
   * @param request
   * @param admin
   * @param data
   * @throws Exception
   */
  @Override
  @SuppressWarnings("unused")
  protected void processCreateRequest(EntityManager entityManager, ProcessRequest request, Admin admin, Data data) throws Exception {
    LOG.debug("Started Create processing of Request " + admin.getId().getReqId());
    String resultCode = "";
    String actionRdc = "";
    StringBuffer siteIds = new StringBuffer();
    String cmrNo = null;
    String applicationId = BatchUtil.getAppId(data.getCmrIssuingCntry());

    // lock
    lockRecord(entityManager, admin);

    CmrServiceInput cmrServiceInput = getReqParam(entityManager, admin.getId().getReqId(), admin.getReqType(), data);

    convertToProspectToLegalCMRInput(request, entityManager, request.getReqId());

    LOG.info("Sending request to Process Service [Request ID: " + request.getReqId() + " CMR No: " + request.getCmrNo() + " Type: "
        + request.getReqType() + "]");

    LOG.trace("Request JSON:");
    if (LOG.isTraceEnabled()) {
      DebugUtil.printObjectAsJson(LOG, request);
    }
    ProcessResponse response = null;

    String overallStatus = null;
    boolean prospectConversion = CmrConstants.YES_NO.Y.equals(admin.getProspLegalInd()) ? true : false;

    try {
      this.serviceClient.setReadTimeout(60 * 20 * 1000); // 10 mins
      response = this.serviceClient.executeAndWrap(applicationId, request, ProcessResponse.class);
    } catch (Exception e) {
      e.printStackTrace();
      LOG.error("Error when connecting to the service.", e);
      response = new ProcessResponse();
      response.setStatus(CmrConstants.RDC_STATUS_ABORTED);
      response.setReqId(request.getReqId());
      response.setCmrNo(request.getCmrNo());
      response.setMandt(request.getMandt());
      response.setMessage("Cannot connect to the service at the moment.");
    }
    resultCode = response.getStatus();

    LOG.trace("Response JSON:");
    if (LOG.isTraceEnabled()) {
      DebugUtil.printObjectAsJson(LOG, response);
    }

    LOG.info("Response received from Process Service [Request ID: " + response.getReqId() + " CMR No: " + response.getCmrNo() + " Status: "
        + response.getStatus() + " Message: " + (response.getMessage() != null ? response.getMessage() : "-") + "]");

    StringBuffer statusMessage = new StringBuffer();
    String currProcStatus = admin.getRdcProcessingStatus();

    actionRdc = "System Action:RDc Create";

    // if status is completed, we need to do something
    LOG.debug("Updating Admin table. Current Status: " + currProcStatus);

    if (CmrConstants.RDC_STATUS_COMPLETED.equals(response.getStatus())
        || CmrConstants.RDC_STATUS_COMPLETED_WITH_WARNINGS.equals(response.getStatus())) {
      overallStatus = CmrConstants.RDC_STATUS_COMPLETED;
      statusMessage
          .append("Record with request ID " + admin.getId().getReqId() + " and CMR Number " + response.getCmrNo() + " created SUCCESSFULLY. ");
      statusMessage.append("CMR No. " + response.getCmrNo() + " generated for this request. ");
      if (prospectConversion) {
        statusMessage.append(" RDc processing converted prospect " + cmrServiceInput + " to KUNNR(s): ");
      } else {
        statusMessage.append(" RDc processing successfully created KUNNR(s): ");
      }
      if (response.getRecords() != null && response.getRecords().size() != 0) {
        for (int i = 0; i < response.getRecords().size(); i++) {
          statusMessage.append(response.getRecords().get(i).getSapNo() + " ");
          if (StringUtils.isEmpty(siteIds.toString())) {
            siteIds.append(response.getRecords().get(i).getIerpSitePartyId());
          } else {
            siteIds.append(", " + response.getRecords().get(i).getIerpSitePartyId());
          }
        }
        if (CmrConstants.RDC_STATUS_COMPLETED_WITH_WARNINGS.equals(response.getStatus())) {
          if (StringUtils.isEmpty(siteIds.toString())) {
            siteIds.append("\nWarning Message: " + response.getMessage());
          } else {
            siteIds.append(", " + "\nWarning Message: " + response.getMessage());
          }
        }

      }
    } else if (CmrConstants.RDC_STATUS_ABORTED.equals(response.getStatus())) {
      overallStatus = CmrConstants.RDC_STATUS_ABORTED;
      statusMessage.append("Record with request ID " + admin.getId().getReqId() + " has FAILED processing. Status: ABORTED");
      SlackAlertsUtil.recordGenericAlert("USService", "Request " + admin.getId().getReqId(),
          SlackAlertsUtil.bold("Processing Result: " + resultCode + ", Message: " + response.getMessage()));
    } else if (CmrConstants.RDC_STATUS_NOT_COMPLETED.equals(response.getStatus())) {
      overallStatus = CmrConstants.RDC_STATUS_NOT_COMPLETED;
      statusMessage.append("Record with request ID " + admin.getId().getReqId() + " has FAILED processing. Status: NOT COMPLETED. Response message: "
          + response.getMessage());
      SlackAlertsUtil.recordGenericAlert("USService", "Request " + admin.getId().getReqId(),
          SlackAlertsUtil.bold("Processing Result: " + resultCode + ", Message: " + response.getMessage()));
    } else {
      statusMessage.append("Record with request ID " + admin.getId().getReqId() + " has FAILED processing. Status: ABORTED ");

      if (response.getStatus() == null) {
        statusMessage.append("Service Response: EMPTY SERVICE STATUS ON RESPONSE");
      } else {
        statusMessage.append("Service Response: " + response.getStatus());
      }
      SlackAlertsUtil.recordGenericAlert("USService", "Request " + admin.getId().getReqId(),
          SlackAlertsUtil.bold("Processing Result: " + resultCode + ", Message: " + response.getMessage()));

    }

    createCommentLog(entityManager, admin, statusMessage.toString());

    String disableAutoProc = "N";
    // if (CmrConstants.RDC_STATUS_COMPLETED.equals(overallStatus)) {
    // disableAutoProc = CmrConstants.YES_NO.Y.toString();
    // } else {
    // disableAutoProc = CmrConstants.YES_NO.N.toString();
    // }

    // update admin status
    admin.setDisableAutoProc(disableAutoProc);
    LOG.debug("*** Setting DISABLE_AUTO_PROC >> " + admin.getDisableAutoProc());
    admin.setProcessedFlag(
        CmrConstants.RDC_STATUS_COMPLETED.equals(overallStatus) ? CmrConstants.YES_NO.Y.toString() : CmrConstants.YES_NO.N.toString());
    LOG.debug("*** Setting PROCESSED_FLAG >> " + admin.getProcessedFlag());

    // edited to return the request back to processor if an error
    // occurred, to avoid endless loop

    if (CmrConstants.RDC_STATUS_COMPLETED.equals(overallStatus)) {
      admin.setReqStatus(CmrConstants.REQUEST_STATUS.COM.toString());
    } else {
      LOG.debug("Unlocking request due to error..");
      admin.setReqStatus(CmrConstants.REQUEST_STATUS.PPN.toString());
      admin.setLockBy(null);
      admin.setLockByNm(null);
      admin.setLockTs(null);
      admin.setLockInd(CmrConstants.YES_NO.N.toString());
    }

    LOG.debug("*** Setting REQ_STATUS >> " + admin.getReqStatus());

    if (CmrConstants.RDC_STATUS_NOT_COMPLETED.equals(response.getStatus()) || CmrConstants.RDC_STATUS_ABORTED.equals(response.getStatus())) {
      admin.setRdcProcessingStatus(CmrConstants.RDC_STATUS_NOT_COMPLETED);
    } else {
      admin.setRdcProcessingStatus(overallStatus);
    }
    LOG.debug("*** Setting RDC_PROCESSING_STATUS >> " + admin.getRdcProcessingStatus());

    admin.setRdcProcessingMsg(response.getMessage());
    admin.setRdcProcessingTs(SystemUtil.getCurrentTimestamp());
    updateEntity(admin, entityManager);

    // update cmr_no on data if create
    if (CmrConstants.RDC_STATUS_COMPLETED.equals(overallStatus)) {
      // Data data = entity.getEntity(Data.class);
      data.setCmrNo(response.getCmrNo());
      updateEntity(data, entityManager);
    } else {
      data.setCmrNo("");
      updateEntity(data, entityManager);
      LOG.debug("*** Response CMR_No >> " + response.getCmrNo());
    }

    // update addr
    if (CmrConstants.RDC_STATUS_COMPLETED.equals(overallStatus)) {
      // see below query
      String sql = ExternalizedQuery.getSql("BATCH.GET_ADDR_FOR_SAP_NO_SORT_BY_ADDR_TYPE");
      PreparedQuery query3 = new PreparedQuery(entityManager, sql);
      query3.setParameter("REQ_ID", admin.getId().getReqId());
      List<Addr> addresses = query3.getResults(Addr.class);
      int index = 0;
      int rdcindex = 0;

      for (Addr addr : addresses) {
        if (index > addresses.size()) {
          LOG.error("***Cannot continue update of ADDR table for REQ_ID" + admin.getId().getReqId()
              + ". Number of response records and on ADDR table are inconsistent.");
          break;
        }

        if (response.getRecords() != null && response.getRecords().size() != 0) {

          for (RDcRecord red : response.getRecords()) {

            if (("ZS01".equalsIgnoreCase(red.getAddressType()) || "ZI01".equalsIgnoreCase(red.getAddressType()))
                && red.getSeqNo().equalsIgnoreCase(addr.getId().getAddrSeq())) {
              addr.setPairedAddrSeq(red.getSeqNo());
              addr.setSapNo(red.getSapNo());
              addr.setIerpSitePrtyId(red.getIerpSitePartyId());
            }

            if (("ZP01").equalsIgnoreCase(red.getAddressType()) && red.getSeqNo().equalsIgnoreCase(addr.getId().getAddrSeq())) {
              addr.setPairedAddrSeq(red.getSeqNo());
              addr.setSapNo(red.getSapNo());
              addr.setIerpSitePrtyId(red.getIerpSitePartyId());
            }
            rdcindex++;
          }

          updateEntity(addr, entityManager);
        }
        index++;
      }
    }

    if (!CmrConstants.RDC_STATUS_IGNORED.equals(response.getStatus())) {
      IERPRequestUtils.createWorkflowHistoryFromBatch(entityManager, BATCH_USER_ID, admin, statusMessage.toString().trim(), actionRdc, null, null,
          "COM".equals(admin.getReqStatus()));
    }

    partialCommit(entityManager);

    // send email notif regardless of abort or complete
    LOG.debug("*** IERP Site IDs on EMAIL >> " + siteIds.toString());
    try {
      sendEmailNotifications(entityManager, admin, siteIds.toString(), statusMessage.toString());
    } catch (Exception e) {
      e.printStackTrace();
      LOG.error("ERROR: " + e.getMessage());
    }
  }

  @Override
  protected void updateRequestAddress(EntityManager entityManager, Admin admin, Data data, RDcRecord record) {
    AddrPK pk = new AddrPK();
    pk.setReqId(admin.getId().getReqId());
    pk.setAddrType(record.getAddressType());
    pk.setAddrSeq(record.getSeqNo());
    Addr addr = entityManager.find(Addr.class, pk);
    if (addr != null) {
      addr.setSapNo(record.getSapNo());
      addr.setIerpSitePrtyId(record.getIerpSitePartyId());
      LOG.info("Address Record Updated [Request ID: " + addr.getId().getReqId() + " Type: " + addr.getId().getAddrType() + " SAP No: "
          + record.getSapNo() + "]");
      updateEntity(addr, entityManager);
    }
  }

  private DataRdc getDataRdcRecords(EntityManager entityManager, Data data) {
    LOG.debug("Searching for DATA_RDC records for US Processing " + data.getId().getReqId());
    String sql = ExternalizedQuery.getSql("SUMMARY.OLDDATA");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", data.getId().getReqId());
    query.setForReadOnly(true);
    return query.getSingleResult(DataRdc.class);
  }

  private AddrRdc getAddrRdcRecords(EntityManager entityManager, Addr addr) {
    LOG.debug("Searching for ADDR_RDC records for Request " + addr.getId().getReqId());
    String sql = ExternalizedQuery.getSql("REQUESTENTRY.ADDRRDC.SEARCH_BY_REQID_TYPE_SEQ");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", addr.getId().getReqId());
    query.setParameter("ADDR_TYPE", addr.getId().getAddrType());
    query.setParameter("ADDR_SEQ", addr.getId().getAddrSeq());
    query.setForReadOnly(true);
    return query.getSingleResult(AddrRdc.class);
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
  @Override
  @SuppressWarnings("unused")
  protected void processUpdateRequest(EntityManager entityManager, ProcessRequest request, Admin admin, Data data) throws Exception {
    HashMap<String, Object> overallResponse = new HashMap<String, Object>();
    List<ProcessResponse> responses = new ArrayList<ProcessResponse>();
    List<String> respStatuses = new ArrayList<String>();
    ProcessResponse response = null;
    String actionRdc = "System Action:RDc Update";
    StringBuffer statusMessage = new StringBuffer();
    String overallStatus = null;
    String wfHistCmt = "";
    String rdcProcessingMessage = "";
    String siteIds = "";
    String resultCode = null;
    String processingStatus = admin.getRdcProcessingStatus() != null ? admin.getRdcProcessingStatus() : "";
    boolean isIndexNotUpdated = false;
    boolean isTempReactivate = false;
    DataRdc dataRdc = getDataRdcRecords(entityManager, data);
    long reqId = admin.getId().getReqId();

    CmrServiceInput cmrServiceInput = getReqParam(entityManager, admin.getId().getReqId(), admin.getReqType(), data);
    request.setCmrNo(cmrServiceInput.getInputCmrNo());
    request.setMandt(cmrServiceInput.getInputMandt());
    request.setReqId(cmrServiceInput.getInputReqId());
    request.setReqType(cmrServiceInput.getInputReqType());
    request.setUserId(cmrServiceInput.getInputUserId());

    if ("TREC".equalsIgnoreCase(admin.getReqReason()) && (data.getOrdBlk() == null || StringUtils.isEmpty(data.getOrdBlk()))
        && "88".equals(dataRdc.getOrdBlk()))
      isTempReactivate = true;
    // Process update
    if (isTempReactivate) {
      // Process Temporary Reactivate Requests Type Only
      if ((admin.getRdcProcessingTs() == null || isTimeStampEquals(admin.getRdcProcessingTs())) && "N".equals(admin.getProcessedFlag())) {
        LOG.info("Temporary Reactivate Embargo process: Batch First run for Req Id :" + admin.getId().getReqId());
        try {

          List<Addr> addresses = this.cmrObjects.getAddresses();

          List<String> statusCodes = new ArrayList<String>();

          StringBuilder comment = new StringBuilder();

          Set<String> usedSequences = new HashSet<String>();
          String applicationId = BatchUtil.getAppId(data.getCmrIssuingCntry());

          boolean isDataUpdated = false;
          isDataUpdated = USHandler.isDataUpdated(data, dataRdc, data.getCmrIssuingCntry());
          lockRecordUpdt(entityManager, admin);

          for (Addr addr : addresses) {
            entityManager.detach(addr);
            if (usedSequences.contains(addr.getId().getAddrSeq())) {
              LOG.warn("Sequence " + addr.getId().getAddrSeq() + " already sent in a previous request. Skipping.");
              continue;
            }
            if (!isDataUpdated && "Y".equals(addr.getImportInd()) && !"Y".equals(addr.getChangedIndc())) {
              LOG.warn("Sequence " + addr.getId().getAddrSeq() + " address not updated. Skipping.");
              continue;
            }
            request.setSapNo(addr.getSapNo());

            request.setAddrType(addr.getId().getAddrType());

            request.setSeqNo(addr.getId().getAddrSeq());

            // call the create cmr service
            LOG.info("Sending request to Process Service for TREC [Request ID: " + request.getReqId() + " CMR No: " + request.getCmrNo() + " Type: "
                + request.getReqType() + " SAP No: " + request.getSapNo() + "]");

            if (LOG.isTraceEnabled()) {
              LOG.trace("Request JSON:");
              DebugUtil.printObjectAsJson(LOG, request);
            }

            response = null;
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
                this.serviceClient.setReadTimeout(60 * 25 * 1000); // 25 mins
                response = this.serviceClient.executeAndWrap(applicationId, request, ProcessResponse.class);
              } catch (Exception e) {
                LOG.error("Error when connecting to the service.", e);
                response = new ProcessResponse();
                response.setReqId(admin.getId().getReqId());
                response.setCmrNo(request.getCmrNo());
                response.setMandt(request.getMandt());
                response.setStatus(CmrConstants.RDC_STATUS_ABORTED);
                response.setMessage("Cannot connect to the service at the moment.");
              }
            }
            addr = entityManager.find(Addr.class, addr.getId());
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

            LOG.info("Response received from Process Service for TREC [Request ID: " + response.getReqId() + " CMR No: " + response.getCmrNo()
                + " KUNNR: " + addr.getSapNo() + " Status: " + response.getStatus() + " Message: "
                + (response.getMessage() != null ? response.getMessage() : "-") + "]");

            // get the results from the service and process jason response

            // create comment log and workflow history entries for update type
            // of
            // request
            if (isCompletedSuccessfully(resultCode)) {

              addr.setRdcLastUpdtDt(SystemUtil.getCurrentTimestamp());
              if (response.getRecords() != null) {
                for (RDcRecord rdcRec : response.getRecords()) {
                  if (rdcRec.getAddressType().equals(addr.getId().getAddrType()) && rdcRec.getSeqNo().equals(addr.getId().getAddrSeq())
                      && StringUtils.isEmpty(addr.getSapNo())) {
                    LOG.debug("Updating SAP No. to " + rdcRec.getSapNo() + " for Type " + rdcRec.getAddressType() + "/" + rdcRec.getSeqNo());
                    addr.setSapNo(rdcRec.getSapNo());
                    addr.setIerpSitePrtyId(rdcRec.getIerpSitePartyId());
                  }
                }
              }
              updateEntity(addr, entityManager);

              if (response.getRecords() != null) {
                comment = comment.append("\nSuccessfully processed in RDc KUNNR: ");
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
                comment = comment.append("\nRDc update processing for KUNNR " + (request.getSapNo() != null ? request.getSapNo() : "(not generated)")
                    + " failed. Error: " + response.getMessage());
              } else if (CmrConstants.RDC_STATUS_ABORTED.equalsIgnoreCase(resultCode)) {
                comment = comment.append("\nRDc update processing for KUNNR " + (request.getSapNo() != null ? request.getSapNo() : "(not generated)")
                    + " failed. Error: " + response.getMessage() + " System will retry processing once.");
              } else if (CmrConstants.RDC_STATUS_NOT_COMPLETED.equalsIgnoreCase(resultCode)) {
                comment = comment.append("\nRDc update processing for KUNNR " + (request.getSapNo() != null ? request.getSapNo() : "(not generated)")
                    + " failed. Error: " + response.getMessage());
              } else if (CmrConstants.RDC_STATUS_IGNORED.equalsIgnoreCase(resultCode)) {
                comment = comment.append("\nUpdate processing for " + (addr.getId().getAddrType() + "/" + addr.getId().getAddrSeq())
                    + " in RDc skipped: " + response.getMessage());
              }
              SlackAlertsUtil.recordGenericAlert("USService", "Request " + admin.getId().getReqId(),
                  SlackAlertsUtil.bold("Processing Result: " + resultCode + ", Message: " + response.getMessage()));
            }

            usedSequences.add(addr.getId().getAddrSeq());

          }
          comment = comment.append("\nTemporary Reactivate Embargo process in RDc started.");

          RequestUtils.createCommentLogFromBatch(entityManager, BATCH_USER_ID, reqId, comment.toString().trim());

          LOG.debug("Updating Admin record for Request ID " + admin.getId().getReqId());

          // only update Admin record once depending on the overall status of
          // the
          // request
          if (statusCodes.contains(CmrConstants.RDC_STATUS_NOT_COMPLETED)) {
            admin.setRdcProcessingStatus(CmrConstants.RDC_STATUS_NOT_COMPLETED);
            admin.setProcessedFlag("N");
          } else if (statusCodes.contains(CmrConstants.RDC_STATUS_ABORTED)) {
            admin.setRdcProcessingStatus(
                processingStatus.equals(CmrConstants.RDC_STATUS_ABORTED) ? CmrConstants.RDC_STATUS_NOT_COMPLETED : CmrConstants.RDC_STATUS_ABORTED);
            admin.setProcessedFlag("E");
          } else if (statusCodes.contains(CmrConstants.RDC_STATUS_COMPLETED_WITH_WARNINGS)) {
            admin.setRdcProcessingStatus(CmrConstants.RDC_STATUS_COMPLETED_WITH_WARNINGS);
            admin.setProcessedFlag("E");
          } else {
            admin.setRdcProcessingStatus(CmrConstants.RDC_STATUS_COMPLETED);
            admin.setProcessedFlag("Wx");
          }

          String rdcProcessingMsg = null;
          if ("N".equals(admin.getRdcProcessingStatus()) || "A".equals(admin.getRdcProcessingStatus())) {
            rdcProcessingMsg = "Some errors occurred during processing. Please check request's comment log for details.";
          } else {
            rdcProcessingMsg = "RDc Processing has been completed(first batch run) for Temporary Reactivate Embargo Code. Please check request's comment log for details.";
          }

          LOG.debug("Unlocking request to be picked by batch for second run ...");
          admin.setLockInd(CmrConstants.YES_NO.N.toString());

          admin.setRdcProcessingMsg(rdcProcessingMsg.toString().trim());
          if (!"A".equals(admin.getRdcProcessingStatus()) && !"N".equals(admin.getRdcProcessingStatus())) {
            admin.setRdcProcessingTs(SystemUtil.getCurrentTimestamp());
            admin.setReqStatus("CPR");
          } else if ("A".equals(admin.getRdcProcessingStatus()) || "N".equals(admin.getRdcProcessingStatus())) {
            admin.setReqStatus("PPN");
            admin.setProcessedTs(getZeroDate());
            admin.setRdcProcessingTs(getZeroDate());
          }

          updateEntity(admin, entityManager);

          if ("N".equals(admin.getRdcProcessingStatus()) || "A".equals(admin.getRdcProcessingStatus())) {
            RequestUtils.createWorkflowHistoryFromBatch(entityManager, BATCH_USER_ID, admin,
                "Some errors occurred during RDc processing. Please check request's comment log for details.", ACTION_RDC_UPDATE, null, null,
                "CPR".equals(admin.getReqStatus()));
          } else {
            RequestUtils.createWorkflowHistoryFromBatch(entityManager, BATCH_USER_ID, admin,
                "RDc  Processing has been completed(First batch run). Please check request's comment log for details.", ACTION_RDC_UPDATE, null, null,
                "CPR".equals(admin.getReqStatus()));
          }

          partialCommit(entityManager);
          LOG.debug(
              "Request ID " + admin.getId().getReqId() + " Status: " + admin.getRdcProcessingStatus() + " Message: " + admin.getRdcProcessingMsg());

        } catch (Exception e) {
          LOG.error("Error in processing Update Request " + admin.getId().getReqId(), e);
          addError("Update Request " + admin.getId().getReqId() + " Error: " + e.getMessage());
        }
        LOG.info("End First run Temporary Reactivate Embargo process...");

      } else if ("Wx".equals(admin.getProcessedFlag())) {
        long noOFWorkingHours = 0;
        if (admin.getReqStatus() != null && admin.getReqStatus().equals(CMR_REQUEST_STATUS_CPR)) {
          noOFWorkingHours = checked2WorkingDays(admin.getRdcProcessingTs(), SystemUtil.getCurrentTimestamp());
        }
        if (noOFWorkingHours >= 48) {
          lockRecordUpdt(entityManager, admin);
          LOG.info("RDc: Temporary Reactivate Embargo process: run after 2 working days for Req Id :" + admin.getId().getReqId());
          try {
            admin.setRdcProcessingTs(SystemUtil.getCurrentTimestamp());
            List<Addr> addresses = this.cmrObjects.getAddresses();

            List<String> statusCodes = new ArrayList<String>();

            StringBuilder comment = new StringBuilder();

            Set<String> usedSequences = new HashSet<String>();
            String applicationId = BatchUtil.getAppId(data.getCmrIssuingCntry());

            boolean isDataUpdated = false;
            isDataUpdated = USHandler.isDataUpdated(data, dataRdc, data.getCmrIssuingCntry());

            data.setOrdBlk("88");
            updateEntity(data, entityManager);

            for (Addr addr : addresses) {
              entityManager.detach(addr);
              if (usedSequences.contains(addr.getId().getAddrSeq())) {
                LOG.warn("Sequence " + addr.getId().getAddrSeq() + " already sent in a previous request. Skipping.");
                continue;
              }

              if (!isDataUpdated && "Y".equals(addr.getImportInd()) && !"Y".equals(addr.getChangedIndc())) {
                LOG.warn("Sequence " + addr.getId().getAddrSeq() + " address not updated. Skipping.");
                continue;
              }
              request.setSapNo(addr.getSapNo());

              request.setAddrType(addr.getId().getAddrType());

              request.setSeqNo(addr.getId().getAddrSeq());

              // call the create cmr service
              LOG.info("Sending request to Process Service [Request ID: " + request.getReqId() + " CMR No: " + request.getCmrNo() + " Type: "
                  + request.getReqType() + " SAP No: " + request.getSapNo() + "]");

              if (LOG.isTraceEnabled()) {
                LOG.trace("Request JSON:");
                DebugUtil.printObjectAsJson(LOG, request);
              }

              response = null;
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
                  this.serviceClient.setReadTimeout(60 * 20 * 1000); // 10 mins
                  response = this.serviceClient.executeAndWrap(applicationId, request, ProcessResponse.class);
                } catch (Exception e) {
                  LOG.error("Error when connecting to the service.", e);
                  response = new ProcessResponse();
                  response.setReqId(admin.getId().getReqId());
                  response.setCmrNo(request.getCmrNo());
                  response.setMandt(request.getMandt());
                  response.setStatus(CmrConstants.RDC_STATUS_ABORTED);
                  response.setMessage("Cannot connect to the service at the moment.");
                }
              }
              addr = entityManager.find(Addr.class, addr.getId());
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

              LOG.info("Response received from Process Service [Request ID: " + response.getReqId() + " CMR No: " + response.getCmrNo() + " KUNNR: "
                  + addr.getSapNo() + " Status: " + response.getStatus() + " Message: "
                  + (response.getMessage() != null ? response.getMessage() : "-") + "]");

              // get the results from the service and process jason response

              // create comment log and workflow history entries for update type
              // of
              // request
              if (isCompletedSuccessfully(resultCode)) {

                addr.setRdcLastUpdtDt(SystemUtil.getCurrentTimestamp());
                if (response.getRecords() != null) {
                  for (RDcRecord rdcRec : response.getRecords()) {
                    if (rdcRec.getAddressType().equals(addr.getId().getAddrType()) && rdcRec.getSeqNo().equals(addr.getId().getAddrSeq())
                        && StringUtils.isEmpty(addr.getSapNo())) {
                      LOG.debug("Updating SAP No. to " + rdcRec.getSapNo() + " for Type " + rdcRec.getAddressType() + "/" + rdcRec.getSeqNo());
                      addr.setSapNo(rdcRec.getSapNo());
                      addr.setIerpSitePrtyId(rdcRec.getIerpSitePartyId());
                    }
                  }
                }
                updateEntity(addr, entityManager);

                if (response.getRecords() != null) {
                  comment = comment.append("\nSuccessfully processed in RDc KUNNR: ");
                  if (response.getRecords() != null && response.getRecords().size() != 0) {
                    for (int i = 0; i < response.getRecords().size(); i++) {
                      comment = comment.append(response.getRecords().get(i).getSapNo() + " ");
                    }
                    comment = comment.append("\nTemporary Reactivate Embargo process in RDc ended.");
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
                  comment = comment.append("\nRDc update processing for KUNNR "
                      + (request.getSapNo() != null ? request.getSapNo() : "(not generated)") + " failed. Error: " + response.getMessage());
                } else if (CmrConstants.RDC_STATUS_ABORTED.equalsIgnoreCase(resultCode)) {
                  comment = comment
                      .append("\nRDc update processing for KUNNR " + (request.getSapNo() != null ? request.getSapNo() : "(not generated)")
                          + " failed. Error: " + response.getMessage() + " System will retry processing once.");
                } else if (CmrConstants.RDC_STATUS_NOT_COMPLETED.equalsIgnoreCase(resultCode)) {
                  comment = comment.append("\nRDc update processing for KUNNR "
                      + (request.getSapNo() != null ? request.getSapNo() : "(not generated)") + " failed. Error: " + response.getMessage());
                } else if (CmrConstants.RDC_STATUS_IGNORED.equalsIgnoreCase(resultCode)) {
                  comment = comment.append("\nUpdate processing for " + (addr.getId().getAddrType() + "/" + addr.getId().getAddrSeq())
                      + " in RDc skipped: " + response.getMessage());
                }
              }

              usedSequences.add(addr.getId().getAddrSeq());

            }

            RequestUtils.createCommentLogFromBatch(entityManager, BATCH_USER_ID, reqId, comment.toString().trim());

            LOG.debug("Updating Admin record for Request ID " + admin.getId().getReqId());

            // only update Admin record once depending on the overall status of
            // the
            // request
            if (statusCodes.contains(CmrConstants.RDC_STATUS_NOT_COMPLETED)) {
              admin.setRdcProcessingStatus(CmrConstants.RDC_STATUS_NOT_COMPLETED);
              admin.setProcessedFlag("N");
            } else if (statusCodes.contains(CmrConstants.RDC_STATUS_ABORTED)) {
              admin.setRdcProcessingStatus(
                  processingStatus.equals(CmrConstants.RDC_STATUS_ABORTED) ? CmrConstants.RDC_STATUS_NOT_COMPLETED : CmrConstants.RDC_STATUS_ABORTED);
              admin.setProcessedFlag("N");
            } else if (statusCodes.contains(CmrConstants.RDC_STATUS_COMPLETED_WITH_WARNINGS)) {
              admin.setRdcProcessingStatus(CmrConstants.RDC_STATUS_COMPLETED_WITH_WARNINGS);
              admin.setProcessedFlag("N");
            } else {
              admin.setRdcProcessingStatus(CmrConstants.RDC_STATUS_COMPLETED);
              admin.setProcessedFlag("Y");
            }

            String rdcProcessingMsg = null;
            if ("N".equals(admin.getRdcProcessingStatus()) || "A".equals(admin.getRdcProcessingStatus())) {
              rdcProcessingMsg = "Some errors occurred during processing. Please check request's comment log for details.";
            } else {
              rdcProcessingMsg = "RDc Processing has been completed(Second batch run). Please check request's comment log for details.";
            }

            admin.setRdcProcessingTs(SystemUtil.getCurrentTimestamp());
            admin.setRdcProcessingMsg(rdcProcessingMsg.toString().trim());
            if (!"A".equals(admin.getRdcProcessingStatus()) && !"N".equals(admin.getRdcProcessingStatus())) {
              admin.setReqStatus("COM");
            } else if ("A".equals(admin.getRdcProcessingStatus()) || "N".equals(admin.getRdcProcessingStatus())) {
              admin.setReqStatus("PPN");
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

          } catch (Exception e) {
            LOG.error("Error in processing Update Request " + admin.getId().getReqId(), e);
            addError("Update Request " + admin.getId().getReqId() + " Error: " + e.getMessage());
          }
        }
      }

      // ----- Temporary Reactivate Requests Type Code handling END!!!
    } else {
      // handling simple update requests
      try {
        lockRecordUpdt(entityManager, admin);
        // 1. Get first the ones that are new -- BATCH.GET_NEW_ADDR_FOR_UPDATE
        String sql = ExternalizedQuery.getSql("BATCH.GET_NEW_ADDR_FOR_UPDATE");
        PreparedQuery query = new PreparedQuery(entityManager, sql);
        query.setParameter("REQ_ID", admin.getId().getReqId());
        List<Addr> addresses = query.getResults(Addr.class);
        // CREATCMR-7152
        boolean isSkipFlg = false;
        if (addresses != null && addresses.size() > 0) {
          for (Addr addr : addresses) {
            // setting sequence no.
            // CREATCMR-7152
            isSkipFlg = false;
            if ("ZS01".equals(addr.getId().getAddrType()) || "ZI01".equals(addr.getId().getAddrType()) || "ZZ01".equals(addr.getId().getAddrType())) {
              if (!"001".equals(addr.getId().getAddrSeq()) && !"002".equals(addr.getId().getAddrSeq())) {
                isSkipFlg = true;
              }
            }
            if (isSkipFlg) {
              LOG.debug("AddrType is " + addr.getId().getAddrType() + " and addrSeq is " + addr.getId().getAddrSeq() + " is skip.");
              continue;
            }
            response = sendAddrForProcessing(addr, request, responses, isIndexNotUpdated, siteIds, entityManager, isTempReactivate);
            respStatuses.add(response.getStatus());
          }
        }

        // 2. Get the ones that are updated -- BATCH.GET_UPDATED_ADDR_FOR_UPDATE
        sql = ExternalizedQuery.getSql("BATCH.GET_UPDATED_ADDR_FOR_UPDATE");
        query = new PreparedQuery(entityManager, sql);
        query.setParameter("REQ_ID", admin.getId().getReqId());
        addresses = query.getResults(Addr.class);
        List<Addr> notProcessed = new ArrayList<Addr>();

        if (addresses != null && addresses.size() > 0) {
          for (Addr addr : addresses) {
            // CREATCMR-7152
            isSkipFlg = false;
            if ("ZS01".equals(addr.getId().getAddrType()) || "ZI01".equals(addr.getId().getAddrType()) || "ZZ01".equals(addr.getId().getAddrType())) {
              if (!"001".equals(addr.getId().getAddrSeq()) && !"002".equals(addr.getId().getAddrSeq())) {
                isSkipFlg = true;
              }
            }
            if (isSkipFlg) {
              LOG.debug("AddrType is " + addr.getId().getAddrType() + " and addrSeq is " + addr.getId().getAddrSeq() + " is skip.");
              continue;
            }
            AddrRdc addrRdc = getAddrRdcRecords(entityManager, addr);
            boolean isAddrUpdated = false;

            if ("897".equals(data.getCmrIssuingCntry())) {
              USHandler swHandler = new USHandler();
              isAddrUpdated = swHandler.isAddrUpdated(addr, addrRdc, data.getCmrIssuingCntry());
            }
            if (isAddrUpdated) {
              response = sendAddrForProcessing(addr, request, responses, isIndexNotUpdated, siteIds, entityManager, isTempReactivate);
              respStatuses.add(response.getStatus());
            } else {
              notProcessed.add(addr);
            }
          }
        }

        // 3. Check if there are customer and IBM changes, propagate to other
        // addresses
        boolean isDataUpdated = false;

        if ("897".equals(data.getCmrIssuingCntry())) {
          isDataUpdated = USHandler.isDataUpdated(data, dataRdc, data.getCmrIssuingCntry());
        }

        if (isDataUpdated && (notProcessed != null && notProcessed.size() > 0)) {
          LOG.debug("Processing CMR Data changes to " + notProcessed.size() + " addresses of CMR# " + data.getCmrNo());
          for (Addr addr : notProcessed) {
            // CREATCMR-7152
            isSkipFlg = false;
            if ("ZS01".equals(addr.getId().getAddrType()) || "ZI01".equals(addr.getId().getAddrType()) || "ZZ01".equals(addr.getId().getAddrType())) {
              if (!"001".equals(addr.getId().getAddrSeq()) && !"002".equals(addr.getId().getAddrSeq())) {
                isSkipFlg = true;
              }
            }
            if (isSkipFlg) {
              LOG.debug("AddrType is " + addr.getId().getAddrType() + " and addrSeq is " + addr.getId().getAddrSeq() + " is skip.");
              continue;
            }
            response = sendAddrForProcessing(addr, request, responses, isIndexNotUpdated, siteIds, entityManager, isTempReactivate);
            respStatuses.add(response.getStatus());
          }
        }

        if (respStatuses.size() > 0) {
          if (respStatuses.contains(CmrConstants.RDC_STATUS_ABORTED)) {
            if (isIndexNotUpdated) {
              overallResponse.put("overallStatus", CmrConstants.RDC_STATUS_COMPLETED);
            } else {
              overallResponse.put("overallStatus", CmrConstants.RDC_STATUS_ABORTED);
            }
          } else if (respStatuses.contains(CmrConstants.RDC_STATUS_NOT_COMPLETED)) {
            overallResponse.put("overallStatus", CmrConstants.RDC_STATUS_NOT_COMPLETED);
          } else if (respStatuses.contains(CmrConstants.RDC_STATUS_COMPLETED)) {
            overallResponse.put("overallStatus", CmrConstants.RDC_STATUS_COMPLETED);
          }
        } else {
          LOG.error("Response statuses is empty for request ID: " + admin.getId().getReqId());
          ProcessResponse customResponse = new ProcessResponse();
          responses = new ArrayList<ProcessResponse>();
          customResponse.setMessage("No data was updated on RDc for this request. Please contact Ops for assistance.");
          responses.add(customResponse);
          overallResponse.put("overallStatus", CmrConstants.RDC_STATUS_ABORTED);
        }

        overallResponse.put("siteIds", siteIds);
        overallResponse.put("responses", responses);

        if (overallResponse != null) {

          overallStatus = (String) overallResponse.get("overallStatus");
          responses = (List<ProcessResponse>) overallResponse.get("responses");

          if (CmrConstants.RDC_STATUS_COMPLETED.equals(overallStatus) || CmrConstants.RDC_STATUS_COMPLETED_WITH_WARNINGS.equals(overallStatus)) {
            wfHistCmt = "All address records on request ID " + admin.getId().getReqId() + " were SUCCESSFULLY processed";
            statusMessage.append("Record with the following CMR number, Address sequence and address types on request ID " + admin.getId().getReqId()
                + " was SUCCESSFULLY processed:\n");
            if (responses != null && responses.size() > 0) {
              for (int i = 0; i < responses.size(); i++) {
                ProcessResponse resp = responses.get(i);

                if (resp != null && resp.getRecords() != null && resp.getRecords().size() > 0) {
                  if (i == 0) {
                    rdcProcessingMessage = resp.getMessage();
                  }
                  statusMessage.append("CMR Number " + resp.getCmrNo() + ", sequence number " + resp.getRecords().get(0).getSeqNo() + ", ");
                  statusMessage.append(" address type " + resp.getRecords().get(0).getAddressType() + "\n");
                }
                if (CmrConstants.RDC_STATUS_COMPLETED_WITH_WARNINGS.equals(overallStatus)) {
                  statusMessage.append("\nWarning Message: " + resp.getMessage());
                }
              }
            }
          } else if (CmrConstants.RDC_STATUS_ABORTED.equals(overallStatus)) {
            statusMessage.append("Record with request ID " + admin.getId().getReqId() + " has FAILED processing. Status: ABORTED");

            if (responses != null && responses.size() > 0) {
              ProcessResponse resp = responses.get(0);
              statusMessage.append(". Reason: " + resp.getMessage());
            }

            wfHistCmt = statusMessage.toString();
            if (responses != null && responses.size() > 0) {
              ProcessResponse resp = responses.get(0);
              rdcProcessingMessage = resp.getMessage();
            }

          } else if (CmrConstants.RDC_STATUS_NOT_COMPLETED.equals(overallStatus)) {
            String ncMessage = "";
            if (responses != null && responses.size() > 0) {
              ProcessResponse resp = responses.get(0);
              rdcProcessingMessage = resp.getMessage();
              ncMessage = resp.getMessage();
            }

            statusMessage.append("Record with request ID " + admin.getId().getReqId()
                + " has FAILED processing. Status: NOT COMPLETED. Response message: " + ncMessage);
            wfHistCmt = statusMessage.toString();
          }

          // createCommentLog(entityManager, admin, statusMessage.toString());
          RequestUtils.createCommentLogFromBatch(entityManager, BATCH_USER_ID, reqId, statusMessage.toString().trim());

          String disableAutoProc = "N";
          // if (CmrConstants.RDC_STATUS_COMPLETED.equals(overallStatus)) {
          // disableAutoProc = CmrConstants.YES_NO.Y.toString();
          // } else {
          // disableAutoProc = CmrConstants.YES_NO.N.toString();
          // }

          // update admin status
          admin.setDisableAutoProc(disableAutoProc);
          LOG.debug("*** Setting DISABLE_AUTO_PROC >> " + admin.getDisableAutoProc());
          admin.setProcessedFlag(
              CmrConstants.RDC_STATUS_COMPLETED.equals(overallStatus) ? CmrConstants.YES_NO.Y.toString() : CmrConstants.YES_NO.N.toString());
          LOG.debug("*** Setting PROCESSED_FLAG >> " + admin.getProcessedFlag());

          /*
           * jzamora - edited to return the request back to processor if an
           * error occurred, to avoid endless loop
           */
          if (CmrConstants.RDC_STATUS_COMPLETED.equals(overallStatus)) {
            admin.setReqStatus(CmrConstants.REQUEST_STATUS.COM.toString());
          } else {
            LOG.debug("Unlocking request due to error..");
            admin.setReqStatus(CmrConstants.REQUEST_STATUS.PPN.toString());
            admin.setLockBy(null);
            admin.setLockByNm(null);
            admin.setLockTs(null);
            admin.setLockInd(CmrConstants.YES_NO.N.toString());
          }

          LOG.debug("*** Setting REQ_STATUS >> " + admin.getReqStatus());

          if (CmrConstants.RDC_STATUS_NOT_COMPLETED.equals(overallStatus)) {
            admin.setRdcProcessingStatus(CmrConstants.RDC_STATUS_NOT_COMPLETED);
          } else if (CmrConstants.RDC_STATUS_ABORTED.equals(overallStatus)) {
            admin.setRdcProcessingStatus(CmrConstants.RDC_STATUS_ABORTED);
            admin.setProcessedFlag("E");
          } else {
            admin.setRdcProcessingStatus(overallStatus);
          }
          LOG.debug("*** Setting RDC_PROCESSING_STATUS >> " + admin.getRdcProcessingStatus());

          admin.setRdcProcessingMsg(rdcProcessingMessage);
          admin.setRdcProcessingTs(SystemUtil.getCurrentTimestamp());
          updateEntity(admin, entityManager);

          siteIds = overallResponse.get("siteIds").toString();

          if (!CmrConstants.RDC_STATUS_IGNORED.equals(overallStatus)) {
            RequestUtils.createWorkflowHistoryFromBatch(entityManager, BATCH_USER_ID, admin, wfHistCmt.trim(), actionRdc, null, null,
                "COM".equals(admin.getReqStatus()));
          }

          partialCommit(entityManager);

          // send email notif regardless of abort or complete
          LOG.debug("*** IERP Site IDs on EMAIL >> " + siteIds.toString());
          // try {
          // sendEmailNotifications(entityManager, admin, siteIds.toString(),
          // statusMessage.toString());
          // } catch (Exception e) {
          // LOG.error("ERROR: " + e.getMessage());
          // }

        }

      } catch (Exception e) {
        LOG.error("Error in processing Update Request " + admin.getId().getReqId(), e);
        addError("Update Request " + admin.getId().getReqId() + " Error: " + e.getMessage());
      }

    }
  }

  private long checked2WorkingDays(Date processedDate, Timestamp currentTimestamp) {
    LOG.debug("processedTs=" + processedDate + " currentTimestamp=" + currentTimestamp);
    if (processedDate == null)
      return 0;
    long hoursBetween = 0;
    String curStringDate = currentTimestamp.toString();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
    try {

      Calendar processed = Calendar.getInstance();
      processed.setTime(processedDate);
      Calendar current = Calendar.getInstance();
      current.setTime(sdf.parse(curStringDate));

      LOG.debug("processed.setTime(processedDate) O/P >>> " + processed.getTime());
      LOG.debug("current.setTime(processedDate) O/P >>> " + current.getTime());

      int day = current.get(Calendar.DAY_OF_WEEK);
      if ((day != Calendar.SATURDAY) && (day != Calendar.SUNDAY) && current.after(processed)) {
        if (current.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY && processed.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY) {
          hoursBetween = 0;
        } else {
          hoursBetween = (current.getTimeInMillis() - processed.getTimeInMillis()) / (60 * 60 * 1000);
        }
        LOG.debug("current.get(Calendar.DAY_OF_YEAR) >>> " + current.getTime());
        LOG.debug("processed.get(Calendar.DAY_OF_YEAR) >>> " + processed.getTime());
        LOG.debug("hoursBetween >>> " + hoursBetween);

      }

      LOG.debug("No of workingDays=" + hoursBetween);
    } catch (Exception e) {
      e.printStackTrace();
    }

    return hoursBetween;
  }

  private boolean isTimeStampEquals(Date date) {
    @SuppressWarnings("serial")
    Timestamp ts = new Timestamp(Long.MIN_VALUE) {
      @Override
      public String toString() {
        return "0000-00-00 00:00:00.000";
      }
    };
    String sDate1 = ts.toString().substring(0, 3);
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
    String sDate2 = sdf.format(date).substring(0, 3);

    LOG.info("Check equality of sDate1 :" + sDate1 + " sDate2 :" + sDate2);
    return sDate1.equals(sDate2) ? true : false;
  }

  private Date getZeroDate() {
    String sDate = "0000-00-00 00:00:00.000";
    Date date = null;
    try {
      date = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS").parse(sDate);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return date;
  }

  /**
   * Processes Request Type 'R'
   * 
   * @param entityManager
   * @param request
   * @param admin
   * @param data
   * @throws Exception
   */
  protected void processReactivateDeleteRequest(EntityManager entityManager, Admin admin, Data data) throws Exception {

    // Create the request

    long reqId = admin.getId().getReqId();
    long iterationId = admin.getIterationId();
    String processingStatus = admin.getRdcProcessingStatus();

    MassUpdateServiceInput input = prepareMassChangeInput(entityManager, admin);

    MassProcessRequest request = new MassProcessRequest();
    // set the update mass record in request
    if (input.getInputReqType() != null && (input.getInputReqType().equalsIgnoreCase("D") || input.getInputReqType().equalsIgnoreCase("R"))) {
      request = prepareReactivateDelRequestUS(entityManager, admin, data, input);
    }

    createComment(entityManager, "Processing started.", admin.getId().getReqId());
    // to indicate that batch has picked but pending complete
    admin.setReqStatus("PCO");

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
        this.massServiceClient.setReadTimeout(60 * 20 * 1000); // 20 mins
        response = this.massServiceClient.executeAndWrap(applicationId, request, MassProcessResponse.class);
        response.setReqId(request.getReqId());
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

    String resultCode = response.getStatus();
    if (StringUtils.isBlank(resultCode)) {
      resultCode = CmrConstants.RDC_STATUS_NOT_COMPLETED;
    }

    try {
      // update MASS_UPDT table with the error txt and row status cd
      if (response != null) {
        if (response.getRecords() != null && response.getRecords().size() > 0) {
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
        }
      }

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
      }

      // create comment log and workflow history entries for update type of
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

      createCommentLog(entityManager, admin, comment.toString());

      if (!CmrConstants.RDC_STATUS_IGNORED.equals(resultCode)) {
        RequestUtils.createWorkflowHistoryFromBatch(entityManager, BATCH_USER_ID, admin, comment.toString().trim(), action, null, null,
            "COM".equals(admin.getReqStatus()));
      }
      // RequestUtils.createCommentLogFromBatch(entityManager, BATCH_USER_ID,
      // admin.getId().getReqId(), comment.toString().trim());

      // only update Admin record once depending on the overall status of the
      // request
      LOG.debug("Updating Admin record for Request ID " + admin.getId().getReqId());

      if (CmrConstants.RDC_STATUS_ABORTED.equals(resultCode) || CmrConstants.RDC_STATUS_NOT_COMPLETED.equals(resultCode)) {
        // reject and send back to processor
        admin.setRdcProcessingStatus(CmrConstants.RDC_STATUS_NOT_COMPLETED);
        admin.setReqStatus("PPN");
        admin.setProcessedFlag("E"); // set request status to error.

      } else if (CmrConstants.RDC_STATUS_IGNORED.equals(resultCode)) {
        admin.setRdcProcessingStatus(CmrConstants.RDC_STATUS_COMPLETED_WITH_WARNINGS);
        admin.setReqStatus("COM");
        admin.setProcessedFlag("N"); // set request status to not processed.

      } else if (CmrConstants.RDC_STATUS_COMPLETED.equals(resultCode)) {
        admin.setRdcProcessingStatus(CmrConstants.RDC_STATUS_COMPLETED);
        admin.setReqStatus("COM");
        admin.setProcessedFlag("Y"); // set request status to processed.
      } else {
        admin.setRdcProcessingStatus(resultCode);
      }

      admin.setRdcProcessingTs(SystemUtil.getCurrentTimestamp());
      admin.setRdcProcessingMsg(response.getMsg());
      updateEntity(admin, entityManager);
      LOG.debug("Request ID " + admin.getId().getReqId() + " Status: " + admin.getRdcProcessingStatus() + " Message: " + admin.getRdcProcessingMsg());

      if (CmrConstants.RDC_STATUS_ABORTED.equalsIgnoreCase(admin.getRdcProcessingStatus())
          || CmrConstants.RDC_STATUS_NOT_COMPLETED.equalsIgnoreCase(admin.getRdcProcessingStatus())) {
        admin.setReqStatus("PPN");
        admin.setProcessedFlag("E"); // set request status to error.
        WfHist hist = createHistory(entityManager, "Sending back to processor due to error on RDC processing", "PPN", "RDC Processing",
            admin.getId().getReqId());
      } else if ((CmrConstants.RDC_STATUS_COMPLETED.equalsIgnoreCase(admin.getRdcProcessingStatus())
          || CmrConstants.RDC_STATUS_COMPLETED_WITH_WARNINGS.equalsIgnoreCase(admin.getRdcProcessingStatus()))
          && !"TREC".equalsIgnoreCase(admin.getReqReason())) {
        admin.setReqStatus("COM");
        admin.setProcessedFlag("Y"); // set request status to processed
        WfHist hist = createHistory(entityManager, "Request processing Completed Successfully", "COM", "RDC Processing", admin.getId().getReqId());
      }

      partialCommit(entityManager);
      try {
        String siteIds = "";
        sendEmailNotifications(entityManager, admin, siteIds, comment.toString());
      } catch (Exception e) {
        e.printStackTrace();
        LOG.error("ERROR: " + e.getMessage());
      }

    } catch (Exception e) {
      LOG.error("Error in processing Mass Change Request  " + admin.getId().getReqId(), e);
      addError("Mass Change Request " + admin.getId().getReqId() + " Error: " + e.getMessage());
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
  public MassProcessRequest prepareReactivateDelRequestUS(EntityManager entityManager, Admin admin, Data data, MassUpdateServiceInput input)
      throws JsonGenerationException, JsonMappingException, IOException, Exception {

    MassProcessRequest request = new MassProcessRequest();
    request.setMandt(input.getInputMandt());
    // request.setMandt("100");
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
    massquery.setParameter("STATUS", "READY");
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
   * Checks if the status is a completed status
   * 
   * @param status
   * @return
   */
  @Override
  protected boolean isCompletedSuccessfully(String status) {
    return CmrConstants.RDC_STATUS_COMPLETED.equals(status) || CmrConstants.RDC_STATUS_COMPLETED_WITH_WARNINGS.equals(status);
  }

  /**
   * This is the batch process for mass update requests
   * 
   * @param entityManager
   * @param admin
   */
  public void processMassUpdate(EntityManager entityManager, Admin admin) throws Exception {
    if (admin == null) {
      throw new Exception("Cannot process mass update request. Admin information is null or empty.");
    }
    PreparedQuery query = new PreparedQuery(entityManager, ExternalizedQuery.getSql("QUERY.DATA.GET.CMR.BY_REQID"));
    query.setParameter("REQ_ID", admin.getId().getReqId());
    List<Object[]> cntryList = query.getResults();
    String cntry = "";

    if (cntryList != null && cntryList.size() > 0) {
      Object[] result = cntryList.get(0);
      cntry = (String) result[0];
    } else {
      throw new Exception("Cannot process mass update request. Data information is null or empty.");
    }

    query = new PreparedQuery(entityManager, ExternalizedQuery.getSql("SYSTEM.SUPP_CNTRY_BY_CNTRY_CD"));
    query.setParameter("CNTRY_CD", cntry);
    SuppCntry suppCntry = query.getSingleResult(SuppCntry.class);

    if (suppCntry == null) {
      throw new Exception("Cannot process mass update request. Data information is null or empty.");
    } else {
      String mode = suppCntry.getSuppReqType();

      if (mode.contains("M0")) {
        throw new Exception("Cannot process mass update request. Mass update processing is currently set to manual.");
      }
    }
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

    String resultCode = null;
    String processingStatus = admin.getRdcProcessingStatus() != null ? admin.getRdcProcessingStatus() : "";
    long reqId = admin.getId().getReqId();
    boolean isIndexNotUpdated = false;

    try {
      // 1. Get request to process
      PreparedQuery query = new PreparedQuery(entityManager, ExternalizedQuery.getSql("BATCH.US.GET.MASS_UPDT"));
      query.setParameter("REQ_ID", admin.getId().getReqId());
      query.setParameter("ITER_ID", admin.getIterationId());

      List<MassUpdt> results = query.getResults(MassUpdt.class);
      List<String> statusCodes = new ArrayList<String>();
      StringBuilder comment = null;

      ProcessResponse response = null;
      String applicationId = BatchUtil.getAppId(data.getCmrIssuingCntry());
      List<String> rdcProcessStatusMsgs = new ArrayList<String>();
      HashMap<String, String> overallStatus = new HashMap<String, String>();

      if (results != null && results.size() > 0) {
        for (MassUpdt sMassUpdt : results) {
          comment = new StringBuilder();
          // if
          // (!CmrConstants.REQUEST_STATUS.PCO.toString().equals(admin.getReqStatus()))
          // {
          // admin.setReqStatus(CmrConstants.REQUEST_STATUS.PCO.toString());
          // updateEntity(admin, entityManager);
          // partialCommit(entityManager);
          // }

          request.setCmrNo(sMassUpdt.getCmrNo());
          request.setMandt(SystemConfiguration.getValue("MANDT"));
          request.setIterationId(sMassUpdt.getId().getIterationId());
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
              this.massServiceClient.setReadTimeout(60 * 30 * 1000); // 30 mins
              response = this.massServiceClient.executeAndWrap(applicationId, request, ProcessResponse.class);

              if (response != null && response.getStatus().equals("A")
                  && response.getMessage().contains("was not successfully updated on the index.")) {
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
                LOG.info("Record with the following Kunnr, Address sequence and address types on request ID " + admin.getId().getReqId()
                    + " was SUCCESSFULLY processed:\n");
                for (RDcRecord pRecord : response.getRecords()) {
                  if (comment.length() > 9900) {
                    LOG.info("Kunnr: " + pRecord.getSapNo() + ", sequence number: " + pRecord.getSeqNo() + ", ");
                    LOG.info(" address type: " + pRecord.getAddressType() + "\n");
                  } else {
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
          updateEntity(sMassUpdt, entityManager);
          admin.setReqStatus(CmrConstants.REQUEST_STATUS.COM.toString());
          admin.setProcessedFlag("Y");
          admin.setLockInd("N");
          updateEntity(admin, entityManager);
          partialCommit(entityManager);
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
      LOG.error("Error in processing Update Request " + admin.getId().getReqId(), e);
      addError("Update Request " + admin.getId().getReqId() + " Error: " + e.getMessage());
    }
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

    createHistory(entityManager, "Processing started.", "PCR", "Claim", admin.getId().getReqId());
    createComment(entityManager, "Processing started.", admin.getId().getReqId());

    partialCommit(entityManager);
  }

  /**
   * Locks the admin record
   * 
   * @param entityManager
   * @param admin
   * @throws Exception
   */
  private void lockRecordUpdt(EntityManager entityManager, Admin admin) throws Exception {
    LOG.info("Locking Request " + admin.getId().getReqId());
    admin.setLockBy(BATCH_USER_ID);
    admin.setLockByNm(BATCH_USER_ID);
    admin.setLockInd("Y");
    // error
    admin.setReqStatus("PCR");
    admin.setLastUpdtBy(BATCH_USER_ID);
    updateEntity(admin, entityManager);

    createHistory(entityManager, "Processing started.", "PCR", "Claim", admin.getId().getReqId());
    createComment(entityManager, "Processing started.", admin.getId().getReqId());

    partialCommit(entityManager);
  }

  public CmrServiceInput getReqParam(EntityManager em, long reqId, String reqType, Data data) {
    String cmrNo = "";

    if (!StringUtils.isEmpty(reqType)) {
      // if (CmrConstants.REQ_TYPE_CREATE.equals(reqType)) {
      // cmrNo = "TEMP";
      // } else if (CmrConstants.REQ_TYPE_UPDATE.equals(reqType)) {
      // cmrNo = data.getCmrNo();
      // }
      cmrNo = data.getCmrNo();
    }

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
   * Converts the input to Legal CMR Input
   * 
   * @param request
   * @param entityManager
   * @param reqId
   * @throws Exception
   */
  @Override
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
          throw new Exception("Cannot process Propsect to Legal CMR conversion for Request ID " + reqId + ". Propsect CMR No. is missing.");
        }
        request.setProspectCMRNo(prospectCMR);
        request.setSeqNo(!StringUtils.isEmpty((String) result[2]) ? (String) result[2] : "A");
      }
    }
  }

  private void createCommentLog(EntityManager em, Admin admin, String message) throws CmrException, SQLException {
    LOG.info("Creating Comment Log for Req ID " + admin.getId().getReqId());
    ReqCmtLog reqCmtLog = new ReqCmtLog();
    ReqCmtLogPK reqCmtLogpk = new ReqCmtLogPK();
    reqCmtLogpk.setCmtId(SystemUtil.getNextID(em, SystemConfiguration.getValue("MANDT"), "CMT_ID"));
    reqCmtLog.setId(reqCmtLogpk);
    reqCmtLog.setReqId(admin.getId().getReqId());
    reqCmtLog.setCmt(message != null ? message : "No message provided.");
    // save cmtlockedIn as Y default for current realese
    reqCmtLog.setCmtLockedIn(CmrConstants.CMT_LOCK_IND_YES);
    reqCmtLog.setCreateById(admin.getLastUpdtBy());
    reqCmtLog.setCreateByNm(COMMENT_LOGGER);
    // set createTs as current timestamp and updateTs same as CreateTs
    reqCmtLog.setCreateTs(SystemUtil.getCurrentTimestamp());
    reqCmtLog.setUpdateTs(reqCmtLog.getCreateTs());
    createEntity(reqCmtLog, em);
    partialCommit(em);
  }

  private void sendEmailNotifications(EntityManager em, Admin admin, String siteIds, String emailCmt) throws Exception {
    // send mail and get the input params for create cmr service
    String sqlMail = "BATCH.GET_MAIL_CONTENTS";
    String mapping = Admin.BATCH_SERVICE_MAPPING;
    PreparedQuery queryMail = new PreparedQuery(em, ExternalizedQuery.getSql(sqlMail));
    queryMail.setParameter("REQ_ID", admin.getId().getReqId());
    queryMail.setParameter("REQ_STATUS", admin.getReqStatus());
    queryMail.setParameter("CHANGED_BY_ID", BATCH_USER_ID);
    List<CompoundEntity> rs = queryMail.getCompundResults(1, Admin.class, mapping);
    WfHist wfHist = null;

    for (CompoundEntity entity : rs) {
      wfHist = entity.getEntity(WfHist.class);
    }
    wfHist.setReqStatus(admin.getReqStatus());

    if (em == null || admin == null || wfHist == null) {
      throw new Exception("Some paramaters to sendEmailNotification are null. Please check the call.");
    }

    try {
      IERPRequestUtils.sendEmailNotifications(em, admin, wfHist, siteIds, emailCmt);
    } catch (Exception e) {
      e.printStackTrace();
      throw new Exception(e.getMessage());
    }

  }

  private ProcessResponse sendAddrForProcessing(Addr addr, ProcessRequest request, List<ProcessResponse> responses, boolean isIndexNotUpdated,
      String siteIds, EntityManager em, boolean isTempReactivate) {
    String applicationId = BatchUtil.getAppId("897");
    ProcessResponse response = null;
    request.setSapNo(addr.getSapNo());
    request.setAddrType(addr.getId().getAddrType());
    request.setSeqNo(addr.getId().getAddrSeq());
    // call the service
    if (isTempReactivate) {
      LOG.info("Sending request to US Service for TREC(Temporary Reactivate) [Request ID: " + request.getReqId() + " CMR No: " + request.getCmrNo()
          + " Type: " + request.getReqType() + " SAP No: " + request.getSapNo() + "]");
    } else {
      LOG.info("Sending request to USService [Request ID: " + request.getReqId() + " CMR No: " + request.getCmrNo() + " Type: " + request.getReqType()
          + " SAP No: " + request.getSapNo() + "]");
    }
    LOG.trace("Request JSON:");
    if (LOG.isTraceEnabled()) {
      DebugUtil.printObjectAsJson(LOG, request);
    }

    try {
      this.serviceClient.setReadTimeout(60 * 20 * 1000); // 10 mins
      response = this.serviceClient.executeAndWrap(applicationId, request, ProcessResponse.class);

      if (response != null && response.getStatus().equals("A") && response.getMessage().contains("was not successfully updated on the index.")) {
        isIndexNotUpdated = true;
        response.setStatus("C");
        response.setMessage("");
      }
      responses.add(response);
    } catch (Exception e) {
      e.printStackTrace();
      LOG.error("Error when connecting to the service.", e);
      response = new ProcessResponse();
      response.setStatus(CmrConstants.RDC_STATUS_ABORTED);
      response.setReqId(request.getReqId());
      response.setCmrNo(request.getCmrNo());
      response.setMandt(request.getMandt());
      response.setMessage("Cannot connect to the service at the moment.");
      responses.add(response);
      isIndexNotUpdated = false;
    }

    LOG.trace("Response JSON:");
    if (LOG.isTraceEnabled()) {
      DebugUtil.printObjectAsJson(LOG, response);
    }
    if (isTempReactivate) {
      LOG.info("Response received from Process Service for TREC [Request ID: " + response.getReqId() + " CMR No: " + response.getCmrNo() + " KUNNR: "
          + addr.getSapNo() + " Status: " + response.getStatus() + " Message: " + (response.getMessage() != null ? response.getMessage() : "-")
          + "]");
    } else {
      LOG.info("Response received from USProcessService [Request ID: " + response.getReqId() + " CMR No: " + response.getCmrNo() + " KUNNR: "
          + addr.getSapNo() + " Status: " + response.getStatus() + " Message: " + (response.getMessage() != null ? response.getMessage() : "-")
          + "]");
    }
    if (response != null && response.getRecords() != null && response.getRecords().size() > 0) {
      RDcRecord record = response.getRecords().get(0);
      siteIds = siteIds.length() == 0 ? record.getIerpSitePartyId() : siteIds + ", " + record.getIerpSitePartyId();
      addr.setIerpSitePrtyId(record.getIerpSitePartyId());
      addr.setPairedAddrSeq(record.getSeqNo());
      addr.setSapNo(record.getSapNo());
      updateEntity(addr, em);
      partialCommit(em);
    }
    return response;
  }

  private String generateUSCmr(EntityManager entityManager, Admin admin) {

    String cndCMR = "";
    String cmrType = "COMM";

    boolean isPoaFed = "4".equals(admin.getCustType()) || "6".equals(admin.getCustType());
    boolean isMainCustNm1 = false;

    if (admin.getMainCustNm1() != null) {
      isMainCustNm1 = admin.getMainCustNm1().trim().toUpperCase().startsWith("A");
    }

    if (isPoaFed) {
      cmrType = "POA";
    } else if (isMainCustNm1) {
      cmrType = "MAIN";
    }
    cndCMR = USCMRNumGen.genCMRNum(entityManager, cmrType);

    return cndCMR;
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
  @Override
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
          this.updtByEntClient.setConnectTimeout(60 * 1000); // 1 min to connect
          this.updtByEntClient.setReadTimeout(60 * 30 * 1000); // 30 min to
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
            // this thread has been waiting more than 50 mins, means backend
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

        // create comment log and workflow history entries for update type of
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
        // RequestUtils.createCommentLogFromBatch(entityManager, BATCH_USER_ID,
        // admin.getId().getReqId(), comment.toString().trim());

        // only update Admin record once depending on the overall status of the
        // request

        if (CmrConstants.RDC_STATUS_ABORTED.equals(resultCode) || CmrConstants.RDC_STATUS_NOT_COMPLETED.equals(resultCode)) {
          // reject and send back to processor
          admin.setRdcProcessingStatus(CmrConstants.RDC_STATUS_NOT_COMPLETED);
          admin.setReqStatus("PPN");
          admin.setProcessedFlag("E"); // set request status to error.

        } else if (CmrConstants.RDC_STATUS_IGNORED.equals(resultCode)) {
          admin.setRdcProcessingStatus(CmrConstants.RDC_STATUS_COMPLETED_WITH_WARNINGS);
          admin.setReqStatus("COM");
          admin.setProcessedFlag("N"); // set request status to not processed.

        } else if (CmrConstants.RDC_STATUS_COMPLETED.equals(resultCode)) {
          admin.setRdcProcessingStatus(CmrConstants.RDC_STATUS_COMPLETED);
          admin.setReqStatus("COM");
          admin.setProcessedFlag("Y"); // set request status to processed.
        } else {
          admin.setRdcProcessingStatus(resultCode);
        }

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

        // only update Admin record once depending on the overall status of the
        // request
        LOG.debug("Updating Admin record for Request ID " + admin.getId().getReqId());

        if (CmrConstants.RDC_STATUS_ABORTED.equalsIgnoreCase(admin.getRdcProcessingStatus())
            || CmrConstants.RDC_STATUS_NOT_COMPLETED.equalsIgnoreCase(admin.getRdcProcessingStatus())) {
          admin.setReqStatus("PPN");
          admin.setProcessedFlag("E"); // set request status to error.
          WfHist hist = createHistory(entityManager, "Sending back to processor due to error on RDC processing", "PPN", "RDC Processing",
              admin.getId().getReqId());
        } else if ((CmrConstants.RDC_STATUS_COMPLETED.equalsIgnoreCase(admin.getRdcProcessingStatus())
            || CmrConstants.RDC_STATUS_COMPLETED_WITH_WARNINGS.equalsIgnoreCase(admin.getRdcProcessingStatus()))
            && !"TREC".equalsIgnoreCase(admin.getReqReason())) {
          admin.setReqStatus("COM");
          admin.setProcessedFlag("Y"); // set request status to processed
          WfHist hist = createHistory(entityManager, "Request processing Completed Successfully", "COM", "RDC Processing", admin.getId().getReqId());
        }

        partialCommit(entityManager);

        try {
          String siteIds = "";
          sendEmailNotifications(entityManager, admin, siteIds, comment.toString());
        } catch (Exception e) {
          e.printStackTrace();
          LOG.error("ERROR: " + e.getMessage());
        }

      }
    } catch (Exception e) {
      LOG.error("Error in processing Enterprise Record " + admin.getId().getReqId() + " for Request ID " + admin.getId().getReqId(), e);
      addError("Update By Enterprise Request " + admin.getId().getReqId() + " Error: " + e.getMessage());
    }
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
  @Override
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
   * Updates MASS_UPDT.ROW_STATUS_CD
   * 
   * @param statusCode
   * @param entityManager
   * @param parReqId
   */
  @Override
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

  protected List<Long> gatherSingleRequests(EntityManager entityManager) {
    long start = new Date().getTime();
    List<Admin> list = getPendingRecords(entityManager);
    List<Long> ids = new ArrayList<Long>();
    for (Admin admin : list) {
      ids.add(admin.getId().getReqId());
    }
    ProfilerLogger.LOG.trace("After gatherSingleRequests " + DurationFormatUtils.formatDuration(new Date().getTime() - start, "m 'm' s 's'"));
    return ids;
  }

  protected List<Long> gatherMassUpdateRequests(EntityManager entityManager) {
    long start = new Date().getTime();
    List<Admin> list = getPendingRecordsMassUpd(entityManager);
    List<Long> ids = new ArrayList<Long>();
    for (Admin admin : list) {
      ids.add(admin.getId().getReqId());
    }
    ProfilerLogger.LOG.trace("After gatherMassUpdateRequests " + DurationFormatUtils.formatDuration(new Date().getTime() - start, "m 'm' s 's'"));
    return ids;
  }

}