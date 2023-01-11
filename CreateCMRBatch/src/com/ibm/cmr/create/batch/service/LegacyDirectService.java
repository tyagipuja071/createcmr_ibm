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
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.log4j.Logger;
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
import com.ibm.cio.cmr.request.entity.CmrtAddr;
import com.ibm.cio.cmr.request.entity.CmrtAddrPK;
import com.ibm.cio.cmr.request.entity.CmrtCust;
import com.ibm.cio.cmr.request.entity.CmrtCustExt;
import com.ibm.cio.cmr.request.entity.CmrtCustExtPK;
import com.ibm.cio.cmr.request.entity.CmrtCustPK;
import com.ibm.cio.cmr.request.entity.CompoundEntity;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.entity.DataPK;
import com.ibm.cio.cmr.request.entity.DataRdc;
import com.ibm.cio.cmr.request.entity.Kna1;
import com.ibm.cio.cmr.request.entity.MassUpdt;
import com.ibm.cio.cmr.request.entity.MassUpdtAddr;
import com.ibm.cio.cmr.request.entity.MassUpdtData;
import com.ibm.cio.cmr.request.entity.MassUpdtDataPK;
import com.ibm.cio.cmr.request.entity.MqIntfReqQueue;
import com.ibm.cio.cmr.request.entity.ReqCmtLog;
import com.ibm.cio.cmr.request.entity.ReservedCMRNos;
import com.ibm.cio.cmr.request.entity.ReservedCMRNosPK;
import com.ibm.cio.cmr.request.entity.SuppCntry;
import com.ibm.cio.cmr.request.entity.WfHist;
import com.ibm.cio.cmr.request.model.BatchEmailModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.util.RequestUtils;
import com.ibm.cio.cmr.request.util.SlackAlertsUtil;
import com.ibm.cio.cmr.request.util.SystemLocation;
import com.ibm.cio.cmr.request.util.SystemUtil;
import com.ibm.cio.cmr.request.util.legacy.LegacyCommonUtil;
import com.ibm.cio.cmr.request.util.legacy.LegacyDirectObjectContainer;
import com.ibm.cio.cmr.request.util.legacy.LegacyDirectUtil;
import com.ibm.cmr.create.batch.model.MassUpdateServiceInput;
import com.ibm.cmr.create.batch.util.BatchUtil;
import com.ibm.cmr.create.batch.util.CMRRequestContainer;
import com.ibm.cmr.create.batch.util.DebugUtil;
import com.ibm.cmr.create.batch.util.ProfilerLogger;
import com.ibm.cmr.create.batch.util.mq.LandedCountryMap;
import com.ibm.cmr.create.batch.util.mq.MQMsgConstants;
import com.ibm.cmr.create.batch.util.mq.handler.impl.SOFMessageHandler;
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
 * Class to process CMRs moved to the CMRDB2D schema.
 * 
 * @author JeffZAMORA O
 */
public class LegacyDirectService extends TransConnService {

  private boolean devMode;
  protected boolean multiMode;
  private static final Logger LOG = Logger.getLogger(LegacyDirectService.class);

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
  // private CMRRequestContainer cmrObjects;
  private static final String MASS_UPDATE_FAIL = "FAIL";
  private static final String MASS_UPDATE_DONE = "DONE";
  private static final String MASS_UDPATE_LEGACY_FAIL_MSG = "Errors happened in legacy mass updates. Pleaes see request summary for details.";
  private static final List<String> EMBARGO_LIST = Arrays.asList("D", "E", "Y");

  private static final List<String> CEE_COUNTRY_LIST = Arrays.asList(SystemLocation.SLOVAKIA, SystemLocation.KYRGYZSTAN, SystemLocation.SERBIA,
      SystemLocation.ARMENIA, SystemLocation.AZERBAIJAN, SystemLocation.TURKMENISTAN, SystemLocation.TAJIKISTAN, SystemLocation.ALBANIA,
      SystemLocation.BELARUS, SystemLocation.BULGARIA, SystemLocation.GEORGIA, SystemLocation.KAZAKHSTAN, SystemLocation.BOSNIA_AND_HERZEGOVINA,
      SystemLocation.MACEDONIA, SystemLocation.SLOVENIA, SystemLocation.HUNGARY, SystemLocation.UZBEKISTAN, SystemLocation.MOLDOVA,
      SystemLocation.POLAND, SystemLocation.RUSSIAN_FEDERATION, SystemLocation.ROMANIA, SystemLocation.UKRAINE, SystemLocation.CROATIA);

  @Override
  protected Boolean executeBatch(EntityManager entityManager) throws Exception {

    if (this.devMode) {
      LOG.info("RUNNING IN DEVELOPMENT MODE");
    } else {
      LOG.info("RUNNING IN NON-DEVELOPMENT MODE");
    }

    LOG.info("Multi Mode: " + this.multiMode);

    LOG.info("Initializing Country Map..");
    long start = new Date().getTime();
    LandedCountryMap.init(entityManager);
    ProfilerLogger.LOG.trace("After Landed country init " + DurationFormatUtils.formatDuration(new Date().getTime() - start, "m 'm' s 's'"));
    // Retrieve the PCP records and create in the Legacy DB
    LOG.info("Retreiving pending records for processing..");
    List<Long> pending = gatherPendingRecords(entityManager);

    processPendingLegacy(entityManager, pending);
    // fresh cache for send to rdc
    entityManager.clear();

    initClient();

    // now send to RDc Retrieve the PCP records and create in the Legacy DB
    pending = gatherPendingRecordsRDC(entityManager);

    processPendingRDC(entityManager, pending);

    return true;
  }

  protected void processPendingLegacy(EntityManager entityManager, List<Long> pending) throws CmrException, SQLException {
    LOG.debug((pending != null ? pending.size() : 0) + " records to process.");
    // pending = new ArrayList<Admin>();
    for (Long id : pending) {
      Thread.currentThread().setName("REQ-" + id);
      long start = new Date().getTime();
      AdminPK pk = new AdminPK();
      pk.setReqId(id);
      Admin admin = entityManager.find(Admin.class, pk);
      if (BatchUtil.excludeForEnvironment(this.context, entityManager, admin)) {
        // exclude data created from a diff env
        continue;
      }
      try {
        switch (admin.getReqType()) {
        case CmrConstants.REQ_TYPE_CREATE:
          processCreate(entityManager, admin);
          break;
        case CmrConstants.REQ_TYPE_UPDATE:
          if (LegacyDirectUtil.isItalyLegacyDirect(entityManager, admin.getId().getReqId())) {
            boolean isUpdated = LegacyDirectUtil.updateITCmrtAddrSeqByReqId(entityManager, admin);
            if (isUpdated) {
              partialCommit(entityManager);
              entityManager.clear();
            }
          }
          processUpdate(entityManager, admin);
          break;
        case CmrConstants.REQ_TYPE_DELETE:
          processDelete(entityManager, admin);
          break;
        case CmrConstants.REQ_TYPE_REACTIVATE:
          processReactivate(entityManager, admin);
          break;
        case CmrConstants.REQ_TYPE_SINGLE_REACTIVATE:
          processSingleReactivate(entityManager, admin);
          break;
        // case CmrConstants.REQ_TYPE_MASS_UPDATE:
        // processMassUpdate(entityManager, admin);
        // break;
        }

      } catch (Exception e) {
        SlackAlertsUtil.recordException("LegacyDirectService", "Unexpected Error", e);
        if (!MASS_UDPATE_LEGACY_FAIL_MSG.equalsIgnoreCase(e.getMessage())) {
          partialRollback(entityManager);
        }
        LOG.error("Unexpected error occurred during processing of Request " + admin.getId().getReqId(), e);
        processError(entityManager, admin, e.getMessage());
      }
      partialCommit(entityManager);
      ProfilerLogger.LOG.trace(
          "After processPendingLegacy for Request ID: " + id + " " + DurationFormatUtils.formatDuration(new Date().getTime() - start, "m 'm' s 's'"));
    }
    Thread.currentThread().setName("LDService-" + Thread.currentThread().getId());

  }

  protected void processPendingRDC(EntityManager entityManager, List<Long> pending) throws CmrException, SQLException {
    LOG.debug((pending != null ? pending.size() : 0) + " records to process to RDc.");

    Data data = null;
    ProcessRequest request = null;
    for (Long id : pending) {
      Thread.currentThread().setName("REQ-" + id);
      long start = new Date().getTime();
      AdminPK pk = new AdminPK();
      pk.setReqId(id);
      Admin admin = entityManager.find(Admin.class, pk);
      if (BatchUtil.excludeForEnvironment(this.context, entityManager, admin)) {
        // exclude data created from a diff env
        continue;
      }
      try {
        CMRRequestContainer cmrObjects = prepareRequest(entityManager, admin, true);
        data = cmrObjects.getData();

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
          processMassReactDelChanges(entityManager, admin, data);
          break;
        case CmrConstants.REQ_TYPE_DELETE:
          processMassReactDelChanges(entityManager, admin, data);
          break;
        case CmrConstants.REQ_TYPE_SINGLE_REACTIVATE:
          processSingleReactivateRequest(entityManager, request, admin, data);
          break;
        // case CmrConstants.REQ_TYPE_MASS_UPDATE:
        // processMassUpdateRequest(entityManager, request, admin, data);
        // break;
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
        ProfilerLogger.LOG.trace(
            "After processPendingRDC for Request ID: " + id + " " + DurationFormatUtils.formatDuration(new Date().getTime() - start, "m 'm' s 's'"));
      } catch (Exception e) {
        SlackAlertsUtil.recordException("LegacyDirectService", "Request " + admin.getId().getReqId(), e);
        partialRollback(entityManager);
        LOG.error("Unexpected error occurred during processing of Request " + admin.getId().getReqId(), e);
        processError(entityManager, admin, e.getMessage());
      }
    }
    Thread.currentThread().setName("LDService-" + Thread.currentThread().getId());
  }

  /**
   * Gets the Admin records with status = 'PCP' and country has processing type
   * = 'LD'
   * 
   * @param entityManager
   * @return
   */
  private List<Admin> getPendingRecords(EntityManager entityManager) {
    String sql = ExternalizedQuery.getSql("LEGACYD.GET_PENDING");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    return query.getResults(Admin.class);
  }

  protected List<Long> gatherPendingRecords(EntityManager entityManager) {
    long start = new Date().getTime();
    String sql = ExternalizedQuery.getSql("LEGACYD.GET_PENDING");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    List<Admin> pendingRecords = query.getResults(Admin.class);
    LOG.debug("Size of Pending Records : " + pendingRecords.size());
    List<Long> queue = new ArrayList<>();
    for (Admin admin : pendingRecords) {
      queue.add(admin.getId().getReqId());
    }
    ProfilerLogger.LOG.trace("After gatherPendingRecords " + DurationFormatUtils.formatDuration(new Date().getTime() - start, "m 'm' s 's'"));
    return queue;
  }

  /**
   * Gets the Admin records with status = 'PCO' and country has processing type
   * = 'LD'
   * 
   * @param entityManager
   * @return
   */
  private List<Admin> getPendingRecordsRDC(EntityManager entityManager) {
    LOG.info("Retrieving pending RDc records for processing..");
    String sql = ExternalizedQuery.getSql("LEGACYD.GET_PENDING.RDC");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    return query.getResults(Admin.class);
  }

  protected List<Long> gatherPendingRecordsRDC(EntityManager entityManager) {
    long start = new Date().getTime();
    String sql = ExternalizedQuery.getSql("LEGACYD.GET_PENDING.RDC");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    List<Admin> pendingRecords = query.getResults(Admin.class);
    LOG.debug("Size of Pending Records to RDC : " + pendingRecords.size());
    List<Long> queue = new ArrayList<>();
    for (Admin admin : pendingRecords) {
      queue.add(admin.getId().getReqId());
    }
    ProfilerLogger.LOG.trace("After gatherPendingRecordsRDC " + DurationFormatUtils.formatDuration(new Date().getTime() - start, "m 'm' s 's'"));
    return queue;
  }

  /**
   * From the {@link Admin} record, gathers the {@link Data}, {@link Addr} and
   * other relevant records and writes directly to the legacy cmr db
   * 
   * @param entityManager
   * @param admin
   * @throws Exception
   */
  private void processCreate(EntityManager entityManager, Admin admin) throws Exception {
    LOG.debug("Started Create processing of Request " + admin.getId().getReqId());

    // lock
    lockRecord(entityManager, admin);

    // get CreateCMR data
    CMRRequestContainer cmrObjects = prepareRequest(entityManager, admin);

    boolean rdcRecExists = checkIfRecExistOnRDC(entityManager, cmrObjects.getData().getCmrNo(), cmrObjects.getData().getCmrIssuingCntry()).size() > 0;
    boolean legacyRecExists = checkIfRecExistOnLegacy(entityManager, cmrObjects.getData().getCmrNo(), cmrObjects.getData().getCmrIssuingCntry())
        .size() > 0;

    if (admin.getRdcProcessingStatus() != null
        && (CmrConstants.RDC_STATUS_ABORTED.equals(admin.getRdcProcessingStatus())
            || CmrConstants.RDC_STATUS_NOT_COMPLETED.equals(admin.getRdcProcessingStatus()))
        && !StringUtils.isEmpty(cmrObjects.getData().getCmrNo())) {

      skipLegacyProcessingforCreateUpdate(entityManager, admin);

    } else if (SystemLocation.ITALY.equals(cmrObjects.getData().getCmrIssuingCntry())
        && CmrConstants.REQ_TYPE_CREATE.equals(cmrObjects.getAdmin().getReqType()) && !rdcRecExists && legacyRecExists) {

      LOG.info("Legacy record already exists for the current request. Skipping Legacy Processing...");
      skipLegacyProcessingforCreateUpdate(entityManager, admin);
    } else {
      // prepare legacy data, map to the values needed
      LegacyDirectObjectContainer legacyObjects = mapRequestDataForCreate(entityManager, cmrObjects);
      MessageTransformer transformer = TransformerManager.getTransformer(cmrObjects.getData().getCmrIssuingCntry());

      // finally persist all data
      CmrtCust legacyCust = legacyObjects.getCustomer();
      if (legacyCust == null) {
        throw new Exception("Customer record cannot be created.");
      }
      LOG.info("Creating Legacy Records for Request ID " + admin.getId().getReqId());
      LOG.info(" - SOF Country: " + legacyCust.getId().getSofCntryCode() + " CMR No.: " + legacyCust.getId().getCustomerNo());
      createEntity(legacyCust, entityManager);
      if (legacyObjects.getCustomerExt() != null) {
        createEntity(legacyObjects.getCustomerExt(), entityManager);
      }
      for (CmrtAddr legacyAddr : legacyObjects.getAddresses()) {
        if (legacyAddr.isForCreate()) {
          createEntity(legacyAddr, entityManager);
        }
      }
      /*
       * for (CmrtAddrUse legacyAddrUse : legacyObjects.getUses()) {
       * createEntity(legacyAddrUse, entityManager); }
       */

      // update cmrNo on DATA
      cmrObjects.getData().setCmrNo(legacyObjects.getCustomerNo());
      if (cmrObjects.getAdmin().getProspLegalInd() != null && cmrObjects.getAdmin().getProspLegalInd().equalsIgnoreCase("Y")
          && cmrObjects.getData().getOrdBlk() != null && CmrConstants.PROSPECT_ORDER_BLOCK.equals(cmrObjects.getData().getOrdBlk())) {
        // MessageTransformer transformer =
        // TransformerManager.getTransformer(cmrObjects.getData().getCmrIssuingCntry());
        if (transformer != null) {
          cmrObjects.getData().setProspectSeqNo(transformer.getFixedAddrSeqForProspectCreation());
        }
        if (StringUtils.isBlank(cmrObjects.getData().getProspectSeqNo())) {
          cmrObjects.getData().setProspectSeqNo("1");
        }
      }

      updateEntity(cmrObjects.getData(), entityManager);

      // completeRecord(entityManager, admin, legacyObjects.getCustomerNo(),
      // legacyObjects);
      if (SystemLocation.ITALY.equals(legacyCust.getId().getSofCntryCode()) && legacyObjects.getCustomerExt() != null) {
        updateCompanyBillingChildRecordsItaly(entityManager, legacyObjects, cmrObjects);
      }
      // Add to build duplicate CMR data for Russia -CMR4606
      Data data = cmrObjects.getData();
      if ("Y".equals(data.getCisServiceCustIndc()) && data.getDupIssuingCntryCd() != null) {
        CEEProcessService theService = new CEEProcessService();
        theService.processDupCreate(entityManager, admin, cmrObjects);
      }

      // Add to build duplicate CMR data for ME countries -CMR6019
      if ("Y".equals(cmrObjects.getData().getDupCmrIndc())) {
        DupCMRProcessService theService = new DupCMRProcessService();
        theService.processDupCreate(entityManager, admin, cmrObjects);
      }

      if (!"NA".equals(transformer.getGmllcDupCreation(data))) {
        LegacyDirectDuplicateProcessService dupService = new LegacyDirectDuplicateProcessService();
        dupService.processDupCreate(entityManager, admin, cmrObjects);
      }

      completeRecord(entityManager, admin, legacyObjects.getCustomerNo(), legacyObjects, cmrObjects);

      if (transformer.isPG01Supported()) {
        modifyPG01Sequences(cmrObjects, entityManager);
      }
    }
  }

