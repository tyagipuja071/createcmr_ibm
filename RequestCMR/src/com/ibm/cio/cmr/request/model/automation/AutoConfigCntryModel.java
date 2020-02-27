/**
 * 
 */
package com.ibm.cio.cmr.request.model.automation;

import java.util.List;

/**
 * @author JeffZAMORA
 * 
 */
public class AutoConfigCntryModel {

  private String configId;
  private String autoEngineIndc;
  private String processOnCompletion;
  private boolean saveConfig;
  private boolean saveProcessOnCompletion;
  private boolean saveEnablement;
  private boolean removeCountry;

  private List<String> countries;

  public String getConfigId() {
    return configId;
  }

  public void setConfigId(String configId) {
    this.configId = configId;
  }

  public String getAutoEngineIndc() {
    return autoEngineIndc;
  }

  public void setAutoEngineIndc(String autoEngineIndc) {
    this.autoEngineIndc = autoEngineIndc;
  }

  public List<String> getCountries() {
    return countries;
  }

  public void setCountries(List<String> countries) {
    this.countries = countries;
  }

  public boolean isSaveConfig() {
    return saveConfig;
  }

  public void setSaveConfig(boolean saveConfig) {
    this.saveConfig = saveConfig;
  }

  public boolean isSaveEnablement() {
    return saveEnablement;
  }

  public void setSaveEnablement(boolean saveEnablement) {
    this.saveEnablement = saveEnablement;
  }

  public boolean isRemoveCountry() {
    return removeCountry;
  }

  public void setRemoveCountry(boolean removeCountry) {
    this.removeCountry = removeCountry;
  }

  public boolean isSaveProcessOnCompletion() {
    return saveProcessOnCompletion;
  }

  public void setSaveProcessOnCompletion(boolean saveProcessOnCompletion) {
    this.saveProcessOnCompletion = saveProcessOnCompletion;
  }

  public String getProcessOnCompletion() {
    return processOnCompletion;
  }

  public void setProcessOnCompletion(String processOnCompletion) {
    this.processOnCompletion = processOnCompletion;
  }

}
