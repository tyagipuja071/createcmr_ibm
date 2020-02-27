package com.ibm.cio.cmr.request.automation.util;

/**
 * Holds details about duplicate CMRs
 * 
 * @author JeffZAMORA
 *
 */
public class DuplicateContainer {
  private String cmrNo;
  private String name1;
  private String name2;
  private String vat;

  public String getCmrNo() {
    return cmrNo;
  }

  public void setCmrNo(String cmrNo) {
    this.cmrNo = cmrNo;
  }

  public String getName1() {
    return name1;
  }

  public void setName1(String name1) {
    this.name1 = name1;
  }

  public String getName2() {
    return name2;
  }

  public void setName2(String name2) {
    this.name2 = name2;
  }

  public String getVat() {
    return vat;
  }

  public void setVat(String vat) {
    this.vat = vat;
  }
}
