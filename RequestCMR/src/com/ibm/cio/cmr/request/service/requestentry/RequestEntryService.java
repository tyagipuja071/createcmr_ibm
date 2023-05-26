/**
 * 
 */
package com.ibm.cio.cmr.request.service.requestentry;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.springframework.stereotype.Component;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.automation.RequestData;
import com.ibm.cio.cmr.request.automation.util.AutomationConst;
import com.ibm.cio.cmr.request.automation.util.RequestChangeContainer;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.AdminPK;
import com.ibm.cio.cmr.request.entity.Attachment;
import com.ibm.cio.cmr.request.entity.CmrInternalTypes;
import com.ibm.cio.cmr.request.entity.CompoundEntity;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.entity.Kna1;
import com.ibm.cio.cmr.request.entity.ProcCenter;
import com.ibm.cio.cmr.request.entity.ProlifChecklist;
import com.ibm.cio.cmr.request.entity.ProlifChecklistPK;
import com.ibm.cio.cmr.request.entity.Scorecard;
import com.ibm.cio.cmr.request.entity.StatusTrans;
import com.ibm.cio.cmr.request.entity.StatusTransPK;
import com.ibm.cio.cmr.request.entity.ValidationUrl;
import com.ibm.cio.cmr.request.model.BaseModel;
import com.ibm.cio.cmr.request.model.approval.ApprovalResponseModel;
import com.ibm.cio.cmr.request.model.code.ValidateFiscalDataModel;
import com.ibm.cio.cmr.request.model.requestentry.AddressModel;
import com.ibm.cio.cmr.request.model.requestentry.AttachmentModel;
import com.ibm.cio.cmr.request.model.requestentry.AutoDNBDataModel;
import com.ibm.cio.cmr.request.model.requestentry.CheckListModel;
import com.ibm.cio.cmr.request.model.requestentry.DataModel;
import com.ibm.cio.cmr.request.model.requestentry.FindCMRRecordModel;
import com.ibm.cio.cmr.request.model.requestentry.FindCMRResultModel;
import com.ibm.cio.cmr.request.model.requestentry.NotifyListModel;
import com.ibm.cio.cmr.request.model.requestentry.RequestEntryModel;
import com.ibm.cio.cmr.request.model.requestentry.SubindustryIsicSearchModel;
import com.ibm.cio.cmr.request.model.requestentry.ValidationUrlModel;
import com.ibm.cio.cmr.request.model.window.UpdatedNameAddrModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.BaseService;
import com.ibm.cio.cmr.request.service.CmrClientService;
import com.ibm.cio.cmr.request.service.approval.ApprovalService;
import com.ibm.cio.cmr.request.ui.PageManager;
import com.ibm.cio.cmr.request.user.AppUser;
import com.ibm.cio.cmr.request.util.BluePagesHelper;
import com.ibm.cio.cmr.request.util.JpaManager;
import com.ibm.cio.cmr.request.util.MessageUtil;
import com.ibm.cio.cmr.request.util.Person;
import com.ibm.cio.cmr.request.util.RequestUtils;
import com.ibm.cio.cmr.request.util.SystemLocation;
import com.ibm.cio.cmr.request.util.SystemUtil;
import com.ibm.cio.cmr.request.util.dnb.DnBUtil;
import com.ibm.cio.cmr.request.util.geo.GEOHandler;
import com.ibm.cio.cmr.request.util.geo.impl.CNDHandler;
import com.ibm.cio.cmr.request.util.geo.impl.CNHandler;
import com.ibm.cio.cmr.request.util.geo.impl.LAHandler;
import com.ibm.cmr.services.client.AutomationServiceClient;
import com.ibm.cmr.services.client.CmrServicesFactory;
import com.ibm.cmr.services.client.ServiceClient.Method;
import com.ibm.cmr.services.client.automation.AutomationResponse;
import com.ibm.cmr.services.client.automation.ap.nz.NZBNValidationRequest;
import com.ibm.cmr.services.client.automation.ap.nz.NZBNValidationResponse;
import com.ibm.cmr.services.client.dnb.DnBCompany;
import com.ibm.cmr.services.client.dnb.DnbData;
import com.ibm.cmr.services.client.dnb.DnbOrganizationId;
import com.ibm.cmr.services.client.matching.MatchingResponse;
import com.ibm.cmr.services.client.matching.dnb.DnBMatchingResponse;

/**
 * Main Service for request entry
 * 
 * @author Jeffrey Zamora
 * 
 */
@Component
public class RequestEntryService extends BaseService<RequestEntryModel, CompoundEntity> {

  private final AdminService adminService = new AdminService();
  private final DataService dataService = new DataService();
  private final ScorecardService scorecardService = new ScorecardService();
  private final ApprovalService approvalService = new ApprovalService();
  private static final String STATUS_CHG_CMT_PRE_PREFIX = "ACTION \"";
  private static final String STATUS_CHG_CMT_MID_PREFIX = "\" changed the REQUEST STATUS to \"";
  private static final String STATUS_CHG_CMT_POST_PREFIX = "\"" + "\n ";
  private CheckListModel checklist;

  @Override
  protected Logger initLogger() {
    return Logger.getLogger(RequestEntryService.class);
  }

