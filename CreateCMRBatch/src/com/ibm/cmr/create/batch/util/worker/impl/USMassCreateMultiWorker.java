/**
 * 
 */
package com.ibm.cmr.create.batch.util.worker.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.MassCreate;
import com.ibm.cio.cmr.request.entity.MassCreateAddr;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.util.SystemLocation;
import com.ibm.cmr.create.batch.model.CmrServiceInput;
import com.ibm.cmr.create.batch.service.MultiThreadedBatchService;
import com.ibm.cmr.create.batch.util.DebugUtil;
import com.ibm.cmr.create.batch.util.worker.MassCreateMultiWorker;
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
public class USMassCreateMultiWorker extends MassCreateMultiWorker {

  private static final Logger LOG = Logger.getLogger(USMassCreateMultiWorker.class);

  private MassCreate record;
  private CmrServiceInput input;
  private long iterationId;
  private Map<String, List<String>> cmrNoSapNoMap;
  private String cntryCode;

  /**
   * @param parentAdmin
   * @param parentEntity
   */
  public USMassCreateMultiWorker(MultiThreadedBatchService<?> parentService, Admin parentAdmin, MassCreate parentEntity, CmrServiceInput input,
      Map<String, List<String>> cmrNoSapNoMap, String cntryCode) {
    super(parentService, parentAdmin, parentEntity);
    this.record = parentEntity;
    this.input = input;
    this.iterationId = parentEntity.getId().getIterationId();
    this.cmrNoSapNoMap = cmrNoSapNoMap;
    this.cntryCode=cntryCode;

  }

