/**
 * 
 */
package com.ibm.cmr.create.batch.service;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.Changelog;
import com.ibm.cio.cmr.request.entity.ChangelogPK;
import com.ibm.cio.cmr.request.entity.CmrCloningQueue;
import com.ibm.cio.cmr.request.entity.Kna1;
import com.ibm.cio.cmr.request.entity.Kna1PK;
import com.ibm.cio.cmr.request.entity.KunnrExt;
import com.ibm.cio.cmr.request.entity.KunnrExtPK;
import com.ibm.cio.cmr.request.entity.RdcCloningRefn;
import com.ibm.cio.cmr.request.entity.RdcCloningRefnPK;
import com.ibm.cio.cmr.request.entity.Sadr;
import com.ibm.cio.cmr.request.entity.SadrPK;
import com.ibm.cio.cmr.request.entity.ScAccountCmrStg;
import com.ibm.cio.cmr.request.entity.ScAccountCmrStgPK;
import com.ibm.cio.cmr.request.entity.TransService;
import com.ibm.cio.cmr.request.entity.TransServicePK;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.util.JpaManager;
import com.ibm.cio.cmr.request.util.SystemUtil;

/**
 * @author 136786PH1
 *
 */
public class ProspectCloningService extends CloningProcessService {

  private static final Logger LOG = Logger.getLogger(ProspectCloningService.class);

  @Override
  protected List<CmrCloningQueue> getCloningPendingRecords(EntityManager entityManager) {
    LOG.debug("Retreiving pending prospect cloning records");
    String sql = "select * from CREQCMR.CMR_CLONING_QUEUE where STATUS in ('PENDING','LEGACYSKIP','RDC_ERR') and CLONING_PROCESS_CD in ('PROSPECT')";
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    return query.getResults(CmrCloningQueue.class);
  }

  @Override
  public Boolean executeBatchForRequests(EntityManager entityManager, List<CmrCloningQueue> pendingLists) throws Exception {

    SystemUtil.setManager(entityManager);

    for (CmrCloningQueue record : pendingLists) {

      String status = record.getStatus();
      switch (status) {
      case "PENDING":
        assignProspectCMRNo(entityManager, record);
        break;
      case "LEGACYSKIP":
      case "RDC_ERR":
        cloneProspect(entityManager, record);
        break;
      }
    }

    return true;

  }

  /**
   * Assigns the new prospect CMR No.
   * 
   * @param entityManager
   * @param record
   * @throws Exception
   */
  private synchronized void assignProspectCMRNo(EntityManager entityManager, CmrCloningQueue record) throws Exception {
    try {
      String newCmrNo = generateCMRNo(SystemConfiguration.getValue("MANDT"), record.getId().getCmrIssuingCntry());
      if (StringUtils.isBlank(newCmrNo)) {
        throw new Exception("Prospect CMR No. cannot be generated.");
      }
      record.setClonedCmrNo(newCmrNo);
      record.setLastUpdtBy(BATCH_USER_ID);
      record.setLastUpdtTs(SystemUtil.getCurrentTimestamp());
      record.setStatus("LEGACYSKIP");
    } catch (Exception e) {
      LOG.debug("Error in generating CMR No", e);
      record.setStatus("STOP");
      record.setErrorMsg(e.getMessage());
    }
    updateEntity(record, entityManager);
    partialCommit(entityManager);
  }

