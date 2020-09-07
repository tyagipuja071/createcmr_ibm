/**
 * 
 */
package com.ibm.cio.cmr.request.controller.legacy;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.controller.BaseController;
import com.ibm.cio.cmr.request.model.ParamContainer;
import com.ibm.cio.cmr.request.model.legacy.LegacySearchModel;
import com.ibm.cio.cmr.request.model.legacy.LegacySearchResultModel;
import com.ibm.cio.cmr.request.service.legacy.LegacySearchService;
import com.ibm.cio.cmr.request.user.AppUser;

/**
 * Controller for Legacy Search Page
 * 
 * @author JeffZAMORA
 *
 */
@Controller
public class LegacySearchController extends BaseController {

  private static final Logger LOG = Logger.getLogger(LegacySearchController.class);

  private static final String ERROR_MSG = "You do not have authority to access this function. Make sure you were given access to this facility by the Administrator.";

  @Autowired
  private LegacySearchService service;

  /**
   * Handles the refresh page
   * 
   * @param request
   * @param model
   * @return
   */
  @RequestMapping(value = "/legacysearch", method = RequestMethod.GET)
  public @ResponseBody ModelAndView showLegacySearchPage(HttpServletRequest request) {
    LOG.debug("Showing Legacy Search page..");
    ModelMap map = new ModelMap();

    AppUser user = AppUser.getUser(request);

    if (user == null) {
      map.put("ERROR", ERROR_MSG);
    } else {
      if (!user.isAdmin() && !user.isCmde()) {
        map.put("ERROR", ERROR_MSG);
      } else {
      }
    }

    ModelAndView mv = new ModelAndView("legacysearch", "model", new LegacySearchModel());
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
  @RequestMapping(value = "/legacysearch/process", method = RequestMethod.POST)
  public @ResponseBody ModelMap showLegacySearchPage(HttpServletRequest request, LegacySearchModel model) throws CmrException {
    LOG.debug("Showing Legacy Search page..");
    ModelMap map = new ModelMap();

    ParamContainer params = new ParamContainer();
    params.addParam("crit", model);

    List<LegacySearchResultModel> results = service.process(request, params);
    map.addAttribute("results", results);
    return map;
  }

}
