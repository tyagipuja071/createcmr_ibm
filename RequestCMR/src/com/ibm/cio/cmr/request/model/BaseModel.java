/**
 * 
 */
package com.ibm.cio.cmr.request.model;

import java.io.Serializable;

import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;

/**
 * BaseModel for MVC models
 * 
 * @author Jeffrey Zamora
 * 
 */
public abstract class BaseModel implements Serializable {

  private static final long serialVersionUID = 1L;

  public static final int STATE_NEW = 0;
  public static final int STATE_EXISTING = 1;

  public static final String ACT_INSERT = "I";
  public static final String ACT_UPDATE = "U";
  public static final String ACT_DELETE = "D";
  public static final String ACT_PROCESS_SELECTED = "S";

  private int state;

  private String action;

  private String massAction;

  private String[] gridchk;

  /**
   * Checks if the model's primary keys have all been assigned
   * 
   * @return
   */
  public abstract boolean allKeysAssigned();

  /**
   * Gets the logical description of this model
   * 
   * @return
   */
  public abstract String getRecordDescription();

  /**
   * Adds the keys to the mv
   * 
   * @param mv
   */
  public abstract void addKeyParameters(ModelAndView mv);

  /**
   * Adds the keys to the model
   * 
   * @param map
   */
  public abstract void addKeyParameters(ModelMap map);

  public int getState() {
    return state;
  }

  public void setState(int state) {
    this.state = state;
  }

  public String getAction() {
    return action;
  }

  public void setAction(String action) {
    this.action = action;
  }

  public String[] getGridchk() {
    return gridchk;
  }

  public void setGridchk(String[] gridchk) {
    this.gridchk = gridchk;
  }

  public String getMassAction() {
    return massAction;
  }

  public void setMassAction(String massAction) {
    this.massAction = massAction;
  }
}
