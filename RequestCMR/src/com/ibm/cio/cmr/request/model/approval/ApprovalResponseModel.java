/**
 * 
 */
package com.ibm.cio.cmr.request.model.approval;

import java.util.Date;
import java.util.List;

import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.cio.cmr.request.model.BaseModel;

/**
 * Model for approvals
 * 
 * @author Jeffrey Zamora
 * 
 */
public class ApprovalResponseModel extends BaseModel {

  private static final long serialVersionUID = 1L;
  private String approverId;
  private String type;
  private long approvalId;
  private String comments;
  private String rejReason;

  private String requester;
  private String requesterId;
  private String approverNm;
  private String currentStatus;
  private boolean processed;
  private String processing;
  private String approvalCode;
  private String actionDone;

  private long reqId;
  private int typId;
  private String notesId;
  private String intranetId;
  private String displayName;
  private Date createTs;
  private String createTsString;
  private String status;
  private String statusStr;
  private String requiredIndc;

  private List<ApprovalCommentModel> commentList;

  public String getApproverId() {
    return approverId;
  }

  public void setApproverId(String approverId) {
    this.approverId = approverId;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public long getApprovalId() {
    return approvalId;
  }

  public void setApprovalId(long approvalId) {
    this.approvalId = approvalId;
  }

  public String getComments() {
    return comments;
  }

  public void setComments(String comments) {
    this.comments = comments;
  }

  public String getRejReason() {
    return rejReason;
  }

  public void setRejReason(String rejReason) {
    this.rejReason = rejReason;
  }

  public String getRequester() {
    return requester;
  }

  public void setRequester(String requester) {
    this.requester = requester;
  }

  public String getApproverNm() {
    return approverNm;
  }

  public void setApproverNm(String approverNm) {
    this.approverNm = approverNm;
  }

  public String getCurrentStatus() {
    return currentStatus;
  }

  public void setCurrentStatus(String currentStatus) {
    this.currentStatus = currentStatus;
  }

  public boolean isProcessed() {
    return processed;
  }

  public void setProcessed(boolean processed) {
    this.processed = processed;
  }

  public String getProcessing() {
    return processing;
  }

  public void setProcessing(String processing) {
    this.processing = processing;
  }

  public String getRequesterId() {
    return requesterId;
  }

  public void setRequesterId(String requesterId) {
    this.requesterId = requesterId;
  }

  @Override
  public boolean allKeysAssigned() {
    return false;
  }

  @Override
  public String getRecordDescription() {
    return null;
  }

  @Override
  public void addKeyParameters(ModelAndView mv) {
  }

  @Override
  public void addKeyParameters(ModelMap map) {
  }

  public String getApprovalCode() {
    return approvalCode;
  }

  public void setApprovalCode(String approvalCode) {
    this.approvalCode = approvalCode;
  }

  public String getActionDone() {
    return actionDone;
  }

  public void setActionDone(String actionDone) {
    this.actionDone = actionDone;
  }

  public long getReqId() {
    return reqId;
  }

  public void setReqId(long reqId) {
    this.reqId = reqId;
  }

  public int getTypId() {
    return typId;
  }

  public void setTypId(int typId) {
    this.typId = typId;
  }

  public String getNotesId() {
    return notesId;
  }

  public void setNotesId(String notesId) {
    this.notesId = notesId;
  }

  public String getIntranetId() {
    return intranetId;
  }

  public void setIntranetId(String intranetId) {
    this.intranetId = intranetId;
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public Date getCreateTs() {
    return createTs;
  }

  public void setCreateTs(Date createTs) {
    this.createTs = createTs;
  }

  public String getCreateTsString() {
    return createTsString;
  }

  public void setCreateTsString(String createTsString) {
    this.createTsString = createTsString;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getStatusStr() {
    return statusStr;
  }

  public void setStatusStr(String statusStr) {
    this.statusStr = statusStr;
  }

  public List<ApprovalCommentModel> getCommentList() {
    return commentList;
  }

  public void setCommentList(List<ApprovalCommentModel> commentList) {
    this.commentList = commentList;
  }

  public String getRequiredIndc() {
    return requiredIndc;
  }

  public void setRequiredIndc(String requiredIndc) {
    this.requiredIndc = requiredIndc;
  }

}
