/**
 * 
 */
package com.ibm.cmr.create.batch.util.worker.impl;

import java.util.List;

import javax.persistence.EntityManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.entity.DataPK;
import com.ibm.cio.cmr.request.entity.MassUpdt;
import com.ibm.cio.cmr.request.entity.MassUpdtData;
import com.ibm.cio.cmr.request.entity.MassUpdtDataPK;
import com.ibm.cio.cmr.request.entity.ReqCmtLog;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.util.SystemLocation;
import com.ibm.cmr.create.batch.service.MultiThreadedBatchService;
import com.ibm.cmr.create.batch.util.BatchUtil;
import com.ibm.cmr.create.batch.util.DebugUtil;
import com.ibm.cmr.create.batch.util.worker.MassUpdateMultiWorker;
import com.ibm.cmr.services.client.CmrServicesFactory;
import com.ibm.cmr.services.client.ProcessClient;
import com.ibm.cmr.services.client.ServiceClient;
import com.ibm.cmr.services.client.process.ProcessRequest;
import com.ibm.cmr.services.client.process.ProcessResponse;
import com.ibm.cmr.services.client.process.RDcRecord;

/**
 * @author 136786PH1
 *
 */
public class LDMassUpdtRdcMultiWorker extends MassUpdateMultiWorker {

  private static final Logger LOG = Logger.getLogger(LDMassUpdtRdcMultiWorker.class);
  private boolean indexNotUpdated;
  private Integer cmrLimit;

  /**
   * @param parentAdmin
   * @param parentEntity
   */
  public LDMassUpdtRdcMultiWorker(MultiThreadedBatchService<?> parentService, Admin parentAdmin, MassUpdt parentEntity) {
    super(parentService, parentAdmin, parentEntity);

  }

  public LDMassUpdtRdcMultiWorker(MultiThreadedBatchService<?> parentService, Admin parentAdmin, MassUpdt parentEntity, Integer cmrLimit) {
    super(parentService, parentAdmin, parentEntity);
    this.cmrLimit = cmrLimit;
  }

