/**
 * 
 */
package com.ibm.cmr.create.batch.service;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.AddrPK;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.CmrtAddr;
import com.ibm.cio.cmr.request.entity.CmrtAddrUse;
import com.ibm.cio.cmr.request.entity.CmrtCust;
import com.ibm.cio.cmr.request.entity.CmrtCustExt;
import com.ibm.cio.cmr.request.entity.CompoundEntity;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.entity.DataPK;
import com.ibm.cio.cmr.request.entity.MassUpdt;
import com.ibm.cio.cmr.request.entity.MassUpdtAddr;
import com.ibm.cio.cmr.request.entity.MassUpdtData;
import com.ibm.cio.cmr.request.entity.MassUpdtDataPK;
import com.ibm.cio.cmr.request.entity.ReqCmtLog;
import com.ibm.cio.cmr.request.entity.SuppCntry;
import com.ibm.cio.cmr.request.entity.WfHist;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.util.RequestUtils;
import com.ibm.cio.cmr.request.util.SystemLocation;
import com.ibm.cio.cmr.request.util.SystemUtil;
import com.ibm.cio.cmr.request.util.legacy.LegacyDirectObjectContainer;
import com.ibm.cio.cmr.request.util.legacy.LegacyDirectUtil;
import com.ibm.cmr.create.batch.model.MassUpdateServiceInput;
import com.ibm.cmr.create.batch.util.CMRRequestContainer;
import com.ibm.cmr.create.batch.util.mq.LandedCountryMap;
import com.ibm.cmr.create.batch.util.mq.transformer.MessageTransformer;
import com.ibm.cmr.create.batch.util.mq.transformer.TransformerManager;
import com.ibm.cmr.services.client.process.RDcRecord;
import com.ibm.cmr.services.client.process.mass.MassProcessRequest;
import com.ibm.cmr.services.client.process.mass.MassUpdateRecord;

/**
 * Class to process CMRs moved to the CMRDB2D schema for mass updates.
 * 
 * @author JeffZAMORA
 * 
 */
public class LegacyDirectLegacyMassProcessService extends TransConnService {

  private boolean devMode;
  private static final Logger LOG = Logger.getLogger(LegacyDirectLegacyMassProcessService.class);

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
  private static final String MASS_UPDATE_FAIL = "FAIL";
  private static final String MASS_UPDATE_DONE = "DONE";
  private static final String MASS_UPDATE_LEGACYDONE = "LDONE";
  private static final String MASS_UDPATE_LEGACY_FAIL_MSG = "Errors happened in legacy mass updates. Pleaes see request summary for details.";
  private static final int MASS_UPDATE_TXT_LEN = 10000;

  @Override
  protected Boolean executeBatch(EntityManager entityManager) throws Exception {

    if (this.devMode) {
      LOG.info("RUNNING IN DEVELOPMENT MODE");
    }
    LOG.info("Initializing Country Map..");
    LandedCountryMap.init(entityManager);
    // Retrieve the PCP records and create in the Legacy DB
    LOG.info("Retreiving pending records for processing..");
    List<Admin> pending = getPendingRecords(entityManager);

    LOG.debug((pending != null ? pending.size() : 0) + " records to process.");
    // pending = new ArrayList<Admin>();
    for (Admin admin : pending) {
      try {
        switch (admin.getReqType()) {
        case CmrConstants.REQ_TYPE_MASS_UPDATE:
          processMassUpdate(entityManager, admin);
          break;
        }

      } catch (Exception e) {
        if (!MASS_UDPATE_LEGACY_FAIL_MSG.equalsIgnoreCase(e.getMessage())) {
          partialRollback(entityManager);
        }
        LOG.error("Unexpected error occurred during processing of Request " + admin.getId().getReqId(), e);
        processError(entityManager, admin, e.getMessage());
      }
      partialCommit(entityManager);
    }
    return true;
  }

  /**
   * Gets the Admin records with status = 'PCP' and country has processing type
   * = 'LD'
   * 
   * @param entityManager
   * @return
   */
  private List<Admin> getPendingRecords(EntityManager entityManager) {
    String sql = ExternalizedQuery.getSql("LEGACYD.GET_MASS_PROCESS_PENDING");
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

    createHistory(entityManager, "Legacy database processing started.", "PCR", "Claim", admin.getId().getReqId());
    createComment(entityManager, "Legacy database processing started.", admin.getId().getReqId());

    // partialCommit(entityManager);
  }

