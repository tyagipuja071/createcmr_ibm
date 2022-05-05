/**
 * 
 */
package com.ibm.cmr.create.batch.service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.persistence.EntityManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.AdminPK;
import com.ibm.cio.cmr.request.entity.CmrtAddr;
import com.ibm.cio.cmr.request.entity.CmrtCust;
import com.ibm.cio.cmr.request.entity.CmrtCustExt;
import com.ibm.cio.cmr.request.entity.CmrtCustExtPK;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.entity.DataPK;
import com.ibm.cio.cmr.request.entity.MassUpdt;
import com.ibm.cio.cmr.request.entity.MassUpdtAddr;
import com.ibm.cio.cmr.request.entity.MassUpdtData;
import com.ibm.cio.cmr.request.entity.MassUpdtDataPK;
import com.ibm.cio.cmr.request.entity.ReqCmtLog;
import com.ibm.cio.cmr.request.entity.SuppCntry;
import com.ibm.cio.cmr.request.entity.WfHist;
import com.ibm.cio.cmr.request.entity.listeners.ChangeLogListener;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.util.RequestUtils;
import com.ibm.cio.cmr.request.util.SystemLocation;
import com.ibm.cio.cmr.request.util.SystemUtil;
import com.ibm.cio.cmr.request.util.legacy.LegacyDirectObjectContainer;
import com.ibm.cio.cmr.request.util.legacy.LegacyDirectUtil;
import com.ibm.cmr.create.batch.util.BatchUtil;
import com.ibm.cmr.create.batch.util.CMRRequestContainer;
import com.ibm.cmr.create.batch.util.masscreate.WorkerThreadFactory;
import com.ibm.cmr.create.batch.util.mq.LandedCountryMap;
import com.ibm.cmr.create.batch.util.mq.transformer.MessageTransformer;
import com.ibm.cmr.create.batch.util.mq.transformer.TransformerManager;
import com.ibm.cmr.create.batch.util.worker.impl.LDMassUpdtDb2MultiWorker;

/**
 * @author JeffZAMORA
 *
 */
public class LDMassProcessMultiLegacyService extends MultiThreadedBatchService<Long> {

  private static final Logger LOG = Logger.getLogger(LDMassProcessMultiLegacyService.class);

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
  public static final String MASS_UPDATE_FAIL = "FAIL";
  private static final String MASS_UPDATE_LEGACYDONE = "LDONE";
  public static final String MASS_UDPATE_LEGACY_FAIL_MSG = "Errors happened in legacy mass updates. Pleaes see request summary for details.";
  private static final int MASS_UPDATE_TXT_LEN = 10000;

  private static final List<String> SKIP_RDc_COUNTRY_LIST = Arrays.asList("675");

  @Override
  protected Queue<Long> getRequestsToProcess(EntityManager entityManager) {
    LOG.info("Initializing Country Map..");
    LandedCountryMap.init(entityManager);
    // Retrieve the PCP records and create in the Legacy DB
    LOG.info("Retreiving pending records for processing..");
    String sql = ExternalizedQuery.getSql("LEGACYD.GET_PENDING");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    List<Admin> pending = query.getResults(Admin.class);
    LinkedList<Long> toProcess = new LinkedList<>();
    LOG.debug((pending != null ? pending.size() : 0) + " records to process.");
    if (pending != null) {
      for (Admin admin : pending) {
        toProcess.add(admin.getId().getReqId());
      }
    }
    return toProcess;
  }

  @Override
  public Boolean executeBatchForRequests(EntityManager entityManager, List<Long> requests) throws Exception {
    ChangeLogListener.setManager(entityManager);
    for (Long reqId : requests) {
      AdminPK pk = new AdminPK();
      pk.setReqId(reqId);
      Admin admin = entityManager.find(Admin.class, pk);
      try {
        switch (admin.getReqType()) {
        case CmrConstants.REQ_TYPE_MASS_UPDATE:
          processMassUpdate(entityManager, admin);
          break;
        }

      } catch (Exception e) {
        if (!LegacyDirectLegacyMassProcessService.MASS_UDPATE_LEGACY_FAIL_MSG.equalsIgnoreCase(e.getMessage())) {
          partialRollback(entityManager);
        }
        LOG.error("Unexpected error occurred during processing of Request " + admin.getId().getReqId(), e);
        processError(entityManager, admin, e.getMessage());
      }
      partialCommit(entityManager);
    }
    ChangeLogListener.clean();
    return true;
  }

