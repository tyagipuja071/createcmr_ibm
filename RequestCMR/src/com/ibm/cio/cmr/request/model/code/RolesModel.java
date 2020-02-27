package com.ibm.cio.cmr.request.model.code;

import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.cio.cmr.request.model.BaseModel;

public class RolesModel extends BaseModel {

  /**
   * @author Jose Belgira
   */
  private static final long serialVersionUID = 619021112605131138L;
  private String roleId;
  private String roleName;
  private String applicationCd;
  private Date createTs;
  private String createBy;
  private Date updateTs;
  private String updateBy;
  private String status;
  private String comments;
  private String createTsString;
  private String updateTsString;
  private String user;
  private String statusDefinition;
  private String applicationCdDefinition;

  @Override
  public boolean allKeysAssigned() {
    return !StringUtils.isEmpty(this.roleId);
  }

  @Override
  public String getRecordDescription() {
    return "Roles";
  }

  @Override
  public void addKeyParameters(ModelAndView mv) {
  }

  @Override
  public void addKeyParameters(ModelMap map) {
  }

  public String getRoleId() {
    return roleId;
  }

  public void setRoleId(String roleId) {
    this.roleId = roleId;
  }

  public String getRoleName() {
    return roleName;
  }

  public void setRoleName(String roleName) {
    this.roleName = roleName;
  }

  public String getApplicationCd() {
    return applicationCd;
  }

  public void setApplicationCd(String applicationCd) {
    this.applicationCd = applicationCd;
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

  public String getCreateTsString() {
    return createTsString;
  }

  public void setCreateTsString(String createTsString) {
    this.createTsString = createTsString;
  }

  public String getUpdateTsString() {
    return updateTsString;
  }

  public void setUpdateTsString(String updateTsString) {
    this.updateTsString = updateTsString;
  }

  public String getUser() {
    return user;
  }

  public void setUser(String user) {
    this.user = user;
  }

  public String getStatusDefinition() {
    return statusDefinition;
  }

  public void setStatusDefinition(String statusDefinition) {
    this.statusDefinition = statusDefinition;
  }

  public String getApplicationCdDefinition() {
    return applicationCdDefinition;
  }

  public void setApplicationCdDefinition(String applicationCdDefinition) {
    this.applicationCdDefinition = applicationCdDefinition;
  }

}
