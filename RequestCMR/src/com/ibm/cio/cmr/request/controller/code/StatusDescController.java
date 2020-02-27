package com.ibm.cio.cmr.request.controller.code;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.controller.BaseController;
import com.ibm.cio.cmr.request.model.code.StatusDescModel;
import com.ibm.cio.cmr.request.model.system.UserModel;
import com.ibm.cio.cmr.request.service.code.StatusDescMaintService;
import com.ibm.cio.cmr.request.service.code.StatusDescService;
import com.ibm.cio.cmr.request.user.AppUser;
import com.ibm.cio.cmr.request.util.MessageUtil;

/**
 * @author Rochelle Salazar
 * 
 */
@Controller
public class StatusDescController extends BaseController {

  private static final Logger LOG = Logger.getLogger(StatusDescController.class);

  @Autowired
  private StatusDescService service;

  @Autowired
  private StatusDescMaintService addservice;

  @RequestMapping(value = "/code/status_desc", method = RequestMethod.GET)
  public @ResponseBody
  ModelAndView showStatusDesc(HttpServletRequest request, ModelMap model) {
    AppUser user = AppUser.getUser(request);
    if (!user.isAdmin()) {
      LOG.warn("User " + user.getIntranetId() + " (" + user.getBluePagesName() + ") tried accessing the Users system function.");
      ModelAndView mv = new ModelAndView("noaccess", "users", new UserModel());
      return mv;
    }

    // access granted
    ModelAndView mv = new ModelAndView("statusdesclist", "statusdesc", new StatusDescModel());
    setPageKeys("ADMIN", "USER_ADMIN", mv);
    return mv;

  }

  @RequestMapping(value = "/code/addstatusdesc")
  public @ResponseBody
  ModelAndView maintainStatusDesc(HttpServletRequest request, HttpServletResponse response, StatusDescModel model) throws CmrException {
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
          StatusDescModel newModel = addservice.save(model, request);
          mv = new ModelAndView("redirect:/code/addstatusdesc?reqStatus=" + newModel.getReqStatus(), "statusdesc", newModel);
          MessageUtil.setInfoMessage(mv, MessageUtil.INFO_RECORD_SAVED, model.getRecordDescription());
        } catch (Exception e) {
          mv = new ModelAndView("addstatusdesc", "statusdesc", model);
          setError(e, mv);
        }
      } else {
        StatusDescModel currentModel = new StatusDescModel();
        List<StatusDescModel> current = addservice.search(model, request);
        if (current != null && current.size() > 0) {
          currentModel = current.get(0);
        }
        mv = new ModelAndView("addstatusdesc", "statusdesc", currentModel);
      }
    }

    if (mv == null) {
      mv = new ModelAndView("addstatusdesc", "statusdesc", new StatusDescModel());
    }

    setPageKeys("ADMIN", "USER_ADMIN", mv);
    return mv;
  }

  @RequestMapping(value = "/code/statusdescdetails/{reqStatus}")
  public @ResponseBody
  ModelAndView maintainStatusDescDetails(@PathVariable("reqStatus") String reqStatus, HttpServletRequest request, HttpServletResponse response,
      StatusDescModel model) throws CmrException {
    AppUser user = AppUser.getUser(request);
    if (!user.isAdmin()) {
      LOG.warn("User " + user.getIntranetId() + " (" + user.getBluePagesName() + ") tried accessing the Users system function.");
      ModelAndView mv = new ModelAndView("noaccess", "users", new UserModel());
      return mv;
    }

    ModelAndView mv = null;
    StatusDescModel currentModel = new StatusDescModel();
    model.setReqStatus(reqStatus);
    List<StatusDescModel> current = addservice.search(model, request);
    if (current != null && current.size() > 0) {
      currentModel = current.get(0);
    }
    mv = new ModelAndView("addstatusdesc", "statusdesc", currentModel);
    setPageKeys("ADMIN", "USER_ADMIN", mv);
    return mv;
  }

  @RequestMapping(value = "/code/statusdesclist", method = { RequestMethod.POST, RequestMethod.GET })
  public ModelMap getStatusDescList(HttpServletRequest request, HttpServletResponse response, StatusDescModel model) throws CmrException {
    List<StatusDescModel> results = service.search(model, request);
    return wrapAsSearchResult(results);
  }

}
