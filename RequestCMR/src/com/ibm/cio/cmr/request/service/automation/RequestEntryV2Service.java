/**
 * 
 */
package com.ibm.cio.cmr.request.service.automation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.automation.util.AutomationConst;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.AddrPK;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.AdminPK;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.entity.DataPK;
import com.ibm.cio.cmr.request.model.requestentry.AddressModel;
import com.ibm.cio.cmr.request.model.requestentry.RequestEntryModel;
import com.ibm.cio.cmr.request.service.requestentry.RequestEntryService;
import com.ibm.cio.cmr.request.user.AppUser;
import com.ibm.cio.cmr.request.util.RequestUtils;
import com.ibm.cio.cmr.request.util.geo.GEOHandler;

/**
 * @author JeffZAMORA
 * 
 */
public class RequestEntryV2Service extends RequestEntryService {

  private static final Logger LOG = Logger.getLogger(RequestEntryV2Service.class);

  @Override
  protected void performTransaction(RequestEntryModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {

    GEOHandler handler = RequestUtils.getGEOHandler(model.getCmrIssuingCntry());

    if (handler != null) {
      handler.alterModelBeforeSave(model);
    }

    LOG.debug("Saving " + model.getCmrIssuingCntry() + "/" + model.getReqType() + " request..");
    performSave(model, entityManager, request, false);

    long reqId = model.getReqId();
    LOG.debug("- Generated ID: " + reqId);

    // also create an address record
    AddressModel addrModel = createFromRequest(request);

    AddrPK pk = new AddrPK();
    pk.setReqId(reqId);
    pk.setAddrType("ZS01");

    String seq = null;
    if (handler != null) {
      seq = handler.generateAddrSeq(entityManager, "ZS01", reqId, model.getCmrIssuingCntry());
    }
    pk.setAddrSeq(seq != null ? seq : "1");

    Addr addr = new Addr();
    List<Addr> addrTypes = new ArrayList<>();
    PropertyUtils.copyProperties(addr, addrModel);
    addr.setDplChkResult(null);
    addr.setId(pk);

    LOG.debug("Creating ZS01 address for Request " + reqId);
    createEntity(addr, entityManager);
    addrTypes.add(addr);

    // now move the request to Automated Processing or Processing Pending
    AdminPK adminPK = new AdminPK();
    adminPK.setReqId(reqId);
    Admin admin = entityManager.find(Admin.class, adminPK);
    admin.setLockInd("N");
    admin.setLockByNm(null);
    admin.setLockBy(null);
    admin.setLockTs(null);
    admin.setDisableAutoProc("N");

    DataPK dataPK = new DataPK();
    dataPK.setReqId(reqId);
    Data data = entityManager.find(Data.class, dataPK);

    if (handler != null) {
      for (Addr address : addrTypes) {
        handler.saveV2Entries(entityManager, model, request, admin, data, address);
      }
    }

    String automationIndc = RequestUtils.getAutomationConfig(entityManager, model.getCmrIssuingCntry());
    if (AutomationConst.AUTOMATE_PROCESSOR.equals(automationIndc) || AutomationConst.AUTOMATE_BOTH.equals(automationIndc)) {
      // AUT
      admin.setReqStatus(AutomationConst.STATUS_AUTOMATED_PROCESSING);
    } else {
      // PPN
      String procCenter = RequestUtils.getProcessingCenter(entityManager, model.getCmrIssuingCntry());
      admin.setLastProcCenterNm(procCenter);
      admin.setReqStatus("PPN");

    }

    updateEntity(admin, entityManager);

    String comments = model.getComment();
    if (!StringUtils.isBlank(comments)) {
      LOG.debug("Saving comments here..");
      AppUser user = AppUser.getUser(request);
      RequestUtils.createCommentLog(this, entityManager, user, reqId, comments);
    }

    RequestUtils.createWorkflowHistory(this, entityManager, request, admin, "The request is sent for processing.", "Submit");

  }

  /**
   * Creates an {@link AddressModel} based on request parameters
   * 
   * @param request
   * @return
   */
  private AddressModel createFromRequest(HttpServletRequest request) {
    AddressModel addrModel = new AddressModel();

    for (Enumeration<String> elems = request.getParameterNames(); elems.hasMoreElements();) {
      String paramName = elems.nextElement();
      try {
        PropertyUtils.copyProperties(addrModel, Collections.singletonMap(paramName, request.getParameter(paramName)));
      } catch (Exception e) {
        // do a one by one copy so that only the errors will be skipped, not
        // whole copy
      }
    }
    return addrModel;
  }

}
