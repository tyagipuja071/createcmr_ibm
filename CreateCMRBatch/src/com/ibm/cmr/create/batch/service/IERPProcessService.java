package com.ibm.cmr.create.batch.service;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import javax.persistence.EntityManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.AddrRdc;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.CompoundEntity;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.entity.DataRdc;
import com.ibm.cio.cmr.request.entity.MassUpdt;
import com.ibm.cio.cmr.request.entity.ReqCmtLog;
import com.ibm.cio.cmr.request.entity.ReqCmtLogPK;
import com.ibm.cio.cmr.request.entity.SuppCntry;
import com.ibm.cio.cmr.request.entity.WfHist;
import com.ibm.cio.cmr.request.entity.listeners.ChangeLogListener;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.util.IERPRequestUtils;
import com.ibm.cio.cmr.request.util.RequestUtils;
import com.ibm.cio.cmr.request.util.SystemLocation;
import com.ibm.cio.cmr.request.util.SystemUtil;
import com.ibm.cio.cmr.request.util.geo.GEOHandler;
import com.ibm.cio.cmr.request.util.geo.impl.CNHandler;
import com.ibm.cio.cmr.request.util.geo.impl.DEHandler;
import com.ibm.cio.cmr.request.util.geo.impl.LAHandler;
import com.ibm.cmr.create.batch.model.CmrServiceInput;
import com.ibm.cmr.create.batch.model.MassUpdateServiceInput;
import com.ibm.cmr.create.batch.util.BatchUtil;
import com.ibm.cmr.create.batch.util.DebugUtil;
import com.ibm.cmr.create.batch.util.ProfilerLogger;
import com.ibm.cmr.services.client.CmrServicesFactory;
import com.ibm.cmr.services.client.GenerateCMRNoClient;
import com.ibm.cmr.services.client.ProcessClient;
import com.ibm.cmr.services.client.cmrno.GenerateCMRNoRequest;
import com.ibm.cmr.services.client.cmrno.GenerateCMRNoResponse;
import com.ibm.cmr.services.client.process.ProcessRequest;
import com.ibm.cmr.services.client.process.ProcessResponse;
import com.ibm.cmr.services.client.process.RDcRecord;
import com.ibm.cmr.services.client.process.mass.MassProcessRequest;
import com.ibm.cmr.services.client.process.mass.MassUpdateRecord;
import com.ibm.cmr.services.client.process.mass.RequestValueRecord;

public class IERPProcessService extends BaseBatchService {

  private static final String BATCH_SERVICES_URL = SystemConfiguration.getValue("BATCH_SERVICES_URL");
  private ProcessClient serviceClient;
  private static final String COMMENT_LOGGER = "IERP Process Service";
  public static final String CMR_REQUEST_REASON_TEMP_REACT_EMBARGO = "TREC";
  public static final String CMR_REQUEST_STATUS_CPR = "CPR";
  public static final String CMR_REQUEST_STATUS_PCR = "PCR";
  protected static final String ACTION_RDC_UPDATE = "System Action:RDc Update";
  private boolean multiMode;
  // private List<Long> pendingReqIds;

  @Override
  protected Boolean executeBatch(EntityManager entityManager) throws Exception {
    try {

      ChangeLogListener.setUser(COMMENT_LOGGER);
      initClient();

      List<Long> reqIds = gatherDRPending(entityManager);

      monitorCreqcmr(entityManager, reqIds);

    } catch (Exception e) {
      e.printStackTrace();
      addError(e);
      return false;
    }
    return true;
  }

  @Override
  protected boolean isTransactional() {
    return true;
  }

  public MassProcessRequest prepareReactivateDelRequest(EntityManager entityManager, Admin admin, Data data, MassUpdateServiceInput input)
      throws JsonGenerationException, JsonMappingException, IOException, Exception {

    MassProcessRequest request = new MassProcessRequest();
    // request.setMandt(input.getInputMandt());
    request.setMandt(SystemConfiguration.getValue("MANDT"));
    request.setReqId(input.getInputReqId());
    request.setReqType(input.getInputReqType());
    request.setUserId(input.getInputUserId());

    long reqId = admin.getId().getReqId();
    long iterationId = admin.getIterationId();
    String sysLoc = data.getCmrIssuingCntry();

    // get PASS records for the current iteration
    List<MassUpdateRecord> updtRecordsList = new ArrayList<MassUpdateRecord>();
    PreparedQuery massquery = new PreparedQuery(entityManager, ExternalizedQuery.getSql("BATCH.REACT_DEL_RECORDS"));
    massquery.setParameter("REQ_ID", reqId);
    massquery.setParameter("ITERATION_ID", iterationId);
    massquery.setParameter("STATUS", "READY");
    List<CompoundEntity> results = massquery.getCompundResults(Admin.class, Admin.BATCH_REACT_DEL_SERVICE_MAPPING);

    MassUpdt massUpdt = null;
    MassUpdateRecord record = null;
    LOG.debug("Size of Mass Update Record list for Iteration " + iterationId + " : " + results.size());
    for (CompoundEntity entity : results) {
      record = new MassUpdateRecord();
      massUpdt = entity.getEntity(MassUpdt.class);

      // set the values in the update record
      record.setCmrNo(massUpdt.getCmrNo());
      record.setSysLoc(sysLoc);
      record.setAddrType("ZS01");

      // set all the name and values pair the document here
      List<RequestValueRecord> requestValueRecords = new ArrayList<RequestValueRecord>();

      // add LOEVM_Record
      if (CmrConstants.REQ_TYPE_DELETE.equals(input.getInputReqType())) {
        RequestValueRecord loevm = new RequestValueRecord();
        loevm.setField("LOEVM");
        loevm.setValue("X");
        requestValueRecords.add(loevm);
      } else if (CmrConstants.REQ_TYPE_REACTIVATE.equals(input.getInputReqType())) {
        RequestValueRecord loevm = new RequestValueRecord();
        loevm.setField("LOEVM");
        loevm.setValue("");
        requestValueRecords.add(loevm);
      }

      // set requestValueRecords list updatRec

      record.setValues(requestValueRecords);
      // set updtRec in updtRecordsList
      updtRecordsList.add(record);
      // request.getRecords()
    }
    // set updtRecordsList in request
    request.setRecords(updtRecordsList);

    return request;

  }

