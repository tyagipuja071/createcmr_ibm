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

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.automation.impl.DuplicateCheckElement;
import com.ibm.cio.cmr.request.automation.impl.ProcessWaitingElement;
import com.ibm.cio.cmr.request.automation.out.AutomationOutput;
import com.ibm.cio.cmr.request.automation.out.AutomationResult;
import com.ibm.cio.cmr.request.automation.util.AutomationConst;
import com.ibm.cio.cmr.request.automation.util.AutomationUtil;
import com.ibm.cio.cmr.request.automation.util.RejectionContainer;
import com.ibm.cio.cmr.request.automation.util.ScenarioExceptionsUtil;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.AutoEngineConfig;
import com.ibm.cio.cmr.request.entity.AutomationResults;
import com.ibm.cio.cmr.request.entity.AutomationResultsPK;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.entity.Lov;
import com.ibm.cio.cmr.request.entity.ReqCmtLog;
import com.ibm.cio.cmr.request.entity.ReqCmtLogPK;
import com.ibm.cio.cmr.request.entity.WfHist;
import com.ibm.cio.cmr.request.entity.WfHistPK;
import com.ibm.cio.cmr.request.entity.listeners.ChangeLogListener;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.user.AppUser;
import com.ibm.cio.cmr.request.util.BluePagesHelper;
import com.ibm.cio.cmr.request.util.RequestUtils;
import com.ibm.cio.cmr.request.util.SlackAlertsUtil;
import com.ibm.cio.cmr.request.util.SystemParameters;
import com.ibm.cio.cmr.request.util.SystemUtil;
import com.ibm.cio.cmr.request.util.geo.GEOHandler;
import com.ibm.cio.cmr.request.util.geo.impl.USHandler;
import com.ibm.cio.cmr.request.util.legacy.LegacyDowntimes;
import com.ibm.cio.cmr.request.util.mail.Email;
import com.ibm.cio.cmr.request.util.mail.MessageType;

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
  private static Map<String, String> rejectionReasons = new HashMap<String, String>();

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

    synchronized (this) {
      if (rejectionReasons.isEmpty()) {
        initRejectReasons(entityManager);
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

    // failsafes before engine run for any request
    requestData.getAdmin().setReviewReqIndc("N");
    requestData.getAdmin().setDisableAutoProc("N");
    ScenarioExceptionsUtil scenarioExceptions = AutomationUtil.getScenarioExceptions(entityManager, requestData, engineData.get());
    LOG.debug("Verifying PayGo Accreditation for " + requestData.getAdmin().getSourceSystId());
    boolean payGoAddredited = RequestUtils.isPayGoAccredited(entityManager, requestData.getAdmin().getSourceSystId());
    LOG.debug(" PayGo: " + payGoAddredited);
    int nonCompanyVerificationErrorCount = 0;

    // CREATCMR-4872
    boolean isUsTaxSkipToPcp = false;
    // CREATCMR-5447
    boolean isUsTaxSkipToPpn = false;
    boolean requesterFromTaxTeam = false;
    String strRequesterId = requestData.getAdmin().getRequesterId().toLowerCase();
    requesterFromTaxTeam = BluePagesHelper.isUserInUSTAXBlueGroup(strRequesterId);

    // CREATCMR-6331
    boolean isUsEntCompToPpn = false;
    String strCmtUsEntCompToPpn = "";

    if ("897".equals(requestData.getData().getCmrIssuingCntry())) {
      if ("U".equals(requestData.getAdmin().getReqType())) {
        // CREATCMR-5447
        if (requesterFromTaxTeam) {
          isUsTaxSkipToPcp = USHandler.getUSDataSkipToPCP(entityManager, requestData.getData());
        } else {
          isUsTaxSkipToPpn = USHandler.getUSDataSkipToPPN(entityManager, requestData.getData());
        }
      } else if ("M".equals(requestData.getAdmin().getReqType())) {
        // CREATCMR-6331
        strCmtUsEntCompToPpn = USHandler.getUSEntCompToPPN(entityManager, requestData.getAdmin());
        if (StringUtils.isNotBlank(strCmtUsEntCompToPpn)) {
          isUsEntCompToPpn = true;
        }
      }
    }

    for (AutomationElement<?> element : this.elements) {
      // determine if element is to be skipped
      boolean skipChecks = scenarioExceptions != null ? scenarioExceptions.isSkipChecks() : false;
      boolean skipElement = (skipChecks || engineData.get().isSkipChecks())
          && (ProcessType.StandardProcess.equals(element.getProcessType()) || ProcessType.DataOverride.equals(element.getProcessType())
              || (ProcessType.Matching.equals(element.getProcessType()) && !(element instanceof DuplicateCheckElement)));

      boolean skipVerification = scenarioExceptions != null && scenarioExceptions.isSkipCompanyVerification();
      skipVerification = skipVerification && (element instanceof CompanyVerifier);

      // CREATCMR-4872
      if (isUsTaxSkipToPcp) {
        break;
      }

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
            result = element.executeAutomationElement(entityManager, requestData, engineData.get());
          } catch (Exception e) {
            SlackAlertsUtil.recordException("Automation", element.getProcessDesc() + " - System Error Request " + reqId, e);
            LOG.warn("System error for element " + element.getProcessDesc(), e);
            result = new AutomationResult<>();
            result.setOnError(true);
            systemError = true;
            createSystemErrorResult(entityManager, reqId, resultId, element, appUser);
            StringBuilder details = new StringBuilder();
            details.append("System error for element " + element.getProcessDesc() + " with Req ID -> " + requestData.getAdmin().getId().getReqId()
                + " has occured. Please check concerned logs for the same.");
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
          // ensure to reset waiting status as elements are cached
          ((ProcessWaitingElement) element).resetWaitingStatus();
          break;
        } else if (result.isOnError()) {
          if (!(element instanceof CompanyVerifier)) {
            nonCompanyVerificationErrorCount++;
          }
          if (ActionOnError.Ignore.equals(element.getActionOnError())) {
            LOG.debug("Element " + element.getProcessDesc() + " encountered an error but was ignored.");
          } else {
            actionsOnError.add(element.getActionOnError());
            if (element.isStopOnError()) {
              //if ((element instanceof CompanyVerifier) && payGoAddredited) {
              if (element instanceof CompanyVerifier) {
                // don't stop for paygo accredited and verifier element
                LOG.debug("Error in " + element.getProcessDesc() + " but continuing process for PayGo.");
              } else {
                LOG.info("Stopping execution of other elements because of an error in " + element.getProcessDesc());
                createComment(entityManager, "An error in execution of " + element.getProcessDesc() + " caused the process to stop.", reqId, appUser);
                stopExecution = true;
                break;
              }
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

    boolean sccIsValid = false;
    if ("897".equals(requestData.getData().getCmrIssuingCntry())) {
      String setPPNFlag = USHandler.validateForSCC(entityManager, reqId);
      if ("N".equals(setPPNFlag)) {
        sccIsValid = true;
      }
    } else if ("796".equals(requestData.getData().getCmrIssuingCntry())) {
      checkNZBNAPI(stopExecution, actionsOnError);
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
      if (!"N".equals(admin.getCompVerifiedIndc())) {
        admin.setCompVerifiedIndc("Y");
      }
      if (!"S".equals(admin.getCompInfoSrc())) {
        admin.setCompInfoSrc(compInfoSrc);
      }
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
            if ("S".equalsIgnoreCase(admin.getCompInfoSrc())) {
              createComment(entityManager, "Processing error encountered as SCC(State / County / City) values unavailable.", reqId, appUser);
            } else {
              createComment(entityManager, "Processing error encountered as data is not company verified.", reqId, appUser);
            }
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
        // get rejection comments
        rejectInfo = engineData.get().getRejectionReasons();
        HashMap<String, String> pendingChecks = engineData.get().getPendingChecks();

        boolean moveForPayGo = false;
        // CMR-5954 - paygo solution - move to next step if only company checks
        // failed
        LOG.debug("No. of Non-Verification Errors: " + nonCompanyVerificationErrorCount + ", No. of Non-Verification Negative Checks: "
            + engineData.get().getTrackedNegativeCheckCount());
        // added check that there was indeed an error somewhere before going the
        // paygo route
        if ((!actionsOnError.isEmpty() || (engineData.get().getNegativeChecks() != null && !engineData.get().getNegativeChecks().isEmpty()))
            && nonCompanyVerificationErrorCount == 0 && engineData.get().getTrackedNegativeCheckCount() == 0 && payGoAddredited) {
          moveForPayGo = true;
        }

        if ("C".equals(admin.getReqType()) && !actionsOnError.isEmpty() && payGoAddredited) {
          admin.setPaygoProcessIndc("Y");
          createComment(entityManager, "Pay-Go accredited partner.", reqId, appUser);
        }

        if ("U".equals(admin.getReqType())) {
          if ("PG".equals(data.getOrdBlk())) {
            // admin.setPaygoProcessIndc("Y");
            createComment(entityManager, "Pay-Go accredited partner.", reqId, appUser);
          } else if (!engineData.get().getNegativeChecks().isEmpty() && payGoAddredited) {
            // admin.setPaygoProcessIndc("Y");
            createComment(entityManager, "Pay-Go accredited partner.", reqId, appUser);
          }
        }

        if ("C".equals(admin.getReqType()) && moveForPayGo) {
          createComment(entityManager, "Pay-Go accredited partner. Request passed all other checks, moving to processing.", reqId, appUser);
          admin.setPaygoProcessIndc("Y");
          // data.setIsicCd("8888");
          // data.setUsSicmen("8888");
          // data.setSubIndustryCd("ZZ");
        } else {

          if (!actionsOnError.isEmpty()) {
            // an error has occurred
            if (actionsOnError.contains(ActionOnError.Reject)) {
              moveToNextStep = false;
              // reject the record
              StringBuilder rejCmtBuilder = new StringBuilder();
              rejCmtBuilder.append("The record has been rejected due to some system processing errors");
              if (rejectInfo != null && !rejectInfo.isEmpty()) {
                rejCmtBuilder.append(":");
                for (RejectionContainer rejCont : rejectInfo) {
                  rejCmtBuilder.append("\n" + rejCont.getRejComment());
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
                createHistory(entityManager, admin, r.getRejComment(), "PRJ", "Automated Processing", reqId, appUser, null,
                    "Errors in automated checks", true, r);
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
              if ((rejectInfo != null && !rejectInfo.isEmpty()) || (pendingChecks != null && !pendingChecks.isEmpty())) {
                rejectCmt.append("Processor review is required for following issues");
                rejectCmt.append(":");
                if (rejectInfo != null && !rejectInfo.isEmpty()) {
                  for (RejectionContainer rejCont : rejectInfo) {
                    rejectCmt.append("\n" + rejCont.getRejComment());
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
        }
        if (moveToNextStep) {

          // if there is anything that changed on the request via automated
          // import of overrides /match /standard output, do a save
          if (hasOverrideOrMatchingApplied) {
            GEOHandler geoHandler = RequestUtils.getGEOHandler(data.getCmrIssuingCntry());
            if (geoHandler != null) {
              LOG.debug("Performing GEO Handler saves on request..");
              geoHandler.doBeforeAdminSave(entityManager, admin, data.getCmrIssuingCntry());
            }
          }

          // check configured setting for what to do upon completion
          boolean processOnCompletion = isProcessOnCompletion(entityManager, data.getCmrIssuingCntry(), admin.getReqType());

          if (moveForPayGo) {
            // for paygo, ignore errors
            processOnCompletion = true;
          } else {
            processOnCompletion = processOnCompletion && actionsOnError.isEmpty() && !scenarioExceptions.isManualReviewIndc()
                && (StringUtils.isBlank(admin.getSourceSystId()) || !scenarioExceptions.isReviewExtReqIndc());
          }
          LOG.debug("Process on Completion: " + processOnCompletion);
          String processingCenter = RequestUtils.getProcessingCenter(entityManager, data.getCmrIssuingCntry());
          admin.setLastProcCenterNm(processingCenter);
          admin.setLockInd("N");
          admin.setLockByNm(null);
          admin.setLockBy(null);
          admin.setLockTs(null);
          admin.setProcessedFlag("N");
          if (moveForPayGo) {
            pendingChecks.clear();
          }
          // CREATCMR-4872
          if (isUsTaxSkipToPpn) {
            pendingChecks.put("_ustaxerr", "A member outside tax team has updated the Tax fields.");
            // CREATCMR-5447
          }
          if (isUsEntCompToPpn) {
            // CREATCMR-6331
            pendingChecks.put("_usenttocomp", strCmtUsEntCompToPpn);
            // CREATCMR-5447
          } else if ((processOnCompletion && (pendingChecks == null || pendingChecks.isEmpty())) || (isUsTaxSkipToPcp)) {
            String country = data.getCmrIssuingCntry();
            if (LegacyDowntimes.isUp(country, SystemUtil.getActualTimestamp())) {
              // move to PCP
              LOG.debug("Moving Request " + reqId + " to PCP");
              admin.setReqStatus("PCP");
              String cmt = "Automated checks completed successfully. Request is ready for processing.";
              createComment(entityManager, cmt, reqId, appUser);
              createHistory(entityManager, admin, cmt, "PCP", "Automated Processing", reqId, appUser, processingCenter, null, true, null);
            } else {
              LOG.debug("Country " + country + " is down in legacy. Moving to LEG ");
              admin.setReqStatus("LEG");
              String cmt = "Automated checks completed successfully. Request is ready for processing once Legacy system comes up.";
              createComment(entityManager, cmt, reqId, appUser);
              createHistory(entityManager, admin, cmt, "LEG", "Automated Processing", reqId, appUser, processingCenter, null, true, null);
            }
          } else {
            // move to PPN
            LOG.debug("Moving Request " + reqId + " to PPN");
            String cmt = null;
            admin.setReqStatus("PPN");
            StringBuilder rejCmtBuilder = new StringBuilder();
            rejectInfo = (List<RejectionContainer>) engineData.get().get("rejections");
            // add comments if request rejected or if pending checks
            if ((rejectInfo != null && !rejectInfo.isEmpty()) || (pendingChecks != null && !pendingChecks.isEmpty())) {
              rejCmtBuilder.append("The request needs further review due to some issues found during automated checks");
              rejCmtBuilder.append(":");
              for (RejectionContainer rejCont : rejectInfo) {
                rejCmtBuilder.append("\n" + rejCont.getRejComment());
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
            if ("N".equalsIgnoreCase(admin.getCompVerifiedIndc())) {
              createComment(entityManager, "Processing error encountered as data is not company verified.", reqId, appUser);
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
   * Check the NZBN API and DNB result to judge new result
   * 
   * @param boolean
   *          stopExecution
   * @param List<ActionOnError>
   *          actionsOnError
   * @return
   * @throws @throws
   */
  private void checkNZBNAPI(boolean stopExecution, List<ActionOnError> actionsOnError) {
    if (engineData.get().isNZBNAPICheck()
        && (engineData.get().getPendingChecks().containsKey("DnBMatch") || engineData.get().getPendingChecks().containsKey("DNBCheck"))) {
      stopExecution = false;
      if (engineData.get().getPendingChecks().containsKey("DnBMatch")) {
        engineData.get().getPendingChecks().remove("DnBMatch");
        if (actionsOnError != null && actionsOnError.size() > 0)
          actionsOnError.remove(0);
      }
      if (engineData.get().getPendingChecks().containsKey("DNBCheck"))
        engineData.get().getPendingChecks().remove("DNBCheck");
    }
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
  public static WfHist createHistory(EntityManager entityManager, Admin admin, String comment, String status, String action, long reqId, AppUser user,
      String processingCenter, String rejectReason, boolean sendMail, RejectionContainer rejectCont) throws CmrException, SQLException {
    // create workflow history record
    completeLastHistoryRecord(entityManager, admin.getId().getReqId());
    WfHist hist = new WfHist();
    WfHistPK histpk = new WfHistPK();
    String rejCd = null;
    String supplInfo1 = null;
    String supplInfo2 = null;
    String reason = null;

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
      reason = getRejectionReason(rejCd);
      if (reason == null) {
        reason = rejectReason;
      }
    }

    histpk.setWfId(SystemUtil.getNextID(entityManager, SystemConfiguration.getValue("MANDT"), "WF_ID"));
    hist.setId(histpk);
    if (comment != null && comment.length() > 1000) {
      comment = comment.substring(0, 980) + "...";
    }
    hist.setCmt(comment);
    hist.setReqStatus(admin.getReqStatus());
    hist.setCreateById(user.getIntranetId());
    hist.setCreateByNm(user.getIntranetId());
    hist.setCreateTs(SystemUtil.getCurrentTimestamp());
    hist.setReqId(admin.getId().getReqId());
    if (reason != null && reason.length() > 60) {
      reason = reason.substring(0, 55) + "..";
    }
    hist.setRejReason(reason);
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
  public static void createComment(EntityManager entityManager, String comment, long reqId, AppUser user) throws CmrException, SQLException {
    // create request comment
    ReqCmtLog cmt = new ReqCmtLog();
    ReqCmtLogPK cmtPk = new ReqCmtLogPK();
    long cmtId = SystemUtil.getNextID(entityManager, SystemConfiguration.getValue("MANDT"), "CMT_ID");
    cmtPk.setCmtId(cmtId);
    cmt.setId(cmtPk);
    if (comment != null && comment.length() > 2000) {
      comment = comment.substring(0, 1980) + "...";
    }
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
  public static AppUser createAutomationAppUser() {
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

  /**
   * Initializes the LOVs for Rejection Reason
   * 
   * @param entityManager
   */
  private void initRejectReasons(EntityManager entityManager) {
    LOG.debug("Initializing Rejection Reasons");
    String sql = ExternalizedQuery.getSql("AUTO.REJECTION_CODES");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setForReadOnly(true);
    List<Lov> codes = query.getResults(Lov.class);
    if (codes != null) {
      for (Lov code : codes) {
        rejectionReasons.put(code.getId().getCd(), code.getTxt());
      }
    }
  }

  /**
   * Gets the standard rejection reason given the code
   * 
   * @param code
   * @return
   */
  public static String getRejectionReason(String code) {
    return rejectionReasons.get(code);
  }

  /**
   * 
   * 
   * @param admin
   * @param data
   * @param details
   * @deprecated use {@link SlackAlertsUtil}
   */
  @Deprecated
  protected void sendBlueSquadEmail(Admin admin, Data data, StringBuilder details) {
    String blueSquad = null;
    try {
      blueSquad = SystemParameters.getString("AUT_ENG_SYSTEM_ERR");
    } catch (Exception e) {
      LOG.debug("Failed in getting blue squad Ids. ReqID = " + data.getId().getReqId() + ", requester = " + admin.getRequesterId());
      e.printStackTrace();
    }
    if (blueSquad != null) {
      LOG.debug("Sending email notification to blue squad as System error has occurred. ReqID = " + data.getId().getReqId() + ", requester = "
          + admin.getRequesterId());
      String host = SystemConfiguration.getValue("MAIL_HOST");
      String subject = "System Error has occurred for Request " + data.getId().getReqId();
      String from = "CreateCMR_Automation_GBL";
      String email = details.toString();
      Email mail = new Email();
      mail.setSubject(subject);
      mail.setTo(blueSquad);
      mail.setFrom(from);
      mail.setMessage(email);
      mail.setType(MessageType.HTML);
      mail.send(host);
    }
  }
}
