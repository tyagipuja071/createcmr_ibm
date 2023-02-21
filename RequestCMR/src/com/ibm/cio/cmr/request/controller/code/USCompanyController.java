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
import com.ibm.cio.cmr.request.model.code.USCompanyModel;
import com.ibm.cio.cmr.request.model.code.USIbmBoModel;
import com.ibm.cio.cmr.request.service.code.USCompanyMaintainService;
import com.ibm.cio.cmr.request.service.code.USCompanyService;
import com.ibm.cio.cmr.request.user.AppUser;
import com.ibm.cio.cmr.request.util.MessageUtil;

/**
 * 
 * @author Priyanka Kandhare
 *
 */

@Controller
public class USCompanyController extends BaseController {

  private static final Logger LOG = Logger.getLogger(USIbmBoController.class);

  @Autowired
  private USCompanyService service;

  @Autowired
  private USCompanyMaintainService maintainService;

  @RequestMapping(value = "/code/us_company", method = RequestMethod.GET)
  public @ResponseBody ModelAndView showUSCompany(HttpServletRequest request, ModelMap model) {
    AppUser user = AppUser.getUser(request);
    if (!user.isAdmin() && !user.isCmde()) {
      LOG.warn("User " + user.getIntranetId() + " (" + user.getBluePagesName() + ") tried accessing the US COMPANY Maintenance system function.");
      ModelAndView mv = new ModelAndView("noaccess", "us_company", new USCompanyModel());
      return mv;
    }

    // access granted
    ModelAndView mv = new ModelAndView("uscompany", "us_company", new USCompanyModel());
    setPageKeys("ADMIN", "CODE_ADMIN", mv);
    return mv;

  }

  @RequestMapping(value = "/code/us_company_list", method = { RequestMethod.GET, RequestMethod.POST })
  public ModelMap getUSCompanyList(HttpServletRequest request, HttpServletResponse response, USCompanyModel model) throws CmrException {

    List<USCompanyModel> results = service.search(model, request);
    return wrapAsSearchResult(results);

  }

  @RequestMapping(value = "/code/us_company_form", method = { RequestMethod.GET, RequestMethod.POST })
  public ModelAndView usCompanyMaintenance(HttpServletRequest request, HttpServletResponse response, USCompanyModel model) throws CmrException {

    AppUser user = AppUser.getUser(request);
    if (!user.isAdmin() && !user.isCmde()) {
      LOG.warn("User " + user.getIntranetId() + " (" + user.getBluePagesName() + ") tried accessing the US COMPANY Maintenance system function.");
      ModelAndView mv = new ModelAndView("noaccess", "us_company", new USIbmBoModel());
      return mv;
    }

    ModelAndView mv = null;

    if ("I".equals(model.getAction())) {
      model.setAction(BaseModel.ACT_INSERT);
      model.setState(BaseModel.STATE_NEW);
    }

    if ("U".equals(model.getAction())) {
      model.setAction(BaseModel.ACT_UPDATE);
      model.setState(BaseModel.STATE_EXISTING);
    }

    if (model.allKeysAssigned()) {

      if (shouldProcess(model)) {
        try {
          maintainService.save(model, request);

          String url = "/code/us_company_form?mandt=" + model.getMandt() + "&compNo=" + String.valueOf(model.getCompNo());
          mv = new ModelAndView("redirect:" + url, "us_company", model);

          MessageUtil.setInfoMessage(mv, MessageUtil.INFO_RECORD_SAVED, model.getRecordDescription());
        } catch (Exception e) {
          mv = new ModelAndView("uscompanyform", "us_company", model);
          setError(e, mv);
        }
      } else {
        USCompanyModel uSCompanyModel = new USCompanyModel();
        List<USCompanyModel> current = maintainService.search(model, request);

        if (current != null && current.size() > 0) {
          uSCompanyModel = current.get(0);
        }

        mv = new ModelAndView("uscompanyform", "us_company", uSCompanyModel);
      }
    }

    if (mv == null) {
      mv = new ModelAndView("uscompanyform", "us_company", new USCompanyModel());
    }

    setPageKeys("ADMIN", "CODE_ADMIN", mv);
    return mv;

  }

}
