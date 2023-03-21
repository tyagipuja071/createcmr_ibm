/**
 * 
 */
package com.ibm.cmr.create.batch.service;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.persistence.EntityManager;

import org.apache.commons.lang3.StringUtils;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.CompoundEntity;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.model.requestentry.RequestEntryModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.approval.ApprovalService;
import com.ibm.cio.cmr.request.user.AppUser;
import com.ibm.cio.cmr.request.util.RequestUtils;
import com.ibm.cio.cmr.request.util.SystemLocation;
import com.ibm.cio.cmr.request.util.SystemUtil;
import com.ibm.cio.cmr.request.util.masscreate.MassCreateFile;
import com.ibm.cio.cmr.request.util.masscreate.MassCreateFile.ValidationResult;
import com.ibm.cio.cmr.request.util.masscreate.MassCreateFileParser;
import com.ibm.cio.cmr.request.util.masscreate.MassCreateFileRow;
import com.ibm.cio.cmr.request.util.masscreate.MassCreateUtil;
import com.ibm.cmr.create.batch.util.BatchUtil;
import com.ibm.cmr.create.batch.util.masscreate.ValidatorWorker;
import com.ibm.cmr.create.batch.util.masscreate.WorkerThreadFactory;
import com.ibm.cmr.create.batch.util.masscreate.handler.HandlerEngine;
import com.ibm.cmr.create.batch.util.masscreate.handler.impl.AddressHandler;
import com.ibm.cmr.create.batch.util.masscreate.handler.impl.CAAddressHandler;
import com.ibm.cmr.create.batch.util.masscreate.handler.impl.CACreatebyModelHandler;
import com.ibm.cmr.create.batch.util.masscreate.handler.impl.CADefaultFields;
import com.ibm.cmr.create.batch.util.masscreate.handler.impl.CALocationNoHandler;
import com.ibm.cmr.create.batch.util.masscreate.handler.impl.CAPhoneNoHandler;
import com.ibm.cmr.create.batch.util.masscreate.handler.impl.CATaxHandler;
import com.ibm.cmr.create.batch.util.masscreate.handler.impl.CMRNoHandler;
import com.ibm.cmr.create.batch.util.masscreate.handler.impl.CMRNoNonUSHandler;
import com.ibm.cmr.create.batch.util.masscreate.handler.impl.CityAndCountyHandler;
import com.ibm.cmr.create.batch.util.masscreate.handler.impl.CreatebyModelHandler;
import com.ibm.cmr.create.batch.util.masscreate.handler.impl.DPLCheckHandler;
import com.ibm.cmr.create.batch.util.masscreate.handler.impl.DataHandler;
import com.ibm.cmr.create.batch.util.masscreate.handler.impl.EnterpriseAffiliateHandler;
import com.ibm.cmr.create.batch.util.masscreate.handler.impl.INACHandler;
import com.ibm.cmr.create.batch.util.masscreate.handler.impl.InternalTypeAbbrevNameHandler;
import com.ibm.cmr.create.batch.util.masscreate.handler.impl.SubindustryISICHandler;
import com.ibm.cmr.create.batch.util.masscreate.handler.impl.TgmeAddrStdHandler;
import com.ibm.cmr.create.batch.util.masscreate.handler.impl.USPostCodeAndStateHandler;
import com.ibm.cmr.create.batch.util.masscreate.handler.impl.WWSubindustryISICHandler;

/**
 * @author Jeffrey Zamora
 * 
 */
public class MassCreateValidatorService extends BaseBatchService {

  private static Map<String, HandlerEngine> engines = new HashMap<String, HandlerEngine>();

  private static final String VALIDATION_ACTION = "System Validation";

  private static final int MAX_CELL_CONTENTS = 1000;

  public MassCreateValidatorService() {
    super();
  }

