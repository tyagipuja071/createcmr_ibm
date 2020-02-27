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
import com.ibm.cio.cmr.request.model.code.SCCModel;
import com.ibm.cio.cmr.request.service.code.SCCMaintainService;
import com.ibm.cio.cmr.request.service.code.SCCService;
import com.ibm.cio.cmr.request.user.AppUser;
import com.ibm.cio.cmr.request.util.MessageUtil;

/**
 * @author Sonali Jain
 * 
 */
@Controller
public class SCCController extends BaseController {
  @Autowired
  private SCCService service;

  @Autowired
  private SCCMaintainService maintService;

  private static final Logger LOG = Logger.getLogger(SCCController.class);

  @RequestMapping(value = "/code/scclist", method = RequestMethod.GET)
  public @ResponseBody
  ModelAndView showSCC(HttpServletRequest request, ModelMap model) {
    AppUser user = AppUser.getUser(request);
    if (!user.isAdmin() && !user.isCmde()) {
      LOG.warn("User " + user.getIntranetId() + " (" + user.getBluePagesName() + ") tried accessing the Fields system function.");
      ModelAndView mv = new ModelAndView("noaccess", "scc", new SCCModel());
      return mv;
    }

    // access granted
    ModelAndView mv = new ModelAndView("scclist", "scc", new SCCModel());
    setPageKeys("ADMIN", "CODE_ADMIN", mv);
    return mv;
  }

  @RequestMapping(value = "/scclist", method = { RequestMethod.POST, RequestMethod.GET })
  public ModelMap getSCCList(HttpServletRequest request, HttpServletResponse response, SCCModel model) throws CmrException {

    List<SCCModel> results = service.search(model, request);
    return wrapAsSearchResult(results);
  }

  @RequestMapping(value = "/code/sccdetails")
  public @ResponseBody
  ModelAndView sccMaintenance(HttpServletRequest request, HttpServletResponse response, SCCModel model) throws CmrException {
    AppUser user = AppUser.getUser(request);
    if (!user.isAdmin() && !user.isCmde()) {
      LOG.warn("User " + user.getIntranetId() + " (" + user.getBluePagesName() + ") tried accessing the field Maintenance system function.");
      ModelAndView mv = new ModelAndView("noaccessfieldinfo", "fields", new SCCModel());
      return mv;
    }

    ModelAndView mv = null;
    if (model.allKeysAssigned()) {
      if (shouldProcess(model)) {
        try {
          SCCModel newModel = maintService.save(model, request);
          String url = "/code/sccdetails?nSt=" + newModel.getnSt() + "&nCity=" + newModel.getnCity() + "&nCnty=" + newModel.getnCnty() + "&cZip="
              + newModel.getcZip();
          mv = new ModelAndView("redirect:" + url, "scc", newModel);

          MessageUtil.setInfoMessage(mv, MessageUtil.INFO_RECORD_SAVED, model.getRecordDescription());
        } catch (Exception e) {
          mv = new ModelAndView("sccdetails", "scc", model);
          setError(e, mv);
        }
      } else {
        SCCModel currentModel = new SCCModel();
        List<SCCModel> current = maintService.search(model, request);
        if (current != null && current.size() > 0) {
          currentModel = current.get(0);
        }
        mv = new ModelAndView("sccdetails", "scc", currentModel);
      }
    }
    if (mv == null) {
      mv = new ModelAndView("sccdetails", "scc", new SCCModel());
    }

    setPageKeys("ADMIN", "CODE_ADMIN", mv);
    return mv;
  }

  @RequestMapping(value = "/code/scc/process")
  public @ResponseBody
  ModelAndView processMassAction(HttpServletRequest request, HttpServletResponse response, SCCModel model) throws CmrException {
    AppUser user = AppUser.getUser(request);
    if (!user.isAdmin() && !user.isCmde()) {
      LOG.warn("User " + user.getIntranetId() + " (" + user.getBluePagesName() + ") tried accessing the SCC Maintenance system function.");
      ModelAndView mv = new ModelAndView("noaccessfieldinfo", "fields", new SCCModel());
      return mv;
    }

    ModelAndView mv = null;
    if (shouldProcess(model) && isMassProcess(model)) {
      try {
        service.processTransaction(model, request);
        mv = new ModelAndView("redirect:/code/sccdetails", "scc", model);
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
