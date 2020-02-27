package com.ibm.cio.cmr.request.model.code;

import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.cio.cmr.request.model.BaseModel;

public class SysParametersModel extends BaseModel {

  /**
   * @author Priy Ranjan
   */
  private static final long serialVersionUID = 1L;
  private String parameterCd;
  private String parameterName;
  private String parameterTyp;
  private String parameterValue;
  private boolean cmdeMaintIndcCheckBox;
  private String cmdeMaintainableIndc;
  private Date createDt;
  private String createBy;
  private String createDtStringFormat;
  private Date updateDt;
  private String updateBy;
  private String updateDtStringFormat;

  @Override
  public boolean allKeysAssigned() {
    return !StringUtils.isEmpty(this.parameterCd);
  }

  @Override
  public String getRecordDescription() {
    return "System Parameters";
  }

  @Override
  public void addKeyParameters(ModelAndView mv) {
  }

  @Override
  public void addKeyParameters(ModelMap map) {
  }

  public String getParameterCd() {
    return parameterCd;
  }

  public void setParameterCd(String parameterCd) {
    this.parameterCd = parameterCd;
  }

  public String getParameterName() {
    return parameterName;
  }

  public void setParameterName(String parameterName) {
    this.parameterName = parameterName;
  }

  public String getParameterTyp() {
    return parameterTyp;
  }

  public void setParameterTyp(String parameterTyp) {
    this.parameterTyp = parameterTyp;
  }

  public String getParameterValue() {
    return parameterValue;
  }

  public void setParameterValue(String parameterValue) {
    this.parameterValue = parameterValue;
  }

  public boolean isCmdeMaintIndcCheckBox() {
    return cmdeMaintIndcCheckBox;
  }

  public void setCmdeMaintIndcCheckBox(boolean cmdeMaintIndcCheckBox) {
    this.cmdeMaintIndcCheckBox = cmdeMaintIndcCheckBox;
  }

  public String getCmdeMaintainableIndc() {
    return cmdeMaintainableIndc;
  }

  public void setCmdeMaintainableIndc(String cmdeMaintainableIndc) {
    this.cmdeMaintainableIndc = cmdeMaintainableIndc;
  }

  public Date getCreateDt() {
    return createDt;
  }

  public void setCreateDt(Date createDt) {
    this.createDt = createDt;
  }

  public String getCreateBy() {
    return createBy;
  }

  public void setCreateBy(String createBy) {
    this.createBy = createBy;
  }

  public String getCreateDtStringFormat() {
    return createDtStringFormat;
  }

  public void setCreateDtStringFormat(String createDtStringFormat) {
    this.createDtStringFormat = createDtStringFormat;
  }

  public Date getUpdateDt() {
    return updateDt;
  }

  public void setUpdateDt(Date updateDt) {
    this.updateDt = updateDt;
  }

  public String getUpdateBy() {
    return updateBy;
  }

  public void setUpdateBy(String updateBy) {
    this.updateBy = updateBy;
  }

  public String getUpdateDtStringFormat() {
    return updateDtStringFormat;
  }

  public void setUpdateDtStringFormat(String updateDtStringFormat) {
    this.updateDtStringFormat = updateDtStringFormat;
  }

}
