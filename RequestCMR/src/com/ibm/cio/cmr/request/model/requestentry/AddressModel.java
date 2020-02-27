package com.ibm.cio.cmr.request.model.requestentry;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.model.BaseModel;

/**
 * @author Sonali Jain
 * 
 */
public class AddressModel extends BaseModel {

  private static final long serialVersionUID = 1L;

  private long reqId;
  private String custNm1;
  private String addrType;
  private String remAddrType;
  private String saveAddrType;
  private String city1;
  private String addrTxt;
  private String addrTypeText;
  private String cmrIssuingCntry;

  // all ADDR Fields
  private String addrSeq;
  private String custNm2;
  private String custNm3;
  private String custNm4;
  private String city2;
  private String stateProv;
  private String postCd;
  private String landCntry;
  private String county;
  private String bldg;
  private String floor;
  private String office;
  private String dept;
  private String sitePartyId;
  private String poBox;
  private String poBoxCity;
  private String poBoxPostCd;
  private String custFax;
  private String custPhone;
  private String transportZone;
  private String addrStdResult;
  private String addrStdAcceptInd;
  private String addrStdRejReason;
  private String addrStdRejCmt;
  private Date addrStdTs;
  private String addrStdTsString;

  private Date createTs;
  private String createTsString;
  private Date lastUpdtTs;
  private String lastUpdtTsString;
  private String ierpSitePrtyId;
  SimpleDateFormat formatter = CmrConstants.DATE_FORMAT();

  private String cmrNo;
  private String sapNo;
  private String importInd;

  private String dplChkResult;

  private String dplChkInfo;

  private String addrStdResultText;

  private String countyName;

  private String stdCityNm;

  private String divn;

  private String addrTxt2;

  // 1164561 special case for BR(631) customer template scenario
  private String taxCd1;

  // 1164558 special case for BR(631) mapping
  private String taxCd2;

  // 1164558 special case for BR(631) mapping
  private String vat;

  private String taxOffice;

  private String prefSeqNo;

  private String updateInd;

  private String pairedSeq;

  private String filterInd;

  private String currCustType;

  private String dplChkErrList;

  //
  private String oldAddrText;

  private String oldAddrText2;

  // IT Specfic
  private String dropDownCity;
  private String locationCode;
  private String crossbStateProvPostalMapIT;
  private String paymentAddrNo;
  private String crossbCntryStateProvMapIT;

  private String countryDesc;
  private String stateProvDesc;

  private String streetAbbrev;
  private String addrAbbrevName;
  private String addrAbbrevLocn;
  private String parCmrNo;
  private String billingPstlAddr;

  // CN Specific
  private String cnCity;
  private String cnAddrTxt;
  private String cnAddrTxt2;
  private String cnCustName1;
  private String cnCustName2;
  private String cnDistrict;
  private String cnInterAddrKey;
  private String cnCustContNm;
  private String cnCustContJobTitle;
  private String cnCustContPhone2;
  private String city1DrpDown;
  private String cnCustName3;

  // JP Specific
  private String estabFuncCd;
  private String rol;
  private int companySize;
  private String contact;

  // Machine Type Serial Number
  private String machineTyp;
  private String machineSerialNo;

  // CH Specific
  private String custLangCd;
  private String hwInstlMstrFlg;

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

  public String getCustNm1() {
    return custNm1;
  }

  public void setCustNm1(String custNm1) {
    this.custNm1 = custNm1;
  }

  public String getAddrType() {
    return addrType;
  }

  public void setAddrType(String addrType) {
    this.addrType = addrType;
  }

  public String getCity1() {
    return city1;
  }

  public void setCity1(String city1) {
    this.city1 = city1;
  }

  public String getAddrTxt() {
    return addrTxt;
  }

  public void setAddrTxt(String addrTxt) {
    this.addrTxt = addrTxt;
  }

  public long getReqId() {
    return reqId;
  }

  public void setReqId(long reqId) {
    this.reqId = reqId;
  }

  public String getAddrTypeText() {
    return addrTypeText;
  }

  public void setAddrTypeText(String addrTypeText) {
    this.addrTypeText = addrTypeText;
  }

  public String getAddrSeq() {
    return addrSeq;
  }

