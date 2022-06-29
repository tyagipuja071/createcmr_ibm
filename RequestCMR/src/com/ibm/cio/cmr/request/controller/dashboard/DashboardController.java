package com.ibm.cio.cmr.request.controller.dashboard;

import java.util.ArrayList;

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
import com.ibm.cio.cmr.request.entity.dashboard.ProcessingMonitor;
import com.ibm.cio.cmr.request.model.ParamContainer;
import com.ibm.cio.cmr.request.model.dashboard.DashboardResult;
import com.ibm.cio.cmr.request.model.dashboard.ProcessingModel;
import com.ibm.cio.cmr.request.service.dashboard.DashboardService;

/**
 * 
 * @author 136786PH1
 *
 */
@Controller
public class DashboardController extends BaseController {

  private static final Logger LOG = Logger.getLogger(DashboardController.class);

  @Autowired
  private DashboardService service;

  @RequestMapping(value = "/monitor", method = RequestMethod.GET)
  public ModelMap getDashboard(HttpServletRequest request, HttpServletResponse response) throws CmrException {
    ModelMap map = new ModelMap();
    ParamContainer params = new ParamContainer();
    DashboardResult result = null;
    try {
      params.addParam("SOURCE", request.getParameter("source"));
      params.addParam("CNTRY", request.getParameter("cntry"));
      params.addParam("PROC_TYPE", request.getParameter("procType"));
      params.addParam("LIST_RECORDS", request.getParameter("listRecords"));
      result = this.service.process(request, params);
    } catch (Exception e) {
      LOG.error("Error in generating monitor results", e);
      result = new DashboardResult();
      result.setProcessing(new ProcessingModel());
      result.setProcessingRecords(new ArrayList<ProcessingMonitor>());
      result.setOverallStatus("RED");
      result.setAlertMessage("An error occured when checking system status. " + e.getMessage());
    }
    map.addAttribute(result);
    return map;
  }

  @RequestMapping(value = "/dashboard", method = RequestMethod.GET)
  public ModelAndView openDashboard(HttpServletRequest request, HttpServletResponse response) {
    LOG.debug("Opening the dashboard..");
    ModelAndView mv = new ModelAndView("dashboard");
    setPageKeys("HOME", "DASHBOARD", mv);
    return mv;
  }

}
