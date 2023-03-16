/**
 * 
 */
package com.ibm.cio.cmr.request.masschange.obj;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

/**
 * Stores the validation results of a {@link MassChangeTemplate} validation
 * 
 * @author JeffZAMORA
 * 
 */
public class TemplateValidation {

  private List<ValidationRow> rows = new ArrayList<TemplateValidation.ValidationRow>();
  private final String tabName;

  private Map<String, StringBuilder> allError = new LinkedHashMap<String, StringBuilder>();

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

  public boolean hasErrors() {
    return this.allError.size() > 0;
  }

  public String getAllError() {
    StringBuilder tabErrors = new StringBuilder();

    for (Map.Entry<String, StringBuilder> entry : this.allError.entrySet()) {
      tabErrors.append(entry.getValue().toString()).append(": ").append(entry.getKey());
    }

    return tabErrors.toString();
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
      // this.error.append(this.error.length() > 0 ? "\n" : "");
      this.error.append(field + ": " + msg);

      if (allError.containsKey(msg)) {
        allError.get(msg).append(" Row" + this.rowNumber);
      } else {
        StringBuilder sbRowError = new StringBuilder();

        if (StringUtils.isNotEmpty(field)) {
          sbRowError.append(field).append(" :");
        }

        sbRowError.append(" Row").append("" + this.rowNumber);
        allError.put(msg, sbRowError);
      }
    }
  }

  public List<ValidationRow> getRows() {
    return rows;
  }

  public void setRows(List<ValidationRow> rows) {
    this.rows = rows;
  }
}
