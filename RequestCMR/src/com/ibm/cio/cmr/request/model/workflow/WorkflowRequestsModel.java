/**
 * 
 */
package com.ibm.cio.cmr.request.model.workflow;

import java.util.Date;

import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.model.BaseModel;

//import com.ibm.security.util.calendar.BaseCalendar.Date;

/**
 * @author Sonali Jain
 * 
 */
public class WorkflowRequestsModel extends BaseModel {

  private static final long serialVersionUID = 1L;

  private long reqId;
  private String requesterId;
  private String expediteInd;
  private String reqType;
  private String custType;
  private String reqStatus;
  private Date createTs;
  private Date lastUpdtTs;
  private String createTsString;
  private String lastUpdtTsString;
  private long createTsMillis;
  private long lastUpdtTsMillis;
  private String lockBy;

  private String userId;

  private String custName;
  private String overallStatus;
  private String reqTypeText;
  private String claimField;

  private String workflowType;

  private String rejectReason;

  private String cmrIssuingCntry;
  private String cmrNo;
  private String cmrOwner;
  private String requesterNm;
  private String originatorNm;

  private String cmrOwnerDesc;
  private String cmrIssuingCntryDesc;

  private String processedFlag;
  private String processingStatus;

  private String canClaim;
  private String canClaimAll;
  private String typeDescription;

  private String prospect;
  private int iterationId;

  private String newReqType;
  private String newReqCntry;

  private String reqReason;

  private String pendingAppr;

  private String sourceSystId;
  private String requestDueDate;

  @Override
  public boolean allKeysAssigned() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public String getRecordDescription() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void addKeyParameters(ModelAndView mv) {
    // TODO Auto-generated method stub

  }

  @Override
  public void addKeyParameters(ModelMap map) {
    // TODO Auto-generated method stub

  }

  public String getRequesterId() {
    return requesterId;
  }

  public void setRequesterId(String requesterId) {
    this.requesterId = requesterId;
  }

  public String getExpediteInd() {
    return expediteInd;
  }

  public void setExpediteInd(String expediteInd) {
    this.expediteInd = expediteInd;
  }

  public String getReqType() {
    return reqType;
  }

  public void setReqType(String reqType) {
    this.reqType = reqType;
  }

  public String getCustType() {
    return custType;
  }

  public void setCustType(String custType) {
    this.custType = custType;
  }

  public String getReqStatus() {
    return reqStatus;
  }

  public void setReqStatus(String reqStatus) {
    this.reqStatus = reqStatus;
  }

  public String getLockBy() {
    return lockBy;
  }

  public void setLockBy(String lockBy) {
    this.lockBy = lockBy;
  }

