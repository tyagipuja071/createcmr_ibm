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
import org.springframework.web.servlet.ModelAndView;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.controller.BaseController;
import com.ibm.cio.cmr.request.model.ParamContainer;
import com.ibm.cio.cmr.request.model.ProcessResultModel;
import com.ibm.cio.cmr.request.model.code.GermanyDeptModel;
import com.ibm.cio.cmr.request.service.code.GermanyDeptService;

/**
 * @author Anuja Srivastava
 *
 */
@Controller
public class GermanyDeptController extends BaseController {

  @Autowired
  private GermanyDeptService germanyDeptService;

  private static final Logger LOG = Logger.getLogger(GermanyDeptController.class);

  @RequestMapping(
      value = "/code/germanyDept")
  public ModelAndView showDivDeptPage(HttpServletRequest request, HttpServletResponse response) throws Exception {
    request.getParameter("DEPT");
    LOG.debug("Displaying Dept page");
    return new ModelAndView("germanyDept");
  }

  @RequestMapping(
      value = "/code/germanyDeptList",
      method = { RequestMethod.POST, RequestMethod.GET })
  public ModelMap getRepTeamList(HttpServletRequest request, HttpServletResponse response, GermanyDeptModel model) throws CmrException {
    List<GermanyDeptModel> results = germanyDeptService.search(model, request);
    return wrapAsPlainSearchResult(results);
  }

  @RequestMapping(
      value = "/code/dept/process",
      method = { RequestMethod.POST, RequestMethod.GET })
  public ModelMap processDept(HttpServletRequest request, HttpServletResponse response, GermanyDeptModel model) throws CmrException {
    LOG.debug("Processing Dept");
    ParamContainer param = new ParamContainer();
    param.addParam("model", model);
    ProcessResultModel result = germanyDeptService.process(request, param);
    // germanyDeptService.addDepartment(model, request);
    return wrapAsProcessResult(result);
  }

}
