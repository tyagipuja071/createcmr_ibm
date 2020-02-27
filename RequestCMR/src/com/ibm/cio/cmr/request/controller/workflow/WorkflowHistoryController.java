package com.ibm.cio.cmr.request.controller.workflow;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.controller.BaseController;
import com.ibm.cio.cmr.request.model.WorkflowHistoryModel;
import com.ibm.cio.cmr.request.service.workflow.WorkflowHistoryService;

/**
 * Controller WorkFlow Home page
 * 
 * @author Rama
 * 
 */
@Controller
public class WorkflowHistoryController extends BaseController {

  @Autowired
  private WorkflowHistoryService service;

  @RequestMapping(value = "/workflow/history/list", method = { RequestMethod.POST, RequestMethod.GET })
  public ModelMap getWorkflowHistoryList(HttpServletRequest request, HttpServletResponse response, WorkflowHistoryModel model) throws CmrException {

    if (StringUtils.isNotEmpty(request.getParameter("reqId"))) {
      model.setReqId(Long.parseLong(request.getParameter("reqId")));
    } else {
      // return empty list if no ID
      List<WorkflowHistoryModel> results = new ArrayList<WorkflowHistoryModel>();
      ModelMap map = new ModelMap();
      map.addAttribute("items", results);
      return map;
    }

    List<WorkflowHistoryModel> results = service.search(model, request);
    return wrapAsSearchResult(results);
  }

}
