package com.ibm.cio.cmr.request.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import org.apache.commons.lang3.StringUtils;

@Embeddable
public class USIbmOrgPK extends BaseEntityPk implements Serializable {

  private static final long serialVersionUID = 1L;

  @Column(name = "MANDT")
  private String mandt;

  @Column(name = "A_LEVEL_1_VALUE")
  private String aLevel1Value;

  @Column(name = "A_LEVEL_2_VALUE")
  private String aLevel2Value;

  @Column(name = "A_LEVEL_3_VALUE")
  private String aLevel3Value;

  @Column(name = "A_LEVEL_4_VALUE")
  private String aLevel4Value;

  public String getMandt() {
    return mandt;
  }

  public void setMandt(String mandt) {
    this.mandt = mandt;
  }

  public String getaLevel1Value() {
    return aLevel1Value;
  }

  public void setaLevel1Value(String aLevel1Value) {
    this.aLevel1Value = aLevel1Value;
  }

  public String getaLevel2Value() {
    return aLevel2Value;
  }

  public void setaLevel2Value(String aLevel2Value) {
    this.aLevel2Value = aLevel2Value;
  }

  public String getaLevel3Value() {
    return aLevel3Value;
  }

  public void setaLevel3Value(String aLevel3Value) {
    this.aLevel3Value = aLevel3Value;
  }

  public String getaLevel4Value() {
    return aLevel4Value;
  }

  public void setaLevel4Value(String aLevel4Value) {
    this.aLevel4Value = aLevel4Value;
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }

    if (!(other instanceof USIbmOrgPK)) {
      return false;
    }

    USIbmOrgPK o = (USIbmOrgPK) other;
    return this.mandt.equals(o.mandt) && this.aLevel1Value.equals(o.aLevel1Value) && this.aLevel2Value.equals(o.aLevel2Value)
        && this.aLevel3Value.equals(o.aLevel3Value) && this.aLevel4Value.equals(o.aLevel4Value);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int hash = 17;
    hash = hash * prime + (this.mandt != null ? this.mandt.hashCode() : 0);
    hash = hash * prime + (this.aLevel1Value != null ? this.aLevel1Value.hashCode() : 0);
    hash = hash * prime + (this.aLevel2Value != null ? this.aLevel2Value.hashCode() : 0);
    hash = hash * prime + (this.aLevel3Value != null ? this.aLevel3Value.hashCode() : 0);
    hash = hash * prime + (this.aLevel4Value != null ? this.aLevel4Value.hashCode() : 0);

    return hash;
  }

  @Override
  protected boolean allKeysAssigned() {
    return !StringUtils.isEmpty(this.mandt) && !StringUtils.isEmpty(this.aLevel1Value) && !StringUtils.isEmpty(this.aLevel2Value)
        && !StringUtils.isEmpty(this.aLevel3Value) && !StringUtils.isEmpty(this.aLevel4Value);
  }
}