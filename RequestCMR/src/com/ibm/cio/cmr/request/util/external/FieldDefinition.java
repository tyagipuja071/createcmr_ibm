/**
 * 
 */
package com.ibm.cio.cmr.request.util.external;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Defines a UI field for external service use
 * 
 * @author JeffZAMORA
 * 
 */
public class FieldDefinition implements Serializable {

  /**
   * 
   */
  private static final long serialVersionUID = 4336027640632534389L;
  private String fieldId;
  private String fieldName;
  private String requiredInd;
  private String defaultValue;
  private int maxLength = 0;

  private List<String> values = new ArrayList<String>();
  private Map<String, List<String>> valueMap = new HashMap<String, List<String>>();
  private List<LovItem> lov = new ArrayList<LovItem>();

  public String getFieldId() {
    return fieldId;
  }

  public void setFieldId(String fieldId) {
    this.fieldId = fieldId;
  }

  public String getFieldName() {
    return fieldName;
  }

  public void setFieldName(String fieldName) {
    this.fieldName = fieldName;
  }

  public String getRequiredInd() {
    return requiredInd;
  }

  public void setRequiredInd(String requiredInd) {
    this.requiredInd = requiredInd;
  }

  public List<String> getValues() {
    return values;
  }

  public void setValues(List<String> values) {
    this.values = values;
  }

  public String getDefaultValue() {
    return defaultValue;
  }

  public void setDefaultValue(String defaultValue) {
    this.defaultValue = defaultValue;
  }

  public Map<String, List<String>> getValueMap() {
    return valueMap;
  }

  public void setValueMap(Map<String, List<String>> valueMap) {
    this.valueMap = valueMap;
  }

  public int getMaxLength() {
    return maxLength;
  }

  public void setMaxLength(int maxLength) {
    this.maxLength = maxLength;
  }

  public List<LovItem> getLov() {
    return lov;
  }

  public void setLov(List<LovItem> lov) {
    this.lov = lov;
  }

}
