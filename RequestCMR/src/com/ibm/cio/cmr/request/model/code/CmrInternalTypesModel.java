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
 * @author Max
 * 
 */
public class CmrInternalTypesModel extends BaseModel {

  private static final long serialVersionUID = -9202929601837918025L;

  private String internalTyp;
  private String internalTypDesc;
  private String cmrIssuingCntry;
  private String reqTyp;
  private int priority;

  private String condition;

  private String sepValInd;

  private String createBy;
  private Date createTs;
  private Date updateTs;
  private String createTsString;
  private String updateTsString;
  private String updateBy;

  private String status;

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

  @Override
  public boolean allKeysAssigned() {
    return !StringUtils.isEmpty(this.internalTyp) && !StringUtils.isEmpty(this.cmrIssuingCntry);
  }

  @Override
  public String getRecordDescription() {
    return "CmrInternalTypes";
  }

  @Override
  public void addKeyParameters(ModelAndView mv) {
    mv.addObject("internalTyp", this.internalTyp);
    mv.addObject("cmrIssuingCntry", this.cmrIssuingCntry);
  }

  @Override
  public void addKeyParameters(ModelMap map) {
    map.put("internalTyp", this.internalTyp);
    map.put("cmrIssuingCntry", this.cmrIssuingCntry);
  }

  public Date getCreateTs() {
    return createTs;
  }

  public void setCreateTs(Date createTs) {
    this.createTs = createTs;
  }

  public Date getUpdateTs() {
    return updateTs;
  }

  public void setUpdateTs(Date updateTs) {
    this.updateTs = updateTs;
  }

  public String getInternalTypDesc() {
    return internalTypDesc;
  }

  public void setInternalTypDesc(String internalTypDesc) {
    this.internalTypDesc = internalTypDesc;
  }

  public String getReqTyp() {
    return reqTyp;
  }

  public void setReqTyp(String reqTyp) {
    this.reqTyp = reqTyp;
  }

  public int getPriority() {
    return priority;
  }

  public void setPriority(int priority) {
    this.priority = priority;
  }

  public String getCondition() {
    return condition;
  }

  public void setCondition(String condition) {
    this.condition = condition;
  }

  public String getSepValInd() {
    return sepValInd;
  }

  public void setSepValInd(String sepValInd) {
    this.sepValInd = sepValInd;
  }

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

}
