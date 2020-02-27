/**
 * 
 */
package com.ibm.cio.cmr.request.entity;

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * @author JeffZAMORA
 * 
 */
@Embeddable
public class MyApprovalsPK extends BaseEntityPk {

  @Column(
      name = "REQ_ID")
  private long reqId;

  @Column(
      name = "APPROVAL_ID")
  private long approvalId;

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof MyApprovalsPK)) {
      return false;
    }
    MyApprovalsPK o = (MyApprovalsPK) other;
    return this.approvalId == o.approvalId && this.reqId == o.reqId;

  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int hash = 17;
    hash = hash * prime + (this.approvalId > 0 ? new java.lang.Long(this.approvalId).hashCode() : 0);
    hash = hash * prime + (this.reqId > 0 ? new java.lang.Long(this.reqId).hashCode() : 0);

    return hash;
  }

  public long getReqId() {
    return reqId;
  }

  public void setReqId(long reqId) {
    this.reqId = reqId;
  }

  public long getApprovalId() {
    return approvalId;
  }

  public void setApprovalId(long approvalId) {
    this.approvalId = approvalId;
  }

  @Override
  protected boolean allKeysAssigned() {
    return this.approvalId > 0 && this.reqId > 0;
  }
}
