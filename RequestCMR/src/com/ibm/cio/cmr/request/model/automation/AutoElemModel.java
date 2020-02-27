/**
 * 
 */
package com.ibm.cio.cmr.request.model.automation;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

/**
 * Represents one item in the automation engine admin page
 * 
 * @author JeffZAMORA
 * 
 */
@JsonIgnoreProperties(
    ignoreUnknown = true)
public class AutoElemModel {

  private String configId;
  private long elementId;
  private long execOrd;
  private String processCd;
  private String requestTyp;
  private String actionOnError;
  private boolean overrideDataIndc;
  private boolean stopOnErrorIndc;
  private boolean status;

  public String getConfigId() {
    return configId;
  }

  public void setConfigId(String configId) {
    this.configId = configId;
  }

  public long getElementId() {
    return elementId;
  }

  public void setElementId(long elementId) {
    this.elementId = elementId;
  }

  public long getExecOrd() {
    return execOrd;
  }

  public void setExecOrd(long execOrd) {
    this.execOrd = execOrd;
  }

  public String getProcessCd() {
    return processCd;
  }

  public void setProcessCd(String processCd) {
    this.processCd = processCd;
  }

  public String getRequestTyp() {
    return requestTyp;
  }

  public void setRequestTyp(String requestTyp) {
    this.requestTyp = requestTyp;
  }

  public String getActionOnError() {
    return actionOnError;
  }

  public void setActionOnError(String actionOnError) {
    this.actionOnError = actionOnError;
  }

  public boolean isOverrideDataIndc() {
    return overrideDataIndc;
  }

  public void setOverrideDataIndc(boolean overrideDataIndc) {
    this.overrideDataIndc = overrideDataIndc;
  }

  public boolean isStopOnErrorIndc() {
    return stopOnErrorIndc;
  }

  public void setStopOnErrorIndc(boolean stopOnErrorIndc) {
    this.stopOnErrorIndc = stopOnErrorIndc;
  }

  public boolean isStatus() {
    return status;
  }

  public void setStatus(boolean status) {
    this.status = status;
  }

}
