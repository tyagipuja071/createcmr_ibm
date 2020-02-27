/**
 * 
 */
package com.ibm.cio.cmr.request.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.cio.cmr.request.user.AppUser;

/**
 * Controller for the error page
 * 
 * @author Jeffrey Zamora
 * 
 */
@Controller
public class ErrorController extends BaseController {

  private static final Logger LOG = Logger.getLogger(ErrorController.class);

  /**
   * Shows the error page
   * 
   * @param request
   * @param response
   * @param model
   * @return
   */
  @RequestMapping(value = "/error")
  public ModelAndView showErrorPage(HttpServletRequest request, HttpServletResponse response, ModelMap model) {
    return new ModelAndView("error", "page", model);
  }

  @RequestMapping(value = "/{url:^(?!.*resources).*$}")
  public String handleNonMapped(HttpServletRequest request, HttpServletResponse response, ModelMap model) {
    String requestUrl = request.getRequestURI().toString();
    AppUser user = AppUser.getUser(request);
    if (user != null) {
      LOG.error("URL: " + requestUrl + " accessed by " + user.getIntranetId());
    } else {
      LOG.error("URL: " + requestUrl + " accessed by anonymous user");
    }
    return "error";
  }

  @RequestMapping(value = "/{url:^(?!.*resources).*$}/**")
  public String handleNonMapped2ndLevel(HttpServletRequest request, HttpServletResponse response, ModelMap model) {
    String requestUrl = request.getRequestURI().toString();
    AppUser user = AppUser.getUser(request);
    if (user != null) {
      LOG.error("URL: " + requestUrl + " accessed by " + user.getIntranetId());
    } else {
      LOG.error("URL: " + requestUrl + " accessed by anonymous user");
    }
    return "error";
  }

}
