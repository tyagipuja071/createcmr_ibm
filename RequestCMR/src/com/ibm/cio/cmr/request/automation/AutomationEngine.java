/**
 * 
 */
package com.ibm.cio.cmr.request.automation;

import java.lang.reflect.Constructor;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.automation.impl.DuplicateCheckElement;
import com.ibm.cio.cmr.request.automation.impl.ProcessWaitingElement;
import com.ibm.cio.cmr.request.automation.out.AutomationOutput;
import com.ibm.cio.cmr.request.automation.out.AutomationResult;
import com.ibm.cio.cmr.request.automation.util.AutomationConst;
import com.ibm.cio.cmr.request.automation.util.RejectionContainer;
import com.ibm.cio.cmr.request.automation.util.ScenarioExceptionsUtil;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.AutoEngineConfig;
import com.ibm.cio.cmr.request.entity.AutomationResults;
import com.ibm.cio.cmr.request.entity.AutomationResultsPK;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.entity.ReqCmtLog;
import com.ibm.cio.cmr.request.entity.ReqCmtLogPK;
import com.ibm.cio.cmr.request.entity.WfHist;
import com.ibm.cio.cmr.request.entity.WfHistPK;
import com.ibm.cio.cmr.request.entity.listeners.ChangeLogListener;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.user.AppUser;
import com.ibm.cio.cmr.request.util.RequestUtils;
import com.ibm.cio.cmr.request.util.SystemUtil;
import com.ibm.cio.cmr.request.util.geo.GEOHandler;
import com.ibm.cio.cmr.request.util.geo.impl.LAHandler;

/**
 * The engine that runs a set of {@link AutomationElement} objects. This engine
 * handles the creation of relevant records in AUTOMATION_RESULTS and
 * AUTOMATION_DATA tables based on the results of an {@link AutomationElement}
 * processing
 * 
 * @author JeffZAMORA
 * 
 */
public class AutomationEngine {

  private static final Logger LOG = Logger.getLogger(AutomationEngine.class);
  private final List<AutomationElement<?>> elements = new ArrayList<>();
  private static ThreadLocal<AutomationEngineData> engineData = new ThreadLocal<>();

  /**
   * Creates an instance of the {@link AutomationEngine}
   */
  public AutomationEngine(EntityManager entityManager, String country) {
    LOG.info("Initializing Automation Engine for Country " + country);
    // retrieve the configuration for the country, initialize the elements to
    // execute

    String sql = ExternalizedQuery.getSql("AUTOMATION.GET_ENGINE");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("CNTRY", country);
    query.setForReadOnly(true);

    List<AutoEngineConfig> configs = query.getResults(AutoEngineConfig.class);
    if (configs != null) {
      for (AutoEngineConfig config : configs) {
        LOG.trace("Trying to initialize element " + config.getProcessCd());
        AutomationElement<?> element = initializeElement(config.getProcessCd(), config.getRequestTyp(), config.getActionOnError(),
            "Y".equals(config.getOverrideDataIndc()), "Y".equals(config.getStopOnErrorIndc()));
        if (element != null) {
          this.elements.add(element);
          LOG.debug("Element " + config.getProcessCd() + " added to the engine.");
        } else {
          LOG.warn("Element " + config.getProcessCd() + " cannot be initialized on the engine.");
        }
      }
    }
  }

  @SuppressWarnings("unchecked")
  private AutomationElement<?> initializeElement(String processCode, String reqTypes, String actionOnError, boolean overrideData,
      boolean stopOnError) {
    Class<?> elementClass = AutomationElementRegistry.getInstance().get(processCode);
    if (elementClass != null) {
      try {
        Constructor<AutomationElement<?>> constructor = (Constructor<AutomationElement<?>>) elementClass.getConstructor(String.class, String.class,
            boolean.class, boolean.class);
        return constructor.newInstance(reqTypes, actionOnError, overrideData, stopOnError);
      } catch (Exception e) {
        LOG.warn("Element for process code " + processCode + " cannot be determined via registry.");
      }
    }
    return null;
  }

  /**
   * Runs the {@link AutomationEngine} and executes the supported
   * {@link AutomationElement} one by one
   * 
   * @param entityManager
   * @param reqId
   * @throws CmrException
   */
  public void runAutomationEngine(EntityManager entityManager, long reqId) throws Exception {
    RequestData requestData = new RequestData(entityManager, reqId);
    runAutomationEngine(entityManager, reqId, requestData);
  }

