/**
 * 
 */
package com.ibm.cio.cmr.request.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Container for Keys
 * 
 * @author Jeffrey Zamora
 * 
 */
public class KeyContainer {

  private BaseModel model;
  private Map<String, String> keys;

  public KeyContainer(BaseModel model) {
    this.model = model;
    this.keys = new HashMap<String, String>();
  }

  public void parse(String values) {
    String[] valuePairs = values.split("&");
    String[] pair = null;
    for (String value : valuePairs) {
      pair = value.split("=");
      this.keys.put(pair[0], pair.length > 1 ? pair[1] : null);
    }
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("[");
    int cnt = 0;
    for (String key : this.keys.keySet()) {
      sb.append(cnt > 0 ? ", " : "");
      sb.append(key).append(" = ").append(getKey(key));
      cnt++;
    }
    sb.append("");
    return sb.toString();
  }

  public String getKey(String name) {
    return this.keys.get(name);
  }

  public BaseModel getModel() {
    return model;
  }

  public void setModel(BaseModel model) {
    this.model = model;
  }
}
