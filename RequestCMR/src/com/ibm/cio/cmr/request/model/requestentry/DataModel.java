/**
 * 
 */
package com.ibm.cio.cmr.request.model.requestentry;

import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.cio.cmr.request.model.BaseModel;

/**
 * @author Jeffrey Zamora
 * 
 */
public class DataModel extends BaseModel {

  private static final long serialVersionUID = 525330114145218170L;

  private String abbrevNm;

  private String affiliate;

  private String bpRelType;

  private String capInd;

  private String clientTier;

  private String cmrIssuingCntry;

  private String cmrNo;

  private String cmrOwner;

  private String company;

  private String custClass;

  private String enterprise;

  private String custPrefLang;

  private String inacCd;

  private String inacType;

  private String isuCd;

  private String taxCd1;

  private String taxCd2;

  private String taxCd3;

  private String memLvl;

  private String ppsceid;

  private String searchTerm;

  private String sensitiveFlag;

  private String isicCd;

  private String sitePartyId;

  private String subIndustryCd;

  private String vat;

  private String collBoId;

  private String repTeamMemberNo;

  private String govType;

  private String ibmDeptCostCenter;

  private String covId;

  private String bgId;

  private String gbgId;

  private String geoLocCd;

  private String dunsNo;

  private String covDesc;

  private String bgDesc;

  private String gbgDesc;

  private String geoLocDesc;

  private String bgRuleId;

  private String ordBlk;

  private String restrictInd;

  private String restrictTo;

  private String oemInd;

  private String bpAcctTyp;

  private String mktgDept;

  private String mtkgArDept;

  private String adminDeptCd;

  private String pccMktgDept;

  private String pccArDept;

  private String svcArOffice;

  private String outCityLimit;

  private String csoSite;

  private String fedSiteInd;

  private String sizeCd;

  private String svcTerritoryZone;

  private String miscBillCd;

  private String bpName;

  private String iccTaxClass;

  private String iccTaxExemptStatus;

  private String nonIbmCompanyInd;

  private String div;

  private String dept;

  private String func;

  private String user;

  private String loc;

  private String collectorNo;

  private String marketingChnlIndcValue;

  private String representativeTeamMemberNo;

  private String contactName1;

  private String contactName2;

  private String contactName3;

  private String phone1;

  private String phone2;

  private String phone3;

  private String email1;

  private String email2;

  private String email3;

  private String busnTyp;

  private String icmsInd;

  private String mrcCd;

  private String salesBoCd;

  private String salesTeamCd;

  private String specialTaxCd;

  private String engineeringBo;

  private String collectionCd;

  private String ibmBankNo;

  private String economicCd;

  private String vatExempt; 
  
  private String vatInd;

  private String denialCusInd;

  private String privIndc;

  private String modeOfPayment;

  private String locationNumber;

  private String orgNo;

  private String cusInvoiceCopies;

  private String currencyCd;

  private String legacyCurrencyCd;

  private String mailingCondition;

  private String salesBusOffCd;

  private String commercialFinanced;

  private String creditCd;

  private String installBranchOff;

  private String identClient;

  private String bpSalesRepNo;

  private String embargoCd;

  private String agreementSignDate;

  private String cisServiceCustIndc;

  private String fiscalDataStatus;

  private String fiscalDataCompanyNo;

  private String nationalCusId;

  private String rdcComment;

  private String abbrevLocn;

  private String collectorNameNo;

  private String mrktChannelInd;

  // CH Specific
  private String custLangCd;
  private String hwInstlMstrFlg;

  // Italy Legacy Fields
  private String hwSvcsRepTeamNo;

  private String usSicmen;

  private String military;

  private String apCustClusterId;

  private String custAcctType;

  private String customerIdCd;

  private String mexicoBillingName;

  public String getMexicoBillingName() {
    return mexicoBillingName;
  }

  public void setMexicoBillingName(String mexicoBillingName) {
    this.mexicoBillingName = mexicoBillingName;
  }

