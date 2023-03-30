/**
 * 
 */
package com.ibm.cio.cmr.request.model.auto;

/**
 * @author JeffZAMORA
 * 
 */
public class BrazilV2ReqModel extends BaseV2RequestModel {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  private String vat;
  private String municipalFiscalCode;
  private String taxCode;
  private String email1;
  private String email2;
  private String email3;
  private String govType;
  private String proxiLocnNo;
  private String updateReason;
  private String vatEndUser;
  private String municipalFiscalCodeEndUser;

  private String isuCd;
  private String inacCd;
  private String company;
  private String collectorNameNo;
  private String salesBusOffCd;

  public String getVat() {
    return vat;
  }

  public void setVat(String vat) {
    this.vat = vat;
  }

  public String getTaxCode() {
    return taxCode;
  }

  public void setTaxCode(String taxCode) {
    this.taxCode = taxCode;
  }

  public String getEmail1() {
    return email1;
  }

  public void setEmail1(String email1) {
    this.email1 = email1;
  }

  public String getEmail2() {
    return email2;
  }

  public void setEmail2(String email2) {
    this.email2 = email2;
  }

  public String getEmail3() {
    return email3;
  }

  public void setEmail3(String email3) {
    this.email3 = email3;
  }

  public String getGovType() {
    return govType;
  }

  public void setGovType(String govType) {
    this.govType = govType;
  }

  public String getVatEndUser() {
    return vatEndUser;
  }

  public void setVatEndUser(String vatEndUser) {
    this.vatEndUser = vatEndUser;
  }

  public String getMunicipalFiscalCode() {
    return municipalFiscalCode;
  }

  public void setMunicipalFiscalCode(String municipalFiscalCode) {
    this.municipalFiscalCode = municipalFiscalCode;
  }

  public String getMunicipalFiscalCodeEndUser() {
    return municipalFiscalCodeEndUser;
  }

  public void setMunicipalFiscalCodeEndUser(String municipalFiscalCodeEndUser) {
    this.municipalFiscalCodeEndUser = municipalFiscalCodeEndUser;
  }

  public String getProxiLocnNo() {
    return proxiLocnNo;
  }

  public void setProxiLocnNo(String proxiLocnNo) {
    this.proxiLocnNo = proxiLocnNo;
  }

  public String getUpdateReason() {
    return updateReason;
  }

  public void setUpdateReason(String updateReason) {
    this.updateReason = updateReason;
  }

  public String getIsuCd() {
    return isuCd;
  }

  public void setIsuCd(String isuCd) {
    this.isuCd = isuCd;
  }

  public String getInacCd() {
    return inacCd;
  }

  public void setInacCd(String inacCd) {
    this.inacCd = inacCd;
  }

  public String getCompany() {
    return company;
  }

  public void setCompany(String company) {
    this.company = company;
  }

  public String getCollectorNameNo() {
    return collectorNameNo;
  }

  public void setCollectorNameNo(String collectorNameNo) {
    this.collectorNameNo = collectorNameNo;
  }

  public String getSalesBusOffCd() {
    return salesBusOffCd;
  }

  public void setSalesBusOffCd(String salesBusOffCd) {
    this.salesBusOffCd = salesBusOffCd;
  }

}