  /**
   * Clones the prospect record
   * 
   * @param entityManager
   * @param queue
   */
  private void cloneProspect(EntityManager entityManager, CmrCloningQueue record) {
    SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
    String mandt = SystemConfiguration.getValue("MANDT");
    Kna1 kna1 = getKna1Record(entityManager, record);
    if (kna1 == null) {
      record.setStatus("STOP");
      record.setErrorMsg("Prospect record not found.");
    } else {
      try {

        Timestamp now = SystemUtil.getActualTimestamp();

        LOG.debug("Copying KNA1 record..");
        Kna1 kna1Clone = new Kna1();
        PropertyUtils.copyProperties(kna1Clone, kna1);
        String cloneKunnr = generateId(mandt, KUNNR_KEY, entityManager);
        LOG.debug(" - KUNNR " + cloneKunnr + " generated.");
        Kna1PK pkClone = new Kna1PK();
        pkClone.setMandt(mandt);
        pkClone.setKunnr(cloneKunnr);
        kna1Clone.setId(pkClone);
        String scId = record.getCreatedBy();
        kna1Clone.setKatr10("GTS");
        kna1Clone.setZzkvCusno(record.getClonedCmrNo());
        kna1Clone.setErnam(BATCH_USER_ID);
        kna1Clone.setShadUpdateInd("I");
        kna1Clone.setShadUpdateTs(now);
        kna1Clone.setSapTs(now);
        // kna1Clone.setBran5("S" + kna1Clone.getId().getKunnr().substring(1));
        kna1Clone.setErdat(formatter.format(now));
        createEntity(kna1Clone, entityManager);
        createChangeLogs(entityManager, record, kna1Clone, kna1Clone);

        LOG.debug("Copying KUNNR_EXT record..");
        KunnrExtPK extPk = new KunnrExtPK();
        extPk.setKunnr(kna1.getId().getKunnr());
        extPk.setMandt(kna1.getId().getMandt());
        KunnrExt kunnrExt = entityManager.find(KunnrExt.class, extPk);
        KunnrExt kunnrExtTemp = new KunnrExt();
        PropertyUtils.copyProperties(kunnrExtTemp, kunnrExt);

        KunnrExtPK kunnrExtPkClone = new KunnrExtPK();
        kunnrExtPkClone.setKunnr(kna1Clone.getId().getKunnr());
        kunnrExtPkClone.setMandt(kna1Clone.getId().getMandt());
        KunnrExt kunnrExtClone = entityManager.find(KunnrExt.class, kunnrExtPkClone);
        kunnrExtTemp.setId(kunnrExtClone.getId());
        PropertyUtils.copyProperties(kunnrExtClone, kunnrExtTemp);
        kunnrExtClone.setUpdateInd("U");
        kunnrExtClone.setUpdateUser(BATCH_USER_ID);
        kunnrExtClone.setUpdateTs(SystemUtil.getActualTimestamp());
        kunnrExtClone.setCreateTs(now);
        // kunnrExtClone.setCreateUser(BATCH_USER_ID);
        kunnrExtClone.setScAccountId(scId);
        createEntity(kunnrExtClone, entityManager);

        if (!StringUtils.isBlank(kna1.getAdrnr())) {
          Sadr sadr = getSadrRecord(entityManager, kna1);
          if (sadr != null) {
            LOG.debug("Copying SADR record..");
            Sadr sadrClone = new Sadr();
            PropertyUtils.copyProperties(sadrClone, sadr);
            String cloneAdrnr = generateId(mandt, ADRNR_KEY, entityManager);
            kna1Clone.setAdrnr(cloneAdrnr);
            LOG.debug(" - ADRNR " + cloneAdrnr + " generated.");
            SadrPK sadrPkClone = new SadrPK();
            sadrPkClone.setMandt(sadr.getId().getMandt());
            sadrPkClone.setAdrnr(cloneAdrnr);
            sadrPkClone.setNatio(sadr.getId().getNatio());
            sadrClone.setId(sadrPkClone);
            sadrClone.setCrnam(BATCH_USER_ID);
            sadrClone.setCrdat(formatter.format(now));
            sadrClone.setShadUpdateInd("I");
            sadrClone.setShadUpdateTs(now);
            sadrClone.setSapTs(now);
            createEntity(sadrClone, entityManager);
            updateEntity(kna1Clone, entityManager);

            createChangeLogs(entityManager, record, kna1Clone, sadr);

          } else {
            LOG.debug("No SADR record to copy.");
          }
        } else {
          LOG.debug("No SADR record to copy.");
        }

        processSCStaging(entityManager, kna1Clone, kunnrExtClone);

        processTransService(entityManager, record, kna1Clone, kunnrExtClone);

        processRDCReference(entityManager, record, kna1, kna1Clone);
        record.setStatus("COMPLETED");
        record.setErrorMsg("");
      } catch (Exception e) {
        LOG.error("Error in cloning prospect.", e);
        partialRollback(entityManager);
        if ("RDC_ERR".equals(record.getStatus())) {
          record.setStatus("STOP");
        } else {
          record.setStatus("RDC_ERR");
        }
        record.setErrorMsg(e.getMessage());
        if (record.getErrorMsg() != null && record.getErrorMsg().length() > 200) {
          record.setErrorMsg(record.getErrorMsg().substring(0, 199));
        }
      }
    }
    updateEntity(record, entityManager);
    partialCommit(entityManager);

  }

