/**
 * 
 */
package com.ibm.cio.cmr.request.util.search;

import com.ibm.cio.cmr.request.util.LegacyMQRecord;

/**
 * @author JeffZAMORA
 *
 */
public class MQSearchResult<T extends LegacyMQRecord> {

  public static enum System {
    SOF, WTAAS
  }

  private boolean success;
  private String msg;

  private String country;
  private String cmrNo;
  private System system;
  private T record;

  public String getCountry() {
    return country;
  }

  public void setCountry(String country) {
    this.country = country;
  }

  public String getCmrNo() {
    return cmrNo;
  }

  public void setCmrNo(String cmrNo) {
    this.cmrNo = cmrNo;
  }

  public T getRecord() {
    return record;
  }

  public void setRecord(T record) {
    this.record = record;
  }

  public System getSystem() {
    return system;
  }

  public void setSystem(System system) {
    this.system = system;
  }

  public boolean isSuccess() {
    return success;
  }

  public void setSuccess(boolean success) {
    this.success = success;
  }

  public String getMsg() {
    return msg;
  }

  public void setMsg(String msg) {
    this.msg = msg;
  }
}
