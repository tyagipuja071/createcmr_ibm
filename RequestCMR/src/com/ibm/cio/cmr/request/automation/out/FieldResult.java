/**
 * 
 */
package com.ibm.cio.cmr.request.automation.out;

/**
 * Contains information on a specific field with old and new values
 * 
 * @author JeffZAMORA
 * 
 */
public class FieldResult {

  private String fieldName;
  private String addrType;
  private String oldValue;
  private String newValue;
  private String processCode;

  public FieldResult(String processCode, String addrType, String field, String oldValue, String newValue) {
    this.fieldName = field;
    this.oldValue = oldValue;
    this.newValue = newValue;
    this.addrType = addrType;
    this.processCode = processCode;
  }

  public String getFieldName() {
    return fieldName;
  }

  public void setFieldName(String fieldName) {
    this.fieldName = fieldName;
  }

  public String getOldValue() {
    return oldValue;
  }

  public void setOldValue(String oldValue) {
    this.oldValue = oldValue;
  }

  public String getNewValue() {
    return newValue;
  }

  public void setNewValue(String newValue) {
    this.newValue = newValue;
  }

  public String getAddrType() {
    return addrType;
  }

  public void setAddrType(String addrType) {
    this.addrType = addrType;
  }

  public String getProcessCode() {
    return processCode;
  }

  public void setProcessCode(String processCode) {
    this.processCode = processCode;
  }
}
