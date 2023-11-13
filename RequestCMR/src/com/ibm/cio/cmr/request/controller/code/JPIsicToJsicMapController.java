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
import com.ibm.cio.cmr.request.model.code.JpIsicToJsicMapModel;
import com.ibm.cio.cmr.request.service.code.JPIsicToJsicMapMaintainService;
import com.ibm.cio.cmr.request.service.code.JPIsicToJsicMapService;
import com.ibm.cio.cmr.request.user.AppUser;
import com.ibm.cio.cmr.request.util.MessageUtil;

/**
 * 
 * @author XiangBin Liu
 *
 */

@Controller
public class JPIsicToJsicMapController extends BaseController {

  private static final Logger LOG = Logger.getLogger(JPIsicToJsicMapController.class);

  @Autowired
  private JPIsicToJsicMapService service;

  @Autowired
  private JPIsicToJsicMapMaintainService maintainService;

  @RequestMapping(value = "/code/jpisictojsicmap", method = RequestMethod.GET)
  public @ResponseBody ModelAndView showjpisictojsicmap(HttpServletRequest request, ModelMap model) {
    AppUser user = AppUser.getUser(request);
    if (!user.isAdmin() && !user.isCmde()) {
      LOG.warn(
          "User " + user.getIntranetId() + " (" + user.getBluePagesName() + ") tried accessing the JP JSIC Code Map Maintenance system function.");
      ModelAndView mv = new ModelAndView("noaccess", "jpisictojsicmap", new JpIsicToJsicMapModel());
      return mv;
    }

    // access granted
    ModelAndView mv = new ModelAndView("jpisictojsicmap", "jpisictojsicmap", new JpIsicToJsicMapModel());
    setPageKeys("ADMIN", "CODE_ADMIN", mv);
    return mv;

  }

  @RequestMapping(value = "/code/jp_isic_to_jsic_map_list", method = { RequestMethod.GET, RequestMethod.POST })
  public ModelMap getjpisictojsicmapList(HttpServletRequest request, HttpServletResponse response, JpIsicToJsicMapModel model) throws CmrException {

    List<JpIsicToJsicMapModel> results = service.search(model, request);
    return wrapAsSearchResult(results);

  }

  @RequestMapping(value = "/code/jpisictojsicmapform", method = { RequestMethod.GET, RequestMethod.POST })
  public ModelAndView jpisictojsicmapMaintenance(HttpServletRequest request, HttpServletResponse response, JpIsicToJsicMapModel model)
      throws CmrException {

    AppUser user = AppUser.getUser(request);
    if (!user.isAdmin() && !user.isCmde()) {
      LOG.warn(
          "User " + user.getIntranetId() + " (" + user.getBluePagesName() + ") tried accessing the JP JSIC CODE MAP Maintenance system function.");
      ModelAndView mv = new ModelAndView("noaccess", "jpisictojsicmapform", new JpIsicToJsicMapModel());
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

          String url = "/code/jpisictojsicmapform?jsicCd=" + model.getJsicCd();
          url += "&mandt=" + String.valueOf(model.getMandt());
          mv = new ModelAndView("redirect:" + url, "jpisictojsicmapform", model);

          MessageUtil.setInfoMessage(mv, MessageUtil.INFO_RECORD_SAVED, model.getRecordDescription());
        } catch (Exception e) {
          mv = new ModelAndView("jpisictojsicmapform", "jpisictojsicmapform", model);
          setError(e, mv);
        }
      } else {
        JpIsicToJsicMapModel jpIsicToJsicMapModel = new JpIsicToJsicMapModel();
        List<JpIsicToJsicMapModel> current = maintainService.search(model, request);

        if (current != null && current.size() > 0) {
          jpIsicToJsicMapModel = current.get(0);
        }

        mv = new ModelAndView("jpisictojsicmapform", "jpisictojsicmapform", jpIsicToJsicMapModel);
      }
    }

    if (mv == null) {
      mv = new ModelAndView("jpisictojsicmapform", "jpisictojsicmapform", new JpIsicToJsicMapModel());
    }

    setPageKeys("ADMIN", "CODE_ADMIN", mv);
    return mv;

  }

  @RequestMapping(value = "/code/jpisictojsicmap/delete", method = { RequestMethod.POST, RequestMethod.GET })
  public ModelAndView deleteJPIsicToJsicMap(HttpServletRequest request, HttpServletResponse response, JpIsicToJsicMapModel model)
      throws CmrException {

    model.setAction(BaseModel.ACT_DELETE);
    model.setState(BaseModel.STATE_EXISTING);

    AppUser user = AppUser.getUser(request);
    if (!user.isAdmin() && !user.isCmde()) {
      LOG.warn("User " + user.getIntranetId() + " (" + user.getBluePagesName() + ") tried accessing the JP Isic To Jsic Map delete function.");
      ModelAndView mv = new ModelAndView("noaccess", "jpisictojsicmap", new JpIsicToJsicMapModel());
      return mv;
    }

    ModelAndView mv = null;
    if (model.allKeysAssigned()) {
      if (shouldProcess(model)) {
        try {
          maintainService.save(model, request);
          mv = new ModelAndView("jpisictojsicmap", "jpisictojsicmap", new JpIsicToJsicMapModel());
          MessageUtil.setInfoMessage(mv, MessageUtil.INFO_RECORD_DELETED, model.getRecordDescription());
        } catch (Exception e) {
          mv = new ModelAndView("jpisictojsicmap", "jpisictojsicmap", new JpIsicToJsicMapModel());
          setError(e, mv);
        }
      }
    }

    setPageKeys("ADMIN", "CODE_ADMIN", mv);
    return mv;
  }

}
