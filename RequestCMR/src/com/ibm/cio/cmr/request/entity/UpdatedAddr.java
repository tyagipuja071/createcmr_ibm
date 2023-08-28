/**
 * 
 */
package com.ibm.cio.cmr.request.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;

/**
 * @author Jeffrey Zamora
 * 
 */
@Entity
public class UpdatedAddr implements Serializable {

  private static final long serialVersionUID = 1L;

  @EmbeddedId
  private AddrPK id;

  public AddrPK getId() {
    return id;
  }

  public void setId(AddrPK id) {
    this.id = id;
  }

  @Column(
      name = "CMR_COUNTRY")
  private String cmrCountry;

  @Column(
      name = "SAP_NO")
  private String sapNo;

  @Column(
      name = "CUST_NM1")
  private String custNm1;

  @Column(
      name = "CUST_NM2")
  private String custNm2;

  @Column(
      name = "CUST_NM3")
  private String custNm3;

  @Column(
      name = "CUST_NM4")
  private String custNm4;

  @Column(
      name = "ADDR_TXT")
  private String addrTxt;

  private String city1;

  private String city2;

  @Column(
      name = "STATE_PROV")
  private String stateProv;

  @Column(
      name = "POST_CD")
  private String postCd;

  @Column(
      name = "LAND_CNTRY")
  private String landCntry;

  private String county;

  private String bldg;

  private String floor;

  private String office;

  private String dept;

  @Column(
      name = "PO_BOX")
  private String poBox;

  @Column(
      name = "PO_BOX_CITY")
  private String poBoxCity;

  @Column(
      name = "PO_BOX_POST_CD")
  private String poBoxPostCd;

  @Column(
      name = "CUST_FAX")
  private String custFax;

  @Column(
      name = "CUST_LANG_CD")
  private String custLangCd;

  @Column(
      name = "HW_INSTL_MSTR_FLG")
  private String hwInstlMstrFlg;

  @Column(
      name = "CUST_PHONE")
  private String custPhone;

  @Column(
      name = "TRANSPORT_ZONE")
  private String transportZone;

  private String divn;

  @Column(
      name = "ADDR_TXT_2")
  private String addrTxt2;

  @Column(
      name = "TAX_CD_1")
  private String taxCd1;

  @Column(
      name = "TAX_CD_2")
  private String taxCd2;

  @Column(
      name = "VAT")
  private String vat;

  @Column(
      name = "VAT_IND")
  private String vatInd;

  @Column(
      name = "BILLING_PSTL_ADDR")
  private String billingPstlAddr;

  @Column(
      name = "PAIRED_ADDR_SEQ")
  private String pairedAddrSeq;

  @Column(
      name = "CMR_NUMBER")
  private String cmrNumber;

  @Column(
      name = "CONTACT")
  private String contact;

  @Column(
      name = "TAX_OFFICE")
  private String taxOffice;

  @Column(
      name = "EXT_WALLET_ID")
  private String extWalletId;

  /* Old Fields */
  @Column(
      name = "SAP_NO_OLD")
  private String sapNoOld;

  @Column(
      name = "CUST_NM1_OLD")
  private String custNm1Old;

  @Column(
      name = "CUST_NM2_OLD")
  private String custNm2Old;

  @Column(
      name = "CUST_NM3_OLD")
  private String custNm3Old;

  @Column(
      name = "CUST_NM4_OLD")
  private String custNm4Old;

  @Column(
      name = "ADDR_TXT_OLD")
  private String addrTxtOld;

  @Column(
      name = "CITY1_OLD")
  private String city1Old;

  @Column(
      name = "CITY2_OLD")
  private String city2Old;

  @Column(
      name = "STATE_PROV_OLD")
  private String stateProvOld;

  @Column(
      name = "POST_CD_OLD")
  private String postCdOld;

  @Column(
      name = "LAND_CNTRY_OLD")
  private String landCntryOld;

  @Column(
      name = "COUNTY_OLD")
  private String countyOld;

  @Column(
      name = "BLDG_OLD")
  private String bldgOld;

  @Column(
      name = "FLOOR_OLD")
  private String floorOld;

  @Column(
      name = "OFFICE_OLD")
  private String officeOld;

  @Column(
      name = "DEPT_OLD")
  private String deptOld;

  @Column(
      name = "PO_BOX_OLD")
  private String poBoxOld;