  /**
   * Runs the {@link AutomationEngine} and executes the supported
   * {@link AutomationElement} one by one
   * 
   * @param entityManager
   * @param reqId
   * @throws CmrException
   */
  @SuppressWarnings("unchecked")
  public void runAutomationEngine(EntityManager entityManager, long reqId, RequestData requestData) throws Exception {
    if (requestData.getAdmin() == null || requestData.getData() == null) {
      throw new IllegalArgumentException("The records for reqId " + reqId + " cannot be found");
    }
    String reqType = requestData.getAdmin().getReqType();
    String reqStatus = requestData.getAdmin().getReqStatus();
    // put the current engine data on the thread, for reuse if needed

    // check child request status first
    long childReqId = requestData.getAdmin().getChildReqId();
    if (childReqId > 0 && isChildPending(entityManager, childReqId)) {
      LOG.debug("Child request for this request is still pending. Skipping all processing.");
      return;
    }

    List<ActionOnError> actionsOnError = new ArrayList<>();
    LOG.trace("Preparing engine data..");
    engineData.remove();
    AutomationEngineData threadData = new AutomationEngineData();
    AppUser appUser = createAutomationAppUser();
    List<RejectionContainer> rejectInfo = new ArrayList<RejectionContainer>();
    threadData.put(AutomationEngineData.APP_USER, appUser);
    threadData.put(AutomationEngineData.REJECTIONS, rejectInfo);
    engineData.set(threadData);

    ChangeLogListener.setUser(appUser.getIntranetId());

    long resultId = getNextResultId(entityManager);

    createComment(entityManager, "Automated system checks have been started.", reqId, appUser);

    boolean systemError = false;
    boolean processWaiting = false;
    boolean stopExecution = false;
    int lastElementIndex = 0;

    boolean hasOverrideOrMatchingApplied = false;
    requestData.getAdmin().setReviewReqIndc("N");
    for (AutomationElement<?> element : this.elements) {
      ScenarioExceptionsUtil scenarioExceptions = element.getScenarioExceptions(entityManager, requestData, engineData.get());

      // determine if element is to be skipped

      boolean skipChecks = scenarioExceptions != null ? scenarioExceptions.isSkipChecks() : false;
      boolean skipElement = (skipChecks || engineData.get().isSkipChecks())
          && (ProcessType.StandardProcess.equals(element.getProcessType()) || ProcessType.DataOverride.equals(element.getProcessType())
              || (ProcessType.Matching.equals(element.getProcessType()) && !(element instanceof DuplicateCheckElement)));

      boolean skipVerification = scenarioExceptions != null && scenarioExceptions.isSkipCompanyVerification();
      skipVerification = skipVerification && (element instanceof CompanyVerifier);

      if (ProcessType.StandardProcess.equals(element.getProcessType())) {
        hasOverrideOrMatchingApplied = true;
      }
      // handle the special ALL types (approvals)
      if (element.getRequestTypes().contains("*") || element.getRequestTypes().contains(reqType)) {
        LOG.debug("Executing element " + element.getProcessDesc() + " for Request " + reqId);
        AutomationResult<?> result = null;

        if (skipElement) {
          result = element.createSkippedResult(reqId, "Checks skipped because of scenario exceptions and/or previous element results.");
        } else if (skipVerification) {
          result = element.createSkippedResult(reqId, "Company verification skipped for this request scenario.");
        } else {
          try {
            ChangeLogListener.setManager(entityManager);
            result = element.executeElement(entityManager, requestData, engineData.get());
            ChangeLogListener.clearManager();
          } catch (Exception e) {
            LOG.warn("System error for element " + element.getProcessDesc(), e);
            result = new AutomationResult<>();
            result.setOnError(true);
            systemError = true;
            createSystemErrorResult(entityManager, reqId, resultId, element, appUser);
            break;
          }
        }

        LOG.trace("Result for " + element.getProcessDesc() + ": " + result.getResults());
        LOG.trace(" - " + result.getDetails());
        // record the result first
        result.record(entityManager, resultId, appUser, requestData, systemError ? "S" : (result.isOnError() ? "P" : null));

        // check processing
        if ((element instanceof ProcessWaitingElement) && ((ProcessWaitingElement) element).isWaiting()) {
          LOG.debug("Element is waiting on external processes. No action for this run");
          stopExecution = true;
          processWaiting = true;
          break;
        } else if (result.isOnError()) {
          if (ActionOnError.Ignore.equals(element.getActionOnError())) {
            LOG.debug("Element " + element.getProcessDesc() + " encountered an error but was ignored.");
          } else {
            actionsOnError.add(element.getActionOnError());
            if (element.isStopOnError()) {
              LOG.info("Stopping execution of other elements because of an error in " + element.getProcessDesc());
              createComment(entityManager, "An error in execution of " + element.getProcessDesc() + " caused the process to stop.", reqId, appUser);
              stopExecution = true;
              break;
            }
          }
        } else {
          AutomationOutput output = result.getProcessOutput();
          LOG.debug("Successful execution of " + element.getProcessDesc());
          if (output != null) {
            if (element.isOverrideData() && !element.isNonImportable()) {
              switch (element.getProcessType()) {
              case DataOverride:
                LOG.debug("Applying data overrides to output..");
                output.apply(entityManager, appUser, requestData, resultId, AutomationConst.ITEM_NO_ALL, element.getProcessCode(), true);
                hasOverrideOrMatchingApplied = true;
                break;
              case Matching:
                LOG.debug("Applying highest matched results to output..");
                output.apply(entityManager, appUser, requestData, resultId, AutomationConst.ITEM_NO_FIRST, element.getProcessCode(), true);
                hasOverrideOrMatchingApplied = true;
                break;
              case StandardProcess:
                hasOverrideOrMatchingApplied = true;
                break;
              case Validation:
                break;
              case Approvals:
                break;
              }
            }
          }
        }
      } else {
        LOG.trace("Skipping element " + element.getProcessDesc() + " for request type " + reqType);
      }
      lastElementIndex++;
    }

    LOG.debug("Automation elements executed for Request " + reqId);

    Admin admin = requestData.getAdmin();
    Data data = requestData.getData();
    String compInfoSrc = (String) engineData.get().get(AutomationEngineData.COMPANY_INFO_SOURCE);
    String scenarioVerifiedIndc = (String) engineData.get().get(AutomationEngineData.SCENARIO_VERIFIED_INDC);

    if (stopExecution) {
      createStopResult(entityManager, reqId, resultId, lastElementIndex, appUser);
    }

    // check company verified info
    if (compInfoSrc != null && StringUtils.isNotBlank(compInfoSrc)) {
      admin.setCompVerifiedIndc("Y");
      admin.setCompInfoSrc(compInfoSrc);
    }
    // check scenario verified info
    if (scenarioVerifiedIndc != null && StringUtils.isNotBlank(scenarioVerifiedIndc)) {
      admin.setScenarioVerifiedIndc(scenarioVerifiedIndc);
    }

    if (processWaiting) {
      if (requestData.getAdmin().getChildReqId() > 0) {
        LOG.debug("Adding process waiting comment log.");
        createComment(entityManager, "Automated checks is waiting on child request " + requestData.getAdmin().getChildReqId() + " before proceeding.",
            reqId, appUser);
      }
    } else {
      if (systemError || "N".equalsIgnoreCase(admin.getCompVerifiedIndc())) {
        if (AutomationConst.STATUS_AUTOMATED_PROCESSING.equals(reqStatus)) {
          // change status to retry
          if ("N".equalsIgnoreCase(admin.getCompVerifiedIndc())) {
            createComment(entityManager, "Processing error encountered as data is not company verified.", reqId, appUser);
            admin.setReqStatus("PPN");
          } else {
            createComment(entityManager, "A system error occurred during the processing. A retry will be attempted shortly.", reqId, appUser);
            admin.setReqStatus(AutomationConst.STATUS_AUTOMATED_PROCESSING_RETRY);
          }

        } else {
          String processingCenter = RequestUtils.getProcessingCenter(entityManager, data.getCmrIssuingCntry());
          // change status to awaiting processing
          createComment(entityManager, "System failed to complete processing during the retry. Please wait a while then reprocess the record.", reqId,
              appUser);
          admin.setLastProcCenterNm(processingCenter);
          admin.setReqStatus(AutomationConst.STATUS_AWAITING_PROCESSING);
          createHistory(entityManager, admin, "System failed to complete processing during the retry. Please wait a while then reprocess the record.",
              AutomationConst.STATUS_AWAITING_PROCESSING, "Automated Processing", reqId, appUser, null, null, false, null);
        }
      } else {
        boolean moveToNextStep = true;
        if (!actionsOnError.isEmpty()) {
          // an error has occurred
          if (actionsOnError.contains(ActionOnError.Reject)) {
            moveToNextStep = false;
            // reject the record
            StringBuilder rejCmtBuilder = new StringBuilder();
            rejCmtBuilder.append("The record has been rejected due to some system processing errors");
            rejectInfo = (List<RejectionContainer>) engineData.get().get("rejections");
            if (rejectInfo != null && !rejectInfo.isEmpty()) {
              rejCmtBuilder.append(":");
              for (RejectionContainer rejCont : rejectInfo) {
                rejCmtBuilder.append(rejCont.getRejComment());
              }
            } else {
              rejCmtBuilder.append(".");
            }
            String cmt = rejCmtBuilder.toString();
            if (cmt.length() > 1000) {
              cmt = cmt.substring(0, 1996) + "...";
            }
            admin.setReqStatus("PRJ");
            for (RejectionContainer r : rejectInfo) {
              createHistory(entityManager, admin, cmt, "PRJ", "Automated Processing", reqId, appUser, null, "Errors in automated checks", true, r);
            }
            admin.setLastProcCenterNm(null);
            admin.setLockInd("N");
            admin.setLockByNm(null);
            admin.setLockBy(null);
            admin.setLockTs(null);
            createComment(entityManager, cmt, reqId, appUser);

          } else if (actionsOnError.contains(ActionOnError.Wait)) {
            // do not change status
            moveToNextStep = false;
            String cmt = "";
            StringBuilder rejectCmt = new StringBuilder();
            List<RejectionContainer> rejectionInfo = (List<RejectionContainer>) engineData.get().get("rejections");
            Map<String, String> pendingChecks = (Map<String, String>) engineData.get().get(AutomationEngineData.NEGATIVE_CHECKS);
            if ((rejectionInfo != null && !rejectionInfo.isEmpty()) || (pendingChecks != null && !pendingChecks.isEmpty())) {
              rejectCmt.append("Processor review is required for following issues");
              rejectCmt.append(":");
              if (rejectionInfo != null && !rejectionInfo.isEmpty()) {
                for (RejectionContainer rejCont : rejectionInfo) {
                  rejectCmt.append(rejCont.getRejComment() + "\n");
                }
              }
              // append pending checks
              if (pendingChecks != null && !pendingChecks.isEmpty()) {
                for (String pendingCheck : pendingChecks.values()) {
                  rejectCmt.append("\n ");
                  rejectCmt.append(pendingCheck);
                }
              }
              cmt = rejectCmt.toString();

              if (cmt.length() > 1930) {
                cmt = cmt.substring(0, 1920) + "...";
              }
              cmt += "\n\nPlease view system processing results for more details.";
              admin.setReviewReqIndc("Y");
              createComment(entityManager, cmt, reqId, appUser);
            }
            if (actionsOnError.size() > 1) {
              admin.setReviewReqIndc("Y");
            }
            cmt = "Automated checks indicate that external processes are needed to move this request to the next step.";
            createComment(entityManager, cmt, reqId, appUser);
            admin.setReqStatus(AutomationConst.STATUS_AWAITING_REPLIES);
            createHistory(entityManager, admin, cmt, AutomationConst.STATUS_AWAITING_REPLIES, "Automated Processing", reqId, appUser, null, null,
                false, null);
          }
        }
        if (moveToNextStep) {

          // if there is anything that changed on the request via automated
          // import
          // of overrides/match/standard output, do a save

          if (hasOverrideOrMatchingApplied) {
            GEOHandler geoHandler = RequestUtils.getGEOHandler(data.getCmrIssuingCntry());
            if (geoHandler != null) {

              LOG.debug("Performing GEO Handler saves on request..");

              geoHandler.doBeforeAdminSave(entityManager, admin, data.getCmrIssuingCntry());

              if (LAHandler.isLACountry(data.getCmrIssuingCntry())) {
                // TODO - check if this is needed
                // geoHandler.setDataDefaultsOnCreate(data, entityManager);
                // geoHandler.doBeforeDataSave(entityManager, admin, data,
                // data.getCmrIssuingCntry());
              } else {
                // geoHandler.doBeforeDataSave(entityManager, admin, data,
                // data.getCmrIssuingCntry());
              }
            }
          }

          // check configured setting for what to do upon completion
          boolean processOnCompletion = isProcessOnCompletion(entityManager, data.getCmrIssuingCntry(), admin.getReqType());

          // if any element reported an error, it should always be reviewed by
          // processor
          processOnCompletion = processOnCompletion && actionsOnError.isEmpty();

          String processingCenter = RequestUtils.getProcessingCenter(entityManager, data.getCmrIssuingCntry());
          admin.setLastProcCenterNm(processingCenter);
          admin.setLockInd("N");
          admin.setLockByNm(null);
          admin.setLockBy(null);
          admin.setLockTs(null);
          admin.setProcessedFlag("N");
          Map<String, String> pendingChecks = (Map<String, String>) engineData.get().get(AutomationEngineData.NEGATIVE_CHECKS);
          pendingChecks = pendingChecks != null ? pendingChecks : new HashMap<String, String>();
          if (processOnCompletion && (pendingChecks == null || pendingChecks.isEmpty())) {
            // move to PCP
            admin.setReqStatus("PCP");
            String cmt = "Automated checks completed successfully. Request is ready for processing.";
            createComment(entityManager, cmt, reqId, appUser);
            createHistory(entityManager, admin, cmt, "PCP", "Automated Processing", reqId, appUser, processingCenter, null, true, null);
          } else {
            // move to PPN
            String cmt = null;
            admin.setReqStatus("PPN");
            StringBuilder rejCmtBuilder = new StringBuilder();
            rejectInfo = (List<RejectionContainer>) engineData.get().get("rejections");
            // add comments if request rejected or if pending checks
            if ((rejectInfo != null && !rejectInfo.isEmpty()) || (pendingChecks != null && !pendingChecks.isEmpty())) {
              rejCmtBuilder.append("The request needs further review due to some issues found during automated checks");
              rejCmtBuilder.append(":");
              for (RejectionContainer rejCont : rejectInfo) {
                rejCmtBuilder.append(rejCont.getRejComment() + "\n");
              }
              // append pending checks
              for (String pendingCheck : pendingChecks.values()) {
                rejCmtBuilder.append("\n ");
                rejCmtBuilder.append(pendingCheck);
              }
              cmt = rejCmtBuilder.toString();

              if (cmt.length() > 1930) {
                cmt = cmt.substring(0, 1920) + "...";
              }
              cmt += "\n\nPlease view system processing results for more details.";
            } else {
              cmt = "Automated checks completed successfully.";
            }
            if (stopExecution) {
              cmt = "Automated checks stopped execution due to an error reported by one of the processes.";
            } else {
              createComment(entityManager, cmt, reqId, appUser);
            }
            String histCmt = cmt;
            if (cmt.length() > 1000) {
              histCmt = "The request needs further review due to some issues found during automated checks. Check the request comment log and system processing results for details.";
            }
            createHistory(entityManager, admin, histCmt, "PPN", "Automated Processing", reqId, appUser, processingCenter, null, false, null);
          }

        }

      }
    }
    admin.setLastUpdtBy(appUser.getIntranetId());
    admin.setLastUpdtTs(SystemUtil.getActualTimestamp());
    entityManager.merge(admin);
    entityManager.flush();
    LOG.debug("Automation engine for Request " + reqId + " finished");
  }

