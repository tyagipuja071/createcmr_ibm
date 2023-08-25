/**
 * 
 */
package com.ibm.cio.cmr.request.service.approval;

import java.io.IOException;
import java.io.StringWriter;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.automation.util.geo.ChinaUtil;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.ApprovalComments;
import com.ibm.cio.cmr.request.entity.ApprovalCommentsPK;
import com.ibm.cio.cmr.request.entity.ApprovalReq;
import com.ibm.cio.cmr.request.entity.ApprovalReqPK;
import com.ibm.cio.cmr.request.entity.ApprovalTyp;
import com.ibm.cio.cmr.request.entity.CompoundEntity;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.entity.DefaultApprovalRecipients;
import com.ibm.cio.cmr.request.entity.DefaultApprovals;
import com.ibm.cio.cmr.request.model.approval.ApprovalCommentModel;
import com.ibm.cio.cmr.request.model.approval.ApprovalResponseModel;
import com.ibm.cio.cmr.request.model.requestentry.RequestEntryModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.BaseService;
import com.ibm.cio.cmr.request.service.requestentry.DataService;
import com.ibm.cio.cmr.request.user.AppUser;
import com.ibm.cio.cmr.request.util.BluePagesHelper;
import com.ibm.cio.cmr.request.util.ConfigUtil;
import com.ibm.cio.cmr.request.util.JpaManager;
import com.ibm.cio.cmr.request.util.MessageUtil;
import com.ibm.cio.cmr.request.util.Person;
import com.ibm.cio.cmr.request.util.RequestUtils;
import com.ibm.cio.cmr.request.util.SystemLocation;
import com.ibm.cio.cmr.request.util.SystemParameters;
import com.ibm.cio.cmr.request.util.SystemUtil;
import com.ibm.cio.cmr.request.util.approval.ApprovalUtil;
import com.ibm.cio.cmr.request.util.geo.GEOHandler;
import com.ibm.cio.cmr.request.util.legacy.LegacyDowntimes;
import com.ibm.cio.cmr.request.util.mail.Email;
import com.ibm.cio.cmr.request.util.mail.MessageType;

/**
 * @author Jeffrey Zamora
 * 
 */
@Component
public class ApprovalService extends BaseService<ApprovalResponseModel, ApprovalReq> {

  private static VelocityEngine engine;
  private static final String TEMPLATE_NAME = "approval-generic-email.html";
  private static final String VATEXEMPT_TEMPLATE_NAME = "approval-vatexempt-email.html";
  private static final List<String> PENDING_STATUSES_TO_MOVE = Arrays.asList("DRA", "REP");
  private static final List<String> PENDING_STATUSES_TO_RETURN = Arrays.asList("REP");
  private static final String BP_MANAGER_ID = "BPMANAGER";
  private final DataService dataService = new DataService();

  public static void main(String[] args) {

  }

  @Override
  protected void performTransaction(ApprovalResponseModel approval, EntityManager entityManager, HttpServletRequest request) throws Exception {
    AppUser user = AppUser.getUser(request);
    if ("ADD_APPROVAL".equals(approval.getAction())) {
      if (user.getIntranetId().equals(approval.getIntranetId())) {
        throw new CmrException(MessageUtil.ERROR_CANNOT_ADD_YOURSELF_AS_APPROVAL);
      }

      approval.setApprovalId(SystemUtil.getNextID(entityManager, SystemConfiguration.getValue("MANDT"), "APPROVAL_ID", "CREQCMR"));
      ApprovalReq approver = createFromModel(approval, entityManager, request);
      approver.setStatus(CmrConstants.APPROVAL_DRAFT);

      ApprovalTyp type = getApprovalTypeById(entityManager, approver);
      approver.setTypId(type.getId().getTypId());
      approver.setGeoCd(type.getId().getGeoCd());

      String userCNUM = BluePagesHelper.getCNUMByIntranetAddr(approver.getIntranetId());
      approver.setIntranetId(approval.getIntranetId());
      approver.setDisplayName(approval.getDisplayName());
      approver.setNotesId(BluePagesHelper.getNotesIdByCNUM(userCNUM));

      approver.setCreateBy(user.getIntranetId());
      approver.setCreateTs(this.currentTimestamp);

      approver.setLastUpdtBy(user.getIntranetId());
      approver.setLastUpdtTs(this.currentTimestamp);

      approver.setRequiredIndc(approval.getRequiredIndc());

      createEntity(approver, entityManager);

      createApprovalComment(entityManager, CmrConstants.APPROVAL_DRAFT, user, approval, null);

    } else if ("GET_COMMENTS".equals(approval.getAction())) {
      retrieveComments(entityManager, approval);
    } else if ("SEND_REMINDER".equals(approval.getAction())) {
      processSendReminder(entityManager, user, approval);
    } else if ("CANCEL".equals(approval.getAction())) {
      processCancel(entityManager, user, approval);
    } else if ("OVERRIDE".equals(approval.getAction())) {
      if (user.getIntranetId().equals(approval.getIntranetId())) {
        throw new CmrException(MessageUtil.ERROR_CANNOT_ADD_YOURSELF_AS_APPROVAL);
      }
      processOverride(entityManager, user, approval);
    } else if ("SEND_REQUEST".equals(approval.getAction())) {
      processSendRequest(entityManager, user, approval);
    } else if ("RESUBMIT".equals(approval.getAction())) {
      processResubmit(entityManager, user, approval);
    } else if ("ADMIN_OVERRIDE".equals(approval.getAction())) {
      processAdminOverride(entityManager, user, approval);
    } else {
      this.log.debug("Processing Approval " + approval.getApprovalId() + " " + approval.getType());
      ApprovalReq req = getApprovalRecord(entityManager, approval.getApprovalId());
      Admin admin = fillApprovalInformation(req, approval, entityManager);
      switch (approval.getType()) {
      case "A":
        updateApprovalStatus(entityManager, req, CmrConstants.APPROVAL_APPROVED, approval, admin);
        if (!"REP".equals(admin.getReqStatus())) {
          moveToNextStep(entityManager, admin);

          // CREATCMR-3377 - CN 2.0
          String cmrIssuingCntry = getCmrIssuingCntry(entityManager, req.getReqId());
          if (SystemLocation.CHINA.equals(cmrIssuingCntry)) {
            GEOHandler geoHandler = RequestUtils.getGEOHandler(cmrIssuingCntry);
            geoHandler.setReqStatusAfterApprove(entityManager, approval, req, admin);
            log.debug("Updating Approval Request ID " + req.getId().getApprovalId() + " to " + admin.getReqStatus());
            updateEntity(admin, entityManager);
          }
        }
        approval.setProcessed(true);
        approval.setActionDone(CmrConstants.YES_NO.Y.toString());
        break;
      case "R":
        updateApprovalStatus(entityManager, req, CmrConstants.APPROVAL_REJECTED, approval, admin);
        // moveBackToRequester(entityManager, admin);
        // defunctCurrentApprovals(entityManager, approval, req);
        approval.setProcessed(true);
        approval.setActionDone(CmrConstants.YES_NO.Y.toString());
        break;
      case "C":
        updateApprovalStatus(entityManager, req, CmrConstants.APPROVAL_CONDITIONALLY_APPROVED, approval, admin);
        if (admin != null && admin.getId() != null && dataService != null) {
          Data data = dataService.getCurrentRecordById(admin.getId().getReqId(), entityManager);
          if (data != null && SystemLocation.CHINA.equals(data.getCmrIssuingCntry())) {
            GEOHandler geoHandler = RequestUtils.getGEOHandler(data.getCmrIssuingCntry());
            geoHandler.handleMEUCondApproval(entityManager, approval, data);
          }
        }
        if (!"REP".equals(admin.getReqStatus())) {
          moveToNextStep(entityManager, admin);
        }
        approval.setProcessed(true);
        approval.setActionDone(CmrConstants.YES_NO.Y.toString());
        break;
      case "L":
        // test status if still valid
        if (!CmrConstants.APPROVAL_PENDING_APPROVAL.equals(req.getStatus()) && !CmrConstants.APPROVAL_PENDING_REMINDER.equals(req.getStatus())) {
          approval.setProcessed(false);
        } else {
          approval.setProcessed(true);
        }
        ApprovalReq a = getApprovalRecord(entityManager, approval.getApprovalId());
        entityManager.detach(a);
        approval.setReqId(a != null ? a.getReqId() : 0);
        getLogicalStatus(req, approval, entityManager);
        break;
      }
    }
  }

