package com.ibm.cio.cmr.request.controller.code;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.controller.BaseController;
import com.ibm.cio.cmr.request.model.code.BusinessDataSrcModel;
import com.ibm.cio.cmr.request.model.system.UserModel;
import com.ibm.cio.cmr.request.service.code.BdsMaintainService;
import com.ibm.cio.cmr.request.service.code.BusinessDataSrcService;
import com.ibm.cio.cmr.request.user.AppUser;
import com.ibm.cio.cmr.request.util.MessageUtil;

/**
 * @author Rochelle Salazar
 * 
 */
@Controller
public class BusinessDataSrcController extends BaseController {

  private static final Logger LOG = Logger.getLogger(BusinessDataSrcController.class);

  @Autowired
  private BusinessDataSrcService service;

  @Autowired
  private BdsMaintainService addservice;

  @RequestMapping(value = "/code/bds_tbl_info", method = RequestMethod.GET)
  public @ResponseBody
  ModelAndView showbds(HttpServletRequest request, ModelMap model) {
    AppUser user = AppUser.getUser(request);
    if (!user.isAdmin()) {
      LOG.warn("User " + user.getIntranetId() + " (" + user.getBluePagesName() + ") tried accessing the Users system function.");
      ModelAndView mv = new ModelAndView("noaccess", "users", new UserModel());
      return mv;
    }

    // access granted
    ModelAndView mv = new ModelAndView("bdslist", "bds", new BusinessDataSrcModel());
    setPageKeys("ADMIN", "USER_ADMIN", mv);
    return mv;

  }

  @RequestMapping(value = "/code/addbds")
  public @ResponseBody
  ModelAndView maintainBds(HttpServletRequest request, HttpServletResponse response, BusinessDataSrcModel model) throws CmrException {
    AppUser user = AppUser.getUser(request);
    if (!user.isAdmin()) {
      LOG.warn("User " + user.getIntranetId() + " (" + user.getBluePagesName() + ") tried accessing the Users system function.");
      ModelAndView mv = new ModelAndView("noaccess", "users", new UserModel());
      return mv;
    }

    ModelAndView mv = null;
    if (model.allKeysAssigned()) {
      if (shouldProcess(model)) {
        try {
          BusinessDataSrcModel newModel = addservice.save(model, request);
          mv = new ModelAndView("redirect:/code/addbds?fieldId=" + newModel.getFieldId(), "bds", newModel);
          MessageUtil.setInfoMessage(mv, MessageUtil.INFO_RECORD_SAVED, model.getRecordDescription());
        } catch (Exception e) {
          mv = new ModelAndView("addbds", "bds", model);
          setError(e, mv);
        }
      } else {
        BusinessDataSrcModel currentModel = new BusinessDataSrcModel();
        List<BusinessDataSrcModel> current = addservice.search(model, request);
        if (current != null && current.size() > 0) {
          currentModel = current.get(0);
        }
        mv = new ModelAndView("addbds", "bds", currentModel);
      }
    }

    if (mv == null) {
      mv = new ModelAndView("addbds", "bds", new BusinessDataSrcModel());
    }

    setPageKeys("ADMIN", "USER_ADMIN", mv);
    return mv;
  }

  @RequestMapping(value = "/code/bdsdetails/{fieldId}")
  public @ResponseBody
  ModelAndView maintainBdsDetails(@PathVariable("fieldId") String fieldId, HttpServletRequest request, HttpServletResponse response,
      BusinessDataSrcModel model) throws CmrException {
    AppUser user = AppUser.getUser(request);
    if (!user.isAdmin()) {
      LOG.warn("User " + user.getIntranetId() + " (" + user.getBluePagesName() + ") tried accessing the Users system function.");
      ModelAndView mv = new ModelAndView("noaccess", "users", new UserModel());
      return mv;
    }

    ModelAndView mv = null;
    BusinessDataSrcModel currentModel = new BusinessDataSrcModel();
    model.setFieldId(fieldId);
    List<BusinessDataSrcModel> current = addservice.search(model, request);
    if (current != null && current.size() > 0) {
      currentModel = current.get(0);
    }
    mv = new ModelAndView("addbds", "bds", currentModel);
    setPageKeys("ADMIN", "USER_ADMIN", mv);
    return mv;
  }

  @RequestMapping(value = "/code/bds/bdslist", method = { RequestMethod.POST, RequestMethod.GET })
  public ModelMap getBdsList(HttpServletRequest request, HttpServletResponse response, BusinessDataSrcModel model) throws CmrException {
    List<BusinessDataSrcModel> results = service.search(model, request);
    return wrapAsSearchResult(results);
  }
}
