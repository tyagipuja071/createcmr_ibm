/**
 * 
 */
package com.ibm.cio.cmr.request.model.system;

/**
 * @author JeffZAMORA
 *
 */
public class RequesterStatsModel {

  private String requesterId;
  private String requesterName;
  private String country;
  private String countryName;
  private long noIssueCount;
  private long rejectedCount;
  private long totalRejections;
  private long openCount;

  public String getRequesterId() {
    return requesterId;
  }

  public void setRequesterId(String requesterId) {
    this.requesterId = requesterId;
  }

  public String getCountry() {
    return country;
  }

  public void setCountry(String country) {
    this.country = country;
  }

  public long getNoIssueCount() {
    return noIssueCount;
  }

  public void setNoIssueCount(long noIssueCount) {
    this.noIssueCount = noIssueCount;
  }

  public long getRejectedCount() {
    return rejectedCount;
  }

  public void setRejectedCount(long rejectedCount) {
    this.rejectedCount = rejectedCount;
  }

  public long getOpenCount() {
    return openCount;
  }

  public void setOpenCount(long openCount) {
    this.openCount = openCount;
  }

  public String getRequesterName() {
    return requesterName;
  }

  public void setRequesterName(String requesterName) {
    this.requesterName = requesterName;
  }

  public String getCountryName() {
    return countryName;
  }

  public void setCountryName(String countryName) {
    this.countryName = countryName;
  }

  public long getTotalRejections() {
    return totalRejections;
  }

  public void setTotalRejections(long totalRejections) {
    this.totalRejections = totalRejections;
  }

}
