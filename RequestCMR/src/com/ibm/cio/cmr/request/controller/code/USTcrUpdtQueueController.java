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
import com.ibm.cio.cmr.request.model.code.USTCRUpdtQueueModel;
import com.ibm.cio.cmr.request.service.code.USTcrUpdtQueueMaintainService;
import com.ibm.cio.cmr.request.service.code.USTcrUpdtQueueService;
import com.ibm.cio.cmr.request.user.AppUser;

@Controller
public class USTcrUpdtQueueController extends BaseController {

  @Autowired
  private USTcrUpdtQueueService service;

  @Autowired
  private USTcrUpdtQueueMaintainService maintainService;

  private static final Logger LOG = Logger.getLogger(USTcrUpdtQueueController.class);

  @RequestMapping(value = "/code/us_tcr_updt_queue", method = RequestMethod.GET)
  public @ResponseBody ModelAndView showUSTcrUpdtQueue(HttpServletRequest request, ModelMap model) {
    AppUser user = AppUser.getUser(request);
    if (!user.isAdmin() && !user.isCmde()) {
      LOG.warn(
          "User " + user.getIntranetId() + " (" + user.getBluePagesName() + ") tried accessing the US TCR UPDT QUEUE Maintenance system function.");

      ModelAndView mv = new ModelAndView("noaccess", "us_tcr_updt_queue", new USTCRUpdtQueueModel());
      return mv;
    }

    // access granted
    ModelAndView mv = new ModelAndView("ustcrupdtqueue", "us_tcr_updt_queue", new USTCRUpdtQueueModel());
    setPageKeys("ADMIN", "CODE_ADMIN", mv);

    return mv;
  }

  @RequestMapping(value = "/code/us_tcr_updt_queue_list", method = { RequestMethod.POST, RequestMethod.GET })
  public ModelMap getUSTcrUpdtQueueList(HttpServletRequest request, HttpServletResponse response, USTCRUpdtQueueModel model) throws CmrException {
    List<USTCRUpdtQueueModel> results = service.search(model, request);
    return wrapAsSearchResult(results);
  }

  @RequestMapping(value = "/code/us_tcr_updt_queue_form")
  public @ResponseBody ModelAndView usTcrUpdtQueueMaintenance(HttpServletRequest request, HttpServletResponse response, USTCRUpdtQueueModel model)
      throws CmrException {

    AppUser user = AppUser.getUser(request);
    if (!user.isAdmin() && !user.isCmde()) {
      LOG.warn(
          "User " + user.getIntranetId() + " (" + user.getBluePagesName() + ") tried accessing the US TCR UPDT QUEUE Maintenance system function.");
      ModelAndView mv = new ModelAndView("noaccess", "us_tcr_updt_queue", new USTCRUpdtQueueModel());
      return mv;
    }

    ModelAndView mv = null;

    if (model.allKeysAssigned()) {

      if (!shouldProcess(model)) {
        USTCRUpdtQueueModel currentModel = new USTCRUpdtQueueModel();
        List<USTCRUpdtQueueModel> current = maintainService.search(model, request);

        if (current != null && current.size() > 0) {
          currentModel = current.get(0);
        }

        mv = new ModelAndView("ustcrupdtqueueform", "us_tcr_updt_queue", currentModel);
      }
    }

    if (mv == null) {
      mv = new ModelAndView("ustcrupdtqueueform", "us_tcr_updt_queue", new USTCRUpdtQueueModel());
    }

    setPageKeys("ADMIN", "CODE_ADMIN", mv);
    return mv;
  }

}