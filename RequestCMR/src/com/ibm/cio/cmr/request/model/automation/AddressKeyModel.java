/**
 * 
 */
package com.ibm.cio.cmr.request.model.automation;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

/**
 * @author JeffZAMORA
 *
 */
@JsonIgnoreProperties(
    ignoreUnknown = true)
public class AddressKeyModel {

  private String cmrType;
  private String rdcType;

  public String getCmrType() {
    return cmrType;
  }

  public void setCmrType(String cmrType) {
    this.cmrType = cmrType;
  }

  public String getRdcType() {
    return rdcType;
  }

  public void setRdcType(String rdcType) {
    this.rdcType = rdcType;
  }

}
