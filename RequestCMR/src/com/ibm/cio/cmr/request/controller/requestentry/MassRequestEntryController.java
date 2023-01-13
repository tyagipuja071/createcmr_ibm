/**
 * 
 */
package com.ibm.cio.cmr.request.controller.requestentry;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.activation.MimetypesFileTypeMap;
import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.controller.BaseController;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.masschange.MassChangeTemplateManager;
import com.ibm.cio.cmr.request.masschange.obj.MassChangeTemplate;
import com.ibm.cio.cmr.request.model.ParamContainer;
import com.ibm.cio.cmr.request.model.ProcessResultModel;
import com.ibm.cio.cmr.request.model.approval.ApprovalResponseModel;
import com.ibm.cio.cmr.request.model.requestentry.AttachmentModel;
import com.ibm.cio.cmr.request.model.requestentry.DataModel;
import com.ibm.cio.cmr.request.model.requestentry.DeleteReactivateModel;
import com.ibm.cio.cmr.request.model.requestentry.MassUpdateModel;
import com.ibm.cio.cmr.request.model.requestentry.NotifyListModel;
import com.ibm.cio.cmr.request.model.requestentry.RequestEntryModel;
import com.ibm.cio.cmr.request.service.requestentry.CommentLogService;
import com.ibm.cio.cmr.request.service.requestentry.DeleteReactivateService;
import com.ibm.cio.cmr.request.service.requestentry.MassRequestEntryService;
import com.ibm.cio.cmr.request.service.requestentry.MassUpdtService;
import com.ibm.cio.cmr.request.ui.PageManager;
import com.ibm.cio.cmr.request.user.AppUser;
import com.ibm.cio.cmr.request.util.JpaManager;
import com.ibm.cio.cmr.request.util.MessageUtil;
import com.ibm.cio.cmr.request.util.RequestUtils;
import com.ibm.cio.cmr.request.util.SystemUtil;
import com.ibm.cio.cmr.request.util.geo.GEOHandler;
import com.ibm.cio.cmr.request.util.geo.impl.CNDHandler;
import com.ibm.cio.cmr.request.util.geo.impl.DEHandler;
import com.ibm.cio.cmr.request.util.geo.impl.LAHandler;
import com.ibm.cio.cmr.request.util.geo.impl.SWISSHandler;
import com.ibm.cio.cmr.request.util.legacy.LegacyDirectUtil;

/**
 * Controller for handling request entry page functions
 * 
 * @author Sonali Jain
 * 
 */
@Controller
public class MassRequestEntryController extends BaseController {

  private static Logger LOG = Logger.getLogger(MassRequestEntryController.class);
  private static final MimetypesFileTypeMap MIME_TYPES = new MimetypesFileTypeMap();
  private static final String MASS_UPDATE_TEMPLATE = "MassUpdateTemplate.xlsx";
  private static final String MASS_CREATE_TEMPLATE = "MassCreateTemplateUS";

  @Autowired
  MassRequestEntryService service;

  @Autowired
  CommentLogService cmtservice;

  @Autowired
  MassUpdtService massUpdtService;

  @Autowired
  private DeleteReactivateService delReactivateService;

  @RequestMapping(
      value = "/massrequest/{reqId1}")
  public ModelAndView showRequestDetail(@PathVariable("reqId1") long reqId, HttpServletRequest request, HttpServletResponse response,
      RequestEntryModel model) throws Exception {
    ModelAndView mv = null;
    if (model.getReqId() == 0) {
      model.setReqId(reqId);
    }
    Thread.currentThread().setName("REQ-" + model.getReqId());
    request.getSession().setAttribute("lastReqId", reqId);
    String fromUrl = model.getFromUrl();
    String redirectUrl = model.getRedirectUrl();
    if (StringUtils.isEmpty(fromUrl)) {
      fromUrl = (String) request.getSession().getAttribute("_fromUrl");
    } else {
      request.getSession().setAttribute("_fromUrl", fromUrl);
    }

    String action = model.getAction();

    if (!StringUtils.isEmpty(action)) {
      mv = processYourAction(request, response, model);
      if (!StringUtils.isEmpty(redirectUrl)) {
        mv.setViewName("redirect:" + redirectUrl);
      }
      model.setFromUrl(fromUrl);
      // this is an existing request, set date values in specified format
      setExistingReqParamValues(model, request);
      setYourActionsAttributes(request, model, false);
    } else {
      if (!shouldProcess(model)) {
        if (model.allKeysAssigned()) {
          List<RequestEntryModel> results = service.search(model, request);
          if (results.size() > 0) {
            // assign this record as the model to show on the page
            RequestEntryModel reqModel = results.get(0);

            if (dplStatusInvalid(reqModel)) {
              if (model.getCmrIssuingCntry().equalsIgnoreCase("631")) {
                service.clearDplResults(reqModel.getReqId(), null, false);
                mv = new ModelAndView("redirect:/request/" + reqModel.getReqId(), "reqentry", reqModel);
                MessageUtil.setErrorMessage(mv, MessageUtil.ERROR_DPL_RESET);
              }
            }

            reqModel.setFromUrl(fromUrl);
            // this is an existing request, set date values in specified
            // format
            setExistingReqParamValues(reqModel, request);
            setYourActionsAttributes(request, reqModel, false);

            // If normal request record, redirect to Normal Request page...
            if (!StringUtils.isEmpty(reqModel.getReqType())
                && ("C".equals(reqModel.getReqType().trim()) || "U".equals(reqModel.getReqType().trim()))) {
              mv = new ModelAndView("redirect:/request/" + reqId, "reqentry", reqModel);
              Thread.currentThread().setName("Executor-" + Thread.currentThread().getId());
              return mv;
            }
            Admin admin = service.getCurrentRecordById(reqId);
            reqModel.setIterId(admin.getIterationId());

            // set approval result
            service.setApprovalResult(reqModel);

            if (reqModel.getReqType().trim().equalsIgnoreCase(CmrConstants.REQ_TYPE_UPDT_BY_ENT)) {
              service.setEnterpriseModel(reqId, reqModel);
            }
            model = reqModel;
            mv = new ModelAndView("massrequestentry", "reqentry", reqModel);
          }
        }
      }
    }
    if (mv == null) {
      mv = new ModelAndView("massrequestentry", "reqentry", model);
    }

    model.setAction(null);

    setPageKeys("REQUEST", "REQUEST", mv);
    addExtraModelEntries(mv, model);

    Thread.currentThread().setName("Executor-" + Thread.currentThread().getId());
    return mv;
  }

