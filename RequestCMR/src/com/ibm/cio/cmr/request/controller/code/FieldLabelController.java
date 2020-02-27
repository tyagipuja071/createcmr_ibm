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
import com.ibm.cio.cmr.request.model.code.FieldLabelModel;
import com.ibm.cio.cmr.request.service.code.FieldLabelService;
import com.ibm.cio.cmr.request.service.code.FieldLblMaintainService;
import com.ibm.cio.cmr.request.user.AppUser;
import com.ibm.cio.cmr.request.util.MessageUtil;

/**
 * @author Rochelle Salazar
 * 
 */
@Controller
public class FieldLabelController extends BaseController {

  private static final Logger LOG = Logger.getLogger(FieldLabelController.class);

  @Autowired
  private FieldLabelService service;

  @Autowired
  private FieldLblMaintainService addservice;

  @RequestMapping(value = "/code/field_lbl", method = RequestMethod.GET)
  public @ResponseBody
  ModelAndView showfieldlbl(HttpServletRequest request, ModelMap model) {
    AppUser user = AppUser.getUser(request);
    if (!user.isAdmin()) {
      LOG.warn("User " + user.getIntranetId() + " (" + user.getBluePagesName() + ") tried accessing the field label system function.");
      ModelAndView mv = new ModelAndView("noaccess", "fieldlbl", new FieldLabelModel());
      return mv;
    }

    // access granted
    ModelAndView mv = new ModelAndView("fieldlbllist", "fieldlbl", new FieldLabelModel());
    setPageKeys("ADMIN", "CODE_ADMIN", mv);
    return mv;

  }

  @RequestMapping(value = "/code/addfieldlbl")
  public @ResponseBody
  ModelAndView maintainfieldlbl(HttpServletRequest request, HttpServletResponse response, FieldLabelModel model) throws CmrException {
    AppUser user = AppUser.getUser(request);
    if (!user.isAdmin()) {
      LOG.warn("User " + user.getIntranetId() + " (" + user.getBluePagesName() + ") tried accessing the field label system function.");
      ModelAndView mv = new ModelAndView("noaccess", "fieldlbl", new FieldLabelModel());
      return mv;
    }

    ModelAndView mv = null;
    if (model.allKeysAssigned()) {
      if (shouldProcess(model)) {
        try {
          FieldLabelModel newModel = addservice.save(model, request);
          // mv = new ModelAndView("redirect:/code/addfieldlbl?fieldId=" +
          // newModel.getFieldId(), "fieldlbl", newModel);
          mv = new ModelAndView("redirect:/code/field_lbl", "fieldlbl", newModel);
          MessageUtil.setInfoMessage(mv, MessageUtil.INFO_RECORD_SAVED, model.getRecordDescription());
        } catch (Exception e) {
          mv = new ModelAndView("addfieldlbl", "fieldlbl", model);
          setError(e, mv);
        }
      } else {
        FieldLabelModel currentModel = new FieldLabelModel();
        List<FieldLabelModel> current = addservice.search(model, request);
        if (current != null && current.size() > 0) {
          currentModel = current.get(0);
        }
        mv = new ModelAndView("addfieldlbl", "fieldlbl", currentModel);
      }
    }

    if (mv == null) {
      mv = new ModelAndView("addfieldlbl", "fieldlbl", new FieldLabelModel());
    }

    setPageKeys("ADMIN", "CODE_ADMIN", mv);
    return mv;
  }

  @RequestMapping(value = "/code/fieldlbllist", method = { RequestMethod.POST, RequestMethod.GET })
  public ModelMap getFieldLblList(HttpServletRequest request, HttpServletResponse response, FieldLabelModel model) throws CmrException {
    List<FieldLabelModel> results = service.search(model, request);
    return wrapAsSearchResult(results);
  }

}
