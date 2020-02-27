/**
 * 
 */
package com.ibm.cio.cmr.request.controller.code;

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
import com.ibm.cio.cmr.request.model.ParamContainer;
import com.ibm.cio.cmr.request.model.ProcessResultModel;
import com.ibm.cio.cmr.request.model.code.FieldInfoModel;
import com.ibm.cio.cmr.request.model.code.ScenariosModel;
import com.ibm.cio.cmr.request.service.code.ScenariosService;
import com.ibm.cio.cmr.request.user.AppUser;

/**
 * @author Jeffrey Zamora
 * 
 */
@Controller
public class ScenariosController extends BaseController {

  private static final Logger LOG = Logger.getLogger(ScenariosController.class);

  @Autowired
  private ScenariosService service;

  @RequestMapping(value = "/code/scenarios")
  public @ResponseBody
  ModelAndView showFields(HttpServletRequest request, ModelMap model) {
    AppUser user = AppUser.getUser(request);
    if (!user.isAdmin() && !user.isCmde()) {
      LOG.warn("User " + user.getIntranetId() + " (" + user.getBluePagesName() + ") tried accessing the Fields system function.");
      ModelAndView mv = new ModelAndView("noaccess", "fields", new FieldInfoModel());
      return mv;
    }

    // access granted
    ModelAndView mv = new ModelAndView("scenarios", "scenario", new ScenariosModel());
    setPageKeys("ADMIN", "CODE_ADMIN", mv);
    return mv;
  }

  @RequestMapping(value = "/code/scenarios/process", method = { RequestMethod.POST, RequestMethod.GET })
  public ModelMap processlov(HttpServletRequest request, HttpServletResponse response) throws CmrException {
    ProcessResultModel result = new ProcessResultModel();
    try {
      ParamContainer params = new ParamContainer();

      String action = request.getParameter("action");
      String json = request.getParameter("jsonInput");
      if (StringUtils.isEmpty(action)) {
        result.setSuccess(false);
        result.setMessage("No action to execute");
      } else if (StringUtils.isEmpty(json)) {
        result.setSuccess(false);
        result.setMessage("No JSON param to process.");
      } else {
        System.out.println("Action: " + action);
        System.out.println("Json: " + json);
        params.addParam("action", action);
        params.addParam("json", json);
        result = service.process(request, params);
      }
    } catch (Exception e) {
      result.setSuccess(false);
      result.setMessage("A general error occurred. " + e.getMessage());
    }
    return wrapAsProcessResult(result);
  }
}
