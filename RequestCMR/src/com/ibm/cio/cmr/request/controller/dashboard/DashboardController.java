package com.ibm.cio.cmr.request.controller.dashboard;

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
import com.ibm.cio.cmr.request.model.dashboard.DashboardResult;
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
    DashboardResult result = this.service.process(request, params);
    map.addAttribute(result);
    return map;
  }

  @RequestMapping(value = "/dashboard", method = RequestMethod.GET)
  public ModelAndView openDashboard(HttpServletRequest request, HttpServletResponse response) {
    LOG.debug("Opening the dashboard..");
    ModelAndView mv = new ModelAndView("dashboard");
    return mv;
  }

}
