/**
 * 
 */
package com.ibm.cio.cmr.request.model.code;

/**
 * @author Jeffrey Zamora
 * 
 */
public class LovValues {

  private int dispOrder;
  private String cd;
  private String txt;
  private String status;
  private boolean defaultInd;

  public int getDispOrder() {
    return dispOrder;
  }

  public void setDispOrder(int dispOrder) {
    this.dispOrder = dispOrder;
  }

  public String getCd() {
    return cd;
  }

  public void setCd(String cd) {
    this.cd = cd;
  }

  public String getTxt() {
    return txt;
  }

  public void setTxt(String txt) {
    this.txt = txt;
  }

  public boolean isDefaultInd() {
    return defaultInd;
  }

  public void setDefaultInd(boolean defaultInd) {
    this.defaultInd = defaultInd;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }
}
