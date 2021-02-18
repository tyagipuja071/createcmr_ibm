package com.ibm.cmr.create.batch.service;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import javax.persistence.Column;
import javax.persistence.EntityManager;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.Addlctrydata;
import com.ibm.cio.cmr.request.entity.AddlctrydataPK;
import com.ibm.cio.cmr.request.entity.CmrCloningQueue;
import com.ibm.cio.cmr.request.entity.CmrCloningQueuePK;
import com.ibm.cio.cmr.request.entity.CmrtAddr;
import com.ibm.cio.cmr.request.entity.CmrtAddrPK;
import com.ibm.cio.cmr.request.entity.CmrtCust;
import com.ibm.cio.cmr.request.entity.CmrtCustExt;
import com.ibm.cio.cmr.request.entity.CmrtCustExtPK;
import com.ibm.cio.cmr.request.entity.CmrtCustPK;
import com.ibm.cio.cmr.request.entity.Kna1;
import com.ibm.cio.cmr.request.entity.Kna1PK;
import com.ibm.cio.cmr.request.entity.Knb1;
import com.ibm.cio.cmr.request.entity.Knb1PK;
import com.ibm.cio.cmr.request.entity.Knbk;
import com.ibm.cio.cmr.request.entity.KnbkPK;
import com.ibm.cio.cmr.request.entity.Knex;
import com.ibm.cio.cmr.request.entity.KnexPK;
import com.ibm.cio.cmr.request.entity.Knva;
import com.ibm.cio.cmr.request.entity.KnvaPK;
import com.ibm.cio.cmr.request.entity.Knvi;
import com.ibm.cio.cmr.request.entity.KnviPK;
import com.ibm.cio.cmr.request.entity.Knvk;
import com.ibm.cio.cmr.request.entity.KnvkPK;
import com.ibm.cio.cmr.request.entity.Knvl;
import com.ibm.cio.cmr.request.entity.KnvlPK;
import com.ibm.cio.cmr.request.entity.Knvp;
import com.ibm.cio.cmr.request.entity.KnvpPK;
import com.ibm.cio.cmr.request.entity.Knvv;
import com.ibm.cio.cmr.request.entity.KnvvPK;
import com.ibm.cio.cmr.request.entity.KunnrExt;
import com.ibm.cio.cmr.request.entity.KunnrExtPK;
import com.ibm.cio.cmr.request.entity.RdcCloningRefn;
import com.ibm.cio.cmr.request.entity.RdcCloningRefnPK;
import com.ibm.cio.cmr.request.entity.ReservedCMRNos;
import com.ibm.cio.cmr.request.entity.ReservedCMRNosPK;
import com.ibm.cio.cmr.request.entity.Sadr;
import com.ibm.cio.cmr.request.entity.SadrPK;
import com.ibm.cio.cmr.request.entity.Sizeinfo;
import com.ibm.cio.cmr.request.entity.SizeinfoPK;
import com.ibm.cio.cmr.request.entity.listeners.ChangeLogListener;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.ui.PageManager;
import com.ibm.cio.cmr.request.util.RequestUtils;
import com.ibm.cio.cmr.request.util.SystemUtil;
import com.ibm.cio.cmr.request.util.geo.GEOHandler;
import com.ibm.cio.cmr.request.util.legacy.CloningMapping;
import com.ibm.cio.cmr.request.util.legacy.CloningOverrideMapping;
import com.ibm.cio.cmr.request.util.legacy.CloningOverrideUtil;
import com.ibm.cio.cmr.request.util.legacy.CloningRDCConfiguration;
import com.ibm.cio.cmr.request.util.legacy.CloningRDCUtil;
import com.ibm.cio.cmr.request.util.legacy.CloningUtil;
import com.ibm.cio.cmr.request.util.legacy.LegacyDirectObjectContainer;
import com.ibm.cio.cmr.request.util.legacy.LegacyDirectUtil;
import com.ibm.cmr.create.batch.util.mq.LandedCountryMap;
import com.ibm.cmr.create.batch.util.mq.transformer.MessageTransformer;
import com.ibm.cmr.create.batch.util.mq.transformer.TransformerManager;
import com.ibm.cmr.services.client.CmrServicesFactory;
import com.ibm.cmr.services.client.GenerateCMRNoClient;
import com.ibm.cmr.services.client.cmrno.GenerateCMRNoRequest;
import com.ibm.cmr.services.client.cmrno.GenerateCMRNoResponse;

public class CloningProcessService extends MultiThreadedBatchService<CmrCloningQueue> {

  private static final Logger LOG = Logger.getLogger(CloningProcessService.class);

  private static final String BATCH_SERVICE_URL = SystemConfiguration.getValue("BATCH_SERVICES_URL");
  private static final String COMMENT_LOGGER = "Cloning Process Service";
  private boolean devMode;
  private static final String KUNNR_KEY = "KUNNR";
  private static final String ADRNR_KEY = "ADRNR";
  private static final String PARNR_KEY = "PARNR";

  private static final String LEGACY_CUST_TABLE = "CMRTCUST";
  private static final String LEGACY_CUSTEXT_TABLE = "CMRTCEXT";
  private static final String LEGACY_ADDR_TABLE = "CMRTADDR";
  private static final String KNA1_TABLE = "KNA1";

  private static final List<String> KNB1_ADDR = Arrays.asList("ZS01", "ZS02");
  private static final List<String> KNVI_PK = Arrays.asList("aland", "tatyp");
  private static final List<String> KNVP_PK = Arrays.asList("vkorg", "vtweg", "spart", "parvw", "parza");
  private static final List<String> KNBK_PK = Arrays.asList("banks", "bankl", "bankn");
  private static final List<String> KNVL_PK = Arrays.asList("aland", "tatyp", "licnr");

  private static final List<String> PROCESSING_TYPES = Arrays.asList("DR", "MA", "MD");

  private static final List<String> STATUS_CLONING = Arrays.asList("IN_PROG", "LEGACY_ERR");
  private static final List<String> STATUS_LEGACY = Arrays.asList("LEGACY_OK", "LEGACYSKIP");
  private static final List<String> STATUS_RDC = Arrays.asList("RDC_INPROG", "RDC_ERR");

  @Override
  public boolean isTransactional() {
    // TODO Auto-generated method stub
    return true;
  }

  public boolean isDevMode() {
    return devMode;
  }

  public void setDevMode(boolean devMode) {
    this.devMode = devMode;
  }

  private List<CmrCloningQueue> getCloningPendingRecords(EntityManager entityManager) {
    String sql = ExternalizedQuery.getSql("CLONING_PENDING_RECORDS");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    return query.getResults(CmrCloningQueue.class);
  }

  private void processCloningRecord(EntityManager entityManager, CmrCloningQueue cloningQueue) throws Exception {
    String cmrNo = cloningQueue.getId().getCmrNo();
    String cntry = cloningQueue.getId().getCmrIssuingCntry();
    LOG.debug("Started Cloning process for CMR No " + cmrNo);

    String cloningCmrNo = "";
    String processingType = PageManager.getProcessingType(cntry, "U");
    // String processingType = "LD";
    if (CmrConstants.PROCESSING_TYPE_LEGACY_DIRECT.equals(processingType))
      cloningCmrNo = generateCMRNoLegacy(entityManager, cntry, cmrNo);
    else if (PROCESSING_TYPES.contains(processingType))
      cloningCmrNo = generateCMRNoNonLegacy(entityManager, cntry, cmrNo);
    else
      throw new Exception("CMR no generation not supported for this country");

    LOG.debug("Cloning CMR No. " + cloningCmrNo + " generated and assigned.");

    // Cloning CMR No entry in Reserved CMR No table
    ReservedCMRNos reservedCMRNo = new ReservedCMRNos();
    ReservedCMRNosPK reservedCMRNoPK = new ReservedCMRNosPK();

    reservedCMRNoPK.setCmrIssuingCntry(cntry);
    reservedCMRNoPK.setCmrNo(cloningCmrNo);
    reservedCMRNoPK.setMandt(SystemConfiguration.getValue("MANDT"));
    reservedCMRNo.setId(reservedCMRNoPK);

    reservedCMRNo.setStatus("A");
    reservedCMRNo.setComments("Legacy Cloning CMR Generated");
    reservedCMRNo.setCreateTs(SystemUtil.getCurrentTimestamp());
    reservedCMRNo.setLastUpdtTs(SystemUtil.getCurrentTimestamp());
    reservedCMRNo.setCreateBy(BATCH_USER_ID);
    reservedCMRNo.setLastUpdtBy(BATCH_USER_ID);

    createEntity(reservedCMRNo, entityManager);

    // Update CMR_CLONING_QUEUE and set the CLONED_CMR_NO to the generated CMR
    // Update STATUS to 'IN_PROG'.
    cloningQueue.setClonedCmrNo(cloningCmrNo);
    if ("LEGACYSKIP".equalsIgnoreCase(cloningQueue.getCloningProcessCd()))
      cloningQueue.setStatus("LEGACYSKIP");
    else
      cloningQueue.setStatus("IN_PROG");

    updateEntity(cloningQueue, entityManager);

  }

