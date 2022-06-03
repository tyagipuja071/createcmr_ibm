/**
 * 
 */
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
import com.ibm.cio.cmr.request.model.code.RestrictModel;
import com.ibm.cio.cmr.request.service.code.RestrictMaintainService;
import com.ibm.cio.cmr.request.service.code.RestrictService;
import com.ibm.cio.cmr.request.user.AppUser;
import com.ibm.cio.cmr.request.util.MessageUtil;

/**
 * @author Sonali Jain
 * 
 */
@Controller
public class RestrictController extends BaseController {
  @Autowired
  private RestrictService service;

  @Autowired
  private RestrictMaintainService maintService;

  private static final Logger LOG = Logger.getLogger(RestrictController.class);

  @RequestMapping(value = "/code/restrictlist", method = RequestMethod.GET)
  public @ResponseBody ModelAndView showRST(HttpServletRequest request, ModelMap model) {
    AppUser user = AppUser.getUser(request);
    if (!user.isAdmin() && !user.isCmde()) {
      LOG.warn("User " + user.getIntranetId() + " (" + user.getBluePagesName() + ") tried accessing the Fields system function.");
      ModelAndView mv = new ModelAndView("noaccess", "restrict", new RestrictModel());
      return mv;
    }

    // access granted
    ModelAndView mv = new ModelAndView("restrictlist", "restrict", new RestrictModel());
    setPageKeys("ADMIN", "CODE_ADMIN", mv);
    return mv;
  }

  @RequestMapping(value = "/code/rstdelete", method = { RequestMethod.POST, RequestMethod.GET })
  public ModelAndView deleteRST(HttpServletRequest request, HttpServletResponse response, RestrictModel model) throws CmrException {
    model.setAction(BaseModel.ACT_DELETE);
    model.setState(BaseModel.STATE_EXISTING);

    AppUser user = AppUser.getUser(request);
    if (!user.isAdmin() && !user.isCmde()) {
      LOG.warn("User " + user.getIntranetId() + " (" + user.getBluePagesName() + ") tried accessing the field Maintenance system function.");
      ModelAndView mv = new ModelAndView("noaccessfieldinfo", "fields", new RestrictModel());
      return mv;
    }

    ModelAndView mv = null;
    if (model.allKeysAssigned()) {
      if (shouldProcess(model)) {
        try {
          maintService.save(model, request);
          mv = new ModelAndView("restrictlist", "restrict", new RestrictModel());
          MessageUtil.setInfoMessage(mv, MessageUtil.INFO_RECORD_DELETED, model.getRecordDescription());
        } catch (Exception e) {
          mv = new ModelAndView("restrictlist", "restrict", new RestrictModel());
          setError(e, mv);
        }
      }
    }
    setPageKeys("ADMIN", "CODE_ADMIN", mv);
    return mv;
  }

  @RequestMapping(value = "/rstlist", method = { RequestMethod.POST, RequestMethod.GET })
  public ModelMap getRSTList(HttpServletRequest request, HttpServletResponse response, RestrictModel model) throws CmrException {

    List<RestrictModel> results = service.search(model, request);
    return wrapAsSearchResult(results);
  }

  @RequestMapping(value = "/code/rstdetails", method = { RequestMethod.POST, RequestMethod.GET })
  public @ResponseBody ModelAndView rstMaintenance(HttpServletRequest request, HttpServletResponse response, RestrictModel model)
      throws CmrException {
    AppUser user = AppUser.getUser(request);
    if (!user.isAdmin() && !user.isCmde()) {
      LOG.warn("User " + user.getIntranetId() + " (" + user.getBluePagesName() + ") tried accessing the field Maintenance system function.");
      ModelAndView mv = new ModelAndView("noaccessfieldinfo", "fields", new RestrictModel());
      return mv;
    }

    if ("I".equals(model.getAction())) {
      model.setAction(BaseModel.ACT_INSERT);
      model.setState(BaseModel.STATE_NEW);
    }
    if ("U".equals(model.getAction())) {
      model.setAction(BaseModel.ACT_UPDATE);
      model.setState(BaseModel.STATE_EXISTING);
    }
    ModelAndView mv = null;
    if (model.allKeysAssigned()) {
      if (shouldProcess(model)) {
        try {
          maintService.save(model, request);
          String url = "";
          url = "/code/rstdetails?mandt=" + model.getMandt() + "&restrictToCd=" + String.valueOf(model.getRestrictToCd());

          mv = new ModelAndView("redirect:" + url, "restrict", model);

          MessageUtil.setInfoMessage(mv, MessageUtil.INFO_RECORD_SAVED, model.getRecordDescription());
        } catch (Exception e) {
          mv = new ModelAndView("rstdetails", "restrict", model);
          setError(e, mv);
        }
      } else {
        RestrictModel currentModel = new RestrictModel();
        List<RestrictModel> current = maintService.search(model, request);
        if (current != null && current.size() > 0) {
          currentModel = current.get(0);
        }
        mv = new ModelAndView("rstdetails", "restrict", currentModel);
      }
    }
    if (mv == null) {
      mv = new ModelAndView("rstdetails", "restrict", new RestrictModel());
    }

    setPageKeys("ADMIN", "CODE_ADMIN", mv);
    return mv;
  }

  @RequestMapping(value = "/code/rst/process")
  public @ResponseBody ModelAndView processMassAction(HttpServletRequest request, HttpServletResponse response, RestrictModel model)
      throws CmrException {
    AppUser user = AppUser.getUser(request);
    if (!user.isAdmin() && !user.isCmde()) {
      LOG.warn("User " + user.getIntranetId() + " (" + user.getBluePagesName() + ") tried accessing the rst Maintenance system function.");
      ModelAndView mv = new ModelAndView("noaccessfieldinfo", "fields", new RestrictModel());
      return mv;
    }

    ModelAndView mv = null;
    if (shouldProcess(model) && isMassProcess(model)) {
      try {
        service.processTransaction(model, request);
        mv = new ModelAndView("redirect:/code/rstdetails", "restrict", model);
        if ("MASS_DELETE".equals(model.getMassAction())) {
          MessageUtil.setInfoMessage(mv, "Record(s) deleted successfully.");
        } else {
          MessageUtil.setInfoMessage(mv, "No action to perform.");
        }
      } catch (Exception e) {
        mv = new ModelAndView("redirect:/code/field_info", "fieldInfo", model);
        MessageUtil.setErrorMessage(mv, (e instanceof CmrException) ? ((CmrException) e).getCode() : MessageUtil.ERROR_GENERAL);
      }
    } else {
      mv = new ModelAndView("redirect:/code/field_info", "fieldInfo", model);
    }
    setPageKeys("ADMIN", "CODE_ADMIN", mv);
    return mv;
  }

}
