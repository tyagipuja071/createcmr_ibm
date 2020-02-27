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
import com.ibm.cio.cmr.request.model.code.SuppCountryModel;
import com.ibm.cio.cmr.request.service.code.SuppCountryAdminService;
import com.ibm.cio.cmr.request.service.code.SuppCountryMaintainService;
import com.ibm.cio.cmr.request.user.AppUser;
import com.ibm.cio.cmr.request.util.MessageUtil;

/**
 * @author Jose Belgira
 * 
 * 
 */

@Controller
public class SuppCountryController extends BaseController {
  private static final Logger LOG = Logger.getLogger(SuppCountryController.class);
  @Autowired
  private SuppCountryMaintainService suppCountryMaintainService;
  @Autowired
  private SuppCountryAdminService suppCountryAdminService;

  @RequestMapping(
      value = "/code/suppcountry",
      method = RequestMethod.GET)
  public @ResponseBody ModelAndView showCountryListing(HttpServletRequest request, ModelMap model) {
    AppUser user = AppUser.getUser(request);
    if (!user.isAdmin() && !user.isCmde()) {
      LOG.warn("User " + user.getIntranetId() + " (" + user.getBluePagesName() + ") tried accessing the maintain Validation_Urls Table function.");
      ModelAndView mv = new ModelAndView("noaccess", "user", new SuppCountryModel());
      return mv;
    }

    // access granted
    ModelAndView mv = new ModelAndView("suppcountrylist", "suppcountrymodel", new SuppCountryModel());
    setPageKeys("ADMIN", "CODE_ADMIN", mv);
    return mv;
  }

  @RequestMapping(
      value = "/code/addsuppcountrypage")
  public @ResponseBody ModelAndView maintainSuppCountry(HttpServletRequest request, HttpServletResponse response, SuppCountryModel model)
      throws CmrException {
    AppUser user = AppUser.getUser(request);
    if (!user.isAdmin() && !user.isCmde()) {
      LOG.warn("User " + user.getIntranetId() + " (" + user.getBluePagesName() + ") tried accessing the maintain SUPP_CNTRY Table function.");
      ModelAndView mv = new ModelAndView("noaccess", "suppcountrymodel", new SuppCountryModel());
      return mv;
    }

    ModelAndView mv = null;

    if (model.allKeysAssigned()) {

      if (shouldProcess(model)) {
        try {
          if (model.isAutoProcCheckBox()) {
            model.setAutoProcEnabled("Y");
          } else {
            model.setAutoProcEnabled("N");
          }

          SuppCountryModel newModel = suppCountryMaintainService.save(model, request);
          mv = new ModelAndView("redirect:/code/suppcountry", "suppcountrymodel", newModel);
          MessageUtil.setInfoMessage(mv, MessageUtil.INFO_RECORD_SAVED, model.getRecordDescription());
        } catch (Exception e) {
          mv = new ModelAndView("addsuppcountrypage", "suppcountrymodel", model);
          setError(e, mv);
        }
      } else {
        SuppCountryModel currentModel = new SuppCountryModel();
        List<SuppCountryModel> current = suppCountryMaintainService.search(model, request);
        if (current != null && current.size() > 0) {
          currentModel = current.get(0);
        }
        if (currentModel.getAutoProcEnabled().equalsIgnoreCase("Y")) {
          currentModel.setAutoProcCheckBox(true);
        } else {
          currentModel.setAutoProcCheckBox(false);
        }
        mv = new ModelAndView("addsuppcountrypage", "suppcountrymodel", currentModel);
      }
    }
    if (mv == null) {
      mv = new ModelAndView("addsuppcountrypage", "suppcountrymodel", new SuppCountryModel());
    }

    setPageKeys("ADMIN", "CODE_ADMIN", mv);
    return mv;
  }

  @RequestMapping(
      value = "/code/suppcountrylisting",
      method = { RequestMethod.POST, RequestMethod.GET })
  public ModelMap getSuppCountryListing(HttpServletRequest request, HttpServletResponse response, SuppCountryModel model) throws CmrException {

    List<SuppCountryModel> results = suppCountryAdminService.search(model, request);
    return wrapAsSearchResult(results);
  }

}
