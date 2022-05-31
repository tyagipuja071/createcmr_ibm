package com.ibm.cio.cmr.request.entity;

import java.io.Serializable;

import javax.persistence.Embeddable;

import org.apache.commons.lang3.StringUtils;

@Embeddable
public class USEnterPrisePK implements Serializable {
  private static final long serialVersionUID = 1L;

  private String entNo;

  public String getEntNo() {
    return entNo;
  }

  public void setEntNo(String entNo) {
    this.entNo = entNo;
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof USEnterPrisePK)) {
      return false;
    }
    USEnterPrisePK o = (USEnterPrisePK) other;
    return this.entNo.equals(o.entNo) && this.entNo.equals(o.entNo);

  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int hash = 17;
    hash = hash * prime + (this.entNo != null ? this.entNo.hashCode() : 0);

    return hash;
  }

  @Override
  public String toString() {
    return "USEnterPrisePK [entNo=" + entNo + "]";
  }

  protected boolean allKeysAssigned() {
    return !StringUtils.isEmpty(this.entNo);
  }
}
