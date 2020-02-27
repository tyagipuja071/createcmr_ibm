package com.ibm.cio.cmr.request.model.code;

import org.apache.commons.lang.StringUtils;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.cio.cmr.request.model.BaseModel;

public class FieldLabelModel extends BaseModel {

  private static final long serialVersionUID = 8930465768154250147L;

  private String lbl;
  private String cmt;
  private String fieldId;
  private String cmrIssuingCntry;

  public String getLbl() {
    return lbl;
  }

  public void setLbl(String lbl) {
    this.lbl = lbl;
  }

  public String getCmt() {
    return cmt;
  }

  public void setCmt(String cmt) {
    this.cmt = cmt;
  }

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

  @Override
  public boolean allKeysAssigned() {
    return !StringUtils.isEmpty(this.fieldId) && !StringUtils.isEmpty(this.cmrIssuingCntry);
  }

  @Override
  public String getRecordDescription() {
    return "Field Label";
  }

  @Override
  public void addKeyParameters(ModelAndView mv) {
    mv.addObject("fieldId", this.fieldId);
  }

  @Override
  public void addKeyParameters(ModelMap map) {
    map.put("fieldId", this.fieldId);
    map.put("cmrIssuingCntry", this.cmrIssuingCntry);
  }

}