  public void setAddrSeq(String addrSeq) {
    this.addrSeq = addrSeq;
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

  public void setOffice(String office) {
    this.office = office;
  }

  public String getDept() {
    return dept;
  }

  public void setDept(String dept) {
    this.dept = dept;
  }

  public String getSitePartyId() {
    return sitePartyId;
  }

  public void setSitePartyId(String sitePartyId) {
    this.sitePartyId = sitePartyId;
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

  public String getAddrStdResult() {
    return addrStdResult;
  }

  public void setAddrStdResult(String addrStdResult) {
    this.addrStdResult = addrStdResult;
  }

  public String getAddrStdAcceptInd() {
    return addrStdAcceptInd;
  }

  public void setAddrStdAcceptInd(String addrStdAcceptInd) {
    this.addrStdAcceptInd = addrStdAcceptInd;
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

  public Date getAddrStdTs() {
    return addrStdTs;
  }

  public void setAddrStdTs(Date addrStdTs) {
    this.addrStdTs = addrStdTs;
  }

  public String getCmrNo() {
    return cmrNo;
  }

  public void setCmrNo(String cmrNo) {
    this.cmrNo = cmrNo;
  }

  public String getSapNo() {
    return sapNo;
  }

  public void setSapNo(String sapNo) {
    this.sapNo = sapNo;
  }

  public String getAddrStdTsString() {
    return addrStdTsString;
  }

  public void setAddrStdTsString(String addrStdTsString) {
    this.addrStdTsString = addrStdTsString;
  }

  public Date getCreateTs() {
    return createTs;
  }

  public void setCreateTs(Date createTs) {
    this.createTs = createTs;
    this.createTsString = formatter.format(createTs);
  }

  public String getCreateTsString() {
    return createTsString;
  }

  public void setCreateTsString(String createTsString) {
    this.createTsString = createTsString;
  }

  public Date getLastUpdtTs() {
    return lastUpdtTs;
  }

  public void setLastUpdtTs(Date lastUpdtTs) {
    this.lastUpdtTs = lastUpdtTs;
    this.lastUpdtTsString = formatter.format(lastUpdtTs);
  }

  public String getLastUpdtTsString() {
    return lastUpdtTsString;
  }

  public void setLastUpdtTsString(String lastUpdtTsString) {
    this.lastUpdtTsString = lastUpdtTsString;
  }

  public String getImportInd() {
    return importInd;
  }

  public void setImportInd(String importInd) {
    this.importInd = importInd;
  }

  public String getDplChkResult() {
    return dplChkResult;
  }

  public void setDplChkResult(String dplChkResult) {
    this.dplChkResult = dplChkResult;
  }

  public String getDplChkInfo() {
    return dplChkInfo;
  }

  public void setDplChkInfo(String dplChkInfo) {
    this.dplChkInfo = dplChkInfo;
  }

  public String getAddrStdResultText() {
    return addrStdResultText;
  }

  public void setAddrStdResultText(String addrStdResultText) {
    this.addrStdResultText = addrStdResultText;
  }

  public String getRemAddrType() {
    return remAddrType;
  }

  public void setRemAddrType(String remAddrType) {
    this.remAddrType = remAddrType;
  }

  public String getCountyName() {
    return countyName;
  }

  public void setCountyName(String countyName) {
    this.countyName = countyName;
  }

  public String getSaveAddrType() {
    return saveAddrType;
  }

  public void setSaveAddrType(String saveAddrType) {
    this.saveAddrType = saveAddrType;
  }

  public String getStdCityNm() {
    return stdCityNm;
  }

  public void setStdCityNm(String stdCityNm) {
    this.stdCityNm = stdCityNm;
  }

  public String getDivn() {
    return divn;
  }

  public void setDivn(String divn) {
    this.divn = divn;
  }

  public String getAddrTxt2() {
    return addrTxt2;
  }

  public void setAddrTxt2(String addrTxt2) {
    this.addrTxt2 = addrTxt2;
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

  public String getCmrIssuingCntry() {
    return cmrIssuingCntry;
  }

  public void setCmrIssuingCntry(String cmrIssuingCntry) {
    this.cmrIssuingCntry = cmrIssuingCntry;
  }

  public String getUpdateInd() {
    return updateInd;
  }

  public void setUpdateInd(String updateInd) {
    this.updateInd = updateInd;
  }

  public String getPairedSeq() {
    return pairedSeq;
  }

  public void setPairedSeq(String pairedSeq) {
    this.pairedSeq = pairedSeq;
  }

  public String getFilterInd() {
    return filterInd;
  }

  public void setFilterInd(String filterInd) {
    this.filterInd = filterInd;
  }

  public String getCurrCustType() {
    return currCustType;
  }

  public void setCurrCustType(String currCustType) {
    this.currCustType = currCustType;
  }

  public String getIerpSitePrtyId() {
    return ierpSitePrtyId;
  }

  public void setIerpSitePrtyId(String ierpSitePrtyId) {
    this.ierpSitePrtyId = ierpSitePrtyId;
  }

  public String getTaxOffice() {
    return taxOffice;
  }

  public void setTaxOffice(String taxOffice) {
    this.taxOffice = taxOffice;
  }

  public String getDplChkErrList() {
    return dplChkErrList;
  }

  public void setDplChkErrList(String dplChkErrList) {
    this.dplChkErrList = dplChkErrList;
  }

  public String getOldAddrText() {
    return oldAddrText;
  }

  public void setOldAddrText(String oldAddrText) {
    this.oldAddrText = oldAddrText;
  }

  public String getOldAddrText2() {
    return oldAddrText2;
  }

  public void setOldAddrText2(String oldAddrText2) {
    this.oldAddrText2 = oldAddrText2;
  }

  public String getPrefSeqNo() {
    return prefSeqNo;
  }

  public void setPrefSeqNo(String prefSeqNo) {
    this.prefSeqNo = prefSeqNo;
  }

  public String getDropDownCity() {
    return dropDownCity;
  }

  public void setDropDownCity(String dropDownCity) {
    this.dropDownCity = dropDownCity;
  }

  public String getLocationCode() {
    return locationCode;
  }

  public void setLocationCode(String locationCode) {
    this.locationCode = locationCode;
  }

  public String getCrossbStateProvPostalMapIT() {
    return crossbStateProvPostalMapIT;
  }

  public void setCrossbStateProvPostalMapIT(String crossbStateProvPostalMapIT) {
    this.crossbStateProvPostalMapIT = crossbStateProvPostalMapIT;
  }

  public String getPaymentAddrNo() {
    return paymentAddrNo;
  }

  public void setPaymentAddrNo(String paymentAddrNo) {
    this.paymentAddrNo = paymentAddrNo;
  }

  public String getCountryDesc() {
    return countryDesc;
  }

  public void setCountryDesc(String countryDesc) {
    this.countryDesc = countryDesc;
  }

  public String getStateProvDesc() {
    return stateProvDesc;
  }

  public void setStateProvDesc(String stateProvDesc) {
    this.stateProvDesc = stateProvDesc;
  }

  public String getCrossbCntryStateProvMapIT() {
    return crossbCntryStateProvMapIT;
  }

  public void setCrossbCntryStateProvMapIT(String crossbCntryStateProvMapIT) {
    this.crossbCntryStateProvMapIT = crossbCntryStateProvMapIT;
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

  public String getParCmrNo() {
    return parCmrNo;
  }

  public void setParCmrNo(String parCmrNo) {
    this.parCmrNo = parCmrNo;
  }

  public String getBillingPstlAddr() {
    return billingPstlAddr;
  }

  public void setBillingPstlAddr(String billingPstlAddr) {
    this.billingPstlAddr = billingPstlAddr;
  }

  public String getCnCity() {
    return cnCity;
  }

  public void setCnCity(String cnCity) {
    this.cnCity = cnCity;
  }

  public String getCnAddrTxt() {
    return cnAddrTxt;
  }

  public void setCnAddrTxt(String cnAddrTxt) {
    this.cnAddrTxt = cnAddrTxt;
  }

  public String getCnAddrTxt2() {
    return cnAddrTxt2;
  }

  public void setCnAddrTxt2(String cnAddrTxt2) {
    this.cnAddrTxt2 = cnAddrTxt2;
  }

  public String getCnCustName1() {
    return cnCustName1;
  }

  public void setCnCustName1(String cnCustName1) {
    this.cnCustName1 = cnCustName1;
  }

  public String getCnCustName2() {
    return cnCustName2;
  }

  public void setCnCustName2(String cnCustName2) {
    this.cnCustName2 = cnCustName2;
  }

  public String getCnDistrict() {
    return cnDistrict;
  }

  public void setCnDistrict(String cnDistrict) {
    this.cnDistrict = cnDistrict;
  }

  public String getCnInterAddrKey() {
    return cnInterAddrKey;
  }

  public void setCnInterAddrKey(String cnInterAddrKey) {
    this.cnInterAddrKey = cnInterAddrKey;
  }

  public String getCnCustContNm() {
    return cnCustContNm;
  }

  public void setCnCustContNm(String cnCustContNm) {
    this.cnCustContNm = cnCustContNm;
  }

  public String getCnCustContJobTitle() {
    return cnCustContJobTitle;
  }

  public void setCnCustContJobTitle(String cnCustContJobTitle) {
    this.cnCustContJobTitle = cnCustContJobTitle;
  }

  public String getCnCustContPhone2() {
    return cnCustContPhone2;
  }

  public void setCnCustContPhone2(String cnCustContPhone2) {
    this.cnCustContPhone2 = cnCustContPhone2;
  }

  public String getMachineTyp() {
    return machineTyp;
  }

  public void setMachineTyp(String machineTyp) {
    this.machineTyp = machineTyp;
  }

  public String getMachineSerialNo() {
    return machineSerialNo;
  }

  public void setMachineSerialNo(String machineSerialNo) {
    this.machineSerialNo = machineSerialNo;
  }

  public String getCity1DrpDown() {
    return city1DrpDown;
  }

  public void setCity1DrpDown(String city1DrpDown) {
    this.city1DrpDown = city1DrpDown;
  }

  public String getEstabFuncCd() {
    return estabFuncCd;
  }

  public void setEstabFuncCd(String estabFuncCd) {
    this.estabFuncCd = estabFuncCd;
  }

  public String getRol() {
    return rol;
  }

  public void setRol(String rol) {
    this.rol = rol;
  }

  public int getCompanySize() {
    return companySize;
  }

  public void setCompanySize(int companySize) {
    this.companySize = companySize;
  }

  public String getContact() {
    return contact;
  }

  public void setContact(String contact) {
    this.contact = contact;
  }

  public String getCnCustName3() {
    return cnCustName3;
  }

  public void setCnCustName3(String cnCustName3) {
    this.cnCustName3 = cnCustName3;
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

}
