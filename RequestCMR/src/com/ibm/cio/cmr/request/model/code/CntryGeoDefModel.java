package com.ibm.cio.cmr.request.model.code;

import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.cio.cmr.request.model.BaseModel;

public class CntryGeoDefModel extends BaseModel {

  private static final long serialVersionUID = 1L;

  private String geoCd;
  private String cmrIssuingCntry;
  private String cntryDesc;
  private String comments;
  private Date createTs;
  private String createBy;
  private Date lastUpdtTs;
  private String lastUpdtBy;

  public String getGeoCd() {
    return geoCd;
  }

  public void setGeoCd(String geoCd) {
    this.geoCd = geoCd;
  }

  public String getCmrIssuingCntry() {
    return cmrIssuingCntry;
  }

  public void setCmrIssuingCntry(String cmrIssuingCntry) {
    this.cmrIssuingCntry = cmrIssuingCntry;
  }

  public String getCntryDesc() {
    return cntryDesc;
  }

  public void setCntryDesc(String cntryDesc) {
    this.cntryDesc = cntryDesc;
  }

  public String getComments() {
    return comments;
  }

  public void setComments(String comments) {
    this.comments = comments;
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

  @Override
  public boolean allKeysAssigned() {
    return !StringUtils.isEmpty(this.geoCd) && !StringUtils.isEmpty(this.cmrIssuingCntry);
  }

  @Override
  public String getRecordDescription() {
    return "Country Geo Default";
  }

  @Override
  public void addKeyParameters(ModelAndView mv) {
  }

  @Override
  public void addKeyParameters(ModelMap map) {
  }

}
