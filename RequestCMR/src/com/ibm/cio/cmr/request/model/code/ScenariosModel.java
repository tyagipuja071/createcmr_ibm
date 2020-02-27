/**
 * 
 */
package com.ibm.cio.cmr.request.model.code;

import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

/**
 * @author Jeffrey Zamora
 * 
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ScenariosModel {

  private String issuingCntry;
  private String custTypeVal;
  private String custSubTypeVal;
  private String addMode;
  private String code;
  private String desc;
  private List<ScenarioField> fields;

  public String getIssuingCntry() {
    return issuingCntry;
  }

  public void setIssuingCntry(String issuingCntry) {
    this.issuingCntry = issuingCntry;
  }

  public String getCustTypeVal() {
    return custTypeVal;
  }

  public void setCustTypeVal(String custTypeVal) {
    this.custTypeVal = custTypeVal;
  }

  public String getCustSubTypeVal() {
    return custSubTypeVal;
  }

  public void setCustSubTypeVal(String custSubTypeVal) {
    this.custSubTypeVal = custSubTypeVal;
  }

  public String getAddMode() {
    return addMode;
  }

  public void setAddMode(String addMode) {
    this.addMode = addMode;
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public String getDesc() {
    return desc;
  }

  public void setDesc(String desc) {
    this.desc = desc;
  }

  public List<ScenarioField> getFields() {
    return fields;
  }

  public void setFields(List<ScenarioField> fields) {
    this.fields = fields;
  }

}