  private void processLegacyCloningProcess(EntityManager entityManager, CmrCloningQueue cloningQueue, String targetCntry) throws Exception {
    String cmrNo = cloningQueue.getId().getCmrNo();
    String cntry = cloningQueue.getId().getCmrIssuingCntry();
    LOG.debug("Started Cloning process for CMR No " + cmrNo);

    MessageTransformer transformer = TransformerManager.getTransformer(cntry);

    LegacyDirectObjectContainer legacyObjectsClone = new LegacyDirectObjectContainer();

    LegacyDirectObjectContainer legacyObjects = LegacyDirectUtil.getLegacyDBValues(entityManager, cntry, cmrNo, false, transformer.hasAddressLinks());

    CmrtCust cust = legacyObjects.getCustomer();

    if (cust == null) {
      // set STATUS to 'STOP' and set ERROR_MSG to 'CMR not found in Legacy'.
      cloningQueue.setStatus("STOP");
      updateEntity(cloningQueue, entityManager);
      throw new Exception("CMR not found in Legacy");
    }

    CloningOverrideUtil overrideUtil = new CloningOverrideUtil();
    // DB2 Cust Table
    CmrtCust custClone = initEmpty(CmrtCust.class);

    // default mapping for DATA and CMRTCUST
    LOG.debug("Mapping default Data values..");
    CmrtCustPK custPkClone = new CmrtCustPK();
    custPkClone.setCustomerNo(cloningQueue.getClonedCmrNo());
    if ("NA".equals(targetCntry))
      custPkClone.setSofCntryCode(cntry);
    else
      custPkClone.setSofCntryCode(targetCntry);

    // copy value same as old from cust object
    PropertyUtils.copyProperties(custClone, cust);
    custClone.setId(custPkClone);

    // override config changes
    List<CloningOverrideMapping> overrideValues = null;
    if ("NA".equals(targetCntry))
      overrideValues = overrideUtil.getOverrideValueFromMapping(cntry);
    else
      overrideValues = overrideUtil.getOverrideValueFromMapping(targetCntry);

    overrideConfigChanges(entityManager, overrideValues, custClone, LEGACY_CUST_TABLE, custPkClone);

    custClone.setCreateTs(SystemUtil.getCurrentTimestamp());
    custClone.setUpdateTs(SystemUtil.getCurrentTimestamp());
    custClone.setUpdStatusTs(SystemUtil.getCurrentTimestamp());

    legacyObjectsClone.setCustomer(custClone);

    // DB2 ADDR Table
    CmrtAddr legacyAddrClone = null;
    CmrtAddrPK legacyAddrPkClone = null;
    for (CmrtAddr addr : legacyObjects.getAddresses()) {
      legacyAddrClone = initEmpty(CmrtAddr.class);
      legacyAddrPkClone = new CmrtAddrPK();
      legacyAddrPkClone.setCustomerNo(cloningQueue.getClonedCmrNo());
      if ("NA".equals(targetCntry))
        legacyAddrPkClone.setSofCntryCode(cntry);
      else
        legacyAddrPkClone.setSofCntryCode(targetCntry);

      legacyAddrPkClone.setAddrNo(addr.getId().getAddrNo());

      // copy value same as old from addr object
      PropertyUtils.copyProperties(legacyAddrClone, addr);
      legacyAddrClone.setId(legacyAddrPkClone);

      overrideConfigChanges(entityManager, overrideValues, legacyAddrClone, LEGACY_ADDR_TABLE, legacyAddrPkClone);

      legacyAddrClone.setCreateTs(SystemUtil.getCurrentTimestamp());
      legacyAddrClone.setUpdateTs(SystemUtil.getCurrentTimestamp());
      legacyObjectsClone.addAddress(legacyAddrClone);
    }

    // DB2 CUST EXT Table
    CmrtCustExt custExtClone = null;
    CmrtCustExtPK custExtPkClone = null;

    boolean isCustExt = transformer.hasCmrtCustExt();
    if (isCustExt) {
      LOG.debug("Mapping default Data values with Legacy CmrtCustExt table.....");
      CmrtCustExt custExt = legacyObjects.getCustomerExt();
      // Initialize the object
      custExtClone = initEmpty(CmrtCustExt.class);
      // default mapping for ADDR and CMRTCEXT
      custExtPkClone = new CmrtCustExtPK();
      custExtPkClone.setCustomerNo(cloningQueue.getClonedCmrNo());
      if ("NA".equals(targetCntry))
        custExtPkClone.setSofCntryCode(cntry);
      else
        custExtPkClone.setSofCntryCode(targetCntry);

      // copy value same as old from cust ext object
      PropertyUtils.copyProperties(custExtClone, custExt);
      custExtClone.setId(custExtPkClone);

      overrideConfigChanges(entityManager, overrideValues, custExtClone, LEGACY_CUSTEXT_TABLE, custExtPkClone);

      custExtClone.setUpdateTs(SystemUtil.getCurrentTimestamp());
      // need to check regarding CODCC and CODCP field
      legacyObjectsClone.setCustomerExt(custExtClone);
    }

    // finally persist all data
    CmrtCust legacyCust = legacyObjectsClone.getCustomer();
    if (legacyCust == null) {
      throw new Exception("Customer record cannot be created.");
    }
    LOG.info("Creating Legacy Records for Cloning CMR No " + cloningQueue.getClonedCmrNo());
    LOG.info("Country: " + legacyCust.getId().getSofCntryCode() + " Cloning CMR No.: " + legacyCust.getId().getCustomerNo());
    createEntity(legacyCust, entityManager);
    if (legacyObjectsClone.getCustomerExt() != null) {
      createEntity(legacyObjectsClone.getCustomerExt(), entityManager);
    }
    for (CmrtAddr legacyAddr : legacyObjectsClone.getAddresses()) {
      createEntity(legacyAddr, entityManager);
    }

    // Update CMR_CLONING_QUEUE sttaus as LEGACY_OK
    cloningQueue.setStatus("LEGACY_OK");
    updateEntity(cloningQueue, entityManager);

  }

  private String generateCMRNoLegacy(EntityManager entityManager, String cmrIssuingCntry, String cmrNo) throws Exception {
    GenerateCMRNoRequest request = new GenerateCMRNoRequest();
    request.setLoc1(cmrIssuingCntry);
    request.setLoc2(cmrIssuingCntry);
    request.setMandt(SystemConfiguration.getValue("MANDT"));
    request.setSystem(GenerateCMRNoClient.SYSTEM_SOF);

    if (this.devMode) {
      request.setDirect("DEV");
    } else {
      request.setDirect("Y");
    }

    MessageTransformer transformer = TransformerManager.getTransformer(cmrIssuingCntry);
    if (transformer != null) {
      transformer.getTargetCountryId(entityManager, request, cmrIssuingCntry, cmrNo);
    }
    int CmrNoVal = Integer.parseInt(cmrNo);
    CloningMapping cMapping = null;
    try {
      cMapping = getCMRNoRangeFromMapping(cmrIssuingCntry, CmrNoVal, request);
    } catch (Exception e) {
      LOG.error("Error occured while digesting xml.", e);
    }

    if (cMapping == null && (CmrNoVal >= 990000 && CmrNoVal <= 999999)) {
      request.setMin(990000);
      request.setMax(999999);
    }

    GenerateCMRNoClient client = CmrServicesFactory.getInstance().createClient(BATCH_SERVICE_URL, GenerateCMRNoClient.class);

    LOG.debug("Generating Cloning CMR No. for Issuing Country " + cmrIssuingCntry);
    GenerateCMRNoResponse response = client.executeAndWrap(request, GenerateCMRNoResponse.class);

    if (response.isSuccess()) {
      return response.getCmrNo();
    } else {
      LOG.error("Cloning CMR No cannot be generated. Error: " + response.getMsg());
      throw new Exception("Cloning CMR No cannot be generated.");
    }
  }

