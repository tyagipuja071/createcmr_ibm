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
import com.ibm.cio.cmr.request.model.code.USEnterpriseModel;
import com.ibm.cio.cmr.request.service.code.USEnterpriseMaintainService;
import com.ibm.cio.cmr.request.service.code.USEnterpriseService;
import com.ibm.cio.cmr.request.user.AppUser;
import com.ibm.cio.cmr.request.util.MessageUtil;

/**
 * 
 * @author Bikash Das
 *
 */

@Controller
public class USEnterpriseController extends BaseController {

  private static final Logger LOG = Logger.getLogger(USEnterpriseController.class);

  @Autowired
  private USEnterpriseService service;

  @Autowired
  private USEnterpriseMaintainService maintainService;

  @RequestMapping(
      value = "/code/us_enterprise",
      method = RequestMethod.GET)
  public @ResponseBody ModelAndView showUSEnerprise(HttpServletRequest request, ModelMap model) {
    AppUser user = AppUser.getUser(request);
    if (!user.isAdmin() && !user.isCmde()) {
      LOG.warn("User " + user.getIntranetId() + " (" + user.getBluePagesName() + ") tried accessing the US ENTERPRISE Maintenance system function.");
      ModelAndView mv = new ModelAndView("noaccess", "us_enterprise", new USEnterpriseModel());
      return mv;
    }

    // access granted
    ModelAndView mv = new ModelAndView("usenterprise", "us_enterprise", new USEnterpriseModel());
    setPageKeys("ADMIN", "CODE_ADMIN", mv);
    return mv;

  }

  @RequestMapping(
      value = "/code/us_enterprise_list",
      method = { RequestMethod.GET, RequestMethod.POST })
  public ModelMap getUSEnterpriseList(HttpServletRequest request, HttpServletResponse response, USEnterpriseModel model) throws CmrException {

    List<USEnterpriseModel> results = service.search(model, request);
    return wrapAsSearchResult(results);

  }

  @RequestMapping(
      value = "/code/us_enterprise_form",
      method = { RequestMethod.GET, RequestMethod.POST })
  public ModelAndView usEnterpriseMaintenance(HttpServletRequest request, HttpServletResponse response, USEnterpriseModel model) throws CmrException {

    AppUser user = AppUser.getUser(request);
    if (!user.isAdmin() && !user.isCmde()) {
      LOG.warn("User " + user.getIntranetId() + " (" + user.getBluePagesName() + ") tried accessing the US ENTERPRISE Maintenance system function.");
      ModelAndView mv = new ModelAndView("noaccess", "us_enterprise", new USEnterpriseModel());
      return mv;
    }

    ModelAndView mv = null;

    if ("I".equals(model.getAction())) {
      model.setAction(BaseModel.ACT_INSERT);
      model.setState(BaseModel.STATE_NEW);
    }

    if ("U".equals(model.getAction())) {
      model.setAction(BaseModel.ACT_UPDATE);
      model.setState(BaseModel.STATE_EXISTING);
    }

    if (model.allKeysAssigned()) {

      if (shouldProcess(model)) {
        try {
          maintainService.save(model, request);

          String url = "/code/us_enterprise_form?mandt=" + model.getMandt() + "&entNo=" + String.valueOf(model.getEntNo());
          mv = new ModelAndView("redirect:" + url, "us_company", model);

          MessageUtil.setInfoMessage(mv, MessageUtil.INFO_RECORD_SAVED, model.getRecordDescription());
        } catch (Exception e) {
          mv = new ModelAndView("usenterpriseform", "us_enterprise", model);
          setError(e, mv);
        }
      } else {
        USEnterpriseModel uSEnterpriseModel = new USEnterpriseModel();
        List<USEnterpriseModel> current = maintainService.search(model, request);

        if (current != null && current.size() > 0) {
          uSEnterpriseModel = current.get(0);
        }

        mv = new ModelAndView("usenterpriseform", "us_enterprise", uSEnterpriseModel);
      }
    }

    if (mv == null) {
      mv = new ModelAndView("usenterpriseform", "us_enterprise", new USEnterpriseModel());
    }

    setPageKeys("ADMIN", "CODE_ADMIN", mv);
    return mv;

  }

}
