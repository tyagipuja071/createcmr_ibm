/**
 * 
 */
package com.ibm.cio.cmr.request.controller.code;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.controller.BaseController;
import com.ibm.cio.cmr.request.model.MQStatusModel;
import com.ibm.cio.cmr.request.model.code.FieldInfoModel;
import com.ibm.cio.cmr.request.service.code.MQStatusService;
import com.ibm.cio.cmr.request.user.AppUser;

/**
 * @author Jeffrey Zamora
 * 
 */
@Controller
public class MQStatusController extends BaseController {

  private static final Logger LOG = Logger.getLogger(MQStatusController.class);

  @Autowired
  private MQStatusService service;

  @RequestMapping(
      value = "/code/mqstatus",
      method = RequestMethod.GET)
  public @ResponseBody
  ModelAndView showQueue(HttpServletRequest request, ModelMap model) {
    AppUser user = AppUser.getUser(request);
    if (!user.isAdmin() && !user.isCmde()) {
      LOG.warn("User " + user.getIntranetId() + " (" + user.getBluePagesName() + ") tried accessing the Fields system function.");
      ModelAndView mv = new ModelAndView("noaccess", "fields", new FieldInfoModel());
      return mv;
    }

    // access granted
    ModelAndView mv = new ModelAndView("mqstatus", "mqstatus", new MQStatusModel());
    setPageKeys("ADMIN", "CODE_ADMIN", mv);
    return mv;
  }

  @RequestMapping(
      value = "/mqstatuslist",
      method = { RequestMethod.POST, RequestMethod.GET })
  public ModelMap getStatusList(HttpServletRequest request, HttpServletResponse response, MQStatusModel model) throws CmrException {

    List<MQStatusModel> results = service.search(model, request);
    return wrapAsSearchResult(results);
  }

  @RequestMapping(
      value = "/code/mqstatus/process")
  public @ResponseBody
  ModelMap processMQAction(HttpServletRequest request, HttpServletResponse response, MQStatusModel model) throws CmrException {

    ModelMap map = new ModelMap();
    try {
      service.processTransaction(model, request);
      map.addAttribute("success", true);
      map.addAttribute("msg", null);
    } catch (Exception e) {
      map.addAttribute("success", false);
      map.addAttribute("msg", e.getMessage());
    }

    return map;
  }

  @RequestMapping(
      value = "/code/mqstatus/xml")
  public void downloadXML(HttpServletRequest request, HttpServletResponse response, MQStatusModel model) throws CmrException, IOException {
    String fileName = request.getParameter("fileName");

    if (!StringUtils.isEmpty(fileName)) {
      service.processTransaction(model, request);
    }

    String xml = service.getXml();

    String type = "application/octet-stream";
    response.setContentType(type);
    response.addHeader("Content-Type", type);

    if (StringUtils.isEmpty(xml)) {
      response.addHeader("Content-Disposition", "attachment; filename=\"nodata.txt\"");
      response.getOutputStream().write("XML cannot be found.".getBytes());
    } else {
      response.addHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
      response.getOutputStream().write(xml.getBytes());
    }

  }

}
