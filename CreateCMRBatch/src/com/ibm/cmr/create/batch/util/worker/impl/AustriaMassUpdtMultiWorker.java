/**
 * 
 */
package com.ibm.cmr.create.batch.util.worker.impl;

import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.entity.DataPK;
import com.ibm.cio.cmr.request.entity.MassUpdt;
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
public class AustriaMassUpdtMultiWorker extends MassUpdateMultiWorker {

  private static final Logger LOG = Logger.getLogger(AustriaMassUpdtMultiWorker.class);
  private static final String MASS_UPDATE_FAIL = "FAIL";
  private static final String MASS_UPDATE_DONE = "DONE";
  private boolean indexNotUpdated;

  /**
   * @param parentAdmin
   * @param parentEntity
   */
  public AustriaMassUpdtMultiWorker(MultiThreadedBatchService<?> parentService, Admin parentAdmin, MassUpdt parentEntity) {
    super(parentService, parentAdmin, parentEntity);

  }

  @Override
  public void executeProcess(EntityManager entityManager) throws Exception {
    String resultCode = null;
    ServiceClient serviceClient = CmrServicesFactory.getInstance().createClient(SystemConfiguration.getValue("BATCH_SERVICES_URL"),
        ProcessClient.class);

    DataPK dataPk = new DataPK();
    dataPk.setReqId(this.parentAdmin.getId().getReqId());
    Data data = entityManager.find(Data.class, dataPk);

    String processingStatus = this.parentAdmin.getRdcProcessingStatus() != null ? this.parentAdmin.getRdcProcessingStatus() : "";

    ProcessRequest request = new ProcessRequest();
    request.setCmrNo(this.parentRow.getCmrNo());
    request.setMandt(SystemConfiguration.getValue("MANDT"));
    request.setReqId(this.parentAdmin.getId().getReqId());
    request.setReqType(this.parentAdmin.getReqType());
    request.setUserId(BATCH_USER_ID);
    request.setSapNo("");
    request.setAddrType("");
    request.setSeqNo("");

    // call the create cmr service
    LOG.info("Sending request to Process Service [Request ID: " + request.getReqId() + " Type: " + request.getReqType() + " CMR No.: "
        + request.getCmrNo() + "]");

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
        serviceClient.setReadTimeout(60 * 30 * 1000); // 30 mins
        response = serviceClient.executeAndWrap(applicationId, request, ProcessResponse.class);

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

    if (response.getReqId() <= 0) {
      response.setReqId(request.getReqId());
    }

    resultCode = response.getStatus();
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
      if (response.getRecords() != null) {
        if (response != null && response.getRecords() != null && response.getRecords().size() > 0) {
          addComment("Record with the following Kunnr, Address sequence and address types on request ID " + this.parentAdmin.getId().getReqId()
              + " was SUCCESSFULLY processed:\n");
          for (RDcRecord pRecord : response.getRecords()) {
            addComment("Kunnr: " + pRecord.getSapNo() + ", sequence number: " + pRecord.getSeqNo() + ", ");
            addComment(" address type: " + pRecord.getAddressType() + "\n");
          }
        }
      } else {
        addComment("RDc records were not processed.");
        if (CmrConstants.RDC_STATUS_COMPLETED_WITH_WARNINGS.equals(resultCode)) {
          addComment("Warning Message: " + response.getMessage());
        }
      }

      String errorTxt = (!StringUtils.isBlank(this.parentRow.getErrorTxt()) ? this.parentRow.getErrorTxt() : "") + getComments();
      this.parentRow.setErrorTxt(errorTxt);

      this.parentRow.setRowStatusCd(MASS_UPDATE_DONE);

      addRdcStatus(CmrConstants.RDC_STATUS_COMPLETED);
    } else {
      if (CmrConstants.RDC_STATUS_ABORTED.equals(resultCode) && CmrConstants.RDC_STATUS_ABORTED.equals(processingStatus)) {
        addComment("\nRDc mass update processing for REQ ID " + request.getReqId() + " and CMR number" + request.getCmrNo() + "was ABORTED.");
        this.parentRow.setRowStatusCd(CmrConstants.MASS_CREATE_ROW_STATUS_FAIL);
      } else if (CmrConstants.RDC_STATUS_ABORTED.equalsIgnoreCase(resultCode)) {
        addComment("\nRDc mass update processing for REQ ID " + request.getReqId() + " and CMR number" + request.getCmrNo() + " was ABORTED.");
        this.parentRow.setRowStatusCd(CmrConstants.MASS_CREATE_ROW_STATUS_FAIL);
      } else if (CmrConstants.RDC_STATUS_NOT_COMPLETED.equalsIgnoreCase(resultCode)) {
        addComment("\nRDc mass update processing for REQ ID " + request.getReqId() + " and CMR number" + request.getCmrNo() + " is NOT COMPLETED.");
        this.parentRow.setRowStatusCd(CmrConstants.MASS_CREATE_ROW_STATUS_FAIL);
      } else if (CmrConstants.RDC_STATUS_IGNORED.equalsIgnoreCase(resultCode)) {
        addComment("\nRDc mass update processing for REQ ID " + request.getReqId() + " and CMR number" + request.getCmrNo() + " is IGNORED.");
        this.parentRow.setRowStatusCd(CmrConstants.MASS_CREATE_ROW_STATUS_UPDATE_FAILE);
      } else {
        this.parentRow.setRowStatusCd(CmrConstants.MASS_CREATE_ROW_STATUS_DONE);
        this.parentRow.setErrorTxt("");
      }
      if (!CmrConstants.MASS_CREATE_ROW_STATUS_DONE.equals(this.parentRow.getRowStatusCd())) {
        this.parentRow.setErrorTxt(getComments());
        addRdcStatus(resultCode);
      }

    }
    if (this.parentRow.getErrorTxt() != null && this.parentRow.getErrorTxt().length() > 10000) {
      this.parentRow.setErrorTxt(this.parentRow.getErrorTxt().substring(0, 9999));
    }
    entityManager.merge(this.parentRow);

  }

  public boolean isIndexNotUpdated() {
    return indexNotUpdated;
  }

}
