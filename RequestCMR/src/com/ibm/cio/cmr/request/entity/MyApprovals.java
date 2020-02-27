/**
 * 
 */
package com.ibm.cio.cmr.request.entity;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;

/**
 * @author JeffZAMORA
 * 
 */
@Entity
public class MyApprovals extends BaseEntity<MyApprovalsPK> {

  @EmbeddedId
  private MyApprovalsPK id;

  @Column(
      name = "CUST_NM")
  private String custNm;

  @Column(
      name = "REQUESTER_NM")
  private String requesterNm;

  @Column(
      name = "REQUESTER_ID")
  private String requesterId;

  @Column(
      name = "REQ_TYPE")
  private String reqType;

  @Column(
      name = "REQ_STATUS")
  private String reqStatus;

  @Column(
      name = "CNTRY_CD")
  private String cntryCd;

  @Column(
      name = "CNTRY_DESC")
  private String cntryDesc;

  @Column(
      name = "REQ_REASON")
  private String reqReason;

  @Column(
      name = "LOB")
  private String lob;

  @Column(
      name = "DPL_CHK_RESULT")
  private String dplChkResult;

  @Column(
      name = "APPROVAL_TYPE")
  private String approvalType;

  @Column(
      name = "APPROVER_NM")
  private String approverNm;

  @Column(
      name = "APPROVER_ID")
  private String approverId;

  @Column(
      name = "APPROVAL_STATUS")
  private String approvalStatus;

  @Override
  public MyApprovalsPK getId() {
    return id;
  }

  @Override
  public void setId(MyApprovalsPK id) {
    this.id = id;
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

}
