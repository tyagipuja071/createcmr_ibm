package com.ibm.cio.cmr.request.model.requestentry;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.model.BaseModel;

/**
 * @author Sonali Jain
 * 
 */
public class CmtModel extends BaseModel {

  private static final long serialVersionUID = 1L;

  private long reqId;
  private long cmtId;
  private String createById;
  private String createByNm;
  private Date createTs;
  private String createTsString;
  private Date updateTs;
  private String cmt;
  private String cmtLockedIn;

  SimpleDateFormat formatter = CmrConstants.DATE_TIME_FORMAT();

  @Override
  public boolean allKeysAssigned() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public String getRecordDescription() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void addKeyParameters(ModelAndView mv) {
    // TODO Auto-generated method stub

  }

  @Override
  public void addKeyParameters(ModelMap map) {
    // TODO Auto-generated method stub

  }

  public long getReqId() {
    return reqId;
  }

  public void setReqId(long reqId) {
    this.reqId = reqId;
  }

  public long getCmtId() {
    return cmtId;
  }

  public void setCmtId(long cmtId) {
    this.cmtId = cmtId;
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

  public Date getCreateTs() {
    return createTs;
  }

  public void setCreateTs(Date createTs) {
    this.createTs = createTs;
    this.createTsString = formatter.format(createTs);
  }

  public String getCreateTsString() {
    return createTsString;
  }

  public Date getUpdateTs() {
    return updateTs;
  }

  public void setUpdateTs(Date updateTs) {
    this.updateTs = updateTs;
  }

  public String getCmt() {
    return cmt;
  }

  public void setCmt(String cmt) {
    this.cmt = cmt;
  }

  public String getCmtLockedIn() {
    return cmtLockedIn;
  }

  public void setCmtLockedIn(String cmtLockedIn) {
    this.cmtLockedIn = cmtLockedIn;
  }

  public SimpleDateFormat getFormatter() {
    return formatter;
  }

  public void setFormatter(SimpleDateFormat formatter) {
    this.formatter = formatter;
  }

}
