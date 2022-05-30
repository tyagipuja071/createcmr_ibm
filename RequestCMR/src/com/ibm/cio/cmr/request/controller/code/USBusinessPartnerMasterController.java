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
import com.ibm.cio.cmr.request.model.code.USBusinessPartnerMasterModel;
import com.ibm.cio.cmr.request.service.code.USBusinessPartnerMasterMaintainService;
import com.ibm.cio.cmr.request.service.code.USBusinessPartnerMasterService;
import com.ibm.cio.cmr.request.user.AppUser;
import com.ibm.cio.cmr.request.util.MessageUtil;

@Controller
public class USBusinessPartnerMasterController extends BaseController {

  @Autowired
  private USBusinessPartnerMasterService service;

  @Autowired
  private USBusinessPartnerMasterMaintainService maintainService;

  private static final Logger LOG = Logger.getLogger(USBusinessPartnerMasterController.class);

  @RequestMapping(value = "/code/us_bp_master", method = RequestMethod.GET)
  public @ResponseBody ModelAndView showUSBusinessPartnerMaster(HttpServletRequest request, ModelMap model) {
    AppUser user = AppUser.getUser(request);
    if (!user.isAdmin() && !user.isCmde()) {
      LOG.warn("User " + user.getIntranetId() + " (" + user.getBluePagesName()
          + ") tried accessing the US Business Partner Master Maintenance system function.");
      ModelAndView mv = new ModelAndView("noaccess", "us_bp_master", new USBusinessPartnerMasterModel());
      return mv;
    }

    // access granted
    ModelAndView mv = new ModelAndView("usbpmaster", "us_bp_master", new USBusinessPartnerMasterModel());
    setPageKeys("ADMIN", "CODE_ADMIN", mv);

    return mv;
  }

  @RequestMapping(value = "/code/us_bp_master_list", method = { RequestMethod.POST, RequestMethod.GET })
  public ModelMap getUSBusinessPartnerMasterList(HttpServletRequest request, HttpServletResponse response, USBusinessPartnerMasterModel model)
      throws CmrException {
    List<USBusinessPartnerMasterModel> results = service.search(model, request);
    return wrapAsSearchResult(results);
  }

  @RequestMapping(value = "/code/us_bp_master_form")
  public @ResponseBody ModelAndView usBusinessPartnerMasterMaintenance(HttpServletRequest request, HttpServletResponse response,
      USBusinessPartnerMasterModel model) throws CmrException {

    AppUser user = AppUser.getUser(request);
    if (!user.isAdmin() && !user.isCmde()) {
      LOG.warn("User " + user.getIntranetId() + " (" + user.getBluePagesName()
          + ") tried accessing the US Business Partner Master Maintenance system function.");
      ModelAndView mv = new ModelAndView("noaccess", "us_bp_master", new USBusinessPartnerMasterModel());
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

          String url = "/code/us_bp_master_form?mandt=" + model.getMandt() + "&companyNo=" + String.valueOf(model.getCompanyNo());
          mv = new ModelAndView("redirect:" + url, "us_bp_master", model);

          MessageUtil.setInfoMessage(mv, MessageUtil.INFO_RECORD_SAVED, model.getRecordDescription());
        } catch (Exception e) {
          mv = new ModelAndView("usbpmasterform", "us_bp_master", model);
          setError(e, mv);
        }
      } else {
        USBusinessPartnerMasterModel currentModel = new USBusinessPartnerMasterModel();
        List<USBusinessPartnerMasterModel> current = maintainService.search(model, request);

        if (current != null && current.size() > 0) {
          currentModel = current.get(0);
        }

        mv = new ModelAndView("usbpmasterform", "us_bp_master", currentModel);
      }

    }

    if (mv == null) {
      mv = new ModelAndView("usbpmasterform", "us_bp_master", new USBusinessPartnerMasterModel());
    }

    setPageKeys("ADMIN", "CODE_ADMIN", mv);

    return mv;

  }

}