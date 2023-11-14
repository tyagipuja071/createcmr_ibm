/**
 * 
 */
package com.ibm.cio.cmr.request.model.requestentry;

import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.cio.cmr.request.model.BaseModel;

/**
 * @author Eduard Bernardo
 * 
 */
public class MassUpdateAddressModel extends BaseModel {

  private static final long serialVersionUID = 1L;

  private long parReqId;

  private int seqNo;

  private int iterationId;

  private String addrType;

  private String divn;

  private String dept;

  private String addrTxt;

  private String addrTxt2;

  private String city1;

  private String city2;

  private String stateProv;

  private String postCd;

  private String county;

  private String custNm1;

  private String custNm2;

  private String cmrNo;

  private String addrSeqNo;

  private String poBox;

  private String landCntry;

  // CH Specific
  private String custLangCd;

  private String floor;

  private String bldg;

  private String custPhone;

  private String custFax;

  private String hwInstlMstrFlg;

  private String custNm3;

  private String custNm4;

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

  public String getAddrType() {
    return addrType;
  }

  public void setAddrType(String addrType) {
    this.addrType = addrType;
  }

  public String getDivn() {
    return divn;
  }

  public void setDivn(String divn) {
    this.divn = divn;
  }

  public String getDept() {
    return dept;
  }

  public void setDept(String dept) {
    this.dept = dept;
  }

  public String getAddrTxt() {
    return addrTxt;
  }

  public void setAddrTxt(String addrTxt) {
    this.addrTxt = addrTxt;
  }

  public String getAddrTxt2() {
    return addrTxt2;
  }

  public void setAddrTxt2(String addrTxt2) {
    this.addrTxt2 = addrTxt2;
  }

  public String getCity1() {
    return city1;
  }

  public void setCity1(String city1) {
    this.city1 = city1;
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

  public String getCounty() {
    return county;
  }

  public void setCounty(String county) {
    this.county = county;
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

  public String getCmrNo() {
    return cmrNo;
  }

  public void setCmrNo(String cmrNo) {
    this.cmrNo = cmrNo;
  }

  public String getAddrSeqNo() {
    return addrSeqNo;
  }

  public void setAddrSeqNo(String addrSeqNo) {
    this.addrSeqNo = addrSeqNo;
  }

  public String getPoBox() {
    return poBox;
  }

  public void setPoBox(String poBox) {
    this.poBox = poBox;
  }

  public String getLandCntry() {
    return landCntry;
  }

  public void setLandCntry(String landCntry) {
    this.landCntry = landCntry;
  }

  public String getCustLangCd() {
    return custLangCd;
  }

  public void setCustLangCd(String custLangCd) {
    this.custLangCd = custLangCd;
  }

  public String getFloor() {
    return floor;
  }

  public void setFloor(String floor) {
    this.floor = floor;
  }

  public String getBldg() {
    return bldg;
  }

  public void setBldg(String bldg) {
    this.bldg = bldg;
  }

  public String getCustPhone() {
    return custPhone;
  }

  public void setCustPhone(String custPhone) {
    this.custPhone = custPhone;
  }

  public String getCustFax() {
    return custFax;
  }

  public void setCustFax(String custFax) {
    this.custFax = custFax;
  }

  public String getHwInstlMstrFlg() {
    return hwInstlMstrFlg;
  }

  public void setHwInstlMstrFlg(String hwInstlMstrFlg) {
    this.hwInstlMstrFlg = hwInstlMstrFlg;
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

}
