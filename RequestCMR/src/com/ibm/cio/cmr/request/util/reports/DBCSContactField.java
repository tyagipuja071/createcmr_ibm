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
    String sValue = value != null ? value.toString().trim() : "";
    if (sValue.trim().contains("ご担当者")) {
      sValue = "";
    }
    String fwValue = super.getFixedWidthValue(sValue);
    return fwValue;
  }

}
