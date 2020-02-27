/**
 * 
 */
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
import com.ibm.cio.cmr.request.model.requestentry.NotifyListModel;
import com.ibm.cio.cmr.request.service.requestentry.NotifyListService;
import com.ibm.cio.cmr.request.user.AppUser;
import com.ibm.cio.cmr.request.util.MessageUtil;

/**
 * @author Jeffrey Zamora
 * 
 */
@Controller
public class NotifyListController extends BaseController {

  @Autowired
  private NotifyListService service;

  @RequestMapping(value = "/request/notify/list")
  public ModelMap doSearch(HttpServletRequest request, HttpServletResponse response, NotifyListModel model) throws CmrException {
    List<NotifyListModel> results = service.search(model, request);
    return wrapAsSearchResult(results);
  }

  @RequestMapping(value = "/request/notify/process", method = { RequestMethod.POST, RequestMethod.GET })
  public ModelMap processNotifyAjax(HttpServletRequest request, HttpServletResponse response, NotifyListModel model) throws CmrException {

    ProcessResultModel result = new ProcessResultModel();
    try {
      service.processTransaction(model, request);

      String action = model.getAction();

      // set correct information message
      if ("ADD_NOTIFY".equals(action)) {
        result.setMessage(MessageUtil.getMessage(MessageUtil.INFO_NOTIFY_ADD_LIST));
        AppUser.getUser(request).setPreferencesSet(true);
      } else if ("REMOVE_NOTIF".equals(action)) {
        result.setMessage(MessageUtil.getMessage(MessageUtil.INFO_NOTIFY_REMOVE_LIST));
      }
      result.setSuccess(true);
    } catch (Exception e) {
      result.setSuccess(false);
      result.setMessage(e.getMessage());
    }

    return wrapAsProcessResult(result);
  }

}
