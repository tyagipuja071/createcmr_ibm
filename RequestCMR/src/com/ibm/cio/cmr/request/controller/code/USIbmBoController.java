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
import com.ibm.cio.cmr.request.model.code.USIbmBoModel;
import com.ibm.cio.cmr.request.service.code.USIbmBoMaintainService;
import com.ibm.cio.cmr.request.service.code.USIbmBoService;
import com.ibm.cio.cmr.request.user.AppUser;
import com.ibm.cio.cmr.request.util.MessageUtil;

@Controller
public class USIbmBoController extends BaseController {

  @Autowired
  private USIbmBoService service;

  @Autowired
  private USIbmBoMaintainService maintainService;

  private static final Logger LOG = Logger.getLogger(USIbmBoController.class);

  @RequestMapping(value = "/code/us_ibm_bo", method = RequestMethod.GET)
  public @ResponseBody ModelAndView showUSIbmBo(HttpServletRequest request, ModelMap model) {
    AppUser user = AppUser.getUser(request);
    if (!user.isAdmin() && !user.isCmde()) {
      LOG.warn("User " + user.getIntranetId() + " (" + user.getBluePagesName() + ") tried accessing the US IBM BO Maintenance system function.");
      ModelAndView mv = new ModelAndView("noaccess", "us_ibm_bo", new USIbmBoModel());
      return mv;
    }

    // access granted
    ModelAndView mv = new ModelAndView("usibmbo", "us_ibm_bo", new USIbmBoModel());
    setPageKeys("ADMIN", "CODE_ADMIN", mv);

    return mv;
  }

  @RequestMapping(value = "/code/us_ibm_bo_list", method = { RequestMethod.POST, RequestMethod.GET })
  public ModelMap getUSIbmBoList(HttpServletRequest request, HttpServletResponse response, USIbmBoModel model) throws CmrException {
    List<USIbmBoModel> results = service.search(model, request);
    return wrapAsSearchResult(results);
  }

  @RequestMapping(value = "/code/us_ibm_bo_form")
  public @ResponseBody ModelAndView usIbmBoMaintenance(HttpServletRequest request, HttpServletResponse response, USIbmBoModel model)
      throws CmrException {

    AppUser user = AppUser.getUser(request);
    if (!user.isAdmin() && !user.isCmde()) {
      LOG.warn("User " + user.getIntranetId() + " (" + user.getBluePagesName() + ") tried accessing the US IBM BO Maintenance system function.");
      ModelAndView mv = new ModelAndView("noaccess", "us_ibm_bo", new USIbmBoModel());
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

          String url = "/code/us_ibm_bo_form?mandt=" + model.getMandt() + "&iOff=" + String.valueOf(model.getiOff());
          mv = new ModelAndView("redirect:" + url, "us_ibm_bo", model);

          MessageUtil.setInfoMessage(mv, MessageUtil.INFO_RECORD_SAVED, model.getRecordDescription());
        } catch (Exception e) {
          mv = new ModelAndView("usibmboform", "us_ibm_bo", model);
          setError(e, mv);
        }
      } else {
        USIbmBoModel currentModel = new USIbmBoModel();
        List<USIbmBoModel> current = maintainService.search(model, request);

        if (current != null && current.size() > 0) {
          currentModel = current.get(0);
        }

        mv = new ModelAndView("usibmboform", "us_ibm_bo", currentModel);
      }
    }

    if (mv == null) {
      mv = new ModelAndView("usibmboform", "us_ibm_bo", new USIbmBoModel());
    }

    setPageKeys("ADMIN", "CODE_ADMIN", mv);
    return mv;
  }

}
