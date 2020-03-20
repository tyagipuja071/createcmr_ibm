package com.ibm.cmr.create.batch.service;

import java.io.IOException;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;

import com.ibm.cio.cmr.create.entity.NotifyReq;
import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.CompoundEntity;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.entity.DataRdc;
import com.ibm.cio.cmr.request.entity.MassUpdt;
import com.ibm.cio.cmr.request.entity.MassUpdtAddr;
import com.ibm.cio.cmr.request.entity.MassUpdtData;
import com.ibm.cio.cmr.request.entity.ReqCmtLog;
import com.ibm.cio.cmr.request.entity.ReqCmtLogPK;
import com.ibm.cio.cmr.request.entity.WfHist;
import com.ibm.cio.cmr.request.entity.WfHistPK;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.BaseService;
import com.ibm.cio.cmr.request.user.AppUser;
import com.ibm.cio.cmr.request.util.RequestUtils;
import com.ibm.cio.cmr.request.util.SystemUtil;
import com.ibm.cmr.create.batch.model.CmrServiceInput;
import com.ibm.cmr.create.batch.model.MassUpdateServiceInput;
import com.ibm.cmr.create.batch.model.NotifyReqModel;
import com.ibm.cmr.create.batch.util.BatchUtil;
import com.ibm.cmr.create.batch.util.DebugUtil;
import com.ibm.cmr.services.client.CmrServicesFactory;
import com.ibm.cmr.services.client.MassProcessClient;
import com.ibm.cmr.services.client.MassServicesFactory;
import com.ibm.cmr.services.client.ProcessClient;
import com.ibm.cmr.services.client.UpdateByEntClient;
import com.ibm.cmr.services.client.process.ProcessRequest;
import com.ibm.cmr.services.client.process.ProcessResponse;
import com.ibm.cmr.services.client.process.RDcRecord;
import com.ibm.cmr.services.client.process.ent.EnterpriseUpdtRequest;
import com.ibm.cmr.services.client.process.ent.EnterpriseUpdtResponse;
import com.ibm.cmr.services.client.process.mass.MassProcessRequest;
import com.ibm.cmr.services.client.process.mass.MassProcessResponse;
import com.ibm.cmr.services.client.process.mass.MassUpdateRecord;
import com.ibm.cmr.services.client.process.mass.RequestValueRecord;
import com.ibm.cmr.services.client.process.mass.ResponseRecord;

/**
 * @author Rangoli Saxena
 * @deprecated Use {@link TransConnService} for new implementation
 * 
 */
@Deprecated
public class NotifyReqService extends BaseService<NotifyReqModel, NotifyReq> {

  private static final String BATCH_SERVICES_URL = SystemConfiguration.getValue("BATCH_SERVICES_URL");
  private ProcessClient serviceClient;
  private MassProcessClient massServiceClient;
  private static final String FORCED_CHANGE_ACTION = "System Action: Forced Changes.";

  @Override
  protected Logger initLogger() {
    return Logger.getLogger(NotifyReqService.class);
  }

  @Override
  protected void performTransaction(NotifyReqModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    try {
      initClient();

      this.log.info("Processing Aborted records (retry)...");
      monitorAbortedRecords(entityManager);

      this.log.info("Processing TransConn records...");
      monitorTransconn(entityManager);

      this.log.info("Processing Completed Manual records...");
      monitorDisAutoProcRec(entityManager);

      // this.log.info("Processing Rdc Tagged records ...");
      // monitorRDCTaggedRecords(entityManager);

    } catch (Exception e) {
      throw new CmrException(e);
    }
  }

  @Override
  protected List<NotifyReqModel> doSearch(NotifyReqModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    List<NotifyReqModel> results = new ArrayList<NotifyReqModel>();
    return results;
  }

  @Override
  protected NotifyReq getCurrentRecord(NotifyReqModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    return null;
  }

  @Override
  protected NotifyReq createFromModel(NotifyReqModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    return null;
  }

  /**
   * Monitor records from the TRANSCONN.NOTIFY_REQ
   * 
   * @param entityManager
   * @throws JsonGenerationException
   * @throws JsonMappingException
   * @throws IOException
   * @throws Exception
   */
  public void monitorTransconn(EntityManager em) throws JsonGenerationException, JsonMappingException, IOException, Exception {

    // search the notify requests from transconn
    String sql = ExternalizedQuery.getSql("BATCH.MONITOR_TRANSCONN");
    PreparedQuery query = new PreparedQuery(em, sql);
    query.setParameter("NOTIFIED_IND", CmrConstants.NOTIFY_IND_YES);

    // String sql = ExternalizedQuery.getSql("BATCH.MONITOR_TRANSCONN_commted");
    // PreparedQuery query = new PreparedQuery(em, sql);

    List<NotifyReq> notifyList = query.getResults(NotifyReq.class);
    this.log.debug("Size of notify list : " + notifyList.size());

    for (NotifyReq notifyReq : notifyList) {
      try {
        log.info("Processing Notify Req " + notifyReq.getId().getNotifyId() + " [Request ID: " + notifyReq.getReqId() + "]");
        NotifyReqModel notifyReqModel = new NotifyReqModel();
        copyValuesFromEntity(notifyReq, notifyReqModel);

        // create a entry in request's comment log re_cmt_log table
        if (null != notifyReq.getCmtLogMsg() && !notifyReq.getCmtLogMsg().isEmpty()) {
          createCommentLog(this, em, notifyReqModel);
        }
        // update the NOTIFIED_IND of NOTIFY_REQ Table for each record processed
        notifyReq.setNotifiedInd(CmrConstants.NOTIFY_IND_YES);
        updateEntity(notifyReq, em);

        // send mail and get the input params for create cmr service
        String sqlMail = "BATCH.GET_MAIL_CONTENTS";
        String mapping = Admin.BATCH_SERVICE_MAPPING;
        PreparedQuery queryMail = new PreparedQuery(em, ExternalizedQuery.getSql(sqlMail));
        queryMail.setParameter("REQ_ID", notifyReq.getReqId());
        queryMail.setParameter("REQ_STATUS", notifyReq.getReqStatus());
        queryMail.setParameter("CHANGED_BY_ID", notifyReq.getChangedById());
        List<CompoundEntity> rs = queryMail.getCompundResults(1, Admin.class, mapping);
        Admin admin = null;
        Data data = null;
        WfHist wfHist = null;
        for (CompoundEntity entity : rs) {
          admin = entity.getEntity(Admin.class);
          data = entity.getEntity(Data.class);
          wfHist = entity.getEntity(WfHist.class);
        }
        RequestUtils.sendEmailNotifications(em, admin, wfHist);
        partialCommit(em);
        CmrServiceInput cmrServiceInput = getReqParam(em, notifyReqModel.getReqId(), admin.getReqType(), data.getCmrNo());
        cmrServiceInput.setCmrIssuingCntry(data.getCmrIssuingCntry());

        boolean isMassRecord = false;
        // boolean isMassRdcTaggedOnly = false;
        boolean isReactivateRecord = false;
        boolean isDeleteRecord = false;
        boolean isEnterpriseRecord = false;

        if (null != admin.getReqType() && !"".equalsIgnoreCase(admin.getReqType())
            && CmrConstants.REQ_TYPE_MASS_UPDATE.equalsIgnoreCase(admin.getReqType())) {
          isMassRecord = true;
          /*
           * if (null != admin.getMassUpdtRdcOnly() &&
           * "Y".equalsIgnoreCase(admin.getMassUpdtRdcOnly())) {
           * isMassRdcTaggedOnly = true; }
           */
        } else if (null != admin.getReqType() && !"".equalsIgnoreCase(admin.getReqType())
            && CmrConstants.REQ_TYPE_REACTIVATE.equalsIgnoreCase(admin.getReqType())) {
          isReactivateRecord = true;
        } else if (null != admin.getReqType() && !"".equalsIgnoreCase(admin.getReqType())
            && CmrConstants.REQ_TYPE_DELETE.equalsIgnoreCase(admin.getReqType())) {
          isDeleteRecord = true;
        } else if (null != admin.getReqType() && !"".equalsIgnoreCase(admin.getReqType())
            && CmrConstants.REQ_TYPE_UPDT_BY_ENT.equalsIgnoreCase(admin.getReqType())) {
          isEnterpriseRecord = true;
        }

        String batchAction = "MonitorTransconn";
        // if (!isMassRdcTaggedOnly) {
        if (isMassRecord || isReactivateRecord || isDeleteRecord) {
          MassUpdateServiceInput mssUpdateServiceInput = getMassReqParam(em, notifyReqModel.getReqId(), admin.getReqType());
          mssUpdateServiceInput.setCmrIssuingCntry(data.getCmrIssuingCntry());
          processMassUpdateService(em, notifyReq.getReqId(), admin.getIterationId(), data.getCmrIssuingCntry(), mssUpdateServiceInput, batchAction,
              admin.getRdcProcessingStatus());
        } else if (isEnterpriseRecord) {
          processingUpdateByEnterprise(em, notifyReq.getReqId(), admin.getIterationId(), data.getCmrIssuingCntry(), batchAction,
              admin.getRdcProcessingStatus());
        } else {
          // connect to the create cmr service for the processed records
          if (notifyReq.getReqStatus() != null && !"".equalsIgnoreCase(notifyReq.getReqStatus())
              && notifyReq.getReqStatus().equalsIgnoreCase("COM")) {
            String result = processCreateCMRService(em, notifyReq.getReqId(), cmrServiceInput, batchAction,
                admin.getRdcProcessingStatus() == null ? "" : admin.getRdcProcessingStatus());

            this.log.debug("Result from Create Service: '" + result + "'");
            // changes to force the type to be update and send back to tc
            if ((CmrConstants.RDC_STATUS_COMPLETED.equals(result) || CmrConstants.RDC_STATUS_COMPLETED_WITH_WARNINGS.equals(result))
                && CmrConstants.REQ_TYPE_CREATE.equals(admin.getReqType()) && "COM".equals(notifyReq.getReqStatus())) {
              forceChangeToUpdate(em, notifyReq.getReqId());
            } else if (CmrConstants.RDC_STATUS_NOT_COMPLETED.equals(result) && CmrConstants.REQ_TYPE_CREATE.equals(admin.getReqType())) {
              // if this is not completed, try still to generate an auto-update
              forceChangeToUpdate(em, notifyReq.getReqId());
            } else if ((CmrConstants.RDC_STATUS_COMPLETED.equals(result) || CmrConstants.RDC_STATUS_COMPLETED_WITH_WARNINGS.equals(result))
                && CmrConstants.REQ_TYPE_UPDATE.equals(admin.getReqType()) && "COM".equals(notifyReq.getReqStatus())) {
              processRevertToCreate(em, notifyReq.getReqId());
            } else if ((CmrConstants.RDC_STATUS_NOT_COMPLETED.equals(result)) && CmrConstants.REQ_TYPE_UPDATE.equals(admin.getReqType())
                && "COM".equals(admin.getReqStatus())) {
              processRevertToCreate(em, notifyReq.getReqId());
            }
          }
        }
        // }
        partialCommit(em);

      } catch (Exception e) {
        this.log.error("Error in processing TransConn Record " + notifyReq.getId().getNotifyId() + " for Request ID " + notifyReq.getReqId(), e);
      }
    }
  }

