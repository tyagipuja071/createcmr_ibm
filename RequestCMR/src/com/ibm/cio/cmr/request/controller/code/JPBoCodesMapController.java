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
import com.ibm.cio.cmr.request.model.code.JpBoCodesMapModel;
import com.ibm.cio.cmr.request.service.code.JPBoCodesMapMaintainService;
import com.ibm.cio.cmr.request.service.code.JPBoCodesMapService;
import com.ibm.cio.cmr.request.user.AppUser;
import com.ibm.cio.cmr.request.util.MessageUtil;

/**
 * 
 * @author XiangBin Liu
 *
 */

@Controller
public class JPBoCodesMapController extends BaseController {

  private static final Logger LOG = Logger.getLogger(JPBoCodesMapController.class);

  @Autowired
  private JPBoCodesMapService service;

  @Autowired
  private JPBoCodesMapMaintainService maintainService;

  @RequestMapping(value = "/code/jpbocodesmap", method = RequestMethod.GET)
  public @ResponseBody ModelAndView showJPJsicCodeMap(HttpServletRequest request, ModelMap model) {
    AppUser user = AppUser.getUser(request);
    if (!user.isAdmin() && !user.isCmde()) {
      LOG.warn(
          "User " + user.getIntranetId() + " (" + user.getBluePagesName() + ") tried accessing the JP JSIC Code Map Maintenance system function.");
      ModelAndView mv = new ModelAndView("noaccess", "jpbocodesmap", new JpBoCodesMapModel());
      return mv;
    }

    // access granted
    ModelAndView mv = new ModelAndView("jpbocodesmap", "jpbocodesmap", new JpBoCodesMapModel());
    setPageKeys("ADMIN", "CODE_ADMIN", mv);
    return mv;

  }

  @RequestMapping(value = "/code/jp_bo_codes_map_list", method = { RequestMethod.GET, RequestMethod.POST })
  public ModelMap getJPBoCodesMapList(HttpServletRequest request, HttpServletResponse response, JpBoCodesMapModel model) throws CmrException {

    List<JpBoCodesMapModel> results = service.search(model, request);
    return wrapAsSearchResult(results);

  }

  @RequestMapping(value = "/code/jpbocodesmapform", method = { RequestMethod.GET, RequestMethod.POST })
  public ModelAndView jpBoCodesMapMaintenance(HttpServletRequest request, HttpServletResponse response, JpBoCodesMapModel model) throws CmrException {

    AppUser user = AppUser.getUser(request);
    if (!user.isAdmin() && !user.isCmde()) {
      LOG.warn(
          "User " + user.getIntranetId() + " (" + user.getBluePagesName() + ") tried accessing the JP JSIC CODE MAP Maintenance system function.");
      ModelAndView mv = new ModelAndView("noaccess", "jpbocodesmapform", new JpBoCodesMapModel());
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

          String url = "/code/jpbocodesmapform?subsidiaryCd=" + model.getSubsidiaryCd();
          url += "&officeCd=" + String.valueOf(model.getOfficeCd());
          url += "&subOfficeCd=" + String.valueOf(model.getSubOfficeCd());
          mv = new ModelAndView("redirect:" + url, "jpbocodesmapform", model);

          MessageUtil.setInfoMessage(mv, MessageUtil.INFO_RECORD_SAVED, model.getRecordDescription());
        } catch (Exception e) {
          mv = new ModelAndView("jpbocodesmapform", "jpbocodesmapform", model);
          setError(e, mv);
        }
      } else {
        JpBoCodesMapModel jpBoCodesMapModel = new JpBoCodesMapModel();
        List<JpBoCodesMapModel> current = maintainService.search(model, request);

        if (current != null && current.size() > 0) {
          jpBoCodesMapModel = current.get(0);
        }

        mv = new ModelAndView("jpbocodesmapform", "jpbocodesmapform", jpBoCodesMapModel);
      }
    }

    if (mv == null) {
      mv = new ModelAndView("jpbocodesmapform", "jpbocodesmapform", new JpBoCodesMapModel());
    }

    setPageKeys("ADMIN", "CODE_ADMIN", mv);
    return mv;

  }

  @RequestMapping(value = "/code/jpbocodesmap/delete", method = { RequestMethod.POST, RequestMethod.GET })
  public ModelAndView deletejpbocodesmap(HttpServletRequest request, HttpServletResponse response, JpBoCodesMapModel model) throws CmrException {

    model.setAction(BaseModel.ACT_DELETE);
    model.setState(BaseModel.STATE_EXISTING);

    AppUser user = AppUser.getUser(request);
    if (!user.isAdmin() && !user.isCmde()) {
      LOG.warn("User " + user.getIntranetId() + " (" + user.getBluePagesName() + ") tried accessing the JP BO Codes map delete function.");
      ModelAndView mv = new ModelAndView("noaccess", "jpbocodesmap", new JpBoCodesMapModel());
      return mv;
    }

    ModelAndView mv = null;
    if (model.allKeysAssigned()) {
      if (shouldProcess(model)) {
        try {
          maintainService.save(model, request);
          mv = new ModelAndView("jpbocodesmap", "jpbocodesmap", new JpBoCodesMapModel());
          MessageUtil.setInfoMessage(mv, MessageUtil.INFO_RECORD_DELETED, model.getRecordDescription());
        } catch (Exception e) {
          mv = new ModelAndView("jpbocodesmap", "jpbocodesmap", new JpBoCodesMapModel());
          setError(e, mv);
        }
      }
    }

    setPageKeys("ADMIN", "CODE_ADMIN", mv);
    return mv;
  }

}
