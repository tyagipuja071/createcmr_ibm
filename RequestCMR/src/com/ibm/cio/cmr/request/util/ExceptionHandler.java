package com.ibm.cio.cmr.request.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;

public class ExceptionHandler {

  protected final static Logger LOG = Logger.getLogger(ExceptionHandler.class);

  /**
   * Static handle method to call throughout business logic
   * 
   * @param throwable
   * @param errorDescription
   */
  public static void handle(Throwable throwable, String errorDescription) {
    ExceptionHandler handler = new ExceptionHandler();
    handler.process(throwable, errorDescription, null, true);
  }

  /**
   * Static handle method to call throughout business logic
   * 
   * @param throwable
   * @param errorDescription
   */
  public static void handle(Throwable throwable, String errorDescription, Object[] params) {
    ExceptionHandler handler = new ExceptionHandler();
    handler.process(throwable, errorDescription, params, true);
  }

  /**
   * Static handle method to call throughout business logic
   * 
   * @param throwable
   * @param errorDescription
   */
  public static String handle(Throwable throwable, String errorDescription, Object[] params, boolean doLog) {
    String result = null;
    ExceptionHandler handler = new ExceptionHandler();
    result = handler.process(throwable, errorDescription, params, doLog);
    return result;
  }

  public static String getStackTrace(Throwable e) {
    String result = null;
    StringWriter out = new StringWriter();
    e.printStackTrace(new PrintWriter(out));
    result = out.toString();
    return result;
  }

  /**
   * Process the exception and its nested throwables
   * 
   * @param throwable
   * @param errorDescription
   */
  private String process(Throwable throwable, String errorDescription, Object[] params, boolean doLog) {

    StringBuffer entry = new StringBuffer();

    entry.append("\n==================================================================================");
    entry.append("\n============================   Exception begin   =================================");
    entry.append("\n==================================================================================");

    // Prepare current date & time
    SimpleDateFormat sdf = new SimpleDateFormat("M/dd/yy HH:mm z");
    String stringTime = sdf.format(new Date());

    entry.append("\n\nDate: " + stringTime);

    // Determine name of server error occured on
    String server = null;
    try {
      server = InetAddress.getLocalHost().getHostName();
    } catch (Exception ex) {
      server = "(unable to determine)";
    }
    entry.append("\n\nServer: " + server);

    // Determine user TID logged in the session whilst exception occurred
    // AppUser appUser = (AppUser)CMRSearchThreadLocalMap.get("user");
    // String userCnum = appUser == null ? "UNKNOWN" : appUser.getIntranetId()
    // +"-"+appUser.getUserCnum();
    // entry.append("\n\nUser: " + userCnum);

    // Determine and log the details about the "handled from" location
    StackTraceElement handledFrom = determineCallingTraceElement();
    entry.append("\n\nHandler Description: " + errorDescription);
    entry.append("\n   Handled by: ");
    if (handledFrom == null) {
      entry.append("\n(Unable to determine details about handling location)");
    } else {
      StringBuffer msg = new StringBuffer();
      msg.append("\n      Class: ");
      msg.append(handledFrom.getClassName());
      msg.append("\n      Method: ");
      msg.append(handledFrom.getMethodName());
      msg.append("\n      File: ");
      msg.append(handledFrom.getFileName());
      msg.append("\n      Line Number: ");
      msg.append(handledFrom.getLineNumber());
      msg.append("\n      Native Method: ");
      msg.append(handledFrom.isNativeMethod());
      entry.append(msg);
    }

    Throwable t = throwable;
    while (t != null) {

      // Determine and log the details about the exception and each
      // "nested exception" location
      StackTraceElement errorLoc = determineErrorLocationTraceElement(t);

      String exceptionMessage;

      // Determine error message
      if (t.getMessage() == null) {
        exceptionMessage = "(no error description specified -- check stack trace)";
      } else {
        exceptionMessage = t.getMessage();
      }

      entry.append("\n\nException: " + t.getClass().getName() + "   -   " + exceptionMessage);

      entry.append("\n   Exception thrown by: ");
      if (errorLoc == null) {
        entry.append("\n(Unable to determine details about location causing error)");
      } else {
        StringBuffer msg = new StringBuffer();
        msg.append("\n      Class: ");
        msg.append(errorLoc.getClassName());
        msg.append("\n      Method: ");
        msg.append(errorLoc.getMethodName());
        msg.append("\n      File: ");
        msg.append(errorLoc.getFileName());
        msg.append("\n      Line Number: ");
        msg.append(errorLoc.getLineNumber());
        msg.append("\n      Native Method: ");
        msg.append(errorLoc.isNativeMethod());
        entry.append(msg);
      }

      // Unwrap the next nested exception
      t = t.getCause();
    }

    /*
     * if (throwable instanceof AcePersistenceException) {
     * entry.append("\n\nSQL String: " +
     * ((AcePersistenceException)throwable).getSqlString()); }
     */

    entry.append("\n\nStack Trace:\n");

    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    throwable.printStackTrace(pw);
    entry.append(sw.toString());
    pw.close();
    try {
      sw.close();
    } catch (IOException ignored) {
    }

    if (params != null && params.length > 0) {
      logParams(params, entry);
    }

    entry.append("\n\n==================================================================================");
    entry.append("\n============================   Exception end     =================================");
    entry.append("\n==================================================================================");

    String result = entry.toString();
    if (doLog) {
      LOG.error(result);
    }

    return result;
  }

