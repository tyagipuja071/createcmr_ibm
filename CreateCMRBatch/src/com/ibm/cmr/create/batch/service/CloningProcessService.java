package com.ibm.cmr.create.batch.service;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
import com.ibm.cio.cmr.request.entity.Changelog;
import com.ibm.cio.cmr.request.entity.ChangelogPK;
import com.ibm.cio.cmr.request.entity.CmrCloningQueue;
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
import com.ibm.cio.cmr.request.entity.Stxl;
import com.ibm.cio.cmr.request.entity.StxlPK;
import com.ibm.cio.cmr.request.entity.TransService;
import com.ibm.cio.cmr.request.entity.TransServicePK;
import com.ibm.cio.cmr.request.entity.listeners.ChangeLogListener;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.util.RequestUtils;
import com.ibm.cio.cmr.request.util.SystemUtil;
import com.ibm.cio.cmr.request.util.geo.GEOHandler;
import com.ibm.cio.cmr.request.util.legacy.CloningMapping;
import com.ibm.cio.cmr.request.util.legacy.CloningOverrideMapping;
import com.ibm.cio.cmr.request.util.legacy.CloningOverrideUtil;
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

/**
 * @author PriyRanjan
 * 
 */

public class CloningProcessService extends MultiThreadedBatchService<CmrCloningQueue> {

  private static final Logger LOG = Logger.getLogger(CloningProcessService.class);

  private static final String BATCH_SERVICE_URL = SystemConfiguration.getValue("BATCH_SERVICES_URL");
  private static final String COMMENT_LOGGER = "Cloning Process Service";
  private boolean devMode;
  protected static final String KUNNR_KEY = "KUNNR";
  protected static final String ADRNR_KEY = "ADRNR";
  private static final String PARNR_KEY = "PARNR";

  private static final String LEGACY_CUST_TABLE = "CMRTCUST";
  private static final String LEGACY_CUSTEXT_TABLE = "CMRTCEXT";
  private static final String LEGACY_ADDR_TABLE = "CMRTADDR";
  private static final String KNA1_TABLE = "KNA1";

  private static final List<String> KNVI_PK = Arrays.asList("aland", "tatyp");
  private static final List<String> KNVP_PK = Arrays.asList("vkorg", "vtweg", "spart", "parvw", "parza");
  private static final List<String> KNBK_PK = Arrays.asList("banks", "bankl", "bankn");
  private static final List<String> KNVL_PK = Arrays.asList("aland", "tatyp", "licnr");

  private static final List<String> PROCESSING_TYPES = Arrays.asList("DR", "MA", "MD", "FR");

  private static final List<String> STATUS_CLONING_REFN = Arrays.asList("E", "X", "C");

  private SimpleDateFormat ERDAT_FORMATTER = new SimpleDateFormat("yyyyMMdd");

  private static final String targetMandt = SystemConfiguration.getValue("TARGET_MANDT");

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

  protected List<CmrCloningQueue> getCloningPendingRecords(EntityManager entityManager) {
    String sql = ExternalizedQuery.getSql("CLONING_PENDING_RECORDS");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    return query.getResults(CmrCloningQueue.class);
  }

  protected synchronized void processCloningRecord(EntityManager entityManager, CmrCloningQueue cloningQueue) throws Exception {
    String cmrNo = cloningQueue.getId().getCmrNo();
    String cntry = cloningQueue.getId().getCmrIssuingCntry();
    LOG.debug("Inside processCloningRecord Started Cloning process for CMR No " + cmrNo);

    if (StringUtils.isBlank(cntry) || StringUtils.isBlank(cmrNo)) {
      throw new Exception("CMR No or Country missing");
    }

    if ("760".equals(cntry)) {
      cloningQueue.setStatus("LEGACYSKIP");
      updateEntity(cloningQueue, entityManager);
      return;
    }

    String cloningCmrNo = "";
    String processingType = "";
    String sql = ExternalizedQuery.getSql("GET.PROCESSING_TYPE");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("CNTRY", cntry);

    List<String> results = query.getResults(String.class);
    if (results != null && results.size() > 0) {
      processingType = results.get(0);
    }

    if (CmrConstants.PROCESSING_TYPE_LEGACY_DIRECT.equals(processingType))
      cloningCmrNo = generateCMRNoLegacy(entityManager, cntry, cmrNo, cloningQueue);
    else if (PROCESSING_TYPES.contains(processingType))
      cloningCmrNo = generateCMRNoNonLegacy(entityManager, cntry, cmrNo, cloningQueue);
    else {
      cloningQueue.setErrorMsg("CMR no generation not supported for this country");
      cloningQueue.setStatus("STOP");
      throw new Exception("CMR no generation not supported for this country");
    }

    LOG.debug("Cloning CMR No. " + cloningCmrNo + " generated and assigned for country " + cntry);

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

    if ("641".equals(cntry)) {
      String kukla = "";
      if (StringUtils.isBlank(cloningQueue.getLastUpdtBy())) {
        kukla = CloningUtil.getKuklaFromCMR(entityManager, cntry, cmrNo, SystemConfiguration.getValue("MANDT"));
      } else {
        kukla = cloningQueue.getLastUpdtBy();
      }
      // String kukla = CloningUtil.getKuklaFromCMR(entityManager, cntry, cmrNo,
      // SystemConfiguration.getValue("MANDT"));
      setCNLastUsedCMR(entityManager, cloningCmrNo, SystemConfiguration.getValue("MANDT"), kukla, cntry);
    }

  }

