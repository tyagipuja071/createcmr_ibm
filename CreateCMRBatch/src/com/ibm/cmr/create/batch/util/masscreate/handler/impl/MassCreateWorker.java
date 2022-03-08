/**
 * 
 */
package com.ibm.cmr.create.batch.util.masscreate.handler.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.MassCreate;
import com.ibm.cio.cmr.request.entity.MassCreateAddr;
import com.ibm.cio.cmr.request.entity.listeners.ChangeLogListener;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.util.SystemUtil;
import com.ibm.cio.cmr.request.util.masscreate.MassCreateFileRow;
import com.ibm.cmr.create.batch.model.CmrServiceInput;
import com.ibm.cmr.create.batch.util.DebugUtil;
import com.ibm.cmr.create.batch.util.masscreate.handler.HandlerEngine;
import com.ibm.cmr.services.client.CmrServicesFactory;
import com.ibm.cmr.services.client.ProcessClient;
import com.ibm.cmr.services.client.ServiceClient;
import com.ibm.cmr.services.client.process.ProcessRequest;
import com.ibm.cmr.services.client.process.ProcessResponse;
import com.ibm.cmr.services.client.process.RDcRecord;

/**
 * @author JeffZAMORA
 *
 */
public class MassCreateWorker implements Runnable {

  private static final Logger LOG = Logger.getLogger(MassCreateWorker.class);
  private static final int MAX_CELL_CONTENTS = 1000;
  private static final String BATCH_SERVICES_URL = SystemConfiguration.getValue("BATCH_SERVICES_URL");

  private HandlerEngine engine;
  private MassCreateFileRow row;
  private EntityManager entityManager;
  private List<String> errors = new ArrayList<String>();

  private MassCreate record;
  private long reqId;
  private CmrServiceInput input;
  private long iterationId;
  private Map<String, List<String>> cmrNoSapNoMap;
  private List<String> resultCodes = new ArrayList<String>();

  private boolean error;
  private String errorMsg;

  private String mode;

  public MassCreateWorker(EntityManager entityManager, HandlerEngine engine, MassCreateFileRow row, long reqId) {
    this.entityManager = entityManager;
    this.engine = engine;
    this.row = row;
    this.reqId = reqId;
  }

  public MassCreateWorker(EntityManager entityManager, MassCreate record, CmrServiceInput input, long iterationId,
      Map<String, List<String>> cmrNoSapNoMap) {
    this.entityManager = entityManager;
    this.record = record;
    this.input = input;
    this.iterationId = iterationId;
    this.cmrNoSapNoMap = cmrNoSapNoMap;
    this.mode = "C";
  }

  @Override
  public void run() {
    ChangeLogListener.setUser(SystemConfiguration.getValue("BATCH_USERID"));
    ChangeLogListener.setManager(this.entityManager);
    SystemUtil.setManager(this.entityManager);
    if (StringUtils.isBlank(this.mode) || "V".equals(this.mode)) {
      validateRow();
    } else if ("C".equals(this.mode)) {
      processMassCreate();
    }
  }

  private void validateRow() {
    try {
      LOG.debug("Validating row Request " + this.reqId + " Row Number " + this.row.getSeqNo());
      StringBuilder errorMsg = new StringBuilder();

      this.errors = this.engine.validateRow(this.entityManager, this.row);
      if (this.errors.size() > 0) {
        errorMsg.delete(0, errorMsg.length());
        for (String error : this.errors) {
          errorMsg.append(errorMsg.length() > 0 ? "\n" : "");
          errorMsg.append(error);
        }
        if (errorMsg.length() > MAX_CELL_CONTENTS) {
          // limit to 200 so as not to exceed excel's limit
          errorMsg.delete(MAX_CELL_CONTENTS, errorMsg.length());
          errorMsg.append("\nToo many errors.");
        }
        this.row.setErrorMessage(errorMsg.toString());
      } else {
        // if no errrors, call transformation
        this.engine.transform(entityManager, row);
      }

    } catch (Exception e) {
      LOG.warn("Error encountered during validations.", e);
      this.error = true;
      this.errorMsg = e.getMessage();
    }
  }

  private void processMassCreate() {

    try {
      ServiceClient serviceClient = CmrServicesFactory.getInstance().createClient(BATCH_SERVICES_URL, ProcessClient.class);

      ProcessRequest request = new ProcessRequest();
      request.setCmrNo(this.record.getCmrNo());
      request.setMandt(this.input.getInputMandt());
      request.setReqId(this.input.getInputReqId());
      request.setReqType(CmrConstants.REQ_TYPE_CREATE);
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

      if (!StringUtils.isBlank(resultCode) && !resultCodes.contains(resultCode)) {
        resultCodes.add(resultCode);
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
          PreparedQuery createAddrQry = new PreparedQuery(this.entityManager, ExternalizedQuery.getSql("BATCH.GET_MASS_ADDR_ENTITY_CREATE_REQ"));
          createAddrQry.setParameter("REQ_ID", this.record.getId().getParReqId());
          createAddrQry.setParameter("ITERATION_ID", this.iterationId);
          createAddrQry.setParameter("ADDR_TYPE", "ZS01".equals(record.getAddressType()) || "ZI01".equals(record.getAddressType()) ? "ZS01" : "ZI01");
          createAddrQry.setParameter("SEQ_NO", this.record.getId().getSeqNo());
          List<MassCreateAddr> cretAddrList = createAddrQry.getResults(MassCreateAddr.class);
          for (MassCreateAddr cretAddrEntity : cretAddrList) {
            cretAddrEntity.setSapNo(record.getSapNo());
            this.entityManager.merge(cretAddrEntity);
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
      this.entityManager.merge(massCretEntity);
      // }

      // this.entityManager.flush();

    } catch (Exception e) {
      LOG.error("Error in processing Mass Create and Mass Create Addr Updates for Create Mass Request " + this.record.getId().getParReqId() + " Seq "
          + this.record.getId().getSeqNo() + " [" + e.getMessage() + "]", e);
      this.error = true;
      this.errorMsg = e.getMessage();
    }

  }

  public boolean isError() {
    return error;
  }

  public String getErrorMsg() {
    return errorMsg;
  }

  public List<String> getErrors() {
    return errors;
  }

  public String getMode() {
    return mode;
  }

  public void setMode(String mode) {
    this.mode = mode;
  }

  public List<String> getResultCodes() {
    return resultCodes;
  }

}
