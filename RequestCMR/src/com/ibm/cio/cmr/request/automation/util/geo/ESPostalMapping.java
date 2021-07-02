package com.ibm.cio.cmr.request.automation.util.geo;

public class ESPostalMapping {
  private String isuCTC;
  private String enterprise;
  private String salesRep;
  private String postalCdStarts;
  private String isicBelongs;
  private String scenarios;

  public String getPostalCdStarts() {
    return postalCdStarts;
  }

  public void setPostalCdStarts(String postalCdStarts) {
    this.postalCdStarts = postalCdStarts;
  }

  public String getEnterprise() {
    return enterprise;
  }

  public void setEnterprise(String enterprise) {
    this.enterprise = enterprise;
  }

  public String getSaleRep() {
    return salesRep;
  }

  public void setSalesRep(String salesRep) {
    this.salesRep = salesRep;
  }

  public String getIsicBelongs() {
    return isicBelongs;
  }

  public void setIsicBelongs(String isicBelongs) {
    this.isicBelongs = isicBelongs;
  }

  public String getIsuCTC() {
    return isuCTC;
  }

  public void setIsuCTC(String isuCTC) {
    this.isuCTC = isuCTC;
  }

  public String getScenarios() {
    return scenarios;
  }

  public void setScenario(String scenarios) {
    this.scenarios = scenarios;
  }
}
