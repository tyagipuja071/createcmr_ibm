/**
 * 
 */
package com.ibm.cio.cmr.request.util.external;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Container for the fields
 * 
 * @author JeffZAMORA
 * 
 */
public class FieldContainer implements Serializable {

  private static final long serialVersionUID = 1L;
  private Map<String, FieldDefinition> definitions = new HashMap<String, FieldDefinition>();

  /**
   * Gets the {@link FieldDefinition} for the given fieldId
   * 
   * @param fieldId
   * @return
   */
  public FieldDefinition getFieldDefinition(String fieldId) {
    return this.definitions.get(fieldId);
  }

  /**
   * Locates a {@link FieldDefinition} via the registered fieldName
   * 
   * @param fieldName
   * @return
   */
  public FieldDefinition locateByFieldName(String fieldName) {
    for (FieldDefinition def : this.definitions.values()) {
      if (fieldName.equals(def.getFieldName())) {
        return def;
      }
    }
    return null;
  }

  /**
   * Checks if the container has the field
   * 
   * @param fieldId
   * @return
   */
  public boolean contains(String fieldId) {
    return this.definitions.containsKey(fieldId);
  }

  /**
   * Adds a definition
   * 
   * @param fieldDef
   */
  public void add(FieldDefinition fieldDef) {
    this.definitions.put(fieldDef.getFieldId(), fieldDef);
  }

  /**
   * Gets the size of number of field definitions on the container
   * 
   * @return
   */
  public int size() {
    return this.definitions.size();
  }

  public Map<String, FieldDefinition> getDefinitions() {
    return definitions;
  }

  public void setDefinitions(Map<String, FieldDefinition> definitions) {
    this.definitions = definitions;
  }
}