  public CloningMapping getCMRNoRangeFromMapping(String countryId, int cmrNo, GenerateCMRNoRequest generateCMRNoObj) throws Exception {
    CloningMapping mapping = new CloningUtil().getCmrNoRangeFromMapping(countryId);
    if (mapping != null) {
      if (StringUtils.isNotBlank(mapping.getCmrNoRange())) {
        String[] cmrNoRanges = mapping.getCmrNoRange().replaceAll("\n", "").replaceAll(" ", "").split(",");
        for (String cmrNoRange : cmrNoRanges) {
          String[] range = cmrNoRange.split("to");
          if (range.length == 2) {
            int start = Integer.parseInt(range[0]);
            int end = Integer.parseInt(range[1]);
            if (cmrNo >= start && cmrNo <= end) {
              generateCMRNoObj.setMin(start);
              generateCMRNoObj.setMax(end);
              return mapping;
            }
          }
        }
      }
    }

    return null;
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

  private void processCloningRDC(EntityManager entityManager, CmrCloningQueue cloningQueue, String targetCntry) throws Exception {
    LOG.debug("Started Cloning process for CMR No " + cloningQueue.getId().getCmrNo());

    RdcCloningRefn cloningRefn = null;

    String sql = ExternalizedQuery.getSql("BATCH.GET.KNA1_MANDT_CMRNO");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    if ("NA".equals(targetCntry))
      query.setParameter("KATR6", cloningQueue.getId().getCmrIssuingCntry());
    else
      query.setParameter("KATR6", targetCntry);

    query.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
    query.setParameter("CMR_NO", cloningQueue.getId().getCmrNo());
    List<Kna1> kna1List = query.getResults(Kna1.class);
    if (kna1List != null && kna1List.size() > 0) {
      for (Kna1 kna1 : kna1List) {
        cloningRefn = new RdcCloningRefn();
        RdcCloningRefnPK cloningRefnPk = new RdcCloningRefnPK();
        cloningRefnPk.setCmrCloningProcessId(cloningQueue.getId().getCmrCloningProcessId());
        cloningRefnPk.setMandt(SystemConfiguration.getValue("MANDT"));
        cloningRefnPk.setKunnr(kna1.getId().getKunnr());
        cloningRefn.setId(cloningRefnPk);

        if ("NA".equals(targetCntry))
          cloningRefn.setCmrIssuingCntry(cloningQueue.getId().getCmrIssuingCntry());
        else
          cloningRefn.setCmrIssuingCntry(targetCntry);

        cloningRefn.setCmrNo(cloningQueue.getId().getCmrNo());
        cloningRefn.setStatus("P");
        cloningRefn.setCreatedBy(BATCH_USER_ID);
        cloningRefn.setCreateTs(SystemUtil.getCurrentTimestamp());
        cloningRefn.setLastUpdtBy(BATCH_USER_ID);
        cloningRefn.setLastUpdtTs(SystemUtil.getCurrentTimestamp());
        createEntity(cloningRefn, entityManager);

        cloningQueue.setStatus("RDC_INPROG");
        updateEntity(cloningQueue, entityManager);
      }
    } else {
      cloningRefn = new RdcCloningRefn();
      RdcCloningRefnPK cloningRefnPk = new RdcCloningRefnPK();
      cloningRefnPk.setCmrCloningProcessId(cloningQueue.getId().getCmrCloningProcessId());
      cloningRefnPk.setMandt(SystemConfiguration.getValue("MANDT"));
      cloningRefnPk.setKunnr("(missing)");
      cloningRefn.setId(cloningRefnPk);

      if ("NA".equals(targetCntry))
        cloningRefn.setCmrIssuingCntry(cloningQueue.getId().getCmrIssuingCntry());
      else
        cloningRefn.setCmrIssuingCntry(targetCntry);

      cloningRefn.setCmrNo(cloningQueue.getId().getCmrNo());
      cloningRefn.setStatus("E");
      cloningRefn.setErrorMsg("KNA1 record not found");
      cloningRefn.setCreatedBy(BATCH_USER_ID);
      cloningRefn.setCreateTs(SystemUtil.getCurrentTimestamp());
      cloningRefn.setLastUpdtBy(BATCH_USER_ID);
      cloningRefn.setLastUpdtTs(SystemUtil.getCurrentTimestamp());
      createEntity(cloningRefn, entityManager);
    }

  }

  private boolean getKNA1Count(EntityManager entityManager, String targetCntry, String cmrNo) {
    String sql = ExternalizedQuery.getSql("CLONING_KNA1_COUNT");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("KATR6", targetCntry);
    query.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
    query.setParameter("ZZKV_CUSNO", cmrNo);
    int count = query.getSingleResult(Integer.class);
    if (count > 0)
      return true;
    else
      return false;
  }

  private void cmrCloningQueueStatusUpdate(EntityManager entityManager, RdcCloningRefn rdcCloningRefn) throws Exception {
    boolean statusCheck = false;
    String sql = ExternalizedQuery.getSql("CLONING_CMR_STATUS_CHK");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("MANDT", rdcCloningRefn.getId().getMandt());
    query.setParameter("CNTRY", rdcCloningRefn.getCmrIssuingCntry());
    query.setParameter("CMR_NO", rdcCloningRefn.getCmrNo());
    List<Object[]> status = query.getResults();
    if (status.size() == 1) {
      String curentStatus = String.valueOf(status.get(0));
      if ("C".equals(curentStatus))
        statusCheck = true;
    }
    if (statusCheck) {
      CmrCloningQueuePK cloningPk = new CmrCloningQueuePK();
      cloningPk.setCmrCloningProcessId(rdcCloningRefn.getId().getCmrCloningProcessId());
      cloningPk.setCmrIssuingCntry(rdcCloningRefn.getCmrIssuingCntry());
      cloningPk.setCmrNo(rdcCloningRefn.getCmrNo());
      CmrCloningQueue cmrCloningQueue = entityManager.find(CmrCloningQueue.class, cloningPk);
      if (cmrCloningQueue == null) {
        throw new Exception("Cannot locate CmrCloningQueue record");
      }

      cmrCloningQueue.setStatus("COMPLETED");
      updateEntity(cmrCloningQueue, entityManager);
    }

  }

  private void processError(EntityManager entityManager, RdcCloningRefn rdcCloningRefn, CmrCloningQueue cmrCloningQueue, String errorMsg)
      throws Exception {
    LOG.info("Processing error for CMR No: " + rdcCloningRefn.getCmrNo() + ": " + errorMsg);

    if (cmrCloningQueue == null) {
      CmrCloningQueuePK cloningPk = new CmrCloningQueuePK();
      cloningPk.setCmrCloningProcessId(rdcCloningRefn.getId().getCmrCloningProcessId());
      cloningPk.setCmrIssuingCntry(rdcCloningRefn.getCmrIssuingCntry());
      cloningPk.setCmrNo(rdcCloningRefn.getCmrNo());
      cmrCloningQueue = entityManager.find(CmrCloningQueue.class, cloningPk);
      if (cmrCloningQueue == null) {
        throw new Exception("Cannot locate CmrCloningQueue record");
      }
    }

    cmrCloningQueue.setStatus("RDC_ERROR");

    if ("P".equals(rdcCloningRefn.getStatus())) {
      rdcCloningRefn.setStatus("R");
    } else if ("R".equals(rdcCloningRefn.getStatus())) {
      rdcCloningRefn.setStatus("X");
    }

    rdcCloningRefn.setErrorMsg(errorMsg);

  }

  private List<RdcCloningRefn> getPendingCloningRecordsKNA1RDC(EntityManager entityManager, CmrCloningQueue cmrCloningQueue) {
    String sql = ExternalizedQuery.getSql("CLONING_CMR_CLONING_RDC_REF_PENDING");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("ID", cmrCloningQueue.getId().getCmrCloningProcessId());
    query.setParameter("CNTRY", cmrCloningQueue.getId().getCmrIssuingCntry());
    query.setParameter("CMR", cmrCloningQueue.getId().getCmrNo());
    return query.getResults(RdcCloningRefn.class);
  }

  private void processCloningKNA1RDC(EntityManager entityManager, RdcCloningRefn rdcCloningRefn, CloningRDCConfiguration rdcConfig,
      List<CloningOverrideMapping> overrideValues) throws Exception {
    CmrCloningQueuePK cloningPk = new CmrCloningQueuePK();
    cloningPk.setCmrCloningProcessId(rdcCloningRefn.getId().getCmrCloningProcessId());
    cloningPk.setCmrIssuingCntry(rdcCloningRefn.getCmrIssuingCntry());
    cloningPk.setCmrNo(rdcCloningRefn.getCmrNo());
    CmrCloningQueue cmrCloningQueue = entityManager.find(CmrCloningQueue.class, cloningPk);
    if (cmrCloningQueue == null) {
      throw new Exception("Cannot locate CmrCloningQueue record");
    }

    try {
      Kna1 kna1 = null;
      String kunnr = "";
      Timestamp ts = SystemUtil.getCurrentTimestamp();
      kna1 = getKna1ByKunnr(entityManager, SystemConfiguration.getValue("MANDT"), rdcCloningRefn.getId().getKunnr());
      if (kna1 != null) {
        // generate kunnr, prepare the kna1 clone data
        kunnr = generateId(rdcConfig.getTargetMandt(), KUNNR_KEY, entityManager);
        Kna1 kna1Clone = new Kna1();
        Kna1PK kna1PkClone = new Kna1PK();
        kna1PkClone.setMandt(rdcConfig.getTargetMandt());// from config file
        kna1PkClone.setKunnr(kunnr);
        try {
          PropertyUtils.copyProperties(kna1Clone, kna1);
          kna1Clone.setId(kna1PkClone);
          kna1Clone.setBran5("S" + kunnr.substring(1));
          kna1Clone.setZzkvCusno(cmrCloningQueue.getClonedCmrNo());
          kna1Clone.setKatr10(rdcConfig.getKatr10());// from config file
          if (StringUtils.isNotBlank(kna1.getAdrnr())) {
            String adrnr = generateId(rdcConfig.getTargetMandt(), ADRNR_KEY, entityManager);
            kna1Clone.setAdrnr(adrnr);
          }

          overrideConfigChanges(entityManager, overrideValues, kna1Clone, KNA1_TABLE, kna1PkClone);

          kna1Clone.setSapTs(ts);
          kna1Clone.setShadUpdateInd("I");
          kna1Clone.setShadUpdateTs(ts);

          createEntity(kna1Clone, entityManager);
        } catch (Exception e) {
          processError(entityManager, rdcCloningRefn, cmrCloningQueue, "Issue in Copy KNA1 record");
        }
      }

      rdcCloningRefn.setTargetMandt(rdcConfig.getTargetMandt()); // from config
      rdcCloningRefn.setTargetKunnr(kunnr);

    } catch (Exception e) {
      processError(entityManager, rdcCloningRefn, cmrCloningQueue, "Issue in Creating KNA1 record");
    }

    updateEntity(cmrCloningQueue, entityManager);
    updateEntity(rdcCloningRefn, entityManager);
    LOG.debug("CMR No " + rdcCloningRefn.getCmrNo() + " Status: " + rdcCloningRefn.getStatus() + " Message: " + rdcCloningRefn.getErrorMsg());

    partialCommit(entityManager);
  }

  private List<RdcCloningRefn> getPendingRecordsRDCKna1Child(EntityManager entityManager, CmrCloningQueue cmrCloningQueue) {
    String sql = ExternalizedQuery.getSql("CLONING_CMR_CLONING_RDC_REF_PENDING_CHILD");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("ID", cmrCloningQueue.getId().getCmrCloningProcessId());
    query.setParameter("CNTRY", cmrCloningQueue.getId().getCmrIssuingCntry());
    query.setParameter("CMR", cmrCloningQueue.getId().getCmrNo());
    return query.getResults(RdcCloningRefn.class);
  }

  private String generateId(String mandt, String key, EntityManager entityManager) {
    LOG.debug("Calling stored procedure to produce next " + key);
    String generatedId = "";
    // mandt = "030";
    try {
      Connection conn = entityManager.unwrap(Connection.class);
      CallableStatement stmt = null;

      try {
        stmt = conn.prepareCall("CALL NULLID.RDID(?,?,?)");
        stmt.setString(1, mandt);
        stmt.setString(2, key);
        stmt.setString(3, generatedId);
        stmt.registerOutParameter(3, java.sql.Types.VARCHAR);
        stmt.execute();
        generatedId = stmt.getString(3);

        LOG.debug("Stored procedure called result is : " + generatedId);
      } catch (Exception e) {
        LOG.error("Exception occured while retrieving " + key + ".", e);
        try {
          conn.rollback();
          partialRollback(entityManager);
        } catch (SQLException e1) {
          LOG.error("SQLException in CloningProcessService.generateId (connection close)", e);
        }
      } finally {
        try {
          if (stmt != null) {
            stmt.close();
          }

        } catch (SQLException e) {
          LOG.error("SQLException in CloningProcessService.generateId  (statement close)", e);
        }
      }
    } catch (Exception e) {
      LOG.error("An error encountered in CloningProcessService.generateId .", e);
    }
    return generatedId.trim();
  }

  /**
   * Retrieves the KNA1 record by KUNNR and MANDT
   * 
   * @param rdcMgr
   * @param mandt
   * @param kunnr
   * @return
   */
  protected Kna1 getKna1ByKunnr(EntityManager rdcMgr, String mandt, String kunnr) {
    if (StringUtils.isEmpty(kunnr)) {
      return null;
    }
    String sql = ExternalizedQuery.getSql("GET.KNA1.RECORD");
    PreparedQuery query = new PreparedQuery(rdcMgr, sql);
    query.setParameter("MANDT", mandt);
    query.setParameter("KUNNR", kunnr);
    return query.getSingleResult(Kna1.class);

  }

  protected void processKna1Children(EntityManager rdcMgr, Kna1 kna1, Kna1 kna1Clone, CloningRDCConfiguration rdcConfig,
      List<CloningOverrideMapping> overrideValues) throws Exception {

    processKnb1(rdcMgr, kna1, kna1Clone, rdcConfig, overrideValues);

    processKnvv(rdcMgr, kna1, kna1Clone, rdcConfig, overrideValues);

    processKnvi(rdcMgr, kna1, kna1Clone, rdcConfig, overrideValues);

    processKnex(rdcMgr, kna1, kna1Clone, rdcConfig, overrideValues);

    processKnvp(rdcMgr, kna1, kna1Clone, rdcConfig, overrideValues);

    processSadr(rdcMgr, kna1, kna1Clone, rdcConfig, overrideValues);

    processAddlCtryData(rdcMgr, kna1, kna1Clone, rdcConfig, overrideValues);

    processKnvk(rdcMgr, kna1, kna1Clone, rdcConfig, overrideValues);

    processKunnrExt(rdcMgr, kna1, kna1Clone, rdcConfig, overrideValues);

    processKnbk(rdcMgr, kna1, kna1Clone, rdcConfig, overrideValues);

    processKnva(rdcMgr, kna1, kna1Clone, rdcConfig, overrideValues);

    processKnvl(rdcMgr, kna1, kna1Clone, rdcConfig, overrideValues);

    processSizeInfo(rdcMgr, kna1, kna1Clone, rdcConfig, overrideValues);
  }

  private void processKnb1(EntityManager entityManager, Kna1 kna1, Kna1 kna1Clone, CloningRDCConfiguration rdcConfig,
      List<CloningOverrideMapping> overrideValues) throws Exception {

    Knb1 knb1 = null;
    Knb1 knb1Clone = null;
    Timestamp ts = SystemUtil.getCurrentTimestamp();

    if (rdcConfig.getCountriesForKnb1Create().contains(kna1.getKatr6()) && KNB1_ADDR.contains(kna1.getKtokd())) {
      knb1 = getKnb1ByKunnr(entityManager, kna1.getId().getMandt(), kna1.getId().getKunnr());
      knb1Clone = getKnb1ByKunnr(entityManager, kna1Clone.getId().getMandt(), kna1Clone.getId().getKunnr());
      if (knb1 != null && knb1Clone == null) {
        try {
          knb1Clone = new Knb1();
          Knb1PK knb1PKClone = new Knb1PK();
          knb1PKClone.setMandt(kna1Clone.getId().getMandt());
          knb1PKClone.setKunnr(kna1Clone.getId().getKunnr());
          knb1PKClone.setBukrs(knb1.getId().getBukrs());
          PropertyUtils.copyProperties(knb1Clone, knb1);

          // knb1Clone.setId(knb1PKClone);

          overrideConfigChanges(entityManager, overrideValues, knb1Clone, "KNB1", knb1PKClone);

          knb1Clone.setId(knb1PKClone);

          knb1Clone.setSapTs(ts);
          knb1Clone.setShadUpdateInd("I");
          knb1Clone.setShadUpdateTs(ts);

          createEntity(knb1Clone, entityManager);
        } catch (Exception e) {
          LOG.debug("Error in copy knb1");
        }
      } else {
        LOG.info("KNB1 record not exist with KUNNR " + kna1.getId().getKunnr());
      }

      logObject(knb1Clone);
    }

  }

  public Knb1 getKnb1ByKunnr(EntityManager rdcMgr, String mandt, String kunnr) throws Exception {
    String sql = ExternalizedQuery.getSql("GET.KNB1.RECORD");
    PreparedQuery query = new PreparedQuery(rdcMgr, sql);
    query.setParameter("MANDT", mandt);
    query.setParameter("KUNNR", kunnr);
    return query.getSingleResult(Knb1.class);
  }

  private void processKnvv(EntityManager entityManager, Kna1 kna1, Kna1 kna1Clone, CloningRDCConfiguration rdcConfig,
      List<CloningOverrideMapping> overrideValues) throws Exception {

    Knvv knvv = null;
    Knvv knvvClone = null;
    Timestamp ts = SystemUtil.getCurrentTimestamp();

    if (rdcConfig.getCountriesForKnvvCreate().contains(kna1.getKatr6())) {
      knvv = getKnvvByKunnr(entityManager, kna1.getId().getMandt(), kna1.getId().getKunnr());
      knvvClone = getKnvvByKunnr(entityManager, kna1Clone.getId().getMandt(), kna1Clone.getId().getKunnr());
      if (knvv != null && knvvClone == null) {
        try {
          knvvClone = new Knvv();
          KnvvPK knvvPKClone = new KnvvPK();
          knvvPKClone.setKunnr(kna1Clone.getId().getKunnr());
          knvvPKClone.setMandt(kna1Clone.getId().getMandt());
          knvvPKClone.setSpart(knvv.getId().getSpart());
          knvvPKClone.setVkorg(knvv.getId().getVkorg());
          knvvPKClone.setVtweg(knvv.getId().getVtweg());

          PropertyUtils.copyProperties(knvvClone, knvv);

          overrideConfigChanges(entityManager, overrideValues, knvvClone, "KNVV", knvvPKClone);

          knvvClone.setId(knvvPKClone);

          knvvClone.setSapTs(ts);
          knvvClone.setShadUpdateInd("I");
          knvvClone.setShadUpdateTs(ts);

          createEntity(knvvClone, entityManager);
        } catch (Exception e) {
          LOG.debug("Error in copy knvv");
        }
      } else {
        LOG.info("KNVV record not exist with KUNNR " + kna1.getId().getKunnr());
      }

      logObject(knvvClone);
    }

  }

  public Knvv getKnvvByKunnr(EntityManager rdcMgr, String mandt, String kunnr) throws Exception {
    String sql = ExternalizedQuery.getSql("GET.KNVV.RECORD");
    PreparedQuery query = new PreparedQuery(rdcMgr, sql);
    query.setParameter("MANDT", mandt);
    query.setParameter("KUNNR", kunnr);
    return query.getSingleResult(Knvv.class);
  }

  private void processKnex(EntityManager entityManager, Kna1 kna1, Kna1 kna1Clone, CloningRDCConfiguration rdcConfig,
      List<CloningOverrideMapping> overrideValues) throws Exception {

    Knex knex = null;
    Knex knexClone = null;
    Timestamp ts = SystemUtil.getCurrentTimestamp();

    if (rdcConfig.getCountriesForKnexCreate().contains(kna1.getKatr6())) {
      knex = getKnexByKunnr(entityManager, kna1.getId().getMandt(), kna1.getId().getKunnr());
      knexClone = getKnexByKunnr(entityManager, kna1Clone.getId().getMandt(), kna1Clone.getId().getKunnr());
      if (knex != null && knexClone == null) {
        try {
          knexClone = new Knex();
          KnexPK knexPKClone = new KnexPK();
          knexPKClone.setKunnr(kna1Clone.getId().getKunnr());
          knexPKClone.setLndex(knex.getId().getLndex());
          knexPKClone.setMandt(kna1Clone.getId().getMandt());

          PropertyUtils.copyProperties(knexClone, knex);

          overrideConfigChanges(entityManager, overrideValues, knexClone, "KNEX", knexPKClone);

          knexClone.setId(knexPKClone);

          knexClone.setSapTs(ts);
          knexClone.setShadUpdateInd("I");
          knexClone.setShadUpdateTs(ts);

          createEntity(knexClone, entityManager);
        } catch (Exception e) {
          LOG.debug("Error in copy knex");
        }
      } else {
        LOG.info("KNEX record not exist with KUNNR " + kna1.getId().getKunnr());
      }

      logObject(knexClone);
    }

  }

  public Knex getKnexByKunnr(EntityManager rdcMgr, String mandt, String kunnr) throws Exception {
    String sql = ExternalizedQuery.getSql("GET.KNEX.RECORD");
    PreparedQuery query = new PreparedQuery(rdcMgr, sql);
    query.setParameter("MANDT", mandt);
    query.setParameter("KUNNR", kunnr);
    return query.getSingleResult(Knex.class);
  }

  private void processSadr(EntityManager entityManager, Kna1 kna1, Kna1 kna1Clone, CloningRDCConfiguration rdcConfig,
      List<CloningOverrideMapping> overrideValues) throws Exception {

    Sadr sadr = null;
    Sadr sadrClone = null;
    Timestamp ts = SystemUtil.getCurrentTimestamp();

    if (rdcConfig.getCountriesForSadrCreate().contains(kna1.getKatr6())) {
      sadr = getSadrByAdrnr(entityManager, kna1);
      sadrClone = getSadrByAdrnr(entityManager, kna1Clone);
      if (sadr != null && sadrClone == null) {
        try {
          sadrClone = new Sadr();
          SadrPK sadrPKClone = new SadrPK();
          sadrPKClone.setAdrnr(kna1Clone.getAdrnr());
          sadrPKClone.setMandt(kna1Clone.getId().getMandt());
          sadrPKClone.setNatio(sadr.getId().getNatio());

          PropertyUtils.copyProperties(sadrClone, sadr);

          overrideConfigChanges(entityManager, overrideValues, sadrClone, "SADR", sadrPKClone);

          sadrClone.setId(sadrPKClone);

          sadrClone.setSapTs(ts);
          sadrClone.setShadUpdateInd("I");
          sadrClone.setShadUpdateTs(ts);

          createEntity(sadrClone, entityManager);
        } catch (Exception e) {
          LOG.debug("Error in copy sadr");
        }
      } else {
        LOG.info("SADR record not exist with ADRNR " + kna1.getAdrnr());
      }

      logObject(sadrClone);
    }

  }

  public Sadr getSadrByAdrnr(EntityManager rdcMgr, Kna1 kna1) throws Exception {
    String sql = ExternalizedQuery.getSql("GET.SADR.RECORD");
    PreparedQuery query = new PreparedQuery(rdcMgr, sql);
    query.setParameter("MANDT", kna1.getId().getMandt());
    query.setParameter("ADRNR", kna1.getAdrnr());
    return query.getSingleResult(Sadr.class);
  }

  private void processKnvi(EntityManager entityManager, Kna1 kna1, Kna1 kna1Clone, CloningRDCConfiguration rdcConfig,
      List<CloningOverrideMapping> overrideValues) throws Exception {

    List<Knvi> knvi = null;
    List<Knvi> knviClone = null;
    Knvi knviCloneInsert = null;
    Timestamp ts = SystemUtil.getCurrentTimestamp();

    if (rdcConfig.getCountriesForKnviCreate().contains(kna1.getKatr6())) {
      knvi = getKnviByKunnr(entityManager, kna1.getId().getMandt(), kna1.getId().getKunnr());
      knviClone = getKnviByKunnr(entityManager, kna1Clone.getId().getMandt(), kna1Clone.getId().getKunnr());
      if (knvi != null && knviClone.size() == 0) {
        try {
          for (Knvi currentKnvi : knvi) {
            knviCloneInsert = new Knvi();
            KnviPK knviPKClone = new KnviPK();
            knviPKClone.setAland(currentKnvi.getId().getAland());
            knviPKClone.setKunnr(kna1Clone.getId().getKunnr());
            knviPKClone.setMandt(kna1Clone.getId().getMandt());
            knviPKClone.setTatyp(currentKnvi.getId().getTatyp());

            PropertyUtils.copyProperties(knviCloneInsert, currentKnvi);

            overrideConfigChanges(entityManager, overrideValues, knviCloneInsert, "KNVI", knviPKClone);

            knviCloneInsert.setId(knviPKClone);

            knviCloneInsert.setSapTs(ts);
            knviCloneInsert.setShadUpdateInd("I");
            knviCloneInsert.setShadUpdateTs(ts);

            createEntity(knviCloneInsert, entityManager);
          }
        } catch (Exception e) {
          LOG.debug("Error in copy knvi");
        }
      } else {
        LOG.info("KNVI record not exist with KUNNR " + kna1.getId().getKunnr());
      }

      logObject(knviCloneInsert);
    }

  }

  public List<Knvi> getKnviByKunnr(EntityManager rdcMgr, String mandt, String kunnr) throws Exception {
    String sql = ExternalizedQuery.getSql("GET.KNVI.CURRENT");
    PreparedQuery query = new PreparedQuery(rdcMgr, sql);
    query.setParameter("MANDT", mandt);
    query.setParameter("KUNNR", kunnr);
    return query.getResults(Knvi.class);
  }

  private void processKnvk(EntityManager entityManager, Kna1 kna1, Kna1 kna1Clone, CloningRDCConfiguration rdcConfig,
      List<CloningOverrideMapping> overrideValues) throws Exception {

    Knvk knvk = null;
    Knvk knvkClone = null;
    Timestamp ts = SystemUtil.getCurrentTimestamp();

    if (rdcConfig.getCountriesForKnvkCreate().contains(kna1.getKatr6())) {
      knvk = getKnvkByKunnr(entityManager, kna1.getId().getMandt(), kna1.getId().getKunnr());
      knvkClone = getKnvkByKunnr(entityManager, kna1Clone.getId().getMandt(), kna1Clone.getId().getKunnr());
      if (knvk != null && knvkClone == null) {
        try {
          knvkClone = new Knvk();
          KnvkPK knvkPKClone = new KnvkPK();
          knvkPKClone.setMandt(kna1Clone.getId().getMandt());
          String parnr = generateId(kna1Clone.getId().getMandt(), PARNR_KEY, entityManager);
          knvkPKClone.setParnr(parnr);

          PropertyUtils.copyProperties(knvkClone, knvk);

          overrideConfigChanges(entityManager, overrideValues, knvkClone, "KNVK", knvkPKClone);

          knvkClone.setId(knvkPKClone);

          knvkClone.setSapTs(ts);
          knvkClone.setShadUpdateInd("I");
          knvkClone.setShadUpdateTs(ts);

          createEntity(knvkClone, entityManager);
        } catch (Exception e) {
          LOG.debug("Error in copy knvk");
        }
      } else {
        LOG.info("KNVK record not exist with KUNNR " + kna1.getId().getKunnr());
      }

      logObject(knvkClone);
    }

  }

  public Knvk getKnvkByKunnr(EntityManager rdcMgr, String mandt, String kunnr) throws Exception {
    String sql = ExternalizedQuery.getSql("GET.KNVK.RECORD");
    PreparedQuery query = new PreparedQuery(rdcMgr, sql);
    query.setParameter("MANDT", mandt);
    query.setParameter("KUNNR", kunnr);
    return query.getSingleResult(Knvk.class);
  }

  private void processKnvp(EntityManager entityManager, Kna1 kna1, Kna1 kna1Clone, CloningRDCConfiguration rdcConfig,
      List<CloningOverrideMapping> overrideValues) throws Exception {

    List<Knvp> knvp = null;
    List<Knvp> knvpClone = null;
    Knvp knvpCloneInsert = null;
    Timestamp ts = SystemUtil.getCurrentTimestamp();

    if (rdcConfig.getCountriesForKnvpCreate().contains(kna1.getKatr6())) {
      knvp = getKnvpByKunnr(entityManager, kna1.getId().getMandt(), kna1.getId().getKunnr());
      knvpClone = getKnvpByKunnr(entityManager, kna1Clone.getId().getMandt(), kna1Clone.getId().getKunnr());
      if (knvp != null && knvpClone.size() == 0) {
        try {
          for (Knvp currentKnvp : knvp) {
            knvpCloneInsert = new Knvp();
            KnvpPK knvpPKClone = new KnvpPK();
            knvpPKClone.setKunnr(kna1Clone.getId().getKunnr());
            knvpPKClone.setMandt(kna1Clone.getId().getMandt());
            knvpPKClone.setParvw(currentKnvp.getId().getParvw());
            knvpPKClone.setParza(currentKnvp.getId().getParza());
            knvpPKClone.setSpart(currentKnvp.getId().getSpart());
            knvpPKClone.setVkorg(currentKnvp.getId().getVkorg());
            knvpPKClone.setVtweg(currentKnvp.getId().getVtweg());

            PropertyUtils.copyProperties(knvpCloneInsert, currentKnvp);

            overrideConfigChanges(entityManager, overrideValues, knvpCloneInsert, "KNVP", knvpPKClone);

            knvpCloneInsert.setId(knvpPKClone);

            knvpCloneInsert.setSapTs(ts);
            knvpCloneInsert.setShadUpdateInd("I");
            knvpCloneInsert.setShadUpdateTs(ts);

            createEntity(knvpCloneInsert, entityManager);
          }
        } catch (Exception e) {
          LOG.debug("Error in copy knvp");
        }
      } else {
        LOG.info("KNVP record not exist with KUNNR " + kna1.getId().getKunnr());
      }

      logObject(knvpCloneInsert);
    }

  }

  public List<Knvp> getKnvpByKunnr(EntityManager rdcMgr, String mandt, String kunnr) throws Exception {
    String sql = ExternalizedQuery.getSql("GET.KNVP.RECORD");
    PreparedQuery query = new PreparedQuery(rdcMgr, sql);
    query.setParameter("MANDT", mandt);
    query.setParameter("KUNNR", kunnr);
    return query.getResults(Knvp.class);
  }

  private void processAddlCtryData(EntityManager entityManager, Kna1 kna1, Kna1 kna1Clone, CloningRDCConfiguration rdcConfig,
      List<CloningOverrideMapping> overrideValues) throws Exception {

    Addlctrydata addlctrydata = null;
    Addlctrydata addlctrydataClone = null;
    Timestamp ts = SystemUtil.getCurrentTimestamp();

    if (rdcConfig.getCountriesForAddlCtryDataCreate().contains(kna1.getKatr6())) {
      addlctrydata = getAddlCtryDataByKunnr(entityManager, kna1.getId().getMandt(), kna1.getId().getKunnr());
      addlctrydataClone = getAddlCtryDataByKunnr(entityManager, kna1Clone.getId().getMandt(), kna1Clone.getId().getKunnr());
      if (addlctrydata != null && addlctrydataClone == null) {
        try {
          addlctrydataClone = new Addlctrydata();
          AddlctrydataPK pk = new AddlctrydataPK();
          pk.setFieldName(addlctrydata.getId().getFieldName());
          pk.setKunnr(kna1Clone.getId().getKunnr());
          pk.setMandt(kna1Clone.getId().getMandt());

          PropertyUtils.copyProperties(addlctrydataClone, addlctrydata);

          overrideConfigChanges(entityManager, overrideValues, addlctrydataClone, "ADDLCTRYDATA", pk);

          addlctrydataClone.setId(pk);

          addlctrydataClone.setCreateDt(ts);
          addlctrydataClone.setUpdateDt(ts);

          createEntity(addlctrydataClone, entityManager);
        } catch (Exception e) {
          LOG.debug("Error in copy addlctrydata");
        }
      } else {
        LOG.info("ADDLCTRYDATA record not exist with KUNNR " + kna1.getId().getKunnr());
      }

      logObject(addlctrydataClone);
    }

  }

  public Addlctrydata getAddlCtryDataByKunnr(EntityManager rdcMgr, String mandt, String kunnr) throws Exception {
    String sql = ExternalizedQuery.getSql("GET.ADDLCTRYDATA.RECORD");
    PreparedQuery query = new PreparedQuery(rdcMgr, sql);
    query.setParameter("MANDT", mandt);
    query.setParameter("KUNNR", kunnr);
    return query.getSingleResult(Addlctrydata.class);
  }

  private void processKunnrExt(EntityManager entityManager, Kna1 kna1, Kna1 kna1Clone, CloningRDCConfiguration rdcConfig,
      List<CloningOverrideMapping> overrideValues) throws Exception {

    KunnrExt kunnrExt = null;
    KunnrExt kunnrExtClone = null;
    Timestamp ts = SystemUtil.getCurrentTimestamp();

    if (rdcConfig.getCountriesForKunnrExtCreate().contains(kna1.getKatr6())) {
      kunnrExt = getKunnrExtByKunnr(entityManager, kna1.getId().getMandt(), kna1.getId().getKunnr());
      kunnrExtClone = getKunnrExtByKunnr(entityManager, kna1Clone.getId().getMandt(), kna1Clone.getId().getKunnr());
      if (kunnrExt != null && kunnrExtClone == null) {
        try {
          kunnrExtClone = new KunnrExt();
          KunnrExtPK pk = new KunnrExtPK();
          pk.setKunnr(kna1Clone.getId().getKunnr());
          pk.setMandt(kna1Clone.getId().getMandt());

          PropertyUtils.copyProperties(kunnrExtClone, kunnrExt);

          overrideConfigChanges(entityManager, overrideValues, kunnrExtClone, "KUNNR_EXT", pk);

          kunnrExtClone.setId(pk);

          kunnrExtClone.setCreateTs(ts);
          kunnrExtClone.setUpdateInd("I");
          kunnrExtClone.setUpdateTs(ts);

          createEntity(kunnrExtClone, entityManager);
        } catch (Exception e) {
          LOG.debug("Error in copy kunnrext");
        }
      } else {
        LOG.info("KUNNR_EXT record not exist with KUNNR " + kna1.getId().getKunnr());
      }

      logObject(kunnrExtClone);
    }

  }

  public KunnrExt getKunnrExtByKunnr(EntityManager rdcMgr, String mandt, String kunnr) throws Exception {
    String sql = ExternalizedQuery.getSql("GET.KUNNR_EXT.RECORD");
    PreparedQuery query = new PreparedQuery(rdcMgr, sql);
    query.setParameter("MANDT", mandt);
    query.setParameter("KUNNR", kunnr);
    return query.getSingleResult(KunnrExt.class);
  }

  private void processKnbk(EntityManager entityManager, Kna1 kna1, Kna1 kna1Clone, CloningRDCConfiguration rdcConfig,
      List<CloningOverrideMapping> overrideValues) throws Exception {

    Knbk knbk = null;
    Knbk knbkClone = null;
    Timestamp ts = SystemUtil.getCurrentTimestamp();

    if (rdcConfig.getCountriesForKnbkCreate().contains(kna1.getKatr6())) {
      knbk = getKnbkByKunnr(entityManager, kna1.getId().getMandt(), kna1.getId().getKunnr());
      knbkClone = getKnbkByKunnr(entityManager, kna1Clone.getId().getMandt(), kna1Clone.getId().getKunnr());
      if (knbk != null && knbkClone == null) {
        try {
          knbkClone = new Knbk();
          KnbkPK knbkPKClone = new KnbkPK();
          knbkPKClone.setBankl(knbk.getId().getBankl());
          knbkPKClone.setBankn(knbk.getId().getBankn());
          knbkPKClone.setBanks(knbk.getId().getBanks());
          knbkPKClone.setKunnr(kna1Clone.getId().getKunnr());
          knbkPKClone.setMandt(kna1Clone.getId().getMandt());

          PropertyUtils.copyProperties(knbkClone, knbk);

          overrideConfigChanges(entityManager, overrideValues, knbkClone, "KNBK", knbkPKClone);

          knbkClone.setId(knbkPKClone);

          knbkClone.setSapTs(ts);
          knbkClone.setShadUpdateInd("I");
          knbkClone.setShadUpdateTs(ts);

          createEntity(knbkClone, entityManager);
        } catch (Exception e) {
          LOG.debug("Error in copy knbk");
        }
      } else {
        LOG.info("KNBK record not exist with KUNNR " + kna1.getId().getKunnr());
      }

      logObject(knbkClone);
    }

  }

  public Knbk getKnbkByKunnr(EntityManager rdcMgr, String mandt, String kunnr) throws Exception {
    String sql = ExternalizedQuery.getSql("GET.KNBK.RECORD");
    PreparedQuery query = new PreparedQuery(rdcMgr, sql);
    query.setParameter("MANDT", mandt);
    query.setParameter("KUNNR", kunnr);
    return query.getSingleResult(Knbk.class);
  }

  private void processKnva(EntityManager entityManager, Kna1 kna1, Kna1 kna1Clone, CloningRDCConfiguration rdcConfig,
      List<CloningOverrideMapping> overrideValues) throws Exception {

    Knva knva = null;
    Knva knvaClone = null;
    Timestamp ts = SystemUtil.getCurrentTimestamp();

    if (rdcConfig.getCountriesForKnvaCreate().contains(kna1.getKatr6())) {
      knva = getKnvaByKunnr(entityManager, kna1.getId().getMandt(), kna1.getId().getKunnr());
      knvaClone = getKnvaByKunnr(entityManager, kna1Clone.getId().getMandt(), kna1Clone.getId().getKunnr());
      if (knva != null && knvaClone == null) {
        try {
          knvaClone = new Knva();
          KnvaPK knvaPKClone = new KnvaPK();
          knvaPKClone.setAblad(knva.getId().getAblad());
          knvaPKClone.setKunnr(kna1Clone.getId().getKunnr());
          knvaPKClone.setMandt(kna1Clone.getId().getMandt());

          PropertyUtils.copyProperties(knvaClone, knva);

          overrideConfigChanges(entityManager, overrideValues, knvaClone, "KNVA", knvaPKClone);

          knvaClone.setId(knvaPKClone);

          knvaClone.setSapTs(ts);
          knvaClone.setShadUpdateInd("I");
          knvaClone.setShadUpdateTs(ts);

          createEntity(knvaClone, entityManager);
        } catch (Exception e) {
          LOG.debug("Error in copy knva");
        }
      } else {
        LOG.info("KNVA record not exist with KUNNR " + kna1.getId().getKunnr());
      }

      logObject(knvaClone);
    }

  }

  public Knva getKnvaByKunnr(EntityManager rdcMgr, String mandt, String kunnr) throws Exception {
    String sql = ExternalizedQuery.getSql("GET.KNVA.RECORD");
    PreparedQuery query = new PreparedQuery(rdcMgr, sql);
    query.setParameter("MANDT", mandt);
    query.setParameter("KUNNR", kunnr);
    return query.getSingleResult(Knva.class);
  }

  private void processKnvl(EntityManager entityManager, Kna1 kna1, Kna1 kna1Clone, CloningRDCConfiguration rdcConfig,
      List<CloningOverrideMapping> overrideValues) throws Exception {

    Knvl knvl = null;
    Knvl knvlClone = null;
    Timestamp ts = SystemUtil.getCurrentTimestamp();

    if (rdcConfig.getCountriesForKnvlCreate().contains(kna1.getKatr6())) {
      knvl = getKnvlByKunnr(entityManager, kna1.getId().getMandt(), kna1.getId().getKunnr());
      knvlClone = getKnvlByKunnr(entityManager, kna1Clone.getId().getMandt(), kna1Clone.getId().getKunnr());
      if (knvl != null && knvlClone == null) {
        try {
          knvlClone = new Knvl();
          KnvlPK knvlPKClone = new KnvlPK();
          knvlPKClone.setAland(knvl.getId().getAland());
          knvlPKClone.setKunnr(kna1Clone.getId().getKunnr());
          knvlPKClone.setLicnr(knvl.getId().getLicnr());
          knvlPKClone.setMandt(kna1Clone.getId().getMandt());
          knvlPKClone.setTatyp(knvl.getId().getTatyp());

          PropertyUtils.copyProperties(knvlClone, knvl);

          overrideConfigChanges(entityManager, overrideValues, knvlClone, "KNVL", knvlPKClone);

          knvlClone.setId(knvlPKClone);

          knvlClone.setSapTs(ts);
          knvlClone.setShadUpdateInd("I");
          knvlClone.setShadUpdateTs(ts);

          createEntity(knvlClone, entityManager);
        } catch (Exception e) {
          LOG.debug("Error in copy knvl");
        }
      } else {
        LOG.info("KNVL record not exist with KUNNR " + kna1.getId().getKunnr());
      }

      logObject(knvlClone);
    }

  }

  public Knvl getKnvlByKunnr(EntityManager rdcMgr, String mandt, String kunnr) throws Exception {
    String sql = ExternalizedQuery.getSql("GET.KNVL.RECORD");
    PreparedQuery query = new PreparedQuery(rdcMgr, sql);
    query.setParameter("MANDT", mandt);
    query.setParameter("KUNNR", kunnr);
    return query.getSingleResult(Knvl.class);
  }

  private void processSizeInfo(EntityManager entityManager, Kna1 kna1, Kna1 kna1Clone, CloningRDCConfiguration rdcConfig,
      List<CloningOverrideMapping> overrideValues) throws Exception {

    Sizeinfo sizeInfo = null;
    Sizeinfo sizeInfoClone = null;
    Timestamp ts = SystemUtil.getCurrentTimestamp();

    if (rdcConfig.getCountriesForSizeInfoCreate().contains(kna1.getKatr6())) {
      sizeInfo = getSizeInfoByKunnr(entityManager, kna1.getId().getMandt(), kna1.getId().getKunnr());
      sizeInfoClone = getSizeInfoByKunnr(entityManager, kna1Clone.getId().getMandt(), kna1Clone.getId().getKunnr());
      if (sizeInfo != null && sizeInfoClone == null) {
        try {
          sizeInfoClone = new Sizeinfo();
          SizeinfoPK pk = new SizeinfoPK();
          pk.setKunnr(kna1Clone.getId().getKunnr());
          pk.setMandt(kna1Clone.getId().getMandt());
          pk.setSizeunittype(sizeInfo.getId().getSizeunittype());

          PropertyUtils.copyProperties(sizeInfoClone, sizeInfo);

          overrideConfigChanges(entityManager, overrideValues, sizeInfoClone, "SIZEINFO", pk);

          sizeInfoClone.setId(pk);

          sizeInfoClone.setChgTs(ts);
          sizeInfoClone.setCreateDate(ts);

          createEntity(sizeInfoClone, entityManager);
        } catch (Exception e) {
          LOG.debug("Error in copy sizeInfo");
        }
      } else {
        LOG.info("SIZEINFO record not exist with KUNNR " + kna1.getId().getKunnr());
      }

      logObject(sizeInfoClone);
    }

  }

  public Sizeinfo getSizeInfoByKunnr(EntityManager rdcMgr, String mandt, String kunnr) throws Exception {
    String sql = ExternalizedQuery.getSql("GET.SIZEINFO.RECORD");
    PreparedQuery query = new PreparedQuery(rdcMgr, sql);
    query.setParameter("MANDT", mandt);
    query.setParameter("KUNNR", kunnr);
    return query.getSingleResult(Sizeinfo.class);
  }

  /**
   * Logs Object details
   * 
   * @param object
   */
  protected void logObject(Object object) {
    if (LOG.isTraceEnabled()) {
      try {
        ObjectMapper mapper = new ObjectMapper();
        String objJson = mapper.writeValueAsString(object);
        LOG.trace(object.getClass().getSimpleName() + " Details:");
        LOG.trace(objJson);
      } catch (Exception e) {
        LOG.warn("Cannot log " + object.getClass().getSimpleName() + " object details.", e);
      }
    }
  }

  private void processLegacyCloningDupCreate(EntityManager entityManager, CmrCloningQueue cloningQueue, String targetCntry) throws Exception {
    LOG.debug("Inside legacy duplicate create check for CMR No : " + cloningQueue.getId().getCmrNo() + ": Country : "
        + cloningQueue.getId().getCmrIssuingCntry());

    LegacyDirectObjectContainer legacyObjects = LegacyDirectUtil.getLegacyDBValues(entityManager, targetCntry, cloningQueue.getId().getCmrNo(), false,
        false);

    CmrtCust cust = legacyObjects.getCustomer();

    if (cust == null) {
      LOG.debug("No duplicate cmr in legacy for target country :" + targetCntry);
    } else {
      processLegacyCloningProcess(entityManager, cloningQueue, targetCntry);
    }

  }

  private void processRdcCloningDupCreate(EntityManager entityManager, CmrCloningQueue cloningQueue, String targetCntry) throws Exception {
    LOG.debug("Inside rdc duplicate create check for CMR No : " + cloningQueue.getId().getCmrNo() + ": Country : "
        + cloningQueue.getId().getCmrIssuingCntry());

    boolean rdcCount = getKNA1Count(entityManager, targetCntry, cloningQueue.getId().getCmrNo());

    if (rdcCount) {
      processCloningRDC(entityManager, cloningQueue, targetCntry);
    } else {
      LOG.debug("No duplicate cmr in rdc for target country :" + targetCntry);
    }

  }

  private void overrideConfigChanges(EntityManager entityManager, List<CloningOverrideMapping> overrideValues, Object entity, String table,
      Object entityPK) throws Exception {
    LOG.debug("Inside ovverideConfigChanges method with table : " + table + " Entity : " + entity.getClass().getName());
    boolean pkUpdate = false;
    if (overrideValues != null && overrideValues.size() > 0) {
      if (overrideValues.size() == 1) {
        if (table.equalsIgnoreCase(overrideValues.get(0).getTable())) {
          pkUpdate = checkPrimaryKeyOverride(entityManager, table, overrideValues.get(0).getField());
          if (pkUpdate)
            setEntityValue(entityPK, overrideValues.get(0).getField(), overrideValues.get(0).getValue());
          else
            setEntityValue(entity, overrideValues.get(0).getField(), overrideValues.get(0).getValue());
        }
      } else {
        for (CloningOverrideMapping mapping : overrideValues) {
          if (table.equalsIgnoreCase(mapping.getTable())) {
            pkUpdate = checkPrimaryKeyOverride(entityManager, table, mapping.getField());
            if (pkUpdate)
              setEntityValue(entityPK, mapping.getField(), mapping.getValue());
            else
              setEntityValue(entity, mapping.getField(), mapping.getValue());
          }
        }
      }
    }

  }

  private boolean checkPrimaryKeyOverride(EntityManager entityManager, String table, String field) throws Exception {
    LOG.debug("Inside checkPrimaryKeyOverride method ");
    if ("KNB1".equalsIgnoreCase(table) && "BUKRS".equalsIgnoreCase(field))
      return true;
    else if ("KNVV".equalsIgnoreCase(table) && "VKORG".equalsIgnoreCase(field))
      return true;
    else if ("KNVI".equalsIgnoreCase(table) && (KNVI_PK.contains(field.toLowerCase())))
      return true;
    else if ("KNVP".equalsIgnoreCase(table) && (KNVP_PK.contains(field.toLowerCase())))
      return true;
    else if ("KNEX".equalsIgnoreCase(table) && "lndex".equalsIgnoreCase(field))
      return true;
    else if ("ADDLCTRYDATA".equalsIgnoreCase(table) && "fieldName".equalsIgnoreCase(field))
      return true;
    else if ("KNBK".equalsIgnoreCase(table) && (KNBK_PK.contains(field.toLowerCase())))
      return true;
    else if ("KNVA".equalsIgnoreCase(table) && "ablad".equalsIgnoreCase(field))
      return true;
    else if ("KNVL".equalsIgnoreCase(table) && (KNVL_PK.contains(field.toLowerCase())))
      return true;
    else if ("SIZEINFO".equalsIgnoreCase(table) && "sizeunittype".equalsIgnoreCase(field))
      return true;

    return false;

  }

  /**
   * Sets the data value by finding the relevant column having the given field
   * name
   * 
   * @param entity
   * @param fieldName
   * @param value
   */
  protected void setEntityValue(Object entity, String fieldName, Object value) {
    boolean fieldMatched = false;
    for (Field field : entity.getClass().getDeclaredFields()) {
      // match the entity name to field name
      fieldMatched = false;
      Column col = field.getAnnotation(Column.class);
      if (col != null && fieldName.toUpperCase().equals(col.name().toUpperCase())) {
        fieldMatched = true;
      } else if (field.getName().toUpperCase().equals(fieldName.toUpperCase())) {
        fieldMatched = true;
      }
      if (fieldMatched) {
        try {
          field.setAccessible(true);
          try {
            Method set = entity.getClass().getDeclaredMethod("set" + (field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1)),
                value != null ? value.getClass() : String.class);
            if (set != null) {
              set.invoke(entity, value);
            }
          } catch (Exception e) {
            LOG.trace("Field " + fieldName + " cannot be set vis method.", e);
            field.set(entity, value);
          }
        } catch (Exception e) {
          LOG.trace("Field " + fieldName + " cannot be assigned. ", e);
        }
      }
    }
  }

