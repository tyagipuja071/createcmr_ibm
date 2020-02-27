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
import com.ibm.cio.cmr.request.model.code.CmrInternalTypesModel;
import com.ibm.cio.cmr.request.service.code.CmrInternalTypesAdminService;
import com.ibm.cio.cmr.request.service.code.CmrInternalTypesMaintainService;
import com.ibm.cio.cmr.request.user.AppUser;
import com.ibm.cio.cmr.request.util.MessageUtil;

/**
 * @author Max
 * 
 */
@Controller
public class CmrInternalTypesController extends BaseController {

  @Autowired
  private CmrInternalTypesMaintainService maintService;
  @Autowired
  private CmrInternalTypesAdminService adminService;

  private static final Logger LOG = Logger.getLogger(CmrInternalTypesController.class);

  @RequestMapping(value = "/code/cmrinternaltypes", method = RequestMethod.GET)
  public @ResponseBody
  ModelAndView showCmrInternalTypes(HttpServletRequest request, ModelMap model) {
    AppUser user = AppUser.getUser(request);
    if (!user.isAdmin()) {
      LOG.warn("User " + user.getIntranetId() + " (" + user.getBluePagesName() + ") tried accessing the maintain CmrInternalTypes Table function.");
      ModelAndView mv = new ModelAndView("noaccess", "cmrinternaltypes", new CmrInternalTypesModel());
      return mv;
    }

    // access granted
    ModelAndView mv = new ModelAndView("cmrinternaltypeslist", "cmrinternaltypes", new CmrInternalTypesModel());
    setPageKeys("ADMIN", "CODE_ADMIN", mv);
    return mv;
  }

  @RequestMapping(value = "/code/cmrinternaltypesmain")
  public @ResponseBody
  ModelAndView maintainCmrInternalTypes(HttpServletRequest request, HttpServletResponse response, CmrInternalTypesModel model) throws CmrException {
    AppUser user = AppUser.getUser(request);
    if (!user.isAdmin()) {
      LOG.warn("User " + user.getIntranetId() + " (" + user.getBluePagesName() + ") tried accessing the maintain CmrInternalTypes Table function.");
      ModelAndView mv = new ModelAndView("noaccess", "cmrinternaltypesmain", new CmrInternalTypesModel());
      return mv;
    }

    ModelAndView mv = null;

    if (model.allKeysAssigned()) {

      if (shouldProcess(model)) {
        try {

          CmrInternalTypesModel newModel = maintService.save(model, request);
          mv = new ModelAndView("redirect:/code/cmrinternaltypes", "cmrinternaltypes", newModel);
          MessageUtil.setInfoMessage(mv, MessageUtil.INFO_RECORD_SAVED, model.getRecordDescription());
        } catch (Exception e) {
          mv = new ModelAndView("cmrinternaltypesmaintain", "cmrinternaltypesmain", model);
          setError(e, mv);
        }
      } else {
        CmrInternalTypesModel currentModel = new CmrInternalTypesModel();
        List<CmrInternalTypesModel> current = maintService.search(model, request);
        if (current != null && current.size() > 0) {
          currentModel = current.get(0);
        }
        mv = new ModelAndView("cmrinternaltypesmaintain", "cmrinternaltypesmain", currentModel);
      }
    }
    if (mv == null) {
      mv = new ModelAndView("cmrinternaltypesmaintain", "cmrinternaltypesmain", new CmrInternalTypesModel());
    }

    setPageKeys("ADMIN", "CODE_ADMIN", mv);
    return mv;
  }

  @RequestMapping(value = "/code/cmrinternaltypelist", method = { RequestMethod.POST, RequestMethod.GET })
  public ModelMap getCmrInternalTypesList(HttpServletRequest request, HttpServletResponse response, CmrInternalTypesModel model) throws CmrException {

    List<CmrInternalTypesModel> results = adminService.search(model, request);
    return wrapAsSearchResult(results);
  }
}