  private boolean dplStatusInvalid(RequestEntryModel model) {
    // 1150478: for completed records, do not reset DPL, JZ Mar 2, 2017
    if ("COM".equals(model.getReqStatus())) {
      LOG.debug("Completed record, DPL check will not be reset.");
      return false;
    }
    if ("NR".equals(model.getDplChkResult())) {
      LOG.debug("DPL Check is not required for this record, will not reset status.");
      return false;
    }
    Date ts = model.getDplChkTs();
    if (ts == null) {
      return false;
    }
    Calendar cal = Calendar.getInstance();
    cal.setTimeInMillis(ts.getTime());

    Timestamp current = SystemUtil.getCurrentTimestamp();
    Calendar cal1 = Calendar.getInstance();
    cal1.setTimeInMillis(current.getTime());

    if (cal.get(Calendar.YEAR) == cal1.get(Calendar.YEAR)) {
      if (cal.get(Calendar.MONTH) == cal1.get(Calendar.MONTH)) {
        return false;
      }
    }
    return true;
  }

  @RequestMapping(
      value = "/massrequest")
  public ModelAndView showRequestEntryPage(HttpServletRequest request, HttpServletResponse response, RequestEntryModel model) throws Exception {

    ModelAndView mv = null;

    if (!"Y".equals(request.getParameter("create"))) {
      Long lastReqId = (Long) request.getSession().getAttribute("lastReqId");
      if (lastReqId != null) {
        mv = new ModelAndView("redirect:/massrequest/" + lastReqId.longValue(), "reqentry", model);
        return mv;
      }
    } else {
      request.getSession().removeAttribute("lastReqId");
    }

    request.getSession().removeAttribute("_fromUrl");

    String action = model.getAction();
    String redirectUrl = model.getRedirectUrl();
    if (!StringUtils.isEmpty(action)) {
      // save
      mv = processYourAction(request, response, model);
      if (!StringUtils.isEmpty(redirectUrl)) {
        mv.setViewName("redirect:" + redirectUrl);
      }
    } else {
      // this is a new request, set default values
      setDefaultValues(model, request);
      mv = new ModelAndView("massrequestentry", "reqentry", model);
    }
    setPageKeys("REQUEST", "REQUEST", mv);
    addExtraModelEntries(mv, model);

    setYourActionsAttributes(request, model, true);

    return mv;
  }

  /**
   * Sets the default values
   */
  private void setDefaultValues(RequestEntryModel model, HttpServletRequest request) throws Exception {

    AppUser user = AppUser.getUser(request);

    // lock the new request to the user
    // model.setLockBy(user.getIntranetId());
    // model.setLockByNm(user.getBluePagesName());
    // model.setLockInd(CmrConstants.YES_NO.Y.toString());
    model.setYourId(user.getIntranetId());
    model.setYourNm(user.getBluePagesName());
    model.setRequestingLob(user.getDefaultLineOfBusn());
    model.setReqReason(user.getDefaultRequestRsn());

    if (!StringUtils.isEmpty(model.getNewReqCntry())) {
      if (model.getNewReqCntry().length() > 3) {
        // for extensions, they will have CMR issuing country + A-Z, so assign
        // only first 3
        model.setCountryUse(model.getNewReqCntry());
        model.setCmrIssuingCntry(model.getNewReqCntry().substring(0, 3));
      } else {
        model.setCountryUse(model.getNewReqCntry());
        model.setCmrIssuingCntry(model.getNewReqCntry());
      }
    } else {
      model.setCmrIssuingCntry(user.getCmrIssuingCntry());
    }

    if (!StringUtils.isEmpty(model.getNewReqType())) {
      model.setReqType(model.getNewReqType());
    }

    // new entries are with Draft status
    model.setReqStatus(CmrConstants.REQUEST_STATUS.DRA.toString());
    model.setOverallStatus(CmrConstants.ReqStatus_Draft); // TODO retrieve
                                                          // description from DB
    model.setUserRole(CmrConstants.Role_Requester);
    model.setApprovalResult(CmrConstants.APPROVAL_RESULT_NONE);

    if (model.getCmrIssuingCntry().equalsIgnoreCase("631")) {
      model.setFindCmrResult(CmrConstants.Scorecard_Not_Done);
      model.setDplChkResult(CmrConstants.Scorecard_Not_Done);
      model.setFindDnbResult(CmrConstants.Scorecard_Not_Done);
      model.setAddrStdResult(CmrConstants.Scorecard_Not_Done);
    } else {
      model.setDplChkResult(CmrConstants.Scorecard_Not_Required);
    }
  }

