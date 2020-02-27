package com.ibm.cio.cmr.request.model.code;

import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.cio.cmr.request.model.BaseModel;

public class GeoCitiesModel extends BaseModel {
  private String cmrIssuingCntry;
  private String cityId;
  private String cityDesc;
  private String stateProv;
  private Date createTs;
  private String createBy;
  private Date lastUpdtTs;
  private String lastUpdtBy;

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  @Override
  public boolean allKeysAssigned() {
    boolean allKeysAssigned = false;
    if (!StringUtils.isEmpty(this.cmrIssuingCntry) && !StringUtils.isEmpty(this.cityId) && !StringUtils.isEmpty(this.cityDesc)) {
      allKeysAssigned = true;
    }
    return allKeysAssigned;
  }

  @Override
  public String getRecordDescription() {
    return "Geo Cities Definition";
  }

  @Override
  public void addKeyParameters(ModelAndView mv) {
    // TODO Auto-generated method stub

  }

  @Override
  public void addKeyParameters(ModelMap map) {
    // TODO Auto-generated method stub

  }

  public String getCmrIssuingCntry() {
    return cmrIssuingCntry;
  }

  public void setCmrIssuingCntry(String cmrIssuingCntry) {
    this.cmrIssuingCntry = cmrIssuingCntry;
  }

  public String getCityId() {
    return cityId;
  }

  public void setCityId(String cityId) {
    this.cityId = cityId;
  }

  public String getCityDesc() {
    return cityDesc;
  }

  public void setCityDesc(String cityDesc) {
    this.cityDesc = cityDesc;
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

  public Date getLastUpdtTs() {
    return lastUpdtTs;
  }

  public void setLastUpdtTs(Date lastUpdtTs) {
    this.lastUpdtTs = lastUpdtTs;
  }

  public String getLastUpdtBy() {
    return lastUpdtBy;
  }

  public void setLastUpdtBy(String lastUpdtBy) {
    this.lastUpdtBy = lastUpdtBy;
  }

  public String getStateProv() {
    return stateProv;
  }

  public void setStateProv(String stateProv) {
    this.stateProv = stateProv;
  }

}
