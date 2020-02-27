/**
 * 
 */
package com.ibm.cio.cmr.request.automation.out;

import javax.persistence.EntityManager;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.automation.RequestData;
import com.ibm.cio.cmr.request.entity.AutomationMatching;
import com.ibm.cio.cmr.request.entity.AutomationMatchingPK;
import com.ibm.cio.cmr.request.user.AppUser;
import com.ibm.cio.cmr.request.util.SystemUtil;

/**
 * One matched record container
 * 
 * @author JeffZAMORA
 * 
 */
public class MatchRecord {

  private int itemNo;
  private String matchKeyName;
  private String matchKeyValue;
  private String matchGradeType;
  private String matchGradeValue;
  private String recordType;
  private String processCode;
  private MatchingOutput processOutput;

  /**
   * Stores the contents of the data on this output object to the database table
   * CREQCMR.AUTOMATION_RESULTS
   * 
   * @param entityManager
   * @param resultId
   * @param requestData
   * @param result
   * @throws CmrException
   */
  public void matchedRecord(EntityManager entityManager, long resultId, AppUser user, RequestData requestData) throws Exception {
    AutomationMatching matchedRecord = new AutomationMatching();
    AutomationMatchingPK pk = new AutomationMatchingPK();

    pk.setAutomationResultId(resultId);
    pk.setProcessCd(this.processCode);
    matchedRecord.setId(pk);
    matchedRecord.setCreateBy(user.getIntranetId());
    matchedRecord.setCreateTs(SystemUtil.getActualTimestamp());
    matchedRecord.setRecordTyp(this.recordType);
    matchedRecord.setMatchGradeTyp(this.matchGradeType);
    matchedRecord.setMatchGradeValue(this.matchGradeValue);

    entityManager.persist(matchedRecord);
    entityManager.flush();

    if (this.processOutput != null) {
      this.processOutput.recordData(entityManager, resultId, user, requestData);
    }

  }

  public String getMatchKeyName() {
    return matchKeyName;
  }

  public void setMatchKeyName(String matchKeyName) {
    this.matchKeyName = matchKeyName;
  }

  public String getMatchKeyValue() {
    return matchKeyValue;
  }

  public void setMatchKeyValue(String matchKeyValue) {
    this.matchKeyValue = matchKeyValue;
  }

  public String getMatchGradeType() {
    return matchGradeType;
  }

  public void setMatchGradeType(String matchGradeType) {
    this.matchGradeType = matchGradeType;
  }

  public String getMatchGradeValue() {
    return matchGradeValue;
  }

  public void setMatchGradeValue(String matchGradeValue) {
    this.matchGradeValue = matchGradeValue;
  }

  public String getRecordType() {
    return recordType;
  }

  public void setRecordType(String recordType) {
    this.recordType = recordType;
  }

  public String getProcessCode() {
    return processCode;
  }

  public void setProcessCode(String processCode) {
    this.processCode = processCode;
  }

  public int getItemNo() {
    return itemNo;
  }

  public void setItemNo(int itemNo) {
    this.itemNo = itemNo;
  }

  public MatchingOutput getProcessOutput() {
    return processOutput;
  }

  public void setProcessOutput(MatchingOutput processOutput) {
    this.processOutput = processOutput;
  }

}
