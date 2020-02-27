/**
 * 
 */
package com.ibm.cio.cmr.request.model.system;

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * @author Jeffrey Zamora
 * 
 */
@Embeddable
public class StatsPK {

  @Column(name = "REQ_ID")
  private long reqId;

  public long getReqId() {
    return reqId;
  }

  public void setReqId(long reqId) {
    this.reqId = reqId;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof StatsPK)) {
      return false;
    }
    return this.reqId == ((StatsPK) o).reqId;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int hash = 17;
    hash = hash * prime + (this.reqId > 0 ? new java.lang.Long(this.reqId).hashCode() : 0);

    return hash;
  }
}
