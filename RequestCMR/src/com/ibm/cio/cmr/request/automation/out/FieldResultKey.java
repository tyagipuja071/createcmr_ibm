/**
 * 
 */
package com.ibm.cio.cmr.request.automation.out;

/**
 * @author JeffZAMORA
 * 
 */
public class FieldResultKey {

  private String type;
  private String fieldName;

  public FieldResultKey(String type, String fieldName) {
    this.type = type;
    this.fieldName = fieldName;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof FieldResultKey)) {
      return false;
    }
    FieldResultKey k = (FieldResultKey) o;
    if (this.type == null && k.type != null) {
      return false;
    }
    if (this.type != null && k.type == null) {
      return false;
    }
    if (this.fieldName == null && k.fieldName != null) {
      return false;
    }
    if (this.fieldName != null && k.fieldName == null) {
      return false;
    }
    return this.fieldName.equals(k.fieldName) && this.type.equals(k.type);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int hash = 17;
    hash = hash * prime + (this.type != null ? this.type.hashCode() : 0);
    hash = hash * prime + (this.fieldName != null ? this.fieldName.hashCode() : 0);
    return hash;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getFieldName() {
    return fieldName;
  }

  public void setFieldName(String fieldName) {
    this.fieldName = fieldName;
  }

}
