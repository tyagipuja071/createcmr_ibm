/**
 * 
 */
package com.ibm.cio.cmr.request.model.test;

import javax.persistence.Transient;

import org.apache.commons.lang.StringUtils;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.cio.cmr.request.model.BaseModel;
import com.ibm.cio.cmr.request.ui.UIMgr;

/**
 * @author Jeffrey Zamora
 * 
 */
public class SadrModel extends BaseModel {

  private static final long serialVersionUID = 1L;

  private String mandt;

  private String adrnr;

  private String natio;

  private String name1;

  private String name2;

  private String name3;

  @Transient
  private String facesInput;

  @Transient
  private String facesInputId;

  public String getMandt() {
    return mandt;
  }

  public void setMandt(String mandt) {
    this.mandt = mandt;
  }

  public String getAdrnr() {
    return adrnr;
  }

  public void setAdrnr(String adrnr) {
    this.adrnr = adrnr;
  }

  public String getNatio() {
    return natio;
  }

  public void setNatio(String natio) {
    this.natio = natio;
  }

  public String getName1() {
    return name1;
  }

  public void setName1(String name1) {
    this.name1 = name1;
  }

  public String getName2() {
    return name2;
  }

  public void setName2(String name2) {
    this.name2 = name2;
  }

  public String getName3() {
    return name3;
  }

  public void setName3(String name3) {
    this.name3 = name3;
  }

  @Override
  public boolean allKeysAssigned() {
    return !StringUtils.isEmpty(this.adrnr) && !StringUtils.isEmpty(this.natio) && !StringUtils.isEmpty(this.mandt);
  }

  @Override
  public String getRecordDescription() {
    return UIMgr.getText("record.sadr");
  }

  @Override
  public void addKeyParameters(ModelAndView mv) {
    mv.addObject("adrnr", this.adrnr);
    mv.addObject("mandt", this.mandt);
    mv.addObject("natio", this.natio);
  }

  @Override
  public void addKeyParameters(ModelMap model) {
    model.addAttribute("adrnr", this.adrnr);
    model.addAttribute("mandt", this.mandt);
    model.addAttribute("natio", this.natio);
  }

  public String getFacesInput() {
    return facesInput;
  }

  public void setFacesInput(String facesInput) {
    this.facesInput = facesInput;
  }

  public String getFacesInputId() {
    return facesInputId;
  }

  public void setFacesInputId(String facesInputId) {
    this.facesInputId = facesInputId;
  }

}
