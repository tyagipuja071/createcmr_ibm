/**
 * 
 */
package com.ibm.cio.cmr.request.controller;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.user.AppUser;

/**
 * @author Jeffrey Zamora
 * 
 */
@Controller
public class SessionExpiryController {

  @RequestMapping(
      value = "/sessioncheck")
  public ModelMap getRecord(HttpServletRequest request) {
    ModelMap map = new ModelMap();
    try {
      AppUser user = AppUser.getUser(request);
      if (user == null) {
        map.addAttribute("check", false);
      } else {
        map.addAttribute("check", true);
      }
    } catch (Exception e) {
      map.addAttribute("check", false);
    }
    return map;
  }

  @RequestMapping(
      value = "/currenterror")
  public ModelMap getCurrentError(HttpServletRequest request) {
    ModelMap map = new ModelMap();
    try {
      Exception e = (Exception) request.getSession().getAttribute(CmrConstants.SESSION_ERROR_KEY);
      List<String> errors = new ArrayList<String>();

      Throwable t = e;
      boolean addCause = false;
      while (t != null) {
        errors.add((addCause ? "Caused by: " : "") + t.getClass().getName() + " : " + t.getMessage());
        StackTraceElement[] trace = t.getStackTrace();
        if (trace != null && trace.length > 0) {
          errors.add(trace[0].toString());
        }
        t = t.getCause();
        addCause = true;
      }
      map.addAttribute("errors", errors);
      map.addAttribute("success", true);
    } catch (Exception e) {
      map.addAttribute("success", false);
    }
    return map;
  }

}
