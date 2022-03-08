/**
 * 
 */
package com.ibm.cio.cmr.request.controller.window;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
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
public class LegacyDetailsController extends BaseWindowController {

  /**
   * 
   * @param request
   * @param response
   * @param model
   * @return
   * @throws CmrException
   * @throws IOException
   * @throws JsonMappingException
   * @throws JsonGenerationException
   */
  @RequestMapping(value = WINDOW_URL + "/legacydetails")
  public ModelAndView showCompanyDetails(HttpServletRequest request, HttpServletResponse response)
      throws CmrException, JsonGenerationException, JsonMappingException, IOException {
    String country = request.getParameter("country");
    String cmrNo = request.getParameter("cmrNo");
    String realCty = request.getParameter("realCty");
    String title = null;
    if (StringUtils.isBlank(cmrNo) && StringUtils.isBlank(country)) {
      title = "CMR No. and Country not specified.";
    } else {
      title = "Details for CMR No. " + cmrNo + " under " + realCty;
    }
    ModelAndView mv = new ModelAndView("legacydetails");
    return getWindow(mv, title);
  }
}