  protected void processReactivateDeleteRequest(EntityManager entityManager, Admin admin, Data data) throws Exception {

    // Create the request

    long reqId = admin.getId().getReqId();
    long iterationId = admin.getIterationId();
    String processingStatus = admin.getRdcProcessingStatus();

    MassUpdateServiceInput input = TransConnService.prepareMassChangeInput(entityManager, admin);

    MassProcessRequest request = new MassProcessRequest();
    // set the update mass record in request
    if (input.getInputReqType() != null && (input.getInputReqType().equalsIgnoreCase("D") || input.getInputReqType().equalsIgnoreCase("R"))) {
      request = prepareReactivateDelRequest(entityManager, admin, data, input);
    }

    createComment(entityManager, "Processing started.", admin.getId().getReqId());
    // to indicate that batch has picked but pending complete
    admin.setReqStatus("PCO");

    LOG.trace("Request JSON:");
    if (LOG.isTraceEnabled()) {
      DebugUtil.printObjectAsJson(LOG, request);
    }
    // actual service call here
    ProcessResponse response = null;
    // String applicationId = BatchUtil.getAppId(data.getCmrIssuingCntry());
    /*
     * if (applicationId == null) { LOG.debug("No Application ID mapped to " +
     * data.getCmrIssuingCntry()); response = new ProcessResponse();
     * response.setReqId(request.getReqId());
     * response.setMandt(request.getMandt());
     * response.setStatus(CmrConstants.RDC_STATUS_NOT_COMPLETED);
     * response.setMessage("No application ID defined for Country: " +
     * data.getCmrIssuingCntry() + ". Cannot process RDc records."); } else {
     */
    try {
      this.serviceClient.setReadTimeout(60 * 20 * 1000); // 20 mins
      response = this.serviceClient.executeAndWrap(ProcessClient.IERP_APP_ID, request, ProcessResponse.class);
      response.setReqId(request.getReqId());
    } catch (Exception e) {
      LOG.error("Error when connecting to the mass change service.", e);
      response = new ProcessResponse();
      response.setReqId(request.getReqId());
      response.setStatus(CmrConstants.RDC_STATUS_ABORTED);
      response.setMessage("A system error has occured. Setting to aborted.");
    }
    // }

    LOG.trace("Response JSON:");
    if (LOG.isTraceEnabled()) {
      DebugUtil.printObjectAsJson(LOG, response);
    }
    LOG.info("Response received from Process Service [Request ID: " + response.getReqId() + " Status: " + response.getStatus() + " Message: "
        + (response.getMessage() != null ? response.getMessage() : "-") + "]");

    if (response.getReqId() <= 0) {
      response.setReqId(request.getReqId());
    }

    String resultCode = response.getStatus();
    if (StringUtils.isBlank(resultCode)) {
      resultCode = CmrConstants.RDC_STATUS_NOT_COMPLETED;
    }

    try {
      // update MASS_UPDT table with the error txt and row status cd
      if (response != null) {
        if (response.getRecords() != null && response.getRecords().size() > 0) {
          for (RDcRecord record : response.getRecords()) {
            PreparedQuery updtQuery = new PreparedQuery(entityManager, ExternalizedQuery.getSql("BATCH.GET_MASS_UPDT_ENTITY"));
            updtQuery.setParameter("REQ_ID", reqId);
            updtQuery.setParameter("ITERATION_ID", iterationId);
            updtQuery.setParameter("CMR_NO", record.getCmrNo());
            List<MassUpdt> updateList = updtQuery.getResults(MassUpdt.class);

            for (MassUpdt massUpdt : updateList) {
              massUpdt.setErrorTxt(record.getMessage());
              if (CmrConstants.RDC_STATUS_NOT_COMPLETED.equals(record.getStatus())) {
                massUpdt.setRowStatusCd("RDCER");
              } else if (CmrConstants.RDC_STATUS_COMPLETED.equals(record.getStatus())
                  || CmrConstants.RDC_STATUS_COMPLETED_WITH_WARNINGS.equals(record.getStatus())) {
                massUpdt.setRowStatusCd("DONE");
              }
              LOG.info("Mass Update Record Updated [Request ID: " + massUpdt.getId().getParReqId() + " CMR_NO: " + massUpdt.getCmrNo() + " SEQ No: "
                  + massUpdt.getId().getSeqNo() + "]");
              updateEntity(massUpdt, entityManager);
            }
          }
        }
      }

      String requestType = "";
      String action = "";
      switch (admin.getReqType()) {
      case "D":
        action = TransConnService.ACTION_RDC_DELETE;
        requestType = "Mass Delete";
        break;
      case "R":
        action = TransConnService.ACTION_RDC_REACTIVATE;
        requestType = "Mass Reactivate";
        break;
      }

      // create comment log and workflow history entries for update type of
      // request
      StringBuilder comment = new StringBuilder();
      if (isCompletedSuccessfully(resultCode)) {
        comment = comment.append(requestType + " in RDc successfully completed.");
      } else {
        if (CmrConstants.RDC_STATUS_ABORTED.equals(resultCode) && CmrConstants.RDC_STATUS_ABORTED.equals(processingStatus)) {
          comment = comment.append(requestType + " in RDc failed: " + response.getMessage());
        } else if (CmrConstants.RDC_STATUS_ABORTED.equalsIgnoreCase(resultCode)) {
          comment = comment.append(requestType + " in RDc aborted: " + response.getMessage() + "\nSystem will retry once.");
        } else if (CmrConstants.RDC_STATUS_NOT_COMPLETED.equalsIgnoreCase(resultCode)) {
          comment = comment.append(requestType + " in RDc failed: " + response.getMessage());
        } else if (CmrConstants.RDC_STATUS_IGNORED.equalsIgnoreCase(resultCode)) {
          comment = comment.append(requestType + " in RDc skipped: " + response.getMessage());
        }
      }

      createCommentLog(entityManager, admin, comment.toString());

      if (!CmrConstants.RDC_STATUS_IGNORED.equals(resultCode)) {
        RequestUtils.createWorkflowHistoryFromBatch(entityManager, BATCH_USER_ID, admin, comment.toString().trim(), action, null, null,
            "COM".equals(admin.getReqStatus()));
      }
      // RequestUtils.createCommentLogFromBatch(entityManager, BATCH_USER_ID,
      // admin.getId().getReqId(), comment.toString().trim());

      // only update Admin record once depending on the overall status of the
      // request
      LOG.debug("Updating Admin record for Request ID " + admin.getId().getReqId());

      if (CmrConstants.RDC_STATUS_ABORTED.equals(resultCode) || CmrConstants.RDC_STATUS_NOT_COMPLETED.equals(resultCode)) {
        // reject and send back to processor
        admin.setRdcProcessingStatus(CmrConstants.RDC_STATUS_NOT_COMPLETED);
        admin.setReqStatus("PPN");
        admin.setProcessedFlag("E"); // set request status to error.

      } else if (CmrConstants.RDC_STATUS_IGNORED.equals(resultCode)) {
        admin.setRdcProcessingStatus(CmrConstants.RDC_STATUS_COMPLETED_WITH_WARNINGS);
        admin.setReqStatus("COM");
        admin.setProcessedFlag("N"); // set request status to not processed.

      } else if (CmrConstants.RDC_STATUS_COMPLETED.equals(resultCode)) {
        admin.setRdcProcessingStatus(CmrConstants.RDC_STATUS_COMPLETED);
        admin.setReqStatus("COM");
        admin.setProcessedFlag("Y"); // set request status to processed.
      } else {
        admin.setRdcProcessingStatus(resultCode);
      }

      admin.setRdcProcessingTs(SystemUtil.getCurrentTimestamp());
      admin.setRdcProcessingMsg(response.getMessage());
      updateEntity(admin, entityManager);
      LOG.debug("Request ID " + admin.getId().getReqId() + " Status: " + admin.getRdcProcessingStatus() + " Message: " + admin.getRdcProcessingMsg());

      partialCommit(entityManager);
      try {
        String siteIds = "";
        sendEmailNotifications(entityManager, admin, siteIds, comment.toString());
      } catch (Exception e) {
        e.printStackTrace();
        LOG.error("ERROR: " + e.getMessage());
      }

    } catch (Exception e) {
      LOG.error("Error in processing Mass Change Request  " + admin.getId().getReqId(), e);
      addError("Mass Change Request " + admin.getId().getReqId() + " Error: " + e.getMessage());
    }

  }

  protected boolean isCompletedSuccessfully(String status) {
    return CmrConstants.RDC_STATUS_COMPLETED.equals(status) || CmrConstants.RDC_STATUS_COMPLETED_WITH_WARNINGS.equals(status);
  }