  /**
   * Updates the approval status, retrieves the current logical name of the
   * status, and sends the generic email notification
   * 
   * @param entityManager
   * @param req
   * @throws CmrException
   * @throws IOException
   * @throws SQLException
   */
  private void updateApprovalStatus(EntityManager entityManager, ApprovalReq req, String status, ApprovalResponseModel approval, Admin admin)
      throws CmrException, IOException, SQLException {
    // update the status of the approval
    this.log.debug("Updating Approval Request ID " + req.getId().getApprovalId() + " to " + status);
    req.setStatus(status);
    req.setLastUpdtBy(approval.getApproverId());
    req.setLastUpdtTs(SystemUtil.getCurrentTimestamp());
    updateEntity(req, entityManager);

    // get the logical name of the updated status
    getLogicalStatus(req, approval, entityManager);

    String sourceSysSkip = admin.getSourceSystId() + ".SKIP";
    String onlySkipPartner = SystemParameters.getString(sourceSysSkip);
    boolean skip = false;

    if (StringUtils.isNotBlank(admin.getSourceSystId()) && "Y".equals(onlySkipPartner)) {
      skip = true;
    }

    // send generic mail
    ApprovalTyp type = getApprovalType(entityManager, req);
    if (skip == false) {
      sendGenericMail(entityManager, type, approval, admin);
    }
    // create the comment

    String approvalComment = StringUtils.isEmpty(approval.getComments()) ? "(no comments specified)" : approval.getComments();
    ApprovalComments cmt = new ApprovalComments();
    ApprovalCommentsPK cmtPk = new ApprovalCommentsPK();
    long cmtId = SystemUtil.getNextID(entityManager, SystemConfiguration.getValue("MANDT"), "APPROVAL_CMT_ID", "CREQCMR");
    cmtPk.setApprovalCmtId(cmtId);
    cmt.setId(cmtPk);
    cmt.setApprovalId(req.getId().getApprovalId());
    cmt.setComments(approvalComment);
    cmt.setCreateBy(approval.getApproverId());
    cmt.setCreateTs(SystemUtil.getCurrentTimestamp());
    cmt.setStatus(req.getStatus());
    if (CmrConstants.APPROVAL_REJECTED.equals(status)) {
      cmt.setRejReason(approval.getRejReason());
    }
    createEntity(cmt, entityManager);

    processAutomatedApprovals(entityManager, req, approval, null);

  }

  /**
   * Processes automated actions for ERO/DPL approvals
   * 
   * @param entityManager
   * @param request
   * @throws SQLException
   * @throws CmrException
   */
  private void processAutomatedApprovals(EntityManager entityManager, ApprovalReq request, ApprovalResponseModel approval, AppUser user)
      throws CmrException, SQLException {
    ApprovalTyp type = getApprovalType(entityManager, request);
    if (type != null && shouldAutomateApproval(type)) {
      this.log.debug("Automated Approval Approval Type: " + type.getTemplateName());
      // need to automatically set the same responses to these
      String sql = ExternalizedQuery.getSql("APPROVAL.GET_SAME_REQUESTS");
      PreparedQuery query = new PreparedQuery(entityManager, sql);
      query.setParameter("REQ_ID", request.getReqId());
      query.setParameter("APPROVAL_ID", request.getId().getApprovalId());
      query.setParameter("TYPE", request.getTypId());
      query.setParameter("GEO", request.getGeoCd());
      List<ApprovalReq> requests = query.getResults(ApprovalReq.class);

      String status = request.getStatus();

      String approverId = user != null ? user.getIntranetId() : approval.getApproverId();
      for (ApprovalReq req : requests) {
        this.log.debug("Updating Approval Request ID " + req.getId().getApprovalId() + " to " + status);
        req.setStatus(status);
        req.setLastUpdtBy(approverId);
        req.setLastUpdtTs(SystemUtil.getCurrentTimestamp());
        updateEntity(req, entityManager);

        ApprovalComments cmt = new ApprovalComments();
        ApprovalCommentsPK cmtPk = new ApprovalCommentsPK();
        long cmtId = SystemUtil.getNextID(entityManager, SystemConfiguration.getValue("MANDT"), "APPROVAL_CMT_ID", "CREQCMR");
        cmtPk.setApprovalCmtId(cmtId);
        cmt.setId(cmtPk);
        cmt.setApprovalId(req.getId().getApprovalId());
        cmt.setComments("Approval automatically processed due to action/response from " + approverId);
        cmt.setCreateBy("CreateCMR");
        cmt.setCreateTs(SystemUtil.getCurrentTimestamp());
        cmt.setStatus(req.getStatus());
        if (CmrConstants.APPROVAL_REJECTED.equals(status)) {
          cmt.setRejReason("Automatically rejected.");
        }
        createEntity(cmt, entityManager);

      }
    }
  }

  /**
   * Retrieves the logical status of the approval status
   * 
   * @param req
   * @param approval
   * @param entityManager
   * @throws CmrException
   */
  public void getLogicalStatus(ApprovalReq req, ApprovalResponseModel approval, EntityManager entityManager) throws CmrException {
    String sql = ExternalizedQuery.getSql("APPROVAL.GETSTATUS");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("CD", req.getStatus());
    String status = query.getSingleResult(String.class);
    approval.setCurrentStatus(status);
  }

  /**
   * Sends a generic email for approvals to the notify list
   * 
   * @param entityManager
   * @param approval
   * @param admin
   * @throws IOException
   */
  public synchronized void sendGenericMail(EntityManager entityManager, ApprovalTyp type, ApprovalResponseModel approval, Admin admin)
      throws IOException {
    if (engine == null) {
      initEngine();
    }

    this.log.debug("Sending Generic Mail for Approvals");
    VelocityContext ctx = new VelocityContext();
    ctx.put("admin", admin);
    ctx.put("approval", approval);
    ctx.put("type", type);
    ctx.put("link", SystemConfiguration.getValue("APPLICATION_URL") + "/login?r=" + admin.getId().getReqId());
    Template mailTemplate = null;
    if (type != null && type.getTitle() != null && type.getTitle().equals(CmrConstants.VAT_EXEMPT_TITLE)) {
      mailTemplate = engine.getTemplate(VATEXEMPT_TEMPLATE_NAME);
    } else {
      mailTemplate = engine.getTemplate(TEMPLATE_NAME);
    }

    StringWriter mail = new StringWriter();
    try {
      mailTemplate.merge(ctx, mail);
    } finally {
      mail.close();
    }

    Email email = prepareEmail(entityManager, admin, approval, mail.toString(), type);
    if (StringUtils.isEmpty(email.getTo())) {
      this.log.info("Skipping sending mail as the notification list is empty.");
      return;
    }
    String host = SystemConfiguration.getValue("MAIL_HOST");
    email.send(host);
  }

