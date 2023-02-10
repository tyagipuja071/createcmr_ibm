/**
 * 
 */
package com.ibm.cio.cmr.request.model.requestentry;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.cio.cmr.request.model.BaseModel;

/**
 * @author Eduard Bernardo
 * 
 */
public class MassUpdateModel extends BaseModel {

  private static final long serialVersionUID = 1L;

  private long parReqId;

  private int seqNo;

  private int iterationId;

  private String errorTxt;

  private String rowStatusCd;

  private String cmrNo;

  private String abbrevNm;

  private String custNm1;

  private String custNm2;

  private String custNm3;

  private String military;

  private String restrictTo;

  private String isicCd;

  private String outCityLimit;

  private String pccArDept;

  private String restrictInd;

  private String svcArOffice;

  private String affiliate;

  private String mktgArDept;

  private String csoSite;

  private String mktgDept;

  private String miscBillCd;

  private String iccTaxClass;

  private String iccTaxExemptStatus;

  private String taxCd1;

  private String taxCd2;

  private String taxCd3;

  private String isuCd;

  private String inacCd;

  private String clientTier;

  private String cmrList;

  private String parReqType;

  private boolean displayMsg = true;

  private String enterprise;

  private String vat;

  private String ordBlk;

  private String repTeamMemberNo;

  private String salesBoCd;

  private String installBranchOff;

  private String modeOfPayment;

  private String specialTaxCd;

  private String engineeringBo;

  private String collectionCd;

  private String company;

  private String abbrevLocn;

  private String subIndustryCd;

  private String taxCd;

  private String taxSeparationIndc;

  private String billingPrintIndc;

  private String contractPrintIndc;

  private String countryUse;

  private String contactName1;

  private String contactName2;

  private String contactName3;

  private String phone1;

  private String phone2;

  private String phone3;

  private String email1;

  private String email2;

  private String email3;

  private String mrktChannelInd;

  private String collBoId;

  private String salesBusOffCd;

  private String collectorNameNo;

  private String custClass;

  private String currencyCd;

  private String searchTerm;

  private String newEntp;

  private String entpUpdtTyp;

  private String newEntpName1;
  //
  // private String taxExemptStatus;
  //
  // public String getTaxExemptStatus() {
  // return taxExemptStatus;
  // }
  //
  // public void setTaxExemptStatus(String taxExemptStatus) {
  // this.taxExemptStatus = taxExemptStatus;
  // }

  private String taxExemptStatus;

  private String taxExemptStatus2;

  private String taxExemptStatus3;

  private String educAllowCd;

  private String bpAcctTyp;

  private String bpName;

  private String ibmBankNumber;

  private String codCondition;

  private String codReason;

  private String creditCode;

  private String taxNum;

  private String mexicoBillingName;

  public String getMexicoBillingName() {
    return mexicoBillingName;
  }

  public void setMexicoBillingName(String mexicoBillingName) {
    this.mexicoBillingName = mexicoBillingName;
  }

  public String getBpAcctTyp() {
    return bpAcctTyp;
  }

  public void setBpAcctTyp(String bpAcctTyp) {
    this.bpAcctTyp = bpAcctTyp;
  }

  public String getBpName() {
    return bpName;
  }

  public void setBpName(String bpName) {
    this.bpName = bpName;
  }

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

  public String getTaxExemptStatus() {
    return taxExemptStatus;
  }

  public void setTaxExemptStatus(String taxExemptStatus) {
    this.taxExemptStatus = taxExemptStatus;
  }

  // JP new fields
  private Date requestDueDate;
  private String requestDueDateTemp;

  private List<MassUpdateAddressModel> massUpdatAddr;

  public long getParReqId() {
    return parReqId;
  }

  public void setParReqId(long parReqId) {
    this.parReqId = parReqId;
  }

  public int getSeqNo() {
    return seqNo;
  }

  public void setSeqNo(int seqNo) {
    this.seqNo = seqNo;
  }

  public int getIterationId() {
    return iterationId;
  }

  public void setIterationId(int iterationId) {
    this.iterationId = iterationId;
  }

  public String getErrorTxt() {
    return errorTxt;
  }

  public void setErrorTxt(String errorTxt) {
    this.errorTxt = errorTxt;
  }

  public String getRowStatusCd() {
    return rowStatusCd;
  }

