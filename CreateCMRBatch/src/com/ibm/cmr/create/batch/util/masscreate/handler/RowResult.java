/**
 * 
 */
package com.ibm.cmr.create.batch.util.masscreate.handler;

import java.util.ArrayList;
import java.util.List;

/**
 * Container for the errors encountered during validation of rows
 * 
 * @author Jeffrey Zamora
 * 
 */
public class RowResult {

  private List<String> errors = new ArrayList<String>();

  public boolean isPassed() {
    return errors.size() <= 0;
  }

  public List<String> getErrors() {
    return errors;
  }

  public void addError(String errorMessage) {
    this.errors.add(errorMessage);
  }

  public static RowResult passed() {
    return new RowResult();
  }
}
