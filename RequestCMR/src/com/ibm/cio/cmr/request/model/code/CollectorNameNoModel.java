package com.ibm.cio.cmr.request.model.code;

import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.cio.cmr.request.model.BaseModel;

public class CollectorNameNoModel extends BaseModel {

  private String cmrIssuingCntry;
  private String collectorNo;
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
    if (!StringUtils.isEmpty(this.cmrIssuingCntry) && !StringUtils.isEmpty(this.collectorNo)) {
      allKeysAssigned = true;
    }
    return allKeysAssigned;
  }

  @Override
  public String getRecordDescription() {
    return "Collector Number Definition";
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

  public String getCollectorNo() {
    return collectorNo;
  }

  public void setCollectorNo(String collectorNo) {
    this.collectorNo = collectorNo;
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

}
