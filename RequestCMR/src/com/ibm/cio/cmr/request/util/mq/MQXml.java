/**
 * 
 */
package com.ibm.cio.cmr.request.util.mq;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author JeffZAMORA
 * 
 */
public class MQXml implements Comparable<MQXml> {

  private String name;
  private String rootName;
  private Map<String, String> values = new LinkedHashMap<>();
  private boolean response;

  public void addValue(String key, String value) {
    this.values.put(key, value);
  }

  public Map<String, String> getValues() {
    return this.values;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public int compareTo(MQXml o) {
    if (o == null) {
      return -1;
    }
    return this.name.compareTo(o.name);
  }

  public boolean isResponse() {
    return response;
  }

  public void setResponse(boolean response) {
    this.response = response;
  }

  public String getRootName() {
    return rootName;
  }

  public void setRootName(String rootName) {
    this.rootName = rootName;
  }
}
