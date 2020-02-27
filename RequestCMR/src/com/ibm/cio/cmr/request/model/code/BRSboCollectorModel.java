/**
 * 
 */
package com.ibm.cio.cmr.request.model.code;

import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.cio.cmr.request.model.BaseModel;

/**
 * @author Rangoli Saxena
 * 
 */
public class BRSboCollectorModel extends BaseModel {

  private static final long serialVersionUID = 1L;

  private String stateCd;
  private String stateName;
  private String sbo;
  private String mrcCd;
  private String collectorNo;

  private String createBy;
  private Date createTs;
  private String lastUpdtBy;
  private Date lastUpdtTs;
  private String createTsString;
  private String updtTsString;

  @Override
  public boolean allKeysAssigned() {
    return !StringUtils.isBlank(this.stateCd);
  }

  @Override
  public String getRecordDescription() {
    return "SBO/Collector Entry";
  }

  @Override
  public void addKeyParameters(ModelAndView mv) {
  }

  @Override
  public void addKeyParameters(ModelMap map) {
  }

  public String getStateCd() {
    return stateCd;
  }

  public void setStateCd(String stateCd) {
    this.stateCd = stateCd;
  }

  public String getStateName() {
    return stateName;
  }

  public void setStateName(String stateName) {
    this.stateName = stateName;
  }

  public String getSbo() {
    return sbo;
  }

  public void setSbo(String sbo) {
    this.sbo = sbo;
  }

  public String getMrcCd() {
    return mrcCd;
  }

  public void setMrcCd(String mrcCd) {
    this.mrcCd = mrcCd;
  }

  public String getCollectorNo() {
    return collectorNo;
  }

  public void setCollectorNo(String collectorNo) {
    this.collectorNo = collectorNo;
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

  public String getLastUpdtBy() {
    return lastUpdtBy;
  }

  public void setLastUpdtBy(String lastUpdtBy) {
    this.lastUpdtBy = lastUpdtBy;
  }

  public Date getLastUpdtTs() {
    return lastUpdtTs;
  }

  public void setLastUpdtTs(Date lastUpdtTs) {
    this.lastUpdtTs = lastUpdtTs;
  }

  public String getCreateTsString() {
    return createTsString;
  }

  public void setCreateTsString(String createTsString) {
    this.createTsString = createTsString;
  }

  public String getUpdtTsString() {
    return updtTsString;
  }

  public void setUpdtTsString(String updtTsString) {
    this.updtTsString = updtTsString;
  }
}
