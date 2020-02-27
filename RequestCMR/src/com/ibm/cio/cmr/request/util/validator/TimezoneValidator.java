/**
 * 
 */
package com.ibm.cio.cmr.request.util.validator;

import java.util.TimeZone;

/**
 * @author Jeffrey Zamora
 * 
 */
public class TimezoneValidator implements ParamValidator {

  @Override
  public boolean validate(String value) {
    try {
      TimeZone tz = TimeZone.getTimeZone(value);
      return tz != null;
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
