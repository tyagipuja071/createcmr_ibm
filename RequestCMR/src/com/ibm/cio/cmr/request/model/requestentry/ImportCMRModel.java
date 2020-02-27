package com.ibm.cio.cmr.request.model.requestentry;

/**
 * package com.ibm.cio.cmrsearch.web.model;
 * 
 * /**
 * 
 * @author Jeffrey Zamora
 * 
 */
public class ImportCMRModel {

  private String cmrNum;
  private long reqId;
  private String cmrIssuingCntry;
  private String searchIssuingCntry;
  private String system;
  private String productString;
  private String addrType;
  private String addrSeq;
  private boolean prospect;
  private boolean skipAddress;
  private boolean addressOnly;
  private String quickSearchData;

  public String getCmrNum() {
    return cmrNum;
  }

  public void setCmrNum(String cmrNum) {
    this.cmrNum = cmrNum;
  }

  public long getReqId() {
    return reqId;
  }

  public void setReqId(long reqId) {
    this.reqId = reqId;
  }

  public String getCmrIssuingCntry() {
    return cmrIssuingCntry;
  }

  public void setCmrIssuingCntry(String cmrIssuingCntry) {
    this.cmrIssuingCntry = cmrIssuingCntry;
  }

  public String getSystem() {
    return system;
  }

  public void setSystem(String system) {
    this.system = system;
  }

  public String getProductString() {
    return productString;
  }

  public void setProductString(String productString) {
    this.productString = productString;
  }

  public boolean isProspect() {
    return prospect;
  }

  public void setProspect(boolean prospect) {
    this.prospect = prospect;
  }

  public String getSearchIssuingCntry() {
    return searchIssuingCntry;
  }

  public void setSearchIssuingCntry(String searchIssuingCntry) {
    this.searchIssuingCntry = searchIssuingCntry;
  }

  public boolean isSkipAddress() {
    return skipAddress;
  }

  public void setSkipAddress(boolean skipAddress) {
    this.skipAddress = skipAddress;
  }

  public String getAddrType() {
    return addrType;
  }

  public void setAddrType(String addrType) {
    this.addrType = addrType;
  }

  public boolean isAddressOnly() {
    return addressOnly;
  }

  public void setAddressOnly(boolean addressOnly) {
    this.addressOnly = addressOnly;
  }

  public String getAddrSeq() {
    return addrSeq;
  }

  public void setAddrSeq(String addrSeq) {
    this.addrSeq = addrSeq;
  }

  public String getQuickSearchData() {
    return quickSearchData;
  }

  public void setQuickSearchData(String quickSearchData) {
    this.quickSearchData = quickSearchData;
  }
}
