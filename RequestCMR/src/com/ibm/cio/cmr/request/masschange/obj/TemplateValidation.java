/**
 * 
 */
package com.ibm.cio.cmr.request.masschange.obj;

import java.util.ArrayList;
import java.util.List;

/**
 * Stores the validation results of a {@link MassChangeTemplate} validation
 * 
 * @author JeffZAMORA
 * 
 */
public class TemplateValidation {

  private List<ValidationRow> rows = new ArrayList<TemplateValidation.ValidationRow>();
  private final String tabName;

  public TemplateValidation(String tabName) {
    // only allow template to instantiate this
    this.tabName = tabName;
  }

  public String getTabName() {
    return this.tabName;
  }

  public void setSuccess(int rowNumber) {
    ValidationRow row = new ValidationRow(rowNumber);
    this.rows.add(row);
  }

  public void addError(int rowNumber, String field, String msg) {
    boolean found = false;
    for (ValidationRow row : this.rows) {
      if (row.getRowNumber() == rowNumber) {
        row.addError(field, msg);
        found = true;
      }
    }
    if (!found) {
      ValidationRow row = new ValidationRow(rowNumber);
      row.addError(field, msg);
      this.rows.add(row);
    }
  }

  public class ValidationRow {
    private final int rowNumber;
    private final StringBuilder error = new StringBuilder();

    private ValidationRow(int rowNumber) {
      this.rowNumber = rowNumber;
    }

    public boolean isSuccess() {
      return this.error.length() == 0;
    }

    public int getRowNumber() {
      return this.rowNumber;
    }

    public String getError() {
      return this.error.toString();
    }

    public void addError(String field, String msg) {
      this.error.append(this.error.length() > 0 ? "\n" : "");
      this.error.append(field + ": " + msg);

    }
  }

  public List<ValidationRow> getRows() {
    return rows;
  }

  public void setRows(List<ValidationRow> rows) {
    this.rows = rows;
  }
}
