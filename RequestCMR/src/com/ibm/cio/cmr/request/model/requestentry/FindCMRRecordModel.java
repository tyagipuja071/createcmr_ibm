package com.ibm.cio.cmr.request.model.requestentry;

import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import com.ibm.cio.cmr.request.CmrConstants;

@JsonIgnoreProperties(
    ignoreUnknown = true)
public class FindCMRRecordModel implements Serializable, Comparable<FindCMRRecordModel> {

  private static final long serialVersionUID = 1L;
  private String cmrName;
  private String cmrNum;
  private String cmrIssueCategory;
  private String cmrOwner;
  private String cmrCity;
  private String cmrState;
  private String cmrCountry;
  private String[] cmrAddrTypes;
  private String cmrAddrType;
  private String cmrPostalCode;
  private int cmrResultRows;
  private String cmrCoverage;
  private String extWalletId;
  // base cov
  private String cmrBaseCoverage;
  private String cmrRevenue;
  private float cmrRevenueNumber;
  private String cmrSapNumber;
  private String isuCode;
  private String isuDescription;

  // new Coverage Client fields (NDA / Route120 Requirements)
  private String cmrCovClientType;
  private String cmrCovClientSubType;
  private String cmrCovClientTypeDesc;
  private String cmrCovClientSubTypeDesc;

  /* PROD new properties */
  private String cmrCountryLanded;
  private String cmrCity2;
  private String cmrCounty;
  private String cmrShortName;
  private String cmrOrderBlock;
  private String cmrVat;
  private String cmrVatInd;
  private String cmrBusinessReg;
  private String cmrClientId;
  private String cmrEnterpriseNumber;
  private String cmrDuns;
  private String cmrTradestyle;
  private String cmrIsic;
  private String cmrClass;
  private String cmrAffiliate;
  private String cmrIsu;
  private String cmrBuyingGroup;
  private String cmrGlobalBuyingGroup;
  private String cmrTier;
  private String cmrIssuedBy;
  private String cmrIntlName;
  private String cmrIntlAddress;
  private String cmrIntlCity1;
  private String cmrIntlCity2;
  private String cmrParent;
  private String cmrGuNumber;
  private String cmrDuNumber;
  private String cmrProspect;
  // Intl name12 and intl name34
  private String cmrIntlName12;
  private String cmrIntlName34;
  private String cmrName2;
  private String cmrIsuCodeAndDesc;
  private String cmrDomClient;
  private String cmrGlobClient;

  private String cmrIssuedByDesc;
  private String cmrStateDesc;
  private String cmrCountryLandedDesc;
  private String cmrSubIndustry;
  private String cmrSubIndustryDesc;
  private String cmrIsicDesc;
  private String cmrBuyingGroupDesc;
  private String cmrGlobalBuyingGroupDesc;
  private String cmrClassDesc;
  private String cmrIsuDesc;
  private String cmrTierDesc;
  private String cmrInac;
  private String cmrInacDesc;
  private String cmrCompanyNo;
  private String cmrSortl;
  private String cmrDomClientDesc;
  private String cmrGlobClientDesc;
  private String cmrOrderBlockDesc;

  // added for details integration
  private String contractParams;
  private String cmrAddrTypeCode;
  private String cmrIntlName1;
  private String cmrIntlName2;
  private String cmrIntlName3;
  private String cmrIntlName4;
  private String cmrCoverageName;
  private String cmrBaseCoverageName;
  private String cmrName1Plain;
  private String cmrName2Plain;
  private String cmrName3;
  private String cmrName4;
  private String cmrInacType;
  private String cmrDelegated;
  private String cmrSitePartyID;
  private String cmrRdcCreateDate;
  private String cmrCountyCode;
  private String cmrCapIndicator;

  // request cmr fields
  private String cmrCustPhone; // TELF1
  private String cmrCustFax; // TELFX
  private String cmrPrefLang; // SPRAS
  private String cmrLocalTax2; // STCD2
  private String cmrSensitiveFlag; // BEGRU
  private String cmrTransportZone; // LZONE
  private String cmrPOBox; // PFACH
  private String cmrPOBoxCity; // PFORT
  private String cmrPOBoxPostCode; // PSTL2
  private String cmrPpsceid; // BRAN3
  private String cmrBldg; // BUILDING
  private String cmrFloor; // FLOOR
  private String cmrOffice; // OFFICE
  private String cmrDept; // DEPARTMENT
  private String cmrBPRelType; // BP_REL_TYPE_CD
  private String cmrMembLevel; // BP_MBR_LVL_TYPE

  /* added for LH requirement, 797865 */
  private String cmrClientType;

  // added for GEO indicator
  private String cmrGOEIndicator;

  // changed from original field cmr Addr Type Txt
  private String cmrStreetAddress;

  // DnB specicifi
  private String cmrStreet;

  /* 814271 */
  private String cmrGeoLocCd;
  private String cmrGeoLocDesc;

  /* 825446 - add Global Ultimate Client ID / Name */
  private String cmrGUClientId;
  private String cmrGUClientName;

  /* 825444 - add SADR Lang Code (SPRAS) */
  private String cmrIntlLangCd;
  private String cmrIntlSubLangCd;
  private String cmrIntlSubLangDesc;

  private String cmrOtherIntlBusinessName;
  private String cmrOtherIntlAddress;
  private String cmrOtherIntlCity1;
  private String cmrOtherIntlCity2;
  private String cmrOtherIntlLangCd;
  private String cmrOtherIntlSubLangCd;
  private String cmrOtherIntlSubLangDesc;

  private String cmrLeadClientRepName;
  private String cmrLeadClientRepNotesId;
  // field for LDE
  private String cmrLde;

  /* 879648 - show business name and lang code in the results */
  private String cmrBusNm;
  private String cmrBusNmLangCd;
  private String cmrBusNmLangDesc;
  private String cmrBusNmNative;
  private String cmrBusNmNativeLangCd;
  private String cmrBusNmNativeLangDesc;

  /* 960371 - Add Industry Code and Name */
  private String cmrIndustryCd;
  private String cmrIndustryName;
  private float searchScore;

  /* SaaS changes */
  private String cmrLite;
  private String cmrCoverageEligible;

  private String cmrAddrSeq;

  private String cmrStreetAddressCont;

  private String cmrTaxNumber;
  private String cmrTaxOffice;

  private String transAddrNo;

  private String parentCMRNo;

  /* JP CRIS changes */
  private String companyNo;
  private String estabNo;
  private String locationNo;
  private String jsic;
  private String csbo;
  private String sbo;
  private int companySize;
  private String estabFuncCd;
  private String educAllowanceGrp;
  private String cRSCode;
  private String SR;
  private String billingProcessCode;
  private String invoiceSplitCode;
  private String creditToCustNo;
  private String siInd;
  private String iinInd;
  private String leasingCompanyIndc;
  private String channelCd;
  private String creditCd;
  private String valueAddRem;
  private String icmsInd;
  private String csDiv;
  private String oemInd;
  private String repTeamMemberNo;
  private String salesTeamCd;
  private String govOfficeDivCode;
  private String companyCd;
  private String nameKanji;
  private String sboSub;
  private String attach;
  private String custGroup;
  private String custClass;

  // Italy Legacy Fields
  private String hwSvcsRepTeamNo;
  private String email2;
  private String email3;

  // new fields from FindCMR
  private String usCmrCsoSite;
  private String usCmrMktgArDept;
  private String usCmrRestrictTo;
  private String usCmrBpAccountType;

  private String militaryFlag;

  // new canada fields
  private String cmrSellBoGrp;
  private String cmrInstlBoGrp;
  private String cmrTaxCertStatus;
  private String cmrAddrUse;
  private String cmrDataLine;
  private String cmrQstNo;
  private String cmrTaxExInd;
  private String cmrLicNo;
  private String cmrTaxExemptReas;
  private String cmrLeasingInd;
  private String cmrPurOrdNo;
  private String cmrEstabFnInd;
  private String cmrSellBoNum;
  private String cmrInstlBoNum;
  private String cmrAccRecvBo;
  private String cmrPymtTerms;
  private String cmrBillPlnTyp;
  private String cmrNoInvc;
  private String cmrEnggBoGrp;
  private String cmrLtPymntInd;
  private String cmrCustCreditCode;

