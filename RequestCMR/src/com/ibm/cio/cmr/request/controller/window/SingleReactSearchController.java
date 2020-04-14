/**
 * 
 */
package com.ibm.cio.cmr.request.controller.window;

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
import com.ibm.cio.cmr.request.model.ParamContainer;
import com.ibm.cio.cmr.request.model.requestentry.FindCMRRecordModel;
import com.ibm.cio.cmr.request.model.requestentry.FindCMRResultModel;
import com.ibm.cio.cmr.request.model.window.SingleReactQueryRequest;
import com.ibm.cio.cmr.request.service.window.SingleReactService;
import com.ibm.cio.cmr.request.util.MessageUtil;

/**
 * @author MukeshKumar
 * 
 */
@Controller
public class SingleReactSearchController extends BaseWindowController {

  private static final String SESSION_KEY_CRITERIA = "_single_react_crit";
  private static final String SESSION_KEY_RESULTS = "_single_react_results";
  private static final Logger LOG = Logger.getLogger(SingleReactSearchController.class);

  @Autowired
  private SingleReactService service;

  @RequestMapping(value = WINDOW_URL + "/singlereactsearch")
  public ModelAndView showSearchPage(HttpServletRequest request, HttpServletResponse response) throws Exception {
    ModelAndView mv = new ModelAndView("singlereactsearch");
    if ("Y".equals(request.getParameter("clear"))) {
      request.getSession().removeAttribute(SESSION_KEY_CRITERIA);
    }
    addCriteria(request, mv);
    return getWindow(mv, "Single Reactivation - Search");
  }

  private void addCriteria(HttpServletRequest request, ModelAndView mv) {
    SingleReactQueryRequest criteria = (SingleReactQueryRequest) request.getSession().getAttribute(SESSION_KEY_CRITERIA);
    if (criteria == null) {
      criteria = new SingleReactQueryRequest();
      String cntry = request.getParameter("cntry");
      String reqId = request.getParameter("reqId");
      criteria.setKatr6(cntry);
      criteria.setReqId(reqId);
    }
    mv.addObject("sreact", criteria);
  }

  @RequestMapping(value = WINDOW_URL + "/singlereactprocess")
  public ModelAndView searchAndRedirect(SingleReactQueryRequest criteria, HttpServletRequest request, HttpServletResponse response) throws Exception {

    storeToSession(criteria, SESSION_KEY_CRITERIA, request);
    ParamContainer params = new ParamContainer();
    params.addParam("criteria", criteria);

    try {

      FindCMRResultModel queryResponse = service.process(request, params);
      if (queryResponse == null) {
        throw new Exception("Single Reactivation response was null or is not successful.");
      }

      storeToSession(queryResponse, SESSION_KEY_RESULTS, request);
      ModelAndView mv = null;
      List<FindCMRRecordModel> records = queryResponse.getItems();
      if (records == null || records.size() == 0) {
        mv = new ModelAndView("redirect:/window/singlereactnoresults");
      } else {
        mv = new ModelAndView("redirect:/window/singlereactlist");
      }

      return mv;
    } catch (Exception e) {
      LOG.debug("Error in executing Single Reactivation search.", e);
      ModelAndView mv = new ModelAndView("redirect:/window/singlereactsearch");
      if (e instanceof CmrException) {
        CmrException cmre = (CmrException) e;
        MessageUtil.setErrorMessage(mv, cmre.getCode());
      } else {
        MessageUtil.setErrorMessage(mv, MessageUtil.SINGLE_REACT_ERROR_QUERY);
      }
      return mv;
    }
  }

  private void storeToSession(Object object, String key, HttpServletRequest request) {
    request.getSession().setAttribute(key, object);
  }

  @RequestMapping(value = WINDOW_URL + "/singlereactlist")
  public ModelAndView showSingleReactList(HttpServletRequest request, HttpServletResponse response) throws Exception {
    ModelAndView mv = new ModelAndView("singlereactlist");
    FindCMRResultModel result = (FindCMRResultModel) request.getSession().getAttribute(SESSION_KEY_RESULTS);
    mv.addObject("list", result.getItems());
    mv.addObject("cmrCountry", result.getItems().get(0).getCmrIssuedBy());
    addCriteria(request, mv);
    return getWindow(mv, "Single Reactivation - List Results");
  }

  @RequestMapping(value = "/singlereactlistget")
  public ModelMap getSingleReactyList(HttpServletRequest request, HttpServletResponse response) throws Exception {
    ModelMap map = new ModelMap();
    FindCMRResultModel result = (FindCMRResultModel) request.getSession().getAttribute(SESSION_KEY_RESULTS);
    map.addAttribute("items", result.getItems());
    return map;
  }

  @RequestMapping(value = WINDOW_URL + "/singlereactdet/{cntry}/{kunnr}")
  public ModelAndView showCompanyDetails(@PathVariable String cntry, @PathVariable String kunnr, HttpServletRequest request,
      HttpServletResponse response) throws Exception {
    ModelAndView mv = new ModelAndView("singlereactdet");
    SingleReactQueryRequest query = new SingleReactQueryRequest();
    query.setKunnr(kunnr);
    query.setKatr6(cntry);
    ParamContainer params = new ParamContainer();
    params.addParam("criteria", query);
    FindCMRResultModel queryResp = this.service.process(request, params);
    if (queryResp != null && queryResp.isSuccess() && queryResp.getItems().size() > 0) {
      mv.addObject("record", queryResp.getItems().get(0));
    } else {
      mv.addObject("record", new FindCMRRecordModel());
      mv.addObject("qRequest", query);
    }
    addCriteria(request, mv);
    return getWindow(mv, "Single Reactivation - Customer Details - " + kunnr);
  }

  @RequestMapping(value = WINDOW_URL + "/singlereactnoresults")
  public ModelAndView showNoResults(HttpServletRequest request, HttpServletResponse response) throws Exception {
    ModelAndView mv = new ModelAndView("singlereactnoresults");
    addCriteria(request, mv);
    return getWindow(mv, "Single Reactivation - No Results");
  }

}