  /**
   * Prepare the generic email
   * 
   * @param entityManager
   * @param admin
   * @param approval
   * @param mailContent
   * @param type
   * @return
   */
  private Email prepareEmail(EntityManager entityManager, Admin admin, ApprovalResponseModel approval, String mailContent, ApprovalTyp type) {
    String sql = ExternalizedQuery.getSql("REQUESTENTRY.GETNOTIFLIST");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", admin.getId().getReqId());
    query.setParameter("USER_ID", "x");
    query.setParameter("REQ_STATUS", admin.getReqStatus());

    StringBuilder recipients = new StringBuilder();
    String status = null;
    List<Object[]> results = query.getResults();
    for (Object[] result : results) {
      recipients.append(recipients.length() > 0 ? "," : "");
      recipients.append((String) result[0]);
      if (status == null) {
        status = (String) result[1];
      }
    }
    String from = SystemConfiguration.getValue("MAIL_FROM");

    Email mail = new Email();
    String subject = "Request " + admin.getId().getReqId() + ": Approval Status Change - " + type.getTitle() + " - " + approval.getCurrentStatus();
    mail.setSubject(subject);
    // if (type != null && type.getTitle() != null &&
    // type.getTitle().equals(CmrConstants.VAT_EXEMPT_TITLE)) {
    // mail.setSubject("Approval Request has been set to " +
    // approval.getCurrentStatus());
    // } else
    // mail.setSubject("Approval Request sent to " + approval.getApproverNm() +
    // " has been set to " + approval.getCurrentStatus());
    mail.setTo(recipients.toString());
    mail.setFrom(from);
    mail.setMessage(mailContent);
    mail.setType(MessageType.HTML);

    return mail;
  }

  /**
   * Moves the request to the next step
   * 
   * @param entityManager
   * @param admin
   * @throws CmrException
   * @throws SQLException
   */
  public void moveToNextStep(EntityManager entityManager, Admin admin) throws CmrException, SQLException {
    this.log.debug("Checking if all approvals are complete for Request ID " + admin.getId().getReqId());
    String sql = ExternalizedQuery.getSql("APPROVAL.CHECKIFALLAPPROVED");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", admin.getId().getReqId());
    boolean approvalsReceived = !query.exists();
    sql = ExternalizedQuery.getSql("APPROVAL.CHECKIFCONDAPPROVED");
    query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", admin.getId().getReqId());
    boolean conditionallyApproved = query.exists();
    boolean cnConditionallyApproved = false;
    sql = ExternalizedQuery.getSql("APPROVAL.CHECKIFCONDCANCELLED");
    query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", admin.getId().getReqId());
    boolean conditionallyCancelled = query.exists();
    Data data = null;
    try {
      if (dataService != null) {
        data = dataService.getCurrentRecordById(admin.getId().getReqId(), entityManager);
      }
    } catch (Exception e) {
      this.log.debug("Error in Querying data table using admin's reqid in ApprovalService.java");
    }
    if (conditionallyApproved && admin != null && admin.getId() != null && dataService != null) {
      if (data != null && SystemLocation.CHINA.equals(data.getCmrIssuingCntry()) && conditionallyApproved) {
        cnConditionallyApproved = true;
      }
      // conditionallyApproved = conditionallyApproved &&
      // !cnConditionallyApproved;
    }

    if (approvalsReceived && PENDING_STATUSES_TO_MOVE.contains(admin.getReqStatus())) {
      // move only if it is one of the middle statuses
      this.log.debug("All approvals complete. Moving to next step");
      String procCenter = null;
      if (CmrConstants.REQ_TYPE_MASS_CREATE.equals(admin.getReqType())) {
        admin.setReqStatus(CmrConstants.REQUEST_STATUS.SVA.toString());
      } else {
        if (CmrConstants.REQUEST_STATUS.REP.toString().equals(admin.getReqStatus()) && "N".equalsIgnoreCase(admin.getReviewReqIndc())) {
          boolean processOnCompletion = isProcessOnCompletionChk(entityManager, admin.getId().getReqId(), admin.getReqType())
              && !conditionallyApproved && !conditionallyCancelled;
          if (processOnCompletion) {
            if (data == null || LegacyDowntimes.isUp(data.getCmrIssuingCntry(), SystemUtil.getActualTimestamp())) {
              admin.setReqStatus(CmrConstants.REQUEST_STATUS.PCP.toString());
            } else {
              admin.setReqStatus("LEG");
            }
          } else if (cnConditionallyApproved) {
            setAdminStatus4CN(entityManager, admin);
          } else if (data != null && admin != null && ("AR".equals(admin.getCustType()) || "CR".equals(admin.getCustType()))
              && SystemLocation.JAPAN.equals(data.getCmrIssuingCntry())) {
            admin.setReqStatus(CmrConstants.REQUEST_STATUS.PCP.toString());
          } else {
            admin.setReqStatus(CmrConstants.REQUEST_STATUS.PPN.toString());
          }
        } else if (cnConditionallyApproved) {
          setAdminStatus4CN(entityManager, admin);
        } else if (data != null && admin != null && ("AR".equals(admin.getCustType()) || "CR".equals(admin.getCustType()))
            && SystemLocation.JAPAN.equals(data.getCmrIssuingCntry())) {
          admin.setReqStatus(CmrConstants.REQUEST_STATUS.PCP.toString());
        } else {
          admin.setReqStatus(CmrConstants.REQUEST_STATUS.PPN.toString());
        }
        procCenter = getProcessingCenter(entityManager, admin);
        admin.setLastProcCenterNm(procCenter);
      }
      admin.setLockBy(null);
      admin.setLockTs(null);
      admin.setLockInd(CmrConstants.YES_NO.N.toString());
      admin.setLockByNm(null);

      updateEntity(admin, entityManager);
      this.log.debug("Creating workflow history and sending notifications.");
      String user = SystemConfiguration.getValue("BATCH_USERID");
      String comment = "All approval requests have been approved.";
      if ("Y".equalsIgnoreCase(admin.getReviewReqIndc())) {
        comment += "\nThe request requires a processor review before proceeding.";
      }
      if (conditionallyApproved) {
        comment = "Approval requests have been approved conditionally.\nThe request requires a processor review before proceeding.";
      }
      AppUser appuser = new AppUser();
      appuser.setIntranetId(user);
      appuser.setBluePagesName(user);
      RequestUtils.createWorkflowHistory(this, entityManager, user, admin, comment, "Approval", procCenter, procCenter, false, null, null);
      RequestUtils.createCommentLog(this, entityManager, appuser, admin.getId().getReqId(), comment);
    } else {
      this.log.debug("The request is not in Draft Status and/or Pending Approvals need to be received.");
    }
  }

  private void setAdminStatus4CN(EntityManager entityManager, Admin admin) {
    String sqlRej = ExternalizedQuery.getSql("APPROVAL.CHECKIFREJAPPROVED");
    PreparedQuery queryRej = new PreparedQuery(entityManager, sqlRej);
    queryRej.setParameter("REQ_ID", admin.getId().getReqId());
    boolean approvalsRej = queryRej.exists();
    if (approvalsRej) {
      admin.setReqStatus("AUT");
    } else if (isConditionApprovalCN(entityManager, admin.getId().getReqId())) {
      if (hasCNAttachment(entityManager, admin.getId().getReqId())) {
        admin.setReqStatus(CmrConstants.REQUEST_STATUS.PPN.toString());
      } else {
        admin.setReqStatus(CmrConstants.REQUEST_STATUS.PCP.toString());
      }
    } else {
      admin.setReqStatus(CmrConstants.REQUEST_STATUS.PCP.toString());
    }
  }

  private boolean isConditionApprovalCN(EntityManager entityManager, long reqId) {
    String sql = ExternalizedQuery.getSql("APPROVAL.CHECKIFCONDAPPROVED");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", reqId);
    return query.exists();
  }

  private boolean hasCNAttachment(EntityManager entityManager, long reqId) {
    String ret = ChinaUtil.geDocContent(entityManager, reqId);
    if ("Y".equals(ret)) {
      return true;
    }
    return false;
  }

  /**
   * Checks if this country will go to processing automatically on successful
   * execution or not
   * 
   * @param entityManager
   * @param reqId
   * @param reqType
   * @return
   */
  private boolean isProcessOnCompletionChk(EntityManager entityManager, long reqId, String reqType) {
    String sql = ExternalizedQuery.getSql("AUTOMATION.GET_ON_COMPLETE_ACTION_REQ_ID");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", reqId);
    query.setForReadOnly(true);
    String result = query.getSingleResult(String.class);
    boolean onCompleteAction = false;
    if ("Y".equals(result) || ("C".equals(result) && "C".equals(reqType)) || ("U".equals(result) && "U".equals(reqType))) {
      onCompleteAction = true;
    }
    return onCompleteAction;

  }

