package com.ibm.cio.cmr.request.model.code;

import org.apache.commons.lang.StringUtils;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.cio.cmr.request.model.BaseModel;

/**
 * 
 * @author RoopakChugh
 *
 */

public class SalesBoModel extends BaseModel {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  private String issuingCntry;
  private String repTeamCd;
  private String salesBoCd;
  private String salesBoDesc;
  private String mrcCd;
  private String clientTier;
  private String isuCd;

  @Override
  public boolean allKeysAssigned() {
    boolean allKeysAssigned = false;
    if (!StringUtils.isEmpty(this.issuingCntry) && !StringUtils.isEmpty(this.repTeamCd) && !StringUtils.isEmpty(this.salesBoCd)) {
      allKeysAssigned = true;
    }
    return allKeysAssigned;
  }

  @Override
  public String getRecordDescription() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void addKeyParameters(ModelAndView mv) {
    // TODO Auto-generated method stub

  }

  @Override
  public void addKeyParameters(ModelMap map) {
    // TODO Auto-generated method stub

  }

  public String getIssuingCntry() {
    return issuingCntry;
  }

  public void setIssuingCntry(String issuingCntry) {
    this.issuingCntry = issuingCntry;
  }

  public String getRepTeamCd() {
    return repTeamCd;
  }

  public void setRepTeamCd(String repTeamCd) {
    this.repTeamCd = repTeamCd;
  }

  public String getSalesBoCd() {
    return salesBoCd;
  }

  public void setSalesBoCd(String salesBoCd) {
    this.salesBoCd = salesBoCd;
  }

  public String getSalesBoDesc() {
    return salesBoDesc;
  }

  public void setSalesBoDesc(String salesBoDesc) {
    this.salesBoDesc = salesBoDesc;
  }

  public String getMrcCd() {
    return mrcCd;
  }

  public void setMrcCd(String mrcCd) {
    this.mrcCd = mrcCd;
  }

  public String getClientTier() {
    return clientTier;
  }

  public void setClientTier(String clientTier) {
    this.clientTier = clientTier;
  }

  public String getIsuCd() {
    return isuCd;
  }

  public void setIsuCd(String isuCd) {
    this.isuCd = isuCd;
  }

}
