package com.ibm.cio.cmr.request.model.window;

public class CMRListModel {

  private long parReqId;

  private int seqNo;

  private int iterationId;

  private String errorTxt;

  private String rowStatusCd;

  private String cmrNo;

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
}
