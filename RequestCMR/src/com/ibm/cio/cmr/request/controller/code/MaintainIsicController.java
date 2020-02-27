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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.cio.cmr.request.controller.BaseController;
import com.ibm.cio.cmr.request.model.ParamContainer;
import com.ibm.cio.cmr.request.model.ProcessResultModel;
import com.ibm.cio.cmr.request.model.code.IsicModel;
import com.ibm.cio.cmr.request.service.code.MaintainIsicService;

/**
 * @author JeffZAMORA
 *
 */
@Controller
public class MaintainIsicController extends BaseController {

  private static final Logger LOG = Logger.getLogger(MaintainIsicController.class);

  @Autowired
  private MaintainIsicService service;

  @RequestMapping(value = "/code/isic")
  public ModelAndView showDivDeptPage(HttpServletRequest request, HttpServletResponse response) throws Exception {
    LOG.debug("Displaying ISIC page");
    return new ModelAndView("isic", "isic", new IsicModel());
  }

  @RequestMapping(value = "/code/isic/process")
  public @ResponseBody ModelMap processDivDept(HttpServletRequest request, HttpServletResponse response, @RequestBody IsicModel model)
      throws Exception {
    LOG.debug("Processing ISIC..");
    ParamContainer params = new ParamContainer();
    params.addParam("model", model);
    ProcessResultModel result = service.process(null, params);
    return wrapAsProcessResult(result);
  }

}
