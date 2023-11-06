
package com.ibm.cio.cmr.request.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * The persistent class for the JP_OFFICE_SECTOR_INAC_MAPPING database table.
 * 
 * @author XiangBinLiu
 */
@Entity
@Table(name = "JP_OFFICE_SECTOR_INAC_MAPPING", schema = "CREQCMR")
public class JPOfficeSectorInacMap extends BaseEntity<JPOfficeSectorInacMapPK> implements Serializable {
  private static final long serialVersionUID = 1L;

  @EmbeddedId
  private JPOfficeSectorInacMapPK id;

  @Override
  public JPOfficeSectorInacMapPK getId() {
    return id;
  }

  @Override
  public void setId(JPOfficeSectorInacMapPK id) {
    this.id = id;
  }

  @Column(name = "CREATE_TS")
  @Temporal(TemporalType.TIMESTAMP)
  private Date createTs;

  @Column(name = "CREATE_BY")
  private String createBy;

  @Column(name = "UPDT_TS")
  @Temporal(TemporalType.TIMESTAMP)
  private Date updateTs;

  @Column(name = "UPDT_BY")
  private String UpdateBy;

  public Date getCreateTs() {
    return this.createTs;
  }

  public void setCreateTs(Date createTs) {
    this.createTs = createTs;
  }

  public String getCreateBy() {
    return this.createBy;
  }

  public void setCreateBy(String createBy) {
    this.createBy = createBy;
  }

  public Date getUpdateTs() {
    return this.updateTs;
  }

  public void setUpdateTs(Date updateTs) {
    this.updateTs = updateTs;
  }

  public String getUpdateBy() {
    return this.UpdateBy;
  }

  public void setUpdateBy(String UpdateBy) {
    this.UpdateBy = UpdateBy;
  }

}