package com.ibm.cio.cmr.request.automation.util.geo;

public class NordicsCovMapping {

  private String country;
  private String isuCTC;
  private String sortl;
  private String salesRep;
  private String subIndustry;
  private boolean exclude;

  public String getIsuCTC() {
    return isuCTC;
  }

  public void setIsuCTC(String isuCTC) {
    this.isuCTC = isuCTC;
  }

  public String getSortl() {
    return sortl;
  }

  public void setSortl(String sortl) {
    this.sortl = sortl;
  }

  public String getSalesRep() {
    return salesRep;
  }

  public void setSalesRep(String salesRep) {
    this.salesRep = salesRep;
  }

  public String getSubIndustry() {
    return subIndustry;
  }

  public void setSubIndustry(String subIndustry) {
    this.subIndustry = subIndustry;
  }

  public String getCountry() {
    return country;
  }

  public void setCountry(String country) {
    this.country = country;
  }

  public boolean isExclude() {
    return exclude;
  }

  public void setExclude(boolean exclude) {
    this.exclude = exclude;
  }

}