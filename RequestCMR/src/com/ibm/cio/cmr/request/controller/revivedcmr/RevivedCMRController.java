package com.ibm.cio.cmr.request.controller.revivedcmr;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.controller.BaseController;
import com.ibm.cio.cmr.request.controller.automation.DuplicateCheckController;
import com.ibm.cio.cmr.request.model.ParamContainer;
import com.ibm.cio.cmr.request.model.requestentry.RequestEntryModel;
import com.ibm.cio.cmr.request.model.revivedcmr.RevivedCMRModel;
import com.ibm.cio.cmr.request.model.system.ForcedStatusChangeModel;
import com.ibm.cio.cmr.request.model.system.UserModel;
import com.ibm.cio.cmr.request.service.revivedcmr.RevivedCMRService;
import com.ibm.cio.cmr.request.user.AppUser;
import com.ibm.cio.cmr.request.util.MessageUtil;

/**
 * Controller for revived cmrs process
 * 
 * @author clint
 * 
 */
@Controller
public class RevivedCMRController extends BaseController {

  private static final Logger LOG = Logger.getLogger(DuplicateCheckController.class);

  @Autowired
  private RevivedCMRService service;

  @RequestMapping(
      value = "/revivedcmrs")
  public ModelAndView showForcedStatusChangePage(HttpServletRequest request, HttpServletResponse response, ForcedStatusChangeModel model)
      throws CmrException {
    ModelAndView mv = new ModelAndView("revivedcmrs", "revived", model);

    AppUser user = AppUser.getUser(request);
    if (!user.isAdmin() && !user.isCmde()) {
      LOG.warn("User " + user.getIntranetId() + " (" + user.getBluePagesName() + ") tried accessing the Forced Status Change system function.");
      mv = new ModelAndView("noaccess", "users", new UserModel());
      return mv;
    }

    // if (!StringUtils.isEmpty(model.getSearchReqId())) {
    // model.setReqId(Long.parseLong(model.getSearchReqId()));
    // List<ForcedStatusChangeModel> records = service.search(model, request);
    // if (records != null && records.size() > 0) {
    // mv = new ModelAndView("statuschange", "status", records.get(0));
    // } else {
    // mv = new ModelAndView("redirect:/statuschange", "status", new
    // ForcedStatusChangeModel());
    // MessageUtil.setErrorMessage(mv, MessageUtil.ERROR_INVALID_REQ_ID,
    // model.getSearchReqId());
    // }
    // }
    setPageKeys("ADMIN", "REVIVED_CMRS_ADMIN", mv);
    return mv;
  }

  @SuppressWarnings("unchecked")
  @RequestMapping(
      value = "/revivedcmrs/process",
      method = RequestMethod.POST)
  public void process(HttpServletRequest request, HttpServletResponse response, RevivedCMRModel model) throws CmrException {
    ParamContainer params = new ParamContainer();
    try {
      boolean isMultipart = ServletFileUpload.isMultipartContent(request);
      if (isMultipart) {
        List<RevivedCMRModel> revivedCMRModel = this.service.process(request, params);
        service.exportToExcel(revivedCMRModel, response);
        request.getSession().setAttribute((String) params.getParam("token"), "Y," + MessageUtil.getMessage(MessageUtil.INFO_REV_CMR_FILE_PROCESSED));
      }
    } catch (Exception e) {
      e.printStackTrace();
      LOG.error("Failed processing the revived cmrs file...");
      request.getSession().setAttribute((String) params.getParam("token"), "N," + MessageUtil.getMessage(MessageUtil.ERROR_GENERAL));
    }
  }

  @RequestMapping(
      value = "/revivedcmrs/processfile",
      method = { RequestMethod.POST, RequestMethod.GET })
  public void processMassFile(HttpServletRequest request, HttpServletResponse response, RequestEntryModel model) throws CmrException {
    try {
      boolean isMultipart = ServletFileUpload.isMultipartContent(request);
      if (isMultipart) {
        // process mass file here
        model.setAction("PROCESS_FILE");
        // service.processTransaction(model, request);
      }
    } catch (Exception e) {
      e.printStackTrace();
      LOG.error("Failed processing the mass file...");
    }
  }
}
