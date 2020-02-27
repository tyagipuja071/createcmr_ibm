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
}
