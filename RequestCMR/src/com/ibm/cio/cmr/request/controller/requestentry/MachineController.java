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
import com.ibm.cio.cmr.request.model.requestentry.MachineModel;
import com.ibm.cio.cmr.request.service.requestentry.MachineService;
import com.ibm.cio.cmr.request.user.AppUser;
import com.ibm.cio.cmr.request.util.MessageUtil;

/**
 * Controller for searching Machine Type and Serial Number
 * 
 * @author Rangoli Saxena
 * 
 */
@Controller
public class MachineController extends BaseController {

  @Autowired
  private MachineService service;

  @RequestMapping(
      value = "/request/address/machines/list/install")
  public ModelMap doSearch(HttpServletRequest request, HttpServletResponse response, MachineModel model) throws CmrException {

    List<MachineModel> results = service.search(model, request);
    ModelMap map = new ModelMap();
    map.addAttribute("items", results);
    return map;
  }

  @RequestMapping(
      value = "/request/address/machines/process",
      method = { RequestMethod.POST, RequestMethod.GET })
  public ModelMap processMachines(HttpServletRequest request, HttpServletResponse response, MachineModel model) throws CmrException {

    ProcessResultModel result = new ProcessResultModel();
    try {
      service.processTransaction(model, request);

      String action = model.getAction();

      // set correct information message
      if ("ADD_DELEGATE".equals(action) || "ADD_MGR".equals(action)) {
        result.setMessage(MessageUtil.getMessage(MessageUtil.INFO_PREF_ADD_DELEGATE));
        AppUser.getUser(request).setPreferencesSet(true);
      } else if ("REMOVE_DELEGATE".equals(action)) {
        result.setMessage(MessageUtil.getMessage(MessageUtil.INFO_PREF_REMOVE_DELEGATE));
      }
      result.setSuccess(true);
    } catch (Exception e) {
      result.setSuccess(false);
      result.setMessage(e.getMessage());
    }

    return wrapAsProcessResult(result);
  }

}