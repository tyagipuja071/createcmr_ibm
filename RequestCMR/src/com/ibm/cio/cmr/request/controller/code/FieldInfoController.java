/**
 * 
 */
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
import com.ibm.cio.cmr.request.model.code.FieldInfoModel;
import com.ibm.cio.cmr.request.service.code.FieldInfoMaintainService;
import com.ibm.cio.cmr.request.service.code.FieldInfoService;
import com.ibm.cio.cmr.request.user.AppUser;
import com.ibm.cio.cmr.request.util.MessageUtil;

/**
 * @author Sonali Jain
 * 
 */
@Controller
public class FieldInfoController extends BaseController {
  @Autowired
  private FieldInfoService service;

  @Autowired
  private FieldInfoMaintainService maintService;

  private static final Logger LOG = Logger.getLogger(FieldInfoController.class);

  @RequestMapping(value = "/code/field_info", method = RequestMethod.GET)
  public @ResponseBody
  ModelAndView showFields(HttpServletRequest request, ModelMap model) {
    AppUser user = AppUser.getUser(request);
    if (!user.isAdmin()) {
      LOG.warn("User " + user.getIntranetId() + " (" + user.getBluePagesName() + ") tried accessing the Fields system function.");
      ModelAndView mv = new ModelAndView("noaccess", "fields", new FieldInfoModel());
      return mv;
    }

    // access granted
    ModelAndView mv = new ModelAndView("fieldInfolist", "fieldInfo", new FieldInfoModel());
    setPageKeys("ADMIN", "CODE_ADMIN", mv);
    return mv;
  }

  @RequestMapping(value = "/fieldInfolist", method = { RequestMethod.POST, RequestMethod.GET })
  public ModelMap getFieldInfoList(HttpServletRequest request, HttpServletResponse response, FieldInfoModel model) throws CmrException {

    List<FieldInfoModel> results = service.search(model, request);
    return wrapAsSearchResult(results);
  }

  @RequestMapping(value = "/code/field_info_details")
  public @ResponseBody
  ModelAndView fieldMaintenance(HttpServletRequest request, HttpServletResponse response, FieldInfoModel model) throws CmrException {
    AppUser user = AppUser.getUser(request);
    if (!user.isAdmin()) {
      LOG.warn("User " + user.getIntranetId() + " (" + user.getBluePagesName() + ") tried accessing the field Maintenance system function.");
      ModelAndView mv = new ModelAndView("noaccessfieldinfo", "fields", new FieldInfoModel());
      return mv;
    }

    ModelAndView mv = null;
    if (model.allKeysAssigned()) {
      if (shouldProcess(model)) {
        try {
          if (!StringUtils.isBlank(model.getValidation()) && "DB".equals(model.getValidation())) {
            model.setValidation("DB-" + model.getQueryId());
          }

          FieldInfoModel newModel = maintService.save(model, request);
          String fieldId = newModel.getFieldId();
          String encodedFieldID = URLEncoder.encode(fieldId, "UTF-8");
          String url = "/code/field_info_details?fieldId=" + encodedFieldID + "&seqNo=" + newModel.getSeqNo() + "&cmrIssuingCntry="
              + newModel.getCmrIssuingCntry();
          mv = new ModelAndView("redirect:" + url, "field", newModel);

          // mv = new ModelAndView("redirect:/code/field_info_details", "field",
          // newModel);

          MessageUtil.setInfoMessage(mv, MessageUtil.INFO_RECORD_SAVED, model.getRecordDescription());
        } catch (Exception e) {
          mv = new ModelAndView("fieldInfomaintain", "field", model);
          setError(e, mv);
        }
      } else {
        FieldInfoModel currentModel = new FieldInfoModel();
        List<FieldInfoModel> current = maintService.search(model, request);
        if (current != null && current.size() > 0) {
          currentModel = current.get(0);
          if (!StringUtils.isBlank(currentModel.getValidation()) && currentModel.getValidation().startsWith("DB")) {
            currentModel.setQueryId(currentModel.getValidation().substring(3));
            currentModel.setValidation("DB");
          }
        }
        mv = new ModelAndView("fieldInfomaintain", "field", currentModel);
      }
    }
    if (mv == null) {
      mv = new ModelAndView("fieldInfomaintain", "field", new FieldInfoModel());
    }

    setPageKeys("ADMIN", "CODE_ADMIN", mv);
    return mv;
  }

  @RequestMapping(value = "/code/field_info/process")
  public @ResponseBody
  ModelAndView processMassAction(HttpServletRequest request, HttpServletResponse response, FieldInfoModel model) throws CmrException {
    AppUser user = AppUser.getUser(request);
    if (!user.isAdmin()) {
      LOG.warn("User " + user.getIntranetId() + " (" + user.getBluePagesName() + ") tried accessing the field Maintenance system function.");
      ModelAndView mv = new ModelAndView("noaccessfieldinfo", "fields", new FieldInfoModel());
      return mv;
    }

    ModelAndView mv = null;
    if (shouldProcess(model) && isMassProcess(model)) {
      try {
        service.processTransaction(model, request);
        mv = new ModelAndView("redirect:/code/field_info", "fieldInfo", model);
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
    mv.addObject("fieldId", model.getFieldId());
    setPageKeys("ADMIN", "CODE_ADMIN", mv);
    return mv;
  }

}
