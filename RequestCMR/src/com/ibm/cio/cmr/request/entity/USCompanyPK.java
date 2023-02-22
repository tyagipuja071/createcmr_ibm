package com.ibm.cio.cmr.request.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import org.apache.commons.lang3.StringUtils;

@Embeddable
public class USCompanyPK extends BaseEntityPk implements Serializable {
  private static final long serialVersionUID = 1L;

  @Column(name = "COMP_NO")
  private String compNo;

  @Column(name = "MANDT")
  private String mandt;

  public String getCompNo() {
    return compNo;
  }

  public void setCompNo(String compNo) {
    this.compNo = compNo;
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
    USCompanyPK o = (USCompanyPK) other;
    return this.mandt.equals(o.mandt) && this.compNo.equals(o.compNo);

  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int hash = 17;
    hash = hash * prime + (this.mandt != null ? this.mandt.hashCode() : 0);
    hash = hash * prime + (this.compNo != null ? this.compNo.hashCode() : 0);

    return hash;
  }

  @Override
  public String toString() {
    return "USCompanyPK [compNo=" + compNo + "]";
  }

  protected boolean allKeysAssigned() {
    return !StringUtils.isEmpty(this.mandt) && !StringUtils.isEmpty(this.compNo);
  }
}
