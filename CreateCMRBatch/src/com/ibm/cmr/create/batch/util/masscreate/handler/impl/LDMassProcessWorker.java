package com.ibm.cmr.create.batch.util.masscreate.handler.impl;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.CmrtAddr;
import com.ibm.cio.cmr.request.entity.CmrtCust;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.entity.MassUpdt;
import com.ibm.cio.cmr.request.entity.listeners.ChangeLogListener;
import com.ibm.cio.cmr.request.util.SystemLocation;
import com.ibm.cio.cmr.request.util.SystemUtil;
import com.ibm.cio.cmr.request.util.legacy.LegacyDirectObjectContainer;
import com.ibm.cmr.create.batch.service.LDMassProcessMultiLegacyService;
import com.ibm.cmr.create.batch.util.CMRRequestContainer;

/**
 * 
 * @author JeffZAMORA
 *
 */
public class LDMassProcessWorker implements Runnable {

  private static final Logger LOG = Logger.getLogger(LDMassProcessWorker.class);
  private static final String MASS_UPDATE_FAIL = "FAIL";
  private static final String MASS_UPDATE_LEGACYDONE = "LDONE";

  private EntityManager entityManager;
  private Admin admin;
  private MassUpdt massUpdt;
  private List<String> errorCmrs = new ArrayList<String>();
  private LDMassProcessMultiLegacyService service;

  private boolean error;
  private Exception errorMsg;

  public LDMassProcessWorker(EntityManagerFactory emf, Admin admin, MassUpdt massUpdt, LDMassProcessMultiLegacyService service) {
    // SystemUtil.setManager(entityManager);
    EntityManager entityManager = emf.createEntityManager();
    this.entityManager = entityManager;
    this.admin = admin;
    this.massUpdt = massUpdt;
    this.service = service;
  }

  @Override
  public void run() {
    EntityTransaction transaction = null;
    try {
      ChangeLogListener.setManager(entityManager);
      transaction = entityManager.getTransaction();
      transaction.begin();
      processLDLegacy(entityManager);
      if (transaction != null && transaction.isActive() && !transaction.getRollbackOnly()) {
        transaction.commit();
      }
    } catch (Throwable e) {
      LOG.error("An error was encountered during processing. Transaction will be rolled back.", e);
      if (transaction != null && transaction.isActive()) {
        transaction.rollback();
      }
      throw e;
    } finally {
      if (transaction != null && transaction.isActive()) {
        transaction.rollback();
      }
      // empty the manager
      entityManager.clear();
      entityManager.close();
    }
  }

  private void processLDLegacy(EntityManager entityManager) {
    try {
      LOG.debug("BEGIN PROCESSING CMR# >> " + massUpdt.getCmrNo());
      CMRRequestContainer cmrObjects = service.prepareRequest(entityManager, massUpdt, admin);
      Data data = cmrObjects.getData();

      // for every mass update data
      // prepare the createCMR data to be saved
      LegacyDirectObjectContainer legacyObjects = service.mapRequestDataForMassUpdate(entityManager, cmrObjects, massUpdt, errorCmrs, admin);

      if (legacyObjects.getErrTxt() != null
          && legacyObjects.getErrTxt().contains("Mass Update can not process if there are data changes and there are more than")) {
        return;
      } else if (legacyObjects.getErrTxt() != null && legacyObjects.getErrTxt().contains("does not exist on the Legacy DB.")) {
        return;
      }

      if (data != null && SystemLocation.ITALY.equals(data.getCmrIssuingCntry()) && legacyObjects != null) {
        service.processMassUpdateIT(entityManager, legacyObjects, admin, massUpdt, data, errorCmrs);
        return;
      } else if (data != null && SystemLocation.ITALY.equals(data.getCmrIssuingCntry()) && legacyObjects == null) {
        errorCmrs.add("The parameter legacyObjects is null. Mass updates can not proceed.");
        return;
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
        entityManager.merge(massUpdt);
        entityManager.flush();
        return;
        // throw new Exception("Customer record cannot be updated.");
      }
      LOG.info("Mass Updating Legacy Records for Request ID " + admin.getId().getReqId());
      LOG.info(" - SOF Country: " + legacyCust.getId().getSofCntryCode() + " CMR No.: " + legacyCust.getId().getCustomerNo());
      LOG.info("legacyCust.getUpdateTs() before==" + legacyCust.getUpdateTs());
      LOG.info("SystemUtil.getCurrentTimestamp() Cust==" + SystemUtil.getCurrentTimestamp());
      legacyCust.setUpdateTs(SystemUtil.getCurrentTimestamp());
      LOG.info("legacyCust.getUpdateTs() after==" + legacyCust.getUpdateTs());

      entityManager.merge(legacyCust);
      entityManager.flush();
      // partialCommit(entityManager);

      if (legacyObjects.getCustomerExt() != null) {
        LOG.info("legacyCustExt.getUpdateTs() before==" + legacyObjects.getCustomerExt().getUpdateTs());
        LOG.info("SystemUtil.getCurrentTimestamp() CustExt==" + SystemUtil.getCurrentTimestamp());
        legacyObjects.getCustomerExt().setUpdateTs(SystemUtil.getCurrentTimestamp());
        LOG.info("legacyCustExt.getUpdateTs() after==" + legacyObjects.getCustomerExt().getUpdateTs());

        entityManager.merge(legacyObjects.getCustomerExt());
        entityManager.flush();
        // partialCommit(entityManager);
      }

      for (CmrtAddr legacyAddr : legacyObjects.getAddresses()) {
        if (legacyAddr.isForUpdate()) {
          legacyAddr.setUpdateTs(SystemUtil.getCurrentTimestamp());
          entityManager.merge(legacyAddr);
          entityManager.flush();
          // partialCommit(entityManager);
        } else if (legacyAddr.isForCreate()) {
          entityManager.persist(legacyAddr);
          entityManager.flush();
        }
      }

      // CMR-2279: update muData
      if (SystemLocation.TURKEY.equals(data.getCmrIssuingCntry())) {
        entityManager.merge(cmrObjects.getMassUpdateData());
        entityManager.flush();
      }
      LOG.info("legacyObjects.getErrTxt()==" + legacyObjects.getErrTxt() + "==CMR==" + massUpdt.getCmrNo());
      if (StringUtils.isEmpty(legacyObjects.getErrTxt())) {
        LOG.info("massUpdt.getCmrNo()==" + massUpdt.getCmrNo());
        massUpdt.setRowStatusCd(MASS_UPDATE_LEGACYDONE);
        massUpdt.setErrorTxt("Legacy data processing completed.\n\n");
        if (massUpdt.getErrorTxt() != null && massUpdt.getErrorTxt().length() > 10000) {
          massUpdt.setErrorTxt(massUpdt.getErrorTxt().substring(0, 9999));
        }
        entityManager.merge(massUpdt);
        entityManager.flush();
        LOG.info("legacyObjects.getErrTxt()==" + massUpdt.getRowStatusCd() + "==CMR==" + massUpdt.getCmrNo());
      }
      // entityManager.flush();

      LOG.debug("END PROCESSING CMR# >> " + massUpdt.getCmrNo());
    } catch (Exception e) {
      LOG.error("Error in processing CMR No. : " + massUpdt.getCmrNo(), e);
      this.error = true;
      this.errorMsg = e;
    }
  }

  public boolean isError() {
    return error;
  }

  public Exception getErrorMsg() {
    return errorMsg;
  }

  public List<String> getErrorCmrs() {
    return errorCmrs;
  }

}
