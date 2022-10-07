/**
 * 
 */
package com.ibm.cmr.create.batch.util.worker.impl;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.CmrtAddr;
import com.ibm.cio.cmr.request.entity.CmrtCust;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.entity.MassUpdt;
import com.ibm.cio.cmr.request.util.SystemLocation;
import com.ibm.cio.cmr.request.util.SystemUtil;
import com.ibm.cio.cmr.request.util.legacy.LegacyDirectObjectContainer;
import com.ibm.cmr.create.batch.service.LDMassProcessMultiLegacyService;
import com.ibm.cmr.create.batch.service.MultiThreadedBatchService;
import com.ibm.cmr.create.batch.util.CMRRequestContainer;
import com.ibm.cmr.create.batch.util.worker.MassUpdateMultiWorker;

/**
 * @author 136786PH1
 *
 */
public class LDMassUpdtDb2MultiWorker extends MassUpdateMultiWorker {

  private static final Logger LOG = Logger.getLogger(LDMassUpdtDb2MultiWorker.class);
  private static final String MASS_UPDATE_LEGACYDONE = "LDONE";

  private List<String> errorCmrs = new ArrayList<String>();
  private LDMassProcessMultiLegacyService service;

  /**
   * @param parentAdmin
   * @param parentEntity
   */
  public LDMassUpdtDb2MultiWorker(MultiThreadedBatchService<?> parentService, Admin parentAdmin, MassUpdt parentEntity,
      LDMassProcessMultiLegacyService service) {
    super(parentService, parentAdmin, parentEntity);
    this.service = service;

  }

  @Override
  public void executeProcess(EntityManager entityManager) throws Exception {
    LOG.debug("BEGIN PROCESSING CMR# >> " + this.parentRow.getCmrNo());
    CMRRequestContainer cmrObjects = service.prepareRequest(entityManager, this.parentRow, this.parentAdmin);
    Data data = cmrObjects.getData();
    StringBuffer errTxt = new StringBuffer(this.parentRow.getErrorTxt());

    if (!isOwnerCorrect(entityManager, this.parentRow.getCmrNo(), data.getCmrIssuingCntry())) {
      String errorMsg = "The CMR " + this.parentRow.getCmrNo() + " is not owned by IBM. Please remove it and reupload the spreadsheet.";
      if (!errTxt.toString().contains(errorMsg))
        errTxt.append(errorMsg);
      this.parentRow.setErrorTxt(errTxt.toString());
      this.parentRow.setRowStatusCd(MASS_UPDATE_FAIL);
      entityManager.merge(this.parentRow);
      this.setError(true);
      this.setErrorMsg(new Throwable("Some CMRs on the request are not owned by IBM. Please check the Summary for more details."));
      return;
    }

    // for every mass update data
    // prepare the createCMR data to be saved
    LegacyDirectObjectContainer legacyObjects = service.mapRequestDataForMassUpdate(entityManager, cmrObjects, this.parentRow, this.errorCmrs,
        this.parentAdmin);

    if (legacyObjects.getErrTxt() != null
        && legacyObjects.getErrTxt().contains("Mass Update can not process if there are data changes and there are more than")) {
      return;
    } else if (legacyObjects.getErrTxt() != null && legacyObjects.getErrTxt().contains("does not exist on the Legacy DB.")) {
      return;
    }

    if (data != null && SystemLocation.ITALY.equals(data.getCmrIssuingCntry()) && legacyObjects != null) {
      service.processMassUpdateIT(entityManager, legacyObjects, this.parentAdmin, this.parentRow, data, this.errorCmrs);
      return;
    } else if (data != null && SystemLocation.ITALY.equals(data.getCmrIssuingCntry()) && legacyObjects == null) {
      this.errorCmrs.add("The parameter legacyObjects is null. Mass updates can not proceed.");
      return;
    }

    // finally update all data
    CmrtCust legacyCust = legacyObjects.getCustomer();

    if (legacyCust == null) {
      this.parentRow.setRowStatusCd(MASS_UPDATE_FAIL);

      if (!StringUtils.isEmpty(errTxt.toString())) {
        errTxt.append("<br/>");
      }

      errTxt.append("Legacy customer record cannot be updated because it does not exist on LEGACY. CMR NO:" + this.parentRow.getCmrNo());
      this.parentRow.setErrorTxt(errTxt.toString());
      entityManager.merge(this.parentRow);
      return;
      // throw new Exception("Customer record cannot be updated.");
    }
    LOG.info("Mass Updating Legacy Records for Request ID " + this.parentAdmin.getId().getReqId());
    LOG.info(" - SOF Country: " + legacyCust.getId().getSofCntryCode() + " CMR No.: " + legacyCust.getId().getCustomerNo());
    LOG.info("legacyCust.getUpdateTs() before==" + legacyCust.getUpdateTs());
    LOG.info("SystemUtil.getCurrentTimestamp() Cust==" + SystemUtil.getCurrentTimestamp());
    legacyCust.setUpdateTs(SystemUtil.getCurrentTimestamp());
    LOG.info("legacyCust.getUpdateTs() after==" + legacyCust.getUpdateTs());

    entityManager.merge(legacyCust);

    if (legacyObjects.getCustomerExt() != null) {
      LOG.info("legacyCustExt.getUpdateTs() before==" + legacyObjects.getCustomerExt().getUpdateTs());
      LOG.info("SystemUtil.getCurrentTimestamp() CustExt==" + SystemUtil.getCurrentTimestamp());
      legacyObjects.getCustomerExt().setUpdateTs(SystemUtil.getCurrentTimestamp());
      LOG.info("legacyCustExt.getUpdateTs() after==" + legacyObjects.getCustomerExt().getUpdateTs());

      entityManager.merge(legacyObjects.getCustomerExt());
    }

    for (CmrtAddr legacyAddr : legacyObjects.getAddresses()) {
      if (legacyAddr.isForUpdate()) {
        legacyAddr.setUpdateTs(SystemUtil.getCurrentTimestamp());
        entityManager.merge(legacyAddr);
      } else if (legacyAddr.isForCreate()) {
        entityManager.persist(legacyAddr);
      }
    }

    // CMR-2279: update muData
    if (SystemLocation.TURKEY.equals(data.getCmrIssuingCntry())) {
      entityManager.merge(cmrObjects.getMassUpdateData());
    }
    LOG.info("legacyObjects.getErrTxt()==" + legacyObjects.getErrTxt() + "==CMR==" + this.parentRow.getCmrNo());
    if (StringUtils.isEmpty(legacyObjects.getErrTxt())) {
      LOG.info("this.parentRow.getCmrNo()==" + this.parentRow.getCmrNo());
      this.parentRow.setRowStatusCd(MASS_UPDATE_LEGACYDONE);
      this.parentRow.setErrorTxt("Legacy data processing completed.\n\n");
      if (this.parentRow.getErrorTxt() != null && this.parentRow.getErrorTxt().length() > 10000) {
        this.parentRow.setErrorTxt(this.parentRow.getErrorTxt().substring(0, 9999));
      }
      entityManager.merge(this.parentRow);
      LOG.info("legacyObjects.getErrTxt()==" + this.parentRow.getRowStatusCd() + "==CMR==" + this.parentRow.getCmrNo());
    }

    LOG.debug("END PROCESSING CMR# >> " + this.parentRow.getCmrNo());
  }

  public List<String> getErrorCmrs() {
    return this.errorCmrs;
  }

}
