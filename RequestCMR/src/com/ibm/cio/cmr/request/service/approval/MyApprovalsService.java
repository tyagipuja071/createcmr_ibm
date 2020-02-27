/**
 * 
 */
package com.ibm.cio.cmr.request.service.approval;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.entity.MyApprovals;
import com.ibm.cio.cmr.request.entity.MyApprovalsPK;
import com.ibm.cio.cmr.request.model.KeyContainer;
import com.ibm.cio.cmr.request.model.approval.ApprovalResponseModel;
import com.ibm.cio.cmr.request.model.approval.MyApprovalsModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.BaseService;
import com.ibm.cio.cmr.request.user.AppUser;

/**
 * @author JeffZAMORA
 * 
 */
@Component
public class MyApprovalsService extends BaseService<MyApprovalsModel, MyApprovals> {

  private ApprovalService approvalService;

  @Override
  protected Logger initLogger() {
    return Logger.getLogger(MyApprovalsService.class);
  }

  @Override
  protected void performTransaction(MyApprovalsModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    boolean mass = "Y".equals(model.getMass());

    if (mass) {
      processMassApproval(model, entityManager, request);
    } else {
      processSingleApproval(model, entityManager, request);
    }
  }

  private void processSingleApproval(MyApprovalsModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    AppUser user = AppUser.getUser(request);
    if (user == null) {
      throw new Exception("No logged in user.");
    }

    String action = model.getAction();

    this.log.debug("Processing Approval record for Request ID " + model.getReqId() + " Approval ID: " + model.getApprovalId());

    ApprovalResponseModel approval = new ApprovalResponseModel();
    approval.setApproverId(user.getIntranetId());
    approval.setApprovalId(model.getApprovalId());
    approval.setComments(model.getComments());
    approval.setIntranetId(user.getIntranetId());
    approval.setRejReason(model.getRejReason());

    this.approvalService = new ApprovalService();
    switch (action) {
    case "APPROVE":
      approval.setType("A");
      break;
    case "COND_APPROVE":
      approval.setType("C");
      break;
    case "REJECT":
      approval.setType("R");
      break;
    }

    this.approvalService.performTransaction(approval, entityManager, request);
  }

  private void processMassApproval(MyApprovalsModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    AppUser user = AppUser.getUser(request);
    if (user == null) {
      throw new Exception("No logged in user.");
    }
    String action = model.getAction();
    List<KeyContainer> keys = extractKeys(model);
    long reqId = -1;
    long approvalId = -1;
    ApprovalResponseModel approval = null;

    for (KeyContainer key : keys) {
      reqId = Long.parseLong(key.getKey("reqId"));
      approvalId = Long.parseLong(key.getKey("approvalId"));

      this.log.debug("Processing Approval record for Request ID " + reqId + " Approval ID: " + approvalId);

      approval = new ApprovalResponseModel();
      approval.setApproverId(user.getIntranetId());
      approval.setApprovalId(approvalId);
      approval.setComments(model.getComments());
      approval.setRejReason(model.getRejReason());
      approval.setIntranetId(user.getIntranetId());

      this.approvalService = new ApprovalService();
      switch (action) {
      case "APPROVE":
        approval.setType("A");
        break;
      case "COND_APPROVE":
        approval.setType("C");
        break;
      case "REJECT":
        approval.setType("R");
        break;
      }

      this.approvalService.performTransaction(approval, entityManager, request);

    }
  }

  @Override
  protected List<MyApprovalsModel> doSearch(MyApprovalsModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    List<MyApprovalsModel> list = new ArrayList<MyApprovalsModel>();

    String sql = ExternalizedQuery.getSql("APPROVAL.MYAPPROVALS");
    if ("Y".equals(model.getPendingOnly())) {
      sql = StringUtils.replace(sql, "{pending}", " and ap.STATUS = 'PAPR' ");
    } else {
      sql = StringUtils.replace(sql, "{pending}", " ");
    }
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("APPROVER_ID", model.getApproverId().toLowerCase().trim());
    query.setForReadOnly(true);

    MyApprovalsModel outModel = null;
    List<MyApprovals> approvals = query.getResults(1000, MyApprovals.class);
    for (MyApprovals approval : approvals) {
      outModel = new MyApprovalsModel();
      String type = approval.getApprovalType();
      if (type != null && type.contains("|")) {
        String[] parts = type.split("[|]");
        type = parts[0];
      }
      approval.setApprovalType(type);
      copyValuesFromEntity(approval, outModel);

      list.add(outModel);
    }
    return list;
  }

  @Override
  protected MyApprovals getCurrentRecord(MyApprovalsModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    return null;
  }

  @Override
  protected MyApprovals createFromModel(MyApprovalsModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    MyApprovals approvals = new MyApprovals();
    MyApprovalsPK pk = new MyApprovalsPK();
    approvals.setId(pk);

    copyValuesToEntity(model, approvals);
    return approvals;
  }

}
