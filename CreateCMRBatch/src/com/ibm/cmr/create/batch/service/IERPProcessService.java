package com.ibm.cmr.create.batch.service;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;
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
import com.ibm.cio.cmr.request.entity.ReqCmtLog;
import com.ibm.cio.cmr.request.entity.ReqCmtLogPK;
import com.ibm.cio.cmr.request.entity.SuppCntry;
import com.ibm.cio.cmr.request.entity.WfHist;
import com.ibm.cio.cmr.request.entity.listeners.ChangeLogListener;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.util.IERPRequestUtils;
import com.ibm.cio.cmr.request.util.RequestUtils;
import com.ibm.cio.cmr.request.util.SystemUtil;
import com.ibm.cio.cmr.request.util.geo.impl.CNHandler;
import com.ibm.cio.cmr.request.util.geo.impl.DEHandler;
import com.ibm.cmr.create.batch.model.CmrServiceInput;
import com.ibm.cmr.create.batch.util.DebugUtil;
import com.ibm.cmr.services.client.CmrServicesFactory;
import com.ibm.cmr.services.client.ProcessClient;
import com.ibm.cmr.services.client.process.ProcessRequest;
import com.ibm.cmr.services.client.process.ProcessResponse;
import com.ibm.cmr.services.client.process.RDcRecord;

public class IERPProcessService extends BaseBatchService {

  private static final String BATCH_SERVICES_URL = SystemConfiguration.getValue("BATCH_SERVICES_URL");
  private ProcessClient serviceClient;
  private static final String COMMENT_LOGGER = "IERP Process Service";