  @Override
  public void executeProcess(EntityManager entityManager) throws Exception {

    ServiceClient serviceClient = CmrServicesFactory.getInstance().createClient(BATCH_SERVICES_URL, ProcessClient.class);

    ProcessRequest request = new ProcessRequest();
    request.setCmrNo(this.record.getCmrNo());
    request.setMandt(this.input.getInputMandt());
    request.setReqId(this.input.getInputReqId());

    if (CmrConstants.REQ_TYPE_MASS_CREATE.equals(this.input.getInputReqType()) && SystemLocation.UNITED_STATES.equals(cntryCode)) {
      request.setReqType(CmrConstants.REQ_TYPE_MASS_CREATE);
    } else {
      request.setReqType(CmrConstants.REQ_TYPE_CREATE);
    }
    request.setUserId(this.input.getInputUserId());
    request.setIterationId(this.iterationId);

    if (!cmrNoSapNoMap.containsKey(this.record.getCmrNo())) {
      cmrNoSapNoMap.put(this.record.getCmrNo(), new ArrayList<String>());
    }

    LOG.info("Sending request to Process Service [Request ID: " + request.getReqId() + " CMR No: " + request.getCmrNo() + " Type: "
        + request.getReqType() + "]");

    // call the create cmr service
    LOG.trace("Request JSON:");
    if (LOG.isTraceEnabled()) {
      DebugUtil.printObjectAsJson(LOG, request);
    }

    ProcessResponse response = null;
    try {
      serviceClient.setReadTimeout(60 * 10 * 1000); // 10 mins
      response = serviceClient.executeAndWrap(ProcessClient.US_APP_ID, request, ProcessResponse.class);
    } catch (Exception e) {
      LOG.error("Error when connecting to the service.", e);
      response = new ProcessResponse();
      response.setStatus(CmrConstants.RDC_STATUS_ABORTED);
      response.setReqId(request.getReqId());
      response.setCmrNo(this.record.getCmrNo());
      response.setMandt(request.getMandt());
      response.setMessage("Cannot connect to the service at the moment.");
    }

    String resultCode = response.getStatus();
    LOG.trace("Response JSON:");
    if (LOG.isTraceEnabled()) {
      DebugUtil.printObjectAsJson(LOG, response);
    }

    if (!StringUtils.isBlank(resultCode) && !getStatusCodes().contains(resultCode)) {
      addStatusCode(resultCode);
    }
    LOG.info("Response received from Process Service [Request ID: " + response.getReqId() + " CMR No: " + response.getCmrNo() + " Status: "
        + response.getStatus() + " Message: " + (response.getMessage() != null ? response.getMessage() : "-") + "]");

    // get the results from the service and process json response

    if (CmrConstants.RDC_STATUS_COMPLETED.equalsIgnoreCase(response.getStatus())
        || CmrConstants.RDC_STATUS_COMPLETED_WITH_WARNINGS.equalsIgnoreCase(response.getStatus())) {
      // get the update date from the RDC Records returned from the
      // service
      for (RDcRecord record : response.getRecords()) {
        // update MASS_CREATE_ADDR Table for the update requests
        PreparedQuery createAddrQry = new PreparedQuery(entityManager, ExternalizedQuery.getSql("BATCH.GET_MASS_ADDR_ENTITY_CREATE_REQ"));
        createAddrQry.setParameter("REQ_ID", this.record.getId().getParReqId());
        createAddrQry.setParameter("ITERATION_ID", this.iterationId);
        createAddrQry.setParameter("ADDR_TYPE", "ZS01".equals(record.getAddressType()) || "ZI01".equals(record.getAddressType()) ? "ZS01" : "ZI01");
        createAddrQry.setParameter("SEQ_NO", this.record.getId().getSeqNo());
        List<MassCreateAddr> cretAddrList = createAddrQry.getResults(MassCreateAddr.class);
        for (MassCreateAddr cretAddrEntity : cretAddrList) {
          cretAddrEntity.setSapNo(record.getSapNo());
          entityManager.merge(cretAddrEntity);
        }
        if (!cmrNoSapNoMap.containsKey(this.record.getCmrNo())) {
          cmrNoSapNoMap.put(this.record.getCmrNo(), new ArrayList<String>());
        }
        cmrNoSapNoMap.get(this.record.getCmrNo()).add(record.getSapNo());
      }
    }

    // update MASS_CREATE table with the error txt and row status cd
    // PreparedQuery cretMassAddrQry = new PreparedQuery(this.entityManager,
    // ExternalizedQuery.getSql("BATCH.GET_MASS_CREATE_ENTITY"));
    // cretMassAddrQry.setParameter("REQ_ID",
    // this.record.getId().getParReqId());
    // cretMassAddrQry.setParameter("ITERATION_ID", this.iterationId);
    // cretMassAddrQry.setParameter("CMR_NO", response.getCmrNo());
    // List<MassCreate> createList =
    // cretMassAddrQry.getResults(MassCreate.class);

    MassCreate massCretEntity = this.record;
    // for (MassCreate massCretEntity : createList) {
    if (null != response.getStatus() && CmrConstants.RDC_STATUS_NOT_COMPLETED.equalsIgnoreCase((response.getStatus()))) {
      if (massCretEntity.getRowStatusCd().equalsIgnoreCase(CmrConstants.MASS_CREATE_ROW_STATUS_UPDATE_FAILE)) {
        massCretEntity.setErrorTxt(response.getMessage() + " The automatic update also failed.");
      } else {
        massCretEntity.setErrorTxt(response.getMessage());
        massCretEntity.setRowStatusCd("RDCER");
      }
    } else if (null != response.getStatus() && (CmrConstants.RDC_STATUS_COMPLETED.equalsIgnoreCase(response.getStatus())
        || CmrConstants.RDC_STATUS_COMPLETED_WITH_WARNINGS.equalsIgnoreCase(response.getStatus()))) {
      if (massCretEntity.getRowStatusCd().equalsIgnoreCase(CmrConstants.MASS_CREATE_ROW_STATUS_UPDATE_FAILE)) {
        massCretEntity.setErrorTxt("The automatic update failed.");
      } else {
        massCretEntity.setRowStatusCd("DONE");
      }
    }
    LOG.info("Mass Create Record Updated [Request ID: " + massCretEntity.getId().getParReqId() + " CMR_NO: " + this.record.getCmrNo() + " SEQ No: "
        + massCretEntity.getId().getSeqNo() + "]");
    entityManager.merge(massCretEntity);
    // }

    // this.entityManager.flush();

  }

}