  @Override
  protected Boolean executeBatch(EntityManager entityManager) throws Exception {

    // query records for validation
    String sql = ExternalizedQuery.getSql("BATCH.MC.GET_PENDING_VALIDATION");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    List<CompoundEntity> mcRecords = query.getCompundResults(Admin.class, Admin.BATCH_MASS_CREATE_GET_RECORD);
    if (mcRecords != null && mcRecords.size() > 0) {

      if ("Y".equals(BatchUtil.getProperty("masscreate.multithreaded"))) {
        LOG.info("Staring async validation of records...");
        ExecutorService executor = Executors.newFixedThreadPool(5, new WorkerThreadFactory("MassCreateValidator"));

        ValidatorWorker worker = null;
        List<ValidatorWorker> workers = new ArrayList<ValidatorWorker>();
        for (CompoundEntity qResult : mcRecords) {
          Thread.sleep(3);
          worker = new ValidatorWorker(entityManager, this, qResult);
          if (worker != null) {
            executor.execute(worker);
          }
          workers.add(worker);

        }

        // wait till execution finish
        executor.shutdown();

        while (!executor.isTerminated()) {
          Thread.sleep(5000);
        }

        for (ValidatorWorker workerThread : workers) {
          if (workerThread.isError()) {
            addError(workerThread.getErrorMessage());
          }
        }

        LOG.info("Validator workers finished.");
      } else {
        for (CompoundEntity qResult : mcRecords) {
          try {
            processRecord(entityManager, qResult);
          } catch (Exception e) {
            Admin request = qResult.getEntity(Admin.class);
            LOG.error("A general error occurred for Request ID " + request.getId().getReqId(), e);
            addError(e);
            request.setLockBy(null);
            request.setLockByNm(null);
            request.setLockInd(CmrConstants.YES_NO.N.toString());
            request.setLockTs(null);
            updateEntity(request, entityManager);
            partialCommit(entityManager);
          }
        }
      }
    }
    return true;
  }

