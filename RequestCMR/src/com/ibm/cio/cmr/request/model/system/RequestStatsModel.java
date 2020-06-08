/**
 * 
 */
package com.ibm.cio.cmr.request.model.system;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;

/**
 * @author Jeffrey Zamora
 * 
 */
@Entity
public class RequestStatsModel {

  @EmbeddedId
  private StatsPK id;

  @Column(name = "CNTRY_CD")
  private String cntryCd;

  @Column(name = "CNTRY_DESC")
  private String cntryDesc;

  private String iot;

  private String owner;

  private String lob;

  @Column(name = "REQ_TYPE")
  private String reqType;

  @Column(name = "PROSPECT")
  private String prospectConversion;

  @Column(name = "CUST_NM")
  private String custNm;

  @Column(name = "CMR_NO")
  private String cmrNo;

  @Column(name = "REQ_REASON")
  private String reqReason;

  @Column(name = "SOURCE_SYST_ID")
  private String sourceSystId;

  @Column(name = "REQUESTER_NM")
  private String requesterNm;

  @Column(name = "REQUESTER_ID")
  private String requesterId;

  @Column(name = "REVIEWER_NM")
  private String reviewerNm;

  @Column(name = "REVIEWER_ID")
  private String reviewerId;

  @Column(name = "APPROVER_NM")
  private String approverNm;

  @Column(name = "APPROVER_ID")
  private String approverId;

  @Column(name = "PROCESSOR_NM")
  private String processorNm;

  @Column(name = "PROCESSOR_ID")
  private String processorId;

  @Column(name = "REQ_YR")
  private String reqYr;

  @Column(name = "REQ_MONTH")
  private String reqMonth;

  @Column(name = "REQ_DT")
  private String reqDt;

  @Column(name = "CLOSE_YR")
  private String closeYr;

  @Column(name = "CLOSE_MONTH")
  private String closeMonth;

  @Column(name = "CLOSE_DT")
  private String closeDt;

  @Column(name = "FINAL_STATUS")
  private String finalStatus;

  @Column(name = "REJECT_TOTAL")
  private long rejectTotal;

  @Column(name = "LAST_REJ_REASON")
  private String lastRejReason;

  @Column(name = "DAY_PROCESS")
  private String dayProcess;

  @Column(name = "REQUEST_TAT")
  private long requestTat = -1;

  @Column(name = "PENDING_TAT")
  private long pendingTat = -1;

  @Column(name = "REVIEW_TAT")
  private long reviewTat = -1;

  @Column(name = "APPROVAL_TAT")
  private long approvalTat = -1;

  @Column(name = "APPROVAL2_TAT")
  private long approval2Tat = -1;

  @Column(name = "PROCESS_TAT")
  private long processTat = -1;

  @Column(name = "OVERALL_TAT")
  private long overallTat = -1;

  @Column(name = "AUTO_TAT")
  private long autoTat = -1;

  @Column(name = "SUBMIT_TO_COMPLETE_TAT")
  private long submitToCompleteTat = -1;

  @Column(name = "CUST_TYPE")
  private String custType;

  @Column(name = "SCENARIO_TYPE_DESC")
  private String scenarioType;

  @Column(name = "SCENARIO_SUBTYPE_DESC")
  private String scenarioSubType;

  @Column(name = "REQUEST_DUE_DATE")
  private String requestDueDate;

  @Column(name = "COMPLETION_TS")
  private String completionTs;

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

  public String getIot() {
    return iot;
  }

  public void setIot(String iot) {
    this.iot = iot;
  }

  public String getOwner() {
    return owner;
  }

  public void setOwner(String owner) {
    this.owner = owner;
  }

  public String getLob() {
    return lob;
  }

  public void setLob(String lob) {
    this.lob = lob;
  }

  public String getReqType() {
    return reqType;
  }

  public void setReqType(String reqType) {
    this.reqType = reqType;
  }

  public String getCustNm() {
    return custNm;
  }

  public void setCustNm(String custNm) {
    this.custNm = custNm;
  }

  public String getCmrNo() {
    return cmrNo;
  }

