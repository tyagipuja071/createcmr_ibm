/**
 * 
 */
package com.ibm.cio.cmr.request.model.code;

import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.cio.cmr.request.model.BaseModel;

/**
 * @author Jeffrey Zamora
 * 
 */
public class DefaultApprovalRecipentsModel extends BaseModel {

  private static final long serialVersionUID = 1L;
  private long defaultApprovalId;
  private String intranetId;
  private String notesId;
  private String displayName;
  private boolean newEntry;

  @Override
  public boolean allKeysAssigned() {
    return false;
  }

  @Override
  public String getRecordDescription() {
    return "Default Approval Recipient";
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

  public String getIntranetId() {
    return intranetId;
  }

  public void setIntranetId(String intranetId) {
    this.intranetId = intranetId;
  }

  public String getNotesId() {
    return notesId;
  }

  public void setNotesId(String notesId) {
    this.notesId = notesId;
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public boolean isNewEntry() {
    return newEntry;
  }

  public void setNewEntry(boolean newEntry) {
    this.newEntry = newEntry;
  }

}
