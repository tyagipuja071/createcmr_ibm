package com.ibm.cio.cmr.request.entity.listeners;

/**
 * @author
 * 
 */
public class GenChangeLogParentContainer {

  private String kunnr;
  private String tabKey1; // comp_no
  private String tabKey2; // ent_no
  private String mandt;
  private String changeBy;
  private String reqType;
  private long reqId;

  public String getKunnr() {
    return kunnr;
  }

  public void setKunnr(String kunnr) {
    this.kunnr = kunnr;
  }

  public String getTabKey1() {
    return tabKey1;
  }

  public void setTabKey1(String tabKey1) {
    this.tabKey1 = tabKey1;
  }

  public String getTabKey2() {
    return tabKey2;
  }

  public void setTabKey2(String tabKey2) {
    this.tabKey2 = tabKey2;
  }

  public String getMandt() {
    return mandt;
  }

  public void setMandt(String mandt) {
    this.mandt = mandt;
  }

  public String getChangeBy() {
    return changeBy;
  }

  public void setChangeBy(String changeBy) {
    this.changeBy = changeBy;
  }

  public String getReqType() {
    return reqType;
  }

  public void setReqType(String reqType) {
    this.reqType = reqType;
  }

  public long getReqId() {
    return reqId;
  }

  public void setReqId(long reqId) {
    this.reqId = reqId;
  }

}
