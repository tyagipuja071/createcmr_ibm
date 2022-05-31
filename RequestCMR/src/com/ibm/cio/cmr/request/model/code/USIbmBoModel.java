package com.ibm.cio.cmr.request.model.code;

import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.cio.cmr.request.model.BaseModel;

public class USIbmBoModel extends BaseModel {

  private static final long serialVersionUID = 1L;

  private String mandt;

  private String iOff;

  private String aLevel1Value;

  private String aLevel2Value;

  private String aLevel3Value;

  private String aLevel4Value;

  private String nOff;

  private String fDistrcOn;

  private String iArOff;

  private String fApplicCash;

  private String fApplicColl;

  private String fOffFunc;

  private String qTieLineTelOff;

  private String tInqAddrLine1;

  private String tInqAddrLine2;

  private String nInqCity;

  private String nInqSt;

  private String cInqZip;

  private String cInqCnty;

  private String nInqScc;

  private String tRemitToAddrL1;

  private String tRemitToAddrL2;

  private String nRemitToCity;

  private String nRemitToSt;

  private String cRemitToZip;

  private String cRemitToCnty;

  private String nRemitToScc;

  private String tPhysicAddrLn1;

  private String tPhysicAddrLn2;

  private String nPhysicCity;

  private String nPhysicSt;

  private String cPhysicZip;

  private String cPhysicCnty;

  private String nPhysicScc;

  private String iCtrlgOff;

  private String createdBy;

  private Date createDt;

  private String updatedBy;

  private Date updateDt;

  private String updateType;

  private String createdTsStr;

  private String updatedTsStr;

  public String getMandt() {
    return mandt;
  }

  public void setMandt(String mandt) {
    this.mandt = mandt;
  }

  public String getiOff() {
    return iOff;
  }

  public void setiOff(String iOff) {
    this.iOff = iOff;
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

  public String getCreatedTsStr() {
    return createdTsStr;
  }

  public void setCreatedTsStr(String createdTsStr) {
    this.createdTsStr = createdTsStr;
  }

  public String getUpdatedTsStr() {
    return updatedTsStr;
  }

  public void setUpdatedTsStr(String updatedTsStr) {
    this.updatedTsStr = updatedTsStr;
  }

  @Override
  public boolean allKeysAssigned() {
    boolean blnAllKeys = false;
    blnAllKeys = !StringUtils.isEmpty(this.mandt) && !StringUtils.isEmpty(this.iOff);

    return blnAllKeys;
  }

  @Override
  public String getRecordDescription() {
    return "US IBM BO Entry";
  }

  @Override
  public void addKeyParameters(ModelAndView mv) {
  }

  @Override
  public void addKeyParameters(ModelMap map) {
  }

}
