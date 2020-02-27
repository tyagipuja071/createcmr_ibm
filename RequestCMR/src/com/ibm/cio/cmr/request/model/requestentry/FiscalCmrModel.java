package com.ibm.cio.cmr.request.model.requestentry;

import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.cio.cmr.request.model.BaseModel;

public class FiscalCmrModel extends BaseModel {
private static final long serialVersionUID = 1L;
  
  private long reqId;
  private String cmrNo;
  private String addrType;
  private String landCnty;
  private String city;
  private String streetAddress;
  private String postalCode;
  private String identClient;
  private String vat;
  private String enterprise;
  private String affiliate;
  private String fiscalCode;
  private String zzkvNode1;

  public long getReqId() {
    return reqId;
  }

  public void setReqId(long reqId) {
    this.reqId = reqId;
  }

  public String getCmrNo() {
    return cmrNo;
  }

  public void setCmrNo(String cmrNo) {
    this.cmrNo = cmrNo;
  }
  
  public String getAddrType() {
    return addrType;
  }

  public void setAddrType(String addrType) {
    this.addrType = addrType;
  }
  
  public String getLandCnty() {
    return landCnty;
  }

  public void setLandedCnty(String landCnty) {
    this.landCnty = landCnty;
  }

  public String getCity() {
    return city;
  }

  public void setCity(String city) {
    this.city = city;
  }

  public String getStreetAddress() {
    return streetAddress;
  }

  public void setStreetAddress(String streetAddress) {
    this.streetAddress = streetAddress;
  }

  public String getPostalCode() {
    return postalCode;
  }

  public void setPostalCode(String postalCode) {
    this.postalCode = postalCode;
  }

  public String getIdentClient() {
    return identClient;
  }

  public void setIdentClient(String identClient) {
    this.identClient = identClient;
  }

  public String getVat() {
    return vat;
  }

  public void setVat(String vat) {
    this.vat = vat;
  }

  public String getEnterprise() {
    return enterprise;
  }

  public void setEnterprise(String enterprise) {
    this.enterprise = enterprise;
  }

  public String getAffiliate() {
    return affiliate;
  }

  public void setAffiliate(String affiliate) {
    this.affiliate = affiliate;
  }
  

  public String getFiscalCode() {
    return fiscalCode;
  }

  public void setFiscalCode(String fiscalCode) {
    this.fiscalCode = fiscalCode;
  }

  public String getZzkvNode1() {
    return zzkvNode1;
  }

  public void setZzkvNode1(String zzkvNode1) {
    this.zzkvNode1 = zzkvNode1;
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

}