  /**
   * Sets the default values
   */
  private void setExistingReqParamValues(RequestEntryModel reqModel, HttpServletRequest request) {

    AppUser user = AppUser.getUser(request);
    SimpleDateFormat dateFormat = CmrConstants.DATE_FORMAT();
    // general screen date fields
    if (reqModel.getCreateTs() != null) {
      reqModel.setCreateDate(dateFormat.format(reqModel.getCreateTs()));
    }
    if (reqModel.getLastUpdtTs() != null) {
      reqModel.setLstUpdDate(dateFormat.format(reqModel.getLastUpdtTs()));
    }
    if (reqModel.getLockTs() != null) {
      reqModel.setLockedDate(dateFormat.format(reqModel.getLockTs()));
    }

    if (reqModel.getCmrIssuingCntry().equalsIgnoreCase("631")) {
      if (reqModel.getDplChkTs() != null) {
        reqModel.setDplChkDate(dateFormat.format(reqModel.getDplChkTs()));
      }
    }

    reqModel.setYourId(user.getIntranetId());
    reqModel.setYourNm(user.getBluePagesName());
    if (!StringUtils.isEmpty(reqModel.getCmrIssuingCntry())) {
      reqModel.setCmrIssuingCntry(reqModel.getCmrIssuingCntry());
    }
    // your role field
    String requestStatus = reqModel.getReqStatus();
    if (CmrConstants.YES_NO.Y.toString().equals(reqModel.getLockInd())) {
      if (!StringUtils.isEmpty(reqModel.getLockBy())) {
        if (reqModel.getLockBy().equals(user.getIntranetId())) {
          if (CmrConstants.REQUEST_STATUS.DRA.toString().equals(requestStatus) || CmrConstants.REQUEST_STATUS.SVA.toString().equals(requestStatus)) {
            reqModel.setUserRole(CmrConstants.Role_Requester);
          } else if (CmrConstants.REQUEST_STATUS.PCO.toString().equals(requestStatus)
              || CmrConstants.REQUEST_STATUS.PVA.toString().equals(requestStatus) || CmrConstants.REQUEST_STATUS.PCR.toString().equals(requestStatus)
              || CmrConstants.REQUEST_STATUS.ACU.toString().equals(requestStatus)
              || CmrConstants.REQUEST_STATUS.SV2.toString().equals(requestStatus)) {
            reqModel.setUserRole(CmrConstants.Role_Processor);
          } else if (CmrConstants.REQUEST_STATUS.IIP.toString().equals(requestStatus)) {
            reqModel.setUserRole(CmrConstants.Role_Info_Provider);
          } else if (CmrConstants.REQUEST_STATUS.RIP.toString().equals(requestStatus)) {
            reqModel.setUserRole(CmrConstants.Role_Reviewer);
          }
        } else {
          reqModel.setUserRole(CmrConstants.Role_Viewer);
        }
      }
    } else {
      reqModel.setUserRole(CmrConstants.Role_Viewer);
    }
  }

  /**
   * Where the other models for the embedded forms should be added
   * 
   * @param mv
   * @throws CmrException
   */
  private void addExtraModelEntries(ModelAndView mv, RequestEntryModel model) throws CmrException {
    mv.addObject("notif", new NotifyListModel());
    mv.addObject("attach", new AttachmentModel());
    DataModel rdcData = service.getRdcDataModel(model.getReqId());
    mv.addObject("rdcdata", rdcData);

    // Fixed Approval type for Mass Change requests
    ApprovalResponseModel approval = new ApprovalResponseModel();
    // approval.setTypId(CmrConstants.MASS_REQUEST_APPROVAL_TYPE_ID);
    mv.addObject("approval", approval);
    service.appendExtraModelEntries(mv, model);
  }