  public void setRowStatusCd(String rowStatusCd) {
    this.rowStatusCd = rowStatusCd;
  }

  public String getCmrNo() {
    return cmrNo;
  }

  public void setCmrNo(String cmrNo) {
    this.cmrNo = cmrNo;
  }

  public String getAbbrevNm() {
    return abbrevNm;
  }

  public void setAbbrevNm(String abbrevNm) {
    this.abbrevNm = abbrevNm;
  }

  public String getIsicCd() {
    return isicCd;
  }

  public void setIsicCd(String isicCd) {
    this.isicCd = isicCd;
  }

  public String getOutCityLimit() {
    return outCityLimit;
  }

  public void setOutCityLimit(String outCityLimit) {
    this.outCityLimit = outCityLimit;
  }

  public String getPccArDept() {
    return pccArDept;
  }

  public void setPccArDept(String pccArDept) {
    this.pccArDept = pccArDept;
  }

  public String getRestrictInd() {
    return restrictInd;
  }

  public void setRestrictInd(String restrictInd) {
    this.restrictInd = restrictInd;
  }

  public String getSvcArOffice() {
    return svcArOffice;
  }

  public void setSvcArOffice(String svcArOffice) {
    this.svcArOffice = svcArOffice;
  }

  public String getAffiliate() {
    return affiliate;
  }

  public void setAffiliate(String affiliate) {
    this.affiliate = affiliate;
  }

  public String getCsoSite() {
    return csoSite;
  }

  public void setCsoSite(String csoSite) {
    this.csoSite = csoSite;
  }

  public String getMktgDept() {
    return mktgDept;
  }

  public void setMktgDept(String mktgDept) {
    this.mktgDept = mktgDept;
  }

  public String getMiscBillCd() {
    return miscBillCd;
  }

