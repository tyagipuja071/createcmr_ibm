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
import com.ibm.cio.cmr.request.model.code.DefaultApprovalConditionsModel;
import com.ibm.cio.cmr.request.model.code.DefaultApprovalModel;
import com.ibm.cio.cmr.request.model.code.DefaultApprovalRecipentsModel;
import com.ibm.cio.cmr.request.model.code.FieldInfoModel;
import com.ibm.cio.cmr.request.service.code.DefaultApprovalConditionsService;
import com.ibm.cio.cmr.request.service.code.DefaultApprovalMaintService;
import com.ibm.cio.cmr.request.service.code.DefaultApprovalRecipientService;
import com.ibm.cio.cmr.request.service.code.DefaultApprovalService;
import com.ibm.cio.cmr.request.user.AppUser;
import com.ibm.cio.cmr.request.util.MessageUtil;

/**
 * @author Jeffrey Zamora
 * 
 */
@Controller
public class DefaultApprovalsController extends BaseController {

  @Autowired
  private DefaultApprovalService service;

  @Autowired
  private DefaultApprovalMaintService maintService;

  @Autowired
  private DefaultApprovalRecipientService recipientService;
  @Autowired
  private DefaultApprovalConditionsService conditionService;

  private static final Logger LOG = Logger.getLogger(DefaultApprovalsController.class);

  @RequestMapping(value = "/code/defaultappr", method = RequestMethod.GET)
  public @ResponseBody
  ModelAndView showDefaultApprovals(HttpServletRequest request, ModelMap model) {
    AppUser user = AppUser.getUser(request);
    if (!user.isAdmin() && !user.isCmde()) {
      LOG.warn("User " + user.getIntranetId() + " (" + user.getBluePagesName() + ") tried accessing the Fields system function.");
      ModelAndView mv = new ModelAndView("noaccess", "appr", new DefaultApprovalModel());
      return mv;
    }

    // access granted
    ModelAndView mv = new ModelAndView("defaultapprlist", "appr", new DefaultApprovalModel());
    setPageKeys("ADMIN", "CODE_ADMIN", mv);
    return mv;
  }

  @RequestMapping(value = "/code/defaultapprdetails")
  public @ResponseBody
  ModelAndView showDefaultApprovalDetails(HttpServletRequest request, HttpServletResponse response, DefaultApprovalModel model) throws CmrException {
    AppUser user = AppUser.getUser(request);
    if (!user.isAdmin() && !user.isCmde()) {
      LOG.warn("User " + user.getIntranetId() + " (" + user.getBluePagesName() + ") tried accessing the Fields system function.");
      ModelAndView mv = new ModelAndView("noaccess", "appr", new DefaultApprovalModel());
      return mv;
    }

    ModelAndView mv = null;
    if (model.allKeysAssigned() || shouldProcess(model)) {
      if (shouldProcess(model)) {
        try {

          DefaultApprovalModel newModel = maintService.save(model, request);
          String url = "/code/defaultapprdetails?defaultApprovalId=" + newModel.getDefaultApprovalId();
          mv = new ModelAndView("redirect:" + url, "appr", newModel);

          MessageUtil.setInfoMessage(mv, MessageUtil.INFO_RECORD_SAVED, model.getRecordDescription());
        } catch (Exception e) {
          mv = new ModelAndView("defaultapprdetails", "appr", model);
          setError(e, mv);
        }
      } else {
        long defaultApprovalId = -1;
        try {

          defaultApprovalId = Long.parseLong(request.getParameter("defaultApprovalId"));
          DefaultApprovalModel temp = new DefaultApprovalModel();
          temp.setDefaultApprovalId(defaultApprovalId);
          List<DefaultApprovalModel> records = maintService.search(temp, request);
          if (records != null && records.size() > 0) {
            DefaultApprovalModel record = records.get(0);
            record.setState(BaseModel.STATE_EXISTING);
            mv = new ModelAndView("defaultapprdetails", "appr", record);
            setPageKeys("ADMIN", "CODE_ADMIN", mv);
            return mv;
          }
        } catch (Exception e) {
        }
        mv = new ModelAndView("redirect:/code/defaultappr");
        setError(new Exception("Cannot locate default approval record."), mv);
        setPageKeys("ADMIN", "CODE_ADMIN", mv);
        return mv;
      }
    } else {
      mv = new ModelAndView("defaultapprdetails", "appr", new DefaultApprovalModel());
    }

    setPageKeys("ADMIN", "CODE_ADMIN", mv);
    return mv;

  }

  @RequestMapping(value = "/defaultapprlist", method = { RequestMethod.POST, RequestMethod.GET })
  public ModelMap getDefaultApprovals(HttpServletRequest request, HttpServletResponse response, DefaultApprovalModel model) throws CmrException {

    List<DefaultApprovalModel> results = service.search(model, request);
    return wrapAsSearchResult(results);
  }

  @RequestMapping(value = "/code/defaultappr/recipients", method = { RequestMethod.POST })
  public ModelMap processRecipient(HttpServletRequest request, HttpServletResponse response, DefaultApprovalRecipentsModel model) throws CmrException {
    ModelMap returnMap = new ModelMap();
    try {
      recipientService.processTransaction(model, request);
      returnMap.put("success", true);
    } catch (Exception e) {
      returnMap.put("success", false);
      returnMap.put("error", e.getMessage());
    }
    return returnMap;
  }

  @RequestMapping(value = "/code/defaultappr/conditions", method = { RequestMethod.POST })
  public ModelMap processCondition(HttpServletRequest request, HttpServletResponse response, DefaultApprovalConditionsModel model)
      throws CmrException {
    ModelMap returnMap = new ModelMap();
    try {
      conditionService.processTransaction(model, request);
      returnMap.put("success", true);
      returnMap.put("sequence", model.getSequenceNo());
    } catch (Exception e) {
      returnMap.put("success", false);
      returnMap.put("error", e.getMessage());
    }
    return returnMap;
  }

  @RequestMapping(value = "/code/defaultappr/process")
  public @ResponseBody
  ModelAndView processMassAction(HttpServletRequest request, HttpServletResponse response, DefaultApprovalModel model) throws CmrException {
    AppUser user = AppUser.getUser(request);
    if (!user.isAdmin() && !user.isCmde()) {
      LOG.warn("User " + user.getIntranetId() + " (" + user.getBluePagesName() + ") tried accessing the field Maintenance system function.");
      ModelAndView mv = new ModelAndView("noaccessfieldinfo", "fields", new FieldInfoModel());
      return mv;
    }

    ModelAndView mv = null;
    if (shouldProcess(model) && isMassProcess(model)) {
      try {
        maintService.processTransaction(model, request);
        mv = new ModelAndView("redirect:/code/defaultappr", "appr", model);
        if ("MASS_DELETE".equals(model.getMassAction())) {
          MessageUtil.setInfoMessage(mv, "Record(s) deleted successfully.");
        } else {
          MessageUtil.setInfoMessage(mv, "No action to perform.");
        }
      } catch (Exception e) {
        mv = new ModelAndView("redirect:/code/defaultappr", "appr", model);
        MessageUtil.setErrorMessage(mv, (e instanceof CmrException) ? ((CmrException) e).getCode() : MessageUtil.ERROR_GENERAL);
      }
    } else {
      mv = new ModelAndView("redirect:/code/defaultappr", "appr", model);
    }
    setPageKeys("ADMIN", "CODE_ADMIN", mv);
    return mv;
  }

}
