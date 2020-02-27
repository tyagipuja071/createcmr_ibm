/**
 * 
 */
package com.ibm.cio.cmr.request.controller.window;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.xml.sax.SAXException;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.model.ParamContainer;
import com.ibm.cio.cmr.request.model.system.MQXmlModel;
import com.ibm.cio.cmr.request.service.system.MQXmlService;

/**
 * @author JeffZAMORA
 * 
 */
@Controller
public class MQXmlController extends BaseWindowController {

  @Autowired
  private MQXmlService service;

  /**
   * 
   * @param request
   * @param response
   * @param model
   * @return
   * @throws CmrException
   * @throws SAXException
   * @throws ParserConfigurationException
   * @throws IOException
   * @throws JsonMappingException
   * @throws JsonGenerationException
   */
  @RequestMapping(
      value = WINDOW_URL + "/mqxml")
  public ModelAndView showMQXmls(@RequestParam("uniqueId") final String uniqueId, HttpServletRequest request, HttpServletResponse response)
      throws Exception {

    ParamContainer params = new ParamContainer();
    params.addParam("ID", uniqueId);
    MQXmlModel model = this.service.process(request, params);
    ModelAndView mv = new ModelAndView("mqxml", "xml", model);
    return mv;
  }

}
