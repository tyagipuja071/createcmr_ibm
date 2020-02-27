/**
 * 
 */
package com.ibm.cio.cmr.request.model.code;

import java.util.List;

/**
 * @author Jeffrey Zamora
 * 
 */
public class LovValuesModel {

  private String cmrIssuingCntry;
  private String fieldId;
  private String cmt;
  private String dispType;
  private boolean delete;
  private List<String> values;

  public String getCmrIssuingCntry() {
    return cmrIssuingCntry;
  }

  public void setCmrIssuingCntry(String cmrIssuingCntry) {
    this.cmrIssuingCntry = cmrIssuingCntry;
  }

  public String getFieldId() {
    return fieldId;
  }

  public void setFieldId(String fieldId) {
    this.fieldId = fieldId;
  }

  public String getCmt() {
    return cmt;
  }

  public void setCmt(String cmt) {
    this.cmt = cmt;
  }

  public String getDispType() {
    return dispType;
  }

  public void setDispType(String dispType) {
    this.dispType = dispType;
  }

  public List<String> getValues() {
    return values;
  }

  public void setValues(List<String> values) {
    this.values = values;
  }

  public boolean isDelete() {
    return delete;
  }

  public void setDelete(boolean delete) {
    this.delete = delete;
  }

}