  private void processLegacyCloningProcess(EntityManager entityManager, CmrCloningQueue cloningQueue, String targetCntry) throws Exception {
    String cntry = "";
    String cmrNo = cloningQueue.getId().getCmrNo();
    if ("NA".equals(targetCntry))
      cntry = cloningQueue.getId().getCmrIssuingCntry();
    else
      cntry = targetCntry;

    LOG.debug("Inside processLegacyCloningProcess Started Cloning process for CMR No " + cmrNo + " country " + cntry);

    MessageTransformer transformer = TransformerManager.getTransformer(cntry);

    LegacyDirectObjectContainer legacyObjectsClone = new LegacyDirectObjectContainer();
    LegacyDirectObjectContainer legacyObjects = null;
    try {
      legacyObjects = LegacyDirectUtil.getLegacyDBValues(entityManager, cntry, cmrNo, false, transformer.hasAddressLinks());
    } catch (Exception e) {
      throw new Exception("CMR not found in Legacy");
    }

    CmrtCust cust = legacyObjects.getCustomer();

    if (cust == null) {
      // set STATUS to 'STOP' and set ERROR_MSG to 'CMR not found in Legacy'.
      cloningQueue.setStatus("STOP");
      updateEntity(cloningQueue, entityManager);
      throw new Exception("CMR not found in Legacy");
    } else if ("C".equals(cust.getStatus())) {
      throw new Exception("CMR cancelled in Legacy");
    }

    CloningOverrideUtil overrideUtil = new CloningOverrideUtil();
    // DB2 Cust Table
    CmrtCust custClone = initEmpty(CmrtCust.class);

    // default mapping for DATA and CMRTCUST
    LOG.debug("Mapping default Data values.." + cmrNo);
    CmrtCustPK custPkClone = new CmrtCustPK();
    custPkClone.setCustomerNo(cloningQueue.getClonedCmrNo());
    if ("754".equals(cntry))
      custPkClone.setSofCntryCode("866");
    else
      custPkClone.setSofCntryCode(cntry);

    // copy value same as old from cust object
    PropertyUtils.copyProperties(custClone, cust);
    custClone.setId(custPkClone);

    // override config changes
    List<CloningOverrideMapping> overrideValues = overrideUtil.getOverrideValueFromMapping(cntry, cloningQueue.getCreatedBy());

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
      if ("754".equals(cntry))
        legacyAddrPkClone.setSofCntryCode("866");
      else
        legacyAddrPkClone.setSofCntryCode(cntry);

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
      LOG.debug("Mapping default Data values with Legacy CmrtCustExt table....." + cmrNo);
      CmrtCustExt custExt = legacyObjects.getCustomerExt();
      if (custExt != null) {
        // Initialize the object
        custExtClone = initEmpty(CmrtCustExt.class);
        // default mapping for ADDR and CMRTCEXT
        custExtPkClone = new CmrtCustExtPK();
        custExtPkClone.setCustomerNo(cloningQueue.getClonedCmrNo());
        if ("754".equals(cntry))
          custExtPkClone.setSofCntryCode("866");
        else
          custExtPkClone.setSofCntryCode(cntry);

        // copy value same as old from cust ext object
        PropertyUtils.copyProperties(custExtClone, custExt);
        custExtClone.setId(custExtPkClone);

        overrideConfigChanges(entityManager, overrideValues, custExtClone, LEGACY_CUSTEXT_TABLE, custExtPkClone);

        custExtClone.setUpdateTs(SystemUtil.getCurrentTimestamp());
        // need to check regarding CODCC and CODCP field
        legacyObjectsClone.setCustomerExt(custExtClone);
      }

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

  private synchronized String generateCMRNoLegacy(EntityManager entityManager, String cmrIssuingCntry, String cmrNo, CmrCloningQueue cloningQueue)
      throws Exception {
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

    if ("11".equals(cloningQueue.getLastUpdtBy()) && cmrNo.startsWith("99")) {
      LOG.debug("Skip setting of CMR No for Internal for CMR : " + cmrNo);
    } else if (Arrays.asList("81", "85").contains(cloningQueue.getLastUpdtBy())) {
      if ("81".equals(cloningQueue.getLastUpdtBy())) {
        request.setMin(990000);
        request.setMax(999999);
      } else {
        request.setMin(997000);
        request.setMax(998999);
      }

    } else {
      int CmrNoVal = Integer.parseInt(cmrNo);
      CloningMapping cMapping = null;
      try {
        cMapping = getCMRNoRangeFromMapping(cmrIssuingCntry, CmrNoVal, request);
      } catch (Exception e) {
        LOG.error("Error occured while digesting xml in CMR No generation.", e);
      }

      if (cMapping == null && (CmrNoVal >= 990000 && CmrNoVal <= 999999)) {
        request.setMin(990000);
        request.setMax(999999);
      }
    }

    GenerateCMRNoClient client = CmrServicesFactory.getInstance().createClient(BATCH_SERVICE_URL, GenerateCMRNoClient.class);

    LOG.debug("Generating Cloning CMR No. for Issuing Country " + cmrIssuingCntry + " and input CMR " + cmrNo);
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

  private void processCloningRDC(EntityManager entityManager, CmrCloningQueue cloningQueue, String targetCntry) throws Exception {
    LOG.debug("Inside processCloningRDC Started Cloning process for CMR No " + cloningQueue.getId().getCmrNo());

    RdcCloningRefn cloningRefn = null;

    boolean japanFlag = "760".equals(cloningQueue.getId().getCmrIssuingCntry()) ? true : false;

    String sql = "";
    if (japanFlag)
      sql = ExternalizedQuery.getSql("CLONING.KNA1_MANDT_CMRNO_JP");
    else
      sql = ExternalizedQuery.getSql("CLONING.KNA1_MANDT_CMRNO");

    PreparedQuery query = new PreparedQuery(entityManager, sql);
    if ("NA".equals(targetCntry))
      query.setParameter("KATR6", cloningQueue.getId().getCmrIssuingCntry());
    else
      query.setParameter("KATR6", targetCntry);

    query.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
    query.setParameter("CMR_NO", cloningQueue.getId().getCmrNo());
    if (japanFlag)
      query.setParameter("CLONED_CMR_NO", cloningQueue.getClonedCmrNo());

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
      // cloningRefnPk.setKunnr("(missing)");
      cloningRefnPk.setKunnr("?" + cloningQueue.getId().getCmrNo());
      cloningRefn.setId(cloningRefnPk);

      if ("NA".equals(targetCntry))
        cloningRefn.setCmrIssuingCntry(cloningQueue.getId().getCmrIssuingCntry());
      else
        cloningRefn.setCmrIssuingCntry(targetCntry);

      cloningRefn.setCmrNo(cloningQueue.getId().getCmrNo());
      cloningRefn.setStatus("E");
      if (japanFlag)
        cloningRefn.setErrorMsg("KNA1 record not found or cloned cmr already exists with same sequence");
      else
        cloningRefn.setErrorMsg("KNA1 record not found");
      cloningRefn.setCreatedBy(BATCH_USER_ID);
      cloningRefn.setCreateTs(SystemUtil.getCurrentTimestamp());
      cloningRefn.setLastUpdtBy(BATCH_USER_ID);
      cloningRefn.setLastUpdtTs(SystemUtil.getCurrentTimestamp());
      createEntity(cloningRefn, entityManager);

      cloningQueue.setStatus("STOP");
      if (japanFlag)
        cloningQueue.setErrorMsg("KNA1 record not found or cloned cmr already exists with same sequence");
      else
        cloningQueue.setErrorMsg("KNA1 record not found");

      updateEntity(cloningQueue, entityManager);
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

  private void cmrCloningQueueStatusUpdate(EntityManager entityManager, CmrCloningQueue cloningQueue, String cntryDup) throws Exception {
    LOG.info("Inside cmrCloningQueueStatusUpdate for CMR No : " + cloningQueue.getId().getCmrNo());
    boolean statusCheck = false;
    String curentStatus = "";
    String sql = "";
    if (!"NA".equals(cntryDup)) {
      List<String> duplicateCntryList = new ArrayList<String>();
      duplicateCntryList.add(cloningQueue.getId().getCmrIssuingCntry());
      duplicateCntryList.add(cntryDup);
      String cntryList = convertToIssuingCntry(duplicateCntryList);
      sql = ExternalizedQuery.getSql("CLONING_CMR_STATUS_CHK_DC");

      sql = sql.replace("<CMR_ISSUING_CNTRY>", cntryList);
    } else {
      sql = ExternalizedQuery.getSql("CLONING_CMR_STATUS_CHK");
    }
    // String sql = ExternalizedQuery.getSql("CLONING_CMR_STATUS_CHK");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("ID", cloningQueue.getId().getCmrCloningProcessId());
    if ("NA".equals(cntryDup))
      query.setParameter("CNTRY", cloningQueue.getId().getCmrIssuingCntry());

    query.setParameter("CMR_NO", cloningQueue.getId().getCmrNo());
    List<Object[]> status = query.getResults();
    if (status.size() == 1) {
      curentStatus = String.valueOf(status.get(0));
      if ("C".equals(curentStatus)) {
        cloningQueue.setStatus("COMPLETED");
        updateEntity(cloningQueue, entityManager);
      }

    } else if (status.size() > 1) {
      for (int index = 0; index < status.size(); index++) {
        curentStatus = String.valueOf(status.get(index));
        if (STATUS_CLONING_REFN.contains(curentStatus)) {
          statusCheck = true;
        } else {
          statusCheck = false;
          break;
        }

      }

      if (statusCheck) {
        cloningQueue.setStatus("STOP");
        cloningQueue.setErrorMsg("Some data issue with kna1 or child tables");
        updateEntity(cloningQueue, entityManager);
      }
    }

  }

  private void processError(EntityManager entityManager, RdcCloningRefn rdcCloningRefn, CmrCloningQueue cmrCloningQueue, String errorMsg) {
    LOG.info("Processing error for CMR No: " + rdcCloningRefn.getCmrNo() + ": " + errorMsg);

    cmrCloningQueue.setStatus("RDC_ERR");

    if ("P".equals(rdcCloningRefn.getStatus())) {
      rdcCloningRefn.setStatus("R");
    } else if ("R".equals(rdcCloningRefn.getStatus())) {
      rdcCloningRefn.setStatus("X");
    }

    rdcCloningRefn.setErrorMsg(errorMsg);

    updateEntity(cmrCloningQueue, entityManager);
    updateEntity(rdcCloningRefn, entityManager);

  }

  private List<RdcCloningRefn> getPendingCloningRecordsKNA1RDC(EntityManager entityManager, CmrCloningQueue cmrCloningQueue, String cntryDup) {
    String sql = "";
    if (!"NA".equals(cntryDup)) {
      List<String> duplicateCntryList = new ArrayList<String>();
      duplicateCntryList.add(cmrCloningQueue.getId().getCmrIssuingCntry());
      duplicateCntryList.add(cntryDup);
      String cntryList = convertToIssuingCntry(duplicateCntryList);
      sql = ExternalizedQuery.getSql("CLONING_CMR_CLONING_RDC_REF_PENDING_DC");

      sql = sql.replace("<CMR_ISSUING_CNTRY>", cntryList);
    } else {
      sql = ExternalizedQuery.getSql("CLONING_CMR_CLONING_RDC_REF_PENDING");
    }

    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("ID", cmrCloningQueue.getId().getCmrCloningProcessId());
    if ("NA".equals(cntryDup))
      query.setParameter("CNTRY", cmrCloningQueue.getId().getCmrIssuingCntry());

    query.setParameter("CMR", cmrCloningQueue.getId().getCmrNo());
    return query.getResults(RdcCloningRefn.class);
  }

  private void processCloningKNA1RDC(EntityManager entityManager, RdcCloningRefn rdcCloningRefn, List<CloningOverrideMapping> overrideValues,
      CmrCloningQueue cloningQueue) throws Exception {
    LOG.info("Inside processCloningKNA1RDC CMR No: " + rdcCloningRefn.getCmrNo() + " and Country: " + rdcCloningRefn.getCmrIssuingCntry());
    try {
      Kna1 kna1 = null;
      String kunnr = "";
      Changelog changelog = null;
      Timestamp ts = SystemUtil.getCurrentTimestamp();
      boolean successFlag = true;
      kna1 = getKna1ByKunnr(entityManager, SystemConfiguration.getValue("MANDT"), rdcCloningRefn.getId().getKunnr());
      if (kna1 != null) {
        // generate kunnr, prepare the kna1 clone data
        kunnr = generateId(targetMandt, KUNNR_KEY, entityManager);
        Kna1 kna1Clone = new Kna1();
        Kna1PK kna1PkClone = new Kna1PK();
        kna1PkClone.setMandt(targetMandt);// from config file
        kna1PkClone.setKunnr(kunnr);
        try {
          PropertyUtils.copyProperties(kna1Clone, kna1);
          kna1Clone.setId(kna1PkClone);
          kna1Clone.setBran5("S" + kunnr.substring(1));
          kna1Clone.setZzkvCusno(cloningQueue.getClonedCmrNo());
          // kna1Clone.setKatr10(rdcConfig.getKatr10());// from config file
          if (StringUtils.isNotBlank(kna1.getAdrnr())) {
            String adrnr = generateId(targetMandt, ADRNR_KEY, entityManager);
            kna1Clone.setAdrnr(adrnr);
          }

          // dual create case handling
          if (!cloningQueue.getId().getCmrIssuingCntry().equals(rdcCloningRefn.getCmrIssuingCntry()))
            kna1Clone.setKatr6(rdcCloningRefn.getCmrIssuingCntry());

          overrideConfigChanges(entityManager, overrideValues, kna1Clone, KNA1_TABLE, kna1PkClone);

          if ("ZS01".equals(kna1Clone.getKtokd()) && StringUtils.isNotBlank(kna1.getAufsd()))
            kna1Clone.setAufsd(kna1.getAufsd());

          kna1Clone.setSapTs(ts);
          kna1Clone.setShadUpdateInd("I");
          kna1Clone.setShadUpdateTs(ts);
          kna1Clone.setErdat(ERDAT_FORMATTER.format(ts));

          createEntity(kna1Clone, entityManager);
          changelog = new Changelog();
          ChangelogPK changelogPk = new ChangelogPK();

          changelogPk.setChgts(ts);
          changelogPk.setField("");
          changelogPk.setKunnr(kna1Clone.getId().getKunnr());
          changelogPk.setMandt(kna1Clone.getId().getMandt());
          changelogPk.setTab("KNA1");

          changelog.setId(changelogPk);

          changelog.setAction("I");
          changelog.setUserid(kna1Clone.getErnam());
          changelog.setChgpnt("Y");
          changelog.setActgrp(kna1Clone.getKtokd());
          changelog.setLoadfilename("Cloning");
          changelog.setTabkey1(kna1Clone.getId().getKunnr());
          changelog.setLinenumber(String.valueOf(cloningQueue.getId().getCmrCloningProcessId()));
          entityManager.persist(changelog);
          entityManager.flush();
        } catch (Exception e) {
          LOG.debug("Issue in Copy KNA1 record for cmr no : " + kna1Clone.getZzkvCusno() + " and KUNNR: " + kna1Clone.getId().getKunnr(), e);
          processError(entityManager, rdcCloningRefn, cloningQueue, "Issue in Copy KNA1 record");
          successFlag = false;
        }
      }

      if (successFlag) {
        rdcCloningRefn.setTargetMandt(targetMandt); // from config
        rdcCloningRefn.setTargetKunnr(kunnr);
      }

    } catch (Exception e) {
      LOG.debug("Issue in Creating KNA1 record for cmr no : " + rdcCloningRefn.getCmrNo() + " and KUNNR: " + rdcCloningRefn.getTargetKunnr(), e);
      processError(entityManager, rdcCloningRefn, cloningQueue, "Issue in Creating KNA1 record");
    }

    updateEntity(cloningQueue, entityManager);
    updateEntity(rdcCloningRefn, entityManager);
    LOG.debug("CMR No " + rdcCloningRefn.getCmrNo() + " Status: " + rdcCloningRefn.getStatus() + " Message: " + rdcCloningRefn.getErrorMsg());

    partialCommit(entityManager);
  }

  private List<RdcCloningRefn> getPendingRecordsRDCKna1Child(EntityManager entityManager, CmrCloningQueue cmrCloningQueue, String cntryDup) {
    String sql = "";
    if (!"NA".equals(cntryDup)) {
      List<String> duplicateCntryList = new ArrayList<String>();
      duplicateCntryList.add(cmrCloningQueue.getId().getCmrIssuingCntry());
      duplicateCntryList.add(cntryDup);
      String cntryList = convertToIssuingCntry(duplicateCntryList);
      sql = ExternalizedQuery.getSql("CLONING_CMR_CLONING_RDC_REF_PENDING_CHILD_DC");

      sql = sql.replace("<CMR_ISSUING_CNTRY>", cntryList);
    } else {
      sql = ExternalizedQuery.getSql("CLONING_CMR_CLONING_RDC_REF_PENDING_CHILD");
    }

    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("ID", cmrCloningQueue.getId().getCmrCloningProcessId());
    if ("NA".equals(cntryDup))
      query.setParameter("CNTRY", cmrCloningQueue.getId().getCmrIssuingCntry());

    query.setParameter("CMR", cmrCloningQueue.getId().getCmrNo());
    return query.getResults(RdcCloningRefn.class);
  }

  protected String generateId(String mandt, String key, EntityManager entityManager) {
    LOG.debug("Calling stored procedure to produce next " + key);
    String generatedId = "";

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

  protected void processKna1Children(EntityManager rdcMgr, Kna1 kna1, Kna1 kna1Clone, List<CloningOverrideMapping> overrideValues) throws Exception {

    processKnb1(rdcMgr, kna1, kna1Clone, overrideValues);

    processKnvv(rdcMgr, kna1, kna1Clone, overrideValues);

    // processKnvi(rdcMgr, kna1, kna1Clone, overrideValues);

    processKnex(rdcMgr, kna1, kna1Clone, overrideValues);

    processKnvp(rdcMgr, kna1, kna1Clone, overrideValues);

    if (StringUtils.isNotBlank(kna1.getAdrnr())) {
      processSadr(rdcMgr, kna1, kna1Clone, overrideValues);
    }

    processAddlCtryData(rdcMgr, kna1, kna1Clone, overrideValues);

    processKnvk(rdcMgr, kna1, kna1Clone, overrideValues);

    processKunnrExt(rdcMgr, kna1, kna1Clone, overrideValues);

    processKnbk(rdcMgr, kna1, kna1Clone, overrideValues);

    processKnva(rdcMgr, kna1, kna1Clone, overrideValues);

    processKnvl(rdcMgr, kna1, kna1Clone, overrideValues);

    processSizeInfo(rdcMgr, kna1, kna1Clone, overrideValues);

    processTransService(rdcMgr, kna1, kna1Clone, overrideValues);
  }

  private void processKnb1(EntityManager entityManager, Kna1 kna1, Kna1 kna1Clone, List<CloningOverrideMapping> overrideValues) throws Exception {

    List<Knb1> knb1 = null;
    List<Knb1> knb1Clone = null;
    Knb1 cloneInsert = null;
    Timestamp ts = SystemUtil.getCurrentTimestamp();

    knb1 = getKnb1ByKunnr(entityManager, kna1.getId().getMandt(), kna1.getId().getKunnr());
    knb1Clone = getKnb1ByKunnr(entityManager, kna1Clone.getId().getMandt(), kna1Clone.getId().getKunnr());
    if (knb1 != null && knb1.size() > 0 && knb1Clone.size() == 0) {
      try {
        int count = 0;
        for (Knb1 current : knb1) {
          cloneInsert = new Knb1();
          Knb1PK knb1PKClone = new Knb1PK();
          knb1PKClone.setMandt(kna1Clone.getId().getMandt());
          knb1PKClone.setKunnr(kna1Clone.getId().getKunnr());
          knb1PKClone.setBukrs(current.getId().getBukrs());
          count++;

          PropertyUtils.copyProperties(cloneInsert, current);

          overrideConfigChanges(entityManager, overrideValues, cloneInsert, "KNB1", knb1PKClone);

          cloneInsert.setId(knb1PKClone);

          if (!(cloneInsert.getId().getBukrs().equalsIgnoreCase(current.getId().getBukrs())) && count > 1)
            break;

          cloneInsert.setSapTs(ts);
          cloneInsert.setShadUpdateInd("I");
          cloneInsert.setShadUpdateTs(ts);

          createEntity(cloneInsert, entityManager);
        }
      } catch (Exception e) {
        LOG.debug("Error in copy knb1 :", e);
      }
    } else {
      LOG.info("KNB1 record not exist with KUNNR " + kna1.getId().getKunnr());
    }

  }

  public List<Knb1> getKnb1ByKunnr(EntityManager rdcMgr, String mandt, String kunnr) throws Exception {
    String sql = ExternalizedQuery.getSql("GET.KNB1.RECORD");
    PreparedQuery query = new PreparedQuery(rdcMgr, sql);
    query.setParameter("MANDT", mandt);
    query.setParameter("KUNNR", kunnr);
    return query.getResults(Knb1.class);
  }

  private void processKnvv(EntityManager entityManager, Kna1 kna1, Kna1 kna1Clone, List<CloningOverrideMapping> overrideValues) throws Exception {

    List<Knvv> knvv = null;
    List<Knvv> knvvClone = null;
    Knvv cloneInsert = null;
    Timestamp ts = SystemUtil.getCurrentTimestamp();

    knvv = getKnvvByKunnr(entityManager, kna1.getId().getMandt(), kna1.getId().getKunnr());
    knvvClone = getKnvvByKunnr(entityManager, kna1Clone.getId().getMandt(), kna1Clone.getId().getKunnr());
    if (knvv != null && knvv.size() > 0 && knvvClone.size() == 0) {
      try {
        // Set<String> usedSpart = new HashSet<String>();
        // Set<String> usedVtweg = new HashSet<String>();
        for (Knvv current : knvv) {
          cloneInsert = new Knvv();
          KnvvPK knvvPKClone = new KnvvPK();
          knvvPKClone.setKunnr(kna1Clone.getId().getKunnr());
          knvvPKClone.setMandt(kna1Clone.getId().getMandt());
          knvvPKClone.setSpart(current.getId().getSpart());
          knvvPKClone.setVkorg(current.getId().getVkorg());
          knvvPKClone.setVtweg(current.getId().getVtweg());

          PropertyUtils.copyProperties(cloneInsert, current);

          overrideConfigChanges(entityManager, overrideValues, cloneInsert, "KNVV", knvvPKClone);

          cloneInsert.setId(knvvPKClone);

          /*
           * if
           * (!(cloneInsert.getId().getVkorg().equalsIgnoreCase(current.getId().
           * getVkorg())) && usedSpart.contains(cloneInsert.getId().getSpart())
           * && usedVtweg.contains(cloneInsert.getId().getVtweg())) continue;
           */

          if (!(cloneInsert.getId().getVkorg().equalsIgnoreCase(current.getId().getVkorg()))) {
            int exists = isExistKnvv(entityManager, cloneInsert.getId().getKunnr(), cloneInsert.getId().getMandt(), cloneInsert.getId().getVkorg(),
                cloneInsert.getId().getSpart(), cloneInsert.getId().getVtweg());
            if (exists != 0)
              continue;
          }

          cloneInsert.setSapTs(ts);
          cloneInsert.setShadUpdateInd("I");
          cloneInsert.setShadUpdateTs(ts);

          createEntity(cloneInsert, entityManager);
          processKnvi(entityManager, kna1, kna1Clone, overrideValues);
          // usedSpart.add(cloneInsert.getId().getSpart());
          // usedVtweg.add(cloneInsert.getId().getVtweg());
        }

      } catch (Exception e) {
        LOG.debug("Error in copy knvv:", e);
      }
    } else {
      LOG.info("KNVV record not exist with KUNNR " + kna1.getId().getKunnr());
    }

  }

  public List<Knvv> getKnvvByKunnr(EntityManager rdcMgr, String mandt, String kunnr) throws Exception {
    String sql = ExternalizedQuery.getSql("GET.KNVV.RECORD");
    PreparedQuery query = new PreparedQuery(rdcMgr, sql);
    query.setParameter("MANDT", mandt);
    query.setParameter("KUNNR", kunnr);
    return query.getResults(Knvv.class);
  }

  private void processKnex(EntityManager entityManager, Kna1 kna1, Kna1 kna1Clone, List<CloningOverrideMapping> overrideValues) throws Exception {

    List<Knex> knex = null;
    List<Knex> knexClone = null;
    Knex cloneInsert = null;
    Timestamp ts = SystemUtil.getCurrentTimestamp();

    knex = getKnexByKunnr(entityManager, kna1.getId().getMandt(), kna1.getId().getKunnr());
    knexClone = getKnexByKunnr(entityManager, kna1Clone.getId().getMandt(), kna1Clone.getId().getKunnr());
    if (knex != null && knex.size() > 0 && knexClone.size() == 0) {
      try {
        for (Knex current : knex) {
          cloneInsert = new Knex();
          KnexPK knexPKClone = new KnexPK();
          knexPKClone.setKunnr(kna1Clone.getId().getKunnr());
          knexPKClone.setLndex(current.getId().getLndex());
          knexPKClone.setMandt(kna1Clone.getId().getMandt());

          PropertyUtils.copyProperties(cloneInsert, current);

          overrideConfigChanges(entityManager, overrideValues, cloneInsert, "KNEX", knexPKClone);

          cloneInsert.setId(knexPKClone);

          cloneInsert.setSapTs(ts);
          cloneInsert.setShadUpdateInd("I");
          cloneInsert.setShadUpdateTs(ts);

          createEntity(cloneInsert, entityManager);
        }

      } catch (Exception e) {
        LOG.debug("Error in copy knex :", e);
      }
    } else {
      LOG.info("KNEX record not exist with KUNNR " + kna1.getId().getKunnr());
    }

  }

  public List<Knex> getKnexByKunnr(EntityManager rdcMgr, String mandt, String kunnr) throws Exception {
    String sql = ExternalizedQuery.getSql("GET.KNEX.RECORD");
    PreparedQuery query = new PreparedQuery(rdcMgr, sql);
    query.setParameter("MANDT", mandt);
    query.setParameter("KUNNR", kunnr);
    return query.getResults(Knex.class);
  }

  private void processSadr(EntityManager entityManager, Kna1 kna1, Kna1 kna1Clone, List<CloningOverrideMapping> overrideValues) throws Exception {

    List<Sadr> sadr = null;
    List<Sadr> sadrClone = null;
    Sadr cloneInsert = null;
    Timestamp ts = SystemUtil.getCurrentTimestamp();

    sadr = getSadrByAdrnr(entityManager, kna1);
    sadrClone = getSadrByAdrnr(entityManager, kna1Clone);
    if (sadr != null && sadr.size() > 0 && sadrClone.size() == 0) {
      try {
        for (Sadr current : sadr) {
          cloneInsert = new Sadr();
          SadrPK sadrPKClone = new SadrPK();
          sadrPKClone.setAdrnr(kna1Clone.getAdrnr());
          sadrPKClone.setMandt(kna1Clone.getId().getMandt());
          sadrPKClone.setNatio(current.getId().getNatio());

          PropertyUtils.copyProperties(cloneInsert, current);

          overrideConfigChanges(entityManager, overrideValues, cloneInsert, "SADR", sadrPKClone);

          cloneInsert.setId(sadrPKClone);

          if ("760".equals(kna1Clone.getKatr6())) {
            cloneInsert.setSortl(kna1Clone.getZzkvCusno() + kna1Clone.getZzkvSeqno());
          }
          cloneInsert.setSapTs(ts);
          cloneInsert.setShadUpdateInd("I");
          cloneInsert.setShadUpdateTs(ts);

          createEntity(cloneInsert, entityManager);
        }

      } catch (Exception e) {
        LOG.debug("Error in copy sadr :", e);
      }
    } else {
      LOG.info("SADR record not exist with ADRNR " + kna1.getAdrnr());
    }

  }

  public List<Sadr> getSadrByAdrnr(EntityManager rdcMgr, Kna1 kna1) throws Exception {
    String sql = ExternalizedQuery.getSql("GET.SADR.RECORD");
    PreparedQuery query = new PreparedQuery(rdcMgr, sql);
    query.setParameter("MANDT", kna1.getId().getMandt());
    query.setParameter("ADRNR", kna1.getAdrnr());
    return query.getResults(Sadr.class);
  }

  private void processKnvi(EntityManager entityManager, Kna1 kna1, Kna1 kna1Clone, List<CloningOverrideMapping> overrideValues) throws Exception {

    List<Knvi> knvi = null;
    List<Knvi> knviClone = null;
    // Knvi knviCloneInsert = null;
    Timestamp ts = SystemUtil.getCurrentTimestamp();

    // knvi = getKnviByKunnr(entityManager, kna1.getId().getMandt(),
    // kna1.getId().getKunnr());
    knviClone = getKnviByKunnr(entityManager, kna1Clone.getId().getMandt(), kna1Clone.getId().getKunnr());
    if (knviClone != null && knviClone.size() == 0) {
      try {
        String sql = ExternalizedQuery.getSql("GET.KNVI.DEFAULT");
        PreparedQuery query = new PreparedQuery(entityManager, sql);
        query.setParameter("MANDT", kna1Clone.getId().getMandt());
        query.setParameter("KUNNR", kna1Clone.getId().getKunnr());
        query.setForReadOnly(true);
        List<Object[]> defaultKnvi = query.getResults();

        Knvi newKnvi = null;
        KnviPK newKnviPk = null;
        LOG.debug("Creating " + defaultKnvi.size() + " KNVI records.");
        for (Object[] result : defaultKnvi) {
          newKnviPk = new KnviPK();
          newKnviPk.setMandt(kna1Clone.getId().getMandt());
          newKnviPk.setKunnr(kna1Clone.getId().getKunnr());
          newKnviPk.setAland((String) result[2]);
          newKnviPk.setTatyp((String) result[3]);

          newKnvi = new Knvi();
          newKnvi.setTaxkd((String) result[4]);

          overrideConfigChanges(entityManager, overrideValues, newKnvi, "KNVI", newKnviPk);

          newKnvi.setId(newKnviPk);

          newKnvi.setSapTs(ts);
          newKnvi.setShadUpdateInd("I");
          newKnvi.setShadUpdateTs(ts);

          createEntity(newKnvi, entityManager);

        }

      } catch (Exception e) {
        LOG.debug("Error in inserting knvi :", e);
      }
    } else {
      LOG.info("KNVI record already exists with KUNNR " + kna1Clone.getId().getKunnr());
    }

  }

  public List<Knvi> getKnviByKunnr(EntityManager rdcMgr, String mandt, String kunnr) throws Exception {
    String sql = ExternalizedQuery.getSql("GET.KNVI.CURRENT");
    PreparedQuery query = new PreparedQuery(rdcMgr, sql);
    query.setParameter("MANDT", mandt);
    query.setParameter("KUNNR", kunnr);
    return query.getResults(Knvi.class);
  }

  private void processKnvk(EntityManager entityManager, Kna1 kna1, Kna1 kna1Clone, List<CloningOverrideMapping> overrideValues) throws Exception {

    List<Knvk> knvk = null;
    List<Knvk> knvkClone = null;
    Knvk cloneInsert = null;
    Timestamp ts = SystemUtil.getCurrentTimestamp();

    knvk = getKnvkByKunnr(entityManager, kna1.getId().getMandt(), kna1.getId().getKunnr());
    knvkClone = getKnvkByKunnr(entityManager, kna1Clone.getId().getMandt(), kna1Clone.getId().getKunnr());
    if (knvk != null && knvk.size() > 0 && knvkClone.size() == 0) {
      try {
        for (Knvk current : knvk) {
          cloneInsert = new Knvk();
          KnvkPK knvkPKClone = new KnvkPK();
          knvkPKClone.setMandt(kna1Clone.getId().getMandt());
          String parnr = generateId(kna1Clone.getId().getMandt(), PARNR_KEY, entityManager);
          knvkPKClone.setParnr(parnr);

          PropertyUtils.copyProperties(cloneInsert, current);

          overrideConfigChanges(entityManager, overrideValues, cloneInsert, "KNVK", knvkPKClone);

          cloneInsert.setId(knvkPKClone);

          cloneInsert.setKunnr(kna1Clone.getId().getKunnr());
          if ("760".equals(kna1Clone.getKatr6())) {
            cloneInsert.setParau("760" + kna1Clone.getZzkvCusno() + kna1Clone.getZzkvSeqno());
            cloneInsert.setSortl(kna1Clone.getZzkvCusno() + kna1Clone.getZzkvSeqno());
          }

          cloneInsert.setSapTs(ts);
          cloneInsert.setShadUpdateInd("I");
          cloneInsert.setShadUpdateTs(ts);

          createEntity(cloneInsert, entityManager);

          // Japan Handling
          if ("760".equals(kna1Clone.getKatr6())) {
            processStxl(entityManager, current, cloneInsert, overrideValues);
          }
        }

      } catch (Exception e) {
        LOG.debug("Error in copy knvk :", e);
      }
    } else {
      LOG.info("KNVK record not exist with KUNNR " + kna1.getId().getKunnr());
    }

  }

  public List<Knvk> getKnvkByKunnr(EntityManager rdcMgr, String mandt, String kunnr) throws Exception {
    String sql = ExternalizedQuery.getSql("GET.KNVK.RECORD");
    PreparedQuery query = new PreparedQuery(rdcMgr, sql);
    query.setParameter("MANDT", mandt);
    query.setParameter("KUNNR", kunnr);
    return query.getResults(Knvk.class);
  }

  private void processKnvp(EntityManager entityManager, Kna1 kna1, Kna1 kna1Clone, List<CloningOverrideMapping> overrideValues) throws Exception {

    List<Knvp> knvp = null;
    List<Knvp> knvpClone = null;
    Knvp knvpCloneInsert = null;
    Timestamp ts = SystemUtil.getCurrentTimestamp();

    knvp = getKnvpByKunnr(entityManager, kna1.getId().getMandt(), kna1.getId().getKunnr());
    knvpClone = getKnvpByKunnr(entityManager, kna1Clone.getId().getMandt(), kna1Clone.getId().getKunnr());
    if (knvp != null && knvp.size() > 0 && knvpClone.size() == 0) {
      try {
        /*
         * Set<String> usedVtweg = new HashSet<String>(); Set<String> usedSpart
         * = new HashSet<String>(); Set<String> usedParvw = new
         * HashSet<String>(); Set<String> usedParza = new HashSet<String>();
         */

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

          /*
           * if
           * (!(knvpCloneInsert.getId().getVkorg().equalsIgnoreCase(currentKnvp.
           * getId().getVkorg())) &&
           * usedVtweg.contains(knvpCloneInsert.getId().getVtweg()) &&
           * usedSpart.contains(knvpCloneInsert.getId().getSpart()) &&
           * usedParvw.contains(knvpCloneInsert.getId().getParvw()) &&
           * usedParza.contains(knvpCloneInsert.getId().getParza())) continue;
           */

          if (!(knvpCloneInsert.getId().getVkorg().equalsIgnoreCase(currentKnvp.getId().getVkorg()))) {
            int exists = isExistKnvp(entityManager, knvpCloneInsert.getId().getKunnr(), knvpCloneInsert.getId().getMandt(),
                knvpCloneInsert.getId().getParvw(), knvpCloneInsert.getId().getParza(), knvpCloneInsert.getId().getVkorg(),
                knvpCloneInsert.getId().getSpart(), knvpCloneInsert.getId().getVtweg());
            if (exists != 0)
              continue;
          }

          knvpCloneInsert.setSapTs(ts);
          knvpCloneInsert.setShadUpdateInd("I");
          knvpCloneInsert.setShadUpdateTs(ts);

          createEntity(knvpCloneInsert, entityManager);
          // usedVtweg.add(knvpCloneInsert.getId().getVtweg());
          // usedSpart.add(knvpCloneInsert.getId().getSpart());
          // usedParvw.add(knvpCloneInsert.getId().getParvw());
          // usedParza.add(knvpCloneInsert.getId().getParza());
        }
      } catch (Exception e) {
        LOG.debug("Error in copy knvp :", e);
      }
    } else {
      LOG.info("KNVP record not exist with KUNNR " + kna1.getId().getKunnr());
    }

  }

  public List<Knvp> getKnvpByKunnr(EntityManager rdcMgr, String mandt, String kunnr) throws Exception {
    String sql = ExternalizedQuery.getSql("GET.KNVP.RECORD");
    PreparedQuery query = new PreparedQuery(rdcMgr, sql);
    query.setParameter("MANDT", mandt);
    query.setParameter("KUNNR", kunnr);
    return query.getResults(Knvp.class);
  }

  private void processAddlCtryData(EntityManager entityManager, Kna1 kna1, Kna1 kna1Clone, List<CloningOverrideMapping> overrideValues)
      throws Exception {

    List<Addlctrydata> addlctrydata = null;
    List<Addlctrydata> addlctrydataClone = null;
    Addlctrydata cloneInsert = null;
    Timestamp ts = SystemUtil.getCurrentTimestamp();

    addlctrydata = getAddlCtryDataByKunnr(entityManager, kna1.getId().getMandt(), kna1.getId().getKunnr());
    addlctrydataClone = getAddlCtryDataByKunnr(entityManager, kna1Clone.getId().getMandt(), kna1Clone.getId().getKunnr());
    if (addlctrydata != null && addlctrydata.size() > 0 && addlctrydataClone.size() == 0) {
      try {
        for (Addlctrydata current : addlctrydata) {
          cloneInsert = new Addlctrydata();
          AddlctrydataPK pk = new AddlctrydataPK();
          pk.setFieldName(current.getId().getFieldName());
          pk.setKunnr(kna1Clone.getId().getKunnr());
          pk.setMandt(kna1Clone.getId().getMandt());

          PropertyUtils.copyProperties(cloneInsert, current);

          overrideConfigChanges(entityManager, overrideValues, cloneInsert, "ADDLCTRYDATA", pk);

          cloneInsert.setId(pk);

          cloneInsert.setCreateDt(ts);
          cloneInsert.setUpdateDt(ts);

          createEntity(cloneInsert, entityManager);
        }

      } catch (Exception e) {
        LOG.debug("Error in copy addlctrydata :", e);
      }
    } else {
      LOG.info("ADDLCTRYDATA record not exist with KUNNR " + kna1.getId().getKunnr());
    }

  }

  public List<Addlctrydata> getAddlCtryDataByKunnr(EntityManager rdcMgr, String mandt, String kunnr) throws Exception {
    String sql = ExternalizedQuery.getSql("GET.ADDLCTRYDATA.RECORD");
    PreparedQuery query = new PreparedQuery(rdcMgr, sql);
    query.setParameter("MANDT", mandt);
    query.setParameter("KUNNR", kunnr);
    return query.getResults(Addlctrydata.class);
  }

  private void processKunnrExt(EntityManager entityManager, Kna1 kna1, Kna1 kna1Clone, List<CloningOverrideMapping> overrideValues) throws Exception {

    List<KunnrExt> kunnrExt = null;
    List<KunnrExt> kunnrExtClone = null;
    KunnrExt cloneInsert = null;
    Timestamp ts = SystemUtil.getCurrentTimestamp();

    kunnrExt = getKunnrExtByKunnr(entityManager, kna1.getId().getMandt(), kna1.getId().getKunnr());
    kunnrExtClone = getKunnrExtByKunnr(entityManager, kna1Clone.getId().getMandt(), kna1Clone.getId().getKunnr());
    if (kunnrExt != null && kunnrExt.size() > 0 && kunnrExtClone.size() == 0) {
      LOG.info("Inside KUNNR_EXT record Insert with KUNNR " + kna1.getId().getKunnr());
      try {
        for (KunnrExt current : kunnrExt) {
          cloneInsert = new KunnrExt();
          KunnrExtPK pk = new KunnrExtPK();
          pk.setKunnr(kna1Clone.getId().getKunnr());
          pk.setMandt(kna1Clone.getId().getMandt());

          PropertyUtils.copyProperties(cloneInsert, current);

          overrideConfigChanges(entityManager, overrideValues, cloneInsert, "KUNNR_EXT", pk);

          cloneInsert.setId(pk);

          cloneInsert.setCreateTs(ts);
          cloneInsert.setUpdateInd("I");
          cloneInsert.setUpdateTs(ts);

          createEntity(cloneInsert, entityManager);
        }

      } catch (Exception e) {
        LOG.debug("Error in copy kunnrext :", e);
      }
    } else {
      LOG.info("Inside KUNNR_EXT record updating with KUNNR " + kna1.getId().getKunnr());
      try {
        for (KunnrExt current : kunnrExt) {
          KunnrExtPK pk = new KunnrExtPK();
          pk.setKunnr(kna1Clone.getId().getKunnr());
          pk.setMandt(kna1Clone.getId().getMandt());

          KunnrExt cloneInsertKE = entityManager.find(KunnrExt.class, pk);

          PropertyUtils.copyProperties(cloneInsertKE, current);

          overrideConfigChanges(entityManager, overrideValues, cloneInsertKE, "KUNNR_EXT", pk);

          cloneInsertKE.setId(pk);

          cloneInsertKE.setUpdateInd("U");
          cloneInsertKE.setUpdateTs(ts);

          updateEntity(cloneInsertKE, entityManager);
        }

      } catch (Exception e) {
        LOG.debug("Error in updating kunnrext :", e);
      }
    }

  }

  public List<KunnrExt> getKunnrExtByKunnr(EntityManager rdcMgr, String mandt, String kunnr) throws Exception {
    String sql = ExternalizedQuery.getSql("GET.KUNNR_EXT.RECORD");
    PreparedQuery query = new PreparedQuery(rdcMgr, sql);
    query.setParameter("MANDT", mandt);
    query.setParameter("KUNNR", kunnr);
    return query.getResults(KunnrExt.class);
  }

  private void processKnbk(EntityManager entityManager, Kna1 kna1, Kna1 kna1Clone, List<CloningOverrideMapping> overrideValues) throws Exception {

    List<Knbk> knbk = null;
    List<Knbk> knbkClone = null;
    Knbk cloneInsert = null;
    Timestamp ts = SystemUtil.getCurrentTimestamp();

    knbk = getKnbkByKunnr(entityManager, kna1.getId().getMandt(), kna1.getId().getKunnr());
    knbkClone = getKnbkByKunnr(entityManager, kna1Clone.getId().getMandt(), kna1Clone.getId().getKunnr());
    if (knbk != null && knbk.size() > 0 && knbkClone.size() == 0) {
      try {
        for (Knbk current : knbk) {
          cloneInsert = new Knbk();
          KnbkPK knbkPKClone = new KnbkPK();
          knbkPKClone.setBankl(current.getId().getBankl());
          knbkPKClone.setBankn(current.getId().getBankn());
          knbkPKClone.setBanks(current.getId().getBanks());
          knbkPKClone.setKunnr(kna1Clone.getId().getKunnr());
          knbkPKClone.setMandt(kna1Clone.getId().getMandt());

          PropertyUtils.copyProperties(cloneInsert, current);

          overrideConfigChanges(entityManager, overrideValues, cloneInsert, "KNBK", knbkPKClone);

          cloneInsert.setId(knbkPKClone);

          cloneInsert.setSapTs(ts);
          cloneInsert.setShadUpdateInd("I");
          cloneInsert.setShadUpdateTs(ts);

          createEntity(cloneInsert, entityManager);
        }

      } catch (Exception e) {
        LOG.debug("Error in copy knbk :", e);
      }
    } else {
      LOG.info("KNBK record not exist with KUNNR " + kna1.getId().getKunnr());
    }

  }

  public List<Knbk> getKnbkByKunnr(EntityManager rdcMgr, String mandt, String kunnr) throws Exception {
    String sql = ExternalizedQuery.getSql("GET.KNBK.RECORD");
    PreparedQuery query = new PreparedQuery(rdcMgr, sql);
    query.setParameter("MANDT", mandt);
    query.setParameter("KUNNR", kunnr);
    return query.getResults(Knbk.class);
  }

  private void processKnva(EntityManager entityManager, Kna1 kna1, Kna1 kna1Clone, List<CloningOverrideMapping> overrideValues) throws Exception {

    List<Knva> knva = null;
    List<Knva> knvaClone = null;
    Knva cloneInsert = null;
    Timestamp ts = SystemUtil.getCurrentTimestamp();

    knva = getKnvaByKunnr(entityManager, kna1.getId().getMandt(), kna1.getId().getKunnr());
    knvaClone = getKnvaByKunnr(entityManager, kna1Clone.getId().getMandt(), kna1Clone.getId().getKunnr());
    if (knva != null && knva.size() > 0 && knvaClone.size() == 0) {
      try {
        for (Knva current : knva) {
          cloneInsert = new Knva();
          KnvaPK knvaPKClone = new KnvaPK();
          knvaPKClone.setAblad(current.getId().getAblad());
          knvaPKClone.setKunnr(kna1Clone.getId().getKunnr());
          knvaPKClone.setMandt(kna1Clone.getId().getMandt());

          PropertyUtils.copyProperties(cloneInsert, current);

          overrideConfigChanges(entityManager, overrideValues, cloneInsert, "KNVA", knvaPKClone);

          cloneInsert.setId(knvaPKClone);

          cloneInsert.setSapTs(ts);
          cloneInsert.setShadUpdateInd("I");
          cloneInsert.setShadUpdateTs(ts);

          createEntity(cloneInsert, entityManager);
        }

      } catch (Exception e) {
        LOG.debug("Error in copy knva :", e);
      }
    } else {
      LOG.info("KNVA record not exist with KUNNR " + kna1.getId().getKunnr());
    }

  }

  public List<Knva> getKnvaByKunnr(EntityManager rdcMgr, String mandt, String kunnr) throws Exception {
    String sql = ExternalizedQuery.getSql("GET.KNVA.RECORD");
    PreparedQuery query = new PreparedQuery(rdcMgr, sql);
    query.setParameter("MANDT", mandt);
    query.setParameter("KUNNR", kunnr);
    return query.getResults(Knva.class);
  }

  private void processKnvl(EntityManager entityManager, Kna1 kna1, Kna1 kna1Clone, List<CloningOverrideMapping> overrideValues) throws Exception {

    List<Knvl> knvl = null;
    List<Knvl> knvlClone = null;
    Knvl cloneInsert = null;
    Timestamp ts = SystemUtil.getCurrentTimestamp();

    knvl = getKnvlByKunnr(entityManager, kna1.getId().getMandt(), kna1.getId().getKunnr());
    knvlClone = getKnvlByKunnr(entityManager, kna1Clone.getId().getMandt(), kna1Clone.getId().getKunnr());
    if (knvl != null && knvl.size() > 0 && knvlClone.size() == 0) {
      try {
        for (Knvl current : knvl) {
          cloneInsert = new Knvl();
          KnvlPK knvlPKClone = new KnvlPK();
          knvlPKClone.setAland(current.getId().getAland());
          knvlPKClone.setKunnr(kna1Clone.getId().getKunnr());
          knvlPKClone.setLicnr(current.getId().getLicnr());
          knvlPKClone.setMandt(kna1Clone.getId().getMandt());
          knvlPKClone.setTatyp(current.getId().getTatyp());

          PropertyUtils.copyProperties(cloneInsert, current);

          overrideConfigChanges(entityManager, overrideValues, cloneInsert, "KNVL", knvlPKClone);

          cloneInsert.setId(knvlPKClone);

          cloneInsert.setSapTs(ts);
          cloneInsert.setShadUpdateInd("I");
          cloneInsert.setShadUpdateTs(ts);

          createEntity(cloneInsert, entityManager);
        }

      } catch (Exception e) {
        LOG.debug("Error in copy knvl :", e);
      }
    } else {
      LOG.info("KNVL record not exist with KUNNR " + kna1.getId().getKunnr());
    }

  }

  public List<Knvl> getKnvlByKunnr(EntityManager rdcMgr, String mandt, String kunnr) throws Exception {
    String sql = ExternalizedQuery.getSql("GET.KNVL.RECORD");
    PreparedQuery query = new PreparedQuery(rdcMgr, sql);
    query.setParameter("MANDT", mandt);
    query.setParameter("KUNNR", kunnr);
    return query.getResults(Knvl.class);
  }

  private void processSizeInfo(EntityManager entityManager, Kna1 kna1, Kna1 kna1Clone, List<CloningOverrideMapping> overrideValues) throws Exception {

    List<Sizeinfo> sizeInfo = null;
    List<Sizeinfo> sizeInfoClone = null;
    Sizeinfo cloneInsert = null;
    Timestamp ts = SystemUtil.getCurrentTimestamp();

    sizeInfo = getSizeInfoByKunnr(entityManager, kna1.getId().getMandt(), kna1.getId().getKunnr());
    sizeInfoClone = getSizeInfoByKunnr(entityManager, kna1Clone.getId().getMandt(), kna1Clone.getId().getKunnr());
    if (sizeInfo != null && sizeInfo.size() > 0 && sizeInfoClone.size() == 0) {
      try {
        for (Sizeinfo current : sizeInfo) {
          cloneInsert = new Sizeinfo();
          SizeinfoPK pk = new SizeinfoPK();
          pk.setKunnr(kna1Clone.getId().getKunnr());
          pk.setMandt(kna1Clone.getId().getMandt());
          pk.setSizeunittype(current.getId().getSizeunittype());

          PropertyUtils.copyProperties(cloneInsert, current);

          overrideConfigChanges(entityManager, overrideValues, cloneInsert, "SIZEINFO", pk);

          cloneInsert.setId(pk);

          cloneInsert.setChgTs(ts);
          cloneInsert.setCreateDate(ts);

          createEntity(cloneInsert, entityManager);
        }

      } catch (Exception e) {
        LOG.debug("Error in copy sizeInfo :", e);
      }
    } else {
      LOG.info("SIZEINFO record not exist with KUNNR " + kna1.getId().getKunnr());
    }

  }

  public List<Sizeinfo> getSizeInfoByKunnr(EntityManager rdcMgr, String mandt, String kunnr) throws Exception {
    String sql = ExternalizedQuery.getSql("GET.SIZEINFO.RECORD");
    PreparedQuery query = new PreparedQuery(rdcMgr, sql);
    query.setParameter("MANDT", mandt);
    query.setParameter("KUNNR", kunnr);
    return query.getResults(Sizeinfo.class);
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

    LegacyDirectObjectContainer legacyObjects = null;
    boolean targetCntryFlag = false;
    try {
      legacyObjects = CloningUtil.getLegacyDBValues(entityManager, targetCntry, cloningQueue.getId().getCmrNo(), false, false);
      targetCntryFlag = getLegacyCustCount(entityManager, targetCntry, cloningQueue.getClonedCmrNo());
    } catch (Exception e) {
      LOG.debug("No duplicate cmr in legacy for target country :" + targetCntry);
    }

    if (legacyObjects != null && targetCntryFlag) {
      CmrtCust cust = legacyObjects.getCustomer();

      if (cust == null) {
        LOG.debug("No duplicate cmr in legacy for target country :" + targetCntry);
      } else {
        processLegacyCloningProcess(entityManager, cloningQueue, targetCntry);
      }
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

  private synchronized String generateCMRNoNonLegacy(EntityManager entityManager, String cmrIssuingCntry, String cmrNo, CmrCloningQueue cloningQueue)
      throws Exception {
    LOG.debug("Inside generateCMRNoNonLegacy method ");
    String mandt = SystemConfiguration.getValue("MANDT");
    // CloningUtil cUtil = new CloningUtil();
    String kukla = "";
    if (StringUtils.isBlank(cloningQueue.getLastUpdtBy())) {
      kukla = CloningUtil.getKuklaFromCMR(entityManager, cmrIssuingCntry, cmrNo, mandt);
    } else {
      kukla = cloningQueue.getLastUpdtBy();
    }

    GEOHandler geoHandler = RequestUtils.getGEOHandler(cmrIssuingCntry);
    String generatedCmrNo = "";
    if (geoHandler != null) {
      generatedCmrNo = geoHandler.getCMRNo(entityManager, kukla, mandt, cmrIssuingCntry, cmrNo, cloningQueue);
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
        synchronized (this) {
          try {
            processCloningRecord(entityManager, cloningQueue);
          } catch (Exception e) {
            partialRollback(entityManager);
            LOG.error("Unexpected error occurred during reservedcmr and cloning queue insertion " + cloningQueue.getId().getCmrNo(), e);
            if ("CMR No or Country missing".equals(e.getMessage())) {
              cloningQueue.setStatus("STOP");
              cloningQueue.setErrorMsg("CMR No or Country missing");
              updateEntity(cloningQueue, entityManager);
            }
            break;
          }
          partialCommit(entityManager);
        }

      case "IN_PROG":
      case "LEGACY_ERR":
        if (!"LEGACYSKIP".equalsIgnoreCase(cloningQueue.getStatus())) {
          try {
            processLegacyCloningProcess(entityManager, cloningQueue, "NA");
            transformer = TransformerManager.getTransformer(cloningQueue.getId().getCmrIssuingCntry());
            if (transformer != null) {
              cntryDup = transformer.getDupCreationCountryId(entityManager, cloningQueue.getId().getCmrIssuingCntry(),
                  cloningQueue.getId().getCmrNo());
              if (!"NA".equals(cntryDup)) {
                processLegacyCloningDupCreate(entityManager, cloningQueue, cntryDup);
              }
            }

          } catch (Exception e) {
            partialRollback(entityManager);
            if ("CMR not found in Legacy".equals(e.getMessage())) {
              cloningQueue.setStatus("STOP");
              cloningQueue.setErrorMsg("CMR not found in Legacy");
            } else if ("CMR cancelled in Legacy".equals(e.getMessage())) {
              cloningQueue.setStatus("STOP");
              cloningQueue.setErrorMsg("CMR Cancelled in Legacy");
            } else {
              cloningQueue.setStatus("LEGACY_ERR");
              cloningQueue.setErrorMsg("Error occured during legacy cloning");
            }

            updateEntity(cloningQueue, entityManager);
            LOG.error("Unexpected error occurred during legacy cloning process for CMR No :" + cloningQueue.getId().getCmrNo(), e);
            // processError(entityManager, null, e.getMessage());
            break;
          }
          partialCommit(entityManager);
        }

      case "LEGACY_OK":
      case "LEGACYSKIP":
        try {
          processCloningRDC(entityManager, cloningQueue, "NA");
          transformer = TransformerManager.getTransformer(cloningQueue.getId().getCmrIssuingCntry());
          if (transformer != null) {
            cntryDup = transformer.getDupCreationCountryId(entityManager, cloningQueue.getId().getCmrIssuingCntry(), cloningQueue.getId().getCmrNo());
            if (!"NA".equals(cntryDup)) {
              processRdcCloningDupCreate(entityManager, cloningQueue, cntryDup);
            }
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
        try {
          processRDCParentChildRecords(entityManager, cloningQueue);
        } catch (Exception e) {
          partialRollback(entityManager);
          LOG.error("Error occurred during kna1 and kna1 child cloning process for CMR No : " + cloningQueue.getId().getCmrNo());
          break;
        }

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

    MessageTransformer transformer = TransformerManager.getTransformer(cloningQueue.getId().getCmrIssuingCntry());
    String cntryDup = "NA";
    boolean dupCreateFlag = false;
    if (transformer != null) {
      cntryDup = transformer.getDupCreationCountryId(entityManager, cloningQueue.getId().getCmrIssuingCntry(), cloningQueue.getId().getCmrNo());
      if (!"NA".equals(cntryDup)) {
        dupCreateFlag = getKNA1Count(entityManager, cntryDup, cloningQueue.getId().getCmrNo());
        if (!dupCreateFlag)
          cntryDup = "NA";
      }
    }

    List<RdcCloningRefn> pendingCloningRefn = getPendingCloningRecordsKNA1RDC(entityManager, cloningQueue, cntryDup);

    LOG.debug((pendingCloningRefn != null ? pendingCloningRefn.size() : 0) + " records to process to KNA1 RDc.");

    List<CloningOverrideMapping> overrideValues = null;
    CloningOverrideUtil overrideUtil = new CloningOverrideUtil();

    for (RdcCloningRefn rdcCloningRefn : pendingCloningRefn) {
      try {
        overrideValues = overrideUtil.getOverrideValueFromMapping(rdcCloningRefn.getCmrIssuingCntry(), cloningQueue.getCreatedBy());
        processCloningKNA1RDC(entityManager, rdcCloningRefn, overrideValues, cloningQueue);
      } catch (Exception e) {
        partialRollback(entityManager);
        LOG.error("Unexpected error occurred during kna1 cloning process for CMR No : " + rdcCloningRefn.getCmrNo(), e);
        processError(entityManager, rdcCloningRefn, cloningQueue, "Issue while cloning KNA1 records");
      }
      partialCommit(entityManager);
    }

    // now start cloning at RDc end for KNA1 child tables
    LOG.info("Retrieving pending RDc records for kna1 child processing..");
    List<RdcCloningRefn> pendingCloningRefnChild = getPendingRecordsRDCKna1Child(entityManager, cloningQueue, cntryDup);

    LOG.debug((pendingCloningRefnChild != null ? pendingCloningRefnChild.size() : 0) + " records to process to KNA1 child RDc.");

    Kna1 kna1 = null;
    Kna1 kna1Clone = null;
    List<CloningOverrideMapping> overrideValuesChild = null;

    for (RdcCloningRefn rdcCloningRefn : pendingCloningRefnChild) {
      try {
        kna1 = getKna1ByKunnr(entityManager, rdcCloningRefn.getId().getMandt(), rdcCloningRefn.getId().getKunnr());
        kna1Clone = getKna1ByKunnr(entityManager, rdcCloningRefn.getTargetMandt(), rdcCloningRefn.getTargetKunnr());
        overrideValuesChild = overrideUtil.getOverrideValueFromMapping(rdcCloningRefn.getCmrIssuingCntry(), cloningQueue.getCreatedBy());
        processKna1Children(entityManager, kna1, kna1Clone, overrideValuesChild);
        rdcCloningRefn.setStatus("C");
        updateEntity(rdcCloningRefn, entityManager);

      } catch (Exception e) {
        partialRollback(entityManager);
        LOG.error("Unexpected error occurred during kna1 child cloning process for CMR No : " + rdcCloningRefn.getCmrNo(), e);
        processError(entityManager, rdcCloningRefn, cloningQueue, "Issue while cloning KNA1 child records");
        // updateEntity(rdcCloningRefn, entityManager);
      }
      partialCommit(entityManager);
    }

    // all rdcCloningRefn status complete then update cloningQueue completed
    cmrCloningQueueStatusUpdate(entityManager, cloningQueue, cntryDup);
  }

  public void setCNLastUsedCMR(EntityManager rdcMgr, String newLastUsed, String mandt, String kukla, String katr6) {
    LOG.debug("setCNLastUsedCMR :: START");
    String sql = ExternalizedQuery.getSql("UPDATE.KEY_AUTO_GEN.LST_USED");
    sql = StringUtils.replaceOnce(sql, ":MANDT", "'" + mandt + "'");
    sql = StringUtils.replaceOnce(sql, ":KATR6", "'" + katr6 + "'");
    sql = StringUtils.replaceOnce(sql, ":LST_USED", "'" + newLastUsed + "'");

    if (CmrConstants.CN_KUKLA81.equals(kukla) || CmrConstants.CN_KUKLA85.equals(kukla)) {
      sql = StringUtils.replaceOnce(sql, ":KEYID", "'" + CmrConstants.CN_KUKLA81_KEYID + "'");
    } else if (CmrConstants.CN_KUKLA45.equals(kukla)) {
      sql = StringUtils.replaceOnce(sql, ":KEYID", "'" + CmrConstants.CN_KUKLA45_KEYID + "'");
    } else { // use default key_id
      sql = StringUtils.replaceOnce(sql, ":KEYID", "'" + CmrConstants.CN_DEFAULT_KEYID + "'");
    }

    int rows = rdcMgr.createNativeQuery(sql).executeUpdate();
    LOG.debug("****" + rows + " ROWS WERE AFFECTED BY THE UPDATE");
    LOG.debug("setCNLastUsedCMR :: END :: NEW VALUE >> " + newLastUsed);

  }

  private String convertToIssuingCntry(List<String> list) {
    String ret = "";

    if (list != null && list.size() > 0) {
      for (int i = 0; i < list.size(); i++) {
        String cntry = list.get(i);
        if (StringUtils.isEmpty(ret)) {
          ret = ret + "'" + cntry + "'";
        } else {
          ret = ret + "," + "'" + cntry + "'";
        }
      }
    }

    return ret;
  }

  private void processTransService(EntityManager entityManager, Kna1 kna1, Kna1 kna1Clone, List<CloningOverrideMapping> overrideValues)
      throws Exception {

    List<TransService> transSer = null;
    List<TransService> transSerClone = null;
    TransService cloneInsert = null;
    Timestamp ts = SystemUtil.getCurrentTimestamp();

    transSer = getTransSerByKunnr(entityManager, kna1.getKatr6(), kna1.getId().getKunnr());
    transSerClone = getTransSerByKunnr(entityManager, kna1Clone.getKatr6(), kna1Clone.getId().getKunnr());
    if (transSer != null && transSer.size() > 0 && transSerClone.size() == 0) {
      try {
        for (TransService current : transSer) {
          cloneInsert = new TransService();
          TransServicePK pk = new TransServicePK();
          String transServiceId = generateTransServId(entityManager);
          pk.setTransServiceId(transServiceId);

          PropertyUtils.copyProperties(cloneInsert, current);

          overrideConfigChanges(entityManager, overrideValues, cloneInsert, "TRANS_SERVICE", pk);

          cloneInsert.setId(pk);

          if ("ZS01".equalsIgnoreCase(kna1Clone.getKtokd()) && !"90".equals(kna1Clone.getAufsd()))
            cloneInsert.setParSitePrtyId(kna1Clone.getBran5());
          else {
            String parSitePartyid = getParSitePartyId(entityManager, kna1Clone);
            cloneInsert.setChildSitePrtyId(kna1Clone.getBran5());
            cloneInsert.setParSitePrtyId(parSitePartyid);
          }

          cloneInsert.setCmrNum(kna1Clone.getZzkvCusno());
          cloneInsert.setMppNum(kna1Clone.getId().getKunnr());
          cloneInsert.setInsTimestamp(ts);
          cloneInsert.setUpdTimestamp(ts);
          cloneInsert.setInsUserid("CreateCMR");

          createEntity(cloneInsert, entityManager);
        }

      } catch (Exception e) {
        LOG.debug("Error in copy TransService :", e);
      }
    } else {
      LOG.info("TransService record not exist with KUNNR " + kna1.getId().getKunnr());
    }

  }

  public List<TransService> getTransSerByKunnr(EntityManager rdcMgr, String cntry, String kunnr) throws Exception {
    String sql = ExternalizedQuery.getSql("GET.TRANS.SERVICE.RECORD");
    PreparedQuery query = new PreparedQuery(rdcMgr, sql);
    query.setParameter("COUNTRY", cntry);
    query.setParameter("KUNNR", kunnr);
    return query.getResults(TransService.class);
  }

  public String generateTransServId(EntityManager entityManager) {
    String sql = ExternalizedQuery.getSql("SERVICE.TRANS_SERV.ID");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    return query.getSingleResult(String.class);
  }

  private String getParSitePartyId(EntityManager rdcMgr, Kna1 kna1) {
    String retVal = "";

    String sql = ExternalizedQuery.getSql("LD.GET.IERP.SOLDTO.BRAN5");
    PreparedQuery query = new PreparedQuery(rdcMgr, sql);
    query.setParameter("CMR", kna1.getZzkvCusno());
    query.setParameter("KATR6", kna1.getKatr6());
    query.setParameter("MANDT", kna1.getId().getMandt());
    query.setForReadOnly(true);

    List<Object[]> results = query.getResults();

    if (results != null && results.size() > 0) {
      Object[] result = results.get(0);
      retVal = (String) result[0];
    }

    return retVal;

  }

  private boolean getLegacyCustCount(EntityManager entityManager, String targetCntry, String cmrNo) {
    String sql = ExternalizedQuery.getSql("CLONING_LEGACY_CUST_COUNT");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("COUNTRY", targetCntry);
    query.setParameter("CMR_NO", cmrNo);
    int count = query.getSingleResult(Integer.class);
    if (count > 0)
      return false;
    else
      return true;
  }

  private int isExistKnvp(EntityManager entityManager, String kunnr, String mandt, String parvw, String parza, String vkorg, String spart,
      String vtweg) {
    String sql = ExternalizedQuery.getSql("CLONING.KNVP.EXIST.RECORD");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("KUNNR", kunnr);
    query.setParameter("MANDT", mandt);
    query.setParameter("PARVW", parvw);
    query.setParameter("PARZA", parza);
    query.setParameter("VKORG", vkorg);
    query.setParameter("SPART", spart);
    query.setParameter("VTWEG", vtweg);
    query.setForReadOnly(true);
    return query.getSingleResult(Integer.class);
  }

  private int isExistKnvv(EntityManager entityManager, String kunnr, String mandt, String vkorg, String spart, String vtweg) {
    String sql = ExternalizedQuery.getSql("CLONING.KNVV.EXIST.RECORD");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("KUNNR", kunnr);
    query.setParameter("MANDT", mandt);
    query.setParameter("VKORG", vkorg);
    query.setParameter("SPART", spart);
    query.setParameter("VTWEG", vtweg);
    query.setForReadOnly(true);
    return query.getSingleResult(Integer.class);
  }

  private void processStxl(EntityManager entityManager, Knvk knvk, Knvk knvkClone, List<CloningOverrideMapping> overrideValues) throws Exception {

    List<Stxl> stxl = null;
    List<Stxl> stxlClone = null;
    Stxl cloneInsert = null;
    Timestamp ts = SystemUtil.getCurrentTimestamp();

    stxl = getStxlByParnr(entityManager, knvk.getId().getMandt(), knvk.getId().getParnr());
    stxlClone = getStxlByParnr(entityManager, knvkClone.getId().getMandt(), knvkClone.getId().getParnr());
    if (stxl != null && stxl.size() > 0 && stxlClone.size() == 0) {
      try {
        for (Stxl current : stxl) {
          cloneInsert = new Stxl();
          StxlPK stxlPKClone = new StxlPK();
          stxlPKClone.setMandt(knvkClone.getId().getMandt());
          stxlPKClone.setRelid(current.getId().getRelid());
          stxlPKClone.setTdid(current.getId().getTdid());
          stxlPKClone.setTdname(knvkClone.getId().getParnr());
          stxlPKClone.setTdobject(current.getId().getTdobject());
          stxlPKClone.setTdspras(current.getId().getTdspras());
          stxlPKClone.setSrtf2(current.getId().getSrtf2());

          PropertyUtils.copyProperties(cloneInsert, current);

          overrideConfigChanges(entityManager, overrideValues, cloneInsert, "STXL", stxlPKClone);

          cloneInsert.setId(stxlPKClone);

          cloneInsert.setSapTs(ts);
          cloneInsert.setShadUpdateInd("I");
          cloneInsert.setShadUpdateTs(ts);

          createEntity(cloneInsert, entityManager);

        }

      } catch (Exception e) {
        LOG.debug("Error in copy stxl :", e);
      }
    } else {
      LOG.info("STXL record not exist with PARNR " + knvk.getId().getParnr());
    }

  }

  public List<Stxl> getStxlByParnr(EntityManager rdcMgr, String mandt, String parnr) throws Exception {
    String sql = ExternalizedQuery.getSql("CLONING.GET_STXL_BYPARNR");
    PreparedQuery query = new PreparedQuery(rdcMgr, sql);
    query.setParameter("MANDT", mandt);
    query.setParameter("PARNR", parnr);
    return query.getResults(Stxl.class);
  }

  @Override
  public boolean flushOnCommitOnly() {
    return true;
  }

}