/**
 * 
 */
package com.ibm.cio.cmr.request.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Hanldes compound entities
 * 
 * @author Jeffrey Zamora
 * 
 */
public class CompoundEntity extends BaseEntity<CompoundEntityPK> {

  private List<BaseEntity<?>> entityList;
  private Map<String, Object> valueMap;

  public CompoundEntity() {
    this.entityList = new ArrayList<BaseEntity<?>>();
    this.valueMap = new HashMap<String, Object>();
  }

  public void addEntity(BaseEntity<?> entity) {
    if (entity == null) {
      return;
    }
    this.entityList.add(entity);
  }

  @SuppressWarnings("unchecked")
  public <M extends BaseEntity<?>> M getEntity(Class<M> entityClass) {
    for (BaseEntity<?> value : this.entityList) {
      if (entityClass.isInstance(value)) {
        return (M) value;
      }
    }
    return null;
  }

  public void setValue(String key, Object value) {
    this.valueMap.put(key, value);
  }

  public Object getValue(String key) {
    return this.valueMap.get(key);
  }

}
