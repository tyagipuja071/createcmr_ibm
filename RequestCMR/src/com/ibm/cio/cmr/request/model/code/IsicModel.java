/**
 * 
 */
package com.ibm.cio.cmr.request.model.code;

/**
 * @author JeffZAMORA
 *
 */
public class IsicModel {

  private int reftUnsicKey;

  private int reftIndclKey;

  private String reftUnsicCd;

  private String reftUnsicDesc;

  private String reftUnsicAbbrevDesc;

  private String comments;

  private String geoCd;

  public int getReftUnsicKey() {
    return reftUnsicKey;
  }

  public void setReftUnsicKey(int reftUnsicKey) {
    this.reftUnsicKey = reftUnsicKey;
  }

  public int getReftIndclKey() {
    return reftIndclKey;
  }

  public void setReftIndclKey(int reftIndclKey) {
    this.reftIndclKey = reftIndclKey;
  }

  public String getReftUnsicCd() {
    return reftUnsicCd;
  }

  public void setReftUnsicCd(String reftUnsicCd) {
    this.reftUnsicCd = reftUnsicCd;
  }

  public String getReftUnsicDesc() {
    return reftUnsicDesc;
  }

  public void setReftUnsicDesc(String reftUnsicDesc) {
    this.reftUnsicDesc = reftUnsicDesc;
  }

  public String getReftUnsicAbbrevDesc() {
    return reftUnsicAbbrevDesc;
  }

  public void setReftUnsicAbbrevDesc(String reftUnsicAbbrevDesc) {
    this.reftUnsicAbbrevDesc = reftUnsicAbbrevDesc;
  }

  public String getComments() {
    return comments;
  }

  public void setComments(String comments) {
    this.comments = comments;
  }

  public String getGeoCd() {
    return geoCd;
  }

  public void setGeoCd(String geoCd) {
    this.geoCd = geoCd;
  }

}
