package com.ibm.cio.cmr.request.entity;

import javax.persistence.Column;
import javax.persistence.Embeddable;
/**
 * @author GarimaNarang
 * 
 */
@Embeddable
public class DeleteReactivatePK extends BaseEntityPk{
  
  @Column(name = "PAR_REQ_ID")
  private long parReqId;

  @Column(name = "SEQ_NO")
  private int seqNo;

  @Column(name = "ITERATION_ID")
  private int iterationId;

  public long getParReqId() {
    return parReqId;
  }

  public void setParReqId(long parReqId) {
    this.parReqId = parReqId;
  }

  public int getSeqNo() {
    return seqNo;
  }


  public void setSeqNo(int seqNo) {
    this.seqNo = seqNo;
  }


  public int getIterationId() {
    return iterationId;
  }


  public void setIterationId(int iterationId) {
    this.iterationId = iterationId;
  }


  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof DeleteReactivatePK)) {
      return false;
    }
    DeleteReactivatePK o = (DeleteReactivatePK) other;
    return this.parReqId == o.parReqId && this.seqNo == o.seqNo && this.iterationId == o.iterationId;

  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int hash = 17;
    hash = hash * prime + (this.parReqId > 0 ? new java.lang.Long(this.parReqId).hashCode() : 0);
    hash = hash * prime + this.seqNo;
    hash = hash * prime + this.iterationId;

    return hash;
  }

  @Override
  protected boolean allKeysAssigned() {
    return this.parReqId > 0 && this.seqNo > 0 && this.iterationId > 0;

  }

}
