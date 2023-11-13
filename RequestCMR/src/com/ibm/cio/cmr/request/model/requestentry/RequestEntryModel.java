package com.ibm.cio.cmr.request.model.requestentry;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.cio.cmr.request.model.BaseModel;
import com.ibm.cio.cmr.request.ui.UIMgr;

/**
 * @author Sonali Jain
 * 
 */
public class RequestEntryModel extends BaseModel {

  private static final long serialVersionUID = 1L;

  // extra fields for general screen
  private String overallStatus;
  private String userRole;
  private String yourId;
  private String yourNm;
  private String createDate;
  private String lstUpdDate;
  private String lockedDate;

  // fields for scoreCard table
  private String findCmrDate;
  private String findDnbDate;
  private String dplChkDate;
  private String addrStdDate;
  private String vatAcknowledge;

  // field matching to ADMIN entity
  private long reqId;
  private String requesterId;
  private String requesterNm;
  private String originatorId;
  private String originatorNm;
  private String requestingLob;
  private String expediteInd;
  private String expediteReason;
  private String reqSrc;
  private String reqStatus;
  private String lockInd;
  private String lockBy;
  private Date lockTs;
  private String waitInfoInd;
  private String reqType;
  private String reqReason;
  private String custType;
  private String soeReqNo;
  // private String cmt;
  // private Date createTs;
  private String lastUpdtBy;
  private Date lastUpdtTs;
  private String delInd;
  private String lockByNm;
  private String mainAddrType;
  private String prospLegalInd;

  // fields for Comment log table

  private long cmtId;
  private String createById;
  private String cmt;
  private Date createTs;
  private String createByNm;
  private Date updateTs;
  private String cmtLockedIn;
  private String createTsString;
  // field matching to DATA entity

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
  private String memLvl;
  private String ppsceid;
  private String sapNo;
  private String searchTerm;
  private String sensitiveFlag;
  private String isicCd;
  private String sitePartyId;
  private String subIndustryCd;
  private String vat;
  private String engageSupportNum;
  private String busnType;
  private String icmsInd;
  private String govType;
  private String goeType;
  private String abbrevLocn;
  private String ibmDeptCostCenter;
  private String privIndc;
  private String countryUse;
  private String usSicmen;

  // scorecard fields
  private String dplChkResult;
  private String dplChkUsrId;
  private String dplChkUsrNm;
  private Date dplChkTs;
  private String findCmrResult;
  private String findCmrUsrId;
  private String findCmrUsrNm;
  private String findCmrRejReason;
  private String findCmrRejCmt;
  private Date findCmrTs;
  private String findDnbResult;
  private String findDnbUsrId;
  private String findDnbUsrNm;
  private String findDnbRejReason;
  private String findDnbRejCmt;
  private Date findDnbTs;
  private String addrStdResult;
  private String addrStdRejReason;
  private String addrStdRejCmt;
  private Date addrStdTs;
  private String approvalResult;
  private String approvalDateStr;
  private String approvalMaxTs;
  private String checklistStatus;

  // extra fields
  private String yourAction;
  private String claimRole;
  private String fromUrl;
  private String redirectUrl;
  private String statusChgCmt;
  private String rejectReason;
  private String rejReasonCd;
  private String rejSupplInfo1;
  private String rejSupplInfo2;

  private String enterCMRNo;
  private String cmrSearchDone;
  private String subIndustryDesc;
  private String isicDesc;
  private String saveRejectScore;
  private String hasError;
  private String dplMessage;

  // create CMR new fields
  private String mainCustNm1;
  private String mainCustNm2;
  private String covId;
  private String bgId;
  private String geoLocationCd;
  private String dunsNo;
  private String disableAutoProc;

  private String custGrp;
  private String custSubGrp;
  private String processedFlag;
  private Date processedTs;
  private String internalTyp;
  private String sepValInd;
  private String rdcProcessingStatus;
  private Date rdcProcessingTs;
  private String covBgRetrievedInd;
  private String rdcProcessingMsg;
  private String processingStatus;
  private String canClaim;
  private String canClaimAll;
  private String autoProcessing;

  // template driven
  private String restrictInd;
  private String restrictTo;
  private String oemInd;
  private String bpAcctTyp;
  private String mktgDept;
  private String mtkgArDept;
  private String pccMktgDept;
  private String pccArDept;
  private String svcArOffice;
  private String outCityLimit;
  private String csoSite;
  private String fedSiteInd;
  private String sizeCd;
  private String svcTerritoryZone;
  private String miscBillCd;
  private String taxCd3;
  private String bpName;
  private String iccTaxClass;
  private String iccTaxExemptStatus;
  private String nonIbmCompanyInd;
  private String companyNm;
  private String div;
  private String dept;
  private String dept_int;
  private String func;
  private String user;
  private String loc;
  private String ordBlk;
  private String crosTyp;
  private String crosSubTyp;
  private String bgRuleId;
  private String covDesc;
  private String bgDesc;
  private String geoLocDesc;
  private String gbgId;
  private String gbgDesc;

  // For Mass Update entry
  private int iterId;
  // for mass update emea validatio
  private int emeaSeqNo = 0;

  private String oldCustNm1;
  private String oldCustNm2;

  private String modelCmrNo;

  private List<MassUpdateModel> massUpdateList;

  // for reactivate and delete entry
  private String entryType;

  private List<ValidationUrlModel> validations;

  // LA country specific fields
  private String MrktChannelInd;
  private String contactName1;
  private String contactName2;
  private String contactName3;
  private String phone1;
  private String phone2;
  private String phone3;

  private String email1;
  private String email2;
  private String email3;
  private String collBoId;
  private String collBODesc;
  private String salesBusOffCd;
  private String mrcCd;

  private String custSubType;
  private String collectorNameNo;
  private String repTeamMemberNo;
  private String repTeamMemberName;
  private String salesTeamCd;
  private String locationNo;
  private String oldMainCustomerName1;

  private String newReqType;
  private String newReqCntry;

  private String entNo;
  private String entUpdTyp;
  private String comp;
  private String cname1;
  private String comp1;
  private String newEntpName;
  private String newEntp;
  private String newEntpNameCont;
  private String messageDefaultApproval;

  private String currencyCd;
  private String legacyCurrencyCd;
  private String collectionCd;
  private String specialTaxCd;

  private String taxExemptStatus2;
  private String taxExemptStatus3;

  public String getTaxExemptStatus2() {
    return taxExemptStatus2;
  }

  public void setTaxExemptStatus2(String taxExemptStatus2) {
    this.taxExemptStatus2 = taxExemptStatus2;
  }

  public String getTaxExemptStatus3() {
    return taxExemptStatus3;
  }

  public void setTaxExemptStatus3(String taxExemptStatus3) {
    this.taxExemptStatus3 = taxExemptStatus3;
  }

  private String acAdminBo;
  private String engineeringBo;
  private String mktgResponsibilityCd;
  private String sourceCd;
  private String orgNo;

  private String economicCd;
  private String vatExempt;
  private String vatInd;
  private String billingPstlAddr;
  private String ibmBankNumber;
  private String proxiLocnNo;
  private String locationNumber;
  private String denialCusInd;

  // AP specific fields
  private String isbuCd;
  private String sectorCd;
  private String territoryCd;
  private String apCustClusterId;

  // last proc center, use a diff name to avoid saving
  private String procCenter;

  // BR save indicator after template load for filtering addr
  private String saveIndAftrTempLoad;

  private String paymentMode;

  private String mailingCondition;

  private String cmrNoPrefix;

  private String commercialFinanced;

  private String creditCd;

  private String modeOfPayment;

  private String identClient;

  private String fiscalDataStatus;

  private String fiscalDataCompanyNo;

  // IT - Dropdown City field
  private String dropDownCity;

  // FR - Installing BO
  private String installBranchOff;

  private String streetAbbrev;
  private String addrAbbrevName;
  private String addrAbbrevLocn;

  // LA - Install team code
  private String installTeamCd;