  /**
   * Sets the attributes needed by the Your Actions dropdown
   * 
   * @param request
   * @param newRequest
   */
  private void setYourActionsAttributes(HttpServletRequest request, RequestEntryModel model, boolean newRequest) {
    AppUser user = AppUser.getUser(request);
    String currUser = user.getIntranetId();
    String lockUser = model.getLockBy();
    String requester = model.getRequesterId() != null ? model.getRequesterId() : "x";
    boolean sameOriginator = requester.equals(model.getOriginatorId());
    String lockInd = "";
    String sqlId = "YOUR_ACTIONS";
    String claimRole = model.getClaimRole();
    String canClaim = model.getCanClaim();
    String canClaimAll = model.getCanClaimAll();

    if (CmrConstants.YES_NO.Y.toString().equals(model.getLockInd() != null ? model.getLockInd().trim() : "")) {
      if (currUser.equals(lockUser)) {
        // if current user is the one locking the request, proceed with normal
        // status trans check
        lockInd = "Y";
      } else {
        // avoid status trans check if locking user is not current user
        lockInd = "X";
      }
    } else {
      if (("P".equals(claimRole) || "R".equals(claimRole) || "D".equals(claimRole)) && ("Y".equals(canClaim) || "Y".equals(canClaimAll))) {
        // if not locked, check for Requester, Delegate, Processor role
        lockInd = "N";
      } else {
        // if not locked and not business with the request, avoid status trans
        // check
        lockInd = "X";
      }
    }

    // if claim role is delegate of locking (L) or originator himself, show
    // unlock
    if (("L".equals(claimRole) || "O".equals(claimRole)) && !sameOriginator) {
      sqlId = "YOUR_ACTIONS_UNLOCK";
    }

    // new request, only save and validate available
    if (newRequest) {
      sqlId = "YOUR_ACTIONS_NEW";
    }

    if (("X".equals(lockInd) || ("P".equals(claimRole) || "R".equals(claimRole) || "D".equals(claimRole))
        || (("L".equals(claimRole) || "O".equals(claimRole)) && !sameOriginator)) && !newRequest) {
      request.setAttribute("yourActionsViewOnly", true);
    }

    request.setAttribute("yourActionsSqlId", sqlId);
    request.setAttribute("yourActionsLockInd", lockInd);
  }

  /* Your Action Controller */

