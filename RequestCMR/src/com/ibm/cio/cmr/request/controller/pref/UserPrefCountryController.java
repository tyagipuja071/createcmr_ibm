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
import com.ibm.cio.cmr.request.model.pref.UserPrefCountryModel;
import com.ibm.cio.cmr.request.service.pref.UserPrefCountryService;
import com.ibm.cio.cmr.request.user.AppUser;
import com.ibm.cio.cmr.request.util.MessageUtil;

/**
 * @author Jeffrey Zamora
 * 
 */
@Controller
public class UserPrefCountryController extends BaseController {

  @Autowired
  private UserPrefCountryService service;

  @RequestMapping(value = "/search/userprefctry")
  public ModelMap doSearch(HttpServletRequest request, HttpServletResponse response, UserPrefCountryModel model) throws CmrException {
    if (StringUtils.isEmpty(model.getRequesterId()) && AppUser.getUser(request) != null) {
      model.setRequesterId(AppUser.getUser(request).getIntranetId());
    }
    List<UserPrefCountryModel> results = service.search(model, request);
    ModelMap map = new ModelMap();
    map.addAttribute("items", results);
    return map;
  }

  @RequestMapping(value = "/preferences/cntry", method = { RequestMethod.POST, RequestMethod.GET })
  public ModelMap processDelegateAjax(HttpServletRequest request, HttpServletResponse response, UserPrefCountryModel model) throws CmrException {

    // always set to the login user's id
    if (StringUtils.isEmpty(model.getRequesterId())) {
      model.setRequesterId(AppUser.getUser(request).getIntranetId());
    }
    ProcessResultModel result = new ProcessResultModel();
    try {
      service.processTransaction(model, request);

      String action = model.getAction();

      // set correct information message
      if ("ADD_CNTRY".equals(action)) {
        result.setMessage(MessageUtil.getMessage(MessageUtil.INFO_PREF_CNTRY_ADDED));
      } else if ("REMOVE_CNTRY".equals(action)) {
        result.setMessage(MessageUtil.getMessage(MessageUtil.INFO_PREF_CNTRY_REMOVED));
      }
      result.setSuccess(true);
    } catch (Exception e) {
      result.setSuccess(false);
      result.setMessage(e.getMessage());
    }

    return wrapAsProcessResult(result);
  }

}
