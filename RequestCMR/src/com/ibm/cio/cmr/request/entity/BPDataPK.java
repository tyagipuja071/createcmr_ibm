package com.ibm.cio.cmr.request.entity;

import javax.persistence.Embeddable;

import org.apache.commons.lang3.StringUtils;

@Embeddable
public class BPDataPK {

  private static final long serialVersionUID = 1L;

  private String mandt;

  private String kunnr;

  public String getMandt() {
    return mandt;
  }

  public void setMandt(String mandt) {
    this.mandt = mandt;
  }

  public String getKunnr() {
    return kunnr;
  }

  public void setKunnr(String kunnr) {
    this.kunnr = kunnr;
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof KnvvExtPK)) {
      return false;
    }
    BPDataPK o = (BPDataPK) other;
    return this.mandt.equals(o.mandt) && this.kunnr.equals(o.kunnr);

  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int hash = 17;
    hash = hash * prime + (this.mandt != null ? this.mandt.hashCode() : 0);
    hash = hash * prime + (this.kunnr != null ? this.kunnr.hashCode() : 0);

    return hash;
  }

  @Override
  public String toString() {
    return "BPDataPK [mandt=" + mandt + ", kunnr" + kunnr + "]";
  }

  protected boolean allKeysAssigned() {
    return !StringUtils.isEmpty(this.mandt) && !StringUtils.isEmpty(this.kunnr);

  }
}
