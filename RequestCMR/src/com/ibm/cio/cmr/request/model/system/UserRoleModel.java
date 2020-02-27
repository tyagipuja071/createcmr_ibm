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
public class UserRoleModel extends BaseModel {

  /**
   * 
   */
  private static final long serialVersionUID = -1016076254077055288L;
  private String userId;
  private String roleId;
  private String subRoleId;
  private String cntryCode;
  private String createTsString;
  private Date createTs;

  private String createBy;

  private String updateTsString;
  private Date updateTs;

  private String updateBy;

  private String status;

  private String comments;

  private String roleDesc;
  private String roleStatus;
  private String subRoleDesc;

  private String rolesToAdd;

  @Override
  public boolean allKeysAssigned() {
    return !StringUtils.isEmpty(this.userId) && !StringUtils.isEmpty(this.roleId) && this.subRoleId != null;
  }

  @Override
  public String getRecordDescription() {
    return "User Role";
  }

  @Override
  public void addKeyParameters(ModelAndView mv) {
  }

  @Override
  public void addKeyParameters(ModelMap map) {
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public String getRoleId() {
    return roleId;
  }

  public void setRoleId(String roleId) {
    this.roleId = roleId;
  }

  public String getSubRoleId() {
    return subRoleId;
  }

  public void setSubRoleId(String subRoleId) {
    this.subRoleId = subRoleId;
  }

  public String getCntryCode() {
    return cntryCode;
  }

  public void setCntryCode(String cntryCode) {
    this.cntryCode = cntryCode;
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

  public String getCreateBy() {
    return createBy;
  }

  public void setCreateBy(String createBy) {
    this.createBy = createBy;
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

  public String getRoleDesc() {
    return roleDesc;
  }

  public void setRoleDesc(String roleDesc) {
    this.roleDesc = roleDesc;
  }

  public String getRoleStatus() {
    return roleStatus;
  }

  public void setRoleStatus(String roleStatus) {
    this.roleStatus = roleStatus;
  }

  public String getSubRoleDesc() {
    return subRoleDesc;
  }

  public void setSubRoleDesc(String subRoleDesc) {
    this.subRoleDesc = subRoleDesc;
  }

  public String getRolesToAdd() {
    return rolesToAdd;
  }

  public void setRolesToAdd(String rolesToAdd) {
    this.rolesToAdd = rolesToAdd;
  }

}
