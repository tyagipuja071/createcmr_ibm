/**
 * 
 */
package com.ibm.cio.cmr.request.model.code;

import org.apache.commons.lang.StringUtils;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.cio.cmr.request.model.BaseModel;

/**
 * @author RoopakChugh
 * 
 */
public class CnaeModel extends BaseModel {

  private static final long serialVersionUID = 1L;

  private String cnaeNo;

  private String cnaeDescrip;

  private String isicCd;

  private String isuCd;

  private String subIndustryCd;

  @Override
  public boolean allKeysAssigned() {
    return !StringUtils.isBlank(this.cnaeNo);
  }

  @Override
  public String getRecordDescription() {
    return "CNAE Entry";
  }

  @Override
  public void addKeyParameters(ModelAndView mv) {
  }

  @Override
  public void addKeyParameters(ModelMap map) {
  }

  public String getCnaeNo() {
    return cnaeNo;
  }

  public void setCnaeNo(String cnaeNo) {
    this.cnaeNo = cnaeNo;
  }

  public String getCnaeDescrip() {
    return cnaeDescrip;
  }

  public void setCnaeDescrip(String cnaeDescrip) {
    this.cnaeDescrip = cnaeDescrip;
  }

  public String getIsicCd() {
    return isicCd;
  }

  public void setIsicCd(String isicCd) {
    this.isicCd = isicCd;
  }

  public String getIsuCd() {
    return isuCd;
  }

  public void setIsuCd(String isuCd) {
    this.isuCd = isuCd;
  }

  public String getSubIndustryCd() {
    return subIndustryCd;
  }

  public void setSubIndustryCd(String subIndustryCd) {
    this.subIndustryCd = subIndustryCd;
  }

}