  @Override
  public void executeProcess(EntityManager entityManager) throws Exception {
    ServiceClient serviceClient = CmrServicesFactory.getInstance().createClient(SystemConfiguration.getValue("BATCH_SERVICES_URL"),
        ProcessClient.class);

    String processingStatus = this.parentAdmin.getRdcProcessingStatus() != null ? this.parentAdmin.getRdcProcessingStatus() : "";

    DataPK dataPk = new DataPK();
    dataPk.setReqId(this.parentAdmin.getId().getReqId());
    Data data = entityManager.find(Data.class, dataPk);

    if (!CmrConstants.REQUEST_STATUS.PCO.toString().equals(this.parentAdmin.getReqStatus())) {
      this.parentAdmin.setReqStatus(CmrConstants.REQUEST_STATUS.PCO.toString());
      entityManager.merge(this.parentAdmin);
    }
    // CMR-2279: ISR update in this.parentRowData for Turkey
    if (SystemLocation.TURKEY.equals(data.getCmrIssuingCntry())) {
      MassUpdtDataPK muDataPK = new MassUpdtDataPK();
      muDataPK.setIterationId(this.parentRow.getId().getIterationId());
      muDataPK.setParReqId(this.parentRow.getId().getParReqId());
      muDataPK.setSeqNo(this.parentRow.getId().getSeqNo());
      MassUpdtData muData = entityManager.find(MassUpdtData.class, muDataPK);

      if (!StringUtils.isBlank(muData.getCustNm1())) {
        String sql = ExternalizedQuery.getSql("LEGACY.GET_ISR_BYSBO");
        PreparedQuery q = new PreparedQuery(entityManager, sql);
        q.setParameter("SBO", muData.getCustNm1());
        q.setParameter("CNTRY", SystemLocation.TURKEY);
        String isr = q.getSingleResult(String.class);
        if (!StringUtils.isBlank(isr)) {
          muData.setRepTeamMemberNo(isr);
          entityManager.merge(muData);
        } else {
          muData.setRepTeamMemberNo("");
          entityManager.merge(muData);
        }
      }
    }

    ProcessRequest request = new ProcessRequest();
    request.setCmrNo(this.parentRow.getCmrNo());
    request.setMandt(SystemConfiguration.getValue("MANDT"));
    request.setReqId(this.parentAdmin.getId().getReqId());
    request.setReqType(this.parentAdmin.getReqType());
    request.setUserId(BATCH_USER_ID);
    request.setSapNo("");
    request.setAddrType("");
    request.setSeqNo("");

    if (this.cmrLimit != null) {
      request.setCmrLimit(this.cmrLimit);
    }

    // call the create cmr service
    LOG.info("Sending request to Process Service [Request ID: " + request.getReqId() + " Type: " + request.getReqType() + "]");

    if (LOG.isTraceEnabled()) {
      LOG.trace("Request JSON:");
      DebugUtil.printObjectAsJson(LOG, request);
    }

    ProcessResponse response = null;
    String applicationId = BatchUtil.getAppId(data.getCmrIssuingCntry());

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
        if (isForErrorTests(entityManager, this.parentAdmin)) {
          response = processMassUpdateError(this.parentAdmin, request.getCmrNo());
        } else {
          serviceClient.setReadTimeout(60 * 60 * 1000); // 30 mins
          response = serviceClient.executeAndWrap(applicationId, request, ProcessResponse.class);
        }

        if (response != null && response.getStatus().equals("A") && response.getMessage().contains("was not successfully updated on the index.")) {
          this.indexNotUpdated = true;
          response.setStatus("C");
          response.setMessage("");
        }
      } catch (Exception e) {
        this.parentRow.setRowStatusCd(MASS_UPDATE_FAIL);
        LOG.error("Error when connecting to the service.", e);
        response = new ProcessResponse();
        response.setReqId(this.parentAdmin.getId().getReqId());
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

    String resultCode = response.getStatus();
    if (StringUtils.isBlank(resultCode)) {
      addStatusCode(CmrConstants.RDC_STATUS_NOT_COMPLETED);
    } else {
      addStatusCode(resultCode);
    }

    if (LOG.isTraceEnabled()) {
      LOG.trace("Response JSON:");
      DebugUtil.printObjectAsJson(LOG, response);
    }

    if (isCompletedSuccessfully(resultCode)) {
      if (response.getRecords() != null && response.getRecords().size() > 0) {
        if (response != null && response.getRecords() != null && response.getRecords().size() > 0) {
          addComment("Record with the following Kunnr, Address sequence and address types on request ID " + this.parentAdmin.getId().getReqId()
              + " was SUCCESSFULLY processed:\n");
          for (RDcRecord pRecord : response.getRecords()) {
            addComment("Kunnr: " + pRecord.getSapNo() + ", sequence number: " + pRecord.getSeqNo() + ", ");
            addComment(" address type: " + pRecord.getAddressType() + "\n");
          }
        }
      } else {
        addComment("\n\nRDc records were not processed.");
        if (CmrConstants.RDC_STATUS_COMPLETED_WITH_WARNINGS.equals(resultCode)) {
          addComment("\nWarning Message: " + response.getMessage());
        }
      }
      LOG.debug(getComments());

      String errorTxt = (!StringUtils.isBlank(this.parentRow.getErrorTxt()) ? this.parentRow.getErrorTxt() : "") + getComments();
      this.parentRow.setErrorTxt(errorTxt);

      this.parentRow.setRowStatusCd(MASS_UPDATE_DONE);
      addRdcStatus(CmrConstants.RDC_STATUS_COMPLETED);
    } else {
      if (CmrConstants.RDC_STATUS_ABORTED.equals(resultCode) && CmrConstants.RDC_STATUS_ABORTED.equals(processingStatus)) {
        addComment("\nRDc mass update processing for REQ ID " + request.getReqId() + " was ABORTED.");
        this.parentRow.setRowStatusCd(CmrConstants.MASS_CREATE_ROW_STATUS_FAIL);
      } else if (CmrConstants.RDC_STATUS_ABORTED.equalsIgnoreCase(resultCode)) {
        addComment("\nRDc mass update processing for REQ ID " + request.getReqId() + " was ABORTED.");
        this.parentRow.setRowStatusCd(CmrConstants.MASS_CREATE_ROW_STATUS_FAIL);
      } else if (CmrConstants.RDC_STATUS_NOT_COMPLETED.equalsIgnoreCase(resultCode)) {
        addComment("\nRDc mass update processing for REQ ID " + request.getReqId() + " is NOT COMPLETED.");
        this.parentRow.setRowStatusCd(CmrConstants.MASS_CREATE_ROW_STATUS_FAIL);
      } else if (CmrConstants.RDC_STATUS_IGNORED.equalsIgnoreCase(resultCode)) {
        addComment("\nRDc mass update processing for REQ ID " + request.getReqId() + " is IGNORED.");
        this.parentRow.setRowStatusCd(CmrConstants.MASS_CREATE_ROW_STATUS_UPDATE_FAILE);
      } else {
        this.parentRow.setRowStatusCd(CmrConstants.MASS_CREATE_ROW_STATUS_DONE);
        this.parentRow.setErrorTxt("");
      }
      if (!CmrConstants.MASS_CREATE_ROW_STATUS_DONE.equals(this.parentRow.getRowStatusCd())) {
        this.parentRow.setErrorTxt(getComments());
        addRdcStatus(resultCode);
      }
      LOG.debug(getComments());
    }
    if (this.parentRow.getErrorTxt() != null && this.parentRow.getErrorTxt().length() > 10000) {
      this.parentRow.setErrorTxt(this.parentRow.getErrorTxt().substring(0, 9999));
    }

    entityManager.merge(this.parentRow);
  }

