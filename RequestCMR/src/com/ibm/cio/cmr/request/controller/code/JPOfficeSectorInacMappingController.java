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
import com.ibm.cio.cmr.request.model.code.JPOfficeSectorInacMapModel;
import com.ibm.cio.cmr.request.service.code.JPOfficeSectorInacMappingMaintainService;
import com.ibm.cio.cmr.request.service.code.JPOfficeSectorInacMappingService;
import com.ibm.cio.cmr.request.user.AppUser;
import com.ibm.cio.cmr.request.util.MessageUtil;

/**
 * 
 * @author XiangBin Liu
 *
 */

@Controller
public class JPOfficeSectorInacMappingController extends BaseController {

  private static final Logger LOG = Logger.getLogger(JPOfficeSectorInacMappingController.class);

  @Autowired
  private JPOfficeSectorInacMappingService service;

  @Autowired
  private JPOfficeSectorInacMappingMaintainService maintainService;

  @RequestMapping(value = "/code/jpofficesectorinacmap", method = RequestMethod.GET)
  public @ResponseBody ModelAndView showJPOfficeSectorInacMapping(HttpServletRequest request, ModelMap model) {
    AppUser user = AppUser.getUser(request);
    if (!user.isAdmin() && !user.isCmde()) {
      LOG.warn("User " + user.getIntranetId() + " (" + user.getBluePagesName()
          + ") tried accessing the JP OFFICE SECTOR INAC MAPPING Maintenance system function.");
      ModelAndView mv = new ModelAndView("noaccess", "jpofficesectorinacmap", new JPOfficeSectorInacMapModel());
      return mv;
    }

    // access granted
    ModelAndView mv = new ModelAndView("jpofficesectorinacmap", "jpofficesectorinacmap", new JPOfficeSectorInacMapModel());
    setPageKeys("ADMIN", "CODE_ADMIN", mv);
    return mv;

  }

  @RequestMapping(value = "/code/jp_office_sector_inac_map_list", method = { RequestMethod.GET, RequestMethod.POST })
  public ModelMap getJPOfficeSectorInacMappingList(HttpServletRequest request, HttpServletResponse response, JPOfficeSectorInacMapModel model)
      throws CmrException {

    List<JPOfficeSectorInacMapModel> results = service.search(model, request);
    return wrapAsSearchResult(results);

  }

  @RequestMapping(value = "/code/jpofficesectorinacform", method = { RequestMethod.GET, RequestMethod.POST })
  public ModelAndView jpOfficeSectorInacMappingMaintenance(HttpServletRequest request, HttpServletResponse response, JPOfficeSectorInacMapModel model)
      throws CmrException {

    AppUser user = AppUser.getUser(request);
    if (!user.isAdmin() && !user.isCmde()) {
      LOG.warn("User " + user.getIntranetId() + " (" + user.getBluePagesName()
          + ") tried accessing the JP OFFICE SECTOR INAC MAPPING Maintenance system function.");
      ModelAndView mv = new ModelAndView("noaccess", "jpofficesectorinacform", new JPOfficeSectorInacMapModel());
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

          String url = "/code/jp_office_sector_inac_map_form?office=" + model.getOfficeCd();
          url += "&sector=" + String.valueOf(model.getSectorCd());
          url += "&inac=" + String.valueOf(model.getInacCd());
          url += "&apCustClusterId=" + String.valueOf(model.getApCustClusterId());
          mv = new ModelAndView("redirect:" + url, "jpofficesectorinacform", model);

          MessageUtil.setInfoMessage(mv, MessageUtil.INFO_RECORD_SAVED, model.getRecordDescription());
        } catch (Exception e) {
          mv = new ModelAndView("jpofficesectorinacform", "jpofficesectorinacform", model);
          setError(e, mv);
        }
      } else {
        JPOfficeSectorInacMapModel jPOfficeSectorInacMapModel = new JPOfficeSectorInacMapModel();
        List<JPOfficeSectorInacMapModel> current = maintainService.search(model, request);

        if (current != null && current.size() > 0) {
          jPOfficeSectorInacMapModel = current.get(0);
        }

        mv = new ModelAndView("jpofficesectorinacform", "jpofficesectorinacform", jPOfficeSectorInacMapModel);
      }
    }

    if (mv == null) {
      mv = new ModelAndView("jpofficesectorinacform", "jpofficesectorinacform", new JPOfficeSectorInacMapModel());
    }

    setPageKeys("ADMIN", "CODE_ADMIN", mv);
    return mv;

  }

}