  /**
   * Helper function to determine the location of the exception in source
   * 
   * @param throwable
   * @return
   */
  private StackTraceElement determineErrorLocationTraceElement(Throwable throwable) {

    StackTraceElement errorLocation = null;
    StackTraceElement stackTrace[] = throwable.getStackTrace();

    // Loop over stack of current execution and determine class that called
    // handle
    for (int i = 0; i < stackTrace.length; i++) {
      StackTraceElement element = stackTrace[i];

      // Ignore all classes not in quota
      if (element.getClassName().startsWith("com.ibm.cio")) {
        errorLocation = element;
        break;
      }
    }

    // If not yet found and if stack not empty then return first element
    if (errorLocation == null && stackTrace.length > 0) {
      errorLocation = stackTrace[0];
    }

    return errorLocation;
  }

  /**
   * Helper function to determine the caller information
   * 
   * @return
   */
  @SuppressWarnings("unchecked")
  protected StackTraceElement determineCallingTraceElement() {

    Throwable throwable = new Throwable(); // NOPMD by vivek on 7/5/13 12:29 PM
    StackTraceElement callingElement = null;
    StackTraceElement[] stackTrace = throwable.getStackTrace();

    // Loop over stack of current execution and determine class that called
    // handle
    for (int i = 0; i < stackTrace.length; i++) {
      StackTraceElement element = stackTrace[i];
      @SuppressWarnings("rawtypes")
      Class clazz = null;

      // In case of class loader issues, return the first one that can't be
      // accessed
      try {
        clazz = Class.forName(element.getClassName());
      } catch (Exception ex) {
        callingElement = element;
        break;
      }

      // Ignore this handler and all classes extended from this handler
      if (!clazz.isAssignableFrom(ExceptionHandler.class)) {
        callingElement = element;
        break;
      }
    }

    // If not yet found and if stack not empty then return last element
    if (callingElement == null && stackTrace.length > 0) {
      callingElement = stackTrace[stackTrace.length - 1];
    }

    return callingElement;
  }

  private void logParams(Object[] params, StringBuffer entry) {

    // Log Parameters
    entry.append("\n\n=============   Parameters   =================\n");
    for (int i = 0; i < params.length; i++) {
      Object param = params[i];
      entry.append("Parameter " + i + 1 + ": \n");
      if (param != null) {
        entry.append(param.toString());
      } else {
        entry.append("null");
      }
      entry.append("\n");
      entry.append("=--------------------------------------------=\n");
    }
    entry.append("==============================================\n\n");

  }

}