  @SuppressWarnings("unchecked")
  public void monitorCreqcmr(EntityManager em, List<Long> pending) throws JsonGenerationException, JsonMappingException, IOException, Exception {
    String actionRdc = "";
    StringBuffer siteIds = new StringBuffer();
    List<CompoundEntity> reqIdList = null;

    if (multiMode) {
      reqIdList = getPendingRequestByReqIds(em, pending);
    } else {
      reqIdList = getPendingRequest(em);
    }
    LOG.debug("Size of REQ_ID list : " + reqIdList.size());

    if (reqIdList != null && !reqIdList.isEmpty()) {
      // lock records for processing
      // lockAdminRecordsForProcessing(reqIdList, em);

      // start processing
      ArrayList<String> cmrNoList = new ArrayList<String>();
      for (CompoundEntity entity : reqIdList) {
        long start = new Date().getTime();
        Admin admin = entity.getEntity(Admin.class);
        if (BatchUtil.excludeForEnvironment(this.context, em, admin)) {
          // exclude data created from a diff env
          continue;
        }
        Data data = entity.getEntity(Data.class);
        Thread.currentThread().setName("REQ-" + data.getId().getReqId());
        // create a entry in request's comment log re_cmt_log table
        if (admin != null && !CMR_REQUEST_STATUS_CPR.equals(admin.getReqStatus())) {
          createCommentLog(em, admin, "RDc processing has started. Waiting for completion.");
        }
        CmrServiceInput cmrServiceInput = getReqParam(em, admin.getId().getReqId(), admin.getReqType(), data, admin, cmrNoList);

        GEOHandler cntryHandler = RequestUtils.getGEOHandler(data.getCmrIssuingCntry());
        boolean enableTempReact = cntryHandler.enableTempReactivateOnUpdate() && CMR_REQUEST_REASON_TEMP_REACT_EMBARGO.equals(admin.getReqReason());

        if ((SystemLocation.GERMANY.equals(data.getCmrIssuingCntry()) || SystemLocation.CHINA.equals(data.getCmrIssuingCntry()))
            && CMR_REQUEST_REASON_TEMP_REACT_EMBARGO.equals(admin.getReqReason())) {
          enableTempReact = true;
        }

        ProcessResponse response = null;
        String overallStatus = null;

        try {
          switch (admin.getReqType()) {
          case CmrConstants.REQ_TYPE_REACTIVATE:
          case CmrConstants.REQ_TYPE_DELETE:
            processReactivateDeleteRequest(em, admin, data);
            break;
          }
          if (!(CmrConstants.REQ_TYPE_DELETE.equalsIgnoreCase(admin.getReqType())
              && CmrConstants.REQ_TYPE_REACTIVATE.equalsIgnoreCase(admin.getReqType()))) {
            if (CmrConstants.RDC_STATUS_ABORTED.equalsIgnoreCase(admin.getRdcProcessingStatus())
                || CmrConstants.RDC_STATUS_NOT_COMPLETED.equalsIgnoreCase(admin.getRdcProcessingStatus())) {
              admin.setReqStatus("PPN");
              admin.setProcessedFlag("E"); // set request status to error.
              WfHist hist = createHistory(em, "Sending back to processor due to error on RDC processing", "PPN", "RDC Processing",
                  admin.getId().getReqId());
            } else if ((CmrConstants.RDC_STATUS_COMPLETED.equalsIgnoreCase(admin.getRdcProcessingStatus())
                || CmrConstants.RDC_STATUS_COMPLETED_WITH_WARNINGS.equalsIgnoreCase(admin.getRdcProcessingStatus()))
                && !"TREC".equalsIgnoreCase(admin.getReqReason())) {
              admin.setReqStatus("COM");
              admin.setProcessedFlag("Y"); // set request status to processed
              WfHist hist = createHistory(em, "Request processing Completed Successfully", "COM", "RDC Processing", admin.getId().getReqId());
            }
          }
          partialCommit(em);
        } catch (Exception e) {
          partialRollback(em);
          LOG.error("Unexpected error occurred during processing of Request " + admin.getId().getReqId(), e);
          processError(em, admin, e.getMessage());
        }

        boolean prospectConversion = CmrConstants.YES_NO.Y.equals(admin.getProspLegalInd()) ? true : false;

        if (CmrConstants.REQ_TYPE_UPDATE.equals(cmrServiceInput.getInputReqType()) && !enableTempReact) {
          actionRdc = "System Action:RDc Update";
          StringBuffer statusMessage = new StringBuffer();
          List<ProcessResponse> responses = null;
          HashMap<String, Object> overallResponse = null;
          if (!CmrConstants.DE_CND_ISSUING_COUNTRY_VAL.contains(data.getCmrIssuingCntry())
              && !CNHandler.isCNIssuingCountry(data.getCmrIssuingCntry())) {
            overallResponse = processUpdateRequestLegacyStyle(admin, data, cmrServiceInput, em);
          } else {
            overallResponse = processUpdateRequest(admin, data, cmrServiceInput, em);
          }
          String rdcProcessingMessage = "";
          String wfHistCmt = "";

          if (overallResponse != null) {
            overallStatus = (String) overallResponse.get("overallStatus");
            responses = (List<ProcessResponse>) overallResponse.get("responses");

            if (CmrConstants.RDC_STATUS_COMPLETED.equals(overallStatus)) {
              wfHistCmt = "All address records on request ID " + admin.getId().getReqId() + " were SUCCESSFULLY processed";
              statusMessage.append("Record with the following CMR number, Address sequence and address types on request ID "
                  + admin.getId().getReqId() + " was SUCCESSFULLY processed:\n");
              if (responses != null && responses.size() > 0) {
                for (int i = 0; i < responses.size(); i++) {
                  ProcessResponse resp = responses.get(i);

                  if (resp != null && resp.getRecords() != null && resp.getRecords().size() > 0) {
                    if (i == 0) {
                      rdcProcessingMessage = resp.getMessage();
                    }
                    statusMessage.append("CMR Number " + resp.getCmrNo() + ", sequence number " + resp.getRecords().get(0).getSeqNo() + ", ");
                    statusMessage.append(" address type " + resp.getRecords().get(0).getAddressType() + "\n");
                  }
                }
              }
            } else if (CmrConstants.RDC_STATUS_ABORTED.equals(overallStatus)) {
              statusMessage.append("Record with request ID " + admin.getId().getReqId() + " has FAILED processing. Status: ABORTED");

              if (responses != null && responses.size() > 0) {
                ProcessResponse resp = responses.get(0);
                statusMessage.append(". Reason: " + resp.getMessage());
              }

              wfHistCmt = statusMessage.toString();
              if (responses != null && responses.size() > 0) {
                ProcessResponse resp = responses.get(0);
                rdcProcessingMessage = resp.getMessage();
              }

            } else if (CmrConstants.RDC_STATUS_NOT_COMPLETED.equals(overallStatus)) {
              String ncMessage = "";
              if (responses != null && responses.size() > 0) {
                ProcessResponse resp = responses.get(0);
                rdcProcessingMessage = resp.getMessage();
                ncMessage = resp.getMessage();
              }

              statusMessage.append("Record with request ID " + admin.getId().getReqId()
                  + " has FAILED processing. Status: NOT COMPLETED. Response message: " + ncMessage);
              wfHistCmt = statusMessage.toString();
            }

            if (!CmrConstants.ANZ_COUNTRIES.contains(data.getCmrIssuingCntry())) {
            statusMessage = processPartnerFunctionForZS01(admin, data, cmrServiceInput, overallStatus, statusMessage);
            }
            
            createCommentLog(em, admin, statusMessage.toString());

            String disableAutoProc = "N";
            if (CmrConstants.RDC_STATUS_COMPLETED.equals(overallStatus)) {
              disableAutoProc = CmrConstants.YES_NO.Y.toString();
            } else {
              disableAutoProc = CmrConstants.YES_NO.N.toString();
            }

            // update admin status
            admin.setDisableAutoProc(disableAutoProc);
            LOG.debug("*** Setting DISABLE_AUTO_PROC >> " + admin.getDisableAutoProc());
            admin.setProcessedFlag(
                CmrConstants.RDC_STATUS_COMPLETED.equals(overallStatus) ? CmrConstants.YES_NO.Y.toString() : CmrConstants.YES_NO.N.toString());
            LOG.debug("*** Setting PROCESSED_FLAG >> " + admin.getProcessedFlag());

            /*
             * jzamora - edited to return the request back to processor if an
             * error occurred, to avoid endless loop
             */
            if (CmrConstants.RDC_STATUS_COMPLETED.equals(overallStatus)) {
              admin.setReqStatus(CmrConstants.REQUEST_STATUS.COM.toString());
            } else {
              LOG.debug("Unlocking request due to error..");
              admin.setReqStatus(CmrConstants.REQUEST_STATUS.PPN.toString());
              admin.setLockBy(null);
              admin.setLockByNm(null);
              admin.setLockTs(null);
              admin.setLockInd(CmrConstants.YES_NO.N.toString());
              admin.setProcessedFlag("E");
            }
            // admin.setReqStatus(CmrConstants.RDC_STATUS_COMPLETED.equals(overallStatus)
            // ? CmrConstants.REQUEST_STATUS.COM.toString()
            // : CmrConstants.REQUEST_STATUS.PCP.toString());
            LOG.debug("*** Setting REQ_STATUS >> " + admin.getReqStatus());

            if (CmrConstants.RDC_STATUS_NOT_COMPLETED.equals(overallStatus) || CmrConstants.RDC_STATUS_ABORTED.equals(overallStatus)) {
              admin.setRdcProcessingStatus(CmrConstants.RDC_STATUS_NOT_COMPLETED);
            } else {
              admin.setRdcProcessingStatus(overallStatus);
            }
            LOG.debug("*** Setting RDC_PROCESSING_STATUS >> " + admin.getRdcProcessingStatus());

            admin.setRdcProcessingMsg(rdcProcessingMessage);
            admin.setRdcProcessingTs(SystemUtil.getCurrentTimestamp());
            updateEntity(admin, em);

            siteIds = new StringBuffer((String) overallResponse.get("siteIds"));

            if (!CmrConstants.RDC_STATUS_IGNORED.equals(overallStatus)) {
              IERPRequestUtils.createWorkflowHistoryFromBatch(em, BATCH_USER_ID, admin, wfHistCmt.trim(), actionRdc, null, null,
                  "COM".equals(admin.getReqStatus()));
            }

            partialCommit(em);

            // send email notif regardless of abort or complete
            LOG.debug("*** IERP Site IDs on EMAIL >> " + siteIds.toString());
            try {
              sendEmailNotifications(em, admin, siteIds.toString(), statusMessage.toString());
            } catch (Exception e) {
              LOG.error("ERROR: " + e.getMessage());
            }

          } // if overallResponse is not null

        } else if (CmrConstants.REQ_TYPE_UPDATE.equals(cmrServiceInput.getInputReqType()) && enableTempReact) {
          actionRdc = "System Action:RDc Update";
          StringBuffer statusMessage = new StringBuffer();
          List<ProcessResponse> responses = null;
          String rdcProcessingMessage = "";
          String wfHistCmt = "";
          DataRdc dataRdc = getDataRdcRecords(em, data);
          String rdcOrderBlk = dataRdc.getCustAcctType();
          String dataOrderBlk = data.getCustAcctType();
          StringBuilder comment = new StringBuilder();
          HashMap<String, Object> overallResponse = null;
          boolean firstRun = false;
          
          // For temporary reactivate
          if (SystemLocation.CHINA.equals(data.getCmrIssuingCntry()) || SystemLocation.GERMANY.equals(data.getCmrIssuingCntry())) {
            rdcOrderBlk = dataRdc.getOrdBlk();
            data.setOrdBlk("");
            dataOrderBlk = data.getOrdBlk();
          }
          
          if ((admin.getReqReason() != null && !StringUtils.isBlank(admin.getReqReason()))
              && CMR_REQUEST_REASON_TEMP_REACT_EMBARGO.equals(admin.getReqReason()) && (rdcOrderBlk != null && !StringUtils.isBlank(rdcOrderBlk))
              && CmrConstants.ORDER_BLK_LIST.contains(rdcOrderBlk) && (dataOrderBlk == null || StringUtils.isBlank(dataOrderBlk))) {
            if (admin.getProcessedTs() == null || IERPRequestUtils.isTimeStampEquals(admin.getProcessedTs())) {
              firstRun = true;
              if (SystemLocation.CHINA.equals(data.getCmrIssuingCntry())) {
                overallResponse = processUpdateRequest(admin, data, cmrServiceInput, em);
              } else {
                overallResponse = processUpdateRequestLegacyStyle(admin, data, cmrServiceInput, em);
              }
            } else {
              int noOFWorkingDays = 0;
              if (admin.getReqStatus() != null && !CMR_REQUEST_STATUS_CPR.equals(admin.getReqStatus())
                  && CMR_REQUEST_REASON_TEMP_REACT_EMBARGO.equals(admin.getReqReason()) && (rdcOrderBlk != null && !StringUtils.isBlank(rdcOrderBlk))
                  && CmrConstants.ORDER_BLK_LIST.contains(rdcOrderBlk) && (dataOrderBlk == null || StringUtils.isBlank(dataOrderBlk))) {
                admin.setReqStatus(CMR_REQUEST_STATUS_CPR);
              }
              if (admin.getReqStatus() != null && CMR_REQUEST_STATUS_CPR.equals(admin.getReqStatus())) {
                LOG.debug("no Of Working days before check= " + noOFWorkingDays + " For Request ID=" + admin.getId().getReqId());
                noOFWorkingDays = IERPRequestUtils.checkNoOfWorkingDays(admin.getProcessedTs(), SystemUtil.getCurrentTimestamp());
                LOG.debug("no Of Working days after check= " + noOFWorkingDays + " For Request ID=" + admin.getId().getReqId());

              }
              int tempReactThres = 4;
              if (noOFWorkingDays >= tempReactThres) {
                LOG.debug("Processing 2nd time ,no Of Working days = " + noOFWorkingDays);
                createCommentLog(em, admin, "RDc processing has started. Waiting for completion.");
                data.setCustAcctType(rdcOrderBlk);
                data.setOrdBlk(rdcOrderBlk);
                updateEntity(data, em);
                if (SystemLocation.CHINA.equals(data.getCmrIssuingCntry())) {
                  overallResponse = processUpdateRequest(admin, data, cmrServiceInput, em);
                } else {
                  overallResponse = processUpdateRequestLegacyStyle(admin, data, cmrServiceInput, em);
                }
              }
            }
          }

          if (overallResponse != null) {
            overallStatus = (String) overallResponse.get("overallStatus");
            responses = (List<ProcessResponse>) overallResponse.get("responses");

            if (CmrConstants.RDC_STATUS_COMPLETED.equals(overallStatus)) {
              wfHistCmt = "All address records on request ID " + admin.getId().getReqId() + " were SUCCESSFULLY processed";
              statusMessage.append("Record with the following CMR number, Address sequence and address types on request ID "
                  + admin.getId().getReqId() + " was SUCCESSFULLY processed:\n");
              if (responses != null && responses.size() > 0) {
                for (int i = 0; i < responses.size(); i++) {
                  ProcessResponse resp = responses.get(i);

                  if (resp != null && resp.getRecords() != null && resp.getRecords().size() > 0) {
                    if (i == 0) {
                      rdcProcessingMessage = resp.getMessage();
                    }
                    statusMessage.append("CMR Number " + resp.getCmrNo() + ", sequence number " + resp.getRecords().get(0).getSeqNo() + ", ");
                    statusMessage.append(" address type " + resp.getRecords().get(0).getAddressType() + "\n");
                  }
                }
              }
              if (firstRun) {
                statusMessage.append("Temporary Reactivation - Order Block removal process done in RDc.\n");
              } else {
                statusMessage.append("RDc Processing has been completed at 2nd time for Temporary Reactivate Embargo Code.");
              }
            } else if (CmrConstants.RDC_STATUS_ABORTED.equals(overallStatus)) {
              statusMessage.append("Record with request ID " + admin.getId().getReqId() + " has FAILED processing. Status: ABORTED");

              if (responses != null && responses.size() > 0) {
                ProcessResponse resp = responses.get(0);
                statusMessage.append(". Reason: " + resp.getMessage());
              }

              wfHistCmt = statusMessage.toString();
              if (responses != null && responses.size() > 0) {
                ProcessResponse resp = responses.get(0);
                rdcProcessingMessage = resp.getMessage();
              }

            } else if (CmrConstants.RDC_STATUS_NOT_COMPLETED.equals(overallStatus)) {
              String ncMessage = "";
              if (responses != null && responses.size() > 0) {
                ProcessResponse resp = responses.get(0);
                rdcProcessingMessage = resp.getMessage();
                ncMessage = resp.getMessage();
              }

              statusMessage.append("Record with request ID " + admin.getId().getReqId()
                  + " has FAILED processing. Status: NOT COMPLETED. Response message: " + ncMessage);
              wfHistCmt = statusMessage.toString();
            }
            
            if (!CmrConstants.ANZ_COUNTRIES.contains(data.getCmrIssuingCntry()) && !SystemLocation.CHINA.equals(data.getCmrIssuingCntry())) {
              statusMessage = processPartnerFunctionForZS01(admin, data, cmrServiceInput, overallStatus, statusMessage);
            }
            createCommentLog(em, admin, statusMessage.toString());

            String disableAutoProc = "N";
            if (CmrConstants.RDC_STATUS_COMPLETED.equals(overallStatus)) {
              disableAutoProc = CmrConstants.YES_NO.Y.toString();
            } else {
              disableAutoProc = CmrConstants.YES_NO.N.toString();
            }

            // update admin status
            admin.setDisableAutoProc(disableAutoProc);
            LOG.debug("*** Setting DISABLE_AUTO_PROC >> " + admin.getDisableAutoProc());
            admin.setProcessedFlag(
                CmrConstants.RDC_STATUS_COMPLETED.equals(overallStatus) ? CmrConstants.YES_NO.Y.toString() : CmrConstants.YES_NO.N.toString());
            LOG.debug("*** Setting PROCESSED_FLAG >> " + admin.getProcessedFlag());

            /*
             * jzamora - edited to return the request back to processor if an
             * error occurred, to avoid endless loop
             */
            if (CmrConstants.RDC_STATUS_COMPLETED.equals(overallStatus) && !firstRun) {
              admin.setReqStatus(CmrConstants.REQUEST_STATUS.COM.toString());
            } else if (CmrConstants.RDC_STATUS_COMPLETED.equals(overallStatus) && firstRun) {
              admin.setReqStatus(CMR_REQUEST_STATUS_CPR);
              admin.setDisableAutoProc(CmrConstants.YES_NO.N.toString());
              admin.setProcessedFlag(CmrConstants.YES_NO.N.toString());
              admin.setProcessedTs(SystemUtil.getCurrentTimestamp());
            } else {
              LOG.debug("Unlocking request due to error..");
              admin.setReqStatus(CmrConstants.REQUEST_STATUS.PPN.toString());
              admin.setLockBy(null);
              admin.setLockByNm(null);
              admin.setLockTs(null);
              admin.setLockInd(CmrConstants.YES_NO.N.toString());
              admin.setProcessedFlag("E");
              if (firstRun) {
                // revert as rdc run has resulted into error
                data.setCustAcctType("");
                updateEntity(data, em);
              }
            }
            // admin.setReqStatus(CmrConstants.RDC_STATUS_COMPLETED.equals(overallStatus)
            // ? CmrConstants.REQUEST_STATUS.COM.toString()
            // : CmrConstants.REQUEST_STATUS.PCP.toString());
            LOG.debug("*** Setting REQ_STATUS >> " + admin.getReqStatus());

            if (CmrConstants.RDC_STATUS_NOT_COMPLETED.equals(overallStatus) || CmrConstants.RDC_STATUS_ABORTED.equals(overallStatus)) {
              admin.setRdcProcessingStatus(CmrConstants.RDC_STATUS_NOT_COMPLETED);
            } else {
              admin.setRdcProcessingStatus(overallStatus);
            }
            LOG.debug("*** Setting RDC_PROCESSING_STATUS >> " + admin.getRdcProcessingStatus());

            admin.setRdcProcessingMsg(rdcProcessingMessage);
            admin.setRdcProcessingTs(SystemUtil.getCurrentTimestamp());
            updateEntity(admin, em);

            if (!SystemLocation.GERMANY.equals(data.getCmrIssuingCntry())) {
              siteIds = new StringBuffer((String) overallResponse.get("siteIds"));
            } else {
              siteIds = new StringBuffer(dataRdc.getSitePartyId());
            }

            if (!CmrConstants.RDC_STATUS_IGNORED.equals(overallStatus) && !firstRun) {
              IERPRequestUtils.createWorkflowHistoryFromBatch(em, BATCH_USER_ID, admin, wfHistCmt.trim(), actionRdc, null, null,
                  "COM".equals(admin.getReqStatus()));
            }

            WfHist history = null;

            if (!CmrConstants.RDC_STATUS_IGNORED.equals(overallStatus) && firstRun) {
              history = IERPRequestUtils.createWorkflowHistoryFromBatch(em, BATCH_USER_ID, admin, wfHistCmt.trim(), actionRdc, null, null,
                  "CPR".equals(admin.getReqStatus()));
            }

            partialCommit(em);

            // send email notif regardless of abort or complete
            LOG.debug("*** IERP Site IDs on EMAIL >> " + siteIds.toString());
            try {
              if ("CPR".equals(admin.getReqStatus())) {
                RequestUtils.sendEmailNotifications(em, admin, history, false, false);
              } else {
                sendEmailNotifications(em, admin, siteIds.toString(), statusMessage.toString());
              }
            } catch (Exception e) {
              LOG.error("ERROR: " + e.getMessage());
            }
            // if overallResponse is not null
          } else if (overallResponse == null && firstRun) {
            // revert as rdc run has resulted into error
            data.setCustAcctType("");
            updateEntity(data, em);
          }

        } else if (CmrConstants.REQ_TYPE_CREATE.equals(cmrServiceInput.getInputReqType())) {
          response = processCreateRequest(admin, cmrServiceInput, em);
          actionRdc = "System Action:RDc Create";

          // if status is completed, we need to do something
          String currProcStatus = admin.getRdcProcessingStatus();
          // Admin adminEntityWf = new Admin();
          LOG.debug("Updating Admin table. Current Status: " + currProcStatus);

          StringBuffer statusMessage = new StringBuffer();
          if (CmrConstants.RDC_STATUS_COMPLETED.equals(response.getStatus())) {
            overallStatus = CmrConstants.RDC_STATUS_COMPLETED;
            // response = processCreateRequest(admin, cmrServiceInput, em);
            statusMessage
                .append("Record with request ID " + admin.getId().getReqId() + " and CMR Number " + response.getCmrNo() + " created SUCCESSFULLY. ");
            statusMessage.append("CMR No. " + response.getCmrNo() + " generated for this request. ");
            if (prospectConversion) {
              statusMessage.append(" RDc processing converted prospect " + cmrServiceInput + " to KUNNR(s): ");
            } else {
              statusMessage.append(" RDc processing successfully created KUNNR(s): ");
            }
            if (response.getRecords() != null && response.getRecords().size() != 0) {
              for (int i = 0; i < response.getRecords().size(); i++) {
                statusMessage.append(response.getRecords().get(i).getSapNo() + " ");

                if (StringUtils.isEmpty(siteIds.toString())) {
                  siteIds.append(response.getRecords().get(i).getIerpSitePartyId());
                } else {
                  siteIds.append(", " + response.getRecords().get(i).getIerpSitePartyId());
                }
              }
            }
          } else if (CmrConstants.RDC_STATUS_ABORTED.equals(response.getStatus())) {
            overallStatus = CmrConstants.RDC_STATUS_ABORTED;
            statusMessage.append("Record with request ID " + admin.getId().getReqId() + " has FAILED processing. Status: ABORTED");
          } else if (CmrConstants.RDC_STATUS_NOT_COMPLETED.equals(response.getStatus())) {
            overallStatus = CmrConstants.RDC_STATUS_NOT_COMPLETED;
            statusMessage.append("Record with request ID " + admin.getId().getReqId()
                + " has FAILED processing. Status: NOT COMPLETED. Response message: " + response.getMessage());
          } else {
            statusMessage.append("Record with request ID " + admin.getId().getReqId() + " has FAILED processing. Status: ABORTED ");

            if (response.getStatus() == null) {
              statusMessage.append("Service Response: EMPTY SERVICE STATUS ON RESPONSE");
            } else {
              statusMessage.append("Service Response: " + response.getStatus());
            }

          }

          createCommentLog(em, admin, statusMessage.toString());

          String disableAutoProc = "N";
          if (CmrConstants.RDC_STATUS_COMPLETED.equals(overallStatus)) {
            disableAutoProc = CmrConstants.YES_NO.Y.toString();
          } else {
            disableAutoProc = CmrConstants.YES_NO.N.toString();
          }

          // update admin status
          admin.setDisableAutoProc(disableAutoProc);
          LOG.debug("*** Setting DISABLE_AUTO_PROC >> " + admin.getDisableAutoProc());
          admin.setProcessedFlag(
              CmrConstants.RDC_STATUS_COMPLETED.equals(overallStatus) ? CmrConstants.YES_NO.Y.toString() : CmrConstants.YES_NO.N.toString());
          LOG.debug("*** Setting PROCESSED_FLAG >> " + admin.getProcessedFlag());
          /*
           * jzamora - edited to return the request back to processor if an
           * error occurred, to avoid endless loop
           */
          if (CmrConstants.RDC_STATUS_COMPLETED.equals(overallStatus)) {
            admin.setReqStatus(CmrConstants.REQUEST_STATUS.COM.toString());
          } else {
            LOG.debug("Unlocking request due to error..");
            admin.setReqStatus(CmrConstants.REQUEST_STATUS.PPN.toString());
            admin.setLockBy(null);
            admin.setLockByNm(null);
            admin.setLockTs(null);
            admin.setLockInd(CmrConstants.YES_NO.N.toString());
            admin.setProcessedFlag("E");
          }
          // admin.setReqStatus(CmrConstants.RDC_STATUS_COMPLETED.equals(overallStatus)
          // ? CmrConstants.REQUEST_STATUS.COM.toString()
          // : CmrConstants.REQUEST_STATUS.PCP.toString());
          LOG.debug("*** Setting REQ_STATUS >> " + admin.getReqStatus());

          if (CmrConstants.RDC_STATUS_NOT_COMPLETED.equals(response.getStatus()) || CmrConstants.RDC_STATUS_ABORTED.equals(response.getStatus())) {
            admin.setRdcProcessingStatus(CmrConstants.RDC_STATUS_NOT_COMPLETED);
          } else {
            admin.setRdcProcessingStatus(overallStatus);
          }
          LOG.debug("*** Setting RDC_PROCESSING_STATUS >> " + admin.getRdcProcessingStatus());

          admin.setRdcProcessingMsg(response.getMessage());
          admin.setRdcProcessingTs(SystemUtil.getCurrentTimestamp());
          updateEntity(admin, em);

          // update cmr_no on data if create
          if (CmrConstants.RDC_STATUS_COMPLETED.equals(overallStatus)) {
            // Data data = entity.getEntity(Data.class);
            data.setCmrNo(response.getCmrNo());
            updateEntity(data, em);
          }

          // update addr
          if (CmrConstants.RDC_STATUS_COMPLETED.equals(overallStatus)) {
            String sql = ExternalizedQuery.getSql("BATCH.GET_ADDR_FOR_SAP_NO_SORT_BY_ADDR_TYPE");
            PreparedQuery query3 = new PreparedQuery(em, sql);
            query3.setParameter("REQ_ID", admin.getId().getReqId());
            List<Addr> addresses = query3.getResults(Addr.class);
            int index = 0;

            for (Addr addr : addresses) {
              if (index > addresses.size()) {
                LOG.error("***Cannot continue update of ADDR table for REQ_ID" + admin.getId().getReqId()
                    + ". Number of response records and on ADDR table are inconsistent.");
                break;
              }

              if (response.getRecords() != null && response.getRecords().size() != 0) {
            	  if(index >= response.getRecords().size() ){
            		  LOG.debug("size = " + response.getRecords().size());
            		  LOG.debug("index = " + index);
            		  break;
            	  }

                if (CmrConstants.RDC_SOLD_TO.equals(response.getRecords().get(index).getAddressType())) {
                  String[] addrSeqs = response.getRecords().get(index).getSeqNo().split(",");
                  addr.setPairedAddrSeq(addrSeqs[0]);
                  addr.setSapNo(response.getRecords().get(index).getSapNo());
                  addr.setIerpSitePrtyId(response.getRecords().get(index).getIerpSitePartyId());
                } else {
                  for (RDcRecord red : response.getRecords()) {
                    String[] addrSeqs = { "", "" };

                    if (red.getSeqNo() != null && red.getSeqNo() != "") {
                      addrSeqs = red.getSeqNo().split(",");
                    }

                    if (red.getAddressType().equalsIgnoreCase(addr.getId().getAddrType())
                        && addrSeqs[1].equalsIgnoreCase(addr.getId().getAddrSeq())) {
                      LOG.debug("Address matched");
                      addr.setPairedAddrSeq(addrSeqs[0]);
                      addr.setSapNo(red.getSapNo());
                      addr.setIerpSitePrtyId(red.getIerpSitePartyId());
                    }
                    if (("ZP01").equalsIgnoreCase(red.getAddressType()) && "PG01".equals(addr.getId().getAddrType())
                        && addrSeqs[1].equalsIgnoreCase(addr.getId().getAddrSeq())) {
                      LOG.debug("ZP01 matched");
                      addr.setPairedAddrSeq(addrSeqs[0]);
                      addr.setSapNo(red.getSapNo());
                      addr.setIerpSitePrtyId(red.getIerpSitePartyId());
                      if ((SystemLocation.NEW_ZEALAND.equals(data.getCmrIssuingCntry()) || SystemLocation.AUSTRALIA.equals(data.getCmrIssuingCntry()))
                          && !StringUtils.isEmpty(response.getCmrNo()) && !StringUtils.isEmpty(addr.getId().getAddrSeq())) {
                        String strPaygoNo = getPaygoSapnoForNZ(em, response.getCmrNo(), addr.getId().getAddrSeq(), data.getCmrIssuingCntry());
                        if (!StringUtils.isEmpty(strPaygoNo)) {
                          addr.setSapNo(strPaygoNo);
                          addr.setIerpSitePrtyId("S" + strPaygoNo.substring(1));
                        }
                      }
                    }
                  }
                }

                updateEntity(addr, em);
              }
              index++;
            }
          }

          if (!CmrConstants.RDC_STATUS_IGNORED.equals(response.getStatus())) {
            IERPRequestUtils.createWorkflowHistoryFromBatch(em, BATCH_USER_ID, admin, statusMessage.toString().trim(), actionRdc, null, null,
                "COM".equals(admin.getReqStatus()));
          }

          partialCommit(em);

          // send email notif regardless of abort or complete
          LOG.debug("*** IERP Site IDs on EMAIL >> " + siteIds.toString());
          try {
            sendEmailNotifications(em, admin, siteIds.toString(), statusMessage.toString());
          } catch (Exception e) {
            e.printStackTrace();
            LOG.error("ERROR: " + e.getMessage());
          }
        }
        ProfilerLogger.LOG.trace("After monitorCreqcmr for Request ID: " + admin.getId().getReqId() + " "
            + DurationFormatUtils.formatDuration(new Date().getTime() - start, "m 'm' s 's'"));
      }
      
      // Check and send the notifications to the recipients for soon to be blocked back CMRs..
      RequestUtils.sendEmailNotificationsTREC_CN(em);
      Thread.currentThread().setName("IERPProcess-" + Thread.currentThread().getId());
    }

  }

