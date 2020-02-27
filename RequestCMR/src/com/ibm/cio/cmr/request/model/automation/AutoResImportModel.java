package com.ibm.cio.cmr.request.model.automation;

import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.cio.cmr.request.model.BaseModel;

/**
 * 
 */
public class AutoResImportModel extends BaseModel {

  private static final long serialVersionUID = 1L;

  private long automationResultId;

  private long itemNo;

  private long requestId;

  public long getItemNo() {
    return itemNo;
  }

  private String processCd;

  private String matchKeyName;

  private String matchKeyValue;

  private String matchGradeTyp;

  private String matchGradeValue;

  private String recordType;

  private String createBy;

  private Date createTs;

  private String importedIndc;

  @Override
  public boolean allKeysAssigned() {
    return this.automationResultId > 0 && !StringUtils.isBlank(this.processCd);
  }

  @Override
  public String getRecordDescription() {
    return "Automation Results";
  }

  @Override
  public void addKeyParameters(ModelAndView mv) {
  }

  @Override
  public void addKeyParameters(ModelMap map) {
  }

  public long getAutomationResultId() {
    return automationResultId;
  }

  public void setAutomationResultId(long automationResultId) {
    this.automationResultId = automationResultId;
  }

  public String getProcessCd() {
    return processCd;
  }

  public void setProcessCd(String processCd) {
    this.processCd = processCd;
  }

  public void setItemNo(long itemNo) {
    this.itemNo = itemNo;
  }

  public String getMatchKeyName() {
    return matchKeyName;
  }

  public void setMatchKeyName(String matchKeyName) {
    this.matchKeyName = matchKeyName;
  }

  public String getMatchKeyValue() {
    return matchKeyValue;
  }

  public void setMatchKeyValue(String matchKeyValue) {
    this.matchKeyValue = matchKeyValue;
  }

  public String getMatchGradeTyp() {
    return matchGradeTyp;
  }

  public void setMatchGradeTyp(String matchGradeTyp) {
    this.matchGradeTyp = matchGradeTyp;
  }

  public String getMatchGradeValue() {
    return matchGradeValue;
  }

  public void setMatchGradeValue(String matchGradeValue) {
    this.matchGradeValue = matchGradeValue;
  }

  public String getRecordType() {
    return recordType;
  }

  public void setRecordType(String recordType) {
    this.recordType = recordType;
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

  public String getImportedIndc() {
    return importedIndc;
  }

  public void setImportedIndc(String importedIndc) {
    this.importedIndc = importedIndc;
  }

  public long getRequestId() {
    return requestId;
  }

  public void setRequestId(long requestId) {
    this.requestId = requestId;
  }

}
