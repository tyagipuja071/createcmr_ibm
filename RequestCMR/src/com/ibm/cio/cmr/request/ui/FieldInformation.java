/**
 * 
 */
package com.ibm.cio.cmr.request.ui;

import java.io.Serializable;

import com.ibm.cio.cmr.request.entity.FieldInfo;

/**
 * UI counterpart of {@link FieldInfo}
 * 
 * @author Jeffrey Zamora
 * 
 */
public class FieldInformation implements Serializable, Comparable<FieldInformation> {

  private static final long serialVersionUID = 1L;

  private String fieldId;

  private String cmrIssuingCntry;

  private int seqNo;

  private String type;

  private String choice;

  private int minLength;

  private int maxLength;

  private String validation;

  private String required;

  private String dependsOn;

  private String dependsSetting;

  private String condReqInd;

  private String readOnlyReqInd;

  private String readOnlyInfoInd;

  private String readOnlyRevInd;

  private String readOnlyProcInd;

  private String label;

  private String valDependsOn;

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getChoice() {
    return choice;
  }

  public void setChoice(String choice) {
    this.choice = choice;
  }

  public int getMinLength() {
    return minLength;
  }

  public void setMinLength(int minLength) {
    this.minLength = minLength;
  }

  public int getMaxLength() {
    return maxLength;
  }

  public void setMaxLength(int maxLength) {
    this.maxLength = maxLength;
  }

  public String getValidation() {
    return validation;
  }

  public void setValidation(String validation) {
    this.validation = validation;
  }

  public String getRequired() {
    return required;
  }

  public void setRequired(String required) {
    this.required = required;
  }

  public String getDependsOn() {
    return dependsOn;
  }

  public void setDependsOn(String dependsOn) {
    this.dependsOn = dependsOn;
  }

  public String getDependsSetting() {
    return dependsSetting;
  }

  public void setDependsSetting(String dependsSetting) {
    this.dependsSetting = dependsSetting;
  }

  public String getCondReqInd() {
    return condReqInd;
  }

  public void setCondReqInd(String condReqInd) {
    this.condReqInd = condReqInd;
  }

  public String getReadOnlyReqInd() {
    return readOnlyReqInd;
  }

  public void setReadOnlyReqInd(String readOnlyReqInd) {
    this.readOnlyReqInd = readOnlyReqInd;
  }

  public String getReadOnlyInfoInd() {
    return readOnlyInfoInd;
  }

  public void setReadOnlyInfoInd(String readOnlyInfoInd) {
    this.readOnlyInfoInd = readOnlyInfoInd;
  }

  public String getReadOnlyRevInd() {
    return readOnlyRevInd;
  }

  public void setReadOnlyRevInd(String readOnlyRevInd) {
    this.readOnlyRevInd = readOnlyRevInd;
  }

  public String getReadOnlyProcInd() {
    return readOnlyProcInd;
  }

  public void setReadOnlyProcInd(String readOnlyProcInd) {
    this.readOnlyProcInd = readOnlyProcInd;
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

  public int getSeqNo() {
    return seqNo;
  }

  public void setSeqNo(int seqNo) {
    this.seqNo = seqNo;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof FieldInformation)) {
      return false;
    }
    FieldInformation inf = (FieldInformation) o;
    return this.fieldId.equals(inf.fieldId) && this.cmrIssuingCntry.equals(inf.cmrIssuingCntry) && this.seqNo == inf.seqNo;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  @Override
  public int compareTo(FieldInformation o) {
    return this.seqNo > o.seqNo ? 1 : (this.seqNo < o.seqNo ? -1 : 0);
  }

  public String getValDependsOn() {
    return valDependsOn;
  }

  public void setValDependsOn(String valDependsOn) {
    this.valDependsOn = valDependsOn;
  }
}
