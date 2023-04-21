package com.ibm.cmr.create.batch.service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.CompoundEntity;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.entity.DataPK;
import com.ibm.cio.cmr.request.entity.MassCreate;
import com.ibm.cio.cmr.request.entity.MassCreateData;
import com.ibm.cio.cmr.request.entity.MassUpdt;
import com.ibm.cio.cmr.request.entity.ReqCmtLog;
import com.ibm.cio.cmr.request.entity.SuppCntry;
import com.ibm.cio.cmr.request.entity.WfHist;
import com.ibm.cio.cmr.request.model.requestentry.MassCreateBatchEmailModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.util.RequestUtils;
import com.ibm.cio.cmr.request.util.SystemUtil;
import com.ibm.cmr.create.batch.util.BatchUtil;
import com.ibm.cmr.create.batch.util.CMRRequestContainer;
import com.ibm.cmr.create.batch.util.DebugUtil;
import com.ibm.cmr.create.batch.util.mq.LandedCountryMap;
import com.ibm.cmr.services.client.process.ProcessRequest;
import com.ibm.cmr.services.client.process.ProcessResponse;
import com.ibm.cmr.services.client.process.RDcRecord;

public class IERPMassProcessService extends TransConnService {

  private boolean devMode;
  private static final Logger LOG = Logger.getLogger(IERPMassProcessService.class);
  private CMRRequestContainer cmrObjects;
  private static final String MASS_UPDATE_FAIL = "FAIL";
  private static final String MASS_UPDATE_DONE = "DONE";
  public static final String CMR_REQUEST_STATUS_CPR = "CPR";
  public static final String CMR_REQUEST_STATUS_PCP = "PCP";
  private static final String[] ADDRESS_ORDER = { "ZS01", "ZP01", "ZI01", "ZD01", "ZD02", "ZP02" };

  public boolean isDevMode() {
    return devMode;
  }

  public void setDevMode(boolean devMode) {
    this.devMode = devMode;
  }

  public String[] getIerpAddressOrder() {
    return ADDRESS_ORDER;
  }

  @Override
  protected Boolean executeBatch(EntityManager entityManager) throws Exception {

    if (this.devMode) {
      LOG.info("RUNNING IN DEVELOPMENT MODE");
    }
    LOG.info("Initializing Country Map..");
    LandedCountryMap.init(entityManager);

    entityManager.clear();
    initClient();
    List<Admin> pending = getPendingRecordsRDC(entityManager);

    LOG.debug((pending != null ? pending.size() : 0) + " records to process to RDc.");

    Data data = null;
    ProcessRequest request = null;
    for (Admin admin : pending) {
      try {

        this.cmrObjects = prepareRequest(entityManager, admin);
        data = this.cmrObjects.getData();

        request = new ProcessRequest();
        request.setCmrNo(data.getCmrNo());
        request.setMandt(SystemConfiguration.getValue("MANDT"));
        request.setReqId(admin.getId().getReqId());
        request.setReqType(admin.getReqType());
        request.setUserId(BATCH_USER_ID);

        switch (admin.getReqType()) {
        case CmrConstants.REQ_TYPE_MASS_UPDATE:
          processMassUpdateRequest(entityManager, request, admin, data);
          break;
        case CmrConstants.REQ_TYPE_MASS_CREATE:
          processMassCreateRequest(entityManager, request, admin, data);
          break;
        }

        if (CmrConstants.RDC_STATUS_ABORTED.equalsIgnoreCase(admin.getRdcProcessingStatus())
            || CmrConstants.RDC_STATUS_NOT_COMPLETED.equalsIgnoreCase(admin.getRdcProcessingStatus())) {
          admin.setReqStatus("PPN");
          admin.setProcessedFlag("E"); // set request status to error.
          createHistory(entityManager, "Sending back to processor due to error on RDC processing", "PPN", "RDC Processing", admin.getId().getReqId());
        } else if ((CmrConstants.RDC_STATUS_COMPLETED.equalsIgnoreCase(admin.getRdcProcessingStatus())
            || CmrConstants.RDC_STATUS_COMPLETED_WITH_WARNINGS.equalsIgnoreCase(admin.getRdcProcessingStatus()))
            && CmrConstants.REQ_TYPE_CREATE.equals(admin.getReqType())) {
          admin.setReqStatus("COM");
          admin.setProcessedFlag("Y"); // set request status to processed
          createHistory(entityManager, "Request processing Completed Successfully", "COM", "RDC Processing", admin.getId().getReqId());
        }
        partialCommit(entityManager);
      } catch (Exception e) {
        partialRollback(entityManager);
        LOG.error("Unexpected error occurred during processing of Request " + admin.getId().getReqId(), e);
        processError(entityManager, admin, e.getMessage());
      }
    }

    List<Admin> pendingLA = getPendingRecordsRDCLA(entityManager);

    LOG.debug((pendingLA != null ? pendingLA.size() : 0) + " LA records to process to RDc.");

    Data dataLA = null;
    ProcessRequest requestLA = null;
    for (Admin admin : pendingLA) {

      try {
        this.cmrObjects = prepareRequest(entityManager, admin);
        dataLA = this.cmrObjects.getData();

        requestLA = new ProcessRequest();
        requestLA.setCmrNo(dataLA.getCmrNo());
        requestLA.setMandt(SystemConfiguration.getValue("MANDT"));
        requestLA.setReqId(admin.getId().getReqId());
        requestLA.setReqType(admin.getReqType());
        requestLA.setUserId(BATCH_USER_ID);

        switch (admin.getReqType()) {
        case CmrConstants.REQ_TYPE_MASS_UPDATE:
          processMassUpdateRequest(entityManager, requestLA, admin, dataLA);
          break;
        }

        if (CmrConstants.RDC_STATUS_ABORTED.equalsIgnoreCase(admin.getRdcProcessingStatus())
            || CmrConstants.RDC_STATUS_NOT_COMPLETED.equalsIgnoreCase(admin.getRdcProcessingStatus())) {
          admin.setReqStatus("PPN");
          admin.setProcessedFlag("E"); // set requestLA status to error.
          createHistory(entityManager, "Sending back to processor due to error on RDC processing", "PPN", "RDC Processing", admin.getId().getReqId());
        } else if ((CmrConstants.RDC_STATUS_COMPLETED.equalsIgnoreCase(admin.getRdcProcessingStatus())
            || CmrConstants.RDC_STATUS_COMPLETED_WITH_WARNINGS.equalsIgnoreCase(admin.getRdcProcessingStatus()))
            && CmrConstants.REQ_TYPE_CREATE.equals(admin.getReqType())) {
          admin.setReqStatus("COM");
          admin.setProcessedFlag("Y"); // set requestLA status to processed
          createHistory(entityManager, "Request processing Completed Successfully", "COM", "RDC Processing", admin.getId().getReqId());
        }
        partialCommit(entityManager);
      } catch (Exception e) {
        partialRollback(entityManager);
        LOG.error("Unexpected error occurred during processing of Request " + admin.getId().getReqId(), e);
        processError(entityManager, admin, e.getMessage());
      }
    }

    return true;
  }