  /**
   * Gets the next result id from sequence
   * 
   * @param entityManager
   * @return
   * @throws CmrException
   * @throws SQLException
   */
  private long getNextResultId(EntityManager entityManager) throws CmrException, SQLException {
    long nextId = SystemUtil.getNextID(entityManager, SystemConfiguration.getValue("MANDT"), "AUTOMATION_RESULT_ID", "CREQCMR");
    return nextId;
  }

  /**
   * Creates a system error based on the given {@link AutomationElement}
   * 
   * @param entityManager
   * @param reqId
   * @param resultId
   * @param element
   * @param user
   */
  private void createSystemErrorResult(EntityManager entityManager, long reqId, long resultId, AutomationElement<?> element, AppUser user) {
    AutomationResults result = new AutomationResults();
    AutomationResultsPK pk = new AutomationResultsPK();
    pk.setAutomationResultId(resultId);
    pk.setReqId(reqId);
    pk.setProcessCd(element.getProcessCode());
    result.setId(pk);
    result.setCreateBy(user.getIntranetId());
    result.setCreateTs(SystemUtil.getActualTimestamp());
    result.setDetailedResults("Automated processing stopped due to a SYSTEM error.");
    result.setProcessDesc(element.getProcessDesc());
    result.setProcessResult("System Error");
    result.setProcessTyp(element.getProcessType().toCode());
    result.setFailureIndc("S");
    LOG.debug("Creating system error for " + element.getProcessDesc());
    entityManager.persist(result);
  }

