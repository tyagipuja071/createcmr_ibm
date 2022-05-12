/**
 * 
 */
package com.ibm.cio.cmr.request.controller.template;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.controller.BaseController;
import com.ibm.cio.cmr.request.ui.template.Template;
import com.ibm.cio.cmr.request.ui.template.TemplateManager;
import com.ibm.cio.cmr.request.util.MessageUtil;

/**
 * @author Jeffrey Zamora
 * 
 */
@Controller
public class TemplatesController extends BaseController {

  private static Logger LOG = Logger.getLogger(TemplatesController.class);

  @RequestMapping(
      value = "/templates")
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

  @RequestMapping(
      value = "/template/download",
      method = RequestMethod.POST)
  public void downloadTemplateFile(HttpServletRequest request, HttpServletResponse response) throws Exception {
    String token = request.getParameter("dlTokenId");

    String docLink = null;
    String templateName = null;
    try {
      docLink = "Israel-checklist.docx";
      templateName = docLink;

      response.setContentType("application/octet-stream");
      response.addHeader("Content-Type", "application/octet-steam");
      response.addHeader("Content-Disposition", "attachment; filename=\"" + templateName + "\"");

      SystemConfiguration.download(response.getOutputStream(), docLink);
      request.getSession().setAttribute(token, "Y," + "Checklist template downloaded successfully.");

    } catch (Exception e) {
      if (e instanceof CmrException) {
        CmrException cmre = (CmrException) e;
        if (MessageUtil.getMessage(MessageUtil.INFO_ERROR_LOG_EMPTY).equals(cmre.getMessage())) {
          request.getSession().setAttribute(token, "Y," + cmre.getMessage());
        } else {
          request.getSession().setAttribute(token, "N," + cmre.getMessage());
        }
      } else {
        request.getSession().setAttribute(token, "N," + MessageUtil.getMessage(MessageUtil.ERROR_GENERAL));
      }
    }
  }
  
  @RequestMapping(
      value = "/revcmrs/template/download",
      method = RequestMethod.POST)
  public void downloadRevivedCMRTemplateFile(HttpServletRequest request, HttpServletResponse response) throws Exception {
    String token = request.getParameter("dlTokenId");

    String docLink = null;
    String templateName = null;
    try {
      docLink = "RevCMR-template.xlsx";
      templateName = docLink;

      response.setContentType("application/octet-stream");
      response.addHeader("Content-Type", "application/octet-steam");
      response.addHeader("Content-Disposition", "attachment; filename=\"" + templateName + "\"");

      SystemConfiguration.download(response.getOutputStream(), docLink);
      request.getSession().setAttribute(token, "Y," + "Revived CMRs template downloaded successfully.");

    } catch (Exception e) {
      if (e instanceof CmrException) {
        CmrException cmre = (CmrException) e;
        if (MessageUtil.getMessage(MessageUtil.INFO_ERROR_LOG_EMPTY).equals(cmre.getMessage())) {
          request.getSession().setAttribute(token, "Y," + cmre.getMessage());
        } else {
          request.getSession().setAttribute(token, "N," + cmre.getMessage());
        }
      } else {
        request.getSession().setAttribute(token, "N," + MessageUtil.getMessage(MessageUtil.ERROR_GENERAL));
      }
    }
  }

}
