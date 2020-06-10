/**
 * 
 */
package com.ibm.cio.cmr.request.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * Class that manages getting resources from external directories or from
 * classpath
 * 
 * @author JeffZAMORA
 *
 */
public class ConfigUtil {

  private static final Logger LOG = Logger.getLogger(ConfigUtil.class);

  private static String configDir = null;

  /**
   * Initiliazes the configuration directories
   */
  public static synchronized void init() {
    File configDirFile = null;

    String config = System.getenv("config_createcmr");
    if (!StringUtils.isBlank(config)) {
      configDirFile = new File(config);
      if (configDirFile.exists()) {
        System.out.println("Found app config dir at: " + config);
        configDir = config;
      }
    }
    if (configDir == null) {
      String ciwebconfig = System.getenv("ciwebconfig");
      if (!StringUtils.isBlank(ciwebconfig)) {
        configDirFile = new File(ciwebconfig);
        if (configDirFile.exists()) {
          System.out.println("Found ciwebconfig dir at: " + ciwebconfig);
          configDir = ciwebconfig;
        }
      }
    }
    if (configDir == null) {
      System.err.println("Error in getting configuration directory. No app directory or ciwebconfig directories defined on the system");
    }

  }

  /**
   * Returns the current configDir location
   * 
   * @return
   */
  public static String getConfigDir() {
    return configDir;
  }

  /**
   * Handles retrieval of the resources via the configuratoin direction found
   * 
   * @param resourceName
   * @return
   * @throws FileNotFoundException
   */
  public static InputStream getResourceStream(String resourceName) {
    File file = new File(configDir + File.separator + resourceName);
    if (!file.exists()) {
      // check secondary on default CP
      InputStream classPathResource = ConfigUtil.class.getClassLoader().getResourceAsStream(resourceName);
      if (classPathResource != null) {
        return classPathResource;
      }
      LOG.warn("Resource " + resourceName + " not found.");
      return null;
    }
    try {
      return new FileInputStream(file);
    } catch (FileNotFoundException e) {
      return null;
    }
  }
}
