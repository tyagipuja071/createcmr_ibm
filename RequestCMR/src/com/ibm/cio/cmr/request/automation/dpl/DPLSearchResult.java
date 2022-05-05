/**
 * 
 */
package com.ibm.cio.cmr.request.automation.dpl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.ibm.cio.cmr.request.automation.util.CommonWordsUtil;
import com.ibm.cmr.services.client.dpl.DPLRecord;
import com.ibm.cmr.services.client.dpl.DPLSearchResults;

/**
 * Contains the information about a DPL name search done against the DPL web
 * site
 * 
 * @author JeffZAMORA
 * 
 */
public class DPLSearchResult {

  /**
   * Computes for the top matches
   * 
   * @return
   */
  public static List<DPLRecord> getTopMatches(DPLSearchResults results) {
    if (results == null || results.getDeniedPartyRecords() == null) {
      return Collections.emptyList();
    }
    DPLRecord topMatch = null;

    List<DPLRecord> topMatches = new ArrayList<DPLRecord>();
    int lowestDistance = Integer.MAX_VALUE;
    for (DPLRecord item : results.getDeniedPartyRecords()) {
      String dplName = item.getCompanyName();
      if (StringUtils.isBlank(dplName) && !StringUtils.isBlank(item.getCustomerLastName())) {
        dplName = item.getCustomerFirstName() + " " + item.getCustomerLastName();
      }
      if (dplName == null) {
        dplName = "";
      }
      int distance = StringUtils.getLevenshteinDistance(results.getSearchArgument().toUpperCase(), dplName.toUpperCase());
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
  public static boolean exactMatchFound(DPLSearchResults results) {
    if (results == null || results.getDeniedPartyRecords() == null) {
      return false;
    }
    for (DPLRecord item : results.getDeniedPartyRecords()) {
      String dplName = item.getCompanyName();
      if (StringUtils.isBlank(dplName) && !StringUtils.isBlank(item.getCustomerLastName())) {
        dplName = item.getCustomerFirstName() + " " + item.getCustomerLastName();
      }
      if (dplName == null) {
        dplName = "";
      }
      if (results.getSearchArgument().toUpperCase().equals(dplName.toUpperCase())) {
        return true;
      }
      if (dplName.toUpperCase().contains(results.getSearchArgument().toUpperCase())
          || results.getSearchArgument().toUpperCase().contains(dplName.toUpperCase())) {
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
  public static boolean partialMatchFound(DPLSearchResults results) {
    if (results == null || results.getDeniedPartyRecords() == null) {
      return false;
    }
    for (DPLRecord item : results.getDeniedPartyRecords()) {
      String dplName = item.getCompanyName();
      if (StringUtils.isBlank(dplName) && !StringUtils.isBlank(item.getCustomerLastName())) {
        dplName = item.getCustomerFirstName() + " " + item.getCustomerLastName();
      }
      if (dplName == null) {
        dplName = "";
      }
      if (dplName.toUpperCase().contains(results.getSearchArgument().toUpperCase())) {
        return true;
      }
      if (dplName.toUpperCase().contains(CommonWordsUtil.minimize(results.getSearchArgument().toUpperCase()))) {
        return true;
      }
    }
    return false;
  }

}
