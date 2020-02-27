/**
 * 
 */
package com.ibm.cio.cmr.request.controller.approval;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.cio.cmr.request.controller.BaseController;
import com.ibm.cio.cmr.request.model.ProcessResultModel;
import com.ibm.cio.cmr.request.model.approval.MyApprovalsModel;
import com.ibm.cio.cmr.request.model.requestentry.AttachmentModel;
import com.ibm.cio.cmr.request.service.approval.MyApprovalsService;

/**
 * @author JeffZAMORA
 * 
 */
@Controller
public class MyApprovalsController extends BaseController {

  private static final Logger LOG = Logger.getLogger(MyApprovalsController.class);

  @Autowired
  private MyApprovalsService service;

  @RequestMapping(
      value = "/myappr")
  public ModelAndView showApprovalsList(HttpServletRequest request, HttpServletResponse response, ModelMap model) throws Exception {
    boolean pending = !"Y".equals(request.getParameter("all"));
    ModelAndView mv = new ModelAndView("myapprovals", "approval", new MyApprovalsModel());
    mv.addObject("attach", new AttachmentModel());
    if (pending) {
      setPageKeys("APPROVALS", "APPROVALS_PENDING", mv);
    } else {
      setPageKeys("APPROVALS", "APPROVALS_ALL", mv);
    }
    return mv;
  }

  @RequestMapping(
      value = "/myappr/list")
  public ModelMap getApprovalsList(HttpServletRequest request, HttpServletResponse response, MyApprovalsModel model) throws Exception {
    List<MyApprovalsModel> results = this.service.search(model, request);
    ModelMap map = new ModelMap();
    map.addAttribute("items", results);
    return map;
  }

  @RequestMapping(
      value = "/myappr/process")
  public ModelMap processApprovals(HttpServletRequest request, HttpServletResponse response, MyApprovalsModel model) throws Exception {
    ProcessResultModel result = new ProcessResultModel();
    try {
      LOG.debug("Executing approval (" + ("Y".equals(model.getMass()) ? "mass" : "single") + ") process "
          + (model.getAction() != null ? model.getAction() : model.getMassAction()));
      this.service.processTransaction(model, request);
      result.setSuccess(true);
      result.setMessage("Approval response successfully processed.");
    } catch (Exception e) {
      LOG.error("Error in processing approvals.", e);
      result.setSuccess(false);
      result.setMessage("An error occurred while processing the approvals.");
      result.setDisplayMsg(true);
    }
    return wrapAsProcessResult(result);
  }
}
