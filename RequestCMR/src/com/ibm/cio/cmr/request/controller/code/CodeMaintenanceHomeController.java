/**
 * 
 */
package com.ibm.cio.cmr.request.controller.code;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.cio.cmr.request.controller.BaseController;
import com.ibm.cio.cmr.request.controller.system.UsersController;
import com.ibm.cio.cmr.request.model.code.CodeMaintHomeModel;
import com.ibm.cio.cmr.request.model.system.UserModel;
import com.ibm.cio.cmr.request.user.AppUser;

/**
 * @author Jeffrey Zamora
 * 
 */
@Controller
public class CodeMaintenanceHomeController extends BaseController {

  private static final Logger LOG = Logger.getLogger(UsersController.class);

  @RequestMapping(value = "/code", method = RequestMethod.GET)
  public @ResponseBody
  ModelAndView showCodeMaintHome(HttpServletRequest request, ModelMap model) {
    AppUser user = AppUser.getUser(request);
    if (!user.isAdmin() && !user.isCmde()) {
      LOG.warn("User " + user.getIntranetId() + " (" + user.getBluePagesName() + ") tried accessing the Code Maintenance system function.");
      ModelAndView mv = new ModelAndView("noaccess", "users", new UserModel());
      return mv;
    }

    // access granted
    ModelAndView mv = new ModelAndView("codehome", "code", new CodeMaintHomeModel());
    setPageKeys("ADMIN", "CODE_ADMIN", mv);
    return mv;
  }

}
