/**
 * 
 */
package com.ibm.cio.cmr.request.controller.requestentry;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.controller.BaseController;
import com.ibm.cio.cmr.request.model.ParamContainer;
import com.ibm.cio.cmr.request.model.ProcessResultModel;
import com.ibm.cio.cmr.request.model.requestentry.AttachmentModel;
import com.ibm.cio.cmr.request.model.requestentry.AutoDNBDataModel;
import com.ibm.cio.cmr.request.model.requestentry.CheckListModel;
import com.ibm.cio.cmr.request.model.requestentry.CmtModel;
import com.ibm.cio.cmr.request.model.requestentry.FindCMRRecordModel;
import com.ibm.cio.cmr.request.model.requestentry.FindCMRResultModel;
import com.ibm.cio.cmr.request.model.requestentry.ImportCMRModel;
import com.ibm.cio.cmr.request.model.requestentry.RequestEntryModel;
import com.ibm.cio.cmr.request.model.requestentry.ValidationUrlModel;
import com.ibm.cio.cmr.request.service.CmrClientService;
import com.ibm.cio.cmr.request.service.requestentry.AddressService;
import com.ibm.cio.cmr.request.service.requestentry.CommentLogService;
import com.ibm.cio.cmr.request.service.requestentry.ImportCMRService;
import com.ibm.cio.cmr.request.service.requestentry.ImportDnBService;
import com.ibm.cio.cmr.request.service.requestentry.PDFConverterService;
import com.ibm.cio.cmr.request.service.requestentry.RequestEntryService;
import com.ibm.cio.cmr.request.user.AppUser;
import com.ibm.cio.cmr.request.util.MessageUtil;
import com.ibm.cio.cmr.request.util.RequestUtils;
import com.ibm.cio.cmr.request.util.SystemUtil;
import com.ibm.cio.cmr.request.util.dnb.DnBUtil;

/**
 * Controller for handling request entry page functions
 * 
 * @author Sonali Jain
 * 
 */
@Controller
public class RequestEntryController extends BaseController {

  private static Logger LOG = Logger.getLogger(RequestEntryController.class);

  @Autowired
  RequestEntryService service;

  @Autowired
  ImportCMRService cmrService;

  @Autowired
  ImportDnBService dnbService;

  @Autowired
  AddressService addressService;

  @Autowired
  CommentLogService cmtservice;

  @Autowired
  PDFConverterService pdfService;

