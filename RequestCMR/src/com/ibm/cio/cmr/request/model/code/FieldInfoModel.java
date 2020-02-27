package com.ibm.cio.cmr.request.model.code;

import org.apache.commons.lang.StringUtils;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.cio.cmr.request.model.BaseModel;

/**
 * @author Sonali Jain
 * 
 */
public class FieldInfoModel extends BaseModel {

  private static final long serialVersionUID = -9202929601837918024L;

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
  private String cmt;
  private String valDependsOn;
  private String queryId;

  @Override
  public boolean allKeysAssigned() {
    boolean allKeysAssigned = false;
    if (!StringUtils.isEmpty(this.fieldId) && !StringUtils.isEmpty(this.cmrIssuingCntry) && (this.seqNo > 0)) {
      allKeysAssigned = true;
    }
    return allKeysAssigned;
  }

  @Override
  public String getRecordDescription() {
    return "FieldInfo";
  }

  @Override
  public void addKeyParameters(ModelAndView mv) {
    // TODO Auto-generated method stub

  }

  @Override
  public void addKeyParameters(ModelMap map) {
    // TODO Auto-generated method stub

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

  public String getCmt() {
    return cmt;
  }

  public void setCmt(String cmt) {
    this.cmt = cmt;
  }

  public String getValDependsOn() {
    return valDependsOn;
  }

  public void setValDependsOn(String valDependsOn) {
    this.valDependsOn = valDependsOn;
  }

  public int getSeqNo() {
    return seqNo;
  }

  public void setSeqNo(int seqNo) {
    this.seqNo = seqNo;
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

  public String getQueryId() {
    return queryId;
  }

  public void setQueryId(String queryId) {
    this.queryId = queryId;
  }

}
