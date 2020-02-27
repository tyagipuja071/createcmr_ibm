/**
 * 
 */
package com.ibm.cio.cmr.request.controller.system;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.cio.cmr.request.controller.BaseController;

/**
 * Handles NPS survey feedbacks
 * 
 * @author JeffZAMORA
 * 
 */
@Controller
public class NPSController extends BaseController {

  @RequestMapping(
      value = "/nps",
      method = RequestMethod.GET)
  public @ResponseBody
  ModelAndView forwardNPS(HttpServletRequest request, ModelMap model) {
    ModelAndView mv = new ModelAndView("nps");
    return mv;
  }

}
