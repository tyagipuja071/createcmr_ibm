/**
 * 
 */
package com.ibm.cio.cmr.request.service.system;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.AddrPK;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.AdminPK;
import com.ibm.cio.cmr.request.entity.CmrtAddr;
import com.ibm.cio.cmr.request.entity.CmrtAddrPK;
import com.ibm.cio.cmr.request.entity.CmrtCust;
import com.ibm.cio.cmr.request.entity.CmrtCustExt;
import com.ibm.cio.cmr.request.entity.CmrtCustExtPK;
import com.ibm.cio.cmr.request.entity.CmrtCustPK;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.entity.DataPK;
import com.ibm.cio.cmr.request.entity.RequestChangeLog;
import com.ibm.cio.cmr.request.entity.RequestChangeLogPK;
import com.ibm.cio.cmr.request.model.ParamContainer;
import com.ibm.cio.cmr.request.model.system.CorrectionsModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.BaseSimpleService;
import com.ibm.cio.cmr.request.user.AppUser;
import com.ibm.cio.cmr.request.util.RequestUtils;
import com.ibm.cio.cmr.request.util.SystemLocation;
import com.ibm.cio.cmr.request.util.SystemUtil;

/**
 * Service class to process CMDE Administrator corrections on records
 * 
 * @author 136786PH1
 *
 */
@Component
public class CorrectionsService extends BaseSimpleService<CorrectionsModel> {

  private static final Logger LOG = Logger.getLogger(CorrectionsService.class);

  private static final String PROCESS_TYPE_RETRIEVE = "R";
  private static final String PROCESS_TYPE_CORRECT = "C";
  private static final String CORRECTION_TYPE_REQUEST = "R";
  private static final String CORRECTION_TYPE_LEGACY = "L";
  private static final String CLEAR_CHARACTER = "#";

  public static void main(String[] args) {
    DecimalFormat f = new DecimalFormat("00");
    for (int i = 1; i < 60; i += 2) {
      System.out.print(f.format(i) + ",");
    }
  }

  @Override
  protected CorrectionsModel doProcess(EntityManager entityManager, HttpServletRequest request, ParamContainer params) throws Exception {

    CorrectionsModel model = (CorrectionsModel) params.getParam("model");
    String processType = model.getProcessType();
    String correctionType = model.getCorrectionType();
    AppUser user = AppUser.getUser(request);

    LOG.debug("Corrections Process Type: " + processType + " Correction Type: " + correctionType);
    CorrectionsModel out = null;
    if (!StringUtils.isBlank(processType)) {
      switch (processType) {
      case PROCESS_TYPE_RETRIEVE:

        if (!StringUtils.isBlank(correctionType)) {
          switch (correctionType) {
          case CORRECTION_TYPE_REQUEST:
            out = retrieveRequest(entityManager, user, model);
            break;
          case CORRECTION_TYPE_LEGACY:
            out = retrieveCMR(entityManager, user, model);
            break;
          }
        }

        break;
      case PROCESS_TYPE_CORRECT:

        if (!StringUtils.isBlank(correctionType)) {
          switch (correctionType) {
          case CORRECTION_TYPE_REQUEST:
            out = correctRequest(entityManager, user, model);
            break;
          case CORRECTION_TYPE_LEGACY:
            out = correctLegacyDB2(entityManager, user, model);
            break;
          }
        }
        break;
      }
    }

    out.setProcessType(model.getProcessType());
    out.setCorrectionType(model.getCorrectionType());
    out.setReqId(model.getReqId());
    out.setCmrIssuingCntry(model.getCmrIssuingCntry());
    out.setCmrNo(model.getCmrNo());
    return out;
  }

  /**
   * Retrieves CMR Request data
   * 
   * @param entityManager
   * @param user
   * @param model
   * @return
   * @throws Exception
   */
  private CorrectionsModel retrieveRequest(EntityManager entityManager, AppUser user, CorrectionsModel model) throws Exception {
    CorrectionsModel out = new CorrectionsModel();
    long reqId = model.getReqId();

    LOG.debug("Retreiving request data for " + reqId);
    AdminPK adminPk = new AdminPK();
    adminPk.setReqId(reqId);
    Admin oldAdmin = entityManager.find(Admin.class, adminPk);
    out.setAdmin(oldAdmin);

    DataPK dataPk = new DataPK();
    dataPk.setReqId(reqId);
    Data oldData = entityManager.find(Data.class, dataPk);
    out.setData(oldData);

    String sql = ExternalizedQuery.getSql("BATCH.EMAILENGINE.GETADDR");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", reqId);
    query.setForReadOnly(true);
    List<Addr> addresses = query.getResults(Addr.class);
    out.setAddresses(addresses);

    return out;
  }

