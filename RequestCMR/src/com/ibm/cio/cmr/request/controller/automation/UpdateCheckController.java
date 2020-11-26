package com.ibm.cio.cmr.request.controller.automation;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.controller.BaseController;
import com.ibm.cio.cmr.request.model.ParamContainer;
import com.ibm.cio.cmr.request.model.automation.UpdateCheckModel;
import com.ibm.cio.cmr.request.model.requestentry.RequestEntryModel;
import com.ibm.cio.cmr.request.service.automation.UpdateCheckService;

/**
 * Controller for the automation results page
 * 
 * @author PoojaTyagi
 * 
 */
@Controller
public class UpdateCheckController extends BaseController {

  private static final Logger LOG = Logger.getLogger(DuplicateCheckController.class);

  @Autowired
  private UpdateCheckService service;

  @SuppressWarnings("unchecked")
  @RequestMapping(value = "/auto/element/updateCheck")
  public ModelMap getUpdateCheckResult(HttpServletRequest request, HttpServletResponse response, UpdateCheckModel model, RequestEntryModel reqModel)
      throws CmrException {
    ParamContainer params = new ParamContainer();
    ModelMap map = new ModelMap();
    UpdateCheckModel updtChksModel = new UpdateCheckModel();
    try {
      params.addParam("model", model);
      params.addParam("reqModel", reqModel);
      updtChksModel = this.service.process(request, params);
      map.addAttribute("updtChkModel", updtChksModel);
      if (updtChksModel != null) {
        map.addAttribute("success", true);
        map.addAttribute("onError", updtChksModel.isOnError());
        map.addAttribute("result", updtChksModel.getResult());
        map.addAttribute("rejectionMsg", updtChksModel.getRejectionMsg());
        map.addAttribute("negativeChksMsg", updtChksModel.getNegativeChksMsg());

      } else {
        map.addAttribute("success", false);
      }
    } catch (Exception e) {
      map.addAttribute("success", false);
      map.addAttribute("error", e.getMessage());
    }
    return map;
  }
}