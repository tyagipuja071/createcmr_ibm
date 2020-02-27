/**
 * 
 */
package com.ibm.cio.cmr.request.controller.requestentry;

import java.io.File;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.activation.MimetypesFileTypeMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.controller.BaseController;
import com.ibm.cio.cmr.request.model.ProcessResultModel;
import com.ibm.cio.cmr.request.model.requestentry.AttachmentModel;
import com.ibm.cio.cmr.request.service.requestentry.AttachmentService;
import com.ibm.cio.cmr.request.util.MessageUtil;

/**
 * Controller for handling request entry page functions
 * 
 * @author Rangoli Saxena
 * 
 */
@Controller
public class AttachmentController extends BaseController {

  private static final MimetypesFileTypeMap MIME_TYPES = new MimetypesFileTypeMap();
  @Autowired
  AttachmentService service;

  @RequestMapping(
      value = "/token",
      method = { RequestMethod.POST, RequestMethod.GET })
  public ModelMap checkTokenStatus(HttpServletRequest request, HttpServletResponse response) {
    ModelMap map = new ModelMap();
    String tokenId = request.getParameter("tokenId");
    if (request.getSession().getAttribute(tokenId) != null) {
      String message = (String) request.getSession().getAttribute(tokenId);
      String[] status = message.split(",");
      if (status.length >= 2) {
        map.addAttribute("ok", true);
        map.addAttribute("success", "Y".equals(status[0]));
        if (status.length == 2) {
          map.addAttribute("message", status[1]);
        } else {
          map.addAttribute("message", message.substring(message.indexOf(",") + 1));
        }
        request.getSession().removeAttribute(tokenId);
      } else {
        map.addAttribute("ok", false);
      }
    } else {
      map.addAttribute("ok", false);
    }
    return map;
  }

  @RequestMapping(
      value = "/request/attachment",
      method = { RequestMethod.POST, RequestMethod.GET })
  public ModelMap processAttachment(HttpServletRequest request, HttpServletResponse response, AttachmentModel model) throws CmrException {

    ProcessResultModel result = new ProcessResultModel();
    try {
      boolean isMultipart = ServletFileUpload.isMultipartContent(request);
      if (isMultipart) {
        // add attachment here
        service.addAttachment(request);
        result.setMessage(MessageUtil.getMessage(MessageUtil.INFO_ATTACHMENT_ADDED));
      } else {
        service.processTransaction(model, request);
        String action = model.getAction();

        if ("REMOVE_FILE".equals(action)) {
          result.setMessage(MessageUtil.getMessage(MessageUtil.INFO_REMOVE_ATTACHMENT));
        }
      }
      result.setSuccess(true);
    } catch (Exception e) {
      result.setSuccess(false);
      result.setMessage(e.getMessage());
    }

    return wrapAsProcessResult(result);
  }

  @RequestMapping(
      value = "/search/attachment")
  public ModelMap doAttachmentSearch(HttpServletRequest request, HttpServletResponse response, AttachmentModel model) throws CmrException {

    List<AttachmentModel> results = service.search(model, request);
    ModelMap map = new ModelMap();
    map.addAttribute("items", results);
    return map;
  }

  @RequestMapping(
      value = "/request/attachment/download",
      method = RequestMethod.POST)
  public void downloadAttachment(HttpServletRequest request, HttpServletResponse response, AttachmentModel model) throws Exception {

    String token = request.getParameter("tokenId");
    try {
      String docLink = model.getDocLink();

      String fileName = docLink + ".zip";

      File file = new File(fileName);
      if (!file.exists()) {
        throw new CmrException(MessageUtil.ERROR_FILE_DL_ERROR);
      }
      ZipFile zip = new ZipFile(file);
      try {
        Enumeration<?> entry = zip.entries();
        if (entry.hasMoreElements()) {
          ZipEntry document = (ZipEntry) entry.nextElement();
          InputStream is = zip.getInputStream(document);
          try {
            String dlfileName = docLink.substring(docLink.lastIndexOf("/") + 1);

            String type = "";
            try {
              type = MIME_TYPES.getContentType(docLink);
            } catch (Exception e) {
            }
            if (StringUtils.isEmpty(type)) {
              type = "application/octet-stream";
            }
            response.setContentType(type);
            response.addHeader("Content-Type", type);
            response.addHeader("Content-Disposition", "attachment; filename=\"" + dlfileName + "\"");
            IOUtils.copy(is, response.getOutputStream());
          } finally {
            is.close();
          }
        }
      } finally {
        zip.close();
      }
      request.getSession().setAttribute(token, "Y," + MessageUtil.getMessage(MessageUtil.INFO_ATTACHMENT_DOWNLOADED));
    } catch (Exception e) {
      if (e instanceof CmrException) {
        CmrException cmre = (CmrException) e;
        request.getSession().setAttribute(token, "N," + cmre.getMessage());
      } else {
        request.getSession().setAttribute(token, "N," + MessageUtil.getMessage(MessageUtil.ERROR_GENERAL));
      }
    }
  }

}
