/**
 * 
 */
package com.ibm.cmr.create.batch.util.mq.exception;

/**
 * @author Jeffrey Zamora
 * 
 */
public class MQErrorLine implements Comparable<MQErrorLine> {

  public static final String TYPE_WARNING = "W-";
  public static final String TYPE_MANDATORY_ERROR = "M-";
  public static final String TYPE_GENERAL_ERROR = "E-";
  public static final String TYPE_ANY_ERROR = "G-";
  private String code;
  private String type;
  private String msg;

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getMsg() {
    return msg;
  }

  public void setMsg(String msg) {
    this.msg = msg;
  }

  @Override
  public int compareTo(MQErrorLine o) {
    int thisWeight = TYPE_MANDATORY_ERROR.equals(this.type) ? -2 : (TYPE_GENERAL_ERROR.equals(this.type) ? -1 : 1);
    int trgWeight = TYPE_MANDATORY_ERROR.equals(o.type) ? -2 : (TYPE_GENERAL_ERROR.equals(o.type) ? -1 : 1);

    if (thisWeight == trgWeight) {
      return this.code.compareTo(o.code);
    } else {
      return thisWeight < trgWeight ? -1 : (thisWeight > trgWeight ? 1 : 0);
    }
  }

}
