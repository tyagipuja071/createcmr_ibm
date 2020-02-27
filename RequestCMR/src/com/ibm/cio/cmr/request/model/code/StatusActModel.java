package com.ibm.cio.cmr.request.model.code;

import org.apache.commons.lang.StringUtils;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.cio.cmr.request.model.BaseModel;

public class StatusActModel extends BaseModel {

  /**
   * @author Jose Belgira
   */
  private static final long serialVersionUID = 307437854940355463L;
  private String modelAction;
  private String cmrIssuingCntry;
  private String actionDesc;
  private String cmrIssuingCntryDesc;

  @Override
  public boolean allKeysAssigned() {
    return !StringUtils.isEmpty(this.modelAction);
  }

  @Override
  public String getRecordDescription() {
    return "Status-Action";
  }

  @Override
  public void addKeyParameters(ModelAndView mv) {
  }

  @Override
  public void addKeyParameters(ModelMap map) {
  }

  public String getModelAction() {
    return modelAction;
  }

  public void setModelAction(String modelAction) {
    this.modelAction = modelAction;
  }

  public String getCmrIssuingCntry() {
    return cmrIssuingCntry;
  }

  public void setCmrIssuingCntry(String cmrIssuingCntry) {
    this.cmrIssuingCntry = cmrIssuingCntry;
  }

  public String getActionDesc() {
    return actionDesc;
  }

  public void setActionDesc(String actionDesc) {
    this.actionDesc = actionDesc;
  }

  public String getCmrIssuingCntryDesc() {
    return cmrIssuingCntryDesc;
  }

  public void setCmrIssuingCntryDesc(String cmrIssuingCntryDesc) {
    this.cmrIssuingCntryDesc = cmrIssuingCntryDesc;
  }

}
