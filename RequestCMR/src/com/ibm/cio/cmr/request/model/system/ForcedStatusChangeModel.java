/**
 * 
 */
package com.ibm.cio.cmr.request.model.system;

import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.cio.cmr.request.model.BaseModel;
import com.ibm.cio.cmr.request.ui.UIMgr;

/**
 * @author Jeffrey Zamora
 * 
 */
public class ForcedStatusChangeModel extends BaseModel {

  private static final long serialVersionUID = 1L;

  private String searchReqId;
  private long reqId;
  private String reqStatus;
  private String lockInd;
  private String newReqStatus;
  private String newLockedInd;
  private String newLockedByNm;
  private String newLockedById;
  private String cmt;

  private String processedFlag;
  private String newProcessedFlag;

  @Override
  public boolean allKeysAssigned() {
    return this.reqId > 0;
  }

  @Override
  public String getRecordDescription() {
    return UIMgr.getText("record.request");
  }

  @Override
  public void addKeyParameters(ModelAndView mv) {
    mv.addObject("reqId", this.reqId);
  }

  @Override
  public void addKeyParameters(ModelMap map) {
    map.addAttribute("reqId", this.reqId);
  }

  public long getReqId() {
    return reqId;
  }

  public void setReqId(long reqId) {
    this.reqId = reqId;
  }

  public String getNewReqStatus() {
    return newReqStatus;
  }

  public void setNewReqStatus(String newReqStatus) {
    this.newReqStatus = newReqStatus;
  }

  public String getNewLockedByNm() {
    return newLockedByNm;
  }

  public void setNewLockedByNm(String newLockedByNm) {
    this.newLockedByNm = newLockedByNm;
  }

  public String getNewLockedById() {
    return newLockedById;
  }

  public void setNewLockedById(String newLockedById) {
    this.newLockedById = newLockedById;
  }

  public String getReqStatus() {
    return reqStatus;
  }

  public void setReqStatus(String reqStatus) {
    this.reqStatus = reqStatus;
  }

  public String getNewLockedInd() {
    return newLockedInd;
  }

  public void setNewLockedInd(String newLockedInd) {
    this.newLockedInd = newLockedInd;
  }

  public String getLockInd() {
    return lockInd;
  }

  public void setLockInd(String lockInd) {
    this.lockInd = lockInd;
  }

  public String getCmt() {
    return cmt;
  }

  public void setCmt(String cmt) {
    this.cmt = cmt;
  }

  public String getSearchReqId() {
    return searchReqId;
  }

  public void setSearchReqId(String searchReqId) {
    this.searchReqId = searchReqId;
  }

  public String getProcessedFlag() {
    return processedFlag;
  }

  public void setProcessedFlag(String processedFlag) {
    this.processedFlag = processedFlag;
  }

  public String getNewProcessedFlag() {
    return newProcessedFlag;
  }

  public void setNewProcessedFlag(String newProcessedFlag) {
    this.newProcessedFlag = newProcessedFlag;
  }

}
