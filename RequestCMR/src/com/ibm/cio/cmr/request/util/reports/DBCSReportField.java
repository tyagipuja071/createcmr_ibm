/**
 * 
 */
package com.ibm.cio.cmr.request.util.reports;

import java.io.UnsupportedEncodingException;

/**
 * Handles report fields containing DBCS characters
 * 
 * @author 136786PH1
 *
 */
public class DBCSReportField extends ReportField {

  /**
   * @param name
   * @param length
   * @param padCharacter
   */
  public DBCSReportField(String name, int length) {
    super(name, length, (char) 12288);
  }

  /**
   * Returns the value to be written on the report, padded according to the
   * specified length in DBCS characters
   * 
   * @param value
   * @return
   */
  @Override
  public String getFixedWidthValue(Object value) {
    String fwValue = value == null ? "" : value.toString().trim();
    int toLength = (this.length / 2) * 3;
    try {
      while (fwValue.getBytes("UTF-8").length < toLength) {
        fwValue += this.padCharacter;
      }
    } catch (UnsupportedEncodingException e) {
      // noop
    }
    return fwValue;
  }

}
