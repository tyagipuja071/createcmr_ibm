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
@Table(name = "US_IBM_ORG", schema = "SAPR3")
public class USIbmOrg extends BaseEntity<USIbmOrgPK> implements Serializable {

  private static final long serialVersionUID = 1L;

  public static long getSerialversionuid() {
    return serialVersionUID;
  }

  @EmbeddedId
  private USIbmOrgPK id;

  @Override
  public USIbmOrgPK getId() {
    return id;
  }

  @Override
  public void setId(USIbmOrgPK id) {
    this.id = id;
  }

  @Column(name = "I_ORG_PRIMRY")
  private String iOrgPrimry;

  @Column(name = "I_ORG_SECNDR")
  private String iOrgSecndr;

  @Column(name = "I_ORG_PRIMRY_ABBV")
  private String iOrgPrimryAbbv;

  @Column(name = "I_ORG_SECNDR_ABBV")
  private String iOrgSecndrAbbv;

  @Column(name = "N_ORG_FULL")
  private String nOrgFull;

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

  public String getiOrgPrimry() {
    return iOrgPrimry;
  }

  public void setiOrgPrimry(String iOrgPrimry) {
    this.iOrgPrimry = iOrgPrimry;
  }

  public String getiOrgSecndr() {
    return iOrgSecndr;
  }

  public void setiOrgSecndr(String iOrgSecndr) {
    this.iOrgSecndr = iOrgSecndr;
  }

  public String getiOrgPrimryAbbv() {
    return iOrgPrimryAbbv;
  }

  public void setiOrgPrimryAbbv(String iOrgPrimryAbbv) {
    this.iOrgPrimryAbbv = iOrgPrimryAbbv;
  }

  public String getiOrgSecndrAbbv() {
    return iOrgSecndrAbbv;
  }

  public void setiOrgSecndrAbbv(String iOrgSecndrAbbv) {
    this.iOrgSecndrAbbv = iOrgSecndrAbbv;
  }

  public String getnOrgFull() {
    return nOrgFull;
  }

  public void setnOrgFull(String nOrgFull) {
    this.nOrgFull = nOrgFull;
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