  /**
   * Handles the display of Update page of Request Entry
   * 
   * @param reqId
   * @param request
   * @param response
   * @param model
   * @return
   * @throws CmrException
   */
  @RequestMapping(
      value = "/request/{reqId1}")
  public ModelAndView showRequestDetail(@PathVariable("reqId1") long reqId, HttpServletRequest request, HttpServletResponse response,
      RequestEntryModel model, CheckListModel checklist) throws Exception {
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
      LOG.debug(" >>> action is NOT empty! >>> ");
      mv = processYourAction(request, response, model, checklist);
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
            // get the value of addr Std here

            if (dplStatusInvalid(AppUser.getUser(request), reqModel)) {
              AddressService.clearDplResults(reqModel.getReqId());
              mv = new ModelAndView("redirect:/request/" + reqModel.getReqId(), "reqentry", reqModel);
              MessageUtil.setErrorMessage(mv, MessageUtil.ERROR_DPL_RESET);
            } else {
              String result = service.getAddrStdResult(model.getReqId());
              if (null != result) {
                reqModel.setAddrStdResult(result);
                if (result.equalsIgnoreCase(CmrConstants.Scorecard_Not_Done)) {
                  reqModel.setAddrStdTs(null);
                }
                if (result.equalsIgnoreCase(CmrConstants.Scorecard_COMPLETED_WITH_ISSUES)
                    || result.equalsIgnoreCase(CmrConstants.Scorecard_COMPLETED)) {
                  reqModel.setAddrStdTs(SystemUtil.getCurrentTimestamp());
                }
              }
              reqModel.setFromUrl(fromUrl);
              // this is an existing request, set date values in specified
              // format
              setExistingReqParamValues(reqModel, request);
              setYourActionsAttributes(request, reqModel, false);

              // If mass request, redirect to Mass Request page...
              if (!StringUtils.isEmpty(reqModel.getReqType()) && CmrConstants.MASS_CHANGE_REQUESTS_TYPES.contains(reqModel.getReqType().trim())) {
                mv = new ModelAndView("redirect:/massrequest/" + reqId, "reqentry", reqModel);
                Thread.currentThread().setName("Executor-" + Thread.currentThread().getId());
                return mv;
              }
              List<ValidationUrlModel> validations = service.getValidationUrls(reqModel.getCmrIssuingCntry());
              reqModel.setValidations(validations);

              // set approval result
              service.setApprovalResult(reqModel);

              // internal division fix
              reqModel.setDept_int(reqModel.getDept());
              model = reqModel;
              mv = new ModelAndView("requestentry", "reqentry", reqModel);
            }
          }
        }
      }
    }
    if (mv == null) {
      mv = new ModelAndView("requestentry", "reqentry", model);
    }

    model.setAction(null);

    // add for Defect 1887938- UKI - INAC removal - update requests by charles
    // 20191018 start
    // -----------------------TODO
    mv.getModel().get("reqentry");
    // add for Defect 1887938- UKI - INAC removal - update requests by charles
    // 20191018 end
    setPageKeys("REQUEST", "REQUEST", mv);
    addExtraModelEntries(mv, model);
    Thread.currentThread().setName("Executor-" + Thread.currentThread().getId());

    return mv;
  }

  private boolean dplStatusInvalid(AppUser user, RequestEntryModel model) {
    // 1150478: for completed records, do not reset DPL, JZ Mar 2, 2017
    if ("COM".equals(model.getReqStatus())) {
      LOG.debug("Completed record, DPL check will not be reset.");
      return false;
    }
    if ("NR".equals(model.getDplChkResult())) {
      LOG.debug("DPL Check is not required for this record, will not reset status.");
      return false;
    }
    if (user != null && !user.getIntranetId().toLowerCase().equals(model.getLockBy() != null ? model.getLockBy().toLowerCase() : "")) {
      LOG.debug("Request is not locked by the user, status will not be reset.");
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

  /**
   * Handles display of new request entry page for creating new request.
   * 
   * @param request
   * @param response
   * @param model
   * @return
   */
  @RequestMapping(
      value = "/request")
  public ModelAndView showRequestEntryPage(HttpServletRequest request, HttpServletResponse response, RequestEntryModel model,
      CheckListModel checklist) throws Exception {

    ModelAndView mv = null;

    String action = model.getAction();

    if (StringUtils.isBlank(action) && !"Y".equals(request.getParameter("_f"))) {
      String country = model.getNewReqCntry();
      if (StringUtils.isBlank(country)) {
        country = model.getCmrIssuingCntry();
      }
      String reqType = model.getNewReqType();
      if (StringUtils.isBlank(reqType)) {
        reqType = model.getReqType();
      }
      if (!StringUtils.isBlank(country) && RequestUtils.isRequesterAutomationEnabled(country)) {
        LOG.debug("Country " + country + " has requester automation enabled. Redirecting to CreateCMR2.0 requester page..");
        return new ModelAndView("redirect:/autoreq?cmrIssuingCntry=" + country + "&reqType=" + reqType, "reqentry", model);
      }
      if (RequestUtils.isQuickSearchFirstEnabled(country) && ("C".equals(reqType) || "U".equals(reqType))) {
        LOG.debug("Country " + country + " has enabled quick search as first interface. Redirecting to Quick Search page..");
        return new ModelAndView("redirect:/quick_search?issuingCntry=" + country
            + "&infoMessage=Quick Search should be done for CMR being requested for or updated before creating a request.", "reqentry", model);
      }
    }

    request.getSession().removeAttribute("_fromUrl");

    String redirectUrl = model.getRedirectUrl();
    if (!StringUtils.isEmpty(action)) {
      // save
      mv = processYourAction(request, response, model, checklist);
      if (!StringUtils.isEmpty(redirectUrl)) {
        mv.setViewName("redirect:" + redirectUrl);
      }
    } else {
      // this is a new request, set default values
      setDefaultValues(model, request);
      mv = new ModelAndView("requestentry", "reqentry", model);
    }
    setPageKeys("REQUEST", "REQUEST", mv);
    addExtraModelEntries(mv, model);

    setYourActionsAttributes(request, model, true);

    return mv;
  }

  /**
   * Sets the default values
   */
  public void setDefaultValues(RequestEntryModel model, HttpServletRequest request) throws Exception {

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
    } else if (StringUtils.isEmpty(model.getCmrIssuingCntry())) {
      model.setCmrIssuingCntry(user.getCmrIssuingCntry());
    } else if (StringUtils.isEmpty(model.getNewReqCntry())) {
      model.setCountryUse(model.getCmrIssuingCntry());
    }
    // avoid setting default pref lang as E for NORDX -> CREATCMR - 8388
    if (StringUtils.isNotEmpty(model.getCmrIssuingCntry()) && !Arrays.asList("678", "702", "846", "806").contains(model.getCmrIssuingCntry())) {
      model.setCustPrefLang(CmrConstants.LANGUAGE_ENGLISH);
    }

    if (!StringUtils.isEmpty(model.getNewReqType())) {
      model.setReqType(model.getNewReqType());
    }

    // new entries are with Draft status
    model.setReqStatus(CmrConstants.REQUEST_STATUS.DRA.toString());
    model.setOverallStatus(CmrConstants.ReqStatus_Draft); // TODO retrieve
                                                          // description from DB
    model.setUserRole(CmrConstants.Role_Requester);
    model.setFindCmrResult(CmrConstants.Scorecard_Not_Done);
    model.setDplChkResult(CmrConstants.Scorecard_Not_Done);
    model.setFindDnbResult(CmrConstants.Scorecard_Not_Done);
    model.setAddrStdResult(CmrConstants.Scorecard_Not_Done);
    model.setApprovalResult(CmrConstants.APPROVAL_RESULT_NONE);
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
    // scorecard date fields
    if (reqModel.getFindCmrTs() != null) {
      reqModel.setFindCmrDate(dateFormat.format(reqModel.getFindCmrTs()));
    }
    if (reqModel.getDplChkTs() != null) {
      reqModel.setDplChkDate(dateFormat.format(reqModel.getDplChkTs()));
    }
    if (reqModel.getFindDnbTs() != null) {
      reqModel.setFindDnbDate(dateFormat.format(reqModel.getFindDnbTs()));
    }
    if (reqModel.getAddrStdTs() != null) {
      reqModel.setAddrStdDate(dateFormat.format(reqModel.getAddrStdTs()));
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
          if (CmrConstants.REQUEST_STATUS.DRA.toString().equals(requestStatus)) {
            reqModel.setUserRole(CmrConstants.Role_Requester);
          } else if (CmrConstants.REQUEST_STATUS.PCO.toString().equals(requestStatus)
              || CmrConstants.REQUEST_STATUS.PVA.toString().equals(requestStatus) || CmrConstants.REQUEST_STATUS.PCR.toString().equals(requestStatus)
              || CmrConstants.REQUEST_STATUS.ACU.toString().equals(requestStatus)) {
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

  public ModelAndView processYourAction(HttpServletRequest request, HttpServletResponse response, RequestEntryModel model, CheckListModel checklist) {
    ModelAndView mv = null;
    try {
      service.setChecklist(checklist);
      service.processTransaction(model, request);

      mv = new ModelAndView("redirect:/request/" + model.getReqId(), "reqentry", new RequestEntryModel());

      String action = model.getAction();
      if (CmrConstants.Save().equalsIgnoreCase(action)) {
        mv = new ModelAndView("redirect:/request/" + model.getReqId(), "reqentry", new RequestEntryModel());
        if ("Y".equals(model.getHasError())) {
          MessageUtil.setInfoMessage(mv, MessageUtil.INFO_REQUEST_SAVED_WITH_ERROR, model.getReqId() + "");
        } else {
          MessageUtil.setInfoMessage(mv, MessageUtil.INFO_REQUEST_SAVED, model.getReqId() + "");
        }

      } else if ("CREATE_NEW".equalsIgnoreCase(action)) {
        mv = new ModelAndView("redirect:/request/" + model.getReqId(), "reqentry", new RequestEntryModel());
        MessageUtil.setInfoMessage(mv, MessageUtil.INFO_REQUEST_CREATED, model.getReqId() + "");
      } else if ("REJECT_SEARCH".equalsIgnoreCase(action) || "NO_DATA_SEARCH".equalsIgnoreCase(action)) {
        mv = new ModelAndView("redirect:/request/" + model.getReqId(), "reqentry", new RequestEntryModel());
        MessageUtil.setInfoMessage(mv, MessageUtil.INFO_REQUEST_SAVED_REJECT, model.getReqId() + "");
      } else if (CmrConstants.Claim().equalsIgnoreCase(action)) {
        mv = new ModelAndView("redirect:/request/" + model.getReqId(), "reqentry", new RequestEntryModel());
        MessageUtil.setInfoMessage(mv, MessageUtil.INFO_REQUEST_PROCESSED, "claimed", model.getReqId() + "");

      } else if (CmrConstants.Unlock().equalsIgnoreCase(action)) {
        mv = new ModelAndView("redirect:/workflow/open", "reqentry", new RequestEntryModel());
        MessageUtil.setInfoMessage(mv, MessageUtil.INFO_REQUEST_PROCESSED, "unlocked", model.getReqId() + "");

      } else if (CmrConstants.Send_for_Processing().equalsIgnoreCase(action)) {
        if ((!StringUtils.isBlank(model.getMessageDefaultApproval()))) {
          mv = new ModelAndView("redirect:/request/" + model.getReqId(), "reqentry", new RequestEntryModel());
          if (model.getApprovalResult().equalsIgnoreCase("Pending") || model.getApprovalResult().equalsIgnoreCase("None")) {
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
        mv = new ModelAndView("redirect:/request/" + model.getReqId(), "reqentry", new RequestEntryModel());
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
        MessageUtil.setInfoMessage(mv, MessageUtil.INFO_REQUEST_PROCESSED, "updated to processing create/update pending", model.getReqId() + "");
      } else if (CmrConstants.Create_Update_Approved().equalsIgnoreCase(action)) {
        mv = new ModelAndView("redirect:/request/" + model.getReqId(), "reqentry", new RequestEntryModel());
        MessageUtil.setInfoMessage(mv, MessageUtil.INFO_REQUEST_PROCESSED, "updated to processing create/update pending", model.getReqId() + "");
      } else if (CmrConstants.Reprocess_Checks().equalsIgnoreCase(action)) {
        mv = new ModelAndView("redirect:/workflow/open", "reqentry", new RequestEntryModel());
        MessageUtil.setInfoMessage(mv, MessageUtil.INFO_REQUEST_PROCESSED, "requeued for automated checks", model.getReqId() + "");
      } else if (CmrConstants.VERIFY_COMPANY.equalsIgnoreCase(action)) {
        MessageUtil.setInfoMessage(mv, MessageUtil.VERIFY_COMPANY_SUCCESS);
      } else if (CmrConstants.VERIFY_SCENARIO.equalsIgnoreCase(action)) {
        MessageUtil.setInfoMessage(mv, MessageUtil.VERIFY_SCENARIO_SUCCESS);
      } else if (CmrConstants.OVERRIDE_DNB.equalsIgnoreCase(action)) {
        MessageUtil.setInfoMessage(mv, MessageUtil.OVERRIDE_DNB_SUCCESS);
      }

      if (!StringUtils.isEmpty(model.getDplMessage())) {
        MessageUtil.setInfoMessage(mv, model.getDplMessage());
      }
      SystemUtil.storeToSession(request, null);
    } catch (Exception e) {
      SystemUtil.storeToSession(request, e);
      mv = new ModelAndView("requestentry", "reqentry", model);
      setPageKeys("REQUEST", "REQUEST", mv);
      if (e instanceof CmrException) {
        MessageUtil.setErrorMessage(mv, ((CmrException) e).getCode());
      } else {
        MessageUtil.setErrorMessage(mv, MessageUtil.ERROR_CANNOT_SAVE_REQUEST);
      }
    }
    return mv;
  }

  /**
   * Processes importing the CMR into the Request
   * 
   * @param reqId
   * @param request
   * @param response
   * @param model
   * @return
   * @throws Exception
   */
  @RequestMapping(
      value = "/request/import")
  public ModelAndView importCMRs(HttpServletRequest request, HttpServletResponse response, ImportCMRModel model, RequestEntryModel reqModel)
      throws Exception {

    ModelAndView mv = null;
    if (model.getCmrIssuingCntry().equals("760")) {
      return importCRISRecords(request, response, model, reqModel);
    }
    String cmrNo = model.getCmrNum();
    long reqId = model.getReqId();
    String cmrCntry = model.getCmrIssuingCntry();
    String searchCntry = model.getSearchIssuingCntry();
    String newParams = "?cmrIssuingCntry=" + cmrCntry + "&reqType=" + reqModel.getReqType();
    try {
      FindCMRResultModel results = null;
      if (!model.isPoolRecord()) {
        results = SystemUtil.findCMRs(cmrNo, cmrCntry, 2000, searchCntry);
      } else {
        results = SystemUtil.findCMRs(cmrNo, cmrCntry, 2000, searchCntry, true); // searching
                                                                                 // for
                                                                                 // pool
                                                                                 // records
      }

      boolean noResults = false;
      if (results == null || results.getItems() == null || results.getItems().size() == 0) {
        LOG.debug("NO RESULTS");
        results = new FindCMRResultModel();
        results.setItems(new ArrayList<FindCMRRecordModel>());
        noResults = true;
      }
      ParamContainer params = new ParamContainer();
      params.addParam("reqId", reqId);
      params.addParam("results", results);
      params.addParam("system", model.getSystem());
      params.addParam("model", reqModel);
      params.addParam("searchModel", model);
      params.addParam("skipAddress", model.isSkipAddress());
      if (cmrService == null) {
        cmrService = new ImportCMRService();
      }
      ImportCMRModel retModel = cmrService.process(request, params);
      long reqIdNew = retModel.getReqId();
      mv = new ModelAndView("redirect:/request" + (reqIdNew > 0 ? "/" + reqIdNew : newParams), "cmr", new ImportCMRModel());
      mv.addObject("internalReqId", reqIdNew);
      mv.addObject("success", true);
      if (!noResults) {
        MessageUtil.setInfoMessage(mv, MessageUtil.INFO_CMR_IMPORTED, cmrNo);
      } else {
        MessageUtil.setErrorMessage(mv, MessageUtil.INFO_NO_CMR_IMPORTED, cmrNo);
      }
      SystemUtil.storeToSession(request, null);
    } catch (Exception e) {
      SystemUtil.storeToSession(request, e);
      LOG.error("Error in Processing CMRs", e);
      mv = new ModelAndView("redirect:/request" + (reqId > 0 ? "/" + reqId : newParams), "cmr", new ImportCMRModel());
      if (e instanceof CmrException) {
        setError(e, mv);
      } else {
        setError(new CmrException(MessageUtil.ERROR_GENERAL), mv);
      }
      mv.addObject("success", false);
    }
    return mv;
  }

  /**
   * Processes importing the D&B Record into the Request
   * 
   * @param reqId
   * @param request
   * @param response
   * @param model
   * @return
   * @throws Exception
   */
  @RequestMapping(
      value = "/request/dnbimport")
  public ModelAndView importDnB(HttpServletRequest request, HttpServletResponse response, ImportCMRModel model, RequestEntryModel reqModel)
      throws Exception {

    ModelAndView mv = null;

    long reqId = model.getReqId();

    String newParams = "?cmrIssuingCntry=" + model.getCmrIssuingCntry() + "&reqType=" + reqModel.getReqType();
    try {
      ObjectMapper mapper = new ObjectMapper();
      FindCMRResultModel results = mapper.readValue(model.getProductString(), FindCMRResultModel.class);

      boolean noResults = false;
      if (results == null || results.getItems() == null || results.getItems().size() == 0) {
        results = new FindCMRResultModel();
        results.setItems(new ArrayList<FindCMRRecordModel>());
        noResults = true;
      }

      FindCMRRecordModel record = results.getItems().get(0);
      String dunsNo = record.getCmrDuns();
      String dnBname = "";
      if (record != null && StringUtils.isNotBlank(record.getCmrName1Plain())) {
        dnBname = record.getCmrName1Plain() + (StringUtils.isNotBlank(record.getCmrName2Plain()) ? " " + record.getCmrName2Plain() : "");
      }
      // do a fresh check on details for proper formatting
      record = DnBUtil.extractRecordFromDnB(reqModel.getCmrIssuingCntry(), dunsNo, record.getCmrPostalCode());
      if (record != null) {
        results.setItems(new ArrayList<FindCMRRecordModel>());
        results.getItems().add(record);
      }
      ParamContainer params = new ParamContainer();
      params.addParam("reqId", reqId);
      params.addParam("results", results);
      params.addParam("system", model.getSystem());
      params.addParam("model", reqModel);
      params.addParam("dnBname", dnBname);
      ImportCMRModel retModel = dnbService.process(request, params);
      long reqIdNew = retModel.getReqId();
      mv = new ModelAndView("redirect:/request" + (reqIdNew > 0 ? "/" + reqIdNew : newParams), "cmr", new ImportCMRModel());
      if (!noResults) {
        MessageUtil.setInfoMessage(mv, MessageUtil.INFO_DNB_IMPORTED);
      } else {
        MessageUtil.setErrorMessage(mv, MessageUtil.INFO_NO_CMR_IMPORTED);
      }
      mv.addObject("internalReqId", reqIdNew);
      mv.addObject("success", true);
      SystemUtil.storeToSession(request, null);
    } catch (Exception e) {
      SystemUtil.storeToSession(request, e);
      LOG.error("Error in Processing CMRs", e);
      mv = new ModelAndView("redirect:/request" + (reqId > 0 ? "/" + reqId : newParams), "cmr", new ImportCMRModel());
      setError(new CmrException(e), mv);
      mv.addObject("success", false);
    }
    return mv;
  }

  /**
   * Gets the addr std result dynamically
   * 
   * @param request
   * @param response
   * @return
   * @throws IOException
   */
  @RequestMapping(
      value = "/request/scorecard")
  public ModelMap getScorecardResult(HttpServletRequest request, HttpServletResponse response) throws IOException {

    ModelMap model = new ModelMap();
    long reqId = (Long) request.getSession().getAttribute("lastReqId");
    String result = service.getAddrStdResult(reqId);
    model.addAttribute("result", result);
    return model;
  }

  /**
   * Gets the comment log for given request
   * 
   * @param request
   * @param response
   * @return
   * @throws IOException
   */
  @RequestMapping(
      value = "/request/commentlog/list")
  public ModelMap getCommentLogList(HttpServletRequest request, HttpServletResponse response, CmtModel cmtModel) throws CmrException {
    List<CmtModel> results = cmtservice.search(cmtModel, request);
    ModelMap map = new ModelMap();
    map.addAttribute("items", results);
    return map;
  }

  @RequestMapping(
      value = "/request/pdf",
      method = RequestMethod.POST)
  public void exportToPDF(HttpServletRequest request, HttpServletResponse response, AttachmentModel model) throws Exception {

    String token = request.getParameter("tokenId");
    String id = request.getParameter("reqId");
    try {
      long reqId = Long.parseLong(id);
      ParamContainer params = new ParamContainer();
      params.addParam("reqId", reqId);
      params.addParam("response", response);
      response.setContentType("application/pdf");
      response.addHeader("Content-Type", "application/pdf");
      response.addHeader("Content-Disposition", "attachment; filename=\"RequestDetails_" + reqId + ".pdf\"");
      this.pdfService.process(request, params);
      request.getSession().setAttribute(token, "Y," + MessageUtil.getMessage(MessageUtil.INFO_PDF_GENERATED));
    } catch (Exception e) {
      if (e instanceof CmrException) {
        CmrException cmre = (CmrException) e;
        request.getSession().setAttribute(token, "N," + cmre.getMessage());
      } else {
        request.getSession().setAttribute(token, "N," + MessageUtil.getMessage(MessageUtil.ERROR_GENERAL));
      }
    }
  }

  /*
   * Code below specifically to handle Japan imports
   */
  /**
   * Processes importing the CMR into the Request
   * 
   * @param reqId
   * @param request
   * @param response
   * @param model
   * @return
   * @throws Exception
   */
  @RequestMapping(
      value = "/request/crisimport")
  public ModelAndView importCRISRecords(HttpServletRequest request, HttpServletResponse response, ImportCMRModel model, RequestEntryModel reqModel)
      throws Exception {

    ModelAndView mv = null;

    String cmrNo = model.getCmrNum();
    long reqId = model.getReqId();
    String cmrCntry = model.getCmrIssuingCntry();
    String searchCntry = model.getSearchIssuingCntry();
    try {

      FindCMRResultModel results = null;
      if (!model.isAddressOnly()) {
        results = SystemUtil.findCMRs(cmrNo, cmrCntry, 2000, searchCntry);
      }
      boolean noResults = false;
      if (results == null || results.getItems() == null || results.getItems().size() == 0) {
        LOG.debug("NO RESULTS");
        results = new FindCMRResultModel();
        results.setItems(new ArrayList<FindCMRRecordModel>());
        noResults = true;
      }
      if (model.isAddressOnly()) {
        noResults = false;
      }
      ParamContainer params = new ParamContainer();
      params.addParam("reqId", reqId);
      params.addParam("results", results);
      params.addParam("system", model.getSystem());
      params.addParam("model", reqModel);
      params.addParam("searchModel", model);
      params.addParam("skipAddress", model.isSkipAddress());
      if (cmrService == null) {
        cmrService = new ImportCMRService();
      }
      ImportCMRModel retModel = this.cmrService.process(request, params);
      long reqIdNew = retModel.getReqId();
      mv = new ModelAndView("redirect:/request" + (reqIdNew > 0 ? "/" + reqIdNew : ""), "cmr", new ImportCMRModel());
      if (!noResults) {
        MessageUtil.setInfoMessage(mv, MessageUtil.INFO_GEN_IMPORT_SUCCESS, cmrNo);
      } else {
        MessageUtil.setErrorMessage(mv, MessageUtil.INFO_NO_CMR_ONLY_CRIS_IMPORTED, cmrNo);
      }
      mv.addObject("internalReqId", reqIdNew);
      mv.addObject("success", true);
    } catch (Exception e) {
      LOG.error("Error in Processing CRIS Import", e);
      mv = new ModelAndView("redirect:/request" + (reqId > 0 ? "/" + reqId : ""), "cmr", new ImportCMRModel());
      if (e instanceof CmrException) {
        setError(e, mv);
      } else {
        setError(new CmrException(MessageUtil.ERROR_GENERAL), mv);
      }
      mv.addObject("success", false);
    }
    return mv;
  }

  @RequestMapping(
      value = "/request/verify/company")
  public ModelMap processAddressModal(HttpServletRequest request, HttpServletResponse response, RequestEntryModel model) throws CmrException {

    ProcessResultModel result = new ProcessResultModel();
    model.setReqType("U");
    try {
      result.setMessage("company verfied.");
      result.setSuccess(true);
    } catch (Exception e) {
      result.setSuccess(false);
      result.setMessage(e.getMessage());
    }
    return wrapAsProcessResult(result);
  }

  /**
   * Gets the highest dnb matches for the request
   * 
   * @param request
   * @param response
   * @return
   * @throws IOException
   */
  @RequestMapping(
      value = "/request/dnb/matchlist")
  public ModelMap getDnBMatchList(HttpServletRequest request, HttpServletResponse response, AutoDNBDataModel model) throws Exception {
    String reqIdString = request.getParameter("reqId");
    long reqId = reqIdString != null ? Long.parseLong(reqIdString) : 0L;
    List<AutoDNBDataModel> results = service.getDnBMatchList(model, reqId);
    return wrapAsSearchResult(results);
  }

  @RequestMapping(
      value = "/request/dnb/checkMatch")
  public ModelMap checkIfMatchesDnB(HttpServletRequest request, HttpServletResponse response, AutoDNBDataModel model) throws Exception {
    ModelMap map = new ModelMap();
    try {
      String reqIdString = request.getParameter("reqId");
      long reqId = reqIdString != null ? Long.parseLong(reqIdString) : 0L;
      map = service.isDnBMatch(model, reqId, "ZS01");
    } catch (Exception e) {
      LOG.error("Error occured in D&B matching", e);
      map.put("success", false);
    }
    return map;
  }

  @RequestMapping(
      value = "/request/dnb/checkMatchUpdate")
  public ModelMap checkIfMatchesDnBUpdate(HttpServletRequest request, HttpServletResponse response, AutoDNBDataModel model) throws Exception {
    ModelMap map = new ModelMap();
    try {
      String reqIdString = request.getParameter("reqId");
      long reqId = reqIdString != null ? Long.parseLong(reqIdString) : 0L;
      map = service.isDnBMatchAddrsUpdateAU(model, reqId);
    } catch (Exception e) {
      LOG.error("Error occured in D&B matching", e);
      map.put("success", false);
    }
    return map;
  }

  @RequestMapping(value = "/request/dnb/custNmUpdate")
  public ModelMap checkIfCustNMMatchesDnBUpdate(HttpServletRequest request, HttpServletResponse response, AutoDNBDataModel model) throws Exception {
    ModelMap map = new ModelMap();
    try {
      String reqIdString = request.getParameter("reqId");
      String custNm = request.getParameter("custNm");
      String formerCustNm = request.getParameter("formerCustNm");
      long reqId = reqIdString != null ? Long.parseLong(reqIdString) : 0L;
      map = service.isCustNmMatch(model, reqId, custNm, formerCustNm);
    } catch (Exception e) {
      LOG.error("Error occured in D&B matching", e);
      map.put("success", false);
    }
    return map;
  }

  @RequestMapping(
      value = "/request/dnb/matchlist/importrecord")
  public ModelMap importMatchIntoRequest(HttpServletRequest request, HttpServletResponse response, AutoDNBDataModel model) throws Exception {
    CmrClientService dnbService = new CmrClientService();
    ModelMap map = new ModelMap();
    try {
      dnbService.getAutoDnBDetails(map, model.getAutoDnbDunsNo());
    } catch (Exception e) {
      LOG.error("Error in getting D&B data.", e);
      map.put("success", false);
    }
    return map;
  }

  /*
   * Code below specifically to Single Reactivation imports
   */
  /**
   * Processes importing the CMR into the Request
   * 
   * @param reqId
   * @param request
   * @param response
   * @param model
   * @return
   * @throws Exception
   */
  @RequestMapping(
      value = "/request/singlereactimport")
  public ModelAndView importSREACTRecords(HttpServletRequest request, HttpServletResponse response, ImportCMRModel model, RequestEntryModel reqModel)
      throws Exception {

    ModelAndView mv = null;

    String cmrNo = model.getCmrNum();
    long reqId = model.getReqId();
    String cmrCntry = model.getCmrIssuingCntry();
    String searchCntry = model.getSearchIssuingCntry();
    String newParams = "?cmrIssuingCntry=" + cmrCntry + "&reqType=" + reqModel.getReqType();
    try {
      FindCMRResultModel results = service.findSingleReactCMRs(cmrNo, cmrCntry, 2000, searchCntry);

      boolean noResults = false;
      if (results == null || results.getItems() == null || results.getItems().size() == 0) {
        LOG.debug("NO RESULTS");
        results = new FindCMRResultModel();
        results.setItems(new ArrayList<FindCMRRecordModel>());
        noResults = true;
      }
      ParamContainer params = new ParamContainer();
      params.addParam("reqId", reqId);
      params.addParam("results", results);
      params.addParam("system", model.getSystem());
      params.addParam("model", reqModel);
      params.addParam("searchModel", model);
      params.addParam("skipAddress", model.isSkipAddress());
      if (cmrService == null) {
        cmrService = new ImportCMRService();
      }
      ImportCMRModel retModel = cmrService.process(request, params);
      long reqIdNew = retModel.getReqId();
      mv = new ModelAndView("redirect:/request" + (reqIdNew > 0 ? "/" + reqIdNew : newParams), "cmr", new ImportCMRModel());
      mv.addObject("internalReqId", reqIdNew);
      mv.addObject("success", true);
      if (!noResults) {
        MessageUtil.setInfoMessage(mv, MessageUtil.INFO_CMR_IMPORTED, cmrNo);
      } else {
        MessageUtil.setErrorMessage(mv, MessageUtil.INFO_NO_CMR_IMPORTED, cmrNo);
      }
      SystemUtil.storeToSession(request, null);
    } catch (Exception e) {
      SystemUtil.storeToSession(request, e);
      LOG.error("Error in Processing CMRs", e);
      mv = new ModelAndView("redirect:/request" + (reqId > 0 ? "/" + reqId : newParams), "cmr", new ImportCMRModel());
      if (e instanceof CmrException) {
        setError(e, mv);
      } else {
        setError(new CmrException(MessageUtil.ERROR_GENERAL), mv);
      }
      mv.addObject("success", false);
    }
    return mv;
  }

  /**
   * Processing DNB and NZAPI check for NZ update request
   * 
   * @param request
   * @param response
   * @param model
   * @return
   * @throws Exception
   */
  @RequestMapping(value = "/request/dnb/checkDNBAPIMatchUpdateForNZ")
  public ModelMap checkIfCustNmMatchesUpdateForNZ(HttpServletRequest request, HttpServletResponse response, AutoDNBDataModel model) throws Exception {
    ModelMap map = new ModelMap();
    try {
      String reqIdString = request.getParameter("reqId");
      long reqId = reqIdString != null ? Long.parseLong(reqIdString) : 0L;
      String regex = "\\s+$";
      String businessNumber = request.getParameter("businessNumber").replaceAll(regex, "");
      map = service.isDnBAPIMatchAddrsUpdateNZ(model, reqId, businessNumber);
    } catch (Exception e) {
      LOG.error("Error occured in D&B matching", e);
      map.put("success", false);
    }
    return map;
  }
}