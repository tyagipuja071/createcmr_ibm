package com.ibm.cio.cmr.request.model.system;

import java.util.ArrayList;
import java.util.List;

public class MetricsChart {

  private List<String> labels = new ArrayList<String>();
  private List<MetricsDataSet> datasets = new ArrayList<MetricsDataSet>();

  public void addLabel(String label) {
    this.labels.add(label);
  }

  public void addDataSet(MetricsDataSet dataSet) {
    this.datasets.add(dataSet);
  }

  public List<String> getLabels() {
    return labels;
  }

  public void setLabels(List<String> labels) {
    this.labels = labels;
  }

  public List<MetricsDataSet> getDatasets() {
    return datasets;
  }

  public void setDatasets(List<MetricsDataSet> datasets) {
    this.datasets = datasets;
  }
}
