/**
 * 
 */
package com.ibm.cio.cmr.request.controller.code;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
import com.ibm.cio.cmr.request.model.BaseModel;
import com.ibm.cio.cmr.request.model.requestentry.ValidationUrlModel;
import com.ibm.cio.cmr.request.service.code.ValidationUrlsAdminService;
import com.ibm.cio.cmr.request.service.code.ValidationUrlsMaintainService;
import com.ibm.cio.cmr.request.user.AppUser;
import com.ibm.cio.cmr.request.util.MessageUtil;

/**
 * @author Eduard Bernardo
 * 
 */
@Controller
public class ValidationUrlsController extends BaseController {

  @Autowired
  private ValidationUrlsMaintainService maintService;
  @Autowired
  private ValidationUrlsAdminService adminService;

  private static final Logger LOG = Logger.getLogger(ValidationUrlsController.class);

  @RequestMapping(value = "/code/validationurls", method = RequestMethod.GET)
  public @ResponseBody
  ModelAndView showValidationUrls(HttpServletRequest request, ModelMap model) {
    AppUser user = AppUser.getUser(request);
    if (!user.isAdmin() && !user.isCmde()) {
      LOG.warn("User " + user.getIntranetId() + " (" + user.getBluePagesName() + ") tried accessing the maintain Validation_Urls Table function.");
      ModelAndView mv = new ModelAndView("noaccess", "user", new ValidationUrlModel());
      return mv;
    }

    // access granted
    ModelAndView mv = new ModelAndView("validationurlslist", "validationurls", new ValidationUrlModel());
    setPageKeys("ADMIN", "CODE_ADMIN", mv);
    return mv;
  }

  @RequestMapping(value = "/code/validationurlsmain")
  public @ResponseBody
  ModelAndView maintainValidationUrl(HttpServletRequest request, HttpServletResponse response, ValidationUrlModel model) throws CmrException {
    AppUser user = AppUser.getUser(request);
    if (!user.isAdmin() && !user.isCmde()) {
      LOG.warn("User " + user.getIntranetId() + " (" + user.getBluePagesName() + ") tried accessing the maintain Validation_Urls Table function.");
      ModelAndView mv = new ModelAndView("noaccess", "validationurl", new ValidationUrlModel());
      return mv;
    }

    ModelAndView mv = null;

    if (model.allKeysAssigned()) {

      if (shouldProcess(model)) {
        try {

          ValidationUrlModel newModel = maintService.save(model, request);
          mv = new ModelAndView("redirect:/code/validationurls", "validationurls", newModel);
          if (BaseModel.ACT_DELETE.equals(model.getAction())) {
            MessageUtil.setInfoMessage(mv, MessageUtil.INFO_RECORD_DELETED, model.getRecordDescription());
          } else {
            MessageUtil.setInfoMessage(mv, MessageUtil.INFO_RECORD_SAVED, model.getRecordDescription());
          }
        } catch (Exception e) {
          mv = new ModelAndView("validationurlsmaintain", "validationurl", model);
          setError(e, mv);
        }
      } else {
        ValidationUrlModel currentModel = new ValidationUrlModel();
        List<ValidationUrlModel> current = maintService.search(model, request);
        if (current != null && current.size() > 0) {
          currentModel = current.get(0);
        }
        mv = new ModelAndView("validationurlsmaintain", "validationurl", currentModel);
      }
    }
    if (mv == null) {
      mv = new ModelAndView("validationurlsmaintain", "validationurl", new ValidationUrlModel());
    }

    setPageKeys("ADMIN", "CODE_ADMIN", mv);
    return mv;
  }

  @RequestMapping(value = "/code/validationurlslist", method = { RequestMethod.POST, RequestMethod.GET })
  public ModelMap getValidationUrls(HttpServletRequest request, HttpServletResponse response, ValidationUrlModel model) throws CmrException {

    List<ValidationUrlModel> results = adminService.search(model, request);
    return wrapAsSearchResult(results);
  }
}