  public void processRecord(EntityManager entityManager, CompoundEntity qResult) throws Exception {
    MassCreateFile massCreate = null;
    Admin request = null;
    Data data = null;

    String comment = null;
    String originalStatus = null;
    String sendToId = null;
    String processingCenter = null;

    request = qResult.getEntity(Admin.class);
    data = qResult.getEntity(Data.class);
    entityManager.detach(data);
    Thread.currentThread().setName("REQ-" + request.getId().getReqId());

    LOG.debug("Checking latest values for the request..");
    refresh(entityManager, request);
    LOG.debug("Request Status: " + request.getReqStatus() + " Locked By: " + request.getLockBy());

    sendToId = processingCenter;

    if ((!"SVA".equals(request.getReqStatus()) && !"SV2".equals(request.getReqStatus()))) {
      LOG.debug("Request " + request.getId().getReqId() + " already locked by another process or has invalid status. Skipping.");
      return;
    }

    // lock to CreateCMR so that on overlaps will not be processed anymore
    lockAdminRecordsForProcessing(Collections.singletonList(request), entityManager);

    LOG.info("Processing Mass Create Request ID: " + request.getId().getReqId());
    massCreate = validateFile(entityManager, data.getCmrIssuingCntry(), request.getFileName(), request.getId().getReqId(), request.getIterationId());

    originalStatus = request.getReqStatus();

    if (massCreate != null && massCreate.getValidationResult() == ValidationResult.Passed) {
      // process records that passed validation

      boolean approvalsNeeded = false;
      String defaultApprovalResult = "";
      LOG.info("Mass Create Request ID: " + request.getId().getReqId() + " passed system validations.");
      switch (originalStatus) {
      case "SVA":
        if (!hasRecordsForIteration(entityManager, request.getId().getReqId(), request.getIterationId())) {
          LOG.debug("Mass Create records does not exist for the current iteration, creating...");
          // MassCreateUtil.createMassCreateRecords(massCreate, entityManager);
          MassCreateUtil.createMassCreateRecords(massCreate, entityManager, data.getCmrIssuingCntry(), originalStatus);
        }
        ApprovalService approvalService = new ApprovalService();
        AppUser dummyUser = new AppUser();
        dummyUser.setIntranetId(BATCH_USER_ID);
        dummyUser.setBluePagesName(BATCH_USER_ID);
        LOG.debug("Checking default approvals for Request " + request.getId().getReqId());
        if ("897".equals(data.getCmrIssuingCntry())) {
          // For US no need approval
          defaultApprovalResult = "";
        } else {
          defaultApprovalResult = approvalService.processDefaultApproval(entityManager, request.getId().getReqId(), CmrConstants.REQ_TYPE_MASS_CREATE,
              dummyUser, new RequestEntryModel());
          LOG.debug("Default Approval Response Code " + defaultApprovalResult);
        }
        if (StringUtils.isBlank(defaultApprovalResult)) {
          boolean isCMDERequester = isCMDERequester(entityManager, request.getRequesterId(), data.getCmrIssuingCntry());
          if (isCMDERequester) {
            request.setReqStatus(CmrConstants.REQUEST_STATUS.AUT.toString());
            comment = "System validation succeeded. Request sent for Automated Processing.";
          } else {
            request.setReqStatus(CmrConstants.REQUEST_STATUS.PPN.toString());
            comment = "System validation succeeded. Request sent for Processing.";
          }
          processingCenter = getProcessingCenter(entityManager, data.getCmrIssuingCntry());
          request.setLastProcCenterNm(processingCenter);

          sendToId = processingCenter;
        } else {
          approvalsNeeded = true;
          request.setReqStatus(CmrConstants.REQUEST_STATUS.DRA.toString());
          request.setLastProcCenterNm(processingCenter);
          request.setLockBy(request.getRequesterId());
          request.setLockByNm(request.getRequesterNm());
          request.setLockInd(CmrConstants.YES_NO.Y.toString());
          request.setLockTs(SystemUtil.getCurrentTimestamp());
          comment = "System validation succeeded but the request have pending required approvals.";
          sendToId = null;
        }
        break;
      case "SV2":
        if (!hasRecordsForIteration(entityManager, request.getId().getReqId(), request.getIterationId())) {
          LOG.debug("Mass Create records does not exist for the current iteration, creating...");
          // MassCreateUtil.createMassCreateRecords(massCreate, entityManager);
          MassCreateUtil.createMassCreateRecords(massCreate, entityManager, data.getCmrIssuingCntry(), originalStatus);
        }
        request.setReqStatus(CmrConstants.REQUEST_STATUS.PCP.toString());
        request.setProcessedFlag(CmrConstants.YES_NO.N.toString());
        // For US, set rdcProcessingStatus
        if (SystemLocation.UNITED_STATES.equals(data.getCmrIssuingCntry())) {
          request.setRdcProcessingStatus(CmrConstants.RDC_STATUS_ABORTED);
        }
        comment = "System validation succeeded. Request ready for automatic processing.";
        sendToId = null;
        break;
      }
      request.setLastUpdtTs(SystemUtil.getCurrentTimestamp());
      request.setLastUpdtBy(BATCH_USER_ID);
      if (!approvalsNeeded) {
        request.setLockBy(null);
        request.setLockByNm(null);
        request.setLockInd(CmrConstants.YES_NO.N.toString());
        request.setLockTs(null);
      }
      updateEntity(request, entityManager);
      LOG.debug("Mass Create Request ID: " + request.getId().getReqId() + " creating Workflow History and Change Logs");

      RequestUtils.createWorkflowHistoryFromBatch(entityManager, BATCH_USER_ID, request, comment, VALIDATION_ACTION, sendToId, null, false);
      RequestUtils.createCommentLogFromBatch(entityManager, BATCH_USER_ID, request.getId().getReqId(), comment);
    } else {
      // process records that failed validation
      LOG.info("Mass Create Request ID: " + request.getId().getReqId() + " FAILED system validations.");
      boolean unexpectedError = massCreate != null && massCreate.getValidationResult().equals(ValidationResult.UnknownError);

      switch (originalStatus) {
      case "SVA":
        LOG.debug("Sending back to requester..");
        request.setReqStatus(CmrConstants.REQUEST_STATUS.DRA.toString());
        request.setLastProcCenterNm(processingCenter);
        request.setLockBy(request.getRequesterId());
        request.setLockByNm(request.getRequesterNm());
        request.setLockInd(CmrConstants.YES_NO.Y.toString());
        request.setLockTs(SystemUtil.getCurrentTimestamp());
        break;
      case "SV2":
        LOG.debug("Setting to processing pending.");
        request.setReqStatus(CmrConstants.REQUEST_STATUS.PPN.toString());
        request.setLockBy(null);
        request.setLockByNm(null);
        request.setLockInd(CmrConstants.YES_NO.N.toString());
        request.setLockTs(null);
        break;
      }

      request.setLastUpdtTs(SystemUtil.getCurrentTimestamp());
      request.setLastUpdtBy(BATCH_USER_ID);
      updateEntity(request, entityManager);

      if (unexpectedError) {
        comment = "An unexpected error occurred during validation. Please try to submit the request again.";
      } else {
        comment = "System validation FAILED. Check error messages on the current file.";
      }
      sendToId = null;

      LOG.debug("Mass Create Request ID: " + request.getId().getReqId() + " creating Workflow History and Change Logs");
      RequestUtils.createWorkflowHistoryFromBatch(entityManager, BATCH_USER_ID, request, comment, VALIDATION_ACTION, sendToId, null, false);
      RequestUtils.createCommentLogFromBatch(entityManager, BATCH_USER_ID, request.getId().getReqId(),
          comment + (unexpectedError ? "" : "\nDownload the current file from the Processing Tab."));

      partialCommit(entityManager);
    }
    if ("897".equals(data.getCmrIssuingCntry())) {
      cleanMassCrtPreData(entityManager, request.getId().getReqId(), request.getIterationId());
    }
    Thread.currentThread().setName("MCValidate-" + Thread.currentThread().getId());

  }

