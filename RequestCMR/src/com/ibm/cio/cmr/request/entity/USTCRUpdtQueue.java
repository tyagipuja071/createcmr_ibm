package com.ibm.cio.cmr.request.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * The persistent class for the US_TCR_UPDT_QUEUE database table.
 * 
 */
@Entity
@Table(name = "US_TCR_UPDT_QUEUE", schema = "USINTERIM")
public class USTCRUpdtQueue extends BaseEntity<USTCRUpdtQueuePK> implements Serializable {
  private static final long serialVersionUID = 1L;

  @EmbeddedId
  private USTCRUpdtQueuePK id;

  @Override
  public USTCRUpdtQueuePK getId() {
    return id;
  }

  @Override
  public void setId(USTCRUpdtQueuePK id) {
    this.id = id;
  }

  @Column(name = "LINE_CONTENT")
  private String lineContent;

  @Column(name = "CMR_NO")
  private String cmrNo;

  @Column(name = "TAX_CUST_TYP_1")
  private String taxCustTyp1;

  @Column(name = "TAX_CUST_TYP_2")
  private String taxCustTyp2;

  @Column(name = "TAX_CUST_TYP_3")
  private String taxCustTyp3;

  @Column(name = "TAX_CLASS_1")
  private String taxClass1;

  @Column(name = "TAX_CLASS_2")
  private String taxClass2;

  @Column(name = "TAX_CLASS_3")
  private String taxClass3;

  @Column(name = "TAX_EXEMPT_STATUS_1")
  private String taxExemptStatus1;

  @Column(name = "TAX_EXEMPT_STATUS_2")
  private String taxExemptStatus2;

  @Column(name = "TAX_EXEMPT_STATUS_3")
  private String taxExemptStatus3;

  @Column(name = "PROC_STATUS")
  private String procStatus;

  @Column(name = "PROC_MSG")
  private String procMsg;

  @Column(name = "CREATED_BY")
  private String createBy;

  @Column(name = "CREATE_DT")
  @Temporal(TemporalType.TIMESTAMP)
  private Date createDt;

  @Column(name = "UPDATED_BY")
  private String updateBy;

  @Column(name = "UPDATE_DT")
  @Temporal(TemporalType.TIMESTAMP)
  private Date updateDt;

  @Column(name = "KATR10")
  private String katr10;

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

  public String getTaxCustTyp2() {
    return taxCustTyp2;
  }

  public void setTaxCustTyp2(String taxCustTyp2) {
    this.taxCustTyp2 = taxCustTyp2;
  }

  public String getTaxCustTyp3() {
    return taxCustTyp3;
  }

  public void setTaxCustTyp3(String taxCustTyp3) {
    this.taxCustTyp3 = taxCustTyp3;
  }

  public String getTaxClass1() {
    return taxClass1;
  }

  public void setTaxClass1(String taxClass1) {
    this.taxClass1 = taxClass1;
  }

  public String getTaxClass2() {
    return taxClass2;
  }

  public void setTaxClass2(String taxClass2) {
    this.taxClass2 = taxClass2;
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

  public String getCreateBy() {
    return createBy;
  }

  public void setCreateBy(String createBy) {
    this.createBy = createBy;
  }

  public Date getCreateDt() {
    return createDt;
  }

  public void setCreateDt(Date createDt) {
    this.createDt = createDt;
  }

  public String getUpdateBy() {
    return updateBy;
  }

  public void setUpdateBy(String updateBy) {
    this.updateBy = updateBy;
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

}