  public void setMiscBillCd(String miscBillCd) {
    this.miscBillCd = miscBillCd;
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

  public String getClientTier() {
    return clientTier;
  }

  public void setClientTier(String clientTier) {
    this.clientTier = clientTier;
  }

  public List<MassUpdateAddressModel> getMassUpdatAddr() {
    return massUpdatAddr;
  }

  public void setMassUpdatAddr(List<MassUpdateAddressModel> massUpdatAddr) {
    this.massUpdatAddr = massUpdatAddr;
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

  public String getRestrictTo() {
    return restrictTo;
  }

  public void setRestrictTo(String restrictTo) {
    this.restrictTo = restrictTo;
  }

  public String getMktgArDept() {
    return mktgArDept;
  }

  public void setMktgArDept(String mktgArDept) {
    this.mktgArDept = mktgArDept;
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

  public String getCmrList() {
    return cmrList;
  }

  public void setCmrList(String cmrList) {
    this.cmrList = cmrList;
  }

  public String getParReqType() {
    return parReqType;
  }

  public void setParReqType(String parReqType) {
    this.parReqType = parReqType;
  }

  public boolean isDisplayMsg() {
    return displayMsg;
  }

  public void setDisplayMsg(boolean displayMsg) {
    this.displayMsg = displayMsg;
  }

  public String getEnterprise() {
    return enterprise;
  }

  public void setEnterprise(String enterprise) {
    this.enterprise = enterprise;
  }

  public String getVat() {
    return vat;
  }

  public void setVat(String vat) {
    this.vat = vat;
  }

  public String getSalesBoCd() {
    return salesBoCd;
  }

  public void setSalesBoCd(String salesBoCd) {
    this.salesBoCd = salesBoCd;
  }

  public String getInstallBranchOff() {
    return installBranchOff;
  }

  public void setInstallBranchOff(String installBranchOff) {
    this.installBranchOff = installBranchOff;
  }

  public String getModeOfPayment() {
    return modeOfPayment;
  }

  public void setModeOfPayment(String modeOfPayment) {
    this.modeOfPayment = modeOfPayment;
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

  public String getCollectionCd() {
    return collectionCd;
  }

  public void setCollectionCd(String collectionCd) {
    this.collectionCd = collectionCd;
  }

  public String getOrdBlk() {
    return ordBlk;
  }

  public void setOrdBlk(String ordBlk) {
    this.ordBlk = ordBlk;
  }

  public String getRepTeamMemberNo() {
    return repTeamMemberNo;
  }

  public void setRepTeamMemberNo(String repTeamMemberNo) {
    this.repTeamMemberNo = repTeamMemberNo;
  }

  public String getCompany() {
    return company;
  }

  public void setCompany(String company) {
    this.company = company;
  }

  public String getAbbrevLocn() {
    return abbrevLocn;
  }

  public void setAbbrevLocn(String abbrevLocn) {
    this.abbrevLocn = abbrevLocn;
  }

  public String getSubIndustryCd() {
    return subIndustryCd;
  }

  public void setSubIndustryCd(String subIndustryCd) {
    this.subIndustryCd = subIndustryCd;
  }

  public String getTaxCd() {
    return taxCd;
  }

  public void setTaxCd(String taxCd) {
    this.taxCd = taxCd;
  }

  public String getTaxSeparationIndc() {
    return taxSeparationIndc;
  }

  public void setTaxSeparationIndc(String taxSeparationIndc) {
    this.taxSeparationIndc = taxSeparationIndc;
  }

  public String getBillingPrintIndc() {
    return billingPrintIndc;
  }

  public void setBillingPrintIndc(String billingPrintIndc) {
    this.billingPrintIndc = billingPrintIndc;
  }

  public String getContractPrintIndc() {
    return contractPrintIndc;
  }

  public void setContractPrintIndc(String contractPrintIndc) {
    this.contractPrintIndc = contractPrintIndc;
  }

  public String getCountryUse() {
    return countryUse;
  }

  public void setCountryUse(String countryUse) {
    this.countryUse = countryUse;
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

  public String getMrktChannelInd() {
    return mrktChannelInd;
  }

  public void setMrktChannelInd(String mrktChannelInd) {
    this.mrktChannelInd = mrktChannelInd;
  }

  public String getCollBoId() {
    return collBoId;
  }

  public void setCollBoId(String collBoId) {
    this.collBoId = collBoId;
  }

  public String getSalesBusOffCd() {
    return salesBusOffCd;
  }

  public void setSalesBusOffCd(String salesBusOffCd) {
    this.salesBusOffCd = salesBusOffCd;
  }

  public String getCollectorNameNo() {
    return collectorNameNo;
  }

  public void setCollectorNameNo(String collectorNameNo) {
    this.collectorNameNo = collectorNameNo;
  }

  public String getCustClass() {
    return this.custClass;
  }

  public void setCustClass(String custClass) {
    this.custClass = custClass;
  }

  public String getCurrencyCd() {
    return this.currencyCd;
  }

  public void setCurrencyCd(String currencyCd) {
    this.currencyCd = currencyCd;
  }

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

  public String getSearchTerm() {
    return searchTerm;
  }

  public void setSearchTerm(String searchTerm) {
    this.searchTerm = searchTerm;
  }

  public String getNewEntp() {
    return newEntp;
  }

  public void setNewEntp(String newEntp) {
    this.newEntp = newEntp;
  }

  public String getEntpUpdtTyp() {
    return entpUpdtTyp;
  }

  public void setEntpUpdtTyp(String entpUpdtTyp) {
    this.entpUpdtTyp = entpUpdtTyp;
  }

  public String getNewEntpName1() {
    return newEntpName1;
  }

  public void setNewEntpName1(String newEntpName1) {
    this.newEntpName1 = newEntpName1;
  }

  public String getMilitary() {
    return military;
  }

  public void setMilitary(String military) {
    this.military = military;
  }

  public String getEducAllowCd() {
    return educAllowCd;
  }

  public void setEducAllowCd(String educAllowCd) {
    this.educAllowCd = educAllowCd;
  }

  public String getIbmBankNumber() {
    return ibmBankNumber;
  }

  public void setIbmBankNumber(String ibmBankNumber) {
    this.ibmBankNumber = ibmBankNumber;
  }

  public String getCodCondition() {
    return codCondition;
  }

  public void setCodCondition(String codCondition) {
    this.codCondition = codCondition;
  }

  public String getCodReason() {
    return codReason;
  }

  public void setCodReason(String codReason) {
    this.codReason = codReason;
  }

  public String getCreditCode() {
    return creditCode;
  }

  public void setCreditCode(String creditCode) {
    this.creditCode = creditCode;
  }

  public String getTaxNum() {
    return taxNum;
  }

  public void setTaxNum(String taxNum) {
    this.taxNum = taxNum;
  }

}
