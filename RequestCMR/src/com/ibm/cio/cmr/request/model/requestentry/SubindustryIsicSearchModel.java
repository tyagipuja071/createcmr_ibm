/**
 * 
 */
package com.ibm.cio.cmr.request.model.requestentry;

import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.cio.cmr.request.model.BaseModel;

/**
 * @author Jeffrey Zamora
 * 
 */
public class SubindustryIsicSearchModel extends BaseModel {

  private static final long serialVersionUID = 1L;

  private String subIndustryCdSearch;
  private String subIndustryDescSearch;
  private String isicCdSearch;
  private String isicDescSearch;

  @Override
  public boolean allKeysAssigned() {
    return false;
  }

  @Override
  public String getRecordDescription() {
    return "";
  }

  @Override
  public void addKeyParameters(ModelAndView mv) {
  }

  @Override
  public void addKeyParameters(ModelMap map) {
  }

  public String getSubIndustryCdSearch() {
    return subIndustryCdSearch;
  }

  public void setSubIndustryCdSearch(String subIndustryCdSearch) {
    this.subIndustryCdSearch = subIndustryCdSearch;
  }

  public String getSubIndustryDescSearch() {
    return subIndustryDescSearch;
  }

  public void setSubIndustryDescSearch(String subIndustryDescSearch) {
    this.subIndustryDescSearch = subIndustryDescSearch;
  }

  public String getIsicCdSearch() {
    return isicCdSearch;
  }

  public void setIsicCdSearch(String isicCdSearch) {
    this.isicCdSearch = isicCdSearch;
  }

  public String getIsicDescSearch() {
    return isicDescSearch;
  }

  public void setIsicDescSearch(String isicDescSearch) {
    this.isicDescSearch = isicDescSearch;
  }

}
