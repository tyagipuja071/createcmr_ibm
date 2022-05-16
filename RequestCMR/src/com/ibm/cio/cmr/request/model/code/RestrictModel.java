/**
 * 
 */
package com.ibm.cio.cmr.request.model.code;

import org.apache.commons.lang3.StringUtils;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.cio.cmr.request.model.BaseModel;

/**
 * @author Jeffrey Zamora
 * 
 */
public class RestrictModel extends BaseModel {

  private static final long serialVersionUID = 1L;
  private String restrictToCd;

  private String nRestrictToAbbrevNm;
  private String nRestrictToNm;
  private String nKatr10;
  private String nLoevm;
  private String mandt;

  private String createdBy;
  private String createDt;

  public String getCreateDt() {
    return createDt;
  }

  public void setCreateDt(String createDt) {
    this.createDt = createDt;
  }

  public String getUpdateDt() {
    return updateDt;
  }

  public void setUpdateDt(String updateDt) {
    this.updateDt = updateDt;
  }

  private String updateType;
  private String updatedBy;
  private String updateDt;

  @Override
  public boolean allKeysAssigned() {

    boolean blnAllKeys = false;
    if (!StringUtils.isBlank(this.restrictToCd) && !StringUtils.isBlank(this.mandt)) {
      blnAllKeys = true;
    }
    return blnAllKeys;
  }

  @Override
  public String getRecordDescription() {
    return "RestrictTo Entry";
  }

  @Override
  public void addKeyParameters(ModelAndView mv) {
  }

  @Override
  public void addKeyParameters(ModelMap map) {
  }

  public String getRestrictToCd() {
    return restrictToCd;
  }

  public void setRestrictToCd(String restrictToCd) {
    this.restrictToCd = restrictToCd;
  }

  public String getnRestrictToAbbrevNm() {
    return nRestrictToAbbrevNm;
  }

  public void setnRestrictToAbbrevNm(String nRestrictToAbbrevNm) {
    this.nRestrictToAbbrevNm = nRestrictToAbbrevNm;
  }

  public String getnRestrictToNm() {
    return nRestrictToNm;
  }

  public void setnRestrictToNm(String nRestrictToNm) {
    this.nRestrictToNm = nRestrictToNm;
  }

  public String getUpdateType() {
    return updateType;
  }

  public void setUpdateType(String updateType) {
    this.updateType = updateType;
  }

  public String getnKatr10() {
    return nKatr10;
  }

  public void setnKatr10(String nKatr10) {
    this.nKatr10 = nKatr10;
  }

  public String getnLoevm() {
    return nLoevm;
  }

  public void setnLoevm(String nLoevm) {
    this.nLoevm = nLoevm;
  }

  public String getMandt() {
    return mandt;
  }

  public void setMandt(String mandt) {
    this.mandt = mandt;
  }

  public String getCreatedBy() {
    return createdBy;
  }

  public void setCreatedBy(String createdBy) {
    this.createdBy = createdBy;
  }

  public String getUpdatedBy() {
    return this.updatedBy;
  }

  public void setUpdatedBy(String updatedBy) {
    this.updatedBy = updatedBy;
  }

}
