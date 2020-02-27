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
import com.ibm.cio.cmr.request.model.code.CnaeModel;
import com.ibm.cio.cmr.request.service.code.CnaeMaintainService;
import com.ibm.cio.cmr.request.service.code.CnaeService;
import com.ibm.cio.cmr.request.user.AppUser;
import com.ibm.cio.cmr.request.util.MessageUtil;

/**
 * @author RoopakChugh
 * 
 */
@Controller
public class CnaeController extends BaseController {
  @Autowired
  private CnaeService service;

  @Autowired
  private CnaeMaintainService maintService;

  private static final Logger LOG = Logger.getLogger(CnaeController.class);

  @RequestMapping(value = "/code/cnae", method = RequestMethod.GET)
  public @ResponseBody
  ModelAndView showCnae(HttpServletRequest request, ModelMap model) {
    AppUser user = AppUser.getUser(request);
    if (!user.isAdmin() && !user.isCmde()) {
      LOG.warn("User " + user.getIntranetId() + " (" + user.getBluePagesName() + ") tried accessing the Fields system function.");
      ModelAndView mv = new ModelAndView("noaccess", "cnae", new CnaeModel());
      return mv;
    }

    // access granted
    ModelAndView mv = new ModelAndView("cnae", "cnae", new CnaeModel());
    setPageKeys("ADMIN", "CODE_ADMIN", mv);
    return mv;
  }

  @RequestMapping(value = "/cnae", method = { RequestMethod.POST, RequestMethod.GET })
  public ModelMap getCnaeList(HttpServletRequest request, HttpServletResponse response, CnaeModel model) throws CmrException {

    List<CnaeModel> results = service.search(model, request);
    return wrapAsSearchResult(results);
  }

  @RequestMapping(value = "/code/cnaedetails")
  public @ResponseBody
  ModelAndView cnaeMaintenance(HttpServletRequest request, HttpServletResponse response, CnaeModel model) throws CmrException {
    AppUser user = AppUser.getUser(request);
    if (!user.isAdmin() && !user.isCmde()) {
      LOG.warn("User " + user.getIntranetId() + " (" + user.getBluePagesName() + ") tried accessing the field Maintenance system function.");
      ModelAndView mv = new ModelAndView("noaccessfieldinfo", "fields", new CnaeModel());
      return mv;
    }

    ModelAndView mv = null;
    if (model.allKeysAssigned()) {
      if (shouldProcess(model)) {
        try {

          CnaeModel newModel = maintService.save(model, request);
          // String url = "/code/cnaedetails?cnaeNo=" + newModel.getCnaeNo();
          String url = "/cnae" + newModel.getCnaeNo();
          // mv = new ModelAndView("redirect:" + url, "cnae", newModel);
          mv = new ModelAndView("redirect:/code/cnae", "cnae", newModel);

          MessageUtil.setInfoMessage(mv, MessageUtil.INFO_RECORD_SAVED, model.getRecordDescription());
        } catch (Exception e) {
          mv = new ModelAndView("cnaedetails", "cnae", model);
          setError(e, mv);
        }
      } else {
        CnaeModel currentModel = new CnaeModel();
        List<CnaeModel> current = maintService.search(model, request);
        if (current != null && current.size() > 0) {
          currentModel = current.get(0);
        }
        mv = new ModelAndView("cnaedetails", "cnae", currentModel);
      }
    }
    if (mv == null) {
      mv = new ModelAndView("cnaedetails", "cnae", new CnaeModel());
    }

    setPageKeys("ADMIN", "CODE_ADMIN", mv);
    return mv;
  }

}