  public void setCmrNo(String cmrNo) {
    this.cmrNo = cmrNo;
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

  public String getReviewerNm() {
    return reviewerNm;
  }

  public void setReviewerNm(String reviewerNm) {
    this.reviewerNm = reviewerNm;
  }

  public String getReviewerId() {
    return reviewerId;
  }

  public void setReviewerId(String reviewerId) {
    this.reviewerId = reviewerId;
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

  public String getProcessorNm() {
    return processorNm;
  }

  public void setProcessorNm(String processorNm) {
    this.processorNm = processorNm;
  }

  public String getProcessorId() {
    return processorId;
  }

  public void setProcessorId(String processorId) {
    this.processorId = processorId;
  }

  public String getReqYr() {
    return reqYr;
  }

  public void setReqYr(String reqYr) {
    this.reqYr = reqYr;
  }

  public String getReqMonth() {
    return reqMonth;
  }

  public void setReqMonth(String reqMonth) {
    this.reqMonth = reqMonth;
  }

  public String getReqDt() {
    return reqDt;
  }

  public void setReqDt(String reqDt) {
    this.reqDt = reqDt;
  }

  public String getCloseYr() {
    return closeYr;
  }

  public void setCloseYr(String closeYr) {
    this.closeYr = closeYr;
  }

  public String getCloseMonth() {
    return closeMonth;
  }

  public void setCloseMonth(String closeMonth) {
    this.closeMonth = closeMonth;
  }

  public String getCloseDt() {
    return closeDt;
  }

  public void setCloseDt(String closeDt) {
    this.closeDt = closeDt;
  }

  public String getFinalStatus() {
    return finalStatus;
  }

  public void setFinalStatus(String finalStatus) {
    this.finalStatus = finalStatus;
  }

  public String getLastRejReason() {
    return lastRejReason;
  }

  public void setLastRejReason(String lastRejReason) {
    this.lastRejReason = lastRejReason;
  }

  public long getRequestTat() {
    return requestTat;
  }

  public void setRequestTat(long requestTat) {
    this.requestTat = requestTat;
  }

  public long getPendingTat() {
    return pendingTat;
  }

  public void setPendingTat(long pendingTat) {
    this.pendingTat = pendingTat;
  }

  public long getReviewTat() {
    return reviewTat;
  }

  public void setReviewTat(long reviewTat) {
    this.reviewTat = reviewTat;
  }

  public long getApprovalTat() {
    return approvalTat;
  }

  public void setApprovalTat(long approvalTat) {
    this.approvalTat = approvalTat;
  }

  public long getOverallTat() {
    return overallTat;
  }

  public void setOverallTat(long overallTat) {
    this.overallTat = overallTat;
  }

  public String getDayProcess() {
    return dayProcess;
  }

  public void setDayProcess(String dayProcess) {
    this.dayProcess = dayProcess;
  }

  public long getProcessTat() {
    return processTat;
  }

  public void setProcessTat(long processTat) {
    this.processTat = processTat;
  }

  public StatsPK getId() {
    return id;
  }

  public void setId(StatsPK id) {
    this.id = id;
  }

  public long getSubmitToCompleteTat() {
    return submitToCompleteTat;
  }

  public void setSubmitToCompleteTat(long submitToCompleteTat) {
    this.submitToCompleteTat = submitToCompleteTat;
  }

  public long getRejectTotal() {
    return rejectTotal;
  }

  public void setRejectTotal(long rejectTotal) {
    this.rejectTotal = rejectTotal;
  }

  public String getProspectConversion() {
    return prospectConversion;
  }

  public void setProspectConversion(String prospectConversion) {
    this.prospectConversion = prospectConversion;
  }

  public long getApproval2Tat() {
    return approval2Tat;
  }

  public void setApproval2Tat(long approval2Tat) {
    this.approval2Tat = approval2Tat;
  }

  public String getScenarioType() {
    return scenarioType;
  }

  public void setScenarioType(String scenarioType) {
    this.scenarioType = scenarioType;
  }

  public String getScenarioSubType() {
    return scenarioSubType;
  }

  public void setScenarioSubType(String scenarioSubType) {
    this.scenarioSubType = scenarioSubType;
  }

  public String getCustType() {
    return custType;
  }

  public void setCustType(String custType) {
    this.custType = custType;
  }

  public String getReqReason() {
    return reqReason;
  }

  public void setReqReason(String reqReason) {
    this.reqReason = reqReason;
  }

  public String getRequestDueDate() {
    return requestDueDate;
  }

  public void setRequestDueDate(String requestDueDate) {
    this.requestDueDate = requestDueDate;
  }

  public String getCompletionTs() {
    return completionTs;
  }

  public void setCompletionTs(String completionTs) {
    this.completionTs = completionTs;
  }

  public String getSourceSystId() {
    return sourceSystId;
  }

  public void setSourceSystId(String sourceSystId) {
    this.sourceSystId = sourceSystId;
  }

  public long getAutoTat() {
    return autoTat;
  }

  public void setAutoTat(long autoTat) {
    this.autoTat = autoTat;
  }
}