  // CEMEA new fields
  private String bpSalesRepNo;
  private String dupCmrIndc;
  private String dupIssuingCntryCd;
  private String dupSalesRepNo;
  private String dupEnterpriseNo;
  private String dupSalesBoCd;
  private String dupIsuCd;
  private String dupClientTierCd;
  private String embargoCd;
  private String agreementSignDate;
  private String cisServiceCustIndc;
  private String rdcComment;
  private String typeofCustomer;
  // China new fields
  /*
   * private String exportCodesCountry; private String exportCodesTDODate;
   * private String exportCodesTDOIndicator;
   */
  private String custAcctType;
  private String bioChemMissleMfg;

  // JP new fields
  private Date requestDueDate;
  private String requestDueDateTemp;

  private String nationalCusId;
  private String leasingCompanyIndc;
  private String educAllowCd;
  // private String estabNo;
  private String jsicCd;
  private String tier2;
  private String csBo;
  private String csDiv;
  private String iinInd;
  private String valueAddRem;
  private String siInd;
  private String crsCd;
  private String billingProcCd;
  private String channelCd;
  private String invoiceSplitCd;
  private String billToCustNo;
  private String creditToCustNo;
  // private String govOfficeDivCd;
  private String dealerNo;
  private String soProjectCd;
  // private String prodType;
  private String adminDeptCd;
  private String adminDeptLine;
  private String chargeCd;
  private String outsourcingService;
  private String creditBp;
  private String zseriesSw;
  private String cmrNo2;

  // JP new fields for ProdType
  private String codCondition;
  private String remoteCustInd;
  private String decentralizedOptIndc;
  private String importActIndc;
  private String footnoteNo;
  private String fomeZero;
  private int companySize;

  // JP new fields for AR
  private String jpCloseDays;
  private String jpCloseDays1;
  private String jpCloseDays2;
  private String jpCloseDays3;
  private String jpCloseDays4;
  private String jpCloseDays5;
  private String jpCloseDays6;
  private String jpCloseDays7;
  private String jpCloseDays8;

  private String jpPayCycles;
  private String jpPayCycles1;
  private String jpPayCycles2;
  private String jpPayCycles3;
  private String jpPayCycles4;
  private String jpPayCycles5;
  private String jpPayCycles6;
  private String jpPayCycles7;
  private String jpPayCycles8;

  private String jpPayDays;
  private String jpPayDays1;
  private String jpPayDays2;
  private String jpPayDays3;
  private String jpPayDays4;
  private String jpPayDays5;
  private String jpPayDays6;
  private String jpPayDays7;
  private String jpPayDays8;

  // CH Specific
  private String custLangCd;
  private String hwInstlMstrFlg;
  private String divn;

  private Map<String, Object> legacyDirectMassUpdtList;

  private String comment;
  // Italy Legacy Fields
  private String hwSvcsRepTeamNo;

  // automation specific
  private String compVerifiedIndc;
  private String scenarioVerifiedIndc;
  private String compInfoSrc;
  private String matchIndc;
  private String matchOverrideIndc;
  private String dupCmrReason;
  private String paygoProcessIndc;

  // CMR-5910 - military flag
  private String military;

  private String extWalletId;

  // dpl assessment internal fields
  private String intDplAssessmentResult;
  private String intDplAssessmentDate;
  private String intDplAssessmentBy;
  private String intDplAssessmentCmt;

  // canada fields
  private String invoiceDistCd;
  private String cusInvoiceCopies;
  private String taxPayerCustCd;

  private String sourceSystId;

  // taiwan fields
  private String footnoteTxt1;
  private String footnoteTxt2;
  private String customerIdCd;

  // korea fields
  private String installRep;
  private String mexicoBillingName;

  private long overrideReqId;

  // new JP fields
  private String marketingContCd;

  public String getMexicoBillingName() {
    return mexicoBillingName;
  }

  public void setMexicoBillingName(String mexicoBillingName) {
    this.mexicoBillingName = mexicoBillingName;
  }

  public String getMatchIndc() {
    return matchIndc;
  }

  public void setMatchIndc(String matchIndc) {
    this.matchIndc = matchIndc;
  }

  public String getMatchOverrideIndc() {
    return matchOverrideIndc;
  }

  public void setMatchOverrideIndc(String matchOverrideIndc) {
    this.matchOverrideIndc = matchOverrideIndc;
  }

  public int getCompanySize() {
    return companySize;
  }

  public void setCompanySize(int companySize) {
    this.companySize = companySize;
  }