  private StringBuffer processPartnerFunctionForZS01(Admin admin, Data data, CmrServiceInput cmrServiceInput, String overallStatus,
      StringBuffer statusMessage) {
    ProcessResponse response;
    if (!overallStatus.contains(CmrConstants.RDC_STATUS_NOT_COMPLETED) && !overallStatus.contains(CmrConstants.RDC_STATUS_ABORTED)) {
      // now process the partner functions
      LOG.info("Processing partner functions for Request " + admin.getId().getReqId());
      ProcessRequest request = new ProcessRequest();
      request.setReqId(cmrServiceInput.getInputReqId());
      request.setReqType(ProcessRequest.TYPE_KNVP);
      request.setAddrType("ZS01");
      request.setSapNo(null);
      request.setCmrNo(data.getCmrNo());
      request.setMandt(SystemConfiguration.getValue("MANDT"));
      request.setUserId(cmrServiceInput.getInputUserId());
      String applicationId = BatchUtil.getAppId(data.getCmrIssuingCntry());

      if (LOG.isTraceEnabled()) {
        LOG.trace("Request JSON:");
        DebugUtil.printObjectAsJson(LOG, request);
      }

      try {
        this.serviceClient.setReadTimeout(60 * 10 * 1000); // 10 mins
        response = this.serviceClient.executeAndWrap(applicationId, request, ProcessResponse.class);
      } catch (Exception e) {
        LOG.error("Error when connecting to the service.", e);
        response = new ProcessResponse();
        response.setReqId(admin.getId().getReqId());
        response.setCmrNo(request.getCmrNo());
        response.setMandt(request.getMandt());
        response.setStatus(CmrConstants.RDC_STATUS_ABORTED);
        response.setMessage("Cannot connect to the service at the moment.");
      }
      if (LOG.isTraceEnabled()) {
        LOG.trace("Response JSON:");
        DebugUtil.printObjectAsJson(LOG, response);
      }

      statusMessage = statusMessage.append(
          "\nPartner functions " + (isCompletedSuccessfully(response.getStatus()) ? "processed successfully." : "not successfully processed."));
    }
    return statusMessage;
  }

