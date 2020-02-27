package com.ibm.cio.cmr.request.entity;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;

@Entity
public class DeleteReactivate extends BaseEntity<DeleteReactivatePK>{
  
  @EmbeddedId
  private DeleteReactivatePK id;
  
  @Override
  public DeleteReactivatePK getId() {
    return id;
  }

  @Override
  public void setId(DeleteReactivatePK id) {
    this.id = id;
  }

  @Column(name = "ERROR_TXT")
  private String errorTxt;

  @Column(name = "ROW_STATUS_CD")
  private String rowStatusCd;

  @Column(name = "CMR_NO")
  private String cmrNo;
  
  @Column(name = "NAME")
  private String name;
  
  @Column(name = "ORDER_BLOCK")
  private String orderBlock;
  
  @Column(name = "DELETED")
  private String deleted;

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

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getOrderBlock() {
    return orderBlock;
  }

  public void setOrderBlock(String orderBlock) {
    this.orderBlock = orderBlock;
  }

  public String getDeleted() {
    return deleted;
  }

  public void setDeleted(String deleted) {
    this.deleted = deleted;
  }

}
