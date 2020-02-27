/**
 * 
 */
package com.ibm.cio.cmr.request.controller.pref;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.controller.BaseController;
import com.ibm.cio.cmr.request.model.ProcessResultModel;
import com.ibm.cio.cmr.request.model.pref.DelegateModel;
import com.ibm.cio.cmr.request.service.pref.DelegateService;
import com.ibm.cio.cmr.request.user.AppUser;
import com.ibm.cio.cmr.request.util.MessageUtil;

/**
 * Controller for searching delegates
 * 
 * @author Jeffrey Zamora
 * 
 */
@Controller
public class DelegateController extends BaseController {

  @Autowired
  private DelegateService service;

  @RequestMapping(value = "/search/delegate")
  public ModelMap doSearch(HttpServletRequest request, HttpServletResponse response, DelegateModel model) throws CmrException {
    if (StringUtils.isEmpty(model.getUserId()) && AppUser.getUser(request) != null) {
      model.setUserId(AppUser.getUser(request).getIntranetId());
    }
    List<DelegateModel> results = service.search(model, request);
    ModelMap map = new ModelMap();
    map.addAttribute("items", results);
    return map;
  }

  @RequestMapping(value = "/preferences/delegate", method = { RequestMethod.POST, RequestMethod.GET })
  public ModelMap processDelegateAjax(HttpServletRequest request, HttpServletResponse response, DelegateModel model) throws CmrException {

    // always set to the login user's id
    if (StringUtils.isEmpty(model.getUserId())) {
      model.setUserId(AppUser.getUser(request).getIntranetId());
    }
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
