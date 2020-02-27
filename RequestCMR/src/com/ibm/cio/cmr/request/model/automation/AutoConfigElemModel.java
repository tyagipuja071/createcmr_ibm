/**
 * 
 */
package com.ibm.cio.cmr.request.model.automation;

import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

/**
 * Model to handle the parameters passed from the Automation Engine admin
 * screens
 * 
 * @author JeffZAMORA
 * 
 */
@JsonIgnoreProperties(
    ignoreUnknown = true)
public class AutoConfigElemModel {

  private String action;
  private String configId;
  private List<AutoElemModel> elements;

  public String getAction() {
    return action;
  }

  public void setAction(String action) {
    this.action = action;
  }

  public String getConfigId() {
    return configId;
  }

  public void setConfigId(String configId) {
    this.configId = configId;
  }

  public List<AutoElemModel> getElements() {
    return elements;
  }

  public void setElements(List<AutoElemModel> elements) {
    this.elements = elements;
  }

}
