package com.ibm.cio.cmr.request.model.code;

import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.cio.cmr.request.model.BaseModel;

public class GCARSUpdtQueueModel extends BaseModel {

  private static final long serialVersionUID = 1L;

  private String sourceName;

  private Long seqNo;

  private String cmrIssuingCntry;

  private String cmrNo;

  private String codCondition;

  private String codRsn;

  private Date codEffDate;

  private String procStatus;

  private String procMsg;

  private String createdBy;

  private Date createDt;

  private String updatedBy;

  private Date updateDt;

  private String katr10;

  private String createdTsStr;

  private String updatedTsStr;

  private String searchCriteria;

  public String getSearchCriteria() {
    return searchCriteria;
  }

  public void setSearchCriteria(String searchCriteria) {
    this.searchCriteria = searchCriteria;
  }

  public String getSourceName() {
    return sourceName;
  }

  public void setSourceName(String sourceName) {
    this.sourceName = sourceName;
  }

  public Long getSeqNo() {
    return seqNo;
  }

  public void setSeqNo(Long seqNo) {
    this.seqNo = seqNo;
  }

  public String getCmrIssuingCntry() {
    return cmrIssuingCntry;
  }

  public void setCmrIssuingCntry(String cmrIssuingCntry) {
    this.cmrIssuingCntry = cmrIssuingCntry;
  }

  public String getCmrNo() {
    return cmrNo;
  }

  public void setCmrNo(String cmrNo) {
    this.cmrNo = cmrNo;
  }

  public String getCreatedTsStr() {
    return createdTsStr;
  }

  public void setCreatedTsStr(String createdTsStr) {
    this.createdTsStr = createdTsStr;
  }

  public String getUpdatedTsStr() {
    return updatedTsStr;
  }

  public void setUpdatedTsStr(String updatedTsStr) {
    this.updatedTsStr = updatedTsStr;
  }

  public String getCodCondition() {
    return codCondition;
  }

  public void setCodCondition(String codCondition) {
    this.codCondition = codCondition;
  }

  public String getCodRsn() {
    return codRsn;
  }

  public void setCodRsn(String codRsn) {
    this.codRsn = codRsn;
  }

  public Date getCodEffDate() {
    return codEffDate;
  }

  public void setCodEffDate(Date codEffDate) {
    this.codEffDate = codEffDate;
  }

  public String getProcStatus() {
    return procStatus;
  }

  public void setProcStatus(String procStatus) {
    this.procStatus = procStatus;
  }

  public String getProcMsg() {
    return procMsg;
  }

  public void setProcMsg(String procMsg) {
    this.procMsg = procMsg;
  }

  public String getCreatedBy() {
    return createdBy;
  }

  public void setCreatedBy(String createdBy) {
    this.createdBy = createdBy;
  }

  public Date getCreateDt() {
    return createDt;
  }

  public void setCreateDt(Date createDt) {
    this.createDt = createDt;
  }

  public String getUpdatedBy() {
    return updatedBy;
  }

  public void setUpdatedBy(String updatedBy) {
    this.updatedBy = updatedBy;
  }

  public Date getUpdateDt() {
    return updateDt;
  }

  public void setUpdateDt(Date updateDt) {
    this.updateDt = updateDt;
  }

  public String getKatr10() {
    return katr10;
  }

  public void setKatr10(String katr10) {
    this.katr10 = katr10;
  }

  @Override
  public boolean allKeysAssigned() {
    boolean blnAllKeys = false;
    blnAllKeys = !StringUtils.isEmpty(this.sourceName) && !StringUtils.isEmpty(this.seqNo + "") && !StringUtils.isEmpty(this.cmrIssuingCntry)
        && !StringUtils.isEmpty(this.cmrNo);

    return blnAllKeys;
  }

  @Override
  public String getRecordDescription() {
    return "GCARS UPDT QUEUE Entry";
  }

  @Override
  public void addKeyParameters(ModelAndView mv) {
  }

  @Override
  public void addKeyParameters(ModelMap map) {
  }
}
