/**
 * 
 */
package com.ibm.cio.cmr.request.model.ws;

/**
 * 
 * @author Jeffrey Zamora
 * 
 */
public class TgmeAddrStdModel {

  // ids
  private long reqId;
  private String addrType;

  // inputs
  private String addrTxt;
  private String city1;
  private String stateProv;
  private String stateProvDesc;
  private String postCd;
  private String landCntry;

  // response
  private String stdAddrTxt;
  private String stdAddrTxt2;
  private String stdCity;
  private String stdStateProv;
  private String stdStateCode;
  private String stdPostCd;
  private String stdLandCntry;

  private String stdResultCode;
  private String stdResultDesc;
  private String stdResultText;
  private String stdResultStatus;

  private String stdResultPostCd;
  private String stdResultCity;
  private String stdResultProvince;
  private String stdResultStreet;

  public long getReqId() {
    return reqId;
  }

  public void setReqId(long reqId) {
    this.reqId = reqId;
  }

  public String getAddrType() {
    return addrType;
  }

  public void setAddrType(String addrType) {
    this.addrType = addrType;
  }

  public String getAddrTxt() {
    return addrTxt;
  }

  public void setAddrTxt(String addrTxt) {
    this.addrTxt = addrTxt;
  }

  public String getCity1() {
    return city1;
  }

  public void setCity1(String city1) {
    this.city1 = city1;
  }

  public String getStateProv() {
    return stateProv;
  }

  public void setStateProv(String stateProv) {
    this.stateProv = stateProv;
  }

  public String getPostCd() {
    return postCd;
  }

  public void setPostCd(String postCd) {
    this.postCd = postCd;
  }

  public String getLandCntry() {
    return landCntry;
  }

  public void setLandCntry(String landCntry) {
    this.landCntry = landCntry;
  }

  public String getStdAddrTxt() {
    return stdAddrTxt;
  }

  public void setStdAddrTxt(String stdAddrTxt) {
    this.stdAddrTxt = stdAddrTxt;
  }

  public String getStdCity() {
    return stdCity;
  }

  public void setStdCity(String stdCity) {
    this.stdCity = stdCity;
  }

  public String getStdStateProv() {
    return stdStateProv;
  }

  public void setStdStateProv(String stdStateProv) {
    this.stdStateProv = stdStateProv;
  }

  public String getStdPostCd() {
    return stdPostCd;
  }

  public void setStdPostCd(String stdPostCd) {
    this.stdPostCd = stdPostCd;
  }

  public String getStdLandCntry() {
    return stdLandCntry;
  }

  public void setStdLandCntry(String stdLandCntry) {
    this.stdLandCntry = stdLandCntry;
  }

  public String getStdResultCode() {
    return stdResultCode;
  }

  public void setStdResultCode(String stdResultCode) {
    this.stdResultCode = stdResultCode;
  }

  public String getStdResultPostCd() {
    return stdResultPostCd;
  }

  public void setStdResultPostCd(String stdResultPostCd) {
    this.stdResultPostCd = stdResultPostCd;
  }

  public String getStdResultCity() {
    return stdResultCity;
  }

  public void setStdResultCity(String stdResultCity) {
    this.stdResultCity = stdResultCity;
  }

  public String getStdResultProvince() {
    return stdResultProvince;
  }

  public void setStdResultProvince(String stdResultProvince) {
    this.stdResultProvince = stdResultProvince;
  }

  public String getStdResultStreet() {
    return stdResultStreet;
  }

  public void setStdResultStreet(String stdResultStreet) {
    this.stdResultStreet = stdResultStreet;
  }

  public String getStdResultDesc() {
    return stdResultDesc;
  }

  public void setStdResultDesc(String stdResultDesc) {
    this.stdResultDesc = stdResultDesc;
  }

  public String getStdResultText() {
    return stdResultText;
  }

  public void setStdResultText(String stdResultText) {
    this.stdResultText = stdResultText;
  }

  public String getStdResultStatus() {
    return stdResultStatus;
  }

  public void setStdResultStatus(String stdResultStatus) {
    this.stdResultStatus = stdResultStatus;
  }

  public String getStateProvDesc() {
    return stateProvDesc;
  }

  public void setStateProvDesc(String stateProvDesc) {
    this.stateProvDesc = stateProvDesc;
  }

  public String getStdStateCode() {
    return stdStateCode;
  }

  public void setStdStateCode(String stdStateCode) {
    this.stdStateCode = stdStateCode;
  }

  public String getStdAddrTxt2() {
    return stdAddrTxt2;
  }

  public void setStdAddrTxt2(String stdAddrTxt2) {
    this.stdAddrTxt2 = stdAddrTxt2;
  }
}
