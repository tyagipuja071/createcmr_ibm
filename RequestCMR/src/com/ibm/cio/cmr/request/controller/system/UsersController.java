/**
 * 
 */
package com.ibm.cio.cmr.request.controller.system;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.controller.BaseController;
import com.ibm.cio.cmr.request.model.system.UserModel;
import com.ibm.cio.cmr.request.model.system.UserRoleModel;
import com.ibm.cio.cmr.request.service.system.UserAdminService;
import com.ibm.cio.cmr.request.service.system.UserMaintainService;
import com.ibm.cio.cmr.request.service.system.UserRoleService;
import com.ibm.cio.cmr.request.user.AppUser;
import com.ibm.cio.cmr.request.util.MessageUtil;

/**
 * @author Jeffrey Zamora
 * 
 */
@Controller
public class UsersController extends BaseController {

  @Autowired
  private UserAdminService service;

  @Autowired
  private UserMaintainService maintService;

  @Autowired
  private UserRoleService userRoleService;

  private static final Logger LOG = Logger.getLogger(UsersController.class);

  @RequestMapping(value = "/users", method = RequestMethod.GET)
  public @ResponseBody
  ModelAndView showUsers(HttpServletRequest request, ModelMap model) {
    AppUser user = AppUser.getUser(request);
    if (!user.isAdmin()) {
      LOG.warn("User " + user.getIntranetId() + " (" + user.getBluePagesName() + ") tried accessing the Users system function.");
      ModelAndView mv = new ModelAndView("noaccess", "users", new UserModel());
      return mv;
    }

    // access granted
    ModelAndView mv = new ModelAndView("userlist", "users", new UserModel());
    setPageKeys("ADMIN", "USER_ADMIN", mv);
    return mv;
  }

  @RequestMapping(value = "/user")
  public @ResponseBody
  ModelAndView maintainUser(HttpServletRequest request, HttpServletResponse response, UserModel model) throws CmrException {
    AppUser user = AppUser.getUser(request);
    if (!user.isAdmin()) {
      LOG.warn("User " + user.getIntranetId() + " (" + user.getBluePagesName() + ") tried accessing the Users system function.");
      ModelAndView mv = new ModelAndView("noaccess", "users", new UserModel());
      return mv;
    }

    ModelAndView mv = null;

    if (model.allKeysAssigned()) {

      if (shouldProcess(model)) {
        try {

          UserModel newModel = maintService.save(model, request);
          mv = new ModelAndView("redirect:/user?userId=" + newModel.getUserId(), "user", newModel);
          MessageUtil.setInfoMessage(mv, MessageUtil.INFO_RECORD_SAVED, model.getRecordDescription());
        } catch (Exception e) {
          mv = new ModelAndView("usermaintain", "user", model);
          setError(e, mv);
        }
      } else {
        UserModel currentModel = new UserModel();
        List<UserModel> current = maintService.search(model, request);
        if (current != null && current.size() > 0) {
          currentModel = current.get(0);
        }
        mv = new ModelAndView("usermaintain", "user", currentModel);
      }
    }
    if (mv == null) {
      mv = new ModelAndView("usermaintain", "user", new UserModel());
    }

    mv.addObject("userrole", new UserRoleModel());

    setPageKeys("ADMIN", "USER_ADMIN", mv);
    return mv;
  }

  @RequestMapping(value = "/userroles")
  public @ResponseBody
  ModelAndView maintainUserRoles(HttpServletRequest request, HttpServletResponse response, UserRoleModel model) throws CmrException {
    AppUser user = AppUser.getUser(request);
    if (!user.isAdmin()) {
      LOG.warn("User " + user.getIntranetId() + " (" + user.getBluePagesName() + ") tried accessing the Users system function.");
      ModelAndView mv = new ModelAndView("noaccess", "users", new UserModel());
      return mv;
    }

    ModelAndView mv = null;

    if (shouldProcess(model) || model.getAction() != null) {
      try {
        // only batch processing supported here
        mv = new ModelAndView("redirect:/user?userId=" + model.getUserId(), "user", model);
        userRoleService.processTransaction(model, request);
        if ("REMOVE_ROLES".equals(model.getMassAction())) {
          MessageUtil.setInfoMessage(mv, "Roles removed successfully.");
        } else if ("ADD_ROLES".equals(model.getAction())) {
          MessageUtil.setInfoMessage(mv, "Roles added successfully.");
        }
      } catch (Exception e) {
        mv = new ModelAndView("usermaintain", "user", model);
        setError(e, mv);
      }
    }
    if (mv == null) {
      mv = new ModelAndView("usermaintain", "user", model);
    }

    mv.addObject("userrole", new UserRoleModel());

    setPageKeys("ADMIN", "USER_ADMIN", mv);
    return mv;
  }

  @RequestMapping(value = "/userlist", method = { RequestMethod.POST, RequestMethod.GET })
  public ModelMap getUserList(HttpServletRequest request, HttpServletResponse response, UserModel model) throws CmrException {

    List<UserModel> results = service.search(model, request);
    return wrapAsSearchResult(results);
  }

  @RequestMapping(value = "/userrolelist", method = { RequestMethod.POST, RequestMethod.GET })
  public ModelMap getUserRoleList(HttpServletRequest request, HttpServletResponse response, UserRoleModel model) throws CmrException {

    List<UserRoleModel> userRoles = userRoleService.search(model, request);
    return wrapAsSearchResult(userRoles);
  }

}
