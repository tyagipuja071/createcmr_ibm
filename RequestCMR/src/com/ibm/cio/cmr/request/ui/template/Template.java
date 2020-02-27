/**
 * 
 */
package com.ibm.cio.cmr.request.ui.template;

import java.util.List;

/**
 * @author Jeffrey Zamora
 * 
 */
public class Template {

  private String cmrIssuingCountry;
  private List<TemplatedField> fields;
  private TemplateDriver driver;

  public String getCmrIssuingCountry() {
    return cmrIssuingCountry;
  }

  public void setCmrIssuingCountry(String cmrIssuingCountry) {
    this.cmrIssuingCountry = cmrIssuingCountry;
  }

  public List<TemplatedField> getFields() {
    return fields;
  }

  public void setFields(List<TemplatedField> fields) {
    this.fields = fields;
  }

  public TemplateDriver getDriver() {
    return driver;
  }

  public void setDriver(TemplateDriver driver) {
    this.driver = driver;
  }
}
