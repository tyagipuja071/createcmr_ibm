/**
 * 
 */
package com.ibm.cio.cmr.request.controller.workflow;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.controller.BaseController;
import com.ibm.cio.cmr.request.model.WorkflowHistoryModel;
import com.ibm.cio.cmr.request.model.workflow.RequestSearchCriteriaModel;
import com.ibm.cio.cmr.request.service.workflow.RequestSearchService;
import com.ibm.cio.cmr.request.user.AppUser;
import com.ibm.cio.cmr.request.util.SystemUtil;

/**
 * @author Rangoli Saxena
 * 
 */
@Controller
public class RequestSearchController extends BaseController {

  private static final String REQUEST_KEY = "_cmrRequestCriteria";

  @Autowired
  private RequestSearchService service;

  /**
   * Handles the display of the request search criteria page
   * 
   * @param request
   * @param response
   * @param model
   * @return
   */
  @RequestMapping(value = "/workflow/search")
  public ModelAndView showRequestSearchCriteriaPage(HttpServletRequest request, HttpServletResponse response, RequestSearchCriteriaModel model) {

    boolean clear = false;
    SimpleDateFormat dateFormat = CmrConstants.DATE_FORMAT();
    Timestamp currentTs = SystemUtil.getCurrentTimestamp();
    Date today = new Date(currentTs.getTime());
    GregorianCalendar gregorianCalendar = (GregorianCalendar) GregorianCalendar.getInstance();
    gregorianCalendar.add(GregorianCalendar.MONTH, -1);

    if (CmrConstants.YES_NO.Y.toString().equals(request.getParameter("clear"))) {
      request.getSession().removeAttribute(REQUEST_KEY);
      model = new RequestSearchCriteriaModel();
      clear = true;
    }

    RequestSearchCriteriaModel sessionModel = (RequestSearchCriteriaModel) request.getSession().getAttribute(REQUEST_KEY);

    ModelAndView mv = null;
    if (sessionModel != null && !clear) {
      // get the criteria from the session
      mv = new ModelAndView("criteria", "requestSearchCriteriaModel", sessionModel);

    } else {
      // always set to the login user's cmr country
      if (StringUtils.isEmpty(model.getCmrIssuingCountry())) {
        AppUser user = AppUser.getUser(request);
        model.setCmrIssuingCountry(user.getCmrIssuingCntry());
      }
      if (StringUtils.isEmpty(model.getCreateDateFrom())) {
        model.setCreateDateFrom(dateFormat.format(gregorianCalendar.getTime()));
      }
      if (StringUtils.isEmpty(model.getCreateDateTo())) {
        model.setCreateDateTo(dateFormat.format(today));
      }
      mv = new ModelAndView("criteria", "requestSearchCriteriaModel", model);
    }

    setPageKeys("WORKFLOW", "SEARCH_REQUESTS", mv);
    return mv;
  }

  /**
   * Handles the display of the request search result page
   * 
   * @param request
   * @param response
   * @param model
   * @return
   */

  @RequestMapping(value = "/workflow/results", method = RequestMethod.POST)
  public ModelAndView getRequestSearchResultScreen(
      @ModelAttribute("requestSearchCriteriaModel") RequestSearchCriteriaModel requestSearchCriteriaModel, HttpServletRequest request,
      HttpServletResponse response) {
    RequestSearchCriteriaModel model = (RequestSearchCriteriaModel) request.getSession().getAttribute(REQUEST_KEY);
    if (model != null) {
      request.getSession().removeAttribute(REQUEST_KEY);
    }
    request.getSession().setAttribute(REQUEST_KEY, requestSearchCriteriaModel);
    ModelAndView mv = new ModelAndView("results", "requestSearchCriteriaModel", requestSearchCriteriaModel);
    setPageKeys("WORKFLOW", "SEARCH_REQUESTS", mv);
    mv.addObject("wfhist", new WorkflowHistoryModel());
    return mv;
  }

  @RequestMapping(value = "/workflow/results", method = RequestMethod.GET)
  public ModelAndView getRequestSearchResultScreenGET(
      @ModelAttribute("requestSearchCriteriaModel") RequestSearchCriteriaModel requestSearchCriteriaModel, HttpServletRequest request,
      HttpServletResponse response) {
    RequestSearchCriteriaModel model = (RequestSearchCriteriaModel) request.getSession().getAttribute(REQUEST_KEY);
    ModelAndView mv = null;
    if (model != null) {
      mv = new ModelAndView("results", "requestSearchCriteriaModel", model);
    } else {
      mv = new ModelAndView("results", "requestSearchCriteriaModel", requestSearchCriteriaModel);
    }
    setPageKeys("WORKFLOW", "SEARCH_REQUESTS", mv);
    mv.addObject("wfhist", new WorkflowHistoryModel());
    return mv;
  }

  @RequestMapping(value = "/workflow/search/results/list")
  public ModelMap doSearch(HttpServletRequest request, HttpServletResponse response, RequestSearchCriteriaModel model) throws CmrException {
    List<RequestSearchCriteriaModel> results = service.search(model, request);
    ModelMap map = new ModelMap();
    map.addAttribute("items", results);
    return map;
  }

}
