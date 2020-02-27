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
import com.ibm.cio.cmr.request.model.code.StatusActModel;
import com.ibm.cio.cmr.request.service.code.StatusActAdminService;
import com.ibm.cio.cmr.request.service.code.StatusActMaintainService;
import com.ibm.cio.cmr.request.user.AppUser;
import com.ibm.cio.cmr.request.util.MessageUtil;

@Controller
public class StatusActController extends BaseController {
  private static final Logger LOG = Logger.getLogger(StatusActController.class);
  @Autowired
  private StatusActMaintainService statusActMaintainService;
  @Autowired
  private StatusActAdminService statusActAdminService;

  @RequestMapping(value = "/code/statusAct", method = RequestMethod.GET)
  public @ResponseBody
  ModelAndView showRoles(HttpServletRequest request, ModelMap model) {
    AppUser user = AppUser.getUser(request);
    if (!user.isAdmin()) {
      LOG.warn("User " + user.getIntranetId() + " (" + user.getBluePagesName() + ") tried accessing the maintain Validation_Urls Table function.");
      ModelAndView mv = new ModelAndView("noaccess", "user", new StatusActModel());
      return mv;
    }

    // access granted
    ModelAndView mv = new ModelAndView("statusAct", "statusActModel", new StatusActModel());
    setPageKeys("ADMIN", "CODE_ADMIN", mv);
    return mv;
  }

  @RequestMapping(value = "/code/addStatusAct")
  public @ResponseBody
  ModelAndView maintainRoles(HttpServletRequest request, HttpServletResponse response, StatusActModel model) throws CmrException {
    AppUser user = AppUser.getUser(request);
    if (!user.isAdmin()) {
      LOG.warn("User " + user.getIntranetId() + " (" + user.getBluePagesName() + ") tried accessing the maintain SUPP_CNTRY Table function.");
      ModelAndView mv = new ModelAndView("noaccess", "statusActModel", new StatusActModel());
      return mv;
    }

    ModelAndView mv = null;

    if (model.allKeysAssigned()) {

      if (model.getState() == BaseModel.STATE_NEW) {
        model.setModelAction(model.getModelAction().toUpperCase());
      }

      if (shouldProcess(model)) {
        try {
          /*
           * if (model.getCmrIssuingCntry().equals("000")) {
           * model.setCmrIssuingCntry("*"); }
           */
          StatusActModel newModel = statusActMaintainService.save(model, request);
          mv = new ModelAndView("redirect:/code/statusAct", "statusActModel", newModel);
          MessageUtil.setInfoMessage(mv, MessageUtil.INFO_RECORD_SAVED, model.getRecordDescription());
        } catch (Exception e) {
          mv = new ModelAndView("addStatusAct", "statusActModel", model);
          setError(e, mv);
        }
      } else {
        StatusActModel currentModel = new StatusActModel();
        List<StatusActModel> current = statusActMaintainService.search(model, request);
        if (current != null && current.size() > 0) {
          currentModel = current.get(0);
        }
        mv = new ModelAndView("addStatusAct", "statusActModel", currentModel);
      }
    }
    if (mv == null) {
      mv = new ModelAndView("addStatusAct", "statusActModel", new StatusActModel());
    }

    setPageKeys("ADMIN", "CODE_ADMIN", mv);
    return mv;
  }

  @RequestMapping(value = "/code/statusActList", method = { RequestMethod.POST, RequestMethod.GET })
  public ModelMap getRolesList(HttpServletRequest request, HttpServletResponse response, StatusActModel model) throws CmrException {

    List<StatusActModel> results = statusActAdminService.search(model, request);
    return wrapAsSearchResult(results);
  }

}