  public ModelAndView processYourAction(HttpServletRequest request, HttpServletResponse response, RequestEntryModel model) {
    ModelAndView mv = null;
    try {
      service.processTransaction(model, request);

      mv = new ModelAndView("redirect:/massrequest/" + model.getReqId(), "reqentry", new RequestEntryModel());

      String action = model.getAction();

      if (CmrConstants.Save().equalsIgnoreCase(action)) {
        mv = new ModelAndView("redirect:/massrequest/" + model.getReqId(), "reqentry", new RequestEntryModel());
        if ("Y".equals(model.getHasError())) {
          MessageUtil.setInfoMessage(mv, MessageUtil.INFO_REQUEST_SAVED_WITH_ERROR, model.getReqId() + "");
        } else {
          MessageUtil.setInfoMessage(mv, MessageUtil.INFO_REQUEST_SAVED, model.getReqId() + "");
        }

      } else if ("CREATE_NEW".equalsIgnoreCase(action)) {
        mv = new ModelAndView("redirect:/massrequest/" + model.getReqId(), "reqentry", new RequestEntryModel());
        MessageUtil.setInfoMessage(mv, MessageUtil.INFO_REQUEST_CREATED, model.getReqId() + "");
      } else if ("REJECT_SEARCH".equalsIgnoreCase(action) || "NO_DATA_SEARCH".equalsIgnoreCase(action)) {
        mv = new ModelAndView("redirect:/massrequest/" + model.getReqId(), "reqentry", new RequestEntryModel());
        MessageUtil.setInfoMessage(mv, MessageUtil.INFO_REQUEST_SAVED_REJECT, model.getReqId() + "");
      } else if (CmrConstants.Claim().equalsIgnoreCase(action)) {
        mv = new ModelAndView("redirect:/massrequest/" + model.getReqId(), "reqentry", new RequestEntryModel());
        MessageUtil.setInfoMessage(mv, MessageUtil.INFO_REQUEST_PROCESSED, "claimed", model.getReqId() + "");

      } else if (CmrConstants.Unlock().equalsIgnoreCase(action)) {
        mv = new ModelAndView("redirect:/workflow/open", "reqentry", new RequestEntryModel());
        MessageUtil.setInfoMessage(mv, MessageUtil.INFO_REQUEST_PROCESSED, "unlocked", model.getReqId() + "");

      } else if (CmrConstants.Send_for_Processing().equalsIgnoreCase(action)) {

        if ((!StringUtils.isBlank(model.getMessageDefaultApproval()))) {
          mv = new ModelAndView("redirect:/massrequest/" + model.getReqId(), "reqentry", new RequestEntryModel());
          // CREATCMR 537
          if (model.getApprovalResult().equalsIgnoreCase("Pending")) {
            MessageUtil.setErrorMessage(mv, MessageUtil.ERROR_APPROVAL_DEFAULT_DRA);
          }
        } else {
          mv = new ModelAndView("redirect:/workflow/open", "reqentry", new RequestEntryModel());
          MessageUtil.setInfoMessage(mv, MessageUtil.INFO_REQUEST_PROCESSED, "sent for processing", model.getReqId() + "");
        }

      } else if (CmrConstants.Processing_Validation_Complete().equalsIgnoreCase(action)) {
        mv = new ModelAndView("redirect:/workflow/open", "reqentry", new RequestEntryModel());
        MessageUtil.setInfoMessage(mv, MessageUtil.INFO_REQUEST_PROCESSED, "updated to completed validation", model.getReqId() + "");

      } else if (CmrConstants.Processing_Validation_Complete2().equalsIgnoreCase(action)) {
        mv = new ModelAndView("redirect:/massrequest/" + model.getReqId(), "reqentry", new RequestEntryModel());
        MessageUtil.setInfoMessage(mv, MessageUtil.INFO_REQUEST_PROCESSED, "updated to completed validation", model.getReqId() + "");
      } else if (CmrConstants.Processing_Create_Up_Complete().equalsIgnoreCase(action)) {
        mv = new ModelAndView("redirect:/workflow/open", "reqentry", new RequestEntryModel());
        MessageUtil.setInfoMessage(mv, MessageUtil.INFO_REQUEST_PROCESSED, "updated to completed processing", model.getReqId() + "");

      } else if (CmrConstants.All_Processing_Complete().equalsIgnoreCase(action)) {
        mv = new ModelAndView("redirect:/workflow/open", "reqentry", new RequestEntryModel());
        MessageUtil.setInfoMessage(mv, MessageUtil.INFO_REQUEST_PROCESSED, "updated to completed", model.getReqId() + "");

      } else if (CmrConstants.Reject().equalsIgnoreCase(action)) {
        mv = new ModelAndView("redirect:/workflow/open", "reqentry", new RequestEntryModel());
        MessageUtil.setInfoMessage(mv, MessageUtil.INFO_REQUEST_PROCESSED, "rejected", model.getReqId() + "");

      } else if (CmrConstants.Cancel_Processing().equalsIgnoreCase(action)) {
        mv = new ModelAndView("redirect:/workflow/open", "reqentry", new RequestEntryModel());
        MessageUtil.setInfoMessage(mv, MessageUtil.INFO_REQUEST_CANCEL_PROCESSING, model.getReqId() + "");

      } else if (CmrConstants.Cancel_Request().equalsIgnoreCase(action)) {
        mv = new ModelAndView("redirect:/workflow/open", "reqentry", new RequestEntryModel());
        MessageUtil.setInfoMessage(mv, MessageUtil.INFO_REQUEST_PROCESSED, "cancelled", model.getReqId() + "");
      } else if (CmrConstants.Create_Update_CMR().equalsIgnoreCase(action)) {
        mv = new ModelAndView("redirect:/workflow/open", "reqentry", new RequestEntryModel());
        MessageUtil.setInfoMessage(mv, MessageUtil.INFO_REQUEST_PROCESSED, "updated to processing pending", model.getReqId() + "");
      } else if (CmrConstants.Create_Update_Approved().equalsIgnoreCase(action)) {
        mv = new ModelAndView("redirect:/massrequest/" + model.getReqId(), "reqentry", new RequestEntryModel());
        MessageUtil.setInfoMessage(mv, MessageUtil.INFO_REQUEST_PROCESSED, "updated to processing pending", model.getReqId() + "");
      } else if (CmrConstants.Mark_as_Completed().equalsIgnoreCase(action)) {
        mv = new ModelAndView("redirect:/workflow/open", "reqentry", new RequestEntryModel());
        MessageUtil.setInfoMessage(mv, MessageUtil.INFO_REQUEST_PROCESSED, "marked to completed", model.getReqId() + "");
      } else if ("DPL_CHECK".equalsIgnoreCase(action)) {
        mv = new ModelAndView("redirect:/workflow/open", "reqentry", new RequestEntryModel());
        MessageUtil.setInfoMessage(mv, MessageUtil.INFO_MASSLDDPLCHECK_SUCCESS, "mass dpl checked", model.getReqId() + "");
      }

      if (model.getCmrIssuingCntry().equalsIgnoreCase("631")) {
        if (!StringUtils.isEmpty(model.getDplMessage())) {
          MessageUtil.setInfoMessage(mv, model.getDplMessage());
        }
      }

    } catch (Exception e) {
      mv = new ModelAndView("massrequestentry", "reqentry", model);
      setPageKeys("REQUEST", "REQUEST", mv);
      if (e instanceof CmrException) {
        if (model.getEmeaSeqNo() >= 3) {
          MessageUtil.setErrorMessage(mv, ((CmrException) e).getCode(), model.getEmeaSeqNo());
        } else
          MessageUtil.setErrorMessage(mv, ((CmrException) e).getCode());
      } else {
        MessageUtil.setErrorMessage(mv, MessageUtil.ERROR_CANNOT_SAVE_REQUEST);
      }
    }
    return mv;
  }

