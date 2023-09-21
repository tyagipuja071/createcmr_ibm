/**
 * 
 */
package com.ibm.cio.cmr.request.controller.legacy;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;

import javax.activation.MimetypesFileTypeMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.controller.BaseController;
import com.ibm.cio.cmr.request.model.ParamContainer;
import com.ibm.cio.cmr.request.model.code.CrisReportModel;
import com.ibm.cio.cmr.request.model.legacy.AttachListModel;
import com.ibm.cio.cmr.request.model.legacy.LegacySearchModel;
import com.ibm.cio.cmr.request.model.legacy.LegacySearchResultModel;
import com.ibm.cio.cmr.request.service.code.CrisReportService;
import com.ibm.cio.cmr.request.service.legacy.AttachListService;
import com.ibm.cio.cmr.request.service.legacy.LegacyDetailsService;
import com.ibm.cio.cmr.request.service.legacy.LegacySearchService;
import com.ibm.cio.cmr.request.service.legacy.MQSearchService;
import com.ibm.cio.cmr.request.user.AppUser;
import com.ibm.cio.cmr.request.util.legacy.LegacyDirectObjectContainer;
import com.ibm.cio.cmr.request.util.search.MQSearchResult;

/**
 * Controller for Legacy Search Page
 * 
 * @author JeffZAMORA
 *
 */
@Controller
public class LegacySearchController extends BaseController {

  private static final Logger LOG = Logger.getLogger(LegacySearchController.class);
  private static final MimetypesFileTypeMap MIME_TYPES = new MimetypesFileTypeMap();

  private static final String ERROR_MSG = "You do not have authority to access this function. Make sure you were given access to this facility by the Administrator.";

  @Autowired
  private LegacySearchService service;

  @Autowired
  private LegacyDetailsService detailsService;

  @Autowired
  private MQSearchService mqSearchService;

  @Autowired
  private AttachListService attachService;

  @Autowired
  private CrisReportService crisReportService;

  /**
   * Handles the refresh page
   * 
   * @param request
   * @param model
   * @return
   */
  @RequestMapping(
      value = "/legacysearch",
      method = RequestMethod.GET)
  public @ResponseBody ModelAndView showLegacySearchPage(HttpServletRequest request) {
    LOG.debug("Showing Legacy Search page..");
    ModelMap map = new ModelMap();

    AppUser user = AppUser.getUser(request);

    if (user == null) {
      map.put("ERROR", ERROR_MSG);
    }

    ModelAndView mv = new ModelAndView("legacysearch", "model", new LegacySearchModel());
    setPageKeys("SEARCH_HOME", "LSEARCH", mv);
    return mv;
  }

  /**
   * Handles the refresh page
   * 
   * @param request
   * @param model
   * @return
   * @throws CmrException
   */
  @RequestMapping(
      value = "/legacysearch/process",
      method = RequestMethod.POST)
  public @ResponseBody ModelMap showLegacySearchPage(HttpServletRequest request, LegacySearchModel model) throws CmrException {
    LOG.debug("Showing Legacy Search page..");
    ModelMap map = new ModelMap();

    ParamContainer params = new ParamContainer();
    params.addParam("crit", model);

    List<LegacySearchResultModel> results = service.process(request, params);
    map.addAttribute("results", results);
    return map;
  }

  /**
   * Handles the refresh page
   * 
   * @param request
   * @param model
   * @return
   * @throws CmrException
   */
  @RequestMapping(
      value = "/legacydetails/process",
      method = RequestMethod.GET)
  public @ResponseBody ModelMap showLegacySearchPage(HttpServletRequest request, @RequestParam("cmrNo") String cmrNo,
      @RequestParam("country") String country, @RequestParam("realCty") String realCty) throws CmrException {
    LOG.debug("Processing Legacy Details for CMR No. " + cmrNo + " under " + country);
    ModelMap map = new ModelMap();

    ParamContainer params = new ParamContainer();
    params.addParam("cmrNo", cmrNo);
    params.addParam("country", country);
    params.addParam("realCty", realCty);

    LegacyDirectObjectContainer results = detailsService.process(request, params);
    map.addAttribute("details", results);
    return map;
  }

  /**
   * Handles the refresh page
   * 
   * @param request
   * @param model
   * @return
   */
  @RequestMapping(
      value = "/searchhome",
      method = RequestMethod.GET)
  public @ResponseBody ModelAndView showSearchHomePage(HttpServletRequest request) {
    LOG.debug("Showing Search Home page..");
    ModelMap map = new ModelMap();

    AppUser user = AppUser.getUser(request);

    if (user == null) {
      map.put("ERROR", ERROR_MSG);
    }

    ModelAndView mv = new ModelAndView("searchhome");
    setPageKeys("SEARCH_HOME", "SEARCH_HOME", mv);
    return mv;
  }

  /**
   * Handles the refresh page
   * 
   * @param request
   * @param model
   * @return
   */
  @RequestMapping(
      value = "/mqsearch",
      method = RequestMethod.GET)
  public @ResponseBody ModelAndView showMQSearchPage(HttpServletRequest request) {
    LOG.debug("Showing MQ Search page..");
    ModelMap map = new ModelMap();

    AppUser user = AppUser.getUser(request);

    if (user == null) {
      map.put("ERROR", ERROR_MSG);
    }

    ModelAndView mv = new ModelAndView("mqsearch");
    setPageKeys("SEARCH_HOME", "MQSEARCH", mv);
    return mv;
  }

