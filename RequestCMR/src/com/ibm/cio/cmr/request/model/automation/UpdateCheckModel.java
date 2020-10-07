package com.ibm.cio.cmr.request.model.automation;

import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.cio.cmr.request.model.BaseModel;

public class UpdateCheckModel extends BaseModel {
  private static final long serialVersionUID = 1L;

  private String issuingCountry;
  private String reqId;
  private String result;
  private String validationMessage;
  private boolean onError;

  public String getIssuingCountry() {
    return issuingCountry;
  }

  public void setIssuingCountry(String issuingCountry) {
    this.issuingCountry = issuingCountry;
  }

  public String getReqId() {
    return reqId;
  }

  public void setReqId(String reqId) {
    this.reqId = reqId;
  }

  public String getResult() {
    return result;
  }

  public void setResult(String result) {
    this.result = result;
  }

  @Override
  public boolean allKeysAssigned() {
    return false;
  }

  @Override
  public String getRecordDescription() {
    return null;
  }

  @Override
  public void addKeyParameters(ModelAndView mv) {
  }

  @Override
  public void addKeyParameters(ModelMap map) {
  }

  public boolean isOnError() {
    return onError;
  }

  public void setOnError(Boolean onError) {
    this.onError = onError;
  }

  public String getValidationMessage() {
    return validationMessage;
  }

  public void setValidationMessage(String validationMessage) {
    this.validationMessage = validationMessage;
  }
}
