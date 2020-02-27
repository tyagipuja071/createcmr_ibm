package com.ibm.cio.cmr.request;

import java.text.MessageFormat;

import com.ibm.cio.cmr.request.util.MessageUtil;

/**
 * Class to handle ALL exceptions in this system. Message codes submitted to
 * this class should be mapped in the cmr-messages.properties file
 * 
 * @author Jeffrey Zamora
 * 
 */
public class CmrException extends Exception {

  private static final long serialVersionUID = 1L;

  private Object[] params;
  private int code;
  private String message;

  /**
   * Wrap a general exception as CmrException
   * 
   * @param e
   */
  public CmrException(Exception e) {
    super(e);
    this.message = e.getMessage();
  }

  /**
   * Creates an instance of the CmrException with the given code.
   * 
   * @param exceptionCode
   */
  public CmrException(int exceptionCode) {
    this(exceptionCode, (String[]) null);
  }

  /**
   * Creates an instance of the CmrException with the given code.
   * 
   * @param exceptionCode
   */
  public CmrException(int exceptionCode, Exception cause) {
    this(exceptionCode, cause, (String[]) null);
  }

  /**
   * 
   * Creates an instance of the CmrException with the given code and parameters
   * 
   * @param exceptionCode
   * @param parameters
   */
  public CmrException(int exceptionCode, Exception cause, String... parameters) {
    super(cause);
    this.code = exceptionCode;
    this.params = parameters;
    extractMessage();
  }

  /**
   * 
   * Creates an instance of the CmrException with the given code and parameters
   * 
   * @param exceptionCode
   * @param parameters
   */
  public CmrException(int exceptionCode, String... parameters) {
    this.code = exceptionCode;
    this.params = parameters;
    extractMessage();
  }

  /**
   * Extracts the message from the bundle, assigning parameters if available
   */
  private void extractMessage() {
    String message = MessageUtil.getMessage(this.code);
    if (this.params == null) {
      setMessage(message);
    } else {
      setMessage(MessageFormat.format(message, this.params));
    }
  }

  public String[] getParams() {
    return (String[]) params;
  }

  public void setParams(String[] params) {
    this.params = params;
  }

  public int getCode() {
    return code;
  }

  public void setCode(int code) {
    this.code = code;
  }

  @Override
  public String getMessage() {
    return message;
  }

  private void setMessage(String message) {
    this.message = message;
  }

}
