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
import com.ibm.cio.cmr.request.model.code.SysParametersModel;
import com.ibm.cio.cmr.request.service.code.SysParametersAdminService;
import com.ibm.cio.cmr.request.service.code.SysParametersMaintainService;
import com.ibm.cio.cmr.request.user.AppUser;
import com.ibm.cio.cmr.request.util.MessageUtil;
import com.ibm.cio.cmr.request.util.SlackAlertsUtil;

/**
 * @author Priy Ranjan
 * 
 * 
 */

@Controller
public class SysParametersController extends BaseController {
  private static final Logger LOG = Logger.getLogger(SysParametersController.class);
  @Autowired
  private SysParametersMaintainService sysParametersMaintainService;
  @Autowired
  private SysParametersAdminService sysParametersAdminService;

  private String currentParam;

  @RequestMapping(
      value = "/code/sysparameters",
      method = RequestMethod.GET)
  public @ResponseBody ModelAndView showCountryListing(HttpServletRequest request, ModelMap model) {
    AppUser user = AppUser.getUser(request);
    if (!user.isAdmin()) {
      LOG.warn("User " + user.getIntranetId() + " (" + user.getBluePagesName() + ") tried accessing the maintain Validation_Urls Table function.");
      ModelAndView mv = new ModelAndView("noaccess", "user", new SysParametersModel());
      return mv;
    }

    // access granted
    ModelAndView mv = new ModelAndView("sysparameterlist", "sysparametersmodel", new SysParametersModel());
    setPageKeys("ADMIN", "CODE_ADMIN", mv);
    return mv;
  }

  @RequestMapping(
      value = "/code/addsysparameterpage")
  public @ResponseBody ModelAndView maintainSuppCountry(HttpServletRequest request, HttpServletResponse response, SysParametersModel model)
      throws CmrException {
    AppUser user = AppUser.getUser(request);
    if (!user.isAdmin()) {
      LOG.warn("User " + user.getIntranetId() + " (" + user.getBluePagesName() + ") tried accessing the maintain SYST_PARAMETERS Table function.");
      ModelAndView mv = new ModelAndView("noaccess", "sysparametersmodel", new SysParametersModel());
      return mv;
    }

    ModelAndView mv = null;

    if (model.allKeysAssigned()) {

      if (shouldProcess(model)) {
        try {
          if (model.isCmdeMaintIndcCheckBox()) {
            model.setCmdeMaintainableIndc("Y");
          } else {
            model.setCmdeMaintainableIndc("N");
          }

          SysParametersModel newModel = sysParametersMaintainService.save(model, request);
          mv = new ModelAndView("redirect:/code/sysparameters", "sysparametersmodel", newModel);
          if (newModel.getParameterCd().contains("XRUN")) {
            int dotIndex = newModel.getParameterCd().indexOf(".");
            String batchname = newModel.getParameterCd().substring(dotIndex + 1);
            String message = "@here Batch *" + batchname + "* switched from *" + this.currentParam + "* to *" + newModel.getParameterValue() + "* by "
                + user.getBluePagesName();
            SlackAlertsUtil.recordBatchAlert("CreateCMR", "BATCH", message);
          }
          MessageUtil.setInfoMessage(mv, MessageUtil.INFO_RECORD_SAVED, model.getRecordDescription());
        } catch (Exception e) {
          mv = new ModelAndView("addsysparameterpage", "sysparametersmodel", model);
          setError(e, mv);
        }
      } else {
        SysParametersModel currentModel = new SysParametersModel();
        List<SysParametersModel> current = sysParametersMaintainService.search(model, request);
        if (current != null && current.size() > 0) {
          currentModel = current.get(0);
          this.currentParam = currentModel.getParameterValue();
        }
        if (currentModel.getCmdeMaintainableIndc().equalsIgnoreCase("Y")) {
          currentModel.setCmdeMaintIndcCheckBox(true);
        } else {
          currentModel.setCmdeMaintIndcCheckBox(false);
        }
        mv = new ModelAndView("addsysparameterpage", "sysparametersmodel", currentModel);
      }
    }
    if (mv == null) {
      mv = new ModelAndView("addsysparameterpage", "sysparametersmodel", new SysParametersModel());
    }

    setPageKeys("ADMIN", "CODE_ADMIN", mv);
    return mv;
  }

  @RequestMapping(
      value = "/code/systemparameterlisting",
      method = { RequestMethod.POST, RequestMethod.GET })
  public ModelMap getSuppCountryListing(HttpServletRequest request, HttpServletResponse response, SysParametersModel model) throws CmrException {

    List<SysParametersModel> results = sysParametersAdminService.search(model, request);
    return wrapAsSearchResult(results);
  }

}
