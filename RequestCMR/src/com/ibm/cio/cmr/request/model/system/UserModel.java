/**
 * 
 */
package com.ibm.cio.cmr.request.model.system;

import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.cio.cmr.request.model.BaseModel;

/**
 * @author Jeffrey Zamora
 * 
 */
public class UserModel extends BaseModel {

  private static final long serialVersionUID = -9202929601837918024L;

  private String userId;

  private String userName;

  private String createTsString;
  private Date createTs;

  private String createBy;

  private String updateTsString;
  private Date updateTs;

  private String updateBy;

  private String status;

  private String comments;

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public String getUserName() {
    return userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

  public String getCreateBy() {
    return createBy;
  }

  public void setCreateBy(String createBy) {
    this.createBy = createBy;
  }

  public String getUpdateBy() {
    return updateBy;
  }

  public void setUpdateBy(String updateBy) {
    this.updateBy = updateBy;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getComments() {
    return comments;
  }

  public void setComments(String comments) {
    this.comments = comments;
  }

  @Override
  public boolean allKeysAssigned() {
    return !StringUtils.isEmpty(this.userId);
  }

  @Override
  public String getRecordDescription() {
    return "User";
  }

  @Override
  public void addKeyParameters(ModelAndView mv) {
    mv.addObject("userId", this.userId);
  }

  @Override
  public void addKeyParameters(ModelMap map) {
    map.put("userId", this.userId);
  }

  public String getCreateTsString() {
    return createTsString;
  }

  public void setCreateTsString(String createTsString) {
    this.createTsString = createTsString;
  }

  public Date getCreateTs() {
    return createTs;
  }

  public void setCreateTs(Date createTs) {
    this.createTs = createTs;
  }

  public String getUpdateTsString() {
    return updateTsString;
  }

  public void setUpdateTsString(String updateTsString) {
    this.updateTsString = updateTsString;
  }

  public Date getUpdateTs() {
    return updateTs;
  }

  public void setUpdateTs(Date updateTs) {
    this.updateTs = updateTs;
  }

}
