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
public class CheckListModel extends BaseModel {

  private static final long serialVersionUID = 1L;

  private long reqId;
  private String usDplSanctioned;
  private String potentialMatch;
  private String localCustNm;
  private String localAddr;
  private String taxCd;

  private String sectionA1;
  private String sectionA2;
  private String sectionA3;
  private String sectionA4;
  private String sectionA5;
  private String sectionA6;
  private String sectionA7;
  private String sectionA8;
  private String sectionA9;
  private String sectionA10;
  private String sectionB1;
  private String sectionB2;
  private String sectionB3;
  private String sectionB4;
  private String sectionB5;
  private String sectionB6;
  private String sectionB7;
  private String sectionB8;
  private String sectionB9;
  private String sectionB10;
  private String sectionC1;
  private String sectionC2;
  private String sectionC3;
  private String sectionC4;
  private String sectionC5;
  private String sectionC6;
  private String sectionC7;
  private String sectionC8;
  private String sectionC9;
  private String sectionC10;
  private String sectionD1;
  private String sectionD2;
  private String sectionD3;
  private String sectionD4;
  private String sectionD5;
  private String sectionD6;
  private String sectionD7;
  private String sectionD8;
  private String sectionD9;
  private String sectionD10;
  private String sectionE1;
  private String sectionE2;
  private String sectionE3;
  private String sectionE4;
  private String sectionE5;
  private String sectionE6;
  private String sectionE7;
  private String sectionE8;
  private String sectionE9;
  private String sectionE10;

  private String salesMgrId;
  private String freeTxtField1;
  private String freeTxtField2;
  private String freeTxtField3;
  private String freeTxtField4;
  private String freeTxtField5;

  @Override
  public boolean allKeysAssigned() {
    return this.reqId > 0;
  }

  @Override
  public String getRecordDescription() {
    return "Checklist";
  }

  @Override
  public void addKeyParameters(ModelAndView mv) {
  }

  @Override
  public void addKeyParameters(ModelMap map) {
  }

  public long getReqId() {
    return reqId;
  }

  public void setReqId(long reqId) {
    this.reqId = reqId;
  }

  public String getUsDplSanctioned() {
    return usDplSanctioned;
  }

  public void setUsDplSanctioned(String usDplSanctioned) {
    this.usDplSanctioned = usDplSanctioned;
  }

  public String getPotentialMatch() {
    return potentialMatch;
  }

  public void setPotentialMatch(String potentialMatch) {
    this.potentialMatch = potentialMatch;
  }

  public String getLocalCustNm() {
    return localCustNm;
  }

  public void setLocalCustNm(String localCustNm) {
    this.localCustNm = localCustNm;
  }

  public String getLocalAddr() {
    return localAddr;
  }

  public void setLocalAddr(String localAddr) {
    this.localAddr = localAddr;
  }

  public String getTaxCd() {
    return taxCd;
  }

  public void setTaxCd(String taxCd) {
    this.taxCd = taxCd;
  }

  public String getSectionA1() {
    return sectionA1;
  }

  public void setSectionA1(String sectionA1) {
    this.sectionA1 = sectionA1;
  }

  public String getSectionA2() {
    return sectionA2;
  }

  public void setSectionA2(String sectionA2) {
    this.sectionA2 = sectionA2;
  }

  public String getSectionA3() {
    return sectionA3;
  }

  public void setSectionA3(String sectionA3) {
    this.sectionA3 = sectionA3;
  }

  public String getSectionA4() {
    return sectionA4;
  }

  public void setSectionA4(String sectionA4) {
    this.sectionA4 = sectionA4;
  }

  public String getSectionA5() {
    return sectionA5;
  }

  public void setSectionA5(String sectionA5) {
    this.sectionA5 = sectionA5;
  }

  public String getSectionA6() {
    return sectionA6;
  }

  public void setSectionA6(String sectionA6) {
    this.sectionA6 = sectionA6;
  }

  public String getSectionA7() {
    return sectionA7;
  }