  @RequestMapping(
      value = "/mqsearch/process",
      method = RequestMethod.GET)
  public @ResponseBody ModelMap processMQSearch(HttpServletRequest request, @RequestParam("cmrNo") String cmrNo,
      @RequestParam("country") String country) throws CmrException {
    LOG.debug("Processing MQ Search for CMR No. " + cmrNo + " under " + country);
    ModelMap map = new ModelMap();

    ParamContainer params = new ParamContainer();
    params.addParam("cmrNo", cmrNo);
    params.addParam("country", country);

    MQSearchResult<?> result = mqSearchService.process(request, params);
    map.addAttribute("result", result);
    return map;
  }

  /**
   * Handles the refresh page
   * 
   * @param request
   * @param model
   * @return
   */
  @RequestMapping(
      value = "/attachlist",
      method = RequestMethod.GET)
  public @ResponseBody ModelAndView showAttachmentList(HttpServletRequest request) {
    LOG.debug("Showing Attachment page..");
    ModelMap map = new ModelMap();

    AppUser user = AppUser.getUser(request);

    if (user == null) {
      map.put("ERROR", ERROR_MSG);
    } else {
      if (!user.isAdmin() && !user.isCmde() && !user.isProcessor()) {
        map.put("ERROR", ERROR_MSG);
      } else {
      }
    }

    ModelAndView mv = new ModelAndView("attachlist");
    setPageKeys("SEARCH_HOME", "FILEATTACH", mv);
    return mv;
  }

  /**
   * Handles the refresh page
   * 
   * @param request
   * @param model
   * @return
   * @throws CmrException
   */
  @RequestMapping(
      value = "/attachlist/get",
      method = RequestMethod.GET)
  public @ResponseBody ModelMap getAttachmentList(HttpServletRequest request) throws CmrException {
    ModelMap map = new ModelMap();
    String reqId = request.getParameter("reqId");
    if (StringUtils.isBlank(reqId) || !StringUtils.isNumeric(reqId)) {
      map.addAttribute("success", false);
      map.addAttribute("msg", "reqId id invalid.");
      map.addAttribute("data", null);
      return map;
    }
    LOG.debug("Getting attachments for Request " + reqId);
    ParamContainer params = new ParamContainer();
    params.addParam("reqId", Long.parseLong(reqId));
    AttachListModel attachList = attachService.process(request, params);
    map.addAttribute("success", true);
    map.addAttribute("msg", null);
    map.addAttribute("data", attachList);
    return map;
  }

  @RequestMapping(
      value = "/attachlist/dl",
      method = RequestMethod.POST)
  public void downloadAttachment(HttpServletRequest request, HttpServletResponse response) throws Exception {

    String fileName = request.getParameter("fileName");
    try {

      File file = new File(fileName);
      if (file.exists()) {
        try (FileInputStream fis = new FileInputStream(file)) {
          String type = "";
          try {
            type = MIME_TYPES.getContentType(fileName);
          } catch (Exception e) {
          }
          if (StringUtils.isEmpty(type)) {
            type = "application/octet-stream";
          }
          response.setCharacterEncoding("UTF-8");
          response.setContentType(type);
          response.addHeader("Content-Type", type);
          response.addHeader("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"");
          IOUtils.copy(fis, response.getOutputStream());
        }
      }
    } catch (Exception e) {
      LOG.debug("Error in downloading file.", e);
    }
  }

  /**
   * Handles the refresh page
   *
   * @param request
   * @param model
   * @return
   */
  @RequestMapping(
      value = "/crisreport",
      method = RequestMethod.GET)
  public @ResponseBody ModelAndView showCrisReportPage(HttpServletRequest request) {
    LOG.debug("Showing CRIS Report page..");
    ModelMap map = new ModelMap();

    AppUser user = AppUser.getUser(request);

    if (user == null) {
      map.put("ERROR", ERROR_MSG);
    }

    ModelAndView mv = new ModelAndView("crisreport");
    setPageKeys("SEARCH_HOME", "CRISREPORT", mv);
    return mv;
  }

  @RequestMapping(
      value = "/crisreport/export",
      method = RequestMethod.GET)
  public void generateReport(HttpServletRequest request, HttpServletResponse response, CrisReportModel model) throws CmrException {

    String timeframe = request.getParameter("timeframe");
    String dateFrom = request.getParameter("dateFrom");
    String dateTo = request.getParameter("dateTo");

    ParamContainer params = new ParamContainer();

    params.addParam("timeframe", timeframe);
    params.addParam("dateFrom", dateFrom);
    params.addParam("dateTo", dateTo);

    try {
      List<CrisReportModel> records = crisReportService.process(request, params);
      if (records != null) {
        if (timeframe.equalsIgnoreCase("RAONDEMAND")) {
          crisReportService.raOnDemandExportToCsvFile(records, timeframe, response);
        } else if (timeframe.equalsIgnoreCase("TAIGADAILY")) {
          crisReportService.taigaDailyExportToTextFile(records, timeframe, response);
        } else if (timeframe.equalsIgnoreCase("TAIGAMONTHLY")) {
          crisReportService.taigaMonthlyExportToTextFile(records, timeframe, response);
        } else if (timeframe.equalsIgnoreCase("ROLDAILY")) {
          crisReportService.rolDailyExportToTextFile(records, timeframe, response);
        } else if (timeframe.equalsIgnoreCase("ROLMONTHLY")) {
          crisReportService.rolMonthlyExportToTextFile(records, timeframe, response);
        }
      }
    } catch (Exception e) {
      LOG.debug("Cannot export Japan CRIS Report for Users.", e);
    }
    LOG.info("Successfully exported Japan CRIS Report for Users.");
  }

}