  /**
   * Gets the prospect cmr record
   * 
   * @param entityManager
   * @param record
   * @return
   */
  private Kna1 getKna1Record(EntityManager entityManager, CmrCloningQueue record) {
    String sql = "select * from SAPR3.KNA1 where MANDT = :MANDT and KATR6 = :COUNTRY and ZZKV_CUSNO = :CMR_NO and KTOKD = 'ZS01' and AUFSD = '75'";
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
    query.setParameter("COUNTRY", record.getId().getCmrIssuingCntry());
    query.setParameter("CMR_NO", record.getId().getCmrNo());
    query.setForReadOnly(true);
    Kna1 kna1 = query.getSingleResult(Kna1.class);
    return kna1;
  }

  /**
   * Gets the child SADR record
   * 
   * @param entityManager
   * @param kna1
   * @return
   */
  private Sadr getSadrRecord(EntityManager entityManager, Kna1 kna1) {
    String sql = "select * from SAPR3.SADR where MANDT = :MANDT and ADRNR = :ADRNR";
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
    query.setParameter("ADRNR", kna1.getAdrnr());
    query.setForReadOnly(true);
    Sadr sadr = query.getSingleResult(Sadr.class);
    return sadr;
  }

  /**
   * Creates the SC_ACCOUNT_STG record
   * 
   * @param entityManager
   * @param kna1
   * @param kunnrExt
   */
  protected void processSCStaging(EntityManager entityManager, Kna1 kna1, KunnrExt kunnrExt) {
    String scAccountId = kunnrExt.getScAccountId();

    if (!StringUtils.isBlank(scAccountId)) {
      ScAccountCmrStg scAccount = new ScAccountCmrStg();
      scAccount.setId(new ScAccountCmrStgPK());
      scAccount.getId().setScAccountId(scAccountId);
      scAccount.setCmrSysLocCd(kna1.getKatr6());
      scAccount.setLegacyCmrNo(kna1.getZzkvCusno());

      LOG.debug("Creating SC_ACCOUNT_CMR_STG record for Prospect CMR " + kna1.getZzkvCusno() + " kunnr: " + kna1.getId().getKunnr());
      entityManager.persist(scAccount);
      entityManager.flush();
      LOG.trace("SC_ACCOUNT_CMR_STG record created.");
    } else {
      LOG.debug("Sales Connect Account ID not specified. Skipping SC_ACCOUNT_CMR_STG creation.");
    }

  }

  /**
   * Creates the TRANS_SERVICE record
   * 
   * @param entityManager
   * @param kna1
   * @param kunnrExt
   * @throws Exception
   */
  protected void processTransService(EntityManager entityManager, CmrCloningQueue queue, Kna1 kna1, KunnrExt kunnrExt) throws Exception {

    LOG.debug("Processing TRANS_SERVICE for kunnr: " + kna1.getId().getKunnr());

    int transServiceIdInsUpd = 30031;
    int transServiceIdDel = 0;
    TransService transService = new TransService();

    String transServiceId = generateTransServId(entityManager);
    LOG.debug("TRANS_SERVICE ID for kunnr: " + kna1.getId().getKunnr() + " = " + transServiceId);
    TransServicePK pk = new TransServicePK();
    pk.setTransServiceId(transServiceId);
    transService.setId(pk);

    transService.getId().setTransServiceId(transServiceId);
    transService.setSeqNo(1);
    transService.setChildPartFuncCd("");
    transService.setParBaseUseCd("SLDTO");
    transService.setParSitePrtyId(kna1.getBran5());
    transService.setCmrNum(kna1.getZzkvCusno());
    transService.setCntryCd(kna1.getLand1());
    transService.setMppNum(kna1.getId().getKunnr());
    transService.setCmrSysLocCd(kna1.getKatr6());

    Timestamp ts = SystemUtil.getActualTimestamp();
    transService.setInsUserid(BATCH_USER_ID);
    transService.setInsCiispgm("");
    transService.setInsSystemId(transServiceIdInsUpd);
    transService.setUpdSystemId(transServiceIdInsUpd);
    transService.setLogDelSystemId(transServiceIdDel);
    transService.setLogDelTimestamp(Timestamp.valueOf("9999-12-31 00:00:00.000000"));
    transService.setInsTimestamp(ts);
    transService.setUpdTimestamp(ts);
    transService.setCmrAddrRecType("A");
    transService.setCmrAddrSeqNum("");
    transService.setChildSitePrtyId("");
    LOG.debug("Creating TRANS_SERVICE record for kunnr: " + kna1.getId().getKunnr() + " id: " + transServiceId);
    createEntity(transService, entityManager);

    createChangeLogs(entityManager, queue, kna1, transService);

  }

