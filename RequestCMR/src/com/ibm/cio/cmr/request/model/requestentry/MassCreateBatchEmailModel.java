package com.ibm.cio.cmr.request.model.requestentry;

public class MassCreateBatchEmailModel {

  private String cmrNo;
  private String custName;
  private String sapNo;
  private String errorMsg;
  private int rowNo;

  public String getCmrNo() {
    return cmrNo;
  }

  public void setCmrNo(String cmrNo) {
    this.cmrNo = cmrNo;
  }

  public String getCustName() {
    return custName;
  }

  public void setCustName(String custName) {
    this.custName = custName;
  }

  public String getErrorMsg() {
    return errorMsg;
  }

  public void setErrorMsg(String errorMsg) {
    this.errorMsg = errorMsg;
  }

  public int getRowNo() {
    return rowNo;
  }

  public void setRowNo(int rowNo) {
    this.rowNo = rowNo;
  }

  public String getSapNo() {
    return sapNo;
  }

  public void setSapNo(String sapNo) {
    this.sapNo = sapNo;
  }

}
