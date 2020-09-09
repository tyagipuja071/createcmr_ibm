/**
 * 
 */
package com.ibm.cio.cmr.request.model.legacy;

import java.util.ArrayList;
import java.util.List;

/**
 * Model for Legacy Search page
 * 
 * @author JeffZAMORA
 *
 */
public class LegacySearchModel {

  private String sofCntryCode;
  private String customerNo;
  private String realCtyCd;
  private String status;
  private String abbrevNm;
  private String abbrevLocn;
  private String locNo;
  private String mrcCd;
  private String modeOfPayment;
  private String isuCd;
  private String creditCd;
  private String taxCd;
  private String sbo;
  private String salesRepNo;
  private String enterpriseNo;
  private String collectionCd;
  private String embargoCd;
  private String isicCd;
  private String inacCd;
  private String vat;

  private String addressLinePrimary;
  private String addressLineSecondary;
  private String name;
  private String street;
  private String city;
  private String zipCode;

  private String createTsFrom;
  private String createTsTo;
  private String updateTsFrom;
  private String updateTsTo;

  private List<String> addressUses = new ArrayList<String>();

  private int recCount;

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

  public String getLocNo() {
    return locNo;
  }

  public void setLocNo(String locNo) {
    this.locNo = locNo;
  }

  public String getMrcCd() {
    return mrcCd;
  }

  public void setMrcCd(String mrcCd) {
    this.mrcCd = mrcCd;
  }

  public String getModeOfPayment() {
    return modeOfPayment;
  }

  public void setModeOfPayment(String modeOfPayment) {
    this.modeOfPayment = modeOfPayment;
  }

  public String getIsuCd() {
    return isuCd;
  }

  public void setIsuCd(String isuCd) {
    this.isuCd = isuCd;
  }

  public String getCreditCd() {
    return creditCd;
  }

  public void setCreditCd(String creditCd) {
    this.creditCd = creditCd;
  }

  public String getTaxCd() {
    return taxCd;
  }

  public void setTaxCd(String taxCd) {
    this.taxCd = taxCd;
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

  public String getEnterpriseNo() {
    return enterpriseNo;
  }

  public void setEnterpriseNo(String enterpriseNo) {
    this.enterpriseNo = enterpriseNo;
  }

  public String getCollectionCd() {
    return collectionCd;
  }

  public void setCollectionCd(String collectionCd) {
    this.collectionCd = collectionCd;
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

  public String getAddressLinePrimary() {
    return addressLinePrimary;
  }

  public void setAddressLinePrimary(String addressLinePrimary) {
    this.addressLinePrimary = addressLinePrimary;
  }

  public String getAddressLineSecondary() {
    return addressLineSecondary;
  }

  public void setAddressLineSecondary(String addressLineSecondary) {
    this.addressLineSecondary = addressLineSecondary;
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

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public int getRecCount() {
    return recCount;
  }

  public void setRecCount(int recCount) {
    this.recCount = recCount;
  }

  public List<String> getAddressUses() {
    return addressUses;
  }

  public void setAddressUses(List<String> addressUses) {
    this.addressUses = addressUses;
  }

  public String getCreateTsFrom() {
    return createTsFrom;
  }

  public void setCreateTsFrom(String createTsFrom) {
    this.createTsFrom = createTsFrom;
  }

  public String getCreateTsTo() {
    return createTsTo;
  }

  public void setCreateTsTo(String createTsTo) {
    this.createTsTo = createTsTo;
  }

  public String getUpdateTsFrom() {
    return updateTsFrom;
  }

  public void setUpdateTsFrom(String updateTsFrom) {
    this.updateTsFrom = updateTsFrom;
  }

  public String getUpdateTsTo() {
    return updateTsTo;
  }

  public void setUpdateTsTo(String updateTsTo) {
    this.updateTsTo = updateTsTo;
  }

}