  /**
   * Creates the RDC_CLONING_REFN record
   * 
   * @param entityManager
   * @param queue
   * @param kna1
   * @param clone
   */
  private void processRDCReference(EntityManager entityManager, CmrCloningQueue queue, Kna1 kna1, Kna1 clone) {
    LOG.debug("Creating Reference record..");
    RdcCloningRefnPK pk = new RdcCloningRefnPK();
    pk.setCmrCloningProcessId(queue.getId().getCmrCloningProcessId());
    pk.setKunnr(kna1.getId().getKunnr());
    pk.setMandt(kna1.getId().getMandt());
    RdcCloningRefn refn = new RdcCloningRefn();
    refn.setId(pk);
    refn.setCmrIssuingCntry(queue.getId().getCmrIssuingCntry());
    refn.setCmrNo(queue.getId().getCmrNo());
    refn.setCreatedBy(BATCH_USER_ID);
    Timestamp now = SystemUtil.getCurrentTimestamp();
    refn.setCreateTs(now);
    refn.setLastUpdtBy(BATCH_USER_ID);
    refn.setLastUpdtTs(now);
    refn.setStatus("C");
    refn.setTargetKunnr(clone.getId().getKunnr());
    refn.setTargetMandt(clone.getId().getMandt());

    createEntity(refn, entityManager);
  }

  /**
   * Calls the stored procedure to generate the next Prospect CMR No. for the
   * country
   * 
   * @param mandt
   * @param katr6
   * @return
   */
  private synchronized String generateCMRNo(String mandt, String katr6) {
    String cmrNo = null;
    try {
      EntityManager entityManager = JpaManager.getEntityManager();
      entityManager.getTransaction().begin();
      Connection conn = entityManager.unwrap(Connection.class);
      CallableStatement stmt = null;

      try {
        LOG.debug("Generating prospect CMR No for MANDT " + mandt + " under " + katr6);
        stmt = conn.prepareCall("CALL NULLID.GEN_PROSPECT_CUST_ID(?,?,?)");
        stmt.setString(1, mandt);
        stmt.setString(2, katr6);
        stmt.registerOutParameter(3, java.sql.Types.VARCHAR);
        stmt.execute();
        cmrNo = stmt.getString(3);

        LOG.debug(" New Prospect CMR No. generated : : " + cmrNo);
      } catch (Exception e) {
        conn.rollback();
        entityManager.getTransaction().rollback();
        LOG.error("Error generating Prospect CMR No.", e);
      } finally {
        try {
          if (stmt != null) {
            stmt.close();
            entityManager.getTransaction().commit();
          }
        } catch (SQLException e) {
          LOG.error("Error generating CMR", e);
        }
      }
    } catch (Exception e) {
      LOG.error("Error generating CMR", e);
    }
    return cmrNo;
  }

  /**
   * Creates the change logs
   * 
   * @param entityManager
   * @param queue
   * @param kna1
   * @param entity
   */
  private void createChangeLogs(EntityManager entityManager, CmrCloningQueue queue, Kna1 kna1, Object entity) {
    Timestamp now = SystemUtil.getActualTimestamp();
    ChangelogPK logpk = new ChangelogPK();
    logpk.setChgts(now);
    logpk.setField("");
    logpk.setMandt(kna1.getId().getMandt());
    logpk.setKunnr(kna1.getId().getKunnr());

    Changelog log = new Changelog();
    log.setId(logpk);
    log.setAction("I");
    log.setActgrp(kna1.getKtokd());
    log.setChgpnt("Y");
    log.setLoadfilename("Cloning");
    log.setLinenumber(queue.getId().getCmrCloningProcessId() + "");
    log.setUserid(BATCH_USER_ID);

    if (entity instanceof Kna1) {
      log.getId().setTab("KNA1");
      log.setTabkey1(kna1.getId().getKunnr());
    } else if (entity instanceof Sadr) {
      log.getId().setTab("SADR");
      Sadr sadr = (Sadr) entity;
      log.setTabkey1(sadr.getId().getAdrnr());
      log.setTabkey2(sadr.getId().getNatio());
    } else if (entity instanceof TransService) {
      log.getId().setTab("TRANS_SER");
      log.setTabkey1(kna1.getId().getKunnr());
    }
    LOG.debug("Creating changelog for " + log.getId().getTab());
    entityManager.persist(log);
    entityManager.flush();

  }

}
