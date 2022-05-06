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
@Table(name = "US_IBM_BO", schema = "SAPR3")
public class USIbmBo extends BaseEntity<USIbmBoPK> implements Serializable {

  private static final long serialVersionUID = 1L;

  public static long getSerialversionuid() {
    return serialVersionUID;
  }

  @EmbeddedId
  private USIbmBoPK id;

  @Override
  public USIbmBoPK getId() {
    return id;
  }

  @Override
  public void setId(USIbmBoPK id) {
    this.id = id;
  }

  @Column(name = "A_LEVEL_1_VALUE")
  private String aLevel1Value;

  @Column(name = "A_LEVEL_2_VALUE")
  private String aLevel2Value;

  @Column(name = "A_LEVEL_3_VALUE")
  private String aLevel3Value;

  @Column(name = "A_LEVEL_4_VALUE")
  private String aLevel4Value;

  @Column(name = "N_OFF")
  private String nOff;

  @Column(name = "F_DISTRC_ON")
  private String fDistrcOn;

  @Column(name = "I_AR_OFF")
  private String iArOff;

  @Column(name = "F_APPLIC_CASH")
  private String fApplicCash;

  @Column(name = "F_APPLIC_COLL")
  private String fApplicColl;

  @Column(name = "F_OFF_FUNC")
  private String fOffFunc;

  @Column(name = "Q_TIE_LINE_TEL_OFF")
  private String qTieLineTelOff;

  @Column(name = "T_INQ_ADDR_LINE_1")
  private String tInqAddrLine1;

  @Column(name = "T_INQ_ADDR_LINE_2")
  private String tInqAddrLine2;

  @Column(name = "N_INQ_CITY")
  private String nInqCity;

  @Column(name = "N_INQ_ST")
  private String nInqSt;

  @Column(name = "C_INQ_ZIP")
  private String cInqZip;

  @Column(name = "C_INQ_CNTY")
  private String cInqCnty;

  @Column(name = "N_INQ_SCC")
  private String nInqScc;

  @Column(name = "T_REMIT_TO_ADDR_L1")
  private String tRemitToAddrL1;

  @Column(name = "T_REMIT_TO_ADDR_L2")
  private String tRemitToAddrL2;

  @Column(name = "N_REMIT_TO_CITY")
  private String nRemitToCity;

  @Column(name = "N_REMIT_TO_ST")
  private String nRemitToSt;

  @Column(name = "C_REMIT_TO_ZIP")
  private String cRemitToZip;

  @Column(name = "C_REMIT_TO_CNTY")
  private String cRemitToCnty;

  @Column(name = "N_REMIT_TO_SCC")
  private String nRemitToScc;

  @Column(name = "T_PHYSIC_ADDR_LN1")
  private String tPhysicAddrLn1;

  @Column(name = "T_PHYSIC_ADDR_LN2")
  private String tPhysicAddrLn2;

  @Column(name = "N_PHYSIC_CITY")
  private String nPhysicCity;

  @Column(name = "N_PHYSIC_ST")
  private String nPhysicSt;

  @Column(name = "C_PHYSIC_ZIP")
  private String cPhysicZip;

  @Column(name = "C_PHYSIC_CNTY")
  private String cPhysicCnty;

  @Column(name = "N_PHYSIC_SCC")
  private String nPhysicScc;

  @Column(name = "I_CTRLG_OFF")
  private String iCtrlgOff;

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

  public String getnOff() {
    return nOff;
  }

  public void setnOff(String nOff) {
    this.nOff = nOff;
  }

  public String getfDistrcOn() {
    return fDistrcOn;
  }

  public void setfDistrcOn(String fDistrcOn) {
    this.fDistrcOn = fDistrcOn;
  }

  public String getiArOff() {
    return iArOff;
  }

  public void setiArOff(String iArOff) {
    this.iArOff = iArOff;
  }

  public String getfApplicCash() {
    return fApplicCash;
  }

  public void setfApplicCash(String fApplicCash) {
    this.fApplicCash = fApplicCash;
  }

  public String getfApplicColl() {
    return fApplicColl;
  }

  public void setfApplicColl(String fApplicColl) {
    this.fApplicColl = fApplicColl;
  }

  public String getfOffFunc() {
    return fOffFunc;
  }

  public void setfOffFunc(String fOffFunc) {
    this.fOffFunc = fOffFunc;
  }

  public String getqTieLineTelOff() {
    return qTieLineTelOff;
  }

