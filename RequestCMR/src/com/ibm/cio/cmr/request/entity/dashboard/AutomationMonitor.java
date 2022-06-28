/**
 * 
 */
package com.ibm.cio.cmr.request.entity.dashboard;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * @author 136786PH1
 *
 */
@Entity
public class AutomationMonitor {

  @Id
  @Column(name = "REQ_ID")
  private long reqId;

  @Column(name = "LOCK_BY")
  private String lockBy;

  @Column(name = "REQ_STATUS")
  private String reqStatus;

  @Column(name = "REQ_TYPE")
  private String reqType;

  @Column(name = "CMR_ISSUING_CNTRY")
  private String cmrIssuingCntry;

  @Column(name = "CNTRY_NM")
  private String cntryNm;

  @Column(name = "CUST_NM")
  private String custNm;

  @Column(name = "SOURCE_SYST_ID")
  private String sourceSystId;

  @Column(name = "PROCESSING_TYP")
  private String processingTyp;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "LAST_UPDT_TS")
  private Date lastUpdtTs;

  @Column(name = "DIFF_MIN")
  private long diffMin;

  @Column(name = "APR_MIN")
  private long aprMin;

  @Column(name = "MANUAL")
  private String manual;

  @Column(name = "HOST_DOWN")
  private String hostDown;

  public long getReqId() {
    return reqId;
  }

  public void setReqId(long reqId) {
    this.reqId = reqId;
  }

  public String getReqStatus() {
    return reqStatus;
  }

  public void setReqStatus(String reqStatus) {
    this.reqStatus = reqStatus;
  }

  public String getReqType() {
    return reqType;
  }

  public void setReqType(String reqType) {
    this.reqType = reqType;
  }

  public String getCmrIssuingCntry() {
    return cmrIssuingCntry;
  }

  public void setCmrIssuingCntry(String cmrIssuingCntry) {
    this.cmrIssuingCntry = cmrIssuingCntry;
  }

  public String getCntryNm() {
    return cntryNm;
  }

  public void setCntryNm(String cntryNm) {
    this.cntryNm = cntryNm;
  }

  public String getCustNm() {
    return custNm;
  }

  public void setCustNm(String custNm) {
    this.custNm = custNm;
  }

  public String getSourceSystId() {
    return sourceSystId;
  }

  public void setSourceSystId(String sourceSystId) {
    this.sourceSystId = sourceSystId;
  }

  public String getProcessingTyp() {
    return processingTyp;
  }

  public void setProcessingTyp(String processingTyp) {
    this.processingTyp = processingTyp;
  }

  public Date getLastUpdtTs() {
    return lastUpdtTs;
  }

  public void setLastUpdtTs(Date lastUpdtTs) {
    this.lastUpdtTs = lastUpdtTs;
  }

  public long getDiffMin() {
    return diffMin;
  }

  public void setDiffMin(long diffMin) {
    this.diffMin = diffMin;
  }

  public long getAprMin() {
    return aprMin;
  }

  public void setAprMin(long aprMin) {
    this.aprMin = aprMin;
  }

  public String getManual() {
    return manual;
  }

  public void setManual(String manual) {
    this.manual = manual;
  }

  public String getHostDown() {
    return hostDown;
  }

  public void setHostDown(String hostDown) {
    this.hostDown = hostDown;
  }

  public String getLockBy() {
    return lockBy;
  }

  public void setLockBy(String lockBy) {
    this.lockBy = lockBy;
  }

}
