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
import javax.persistence.Transient;

/**
 * @author 136786PH1
 *
 */
@Entity
public class ProcessingMonitor {

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

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "TS")
  private Date ts;

  @Column(name = "DIFF_MIN")
  private long diffMin;

  @Column(name = "DIFF_HR")
  private long diffHour;

  @Column(name = "DIFF_DAY")
  private long diffDay;

  @Column(name = "HOST_DOWN")
  private String hostDown;

  @Column(name = "PROCESSED_FLAG")
  private String processedFlag;

  @Column(name = "RDC_PROCESSING_STATUS")
  private String rdcProcessingStatus;

  @Transient
  private String pendingTime;

  @Transient
  private String processBy;

  @Transient
  private String lastUpdated;

  @Transient
  private boolean obsolete;

  @Transient
  private boolean manual;

  @Transient
  private boolean stuck;

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

  public Date getTs() {
    return ts;
  }

  public void setTs(Date ts) {
    this.ts = ts;
  }

  public long getDiffMin() {
    return diffMin;
  }

  public void setDiffMin(long diffMin) {
    this.diffMin = diffMin;
  }

  public long getDiffHour() {
    return diffHour;
  }

  public void setDiffHour(long diffHour) {
    this.diffHour = diffHour;
  }

  public long getDiffDay() {
    return diffDay;
  }

  public void setDiffDay(long diffDay) {
    this.diffDay = diffDay;
  }

  public boolean isObsolete() {
    return obsolete;
  }

  public void setObsolete(boolean obsolete) {
    this.obsolete = obsolete;
  }

  public boolean isStuck() {
    return stuck;
  }

  public void setStuck(boolean stuck) {
    this.stuck = stuck;
  }

  public String getHostDown() {
    return hostDown;
  }

  public void setHostDown(String hostDown) {
    this.hostDown = hostDown;
  }

  public String getPendingTime() {
    return pendingTime;
  }

  public void setPendingTime(String pendingTime) {
    this.pendingTime = pendingTime;
  }

  public String getLastUpdated() {
    return lastUpdated;
  }

  public void setLastUpdated(String lastUpdated) {
    this.lastUpdated = lastUpdated;
  }

  public String getLockBy() {
    return lockBy;
  }

  public void setLockBy(String lockBy) {
    this.lockBy = lockBy;
  }

  public String getProcessBy() {
    return processBy;
  }

  public void setProcessBy(String processBy) {
    this.processBy = processBy;
  }

  public boolean isManual() {
    return manual;
  }

  public void setManual(boolean manual) {
    this.manual = manual;
  }

  public String getProcessedFlag() {
    return processedFlag;
  }

  public void setProcessedFlag(String processedFlag) {
    this.processedFlag = processedFlag;
  }

  public String getRdcProcessingStatus() {
    return rdcProcessingStatus;
  }

  public void setRdcProcessingStatus(String rdcProcessingStatus) {
    this.rdcProcessingStatus = rdcProcessingStatus;
  }

}
