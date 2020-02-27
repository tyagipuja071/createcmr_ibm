package com.ibm.cio.cmr.request.model.requestentry;

import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.cio.cmr.request.model.BaseModel;

/**
 * @author Mukesh
 * 
 */
public class SalesRepNameNoModel extends BaseModel {

  private static final long serialVersionUID = 1L;

  private String repTeamMemberNo;
  private String repTeamMemberName;
  private String repTeamMemberId;

  @Override
  public boolean allKeysAssigned() {
    return false;
  }

  public String getRepTeamMemberNo() {
    return repTeamMemberNo;
  }

  public void setRepTeamMemberNo(String repTeamMemberNo) {
    this.repTeamMemberNo = repTeamMemberNo;
  }

  public String getRepTeamMemberName() {
    return repTeamMemberName;
  }

  public void setRepTeamMemberName(String repTeamMemberName) {
    this.repTeamMemberName = repTeamMemberName;
  }

  public String getRepTeamMemberId() {
    return repTeamMemberId;
  }

  public void setRepTeamMemberId(String repTeamMemberId) {
    this.repTeamMemberId = repTeamMemberId;
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
