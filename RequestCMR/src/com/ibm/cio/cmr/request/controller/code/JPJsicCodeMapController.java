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
import com.ibm.cio.cmr.request.model.code.JpJsicCodeMapModel;
import com.ibm.cio.cmr.request.service.code.JPJsicCodeMapMaintainService;
import com.ibm.cio.cmr.request.service.code.JPJsicCodeMapService;
import com.ibm.cio.cmr.request.user.AppUser;
import com.ibm.cio.cmr.request.util.MessageUtil;

/**
 * 
 * @author XiangBin Liu
 *
 */

@Controller
public class JPJsicCodeMapController extends BaseController {

  private static final Logger LOG = Logger.getLogger(JPJsicCodeMapController.class);

  @Autowired
  private JPJsicCodeMapService service;

  @Autowired
  private JPJsicCodeMapMaintainService maintainService;

  @RequestMapping(value = "/code/jpjsiccodemap", method = RequestMethod.GET)
  public @ResponseBody ModelAndView showJPJsicCodeMap(HttpServletRequest request, ModelMap model) {
    AppUser user = AppUser.getUser(request);
    if (!user.isAdmin() && !user.isCmde()) {
      LOG.warn(
          "User " + user.getIntranetId() + " (" + user.getBluePagesName() + ") tried accessing the JP JSIC Code Map Maintenance system function.");
      ModelAndView mv = new ModelAndView("noaccess", "jpjsiccodemap", new JpJsicCodeMapModel());
      return mv;
    }

    // access granted
    ModelAndView mv = new ModelAndView("jpjsiccodemap", "jpjsiccodemap", new JpJsicCodeMapModel());
    setPageKeys("ADMIN", "CODE_ADMIN", mv);
    return mv;

  }

  @RequestMapping(value = "/code/jp_jsic_code_map_list", method = { RequestMethod.GET, RequestMethod.POST })
  public ModelMap getJPJsicCodeMapList(HttpServletRequest request, HttpServletResponse response, JpJsicCodeMapModel model) throws CmrException {

    List<JpJsicCodeMapModel> results = service.search(model, request);
    return wrapAsSearchResult(results);

  }

  @RequestMapping(value = "/code/jpjsiccodemapform", method = { RequestMethod.GET, RequestMethod.POST })
  public ModelAndView jpJsicCodeMapMaintenance(HttpServletRequest request, HttpServletResponse response, JpJsicCodeMapModel model)
      throws CmrException {

    AppUser user = AppUser.getUser(request);
    if (!user.isAdmin() && !user.isCmde()) {
      LOG.warn(
          "User " + user.getIntranetId() + " (" + user.getBluePagesName() + ") tried accessing the JP JSIC CODE MAP Maintenance system function.");
      ModelAndView mv = new ModelAndView("noaccess", "jpjsiccodemapform", new JpJsicCodeMapModel());
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

          String url = "/code/jpjsiccodemapform?jsicCd=" + model.getJsicCd();
          url += "&subIndustryCd=" + String.valueOf(model.getSubIndustryCd());
          url += "&isuCd=" + String.valueOf(model.getIsuCd());
          url += "&isicCd=" + String.valueOf(model.getIsicCd());
          url += "&dept=" + String.valueOf(model.getDept());
          mv = new ModelAndView("redirect:" + url, "jpjsiccodemapform", model);

          MessageUtil.setInfoMessage(mv, MessageUtil.INFO_RECORD_SAVED, model.getRecordDescription());
        } catch (Exception e) {
          mv = new ModelAndView("jpjsiccodemapform", "jpjsiccodemapform", model);
          setError(e, mv);
        }
      } else {
        JpJsicCodeMapModel JpJsicCodeMapModel = new JpJsicCodeMapModel();
        List<JpJsicCodeMapModel> current = maintainService.search(model, request);

        if (current != null && current.size() > 0) {
          JpJsicCodeMapModel = current.get(0);
        }

        mv = new ModelAndView("jpjsiccodemapform", "jpjsiccodemapform", JpJsicCodeMapModel);
      }
    }

    if (mv == null) {
      mv = new ModelAndView("jpjsiccodemapform", "jpjsiccodemapform", new JpJsicCodeMapModel());
    }

    setPageKeys("ADMIN", "CODE_ADMIN", mv);
    return mv;

  }

  @RequestMapping(value = "/code/jpjsiccodemap/delete", method = { RequestMethod.POST, RequestMethod.GET })
  public ModelAndView deleteJPJsicCodeMap(HttpServletRequest request, HttpServletResponse response, JpJsicCodeMapModel model) throws CmrException {

    model.setAction(BaseModel.ACT_DELETE);
    model.setState(BaseModel.STATE_EXISTING);

    AppUser user = AppUser.getUser(request);
    if (!user.isAdmin() && !user.isCmde()) {
      LOG.warn("User " + user.getIntranetId() + " (" + user.getBluePagesName() + ") tried accessing the JP Jsic Code Map delete function.");
      ModelAndView mv = new ModelAndView("noaccess", "jpjsiccodemap", new JpJsicCodeMapModel());
      return mv;
    }

    ModelAndView mv = null;
    if (model.allKeysAssigned()) {
      if (shouldProcess(model)) {
        try {
          maintainService.save(model, request);
          mv = new ModelAndView("jpjsiccodemap", "jpjsiccodemap", new JpJsicCodeMapModel());
          MessageUtil.setInfoMessage(mv, MessageUtil.INFO_RECORD_DELETED, model.getRecordDescription());
        } catch (Exception e) {
          mv = new ModelAndView("jpjsiccodemap", "jpjsiccodemap", new JpJsicCodeMapModel());
          setError(e, mv);
        }
      }
    }

    setPageKeys("ADMIN", "CODE_ADMIN", mv);
    return mv;
  }

}
