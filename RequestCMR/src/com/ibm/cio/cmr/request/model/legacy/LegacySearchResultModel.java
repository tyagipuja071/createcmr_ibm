/**
 * 
 */
package com.ibm.cio.cmr.request.model.legacy;

import java.util.ArrayList;
import java.util.List;

/**
 * @author JeffZAMORA
 *
 */
public class LegacySearchResultModel {

  private String sofCntryCode;
  private String customerNo;
  private String realCtyCd;
  private String status;
  private String abbrevNm;
  private String abbrevLocn;
  private String isuCd;
  private String sbo;
  private String salesRepNo;
  private String embargoCd;
  private String isicCd;
  private String inacCd;
  private String vat;

  private String addrNo;
  private String addrLine1;
  private String addrLine2;
  private String addrLine3;
  private String addrLine4;
  private String addrLine5;
  private String addrLine6;
  private String name;
  private String street;
  private String city;
  private String zipCode;

  private List<String> addressUses = new ArrayList<String>();

  public String getSofCntryCode() {
    return sofCntryCode;
  }

  public void setSofCntryCode(String sofCntryCode) {
    this.sofCntryCode = sofCntryCode;
  }

  public String getCustomerNo() {
    return customerNo;
  }

  public void setCustomerNo(String customerNo) {
    this.customerNo = customerNo;
  }

  public String getRealCtyCd() {
    return realCtyCd;
  }

  public void setRealCtyCd(String realCtyCd) {
    this.realCtyCd = realCtyCd;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getAbbrevNm() {
    return abbrevNm;
  }

  public void setAbbrevNm(String abbrevNm) {
    this.abbrevNm = abbrevNm;
  }

  public String getAbbrevLocn() {
    return abbrevLocn;
  }

  public void setAbbrevLocn(String abbrevLocn) {
    this.abbrevLocn = abbrevLocn;
  }

  public String getIsuCd() {
    return isuCd;
  }

  public void setIsuCd(String isuCd) {
    this.isuCd = isuCd;
  }

  public String getSbo() {
    return sbo;
  }

  public void setSbo(String sbo) {
    this.sbo = sbo;
  }

  public String getSalesRepNo() {
    return salesRepNo;
  }

  public void setSalesRepNo(String salesRepNo) {
    this.salesRepNo = salesRepNo;
  }

  public String getEmbargoCd() {
    return embargoCd;
  }

  public void setEmbargoCd(String embargoCd) {
    this.embargoCd = embargoCd;
  }

  public String getIsicCd() {
    return isicCd;
  }

  public void setIsicCd(String isicCd) {
    this.isicCd = isicCd;
  }

  public String getInacCd() {
    return inacCd;
  }

  public void setInacCd(String inacCd) {
    this.inacCd = inacCd;
  }

  public String getVat() {
    return vat;
  }

  public void setVat(String vat) {
    this.vat = vat;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getStreet() {
    return street;
  }

  public void setStreet(String street) {
    this.street = street;
  }

  public String getCity() {
    return city;
  }

  public void setCity(String city) {
    this.city = city;
  }

  public String getZipCode() {
    return zipCode;
  }

  public void setZipCode(String zipCode) {
    this.zipCode = zipCode;
  }

  public String getAddrLine1() {
    return addrLine1;
  }

  public void setAddrLine1(String addrLine1) {
    this.addrLine1 = addrLine1;
  }

  public String getAddrLine2() {
    return addrLine2;
  }

  public void setAddrLine2(String addrLine2) {
    this.addrLine2 = addrLine2;
  }

  public String getAddrLine3() {
    return addrLine3;
  }

  public void setAddrLine3(String addrLine3) {
    this.addrLine3 = addrLine3;
  }

  public String getAddrLine4() {
    return addrLine4;
  }

  public void setAddrLine4(String addrLine4) {
    this.addrLine4 = addrLine4;
  }

  public String getAddrLine5() {
    return addrLine5;
  }

  public void setAddrLine5(String addrLine5) {
    this.addrLine5 = addrLine5;
  }

  public String getAddrLine6() {
    return addrLine6;
  }

  public void setAddrLine6(String addrLine6) {
    this.addrLine6 = addrLine6;
  }

  public List<String> getAddressUses() {
    return addressUses;
  }

  public void setAddressUses(List<String> addressUses) {
    this.addressUses = addressUses;
  }

  public String getAddrNo() {
    return addrNo;
  }

  public void setAddrNo(String addrNo) {
    this.addrNo = addrNo;
  }

}
