package com.ibm.cio.cmr.request.model;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.ui.UIMgr;

/**
 * @author Rama mohan
 * 
 */
public class WorkflowHistoryModel extends BaseModel {

  private static final long serialVersionUID = 1L;

  private long wfId;

  private long reqId;

  private String reqStatus;

  private String subOperStatus;

  private int subOperSeq;

  private String createById;

  private String createByNm;

  private Date createTs;

  private String createTsString;

  private long createTsMillis;

  private Date completeTs;

  private String sentToId;

  private String sentToNm;

  private String cmt;

  private String rejReason;

  private String overallStatus;

  private String reqStatusAct;

  // used for main request track
  private String mainReqType;
  private String mainCustName;
  private String mainExpedite;
  SimpleDateFormat formatter = CmrConstants.DATE_TIME_FORMAT();

  public long getWfId() {
    return wfId;
  }

  public void setWfId(long wfId) {
    this.wfId = wfId;
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

  public String getSubOperStatus() {
    return subOperStatus;
  }

  public void setSubOperStatus(String subOperStatus) {
    this.subOperStatus = subOperStatus;
  }

  public int getSubOperSeq() {
    return subOperSeq;
  }

  public void setSubOperSeq(int subOperSeq) {
    this.subOperSeq = subOperSeq;
  }

  public String getCreateById() {
    return createById;
  }

  public void setCreateById(String createById) {
    this.createById = createById;
  }

  public String getCreateByNm() {
    return createByNm;
  }

  public void setCreateByNm(String createByNm) {
    this.createByNm = createByNm;
  }

  public void setCreateTs(Date createTs) {
    this.createTs = createTs;
    this.createTsString = formatter.format(createTs);
    this.createTsMillis = createTs.getTime();
  }

  public Date getCompleteTs() {
    return completeTs;
  }

  public void setCompleteTs(Date completeTs) {
    this.completeTs = completeTs;
  }

  public String getSentToId() {
    return sentToId;
  }

  public void setSentToId(String sentToId) {
    this.sentToId = sentToId;
  }

  public String getSentToNm() {
    return sentToNm;
  }

  public void setSentToNm(String sentToNm) {
    this.sentToNm = sentToNm;
  }

  public String getCmt() {
    return cmt;
  }

  public void setCmt(String cmt) {
    this.cmt = cmt;
  }

  public String getRejReason() {
    return rejReason;
  }

  public void setRejReason(String rejReason) {
    this.rejReason = rejReason;
  }

  @Override
  public String getRecordDescription() {
    return UIMgr.getText("record.wfhistory");
  }

  @Override
  public void addKeyParameters(ModelAndView mv) {
    mv.addObject("wfId", this.wfId);
  }

  @Override
  public boolean allKeysAssigned() {
    return this.wfId > 0 && this.reqId > 0;

  }

  @Override
  public void addKeyParameters(ModelMap model) {
    model.addAttribute("adrnr", this.wfId);
    model.addAttribute("mandt", this.reqId);
  }

  public String getCreateTsString() {
    return createTsString;
  }

  public void setCreateTsString(String createTsString) {
    this.createTsString = createTsString;
  }

  public long getCreateTsMillis() {
    return createTsMillis;
  }

  public void setCreateTsMillis(long createTsMillis) {
    this.createTsMillis = createTsMillis;
  }

  public String getOverallStatus() {
    return overallStatus;
  }

  public void setOverallStatus(String overallStatus) {
    this.overallStatus = overallStatus;
  }

  public String getMainReqType() {
    return mainReqType;
  }

  public void setMainReqType(String mainReqType) {
    this.mainReqType = mainReqType;
  }

  public String getMainCustName() {
    return mainCustName;
  }

  public void setMainCustName(String mainCustName) {
    this.mainCustName = mainCustName;
  }

  public String getMainExpedite() {
    return mainExpedite;
  }

  public void setMainExpedite(String mainExpedite) {
    this.mainExpedite = mainExpedite;
  }

  public String getReqStatusAct() {
    return reqStatusAct;
  }

  public void setReqStatusAct(String reqStatusAct) {
    this.reqStatusAct = reqStatusAct;
  }

  public Date getCreateTs() {
    return createTs;
  }

}
