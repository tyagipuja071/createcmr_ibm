/**
 * 
 */
package com.ibm.cio.cmr.request.model.approval;

import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.cio.cmr.request.model.BaseModel;

/**
 * @author JeffZAMORA
 *
 */
public class ApprovalTypeModel extends BaseModel {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  private int typId;

  private String geoCd;

  private String title;

  private String description;

  private String templateName;

  private Date createTs;

  private String createBy;

  private Date lastUpdtTs;

  private String lastUpdtBy;

  private String grpApprovalIndc;

  @Override
  public boolean allKeysAssigned() {
    return this.typId > 0 && !StringUtils.isBlank(this.geoCd);
  }

  @Override
  public String getRecordDescription() {
    return "Approval Type";
  }

  @Override
  public void addKeyParameters(ModelAndView mv) {
    mv.addObject("typeId", this.typId);
    mv.addObject("geoCd", this.geoCd);
  }

  @Override
  public void addKeyParameters(ModelMap map) {
    map.put("typeId", this.typId);
    map.put("geoCd", this.geoCd);
  }

  public int getTypId() {
    return typId;
  }

  public void setTypId(int typId) {
    this.typId = typId;
  }

  public String getGeoCd() {
    return geoCd;
  }

  public void setGeoCd(String geoCd) {
    this.geoCd = geoCd;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getTemplateName() {
    return templateName;
  }

  public void setTemplateName(String templateName) {
    this.templateName = templateName;
  }

  public Date getCreateTs() {
    return createTs;
  }

  public void setCreateTs(Date createTs) {
    this.createTs = createTs;
  }

  public String getCreateBy() {
    return createBy;
  }

  public void setCreateBy(String createBy) {
    this.createBy = createBy;
  }

  public Date getLastUpdtTs() {
    return lastUpdtTs;
  }

  public void setLastUpdtTs(Date lastUpdtTs) {
    this.lastUpdtTs = lastUpdtTs;
  }

  public String getLastUpdtBy() {
    return lastUpdtBy;
  }

  public void setLastUpdtBy(String lastUpdtBy) {
    this.lastUpdtBy = lastUpdtBy;
  }

  public String getGrpApprovalIndc() {
    return grpApprovalIndc;
  }

  public void setGrpApprovalIndc(String grpApprovalIndc) {
    this.grpApprovalIndc = grpApprovalIndc;
  }

}