  /**
   * Moves the request to the next step
   * 
   * @param entityManager
   * @param admin
   * @throws CmrException
   * @throws SQLException
   */
  public void moveBackToRequester(EntityManager entityManager, Admin admin) throws CmrException, SQLException {

    if (PENDING_STATUSES_TO_RETURN.contains(admin.getReqStatus())) {
      // move only if it is one of the auto statuses
      this.log.debug("Moving Request ID " + admin.getId().getReqId() + " back to requester because of rejection.");
      admin.setReqStatus(CmrConstants.REQUEST_STATUS.PRJ.toString());
      admin.setLastProcCenterNm(null);
      admin.setLockInd("N");
      admin.setLockByNm(null);
      admin.setLockBy(null);
      admin.setLockTs(null);

      updateEntity(admin, entityManager);
      String user = SystemConfiguration.getValue("BATCH_USERID");
      String comment = "Request has been rejected due to rejected approvals. Please check the approval comments.";
      AppUser appuser = new AppUser();
      appuser.setIntranetId(user);
      appuser.setBluePagesName(user);
      List<ApprovalComments> rejApprovalComments = getRejectionCmtAsRejRsn(entityManager, admin);
      String rejectReasonCmt = null;
      String rejectReasonCd = null;
      // filter automated comments out
      if (!rejApprovalComments.isEmpty()) {
        for (ApprovalComments apprCmt : rejApprovalComments) {
          this.log.debug("Iterating through approval comments...");
          String createBy = StringUtils.isNotBlank(apprCmt.getCreateBy()) ? apprCmt.getCreateBy() : "";
          Person p = null;
          try {
            p = BluePagesHelper.getPerson(createBy);
          } catch (Exception e) {
            this.log.debug("Error in Querying BluepagesHelper service in ApprovalService.java");
          }
          String rsn = StringUtils.isNotBlank(apprCmt.getRejReason()) ? apprCmt.getRejReason() : "";
          if (p != null && !"Automatically rejected.".equalsIgnoreCase(rsn) && rsn != "") {
            this.log.debug("Approval Comments are -> " + apprCmt.getComments() + " \n Reject reason is -> " + apprCmt.getRejReason());
            rejectReasonCmt = apprCmt.getComments();
            rejectReasonCd = apprCmt.getRejReason();
            break;
          }
        }
      }

      if (StringUtils.isNotBlank(rejectReasonCmt)) {
        this.log.debug("Appending the rejection reason comment with general comments.");
        comment = comment + "\n" + rejectReasonCmt;
      }
      this.log.debug("Creating workflow history and sending notifications.");
      RequestUtils.createWorkflowHistory(this, entityManager, user, admin, comment, "Approval", null, null, false, null, rejectReasonCd);
      RequestUtils.createCommentLog(this, entityManager, appuser, admin.getId().getReqId(), comment);

    } else {
      this.log.debug("The request " + admin.getId().getReqId() + " is not in any automation statuses.");
    }
  }

  private List<ApprovalComments> getRejectionCmtAsRejRsn(EntityManager entityManager, Admin admin) {
    this.log.debug("Started executing function getRejectionCmtAsRejRsn() ... ");
    List<ApprovalReq> records = getApprovalRecords(entityManager, admin.getId().getReqId());
    ApprovalComments comment = null;
    List<ApprovalComments> comments = new ArrayList<ApprovalComments>();

    if (!records.isEmpty()) {
      for (ApprovalReq request : records) {
        this.log.debug("Processing Approval Request ID " + request.getId().getApprovalId());

        // get approval comments for rejection
        try {
          String sql = ExternalizedQuery.getSql("BATCH.APPR.GET_REJ_CMT");
          PreparedQuery query = new PreparedQuery(entityManager, sql);
          query.setParameter("APPROVAL_ID", request.getId().getApprovalId());
          query.setForReadOnly(true);
          comment = query.getSingleResult(ApprovalComments.class);
        } catch (Exception e) {
          this.log.debug("Error while querying for comments in table APPROVAL_COMMENTS for req_id -> " + admin.getId().getReqId());
        }

        if (comment != null) {
          this.log.debug("Adding comment with approval Id -> " + request.getId().getApprovalId());
          comments.add(comment);
        }
      }
    } else {
      this.log.debug("Records from CREQCMR.APPROVAL_REQ is empty for req id ->  " + admin.getId().getReqId());
    }

    return comments;

  }

  private List<ApprovalReq> getApprovalRecords(EntityManager entityManager, Long reqId) {
    List<ApprovalReq> records = null;
    try {
      String sql = ExternalizedQuery.getSql("BATCH.APPR.GET_REJ_RECORDS");
      PreparedQuery query = new PreparedQuery(entityManager, sql);
      query.setParameter("REQ_ID", reqId);
      records = query.getResults(ApprovalReq.class);
    } catch (Exception e) {
      this.log.debug("Error while querying for approval records in APPROVAL_REQ table for req id -> " + reqId);
    }
    return records;

  }

  /**
   * Set the approval requests under the request to Defunct
   * 
   * @param entityManager
   * @param approval
   * @param req
   */
  protected void defunctCurrentApprovals(EntityManager entityManager, ApprovalResponseModel approval, ApprovalReq req) {
    this.log.debug("Setting other approvals to Defunct..");
    String updateSql = ExternalizedQuery.getSql("APPROVAL.DEFUNCTOTHERS");
    PreparedQuery query = new PreparedQuery(entityManager, updateSql);
    query.setParameter("REQ_ID", req.getReqId());
    query.setParameter("APPROVAL_ID", req.getId().getApprovalId());
    int updated = query.executeSql();
    this.log.debug(updated + " records set to defunct.");

  }

  /**
   * Gets the processing center for the request
   * 
   * @param entityManager
   * @param admin
   * @return
   */
  private String getProcessingCenter(EntityManager entityManager, Admin admin) {
    String sql = ExternalizedQuery.getSql("APPROVAL.GETPROCCENTER");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", admin.getId().getReqId());
    return query.getSingleResult(String.class);
  }

  /**
   * Retrieves the {@link ApprovalReq} record
   * 
   * @param entityManager
   * @param approvalId
   * @return
   */
  public ApprovalReq getApprovalRecord(EntityManager entityManager, long approvalId) {
    String sql = ExternalizedQuery.getSql("APPROVAL.GETRECORD");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("APPROVAL_ID", approvalId);
    return query.getSingleResult(ApprovalReq.class);
  }

  /**
   * Adds the missing information needed on the page
   * 
   * @param req
   * @param approval
   * @param entityManager
   * @return
   * @throws CmrException
   */
  public Admin fillApprovalInformation(ApprovalReq req, ApprovalResponseModel approval, EntityManager entityManager) throws CmrException {

    approval.setApproverNm(req.getDisplayName());

    String sql = ExternalizedQuery.getSql("APPROVAL.GETREQUESTER");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", req.getReqId());
    Admin admin = query.getSingleResult(Admin.class);
    Person p = BluePagesHelper.getPerson(req.getCreateBy());
    approval.setRequester(p != null ? p.getName() : admin.getLastUpdtBy());

    return admin;
  }

  /**
   * Gets the approval type record for the request
   * 
   * @param entityManager
   * @param request
   * @return
   */
  private ApprovalTyp getApprovalType(EntityManager entityManager, ApprovalReq request) {
    String sql = ExternalizedQuery.getSql("APPROVAL.GETTYPE");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("TYP_ID", request.getTypId());
    query.setParameter("GEO_CD", request.getGeoCd());
    query.setForReadOnly(true);
    return query.getSingleResult(ApprovalTyp.class);
  }

