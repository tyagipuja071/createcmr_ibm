/**
 * 
 */
package com.ibm.cmr.create.batch.service;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.AdminPK;
import com.ibm.cio.cmr.request.entity.CompoundEntity;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.entity.DataPK;
import com.ibm.cio.cmr.request.model.requestentry.RequestEntryModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.approval.ApprovalService;
import com.ibm.cio.cmr.request.user.AppUser;
import com.ibm.cio.cmr.request.util.RequestUtils;
import com.ibm.cio.cmr.request.util.SystemParameters;
import com.ibm.cio.cmr.request.util.SystemUtil;
import com.ibm.cio.cmr.request.util.masscreate.MassCreateFile;
import com.ibm.cio.cmr.request.util.masscreate.MassCreateFile.ValidationResult;
import com.ibm.cio.cmr.request.util.masscreate.MassCreateFileParser;
import com.ibm.cio.cmr.request.util.masscreate.MassCreateFileRow;
import com.ibm.cio.cmr.request.util.masscreate.MassCreateUtil;
import com.ibm.cmr.create.batch.util.BatchUtil;
import com.ibm.cmr.create.batch.util.masscreate.WorkerThreadFactory;
import com.ibm.cmr.create.batch.util.masscreate.handler.HandlerEngine;
import com.ibm.cmr.create.batch.util.masscreate.handler.impl.AddressHandler;
import com.ibm.cmr.create.batch.util.masscreate.handler.impl.CMRNoHandler;
import com.ibm.cmr.create.batch.util.masscreate.handler.impl.CityAndCountyHandler;
import com.ibm.cmr.create.batch.util.masscreate.handler.impl.CoverageBgGlcISUHandler;
import com.ibm.cmr.create.batch.util.masscreate.handler.impl.CreatebyModelHandler;
import com.ibm.cmr.create.batch.util.masscreate.handler.impl.DPLCheckHandler;
import com.ibm.cmr.create.batch.util.masscreate.handler.impl.DataHandler;
import com.ibm.cmr.create.batch.util.masscreate.handler.impl.EnterpriseAffiliateHandler;
import com.ibm.cmr.create.batch.util.masscreate.handler.impl.INACHandler;
import com.ibm.cmr.create.batch.util.masscreate.handler.impl.InternalTypeAbbrevNameHandler;
import com.ibm.cmr.create.batch.util.masscreate.handler.impl.MassCreateWorker;
import com.ibm.cmr.create.batch.util.masscreate.handler.impl.SubindustryISICHandler;
import com.ibm.cmr.create.batch.util.masscreate.handler.impl.TgmeAddrStdHandler;
import com.ibm.cmr.create.batch.util.masscreate.handler.impl.USPostCodeAndStateHandler;

/**
 * @author JeffZAMORA
 *
 */
public class MassCreateValidatorMultiService extends MultiThreadedBatchService<Long> {

  private static Map<String, HandlerEngine> engines = new HashMap<String, HandlerEngine>();

  private static final String VALIDATION_ACTION = "System Validation";

  private static final Logger LOG = Logger.getLogger(MassCreateValidatorMultiService.class);

  @Override
  public Boolean executeBatchForRequests(EntityManager entityManager, List<Long> requests) throws Exception {
    LOG.debug("Executing validations for " + requests.size() + " requests..");
    for (Long reqId : requests) {
      processRecord(entityManager, reqId);
    }
    return true;
  }

  @Override
  protected Queue<Long> getRequestsToProcess(EntityManager entityManager) {
    LOG.debug("Getting records to process for multi-threaded validations..");
    String sql = ExternalizedQuery.getSql("BATCH.MC.GET_PENDING_VALIDATION");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    LinkedList<Long> toProcess = new LinkedList<>();
    List<CompoundEntity> pending = query.getCompundResults(Admin.class, Admin.BATCH_MASS_CREATE_GET_RECORD);
    if (pending != null) {
      for (CompoundEntity record : pending) {
        Admin admin = record.getEntity(Admin.class);
        if (admin != null) {
          toProcess.add(admin.getId().getReqId());
        }
      }
    }
    LOG.debug(toProcess.size() + " records to process..");
    return toProcess;
  }