  public String getCreditCd() {
    return creditCd;
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

  public String getSpecialTaxCd() {
    return specialTaxCd;
  }

  public void setSpecialTaxCd(String specialTaxCd) {
    this.specialTaxCd = specialTaxCd;
  }

  public String getAcAdminBo() {
    return acAdminBo;
  }

  public void setAcAdminBo(String acAdminBo) {
    this.acAdminBo = acAdminBo;
  }

  public String getEngineeringBo() {
    return engineeringBo;
  }

  public void setEngineeringBo(String engineeringBo) {
    this.engineeringBo = engineeringBo;
  }

  public String getMktgResponsibilityCd() {
    return mktgResponsibilityCd;
  }

  public void setMktgResponsibilityCd(String mktgResponsibilityCd) {
    this.mktgResponsibilityCd = mktgResponsibilityCd;
  }

  public String getSourceCd() {
    return sourceCd;
  }

  public void setSourceCd(String sourceCd) {
    this.sourceCd = sourceCd;
  }

  public String getOrgNo() {
    return orgNo;
  }

  public void setOrgNo(String orgNo) {
    this.orgNo = orgNo;
  }

  public String getCurrencyCd() {
    return currencyCd;
  }

  public String getCollectionCd() {
    return collectionCd;
  }

  public void setCollectionCd(String collectionCd) {
    this.collectionCd = collectionCd;
  }

  public void setCurrencyCd(String currencyCd) {
    this.currencyCd = currencyCd;
  }

  public String getLegacyCurrencyCd() {
    return legacyCurrencyCd;
  }

  public void setLegacyCurrencyCd(String legacyCurrencyCd) {
    this.legacyCurrencyCd = legacyCurrencyCd;
  }

  @Override
  public boolean allKeysAssigned() {
    return this.reqId > 0;
  }

  @Override
  public String getRecordDescription() {
    return UIMgr.getText("record.request");
  }

  @Override
  public void addKeyParameters(ModelAndView mv) {
    mv.addObject("reqId", this.reqId);
  }

  @Override
  public void addKeyParameters(ModelMap map) {
    map.addAttribute("reqId", this.reqId);
  }

  // GETTERS AND SETTERS

  public long getReqId() {
    return reqId;
  }

  public void setReqId(long reqId) {
    this.reqId = reqId;
  }

  public String getRequesterId() {
    return requesterId;
  }

  public void setRequesterId(String requesterId) {
    this.requesterId = requesterId;
  }

  public String getRequesterNm() {
    return requesterNm;
  }

  public void setRequesterNm(String requesterNm) {
    this.requesterNm = requesterNm;
  }

  public String getOriginatorId() {
    return originatorId;
  }

  public void setOriginatorId(String originatorId) {
    this.originatorId = originatorId;
  }

  public String getOriginatorNm() {
    return originatorNm;
  }

  public void setOriginatorNm(String originatorNm) {
    this.originatorNm = originatorNm;
  }

  public String getRequestingLob() {
    return requestingLob;
  }

  public void setRequestingLob(String requestingLob) {
    this.requestingLob = requestingLob;
  }

  public String getExpediteInd() {
    return expediteInd;
  }

  public void setExpediteInd(String expediteInd) {
    this.expediteInd = expediteInd;
  }

  public String getExpediteReason() {
    return expediteReason;
  }

  public void setExpediteReason(String expediteReason) {
    this.expediteReason = expediteReason;
  }

  public String getReqSrc() {
    return reqSrc;
  }

  public void setReqSrc(String reqSrc) {
    this.reqSrc = reqSrc;
  }

  public String getReqStatus() {
    return reqStatus;
  }

  public void setReqStatus(String reqStatus) {
    this.reqStatus = reqStatus;
  }

  public String getLockInd() {
    return lockInd;
  }

  public void setLockInd(String lockInd) {
    this.lockInd = lockInd;
  }

  public String getLockBy() {
    return lockBy;
  }

  public void setLockBy(String lockBy) {
    this.lockBy = lockBy;
  }

  public Date getLockTs() {
    return lockTs;
  }

  public void setLockTs(Date lockTs) {
    this.lockTs = lockTs;
  }

  public String getWaitInfoInd() {
    return waitInfoInd;
  }

  public void setWaitInfoInd(String waitInfoInd) {
    this.waitInfoInd = waitInfoInd;
  }

  public String getReqType() {
    return reqType;
  }

  public void setReqType(String reqType) {
    this.reqType = reqType;
  }

  public String getReqReason() {
    return reqReason;
  }

  public void setReqReason(String reqReason) {
    this.reqReason = reqReason;
  }

  public String getCustType() {
    return custType;
  }

  public void setCustType(String custType) {
    this.custType = custType;
  }

  public String getSoeReqNo() {
    return soeReqNo;
  }

  public void setSoeReqNo(String soeReqNo) {
    this.soeReqNo = soeReqNo;
  }

  public String getCmt() {
    return cmt;
  }

  public void setCmt(String cmt) {
    this.cmt = cmt;
  }

  public Date getCreateTs() {
    return createTs;
  }

  public void setCreateTs(Date createTs) {
    this.createTs = createTs;
  }

  public String getLastUpdtBy() {
    return lastUpdtBy;
  }

  public void setLastUpdtBy(String lastUpdtBy) {
    this.lastUpdtBy = lastUpdtBy;
  }

  public Date getLastUpdtTs() {
    return lastUpdtTs;
  }

  public void setLastUpdtTs(Date lastUpdtTs) {
    this.lastUpdtTs = lastUpdtTs;
  }

  public String getDelInd() {
    return delInd;
  }

  public void setDelInd(String delInd) {
    this.delInd = delInd;
  }

  public String getLockByNm() {
    return lockByNm;
  }

  public void setLockByNm(String lockByNm) {
    this.lockByNm = lockByNm;
  }

  public String getMainAddrType() {
    return mainAddrType;
  }

  public void setMainAddrType(String mainAddrType) {
    this.mainAddrType = mainAddrType;
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

  public String getModeOfPayment() {
    return modeOfPayment;
  }

  public void setModeOfPayment(String modeOfPayment) {
    this.modeOfPayment = modeOfPayment;
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

  public String getSapNo() {
    return sapNo;
  }

  public void setSapNo(String sapNo) {
    this.sapNo = sapNo;
  }

  public String getSearchTerm() {
    return searchTerm;
  }

  public void setSearchTerm(String searchTerm) {
    this.searchTerm = searchTerm;
  }

  public String getTaxPayerCustCd() {
    return taxPayerCustCd;
  }

  public void setTaxPayerCustCd(String taxPayerCustCd) {
    this.taxPayerCustCd = taxPayerCustCd;
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

  public String getOverallStatus() {
    return overallStatus;
  }

  public void setOverallStatus(String overallStatus) {
    this.overallStatus = overallStatus;
  }

  public String getUserRole() {
    return userRole;
  }

  public void setUserRole(String userRole) {
    this.userRole = userRole;
  }

  public String getDplChkResult() {
    return dplChkResult;
  }

  public void setDplChkResult(String dplChkResult) {
    this.dplChkResult = dplChkResult;
  }

  public String getDplChkUsrId() {
    return dplChkUsrId;
  }

  public void setDplChkUsrId(String dplChkUsrId) {
    this.dplChkUsrId = dplChkUsrId;
  }

  public String getDplChkUsrNm() {
    return dplChkUsrNm;
  }

  public void setDplChkUsrNm(String dplChkUsrNm) {
    this.dplChkUsrNm = dplChkUsrNm;
  }

  public String getFindCmrResult() {
    return findCmrResult;
  }

  public void setFindCmrResult(String findCmrResult) {
    this.findCmrResult = findCmrResult;
  }

  public String getFindCmrUsrId() {
    return findCmrUsrId;
  }

  public void setFindCmrUsrId(String findCmrUsrId) {
    this.findCmrUsrId = findCmrUsrId;
  }

  public String getFindCmrUsrNm() {
    return findCmrUsrNm;
  }

  public void setFindCmrUsrNm(String findCmrUsrNm) {
    this.findCmrUsrNm = findCmrUsrNm;
  }

  public String getFindCmrRejReason() {
    return findCmrRejReason;
  }

  public void setFindCmrRejReason(String findCmrRejReason) {
    this.findCmrRejReason = findCmrRejReason;
  }

  public String getFindCmrRejCmt() {
    return findCmrRejCmt;
  }

  public void setFindCmrRejCmt(String findCmrRejCmt) {
    this.findCmrRejCmt = findCmrRejCmt;
  }

  public String getFindDnbResult() {
    return findDnbResult;
  }

  public void setFindDnbResult(String findDnbResult) {
    this.findDnbResult = findDnbResult;
  }

  public String getFindDnbUsrId() {
    return findDnbUsrId;
  }

  public void setFindDnbUsrId(String findDnbUsrId) {
    this.findDnbUsrId = findDnbUsrId;
  }

  public String getFindDnbUsrNm() {
    return findDnbUsrNm;
  }

  public void setFindDnbUsrNm(String findDnbUsrNm) {
    this.findDnbUsrNm = findDnbUsrNm;
  }

  public String getFindDnbRejReason() {
    return findDnbRejReason;
  }

  public void setFindDnbRejReason(String findDnbRejReason) {
    this.findDnbRejReason = findDnbRejReason;
  }

  public String getFindDnbRejCmt() {
    return findDnbRejCmt;
  }

  public void setFindDnbRejCmt(String findDnbRejCmt) {
    this.findDnbRejCmt = findDnbRejCmt;
  }

  public String getAddrStdResult() {
    return addrStdResult;
  }

  public void setAddrStdResult(String addrStdResult) {
    this.addrStdResult = addrStdResult;
  }

  public String getAddrStdRejReason() {
    return addrStdRejReason;
  }

  public void setAddrStdRejReason(String addrStdRejReason) {
    this.addrStdRejReason = addrStdRejReason;
  }

  public String getAddrStdRejCmt() {
    return addrStdRejCmt;
  }

  public void setAddrStdRejCmt(String addrStdRejCmt) {
    this.addrStdRejCmt = addrStdRejCmt;
  }

  public String getYourAction() {
    return yourAction;
  }

  public void setYourAction(String yourAction) {
    this.yourAction = yourAction;
  }

  public String getFromUrl() {
    return fromUrl;
  }

  public void setFromUrl(String fromUrl) {
    this.fromUrl = fromUrl;
  }

  public String getClaimRole() {
    return claimRole;
  }

  public void setClaimRole(String claimRole) {
    this.claimRole = claimRole;
  }

  public String getCreateDate() {
    return createDate;
  }

  public void setCreateDate(String createDate) {
    this.createDate = createDate;
  }

  public String getLstUpdDate() {
    return lstUpdDate;
  }

  public void setLstUpdDate(String lstUpdDate) {
    this.lstUpdDate = lstUpdDate;
  }

  public String getLockedDate() {
    return lockedDate;
  }

  public void setLockedDate(String lockedDate) {
    this.lockedDate = lockedDate;
  }

  public String getYourId() {
    return yourId;
  }

  public void setYourId(String yourId) {
    this.yourId = yourId;
  }

  public String getYourNm() {
    return yourNm;
  }

  public void setYourNm(String yourNm) {
    this.yourNm = yourNm;
  }

  public String getFindCmrDate() {
    return findCmrDate;
  }

  public void setFindCmrDate(String findCmrDate) {
    this.findCmrDate = findCmrDate;
  }

  public String getFindDnbDate() {
    return findDnbDate;
  }

  public void setFindDnbDate(String findDnbDate) {
    this.findDnbDate = findDnbDate;
  }

  public String getDplChkDate() {
    return dplChkDate;
  }

  public void setDplChkDate(String dplChkDate) {
    this.dplChkDate = dplChkDate;
  }

  public String getAddrStdDate() {
    return addrStdDate;
  }

  public void setAddrStdDate(String addrStdDate) {
    this.addrStdDate = addrStdDate;
  }

  public String getEngageSupportNum() {
    return engageSupportNum;
  }

  public void setEngageSupportNum(String engageSupportNum) {
    this.engageSupportNum = engageSupportNum;
  }

  public String getStatusChgCmt() {
    return statusChgCmt;
  }

  public void setStatusChgCmt(String statusChgCmt) {
    this.statusChgCmt = statusChgCmt;
  }

  public String getRejectReason() {
    return rejectReason;
  }

  public void setRejectReason(String rejectReason) {
    this.rejectReason = rejectReason;
  }

  public String getRejReasonCd() {
    return rejReasonCd;
  }

  public void setRejReasonCd(String rejReasonCd) {
    this.rejReasonCd = rejReasonCd;
  }

  public String getRedirectUrl() {
    return redirectUrl;
  }

  public void setRedirectUrl(String redirectUrl) {
    this.redirectUrl = redirectUrl;
  }

  public String getEnterCMRNo() {
    return enterCMRNo;
  }

  public void setEnterCMRNo(String enterCMRNo) {
    this.enterCMRNo = enterCMRNo;
  }

  public String getCmrSearchDone() {
    return cmrSearchDone;
  }

  public void setCmrSearchDone(String cmrSearchDone) {
    this.cmrSearchDone = cmrSearchDone;
  }

  public String getSubIndustryDesc() {
    return subIndustryDesc;
  }

  public void setSubIndustryDesc(String subIndustryDesc) {
    this.subIndustryDesc = subIndustryDesc;
  }

  public String getIsicDesc() {
    return isicDesc;
  }

  public void setIsicDesc(String isicDesc) {
    this.isicDesc = isicDesc;
  }

  public String getSaveRejectScore() {
    return saveRejectScore;
  }

  public void setSaveRejectScore(String saveRejectScore) {
    this.saveRejectScore = saveRejectScore;
  }

  public String getProspLegalInd() {
    return prospLegalInd;
  }

  public void setProspLegalInd(String prospLegalInd) {
    this.prospLegalInd = prospLegalInd;
  }

  public String getHasError() {
    return hasError;
  }

  public void setHasError(String hasError) {
    this.hasError = hasError;
  }

  public String getDplMessage() {
    return dplMessage;
  }

  public void setDplMessage(String dplMessage) {
    this.dplMessage = dplMessage;
  }

  public long getCmtId() {
    return cmtId;
  }

  public void setCmtId(long cmtId) {
    this.cmtId = cmtId;
  }

  public String getCreateById() {
    return createById;
  }

  public void setCreateById(String createById) {
    this.createById = createById;
  }

  public String getCreateByNm() {
    return createByNm;
  }

  public void setCreateByNm(String createByNm) {
    this.createByNm = createByNm;
  }

  public Date getUpdateTs() {
    return updateTs;
  }

  public void setUpdateTs(Date updateTs) {
    this.updateTs = updateTs;
  }

  public String getCmtLockedIn() {
    return cmtLockedIn;
  }

  public void setCmtLockedIn(String cmtLockedIn) {
    this.cmtLockedIn = cmtLockedIn;
  }

  public String getCreateTsString() {
    return createTsString;
  }

  public void setCreateTsString(String createTsString) {
    this.createTsString = createTsString;
  }

  public String getMainCustNm1() {
    return mainCustNm1;
  }

  public void setMainCustNm1(String mainCustNm1) {
    this.mainCustNm1 = mainCustNm1;
  }

  public String getMainCustNm2() {
    return mainCustNm2;
  }

  public void setMainCustNm2(String mainCustNm2) {
    this.mainCustNm2 = mainCustNm2;
  }

  public String getDunsNo() {
    return dunsNo;
  }

  public void setDunsNo(String dunsNo) {
    this.dunsNo = dunsNo;
  }

  public String getDisableAutoProc() {
    return disableAutoProc;
  }

  public void setDisableAutoProc(String disableAutoProc) {
    this.disableAutoProc = disableAutoProc;
  }

  public String getCustGrp() {
    return custGrp;
  }

  public void setCustGrp(String custGrp) {
    this.custGrp = custGrp;
  }

  public String getCustSubGrp() {
    return custSubGrp;
  }

  public void setCustSubGrp(String custSubGrp) {
    this.custSubGrp = custSubGrp;
  }

  public String getProcessedFlag() {
    return processedFlag;
  }

  public void setProcessedFlag(String processedFlag) {
    this.processedFlag = processedFlag;
  }

  public Date getProcessedTs() {
    return processedTs;
  }

  public void setProcessedTs(Date processedTs) {
    this.processedTs = processedTs;
  }

  public String getInternalTyp() {
    return internalTyp;
  }

  public void setInternalTyp(String internalTyp) {
    this.internalTyp = internalTyp;
  }

  public String getSepValInd() {
    return sepValInd;
  }

  public void setSepValInd(String sepValInd) {
    this.sepValInd = sepValInd;
  }

  public String getRdcProcessingStatus() {
    return rdcProcessingStatus;
  }

  public void setRdcProcessingStatus(String rdcProcessingStatus) {
    this.rdcProcessingStatus = rdcProcessingStatus;
  }

  public Date getRdcProcessingTs() {
    return rdcProcessingTs;
  }

  public void setRdcProcessingTs(Date rdcProcessingTs) {
    this.rdcProcessingTs = rdcProcessingTs;
  }

  public String getCovBgRetrievedInd() {
    return covBgRetrievedInd;
  }

  public void setCovBgRetrievedInd(String covBgRetrievedInd) {
    this.covBgRetrievedInd = covBgRetrievedInd;
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

  public String getGeoLocationCd() {
    return geoLocationCd;
  }

  public void setGeoLocationCd(String geoLocationCd) {
    this.geoLocationCd = geoLocationCd;
  }

  public String getProcessingStatus() {
    return processingStatus;
  }

  public void setProcessingStatus(String processingStatus) {
    this.processingStatus = processingStatus;
  }

  public String getCanClaim() {
    return canClaim;
  }

  public void setCanClaim(String canClaim) {
    this.canClaim = canClaim;
  }

  public String getCanClaimAll() {
    return canClaimAll;
  }

  public void setCanClaimAll(String canClaimAll) {
    this.canClaimAll = canClaimAll;
  }

  public String getAutoProcessing() {
    return autoProcessing;
  }

  public void setAutoProcessing(String autoProcessing) {
    this.autoProcessing = autoProcessing;
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

  public String getTaxCd3() {
    return taxCd3;
  }

  public void setTaxCd3(String taxCd3) {
    this.taxCd3 = taxCd3;
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

  public String getRdcProcessingMsg() {
    return rdcProcessingMsg;
  }

  public void setRdcProcessingMsg(String rdcProcessingMsg) {
    this.rdcProcessingMsg = rdcProcessingMsg;
  }

  public String getCompanyNm() {
    return companyNm;
  }

  public void setCompanyNm(String companyNm) {
    this.companyNm = companyNm;
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

  public String getOrdBlk() {
    return ordBlk;
  }

  public void setOrdBlk(String ordBlk) {
    this.ordBlk = ordBlk;
  }

  public String getBgRuleId() {
    return bgRuleId;
  }

  public void setBgRuleId(String bgRuleId) {
    this.bgRuleId = bgRuleId;
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

  public String getGeoLocDesc() {
    return geoLocDesc;
  }

  public void setGeoLocDesc(String geoLocDesc) {
    this.geoLocDesc = geoLocDesc;
  }

  public String getGbgId() {
    return gbgId;
  }

  public void setGbgId(String gbgId) {
    this.gbgId = gbgId;
  }

  public String getGbgDesc() {
    return gbgDesc;
  }

  public void setGbgDesc(String gbgDesc) {
    this.gbgDesc = gbgDesc;
  }

  public int getIterId() {
    return iterId;
  }

  public void setIterId(int iterId) {
    this.iterId = iterId;
  }

  public List<MassUpdateModel> getMassUpdateList() {
    return massUpdateList;
  }

  public void setMassUpdateList(List<MassUpdateModel> massUpdateList) {
    this.massUpdateList = massUpdateList;
  }

  public String getEntryType() {
    return entryType;
  }

  public void setEntryType(String entryType) {
    this.entryType = entryType;
  }

  public String getOldCustNm1() {
    return oldCustNm1;
  }

  public void setOldCustNm1(String oldCustNm1) {
    this.oldCustNm1 = oldCustNm1;
  }

  public String getOldCustNm2() {
    return oldCustNm2;
  }

  public void setOldCustNm2(String oldCustNm2) {
    this.oldCustNm2 = oldCustNm2;
  }

  public List<ValidationUrlModel> getValidations() {
    return validations;
  }

  public void setValidations(List<ValidationUrlModel> validations) {
    this.validations = validations;
  }

  public String getDept_int() {
    return dept_int;
  }

  public void setDept_int(String dept_int) {
    this.dept_int = dept_int;
  }

  public String getModelCmrNo() {
    return modelCmrNo;
  }

  public void setModelCmrNo(String modelCmrNo) {
    this.modelCmrNo = modelCmrNo;
  }

  public String getBusnType() {
    return busnType;
  }

  public void setBusnType(String busnType) {
    this.busnType = busnType;
  }

  public String getIcmsInd() {
    return icmsInd;
  }

  public void setIcmsInd(String icmsInd) {
    this.icmsInd = icmsInd;
  }

  public String getGovType() {
    return govType;
  }

  public void setGovType(String govType) {
    this.govType = govType;
  }

  public String getGoeType() {
    return goeType;
  }

  public void setGoeType(String goeType) {
    this.goeType = goeType;
  }

  public String getMrktChannelInd() {
    return MrktChannelInd;
  }

  public void setMrktChannelInd(String mrktChannelInd) {
    MrktChannelInd = mrktChannelInd;
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

  public String getCollBoId() {
    return collBoId;
  }

  public void setCollBoId(String collBoId) {
    this.collBoId = collBoId;
  }

  public String getCollBODesc() {
    return collBODesc;
  }

  public void setCollBODesc(String collBODesc) {
    this.collBODesc = collBODesc;
  }

  public String getSalesBusOffCd() {
    return salesBusOffCd;
  }

  public void setSalesBusOffCd(String salesBusOffCd) {
    this.salesBusOffCd = salesBusOffCd;
  }

  public String getMrcCd() {
    return mrcCd;
  }

  public void setMrcCd(String mrcCd) {
    this.mrcCd = mrcCd;
  }

  public String getCustSubType() {
    return custSubType;
  }

  public void setCustSubType(String custSubType) {
    this.custSubType = custSubType;
  }

  public String getCollectorNameNo() {
    return collectorNameNo;
  }

  public void setCollectorNameNo(String collectorNameNo) {
    this.collectorNameNo = collectorNameNo;
  }

  public String getRepTeamMemberNo() {
    return repTeamMemberNo;
  }

  public void setRepTeamMemberNo(String repTeamMemberNo) {
    this.repTeamMemberNo = repTeamMemberNo;
  }

  public String getRepTeamMemberName() {
    return repTeamMemberName;
  }

  public void setRepTeamMemberName(String repTeamMemberName) {
    this.repTeamMemberName = repTeamMemberName;
  }

  public String getSalesTeamCd() {
    return salesTeamCd;
  }

  public void setSalesTeamCd(String salesTeamCd) {
    this.salesTeamCd = salesTeamCd;
  }

  public String getApprovalResult() {
    return approvalResult;
  }

  public void setApprovalResult(String approvalResult) {
    this.approvalResult = approvalResult;
  }

  public String getApprovalDateStr() {
    return approvalDateStr;
  }

  public void setApprovalDateStr(String approvalDateStr) {
    this.approvalDateStr = approvalDateStr;
  }

  public String getApprovalMaxTs() {
    return approvalMaxTs;
  }

  public void setApprovalMaxTs(String approvalMaxTs) {
    this.approvalMaxTs = approvalMaxTs;
  }

  public String getNewReqType() {
    return newReqType;
  }

  public void setNewReqType(String newReqType) {
    this.newReqType = newReqType;
  }

  public String getNewReqCntry() {
    return newReqCntry;
  }

  public void setNewReqCntry(String newReqCntry) {
    this.newReqCntry = newReqCntry;
  }

  public String getEntNo() {
    return entNo;
  }

  public void setEntNo(String entNo) {
    this.entNo = entNo;
  }

  public String getEntUpdTyp() {
    return entUpdTyp;
  }

  public void setEntUpdTyp(String entUpdTyp) {
    this.entUpdTyp = entUpdTyp;
  }

  public String getComp() {
    return comp;
  }

  public void setComp(String comp) {
    this.comp = comp;
  }

  public String getCname1() {
    return cname1;
  }

  public void setCname1(String cname1) {
    this.cname1 = cname1;
  }

  public String getComp1() {
    return comp1;
  }

  public void setComp1(String comp1) {
    this.comp1 = comp1;
  }

  public String getNewEntpName() {
    return newEntpName;
  }

  public void setNewEntpName(String newEntpName) {
    this.newEntpName = newEntpName;
  }

  public String getNewEntpNameCont() {
    return newEntpNameCont;
  }

  public void setNewEntpNameCont(String newEntpNameCont) {
    this.newEntpNameCont = newEntpNameCont;
  }

  public String getNewEntp() {
    return newEntp;
  }

  public void setNewEntp(String newEntp) {
    this.newEntp = newEntp;
  }

  public String getCrosTyp() {
    return crosTyp;
  }

  public void setCrosTyp(String crosTyp) {
    this.crosTyp = crosTyp;
  }

  public String getCrosSubTyp() {
    return crosSubTyp;
  }

  public void setCrosSubTyp(String crosSubTyp) {
    this.crosSubTyp = crosSubTyp;
  }

  public String getMessageDefaultApproval() {
    return messageDefaultApproval;
  }

  public void setMessageDefaultApproval(String messageDefaultApproval) {
    this.messageDefaultApproval = messageDefaultApproval;
  }

  public String getEconomicCd() {
    return economicCd;
  }

  public void setEconomicCd(String economicCd) {
    this.economicCd = economicCd;
  }

  public String getAbbrevLocn() {
    return abbrevLocn;
  }

  public void setAbbrevLocn(String abbrevLocn) {
    this.abbrevLocn = abbrevLocn;
  }

  public int getEmeaSeqNo() {
    return emeaSeqNo;
  }

  public void setEmeaSeqNo(int emeaSeqNo) {
    this.emeaSeqNo = emeaSeqNo;
  }

  public String getIbmBankNumber() {
    return ibmBankNumber;
  }

  public void setIbmBankNumber(String ibmBankNo) {
    this.ibmBankNumber = ibmBankNo;
  }

  public String getOldMainCustomerName1() {
    return oldMainCustomerName1;
  }

  public void setOldMainCustomerName1(String oldMainCustomerName1) {
    this.oldMainCustomerName1 = oldMainCustomerName1;
  }

  public String getPrivIndc() {
    return privIndc;
  }

  public void setPrivIndc(String privIndc) {
    this.privIndc = privIndc;
  }

  public String getSaveIndAftrTempLoad() {
    return saveIndAftrTempLoad;
  }

  public void setSaveIndAftrTempLoad(String saveIndAftrTempLoad) {
    this.saveIndAftrTempLoad = saveIndAftrTempLoad;
  }

  public String getIbmDeptCostCenter() {
    return ibmDeptCostCenter;
  }

  public void setIbmDeptCostCenter(String ibmDeptCostCenter) {
    this.ibmDeptCostCenter = ibmDeptCostCenter;
  }

  public String getIsbuCd() {
    return isbuCd;
  }

  public void setIsbuCd(String isbuCd) {
    this.isbuCd = isbuCd;
  }

  public String getSectorCd() {
    return sectorCd;
  }

  public void setSectorCd(String sectorCd) {
    this.sectorCd = sectorCd;
  }

  public String getTerritoryCd() {
    return territoryCd;
  }

  public void setTerritoryCd(String territoryCd) {
    this.territoryCd = territoryCd;
  }

  public String getApCustClusterId() {
    return apCustClusterId;
  }

  public void setApCustClusterId(String apCustClusterId) {
    this.apCustClusterId = apCustClusterId;
  }

  public String getProcCenter() {
    return procCenter;
  }

  public void setProcCenter(String procCenter) {
    this.procCenter = procCenter;
  }

  public String getMailingCondition() {
    return mailingCondition;
  }

  public void setMailingCondition(String mailingCondition) {
    this.mailingCondition = mailingCondition;
  }

  public String getCmrNoPrefix() {
    return cmrNoPrefix;
  }

  public void setCmrNoPrefix(String cmrNoPrefix) {
    this.cmrNoPrefix = cmrNoPrefix;
  }

  public String getLocationNo() {
    return locationNo;
  }

  public void setLocationNo(String locationNo) {
    this.locationNo = locationNo;
  }

  public String getPaymentMode() {
    return paymentMode;
  }

  public void setPaymentMode(String paymentMode) {
    this.paymentMode = paymentMode;
  }

  public String getCountryUse() {
    return countryUse;
  }

  public void setCountryUse(String countryUse) {
    this.countryUse = countryUse;
  }

  public String getDropDownCity() {
    return dropDownCity;
  }

  public void setDropDownCity(String dropDownCity) {
    this.dropDownCity = dropDownCity;
  }

  public String getChecklistStatus() {
    return checklistStatus;
  }

  public void setChecklistStatus(String checklistStatus) {
    this.checklistStatus = checklistStatus;
  }

  public String getCommercialFinanced() {
    return commercialFinanced;
  }

  public void setCommercialFinanced(String commercialFinanced) {
    this.commercialFinanced = commercialFinanced;
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

  public String getInstallTeamCd() {
    return installTeamCd;
  }

  public void setInstallTeamCd(String installTeamCd) {
    this.installTeamCd = installTeamCd;
  }

  public String getStreetAbbrev() {
    return streetAbbrev;
  }

  public void setStreetAbbrev(String streetAbbrev) {
    this.streetAbbrev = streetAbbrev;
  }

  public String getAddrAbbrevName() {
    return addrAbbrevName;
  }

  public void setAddrAbbrevName(String addrAbbrevName) {
    this.addrAbbrevName = addrAbbrevName;
  }

  public String getAddrAbbrevLocn() {
    return addrAbbrevLocn;
  }

  public void setAddrAbbrevLocn(String addrAbbrevLocn) {
    this.addrAbbrevLocn = addrAbbrevLocn;
  }

  public String getBpSalesRepNo() {
    return this.bpSalesRepNo;
  }

  public void setBpSalesRepNo(String bpSalesRepNo) {
    this.bpSalesRepNo = bpSalesRepNo;
  }

  public String getDupCmrIndc() {
    return this.dupCmrIndc;
  }

  public void setDupCmrIndc(String dupCmrIndc) {
    this.dupCmrIndc = dupCmrIndc;
  }

  public String getDupIssuingCntryCd() {
    return this.dupIssuingCntryCd;
  }

  public void setDupIssuingCntryCd(String dupIssuingCntryCd) {
    this.dupIssuingCntryCd = dupIssuingCntryCd;
  }

  public String getDupSalesRepNo() {
    return this.dupSalesRepNo;
  }

  public void setDupSalesRepNo(String dupSalesRepNo) {
    this.dupSalesRepNo = dupSalesRepNo;
  }

  public String getDupEnterpriseNo() {
    return this.dupEnterpriseNo;
  }

  public void setDupEnterpriseNo(String dupEnterpriseNo) {
    this.dupEnterpriseNo = dupEnterpriseNo;
  }

  public String getDupSalesBoCd() {
    return this.dupSalesBoCd;
  }

  public void setDupSalesBoCd(String dupSalesBoCd) {
    this.dupSalesBoCd = dupSalesBoCd;
  }

  public String getDupIsuCd() {
    return this.dupIsuCd;
  }

  public void setDupIsuCd(String dupIsuCd) {
    this.dupIsuCd = dupIsuCd;
  }

  public String getDupClientTierCd() {
    return this.dupClientTierCd;
  }

  public void setDupClientTierCd(String dupClientTierCd) {
    this.dupClientTierCd = dupClientTierCd;
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

  public String getBillingPstlAddr() {
    return billingPstlAddr;
  }

  public void setBillingPstlAddr(String billingPstlAddr) {
    this.billingPstlAddr = billingPstlAddr;
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

  public String getTypeofCustomer() {
    return typeofCustomer;
  }

  public void setTypeofCustomer(String typeofCustomer) {
    this.typeofCustomer = typeofCustomer;
  }

  // public String getExportCodesCountry() {
  // return exportCodesCountry;
  // }
  //
  // public void setExportCodesCountry(String exportCodesCountry) {
  // this.exportCodesCountry = exportCodesCountry;
  // }
  //
  // public String getExportCodesTDODate() {
  // return exportCodesTDODate;
  // }
  //
  // public void setExportCodesTDODate(String exportCodesTDODate) {
  // this.exportCodesTDODate = exportCodesTDODate;
  // }
  //
  // public String getExportCodesTDOIndicator() {
  // return exportCodesTDOIndicator;
  // }
  //
  // public void setExportCodesTDOIndicator(String exportCodesTDOIndicator) {
  // this.exportCodesTDOIndicator = exportCodesTDOIndicator;
  // }

  public Date getRequestDueDate() {
    return requestDueDate;
  }

  public void setRequestDueDate(Date requestDueDate) {
    this.requestDueDate = requestDueDate;
    SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    try {
      this.requestDueDateTemp = DATE_FORMAT.format(requestDueDate);
    } catch (Exception ex) {
      this.requestDueDateTemp = "";
    }
  }

  public String getRequestDueDateTemp() {
    return requestDueDateTemp;
  }

  public void setRequestDueDateTemp(String requestDueDateTemp) {
    this.requestDueDateTemp = requestDueDateTemp;
    final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    try {
      if (StringUtils.isNotEmpty(requestDueDateTemp))
        this.requestDueDate = DATE_FORMAT.parse(requestDueDateTemp);
    } catch (Exception ex) {
      this.requestDueDate = new Date();
    }

  }

  public String getNationalCusId() {
    return nationalCusId;
  }

  public void setNationalCusId(String nationalCusId) {
    this.nationalCusId = nationalCusId;
  }

  public String getLeasingCompanyIndc() {
    return leasingCompanyIndc;
  }

  public void setLeasingCompanyIndc(String leasingCompanyIndc) {
    this.leasingCompanyIndc = leasingCompanyIndc;
  }

  public String getEducAllowCd() {
    return educAllowCd;
  }

  public void setEducAllowCd(String educAllowCd) {
    this.educAllowCd = educAllowCd;
  }

  public String getCustAcctType() {
    return custAcctType;
  }

  public void setCustAcctType(String custAcctType) {
    this.custAcctType = custAcctType;
  }

  public String getBioChemMissleMfg() {
    return bioChemMissleMfg;
  }

  public void setBioChemMissleMfg(String bioChemMissleMfg) {
    this.bioChemMissleMfg = bioChemMissleMfg;
  }

  // public String getEstabNo() {
  // return estabNo;
  // }
  //
  // public void setEstabNo(String estabNo) {
  // this.estabNo = estabNo;
  // }

  public String getJsicCd() {
    return jsicCd;
  }

  public void setJsicCd(String jsicCd) {
    this.jsicCd = jsicCd;
  }

  public String getTier2() {
    return tier2;
  }

  public void setTier2(String tier2) {
    this.tier2 = tier2;
  }

  public String getCsBo() {
    return csBo;
  }

  public void setCsBo(String csBo) {
    this.csBo = csBo;
  }

  public String getCsDiv() {
    return csDiv;
  }

  public void setCsDiv(String csDiv) {
    this.csDiv = csDiv;
  }

  public String getIinInd() {
    return iinInd;
  }

  public void setIinInd(String iinInd) {
    this.iinInd = iinInd;
  }

  public String getValueAddRem() {
    return valueAddRem;
  }

  public void setValueAddRem(String valueAddRem) {
    this.valueAddRem = valueAddRem;
  }

  public String getSiInd() {
    return siInd;
  }

  public void setSiInd(String siInd) {
    this.siInd = siInd;
  }

  public String getCrsCd() {
    return crsCd;
  }

  public void setCrsCd(String crsCd) {
    this.crsCd = crsCd;
  }

  public String getBillingProcCd() {
    return billingProcCd;
  }

  public void setBillingProcCd(String billingProcCd) {
    this.billingProcCd = billingProcCd;
  }

  public String getChannelCd() {
    return channelCd;
  }

  public void setChannelCd(String channelCd) {
    this.channelCd = channelCd;
  }

  public String getInvoiceSplitCd() {
    return invoiceSplitCd;
  }

  public void setInvoiceSplitCd(String invoiceSplitCd) {
    this.invoiceSplitCd = invoiceSplitCd;
  }

  public String getBillToCustNo() {
    return billToCustNo;
  }

  public void setBillToCustNo(String billToCustNo) {
    this.billToCustNo = billToCustNo;
  }

  public String getCreditToCustNo() {
    return creditToCustNo;
  }

  public void setCreditToCustNo(String creditToCustNo) {
    this.creditToCustNo = creditToCustNo;
  }

  // public String getGovOfficeDivCd() {
  // return govOfficeDivCd;
  // }
  //
  // public void setGovOfficeDivCd(String govOfficeDivCd) {
  // this.govOfficeDivCd = govOfficeDivCd;
  // }

  public String getDealerNo() {
    return dealerNo;
  }

  public void setDealerNo(String dealerNo) {
    this.dealerNo = dealerNo;
  }

  public String getSoProjectCd() {
    return soProjectCd;
  }

  public void setSoProjectCd(String soProjectCd) {
    this.soProjectCd = soProjectCd;
  }

  // public String getProdType() {
  // return prodType;
  // }
  //
  // public void setProdType(String prodType) {
  // this.prodType = prodType;
  // }

  public String getAdminDeptCd() {
    return adminDeptCd;
  }

  public void setAdminDeptCd(String adminDeptCd) {
    this.adminDeptCd = adminDeptCd;
  }

  public String getAdminDeptLine() {
    return adminDeptLine;
  }

  public void setAdminDeptLine(String adminDeptLine) {
    this.adminDeptLine = adminDeptLine;
  }

  public String getChargeCd() {
    return chargeCd;
  }

  public void setChargeCd(String chargeCd) {
    this.chargeCd = chargeCd;
  }

  public String getOutsourcingService() {
    return outsourcingService;
  }

  public void setOutsourcingService(String outsourcingService) {
    this.outsourcingService = outsourcingService;
  }

  public String getCreditBp() {
    return creditBp;
  }

  public void setCreditBp(String creditBp) {
    this.creditBp = creditBp;
  }

  public String getZseriesSw() {
    return zseriesSw;
  }

  public void setZseriesSw(String zseriesSw) {
    this.zseriesSw = zseriesSw;
  }

  public String getCmrNo2() {
    return cmrNo2;
  }

  public void setCmrNo2(String cmrNo2) {
    this.cmrNo2 = cmrNo2;
  }

  public String getCodCondition() {
    return codCondition;
  }

  public void setCodCondition(String codCondition) {
    this.codCondition = codCondition;
  }

  public String getRemoteCustInd() {
    return remoteCustInd;
  }

  public void setRemoteCustInd(String remoteCustInd) {
    this.remoteCustInd = remoteCustInd;
  }

  public String getDecentralizedOptIndc() {
    return decentralizedOptIndc;
  }

  public void setDecentralizedOptIndc(String decentralizedOptIndc) {
    this.decentralizedOptIndc = decentralizedOptIndc;
  }

  public String getImportActIndc() {
    return importActIndc;
  }

  public void setImportActIndc(String importActIndc) {
    this.importActIndc = importActIndc;
  }

  public String getFootnoteNo() {
    return footnoteNo;
  }

  public void setFootnoteNo(String footnoteNo) {
    this.footnoteNo = footnoteNo;
  }

  public String getFomeZero() {
    return fomeZero;
  }

  public void setFomeZero(String fomeZero) {
    this.fomeZero = fomeZero;
  }

  public String getProxiLocnNo() {
    return proxiLocnNo;
  }

  public void setProxiLocnNo(String proxiLocnNo) {
    this.proxiLocnNo = proxiLocnNo;
  }

  public String getLocationNumber() {
    return locationNumber;
  }

  public void setLocationNumber(String locationNumber) {
    this.locationNumber = locationNumber;
  }

  public Map<String, Object> getLegacyDirectMassUpdtList() {
    return legacyDirectMassUpdtList;
  }

  public void setLegacyDirectMassUpdtList(Map<String, Object> legacyDirectMassUpdtList) {
    this.legacyDirectMassUpdtList = legacyDirectMassUpdtList;
  }

  public String getDenialCusInd() {
    return denialCusInd;
  }

  public void setDenialCusInd(String denialCusInd) {
    this.denialCusInd = denialCusInd;
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

  public String getDivn() {
    return divn;
  }

  public void setDivn(String divn) {
    this.divn = divn;
  }

  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }

  public String getHwSvcsRepTeamNo() {
    return hwSvcsRepTeamNo;
  }

  public void setHwSvcsRepTeamNo(String hwSvcsRepTeamNo) {
    this.hwSvcsRepTeamNo = hwSvcsRepTeamNo;
  }

  public String getCompVerifiedIndc() {
    return compVerifiedIndc;
  }

  public void setCompVerifiedIndc(String compVerifiedIndc) {
    this.compVerifiedIndc = compVerifiedIndc;
  }

  public String getScenarioVerifiedIndc() {
    return scenarioVerifiedIndc;
  }

  public void setScenarioVerifiedIndc(String scenarioVerifiedIndc) {
    this.scenarioVerifiedIndc = scenarioVerifiedIndc;
  }

  public String getCompInfoSrc() {
    return compInfoSrc;
  }

  public void setCompInfoSrc(String compInfoSrc) {
    this.compInfoSrc = compInfoSrc;
  }

  public String getDupCmrReason() {
    return dupCmrReason;
  }

  public void setDupCmrReason(String dupCmrReason) {
    this.dupCmrReason = dupCmrReason;
  }

  public String getRejSupplInfo1() {
    return rejSupplInfo1;
  }

  public void setRejSupplInfo1(String rejSupplInfo1) {
    this.rejSupplInfo1 = rejSupplInfo1;
  }

  public String getRejSupplInfo2() {
    return rejSupplInfo2;
  }

  public void setRejSupplInfo2(String rejSupplInfo2) {
    this.rejSupplInfo2 = rejSupplInfo2;
  }

  public String getUsSicmen() {
    return usSicmen;
  }

  public void setUsSicmen(String usSicmen) {
    this.usSicmen = usSicmen;
  }

  public Date getDplChkTs() {
    return dplChkTs;
  }

  public void setDplChkTs(Date dplChkTs) {
    this.dplChkTs = dplChkTs;
  }

  public Date getFindCmrTs() {
    return findCmrTs;
  }

  public void setFindCmrTs(Date findCmrTs) {
    this.findCmrTs = findCmrTs;
  }

  public Date getFindDnbTs() {
    return findDnbTs;
  }

  public void setFindDnbTs(Date findDnbTs) {
    this.findDnbTs = findDnbTs;
  }

  public Date getAddrStdTs() {
    return addrStdTs;
  }

  public void setAddrStdTs(Date addrStdTs) {
    this.addrStdTs = addrStdTs;
  }

  public String getMilitary() {
    return military;
  }

  public void setMilitary(String military) {
    this.military = military;
  }

  public String getPaygoProcessIndc() {
    return paygoProcessIndc;
  }

  public void setPaygoProcessIndc(String paygoProcessIndc) {
    this.paygoProcessIndc = paygoProcessIndc;
  }

  public String getIntDplAssessmentResult() {
    return intDplAssessmentResult;
  }

  public void setIntDplAssessmentResult(String intDplAssessmentResult) {
    this.intDplAssessmentResult = intDplAssessmentResult;
  }

  public String getIntDplAssessmentDate() {
    return intDplAssessmentDate;
  }

  public void setIntDplAssessmentDate(String intDplAssessmentDate) {
    this.intDplAssessmentDate = intDplAssessmentDate;
  }

  public String getIntDplAssessmentBy() {
    return intDplAssessmentBy;
  }

  public void setIntDplAssessmentBy(String intDplAssessmentBy) {
    this.intDplAssessmentBy = intDplAssessmentBy;
  }

  public String getIntDplAssessmentCmt() {
    return intDplAssessmentCmt;
  }

  public void setIntDplAssessmentCmt(String intDplAssessmentCmt) {
    this.intDplAssessmentCmt = intDplAssessmentCmt;
  }

  public String getInvoiceDistCd() {
    return invoiceDistCd;
  }

  public void setInvoiceDistCd(String invoiceDistCd) {
    this.invoiceDistCd = invoiceDistCd;
  }

  public String getCusInvoiceCopies() {
    return cusInvoiceCopies;
  }

  public void setCusInvoiceCopies(String cusInvoiceCopies) {
    this.cusInvoiceCopies = cusInvoiceCopies;
  }

  public String getSourceSystId() {
    return sourceSystId;
  }

  public void setSourceSystId(String sourceSystId) {
    this.sourceSystId = sourceSystId;
  }

  public String getFootnoteTxt1() {
    return footnoteTxt1;
  }

  public void setFootnoteTxt1(String footnoteTxt1) {
    this.footnoteTxt1 = footnoteTxt1;
  }

  public String getFootnoteTxt2() {
    return footnoteTxt2;
  }

  public void setFootnoteTxt2(String footnoteTxt2) {
    this.footnoteTxt2 = footnoteTxt2;
  }

  public String getCustomerIdCd() {
    return customerIdCd;
  }

  public void setCustomerIdCd(String customerIdCd) {
    this.customerIdCd = customerIdCd;
  }

  public String getInstallRep() {
    return installRep;
  }

  public void setInstallRep(String installRep) {
    this.installRep = installRep;
  }

  public String getExtWalletId() {
    return extWalletId;
  }

  public void setExtWalletId(String extWalletId) {
    this.extWalletId = extWalletId;
  }

  public long getOverrideReqId() {
    return overrideReqId;
  }

  public void setOverrideReqId(long overrideReqId) {
    this.overrideReqId = overrideReqId;
  }

  public String getVatInd() {
    return vatInd;
  }

  public void setVatInd(String vatInd) {
    this.vatInd = vatInd;
  }

  public String getVatAcknowledge() {
    return vatAcknowledge;
  }

  public void setVatAcknowledge(String vatAcknowledge) {
    this.vatAcknowledge = vatAcknowledge;
  }

  public String getJpCloseDays() {
    return jpCloseDays;
  }

  public void setJpCloseDays(String jpCloseDays) {
    this.jpCloseDays = jpCloseDays;
  }

  public String getJpCloseDays1() {
    return jpCloseDays1;
  }

  public void setJpCloseDays1(String jpCloseDays1) {
    this.jpCloseDays1 = jpCloseDays1;
  }

  public String getJpCloseDays2() {
    return jpCloseDays2;
  }

  public void setJpCloseDays2(String jpCloseDays2) {
    this.jpCloseDays2 = jpCloseDays2;
  }

  public String getJpCloseDays3() {
    return jpCloseDays3;
  }

  public void setJpCloseDays3(String jpCloseDays3) {
    this.jpCloseDays3 = jpCloseDays3;
  }

  public String getJpCloseDays4() {
    return jpCloseDays4;
  }

  public void setJpCloseDays4(String jpCloseDays4) {
    this.jpCloseDays4 = jpCloseDays4;
  }

  public String getJpCloseDays5() {
    return jpCloseDays5;
  }

  public void setJpCloseDays5(String jpCloseDays5) {
    this.jpCloseDays5 = jpCloseDays5;
  }

  public String getJpPayCycles() {
    return jpPayCycles;
  }

  public void setJpPayCycles(String jpPayCycles) {
    this.jpPayCycles = jpPayCycles;
  }

  public String getJpPayCycles1() {
    return jpPayCycles1;
  }

  public void setJpPayCycles1(String jpPayCycles1) {
    this.jpPayCycles1 = jpPayCycles1;
  }

  public String getJpPayCycles2() {
    return jpPayCycles2;
  }

  public void setJpPayCycles2(String jpPayCycles2) {
    this.jpPayCycles2 = jpPayCycles2;
  }

  public String getJpPayCycles3() {
    return jpPayCycles3;
  }

  public void setJpPayCycles3(String jpPayCycles3) {
    this.jpPayCycles3 = jpPayCycles3;
  }

  public String getJpPayCycles4() {
    return jpPayCycles4;
  }

  public void setJpPayCycles4(String jpPayCycles4) {
    this.jpPayCycles4 = jpPayCycles4;
  }

  public String getJpPayCycles5() {
    return jpPayCycles5;
  }

  public void setJpPayCycles5(String jpPayCycles5) {
    this.jpPayCycles5 = jpPayCycles5;
  }

  public String getJpPayDays() {
    return jpPayDays;
  }

  public void setJpPayDays(String jpPayDays) {
    this.jpPayDays = jpPayDays;
  }

  public String getJpPayDays1() {
    return jpPayDays1;
  }

  public void setJpPayDays1(String jpPayDays1) {
    this.jpPayDays1 = jpPayDays1;
  }

  public String getJpPayDays2() {
    return jpPayDays2;
  }

  public void setJpPayDays2(String jpPayDays2) {
    this.jpPayDays2 = jpPayDays2;
  }

  public String getJpPayDays3() {
    return jpPayDays3;
  }

  public void setJpPayDays3(String jpPayDays3) {
    this.jpPayDays3 = jpPayDays3;
  }

  public String getJpPayDays4() {
    return jpPayDays4;
  }

  public void setJpPayDays4(String jpPayDays4) {
    this.jpPayDays4 = jpPayDays4;
  }

  public String getJpPayDays5() {
    return jpPayDays5;
  }

  public void setJpPayDays5(String jpPayDays5) {
    this.jpPayDays5 = jpPayDays5;
  }

  public String getMarketingContCd() {
    return marketingContCd;
  }

  public void setMarketingContCd(String marketingContCd) {
    this.marketingContCd = marketingContCd;
  }

  public String getJpCloseDays6() {
    return jpCloseDays6;
  }

  public void setJpCloseDays6(String jpCloseDays6) {
    this.jpCloseDays6 = jpCloseDays6;
  }

  public String getJpCloseDays7() {
    return jpCloseDays7;
  }

  public void setJpCloseDays7(String jpCloseDays7) {
    this.jpCloseDays7 = jpCloseDays7;
  }

  public String getJpCloseDays8() {
    return jpCloseDays8;
  }

  public void setJpCloseDays8(String jpCloseDays8) {
    this.jpCloseDays8 = jpCloseDays8;
  }

  public String getJpPayCycles6() {
    return jpPayCycles6;
  }

  public void setJpPayCycles6(String jpPayCycles6) {
    this.jpPayCycles6 = jpPayCycles6;
  }

  public String getJpPayCycles7() {
    return jpPayCycles7;
  }

  public void setJpPayCycles7(String jpPayCycles7) {
    this.jpPayCycles7 = jpPayCycles7;
  }

  public String getJpPayCycles8() {
    return jpPayCycles8;
  }

  public void setJpPayCycles8(String jpPayCycles8) {
    this.jpPayCycles8 = jpPayCycles8;
  }

  public String getJpPayDays6() {
    return jpPayDays6;
  }

  public void setJpPayDays6(String jpPayDays6) {
    this.jpPayDays6 = jpPayDays6;
  }

  public String getJpPayDays7() {
    return jpPayDays7;
  }

  public void setJpPayDays7(String jpPayDays7) {
    this.jpPayDays7 = jpPayDays7;
  }

  public String getJpPayDays8() {
    return jpPayDays8;
  }

  public void setJpPayDays8(String jpPayDays8) {
    this.jpPayDays8 = jpPayDays8;
  }
}