  private CorrectionsModel retrieveCMR(EntityManager entityManager, AppUser user, CorrectionsModel model) throws Exception {
    CorrectionsModel out = new CorrectionsModel();
    String cmrNo = model.getCmrNo();
    String cmrIssuingCntry = model.getCmrIssuingCntry();
    if (SystemLocation.IRELAND.equals(cmrIssuingCntry)) {
      // ireland is under UK -866
      cmrIssuingCntry = SystemLocation.UNITED_KINGDOM;
    }

    LOG.debug("Retreiving customer data for " + model.getCmrIssuingCntry() + " - " + cmrNo);
    CmrtCustPK custPk = new CmrtCustPK();
    custPk.setCustomerNo(cmrNo);
    custPk.setSofCntryCode(cmrIssuingCntry);
    CmrtCust cust = entityManager.find(CmrtCust.class, custPk);
    out.setCust(cust);

    LOG.debug("Retreiving customer data extension for " + model.getCmrIssuingCntry() + " - " + cmrNo);
    CmrtCustExtPK extPk = new CmrtCustExtPK();
    extPk.setCustomerNo(cmrNo);
    extPk.setSofCntryCode(cmrIssuingCntry);
    CmrtCustExt ext = entityManager.find(CmrtCustExt.class, extPk);
    out.setCustExt(ext);

    LOG.debug("Retreiving customer addresses for " + model.getCmrIssuingCntry() + " - " + cmrNo);
    String sql = ExternalizedQuery.getSql("LEGACYD.GETADDR");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("COUNTRY", cmrIssuingCntry);
    query.setParameter("CMR_NO", cmrNo);
    query.setForReadOnly(true);
    List<CmrtAddr> addresses = query.getResults(CmrtAddr.class);
    out.setCustAddresses(addresses);

    return out;
  }

  /**
   * Applies the corrections to CMR Request data
   * 
   * @param entityManager
   * @param user
   * @param model
   * @return
   * @throws Exception
   */
  private CorrectionsModel correctRequest(EntityManager entityManager, AppUser user, CorrectionsModel model) throws Exception {
    CorrectionsModel out = new CorrectionsModel();
    long reqId = model.getReqId();

    Admin admin = model.getAdmin();
    AdminPK adminPk = new AdminPK();
    adminPk.setReqId(reqId);
    Admin oldAdmin = entityManager.find(Admin.class, adminPk);

    if (hasCorrections(admin)) {
      LOG.debug("Applying ADMIN corrections for Request " + reqId);
      apply(admin, oldAdmin);
      entityManager.merge(oldAdmin);
    }
    Data data = model.getData();
    if (hasCorrections(data)) {
      DataPK dataPk = new DataPK();
      dataPk.setReqId(reqId);
      Data oldData = entityManager.find(Data.class, dataPk);
      LOG.debug("Applying DATA corrections for Request " + reqId);
      apply(data, oldData);
      entityManager.merge(oldData);
    }
    List<Addr> addresses = model.getAddresses();
    for (Addr addr : addresses) {
      if (hasCorrections(addr)) {
        String newSeqNo = addr.getNewAddrSeq();
        String addrSeq = addr.getId().getAddrSeq();
        if (!StringUtils.isBlank(newSeqNo)) {
          // do an update of the sequence first then update the new address
          updateAddrSeq(entityManager, user, oldAdmin.getReqStatus(), reqId, addr, newSeqNo);
          addrSeq = newSeqNo;
        }
        AddrPK addrPk = new AddrPK();
        addrPk.setReqId(reqId);
        addrPk.setAddrType(addr.getId().getAddrType());
        addrPk.setAddrSeq(addrSeq);
        Addr oldAddr = entityManager.find(Addr.class, addrPk);
        LOG.debug("Applying ADDR corrections for Request " + reqId + " Type: " + addrPk.getAddrType() + "/" + addrPk.getAddrSeq());
        apply(addr, oldAddr);
        entityManager.merge(oldAddr);
      }
    }

    // add a comment on the request about the correction
    String comment = "** Request Corrections Applied **\n\n";
    comment += "Field values on the request were corrected and modified. Please check the request change log for details.";

    LOG.debug("Adding comment log for corrections..");
    RequestUtils.createCommentLogFromBatch(entityManager, user.getIntranetId(), reqId, comment);

    entityManager.flush();
    return out;
  }

