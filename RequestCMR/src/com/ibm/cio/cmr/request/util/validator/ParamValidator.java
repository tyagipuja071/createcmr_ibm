package com.ibm.cio.cmr.request.util.validator;

public interface ParamValidator {

  public boolean validate(String value);

  public String getErrorMessage();

}
