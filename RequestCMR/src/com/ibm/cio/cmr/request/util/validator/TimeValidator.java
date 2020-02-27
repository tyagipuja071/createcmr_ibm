/**
 * 
 */
package com.ibm.cio.cmr.request.util.validator;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Jeffrey Zamora
 * 
 */
public class TimeValidator implements ParamValidator {

  private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm");

  @Override
  public boolean validate(String value) {
    try {
      Date dt = TIME_FORMAT.parse(value);
      String out = TIME_FORMAT.format(dt);
      return value.equals(out);
    } catch (Exception e) {
      return false;
    }
  }

  @Override
  public String getErrorMessage() {
    // TODO Auto-generated method stub
    return null;
  }

}
