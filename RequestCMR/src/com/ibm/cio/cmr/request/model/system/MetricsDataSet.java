/**
 * 
 */
package com.ibm.cio.cmr.request.model.system;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Jeffrey Zamora
 * 
 */
public class MetricsDataSet implements Comparable<MetricsDataSet> {

  private String label;
  private String backgroundColor = "CmrMetrics.getRandomColor()";
  private List<Integer> data = new ArrayList<Integer>();

  public void addData(Integer data) {
    this.data.add(data);
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public String getBackgroundColor() {
    return backgroundColor;
  }

  public void setBackgroundColor(String backgroundColor) {
    this.backgroundColor = backgroundColor;
  }

  public List<Integer> getData() {
    return data;
  }

  public void setData(List<Integer> data) {
    this.data = data;
  }

  @Override
  public int compareTo(MetricsDataSet o) {
    if (o == null) {
      return -1;
    }
    return this.label.compareTo(o.label);
  }

}
