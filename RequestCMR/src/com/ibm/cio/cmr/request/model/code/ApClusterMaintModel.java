package com.ibm.cio.cmr.request.model.code;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.cio.cmr.request.model.BaseModel;

/**
 * 
 * @author PoojaTyagi
 *
 */

public class ApClusterMaintModel extends BaseModel {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  private String issuingCntry;
  private List<String> items = new ArrayList<>();

  @Override
  public boolean allKeysAssigned() {
    boolean allKeysAssigned = false;
    if (!StringUtils.isEmpty(this.issuingCntry)) {
      allKeysAssigned = true;
    }
    return allKeysAssigned;
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

  public String getIssuingCntry() {
    return issuingCntry;
  }

  public void setIssuingCntry(String issuingCntry) {
    this.issuingCntry = issuingCntry;
  }

  public List<String> getItems() {
    return items;
  }

  public void setItems(List<String> items) {
    this.items = items;
  }

}
