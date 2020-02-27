/**
 * 
 */
package com.ibm.cio.cmr.request.model.window;

import javax.persistence.Transient;

/**
 * @author Eduard Bernardo
 * 
 */
public class MassDataSummaryModel implements Comparable<MassDataSummaryModel> {

  private String cmrNo;
  private int iterationId;
  private int seqNo;
  private String status;
  private String errorTxt;
  private String dplChkStatus;
  private String dplChkTS;

  @Transient
  private String name;

  @Transient
  private String orderBlock;

  @Transient
  private String deleted;

  public String getCmrNo() {
    return cmrNo;
  }

  public void setCmrNo(String cmrNo) {
    this.cmrNo = cmrNo;
  }

  public int getIterationId() {
    return iterationId;
  }

  public void setIterationId(int iterationId) {
    this.iterationId = iterationId;
  }

  public int getSeqNo() {
    return seqNo;
  }

  public void setSeqNo(int seqNo) {
    this.seqNo = seqNo;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  @Override
  public int compareTo(MassDataSummaryModel col) {
    if (this.iterationId > col.iterationId) {
      return -1;
    } else if (this.iterationId < col.iterationId) {
      return 1;
    } else if (this.seqNo < col.seqNo) {
      return -1;
    } else if (this.seqNo > col.seqNo) {
      return 1;
    }
    return 0;
  }

  public String getErrorTxt() {
    return errorTxt;
  }

  public void setErrorTxt(String errorTxt) {
    this.errorTxt = errorTxt;
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

  public String getDplChkStatus() {
    return dplChkStatus;
  }

  public void setDplChkStatus(String dplChkStatus) {
    this.dplChkStatus = dplChkStatus;
  }

  public String getDplChkTS() {
    return dplChkTS;
  }

  public void setDplChkTS(String dplChkTS) {
    this.dplChkTS = dplChkTS;
  }

}
