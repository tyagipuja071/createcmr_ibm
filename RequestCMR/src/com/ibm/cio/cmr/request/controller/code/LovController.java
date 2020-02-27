package com.ibm.cio.cmr.request.controller.code;

import java.net.URLEncoder;
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
import com.ibm.cio.cmr.request.model.code.LovModel;
import com.ibm.cio.cmr.request.model.code.LovValuesModel;
import com.ibm.cio.cmr.request.service.code.LovMaintService;
import com.ibm.cio.cmr.request.service.code.LovService;
import com.ibm.cio.cmr.request.user.AppUser;
import com.ibm.cio.cmr.request.util.MessageUtil;

/**
 * @author Rochelle Salazar
 * 
 */
@Controller
public class LovController extends BaseController {

  @Autowired
  private LovService lovService;

  @Autowired
  private LovMaintService lovMaintService;

  private static final Logger LOG = Logger.getLogger(LovController.class);

  @RequestMapping(value = "/code/lovs", method = RequestMethod.GET)
  public @ResponseBody
  ModelAndView showLovMaint(HttpServletRequest request, ModelMap model) {
    AppUser user = AppUser.getUser(request);
    if (!user.isAdmin()) {
      LOG.warn("User " + user.getIntranetId() + " (" + user.getBluePagesName() + ") tried accessing the maintain Lov Table function.");
      ModelAndView mv = new ModelAndView("noaccess", "lovs", new LovModel());
      return mv;
    }

    // access granted
    ModelAndView mv = new ModelAndView("lovslist", "lovs", new LovModel());
    setPageKeys("ADMIN", "CODE_ADMIN", mv);
    return mv;
  }

  @RequestMapping(value = "/code/lovsmain")
  public @ResponseBody
  ModelAndView maintainlovs(HttpServletRequest request, HttpServletResponse response, LovModel model) throws CmrException {
    AppUser user = AppUser.getUser(request);
    if (!user.isAdmin()) {
      LOG.warn("User " + user.getIntranetId() + " (" + user.getBluePagesName() + ") tried accessing the maintain Lov Table function.");
      ModelAndView mv = new ModelAndView("noaccess", "lovsmain", new LovModel());
      return mv;
    }

    ModelAndView mv = null;

    if (model.allKeysAssigned()) {

      if (shouldProcess(model)) {
        try {

          LovModel newModel = lovMaintService.save(model, request);
          String fieldId = newModel.getFieldId();
          String encodedFieldID = URLEncoder.encode(fieldId, "UTF-8");
          String url = "/code/lovsmain?fieldId=" + encodedFieldID + "&cmrIssuingCntry=" + newModel.getCmrIssuingCntry() + "&cd=" + newModel.getCd();
          mv = new ModelAndView("redirect:" + url, "lovsmain", newModel);
          // mv = new ModelAndView("redirect:/code/lovs", "lovs", newModel);
          MessageUtil.setInfoMessage(mv, MessageUtil.INFO_RECORD_SAVED, model.getRecordDescription());
        } catch (Exception e) {
          mv = new ModelAndView("lovsmaintain", "lovsmain", model);
          setError(e, mv);
        }
      } else {
        LovModel currentModel = new LovModel();
        List<LovModel> current = lovMaintService.search(model, request);
        if (current != null && current.size() > 0) {
          currentModel = current.get(0);
        }
        mv = new ModelAndView("lovsmaintain", "lovsmain", currentModel);
      }
    }
    if (mv == null) {
      mv = new ModelAndView("lovsmaintain", "lovsmain", new LovModel());
    }
    setPageKeys("ADMIN", "CODE_ADMIN", mv);
    return mv;
  }

  @RequestMapping(value = "/code/lovslist", method = { RequestMethod.POST, RequestMethod.GET })
  public ModelMap getLovsList(HttpServletRequest request, HttpServletResponse response, LovModel model) throws CmrException {
    List<LovModel> results = lovService.search(model, request);
    return wrapAsSearchResult(results);
  }

  @RequestMapping(value = "/code/lovs/process", method = { RequestMethod.POST, RequestMethod.GET })
  public ModelMap processlov(HttpServletRequest request, HttpServletResponse response, LovValuesModel model) throws CmrException {
    ModelMap map = new ModelMap();
    lovService.setValuesModel(model);
    try {
      lovService.processTransaction(new LovModel(), request);
      map.put("success", true);
    } catch (Exception e) {
      map.put("success", false);
    }
    return map;
  }

}
