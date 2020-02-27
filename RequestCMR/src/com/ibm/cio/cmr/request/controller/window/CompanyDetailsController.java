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
public class CompanyDetailsController extends BaseWindowController {

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
  @RequestMapping(value = WINDOW_URL + "/company_details")
  public ModelAndView showCompanyDetails(HttpServletRequest request, HttpServletResponse response)
      throws CmrException, JsonGenerationException, JsonMappingException, IOException {
    String cmrNo = request.getParameter("cmrNo");
    String dunsNo = request.getParameter("dunsNo");
    String title = null;
    if (StringUtils.isBlank(cmrNo) && StringUtils.isBlank(dunsNo)) {
      title = "CMR No. and DUNS No. not specified.";
    } else {
      if (!StringUtils.isBlank(cmrNo)) {
        title = "Details for CMR No. " + cmrNo;
      } else {
        title = "Details for DUNS No. " + dunsNo;
      }
    }
    ModelAndView mv = new ModelAndView("company_details");
    return getWindow(mv, title);
  }

}