  private String generateCMRNoNonLegacy(EntityManager entityManager, String cmrIssuingCntry, String cmrNo) throws Exception {
    LOG.debug("Inside generateCMRNoNonLegacy method ");
    String mandt = SystemConfiguration.getValue("MANDT");
    CloningUtil cUtil = new CloningUtil();
    String kukla = cUtil.getKuklaFromCMR(entityManager, cmrIssuingCntry, cmrNo, mandt);
    GEOHandler geoHandler = RequestUtils.getGEOHandler(cmrIssuingCntry);
    String generatedCmrNo = "";
    if (geoHandler != null) {
      generatedCmrNo = geoHandler.getCMRNo(entityManager, kukla, mandt, cmrIssuingCntry, cmrNo);
      if (StringUtils.isNotBlank(generatedCmrNo))
        return generatedCmrNo;
      else {
        LOG.error("CMR No cannot be generated due to some Error");
        throw new Exception("CMR No cannot be generated.");
      }
    }
    return generatedCmrNo;
  }

  @Override
  public Boolean executeBatchForRequests(EntityManager entityManager, List<CmrCloningQueue> pendingLists) throws Exception {
    // TODO Auto-generated method stub
    LOG.info("Inside executeBatchForRequests method");
    // CmrCloningQueue pendingCloning = null;
    MessageTransformer transformer = null;
    String cntryDup = "";

    for (CmrCloningQueue cloningQueue : pendingLists) {
      switch (cloningQueue.getStatus()) {
      case "PENDING":
        try {
          processCloningRecord(entityManager, cloningQueue);
        } catch (Exception e) {
          partialRollback(entityManager);
          LOG.error("Unexpected error occurred during reservedcmr and cloning queue insertion " + cloningQueue.getId().getCmrNo(), e);
          // processError(entityManager, null, e.getMessage());
          break;
        }
        partialCommit(entityManager);

      case "IN_PROG":
      case "LEGACY_ERR":
        try {
          processLegacyCloningProcess(entityManager, cloningQueue, "NA");
          transformer = TransformerManager.getTransformer(cloningQueue.getId().getCmrIssuingCntry());
          cntryDup = transformer.getDupCreationCountryId(entityManager, cloningQueue.getId().getCmrIssuingCntry(), cloningQueue.getId().getCmrNo());
          if (!"NA".equals(cntryDup)) {
            processLegacyCloningDupCreate(entityManager, cloningQueue, cntryDup);
          }
        } catch (Exception e) {
          partialRollback(entityManager);
          cloningQueue.setStatus("LEGACY_ERR");
          cloningQueue.setErrorMsg("Error occured during legacy cloning");
          updateEntity(cloningQueue, entityManager);
          LOG.error("Unexpected error occurred during legacy cloning process for CMR No :" + cloningQueue.getId().getCmrNo(), e);
          // processError(entityManager, null, e.getMessage());
          break;
        }
        partialCommit(entityManager);

      case "LEGACY_OK":
      case "LEGACYSKIP":
        try {
          processCloningRDC(entityManager, cloningQueue, "NA");
          transformer = TransformerManager.getTransformer(cloningQueue.getId().getCmrIssuingCntry());
          cntryDup = transformer.getDupCreationCountryId(entityManager, cloningQueue.getId().getCmrIssuingCntry(), cloningQueue.getId().getCmrNo());
          if (!"NA".equals(cntryDup)) {
            processRdcCloningDupCreate(entityManager, cloningQueue, cntryDup);
          }
        } catch (Exception e) {
          partialRollback(entityManager);
          LOG.error("Unexpected error occurred during RDC_CLONING_REFN insertion for CMR No :" + cloningQueue.getId().getCmrNo(), e);
          // processError(entityManager, null, e.getMessage());
          break;
        }
        partialCommit(entityManager);

      case "RDC_INPROG":
      case "RDC_ERR":
        processRDCParentChildRecords(entityManager, cloningQueue);

      }

    }

    return true;
  }

