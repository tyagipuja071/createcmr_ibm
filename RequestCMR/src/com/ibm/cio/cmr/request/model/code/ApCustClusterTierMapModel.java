package com.ibm.cio.cmr.request.model.code;

import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.cio.cmr.request.model.BaseModel;

/**
 * @author Anuja Srivastava
 * 
 */
public class ApCustClusterTierMapModel extends BaseModel {

  private static final long serialVersionUID = 1L;

  private String issuingCntry;
  private String apCustClusterId;
  private String clientTierCd;
  private String clusterDesc;
  private String createBy;
  private Date createTs;
  private String updtBy;
  private Date updtTs;
  private String isuCode;
  private String cmrIssuingCntry;

  public String getCmrIssuingCntry() {
    return cmrIssuingCntry;
  }

  public void setCmrIssuingCntry(String cmrIssuingCntry) {
    this.cmrIssuingCntry = cmrIssuingCntry;
  }

  @Override
  public boolean allKeysAssigned() {
    boolean allKeysAssigned = false;
    if (!StringUtils.isEmpty(this.issuingCntry) && !StringUtils.isEmpty(this.apCustClusterId) && !StringUtils.isEmpty(this.clientTierCd)
        && !StringUtils.isEmpty(this.isuCode)) {
      allKeysAssigned = true;
    }
    return allKeysAssigned;
  }

  @Override
  public String getRecordDescription() {
    return "Customer Clusters Tier Mapping";
  }

  @Override
  public void addKeyParameters(ModelAndView mv) {
  }

  @Override
  public void addKeyParameters(ModelMap map) {
  }

  public String getIssuingCntry() {
    return issuingCntry;
  }

  public void setIssuingCntry(String issuingCntry) {
    this.issuingCntry = issuingCntry;
  }

  public String getApCustClusterId() {
    return apCustClusterId;
  }

  public void setApCustClusterId(String apCustClusterId) {
    this.apCustClusterId = apCustClusterId;
  }

  public String getClientTierCd() {
    return clientTierCd;
  }

  public void setClientTierCd(String clientTierCd) {
    this.clientTierCd = clientTierCd;
  }

  public String getClusterDesc() {
    return clusterDesc;
  }

  public void setClusterDesc(String clusterDesc) {
    this.clusterDesc = clusterDesc;
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

  public String getUpdtBy() {
    return updtBy;
  }

  public void setUpdtBy(String updtBy) {
    this.updtBy = updtBy;
  }

  public Date getUpdtTs() {
    return updtTs;
  }

  public void setUpdtTs(Date updtTs) {
    this.updtTs = updtTs;
  }

  public String getIsuCode() {
    return isuCode;
  }

  public void setIsuCode(String isuCode) {
    this.isuCode = isuCode;
  }

}
