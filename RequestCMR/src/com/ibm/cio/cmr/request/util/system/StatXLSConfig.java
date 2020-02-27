/**
 * 
 */
package com.ibm.cio.cmr.request.util.system;

/**
 * @author Jeffrey Zamora
 * 
 */
public class StatXLSConfig {

  private String comment;
  private int width;
  private String dbField;
  private String label;

  public StatXLSConfig(String label, String dbField, int width, String comment) {
    this.label = label;
    this.dbField = dbField;
    this.width = width;
    this.comment = comment;
  }

  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }

  public int getWidth() {
    return width;
  }

  public void setWidth(int width) {
    this.width = width;
  }

  public String getDbField() {
    return dbField;
  }

  public void setDbField(String dbField) {
    this.dbField = dbField;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }
}
