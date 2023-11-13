package com.ibm.cio.cmr.request.model.code;

import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.cio.cmr.request.model.BaseModel;

public class JpIsicToJsicMapModel extends BaseModel {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  private String mandt;
  private String jsicCd;
  private String isicCd;
  private String createBy;
  private Date createTs;
  private String createTsStr;
  private String updateBy;
  private Date updateTs;
  private String updateTsStr;

  public String getMandt() {
    return mandt;
  }

  public void setMandt(String mandt) {
    this.mandt = mandt;
  }

  public String getJsicCd() {
    return jsicCd;
  }

  public void setJsicCd(String jsicCd) {
    this.jsicCd = jsicCd;
  }

  public String getIsicCd() {
    return isicCd;
  }

  public void setIsicCd(String isicCd) {
    this.isicCd = isicCd;
  }

  public String getCreateBy() {
    return createBy;
  }

  public void setCreateBy(String createBy) {
    this.createBy = createBy;
  }

  public Date getCreateTs() {
    return createTs;
  }

  public void setCreateTs(Date createTs) {
    this.createTs = createTs;
  }

  public String getUpdateBy() {
    return updateBy;
  }

  public void setUpdateBy(String updateBy) {
    this.updateBy = updateBy;
  }

  public Date getUpdateTs() {
    return updateTs;
  }

  public void setUpdateTs(Date updateTs) {
    this.updateTs = updateTs;
  }

  @Override
  public boolean allKeysAssigned() {
    boolean allKeysAssigned = false;
    if (!StringUtils.isEmpty(this.jsicCd) && !StringUtils.isEmpty(this.mandt)) {
      allKeysAssigned = true;
    }
    return allKeysAssigned;
  }

  @Override
  public String getRecordDescription() {
    return "JP ISIC TO JSIC MAP";
  }

  @Override
  public void addKeyParameters(ModelAndView mv) {
  }

  @Override
  public void addKeyParameters(ModelMap map) {
  }

  public String getCreateTsStr() {
    return createTsStr;
  }

  public void setCreateTsStr(String createTsStr) {
    this.createTsStr = createTsStr;
  }

  public String getUpdateTsStr() {
    return updateTsStr;
  }

  public void setUpdateTsStr(String updateTsStr) {
    this.updateTsStr = updateTsStr;
  }

}
