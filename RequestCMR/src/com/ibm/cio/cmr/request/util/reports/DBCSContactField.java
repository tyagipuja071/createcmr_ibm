/**
 * 
 */
package com.ibm.cio.cmr.request.util.reports;

/**
 * @author JeffreyAZamora
 *
 */
public class DBCSContactField extends DBCSReportField {

  /**
   * @param name
   * @param length
   */
  public DBCSContactField(String name, int length) {
    super(name, length);

  }

  @Override
  public String getFixedWidthValue(Object value) {
    String fwValue = super.getFixedWidthValue(value);
    if (fwValue.trim().contains("ご担当者")) {
      System.err.println("person in charge");
      return "";
    }
    return fwValue;
  }

}
