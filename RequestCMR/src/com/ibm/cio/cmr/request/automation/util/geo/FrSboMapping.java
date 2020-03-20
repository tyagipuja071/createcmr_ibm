package com.ibm.cio.cmr.request.automation.util.geo;

/**
 * 
 * @author PoojaTyagi
 *
 */

public class FrSboMapping {
  private String isicCds;
  private String isu;
  private String ctc;
  private String sbo;
  private String postalCdStarts;
  private String countryUse;

  public String getIsicCds() {
    return isicCds;
  }

  public void setIsicCds(String isicCds) {
    this.isicCds = isicCds;
  }

  public String getPostalCdStarts() {
    return postalCdStarts;
  }

  public void setPostalCdStarts(String postalCdStarts) {
    this.postalCdStarts = postalCdStarts;
  }

  public String getIsu() {
    return isu;
  }

  public void setIsu(String isu) {
    this.isu = isu;
  }

  public String getCtc() {
    return ctc;
  }

  public void setCtc(String ctc) {
    this.ctc = ctc;
  }

  public String getSbo() {
    return sbo;
  }

  public void setSbo(String sbo) {
    this.sbo = sbo;
  }

  public String getCountryUse() {
    return countryUse;
  }

  public void setCountryUse(String countryUse) {
    this.countryUse = countryUse;
  }

}
