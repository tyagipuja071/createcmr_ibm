/**
 * 
 */
package com.ibm.cio.cmr.request.model.code;

import org.apache.commons.lang3.StringUtils;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.cio.cmr.request.model.BaseModel;

/**
 * @author Jeffrey Zamora
 * 
 */
public class SCCModel extends BaseModel {

  private static final long serialVersionUID = 1L;
  private long sccId;

  public long getSccId() {
    return sccId;
  }

  public void setSccId(long sccId) {
    this.sccId = sccId;
  }

  private String nCity;

  private String nSt;

  private String nCnty;

  private int cZip;

  private float cCnty;

  private float cCity;

  private float cSt;

  private String nLand;

  private String cLand;

  @Override
  public boolean allKeysAssigned() {

    boolean blnAllKeys = false;
    if (this.sccId > 0) {
      blnAllKeys = true;
    } else if (!StringUtils.isBlank(this.nCity) && !StringUtils.isBlank(this.nCnty) && !StringUtils.isBlank(this.nSt)) {
      blnAllKeys = true;
    }
    return blnAllKeys;
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

  public int getcZip() {
    return cZip;
  }

  public void setcZip(int cZip) {
    this.cZip = cZip;
  }

  public float getcCnty() {
    return cCnty;
  }

  public void setcCnty(float cCnty) {
    this.cCnty = cCnty;
  }

  public void setcCity(float cCity) {
    this.cCity = cCity;
  }

  public float getcCity() {
    return cCity;
  }

  public float getcSt() {
    return cSt;
  }

  public void setcSt(float cSt) {
    this.cSt = cSt;
  }

  public void setnLand(String nLand) {
    this.nLand = nLand;
  }

  public String getnLand() {
    return nLand;
  }

  public String getcLand() {
    return cLand;
  }

  public void setcLand(String cLand) {
    this.cLand = cLand;
  }
}
