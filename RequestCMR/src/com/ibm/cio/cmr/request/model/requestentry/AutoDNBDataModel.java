package com.ibm.cio.cmr.request.model.requestentry;

import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.cio.cmr.request.model.BaseModel;

public class AutoDNBDataModel extends BaseModel {

  private static final long serialVersionUID = 1L;

  // dnbautocheck
  private int itemNo;
  private String autoDnbDunsNo;
  private String autoDnbName;
  private String autoDnbMatchGrade;
  private String autoDnbImportedIndc;
  private String fullAddress;
  private String ibmIsic;

  public String getAutoDnbImportedIndc() {
    return autoDnbImportedIndc;
  }

  public void setAutoDnbImportedIndc(String autoDnbImportedIndc) {
    this.autoDnbImportedIndc = autoDnbImportedIndc;
  }

  public int getItemNo() {
    return itemNo;
  }

  public void setItemNo(int itemNo) {
    this.itemNo = itemNo;
  }

  public String getAutoDnbDunsNo() {
    return autoDnbDunsNo;
  }

  public void setAutoDnbDunsNo(String autoDnbDunsNo) {
    this.autoDnbDunsNo = autoDnbDunsNo;
  }

  public String getAutoDnbName() {
    return autoDnbName;
  }

  public void setAutoDnbName(String autoDnbName) {
    this.autoDnbName = autoDnbName;
  }

  public String getAutoDnbMatchGrade() {
    return autoDnbMatchGrade;
  }

  public void setAutoDnbMatchGrade(String autoDnbMatchGrade) {
    this.autoDnbMatchGrade = autoDnbMatchGrade;
  }

  public String getFullAddress() {
    return fullAddress;
  }

  public void setFullAddress(String fullAddress) {
    this.fullAddress = fullAddress;
  }

  public String getIbmIsic() {
    return ibmIsic;
  }

  public void setIbmIsic(String ibmIsic) {
    this.ibmIsic = ibmIsic;
  }

  @Override
  public boolean allKeysAssigned() {
    return this.itemNo > 0;
  }

  @Override
  public String getRecordDescription() {
    return "DNB Matches";
  }

  @Override
  public void addKeyParameters(ModelAndView mv) {
  }

  @Override
  public void addKeyParameters(ModelMap map) {
  }
}
