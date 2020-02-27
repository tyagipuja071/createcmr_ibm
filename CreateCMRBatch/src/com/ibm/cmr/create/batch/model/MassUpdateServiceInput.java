package com.ibm.cmr.create.batch.model;

/**
 * @author Sonali Jain
 * 
 */
public class MassUpdateServiceInput {

  private String inputMandt;
  private long inputReqId;
  private String inputReqType;
  private String inputUserId;
  private String cmrIssuingCntry;

  public String getInputMandt() {
    return inputMandt;
  }

  public void setInputMandt(String inputMandt) {
    this.inputMandt = inputMandt;
  }

  public long getInputReqId() {
    return inputReqId;
  }

  public void setInputReqId(long inputReqId) {
    this.inputReqId = inputReqId;
  }

  public String getInputReqType() {
    return inputReqType;
  }

  public void setInputReqType(String inputReqType) {
    this.inputReqType = inputReqType;
  }

  public String getInputUserId() {
    return inputUserId;
  }

  public void setInputUserId(String inputUserId) {
    this.inputUserId = inputUserId;
  }

  public String getCmrIssuingCntry() {
    return cmrIssuingCntry;
  }

  public void setCmrIssuingCntry(String cmrIssuingCntry) {
    this.cmrIssuingCntry = cmrIssuingCntry;
  }
}