  public static long getSerialversionuid() {
    return serialVersionUID;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public Date getCreateTs() {
    return createTs;
  }

  public void setCreateTs(Date createTs) {
    this.createTs = createTs;
    this.createTsString = CmrConstants.DATE_FORMAT().format(createTs);
    this.createTsMillis = createTs.getTime();
  }

  public Date getLastUpdtTs() {
    return lastUpdtTs;
  }

  public void setLastUpdtTs(Date lastUpdtTs) {
    this.lastUpdtTs = lastUpdtTs;
    this.lastUpdtTsString = CmrConstants.DATE_FORMAT().format(lastUpdtTs);
    this.lastUpdtTsMillis = lastUpdtTs.getTime();
  }

  public String getCreateTsString() {
    return createTsString;
  }

  public void setCreateTsString(String createTsString) {
    this.createTsString = createTsString;
  }

  public String getLastUpdtTsString() {
    return lastUpdtTsString;
  }

  public void setLastUpdtTsString(String lastUpdtTsString) {
    this.lastUpdtTsString = lastUpdtTsString;
  }

  public long getReqId() {
    return reqId;
  }

  public void setReqId(long reqId) {
    this.reqId = reqId;
  }

  public String getCustName() {
    return custName;
  }

  public void setCustName(String custName) {
    this.custName = custName;
  }

  public String getWorkflowType() {
    return workflowType;
  }

  public void setWorkflowType(String workflowType) {
    this.workflowType = workflowType;
  }

  public String getOverallStatus() {
    return overallStatus;
  }

  public void setOverallStatus(String overallStatus) {
    this.overallStatus = overallStatus;
  }

  public String getReqTypeText() {
    return reqTypeText;
  }

  public void setReqTypeText(String reqTypeText) {
    this.reqTypeText = reqTypeText;
  }

  public long getCreateTsMillis() {
    return createTsMillis;
  }

  public void setCreateTsMillis(long createTsMillis) {
    this.createTsMillis = createTsMillis;
  }

  public long getLastUpdtTsMillis() {
    return lastUpdtTsMillis;
  }

  public void setLastUpdtTsMillis(long lastUpdtTsMillis) {
    this.lastUpdtTsMillis = lastUpdtTsMillis;
  }

  public String getRejectReason() {
    return rejectReason;
  }

  public void setRejectReason(String rejectReason) {
    this.rejectReason = rejectReason;
  }

  public String getClaimField() {
    return claimField;
  }

  public void setClaimField(String claimField) {
    this.claimField = claimField;
  }

  public String getCmrIssuingCntry() {
    return cmrIssuingCntry;
  }

  public void setCmrIssuingCntry(String cmrIssuingCntry) {
    this.cmrIssuingCntry = cmrIssuingCntry;
  }

  public String getCmrNo() {
    return cmrNo;
  }

  public void setCmrNo(String cmrNo) {
    this.cmrNo = cmrNo;
  }

  public String getCmrOwner() {
    return cmrOwner;
  }

  public void setCmrOwner(String cmrOwner) {
    this.cmrOwner = cmrOwner;
  }

  public String getRequesterNm() {
    return requesterNm;
  }

  public void setRequesterNm(String requesterNm) {
    this.requesterNm = requesterNm;
  }

  public String getOriginatorNm() {
    return originatorNm;
  }

  public void setOriginatorNm(String originatorNm) {
    this.originatorNm = originatorNm;
  }

  public String getCmrIssuingCntryDesc() {
    return cmrIssuingCntryDesc;
  }

  public void setCmrIssuingCntryDesc(String cmrIssuingCntryDesc) {
    this.cmrIssuingCntryDesc = cmrIssuingCntryDesc;
  }

  public String getCmrOwnerDesc() {
    return cmrOwnerDesc;
  }

  public void setCmrOwnerDesc(String cmrOwnerDesc) {
    this.cmrOwnerDesc = cmrOwnerDesc;
  }

  public String getProcessedFlag() {
    return processedFlag;
  }

  public void setProcessedFlag(String processedFlag) {
    this.processedFlag = processedFlag;
  }

  public String getProcessingStatus() {
    return processingStatus;
  }

  public void setProcessingStatus(String processingStatus) {
    this.processingStatus = processingStatus;
  }

  public String getCanClaim() {
    return canClaim;
  }

  public void setCanClaim(String canClaim) {
    this.canClaim = canClaim;
  }

  public String getCanClaimAll() {
    return canClaimAll;
  }

  public void setCanClaimAll(String canClaimAll) {
    this.canClaimAll = canClaimAll;
  }

  public String getTypeDescription() {
    return typeDescription;
  }

  public void setTypeDescription(String typeDescription) {
    this.typeDescription = typeDescription;
  }

  public String getProspect() {
    return prospect;
  }

  public void setProspect(String prospect) {
    this.prospect = prospect;
  }

  public int getIterationId() {
    return iterationId;
  }

  public void setIterationId(int iterationId) {
    this.iterationId = iterationId;
  }

  public String getNewReqType() {
    return newReqType;
  }

  public void setNewReqType(String newReqType) {
    this.newReqType = newReqType;
  }

  public String getNewReqCntry() {
    return newReqCntry;
  }

  public void setNewReqCntry(String newReqCntry) {
    this.newReqCntry = newReqCntry;
  }

  public String getReqReason() {
    return reqReason;
  }

  public void setReqReason(String reqReason) {
    this.reqReason = reqReason;
  }

  public String getPendingAppr() {
    return pendingAppr;
  }

  public void setPendingAppr(String pendingAppr) {
    this.pendingAppr = pendingAppr;
  }

  public String getSourceSystId() {
    return sourceSystId;
  }

  public void setSourceSystId(String sourceSystId) {
    this.sourceSystId = sourceSystId;
  }

  public String getRequestDueDate() {
    return requestDueDate;
  }

  public void setRequestDueDate(String requestDueDate) {
    this.requestDueDate = requestDueDate;
  }

}
