/**
 * 
 */
package com.ibm.cio.cmr.request.model.workflow;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.model.BaseModel;

/**
 * @author Rangoli Saxena
 * 
 */
public class RequestSearchCriteriaModel extends BaseModel {

  private static final long serialVersionUID = 1L;

  private String wfProcCentre;
  private String wfbluePagesName;
  private String wfbluePagesId;
  private String customerName;

  // chnages in criteria mockup on 8 Sep
  private String wfReqName;
  private String wfReqId;
  private String wfOrgName;
  private String wfOrgId;
  private String wfClaimByName;
  private String wfClaimById;
  private String cmrIssuingCountry;
  private String cmrOwnerCriteria;
  private String processedBy;
  private String processedByName;
  private String cmrNoCriteria;
  private String requestType;
  private String procStatus;

  public String getResultRows() {
    return resultRows;
  }

  public void setResultRows(String resultRows) {
    this.resultRows = resultRows;
  }

  private String searchCusType;
  private String requestId;
  private String requestStatus;
  private String expediteChk;
  private String resultRows;
  // private int resultRows;
  private String createDateFrom;
  private String createDateTo;
  private String lastActDateFrom;
  private String lastActDateTo;

  private long reqId;
  private String expediteInd;
  private String reqType;
  private String custName;
  private String reqStatus;
  private String createTsString;
  private String lastUpdtTsString;
  private String lockBy;
  private String overallStatus;
  private String reqTypeText;

  private Date createTs;
  private Date lastUpdtTs;
  private String custType;
  private String requesterId;

  private String claimField;
  SimpleDateFormat formatter = CmrConstants.DATE_FORMAT();

  private String cmrIssuingCntry;
  private String cmrNo;
  private String cmrOwner;
  private String requesterNm;
  private String originatorNm;
  private String processedFlag;
  private String processingStatus;

  private String canClaim;
  private String canClaimAll;
  private String typeDescription;
  private String prospect;
  private int iterationId;
  private String pendingAppr;
  private String sourceSystId;

  public String getCmrOwnerDesc() {
    return cmrOwnerDesc;
  }

  public void setCmrOwnerDesc(String cmrOwnerDesc) {
    this.cmrOwnerDesc = cmrOwnerDesc;
  }

  public String getCmrIssuingCntryDesc() {
    return cmrIssuingCntryDesc;
  }

  public void setCmrIssuingCntryDesc(String cmrIssuingCntryDesc) {
    this.cmrIssuingCntryDesc = cmrIssuingCntryDesc;
  }

  private String cmrOwnerDesc;
  private String cmrIssuingCntryDesc;

  public String getWfProcCentre() {
    return wfProcCentre;
  }

  public void setWfProcCentre(String wfProcCentre) {
    this.wfProcCentre = wfProcCentre;
  }

  public String getWfbluePagesName() {
    return wfbluePagesName;
  }

  public void setWfbluePagesName(String wfbluePagesName) {
    this.wfbluePagesName = wfbluePagesName;
  }

  public String getWfbluePagesId() {
    return wfbluePagesId;
  }

  public void setWfbluePagesId(String wfbluePagesId) {
    this.wfbluePagesId = wfbluePagesId;
  }

  public String getCustomerName() {
    return customerName;
  }

  public void setCustomerName(String customerName) {
    this.customerName = customerName;
  }

  public String getSearchCusType() {
    return searchCusType;
  }

  public void setSearchCusType(String searchCusType) {
    this.searchCusType = searchCusType;
  }

  public String getRequestId() {
    return requestId;
  }

  public void setRequestId(String requestId) {
    this.requestId = requestId;
  }

  public String getRequestStatus() {
    return requestStatus;
  }

  public void setRequestStatus(String requestStatus) {
    this.requestStatus = requestStatus;
  }

  public String getExpediteChk() {
    return expediteChk;
  }

  public void setExpediteChk(String expediteChk) {
    this.expediteChk = expediteChk;
  }

  /*
   * public int getResultRows() { return resultRows; } public void
   * setResultRows(int resultRows) { this.resultRows = resultRows; }
   */
  public String getCreateDateFrom() {
    return createDateFrom;
  }

