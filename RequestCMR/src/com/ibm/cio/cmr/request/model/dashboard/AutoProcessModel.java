/**
 * 
 */
package com.ibm.cio.cmr.request.model.dashboard;

import java.util.HashMap;
import java.util.Map;

/**
 * @author 136786PH1
 *
 */
public class AutoProcessModel {

  private String automationStatus;
  private String alert;
  private long pendingThreshold;
  private long processTimeThreshold;
  private long manualPctThreshold;
  private long tatThreshold;
  private long allPending;
  private long totalRecords;
  private Map<String, CountryAutoStats> countryStats = new HashMap<String, CountryAutoStats>();

  public String getAutomationStatus() {
    return automationStatus;
  }

  public void setAutomationStatus(String automationStatus) {
    this.automationStatus = automationStatus;
  }

  public Map<String, CountryAutoStats> getCountryStats() {
    return countryStats;
  }

  public void setCountryStats(Map<String, CountryAutoStats> countryStats) {
    this.countryStats = countryStats;
  }

  public String getAlert() {
    return alert;
  }

  public void setAlert(String alert) {
    this.alert = alert;
  }

  public long getPendingThreshold() {
    return pendingThreshold;
  }

  public void setPendingThreshold(long pendingThreshold) {
    this.pendingThreshold = pendingThreshold;
  }

  public long getProcessTimeThreshold() {
    return processTimeThreshold;
  }

  public void setProcessTimeThreshold(long processTimeThreshold) {
    this.processTimeThreshold = processTimeThreshold;
  }

  public long getManualPctThreshold() {
    return manualPctThreshold;
  }

  public void setManualPctThreshold(long manualPctThreshold) {
    this.manualPctThreshold = manualPctThreshold;
  }

  public long getTatThreshold() {
    return tatThreshold;
  }

  public void setTatThreshold(long tatThreshold) {
    this.tatThreshold = tatThreshold;
  }

  public long getAllPending() {
    return allPending;
  }

  public void setAllPending(long allPending) {
    this.allPending = allPending;
  }

  public long getTotalRecords() {
    return totalRecords;
  }

  public void setTotalRecords(long totalRecords) {
    this.totalRecords = totalRecords;
  }

}