  private void completeMassUpdateRecord(EntityManager entityManager, Admin admin, List<String> errors) throws CmrException, SQLException {
    LOG.info("Completing LEGACY processing for Mass Request " + admin.getId().getReqId());
    admin.setLockBy(null);
    admin.setLockByNm(null);
    admin.setLockInd("N");
    // completing

    // DTN: 1795577: Spain - Mass Update - processing should not stop when
    // template contains non-existent CNs
    /*
     * if (errors != null && errors.size() > 0) { admin.setReqStatus("PPN");
     * admin.setProcessedFlag(CmrConstants.PROCESSING_STATUS.E.toString()); }
     * else { admin.setReqStatus("PCO"); }
     */

    admin.setReqStatus("PCO");
    admin.setLastUpdtBy(BATCH_USER_ID);
    updateEntity(admin, entityManager);

    String message = "Records updated successfully on the Legacy Database ";

    if (errors != null && errors.size() > 0) {
      message = "Some CMRs were not processed to legacy. Please see request summary for details.<br>Legacy Database procesing is finished.";
    }
    // String errMsg =
    // "Errors happened in LEGACY mass updates. Please see request summary for
    // details.";

    WfHist hist = createHistory(entityManager, message, "PCO", "Legacy Processing", admin.getId().getReqId());

    // DTN: 1795577: Spain - Mass Update - processing should not stop when
    // template contains non-existent CNs
    /*
     * if (errors != null && errors.size() > 0) { createComment(entityManager,
     * errMsg, admin.getId().getReqId()); } else if (errors != null &&
     * errors.size() <= 0) { createComment(entityManager, message,
     * admin.getId().getReqId()); }
     */

    createComment(entityManager, message, admin.getId().getReqId());
    RequestUtils.sendEmailNotifications(entityManager, admin, hist, false, true);

    partialCommit(entityManager);

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

    WfHist hist = createHistory(entityManager, "An error occurred during processing: " + errorMsg, "PPN", "Processing Error", admin.getId()
        .getReqId());
    createComment(entityManager, "An error occurred during processing:\n" + errorMsg, admin.getId().getReqId());

    RequestUtils.sendEmailNotifications(entityManager, admin, hist);
  }