  @Override
  protected Boolean executeBatch(EntityManager entityManager) throws Exception {
    try {

      ChangeLogListener.setUser(COMMENT_LOGGER);
      initClient();

      monitorCreqcmr(entityManager);

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

  @SuppressWarnings("unchecked")
  public void monitorCreqcmr(EntityManager em) throws JsonGenerationException, JsonMappingException, IOException, Exception {
    // DTN: 1) Get the list of DR type countries
    String actionRdc = "";
    String sql1 = ExternalizedQuery.getSql("BATCH.GET_DR_COUNTRIES");
    PreparedQuery query = new PreparedQuery(em, sql1);
    List<SuppCntry> drList = query.getResults(SuppCntry.class);
    LOG.debug(
        "Executing batch code for R" + SystemConfiguration.getSystemProperty("RELEASE") + ".b" + SystemConfiguration.getSystemProperty("BUILD"));
    LOG.debug("Size of DR list : " + drList.size());

    // DTN: 2) Get the requests
    String isngCntryCds = convertToIssuingCntryCSV(drList);
    String sql2 = ExternalizedQuery.getSql("BATCH.GET_DR_PROC_PENDING");
    String mapping = Admin.BATCH_SERVICE_MAPPING;
    StringBuffer siteIds = new StringBuffer();
    sql2 = sql2.replace("<CMR_ISSUING_CNTRY>", isngCntryCds);
    query = new PreparedQuery(em, sql2);
    List<CompoundEntity> reqIdList = query.getCompundResults(Admin.class, Admin.BATCH_MASS_CREATE_GET_RECORD);
    LOG.debug("Size of REQ_ID list : " + reqIdList.size());

    if (reqIdList != null && !reqIdList.isEmpty()) {
      // lock records for processing
      lockAdminRecordsForProcessing(reqIdList, em);

      // start processing
      for (CompoundEntity entity : reqIdList) {
        Admin admin = entity.getEntity(Admin.class);
        Data data = entity.getEntity(Data.class);
        // create a entry in request's comment log re_cmt_log table
        createCommentLog(em, admin, "RDc processing has started. Waiting for completion.");
        CmrServiceInput cmrServiceInput = getReqParam(em, admin.getId().getReqId(), admin.getReqType(), data);

        ProcessResponse response = null;
        String overallStatus = null;

        boolean prospectConversion = CmrConstants.YES_NO.Y.equals(admin.getProspLegalInd()) ? true : false;

        if (CmrConstants.REQ_TYPE_UPDATE.equals(cmrServiceInput.getInputReqType())) {
          actionRdc = "System Action:RDc Update";
          StringBuffer statusMessage = new StringBuffer();
          List<ProcessResponse> responses = null;
          HashMap<String, Object> overallResponse = processUpdateRequest(admin, data, cmrServiceInput, em);
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

                if (CmrConstants.ADDR_TYPE.ZS01.equals(response.getRecords().get(index).getAddressType())) {
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
                      addr.setPairedAddrSeq(addrSeqs[0]);
                      addr.setSapNo(red.getSapNo());
                      addr.setIerpSitePrtyId(red.getIerpSitePartyId());
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
      }
    }
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
      String siteIds, EntityManager em) {
    ProcessResponse response = null;
    request.setSapNo(addr.getSapNo());
    request.setAddrType(addr.getId().getAddrType());
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

    try {
      // 1. Get first the ones that are new -- BATCH.GET_NEW_ADDR_FOR_UPDATE
      String sql = ExternalizedQuery.getSql("BATCH.GET_NEW_ADDR_FOR_UPDATE");
      PreparedQuery query = new PreparedQuery(em, sql);
      query.setParameter("REQ_ID", admin.getId().getReqId());
      List<Addr> addresses = query.getResults(Addr.class);

      if (addresses != null && addresses.size() > 0) {
        for (Addr addr : addresses) {
          response = sendAddrForProcessing(addr, request, responses, isIndexNotUpdated, siteIds, em);
          respStatuses.add(response.getStatus());
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
            response = sendAddrForProcessing(addr, request, responses, isIndexNotUpdated, siteIds, em);
            respStatuses.add(response.getStatus());
          } else {
            notProcessed.add(addr);
          }
        }
      }

      // 3. Check if there are customer and IBM changes, propagate to other
      // addresses
      DataRdc dataRdc = getDataRdcRecords(em, data);
      boolean isDataUpdated = false;

      if (CNHandler.isCNIssuingCountry(data.getCmrIssuingCntry())) {
        isDataUpdated = CNHandler.isDataUpdated(data, dataRdc, data.getCmrIssuingCntry());
      } else {
        isDataUpdated = DEHandler.isDataUpdated(data, dataRdc, data.getCmrIssuingCntry());
      }

      if (isDataUpdated && (notProcessed != null && notProcessed.size() > 0)) {
        LOG.debug("Processing CMR Data changes to " + notProcessed.size() + " addresses of CMR# " + data.getCmrNo());
        for (Addr addr : notProcessed) {
          response = sendAddrForProcessing(addr, request, responses, isIndexNotUpdated, siteIds, em);
          respStatuses.add(response.getStatus());
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

  public CmrServiceInput getReqParam(EntityManager em, long reqId, String reqType, Data data) {
    String cmrNo = "";

    if (!StringUtils.isEmpty(reqType)) {
      // if (CmrConstants.REQ_TYPE_CREATE.equals(reqType)) {
      // cmrNo = "TEMP";
      // } else if (CmrConstants.REQ_TYPE_UPDATE.equals(reqType)) {
      // cmrNo = data.getCmrNo();
      // }
      cmrNo = data.getCmrNo();
    }

    String requestType = ((reqType) != null && (reqType).trim().length() > 0) ? (reqType) : "";
    long requestId = reqId;
    CmrServiceInput cmrServiceInput = new CmrServiceInput();

    cmrServiceInput.setInputMandt(SystemConfiguration.getValue("MANDT"));
    cmrServiceInput.setInputReqId(requestId);
    cmrServiceInput.setInputReqType(requestType);
    cmrServiceInput.setInputCmrNo(cmrNo);
    cmrServiceInput.setInputUserId(SystemConfiguration.getValue("BATCH_USERID"));

    return cmrServiceInput;
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
    reqCmtLog.setCreateById(admin.getLastUpdtBy());
    reqCmtLog.setCreateByNm(COMMENT_LOGGER);
    // set createTs as current timestamp and updateTs same as CreateTs
    reqCmtLog.setCreateTs(SystemUtil.getCurrentTimestamp());
    reqCmtLog.setUpdateTs(reqCmtLog.getCreateTs());
    createEntity(reqCmtLog, em);
    partialCommit(em);
  }

  private void initClient() throws Exception {
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
}
