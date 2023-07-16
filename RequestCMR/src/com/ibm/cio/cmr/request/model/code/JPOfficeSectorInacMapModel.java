package com.ibm.cio.cmr.request.model.code;

import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.cio.cmr.request.model.BaseModel;

public class JPOfficeSectorInacMapModel extends BaseModel {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  private String officeCd;
  private String sectorCd;
  private String inacCd;
  private String apCustClusterId;
  private String createBy;
  private Date createTs;
  private String updateBy;
  private Date updateTs;

  public String getOfficeCd() {
    return officeCd;
  }

  public void setOfficeCd(String officeCd) {
    this.officeCd = officeCd;
  }

  public String getSectorCd() {
    return sectorCd;
  }

  public void setSectorCd(String sectorCd) {
    this.sectorCd = sectorCd;
  }

  public String getInacCd() {
    return inacCd;
  }

  public void setInacCd(String inacCd) {
    this.inacCd = inacCd;
  }

  public String getApCustClusterId() {
    return apCustClusterId;
  }

  public void setApCustClusterId(String apCustClusterId) {
    this.apCustClusterId = apCustClusterId;
  }

  public String getCreateBy() {
    return createBy;
  }

  public void setCreateBy(String createBy) {
    this.createBy = createBy;
  }

  public Date getCreateTs() {
    return createTs;
  }

  public void setCreateTs(Date createTs) {
    this.createTs = createTs;
  }

  public String getUpdateBy() {
    return updateBy;
  }

  public void setUpdateBy(String updateBy) {
    this.updateBy = updateBy;
  }

  public Date getUpdateTs() {
    return updateTs;
  }

  public void setUpdateTs(Date updateTs) {
    this.updateTs = updateTs;
  }

  @Override
  public boolean allKeysAssigned() {
    boolean allKeysAssigned = false;
    if (!StringUtils.isEmpty(this.officeCd) && !StringUtils.isEmpty(this.inacCd) && !StringUtils.isEmpty(this.sectorCd)
        && !StringUtils.isEmpty(this.apCustClusterId)) {
      allKeysAssigned = true;
    }
    return allKeysAssigned;
  }

  @Override
  public String getRecordDescription() {
    return "JP OFFICE SECTOR INAC MAPPING";
  }

  @Override
  public void addKeyParameters(ModelAndView mv) {
  }

  @Override
  public void addKeyParameters(ModelMap map) {
  }

}
