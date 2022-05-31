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
@Table(name = "US_BP_MASTER", schema = "SAPR3")
public class USBusinessPartnerMaster extends BaseEntity<USBusinessPartnerMasterPK> implements Serializable {

  private static final long serialVersionUID = 1L;

  public static long getSerialversionuid() {
    return serialVersionUID;
  }

  @EmbeddedId
  private USBusinessPartnerMasterPK id;

  @Override
  public USBusinessPartnerMasterPK getId() {
    return id;
  }

  @Override
  public void setId(USBusinessPartnerMasterPK id) {
    this.id = id;
  }

  @Column(name = "CMR_NO")
  private String cmrNo;

  @Column(name = "KATR10")
  private String katr10;

  @Column(name = "LOEVM")
  private String loevm;

  @Column(name = "CREATED_BY")
  private String createdBy;

  @Column(name = "CREATE_DT")
  @Temporal(TemporalType.TIMESTAMP)
  private Date createDt;

  @Column(name = "UPDATED_BY")
  private String updatedBy;

  @Column(name = "UPDATE_DT")
  @Temporal(TemporalType.TIMESTAMP)
  private Date updateDt;

  @Column(name = "UPDATE_TYPE")
  private String updateType;

  public String getCmrNo() {
    return cmrNo;
  }

  public void setCmrNo(String cmrNo) {
    this.cmrNo = cmrNo;
  }

  public String getKatr10() {
    return katr10;
  }

  public void setKatr10(String katr10) {
    this.katr10 = katr10;
  }

  public String getLoevm() {
    return loevm;
  }

  public void setLoevm(String loevm) {
    this.loevm = loevm;
  }

  public String getCreatedBy() {
    return createdBy;
  }

  public void setCreatedBy(String createdBy) {
    this.createdBy = createdBy;
  }

  public Date getCreateDt() {
    return createDt;
  }

  public void setCreateDt(Date createDt) {
    this.createDt = createDt;
  }

  public String getUpdatedBy() {
    return updatedBy;
  }

  public void setUpdatedBy(String updatedBy) {
    this.updatedBy = updatedBy;
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