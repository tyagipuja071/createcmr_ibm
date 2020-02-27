package com.ibm.cio.cmr.request.model.code;

import org.apache.commons.lang.StringUtils;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.cio.cmr.request.model.BaseModel;

public class LovModel extends BaseModel {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  private String fieldId;
  private String cmrIssuingCntry;
  private String cd;
  private String defaultInd;
  private int dispOrder;
  private String dispType;
  private String txt;
  private String cmt;

  public String getFieldId() {
    return fieldId;
  }

  public void setFieldId(String fieldId) {
    this.fieldId = fieldId;
  }

  public String getCmrIssuingCntry() {
    return cmrIssuingCntry;
  }

  public void setCmrIssuingCntry(String cmrIssuingCntry) {
    this.cmrIssuingCntry = cmrIssuingCntry;
  }

  public String getCd() {
    return cd;
  }

  public void setCd(String cd) {
    this.cd = cd;
  }

  public String getDefaultInd() {
    return defaultInd;
  }

  public void setDefaultInd(String defaultInd) {
    this.defaultInd = defaultInd;
  }

  public int getDispOrder() {
    return dispOrder;
  }

  public void setDispOrder(int dispOrder) {
    this.dispOrder = dispOrder;
  }

  public String getDispType() {
    return dispType;
  }

  public void setDispType(String dispType) {
    this.dispType = dispType;
  }

  public String getTxt() {
    return txt;
  }

  public void setTxt(String txt) {
    this.txt = txt;
  }

  public String getCmt() {
    return cmt;
  }

  public void setCmt(String cmt) {
    this.cmt = cmt;
  }

  @Override
  public boolean allKeysAssigned() {
    boolean allKeysAssigned = false;
    if (!StringUtils.isEmpty(this.fieldId) && !StringUtils.isEmpty(this.cmrIssuingCntry) && !StringUtils.isEmpty(this.cd)) {
      allKeysAssigned = true;
    }
    return allKeysAssigned;
  }

  @Override
  public String getRecordDescription() {
    return "LOV";
  }

  @Override
  public void addKeyParameters(ModelAndView mv) {
  }

  @Override
  public void addKeyParameters(ModelMap map) {
  }

}
