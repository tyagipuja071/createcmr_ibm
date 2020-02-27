package com.ibm.cio.cmr.request.model;

import java.io.Serializable;

public class DropdownItemModel implements Serializable {

  private static final long serialVersionUID = 1L;

  private String id;

  private String name;

  @Override
  public String toString() {
    return "[Value=" + this.id + ", Label=" + this.name + "]";
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getId() {
    return id;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

}
