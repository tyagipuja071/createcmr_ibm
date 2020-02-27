/**
 * 
 */
package com.ibm.cmr.create.batch.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import org.apache.commons.lang.StringUtils;

/**
 * Retrieves properties from batch-props.properties<br>
 * Contains other utility functions for the batch
 * 
 * @author Jeffrey Zamora
 * 
 */
public class BatchUtil {

  private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("batch-props");

  /**
   * Gets a property from batch-props.properties
   * 
   * @param key
   * @return
   */
  public static String getProperty(String key) {
    if (BUNDLE.containsKey(key)) {
      return BUNDLE.getString(key);
    }
    return null;
  }

  /**
   * Gets the application ID to use when connecting to the services for the
   * specific CMR issuing country
   * 
   * @param cmrIssuingCntry
   * @return
   */
  public static String getAppId(String cmrIssuingCntry) {
    return getProperty("appId." + cmrIssuingCntry);
  }

  /**
   * Returns the list of keys on the property file that starts with the given
   * prefix
   * 
   * @param keySuffix
   * @return
   */
  public static List<String> getKeysWithPrefix(String keyPrefix) {
    List<String> keys = new ArrayList<String>();
    for (String key : BUNDLE.keySet()) {
      if (key.startsWith(keyPrefix)) {
        keys.add(key);
      }
    }
    return keys;
  }

  public static List<String> getAsList(String key) {
    String prop = getProperty(key);
    if (!StringUtils.isEmpty(prop)) {
      return Arrays.asList(prop.split(","));
    }
    return new ArrayList<>();
  }

}