  public void setSectionA7(String sectionA7) {
    this.sectionA7 = sectionA7;
  }

  public String getSectionA8() {
    return sectionA8;
  }

  public void setSectionA8(String sectionA8) {
    this.sectionA8 = sectionA8;
  }

  public String getSectionA9() {
    return sectionA9;
  }

  public void setSectionA9(String sectionA9) {
    this.sectionA9 = sectionA9;
  }

  public String getSectionA10() {
    return sectionA10;
  }

  public void setSectionA10(String sectionA10) {
    this.sectionA10 = sectionA10;
  }

  public String getSectionB1() {
    return sectionB1;
  }

  public void setSectionB1(String sectionB1) {
    this.sectionB1 = sectionB1;
  }

  public String getSectionB2() {
    return sectionB2;
  }

  public void setSectionB2(String sectionB2) {
    this.sectionB2 = sectionB2;
  }

  public String getSectionB3() {
    return sectionB3;
  }

  public void setSectionB3(String sectionB3) {
    this.sectionB3 = sectionB3;
  }

  public String getSectionB4() {
    return sectionB4;
  }

  public void setSectionB4(String sectionB4) {
    this.sectionB4 = sectionB4;
  }

  public String getSectionB5() {
    return sectionB5;
  }

  public void setSectionB5(String sectionB5) {
    this.sectionB5 = sectionB5;
  }

  public String getSectionB6() {
    return sectionB6;
  }

  public void setSectionB6(String sectionB6) {
    this.sectionB6 = sectionB6;
  }

  public String getSectionB7() {
    return sectionB7;
  }

  public void setSectionB7(String sectionB7) {
    this.sectionB7 = sectionB7;
  }

  public String getSectionB8() {
    return sectionB8;
  }

  public void setSectionB8(String sectionB8) {
    this.sectionB8 = sectionB8;
  }

  public String getSectionB9() {
    return sectionB9;
  }

  public void setSectionB9(String sectionB9) {
    this.sectionB9 = sectionB9;
  }

  public String getSectionB10() {
    return sectionB10;
  }

  public void setSectionB10(String sectionB10) {
    this.sectionB10 = sectionB10;
  }

  public String getSectionC1() {
    return sectionC1;
  }

  public void setSectionC1(String sectionC1) {
    this.sectionC1 = sectionC1;
  }

  public String getSectionC2() {
    return sectionC2;
  }

  public void setSectionC2(String sectionC2) {
    this.sectionC2 = sectionC2;
  }

  public String getSectionC3() {
    return sectionC3;
  }

  public void setSectionC3(String sectionC3) {
    this.sectionC3 = sectionC3;
  }

  public String getSectionC4() {
    return sectionC4;
  }

  public void setSectionC4(String sectionC4) {
    this.sectionC4 = sectionC4;
  }

  public String getSectionC5() {
    return sectionC5;
  }

  public void setSectionC5(String sectionC5) {
    this.sectionC5 = sectionC5;
  }

  public String getSectionC6() {
    return sectionC6;
  }

  public void setSectionC6(String sectionC6) {
    this.sectionC6 = sectionC6;
  }

  public String getSectionC7() {
    return sectionC7;
  }

  public void setSectionC7(String sectionC7) {
    this.sectionC7 = sectionC7;
  }

  public String getSectionC8() {
    return sectionC8;
  }

  public void setSectionC8(String sectionC8) {
    this.sectionC8 = sectionC8;
  }

  public String getSectionC9() {
    return sectionC9;
  }

  public void setSectionC9(String sectionC9) {
    this.sectionC9 = sectionC9;
  }

  public String getSectionC10() {
    return sectionC10;
  }

  public void setSectionC10(String sectionC10) {
    this.sectionC10 = sectionC10;
  }

  public String getSectionD1() {
    return sectionD1;
  }

  public void setSectionD1(String sectionD1) {
    this.sectionD1 = sectionD1;
  }

  public String getSectionD2() {
    return sectionD2;
  }

  public void setSectionD2(String sectionD2) {
    this.sectionD2 = sectionD2;
  }

