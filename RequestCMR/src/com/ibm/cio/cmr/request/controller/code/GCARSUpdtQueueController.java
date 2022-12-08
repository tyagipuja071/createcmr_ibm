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
import com.ibm.cio.cmr.request.model.code.GCARSUpdtQueueModel;
import com.ibm.cio.cmr.request.service.code.GCARSUpdtQueueMaintainService;
import com.ibm.cio.cmr.request.service.code.GCARSUpdtQueueService;
import com.ibm.cio.cmr.request.user.AppUser;

@Controller
public class GCARSUpdtQueueController extends BaseController {

  @Autowired
  private GCARSUpdtQueueService service;

  @Autowired
  private GCARSUpdtQueueMaintainService maintainService;

  private static final Logger LOG = Logger.getLogger(GCARSUpdtQueueController.class);

  @RequestMapping(
      value = "/code/gcars_updt_queue",
      method = RequestMethod.GET)
  public @ResponseBody ModelAndView showGCARSUpdtQueue(HttpServletRequest request, ModelMap model) {
    AppUser user = AppUser.getUser(request);
    if (!user.isAdmin() && !user.isCmde()) {
      LOG.warn(
          "User " + user.getIntranetId() + " (" + user.getBluePagesName() + ") tried accessing the GCARS UPDT QUEUE Maintenance system function.");

      ModelAndView mv = new ModelAndView("noaccess", "gcars_updt_queue", new GCARSUpdtQueueModel());
      return mv;
    }

    // access granted
    ModelAndView mv = new ModelAndView("gcarsupdtqueue", "gcars_updt_queue", new GCARSUpdtQueueModel());
    setPageKeys("ADMIN", "CODE_ADMIN", mv);

    return mv;
  }

  @RequestMapping(
      value = "/code/gcars_updt_queue_list",
      method = { RequestMethod.POST, RequestMethod.GET })
  public ModelMap getGCARSUpdtQueueList(HttpServletRequest request, HttpServletResponse response, GCARSUpdtQueueModel model) throws CmrException {
    List<GCARSUpdtQueueModel> results = service.search(model, request);
    return wrapAsSearchResult(results);
  }

  @RequestMapping(
      value = "/code/gcars_updt_queue_form")
  public @ResponseBody ModelAndView gcarsUpdtQueueMaintenance(HttpServletRequest request, HttpServletResponse response, GCARSUpdtQueueModel model)
      throws CmrException {

    AppUser user = AppUser.getUser(request);
    if (!user.isAdmin() && !user.isCmde()) {
      LOG.warn(
          "User " + user.getIntranetId() + " (" + user.getBluePagesName() + ") tried accessing the US GCARS UPDT QUEUE Maintenance system function.");
      ModelAndView mv = new ModelAndView("noaccess", "gcars_updt_queue", new GCARSUpdtQueueModel());
      return mv;
    }

    ModelAndView mv = null;

    if (model.allKeysAssigned()) {

      if (!shouldProcess(model)) {
        GCARSUpdtQueueModel currentModel = new GCARSUpdtQueueModel();
        List<GCARSUpdtQueueModel> current = maintainService.search(model, request);

        if (current != null && current.size() > 0) {
          currentModel = current.get(0);
        }

        mv = new ModelAndView("gcarsupdtqueueform", "gcars_updt_queue", currentModel);
      }
    }

    if (mv == null) {
      mv = new ModelAndView("gcarsupdtqueueform", "gcars_updt_queue", new GCARSUpdtQueueModel());
    }

    setPageKeys("ADMIN", "CODE_ADMIN", mv);
    return mv;
  }
}