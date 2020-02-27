/**
 * 
 */
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
import com.ibm.cio.cmr.request.model.code.ClaimRoleModel;
import com.ibm.cio.cmr.request.model.system.UserModel;
import com.ibm.cio.cmr.request.service.code.ClaimRolesAdminService;
import com.ibm.cio.cmr.request.service.code.ClaimRolesMaintainService;
import com.ibm.cio.cmr.request.user.AppUser;
import com.ibm.cio.cmr.request.util.MessageUtil;

/**
 * @author Eduard Bernardo
 * 
 */
@Controller
public class ClaimRolesController extends BaseController {

  @Autowired
  private ClaimRolesMaintainService maintService;

  @Autowired
  private ClaimRolesAdminService service;

  private static final Logger LOG = Logger.getLogger(ClaimRolesController.class);

  @RequestMapping(value = "/code/claimroles", method = RequestMethod.GET)
  public @ResponseBody
  ModelAndView showClaimRoles(HttpServletRequest request, ModelMap model) {
    AppUser user = AppUser.getUser(request);
    if (!user.isAdmin()) {
      LOG.warn("User " + user.getIntranetId() + " (" + user.getBluePagesName() + ") tried accessing the maintain Claim_Roles Table function.");
      ModelAndView mv = new ModelAndView("noaccess", "users", new UserModel());
      return mv;
    }

    // access granted
    ModelAndView mv = new ModelAndView("claimroleslist", "claimroles", new ClaimRoleModel());
    setPageKeys("ADMIN", "CODE_ADMIN", mv);
    return mv;
  }

  @RequestMapping(value = "/code/claimrolesmain")
  public @ResponseBody
  ModelAndView maintainClaimRoles(HttpServletRequest request, HttpServletResponse response, ClaimRoleModel model) throws CmrException {
    AppUser user = AppUser.getUser(request);
    if (!user.isAdmin()) {
      LOG.warn("User " + user.getIntranetId() + " (" + user.getBluePagesName() + ") tried accessing the maintain Claim_Roles Table function.");
      ModelAndView mv = new ModelAndView("noaccess", "users", new UserModel());
      return mv;
    }

    ModelAndView mv = null;

    if (model.allKeysAssigned()) {

      if (shouldProcess(model)) {
        try {

          ClaimRoleModel newModel = maintService.save(model, request);
          mv = new ModelAndView("redirect:/code/claimroles", "claimroles", newModel);
          MessageUtil.setInfoMessage(mv, MessageUtil.INFO_RECORD_SAVED, model.getRecordDescription());
        } catch (Exception e) {
          mv = new ModelAndView("claimrolesmaintain", "claimrole", model);
          setError(e, mv);
        }
      } else {
        ClaimRoleModel currentModel = new ClaimRoleModel();
        List<ClaimRoleModel> current = maintService.search(model, request);
        if (current != null && current.size() > 0) {
          currentModel = current.get(0);
        }
        mv = new ModelAndView("claimrolesmaintain", "claimrole", currentModel);
      }
    }
    if (mv == null) {
      mv = new ModelAndView("claimrolesmaintain", "claimrole", new ClaimRoleModel());
    }

    setPageKeys("ADMIN", "CODE_ADMIN", mv);
    return mv;
  }

  @RequestMapping(value = "/code/claimroleslist", method = { RequestMethod.POST, RequestMethod.GET })
  public ModelMap getClaimRoleList(HttpServletRequest request, HttpServletResponse response, ClaimRoleModel model) throws CmrException {

    List<ClaimRoleModel> results = service.search(model, request);
    return wrapAsSearchResult(results);
  }
}
