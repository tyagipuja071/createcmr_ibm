/**
 * 
 */
package com.ibm.cio.cmr.request.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Container for parameters in services. The class has a backing Map for the
 * params.
 * 
 * @author Jeffrey Zamora
 * 
 */
public class ParamContainer {

  private Map<String, Object> params = new HashMap<String, Object>();

  /**
   * Adds a parameter to the container
   * 
   * @param key
   * @param value
   */
  public void addParam(String key, Object value) {
    this.params.put(key, value);
  }

  /**
   * Gets a parameter from the container
   * 
   * @param key
   * @return
   */
  public Object getParam(String key) {
    return this.params.get(key);
  }

  /**
   * /** Gets the parameter names as list
   * 
   * @param excludeList
   *          parameter names that will not be part of the returned list
   * @return
   */
  public List<String> getParameterNames(String... excludeList) {
    List<String> names = new ArrayList<String>();
    names.addAll(this.params.keySet());
    Collections.sort(names);
    if (excludeList != null) {
      for (String excludeName : excludeList) {
        names.remove(excludeName);
      }
    }
    return names;
  }

  /**
   * Gets the parameter names as list
   * 
   * @return
   */
  public List<String> getParameterNames() {
    return getParameterNames((String[]) null);
  }

}
