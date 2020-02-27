package com.ibm.cio.cmr.request.model.requestentry;

import java.util.Date;

import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.cio.cmr.request.model.BaseModel;

public class GeoTaxInfoModel extends BaseModel {
  private static final long serialVersionUID = 1L;

  private long reqId;
  private int geoTaxInfoId;
  private String taxNum;
  private String taxCd;
  private String taxSeparationIndc;
  private String billingPrintIndc;
  private String contractPrintIndc;
  private String cntryUse;

  private Date createTs;
  private String createTsString;
  private Date updtTs;
  private String updtTsString;
  private String createById;
  private String updtById;

  public long getReqId() {
    return reqId;
  }

  public void setReqId(long reqId) {
    this.reqId = reqId;
  }

  public int getGeoTaxInfoId() {
    return geoTaxInfoId;
  }

  public void setGeoTaxInfoId(int geoTaxInfoId) {
    this.geoTaxInfoId = geoTaxInfoId;
  }

  public String getBillingPrintIndc() {
    return billingPrintIndc;
  }

  public void setBillingPrintIndc(String billingPrintIndc) {
    this.billingPrintIndc = billingPrintIndc;
  }

  public String getContractPrintIndc() {
    return contractPrintIndc;
  }

  public void setContractPrintIndc(String contractPrintIndc) {
    this.contractPrintIndc = contractPrintIndc;
  }

  public Date getCreateTs() {
    return createTs;
  }

  public void setCreateTs(Date createTs) {
    this.createTs = createTs;
  }

  public String getCreateTsString() {
    return createTsString;
  }

  public void setCreateTsString(String createTsString) {
    this.createTsString = createTsString;
  }

  public Date getUpdtTs() {
    return updtTs;
  }

  public void setUpdtTs(Date updtTs) {
    this.updtTs = updtTs;
  }

  public String getUpdtTsString() {
    return updtTsString;
  }

  public void setUpdtTsString(String updtTsString) {
    this.updtTsString = updtTsString;
  }

  public String getTaxNum() {
    return taxNum;
  }

  public void setTaxNum(String taxNum) {
    this.taxNum = taxNum;
  }

  public String getTaxCd() {
    return taxCd;
  }

  public void setTaxCd(String taxCd) {
    this.taxCd = taxCd;
  }

  public String getTaxSeparationIndc() {
    return taxSeparationIndc;
  }

  public void setTaxSeparationIndc(String taxSeparationIndc) {
    this.taxSeparationIndc = taxSeparationIndc;
  }

  public String getCntryUse() {
    return cntryUse;
  }

  public void setCntryUse(String cntryUse) {
    this.cntryUse = cntryUse;
  }

  public String getCreateById() {
    return createById;
  }

  public void setCreateById(String createById) {
    this.createById = createById;
  }

  public String getUpdtById() {
    return updtById;
  }

  public void setUpdtById(String updtById) {
    this.updtById = updtById;
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