  /**
   * Initializes the {@link VelocityEngine} object
   */
  private void initEngine() {
    this.log.info("Initializing velocity engine for Approvals...");
    engine = new VelocityEngine();
    // engine.addProperty(Velocity.RESOURCE_LOADER, "classpath");
    engine.addProperty(Velocity.FILE_RESOURCE_LOADER_PATH, ConfigUtil.getConfigDir());
    // engine.setProperty("classpath.resource.loader.class",
    // ClasspathResourceLoader.class.getName());
    engine.init();
  }

  @Override
  protected Logger initLogger() {
    return Logger.getLogger(ApprovalService.class);
  }

  @Override
  protected List<ApprovalResponseModel> doSearch(ApprovalResponseModel model, EntityManager entityManager, HttpServletRequest request)
      throws Exception {

    boolean adminOverride = "Y".equals(request.getParameter("adminOR"));
    SimpleDateFormat formatter = new SimpleDateFormat(SystemConfiguration.getValue("DATE_TIME_FORMAT"));
    List<ApprovalResponseModel> results = new ArrayList<ApprovalResponseModel>();
    String sql = ExternalizedQuery.getSql("APPROVALS.GET.BYREQID");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", model.getReqId());

    ApprovalResponseModel appReqModel = null;
    ApprovalReq appReq = null;
    String statusStr = null;
    String type = null;

    List<CompoundEntity> rs1 = query.getCompundResults(ApprovalReq.class, ApprovalReq.APPROVAL_LIST_MAPPING);
    for (CompoundEntity ent : rs1) {
      appReq = ent.getEntity(ApprovalReq.class);
      statusStr = (String) ent.getValue("STATUS_STR");
      type = (String) ent.getValue("TYPE");
      if (!adminOverride || (adminOverride && (CmrConstants.APPROVAL_PENDING_REMINDER.equals(appReq.getStatus())
          || CmrConstants.APPROVAL_PENDING_APPROVAL.equals(appReq.getStatus())))) {
        appReqModel = new ApprovalResponseModel();
        copyValuesFromEntity(appReq, appReqModel);
        appReqModel.setStatusStr(statusStr);
        if (type != null && type.contains("|")) {
          String[] parts = type.split("[|]");
          type = parts[0];
        }
        appReqModel.setType(type);
        appReqModel.setApproverNm(appReq.getDisplayName());
        appReqModel.setApproverId(appReq.getIntranetId());
        appReqModel.setCreateTsString(formatter.format(appReq.getCreateTs()));
        results.add(appReqModel);
      }
    }

    return results;
  }

  @Override
  protected ApprovalReq getCurrentRecord(ApprovalResponseModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    return null;
  }

  private ApprovalTyp getApprovalTypeById(EntityManager entityManager, ApprovalReq approver) throws Exception {
    String sql = ExternalizedQuery.getSql("APPROVAL.GETTYPE.BYTYPID");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("TYP_ID", approver.getTypId());
    query.setForReadOnly(true);
    return query.getSingleResult(ApprovalTyp.class);
  }

  @Override
  protected ApprovalReq createFromModel(ApprovalResponseModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    ApprovalReq approver = new ApprovalReq();
    ApprovalReqPK pk = new ApprovalReqPK();
    approver.setId(pk);
    copyValuesToEntity(model, approver);
    return approver;
  }

  private void createApprovalComment(EntityManager entityManager, String status, AppUser user, ApprovalResponseModel model, String commentPreFix)
      throws Exception {
    ApprovalComments comment = new ApprovalComments();
    ApprovalCommentsPK pk = new ApprovalCommentsPK();
    pk.setApprovalCmtId(SystemUtil.getNextID(entityManager, SystemConfiguration.getValue("MANDT"), "APPROVAL_CMT_ID", "CREQCMR"));
    comment.setId(pk);

    comment.setApprovalId(model.getApprovalId());
    comment.setStatus(status);
    comment.setComments((commentPreFix != null ? commentPreFix + " " : "") + model.getComments());
    comment.setCreateBy(user.getIntranetId());
    comment.setCreateTs(this.currentTimestamp);
    createEntity(comment, entityManager);
  }

  /**
   * Retrieves the comments for the approval
   * 
   * @param entityManager
   * @param approval
   * @throws CmrException
   */
  private void retrieveComments(EntityManager entityManager, ApprovalResponseModel approval) throws CmrException {
    this.log.debug("Retrieving comments...");
    String sql = ExternalizedQuery.getSql("APPROVAL.GETCOMMENTS");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", approval.getReqId());
    query.setParameter("APPROVAL_ID", approval.getApprovalId());
    query.setForReadOnly(true);
    List<ApprovalComments> comments = query.getResults(ApprovalComments.class);
    if (comments != null && comments.size() > 0) {

      List<ApprovalCommentModel> commentList = new ArrayList<ApprovalCommentModel>();
      ApprovalCommentModel cmtModel = null;
      String createBy = null;
      Person person = null;
      SimpleDateFormat FORMATTER = CmrConstants.DATE_TIME_FORMAT();
      for (ApprovalComments comment : comments) {
        cmtModel = new ApprovalCommentModel();
        cmtModel.setApprovalId(approval.getApprovalId());
        createBy = comment.getCreateBy();
        person = BluePagesHelper.getPerson(createBy);
        if (person != null) {
          cmtModel.setCommentBy(person.getNotesEmail());
        } else {
          cmtModel.setCommentBy(createBy);
        }
        cmtModel.setComments(comment.getComments());
        cmtModel.setStatus(comment.getStatus());
        cmtModel.setReqId(approval.getReqId());
        cmtModel.setCreateTs(FORMATTER.format(comment.getCreateTs()));

        commentList.add(cmtModel);
      }
      approval.setCommentList(commentList);
    } else {
      approval.setCommentList(new ArrayList<ApprovalCommentModel>());
    }
  }

  /**
   * Processes Send Reminder
   * 
   * @param entityManager
   * @param user
   * @param model
   * @throws Exception
   */
  private void processSendReminder(EntityManager entityManager, AppUser user, ApprovalResponseModel model) throws Exception {
    this.log.debug("Processing Send Reminder for Approval ID " + model.getApprovalId());
    ApprovalReq approval = getApprovalRecord(entityManager, model.getApprovalId());
    if (!model.getStatus().equals(approval.getStatus())) {
      throw new CmrException(MessageUtil.ERROR_APPROVAL_STATUS_CHANGED);
    }
    approval.setStatus(CmrConstants.APPROVAL_PENDING_REMINDER);
    approval.setLastUpdtBy(user.getIntranetId());
    approval.setLastUpdtTs(SystemUtil.getCurrentTimestamp());
    updateEntity(approval, entityManager);
    createApprovalComment(entityManager, "Reminder was sent by the system.", approval, user);

    processAutomatedApprovals(entityManager, approval, model, user);
  }

  /**
   * Processes Cancel
   * 
   * @param entityManager
   * @param user
   * @param model
   * @throws Exception
   */
  private void processCancel(EntityManager entityManager, AppUser user, ApprovalResponseModel model) throws Exception {
    this.log.debug("Processing Cancel for Approval ID " + model.getApprovalId());
    ApprovalReq approval = getApprovalRecord(entityManager, model.getApprovalId());
    if (!model.getStatus().equals(approval.getStatus())) {
      throw new CmrException(MessageUtil.ERROR_APPROVAL_STATUS_CHANGED);
    }
    String statusToSet = CmrConstants.APPROVAL_PENDING_CANCELLATION;
    if (CmrConstants.APPROVAL_DRAFT.equals(approval.getStatus())) {
      statusToSet = CmrConstants.APPROVAL_CANCELLED;
    }
    approval.setStatus(statusToSet);
    approval.setLastUpdtBy(user.getIntranetId());
    approval.setLastUpdtTs(SystemUtil.getCurrentTimestamp());
    updateEntity(approval, entityManager);

    createApprovalComment(entityManager, statusToSet, user, model, null);
  }

