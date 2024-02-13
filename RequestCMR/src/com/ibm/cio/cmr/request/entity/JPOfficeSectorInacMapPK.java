package com.ibm.cio.cmr.request.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import org.apache.commons.lang3.StringUtils;

/**
 * The primary key class for the JP_OFFICE_SECTOR_INAC_MAPPING database table.
 * 
 * @author XiangBinLiu
 */
@Embeddable
public class JPOfficeSectorInacMapPK extends BaseEntityPk implements Serializable {

  // default serial version id, required for serializable classes.
  private static final long serialVersionUID = 1L;

  @Column(name = "OFFICE_CD")
  private String officeCd;

  @Column(name = "SECTOR_CD")
  private String sectorCd;

  @Column(name = "INAC_CD")
  private String inacCd;

  @Column(name = "AP_CUST_CLUSTER_ID")
  private String apCustClusterId;

  public String getOfficeCd() {
    return officeCd;
  }

  public void setOfficeCd(String officeCd) {
    this.officeCd = officeCd;
  }

  public String getSectorCd() {
    return this.sectorCd;
  }

  public void setSectorCd(String sectorCd) {
    this.sectorCd = sectorCd;
  }

  public String getInacCd() {
    return inacCd;
  }

  public void setInacCd(String inacCd) {
    this.inacCd = inacCd;
  }

  public String getApCustClusterId() {
    return apCustClusterId;
  }

  public void setApCustClusterId(String apCustClusterId) {
    this.apCustClusterId = apCustClusterId;
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof JPOfficeSectorInacMapPK)) {
      return false;
    }
    JPOfficeSectorInacMapPK o = (JPOfficeSectorInacMapPK) other;
    return this.officeCd.equals(o.officeCd) && this.inacCd.equals(o.inacCd) && this.sectorCd.equals(o.sectorCd)
        && this.apCustClusterId.equals(o.apCustClusterId);

  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int hash = 17;

    hash = hash * prime + (this.officeCd != null ? this.officeCd.hashCode() : 0);
    hash = hash * prime + (this.inacCd != null ? this.inacCd.hashCode() : 0);
    hash = hash * prime + (this.sectorCd != null ? this.sectorCd.hashCode() : 0);
    hash = hash * prime + (this.apCustClusterId != null ? this.apCustClusterId.hashCode() : 0);

    return hash;
  }

  @Override
  protected boolean allKeysAssigned() {
    return !StringUtils.isEmpty(this.officeCd) && !StringUtils.isEmpty(this.inacCd) && !StringUtils.isEmpty(this.sectorCd)
        && !StringUtils.isEmpty(this.apCustClusterId);

  }
}