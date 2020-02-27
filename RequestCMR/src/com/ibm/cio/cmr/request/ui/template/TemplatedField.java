package com.ibm.cio.cmr.request.ui.template;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

public class TemplatedField {

  private String fieldId;
  private String fieldName;
  private boolean retainValue;
  private String parentTab;
  private boolean addressField;
  private String requiredInd;
  private String lockInd;

  private Map<String, List<String>> valueMap = new HashMap<String, List<String>>();

  private List<String> values = new ArrayList<String>();

  public void clearValues() {
    this.values.clear();
  }

  public void clearValues(String addressType) {
    if (this.valueMap.get(addressType) != null) {
      this.valueMap.get(addressType).clear();
    }
  }

  public void addValue(String value) {
    if (value != null && !StringUtils.isBlank(value)) {
      this.values.add(value);
    }
  }

  public void addValue(String value, String addrType) {
    if (value != null && !StringUtils.isBlank(value)) {
      if (this.valueMap.get(addrType) == null) {
        this.valueMap.put(addrType, new ArrayList<String>());
      }
      this.valueMap.get(addrType).add(value);
    }
  }

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

  public List<String> getValues() {
    return values;
  }

  public void setValues(List<String> values) {
    this.values = values;
  }

  public boolean isRetainValue() {
    return retainValue;
  }

  public void setRetainValue(boolean retainValue) {
    this.retainValue = retainValue;
  }

  public String getParentTab() {
    return parentTab;
  }

  public void setParentTab(String parentTab) {
    this.parentTab = parentTab;
  }

  public Map<String, List<String>> getValueMap() {
    return valueMap;
  }

  public void setValueMap(Map<String, List<String>> valueMap) {
    this.valueMap = valueMap;
  }

  public boolean isAddressField() {
    return addressField;
  }

  public void setAddressField(boolean addressField) {
    this.addressField = addressField;
  }

  public String getRequiredInd() {
    return requiredInd;
  }

  public void setRequiredInd(String requiredInd) {
    this.requiredInd = requiredInd;
  }

  public String getLockInd() {
    return lockInd;
  }

  public void setLockInd(String lockInd) {
    this.lockInd = lockInd;
  }

}
