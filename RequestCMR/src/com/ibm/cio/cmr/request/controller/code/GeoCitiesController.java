package com.ibm.cio.cmr.request.controller.code;

import java.net.URLEncoder;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
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
import com.ibm.cio.cmr.request.model.code.GeoCitiesModel;
import com.ibm.cio.cmr.request.service.code.GeoCitiesService;
import com.ibm.cio.cmr.request.user.AppUser;
import com.ibm.cio.cmr.request.util.MessageUtil;

@Controller
public class GeoCitiesController extends BaseController {
  private static final Logger LOG = Logger.getLogger(GeoCitiesController.class);

  @Autowired
  private GeoCitiesService gcService;

  @RequestMapping(value = "/code/geocitieslistdef", method = RequestMethod.GET)
  public @ResponseBody
  ModelAndView showGeoCitiesList(HttpServletRequest request, ModelMap model) {
    AppUser user = AppUser.getUser(request);
    if (!user.isAdmin()) {
      LOG.warn("User " + user.getIntranetId() + " (" + user.getBluePagesName() + ") tried accessing the maintain Geo Cities function.");
      ModelAndView mv = new ModelAndView("noaccess", "geocities", new GeoCitiesModel());
      return mv;
    }

    // access granted
    ModelAndView mv = new ModelAndView("geocitieslist", "geocities", new GeoCitiesModel());
    setPageKeys("ADMIN", "CODE_ADMIN", mv);
    return mv;
  }

  @RequestMapping(value = "/code/geocitiesdeflist", method = { RequestMethod.POST, RequestMethod.GET })
  public ModelMap getCntryGeoDefList(HttpServletRequest request, HttpServletResponse response, GeoCitiesModel model) throws CmrException {
    List<GeoCitiesModel> results = gcService.search(model, request);
    return wrapAsSearchResult(results);
  }

  @RequestMapping(value = "/code/geocitiesmain")
  public @ResponseBody
  ModelAndView maintainGeoCities(HttpServletRequest request, GeoCitiesModel model) {
    AppUser user = AppUser.getUser(request);
    if (!user.isAdmin()) {
      LOG.warn("User " + user.getIntranetId() + " (" + user.getBluePagesName() + ") tried accessing the maintain Geo Cities function.");
      ModelAndView mv = new ModelAndView("noaccess", "geocities", new GeoCitiesModel());
      return mv;
    }

    // access granted
    ModelAndView mv = null;

    if (StringUtils.isEmpty(model.getCityId())) {
      model.setCityId(request.getParameter("cityId"));
    }

    if (StringUtils.isEmpty(model.getCmrIssuingCntry())) {
      model.setCmrIssuingCntry(request.getParameter("cmrIssuingCntry"));
    }

    if (StringUtils.isEmpty(model.getCityDesc())) {
      model.setCityDesc(request.getParameter("cityDesc"));
    }

    if (model.allKeysAssigned()) {
      if (shouldProcess(model)) {
        try {

          GeoCitiesModel newModel = gcService.save(model, request);
          String cityId = newModel.getCityId();
          String encodedFieldID = URLEncoder.encode(cityId, "UTF-8");
          String url = "/code/geocitiesmain?cityId=" + encodedFieldID + "&cmrIssuingCntry=" + newModel.getCmrIssuingCntry();
          mv = new ModelAndView("redirect:" + url, "geocitiesmain", newModel);
          MessageUtil.setInfoMessage(mv, MessageUtil.INFO_RECORD_SAVED, model.getRecordDescription());
        } catch (Exception e) {
          mv = new ModelAndView("geocitiesmain", "geoCitiesMain", model);
          setError(e, mv);
        }
      } else {
        GeoCitiesModel currentModel = new GeoCitiesModel();
        List<GeoCitiesModel> current;
        try {
          current = gcService.search(model, request);
          if (current != null && current.size() > 0) {
            currentModel = current.get(0);
            currentModel.setState(BaseModel.STATE_EXISTING);
          }
          mv = new ModelAndView("geocitiesmain", "geoCitiesMain", currentModel);
        } catch (CmrException e) {
          e.printStackTrace();
        }
      }
    }

    if (mv == null) {
      mv = new ModelAndView("geocitiesmain", "geoCitiesMain", new GeoCitiesModel());
    }

    setPageKeys("ADMIN", "CODE_ADMIN", mv);
    return mv;
  }
}
