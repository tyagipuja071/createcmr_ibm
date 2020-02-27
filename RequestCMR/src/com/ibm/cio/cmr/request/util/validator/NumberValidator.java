package com.ibm.cio.cmr.request.util.validator;

public class NumberValidator implements ParamValidator {

  @Override
  public boolean validate(String value) {
    try {
      Double.parseDouble(value);
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
