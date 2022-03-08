/**
 * 
 */
package com.ibm.cio.cmr.request.controller.window;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.model.ParamContainer;
import com.ibm.cio.cmr.request.service.dpl.DPLSearchService;
import com.ibm.cio.cmr.request.user.AppUser;

/**
 * @author JeffZAMORA
 *
 */
@Controller
public class DPLSearchController extends BaseWindowController {

  private static final Logger LOG = Logger.getLogger(DPLSearchController.class);

  @Autowired
  private DPLSearchService service;

  @RequestMapping(value = WINDOW_URL + "/dpl/request")
  public ModelAndView showDPLSearchForRequest(HttpServletRequest request, HttpServletResponse response)
      throws CmrException, JsonGenerationException, JsonMappingException, IOException {

    ModelAndView mv = new ModelAndView("dplrequest");
    String reqId = request.getParameter("reqId");

    if (!StringUtils.isNumeric(reqId)) {
      mv.addObject("error", true);
      mv.addObject("reqId", 0);
      mv.addObject("msg", "Invalid Request ID. Request " + reqId + " does not exist.");
    } else {
      mv.addObject("reqId", Long.parseLong(reqId));
      mv.addObject("error", false);
    }

    return getWindow(mv, "Denied Parties List Search");
  }

  @RequestMapping(value = "/dplsearch")
  public ModelAndView showDPLSearchPage(HttpServletRequest request, HttpServletResponse response)
      throws CmrException, JsonGenerationException, JsonMappingException, IOException {

    ModelAndView mv = new ModelAndView("dplsearch");
    setPageKeys("SEARCH_HOME", "DPLSEARCH", mv);
    return mv;
  }

  @RequestMapping("/dplsearch/process")
  public ModelMap processDPLAction(HttpServletRequest request, HttpServletResponse response)
      throws CmrException, JsonGenerationException, JsonMappingException, IOException {

    ModelMap map = new ModelMap();
    String reqId = request.getParameter("reqId");

    if (!StringUtils.isBlank(reqId) && !StringUtils.isNumeric(reqId)) {
      map.addAttribute("error", true);
      map.addAttribute("msg", "Invalid Request ID. Request " + reqId + " does not exist.");
      map.addAttribute("data", null);
    }

    try {
      ParamContainer params = new ParamContainer();
      params.addParam("reqId", Long.parseLong(reqId));
      params.addParam("searchString", request.getParameter("searchString"));
      params.addParam("processType", request.getParameter("processType"));
      params.addParam("user", AppUser.getUser(request));
      params.addParam("assessment", request.getParameter("assessment"));
      params.addParam("assessmentCmt", request.getParameter("assessmentCmt"));
      Object results = service.process(request, params);

      map.addAttribute("data", results);
      map.addAttribute("error", false);
      map.addAttribute("msg", null);
    } catch (Exception e) {
      LOG.debug("Error in processing DPL search request", e);
    }

    return map;
  }

  @RequestMapping("/dplsearch/pdf")
  public void generatePDF(HttpServletRequest request, HttpServletResponse response) throws Exception {
    ParamContainer params = new ParamContainer();
    params.addParam("searchString", request.getParameter("searchString"));
    params.addParam("processType", request.getParameter("processType"));
    AppUser user = AppUser.getUser(request);
    service.generatePDF(user, response, params);

  }
}