  // CREATCMR-5116 new US sunset fields
  private String usCmrMktgArea;
  private String usCmrMktgDept;
  private String usCmrOrgMktgBrOfc;
  private String usCmrPccArBo;
  private String usCmrPccMktgBo;
  private String usCmrSvcArOfc;
  private String usCmrSvcLgsysOfc;
  private String usCmrSvcSmsysOfc;
  private String usCmrSvcOthOfc;
  private String usCmrSvcMasterBo;
  private String usCmrBoDivision;
  private String usCmrBoTradingArea;
  private String usCmrNonIbmCompInd;
  private String usCmrOemInd;
  private String usCmrBpAbbrevNm;
  private String usCmrEnterpriseNm;
  private String usCmrEnterpriseType;
  private String usCmrTaxType1;
  private String usCmrTaxType2;
  private String usCmrTaxType3;
  private String usCmrTaxClass1;
  private String usCmrTaxClass2;
  private String usCmrTaxClass3;
  private String usCmrIccTaxExempt;
  private String usCmrIccTaxClass;
  private String usCmrTaxCertStat1;
  private String usCmrTaxCertStat2;
  private String usCmrTaxCertStat3;
  private String usCmrSccCd;
  private String usCmrOcl;
  private String usCmrEducAllowStat;
  private String usCmrRestrictToCd;
  private String usCmrRestrictToDesc;
  private String usCmrBpCd;
  private String usCmrBpNm;
  private String usCmrBpType;

  private String usCmrCompanyNm;

  // LA fields
  private String cmrMexFiscalRegime;
  private String cmrMexBillingName;
  private String cmrProxiLocn;
  private String cmrCollBo;
  private String cmrCollectorNo;
  private String cmrFiscalCd;
  private String cmrStxlTxtVal;

  public String getCmrStxlTxtVal() {
    return cmrStxlTxtVal;
  }

  public void setCmrStxlTxtVal(String cmrStxlTxtVal) {
    this.cmrStxlTxtVal = cmrStxlTxtVal;
  }

  public String getCmrMexFiscalRegime() {
    return cmrMexFiscalRegime;
  }

  public void setCmrMexFiscalRegime(String cmrMexFiscalRegime) {
    this.cmrMexFiscalRegime = cmrMexFiscalRegime;
  }

  public String getCmrMexBillingName() {
    return cmrMexBillingName;
  }

  public void setCmrMexBillingName(String cmrMexBillingName) {
    this.cmrMexBillingName = cmrMexBillingName;
  }

  public String getUsCmrCompanyNm() {
    return usCmrCompanyNm;
  }

  public void setUsCmrCompanyNm(String usCmrCompanyNm) {
    this.usCmrCompanyNm = usCmrCompanyNm;
  }

  public String getCustClass() {
    return custClass;
  }

  public void setCustClass(String custClass) {
    this.custClass = custClass;
  }

  public String getCustGroup() {
    return custGroup;
  }

  public void setCustGroup(String custGroup) {
    this.custGroup = custGroup;
  }

  public String getSboSub() {
    return sboSub;
  }

  public void setSboSub(String sboSub) {
    this.sboSub = sboSub;
  }

  public String getCompanyCd() {
    return companyCd;
  }

  public String getcRSCode() {
    return cRSCode;
  }

  public void setcRSCode(String cRSCode) {
    this.cRSCode = cRSCode;
  }

  public String getNameKanji() {
    return nameKanji;
  }

  public void setNameKanji(String nameKanji) {
    this.nameKanji = nameKanji;
  }

  public void setCompanyCd(String companyCd) {
    this.companyCd = companyCd;
  }

  public String getGovOfficeDivCode() {
    return govOfficeDivCode;
  }

  public void setGovOfficeDivCode(String govOfficeDivCode) {
    this.govOfficeDivCode = govOfficeDivCode;
  }

  public String getRepTeamMemberNo() {
    return repTeamMemberNo;
  }

  public void setRepTeamMemberNo(String repTeamMemberNo) {
    this.repTeamMemberNo = repTeamMemberNo;
  }

  public String getSalesTeamCd() {
    return salesTeamCd;
  }

  public void setSalesTeamCd(String salesTeamCd) {
    this.salesTeamCd = salesTeamCd;
  }

  public String getSiInd() {
    return siInd;
  }

  public void setSiInd(String siInd) {
    this.siInd = siInd;
  }

  public String getIinInd() {
    return iinInd;
  }

  public void setIinInd(String iinInd) {
    this.iinInd = iinInd;
  }

  public String getLeasingCompanyIndc() {
    return leasingCompanyIndc;
  }

  public void setLeasingCompanyIndc(String leasingCompanyIndc) {
    this.leasingCompanyIndc = leasingCompanyIndc;
  }

  public String getChannelCd() {
    return channelCd;
  }

  public void setChannelCd(String channelCd) {
    this.channelCd = channelCd;
  }

  public String getCreditCd() {
    return creditCd;
  }

  public void setCreditCd(String creditCd) {
    this.creditCd = creditCd;
  }

  public String getValueAddRem() {
    return valueAddRem;
  }

  public void setValueAddRem(String valueAddRem) {
    this.valueAddRem = valueAddRem;
  }

  public String getIcmsInd() {
    return icmsInd;
  }

  public void setIcmsInd(String icmsInd) {
    this.icmsInd = icmsInd;
  }

  public String getCsDiv() {
    return csDiv;
  }

  public void setCsDiv(String csDiv) {
    this.csDiv = csDiv;
  }

  public String getOemInd() {
    return oemInd;
  }

  public void setOemInd(String oemInd) {
    this.oemInd = oemInd;
  }

  public String getEducAllowanceGrp() {
    return educAllowanceGrp;
  }

  public void setEducAllowanceGrp(String educAllowanceGrp) {
    this.educAllowanceGrp = educAllowanceGrp;
  }

  public String getCRSCode() {
    return cRSCode;
  }

  public void setCRSCode(String cRSCode) {
    this.cRSCode = cRSCode;
  }

  public String getSR() {
    return SR;
  }

  public void setSR(String sR) {
    SR = sR;
  }

  public String getBillingProcessCode() {
    return billingProcessCode;
  }

  public void setBillingProcessCode(String billingProcessCode) {
    this.billingProcessCode = billingProcessCode;
  }

  public String getInvoiceSplitCode() {
    return invoiceSplitCode;
  }

  public void setInvoiceSplitCode(String invoiceSplitCode) {
    this.invoiceSplitCode = invoiceSplitCode;
  }

  public String getCreditToCustNo() {
    return creditToCustNo;
  }

  public void setCreditToCustNo(String creditToCustNo) {
    this.creditToCustNo = creditToCustNo;
  }

  public String getBillingCustNo() {
    return billingCustNo;
  }

  public void setBillingCustNo(String billingCustNo) {
    this.billingCustNo = billingCustNo;
  }

  public String getTier2() {
    return tier2;
  }

  public void setTier2(String tier2) {
    this.tier2 = tier2;
  }

  private String billingCustNo;
  private String tier2;

  public String getEstabFuncCd() {
    return estabFuncCd;
  }

  public void setEstabFuncCd(String estabFuncCd) {
    this.estabFuncCd = estabFuncCd;
  }

  public int getCompanySize() {
    return companySize;
  }

  public void setCompanySize(int companySize) {
    this.companySize = companySize;
  }

  public String getCompanyNo() {
    return companyNo;
  }

  public void setCompanyNo(String companyNo) {
    this.companyNo = companyNo;
  }

  public String getEstabNo() {
    return estabNo;
  }

  public void setEstabNo(String estabNo) {
    this.estabNo = estabNo;
  }

  public String getLocationNo() {
    return locationNo;
  }

  public void setLocationNo(String locationNo) {
    this.locationNo = locationNo;
  }

  public String getJsic() {
    return jsic;
  }

  public void setJsic(String jsic) {
    this.jsic = jsic;
  }

  public String getCsbo() {
    return csbo;
  }

  public void setCsbo(String csbo) {
    this.csbo = csbo;
  }

  public String getSbo() {
    return sbo;
  }

  public void setSbo(String sbo) {
    this.sbo = sbo;
  }

  public String getCmrIsuCodeAndDesc() {
    return cmrIsuCodeAndDesc;
  }

