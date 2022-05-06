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
import com.ibm.cio.cmr.request.model.code.BPIntModel;
import com.ibm.cio.cmr.request.service.code.BPIMaintainService;
import com.ibm.cio.cmr.request.service.code.BPIService;
import com.ibm.cio.cmr.request.user.AppUser;
import com.ibm.cio.cmr.request.util.MessageUtil;

/**
 * @author Sonali Jain
 * 
 */
@Controller
public class BPIController extends BaseController {
  @Autowired
  private BPIService service;

  @Autowired
  private BPIMaintainService maintService;

  private static final Logger LOG = Logger.getLogger(BPIController.class);

  @RequestMapping(value = "/code/bpintlist", method = RequestMethod.GET)
  public @ResponseBody ModelAndView showBPI(HttpServletRequest request, ModelMap model) {
    AppUser user = AppUser.getUser(request);
    if (!user.isAdmin() && !user.isCmde()) {
      LOG.warn("User " + user.getIntranetId() + " (" + user.getBluePagesName() + ") tried accessing the Fields system function.");
      ModelAndView mv = new ModelAndView("noaccess", "bpInt", new BPIntModel());
      return mv;
    }

    // access granted
    ModelAndView mv = new ModelAndView("bpintlist", "bpInt", new BPIntModel());
    setPageKeys("ADMIN", "CODE_ADMIN", mv);
    return mv;
  }

  @RequestMapping(value = "/code/bpdelete", method = { RequestMethod.POST, RequestMethod.GET })
  public ModelAndView deleteBPI(HttpServletRequest request, HttpServletResponse response, BPIntModel model) throws CmrException {
    model.setAction(BaseModel.ACT_DELETE);
    model.setState(BaseModel.STATE_EXISTING);

    AppUser user = AppUser.getUser(request);
    if (!user.isAdmin() && !user.isCmde()) {
      LOG.warn("User " + user.getIntranetId() + " (" + user.getBluePagesName() + ") tried accessing the field Maintenance system function.");
      ModelAndView mv = new ModelAndView("noaccessfieldinfo", "fields", new BPIntModel());
      return mv;
    }

    ModelAndView mv = null;
    if (model.allKeysAssigned()) {
      if (shouldProcess(model)) {
        try {
          maintService.save(model, request);
          mv = new ModelAndView("bpilist", "bpInt", new BPIntModel());
          MessageUtil.setInfoMessage(mv, MessageUtil.INFO_RECORD_DELETED, model.getRecordDescription());
        } catch (Exception e) {
          mv = new ModelAndView("bpilist", "bpInt", new BPIntModel());
          setError(e, mv);
        }
      }
    }
    setPageKeys("ADMIN", "CODE_ADMIN", mv);
    return mv;
  }

  @RequestMapping(value = "/bpilist", method = { RequestMethod.POST, RequestMethod.GET })
  public ModelMap getBPIList(HttpServletRequest request, HttpServletResponse response, BPIntModel model) throws CmrException {

    List<BPIntModel> results = service.search(model, request);
    return wrapAsSearchResult(results);
  }

  @RequestMapping(value = "/code/bpidetails", method = { RequestMethod.POST, RequestMethod.GET })
  public @ResponseBody ModelAndView bpiMaintenance(HttpServletRequest request, HttpServletResponse response, BPIntModel model) throws CmrException {
    AppUser user = AppUser.getUser(request);
    if (!user.isAdmin() && !user.isCmde()) {
      LOG.warn("User " + user.getIntranetId() + " (" + user.getBluePagesName() + ") tried accessing the field Maintenance system function.");
      ModelAndView mv = new ModelAndView("noaccessfieldinfo", "fields", new BPIntModel());
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
          url = "/code/bpidetails?mandt=" + model.getMandt() + "&bpCode=" + String.valueOf(model.getBpCode());

          mv = new ModelAndView("redirect:" + url, "bpInt", model);

          MessageUtil.setInfoMessage(mv, MessageUtil.INFO_RECORD_SAVED, model.getRecordDescription());
        } catch (Exception e) {
          mv = new ModelAndView("bpidetails", "bpInt", model);
          setError(e, mv);
        }
      } else {
        BPIntModel currentModel = new BPIntModel();
        List<BPIntModel> current = maintService.search(model, request);
        if (current != null && current.size() > 0) {
          currentModel = current.get(0);
        }
        mv = new ModelAndView("bpidetails", "bpInt", currentModel);
      }
    }
    if (mv == null) {
      mv = new ModelAndView("bpidetails", "bpInt", new BPIntModel());
    }

    setPageKeys("ADMIN", "CODE_ADMIN", mv);
    return mv;
  }

  @RequestMapping(value = "/code/bpi/process")
  public @ResponseBody ModelAndView processMassAction(HttpServletRequest request, HttpServletResponse response, BPIntModel model)
      throws CmrException {
    AppUser user = AppUser.getUser(request);
    if (!user.isAdmin() && !user.isCmde()) {
      LOG.warn("User " + user.getIntranetId() + " (" + user.getBluePagesName() + ") tried accessing the bpi Maintenance system function.");
      ModelAndView mv = new ModelAndView("noaccessfieldinfo", "fields", new BPIntModel());
      return mv;
    }

    ModelAndView mv = null;
    if (shouldProcess(model) && isMassProcess(model)) {
      try {
        service.processTransaction(model, request);
        mv = new ModelAndView("redirect:/code/bpidetails", "bpInt", model);
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
