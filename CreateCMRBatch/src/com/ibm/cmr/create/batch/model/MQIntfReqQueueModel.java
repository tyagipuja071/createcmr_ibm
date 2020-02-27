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
public class MQIntfReqQueueModel extends BaseModel {

  private static final long serialVersionUID = 1L;

  // MQ_INTF_REQ entries
  private long queryReqId;
  private long reqId;
  private String reqStatus;
  private String targetSys;
  private Date lastUpdtTs;
  private String lastUpdtBy;
  private String execpMessage;
  private String mqInd;

  @Override
  public boolean allKeysAssigned() {
    return this.reqId > 0 && this.queryReqId > 0;
  }

  @Override
  public String getRecordDescription() {
    return UIMgr.getText("record.mq");
  }

  @Override
  public void addKeyParameters(ModelAndView mv) {
    mv.addObject("reqId", this.reqId);
    mv.addObject("queryReqId", this.queryReqId);
  }

  @Override
  public void addKeyParameters(ModelMap map) {
    map.addAttribute("reqId", this.reqId);
    map.addAttribute("queryReqId", this.queryReqId);
  }

  public long getQueryReqId() {
    return queryReqId;
  }

  public void setQueryReqId(long queryReqId) {
    this.queryReqId = queryReqId;
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

  public Date getLastUpdtTs() {
    return lastUpdtTs;
  }

  public void setLastUpdtTs(Date lastUpdtTs) {
    this.lastUpdtTs = lastUpdtTs;
  }

  public String getLastUpdtBy() {
    return lastUpdtBy;
  }

  public void setLastUpdtBy(String lastUpdtBy) {
    this.lastUpdtBy = lastUpdtBy;
  }

  public String getExecpMessage() {
    return execpMessage;
  }

  public void setExecpMessage(String execpMessage) {
    this.execpMessage = execpMessage;
  }

  public String getMqInd() {
    return mqInd;
  }

  public void setMqInd(String mqInd) {
    this.mqInd = mqInd;
  }

  public String getTargetSys() {
    return targetSys;
  }

  public void setTargetSys(String targetSys) {
    this.targetSys = targetSys;
  }

}
