/**
 * 
 */
package com.ibm.cio.cmr.request.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * @author Bin
 * 
 */
@Entity
@Table(name = "SALES_PAYMENT", schema = "JPINTERIM")
public class SalesPayment {

  @EmbeddedId
  private SalesPaymentPK id;

  @Column(name = "CLOSE_DAY_1")
  private String closeDay1;
  @Column(name = "PAY_CYCLE_CD_1")
  private String payCycleCd1;
  @Column(name = "PAY_DAY_1")
  private String payDay1;
  @Column(name = "CLOSE_DAY_2")
  private String closeDay2;
  @Column(name = "PAY_CYCLE_CD_2")
  private String payCycleCd2;
  @Column(name = "PAY_DAY_2")
  private String payDay2;
  @Column(name = "CLOSE_DAY_3")
  private String closeDay3;
  @Column(name = "PAY_CYCLE_CD_3")
  private String payCycleCd3;
  @Column(name = "PAY_DAY_3")
  private String payDay3;
  @Column(name = "CLOSE_DAY_4")
  private String closeDay4;
  @Column(name = "PAYMENT_CYCLE_CD_4")
  private String payCycleCd4;
  @Column(name = "PAY_DAY_4")
  private String payDay4;
  @Column(name = "CLOSE_DAY_5")
  private String closeDay5;
  @Column(name = "PAYMENT_CYCLE_CD_5")
  private String payCycleCd5;
  @Column(name = "PAY_DAY_5")
  private String payDay5;
  @Column(name = "CLOSE_DAY_6")
  private String closeDay6;
  @Column(name = "PAYMENT_CYCLE_CD_6")
  private String payCycleCd6;
  @Column(name = "PAY_DAY_6")
  private String payDay6;
  @Column(name = "CLOSE_DAY_7")
  private String closeDay7;
  @Column(name = "PAYMENT_CYCLE_CD_7")
  private String payCycleCd7;
  @Column(name = "PAY_DAY_7")
  private String payDay7;
  @Column(name = "CLOSE_DAY_8")
  private String closeDay8;
  @Column(name = "PAYMENT_CYCLE_CD_8")
  private String payCycleCd8;
  @Column(name = "PAY_DAY_8")
  private String payDay8;
  @Column(name = "CONTRACT_SIGN_DT")
  private String contractSignDt;
  @Column(name = "SALES_TEAM_NO")
  private String salesTeamNo;
  @Column(name = "SALES_TEAM_UPDT_DT")
  private Date salesTeamUpdtDt;
  @Column(name = "CREATE_BY")
  private String createBy;
  @Column(name = "CREATE_TS")
  private Date createTs;
  @Column(name = "LAST_UPDT_BY")
  private String lastUpdtBy;
  @Column(name = "LAST_UPDT_TS")
  private Date lastUpdtTs;

  public SalesPaymentPK getId() {
    return id;
  }

  public void setId(SalesPaymentPK id) {
    this.id = id;
  }

  public String getCloseDay1() {
    return closeDay1;
  }

  public void setCloseDay1(String closeDay1) {
    this.closeDay1 = closeDay1;
  }

  public String getPayCycleCd1() {
    return payCycleCd1;
  }

  public void setPayCycleCd1(String payCycleCd1) {
    this.payCycleCd1 = payCycleCd1;
  }

  public String getPayDay1() {
    return payDay1;
  }

  public void setPayDay1(String payDay1) {
    this.payDay1 = payDay1;
  }

  public String getCloseDay2() {
    return closeDay2;
  }

  public void setCloseDay2(String closeDay2) {
    this.closeDay2 = closeDay2;
  }

  public String getPayCycleCd2() {
    return payCycleCd2;
  }

  public void setPayCycleCd2(String payCycleCd2) {
    this.payCycleCd2 = payCycleCd2;
  }

  public String getPayDay2() {
    return payDay2;
  }

