package com.ibm.cio.cmr.request.util.validator;

public class BooleanValidator implements ParamValidator {

  @Override
  public boolean validate(String value) {
    try {
      return "TRUE".equalsIgnoreCase(value) || "FALSE".equalsIgnoreCase(value);
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
