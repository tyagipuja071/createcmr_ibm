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
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.controller.BaseController;
import com.ibm.cio.cmr.request.controller.automation.DuplicateCheckController;
import com.ibm.cio.cmr.request.model.ParamContainer;
import com.ibm.cio.cmr.request.model.revivedcmr.RevivedCMRModel;
import com.ibm.cio.cmr.request.model.system.ForcedStatusChangeModel;
import com.ibm.cio.cmr.request.model.system.UserModel;
import com.ibm.cio.cmr.request.service.revivedcmr.RevivedCMRService;
import com.ibm.cio.cmr.request.user.AppUser;
import com.ibm.cio.cmr.request.util.mail.Email;
import com.ibm.cio.cmr.request.util.mail.MessageType;

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
  public ModelAndView showRevivedCMRsPage(HttpServletRequest request, HttpServletResponse response, ForcedStatusChangeModel model)
      throws CmrException {
    ModelAndView mv = new ModelAndView("revivedcmrs", "revived", model);

    AppUser user = AppUser.getUser(request);
    if (!user.isAdmin() && !user.isCmde()) {
      LOG.warn("User " + user.getIntranetId() + " (" + user.getBluePagesName() + ") tried accessing the Forced Status Change system function.");
      mv = new ModelAndView("noaccess", "users", new UserModel());
      return mv;
    }

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
        service.exportToExcel(revivedCMRModel, request, response);
        // request.getSession().setAttribute((String) params.getParam("token"),
        // "Y," +
        // MessageUtil.getMessage(MessageUtil.INFO_REV_CMR_FILE_PROCESSED));
      }
    } catch (Exception e) {
      e.printStackTrace();
      LOG.error("Failed processing the revived cmrs file...");
      // request.getSession().setAttribute((String) params.getParam("token"),
      // "N," + MessageUtil.getMessage(MessageUtil.ERROR_GENERAL));
      String host = SystemConfiguration.getValue("MAIL_HOST");
      AppUser user = AppUser.getUser(request);

      Email mail = new Email();
      String from = SystemConfiguration.getValue("MAIL_FROM");
      mail.setSubject("Revived CMRs processing result");
      mail.setTo(user.getIntranetId());
      mail.setFrom(from);
      mail.setType(MessageType.HTML);
      mail.setMessage("Revived CMRs processing encountered an error. Please try again.");

      mail.send(host);
    }
  }

}
