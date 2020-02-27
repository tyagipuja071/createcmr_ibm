/**
 * 
 */
package com.ibm.cio.cmr.request.controller.system;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.controller.BaseController;
import com.ibm.cio.cmr.request.model.system.ForcedStatusChangeModel;
import com.ibm.cio.cmr.request.model.system.UserModel;
import com.ibm.cio.cmr.request.service.system.ForcedStatusChangeService;
import com.ibm.cio.cmr.request.user.AppUser;
import com.ibm.cio.cmr.request.util.MessageUtil;

/**
 * @author Jeffrey Zamora
 * 
 */
@Controller
public class ForceStatusChangeController extends BaseController {

  private static final Logger LOG = Logger.getLogger(ForceStatusChangeController.class);

  @Autowired
  private ForcedStatusChangeService service;

  @RequestMapping(value = "/statuschange")
  public ModelAndView showForcedStatusChangePage(HttpServletRequest request, HttpServletResponse response, ForcedStatusChangeModel model)
      throws CmrException {
    ModelAndView mv = new ModelAndView("statuschange", "status", model);

    AppUser user = AppUser.getUser(request);
    if (!user.isAdmin() && !user.isCmde()) {
      LOG.warn("User " + user.getIntranetId() + " (" + user.getBluePagesName() + ") tried accessing the Forced Status Change system function.");
      mv = new ModelAndView("noaccess", "users", new UserModel());
      return mv;
    }

    if (!StringUtils.isEmpty(model.getSearchReqId())) {
      model.setReqId(Long.parseLong(model.getSearchReqId()));
      List<ForcedStatusChangeModel> records = service.search(model, request);
      if (records != null && records.size() > 0) {
        mv = new ModelAndView("statuschange", "status", records.get(0));
      } else {
        mv = new ModelAndView("redirect:/statuschange", "status", new ForcedStatusChangeModel());
        MessageUtil.setErrorMessage(mv, MessageUtil.ERROR_INVALID_REQ_ID, model.getSearchReqId());
      }
    }
    setPageKeys("ADMIN", "FORCE_CHANGE", mv);
    return mv;
  }

  @RequestMapping(value = "/statuschange/process")
  public ModelAndView processForceChange(HttpServletRequest request, HttpServletResponse response, ForcedStatusChangeModel model) {
    ModelAndView mv = null;
    try {
      service.processTransaction(model, request);
      mv = new ModelAndView("redirect:/statuschange?searchReqId=" + model.getReqId(), "status", new ForcedStatusChangeModel());
      setPageKeys("ADMIN", "FORCE_CHANGE", mv);
      MessageUtil.setInfoMessage(mv, MessageUtil.INFO_FORCE_CHANGE_STATUS_OK, model.getReqId() + "");
    } catch (Exception e) {
      mv = new ModelAndView("redirect:/statuschange?searchReqId=" + model.getReqId(), "status", model);
      MessageUtil.setErrorMessage(mv, MessageUtil.ERROR_CANNOT_FORCE_CHANGE_STATUS);
    }
    return mv;
  }

}
