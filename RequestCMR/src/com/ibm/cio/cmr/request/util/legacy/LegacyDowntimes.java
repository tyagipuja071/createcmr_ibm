package com.ibm.cio.cmr.request.util.legacy;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.TimeZone;

import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.util.ConfigUtil;

/**
 * Utility class that loads properties from legacy.properties and determines
 * whether a legacy system is up or not
 * 
 * @author 136786PH1
 *
 */
public class LegacyDowntimes {

  private static final Logger LOG = Logger.getLogger(LegacyDowntimes.class);

  private static Properties PROPS = new Properties();

  static {
    InputStream is = ConfigUtil.getResourceStream("legacy.properties");
    if (is != null) {
      try {
        PROPS.load(is);
        LOG.debug("Legacy properties loaded." + PROPS.size() + " properties found.");
      } catch (Exception e) {
        LOG.warn("Error in loading legacy.properties: " + e.getMessage());
      } finally {
        try {
          is.close();
        } catch (IOException e) {
          LOG.warn("Error in closing legacy.properties: " + e.getMessage());
        }
      }
    }
  }

  /**
   * Checks if the legacy system is up during the given time
   * 
   * @param country
   * @param currentTime
   * @return
   */
  public static boolean isUp(String country, Date currentTime) {
    if (!PROPS.containsKey(country + ".TIMEZONE")) {
      // LOG.trace("No timezone property for " + country);
      return true;
    }
    String tz = PROPS.getProperty(country + ".TIMEZONE");
    SimpleDateFormat formatter = new SimpleDateFormat("EEE");
    formatter.setTimeZone(TimeZone.getTimeZone(tz));
    String day = formatter.format(currentTime).toUpperCase();
    if (!PROPS.containsKey(country + "." + day + ".START") || !PROPS.containsKey(country + "." + day + ".END")) {
      LOG.trace("No start/end found for country " + country);
      return true;
    }
    formatter = new SimpleDateFormat("HHmm");
    formatter.setTimeZone(TimeZone.getTimeZone(tz));
    int curr = Integer.parseInt(formatter.format(currentTime));
    int start = Integer.parseInt(PROPS.getProperty(country + "." + day + ".START"));
    int end = Integer.parseInt(PROPS.getProperty(country + "." + day + ".END"));
    boolean up = curr >= start && curr <= end;
    LOG.debug("Country " + country + " up time: " + start + "-" + end + ", curr: " + currentTime + " [" + curr + " (" + (up) + ")]");
    return up;
  }

}
