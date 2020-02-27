/**
 * 
 */
package com.ibm.cio.cmr.request.entity;

import javax.persistence.EmbeddedId;

/**
 * Base Class for Entities
 * 
 * @author Jeffrey Zamora
 * 
 */
public abstract class BaseEntity<K extends BaseEntityPk> {

  @EmbeddedId
  private K id;

  public K getId() {
    return id;
  }

  public void setId(K id) {
    this.id = id;
  }

}