  private void forceChangeToUpdate(EntityManager em, long reqId) throws CmrException, SQLException {
    this.log.info("Checking type of completed request...");
    String sql = ExternalizedQuery.getSql("BATCH.GET_FORCE_DATA");
    PreparedQuery q = new PreparedQuery(em, sql);
    q.setParameter("REQ_ID", reqId);
    List<CompoundEntity> result = q.getCompundResults(Admin.class, Admin.BATCH_FORCE_UPDATE_MAPPING);
    if (result != null && result.size() > 0) {
      CompoundEntity record = result.get(0);
      Admin admin = record.getEntity(Admin.class);
      Data data = record.getEntity(Data.class);

      String user = SystemConfiguration.getValue("BATCH_USERID");
      AppUser dummyuser = new AppUser();
      dummyuser.setIntranetId(user);
      dummyuser.setBluePagesName(user);
      if (!StringUtils.isBlank(data.getAffiliate()) || !StringUtils.isBlank(data.getEnterprise())) {

        boolean forceUpdate = false;
        if (StringUtils.isBlank(admin.getModelCmrNo())) {
          // automatic updates
          forceUpdate = true;
          log.debug("No Model CMR with Enterprise/Affiliate, forcing update.");
        } else {
          sql = ExternalizedQuery.getSql("BATCH.CHECK_FORCE_UPDATE.RDC");
          q = new PreparedQuery(em, sql);
          q.setParameter("REQ_ID", admin.getId().getReqId());
          DataRdc rdc = q.getSingleResult(DataRdc.class);
          String oldAffiliate = rdc.getAffiliate() != null ? rdc.getAffiliate() : "";
          String oldEnterprise = rdc.getEnterprise() != null ? rdc.getEnterprise() : "";
          String enterprise = data.getEnterprise() != null ? data.getEnterprise() : "";
          String affiliate = data.getAffiliate() != null ? data.getAffiliate() : "";

          if (!StringUtils.equals(oldEnterprise, enterprise) || !StringUtils.equals(oldAffiliate, affiliate)) {
            log.debug("Modeled CMR No changed Enterprise/Affiliate, forcing update.");
            forceUpdate = true;
          }
        }

        if (forceUpdate) {
          this.log.info("Required updated needed for Request " + reqId + " - Force to Update");

          sql = ExternalizedQuery.getSql("BATCH.FORCE_SQL");
          q = new PreparedQuery(em, sql);
          q.setParameter("REQ_ID", reqId);
          q.executeSql();

          em.detach(admin);

          admin.setReqStatus("PCP");
          String cmt = "System Action:  Status changed to 'Processing Create/Updt Pending', Request Type changed to 'Update' for further automatic processing";
          RequestUtils.createWorkflowHistory(this, em, user, admin, cmt, FORCED_CHANGE_ACTION, null, null, false, null, null);

          RequestUtils.createCommentLog(this, em, dummyuser, reqId, cmt);
        }
      }
    }
    this.log.info("Forcing Request " + reqId + " for Update");
  }

  /**
   * Reverts the request to a 'Create' if it detects that the system forced the
   * update
   * 
   * @param em
   * @param reqId
   * @throws CmrException
   * @throws SQLException
   */
  private void processRevertToCreate(EntityManager em, long reqId) throws CmrException, SQLException {
    String sql = ExternalizedQuery.getSql("BATCH.CHECK_FORCED_UPDATE");
    PreparedQuery q = new PreparedQuery(em, sql);
    q.setParameter("REQ_ID", reqId);
    q.setParameter("ACT", FORCED_CHANGE_ACTION);
    List<Admin> adminList = q.getResults(1, Admin.class);
    if (adminList != null && adminList.size() > 0) {
      this.log.info("Reverting Request " + reqId + " to Create");
      Admin admin = adminList.get(0);
      String user = SystemConfiguration.getValue("BATCH_USERID");
      AppUser dummyuser = new AppUser();
      dummyuser.setIntranetId(user);
      dummyuser.setBluePagesName(user);
      String cmt = "System Action:  Request Type reverted to 'Create' (original request type)";
      admin.setReqType(CmrConstants.REQ_TYPE_CREATE);
      admin.setInternalTyp("CREATE_AUTO");
      updateEntity(admin, em);
      RequestUtils.createCommentLog(this, em, dummyuser, reqId, cmt);
    }
    this.log.info("Forcing Request " + reqId + " for Update");
  }