  public boolean isIndexNotUpdated() {
    return indexNotUpdated;
  }

  private boolean isForErrorTests(EntityManager entityManager, Admin admin) {
    boolean isForErrorTests = false;
    // 1. Check if request reason is "Other"
    if ("OTH".equals(admin.getReqReason())) {
      // 2. We need to get the bottom most comment
      // REQUESTENTRY.REQ_CMT_LOG.SEARCH_BY_REQID
      String sql = ExternalizedQuery.getSql("REQUESTENTRY.REQ_CMT_LOG.SEARCH_BY_REQID");
      PreparedQuery query = new PreparedQuery(entityManager, sql);
      query.setParameter("REQ_ID", admin.getId().getReqId());
      List<ReqCmtLog> comments = query.getResults(ReqCmtLog.class);

      if (comments != null && comments.size() > 0) {
        int size = comments.size();
        ReqCmtLog rcLog = comments.get(size - 1);
        // 3.If bottom most comment is "ERROR TEST" then we return true
        if (rcLog != null && "ERROR TEST".equals(rcLog.getCmt() != null ? rcLog.getCmt().toUpperCase() : "")) {
          isForErrorTests = true;
        }
      }
    }
    return isForErrorTests;
  }

  /**
   * This is a test method to test error returns from the service.
   * 
   * @param cmmaMgr
   * @param rdcMgr
   * @param reqContainer
   * @param mandt
   * @param katr6
   * @param requestObj
   * @return
   * @throws Exception
   */
  protected ProcessResponse processMassUpdateError(Admin admin, String cmrNo) throws Exception {
    long reqId = admin.getId().getReqId();
    String cmr = cmrNo;
    LOG.debug("**** START Mass Update operation for reqId >> " + reqId + ", cmrNo >> " + cmr + " ****");
    ProcessResponse response = new ProcessResponse();
    response.setReqId(reqId);
    response.setCmrNo(cmr);
    response.setStatus("N");
    response.setMessage("At least 1 error occurred during the mass update");
    return response;
  }

}
