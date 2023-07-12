package com.ibm.cio.cmr.request.util.geo.impl;

/**
 * 
 * @author PoojaTyagi
 *
 */

public class IndiaProvCdStateCityMapping {
  private String state;
  private String city;
  private String arCode;
  private String povinceCd;

  public String getState() {
    return state;
  }

  public void setState(String state) {
    this.state = state;
  }

  public String getProvinceCd() {
    return povinceCd;
  }

  public void setProvinceCd(String povinceCd) {
    this.povinceCd = povinceCd;
  }

  public String getCity() {
    return city;
  }

  public void setCity(String city) {
    this.city = city;
  }

  public String getArCode() {
    return arCode;
  }

  public void setArCode(String arCode) {
    this.arCode = arCode;
  }

}