  public void setqTieLineTelOff(String qTieLineTelOff) {
    this.qTieLineTelOff = qTieLineTelOff;
  }

  public String gettInqAddrLine1() {
    return tInqAddrLine1;
  }

  public void settInqAddrLine1(String tInqAddrLine1) {
    this.tInqAddrLine1 = tInqAddrLine1;
  }

  public String gettInqAddrLine2() {
    return tInqAddrLine2;
  }

  public void settInqAddrLine2(String tInqAddrLine2) {
    this.tInqAddrLine2 = tInqAddrLine2;
  }

  public String getnInqCity() {
    return nInqCity;
  }

  public void setnInqCity(String nInqCity) {
    this.nInqCity = nInqCity;
  }

  public String getnInqSt() {
    return nInqSt;
  }

  public void setnInqSt(String nInqSt) {
    this.nInqSt = nInqSt;
  }

  public String getcInqZip() {
    return cInqZip;
  }

  public void setcInqZip(String cInqZip) {
    this.cInqZip = cInqZip;
  }

  public String getcInqCnty() {
    return cInqCnty;
  }

  public void setcInqCnty(String cInqCnty) {
    this.cInqCnty = cInqCnty;
  }

  public String getnInqScc() {
    return nInqScc;
  }

  public void setnInqScc(String nInqScc) {
    this.nInqScc = nInqScc;
  }

  public String gettRemitToAddrL1() {
    return tRemitToAddrL1;
  }

  public void settRemitToAddrL1(String tRemitToAddrL1) {
    this.tRemitToAddrL1 = tRemitToAddrL1;
  }

  public String gettRemitToAddrL2() {
    return tRemitToAddrL2;
  }

  public void settRemitToAddrL2(String tRemitToAddrL2) {
    this.tRemitToAddrL2 = tRemitToAddrL2;
  }

  public String getnRemitToCity() {
    return nRemitToCity;
  }

  public void setnRemitToCity(String nRemitToCity) {
    this.nRemitToCity = nRemitToCity;
  }

  public String getnRemitToSt() {
    return nRemitToSt;
  }

  public void setnRemitToSt(String nRemitToSt) {
    this.nRemitToSt = nRemitToSt;
  }

  public String getcRemitToZip() {
    return cRemitToZip;
  }

  public void setcRemitToZip(String cRemitToZip) {
    this.cRemitToZip = cRemitToZip;
  }

  public String getcRemitToCnty() {
    return cRemitToCnty;
  }

  public void setcRemitToCnty(String cRemitToCnty) {
    this.cRemitToCnty = cRemitToCnty;
  }

  public String getnRemitToScc() {
    return nRemitToScc;
  }

  public void setnRemitToScc(String nRemitToScc) {
    this.nRemitToScc = nRemitToScc;
  }

  public String gettPhysicAddrLn1() {
    return tPhysicAddrLn1;
  }

  public void settPhysicAddrLn1(String tPhysicAddrLn1) {
    this.tPhysicAddrLn1 = tPhysicAddrLn1;
  }

  public String gettPhysicAddrLn2() {
    return tPhysicAddrLn2;
  }

  public void settPhysicAddrLn2(String tPhysicAddrLn2) {
    this.tPhysicAddrLn2 = tPhysicAddrLn2;
  }

  public String getnPhysicCity() {
    return nPhysicCity;
  }

  public void setnPhysicCity(String nPhysicCity) {
    this.nPhysicCity = nPhysicCity;
  }

  public String getnPhysicSt() {
    return nPhysicSt;
  }

  public void setnPhysicSt(String nPhysicSt) {
    this.nPhysicSt = nPhysicSt;
  }

  public String getcPhysicZip() {
    return cPhysicZip;
  }

  public void setcPhysicZip(String cPhysicZip) {
    this.cPhysicZip = cPhysicZip;
  }

  public String getcPhysicCnty() {
    return cPhysicCnty;
  }

  public void setcPhysicCnty(String cPhysicCnty) {
    this.cPhysicCnty = cPhysicCnty;
  }

  public String getnPhysicScc() {
    return nPhysicScc;
  }

  public void setnPhysicScc(String nPhysicScc) {
    this.nPhysicScc = nPhysicScc;
  }

  public String getiCtrlgOff() {
    return iCtrlgOff;
  }

  public void setiCtrlgOff(String iCtrlgOff) {
    this.iCtrlgOff = iCtrlgOff;
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