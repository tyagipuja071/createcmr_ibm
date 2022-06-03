package com.ibm.cio.cmr.request.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import org.apache.commons.lang3.StringUtils;

@Embeddable
public class USBusinessPartnerMasterPK extends BaseEntityPk implements Serializable {

  private static final long serialVersionUID = 1L;

  @Column(name = "MANDT")
  private String mandt;

  @Column(name = "COMPANY_NO")
  private String companyNo;

  public String getMandt() {
    return mandt;
  }

  public void setMandt(String mandt) {
    this.mandt = mandt;
  }

  public String getCompanyNo() {
    return companyNo;
  }

  public void setCompanyNo(String companyNo) {
    this.companyNo = companyNo;
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof USBusinessPartnerMasterPK)) {
      return false;
    }

    USBusinessPartnerMasterPK o = (USBusinessPartnerMasterPK) other;
    return this.mandt.equals(o.mandt) && this.companyNo.equals(o.companyNo);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int hash = 17;
    hash = hash * prime + (this.mandt != null ? this.mandt.hashCode() : 0);
    hash = hash * prime + (this.companyNo != null ? this.companyNo.hashCode() : 0);

    return hash;
  }

  @Override
  protected boolean allKeysAssigned() {
    return !StringUtils.isEmpty(this.mandt) && !StringUtils.isEmpty(this.companyNo);
  }
}