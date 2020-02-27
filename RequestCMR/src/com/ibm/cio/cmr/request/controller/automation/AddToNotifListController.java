package com.ibm.cio.cmr.request.controller.automation;

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
import com.ibm.cio.cmr.request.model.requestentry.NotifyListModel;
import com.ibm.cio.cmr.request.service.requestentry.NotifyListService;
import com.ibm.cio.cmr.request.user.AppUser;
import com.ibm.cio.cmr.request.util.MessageUtil;

/**
 * @author PoojaTyagi
 * 
 */
@Controller
public class AddToNotifListController extends BaseController {

  @Autowired
  private NotifyListService service;

  @RequestMapping(value = "/auto/duplicate/reqcheck/notifList", method = { RequestMethod.POST, RequestMethod.GET })
  public ModelMap processNotifyAjaxAutomation(HttpServletRequest request, HttpServletResponse response, NotifyListModel model) throws CmrException {

    AppUser user = AppUser.getUser(request);
    ProcessResultModel result = new ProcessResultModel();
    try {
      model.setAction("ADD_NOTIFY");
      model.setNotifId(user.getIntranetId());
      model.setNotifNm(user.getBluePagesName());
      request.setAttribute("dupReqChkProcess", "Y");
      service.processTransaction(model, request);
      // set correct information message
      result.setMessage(MessageUtil.getMessage(MessageUtil.INFO_NOTIFY_ADD_LIST));
      AppUser.getUser(request).setPreferencesSet(true);
      result.setMessage(MessageUtil.getMessage(MessageUtil.INFO_NOTIFY_ADD_LIST));
      result.setSuccess(true);
    } catch (Exception e) {
      result.setSuccess(false);
      result.setMessage(e.getMessage());
    }

    return wrapAsProcessResult(result);
  }

}