  private void processError(EntityManager entityManager, Admin admin, String errorMsg) throws CmrException, SQLException {
    if (CmrConstants.REQ_TYPE_DELETE.equals(admin.getReqType()) || CmrConstants.REQ_TYPE_REACTIVATE.equals(admin.getReqType())) {
      admin.setDisableAutoProc("Y");// disable auto processing if error on
                                    // processing
    }
    // processing pending
    LOG.info("Processing error for Request ID " + admin.getId().getReqId() + ": " + errorMsg);
    admin.setReqStatus("PPN");
    admin.setLockBy(null);
    admin.setLockByNm(null);
    admin.setLockInd("N");
    // error
    admin.setProcessedFlag("E");
    admin.setLastUpdtBy(BATCH_USER_ID);
    updateEntity(admin, entityManager);

    WfHist hist = createHistory(entityManager, "An error occurred during processing: " + errorMsg, "PPN", "Processing Error",
        admin.getId().getReqId());
    createComment(entityManager, "An error occurred during processing:\n" + errorMsg, admin.getId().getReqId());

    RequestUtils.sendEmailNotifications(entityManager, admin, hist);
  }

  private void sendEmailNotifications(EntityManager em, Admin admin, String siteIds, String emailCmt) throws Exception {
    // send mail and get the input params for create cmr service
    String sqlMail = "BATCH.GET_MAIL_CONTENTS";
    String mapping = Admin.BATCH_SERVICE_MAPPING;
    PreparedQuery queryMail = new PreparedQuery(em, ExternalizedQuery.getSql(sqlMail));
    queryMail.setParameter("REQ_ID", admin.getId().getReqId());
    queryMail.setParameter("REQ_STATUS", admin.getReqStatus());
    queryMail.setParameter("CHANGED_BY_ID", BATCH_USER_ID);
    List<CompoundEntity> rs = queryMail.getCompundResults(1, Admin.class, mapping);
    WfHist wfHist = null;

    for (CompoundEntity entity : rs) {
      wfHist = entity.getEntity(WfHist.class);
    }

    if (em == null || admin == null || wfHist == null) {
      throw new Exception("Some paramaters to sendEmailNotification are null. Please check the call.");
    }

    try {
      IERPRequestUtils.sendEmailNotifications(em, admin, wfHist, siteIds, emailCmt);
    } catch (Exception e) {
      e.printStackTrace();
      throw new Exception(e.getMessage());
    }

    // partialCommit(em);
  }

