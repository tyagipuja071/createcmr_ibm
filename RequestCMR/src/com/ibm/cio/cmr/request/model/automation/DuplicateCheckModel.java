package com.ibm.cio.cmr.request.model.automation;

import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.cio.cmr.request.model.BaseModel;

/**
 * @author PoojaTyagi
 * 
 */

public class DuplicateCheckModel extends BaseModel {
  private static final long serialVersionUID = 1L;

  private String issuingCountry;
  private String customerName;
  private String landedCountry;
  private String streetLine1;
  private String streetLine2;
  private String city;
  private String stateProv;
  private String postalCode;
  private String subscenario;
  private String vat;
  private String vatzi01;
  private String cmrNo;
  private String reqId;
  private String matchType;
  private boolean success;
  private String message;

  public String getIssuingCountry() {
    return issuingCountry;
  }

  public void setIssuingCountry(String issuingCountry) {
    this.issuingCountry = issuingCountry;
  }

  public String getCustomerName() {
    return customerName;
  }

  public void setCustomerName(String customerName) {
    this.customerName = customerName;
  }

  public String getLandedCountry() {
    return landedCountry;
  }

  public void setLandedCountry(String landedCountry) {
    this.landedCountry = landedCountry;
  }

  public String getStreetLine1() {
    return streetLine1;
  }

  public void setStreetLine1(String streetLine1) {
    this.streetLine1 = streetLine1;
  }

  public String getStreetLine2() {
    return streetLine2;
  }

  public void setStreetLine2(String streetLine2) {
    this.streetLine2 = streetLine2;
  }

  public String getCity() {
    return city;
  }

  public void setCity(String city) {
    this.city = city;
  }

  public String getStateProv() {
    return stateProv;
  }

  public void setStateProv(String stateProv) {
    this.stateProv = stateProv;
  }

  public String getPostalCode() {
    return postalCode;
  }

  public void setPostalCode(String postalCode) {
    this.postalCode = postalCode;
  }

  public String getVat() {
    return vat;
  }

  public void setVat(String vat) {
    this.vat = vat;
  }

  public String getMatchType() {
    return matchType;
  }

  public void setMatchType(String matchType) {
    this.matchType = matchType;
  }

  public String getSubscenario() {
    return subscenario;
  }

  public void setSubscenario(String subscenario) {
    this.subscenario = subscenario;
  }

  public boolean isSuccess() {
    return success;
  }

  public void setSuccess(boolean success) {
    this.success = success;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public String getReqId() {
    return reqId;
  }

  public void setReqId(String reqId) {
    this.reqId = reqId;
  }

  public String getCmrNo() {
    return cmrNo;
  }

  public void setCmrNo(String cmrNo) {
    this.cmrNo = cmrNo;
  }

  public String getVatzi01() {
    return vatzi01;
  }

  public void setVatzi01(String vatzi01) {
    this.vatzi01 = vatzi01;
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

}
