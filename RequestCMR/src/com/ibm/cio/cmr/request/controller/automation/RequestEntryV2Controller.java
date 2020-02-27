/**
 * 
 */
package com.ibm.cio.cmr.request.controller.automation;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.cio.cmr.request.controller.BaseController;
import com.ibm.cio.cmr.request.model.BaseModel;
import com.ibm.cio.cmr.request.model.auto.BaseV2RequestModel;
import com.ibm.cio.cmr.request.model.requestentry.RequestEntryModel;
import com.ibm.cio.cmr.request.service.automation.RequestEntryV2Service;
import com.ibm.cio.cmr.request.util.RequestUtils;
import com.ibm.cio.cmr.request.util.geo.GEOHandler;

/**
 * Controller class for the V2 pages on requester side
 * 
 * @author JeffZAMORA
 * 
 */
@Controller
public class RequestEntryV2Controller extends BaseController {

  private static Logger LOG = Logger.getLogger(RequestEntryV2Controller.class);

  private RequestEntryV2Service service;

  @RequestMapping(
      value = "/autoreq")
  public ModelAndView showRequestEntryPage(HttpServletRequest request, HttpServletResponse response, RequestEntryModel model) throws Exception {
    String country = model.getNewReqCntry();
    if (StringUtils.isBlank(country)) {
      country = model.getCmrIssuingCntry();
    }
    String reqType = model.getNewReqType();
    if (StringUtils.isBlank(reqType)) {
      reqType = model.getReqType();
    }
    if (!StringUtils.isBlank(country) && !RequestUtils.isRequesterAutomationEnabled(country)) {
      LOG.debug("Country " + country + " has requester automation disabled. Redirecting to old requester page..");
      return new ModelAndView("redirect:/request?newReqCntry=" + country + "&newReqType=" + reqType, "reqentry", model);
    }

    BaseModel pageModel = null;
    GEOHandler handler = RequestUtils.getGEOHandler(country);
    if (handler != null) {
      pageModel = handler.getAutomationRequestModel(country, reqType);
    }
    if (pageModel == null) {
      pageModel = new BaseV2RequestModel();
    }
    PropertyUtils.copyProperties(pageModel, model);
    return new ModelAndView("reqv2", "reqentry", pageModel);
  }

  @RequestMapping(
      value = "/auto/process")
  public ModelMap saveRequest(HttpServletRequest request, HttpServletResponse response, RequestEntryModel model) throws Exception {
    ModelMap map = new ModelMap();
    try {
      this.service = new RequestEntryV2Service();
      this.service.processTransaction(model, request);
      map.put("success", true);
      map.put("reqId", model.getReqId());
      map.put("error", null);
    } catch (Exception e) {
      map.put("success", false);
      map.put("reqId", -1);
      map.put("error", e.getMessage());
    }
    return map;
  }

}
