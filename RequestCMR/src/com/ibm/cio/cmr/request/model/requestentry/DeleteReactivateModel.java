/**
 * 
 */
package com.ibm.cio.cmr.request.model.requestentry;

/**
 * @author JeffZAMORA
 * 
 */
public class DeleteReactivateModel {

  private long reqId;
  private String cmrNo;
  private String name;
  private String orderBlock;
  private String deleted;

  public long getReqId() {
    return reqId;
  }

  public void setReqId(long reqId) {
    this.reqId = reqId;
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
