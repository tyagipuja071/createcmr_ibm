/**
 * 
 */
package com.ibm.cio.cmr.request.entity;

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * @author Bin
 * 
 */
@Embeddable
public class SalesPaymentPK {

  @Column(name = "MANDT")
  private String mandt;
  @Column(name = "KUNNR")
  private String kunnr;

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof SalesPaymentPK)) {
      return false;
    }
    SalesPaymentPK o = (SalesPaymentPK) other;
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

}
