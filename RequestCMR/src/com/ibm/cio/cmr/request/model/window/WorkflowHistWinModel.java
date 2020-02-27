/**
 * 
 */
package com.ibm.cio.cmr.request.model.window;

/**
 * @author Jeffrey Zamora
 * 
 */
public class WorkflowHistWinModel {

  private long reqId;
  private String expedite;
  private String requestType;
  private String customerName;

  public long getReqId() {
    return reqId;
  }

  public void setReqId(long reqId) {
    this.reqId = reqId;
  }

  public String getExpedite() {
    return expedite;
  }

  public void setExpedite(String expedite) {
    this.expedite = expedite;
  }

  public String getRequestType() {
    return requestType;
  }

  public void setRequestType(String requestType) {
    this.requestType = requestType;
  }

  public String getCustomerName() {
    return customerName;
  }

  public void setCustomerName(String customerName) {
    this.customerName = customerName;
  }

}
