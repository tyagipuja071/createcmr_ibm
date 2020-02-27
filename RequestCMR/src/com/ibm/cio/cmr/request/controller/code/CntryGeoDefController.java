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
import com.ibm.cio.cmr.request.model.code.CntryGeoDefModel;
import com.ibm.cio.cmr.request.service.code.CntryGeoDefMaintService;
import com.ibm.cio.cmr.request.service.code.CntryGeoDefService;
import com.ibm.cio.cmr.request.user.AppUser;
import com.ibm.cio.cmr.request.util.MessageUtil;

/**
 * @author Rochelle Salazar
 * 
 */
@Controller
public class CntryGeoDefController extends BaseController {

  @Autowired
  private CntryGeoDefService cntryGeoDefService;
  @Autowired
  private CntryGeoDefMaintService cntryGeoDefMaintService;

  private static final Logger LOG = Logger.getLogger(CntryGeoDefController.class);

  @RequestMapping(value = "/code/cntrygeodef", method = RequestMethod.GET)
  public @ResponseBody
  ModelAndView showCntrygeoDef(HttpServletRequest request, ModelMap model) {
    AppUser user = AppUser.getUser(request);
    if (!user.isAdmin()) {
      LOG.warn("User " + user.getIntranetId() + " (" + user.getBluePagesName() + ") tried accessing the maintain Country Geo Default Table function.");
      ModelAndView mv = new ModelAndView("noaccess", "cntrygeodef", new CntryGeoDefModel());
      return mv;
    }

    // access granted
    ModelAndView mv = new ModelAndView("cntrygeodeflist", "cntrygeodef", new CntryGeoDefModel());
    setPageKeys("ADMIN", "CODE_ADMIN", mv);
    return mv;
  }

  @RequestMapping(value = "/code/cntrygeodefmain")
  public @ResponseBody
  ModelAndView maintainCntrygeoDefMain(HttpServletRequest request, HttpServletResponse response, CntryGeoDefModel model) throws CmrException {
    AppUser user = AppUser.getUser(request);
    if (!user.isAdmin()) {
      LOG.warn("User " + user.getIntranetId() + " (" + user.getBluePagesName() + ") tried accessing the maintain Country Geo Default Table function.");
      ModelAndView mv = new ModelAndView("noaccess", "cntrygeodefmain", new CntryGeoDefModel());
      return mv;
    }

    ModelAndView mv = null;

    if (model.allKeysAssigned()) {

      if (shouldProcess(model)) {
        try {

          CntryGeoDefModel newModel = cntryGeoDefMaintService.save(model, request);
          mv = new ModelAndView("redirect:/code/cntrygeodef", "cntrygeodef", newModel);
          MessageUtil.setInfoMessage(mv, MessageUtil.INFO_RECORD_SAVED, model.getRecordDescription());
        } catch (Exception e) {
          mv = new ModelAndView("cntrygeodefmaintain", "cntrygeodefmain", model);
          setError(e, mv);
        }
      } else {
        CntryGeoDefModel currentModel = new CntryGeoDefModel();
        List<CntryGeoDefModel> current = cntryGeoDefMaintService.search(model, request);
        if (current != null && current.size() > 0) {
          currentModel = current.get(0);
        }
        mv = new ModelAndView("cntrygeodefmaintain", "cntrygeodefmain", currentModel);
      }
    }
    if (mv == null) {
      mv = new ModelAndView("cntrygeodefmaintain", "cntrygeodefmain", new CntryGeoDefModel());
    }

    setPageKeys("ADMIN", "CODE_ADMIN", mv);
    return mv;
  }

  @RequestMapping(value = "/code/cntrygeodeflist", method = { RequestMethod.POST, RequestMethod.GET })
  public ModelMap getCntryGeoDefList(HttpServletRequest request, HttpServletResponse response, CntryGeoDefModel model) throws CmrException {
    List<CntryGeoDefModel> results = cntryGeoDefService.search(model, request);
    return wrapAsSearchResult(results);
  }
}
