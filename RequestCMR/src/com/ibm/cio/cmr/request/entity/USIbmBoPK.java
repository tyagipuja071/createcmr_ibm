package com.ibm.cio.cmr.request.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import org.apache.commons.lang3.StringUtils;

@Embeddable
public class USIbmBoPK extends BaseEntityPk implements Serializable {

  private static final long serialVersionUID = 1L;

  @Column(name = "MANDT")
  private String mandt;

  @Column(name = "I_OFF")
  private String iOff;

  public String getMandt() {
    return mandt;
  }

  public void setMandt(String mandt) {
    this.mandt = mandt;
  }

  public String getiOff() {
    return iOff;
  }

  public void setiOff(String iOff) {
    this.iOff = iOff;
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }

    if (!(other instanceof USIbmBoPK)) {
      return false;
    }

    USIbmBoPK o = (USIbmBoPK) other;
    return this.mandt.equals(o.mandt) && this.iOff.equals(o.iOff);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int hash = 17;
    hash = hash * prime + (this.mandt != null ? this.mandt.hashCode() : 0);
    hash = hash * prime + (this.iOff != null ? this.iOff.hashCode() : 0);

    return hash;
  }

  @Override
  protected boolean allKeysAssigned() {
    return !StringUtils.isEmpty(this.mandt) && !StringUtils.isEmpty(this.iOff);
  }

}