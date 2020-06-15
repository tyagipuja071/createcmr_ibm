/**
 * 
 */
package com.ibm.cio.cmr.request.query;

import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.util.ConfigUtil;

/**
 * Utility class to get the sql from an external source.
 * 
 * @author Jeffrey Zamora
 * 
 */
public class ExternalizedQuery {

  private static Properties queryBundle = new Properties();
  private static final Logger LOG = Logger.getLogger(ExternalizedQuery.class);

  private ExternalizedQuery() {
    // private constructor
  }

  /**
   * Gets the external SQL for the given sql key
   * 
   * @param sqlKey
   * @return
   */
  public static String getSql(String sqlKey) {
    return getSql(sqlKey, null);
  }

  /**
   * Gets the external SQL for the given sql key
   * 
   * @param sqlKey
   * @return
   */
  public static String getSql(String sqlKey, String schemaToUse) {
    LOG.trace("SQL Key: " + sqlKey);
    String sql = queryBundle.getProperty(sqlKey);
    if (schemaToUse != null) {
      sql = StringUtils.replace(sql, "{SCHEMA}", schemaToUse);
    }
    return sql;
  }

  /**
   * Loads the sql with the given key
   * 
   * @param sqlKey
   */
  private static void load() throws Exception {
    InputStream is = ConfigUtil.getResourceStream("cmr-queries.properties");
    try {
      queryBundle.clear();
      queryBundle.load(is);
    } finally {
      is.close();
    }
  }

  /**
   * Refreshes the Queries
   * 
   * @throws Exception
   */
  public static void refresh() throws Exception {
    load();
  }
}
