/**
 * 
 */
package com.ibm.cio.cmr.request.controller.system;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.controller.BaseController;
import com.ibm.cio.cmr.request.model.ParamContainer;
import com.ibm.cio.cmr.request.model.code.FieldInfoModel;
import com.ibm.cio.cmr.request.model.system.MetricsChart;
import com.ibm.cio.cmr.request.model.system.MetricsModel;
import com.ibm.cio.cmr.request.service.system.MetricsService;
import com.ibm.cio.cmr.request.service.system.SquadStatisticsService;
import com.ibm.cio.cmr.request.service.system.StatisticsService;
import com.ibm.cio.cmr.request.service.system.WebSvcUsageService;
import com.ibm.cio.cmr.request.user.AppUser;
import com.ibm.cio.cmr.request.util.system.RequestStatsContainer;

/**
 * @author Jeffrey Zamora
 * 
 */
@Controller
public class MetricsController extends BaseController {

  private static final Logger LOG = Logger.getLogger(MetricsController.class);

  @Autowired
  private MetricsService service;

  @Autowired
  private StatisticsService statService;

  @Autowired
  private SquadStatisticsService squadService;

  @Autowired
  private WebSvcUsageService usageService;

  /**
   * Handles the metrics page
   * 
   * @param request
   * @param model
   * @return
   */
  @RequestMapping(value = "/metrics/daily", method = RequestMethod.GET)
  public ModelAndView showMetricsPage(HttpServletRequest request, ModelMap model) {
    AppUser user = AppUser.getUser(request);
    if (!user.isAdmin() && !user.isCmde() && !user.isProcessor()) {
      LOG.warn("User " + user.getIntranetId() + " (" + user.getBluePagesName() + ") tried accessing the Fields system function.");
      ModelAndView mv = new ModelAndView("noaccess", "fields", new FieldInfoModel());
      return mv;
    }
    MetricsModel metrics = new MetricsModel();
    Calendar cal = new GregorianCalendar();
    cal.setTime(new Date());
    String now = MetricsService.FORMATTER.format(cal.getTime());
    for (int i = 1; i <= 29; i++) {
      cal.add(Calendar.DATE, -1);
    }
    String prev = MetricsService.FORMATTER.format(cal.getTime());

    metrics.setDateFrom(prev);
    metrics.setDateTo(now);
    ModelAndView mv = new ModelAndView("metrics", "metrics", metrics);
    setPageKeys("METRICS", "METRICS_DAILY", mv);
    return mv;

  }

  /**
   * Handles the metrics page
   * 
   * @param request
   * @param model
   * @return
   */
  @RequestMapping(value = "/metrics/usage", method = RequestMethod.GET)
  public ModelAndView showServiceUsagePage(HttpServletRequest request, ModelMap model) {
    AppUser user = AppUser.getUser(request);
    if (!user.isAdmin()) {
      LOG.warn("User " + user.getIntranetId() + " (" + user.getBluePagesName() + ") tried accessing the Fields system function.");
      ModelAndView mv = new ModelAndView("noaccess", "fields", new FieldInfoModel());
      return mv;
    }
    MetricsModel metrics = new MetricsModel();
    Calendar cal = new GregorianCalendar();
    cal.setTime(new Date());
    String now = MetricsService.FORMATTER.format(cal.getTime());
    for (int i = 1; i <= 29; i++) {
      cal.add(Calendar.DATE, -1);
    }
    String prev = MetricsService.FORMATTER.format(cal.getTime());

    metrics.setDateFrom(prev);
    metrics.setDateTo(now);
    ModelAndView mv = new ModelAndView("usage", "usage", metrics);
    setPageKeys("METRICS", "METRICS_USAGE", mv);
    return mv;

  }

