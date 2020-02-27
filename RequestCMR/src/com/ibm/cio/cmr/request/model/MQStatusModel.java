/**
 * 
 */
package com.ibm.cio.cmr.request.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author Jeffrey Zamora
 * 
 */
public class MQStatusModel extends BaseModel {

  private long queryReqId;

  private long reqId;

  private String reqType;

  private String cmrNo;

  private String cmrIssuingCntry;

  private String reqStatus;

  private String errorCd;

  private String execpMessage;

  private String warning;

  private String adminStatus;
  private String adminStatusDesc;

  @Temporal(TemporalType.TIMESTAMP)
  private Date createTs;

  private String createBy;

  @Temporal(TemporalType.TIMESTAMP)
  private Date lastUpdtTs;

  private String lastUpdtBy;

  private String targetSys;

  private String correlationId;

  private String docmRefnNo;

  private String refnSourceCd;

  private String hasData;

  @Column(
      name = "MQ_IND")
  private String mqInd;

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  @Override
  public boolean allKeysAssigned() {
    return false;
  }

  @Override
  public String getRecordDescription() {
    return "MQ Queue";
  }

  @Override
  public void addKeyParameters(ModelAndView mv) {
  }

  @Override
  public void addKeyParameters(ModelMap map) {
  }

  public long getQueryReqId() {
    return queryReqId;
  }

  public void setQueryReqId(long queryReqId) {
    this.queryReqId = queryReqId;
  }

  public long getReqId() {
    return reqId;
  }

  public void setReqId(long reqId) {
    this.reqId = reqId;
  }

  public String getReqType() {
    return reqType;
  }

  public void setReqType(String reqType) {
    this.reqType = reqType;
  }

  public String getCmrNo() {
    return cmrNo;
  }

  public void setCmrNo(String cmrNo) {
    this.cmrNo = cmrNo;
  }

  public String getCmrIssuingCntry() {
    return cmrIssuingCntry;
  }

  public void setCmrIssuingCntry(String cmrIssuingCntry) {
    this.cmrIssuingCntry = cmrIssuingCntry;
  }

  public String getReqStatus() {
    return reqStatus;
  }

  public void setReqStatus(String reqStatus) {
    this.reqStatus = reqStatus;
  }

  public String getErrorCd() {
    return errorCd;
  }

  public void setErrorCd(String errorCd) {
    this.errorCd = errorCd;
  }

  public String getExecpMessage() {
    return execpMessage;
  }

  public void setExecpMessage(String execpMessage) {
    this.execpMessage = execpMessage;
  }

  public Date getCreateTs() {
    return createTs;
  }

  public void setCreateTs(Date createTs) {
    this.createTs = createTs;
  }

  public String getCreateBy() {
    return createBy;
  }

  public void setCreateBy(String createBy) {
    this.createBy = createBy;
  }

  public Date getLastUpdtTs() {
    return lastUpdtTs;
  }

  public void setLastUpdtTs(Date lastUpdtTs) {
    this.lastUpdtTs = lastUpdtTs;
  }

  public String getLastUpdtBy() {
    return lastUpdtBy;
  }

  public void setLastUpdtBy(String lastUpdtBy) {
    this.lastUpdtBy = lastUpdtBy;
  }

  public String getTargetSys() {
    return targetSys;
  }

  public void setTargetSys(String targetSys) {
    this.targetSys = targetSys;
  }

  public String getCorrelationId() {
    return correlationId;
  }

  public void setCorrelationId(String correlationId) {
    this.correlationId = correlationId;
  }

  public String getMqInd() {
    return mqInd;
  }

  public void setMqInd(String mqInd) {
    this.mqInd = mqInd;
  }

  public String getWarning() {
    return warning;
  }

  public void setWarning(String warning) {
    this.warning = warning;
  }

  public String getAdminStatus() {
    return adminStatus;
  }

  public void setAdminStatus(String adminStatus) {
    this.adminStatus = adminStatus;
  }

  public String getAdminStatusDesc() {
    return adminStatusDesc;
  }

  public void setAdminStatusDesc(String adminStatusDesc) {
    this.adminStatusDesc = adminStatusDesc;
  }

  public String getDocmRefnNo() {
    return docmRefnNo;
  }

  public void setDocmRefnNo(String docmRefnNo) {
    this.docmRefnNo = docmRefnNo;
  }

  public String getRefnSourceCd() {
    return refnSourceCd;
  }

  public void setRefnSourceCd(String refnSourceCd) {
    this.refnSourceCd = refnSourceCd;
  }

  public String getHasData() {
    return hasData;
  }

  public void setHasData(String hasData) {
    this.hasData = hasData;
  }

}
