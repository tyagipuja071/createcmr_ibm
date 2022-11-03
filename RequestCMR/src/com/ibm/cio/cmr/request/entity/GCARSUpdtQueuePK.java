package com.ibm.cio.cmr.request.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import org.apache.commons.lang3.StringUtils;

@Embeddable
public class GCARSUpdtQueuePK extends BaseEntityPk implements Serializable {

  private static final long serialVersionUID = 1L;

  @Column(
      name = "SOURCE_NAME")
  private String sourceName;

  @Column(
      name = "SEQ_NO")
  private long seqNo;

  @Column(
      name = "CMR_ISSUING_CNTRY")
  private String cmrIssuingCntry;

  @Column(
      name = "CMR_NO")
  private String cmrNo;

  public String getSourceName() {
    return sourceName;
  }

  public void setSourceName(String sourceName) {
    this.sourceName = sourceName;
  }

  public long getSeqNo() {
    return seqNo;
  }

  public void setSeqNo(long seqNo) {
    this.seqNo = seqNo;
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

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 17;
    result = prime * result + (this.sourceName != null ? this.sourceName.hashCode() : 0);
    result = prime * result + (this.seqNo > 0 ? new java.lang.Long(this.seqNo).hashCode() : 0);
    result = prime * result + (this.cmrIssuingCntry != null ? this.cmrIssuingCntry.hashCode() : 0);
    result = prime * result + (this.cmrNo != null ? this.cmrNo.hashCode() : 0);
    return result;
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof GCARSUpdtQueuePK)) {
      return false;
    }
    GCARSUpdtQueuePK o = (GCARSUpdtQueuePK) other;
    return this.sourceName.equals(o.sourceName) && this.seqNo == o.seqNo && this.cmrIssuingCntry.equals(o.cmrIssuingCntry)
        && this.cmrNo.equals(o.cmrNo);
  }

  @Override
  protected boolean allKeysAssigned() {
    return !StringUtils.isEmpty(this.sourceName) && this.seqNo > 0 && !StringUtils.isEmpty(this.cmrIssuingCntry) && !StringUtils.isEmpty(this.cmrNo);
  }
}