  public void processRecord(EntityManager entityManager, Long reqId) throws Exception {
    MassCreateFile massCreate = null;
    AdminPK adminPk = new AdminPK();
    adminPk.setReqId(reqId);
    Admin request = entityManager.find(Admin.class, adminPk);

    DataPK dataPk = new DataPK();
    dataPk.setReqId(reqId);
    Data data = entityManager.find(Data.class, dataPk);
    entityManager.detach(data);

    String comment = null;
    String originalStatus = null;
    String sendToId = null;
    String processingCenter = null;

    LOG.debug("Checking latest values for the request..");
    refresh(entityManager, request);
    LOG.debug("Request Status: " + request.getReqStatus() + " Locked By: " + request.getLockBy());

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
      LOG.info("Mass Create Request ID: " + request.getId().getReqId() + " passed system validations.");
      switch (originalStatus) {
      case "SVA":
        if (!hasRecordsForIteration(entityManager, request.getId().getReqId(), request.getIterationId())) {
          LOG.debug("Mass Create records does not exist for the current iteration, creating...");
          MassCreateUtil.createMassCreateRecords(massCreate, entityManager);
        }
        ApprovalService approvalService = new ApprovalService();
        AppUser dummyUser = new AppUser();
        dummyUser.setIntranetId(BATCH_USER_ID);
        dummyUser.setBluePagesName(BATCH_USER_ID);
        LOG.debug("Checking default approvals for Request " + request.getId().getReqId());
        String defaultApprovalResult = approvalService.processDefaultApproval(entityManager, request.getId().getReqId(),
            CmrConstants.REQ_TYPE_MASS_CREATE, dummyUser, new RequestEntryModel());
        LOG.debug("Default Approval Response Code " + defaultApprovalResult);
        if (StringUtils.isBlank(defaultApprovalResult)) {
          request.setReqStatus(CmrConstants.REQUEST_STATUS.PPN.toString());
          processingCenter = getProcessingCenter(entityManager, data.getCmrIssuingCntry());
          request.setLastProcCenterNm(processingCenter);

          comment = "System validation succeeded. Request sent for Processing.";
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
          MassCreateUtil.createMassCreateRecords(massCreate, entityManager);
        }
        request.setReqStatus(CmrConstants.REQUEST_STATUS.PCP.toString());
        request.setProcessedFlag(CmrConstants.YES_NO.N.toString());
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
      try {
        int threads = 5;
        String mcThreads = SystemParameters.getString("PROCESS.THREAD.COUNT");
        if (!StringUtils.isBlank(mcThreads) && StringUtils.isNumeric(mcThreads)) {
          threads = Integer.parseInt(mcThreads);
        } else {
          String threadCount = BatchUtil.getProperty("multithreaded.threadCount");
          if (threadCount != null && StringUtils.isNumeric(threadCount)) {
            threads = Integer.parseInt(threadCount);
          }
        }

        LOG.debug("Worker threads to use: " + threads);
        LOG.debug("Starting validating contents at " + new Date());
        List<MassCreateWorker> workers = new ArrayList<MassCreateWorker>();
        // change to executor and not scheduled
        ExecutorService executor = Executors.newFixedThreadPool(threads, new WorkerThreadFactory("MCWorker-" + data.getReqId()));
        for (MassCreateFileRow row : rows) {
          engine = initEngine(cmrIssuingCountry);
          MassCreateWorker worker = new MassCreateWorker(entityManager, engine, row);
          executor.execute(worker);
          workers.add(worker);
        }

        executor.shutdown();
        while (!executor.isTerminated()) {
          try {
            Thread.sleep(5000);
          } catch (InterruptedException e) {
            // noop
          }
        }

        LOG.debug("Finished validating contents at " + new Date());

        for (MassCreateWorker worker : workers) {
          if (worker != null) {
            if (worker.isError()) {
              LOG.error("Error in validating file for Request ID " + reqId + ": " + worker.getErrorMsg());
              data.setValidationResult(ValidationResult.UnknownError);
              break;
            }
            if (worker.getErrors() != null && worker.getErrors().size() > 0) {
              hasError = true;
              break;
            }
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

  /**
   * Initializes the handler engine for the country. The engine is cached to
   * avoid multiple initializations per run
   * 
   * @param cmrIssuingCountry
   * @return
   * @throws Exception
   */
  private synchronized HandlerEngine initEngine(String cmrIssuingCountry) throws Exception {
    if (engines.get(cmrIssuingCountry) == null) {
      LOG.debug("Initializing handler engine for " + cmrIssuingCountry);
      HandlerEngine engine = new HandlerEngine();

      // ww handlers
      engine.addHandler(new CreatebyModelHandler());
      engine.addHandler(new DataHandler());
      engine.addHandler(new AddressHandler());
      engine.addHandler(new SubindustryISICHandler());
      engine.addHandler(new DPLCheckHandler());
      engine.addHandler(new INACHandler());
      engine.addHandler(new CoverageBgGlcISUHandler());

      // per country handler
      switch (cmrIssuingCountry) {
      case "897":
        engine.addHandler(new TgmeAddrStdHandler());
        engine.addHandler(new USPostCodeAndStateHandler());
        engine.addHandler(new CityAndCountyHandler());
        engine.addHandler(new CMRNoHandler());
        engine.addHandler(new EnterpriseAffiliateHandler());
        engine.addHandler(new InternalTypeAbbrevNameHandler());
      }

      engines.put(cmrIssuingCountry, engine);
    }
    return engines.get(cmrIssuingCountry);
  }

  private boolean hasRecordsForIteration(EntityManager entityManager, long reqId, int iterationId) {
    String sql = ExternalizedQuery.getSql("MC.CHECK_MC_ITERATION");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", reqId);
    query.setParameter("ITER_ID", iterationId);
    return query.exists();
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
  protected String getThreadName() {
    return "MassCreateMulti";
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
