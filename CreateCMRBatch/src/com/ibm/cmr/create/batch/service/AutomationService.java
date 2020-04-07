/**
 * 
 */
package com.ibm.cmr.create.batch.service;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.automation.AutomationEngine;
import com.ibm.cio.cmr.request.automation.RequestData;
import com.ibm.cio.cmr.request.automation.util.AutomationConst;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.approval.ApprovalService;
import com.ibm.cio.cmr.request.util.RequestUtils;
import com.ibm.cio.cmr.request.util.SystemParameters;
import com.ibm.cio.cmr.request.util.SystemUtil;
import com.ibm.cmr.create.batch.util.AutomationCheckAndRecover;

/**
 * Executes the automation engine and processes all pending records
 * 
 * @author JeffZAMORA
 * 
 */

public class AutomationService extends MultiThreadedBatchService {

  private static final Logger LOG = Logger.getLogger(AutomationService.class);
  private static Map<String, AutomationEngine> engines = new HashMap<String, AutomationEngine>();
  private static Map<String, AutomationCheckAndRecover> recovery = new HashMap<String, AutomationCheckAndRecover>();

  @Override
  protected Queue<Long> getRequestsToProcess(EntityManager entityManager) {
    LinkedList<Long> pendingList = new LinkedList<>();

    String sql = ExternalizedQuery.getSql("AUTOMATION.GET_PENDING");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setForReadOnly(true);
    List<Long> reqIds = query.getResults(Long.class);
    if (reqIds != null) {
      pendingList.addAll(reqIds);
    }
    return pendingList;
  }

  @Override
  public Boolean executeBatchForRequests(EntityManager entityManager, List<Long> requests) throws Exception {
    AutomationEngine engine = null;

    Timestamp current = SystemUtil.getActualTimestamp();
    for (Long id : requests) {
      entityManager.clear();
      LOG.info("Processing request " + id);

      try {
        LOG.debug("Getting request data for request " + id);
        RequestData requestData = new RequestData(entityManager, id);
        if (requestData.getAdmin() == null || requestData.getData() == null) {
          throw new IllegalArgumentException("The records for reqId " + id + " cannot be found");
        }
        if (AutomationConst.STATUS_AWAITING_REPLIES.equals(requestData.getAdmin().getReqStatus())) {

          processApprovalNextStep(entityManager, requestData, current);

        } else {
          if (checkAndRecover(entityManager, requestData)) {

            boolean proceedWithExecution = true;

            if (AutomationConst.STATUS_AUTOMATED_PROCESSING_RETRY.equals(requestData.getAdmin().getReqStatus())) {
              // check if the request should be retried
              if (!shouldRetry(requestData.getAdmin().getLastUpdtTs(), current)) {
                LOG.debug("Request ID " + id + " is for retry but is not yet within the retry threshold, skipping.");
                proceedWithExecution = false;
              }
            }
            if (proceedWithExecution) {
              engine = initAutomationEngine(entityManager, requestData.getData());
              engine.runAutomationEngine(entityManager, id, requestData);
            }
          } else {
            LOG.info("Request ID " + id + " has been moved to the next status as part of workflow recovery.");
          }
        }
        LOG.debug("Committing transactions for Request " + id);
        partialCommit(entityManager);
      } catch (Exception e) {
        addError(e);
        LOG.error("An error occurred during the execution of the automation engine for Request " + id, e);
        LOG.debug("Rolling back transactions for Request " + id);
        partialRollback(entityManager);
      }
    }

    return true;
  }

  /**
   * Check awaiting replies requests which did not move properly to next step
   * 
   * @param entityManager
   * @param requestData
   * @param current
   * @throws CmrException
   * @throws SQLException
   */
  private void processApprovalNextStep(EntityManager entityManager, RequestData requestData, Timestamp current) throws CmrException, SQLException {
    Admin admin = requestData.getAdmin();
    LOG.debug("Checking awaiting replies request " + admin.getId().getReqId() + "..");

    // check if the last approval was at least 10 mins back -> removed for now
    String sql = ExternalizedQuery.getSql("APPROVAL.GET_REQ_STATUS");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", admin.getId().getReqId());
    int count = query.getSingleResult(Integer.class);
//    Date maxDt = query.getSingleResult(Date.class);
    Calendar cal = new GregorianCalendar();
    cal.setTime(current);
    cal.add(Calendar.MINUTE, -1 * 10);
    ApprovalService approvalService = new ApprovalService();
    //if (maxDt.after(cal.getTime())) {
    if(count > 0){
      // rejection
      LOG.debug("Rejecting the Request " + admin.getId().getReqId() + " and moving back to requester..");
      approvalService.moveBackToRequester(entityManager, admin);
    }
    else{
      LOG.debug("Moving Request " + admin.getId().getReqId() + " to next step..");
      if("Y".equalsIgnoreCase(admin.getReviewReqIndc())){
        LOG.debug("Setting the request status to " + admin.getId().getReqId() + " because review required indicator is set to Y.");
        admin.setReqStatus("PPN");
      }
      approvalService.moveToNextStep(entityManager, admin);
    }     
//    }
  }

