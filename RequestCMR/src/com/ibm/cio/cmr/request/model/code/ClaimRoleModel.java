/**
 * 
 */
package com.ibm.cio.cmr.request.model.code;

import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.cio.cmr.request.model.BaseModel;

/**
 * 
 * @author Eduard Bernardo
 */
public class ClaimRoleModel extends BaseModel {

  private static final long serialVersionUID = 1L;

  private String cmrIssuingCntry;
  private String internalTyp;
  private String reqStatus;
  private String claimRoleId;
  private String claimSubRoleId;
  private String status;
  private String createBy;
  private Date createTs;
  private String updateBy;
  private Date updateTs;
  private String createTsString;
  private String updateTsString;
  private String country;
  private String reqStatusDesc;
  private String claimRoleDesc;
  private String claimSubRoleDesc;
  private String internalTypDesc;

  public String getCmrIssuingCntry() {
    return cmrIssuingCntry;
  }

  public void setCmrIssuingCntry(String cmrIssuingCntry) {
    this.cmrIssuingCntry = cmrIssuingCntry;
  }

  public String getInternalTyp() {
    return internalTyp;
  }

  public void setInternalTyp(String internalTyp) {
    this.internalTyp = internalTyp;
  }

  public String getReqStatus() {
    return reqStatus;
  }

  public void setReqStatus(String reqStatus) {
    this.reqStatus = reqStatus;
  }

  public String getClaimRoleId() {
    return this.claimRoleId;
  }

  public void setClaimRoleId(String claimRoleId) {
    this.claimRoleId = claimRoleId;
  }

  public String getClaimSubRoleId() {
    return this.claimSubRoleId;
  }

  public void setClaimSubRoleId(String claimSubRoleId) {
    this.claimSubRoleId = claimSubRoleId;
  }

  public String getStatus() {
    return this.status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getCreateBy() {
    return this.createBy;
  }

  public void setCreateBy(String createBy) {
    this.createBy = createBy;
  }

  public Date getCreateTs() {
    return this.createTs;
  }

  public void setCreateTs(Date createTs) {
    this.createTs = createTs;
  }

  public String getUpdateBy() {
    return this.updateBy;
  }

  public void setUpdateBy(String updateBy) {
    this.updateBy = updateBy;
  }

  public Date getUpdateTs() {
    return this.updateTs;
  }

  public void setUpdateTs(Date updateTs) {
    this.updateTs = updateTs;
  }

  @Override
  public boolean allKeysAssigned() {
    return !StringUtils.isEmpty(this.internalTyp) && !StringUtils.isEmpty(this.cmrIssuingCntry) && !StringUtils.isEmpty(this.reqStatus);
  }

  @Override
  public String getRecordDescription() {
    return "Claim Roles";
  }

  @Override
  public void addKeyParameters(ModelAndView mv) {
    mv.addObject("cmrIssuingCntry", this.cmrIssuingCntry);
    mv.addObject("internalTyp", this.internalTyp);
    mv.addObject("reqStatus", this.reqStatus);
  }

  @Override
  public void addKeyParameters(ModelMap map) {
    map.put("cmrIssuingCntry", this.cmrIssuingCntry);
    map.put("internalTyp", this.internalTyp);
    map.put("reqStatus", this.reqStatus);
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

  public String getCountry() {
    return country;
  }

  public void setCountry(String country) {
    this.country = country;
  }

  public String getReqStatusDesc() {
    return reqStatusDesc;
  }

  public void setReqStatusDesc(String reqStatusDesc) {
    this.reqStatusDesc = reqStatusDesc;
  }

  public String getClaimRoleDesc() {
    return claimRoleDesc;
  }

  public void setClaimRoleDesc(String claimRoleDesc) {
    this.claimRoleDesc = claimRoleDesc;
  }

  public String getClaimSubRoleDesc() {
    return claimSubRoleDesc;
  }

  public void setClaimSubRoleDesc(String claimSubRoleDesc) {
    this.claimSubRoleDesc = claimSubRoleDesc;
  }

  public String getInternalTypDesc() {
    return internalTypDesc;
  }

  public void setInternalTypDesc(String internalTypDesc) {
    this.internalTypDesc = internalTypDesc;
  }

}