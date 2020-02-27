/**
 * 
 */
package com.ibm.cio.cmr.request.controller.window;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.model.ParamContainer;
import com.ibm.cio.cmr.request.model.window.WorkflowHistWinModel;
import com.ibm.cio.cmr.request.service.window.WorkflowHistoryWinService;

/**
 * @author Jeffrey Zamora
 * 
 */
@Controller
public class WorfkflowHistoryWindowController extends BaseWindowController {

  @Autowired
  private WorkflowHistoryWinService service;

  /**
   * 
   * @param request
   * @param response
   * @param model
   * @return
   * @throws CmrException
   */
  @RequestMapping(value = WINDOW_URL + "/wfhist")
  public ModelAndView showHistory(@RequestParam("reqId") long reqId, HttpServletRequest request, HttpServletResponse response, ModelMap model)
      throws CmrException {
    ParamContainer params = new ParamContainer();
    params.addParam("reqId", reqId);
    WorkflowHistWinModel wfhist = service.process(request, params);
    return getWindow(new ModelAndView("wfhist", "wfhist", wfhist != null ? wfhist : new WorkflowHistWinModel()), "Workflow History for Request ID "
        + reqId);
  }
}
