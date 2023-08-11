package com.ibm.cio.cmr.request.model.code;

import java.sql.Timestamp;

import org.apache.commons.lang3.StringUtils;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.cio.cmr.request.model.BaseModel;

public class JpJsicCodeMapModel extends BaseModel {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  private String jsicCd;
  private String subIndustryCd;
  private String isuCd;
  private String isicCd;
  private String dept;
  private String sectorCd;
  private String createBy;
  private Timestamp createTs;
  private String updateBy;
  private Timestamp updateTs;

  public String getJsicCd() {
    return jsicCd;
  }

  public void setJsicCd(String jsicCd) {
    this.jsicCd = jsicCd;
  }

  public String getSubIndustryCd() {
    return subIndustryCd;
  }

  public void setSubIndustryCd(String subIndustryCd) {
    this.subIndustryCd = subIndustryCd;
  }

  public String getIsuCd() {
    return isuCd;
  }

  public void setIsuCd(String isuCd) {
    this.isuCd = isuCd;
  }

  public String getIsicCd() {
    return isicCd;
  }

  public void setIsicCd(String isicCd) {
    this.isicCd = isicCd;
  }

  public String getDept() {
    return dept;
  }

  public void setDept(String dept) {
    this.dept = dept;
  }

  public String getSectorCd() {
    return sectorCd;
  }

  public void setSectorCd(String sectorCd) {
    this.sectorCd = sectorCd;
  }

  public String getCreateBy() {
    return createBy;
  }

  public void setCreateBy(String createBy) {
    this.createBy = createBy;
  }

  public Timestamp getCreateTs() {
    return createTs;
  }

  public void setCreateTs(Timestamp createTs) {
    this.createTs = createTs;
  }

  public String getUpdateBy() {
    return updateBy;
  }

  public void setUpdateBy(String updateBy) {
    this.updateBy = updateBy;
  }

  public Timestamp getUpdateTs() {
    return updateTs;
  }

  public void setUpdateTs(Timestamp updateTs) {
    this.updateTs = updateTs;
  }

  @Override
  public boolean allKeysAssigned() {
    boolean allKeysAssigned = false;
    if (!StringUtils.isEmpty(this.jsicCd) && !StringUtils.isEmpty(this.subIndustryCd) && !StringUtils.isEmpty(this.isuCd)
        && !StringUtils.isEmpty(this.isicCd) && !StringUtils.isEmpty(this.dept)) {
      allKeysAssigned = true;
    }
    return allKeysAssigned;
  }

  @Override
  public String getRecordDescription() {
    return "JP JSIC CODE MAP";
  }

  @Override
  public void addKeyParameters(ModelAndView mv) {
  }

  @Override
  public void addKeyParameters(ModelMap map) {
  }

}
