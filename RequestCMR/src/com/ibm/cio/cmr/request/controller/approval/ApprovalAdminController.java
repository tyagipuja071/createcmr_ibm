/**
 * 
 */
package com.ibm.cio.cmr.request.controller.approval;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.controller.BaseController;
import com.ibm.cio.cmr.request.model.approval.ApprovalResponseModel;
import com.ibm.cio.cmr.request.model.system.UserModel;
import com.ibm.cio.cmr.request.user.AppUser;

/**
 * @author Jeffrey Zamora
 * 
 */
@Controller
public class ApprovalAdminController extends BaseController {

  private static final Logger LOG = Logger.getLogger(ApprovalAdminController.class);

  @RequestMapping(
      value = "/approvalsadminlist")
  public ModelAndView showApprovalsAdminPage(HttpServletRequest request, HttpServletResponse response, ApprovalResponseModel model)
      throws CmrException {
    AppUser user = AppUser.getUser(request);
    if (user == null) {
      ModelAndView mv = new ModelAndView("redirect:/timeout");
      return mv;
    } else {
      if (!user.isAdmin() && !user.isCmde()) {
        LOG.warn("User " + user.getIntranetId() + " (" + user.getBluePagesName() + ") tried accessing the Approvals Admin system function.");
        ModelAndView mv = new ModelAndView("noaccess", "users", new UserModel());
        return mv;
      }
      ModelAndView mv = new ModelAndView("approvalsadminlist", "approval", model);

      setPageKeys("ADMIN", "APPROVALS_ADMIN", mv);
      return mv;
    }
  }

  @RequestMapping(
      value = "/approvalsadmin")
  public ModelAndView showApprovalsAdminPageMaint(HttpServletRequest request, HttpServletResponse response, ApprovalResponseModel model)
      throws CmrException {
    AppUser user = AppUser.getUser(request);
    if (user == null) {
      ModelAndView mv = new ModelAndView("redirect:/timeout");
      return mv;
    } else {
      if (!user.isAdmin() && !user.isCmde()) {
        LOG.warn("User " + user.getIntranetId() + " (" + user.getBluePagesName() + ") tried accessing the Approvals Admin system function.");
        ModelAndView mv = new ModelAndView("noaccess", "users", new UserModel());
        return mv;
      }
      ModelAndView mv = new ModelAndView("approvalsadmin", "approval", model);

      setPageKeys("ADMIN", "APPROVALS_ADMIN", mv);
      return mv;
    }
  }

}