  @RequestMapping(
      value = "/massrequest/download",
      method = RequestMethod.POST)
  public void downloadMassFile(HttpServletRequest request, HttpServletResponse response) throws Exception {
    String token = request.getParameter("dlTokenId");
    String reqId = request.getParameter("dlReqId");
    String docType = request.getParameter("dlDocType");
    String cmrIssuingCntry = request.getParameter("cmrIssuingCntry");
    String iterId = request.getParameter("dlIterId");

    String docLink = null;
    String templateName = null;
    try {
      Admin admin = service.getCurrentRecordById(Long.parseLong(reqId));
      if (admin != null) {
        if (CmrConstants.REQ_TYPE_MASS_CREATE.equals(admin.getReqType())) {
          docLink = SystemConfiguration.getSystemProperty("masscreate." + cmrIssuingCntry);
          templateName = docLink;
          if (StringUtils.isEmpty(docLink)) {
            docLink = MASS_CREATE_TEMPLATE + ".xlsm";
            templateName = MASS_CREATE_TEMPLATE + "v" + SystemConfiguration.getValue("MASS_CREATE_TEMPLATE_VER") + ".xlsm";
          }
        } else {
          EntityManager eManager = JpaManager.getEntityManager();
          if (LegacyDirectUtil.isCountryLegacyDirectEnabled(eManager, cmrIssuingCntry)) {
            docLink = SystemConfiguration.getSystemProperty("massupdateauto." + cmrIssuingCntry);
          } else if (SWISSHandler.isCHIssuingCountry(cmrIssuingCntry)) {
            docLink = SystemConfiguration.getSystemProperty("massupdateauto." + cmrIssuingCntry);
          } else if ("618".equals(cmrIssuingCntry) || "706".equals(cmrIssuingCntry)) {
            docLink = SystemConfiguration.getSystemProperty("massupdateauto." + cmrIssuingCntry);
          } else if (LegacyDirectUtil.isCountryDREnabled(eManager, cmrIssuingCntry) || LAHandler.isLACountry(cmrIssuingCntry)) {
            docLink = SystemConfiguration.getSystemProperty("massupdateauto." + cmrIssuingCntry);
          } else {
            docLink = SystemConfiguration.getSystemProperty("massupdate." + cmrIssuingCntry);
          }

          if (StringUtils.isEmpty(docLink)) {
            docLink = MASS_UPDATE_TEMPLATE;
          }

          templateName = docLink;
        }
        LOG.debug("Mass Template to use for " + cmrIssuingCntry + " = " + templateName);

        if ("TEMPLATE".equals(docType)) {
          response.setContentType("application/octet-stream");
          response.addHeader("Content-Type", "application/octet-steam");
          response.addHeader("Content-Disposition", "attachment; filename=\"" + templateName + "\"");
          GEOHandler geoHandler = RequestUtils.getGEOHandler(cmrIssuingCntry);

          if (!CmrConstants.REQ_TYPE_MASS_CREATE.equals(admin.getReqType()) && !"848".equals(cmrIssuingCntry) && geoHandler != null
              && geoHandler.isNewMassUpdtTemplateSupported(cmrIssuingCntry)) {
            MassChangeTemplateManager.initTemplatesAndValidators(cmrIssuingCntry);

            // change to the ID of the config you are generating
            MassChangeTemplate template = MassChangeTemplateManager.getMassUpdateTemplate(cmrIssuingCntry);
            try (FileOutputStream fos = new FileOutputStream(SystemConfiguration.dirLocation.getAbsolutePath() + "/" + templateName)) {
              EntityManager em = JpaManager.getEntityManager();
              try {
                // modify the country for testing
                template.generate(fos, em, cmrIssuingCntry, 2000);
              } catch (Exception e) {
                e.printStackTrace();
              } finally {
                em.close();
              }
            }
          } else if (SWISSHandler.isAutoMassChangeTemplateEnabled(cmrIssuingCntry)) {
            MassChangeTemplateManager.initTemplatesAndValidatorsSwiss();
            MassChangeTemplate template = MassChangeTemplateManager.getMassUpdateTemplate(SWISSHandler.SWISS_MASSCHANGE_TEMPLATE_ID);
            try (FileOutputStream fos = new FileOutputStream(SystemConfiguration.dirLocation.getAbsolutePath() + "/" + templateName)) {
              EntityManager em = JpaManager.getEntityManager();
              try {
                // modify the country for testing
                template.generate(fos, em, cmrIssuingCntry, 2000);
              } finally {
                em.close();
              }
            }
          }
          SystemConfiguration.download(response.getOutputStream(), docLink);
          request.getSession().setAttribute(token, "Y," + MessageUtil.getMessage(MessageUtil.INFO_MASS_TEMPLATE_DOWNLOADED));

        } else if ("ERROR_LOG".equals(docType)) {
          if (CmrConstants.REQ_TYPE_MASS_CREATE.equals(admin.getReqType()))
            service.processMassCreateErrorLog(response, Long.valueOf(reqId), admin.getIterationId());
          else {
            if (cmrIssuingCntry != null && PageManager.fromGeo("EMEA", cmrIssuingCntry)) {
              service.processMassUpdateErrorLogEMEA(admin, response, templateName);
            }
            if (cmrIssuingCntry != null && (DEHandler.isIERPCountry(cmrIssuingCntry) || CNDHandler.isCNDCountry(cmrIssuingCntry))) {
              service.processMassUpdateErrorLogDECND(admin, response, templateName);
            } else {
              service.processMassUpdateErrorLog(admin, response, templateName);
            }

          }
          request.getSession().setAttribute(token, "Y," + MessageUtil.getMessage(MessageUtil.INFO_ERROR_LOG_DOWNLOADED));
        } else if ("MASSDPL_RESULT_FILE".equalsIgnoreCase(docType)) {
          if ("631".equalsIgnoreCase(cmrIssuingCntry)) {
            String filePath = admin.getFileName();
            String logFile = "MassUpdate_" + reqId + "_Iter" + admin.getIterationId() + "_MassDpl_LOG.xlsx";
            String toReplaceStr = "MassUpdate_" + reqId + "_Iter" + admin.getIterationId() + ".xlsx";
            // latest upload file only
            String dplLogFilePath = StringUtils.replaceOnce(filePath, toReplaceStr, logFile);
            service.processMassDPLFileForDownload(dplLogFilePath, response, Long.parseLong(reqId));
            request.getSession().setAttribute(token, "Y," + MessageUtil.getMessage(MessageUtil.INFO_MASSDPLCHECK_LOGFILE_DOWNLOADED));
          }
        } else {
          if (CmrConstants.REQ_TYPE_MASS_CREATE.equals(admin.getReqType())) {
            docLink = admin.getFileName();
            if (!StringUtils.isBlank(iterId)) {
              LOG.debug("Downloading Iteration " + iterId + " file for Request " + reqId);
              docLink = StringUtils.replaceOnce(docLink, "Iter" + admin.getIterationId(), "Iter" + iterId);
            } else {
              LOG.debug("Downloading Current file for Request " + reqId);
            }
            File file = new File(docLink);
            if (!file.exists()) {
              String name = file.getName();
              File parent = file.getParentFile();
              String reqIdDir = parent.getName();

              // 2020 prod
              file = new File("/gsa/nhbgsa/projects/c/cmma2020/prod/masschange" + File.separator + reqIdDir + File.separator + name);
              LOG.debug(" - Checking historical location: " + file.getAbsolutePath());
              if (!file.exists()) {
                // 2018
                file = new File("/gsa/nhbgsa/projects/c/cmma2018/prod/masschange" + File.separator + reqIdDir + File.separator + name);
                LOG.debug(" - Checking historical location: " + file.getAbsolutePath());
                if (!file.exists()) {
                  LOG.error("Mass file: " + docLink + "does not exist.");
                  throw new CmrException(MessageUtil.ERROR_FILE_DL_ERROR);
                }
              }
            }
            // download the mass file
            FileInputStream fis = new FileInputStream(file);
            try {
              String dlfileName = docLink.substring(docLink.lastIndexOf("/") + 1);
              String type = "";
              try {
                type = MIME_TYPES.getContentType(docLink);
              } catch (Exception e) {
              }
              if (StringUtils.isEmpty(type)) {
                type = "application/octet-stream";
              }
              response.setContentType(type);
              response.addHeader("Content-Type", type);
              response.addHeader("Content-Disposition", "attachment; filename=\"" + dlfileName + "\"");
              IOUtils.copy(fis, response.getOutputStream());
            } finally {
              fis.close();
            }
            request.getSession().setAttribute(token, "Y," + MessageUtil.getMessage(MessageUtil.ERROR_GENERAL));
            request.getSession().setAttribute(token, "Y," + MessageUtil.getMessage(MessageUtil.INFO_MASS_FILE_DOWNLOADED));
          } else {
            docLink = admin.getFileName();
            if (!StringUtils.isBlank(iterId)) {
              LOG.debug("Downloading Iteration " + iterId + " file for Request " + reqId);
              docLink = StringUtils.replaceOnce(docLink, "Iter" + admin.getIterationId(), "Iter" + iterId);
            } else {
              LOG.debug("Downloading Current file for Request " + reqId);
            }
            String fileName = docLink + ".zip";
            File file = new File(fileName);
            if (!file.exists()) {
              String name = file.getName();
              File parent = file.getParentFile();
              String reqIdDir = parent.getName();

              // 2020 prod
              file = new File("/gsa/nhbgsa/projects/c/cmma2020/prod/masschange" + File.separator + reqIdDir + File.separator + name);
              LOG.debug(" - Checking historical location: " + file.getAbsolutePath());
              if (!file.exists()) {
                // 2018
                file = new File("/gsa/nhbgsa/projects/c/cmma2018/prod/masschange" + File.separator + reqIdDir + File.separator + name);
                LOG.debug(" - Checking historical location: " + file.getAbsolutePath());
                if (!file.exists()) {
                  LOG.error("Mass file: " + fileName + "does not exist.");
                  throw new CmrException(MessageUtil.ERROR_FILE_DL_ERROR);
                }
              }
            }
            ZipFile zip = new ZipFile(file);
            try {
              Enumeration<?> entry = zip.entries();
              if (entry.hasMoreElements()) {
                ZipEntry document = (ZipEntry) entry.nextElement();
                InputStream is = zip.getInputStream(document);
                try {
                  String dlfileName = docLink.substring(docLink.lastIndexOf("/") + 1);
                  String type = "";
                  try {
                    type = MIME_TYPES.getContentType(docLink);
                  } catch (Exception e) {
                  }
                  if (StringUtils.isEmpty(type)) {
                    type = "application/octet-stream";
                  }
                  response.setContentType(type);
                  response.addHeader("Content-Type", type);
                  response.addHeader("Content-Disposition", "attachment; filename=\"" + dlfileName + "\"");
                  IOUtils.copy(is, response.getOutputStream());
                } finally {
                  is.close();
                }
              }
            } finally {
              zip.close();
            }
            request.getSession().setAttribute(token, "Y," + MessageUtil.getMessage(MessageUtil.INFO_MASS_FILE_DOWNLOADED));
          }
        }
      }
    } catch (Exception e) {
      if (e instanceof CmrException) {
        CmrException cmre = (CmrException) e;
        if (MessageUtil.getMessage(MessageUtil.INFO_ERROR_LOG_EMPTY).equals(cmre.getMessage())) {
          request.getSession().setAttribute(token, "Y," + cmre.getMessage());
        } else {
          request.getSession().setAttribute(token, "N," + cmre.getMessage());
        }
      } else {
        request.getSession().setAttribute(token, "N," + MessageUtil.getMessage(MessageUtil.ERROR_GENERAL));
      }
    }
  }

