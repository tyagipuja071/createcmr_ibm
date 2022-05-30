package com.ibm.cio.cmr.request.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(
    name = "BP_DATA",
    schema = "SAPR3")

public class BPData implements Serializable {
  private static final long serialVersionUID = 1L;

  @EmbeddedId
  private BPDataPK id;

  @Column(
      name = "BP_ABBREV_NM")
  private String bpAbbrevName;
  @Column(
      name = "BP_ACCOUNT_TYPE")
  private String bpAccountType;

  @Column(
      name = "LOEVM")
  private String loevm;

  @Column(
      name = "CREATED_BY")
  private String createBy;

  @Column(
      name = "CREATE_DT")
  @Temporal(TemporalType.TIMESTAMP)
  private Date createDt;

  @Column(
      name = "UPDATED_BY")
  private String updateBy;

  @Column(
      name = "UPDATE_DT")
  @Temporal(TemporalType.TIMESTAMP)
  private Date updateDt;

  @Column(
      name = "UPDATE_TYPE")
  private String updateType;

  public BPDataPK getId() {
    return id;
  }

  public void setId(BPDataPK id) {
    this.id = id;
  }

  public String getBpAbbrevName() {
    return bpAbbrevName;
  }

  public void setBpAbbrevName(String bpAbbrevName) {
    this.bpAbbrevName = bpAbbrevName;
  }

  public String getBpAccountType() {
    return bpAccountType;
  }

  public void setBpAccountType(String bpAccountType) {
    this.bpAccountType = bpAccountType;
  }

  public String getLoevm() {
    return loevm;
  }

  public void setLoevm(String loevm) {
    this.loevm = loevm;
  }

  public String getCreateBy() {
    return createBy;
  }

  public void setCreateBy(String createBy) {
    this.createBy = createBy;
  }

  public Date getCreateDt() {
    return createDt;
  }

  public void setCreateDt(Date createDt) {
    this.createDt = createDt;
  }

  public String getUpdateBy() {
    return updateBy;
  }

  public void setUpdateBy(String updateBy) {
    this.updateBy = updateBy;
  }

  public Date getUpdateDt() {
    return updateDt;
  }

  public void setUpdateDt(Date updateDt) {
    this.updateDt = updateDt;
  }

  public String getUpdateType() {
    return updateType;
  }

  public void setUpdateType(String updateType) {
    this.updateType = updateType;
  }

}