  @Column(
      name = "PO_BOX_CITY_OLD")
  private String poBoxCityOld;

  @Column(
      name = "PO_BOX_POST_CD_OLD")
  private String poBoxPostCdOld;

  @Column(
      name = "CUST_FAX_OLD")
  private String custFaxOld;

  @Column(
      name = "CUST_LANG_CD_OLD")
  private String custLangCdOld;

  @Column(
      name = "HW_INSTL_MSTR_FLG_OLD")
  private String hwInstlMstrFlgOld;

  @Column(
      name = "CUST_PHONE_OLD")
  private String custPhoneOld;

  @Column(
      name = "TRANSPORT_ZONE_OLD")
  private String transportZoneOld;

  @Column(
      name = "DIVN_OLD")
  private String divnOld;

  @Column(
      name = "ADDR_TXT_2_OLD")
  private String addrTxt2Old;

  @Column(
      name = "IMPORT_IND")
  private String importInd;

  @Column(
      name = "TAX_CD_1_OLD")
  private String taxCd1Old;

  @Column(
      name = "TAX_CD_2_OLD")
  private String taxCd2Old;

  @Column(
      name = "VAT_OLD")
  private String vatOld;

  private String vatIndOld;

  @Column(
      name = "BILLING_PSTL_ADDR_OLD")
  private String billingPstlAddrOld;

  @Column(
      name = "CONTACT_OLD")
  private String contactOld;

  @Column(
      name = "TAX_OFFICE_OLD")
  private String taxOfficeOld;

  @Column(
      name = "EXT_WALLET_ID_OLD")
  private String extWalletIdOld;

  @Column(
      name = "ROL")
  private String rol;

  @Column(
      name = "ROL_OLD")
  private String rolOld;

  public String getSapNo() {
    return sapNo;
  }

  public void setSapNo(String sapNo) {
    this.sapNo = sapNo;
  }

  public String getCustNm1() {
    return custNm1;
  }

  public void setCustNm1(String custNm1) {
    this.custNm1 = custNm1;
  }

  public String getCustNm2() {
    return custNm2;
  }

  public void setCustNm2(String custNm2) {
    this.custNm2 = custNm2;
  }

  public String getCustNm3() {
    return custNm3;
  }

  public void setCustNm3(String custNm3) {
    this.custNm3 = custNm3;
  }

  public String getCustNm4() {
    return custNm4;
  }

