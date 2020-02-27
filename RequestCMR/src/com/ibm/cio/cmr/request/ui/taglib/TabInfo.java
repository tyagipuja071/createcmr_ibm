/**
 * 
 */
package com.ibm.cio.cmr.request.ui.taglib;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Jeffrey Zamora
 * 
 */
public class TabInfo {

  private String tabId;
  private String sectionId;
  private List<String> grids = new ArrayList<String>();

  public TabInfo(String tabId, String sectionId, String... gridIds) {
    this.tabId = tabId;
    this.sectionId = sectionId;
    if (gridIds != null && gridIds.length > 0) {
      this.grids.addAll(Arrays.asList(gridIds));
    }
  }

  public String getTabId() {
    return tabId;
  }

  public void setTabId(String tabId) {
    this.tabId = tabId;
  }

  public String getSectionId() {
    return sectionId;
  }

  public void setSectionId(String sectionId) {
    this.sectionId = sectionId;
  }

  public List<String> getGrids() {
    return grids;
  }

  public void setGrids(List<String> grids) {
    this.grids = grids;
  }
}
