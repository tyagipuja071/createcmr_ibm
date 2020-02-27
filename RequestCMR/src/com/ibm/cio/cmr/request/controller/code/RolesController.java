package com.ibm.cio.cmr.request.controller.code;

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
import com.ibm.cio.cmr.request.model.BaseModel;
import com.ibm.cio.cmr.request.model.code.RolesModel;
import com.ibm.cio.cmr.request.service.code.RolesAdminService;
import com.ibm.cio.cmr.request.service.code.RolesMaintainService;
import com.ibm.cio.cmr.request.user.AppUser;
import com.ibm.cio.cmr.request.util.MessageUtil;

/**
 * @author Jose Belgira
 * 
 * 
 */

@Controller
public class RolesController extends BaseController {
  private static final Logger LOG = Logger.getLogger(RolesController.class);
  @Autowired
  private RolesMaintainService rolesMaintainService;
  @Autowired
  private RolesAdminService rolesAdminService;

  @RequestMapping(value = "/code/roles", method = RequestMethod.GET)
  public @ResponseBody
  ModelAndView showRoles(HttpServletRequest request, ModelMap model) {
    AppUser user = AppUser.getUser(request);
    if (!user.isAdmin()) {
      LOG.warn("User " + user.getIntranetId() + " (" + user.getBluePagesName() + ") tried accessing the maintain Validation_Urls Table function.");
      ModelAndView mv = new ModelAndView("noaccess", "user", new RolesModel());
      return mv;
    }

    // access granted
    ModelAndView mv = new ModelAndView("roles", "rolesModel", new RolesModel());
    setPageKeys("ADMIN", "CODE_ADMIN", mv);
    return mv;
  }

  @RequestMapping(value = "/code/addRoles")
  public @ResponseBody
  ModelAndView maintainRoles(HttpServletRequest request, HttpServletResponse response, RolesModel model) throws CmrException {
    AppUser user = AppUser.getUser(request);
    if (!user.isAdmin()) {
      LOG.warn("User " + user.getIntranetId() + " (" + user.getBluePagesName() + ") tried accessing the maintain SUPP_CNTRY Table function.");
      ModelAndView mv = new ModelAndView("noaccess", "rolesModel", new RolesModel());
      return mv;
    }

    ModelAndView mv = null;

    if (model.allKeysAssigned()) {

      if (shouldProcess(model)) {
        try {
          if (model.getState() == BaseModel.STATE_NEW) {
            model.setRoleId(model.getRoleId().toUpperCase());
            model.setCreateBy(user.getIntranetId());
            model.setUpdateBy(user.getIntranetId());
          } else {
            model.setUpdateBy(user.getIntranetId());
          }

          RolesModel newModel = rolesMaintainService.save(model, request);
          mv = new ModelAndView("redirect:/code/roles", "rolesModel", newModel);
          MessageUtil.setInfoMessage(mv, MessageUtil.INFO_RECORD_SAVED, model.getRecordDescription());
        } catch (Exception e) {
          mv = new ModelAndView("addRoles", "rolesModel", model);
          setError(e, mv);
        }
      } else {
        RolesModel currentModel = new RolesModel();
        List<RolesModel> current = rolesMaintainService.search(model, request);
        if (current != null && current.size() > 0) {
          currentModel = current.get(0);
        }
        mv = new ModelAndView("addRoles", "rolesModel", currentModel);
      }
    }
    if (mv == null) {
      RolesModel newModel = new RolesModel();
      newModel.setUser(user.getBluePagesName());

      mv = new ModelAndView("addRoles", "rolesModel", newModel);
    }

    setPageKeys("ADMIN", "CODE_ADMIN", mv);
    return mv;
  }

  @RequestMapping(value = "/code/roleslist", method = { RequestMethod.POST, RequestMethod.GET })
  public ModelMap getRolesList(HttpServletRequest request, HttpServletResponse response, RolesModel model) throws CmrException {

    List<RolesModel> results = rolesAdminService.search(model, request);
    return wrapAsSearchResult(results);
  }

}