  /**
   * Processes Override
   * 
   * @param entityManager
   * @param user
   * @param model
   * @throws Exception
   */
  private void processOverride(EntityManager entityManager, AppUser user, ApprovalResponseModel model) throws Exception {
    this.log.debug("Processing Override for Approval ID " + model.getApprovalId());

    Timestamp currentTs = SystemUtil.getCurrentTimestamp();
    // set old one to Override Pending
    ApprovalReq approval = getApprovalRecord(entityManager, model.getApprovalId());

    String currentApprover = approval.getIntranetId();
    if (!model.getStatus().equals(approval.getStatus())) {
      throw new CmrException(MessageUtil.ERROR_APPROVAL_STATUS_CHANGED);
    }
    if (model.getIntranetId().equals(approval.getIntranetId())) {
      throw new CmrException(MessageUtil.ERROR_APPROVAL_OVERRIDE_SAME_APPROVER);
    }
    approval.setStatus(CmrConstants.APPROVAL_OVERRIDE_PENDING);
    approval.setLastUpdtBy(user.getIntranetId());
    approval.setLastUpdtTs(currentTs);
    updateEntity(approval, entityManager);
    String cmtPrefix = "Override: Changed approver to " + model.getIntranetId() + " -";

    createApprovalComment(entityManager, CmrConstants.APPROVAL_OVERRIDE_PENDING, user, model, cmtPrefix);

    this.log.debug("Creating new approval for Request ID " + model.getReqId() + " Approver " + model.getIntranetId());

    // create new one with Pending Mail
    long overrideId = SystemUtil.getNextID(entityManager, SystemConfiguration.getValue("MANDT"), "APPROVAL_ID", "CREQCMR");
    ApprovalReq override = new ApprovalReq();
    ApprovalReqPK overridePk = new ApprovalReqPK();
    overridePk.setApprovalId(overrideId);
    override.setId(overridePk);
    model.setApprovalId(overrideId);

    Person person = BluePagesHelper.getPerson(model.getIntranetId());
    if (person == null) {
      person = new Person();
      person.setNotesEmail(model.getIntranetId());
      person.setEmail(model.getIntranetId());
      person.setName(model.getIntranetId());
    }
    override.setStatus(CmrConstants.APPROVAL_PENDING_MAIL);
    override.setIntranetId(person.getEmail());
    override.setDisplayName(person.getName());
    override.setNotesId(person.getNotesEmail());

    override.setGeoCd(approval.getGeoCd());
    override.setReqId(approval.getReqId());
    override.setTypId(approval.getTypId());

    override.setCreateBy(user.getIntranetId());
    override.setCreateTs(currentTs);
    override.setLastUpdtBy(user.getIntranetId());
    override.setLastUpdtTs(currentTs);
    override.setRequiredIndc(approval.getRequiredIndc());

    createEntity(override, entityManager);

    cmtPrefix = "Override: New approver, changed from " + currentApprover + " -";
    createApprovalComment(entityManager, CmrConstants.APPROVAL_PENDING_MAIL, user, model, cmtPrefix);

    // this.log.debug("Bringing back defunct approvals..");
    // reprocessDefunctRecords(entityManager, model, user,
    // CmrConstants.APPROVAL_OVERRIDE_PENDING, "Approval Override:");
  }

  /**
   * Processes Resubmit
   * 
   * @param entityManager
   * @param user
   * @param model
   * @throws Exception
   */
  private void processResubmit(EntityManager entityManager, AppUser user, ApprovalResponseModel model) throws Exception {
    this.log.debug("Processing Resubmit for Approval ID " + model.getApprovalId());

    Timestamp currentTs = SystemUtil.getCurrentTimestamp();
    // set old one to Override Pending
    ApprovalReq approval = getApprovalRecord(entityManager, model.getApprovalId());
    if (!model.getStatus().equals(approval.getStatus())) {
      throw new CmrException(MessageUtil.ERROR_APPROVAL_STATUS_CHANGED);
    }
    approval.setStatus(CmrConstants.APPROVAL_PENDING_MAIL);
    approval.setLastUpdtBy(user.getIntranetId());
    approval.setLastUpdtTs(currentTs);
    updateEntity(approval, entityManager);
    createApprovalComment(entityManager, CmrConstants.APPROVAL_PENDING_MAIL, user, model, null);

    processAutomatedApprovals(entityManager, approval, model, user);

    // this.log.debug("Bringing back defunct approvals..");
    // reprocessDefunctRecords(entityManager, model, user,
    // CmrConstants.APPROVAL_PENDING_MAIL, "Resubmit:");
  }

  /**
   * Brings back defunct records to be submitted to the approver again
   * 
   * @param entityManager
   * @param model
   * @param user
   * @param status
   * @param commentPrefix
   * @throws Exception
   */
  protected void reprocessDefunctRecords(EntityManager entityManager, ApprovalResponseModel model, AppUser user, String status, String commentPrefix)
      throws Exception {
    String sql = ExternalizedQuery.getSql("APPROVAL.GETDEFUNCT");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", model.getReqId());
    List<ApprovalReq> defunctList = query.getResults(ApprovalReq.class);
    if (defunctList != null) {
      for (ApprovalReq approval : defunctList) {
        approval.setStatus(CmrConstants.APPROVAL_PENDING_MAIL);
        approval.setLastUpdtBy(user.getIntranetId());
        approval.setLastUpdtTs(SystemUtil.getCurrentTimestamp());
        updateEntity(approval, entityManager);

        model.setApprovalId(approval.getId().getApprovalId());
        createApprovalComment(entityManager, status, user, model, commentPrefix);

      }
    }
  }

  /**
   * Processes Send Reminder
   * 
   * @param entityManager
   * @param user
   * @param model
   * @throws CmrException
   */
  private void processSendRequest(EntityManager entityManager, AppUser user, ApprovalResponseModel model) throws CmrException {
    this.log.debug("Processing Send Approval Requests for Request ID " + model.getReqId());
    String sql = ExternalizedQuery.getSql("APPROVAL.GETDRAFT");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", model.getReqId());
    List<ApprovalReq> requests = query.getResults(ApprovalReq.class);
    if (requests != null) {
      for (ApprovalReq approval : requests) {
        approval.setStatus(CmrConstants.APPROVAL_PENDING_MAIL);
        approval.setLastUpdtBy(user.getIntranetId());
        approval.setLastUpdtTs(SystemUtil.getCurrentTimestamp());
        updateEntity(approval, entityManager);
      }
    }
  }

  public void makeApprovalsObsolete(EntityManager entityManager, long reqId, AppUser user) throws Exception {
    String sql = ExternalizedQuery.getSql("APPROVAL.GETALLUNDERREQ");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", reqId);
    List<ApprovalReq> requests = query.getResults(ApprovalReq.class);
    if (requests != null) {
      for (ApprovalReq approval : requests) {
        approval.setStatus(CmrConstants.APPROVAL_OBSOLETE);
        approval.setLastUpdtBy(user.getIntranetId());
        approval.setLastUpdtTs(SystemUtil.getCurrentTimestamp());
        updateEntity(approval, entityManager);
        createApprovalComment(entityManager, "Approval made obsolete due to request rejection.", approval, user);
      }
    }
  }

