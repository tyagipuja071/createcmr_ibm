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
public class DefaultApprovalConditionsModel extends BaseModel {

  private static final long serialVersionUID = 1L;

  private long defaultApprovalId;
  private int sequenceNo;
  private String addrIndc;
  private int conditionLevel;
  private String fieldId;
  private String databaseFieldName;
  private String operator;
  private String value;
  private String previousValueIndc;
  private boolean edit;
  private boolean newEntry;

  @Override
  public boolean allKeysAssigned() {
    return false;
  }

  @Override
  public String getRecordDescription() {
    return "Default Approval Condition";
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

  public int getSequenceNo() {
    return sequenceNo;
  }

  public void setSequenceNo(int sequenceNo) {
    this.sequenceNo = sequenceNo;
  }

  public String getAddrIndc() {
    return addrIndc;
  }

  public void setAddrIndc(String addrIndc) {
    this.addrIndc = addrIndc;
  }

  public int getConditionLevel() {
    return conditionLevel;
  }

  public void setConditionLevel(int conditionLevel) {
    this.conditionLevel = conditionLevel;
  }

  public String getFieldId() {
    return fieldId;
  }

  public void setFieldId(String fieldId) {
    this.fieldId = fieldId;
  }

  public String getDatabaseFieldName() {
    return databaseFieldName;
  }

  public void setDatabaseFieldName(String databaseFieldName) {
    this.databaseFieldName = databaseFieldName;
  }

  public String getOperator() {
    return operator;
  }

  public void setOperator(String operator) {
    this.operator = operator;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public String getPreviousValueIndc() {
    return previousValueIndc;
  }

  public void setPreviousValueIndc(String previousValueIndc) {
    this.previousValueIndc = previousValueIndc;
  }

  public boolean isEdit() {
    return edit;
  }

  public void setEdit(boolean edit) {
    this.edit = edit;
  }

  public boolean isNewEntry() {
    return newEntry;
  }

  public void setNewEntry(boolean newEntry) {
    this.newEntry = newEntry;
  }

}
