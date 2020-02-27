/**
 * 
 */
package com.ibm.cio.cmr.request.controller.requestentry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.controller.BaseController;
import com.ibm.cio.cmr.request.model.ParamContainer;
import com.ibm.cio.cmr.request.service.requestentry.ExternalProcessService;
import com.ibm.cio.cmr.request.util.external.FieldContainer;

/**
 * Controller handling the external services that use CreateCMR for
 * create/update/query
 * 
 * @author JeffZAMORA
 * 
 */
@Controller
public class ExternalProcessController extends BaseController {

  @Autowired
  private ExternalProcessService service;

  @RequestMapping(
      value = "/external")
  public ModelMap getFieldDefinitions(HttpServletRequest request, HttpServletResponse response) throws Exception {

    ModelMap responseMap = new ModelMap();
    String cmrIssuingCntry = request.getParameter("cmrIssuingCntry");
    if (StringUtils.isEmpty(cmrIssuingCntry)) {
      responseMap.addAttribute("success", false);
      responseMap.addAttribute("msg", "Parameter 'cmrIssuingCntry' is required.");
      return responseMap;
    }
    String reqType = request.getParameter("reqType");
    if (StringUtils.isEmpty(reqType)) {
      responseMap.addAttribute("success", false);
      responseMap.addAttribute("msg", "Parameter 'reqType' is required.");
      responseMap.addAttribute("data", null);
      return responseMap;
    }
    String custGrp = request.getParameter("custGrp");
    String custSubGrp = request.getParameter("custSubGrp");
    String stateProv = request.getParameter("stateProv");
    String landCntry = request.getParameter("landCntry");
    String noCache = request.getParameter("noCache");

    if (CmrConstants.REQ_TYPE_CREATE.equals(reqType)) {
      if (StringUtils.isEmpty(custGrp) || StringUtils.isEmpty(custSubGrp)) {
        responseMap.addAttribute("success", false);
        responseMap.addAttribute("msg", "Parameters 'custGrp' and 'custSubGrp' are required for create requests.");
        responseMap.addAttribute("data", null);
        return responseMap;
      }
    }

    ParamContainer params = new ParamContainer();
    params.addParam("cmrIssuingCntry", cmrIssuingCntry);
    params.addParam("reqType", reqType);
    params.addParam("custGrp", custGrp);
    params.addParam("custSubGrp", custSubGrp);
    params.addParam("stateProv", stateProv);
    params.addParam("landCntry", landCntry);
    params.addParam("nocache", "Y".equals(noCache));

    FieldContainer fields = service.process(request, params);
    responseMap.addAttribute("msg", null);
    responseMap.addAttribute("data", fields);
    responseMap.addAttribute("success", true);
    return responseMap;
  }
}
