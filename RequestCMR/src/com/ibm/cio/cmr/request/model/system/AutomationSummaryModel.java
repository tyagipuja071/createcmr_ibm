/**
 * 
 */
package com.ibm.cio.cmr.request.model.system;

import java.util.HashMap;
import java.util.Map;

/**
 * @author JeffZAMORA
 *
 */
public class AutomationSummaryModel {

  private String country;
  private long touchless;
  private long legacy;
  private long review;
  private long noStatus;

  private Map<String, Long> reviewMap = new HashMap<String, Long>();

  public String getCountry() {
    return country;
  }

  public void setCountry(String country) {
    this.country = country;
  }

  public long getTouchless() {
    return touchless;
  }

  public void setTouchless(long touchless) {
    this.touchless = touchless;
  }

  public long getLegacy() {
    return legacy;
  }

  public void setLegacy(long legacy) {
    this.legacy = legacy;
  }

  public long getReview() {
    return review;
  }

  public void setReview(long review) {
    this.review = review;
  }

  public Map<String, Long> getReviewMap() {
    return reviewMap;
  }

  public void setReviewMap(Map<String, Long> reviewMap) {
    this.reviewMap = reviewMap;
  }

  public long getNoStatus() {
    return noStatus;
  }

  public void setNoStatus(long noStatus) {
    this.noStatus = noStatus;
  }
}