  private ProcessResponse sendAddrForProcessing(Addr addr, ProcessRequest request, List<ProcessResponse> responses, boolean isIndexNotUpdated,
      String siteIds, EntityManager em, boolean isSeqNoRequired) {
    ProcessResponse response = null;
    request.setSapNo(addr.getSapNo());
    request.setAddrType(addr.getId().getAddrType());
    if (isSeqNoRequired) {
      request.setSeqNo(addr.getId().getAddrSeq());
    }
    // call the ierp service
    LOG.info("Sending request to IerpProcessService [Request ID: " + request.getReqId() + " CMR No: " + request.getCmrNo() + " Type: "
        + request.getReqType() + " SAP No: " + request.getSapNo() + "]");

    LOG.trace("Request JSON:");
    if (LOG.isTraceEnabled()) {
      DebugUtil.printObjectAsJson(LOG, request);
    }

    try {
      this.serviceClient.setReadTimeout(60 * 10 * 1000); // 10 mins
      response = this.serviceClient.executeAndWrap(ProcessClient.IERP_APP_ID, request, ProcessResponse.class);

      if (response != null && response.getStatus().equals("A") && response.getMessage().contains("was not successfully updated on the index.")) {
        isIndexNotUpdated = true;
        response.setStatus("C");
        response.setMessage("");
      }
      responses.add(response);
    } catch (Exception e) {
      e.printStackTrace();
      LOG.error("Error when connecting to the service.", e);
      response = new ProcessResponse();
      response.setStatus(CmrConstants.RDC_STATUS_ABORTED);
      response.setReqId(request.getReqId());
      response.setCmrNo(request.getCmrNo());
      response.setMandt(request.getMandt());
      response.setMessage("Cannot connect to the service at the moment.");
      responses.add(response);
      isIndexNotUpdated = false;
    }

    LOG.trace("Response JSON:");
    if (LOG.isTraceEnabled()) {
      DebugUtil.printObjectAsJson(LOG, response);
    }

    LOG.info("Response received from IerpProcessService [Request ID: " + response.getReqId() + " CMR No: " + response.getCmrNo() + " KUNNR: "
        + addr.getSapNo() + " Status: " + response.getStatus() + " Message: " + (response.getMessage() != null ? response.getMessage() : "-") + "]");

    if (response != null && response.getRecords() != null && response.getRecords().size() > 0) {
      RDcRecord record = response.getRecords().get(0);
      siteIds = StringUtils.isEmpty(siteIds) ? record.getIerpSitePartyId() : siteIds + ", " + record.getIerpSitePartyId();
      addr.setIerpSitePrtyId(record.getIerpSitePartyId());
      addr.setPairedAddrSeq(record.getSeqNo());
      addr.setSapNo(record.getSapNo());
      updateEntity(addr, em);
      partialCommit(em);
    }
    return response;
  }

  private AddrRdc getAddrRdcRecords(EntityManager entityManager, Addr addr) {
    LOG.debug("Searching for ADDR_RDC records for Request " + addr.getId().getReqId());
    String sql = ExternalizedQuery.getSql("REQUESTENTRY.ADDRRDC.SEARCH_BY_REQID_TYPE_SEQ");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", addr.getId().getReqId());
    query.setParameter("ADDR_TYPE", addr.getId().getAddrType());
    query.setParameter("ADDR_SEQ", addr.getId().getAddrSeq());
    query.setForReadOnly(true);
    return query.getSingleResult(AddrRdc.class);
  }

  private DataRdc getDataRdcRecords(EntityManager entityManager, Data data) {
    LOG.debug("Searching for DATA_RDC records for Request " + data.getId().getReqId());
    String sql = ExternalizedQuery.getSql("SUMMARY.OLDDATA");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", data.getId().getReqId());
    query.setForReadOnly(true);
    return query.getSingleResult(DataRdc.class);
  }

  public HashMap<String, Object> processUpdateRequest(Admin admin, Data data, CmrServiceInput cmrServiceInput, EntityManager em) throws Exception {

    HashMap<String, Object> overallResponse = new HashMap<String, Object>();
    List<ProcessResponse> responses = new ArrayList<ProcessResponse>();
    List<String> respStatuses = new ArrayList<String>();
    ProcessResponse response = null;
    String siteIds = "";
    ProcessRequest request = new ProcessRequest();
    request.setCmrNo(cmrServiceInput.getInputCmrNo());
    request.setMandt(cmrServiceInput.getInputMandt());
    request.setReqId(cmrServiceInput.getInputReqId());
    request.setReqType(cmrServiceInput.getInputReqType());
    request.setUserId(cmrServiceInput.getInputUserId());
    boolean isIndexNotUpdated = false;
    boolean isSeqNoRequired = false;
    boolean handleTempReact = false;
    boolean processTempReact = false;
    boolean addrUpdateFlag = false;
    if (!CmrConstants.DE_CND_ISSUING_COUNTRY_VAL.contains(data.getCmrIssuingCntry()) && !CNHandler.isCNIssuingCountry(data.getCmrIssuingCntry())) {
      isSeqNoRequired = true;
      handleTempReact = true;
    }
    try {
      // 1. Get first the ones that are new -- BATCH.GET_NEW_ADDR_FOR_UPDATE
      String sql = ExternalizedQuery.getSql("BATCH.GET_NEW_ADDR_FOR_UPDATE");
      PreparedQuery query = new PreparedQuery(em, sql);
      query.setParameter("REQ_ID", admin.getId().getReqId());
      List<Addr> addresses = query.getResults(Addr.class);

      if (addresses != null && addresses.size() > 0) {
        for (Addr addr : addresses) {
          response = sendAddrForProcessing(addr, request, responses, isIndexNotUpdated, siteIds, em, isSeqNoRequired);
          respStatuses.add(response.getStatus());
          keepAlive();
        }
      }

      // 2. Get the ones that are updated -- BATCH.GET_UPDATED_ADDR_FOR_UPDATE
      sql = ExternalizedQuery.getSql("BATCH.GET_UPDATED_ADDR_FOR_UPDATE");
      query = new PreparedQuery(em, sql);
      query.setParameter("REQ_ID", admin.getId().getReqId());
      addresses = query.getResults(Addr.class);
      List<Addr> notProcessed = new ArrayList<Addr>();

      if (addresses != null && addresses.size() > 0) {
        for (Addr addr : addresses) {
          AddrRdc addrRdc = getAddrRdcRecords(em, addr);
          boolean isAddrUpdated = false;

          if (CNHandler.isCNIssuingCountry(data.getCmrIssuingCntry())) {
            CNHandler cnHandler = new CNHandler();
            isAddrUpdated = cnHandler.isAddrUpdated(addr, addrRdc, data.getCmrIssuingCntry(), data, em);
          } else {
            isAddrUpdated = RequestUtils.isUpdated(addr, addrRdc, data.getCmrIssuingCntry());
          }

          if (isAddrUpdated) {
            addrUpdateFlag = true;
            response = sendAddrForProcessing(addr, request, responses, isIndexNotUpdated, siteIds, em, isSeqNoRequired);
            respStatuses.add(response.getStatus());
          } else {
            notProcessed.add(addr);
          }
          keepAlive();
        }
      }

      // 3. Check if there are customer and IBM changes, propagate to other
      // addresses
      DataRdc dataRdc = getDataRdcRecords(em, data);
      boolean isDataUpdated = false;
      boolean isAdminUpdated = false;

      if (CNHandler.isCNIssuingCountry(data.getCmrIssuingCntry())) {
        isDataUpdated = CNHandler.isDataUpdated(data, dataRdc, data.getCmrIssuingCntry());
      } else if (CmrConstants.DE_CND_ISSUING_COUNTRY_VAL.contains(data.getCmrIssuingCntry())) {
        isDataUpdated = DEHandler.isDataUpdated(data, dataRdc, data.getCmrIssuingCntry());
      } else {
        GEOHandler cntryHandler = RequestUtils.getGEOHandler(data.getCmrIssuingCntry());
        isDataUpdated = cntryHandler.isDataUpdate(data, dataRdc, data.getCmrIssuingCntry());
        isAdminUpdated = cntryHandler.isAdminUpdate(admin, data.getCmrIssuingCntry());
        isDataUpdated = isAdminUpdated || isDataUpdated;
      }

      if (handleTempReact && CMR_REQUEST_STATUS_CPR.equals(admin.getReqStatus())) {
        processTempReact = true;
      }

      if ((isDataUpdated && (notProcessed != null && notProcessed.size() > 0)) || processTempReact) {
        LOG.debug("Processing CMR Data changes to " + notProcessed.size() + " addresses of CMR# " + data.getCmrNo());
        for (Addr addr : notProcessed) {
          response = sendAddrForProcessing(addr, request, responses, isIndexNotUpdated, siteIds, em, isSeqNoRequired);
          respStatuses.add(response.getStatus());
        }
        keepAlive();
      }

      if (!isDataUpdated && !addrUpdateFlag && notProcessed != null && notProcessed.size() > 0
          && SystemLocation.GERMANY.equals(data.getCmrIssuingCntry())) {
        for (Addr addr : notProcessed) {
          response = sendAddrForProcessing(addr, request, responses, isIndexNotUpdated, siteIds, em, isSeqNoRequired);
          respStatuses.add(response.getStatus());
          keepAlive();
        }
      }

      if (respStatuses.size() > 0) {
        if (respStatuses.contains(CmrConstants.RDC_STATUS_ABORTED)) {
          if (isIndexNotUpdated) {
            overallResponse.put("overallStatus", CmrConstants.RDC_STATUS_COMPLETED);
          } else {
            overallResponse.put("overallStatus", CmrConstants.RDC_STATUS_ABORTED);
          }
        } else if (respStatuses.contains(CmrConstants.RDC_STATUS_NOT_COMPLETED)) {
          overallResponse.put("overallStatus", CmrConstants.RDC_STATUS_NOT_COMPLETED);
        } else if (respStatuses.contains(CmrConstants.RDC_STATUS_COMPLETED)) {
          overallResponse.put("overallStatus", CmrConstants.RDC_STATUS_COMPLETED);
        }
      } else {
        LOG.error("Response statuses is empty for request ID: " + admin.getId().getReqId());
        ProcessResponse customResponse = new ProcessResponse();
        responses = new ArrayList<ProcessResponse>();
        customResponse.setMessage("No data was updated on RDc for this request. Please contact Ops for assistance.");
        responses.add(customResponse);
        overallResponse.put("overallStatus", CmrConstants.RDC_STATUS_ABORTED);
      }

      overallResponse.put("siteIds", siteIds);
      overallResponse.put("responses", responses);

    } catch (Exception e) {
      LOG.error("Error in processing Update Request " + admin.getId().getReqId(), e);
      addError("Update Request " + admin.getId().getReqId() + " Error: " + e.getMessage());
    }

    return overallResponse;
  }

