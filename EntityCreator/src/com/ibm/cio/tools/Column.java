/**
 * 
 */
package com.ibm.cio.tools;

/**
 * Represents a column of a table
 * 
 * @author Jeffrey Zamora
 * 
 */
public class Column implements Comparable<Column> {

  private String name;
  private boolean primaryKey;
  private String type;
  private int order;
  private String remarks;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public boolean isPrimaryKey() {
    return primaryKey;
  }

  public void setPrimaryKey(boolean primaryKey) {
    this.primaryKey = primaryKey;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public int getOrder() {
    return order;
  }

  public void setOrder(int order) {
    this.order = order;
  }

  @Override
  public int compareTo(Column col) {
    if (this.order < col.order) {
      return -1;
    }
    if (this.order > col.order) {
      return 1;
    }
    if (this.order < col.order) {
      return this.name.compareTo(col.name);
    }
    return 0;
  }

  public String getRemarks() {
    return remarks;
  }

  public void setRemarks(String remarks) {
    this.remarks = remarks;
  }

}
