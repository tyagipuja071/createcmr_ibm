/**
 * 
 */
package com.ibm.cio.cmr.request.ui;

/**
 * Container for a column on the grid
 * 
 * @author Jeffrey Zamora
 * 
 */
public class GridColumn {

  private String field;
  private String name;
  private String width;
  private String formatter;
  private String comparator;
  private String align;

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();

    sb.append("      " + this.field + " : {\n");
    sb.append("        field : '" + this.field + "',\n");
    sb.append("        name : '" + this.name + "',\n");
    if (this.align != null) {
      sb.append("        styles : 'text-align:" + this.align + ";',\n");
    }
    sb.append("        width : '" + this.width + "'");
    if (this.formatter != null && this.formatter.trim().length() > 0) {
      sb.append(",\n");
      sb.append("        formatter : " + this.formatter + "\n");
    } else {
      sb.append("\n");
    }
    sb.append("      }");
    return sb.toString();
  }

  public String getField() {
    return field;
  }

  public void setField(String field) {
    this.field = field;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getWidth() {
    return width;
  }

  public void setWidth(String width) {
    this.width = width;
  }

  public String getFormatter() {
    return formatter;
  }

  public void setFormatter(String formatter) {
    this.formatter = formatter;
  }

  public String getAlign() {
    return align;
  }

  public void setAlign(String align) {
    this.align = align;
  }

  public String getComparator() {
    return comparator;
  }

  public void setComparator(String comparator) {
    this.comparator = comparator;
  }
}