  @RequestMapping(
      value = "/massrequest/process",
      method = { RequestMethod.POST, RequestMethod.GET })
  public void processMassFile(HttpServletRequest request, HttpServletResponse response) throws CmrException {
    try {
      boolean isMultipart = ServletFileUpload.isMultipartContent(request);
      if (isMultipart) {
        RequestEntryModel model = new RequestEntryModel();
        // process mass file here
        model.setAction("PROCESS_FILE");
        service.processTransaction(model, request);
      }
    } catch (Exception e) {
      e.printStackTrace();
      LOG.error("Failed processing the mass file...");
    }
  }

  /**
   * Handles the retrival part of the cmr List for a req id
   * 
   * @param request
   * @param response
   * @param model
   * @return
   */

  @RequestMapping(
      value = "/requestentry/reactivate/cmrNolist")
  public ModelMap getCMRList(HttpServletRequest request, HttpServletResponse response, @RequestParam("reqId") long reqId,
      @RequestParam("reqType") String reqType, @RequestParam("cmrIssuingCntry") String cmrIssuingCntry) throws CmrException {

    List<DeleteReactivateModel> drModel = new ArrayList<DeleteReactivateModel>();
    ParamContainer params = new ParamContainer();
    params.addParam("reqId", reqId);
    params.addParam("reqType", reqType);
    params.addParam("cmrIssuingCntry", cmrIssuingCntry);
    try {
      drModel = delReactivateService.process(request, params);
    } catch (Exception e) {
      LOG.error("Error when retrieving records..", e);
    }
    return wrapAsPlainSearchResult(drModel);

    // List<MassUpdateModel> massUpdList = new ArrayList<>();
    //
    // MassUpdateModel massUpdtModel = new MassUpdateModel();
    // massUpdtModel.setParReqId(reqId);
    // massUpdList = massUpdtService.search(massUpdtModel, request);
    // return wrapAsPlainSearchResult(massUpdList);

  }