  @Override
  protected Queue<CmrCloningQueue> getRequestsToProcess(EntityManager entityManager) {
    LOG.info("Inside getRequestsToProcess method");
    if (this.devMode) {
      LOG.info("RUNNING IN DEVELOPMENT MODE");
    } else {
      LOG.info("RUNNING IN NON-DEVELOPMENT MODE");
    }

    LinkedList<CmrCloningQueue> cloningList = new LinkedList<>();

    try {

      ChangeLogListener.setUser(COMMENT_LOGGER);

      LOG.info("Initializing Country Map..");
      LandedCountryMap.init(entityManager);

      // Retrieve all Pending records for cloning
      LOG.info("Retreiving cloning pending records for processing..");
      List<CmrCloningQueue> pendingCloning = getCloningPendingRecords(entityManager);
      LOG.debug((pendingCloning != null ? pendingCloning.size() : 0) + " records to process for cloning.");
      if (pendingCloning != null) {
        cloningList.addAll(pendingCloning);
      }

    } catch (Exception e) {
      e.printStackTrace();
      addError(e);
      // return false;
    }
    return cloningList;
  }

  @Override
  protected String getThreadName() {
    // TODO Auto-generated method stub
    return "CloningProcessService";
  }

  private void processRDCParentChildRecords(EntityManager entityManager, CmrCloningQueue cloningQueue) throws Exception {
    // now start cloning kna1 rdc
    LOG.info("Retrieving pending KNA1 RDc records for processing.." + cloningQueue.getId().getCmrNo());
    List<RdcCloningRefn> pendingCloningRefn = getPendingCloningRecordsKNA1RDC(entityManager, cloningQueue);

    LOG.debug((pendingCloningRefn != null ? pendingCloningRefn.size() : 0) + " records to process to KNA1 RDc.");

    CloningRDCUtil rdcUtil = new CloningRDCUtil();
    CloningRDCConfiguration rdcConfig = rdcUtil.getConfigDetails();

    List<CloningOverrideMapping> overrideValues = null;
    CloningOverrideUtil overrideUtil = new CloningOverrideUtil();

    for (RdcCloningRefn rdcCloningRefn : pendingCloningRefn) {
      try {
        overrideValues = overrideUtil.getOverrideValueFromMapping(rdcCloningRefn.getCmrIssuingCntry());
        processCloningKNA1RDC(entityManager, rdcCloningRefn, rdcConfig, overrideValues);
      } catch (Exception e) {
        partialRollback(entityManager);
        LOG.error("Unexpected error occurred during kna1 cloning process for CMR No : " + rdcCloningRefn.getCmrNo(), e);
        // processError(entityManager, null, e.getMessage());
      }
      partialCommit(entityManager);
    }

    // now start cloning at RDc end for KNA1 child tables
    LOG.info("Retrieving pending RDc records for kna1 child processing..");
    pendingCloningRefn = getPendingRecordsRDCKna1Child(entityManager, cloningQueue);

    LOG.debug((pendingCloningRefn != null ? pendingCloningRefn.size() : 0) + " records to process to KNA1 child RDc.");

    Kna1 kna1 = null;
    Kna1 kna1Clone = null;

    for (RdcCloningRefn rdcCloningRefn : pendingCloningRefn) {
      try {
        kna1 = getKna1ByKunnr(entityManager, rdcCloningRefn.getId().getMandt(), rdcCloningRefn.getId().getKunnr());
        kna1Clone = getKna1ByKunnr(entityManager, rdcCloningRefn.getTargetMandt(), rdcCloningRefn.getTargetKunnr());
        overrideValues = overrideUtil.getOverrideValueFromMapping(rdcCloningRefn.getCmrIssuingCntry());
        processKna1Children(entityManager, kna1, kna1Clone, rdcConfig, overrideValues);
        rdcCloningRefn.setStatus("C");
        updateEntity(rdcCloningRefn, entityManager);
        // if all rdcCloningRefn complete update cmr queue to completed
        cmrCloningQueueStatusUpdate(entityManager, rdcCloningRefn);
      } catch (Exception e) {
        partialRollback(entityManager);
        LOG.error("Unexpected error occurred during kna1 child cloning process for CMR No : " + rdcCloningRefn.getCmrNo(), e);
        processError(entityManager, rdcCloningRefn, null, "Issue while cloning KNA1 child records");
        updateEntity(rdcCloningRefn, entityManager);
      }
      partialCommit(entityManager);
    }
  }

}
