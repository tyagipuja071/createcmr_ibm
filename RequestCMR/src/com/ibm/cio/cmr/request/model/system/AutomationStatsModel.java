/**
 * 
 */
package com.ibm.cio.cmr.request.model.system;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;

/**
 * @author JeffZAMORA
 *
 */
@Entity
public class AutomationStatsModel {

  @EmbeddedId
  private StatsPK id;

  @Column(name = "CMR_ISSUING_CNTRY")
  private String cmrIssuingCntry;

  @Column(name = "CNTRY_DESC")
  private String cntryDesc;

  @Column(name = "CUST_NM")
  private String custNm;

  @Column(name = "REQ_TYPE")
  private String reqType;

  @Column(name = "REQ_STATUS")
  private String reqStatus;

  @Column(name = "CMR_NO")
  private String cmrNo;

  @Column(name = "SOURCE_SYST_ID")
  private String sourceSystId;

  @Column(name = "FULL_AUTO")
  private String fullAuto;

  private String legacy;

  private String review;

  @Column(name = "PROCESS_CD")
  private String processCd;

  @Column(name = "FAILURE_INDC")
  private String failureIndc;

  private String reject;

  @Column(name = "REJ_REASON")
  private String rejReason;

  @Column(name = "CUST_GRP")
  private String custGrp;

  @Column(name = "CUST_SUB_GRP")
  private String custSubGrp;

  private String geo;

  private String market;

  @Column(name = "PROC_CENTER")
  private String procCenter;

  public StatsPK getId() {
    return id;
  }

  public void setId(StatsPK id) {
    this.id = id;
  }

  public String getCmrIssuingCntry() {
    return cmrIssuingCntry;
  }

  public void setCmrIssuingCntry(String cmrIssuingCntry) {
    this.cmrIssuingCntry = cmrIssuingCntry;
  }

  public String getCntryDesc() {
    return cntryDesc;
  }

  public void setCntryDesc(String cntryDesc) {
    this.cntryDesc = cntryDesc;
  }

  public String getCustNm() {
    return custNm;
  }

  public void setCustNm(String custNm) {
    this.custNm = custNm;
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

  public String getSourceSystId() {
    return sourceSystId;
  }

  public void setSourceSystId(String sourceSystId) {
    this.sourceSystId = sourceSystId;
  }

  public String getFullAuto() {
    return fullAuto;
  }

  public void setFullAuto(String fullAuto) {
    this.fullAuto = fullAuto;
  }

  public String getLegacy() {
    return legacy;
  }

  public void setLegacy(String legacy) {
    this.legacy = legacy;
  }

  public String getReview() {
    return review;
  }

  public void setReview(String review) {
    this.review = review;
  }

  public String getProcessCd() {
    return processCd;
  }

  public void setProcessCd(String processCd) {
    this.processCd = processCd;
  }

  public String getFailureIndc() {
    return failureIndc;
  }

  public void setFailureIndc(String failureIndc) {
    this.failureIndc = failureIndc;
  }

  public String getReject() {
    return reject;
  }

  public void setReject(String reject) {
    this.reject = reject;
  }

  public String getRejReason() {
    return rejReason;
  }

  public void setRejReason(String rejReason) {
    this.rejReason = rejReason;
  }

  public String getCustGrp() {
    return custGrp;
  }

  public void setCustGrp(String custGrp) {
    this.custGrp = custGrp;
  }

  public String getCustSubGrp() {
    return custSubGrp;
  }

  public void setCustSubGrp(String custSubGrp) {
    this.custSubGrp = custSubGrp;
  }

  public String getGeo() {
    return geo;
  }

  public void setGeo(String geo) {
    this.geo = geo;
  }

  public String getMarket() {
    return market;
  }

  public void setMarket(String market) {
    this.market = market;
  }

  public String getProcCenter() {
    return procCenter;
  }

  public void setProcCenter(String procCenter) {
    this.procCenter = procCenter;
  }

  public String getReqStatus() {
    return reqStatus;
  }

  public void setReqStatus(String reqStatus) {
    this.reqStatus = reqStatus;
  }

}
