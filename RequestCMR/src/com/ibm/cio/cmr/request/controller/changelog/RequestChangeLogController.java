/**
 * 
 */
package com.ibm.cio.cmr.request.controller.changelog;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.controller.window.BaseWindowController;
import com.ibm.cio.cmr.request.model.changelog.ChangeLogModel;

/**
 * @author Jeffrey Zamora
 * 
 */
@Controller
public class RequestChangeLogController extends BaseWindowController {

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
  @RequestMapping(value = WINDOW_URL + "/reqchangelog")
  public ModelAndView showChangeLog(@RequestParam("reqId") long reqId, HttpServletRequest request, HttpServletResponse response, ModelMap model)
      throws CmrException, JsonGenerationException, JsonMappingException, IOException {
    return getWindow(new ModelAndView("reqchangelog", "changelog", new ChangeLogModel()), "Change Log: Request (" + reqId + ")");
  }

}
