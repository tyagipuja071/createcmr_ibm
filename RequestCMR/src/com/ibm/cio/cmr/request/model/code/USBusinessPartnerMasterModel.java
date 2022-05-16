package com.ibm.cio.cmr.request.model.code;

import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.cio.cmr.request.model.BaseModel;

public class USBusinessPartnerMasterModel extends BaseModel {

  private static final long serialVersionUID = 1L;

  private String mandt;

  private String companyNo;

  private String cmrNo;

  private String katr10;

  private String loevm;

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

  public String getCompanyNo() {
    return companyNo;
  }

  public void setCompanyNo(String companyNo) {
    this.companyNo = companyNo;
  }

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
    blnAllKeys = !StringUtils.isEmpty(this.mandt) && !StringUtils.isEmpty(this.companyNo);

    return blnAllKeys;
  }

  @Override
  public String getRecordDescription() {
    return "US Business Partner Master Entry";
  }

  @Override
  public void addKeyParameters(ModelAndView mv) {
  }

  @Override
  public void addKeyParameters(ModelMap map) {
  }

}