  /**
   * Applies the corrections to Legacy DB2 CMR data
   * 
   * @param entityManager
   * @param user
   * @param model
   * @return
   */
  private CorrectionsModel correctLegacyDB2(EntityManager entityManager, AppUser user, CorrectionsModel model) throws Exception {
    CorrectionsModel out = new CorrectionsModel();
    String cmrNo = model.getCmrNo();
    String cmrIssuingCntry = model.getCmrIssuingCntry();
    if (SystemLocation.IRELAND.equals(cmrIssuingCntry)) {
      // ireland is under UK -866
      cmrIssuingCntry = SystemLocation.UNITED_KINGDOM;
    }

    Timestamp ts = SystemUtil.getActualTimestamp();

    CmrtCust cust = model.getCust();
    CmrtCustPK custPk = new CmrtCustPK();
    custPk.setCustomerNo(cmrNo);
    custPk.setSofCntryCode(cmrIssuingCntry);
    CmrtCust oldCust = entityManager.find(CmrtCust.class, custPk);
    if (hasCorrections(cust)) {
      LOG.debug("Applying CMRTCUST corrections for " + cmrIssuingCntry + " - " + cmrNo);
      apply(cust, oldCust);
      oldCust.setUpdateTs(ts);
      entityManager.merge(oldCust);
    }

    CmrtCustExt ext = model.getCustExt();
    if (hasCorrections(ext)) {
      CmrtCustExtPK extPk = new CmrtCustExtPK();
      extPk.setCustomerNo(cmrNo);
      extPk.setSofCntryCode(cmrIssuingCntry);
      LOG.debug("Applying CMRTCEXT corrections for " + cmrIssuingCntry + " - " + cmrNo);
      CmrtCustExt oldExt = entityManager.find(CmrtCustExt.class, extPk);
      apply(ext, oldExt);
      oldExt.setUpdateTs(ts);
      entityManager.merge(oldExt);
    }

    List<CmrtAddr> addresses = model.getCustAddresses();
    for (CmrtAddr addr : addresses) {
      if (hasCorrections(addr)) {
        String newSeqNo = addr.getNewAddrSeq();
        String addrSeq = addr.getId().getAddrNo();
        if (!StringUtils.isBlank(newSeqNo)) {
          // do an update of the sequence first then update the new address
          updateCustAddrSeq(entityManager, user, cmrIssuingCntry, cmrNo, addr, newSeqNo);
          addrSeq = newSeqNo;
        }

        CmrtAddrPK addrPk = new CmrtAddrPK();
        addrPk.setAddrNo(addrSeq);
        addrPk.setCustomerNo(cmrNo);
        addrPk.setSofCntryCode(cmrIssuingCntry);
        CmrtAddr oldAddr = entityManager.find(CmrtAddr.class, addrPk);
        LOG.debug("Applying CMRTADDR corrections for Request " + cmrIssuingCntry + " - " + cmrNo + " Sequence " + addr.getId().getAddrNo());
        apply(addr, oldAddr);
        entityManager.merge(oldAddr);
      }
    }

    entityManager.flush();
    return out;
  }

  /**
   * Calls a manual update of ADDR_SEQ via direct SQL. This function also
   * manually creates a {@link RequestChangeLog} record for the update
   * 
   * @param entityManager
   * @param user
   * @param reqStatus
   * @param reqId
   * @param addr
   * @param newSeqNo
   */
  private void updateAddrSeq(EntityManager entityManager, AppUser user, String reqStatus, long reqId, Addr addr, String newSeqNo) {
    String sql = ExternalizedQuery.getSql("MQREQUEST.UPDATE_ADDR_SEQ");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", reqId);
    query.setParameter("TYPE", addr.getId().getAddrType());
    query.setParameter("OLD_SEQ", addr.getId().getAddrSeq());
    query.setParameter("NEW_SEQ", newSeqNo);
    LOG.debug("Updating Request " + reqId + " ADDR " + addr.getId().getAddrType() + "/" + addr.getId().getAddrSeq() + " to new sequence " + newSeqNo);
    query.executeSql();

    RequestChangeLogPK logPk = new RequestChangeLogPK();
    logPk.setTablName("ADDR");
    logPk.setAddrTyp(addr.getId().getAddrType());
    logPk.setChangeTs(SystemUtil.getActualTimestamp());
    logPk.setFieldName("ADDR_SEQ");
    logPk.setRequestId(reqId);

    RequestChangeLog log = new RequestChangeLog();
    log.setAction("U");
    log.setAddrSequence(addr.getId().getAddrSeq());
    log.setId(logPk);
    log.setNewValue(newSeqNo);
    log.setOldValue(addr.getId().getAddrSeq());
    log.setRequestStatus(reqStatus);
    log.setUserId(user.getIntranetId());

    LOG.debug("Creating changelog for sequence update");
    entityManager.persist(log);
    entityManager.flush();
  }

