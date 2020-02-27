/**
 * 
 */
package com.ibm.cio.cmr.request.util.validator;

import com.ibm.swat.password.cwa2;

/**
 * @author Jeffrey Zamora
 * 
 */
public class BlueGroupValidator implements ParamValidator {

  @Override
  public boolean validate(String value) {
    cwa2 bpAPI = new cwa2();
    if (value == null || "".equals(value.trim())) {
      return true;
    }
    String[] blueGroups = value.split(",");
    for (String blueGroup : blueGroups) {
      if (!bpAPI.groupExist(blueGroup.trim())) {
        return false;
      }
    }
    return true;
  }

  @Override
  public String getErrorMessage() {
    return "One or more groups specified is/are not valid Blue Groups";
  }

}
