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
import com.ibm.cio.cmr.request.model.automation.AutoResImportModel;
import com.ibm.cio.cmr.request.service.automation.MatchingRecordsService;

/**
 * Controller for the automation results match import page
 * 
 * @author PoojaTyagi
 * 
 */
@Controller
public class AutoResMatchImportController extends BaseWindowController {

  private static final Logger LOG = Logger.getLogger(AutoResMatchImportController.class);

  @Autowired
  private MatchingRecordsService service;

  @RequestMapping(value = "/auto/results/matches/import/{reqId}")
  public ModelAndView showAutomationResults(@PathVariable("reqId") long reqId, HttpServletRequest request, HttpServletResponse response,
      AutoResImportModel model) throws Exception {
    LOG.debug("Displaying results for Request " + reqId);
    return new ModelAndView("results_match_import");
  }

  @RequestMapping(value = "/auto/results/matches/import/matching_list")
  public ModelMap doSearch(HttpServletRequest request, HttpServletResponse response, AutoResImportModel model) throws CmrException {
    List<AutoResImportModel> results = service.search(model, request);
    return wrapAsSearchResult(results);
  }

  @RequestMapping(value = "/auto/results/matches/importrecord")
  public ModelMap importMatchIntoRequest(HttpServletRequest request, HttpServletResponse response, AutoResImportModel model) throws Exception {
    ModelMap map = new ModelMap();
    try {
      model.setAction("MATCH_IMPORT");
      this.service.processTransaction(model, request);
      map.put("success", true);
      map.put("reqId", model.getRequestId());
      map.put("error", null);
    } catch (Exception e) {
      map.put("success", false);
      map.put("reqId", -1);
      map.put("error", e.getMessage());
    }
    return map;
  }

  @RequestMapping(value = "/auto/results/data/importdata")
  public ModelMap overrideDataIntoRequest(HttpServletRequest request, HttpServletResponse response, AutoResImportModel model) throws Exception {
    ModelMap map = new ModelMap();
    try {
      model.setAction("DATA_OVERRIDE");
      this.service.processTransaction(model, request);
      map.put("success", true);
      map.put("reqId", model.getRequestId());
      map.put("error", null);
    } catch (Exception e) {
      map.put("success", false);
      map.put("reqId", -1);
      map.put("error", e.getMessage());
    }
    return map;
  }
}
