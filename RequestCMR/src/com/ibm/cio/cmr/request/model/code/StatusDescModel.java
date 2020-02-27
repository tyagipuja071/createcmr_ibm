package com.ibm.cio.cmr.request.model.code;

import org.apache.commons.lang.StringUtils;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.cio.cmr.request.model.BaseModel;

public class StatusDescModel extends BaseModel {

  private static final long serialVersionUID = -4806959463235824036L;

  private String reqStatus;
  private String cmrIssuingCntry;
  private String statusDesc;

  public String getReqStatus() {
    return reqStatus;
  }

  public void setReqStatus(String reqStatus) {
    this.reqStatus = reqStatus;
  }

  public String getCmrIssuingCntry() {
    return cmrIssuingCntry;
  }

  public void setCmrIssuingCntry(String cmrIssuingCntry) {
    this.cmrIssuingCntry = cmrIssuingCntry;
  }

  public String getStatusDesc() {
    return statusDesc;
  }

  public void setStatusDesc(String statusDesc) {
    this.statusDesc = statusDesc;
  }

  @Override
  public boolean allKeysAssigned() {
    return !StringUtils.isEmpty(this.reqStatus);
  }

  @Override
  public String getRecordDescription() {
    return "Status Description";
  }

  @Override
  public void addKeyParameters(ModelAndView mv) {
    mv.addObject("reqStatus", this.reqStatus);
  }

  @Override
  public void addKeyParameters(ModelMap map) {
    map.put("reqStatus", this.reqStatus);
  }
}
