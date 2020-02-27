/**
 * 
 */
package com.ibm.cmr.create.batch.util.mq.exception;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Container for the Exception message returned by SOF systems
 * 
 * @author Jeffrey Zamora
 * 
 */
public class MQProcessingException {

  private static final List<String> HANDLED_ERRORS = Arrays.asList("M-", "W-", "E-", "G-");
  private List<MQErrorLine> errors = new ArrayList<MQErrorLine>();
  private static final String TOO_MANY_ERRORS_SUFFIX = "Too many to display...";
  private static final String TARGET_SOF = "SOF";
  private static final String TARGET_WTAAS = "WTAAS";

  public static void main(String[] args) {
    String error = "E- TRANSACTION CODE 'N' REQUIRES A LOCATION NUMBER TO BE ENTERED.                         M-  SALESMAN/SALESTEAM 504274 IS NOT IN THE 'C01' TABLE                                   E- NO ISAM/ISM CE BRANCH OFFICE NUMBER HAS BEEN ENTERED                                   E- LANGUAGE CODE NOT PRESENT                                                              ";
    // error = "Jeff's Error";
    MQProcessingException e = MQProcessingException.parse("WTAAS", error);
    System.out.println(e.toString());
  }

  private MQProcessingException() {
    // do not allow constructing manually
  }

  public static MQProcessingException parse(String targetSystem, String rawErrorMessage) {

    switch (targetSystem) {
    case TARGET_SOF:
      return parseSOFError(rawErrorMessage);
    case TARGET_WTAAS:
      return parseWTAASError(rawErrorMessage);
    default:
      MQProcessingException exception = new MQProcessingException();
      MQErrorLine line = new MQErrorLine();
      line.setType(MQErrorLine.TYPE_ANY_ERROR);
      line.setCode("000");
      line.setMsg(rawErrorMessage.trim());
      exception.errors.add(line);
      return exception;
    }
  }

  private static MQProcessingException parseSOFError(String rawErrorMessage) {
    MQProcessingException exception = new MQProcessingException();
    MQErrorLine line = null;

    if (rawErrorMessage.length() >= 95) {
      String[] errors = rawErrorMessage.split("(?<=\\G.{95})");
      for (String error : errors) {
        error = error.trim();
        if (error.length() >= 9) {
          error = error.trim();
          line = new MQErrorLine();
          line.setType(error.substring(6, 8));
          if (HANDLED_ERRORS.contains(line.getType())) {
            line.setCode(error.substring(0, 5));
            line.setMsg(error.substring(9).trim());
          } else {
            line.setType(MQErrorLine.TYPE_ANY_ERROR);
            line.setCode("000");
            line.setMsg(error.trim());
          }
          exception.errors.add(line);
        }
      }
    } else {
      line = new MQErrorLine();
      line.setType(MQErrorLine.TYPE_ANY_ERROR);
      line.setCode("000");
      line.setMsg(rawErrorMessage);
      exception.errors.add(line);
    }
    return exception;
  }

  private static MQProcessingException parseWTAASError(String rawErrorMessage) {
    // TODO update based on error message format from WTAAS
    MQProcessingException exception = new MQProcessingException();
    MQErrorLine line = null;
    if (rawErrorMessage.length() >= 90) {
      String[] errors = rawErrorMessage.split("(?<=\\G.{90})");
      for (String error : errors) {
        error = error.trim();
        if (error.length() >= 9) {
          error = error.trim();
          line = new MQErrorLine();
          System.err.println(error);
          line.setType(error.substring(0, 2));
          if (HANDLED_ERRORS.contains(line.getType())) {
            line.setCode(line.getType());
            line.setMsg(error.substring(2).trim());
          } else {
            line.setType(MQErrorLine.TYPE_ANY_ERROR);
            line.setCode("000");
            line.setMsg(error.trim());
          }
          exception.errors.add(line);
        }
      }
    } else {
      line = new MQErrorLine();
      line.setType(MQErrorLine.TYPE_ANY_ERROR);
      line.setCode("000");
      line.setMsg(rawErrorMessage);
      exception.errors.add(line);
    }

    return exception;
  }

  @Override
  public String toString() {
    return toString(-1);
  }

  public String toString(String... errorTypes) {
    return toString(-1, errorTypes);
  }

  public String toString(int maxLength, String... errorTypes) {
    StringBuilder sb = new StringBuilder();
    Collections.sort(this.errors);
    for (MQErrorLine line : this.errors) {
      if (errorTypes == null || (errorTypes.length == 0) || (errorTypes != null && Arrays.asList(errorTypes).contains(line.getType()))) {
        sb.append(sb.length() > 0 ? "\n" : "");
        sb.append(line.getType() + " " + line.getMsg());
      }
    }
    String formatted = "Error(s) encountered during processing:\n" + sb.toString();
    if (maxLength > 0 && formatted.length() > maxLength) {
      formatted = formatted.substring(0, maxLength - TOO_MANY_ERRORS_SUFFIX.length() - 10) + "...";
      formatted += "\n" + TOO_MANY_ERRORS_SUFFIX;
    }
    return formatted;
  }
}
