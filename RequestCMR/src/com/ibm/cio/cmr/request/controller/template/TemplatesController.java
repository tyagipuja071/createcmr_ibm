/**
 * 
 */
package com.ibm.cio.cmr.request.controller.template;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.ibm.cio.cmr.request.controller.BaseController;
import com.ibm.cio.cmr.request.ui.template.Template;
import com.ibm.cio.cmr.request.ui.template.TemplateManager;

/**
 * @author Jeffrey Zamora
 * 
 */
@Controller
public class TemplatesController extends BaseController {

  @RequestMapping(value = "/templates")
  public ModelMap retrieveServiceValues(@RequestParam("cmrIssuingCntry") String cmrIssuingCntry, HttpServletRequest request,
      HttpServletResponse response, ModelMap inputMap) throws Exception {
    ModelMap responseMap = new ModelMap();
    try {
      Template template = TemplateManager.getTemplate(cmrIssuingCntry, request);
      if (template == null) {
        responseMap.put("success", false);
      } else {
        responseMap.put("success", true);
        responseMap.put("template", template);
      }
    } catch (Exception e) {
      responseMap.put("success", false);
    }
    return responseMap;
  }

}