  public String getCreditCd() {
    return creditCd;
  }

  public String getAdminDeptCd() {
    return adminDeptCd;
  }

  public void setAdminDeptCd(String adminDeptCd) {
    this.adminDeptCd = adminDeptCd;
  }

  public void setCreditCd(String creditCd) {
    this.creditCd = creditCd;
  }

  public String getVatExempt() {
    return vatExempt;
  }

  public void setVatExempt(String vatExempt) {
    this.vatExempt = vatExempt;
  }

  public String getGovType() {
    return govType;
  }

  public void setGovType(String govType) {
    this.govType = govType;
  }

  @Override
  public boolean allKeysAssigned() {
    return false;
  }

  @Override
  public String getRecordDescription() {
    return null;
  }

  @Override
  public void addKeyParameters(ModelAndView mv) {
  }

  @Override
  public void addKeyParameters(ModelMap map) {
  }

  public String getAbbrevNm() {
    return abbrevNm;
  }

  public void setAbbrevNm(String abbrevNm) {
    this.abbrevNm = abbrevNm;
  }

  public String getAffiliate() {
    return affiliate;
  }

  public void setAffiliate(String affiliate) {
    this.affiliate = affiliate;
  }

  public String getBpRelType() {
    return bpRelType;
  }

  public void setBpRelType(String bpRelType) {
    this.bpRelType = bpRelType;
  }

  public String getCapInd() {
    return capInd;
  }

  public void setCapInd(String capInd) {
    this.capInd = capInd;
  }

  public String getClientTier() {
    return clientTier;
  }

  public void setClientTier(String clientTier) {
    this.clientTier = clientTier;
  }

  public String getCmrIssuingCntry() {
    return cmrIssuingCntry;
  }

  public void setCmrIssuingCntry(String cmrIssuingCntry) {
    this.cmrIssuingCntry = cmrIssuingCntry;
  }

  public String getCmrNo() {
    return cmrNo;
  }

  public void setCmrNo(String cmrNo) {
    this.cmrNo = cmrNo;
  }

  public String getCmrOwner() {
    return cmrOwner;
  }

  public void setCmrOwner(String cmrOwner) {
    this.cmrOwner = cmrOwner;
  }

  public String getCompany() {
    return company;
  }

  public void setCompany(String company) {
    this.company = company;
  }

  public String getCustClass() {
    return custClass;
  }

  public void setCustClass(String custClass) {
    this.custClass = custClass;
  }

  public String getEnterprise() {
    return enterprise;
  }

  public void setEnterprise(String enterprise) {
    this.enterprise = enterprise;
  }

  public String getCustPrefLang() {
    return custPrefLang;
  }

  public void setCustPrefLang(String custPrefLang) {
    this.custPrefLang = custPrefLang;
  }

  public String getInacCd() {
    return inacCd;
  }

  public void setInacCd(String inacCd) {
    this.inacCd = inacCd;
  }

  public String getInacType() {
    return inacType;
  }

  public void setInacType(String inacType) {
    this.inacType = inacType;
  }

  public String getIsuCd() {
    return isuCd;
  }