  @RequestMapping(
      value = "/reactivaterequest/process",
      method = { RequestMethod.POST, RequestMethod.GET })
  public ModelMap maintainCMRList(HttpServletRequest request, HttpServletResponse response, @RequestParam("reqId") long reqId,
      @RequestParam("cmrList") String cmrList, @RequestParam("reqType") String reqType, MassUpdateModel model) throws CmrException {

    ProcessResultModel result = new ProcessResultModel();
    try {
      model.setParReqId(reqId);
      model.setParReqType(reqType);
      model.setCmrList(cmrList != null ? cmrList : "");
      massUpdtService.processTransaction(model, request);

      // String action = model.getAction();

      // set correct information message
      if ("ADD_CMR".equals(model.getAction()) && model.isDisplayMsg()) {
        result.setMessage(MessageUtil.getMessage(MessageUtil.INFO_CMR_ADD_LIST));
        result.setSuccess(true);
        result.setDisplayMsg(true);
        // AppUser.getUser(request).setPreferencesSet(true);
      } else if ("REMOVE_CMR".equals(model.getAction())) {
        result.setMessage(MessageUtil.getMessage(MessageUtil.INFO_CMR_REMOVE_LIST));
        result.setSuccess(true);
        result.setDisplayMsg(true);
      } else if (!model.isDisplayMsg()) {
        result.setMessage("");
        result.setSuccess(false);
        result.setDisplayMsg(false);
      }

    } catch (Exception e) {
      result.setDisplayMsg(true);
      result.setSuccess(false);
      result.setMessage(e.getMessage());
    }

    return wrapAsProcessResult(result);
  }

  @RequestMapping(
      value = "/massrequest/ld_dpl")
  public ModelMap performLDDPLChcek(HttpServletRequest request, HttpServletResponse response, RequestEntryModel model) throws Exception {
    ProcessResultModel result = new ProcessResultModel();
    try {
      service.processTransaction(model, request);
      result.setSuccess(true);
      result.setDisplayMsg(true);
      result.setMessage(MessageUtil.getMessage(MessageUtil.INFO_MASSLDDPLCHECK_SUCCESS));
    } catch (Exception e) {
      result.setSuccess(false);
      result.setMessage(e.getMessage());
    }
    return wrapAsProcessResult(result);
  }

  @RequestMapping(
      value = "/massrequest/dpl")
  public ModelMap performDPLCheck(HttpServletRequest request, HttpServletResponse response, RequestEntryModel model) throws Exception {
    ProcessResultModel result = new ProcessResultModel();
    try {
      service.processTransaction(model, request);
      result.setSuccess(true);
      result.setDisplayMsg(true);
      result.setMessage(MessageUtil.getMessage(MessageUtil.INFO_MASSDPLCHECK_SUCCESS));
    } catch (Exception e) {
      result.setSuccess(false);
      result.setMessage(e.getMessage());
    }
    return wrapAsProcessResult(result);
  }
}