  /**
   * Creates a system error based on the given {@link AutomationElement}
   * 
   * @param entityManager
   * @param reqId
   * @param resultId
   * @param element
   * @param user
   */
  private void createStopResult(EntityManager entityManager, long reqId, long resultId, int lastElementIndex, AppUser user) {

    StringBuilder stopCmt = new StringBuilder();
    AutomationElement<?> errorElem = this.elements.get(lastElementIndex);
    stopCmt.append("Automated checks stopped due to an error on process " + errorElem.getProcessDesc() + "\n");
    if (lastElementIndex >= this.elements.size() - 1) {
      stopCmt.append("All other elements were executed successfully.");
    } else {
      stopCmt.append("The following elements were not executed:\n");
      for (int i = lastElementIndex + 1; i < this.elements.size(); i++) {
        stopCmt.append(" - " + this.elements.get(i).getProcessDesc()).append("\n");
      }
    }

    AutomationResults result = new AutomationResults();
    AutomationResultsPK pk = new AutomationResultsPK();
    pk.setAutomationResultId(resultId);
    pk.setReqId(reqId);
    pk.setProcessCd("STOP");
    result.setId(pk);
    result.setCreateBy(user.getIntranetId());
    result.setCreateTs(SystemUtil.getActualTimestamp());
    result.setDetailedResults(stopCmt.toString().trim());
    result.setProcessDesc("Automation Engine");
    result.setProcessResult("Execution Stopped");
    result.setProcessTyp("V");
    result.setFailureIndc("P");
    LOG.debug("Creating stop result for " + errorElem.getProcessDesc());
    entityManager.persist(result);
  }