  /**
   * Sets the ApprovalResult fields for Main Request
   * 
   * @param model
   */
  public String setApprovalResult(RequestEntryModel model) {
    SimpleDateFormat dateFormat = CmrConstants.DATE_FORMAT();
    EntityManager entityManager = JpaManager.getEntityManager();

    try {
      String sql = ExternalizedQuery.getSql("APPROVAL.GETRESULT");
      PreparedQuery query = new PreparedQuery(entityManager, sql);
      query.setParameter("REQ_ID", model.getReqId());
      query.setForReadOnly(true);
      List<Object[]> results = query.getResults();

      if (results != null && !results.isEmpty()) {
        List<String> noneStats = Arrays.asList("DRA", "OBSLT");
        List<String> pendingStats = Arrays.asList("PMAIL", "PAPR", "OVERP", "PREM");
        List<String> approvedStats = Arrays.asList("APR", "OVERA", "OBSLT", "CAN", "PCAN", "DRA");
        List<String> condApprovedStats = Arrays.asList("CAPR", "APR", "OVERA", "OBSLT", "CAN", "PCAN", "DRA", "CCAN");
        // List<String> rejectedStats = Arrays.asList("REJ", "DFNCT", "OBSLT",
        // "CAN", "PCAN", "DRA");
        List<String> cancelledStats = Arrays.asList("CAN", "PCAN", "OBSLT", "DRA");

        List<String> statuses = new ArrayList<String>();
        for (Object[] result : results) {
          statuses.add((String) result[0]);
        }
        Date lastUpdt = (Date) results.get(0)[1];
        String lastUpdtStr = "";
        if (lastUpdt != null && lastUpdt instanceof Date) {
          lastUpdtStr = dateFormat.format(lastUpdt);
        }

        // Compute approval status for scorecard
        if (containsOnly(statuses, noneStats)) {
          model.setApprovalResult(CmrConstants.APPROVAL_RESULT_NONE);
        } else if (CollectionUtils.containsAny(statuses, pendingStats)) {
          model.setApprovalResult(CmrConstants.APPROVAL_RESULT_PENDING);
        } else if (containsOnly(statuses, approvedStats) && statuses.contains(CmrConstants.APPROVAL_APPROVED)) {
          model.setApprovalResult(CmrConstants.APPROVAL_RESULT_APPROVED);
          model.setApprovalDateStr(lastUpdtStr);
        } else if (containsOnly(statuses, condApprovedStats) && statuses.contains(CmrConstants.APPROVAL_CONDITIONALLY_APPROVED)) {
          model.setApprovalResult(CmrConstants.APPROVAL_RESULT_COND_APPROVED);
        } else if (statuses.contains(CmrConstants.APPROVAL_CONDITIONALLY_CANCELLED)) {
          model.setApprovalResult(CmrConstants.APPROVAL_RESULT_COND_CANCELLED);
        } else if (statuses.contains(CmrConstants.APPROVAL_REJECTED)) {
          model.setApprovalResult(CmrConstants.APPROVAL_RESULT_REJECTED);
          model.setApprovalDateStr(lastUpdtStr);
        } else if (containsOnly(statuses, cancelledStats) && statuses.contains(CmrConstants.APPROVAL_CANCELLED)) {
          model.setApprovalResult(CmrConstants.APPROVAL_RESULT_CANCELLED);
          model.setApprovalDateStr(lastUpdtStr);
        } else {
          model.setApprovalResult(CmrConstants.APPROVAL_RESULT_NONE);
        }
      } else {
        model.setApprovalResult(CmrConstants.APPROVAL_RESULT_NONE);
      }
      model.setApprovalMaxTs(getMaxTimestamp(entityManager, model.getReqId()));
    } finally {
      // empty the manager
      entityManager.clear();
      entityManager.close();
    }
    return model.getApprovalResult();
  }

  private String getMaxTimestamp(EntityManager entityManager, long reqId) {
    String sql = ExternalizedQuery.getSql("APPROVAL.GETLATESTTS");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setForReadOnly(true);
    query.setParameter("REQ_ID", reqId);
    return query.getSingleResult(String.class);
  }

  private boolean containsOnly(List<String> statuses, List<String> checkStats) {
    for (String status : statuses) {
      if (!checkStats.contains(status)) {
        return false;
      }
    }
    return true;
  }

  public void updateApprovals(EntityManager entityManager, String action, long reqId, AppUser user) {

    // Current approvals that are not CAN, PCAN, OBSLT, DRA will be set to OBSLT
    if ("EDIT_REQUEST".equals(action)) {
      String sql = ExternalizedQuery.getSql("APPROVAL.GETRECORDS_FOR_EDITREQ");
      PreparedQuery query = new PreparedQuery(entityManager, sql);
      query.setParameter("REQ_ID", reqId);
      List<ApprovalReq> approvals = query.getResults(ApprovalReq.class);
      if (approvals != null) {
        for (ApprovalReq approval : approvals) {
          approval.setStatus(CmrConstants.APPROVAL_OBSOLETE);
          approval.setLastUpdtBy(user.getIntranetId());
          approval.setLastUpdtTs(SystemUtil.getCurrentTimestamp());
          updateEntity(approval, entityManager);
        }
      }
    }

  }

  /**
   * Processes Resubmit
   * 
   * @param entityManager
   * @param user
   * @param model
   * @throws Exception
   */
  private void processAdminOverride(EntityManager entityManager, AppUser user, ApprovalResponseModel model) throws Exception {
    this.log.debug("Processing Admin Override for Approval ID " + model.getApprovalId());

    Timestamp currentTs = SystemUtil.getCurrentTimestamp();
    // set old one to Override Pending
    ApprovalReq approval = getApprovalRecord(entityManager, model.getApprovalId());
    String currentApprover = approval.getIntranetId();
    String currentStatus = approval.getStatus();
    approval.setStatus(model.getStatus());
    if (!approval.getIntranetId().equals(model.getIntranetId())) {
      Person person = BluePagesHelper.getPerson(model.getIntranetId());
      if (person == null) {
        person = new Person();
        person.setNotesEmail(model.getIntranetId());
        person.setEmail(model.getIntranetId());
        person.setName(model.getIntranetId());
      }
      approval.setIntranetId(person.getEmail());
      approval.setDisplayName(person.getName());
      approval.setNotesId(person.getNotesEmail());
    }
    approval.setLastUpdtBy(user.getIntranetId());
    approval.setLastUpdtTs(currentTs);
    updateEntity(approval, entityManager);

    String prefix = "Administrator Override: ";
    if (!model.getIntranetId().toLowerCase().equals(currentApprover.toLowerCase())) {
      prefix += " Changed approver from " + currentApprover + " to " + model.getIntranetId() + " ";
    }
    if (!model.getStatus().equals(currentStatus)) {
      prefix += " Changed status code from " + currentStatus + " to " + model.getStatus() + " ";
    }

    createApprovalComment(entityManager, model.getStatus(), user, model, prefix + " - ");

  }