  private boolean isMassUpdtDataChanges(EntityManager entityManager, MassUpdt massUpdt, Admin admin) throws Exception {
    boolean isMassUpdtDataChanges = false;

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

    if (!StringUtils.isBlank(muData.getAbbrevNm())) {
      isMassUpdtDataChanges = true;
    }

    if (!StringUtils.isBlank(muData.getAbbrevLocn())) {
      isMassUpdtDataChanges = true;
    }

    if (!StringUtils.isBlank(muData.getModeOfPayment())) {
      isMassUpdtDataChanges = true;
    }

    if (!StringUtils.isEmpty(muData.getIsuCd())) {
      isMassUpdtDataChanges = true;
    }

    if (!StringUtils.isBlank(muData.getSpecialTaxCd())) {
      isMassUpdtDataChanges = true;
    }

    if (!StringUtils.isBlank(muData.getRepTeamMemberNo())) {
      isMassUpdtDataChanges = true;
    }

    if (!StringUtils.isBlank(muData.getEnterprise())) {
      isMassUpdtDataChanges = true;
    }

    if (!StringUtils.isBlank(muData.getCustNm2())) {
      isMassUpdtDataChanges = true;
    }

    if (!StringUtils.isBlank(muData.getCollectionCd())) {
      isMassUpdtDataChanges = true;
    }

    if (!StringUtils.isBlank(muData.getIsicCd())) {
      isMassUpdtDataChanges = true;
    }

    if (!StringUtils.isBlank(muData.getVat())) {
      isMassUpdtDataChanges = true;
    }

    if (!StringUtils.isBlank(muData.getCustNm1())) {
      isMassUpdtDataChanges = true;
    }

    if (!StringUtils.isBlank(muData.getInacCd())) {
      isMassUpdtDataChanges = true;
    }

    if (!StringUtils.isBlank(muData.getMiscBillCd())) {
      isMassUpdtDataChanges = true;
    }

    if (!StringUtils.isBlank(muData.getOutCityLimit())) {
      isMassUpdtDataChanges = true;
    }

    if (!StringUtils.isBlank(muData.getSubIndustryCd())) {
      isMassUpdtDataChanges = true;
    }
    // CMR-1728 Turkey RestrictTo and CsoSite temp used to store CoF and
    // Economic code
    if (!StringUtils.isBlank(muData.getRestrictTo())) {
      isMassUpdtDataChanges = true;
    }

    if (!StringUtils.isBlank(muData.getCsoSite())) {
      isMassUpdtDataChanges = true;
    }

    return isMassUpdtDataChanges;
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
   * Initializes an empty instance of the the entity object
   * 
   * @return
   * @throws Exception
   */
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
      RequestUtils.createWorkflowHistoryFromBatch(entityManager, BATCH_USER_ID, admin, comment, "C".equals(admin.getReqType()) ? ACTION_RDC_CREATE
          : ACTION_RDC_UPDATE, null, null, true, false);
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

    if (address == null) {
      boolean isPadZeroes = seqNo != null && seqNo.length() != 5 ? true : false;
      seqNo = LegacyDirectUtil.handleLDSeqNoScenario(seqNo, isPadZeroes);
      address = legacyObjects.findBySeqNo(seqNo);
    }

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

  /*
   * Helper method for processing Italy mass updates
   */
  private void processMassUpdateIT(EntityManager entityManager, LegacyDirectObjectContainer legacyObjects, Admin admin, MassUpdt mUpdate, Data data,
      List<String> errors) throws Exception {
    LOG.info("Mass Updating Legacy Records for Request ID " + admin.getId().getReqId());
    LOG.info(" - SOF Country: " + data.getCmrIssuingCntry() + " CMR No.: " + mUpdate.getCmrNo());
    if (legacyObjects != null) {
      List<CmrtCust> custs = legacyObjects.getCustomersIT();
      List<CmrtCustExt> custExts = legacyObjects.getCustomersextIT();
      // List<String> errors = new ArrayList<String>();
      // List<String> processedCmrs = new ArrayList<String>();
      String errTxt = "";
      List<String> listErrTxt = new ArrayList<String>();

      if (custs != null && !custs.isEmpty()) {
        // errTxt = "\n\nThe following CMRTCUST data have been processed:\n\n";
        for (CmrtCust cust : custs) {
          updateEntity(cust, entityManager);
          // errTxt += cust.getId().getCustomerNo() + "\n";
          listErrTxt.add(cust.getId().getCustomerNo());
        }
      } else {
        errors.add(mUpdate.getCmrNo());
      }

      if (!StringUtils.isEmpty(errTxt)) {
        errTxt += "\n\n";
      }

      if (custExts != null && !custExts.isEmpty()) {
        // errTxt = "\n\nThe following CMRTCEXT data have been processed:\n\n";
        for (CmrtCustExt custExt : custExts) {
          updateEntity(custExt, entityManager);
          // errTxt += custExt.getId().getCustomerNo() + "\n";
          if (!listErrTxt.contains(custExt.getId().getCustomerNo())) {
            listErrTxt.add(custExt.getId().getCustomerNo());
          }
        }
      } else {
        if (errors.contains(mUpdate.getCmrNo())) {
          errors.add(mUpdate.getCmrNo());
        }
      }

      if (StringUtils.isEmpty(legacyObjects.getErrTxt())) {
        mUpdate.setRowStatusCd(MASS_UPDATE_LEGACYDONE);

        for (String cmrsVal : listErrTxt) {
          errTxt += cmrsVal + ", ";
        }

        mUpdate.setErrorTxt("Legacy data processing completed for the following CMRs: " + errTxt);
        if (errTxt.length() > MASS_UPDATE_TXT_LEN) {
          mUpdate.setErrorTxt("Legacy data processing COMPLETED for MULTIPLE CMRs and is to long to be logged on the summary. "
              + "Please contact admins to get the actual list.");
        }
        updateEntity(mUpdate, entityManager);
      }

    } else {
      throw new CmrException(new Exception("The parameter legacyObjects is null. Mass updates can not proceed."));
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
        Data data = cmrObjects.getData();

        // for every mass update data
        // prepare the createCMR data to be saved
        LegacyDirectObjectContainer legacyObjects = mapRequestDataForMassUpdate(entityManager, cmrObjects, massUpdt, errorCmrs, admin);

        if (legacyObjects.getErrTxt() != null
            && legacyObjects.getErrTxt().contains("Mass Update can not process if there are data changes and there are more than")) {
          continue;
        } else if (legacyObjects.getErrTxt() != null && legacyObjects.getErrTxt().contains("does not exist on the Legacy DB.")) {
          continue;
        }

        if (data != null && SystemLocation.ITALY.equals(data.getCmrIssuingCntry()) && legacyObjects != null) {
          processMassUpdateIT(entityManager, legacyObjects, admin, massUpdt, data, errorCmrs);
          continue;
        } else if (data != null && SystemLocation.ITALY.equals(data.getCmrIssuingCntry()) && legacyObjects == null) {
          errorCmrs.add("The parameter legacyObjects is null. Mass updates can not proceed.");
          continue;
        }

        // finally update all data
        CmrtCust legacyCust = legacyObjects.getCustomer();

        if (legacyCust == null) {
          massUpdt.setRowStatusCd(MASS_UPDATE_FAIL);
          StringBuffer errTxt = new StringBuffer(massUpdt.getErrorTxt());

          if (!StringUtils.isEmpty(errTxt.toString())) {
            errTxt.append("<br/>");
          }

          errTxt.append("Legacy customer record cannot be updated because it does not exist on LEGACY. CMR NO:" + massUpdt.getCmrNo());
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

        // CMR-2279: update muData
        if (SystemLocation.TURKEY.equals(data.getCmrIssuingCntry())) {
          updateEntity(cmrObjects.getMassUpdateData(), entityManager);
        }

        if (StringUtils.isEmpty(legacyObjects.getErrTxt())) {
          massUpdt.setRowStatusCd(MASS_UPDATE_LEGACYDONE);
          massUpdt.setErrorTxt("Legacy data processing completed.\n\n");
          updateEntity(massUpdt, entityManager);
        }

        partialCommit(entityManager);
        LOG.debug("END PROCESSING CMR# >> " + massUpdt.getCmrNo());
      }
      admin.setLastUpdtTs(SystemUtil.getCurrentTimestamp());

      // DTN: This means errors happened in Legacy processing.
      completeMassUpdateRecord(entityManager, admin, errorCmrs);
    }
    // partialCommit(entityManager);
  }

  private LegacyDirectObjectContainer mapRequestDataForMassUpdateIT(EntityManager entityManager, CMRRequestContainer cmrObjects, MassUpdt massUpdt,
      List<String> errorCmrs, Admin admin) throws Exception {
    // DENNIS: Remember, we do not need to process addresses
    // List<MassUpdtAddr> muAddrs = cmrObjects.getMassUpdateAddresses();
    Data data = cmrObjects.getData();
    LegacyDirectObjectContainer legacyObjects = new LegacyDirectObjectContainer();
    MassUpdtData muData = cmrObjects.getMassUpdateData();
    String errTxt = "";
    String addrSeqNos = "";
    String cntry = data != null ? data.getCmrIssuingCntry() : "";
    MessageTransformer transformer = TransformerManager.getTransformer(cntry);

    if (!StringUtils.isBlank(muData.getNewEntpName1())) {
      // we perform check if it is not yet used,
      if (!"@".equals(muData.getNewEntpName1().trim())
          && LegacyDirectUtil.isFisCodeUsed(entityManager, SystemLocation.ITALY, muData.getNewEntpName1(), massUpdt.getCmrNo())) {
        errorCmrs.add("Entered Fiscal Code for CMR:" + massUpdt.getCmrNo() + " and Fiscal Code:" + muData.getNewEntpName1() + " is already in use.");
        legacyObjects.setErrTxt("Entered Fiscal Code for CMR:" + massUpdt.getCmrNo() + " and Fiscal Code:" + muData.getNewEntpName1()
            + " is already in use.");
        massUpdt.setErrorTxt(legacyObjects.getErrTxt());
        massUpdt.setRowStatusCd(MASS_UPDATE_FAIL);
        partialCommit(entityManager);
        errorCmrs.add(massUpdt.getCmrNo());
        return legacyObjects;
      }
    }

    // get all the needed cust data provided the cmr
    legacyObjects = LegacyDirectUtil.getLegacyDBValuesForITMass(entityManager, cntry, massUpdt.getCmrNo(), muData, false);
    List<CmrtCust> custs = legacyObjects != null ? legacyObjects.getCustomersIT() : null;
    List<CmrtCust> finalCusts = new ArrayList<CmrtCust>();

    if (custs != null && custs.size() > 0) {
      for (CmrtCust cust : custs) {
        transformer.transformLegacyCustomerDataMassUpdate(entityManager, cust, cmrObjects, muData);
        finalCusts.add(cust);
      }
      legacyObjects.setCustomersIT(finalCusts);
    } else {
      errorCmrs
          .add("Mass update has encountered an error: The list of legacy customer data with the same fiscal code is empty. Please correct the data.");
      // throw new CmrException(new Exception(
      // "Mass update has encountered an error: The list of legacy customer data with the same fiscal code is empty. Please correct the data."));
      massUpdt.setErrorTxt(legacyObjects.getErrTxt());
      massUpdt.setRowStatusCd(MASS_UPDATE_FAIL);
      partialCommit(entityManager);
      errorCmrs.add(massUpdt.getCmrNo());
      return legacyObjects;
    }

    List<CmrtCustExt> cmrtExts = legacyObjects != null ? legacyObjects.getCustomersextIT() : null;
    List<CmrtCustExt> finalCexts = new ArrayList<CmrtCustExt>();

    if (cmrtExts != null) {
      for (CmrtCustExt custExt : cmrtExts) {
        transformer.transformLegacyCustomerExtDataMassUpdate(entityManager, custExt, cmrObjects, muData, massUpdt.getCmrNo());
        finalCexts.add(custExt);
      }
      legacyObjects.setCustomersextIT(finalCexts);
    } else {
      errorCmrs
          .add("Mass update has encountered an error: The list of legacy customer extended data with the same fiscal code is empty. Please correct "
              + "the data.");
      throw new CmrException(new Exception(
          "Mass update has encountered an error: The list of legacy customer extended data with the same fiscal code is empty. Please correct "
              + "the data."));
    }
    return legacyObjects;
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
      List<String> errorCmrs, Admin admin) throws Exception {
    Data data = cmrObjects.getData();
    LegacyDirectObjectContainer legacyObjects = new LegacyDirectObjectContainer();

    MassUpdtData muData = cmrObjects.getMassUpdateData();
    List<MassUpdtAddr> muAddrs = cmrObjects.getMassUpdateAddresses();
    String errTxt = "";
    String addrSeqNos = "";

    if (SystemLocation.ITALY.equals(data.getCmrIssuingCntry())) {
      return mapRequestDataForMassUpdateIT(entityManager, cmrObjects, massUpdt, errorCmrs, admin);
    }

    if (muAddrs != null && muAddrs.size() > 0) {
      for (MassUpdtAddr muAddr : muAddrs) {
        if (StringUtils.isEmpty(addrSeqNos)) {
          addrSeqNos = "'" + muAddr.getAddrSeqNo() + "'";
        } else {
          addrSeqNos += ", '" + muAddr.getAddrSeqNo() + "'";
        }

        boolean isPadZeroes = muAddr.getAddrSeqNo() != null && muAddr.getAddrSeqNo().length() != 5 ? true : false;
        String seqNo = LegacyDirectUtil.handleLDSeqNoScenario(muAddr.getAddrSeqNo(), isPadZeroes);

        if (StringUtils.isEmpty(addrSeqNos)) {
          addrSeqNos = "'" + seqNo + "'";
        } else {
          addrSeqNos += ", '" + seqNo + "'";
        }

      }
    }

    String cntry = data.getCmrIssuingCntry();
    MessageTransformer transformer = TransformerManager.getTransformer(cntry);
    LegacyDirectObjectContainer legacyObjects2 = new LegacyDirectObjectContainer();

    legacyObjects2 = LegacyDirectUtil.getLegacyAddrDBValuesForMass(entityManager, cntry, massUpdt.getCmrNo(), false);

    if (legacyObjects2.getAddresses().size() >= CmrConstants.LD_MASS_UPDATE_UPPER_LIMIT && isMassUpdtDataChanges(entityManager, massUpdt, admin)
        && "Y".equals(data.getInstallTeamCd())) {

      errTxt = "Legacy direct Mass Update can not process if there are data changes and there are more than "
          + CmrConstants.LD_MASS_UPDATE_UPPER_LIMIT + " addresses on that CMR to process on RDc. Please make a normal update request instead.";
      legacyObjects.setErrTxt(errTxt);
      massUpdt.setErrorTxt(errTxt);
      massUpdt.setRowStatusCd(MASS_UPDATE_FAIL);
      updateEntity(massUpdt, entityManager);
      partialCommit(entityManager);
      errorCmrs.add(massUpdt.getCmrNo());
      return legacyObjects;
    }

    legacyObjects = LegacyDirectUtil.getLegacyDBValuesForMass(entityManager, cntry, massUpdt.getCmrNo(), false, transformer.hasAddressLinks(),
        addrSeqNos);

    CmrtCust cust = legacyObjects.getCustomer();

    if (cust == null) {
      errTxt = "CMR " + massUpdt.getCmrNo() + " does not exist on the Legacy DB.";
      legacyObjects.setErrTxt(errTxt);
      massUpdt.setErrorTxt(errTxt);
      massUpdt.setRowStatusCd(MASS_UPDATE_FAIL);
      updateEntity(massUpdt, entityManager);
      partialCommit(entityManager);
      errorCmrs.add(massUpdt.getCmrNo());
      return legacyObjects;
    }

    transformer.transformLegacyCustomerDataMassUpdate(entityManager, cust, cmrObjects, muData);

    if (transformer.hasCmrtCustExt()) {
      CmrtCustExt custExt = legacyObjects.getCustomerExt();
      if (transformer != null) {
        try {
          transformer.transformLegacyCustomerExtDataMassUpdate(entityManager, custExt, cmrObjects, muData, massUpdt.getCmrNo());
        } catch (Exception e) {
          legacyObjects.setErrTxt(e.getMessage());
          massUpdt.setErrorTxt(e.getMessage());
          massUpdt.setRowStatusCd(MASS_UPDATE_FAIL);
          updateEntity(massUpdt, entityManager);
          partialCommit(entityManager);
          errorCmrs.add(massUpdt.getCmrNo());
          return legacyObjects;
        }
      }
      if (custExt != null) {
        custExt.setUpdateTs(SystemUtil.getCurrentTimestamp());
        custExt.setAeciSubDt(SystemUtil.getDummyDefaultDate());
        legacyObjects.setCustomerExt(custExt);
      }
    }

    capsAndFillNulls(cust, true);
    legacyObjects.setCustomer(cust);

    CmrtAddr legacyAddr = null;
    String addressUse = null;

    if (transformer != null) {
      LOG.debug("Mapping default address values..");
      for (MassUpdtAddr addr : cmrObjects.getMassUpdateAddresses()) {
        // plain update
        LOG.debug(addr.getId().getAddrType() + " address of CMR " + addr.getCmrNo() + " is updated, directly updating relevant records");
        legacyAddr = legacyObjects.findBySeqNo(addr.getAddrSeqNo());

        if (legacyAddr == null) {
          boolean isPadZeroes = addr.getAddrSeqNo() != null && addr.getAddrSeqNo().length() != 5 ? true : false;
          String seqNo = LegacyDirectUtil.handleLDSeqNoScenario(addr.getAddrSeqNo(), isPadZeroes);

          legacyAddr = legacyObjects.findBySeqNo(seqNo);
        }

        if (legacyAddr == null) {
          if (!StringUtils.isEmpty(errTxt)) {
            errTxt += "\nCannot find legacy address. CMR: " + addr.getCmrNo() + ", SEQ NO:" + addr.getAddrSeqNo();
          } else {
            errTxt += "Cannot find legacy address. CMR: " + addr.getCmrNo() + ", SEQ NO:" + addr.getAddrSeqNo();
          }
          legacyObjects.setErrTxt(errTxt);
          LOG.debug("*****Cannot find legacy address. CMR: " + addr.getCmrNo() + ", SEQ NO:" + addr.getAddrSeqNo());
          continue;
        }

        transformer.transformLegacyAddressDataMassUpdate(entityManager, legacyAddr, addr, cntry, cust, data, legacyObjects);
        // DTN: Set again the CmrtCust object on the legacy objects
        // container just to be sure
        legacyObjects.setCustomer(cust);

      }

      if (!StringUtils.isEmpty(errTxt)) {
        legacyObjects.setErrTxt(errTxt);
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

  @Override
  protected boolean terminateOnLongExecution() {
    return false;
  }

  public String getAddressKey(String addrType) {
    switch (addrType) {
    case "ZP01":
      return "Mail";
    case "ZI01":
      return "Install";
    case "ZD01":
      return "Ship";
    case "ZS01":
      return "Billing";
    case "ZS02":
      return "Soft";
    case "ZP02":
      return "Fiscal";
    default:
      return "";
    }
  }

}
