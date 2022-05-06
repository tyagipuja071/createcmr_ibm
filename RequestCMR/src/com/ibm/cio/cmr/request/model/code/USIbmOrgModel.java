package com.ibm.cio.cmr.request.model.code;

import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.cio.cmr.request.model.BaseModel;

public class USIbmOrgModel extends BaseModel {

  private static final long serialVersionUID = 1L;

  private String mandt;

  private String aLevel1Value;

  private String aLevel2Value;

  private String aLevel3Value;

  private String aLevel4Value;

  private String iOrgPrimry;

  private String iOrgSecndr;

  private String iOrgPrimryAbbv;

  private String iOrgSecndrAbbv;

  private String nOrgFull;

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
    blnAllKeys = !StringUtils.isEmpty(this.mandt) && !StringUtils.isEmpty(this.aLevel1Value) && !StringUtils.isEmpty(this.aLevel2Value)
        && !StringUtils.isEmpty(this.aLevel3Value) && !StringUtils.isEmpty(this.aLevel4Value);

    return blnAllKeys;
  }

  @Override
  public String getRecordDescription() {
    return "US IBM ORG Entry";
  }

  @Override
  public void addKeyParameters(ModelAndView mv) {
  }

  @Override
  public void addKeyParameters(ModelMap map) {
  }

}
