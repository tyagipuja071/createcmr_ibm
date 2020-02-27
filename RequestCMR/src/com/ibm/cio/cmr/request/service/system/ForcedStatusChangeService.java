/**
 * 
 */
package com.ibm.cio.cmr.request.service.system;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.model.BaseModel;
import com.ibm.cio.cmr.request.model.system.ForcedStatusChangeModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.BaseService;
import com.ibm.cio.cmr.request.user.AppUser;
import com.ibm.cio.cmr.request.util.RequestUtils;
import com.ibm.cio.cmr.request.util.SystemUtil;

/**
 * @author Jeffrey Zamora
 * 
 */
@Component
public class ForcedStatusChangeService extends BaseService<ForcedStatusChangeModel, Admin> {

  private static final String STATUS_CHG_DEFAULT_PREFIX = "***** FORCED STATUS CHANGE ***** ";
  private static final String FORCE_STATUS_CHG_CMT_PRE_PREFIX = "ACTION \"Forced Status Change\" changed the REQUEST STATUS to \"";
  private static final String FORCE_STATUS_CHG_CMT_POST_PREFIX = "\"<br/>";

  @Override
  protected Logger initLogger() {
    return Logger.getLogger(ForcedStatusChangeService.class);
  }

  @Override
  protected void performTransaction(ForcedStatusChangeModel model, EntityManager entityManager, HttpServletRequest request)
      throws CmrException, SQLException {

    String action = model.getAction();
    if ("FORCE_CHANGE".equals(action)) {

      AppUser user = AppUser.getUser(request);
      Admin admin = getCurrentRecord(model, entityManager, request);
      admin.setReqStatus(model.getNewReqStatus());
      admin.setLockInd(model.getNewLockedInd());
      if (CmrConstants.YES_NO.Y.toString().equals(model.getNewLockedInd())) {
        admin.setLockBy(model.getNewLockedById().toLowerCase());
        admin.setLockByNm(model.getNewLockedByNm());
        admin.setLockTs(SystemUtil.getCurrentTimestamp());
      } else {
        admin.setLockInd(CmrConstants.YES_NO.N.toString());
        admin.setLockBy(null);
        admin.setLockByNm(null);
        admin.setLockTs(null);
      }
      if ("PCP".equals(model.getNewReqStatus())) {
        admin.setProcessedFlag(CmrConstants.YES_NO.N.toString());
        admin.setProcessedTs(null);
      } else {
        admin.setProcessedFlag(model.getNewProcessedFlag());
        admin.setProcessedTs(SystemUtil.getCurrentTimestamp());
      }

      if (CmrConstants.YES_NO.N.toString().equals(model.getNewProcessedFlag())) {
        admin.setProcessedTs(null);
      }

      // if on pending submitted status, ensure Last Processsing Center has a
      // value
      if ("PCP".equals(model.getNewReqStatus()) || "PPN".equals(model.getNewReqStatus()) || "CPN".equals(model.getNewReqStatus())
          || "APN".equals(model.getNewReqStatus())) {
        if (StringUtils.isEmpty(admin.getLastProcCenterNm())) {
          String procCenter = getProcCenter(entityManager, admin.getId().getReqId());
          admin.setLastProcCenterNm(procCenter);
        }
      }

      if (StringUtils.isEmpty(admin.getLockInd())) {
        admin.setLockInd(CmrConstants.YES_NO.N.toString());
      }
      if (StringUtils.isEmpty(admin.getProcessedFlag())) {
        admin.setLockInd(CmrConstants.YES_NO.N.toString());
      }
      if (StringUtils.isEmpty(admin.getDisableAutoProc())) {
        admin.setDisableAutoProc(CmrConstants.YES_NO.N.toString());
      }

      updateEntity(admin, entityManager);

      RequestUtils.createWorkflowHistory(this, entityManager, request, admin, STATUS_CHG_DEFAULT_PREFIX + model.getCmt(), "Forced Status Change");
      if (null != model.getCmt() && !model.getCmt().isEmpty()) {
        String statusDesc = getstatusDescription(model.getNewReqStatus(), entityManager);
        String cmt = FORCE_STATUS_CHG_CMT_PRE_PREFIX + statusDesc + FORCE_STATUS_CHG_CMT_POST_PREFIX + model.getCmt();
        RequestUtils.createCommentLog(this, entityManager, user, model.getReqId(), cmt);
      }
    }
  }

  private String getProcCenter(EntityManager entityManager, long reqId) {
    String sql = ExternalizedQuery.getSql("SYSTEM.GETPROCCENTER");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", reqId);
    return query.getSingleResult(String.class);
  }

  @Override
  protected List<ForcedStatusChangeModel> doSearch(ForcedStatusChangeModel model, EntityManager entityManager, HttpServletRequest request)
      throws CmrException {
    List<ForcedStatusChangeModel> results = new ArrayList<ForcedStatusChangeModel>();

    String sql = ExternalizedQuery.getSql("SYSTEM.GETREQUEST");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", model.getReqId());

    List<Admin> records = query.getResults(Admin.class);
    ForcedStatusChangeModel stat = null;
    for (Admin admin : records) {
      stat = new ForcedStatusChangeModel();
      copyValuesFromEntity(admin, stat);
      stat.setState(BaseModel.STATE_EXISTING);
      stat.setSearchReqId(stat.getReqId() + "");
      stat.setCmt(null);
      results.add(stat);
    }
    return results;
  }

  @Override
  protected Admin getCurrentRecord(ForcedStatusChangeModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    String sql = ExternalizedQuery.getSql("SYSTEM.GETREQUEST");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", model.getReqId());

    List<Admin> records = query.getResults(1, Admin.class);
    if (records != null && records.size() > 0) {
      return records.get(0);
    }
    return null;
  }

  @Override
  protected Admin createFromModel(ForcedStatusChangeModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    return null;
  }

  protected String getstatusDescription(String status, EntityManager entityManager) throws CmrException {
    String desc = null;
    String sql = ExternalizedQuery.getSql("status_desc");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("CD", status);

    List<Object[]> results = query.getResults(1);
    for (Object[] result : results) {
      desc = (String) result[5];
    }

    return desc;
  }
}
