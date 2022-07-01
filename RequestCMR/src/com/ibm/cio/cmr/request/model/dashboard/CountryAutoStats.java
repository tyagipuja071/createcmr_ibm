/**
 * 
 */
package com.ibm.cio.cmr.request.model.dashboard;

/**
 * @author 136786PH1
 *
 */
public class CountryAutoStats {

  private String completionAverage;
  private long completionAverageMin;
  private String fullAutoAverage;
  private long fullAutoOutlier;
  private String fullAutoPercentage;
  private long fullAutoAverageMin;

  private String automationAverage;
  private long automationOutlier;
  private long automationAverageMin;

  private String manualPercentage;
  private long currentQueue;
  private long total;
  private long reviews;
  private long completes;

  public long getCurrentQueue() {
    return currentQueue;
  }

  public void setCurrentQueue(long currentQueue) {
    this.currentQueue = currentQueue;
  }

  public long getFullAutoOutlier() {
    return fullAutoOutlier;
  }

  public void setFullAutoOutlier(long fullAutoOutlier) {
    this.fullAutoOutlier = fullAutoOutlier;
  }

  public long getAutomationOutlier() {
    return automationOutlier;
  }

  public void setAutomationOutlier(long automationOutlier) {
    this.automationOutlier = automationOutlier;
  }

  public String getManualPercentage() {
    return manualPercentage;
  }

  public void setManualPercentage(String manualPercentage) {
    this.manualPercentage = manualPercentage;
  }

  public String getFullAutoPercentage() {
    return fullAutoPercentage;
  }

  public void setFullAutoPercentage(String fullAutoPercentage) {
    this.fullAutoPercentage = fullAutoPercentage;
  }

  public String getCompletionAverage() {
    return completionAverage;
  }

  public void setCompletionAverage(String completionAverage) {
    this.completionAverage = completionAverage;
  }

  public String getFullAutoAverage() {
    return fullAutoAverage;
  }

  public void setFullAutoAverage(String fullAutoAverage) {
    this.fullAutoAverage = fullAutoAverage;
  }

  public String getAutomationAverage() {
    return automationAverage;
  }

  public void setAutomationAverage(String automationAverage) {
    this.automationAverage = automationAverage;
  }

  public long getCompletionAverageMin() {
    return completionAverageMin;
  }

  public void setCompletionAverageMin(long completionAverageMin) {
    this.completionAverageMin = completionAverageMin;
  }

  public long getFullAutoAverageMin() {
    return fullAutoAverageMin;
  }

  public void setFullAutoAverageMin(long fullAutoAverageMin) {
    this.fullAutoAverageMin = fullAutoAverageMin;
  }

  public long getAutomationAverageMin() {
    return automationAverageMin;
  }

  public void setAutomationAverageMin(long automationAverageMin) {
    this.automationAverageMin = automationAverageMin;
  }

  public long getTotal() {
    return total;
  }

  public void setTotal(long total) {
    this.total = total;
  }

  public long getReviews() {
    return reviews;
  }

  public void setReviews(long reviews) {
    this.reviews = reviews;
  }

  public long getCompletes() {
    return completes;
  }

  public void setCompletes(long completes) {
    this.completes = completes;
  }

}
