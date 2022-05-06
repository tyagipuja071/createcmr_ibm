package com.ibm.cio.cmr.request.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import org.apache.commons.lang3.StringUtils;

@Embeddable
public class USTCRUpdtQueuePK extends BaseEntityPk implements Serializable {

  private static final long serialVersionUID = 1L;

  @Column(name = "MANDT")
  private String mandt;

  @Column(name = "SEQ_NO")
  private long seqNo;

  @Column(name = "TCR_INPUT_FILE_NM")
  private String tcrFileNm;

  public String getMandt() {
    return mandt;
  }

  public void setMandt(String mandt) {
    this.mandt = mandt;
  }

  public long getSeqNo() {
    return this.seqNo;
  }

  public void setSeqNo(long seqNo) {
    this.seqNo = seqNo;
  }

  public String getTcrFileNm() {
    return tcrFileNm;
  }

  public void setTcrFileNm(String tcrFileNm) {
    this.tcrFileNm = tcrFileNm;
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof USTCRUpdtQueuePK)) {
      return false;
    }
    USTCRUpdtQueuePK o = (USTCRUpdtQueuePK) other;
    return this.mandt.equals(o.mandt) && this.seqNo == o.seqNo && this.tcrFileNm.equals(o.tcrFileNm);

  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int hash = 17;
    hash = hash * prime + (this.mandt != null ? this.mandt.hashCode() : 0);
    hash = hash * prime + (this.tcrFileNm != null ? this.tcrFileNm.hashCode() : 0);
    hash = hash * prime + (this.seqNo > 0 ? new java.lang.Long(this.seqNo).hashCode() : 0);

    return hash;
  }

  protected boolean allKeysAssigned() {
    return !StringUtils.isEmpty(this.mandt) && this.seqNo > 0 && !StringUtils.isEmpty(this.tcrFileNm);

  }
}