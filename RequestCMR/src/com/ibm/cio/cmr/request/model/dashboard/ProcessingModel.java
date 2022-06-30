/**
 * 
 */
package com.ibm.cio.cmr.request.model.dashboard;

import java.util.Map;

/**
 * @author 136786PH1
 *
 */
public class ProcessingModel {

  private String processingStatus;
  private Map<String, Integer> pendingCounts;
  private Map<String, Long> minCounts;
  private Map<String, Long> maxCounts;
  private Map<String, Integer> stuckCounts;
  private Map<String, Integer> errorCounts;
  private int countsThreshold;
  private int minsThreshold;
  private int errorThreshold;
  private String alert;

  public String getProcessingStatus() {
    return processingStatus;
  }

  public void setProcessingStatus(String processingStatus) {
    this.processingStatus = processingStatus;
  }

  public Map<String, Integer> getPendingCounts() {
    return pendingCounts;
  }

  public void setPendingCounts(Map<String, Integer> pendingCounts) {
    this.pendingCounts = pendingCounts;
  }

  public Map<String, Long> getMinCounts() {
    return minCounts;
  }

  public void setMinCounts(Map<String, Long> minCounts) {
    this.minCounts = minCounts;
  }

  public Map<String, Long> getMaxCounts() {
    return maxCounts;
  }

  public void setMaxCounts(Map<String, Long> maxCounts) {
    this.maxCounts = maxCounts;
  }

  public Map<String, Integer> getStuckCounts() {
    return stuckCounts;
  }

  public void setStuckCounts(Map<String, Integer> stuckCounts) {
    this.stuckCounts = stuckCounts;
  }

  public String getAlert() {
    return alert;
  }

  public void setAlert(String alert) {
    this.alert = alert;
  }

  public int getMinsThreshold() {
    return minsThreshold;
  }

  public void setMinsThreshold(int minsThreshold) {
    this.minsThreshold = minsThreshold;
  }

  public int getCountsThreshold() {
    return countsThreshold;
  }

  public void setCountsThreshold(int countsThreshold) {
    this.countsThreshold = countsThreshold;
  }

  public int getErrorThreshold() {
    return errorThreshold;
  }

  public void setErrorThreshold(int errorThreshold) {
    this.errorThreshold = errorThreshold;
  }

  public Map<String, Integer> getErrorCounts() {
    return errorCounts;
  }

  public void setErrorCounts(Map<String, Integer> errorCounts) {
    this.errorCounts = errorCounts;
  }

}
