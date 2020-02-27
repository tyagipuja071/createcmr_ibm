/**
 * 
 */
package com.ibm.cio.cmr.request.config;

/**
 * Represents an entry in the configuration XML
 * 
 * @author Jeffrey Zamora
 * 
 */
public class ConfigItem implements Comparable<ConfigItem> {

  private String id;
  private int order;
  private String name;
  private String description;
  private String type = "X";
  private boolean editable;
  private String editType;
  private String value;
  private boolean required = true;
  private String hint;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public boolean isEditable() {
    return editable;
  }

  public void setEditable(boolean editable) {
    this.editable = editable;
  }

  public String getEditType() {
    return editType;
  }

  public void setEditType(String editType) {
    this.editType = editType;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
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
  public int compareTo(ConfigItem o) {
    if (o == null) {
      return 1;
    }
    return this.order < o.order ? -1 : (this.order > o.order ? 1 : 0);
  }

  public boolean isRequired() {
    return required;
  }

  public void setRequired(boolean required) {
    this.required = required;
  }

  public String getHint() {
    return hint;
  }

  public void setHint(String hint) {
    this.hint = hint;
  }
}
