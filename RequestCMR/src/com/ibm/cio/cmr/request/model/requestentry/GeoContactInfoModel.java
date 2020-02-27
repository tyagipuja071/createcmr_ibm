package com.ibm.cio.cmr.request.model.requestentry;

import java.util.Date;

import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.cio.cmr.request.model.BaseModel;

/**
 * 
 * Additional Contact Info model LA
 * 
 * @author Neil Sherwin Espolong
 * */

public class GeoContactInfoModel extends BaseModel {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  private long reqId;
  private int contactInfoId;
  private String contactSeqNum;
  private String contactType;
  private String contactName;
  private String contactPhone;
  private String contactEmail;
  private Date createTs;
  private String createById;
  private Date updtTs;
  private String updtById;
  private String reqType;
  private String cmrIssuingCntry;
  private String currentEmail1;
  private String contactTreatment;
  private String contactFunc;

  private String removed;

  @Override
  public boolean allKeysAssigned() {
    return false;
  }

  @Override
  public String getRecordDescription() {
    return null;
  }

  @Override
  public void addKeyParameters(ModelAndView mv) {
  }

  @Override
  public void addKeyParameters(ModelMap map) {
  }

  public long getReqId() {
    return reqId;
  }

  public void setReqId(long reqId) {
    this.reqId = reqId;
  }

  public int getContactInfoId() {
    return contactInfoId;
  }

  public void setContactInfoId(int contactInfoId) {
    this.contactInfoId = contactInfoId;
  }

  public String getContactSeqNum() {
    return contactSeqNum;
  }

  public void setContactSeqNum(String contactSeqNum) {
    this.contactSeqNum = contactSeqNum;
  }

  public String getContactType() {
    return contactType;
  }

  public void setContactType(String contactType) {
    this.contactType = contactType;
  }

  public String getContactName() {
    return contactName;
  }

  public void setContactName(String contactName) {
    this.contactName = contactName;
  }

  public String getContactPhone() {
    return contactPhone;
  }

  public void setContactPhone(String contactPhone) {
    this.contactPhone = contactPhone;
  }

  public String getContactEmail() {
    return contactEmail;
  }

  public void setContactEmail(String contactEmail) {
    this.contactEmail = contactEmail;
  }

  public Date getCreateTs() {
    return createTs;
  }

  public void setCreateTs(Date createTs) {
    this.createTs = createTs;
  }

  public String getCreateById() {
    return createById;
  }

  public void setCreateById(String createById) {
    this.createById = createById;
  }

  public Date getUpdtTs() {
    return updtTs;
  }

  public void setUpdtTs(Date updtTs) {
    this.updtTs = updtTs;
  }

  public String getUpdtById() {
    return updtById;
  }

  public void setUpdtById(String updtById) {
    this.updtById = updtById;
  }

  public String getReqType() {
    return reqType;
  }

  public void setReqType(String reqType) {
    this.reqType = reqType;
  }

  public String getCmrIssuingCntry() {
    return cmrIssuingCntry;
  }

  public void setCmrIssuingCntry(String cmrIssuingCntry) {
    this.cmrIssuingCntry = cmrIssuingCntry;
  }

  public String getCurrentEmail1() {
    return currentEmail1;
  }

  public void setCurrentEmail1(String currentEmail1) {
    this.currentEmail1 = currentEmail1;
  }

  public String getContactTreatment() {
    return contactTreatment;
  }

  public void setContactTreatment(String contactTreatment) {
    this.contactTreatment = contactTreatment;
  }

  public String getContactFunc() {
    return contactFunc;
  }

  public void setContactFunc(String contactFunc) {
    this.contactFunc = contactFunc;
  }

  public String getRemoved() {
    return removed;
  }

  public void setRemoved(String removed) {
    this.removed = removed;
  }

}
