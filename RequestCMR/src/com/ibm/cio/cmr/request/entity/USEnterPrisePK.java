package com.ibm.cio.cmr.request.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import org.apache.commons.lang3.StringUtils;

@Embeddable
public class USEnterPrisePK extends BaseEntityPk implements Serializable {
  private static final long serialVersionUID = 1L;

  @Column(
      name = "ENT_NO")
  private String entNo;

  @Column(
      name = "MANDT")
  private String mandt;

  public String getEntNo() {
    return entNo;
  }

  public void setEntNo(String entNo) {
    this.entNo = entNo;
  }

  public String getMandt() {
    return mandt;
  }

  public void setMandt(String mandt) {
    this.mandt = mandt;
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
    return this.mandt.equals(o.mandt) && this.entNo.equals(o.entNo);

  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int hash = 17;
    hash = hash * prime + (this.mandt != null ? this.mandt.hashCode() : 0);
    hash = hash * prime + (this.entNo != null ? this.entNo.hashCode() : 0);

    return hash;
  }

  @Override
  public String toString() {
    return "USEnterPrisePK [entNo=" + entNo + "]";
  }

  protected boolean allKeysAssigned() {
    return !StringUtils.isEmpty(this.mandt) && !StringUtils.isEmpty(this.entNo);
  }
}