  /**
   * Checks if this country will go to processing automatically on successful
   * execution or not
   * 
   * @param entityManager
   * @param country
   * @param reqType
   * @return
   */
  private boolean isProcessOnCompletion(EntityManager entityManager, String country, String reqType) {
    String sql = ExternalizedQuery.getSql("AUTOMATION.GET_ON_COMPLETE_ACTION");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("CNTRY", country);
    query.setForReadOnly(true);
    String result = query.getSingleResult(String.class);
    boolean onCompleteAction = false;
    if ("Y".equals(result) || ("C".equals(result) && "C".equals(reqType)) || ("U".equals(result) && "U".equals(reqType))) {
      onCompleteAction = true;
    }
    return onCompleteAction;

  }

  /**
   * Creates a {@link WfHist} record
   * 
   * @param entityManager
   * @param comment
   * @param status
   * @param action
   * @param reqId
   * @throws SQLException
   * @throws CmrException
   */
  protected WfHist createHistory(EntityManager entityManager, Admin admin, String comment, String status, String action, long reqId, AppUser user,
      String processingCenter, String rejectReason, boolean sendMail, RejectionContainer rejectCont) throws CmrException, SQLException {
    // create workflow history record
    completeLastHistoryRecord(entityManager, admin.getId().getReqId());
    WfHist hist = new WfHist();
    WfHistPK histpk = new WfHistPK();
    String rejCd = null;
    String supplInfo1 = null;
    String supplInfo2 = null;

    if (rejectCont != null) {
      if (StringUtils.isNotBlank(rejectCont.getRejCode())) {
        rejCd = rejectCont.getRejCode().length() > 5 ? rejectCont.getRejCode().substring(0, 4) : rejectCont.getRejCode();
      }
      if (StringUtils.isNotBlank(rejectCont.getSupplInfo1())) {
        supplInfo1 = rejectCont.getSupplInfo1().length() > 50 ? rejectCont.getSupplInfo1().substring(0, 49) : rejectCont.getSupplInfo1();
      }
      if (StringUtils.isNotBlank(rejectCont.getSupplInfo2())) {
        supplInfo2 = rejectCont.getSupplInfo2().length() > 50 ? rejectCont.getSupplInfo2().substring(0, 49) : rejectCont.getSupplInfo2();
      }
    }
    histpk.setWfId(SystemUtil.getNextID(entityManager, SystemConfiguration.getValue("MANDT"), "WF_ID"));
    hist.setId(histpk);
    hist.setCmt(comment);
    hist.setReqStatus(admin.getReqStatus());
    hist.setCreateById(user.getIntranetId());
    hist.setCreateByNm(user.getIntranetId());
    hist.setCreateTs(SystemUtil.getCurrentTimestamp());
    hist.setReqId(admin.getId().getReqId());
    hist.setRejReason(rejectReason);
    hist.setReqStatusAct(action);
    hist.setRejReasonCd(rejCd);
    hist.setRejSupplInfo1(supplInfo1);
    hist.setRejSupplInfo2(supplInfo2);

    if (user.getIntranetId() != null) {
      hist.setSentToId(user.getIntranetId());
    }

    if (user.getBluePagesName() != null) {
      hist.setSentToNm(user.getBluePagesName());
    }

    // if (complete) {
    // hist.setCompleteTs(SystemUtil.getCurrentTimestamp());
    // }

    entityManager.persist(hist);
    entityManager.flush();

    if (sendMail) {
      RequestUtils.sendEmailNotifications(entityManager, admin, hist);
    }

    return hist;
  }

