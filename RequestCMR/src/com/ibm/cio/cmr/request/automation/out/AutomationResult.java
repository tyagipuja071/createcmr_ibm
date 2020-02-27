/**
 * 
 */
package com.ibm.cio.cmr.request.automation.out;

import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.automation.AutomationElement;
import com.ibm.cio.cmr.request.automation.ProcessType;
import com.ibm.cio.cmr.request.automation.RequestData;
import com.ibm.cio.cmr.request.entity.AutomationResults;
import com.ibm.cio.cmr.request.entity.AutomationResultsPK;
import com.ibm.cio.cmr.request.user.AppUser;
import com.ibm.cio.cmr.request.util.SystemUtil;

/**
 * Represents the results of an {@link AutomationElement} processing.
 * 
 * @author JeffZAMORA
 * 
 */
public class AutomationResult<R extends AutomationOutput> {

  private static final Logger LOG = Logger.getLogger(AutomationResult.class);
  private long reqId;
  private ProcessType processType;
  private String processCode;
  private String processDesc;
  private String results;
  private String details;
  private boolean onError;

  private R processOutput;

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
  public void record(EntityManager entityManager, long resultId, AppUser user, RequestData requestData, String failureType) throws Exception {
    AutomationResults record = new AutomationResults();
    AutomationResultsPK pk = new AutomationResultsPK();

    long reqId = requestData.getAdmin().getId().getReqId();
    pk.setAutomationResultId(resultId);
    pk.setProcessCd(this.processCode);
    pk.setReqId(reqId);
    record.setId(pk);

    record.setCreateBy(user.getIntranetId());
    record.setCreateTs(SystemUtil.getActualTimestamp());
    if (StringUtils.isBlank(this.results)) {
      this.results = "(cannot record results)";
    }
    if (StringUtils.isBlank(this.details)) {
      this.details = this.results;
    }
    this.details = this.details.trim();
    int lengthInBytes = 0;
    lengthInBytes = this.details.getBytes("UTF-8").length;
    if (this.details != null && (this.details.length() > 1000 || lengthInBytes > 1000)) {
      if (lengthInBytes > this.details.length()) {
        record.setDetailedResults(this.details.substring(0, (995 - (lengthInBytes - this.details.length()))) + "...");
      } else {
        record.setDetailedResults(this.details.substring(0, 995) + "...");
      }
    } else {
      record.setDetailedResults(this.details);
    }
    record.setProcessDesc(this.processDesc);
    if (this.results.length() > 50) {
      record.setProcessResult(this.results.substring(0, 49));
    } else {
      record.setProcessResult(this.results);
    }

    record.setProcessTyp(this.processType.toCode());

    record.setFailureIndc(failureType);

    LOG.debug("Creating results record for Request: " + reqId + " Process: " + this.processDesc + " (" + this.processCode + ")");
    entityManager.persist(record);
    entityManager.flush();

    if (this.processOutput != null) {
      this.processOutput.recordData(entityManager, resultId, user, requestData);
    }

  }

  public long getReqId() {
    return reqId;
  }

  public void setReqId(long reqId) {
    this.reqId = reqId;
  }

  public String getProcessCode() {
    return processCode;
  }

  public void setProcessCode(String processCode) {
    this.processCode = processCode;
  }

  public String getResults() {
    return results;
  }

  public void setResults(String results) {
    this.results = results;
  }

  public String getDetails() {
    return details;
  }

  public void setDetails(String details) {
    this.details = details;
  }

  public ProcessType getProcessType() {
    return processType;
  }

  public void setProcessType(ProcessType processType) {
    this.processType = processType;
  }

  public String getProcessDesc() {
    return processDesc;
  }

  public void setProcessDesc(String processDesc) {
    this.processDesc = processDesc;
  }

  public boolean isOnError() {
    return onError;
  }

  public void setOnError(boolean onError) {
    this.onError = onError;
  }

  public R getProcessOutput() {
    return processOutput;
  }

  public void setProcessOutput(R processOutput) {
    this.processOutput = processOutput;
  }

}
