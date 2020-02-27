/**
 * 
 */
package com.ibm.cio.cmr.request.controller.workflow;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.controller.BaseController;
import com.ibm.cio.cmr.request.model.WorkflowHistoryModel;
import com.ibm.cio.cmr.request.model.workflow.WorkflowRequestsModel;
import com.ibm.cio.cmr.request.service.workflow.WorkflowService;
import com.ibm.cio.cmr.request.user.AppUser;
import com.ibm.cio.cmr.request.util.MessageUtil;

/**
 * Controller for displaying open request functions
 * 
 * @author Sonali Jain
 * 
 */
@Controller
public class WorkflowController extends BaseController {

  @Autowired
  private WorkflowService service;

  /**
   * Handles the display of the open request page under workflow
   * 
   * @param request
   * @param response
   * @param model
   * @return
   */

  @RequestMapping(
      value = "/workflow/{type}")
  public ModelAndView showRequestList(@PathVariable("type") String type, HttpServletRequest request, HttpServletResponse response,
      WorkflowRequestsModel model) {

    if ("open".equals(type)) {
      ModelAndView mv = new ModelAndView("openreq", "wfreq", model);
      mv.addObject("wfhist", new WorkflowHistoryModel());
      setPageKeys("WORKFLOW", "OPEN_REQ", mv);
      return mv;
    } else if ("completed".equals(type)) {
      ModelAndView mv = new ModelAndView("completedreq", "wfreq", model);
      mv.addObject("wfhist", new WorkflowHistoryModel());
      setPageKeys("WORKFLOW", "COMPLETED_REQ", mv);
      return mv;
    } else if ("rejected".equals(type)) {
      ModelAndView mv = new ModelAndView("rejectedreq", "wfreq", model);
      mv.addObject("wfhist", new WorkflowHistoryModel());
      setPageKeys("WORKFLOW", "REJECTED_REQ", mv);
      return mv;
    } else if ("all".equals(type)) {
      ModelAndView mv = new ModelAndView("allreq", "wfreq", model);
      mv.addObject("wfhist", new WorkflowHistoryModel());
      setPageKeys("WORKFLOW", "ALL_REQ", mv);
      return mv;
    } else {
      return new ModelAndView("error", "page", model);
    }
  }

  /**
   * Handles the retrival part of the open request
   * 
   * @param request
   * @param response
   * @param model
   * @return
   */

  @RequestMapping(
      value = "/workflow/{type}/list")
  public ModelMap doSearchOpenReq(@PathVariable("type") String type, HttpServletRequest request, HttpServletResponse response,
      WorkflowRequestsModel model) throws CmrException {
    if (StringUtils.isEmpty(model.getUserId()) && AppUser.getUser(request) != null) {
      model.setUserId(AppUser.getUser(request).getIntranetId());
    }

    if ("open".equals(type) || "completed".equals(type) || "rejected".equals(type) || "all".equals(type)) {
      model.setWorkflowType(type);
    } else {
      response.setStatus(HttpStatus.SC_BAD_REQUEST);
      // return an empty list if not mapped
      return wrapAsSearchResult(new ArrayList<WorkflowRequestsModel>());
    }

    // use model.workflowType in the service to know what to search for
    List<WorkflowRequestsModel> results = service.search(model, request);
    return wrapAsSearchResult(results);
  }

  /**
   * Processes the claim
   * 
   * @param type
   * @param request
   * @param response
   * @param model
   * @return
   * @throws CmrException
   */
  @RequestMapping(
      value = "/workflow/{type}/claim")
  public ModelAndView doClaimReq(@PathVariable("type") String type, HttpServletRequest request, HttpServletResponse response,
      WorkflowRequestsModel model) throws CmrException {

    ModelAndView mv = null;
    try {
      service.processTransaction(model, request);
      // change done to redirect to req entry page after claiming
      // mv = new ModelAndView("redirect:/workflow/"+type);
      mv = new ModelAndView("redirect:/request/" + model.getReqId());

      String action = model.getAction();

      if ("CLAIM".equals(action)) {
        MessageUtil.setInfoMessage(mv, MessageUtil.INFO_CLAIM_SUCCESSFUL);
      }
      if ("REPROCESS".equals(action)) {
        MessageUtil.setInfoMessage(mv, MessageUtil.INFO_REQUEST_PROCESSED, "requeued for automated checks", model.getReqId() + "");
      }

      mv.addObject("wfreq", new WorkflowRequestsModel());
      mv.addObject("wfhist", new WorkflowHistoryModel());
    } catch (Exception e) {
      mv = new ModelAndView("redirect:/workflow/" + type, "wfreq", model);
      setError(e, mv);
    }

    return mv;

  }

}