  public void setCustNm4(String custNm4) {
    this.custNm4 = custNm4;
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

  public String getCity2() {
    return city2;
  }

  public void setCity2(String city2) {
    this.city2 = city2;
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

  public String getCounty() {
    return county;
  }

  public void setCounty(String county) {
    this.county = county;
  }

  public String getBldg() {
    return bldg;
  }

  public void setBldg(String bldg) {
    this.bldg = bldg;
  }

  public String getFloor() {
    return floor;
  }

  public void setFloor(String floor) {
    this.floor = floor;
  }

  public String getOffice() {
    return office;
  }

  public String getTaxOfficeOld() {
    return taxOfficeOld;
  }

  public void setTaxOfficeOld(String taxOfficeOld) {
    this.taxOfficeOld = taxOfficeOld;
  }

  public void setOffice(String office) {
    this.office = office;
  }

  public String getDept() {
    return dept;
  }

  public void setDept(String dept) {
    this.dept = dept;
  }

  public String getPoBox() {
    return poBox;
  }

  public void setPoBox(String poBox) {
    this.poBox = poBox;
  }

  public String getPoBoxCity() {
    return poBoxCity;
  }

  public void setPoBoxCity(String poBoxCity) {
    this.poBoxCity = poBoxCity;
  }

  public String getPoBoxPostCd() {
    return poBoxPostCd;
  }

  public void setPoBoxPostCd(String poBoxPostCd) {
    this.poBoxPostCd = poBoxPostCd;
  }

  public String getCustFax() {
    return custFax;
  }

  public void setCustFax(String custFax) {
    this.custFax = custFax;
  }

  public String getCustLangCd() {
    return custLangCd;
  }

  public void setCustLangCd(String custLangCd) {
    this.custLangCd = custLangCd;
  }

  public String getHwInstlMstrFlg() {
    return hwInstlMstrFlg;
  }

  public void setHwInstlMstrFlg(String hwInstlMstrFlg) {
    this.hwInstlMstrFlg = hwInstlMstrFlg;
  }

  public String getCustPhone() {
    return custPhone;
  }

  public void setCustPhone(String custPhone) {
    this.custPhone = custPhone;
  }

  public String getTransportZone() {
    return transportZone;
  }

  public void setTransportZone(String transportZone) {
    this.transportZone = transportZone;
  }

  public String getSapNoOld() {
    return sapNoOld;
  }

  public void setSapNoOld(String sapNoOld) {
    this.sapNoOld = sapNoOld;
  }

  public String getCustNm1Old() {
    return custNm1Old;
  }

  public void setCustNm1Old(String custNm1Old) {
    this.custNm1Old = custNm1Old;
  }

  public String getCustNm2Old() {
    return custNm2Old;
  }

  public void setCustNm2Old(String custNm2Old) {
    this.custNm2Old = custNm2Old;
  }

  public String getCustNm3Old() {
    return custNm3Old;
  }

  public void setCustNm3Old(String custNm3Old) {
    this.custNm3Old = custNm3Old;
  }

  public String getCustNm4Old() {
    return custNm4Old;
  }

  public void setCustNm4Old(String custNm4Old) {
    this.custNm4Old = custNm4Old;
  }

  public String getAddrTxtOld() {
    return addrTxtOld;
  }

  public void setAddrTxtOld(String addrTxtOld) {
    this.addrTxtOld = addrTxtOld;
  }

  public String getCity1Old() {
    return city1Old;
  }

  public void setCity1Old(String city1Old) {
    this.city1Old = city1Old;
  }

  public String getCity2Old() {
    return city2Old;
  }

  public void setCity2Old(String city2Old) {
    this.city2Old = city2Old;
  }

  public String getStateProvOld() {
    return stateProvOld;
  }

  public void setStateProvOld(String stateProvOld) {
    this.stateProvOld = stateProvOld;
  }

  public String getPostCdOld() {
    return postCdOld;
  }

  public void setPostCdOld(String postCdOld) {
    this.postCdOld = postCdOld;
  }

  public String getLandCntryOld() {
    return landCntryOld;
  }

  public void setLandCntryOld(String landCntryOld) {
    this.landCntryOld = landCntryOld;
  }

  public String getCountyOld() {
    return countyOld;
  }

  public void setCountyOld(String countyOld) {
    this.countyOld = countyOld;
  }

  public String getBldgOld() {
    return bldgOld;
  }

  public void setBldgOld(String bldgOld) {
    this.bldgOld = bldgOld;
  }

  public String getFloorOld() {
    return floorOld;
  }

  public void setFloorOld(String floorOld) {
    this.floorOld = floorOld;
  }

  public String getOfficeOld() {
    return officeOld;
  }

  public void setOfficeOld(String officeOld) {
    this.officeOld = officeOld;
  }

  public String getDeptOld() {
    return deptOld;
  }

  public void setDeptOld(String deptOld) {
    this.deptOld = deptOld;
  }

  public String getPoBoxOld() {
    return poBoxOld;
  }

  public void setPoBoxOld(String poBoxOld) {
    this.poBoxOld = poBoxOld;
  }

  public String getPoBoxCityOld() {
    return poBoxCityOld;
  }

  public void setPoBoxCityOld(String poBoxCityOld) {
    this.poBoxCityOld = poBoxCityOld;
  }

  public String getPoBoxPostCdOld() {
    return poBoxPostCdOld;
  }

  public void setPoBoxPostCdOld(String poBoxPostCdOld) {
    this.poBoxPostCdOld = poBoxPostCdOld;
  }

  public String getCustFaxOld() {
    return custFaxOld;
  }

  public void setCustFaxOld(String custFaxOld) {
    this.custFaxOld = custFaxOld;
  }

  public String getCustLangCdOld() {
    return custLangCdOld;
  }

  public void setCustLangCdOld(String custLangCdOld) {
    this.custLangCdOld = custLangCdOld;
  }

  public String getHwInstlMstrFlgOld() {
    return hwInstlMstrFlgOld;
  }

  public void setHwInstlMstrFlgOld(String hwInstlMstrFlgOld) {
    this.hwInstlMstrFlgOld = hwInstlMstrFlgOld;
  }

  public String getCustPhoneOld() {
    return custPhoneOld;
  }

  public void setCustPhoneOld(String custPhoneOld) {
    this.custPhoneOld = custPhoneOld;
  }

  public String getTransportZoneOld() {
    return transportZoneOld;
  }

  public void setTransportZoneOld(String transportZoneOld) {
    this.transportZoneOld = transportZoneOld;
  }

  public String getCmrCountry() {
    return cmrCountry;
  }

  public void setCmrCountry(String cmrCountry) {
    this.cmrCountry = cmrCountry;
  }

  public String getDivn() {
    return divn;
  }

  public void setDivn(String divn) {
    this.divn = divn;
  }

  public String getDivnOld() {
    return divnOld;
  }

  public void setDivnOld(String divnOld) {
    this.divnOld = divnOld;
  }

  public String getAddrTxt2() {
    return addrTxt2;
  }

  public void setAddrTxt2(String addrTxt2) {
    this.addrTxt2 = addrTxt2;
  }

  public String getAddrTxt2Old() {
    return addrTxt2Old;
  }

  public void setAddrTxt2Old(String addrTxt2Old) {
    this.addrTxt2Old = addrTxt2Old;
  }

  public String getImportInd() {
    return importInd;
  }

  public void setImportInd(String importInd) {
    this.importInd = importInd;
  }

  public String getTaxCd1() {
    return taxCd1;
  }

  public void setTaxCd1(String taxCd1) {
    this.taxCd1 = taxCd1;
  }

  public String getTaxCd2() {
    return taxCd2;
  }

  public void setTaxCd2(String taxCd2) {
    this.taxCd2 = taxCd2;
  }

  public String getVat() {
    return vat;
  }

  public void setVat(String vat) {
    this.vat = vat;
  }

  public String getVatInd() {
    return vatInd;
  }

  public void setVatInd(String vatInd) {
    this.vatInd = vatInd;
  }

  public String getTaxCd1Old() {
    return taxCd1Old;
  }

  public void setTaxCd1Old(String taxCd1Old) {
    this.taxCd1Old = taxCd1Old;
  }

  public String getTaxCd2Old() {
    return taxCd2Old;
  }

  public void setTaxCd2Old(String taxCd2Old) {
    this.taxCd2Old = taxCd2Old;
  }

  public String getVatOld() {
    return vatOld;
  }

  public void setVatOld(String vatOld) {
    this.vatOld = vatOld;
  }

  public String getVatIndOld() {
    return vatIndOld;
  }

  public void setVatIndOld(String vatIndOld) {
    this.vatIndOld = vatIndOld;
  }

  public String getBillingPstlAddr() {
    return billingPstlAddr;
  }

  public void setBillingPstlAddr(String billingPstlAddr) {
    this.billingPstlAddr = billingPstlAddr;
  }

  public String getBillingPstlAddrOld() {
    return billingPstlAddrOld;
  }

  public void setBillingPstlAddrOld(String billingPstlAddrOld) {
    this.billingPstlAddrOld = billingPstlAddrOld;
  }

  public String getContact() {
    return contact;
  }

  public void setContact(String contact) {
    this.contact = contact;
  }

  public String getContactOld() {
    return contactOld;
  }

  public void setContactOld(String contactOld) {
    this.contactOld = contactOld;
  }

  public String getPairedAddrSeq() {
    return pairedAddrSeq;
  }

  public void setPairedAddrSeq(String pairedAddrSeq) {
    this.pairedAddrSeq = pairedAddrSeq;
  }

  public String getCmrNumber() {
    return cmrNumber;
  }

  public void setCmrNumber(String cmrNumber) {
    this.cmrNumber = cmrNumber;
  }

  public String getTaxOffice() {
    return taxOffice;
  }

  public void setTaxOffice(String taxOffice) {
    this.taxOffice = taxOffice;
  }

  public String getExtWalletId() {
    return extWalletId;
  }

  public void setExtWalletId(String extWalletId) {
    this.extWalletId = extWalletId;
  }

  public String getExtWalletIdOld() {
    return extWalletIdOld;
  }

  public void setExtWalletIdOld(String extWalletIdOld) {
    this.extWalletIdOld = extWalletIdOld;
  }

  public String getRol() {
    return rol;
  }

  public void setRol(String rol) {
    this.rol = rol;
  }

  public String getRolOld() {
    return rolOld;
  }

  public void setRolOld(String rolOld) {
    this.rolOld = rolOld;
  }

}