  /**
   * Get values of the input parameters for the createCMRService Request
   * 
   * @param entityManager
   * @param reqId
   * @param reqType
   * @param cmrno
   */
  public static CmrServiceInput getReqParam(EntityManager em, long reqId, String reqType, String cmrno) {
    String cmrNo = ((cmrno) != null && (cmrno).trim().length() > 0) ? (cmrno) : "";
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

  /**
   * Calls the createCMRService and process the response returned
   * 
   * @param entityManager
   * @param reqId
   * @param cmrServiceInput
   * @param batchAction
   * @param currentRDCProcStat
   * @throws JsonGenerationException
   * @throws JsonMappingException
   * @throws IOException
   * @throws Exception
   */
  public String processCreateCMRService(EntityManager em, long reqId, CmrServiceInput cmrServiceInput, String batchAction, String currentRDCProcStat)
      throws JsonGenerationException, JsonMappingException, IOException, Exception {

    // get the params for input to the create cmr service
    DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    ProcessRequest request = new ProcessRequest();
    request.setCmrNo(cmrServiceInput.getInputCmrNo());
    request.setMandt(cmrServiceInput.getInputMandt());
    request.setReqId(cmrServiceInput.getInputReqId());
    request.setReqType(cmrServiceInput.getInputReqType());
    request.setUserId(cmrServiceInput.getInputUserId());

    String resultCode = "";
    // FOR Update Request Type
    if (cmrServiceInput.getInputReqType() != null && !"".equalsIgnoreCase(cmrServiceInput.getInputReqType())
        && CmrConstants.REQ_TYPE_UPDATE.equalsIgnoreCase(cmrServiceInput.getInputReqType())) {

      // code to retrieve the sap no list for a req id so that sap no can
      // be
      // passed as input
      String sql = ExternalizedQuery.getSql("BATCH.GET_SAP_NO");
      PreparedQuery query = new PreparedQuery(em, sql);
      query.setParameter("REQ_ID", reqId);
      List<Object[]> sapNos = query.getResults();
      // for each sap no in list for update requests
      for (Object[] result : sapNos) {

        request.setSapNo((String) result[0]);

        // call the create cmr service
        this.log.info("Sending request to Process Service [Request ID: " + request.getReqId() + " CMR No: " + request.getCmrNo() + " Type: "
            + request.getReqType() + " SAP No: " + request.getSapNo() + "]");

        this.log.trace("Request JSON:");
        if (this.log.isTraceEnabled()) {
          DebugUtil.printObjectAsJson(this.log, request);
        }
        ProcessResponse response = null;
        String applicationId = BatchUtil.getAppId(cmrServiceInput.getCmrIssuingCntry());
        if (applicationId == null) {
          this.log.debug("No Application ID mapped to " + cmrServiceInput.getCmrIssuingCntry());
          response = new ProcessResponse();
          response.setReqId(request.getReqId());
          response.setCmrNo(request.getCmrNo());
          response.setMandt(request.getMandt());
          response.setStatus(CmrConstants.RDC_STATUS_NOT_COMPLETED);
          response.setMessage("No application ID defined for Country: " + cmrServiceInput.getCmrIssuingCntry() + ". Cannot process RDc records.");
        } else {
          try {
            this.log.debug("Application ID: " + applicationId);
            this.serviceClient.setReadTimeout(60 * 3 * 1000); // 3 mins
            response = this.serviceClient.executeAndWrap(applicationId, request, ProcessResponse.class);
          } catch (Exception e) {
            this.log.error("Error when connecting to the service.", e);
            response = new ProcessResponse();
            response.setReqId(request.getReqId());
            response.setCmrNo(request.getCmrNo());
            response.setMandt(request.getMandt());
            response.setStatus(CmrConstants.RDC_STATUS_ABORTED);
            response.setMessage("Cannot connect to the service at the moment.");
          }
          resultCode = response.getStatus();
          this.log.trace("Response JSON:");
          if (this.log.isTraceEnabled()) {
            DebugUtil.printObjectAsJson(this.log, response);
          }

          this.log.info("Response received from Process Service [Request ID: " + response.getReqId() + " CMR No: " + response.getCmrNo() + " Status: "
              + response.getStatus() + " Message: " + (response.getMessage() != null ? response.getMessage() : "-") + "]");

        }
        // get the results from the service and process jason response
        try {
          String rdcUpdateDateStr = "";
          String currentTsStr = formatter.format(SystemUtil.getCurrentTimestamp());
          Date currentTs = formatter.parse(currentTsStr);

          // update the Admin table for all the update responses

          PreparedQuery updAdminQry = new PreparedQuery(em, ExternalizedQuery.getSql("BATCH.GET_ADMIN_ENTITY"));
          updAdminQry.setParameter("REQ_ID", reqId);
          List<Admin> adminList = updAdminQry.getResults(Admin.class);
          Admin adminEntityWf = new Admin();
          for (Admin adminEntity : adminList) {
            if (batchAction.equalsIgnoreCase("MonitorAbortedRecords") && currentRDCProcStat.equalsIgnoreCase(CmrConstants.RDC_STATUS_ABORTED)
                && response.getStatus().equalsIgnoreCase(CmrConstants.RDC_STATUS_ABORTED)) {
              adminEntity.setRdcProcessingStatus(CmrConstants.RDC_STATUS_NOT_COMPLETED);
            } else {
              adminEntity.setRdcProcessingStatus(response.getStatus());
            }
            adminEntity.setRdcProcessingMsg(response.getMessage());
            adminEntity.setRdcProcessingTs(currentTs);
            updateEntity(adminEntity, em);
            adminEntityWf = adminEntity;
          }

          if (CmrConstants.RDC_STATUS_COMPLETED.equalsIgnoreCase(response.getStatus())
              || CmrConstants.RDC_STATUS_COMPLETED_WITH_WARNINGS.equalsIgnoreCase(response.getStatus())) {

            // get the update date from the RDC Records returned from the
            // service
            for (RDcRecord record : response.getRecords()) {

              rdcUpdateDateStr = record.getUpdateDate();
              Date updateTs = formatter.parse(rdcUpdateDateStr);
              // update the ADDR Table for the update requests

              PreparedQuery updAddrQry = new PreparedQuery(em, ExternalizedQuery.getSql("BATCH.GET_ADDR_ENTITY_UPDATE_REQ"));
              updAddrQry.setParameter("REQ_ID", reqId);
              updAddrQry.setParameter("BATCH_SAP_NO", result[0]);
              updAddrQry.setParameter("ADDR_TYPE", result[1]);
              updAddrQry.setParameter("ADDR_SEQ", result[2]);

              List<Addr> addrList = updAddrQry.getResults(Addr.class);
              for (Addr addrEntity : addrList) {
                addrEntity.setRdcLastUpdtDt(updateTs);
                updateEntity(addrEntity, em);
              }

            }

          }

          // create comment log and workflow entries for update type of request
          String actionWf = "System Action:RDc Update";
          if (CmrConstants.RDC_STATUS_COMPLETED.equalsIgnoreCase(response.getStatus())) {
            StringBuilder comment = new StringBuilder();
            comment = comment.append("RDc processing successfully updated KUNNR(s): ");
            if (response.getRecords() != null && response.getRecords().size() != 0) {
              for (int i = 0; i < response.getRecords().size(); i++) {
                comment = comment.append(response.getRecords().get(i).getSapNo() + " ");
              }
            }

            createCommentLogAfterProcess(this, em, SystemConfiguration.getValue("BATCH_USERID"), reqId, comment.toString());
            createWfHistory(this, em, reqId, adminEntityWf.getReqStatus(), SystemConfiguration.getValue("BATCH_USERID"), comment.toString(),
                actionWf);
          } else if (CmrConstants.RDC_STATUS_COMPLETED_WITH_WARNINGS.equalsIgnoreCase(response.getStatus())) {

            StringBuilder comment = new StringBuilder();
            comment = comment.append("RDc processing successfully updated KUNNR(s): ");
            if (response.getRecords() != null && response.getRecords().size() != 0) {
              for (int i = 0; i < response.getRecords().size(); i++) {
                comment = comment.append(response.getRecords().get(i).getSapNo() + " ");
              }
            }

            comment = comment.append(" Warning Message: " + response.getMessage());
            createCommentLogAfterProcess(this, em, SystemConfiguration.getValue("BATCH_USERID"), reqId, comment.toString());
            createWfHistory(this, em, reqId, adminEntityWf.getReqStatus(), SystemConfiguration.getValue("BATCH_USERID"), comment.toString(),
                actionWf);

          } else if (batchAction.equalsIgnoreCase("MonitorAbortedRecords") && currentRDCProcStat.equalsIgnoreCase(CmrConstants.RDC_STATUS_ABORTED)
              && response.getStatus().equalsIgnoreCase(CmrConstants.RDC_STATUS_ABORTED)) {
            StringBuilder comment = new StringBuilder();
            comment = comment.append("RDc update processing for KUNNR " + request.getSapNo() + " failed. Error: " + response.getMessage());
            createCommentLogAfterProcess(this, em, SystemConfiguration.getValue("BATCH_USERID"), reqId, comment.toString());
            createWfHistory(this, em, reqId, adminEntityWf.getReqStatus(), SystemConfiguration.getValue("BATCH_USERID"), comment.toString(),
                actionWf);
          } else if (CmrConstants.RDC_STATUS_ABORTED.equalsIgnoreCase(response.getStatus())) {
            StringBuilder comment = new StringBuilder();
            comment = comment.append("RDc update processing for KUNNR " + request.getSapNo() + " failed. Error: " + response.getMessage()
                + " System will retry processing once.");
            createCommentLogAfterProcess(this, em, SystemConfiguration.getValue("BATCH_USERID"), reqId, comment.toString());
            createWfHistory(this, em, reqId, adminEntityWf.getReqStatus(), SystemConfiguration.getValue("BATCH_USERID"), comment.toString(),
                actionWf);
          } else if (CmrConstants.RDC_STATUS_NOT_COMPLETED.equalsIgnoreCase(response.getStatus())) {
            StringBuilder comment = new StringBuilder();
            comment = comment.append("RDc update processing for KUNNR " + request.getSapNo() + " failed. Error: " + response.getMessage());
            createCommentLogAfterProcess(this, em, SystemConfiguration.getValue("BATCH_USERID"), reqId, comment.toString());
            createWfHistory(this, em, reqId, adminEntityWf.getReqStatus(), SystemConfiguration.getValue("BATCH_USERID"), comment.toString(),
                actionWf);
          }

          partialCommit(em);

        } catch (Exception e) {
          this.log.error("Error in processing Admin and ADDR Updates for Update Request " + " [" + e.getMessage() + "]");
          throw e;
        }
      }
    } else if (cmrServiceInput.getInputReqType() != null && !"".equalsIgnoreCase(cmrServiceInput.getInputReqType())
        && CmrConstants.REQ_TYPE_CREATE.equalsIgnoreCase(cmrServiceInput.getInputReqType())) {

      convertToProspectToLegalCMRInput(request, em, request.getReqId());

      this.log.info("Sending request to Process Service [Request ID: " + request.getReqId() + " CMR No: " + request.getCmrNo() + " Type: "
          + request.getReqType() + "]");

      // call the create cmr service
      this.log.trace("Request JSON:");
      if (this.log.isTraceEnabled()) {
        DebugUtil.printObjectAsJson(this.log, request);
      }
      ProcessResponse response = null;
      try {
        this.serviceClient.setReadTimeout(60 * 3 * 1000); // 3 mins
        response = this.serviceClient.executeAndWrap(ProcessClient.US_APP_ID, request, ProcessResponse.class);
      } catch (Exception e) {
        this.log.error("Error when connecting to the service.", e);
        response = new ProcessResponse();
        response.setStatus(CmrConstants.RDC_STATUS_ABORTED);
        response.setReqId(request.getReqId());
        response.setCmrNo(request.getCmrNo());
        response.setMandt(request.getMandt());
        response.setMessage("Cannot connect to the service at the moment.");
      }
      resultCode = response.getStatus();
      this.log.trace("Response JSON:");
      if (this.log.isTraceEnabled()) {
        DebugUtil.printObjectAsJson(this.log, response);
      }

      this.log.info("Response received from Process Service [Request ID: " + response.getReqId() + " CMR No: " + response.getCmrNo() + " Status: "
          + response.getStatus() + " Message: " + (response.getMessage() != null ? response.getMessage() : "-") + "]");

      // get the results from the service and process jason response
      try {
        String currentTsStr = formatter.format(SystemUtil.getCurrentTimestamp());
        Date currentTs = formatter.parse(currentTsStr);

        // update the Admin table for all the update responses
        PreparedQuery updAdminQry = new PreparedQuery(em, ExternalizedQuery.getSql("BATCH.GET_ADMIN_ENTITY"));
        updAdminQry.setParameter("REQ_ID", reqId);
        List<Admin> adminList = updAdminQry.getResults(Admin.class);
        Admin adminEntityWf = new Admin();
        for (Admin adminEntity : adminList) {
          if (batchAction.equalsIgnoreCase("MonitorAbortedRecords") && currentRDCProcStat.equalsIgnoreCase(CmrConstants.RDC_STATUS_ABORTED)
              && response.getStatus().equalsIgnoreCase(CmrConstants.RDC_STATUS_ABORTED)) {
            adminEntity.setRdcProcessingStatus(CmrConstants.RDC_STATUS_NOT_COMPLETED);
          } else {
            adminEntity.setRdcProcessingStatus(response.getStatus());
          }
          adminEntity.setRdcProcessingMsg(response.getMessage());
          adminEntity.setRdcProcessingTs(currentTs);
          updateEntity(adminEntity, em);
          adminEntityWf = adminEntity;
        }

        if (CmrConstants.RDC_STATUS_COMPLETED.equalsIgnoreCase(response.getStatus())
            || CmrConstants.RDC_STATUS_COMPLETED_WITH_WARNINGS.equalsIgnoreCase(response.getStatus())) {

          // get the update date from the RDC Records returned from the
          // service
          Date updateTs = null;
          for (RDcRecord record : response.getRecords()) {

            updateTs = formatter.parse(record.getUpdateDate());

            // update the ADDR Table for each rdc record returned by the
            // create cmr service for create request

            PreparedQuery updAddrQry = new PreparedQuery(em, ExternalizedQuery.getSql("BATCH.GET_ADDR_ENTITY_CREATE_REQ"));
            updAddrQry.setParameter("REQ_ID", reqId);
            // if returned is ZS01/ZI01, update the ZS01 address. Else, Update
            // the ZI01 address
            updAddrQry.setParameter("ADDR_TYPE", "ZS01".equals(record.getAddressType()) || "ZI01".equals(record.getAddressType()) ? "ZS01" : "ZI01");
            List<Addr> addrList = updAddrQry.getResults(Addr.class);
            for (Addr addrEntity : addrList) {
              addrEntity.setSapNo(record.getSapNo());
              addrEntity.setRdcCreateDt(record.getCreateDate());
              addrEntity.setRdcLastUpdtDt(updateTs);
              this.log.info("Address Record Updated [Request ID: " + addrEntity.getId().getReqId() + " Type: " + addrEntity.getId().getAddrType()
                  + " SAP No: " + record.getSapNo() + "]");
              updateEntity(addrEntity, em);
            }
          }
        }

        // create comment log and workflow history entries for create type of
        // request
        String actionWf = "System Action:RDc Create";
        if (CmrConstants.RDC_STATUS_COMPLETED.equalsIgnoreCase(response.getStatus())) {
          StringBuilder comment = new StringBuilder();
          comment = comment.append("RDc processing successfully created KUNNR(s): ");
          if (response.getRecords() != null && response.getRecords().size() != 0) {
            for (int i = 0; i < response.getRecords().size(); i++) {
              comment = comment.append(response.getRecords().get(i).getSapNo() + " ");
            }
          }
          createCommentLogAfterProcess(this, em, SystemConfiguration.getValue("BATCH_USERID"), reqId, comment.toString());
          createWfHistory(this, em, reqId, adminEntityWf.getReqStatus(), SystemConfiguration.getValue("BATCH_USERID"), comment.toString(), actionWf);
        } else if (CmrConstants.RDC_STATUS_COMPLETED_WITH_WARNINGS.equalsIgnoreCase(response.getStatus())) {

          StringBuilder comment = new StringBuilder();
          comment = comment.append("RDc processing successfully created KUNNR(s): ");
          if (response.getRecords() != null && response.getRecords().size() != 0) {
            for (int i = 0; i < response.getRecords().size(); i++) {
              comment = comment.append(response.getRecords().get(i).getSapNo() + " ");
            }
          }

          comment = comment.append(" Warning Message: " + response.getMessage());
          createCommentLogAfterProcess(this, em, SystemConfiguration.getValue("BATCH_USERID"), reqId, comment.toString());
          createWfHistory(this, em, reqId, adminEntityWf.getReqStatus(), SystemConfiguration.getValue("BATCH_USERID"), comment.toString(), actionWf);

        } else if (batchAction.equalsIgnoreCase("MonitorAbortedRecords") && currentRDCProcStat.equalsIgnoreCase(CmrConstants.RDC_STATUS_ABORTED)
            && response.getStatus().equalsIgnoreCase(CmrConstants.RDC_STATUS_ABORTED)) {
          StringBuilder comment = new StringBuilder();
          comment = comment.append("RDc create processing failed. Error: " + response.getMessage());
          createCommentLogAfterProcess(this, em, SystemConfiguration.getValue("BATCH_USERID"), reqId, comment.toString());
          createWfHistory(this, em, reqId, adminEntityWf.getReqStatus(), SystemConfiguration.getValue("BATCH_USERID"), comment.toString(), actionWf);
        } else if (CmrConstants.RDC_STATUS_ABORTED.equalsIgnoreCase(response.getStatus())) {
          StringBuilder comment = new StringBuilder();
          comment = comment.append("RDc create processing failed. Error: " + response.getMessage() + " System will retry processing once.");
          createCommentLogAfterProcess(this, em, SystemConfiguration.getValue("BATCH_USERID"), reqId, comment.toString());
          createWfHistory(this, em, reqId, adminEntityWf.getReqStatus(), SystemConfiguration.getValue("BATCH_USERID"), comment.toString(), actionWf);
        } else if (CmrConstants.RDC_STATUS_NOT_COMPLETED.equalsIgnoreCase(response.getStatus())) {
          StringBuilder comment = new StringBuilder();
          comment = comment.append("RDc create processing failed. Error: " + response.getMessage());
          createCommentLogAfterProcess(this, em, SystemConfiguration.getValue("BATCH_USERID"), reqId, comment.toString());
          createWfHistory(this, em, reqId, adminEntityWf.getReqStatus(), SystemConfiguration.getValue("BATCH_USERID"), comment.toString(), actionWf);
        }

        partialCommit(em);

      } catch (Exception e) {
        this.log.error("Error in processing Admin and ADDR Updates for Create Request " + " [" + e.getMessage() + "]");
        throw e;
      }
    }

    if (currentRDCProcStat.equalsIgnoreCase(CmrConstants.RDC_STATUS_ABORTED) && resultCode.equalsIgnoreCase(CmrConstants.RDC_STATUS_ABORTED)) {
      resultCode = CmrConstants.RDC_STATUS_NOT_COMPLETED;
    }
    return resultCode;
  }

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
        // request.set
      }
    }
  }

  /**
   * Create the comment log entry for the request in Notify_Req
   * 
   * @param service
   * @param entityManager
   * @param notifyReqModel
   * @throws CmrException
   * @throws SQLException
   */
  public void createCommentLog(BaseService<?, ?> service, EntityManager em, NotifyReqModel notifyReqModel) throws CmrException, SQLException {
    this.log.info("Creating Comment Log for  Notify Req " + notifyReqModel.getNotifyId() + " [Request ID: " + notifyReqModel.getReqId() + "]");
    ReqCmtLog reqCmtLog = new ReqCmtLog();
    ReqCmtLogPK reqCmtLogpk = new ReqCmtLogPK();
    reqCmtLogpk.setCmtId(SystemUtil.getNextID(em, SystemConfiguration.getValue("MANDT"), "CMT_ID"));
    reqCmtLog.setId(reqCmtLogpk);
    reqCmtLog.setReqId(notifyReqModel.getReqId());
    reqCmtLog.setCmt(notifyReqModel.getCmtLogMsg());
    // save cmtlockedIn as Y default for current realese
    reqCmtLog.setCmtLockedIn(CmrConstants.CMT_LOCK_IND_YES);
    reqCmtLog.setCreateById(notifyReqModel.getChangedById());
    reqCmtLog.setCreateByNm(notifyReqModel.getChangedById());
    // set createTs as current timestamp and updateTs same as CreateTs
    reqCmtLog.setCreateTs(SystemUtil.getCurrentTimestamp());
    reqCmtLog.setUpdateTs(reqCmtLog.getCreateTs());
    service.createEntity(reqCmtLog, em);
  }

  /**
   * Monitor manually processed records
   * 
   * @param entityManager
   * @throws JsonGenerationException
   * @throws JsonMappingException
   * @throws IOException
   * @throws Exception
   */
  public void monitorDisAutoProcRec(EntityManager em) throws JsonGenerationException, JsonMappingException, IOException, Exception {

    String cmrno = "";

    // search the manual processed records from Admin table
    String sql = ExternalizedQuery.getSql("BATCH.MONITOR_DISABLE_AUTO_PROC");
    PreparedQuery query = new PreparedQuery(em, sql);
    List<Admin> manualRecList = query.getResults(Admin.class);
    this.log.debug("Size of manualRecList : " + manualRecList.size());

    for (Admin manualRec : manualRecList) {
      try {
        log.info("Processing Manual Record " + manualRec.getId().getReqId() + " [Request ID: " + manualRec.getId().getReqId() + "]");
        sql = ExternalizedQuery.getSql("BATCH.GET_DATA");
        query = new PreparedQuery(em, sql);
        query.setParameter("REQ_ID", manualRec.getId().getReqId());
        List<Data> manualRecDataList = query.getResults(Data.class);
        for (Data manualRecData : manualRecDataList) {
          cmrno = manualRecData.getCmrNo();
        }
        // get input params for create cmr service
        CmrServiceInput cmrServiceInput = getReqParam(em, manualRec.getId().getReqId(), manualRec.getReqType(), cmrno);
        cmrServiceInput.setCmrIssuingCntry(manualRecDataList.get(0).getCmrIssuingCntry());
        // connect to the create cmr service for the processed records
        if (manualRec.getReqStatus() != null && !"".equalsIgnoreCase(manualRec.getReqStatus()) && manualRec.getReqStatus().equalsIgnoreCase("COM")) {
          String batchAction = "MonitorManualRecords";
          processCreateCMRService(em, manualRec.getId().getReqId(), cmrServiceInput, batchAction, manualRec.getRdcProcessingStatus());
        }
      } catch (Exception e) {
        this.log.error("Error in processing Manual Record " + manualRec.getId().getReqId() + " for Request ID " + manualRec.getId().getReqId() + " ["
            + e.getMessage() + "]");
      }
    }
  }

  /**
   * Monitor aborted records
   * 
   * @param entityManager
   * @throws JsonGenerationException
   * @throws JsonMappingException
   * @throws IOException
   * @throws Exception
   */
  public void monitorAbortedRecords(EntityManager em) throws JsonGenerationException, JsonMappingException, IOException, Exception {

    String cmrno = "";
    String cmrIssuingCountry = "";
    // search the manual processed records from Admin table
    String sql = ExternalizedQuery.getSql("BATCH.MONITOR_ABORTED_REC");
    PreparedQuery query = new PreparedQuery(em, sql);
    List<Admin> manualRecList = query.getResults(Admin.class);
    this.log.debug("Size of abortedRecList : " + manualRecList.size());

    for (Admin manualRec : manualRecList) {
      try {
        String batchAction = "MonitorAbortedRecords";
        log.info("Processing Aborted Record " + manualRec.getId().getReqId() + " [Request ID: " + manualRec.getId().getReqId() + "]");
        sql = ExternalizedQuery.getSql("BATCH.GET_DATA");
        query = new PreparedQuery(em, sql);
        query.setParameter("REQ_ID", manualRec.getId().getReqId());
        List<Data> manualRecDataList = query.getResults(Data.class);
        for (Data manualRecData : manualRecDataList) {
          cmrno = manualRecData.getCmrNo();
          cmrIssuingCountry = manualRecData.getCmrIssuingCntry();
        }
        boolean isMassRecord = false;
        // boolean isMassRdcTaggedOnly = false;
        boolean isReactivateRecord = false;
        boolean isDeleteRecord = false;
        boolean isEnterpriseRecord = false;

        if (null != manualRec.getReqType() && !"".equalsIgnoreCase(manualRec.getReqType())
            && CmrConstants.REQ_TYPE_MASS_UPDATE.equalsIgnoreCase(manualRec.getReqType())) {
          isMassRecord = true;
        } else if (null != manualRec.getReqType() && !"".equalsIgnoreCase(manualRec.getReqType())
            && CmrConstants.REQ_TYPE_REACTIVATE.equalsIgnoreCase(manualRec.getReqType())) {
          isReactivateRecord = true;
        } else if (null != manualRec.getReqType() && !"".equalsIgnoreCase(manualRec.getReqType())
            && CmrConstants.REQ_TYPE_DELETE.equalsIgnoreCase(manualRec.getReqType())) {
          isDeleteRecord = true;
        } else if (null != manualRec.getReqType() && !"".equalsIgnoreCase(manualRec.getReqType())
            && CmrConstants.REQ_TYPE_UPDT_BY_ENT.equalsIgnoreCase(manualRec.getReqType())) {
          isEnterpriseRecord = true;
        }

        if (isMassRecord || isReactivateRecord || isDeleteRecord) {
          MassUpdateServiceInput mssUpdateServiceInput = getMassReqParam(em, manualRec.getId().getReqId(), manualRec.getReqType());
          mssUpdateServiceInput.setCmrIssuingCntry(cmrIssuingCountry);
          processMassUpdateService(em, manualRec.getId().getReqId(), manualRec.getIterationId(), cmrIssuingCountry, mssUpdateServiceInput,
              batchAction, manualRec.getRdcProcessingStatus());
        } else if (isEnterpriseRecord) {
          processingUpdateByEnterprise(em, manualRec.getId().getReqId(), manualRec.getIterationId(), cmrIssuingCountry, batchAction,
              manualRec.getRdcProcessingStatus());
        } else {
          // get input params for create cmr service
          CmrServiceInput cmrServiceInput = getReqParam(em, manualRec.getId().getReqId(), manualRec.getReqType(), cmrno);
          cmrServiceInput.setCmrIssuingCntry(cmrIssuingCountry);
          // connect to the create cmr service for the processed records
          if (manualRec.getReqStatus() != null && !"".equalsIgnoreCase(manualRec.getReqStatus())
              && manualRec.getReqStatus().equalsIgnoreCase("COM")) {
            String result = processCreateCMRService(em, manualRec.getId().getReqId(), cmrServiceInput, batchAction,
                manualRec.getRdcProcessingStatus());

            this.log.debug("Result from Create Service: '" + result + "'");
            // changes to force the type to be update and send back to tc
            if ((CmrConstants.RDC_STATUS_COMPLETED.equals(result) || CmrConstants.RDC_STATUS_COMPLETED_WITH_WARNINGS.equals(result))
                && CmrConstants.REQ_TYPE_CREATE.equals(manualRec.getReqType()) && "COM".equals(manualRec.getReqStatus())) {
              forceChangeToUpdate(em, manualRec.getId().getReqId());
            } else if (CmrConstants.RDC_STATUS_NOT_COMPLETED.equals(result) && CmrConstants.REQ_TYPE_CREATE.equals(manualRec.getReqType())) {
              forceChangeToUpdate(em, manualRec.getId().getReqId());
            } else if ((CmrConstants.RDC_STATUS_COMPLETED.equals(result) || CmrConstants.RDC_STATUS_COMPLETED_WITH_WARNINGS.equals(result))
                && CmrConstants.REQ_TYPE_UPDATE.equals(manualRec.getReqType()) && "COM".equals(manualRec.getReqStatus())) {
              processRevertToCreate(em, manualRec.getId().getReqId());
            } else if ((CmrConstants.RDC_STATUS_NOT_COMPLETED.equals(result)) && CmrConstants.REQ_TYPE_UPDATE.equals(manualRec.getReqType())
                && "COM".equals(manualRec.getReqStatus())) {
              processRevertToCreate(em, manualRec.getId().getReqId());
            }

          }
        }

      } catch (Exception e) {
        this.log.error("Error in processing Aborted Record " + manualRec.getId().getReqId() + " for Request ID " + manualRec.getId().getReqId() + " ["
            + e.getMessage() + "]");
      }
    }
  }

  /**
   * Initializes the ProcessClient
   * 
   * @throws Exception
   */
  private void initClient() throws Exception {
    if (this.serviceClient == null) {
      this.serviceClient = CmrServicesFactory.getInstance().createClient(BATCH_SERVICES_URL, ProcessClient.class);
    }
    if ((this.massServiceClient == null)) {
      this.massServiceClient = MassServicesFactory.getInstance().createClient(BATCH_SERVICES_URL, MassProcessClient.class);
    }
  }

  /**
   * Get values of the input parameters for the createCMRService Request
   * 
   * @param entityManager
   * @param reqId
   * @param reqType
   * @param cmrno
   */
  public static MassUpdateServiceInput getMassReqParam(EntityManager em, long reqId, String reqType) {
    String requestType = ((reqType) != null && (reqType).trim().length() > 0) ? (reqType) : "";
    long requestId = reqId;
    MassUpdateServiceInput massUpdateServiceInput = new MassUpdateServiceInput();
    massUpdateServiceInput.setInputMandt(SystemConfiguration.getValue("MANDT"));
    massUpdateServiceInput.setInputReqId(requestId);
    massUpdateServiceInput.setInputReqType(requestType);
    massUpdateServiceInput.setInputUserId(SystemConfiguration.getValue("BATCH_USERID"));

    return massUpdateServiceInput;
  }

  /**
   * Calls the MassUpdateService and process rdc tagged records only
   * 
   * @param entityManager
   * @param reqId
   * @param cmrServiceInput
   * @param batchAction
   * @param currentRDCProcStat
   * @throws JsonGenerationException
   * @throws JsonMappingException
   * @throws IOException
   * @throws Exception
   */
  /*
   * public void processMassUpdateServiceRdcTaggedRecord(EntityManager em, long
   * reqId, int itrId, String sysLoc, MassUpdateServiceInput
   * massUpdateServiceInput, String batchAction, String currentRDCProcStat)
   * throws JsonGenerationException, JsonMappingException, IOException,
   * Exception {
   * 
   * try { // get the params for input to the create cmr service DateFormat
   * formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS"); String
   * currentTsStr = formatter.format(SystemUtil.getCurrentTimestamp()); Date
   * currentTs = formatter.parse(currentTsStr); String reqStatus = "PCR"; String
   * lockBy = "CreateCMR Automatic Processing"; String cmt =
   * "Automatic Processing by System"; String action =
   * "Automatic Processing by System"; // update the Admin table o Update
   * REQCMRCREQCMR.ADMIN fields.
   * 
   * PreparedQuery updAdminQry = new PreparedQuery(em,
   * ExternalizedQuery.getSql("BATCH.GET_ADMIN_ENTITY"));
   * updAdminQry.setParameter("REQ_ID", reqId); List<Admin> adminList =
   * updAdminQry.getResults(Admin.class); for (Admin adminEntity : adminList) {
   * adminEntity.setReqStatus(reqStatus); adminEntity.setLockBy(lockBy);
   * adminEntity.setLockInd(CmrConstants.YES_NO.Y.toString());
   * adminEntity.setLockTs(currentTs);
   * 
   * updateEntity(adminEntity, em); } // create a workflow record in wf_hist
   * table
   * 
   * createWorkflowHistory(this, em, reqId, reqStatus, lockBy, currentTs, cmt,
   * action); partialCommit(em);
   * 
   * // Create the request MassProcessRequest request = new
   * MassProcessRequest(); // set the update mass record in request request =
   * generateMassRequest(em, reqId, itrId, sysLoc, massUpdateServiceInput,
   * request); // call the Mass Update service this.log.info(
   * "Sending request of RDC tagged only record to Mass Update Service [Request ID: "
   * + request.getReqId() + "  Type: " + request.getReqType() + "]");
   * 
   * this.log.trace("Request JSON:"); if (this.log.isTraceEnabled()) {
   * DebugUtil.printObjectAsJson(this.log, request); }
   * 
   * // actual service call here MassProcessResponse massProcessResponse =
   * this.massServiceClient .executeAndWrap(MassProcessClient.US_APP_ID,
   * request, MassProcessResponse.class);
   * 
   * this.log.trace("Response JSON:"); if (this.log.isTraceEnabled()) {
   * DebugUtil.printObjectAsJson(this.log, massProcessResponse); }
   * this.log.info("Response received from Mass Update Service [Request ID: " +
   * massProcessResponse.getReqId() + " Status: " +
   * massProcessResponse.getStatus() + " Message: " +
   * (massProcessResponse.getMsg() != null ? massProcessResponse.getMsg() : "-")
   * + "]");
   * 
   * // set the admin values based on the response // try this // get the
   * results from the service and process jason response try { // update the
   * Admin table for all the update responses String currentTimeStampString =
   * formatter.format(SystemUtil.getCurrentTimestamp()); Date currentTimeStamp =
   * formatter.parse(currentTimeStampString);
   * 
   * String returnStatus = massProcessResponse.getStatus(); String adminStatus =
   * ""; PreparedQuery updAdminPreQry = new PreparedQuery(em,
   * ExternalizedQuery.getSql("BATCH.GET_ADMIN_ENTITY"));
   * updAdminPreQry.setParameter("REQ_ID", reqId); List<Admin> adminRecList =
   * updAdminQry.getResults(Admin.class); for (Admin updtAdminEntity :
   * adminRecList) { updtAdminEntity.setLockInd(CmrConstants.CMT_LOCK_IND_NO);
   * updtAdminEntity.setLockByNm(null); updtAdminEntity.setLockBy(null);
   * updtAdminEntity.setLockTs(null);
   * updtAdminEntity.setLastUpdtBy("CreateCMR");
   * updtAdminEntity.setLastUpdtTs(currentTimeStamp); if (null != returnStatus
   * && CmrConstants.RDC_STATUS_COMPLETED.equalsIgnoreCase(returnStatus)) {
   * adminStatus = "COM"; createWorkflowHistory(this, em, reqId, adminStatus,
   * lockBy, currentTimeStamp, cmt, action);
   * updtAdminEntity.setReqStatus(adminStatus); } if (null != returnStatus &&
   * CmrConstants.RDC_STATUS_NOT_COMPLETED.equalsIgnoreCase(returnStatus)) {
   * adminStatus = "PPN"; createCommentLog(this, em, reqId, lockBy,
   * massProcessResponse.getMsg()); createWorkflowHistory(this, em, reqId,
   * adminStatus, lockBy, currentTimeStamp, cmt, action);
   * updtAdminEntity.setReqStatus(adminStatus); }
   * 
   * if (null != returnStatus &&
   * CmrConstants.RDC_STATUS_ABORTED.equalsIgnoreCase(returnStatus)) {
   * adminStatus = "PCP"; createWorkflowHistory(this, em, reqId, adminStatus,
   * lockBy, currentTimeStamp, cmt, action);
   * updtAdminEntity.setReqStatus(adminStatus); }
   * 
   * if (batchAction.equalsIgnoreCase("MonitorAbortedRecords")) { if
   * ((currentRDCProcStat.equalsIgnoreCase(CmrConstants.RDC_STATUS_ABORTED)) &&
   * ((((returnStatus.equalsIgnoreCase(CmrConstants.RDC_STATUS_ABORTED)) ||
   * (returnStatus .equalsIgnoreCase(CmrConstants.RDC_STATUS_NOT_COMPLETED))))))
   * { updtAdminEntity.setRdcProcessingStatus(CmrConstants.
   * RDC_STATUS_NOT_COMPLETED ); } else if
   * (returnStatus.equalsIgnoreCase(CmrConstants.RDC_STATUS_COMPLETED)) {
   * updtAdminEntity.setRdcProcessingStatus(CmrConstants.RDC_STATUS_COMPLETED);
   * } } else {
   * updtAdminEntity.setRdcProcessingStatus(massProcessResponse.getStatus()); }
   * updtAdminEntity.setRdcProcessingMsg(massProcessResponse.getMsg());
   * updtAdminEntity.setRdcProcessingTs(currentTs);
   * updateEntity(updtAdminEntity, em); } // update MASS_UPDT table with the
   * error txt and row status cd
   * 
   * for (ResponseRecord record : massProcessResponse.getRecords()) {
   * PreparedQuery updMassAddrQry = new PreparedQuery(em,
   * ExternalizedQuery.getSql("BATCH.GET_MASS_UPDT_ENTITY"));
   * updMassAddrQry.setParameter("REQ_ID", reqId);
   * updMassAddrQry.setParameter("ITERATION_ID", itrId);
   * updMassAddrQry.setParameter("CMR_NO", record.getCmrNo()); List<MassUpdt>
   * MassUpdtList = updMassAddrQry.getResults(MassUpdt.class); for (MassUpdt
   * MassUpdtEntity : MassUpdtList) {
   * MassUpdtEntity.setErrorTxt(record.getMessage()); if (null !=
   * record.getStatus() &&
   * CmrConstants.RDC_STATUS_COMPLETED.equalsIgnoreCase((record.getStatus()))) {
   * MassUpdtEntity.setRowStatusCd("DONE"); } else {
   * MassUpdtEntity.setRowStatusCd("RDCER"); }
   * this.log.info("Mass Update Record Updated [Request ID: " +
   * MassUpdtEntity.getId().getParReqId() + " CMR_NO: " +
   * MassUpdtEntity.getCmrNo() + " SEQ No: " + MassUpdtEntity.getId().getSeqNo()
   * + "]"); updateEntity(MassUpdtEntity, em); } }
   * 
   * partialCommit(em);
   * 
   * } catch (Exception e) { this.log.error(
   * "Error in processing of Admin and MASS_UPDT Updates(RDC tagged only) " +
   * " [" + e.getMessage() + "]"); throw e; }
   * 
   * }
   * 
   * catch (Exception e) { this.log.
   * error("Error in processing Rdc tagged only  Record  for Request ID " +
   * reqId + " [" + e.getMessage() + "]"); }
   * 
   * }
   */

  public MassProcessRequest generateMassRequest(EntityManager em, long reqId, int itrId, String sysLoc, MassUpdateServiceInput massUpdateServiceInput,
      MassProcessRequest request) throws JsonGenerationException, JsonMappingException, IOException, Exception {
    request.setMandt(massUpdateServiceInput.getInputMandt());
    request.setReqId(massUpdateServiceInput.getInputReqId());
    request.setReqType(massUpdateServiceInput.getInputReqType());
    request.setUserId(massUpdateServiceInput.getInputUserId());

    List<MassUpdateRecord> updtRecordsList = new ArrayList<MassUpdateRecord>();
    fetchAddressDetails(em, reqId, itrId, sysLoc, updtRecordsList);
    fetchDataDetails(em, reqId, itrId, sysLoc, updtRecordsList);
    request.setRecords(updtRecordsList);
    return request;

  }

  private void fetchDataDetails(EntityManager em, long reqId, int itrId, String sysLoc, List<MassUpdateRecord> updtRecordsList) {
    String Status = "PASS";
    String addrTypeAll = "ALL";
    String massSql = "BATCH.MASS_UPDATE_DATA_RECORDS";
    String map = Admin.BATCH_UPDATE_DATA_MAPPING;
    PreparedQuery massquery = new PreparedQuery(em, ExternalizedQuery.getSql(massSql));
    massquery.setParameter("REQ_ID", reqId);
    massquery.setParameter("ITERATION_ID", itrId);
    massquery.setParameter("STATUS", Status);
    List<CompoundEntity> results = massquery.getCompundResults(Admin.class, map);
    MassUpdt mass_updt = null;
    MassUpdtData mass_updt_data = null;
    this.log.debug("Size of Mass Upadte data Record list : " + results.size());
    for (CompoundEntity entity : results) {
      mass_updt = entity.getEntity(MassUpdt.class);
      mass_updt_data = entity.getEntity(MassUpdtData.class);
      // set all the data value with addretype =ALL
      List<RequestValueRecord> requestDataValueRecords = new ArrayList<RequestValueRecord>();
      MassUpdateRecord updtDataRec = new MassUpdateRecord();

      // set the values in the update record
      updtDataRec.setCmrNo(mass_updt.getCmrNo());
      updtDataRec.setSysLoc(sysLoc);
      updtDataRec.setAddrType(addrTypeAll);

      // add NAME1_Record
      // if (!StringUtils.isBlank(mass_updt_data.getCustNm1())) {
      if ((null != mass_updt_data.getCustNm1()) && (mass_updt_data.getCustNm1().length() > 0)) {
        RequestValueRecord NAME1_Record = new RequestValueRecord();
        NAME1_Record.setField("NAME1");
        NAME1_Record.setValue(mass_updt_data.getCustNm1());
        requestDataValueRecords.add(NAME1_Record);
      }

      // add NAME2_Record
      // if (!StringUtils.isBlank(mass_updt_data.getCustNm2())) {
      if ((null != mass_updt_data.getCustNm2()) && (mass_updt_data.getCustNm2().length() > 0)) {
        RequestValueRecord NAME2_Record = new RequestValueRecord();
        NAME2_Record.setField("NAME2");
        NAME2_Record.setValue(mass_updt_data.getCustNm2());
        requestDataValueRecords.add(NAME2_Record);
      }

      // add ZZKV_SIC_Record
      // if (!StringUtils.isBlank(mass_updt_data.getIsicCd())) {
      if ((null != mass_updt_data.getIsicCd()) && (mass_updt_data.getIsicCd().length() > 0)) {
        RequestValueRecord ZZKV_SIC_Record = new RequestValueRecord();
        ZZKV_SIC_Record.setField("ZZKV_SIC");
        ZZKV_SIC_Record.setValue(mass_updt_data.getIsicCd());
        requestDataValueRecords.add(ZZKV_SIC_Record);
      }

      // add ISU_CD_Record
      // if (!StringUtils.isBlank(mass_updt_data.getIsuCd())) {
      if ((null != mass_updt_data.getIsuCd()) && (mass_updt_data.getIsuCd().length() > 0)) {
        RequestValueRecord ISU_CD_Record = new RequestValueRecord();
        ISU_CD_Record.setField("BRSCH");
        ISU_CD_Record.setValue(mass_updt_data.getIsuCd());
        requestDataValueRecords.add(ISU_CD_Record);
      }

      // add INAC_CD_Record
      // if (!StringUtils.isBlank(mass_updt_data.getInacCd())) {
      if ((null != mass_updt_data.getInacCd()) && (mass_updt_data.getInacCd().length() > 0)) {
        RequestValueRecord INAC_CD_Record = new RequestValueRecord();
        INAC_CD_Record.setField("ZZKV_INAC");
        INAC_CD_Record.setValue(mass_updt_data.getInacCd());
        requestDataValueRecords.add(INAC_CD_Record);
      }

      // add CLIENT_TIER_Record
      // if (!StringUtils.isBlank(mass_updt_data.getClientTier())) {
      if ((null != mass_updt_data.getClientTier()) && (mass_updt_data.getClientTier().length() > 0)) {
        RequestValueRecord CLIENT_TIER_Record = new RequestValueRecord();
        CLIENT_TIER_Record.setField("KATR3");
        CLIENT_TIER_Record.setValue(mass_updt_data.getClientTier());
        requestDataValueRecords.add(CLIENT_TIER_Record);
      }

      // add TAX_CD1_Record
      // if (!StringUtils.isBlank(mass_updt_data.getTaxCd1())) {
      if ((null != mass_updt_data.getTaxCd1()) && (mass_updt_data.getTaxCd1().length() > 0)) {
        RequestValueRecord TAX_CD1_Record = new RequestValueRecord();
        TAX_CD1_Record.setField("STCD2");
        TAX_CD1_Record.setValue(mass_updt_data.getTaxCd1());
        requestDataValueRecords.add(TAX_CD1_Record);
      }

      // add ENTERPRISE_Record
      // if (!StringUtils.isBlank(mass_updt_data.getEnterprise())) {
      if ((null != mass_updt_data.getEnterprise()) && (mass_updt_data.getEnterprise().length() > 0)) {
        RequestValueRecord ENTERPRISE_Record = new RequestValueRecord();
        ENTERPRISE_Record.setField("ZZKV_NODE2");
        ENTERPRISE_Record.setValue(mass_updt_data.getEnterprise());
        requestDataValueRecords.add(ENTERPRISE_Record);
      }
      // add KONZS_Record
      if ((null != mass_updt_data.getAffiliate()) && (mass_updt_data.getAffiliate().length() > 0)) {
        RequestValueRecord KONZS_Record = new RequestValueRecord();
        KONZS_Record.setField("KONZS");
        KONZS_Record.setValue(mass_updt_data.getAffiliate());
        requestDataValueRecords.add(KONZS_Record);
      }

      // set requestDataValueRecords list updatDataRec
      updtDataRec.setValues(requestDataValueRecords);
      int recordSize = updtDataRec.getValues().size();
      // set record in request only if not empty
      if (recordSize > 0) {
        // set updatDataRec in updtRecordsList
        updtRecordsList.add(updtDataRec);
      }
    }
  }

  private void fetchAddressDetails(EntityManager em, long reqId, int itrId, String sysLoc, List<MassUpdateRecord> updtRecordsList) {
    // code to retrieve the cmrNo,addrType and other fields
    String Status = "PASS";
    String massSql = "BATCH.MASS_UPDATE_ADDR_RECORDS";
    String map = Admin.BATCH_UPDATE_ADDR_MAPPING;
    PreparedQuery massquery = new PreparedQuery(em, ExternalizedQuery.getSql(massSql));
    massquery.setParameter("REQ_ID", reqId);
    massquery.setParameter("ITERATION_ID", itrId);
    massquery.setParameter("STATUS", Status);
    List<CompoundEntity> results = massquery.getCompundResults(Admin.class, map);
    MassUpdt mass_updt = null;
    MassUpdtAddr mass_updt_addr = null;
    this.log.debug("Size of Mass Upadte Addr Record list : " + results.size());
    for (CompoundEntity entity : results) {
      mass_updt = entity.getEntity(MassUpdt.class);
      mass_updt_addr = entity.getEntity(MassUpdtAddr.class);

      List<RequestValueRecord> requestAddrValueRecords = new ArrayList<RequestValueRecord>();
      MassUpdateRecord updtAddrRec = new MassUpdateRecord();

      // set the values in the update record
      updtAddrRec.setCmrNo(mass_updt.getCmrNo());
      updtAddrRec.setSysLoc(sysLoc);
      updtAddrRec.setAddrType(mass_updt_addr.getId().getAddrType());

      // add ADDR_TXT_Record
      // if (!StringUtils.isBlank(mass_updt_addr.getAddrTxt())) {
      if ((null != mass_updt_addr.getAddrTxt()) && (mass_updt_addr.getAddrTxt().length() > 0)) {
        RequestValueRecord ADDR_TXT_Record = new RequestValueRecord();
        ADDR_TXT_Record.setField("STRAS");
        ADDR_TXT_Record.setValue(mass_updt_addr.getAddrTxt());
        requestAddrValueRecords.add(ADDR_TXT_Record);
      }

      // add CITY1_Record
      // if (!StringUtils.isBlank(mass_updt_addr.getCity1())) {
      if ((null != mass_updt_addr.getCity1()) && (mass_updt_addr.getCity1().length() > 0)) {
        RequestValueRecord CITY1_Record = new RequestValueRecord();
        CITY1_Record.setField("ORT01");
        CITY1_Record.setValue(mass_updt_addr.getCity1());
        requestAddrValueRecords.add(CITY1_Record);
      }

      // add POST_CD_Record
      // if (!StringUtils.isBlank(mass_updt_addr.getPostCd())) {
      if ((null != mass_updt_addr.getPostCd()) && (mass_updt_addr.getPostCd().length() > 0)) {
        RequestValueRecord POST_CD_Record = new RequestValueRecord();
        POST_CD_Record.setField("PSTLZ");
        POST_CD_Record.setValue(mass_updt_addr.getPostCd());
        requestAddrValueRecords.add(POST_CD_Record);
      }

      // add STATE_PROV_Record
      // if (!StringUtils.isBlank(mass_updt_addr.getStateProv())) {
      if ((null != mass_updt_addr.getStateProv()) && (mass_updt_addr.getStateProv().length() > 0)) {
        RequestValueRecord STATE_PROV_Record = new RequestValueRecord();
        STATE_PROV_Record.setField("REGIO");
        STATE_PROV_Record.setValue(mass_updt_addr.getStateProv());
        requestAddrValueRecords.add(STATE_PROV_Record);
      }

      // add DIVN_Record
      // if (!StringUtils.isBlank(mass_updt_addr.getDivn())) {
      if ((null != mass_updt_addr.getDivn()) && (mass_updt_addr.getDivn().length() > 0)) {
        RequestValueRecord DIVN_Record = new RequestValueRecord();
        DIVN_Record.setField("NAME3");
        DIVN_Record.setValue(mass_updt_addr.getDivn());
        requestAddrValueRecords.add(DIVN_Record);
      }

      // add DEPT_Record
      // if (!StringUtils.isBlank(mass_updt_addr.getDept())) {
      if ((null != mass_updt_addr.getDept()) && (mass_updt_addr.getDept().length() > 0)) {
        RequestValueRecord DEPT_Record = new RequestValueRecord();
        DEPT_Record.setField("NAME4");
        DEPT_Record.setValue(mass_updt_addr.getDept());
        requestAddrValueRecords.add(DEPT_Record);
      }

      // add COUNTY_Record
      // if (!StringUtils.isBlank(mass_updt_addr.getCounty())) {
      if ((null != mass_updt_addr.getCounty()) && (mass_updt_addr.getCounty().length() > 0)) {
        RequestValueRecord COUNTY_Record = new RequestValueRecord();
        COUNTY_Record.setField("COUNC");
        COUNTY_Record.setValue(mass_updt_addr.getCounty());
        requestAddrValueRecords.add(COUNTY_Record);
      }

      // set requestAddrValueRecords in updtAddrRec

      updtAddrRec.setValues(requestAddrValueRecords);
      int recordSize = updtAddrRec.getValues().size();
      if (recordSize > 0) {
        // set updtAddrRec in updtRecordsList
        updtRecordsList.add(updtAddrRec);
      }
    }

  }

  public MassProcessRequest generateReactivateDelRequest(EntityManager em, long reqId, int itrId, String sysLoc,
      MassUpdateServiceInput massUpdateServiceInput, MassProcessRequest request)
      throws JsonGenerationException, JsonMappingException, IOException, Exception {
    request.setMandt(massUpdateServiceInput.getInputMandt());
    request.setReqId(massUpdateServiceInput.getInputReqId());
    request.setReqType(massUpdateServiceInput.getInputReqType());
    request.setUserId(massUpdateServiceInput.getInputUserId());

    List<MassUpdateRecord> updtRecordsList = new ArrayList<MassUpdateRecord>();
    // code to retrieve the cmrNo,addrType and other fields
    String Status = "PASS";
    String massSql = "BATCH.REACT_DEL_RECORDS";
    String map = Admin.BATCH_REACT_DEL_SERVICE_MAPPING;
    PreparedQuery massquery = new PreparedQuery(em, ExternalizedQuery.getSql(massSql));
    massquery.setParameter("REQ_ID", reqId);
    massquery.setParameter("ITERATION_ID", itrId);
    massquery.setParameter("STATUS", Status);
    List<CompoundEntity> results = massquery.getCompundResults(Admin.class, map);
    MassUpdt mass_updt = null;

    this.log.debug("Size of Mass Upadte Record list : " + results.size());
    for (CompoundEntity entity : results) {
      MassUpdateRecord updtRec = new MassUpdateRecord();
      mass_updt = entity.getEntity(MassUpdt.class);

      // set the values in the update record
      updtRec.setCmrNo(mass_updt.getCmrNo());
      updtRec.setSysLoc(sysLoc);
      updtRec.setAddrType("ZS01");
      // set all the name and values pair the document here
      List<RequestValueRecord> requestValueRecords = new ArrayList<RequestValueRecord>();

      // add LOEVM_Record
      if (massUpdateServiceInput.getInputReqType() != null && massUpdateServiceInput.getInputReqType().equalsIgnoreCase("D")) {
        RequestValueRecord LOEVM_Record = new RequestValueRecord();
        LOEVM_Record.setField("LOEVM");
        LOEVM_Record.setValue("X");
        requestValueRecords.add(LOEVM_Record);
      } else if (massUpdateServiceInput.getInputReqType() != null && massUpdateServiceInput.getInputReqType().equalsIgnoreCase("R")) {
        RequestValueRecord LOEVM_Record = new RequestValueRecord();
        LOEVM_Record.setField("LOEVM");
        LOEVM_Record.setValue("");
        requestValueRecords.add(LOEVM_Record);
      }

      // set requestValueRecords list updatRec

      updtRec.setValues(requestValueRecords);
      // set updtRec in updtRecordsList
      updtRecordsList.add(updtRec);
      // request.getRecords()

    }
    // set updtRecordsList in request
    request.setRecords(updtRecordsList);

    return request;

  }

  /**
   * Calls the MassUpdateService and process the response returned
   * 
   * @param entityManager
   * @param reqId
   * @param cmrServiceInput
   * @param batchAction
   * @param currentRDCProcStat
   * @throws JsonGenerationException
   * @throws JsonMappingException
   * @throws IOException
   * @throws Exception
   */
  public void processMassUpdateService(EntityManager em, long reqId, int itrId, String sysLoc, MassUpdateServiceInput massUpdateServiceInput,
      String batchAction, String currentRDCProcStat) throws JsonGenerationException, JsonMappingException, IOException, Exception {
    // Create the request
    MassProcessRequest request = new MassProcessRequest();
    // set the update mass record in request
    if (massUpdateServiceInput.getInputReqType() != null
        && (massUpdateServiceInput.getInputReqType().equalsIgnoreCase("D") || massUpdateServiceInput.getInputReqType().equalsIgnoreCase("R"))) {
      request = generateReactivateDelRequest(em, reqId, itrId, sysLoc, massUpdateServiceInput, request);
    } else {
      request = generateMassRequest(em, reqId, itrId, sysLoc, massUpdateServiceInput, request);
    }

    // call the Mass Update service
    this.log.info("Sending request to Mass Update Service [Request ID: " + request.getReqId() + "  Type: " + request.getReqType() + "]");

    this.log.trace("Request JSON:");
    if (this.log.isTraceEnabled()) {
      DebugUtil.printObjectAsJson(this.log, request);
    }
    // actual service call here
    MassProcessResponse massProcessResponse = null;
    String applicationId = BatchUtil.getAppId(massUpdateServiceInput.getCmrIssuingCntry());
    if (applicationId == null) {
      this.log.debug("No Application ID mapped to " + massUpdateServiceInput.getCmrIssuingCntry());
      massProcessResponse = new MassProcessResponse();
      massProcessResponse.setStatus(CmrConstants.RDC_STATUS_NOT_COMPLETED);
      massProcessResponse
          .setMsg("No application ID defined for Country: " + massUpdateServiceInput.getCmrIssuingCntry() + ". Cannot process RDc records.");
    } else {
      try {
        this.massServiceClient.setReadTimeout(60 * 20 * 1000); // 20 mins
        massProcessResponse = this.massServiceClient.executeAndWrap(MassProcessClient.US_APP_ID, request, MassProcessResponse.class);
      } catch (Exception e) {
        this.log.error("Error when connecting to the mass change service.", e);
        massProcessResponse = new MassProcessResponse();
        massProcessResponse.setStatus(CmrConstants.RDC_STATUS_ABORTED);
        massProcessResponse.setMsg("A system error has occured. Setting to aborted.");
      }

      this.log.trace("Response JSON:");
      if (this.log.isTraceEnabled()) {
        DebugUtil.printObjectAsJson(this.log, massProcessResponse);
      }
      this.log.info("Response received from Mass Process Service [Request ID: " + massProcessResponse.getReqId() + " Status: "
          + massProcessResponse.getStatus() + " Message: " + (massProcessResponse.getMsg() != null ? massProcessResponse.getMsg() : "-") + "]");
    }

    // try this
    // get the results from the service and process jason response
    try {
      DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
      String currentTsStr = formatter.format(SystemUtil.getCurrentTimestamp());
      Date currentTs = formatter.parse(currentTsStr);

      // update the Admin table for all the update responses

      PreparedQuery updAdminQry = new PreparedQuery(em, ExternalizedQuery.getSql("BATCH.GET_ADMIN_ENTITY"));
      updAdminQry.setParameter("REQ_ID", reqId);
      List<Admin> adminList = updAdminQry.getResults(Admin.class);
      Admin adminEntityWf = new Admin();
      for (Admin adminEntity : adminList) {
        if (batchAction.equalsIgnoreCase("MonitorAbortedRecords")) {
          if ((currentRDCProcStat.equalsIgnoreCase(CmrConstants.RDC_STATUS_ABORTED))
              && ((((massProcessResponse.getStatus().equalsIgnoreCase(CmrConstants.RDC_STATUS_ABORTED))
                  || (massProcessResponse.getStatus().equalsIgnoreCase(CmrConstants.RDC_STATUS_NOT_COMPLETED)))))) {
            adminEntity.setRdcProcessingStatus(CmrConstants.RDC_STATUS_NOT_COMPLETED);
          } else if (massProcessResponse.getStatus().equalsIgnoreCase(CmrConstants.RDC_STATUS_COMPLETED)) {
            adminEntity.setRdcProcessingStatus(CmrConstants.RDC_STATUS_COMPLETED);
          }
        } else {
          adminEntity.setRdcProcessingStatus(massProcessResponse.getStatus());
        }
        adminEntity.setRdcProcessingMsg(massProcessResponse.getMsg());
        adminEntity.setRdcProcessingTs(currentTs);
        updateEntity(adminEntity, em);
        adminEntityWf = adminEntity;
      }

      // update MASS_UPDT table with the error txt and row status cd
      for (ResponseRecord record : massProcessResponse.getRecords()) {
        PreparedQuery updMassAddrQry = new PreparedQuery(em, ExternalizedQuery.getSql("BATCH.GET_MASS_UPDT_ENTITY"));
        updMassAddrQry.setParameter("REQ_ID", reqId);
        updMassAddrQry.setParameter("ITERATION_ID", itrId);
        updMassAddrQry.setParameter("CMR_NO", record.getCmrNo());
        List<MassUpdt> MassUpdtList = updMassAddrQry.getResults(MassUpdt.class);

        for (MassUpdt MassUpdtEntity : MassUpdtList) {
          MassUpdtEntity.setErrorTxt(record.getMessage());
          if (null != record.getStatus() && CmrConstants.RDC_STATUS_NOT_COMPLETED.equalsIgnoreCase((record.getStatus()))) {
            MassUpdtEntity.setRowStatusCd("RDCER");
          } else if (null != record.getStatus() && (CmrConstants.RDC_STATUS_COMPLETED.equalsIgnoreCase(record.getStatus())
              || CmrConstants.RDC_STATUS_COMPLETED_WITH_WARNINGS.equalsIgnoreCase(record.getStatus()))) {
            MassUpdtEntity.setRowStatusCd("DONE");
          }
          this.log.info("Mass Update Record Updated [Request ID: " + MassUpdtEntity.getId().getParReqId() + " CMR_NO: " + MassUpdtEntity.getCmrNo()
              + " SEQ No: " + MassUpdtEntity.getId().getSeqNo() + "]");
          updateEntity(MassUpdtEntity, em);
        }
      }

      // create comment log and workflow entries for update type of request
      // set the action for mass request
      String actionWf = null;
      String requestTypeStr = null;
      if (massProcessResponse.getReqType() != null && massProcessResponse.getReqType().equalsIgnoreCase("D")) {
        actionWf = "System Action:RDc Mass Delete";
        requestTypeStr = "Mass Delete";
      } else if (massProcessResponse.getReqType() != null && massProcessResponse.getReqType().equalsIgnoreCase("R")) {
        actionWf = "System Action:RDc Mass Reactivate";
        requestTypeStr = "Mass Reactivate";
      } else if (massProcessResponse.getReqType() != null && massProcessResponse.getReqType().equalsIgnoreCase("M")) {
        actionWf = "System Action:RDc Mass Update";
        requestTypeStr = "Mass Update";
      }
      if (CmrConstants.RDC_STATUS_COMPLETED.equalsIgnoreCase(massProcessResponse.getStatus())) {
        StringBuilder comment = new StringBuilder();
        comment = comment.append(requestTypeStr + " in RDc successfully completed.");
        createCommentLogAfterProcess(this, em, SystemConfiguration.getValue("BATCH_USERID"), reqId, comment.toString());
        createWfHistory(this, em, reqId, adminEntityWf.getReqStatus(), SystemConfiguration.getValue("BATCH_USERID"), comment.toString(), actionWf);
      } else if (CmrConstants.RDC_STATUS_COMPLETED_WITH_WARNINGS.equalsIgnoreCase(massProcessResponse.getStatus())) {
        StringBuilder comment = new StringBuilder();
        comment = comment.append(requestTypeStr + " in RDc completed with warning: " + massProcessResponse.getMsg());
        createCommentLogAfterProcess(this, em, SystemConfiguration.getValue("BATCH_USERID"), reqId, comment.toString());
        createWfHistory(this, em, reqId, adminEntityWf.getReqStatus(), SystemConfiguration.getValue("BATCH_USERID"), comment.toString(), actionWf);
      } else if (batchAction.equalsIgnoreCase("MonitorAbortedRecords") && currentRDCProcStat.equalsIgnoreCase(CmrConstants.RDC_STATUS_ABORTED)
          && massProcessResponse.getStatus().equalsIgnoreCase(CmrConstants.RDC_STATUS_ABORTED)) {
        StringBuilder comment = new StringBuilder();
        comment = comment.append(requestTypeStr + " in RDc failed: " + massProcessResponse.getMsg());
        createCommentLogAfterProcess(this, em, SystemConfiguration.getValue("BATCH_USERID"), reqId, comment.toString());
        createWfHistory(this, em, reqId, adminEntityWf.getReqStatus(), SystemConfiguration.getValue("BATCH_USERID"), comment.toString(), actionWf);
      } else if (CmrConstants.RDC_STATUS_ABORTED.equalsIgnoreCase(massProcessResponse.getStatus())) {
        StringBuilder comment = new StringBuilder();
        comment = comment.append("RDC processing aborted with error: " + massProcessResponse.getMsg() + ". System will retry once.");
        createCommentLogAfterProcess(this, em, SystemConfiguration.getValue("BATCH_USERID"), reqId, comment.toString());
        createWfHistory(this, em, reqId, adminEntityWf.getReqStatus(), SystemConfiguration.getValue("BATCH_USERID"), comment.toString(), actionWf);
      } else if (CmrConstants.RDC_STATUS_NOT_COMPLETED.equalsIgnoreCase(massProcessResponse.getStatus())) {
        StringBuilder comment = new StringBuilder();
        comment = comment.append(requestTypeStr + " in RDc failed: " + massProcessResponse.getMsg());
        createCommentLogAfterProcess(this, em, SystemConfiguration.getValue("BATCH_USERID"), reqId, comment.toString());
        createWfHistory(this, em, reqId, adminEntityWf.getReqStatus(), SystemConfiguration.getValue("BATCH_USERID"), comment.toString(), actionWf);
      }

      partialCommit(em);

    } catch (Exception e) {
      this.log.error("Error in processing Admin and MASS_UPDT Updates " + " [" + e.getMessage() + "]");
      throw e;
    }

  }

  /**
   * Create the comment log entry for the request in Notify_Req
   * 
   * @param service
   * @param entityManager
   * @param notifyReqModel
   * @throws CmrException
   * @throws SQLException
   */
  public void createCommentLog(BaseService<?, ?> service, EntityManager em, long reqId, String lockBy, String msg) throws CmrException, SQLException {
    String cmt = "Automatic RDc processing failed:" + msg + ".Download the error log for details.";

    this.log.info("Creating Comment Log for [Request ID: " + reqId + "]");
    ReqCmtLog reqCmtLog = new ReqCmtLog();
    ReqCmtLogPK reqCmtLogpk = new ReqCmtLogPK();
    reqCmtLogpk.setCmtId(SystemUtil.getNextID(em, SystemConfiguration.getValue("MANDT"), "CMT_ID"));
    reqCmtLog.setId(reqCmtLogpk);
    reqCmtLog.setReqId(reqId);
    reqCmtLog.setCmt(cmt);
    // default for current realese need to confirm
    reqCmtLog.setCmtLockedIn(CmrConstants.CMT_LOCK_IND_YES);
    reqCmtLog.setCreateById(lockBy);
    reqCmtLog.setCreateByNm(lockBy); // set createTs
    reqCmtLog.setCreateTs(SystemUtil.getCurrentTimestamp());
    reqCmtLog.setUpdateTs(reqCmtLog.getCreateTs());
    service.createEntity(reqCmtLog, em);

  }

  /**
   * Create the createWfHist record
   * 
   * @param service
   * @param entityManager
   * @param notifyReqModel
   * @throws CmrException
   * @throws SQLException
   */
  public void createWorkflowHistory(BaseService<?, ?> service, EntityManager em, long reqId, String reqStatus, String lockBy, Date currentTs,
      String cmt, String action) throws CmrException, SQLException {
    this.log.info("Creating wf_hist record  Req  for [Request ID: " + reqId + "]");
    WfHist hist = new WfHist();
    WfHistPK histpk = new WfHistPK();
    histpk.setWfId(SystemUtil.getNextID(em, SystemConfiguration.getValue("MANDT"), "WF_ID"));
    hist.setId(histpk);
    hist.setCmt(cmt);
    hist.setReqStatus(reqStatus);
    hist.setCreateById(lockBy);
    hist.setCreateByNm(lockBy);
    hist.setCreateTs(SystemUtil.getCurrentTimestamp());
    hist.setCompleteTs(SystemUtil.getCurrentTimestamp());
    hist.setReqStatusAct(action);
    hist.setReqId(reqId);
    service.createEntity(hist, em);
  }

  /**
   * RDC tagged records
   * 
   * @param entityManager
   * @throws JsonGenerationException
   * @throws JsonMappingException
   * @throws IOException
   * @throws Exception
   */
  /*
   * public void monitorRDCTaggedRecords(EntityManager em) throws
   * JsonGenerationException, JsonMappingException, IOException, Exception {
   * 
   * // search the Rdc tagged records from Admin table String sql =
   * ExternalizedQuery.getSql("BATCH.MONITOR_RDC_TAGGED_REC"); PreparedQuery
   * query = new PreparedQuery(em, sql); List<Admin> rdctaggRecList =
   * query.getResults(Admin.class); this.log.debug("Size of rdctaggRecList : " +
   * rdctaggRecList.size()); String batchAction = "MonitorRDCOnly"; for (Admin
   * rdcTaggRec : rdctaggRecList) { try { String sysLoc = ""; sql =
   * ExternalizedQuery.getSql("BATCH.GET_DATA"); query = new PreparedQuery(em,
   * sql); query.setParameter("REQ_ID", rdcTaggRec.getId().getReqId());
   * List<Data> rdcTaggRecDataList = query.getResults(Data.class); for (Data
   * rdcTagRecData : rdcTaggRecDataList) { sysLoc =
   * rdcTagRecData.getCmrIssuingCntry(); }
   * log.info("Processing Rdc Tagged Record  [Request ID: " +
   * rdcTaggRec.getId().getReqId() + "]"); MassUpdateServiceInput
   * mssUpdateServiceInput = getMassReqParam(em, rdcTaggRec.getId().getReqId(),
   * rdcTaggRec.getReqType()); processMassUpdateServiceRdcTaggedRecord(em,
   * rdcTaggRec.getId().getReqId(), rdcTaggRec.getIterationId(), sysLoc,
   * mssUpdateServiceInput, batchAction, rdcTaggRec.getRdcProcessingStatus());
   * 
   * } catch (Exception e) {
   * this.log.error("Error in processing Rdc tagged Record for Request ID " +
   * rdcTaggRec.getId().getReqId() + " [" + e.getMessage() + "]"); } } }
   */

  /**
   * Create the createWfHist record
   * 
   * @param service
   * @param entityManager
   * @param reqId
   * @param reqStatus
   * @param lockBy
   * @param cmt
   * @param action
   * @throws CmrException
   * @throws SQLException
   */
  public void createWfHistory(BaseService<?, ?> service, EntityManager em, long reqId, String reqStatus, String lockBy, String cmt, String action)
      throws CmrException, SQLException {
    this.log.info("Creating wf_hist record  Req  for [Request ID: " + reqId + "]");
    WfHist hist = new WfHist();
    WfHistPK histpk = new WfHistPK();
    histpk.setWfId(SystemUtil.getNextID(em, SystemConfiguration.getValue("MANDT"), "WF_ID"));
    hist.setId(histpk);
    hist.setCmt(cmt);
    hist.setReqStatus(reqStatus);
    hist.setCreateById(lockBy);
    hist.setCreateByNm(lockBy);
    hist.setCreateTs(SystemUtil.getCurrentTimestamp());
    hist.setCompleteTs(SystemUtil.getCurrentTimestamp());
    hist.setReqStatusAct(action);
    hist.setReqId(reqId);
    service.createEntity(hist, em);
  }

  /**
   * Creates a basic Comment log record after batch receives the result
   * 
   * @param service
   * @param entityManager
   * 
   * @param user
   * @param req
   *          id
   * @param cmt
   * @throws CmrException
   * @throws SQLException
   */
  public void createCommentLogAfterProcess(BaseService<?, ?> service, EntityManager entityManager, String user, long reqId, String cmt)
      throws CmrException, SQLException {
    ReqCmtLog reqCmtLog = new ReqCmtLog();
    ReqCmtLogPK reqCmtLogpk = new ReqCmtLogPK();
    reqCmtLogpk.setCmtId(SystemUtil.getNextID(entityManager, SystemConfiguration.getValue("MANDT"), "CMT_ID"));
    reqCmtLog.setId(reqCmtLogpk);
    reqCmtLog.setReqId(reqId);
    reqCmtLog.setCmt(cmt);
    // save cmtlockedIn as Y default for current realese
    reqCmtLog.setCmtLockedIn(CmrConstants.CMT_LOCK_IND_YES);
    reqCmtLog.setCreateById(user);
    reqCmtLog.setCreateByNm(user);
    // set createTs as current timestamp and updateTs same as CreateTs
    reqCmtLog.setCreateTs(SystemUtil.getCurrentTimestamp());
    reqCmtLog.setUpdateTs(reqCmtLog.getCreateTs());
    service.createEntity(reqCmtLog, entityManager);
  }

  public void processingUpdateByEnterprise(EntityManager em, long reqId, int itrId, String sysLoc, String batchAction, String currentRDCProcStat)
      throws JsonGenerationException, JsonMappingException, IOException, Exception {

    String sql = ExternalizedQuery.getSql("BATCH.GET_ENTERPRISE_DATA");
    PreparedQuery query = new PreparedQuery(em, sql);
    List<Admin> manualRecList = query.getResults(Admin.class);
    this.log.debug("Size of enterprise list : " + manualRecList.size());
    Admin admin = null;
    MassUpdtData massUpdtData = null;
    EnterpriseUpdtResponse enterpriseUpdtResponse = null;
    DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    for (Admin manualRec : manualRecList) {
      try {
        log.info("Processing Enterprise Record " + manualRec.getId().getReqId() + " [Request ID: " + manualRec.getId().getReqId() + "]");
        sql = ExternalizedQuery.getSql("BATCH.MASS_UPDATE_DATA_RECORDS");
        query = new PreparedQuery(em, sql);
        query.setParameter("REQ_ID", manualRec.getId().getReqId());

        int iterationId = 0;
        String Status = "PASS";
        String massUpdtSql = "BATCH.MASS_UPDATE_DATA_RECORDS";
        String map = Admin.BATCH_UPDATE_DATA_MAPPING;
        PreparedQuery massUpdateDataQuery = new PreparedQuery(em, ExternalizedQuery.getSql(massUpdtSql));
        massUpdateDataQuery.setParameter("REQ_ID", manualRec.getId().getReqId());
        massUpdateDataQuery.setParameter("ITERATION_ID", iterationId);
        massUpdateDataQuery.setParameter("STATUS", Status);

        List<CompoundEntity> massUpdtDataResults = massUpdateDataQuery.getCompundResults(Admin.class, map);

        for (CompoundEntity data : massUpdtDataResults) {
          admin = data.getEntity(Admin.class);
          massUpdtData = data.getEntity(MassUpdtData.class);
        }

        EnterpriseUpdtRequest enterpriseUpdtRequest = generateEnterpriseRequest(em, reqId, sysLoc, admin, massUpdtData);

        this.log.trace("Request JSON:");
        if (this.log.isTraceEnabled()) {
          DebugUtil.printObjectAsJson(this.log, enterpriseUpdtRequest);
        }

        UpdateByEntClient updateByEntClient = CmrServicesFactory.getInstance().createClient(BATCH_SERVICES_URL, UpdateByEntClient.class);

        try {
          updateByEntClient.setReadTimeout(60 * 20 * 1000); // 20 mins
          enterpriseUpdtResponse = updateByEntClient.executeAndWrap(enterpriseUpdtRequest, EnterpriseUpdtResponse.class);
        } catch (Exception e) {
          this.log.error("Error when connecting to the enterprise service.", e);
          enterpriseUpdtResponse = new EnterpriseUpdtResponse();
          enterpriseUpdtResponse.setStatus(CmrConstants.RDC_STATUS_ABORTED);
          enterpriseUpdtResponse.setMessage("A system error has occured. Setting to aborted.");
        }

        this.log.trace("Response JSON:");
        if (this.log.isTraceEnabled()) {
          DebugUtil.printObjectAsJson(this.log, enterpriseUpdtResponse);
        }
        this.log.info("Response received from Enterprise Service [Request ID: " + enterpriseUpdtResponse.getReqId() + " Status: "
            + enterpriseUpdtResponse.getStatus() + " Message: "
            + (enterpriseUpdtResponse.getMessage() != null ? enterpriseUpdtResponse.getMessage() : "-") + "]");

      } catch (Exception e) {
        this.log.error("Error in processing Enterprise Record " + manualRec.getId().getReqId() + " for Request ID " + manualRec.getId().getReqId()
            + " [" + e.getMessage() + "]");
      }

      // get the results from the service and process jason response
      admin = null;

      try {
        String currentTsStr = formatter.format(SystemUtil.getCurrentTimestamp());
        Date currentTs = formatter.parse(currentTsStr);

        // update the Admin table for all the update responses
        PreparedQuery updAdminQry = new PreparedQuery(em, ExternalizedQuery.getSql("BATCH.GET_ADMIN_ENTITY"));
        updAdminQry.setParameter("REQ_ID", reqId);
        List<Admin> adminList = updAdminQry.getResults(Admin.class);
        for (Admin adminEntity : adminList) {
          if (batchAction.equalsIgnoreCase("MonitorAbortedRecords") && currentRDCProcStat.equalsIgnoreCase(CmrConstants.RDC_STATUS_ABORTED)
              && enterpriseUpdtResponse.getStatus().equalsIgnoreCase(CmrConstants.RDC_STATUS_ABORTED)) {
            adminEntity.setRdcProcessingStatus(CmrConstants.RDC_STATUS_NOT_COMPLETED);
          } else {
            adminEntity.setRdcProcessingStatus(enterpriseUpdtResponse.getStatus());
          }
          adminEntity.setRdcProcessingMsg(enterpriseUpdtResponse.getMessage());
          adminEntity.setRdcProcessingTs(currentTs);
          updateEntity(adminEntity, em);
          admin = adminEntity;
        }
      } catch (Exception e) {
        log.error("Error in processing Admin and ADDR Updates for Create Request " + " [" + e.getMessage() + "]", e);
        throw e;
      }

      // create comment log and workflow entries for update type of request
      // set the action for mass request
      String actionWf = "System Action:RDc Enterprise update";
      String requestTypeStr = "Enterprise Update";

      if (CmrConstants.RDC_STATUS_COMPLETED.equalsIgnoreCase(enterpriseUpdtResponse.getStatus())) {
        StringBuilder comment = new StringBuilder();
        comment = comment.append(requestTypeStr + " in RDc successfully completed.");
        createCommentLogAfterProcess(this, em, SystemConfiguration.getValue("BATCH_USERID"), reqId, comment.toString());
        createWfHistory(this, em, reqId, admin.getReqStatus(), SystemConfiguration.getValue("BATCH_USERID"), comment.toString(), actionWf);
      } else if (CmrConstants.RDC_STATUS_COMPLETED_WITH_WARNINGS.equalsIgnoreCase(enterpriseUpdtResponse.getStatus())) {
        StringBuilder comment = new StringBuilder();
        comment = comment.append(requestTypeStr + " in RDc completed with warning: " + enterpriseUpdtResponse.getMessage());
        createCommentLogAfterProcess(this, em, SystemConfiguration.getValue("BATCH_USERID"), reqId, comment.toString());
        createWfHistory(this, em, reqId, admin.getReqStatus(), SystemConfiguration.getValue("BATCH_USERID"), comment.toString(), actionWf);
      } else if (batchAction.equalsIgnoreCase("MonitorAbortedRecords") && currentRDCProcStat.equalsIgnoreCase(CmrConstants.RDC_STATUS_ABORTED)
          && enterpriseUpdtResponse.getStatus().equalsIgnoreCase(CmrConstants.RDC_STATUS_ABORTED)) {
        StringBuilder comment = new StringBuilder();
        comment = comment.append(requestTypeStr + " in RDc failed: " + enterpriseUpdtResponse.getMessage());
        createCommentLogAfterProcess(this, em, SystemConfiguration.getValue("BATCH_USERID"), reqId, comment.toString());
        createWfHistory(this, em, reqId, admin.getReqStatus(), SystemConfiguration.getValue("BATCH_USERID"), comment.toString(), actionWf);
      } else if (CmrConstants.RDC_STATUS_ABORTED.equalsIgnoreCase(enterpriseUpdtResponse.getStatus())) {
        StringBuilder comment = new StringBuilder();
        comment = comment.append("RDC processing aborted with error: " + enterpriseUpdtResponse.getMessage() + ". System will retry once.");
        createCommentLogAfterProcess(this, em, SystemConfiguration.getValue("BATCH_USERID"), reqId, comment.toString());
        createWfHistory(this, em, reqId, admin.getReqStatus(), SystemConfiguration.getValue("BATCH_USERID"), comment.toString(), actionWf);
      } else if (CmrConstants.RDC_STATUS_NOT_COMPLETED.equalsIgnoreCase(enterpriseUpdtResponse.getStatus())) {
        StringBuilder comment = new StringBuilder();
        comment = comment.append(requestTypeStr + " in RDc failed: " + enterpriseUpdtResponse.getMessage());
        createCommentLogAfterProcess(this, em, SystemConfiguration.getValue("BATCH_USERID"), reqId, comment.toString());
        createWfHistory(this, em, reqId, admin.getReqStatus(), SystemConfiguration.getValue("BATCH_USERID"), comment.toString(), actionWf);
      }

      partialCommit(em);

    }
  }

  public EnterpriseUpdtRequest generateEnterpriseRequest(EntityManager em, long reqId, String sysLoc, Admin admin, MassUpdtData massUpdtData)
      throws JsonGenerationException, JsonMappingException, IOException, Exception {
    EnterpriseUpdtRequest enterpriseUpdtRequest = new EnterpriseUpdtRequest();

    enterpriseUpdtRequest.setMandt(SystemConfiguration.getValue("MANDT"));
    enterpriseUpdtRequest.setReqId(reqId);
    enterpriseUpdtRequest.setReqType(admin.getReqType());
    enterpriseUpdtRequest.setUserId(SystemConfiguration.getValue("BATCH_USERID"));
    enterpriseUpdtRequest.setEntUpdtType(massUpdtData.getEntpUpdtTyp());
    enterpriseUpdtRequest.setEnterprise(massUpdtData.getEnterprise());
    enterpriseUpdtRequest.setCompany(massUpdtData.getCompany());
    enterpriseUpdtRequest.setNewEnterprise(massUpdtData.getNewEntp());
    enterpriseUpdtRequest.setNewEnterpriseName1(massUpdtData.getNewEntpName1());
    enterpriseUpdtRequest.setNewEnterpriseName2(massUpdtData.getNewEntpName2());
    enterpriseUpdtRequest.setSysLoc(sysLoc);

    return enterpriseUpdtRequest;
  }
}