  @Override
  protected void performTransaction(RequestEntryModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {

    String action = model.getAction();
    if (CmrConstants.Save().equalsIgnoreCase(action) || "REJECT_SEARCH".equalsIgnoreCase(action) || "NO_DATA_SEARCH".equalsIgnoreCase(action)
        || "CREATE_NEW".equalsIgnoreCase(action)) {
      performSave(model, entityManager, request, false);
    } else if ("DPL".equalsIgnoreCase(action)) {
      performSave(model, entityManager, request, true);
    } else if ("VERIFY_COMPANY".equalsIgnoreCase(action)) {
      performSave(model, entityManager, request, true);
    } else if ("VERIFY_SCENARIO".equalsIgnoreCase(action)) {
      performSave(model, entityManager, request, true);
    } else if ("OVERRIDE_DNB".equalsIgnoreCase(action)) {
      performSave(model, entityManager, request, true);
    } else if ("CONFIRM_DOC_UPD".equalsIgnoreCase(action)) {
      updateDeprecatedAttachmentTypes(model, entityManager, request);
    } else if ("RECREATE".equalsIgnoreCase(action)) {
      recreateCMR(model, entityManager, request);
    } else {
      // Claim conditionally approved request (Edit Request)
      /*
       * if (CmrConstants.Claim().equalsIgnoreCase(action) &&
       * CmrConstants.APPROVAL_RESULT_COND_APPROVED
       * .equals(model.getApprovalResult())) { ApprovalService service = new
       * ApprovalService(); service.updateApprovals(entityManager,
       * "EDIT_REQUEST", model.getReqId(), AppUser.getUser(request)); }
       */
      StatusTrans trans = getStatusTransition(entityManager, model);
      if (trans == null) {
        return; // no transition, no processing needed
      }
      if (CmrConstants.Claim().equalsIgnoreCase(action)) {
        // 1150209: throw an error if request has been claimed already, JZ Mar
        // 2, 2017
        if (canClaim(entityManager, model)) {
          throw new CmrException(MessageUtil.ERROR_CLAIMED_ALREADY);
        }
        performGenericAction(trans, model, entityManager, request, null);
        RequestUtils.addToNotifyList(this, entityManager, AppUser.getUser(request), model.getReqId());
      } else if (CmrConstants.Unlock().equalsIgnoreCase(action)) {
        performGenericAction(trans, model, entityManager, request, null);
      } else if (CmrConstants.Send_for_Processing().equalsIgnoreCase(action)) {
        performSendForProcessing(trans, model, entityManager, request);
      } else if (CmrConstants.Processing_Validation_Complete().equalsIgnoreCase(action)
          || CmrConstants.Processing_Validation_Complete2().equalsIgnoreCase(action)) {
        performGenericAction(trans, model, entityManager, request, null);
      } else if (CmrConstants.Processing_Create_Up_Complete().equalsIgnoreCase(action)) {
        performGenericAction(trans, model, entityManager, request, null);
      } else if (CmrConstants.All_Processing_Complete().equalsIgnoreCase(action)) {
        performGenericAction(trans, model, entityManager, request, null, null, true);
      } else if (CmrConstants.Reject().equalsIgnoreCase(action)) {
        performGenericAction(trans, model, entityManager, request, null, null, false);
        processObsoleteApprovals(entityManager, model.getReqId(), AppUser.getUser(request));
      } else if (CmrConstants.Cancel_Processing().equalsIgnoreCase(action)) {
        performGenericAction(trans, model, entityManager, request, null);
      } else if (CmrConstants.Create_Update_CMR().equalsIgnoreCase(action)) {
        performGenericAction(trans, model, entityManager, request, null);
      } else if (CmrConstants.Create_Update_Approved().equalsIgnoreCase(action)) {
        performGenericAction(trans, model, entityManager, request, null);
      } else if (CmrConstants.Reprocess_Checks().equalsIgnoreCase(action)) {
        performGenericAction(trans, model, entityManager, request, null);
      } else if (CmrConstants.Cancel_Request().equalsIgnoreCase(action)) {
        performCancelRequest(trans, model, entityManager, request);
      } else if (CmrConstants.Reprocess_Rdc().equalsIgnoreCase(action)) {
        performReprocessRdcRequest(trans, model, entityManager, request);
      }
    }
  }

  private void performReprocessRdcRequest(StatusTrans trans, RequestEntryModel model, EntityManager entityManager, HttpServletRequest request)
      throws Exception {

    AppUser user = AppUser.getUser(request);
    CompoundEntity entity = getCurrentRecord(model, entityManager, request);

    Admin admin = entity.getEntity(Admin.class);

    // update the Admin record
    admin = entity.getEntity(Admin.class);
    admin.setLastUpdtBy(user.getIntranetId());
    admin.setLastUpdtTs(SystemUtil.getCurrentTimestamp());
    admin.setReqStatus(trans.getNewReqStatus());

    if (CmrConstants.YES_NO.N.toString().equals(trans.getNewLockedInd())
        && CmrConstants.YES_NO.Y.toString().equals(trans.getId().getCurrLockedInd())) {
      // request to be unlocked
      RequestUtils.clearClaimDetails(admin);
    }

    if (StringUtils.isEmpty(admin.getLockInd())) {
      admin.setLockInd(CmrConstants.YES_NO.N.toString());
    }
    if (StringUtils.isEmpty(admin.getProcessedFlag())) {
      admin.setLockInd(CmrConstants.YES_NO.N.toString());
    }
    if (StringUtils.isEmpty(admin.getDisableAutoProc())) {
      admin.setDisableAutoProc(CmrConstants.YES_NO.N.toString());
    }
    if (!PageManager.autoProcEnabled(model.getCmrIssuingCntry(), model.getReqType())) {
      admin.setDisableAutoProc(CmrConstants.YES_NO.Y.toString());
    }
    saveAccessToken(admin, request);
    setLockByName(admin);
    updateEntity(admin, entityManager);
    computeInternalType(entityManager, model, admin);
  }

  private void updateDeprecatedAttachmentTypes(RequestEntryModel model, EntityManager entityManager, HttpServletRequest request) {

    long reqId = model.getReqId();
    if (reqId > 0) {
      String sql = ExternalizedQuery.getSql("AUTO.US.GET_ATTACHMENTS");
      PreparedQuery query = new PreparedQuery(entityManager, sql);
      query.setParameter("REQ_ID", reqId);
      List<Attachment> attachments = query.getResults(Attachment.class);
      if (attachments != null && attachments.size() > 0) {
        for (Attachment attachment : attachments) {
          if ("NAM".equals(attachment.getDocContent()) || "ADDR".equals(attachment.getDocContent())) {
            attachment.setDocContent("COMP");
            log.debug("Updated attachment type for attachment - " + attachment.getId().getDocLink());
            entityManager.merge(attachment);
          }
        }
      }

    }

  }

  /**
   * Saves the request. This method handles both Create and Update
   * 
   * @param model
   * @param entityManager
   * @param request
   * @throws Exception
   */
  public void performSave(RequestEntryModel model, EntityManager entityManager, HttpServletRequest request, boolean noScorecard) throws Exception {
    AppUser user = AppUser.getUser(request);

    GEOHandler geoHandler = RequestUtils.getGEOHandler(model.getCmrIssuingCntry());
    if (SystemLocation.JAPAN.equals(model.getCmrIssuingCntry())) {
      String[] nameArray = geoHandler.dividingCustName1toName2(model.getMainCustNm1(), model.getMainCustNm2());
      if (nameArray != null && nameArray.length == 2) {
        model.setMainCustNm1(nameArray[0]);
        model.setMainCustNm2(nameArray[1]);
      }
    }
    Admin adminToUse = null;
    if (BaseModel.STATE_NEW == model.getState()) {
      CompoundEntity entity = createFromModel(model, entityManager, request);
      long reqId = SystemUtil.getNextID(entityManager, SystemConfiguration.getValue("MANDT"), "REQ_ID");

      // create the Admin record
      Admin admin = entity.getEntity(Admin.class);
      admin.getId().setReqId(reqId);
      admin.setReqStatus(CmrConstants.REQUEST_STATUS.DRA.toString());
      admin.setRequesterId(user.getIntranetId());
      admin.setRequesterNm(user.getBluePagesName());
      admin.setCreateTs(SystemUtil.getCurrentTimestamp());
      admin.setLastUpdtBy(user.getIntranetId());
      admin.setLastUpdtTs(SystemUtil.getCurrentTimestamp());
      admin.setProcessedFlag(CmrConstants.YES_NO.N.toString());
      admin.setCovBgRetrievedInd(CmrConstants.YES_NO.N.toString());
      String sysType = SystemConfiguration.getValue("SYSTEM_TYPE");
      admin.setWaitInfoInd(!StringUtils.isBlank(sysType) ? sysType.substring(0, 1) : null);
      RequestUtils.setClaimDetails(admin, request);
      // clear cmt value as it is saved in new table .

      if (geoHandler != null) {
        geoHandler.setAdminDefaultsOnCreate(admin);
      }

      if (!PageManager.autoProcEnabled(model.getCmrIssuingCntry(), model.getReqType())) {
        admin.setDisableAutoProc(CmrConstants.YES_NO.Y.toString());
      }

      saveAccessToken(admin, request);
      createEntity(admin, entityManager);
      adminToUse = admin;

      // create the Data record
      Data data = entity.getEntity(Data.class);
      data.getId().setReqId(reqId);

      if (geoHandler != null) {
        geoHandler.setDataDefaultsOnCreate(data, entityManager);
        geoHandler.handleImportByType(admin.getReqType(), admin, data, false);
      }
      createEntity(data, entityManager);

      // create the Scorecard record
      Scorecard score = entity.getEntity(Scorecard.class);
      score.getId().setReqId(reqId);
      if (!StringUtils.isEmpty(model.getSaveRejectScore())) {
        String system = model.getSaveRejectScore();
        if ("dnb".equals(system)) {
          score.setFindDnbUsrNm(user.getBluePagesName());
          score.setFindDnbUsrId(user.getIntranetId());
          score.setFindDnbTs(SystemUtil.getCurrentTimestamp());
          if ("NO_DATA_SEARCH".equals(model.getAction())) {
            score.setFindDnbResult(CmrConstants.RESULT_NO_RESULT);
          } else {
            score.setFindDnbResult(CmrConstants.RESULT_REJECTED);
          }
        } else {
          score.setFindCmrUsrNm(user.getBluePagesName());
          score.setFindCmrUsrId(user.getIntranetId());
          score.setFindCmrTs(SystemUtil.getCurrentTimestamp());
          if ("NO_DATA_SEARCH".equals(model.getAction())) {
            score.setFindCmrResult(CmrConstants.RESULT_NO_RESULT);
          } else {
            score.setFindCmrResult(CmrConstants.RESULT_REJECTED);
          }
        }
      }
      if (StringUtils.isBlank(score.getDplChkResult())) {
        score.setDplChkResult(CmrConstants.Scorecard_Not_Done);
        score.setFindCmrResult(CmrConstants.Scorecard_Not_Done);
        score.setFindDnbResult(CmrConstants.Scorecard_Not_Done);
      }
      // if (StringUtils.isNotEmpty(data.getVatInd()) &&
      // "N".equals(data.getVatInd()))
      // score.setVatAcknowledge(CmrConstants.Scorecard_YES);
      // else
      // score.setVatAcknowledge(CmrConstants.Scorecard_NA);
      createEntity(score, entityManager);

      if (geoHandler != null && geoHandler.hasChecklist(model.getCmrIssuingCntry())) {
        saveChecklist(entityManager, reqId, user);
      }
      RequestUtils.createWorkflowHistory(this, entityManager, request, admin, "AUTO: Request created.", CmrConstants.Save());
      RequestUtils.addToNotifyList(this, entityManager, user, reqId);

      model.setReqId(reqId);
    } else {
      CompoundEntity entity = getCurrentRecord(model, entityManager, request);
      boolean clearCmrNoAndSap = false;
      if (entity != null) {
        Admin admin = entity.getEntity(Admin.class);
        if (admin != null && "U".equals(admin.getReqType()) && "C".equals(model.getReqType())) {
          clearCmrNoAndSap = true;
        }
        processDplReset(entityManager, model, admin);
      }
      copyValuesToEntity(model, entity);

      if (noScorecard) {
        Scorecard score = entity.getEntity(Scorecard.class);
        entityManager.detach(score);
      }
      // detachScoreCard(entityManager, entity);

      // create the Admin record
      Admin admin = entity.getEntity(Admin.class);
      admin.setLastUpdtBy(user.getIntranetId());
      admin.setLastUpdtTs(SystemUtil.getCurrentTimestamp());
      admin.setWarnMsgSentDt(null);

      if (StringUtils.isEmpty(admin.getLockInd())) {
        admin.setLockInd(CmrConstants.YES_NO.N.toString());
      }
      if (StringUtils.isEmpty(admin.getProcessedFlag())) {
        admin.setLockInd(CmrConstants.YES_NO.N.toString());
      }
      if (StringUtils.isEmpty(admin.getDisableAutoProc())) {
        admin.setDisableAutoProc(CmrConstants.YES_NO.N.toString());
      }

      // clear cmt value as it is saved in new table .

      // always clear the values when status is changed to PCP
      if ("PCP".equals(admin.getReqStatus())) {
        admin.setProcessedFlag(CmrConstants.YES_NO.N.toString());
        admin.setProcessedTs(null);
      }
      if (!PageManager.autoProcEnabled(model.getCmrIssuingCntry(), model.getReqType())) {
        admin.setDisableAutoProc(CmrConstants.YES_NO.Y.toString());
      }

      if (geoHandler != null) {
        geoHandler.doBeforeAdminSave(entityManager, admin, model.getCmrIssuingCntry());
      }

      Data data = entity.getEntity(Data.class);

      setLockByName(admin);
      saveAccessToken(admin, request);
      RequestUtils.setProspLegalConversionFlag(entityManager, admin, data);
      updateEntity(admin, entityManager);

      adminToUse = admin;
      // create the Data record
      if (clearCmrNoAndSap) {
        data.setCmrNo(null);
      }

      // 1164429
      // Will call this for mrcIsuClientTierLogic during manual creation for LA
      if (null != geoHandler && LAHandler.isLACountry(model.getCmrIssuingCntry())) {
        geoHandler.setDataDefaultsOnCreate(data, entityManager);
        geoHandler.handleImportByType(admin.getReqType(), admin, data, false);
      }

      if (geoHandler != null) {
        geoHandler.doBeforeDataSave(entityManager, admin, data, model.getCmrIssuingCntry());
      }
      updateEntity(data, entityManager);
      if (!noScorecard) {
        if (!StringUtils.isEmpty(model.getSaveRejectScore())) {
          String system = model.getSaveRejectScore();
          Scorecard score = entity.getEntity(Scorecard.class);
          if ("dnb".equals(system)) {
            score.setFindDnbUsrNm(user.getBluePagesName());
            score.setFindDnbUsrId(user.getIntranetId());
            score.setFindDnbTs(SystemUtil.getCurrentTimestamp());
            if ("NO_DATA_SEARCH".equals(model.getAction())) {
              score.setFindDnbResult(CmrConstants.RESULT_NO_RESULT);
            } else {
              score.setFindDnbResult(CmrConstants.RESULT_REJECTED);
            }
          } else {
            score.setFindCmrUsrNm(user.getBluePagesName());
            score.setFindCmrUsrId(user.getIntranetId());
            score.setFindCmrTs(SystemUtil.getCurrentTimestamp());
            if ("NO_DATA_SEARCH".equals(model.getAction())) {
              score.setFindCmrResult(CmrConstants.RESULT_NO_RESULT);
            } else {
              score.setFindCmrResult(CmrConstants.RESULT_REJECTED);
            }
          }
          updateEntity(score, entityManager);
        }
      }

      if (geoHandler != null && geoHandler.hasChecklist(model.getCmrIssuingCntry())) {
        saveChecklist(entityManager, model.getReqId(), user);
      }

      if (clearCmrNoAndSap) {
        clearCMRNoAndSap(entityManager, model.getReqId());
      }
      // save comment in req_cmt_log table while updating a request .
      // save only if it is not null or not blank
      if (null != model.getCmt() && !model.getCmt().isEmpty()) {
        // RequestUtils.createCommentLog(this, entityManager, user,
        // model.getReqId(), model.getCmt());
      }

    }

    // compute the internal type, ensure value is saved to db first before
    // executing
    computeInternalType(entityManager, model, adminToUse);
  }

  public void computeInternalType(EntityManager entityManager, RequestEntryModel model, Admin adminToUse) {
    if (adminToUse != null) {
      String reqType = model.getReqType();
      String cmrIssuingCountry = model.getCmrIssuingCntry();
      CmrInternalTypes type = RequestUtils.computeInternalType(entityManager, reqType, cmrIssuingCountry, adminToUse.getId().getReqId());
      if (type != null) {
        adminToUse.setInternalTyp(type.getId().getInternalTyp());
        adminToUse.setSepValInd(type.getSepValInd());
      }
      updateEntity(adminToUse, entityManager);
    }
  }

  private void clearCMRNoAndSap(EntityManager entityManager, long reqId) {
    PreparedQuery query = new PreparedQuery(entityManager, ExternalizedQuery.getSql("REQUESTENTRY.CLEARSAPNO"));
    query.setParameter("REQ_ID", reqId);
    query.executeSql();

    query = new PreparedQuery(entityManager, ExternalizedQuery.getSql("REQUESTENTRY.CLEARADDRRESULT"));
    query.setParameter("REQ_ID", reqId);
    query.executeSql();

    query = new PreparedQuery(entityManager, ExternalizedQuery.getSql("REQUESTENTRY.CLEARSAPNO_RDC"));
    query.setParameter("REQ_ID", reqId);
    query.executeSql();

  }

  protected void performGenericAction(StatusTrans trans, RequestEntryModel model, EntityManager entityManager, HttpServletRequest request,
      String processingCenter) throws Exception {
    performGenericAction(trans, model, entityManager, request, null, null, false);
  }

  protected void performGenericAction(StatusTrans trans, RequestEntryModel model, EntityManager entityManager, HttpServletRequest request,
      String sendToId, String sendToNm, boolean complete) throws Exception {
    performGenericAction(trans, model, entityManager, request, sendToId, sendToNm, complete, true);
  }

  /**
   * Generic Action
   * 
   * @param model
   * @param entityManager
   * @param request
   * @throws Exception
   */
  protected void performGenericAction(StatusTrans trans, RequestEntryModel model, EntityManager entityManager, HttpServletRequest request,
      String sendToId, String sendToNm, boolean complete, boolean transitionToNext) throws Exception {
    AppUser user = AppUser.getUser(request);
    CompoundEntity entity = getCurrentRecord(model, entityManager, request);
    Scorecard scorecard = entity.getEntity(Scorecard.class);
    Admin admin = entity.getEntity(Admin.class);
    String lockedBy = "";
    String lockedByNm = "";
    String processingStatus = "";

    if (LAHandler.isLACountry(model.getCmrIssuingCntry())) {
      processDplReset(entityManager, model, admin);
    } else {
      processDplReset(entityManager, model, admin);
    }

    copyValuesToEntity(model, entity);
    GEOHandler geoHandler = RequestUtils.getGEOHandler(model.getCmrIssuingCntry());

    // update the Admin record
    admin = entity.getEntity(Admin.class);
    admin.setLastUpdtBy(user.getIntranetId());
    admin.setLastUpdtTs(SystemUtil.getCurrentTimestamp());
    admin.setWarnMsgSentDt(null);

    lockedBy = admin.getLockBy();
    lockedByNm = admin.getLockByNm();
    processingStatus = admin.getProcessedFlag();
    boolean cnSendToPPNFlag = false;
    if (SystemLocation.CHINA.equals(model.getCmrIssuingCntry()) && "DRA".equals(admin.getReqStatus()) && "PPN".equals(trans.getNewReqStatus())) {
      cnSendToPPNFlag = true;
    }
    if (transitionToNext && !cnSendToPPNFlag) {
      admin.setReqStatus(trans.getNewReqStatus());
    }

    // always clear the values when status is changed to PCP
    if (transitionToNext && "PCP".equals(trans.getNewReqStatus())) {
      admin.setProcessedFlag(CmrConstants.YES_NO.N.toString());
      admin.setProcessedTs(null);
      if (geoHandler != null && LAHandler.isLACountry(model.getCmrIssuingCntry())) {
        boolean crosCompleted = reqIsCrosCompleted(entityManager, admin.getId().getReqId());
        if (crosCompleted) {
          this.log.debug("Setting to PCO:" + trans.getNewReqStatus());
          admin.setReqStatus("PCO");
        }
      }
    }

    if (transitionToNext) {
      if (CmrConstants.YES_NO.Y.toString().equals(trans.getNewLockedInd())
          && CmrConstants.YES_NO.N.toString().equals(trans.getId().getCurrLockedInd())) {
        // the request is to be locked
        RequestUtils.setClaimDetails(admin, request);
      } else if (CmrConstants.YES_NO.N.toString().equals(trans.getNewLockedInd())
          && CmrConstants.YES_NO.Y.toString().equals(trans.getId().getCurrLockedInd())) {
        // request to be unlocked
        RequestUtils.clearClaimDetails(admin);
      }
    }
    if (transitionToNext && sendToId != null) {
      admin.setLastProcCenterNm(sendToId);
    }

    if (StringUtils.isEmpty(admin.getLockInd())) {
      admin.setLockInd(CmrConstants.YES_NO.N.toString());
    }
    if (StringUtils.isEmpty(admin.getProcessedFlag())) {
      admin.setLockInd(CmrConstants.YES_NO.N.toString());
    }
    if (StringUtils.isEmpty(admin.getDisableAutoProc())) {
      admin.setDisableAutoProc(CmrConstants.YES_NO.N.toString());
    }
    // set cmt as blank

    if (!PageManager.autoProcEnabled(model.getCmrIssuingCntry(), model.getReqType())) {
      admin.setDisableAutoProc(CmrConstants.YES_NO.Y.toString());
    }

    if (geoHandler != null) {
      geoHandler.doBeforeAdminSave(entityManager, admin, model.getCmrIssuingCntry());
    }

    // update the Data record
    Data data = entity.getEntity(Data.class);

    saveAccessToken(admin, request);
    setLockByName(admin);
    RequestUtils.setProspLegalConversionFlag(entityManager, admin, data);
    updateEntity(admin, entityManager);

    if (CmrConstants.REQ_TYPE_UPDATE.equals(model.getReqType()) && LAHandler.isLACountry(model.getCmrIssuingCntry())
        && CmrConstants.Create_Update_CMR().equals(model.getAction())) {
      LAHandler.doDPLNotDone(String.valueOf(model.getReqId()), entityManager, model.getAction(), admin, lockedBy, lockedByNm, processingStatus);
    }

    if (geoHandler != null && LAHandler.isLACountry(model.getCmrIssuingCntry())) {
      geoHandler.setDataDefaultsOnCreate(data, entityManager);
      geoHandler.doBeforeDataSave(entityManager, admin, data, model.getCmrIssuingCntry());
    } else if (geoHandler != null) {
      geoHandler.doBeforeDataSave(entityManager, admin, data, model.getCmrIssuingCntry());
      if (SystemLocation.UNITED_STATES.equals(model.getCmrIssuingCntry()) && "CreateCMR-BP".equals(admin.getSourceSystId())
          && StringUtils.isNotEmpty(model.getStatusChgCmt()) && model.getStatusChgCmt().length() >= 20
          && model.getStatusChgCmt().startsWith("IBMDIRECT_CMR")) {
        data.setCmrNo2(model.getStatusChgCmt().substring(13, 20));
        model.setStatusChgCmt(model.getStatusChgCmt().substring(20));
      }
    }
    updateEntity(data, entityManager);

    long reqId = model.getReqId();
    RequestData requestData = new RequestData(entityManager, reqId);
    Addr addr = requestData.getAddress("ZS01");
    // Scorecard vat acknowledge initialize, if the data.vatInd=N,
    // then set scorecard.vatAcknowledge=Yes
    if (addr == null)
      addr = new Addr();
    boolean iscrossBorder = isCrossBorder(entityManager, model.getCmrIssuingCntry(), addr.getLandCntry());

    if (StringUtils.isBlank(scorecard.getVatAcknowledge()) && CmrConstants.CROSS_BORDER_COUNTRIES_GROUP1.contains(model.getCmrIssuingCntry())) {
      String oldVatValue = getOldVatValue(entityManager, reqId);
      if (admin.getReqType().equals("C")) {
        if ("N".equals(data.getVatInd()) && (!iscrossBorder)) {
          scorecard.setVatAcknowledge(CmrConstants.VAT_ACKNOWLEDGE_YES);
        } else
          scorecard.setVatAcknowledge(CmrConstants.VAT_ACKNOWLEDGE_NA);
      }
      if (admin.getReqType().equals("U")) {
        if ("N".equals(data.getVatInd()) && (!iscrossBorder) && (oldVatValue == null || oldVatValue.isEmpty())) {
          scorecard.setVatAcknowledge(CmrConstants.VAT_ACKNOWLEDGE_YES);
        } else if ("N".equals(data.getVatInd()) && (!iscrossBorder) && (oldVatValue != null)) {
          scorecard.setVatAcknowledge(CmrConstants.VAT_ACKNOWLEDGE_NA);
        } else {
          scorecard.setVatAcknowledge(CmrConstants.VAT_ACKNOWLEDGE_NA);
        }
      }
    }
    // CREATCMR-3144 - CN 2.0 special
    if (CmrConstants.Send_for_Processing().equals(model.getAction()) && SystemLocation.CHINA.equals(model.getCmrIssuingCntry())) {
      CNHandler.doBeforeSendForProcessing(entityManager, admin, data, model);
    }

    // check if there's a status change
    if (transitionToNext && !trans.getId().getCurrReqStatus().equals(trans.getNewReqStatus())) {
      // String rejectReason = model.getRejectReason();

      String rejectReasonCd1 = model.getRejectReason();
      String rejectReason = null;

      if (StringUtils.isEmpty(rejectReasonCd1)) {
        rejectReason = null;
      } else {
        rejectReason = getRejectReason(entityManager, rejectReasonCd1);
      }
      RequestUtils.createWorkflowHistory(this, entityManager, request, admin, model.getStatusChgCmt(), model.getAction(), sendToId, sendToNm,
          complete, rejectReason, rejectReasonCd1, model.getRejSupplInfo1(), model.getRejSupplInfo2());

      // save comment in req_cmt_log table .
      // save only if it is not null or not blank
      if (null != model.getStatusChgCmt() && !model.getStatusChgCmt().isEmpty()) {
        String action = model.getAction();
        String actionDesc = getActionDescription(action, entityManager);
        String statusDesc = getstatusDescription(trans.getNewReqStatus(), entityManager);
        String comment = STATUS_CHG_CMT_PRE_PREFIX + actionDesc + STATUS_CHG_CMT_MID_PREFIX + statusDesc + STATUS_CHG_CMT_POST_PREFIX
            + model.getStatusChgCmt();
        RequestUtils.createCommentLog(this, entityManager, user, model.getReqId(), comment);
      }
    }

    if (CmrConstants.Send_for_Processing().equals(model.getAction()) && !transitionToNext) {
      // request has been sent for processing, but will not move to next, still
      // save the comments and workflow history
      String wfComment = "Approvals generated/sent. Requester comments: " + model.getStatusChgCmt();
      if (wfComment.length() > 250) {

        wfComment = wfComment.substring(0, 237) + " (truncated)";
      }
      RequestUtils.createWorkflowHistory(this, entityManager, request, admin, wfComment, model.getAction(), null, null, false, null, null, null,
          null);
      String action = model.getAction();
      String actionDesc = getActionDescription(action, entityManager);
      String statusDesc = getstatusDescription(admin.getReqStatus(), entityManager);
      String comment = STATUS_CHG_CMT_PRE_PREFIX + actionDesc + "\" generated and/or sent approvals. Request still in " + statusDesc
          + " status until all approvals are received."
          + (null != model.getStatusChgCmt() && !model.getStatusChgCmt().isEmpty() ? (STATUS_CHG_CMT_POST_PREFIX + model.getStatusChgCmt()) : "");
      RequestUtils.createCommentLog(this, entityManager, user, model.getReqId(), comment);
    }

    computeInternalType(entityManager, model, admin);

    if (geoHandler != null && geoHandler.hasChecklist(model.getCmrIssuingCntry())) {
      saveChecklist(entityManager, model.getReqId(), user);
    }

  }

  private String getOldVatValue(EntityManager entityManager, long reqId) {
    String sql = ExternalizedQuery.getSql("QUERY.GET.OLD.VAT.VALUE");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", reqId);
    String vatVal = query.getSingleResult(String.class);
    return vatVal;
  }

  public static boolean isCrossBorder(EntityManager entityManager, String issuingCntry, String landCntry) {
    boolean isCrossBorder = false;
    boolean isSubRegion = false;
    Map<String, String> issuingLandedCntryMap = new HashMap<String, String>();
    String sql = ExternalizedQuery.getSql("LOAD_LANDCNTRY");
    String landedCntry;
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    List<Object[]> results = query.getResults();
    for (Object[] result : results) {
      issuingLandedCntryMap.put((String) result[0], (String) result[1]);
    }
    if ((Arrays.asList("LV", "LT", "EE").contains(landCntry) && "702".equals(issuingCntry))
        || (Arrays.asList("FO", "GL", "IS").contains(landCntry) && "678".equals(issuingCntry))
        || (Arrays.asList("BE", "LU").contains(landCntry) && "624".equals(issuingCntry))) {
      isSubRegion = true;
    }
    if (StringUtils.isNotBlank(issuingCntry) && issuingCntry.equalsIgnoreCase("624LU")) {
      landedCntry = "LU";
    } else
      landedCntry = issuingLandedCntryMap.get(issuingCntry);
    if (landedCntry != null && landCntry != null && StringUtils.isNotBlank(landedCntry) && StringUtils.isNotBlank(landCntry) && !isSubRegion) {
      if (landedCntry.equalsIgnoreCase(landCntry))
        isCrossBorder = false;
      else if (!landedCntry.equalsIgnoreCase(landCntry))
        isCrossBorder = true;
    }
    return isCrossBorder;
  }

  /**
   * Send for processing
   * 
   * @param trans
   * @param model
   * @param entityManager
   * @param request
   * @param processingCenter
   * @throws Exception
   */
  protected void performSendForProcessing(StatusTrans trans, RequestEntryModel model, EntityManager entityManager, HttpServletRequest request)
      throws Exception {
    String cmrIssuingCntry = model.getCmrIssuingCntry();
    String procCenterName = "Bratislava"; // TODO change
    String sql = ExternalizedQuery.getSql("REQUESTENTRY.GETPROCESSINGCENTER");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("CNTRY", cmrIssuingCntry);
    List<ProcCenter> procCenters = query.getResults(1, ProcCenter.class);
    if (procCenters != null && procCenters.size() > 0) {
      procCenterName = procCenters.get(0).getProcCenterNm();
    }
    AppUser user = AppUser.getUser(request);
    // make sure the latest data are here before
    CompoundEntity entity = getCurrentRecord(model, entityManager, request);
    copyValuesToEntity(model, entity);
    String result = null;
    String autoConfig = RequestUtils.getAutomationConfig(entityManager, model.getCmrIssuingCntry());
    if (!AutomationConst.AUTOMATE_PROCESSOR.equals(autoConfig) && !AutomationConst.AUTOMATE_BOTH.equals(autoConfig)) {
      result = approvalService.processDefaultApproval(entityManager, model.getReqId(), model.getReqType(), user, model);
    } else if (trans != null && !trans.getNewReqStatus().equals("AUT") && SystemLocation.CHINA.equals(model.getCmrIssuingCntry())) {
      approvalService.processDefaultApproval(entityManager, model.getReqId(), model.getReqType(), user, model);
    } else {
      this.log.info("Processor automation enabled, skipping default approvals.");
    }

    performGenericAction(trans, model, entityManager, request, procCenterName, null, false, StringUtils.isBlank(result));
  }

  /**
   * Special unlock only for Named originator or delegate of locking user
   * 
   * @param model
   * @param entityManager
   * @param request
   * @throws Exception
   */
  protected void performSpecialUnlock(RequestEntryModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    StatusTrans trans = new StatusTrans();
    StatusTransPK pk = new StatusTransPK();
    pk.setAction(model.getAction());
    pk.setCurrLockedInd(CmrConstants.YES_NO.Y.toString());
    pk.setCurrReqStatus(model.getReqStatus());
    pk.setReqType("*");
    trans.setId(pk);
    trans.setNewLockedInd(CmrConstants.YES_NO.N.toString());
    trans.setNewReqStatus(model.getReqStatus());
    performGenericAction(trans, model, entityManager, request, null);
  }

  /**
   * Cancel Request
   * 
   * @param trans
   * @param model
   * @param entityManager
   * @param request
   * @throws Exception
   */
  protected void performCancelRequest(StatusTrans trans, RequestEntryModel model, EntityManager entityManager, HttpServletRequest request)
      throws Exception {
    AppUser user = AppUser.getUser(request);
    CompoundEntity entity = getCurrentRecord(model, entityManager, request);

    Admin admin = entity.getEntity(Admin.class);
    admin.setLastUpdtBy(user.getIntranetId());
    admin.setLastUpdtTs(SystemUtil.getCurrentTimestamp());
    admin.setReqStatus(trans.getNewReqStatus());
    if (CmrConstants.YES_NO.Y.toString().equals(trans.getNewLockedInd())
        && CmrConstants.YES_NO.N.toString().equals(trans.getId().getCurrLockedInd())) {
      // the request is to be locked
      RequestUtils.setClaimDetails(admin, request);
    } else if (CmrConstants.YES_NO.N.toString().equals(trans.getNewLockedInd())
        && CmrConstants.YES_NO.Y.toString().equals(trans.getId().getCurrLockedInd())) {
      // request to be unlocked
      RequestUtils.clearClaimDetails(admin);
    }
    updateEntity(admin, entityManager);

    RequestUtils.createWorkflowHistory(this, entityManager, request, admin, model.getStatusChgCmt(), model.getAction(), null, null, false, null, null,
        null, null);

    // save comment in req_cmt_log table .
    // save only if it is not null or not blank
    if (null != model.getStatusChgCmt() && !model.getStatusChgCmt().isEmpty()) {

      String action = model.getAction();
      String actionDesc = getActionDescription(action, entityManager);
      String statusDesc = getstatusDescription(trans.getNewReqStatus(), entityManager);
      String comment = STATUS_CHG_CMT_PRE_PREFIX + actionDesc + STATUS_CHG_CMT_MID_PREFIX + statusDesc + STATUS_CHG_CMT_POST_PREFIX
          + model.getStatusChgCmt();

      RequestUtils.createCommentLog(this, entityManager, user, model.getReqId(), comment);
    }

  }

  /**
   * Gets the {@link StatusTrans} record associated with the request status,
   * lock ind, and the action to be taken
   * 
   * @param entityManager
   * @param model
   * @return
   */
  private StatusTrans getStatusTransition(EntityManager entityManager, RequestEntryModel model) {
    String sql = ExternalizedQuery.getSql("REQUESTENTRY.GETNEXTSTATUS");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("CURR_REQ_STATUS", model.getReqStatus());
    query.setParameter("CURR_LOCKED_IND", model.getLockInd() != null ? model.getLockInd() : CmrConstants.YES_NO.N.toString());
    query.setParameter("ACTION", model.getAction());
    query.setForReadOnly(true);
    List<StatusTrans> trans = query.getResults(2, StatusTrans.class);
    if (trans != null && trans.size() > 0) {
      for (StatusTrans transrec : trans) {
        if ("PPN".equals(transrec.getNewReqStatus())) {
          // check here for any automation
          String processingIndc = SystemUtil.getAutomationIndicator(entityManager, model.getCmrIssuingCntry());
          if ("P".equals(processingIndc) || "B".equals(processingIndc)) {
            if (SystemLocation.CHINA.equals(model.getCmrIssuingCntry()) && StringUtils.isNotBlank(model.getDisableAutoProc())
                && model.getDisableAutoProc().equalsIgnoreCase("Y")) {
              // transrec.setNewReqStatus("PPN");// set to PPN for CHINA
            } else {
              this.log.debug("Processor automation enabled for " + model.getCmrIssuingCntry() + ". Setting " + model.getReqId() + " to AUT");
              transrec.setNewReqStatus("AUT"); // set to automated processing
            }
          }
        }
        if ("*".equals(transrec.getId().getReqType())) {
          return transrec;
        } else if (transrec.getId().getReqType().equals(model.getReqType())) {
          return transrec;
        }
      }
    }
    return null;
  }

  private String getRejectReason(EntityManager entityManager, String code) {
    String sql = ExternalizedQuery.getSql("REQUESTENTRY.GETREJECTREASON");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("CD", code);
    List<Object[]> codes = query.getResults(1);
    if (codes != null && codes.size() > 0) {
      return (String) codes.get(0)[0];
    }
    return code;
  }

  @Override
  protected List<RequestEntryModel> doSearch(RequestEntryModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    CompoundEntity entity = getCurrentRecord(model, entityManager, request);
    RequestEntryModel newModel = new RequestEntryModel();
    copyValuesFromEntity(entity, newModel);
    Admin admin = entity.getEntity(Admin.class);
    if (admin != null) {
      newModel.setProcCenter(admin.getLastProcCenterNm());
    }
    newModel.setState(BaseModel.STATE_EXISTING);
    return Collections.singletonList(newModel);
  }

  @Override
  protected CompoundEntity getCurrentRecord(RequestEntryModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    long reqId = model.getReqId();
    if (reqId > 0) {
      String sql = ExternalizedQuery.getSql("REQUESTENTRY.MAIN");
      PreparedQuery query = new PreparedQuery(entityManager, sql);
      query.setParameter("REQ_ID", reqId);
      AppUser user = AppUser.getUser(request);
      query.setParameter("REQUESTER_ID", user.getIntranetId());
      query.setParameter("PROC_CENTER", user.getProcessingCenter());
      query.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
      List<CompoundEntity> records = query.getCompundResults(1, Admin.class, Admin.REQUEST_ENTRY_SERVICE_MAPPING);
      if (records != null && records.size() > 0) {
        return records.get(0);
      }
    }
    return null;
  }

  @Override
  protected CompoundEntity createFromModel(RequestEntryModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    CompoundEntity entity = new CompoundEntity();
    Admin admin = adminService.createFromModel(model, entityManager, request);
    Data data = dataService.createFromModel(model, entityManager, request);
    Scorecard scorecard = scorecardService.createFromModel(model, entityManager, request);
    entity.addEntity(admin);
    entity.addEntity(data);
    entity.addEntity(scorecard);
    return entity;
  }

  @Override
  public void copyValuesFromEntity(CompoundEntity from, RequestEntryModel to) {
    Admin admin = from.getEntity(Admin.class);
    if (admin != null) {
      adminService.copyValuesFromEntity(admin, to);

    }
    Data data = from.getEntity(Data.class);
    if (data != null) {
      dataService.copyValuesFromEntity(data, to);
      if (PageManager.fromGeo("MCO", data.getCmrIssuingCntry())) {
        this.log.debug("Getting payment mode and location no...");
        to.setPaymentMode(data.getModeOfPayment());
        to.setLocationNo(data.getLocationNumber());
      }

      if (PageManager.fromGeo("EMEA", "758")) {
        this.log.debug("Getting payment mode and location no...");
        to.setPaymentMode(data.getModeOfPayment());
      }
      if (PageManager.fromGeo("CEMEA", data.getCmrIssuingCntry())) {
        this.log.debug("Getting location no...");
        to.setLocationNo(data.getLocationNumber());
      }
      if (PageManager.fromGeo("NORDX", data.getCmrIssuingCntry())) {
        this.log.debug("Getting payment mode...");
        to.setPaymentMode(data.getModeOfPayment());
        this.log.debug("Setting CollectionCode...");
        to.setCollectionCd(data.getCollectionCd());
        this.log.debug("Setting Tax Code...");
        to.setTaxCd1(data.getTaxCd1());
        this.log.debug("Setting Sales Rep Code...");
        to.setRepTeamMemberNo(data.getRepTeamMemberNo());
      }
    }
    Scorecard score = from.getEntity(Scorecard.class);
    if (score != null) {
      scorecardService.copyValuesFromEntity(score, to);
    }

    to.setClaimRole((String) from.getValue("CLAIM_ROLE"));
    to.setOverallStatus((String) from.getValue("OVERALL_STATUS"));
    to.setSubIndustryDesc((String) from.getValue("SUB_INDUSTRY_DESC"));
    to.setIsicDesc((String) from.getValue("ISIC_DESC"));
    to.setProcessingStatus((String) from.getValue("PROCESSING_STATUS"));
    to.setCanClaim((String) from.getValue("CAN_CLAIM"));
    to.setCanClaimAll((String) from.getValue("CAN_CLAIM_ALL"));
    to.setAutoProcessing((String) from.getValue("AUTO_PROCESSING"));

    // copy internal dpl fields, for display only
    SimpleDateFormat dateFormat = CmrConstants.DATE_FORMAT();
    if (score != null) {
      to.setIntDplAssessmentResult(score.getDplAssessmentResult());
      to.setIntDplAssessmentBy(score.getDplAssessmentBy());
      to.setIntDplAssessmentCmt(score.getDplAssessmentCmt());
      if (score.getDplAssessmentDate() != null) {
        to.setIntDplAssessmentDate(dateFormat.format(score.getDplAssessmentDate()));
      }
    }
  }

  @Override
  public void copyValuesToEntity(RequestEntryModel from, CompoundEntity to) {
    Admin admin = to.getEntity(Admin.class);
    if (admin == null) {

    }
    adminService.copyValuesToEntity(from, admin);
    Data data = to.getEntity(Data.class);
    if (data != null) {
      String usemail3 = data.getEmail3() == null ? null : new String(data.getEmail3());
      String bpGbmSbmAffiliate = data.getAffiliate() == null ? null : new String(data.getAffiliate());
      String bpGbmSbmEmbargoCd = data.getEmbargoCd() == null ? null : new String(data.getEmbargoCd());
      String bpGbmSbmOrdBlk = data.getOrdBlk() == null ? null : new String(data.getOrdBlk());
      dataService.copyValuesToEntity(from, data);
      // 1261175 -Dennis - We need to auto assign the cust type if it is an
      // update type and for BR
      if (LAHandler.isSSAMXBRIssuingCountry(data.getCmrIssuingCntry()) && admin != null
          && CmrConstants.REQ_TYPE_UPDATE.equalsIgnoreCase(admin.getReqType())) {
        admin.setCustType(CmrConstants.DEFAULT_CUST_TYPE);
      }

      // #1267149 set countryUse from mrcCd value
      if (LAHandler.isBRIssuingCountry(data.getCmrIssuingCntry())) {
        if (!StringUtils.isEmpty(from.getMrcCd())) {
          data.setCountryUse(from.getMrcCd());
        }
        if (!StringUtils.isEmpty(from.getCrosSubTyp()) && admin != null && CmrConstants.REQ_TYPE_CREATE.equals(admin.getReqType())) {
          data.setModeOfPayment(from.getCrosSubTyp());
        }
      } else {
        if (!StringUtils.isEmpty(from.getCrosSubTyp()) && admin != null && CmrConstants.REQ_TYPE_CREATE.equals(admin.getReqType())) {
          data.setModeOfPayment(from.getCrosSubTyp());
        }
      }
      if (CNDHandler.isCNDCountry(data.getCmrIssuingCntry())) {
        this.log.debug("Setting Mode Of Payment.");
        data.setModeOfPayment(from.getModeOfPayment());
      }
      // only copy from UI to data if from IT
      if (PageManager.fromGeo("EMEA", data.getCmrIssuingCntry())) {
        this.log.debug("Setting payment mode");
        data.setModeOfPayment(from.getPaymentMode());
      }
      // only copy from UI to data if from MCO
      if (PageManager.fromGeo("MCO", data.getCmrIssuingCntry())) {
        this.log.debug("Setting payment mode and location no...");
        data.setModeOfPayment(from.getPaymentMode());
        data.setLocationNumber(from.getLocationNo());
      }

      if (PageManager.fromGeo("NORDX", data.getCmrIssuingCntry())) {
        this.log.debug("Getting payment mode and location no...");
        data.setModeOfPayment(from.getPaymentMode());
        this.log.debug("Setting CollectionCode...");
        data.setCollectionCd(from.getCollectionCd());
        this.log.debug("Setting Tax Code...");
        data.setTaxCd1(from.getTaxCd1());
        this.log.debug("Setting Sales Rep Code...");
        data.setRepTeamMemberNo(from.getRepTeamMemberNo());
      }

      // only copy from UI to data if from CEMEA
      if (PageManager.fromGeo("CEMEA", data.getCmrIssuingCntry())) {
        this.log.debug("Setting location no...");
        data.setLocationNumber(from.getLocationNo());
      }
      if ("CreateCMR-BP".equals(admin.getSourceSystId())) {
        // Save email3 value anywhere for us bp source
        if (SystemLocation.UNITED_STATES.equals(data.getCmrIssuingCntry()) && !StringUtils.isEmpty(usemail3)) {
          this.log.debug("Setting BP us client model:email3");
          data.setEmail3(usemail3);
        }
        if (CmrConstants.BP_GBM_SBM_COUNTRIES.contains(data.getCmrIssuingCntry())) {
          data.setAffiliate(bpGbmSbmAffiliate);
          data.setEmbargoCd(bpGbmSbmEmbargoCd);
          data.setOrdBlk(bpGbmSbmOrdBlk);
        }
      }
    }
    Scorecard score = to.getEntity(Scorecard.class);
    if (score != null) {
      scorecardService.copyValuesToEntity(from, score);
    }
    to.setValue("CLAIM_ROLE", from.getClaimRole());
    to.setValue("OVERALL_STATUS", from.getOverallStatus());
    to.setValue("SUB_INDUSTRY_DESC", from.getSubIndustryDesc());
    to.setValue("ISIC_DESC", from.getIsicDesc());
    to.setValue("PROCESSING_STATUS", from.getProcessingStatus());
    to.setValue("CAN_CLAIM", from.getCanClaim());
    to.setValue("CAN_CLAIM_ALL", from.getCanClaimAll());
    to.setValue("AUTO_PROCESSING", from.getAutoProcessing());
  }

  /**
   * Gets the AddrStdResult for the record
   * 
   * @param reqid
   * @param model
   * @return
   */
  public String getAddrStdResult(long reqId) {
    EntityManager entityManager = JpaManager.getEntityManager();
    try {
      String sql = ExternalizedQuery.getSql("REQUESTENTRY.GETADDRSTDRESULT");
      PreparedQuery query = new PreparedQuery(entityManager, sql);
      query.setParameter("REQ_ID", reqId);
      List<Object[]> codes = query.getResults(-1);
      List<String> strings = new ArrayList<String>();
      for (Object object : codes) {
        strings.add(object != null ? object.toString().trim() : null);
      }

      int yCount = 0;
      int xCount = 0;
      int nCount = 0;

      for (String code : strings) {
        if ("N".equals(code)) {
          nCount++;
        } else if ("Y".equals(code)) {
          yCount++;
        } else if ("X".equals(code)) {
          xCount++;
        }
      }

      if (nCount > 0) {
        return CmrConstants.Scorecard_Not_Done;
      } else if (xCount > 0) {
        return CmrConstants.Scorecard_COMPLETED_WITH_ISSUES;
      } else if (yCount > 0) {
        return CmrConstants.Scorecard_COMPLETED;
      } else {
        return "-";
      }

    } finally {
      // empty the manager
      entityManager.clear();
      entityManager.close();
    }

  }

  protected String getstatusDescription(String status, EntityManager entityManager) throws CmrException {
    String desc = null;
    String sql = ExternalizedQuery.getSql("status_desc");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("CD", status);

    List<Object[]> results = query.getResults(1);
    for (Object[] result : results) {
      desc = (String) result[5];
    }

    return desc;
  }

  protected String getActionDescription(String action, EntityManager entityManager) throws CmrException {
    String desc = null;
    String sql = ExternalizedQuery.getSql("action_desc");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("ACTION", action);

    List<Object[]> results = query.getResults(1);
    for (Object[] result : results) {
      desc = (String) result[2];
    }

    return desc;
  }

  public DataModel getRdcDataModel(EntityManager entityManager, long reqId) throws Exception {
    DataModel model = new DataModel();
    String sql = ExternalizedQuery.getSql("REQUESTENTRY.GET_RDC_DATA");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", reqId);
    query.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
    List<Data> results = query.getResults(1, Data.class);
    if (results != null && results.size() > 0) {
      Data data = results.get(0);
      PropertyUtils.copyProperties(model, data);
    }

    return model;
  }

  private boolean processDplReset(EntityManager entityManager, RequestEntryModel model, Admin admin) {
    String mName1 = StringUtils.isEmpty(model.getMainCustNm1()) ? "" : model.getMainCustNm1();
    String mName2 = StringUtils.isEmpty(model.getMainCustNm2()) ? "" : model.getMainCustNm2();
    String aName1 = StringUtils.isEmpty(admin.getMainCustNm1()) ? "" : admin.getMainCustNm1();
    String aName2 = StringUtils.isEmpty(admin.getMainCustNm2()) ? "" : admin.getMainCustNm2();
    GEOHandler handler = RequestUtils.getGEOHandler(model.getCmrIssuingCntry());
    if (handler == null || (handler != null && !handler.customerNamesOnAddress())) {
      if (!"DPL".equals(model.getAction()) && (!StringUtils.equals(mName1, aName1) || !StringUtils.equals(mName2, aName2))) {
        this.log.debug("Clearing DPL for " + admin.getId().getReqId());
        AddressService.clearDplResults(entityManager, admin.getId().getReqId());
        return true;
      } else {
        return false;
      }
    } else {
      return false;
    }
  }

  /**
   * Appends extra model entries if needed. These attributes can be accessed on
   * the page via request.getAttribute('name') or ${name}
   * 
   * @param mv
   * @param model
   * @throws CmrException
   */
  public void appendExtraModelEntries(ModelAndView mv, RequestEntryModel model) throws CmrException {

    mv.addObject("notif", new NotifyListModel());
    mv.addObject("attach", new AttachmentModel());
    mv.addObject("addressModal", new AddressModel());
    mv.addObject("rdcaddr", new AddressModel());
    mv.addObject("subindisic", new SubindustryIsicSearchModel());
    mv.addObject("approval", new ApprovalResponseModel());
    mv.addObject("fiscalDataModal", new ValidateFiscalDataModel());
    mv.addObject("autoDnbModel", new AutoDNBDataModel());

    EntityManager entityManager = JpaManager.getEntityManager();

    try {

      AdminPK adminPK = new AdminPK();
      adminPK.setReqId(model.getReqId());
      Admin admin = entityManager.find(Admin.class, adminPK);
      if (admin != null) {
        mv.addObject("sourceSystem", admin.getSourceSystId());
      } else {
        mv.addObject("sourceSystem", null);
      }

      String autoEngineIndc = RequestUtils.getAutomationConfig(entityManager, model.getCmrIssuingCntry());
      mv.addObject("autoEngineIndc", autoEngineIndc);
      DataModel rdcData = getRdcDataModel(entityManager, model.getReqId());
      mv.addObject("rdcdata", rdcData);

      /* 1970060 - automation engine */
      mv.addObject("automationIndicator", RequestUtils.getAutomationConfig(entityManager, model.getCmrIssuingCntry()));

      String dnbPrimary = RequestUtils.isDnBCountry(entityManager, model.getCmrIssuingCntry());
      mv.addObject("dnbPrimary", dnbPrimary);

      GEOHandler geoHandler = RequestUtils.getGEOHandler(model.getCmrIssuingCntry());
      if (geoHandler != null) {
        geoHandler.appendExtraModelEntries(entityManager, mv, model);

        if (geoHandler.hasChecklist(model.getCmrIssuingCntry())) {
          this.log.debug("Processing checklist");
          ProlifChecklist pChecklist = getChecklist(entityManager, model.getReqId());
          CheckListModel checklist = new CheckListModel();
          if (pChecklist != null) {
            try {
              PropertyUtils.copyProperties(checklist, pChecklist);
              checklist.setReqId(model.getReqId());
            } catch (Exception e) {
              this.log.warn("Cannot copy properties.", e);
            }
          } else {
            checklist = new CheckListModel();
          }

          mv.addObject("checklist", checklist);
        }
      }
    } catch (Exception e) {
      if (e instanceof CmrException) {
        log.error("CMR Error:" + ((CmrException) e).getMessage());
      } else {
        // log only unexpected errors, exclude validation errors
        log.error("Error in processing transaction " + model, e);
      }

      // only wrap non CmrException errors
      if (e instanceof CmrException) {
        throw (CmrException) e;
      } else {
        throw new CmrException(MessageUtil.ERROR_GENERAL);
      }
    } finally {
      // empty the manager
      entityManager.clear();
      entityManager.close();
    }

    String defltLandCntry = PageManager.getDefaultLandedCountry(model.getCmrIssuingCntry());
    mv.addObject("defaultLandedCountry", defltLandCntry != null ? defltLandCntry.trim() : "");
  }

  public List<ValidationUrlModel> getValidationUrls(String cntryCd) throws CmrException {
    List<ValidationUrlModel> validationUrls = new ArrayList<ValidationUrlModel>();
    EntityManager entityManager = JpaManager.getEntityManager();

    try {
      String sql = ExternalizedQuery.getSql("REQUESTENTRY.GETVALIDATIONURLS");
      PreparedQuery query = new PreparedQuery(entityManager, sql);
      query.setParameter("CNTRY_CD", cntryCd);

      List<ValidationUrl> rs = query.getResults(ValidationUrl.class);

      ValidationUrlModel valModel = null;
      for (ValidationUrl val : rs) {
        valModel = new ValidationUrlModel();
        PropertyUtils.copyProperties(valModel, val);
        PropertyUtils.copyProperties(valModel, val.getId());
        if (valModel.getCntryCd() != null && "000".equals(valModel.getCntryCd().trim())) {
          valModel.setCntryCd("WW");
        }
        validationUrls.add(valModel);
      }

    } catch (Exception e) {
      // only wrap non CmrException errors
      if (e instanceof CmrException) {
        throw (CmrException) e;
      } else {
        this.log.error("Unexpected error occurred", e);
        throw new CmrException(MessageUtil.ERROR_GENERAL);
      }
    } finally {
      // empty the manager
      entityManager.clear();
      entityManager.close();
    }
    return validationUrls;
  }

  /**
   * Checks if the request has been claimed already
   * 
   * @param entityManager
   * @param reqId
   * @return
   */
  private boolean canClaim(EntityManager entityManager, RequestEntryModel model) {
    String sql = ExternalizedQuery.getSql("REQENTRY.ISCLAIMED");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", model.getReqId());
    Object[] result = query.getSingleResult(Object[].class);
    return "Y".equals(result[0]) || !model.getReqStatus().equals(result[1]);
  }

  /**
   * Sets the ApprovalResult for the record
   */
  public void setApprovalResult(RequestEntryModel model) {
    ApprovalService service = new ApprovalService();
    service.setApprovalResult(model);
  }

  private void processObsoleteApprovals(EntityManager entityManager, long reqId, AppUser user) throws Exception {
    ApprovalService service = new ApprovalService();
    service.makeApprovalsObsolete(entityManager, reqId, user);
  }

  public void saveChecklist(EntityManager entityManager, long reqId, AppUser user) {
    if (!isCheckListValid()) {
      this.log.debug("Checklist is blank for Request " + reqId + ", skipping saving.");
      return;
    }
    this.log.debug("Processing checklist for request " + reqId);
    String sql = ExternalizedQuery.getSql("REQENTRY.GETCHECKLIST");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", reqId);
    ProlifChecklist checklist = query.getSingleResult(ProlifChecklist.class);

    boolean create = false;
    if (checklist == null) {
      create = true;
      this.log.info("Creating checklist for request..");
      checklist = new ProlifChecklist();
      ProlifChecklistPK chkPk = new ProlifChecklistPK();
      chkPk.setReqId(reqId);
      checklist.setId(chkPk);
      checklist.setCreateBy(user.getIntranetId());
      checklist.setCreateTs(SystemUtil.getCurrentTimestamp());
    }

    try {
      if (!fromBPPortal(entityManager, reqId)) {
        PropertyUtils.copyProperties(checklist, this.checklist);
      }
    } catch (Exception e) {
      this.log.warn("Cannot copy properties.", e);
    }

    checklist.setLastUpdtBy(user.getIntranetId());
    checklist.setLastUpdtTs(SystemUtil.getCurrentTimestamp());

    if (create) {
      createEntity(checklist, entityManager);
    } else {
      updateEntity(checklist, entityManager);
    }

  }

  private boolean fromBPPortal(EntityManager entityManager, long reqId) {
    String sourceSystId = getSourceSystId(entityManager, reqId);
    if ("CreateCMR-BP".equals(sourceSystId)) {
      return true;
    }
    return false;
  }

  private String getSourceSystId(EntityManager entityManager, long reqId) {
    String output = null;
    String sql = ExternalizedQuery.getSql("QUERY.GET.SOURCESYSTID");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", reqId);
    String result = query.getSingleResult(String.class);

    if (result != null) {
      output = result;
    }
    return output;
  }

  /**
   * Checks if the current checklist on this request has a valid value, which
   * means it was properly submitted
   * 
   * @return
   * @throws IllegalArgumentException
   * @throws IllegalAccessException
   */
  private boolean isCheckListValid() {
    if (this.checklist == null) {
      return false;
    }
    for (Field field : CheckListModel.class.getDeclaredFields()) {
      if (String.class.equals(field.getType())) {
        field.setAccessible(true);
        String value = null;
        try {
          value = (String) field.get(this.checklist);
        } catch (Exception e) {
          this.log.warn("Field " + field.getName() + " cannot be retrieved from checklist model");
        }
        if (!StringUtils.isBlank(value)) {
          // at least one valid checklist item
          return true;
        }
      }
    }
    return false;
  }

  private ProlifChecklist getChecklist(EntityManager entityManager, long reqId) {
    String sql = ExternalizedQuery.getSql("REQENTRY.GETCHECKLIST");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", reqId);
    return query.getSingleResult(ProlifChecklist.class);
  }

  public CheckListModel getChecklist() {
    return checklist;
  }

  public void setChecklist(CheckListModel checklist) {
    this.checklist = checklist;
  }

  /**
   * Extracts the access token from the user's session and saves it on the ADMIN
   * record. This token will be used for automated DPL searching
   * 
   * @param admin
   * @param request
   */
  private void saveAccessToken(Admin admin, HttpServletRequest request) {
    String accessToken = RequestUtils.getAccessToken(request);
    if (!StringUtils.isBlank(accessToken)) {
      admin.setCmt(accessToken);
    }

  }

  /**
   * Gets the highest dnb matches for the request
   * 
   * @param reqid
   * @param model
   * @return
   */
  public List<AutoDNBDataModel> getDnBMatchList(AutoDNBDataModel model, long reqId) throws Exception {
    List<AutoDNBDataModel> resultList = new ArrayList<AutoDNBDataModel>();
    EntityManager entityManager = JpaManager.getEntityManager();
    try {
      if (reqId == 0) {
        return new ArrayList<AutoDNBDataModel>();
      }

      RequestData requestData = new RequestData(entityManager, reqId);
      Addr soldTo = requestData.getAddress("ZS01");
      if (soldTo != null) {
        MatchingResponse<DnBMatchingResponse> response = DnBUtil.getMatches(requestData, null, "ZS01");
        if (response != null && response.getMatched()) {
          List<DnBMatchingResponse> dnbMatches = response.getMatches();
          this.log.debug("DnB Response recieved and no. of matches found. = " + dnbMatches.size());
          AutoDNBDataModel resultModel = null;
          StringBuilder address = null;
          StringBuilder ibmIsic = null;
          int itemNo = 1;
          for (DnBMatchingResponse dnbRecord : dnbMatches) {
            if (dnbRecord.getConfidenceCode() >= 8) {

              resultModel = new AutoDNBDataModel();
              address = new StringBuilder();
              ibmIsic = new StringBuilder();

              resultModel.setItemNo(itemNo);
              resultModel.setAutoDnbDunsNo(dnbRecord.getDunsNo());
              resultModel.setAutoDnbMatchGrade("Confidence Code = " + dnbRecord.getConfidenceCode());
              resultModel.setAutoDnbName(dnbRecord.getDnbName());
              address.append(dnbRecord.getDnbStreetLine1()).append("\n");
              if (!StringUtils.isBlank(dnbRecord.getDnbStreetLine2())) {
                address.append(dnbRecord.getDnbStreetLine2()).append("\n");
              }
              if (!StringUtils.isBlank(dnbRecord.getDnbCity())) {
                address.append(dnbRecord.getDnbCity());
              }
              if (!StringUtils.isBlank(dnbRecord.getDnbStateProv())) {
                address.append(", " + dnbRecord.getDnbStateProv());
              }
              if (!StringUtils.isBlank(dnbRecord.getDnbCountry())) {
                address.append("\n" + dnbRecord.getDnbCountry());
              }
              if (!StringUtils.isBlank(dnbRecord.getDnbPostalCode())) {
                address.append(" " + dnbRecord.getDnbPostalCode());
              }
              resultModel.setFullAddress(address.toString());

              this.log.debug("Connecting to D&B details service..");
              DnBCompany dnbDetailsUI = getDnBDetailsUI(dnbRecord.getDunsNo());
              if (dnbDetailsUI != null) {
                ibmIsic.append("ISIC =  " + dnbDetailsUI.getIbmIsic() + " (" + dnbDetailsUI.getIbmIsicDesc() + ")").append("\n");
              }
              resultModel.setIbmIsic(ibmIsic.toString());

              resultList.add(resultModel);
              itemNo++;
            }
          }
        } else {
          this.log.debug("No D&B record was found using advanced matching.");
        }
      } else {
        this.log.debug("Missing main address on the request.");
      }
    } finally {
      // empty the manager
      entityManager.clear();
      entityManager.close();
    }
    return resultList;
  }

  /**
   * Matches Request Data (ZS01) with DnB and returns the matching result
   * 
   * @param model
   * @param reqId
   * @return
   */
  public ModelMap isDnBMatch(AutoDNBDataModel model, long reqId, String addrType) {
    ModelMap map = new ModelMap();
    map.put("dnbNmMatch", false);
    map.put("dnbAddrMatch", false);
    EntityManager entityManager = null;
    try {
      if (reqId > 0) {
        entityManager = JpaManager.getEntityManager();
        RequestData requestData = new RequestData(entityManager, reqId);
        Data data = requestData.getData();
        Admin admin = requestData.getAdmin();
        Scorecard scorecard = requestData.getScorecard();
        Addr addr = requestData.getAddress(addrType);
        DnBMatchingResponse tradeStyleName = null;
        boolean isicMatch = false;
        boolean isOrgIdMatched = false;
        boolean confidenceCd = false;
        boolean cnCrossFlag = false;
        boolean checkTradestyleNames = ("R".equals(RequestUtils.getTradestyleUsage(entityManager, data.getCmrIssuingCntry()))
            || "O".equals(RequestUtils.getTradestyleUsage(entityManager, data.getCmrIssuingCntry())));
        MatchingResponse<DnBMatchingResponse> response = DnBUtil.getMatches(requestData, null, addrType);
        if (response != null && response.getSuccess()) {
          map.put("success", true);
          boolean match = false;
          if (response.getMatched() && ((("Accepted".equals(scorecard.getFindDnbResult()) && !StringUtils.isBlank(requestData.getData().getDunsNo()))
              || "Rejected".equals(scorecard.getFindDnbResult())) || "U".equals(admin.getReqType()))) {
            for (DnBMatchingResponse record : response.getMatches()) {
              // ISIC on request is compared to IBM ISIC
              if (record.getConfidenceCode() >= 8) {
                confidenceCd = true;
              }
              if (!(SystemLocation.SINGAPORE.equals(data.getCmrIssuingCntry()) && "TH".equals(requestData.getAddress("ZS01").getLandCntry())
                  && !"NA".equals(data.getTaxCd1()))) {
                isOrgIdMatched = true;
              } else {
                List<DnbOrganizationId> dnbOrgIdList = record.getOrgIdDetails();
                for (DnbOrganizationId orgId : dnbOrgIdList) {
                  String dnbOrgId = orgId.getOrganizationIdCode();
                  String dnbOrgType = orgId.getOrganizationIdType();
                  if (data.getVat().equals(dnbOrgId) && "Registration Number (TH)".equals(dnbOrgType)) {
                    isOrgIdMatched = true;
                  }
                }
              }
              log.debug("orgId Match : " + isOrgIdMatched);
              DnBCompany dnbCompny = getDnBDetailsUI(record.getDunsNo());
              if (dnbCompny != null) {
                isicMatch = dnbCompny.getIbmIsic().equals(model.getIsicCd());
              }
              log.debug("ISIC Match : " + isicMatch);
              if (SystemLocation.CHINA.equals(data.getCmrIssuingCntry())
                  && ("ZS01".equals(addr.getId().getAddrType()) && "U".equals(admin.getReqType()) && StringUtils.isBlank(data.getCustSubGrp())
                      && !"CN".equalsIgnoreCase(addr.getLandCntry()) || "C".equals(admin.getReqType()) && "CROSS".equals(data.getCustSubGrp()))) {
                cnCrossFlag = true;
              }
              if (record.getConfidenceCode() >= 8 && SystemLocation.CHINA.equals(data.getCmrIssuingCntry())
                  && ("U".equals(admin.getReqType()) && StringUtils.isBlank(data.getCustSubGrp()) && "CN".equalsIgnoreCase(addr.getLandCntry())
                      || "C".equals(admin.getReqType()) && !"CROSS".equals(data.getCustSubGrp()))) {
                match = true;
                break;
              }
              if (SystemLocation.NEW_ZEALAND.equals(data.getCmrIssuingCntry())) {
                ModelMap nzDNBMatchMap = DnBUtil.closelyMatchesDnbNmAndAddr(data.getCmrIssuingCntry(), addr, admin, record, null, false, true);
                map.putAll(nzDNBMatchMap);
                break;
              } else if (record.getConfidenceCode() >= 8
                  && DnBUtil.closelyMatchesDnb(data.getCmrIssuingCntry(), addr, admin, record, null, false, true)) {
                match = true;
                break;
              } else if (checkTradestyleNames && record.getConfidenceCode() >= 8 && record.getTradeStyleNames() != null && tradeStyleName == null
                  && DnBUtil.closelyMatchesDnb(data.getCmrIssuingCntry(), addr, admin, record, null, true)) {
                tradeStyleName = record;
              }
            }
          }
          if (isDnbOverrideAttachmentProvided(entityManager, admin.getId().getReqId())) {
            map.put("validate", true);
          } else {
            map.put("validate", false);
          }
          map.put("match", match);
          map.put("isicMatch", isicMatch);
          map.put("orgIdMatch", isOrgIdMatched);
          map.put("confidenceCd", confidenceCd);
          map.put("cnCrossFlag", cnCrossFlag);
          if (!match && tradeStyleName != null) {
            map.put("tradeStyleMatch", true);
            map.put("legalName", tradeStyleName.getDnbName());
            map.put("dunsNo", tradeStyleName.getDunsNo());
          }
        } else {
          map.put("success", false);
          map.put("match", false);
          map.put("cnCrossFlag", false);
          String message = "An error occurred while matching with DnB.";
          if (response != null) {
            message = response.getMessage();
          }
          map.put("message", message);
        }
      } else {
        map.put("success", false);
        map.put("match", false);
        String message = "Invalid request Id";
        map.put("message", message);
      }
    } catch (Exception e) {
      log.debug("Error occurred while checking DnB Matches." + e);
      map.put("success", false);
      map.put("match", false);
      String message = "An error occurred while matching with DnB.";
      map.put("message", message);
    } finally {
      if (entityManager != null) {
        entityManager.close();
      }
    }
    return map;

  }

  public ModelMap isDnBMatchAddrsUpdateAU(AutoDNBDataModel model, long reqId) {
    ModelMap dnbmatchmap = new ModelMap();
    ModelMap map = new ModelMap();
    List<ModelMap> maps = new ArrayList<ModelMap>();
    EntityManager entityManager = null;
    try {
      if (reqId > 0) {
        entityManager = JpaManager.getEntityManager();
        RequestData requestData = new RequestData(entityManager, reqId);
        Data data = requestData.getData();
        Admin admin = requestData.getAdmin();
        RequestChangeContainer changes = new RequestChangeContainer(entityManager, data.getCmrIssuingCntry(), admin, requestData);
        List<Addr> addresses = null;
        ArrayList<String> RELEVANT_ADDRESSES = new ArrayList<String>(Arrays.asList(CmrConstants.RDC_SOLD_TO, CmrConstants.RDC_BILL_TO,
            CmrConstants.RDC_INSTALL_AT, "STAT", "MAIL", "ZF01", "PUBS", "PUBB", "EDUC", "CTYG", "CTYH"));

        for (String addrType : RELEVANT_ADDRESSES) {
          if (changes.isAddressChanged(addrType)) {
            addresses = requestData.getAddresses(addrType);

            if (addresses != null) {
              for (Addr addr : addresses) {
                if ("Y".equals(addr.getChangedIndc())) {
                  // update address
                  if (RELEVANT_ADDRESSES.contains(addr.getId().getAddrType())) {
                    if (isRelevantAddressFieldUpdated(changes, addr)) {
                      // dnb match
                      dnbmatchmap = isDnBMatch(model, reqId, addr.getId().getAddrType());
                      maps.add(dnbmatchmap);
                      // dnbmatchmap.clear();
                    } else {
                      // No Dnb match required for update if no changes
                      dnbmatchmap.put("success", true);
                      dnbmatchmap.put("match", true);
                      dnbmatchmap.put("isicMatch", true);
                      maps.add(dnbmatchmap);
                    }
                  }
                } else if ("N".equals(addr.getImportInd())) {
                  // dnbMatch
                  dnbmatchmap = isDnBMatch(model, reqId, addr.getId().getAddrType());
                  maps.add(dnbmatchmap);
                  // dnbmatchmap.clear();
                }
              }
            } else {
              // No Dnb match required for update if no changes
              dnbmatchmap.put("success", true);
              dnbmatchmap.put("match", true);
              dnbmatchmap.put("isicMatch", true);
              maps.add(dnbmatchmap);
            }
          }
        }

        if (maps != null) {
          for (ModelMap dnbMap : maps) {
            if (!dnbMap.isEmpty() && (Boolean) dnbMap.get("success") && (Boolean) dnbMap.get("match") && (Boolean) dnbMap.get("isicMatch")) {
              continue;
            } else {
              map = dnbMap;
              break;
              // return map;
            }
          }
          if (map.isEmpty()) {
            map.put("success", true);
            map.put("match", true);
            map.put("isicMatch", true);
          }
        }
      } else {
        map.put("success", false);
        map.put("match", false);
        String message = "Invalid request Id";
        map.put("message", message);
      }
    } catch (Exception e) {
      log.debug("Error occurred while checking DnB Matches." + e);
      map.put("success", false);
      map.put("match", false);
      String message = "An error occurred while matching with DnB.";
      map.put("message", message);
    } finally {
      if (entityManager != null) {
        entityManager.close();
      }
    }
    return map;

  }

  public ModelMap isCustNmMatch(AutoDNBDataModel model, long reqId, String custNm, String formerCustNm) {

    ModelMap map = new ModelMap();
    EntityManager entityManager = null;
    try {
      if (reqId > 0) {
        entityManager = JpaManager.getEntityManager();
        RequestData requestData = new RequestData(entityManager, reqId);
        List<String> dnbTradestyleNames = new ArrayList<String>();
        boolean dnbCustNmMatch = false;
        boolean dnbFormerCustNmMatch = false;
        boolean dnbSuccess = false;
        boolean dnbMatch = false;

        MatchingResponse<DnBMatchingResponse> response = DnBUtil.getMatches(requestData, null, "ZS01");
        if (response != null && response.getMatched()) {
          dnbSuccess = true;
          List<DnBMatchingResponse> dnbMatches = response.getMatches();
          for (DnBMatchingResponse dnbRecord : dnbMatches) {
            dnbMatch = true;
            String dnbCustNm = StringUtils.isBlank(dnbRecord.getDnbName()) ? "" : dnbRecord.getDnbName().replaceAll("\\s+$", "");
            if (custNm.equalsIgnoreCase(dnbCustNm)) {
              dnbCustNmMatch = true;
              dnbTradestyleNames = dnbRecord.getTradeStyleNames();
              if (dnbTradestyleNames != null) {
                for (String tradestyleNm : dnbTradestyleNames) {
                  tradestyleNm = tradestyleNm.replaceAll("\\s+$", "");
                  if (tradestyleNm.equalsIgnoreCase(formerCustNm)) {
                    dnbFormerCustNmMatch = true;
                    break;
                  }
                }
              }
            }
          }
        }
        map.put("match", dnbMatch);
        map.put("success", dnbSuccess);
        map.put("formerCustNmMatch", dnbFormerCustNmMatch);
        map.put("custNmMatch", dnbCustNmMatch);
      } else {
        map.put("success", false);
        map.put("match", false);
        String message = "Invalid request Id";
        map.put("message", message);
      }
    } catch (Exception e) {
      log.debug("Error occurred while checking DnB Matches." + e);
      map.put("success", false);
      map.put("match", false);
      String message = "An error occurred while matching with DnB.";
      map.put("message", message);
    } finally {
      if (entityManager != null) {
        entityManager.close();
      }
    }
    return map;
  }

  private boolean isRelevantAddressFieldUpdated(RequestChangeContainer changes, Addr addr) {
    List<UpdatedNameAddrModel> addrChanges = changes.getAddressChanges(addr.getId().getAddrType(), addr.getId().getAddrSeq());
    List<String> NON_RELEVANT_ADDRESS_FIELDS_AU = Arrays.asList("Attn", "Phone #", "Customer Name", "Customer Name Con't");
    if (addrChanges == null) {
      return false;
    }
    for (UpdatedNameAddrModel change : addrChanges) {
      if (!NON_RELEVANT_ADDRESS_FIELDS_AU.contains(change.getDataField())) {
        return true;
      }
    }
    return false;
  }

  /**
   * Connects to the details service and gets the details of the DUNS NO from
   * D&B
   * 
   * @param dunsNo
   * @return
   * @throws Exception
   */
  public static DnBCompany getDnBDetailsUI(String dunsNo) throws Exception {
    CmrClientService service = new CmrClientService();
    ModelMap map = new ModelMap();
    service.getDnBDetails(map, dunsNo);
    DnbData data = (DnbData) map.get("data");
    if (data != null && data.getResults() != null && !data.getResults().isEmpty()) {
      return data.getResults().get(0);
    }
    return null;
  }

  public static boolean isDnbOverrideAttachmentProvided(EntityManager entityManager, long reqId) {
    String sql = ExternalizedQuery.getSql("QUERY.CHECK_DNB_MATCH_ATTACHMENT");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("ID", reqId);

    return query.exists();
  }

  public FindCMRResultModel findSingleReactCMRs(String cmrNo, String cmrCntry, int i, String searchCntry) {
    EntityManager entityManager = JpaManager.getEntityManager();
    FindCMRResultModel queryResponse = new FindCMRResultModel();
    try {

      String sql = ExternalizedQuery.getSql("FIND_SINGLE_REACT_RECORDS_RDC");
      PreparedQuery query = new PreparedQuery(entityManager, sql);
      query.setParameter("CMR_NO", cmrNo);
      query.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
      query.setParameter("COUNTRY", searchCntry);
      query.setForReadOnly(true);
      List<Kna1> resultList = query.getResults(Kna1.class);

      if (resultList != null && resultList.size() > 0) {
        List<FindCMRRecordModel> cmrRecordModels = new ArrayList<>();
        for (Kna1 kna1 : resultList) {
          FindCMRRecordModel cmrRecord = new FindCMRRecordModel();

          // Confirmed
          cmrRecord.setCmrName1Plain(kna1.getName1());
          cmrRecord.setCmrName2Plain(kna1.getName2());
          cmrRecord.setCmrName3(kna1.getName3());
          cmrRecord.setCmrName4(kna1.getName4());
          cmrRecord.setCmrOrderBlock(kna1.getAufsd());
          cmrRecord.setCmrIsu(kna1.getBrsch());
          cmrRecord.setCmrAffiliate(kna1.getKonzs());
          cmrRecord.setCmrAddrTypeCode(kna1.getKtokd());
          cmrRecord.setCmrAddrType("");
          cmrRecord.setCmrPrefLang(kna1.getSpras());
          cmrRecord.setCmrBusinessReg(kna1.getStcd1());
          cmrRecord.setCmrShortName(kna1.getTelx1());
          cmrRecord.setCmrVat(kna1.getStceg());
          cmrRecord.setCmrSubIndustry(kna1.getBran1());
          cmrRecord.setCmrPpsceid(kna1.getBran3());
          cmrRecord.setCmrSitePartyID(kna1.getBran5());
          cmrRecord.setCmrIssuedBy(kna1.getKatr6());
          cmrRecord.setCmrCapIndicator(kna1.getKatr8());
          cmrRecord.setCmrNum(kna1.getZzkvCusno());
          cmrRecord.setCmrInac(kna1.getZzkvInac());
          cmrRecord.setCmrCompanyNo(!StringUtils.isEmpty(kna1.getZzkvNode1()) ? kna1.getZzkvNode1().trim() : "");
          cmrRecord.setCmrEnterpriseNumber(!StringUtils.isEmpty(kna1.getZzkvNode2()) ? kna1.getZzkvNode2().trim() : "");
          cmrRecord.setCmrBusinessReg(kna1.getStcd1());
          cmrRecord.setCmrLocalTax2(kna1.getStcd2());
          cmrRecord.setCmrClass(kna1.getKukla());

          // (Addr table)Need to confirm with KNA1 table mapping and set
          cmrRecord.setCmrSapNumber(kna1.getId().getKunnr());
          cmrRecord.setCmrAddrSeq(kna1.getZzkvSeqno());

          cmrRecord.setCmrCity(!StringUtils.isEmpty(kna1.getOrt01()) ? kna1.getOrt01() : "");
          cmrRecord.setCmrCity2(!StringUtils.isEmpty(kna1.getOrt02()) ? kna1.getOrt02() : "");
          cmrRecord.setCmrState(!StringUtils.isEmpty(kna1.getRegio()) ? kna1.getRegio() : "");
          cmrRecord.setCmrPostalCode(!StringUtils.isEmpty(kna1.getPstlz()) ? kna1.getPstlz() : "");

          cmrRecord.setCmrCountyCode("");
          cmrRecord.setCmrCounty("");
          cmrRecord.setCmrStreetAddress(!StringUtils.isEmpty(kna1.getStras()) ? kna1.getStras() : "");
          cmrRecord.setCmrCustPhone("");
          cmrRecord.setCmrCustFax(!StringUtils.isEmpty(kna1.getTelfx()) ? kna1.getTelfx() : "");
          cmrRecord.setCmrBusNmLangCd("");
          cmrRecord.setCmrTransportZone(!StringUtils.isEmpty(kna1.getLzone()) ? kna1.getLzone() : "");
          cmrRecord.setCmrPOBox(!StringUtils.isEmpty(kna1.getPfach()) ? kna1.getPfach() : "");
          cmrRecord.setCmrPOBoxCity(!StringUtils.isEmpty(kna1.getPfort()) ? kna1.getPfort() : "");
          cmrRecord.setCmrPOBoxPostCode(!StringUtils.isEmpty(kna1.getPstl2()) ? kna1.getPstl2() : "");
          cmrRecord.setCmrBldg("");
          cmrRecord.setCmrFloor("");
          cmrRecord.setCmrOffice("");
          cmrRecord.setCmrDept("");

          cmrRecord.setCmrTier("");
          cmrRecord.setCmrInacType("");
          cmrRecord.setCmrIsic(!StringUtils.isEmpty(kna1.getZzkvSic())
              ? (kna1.getZzkvSic().trim().length() > 4 ? kna1.getZzkvSic().trim().substring(0, 4) : kna1.getZzkvSic().trim()) : "");
          cmrRecord.setCmrSortl("");
          cmrRecord.setCmrIssuedByDesc("");
          cmrRecord.setCmrRdcCreateDate("");
          cmrRecord.setCmrCountryLanded(!StringUtils.isEmpty(kna1.getLand1()) ? kna1.getLand1() : "");
          cmrRecordModels.add(cmrRecord);
        }

        queryResponse.setItems(cmrRecordModels);
        queryResponse.setSuccess(true);
        queryResponse.setMessage("Records found..");
      } else {
        queryResponse.setSuccess(false);
        queryResponse.setMessage("Records not found..");
      }

      return queryResponse;
    } finally {
      // empty the manager
      entityManager.clear();
      entityManager.close();
    }

  }

  private void setLockByName(Admin admin) {
    if (StringUtils.isBlank(admin.getLockByNm()) && !StringUtils.isBlank(admin.getLockBy())) {
      try {
        Person person = BluePagesHelper.getPerson(admin.getLockBy());
        if (person != null) {
          admin.setLockByNm(person.getName());
        }
      } catch (CmrException e) {
        this.log.warn("Name for " + admin.getLockBy() + " cannot be retrieved.");
      }
    }
  }

  private boolean reqIsCrosCompleted(EntityManager entityManager, Long reqId) {
    String sql = ExternalizedQuery.getSql("LA.GETCOUNT_COM_NOTIFY");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", reqId);
    int count = query.getSingleResult(Integer.class);
    if (count > 0) {
      return true;
    }
    return false;
  }

  /**
   * Reprocesses a single CREATE request. The process:<br>
   * <ul>
   * <li>Clears DATA.CMR_NO</li>
   * <li>Clears DATA.SITE_ID</li>
   * <li>Clears all ADDR.SAP_NO</li>
   * <li>Clears all ADDR.IERP_SITE_PRTY_ID</li>
   * <li>Clears ADMIN.RDC_PROCESSING_STATUS</li>
   * <li>Sets ADMIN.PROCESSED_FLAG = ''</li>
   * <li>Sets ADMIN.LOCK_IND = 'N'</li>
   * <li>Sets ADMIN.LOCK_BY = null</li>
   * <li>Sets ADMIN.LOCK_TS = null</li>
   * <li>Sets ADMIN.LOCK_BY_NM = null</li>
   * <li>Sets ADMIN.DISABLE_AUTO_PROC = 'N'</li>
   * <li>Sets ADMIN.REQ_STATUS = 'PCP'</li>
   * </ul>
   * The process also creates corresponding comment logs and workflow histories.
   * The function was added as part of CREATCMR-6971
   * 
   * @param model
   * @param entityManager
   * @param request
   * @throws CmrException
   * @throws SQLException
   */
  private void recreateCMR(RequestEntryModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException, SQLException {

    long reqId = model.getReqId();
    if (reqId > 0) {

      AppUser user = AppUser.getUser(request);
      if (!user.isCmde() && !user.isAdmin()) {
        throw new CmrException(new Exception("Reprocess can only be done by Administrators"));
      }
      StringBuilder comments = new StringBuilder();
      comments.append("** Forced Recreation of CMR **\n");
      this.log.debug("Retreiving Request " + reqId + " for Reprocess..");
      RequestData requestData = new RequestData(entityManager, reqId);

      Admin admin = requestData.getAdmin();
      if (!CmrConstants.REQ_TYPE_CREATE.equals(admin.getReqType())) {
        throw new CmrException(new Exception("Reprocess can only be done for Create requests."));
      }

      Data data = requestData.getData();
      comments.append("Previous CMR No.: " + data.getCmrNo()).append("\n");
      data.setCmrNo(null);
      data.setSitePartyId(null);
      this.log.debug("Saving data..");
      updateEntity(data, entityManager);

      List<Addr> addresses = requestData.getAddresses();
      for (Addr addr : addresses) {
        comments.append("Previous Address " + addr.getId().getAddrType() + "/" + addr.getId().getAddrSeq() + " KUNNR: " + addr.getSapNo())
            .append("\n");
        addr.setSapNo(null);
        addr.setIerpSitePrtyId(null);
        this.log.debug("Saving address..");
        updateEntity(addr, entityManager);
      }

      admin.setDisableAutoProc("N");
      admin.setLockInd("N");
      admin.setLockBy(null);
      admin.setLockByNm(null);
      admin.setLockTs(null);
      admin.setProcessedFlag("N");
      admin.setRdcProcessingStatus("");
      admin.setReqStatus("PCP");

      this.log.debug("Saving admin..");
      updateEntity(admin, entityManager);

      // create the workflow history of the status change
      RequestUtils.createWorkflowHistory(this, entityManager, user.getIntranetId(), admin, comments.toString(), "Recreate", null, null, true, null,
          null);
      RequestUtils.createCommentLog(this, entityManager, user, reqId, comments.toString());
    }

  }

  // CREATCMR-8430: do DNB check for NZ update -- Begin
  public ModelMap isDnBAPIMatchAddrsUpdateNZ(AutoDNBDataModel model, long reqId, String businessNumber) {
    ModelMap map = new ModelMap();
    EntityManager entityManager = null;
    try {
      if (reqId > 0) {
        entityManager = JpaManager.getEntityManager();
        RequestData requestData = new RequestData(entityManager, reqId);
        Data data = requestData.getData();
        Admin admin = requestData.getAdmin();
        RequestChangeContainer changes = new RequestChangeContainer(entityManager, data.getCmrIssuingCntry(), admin, requestData);

        ModelMap custNmMap = isCustNmMatchForNZ(requestData, changes, businessNumber);
        if ((boolean) custNmMap.get("success") && (boolean) custNmMap.get("custNmMatch") && (boolean) custNmMap.get("formerCustNmMatch")) {
          ModelMap addrMap = isAddressUpdateMatchForNZ(model, reqId, businessNumber, requestData, changes);
          map.putAll(custNmMap);
          map.put("matchesAddrDnb", (boolean) addrMap.get("matchesAddrDnb"));
          map.put("matchesAddrAPI", (boolean) addrMap.get("matchesAddrAPI"));
          map.put("message", addrMap.get("message"));
        } else {
          return custNmMap;
        }
      } else {
        map.put("success", false);
        map.put("custNmMatch", false);
        map.put("formerCustNmMatch", false);
        map.put("matchesAddrDnb", false);
        map.put("matchesAddrAPI", false);
        String message = "Invalid request Id";
        map.put("message", message);
      }
    } catch (Exception e) {
      log.debug("Error occurred while checking DnB Matches." + e);
      map.put("success", false);
      map.put("custNmMatch", false);
      map.put("formerCustNmMatch", false);
      map.put("matchesAddrDnb", false);
      map.put("matchesAddrAPI", false);
      String message = "An error occurred while matching with DnB.";
      map.put("message", message);
    } finally {
      if (entityManager != null) {
        entityManager.close();
      }
    }
    return map;

  }

  private AutomationResponse<NZBNValidationResponse> getNZBNService(String customerName, String businessNumber) throws Exception {
    AutomationServiceClient client = CmrServicesFactory.getInstance().createClient(SystemConfiguration.getValue("BATCH_SERVICES_URL"),
        AutomationServiceClient.class);
    client.setReadTimeout(1000 * 60 * 5);
    client.setRequestMethod(Method.Get);

    NZBNValidationRequest request = new NZBNValidationRequest();
    if (StringUtils.isNotBlank(businessNumber)) {
      request.setBusinessNumber(businessNumber);
      log.debug("request-businessNumber:" + request.getBusinessNumber());
    }
    request.setName(customerName);
    log.debug("request-name:" + customerName);

    AutomationResponse<?> rawResponse = client.executeAndWrap(AutomationServiceClient.NZ_BN_VALIDATION_SERVICE_ID, request, AutomationResponse.class);
    ObjectMapper mapper = new ObjectMapper();
    String json = mapper.writeValueAsString(rawResponse);
    TypeReference<AutomationResponse<NZBNValidationResponse>> ref = new TypeReference<AutomationResponse<NZBNValidationResponse>>() {
    };
    AutomationResponse<NZBNValidationResponse> nzbnResponse = mapper.readValue(json, ref);
    return nzbnResponse;
  }

  private ModelMap isCustNmMatchForNZ(RequestData requestData, RequestChangeContainer changes, String businessNumber) throws Exception {
    ModelMap map = new ModelMap();
    Admin admin = requestData.getAdmin();
    StringBuilder details = new StringBuilder();

    boolean CustNmChanged = changes.isLegalNameChanged();
    // CustNmChanged = false;
    if (CustNmChanged) {
      AutomationResponse<NZBNValidationResponse> response = null;
      Addr zs01 = requestData.getAddress("ZS01");
      String regex = "\\s+$";
      String customerName = zs01.getCustNm1() + (StringUtils.isBlank(zs01.getCustNm2()) ? "" : " " + zs01.getCustNm2());
      String formerCustName = !StringUtils.isBlank(admin.getOldCustNm1()) ? admin.getOldCustNm1().toUpperCase() : "";
      formerCustName += !StringUtils.isBlank(admin.getOldCustNm2()) ? " " + admin.getOldCustNm2().toUpperCase() : "";
      List<String> dnbTradestyleNames = new ArrayList<String>();
      boolean custNmMatch = false;
      boolean formerCustNmMatch = false;
      String errMessage = "";
      if (!(custNmMatch && formerCustNmMatch)) {
        log.debug("Now Checking with DNB to vrify CustNm update. customerName=" + customerName + ", formerCustName=" + formerCustName);
        MatchingResponse<DnBMatchingResponse> Dnbresponse = DnBUtil.getMatches(requestData, null, "ZS01");
        List<DnBMatchingResponse> matches = Dnbresponse.getMatches();
        if (!matches.isEmpty()) {
          for (DnBMatchingResponse dnbRecord : matches) {
            // checks for CustNm match
            String dnbCustNm = StringUtils.isBlank(dnbRecord.getDnbName()) ? "" : dnbRecord.getDnbName().replaceAll(regex, "");
            if (customerName.equalsIgnoreCase(dnbCustNm)) {
              custNmMatch = true;
              dnbTradestyleNames = dnbRecord.getTradeStyleNames();
              if (dnbTradestyleNames != null) {
                for (String tradestyleNm : dnbTradestyleNames) {
                  tradestyleNm = tradestyleNm.replaceAll(regex, "");
                  if (tradestyleNm.equalsIgnoreCase(formerCustName)) {
                    formerCustNmMatch = true;
                    break;
                  }
                }
              }
            }
          }
        }
      }

      if (!(custNmMatch && formerCustNmMatch)) {
        log.debug("DNB Checking CustNm match failed. Now Checking CustNm with NZBN API with  to vrify CustNm update");
        try {
          response = getNZBNService(customerName, businessNumber);
        } catch (Exception e) {
          if (response == null || !response.isSuccess()) {
            errMessage = "Failed to Connect to NZBN Service.";
            log.debug("\nFailed to Connect to NZBN Service.");
          }
        }
        if (response != null && response.isSuccess()) {
          // custNm Validation
          log.debug("\nSuccess to Connect to NZBN Service.");
          if (StringUtils.isNotEmpty(response.getRecord().getName())) {
            String responseCustNm = StringUtils.isBlank(response.getRecord().getName()) ? "" : response.getRecord().getName().replaceAll(regex, "");

            if (!(custNmMatch)) {
              if (customerName.equalsIgnoreCase(responseCustNm)) {
                custNmMatch = true;
                log.debug("\ncustNmMatch  = true to Connect to NZBN Service.");
              }
            }
          }
          if (response.getRecord() != null && response.getRecord().getPreviousEntityNames() != null
              && response.getRecord().getPreviousEntityNames().length > 0) {
            String[] historicalNameList = new String[response.getRecord().getPreviousEntityNames().length];
            historicalNameList = response.getRecord().getPreviousEntityNames();
            for (String historicalNm : historicalNameList) {
              historicalNm = historicalNm.replaceAll(regex, "");
              if (formerCustName.equalsIgnoreCase(historicalNm)) {
                formerCustNmMatch = true;
                log.debug("\nformerCustNmMatch = true to Connect to NZBN Service.");
              }
            }
          }
        }
      }

      map.put("success", true);
      map.put("custNmMatch", custNmMatch);
      map.put("formerCustNmMatch", formerCustNmMatch);
      map.put("message", errMessage);
      if (!custNmMatch || !formerCustNmMatch) {
        map.put("message", "The Customer Name and Former Customer Name doesn't match from DNB & NZBN API.");
        if (response != null && response.isSuccess() && response.getRecord() != null) {
          details.append(" Call NZBN API result - NZBN:  " + response.getRecord().getBusinessNumber() + " \n");
          details.append(" - Name.:  " + response.getRecord().getName() + " \n");
          if (response.getRecord().getPreviousEntityNames() != null) {
            String[] historicalNameList = new String[response.getRecord().getPreviousEntityNames().length];
            historicalNameList = response.getRecord().getPreviousEntityNames();
            for (String historicalNm : historicalNameList) {
              historicalNm = historicalNm.replaceAll(regex, "");
              details.append(" - historicalName:  " + historicalNm + " \n");
            }
          }
        }
        log.info(details.toString());
      }
    } else {
      map.put("success", true);
      map.put("custNmMatch", true);
      map.put("formerCustNmMatch", true);
      map.put("message", "");
      log.info("Updates to the dataFields fields skipped validation");
    }
    return map;
  }

  private ModelMap isAddressUpdateMatchForNZ(AutoDNBDataModel model, long reqId, String businessNumber, RequestData requestData,
      RequestChangeContainer changes) {
    ModelMap map = new ModelMap();
    map.put("matchesAddrSuccess", true);
    map.put("matchesAddrDnb", true);
    map.put("matchesAddrAPI", true);
    map.put("message", "");

    List<String> RELEVANT_ADDRESSES = Arrays.asList(CmrConstants.RDC_SOLD_TO, "MAIL", "ZP01", "ZI01", "ZF01", "CTYG", "CTYH");

    Addr zs01 = requestData.getAddress("ZS01");
    String customerName = zs01.getCustNm1() + (StringUtils.isBlank(zs01.getCustNm2()) ? "" : " " + zs01.getCustNm2());
    AutomationResponse<NZBNValidationResponse> nZBNAPIresponse = null;
    try {
      nZBNAPIresponse = getNZBNService(customerName, businessNumber);
    } catch (Exception e) {
      if (nZBNAPIresponse == null || !nZBNAPIresponse.isSuccess()) {
        log.debug("\nFailed to Connect to NZBN Service.");
      }
    }

    List<Addr> addresses = null;
    for (String addrType : RELEVANT_ADDRESSES) {
      if (changes.isAddressChanged(addrType)) {
        if (CmrConstants.RDC_SOLD_TO.equals(addrType)) {
          addresses = Collections.singletonList(requestData.getAddress(CmrConstants.RDC_SOLD_TO));
        } else {
          addresses = requestData.getAddresses(addrType);
        }
        for (Addr addr : addresses) {
          Addr addressToChk = requestData.getAddress(addrType);
          String errMessage = "";
          if ("Y".equals(addr.getChangedIndc())) {
            // update address
            if (RELEVANT_ADDRESSES.contains(addrType)) {
              if (isRelevantAddressFieldUpdated(changes, addr)) {

                Map dnbmatchmap = isDnBMatch(model, reqId, addr.getId().getAddrType());
                boolean matchesAddrDnb = (boolean) dnbmatchmap.get("dnbAddrMatch");
                boolean matchesAddrAPI = false;

                if (!matchesAddrDnb) {
                  log.debug("DNB Checking Addr match failed. Now Checking Addr with NZBN API with  to vrify Addr update");
                  if (nZBNAPIresponse != null && nZBNAPIresponse.isSuccess() && nZBNAPIresponse.getRecord() != null) {
                    // addr Validation
                    log.debug("\nSuccess to Connect to NZBN Service.");
                    ModelMap apiMatchMap = this.addrNZBNAPIMatch(addressToChk, addrType, nZBNAPIresponse.getRecord());
                    matchesAddrAPI = (boolean) apiMatchMap.get("matchesAddAPI");
                  }
                }
                if (!matchesAddrDnb && !matchesAddrAPI) {
                  map.put("addressType", addrType);
                  map.put("matchesAddrDnb", matchesAddrDnb);
                  map.put("matchesAddrAPI", matchesAddrAPI);
                  errMessage = "DNB Address and NZAPI Address match failed.";
                  map.put("message", errMessage);
                  return map;
                }

              } else {
                log.debug("Updates to non-address fields for " + addrType + "(" + addr.getId().getAddrSeq() + ") skipped in the checks.");
              }
            } else {
              // proceed
              log.debug("Update to Address " + addrType + "(" + addr.getId().getAddrSeq() + ") skipped in the checks.\\n");
            }
          } else if ("N".equals(addr.getImportInd())) {
            // new address addition
            Map dnbmatchmap = isDnBMatch(model, reqId, addr.getId().getAddrType());
            boolean matchesAddrDnb = (boolean) dnbmatchmap.get("dnbAddrMatch");
            boolean matchesAddrAPI = false;
            if (!matchesAddrDnb) {
              log.debug("DNB Checking Addr match failed. Now Checking Addr with NZBN API with to vrify Addr add");

              if (nZBNAPIresponse != null && nZBNAPIresponse.isSuccess() && nZBNAPIresponse.getRecord() != null) {
                // addr Validation
                log.debug("\nSuccess to Connect to NZBN Service.");
                ModelMap apiMatchMap = this.addrNZBNAPIMatch(addressToChk, addrType, nZBNAPIresponse.getRecord());
                matchesAddrAPI = (boolean) apiMatchMap.get("matchesAddAPI");
              }
            }

            if (!matchesAddrDnb && !matchesAddrAPI) {
              map.put("addressType", addrType);
              map.put("matchesAddrDnb", matchesAddrDnb);
              map.put("matchesAddrAPI", matchesAddrAPI);
              errMessage = "New address for " + addrType + " (" + addr.getId().getAddrSeq() + ") does not match with DNB and NZAPI.";
              map.put("message", errMessage);
              return map;
            }
          }
        }
        // End of addresses loop
      }
      // End of changed address type check
    }
    // End of address type loop
    return map;
  }

  /**
   * matching ZS01 for create request for NewZealand
   * 
   * @param model
   * @param reqId
   * @param businessNumber
   * @return
   */
  public ModelMap isDnBAPIMatchForNZCreate(AutoDNBDataModel model, long reqId, String businessNumber) {

    ModelMap map = new ModelMap();
    EntityManager entityManager = null;

    boolean apiSuccess = false;
    boolean apiCustNmMatch = false;
    boolean apiAddressMatch = false;
    String errorMsg = "";

    try {
      if (reqId > 0) {
        // match DNB for ZS01
        map = isDnBMatch(model, reqId, CmrConstants.RDC_SOLD_TO);
        log.debug("success ? " + map.get("success"));
        log.debug("isic match ? " + map.get("isicMatch"));
        log.debug("dnbNmMatch ? " + map.get("dnbNmMatch"));
        log.debug("dnbAddrMatch ? " + map.get("dnbAddrMatch"));
        log.debug("tradeStyleMatch ? " + map.get("tradeStyleMatch"));
        log.debug("confidenceCd ? " + map.get("confidenceCd"));

        entityManager = JpaManager.getEntityManager();

        String regex = "\\s+$";
        RequestData requestData = new RequestData(entityManager, reqId);
        Addr zs01 = requestData.getAddress(CmrConstants.RDC_SOLD_TO);
        String custNm = zs01.getCustNm1() + (StringUtils.isBlank(zs01.getCustNm2()) ? "" : " " + zs01.getCustNm2());

        if (requestData != null && (!StringUtils.isBlank(businessNumber) || !StringUtils.isBlank(custNm))) {
          log.debug("Validating Customer Name  " + custNm + " and Business Number " + businessNumber + " for New Zealand");

          String baseUrl = SystemConfiguration.getValue("CMR_SERVICES_URL");
          log.debug("Connecting to the NZBNValidation service at " + baseUrl);
          AutomationServiceClient autoClient = CmrServicesFactory.getInstance().createClient(baseUrl, AutomationServiceClient.class);
          autoClient.setReadTimeout(1000 * 60 * 5);
          autoClient.setRequestMethod(Method.Get);

          NZBNValidationRequest requestToAPI = new NZBNValidationRequest();
          requestToAPI.setBusinessNumber(businessNumber);
          requestToAPI.setName(custNm);
          log.debug("requestToAPI: businessNumber = " + requestToAPI.getBusinessNumber() + ", custNm = " + custNm);

          AutomationResponse<?> rawResponse = autoClient.executeAndWrap(AutomationServiceClient.NZ_BN_VALIDATION_SERVICE_ID, requestToAPI,
              AutomationResponse.class);
          ObjectMapper mapper = new ObjectMapper();
          String json = mapper.writeValueAsString(rawResponse);

          TypeReference<AutomationResponse<NZBNValidationResponse>> ref = new TypeReference<AutomationResponse<NZBNValidationResponse>>() {
          };
          AutomationResponse<NZBNValidationResponse> nzbnResponse = mapper.readValue(json, ref);
          if (nzbnResponse != null && nzbnResponse.isSuccess()) {
            apiSuccess = true;
            NZBNValidationResponse nzbnResRec = nzbnResponse.getRecord();
            if (nzbnResRec != null) {
              String responseCustNm = StringUtils.isBlank(nzbnResRec.getName()) ? "" : nzbnResRec.getName().replaceAll(regex, "");
              if (custNm.equalsIgnoreCase(responseCustNm)) {
                apiCustNmMatch = true;
                ModelMap zs01ApiMatchMap = this.addrNZBNAPIMatch(zs01, CmrConstants.RDC_SOLD_TO, nzbnResRec);
                apiAddressMatch = (boolean) zs01ApiMatchMap.get("matchesAddAPI");
              }

              if (!apiAddressMatch) {
                errorMsg = "NZBN Validation Failed - ZS01 address does not match with NZBN API.";
              }
              if (!apiCustNmMatch) {
                errorMsg = "NZBN Validation Failed - Customer Name does not match with NZBN API.";
              }
            }
          } else {
            String respMsg = StringUtils.isNotBlank(nzbnResponse.getMessage()) ? nzbnResponse.getMessage() : "";
            errorMsg = "NZBN Validation Failed - ";
            if (respMsg.replaceAll(regex, "").contains("No Response on the Request".replaceAll(regex, ""))) {
              errorMsg += "Invalid NZBN.";
            } else {
              errorMsg += respMsg;
            }
          }

          // to check other addresses -- BEGIN
          boolean otherAddrDNBMatch = true;
          boolean otherAddrAPIMatch = true;
          List<String> RELEVANT_ADDRESSES = Arrays.asList("MAIL", "ZP01", "ZI01", "ZF01", "CTYG", "CTYH");
          List<Addr> addresses = null;
          for (String addrType : RELEVANT_ADDRESSES) {
            log.debug("Start matching for other addresses...");
            addresses = requestData.getAddresses(addrType);
            for (Addr addr : addresses) {
              ModelMap otherAddrDNBMatchRes = isDnBMatch(model, reqId, addrType);
              otherAddrDNBMatch = (boolean) otherAddrDNBMatchRes.get("dnbAddrMatch");
              if (!otherAddrDNBMatch) {
                log.debug("DNB Checking Addr match failed. Now Checking Addr with NZBN API with to vrify Addr add");
                if (nzbnResponse != null && nzbnResponse.isSuccess() && nzbnResponse.getRecord() != null) {
                  ModelMap zs01ApiMatchMap = this.addrNZBNAPIMatch(addr, addrType, nzbnResponse.getRecord());
                  otherAddrAPIMatch = (boolean) zs01ApiMatchMap.get("matchesAddAPI");
                  if (!otherAddrAPIMatch) {
                    errorMsg = "New address for " + addrType + " (" + addr.getId().getAddrSeq() + ") does not match with DNB and NZAPI.";
                    break;
                  }
                } else {
                  otherAddrAPIMatch = false;
                  errorMsg = "No data retrieved from NZBN API.";
                  break;
                }
              }
            } // End of addresses loop
            if (!otherAddrDNBMatch && !otherAddrAPIMatch) {
              break;
            }
          } // End of address type loop
          // to check other addresses -- End

          map.put("otherAddrDNBMatch", otherAddrDNBMatch);
          map.put("otherAddrAPIMatch", otherAddrAPIMatch);
        }

      } else {
        log.error("Invalid request Id");
        map.put("apiSuccess", false);
        map.put("success", false);
        map.put("message", "Invalid request Id");
        return map;
      }

    } catch (Exception e) {
      log.error("Error occured in D&B & NZBN matching", e);
      map.put("apiSuccess", false);
      map.put("success", false);
      map.put("message", "Error occured in D&B & NZBN matching");
      return map;
    } finally {
      if (entityManager != null) {
        entityManager.close();
      }
    }

    map.put("apiSuccess", apiSuccess);
    map.put("apiCustNmMatch", apiCustNmMatch);
    map.put("apiAddressMatch", apiAddressMatch);
    map.put("message", errorMsg);
    return map;

  }

  private ModelMap addrNZBNAPIMatch(Addr addr, String addrType, NZBNValidationResponse nzbnResp) {
    String regexForAddr = "\\s+|$";
    boolean matchesAddAPI = false;
    ModelMap map = new ModelMap();
    StringBuffer details = new StringBuffer();

    String addressAll = addr.getCustNm1() + (addr.getCustNm2() == null ? "" : addr.getCustNm2()) + addr.getAddrTxt()
        + (addr.getAddrTxt2() == null ? "" : addr.getAddrTxt2()) + (addr.getStateProv() == null ? "" : addr.getStateProv())
        + (addr.getCity1() == null ? "" : addr.getCity1()) + (addr.getPostCd() == null ? "" : addr.getPostCd());
    addressAll = addressAll.toUpperCase();
    log.debug("\n Checking address " + addrType + "(" + addr.getId().getAddrSeq() + ") : " + addressAll);

    if (StringUtils.isNotEmpty(nzbnResp.getAddress())
        && addressAll.replaceAll(regexForAddr, "").contains(nzbnResp.getAddress().replaceAll(regexForAddr, "").toUpperCase())
        && StringUtils.isNotEmpty(nzbnResp.getCity())
        && addressAll.replaceAll(regexForAddr, "").contains(nzbnResp.getCity().replaceAll(regexForAddr, "").toUpperCase())
        && StringUtils.isNotEmpty(nzbnResp.getPostal())
        && addressAll.replaceAll(regexForAddr, "").contains(nzbnResp.getPostal().replaceAll(regexForAddr, "").toUpperCase())) {
      matchesAddAPI = true;

      details.append(" - Address.:  " + nzbnResp.getAddress() + " \n");
      details.append(" - City.:  " + nzbnResp.getCity() + " \n");
      details.append(" - Postal.:  " + nzbnResp.getPostal() + " \n");

      log.debug("\n" + addrType + "(" + addr.getId().getAddrSeq() + ") matchesAddAPI:true.");
    }

    // CREATCMR-8430: checking if the address matches with
    // service type address from API
    log.debug("REGISTERED Address matched ?  " + matchesAddAPI);
    String serviceAddr = nzbnResp.getServiceAddressDetail();
    if (!matchesAddAPI && StringUtils.isNotEmpty(serviceAddr)) {
      log.debug("****** addressOfRequest: " + addressAll);
      log.debug("****** serviceAddr: " + serviceAddr);
      String[] serviceAddrArr = serviceAddr.split("\\^");
      boolean serviceFlag = false;
      for (String partAddr : serviceAddrArr) {
        serviceFlag = (addressAll.replaceAll(regexForAddr, "").contains(partAddr.replaceAll(regexForAddr, "").toUpperCase()));
        if (!serviceFlag) {
          break;
        }
      }

      matchesAddAPI = serviceFlag;
      if (serviceFlag) {
        if (serviceAddrArr.length == 3) {
          details.append(" - Address.:  " + serviceAddrArr[0] + " \n");
          details.append(" - City.:  " + serviceAddrArr[1] + " \n");
          details.append(" - Postal.:  " + serviceAddrArr[2] + " \n");
        }
      }
      log.debug("SERVICE Address matched ?  " + serviceFlag);
    }

    map.put("matchesAddAPI", matchesAddAPI);
    map.put("detail", details);

    return map;
  }

  // CREATCMR-8430: do DNB check for NZ update -- End

}