  public void setPayDay2(String payDay2) {
    this.payDay2 = payDay2;
  }

  public String getCloseDay3() {
    return closeDay3;
  }

  public void setCloseDay3(String closeDay3) {
    this.closeDay3 = closeDay3;
  }

  public String getPayCycleCd3() {
    return payCycleCd3;
  }

  public void setPayCycleCd3(String payCycleCd3) {
    this.payCycleCd3 = payCycleCd3;
  }

  public String getPayDay3() {
    return payDay3;
  }

  public void setPayDay3(String payDay3) {
    this.payDay3 = payDay3;
  }

  public String getCloseDay4() {
    return closeDay4;
  }

  public void setCloseDay4(String closeDay4) {
    this.closeDay4 = closeDay4;
  }

  public String getPayCycleCd4() {
    return payCycleCd4;
  }

  public void setPayCycleCd4(String payCycleCd4) {
    this.payCycleCd4 = payCycleCd4;
  }

  public String getPayDay4() {
    return payDay4;
  }

  public void setPayDay4(String payDay4) {
    this.payDay4 = payDay4;
  }

  public String getCloseDay5() {
    return closeDay5;
  }

  public void setCloseDay5(String closeDay5) {
    this.closeDay5 = closeDay5;
  }

  public String getPayCycleCd5() {
    return payCycleCd5;
  }

  public void setPayCycleCd5(String payCycleCd5) {
    this.payCycleCd5 = payCycleCd5;
  }

  public String getPayDay5() {
    return payDay5;
  }

  public void setPayDay5(String payDay5) {
    this.payDay5 = payDay5;
  }

  public String getCloseDay6() {
    return closeDay6;
  }

  public void setCloseDay6(String closeDay6) {
    this.closeDay6 = closeDay6;
  }

  public String getPayCycleCd6() {
    return payCycleCd6;
  }

  public void setPayCycleCd6(String payCycleCd6) {
    this.payCycleCd6 = payCycleCd6;
  }

  public String getPayDay6() {
    return payDay6;
  }

  public void setPayDay6(String payDay6) {
    this.payDay6 = payDay6;
  }

  public String getCloseDay7() {
    return closeDay7;
  }

  public void setCloseDay7(String closeDay7) {
    this.closeDay7 = closeDay7;
  }

  public String getPayCycleCd7() {
    return payCycleCd7;
  }

  public void setPayCycleCd7(String payCycleCd7) {
    this.payCycleCd7 = payCycleCd7;
  }

  public String getPayDay7() {
    return payDay7;
  }

  public void setPayDay7(String payDay7) {
    this.payDay7 = payDay7;
  }

  public String getCloseDay8() {
    return closeDay8;
  }

  public void setCloseDay8(String closeDay8) {
    this.closeDay8 = closeDay8;
  }

  public String getPayCycleCd8() {
    return payCycleCd8;
  }

  public void setPayCycleCd8(String payCycleCd8) {
    this.payCycleCd8 = payCycleCd8;
  }

  public String getPayDay8() {
    return payDay8;
  }

  public void setPayDay8(String payDay8) {
    this.payDay8 = payDay8;
  }

  public String getContractSignDt() {
    return contractSignDt;
  }

  public void setContractSignDt(String contractSignDt) {
    this.contractSignDt = contractSignDt;
  }

  public String getSalesTeamNo() {
    return salesTeamNo;
  }

  public void setSalesTeamNo(String salesTeamNo) {
    this.salesTeamNo = salesTeamNo;
  }

  public Date getSalesTeamUpdtDt() {
    return salesTeamUpdtDt;
  }

  public void setSalesTeamUpdtDt(Date salesTeamUpdtDt) {
    this.salesTeamUpdtDt = salesTeamUpdtDt;
  }

  public String getCreateBy() {
    return createBy;
  }

  public void setCreateBy(String createBy) {
    this.createBy = createBy;
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

}
