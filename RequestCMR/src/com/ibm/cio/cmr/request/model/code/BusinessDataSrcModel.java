package com.ibm.cio.cmr.request.model.code;

import org.apache.commons.lang.StringUtils;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.cio.cmr.request.model.BaseModel;

public class BusinessDataSrcModel extends BaseModel {

  private static final long serialVersionUID = 3091790355675429688L;

  private String fieldId;
  private String schema;
  private String tbl;
  private String cd;
  private String desc;
  private String dispType;
  private String orderByField;
  private String cmt;

  public String getFieldId() {
    return fieldId;
  }

  public void setFieldId(String fieldId) {
    this.fieldId = fieldId;
  }

  public String getSchema() {
    return schema;
  }

  public void setSchema(String schema) {
    this.schema = schema;
  }

  public String getTbl() {
    return tbl;
  }

  public void setTbl(String tbl) {
    this.tbl = tbl;
  }

  public String getCd() {
    return cd;
  }

  public void setCd(String cd) {
    this.cd = cd;
  }

  public String getDesc() {
    return desc;
  }

  public void setDesc(String desc) {
    this.desc = desc;
  }

  public String getDispType() {
    return dispType;
  }

  public void setDispType(String dispType) {
    this.dispType = dispType;
  }

  public String getOrderByField() {
    return orderByField;
  }

  public void setOrderByField(String orderByField) {
    this.orderByField = orderByField;
  }

  public String getCmt() {
    return cmt;
  }

  public void setCmt(String cmt) {
    this.cmt = cmt;
  }

  @Override
  public boolean allKeysAssigned() {
    return !StringUtils.isEmpty(this.fieldId);
  }

  @Override
  public String getRecordDescription() {
    return "Business Data Source";
  }

  @Override
  public void addKeyParameters(ModelAndView mv) {
    mv.addObject("fieldId", this.fieldId);
  }

  @Override
  public void addKeyParameters(ModelMap map) {
    map.put("fieldId", this.fieldId);
  }

}
