package com.ibm.cio.cmr.request.model.code;

import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.cio.cmr.request.model.BaseModel;

public class JpBoCodesMapModel extends BaseModel {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  private String subsidiaryCd;
  private String officeCd;
  private String subOfficeCd;
  private String boCd;
  private String fieldSalesCd;
  private String salesOfficeCd;
  private String mktgDivCd;
  private String mrcCd;
  private String deptCd;
  private String mktgDeptName;
  private String clusterId;
  private String clientTierCd;
  private String isuCdOverride;
  private String isicCd;
  private String createBy;
  private Date createTs;
  private String createTsStr;
  private String updateBy;
  private Date updateTs;
  private String updateTsStr;

  public String getSubsidiaryCd() {
    return subsidiaryCd;
  }

  public void setSubsidiaryCd(String subsidiaryCd) {
    this.subsidiaryCd = subsidiaryCd;
  }

  public String getOfficeCd() {
    return officeCd;
  }

  public void setOfficeCd(String officeCd) {
    this.officeCd = officeCd;
  }

  public String getSubOfficeCd() {
    return subOfficeCd;
  }

  public void setSubOfficeCd(String subOfficeCd) {
    this.subOfficeCd = subOfficeCd;
  }

  public String getBoCd() {
    return boCd;
  }

  public void setBoCd(String boCd) {
    this.boCd = boCd;
  }

  public String getFieldSalesCd() {
    return fieldSalesCd;
  }

  public void setFieldSalesCd(String fieldSalesCd) {
    this.fieldSalesCd = fieldSalesCd;
  }

  public String getSalesOfficeCd() {
    return salesOfficeCd;
  }

  public void setSalesOfficeCd(String salesOfficeCd) {
    this.salesOfficeCd = salesOfficeCd;
  }

  public String getMktgDivCd() {
    return mktgDivCd;
  }

  public void setMktgDivCd(String mktgDivCd) {
    this.mktgDivCd = mktgDivCd;
  }

  public String getMrcCd() {
    return mrcCd;
  }

  public void setMrcCd(String mrcCd) {
    this.mrcCd = mrcCd;
  }

  public String getDeptCd() {
    return deptCd;
  }

  public void setDeptCd(String deptCd) {
    this.deptCd = deptCd;
  }

  public String getMktgDeptName() {
    return mktgDeptName;
  }

  public void setMktgDeptName(String mktgDeptName) {
    this.mktgDeptName = mktgDeptName;
  }

  public String getClusterId() {
    return clusterId;
  }

  public void setClusterId(String clusterId) {
    this.clusterId = clusterId;
  }

  public String getClientTierCd() {
    return clientTierCd;
  }

  public void setClientTierCd(String clientTierCd) {
    this.clientTierCd = clientTierCd;
  }

  public String getIsuCdOverride() {
    return isuCdOverride;
  }

  public void setIsuCdOverride(String isuCdOverride) {
    this.isuCdOverride = isuCdOverride;
  }

  public String getIsicCd() {
    return isicCd;
  }

  public void setIsicCd(String isicCd) {
    this.isicCd = isicCd;
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
    if (!StringUtils.isEmpty(this.subsidiaryCd) && !StringUtils.isEmpty(this.officeCd) && !StringUtils.isEmpty(this.subOfficeCd)) {
      allKeysAssigned = true;
    }
    return allKeysAssigned;
  }

  @Override
  public String getRecordDescription() {
    return "JP BO CODES MAP";
  }

  @Override
  public void addKeyParameters(ModelAndView mv) {
  }

  @Override
  public void addKeyParameters(ModelMap map) {
  }

  public String getCreateTsStr() {
    return createTsStr;
  }

  public void setCreateTsStr(String createTsStr) {
    this.createTsStr = createTsStr;
  }

  public String getUpdateTsStr() {
    return updateTsStr;
  }

  public void setUpdateTsStr(String updateTsStr) {
    this.updateTsStr = updateTsStr;
  }

}
