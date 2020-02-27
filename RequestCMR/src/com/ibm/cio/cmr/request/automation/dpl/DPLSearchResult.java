/**
 * 
 */
package com.ibm.cio.cmr.request.automation.dpl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.ibm.cio.cmr.request.automation.util.CommonWordsUtil;

/**
 * Contains the information about a DPL name search done against the DPL web
 * site
 * 
 * @author JeffZAMORA
 * 
 */
public class DPLSearchResult {

  private String name;
  private String user;
  private String date;
  private String resultId;
  private List<DPLSearchItem> items = new ArrayList<DPLSearchItem>();

  /**
   * Computes for the top matches
   * 
   * @return
   */
  public List<DPLSearchItem> getTopMatches() {
    DPLSearchItem topMatch = null;

    List<DPLSearchItem> topMatches = new ArrayList<DPLSearchItem>();
    int lowestDistance = Integer.MAX_VALUE;
    for (DPLSearchItem item : this.items) {
      int distance = StringUtils.getLevenshteinDistance(this.name.toUpperCase(), item.getPartyName().toUpperCase());
      item.setDistance(distance);
      if (distance < lowestDistance) {
        lowestDistance = distance;
        topMatch = item;
      }
      if (distance < 10) {
        topMatches.add(item);
      }
    }
    if (topMatches.isEmpty() && topMatch != null) {
      topMatches.add(topMatch);
    }
    Collections.sort(topMatches);
    if (topMatches.size() > 3) {
      topMatches = topMatches.subList(0, 2);
    }
    return topMatches;
  }

  /**
   * Searches for exact matches on the results
   * 
   * @return
   */
  public boolean exactMatchFound() {
    for (DPLSearchItem item : this.items) {
      if (item.getPartyName().toUpperCase().equals(this.name.toUpperCase())) {
        return true;
      }
    }
    return false;
  }

  /**
   * Searches for partial matches on the results
   * 
   * @return
   */
  public boolean partialMatchFound() {
    for (DPLSearchItem item : this.items) {
      if (item.getPartyName().toUpperCase().contains(this.name.toUpperCase())) {
        return true;
      }
      if (item.getPartyName().toUpperCase().contains(CommonWordsUtil.minimize(this.name.toUpperCase()))) {
        return true;
      }
    }
    return false;
  }

  public void addItem(DPLSearchItem item) {
    this.items.add(item);
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDate() {
    return date;
  }

  public void setDate(String date) {
    this.date = date;
  }

  public List<DPLSearchItem> getItems() {
    return items;
  }

  public void setItems(List<DPLSearchItem> items) {
    this.items = items;
  }

  public String getUser() {
    return user;
  }

  public void setUser(String user) {
    this.user = user;
  }

  public String getResultId() {
    return resultId;
  }

  public void setResultId(String resultId) {
    this.resultId = resultId;
  }

}
