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
    int targetLength = this.length - 1;
    String fwValue = value == null ? "" : value.toString().trim();
    fwValue = convert2DBCS(fwValue);
    if ("*".equals(fwValue) || "**".equals(fwValue) || "***".equals(fwValue)) {
      fwValue = "＊＊＊"; // force to use DBCS asterisk
    }
    int toLength = (targetLength / 2) * 3;
    try {
      while (fwValue.getBytes("UTF-8").length < toLength) {
        fwValue += this.padCharacter;
      }
    } catch (UnsupportedEncodingException e) {
      // noop
    }
    fwValue += "!";
    return fwValue;
  }

  private String convert2DBCS(String value) {
    String modifiedVal = value;
    if (value != null && value.length() > 0) {
      modifiedVal = value;
      modifiedVal = modifiedVal.replaceAll("1", "１");
      modifiedVal = modifiedVal.replaceAll("2", "２");
      modifiedVal = modifiedVal.replaceAll("3", "３");
      modifiedVal = modifiedVal.replaceAll("4", "４");
      modifiedVal = modifiedVal.replaceAll("5", "５");
      modifiedVal = modifiedVal.replaceAll("6", "６");
      modifiedVal = modifiedVal.replaceAll("7", "７");
      modifiedVal = modifiedVal.replaceAll("8", "８");
      modifiedVal = modifiedVal.replaceAll("9", "９");
      modifiedVal = modifiedVal.replaceAll("0", "０");
      modifiedVal = modifiedVal.replaceAll("A", "Ａ");
      modifiedVal = modifiedVal.replaceAll("B", "Ｂ");
      modifiedVal = modifiedVal.replaceAll("C", "Ｃ");
      modifiedVal = modifiedVal.replaceAll("D", "Ｄ");
      modifiedVal = modifiedVal.replaceAll("E", "Ｅ");
      modifiedVal = modifiedVal.replaceAll("F", "Ｆ");
      modifiedVal = modifiedVal.replaceAll("G", "Ｇ");
      modifiedVal = modifiedVal.replaceAll("H", "Ｈ");
      modifiedVal = modifiedVal.replaceAll("I", "Ｉ");
      modifiedVal = modifiedVal.replaceAll("J", "Ｊ");
      modifiedVal = modifiedVal.replaceAll("K", "Ｋ");
      modifiedVal = modifiedVal.replaceAll("L", "Ｌ");
      modifiedVal = modifiedVal.replaceAll("M", "Ｍ");
      modifiedVal = modifiedVal.replaceAll("N", "Ｎ");
      modifiedVal = modifiedVal.replaceAll("O", "Ｏ");
      modifiedVal = modifiedVal.replaceAll("О", "Ｏ");
      modifiedVal = modifiedVal.replaceAll("P", "Ｐ");
      modifiedVal = modifiedVal.replaceAll("Q", "Ｑ");
      modifiedVal = modifiedVal.replaceAll("R", "Ｒ");
      modifiedVal = modifiedVal.replaceAll("S", "Ｓ");
      modifiedVal = modifiedVal.replaceAll("T", "Ｔ");
      modifiedVal = modifiedVal.replaceAll("U", "Ｕ");
      modifiedVal = modifiedVal.replaceAll("V", "Ｖ");
      modifiedVal = modifiedVal.replaceAll("W", "Ｗ");
      modifiedVal = modifiedVal.replaceAll("X", "Ｘ");
      modifiedVal = modifiedVal.replaceAll("Y", "Ｙ");
      modifiedVal = modifiedVal.replaceAll("Z", "Ｚ");
      modifiedVal = modifiedVal.replaceAll(" ", "　");
      modifiedVal = modifiedVal.replaceAll("-", "－");
      modifiedVal = modifiedVal.replaceAll("−", "－");
    }
    return modifiedVal;
  }

}