  /**
   * This is the batch process for mass update requests
   * 
   * @param entityManager
   * @param admin
   */
  public void processMassUpdate(EntityManager entityManager, Admin admin) throws Exception {
    try {
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

        int threads = 5;
        String threadCount = BatchUtil.getProperty("multithreaded.threadCount");
        if (threadCount != null && StringUtils.isNumeric(threadCount)) {
          threads = Integer.parseInt(threadCount);
        }

        LOG.debug("Starting processing mass update lines at " + new Date());
        List<LDMassUpdtDb2MultiWorker> workers = new ArrayList<LDMassUpdtDb2MultiWorker>();
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(threads, new WorkerThreadFactory(getThreadName()));
        int currCount = 0;
        for (MassUpdt massUpdt : results) {
          LDMassUpdtDb2MultiWorker worker = new LDMassUpdtDb2MultiWorker(this, admin, massUpdt, this);
          executor.schedule(worker, currCount, TimeUnit.SECONDS);
          currCount++;
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

        LOG.debug("Finished processing mass update lines at " + new Date());

        Throwable processError = null;
        for (LDMassUpdtDb2MultiWorker worker : workers) {
          if (worker != null) {
            if (worker.isError()) {
              LOG.error("Error in processing mass update for Request ID " + admin.getId().getReqId() + ": " + worker.getErrorMsg());
              if (processError == null && worker.getErrorMsg() != null) {
                processError = worker.getErrorMsg();
              }
            }
          }
        }
        if (processError != null) {
          throw new Exception(processError);
        }

        admin.setLastUpdtTs(SystemUtil.getCurrentTimestamp());

        // DTN: This means errors happened in Legacy processing.
        completeMassUpdateRecord(entityManager, admin, errorCmrs);
      }
      partialCommit(entityManager);
    } catch (Exception e) {
      LOG.error("Error in processing mass Update Request " + admin.getId().getReqId(), e);
      addError("Mass Update Request " + admin.getId().getReqId() + " Error: " + e.getMessage());
    }
  }