  public void setCmrIsuCodeAndDesc(String cmrIsuCodeAndDesc) {
    this.cmrIsuCodeAndDesc = cmrIsuCodeAndDesc;
  }

  /**
   * @return the cmrName
   */
  public String getCmrName() {
    return cmrName;
  }

  /**
   * @param cmrName
   *          the cmrName to set
   */
  public void setCmrName(String cmrName) {
    this.cmrName = cmrName;
  }

  /**
   * @return the cmrNum
   */
  public String getCmrNum() {
    return cmrNum;
  }

  /**
   * @param cmrNum
   *          the cmrNum to set
   */
  public void setCmrNum(String cmrNum) {
    this.cmrNum = cmrNum;
  }

  /**
   * @return the cmrIssueCategory
   */
  public String getCmrIssueCategory() {
    return cmrIssueCategory;
  }

  /**
   * @param cmrIssueCategory
   *          the cmrIssueCategory to set
   */
  public void setCmrIssueCategory(String cmrIssueCategory) {
    this.cmrIssueCategory = cmrIssueCategory;
  }

  /**
   * @return the cmrOwner
   */
  public String getCmrOwner() {
    return cmrOwner;
  }

  /**
   * @param cmrOwner
   *          the cmrOwner to set
   */
  public void setCmrOwner(String cmrOwner) {
    this.cmrOwner = cmrOwner;
  }

  /**
   * @return the cmrCity
   */
  public String getCmrCity() {
    return cmrCity;
  }

  /**
   * @param cmrCity
   *          the cmrCity to set
   */
  public void setCmrCity(String cmrCity) {
    this.cmrCity = cmrCity;
  }

  /**
   * @return the cmrCountry
   */
  public String getCmrCountry() {
    return cmrCountry;
  }

  /**
   * @param cmrCountry
   *          the cmrCountry to set
   */
  public void setCmrCountry(String cmrCountry) {
    this.cmrCountry = cmrCountry;
  }

  /**
   * @return the cmrPostalCode
   */
  public String getCmrPostalCode() {
    return cmrPostalCode;
  }

  /**
   * @param cmrPostalCode
   *          the cmrPostalCode to set
   */
  public void setCmrPostalCode(String cmrPostalCode) {
    this.cmrPostalCode = cmrPostalCode;
  }

  /**
   * @return the cmrResultRows
   */
  public int getCmrResultRows() {
    return cmrResultRows;
  }

  /**
   * @param cmrResultRows
   *          the cmrResultRows to set
   */
  public void setCmrResultRows(int cmrResultRows) {
    this.cmrResultRows = cmrResultRows;
  }

  /**
   * @return the cmrCoverage
   */
  public String getCmrCoverage() {
    return cmrCoverage;
  }

  /**
   * @param cmrCoverage
   *          the cmrCoverage to set
   */
  public void setCmrCoverage(String cmrCoverage) {
    this.cmrCoverage = cmrCoverage;
  }

  /**
   * @return the cmrRevenue
   */
  public String getCmrRevenue() {
    return cmrRevenue;
  }

  /**
   * @param cmrRevenue
   *          the cmrRevenue to set
   */
  public void setCmrRevenue(String cmrRevenue) {
    this.cmrRevenue = cmrRevenue;
  }

  /**
   * @return the cmrSapNumber
   */
  public String getCmrSapNumber() {
    return cmrSapNumber;
  }

  /**
   * @param cmrSapNumber
   *          the cmrSapNumber to set
   */
  public void setCmrSapNumber(String cmrSapNumber) {
    this.cmrSapNumber = cmrSapNumber;
  }

  public String getCmrState() {
    return cmrState;
  }

  public void setCmrState(String cmrState) {
    this.cmrState = cmrState;
  }

  public String[] getCmrAddrTypes() {
    return cmrAddrTypes;
  }

  public void setCmrAddrTypes(String[] cmrAddrTypes) {
    this.cmrAddrTypes = cmrAddrTypes;
  }

  public String getCmrAddrType() {
    return cmrAddrType;
  }

  public void setCmrAddrType(String cmrAddrType) {
    this.cmrAddrType = cmrAddrType;
  }

  public void setIsuCode(String isuCode) {
    this.isuCode = isuCode;
  }

  public String getIsuCode() {
    return isuCode;
  }

  public void setIsuDescription(String isuDescription) {
    this.isuDescription = isuDescription;
  }

  public String getIsuDescription() {
    return isuDescription;
  }

  public String getCmrIntlName12() {
    return cmrIntlName12;
  }

  public void setCmrIntlName12(String cmrIntlName12) {
    this.cmrIntlName12 = cmrIntlName12;
  }

  public String getCmrIntlName34() {
    return cmrIntlName34;
  }

