/**
 * 
 */
package com.ibm.cio.cmr.request.model.window;

import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.Data;

/**
 * @author Jeffrey Zamora
 * 
 */
public class RequestSummaryModel {

  private Admin admin = new Admin();
  private Data data = new Data();
  private Addr addr = new Addr();
  private String country;
  private String processingDesc;

  private String landedcountry;
  private String stateprovdesc;
  private String countyDesc;
  private String isicDesc;

  private String addrtypetxt;

  private boolean othraddrexist;

  private String cnCity;
  private String cnAddrTxt;
  private String cnAddrTxt2;
  private String cnCustName1;
  private String cnCustName2;
  private String cnDistrict;
  private String cnInterAddrKey;
  private String cnCustContNm;
  private String cnCustContJobTitle;
  private String cnCustContPhone2;
  private String proxyLocn;
  private String proxyLocnDesc;

  public Admin getAdmin() {
    return admin;
  }

  public void setAdmin(Admin admin) {
    this.admin = admin;
  }

  public Data getData() {
    return data;
  }

  public void setData(Data data) {
    this.data = data;
  }

  public String getCountry() {
    return country;
  }

  public void setCountry(String country) {
    this.country = country;
  }

  public Addr getAddr() {
    return addr;
  }

  public void setAddr(Addr addr) {
    this.addr = addr;
  }

  public String getLandedcountry() {
    return landedcountry;
  }

  public void setLandedcountry(String landedcountry) {
    this.landedcountry = landedcountry;
  }

  public String getStateprovdesc() {
    return stateprovdesc;
  }

  public void setStateprovdesc(String stateprovdesc) {
    this.stateprovdesc = stateprovdesc;
  }

  public String getAddrtypetxt() {
    return addrtypetxt;
  }

  public void setAddrtypetxt(String addrtypetxt) {
    this.addrtypetxt = addrtypetxt;
  }

  public boolean isOthraddrexist() {
    return othraddrexist;
  }

  public void setOthraddrexist(boolean othraddrexist) {
    this.othraddrexist = othraddrexist;
  }

  public String getCountyDesc() {
    return countyDesc;
  }

  public void setCountyDesc(String countyDesc) {
    this.countyDesc = countyDesc;
  }

  public String getProcessingDesc() {
    return processingDesc;
  }

  public void setProcessingDesc(String processingDesc) {
    this.processingDesc = processingDesc;
  }

  public String getIsicDesc() {
    return isicDesc;
  }

  public void setIsicDesc(String isicDesc) {
    this.isicDesc = isicDesc;
  }

  public String getCnAddrTxt() {
    return cnAddrTxt;
  }

  public void setCnAddrTxt(String cnAddrTxt) {
    this.cnAddrTxt = cnAddrTxt;
  }

  public String getCnAddrTxt2() {
    return cnAddrTxt2;
  }

  public void setCnAddrTxt2(String cnAddrTxt2) {
    this.cnAddrTxt2 = cnAddrTxt2;
  }

  public String getCnCustName1() {
    return cnCustName1;
  }

  public void setCnCustName1(String cnCustName1) {
    this.cnCustName1 = cnCustName1;
  }

  public String getCnCustName2() {
    return cnCustName2;
  }

  public void setCnCustName2(String cnCustName2) {
    this.cnCustName2 = cnCustName2;
  }

  public String getCnDistrict() {
    return cnDistrict;
  }

  public void setCnDistrict(String cnDistrict) {
    this.cnDistrict = cnDistrict;
  }

  public String getCnCustContNm() {
    return cnCustContNm;
  }

  public void setCnCustContNm(String cnCustContNm) {
    this.cnCustContNm = cnCustContNm;
  }

  public String getCnCustContJobTitle() {
    return cnCustContJobTitle;
  }

  public void setCnCustContJobTitle(String cnCustContJobTitle) {
    this.cnCustContJobTitle = cnCustContJobTitle;
  }

  public String getCnCustContPhone2() {
    return cnCustContPhone2;
  }

  public void setCnCustContPhone2(String cnCustContPhone2) {
    this.cnCustContPhone2 = cnCustContPhone2;
  }

  public String getCnCity() {
    return cnCity;
  }

  public void setCnCity(String cnCity) {
    this.cnCity = cnCity;
  }

  public String getProxyLocn() {
    return proxyLocn;
  }

  public void setProxyLocn(String proxyLocn) {
    this.proxyLocn = proxyLocn;
  }

  public String getProxyLocnDesc() {
    return proxyLocnDesc;
  }

  public void setProxyLocnDesc(String proxyLocnDesc) {
    this.proxyLocnDesc = proxyLocnDesc;
  }

}
