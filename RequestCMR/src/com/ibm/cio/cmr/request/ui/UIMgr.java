/**
 * 
 */
package com.ibm.cio.cmr.request.ui;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;

import com.ibm.cio.cmr.request.util.ConfigUtil;

/**
 * Hanldes UI related stuff, from FieldConfig, FieldInfo to screen labels
 * 
 * @author Jeffrey Zamora
 * 
 */
public class UIMgr {

  private static Properties BUNDLE = new Properties();

  private static void load() throws Exception {
    InputStream is = ConfigUtil.getResourceStream("cmr-ui.properties");
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

  /**
   * Injects the UI labels to the request
   * 
   * @param request
   */
  @SuppressWarnings("unchecked")
  public static void inject(HttpServletRequest request) {
    try {
      HashMap<String, Object> map = new HashMap<String, Object>();
      HashMap<String, String> subMap = null;
      String skey = null;
      String mainKey = null;
      String subKey = null;
      for (Object key : BUNDLE.keySet()) {
        skey = (String) key;
        if (skey.indexOf(".") > 0) {
          mainKey = skey.substring(0, skey.indexOf("."));
          subKey = skey.substring(skey.indexOf(".") + 1);
          if (map.get(mainKey) == null) {
            map.put(mainKey, new HashMap<String, String>());
          }
          subMap = (HashMap<String, String>) map.get(mainKey);
          subMap.put(subKey, BUNDLE.getProperty(skey));
        } else {
          map.put(key.toString(), BUNDLE.getProperty(skey));
        }
      }
      request.setAttribute("ui", map);
    } catch (Exception e) {
      // noop
    }
  }

  /**
   * Gets the text associated with this key from cmr-ui.properties
   * 
   * @param key
   * @return
   */
  public static String getText(String key) {
    return BUNDLE.getProperty(key);
  }
}
