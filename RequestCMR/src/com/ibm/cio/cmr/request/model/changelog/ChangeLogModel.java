/**
 * 
 */
package com.ibm.cio.cmr.request.model.changelog;

import java.util.Date;

import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.cio.cmr.request.model.BaseModel;

/**
 * Model for CREQCMR ChangeLog
 * 
 * @author Eduard Bernardo
 */
public class ChangeLogModel extends BaseModel {

  private static final long serialVersionUID = 1L;
  private long requestId;
  private Date changeTs;
  private String changeTsStr;
  private String tablName;
  private String fieldName;
  private String addrTyp;
  private String addrSeq;
  private String action;
  private String requestStatus;
  private String oldValue;
  private String newValue;
  private String userId;

  private String requestIdStr;
  private String changeDateFrom;
  private String changeDateTo;
  private String cmrNo;

  public long getRequestId() {
    return requestId;
  }

  public void setRequestId(long requestId) {
    this.requestId = requestId;
  }

  public Date getChangeTs() {
    return changeTs;
  }

  public void setChangeTs(Date changeTs) {
    this.changeTs = changeTs;
  }

  public String getChangeTsStr() {
    return changeTsStr;
  }

  public void setChangeTsStr(String changeTsStr) {
    this.changeTsStr = changeTsStr;
  }

  public String getFieldName() {
    return fieldName;
  }

  public void setFieldName(String fieldName) {
    this.fieldName = fieldName;
  }

  public String getAddrSeq() {
    return addrSeq;
  }

  public void setAddrSeq(String addrSeq) {
    this.addrSeq = addrSeq;
  }

  @Override
  public String getAction() {
    return action;
  }

  @Override
  public void setAction(String action) {
    this.action = action;
  }

  public String getOldValue() {
    return oldValue;
  }

  public void setOldValue(String oldValue) {
    this.oldValue = oldValue;
  }

  public String getNewValue() {
    return newValue;
  }

  public void setNewValue(String newValue) {
    this.newValue = newValue;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

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

  public String getChangeDateFrom() {
    return changeDateFrom;
  }

  public void setChangeDateFrom(String changeDateFrom) {
    this.changeDateFrom = changeDateFrom;
  }

  public String getChangeDateTo() {
    return changeDateTo;
  }

  public void setChangeDateTo(String changeDateTo) {
    this.changeDateTo = changeDateTo;
  }

  public String getTablName() {
    return tablName;
  }

  public void setTablName(String tablName) {
    this.tablName = tablName;
  }

  public String getAddrTyp() {
    return addrTyp;
  }

  public void setAddrTyp(String addrTyp) {
    this.addrTyp = addrTyp;
  }

  public String getRequestStatus() {
    return requestStatus;
  }

  public void setRequestStatus(String requestStatus) {
    this.requestStatus = requestStatus;
  }

  public String getRequestIdStr() {
    return requestIdStr;
  }

  public void setRequestIdStr(String requestIdStr) {
    this.requestIdStr = requestIdStr;
  }

  public String getCmrNo() {
    return cmrNo;
  }

  public void setCmrNo(String cmrNo) {
    this.cmrNo = cmrNo;
  }

}
