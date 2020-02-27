/**
 * 
 */
package com.ibm.cio.cmr.request.model;

import java.io.Serializable;

/**
 * @author Jeffrey Zamora
 * 
 */
public class ProcessResultModel implements Serializable {

  private static final long serialVersionUID = 1L;

  private boolean success;
  private String message;
  private boolean displayMsg;

  public boolean isDisplayMsg() {
    return displayMsg;
  }

  public void setDisplayMsg(boolean displayMsg) {
    this.displayMsg = displayMsg;
  }

  public boolean isSuccess() {
    return success;
  }

  public void setSuccess(boolean success) {
    this.success = success;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

}