  public void setCmrIntlName34(String cmrIntlName34) {
    this.cmrIntlName34 = cmrIntlName34;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.ibm.cio.cmrs.intcontract.IntegrationContract#getParams()
   */
  public String getContractParams() {
    return this.contractParams;
  }

  public String getCmrCountryLanded() {
    return cmrCountryLanded;
  }

  public void setCmrCountryLanded(String cmrCountryLanded) {
    this.cmrCountryLanded = cmrCountryLanded;
  }

  public String getCmrCity2() {
    return cmrCity2;
  }

  public void setCmrCity2(String cmrCity2) {
    this.cmrCity2 = cmrCity2;
  }

  public String getCmrCounty() {
    return cmrCounty;
  }

  public void setCmrCounty(String cmrCounty) {
    this.cmrCounty = cmrCounty;
  }

  public String getCmrShortName() {
    return cmrShortName;
  }

  public void setCmrShortName(String cmrShortName) {
    this.cmrShortName = cmrShortName;
  }

  public String getCmrOrderBlock() {
    return cmrOrderBlock;
  }

  public void setCmrOrderBlock(String cmrOrderBlock) {
    this.cmrOrderBlock = cmrOrderBlock;
  }

  public String getCmrVat() {
    return cmrVat;
  }

  public void setCmrVat(String cmrVat) {
    this.cmrVat = cmrVat;
  }

  public String getCmrVatInd() {
    return cmrVatInd;
  }

  public void setCmrVatInd(String cmrVatInd) {
    this.cmrVatInd = cmrVatInd;
  }

  public String getCmrBusinessReg() {
    return cmrBusinessReg;
  }

  public void setCmrBusinessReg(String cmrBusinessReg) {
    this.cmrBusinessReg = cmrBusinessReg;
  }

  public String getCmrClientId() {
    return cmrClientId;
  }

  public void setCmrClientId(String cmrClientId) {
    this.cmrClientId = cmrClientId;
  }

  public String getCmrEnterpriseNumber() {
    return cmrEnterpriseNumber;
  }

  public void setCmrEnterpriseNumber(String cmrEnterpriseNumber) {
    this.cmrEnterpriseNumber = cmrEnterpriseNumber;
  }

  public String getCmrDuns() {
    return cmrDuns;
  }

  public void setCmrDuns(String cmrDuns) {
    this.cmrDuns = cmrDuns;
  }

  public String getCmrTradestyle() {
    return cmrTradestyle;
  }

  public void setCmrTradestyle(String cmrTradestyle) {
    this.cmrTradestyle = cmrTradestyle;
  }

  public String getCmrIsic() {
    return cmrIsic;
  }

  public void setCmrIsic(String cmrIsic) {
    this.cmrIsic = cmrIsic;
  }

  public String getCmrClass() {
    return cmrClass;
  }

  public void setCmrClass(String cmrClass) {
    this.cmrClass = cmrClass;
  }

  public String getCmrAffiliate() {
    return cmrAffiliate;
  }

  public void setCmrAffiliate(String cmrAffiliate) {
    this.cmrAffiliate = cmrAffiliate;
  }

  public String getCmrIsu() {
    return cmrIsu;
  }

  public void setCmrIsu(String cmrIsu) {
    this.cmrIsu = cmrIsu;
  }

  public String getCmrBuyingGroup() {
    return cmrBuyingGroup;
  }

  public void setCmrBuyingGroup(String cmrBuyingGroup) {
    this.cmrBuyingGroup = cmrBuyingGroup;
  }

  public String getCmrGlobalBuyingGroup() {
    return cmrGlobalBuyingGroup;
  }

  public void setCmrGlobalBuyingGroup(String cmrGlobalBuyingGroup) {
    this.cmrGlobalBuyingGroup = cmrGlobalBuyingGroup;
  }

  public String getCmrTier() {
    return cmrTier;
  }

  public void setCmrTier(String cmrTier) {
    this.cmrTier = cmrTier;
  }

  public String getCmrIssuedBy() {
    return cmrIssuedBy;
  }

  public void setCmrIssuedBy(String cmrIssuedBy) {
    this.cmrIssuedBy = cmrIssuedBy;
  }

  public String getCmrIntlName() {
    return cmrIntlName;
  }

  public void setCmrIntlName(String cmrIntlName) {
    this.cmrIntlName = cmrIntlName;
  }

  public String getCmrIntlAddress() {
    return cmrIntlAddress;
  }

  public void setCmrIntlAddress(String cmrIntlAddress) {
    this.cmrIntlAddress = cmrIntlAddress;
  }

  public String getCmrIntlCity1() {
    return cmrIntlCity1;
  }

  public void setCmrIntlCity1(String cmrIntlCity1) {
    this.cmrIntlCity1 = cmrIntlCity1;
  }

  public String getCmrIntlCity2() {
    return cmrIntlCity2;
  }

  public void setCmrIntlCity2(String cmrIntlCity2) {
    this.cmrIntlCity2 = cmrIntlCity2;
  }

  public String getCmrParent() {
    return cmrParent;
  }

  public void setCmrParent(String cmrParent) {
    this.cmrParent = cmrParent;
  }

  public String getCmrGuNumber() {
    return cmrGuNumber;
  }

  public void setCmrGuNumber(String cmrGuNumber) {
    this.cmrGuNumber = cmrGuNumber;
  }

  public String getCmrDuNumber() {
    return cmrDuNumber;
  }

  public void setCmrDuNumber(String cmrDuNumber) {
    this.cmrDuNumber = cmrDuNumber;
  }

  public String getCmrProspect() {
    return cmrProspect;
  }

  public void setCmrProspect(String cmrProspect) {
    this.cmrProspect = cmrProspect;
  }

  public String getCmrName2() {
    return cmrName2;
  }

  public void setCmrName2(String cmrName2) {
    this.cmrName2 = cmrName2;
  }

  public String getCmrDomClient() {
    return cmrDomClient;
  }

  public void setCmrDomClient(String cmrDomClient) {
    this.cmrDomClient = cmrDomClient;
  }

  public String getCmrGlobClient() {
    return cmrGlobClient;
  }

  public void setCmrGlobClient(String cmrGlobClient) {
    this.cmrGlobClient = cmrGlobClient;
  }

  public String getCmrIssuedByDesc() {
    return cmrIssuedByDesc;
  }

  public void setCmrIssuedByDesc(String cmrIssuedByDesc) {
    this.cmrIssuedByDesc = cmrIssuedByDesc;
  }

  public String getCmrStateDesc() {
    return cmrStateDesc;
  }

  public void setCmrStateDesc(String cmrStateDesc) {
    this.cmrStateDesc = cmrStateDesc;
  }

  public String getCmrCountryLandedDesc() {
    return cmrCountryLandedDesc;
  }

  public void setCmrCountryLandedDesc(String cmrCountryLandedDesc) {
    this.cmrCountryLandedDesc = cmrCountryLandedDesc;
  }

  public String getCmrSubIndustry() {
    return cmrSubIndustry;
  }

  public void setCmrSubIndustry(String cmrSubIndustry) {
    this.cmrSubIndustry = cmrSubIndustry;
  }

  public String getCmrSubIndustryDesc() {
    return cmrSubIndustryDesc;
  }

  public void setCmrSubIndustryDesc(String cmrSubIndustryDesc) {
    this.cmrSubIndustryDesc = cmrSubIndustryDesc;
  }

  public String getCmrIsicDesc() {
    return cmrIsicDesc;
  }

  public void setCmrIsicDesc(String cmrIsicDesc) {
    this.cmrIsicDesc = cmrIsicDesc;
  }

  public String getCmrBuyingGroupDesc() {
    return cmrBuyingGroupDesc;
  }

  public void setCmrBuyingGroupDesc(String cmrBuyingGroupDesc) {
    this.cmrBuyingGroupDesc = cmrBuyingGroupDesc;
  }

  public String getCmrGlobalBuyingGroupDesc() {
    return cmrGlobalBuyingGroupDesc;
  }

  public void setCmrGlobalBuyingGroupDesc(String cmrGlobalBuyingGroupDesc) {
    this.cmrGlobalBuyingGroupDesc = cmrGlobalBuyingGroupDesc;
  }

  public String getCmrClassDesc() {
    return cmrClassDesc;
  }

  public void setCmrClassDesc(String cmrClassDesc) {
    this.cmrClassDesc = cmrClassDesc;
  }

  public String getCmrIsuDesc() {
    return cmrIsuDesc;
  }

  public void setCmrIsuDesc(String cmrIsuDesc) {
    this.cmrIsuDesc = cmrIsuDesc;
  }

  public String getCmrTierDesc() {
    return cmrTierDesc;
  }

  public void setCmrTierDesc(String cmrTierDesc) {
    this.cmrTierDesc = cmrTierDesc;
  }

  public String getCmrInac() {
    return cmrInac;
  }

  public void setCmrInac(String cmrInac) {
    this.cmrInac = cmrInac;
  }

  public String getCmrInacDesc() {
    return cmrInacDesc;
  }

  public void setCmrInacDesc(String cmrInacDesc) {
    this.cmrInacDesc = cmrInacDesc;
  }

  public String getCmrCompanyNo() {
    return cmrCompanyNo;
  }

  public void setCmrCompanyNo(String cmrCompanyNo) {
    this.cmrCompanyNo = cmrCompanyNo;
  }

  public String getCmrSortl() {
    return cmrSortl;
  }

  public void setCmrSortl(String cmrSortl) {
    this.cmrSortl = cmrSortl;
  }

  public String getCmrDomClientDesc() {
    return cmrDomClientDesc;
  }

  public void setCmrDomClientDesc(String cmrDomClientDesc) {
    this.cmrDomClientDesc = cmrDomClientDesc;
  }

  public String getCmrGlobClientDesc() {
    return cmrGlobClientDesc;
  }

  public void setCmrGlobClientDesc(String cmrGlobClientDesc) {
    this.cmrGlobClientDesc = cmrGlobClientDesc;
  }

  public String getCmrOrderBlockDesc() {
    return cmrOrderBlockDesc;
  }

  public void setCmrOrderBlockDesc(String cmrOrderBlockDesc) {
    this.cmrOrderBlockDesc = cmrOrderBlockDesc;
  }

  public void setContractParams(String contractParams) {
    this.contractParams = contractParams;
  }

  public String getCmrAddrTypeCode() {
    return cmrAddrTypeCode;
  }

  public void setCmrAddrTypeCode(String cmrAddrTypeCode) {
    this.cmrAddrTypeCode = cmrAddrTypeCode;
  }

  public String getCmrIntlName1() {
    return cmrIntlName1;
  }

  public void setCmrIntlName1(String cmrIntlName1) {
    this.cmrIntlName1 = cmrIntlName1;
  }

  public String getCmrIntlName2() {
    return cmrIntlName2;
  }

  public void setCmrIntlName2(String cmrIntlName2) {
    this.cmrIntlName2 = cmrIntlName2;
  }

  public String getCmrIntlName3() {
    return cmrIntlName3;
  }

  public void setCmrIntlName3(String cmrIntlName3) {
    this.cmrIntlName3 = cmrIntlName3;
  }

  public String getCmrIntlName4() {
    return cmrIntlName4;
  }

  public void setCmrIntlName4(String cmrIntlName4) {
    this.cmrIntlName4 = cmrIntlName4;
  }

  public String getCmrCoverageName() {
    return cmrCoverageName;
  }

  public void setCmrCoverageName(String cmrCoverageName) {
    this.cmrCoverageName = cmrCoverageName;
  }

  public String getCmrName3() {
    return cmrName3;
  }

  public void setCmrName3(String cmrName3) {
    this.cmrName3 = cmrName3;
  }

  public String getCmrName4() {
    return cmrName4;
  }

  public void setCmrName4(String cmrName4) {
    this.cmrName4 = cmrName4;
  }

  public String getCmrInacType() {
    return cmrInacType;
  }

  public void setCmrInacType(String cmrInacType) {
    this.cmrInacType = cmrInacType;
  }

  public String getCmrDelegated() {
    return cmrDelegated;
  }

  public void setCmrDelegated(String cmrDelegated) {
    this.cmrDelegated = cmrDelegated;
  }

  public String getCmrSitePartyID() {
    return cmrSitePartyID;
  }

  public void setCmrSitePartyID(String cmrSitePartyID) {
    this.cmrSitePartyID = cmrSitePartyID;
  }

  public String getCmrRdcCreateDate() {
    return cmrRdcCreateDate;
  }

  public void setCmrRdcCreateDate(String cmrRdcCreateDate) {
    this.cmrRdcCreateDate = cmrRdcCreateDate;
  }

  public String getCmrName1Plain() {
    return cmrName1Plain;
  }

  public void setCmrName1Plain(String cmrName1Plain) {
    this.cmrName1Plain = cmrName1Plain;
  }

  public String getCmrName2Plain() {
    return cmrName2Plain;
  }

  public void setCmrName2Plain(String cmrName2Plain) {
    this.cmrName2Plain = cmrName2Plain;
  }

  public String getCmrCountyCode() {
    return cmrCountyCode;
  }

  public void setCmrCountyCode(String cmrCountyCode) {
    this.cmrCountyCode = cmrCountyCode;
  }

  public String getCmrCapIndicator() {
    return cmrCapIndicator;
  }

  public void setCmrCapIndicator(String cmrCapIndicator) {
    this.cmrCapIndicator = cmrCapIndicator;
  }

  @Override
  public int compareTo(FindCMRRecordModel o) {
    if (o == null) {
      return -1;
    }
    if (this.getWeight() != o.getWeight()) {
      return this.getWeight() < o.getWeight() ? -1 : 1;
    }
    // if same Address Type, put the Cap records first.
    if (CmrConstants.YES_NO.Y.toString().equals(this.cmrCapIndicator) && !CmrConstants.YES_NO.Y.toString().equals(o.cmrCapIndicator)) {
      return -1;
    }
    if (!CmrConstants.YES_NO.Y.toString().equals(this.cmrCapIndicator) && CmrConstants.YES_NO.Y.toString().equals(o.cmrCapIndicator)) {
      return 1;
    }

    // put primary sold to first
    if (StringUtils.isBlank(this.cmrOrderBlock) && StringUtils.isNotBlank(o.cmrOrderBlock)) {
      return -1;
    }
    if (StringUtils.isBlank(o.cmrOrderBlock) && StringUtils.isNotBlank(this.cmrOrderBlock)) {
      return 1;
    }

    // Same address type, same CAP Indicator, get latest KUNNR first
    if (this.cmrSapNumber != null && o.cmrSapNumber != null) {
      return -1 * this.cmrSapNumber.compareTo(o.cmrSapNumber);
    }
    return this.cmrSapNumber != null && o.cmrSapNumber == null ? -1 : this.cmrSapNumber == null && o.cmrSapNumber != null ? 1 : 0;
  }

  public int getWeight() {
    if (StringUtils.isEmpty(this.cmrAddrTypeCode)) {
      return 5;
    }
    if (CmrConstants.ADDR_TYPE.ZS01.toString().equals(this.cmrAddrTypeCode)) {
      return 1;
    }
    if (CmrConstants.ADDR_TYPE.ZI01.toString().equals(this.cmrAddrTypeCode)) {
      return 2;
    }
    if (CmrConstants.ADDR_TYPE.ZP01.toString().equals(this.cmrAddrTypeCode)) {
      return 3;
    }
    if (CmrConstants.ADDR_TYPE.ZD01.toString().equals(this.cmrAddrTypeCode)) {
      return 4;
    }
    return 6;
  }

  public String getCmrPrefLang() {
    return cmrPrefLang;
  }

  public void setCmrPrefLang(String cmrPrefLang) {
    this.cmrPrefLang = cmrPrefLang;
  }

  public String getCmrLocalTax2() {
    return cmrLocalTax2;
  }

  public void setCmrLocalTax2(String cmrLocalTax2) {
    this.cmrLocalTax2 = cmrLocalTax2;
  }

  public String getCmrSensitiveFlag() {
    return cmrSensitiveFlag;
  }

  public void setCmrSensitiveFlag(String cmrSensitiveFlag) {
    this.cmrSensitiveFlag = cmrSensitiveFlag;
  }

  public String getCmrTransportZone() {
    return cmrTransportZone;
  }

  public void setCmrTransportZone(String cmrTransportZone) {
    this.cmrTransportZone = cmrTransportZone;
  }

  public String getCmrPOBox() {
    return cmrPOBox;
  }

  public void setCmrPOBox(String cmrPOBox) {
    this.cmrPOBox = cmrPOBox;
  }

  public String getCmrPOBoxCity() {
    return cmrPOBoxCity;
  }

  public void setCmrPOBoxCity(String cmrPOBoxCity) {
    this.cmrPOBoxCity = cmrPOBoxCity;
  }

  public String getCmrPOBoxPostCode() {
    return cmrPOBoxPostCode;
  }

  public void setCmrPOBoxPostCode(String cmrPOBoxPostCode) {
    this.cmrPOBoxPostCode = cmrPOBoxPostCode;
  }

  public String getCmrPpsceid() {
    return cmrPpsceid;
  }

  public void setCmrPpsceid(String cmrPpsceid) {
    this.cmrPpsceid = cmrPpsceid;
  }

  public String getCmrBldg() {
    return cmrBldg;
  }

  public void setCmrBldg(String cmrBldg) {
    this.cmrBldg = cmrBldg;
  }

  public String getCmrFloor() {
    return cmrFloor;
  }

  public void setCmrFloor(String cmrFloor) {
    this.cmrFloor = cmrFloor;
  }

  public String getCmrOffice() {
    return cmrOffice;
  }

  public void setCmrOffice(String cmrOffice) {
    this.cmrOffice = cmrOffice;
  }

  public String getCmrDept() {
    return cmrDept;
  }

  public void setCmrDept(String cmrDept) {
    this.cmrDept = cmrDept;
  }

  public String getCmrBPRelType() {
    return cmrBPRelType;
  }

  public void setCmrBPRelType(String cmrBPRelType) {
    this.cmrBPRelType = cmrBPRelType;
  }

  public String getCmrMembLevel() {
    return cmrMembLevel;
  }

  public void setCmrMembLevel(String cmrMembLevel) {
    this.cmrMembLevel = cmrMembLevel;
  }

  public String getCmrCustPhone() {
    return cmrCustPhone;
  }

  public void setCmrCustPhone(String cmrCustPhone) {
    this.cmrCustPhone = cmrCustPhone;
  }

  public String getCmrCustFax() {
    return cmrCustFax;
  }

  public void setCmrCustFax(String cmrCustFax) {
    this.cmrCustFax = cmrCustFax;
  }

  public String getCmrStreetAddress() {
    return cmrStreetAddress;
  }

  public void setCmrStreetAddress(String cmrStreetAddress) {
    this.cmrStreetAddress = cmrStreetAddress;
  }

  public String getCmrBaseCoverage() {
    return cmrBaseCoverage;
  }

  public void setCmrBaseCoverage(String cmrBaseCoverage) {
    this.cmrBaseCoverage = cmrBaseCoverage;
  }

  public float getCmrRevenueNumber() {
    return cmrRevenueNumber;
  }

  public void setCmrRevenueNumber(float cmrRevenueNumber) {
    this.cmrRevenueNumber = cmrRevenueNumber;
  }

  public String getCmrCovClientType() {
    return cmrCovClientType;
  }

  public void setCmrCovClientType(String cmrCovClientType) {
    this.cmrCovClientType = cmrCovClientType;
  }

  public String getCmrCovClientSubType() {
    return cmrCovClientSubType;
  }

  public void setCmrCovClientSubType(String cmrCovClientSubType) {
    this.cmrCovClientSubType = cmrCovClientSubType;
  }

  public String getCmrCovClientTypeDesc() {
    return cmrCovClientTypeDesc;
  }

  public void setCmrCovClientTypeDesc(String cmrCovClientTypeDesc) {
    this.cmrCovClientTypeDesc = cmrCovClientTypeDesc;
  }

  public String getCmrBaseCoverageName() {
    return cmrBaseCoverageName;
  }

  public void setCmrBaseCoverageName(String cmrBaseCoverageName) {
    this.cmrBaseCoverageName = cmrBaseCoverageName;
  }

  public String getCmrClientType() {
    return cmrClientType;
  }

  public void setCmrClientType(String cmrClientType) {
    this.cmrClientType = cmrClientType;
  }

  public String getCmrGOEIndicator() {
    return cmrGOEIndicator;
  }

  public void setCmrGOEIndicator(String cmrGOEIndicator) {
    this.cmrGOEIndicator = cmrGOEIndicator;
  }

  public String getCmrGeoLocCd() {
    return cmrGeoLocCd;
  }

  public void setCmrGeoLocCd(String cmrGeoLocCd) {
    this.cmrGeoLocCd = cmrGeoLocCd;
  }

  public String getCmrGeoLocDesc() {
    return cmrGeoLocDesc;
  }

  public void setCmrGeoLocDesc(String cmrGeoLocDesc) {
    this.cmrGeoLocDesc = cmrGeoLocDesc;
  }

  public String getCmrGUClientId() {
    return cmrGUClientId;
  }

  public void setCmrGUClientId(String cmrGUClientId) {
    this.cmrGUClientId = cmrGUClientId;
  }

  public String getCmrGUClientName() {
    return cmrGUClientName;
  }

  public void setCmrGUClientName(String cmrGUClientName) {
    this.cmrGUClientName = cmrGUClientName;
  }

  public String getCmrIntlLangCd() {
    return cmrIntlLangCd;
  }

  public void setCmrIntlLangCd(String cmrIntlLangCd) {
    this.cmrIntlLangCd = cmrIntlLangCd;
  }

  public String getCmrIntlSubLangCd() {
    return cmrIntlSubLangCd;
  }

  public void setCmrIntlSubLangCd(String cmrIntlSubLangCd) {
    this.cmrIntlSubLangCd = cmrIntlSubLangCd;
  }

  public String getCmrIntlSubLangDesc() {
    return cmrIntlSubLangDesc;
  }

  public void setCmrIntlSubLangDesc(String cmrIntlSubLangDesc) {
    this.cmrIntlSubLangDesc = cmrIntlSubLangDesc;
  }

  public String getCmrOtherIntlBusinessName() {
    return cmrOtherIntlBusinessName;
  }

  public void setCmrOtherIntlBusinessName(String cmrOtherIntlBusinessName) {
    this.cmrOtherIntlBusinessName = cmrOtherIntlBusinessName;
  }

  public String getCmrOtherIntlAddress() {
    return cmrOtherIntlAddress;
  }

  public void setCmrOtherIntlAddress(String cmrOtherIntlAddress) {
    this.cmrOtherIntlAddress = cmrOtherIntlAddress;
  }

  public String getCmrOtherIntlCity1() {
    return cmrOtherIntlCity1;
  }

  public void setCmrOtherIntlCity1(String cmrOtherIntlCity1) {
    this.cmrOtherIntlCity1 = cmrOtherIntlCity1;
  }

  public String getCmrOtherIntlCity2() {
    return cmrOtherIntlCity2;
  }

  public void setCmrOtherIntlCity2(String cmrOtherIntlCity2) {
    this.cmrOtherIntlCity2 = cmrOtherIntlCity2;
  }

  public String getCmrOtherIntlLangCd() {
    return cmrOtherIntlLangCd;
  }

  public void setCmrOtherIntlLangCd(String cmrOtherIntlLangCd) {
    this.cmrOtherIntlLangCd = cmrOtherIntlLangCd;
  }

  public String getCmrOtherIntlSubLangCd() {
    return cmrOtherIntlSubLangCd;
  }

  public void setCmrOtherIntlSubLangCd(String cmrOtherIntlSubLangCd) {
    this.cmrOtherIntlSubLangCd = cmrOtherIntlSubLangCd;
  }

  public String getCmrOtherIntlSubLangDesc() {
    return cmrOtherIntlSubLangDesc;
  }

  public void setCmrOtherIntlSubLangDesc(String cmrOtherIntlSubLangDesc) {
    this.cmrOtherIntlSubLangDesc = cmrOtherIntlSubLangDesc;
  }

  public String getCmrLeadClientRepName() {
    return cmrLeadClientRepName;
  }

  public void setCmrLeadClientRepName(String cmrLeadClientRepName) {
    this.cmrLeadClientRepName = cmrLeadClientRepName;
  }

  public String getCmrLeadClientRepNotesId() {
    return cmrLeadClientRepNotesId;
  }

  public void setCmrLeadClientRepNotesId(String cmrLeadClientRepNotesId) {
    this.cmrLeadClientRepNotesId = cmrLeadClientRepNotesId;
  }

  public String getCmrLde() {
    return cmrLde;
  }

  public void setCmrLde(String cmrLde) {
    this.cmrLde = cmrLde;
  }

  public String getCmrBusNm() {
    return cmrBusNm;
  }

  public void setCmrBusNm(String cmrBusNm) {
    this.cmrBusNm = cmrBusNm;
  }

  public String getCmrBusNmLangCd() {
    return cmrBusNmLangCd;
  }

  public void setCmrBusNmLangCd(String cmrBusNmLangCd) {
    this.cmrBusNmLangCd = cmrBusNmLangCd;
  }

  public String getCmrBusNmLangDesc() {
    return cmrBusNmLangDesc;
  }

  public void setCmrBusNmLangDesc(String cmrBusNmLangDesc) {
    this.cmrBusNmLangDesc = cmrBusNmLangDesc;
  }

  public String getCmrBusNmNative() {
    return cmrBusNmNative;
  }

  public void setCmrBusNmNative(String cmrBusNmNative) {
    this.cmrBusNmNative = cmrBusNmNative;
  }

  public String getCmrBusNmNativeLangCd() {
    return cmrBusNmNativeLangCd;
  }

  public void setCmrBusNmNativeLangCd(String cmrBusNmNativeLangCd) {
    this.cmrBusNmNativeLangCd = cmrBusNmNativeLangCd;
  }

  public String getCmrBusNmNativeLangDesc() {
    return cmrBusNmNativeLangDesc;
  }

  public void setCmrBusNmNativeLangDesc(String cmrBusNmNativeLangDesc) {
    this.cmrBusNmNativeLangDesc = cmrBusNmNativeLangDesc;
  }

  public String getCmrIndustryCd() {
    return cmrIndustryCd;
  }

  public void setCmrIndustryCd(String cmrIndustryCd) {
    this.cmrIndustryCd = cmrIndustryCd;
  }

  public String getCmrIndustryName() {
    return cmrIndustryName;
  }

  public void setCmrIndustryName(String cmrIndustryName) {
    this.cmrIndustryName = cmrIndustryName;
  }

  public float getSearchScore() {
    return searchScore;
  }

  public void setSearchScore(float searchScore) {
    this.searchScore = searchScore;
  }

  public String getCmrLite() {
    return cmrLite;
  }

  public void setCmrLite(String cmrLite) {
    this.cmrLite = cmrLite;
  }

  public String getCmrCoverageEligible() {
    return cmrCoverageEligible;
  }

  public void setCmrCoverageEligible(String cmrCoverageEligible) {
    this.cmrCoverageEligible = cmrCoverageEligible;
  }

  public String getCmrCovClientSubTypeDesc() {
    return cmrCovClientSubTypeDesc;
  }

  public void setCmrCovClientSubTypeDesc(String cmrCovClientSubTypeDesc) {
    this.cmrCovClientSubTypeDesc = cmrCovClientSubTypeDesc;
  }

  public String getCmrStreet() {
    return cmrStreet;
  }

  public void setCmrStreet(String cmrStreet) {
    this.cmrStreet = cmrStreet;
  }

  public String getCmrAddrSeq() {
    return cmrAddrSeq;
  }

  public void setCmrAddrSeq(String cmrAddrSeq) {
    this.cmrAddrSeq = cmrAddrSeq;
  }

  public String getCmrStreetAddressCont() {
    return cmrStreetAddressCont;
  }

  public void setCmrStreetAddressCont(String cmrStreetAddressCont) {
    this.cmrStreetAddressCont = cmrStreetAddressCont;
  }

  public String getTransAddrNo() {
    return transAddrNo;
  }

  public void setTransAddrNo(String transAddrNo) {
    this.transAddrNo = transAddrNo;
  }

  public String getCmrTaxNumber() {
    return cmrTaxNumber;
  }

  public void setCmrTaxNumber(String cmrTaxNumber) {
    this.cmrTaxNumber = cmrTaxNumber;
  }

  public String getCmrTaxOffice() {
    return cmrTaxOffice;
  }

  public void setCmrTaxOffice(String cmrTaxOffice) {
    this.cmrTaxOffice = cmrTaxOffice;
  }

  public String getParentCMRNo() {
    return parentCMRNo;
  }

  public void setParentCMRNo(String parentCMRNo) {
    this.parentCMRNo = parentCMRNo;
  }

  public String getAttach() {
    return attach;
  }

  public void setAttach(String attach) {
    this.attach = attach;
  }

  public String getHwSvcsRepTeamNo() {
    return hwSvcsRepTeamNo;
  }

  public void setHwSvcsRepTeamNo(String hwSvcsRepTeamNo) {
    this.hwSvcsRepTeamNo = hwSvcsRepTeamNo;
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

  public String getUsCmrCsoSite() {
    return usCmrCsoSite;
  }

  public void setUsCmrCsoSite(String usCmrCsoSite) {
    this.usCmrCsoSite = usCmrCsoSite;
  }

  public String getUsCmrMktgArDept() {
    return usCmrMktgArDept;
  }

  public void setUsCmrMktgArDept(String usCmrMktgArDept) {
    this.usCmrMktgArDept = usCmrMktgArDept;
  }

  public String getUsCmrRestrictTo() {
    return usCmrRestrictTo;
  }

  public void setUsCmrRestrictTo(String usCmrRestrictTo) {
    this.usCmrRestrictTo = usCmrRestrictTo;
  }

  public String getUsCmrBpAccountType() {
    return usCmrBpAccountType;
  }

  public void setUsCmrBpAccountType(String usCmrBpAccountType) {
    this.usCmrBpAccountType = usCmrBpAccountType;
  }

  public String getMilitaryFlag() {
    return militaryFlag;
  }

  public void setMilitaryFlag(String militaryFlag) {
    this.militaryFlag = militaryFlag;
  }

  public String getCmrSellBoGrp() {
    return cmrSellBoGrp;
  }

  public void setCmrSellBoGrp(String cmrSellBoGrp) {
    this.cmrSellBoGrp = cmrSellBoGrp;
  }

  public String getCmrInstlBoGrp() {
    return cmrInstlBoGrp;
  }

  public void setCmrInstlBoGrp(String cmrInstlBoGrp) {
    this.cmrInstlBoGrp = cmrInstlBoGrp;
  }

  public String getCmrTaxCertStatus() {
    return cmrTaxCertStatus;
  }

  public void setCmrTaxCertStatus(String cmrTaxCertStatus) {
    this.cmrTaxCertStatus = cmrTaxCertStatus;
  }

  public String getCmrAddrUse() {
    return cmrAddrUse;
  }

  public void setCmrAddrUse(String cmrAddrUse) {
    this.cmrAddrUse = cmrAddrUse;
  }

  public String getCmrDataLine() {
    return cmrDataLine;
  }

  public void setCmrDataLine(String cmrDataLine) {
    this.cmrDataLine = cmrDataLine;
  }

  public String getCmrQstNo() {
    return cmrQstNo;
  }

  public void setCmrQstNo(String cmrQstNo) {
    this.cmrQstNo = cmrQstNo;
  }

  public String getCmrTaxExInd() {
    return cmrTaxExInd;
  }

  public void setCmrTaxExInd(String cmrTaxExInd) {
    this.cmrTaxExInd = cmrTaxExInd;
  }

  public String getCmrLicNo() {
    return cmrLicNo;
  }

  public void setCmrLicNo(String cmrLicNo) {
    this.cmrLicNo = cmrLicNo;
  }

  public String getCmrTaxExemptReas() {
    return cmrTaxExemptReas;
  }

  public void setCmrTaxExemptReas(String cmrTaxExemptReas) {
    this.cmrTaxExemptReas = cmrTaxExemptReas;
  }

  public String getCmrLeasingInd() {
    return cmrLeasingInd;
  }

  public void setCmrLeasingInd(String cmrLeasingInd) {
    this.cmrLeasingInd = cmrLeasingInd;
  }

  public String getCmrPurOrdNo() {
    return cmrPurOrdNo;
  }

  public void setCmrPurOrdNo(String cmrPurOrdNo) {
    this.cmrPurOrdNo = cmrPurOrdNo;
  }

  public String getCmrEstabFnInd() {
    return cmrEstabFnInd;
  }

  public void setCmrEstabFnInd(String cmrEstabFnInd) {
    this.cmrEstabFnInd = cmrEstabFnInd;
  }

  public String getCmrSellBoNum() {
    return cmrSellBoNum;
  }

  public void setCmrSellBoNum(String cmrSellBoNum) {
    this.cmrSellBoNum = cmrSellBoNum;
  }

  public String getCmrInstlBoNum() {
    return cmrInstlBoNum;
  }

  public void setCmrInstlBoNum(String cmrInstlBoNum) {
    this.cmrInstlBoNum = cmrInstlBoNum;
  }

  public String getCmrAccRecvBo() {
    return cmrAccRecvBo;
  }

  public void setCmrAccRecvBo(String cmrAccRecvBo) {
    this.cmrAccRecvBo = cmrAccRecvBo;
  }

  public String getCmrPymtTerms() {
    return cmrPymtTerms;
  }

  public void setCmrPymtTerms(String cmrPymtTerms) {
    this.cmrPymtTerms = cmrPymtTerms;
  }

  public String getCmrBillPlnTyp() {
    return cmrBillPlnTyp;
  }

  public void setCmrBillPlnTyp(String cmrBillPlnTyp) {
    this.cmrBillPlnTyp = cmrBillPlnTyp;
  }

  public String getCmrNoInvc() {
    return cmrNoInvc;
  }

  public void setCmrNoInvc(String cmrNoInvc) {
    this.cmrNoInvc = cmrNoInvc;
  }

  public String getCmrEnggBoGrp() {
    return cmrEnggBoGrp;
  }

  public void setCmrEnggBoGrp(String cmrEnggBoGrp) {
    this.cmrEnggBoGrp = cmrEnggBoGrp;
  }

  public String getCmrLtPymntInd() {
    return cmrLtPymntInd;
  }

  public void setCmrLtPymntInd(String cmrLtPymntInd) {
    this.cmrLtPymntInd = cmrLtPymntInd;
  }

  public String getCmrCustCreditCode() {
    return cmrCustCreditCode;
  }

  public void setCmrCustCreditCode(String cmrCustCreditCode) {
    this.cmrCustCreditCode = cmrCustCreditCode;
  }

  public String getExtWalletId() {
    return extWalletId;
  }

  public void setExtWalletId(String extWalletId) {
    this.extWalletId = extWalletId;
  }

  public String getUsCmrMktgArea() {
    return usCmrMktgArea;
  }

  public void setUsCmrMktgArea(String usCmrMktgArea) {
    this.usCmrMktgArea = usCmrMktgArea;
  }

  public String getUsCmrMktgDept() {
    return usCmrMktgDept;
  }

  public void setUsCmrMktgDept(String usCmrMktgDept) {
    this.usCmrMktgDept = usCmrMktgDept;
  }

  public String getUsCmrOrgMktgBrOfc() {
    return usCmrOrgMktgBrOfc;
  }

  public void setUsCmrOrgMktgBrOfc(String usCmrOrgMktgBrOfc) {
    this.usCmrOrgMktgBrOfc = usCmrOrgMktgBrOfc;
  }

  public String getUsCmrPccArBo() {
    return usCmrPccArBo;
  }

  public void setUsCmrPccArBo(String usCmrPccArBo) {
    this.usCmrPccArBo = usCmrPccArBo;
  }

  public String getUsCmrPccMktgBo() {
    return usCmrPccMktgBo;
  }

  public void setUsCmrPccMktgBo(String usCmrPccMktgBo) {
    this.usCmrPccMktgBo = usCmrPccMktgBo;
  }

  public String getUsCmrSvcArOfc() {
    return usCmrSvcArOfc;
  }

  public void setUsCmrSvcArOfc(String usCmrSvcArOfc) {
    this.usCmrSvcArOfc = usCmrSvcArOfc;
  }

  public String getUsCmrSvcLgsysOfc() {
    return usCmrSvcLgsysOfc;
  }

  public void setUsCmrSvcLgsysOfc(String usCmrSvcLgsysOfc) {
    this.usCmrSvcLgsysOfc = usCmrSvcLgsysOfc;
  }

  public String getUsCmrSvcSmsysOfc() {
    return usCmrSvcSmsysOfc;
  }

  public void setUsCmrSvcSmsysOfc(String usCmrSvcSmsysOfc) {
    this.usCmrSvcSmsysOfc = usCmrSvcSmsysOfc;
  }

  public String getUsCmrSvcOthOfc() {
    return usCmrSvcOthOfc;
  }

  public void setUsCmrSvcOthOfc(String usCmrSvcOthOfc) {
    this.usCmrSvcOthOfc = usCmrSvcOthOfc;
  }

  public String getUsCmrSvcMasterBo() {
    return usCmrSvcMasterBo;
  }

  public void setUsCmrSvcMasterBo(String usCmrSvcMasterBo) {
    this.usCmrSvcMasterBo = usCmrSvcMasterBo;
  }

  public String getUsCmrBoDivision() {
    return usCmrBoDivision;
  }

  public void setUsCmrBoDivision(String usCmrBoDivision) {
    this.usCmrBoDivision = usCmrBoDivision;
  }

  public String getUsCmrBoTradingArea() {
    return usCmrBoTradingArea;
  }

  public void setUsCmrBoTradingArea(String usCmrBoTradingArea) {
    this.usCmrBoTradingArea = usCmrBoTradingArea;
  }

  public String getUsCmrNonIbmCompInd() {
    return usCmrNonIbmCompInd;
  }

  public void setUsCmrNonIbmCompInd(String usCmrNonIbmCompInd) {
    this.usCmrNonIbmCompInd = usCmrNonIbmCompInd;
  }

  public String getUsCmrOemInd() {
    return usCmrOemInd;
  }

  public void setUsCmrOemInd(String usCmrOemInd) {
    this.usCmrOemInd = usCmrOemInd;
  }

  public String getUsCmrBpAbbrevNm() {
    return usCmrBpAbbrevNm;
  }

  public void setUsCmrBpAbbrevNm(String usCmrBpAbbrevNm) {
    this.usCmrBpAbbrevNm = usCmrBpAbbrevNm;
  }

  public String getUsCmrEnterpriseNm() {
    return usCmrEnterpriseNm;
  }

  public void setUsCmrEnterpriseNm(String usCmrEnterpriseNm) {
    this.usCmrEnterpriseNm = usCmrEnterpriseNm;
  }

  public String getUsCmrEnterpriseType() {
    return usCmrEnterpriseType;
  }

  public void setUsCmrEnterpriseType(String usCmrEnterpriseType) {
    this.usCmrEnterpriseType = usCmrEnterpriseType;
  }

  public String getUsCmrTaxType1() {
    return usCmrTaxType1;
  }

  public void setUsCmrTaxType1(String usCmrTaxType1) {
    this.usCmrTaxType1 = usCmrTaxType1;
  }

  public String getUsCmrTaxType2() {
    return usCmrTaxType2;
  }

  public void setUsCmrTaxType2(String usCmrTaxType2) {
    this.usCmrTaxType2 = usCmrTaxType2;
  }

  public String getUsCmrTaxType3() {
    return usCmrTaxType3;
  }

  public void setUsCmrTaxType3(String usCmrTaxType3) {
    this.usCmrTaxType3 = usCmrTaxType3;
  }

  public String getUsCmrTaxClass1() {
    return usCmrTaxClass1;
  }

  public void setUsCmrTaxClass1(String usCmrTaxClass1) {
    this.usCmrTaxClass1 = usCmrTaxClass1;
  }

  public String getUsCmrTaxClass2() {
    return usCmrTaxClass2;
  }

  public void setUsCmrTaxClass2(String usCmrTaxClass2) {
    this.usCmrTaxClass2 = usCmrTaxClass2;
  }

  public String getUsCmrTaxClass3() {
    return usCmrTaxClass3;
  }

  public void setUsCmrTaxClass3(String usCmrTaxClass3) {
    this.usCmrTaxClass3 = usCmrTaxClass3;
  }

  public String getUsCmrIccTaxExempt() {
    return usCmrIccTaxExempt;
  }

  public void setUsCmrIccTaxExempt(String usCmrIccTaxExempt) {
    this.usCmrIccTaxExempt = usCmrIccTaxExempt;
  }

  public String getUsCmrIccTaxClass() {
    return usCmrIccTaxClass;
  }

  public void setUsCmrIccTaxClass(String usCmrIccTaxClass) {
    this.usCmrIccTaxClass = usCmrIccTaxClass;
  }

  public String getUsCmrTaxCertStat1() {
    return usCmrTaxCertStat1;
  }

  public void setUsCmrTaxCertStat1(String usCmrTaxCertStat1) {
    this.usCmrTaxCertStat1 = usCmrTaxCertStat1;
  }

  public String getUsCmrTaxCertStat2() {
    return usCmrTaxCertStat2;
  }

  public void setUsCmrTaxCertStat2(String usCmrTaxCertStat2) {
    this.usCmrTaxCertStat2 = usCmrTaxCertStat2;
  }

  public String getUsCmrTaxCertStat3() {
    return usCmrTaxCertStat3;
  }

  public void setUsCmrTaxCertStat3(String usCmrTaxCertStat3) {
    this.usCmrTaxCertStat3 = usCmrTaxCertStat3;
  }

  public String getUsCmrSccCd() {
    return usCmrSccCd;
  }

  public void setUsCmrSccCd(String usCmrSccCd) {
    this.usCmrSccCd = usCmrSccCd;
  }

  public String getUsCmrOcl() {
    return usCmrOcl;
  }

  public void setUsCmrOcl(String usCmrOcl) {
    this.usCmrOcl = usCmrOcl;
  }

  public String getUsCmrEducAllowStat() {
    return usCmrEducAllowStat;
  }

  public void setUsCmrEducAllowStat(String usCmrEducAllowStat) {
    this.usCmrEducAllowStat = usCmrEducAllowStat;
  }

  public String getUsCmrRestrictToCd() {
    return usCmrRestrictToCd;
  }

  public void setUsCmrRestrictToCd(String usCmrRestrictToCd) {
    this.usCmrRestrictToCd = usCmrRestrictToCd;
  }

  public String getUsCmrRestrictToDesc() {
    return usCmrRestrictToDesc;
  }

  public void setUsCmrRestrictToDesc(String usCmrRestrictToDesc) {
    this.usCmrRestrictToDesc = usCmrRestrictToDesc;
  }

  public String getUsCmrBpCd() {
    return usCmrBpCd;
  }

  public void setUsCmrBpCd(String usCmrBpCd) {
    this.usCmrBpCd = usCmrBpCd;
  }

  public String getUsCmrBpNm() {
    return usCmrBpNm;
  }

  public void setUsCmrBpNm(String usCmrBpNm) {
    this.usCmrBpNm = usCmrBpNm;
  }

  public String getUsCmrBpType() {
    return usCmrBpType;
  }

  public void setUsCmrBpType(String usCmrBpType) {
    this.usCmrBpType = usCmrBpType;
  }

  public String getCmrProxiLocn() {
    return cmrProxiLocn;
  }

  public void setCmrProxiLocn(String cmrProxiLocn) {
    this.cmrProxiLocn = cmrProxiLocn;
  }

  public String getCmrCollBo() {
    return cmrCollBo;
  }

  public void setCmrCollBo(String cmrCollBo) {
    this.cmrCollBo = cmrCollBo;
  }

  public String getCmrCollectorNo() {
    return cmrCollectorNo;
  }

  public void setCmrCollectorNo(String cmrCollectorNo) {
    this.cmrCollectorNo = cmrCollectorNo;
  }

  public String getCmrFiscalCd() {
    return cmrFiscalCd;
  }

  public void setCmrFiscalCd(String cmrFiscalCd) {
    this.cmrFiscalCd = cmrFiscalCd;
  }

}