  public void setCreateDateFrom(String createDateFrom) {
    this.createDateFrom = createDateFrom;
  }

  public String getCreateDateTo() {
    return createDateTo;
  }

  public void setCreateDateTo(String createDateTo) {
    this.createDateTo = createDateTo;
  }

  public String getLastActDateFrom() {
    return lastActDateFrom;
  }

  public void setLastActDateFrom(String lastActDateFrom) {
    this.lastActDateFrom = lastActDateFrom;
  }

  public String getLastActDateTo() {
    return lastActDateTo;
  }

  public void setLastActDateTo(String lastActDateTo) {
    this.lastActDateTo = lastActDateTo;
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

  public String getCustName() {
    return custName;
  }

  public void setCustName(String custName) {
    this.custName = custName;
  }

  public String getReqStatus() {
    return reqStatus;
  }

  public void setReqStatus(String reqStatus) {
    this.reqStatus = reqStatus;
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

  public String getLockBy() {
    return lockBy;
  }

  public void setLockBy(String lockBy) {
    this.lockBy = lockBy;
  }

  public Date getCreateTs() {
    return createTs;
  }

  public void setCreateTs(Date createTs) {
    this.createTs = createTs;
    this.createTsString = formatter.format(createTs);
  }

  public Date getLastUpdtTs() {
    return lastUpdtTs;
  }

  public void setLastUpdtTs(Date lastUpdtTs) {
    this.lastUpdtTs = lastUpdtTs;
    this.lastUpdtTsString = formatter.format(lastUpdtTs);

  }

  public long getReqId() {
    return reqId;
  }

  public void setReqId(long reqId) {
    this.reqId = reqId;
  }

  public String getRequesterId() {
    return requesterId;
  }

  public void setRequesterId(String requesterId) {
    this.requesterId = requesterId;
  }

  public String getCustType() {
    return custType;
  }

  public void setCustType(String custType) {
    this.custType = custType;
  }

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

  public String getWfReqName() {
    return wfReqName;
  }

  public void setWfReqName(String wfReqName) {
    this.wfReqName = wfReqName;
  }

  public String getWfReqId() {
    return wfReqId;
  }

  public void setWfReqId(String wfReqId) {
    this.wfReqId = wfReqId;
  }

  public String getWfOrgName() {
    return wfOrgName;
  }

  public void setWfOrgName(String wfOrgName) {
    this.wfOrgName = wfOrgName;
  }

  public String getWfOrgId() {
    return wfOrgId;
  }

  public void setWfOrgId(String wfOrgId) {
    this.wfOrgId = wfOrgId;
  }

  public String getWfClaimByName() {
    return wfClaimByName;
  }

  public void setWfClaimByName(String wfClaimByName) {
    this.wfClaimByName = wfClaimByName;
  }

  public String getWfClaimById() {
    return wfClaimById;
  }

  public void setWfClaimById(String wfClaimById) {
    this.wfClaimById = wfClaimById;
  }

  public String getCmrIssuingCountry() {
    return cmrIssuingCountry;
  }

  public void setCmrIssuingCountry(String cmrIssuingCountry) {
    this.cmrIssuingCountry = cmrIssuingCountry;
  }

  public String getCmrOwnerCriteria() {
    return cmrOwnerCriteria;
  }

  public void setCmrOwnerCriteria(String cmrOwnerCriteria) {
    this.cmrOwnerCriteria = cmrOwnerCriteria;
  }

  public String getCmrNoCriteria() {
    return cmrNoCriteria;
  }

  public void setCmrNoCriteria(String cmrNoCriteria) {
    this.cmrNoCriteria = cmrNoCriteria;
  }

  public String getRequestType() {
    return requestType;
  }

  public void setRequestType(String requestType) {
    this.requestType = requestType;
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

  public String getProcStatus() {
    return procStatus;
  }

  public void setProcStatus(String procStatus) {
    this.procStatus = procStatus;
  }

  public String getProcessedBy() {
    return processedBy;
  }

  public void setProcessedBy(String processedBy) {
    this.processedBy = processedBy;
  }

  public String getProcessedByName() {
    return processedByName;
  }

  public void setProcessedByName(String processedByName) {
    this.processedByName = processedByName;
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

}
