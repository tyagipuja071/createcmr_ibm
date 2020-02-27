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
import com.ibm.cio.cmr.request.model.code.InternalDivDeptModel;
import com.ibm.cio.cmr.request.service.code.InternalDivDeptService;

/**
 * @author JeffZAMORA
 *
 */
@Controller
public class InternalDivDeptController extends BaseController {

  private static final Logger LOG = Logger.getLogger(InternalDivDeptController.class);

  @Autowired
  private InternalDivDeptService service;

  @RequestMapping(
      value = "/code/div_dept")
  public ModelAndView showDivDeptPage(HttpServletRequest request, HttpServletResponse response) throws Exception {
    LOG.debug("Displaying Div/Dept page");
    return new ModelAndView("div_dept");
  }

  @RequestMapping(
      value = "/code/div_dept/process")
  public @ResponseBody ModelMap processDivDept(HttpServletRequest request, HttpServletResponse response, @RequestBody InternalDivDeptModel model)
      throws Exception {
    LOG.debug("Displaying Div/Dept page");
    ParamContainer params = new ParamContainer();
    params.addParam("model", model);
    ProcessResultModel result = service.process(null, params);
    return wrapAsProcessResult(result);
  }
}
