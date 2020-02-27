package com.ibm.cio.cmr.request.controller.requestentry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.controller.BaseController;
import com.ibm.cio.cmr.request.model.code.ValidateFiscalDataModel;

/**
 * Controller for the CMR Number for Upgrade/Down-grade
 * 
 * @author Dhananjay Yadav
 */

@Controller
public class ValidateFiscalDataController extends BaseController {

  // @Autowired
  // private FiscalCMRCheckService service;

  @RequestMapping(
      value = "/request/fiscalData",
      method = RequestMethod.GET)
  public ModelMap doSearch(HttpServletRequest request, HttpServletResponse response, ValidateFiscalDataModel model) throws CmrException {
    // List<FiscalCmrModel> results = service.search(model, request);
    ModelMap map = new ModelMap();
    map.addAttribute("fiscal", model);
    // map.addAttribute("items", results);
    return map;
  }

  // @RequestMapping(
  // value = "/request/fiscalData/process",
  // method = RequestMethod.GET)
  // public ModelMap processFiscalCMR(HttpServletRequest request,
  // HttpServletResponse response, FiscalCmrModel model) throws CmrException {
  //
  // ProcessResultModel result = new ProcessResultModel();
  // try {
  // service.processTransaction(model, request);
  //
  // String action = model.getAction();
  //
  // // set correct information message
  // if ("ADD_DELEGATE".equals(action) || "ADD_MGR".equals(action)) {
  // result.setMessage(MessageUtil.getMessage(MessageUtil.INFO_PREF_ADD_DELEGATE));
  // AppUser.getUser(request).setPreferencesSet(true);
  // } else if ("REMOVE_DELEGATE".equals(action)) {
  // result.setMessage(MessageUtil.getMessage(MessageUtil.INFO_PREF_REMOVE_DELEGATE));
  // }
  // result.setSuccess(true);
  // } catch (Exception e) {
  // result.setSuccess(false);
  // result.setMessage(e.getMessage());
  // }
  // return wrapAsProcessResult(result);
  // }

}
