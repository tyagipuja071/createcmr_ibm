/**
 * 
 */
package com.ibm.cio.cmr.request.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * @author Jeffrey Zamora
 * 
 */
@Entity
public class DplChkDetails implements Serializable {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  @EmbeddedId
  private AddrPK id;

  @Column(name = "DPL_CHK_RESULT")
  private String dplChkResult;

  @Column(name = "DPL_CHK_TS")
  @Temporal(TemporalType.TIMESTAMP)
  private Date dplChkTs;

  @Column(name = "DPL_CHK_BY_ID")
  private String dplChkById;

  @Column(name = "DPL_CHK_BY_NM")
  private String dplChkByNm;

  @Column(name = "DPL_CHK_ERR_LIST")
  private String dplChkErrList;

  @Column(name = "CUST_NM1")
  private String custNm1;

  @Column(name = "POST_CD")
  private String postCd;

  @Column(name = "LAND_CNTRY")
  private String landCntry;

  @Column(name = "ADDR_TXT")
  private String addrTxt;

  private String city1;

  @Column(name = "STATE_PROV")
  private String stateProv;

  public AddrPK getId() {
    return id;
  }

  public void setId(AddrPK id) {
    this.id = id;
  }

  public String getDplChkResult() {
    return dplChkResult;
  }

  public void setDplChkResult(String dplChkResult) {
    this.dplChkResult = dplChkResult;
  }

  public Date getDplChkTs() {
    return dplChkTs;
  }

  public void setDplChkTs(Date dplChkTs) {
    this.dplChkTs = dplChkTs;
  }

  public String getDplChkById() {
    return dplChkById;
  }

  public void setDplChkById(String dplChkById) {
    this.dplChkById = dplChkById;
  }

  public String getDplChkByNm() {
    return dplChkByNm;
  }

  public void setDplChkByNm(String dplChkByNm) {
    this.dplChkByNm = dplChkByNm;
  }

  public String getDplChkErrList() {
    return dplChkErrList;
  }

  public void setDplChkErrList(String dplChkErrList) {
    this.dplChkErrList = dplChkErrList;
  }

  public String getCustNm1() {
    return custNm1;
  }

  public void setCustNm1(String custNm1) {
    this.custNm1 = custNm1;
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

  public String getAddrTxt() {
    return addrTxt;
  }

  public void setAddrTxt(String addrTxt) {
    this.addrTxt = addrTxt;
  }

  public String getCity1() {
    return city1;
  }

  public void setCity1(String city1) {
    this.city1 = city1;
  }

  public String getStateProv() {
    return stateProv;
  }

  public void setStateProv(String stateProv) {
    this.stateProv = stateProv;
  }

}
