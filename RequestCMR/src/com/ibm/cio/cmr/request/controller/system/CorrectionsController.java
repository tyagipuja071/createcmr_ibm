/**
 * 
 */
package com.ibm.cio.cmr.request.controller.system;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Context;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.controller.BaseController;
import com.ibm.cio.cmr.request.model.ParamContainer;
import com.ibm.cio.cmr.request.model.system.CorrectionsModel;
import com.ibm.cio.cmr.request.model.system.UserModel;
import com.ibm.cio.cmr.request.service.system.CorrectionsService;
import com.ibm.cio.cmr.request.user.AppUser;

/**
 * Controller for the Admin -> Corrections UI and processes
 * 
 * @author 136786PH1
 *
 */
@Controller
public class CorrectionsController extends BaseController {

  private static final Logger LOG = Logger.getLogger(CorrectionsController.class);
  @Autowired
  private CorrectionsService service;

  @RequestMapping(value = "/corrections", method = RequestMethod.GET)
  public @ResponseBody ModelAndView showCorrectionsHome(HttpServletRequest request, ModelMap model) {
    AppUser user = AppUser.getUser(request);
    if (!user.isCmde()) {
      LOG.warn("User " + user.getIntranetId() + " (" + user.getBluePagesName() + ") tried accessing the Corrections System function.");
      ModelAndView mv = new ModelAndView("noaccess", "users", new UserModel());
      return mv;
    }

    // access granted
    ModelAndView mv = new ModelAndView("corrections");
    setPageKeys("ADMIN", "CORRECTIONS", mv);
    return mv;
  }

  @RequestMapping(value = "/corrections/process", method = RequestMethod.POST)
  public @ResponseBody ModelMap processCorrections(@Context HttpServletRequest request, HttpServletResponse response,
      @RequestBody CorrectionsModel model) {
    AppUser user = AppUser.getUser(request);
    ModelMap out = new ModelMap();
    if (!user.isCmde()) {
      LOG.warn("User " + user.getIntranetId() + " (" + user.getBluePagesName() + ") tried accessing the Corrections System function.");
      out.addAttribute("success", false);
      out.addAttribute("msg", "User has no access to perform the specified function");
      return out;
    }

    ParamContainer params = new ParamContainer();
    params.addParam("model", model);

    try {
      CorrectionsModel outModel = this.service.process(request, params);
      out.addAttribute("success", true);
      out.addAttribute("msg", null);
      out.addAttribute("model", outModel);
      out.addAttribute("fieldMap", CorrectionsModel.getFieldMap());
    } catch (CmrException e) {
      LOG.error("Error in processing corrections", e);
      out.addAttribute("success", false);
      out.addAttribute("msg", "An error has occurred trying to process corrections.");
    }
    return out;
  }

}
