/**
 * 
 */
package com.ibm.cio.cmr.request.model.code;

import java.util.Date;

import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.cio.cmr.request.model.BaseModel;

/**
 * @author Jeffrey Zamora
 * 
 */
public class DefaultApprovalModel extends BaseModel {

  private static final long serialVersionUID = 1L;

  private long defaultApprovalId;

  private int typId;

  private String typDesc;

  private String geoCd;

  private String defaultApprovalDesc;

  private String requestTyp;

  private Date createTs;

  private String createBy;

  private Date lastUpdtTs;

  private String lastUpdtBy;

  private String approvalMailContent;

  private String approvalMailSubject;

  private String approvalMailBody;

  @Override
  public boolean allKeysAssigned() {
    return this.defaultApprovalId > 0;
  }

  @Override
  public String getRecordDescription() {
    return "Default Approval";
  }

  @Override
  public void addKeyParameters(ModelAndView mv) {
  }

  @Override
  public void addKeyParameters(ModelMap map) {
  }

  public long getDefaultApprovalId() {
    return defaultApprovalId;
  }

  public void setDefaultApprovalId(long defaultApprovalId) {
    this.defaultApprovalId = defaultApprovalId;
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

  public String getDefaultApprovalDesc() {
    return defaultApprovalDesc;
  }

  public void setDefaultApprovalDesc(String defaultApprovalDesc) {
    this.defaultApprovalDesc = defaultApprovalDesc;
  }

  public String getRequestTyp() {
    return requestTyp;
  }

  public void setRequestTyp(String requestTyp) {
    this.requestTyp = requestTyp;
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

  public String getTypDesc() {
    return typDesc;
  }

  public void setTypDesc(String typDesc) {
    this.typDesc = typDesc;
  }

  public String getApprovalMailContent() {
    return approvalMailContent;
  }

  public void setApprovalMailContent(String approvalMailContent) {
    this.approvalMailContent = approvalMailContent;
  }

  public String getApprovalMailSubject() {
    return approvalMailSubject;
  }

  public void setApprovalMailSubject(String approvalMailSubject) {
    this.approvalMailSubject = approvalMailSubject;
  }

  public String getApprovalMailBody() {
    return approvalMailBody;
  }

  public void setApprovalMailBody(String approvalMailBody) {
    this.approvalMailBody = approvalMailBody;
  }

}
