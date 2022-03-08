package com.ibm.cio.cmr.request.util;

import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

public class SBOFilterUtil {

  private static Properties BUNDLE = new Properties();
  private static final String PREFIX = "sbo.";

  /**
   * Loads the properties
   * 
   * @param sqlKey
   */
  private static void load() throws Exception {
    InputStream is = ConfigUtil.getResourceStream("cmr-sbo-filters.properties");
    try {
      BUNDLE.clear();
      BUNDLE.load(is);

      System.out.println(BUNDLE);
    } finally {
      is.close();
    }
  }

  /**
   * Refreshes the Filters
   * 
   * @throws Exception
   */
  public static void refresh() throws Exception {
    load();
  }

  /**
   * 
   * Returns a combined list of sboFilters for provided issuingCountry
   * 
   * @param issuingCountry
   * @return
   */
  public static Set<String> getSBOFiltersForCountry(String issuingCountry) {
    try {
      Set<String> filterSet = new HashSet<>();
      String sboFilter = BUNDLE.getProperty(PREFIX + issuingCountry);
      if (StringUtils.isNotBlank(sboFilter)) {
        filterSet.addAll(Arrays.asList(sboFilter.split(",")));
      }
      return filterSet;
    } catch (Exception e) {
      return new HashSet<String>();
    }
  }

  /**
   * 
   * Returns a combined list of sboFilters for all countries
   * 
   * @param issuingCountry
   * @return
   */
  public static Set<String> getSBOFilter() {
    try {
      Set<String> filterSet = new HashSet<>();
      for (Object sboFilter : BUNDLE.values()) {
        String filter = sboFilter.toString();
        if (StringUtils.isNotBlank(filter)) {
          filterSet.addAll(Arrays.asList(filter.split(",")));
        }
      }
      return filterSet;
    } catch (Exception e) {
      return new HashSet<String>();
    }
  }

  /**
   * Generates IN query filter
   * 
   * @param filter
   * @return
   */
  public static String getSBOFilterQuery() {
    Set<String> filter = getSBOFilter();
    return "('" + StringUtils.join(filter, "','") + "')";
  }

  /**
   * Generates IN query filter for the country
   * 
   * @param filter
   * @return
   */
  public static String getSBOFilterQueryForCountry(String issuingCountry) {
    Set<String> filter = getSBOFiltersForCountry(issuingCountry);
    return "('" + StringUtils.join(filter, "','") + "')";
  }

  /**
   * Test driver
   * 
   * @param args
   */
  // public static void main(String[] args) {
  // try {
  // refresh();
  // System.out.println(getSBOFilterQuery());
  // } catch (Exception e) {
  // System.out.println(e);
  // }
  // }

}