  /**
   * Processes Default Approval
   * 
   * @param entityManager
   * @param reqId
   * @param reqType
   * @param user
   * @param model
   * @throws Exception
   */
  public String processDefaultApproval(EntityManager entityManager, long reqId, String reqType, AppUser user, RequestEntryModel model)
      throws Exception {

    boolean approverCreated = false;
    boolean approvalRecordSet = false;
    boolean approvalPending = false;
    List<Long> defaultApprovalIds = new ArrayList<Long>();
    defaultApprovalIds = ApprovalUtil.getDefaultApprovalsIds(entityManager, model.getCmrIssuingCntry(), reqId, reqType);

    GEOHandler geoHandler = RequestUtils.getGEOHandler(model.getCmrIssuingCntry());

    for (Long defaultApprovalId : defaultApprovalIds) {

      DefaultApprovals defaultApprovals = null;
      DefaultApprovalRecipients recipients = null;
      String defltApprovalSql = "APPROVAL_DFLT_REQ_RECORDS";
      String map = DefaultApprovals.DEFLT_APPROVAL_REQ_MAPPING;
      PreparedQuery approvalrecqry = new PreparedQuery(entityManager, ExternalizedQuery.getSql(defltApprovalSql));
      approvalrecqry.setParameter("DEFAULT_APPROVAL_ID", defaultApprovalId);

      List<CompoundEntity> results = approvalrecqry.getCompundResults(DefaultApprovals.class, map);

      for (CompoundEntity entity : results) {
        defaultApprovals = entity.getEntity(DefaultApprovals.class);
        recipients = entity.getEntity(DefaultApprovalRecipients.class);
        String IntranetId = recipients.getId().getIntranetId();
        if (IntranetId.equalsIgnoreCase(BP_MANAGER_ID)) {
          IntranetId = geoHandler.getBPMANAGER(entityManager, reqId, recipients, user, model);
        }
        PreparedQuery query = new PreparedQuery(entityManager, ExternalizedQuery.getSql("GET_APPROVAL_REQ"));
        query.setParameter("REQ_ID", reqId);
        query.setParameter("GEO_CD", defaultApprovals.getGeoCd());
        query.setParameter("TYP_ID", defaultApprovals.getTypId());
        query.setParameter("INTRANET_ID", IntranetId);
        List<ApprovalReq> approvalRecords = query.getResults(ApprovalReq.class);

        if (approvalRecords.size() > 0) {
          for (ApprovalReq approvalRecEntity : approvalRecords) {
            if (!approvalRecEntity.getStatus().equalsIgnoreCase(CmrConstants.APPROVAL_OBSOLETE)
                && !approvalRecEntity.getStatus().equalsIgnoreCase(CmrConstants.APPROVAL_CANCELLED)
                && !approvalRecEntity.getStatus().equalsIgnoreCase(CmrConstants.APPROVAL_PENDING_CANCELLATION)) {
              approvalRecEntity.setRequiredIndc(CmrConstants.APPROVAL_DEFLT_REQUIRED_INDC);
              approvalRecEntity.setDefaultApprovalId(defaultApprovalId);
              if (approvalRecEntity.getStatus().equalsIgnoreCase(CmrConstants.APPROVAL_REJECTED)
                  || approvalRecEntity.getStatus().equalsIgnoreCase(CmrConstants.APPROVAL_DEFUNCT)) {
                approvalRecEntity.setStatus(CmrConstants.APPROVAL_PENDING_MAIL);
                createApprovalComment(entityManager, "System triggered resubmission of approval.", approvalRecEntity, user);
              }
              updateEntity(approvalRecEntity, entityManager);
            }

            if (approvalRecEntity.getStatus().equalsIgnoreCase(CmrConstants.APPROVAL_PENDING_APPROVAL)
                || approvalRecEntity.getStatus().equalsIgnoreCase(CmrConstants.APPROVAL_PENDING_REMINDER)) {
              approvalPending = true;
            }
          }
          // no need to set to true if just checking for existing records
          // statusCheck = true;
        } else {

          if ((SystemLocation.JAPAN.equals(model.getCmrIssuingCntry()) || SystemLocation.KOREA.equals(model.getCmrIssuingCntry()))
              && recipients.getId().getIntranetId().equalsIgnoreCase(BP_MANAGER_ID)) {
            ApprovalReq approver = new ApprovalReq();
            approver = geoHandler.handleBPMANAGERApproval(entityManager, reqId, approver, defaultApprovals, recipients, user, model);
            if (approver == null)
              continue;
            approverCreated = true;
            createApprovalComment(entityManager, "System-generated required approval.", approver, user);
          } else {
            Person target = null;
            boolean bpManager = false;
            if (BP_MANAGER_ID.equals(recipients.getId().getIntranetId())) {
              this.log.debug("Handling Adding BP manager..");
              target = BluePagesHelper.getPerson(BluePagesHelper.getManagerEmail(user.getUserCnum()));
              bpManager = true;
            } else {
              this.log.debug("Handling Adding user..");
              target = BluePagesHelper.getPerson(recipients.getId().getIntranetId());
            }
            if (target == null && recipients.getId().getIntranetId().toLowerCase().endsWith("ibm.com")) {
              // only check if the ID is from IBM as a functional id
              String id = recipients.getId().getIntranetId();
              target = new Person();
              target.setEmail(id);
              target.setName(id);
              target.setId(id);
              target.setNotesEmail(id);
            }
            if (target != null) {
              ApprovalReq approver = new ApprovalReq();
              long approverId = SystemUtil.getNextID(entityManager, SystemConfiguration.getValue("MANDT"), "APPROVAL_ID", "CREQCMR");
              ApprovalReqPK approverPk = new ApprovalReqPK();
              approverPk.setApprovalId(approverId);
              approver.setId(approverPk);
              approver.setReqId(reqId);
              approver.setTypId(defaultApprovals.getTypId());
              approver.setGeoCd(defaultApprovals.getGeoCd());
              approver.setIntranetId(target.getEmail());
              approver.setNotesId(target.getNotesEmail());
              approver.setDisplayName(bpManager ? target.getName() : recipients.getDisplayName());
              approver.setStatus(CmrConstants.APPROVAL_PENDING_MAIL);
              approver.setCreateBy(user.getIntranetId());
              approver.setLastUpdtBy(user.getIntranetId());
              approver.setCreateTs(SystemUtil.getCurrentTimestamp());
              approver.setLastUpdtTs(SystemUtil.getCurrentTimestamp());
              approver.setRequiredIndc(CmrConstants.APPROVAL_DEFLT_REQUIRED_INDC);
              approver.setDefaultApprovalId(defaultApprovalId);
              createEntity(approver, entityManager);
              approverCreated = true;

              createApprovalComment(entityManager, "System-generated required approval.", approver, user);
            }

          }
        }
      }

      approvalRecordSet = setApproverReqByReqId(entityManager, reqId, user);

      if (approverCreated == false && approvalPending == true) {
        model.setMessageDefaultApproval(CmrConstants.DEFAULT_APPROVAL_FLAG);
      }

      if (approverCreated == true || approvalRecordSet == true) {
        model.setMessageDefaultApproval(CmrConstants.DEFAULT_APPROVAL_FLAG);
      }
    }

    approvalRecordSet = setApproverReqByReqId(entityManager, reqId, user);
    if (approvalRecordSet == true) {
      model.setMessageDefaultApproval(CmrConstants.DEFAULT_APPROVAL_FLAG);
    }

    return model.getMessageDefaultApproval();
  }

  private void createApprovalComment(EntityManager entityManager, String content, ApprovalReq approvalReq, AppUser user) throws Exception {
    ApprovalComments comment = new ApprovalComments();
    ApprovalCommentsPK pk = new ApprovalCommentsPK();
    pk.setApprovalCmtId(SystemUtil.getNextID(entityManager, SystemConfiguration.getValue("MANDT"), "APPROVAL_CMT_ID", "CREQCMR"));
    comment.setId(pk);

    comment.setApprovalId(approvalReq.getId().getApprovalId());
    comment.setStatus(approvalReq.getStatus());
    comment.setComments(content);
    comment.setCreateBy(user.getIntranetId());
    comment.setCreateTs(SystemUtil.getCurrentTimestamp());
    createEntity(comment, entityManager);
  }

  private boolean setApproverReqByReqId(EntityManager entityManager, long reqId, AppUser user) {
    boolean setApprovalRec = false;
    String sql = ExternalizedQuery.getSql("GET_APPROVAL_REQ_BY_REQID");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", reqId);
    query.setParameter("STATUS", CmrConstants.APPROVAL_DRAFT);
    List<ApprovalReq> approvalRecords = query.getResults(ApprovalReq.class);

    if (approvalRecords != null) {
      for (ApprovalReq approval : approvalRecords) {
        approval.setStatus(CmrConstants.APPROVAL_PENDING_MAIL);
        approval.setLastUpdtBy(user.getIntranetId());
        approval.setLastUpdtTs(SystemUtil.getCurrentTimestamp());
        updateEntity(approval, entityManager);
        setApprovalRec = true;
      }
    }
    return setApprovalRec;
  }

  /**
   * Checks if the approval type is a approve-one-approve-all type
   * 
   * @return
   */
  private boolean shouldAutomateApproval(ApprovalTyp approvalType) {
    if ("Y".equals(approvalType.getGrpApprovalIndc())) {
      return true;
    }
    List<String> automatedTemplates = Arrays.asList("dpl.html", "ero.html", "accounts.html", "so.html");
    for (String template : automatedTemplates) {
      if (approvalType != null && approvalType.getTemplateName().trim().toLowerCase().endsWith(template)) {
        return true;
      }
    }
    return false;
  }

  private String getCmrIssuingCntry(EntityManager entityManager, long reqId) {
    String sql = ExternalizedQuery.getSql("DATA.GET.RECORD.BYID");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", reqId);

    List<Data> rs = query.getResults(1, Data.class);

    if (rs != null && rs.size() > 0) {
      return rs.get(0).getCmrIssuingCntry();
    }
    return null;
  }

}
