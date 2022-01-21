package com.ibm.cio.cmr.request.model.revivedcmr;

import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.cio.cmr.request.model.BaseModel;

public class RevivedCMRModel extends BaseModel {

  private static final long serialVersionUID = 1L;
  private String issuingCountry;
  private String cmrNo;
  private String cmrState;
  private String county;
  private String gbgId;
  private String bgId;
  private String cmrCount;
  private String ldeRule;
  private String intAcctType;
  private String guDunsNo;
  private String dunsNo;
  private String vat;
  private String finalCoverage;
  private String csoSite;
  private String pccArDept;
  private String mtkgArDept;
  private String mktgDept;
  private String svcArOffice;
  private String isuCd;
  private String clientTier;
  private String isicCd;
  private String subIndustryCd;
  private String usSicmen;

  public String getIssuingCountry() {
    return issuingCountry;
  }

  public void setIssuingCountry(String issuingCountry) {
    this.issuingCountry = issuingCountry;
  }

  public String getCmrNo() {
    return cmrNo;
  }

  public void setCmrNo(String cmrNo) {
    this.cmrNo = cmrNo;
  }

  public String getCmrState() {
    return cmrState;
  }

  public void setCmrState(String cmrState) {
    this.cmrState = cmrState;
  }

  public String getCounty() {
    return county;
  }

  public void setCounty(String county) {
    this.county = county;
  }

  @Override
  public boolean allKeysAssigned() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public String getRecordDescription() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void addKeyParameters(ModelAndView mv) {
    // TODO Auto-generated method stub

  }

  @Override
  public void addKeyParameters(ModelMap map) {
    // TODO Auto-generated method stub

  }

  public String getGbgId() {
    return gbgId;
  }

  public void setGbgId(String gbgId) {
    this.gbgId = gbgId;
  }

  public String getBgId() {
    return bgId;
  }

  public void setBgId(String bgId) {
    this.bgId = bgId;
  }

  public String getCmrCount() {
    return cmrCount;
  }

  public void setCmrCount(String cmrCount) {
    this.cmrCount = cmrCount;
  }

  public String getLdeRule() {
    return ldeRule;
  }

  public void setLdeRule(String ldeRule) {
    this.ldeRule = ldeRule;
  }

  public String getIntAcctType() {
    return intAcctType;
  }

  public void setIntAcctType(String intAcctType) {
    this.intAcctType = intAcctType;
  }

  public String getGuDunsNo() {
    return guDunsNo;
  }

  public void setGuDunsNo(String guDunsNo) {
    this.guDunsNo = guDunsNo;
  }

  public String getDunsNo() {
    return dunsNo;
  }

  public void setDunsNo(String dunsNo) {
    this.dunsNo = dunsNo;
  }

  public String getVat() {
    return vat;
  }

  public void setVat(String vat) {
    this.vat = vat;
  }

  public String getFinalCoverage() {
    return finalCoverage;
  }

  public void setFinalCoverage(String finalCoverage) {
    this.finalCoverage = finalCoverage;
  }

  public String getCsoSite() {
    return csoSite;
  }

  public void setCsoSite(String csoSite) {
    this.csoSite = csoSite;
  }

  public String getPccArDept() {
    return pccArDept;
  }

  public void setPccArDept(String pccArDept) {
    this.pccArDept = pccArDept;
  }

  public String getMtkgArDept() {
    return mtkgArDept;
  }

  public void setMtkgArDept(String mtkgArDept) {
    this.mtkgArDept = mtkgArDept;
  }

  public String getMktgDept() {
    return mktgDept;
  }

  public void setMktgDept(String mktgDept) {
    this.mktgDept = mktgDept;
  }

  public String getSvcArOffice() {
    return svcArOffice;
  }

  public void setSvcArOffice(String svcArOffice) {
    this.svcArOffice = svcArOffice;
  }

  public String getIsuCd() {
    return isuCd;
  }

  public void setIsuCd(String isuCd) {
    this.isuCd = isuCd;
  }

  public String getClientTier() {
    return clientTier;
  }

  public void setClientTier(String clientTier) {
    this.clientTier = clientTier;
  }

  public String getIsicCd() {
    return isicCd;
  }

  public void setIsicCd(String isicCd) {
    this.isicCd = isicCd;
  }

  public String getSubIndustryCd() {
    return subIndustryCd;
  }

  public void setSubIndustryCd(String subIndustryCd) {
    this.subIndustryCd = subIndustryCd;
  }

  public String getUsSicmen() {
    return usSicmen;
  }

  public void setUsSicmen(String usSicmen) {
    this.usSicmen = usSicmen;
  }

}
