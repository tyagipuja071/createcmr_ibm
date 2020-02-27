package com.ibm.cio.cmr.request.model.requestentry;

import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.cio.cmr.request.model.BaseModel;

/**
 * @author Mukesh
 *
 */
public class SucursalCollBranchOffModel extends BaseModel{

  private static final long serialVersionUID = 1L;
//ISSUING_CNTRY, COLL_BO_ID, COLL_BO_DESC
  private String issuingCntrySearch;
  private String collBOIDSearch;
  private String collBODescSearch;
  

  
 
  public String getIssuingCntrySearch() {
    return issuingCntrySearch;
  }

  public void setIssuingCntrySearch(String issuingCntrySearch) {
    this.issuingCntrySearch = issuingCntrySearch;
  }

  public String getCollBOIDSearch() {
    return collBOIDSearch;
  }

  public void setCollBOIDSearch(String collBOIDSearch) {
    this.collBOIDSearch = collBOIDSearch;
  }

  public String getCollBODescSearch() {
    return collBODescSearch;
  }

  public void setCollBODescSearch(String collBODescSearch) {
    this.collBODescSearch = collBODescSearch;
  }

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
}
