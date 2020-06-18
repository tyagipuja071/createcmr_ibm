package com.ibm.cio.cmr.request.util;

import java.io.File;
import java.io.InputStream;
import java.util.Date;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;

public class MQProcessUtil {

  private static Properties BUNDLE = new Properties();
  private static final String GENERAL_VAL = "gen";
  public static final String ELEMENTS_DOC_NUM_KEY = "elements.d";
  public static final String MAPPING_KEY = "mapping";

  /**
   * Delete all files in the directory
   * 
   * @param sPath
   *          - the path of the directory
   * 
   * @return successful true��other false
   */
  public static boolean deleteFile(String sPath, int clean_period) {
    if (!sPath.endsWith(File.separator)) {
      sPath = sPath + File.separator;
    }
    File dirFile = new File(sPath);
    if (!dirFile.exists() || !dirFile.isDirectory()) {
      return false;
    }
    Boolean flag = true;
    File[] files = dirFile.listFiles();
    for (int i = 0; i < files.length; i++) {
      if (files[i].isFile()) {
        File file = new File(files[i].getAbsolutePath());
        if (file.isFile() && file.exists() && compareCurrentDate(new Date(file.lastModified()), clean_period)) {
          file.delete();
          flag = true;
        }
        if (!flag)
          break;
      }
    }
    if (!flag)
      return false;
    return flag;
  }

  private static boolean compareCurrentDate(Date fileDate, int clean_period) {

    Date currentDate = new Date();
    float diff = currentDate.getTime() - fileDate.getTime();
    float days = diff / (1000 * 60 * 60 * 24);
    if (days > clean_period)
      return true;
    return false;

  }

  private static void load() throws Exception {
    InputStream is = ConfigUtil.getResourceStream("cmr-mq.properties");
    try {
      BUNDLE.clear();
      BUNDLE.load(is);
    } finally {
      is.close();
    }
  }

  /**
   * Refreshes the Messages
   * 
   * @throws Exception
   */
  public static void refresh() throws Exception {
    load();
  }

  public static String getElements(String doc_num, String countryCd) {
    try {
      String elements = "";
      if (StringUtils.isEmpty(elements)) {
        elements = BUNDLE.getProperty(ELEMENTS_DOC_NUM_KEY + doc_num + "." + countryCd);
      }
      if (StringUtils.isEmpty(elements)) {
        elements = BUNDLE.getProperty(ELEMENTS_DOC_NUM_KEY + doc_num + "." + GENERAL_VAL);
      }
      if (StringUtils.isEmpty(elements)) {
        elements = BUNDLE.getProperty(ELEMENTS_DOC_NUM_KEY + "." + GENERAL_VAL);
      }
      return elements;
    } catch (Exception e) {
      return "";
    }
  }

  public static String getMappingValue(String countryCd, String MQElement) {
    try {
      String elements = BUNDLE.getProperty(MQElement + "." + countryCd + "." + MAPPING_KEY);
      if (StringUtils.isEmpty(elements)) {
        elements = BUNDLE.getProperty(MQElement + "." + MAPPING_KEY);
      }
      return elements;
    } catch (Exception e) {
      return "";
    }
  }

  public static String getItemValue(String item) {
    try {
      return BUNDLE.getProperty(item);
    } catch (Exception e) {
      return "";
    }
  }
}
