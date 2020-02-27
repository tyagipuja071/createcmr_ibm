package com.ibm.cio.cmr.request.model.code;

import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;

/**
 * 
 * @author Dhananjay Yadav
 */

public class ValidateFiscalDataModel {
  
//Address Data
  private String newCustNm1;
  private String oldCustNm1;
  private String newCustNm2;
  private String oldCustNm2;
  private String oldLandCntry;
  private String newLandCntry;
  private String oldCity1;
  private String newCity1;
  private String oldAddrTxt;
  private String newAddrTxt;
  private String oldPostCd;
  private String newPostCd;
  
  //Customer Data
  private String oldVat;
  private String newVat;
  private String cmrNewOld;
  private String newTaxCd1;
  private String oldTaxCd1;
  
  //IBM DATA
  private String newInacCd;
  private String oldInacCd;
  private String newIsuCd;
  private String oldIsuCd;
  private String oldAffiliate;
  private String newAffiliate;
  private String newClientTier;
  private String oldClientTier;
  private String oldEnterprise;
  private String newEnterprise;
  private String newRepTeamMemberNo;
  private String oldRepTeamMemberNo;
  private String newSalesBusOffCd;
  private String oldSalesBusOffCd;

  public String getNewCustNm1() {
    return newCustNm1;
  }

  public void setNewCustNm1(String newCustNm1) {
    this.newCustNm1 = newCustNm1;
  }

  public String getOldCustNm1() {
    return oldCustNm1;
  }

  public void setOldCustNm1(String oldCustNm1) {
    this.oldCustNm1 = oldCustNm1;
  }

  public String getNewCustNm2() {
    return newCustNm2;
  }

  public void setNewCustNm2(String newCustNm2) {
    this.newCustNm2 = newCustNm2;
  }

  public String getOldCustNm2() {
    return oldCustNm2;
  }

  public void setOldCustNm2(String oldCustNm2) {
    this.oldCustNm2 = oldCustNm2;
  }

  public String getOldLandCntry() {
    return oldLandCntry;
  }

  public void setOldLandCntry(String oldLandCntry) {
    this.oldLandCntry = oldLandCntry;
  }

  public String getNewLandCntry() {
    return newLandCntry;
  }

  public void setNewLandCntry(String newLandCntry) {
    this.newLandCntry = newLandCntry;
  }

  public String getOldCity1() {
    return oldCity1;
  }

  public void setOldCity1(String oldCity1) {
    this.oldCity1 = oldCity1;
  }

  public String getNewCity1() {
    return newCity1;
  }

  public void setNewCity1(String newCity1) {
    this.newCity1 = newCity1;
  }

  public String getOldAddrTxt() {
    return oldAddrTxt;
  }

  public void setOldAddrTxt(String oldAddrTxt) {
    this.oldAddrTxt = oldAddrTxt;
  }

  public String getNewAddrTxt() {
    return newAddrTxt;
  }

  public void setNewAddrTxt(String newAddrTxt) {
    this.newAddrTxt = newAddrTxt;
  }

  public String getOldPostCd() {
    return oldPostCd;
  }

  public void setOldPostCd(String oldPostCd) {
    this.oldPostCd = oldPostCd;
  }

  public String getNewPostCd() {
    return newPostCd;
  }

  public void setNewPostCd(String newPostCd) {
    this.newPostCd = newPostCd;
  }

  public String getOldVat() {
    return oldVat;
  }

  public void setOldVat(String oldVat) {
    this.oldVat = oldVat;
  }

  public String getNewVat() {
    return newVat;
  }

  public void setNewVat(String newVat) {
    this.newVat = newVat;
  }

  public String getCmrNewOld() {
    return cmrNewOld;
  }

  public void setCmrNewOld(String cmrNewOld) {
    this.cmrNewOld = cmrNewOld;
  }

  public String getNewTaxCd1() {
    return newTaxCd1;
  }

  public void setNewTaxCd1(String newTaxCd1) {
    this.newTaxCd1 = newTaxCd1;
  }

  public String getOldTaxCd1() {
    return oldTaxCd1;
  }

  public void setOldTaxCd1(String oldTaxCd1) {
    this.oldTaxCd1 = oldTaxCd1;
  }

  public String getNewInacCd() {
    return newInacCd;
  }

  public void setNewInacCd(String newInacCd) {
    this.newInacCd = newInacCd;
  }

  public String getOldInacCd() {
    return oldInacCd;
  }

  public void setOldInacCd(String oldInacCd) {
    this.oldInacCd = oldInacCd;
  }

  public String getNewIsuCd() {
    return newIsuCd;
  }

  public void setNewIsuCd(String newIsuCd) {
    this.newIsuCd = newIsuCd;
  }

  public String getOldIsuCd() {
    return oldIsuCd;
  }

  public void setOldIsuCd(String oldIsuCd) {
    this.oldIsuCd = oldIsuCd;
  }

  public String getOldAffiliate() {
    return oldAffiliate;
  }

  public void setOldAffiliate(String oldAffiliate) {
    this.oldAffiliate = oldAffiliate;
  }

  public String getNewAffiliate() {
    return newAffiliate;
  }

  public void setNewAffiliate(String newAffiliate) {
    this.newAffiliate = newAffiliate;
  }

  public String getNewClientTier() {
    return newClientTier;
  }

  public void setNewClientTier(String newClientTier) {
    this.newClientTier = newClientTier;
  }

  public String getOldClientTier() {
    return oldClientTier;
  }

  public void setOldClientTier(String oldClientTier) {
    this.oldClientTier = oldClientTier;
  }

  public String getOldEnterprise() {
    return oldEnterprise;
  }

  public void setOldEnterprise(String oldEnterprise) {
    this.oldEnterprise = oldEnterprise;
  }

  public String getNewEnterprise() {
    return newEnterprise;
  }

  public void setNewEnterprise(String newEnterprise) {
    this.newEnterprise = newEnterprise;
  }

  public String getNewRepTeamMemberNo() {
    return newRepTeamMemberNo;
  }

  public void setNewRepTeamMemberNo(String newRepTeamMemberNo) {
    this.newRepTeamMemberNo = newRepTeamMemberNo;
  }

  public String getOldRepTeamMemberNo() {
    return oldRepTeamMemberNo;
  }

  public void setOldRepTeamMemberNo(String oldRepTeamMemberNo) {
    this.oldRepTeamMemberNo = oldRepTeamMemberNo;
  }

  public String getNewSalesBusOffCd() {
    return newSalesBusOffCd;
  }

  public void setNewSalesBusOffCd(String newSalesBusOffCd) {
    this.newSalesBusOffCd = newSalesBusOffCd;
  }

  public String getOldSalesBusOffCd() {
    return oldSalesBusOffCd;
  }

  public void setOldSalesBusOffCd(String oldSalesBusOffCd) {
    this.oldSalesBusOffCd = oldSalesBusOffCd;
  }

  // @Override
  public boolean allKeysAssigned() {
    return false;
  }

  // @Override
  public String getRecordDescription() {
    return null;
  }

  // @Override
  public void addKeyParameters(ModelAndView mv) {
  }

  // @Override
  public void addKeyParameters(ModelMap map) {
  }
}