  /**
   * Validates the file currently uploaded to the mass create request and
   * iteration
   * 
   * @param entityManager
   * @param cmrIssuingCountry
   * @param fileName
   * @param reqId
   * @param iterationId
   * @return
   * @throws Exception
   */
  private MassCreateFile validateFile(EntityManager entityManager, String cmrIssuingCountry, String fileName, long reqId, int iterationId)
      throws Exception {
    if (StringUtils.isBlank(fileName)) {
      LOG.warn("Filename is empty for Request " + reqId + ". Skipping this request.");
      return null;
    }
    File file = new File(fileName);
    if (!file.exists()) {
      LOG.warn("File for Request ID " + reqId + " does not exist.");
      return null;
    }
    MassCreateFileParser parser = new MassCreateFileParser();
    MassCreateFile data = null;
    boolean hasError = false;
    try (FileInputStream fis = new FileInputStream(fileName)) {
      data = parser.parse(fis, reqId, iterationId, false);
      if (ValidationResult.Passed != data.getValidationResult()) {
        return data;
      }
      data.setCmrIssuingCntry(cmrIssuingCountry);
      List<MassCreateFileRow> rows = data.getRows();
      HandlerEngine engine = null;
      List<String> errors = null;
      StringBuilder errorMsg = new StringBuilder();
      try {
        for (MassCreateFileRow row : rows) {
          engine = initEngine(cmrIssuingCountry);
          errors = engine.validateRow(entityManager, row);
          if (errors.size() > 0) {
            errorMsg.delete(0, errorMsg.length());
            for (String error : errors) {
              errorMsg.append(errorMsg.length() > 0 ? "\n" : "");
              errorMsg.append(error);
            }
            if (errorMsg.length() > MAX_CELL_CONTENTS) {
              // limit to 200 so as not to exceed excel's limit
              errorMsg.delete(MAX_CELL_CONTENTS, errorMsg.length());
              errorMsg.append("\nToo many errors.");
            }
            row.setErrorMessage(errorMsg.toString());
            hasError = true;
          } else {
            // if no errrors, call transformation
            engine.transform(entityManager, row);
          }
        }
      } catch (Exception e) {
        LOG.error("Error in validating file for Request ID " + reqId, e);
        data.setValidationResult(ValidationResult.UnknownError);
      }
      if (hasError) {
        data.setValidationResult(ValidationResult.HasErrors);
      }
    }

    // validations are done, update the file
    data.updateFile(parser, fileName);

    return data;
  }

  private boolean hasRecordsForIteration(EntityManager entityManager, long reqId, int iterationId) {
    String sql = ExternalizedQuery.getSql("MC.CHECK_MC_ITERATION");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", reqId);
    query.setParameter("ITER_ID", iterationId);
    return query.exists();
  }

