/**
 * 
 */
package com.ibm.cio.cmr.request.model.system;

import java.util.ArrayList;
import java.util.List;

import com.ibm.cio.cmr.request.util.mq.MQXml;

/**
 * @author JeffZAMORA
 * 
 */
public class MQXmlModel {

  private boolean exists;
  private boolean error;
  private String errorMsg;

  private List<MQXml> xmls = new ArrayList<MQXml>();

  public void add(MQXml xml) {
    this.xmls.add(xml);
  }

  public boolean isExists() {
    return exists;
  }

  public void setExists(boolean exists) {
    this.exists = exists;
  }

  public boolean isError() {
    return error;
  }

  public void setError(boolean error) {
    this.error = error;
  }

  public List<MQXml> getXmls() {
    return xmls;
  }

  public void setXmls(List<MQXml> xmls) {
    this.xmls = xmls;
  }

  public String getErrorMsg() {
    return errorMsg;
  }

  public void setErrorMsg(String errorMsg) {
    this.errorMsg = errorMsg;
  }
}
