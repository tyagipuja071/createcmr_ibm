/**
 * 
 */
package com.ibm.cio.cmr.request.model.requestentry;

import org.apache.commons.lang.StringUtils;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.cio.cmr.request.model.BaseModel;
import com.ibm.cio.cmr.request.ui.UIMgr;

/**
 * @author Jeffrey Zamora
 * 
 */
public class NotifyListModel extends BaseModel {

  private static final long serialVersionUID = 1L;

  private long reqId;

  private String notifId;

  private String notifNm;

  private String noEmail;

  private String removable;

  @Override
  public boolean allKeysAssigned() {
    return this.reqId > 0 && !StringUtils.isEmpty(this.notifId);
  }

  @Override
  public String getRecordDescription() {
    return UIMgr.getText("record.notify");
  }

  @Override
  public void addKeyParameters(ModelAndView mv) {
    mv.addObject("reqId", this.reqId);
    mv.addObject("notifId", this.notifId);
  }

  @Override
  public void addKeyParameters(ModelMap map) {
    map.addAttribute("reqId", this.reqId);
    map.addAttribute("notifId", this.notifId);
  }

  public long getReqId() {
    return reqId;
  }

  public void setReqId(long reqId) {
    this.reqId = reqId;
  }

  public String getNotifId() {
    return notifId;
  }

  public void setNotifId(String notifId) {
    this.notifId = notifId;
  }

  public String getNotifNm() {
    return notifNm;
  }

  public void setNotifNm(String notifNm) {
    this.notifNm = notifNm;
  }

  public String getNoEmail() {
    return noEmail;
  }

  public void setNoEmail(String noEmail) {
    this.noEmail = noEmail;
  }

  public String getRemovable() {
    return removable;
  }

  public void setRemovable(String removable) {
    this.removable = removable;
  }

}
