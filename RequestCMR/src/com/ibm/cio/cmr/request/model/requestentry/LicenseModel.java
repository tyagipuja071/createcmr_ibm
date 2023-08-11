package com.ibm.cio.cmr.request.model.requestentry;

import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.cio.cmr.request.model.BaseModel;

public class LicenseModel extends BaseModel {
  private static final long serialVersionUID = 1L;

  private long reqId;
  private String licenseNum;
  private String currentIndc;
  private String validFrom;
  private String validTo;

  public long getReqId() {
    return reqId;
  }

  public void setReqId(long reqId) {
    this.reqId = reqId;
  }

  @Override
  public boolean allKeysAssigned() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public String getRecordDescription() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void addKeyParameters(ModelAndView mv) {
    // TODO Auto-generated method stub

  }

  @Override
  public void addKeyParameters(ModelMap map) {
    // TODO Auto-generated method stub

  }

  public String getCurrentIndc() {
    return currentIndc;
  }

  public void setCurrentIndc(String currentIndc) {
    this.currentIndc = currentIndc;
  }

  public String getValidFrom() {
    return validFrom;
  }

  public void setValidFrom(String validFrom) {
    this.validFrom = validFrom;
  }

  public String getValidTo() {
    return validTo;
  }

  public void setValidTo(String validTo) {
    this.validTo = validTo;
  }

  public String getLicenseNum() {
    return licenseNum;
  }

  public void setLicenseNum(String licenseNum) {
    this.licenseNum = licenseNum;
  }

}
