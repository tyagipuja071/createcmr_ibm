/**
 * 
 */
package com.ibm.cio.cmr.request.model.code;

import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

/**
 * @author Jeffrey Zamora
 * 
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ScenarioField implements Comparable<ScenarioField> {

  private String fieldId;
  private String fieldName;
  private String tabId;
  private String addrTyp;
  private boolean retainValInd;
  private String reqInd;
  private String lockedIndc;
  private List<String> values;

  @Override
  public int compareTo(ScenarioField o) {
    if (o == null) {
      return -1;
    }
    int fieldCompare = this.fieldId.compareTo(o.fieldId);
    if (fieldCompare != 0) {
      return fieldCompare;
    }
    int addrTypCompare = this.addrTyp.compareTo(o.addrTyp);
    return addrTypCompare;
  }

  public String getFieldId() {
    return fieldId;
  }

  public void setFieldId(String fieldId) {
    this.fieldId = fieldId;
  }

  public String getAddrTyp() {
    return addrTyp;
  }

  public void setAddrTyp(String addrTyp) {
    this.addrTyp = addrTyp;
  }

  public boolean isRetainValInd() {
    return retainValInd;
  }

  public void setRetainValInd(boolean retainValInd) {
    this.retainValInd = retainValInd;
  }

  public String getReqInd() {
    return reqInd;
  }

  public void setReqInd(String reqInd) {
    this.reqInd = reqInd;
  }

  public String getLockedIndc() {
    return lockedIndc;
  }

  public void setLockedIndc(String lockedIndc) {
    this.lockedIndc = lockedIndc;
  }

  public List<String> getValues() {
    return values;
  }

  public void setValues(List<String> values) {
    this.values = values;
  }

  public String getFieldName() {
    return fieldName;
  }

  public void setFieldName(String fieldName) {
    this.fieldName = fieldName;
  }

  public String getTabId() {
    return tabId;
  }

  public void setTabId(String tabId) {
    this.tabId = tabId;
  }

}