  /**
   * From the {@link Admin} record, gathers the {@link Data}, {@link Addr} and
   * other relevant records and updates directly to the legacy cmr db
   * 
   * @param entityManager
   * @param admin
   * @throws Exception
   */
  private void processUpdate(EntityManager entityManager, Admin admin) throws Exception {
    LOG.debug("Started Update processing of Request " + admin.getId().getReqId());

    if (admin.getRdcProcessingStatus() != null && (CmrConstants.RDC_STATUS_ABORTED.equals(admin.getRdcProcessingStatus())
        || CmrConstants.RDC_STATUS_NOT_COMPLETED.equals(admin.getRdcProcessingStatus()))) {

      skipLegacyProcessingforCreateUpdate(entityManager, admin);

    } else {
      // Story 1597678: Support temporary reactivation requests due to Embargo
      // Code handling
      String rdcEmbargoCd = LegacyDirectUtil.getEmbargoCdFromDataRdc(entityManager, admin);
      CMRRequestContainer tcmrObjects = prepareRequest(entityManager, admin);
      String dataEmbargoCd = tcmrObjects.getData().getEmbargoCd();
      if ((admin.getReqReason() != null && !StringUtils.isBlank(admin.getReqReason()))
          && CMR_REQUEST_REASON_TEMP_REACT_EMBARGO.equals(admin.getReqReason()) && (rdcEmbargoCd != null && !StringUtils.isBlank(rdcEmbargoCd))
          && EMBARGO_LIST.contains(rdcEmbargoCd) && (dataEmbargoCd == null || StringUtils.isBlank(dataEmbargoCd))) {
        if (admin.getProcessedTs() == null || isTimeStampEquals(admin.getProcessedTs())) {
          // lock
          partialLockRecord(entityManager, admin);

          // get CreateCMR data
          CMRRequestContainer cmrObjects = prepareRequest(entityManager, admin);

          // prepare legacy data, map to the values needed
          LegacyDirectObjectContainer legacyObjects = mapRequestDataForUpdate(entityManager, cmrObjects);

          // finally update all data
          CmrtCust legacyCust = legacyObjects.getCustomer();
          if (legacyCust == null) {
            throw new Exception("Customer record cannot be updated.");
          }
          LOG.info("Updating Legacy Records for Request ID " + admin.getId().getReqId());
          LOG.info(" - SOF Country: " + legacyCust.getId().getSofCntryCode() + " CMR No.: " + legacyCust.getId().getCustomerNo());
          updateEntity(legacyCust, entityManager);

          if (legacyObjects.getCustomerExt() != null) {
            updateEntity(legacyObjects.getCustomerExt(), entityManager);
          }
          for (CmrtAddr legacyAddr : legacyObjects.getAddresses()) {
            if (legacyAddr.isForUpdate()) {
              legacyAddr.setUpdateTs(SystemUtil.getCurrentTimestamp());
              updateEntity(legacyAddr, entityManager);
            } else if (legacyAddr.isForCreate()) {
              createEntity(legacyAddr, entityManager);
            }
            keepAlive();
          }

          // CMR-2279:there should be some Data updated, so update Data
          if (SystemLocation.TURKEY.equals(legacyCust.getId().getSofCntryCode())) {
            updateEntity(cmrObjects.getData(), entityManager);
          }

          partialCompleteRecord(entityManager, admin, legacyObjects.getCustomerNo(), legacyObjects);

          if (SystemLocation.ITALY.equals(legacyCust.getId().getSofCntryCode())) {
            updateBillingSequenceItaly(entityManager, legacyObjects);
          }

          // Add to update duplicate CMR data for Russia CMR-4606
          Data data = cmrObjects.getData();
          if ("Y".equals(data.getCisServiceCustIndc()) && data.getDupIssuingCntryCd() != null) {
            CEEProcessService theService = new CEEProcessService();
            theService.processDupUpdate(entityManager, admin, data, cmrObjects);
          }

          // Add to build duplicate CMR data for ME countries -CMR6019
          if ("Y".equals(cmrObjects.getData().getDupCmrIndc())) {
            DupCMRProcessService theService = new DupCMRProcessService();
            theService.processDupUpdate(entityManager, admin, cmrObjects);
          }

        } else {
          int noOFWorkingDays = 0;
          if (admin.getReqStatus() != null && admin.getReqStatus().equals(CMR_REQUEST_STATUS_CPR)) {
            noOFWorkingDays = checked2WorkingDays(admin.getProcessedTs(), SystemUtil.getCurrentTimestamp());
          }

          if (noOFWorkingDays >= 3) {
            // lock
            lockRecord(entityManager, admin);
            // admin.setProcessedTs(SystemUtil.getCurrentTimestamp());
            // get CreateCMR data
            CMRRequestContainer cmrObjects = prepareRequest(entityManager, admin);

            // prepare legacy data, map to the values needed
            LegacyDirectObjectContainer legacyObjects = mapRequestDataForUpdate(entityManager, cmrObjects);

            // finally update all data
            CmrtCust legacyCust = legacyObjects.getCustomer();
            if (legacyCust == null) {
              throw new Exception("Customer record cannot be updated.");
            }
            LOG.info("Updating Legacy Records for Request ID " + admin.getId().getReqId());
            LOG.info(" - SOF Country: " + legacyCust.getId().getSofCntryCode() + " CMR No.: " + legacyCust.getId().getCustomerNo());
            updateEntity(legacyCust, entityManager);

            // CMR-2279:there should be some Data updated, so update Data
            if (SystemLocation.TURKEY.equals(legacyCust.getId().getSofCntryCode())) {
              updateEntity(cmrObjects.getData(), entityManager);
            }

            completeTRECRecord(entityManager, admin, legacyObjects.getCustomerNo(), legacyObjects);

            // Add to update duplicate CMR data for Russia CMR-4606
            Data data = cmrObjects.getData();
            if ("Y".equals(data.getCisServiceCustIndc()) && data.getDupIssuingCntryCd() != null) {
              CEEProcessService theService = new CEEProcessService();
              theService.processDupUpdate(entityManager, admin, data, cmrObjects);
            }

            // Add to build duplicate CMR data for ME countries -CMR6019
            if ("Y".equals(cmrObjects.getData().getDupCmrIndc())) {
              DupCMRProcessService theService = new DupCMRProcessService();
              theService.processDupUpdate(entityManager, admin, cmrObjects);
            }
          }
        }
      } else {
        // lock
        lockRecord(entityManager, admin);

        // get CreateCMR data
        CMRRequestContainer cmrObjects = prepareRequest(entityManager, admin);

        // prepare legacy data, map to the values needed
        LegacyDirectObjectContainer legacyObjects = mapRequestDataForUpdate(entityManager, cmrObjects);

        // finally update all data
        CmrtCust legacyCust = legacyObjects.getCustomer();
        if (legacyCust == null) {
          throw new Exception("Customer record cannot be updated.");
        }
        LOG.info("Updating Legacy Records for Request ID " + admin.getId().getReqId());
        LOG.info(" - SOF Country: " + legacyCust.getId().getSofCntryCode() + " CMR No.: " + legacyCust.getId().getCustomerNo());
        updateEntity(legacyCust, entityManager);

        if (legacyObjects.getCustomerExt() != null) {
          updateEntity(legacyObjects.getCustomerExt(), entityManager);
        }
        for (CmrtAddr legacyAddr : legacyObjects.getAddresses()) {
          if (legacyAddr.isForUpdate()) {
            legacyAddr.setUpdateTs(SystemUtil.getCurrentTimestamp());
            updateEntity(legacyAddr, entityManager);
          } else if (legacyAddr.isForCreate()) {
            createEntity(legacyAddr, entityManager);
          }
          keepAlive();
        }

        // CMR-2279:there should be some Data updated, so update Data
        if (SystemLocation.TURKEY.equals(legacyCust.getId().getSofCntryCode())) {
          updateEntity(cmrObjects.getData(), entityManager);
        }

        // for updates, the legacy address use records are rebuilt
        /*
         * for (CmrtAddrUse legacyAddrUse : legacyObjects.getUses()) {
         * createEntity(legacyAddrUse, entityManager); }
         */
        completeRecord(entityManager, admin, legacyObjects.getCustomerNo(), legacyObjects, cmrObjects);

        MessageTransformer transformer = TransformerManager.getTransformer(cmrObjects.getData().getCmrIssuingCntry());
        if (transformer.isPG01Supported()) {
          modifyPG01Sequences(cmrObjects, entityManager);
        }

        // CMR-1025 Legacy processing failed when updated Billing seq 0000B
        if (SystemLocation.ITALY.equals(legacyCust.getId().getSofCntryCode())) {
          updateBillingSequenceItaly(entityManager, legacyObjects);

          if (legacyObjects.getCustomerExt() != null) {
            updateCompanyBillingChildRecordsItaly(entityManager, legacyObjects, cmrObjects);
          }
        }
        // Add to update duplicate CMR data for Russia CMR-4606
        Data data = cmrObjects.getData();
        if ("Y".equals(data.getCisServiceCustIndc()) && data.getDupIssuingCntryCd() != null) {
          CEEProcessService theService = new CEEProcessService();
          theService.processDupUpdate(entityManager, admin, data, cmrObjects);
        }

        // Add to build duplicate CMR data for ME countries -CMR6019
        if ("Y".equals(cmrObjects.getData().getDupCmrIndc())) {
          DupCMRProcessService theService = new DupCMRProcessService();
          theService.processDupUpdate(entityManager, admin, cmrObjects);
        }
      }
    }
  }

  private void updateBillingSequenceItaly(EntityManager entityManager, LegacyDirectObjectContainer legacyObjects) {
    CmrtCust legacyCust = legacyObjects.getCustomer();
    for (CmrtAddr legacyAddr : legacyObjects.getAddresses()) {
      if (legacyAddr.getIsAddrUseBilling() != null && legacyAddr.getIsAddrUseBilling().equalsIgnoreCase("Y")) {

        String rdcBillingSeq = LegacyDirectUtil.getBillingSeqOfRDC(entityManager, legacyCust.getId().getCustomerNo(),
            legacyCust.getId().getSofCntryCode(), MQMsgConstants.ADDR_ZP01);

        LOG.info(" Updating Billing seq. according to RDC seq.:" + rdcBillingSeq + "  of CMR No.: " + legacyCust.getId().getCustomerNo());
        if (!StringUtils.isBlank(rdcBillingSeq) && (rdcBillingSeq.equals("2") || rdcBillingSeq.equals("00002"))) {
          LegacyDirectUtil.updateItalyBillingAddrSeq(entityManager, legacyCust.getId().getCustomerNo(), legacyCust.getId().getSofCntryCode(),
              "00002");
          partialCommit(entityManager);
          entityManager.clear();
        } else if (!StringUtils.isBlank(rdcBillingSeq) && rdcBillingSeq.equals("0000B")) {
          LegacyDirectUtil.updateItalyBillingAddrSeq(entityManager, legacyCust.getId().getCustomerNo(), legacyCust.getId().getSofCntryCode(),
              rdcBillingSeq);
          partialCommit(entityManager);
          entityManager.clear();
        }
      }
    }
  }

  private void updateCompanyBillingChildRecordsItaly(EntityManager entityManager, LegacyDirectObjectContainer legacyObjects,
      CMRRequestContainer cmrObjects) {
    Data data = cmrObjects.getData();
    String cmrNo = data.getCmrNo();
    String cntry = data.getCmrIssuingCntry();
    List<Addr> addrs = cmrObjects.getAddresses();
    String abbrevName = "";
    String streetAbbrev = "";
    String abbrevLoc = "";

    LOG.debug(
        "Update child records for request id: " + cmrObjects.getAdmin().getId().getReqId() + " and req type:" + cmrObjects.getAdmin().getReqType());

    if ("C".equals(cmrObjects.getAdmin().getReqType())) {
      // List<Addr> addrs = cmrObjects.getAddresses();
      String billingCmr = "";
      for (Addr addr : addrs) {
        if ("ZP01".equals(addr.getId().getAddrType()) && "Y".equals(addr.getImportInd())) {
          billingCmr = addr.getParCmrNo();
          abbrevName = addr.getBldg();
          streetAbbrev = addr.getDivn();
          abbrevLoc = addr.getCustFax();
          break;
        }
      }
      if (!StringUtils.isEmpty(billingCmr)) {
        LOG.debug("Billing Cmr :" + billingCmr);
        List<CmrtCustExt> custExtList = LegacyDirectUtil.getBillingChildFromCustExt(entityManager, billingCmr, cntry);
        if (custExtList != null && !custExtList.isEmpty()) {

          if (custExtList.size() > 1) {
            for (CmrtCustExt cExt : custExtList) {
              cExt.setItIVA(!StringUtils.isEmpty(data.getSpecialTaxCd()) ? data.getSpecialTaxCd() : "");
              cExt.setItCodeSSV(!StringUtils.isEmpty(data.getCollectionCd()) ? data.getCollectionCd() : "");
              cExt.setItBillingName(StringUtils.isEmpty(abbrevName) ? "" : abbrevName);
              cExt.setItBillingStreet(StringUtils.isEmpty(streetAbbrev) ? "" : streetAbbrev);
              cExt.setItBillingCity(StringUtils.isEmpty(abbrevLoc) ? "" : abbrevLoc);
              cExt.setUpdateTs(SystemUtil.getCurrentTimestamp());
              updateEntity(cExt, entityManager);
            }

            partialCommit(entityManager);
            entityManager.clear();
          }
        }
      }

    } else { // Update Request

      String billingCustNo = legacyObjects.getCustomerExt().getItBillingCustomerNo();
      String compCustNo = legacyObjects.getCustomerExt().getItCompanyCustomerNo();

      // billing child record update
      if (billingCustNo != null && !StringUtils.isEmpty(billingCustNo) && cmrNo.equals(billingCustNo)) {
        LOG.debug("Billing Cmr :" + billingCustNo);
        List<CmrtCustExt> custExtList = LegacyDirectUtil.getBillingChildFromCustExt(entityManager, billingCustNo, cntry);
        if (custExtList != null && !custExtList.isEmpty()) {

          for (Addr addr : addrs) {
            if ("ZP01".equals(addr.getId().getAddrType())) {
              abbrevName = addr.getBldg();
              streetAbbrev = addr.getDivn();
              abbrevLoc = addr.getCustFax();
              break;
            }
          }

          if (custExtList.size() > 1) {
            for (CmrtCustExt cExt : custExtList) {
              cExt.setItIVA(!StringUtils.isEmpty(data.getSpecialTaxCd()) ? data.getSpecialTaxCd() : "");
              cExt.setItCodeSSV(!StringUtils.isEmpty(data.getCollectionCd()) ? data.getCollectionCd() : "");
              cExt.setItBillingName(StringUtils.isEmpty(abbrevName) ? "" : abbrevName);
              cExt.setItBillingStreet(StringUtils.isEmpty(streetAbbrev) ? "" : streetAbbrev);
              cExt.setItBillingCity(StringUtils.isEmpty(abbrevLoc) ? "" : abbrevLoc);
              cExt.setUpdateTs(SystemUtil.getCurrentTimestamp());
              updateEntity(cExt, entityManager);
            }

            partialCommit(entityManager);
            entityManager.clear();
          }
        }
      }
      // Company child record update
      if (compCustNo != null && !StringUtils.isEmpty(compCustNo) && cmrNo.equals(compCustNo)) {
        LOG.debug("Company Cmr :" + compCustNo);
        List<CmrtCustExt> custExtList = LegacyDirectUtil.getCompanyChildFromCustExt(entityManager, compCustNo, cntry);
        if (custExtList != null && !custExtList.isEmpty()) {
          if (custExtList.size() > 1) {
            List<String> rcuxaList = null;
            for (CmrtCustExt cExt : custExtList) {
              if (rcuxaList == null)
                rcuxaList = new ArrayList<>();

              if (!cmrNo.equals(cExt.getId().getCustomerNo())) {
                rcuxaList.add(cExt.getId().getCustomerNo());
              }

              cExt.setItCompanyCustomerNo(compCustNo);
              cExt.setiTaxCode(StringUtils.isEmpty(data.getTaxCd1()) ? "" : data.getTaxCd1());
              cExt.setItIdentClient(StringUtils.isEmpty(data.getIdentClient()) ? "" : data.getIdentClient());
              updateEntity(cExt, entityManager);
            }

            partialCommit(entityManager);
            entityManager.clear();

            if (rcuxaList != null && !rcuxaList.isEmpty()) {
              if (rcuxaList.size() > 0) {
                LegacyDirectUtil.updateCompanyChildCustRecords(entityManager, rcuxaList, cntry, data.getVat(), data.getEnterprise());
                partialCommit(entityManager);
                entityManager.clear();
              }
            }
          }
        }
      }
    }
  }

  private void processReactivate(EntityManager entityManager, Admin admin) throws Exception {
    LOG.debug("Started Reactivate processing of Request " + admin.getId().getReqId());
    boolean ifHasError = false;
    // lock
    lockRecord(entityManager, admin);

    // get CreateCMR data
    CMRRequestContainer cmrObjects = prepareRequest(entityManager, admin);
    String cntry = cmrObjects.getData().getCmrIssuingCntry();
    MessageTransformer transformer = TransformerManager.getTransformer(cntry);
    String sql = ExternalizedQuery.getSql("GET.MASS.UPDATE");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("PAR_REQ_ID", admin.getId().getReqId());

    List<MassUpdt> records = query.getResults(MassUpdt.class);

    if (records != null && records.size() > 0) {
      for (MassUpdt massUpdt : records) {
        try {
          LegacyDirectObjectContainer legacyObjects = LegacyDirectUtil.getLegacyDBValues(entityManager, cntry, massUpdt.getCmrNo(), false,
              transformer.hasAddressLinks());
          CmrtCust cust = legacyObjects.getCustomer();
          if (cust != null) {
            LOG.info(" - SOF Country: " + cust.getId().getSofCntryCode() + " CMR No.: " + massUpdt.getCmrNo());
            LOG.debug("Setting Status=A for CMR.." + massUpdt.getCmrNo());
            // cust.setStatus(LEGACY_STATUS_ACTIVE);
            // cust.setUpdateTs(SystemUtil.getCurrentTimestamp());
            // set record status = PASS
            massUpdt.setRowStatusCd("PASS");
          }

          if (transformer.hasCmrtCustExt()) {
            CmrtCustExt custExt = legacyObjects.getCustomerExt();
            if (custExt != null && cust != null) {
              if (transformer.hasCmrtCustExtErrorMessage(entityManager, cust, custExt, true)) {
                ifHasError = true;
                massUpdt.setRowStatusCd("FAIL");
                massUpdt.setErrorTxt("All company/billing parent is not active on legacy DB so cannot be reactivated." + massUpdt.getCmrNo());
                LOG.debug("All company/billing parent is not active on legacy DB so cannot be reactivated." + massUpdt.getCmrNo());
                addError(" Mass Reactivation Request " + admin.getId().getReqId()
                    + " Error: Either company or billing is not active on legacy DB so cannot be reactivated CMR-" + massUpdt.getCmrNo());
              }
            }
          }
        } catch (CmrException e) {
          ifHasError = true;
          massUpdt.setRowStatusCd("FAIL");
          massUpdt.setErrorTxt("Customer record doesn't exists on legacy DB so cannot be reactivated." + massUpdt.getCmrNo());
          LOG.debug("Customer record doesn't exists on legacy DB so cannot be reactivated." + massUpdt.getCmrNo());
          addError(" Mass Reactivation Request " + admin.getId().getReqId() + " Error: " + e.getMessage());
        }
        updateEntity(massUpdt, entityManager);
      }
    } else {
      LOG.debug("No record found for processing with READY status");
    }

    if (ifHasError) {
      partialCommit(entityManager);
      processError(entityManager, admin,
          "Some of the records got failed while processing Mass Reactivation Request.Please check request summary for details");
    } else {
      completeReactDelRecord(entityManager, admin);
    }

  }