  public String getSectionD3() {
    return sectionD3;
  }

  public void setSectionD3(String sectionD3) {
    this.sectionD3 = sectionD3;
  }

  public String getSectionD4() {
    return sectionD4;
  }

  public void setSectionD4(String sectionD4) {
    this.sectionD4 = sectionD4;
  }

  public String getSectionD5() {
    return sectionD5;
  }

  public void setSectionD5(String sectionD5) {
    this.sectionD5 = sectionD5;
  }

  public String getSectionD6() {
    return sectionD6;
  }

  public void setSectionD6(String sectionD6) {
    this.sectionD6 = sectionD6;
  }

  public String getSectionD7() {
    return sectionD7;
  }

  public void setSectionD7(String sectionD7) {
    this.sectionD7 = sectionD7;
  }

  public String getSectionD8() {
    return sectionD8;
  }

  public void setSectionD8(String sectionD8) {
    this.sectionD8 = sectionD8;
  }

  public String getSectionD9() {
    return sectionD9;
  }

  public void setSectionD9(String sectionD9) {
    this.sectionD9 = sectionD9;
  }

  public String getSectionD10() {
    return sectionD10;
  }

  public void setSectionD10(String sectionD10) {
    this.sectionD10 = sectionD10;
  }

  public String getSectionE1() {
    return sectionE1;
  }

  public void setSectionE1(String sectionE1) {
    this.sectionE1 = sectionE1;
  }

  public String getSectionE2() {
    return sectionE2;
  }

  public void setSectionE2(String sectionE2) {
    this.sectionE2 = sectionE2;
  }

  public String getSectionE3() {
    return sectionE3;
  }

  public void setSectionE3(String sectionE3) {
    this.sectionE3 = sectionE3;
  }

  public String getSectionE4() {
    return sectionE4;
  }

  public void setSectionE4(String sectionE4) {
    this.sectionE4 = sectionE4;
  }

  public String getSectionE5() {
    return sectionE5;
  }

  public void setSectionE5(String sectionE5) {
    this.sectionE5 = sectionE5;
  }

  public String getSectionE6() {
    return sectionE6;
  }

  public void setSectionE6(String sectionE6) {
    this.sectionE6 = sectionE6;
  }

  public String getSectionE7() {
    return sectionE7;
  }

  public void setSectionE7(String sectionE7) {
    this.sectionE7 = sectionE7;
  }

  public String getSectionE8() {
    return sectionE8;
  }

  public void setSectionE8(String sectionE8) {
    this.sectionE8 = sectionE8;
  }

  public String getSectionE9() {
    return sectionE9;
  }

  public void setSectionE9(String sectionE9) {
    this.sectionE9 = sectionE9;
  }

  public String getSectionE10() {
    return sectionE10;
  }

  public void setSectionE10(String sectionE10) {
    this.sectionE10 = sectionE10;
  }

  public String getSalesMgrId() {
    return salesMgrId;
  }

  public void setSalesMgrId(String salesMgrId) {
    this.salesMgrId = salesMgrId;
  }

  public String getFreeTxtField1() {
    return this.freeTxtField1;
  }

  public void setFreeTxtField1(String freeTxtField1) {
    this.freeTxtField1 = freeTxtField1;
  }

  public String getFreeTxtField2() {
    return this.freeTxtField2;
  }

  public void setFreeTxtField2(String freeTxtField2) {
    this.freeTxtField2 = freeTxtField2;
  }

  public String getFreeTxtField3() {
    return this.freeTxtField3;
  }

  public void setFreeTxtField3(String freeTxtField3) {
    this.freeTxtField3 = freeTxtField3;
  }

  public String getFreeTxtField4() {
    return this.freeTxtField4;
  }

  public void setFreeTxtField4(String freeTxtField4) {
    this.freeTxtField4 = freeTxtField4;
  }

  public String getFreeTxtField5() {
    return this.freeTxtField5;
  }

  public void setFreeTxtField5(String freeTxtField5) {
    this.freeTxtField5 = freeTxtField5;
  }

}
