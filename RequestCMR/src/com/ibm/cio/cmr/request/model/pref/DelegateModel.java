/**
 * 
 */
package com.ibm.cio.cmr.request.model.pref;

import java.sql.Timestamp;

import org.apache.commons.lang.StringUtils;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.cio.cmr.request.model.BaseModel;
import com.ibm.cio.cmr.request.ui.UIMgr;

/**
 * Model for User's Delegates
 * 
 * @author Jeffrey Zamora
 * 
 */
public class DelegateModel extends BaseModel {

  private static final long serialVersionUID = 1L;

  private String userId;

  private String delegateId;

  private String delegateNm;

  private Timestamp createTs;

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

  public Timestamp getCreateTs() {
    return createTs;
  }

  public void setCreateTs(Timestamp createTs) {
    this.createTs = createTs;
  }

  @Override
  public boolean allKeysAssigned() {
    return !StringUtils.isEmpty(this.delegateId) && !StringUtils.isEmpty(this.userId);
  }

  @Override
  public String getRecordDescription() {
    return UIMgr.getText("record.delegate");
  }

  @Override
  public void addKeyParameters(ModelAndView mv) {
    mv.addObject("userId", this.userId);
    mv.addObject("delegateId", this.delegateId);
  }

  @Override
  public void addKeyParameters(ModelMap map) {
    map.addAttribute("userId", this.userId);
    map.addAttribute("delegateId", this.delegateId);

  }

}
