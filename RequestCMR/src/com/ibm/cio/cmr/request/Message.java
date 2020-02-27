/**
 * 
 */
package com.ibm.cio.cmr.request;

/**
 * Represents a message
 * 
 * @author Jeffrey Zamora
 * 
 */
public class Message {

  private int code;
  private String message;

  public int getCode() {
    return code;
  }

  public void setCode(int code) {
    this.code = code;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

}
