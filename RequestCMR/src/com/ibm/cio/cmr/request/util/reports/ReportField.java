/**
 * 
 */
package com.ibm.cio.cmr.request.util.reports;

import org.apache.commons.lang3.StringUtils;

/**
 * Represents a report field that handles TEXT display
 * 
 * @author 136786PH1
 *
 */
public class ReportField {

  protected String name;

  protected int length;

  protected char padCharacter = ' ';

  public ReportField(String name, int length, char padCharacter) {
    this.name = name;
    this.length = length;
    this.padCharacter = padCharacter;
  }

  public ReportField(String name, int length) {
    this(name, length, ' ');
  }

  /**
   * Returns the value to be written on the report, padded according to the
   * specified length
   * 
   * @param value
   * @return
   */
  public String getFixedWidthValue(Object value) {
    String fwValue = value == null ? "" : value.toString().trim();
    return StringUtils.rightPad(fwValue, this.length, this.padCharacter);
  }

  public void setPadCharacter(char padCharacter) {
    this.padCharacter = padCharacter;
  }

  public String getName() {
    return name;
  }

  public int getLength() {
    return length;
  }

}
