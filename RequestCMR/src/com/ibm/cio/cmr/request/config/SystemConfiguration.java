package com.ibm.cio.cmr.request.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.xml.sax.SAXException;

import com.ibm.cio.cmr.request.user.AppUser;
import com.ibm.cio.cmr.request.util.ConfigUtil;
import com.ibm.cio.cmr.request.util.EnvUtil;

/**
 * Class for handling system configuration
 *
 * @author Jeffrey Zamora
 *
 */
public class SystemConfiguration {

  public static String LAST_REFRESH_TIME = null;

  private static Map<String, ConfigItem> configurations = new HashMap<String, ConfigItem>();

  private static final ResourceBundle CONFIG = ResourceBundle.getBundle("cmr-config");
  private static final ResourceBundle BUILD_NO = ResourceBundle.getBundle("build-no");

  public static File dirLocation = null;

  /**
   * Loads the config from the configuration file
   * 
   * @throws SAXException
   * @throws IOException
   * @throws IllegalAccessException
   * @throws IllegalArgumentException
   * 
   * @throws Exception
   */
  private static void load() throws IOException, SAXException, IllegalArgumentException, IllegalAccessException {
    configurations.clear();
    ConfigItemDigester digester = new ConfigItemDigester();

    InputStream is = ConfigUtil.getResourceStream(CONFIG.getString("CONFIG_FILE"));

    try {
      @SuppressWarnings("unchecked")
      List<ConfigItem> items = (List<ConfigItem>) digester.parse(is);
      for (ConfigItem item : items) {
        EnvUtil.injectEnvVariables(item);
        configurations.put(item.getId(), item);
      }

      // URL url = new URL(ConfigUtil.getConfigDir());

      File configFile = new File(ConfigUtil.getConfigDir());
      // File parentDir = configFile.getParentFile();
      dirLocation = configFile;
      // System.out.println("Config Location = " + dirLocation);
    } finally {
      is.close();
    }

  }

  /**
   * Updates a configuration value
   *
   * @param key
   * @param value
   */
  public static void update(String key, String value) {
    ConfigItem item = getParameter(key);
    if (item == null) {
      return;
    }
    item.setValue(value);
  }

  /**
   * Exports the current configuration to the physical file
   *
   * @return
   * @throws IOException
   */
  public static boolean export() throws IOException {
    if (dirLocation == null) {
      return false;
    }
    FileOutputStream fos = new FileOutputStream(dirLocation.getAbsolutePath() + "/" + CONFIG.getString("CONFIG_FILE"));
    try {
      PrintWriter pw = new PrintWriter(fos);
      try {
        pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
        pw.println("<!-- Configuration File for the CMR Application -->");
        pw.println("<config>");
        List<ConfigItem> items = new ArrayList<ConfigItem>();
        items.addAll(asList());
        Collections.sort(items);
        for (ConfigItem item : items) {
          pw.println("  <item>");
          ;
          pw.println("    <id>" + item.getId() + "</id>");
          pw.println("    <order>" + item.getOrder() + "</order>");
          pw.println("    <name>" + item.getName() + "</name>");
          pw.println("    <description>" + item.getDescription() + "</description>");
          pw.println("    <type>" + item.getType() + "</type>");
          pw.println("    <editable>" + item.isEditable() + "</editable>");
          if (item.getHint() != null) {
            pw.println("    <hint>" + item.getHint() + "</hint>");
          }
          pw.println("    <required>" + item.isRequired() + "</required>");
          pw.println("    <editType>" + item.getEditType() + "</editType>");
          String value = item.getValue();
          value = value.replaceAll("&", "&amp;");
          value = value.replaceAll("<", "&lt;");
          value = value.replaceAll(">", "&gt;");
          pw.println("    <value>" + value + "</value>");
          ;
          pw.println("  </item>");
          ;
        }
        pw.println("</config>");
      } finally {
        pw.close();
      }
    } finally {
      fos.close();
    }
    return true;

  }

  /**
   * Checks if the given user currently in the session is an administrator
   *
   * @param request
   * @return
   */
  public static boolean isAdmin(HttpServletRequest request) {
    AppUser user = AppUser.getUser(request);
    if (user == null) {
      return false;
    }
    return user.isAdmin();
  }

  /**
   * Gets a configuration param
   *
   * @param key
   * @return
   */
  public static ConfigItem getParameter(String key) {
    return configurations.get(key);
  }

  /**
   * Gets a value from the internal system configuration
   *
   * @param key
   * @return
   */
  public static String getSystemProperty(String key) {
    String value = System.getProperty(key);
    if (!StringUtils.isBlank(value)) {
      System.out.println("Property " + key + " retrieved from System properties.");
      return value;
    }
    if (BUILD_NO.containsKey(key)) {
      return BUILD_NO.getString(key);
    }
    try {
      return CONFIG.getString(key);
    } catch (MissingResourceException e) {
      return null;
    }
  }

  /**
   * Gets the value of a configuration parameter
   *
   * @param key
   * @param defaultValue
   * @return
   */
  public static String getValue(String key, String defaultValue) {
    ConfigItem item = configurations.get(key);
    return item != null ? item.getValue() : defaultValue;
  }

  /**
   * Gets the value of a configuration parameter
   *
   * @param key
   * @param defaultValue
   * @return
   */
  public static String getValue(String key) {
    ConfigItem item = configurations.get(key);
    return item != null ? item.getValue() : null;
  }

  /**
   * Refreshes the parameters
   *
   * @throws Exception
   */
  public static void refresh() throws Exception {
    load();
  }

  /**
   * Gets the {@link ConfigItem} list from the parameters
   *
   * @return
   */
  public static Collection<ConfigItem> asList() {
    return configurations.values();
  }

  public static void download(OutputStream os, String name) throws IOException {
    for (File config : dirLocation.listFiles()) {
      if (config.getName().equalsIgnoreCase(name)) {
        FileInputStream fis = new FileInputStream(config);
        try {
          IOUtils.copy(fis, os);
        } finally {
          fis.close();
        }
      }
    }
  }

  public static List<String> getAsList(String key) {
    String prop = getSystemProperty(key);
    if (!StringUtils.isEmpty(prop)) {
      return Arrays.asList(prop.split(","));
    }
    return new ArrayList<>();
  }

}