  private void processDelete(EntityManager entityManager, Admin admin) throws Exception {
    LOG.debug("Started Delete processing of Request " + admin.getId().getReqId());
    boolean ifHasError = false;
    // lock
    lockRecord(entityManager, admin);

    // get CreateCMR data
    CMRRequestContainer cmrObjects = prepareRequest(entityManager, admin);
    String cntry = cmrObjects.getData().getCmrIssuingCntry();
    MessageTransformer transformer = TransformerManager.getTransformer(cntry);
    String sql = ExternalizedQuery.getSql("GET.MASS.UPDATE");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("PAR_REQ_ID", admin.getId().getReqId());

    List<MassUpdt> records = query.getResults(MassUpdt.class);

    if (records != null && records.size() > 0) {
      for (MassUpdt massUpdt : records) {
        try {
          LegacyDirectObjectContainer legacyObjects = LegacyDirectUtil.getLegacyDBValues(entityManager, cntry, massUpdt.getCmrNo(), false,
              transformer.hasAddressLinks());
          CmrtCust cust = legacyObjects.getCustomer();
          if (cust != null) {
            LOG.info(" - SOF Country: " + cust.getId().getSofCntryCode() + " CMR No.: " + massUpdt.getCmrNo());
            LOG.debug("Setting Status=C for CMR..");
            // cust.setStatus(LEGACY_STATUS_CANCELLED);
            // cust.setUpdateTs(SystemUtil.getCurrentTimestamp());
            // set record status = PASS
            massUpdt.setRowStatusCd("PASS");
          }

          if (transformer.hasCmrtCustExt()) {
            CmrtCustExt custExt = legacyObjects.getCustomerExt();
            if (custExt != null && cust != null) {
              if (transformer.hasCmrtCustExtErrorMessage(entityManager, cust, custExt, false)) {
                ifHasError = true;
                massUpdt.setRowStatusCd("FAIL");
                massUpdt.setErrorTxt("All child of company/billing is not inactive on legacy DB so cannot be inactivated." + massUpdt.getCmrNo());
                LOG.debug("All child of company/billing is not inactive on legacy DB so cannot be inactivated." + massUpdt.getCmrNo());
                addError(" Mass Inactivation Request " + admin.getId().getReqId()
                    + " Error: Either company/billing is not inactive on legacy DB so cannot be inactivated CMR-" + massUpdt.getCmrNo());
              }
            }
          }

        } catch (CmrException e) {
          ifHasError = true;
          massUpdt.setRowStatusCd("FAIL");
          massUpdt.setErrorTxt("Customer record doesn't exists on legacy DB so cannot be inactivated." + massUpdt.getCmrNo());
          LOG.debug("Customer record doesn't exists on legacy DB so cannot be inactivated." + massUpdt.getCmrNo());
          addError(" Mass Inactivation Request " + admin.getId().getReqId() + " Error: " + e.getMessage());
        }
        updateEntity(massUpdt, entityManager);
      }
    } else {
      LOG.debug("No record found for processing with READY status");
    }

    if (ifHasError) {
      partialCommit(entityManager);
      processError(entityManager, admin,
          "Some of the records got failed while processing Mass Delete Request.Please check request summary for details");
    } else {
      completeReactDelRecord(entityManager, admin);
    }
  }

