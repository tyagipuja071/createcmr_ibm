/**
 * 
 */
package com.ibm.cio.cmr.request.controller.code;

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
import com.ibm.cio.cmr.request.model.ParamContainer;
import com.ibm.cio.cmr.request.model.ProcessResultModel;
import com.ibm.cio.cmr.request.model.code.CopyConfigModel;
import com.ibm.cio.cmr.request.model.code.FieldInfoModel;
import com.ibm.cio.cmr.request.service.code.CopyConfigService;
import com.ibm.cio.cmr.request.user.AppUser;

/**
 * @author Jeffrey Zamora
 * 
 */
@Controller
public class CopyConfigController extends BaseController {

  private static final Logger LOG = Logger.getLogger(CopyConfigController.class);

  @Autowired
  private CopyConfigService service;

  @RequestMapping(value = "/code/copy")
  public @ResponseBody
  ModelAndView openCopyConfigPage(HttpServletRequest request, ModelMap model) {
    AppUser user = AppUser.getUser(request);
    if (!user.isAdmin() && !user.isCmde()) {
      LOG.warn("User " + user.getIntranetId() + " (" + user.getBluePagesName() + ") tried accessing the Copy config system function.");
      ModelAndView mv = new ModelAndView("noaccess", "fields", new FieldInfoModel());
      return mv;
    }

    // access granted
    ModelAndView mv = new ModelAndView("copy", "copy", new CopyConfigController());
    setPageKeys("ADMIN", "CODE_ADMIN", mv);
    return mv;
  }

  @RequestMapping(value = "/code/copy/process", method = { RequestMethod.POST })
  public ModelMap processlov(HttpServletRequest request, HttpServletResponse response, CopyConfigModel model) throws CmrException {
    ProcessResultModel result = new ProcessResultModel();
    try {
      ParamContainer params = new ParamContainer();
      params.addParam("model", model);

      result = service.process(request, params);
    } catch (Exception e) {
      result.setSuccess(false);
      result.setMessage("A general error occurred. " + e.getMessage());
    }
    return wrapAsProcessResult(result);
  }

}
