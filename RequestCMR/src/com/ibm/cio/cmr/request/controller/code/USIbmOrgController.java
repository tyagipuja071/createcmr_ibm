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
import com.ibm.cio.cmr.request.model.code.USIbmOrgModel;
import com.ibm.cio.cmr.request.service.code.USIbmOrgMaintainService;
import com.ibm.cio.cmr.request.service.code.USIbmOrgService;
import com.ibm.cio.cmr.request.user.AppUser;
import com.ibm.cio.cmr.request.util.MessageUtil;

@Controller
public class USIbmOrgController extends BaseController {

  @Autowired
  private USIbmOrgService service;

  @Autowired
  private USIbmOrgMaintainService maintainService;

  private static final Logger LOG = Logger.getLogger(USIbmOrgController.class);

  @RequestMapping(value = "/code/us_ibm_org", method = RequestMethod.GET)
  public @ResponseBody ModelAndView showUSIbmOrg(HttpServletRequest request, ModelMap model) {
    AppUser user = AppUser.getUser(request);
    if (!user.isAdmin() && !user.isCmde()) {
      LOG.warn("User " + user.getIntranetId() + " (" + user.getBluePagesName() + ") tried accessing the US IBM ORG Maintenance system function.");
      ModelAndView mv = new ModelAndView("noaccess", "us_ibm_org", new USIbmOrgModel());
      return mv;
    }

    // access granted
    ModelAndView mv = new ModelAndView("usibmorg", "us_ibm_org", new USIbmOrgModel());
    setPageKeys("ADMIN", "CODE_ADMIN", mv);
    return mv;
  }

  @RequestMapping(value = "/code/us_ibm_org_list", method = { RequestMethod.POST, RequestMethod.GET })
  public ModelMap getUSIbmOrgList(HttpServletRequest request, HttpServletResponse response, USIbmOrgModel model) throws CmrException {
    List<USIbmOrgModel> results = service.search(model, request);
    return wrapAsSearchResult(results);
  }

  @RequestMapping(value = "/code/us_ibm_org_form")
  public @ResponseBody ModelAndView usIbmOrgMaintenance(HttpServletRequest request, HttpServletResponse response, USIbmOrgModel model)
      throws CmrException {

    AppUser user = AppUser.getUser(request);
    if (!user.isAdmin() && !user.isCmde()) {
      LOG.warn("User " + user.getIntranetId() + " (" + user.getBluePagesName() + ") tried accessing the US IBM ORG Maintenance system function.");
      ModelAndView mv = new ModelAndView("noaccess", "us_ibm_bo", new USIbmOrgModel());
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

          String url = "/code/us_ibm_org_form?mandt=" + model.getMandt() + "&aLevel1Value=" + String.valueOf(model.getaLevel1Value())
              + "&aLevel2Value=" + String.valueOf(model.getaLevel2Value()) + "&aLevel3Value=" + String.valueOf(model.getaLevel3Value())
              + "&aLevel4Value=" + String.valueOf(model.getaLevel4Value());
          mv = new ModelAndView("redirect:" + url, "us_ibm_org", model);

          MessageUtil.setInfoMessage(mv, MessageUtil.INFO_RECORD_SAVED, model.getRecordDescription());
        } catch (Exception e) {
          mv = new ModelAndView("usibmorgform", "us_ibm_org", model);
          setError(e, mv);
        }
      } else {
        USIbmOrgModel currentModel = new USIbmOrgModel();
        List<USIbmOrgModel> current = maintainService.search(model, request);

        if (current != null && current.size() > 0) {
          currentModel = current.get(0);
        }

        mv = new ModelAndView("usibmorgform", "us_ibm_org", currentModel);
      }
    }

    if (mv == null) {
      mv = new ModelAndView("usibmorgform", "us_ibm_org", new USIbmOrgModel());
    }

    setPageKeys("ADMIN", "CODE_ADMIN", mv);
    return mv;
  }

}
