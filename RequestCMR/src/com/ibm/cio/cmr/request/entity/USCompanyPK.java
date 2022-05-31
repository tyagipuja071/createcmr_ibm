package com.ibm.cio.cmr.request.entity;

import java.io.Serializable;

import javax.persistence.Embeddable;

import org.apache.commons.lang3.StringUtils;

@Embeddable
public class USCompanyPK implements Serializable {
  private static final long serialVersionUID = 1L;

  private String compNo;

  public String getCompNo() {
    return compNo;
  }

  public void setCompNo(String compNo) {
    this.compNo = compNo;
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
    return this.compNo.equals(o.compNo) && this.compNo.equals(o.compNo);

  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int hash = 17;
    hash = hash * prime + (this.compNo != null ? this.compNo.hashCode() : 0);

    return hash;
  }

  @Override
  public String toString() {
    return "USCompanyPK [compNo=" + compNo + "]";
  }

  protected boolean allKeysAssigned() {
    return !StringUtils.isEmpty(this.compNo);
  }
}
