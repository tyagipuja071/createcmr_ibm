/**
 * 
 */
package com.ibm.cio.cmr.request.util.geo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Handles general market-related functions
 * 
 * @author JeffZAMORA
 *
 */
public class MarketUtil {

  private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("market");

  private static Map<String, List<String>> geos = new HashMap<String, List<String>>();
  private static Map<String, List<String>> markets = new HashMap<String, List<String>>();
  private static Map<String, String> countryGeoMap = new HashMap<String, String>();
  private static Map<String, String> countryMarketMap = new HashMap<String, String>();

  static {
    String geosList = BUNDLE.getString("GEOS");
    for (String geo : geosList.split(",")) {
      if (!geos.containsKey(geo)) {
        geos.put(geo, new ArrayList<String>());
      }

      String marketsList = BUNDLE.getString("MARKETS." + geo);
      for (String market : marketsList.split(",")) {
        geos.get(geo).add(market);
        if (!markets.containsKey(market)) {
          markets.put(market, new ArrayList<String>());
          String countryList = BUNDLE.getString("COUNTRIES." + market);
          for (String country : countryList.split(",")) {
            markets.get(market).add(country);
            countryGeoMap.put(country, geo);
            countryMarketMap.put(country, market);
          }
        }
      }
    }
  }

  /**
   * Gets the list of countries under this GEO
   * 
   * @param geo
   * @return
   */
  public static List<String> getCountriesForGeo(String geo) {
    List<String> countries = new ArrayList<String>();
    for (String market : geos.get(geo)) {
      for (String country : markets.get(market)) {
        countries.add(country);
      }
    }
    return countries;
  }

  /**
   * Gets the list of countries under this MARKET
   * 
   * @param market
   * @return
   */
  public static List<String> getCountriesForMarket(String market) {
    List<String> countries = new ArrayList<String>();
    for (String country : markets.get(market)) {
      countries.add(country);
    }
    return countries;
  }

  /**
   * Generates an SQL-compatible filter for countries belonging to the geo
   * 
   * @param geo
   * @return
   */
  public static String createCountryFilterForGeo(String geo) {
    StringBuilder sb = new StringBuilder();
    for (String country : getCountriesForGeo(geo)) {
      sb.append(sb.length() > 0 ? "," : "");
      sb.append("'" + country + "'");
    }
    return sb.toString();
  }

  /**
   * Generates an SQL-compatible filter for countries belonging to the market
   * 
   * @param market
   * @return
   */
  public static String createCountryFilterForMarket(String market) {
    StringBuilder sb = new StringBuilder();
    for (String country : getCountriesForMarket(market)) {
      sb.append(sb.length() > 0 ? "," : "");
      sb.append("'" + country + "'");
    }
    return sb.toString();
  }

  /**
   * Returns the GEO where the country belongs to
   * 
   * @param country
   * @return
   */
  public static String getGEO(String country) {
    return countryGeoMap.get(country);
  }

  /**
   * Returns the Market where the country belongs to
   * 
   * @param country
   * @return
   */
  public static String getMarket(String country) {
    return countryMarketMap.get(country);
  }

}
