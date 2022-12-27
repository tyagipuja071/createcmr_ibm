package com.ibm.cio.cmr.request.model.code;

import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.cio.cmr.request.model.BaseModel;

public class SuppCountryModel extends BaseModel {

  /**
   * @author Jose Belgira
   */
  private static final long serialVersionUID = 1L;
  private String cntryCd;
  private String nm;
  private Date createDt;
  private String cmt;
  private String autoProcEnabled;
  private String hostSysTyp;
  private String suppReqType;
  private String createDtStringFormat;
  private boolean autoProcCheckBox;
  private String defaultLandedCntry;
  private String processingTyp;
  private String autoEngineIndc;
  private String recoveryDirection;
  private String dnbPrimaryIndc;
  private String startQuickSearch;
  private String tradestyleNmUsage;
  private String disableCreateByModel;
  private String hideLocalLangData;

  @Override
  public boolean allKeysAssigned() {
    return !StringUtils.isEmpty(this.cntryCd);
  }

  @Override
  public String getRecordDescription() {
    return "Supporting Country";
  }

  @Override
  public void addKeyParameters(ModelAndView mv) {
  }

  @Override
  public void addKeyParameters(ModelMap map) {
  }

  public String getCntryCd() {
    return cntryCd;
  }

  public void setCntryCd(String cntryCd) {
    this.cntryCd = cntryCd;
  }

  public String getNm() {
    return nm;
  }

  public void setNm(String nm) {
    this.nm = nm;
  }

  public Date getCreateDt() {
    return createDt;
  }

  public void setCreateDt(Date createDt) {
    this.createDt = createDt;
  }

  public String getCmt() {
    return cmt;
  }

  public void setCmt(String cmt) {
    this.cmt = cmt;
  }

  public String getAutoProcEnabled() {
    return autoProcEnabled;
  }

  public void setAutoProcEnabled(String autoProcEnabled) {
    this.autoProcEnabled = autoProcEnabled;
  }

  public String getHostSysTyp() {
    return hostSysTyp;
  }

  public void setHostSysTyp(String hostSysTyp) {
    this.hostSysTyp = hostSysTyp;
  }

  public String getSuppReqType() {
    return suppReqType;
  }

  public void setSuppReqType(String suppReqType) {
    this.suppReqType = suppReqType;
  }

  public String getCreateDtStringFormat() {
    return createDtStringFormat;
  }

  public void setCreateDtStringFormat(String createDtStringFormat) {
    this.createDtStringFormat = createDtStringFormat;
  }

  public boolean isAutoProcCheckBox() {
    return autoProcCheckBox;
  }

  public void setAutoProcCheckBox(boolean autoProcCheckBox) {
    this.autoProcCheckBox = autoProcCheckBox;
  }

  public String getDefaultLandedCntry() {
    return defaultLandedCntry;
  }

  public void setDefaultLandedCntry(String defaultLandedCntry) {
    this.defaultLandedCntry = defaultLandedCntry;
  }

  public String getProcessingTyp() {
    return processingTyp;
  }

  public void setProcessingTyp(String processingTyp) {
    this.processingTyp = processingTyp;
  }

  public String getAutoEngineIndc() {
    return autoEngineIndc;
  }

  public void setAutoEngineIndc(String autoEngineIndc) {
    this.autoEngineIndc = autoEngineIndc;
  }

  public String getRecoveryDirection() {
    return recoveryDirection;
  }

  public void setRecoveryDirection(String recoveryDirection) {
    this.recoveryDirection = recoveryDirection;
  }

  public String getDnbPrimaryIndc() {
    return dnbPrimaryIndc;
  }

  public void setDnbPrimaryIndc(String dnbPrimaryIndc) {
    this.dnbPrimaryIndc = dnbPrimaryIndc;
  }

  public String getStartQuickSearch() {
    return startQuickSearch;
  }

  public void setStartQuickSearch(String startQuickSearch) {
    this.startQuickSearch = startQuickSearch;
  }

  public String getTradestyleNmUsage() {
    return tradestyleNmUsage;
  }

  public void setTradestyleNmUsage(String tradestyleNmUsage) {
    this.tradestyleNmUsage = tradestyleNmUsage;
  }

  public String getDisableCreateByModel() {
    return disableCreateByModel;
  }

  public void setDisableCreateByModel(String disableCreateByModel) {
    this.disableCreateByModel = disableCreateByModel;
  }

  public String getHideLocalLangData() {
    return hideLocalLangData;
  }

  public void setHideLocalLangData(String hideLocalLangData) {
    this.hideLocalLangData = hideLocalLangData;
  }

}
