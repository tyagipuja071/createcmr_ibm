package com.ibm.cio.cmr.request.model.code;

import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.cio.cmr.request.model.BaseModel;

/**
 * 
 * @author Anuja Srivastava
 *
 */

public class RepTeamModel extends BaseModel {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  private String issuingCntry;
  private String repTeamCd;
  private String repTeamMemberNo;
  private String repTeamMemberName;
  private String createById;
  private Date createTs;
  private String updtById;
  private Date updtTs;

  @Override
  public boolean allKeysAssigned() {
    boolean allKeysAssigned = false;
    if (!StringUtils.isEmpty(this.issuingCntry) && !StringUtils.isEmpty(this.repTeamCd) && !StringUtils.isEmpty(this.repTeamMemberNo)) {
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

  public void setIssuingCntry(String issuingCntry) {
    this.issuingCntry = issuingCntry;
  }

  public String getRepTeamCd() {
    return repTeamCd;
  }

  public void setRepTeamCd(String repTeamCd) {
    this.repTeamCd = repTeamCd;
  }

  public String getCreateById() {
    return createById;
  }

  public void setCreateById(String createById) {
    this.createById = createById;
  }

  public Date getCreateTs() {
    return createTs;
  }

  public void setCreateTs(Date createTs) {
    this.createTs = createTs;
  }

  public String getUpdtById() {
    return updtById;
  }

  public void setUpdtById(String updtById) {
    this.updtById = updtById;
  }

  public Date getUpdtTs() {
    return updtTs;
  }

  public void setUpdtTs(Date updtTs) {
    this.updtTs = updtTs;
  }

}
