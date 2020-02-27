/**
 * 
 */
package com.ibm.cio.cmr.request.model.approval;

import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.cio.cmr.request.model.BaseModel;

/**
 * @author JeffZAMORA
 * 
 */
public class MyApprovalsModel extends BaseModel {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  private long reqId;

  private long approvalId;

  private String custNm;

  private String requesterNm;

  private String requesterId;

  private String reqType;

  private String reqStatus;

  private String cntryCd;

  private String cntryDesc;

  private String reqReason;

  private String lob;

  private String dplChkResult;

  private String approvalType;

  private String approverNm;

  private String approverId;

  private String approvalStatus;

  private String mass;

  private String comments;

  private String rejReason;

  private String pendingOnly;

  @Override
  public boolean allKeysAssigned() {
    return this.reqId > 0 && this.approvalId > 0;
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

  public long getReqId() {
    return reqId;
  }

  public void setReqId(long reqId) {
    this.reqId = reqId;
  }

  public long getApprovalId() {
    return approvalId;
  }

  public void setApprovalId(long approvalId) {
    this.approvalId = approvalId;
  }

  public String getCustNm() {
    return custNm;
  }

  public void setCustNm(String custNm) {
    this.custNm = custNm;
  }

  public String getRequesterNm() {
    return requesterNm;
  }

  public void setRequesterNm(String requesterNm) {
    this.requesterNm = requesterNm;
  }

  public String getRequesterId() {
    return requesterId;
  }

  public void setRequesterId(String requesterId) {
    this.requesterId = requesterId;
  }

  public String getReqType() {
    return reqType;
  }

  public void setReqType(String reqType) {
    this.reqType = reqType;
  }

  public String getReqStatus() {
    return reqStatus;
  }

  public void setReqStatus(String reqStatus) {
    this.reqStatus = reqStatus;
  }

  public String getCntryCd() {
    return cntryCd;
  }

  public void setCntryCd(String cntryCd) {
    this.cntryCd = cntryCd;
  }

  public String getCntryDesc() {
    return cntryDesc;
  }

  public void setCntryDesc(String cntryDesc) {
    this.cntryDesc = cntryDesc;
  }

  public String getReqReason() {
    return reqReason;
  }

  public void setReqReason(String reqReason) {
    this.reqReason = reqReason;
  }

  public String getLob() {
    return lob;
  }

  public void setLob(String lob) {
    this.lob = lob;
  }

  public String getDplChkResult() {
    return dplChkResult;
  }

  public void setDplChkResult(String dplChkResult) {
    this.dplChkResult = dplChkResult;
  }

  public String getApprovalType() {
    return approvalType;
  }

  public void setApprovalType(String approvalType) {
    this.approvalType = approvalType;
  }

  public String getApproverNm() {
    return approverNm;
  }

  public void setApproverNm(String approverNm) {
    this.approverNm = approverNm;
  }

  public String getApproverId() {
    return approverId;
  }

  public void setApproverId(String approverId) {
    this.approverId = approverId;
  }

  public String getApprovalStatus() {
    return approvalStatus;
  }

  public void setApprovalStatus(String approvalStatus) {
    this.approvalStatus = approvalStatus;
  }

  public String getMass() {
    return mass;
  }

  public void setMass(String mass) {
    this.mass = mass;
  }

  public String getComments() {
    return comments;
  }

  public void setComments(String comments) {
    this.comments = comments;
  }

  public String getPendingOnly() {
    return pendingOnly;
  }

  public void setPendingOnly(String pendingOnly) {
    this.pendingOnly = pendingOnly;
  }

  public String getRejReason() {
    return rejReason;
  }

  public void setRejReason(String rejReason) {
    this.rejReason = rejReason;
  }

}
