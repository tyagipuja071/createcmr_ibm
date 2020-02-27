/**
 * 
 */
package com.ibm.cio.cmr.request.util.sof;

/**
 * @author Jeffrey Zamora
 * 
 */
public class SOFAttribute {

  private String name;
  private String value;

  public SOFAttribute(String name, String value) {
    this.name = name;
    this.value = value;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }
}
