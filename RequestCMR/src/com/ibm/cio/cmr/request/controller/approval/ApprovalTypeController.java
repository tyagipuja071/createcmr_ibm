/**
 * 
 */
package com.ibm.cio.cmr.request.controller.approval;

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
import com.ibm.cio.cmr.request.model.approval.ApprovalTypeModel;
import com.ibm.cio.cmr.request.service.approval.ApprovalTypeService;
import com.ibm.cio.cmr.request.user.AppUser;
import com.ibm.cio.cmr.request.util.MessageUtil;

/**
 * @author JeffZAMORA
 *
 */
@Controller
public class ApprovalTypeController extends BaseController {

  private static final Logger LOG = Logger.getLogger(ApprovalTypeController.class);

  @Autowired
  private ApprovalTypeService service;

  @RequestMapping(
      value = "/code/approval_types",
      method = RequestMethod.GET)
  public @ResponseBody ModelAndView showApprovalTypes(HttpServletRequest request, ModelMap model) {
    AppUser user = AppUser.getUser(request);
    if (user == null || (!user.isAdmin() && !user.isCmde())) {
      LOG.warn("User " + user.getIntranetId() + " (" + user.getBluePagesName() + ") tried accessing the Fields system function.");
      ModelAndView mv = new ModelAndView("noaccess", "typ", new ApprovalTypeModel());
      return mv;
    }

    // access granted
    ModelAndView mv = new ModelAndView("approval_types", "typ", new ApprovalTypeModel());
    setPageKeys("ADMIN", "CODE_ADMIN", mv);
    return mv;
  }

  @RequestMapping(
      value = "/code/approval_type_details")
  public @ResponseBody ModelAndView showDefaultApprovalDetails(HttpServletRequest request, HttpServletResponse response, ApprovalTypeModel model)
      throws CmrException {
    AppUser user = AppUser.getUser(request);
    if (!user.isAdmin() && !user.isCmde()) {
      LOG.warn("User " + user.getIntranetId() + " (" + user.getBluePagesName() + ") tried accessing the Fields system function.");
      ModelAndView mv = new ModelAndView("noaccess", "typ", new ApprovalTypeModel());
      return mv;
    }

    ModelAndView mv = null;
    if (model.allKeysAssigned() || shouldProcess(model)) {
      if (shouldProcess(model)) {
        try {

          ApprovalTypeModel newModel = service.save(model, request);
          String url = "/code/approval_type_details?typId=" + newModel.getTypId() + "&geoCd=" + newModel.getGeoCd();
          if (BaseModel.ACT_DELETE.equals(model.getAction())) {
            url = "/code/approval_types";
          }
          mv = new ModelAndView("redirect:" + url, "typ", newModel);
          if (BaseModel.ACT_DELETE.equals(model.getAction())) {
            MessageUtil.setInfoMessage(mv, MessageUtil.INFO_RECORD_DELETED, model.getRecordDescription());
          } else {
            MessageUtil.setInfoMessage(mv, MessageUtil.INFO_RECORD_SAVED, model.getRecordDescription());
          }

        } catch (Exception e) {
          mv = new ModelAndView("approval_type_det", "typ", model);
          setError(e, mv);
        }
      } else {
        int typId = -1;
        String geoCd = null;
        try {

          typId = Integer.parseInt(request.getParameter("typId"));
          geoCd = request.getParameter("geoCd");
          ApprovalTypeModel temp = new ApprovalTypeModel();
          temp.setTypId(typId);
          temp.setGeoCd(geoCd);
          List<ApprovalTypeModel> records = service.search(temp, request);
          if (records != null && records.size() > 0) {
            ApprovalTypeModel record = records.get(0);
            record.setState(BaseModel.STATE_EXISTING);
            mv = new ModelAndView("approval_type_det", "typ", record);
            setPageKeys("ADMIN", "CODE_ADMIN", mv);
            return mv;
          }
        } catch (Exception e) {
        }
        mv = new ModelAndView("redirect:/code/approval_type_det");
        setError(new Exception("Cannot locate approval type record."), mv);
        setPageKeys("ADMIN", "CODE_ADMIN", mv);
        return mv;
      }
    } else {
      mv = new ModelAndView("approval_type_det", "typ", new ApprovalTypeModel());
    }

    setPageKeys("ADMIN", "CODE_ADMIN", mv);
    return mv;

  }

  @RequestMapping(
      value = "/approval_types_list",
      method = { RequestMethod.POST, RequestMethod.GET })
  public ModelMap getDefaultApprovals(HttpServletRequest request, HttpServletResponse response, ApprovalTypeModel model) throws CmrException {

    List<ApprovalTypeModel> results = service.search(model, request);
    return wrapAsSearchResult(results);
  }

}