  public ProcessResponse processCreateRequest(Admin admin, CmrServiceInput cmrServiceInput, EntityManager em) throws Exception {
    String resultCode = "";

    ProcessRequest request = new ProcessRequest();
    request.setCmrNo(cmrServiceInput.getInputCmrNo());
    request.setMandt(cmrServiceInput.getInputMandt());
    request.setReqId(cmrServiceInput.getInputReqId());
    request.setReqType(cmrServiceInput.getInputReqType());
    request.setUserId(cmrServiceInput.getInputUserId());

    convertToProspectToLegalCMRInput(request, em, request.getReqId());

    LOG.info("Sending request to Process Service [Request ID: " + request.getReqId() + " CMR No: " + request.getCmrNo() + " Type: "
        + request.getReqType() + "]");

    // call the create cmr service
    LOG.trace("Request JSON:");
    if (LOG.isTraceEnabled()) {
      DebugUtil.printObjectAsJson(LOG, request);
    }

    ProcessResponse response = null;
    try {
      this.serviceClient.setReadTimeout(60 * 10 * 1000); // 10 mins
      response = this.serviceClient.executeAndWrap(ProcessClient.IERP_APP_ID, request, ProcessResponse.class);
    } catch (Exception e) {
      e.printStackTrace();
      LOG.error("Error when connecting to the service.", e);
      response = new ProcessResponse();
      response.setStatus(CmrConstants.RDC_STATUS_ABORTED);
      response.setReqId(request.getReqId());
      response.setCmrNo(request.getCmrNo());
      response.setMandt(request.getMandt());
      response.setMessage("Cannot connect to the service at the moment.");
    }

    resultCode = response.getStatus();
    LOG.trace("Response JSON:");
    if (LOG.isTraceEnabled()) {
      DebugUtil.printObjectAsJson(LOG, response);
    }

    LOG.info("Response received from Process Service [Request ID: " + response.getReqId() + " CMR No: " + response.getCmrNo() + " Status: "
        + response.getStatus() + " Message: " + (response.getMessage() != null ? response.getMessage() : "-") + "]");

    return response;
  }

  public CmrServiceInput getReqParam(EntityManager em, long reqId, String reqType, Data data, Admin admin, ArrayList<String> cmrNoList) {
    String cmrNo = "";

    String requestType = ((reqType) != null && (reqType).trim().length() > 0) ? (reqType) : "";
    if (!StringUtils.isEmpty(reqType)) {
      // if (CmrConstants.REQ_TYPE_CREATE.equals(reqType)) {
      // cmrNo = "TEMP";
      // } else if (CmrConstants.REQ_TYPE_UPDATE.equals(reqType)) {
      // cmrNo = data.getCmrNo();
      // }if (!StringUtils.isBlank(prospectCMR)) {
      cmrNo = data.getCmrNo();
      synchronized (IERPProcessService.class) {
        if (SystemLocation.CHINA.equals(data.getCmrIssuingCntry()) && "C".equals(requestType)
            && (StringUtils.isEmpty(cmrNo) || cmrNo.startsWith("P"))) {
          if (cmrNo != null && cmrNo.startsWith("P"))
            cmrNo = "";
          cmrNo = generateCMRNoForIERP(data);
          // to avoid dup cmr, use this list to check
          if (!StringUtils.isEmpty(cmrNo) && cmrNoList.contains(cmrNo)) {
            cmrNo = generateCMRNoForIERP(data);
            if (!StringUtils.isEmpty(cmrNo))
              cmrNoList.add(cmrNo);
          } else if (!StringUtils.isEmpty(cmrNo)) {
            cmrNoList.add(cmrNo);
          }
        }
      }
    }

    long requestId = reqId;
    CmrServiceInput cmrServiceInput = new CmrServiceInput();

    cmrServiceInput.setInputMandt(SystemConfiguration.getValue("MANDT"));
    cmrServiceInput.setInputReqId(requestId);
    cmrServiceInput.setInputReqType(requestType);
    cmrServiceInput.setInputCmrNo(cmrNo);
    cmrServiceInput.setInputUserId(SystemConfiguration.getValue("BATCH_USERID"));

    return cmrServiceInput;
  }

  /**
   * Calls the Generate CMR no service to get the next available CMR no
   * 
   * @param handler
   * @param targetSystem
   * @return
   */
  protected String generateCMRNoForIERP(Data data) {

    try {
      GenerateCMRNoRequest request = new GenerateCMRNoRequest();
      request.setLoc1(data.getCustClass());
      request.setLoc2(data.getCmrIssuingCntry());
      request.setMandt(SystemConfiguration.getValue("MANDT"));
      request.setSystem("IERP");

      GenerateCMRNoClient client = CmrServicesFactory.getInstance().createClient(BaseBatchService.BATCH_SERVICE_URL, GenerateCMRNoClient.class);

      GenerateCMRNoResponse response = client.executeAndWrap(request, GenerateCMRNoResponse.class);

      if (response.isSuccess()) {
        return response.getCmrNo();
      } else {
        LOG.error("CMR No cannot be generated. Error: " + response.getMsg());
        return null;
      }
    } catch (Exception e) {
      LOG.error("Error in generating CMR no", e);
      return null;
    }

  }

  private void lockAdminRecordsForProcessing(List<CompoundEntity> forProcessing, EntityManager entityManager) {
    for (CompoundEntity entity : forProcessing) {
      Admin admin = entity.getEntity(Admin.class);
      admin.setRdcProcessingStatus(CmrConstants.RDC_STATUS_WAITING);
      admin.setRdcProcessingMsg("Waiting for completion.");
      admin.setRdcProcessingTs(SystemUtil.getCurrentTimestamp());
      updateEntity(admin, entityManager);
      LOG.debug("Locking Request ID " + admin.getId().getReqId() + " for processing.");
      // SEND EMAIL
    }
    partialCommit(entityManager);
  }

  public void createCommentLog(EntityManager em, Admin admin, String message) throws CmrException, SQLException {
    LOG.info("Creating Comment Log for Req ID " + admin.getId().getReqId());
    ReqCmtLog reqCmtLog = new ReqCmtLog();
    ReqCmtLogPK reqCmtLogpk = new ReqCmtLogPK();
    reqCmtLogpk.setCmtId(SystemUtil.getNextID(em, SystemConfiguration.getValue("MANDT"), "CMT_ID"));
    reqCmtLog.setId(reqCmtLogpk);
    reqCmtLog.setReqId(admin.getId().getReqId());
    reqCmtLog.setCmt(message != null ? message : "No message provided.");
    if (reqCmtLog.getCmt().length() > 2000) {
      String cmt = "(some comments trimmed)";
      reqCmtLog.setCmt(reqCmtLog.getCmt().substring(0, 1970) + cmt);
    }
    // save cmtlockedIn as Y default for current realese
    reqCmtLog.setCmtLockedIn(CmrConstants.CMT_LOCK_IND_YES);
    reqCmtLog.setCreateById(COMMENT_LOGGER);
    reqCmtLog.setCreateByNm(COMMENT_LOGGER);
    // set createTs as current timestamp and updateTs same as CreateTs
    reqCmtLog.setCreateTs(SystemUtil.getCurrentTimestamp());
    reqCmtLog.setUpdateTs(reqCmtLog.getCreateTs());
    createEntity(reqCmtLog, em);
    partialCommit(em);
  }

  protected void initClient() throws Exception {
    if (this.serviceClient == null) {
      this.serviceClient = CmrServicesFactory.getInstance().createClient(BATCH_SERVICES_URL, ProcessClient.class);
    }
  }

  private String convertToIssuingCntryCSV(List<SuppCntry> list) {
    String ret = "";

    if (list != null && list.size() > 0) {
      for (int i = 0; i < list.size(); i++) {
        SuppCntry suppCntry = list.get(i);
        if (StringUtils.isEmpty(ret)) {
          ret = ret + "'" + suppCntry.getId().getCntryCd() + "'";
        } else {
          ret = ret + "," + "'" + suppCntry.getId().getCntryCd() + "'";
        }
      }
    }

    return ret;
  }

  /**
   * Converts the input to Legal CMR Input
   * 
   * @param request
   * @param entityManager
   * @param reqId
   * @throws Exception
   */
  private void convertToProspectToLegalCMRInput(ProcessRequest request, EntityManager entityManager, long reqId) throws Exception {
    String sql = ExternalizedQuery.getSql("BATCH.GET_PROSPECT");
    PreparedQuery q = new PreparedQuery(entityManager, sql);
    q.setParameter("REQ_ID", reqId);
    List<Object[]> results = q.getResults();
    if (results != null && results.size() > 0) {
      Object[] result = results.get(0);
      if (CmrConstants.YES_NO.Y.toString().equals(result[0])) {
        String prospectCMR = (String) result[1];
        if (StringUtils.isEmpty(prospectCMR)) {
          throw new Exception("Cannot process Propsect to Legal CMR conversion for Request ID " + reqId + ". Propsect CMR No. is missing.");
        }
        request.setProspectCMRNo(prospectCMR);
        request.setSeqNo(!StringUtils.isEmpty((String) result[2]) ? (String) result[2] : "A");
      }
    }
  }

  @Override
  protected boolean useServicesConnections() {
    return true;
  }

