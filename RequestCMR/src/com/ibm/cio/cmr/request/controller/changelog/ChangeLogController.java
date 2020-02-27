/**
 * 
 */
package com.ibm.cio.cmr.request.controller.changelog;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.controller.BaseController;
import com.ibm.cio.cmr.request.model.changelog.ChangeLogModel;
import com.ibm.cio.cmr.request.service.changelog.ChangeLogService;

/**
 * Handles the change log pages
 * 
 * @author Eduard Bernardo
 * 
 */
@Controller
public class ChangeLogController extends BaseController {

  @Autowired
  private ChangeLogService service;

  /**
   * Handles the display of the Home page
   * 
   * @param request
   * @param response
   * @param model
   * @return
   */
  @RequestMapping(value = "/changelog", method = { RequestMethod.POST, RequestMethod.GET })
  public ModelAndView showChangeLog(HttpServletRequest request, HttpServletResponse response, ModelMap model) {
    ModelAndView mv = new ModelAndView("changelog", "changelog", new ChangeLogModel());
    setPageKeys("CHANGELOG", null, mv);
    return mv;
  }

  @RequestMapping(value = "/changeloglist", method = { RequestMethod.POST, RequestMethod.GET })
  public ModelMap getChangeLogList(HttpServletRequest request, HttpServletResponse response, ChangeLogModel model) throws CmrException {

    List<ChangeLogModel> results = service.search(model, request);
    return wrapAsSearchResult(results);
  }

}
