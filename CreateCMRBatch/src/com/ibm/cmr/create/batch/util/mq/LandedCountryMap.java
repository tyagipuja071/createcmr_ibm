/**
 * 
 */
package com.ibm.cmr.create.batch.util.mq;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;

/**
 * Stores Code + Description for Landed Countries
 * 
 * @author Jeffrey Zamora
 * 
 */
public class LandedCountryMap {

  private static Logger LOG = Logger.getLogger(LandedCountryMap.class);
  private static Map<String, String> countryMap = new HashMap<String, String>();
  private static Map<String, String> countryLovMap = new HashMap<String, String>();
  private static Map<String, String> countryLocalMap = new HashMap<String, String>();
  private static Map<String, String> sysLocMap = new HashMap<String, String>();
  private static Map<String, String> cusLocMap4L = new HashMap<String, String>();
  private static Map<String, String> cusLocMap4C = new HashMap<String, String>();

  public static EntityManager entityManager;

  public static void init(EntityManager entityManager) {
    countryMap.clear();
    countryLovMap.clear();
    countryLocalMap.clear();
    sysLocMap.clear();

    LandedCountryMap.entityManager = entityManager;

    String sql = ExternalizedQuery.getSql("LANDED_COUNTRY_MAP.INIT");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    List<Object[]> results = query.getResults();
    for (Object[] result : results) {
      countryMap.put((String) result[0], (String) result[1]);
    }

    String sqlLov = ExternalizedQuery.getSql("LANDED_COUNTRY_LOV_MAP.INIT");
    PreparedQuery querylov = new PreparedQuery(entityManager, sqlLov);
    List<Object[]> lovResults = querylov.getResults();
    for (Object[] result : lovResults) {
      countryLovMap.put((String) result[0], (String) result[1]);
    }

    sql = ExternalizedQuery.getSql("LANDED_COUNTRY_CEMEA_MAP.INIT");
    query = new PreparedQuery(entityManager, sql);
    results = query.getResults();
    for (Object[] result : results) {
      countryLocalMap.put((String) result[0], (String) result[1]);
    }

    sql = ExternalizedQuery.getSql("LANDED_COUNTRY_MAP.INIT_SYSLOC");
    query = new PreparedQuery(entityManager, sql);
    results = query.getResults();
    for (Object[] result : results) {
      sysLocMap.put((String) result[0], (String) result[1]);
    }

  }

  public static String getCountryName(String code) {
    String desc = countryMap.get(code);
    return desc == null ? code : desc;
  }

  public static String getLovCountryName(String code) {
    String desc = countryLovMap.get(code);
    return desc == null ? code : desc;
  }

  public static String getLocalCountryName(String code) {
    String desc = countryLocalMap.get(code);
    return desc == null ? getCountryName(code) : desc;
  }

  public static String getSysLocDescription(String code) {
    String desc = sysLocMap.get(code);
    return desc == null ? "" : desc;
  }

}
