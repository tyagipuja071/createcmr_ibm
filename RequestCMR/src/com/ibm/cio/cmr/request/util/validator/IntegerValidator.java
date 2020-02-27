/**
 * 
 */
package com.ibm.cio.cmr.request.util.validator;

/**
 * @author Jeffrey Zamora
 * 
 */
public class IntegerValidator implements ParamValidator {

  @Override
  public boolean validate(String value) {
    try {
      Integer.parseInt(value);
      return true;
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
