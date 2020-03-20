/**
 * 
 */
package com.ibm.cio.cmr.request.model.code;

import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.cio.cmr.request.model.BaseModel;

/**
 * @author Anuja Srivastava
 *
 */
public class GermanyDeptModel extends BaseModel {
  private static final long serialVersionUID = 1L;
  // private String action;
  private String deptName;

  /*
   * @Override public String getAction() { return action; }
   * 
   * @Override public void setAction(String action) { this.action = action; }
   */

  public String getDeptName() {
    return deptName;
  }

  public void setDeptName(String deptName) {
    this.deptName = deptName;
  }

  @Override
  public boolean allKeysAssigned() {
    // TODO Auto-generated method stub
    return false;

  }

  @Override
  public String getRecordDescription() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void addKeyParameters(ModelAndView mv) {
    // TODO Auto-generated method stub

  }

  @Override
  public void addKeyParameters(ModelMap map) {
    // TODO Auto-generated method stub

  }

}