  private void cleanMassCrtPreData(EntityManager entityManager, long reqId, int itertionId) {
    PreparedQuery query = new PreparedQuery(entityManager, ExternalizedQuery.getSql("US.MASSCRT.CLEANOLDITER"));
    query.setParameter("REQ_ID", reqId);
    query.setParameter("ITERATION_ID", itertionId);
    query.executeSql();
  }

  @Override
  protected boolean isTransactional() {
    return true;
  }

  /**
   * Initializes the handler engine for the country. The engine is cached to
   * avoid multiple initializations per run
   * 
   * @param cmrIssuingCountry
   * @return
   * @throws Exception
   */
  private HandlerEngine initEngine(String cmrIssuingCountry) throws Exception {
    if (engines.get(cmrIssuingCountry) == null) {
      LOG.debug("Initializing handler engine for " + cmrIssuingCountry);
      HandlerEngine engine = new HandlerEngine();

      // ww handlers
      engine.addHandler(new DataHandler());
      engine.addHandler(new DPLCheckHandler());
      engine.addHandler(new INACHandler());

      // per country handler
      switch (cmrIssuingCountry) {
      case SystemLocation.UNITED_STATES:
        engine.addHandler(new CreatebyModelHandler());
        engine.addHandler(new AddressHandler());
        engine.addHandler(new SubindustryISICHandler());
        // engine.addHandler(new CoverageBgGlcISUHandler());
        engine.addHandler(new TgmeAddrStdHandler());
        engine.addHandler(new USPostCodeAndStateHandler());
        engine.addHandler(new CityAndCountyHandler());
        engine.addHandler(new CMRNoHandler());
        engine.addHandler(new EnterpriseAffiliateHandler());
        engine.addHandler(new InternalTypeAbbrevNameHandler());
        break;
      case SystemLocation.CANADA:
        engine.addHandler(new CADefaultFields());
        engine.addHandler(new WWSubindustryISICHandler());
        engine.addHandler(new CACreatebyModelHandler());
        engine.addHandler(new CMRNoNonUSHandler());
        engine.addHandler(new CATaxHandler());
        engine.addHandler(new CALocationNoHandler());
        engine.addHandler(new CAAddressHandler());
        engine.addHandler(new CAPhoneNoHandler());
        break;
      }

      engines.put(cmrIssuingCountry, engine);
    }
    return engines.get(cmrIssuingCountry);
  }

  /**
   * Gets the processing center associated with the cmr issuing country
   * 
   * @param entityManager
   * @param cmrIssuingCntry
   * @return
   */
  private String getProcessingCenter(EntityManager entityManager, String cmrIssuingCntry) {
    String sql = ExternalizedQuery.getSql("MC.GET_PROC_CENTER");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("CMR_ISSUING_CNTRY", cmrIssuingCntry);
    query.setForReadOnly(true);
    return query.getSingleResult(String.class);

  }

  @Override
  protected boolean useServicesConnections() {
    return true;
  }

  private void lockAdminRecordsForProcessing(List<Admin> forProcessing, EntityManager entityManager) {
    for (Admin admin : forProcessing) {
      admin.setLockBy(BATCH_USER_ID);
      admin.setLockByNm(BATCH_USER_ID);
      admin.setLockInd(CmrConstants.YES_NO.Y.toString());
      admin.setLockTs(SystemUtil.getCurrentTimestamp());
      admin.setLastUpdtBy(BATCH_USER_ID);
      admin.setLastUpdtTs(SystemUtil.getCurrentTimestamp());
      LOG.debug("Locking Request ID " + admin.getId().getReqId() + " for processing.");
      updateEntity(admin, entityManager);
    }
    partialCommit(entityManager);

  }
  
  private boolean isCMDERequester(EntityManager em, String requester_id, String country) {
    boolean isCMDE=false;
    String sql = ExternalizedQuery.getSql("AUTO.CHECK_CMDE_REQUESTER");
    PreparedQuery query = new PreparedQuery(em, sql);
    query.setParameter("REQUESTER_ID", requester_id);
    query.setParameter("CMR_ISSUING_CNTRY", country);
    query.setForReadOnly(true);
    Object result = query.getSingleResult(String.class);
    if (result != null) {
      isCMDE = true;
    }
    return isCMDE;
  }

}
