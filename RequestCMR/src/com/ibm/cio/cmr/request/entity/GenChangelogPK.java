package com.ibm.cio.cmr.request.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * The primary key class for the CHANGELOG database table.
 * 
 * @author
 */
@Embeddable
public class GenChangelogPK implements Serializable {

  private static final long serialVersionUID = 1L;

  private String mandt;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "CHGTS", columnDefinition = "TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP")
  private Date chgts;

  private String tab_nm;

  private String tab_key;

  private String field_nm;

  public String getMandt() {
    return mandt;
  }

  public void setMandt(String mandt) {
    this.mandt = mandt;
  }

  public Date getChgts() {
    return chgts;
  }

  public void setChgts(Date chgts) {
    this.chgts = chgts;
  }

  public String getTab_nm() {
    return tab_nm;
  }

  public void setTab_nm(String tab_nm) {
    this.tab_nm = tab_nm;
  }

  public String getTab_key() {
    return tab_key;
  }

  public void setTab_key(String tab_key) {
    this.tab_key = tab_key;
  }

  public String getField_nm() {
    return field_nm;
  }

  public void setField_nm(String field_nm) {
    this.field_nm = field_nm;
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof GenChangelogPK)) {
      return false;
    }
    GenChangelogPK o = (GenChangelogPK) other;
    return this.mandt.equals(o.mandt) && this.chgts.equals(o.chgts) && this.tab_nm.equals(o.tab_nm) && this.tab_key.equals(o.tab_key)
        && this.field_nm.equals(o.field_nm);

  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int hash = 17;
    hash = hash * prime + (this.mandt != null ? this.mandt.hashCode() : 0);
    hash = hash * prime + (this.chgts != null ? this.chgts.hashCode() : 0);
    hash = hash * prime + (this.tab_nm != null ? this.tab_nm.hashCode() : 0);
    hash = hash * prime + (this.tab_key != null ? this.tab_key.hashCode() : 0);
    hash = hash * prime + (this.field_nm != null ? this.field_nm.hashCode() : 0);

    return hash;
  }

}