  /**
   * Calls a manual update of ADDRESS_NO via direct SQL.
   * 
   * @param entityManager
   * @param user
   * @param reqStatus
   * @param reqId
   * @param addr
   * @param newSeqNo
   */
  private void updateCustAddrSeq(EntityManager entityManager, AppUser user, String cmrIssuingCntry, String cmrNo, CmrtAddr addr, String newSeqNo) {
    String sql = "update CMRDB2D.CMRTADDR set ADDRNO = :NEW_SEQ, UPDATE_TS = current timestamp where RCYAA = :COUNTRY and RCUXA = :CMR_NO and ADDRNO = :OLD_SEQ";
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("COUNTRY", cmrIssuingCntry);
    query.setParameter("CMR_NO", cmrNo);
    query.setParameter("OLD_SEQ", addr.getId().getAddrNo());
    query.setParameter("NEW_SEQ", newSeqNo);
    LOG.debug("Updating CMR  " + cmrIssuingCntry + " - " + cmrNo + " ADDR " + addr.getId().getAddrNo() + " to new sequence " + newSeqNo);
    query.executeSql();
    entityManager.flush();
  }

  /**
   * Checks if there are corrections being applied for the given object. Any
   * non-null value on the object is treated as a change. Clearing of values is
   * marked using the {@value #CLEAR_CHARACTER} value
   * 
   * @param object
   * @return
   * @throws IllegalArgumentException
   * @throws IllegalAccessException
   */
  private boolean hasCorrections(Object object) throws IllegalArgumentException, IllegalAccessException {
    if (object == null) {
      return false;
    }
    for (Field field : object.getClass().getDeclaredFields()) {
      if (!Modifier.isStatic(field.getModifiers())) {
        field.setAccessible(true);
        Object value = field.get(object);
        if (value != null) {
          if (Long.class.equals(value.getClass()) || Integer.class.equals(value.getClass())) {
            String numValue = value.toString();
            long val = Long.parseLong(numValue);
            if (val > 0) {
              return true;
            }
            return false;
          } else {
            return true;
          }
        }
      }
    }
    return false;
  }

  /**
   * Applies the changes to the given object.
   * 
   * @param source
   * @param target
   * @throws Exception
   */
  private void apply(Object source, Object target) throws Exception {
    for (Field field : source.getClass().getDeclaredFields()) {
      if (!"id".equals(field.getName()) && !Modifier.isStatic(field.getModifiers())) {
        field.setAccessible(true);
        Object value = field.get(source);
        if (CLEAR_CHARACTER.equals(value)) {
          value = "";
        }
        if (value != null) {
          Method set = null;
          try {
            set = target.getClass().getDeclaredMethod("set" + (field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1)),
                value != null ? value.getClass() : String.class);
          } catch (Exception e) {
            LOG.warn("Setter method for " + field.getName() + " not found.");
            try {
              // try secondary, diff naming convention
              set = findSetter(field.getName(), source.getClass());
//            set = target.getClass().getDeclaredMethod("set" + field.getName(),
//                value != null ? value.getClass() : String.class);
            } catch (Exception e1) {
              LOG.warn("Setter method2 for " + field.getName() + " not found.");
            }
          }
          if (set != null) {
            if (Long.class.equals(value.getClass()) || Integer.class.equals(value.getClass())) {
              String numValue = value.toString();
              long val = Long.parseLong(numValue);
              if (val > 0) {
                LOG.trace("Correction: " + field.getName() + " = " + value);
                set.invoke(target, value);
              }
            } else {
              LOG.trace("Correction: " + field.getName() + " = " + value);
              set.invoke(target, value);
            }
          }
        }
      }
    }
  }

  private Method findSetter(String fieldName, Class<?> clazz){
    for (Method method : clazz.getDeclaredMethods()){
      if (("SET"+fieldName.toUpperCase()).equals(method.getName().toUpperCase())){
        return method;
      }
    }
    return null;
  }
  @Override
  protected boolean isTransactional() {
    return true;
  }

}
