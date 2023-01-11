/**
 * 
 */
package com.ibm.cmr.create.batch.service;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import javax.persistence.EntityManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.AddrPK;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.CmrtAddr;
import com.ibm.cio.cmr.request.entity.CmrtCust;
import com.ibm.cio.cmr.request.entity.CompoundEntity;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.entity.DataPK;
import com.ibm.cio.cmr.request.entity.MassUpdt;
import com.ibm.cio.cmr.request.entity.MassUpdtAddr;
import com.ibm.cio.cmr.request.entity.MassUpdtData;
import com.ibm.cio.cmr.request.entity.MassUpdtDataPK;
import com.ibm.cio.cmr.request.entity.ReqCmtLog;
import com.ibm.cio.cmr.request.entity.WfHist;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.util.RequestUtils;
import com.ibm.cio.cmr.request.util.SystemLocation;
import com.ibm.cio.cmr.request.util.SystemUtil;
import com.ibm.cio.cmr.request.util.legacy.LegacyDirectObjectContainer;
import com.ibm.cio.cmr.request.util.legacy.LegacyDirectUtil;
import com.ibm.cmr.create.batch.model.MassUpdateServiceInput;
import com.ibm.cmr.create.batch.util.BatchUtil;
import com.ibm.cmr.create.batch.util.CMRRequestContainer;
import com.ibm.cmr.create.batch.util.DebugUtil;
import com.ibm.cmr.create.batch.util.mq.LandedCountryMap;
import com.ibm.cmr.create.batch.util.mq.transformer.MessageTransformer;
import com.ibm.cmr.create.batch.util.mq.transformer.TransformerManager;
import com.ibm.cmr.services.client.CmrServicesFactory;
import com.ibm.cmr.services.client.GenerateCMRNoClient;
import com.ibm.cmr.services.client.cmrno.GenerateCMRNoRequest;
import com.ibm.cmr.services.client.cmrno.GenerateCMRNoResponse;
import com.ibm.cmr.services.client.process.ProcessRequest;
import com.ibm.cmr.services.client.process.ProcessResponse;
import com.ibm.cmr.services.client.process.RDcRecord;
import com.ibm.cmr.services.client.process.mass.MassProcessRequest;
import com.ibm.cmr.services.client.process.mass.MassUpdateRecord;

/**
 * Class to process CMRs moved to the CMRDB2D schema for mass updates.
 * 
 * @author JeffZAMORA
 * 
 */
public class LegacyDirectRdcMassProcessService extends TransConnService {

  private boolean devMode;
  private static final Logger LOG = Logger.getLogger(LegacyDirectRdcMassProcessService.class);

  public static final String LEGACY_STATUS_ACTIVE = "A";
  public static final String LEGACY_STATUS_CANCELLED = "C";
  public static final String CMR_REACTIVATION_REQUEST_REASON = "REAC";
  public static final String CMR_REQUEST_REASON_TEMP_REACT_EMBARGO = "TREC";
  public static final String CMR_REQUEST_STATUS_CPR = "CPR";
  public static final String CMR_REQUEST_STATUS_PCR = "PCR";

  private static final String ADDRESS_USE_MAILING = "1";
  private static final String ADDRESS_USE_BILLING = "2";
  private static final String ADDRESS_USE_INSTALLING = "3";
  private static final String ADDRESS_USE_SHIPPING = "4";
  private static final String ADDRESS_USE_EPL_MAILING = "5";
  private static final String ADDRESS_USE_LIT_MAILING = "6";
  private static final String ADDRESS_USE_COUNTRY_A = "A";
  private static final String ADDRESS_USE_COUNTRY_B = "B";
  private static final String ADDRESS_USE_COUNTRY_C = "C";
  private static final String ADDRESS_USE_COUNTRY_D = "D";
  private static final String ADDRESS_USE_COUNTRY_E = "E";
  private static final String ADDRESS_USE_COUNTRY_F = "F";
  private static final String ADDRESS_USE_COUNTRY_G = "G";
  private static final String ADDRESS_USE_COUNTRY_H = "H";
  private static final String ADDRESS_USE_EXISTS = "Y";
  private static final String ADDRESS_USE_NOT_EXISTS = "N";
  private CMRRequestContainer cmrObjects;
  private static final String MASS_UPDATE_FAIL = "FAIL";
  private static final String MASS_UPDATE_DONE = "DONE";

