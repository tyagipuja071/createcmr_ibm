/**
 * 
 */
package com.ibm.cio.cmr.request.model.pref;

import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.cio.cmr.request.model.BaseModel;

/**
 * @author Jeffrey Zamora
 * 
 */
public class UserPrefCountryModel extends BaseModel {

  private static final long serialVersionUID = 5594056147227920481L;

  private String requesterId;

  private String issuingCntry;
  private String removeCntry;

  private Date createTs;

  private String createTsString;

  private String createBy;

  @Override
  public boolean allKeysAssigned() {
    return !StringUtils.isBlank(this.requesterId) && !StringUtils.isBlank(this.issuingCntry);
  }

  @Override
  public String getRecordDescription() {
    return "User Preferred Country";
  }

  @Override
  public void addKeyParameters(ModelAndView mv) {
    mv.addObject("requesterId", this.requesterId);
    mv.addObject("issuingCntry", this.issuingCntry);
  }

  @Override
  public void addKeyParameters(ModelMap map) {
    map.put("requesterId", this.requesterId);
    map.put("issuingCntry", this.issuingCntry);
  }

  public String getRequesterId() {
    return requesterId;
  }

  public void setRequesterId(String requesterId) {
    this.requesterId = requesterId;
  }

  public String getIssuingCntry() {
    return issuingCntry;
  }

  public void setIssuingCntry(String issuingCntry) {
    this.issuingCntry = issuingCntry;
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

  public String getCreateTsString() {
    return createTsString;
  }

  public void setCreateTsString(String createTsString) {
    this.createTsString = createTsString;
  }

  public String getRemoveCntry() {
    return removeCntry;
  }

  public void setRemoveCntry(String removeCntry) {
    this.removeCntry = removeCntry;
  }

}