  public HashMap<String, Object> processUpdateRequestLegacyStyle(Admin admin, Data data, CmrServiceInput cmrServiceInput, EntityManager em)
      throws Exception {

    HashMap<String, Object> overallResponse = new HashMap<String, Object>();
    List<ProcessResponse> responses = new ArrayList<ProcessResponse>();
    List<String> respStatuses = new ArrayList<String>();
    ProcessResponse response = null;
    String siteIds = "";
    ProcessRequest request = new ProcessRequest();
    request.setCmrNo(cmrServiceInput.getInputCmrNo());
    request.setMandt(cmrServiceInput.getInputMandt());
    request.setReqId(cmrServiceInput.getInputReqId());
    request.setReqType(cmrServiceInput.getInputReqType());
    request.setUserId(cmrServiceInput.getInputUserId());
    boolean isIndexNotUpdated = false;
    boolean isSeqNoRequired = false;
    boolean handleTempReact = false;
    boolean processTempReact = false;
    GEOHandler cntryHandler = RequestUtils.getGEOHandler(data.getCmrIssuingCntry());
    if (!CmrConstants.DE_CND_ISSUING_COUNTRY_VAL.contains(data.getCmrIssuingCntry()) && !CNHandler.isCNIssuingCountry(data.getCmrIssuingCntry())) {
      isSeqNoRequired = true;
      handleTempReact = true;
    }
    try {
      String sql = ExternalizedQuery.getSql("BATCH.GET_UPDATED_ADDR_FOR_UPDATE_ORDERED");
      // if there is a handler, order the addresses correctly
      if (cntryHandler != null && cntryHandler.getAddressOrder() != null) {
        String[] order = cntryHandler.getAddressOrder();
        StringBuilder types = new StringBuilder();
        if (order != null && order.length > 0) {
          for (String type : order) {
            LOG.trace("Looking for Address Types " + type);
            types.append(types.length() > 0 ? ", " : "");
            types.append("'" + type + "'");
          }
        }

        if (types.length() > 0) {
          sql += " and ADDR_TYPE in ( " + types.toString() + ") ";
        }
        StringBuilder orderBy = new StringBuilder();
        int orderIndex = 0;
        for (String type : order) {
          orderBy.append(" when ADDR_TYPE = '").append(type).append("' then ").append(orderIndex);
          orderIndex++;
        }
        orderBy.append(" else 25 end, ADDR_TYPE, case when IMPORT_IND = 'Y' then 0 else 1 end, ADDR_SEQ ");
        sql += " order by case " + orderBy.toString();
      }

      PreparedQuery query = new PreparedQuery(em, sql);
      query.setParameter("REQ_ID", admin.getId().getReqId());
      List<Addr> addresses = query.getResults(Addr.class);
      // List<Addr> notProcessed = new ArrayList<Addr>();
      Queue<Addr> notProcessed = new LinkedList<>();

      DataRdc dataRdc = getDataRdcRecords(em, data);
      boolean isDataUpdated = false;
      boolean isAdminUpdated = false;
      if (!CmrConstants.ANZ_COUNTRIES.contains(data.getCmrIssuingCntry())) {
    	  isDataUpdated = cntryHandler.isDataUpdate(data, dataRdc, data.getCmrIssuingCntry());
      		}else{
      			isDataUpdated = true;
      		}
      isAdminUpdated = cntryHandler.isAdminUpdate(admin, data.getCmrIssuingCntry());
      isDataUpdated = isAdminUpdated || isDataUpdated;

      if (CmrConstants.LA_COUNTRIES.contains(data.getCmrIssuingCntry()) && !isDataUpdated) {
        isDataUpdated = LAHandler.isTaxInfoUpdated(em, admin.getId().getReqId());
      }
      
      // 3. Check if there are customer and IBM changes, propagate to other
      // addresses

      if (handleTempReact && CMR_REQUEST_STATUS_CPR.equals(admin.getReqStatus())) {
        processTempReact = true;
      }

      if ((isDataUpdated && (addresses != null && addresses.size() > 0)) || processTempReact) {
        LOG.debug("Processing CMR Data changes to " + addresses.size() + " addresses of CMR# " + data.getCmrNo());
        for (Addr addr : addresses) {
          response = sendAddrForProcessing(addr, request, responses, isIndexNotUpdated, siteIds, em, isSeqNoRequired);
          respStatuses.add(response.getStatus());
        }
      }

      if (!isDataUpdated && addresses != null && addresses.size() > 0) {
        for (Addr addr : addresses) {
          if ("Y".equals(addr.getImportInd())) {
            AddrRdc addrRdc = getAddrRdcRecords(em, addr);
            boolean isAddrUpdated = false;
            isAddrUpdated = RequestUtils.isUpdated(addr, addrRdc, data.getCmrIssuingCntry());
            if (isAddrUpdated) {
              response = sendAddrForProcessing(addr, request, responses, isIndexNotUpdated, siteIds, em, isSeqNoRequired);
              respStatuses.add(response.getStatus());
            } else {
              notProcessed.add(addr);
            }
          } else {
            response = sendAddrForProcessing(addr, request, responses, isIndexNotUpdated, siteIds, em, isSeqNoRequired);
            respStatuses.add(response.getStatus());
          }
        }
      }

      if (respStatuses.size() > 0) {
        if (respStatuses.contains(CmrConstants.RDC_STATUS_ABORTED)) {
          if (isIndexNotUpdated) {
            overallResponse.put("overallStatus", CmrConstants.RDC_STATUS_COMPLETED);
          } else {
            overallResponse.put("overallStatus", CmrConstants.RDC_STATUS_ABORTED);
          }
        } else if (respStatuses.contains(CmrConstants.RDC_STATUS_NOT_COMPLETED)) {
          overallResponse.put("overallStatus", CmrConstants.RDC_STATUS_NOT_COMPLETED);
        } else if (respStatuses.contains(CmrConstants.RDC_STATUS_COMPLETED)) {
          overallResponse.put("overallStatus", CmrConstants.RDC_STATUS_COMPLETED);
        }
      } else {
        LOG.error("Response statuses is empty for request ID: " + admin.getId().getReqId());
        ProcessResponse customResponse = new ProcessResponse();
        responses = new ArrayList<ProcessResponse>();
        customResponse.setMessage("No data was updated on RDc for this request. Please contact Ops for assistance.");
        responses.add(customResponse);
        overallResponse.put("overallStatus", CmrConstants.RDC_STATUS_ABORTED);
      }

      overallResponse.put("siteIds", siteIds);
      overallResponse.put("responses", responses);

    } catch (Exception e) {
      LOG.error("Error in processing Update Request " + admin.getId().getReqId(), e);
      addError("Update Request " + admin.getId().getReqId() + " Error: " + e.getMessage());
    }

    return overallResponse;
  }

  public List<Long> gatherDRPending(EntityManager entityManager) {
    long start = new Date().getTime();
    String sqlPendingDrReq = ExternalizedQuery.getSql("BATCH.MULTI.GET_DR_PENDING_REQIDS");
    PreparedQuery query = new PreparedQuery(entityManager, sqlPendingDrReq);
    List<Long> reqIds = query.getResults(Long.class);
    ProfilerLogger.LOG.trace("After gatherDRPending " + DurationFormatUtils.formatDuration(new Date().getTime() - start, "m 'm' s 's'"));
    return reqIds;
  }

  private List<CompoundEntity> getPendingRequest(EntityManager em) {
    long start = new Date().getTime();
    // DTN: 1) Get the list of DR type countries
    String sql1 = ExternalizedQuery.getSql("BATCH.GET_DR_COUNTRIES");
    PreparedQuery query = new PreparedQuery(em, sql1);
    List<SuppCntry> drList = query.getResults(SuppCntry.class);
    LOG.debug(
        "Executing batch code for R" + SystemConfiguration.getSystemProperty("RELEASE") + ".b" + SystemConfiguration.getSystemProperty("BUILD"));
    LOG.debug("Size of DR list : " + drList.size());

    // DTN: 2) Get the requests
    String isngCntryCds = convertToIssuingCntryCSV(drList);
    String sql2 = ExternalizedQuery.getSql("BATCH.GET_DR_PROC_PENDING");
    sql2 = sql2.replace("<CMR_ISSUING_CNTRY>", isngCntryCds);
    query = new PreparedQuery(em, sql2);
    List<CompoundEntity> pendingReqList = query.getCompundResults(Admin.class, Admin.BATCH_MASS_CREATE_GET_RECORD);
    LOG.debug("Size of REQ_ID list : " + pendingReqList.size());
    ProfilerLogger.LOG.trace("After getPendingRequest " + DurationFormatUtils.formatDuration(new Date().getTime() - start, "m 'm' s 's'"));
    return pendingReqList;
  }

  private List<CompoundEntity> getPendingRequestByReqIds(EntityManager em, List<Long> pendingReqIds) {
    long start = new Date().getTime();
    List<CompoundEntity> pendingReqList = null;

    if (pendingReqIds != null && pendingReqIds.size() > 0) {
      String sqlPendingDRReq = ExternalizedQuery.getSql("BATCH.MULTI.GET_DR_PROC_PENDING");
      sqlPendingDRReq = sqlPendingDRReq.replace("<PENDING_REQIDS>", StringUtils.join(pendingReqIds, ','));

      PreparedQuery query = new PreparedQuery(em, sqlPendingDRReq);
      pendingReqList = query.getCompundResults(Admin.class, Admin.BATCH_MASS_CREATE_GET_RECORD);
    }
    ProfilerLogger.LOG.trace("After getPendingRequestByReqIds " + DurationFormatUtils.formatDuration(new Date().getTime() - start, "m 'm' s 's'"));
    return pendingReqList;
  }

  public boolean isMultiMode() {
    return multiMode;
  }

  public void setMultiMode(boolean multiMode) {
    this.multiMode = multiMode;
  }

  private String getPaygoSapnoForNZ(EntityManager entityManager, String cmrNo, String seqNo, String cmrIssuingCntry) {
    LOG.debug("getPaygoSapnoForNZ ");
    String PaygoSapno = "";
    PreparedQuery query = new PreparedQuery(entityManager, ExternalizedQuery.getSql("GET.NZ.PAYGOSAPNO"));
    query.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
    query.setParameter("KATR6", cmrIssuingCntry);
    query.setParameter("ZZKV_CUSNO", cmrNo);
    query.setParameter("ZZKV_SEQNO", seqNo);
    query.setForReadOnly(true);
    PaygoSapno = query.getSingleResult(String.class);
    return PaygoSapno;
  }

  // public List<Long> getPendingReqIds() {
  // return pendingReqIds;
  // }
  //
  // public void setPendingReqIds(List<Long> pendingReqIds) {
  // this.pendingReqIds = pendingReqIds;
  // }

}
