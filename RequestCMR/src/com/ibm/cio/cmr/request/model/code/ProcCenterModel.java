/**
 * 
 */
package com.ibm.cio.cmr.request.model.code;

import org.apache.commons.lang.StringUtils;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.cio.cmr.request.model.BaseModel;

/**
 * 
 * @author Eduard Bernardo
 */
public class ProcCenterModel extends BaseModel {

  private static final long serialVersionUID = 1L;

  private String cmrIssuingCntry;
  private String procCenterNm;
  private String cmt;
  private String country;

  public String getCmrIssuingCntry() {
    return cmrIssuingCntry;
  }

  public void setCmrIssuingCntry(String cmrIssuingCntry) {
    this.cmrIssuingCntry = cmrIssuingCntry;
  }

  @Override
  public boolean allKeysAssigned() {
    return !StringUtils.isEmpty(this.cmrIssuingCntry);
  }

  @Override
  public String getRecordDescription() {
    return "Processing Center";
  }

  @Override
  public void addKeyParameters(ModelAndView mv) {
    mv.addObject("cmrIssuingCntry", this.cmrIssuingCntry);
  }

  @Override
  public void addKeyParameters(ModelMap map) {
    map.put("cmrIssuingCntry", this.cmrIssuingCntry);
  }

  public String getProcCenterNm() {
    return procCenterNm;
  }

  public void setProcCenterNm(String procCenterNm) {
    this.procCenterNm = procCenterNm;
  }

  public String getCmt() {
    return cmt;
  }

  public void setCmt(String cmt) {
    this.cmt = cmt;
  }

  public String getCountry() {
    return country;
  }

  public void setCountry(String country) {
    this.country = country;
  }

}