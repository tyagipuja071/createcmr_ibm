/**
 * 
 */
package com.ibm.cio.cmr.request.model.code;

import org.apache.commons.lang.StringUtils;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.cio.cmr.request.model.BaseModel;

/**
 * @author Jeffrey Zamora
 * 
 */
public class SCCModel extends BaseModel {

  private static final long serialVersionUID = 1L;

  private String nCity;

  private String nSt;

  private String nCnty;

  private float cZip;

  @Override
  public boolean allKeysAssigned() {
    return !StringUtils.isBlank(this.nCity) && !StringUtils.isBlank(this.nCnty) && !StringUtils.isBlank(this.nSt);
  }

  @Override
  public String getRecordDescription() {
    return "SCC Entry";
  }

  @Override
  public void addKeyParameters(ModelAndView mv) {
  }

  @Override
  public void addKeyParameters(ModelMap map) {
  }

  public String getnCity() {
    return nCity;
  }

  public void setnCity(String nCity) {
    this.nCity = nCity;
  }

  public String getnSt() {
    return nSt;
  }

  public void setnSt(String nSt) {
    this.nSt = nSt;
  }

  public String getnCnty() {
    return nCnty;
  }

  public void setnCnty(String nCnty) {
    this.nCnty = nCnty;
  }

  public float getcZip() {
    return cZip;
  }

  public void setcZip(float cZip) {
    this.cZip = cZip;
  }

}
