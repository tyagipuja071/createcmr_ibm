package com.ibm.cio.cmr.request.automation.util;

public class DACHFieldContainer {
  String enterprise;
  String isuCd;
  String clientTier;
  String searchTerm;
  String inac;

  public String getEnterprise() {
    return enterprise;
  }

  public void setEnterprise(String enterprise) {
    this.enterprise = enterprise;
  }

  public String getIsuCd() {
    return isuCd;
  }

  public void setIsuCd(String isuCd) {
    this.isuCd = isuCd;
  }

  public String getClientTier() {
    return clientTier;
  }

  public void setClientTier(String clientTier) {
    this.clientTier = clientTier;
  }

  public String getSearchTerm() {
    return searchTerm;
  }

  public void setSearchTerm(String searchTerm) {
    this.searchTerm = searchTerm;
  }

  public String getInac() {
    return inac;
  }

  public void setInac(String inac) {
    this.inac = inac;
  }
}