  public void setIsuCd(String isuCd) {
    this.isuCd = isuCd;
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

  public String getTaxCd3() {
    return taxCd3;
  }

  public void setTaxCd3(String taxCd3) {
    this.taxCd3 = taxCd3;
  }

  public String getMemLvl() {
    return memLvl;
  }

  public void setMemLvl(String memLvl) {
    this.memLvl = memLvl;
  }

  public String getPpsceid() {
    return ppsceid;
  }

  public void setPpsceid(String ppsceid) {
    this.ppsceid = ppsceid;
  }

  public String getSearchTerm() {
    return searchTerm;
  }

  public void setSearchTerm(String searchTerm) {
    this.searchTerm = searchTerm;
  }

  public String getSensitiveFlag() {
    return sensitiveFlag;
  }

  public void setSensitiveFlag(String sensitiveFlag) {
    this.sensitiveFlag = sensitiveFlag;
  }

  public String getIsicCd() {
    return isicCd;
  }

  public void setIsicCd(String isicCd) {
    this.isicCd = isicCd;
  }

  public String getSitePartyId() {
    return sitePartyId;
  }

  public void setSitePartyId(String sitePartyId) {
    this.sitePartyId = sitePartyId;
  }

  public String getSubIndustryCd() {
    return subIndustryCd;
  }

  public void setSubIndustryCd(String subIndustryCd) {
    this.subIndustryCd = subIndustryCd;
  }

  public String getVat() {
    return vat;
  }

  public void setVat(String vat) {
    this.vat = vat;
  }

  public String getCollBoId() {
    return collBoId;
  }

  public void setCollBoId(String collBoId) {
    this.collBoId = collBoId;
  }

  public String getRepTeamMemberNo() {
    return repTeamMemberNo;
  }

  public void setRepTeamMemberNo(String repTeamMemberNo) {
    this.repTeamMemberNo = repTeamMemberNo;
  }

  public String getCovId() {
    return covId;
  }

  public void setCovId(String covId) {
    this.covId = covId;
  }

  public String getBgId() {
    return bgId;
  }

  public void setBgId(String bgId) {
    this.bgId = bgId;
  }

  public String getGbgId() {
    return gbgId;
  }

  public void setGbgId(String gbgId) {
    this.gbgId = gbgId;
  }

  public String getGeoLocCd() {
    return geoLocCd;
  }

  public void setGeoLocCd(String geoLocCd) {
    this.geoLocCd = geoLocCd;
  }

  public String getDunsNo() {
    return dunsNo;
  }

  public void setDunsNo(String dunsNo) {
    this.dunsNo = dunsNo;
  }

  public String getCovDesc() {
    return covDesc;
  }

  public void setCovDesc(String covDesc) {
    this.covDesc = covDesc;
  }

  public String getBgDesc() {
    return bgDesc;
  }

  public void setBgDesc(String bgDesc) {
    this.bgDesc = bgDesc;
  }

  public String getGbgDesc() {
    return gbgDesc;
  }

  public void setGbgDesc(String gbgDesc) {
    this.gbgDesc = gbgDesc;
  }

  public String getGeoLocDesc() {
    return geoLocDesc;
  }

  public void setGeoLocDesc(String geoLocDesc) {
    this.geoLocDesc = geoLocDesc;
  }

  public String getBgRuleId() {
    return bgRuleId;
  }

  public void setBgRuleId(String bgRuleId) {
    this.bgRuleId = bgRuleId;
  }

  public String getOrdBlk() {
    return ordBlk;
  }

  public void setOrdBlk(String ordBlk) {
    this.ordBlk = ordBlk;
  }

  public String getRestrictInd() {
    return restrictInd;
  }

  public void setRestrictInd(String restrictInd) {
    this.restrictInd = restrictInd;
  }

  public String getRestrictTo() {
    return restrictTo;
  }

  public void setRestrictTo(String restrictTo) {
    this.restrictTo = restrictTo;
  }

  public String getOemInd() {
    return oemInd;
  }

  public void setOemInd(String oemInd) {
    this.oemInd = oemInd;
  }

  public String getBpAcctTyp() {
    return bpAcctTyp;
  }

  public void setBpAcctTyp(String bpAcctTyp) {
    this.bpAcctTyp = bpAcctTyp;
  }

  public String getMktgDept() {
    return mktgDept;
  }

  public void setMktgDept(String mktgDept) {
    this.mktgDept = mktgDept;
  }

  public String getMtkgArDept() {
    return mtkgArDept;
  }

  public void setMtkgArDept(String mtkgArDept) {
    this.mtkgArDept = mtkgArDept;
  }

  public String getPccMktgDept() {
    return pccMktgDept;
  }

  public void setPccMktgDept(String pccMktgDept) {
    this.pccMktgDept = pccMktgDept;
  }

  public String getPccArDept() {
    return pccArDept;
  }

  public void setPccArDept(String pccArDept) {
    this.pccArDept = pccArDept;
  }

  public String getSvcArOffice() {
    return svcArOffice;
  }

  public void setSvcArOffice(String svcArOffice) {
    this.svcArOffice = svcArOffice;
  }

  public String getOutCityLimit() {
    return outCityLimit;
  }

  public void setOutCityLimit(String outCityLimit) {
    this.outCityLimit = outCityLimit;
  }

  public String getCsoSite() {
    return csoSite;
  }

  public void setCsoSite(String csoSite) {
    this.csoSite = csoSite;
  }

  public String getFedSiteInd() {
    return fedSiteInd;
  }

  public void setFedSiteInd(String fedSiteInd) {
    this.fedSiteInd = fedSiteInd;
  }

  public String getSizeCd() {
    return sizeCd;
  }

  public void setSizeCd(String sizeCd) {
    this.sizeCd = sizeCd;
  }

  public String getSvcTerritoryZone() {
    return svcTerritoryZone;
  }

  public void setSvcTerritoryZone(String svcTerritoryZone) {
    this.svcTerritoryZone = svcTerritoryZone;
  }

  public String getMiscBillCd() {
    return miscBillCd;
  }

  public void setMiscBillCd(String miscBillCd) {
    this.miscBillCd = miscBillCd;
  }

  public String getBpName() {
    return bpName;
  }

  public void setBpName(String bpName) {
    this.bpName = bpName;
  }

  public String getIccTaxClass() {
    return iccTaxClass;
  }

  public void setIccTaxClass(String iccTaxClass) {
    this.iccTaxClass = iccTaxClass;
  }

  public String getIccTaxExemptStatus() {
    return iccTaxExemptStatus;
  }

  public void setIccTaxExemptStatus(String iccTaxExemptStatus) {
    this.iccTaxExemptStatus = iccTaxExemptStatus;
  }

  public String getNonIbmCompanyInd() {
    return nonIbmCompanyInd;
  }

  public void setNonIbmCompanyInd(String nonIbmCompanyInd) {
    this.nonIbmCompanyInd = nonIbmCompanyInd;
  }

  public String getDiv() {
    return div;
  }

  public void setDiv(String div) {
    this.div = div;
  }

  public String getDept() {
    return dept;
  }

  public void setDept(String dept) {
    this.dept = dept;
  }

  public String getFunc() {
    return func;
  }

  public void setFunc(String func) {
    this.func = func;
  }

  public String getUser() {
    return user;
  }

  public void setUser(String user) {
    this.user = user;
  }

  public String getLoc() {
    return loc;
  }

  public void setLoc(String loc) {
    this.loc = loc;
  }

  public String getCollectorNo() {
    return collectorNo;
  }

  public void setCollectorNo(String collectorNo) {
    this.collectorNo = collectorNo;
  }

  public String getMarketingChnlIndcValue() {
    return marketingChnlIndcValue;
  }

  public void setMarketingChnlIndcValue(String marketingChnlIndcValue) {
    this.marketingChnlIndcValue = marketingChnlIndcValue;
  }

  public String getRepresentativeTeamMemberNo() {
    return representativeTeamMemberNo;
  }

  public void setRepresentativeTeamMemberNo(String representativeTeamMemberNo) {
    this.representativeTeamMemberNo = representativeTeamMemberNo;
  }

  public String getContactName1() {
    return contactName1;
  }

  public void setContactName1(String contactName1) {
    this.contactName1 = contactName1;
  }

  public String getContactName2() {
    return contactName2;
  }

  public void setContactName2(String contactName2) {
    this.contactName2 = contactName2;
  }

  public String getContactName3() {
    return contactName3;
  }

  public void setContactName3(String contactName3) {
    this.contactName3 = contactName3;
  }

  public String getPhone1() {
    return phone1;
  }

  public void setPhone1(String phone1) {
    this.phone1 = phone1;
  }

  public String getPhone2() {
    return phone2;
  }

  public void setPhone2(String phone2) {
    this.phone2 = phone2;
  }

  public String getPhone3() {
    return phone3;
  }

  public void setPhone3(String phone3) {
    this.phone3 = phone3;
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

  public String getBusnTyp() {
    return busnTyp;
  }

  public void setBusnTyp(String busnTyp) {
    this.busnTyp = busnTyp;
  }

  public String getIcmsInd() {
    return icmsInd;
  }

  public void setIcmsInd(String icmsInd) {
    this.icmsInd = icmsInd;
  }

  public String getMrcCd() {
    return mrcCd;
  }

  public void setMrcCd(String mrcCd) {
    this.mrcCd = mrcCd;
  }

  public String getSalesBoCd() {
    return salesBoCd;
  }

  public void setSalesBoCd(String salesBoCd) {
    this.salesBoCd = salesBoCd;
  }

  public String getIbmBankNo() {
    return ibmBankNo;
  }

  public void setIbmBankNo(String ibmBankNo) {
    this.ibmBankNo = ibmBankNo;
  }

  public String getCollectionCd() {
    return collectionCd;
  }

  public void setCollectionCd(String collectionCd) {
    this.collectionCd = collectionCd;
  }

  public String getEconomicCd() {
    return economicCd;
  }

  public void setEconomicCd(String economicCd) {
    this.economicCd = economicCd;
  }

  public String getDenialCusInd() {
    return denialCusInd;
  }

  public void setDenialCusInd(String denialCusInd) {
    this.denialCusInd = denialCusInd;
  }

  public String getIbmDeptCostCenter() {
    return ibmDeptCostCenter;
  }

  public void setIbmDeptCostCenter(String ibmDeptCostCenter) {
    this.ibmDeptCostCenter = ibmDeptCostCenter;
  }

  public String getSalesTeamCd() {
    return salesTeamCd;
  }

  public void setSalesTeamCd(String salesTeamCd) {
    this.salesTeamCd = salesTeamCd;
  }

  public String getSpecialTaxCd() {
    return specialTaxCd;
  }

  public void setSpecialTaxCd(String specialTaxCd) {
    this.specialTaxCd = specialTaxCd;
  }

  public String getEngineeringBo() {
    return engineeringBo;
  }

  public void setEngineeringBo(String engineeringBo) {
    this.engineeringBo = engineeringBo;
  }

  public String getPrivIndc() {
    return privIndc;
  }

  public void setPrivIndc(String privIndc) {
    this.privIndc = privIndc;
  }

  public String getModeOfPayment() {
    return modeOfPayment;
  }

  public void setModeOfPayment(String modeOfPayment) {
    this.modeOfPayment = modeOfPayment;
  }

  public String getLocationNumber() {
    return locationNumber;
  }

  public void setLocationNumber(String locationNumber) {
    this.locationNumber = locationNumber;
  }

  public String getCurrencyCd() {
    return currencyCd;
  }

  public void setCurrencyCd(String currencyCd) {
    this.currencyCd = currencyCd;
  }

  public String getMailingCondition() {
    return mailingCondition;
  }

  public void setMailingCondition(String mailingCondition) {
    this.mailingCondition = mailingCondition;
  }

  public String getSalesBusOffCd() {
    return salesBusOffCd;
  }

  public void setSalesBusOffCd(String salesBusOffCd) {
    this.salesBusOffCd = salesBusOffCd;
  }

  public String getCommercialFinanced() {
    return commercialFinanced;
  }

  public void setCommercialFinanced(String commercialFinanced) {
    this.commercialFinanced = commercialFinanced;
  }

  public String getLegacyCurrencyCd() {
    return legacyCurrencyCd;
  }

  public void setLegacyCurrencyCd(String legacyCurrencyCd) {
    this.legacyCurrencyCd = legacyCurrencyCd;
  }

  public String getInstallBranchOff() {
    return installBranchOff;
  }

  public void setInstallBranchOff(String installBranchOff) {
    this.installBranchOff = installBranchOff;
  }

  public String getIdentClient() {
    return identClient;
  }

  public void setIdentClient(String identClient) {
    this.identClient = identClient;
  }

  public String getBpSalesRepNo() {
    return this.bpSalesRepNo;
  }

  public void setBpSalesRepNo(String bpSalesRepNo) {
    this.bpSalesRepNo = bpSalesRepNo;
  }

  public String getEmbargoCd() {
    return this.embargoCd;
  }

  public void setEmbargoCd(String embargoCd) {
    this.embargoCd = embargoCd;
  }

  public String getAgreementSignDate() {
    return this.agreementSignDate;
  }

  public void setAgreementSignDate(String agreementSignDate) {
    this.agreementSignDate = agreementSignDate;
  }

  public String getCisServiceCustIndc() {
    return this.cisServiceCustIndc;
  }

  public void setCisServiceCustIndc(String cisServiceCustIndc) {
    this.cisServiceCustIndc = cisServiceCustIndc;
  }

  public String getFiscalDataStatus() {
    return fiscalDataStatus;
  }

  public void setFiscalDataStatus(String fiscalDataStatus) {
    this.fiscalDataStatus = fiscalDataStatus;
  }

  public String getFiscalDataCompanyNo() {
    return fiscalDataCompanyNo;
  }

  public void setFiscalDataCompanyNo(String fiscalDataCompanyNo) {
    this.fiscalDataCompanyNo = fiscalDataCompanyNo;
  }

  public String getRdcComment() {
    return rdcComment;
  }

  public void setRdcComment(String rdcComment) {
    this.rdcComment = rdcComment;
  }

  public String getAbbrevLocn() {
    return abbrevLocn;
  }

  public void setAbbrevLocn(String abbrevLocn) {
    this.abbrevLocn = abbrevLocn;
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

  public String getHwSvcsRepTeamNo() {
    return hwSvcsRepTeamNo;
  }

  public void setHwSvcsRepTeamNo(String hwSvcsRepTeamNo) {
    this.hwSvcsRepTeamNo = hwSvcsRepTeamNo;
  }

  public String getNationalCusId() {
    return nationalCusId;
  }

  public void setNationalCusId(String nationalCusId) {
    this.nationalCusId = nationalCusId;
  }

  public String getUsSicmen() {
    return usSicmen;
  }

  public void setUsSicmen(String usSicmen) {
    this.usSicmen = usSicmen;
  }

  public String getMilitary() {
    return military;
  }

  public void setMilitary(String military) {
    this.military = military;
  }

  public String getOrgNo() {
    return orgNo;
  }

  public void setOrgNo(String orgNo) {
    this.orgNo = orgNo;
  }

  public String getCusInvoiceCopies() {
    return cusInvoiceCopies;
  }

  public void setCusInvoiceCopies(String cusInvoiceCopies) {
    this.cusInvoiceCopies = cusInvoiceCopies;
  }

  public String getApCustClusterId() {
    return apCustClusterId;
  }

  public void setApCustClusterId(String apCustClusterId) {
    this.apCustClusterId = apCustClusterId;
  }

  public String getCustAcctType() {
    return custAcctType;
  }

  public void setCustAcctType(String custAcctType) {
    this.custAcctType = custAcctType;
  }

  public String getCustomerIdCd() {
    return customerIdCd;
  }

  public void setCustomerIdCd(String customerIdCd) {
    this.customerIdCd = customerIdCd;
  }
  
  public String getCollectorNameNo() {
	return collectorNameNo;
  }

  public void setCollectorNameNo(String collectorNameNo) {
    this.collectorNameNo = collectorNameNo;
  }
  
  public String getMrktChannelInd() {
    return mrktChannelInd;
  }
  
  public void setMrktChannelInd(String mrktChannelInd) {
    this.mrktChannelInd = mrktChannelInd;
  } 
  
  public String getVatInd() {
    return vatInd;
  }
  
  public void setVatInd(String vatInd) {
    this.vatInd = vatInd;
  }
}
