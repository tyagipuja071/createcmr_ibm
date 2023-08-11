/**
 * 
 */
package com.ibm.cio.cmr.request.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.automation.util.DummyServletRequest;
import com.ibm.cio.cmr.request.model.CompanyRecordModel;
import com.ibm.cio.cmr.request.model.ParamContainer;
import com.ibm.cio.cmr.request.model.requestentry.FindCMRResultModel;
import com.ibm.cio.cmr.request.model.requestentry.RequestEntryModel;
import com.ibm.cio.cmr.request.service.QuickSearchService;
import com.ibm.cio.cmr.request.user.AppUser;
import com.ibm.cio.cmr.request.util.CompanyFinder;
import com.ibm.cio.cmr.request.util.async.AsyncRequestCreator;
import com.ibm.cmr.services.client.dnb.DnbData;

/**
 * @author JeffZAMORA
 *
 */
@Controller
public class QuickSearchController extends BaseController {

  private static Logger LOG = Logger.getLogger(QuickSearchController.class);

  @Autowired
  private QuickSearchService service;

  @RequestMapping(value = "/quick_search", method = RequestMethod.GET)
  public @ResponseBody ModelAndView openQuickSearch(HttpServletRequest request, ModelMap model) {
    // access granted
    ModelAndView mv = new ModelAndView("quick_search", "search", new CompanyRecordModel());
    // setPageKeys("REQUEST", "REQUEST", mv);
    return mv;
  }

  @RequestMapping(value = "/quick_search/find")
  public @ResponseBody ModelMap searchCompany(HttpServletRequest request, CompanyRecordModel search) throws Exception {
    List<CompanyRecordModel> records = CompanyFinder.findCompanies(search);
    return wrapAsPlainSearchResult(records);

  }

  @RequestMapping(value = "/quick_search/details")
  public @ResponseBody ModelMap getDetails(HttpServletRequest request) throws Exception {
    ModelMap map = new ModelMap();
    String issuingCountry = request.getParameter("issuingCountry");
    String cmrNo = request.getParameter("cmrNo");
    String dunsNo = request.getParameter("dunsNo");
    if (StringUtils.isBlank(cmrNo) && StringUtils.isBlank(dunsNo)) {
      map.addAttribute("success", false);
      map.addAttribute("msg", "CMR No. or DUNS No. must be specified.");
    } else {
      Object returnObject = null;
      if (!StringUtils.isBlank(cmrNo)) {
        if (StringUtils.isBlank(issuingCountry)) {
          map.addAttribute("success", false);
          map.addAttribute("msg", "Issuing Country must be specified to get CMR details.");
        } else {
          FindCMRResultModel findCmrResult = CompanyFinder.getCMRDetails(issuingCountry, cmrNo, 2000, null, null);
          if (findCmrResult == null || findCmrResult.getItems() == null || findCmrResult.getItems().isEmpty()) {
            map.addAttribute("success", false);
            map.addAttribute("msg", "Error in retrieving CMR details. Please try again later.");
          } else {
            returnObject = findCmrResult;
            map.addAttribute("success", true);
            map.addAttribute("data", returnObject);
          }
        }
      } else {
        DnbData dnbData = CompanyFinder.getDnBDetails(dunsNo);
        if (dnbData == null) {
          map.addAttribute("success", false);
          map.addAttribute("msg", "Error in retrieving D&B details. Please try again later.");
        } else {
          returnObject = dnbData;
          map.addAttribute("success", true);
          map.addAttribute("data", returnObject);
        }
      }
    }
    return map;

  }

  @RequestMapping(value = "/quick_search/process")
  public @ResponseBody ModelMap processRequest(HttpServletRequest request, CompanyRecordModel company) throws Exception {
    ModelMap map = new ModelMap();

    ParamContainer params = new ParamContainer();
    params.addParam("model", company);
    try {
      RequestEntryModel reqEntry = service.process(request, params);
      if (reqEntry != null && reqEntry.getReqId() > 0) {
        map.addAttribute("success", true);
        map.addAttribute("model", reqEntry);
      } else {
        map.addAttribute("success", false);
        map.addAttribute("msg", "An unexpected error occured. Please try again later.");
      }
    } catch (Exception e) {
      LOG.error("An error was encountered in processing the request creation", e);
      map.addAttribute("success", false);
      map.addAttribute("msg", e.getMessage());
    }
    return map;
  }

  @RequestMapping(value = "/quick_search/async")
  public @ResponseBody ModelMap processRequestAsync(HttpServletRequest request, CompanyRecordModel company) throws Exception {
    ModelMap map = new ModelMap();

    try {
      AppUser user = AppUser.getUser(request);
      AsyncRequestCreator async = new AsyncRequestCreator(user, company.getIssuingCntry(), company.getSubRegion(), company.getCmrNo());
      Thread t = new Thread(async);
      t.start();
      map.addAttribute("success", true);
      map.addAttribute("msg", null);
    } catch (Exception e) {
      LOG.error("An error was encountered in processing the request creation", e);
      map.addAttribute("success", false);
      map.addAttribute("msg", e.getMessage());
    }
    return map;
  }

  // Changes for Update API Framework change- Start
  @RequestMapping(value = "/update", method = RequestMethod.POST, consumes = "application/json")
  public @ResponseBody ModelMap getUpdate(@RequestBody CompanyRecordModel company, @Context HttpServletRequest request) throws Exception {
    ModelMap map = new ModelMap();
    AppUser user = new AppUser();
    user.setIntranetId("CreateCMR");
    user.setBluePagesName("CreateCMR");
    user.setDefaultLineOfBusn("XCRM");
    user.setDefaultRequestRsn("OTH");
    DummyServletRequest dummyReq = new DummyServletRequest();
    if (dummyReq.getSession() != null) {
      LOG.trace("Session found for dummy req");
      dummyReq.getSession().setAttribute(CmrConstants.SESSION_APPUSER_KEY, user);
    } else {
      LOG.warn("Session not found for dummy req");
    }

    ParamContainer params = new ParamContainer();
    params.addParam("model", company);
    try {
      RequestEntryModel reqEntry = service.process(dummyReq, params);
      if (reqEntry != null && reqEntry.getReqId() > 0) {
        map.addAttribute("success", true);
        map.addAttribute("model", reqEntry.getReqId());
      } else {
        map.addAttribute("success", false);
        map.addAttribute("msg", "An unexpected error occured. Please try again later.");
      }
    } catch (Exception e) {
      LOG.error("An error was encountered in processing the request creation", e);
      map.addAttribute("success", false);
      map.addAttribute("msg", e.getMessage());
    }
    return map;
  }

}