  private void processSingleReactivate(EntityManager entityManager, Admin admin) throws Exception {

    LOG.debug("Started Single Reactivate processing of Request " + admin.getId().getReqId());

    processUpdate(entityManager, admin);

    // lock
    lockRecord(entityManager, admin);

    // get CreateCMR data
    CMRRequestContainer cmrObjects = prepareRequest(entityManager, admin);

    if (admin.getRdcProcessingStatus() != null
        && (CmrConstants.RDC_STATUS_ABORTED.equals(admin.getRdcProcessingStatus())
            || CmrConstants.RDC_STATUS_NOT_COMPLETED.equals(admin.getRdcProcessingStatus()))
        && !StringUtils.isEmpty(cmrObjects.getData().getCmrNo())) {
      skipLegacyProcessingforCreateUpdate(entityManager, admin);
    } else {
      // prepare legacy data, map to the values needed
      LegacyDirectObjectContainer legacyObjects = mapRequestDataForCreate(entityManager, cmrObjects);
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
  private void completeTRECRecord(EntityManager entityManager, Admin admin, String cmrNo, LegacyDirectObjectContainer legacyObjects)
      throws CmrException, SQLException {
    LOG.info("Completing legacy processing for  Request " + admin.getId().getReqId());
    admin.setLockBy(null);
    admin.setLockByNm(null);
    admin.setLockInd("N");
    // completing
    admin.setProcessedFlag("Y");
    admin.setReqStatus("CPR");
    admin.setLastUpdtBy(BATCH_USER_ID);
    updateEntity(admin, entityManager);

    String message = "Records updated successfully on the Legacy Database. CMR No. " + cmrNo + " "
        + (CmrConstants.REQ_TYPE_CREATE.equals(admin.getReqType()) ? " assigned." : " updated.");

    if (CmrConstants.REQ_TYPE_UPDATE.equals(admin.getReqType())) {
      StringBuilder seqCmt = new StringBuilder();

      if (seqCmt.toString().trim().length() > 0) {
        message += "\nSequences Generated:\n" + seqCmt.toString();
      }
      message = message.trim();
    }
    WfHist hist = createHistory(entityManager, message, "CPR", "Legacy Processing", admin.getId().getReqId());
    createComment(entityManager, message, admin.getId().getReqId());
    RequestUtils.sendEmailNotifications(entityManager, admin, hist, false, true);
    partialCommit(entityManager);
  }

  private void completeRecord(EntityManager entityManager, Admin admin, String cmrNo, LegacyDirectObjectContainer legacyObjects,
      CMRRequestContainer cmrObjects) throws CmrException, SQLException {
    LOG.info("Completing legacy processing for  Request " + admin.getId().getReqId());
    String cntry = cmrObjects.getData().getCmrIssuingCntry();
    MessageTransformer transformer = TransformerManager.getTransformer(cntry);
    boolean hasDupCreates = false;
    String targetCntry = transformer.getGmllcDupCreation(cmrObjects.getData());
    if (CmrConstants.REQ_TYPE_CREATE.equals(admin.getReqType()) && !"NA".equals(targetCntry)) {
      hasDupCreates = true;
    }

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

    if (hasDupCreates) {
      message += "\n" + "Records created successfully on the Legacy Database. CMR No. " + cmrNo + " assigned for country " + targetCntry;
    }

    WfHist hist = createHistory(entityManager, message, "PCO", "Legacy Processing", admin.getId().getReqId());
    createComment(entityManager, message, admin.getId().getReqId());
    RequestUtils.sendEmailNotifications(entityManager, admin, hist, false, true);
    // CREATCMR-2625,6677
    // String mailFlag = transformer.getMailSendingFlag(cmrObjects.getData(),
    // admin, entityManager);
    // if (!"NA".equals(mailFlag)) {
    // String mailTemplate = transformer.getEmailTemplateName(mailFlag);
    // String statusForMailSend =
    // transformer.getReqStatusForSendingMail(mailFlag);
    // if (statusForMailSend != null && "PCO".equals(statusForMailSend)) {
    // BatchEmailModel mailParams =
    // transformer.getMailFormatParams(entityManager, cmrObjects, mailFlag);
    // LegacyCommonUtil.sendfieldUpdateEmailNotification(entityManager,
    // mailParams, mailTemplate);
    // }
    // }

    partialCommit(entityManager);
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
  private void completeReactDelRecord(EntityManager entityManager, Admin admin) throws CmrException, SQLException {
    LOG.info("Completing legacy processing for Mass Request " + admin.getId().getReqId());
    admin.setLockBy(null);
    admin.setLockByNm(null);
    admin.setLockInd("N");
    // completing
    admin.setProcessedFlag("Y");
    admin.setReqStatus("PCO");
    admin.setLastUpdtBy(BATCH_USER_ID);
    updateEntity(admin, entityManager);

    String message = "Records updated successfully on the Legacy Database ";

    WfHist hist = createHistory(entityManager, message, "PCO", "Legacy Processing", admin.getId().getReqId());
    createComment(entityManager, message, admin.getId().getReqId());

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

  /**
   * Does the mapping of legacy data to CreateCMR data for creates. The flow
   * uses a base mapping which can be overridden on the specific
   * {@link MessageTransformer} class of the country
   * 
   * @param entityManager
   * @param cmrObjects
   * @throws Exception
   */
  private LegacyDirectObjectContainer mapRequestDataForCreate(EntityManager entityManager, CMRRequestContainer cmrObjects) throws Exception {
    LegacyDirectObjectContainer legacyObjects = new LegacyDirectObjectContainer();

    Data data = cmrObjects.getData();
    Admin admin = cmrObjects.getAdmin();
    String cmrNo = data.getCmrNo();
    String cntry = data.getCmrIssuingCntry();
    MessageTransformer transformer = TransformerManager.getTransformer(cntry);
    String targetCountry = null;

    // CREATCMR-1690
    if (!StringUtils.isEmpty(cmrNo) && !"Y".equals(admin.getProspLegalInd())) {
      boolean isCMRExist = LegacyDirectUtil.checkCMRNoInLegacyDB(entityManager, data);
      LOG.info("Checking existing CMR in create Process...");
      if (isCMRExist) {
        LOG.trace("CMR#" + cmrNo + " is already exist in Legacy DB for Country# " + data.getCmrIssuingCntry());
        throw new Exception("CMR#" + cmrNo + " is already exist in Legacy");
      }
    } else {
      if (transformer != null) {
        targetCountry = transformer.getGmllcDupCreation(data);
      }
      targetCountry = "NA".equals(targetCountry) ? data.getCmrIssuingCntry() : targetCountry;

      // CMR-6019
      if (null != data.getDupIssuingCntryCd() && !data.getDupIssuingCntryCd().equals("")
          && ("Y".equals(data.getCisServiceCustIndc()) || "Y".equals(data.getDupCmrIndc()))) {
        targetCountry = data.getDupIssuingCntryCd();
      }

      cmrNo = generateCMRNo(entityManager, cmrObjects, data.getCmrIssuingCntry(), targetCountry);
    }

    legacyObjects.setCustomerNo(cmrNo);
    legacyObjects.setSofCntryCd(cntry);

    LOG.debug("CMR No. " + cmrNo + " generated and assigned.");
    CmrtCust cust = initEmpty(CmrtCust.class);

    // default mapping for DATA and CMRTCUST
    LOG.debug("Mapping default Data values..");
    CmrtCustPK custPk = new CmrtCustPK();
    custPk.setCustomerNo(cmrNo);
    custPk.setSofCntryCode(cntry);
    cust.setId(custPk);
    cust.setRealCtyCd(cntry);
    cust.setStatus(LEGACY_STATUS_ACTIVE);
    cust.setAbbrevNm(data.getAbbrevNm());
    cust.setAbbrevLocn(data.getAbbrevLocn());
    cust.setLocNo(data.getLocationNumber());
    cust.setEconomicCd(data.getEconomicCd());
    String legacyLangCdMapValue = getLangCdLegacyMapping(entityManager, data, cntry);
    cust.setLangCd(!StringUtils.isEmpty(legacyLangCdMapValue) ? legacyLangCdMapValue : "");
    cust.setMrcCd(data.getMrcCd());
    cust.setModeOfPayment(data.getModeOfPayment());
    cust.setIsuCd(
        (!StringUtils.isEmpty(data.getIsuCd()) ? data.getIsuCd() : "") + (!StringUtils.isEmpty(data.getClientTier()) ? data.getClientTier() : ""));
    cust.setCreditCd(data.getCreditCd());
    cust.setTaxCd(data.getSpecialTaxCd());

    cust.setSalesRepNo(data.getRepTeamMemberNo());
    cust.setSalesGroupRep(data.getSalesTeamCd());

    if (!StringUtils.isEmpty(data.getEnterprise())) {
      cust.setEnterpriseNo(data.getEnterprise());
    }
    cust.setCeBo(data.getEngineeringBo());
    cust.setIbo((!StringUtils.isEmpty(data.getSalesBusOffCd()) ? data.getSalesBusOffCd() : ""));
    cust.setSbo((!StringUtils.isEmpty(data.getSalesBusOffCd()) ? data.getSalesBusOffCd() : ""));
    cust.setBankNo(data.getIbmBankNumber());

    cust.setCollectionCd(data.getCollectionCd() != null ? data.getCollectionCd() : "");

    cust.setMailingCond(data.getMailingCondition());
    cust.setAccAdminBo(data.getAcAdminBo());
    cust.setEmbargoCd(data.getEmbargoCd());
    cust.setIsicCd(data.getIsicCd());
    if (!StringUtils.isEmpty(data.getInacCd())) {
      cust.setInacCd(data.getInacCd());
    }
    cust.setVat(data.getVat());
    cust.setImsCd(data.getSubIndustryCd());
    cust.setCurrencyCd(data.getLegacyCurrencyCd());
    cust.setInvoiceCpyReqd(" "); // setting default value blank

    // do a dummy transfer here, reuse MQ objects for formatting
    MqIntfReqQueue dummyQueue = new MqIntfReqQueue();
    dummyQueue.setCmrNo(cmrNo);
    dummyQueue.setCmrIssuingCntry(cntry);
    dummyQueue.setReqType(admin.getReqType());
    SOFMessageHandler dummyHandler = new SOFMessageHandler(entityManager, dummyQueue);
    dummyHandler.adminData = admin;
    dummyHandler.cmrData = data;
    if (cmrObjects.getAddresses() != null && !cmrObjects.getAddresses().isEmpty()) {
      dummyHandler.addrData = cmrObjects.getAddresses().get(0);
    }
    dummyHandler.currentAddresses = cmrObjects.getAddresses();
    if (transformer != null) {
      // call the specific transformer to change data as needed
      dummyQueue.setReqStatus(MQMsgConstants.REQ_STATUS_NEW);
      transformer.transformLegacyCustomerData(entityManager, dummyHandler, cust, cmrObjects);
    }
    cust.setLeadingAccNo(cmrNo + cust.getMrcCd());// setting leading account no
    // = cmr+mrc
    capsAndFillNulls(cust, true);
    legacyObjects.setCustomer(cust);

    CmrtAddr legacyAddr = null;
    CmrtAddrPK legacyAddrPk = null;

    String sofKey = null;
    String addressUse = null;

    int seqNo = 1;
    // int nextSeq = 7;

    LOG.debug("Mapping default address values..");
    if (transformer != null) {
      for (Addr addr : cmrObjects.getAddresses()) {

        // detach the addr, no updates needed here for now
        entityManager.detach(addr);

        if (transformer.skipLegacyAddressData(entityManager, cmrObjects, addr, true)) {
          LOG.debug("Skipping legacy create for address :" + addr.getId().getAddrType());
          continue;
        }

        dummyHandler.addrData = addr;
        dummyQueue.setReqStatus(seqNo == 1 ? MQMsgConstants.REQ_STATUS_NEW : "COM" + (seqNo - 1));
        legacyAddr = initEmpty(CmrtAddr.class);
        legacyAddrPk = new CmrtAddrPK();
        legacyAddrPk.setCustomerNo(cmrNo);
        legacyAddrPk.setSofCntryCode(cntry);
        String newSeqNo = StringUtils.leftPad(Integer.toString(seqNo), 5, '0');
        
        // CREATCMR - 8014 -> for API Requests
    	String reqAddrSeq = addr.getId().getAddrSeq(); 
        if(StringUtils.isNotEmpty(reqAddrSeq) && reqAddrSeq.length() < 5) {
        	String paddedSeq = StringUtils.leftPad(reqAddrSeq, 5, '0');
        	addr.getId().setAddrSeq(paddedSeq);
        }
        // Mukesh:Story 1698123
        if ("00001".equals(addr.getId().getAddrSeq()))
          legacyAddrPk.setAddrNo(newSeqNo);
        else
          legacyAddrPk.setAddrNo(addr.getId().getAddrSeq());
        // legacyAddrPk.setAddrNo(Integer.toString(nextSeq));
        // legacyAddrPk.setAddrNo(Integer.toString(seqNo));
        legacyAddr.setId(legacyAddrPk);

        sofKey = transformer.getAddressKey(addr.getId().getAddrType());
        transformer.formatAddressLines(dummyHandler);

        legacyAddr.setAddrLine1(dummyHandler.messageHash.get(sofKey + "Address1"));
        legacyAddr.setAddrLine2(dummyHandler.messageHash.get(sofKey + "Address2"));
        legacyAddr.setAddrLine3(dummyHandler.messageHash.get(sofKey + "Address3"));
        legacyAddr.setAddrLine4(dummyHandler.messageHash.get(sofKey + "Address4"));
        legacyAddr.setAddrLine5(dummyHandler.messageHash.get(sofKey + "Address5"));
        legacyAddr.setAddrLine6(dummyHandler.messageHash.get(sofKey + "Address6"));
        legacyAddr.setAddrLineI(dummyHandler.messageHash.get(sofKey + "AddressI"));
        legacyAddr.setAddrLineO(dummyHandler.messageHash.get(sofKey + "AddressO"));
        legacyAddr.setAddrLineT(dummyHandler.messageHash.get(sofKey + "AddressT"));
        legacyAddr.setAddrLineU(dummyHandler.messageHash.get(sofKey + "AddressU"));

        legacyAddr.setStreet(addr.getAddrTxt());

        // Turkey not store phone on Address level
        if (!SystemLocation.TURKEY.equals(cntry)) {
          if ("ZD01".equals(addr.getId().getAddrType()) && !StringUtils.isEmpty(addr.getCustPhone())) {
            legacyAddr.setAddrPhone("TF" + addr.getCustPhone().trim());
          }
        }
        legacyAddr.setCity(addr.getCity1());
        legacyAddr.setContact(addr.getDept());
        legacyAddr.setCreateTs(SystemUtil.getCurrentTimestamp());
        legacyAddr.setUpdateTs(SystemUtil.getCurrentTimestamp());

        if (!StringUtils.isBlank(addr.getPoBox())) {
          legacyAddr.setPoBox("APTO " + addr.getPoBox().trim());
        } else {
          legacyAddr.setPoBox(addr.getPoBox());
        }

        legacyAddr.setZipCode(addr.getPostCd());
        legacyAddr.setForCreate(true);
        legacyAddr.setForSharedSeq(false);

        transformer.transformLegacyAddressData(entityManager, dummyHandler, cust, legacyAddr, cmrObjects, addr);
        capsAndFillNulls(legacyAddr, true);
        legacyObjects.addAddress(legacyAddr);

        // parse each address use
        addressUse = transformer.getAddressUse(addr);

        modifyAddrUseFields(legacyAddr.getId().getAddrNo(), addressUse, legacyAddr);

        // update the seqno of the original addr record
        if ("00001".equals(addr.getId().getAddrSeq())) {
          updateAddrSeq(entityManager, admin.getId().getReqId(), addr.getId().getAddrType(), addr.getId().getAddrSeq(),
              legacyAddr.getId().getAddrNo() + "", null, legacyAddr.isForSharedSeq());
          seqNo++;
        }
      }

      CmrtCustExt custExt = null;
      CmrtCustExtPK custExtPk = null;

      boolean isCustExt = transformer.hasCmrtCustExt();
      if (isCustExt) {
        LOG.debug("Mapping default Data values with Legacy CmrtCustExt table.....");
        // Initialize the object
        custExt = initEmpty(CmrtCustExt.class);
        // default mapping for ADDR and CMRTCEXT
        custExtPk = new CmrtCustExtPK();
        custExtPk.setCustomerNo(cmrNo);
        custExtPk.setSofCntryCode(cntry);
        custExt.setId(custExtPk);

        if (transformer != null) {
          transformer.transformLegacyCustomerExtData(entityManager, dummyHandler, custExt, cmrObjects);
        }
        custExt.setUpdateTs(SystemUtil.getCurrentTimestamp());
        custExt.setAeciSubDt(SystemUtil.getDummyDefaultDate());
        legacyObjects.setCustomerExt(custExt);
      }
      transformer.transformOtherData(entityManager, legacyObjects, cmrObjects);

    }

    return legacyObjects;
  }

  /**
   * Does the mapping of legacy data to CreateCMR data for updates. The flow
   * uses a base mapping which can be overridden on the specific
   * {@link MessageTransformer} class of the country
   * 
   * @param entityManager
   * @param cmrObjects
   * @throws Exception
   */
  private LegacyDirectObjectContainer mapRequestDataForUpdate(EntityManager entityManager, CMRRequestContainer cmrObjects) throws Exception {

    Data data = cmrObjects.getData();
    Admin admin = cmrObjects.getAdmin();
    String cmrNo = data.getCmrNo();
    String cntry = data.getCmrIssuingCntry();
    MessageTransformer transformer = TransformerManager.getTransformer(cntry);

    LegacyDirectObjectContainer legacyObjects = LegacyDirectUtil.getLegacyDBValues(entityManager, cntry, cmrNo, false, transformer.hasAddressLinks());

    CmrtCust cust = legacyObjects.getCustomer();

    // default mapping for DATA and CMRTCUST
    LOG.debug("Mapping default Data values..");
    cust.setStatus(LEGACY_STATUS_ACTIVE);
    if (!StringUtils.isBlank(data.getAbbrevNm())) {
      cust.setAbbrevNm(data.getAbbrevNm());
    }
    if (!StringUtils.isBlank(data.getAbbrevLocn())) {
      cust.setAbbrevLocn(data.getAbbrevLocn());
    }
    if (!StringUtils.isBlank(data.getLocationNumber())) {
      cust.setLocNo(data.getLocationNumber());
    }
    if (!StringUtils.isBlank(data.getEconomicCd())) {
      cust.setEconomicCd(data.getEconomicCd());
    }

    String legacyLangCdMapValue = getLangCdLegacyMapping(entityManager, data, cntry);
    cust.setLangCd(!StringUtils.isEmpty(legacyLangCdMapValue) ? legacyLangCdMapValue : " ");

    if (!StringUtils.isBlank(data.getMrcCd())) {
      cust.setMrcCd(data.getMrcCd());
    }
    /*
     * if (!StringUtils.isBlank(data.getModeOfPayment())) {
     * cust.setModeOfPayment(data.getModeOfPayment()); }
     */
    cust.setModeOfPayment(data.getModeOfPayment() != null ? data.getModeOfPayment() : "");

    String isuClientTier = (!StringUtils.isEmpty(data.getIsuCd()) ? data.getIsuCd() : "")
        + (!StringUtils.isEmpty(data.getClientTier()) ? data.getClientTier() : "");
    if (isuClientTier != null && isuClientTier.length() == 3) {
      cust.setIsuCd(isuClientTier);
    }

    if (!StringUtils.isBlank(data.getCreditCd()) && (transformer != null && !transformer.skipCreditCodeUpdateForCountry())) {
      cust.setCreditCd(data.getCreditCd());
    }
    if (!StringUtils.isBlank(data.getSpecialTaxCd())) {
      cust.setTaxCd(data.getSpecialTaxCd());
    } else {
      cust.setTaxCd("");
    }

    if (!StringUtils.isBlank(data.getRepTeamMemberNo())) {
      cust.setSalesRepNo(data.getRepTeamMemberNo());
    }
    if (!StringUtils.isBlank(data.getSalesTeamCd())) {
      cust.setSalesGroupRep(data.getSalesTeamCd());
    }

    if (!StringUtils.isBlank(data.getEnterprise())) {
      cust.setEnterpriseNo(data.getEnterprise());
    }
    if (!StringUtils.isBlank(data.getEngineeringBo())) {
      cust.setCeBo(data.getEngineeringBo());
    }
    if (!StringUtils.isBlank(data.getSalesBusOffCd())) {
      cust.setIbo(data.getSalesBusOffCd());
      cust.setSbo(data.getSalesBusOffCd());
    }
    if (!StringUtils.isBlank(data.getIbmDeptCostCenter())) {
      cust.setBankBranchNo(data.getIbmDeptCostCenter());
    }
    if (!StringUtils.isBlank(data.getIbmBankNumber())) {
      cust.setBankNo(data.getIbmBankNumber());
    }

    cust.setCollectionCd(data.getCollectionCd() != null ? data.getCollectionCd() : "");
    // cust.setDistrictCd(data.getCollectionCd() != null ?
    // data.getCollectionCd() : "");
    if (!StringUtils.isBlank(data.getMailingCondition())) {
      cust.setMailingCond(data.getMailingCondition());
    }
    if (!StringUtils.isBlank(data.getAcAdminBo())) {
      cust.setAccAdminBo(data.getAcAdminBo());
    }

    cust.setEmbargoCd(data.getEmbargoCd() != null ? data.getEmbargoCd() : "");

    if (!StringUtils.isBlank(data.getIsicCd())) {
      cust.setIsicCd(data.getIsicCd());
    }
    cust.setInacCd(data.getInacCd() != null ? data.getInacCd() : "");
    if (!StringUtils.isBlank(data.getVat())) {
      cust.setVat(data.getVat());
    }
    if (!StringUtils.isBlank(data.getSubIndustryCd())) {
      cust.setImsCd(data.getSubIndustryCd());
    }
    if (!StringUtils.isBlank(data.getLegacyCurrencyCd())) {
      cust.setCurrencyCd(data.getLegacyCurrencyCd());
    }
    // Mukesh:Defect 1703041: FVT: Update_TS is not getting updated for Update
    // requests in Legacy DB
    cust.setUpdateTs(SystemUtil.getCurrentTimestamp());
    // cust.setUpdStatusTs(SystemUtil.getCurrentTimestamp());

    // Setting status=A in case of single reactivation requests
    if (admin.getReqReason() != null && !StringUtils.isBlank(admin.getReqReason()) && CMR_REACTIVATION_REQUEST_REASON.equals(admin.getReqReason())) {
      cust.setStatus(LEGACY_STATUS_ACTIVE);
    }

    // put code in country specific: permanent removal-single inactivation

    // put code in country specific: Support temporary reactivation

    // do a dummy transfer here, reuse MQ objects for formatting
    MqIntfReqQueue dummyQueue = new MqIntfReqQueue();
    dummyQueue.setCmrNo(cmrNo);
    dummyQueue.setCmrIssuingCntry(cntry);
    dummyQueue.setReqType(admin.getReqType());
    SOFMessageHandler dummyHandler = new SOFMessageHandler(entityManager, dummyQueue);
    dummyHandler.adminData = admin;
    dummyHandler.cmrData = data;
    if (cmrObjects.getAddresses() != null && !cmrObjects.getAddresses().isEmpty()) {
      dummyHandler.addrData = cmrObjects.getAddresses().get(0);
    }
    dummyHandler.currentAddresses = cmrObjects.getAddresses();
    if (transformer != null) {
      // call the specific transformer to change data as needed
      dummyQueue.setReqStatus(MQMsgConstants.REQ_STATUS_NEW);
      transformer.transformLegacyCustomerData(entityManager, dummyHandler, cust, cmrObjects);
      if (transformer.enableTempReactOnUpdates()) {
        LegacyCommonUtil.processDB2TemporaryReactChanges(admin, cust, data, entityManager);
      }
    }
    capsAndFillNulls(cust, true);
    legacyObjects.setCustomer(cust);

    CmrtAddr legacyAddr = null;
    CmrtAddrPK legacyAddrPk = null;

    String sofKey = null;
    String addressUse = null;

    boolean zs01Updated = false;
    String zs01SeqNo = null;

    boolean ctyaUpdated = false;
    String ctyaSeqNo = null;

    Map<String, Integer> sequences = new HashMap<String, Integer>();
    for (Addr addr : cmrObjects.getAddresses()) {
      if ("ZS01".equals(addr.getId().getAddrType())) {
        zs01SeqNo = addr.getId().getAddrSeq();

        zs01Updated = "Y".equals(addr.getChangedIndc());
      }

      if (SystemLocation.ISRAEL.equals(data.getCmrIssuingCntry()) && "CTYA".equals(addr.getId().getAddrType())) {
        ctyaSeqNo = addr.getId().getAddrSeq();
        ctyaUpdated = "Y".equals(addr.getChangedIndc());
      }

      if (!sequences.containsKey(addr.getId().getAddrSeq())) {
        sequences.put(addr.getId().getAddrSeq(), 0);
      }
      sequences.put(addr.getId().getAddrSeq(), sequences.get(addr.getId().getAddrSeq()) + 1);
    }

    if (SystemLocation.ISRAEL.equals(data.getCmrIssuingCntry()) && (zs01Updated || ctyaUpdated)) {
      zs01Updated = true;
      ctyaUpdated = true;
    }

    LOG.debug("Sequences : " + sequences);

    Queue<Integer> availableSequences = new LinkedList<Integer>();
    if (SystemLocation.IRELAND.equals(cntry)) {
      availableSequences = getAvailableSequences(entityManager, "866", cmrNo);
    } else {
      availableSequences = getAvailableSequences(entityManager, cntry, cmrNo);
    }
    for (Addr addr : cmrObjects.getAddresses()) {
      if (!SystemLocation.ITALY.equals(data.getCmrIssuingCntry())) {
        if (availableSequences.contains(Integer.parseInt(addr.getId().getAddrSeq()))) {
          availableSequences.remove(Integer.parseInt(addr.getId().getAddrSeq()));
          LOG.debug("Removing seq=" + addr.getId().getAddrSeq() + " from available sequences as it already exists on request");
        }
      } else {
        if (availableSequences.contains(addr.getId().getAddrSeq())) {
          availableSequences.remove(Integer.parseInt(addr.getId().getAddrSeq()));
          LOG.debug("Removing seq=" + addr.getId().getAddrSeq() + " from available sequences as it already exists on request");
        }
      }
    }

    List<String> rdcSeq = checkIfSeqExistOnRDC(entityManager, cmrNo, cntry);

    if (!rdcSeq.isEmpty()) {
      for (int i = 0; i < rdcSeq.size(); i++) {
        if (availableSequences.contains(Integer.parseInt(rdcSeq.get(i)))) {
          availableSequences.remove(Integer.parseInt(rdcSeq.get(i)));
          LOG.debug("Removing seq=" + rdcSeq.get(i) + " from available sequences as it already exist on RDC.");
        }
      }
    }

    int seqNo = 0;
    LOG.debug("Mapping default address values..");
    if (transformer != null) {
      List<Integer> secondarySoldToListIL = null;
      if (SystemLocation.ISRAEL.equals(data.getCmrIssuingCntry())) {
        secondarySoldToListIL = getSecondarySoldToFromRDC(entityManager, cmrNo, cntry);
      }

      for (Addr addr : cmrObjects.getAddresses()) {
        if (transformer.skipLegacyAddressData(entityManager, cmrObjects, addr, false)) {
          LOG.debug("Skipping legacy update for address :" + addr.getId().getAddrType());
          continue;
        }
        boolean addrUpdated = RequestUtils.isUpdated(entityManager, addr, data.getCmrIssuingCntry());
        // iterate each address on the request and check if it needs to be
        // processed
        dummyHandler.addrData = addr;
        dummyQueue.setReqStatus(seqNo == 0 ? MQMsgConstants.REQ_STATUS_NEW : "COM" + (seqNo - 1));

        addrUpdated = "N".equals(addr.getImportInd()) || "Y".equals(addr.getChangedIndc());

        boolean isSharedWithSoldToPairIL = SystemLocation.ISRAEL.equals(data.getCmrIssuingCntry()) && ctyaUpdated
            && ctyaSeqNo.equals(addr.getId().getAddrSeq());

        if (SystemLocation.ISRAEL.equals(data.getCmrIssuingCntry()) && !addrUpdated) {
          addrUpdated = isAddrPairUpdatedIL(addr, cmrObjects.getAddresses());
        }

        // address was directly updated, or was not updated and shares sequence
        // with sold to
        if (addrUpdated || (zs01Updated && zs01SeqNo.equals(addr.getId().getAddrSeq()) || isSharedWithSoldToPairIL)) {
          boolean isSecondarySoldToIL = false;
          if (SystemLocation.ISRAEL.equals(data.getCmrIssuingCntry()) && sequences.get(addr.getId().getAddrSeq()) == 1) {
            isSecondarySoldToIL = checkIfAddrIsSecondarySoldToIL(secondarySoldToListIL, addr);
          }

          boolean isSoldToPairIL = SystemLocation.ISRAEL.equals(data.getCmrIssuingCntry()) && "CTYA".equals(addr.getId().getAddrType());
          if ("ZS01".equals(addr.getId().getAddrType()) || isSoldToPairIL) {
            LOG.debug("ZS01 address is updated, directly updating relevant records");
            // zs01 addresses should not change sequence, move everything else
            // Mukesh:Story 1698123
            legacyAddr = legacyObjects.findBySeqNo(addr.getId().getAddrSeq());
            if (legacyAddr == null) {
              throw new Exception("Cannot find legacy address with sequence " + addr.getId().getAddrSeq());
            }
            legacyAddr.setForUpdate(true);
          } else if ("N".equals(addr.getImportInd()) || sequences.get(addr.getId().getAddrSeq()) > 1 || isSecondarySoldToIL) {
            // a. an added address
            // b. address shares a sequence
            LOG.trace("Created? " + ("N".equals(addr.getImportInd())) + " Shared? " + (sequences.get(addr.getId().getAddrSeq()) > 1));
            LOG.debug(addr.getId().getAddrType() + " address needs to be created on a new sequence.");
            // create a new address
            legacyAddr = initEmpty(CmrtAddr.class);
            legacyAddrPk = new CmrtAddrPK();
            legacyAddrPk.setCustomerNo(cmrNo);
            legacyAddrPk.setSofCntryCode(cntry);
            int newSeq;
            String newAddrSeq;
            if (transformer.sequenceNoUpdateLogic(entityManager, cmrObjects, addr, false)) {
              newSeq = availableSequences.remove();
              newAddrSeq = Integer.toString(newSeq);
            } else {
              newAddrSeq = addr.getId().getAddrSeq();
            }

            if (CEE_COUNTRY_LIST.contains(cntry)) {
              if ("598".equals(addr.getId().getAddrSeq()) || "599".equals(addr.getId().getAddrSeq())) {
                newAddrSeq = addr.getId().getAddrSeq();
              } else {
                newAddrSeq = StringUtils.leftPad(newAddrSeq, 5, '0');
              }
            } else {
              newAddrSeq = StringUtils.leftPad(newAddrSeq, 5, '0');
            }

            LOG.debug("Assigning Sequence " + newAddrSeq + " to " + addr.getId().getAddrType() + " address");
            // Mukesh:Story 1698123
            legacyAddrPk.setAddrNo(newAddrSeq);
            legacyAddr.setId(legacyAddrPk);
            legacyAddr.setForUpdate(false);
            legacyAddr.setForCreate(true);
            if (sequences.get(addr.getId().getAddrSeq()) > 1)
              legacyAddr.setForSharedSeq(true);
            else
              legacyAddr.setForSharedSeq(false);
            legacyAddr.setCreateTs(SystemUtil.getCurrentTimestamp());
          } else {
            // plain update
            LOG.debug(addr.getId().getAddrType() + " address is updated, directly updating relevant records");
            // Mukesh:Story 1698123
            if ("ZP01".equals(addr.getId().getAddrType()) && StringUtils.isEmpty(addr.getSapNo())
                && SystemLocation.ITALY.equals(data.getCmrIssuingCntry())) {
              // legacyAddr = legacyObjects.findByAddressUse(addressUse);
              legacyAddr = legacyObjects.findByAddressUseFlag("2");
              // legacyAddr.getId().setAddrNo(addr.getId().getAddrSeq());
            } else {
              legacyAddr = legacyObjects.findBySeqNo(addr.getId().getAddrSeq());
            }

            if (legacyAddr == null) {
              throw new Exception("Cannot find legacy address with sequence " + addr.getId().getAddrSeq());
            }
            legacyAddr.setForUpdate(true);
          }

          legacyAddr.setAddressUse(transformer.getAddressUse(addr));
          // modifyAddrUseFields(legacyAddr.getId().getAddrNo(),transformer.getAddressUse(addr),legacyAddr);
          sofKey = transformer.getAddressKey(addr.getId().getAddrType());

          if (LegacyDirectUtil.isCountryLegacyDirectEnabled(entityManager, cntry)) {
            transformer.transformLegacyAddressData(entityManager, dummyHandler, cust, legacyAddr, cmrObjects, addr);
          } else {
            transformer.formatAddressLines(dummyHandler);
          }

          legacyAddr.setAddrLine1(dummyHandler.messageHash.get(sofKey + "Address1"));
          legacyAddr.setAddrLine2(dummyHandler.messageHash.get(sofKey + "Address2"));
          legacyAddr.setAddrLine3(dummyHandler.messageHash.get(sofKey + "Address3"));
          legacyAddr.setAddrLine4(dummyHandler.messageHash.get(sofKey + "Address4"));
          legacyAddr.setAddrLine5(dummyHandler.messageHash.get(sofKey + "Address5"));
          legacyAddr.setAddrLine6(dummyHandler.messageHash.get(sofKey + "Address6"));
          if (!StringUtils.isBlank(dummyHandler.messageHash.get(sofKey + "AddressI"))) {
            legacyAddr.setAddrLineI(dummyHandler.messageHash.get(sofKey + "AddressI"));
          }
          if (!StringUtils.isBlank(dummyHandler.messageHash.get(sofKey + "AddressO"))) {
            legacyAddr.setAddrLineO(dummyHandler.messageHash.get(sofKey + "AddressO"));
          }
          if (!StringUtils.isBlank(dummyHandler.messageHash.get(sofKey + "AddressT"))) {
            legacyAddr.setAddrLineT(dummyHandler.messageHash.get(sofKey + "AddressT"));
          }
          if (!StringUtils.isBlank(dummyHandler.messageHash.get(sofKey + "AddressU"))) {
            legacyAddr.setAddrLineU(dummyHandler.messageHash.get(sofKey + "AddressU"));
          }

          if (!StringUtils.isBlank(addr.getAddrTxt())) {
            legacyAddr.setStreet(addr.getAddrTxt());
          }
          if ("ZD01".equals(addr.getId().getAddrType()) && !StringUtils.isEmpty(addr.getCustPhone())) {
            legacyAddr.setAddrPhone("TF" + addr.getCustPhone().trim());
          }
          // this is for CEE countries IGF address
          if (CEE_COUNTRY_LIST.contains(cntry)) {
            if ("ZD02".equals(addr.getId().getAddrType()) && !StringUtils.isEmpty(addr.getCustPhone())) {
              legacyAddr.setAddrPhone("TF" + addr.getCustPhone().trim());
            }
          }
          if (!StringUtils.isBlank(addr.getCity1())) {
            legacyAddr.setCity(addr.getCity1());
          }
          if (!StringUtils.isBlank(addr.getDept())) {
            legacyAddr.setContact(addr.getDept());
          }
          legacyAddr.setUpdateTs(SystemUtil.getCurrentTimestamp());

          if (!StringUtils.isBlank(addr.getPoBox())) {
            legacyAddr.setPoBox("APTO " + addr.getPoBox().trim());
          }

          if (!StringUtils.isBlank(addr.getPostCd())) {
            legacyAddr.setZipCode(addr.getPostCd());
          }
          transformer.transformLegacyAddressData(entityManager, dummyHandler, cust, legacyAddr, cmrObjects, addr);
          capsAndFillNulls(legacyAddr, true);

          if (legacyAddr.isForCreate()) {
            legacyObjects.addAddress(legacyAddr);

            if (!"N".equals(addr.getImportInd())) {
              LOG.debug("Untagging use " + legacyAddr.getAddressUse() + " on sequence " + addr.getId().getAddrSeq());

              modifyAddrUseForSequence(addr.getId().getAddrSeq(), legacyAddr.getAddressUse(), legacyObjects);
            }

            // update the seqno of the original addr record
            updateAddrSeq(entityManager, admin.getId().getReqId(), addr.getId().getAddrType(), addr.getId().getAddrSeq(),
                legacyAddr.getId().getAddrNo() + "", null, legacyAddr.isForSharedSeq());

          }
        } else if ("ZP02".equals(addr.getId().getAddrType())) {
          // Defect 1759962: Spain - address F
          legacyAddr = legacyObjects.findBySeqNo(addr.getId().getAddrSeq());
          if (legacyAddr == null) {
            throw new Exception("Cannot find legacy address with sequence " + addr.getId().getAddrSeq());
          }
          legacyAddr.setForUpdate(true);

          legacyAddr.setAddressUse(transformer.getAddressUse(addr));

          sofKey = transformer.getAddressKey(addr.getId().getAddrType());
          transformer.formatAddressLines(dummyHandler);

          legacyAddr.setAddrLine1(dummyHandler.messageHash.get(sofKey + "Address1"));
          legacyAddr.setAddrLine2(dummyHandler.messageHash.get(sofKey + "Address2"));
          legacyAddr.setAddrLine3(dummyHandler.messageHash.get(sofKey + "Address3"));
          legacyAddr.setAddrLine4(dummyHandler.messageHash.get(sofKey + "Address4"));
          legacyAddr.setAddrLine5(dummyHandler.messageHash.get(sofKey + "Address5"));
          legacyAddr.setAddrLine6(dummyHandler.messageHash.get(sofKey + "Address6"));

          if (!StringUtils.isBlank(addr.getAddrTxt())) {
            legacyAddr.setStreet(addr.getAddrTxt());
          }
          if (!StringUtils.isBlank(addr.getCustPhone())) {
            legacyAddr.setAddrPhone(addr.getCustPhone());
          }
          if (!StringUtils.isBlank(addr.getCity1())) {
            legacyAddr.setCity(addr.getCity1());
          }
          if (!StringUtils.isBlank(addr.getPoBox())) {
            legacyAddr.setPoBox("APTO " + addr.getPoBox().trim());
          }

          if (!StringUtils.isBlank(addr.getPostCd())) {
            legacyAddr.setZipCode(addr.getPostCd());
          }
          transformer.transformLegacyAddressData(entityManager, dummyHandler, cust, legacyAddr, cmrObjects, addr);
          capsAndFillNulls(legacyAddr, true);
          LOG.trace("LegacyAddr No: " + legacyAddr.getId().getAddrNo() + " Addr Seq: " + addr.getId().getAddrType());
        }
        seqNo++;
      }

      // clearAddressUses(entityManager, cntry, cmrNo);
      // parse each address use
      // legacyObjects.getUses().clear();
      for (CmrtAddr currAddr : legacyObjects.getAddresses()) {
        addressUse = currAddr.getAddressUse();
        LOG.trace("Address No: " + currAddr.getId().getAddrNo() + " Uses: " + addressUse);
        if (addressUse != null && !"".equals(addressUse)) {
          for (String use : addressUse.split("")) {
            if (!StringUtils.isEmpty(use)) {

              modifyAddrUseFields(currAddr.getId().getAddrNo(), addressUse, currAddr);
            }
          }
        }
      }

      // Mukesh :Dev for custExt tab
      boolean isCustExt = transformer.hasCmrtCustExt();
      if (isCustExt) {
        CmrtCustExt custExt = legacyObjects.getCustomerExt();
        if (custExt != null) {
          if (transformer != null) {
            transformer.transformLegacyCustomerExtData(entityManager, dummyHandler, custExt, cmrObjects);
          }
          custExt.setUpdateTs(SystemUtil.getCurrentTimestamp());
          custExt.setAeciSubDt(SystemUtil.getDummyDefaultDate());
          legacyObjects.setCustomerExt(custExt);
        } else if (SystemLocation.SLOVAKIA.equals(data.getCmrIssuingCntry()) || SystemLocation.CZECH_REPUBLIC.equals(data.getCmrIssuingCntry())
            || SystemLocation.KENYA.equals(data.getCmrIssuingCntry())) {
          CmrtCustExtPK custExtPk = null;
          LOG.debug("Mapping default Data values with Legacy CmrtCustExt table.....");
          // Initialize the object
          custExt = initEmpty(CmrtCustExt.class);
          // default mapping for ADDR and CMRTCEXT
          custExtPk = new CmrtCustExtPK();
          custExtPk.setCustomerNo(cmrNo);
          custExtPk.setSofCntryCode(cntry);
          custExt.setId(custExtPk);

          if (transformer != null) {
            transformer.transformLegacyCustomerExtData(entityManager, dummyHandler, custExt, cmrObjects);
          }
          custExt.setUpdateTs(SystemUtil.getCurrentTimestamp());
          custExt.setAeciSubDt(SystemUtil.getDummyDefaultDate());
          createEntity(custExt, entityManager);
          legacyObjects.setCustomerExt(custExt);
        } else if (SystemLocation.ROMANIA.equals(data.getCmrIssuingCntry())) {
          CmrtCustExtPK custExtPk = null;
          LOG.debug("Mapping default Data values with Legacy CmrtCustExt table.....");
          // Initialize the object
          custExt = initEmpty(CmrtCustExt.class);
          // default mapping for ADDR and CMRTCEXT
          custExtPk = new CmrtCustExtPK();
          custExtPk.setCustomerNo(cmrNo);
          custExtPk.setSofCntryCode(cntry);
          custExt.setId(custExtPk);

          if (transformer != null) {
            transformer.transformLegacyCustomerExtData(entityManager, dummyHandler, custExt, cmrObjects);
          }
          custExt.setUpdateTs(SystemUtil.getCurrentTimestamp());
          custExt.setAeciSubDt(SystemUtil.getDummyDefaultDate());
          createEntity(custExt, entityManager);
          legacyObjects.setCustomerExt(custExt);
        }
      }
      // rebuild the address use table
      transformer.transformOtherData(entityManager, legacyObjects, cmrObjects);

    }

    return legacyObjects;
  }

  private boolean isAddrPairUpdatedIL(Addr currentAddr, List<Addr> addrList) {
    String[] transAddrTypes = { "CTYA", "CTYB", "CTYC" };
    String[] localAddrTypes = { "ZS01", "ZP01", "ZD01" };
    String pairType = getAddrPairTypeIL(currentAddr.getId().getAddrType());
    String curAddrType = currentAddr.getId().getAddrType();
    String pairedAddrSeq = null;
    String addrSeq = null;
    for (Addr addr : addrList) {
      if (pairType.equals(addr.getId().getAddrType())) {
        if (Arrays.asList(localAddrTypes).contains(curAddrType)) {

          pairedAddrSeq = formatAddrSeqLDStyle(addr.getPairedAddrSeq());
          addrSeq = formatAddrSeqLDStyle(currentAddr.getId().getAddrSeq());

        } else if (Arrays.asList(transAddrTypes).contains(curAddrType)) {

          pairedAddrSeq = formatAddrSeqLDStyle(currentAddr.getPairedAddrSeq());
          addrSeq = formatAddrSeqLDStyle(addr.getId().getAddrSeq());

        }
        if (addrSeq.equals(pairedAddrSeq)) {
          return "Y".equals(addr.getChangedIndc());
        }
      }
    }
    return false;
  }

  private String formatAddrSeqLDStyle(String addrSeq) {
    return addrSeq.replaceFirst("^0*", "");
  }

  private String getAddrPairTypeIL(String addrType) {
    switch (addrType) {
    case "ZS01":
      return "CTYA";
    case "ZP01":
      return "CTYB";
    case "ZD01":
      return "CTYC";
    case "CTYA":
      return "ZS01";
    case "CTYB":
      return "ZP01";
    case "CTYC":
      return "ZD01";
    default:
      return "";
    }
  }

  private void blankOrdBlockFromData(EntityManager entityManager, Data data) {
    data.setOrdBlk("");
    updateEntity(data, entityManager);
    LOG.debug("Blank Ord Block in Data Table :" + data.getOrdBlk());
  }

  private void resetOrdBlockToData(EntityManager entityManager, Data data) {
    data.setOrdBlk("88");
    updateEntity(data, entityManager);
    LOG.debug("Reset Ord Block back to CreateCMR DB :" + data.getOrdBlk());
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
  private synchronized String generateCMRNo(EntityManager entityManager, CMRRequestContainer cmrObjects, String cmrIssuingCntry, String targetCountry)
      throws Exception {
    GenerateCMRNoRequest request = new GenerateCMRNoRequest();
    request.setLoc1(cmrIssuingCntry);
    request.setLoc2(targetCountry);
    request.setMandt(SystemConfiguration.getValue("MANDT"));
    request.setSystem(GenerateCMRNoClient.SYSTEM_SOF);

    if (this.devMode) {
      request.setDirect("DEV");
    } else {
      request.setDirect("Y");
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
      String cmrNo = response.getCmrNo();
      ReservedCMRNos reservedCMRNo = new ReservedCMRNos();
      ReservedCMRNosPK reservedCMRNoPK = new ReservedCMRNosPK();

      reservedCMRNoPK.setCmrIssuingCntry(cmrObjects.getData().getCmrIssuingCntry());
      reservedCMRNoPK.setCmrNo(cmrNo);
      reservedCMRNoPK.setMandt(SystemConfiguration.getValue("MANDT"));
      reservedCMRNo.setId(reservedCMRNoPK);

      reservedCMRNo.setStatus("A");
      reservedCMRNo.setCreateTs(SystemUtil.getCurrentTimestamp());
      reservedCMRNo.setLastUpdtTs(SystemUtil.getCurrentTimestamp());
      reservedCMRNo.setCreateBy(BATCH_USER_ID);
      reservedCMRNo.setLastUpdtBy(BATCH_USER_ID);

      createEntity(reservedCMRNo, entityManager);
      return cmrNo;
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
    if (CmrConstants.REQ_TYPE_DELETE.equals(admin.getReqType()) || CmrConstants.REQ_TYPE_REACTIVATE.equals(admin.getReqType())
        || CmrConstants.REQ_TYPE_SINGLE_REACTIVATE.equals(admin.getReqType())) {
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

    // DTN: 02232019 - I am not sure why we need this so I am commenting.
    // String sqlData = ExternalizedQuery.getSql("LEGACYD.GET.MASS_UPDT");
    // PreparedQuery queryData = new PreparedQuery(entityManager, sqlData);
    // queryData.setForReadOnly(true);
    // queryData.setParameter("REQ_ID", massUpdt.getId().getParReqId());
    // queryData.setParameter("ITER_ID", massUpdt.getId().getIterationId());
    // List<MassUpdt> dataList = queryData.getResults(MassUpdt.class);
    //
    // if (dataList != null) {
    // for (MassUpdt muSingleData : dataList) {
    // container.addMassUpdate(muSingleData);
    // }
    // }

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
    return prepareRequest(entityManager, admin, false);
  }

  /**
   * Retrieves the {@link Admin}, {@link Data}, and {@link Addr} records based
   * on the request
   * 
   * @param cmmaMgr
   * @param reqId
   * @param toRdc
   * @return
   * @throws Exception
   */
  private CMRRequestContainer prepareRequest(EntityManager entityManager, Admin admin, boolean toRdc) throws Exception {
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
        // CREATCMR-5865 add PG01 globally as supported
        if (toRdc) {
          sql += " and ADDR_TYPE in ( " + types.toString() + ", 'PG01') ";
        } else {
          sql += " and ADDR_TYPE in ( " + types.toString() + ") ";
        }
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
    String sql = "";

    if (SystemLocation.ITALY.equals(country)) {
      sql = ExternalizedQuery.getSql("LEGACYD.SEQNOTABLE.IT");
    } else {
      sql = ExternalizedQuery.getSql("LEGACYD.SEQNOTABLE");
    }

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
   * Clears the current {@link CmrtAddrUse} records for rebuilding
   * 
   * @param entityManager
   * @param country
   * @param cmrNo
   */
  /*
   * private void clearAddressUses(EntityManager entityManager, String country,
   * String cmrNo) { LOG.debug("CLearing the Address Use table for " + country +
   * " - " + cmrNo); String sql =
   * ExternalizedQuery.getSql("LEGACYD.CLEAR_USES"); PreparedQuery query = new
   * PreparedQuery(entityManager, sql); query.setParameter("COUNTRY", country);
   * query.setParameter("CMR_NO", cmrNo); query.executeSql();
   * 
   * }
   */

  /**
   * Manually updates the sequence number of the given addr
   * 
   * @param entityManager
   * @param reqId
   * @param addrType
   * @param oldSeq
   * @param newSeq
   */
  public void updateAddrSeq(EntityManager entityManager, long reqId, String addrType, String oldSeq, String newSeq, String kunnr, boolean sharedSeq) {
    String updateSeq = ExternalizedQuery.getSql("LEGACYD.UPDATE_ADDR_SEQ");
    PreparedQuery q = new PreparedQuery(entityManager, updateSeq);
    q.setParameter("NEW_SEQ", newSeq);
    q.setParameter("REQ_ID", reqId);
    q.setParameter("TYPE", addrType);
    q.setParameter("OLD_SEQ", oldSeq);
    q.setParameter("SAP_NO", kunnr);
    LOG.debug("Assigning address sequence " + newSeq + " to " + addrType + " address.");
    q.executeSql();

    if (!sharedSeq) {
      updateSeq = ExternalizedQuery.getSql("LEGACYD.UPDATE_ADDR_SEQ_RDC");
      q = new PreparedQuery(entityManager, updateSeq);
      q.setParameter("NEW_SEQ", newSeq);
      q.setParameter("REQ_ID", reqId);
      q.setParameter("TYPE", addrType);
      q.setParameter("OLD_SEQ", oldSeq);
      q.setParameter("SAP_NO", kunnr);
      q.executeSql();
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

  @Override
  protected void handleMailsForResult(EntityManager entityManager, String resultCode, Admin admin, String comment) throws CmrException, SQLException {
    if (!CmrConstants.RDC_STATUS_ABORTED.equals(admin.getRdcProcessingStatus())
        && !CmrConstants.RDC_STATUS_NOT_COMPLETED.equals(admin.getRdcProcessingStatus())) {
      // if not aborted and not completed rdc processing status, complete the
      // request
      admin.setReqStatus("COM");
      admin.setProcessedFlag("Y");
      updateEntity(admin, entityManager);
    } else if (CmrConstants.RDC_STATUS_ABORTED.equals(admin.getRdcProcessingStatus())
        || CmrConstants.RDC_STATUS_NOT_COMPLETED.equals(admin.getRdcProcessingStatus())) {
      admin.setReqStatus("PPN");
      admin.setProcessedFlag("E");
      updateEntity(admin, entityManager);
    }
    if (!CmrConstants.RDC_STATUS_IGNORED.equals(resultCode)) {
      RequestUtils.createWorkflowHistoryFromBatch(entityManager, BATCH_USER_ID, admin, comment,
          "C".equals(admin.getReqType()) ? ACTION_RDC_CREATE : ACTION_RDC_UPDATE, null, null, true, true);
    }
  }

  protected void processMassUpdateRequest(EntityManager entityManager, ProcessRequest request, Admin admin, Data data) throws Exception {

    String resultCode = null;
    String processingStatus = admin.getRdcProcessingStatus() != null ? admin.getRdcProcessingStatus() : "";
    long reqId = admin.getId().getReqId();
    boolean isIndexNotUpdated = false;

    try {
      // 1. Get request to process
      PreparedQuery query = new PreparedQuery(entityManager, ExternalizedQuery.getSql("BATCH.LD.GET.MASS_UPDT"));
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
          if (!CmrConstants.REQUEST_STATUS.PCO.toString().equals(admin.getReqStatus())) {
            admin.setReqStatus(CmrConstants.REQUEST_STATUS.PCO.toString());
            updateEntity(admin, entityManager);
            partialCommit(entityManager);
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
              this.serviceClient.setReadTimeout(60 * 60 * 1000); // 60 mins
              response = this.serviceClient.executeAndWrap(applicationId, request, ProcessResponse.class);

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
          admin.setProcessedFlag("E");
        } else if (statusCodes.contains(CmrConstants.RDC_STATUS_ABORTED)) {
          admin.setReqStatus(CmrConstants.REQUEST_STATUS.PPN.toString());
          admin.setProcessedFlag("E");
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
          admin.setProcessedFlag("Y");
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

  @Override
  protected void processUpdateRequest(EntityManager entityManager, ProcessRequest request, Admin admin, Data data) throws Exception {

    String resultCode = null;
    String processingStatus = admin.getRdcProcessingStatus() != null ? admin.getRdcProcessingStatus() : "";
    long reqId = admin.getId().getReqId();
    // String rdcEmbargoCd = getEmbargoCdFromDataRdc(entityManager, admin);
    DataRdc dataRdc = getDataRdcRecords(entityManager, data);
    CMRRequestContainer cmrObjects = prepareRequest(entityManager, admin, true);
    if ((admin.getReqReason() != null && !StringUtils.isBlank(admin.getReqReason()))
        && CMR_REQUEST_REASON_TEMP_REACT_EMBARGO.equals(admin.getReqReason())
        && (dataRdc.getEmbargoCd() != null && !StringUtils.isBlank(dataRdc.getEmbargoCd())) && EMBARGO_LIST.contains(dataRdc.getEmbargoCd())
        && (data.getEmbargoCd() == null || StringUtils.isBlank(data.getEmbargoCd())) && admin.getReqStatus() != null
        && CMR_REQUEST_STATUS_CPR.equals(admin.getReqStatus())) {

      if (admin.getRdcProcessingTs() == null || isTimeStampEquals(admin.getRdcProcessingTs())) {
        LOG.info("Temporary Reactivate Embargo process: Batch 1st run for Req Id :" + admin.getId().getReqId());
        try {

          List<Addr> addresses = cmrObjects.getAddresses();

          List<String> statusCodes = new ArrayList<String>();

          StringBuilder comment = new StringBuilder();

          Set<String> usedSequences = new HashSet<String>();
          ProcessResponse response = null;
          String applicationId = BatchUtil.getAppId(data.getCmrIssuingCntry());

          boolean isDataUpdated = false;
          isDataUpdated = LegacyDirectUtil.isDataUpdated(data, dataRdc, data.getCmrIssuingCntry());
          MessageTransformer transformer = TransformerManager.getTransformer(data.getCmrIssuingCntry());

          if (SystemLocation.TURKEY.equals(data.getCmrIssuingCntry()) && !isDataUpdated) {
            for (Addr addr : addresses) {
              if ("ZS01".equals(addr.getId().getAddrType())) {
                AddrRdc addrRdc = getAddrRdcRecord(entityManager, addr);
                if (addrRdc == null || (addrRdc != null && !addr.getCustPhone().equals(addrRdc.getCustPhone()))) {
                  isDataUpdated = true;
                }
              }
            }
          }

          if (transformer.isUpdateNeededOnAllAddressType(entityManager, cmrObjects)) {
            isDataUpdated = true;
          }

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
                this.serviceClient.setReadTimeout(60 * 10 * 1000); // 10 mins
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
            }

            usedSequences.add(addr.getId().getAddrSeq());
            keepAlive();

          }
          comment = comment.append("\nTemporary Reactivation - Embargo removal process done in RDc.");

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

          String rdcProcessingMsg = null;
          if ("N".equals(admin.getRdcProcessingStatus()) || "A".equals(admin.getRdcProcessingStatus())) {
            rdcProcessingMsg = "Some errors occurred during processing. Please check request's comment log for details.";
          } else {
            rdcProcessingMsg = "RDc Processing has been completed 1st time for Temporary Reactivate Embargo Code. Please check request's comment log for details.";
          }

          admin.setRdcProcessingTs(SystemUtil.getCurrentTimestamp());
          admin.setRdcProcessingMsg(rdcProcessingMsg.toString().trim());
          if (!"A".equals(admin.getRdcProcessingStatus()) && !"N".equals(admin.getRdcProcessingStatus())) {
            admin.setReqStatus("CPR");
          } else if ("A".equals(admin.getRdcProcessingStatus()) || "N".equals(admin.getRdcProcessingStatus())) {
            admin.setReqStatus("PPN");
            admin.setProcessedFlag("E");
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
                "RDc  Processing has been completed. Please check request's comment log for details.", ACTION_RDC_UPDATE, null, null,
                "CPR".equals(admin.getReqStatus()));
          }

          partialCommit(entityManager);
          LOG.debug(
              "Request ID " + admin.getId().getReqId() + " Status: " + admin.getRdcProcessingStatus() + " Message: " + admin.getRdcProcessingMsg());

        } catch (Exception e) {
          LOG.error("Error in processing Update Request " + admin.getId().getReqId(), e);
          addError("Update Request " + admin.getId().getReqId() + " Error: " + e.getMessage());
        }
        LOG.info("End 1st time run Temporary Reactivate Embargo process...");
      } else {

        int noOFWorkingDays = 0;
        if (admin.getReqStatus() != null && admin.getReqStatus().equals(CMR_REQUEST_STATUS_CPR)) {
          noOFWorkingDays = checked2WorkingDays(admin.getProcessedTs(), SystemUtil.getCurrentTimestamp());
        }
        if (noOFWorkingDays >= 3) {
          LOG.info("RDc: Temporary Reactivate Embargo process: run after 2 working days for Req Id :" + admin.getId().getReqId());
          try {
            admin.setProcessedTs(SystemUtil.getCurrentTimestamp());
            List<Addr> addresses = cmrObjects.getAddresses();

            List<String> statusCodes = new ArrayList<String>();

            StringBuilder comment = new StringBuilder();

            Set<String> usedSequences = new HashSet<String>();
            ProcessResponse response = null;
            String applicationId = BatchUtil.getAppId(data.getCmrIssuingCntry());

            // boolean isDataUpdated = false;
            // isDataUpdated = LegacyDirectUtil.isDataUpdated(data, dataRdc,
            // data.getCmrIssuingCntry());

            for (Addr addr : addresses) {
              entityManager.detach(addr);

              /*
               * if (!"ZS01".equals(addr.getId().getAddrType())) {
               * LOG.warn("Address Type: " + addr.getId().getAddrType() +
               * "  Skipping all address except ZS01 address."); continue; }
               */

              if (usedSequences.contains(addr.getId().getAddrSeq())) {
                LOG.warn("Sequence " + addr.getId().getAddrSeq() + " already sent in a previous request. Skipping.");
                continue;
              }
              if (StringUtils.isEmpty(addr.getSapNo())) {
                LOG.warn("Address Type: " + addr.getId().getAddrType() + "  Skipping as SAP no is blank.");
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
                  this.serviceClient.setReadTimeout(60 * 10 * 1000); // 10 mins
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

            comment = comment.append("\nTemporary Reactivation - Embargo reversal process done in RDc");

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

            String rdcProcessingMsg = null;
            if ("N".equals(admin.getRdcProcessingStatus()) || "A".equals(admin.getRdcProcessingStatus())) {
              rdcProcessingMsg = "Some errors occurred during processing. Please check request's comment log for details.";
            } else {
              rdcProcessingMsg = "RDc Processing has been completed at 2nd time for Temporary Reactivate Embargo Code. Please check request's comment log for details.";
            }

            admin.setRdcProcessingTs(SystemUtil.getCurrentTimestamp());
            admin.setRdcProcessingMsg(rdcProcessingMsg.toString().trim());
            if (!"A".equals(admin.getRdcProcessingStatus()) && !"N".equals(admin.getRdcProcessingStatus())) {
              admin.setReqStatus("COM");
              admin.setProcessedFlag("Y");
            } else if ("A".equals(admin.getRdcProcessingStatus()) || "N".equals(admin.getRdcProcessingStatus())) {
              admin.setReqStatus("PPN");
              admin.setProcessedFlag("E");
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
          LOG.info("End 2nd time run Temporary Reactivate Embargo process...");
        }
      }

    } else { // Normal Update Process
      try {

        List<Addr> addresses = cmrObjects.getAddresses();

        List<String> statusCodes = new ArrayList<String>();

        StringBuilder comment = new StringBuilder();

        Set<String> usedSequences = new HashSet<String>();
        ProcessResponse response = null;
        String applicationId = BatchUtil.getAppId(data.getCmrIssuingCntry());

        boolean isDataUpdated = false;
        boolean isExceptionOccured = false;
        Set<String> rdcProcessedSequences = new HashSet<String>();
        isDataUpdated = LegacyDirectUtil.isDataUpdated(data, dataRdc, data.getCmrIssuingCntry());
        MessageTransformer transformer = TransformerManager.getTransformer(data.getCmrIssuingCntry());

        if (SystemLocation.TURKEY.equals(data.getCmrIssuingCntry()) && !isDataUpdated) {
          for (Addr addr : addresses) {
            if ("ZS01".equals(addr.getId().getAddrType())) {
              AddrRdc addrRdc = getAddrRdcRecord(entityManager, addr);
              if (addrRdc == null || (addrRdc != null && StringUtils.isBlank(addr.getCustPhone()) && !StringUtils.isBlank(addrRdc.getCustPhone()))
                  || (addrRdc != null && addr.getCustPhone() != null && !addr.getCustPhone().equals(addrRdc.getCustPhone()))) {
                isDataUpdated = true;
              }
            }
          }
        }

        if (transformer.isUpdateNeededOnAllAddressType(entityManager, cmrObjects)) {
          isDataUpdated = true;
        }

        for (Addr addr : addresses) {
          entityManager.detach(addr);
          if (usedSequences.contains(addr.getId().getAddrSeq())) {
            LOG.warn("Sequence " + addr.getId().getAddrSeq() + " already sent in a previous request. Skipping.");
            continue;
          }

          if (isExceptionOccured && rdcProcessedSequences.contains(addr.getId().getAddrSeq())) {
            LOG.warn("New kunnr for Sequence " + addr.getId().getAddrSeq() + " already created in RDc. Skipping.");
            continue;
          }

          if (!isDataUpdated && "Y".equals(addr.getImportInd()) && !"Y".equals(addr.getChangedIndc())) {
            LOG.warn("Sequence " + addr.getId().getAddrSeq() + " address not updated. Skipping.");
            continue;
          }

          if (transformer.skipLegacyAddressData(entityManager, cmrObjects, addr, false)) {
            LOG.debug("Skipping RDC update for address :" + addr.getId().getAddrType());
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
              this.serviceClient.setReadTimeout(60 * 20 * 1000); // 20 mins
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

          AddrPK dummyId = new AddrPK();
          dummyId.setReqId(addr.getId().getReqId());
          dummyId.setAddrSeq(addr.getId().getAddrSeq());
          dummyId.setAddrType(addr.getId().getAddrType());
          Addr addrNew = entityManager.find(Addr.class, dummyId);

          LOG.info("Response received from Process Service [Request ID: " + response.getReqId() + " CMR No: " + response.getCmrNo() + " KUNNR: "
              + addrNew.getSapNo() + " Status: " + response.getStatus() + " Message: " + (response.getMessage() != null ? response.getMessage() : "-")
              + "]");

          // get the results from the service and process jason response

          // create comment log and workflow history entries for update type of
          // request
          if (isCompletedSuccessfully(resultCode)) {

            addrNew.setRdcLastUpdtDt(SystemUtil.getCurrentTimestamp());
            if (response.getRecords() != null) {
              for (RDcRecord rdcRec : response.getRecords()) {
                if (rdcRec.getAddressType().equals(addrNew.getId().getAddrType()) && rdcRec.getSeqNo().equals(addrNew.getId().getAddrSeq())
                    && StringUtils.isEmpty(addrNew.getSapNo())) {
                  LOG.debug("Updating SAP No. to " + rdcRec.getSapNo() + " for Type " + rdcRec.getAddressType() + "/" + rdcRec.getSeqNo());
                  addrNew.setSapNo(rdcRec.getSapNo());
                  addrNew.setIerpSitePrtyId(rdcRec.getIerpSitePartyId());
                  if (SystemLocation.ISRAEL.equals(data.getCmrIssuingCntry())
                      && ("CTYB".equals(addrNew.getId().getAddrType()) || "CTYC".equals(addrNew.getId().getAddrType()))) {
                    LegacyDirectObjectContainer legacyObjects = LegacyDirectUtil.getLegacyDBValues(entityManager, data.getCmrIssuingCntry(),
                        response.getCmrNo(), false, false);
                    CmrtAddr legacyAddr = legacyObjects.findBySeqNo(addrNew.getId().getAddrSeq());
                    addrNew.setPairedAddrSeq(legacyAddr.getAddrLineO());
                  }
                }
              }
            }

            try {
              updateEntity(addrNew, entityManager);
            } catch (Exception e) {
              // try {
              LOG.info("Exception in LegacyDirect due to optimistic lock for KUNNR=" + addrNew.getSapNo() + "===" + addr.getSapNo());
              if (addr.getSapNo() == null && addrNew.getSapNo() != null) {
                LOG.info("Kunnr created for new address in RDc with Seq no=" + addrNew.getId().getAddrSeq());
                isExceptionOccured = true;
                rdcProcessedSequences.add(addrNew.getId().getAddrSeq());
              }

              // Thread.sleep(60 * 1000);// 1 minute sleep time
              // } catch (InterruptedException ex) {
              // LOG.debug("Exception in LegacyDirect sleep method for KUNNR=" +
              // addrNew.getSapNo());
              // }
              // updateEntity(addrNew, entityManager);
            }
            // updateEntity(addrNew, entityManager);

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
              comment = comment.append("\nUpdate processing for " + (addrNew.getId().getAddrType() + "/" + addrNew.getId().getAddrSeq())
                  + " in RDc skipped: " + response.getMessage());
            }
          }

          usedSequences.add(addrNew.getId().getAddrSeq());

        }

        if (!statusCodes.contains(CmrConstants.RDC_STATUS_NOT_COMPLETED) && !statusCodes.contains(CmrConstants.RDC_STATUS_ABORTED)) {
          // now process the partner functions
          LOG.info("Processing partner functions for Request " + admin.getId().getReqId());
          request.setReqType(ProcessRequest.TYPE_KNVP);
          request.setAddrType("ZS01");
          request.setSapNo(null);
          request.setCmrNo(data.getCmrNo());

          if (LOG.isTraceEnabled()) {
            LOG.trace("Request JSON:");
            DebugUtil.printObjectAsJson(LOG, request);
          }

          try {
            this.serviceClient.setReadTimeout(60 * 10 * 1000); // 10 mins
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
          if (LOG.isTraceEnabled()) {
            LOG.trace("Response JSON:");
            DebugUtil.printObjectAsJson(LOG, response);
          }

          statusCodes.add(response.getStatus());
          comment = comment.append(
              "\nPartner functions " + (isCompletedSuccessfully(response.getStatus()) ? "processed successfully." : "not successfully processed."));
        }

        RequestUtils.createCommentLogFromBatch(entityManager, BATCH_USER_ID, reqId, comment.toString().trim());

        LOG.debug("Updating Admin record for Request ID " + admin.getId().getReqId());

        // only update Admin record once depending on the overall status of the
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
          admin.setProcessedFlag("Y");
        } else if ("A".equals(admin.getRdcProcessingStatus()) || "N".equals(admin.getRdcProcessingStatus())) {
          admin.setReqStatus("PPN");
          admin.setProcessedFlag("E");
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

        // email Notify
        String mailFlag = transformer.getMailSendingFlag(cmrObjects.getData(), admin, entityManager);
        if (!"NA".equals(mailFlag)) {
          String mailTemplate = transformer.getEmailTemplateName(mailFlag);
          String statusForMailSend = transformer.getReqStatusForSendingMail(mailFlag);
          if (statusForMailSend != null && "COM".equals(statusForMailSend)) {
            BatchEmailModel mailParams = transformer.getMailFormatParams(entityManager, cmrObjects, mailFlag);
            LegacyCommonUtil.sendfieldUpdateEmailNotification(entityManager, mailParams, mailTemplate);
          }
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

  public void processMassReactDelChanges(EntityManager entityManager, Admin admin, Data data)
      throws JsonGenerationException, JsonMappingException, IOException, Exception {
    // Create the request

    long reqId = admin.getId().getReqId();
    long iterationId = admin.getIterationId();
    String processingStatus = admin.getRdcProcessingStatus();

    MassUpdateServiceInput input = prepareMassChangeInput(entityManager, admin);

    MassProcessRequest request = new MassProcessRequest();
    // set the update mass record in request
    if (input.getInputReqType() != null && (input.getInputReqType().equalsIgnoreCase("D") || input.getInputReqType().equalsIgnoreCase("R"))) {
      request = prepareReactDelReq(entityManager, admin, data, input);
    }

    // call the Mass service
    LOG.info("Sending request to Mass Service [Request ID: " + request.getReqId() + "  Type: " + request.getReqType() + "]");

    LOG.trace("Request JSON:");
    if (LOG.isTraceEnabled()) {
      DebugUtil.printObjectAsJson(LOG, request);
    }
    // actual service call here
    ProcessResponse response = null;
    String applicationId = BatchUtil.getAppId(data.getCmrIssuingCntry());
    if (applicationId == null) {
      LOG.debug("No Application ID mapped to " + data.getCmrIssuingCntry());
      response = new ProcessResponse();
      response.setReqId(request.getReqId());
      response.setMandt(request.getMandt());
      response.setStatus(CmrConstants.RDC_STATUS_NOT_COMPLETED);
      response.setMessage("No application ID defined for Country: " + data.getCmrIssuingCntry() + ". Cannot process RDc records.");
    } else {
      try {

        this.serviceClient.setReadTimeout(60 * 15 * 1000); // 15 mins
        response = this.serviceClient.executeAndWrap(applicationId, request, ProcessResponse.class);
      } catch (Exception e) {
        LOG.error("Error when connecting to the service.", e);
        response = new ProcessResponse();
        response.setReqId(request.getReqId());
        response.setStatus(CmrConstants.RDC_STATUS_ABORTED);
        response.setMessage("A system error has occured. Setting to aborted.");
      }
    }

    LOG.trace("Response JSON:");
    if (LOG.isTraceEnabled()) {
      DebugUtil.printObjectAsJson(LOG, response);
    }
    LOG.info("Response received from Process Service [Request ID: " + response.getReqId() + " Status: " + response.getStatus() + " Message: "
        + (response.getMessage() != null ? response.getMessage() : "-") + "]");

    if (response.getReqId() <= 0) {
      response.setReqId(request.getReqId());
    }

    // try this
    // get the results from the service and process jason response
    try {
      // update MASS_UPDT table with the error txt and row status cd
      for (RDcRecord record : response.getRecords()) {
        PreparedQuery updtQuery = new PreparedQuery(entityManager, ExternalizedQuery.getSql("BATCH.GET_MASS_UPDT_ENTITY"));
        updtQuery.setParameter("REQ_ID", reqId);
        updtQuery.setParameter("ITERATION_ID", iterationId);
        updtQuery.setParameter("CMR_NO", record.getCmrNo());
        List<MassUpdt> updateList = updtQuery.getResults(MassUpdt.class);

        for (MassUpdt massUpdt : updateList) {
          LegacyDirectObjectContainer legacyObjects = LegacyDirectUtil.getLegacyDBValues(entityManager, data.getCmrIssuingCntry(),
              massUpdt.getCmrNo(), false, false);
          CmrtCust cust = legacyObjects.getCustomer();

          massUpdt.setErrorTxt(record.getMessage());
          if (CmrConstants.RDC_STATUS_NOT_COMPLETED.equals(record.getStatus())) {
            massUpdt.setRowStatusCd("RDCER");
          } else if (CmrConstants.RDC_STATUS_COMPLETED.equals(record.getStatus())) {
            massUpdt.setRowStatusCd("DONE");
            if (cust != null) {
              LOG.info(" - completed Reactivate/Delete SOF Country: " + cust.getId().getSofCntryCode() + " CMR No.: " + massUpdt.getCmrNo());
              LOG.debug("completed Setting Status=A or C based on CMR.." + massUpdt.getCmrNo());
              if ("R".equals(admin.getReqType()))
                cust.setStatus(LEGACY_STATUS_ACTIVE);
              else if ("D".equals(admin.getReqType()))
                cust.setStatus(LEGACY_STATUS_CANCELLED);

              cust.setUpdateTs(SystemUtil.getCurrentTimestamp());
              cust.setUpdStatusTs(SystemUtil.getCurrentTimestamp());
            }
          } else if (CmrConstants.RDC_STATUS_COMPLETED_WITH_WARNINGS.equals(record.getStatus())) {
            if (cust != null) {
              LOG.info(" - Warning Reactivate/Delete SOF Country: " + cust.getId().getSofCntryCode() + " CMR No.: " + massUpdt.getCmrNo());
              LOG.debug("Warning Setting Status=A or C based on CMR.." + massUpdt.getCmrNo());
              if ("R".equals(admin.getReqType()))
                cust.setStatus(LEGACY_STATUS_ACTIVE);
              else if ("D".equals(admin.getReqType()))
                cust.setStatus(LEGACY_STATUS_CANCELLED);

              cust.setUpdateTs(SystemUtil.getCurrentTimestamp());
              cust.setUpdStatusTs(SystemUtil.getCurrentTimestamp());
            }
          }
          LOG.info("Mass Update Record Updated [Request ID: " + massUpdt.getId().getParReqId() + " CMR_NO: " + massUpdt.getCmrNo() + " SEQ No: "
              + massUpdt.getId().getSeqNo() + "]");
          updateEntity(massUpdt, entityManager);
          updateEntity(cust, entityManager);
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
      case "M":
        action = ACTION_RDC_MASS_UPDATE;
        requestType = "Mass Update";
        break;
      }

      // create comment log and workflow history entries for update type of
      // request
      StringBuilder comment = new StringBuilder();
      if (isCompletedSuccessfully(resultCode)) {
        comment = comment.append(requestType + " in RDc successfully completed.");
      } else {
        if (CmrConstants.RDC_STATUS_ABORTED.equals(resultCode) && CmrConstants.RDC_STATUS_ABORTED.equals(processingStatus)) {
          comment = comment.append(requestType + " in RDc failed: " + response.getMessage());
        } else if (CmrConstants.RDC_STATUS_ABORTED.equalsIgnoreCase(resultCode)) {
          comment = comment.append(requestType + " in RDc aborted: " + response.getMessage() + "\n System will retry once.");
        } else if (CmrConstants.RDC_STATUS_NOT_COMPLETED.equalsIgnoreCase(resultCode)) {
          comment = comment.append(requestType + " in RDc failed: " + response.getMessage());
        } else if (CmrConstants.RDC_STATUS_IGNORED.equalsIgnoreCase(resultCode)) {
          comment = comment.append(requestType + " in RDc skipped: " + response.getMessage());
        }
      }

      RequestUtils.createCommentLogFromBatch(entityManager, BATCH_USER_ID, admin.getId().getReqId(), comment.toString().trim());

      // only update Admin record once depending on the overall status of the
      // request
      LOG.debug("Updating Admin record for Request ID " + admin.getId().getReqId());
      if (CmrConstants.RDC_STATUS_ABORTED.equals(resultCode) && CmrConstants.RDC_STATUS_ABORTED.equals(admin.getRdcProcessingStatus())) {
        admin.setRdcProcessingStatus(CmrConstants.RDC_STATUS_NOT_COMPLETED);
      } else {
        admin.setRdcProcessingStatus(resultCode);
      }
      admin.setRdcProcessingTs(SystemUtil.getCurrentTimestamp());
      admin.setRdcProcessingMsg(response.getMessage());
      if (!"A".equals(admin.getRdcProcessingStatus()) && !"N".equals(admin.getRdcProcessingStatus())) {
        admin.setReqStatus("COM");
        admin.setProcessedFlag("Y");
      } else if ("A".equals(admin.getRdcProcessingStatus()) || "N".equals(admin.getRdcProcessingStatus())) {
        admin.setReqStatus("PPN");
        admin.setProcessedFlag("E");
      }

      updateEntity(admin, entityManager);
      if ("N".equals(admin.getRdcProcessingStatus()) || "A".equals(admin.getRdcProcessingStatus())) {
        RequestUtils.createWorkflowHistoryFromBatch(entityManager, BATCH_USER_ID, admin,
            "Some errors occurred during RDc processing. Please check request's comment log for details.", action, null, null,
            "COM".equals(admin.getReqStatus()));
      } else {
        RequestUtils.createWorkflowHistoryFromBatch(entityManager, BATCH_USER_ID, admin, comment.toString().trim(), action, null, null,
            "COM".equals(admin.getReqStatus()));
      }
      LOG.debug("Request ID " + admin.getId().getReqId() + " Status: " + admin.getRdcProcessingStatus() + " Message: " + admin.getRdcProcessingMsg());

      partialCommit(entityManager);

    } catch (Exception e) {
      LOG.error("Error in processing Mass Request  " + admin.getId().getReqId(), e);
      addError("Mass Request " + admin.getId().getReqId() + " Error: " + e.getMessage());
    }
  }

  private void processSingleReactivateRequest(EntityManager entityManager, ProcessRequest request, Admin admin, Data data) throws Exception {
    // Create the request
    long reqId = admin.getId().getReqId();
    long iterationId = admin.getIterationId();
    String processingStatus = admin.getRdcProcessingStatus();

    MassUpdateServiceInput input = prepareMassChangeInput(entityManager, admin);

    MassProcessRequest singleRequest = new MassProcessRequest();

    if (input.getInputReqType() != null && (input.getInputReqType().equalsIgnoreCase("X"))) {
      processUpdateRequest(entityManager, request, admin, data);
    }

    // set the update mass record in request
    if (input.getInputReqType() != null && (input.getInputReqType().equalsIgnoreCase("X"))) {
      singleRequest = prepareReactDelReq(entityManager, admin, data, input);
    }

    // call the Mass service
    LOG.info("Sending request to Mass Service [Request ID: " + request.getReqId() + "  Type: " + request.getReqType() + "]");

    LOG.trace("Request JSON:");
    if (LOG.isTraceEnabled()) {
      DebugUtil.printObjectAsJson(LOG, request);
    }
    // actual service call here
    ProcessResponse response = null;
    String applicationId = BatchUtil.getAppId(data.getCmrIssuingCntry());
    if (applicationId == null) {
      LOG.debug("No Application ID mapped to " + data.getCmrIssuingCntry());
      response = new ProcessResponse();
      response.setReqId(request.getReqId());
      response.setMandt(request.getMandt());
      response.setStatus(CmrConstants.RDC_STATUS_NOT_COMPLETED);
      response.setMessage("No application ID defined for Country: " + data.getCmrIssuingCntry() + ". Cannot process RDc records.");
    } else {
      try {
        this.serviceClient.setReadTimeout(60 * 15 * 1000); // 15 mins
        response = this.serviceClient.executeAndWrap(applicationId, request, ProcessResponse.class);
      } catch (Exception e) {
        LOG.error("Error when connecting to the service.", e);
        response = new ProcessResponse();
        response.setReqId(request.getReqId());
        response.setStatus(CmrConstants.RDC_STATUS_ABORTED);
        response.setMessage("A system error has occured. Setting to aborted.");
      }
    }

    LOG.trace("Response JSON:");
    if (LOG.isTraceEnabled()) {
      DebugUtil.printObjectAsJson(LOG, response);
    }
    LOG.info("Response received from Process Service [Request ID: " + response.getReqId() + " Status: " + response.getStatus() + " Message: "
        + (response.getMessage() != null ? response.getMessage() : "-") + "]");

    if (response.getReqId() <= 0) {
      response.setReqId(request.getReqId());
    }

    // try this get the results from the service and process jason response
    try {
      // update MASS_UPDT table with the error txt and row status cd
      for (RDcRecord record : response.getRecords()) {
        PreparedQuery updtQuery = new PreparedQuery(entityManager, ExternalizedQuery.getSql("BATCH.GET_MASS_UPDT_ENTITY"));
        updtQuery.setParameter("REQ_ID", reqId);
        updtQuery.setParameter("ITERATION_ID", iterationId);
        updtQuery.setParameter("CMR_NO", record.getCmrNo());
        List<MassUpdt> updateList = updtQuery.getResults(MassUpdt.class);

        for (MassUpdt massUpdt : updateList) {
          LegacyDirectObjectContainer legacyObjects = LegacyDirectUtil.getLegacyDBValues(entityManager, data.getCmrIssuingCntry(),
              massUpdt.getCmrNo(), false, false);
          CmrtCust cust = legacyObjects.getCustomer();

          massUpdt.setErrorTxt(record.getMessage());
          if (CmrConstants.RDC_STATUS_NOT_COMPLETED.equals(record.getStatus())) {
            massUpdt.setRowStatusCd("RDCER");
          } else if (CmrConstants.RDC_STATUS_COMPLETED.equals(record.getStatus())) {
            massUpdt.setRowStatusCd("DONE");
            if (cust != null) {
              LOG.info(" - completed Single Reactivate SOF Country: " + cust.getId().getSofCntryCode() + " CMR No.: " + massUpdt.getCmrNo());
              LOG.debug("completed Setting Status=A or C based on CMR.." + massUpdt.getCmrNo());
              if ("X".equals(admin.getReqType()))
                cust.setStatus(LEGACY_STATUS_ACTIVE);
              cust.setUpdateTs(SystemUtil.getCurrentTimestamp());
            }
          } else if (CmrConstants.RDC_STATUS_COMPLETED_WITH_WARNINGS.equals(record.getStatus())) {
            if (cust != null) {
              LOG.info(" - Warning Single Reactivate SOF Country: " + cust.getId().getSofCntryCode() + " CMR No.: " + massUpdt.getCmrNo());
              LOG.debug("Warning Setting Status=A or C based on CMR.." + massUpdt.getCmrNo());
              if ("X".equals(admin.getReqType()))
                cust.setStatus(LEGACY_STATUS_ACTIVE);
              cust.setUpdateTs(SystemUtil.getCurrentTimestamp());
            }
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
      case "X":
        action = ACTION_RDC_SINGLE_REACTIVATE;
        requestType = "Single Reactivate";
        break;
      case "M":
        action = ACTION_RDC_MASS_UPDATE;
        requestType = "Mass Update";
        break;
      }

      // create comment log and workflow history entries for update type of
      // request
      StringBuilder comment = new StringBuilder();
      if (isCompletedSuccessfully(resultCode)) {
        comment = comment.append(requestType + " in RDc successfully completed.");
      } else {
        if (CmrConstants.RDC_STATUS_ABORTED.equals(resultCode) && CmrConstants.RDC_STATUS_ABORTED.equals(processingStatus)) {
          comment = comment.append(requestType + " in RDc failed: " + response.getMessage());
        } else if (CmrConstants.RDC_STATUS_ABORTED.equalsIgnoreCase(resultCode)) {
          comment = comment.append(requestType + " in RDc aborted: " + response.getMessage() + "\n System will retry once.");
        } else if (CmrConstants.RDC_STATUS_NOT_COMPLETED.equalsIgnoreCase(resultCode)) {
          comment = comment.append(requestType + " in RDc failed: " + response.getMessage());
        } else if (CmrConstants.RDC_STATUS_IGNORED.equalsIgnoreCase(resultCode)) {
          comment = comment.append(requestType + " in RDc skipped: " + response.getMessage());
        }
      }

      RequestUtils.createCommentLogFromBatch(entityManager, BATCH_USER_ID, admin.getId().getReqId(), comment.toString().trim());

      // only update Admin record once depending on the overall status of the
      // request
      LOG.debug("Updating Admin record for Request ID " + admin.getId().getReqId());
      if (CmrConstants.RDC_STATUS_ABORTED.equals(resultCode) && CmrConstants.RDC_STATUS_ABORTED.equals(admin.getRdcProcessingStatus())) {
        admin.setRdcProcessingStatus(CmrConstants.RDC_STATUS_NOT_COMPLETED);
      } else {
        admin.setRdcProcessingStatus(resultCode);
      }
      admin.setRdcProcessingTs(SystemUtil.getCurrentTimestamp());
      admin.setRdcProcessingMsg(response.getMessage());
      if (!"A".equals(admin.getRdcProcessingStatus()) && !"N".equals(admin.getRdcProcessingStatus())) {
        admin.setReqStatus("COM");
        admin.setProcessedFlag("Y");
      } else if ("A".equals(admin.getRdcProcessingStatus()) || "N".equals(admin.getRdcProcessingStatus())) {
        admin.setReqStatus("PPN");
        admin.setProcessedFlag("E");
      }

      updateEntity(admin, entityManager);
      if ("N".equals(admin.getRdcProcessingStatus()) || "A".equals(admin.getRdcProcessingStatus())) {
        RequestUtils.createWorkflowHistoryFromBatch(entityManager, BATCH_USER_ID, admin,
            "Some errors occurred during RDc processing. Please check request's comment log for details.", action, null, null,
            "COM".equals(admin.getReqStatus()));
      } else {
        RequestUtils.createWorkflowHistoryFromBatch(entityManager, BATCH_USER_ID, admin, comment.toString().trim(), action, null, null,
            "COM".equals(admin.getReqStatus()));
      }
      LOG.debug("Request ID " + admin.getId().getReqId() + " Status: " + admin.getRdcProcessingStatus() + " Message: " + admin.getRdcProcessingMsg());

      partialCommit(entityManager);

    } catch (Exception e) {
      LOG.error("Error in processing Single Reactivate Request  " + admin.getId().getReqId(), e);
      addError("Single Reactivate Request " + admin.getId().getReqId() + " Error: " + e.getMessage());
    }
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
  public static void modifyAddrUseFields(String seqNo, String addrUse, CmrtAddr legacyAddr) {

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
          address.setForUpdate(true);
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
    // PreparedQuery query = new PreparedQuery(entityManager,
    // ExternalizedQuery.getSql("SYSTEM.SUPP_CNTRY_BY_CNTRY_CD"));=
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

    // 1. Get request to process
    query = new PreparedQuery(entityManager, ExternalizedQuery.getSql("BATCH.LD.GET.MASS_UPDT"));
    query.setParameter("REQ_ID", admin.getId().getReqId());
    query.setParameter("ITER_ID", admin.getIterationId());

    List<MassUpdt> results = query.getResults(MassUpdt.class);

    if (results != null && results.size() > 0) {
      // 2. If results are not empty, lock the admin record
      lockRecord(entityManager, admin);
      List<String> errorCmrs = new ArrayList<String>();

      for (MassUpdt massUpdt : results) {
        LOG.debug("BEGIN PROCESSING CMR# >> " + massUpdt.getCmrNo());
        CMRRequestContainer cmrObjects = prepareRequest(entityManager, massUpdt, admin);

        // for every mass update data
        // prepare the createCMR data to be saved
        LegacyDirectObjectContainer legacyObjects = mapRequestDataForMassUpdate(entityManager, cmrObjects, massUpdt, errorCmrs);

        // finally update all data
        CmrtCust legacyCust = legacyObjects.getCustomer();

        if (legacyCust == null) {
          massUpdt.setRowStatusCd(MASS_UPDATE_FAIL);
          StringBuffer errTxt = new StringBuffer(massUpdt.getErrorTxt());

          if (!StringUtils.isEmpty(errTxt.toString())) {
            errTxt.append("<br/>");
          }

          errTxt.append("Legacy customer record cannot be updated because it is null. CMR NO:" + massUpdt.getCmrNo());
          massUpdt.setErrorTxt(errTxt.toString());
          updateEntity(massUpdt, entityManager);
          continue;
          // throw new Exception("Customer record cannot be updated.");
        }
        LOG.info("Mass Updating Legacy Records for Request ID " + admin.getId().getReqId());
        LOG.info(" - SOF Country: " + legacyCust.getId().getSofCntryCode() + " CMR No.: " + legacyCust.getId().getCustomerNo());
        updateEntity(legacyCust, entityManager);
        // partialCommit(entityManager);

        if (legacyObjects.getCustomerExt() != null) {
          updateEntity(legacyObjects.getCustomerExt(), entityManager);
          // partialCommit(entityManager);
        }

        for (CmrtAddr legacyAddr : legacyObjects.getAddresses()) {
          if (legacyAddr.isForUpdate()) {
            legacyAddr.setUpdateTs(SystemUtil.getCurrentTimestamp());
            updateEntity(legacyAddr, entityManager);
            // partialCommit(entityManager);
          } else if (legacyAddr.isForCreate()) {
            createEntity(legacyAddr, entityManager);
            // partialCommit(entityManager);
          }
        }
        massUpdt.setErrorTxt("Legacy data processing completed.\n\n");
        updateEntity(massUpdt, entityManager);
        // partialCommit(entityManager);
        LOG.debug("END PROCESSING CMR# >> " + massUpdt.getCmrNo());
      }
      admin.setLastUpdtTs(SystemUtil.getCurrentTimestamp());

      // DTN: This means errors happened in Legacy processing.
      if (errorCmrs != null && errorCmrs.size() > 0) {
        LOG.error("******Errors happened in legacy mass updates. Please see request summary for details.");
        throw new Exception("Errors happened in legacy mass updates. Please see request summary for details.");
      }

      completeReactDelRecord(entityManager, admin);
    }
    // partialCommit(entityManager);
  }

  /**
   * This is a method to map the mass update request data
   * 
   * @param entityManager
   * @param cmrObjects
   * @param muData
   * @return
   * @throws Exception
   */
  private LegacyDirectObjectContainer mapRequestDataForMassUpdate(EntityManager entityManager, CMRRequestContainer cmrObjects, MassUpdt massUpdt,
      List<String> errorCmrs) throws Exception {

    Data data = cmrObjects.getData();
    // String sql = ExternalizedQuery.getSql("BATCH.LD.GET.MASS_UPDT_DATA");
    // PreparedQuery query = new PreparedQuery(entityManager, sql);
    // query.setForReadOnly(true);
    // query.setParameter("REQ_ID", massUpdt.getId().getParReqId());
    // query.setParameter("ITER_ID", massUpdt.getId().getIterationId());
    // query.setParameter("SEQ_NO", massUpdt.getId().getSeqNo());
    // MassUpdtData muData = query.getSingleResult(MassUpdtData.class);
    MassUpdtData muData = cmrObjects.getMassUpdateData();
    List<MassUpdtAddr> muAddrs = cmrObjects.getMassUpdateAddresses();
    String errTxt = "";
    String addrSeqNos = "";

    if (muAddrs != null && muAddrs.size() > 0) {
      for (MassUpdtAddr muAddr : muAddrs) {
        if (StringUtils.isEmpty(addrSeqNos)) {
          addrSeqNos = "'" + muAddr.getAddrSeqNo() + "'";
        } else {
          addrSeqNos += ", '" + muAddr.getAddrSeqNo() + "'";
        }
      }
    }

    String cntry = data.getCmrIssuingCntry();
    MessageTransformer transformer = TransformerManager.getTransformer(cntry);
    LegacyDirectObjectContainer legacyObjects = new LegacyDirectObjectContainer();

    // legacyObjects = LegacyDirectUtil.getLegacyDBValues(entityManager, cntry,
    // massUpdt.getCmrNo(), false, transformer.hasAddressLinks());

    legacyObjects = LegacyDirectUtil.getLegacyDBValuesForMass(entityManager, cntry, massUpdt.getCmrNo(), false, transformer.hasAddressLinks(),
        addrSeqNos);

    CmrtCust cust = legacyObjects.getCustomer();
    // default mapping for DATA and CMRTCUST
    LOG.debug("Mapping default Data values..");

    if (!StringUtils.isBlank(muData.getAbbrevNm())) {
      cust.setAbbrevNm(muData.getAbbrevNm());
    }

    if (!StringUtils.isBlank(muData.getAbbrevLocn())) {
      cust.setAbbrevLocn(muData.getAbbrevLocn());
    }

    if (!StringUtils.isBlank(muData.getModeOfPayment())) {
      if ("@".equals(muData.getModeOfPayment())) {
        cust.setModeOfPayment("");
      } else {
        cust.setModeOfPayment(muData.getModeOfPayment());
      }
    }

    String isuClientTier = (!StringUtils.isEmpty(muData.getIsuCd()) ? muData.getIsuCd() : "")
        + (!StringUtils.isEmpty(muData.getClientTier()) ? muData.getClientTier() : "");
    if (isuClientTier != null && isuClientTier.length() == 3) {
      cust.setIsuCd(isuClientTier);
    }

    if (!StringUtils.isBlank(muData.getSpecialTaxCd())) {
      cust.setTaxCd(muData.getSpecialTaxCd());
    }

    if (!StringUtils.isBlank(muData.getRepTeamMemberNo())) {
      cust.setSalesRepNo(muData.getRepTeamMemberNo());
    }

    if (!StringUtils.isBlank(muData.getEnterprise())) {
      cust.setEnterpriseNo(muData.getEnterprise());
    }

    if (!StringUtils.isBlank(muData.getCustNm2())) {
      cust.setCeBo(muData.getCustNm2());
    }

    if (!StringUtils.isBlank(muData.getCollectionCd())) {
      if ("@".equals(muData.getCollectionCd())) {
        // cust.setCollectionCd("");
        cust.setDistrictCd("");
      } else {
        cust.setDistrictCd(muData.getCollectionCd());
      }
    }

    if (!StringUtils.isBlank(muData.getIsicCd())) {
      cust.setIsicCd(muData.getIsicCd());
    }

    if (!StringUtils.isBlank(muData.getVat())) {
      cust.setVat(muData.getVat());
    }

    if (!StringUtils.isBlank(muData.getCustNm1())) {
      cust.setSbo(muData.getCustNm1());
      cust.setIbo(muData.getCustNm1());
    }

    if (!StringUtils.isBlank(muData.getInacCd())) {
      if ("@".equals(muData.getInacCd())) {
        cust.setInacCd("");
      } else {
        cust.setInacCd(muData.getInacCd());
      }
    }

    if (!StringUtils.isBlank(muData.getMiscBillCd())) {
      if ("@".equals(muData.getMiscBillCd())) {
        cust.setEmbargoCd("");
      } else {
        cust.setEmbargoCd(muData.getMiscBillCd());
      }
    }

    if (!StringUtils.isBlank(muData.getOutCityLimit())) {
      if ("@".equals(muData.getOutCityLimit())) {
        cust.setMailingCond("");
      } else {
        cust.setMailingCond(muData.getOutCityLimit());
      }
    }

    if (!StringUtils.isBlank(muData.getSubIndustryCd())) {
      cust.setImsCd(muData.getSubIndustryCd());
    }

    cust.setUpdateTs(SystemUtil.getCurrentTimestamp());
    // cust.setUpdStatusTs(SystemUtil.getCurrentTimestamp());

    capsAndFillNulls(cust, true);
    legacyObjects.setCustomer(cust);

    CmrtAddr legacyAddr = null;
    String addressUse = null;

    LOG.debug("Mapping default address values..");
    if (transformer != null) {

      for (MassUpdtAddr addr : cmrObjects.getMassUpdateAddresses()) {
        // plain update
        LOG.debug(addr.getId().getAddrType() + " address of CMR " + addr.getCmrNo() + " is updated, directly updating relevant records");
        legacyAddr = legacyObjects.findBySeqNo(addr.getAddrSeqNo());

        if (legacyAddr == null) {
          if (!StringUtils.isEmpty(errTxt)) {
            errTxt += "\nCannot find legacy address. CMR: " + addr.getCmrNo() + ", SEQ NO:" + addr.getAddrSeqNo();
          } else {
            errTxt += "Cannot find legacy address. CMR: " + addr.getCmrNo() + ", SEQ NO:" + addr.getAddrSeqNo();
          }
          LOG.debug("*****Cannot find legacy address. CMR: " + addr.getCmrNo() + ", SEQ NO:" + addr.getAddrSeqNo());
          continue;
        }

        legacyAddr.setForUpdate(true);

        if (!StringUtils.isBlank(addr.getCustNm1())) {
          legacyAddr.setAddrLine1(addr.getCustNm1());
        }

        if (!StringUtils.isBlank(addr.getCustNm2())) {
          legacyAddr.setAddrLine2(addr.getCustNm2());
        }

        if (!StringUtils.isBlank(addr.getAddrTxt())) {
          legacyAddr.setStreet(addr.getAddrTxt());
        }

        if (!StringUtils.isBlank(addr.getAddrTxt2())) {
          legacyAddr.setStreetNo(addr.getAddrTxt2());
        }

        if (!StringUtils.isBlank(addr.getCity1())) {
          legacyAddr.setCity(addr.getCity1());
        }

        if (!StringUtils.isBlank(addr.getDept())) {
          legacyAddr.setContact(addr.getDept());
        }

        if (!StringUtils.isBlank(addr.getPostCd())) {
          legacyAddr.setZipCode(addr.getPostCd());
        }

        String poBox = addr.getPoBox();
        if (!StringUtils.isEmpty(poBox) && !poBox.toUpperCase().startsWith("APTO")) {
          poBox = " APTO " + poBox;
          legacyAddr.setPoBox(addr.getPoBox());
        }

        boolean crossBorder = false;
        if (!"ES".equals(addr.getLandCntry())) {
          crossBorder = true;
        } else {
          crossBorder = false;
        }

        if (!StringUtils.isBlank(addr.getLandCntry()) && crossBorder) {
          legacyAddr.setAddrLine5(LandedCountryMap.getCountryName(addr.getLandCntry()));
        }

        legacyObjects.addAddress(legacyAddr);
      }

      if (!StringUtils.isEmpty(errTxt)) {
        massUpdt.setErrorTxt(errTxt);
        massUpdt.setRowStatusCd(MASS_UPDATE_FAIL);
        updateEntity(massUpdt, entityManager);
        partialCommit(entityManager);
        errorCmrs.add(massUpdt.getCmrNo());
      }

      for (CmrtAddr currAddr : legacyObjects.getAddresses()) {
        addressUse = currAddr.getAddressUse();
        LOG.trace("Address No: " + currAddr.getId().getAddrNo() + " Uses: " + addressUse);
        if (addressUse != null && !"".equals(addressUse)) {
          for (String use : addressUse.split("")) {
            if (!StringUtils.isEmpty(use)) {
              modifyAddrUseFields(currAddr.getId().getAddrNo(), addressUse, currAddr);
            }
          }
        }
      }

      // rebuild the address use table
      transformer.transformOtherData(entityManager, legacyObjects, cmrObjects);

    }

    return legacyObjects;
  }

  public String getLangCdLegacyMapping(EntityManager entityManager, Data data, String cntry) {
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

  private void skipLegacyProcessingforCreateUpdate(EntityManager entityManager, Admin admin) throws CmrException {
    LOG.debug("Legacy Processing skipped .Started processing of Request " + admin.getId().getReqId() + "request type" + admin.getReqType());
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

  private DataRdc getDataRdcRecords(EntityManager entityManager, Data data) {
    LOG.debug("Searching for DATA_RDC records for Legacy Processing " + data.getId().getReqId());
    String sql = ExternalizedQuery.getSql("SUMMARY.OLDDATA");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", data.getId().getReqId());
    query.setForReadOnly(true);
    return query.getSingleResult(DataRdc.class);
  }

  @Override
  public boolean isMultiMode() {
    return multiMode;
  }

  @Override
  public void setMultiMode(boolean multiMode) {
    this.multiMode = multiMode;
  }

  @Override
  protected boolean terminateOnLongExecution() {
    return !this.multiMode;
  }

  private AddrRdc getAddrRdcRecord(EntityManager entityManager, Addr addr) {
    LOG.debug("Searching for Addr_RDC records for Legacy Processing " + addr.getId().getReqId());
    String sql = ExternalizedQuery.getSql("SUMMARY.OLDADDR");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", addr.getId().getReqId());
    query.setParameter("SEQ", addr.getId().getAddrSeq());
    query.setParameter("ADDR_TYPE", addr.getId().getAddrType());
    query.setForReadOnly(true);
    return query.getSingleResult(AddrRdc.class);

  }

  private List<Kna1> checkIfRecExistOnRDC(EntityManager entityManager, String cmrNo, String cntry) {
    String sql = ExternalizedQuery.getSql("BATCH.GET.KNA1_MANDT_CMRNO");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("CMR_NO", cmrNo);
    query.setParameter("KATR6", cntry);
    query.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
    query.setForReadOnly(true);
    List<Kna1> rdcRecs = query.getResults(Kna1.class);
    return rdcRecs;
  }

  private List<CmrtCust> checkIfRecExistOnLegacy(EntityManager entityManager, String cmrNo, String cntry) {
    String sql = ExternalizedQuery.getSql("LEGACYD.GETCUST");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("CMR_NO", cmrNo);
    query.setParameter("COUNTRY", cntry);
    query.setForReadOnly(true);
    List<CmrtCust> legacyRecs = query.getResults(CmrtCust.class);
    return legacyRecs;
  }

  private List<String> checkIfSeqExistOnRDC(EntityManager entityManager, String cmrNo, String cntry) {
    LOG.debug("Searching for DATA_RDC records for Legacy Processing ");
    String sql = ExternalizedQuery.getSql("GET.RDC_SEQ");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("CMR_NO", cmrNo);
    query.setParameter("CNTRY", cntry);
    query.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
    query.setForReadOnly(true);
    List<String> rdcSequences = query.getResults(String.class);
    LOG.debug("RDC sequences =" + rdcSequences);
    return rdcSequences;
  }

  private void modifyPG01Sequences(CMRRequestContainer cmrobjects, EntityManager entityManager) {
    try {
      List<Addr> pgs = new ArrayList<Addr>();
      List<Addr> addresses = new ArrayList<Addr>();
      addresses = cmrobjects.getAddresses();
      int maxseqno = 1;
      boolean isUpdate = "U".equals(cmrobjects.getAdmin().getReqType());
      List<String> existingSeq = addresses.stream().filter(e -> !"PG01".equals(e.getId().getAddrType())).map(e -> e.getId().getAddrSeq())
          .collect(Collectors.toList());
      for (Addr addr : addresses) {
        if ("PG01".equals(addr.getId().getAddrType())) {
          pgs.add(addr);
        }
      }
      if (pgs.size() != 0) {
        Collections.sort(pgs, new Comparator<Addr>() {
          @Override
          public int compare(Addr a1, Addr a2) {
            return a1.getId().getAddrSeq().compareTo(a2.getId().getAddrSeq());
          }
        });

        maxseqno = getMaxSequenceOnAddr(entityManager, cmrobjects.getAdmin().getId().getReqId());

        LOG.debug("Max Sequence on non PG01 address = " + maxseqno);

        maxseqno = maxseqno + 1;

        for (Addr pg : pgs) {
          if ((isUpdate == false) || (isUpdate && !existingSeq.isEmpty() && existingSeq.contains(pg.getId().getAddrSeq())
              && ("N".equals(pg.getImportInd()) || "Y".equals(pg.getChangedIndc())))) {
            String newSeqNo = StringUtils.leftPad(Integer.toString(maxseqno), 5, '0');
            updateAddrSeq(entityManager, cmrobjects.getAdmin().getId().getReqId(), pg.getId().getAddrType(), pg.getId().getAddrSeq(), newSeqNo,
                pg.getSapNo(), false);
            // updateEntity(pg, entityManager);
            maxseqno++;
          }
        }
      }

    } catch (Exception e) {
      LOG.debug("Error occured while updating PG01 sequences" + e.getMessage());
    }

  }

  private int getMaxSequenceOnAddr(EntityManager entityManager, long reqId) {
    String maxAddrSeq = null;
    int addrSeq = 0;
    String sql = ExternalizedQuery.getSql("GETADDRSEQ.NON_PG01.MAX");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", reqId);

    List<Object[]> results = query.getResults();
    if (results != null && results.size() > 0) {
      Object[] result = results.get(0);
      maxAddrSeq = (String) (result != null && result.length > 0 ? result[0] : "0");
      if (StringUtils.isEmpty(maxAddrSeq)) {
        maxAddrSeq = "0";
      }
      addrSeq = Integer.parseInt(maxAddrSeq);
    }

    return addrSeq;
  }

  private List<Integer> getSecondarySoldToFromRDC(EntityManager entityManager, String cmrNo, String cntry) {
    LOG.debug("Retrieve secondary sold to from RDC for Legacy Processing ");
    String sql = ExternalizedQuery.getSql("GET.RDC_SEQ.SECONDARY.SOLDTO");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("ZZKV_CUSNO", cmrNo);
    query.setParameter("KATR6", cntry);
    query.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
    query.setForReadOnly(true);
    List<Integer> rdcSequences = query.getResults(Integer.class);
    LOG.debug("RDC sequences =" + rdcSequences);
    return rdcSequences;
  }

  private boolean checkIfAddrIsSecondarySoldToIL(List<Integer> secondarySoldToListIL, Addr addr) {
    if (secondarySoldToListIL != null && !secondarySoldToListIL.isEmpty()) {
      int curAddrSeq = Integer.parseInt(addr.getId().getAddrSeq());
      if (secondarySoldToListIL.contains(curAddrSeq)) {
        return true;
      }
    }
    return false;
  }

}