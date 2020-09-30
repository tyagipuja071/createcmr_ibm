/**
 * 
 */
package com.ibm.cio.cmr.request.controller.window;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.cio.cmr.request.CmrException;

/**
 * @author JeffZAMORA
 *
 */
@Controller
public class DPLSearchController extends BaseWindowController {

  @RequestMapping(value = WINDOW_URL + "/dpl/request")
  public ModelAndView showDPLSearchForRequest(HttpServletRequest request, HttpServletResponse response)
      throws CmrException, JsonGenerationException, JsonMappingException, IOException {

    ModelAndView mv = new ModelAndView("dplrequest");
    String reqId = request.getParameter("reqId");

    if (!StringUtils.isNumeric(reqId)) {
      mv.addObject("error", true);
      mv.addObject("reqId", 0);
      mv.addObject("msg", "Invalid Request ID. Request " + reqId + " does not exist.");
    } else {
      mv.addObject("reqId", Long.parseLong(reqId));
      mv.addObject("error", false);
    }

    return getWindow(mv, "DPL Search");
  }

}