  /*
   * Helper method for processing Italy mass updates
   */
  public void processMassUpdateIT(EntityManager entityManager, LegacyDirectObjectContainer legacyObjects, Admin admin, MassUpdt mUpdate, Data data,
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
   * This is a method to map the mass update request data
   * 
   * @param entityManager
   * @param cmrObjects
   * @param muData
   * @return
   * @throws Exception
   */
  public LegacyDirectObjectContainer mapRequestDataForMassUpdate(EntityManager entityManager, CMRRequestContainer cmrObjects, MassUpdt massUpdt,
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
      // partialCommit(entityManager);
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
      // partialCommit(entityManager);
      errorCmrs.add(massUpdt.getCmrNo());
      return legacyObjects;
    }

    transformer.transformLegacyCustomerDataMassUpdate(entityManager, cust, cmrObjects, muData);

    if (transformer.hasCmrtCustExt()) {
      CmrtCustExt custExt = legacyObjects.getCustomerExt();
      if (transformer != null && custExt != null) {
        try {
          transformer.transformLegacyCustomerExtDataMassUpdate(entityManager, custExt, cmrObjects, muData, massUpdt.getCmrNo());
        } catch (Exception e) {
          legacyObjects.setErrTxt(e.getMessage());
          massUpdt.setErrorTxt(e.getMessage());
          massUpdt.setRowStatusCd(MASS_UPDATE_FAIL);
          updateEntity(massUpdt, entityManager);
          // partialCommit(entityManager);
          errorCmrs.add(massUpdt.getCmrNo());
          return legacyObjects;
        }
      }
      if (custExt != null) {
        custExt.setUpdateTs(SystemUtil.getCurrentTimestamp());
        custExt.setAeciSubDt(SystemUtil.getDummyDefaultDate());
        legacyObjects.setCustomerExt(custExt);
      } else if (transformer != null
          && (SystemLocation.SLOVAKIA.equals(data.getCmrIssuingCntry()) || SystemLocation.CZECH_REPUBLIC.equals(data.getCmrIssuingCntry()))) {
        CmrtCustExtPK custExtPk = null;
        LOG.debug("Mapping default Data values with Legacy CmrtCustExt table.....");
        // Initialize the object
        custExt = initEmpty(CmrtCustExt.class);
        // default mapping for ADDR and CMRTCEXT
        custExtPk = new CmrtCustExtPK();
        custExtPk.setCustomerNo(massUpdt.getCmrNo());
        custExtPk.setSofCntryCode(cntry);
        custExt.setId(custExtPk);

        if (transformer != null) {
          transformer.transformLegacyCustomerExtDataMassUpdate(entityManager, custExt, cmrObjects, muData, massUpdt.getCmrNo());
        }
        custExt.setUpdateTs(SystemUtil.getCurrentTimestamp());
        custExt.setAeciSubDt(SystemUtil.getDummyDefaultDate());
        createEntity(custExt, entityManager);
        legacyObjects.setCustomerExt(custExt);
      } else if (transformer != null && (SystemLocation.ROMANIA.equals(data.getCmrIssuingCntry()))) {
        CmrtCustExtPK custExtPk = null;
        LOG.debug("Mapping default Data values with Legacy CmrtCustExt table.....");
        // Initialize the object
        custExt = initEmpty(CmrtCustExt.class);
        // default mapping for ADDR and CMRTCEXT
        custExtPk = new CmrtCustExtPK();
        custExtPk.setCustomerNo(massUpdt.getCmrNo());
        custExtPk.setSofCntryCode(cntry);
        custExt.setId(custExtPk);

        if (transformer != null) {
          transformer.transformLegacyCustomerExtDataMassUpdate(entityManager, custExt, cmrObjects, muData, massUpdt.getCmrNo());
        }
        custExt.setUpdateTs(SystemUtil.getCurrentTimestamp());
        custExt.setAeciSubDt(SystemUtil.getDummyDefaultDate());
        createEntity(custExt, entityManager);
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
        capsAndFillNulls(legacyAddr, true);
        // DTN: Set again the CmrtCust object on the legacy objects
        // container just to be sure
        legacyObjects.setCustomer(cust);

      }

      if (!StringUtils.isEmpty(errTxt)) {
        legacyObjects.setErrTxt(errTxt);
        massUpdt.setErrorTxt(errTxt);
        massUpdt.setRowStatusCd(MASS_UPDATE_FAIL);
        updateEntity(massUpdt, entityManager);
        // partialCommit(entityManager);
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

  public LegacyDirectObjectContainer mapRequestDataForMassUpdateIT(EntityManager entityManager, CMRRequestContainer cmrObjects, MassUpdt massUpdt,
      List<String> errorCmrs, Admin admin) throws Exception {
    // DENNIS: Remember, we do not need to process addresses
    // List<MassUpdtAddr> muAddrs = cmrObjects.getMassUpdateAddresses();
    Data data = cmrObjects.getData();
    LegacyDirectObjectContainer legacyObjects = new LegacyDirectObjectContainer();
    MassUpdtData muData = cmrObjects.getMassUpdateData();
    String cntry = data != null ? data.getCmrIssuingCntry() : "";
    MessageTransformer transformer = TransformerManager.getTransformer(cntry);

    if (!StringUtils.isBlank(muData.getNewEntpName1())) {
      // we perform check if it is not yet used,
      if (!"@".equals(muData.getNewEntpName1().trim())
          && LegacyDirectUtil.isFisCodeUsed(entityManager, SystemLocation.ITALY, muData.getNewEntpName1(), massUpdt.getCmrNo())) {
        errorCmrs.add("Entered Fiscal Code for CMR:" + massUpdt.getCmrNo() + " and Fiscal Code:" + muData.getNewEntpName1() + " is already in use.");
        legacyObjects
            .setErrTxt("Entered Fiscal Code for CMR:" + massUpdt.getCmrNo() + " and Fiscal Code:" + muData.getNewEntpName1() + " is already in use.");
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
      // "Mass update has encountered an error: The list of legacy customer data
      // with the same fiscal code is empty. Please correct the data."));
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
  public CMRRequestContainer prepareRequest(EntityManager entityManager, MassUpdt massUpdt, Admin admin) throws Exception {
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

  public boolean isMassUpdtDataChanges(EntityManager entityManager, MassUpdt massUpdt, Admin admin) throws Exception {
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

    String sql = ExternalizedQuery.getSql("QUERY.GET.COUNTRY");
    PreparedQuery queryData = new PreparedQuery(entityManager, sql);
    queryData.setForReadOnly(true);
    queryData.setParameter("REQ_ID", admin.getId().getReqId());
    String country = queryData.getSingleResult(String.class);

    if (SKIP_RDc_COUNTRY_LIST.contains(country)) {
      admin.setReqStatus("COM");
    } else {
      admin.setReqStatus("PCO");
    }
    admin.setLastUpdtBy(BATCH_USER_ID);
    updateEntity(admin, entityManager);

    String message = "Records updated successfully on the Legacy Database ";

    if (errors != null && errors.size() > 0) {
      message = "Some CMRs were not processed to legacy. Please see request summary for details.<br>Legacy Database procesing is finished.";
    }
    // String errMsg =
    // "Errors happened in LEGACY mass updates. Please see request summary for
    // details.";

    WfHist hist = createHistory(entityManager, message, admin.getReqStatus(), "Legacy Processing", admin.getId().getReqId());

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
  public void processError(EntityManager entityManager, Admin admin, String errorMsg) throws CmrException, SQLException {
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

  @Override
  public boolean flushOnCommitOnly() {
    return true;
  }

  @Override
  protected String getThreadName() {
    return "LDLegacyMulti";
  }

  @Override
  protected boolean useServicesConnections() {
    return true;
  }

  @Override
  public boolean isTransactional() {
    return true;
  }

}