  /**
   * Gets the Admin records with status = 'PCO' and country has processing type
   * = 'DR'
   * 
   * @param entityManager
   * @return
   */
  private List<Admin> getPendingRecordsRDC(EntityManager entityManager) {
    String sql = ExternalizedQuery.getSql("DR.GET_MASS_PROCESS_PENDING.RDC");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    return query.getResults(Admin.class);
  }

  /**
   * Processes errors that happened during execution. Updates the status of the
   * {@link Admin} record and creates relevant {@link WfHist} and
   * {@link ReqCmtLog} records
   * 
   * @param entityManager
   * @param admin
   * @param errorMsg
   * @throws CmrException
   * @throws SQLException
   */
  private void processError(EntityManager entityManager, Admin admin, String errorMsg) throws CmrException, SQLException {
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

  /**
   * Retrieves the {@link Admin}, {@link Data}, and {@link Addr} records based
   * on the request
   * 
   * @param cmmaMgr
   * @param reqId
   * @return
   * @throws Exception
   */
  private CMRRequestContainer prepareRequest(EntityManager entityManager, Admin admin) throws Exception {
    LOG.debug("Preparing Request Objects... ");
    CMRRequestContainer container = new CMRRequestContainer();

    DataPK dataPk = new DataPK();
    dataPk.setReqId(admin.getId().getReqId());
    Data data = entityManager.find(Data.class, dataPk);
    if (data == null) {
      throw new Exception("Cannot locate DATA record");
    }

    String sql = ExternalizedQuery.getSql("DR.GET.ADDR");
    // get the address order
    if (getIerpAddressOrder() != null) {
      String[] order = getIerpAddressOrder();
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
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    // query.setForReadOnly(true);
    query.setParameter("REQ_ID", admin.getId().getReqId());
    List<Addr> addresses = query.getResults(Addr.class);

    container.setAdmin(admin);
    container.setData(data);
    if (addresses != null) {
      for (Addr addr : addresses) {
        container.addAddress(addr);
      }
    }
    return container;
  }

  /**
   * Processes Request Type 'M'
   * 
   * @param entityManager
   * @param request
   * @param admin
   * @param data
   * @throws Exception
   */
  protected void processMassUpdateRequest(EntityManager entityManager, ProcessRequest request, Admin admin, Data data) throws Exception {

    if (admin == null) {
      throw new Exception("Cannot process mass update request. Admin information is null or empty.");
    }

    PreparedQuery query = new PreparedQuery(entityManager, ExternalizedQuery.getSql("QUERY.DATA.GET.CMR.BY_REQID"));
    query.setParameter("REQ_ID", admin.getId().getReqId());
    List<Object[]> cntryList = query.getResults();
    String cntry = "";

    if (cntryList != null && cntryList.size() > 0) {
      Object[] result = cntryList.get(0);
      cntry = (String) result[0];
    } else {
      throw new Exception("Cannot process mass update request. Data information is null or empty.");
    }

    query = new PreparedQuery(entityManager, ExternalizedQuery.getSql("SYSTEM.SUPP_CNTRY_BY_CNTRY_CD"));
    query.setParameter("CNTRY_CD", cntry);
    SuppCntry suppCntry = query.getSingleResult(SuppCntry.class);

    if (suppCntry == null) {
      throw new Exception("Cannot process mass update request. Data information is null or empty.");
    } else {
      String mode = suppCntry.getSuppReqType();

      if (mode.contains("M0")) {
        throw new Exception("Cannot process mass update request. Mass update processing is currently set to manual.");
      }
    }

    String resultCode = null;
    String processingStatus = admin.getRdcProcessingStatus() != null ? admin.getRdcProcessingStatus() : "";
    long reqId = admin.getId().getReqId();
    boolean isIndexNotUpdated = false;

    try {
      // 1. Get request to process
      PreparedQuery sql = new PreparedQuery(entityManager, ExternalizedQuery.getSql("BATCH.MD.GET.MASS_UPDT"));
      sql.setParameter("REQ_ID", admin.getId().getReqId());
      sql.setParameter("ITER_ID", admin.getIterationId());

      List<MassUpdt> results = sql.getResults(MassUpdt.class);
      List<String> statusCodes = new ArrayList<String>();
      StringBuilder comment = null;

      ProcessResponse response = null;
      String applicationId = BatchUtil.getAppId(data.getCmrIssuingCntry());
      List<String> rdcProcessStatusMsgs = new ArrayList<String>();
      HashMap<String, String> overallStatus = new HashMap<String, String>();

      if (results != null && results.size() > 0) {
        for (MassUpdt sMassUpdt : results) {
          comment = new StringBuilder();
          request.setCmrNo(sMassUpdt.getCmrNo());
          request.setMandt(SystemConfiguration.getValue("MANDT"));
          request.setReqId(admin.getId().getReqId());
          request.setReqType(admin.getReqType());
          request.setUserId(BATCH_USER_ID);
          request.setSapNo("");
          request.setAddrType("");
          request.setSeqNo("");

          if (!isOwnerCorrect(entityManager, sMassUpdt.getCmrNo(), data.getCmrIssuingCntry())) {
            throw new Exception("Some CMRs on the request are not owned by IBM. Please check input CMRs");
          }

          // call the create cmr service
          LOG.info("Sending request to Process Service [Request ID: " + request.getReqId() + " Type: " + request.getReqType() + "]");

          if (LOG.isTraceEnabled()) {
            LOG.trace("Request JSON:");
            DebugUtil.printObjectAsJson(LOG, request);
          }

          response = null;
          if (applicationId == null) {
            LOG.debug("No Application ID mapped to " + data.getCmrIssuingCntry());
            response = new ProcessResponse();
            response.setReqId(request.getReqId());
            response.setCmrNo(request.getCmrNo());
            response.setMandt(request.getMandt());
            response.setStatus(CmrConstants.RDC_STATUS_NOT_COMPLETED);
            response.setMessage("No application ID defined for Country: " + data.getCmrIssuingCntry() + ". Cannot process RDc records.");
          } else {
            try {
              this.serviceClient.setReadTimeout(60 * 30 * 1000); // 30 mins
              response = this.serviceClient.executeAndWrap(applicationId, request, ProcessResponse.class);

              if (response != null && response.getStatus().equals("A")
                  && response.getMessage().contains("was not successfully updated on the index.")) {
                isIndexNotUpdated = true;
                response.setStatus("C");
                response.setMessage("");
              }
            } catch (Exception e) {
              sMassUpdt.setRowStatusCd(MASS_UPDATE_FAIL);
              LOG.error("Error when connecting to the service.", e);
              response = new ProcessResponse();
              response.setReqId(admin.getId().getReqId());
              response.setCmrNo(request.getCmrNo());
              response.setMandt(request.getMandt());
              response.setStatus(CmrConstants.RDC_STATUS_ABORTED);
              response.setMessage("Cannot connect to the service at the moment.");
            }
          }

          //
          if (response.getReqId() <= 0) {
            response.setReqId(request.getReqId());
          }

          resultCode = response.getStatus();
          if (StringUtils.isBlank(resultCode)) {
            statusCodes.add(CmrConstants.RDC_STATUS_NOT_COMPLETED);
          } else {
            statusCodes.add(resultCode);
          }

          if (LOG.isTraceEnabled()) {
            LOG.trace("Response JSON:");
            DebugUtil.printObjectAsJson(LOG, response);
          }

          if (isCompletedSuccessfully(resultCode)) {
            if (response.getRecords() != null) {
              if (response != null && response.getRecords() != null && response.getRecords().size() > 0) {

                if (CmrConstants.RDC_STATUS_COMPLETED_WITH_WARNINGS.equals(resultCode)) {
                  comment.append("RDc records were not processed.");
                  comment = comment.append("Warning Message: " + response.getMessage());
                } else {
                  comment.append("Record with the following Kunnr, Address sequence and address types on request ID " + admin.getId().getReqId()
                      + " was SUCCESSFULLY processed:\n");
                  for (RDcRecord pRecord : response.getRecords()) {
                    comment.append("Kunnr: " + pRecord.getSapNo() + ", sequence number: " + pRecord.getSeqNo() + ", ");
                    comment.append(" address type: " + pRecord.getAddressType() + "\n");
                  }
                }
              }

            } else {
              comment.append("RDc records were not processed.");
              if (CmrConstants.RDC_STATUS_COMPLETED_WITH_WARNINGS.equals(resultCode)) {
                comment = comment.append("Warning Message: " + response.getMessage());
              }
            }

            if (StringUtils.isEmpty(sMassUpdt.getErrorTxt())) {
              sMassUpdt.setErrorTxt(modifyCommentLength(comment.toString()));
            } else {
              sMassUpdt.setErrorTxt(modifyCommentLength(sMassUpdt.getErrorTxt() + comment.toString()));
            }

            sMassUpdt.setRowStatusCd(MASS_UPDATE_DONE);

            rdcProcessStatusMsgs.add(CmrConstants.RDC_STATUS_COMPLETED);
          } else {
            if (CmrConstants.RDC_STATUS_ABORTED.equals(resultCode) && CmrConstants.RDC_STATUS_ABORTED.equals(processingStatus)) {
              comment = comment.append("\nRDc mass update processing for REQ ID " + request.getReqId() + " was ABORTED.");
              sMassUpdt.setRowStatusCd(CmrConstants.MASS_CREATE_ROW_STATUS_FAIL);
              sMassUpdt.setErrorTxt(modifyCommentLength(comment.toString()));
              rdcProcessStatusMsgs.add(resultCode);
            } else if (CmrConstants.RDC_STATUS_ABORTED.equalsIgnoreCase(resultCode)) {
              comment = comment.append("\nRDc mass update processing for REQ ID " + request.getReqId() + " was ABORTED.");
              sMassUpdt.setRowStatusCd(CmrConstants.MASS_CREATE_ROW_STATUS_FAIL);
              sMassUpdt.setErrorTxt(modifyCommentLength(comment.toString()));
              rdcProcessStatusMsgs.add(resultCode);
            } else if (CmrConstants.RDC_STATUS_NOT_COMPLETED.equalsIgnoreCase(resultCode)) {
              comment = comment.append("\nRDc mass update processing for REQ ID " + request.getReqId() + " is NOT COMPLETED.");
              sMassUpdt.setRowStatusCd(CmrConstants.MASS_CREATE_ROW_STATUS_FAIL);
              rdcProcessStatusMsgs.add(resultCode);
              sMassUpdt.setErrorTxt(modifyCommentLength(comment.toString()));
            } else if (CmrConstants.RDC_STATUS_IGNORED.equalsIgnoreCase(resultCode)) {
              comment = comment.append("\nRDc mass update processing for REQ ID " + request.getReqId() + " is IGNORED.");
              sMassUpdt.setRowStatusCd(CmrConstants.MASS_CREATE_ROW_STATUS_UPDATE_FAILE);
              sMassUpdt.setErrorTxt(modifyCommentLength(comment.toString()));
              rdcProcessStatusMsgs.add(resultCode);
            } else {
              sMassUpdt.setRowStatusCd(CmrConstants.MASS_CREATE_ROW_STATUS_DONE);
              sMassUpdt.setErrorTxt("");
            }
          }
          updateEntity(sMassUpdt, entityManager);
          admin.setReqStatus(CmrConstants.REQUEST_STATUS.COM.toString());
          admin.setProcessedFlag("Y");
          updateEntity(admin, entityManager);
          partialCommit(entityManager);
        }

        // *** START OF FIX
        LOG.debug("**** Placing comment on success --> " + comment);
        comment = new StringBuilder();
        if (rdcProcessStatusMsgs.size() > 0) {
          if (rdcProcessStatusMsgs.contains(CmrConstants.RDC_STATUS_ABORTED)) {
            if (isIndexNotUpdated) {
              overallStatus.put("overallStatus", CmrConstants.RDC_STATUS_COMPLETED);
            } else {
              overallStatus.put("overallStatus", CmrConstants.RDC_STATUS_ABORTED);
            }
          } else if (rdcProcessStatusMsgs.contains(CmrConstants.RDC_STATUS_NOT_COMPLETED)) {
            overallStatus.put("overallStatus", CmrConstants.RDC_STATUS_NOT_COMPLETED);
          } else if (rdcProcessStatusMsgs.contains(CmrConstants.RDC_STATUS_COMPLETED)) {
            overallStatus.put("overallStatus", CmrConstants.RDC_STATUS_COMPLETED);
          }
        } else {
          LOG.error("Response statuses is empty for request ID: " + admin.getId().getReqId());
          ProcessResponse customResponse = new ProcessResponse();
          customResponse.setMessage("No data was updated on RDc for this request. Please contact Ops for assistance.");
          overallStatus.put("overallStatus", CmrConstants.RDC_STATUS_ABORTED);
        }

        if (comment != null && !StringUtils.isEmpty(comment.toString())) {
          RequestUtils.createCommentLogFromBatch(entityManager, BATCH_USER_ID, reqId, comment.toString().trim());
        } else {
          String strOverallStatus = overallStatus.get("overallStatus");

          if (strOverallStatus != null && CmrConstants.RDC_STATUS_COMPLETED.equals(strOverallStatus)) {
            comment.append("Successfully completed RDc processing for mass update request.");
          } else {
            comment.append("Issues happened generating a processing comment. Please contact your Administrator.");
          }
          RequestUtils.createCommentLogFromBatch(entityManager, BATCH_USER_ID, reqId, comment.toString().trim());
        }

        LOG.debug("Updating Admin record for Request ID " + admin.getId().getReqId());

        if (statusCodes.contains(CmrConstants.RDC_STATUS_NOT_COMPLETED)) {
          admin.setRdcProcessingStatus(CmrConstants.RDC_STATUS_NOT_COMPLETED);
          admin.setReqStatus(CmrConstants.REQUEST_STATUS.PPN.toString());
        } else if (statusCodes.contains(CmrConstants.RDC_STATUS_ABORTED)) {
          admin.setReqStatus(CmrConstants.REQUEST_STATUS.PPN.toString());
          admin.setRdcProcessingStatus(
              processingStatus.equals(CmrConstants.RDC_STATUS_ABORTED) ? CmrConstants.RDC_STATUS_NOT_COMPLETED : CmrConstants.RDC_STATUS_ABORTED);
        } else if (statusCodes.contains(CmrConstants.RDC_STATUS_COMPLETED_WITH_WARNINGS)) {
          admin.setRdcProcessingStatus(CmrConstants.RDC_STATUS_COMPLETED_WITH_WARNINGS);
        } else {
          admin.setRdcProcessingStatus(CmrConstants.RDC_STATUS_COMPLETED);
          admin.setProcessedFlag("Y"); // set request status to processed
        }

        String rdcProcessingMsg = null;
        if ("N".equals(admin.getRdcProcessingStatus()) || "A".equals(admin.getRdcProcessingStatus())) {
          rdcProcessingMsg = "Some errors occurred during processing. Please check request's comment log for details.";
        } else {
          rdcProcessingMsg = "RDc Processing has been completed. Please check request's comment log for details.";
        }

        admin.setRdcProcessingTs(SystemUtil.getCurrentTimestamp());
        admin.setRdcProcessingMsg(rdcProcessingMsg.toString().trim());

        if (!"A".equals(admin.getRdcProcessingStatus()) && !"N".equals(admin.getRdcProcessingStatus())) {
          admin.setReqStatus("COM");
        }
        updateEntity(admin, entityManager);

        if ("N".equals(admin.getRdcProcessingStatus()) || "A".equals(admin.getRdcProcessingStatus())) {
          RequestUtils.createWorkflowHistoryFromBatch(entityManager, BATCH_USER_ID, admin,
              "Some errors occurred during RDc processing. Please check request's comment log for details.", ACTION_RDC_UPDATE, null, null,
              "COM".equals(admin.getReqStatus()));
        } else {
          RequestUtils.createWorkflowHistoryFromBatch(entityManager, BATCH_USER_ID, admin,
              "RDc  Processing has been completed. Please check request's comment log for details.", ACTION_RDC_UPDATE, null, null,
              "COM".equals(admin.getReqStatus()));
        }

        partialCommit(entityManager);
        LOG.debug(
            "Request ID " + admin.getId().getReqId() + " Status: " + admin.getRdcProcessingStatus() + " Message: " + admin.getRdcProcessingMsg());
        // *** END OF FIX
      } else {
        LOG.error("*****There are no mass update requests for RDC processing.*****");
      }
    } catch (Exception e) {
      LOG.error("Error in processing Update Request " + admin.getId().getReqId(), e);
      addError("Update Request " + admin.getId().getReqId() + " Error: " + e.getMessage());
    }
  }

  /**
   * Processes Request Type 'N'
   * 
   * @param entityManager
   * @param request
   * @param admin
   * @param data
   * @throws Exception
   */
  protected void processMassCreateRequest(EntityManager entityManager, ProcessRequest request, Admin admin, Data data) throws Exception {

    if (admin == null) {
      throw new Exception("Cannot process mass create request. Admin information is null or empty.");
    }

    PreparedQuery query = new PreparedQuery(entityManager, ExternalizedQuery.getSql("QUERY.DATA.GET.CMR.BY_REQID"));
    query.setParameter("REQ_ID", admin.getId().getReqId());
    List<Object[]> cntryList = query.getResults();
    String cntry = "";

    if (cntryList != null && cntryList.size() > 0) {
      Object[] result = cntryList.get(0);
      cntry = (String) result[0];
    } else {
      throw new Exception("Cannot process mass create request. Data information is null or empty.");
    }

    query = new PreparedQuery(entityManager, ExternalizedQuery.getSql("SYSTEM.SUPP_CNTRY_BY_CNTRY_CD"));
    query.setParameter("CNTRY_CD", cntry);
    SuppCntry suppCntry = query.getSingleResult(SuppCntry.class);

    if (suppCntry == null) {
      throw new Exception("Cannot process mass create request. Data information is null or empty.");
    } else {
      String mode = suppCntry.getSuppReqType();

      if (mode.contains("M0")) {
        throw new Exception("Cannot process mass create request. Mass Create processing is currently set to manual.");
      }
    }

    String resultCode = null;
    String processingStatus = admin.getRdcProcessingStatus() != null ? admin.getRdcProcessingStatus() : "";
    long reqId = admin.getId().getReqId();
    boolean isIndexNotUpdated = false;

    try {
      // 1. Get request to process
      PreparedQuery sql = new PreparedQuery(entityManager, ExternalizedQuery.getSql("BATCH.MD.GET.MASS_CREATE"));
      sql.setParameter("REQ_ID", admin.getId().getReqId());
      sql.setParameter("ITER_ID", admin.getIterationId());

      List<MassCreate> results = sql.getResults(MassCreate.class);
      List<String> statusCodes = new ArrayList<String>();
      StringBuilder comment = null;

      ProcessResponse response = null;
      String applicationId = BatchUtil.getAppId(data.getCmrIssuingCntry());
      List<String> rdcProcessStatusMsgs = new ArrayList<String>();
      HashMap<String, String> overallStatus = new HashMap<String, String>();
      Map<String, List<String>> cmrNoSapNoMap = new HashMap<String, List<String>>();

      if (results != null && results.size() > 0) {
        for (MassCreate massCreate : results) {
          comment = new StringBuilder();
          request.setCmrNo(massCreate.getCmrNo());
          request.setMandt(SystemConfiguration.getValue("MANDT"));
          request.setReqId(admin.getId().getReqId());
          request.setReqType(admin.getReqType());
          request.setUserId(BATCH_USER_ID);
          request.setSapNo("");
          request.setAddrType("");
          request.setSeqNo(Integer.toString(massCreate.getId().getSeqNo()));

          // call the create cmr service
          LOG.info("Sending request to Process Service [Request ID: " + request.getReqId() + " Type: " + request.getReqType() + "]");

          if (LOG.isTraceEnabled()) {
            LOG.trace("Request JSON:");
            DebugUtil.printObjectAsJson(LOG, request);
          }

          response = null;
          if (applicationId == null) {
            LOG.debug("No Application ID mapped to " + data.getCmrIssuingCntry());
            response = new ProcessResponse();
            response.setReqId(request.getReqId());
            response.setCmrNo(request.getCmrNo());
            response.setMandt(request.getMandt());
            response.setStatus(CmrConstants.RDC_STATUS_NOT_COMPLETED);
            response.setMessage("No application ID defined for Country: " + data.getCmrIssuingCntry() + ". Cannot process RDc records.");
          } else {
            try {
              this.serviceClient.setReadTimeout(60 * 30 * 1000); // 30 mins

              response = this.serviceClient.executeAndWrap(applicationId, request, ProcessResponse.class);

              if (response != null && response.getStatus().equals("A")
                  && response.getMessage().contains("was not successfully updated on the index.")) {
                isIndexNotUpdated = true;
                response.setStatus("C");
                response.setMessage("");
              }
            } catch (Exception e) {
              massCreate.setRowStatusCd(MASS_UPDATE_FAIL);
              LOG.error("Error when connecting to the service.", e);
              response = new ProcessResponse();
              response.setReqId(admin.getId().getReqId());
              response.setCmrNo(request.getCmrNo());
              response.setMandt(request.getMandt());
              response.setStatus(CmrConstants.RDC_STATUS_ABORTED);
              response.setMessage("Cannot connect to the service at the moment.");
            }
          }

          if (response.getReqId() <= 0) {
            response.setReqId(request.getReqId());
          }

          resultCode = response.getStatus();
          if (StringUtils.isBlank(resultCode)) {
            statusCodes.add(CmrConstants.RDC_STATUS_NOT_COMPLETED);
          } else {
            statusCodes.add(resultCode);
          }

          if (LOG.isTraceEnabled()) {
            LOG.trace("Response JSON:");
            DebugUtil.printObjectAsJson(LOG, response);
          }

          if (isCompletedSuccessfully(resultCode)) {
            if (response.getRecords() != null) {
              if (response != null && response.getRecords() != null && response.getRecords().size() > 0) {

                if (CmrConstants.RDC_STATUS_COMPLETED_WITH_WARNINGS.equals(resultCode)) {
                  comment.append("RDc records were not processed.");
                  comment = comment.append("Warning Message: " + response.getMessage());
                } else {
                  comment.append("Record with the following Kunnr, Address sequence and address types on request ID " + admin.getId().getReqId()
                      + " was SUCCESSFULLY processed:\n");

                  for (RDcRecord pRecord : response.getRecords()) {
                    comment.append("Kunnr: " + pRecord.getSapNo() + ", sequence number: " + pRecord.getSeqNo() + ", ");
                    comment.append(" address type: " + pRecord.getAddressType() + "\n");

                    if (!cmrNoSapNoMap.containsKey(response.getCmrNo())) {
                      cmrNoSapNoMap.put(response.getCmrNo(), new ArrayList<String>());
                    }
                    cmrNoSapNoMap.get(response.getCmrNo()).add(pRecord.getSapNo());
                  } // for
                }
              }

            } else {
              comment.append("RDc records were not processed.");
              if (CmrConstants.RDC_STATUS_COMPLETED_WITH_WARNINGS.equals(resultCode)) {
                comment = comment.append("Warning Message: " + response.getMessage());
              }
            }

            if (StringUtils.isEmpty(massCreate.getErrorTxt())) {
              massCreate.setErrorTxt(comment.toString());
            } else {
              massCreate.setErrorTxt(massCreate.getErrorTxt() + comment.toString());
            }

            massCreate.setRowStatusCd(MASS_UPDATE_DONE);
            massCreate.setCmrNo(response.getCmrNo());
            rdcProcessStatusMsgs.add(CmrConstants.RDC_STATUS_COMPLETED);

          } else {
            if (CmrConstants.RDC_STATUS_ABORTED.equals(resultCode) && CmrConstants.RDC_STATUS_ABORTED.equals(processingStatus)) {
              comment = comment.append("\nRDc mass create processing for REQ ID " + request.getReqId() + " was ABORTED.");
              massCreate.setRowStatusCd(CmrConstants.MASS_CREATE_ROW_STATUS_FAIL);
              massCreate.setErrorTxt(comment.toString());
              rdcProcessStatusMsgs.add(resultCode);
            } else if (CmrConstants.RDC_STATUS_ABORTED.equalsIgnoreCase(resultCode)) {
              comment = comment.append("\nRDc mass create processing for REQ ID " + request.getReqId() + " was ABORTED.");
              massCreate.setRowStatusCd(CmrConstants.MASS_CREATE_ROW_STATUS_FAIL);
              massCreate.setErrorTxt(comment.toString());
              rdcProcessStatusMsgs.add(resultCode);
            } else if (CmrConstants.RDC_STATUS_NOT_COMPLETED.equalsIgnoreCase(resultCode)) {
              comment = comment.append("\nRDc mass create processing for REQ ID " + request.getReqId() + " is NOT COMPLETED.");
              massCreate.setRowStatusCd(CmrConstants.MASS_CREATE_ROW_STATUS_FAIL);
              rdcProcessStatusMsgs.add(resultCode);
              massCreate.setErrorTxt(comment.toString());
            } else if (CmrConstants.RDC_STATUS_IGNORED.equalsIgnoreCase(resultCode)) {
              comment = comment.append("\nRDc mass create processing for REQ ID " + request.getReqId() + " is IGNORED.");
              massCreate.setRowStatusCd(CmrConstants.MASS_CREATE_ROW_STATUS_UPDATE_FAILE);
              massCreate.setErrorTxt(comment.toString());
              rdcProcessStatusMsgs.add(resultCode);
            } else {
              massCreate.setRowStatusCd(CmrConstants.MASS_CREATE_ROW_STATUS_DONE);
              massCreate.setErrorTxt("");
            }
          }
          updateEntity(massCreate, entityManager);
          admin.setReqStatus(CmrConstants.REQUEST_STATUS.COM.toString());
          admin.setProcessedFlag("Y");
          updateEntity(admin, entityManager);
          partialCommit(entityManager);
        } // FOR

        LOG.debug("**** Placing comment on success --> " + comment);
        comment = new StringBuilder();
        if (rdcProcessStatusMsgs.size() > 0) {
          if (rdcProcessStatusMsgs.contains(CmrConstants.RDC_STATUS_ABORTED)) {
            if (isIndexNotUpdated) {
              overallStatus.put("overallStatus", CmrConstants.RDC_STATUS_COMPLETED);
            } else {
              overallStatus.put("overallStatus", CmrConstants.RDC_STATUS_ABORTED);
            }
          } else if (rdcProcessStatusMsgs.contains(CmrConstants.RDC_STATUS_NOT_COMPLETED)) {
            overallStatus.put("overallStatus", CmrConstants.RDC_STATUS_NOT_COMPLETED);
          } else if (rdcProcessStatusMsgs.contains(CmrConstants.RDC_STATUS_COMPLETED)) {
            overallStatus.put("overallStatus", CmrConstants.RDC_STATUS_COMPLETED);
          }
        } else {
          LOG.error("Response statuses is empty for request ID: " + admin.getId().getReqId());
          ProcessResponse customResponse = new ProcessResponse();
          customResponse.setMessage("No data was updated on RDC for this request. Please contact Ops for assistance.");
          overallStatus.put("overallStatus", CmrConstants.RDC_STATUS_ABORTED);
        }

        if (comment != null && !StringUtils.isEmpty(comment.toString())) {
          RequestUtils.createCommentLogFromBatch(entityManager, BATCH_USER_ID, reqId, comment.toString().trim());
        } else {
          String strOverallStatus = overallStatus.get("overallStatus");

          if (strOverallStatus != null && CmrConstants.RDC_STATUS_COMPLETED.equals(strOverallStatus)) {
            comment.append("All records processed successfully. Please check summary notification mail and/or request summary for details.");
          } else {
            comment.append("Issues happened generating a processing comment. Please contact your Administrator.");
          }
          RequestUtils.createCommentLogFromBatch(entityManager, BATCH_USER_ID, reqId, comment.toString().trim());
        }

        LOG.debug("Updating Admin record for Request ID " + admin.getId().getReqId());

        if (statusCodes.contains(CmrConstants.RDC_STATUS_NOT_COMPLETED)) {
          admin.setRdcProcessingStatus(CmrConstants.RDC_STATUS_NOT_COMPLETED);
          admin.setReqStatus(CmrConstants.REQUEST_STATUS.PPN.toString());
        } else if (statusCodes.contains(CmrConstants.RDC_STATUS_ABORTED)) {
          admin.setReqStatus(CmrConstants.REQUEST_STATUS.PPN.toString());
          admin.setRdcProcessingStatus(
              processingStatus.equals(CmrConstants.RDC_STATUS_ABORTED) ? CmrConstants.RDC_STATUS_NOT_COMPLETED : CmrConstants.RDC_STATUS_ABORTED);
        } else if (statusCodes.contains(CmrConstants.RDC_STATUS_COMPLETED_WITH_WARNINGS)) {
          admin.setRdcProcessingStatus(CmrConstants.RDC_STATUS_COMPLETED_WITH_WARNINGS);
        } else {
          admin.setRdcProcessingStatus(CmrConstants.RDC_STATUS_COMPLETED);
          admin.setProcessedFlag("Y"); // set request status to processed
        }

        String rdcProcessingMsg = null;
        if ("N".equals(admin.getRdcProcessingStatus()) || "A".equals(admin.getRdcProcessingStatus())) {
          rdcProcessingMsg = "Some errors occurred during processing. Please check request's comment log for details.";
        } else {
          rdcProcessingMsg = "RDc Processing has been completed. Please check request's comment log for details.";
        }

        admin.setRdcProcessingTs(SystemUtil.getCurrentTimestamp());
        admin.setRdcProcessingMsg(rdcProcessingMsg.toString().trim());

        if (!"A".equals(admin.getRdcProcessingStatus()) && !"N".equals(admin.getRdcProcessingStatus())) {
          admin.setReqStatus("COM");
        }
        updateEntity(admin, entityManager);

        if ("N".equals(admin.getRdcProcessingStatus()) || "A".equals(admin.getRdcProcessingStatus())) {
          RequestUtils.createWorkflowHistoryFromBatch(entityManager, BATCH_USER_ID, admin,
              "Some errors occurred during RDc processing. Please check request's comment log for details.", ACTION_RDC_UPDATE, null, null,
              "COM".equals(admin.getReqStatus()));
        } else {
          RequestUtils.createWorkflowHistoryFromBatch(entityManager, BATCH_USER_ID, admin,
              "RDc  Processing has been completed. Please check request's comment log for details.", ACTION_RDC_UPDATE, null, null,
              "COM".equals(admin.getReqStatus()));
        }

        partialCommit(entityManager);
        LOG.debug(
            "Request ID " + admin.getId().getReqId() + " Status: " + admin.getRdcProcessingStatus() + " Message: " + admin.getRdcProcessingMsg());

        if ("COM".equals(admin.getReqStatus())) {
          sendMasscreateCMRNos(entityManager, reqId, admin.getIterationId(), cmrNoSapNoMap);
        }

      } else {
        LOG.error("*****There are no mass create requests for RDC processing.*****");
      }
    } catch (Exception e) {
      LOG.error("Error in processing Create Request " + admin.getId().getReqId(), e);
      addError("Create Request " + admin.getId().getReqId() + " Error: " + e.getMessage());
    }
  }

  private void sendMasscreateCMRNos(EntityManager em, long reqId, int itrId, Map<String, List<String>> cmrNoSapNoMap) throws Exception {
    PreparedQuery cmrSuccessQuery = new PreparedQuery(em, ExternalizedQuery.getSql("BATCH.MASS_CREATE_DATA_CMR_RECORDS"));
    cmrSuccessQuery.setParameter("REQ_ID", reqId);
    cmrSuccessQuery.setParameter("ITERATION_ID", itrId);
    String map = Admin.BATCH_CMR_DATA_MAPPING;
    List<CompoundEntity> resultSuccessful = cmrSuccessQuery.getCompundResults(Admin.class, map);

    MassCreate massCreate = null;
    MassCreateData massCreateData = null;
    Admin adminData = null;
    List<MassCreateBatchEmailModel> cmrList = new ArrayList<MassCreateBatchEmailModel>();
    MassCreateBatchEmailModel record = null;
    for (CompoundEntity entity : resultSuccessful) {
      massCreate = entity.getEntity(MassCreate.class);
      record = new MassCreateBatchEmailModel();
      adminData = entity.getEntity(Admin.class);
      massCreateData = entity.getEntity(MassCreateData.class);

      record.setCmrNo(massCreate.getCmrNo());
      record.setCustName(massCreateData.getCustNm1() + (massCreateData.getCustNm2() != null ? massCreateData.getCustNm2() : ""));
      record.setRowNo(massCreate.getId().getSeqNo());
      if ("DONE".equals(massCreate.getRowStatusCd())) {
        record.setErrorMsg("");
      } else {
        record.setErrorMsg(massCreate.getErrorTxt());
      }

      StringBuilder sapNoAll = new StringBuilder();
      List<String> sapNos = cmrNoSapNoMap.get(record.getCmrNo());
      if (sapNos != null) {
        for (String sapNo : sapNos) {
          sapNoAll.append(sapNoAll.length() > 0 ? "<br>" : "");
          sapNoAll.append(sapNo);
        }
      }
      record.setSapNo(sapNoAll.toString());
      cmrList.add(record);
    }

    LOG.info("CMRs records size : " + cmrList.size());
    if (cmrList.size() > 0) {
      RequestUtils.sendMassCreateCMRNotifications(em, cmrList, adminData);
    }
  }

  private List<Admin> getPendingRecordsRDCLA(EntityManager entityManager) {
    String sql = ExternalizedQuery.getSql("LA.GET_MASS_PROCESS_PENDING.RDC");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    return query.getResults(Admin.class);
  }

}
