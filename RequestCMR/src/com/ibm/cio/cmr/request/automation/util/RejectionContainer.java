package com.ibm.cio.cmr.request.automation.util;

/**
 * Holds details for rejections
 * 
 * @author PoojaTyagi
 *
 */
public class RejectionContainer {
  private String rejComment;
  private String rejCode;
  private String supplInfo1;
  private String supplInfo2;

  public String getRejComment() {
    return rejComment;
  }

  public void setRejComment(String rejComment) {
    this.rejComment = rejComment;
  }

  public String getRejCode() {
    return rejCode;
  }

  public void setRejCode(String rejCode) {
    this.rejCode = rejCode;
  }

  public String getSupplInfo1() {
    return supplInfo1;
  }

  public void setSupplInfo1(String supplInfo1) {
    this.supplInfo1 = supplInfo1;
  }

  public String getSupplInfo2() {
    return supplInfo2;
  }

  public void setSupplInfo2(String supplInfo2) {
    this.supplInfo2 = supplInfo2;
  }
}