  /**
   * Creates a {@link ReqCmtLog} record
   * 
   * @param entityManager
   * @param comment
   * @param reqId
   * @throws SQLException
   * @throws CmrException
   */
  protected void createComment(EntityManager entityManager, String comment, long reqId, AppUser user) throws CmrException, SQLException {
    // create request comment
    ReqCmtLog cmt = new ReqCmtLog();
    ReqCmtLogPK cmtPk = new ReqCmtLogPK();
    long cmtId = SystemUtil.getNextID(entityManager, SystemConfiguration.getValue("MANDT"), "CMT_ID");
    cmtPk.setCmtId(cmtId);
    cmt.setId(cmtPk);
    cmt.setCmt(comment);
    cmt.setCmtLockedIn("Y");
    cmt.setCreateById(user.getIntranetId());
    cmt.setCreateByNm(user.getIntranetId());
    cmt.setCreateTs(SystemUtil.getCurrentTimestamp());
    cmt.setReqId(reqId);
    cmt.setUpdateTs(cmt.getCreateTs());
    entityManager.persist(cmt);
  }

  /**
   * Creates a dummy {@link AppUser} object for the automation engine
   * 
   * @return
   */
  private AppUser createAutomationAppUser() {
    AppUser user = new AppUser();
    user.setBluePagesName("AutomationEngine");
    user.setIntranetId("AutomationEngine");
    user.setProcessor(true);
    return user;
  }

  /**
   * Checks if the child request's status is part of the 'stop' statuses.
   * Completed, Draft, Rejected, Cancelled
   * 
   * @param entityManager
   * @param childReqId
   * @return
   */
  private boolean isChildPending(EntityManager entityManager, long childReqId) {
    LOG.debug("Checking child request status..");
    String sql = ExternalizedQuery.getSql("AUTO.CHECK_CHILD_STATUS");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setForReadOnly(true);
    query.setParameter("CHILD_ID", childReqId);
    String status = query.getSingleResult(String.class);
    if (status == null) {
      return false;
    }
    return (!Arrays.asList("COM", "PRJ", "DRA", "CAN").contains(status));
  }

  private static void completeLastHistoryRecord(EntityManager entityManager, long reqId) {
    PreparedQuery update = new PreparedQuery(entityManager, ExternalizedQuery.getSql("WORK_FLOW.COMPLETE_LAST"));
    update.setParameter("REQ_ID", reqId);
    update.executeSql();
  }
}
