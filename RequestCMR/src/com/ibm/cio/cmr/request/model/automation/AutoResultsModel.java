/**
 * 
 */
package com.ibm.cio.cmr.request.model.automation;

import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.cio.cmr.request.model.BaseModel;

/**
 * @author JeffZAMORA
 * 
 */
public class AutoResultsModel extends BaseModel {

  private static final long serialVersionUID = 1L;

  private long automationResultId;

  private long reqId;

  private String processCd;

  private String processTyp;

  private String processDesc;

  private String processResult;

  private String detailedResults;

  private String createBy;

  private Date createTs;

  private String matchImport;

  private String overrideImport;

  private String failureIndc;

  @Override
  public boolean allKeysAssigned() {
    return this.automationResultId > 0 && this.reqId > 0 && !StringUtils.isBlank(this.processCd);
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

  public long getReqId() {
    return reqId;
  }

  public void setReqId(long reqId) {
    this.reqId = reqId;
  }

  public String getProcessCd() {
    return processCd;
  }

  public void setProcessCd(String processCd) {
    this.processCd = processCd;
  }

  public String getProcessTyp() {
    return processTyp;
  }

  public void setProcessTyp(String processTyp) {
    this.processTyp = processTyp;
  }

  public String getProcessDesc() {
    return processDesc;
  }

  public void setProcessDesc(String processDesc) {
    this.processDesc = processDesc;
  }

  public String getProcessResult() {
    return processResult;
  }

  public void setProcessResult(String processResult) {
    this.processResult = processResult;
  }

  public String getDetailedResults() {
    return detailedResults;
  }

  public void setDetailedResults(String detailedResults) {
    this.detailedResults = detailedResults;
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

  public String getMatchImport() {
    return matchImport;
  }

  public void setMatchImport(String matchImport) {
    this.matchImport = matchImport;
  }

  public String getOverrideImport() {
    return overrideImport;
  }

  public void setOverrideImport(String overrideImport) {
    this.overrideImport = overrideImport;
  }

  public String getFailureIndc() {
    return failureIndc;
  }

  public void setFailureIndc(String failureIndc) {
    this.failureIndc = failureIndc;
  }

}
