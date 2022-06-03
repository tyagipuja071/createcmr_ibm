package com.ibm.cio.cmr.request.model.code;

import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.cio.cmr.request.model.BaseModel;

public class USTCRUpdtQueueModel extends BaseModel {

  private static final long serialVersionUID = 1L;

  private String mandt;

  private String tcrFileNm;

  private Long seqNo;

  private String lineContent;

  private String cmrNo;

  private String taxCustTyp1;

  private String taxClass1;

  private String taxCustTyp2;

  private String taxClass2;

  private String taxCustTyp3;

  private String taxClass3;

  private String taxExemptStatus1;

  private String taxExemptStatus2;

  private String taxExemptStatus3;

  private String procStatus;

  private String procMsg;

  private String createdBy;

  private Date createDt;

  private String updatedBy;

  private Date updateDt;

  private String katr10;

  private String createdTsStr;

  private String updatedTsStr;

  public String getMandt() {
    return mandt;
  }

  public void setMandt(String mandt) {
    this.mandt = mandt;
  }

  public String getTcrFileNm() {
    return tcrFileNm;
  }

  public void setTcrFileNm(String tcrFileNm) {
    this.tcrFileNm = tcrFileNm;
  }

  public Long getSeqNo() {
    return seqNo;
  }

  public void setSeqNo(Long seqNo) {
    this.seqNo = seqNo;
  }

  public String getLineContent() {
    return lineContent;
  }

  public void setLineContent(String lineContent) {
    this.lineContent = lineContent;
  }

  public String getCmrNo() {
    return cmrNo;
  }

  public void setCmrNo(String cmrNo) {
    this.cmrNo = cmrNo;
  }

  public String getTaxCustTyp1() {
    return taxCustTyp1;
  }

  public void setTaxCustTyp1(String taxCustTyp1) {
    this.taxCustTyp1 = taxCustTyp1;
  }

  public String getTaxClass1() {
    return taxClass1;
  }

  public void setTaxClass1(String taxClass1) {
    this.taxClass1 = taxClass1;
  }

  public String getTaxCustTyp2() {
    return taxCustTyp2;
  }

  public void setTaxCustTyp2(String taxCustTyp2) {
    this.taxCustTyp2 = taxCustTyp2;
  }

  public String getTaxClass2() {
    return taxClass2;
  }

  public void setTaxClass2(String taxClass2) {
    this.taxClass2 = taxClass2;
  }

  public String getTaxCustTyp3() {
    return taxCustTyp3;
  }

  public void setTaxCustTyp3(String taxCustTyp3) {
    this.taxCustTyp3 = taxCustTyp3;
  }

  public String getTaxClass3() {
    return taxClass3;
  }

  public void setTaxClass3(String taxClass3) {
    this.taxClass3 = taxClass3;
  }

  public String getTaxExemptStatus1() {
    return taxExemptStatus1;
  }

  public void setTaxExemptStatus1(String taxExemptStatus1) {
    this.taxExemptStatus1 = taxExemptStatus1;
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

  public String getProcStatus() {
    return procStatus;
  }

  public void setProcStatus(String procStatus) {
    this.procStatus = procStatus;
  }

  public String getProcMsg() {
    return procMsg;
  }

  public void setProcMsg(String procMsg) {
    this.procMsg = procMsg;
  }

  public String getCreatedBy() {
    return createdBy;
  }

  public void setCreatedBy(String createdBy) {
    this.createdBy = createdBy;
  }

  public Date getCreateDt() {
    return createDt;
  }

  public void setCreateDt(Date createDt) {
    this.createDt = createDt;
  }

  public String getUpdatedBy() {
    return updatedBy;
  }

  public void setUpdatedBy(String updatedBy) {
    this.updatedBy = updatedBy;
  }

  public Date getUpdateDt() {
    return updateDt;
  }

  public void setUpdateDt(Date updateDt) {
    this.updateDt = updateDt;
  }

  public String getKatr10() {
    return katr10;
  }

  public void setKatr10(String katr10) {
    this.katr10 = katr10;
  }

  public String getCreatedTsStr() {
    return createdTsStr;
  }

  public void setCreatedTsStr(String createdTsStr) {
    this.createdTsStr = createdTsStr;
  }

  public String getUpdatedTsStr() {
    return updatedTsStr;
  }

  public void setUpdatedTsStr(String updatedTsStr) {
    this.updatedTsStr = updatedTsStr;
  }

  @Override
  public boolean allKeysAssigned() {
    boolean blnAllKeys = false;
    blnAllKeys = !StringUtils.isEmpty(this.mandt) && !StringUtils.isEmpty(this.tcrFileNm) && !StringUtils.isEmpty(this.seqNo + "");

    return blnAllKeys;
  }

  @Override
  public String getRecordDescription() {
    return "US TCR UPDT QUEUE Entry";
  }

  @Override
  public void addKeyParameters(ModelAndView mv) {
  }

  @Override
  public void addKeyParameters(ModelMap map) {
  }

}
