/**
 * 
 */
package com.ibm.cio.tools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a table on the database
 * 
 * @author Jeffrey Zamora
 * 
 */
public class Table {

  private String name;
  private String schema;
  private List<Column> columns = new ArrayList<Column>();

  /**
   * Adds a column to the list
   * 
   * @param col
   */
  public void add(Column col) {
    this.columns.add(col);
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getSchema() {
    return schema;
  }

  public void setSchema(String schema) {
    this.schema = schema;
  }

  public List<Column> getColumns() {
    Collections.sort(this.columns);
    return columns;
  }

  public boolean hasTimestamp() {
    for (Column col : this.columns) {
      if ("TIMESTMP".equalsIgnoreCase(col.getType())) {
        return true;
      }
      if ("DATE".equalsIgnoreCase(col.getType())) {
        return true;
      }
      if ("TIME".equalsIgnoreCase(col.getType())) {
        return true;
      }
      if ("TIMESTAMP".equalsIgnoreCase(col.getType())) {
        return true;
      }
    }
    return false;
  }
}
