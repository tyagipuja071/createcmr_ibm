package com.ibm.cio.cmr.request.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class DropdownModel implements Serializable {

  private static final long serialVersionUID = 1L;

  private String label;
  private String identifier;
  private String selectedItem;
  private List<DropdownItemModel> items;

  public String getIdentifier() {
    return identifier;
  }

  public void setIdentifier(String identifier) {
    this.identifier = identifier;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public List<DropdownItemModel> getItems() {
    return items;
  }

  public String getSelectedItem() {
    return selectedItem;
  }

  public void setSelectedItem(String selectedItem) {
    this.selectedItem = selectedItem;
  }

  public void addItems(DropdownItemModel item) {
    if (this.items == null) {
      this.items = new ArrayList<DropdownItemModel>();
    }

    this.items.add(item);
  }

}
