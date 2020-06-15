package com.ibm.cio.cmr.request.model.requestentry;

import java.sql.Timestamp;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.cio.cmr.request.model.BaseModel;
import com.ibm.cio.cmr.request.ui.UIMgr;

/**
 * Model for User's Delegates
 * 
 * @author Rangoli
 * 
 */
public class AttachmentModel extends BaseModel {

  private static final long serialVersionUID = 1L;

  private long reqId;
  private String imgContent;
  private String attachMode;
  private String docLink;
  private String docContent;
  private String docAttachById;
  private String docAttachByNm;
  private Date attachTs;
  private String attachTsStr;
  private String cmt;

  @Override
  public boolean allKeysAssigned() {
    return !StringUtils.isEmpty(this.docLink) && !StringUtils.isEmpty(this.docLink);
  }

  @Override
  public String getRecordDescription() {
    return UIMgr.getText("record.attachment");
  }

  @Override
  public void addKeyParameters(ModelAndView mv) {
    mv.addObject("reqId", this.reqId);
    mv.addObject("docLink", this.docLink);
  }

  @Override
  public void addKeyParameters(ModelMap map) {
    map.addAttribute("reqId", this.reqId);
    map.addAttribute("docLink", this.docLink);

  }

  public long getReqId() {
    return reqId;
  }

  public void setReqId(long reqId) {
    this.reqId = reqId;
  }

  public String getDocLink() {
    return docLink;
  }

  public void setDocLink(String docLink) {
    this.docLink = docLink;
  }

  public String getDocContent() {
    return docContent;
  }

  public void setDocContent(String docContent) {
    this.docContent = docContent;
  }

  public String getDocAttachById() {
    return docAttachById;
  }

  public void setDocAttachById(String docAttachById) {
    this.docAttachById = docAttachById;
  }

  public String getDocAttachByNm() {
    return docAttachByNm;
  }

  public void setDocAttachByNm(String docAttachByNm) {
    this.docAttachByNm = docAttachByNm;
  }

  public void setAttachTs(Timestamp attachTs) {
    this.attachTs = attachTs;
  }

  public String getAttachTsStr() {
    return attachTsStr;
  }

  public void setAttachTsStr(String attachTsStr) {
    this.attachTsStr = attachTsStr;
  }

  public String getCmt() {
    return cmt;
  }

  public void setCmt(String cmt) {
    this.cmt = cmt;
  }

  public String getImgContent() {
    return imgContent;
  }

  public void setImgContent(String imgContent) {
    this.imgContent = imgContent;
  }

  public String getAttachMode() {
    return attachMode;
  }

  public void setAttachMode(String attachMode) {
    this.attachMode = attachMode;
  }

  public Date getAttachTs() {
    return attachTs;
  }

  public void setAttachTs(Date attachTs) {
    this.attachTs = attachTs;
  }

}
