/**
 * 
 */
package com.ibm.cio.cmr.request.util.validator;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Validator for type dateformat
 * 
 * @author Jeffrey Zamora
 * 
 */
public class DateFormatValidator implements ParamValidator {

  @Override
  public boolean validate(String value) {
    try {
      SimpleDateFormat format = new SimpleDateFormat(value);
      format.format(new Date());
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  @Override
  public String getErrorMessage() {
    return null;
  }

}
