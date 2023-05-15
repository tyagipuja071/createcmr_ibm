package com.ibm.cio.cmr.request.model.code;

import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.cio.cmr.request.model.BaseModel;

public class USEnterpriseModel extends BaseModel {

  private static final long serialVersionUID = 1L;

  private String mandt;

  private String entNo;

  private String entLegalName;

  private String entTypeCode;

  private String loevm;

  private String createBy;

  private Date createDt;

  private String updateBy;

  private Date updateDt;

  private String updateType;

  private String katr10;

  private String createdTsStr;

  private String updatedTsStr;

  public static long getSerialversionuid() {
    return serialVersionUID;
  }

  public String getMandt() {
    return mandt;
  }

  public void setMandt(String mandt) {
    this.mandt = mandt;
  }

  public String getEntNo() {
    return entNo;
  }

  public void setEntNo(String entNo) {
    this.entNo = entNo;
  }

  public String getEntLegalName() {
    return entLegalName;
  }

  public void setEntLegalName(String entLegalName) {
    this.entLegalName = entLegalName;
  }

  public String getEntTypeCode() {
    return entTypeCode;
  }

  public void setEntTypeCode(String entTypeCode) {
    this.entTypeCode = entTypeCode;
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

  public String getKatr10() {
    return katr10;
  }

  public void setKatr10(String katr10) {
    this.katr10 = katr10;
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
    blnAllKeys = !StringUtils.isEmpty(this.mandt) && !StringUtils.isEmpty(this.entNo);

    return blnAllKeys;
  }

  @Override
  public String getRecordDescription() {
    return "US ENTERPRISE Entry";
  }

  @Override
  public void addKeyParameters(ModelAndView mv) {
    // TODO Auto-generated method stub

  }

  @Override
  public void addKeyParameters(ModelMap map) {
    // TODO Auto-generated method stub

  }

}