/**
 * 
 */
package com.ibm.cio.cmr.request.model.automation;

import java.util.List;

/**
 * @author JeffZAMORA
 * 
 */
public class AutoConfigMapModel {

  private String directive;
  private String configId;
  private String processOnCompletion;
  private List<String> countries;

  public String getDirective() {
    return directive;
  }

  public void setDirective(String directive) {
    this.directive = directive;
  }

  public String getConfigId() {
    return configId;
  }

  public void setConfigId(String configId) {
    this.configId = configId;
  }

  public List<String> getCountries() {
    return countries;
  }

  public void setCountries(List<String> countries) {
    this.countries = countries;
  }

  public String getProcessOnCompletion() {
    return processOnCompletion;
  }

  public void setProcessOnCompletion(String processOnCompletion) {
    this.processOnCompletion = processOnCompletion;
  }

}