  @Override
  protected Boolean executeBatch(EntityManager entityManager) throws Exception {

    if (this.devMode) {
      LOG.info("RUNNING IN DEVELOPMENT MODE");
    }
    LOG.info("Initializing Country Map..");
    LandedCountryMap.init(entityManager);

    entityManager.clear();
    initClient();
    List<Admin> pending = getPendingRecordsRDC(entityManager);

    LOG.debug((pending != null ? pending.size() : 0) + " records to process to RDc.");

    Data data = null;
    ProcessRequest request = null;
    for (Admin admin : pending) {
      try {
        Thread.currentThread().setName("REQ-" + admin.getId().getReqId());
        this.cmrObjects = prepareRequest(entityManager, admin);
        data = this.cmrObjects.getData();

        request = new ProcessRequest();
        request.setCmrNo(data.getCmrNo());
        request.setMandt(SystemConfiguration.getValue("MANDT"));
        request.setReqId(admin.getId().getReqId());
        request.setReqType(admin.getReqType());
        request.setUserId(BATCH_USER_ID);

        String histMessage = "";
        processMassUpdateRequest(entityManager, request, admin, data, histMessage);

        if (CmrConstants.RDC_STATUS_ABORTED.equalsIgnoreCase(admin.getRdcProcessingStatus())
            || CmrConstants.RDC_STATUS_NOT_COMPLETED.equalsIgnoreCase(admin.getRdcProcessingStatus())) {
          admin.setReqStatus("PPN");
          admin.setProcessedFlag("E"); // set request status to error.

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

    Thread.currentThread().setName("LDCRDCMassService-" + Thread.currentThread().getId());
    return true;
  }

  /**
   * Gets the Admin records with status = 'PCO' and country has processing type
   * = 'LD'
   * 
   * @param entityManager
   * @return
   */
  private List<Admin> getPendingRecordsRDC(EntityManager entityManager) {
    String sql = ExternalizedQuery.getSql("LEGACYD.GET_MASS_PROCESS_PENDING.RDC");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    return query.getResults(Admin.class);
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

    // partialCommit(entityManager);
  }

  /**
   * Locks the admin record
   * 
   * @param entityManager
   * @param admin
   * @throws Exception
   */
  private void partialLockRecord(EntityManager entityManager, Admin admin) throws Exception {
    LOG.info("Locking Request " + admin.getId().getReqId());
    admin.setLockBy(BATCH_USER_ID);
    admin.setLockByNm(BATCH_USER_ID);
    admin.setLockInd("N");
    // error
    admin.setProcessedFlag("Wx");
    admin.setReqStatus("CPR");
    admin.setLastUpdtBy(BATCH_USER_ID);
    updateEntity(admin, entityManager);

    createHistory(entityManager, "Processing started.", "CPR", "Claim", admin.getId().getReqId());
    createComment(entityManager, "Processing started.", admin.getId().getReqId());

  }

  /**
   * Completes the processing. Final Status will be set to
   * <strong>Completing</strong>
   * 
   * @param entityManager
   * @param admin
   * @throws SQLException
   * @throws CmrException
   */
  private void completeRecord(EntityManager entityManager, Admin admin, String cmrNo, LegacyDirectObjectContainer legacyObjects)
      throws CmrException, SQLException {
    LOG.info("Completing legacy processing for  Request " + admin.getId().getReqId());
    admin.setLockBy(null);
    admin.setLockByNm(null);
    admin.setLockInd("N");
    // completing
    admin.setProcessedFlag("Y");
    admin.setReqStatus("PCO");
    admin.setLastUpdtBy(BATCH_USER_ID);
    updateEntity(admin, entityManager);

    String message = "Records created successfully on the Legacy Database. CMR No. " + cmrNo + " "
        + (CmrConstants.REQ_TYPE_CREATE.equals(admin.getReqType()) ? " assigned." : " updated.");

    // add the sequences generated
    if (CmrConstants.REQ_TYPE_CREATE.equals(admin.getReqType())) {
      StringBuilder seqCmt = new StringBuilder();
      for (CmrtAddr addr : legacyObjects.getAddresses()) {
        int cnt = 0;
        /*
         * int cnt = 0; for (CmrtAddrUse use : legacyObjects.getUses()) { if
         * (use.getId().getAddrNo() == addr.getId().getAddrNo()) {
         * seqCmt.append(cnt > 0 ? ", " : "");
         * seqCmt.append(LegacyDirectUtil.USES.get(use.getId().getAddrUse()));
         * cnt++; } }
         */
        List<String> uses = new ArrayList<String>();
        uses = legacyObjects.getUsesBySequenceNo(addr.getId().getAddrNo());
        for (String use : uses) {
          seqCmt.append(cnt > 0 ? ", " : "");
          seqCmt.append(LegacyDirectUtil.USES.get(use));
          cnt++;
        }
        seqCmt.append(": ").append(addr.getId().getAddrNo()).append("\n");
      }
      if (seqCmt.toString().trim().length() > 0) {
        message += "\nSequences Generated:\n" + seqCmt.toString();
      }
      message = message.trim();
    } else if (CmrConstants.REQ_TYPE_UPDATE.equals(admin.getReqType())) {
      StringBuilder seqCmt = new StringBuilder();
      for (CmrtAddr addr : legacyObjects.getAddresses()) {
        if (addr.isForCreate()) {
          /*
           * int cnt = 0; for (CmrtAddrUse use : legacyObjects.getUses()) { if
           * (use.getId().getAddrNo() == addr.getId().getAddrNo()) {
           * seqCmt.append(cnt > 0 ? ", " : "");
           * seqCmt.append(LegacyDirectUtil.USES.get(use.getId().getAddrUse()));
           * cnt++; } }
           */
          int cnt = 0;
          List<String> uses = new ArrayList<String>();
          uses = legacyObjects.getUsesBySequenceNo(addr.getId().getAddrNo());
          for (String use : uses) {
            seqCmt.append(cnt > 0 ? ", " : "");
            seqCmt.append(LegacyDirectUtil.USES.get(use));
            cnt++;
          }

          seqCmt.append(": ").append(addr.getId().getAddrNo()).append("\n");
        }
      }
      if (seqCmt.toString().trim().length() > 0) {
        message += "\nSequences Generated:\n" + seqCmt.toString();
      }
      message = message.trim();
    }
    WfHist hist = createHistory(entityManager, message, "PCO", "Legacy Processing", admin.getId().getReqId());
    createComment(entityManager, message, admin.getId().getReqId());
    RequestUtils.sendEmailNotifications(entityManager, admin, hist, false, true);
    partialCommit(entityManager);
  }

  private void completeMassUpdateRecord(EntityManager entityManager, Admin admin, List<String> errors) throws CmrException, SQLException {
    LOG.info("Completing LEGACY processing for Mass Request " + admin.getId().getReqId());
    admin.setLockBy(null);
    admin.setLockByNm(null);
    admin.setLockInd("N");

    admin.setProcessedFlag("Y");
    admin.setLastUpdtBy(BATCH_USER_ID);
    updateEntity(admin, entityManager);

    String message = "Records updated successfully on the RDc Database ";
    String errMsg = "Errors happened in RDc mass updates. Please see request summary for details.";
    WfHist hist = null;

    if (errors != null && errors.size() > 0) {
      admin.setReqStatus("PPN");
      admin.setRdcProcessingMsg(CmrConstants.RDC_STATUS_NOT_COMPLETED);
      hist = createHistory(entityManager, message, "PPN", "RDc Processing", admin.getId().getReqId());
      createComment(entityManager, message, admin.getId().getReqId());
    } else {
      admin.setReqStatus("COM");
      admin.setRdcProcessingMsg(CmrConstants.RDC_STATUS_COMPLETED);
      hist = createHistory(entityManager, message, "COM", "RDc Processing", admin.getId().getReqId());
      createComment(entityManager, errMsg, admin.getId().getReqId());
    }

    RequestUtils.sendEmailNotifications(entityManager, admin, hist, false, true);

    partialCommit(entityManager);

  }

  /**
   * Completes the processing with temporary reactivation. First Status will be
   * set to <strong>Completed Pending Reset</strong>
   * 
   * @param entityManager
   * @param admin
   * @throws SQLException
   * @throws CmrException
   */
  private void partialCompleteRecord(EntityManager entityManager, Admin admin, String cmrNo, LegacyDirectObjectContainer legacyObjects)
      throws CmrException, SQLException {
    LOG.info("Completed Pending Reset legacy processing for  Request " + admin.getId().getReqId());
    admin.setLockBy(null);
    admin.setLockByNm(null);
    admin.setLockInd("N");
    admin.setProcessedFlag("Y");
    admin.setReqStatus("CPR");
    admin.setLastUpdtBy(BATCH_USER_ID);
    admin.setProcessedTs(SystemUtil.getCurrentTimestamp());
    updateEntity(admin, entityManager);

    String message = "Records created successfully on the Legacy Database. CMR No. " + cmrNo + " "
        + (CmrConstants.REQ_TYPE_CREATE.equals(admin.getReqType()) ? " assigned." : " updated.");

    // add the sequences generated

    StringBuilder seqCmt = new StringBuilder();
    for (CmrtAddr addr : legacyObjects.getAddresses()) {
      if (addr.isForCreate()) {
        seqCmt.append(": ").append(addr.getId().getAddrNo()).append("\n");
      }
    }
    if (seqCmt.toString().trim().length() > 0) {
      message += "\nSequences Generated:\n" + seqCmt.toString();
    }
    message = message.trim();

    WfHist hist = createHistory(entityManager, message, "CPR", "Legacy Processing", admin.getId().getReqId());
    createComment(entityManager, message, admin.getId().getReqId());

    RequestUtils.sendEmailNotifications(entityManager, admin, hist, false, true);

    partialCommit(entityManager);

  }

  private int checked2WorkingDays(Date processedTs, Timestamp currentTimestamp) {
    LOG.debug("processedTs=" + processedTs + " currentTimestamp=" + currentTimestamp);

    int workingDays = 0;
    String curStringDate = currentTimestamp.toString();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
    try {

      Calendar start = Calendar.getInstance();
      start.setTime(processedTs);

      Calendar end = Calendar.getInstance();
      end.setTime(sdf.parse(curStringDate));

      while (!start.after(end)) {
        int day = start.get(Calendar.DAY_OF_WEEK);
        if ((day != Calendar.SATURDAY) && (day != Calendar.SUNDAY))
          workingDays++;
        start.add(Calendar.DATE, 1);
      }
      LOG.debug("No of workingDays=" + workingDays);
    } catch (Exception e) {
      e.printStackTrace();
    }

    return workingDays;
  }

  private void resetOrdBlockToData(EntityManager entityManager, Data data) {
    data.setOrdBlk("88");
    updateEntity(data, entityManager);
    LOG.debug("Reset Ord Block back to CreateCMR DB :" + data.getOrdBlk());
  }

  private String getEmbargoCdFromDataRdc(EntityManager entityManager, Admin admin) {
    LOG.debug("Searching DATA_RDC in batch for Request " + admin.getId().getReqId());
    String oldEmbargoCd = "";
    String sql = ExternalizedQuery.getSql("GET.EMBARGO_CD_BY_REQID");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", admin.getId().getReqId());

    List<Object[]> results = query.getResults();
    if (results != null && results.size() > 0) {
      Object[] result = results.get(0);
      oldEmbargoCd = result[0] != null ? (String) result[0] : "";
    }

    LOG.debug("Embargo Code of Data_RDC in batch>" + oldEmbargoCd);
    return oldEmbargoCd;
  }

  /**
   * Generates the CMR No for Creates
   * 
   * @param entityManager
   * @param admin
   * @return
   * @throws InvocationTargetException
   * @throws IllegalArgumentException
   * @throws IllegalAccessException
   * @throws InstantiationException
   * @throws SecurityException
   * @throws NoSuchMethodException
   */
  private String generateCMRNo(EntityManager entityManager, CMRRequestContainer cmrObjects, String cmrIssuingCntry) throws Exception {
    GenerateCMRNoRequest request = new GenerateCMRNoRequest();
    request.setLoc1(cmrIssuingCntry);
    request.setLoc2(cmrIssuingCntry);
    request.setMandt(SystemConfiguration.getValue("MANDT"));
    request.setSystem(GenerateCMRNoClient.SYSTEM_SOF);

    if (this.devMode) {
      request.setDirect("DEV");
    }
    // Story 1585377: CMR No. generation for newly created CMRs
    MessageTransformer transformer = TransformerManager.getTransformer(cmrObjects.getData().getCmrIssuingCntry());
    if (transformer != null) {
      transformer.generateCMRNoByLegacy(entityManager, request, cmrObjects);
    }
    GenerateCMRNoClient client = CmrServicesFactory.getInstance().createClient(BATCH_SERVICE_URL, GenerateCMRNoClient.class);

    LOG.debug("Generating CMR No. for Issuing Country " + cmrIssuingCntry);
    GenerateCMRNoResponse response = client.executeAndWrap(request, GenerateCMRNoResponse.class);

    if (response.isSuccess()) {
      return response.getCmrNo();
    } else {
      LOG.error("CMR No cannot be generated. Error: " + response.getMsg());
      throw new Exception("CMR No cannot be generated.");
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

  /**
   * Overloaded version of prepareRequest. This version is specific to Mass
   * Update requests and takes MassUpdt entity parameter
   * 
   * @param entityManager
   * @param massUpdt
   * @param admin
   * @return Returns CMRRequestContainer with Data, Admin and MassUpdtAddr
   *         values
   * @throws Exception
   */
  private CMRRequestContainer prepareRequest(EntityManager entityManager, MassUpdt massUpdt, Admin admin) throws Exception {
    LOG.debug(">>Preparing Request Objects for CMR# > " + massUpdt.getCmrNo());
    CMRRequestContainer container = new CMRRequestContainer();

    MassUpdtDataPK muDataPK = new MassUpdtDataPK();
    muDataPK.setIterationId(massUpdt.getId().getIterationId());
    muDataPK.setParReqId(massUpdt.getId().getParReqId());
    muDataPK.setSeqNo(massUpdt.getId().getSeqNo());
    MassUpdtData muData = entityManager.find(MassUpdtData.class, muDataPK);

    DataPK dataPk = new DataPK();
    dataPk.setReqId(massUpdt.getId().getParReqId());
    Data data = entityManager.find(Data.class, dataPk);

    if (data == null || muData == null) {
      throw new Exception("Cannot locate DATA record");
    }

    String sql = ExternalizedQuery.getSql("LEGACYD.GET.MASS_UPDT_ADDR");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setForReadOnly(true);
    query.setParameter("REQ_ID", massUpdt.getId().getParReqId());
    query.setParameter("ITER_ID", massUpdt.getId().getIterationId());
    query.setParameter("CMR", massUpdt.getCmrNo());
    List<MassUpdtAddr> addresses = query.getResults(MassUpdtAddr.class);

    container.setAdmin(admin);
    container.setData(data);
    container.setMassUpdateData(muData);

    if (addresses != null) {
      for (MassUpdtAddr addr : addresses) {
        container.addMassUpdateAddresses(addr);
      }
    }

    LOG.debug(">>End preparing Request Objects for CMR# > " + massUpdt.getCmrNo());
    return container;
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

    MessageTransformer transformer = TransformerManager.getTransformer(data.getCmrIssuingCntry());

    String sql = ExternalizedQuery.getSql("LEGACYD.GET.ADDR");

    // if there is a transformer, order the addresses correctly
    if (transformer != null && transformer.getAddressOrder() != null) {
      String[] order = transformer.getAddressOrder();
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
    query.setForReadOnly(true);
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

  /**
   * Initializes an empty instance of the the entity object
   * 
   * @return
   * @throws Exception
   */
  @Override
  public <T> T initEmpty(Class<T> entityClass) throws Exception {
    try {
      T object = entityClass.newInstance();
      Field[] fields = entityClass.getDeclaredFields();
      for (Field field : fields) {
        if (String.class.equals(field.getType()) && !Modifier.isAbstract(field.getModifiers()) && !Modifier.isFinal(field.getModifiers())) {
          field.setAccessible(true);
          field.set(object, "");
        }
        if (Date.class.equals(field.getType()) && !Modifier.isAbstract(field.getModifiers()) && !Modifier.isFinal(field.getModifiers())) {
          field.setAccessible(true);
          field.set(object, SystemUtil.getCurrentTimestamp());
        }
      }
      return object;
    } catch (Exception e) {
      throw new Exception("Cannot initialize " + entityClass.getSimpleName() + " object.");
    }
  }

  /**
   * Initializes an empty instance of the the entity object
   * 
   * @return
   * @throws Exception
   */
  @Override
  public void capsAndFillNulls(Object entity, boolean capitalize) throws Exception {
    try {
      Class<?> entityClass = entity.getClass();
      Field[] fields = entityClass.getDeclaredFields();
      for (Field field : fields) {
        if (String.class.equals(field.getType()) && !Modifier.isAbstract(field.getModifiers()) && !Modifier.isFinal(field.getModifiers())) {
          field.setAccessible(true);
          Object val = field.get(entity);
          if (val == null) {
            field.set(entity, "");
          } else if (capitalize) {
            field.set(entity, ((String) val).toUpperCase().trim());
          }
        }
      }
    } catch (Exception e) {
      // noop
      LOG.warn("Warning: caps and null fill failed. Error = " + e.getMessage());
    }
  }

  /**
   * Gets a list of available sequences
   * 
   * @param entityManager
   * @param country
   * @param cmrNo
   * @return
   */
  private Queue<Integer> getAvailableSequences(EntityManager entityManager, String country, String cmrNo) {
    Queue<Integer> queue = new LinkedList<>();
    String sql = ExternalizedQuery.getSql("LEGACYD.SEQNOTABLE");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("COUNTRY", country);
    query.setParameter("CMR_NO", cmrNo);
    List<Integer> sequences = query.getResults(Integer.class);
    if (sequences == null || sequences.isEmpty()) {
      // no available in 2K sequences, just get max
      sql = ExternalizedQuery.getSql("LEGACYD.MAXSEQ");
      query = new PreparedQuery(entityManager, sql);
      query.setParameter("COUNTRY", country);
      query.setParameter("CMR_NO", cmrNo);
      Integer max = query.getSingleResult(Integer.class);
      if (max != null) {
        sequences = Collections.singletonList(max);
      }
    }
    queue.addAll(sequences);
    return queue;
  }

  /**
   * Manually updates the sequence number of the given addr
   * 
   * @param entityManager
   * @param reqId
   * @param addrType
   * @param oldSeq
   * @param newSeq
   */
  private void updateAddrSeq(EntityManager entityManager, long reqId, String addrType, String oldSeq, String newSeq, String kunnr) {
    String updateSeq = ExternalizedQuery.getSql("LEGACYD.UPDATE_ADDR_SEQ");
    PreparedQuery q = new PreparedQuery(entityManager, updateSeq);
    q.setParameter("NEW_SEQ", newSeq);
    q.setParameter("REQ_ID", reqId);
    q.setParameter("TYPE", addrType);
    q.setParameter("OLD_SEQ", oldSeq);
    q.setParameter("SAP_NO", kunnr);
    LOG.debug("Assigning address sequence " + newSeq + " to " + addrType + " address.");
    q.executeSql();

    updateSeq = ExternalizedQuery.getSql("LEGACYD.UPDATE_ADDR_SEQ_RDC");
    q = new PreparedQuery(entityManager, updateSeq);
    q.setParameter("NEW_SEQ", newSeq);
    q.setParameter("REQ_ID", reqId);
    q.setParameter("TYPE", addrType);
    q.setParameter("OLD_SEQ", oldSeq);
    q.setParameter("SAP_NO", kunnr);
    q.executeSql();

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

  @Override
  protected void handleMailsForResult(EntityManager entityManager, String resultCode, Admin admin, String comment) throws CmrException, SQLException {
    if (!CmrConstants.RDC_STATUS_ABORTED.equals(admin.getRdcProcessingStatus())
        && !CmrConstants.RDC_STATUS_NOT_COMPLETED.equals(admin.getRdcProcessingStatus())) {
      // if not aborted and not completed rdc processing status, complete the
      // request
      admin.setReqStatus("COM");
      updateEntity(admin, entityManager);
    } else if (CmrConstants.RDC_STATUS_ABORTED.equals(admin.getRdcProcessingStatus())
        || CmrConstants.RDC_STATUS_NOT_COMPLETED.equals(admin.getRdcProcessingStatus())) {
      admin.setReqStatus("PPN");
      updateEntity(admin, entityManager);
    }
    if (!CmrConstants.RDC_STATUS_IGNORED.equals(resultCode)) {
      RequestUtils.createWorkflowHistoryFromBatch(entityManager, BATCH_USER_ID, admin, comment,
          "C".equals(admin.getReqType()) ? ACTION_RDC_CREATE : ACTION_RDC_UPDATE, null, null, true, false);
    }
  }

  /**
   * Validates existing abbrev name and abrev location
   * 
   * @param entityManager
   * @param abbrName
   * @param abbrLoc
   */
  private boolean validateNameAndLocation(EntityManager entityManager, String country, String abbrName, String abbrLoc) {
    String sql = ExternalizedQuery.getSql("LEGACYD.VALIDATE");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("COUNTRY", country);
    query.setParameter("NAME", abbrName);
    query.setParameter("LOC", abbrLoc);
    query.setForReadOnly(true);
    return query.exists();
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

  public void processMassUpdateRequest(EntityManager entityManager, ProcessRequest request, Admin admin, Data data, String histMessage)
      throws Exception {

    String resultCode = null;
    String processingStatus = admin.getRdcProcessingStatus() != null ? admin.getRdcProcessingStatus() : "";
    long reqId = admin.getId().getReqId();
    boolean isIndexNotUpdated = false;
    ProcessResponse response = null;

    try {
      // 1. Get request to process
      String sqlName = "";
      if (SystemLocation.ITALY.equals(data.getCmrIssuingCntry())) {
        sqlName = "BATCH.LD.GET.MASS_UPDT_RDC_IT";
      } else {
        sqlName = "BATCH.LD.GET.MASS_UPDT_RDC";
      }

      PreparedQuery query = new PreparedQuery(entityManager, ExternalizedQuery.getSql(sqlName));
      query.setParameter("REQ_ID", admin.getId().getReqId());
      query.setParameter("ITER_ID", admin.getIterationId());
      query.setParameter("MANDT", request.getMandt());
      query.setParameter("CNTRY", data.getCmrIssuingCntry());

      List<MassUpdt> results = query.getResults(MassUpdt.class);
      List<String> statusCodes = new ArrayList<String>();
      StringBuilder comment = null;

      String applicationId = BatchUtil.getAppId(data.getCmrIssuingCntry());
      List<String> rdcProcessStatusMsgs = new ArrayList<String>();
      HashMap<String, String> overallStatus = new HashMap<String, String>();

      if (results != null && results.size() > 0) {
        for (MassUpdt sMassUpdt : results) {
          comment = new StringBuilder();
          if (!CmrConstants.REQUEST_STATUS.PCO.toString().equals(admin.getReqStatus())) {
            admin.setReqStatus(CmrConstants.REQUEST_STATUS.PCO.toString());
            updateEntity(admin, entityManager);
            partialCommit(entityManager);
          }
          // CMR-2279: ISR update in massUpdtData for Turkey
          if (SystemLocation.TURKEY.equals(data.getCmrIssuingCntry())) {
            MassUpdtDataPK muDataPK = new MassUpdtDataPK();
            muDataPK.setIterationId(sMassUpdt.getId().getIterationId());
            muDataPK.setParReqId(sMassUpdt.getId().getParReqId());
            muDataPK.setSeqNo(sMassUpdt.getId().getSeqNo());
            MassUpdtData muData = entityManager.find(MassUpdtData.class, muDataPK);

            if (!StringUtils.isBlank(muData.getCustNm1())) {
              String sql = ExternalizedQuery.getSql("LEGACY.GET_ISR_BYSBO");
              PreparedQuery q = new PreparedQuery(entityManager, sql);
              q.setParameter("SBO", muData.getCustNm1());
              q.setParameter("CNTRY", SystemLocation.TURKEY);
              String isr = q.getSingleResult(String.class);
              if (!StringUtils.isBlank(isr)) {
                muData.setRepTeamMemberNo(isr);
                updateEntity(muData, entityManager);
              } else {
                muData.setRepTeamMemberNo("");
                updateEntity(muData, entityManager);
              }
            }
          }

          request.setCmrNo(sMassUpdt.getCmrNo());
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
                this.serviceClient.setReadTimeout(60 * 60 * 1000); // 30 mins
                response = this.serviceClient.executeAndWrap(applicationId, request, ProcessResponse.class);
              }

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
            if (response.getRecords() != null && response.getRecords().size() > 0) {
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
              comment.append("\n\nRDc records were not processed.");
              if (CmrConstants.RDC_STATUS_COMPLETED_WITH_WARNINGS.equals(resultCode)) {
                comment = comment.append("\nWarning Message: " + response.getMessage());
              }
              LOG.debug(comment.toString());
            }

            if (StringUtils.isEmpty(sMassUpdt.getErrorTxt())) {
              // CREATCMR-5259
              // sMassUpdt.setErrorTxt(comment.toString());
              if (comment.toString().length() > 10000) {
                sMassUpdt.setErrorTxt(comment.toString().substring(0, 10000));
              } else {
                sMassUpdt.setErrorTxt(comment.toString());
              }
            } else {
              // CREATCMR-5259
              // sMassUpdt.setErrorTxt(sMassUpdt.getErrorTxt() +
              // comment.toString());
              String errMsg = sMassUpdt.getErrorTxt() + comment.toString();
              if (errMsg.length() > 10000) {
                sMassUpdt.setErrorTxt(errMsg.substring(0, 10000));
              } else {
                sMassUpdt.setErrorTxt(errMsg);
              }
            }

            sMassUpdt.setRowStatusCd(MASS_UPDATE_DONE);
            admin.setReqStatus(CmrConstants.REQUEST_STATUS.COM.toString());
            rdcProcessStatusMsgs.add(CmrConstants.RDC_STATUS_COMPLETED);
          } else {
            if (CmrConstants.RDC_STATUS_ABORTED.equals(resultCode) && CmrConstants.RDC_STATUS_ABORTED.equals(processingStatus)) {
              comment = comment.append("\nRDc mass update processing for REQ ID " + request.getReqId() + " was ABORTED.");
              sMassUpdt.setRowStatusCd(CmrConstants.MASS_CREATE_ROW_STATUS_FAIL);
              sMassUpdt.setErrorTxt(sMassUpdt.getErrorTxt() + comment.toString());
              rdcProcessStatusMsgs.add(resultCode);
            } else if (CmrConstants.RDC_STATUS_ABORTED.equalsIgnoreCase(resultCode)) {
              comment = comment.append("\nRDc mass update processing for REQ ID " + request.getReqId() + " was ABORTED.");
              sMassUpdt.setRowStatusCd(CmrConstants.MASS_CREATE_ROW_STATUS_FAIL);
              sMassUpdt.setErrorTxt(sMassUpdt.getErrorTxt() + comment.toString());
              rdcProcessStatusMsgs.add(resultCode);
            } else if (CmrConstants.RDC_STATUS_NOT_COMPLETED.equalsIgnoreCase(resultCode)) {
              comment = comment.append("\nRDc mass update processing for REQ ID " + request.getReqId() + " is NOT COMPLETED.");
              sMassUpdt.setRowStatusCd(CmrConstants.MASS_CREATE_ROW_STATUS_FAIL);
              rdcProcessStatusMsgs.add(resultCode);
              sMassUpdt.setErrorTxt(sMassUpdt.getErrorTxt() + comment.toString());
            } else if (CmrConstants.RDC_STATUS_IGNORED.equalsIgnoreCase(resultCode)) {
              comment = comment.append("\nRDc mass update processing for REQ ID " + request.getReqId() + " is IGNORED.");
              sMassUpdt.setRowStatusCd(CmrConstants.MASS_CREATE_ROW_STATUS_UPDATE_FAILE);
              sMassUpdt.setErrorTxt(sMassUpdt.getErrorTxt() + comment.toString());
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
          updateEntity(sMassUpdt, entityManager);
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
          admin.setProcessedFlag(CmrConstants.PROCESSING_STATUS.Y.toString());
        } else {
          admin.setReqStatus("PPN");
          admin.setProcessedFlag(CmrConstants.PROCESSING_STATUS.E.toString());
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
        admin.setRdcProcessingStatus(CmrConstants.RDC_STATUS_ABORTED);
        admin.setRdcProcessingMsg(
            "There are no Mass Update records completed on Legacy that can be processed on RDc. " + "This request is being sent back in error.");
        histMessage = "There are no Mass Update records completed on Legacy that can be processed on RDc. "
            + "This request is being sent back in error.";
        RequestUtils.createCommentLogFromBatch(entityManager, BATCH_USER_ID, reqId, histMessage);
      }
    } catch (Exception e) {
      LOG.error("Error in processing Update Request " + admin.getId().getReqId(), e);
      addError("Update Request " + admin.getId().getReqId() + " Error: " + e.getMessage());
    }

  }

  @Override
  protected boolean isTransactional() {
    return true;
  }

  @Override
  protected boolean useServicesConnections() {
    return true;
  }

  public boolean isDevMode() {
    return devMode;
  }

  public void setDevMode(boolean devMode) {
    this.devMode = devMode;
  }

  public MassProcessRequest prepareReactDelReq(EntityManager entityManager, Admin admin, Data data, MassUpdateServiceInput input)
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

      // set updtRec in updtRecordsList
      updtRecordsList.add(record);

    }
    // set updtRecordsList in request
    request.setRecords(updtRecordsList);

    return request;

  }

  // Mukesh:Story 1698123
  private static void modifyAddrUseFields(String seqNo, String addrUse, CmrtAddr legacyAddr) {

    for (String use : addrUse.split("")) {
      if (!StringUtils.isEmpty(use)) {

        if (ADDRESS_USE_MAILING.equals(use)) {
          legacyAddr.setIsAddrUseMailing(ADDRESS_USE_EXISTS);
        } else {
          legacyAddr.setIsAddrUseMailing(ADDRESS_USE_NOT_EXISTS);
        }

        if (ADDRESS_USE_BILLING.equals(use)) {
          legacyAddr.setIsAddrUseBilling(ADDRESS_USE_EXISTS);
        } else {
          legacyAddr.setIsAddrUseBilling(ADDRESS_USE_NOT_EXISTS);
        }

        if (ADDRESS_USE_INSTALLING.equals(use)) {
          legacyAddr.setIsAddrUseInstalling(ADDRESS_USE_EXISTS);
        } else {
          legacyAddr.setIsAddrUseInstalling(ADDRESS_USE_NOT_EXISTS);
        }

        if (ADDRESS_USE_SHIPPING.equals(use)) {
          legacyAddr.setIsAddrUseShipping(ADDRESS_USE_EXISTS);
        } else {
          legacyAddr.setIsAddrUseShipping(ADDRESS_USE_NOT_EXISTS);
        }

        if (ADDRESS_USE_EPL_MAILING.equals(use)) {
          legacyAddr.setIsAddrUseEPL(ADDRESS_USE_EXISTS);
        } else {
          legacyAddr.setIsAddrUseEPL(ADDRESS_USE_NOT_EXISTS);
        }

        if (ADDRESS_USE_LIT_MAILING.equals(use)) {
          legacyAddr.setIsAddrUseLitMailing(ADDRESS_USE_EXISTS);
        } else {
          legacyAddr.setIsAddrUseLitMailing(ADDRESS_USE_NOT_EXISTS);
        }

        if (ADDRESS_USE_COUNTRY_A.equals(use)) {
          legacyAddr.setIsAddressUseA(ADDRESS_USE_EXISTS);
        } else {
          legacyAddr.setIsAddressUseA(ADDRESS_USE_NOT_EXISTS);
        }

        if (ADDRESS_USE_COUNTRY_B.equals(use)) {
          legacyAddr.setIsAddressUseB(ADDRESS_USE_EXISTS);
        } else {
          legacyAddr.setIsAddressUseB(ADDRESS_USE_NOT_EXISTS);
        }

        if (ADDRESS_USE_COUNTRY_C.equals(use)) {
          legacyAddr.setIsAddressUseC(ADDRESS_USE_EXISTS);
        } else {
          legacyAddr.setIsAddressUseC(ADDRESS_USE_NOT_EXISTS);
        }

        if (ADDRESS_USE_COUNTRY_D.equals(use)) {
          legacyAddr.setIsAddressUseD(ADDRESS_USE_EXISTS);
        } else {
          legacyAddr.setIsAddressUseD(ADDRESS_USE_NOT_EXISTS);
        }

        if (ADDRESS_USE_COUNTRY_E.equals(use)) {
          legacyAddr.setIsAddressUseE(ADDRESS_USE_EXISTS);
        } else {
          legacyAddr.setIsAddressUseE(ADDRESS_USE_NOT_EXISTS);
        }

        if (ADDRESS_USE_COUNTRY_F.equals(use)) {
          legacyAddr.setIsAddressUseF(ADDRESS_USE_EXISTS);
        } else {
          legacyAddr.setIsAddressUseF(ADDRESS_USE_NOT_EXISTS);
        }

        if (ADDRESS_USE_COUNTRY_G.equals(use)) {
          legacyAddr.setIsAddressUseG(ADDRESS_USE_EXISTS);
        } else {
          legacyAddr.setIsAddressUseG(ADDRESS_USE_NOT_EXISTS);
        }

        if (ADDRESS_USE_COUNTRY_H.equals(use)) {
          legacyAddr.setIsAddressUseH(ADDRESS_USE_EXISTS);
        } else {
          legacyAddr.setIsAddressUseH(ADDRESS_USE_NOT_EXISTS);
        }

      }
    }
  }

  // Mukesh:Story 1698123
  public static void modifyAddrUseForSequence(String seqNo, String addrUse, LegacyDirectObjectContainer legacyObjects) {
    CmrtAddr address = legacyObjects.findBySeqNo(seqNo);
    if (address != null) {
      String taggedUse = address.getAddressUse();
      LOG.trace("Current Address Use: " + taggedUse + ". Untagging: " + addrUse);
      for (String use : addrUse.split("")) {
        if (!StringUtils.isEmpty(use)) {
          untagAddrUseForSequence(use, address);
          taggedUse = StringUtils.replace(taggedUse, use, "");
          address.setAddressUse(taggedUse);
        }
      }
      LOG.trace("Untagged Address Use: " + address.getAddressUse());
    }
  }

  public static void untagAddrUseForSequence(String addrUse, CmrtAddr legacyAddr) {
    for (String use : addrUse.split("")) {
      if (!StringUtils.isEmpty(use)) {
        switch (use) {
        case ADDRESS_USE_MAILING:
          legacyAddr.setIsAddrUseMailing(ADDRESS_USE_NOT_EXISTS);
          break;
        case ADDRESS_USE_BILLING:
          legacyAddr.setIsAddrUseBilling(ADDRESS_USE_NOT_EXISTS);
          break;
        case ADDRESS_USE_INSTALLING:
          legacyAddr.setIsAddrUseInstalling(ADDRESS_USE_NOT_EXISTS);
          break;
        case ADDRESS_USE_SHIPPING:
          legacyAddr.setIsAddrUseShipping(ADDRESS_USE_NOT_EXISTS);
          break;
        case ADDRESS_USE_EPL_MAILING:
          legacyAddr.setIsAddrUseEPL(ADDRESS_USE_NOT_EXISTS);
          break;
        case ADDRESS_USE_LIT_MAILING:
          legacyAddr.setIsAddrUseLitMailing(ADDRESS_USE_NOT_EXISTS);
          break;
        case ADDRESS_USE_COUNTRY_A:
          legacyAddr.setIsAddressUseA(ADDRESS_USE_NOT_EXISTS);
          break;
        case ADDRESS_USE_COUNTRY_B:
          legacyAddr.setIsAddressUseB(ADDRESS_USE_NOT_EXISTS);
          break;
        case ADDRESS_USE_COUNTRY_C:
          legacyAddr.setIsAddressUseC(ADDRESS_USE_NOT_EXISTS);
          break;
        case ADDRESS_USE_COUNTRY_D:
          legacyAddr.setIsAddressUseD(ADDRESS_USE_NOT_EXISTS);
          break;
        case ADDRESS_USE_COUNTRY_E:
          legacyAddr.setIsAddressUseE(ADDRESS_USE_NOT_EXISTS);
          break;
        case ADDRESS_USE_COUNTRY_F:
          legacyAddr.setIsAddressUseF(ADDRESS_USE_NOT_EXISTS);
          break;
        case ADDRESS_USE_COUNTRY_G:
          legacyAddr.setIsAddressUseG(ADDRESS_USE_NOT_EXISTS);
          break;
        case ADDRESS_USE_COUNTRY_H:
          legacyAddr.setIsAddressUseH(ADDRESS_USE_NOT_EXISTS);
          break;
        }
      }
    }
  }

  private String getLangCdLegacyMapping(EntityManager entityManager, Data data, String cntry) {
    if (entityManager == null) {
      return null;
    }
    String res = "";
    try {
      String sql = ExternalizedQuery.getSql("GET_LEGACY_LANG_CD");
      PreparedQuery query = new PreparedQuery(entityManager, sql);
      query.setParameter("CD", data.getCustPrefLang());
      query.setParameter("CNTRY", cntry);
      query.setForReadOnly(true);
      res = query.getSingleResult(String.class);
    } catch (Exception e) {
      e.getMessage();
      LOG.debug("Some error occured while extracting Land Cd Legacy Mapping from DB.Returning blank");
      return "";
    }
    return res;
  }

  private void skipLegacyProcessingforCreate(EntityManager entityManager, Admin admin) throws CmrException {
    LOG.debug("Started Create processing of Request " + admin.getId().getReqId());
    admin.setLockBy(null);
    admin.setLockByNm(null);
    admin.setLockInd("N");
    // completing
    admin.setProcessedFlag("Y");
    admin.setReqStatus("PCO");// setting the request status to completing so
                              // that request can be again picked for RDC
                              // processing again.
    admin.setLastUpdtBy(BATCH_USER_ID);
    updateEntity(admin, entityManager);
  }

  private void rollbackLegacyData(EntityManager cmmaMgr, String cntry, String cmr) throws Exception {
    LegacyDirectObjectContainer ldoContainer = LegacyDirectUtil.getLegacyDBValues(cmmaMgr, cntry, cmr, true, false);

    if (ldoContainer != null) {
      List<CmrtAddr> lAddrs = ldoContainer.getAddresses();
      CmrtCust lCust = ldoContainer.getCustomer();

      if (lAddrs != null) {
        for (CmrtAddr addr : lAddrs) {
          deleteEntity(addr, cmmaMgr);
        }
      }

      if (lCust != null) {
        deleteEntity(lCust, cmmaMgr);
      }

    }

  }

  @Override
  protected boolean terminateOnLongExecution() {
    return false;
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
}
