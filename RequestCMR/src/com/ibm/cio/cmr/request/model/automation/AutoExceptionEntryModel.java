/**
 * 
 */
package com.ibm.cio.cmr.request.model.automation;

import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

/**
 * @author JeffZAMORA
 *
 */
@JsonIgnoreProperties(
    ignoreUnknown = true)
public class AutoExceptionEntryModel {

  private String cmrIssuingCntry;
  private String custTyp;
  private String custSubTyp;
  private String skipDupChecksIndc;
  private String importDnbInfoIndc;
  private String checkVatIndc;
  private String skipChecksIndc;
  private String skipVerificationIndc;
  private List<String> skipAddressChecks;
  private List<AddressKeyModel> dupAddressChecks;
  private String status;
  private String region;

  public String cleanDupAddressChecks() {
    StringBuilder sb = new StringBuilder();
    for (AddressKeyModel key : this.dupAddressChecks) {
      sb.append(sb.length() > 0 ? "," : "");
      sb.append(key.getCmrType() + "-" + key.getRdcType());
    }
    return sb.toString();
  }

  public String cleanSkipAddressChecks() {
    StringBuilder sb = new StringBuilder();
    for (String key : this.skipAddressChecks) {
      sb.append(sb.length() > 0 ? "," : "");
      sb.append(key);
    }
    return sb.toString();
  }

  public String getCmrIssuingCntry() {
    return cmrIssuingCntry;
  }

  public void setCmrIssuingCntry(String cmrIssuingCntry) {
    this.cmrIssuingCntry = cmrIssuingCntry;
  }

  public String getCustTyp() {
    return custTyp;
  }

  public void setCustTyp(String custTyp) {
    this.custTyp = custTyp;
  }

  public String getCustSubTyp() {
    return custSubTyp;
  }

  public void setCustSubTyp(String custSubTyp) {
    this.custSubTyp = custSubTyp;
  }

  public String getSkipDupChecksIndc() {
    return skipDupChecksIndc;
  }

  public void setSkipDupChecksIndc(String skipDupChecksIndc) {
    this.skipDupChecksIndc = skipDupChecksIndc;
  }

  public String getImportDnbInfoIndc() {
    return importDnbInfoIndc;
  }

  public void setImportDnbInfoIndc(String importDnbInfoIndc) {
    this.importDnbInfoIndc = importDnbInfoIndc;
  }

  public String getCheckVatIndc() {
    return checkVatIndc;
  }

  public void setCheckVatIndc(String checkVatIndc) {
    this.checkVatIndc = checkVatIndc;
  }

  public String getSkipChecksIndc() {
    return skipChecksIndc;
  }

  public void setSkipChecksIndc(String skipChecksIndc) {
    this.skipChecksIndc = skipChecksIndc;
  }

  public List<AddressKeyModel> getDupAddressChecks() {
    return dupAddressChecks;
  }

  public void setDupAddressChecks(List<AddressKeyModel> dupAddressChecks) {
    this.dupAddressChecks = dupAddressChecks;
  }

  public List<String> getSkipAddressChecks() {
    return skipAddressChecks;
  }

  public void setSkipAddressChecks(List<String> skipAddressChecks) {
    this.skipAddressChecks = skipAddressChecks;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getRegion() {
    return region;
  }

  public void setRegion(String region) {
    this.region = region;
  }

  public String getSkipVerificationIndc() {
    return skipVerificationIndc;
  }

  public void setSkipVerificationIndc(String skipVerificationIndc) {
    this.skipVerificationIndc = skipVerificationIndc;
  }

}
