/**
 * 
 */
package com.ibm.cio.cmr.request.util.reports;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;

/**
 * Represents a report field that handles Date display written in the specified
 * format
 * 
 * @author 136786PH1
 *
 */
public class DateTimeReportField extends ReportField {

  private SimpleDateFormat format;

  /**
   * @param length
   */
  public DateTimeReportField(String name, int length, char padCharacter, String dateFormat) {
    super(name, length, padCharacter);
    this.format = new SimpleDateFormat(dateFormat);

  }

  public DateTimeReportField(String name, int length, String dateFormat) {
    this(name, length, ' ', dateFormat);
  }

  /**
   * Returns the date value to be written on the report, padded according to the
   * specified length
   * 
   * @param value
   * @return
   */
  @Override
  public String getFixedWidthValue(Object value) {
    String fwValue = null;
    if (value != null && !(value instanceof Date)) {
      fwValue = value.toString();
    } else {
      fwValue = value == null ? "" : this.format.format(value);
    }
    return StringUtils.rightPad(fwValue, this.length, ' ');
  }
}
