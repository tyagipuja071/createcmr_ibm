/**
 * 
 */
package com.ibm.cio.cmr.request.controller.automation;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.controller.window.BaseWindowController;
import com.ibm.cio.cmr.request.model.automation.AutoResultsModel;
import com.ibm.cio.cmr.request.model.requestentry.RequestEntryModel;
import com.ibm.cio.cmr.request.service.automation.AutoResultsService;

/**
 * Controller for the automation results page
 * 
 * @author JeffZAMORA
 * 
 */
@Controller
public class AutoResultsController extends BaseWindowController {

  private static final Logger LOG = Logger.getLogger(AutoResultsController.class);

  @Autowired
  private AutoResultsService service;

  @RequestMapping(
      value = "/auto/results/{reqId}")
  public ModelAndView showAutomationResults(@PathVariable("reqId") long reqId, HttpServletRequest request, HttpServletResponse response,
      RequestEntryModel model) throws Exception {
    LOG.debug("Displaying results for Request " + reqId);
    return new ModelAndView("auto_results");
  }

  @RequestMapping(
      value = "/auto/results/list")
  public ModelMap doSearch(HttpServletRequest request, HttpServletResponse response, AutoResultsModel model) throws CmrException {

    List<AutoResultsModel> results = service.search(model, request);
    return wrapAsSearchResult(results);
  }
}
