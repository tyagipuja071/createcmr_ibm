/**
 * 
 */
package com.ibm.cio.cmr.request.model.dashboard;

import java.util.List;

import com.ibm.cio.cmr.request.entity.dashboard.ProcessingMonitor;

/**
 * @author 136786PH1
 *
 */
public class DashboardResult {

  private String overallStatus;
  private String alertMessage;

  private List<ProcessingMonitor> processingRecords;
  private ProcessingModel processing;

  public String getOverallStatus() {
    return overallStatus;
  }

  public void setOverallStatus(String overallStatus) {
    this.overallStatus = overallStatus;
  }

  public List<ProcessingMonitor> getProcessingRecords() {
    return processingRecords;
  }

  public void setProcessingRecords(List<ProcessingMonitor> processingRecords) {
    this.processingRecords = processingRecords;
  }

  public String getAlertMessage() {
    return alertMessage;
  }

  public void setAlertMessage(String alertMessage) {
    this.alertMessage = alertMessage;
  }

  public ProcessingModel getProcessing() {
    return processing;
  }

  public void setProcessing(ProcessingModel processing) {
    this.processing = processing;
  }

}
