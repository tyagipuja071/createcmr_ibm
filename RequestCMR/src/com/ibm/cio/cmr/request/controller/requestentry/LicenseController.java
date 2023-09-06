package com.ibm.cio.cmr.request.controller.requestentry;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.controller.BaseController;
import com.ibm.cio.cmr.request.model.ProcessResultModel;
import com.ibm.cio.cmr.request.model.requestentry.LicenseModel;
import com.ibm.cio.cmr.request.service.requestentry.LicenseService;
import com.ibm.cio.cmr.request.util.MessageUtil;

@Controller
public class LicenseController extends BaseController {

  @Autowired
  private LicenseService service;

  @RequestMapping(
      value = "/request/license/list")
  public ModelMap doSearch(HttpServletRequest request, HttpServletResponse response, LicenseModel model) throws CmrException {

    List<LicenseModel> results = service.search(model, request);
    ModelMap map = new ModelMap();
    map.addAttribute("items", results);
    return map;
  }

  @RequestMapping(
      value = "/request/license/process",
      method = { RequestMethod.POST, RequestMethod.GET })
  public ModelMap processLicense(HttpServletRequest request, HttpServletResponse response, LicenseModel model) throws CmrException {
    ProcessResultModel result = new ProcessResultModel();
    service.processTransaction(model, request);
    String action = model.getAction();
    if ("ADD_LICENSE".equals(action)) {
      result.setMessage(MessageUtil.getMessage(MessageUtil.INFO_LICENSE_ADD_LIST));
    } else if ("REMOVE_LICENSE".equals(action)) {
      result.setMessage(MessageUtil.getMessage(MessageUtil.INFO_LICENSE_REMOVE_LIST));
    }
    result.setSuccess(true);
    return wrapAsProcessResult(result);
  }
}