  /**
   * Checks if the timestamp qualifies for a retry
   * 
   * @param lastUpdateTs
   * @return
   */
  private boolean shouldRetry(Date lastUpdateTs, Date current) {
    int retryTime = SystemParameters.getInt("ENGINE_RETRY_CYCLE");
    if (retryTime <= 0) {
      retryTime = 10;
    }
    Calendar cal = new GregorianCalendar();
    cal.setTime(current);
    cal.add(Calendar.MINUTE, -1 * retryTime);
    if (lastUpdateTs.after(cal.getTime())) {
      return false;
    }
    return true;
  }

  public static void main(String[] args) {
    Calendar cal = new GregorianCalendar();
    cal.setTime(new Date());
    System.out.println(cal.getTime());
    cal.add(Calendar.MINUTE, -1 * 30);
    System.out.println(cal.getTime());
  }

  /**
   * Checks if the country is still enabled for automated processing and returns
   * true. If not, the process will move the request to the direction specified
   * in recovery
   * 
   * @param entityManager
   * @param country
   * @param reqId
   * @return
   * @throws SQLException
   * @throws CmrException
   */
  private boolean checkAndRecover(EntityManager entityManager, RequestData requestData) throws CmrException, SQLException {

    String country = requestData.getData().getCmrIssuingCntry();

    AutomationCheckAndRecover checkAndRecover = null;
    synchronized (this) {
      if (!recovery.containsKey(country)) {
        String sql = ExternalizedQuery.getSql("AUTOMATION.CHECK_AND_RECOVER");
        PreparedQuery query = new PreparedQuery(entityManager, sql);
        query.setForReadOnly(true);
        query.setParameter("CNTRY", country);
        List<Object[]> results = query.getResults();
        if (results != null && !results.isEmpty()) {
          checkAndRecover = new AutomationCheckAndRecover();
          checkAndRecover.setCountry(country);
          checkAndRecover.setAutomationIndicator((String) results.get(0)[0]);
          checkAndRecover.setRecoveryDirection((String) results.get(0)[1]);
          recovery.put(country, checkAndRecover);
        }
      }
    }
    checkAndRecover = recovery.get(country);
    if (AutomationConst.AUTOMATE_PROCESSOR.equals(checkAndRecover.getAutomationIndicator())
        || AutomationConst.AUTOMATE_BOTH.equals(checkAndRecover.getAutomationIndicator())) {
      return true;
    } else {
      // recover and return false
      Admin admin = requestData.getAdmin();
      String direction = checkAndRecover.getRecoveryDirection();
      if (AutomationConst.RECOVERY_BACKWARD.equals(direction)) {
        // return to requester with mail
        String comment = "Your request needs to be manually submitted again to continue with the processing. This is not a rejection of the request but part of a system recovery process.";

        admin.setLastProcCenterNm(null);
        admin.setLockInd("Y");
        admin.setLockByNm(admin.getRequesterNm());
        admin.setLockBy(admin.getRequesterId());
        admin.setLockTs(SystemUtil.getCurrentTimestamp());
        admin.setReqStatus("DRA");
        entityManager.merge(admin);

        RequestUtils.createWorkflowHistoryFromBatch(entityManager, BATCH_USER_ID, admin, comment, "System Recovery", null, null, false, true, null);
      } else {
        // forward to processor without mail
        String comment = "Sending to processors as part of a system recovery procedure.";
        String processingCenter = RequestUtils.getProcessingCenter(entityManager, country);

        admin.setLastProcCenterNm(processingCenter);
        admin.setLockInd("N");
        admin.setLockByNm(null);
        admin.setLockBy(null);
        admin.setLockTs(null);
        admin.setReqStatus("PPN");
        entityManager.merge(admin);

        RequestUtils.createWorkflowHistoryFromBatch(entityManager, BATCH_USER_ID, admin, comment, "System Recovery", processingCenter, null, false,
            false, null);
      }
      return false;
    }

  }

  private synchronized AutomationEngine initAutomationEngine(EntityManager entityManager, Data data) {
    String country = data.getCmrIssuingCntry();
    String subRegion = data.getCountryUse();
    if (!StringUtils.isBlank(subRegion) && subRegion.startsWith(country)) {
      // this is a subregion country, use its key to initialize the engine
      country = subRegion;
    }
    if (engines.containsKey(country)) {
      LOG.trace("Engine retrieved from engine cache.");
      return engines.get(country);
    }
    AutomationEngine engine = new AutomationEngine(entityManager, country);
    engines.put(country, engine);
    return engine;
  }

  @Override
  protected String getThreadName() {
    return "AutomationService";
  }

  @Override
  public boolean isTransactional() {
    return true;
  }

  @Override
  protected boolean useServicesConnections() {
    return true;
  }

}