  /**
   * Handles the metrics page
   * 
   * @param request
   * @param model
   * @return
   */
  @RequestMapping(value = "/metrics/stats", method = RequestMethod.GET)
  public ModelAndView showStatsPage(HttpServletRequest request, ModelMap model) {
    AppUser user = AppUser.getUser(request);
    if (!user.isAdmin() && !user.isCmde() && !user.isProcessor()) {
      LOG.warn("User " + user.getIntranetId() + " (" + user.getBluePagesName() + ") tried accessing the Fields system function.");
      ModelAndView mv = new ModelAndView("noaccess", "fields", new FieldInfoModel());
      return mv;
    }
    MetricsModel metrics = new MetricsModel();
    Calendar cal = new GregorianCalendar();
    cal.setTime(new Date());
    String now = MetricsService.FORMATTER.format(cal.getTime());
    for (int i = 1; i <= 29; i++) {
      cal.add(Calendar.DATE, -1);
    }
    String prev = MetricsService.FORMATTER.format(cal.getTime());

    metrics.setDateFrom(prev);
    metrics.setDateTo(now);
    ModelAndView mv = new ModelAndView("stats", "metrics", metrics);
    setPageKeys("METRICS", "METRICS_STATS", mv);
    return mv;

  }

  /**
   * Handles the param listing page
   * 
   * @param request
   * @param model
   * @return
   * @throws CmrException
   */
  @RequestMapping(value = "/metrics/generate")
  public ModelMap generateMetrics(HttpServletRequest request, MetricsModel model) throws CmrException {
    ModelMap map = new ModelMap();
    ParamContainer params = new ParamContainer();
    params.addParam("model", model);
    params.addParam("export", false);
    try {
      MetricsChart chart = service.process(request, params);
      map.addAttribute("success", true);
      map.addAttribute("chart", chart);
    } catch (Exception e) {
      map.addAttribute("success", false);
      map.addAttribute("chart", null);
      map.addAttribute("error", e.getMessage());
    }
    return map;
  }

  /**
   * Handles the param listing page
   * 
   * @param request
   * @param model
   * @return
   * @throws CmrException
   */
  @RequestMapping(value = "/metrics/gen_usage")
  public ModelMap generateServiceUsage(HttpServletRequest request, MetricsModel model) throws CmrException {
    ModelMap map = new ModelMap();
    ParamContainer params = new ParamContainer();
    params.addParam("model", model);
    params.addParam("export", false);
    try {
      MetricsChart chart = usageService.process(request, params);
      map.addAttribute("success", true);
      map.addAttribute("chart", chart);
    } catch (Exception e) {
      map.addAttribute("success", false);
      map.addAttribute("chart", null);
      map.addAttribute("error", e.getMessage());
    }
    return map;
  }

  /**
   * Handles the param listing page
   * 
   * @param request
   * @param model
   * @return
   * @throws CmrException
   */
  @RequestMapping(value = "/metrics/export")
  public void generateExport(HttpServletRequest request, HttpServletResponse response, MetricsModel model) throws CmrException {
    ParamContainer params = new ParamContainer();
    params.addParam("model", model);
    params.addParam("export", true);
    try {
      MetricsChart chart = service.process(request, params);
      service.exportToExcel(model, chart, response);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Handles the param listing page
   * 
   * @param request
   * @param model
   * @return
   * @throws CmrException
   */
  @RequestMapping(value = "/metrics/usage_export")
  public void generateUsageExport(HttpServletRequest request, HttpServletResponse response, MetricsModel model) throws CmrException {
    ParamContainer params = new ParamContainer();
    params.addParam("model", model);
    params.addParam("export", true);
    try {
      MetricsChart chart = usageService.process(request, params);
      usageService.exportToExcel(model, chart, response);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Handles the param listing page
   * 
   * @param request
   * @param model
   * @return
   * @throws CmrException
   */
  @RequestMapping(value = "/metrics/statexport")
  public void generateStatistics(HttpServletRequest request, HttpServletResponse response, MetricsModel model) throws CmrException {
    ParamContainer params = new ParamContainer();
    params.addParam("model", model);
    try {
      RequestStatsContainer container = statService.process(request, params);
      if (container != null) {
        statService.exportToExcel(container, model, response);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Handles the param listing page
   * 
   * @param request
   * @param model
   * @return
   * @throws CmrException
   */
  @RequestMapping(value = "/metrics/squadexport")
  public void generateSquadStatistics(HttpServletRequest request, HttpServletResponse response, MetricsModel model) throws CmrException {
    ParamContainer params = new ParamContainer();
    params.addParam("model", model);
    try {
      RequestStatsContainer container = squadService.process(request, params);
      if (container != null) {
        statService.exportToSquadReport(container, model, response);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

}
