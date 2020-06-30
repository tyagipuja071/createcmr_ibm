/**
 * 
 */
package com.ibm.cio.cmr.request.service.workflow;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.CompoundEntity;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.entity.DataPK;
import com.ibm.cio.cmr.request.entity.StatusTrans;
import com.ibm.cio.cmr.request.model.workflow.WorkflowRequestsModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.BaseService;
import com.ibm.cio.cmr.request.ui.PageManager;
import com.ibm.cio.cmr.request.user.AppUser;
import com.ibm.cio.cmr.request.util.MessageUtil;
import com.ibm.cio.cmr.request.util.RequestUtils;
import com.ibm.cio.cmr.request.util.SystemUtil;

/**
 * @author Sonali Jain
 * 
 */
@Component
public class WorkflowService extends BaseService<WorkflowRequestsModel, Admin> {

  @Override
  protected Logger initLogger() {
    return Logger.getLogger(WorkflowService.class);
  }

  @Override
  protected void performTransaction(WorkflowRequestsModel model, EntityManager entityManager, HttpServletRequest request)
      throws CmrException, SQLException {
    String action = model.getAction();

    if ("CLAIM".equals(action) || "REPROCESS".equals(action)) {
      String actionCode = CmrConstants.Claim();
      if ("REPROCESS".equals(action)) {
        actionCode = CmrConstants.Reprocess_Checks();
      }

      Admin admin = getCurrentRecord(model, entityManager, request);
      if (admin != null) {
        StatusTrans trans = getStatusTransition(entityManager, admin, actionCode);

        if (CmrConstants.YES_NO.Y.toString().equals(admin.getLockInd())) {
          throw new CmrException(MessageUtil.ERROR_CLAIMED_ALREADY);
        }

        DataPK key = new DataPK();
        key.setReqId(admin.getId().getReqId());
        Data data = entityManager.find(Data.class, key);
        if (data != null) {
          if (PageManager.autoProcEnabled(data.getCmrIssuingCntry(), admin.getReqType()) && "PCP".equals(admin.getReqStatus())) {
            throw new CmrException(MessageUtil.ERROR_CLAIMED_ALREADY);
          }
        }

        if (trans == null) {
          return; // no transition, no processing needed
        }
        AppUser user = AppUser.getUser(request);
        admin.setLastUpdtBy(user.getIntranetId());
        admin.setLastUpdtTs(SystemUtil.getCurrentTimestamp());
        admin.setReqStatus(trans.getNewReqStatus());
        if (CmrConstants.YES_NO.Y.toString().equals(trans.getNewLockedInd())
            && CmrConstants.YES_NO.N.toString().equals(trans.getId().getCurrLockedInd())) {
          // the request is to be locked
          RequestUtils.setClaimDetails(admin, request);
        } else if (CmrConstants.YES_NO.N.toString().equals(trans.getNewLockedInd())
            && CmrConstants.YES_NO.Y.toString().equals(trans.getId().getCurrLockedInd())) {
          // request to be unlocked
          RequestUtils.clearClaimDetails(admin);
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

        // clear cmt field in admin table
        updateEntity(admin, entityManager);

        RequestUtils.addToNotifyList(this, entityManager, user, admin.getId().getReqId());

        String wfAction = "Claimed";
        if ("REPROCESS".equals(action)) {
          wfAction = "Resent for automated checks";
        }
        if (!trans.getId().getCurrReqStatus().equals(trans.getNewReqStatus())) {
          RequestUtils.createWorkflowHistory(this, entityManager, request, admin, "AUTO: " + wfAction + " from Workflow", actionCode);
        }

      }
    }
  }

  private StatusTrans getStatusTransition(EntityManager entityManager, Admin model, String action) {
    String sql = ExternalizedQuery.getSql("REQUESTENTRY.GETNEXTSTATUS");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("CURR_REQ_STATUS", model.getReqStatus());
    query.setParameter("CURR_LOCKED_IND", model.getLockInd() != null ? model.getLockInd() : CmrConstants.YES_NO.N.toString());
    query.setParameter("ACTION", action);
    List<StatusTrans> trans = query.getResults(2, StatusTrans.class);
    if (trans != null && trans.size() > 0) {
      for (StatusTrans transrec : trans) {
        if ("*".equals(transrec.getId().getReqType())) {
          return transrec;
        } else if (transrec.getId().getReqType().equals(model.getReqType())) {
          return transrec;
        }
      }
    }
    return null;
  }

  @Override
  protected List<WorkflowRequestsModel> doSearch(WorkflowRequestsModel model, EntityManager entityManager, HttpServletRequest request)
      throws CmrException {
    List<WorkflowRequestsModel> results = new ArrayList<WorkflowRequestsModel>();

    AppUser user = AppUser.getUser(request);

    String sql = null;
    String queryMapping = Admin.WORKFLOW_REQUESTS_MAPPING;
    String type = model.getWorkflowType();
    String order = user.isShowLatestFirst() ? "desc" : "asc";

    if ("open".equals(type)) {
      sql = ExternalizedQuery.getSql("WORKFLOW.OPEN_REQ_LIST");
      if (user.isHasCountries()) {
        sql += " and data.CMR_ISSUING_CNTRY in (select ISSUING_CNTRY from CREQCMR.USER_PREF_COUNTRIES where REQUESTER_ID = :REQUESTER_ID) ";
      }

      if (user.isShowPendingOnly()) {
        sql += " and (a.LOCK_BY = :REQUESTER_ID or (a.REQ_STATUS in ('PPN', 'APN', 'AWA')) or (a.REQ_STATUS = 'PCP' and a.DISABLE_AUTO_PROC = 'Y') ) ";
      }

      sql += " order by a.REQ_ID " + order;
      if (user.getDefaultNoOfRecords() > 0) {
        sql += " fetch first " + user.getDefaultNoOfRecords() + " rows only ";
      }
    } else if ("completed".equals(type)) {
      sql = ExternalizedQuery.getSql("WORKFLOW.COMPLETED_REQ_LIST");
      if (user.isHasCountries()) {
        sql += " and data.CMR_ISSUING_CNTRY in (select ISSUING_CNTRY from CREQCMR.USER_PREF_COUNTRIES where REQUESTER_ID = :REQUESTER_ID) ";
      }
      sql += " order by a.REQ_ID " + order;
      if (user.getDefaultNoOfRecords() > 0) {
        sql += " fetch first " + user.getDefaultNoOfRecords() + " rows only ";
      }
    } else if ("rejected".equals(type)) {
      sql = ExternalizedQuery.getSql("WORKFLOW.REJECTED_REQ_LIST");
      if (user.isHasCountries()) {
        sql += " and data.CMR_ISSUING_CNTRY in (select ISSUING_CNTRY from CREQCMR.USER_PREF_COUNTRIES where REQUESTER_ID = :REQUESTER_ID) ";
      }
      sql += " order by a.REQ_ID " + order;
      if (user.getDefaultNoOfRecords() > 0) {
        sql += " fetch first " + user.getDefaultNoOfRecords() + " rows only ";
      }
      queryMapping = Admin.WORKFLOW_REJECTED_REQUESTS_MAPPING;
    } else if ("all".equals(type)) {
      sql = ExternalizedQuery.getSql("WORKFLOW.ALL_REQ_LIST");
      sql += " order by a.REQ_ID " + order;
    }

    // order by a.REQ_ID desc
    // String sql = ExternalizedQuery.getSql("WORKFLOW.OPEN_REQ_LIST");
    if (sql != null && !(sql.trim().isEmpty())) {

      PreparedQuery query = new PreparedQuery(entityManager, sql);

      query.setForReadOnly(true);

      query.setParameter("REQUESTER_ID", model.getUserId());
      query.setParameter("PROC_CENTER", AppUser.getUser(request).getProcessingCenter());
      query.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
      List<CompoundEntity> rs = null;
      if ("all".equals(type)) {
        rs = query.getCompundResults(1000, Admin.class, queryMapping);
      } else {
        rs = query.getCompundResults(Admin.class, queryMapping);
      }

      WorkflowRequestsModel workflowRequestsModel = null;

      Admin a = null;
      // WfHist hist = null;
      Data data = null;
      String status = null;
      String reqType = null;
      String claim = null;
      String ownerDesc = null;
      String countryDesc = null;
      String processingStatus = null;
      String canClaim = null;
      String canClaimAll = null;
      String typeDesc = null;
      String rejectReason = null;
      String pendingAppr = null;
      if (rs != null) {
        for (CompoundEntity entity : rs) {
          a = entity.getEntity(Admin.class);
          // hist = entity.getEntity(WfHist.class);
          data = entity.getEntity(Data.class);
          status = (String) entity.getValue("OVERALL_STATUS");
          reqType = (String) entity.getValue("REQ_TYPE_TEXT");
          claim = (String) entity.getValue("CLAIM_FIELD");
          ownerDesc = (String) entity.getValue("OWNER_DESC");
          countryDesc = (String) entity.getValue("COUNTRY_DESC");
          processingStatus = (String) entity.getValue("PROCESSING_STATUS");
          canClaim = (String) entity.getValue("CAN_CLAIM");
          canClaimAll = (String) entity.getValue("CAN_CLAIM_ALL");
          typeDesc = (String) entity.getValue("TYPE_DESCRIPTION");
          pendingAppr = (String) entity.getValue("PENDING_APPROVALS");
          rejectReason = (String) entity.getValue("REJ_REASON");
          workflowRequestsModel = new WorkflowRequestsModel();
          if (a != null) {
            copyValuesFromEntity(a, workflowRequestsModel);
            workflowRequestsModel.setCustName(concat(a.getMainCustNm1(), a.getMainCustNm2()));
          }

          workflowRequestsModel.setRejectReason(rejectReason);

          if (data != null) {
            workflowRequestsModel.setCmrNo(data.getCmrNo());
            workflowRequestsModel.setCmrOwner(data.getCmrOwner());
            workflowRequestsModel.setCmrIssuingCntry(data.getCmrIssuingCntry());
          }

          workflowRequestsModel.setCmrOwnerDesc(ownerDesc);
          workflowRequestsModel.setCmrIssuingCntryDesc(countryDesc);
          workflowRequestsModel.setOverallStatus(status);
          workflowRequestsModel.setReqTypeText(reqType);
          workflowRequestsModel.setClaimField(claim);
          workflowRequestsModel.setProcessingStatus(processingStatus);
          workflowRequestsModel.setCanClaim(canClaim);
          workflowRequestsModel.setCanClaimAll(canClaimAll);
          workflowRequestsModel.setTypeDescription(typeDesc);
          workflowRequestsModel.setProspect(a.getProspLegalInd());
          workflowRequestsModel.setIterationId(a.getIterationId());
          workflowRequestsModel.setReqReason(a.getReqReason());
          workflowRequestsModel.setPendingAppr(pendingAppr);
          workflowRequestsModel.setRequestDueDate(data.getRequestDueDate() == null ? null : getStrFromDate(data.getRequestDueDate()));
          results.add(workflowRequestsModel);
        }
      }
    }
    return results;

  }

  private String getStrFromDate(Date date) {
    String str = null;
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    str = date == null ? null : sdf.format(date);
    return str;
  }

  @Override
  protected Admin getCurrentRecord(WorkflowRequestsModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    String sql = ExternalizedQuery.getSql("WORKFLOW.GETRECORD");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", model.getReqId());
    List<Admin> records = query.getResults(1, Admin.class);
    if (records != null && records.size() > 0) {
      return records.get(0);
    }
    return null;
  }

  @Override
  protected Admin createFromModel(WorkflowRequestsModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    // TODO Auto-generated method stub
    return null;
  }

}
