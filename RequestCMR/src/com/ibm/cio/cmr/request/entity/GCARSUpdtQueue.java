package com.ibm.cio.cmr.request.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

/**
 * The persistent class for the MASS_FTP_QUEUE database table.
 * 
 */
@Entity
@Table(
    name = "MASS_FTP_QUEUE",
    schema = "CREQCMR")
public class GCARSUpdtQueue extends BaseEntity<GCARSUpdtQueuePK> implements Serializable {

  private static final long serialVersionUID = 1L;

  @EmbeddedId
  private GCARSUpdtQueuePK id;

  @Override
  public GCARSUpdtQueuePK getId() {
    return id;
  }

  @Override
  public void setId(GCARSUpdtQueuePK id) {
    this.id = id;
  }

  @Column(
      name = "COD_CONDITION")
  private String codCondition;

  @Transient
  private String fileName;

  public String getFileName() {
    return fileName;
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  @Column(
      name = "COD_RSN")
  private String codRsn;

  @Column(
      name = "CODEFFDATE")
  @Temporal(TemporalType.TIMESTAMP)
  private Date codEffDate;

  @Column(
      name = "PROC_STATUS")
  private String procStatus;

  @Column(
      name = "PROC_MSG")
  private String procMsg;

  @Column(
      name = "CREATED_BY")
  private String createdBy;

  @Column(
      name = "CREATE_DT")
  @Temporal(TemporalType.TIMESTAMP)
  private Date createDt;

  @Column(
      name = "UPDATED_BY")
  private String updatedBy;

  @Column(
      name = "UPDATE_DT")
  @Temporal(TemporalType.TIMESTAMP)
  private Date updateDt;

  @Column(
      name = "KATR10")
  private String katr10;

  public String getCodCondition() {
    return codCondition;
  }

  public void setCodCondition(String codCondition) {
    this.codCondition = codCondition;
  }

  public String getCodRsn() {
    return codRsn;
  }

  public void setCodRsn(String codRsn) {
    this.codRsn = codRsn;
  }

  public Date getCodEffDate() {
    return codEffDate;
  }

  public void setCodEffDate(Date codEffDate) {
    this.codEffDate = codEffDate;
  }

  public String getProcStatus() {
    return procStatus;
  }

  public void setProcStatus(String procStatus) {
    this.procStatus = procStatus;
  }

  public String getProcMsg() {
    return procMsg;
  }

  public void setProcMsg(String procMsg) {
    this.procMsg = procMsg;
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

  public String getKatr10() {
    return katr10;
  }

  public void setKatr10(String katr10) {
    this.katr10 = katr10;
  }
}