/**
 * 
 */
package com.ibm.cio.cmr.request.controller.pref;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.controller.BaseController;
import com.ibm.cio.cmr.request.model.ParamContainer;
import com.ibm.cio.cmr.request.model.pref.DelegateModel;
import com.ibm.cio.cmr.request.model.pref.UserPrefCountryModel;
import com.ibm.cio.cmr.request.model.pref.UserPrefModel;
import com.ibm.cio.cmr.request.service.pref.RoleService;
import com.ibm.cio.cmr.request.service.pref.UserPrefService;
import com.ibm.cio.cmr.request.user.AppUser;
import com.ibm.cio.cmr.request.util.BluePagesHelper;
import com.ibm.cio.cmr.request.util.MessageUtil;
import com.ibm.cio.cmr.request.util.Person;

/**
 * Handles the user preference pages
 * 
 * @author Jeffrey Zamora
 * 
 */
@Controller
public class UserPreferenceController extends BaseController {

  @Autowired
  private UserPrefService service;

  @Autowired
  private RoleService roleService;

  @RequestMapping(
      value = "/preferences",
      method = { RequestMethod.POST, RequestMethod.GET })
  public ModelAndView showUserPrefPage(HttpServletRequest request, HttpServletResponse response, UserPrefModel model) throws CmrException {
    ModelAndView mv = null;

    AppUser user = AppUser.getUser(request);

    // always set to the login user's id
    if (StringUtils.isEmpty(model.getRequesterId())) {
      model.setRequesterId(AppUser.getUser(request).getIntranetId());
    }

    String mgrEmail = BluePagesHelper.getManagerEmail(AppUser.getUser(request).getUserCnum());
    if (mgrEmail != null) {
      Person mgr = BluePagesHelper.getPerson(mgrEmail);
      if (mgr != null) {
        model.setManagerName(mgr.getName());
      }
    }

    if (!shouldProcess(model)) {
      if (model.allKeysAssigned()) {
        List<UserPrefModel> results = service.search(model, request);
        if (results.size() > 0) {
          // assign this record as the model to show on the page
          mv = new ModelAndView("preferences", "pref", results.get(0));
        }
      }
    } else {
      try {
        service.save(model, request);

        if (isUpdating(model)) {

          mv = new ModelAndView("redirect:/preferences");
          model.addKeyParameters(mv);
          MessageUtil.setInfoMessage(mv, MessageUtil.INFO_RECORD_SAVED, model.getRecordDescription());

        }

        user.setCmrIssuingCntry(model.getDftIssuingCntry());
        user.setProcessingCenter(model.getProcCenterNm());
        user.setDefaultLineOfBusn(model.getDefaultLineOfBusn());
        user.setDefaultRequestRsn(model.getDefaultRequestRsn());
        user.setDefaultReqType(model.getDefaultReqType());
        user.setDefaultNoOfRecords(model.getDefaultNoOfRecords());
        user.setShowPendingOnly("Y".equals(model.getDftCounty()));
        user.setShowLatestFirst("Y".equals(model.getDftStateProv()));
        mv.addObject("pref", model);

      } catch (Exception e) {
        mv = new ModelAndView("preferences", "pref", model);
        setError(e, mv);
      }

    }
    if (mv == null) {
      mv = new ModelAndView("preferences", "pref", model);
    }

    model.setAction(null);

    setPageKeys("PREFERENCES", "PREF_SUB", mv);
    DelegateModel delModel = new DelegateModel();
    delModel.setUserId(model.getRequesterId());
    mv.addObject("del", delModel);

    UserPrefCountryModel cntry = new UserPrefCountryModel();
    cntry.setRequesterId(model.getRequesterId());
    mv.addObject("cntry", cntry);

    ParamContainer params = new ParamContainer();
    params.addParam("USER_ID", user.getIntranetId());
    List<String> roles = this.roleService.process(request, params);
    mv.addObject("roles", roles);

    return mv;
  }

}
