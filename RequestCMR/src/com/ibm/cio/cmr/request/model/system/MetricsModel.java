/**
 * 
 */
package com.ibm.cio.cmr.request.model.system;

/**
 * @author Jeffrey Zamora
 * 
 */
public class MetricsModel {

  private String dateFrom;
  private String dateTo;
  private String reportType;
  private String countType;
  private String groupByGeo;
  private String groupByProcCenter;
  private String country;
  private String reqType;

  private String excludeUnsubmitted;
  private String sourceSystId;
  private String excludeExternal;
  private String excludeChildRequests;

  public String getDateFrom() {
    return dateFrom;
  }

  public void setDateFrom(String dateFrom) {
    this.dateFrom = dateFrom;
  }

  public String getDateTo() {
    return dateTo;
  }

  public void setDateTo(String dateTo) {
    this.dateTo = dateTo;
  }

  public String getReportType() {
    return reportType;
  }

  public void setReportType(String reportType) {
    this.reportType = reportType;
  }

  public String getCountType() {
    return countType;
  }

  public void setCountType(String countType) {
    this.countType = countType;
  }

  public String getGroupByGeo() {
    return groupByGeo;
  }

  public void setGroupByGeo(String groupByGeo) {
    this.groupByGeo = groupByGeo;
  }

  public String getGroupByProcCenter() {
    return groupByProcCenter;
  }

  public void setGroupByProcCenter(String groupByProcCenter) {
    this.groupByProcCenter = groupByProcCenter;
  }

  public String getExcludeUnsubmitted() {
    return excludeUnsubmitted;
  }

  public void setExcludeUnsubmitted(String excludeUnsubmitted) {
    this.excludeUnsubmitted = excludeUnsubmitted;
  }

  public String getCountry() {
    return country;
  }

  public void setCountry(String country) {
    this.country = country;
  }

  public String getReqType() {
    return reqType;
  }

  public void setReqType(String reqType) {
    this.reqType = reqType;
  }

  public String getSourceSystId() {
    return sourceSystId;
  }

  public void setSourceSystId(String sourceSystId) {
    this.sourceSystId = sourceSystId;
  }

  public String getExcludeExternal() {
    return excludeExternal;
  }

  public void setExcludeExternal(String excludeExternal) {
    this.excludeExternal = excludeExternal;
  }

  public String getExcludeChildRequests() {
    return excludeChildRequests;
  }

  public void setExcludeChildRequests(String excludeChildRequests) {
    this.excludeChildRequests = excludeChildRequests;
  }

}
