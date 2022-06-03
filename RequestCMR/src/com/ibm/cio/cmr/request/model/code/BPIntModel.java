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
public class BPIntModel extends BaseModel {

  private static final long serialVersionUID = 1L;
  private String bpCode;

  public String getBpCode() {
    return bpCode;
  }

  public void setBpCode(String bpCode) {
    this.bpCode = bpCode;
  }

  private String nBpAbbrevNm;
  private String nBpFullNm;
  private String nKatr10;
  private String nLoevm;
  private String mandt;

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

  public String getCreateDt() {
    return createDt;
  }

  public void setCreateDt(String createDt) {
    this.createDt = createDt;
  }

  public String getUpdatedBy() {
    return updatedBy;
  }

  public void setUpdatedBy(String updatedBy) {
    this.updatedBy = updatedBy;
  }

  public String getUpdateDt() {
    return updateDt;
  }

  public void setUpdateDt(String updateDt) {
    this.updateDt = updateDt;
  }

  private String createdBy;
  private String createDt;

  private String updateType;
  private String updatedBy;
  private String updateDt;

  @Override
  public boolean allKeysAssigned() {

    boolean blnAllKeys = false;
    if (!StringUtils.isBlank(this.bpCode) && !StringUtils.isBlank(this.mandt)) {
      blnAllKeys = true;
    }
    return blnAllKeys;
  }

  @Override
  public String getRecordDescription() {
    return "BPINT Entry";
  }

  @Override
  public void addKeyParameters(ModelAndView mv) {
  }

  @Override
  public void addKeyParameters(ModelMap map) {
  }

  public String getnBpAbbrevNm() {
    return nBpAbbrevNm;
  }

  public void setnBpAbbrevNm(String nBpAbbrevNm) {
    this.nBpAbbrevNm = nBpAbbrevNm;
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

  public String getnBpFullNm() {
    return nBpFullNm;
  }

  public void setnBpFullNm(String nBpFullNm) {
    this.nBpFullNm = nBpFullNm;
  }

}
