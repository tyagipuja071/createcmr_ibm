/**
 * 
 */
package com.ibm.cio.cmr.request.ui;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Helper for PageManager for field configurability.
 * 
 * @author Jeffrey Zamora
 * 
 */
public class PageConfig implements Serializable {

  private static final long serialVersionUID = 1L;
  private Map<String, String> fieldMap = new HashMap<String, String>();
  private Map<String, String> dropdowns = new HashMap<String, String>();
  private Map<String, String> tabs = new HashMap<String, String>();

  public Map<String, String> getFieldMap() {
    return fieldMap;
  }

  public void setFieldMap(Map<String, String> fieldMap) {
    this.fieldMap = fieldMap;
  }

  public Map<String, String> getDropdowns() {
    return dropdowns;
  }

  public void setDropdowns(Map<String, String> dropdowns) {
    this.dropdowns = dropdowns;
  }

  public Map<String, String> getTabs() {
    return tabs;
  }

  public void setTabs(Map<String, String> tabs) {
    this.tabs = tabs;
  }

}
