/**
 * 
 */
package com.ibm.cio.cmr.request.model.dashboard;

import java.util.ArrayList;
import java.util.List;

import com.ibm.cio.cmr.request.entity.dashboard.ProcessingMonitor;
import com.ibm.cio.cmr.request.model.DropdownItemModel;

/**
 * @author 136786PH1
 *
 */
public class DashboardResult {

  private String overallStatus;
  private String alertMessage;

  private List<ProcessingMonitor> processingRecords = new ArrayList<ProcessingMonitor>();
  private List<DropdownItemModel> countries = new ArrayList<DropdownItemModel>();
  private List<DropdownItemModel> procTypes = new ArrayList<DropdownItemModel>();
  private List<DropdownItemModel> partners = new ArrayList<DropdownItemModel>();

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

  public List<DropdownItemModel> getCountries() {
    return countries;
  }

  public void setCountries(List<DropdownItemModel> countries) {
    this.countries = countries;
  }

  public List<DropdownItemModel> getProcTypes() {
    return procTypes;
  }

  public void setProcTypes(List<DropdownItemModel> procTypes) {
    this.procTypes = procTypes;
  }

  public List<DropdownItemModel> getPartners() {
    return partners;
  }

  public void setPartners(List<DropdownItemModel> partners) {
    this.partners = partners;
  }

}
