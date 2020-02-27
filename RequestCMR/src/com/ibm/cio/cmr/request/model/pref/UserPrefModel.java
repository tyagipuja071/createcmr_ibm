/**
 * 
 */
package com.ibm.cio.cmr.request.model.pref;

import org.apache.commons.lang.StringUtils;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.cio.cmr.request.model.BaseModel;
import com.ibm.cio.cmr.request.ui.UIMgr;

/**
 * Model for the User Preference Page
 * 
 * @author Jeffrey Zamora
 * 
 */
public class UserPrefModel extends BaseModel {

  private static final long serialVersionUID = 1L;

  private String requesterId;

  private String requesterNm;

  private String dftIssuingCntry;

  private String dftStateProv;

  private String dftCounty;

  private String dftLandedCntry;

  private String dftReviewerId;

  private String dftReviewerNm;

  private String receiveMailInd;

  private String procCenterNm;

  private String userId;

  private String delegateId;

  private String delegateNm;

  private String managerName;

  private String defaultLineOfBusn;

  private String defaultRequestRsn;

  private String defaultReqType;

  private int defaultNoOfRecords;

  @Override
  public boolean allKeysAssigned() {
    return !StringUtils.isEmpty(this.requesterId);
  }

  @Override
  public String getRecordDescription() {
    return UIMgr.getText("record.userpref");
  }

  @Override
  public void addKeyParameters(ModelAndView mv) {
    mv.addObject("requesterId", this.requesterId);
  }

  @Override
  public void addKeyParameters(ModelMap map) {
    map.addAttribute("requesterId", this.requesterId);
  }

  public String getRequesterId() {
    return requesterId;
  }

  public void setRequesterId(String requesterId) {
    this.requesterId = requesterId;
  }

  public String getRequesterNm() {
    return requesterNm;
  }

  public void setRequesterNm(String requesterNm) {
    this.requesterNm = requesterNm;
  }

  public String getDftIssuingCntry() {
    return dftIssuingCntry;
  }

  public void setDftIssuingCntry(String dftIssuingCntry) {
    this.dftIssuingCntry = dftIssuingCntry;
  }

  public String getDftStateProv() {
    return dftStateProv;
  }

  public void setDftStateProv(String dftStateProv) {
    this.dftStateProv = dftStateProv;
  }

  public String getDftCounty() {
    return dftCounty;
  }

  public void setDftCounty(String dftCounty) {
    this.dftCounty = dftCounty;
  }

  public String getDftLandedCntry() {
    return dftLandedCntry;
  }

  public void setDftLandedCntry(String dftLandedCntry) {
    this.dftLandedCntry = dftLandedCntry;
  }

  public String getDftReviewerId() {
    return dftReviewerId;
  }

  public void setDftReviewerId(String dftReviewerId) {
    this.dftReviewerId = dftReviewerId;
  }

  public String getDftReviewerNm() {
    return dftReviewerNm;
  }

  public void setDftReviewerNm(String dftReviewerNm) {
    this.dftReviewerNm = dftReviewerNm;
  }

  public String getReceiveMailInd() {
    return receiveMailInd;
  }

  public void setReceiveMailInd(String receiveMailInd) {
    this.receiveMailInd = receiveMailInd;
  }

  public String getProcCenterNm() {
    return procCenterNm;
  }

  public void setProcCenterNm(String procCenterNm) {
    this.procCenterNm = procCenterNm;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public String getDelegateId() {
    return delegateId;
  }

  public void setDelegateId(String delegateId) {
    this.delegateId = delegateId;
  }

  public String getDelegateNm() {
    return delegateNm;
  }

  public void setDelegateNm(String delegateNm) {
    this.delegateNm = delegateNm;
  }

  public String getManagerName() {
    return managerName;
  }

  public void setManagerName(String managerName) {
    this.managerName = managerName;
  }

  public String getDefaultLineOfBusn() {
    return defaultLineOfBusn;
  }

  public void setDefaultLineOfBusn(String defaultLineOfBusn) {
    this.defaultLineOfBusn = defaultLineOfBusn;
  }

  public String getDefaultRequestRsn() {
    return defaultRequestRsn;
  }

  public void setDefaultRequestRsn(String defaultRequestRsn) {
    this.defaultRequestRsn = defaultRequestRsn;
  }

  public String getDefaultReqType() {
    return defaultReqType;
  }

  public void setDefaultReqType(String defaultReqType) {
    this.defaultReqType = defaultReqType;
  }

  public int getDefaultNoOfRecords() {
    return defaultNoOfRecords;
  }

  public void setDefaultNoOfRecords(int defaultNoOfRecords) {
    this.defaultNoOfRecords = defaultNoOfRecords;
  }

}
