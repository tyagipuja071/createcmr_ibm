package com.ibm.cmr.create.batch.model;

import java.util.Date;

import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.cio.cmr.request.model.BaseModel;
import com.ibm.cio.cmr.request.ui.UIMgr;

/**
 * @author Rangoli Saxena
 * 
 */
public class NotifyReqModel extends BaseModel {

  private static final long serialVersionUID = 1L;

  // NOTIFY_REQ entries
  private long notifyId;
  private long reqId;
  private String reqStatus;
  private Date changeTs;
  private String changedById;
  private String cmtNotify;
  private String cmtLogMsg;
  private String notifiedInd;

  @Override
  public boolean allKeysAssigned() {
    return this.reqId > 0 && this.notifyId > 0;
  }

  @Override
  public String getRecordDescription() {
    return UIMgr.getText("record.notify");
  }

  @Override
  public void addKeyParameters(ModelAndView mv) {
    mv.addObject("reqId", this.reqId);
    mv.addObject("notifId", this.notifyId);
  }

  @Override
  public void addKeyParameters(ModelMap map) {
    map.addAttribute("reqId", this.reqId);
    map.addAttribute("notifId", this.notifyId);
  }

  public long getNotifyId() {
    return notifyId;
  }

  public void setNotifyId(long notifyId) {
    this.notifyId = notifyId;
  }

  public long getReqId() {
    return reqId;
  }

  public void setReqId(long reqId) {
    this.reqId = reqId;
  }

  public String getReqStatus() {
    return reqStatus;
  }

  public void setReqStatus(String reqStatus) {
    this.reqStatus = reqStatus;
  }

  public Date getChangeTs() {
    return changeTs;
  }

  public void setChangeTs(Date changeTs) {
    this.changeTs = changeTs;
  }

  public String getChangedById() {
    return changedById;
  }

  public void setChangedById(String changedById) {
    this.changedById = changedById;
  }

  public String getCmtNotify() {
    return cmtNotify;
  }

  public void setCmtNotify(String cmtNotify) {
    this.cmtNotify = cmtNotify;
  }

  public String getCmtLogMsg() {
    return cmtLogMsg;
  }

  public void setCmtLogMsg(String cmtLogMsg) {
    this.cmtLogMsg = cmtLogMsg;
  }

  public String getNotifiedInd() {
    return notifiedInd;
  }

  public void setNotifiedInd(String notifiedInd) {
    this.notifiedInd = notifiedInd;
  }

}
