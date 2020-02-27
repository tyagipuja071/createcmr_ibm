/**
 * 
 */
package com.ibm.cio.cmr.request.ui;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ibm.cio.cmr.request.model.DropdownItemModel;

/**
 * @author Jeffrey Zamora
 * 
 */
public class FieldManager implements Serializable {

  private static final long serialVersionUID = 1L;

  private String fieldId;

  private String type;

  private String validation;

  private Map<String, List<DropdownItemModel>> itemMap = new HashMap<String, List<DropdownItemModel>>();

  private Map<String, List<FieldInformation>> fieldInfoMap = new HashMap<String, List<FieldInformation>>();

  public void addFieldInfo(String cmrIssuingCntry, FieldInformation info) {
    if (this.fieldInfoMap.get(cmrIssuingCntry) == null) {
      this.fieldInfoMap.put(cmrIssuingCntry, new ArrayList<FieldInformation>());
    }
    this.fieldInfoMap.get(cmrIssuingCntry).add(info);
  }

  public List<FieldInformation> getFieldInfo(String cmrIssuingCountry) {
    return this.fieldInfoMap.get(cmrIssuingCountry);
  }

  public String getFieldId() {
    return fieldId;
  }

  public void setFieldId(String fieldId) {
    this.fieldId = fieldId;
  }

  public Map<String, List<FieldInformation>> getFieldInfoMap() {
    return fieldInfoMap;
  }

  public void setFieldInfoMap(Map<String, List<FieldInformation>> fieldInfoMap) {
    this.fieldInfoMap = fieldInfoMap;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getValidation() {
    return validation;
  }

  public void setValidation(String validation) {
    this.validation = validation;
  }

  public Map<String, List<DropdownItemModel>> getItemMap() {
    return itemMap;
  }

  public void setItemMap(Map<String, List<DropdownItemModel>> itemMap) {
    this.itemMap = itemMap;
  }

}
