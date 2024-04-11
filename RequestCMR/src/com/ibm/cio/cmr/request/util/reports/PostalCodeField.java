/**
 * 
 */
package com.ibm.cio.cmr.request.util.reports;

/**
 * @author JeffreyAZamora
 *
 */
public class PostalCodeField extends ReportField {

  /**
   * @param name
   * @param length
   * @param padCharacter
   */
  public PostalCodeField(String name, int length, char padCharacter) {
    super(name, length, padCharacter);

  }

  /**
   * @param name
   * @param length
   */
  public PostalCodeField(String name, int length) {
    super(name, length);

  }

  @Override
  public String getFixedWidthValue(Object value) {
    String converted = super.getFixedWidthValue(value);

    converted = converted.replaceAll("[^0-9\\- ]", " ");
    return converted;
  }

}
