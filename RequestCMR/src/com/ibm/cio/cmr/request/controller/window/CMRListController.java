/**
 * 
 */
package com.ibm.cio.cmr.request.controller.window;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.model.ParamContainer;
import com.ibm.cio.cmr.request.model.requestentry.MassUpdateModel;
import com.ibm.cio.cmr.request.model.window.CMRListModel;
import com.ibm.cio.cmr.request.service.window.CMRListService;

/**
 * @author Rangoli Saxena
 * 
 */
@Controller
public class CMRListController extends BaseWindowController {

  @Autowired
  private CMRListService service;

  /**
   * 
   * @param request
   * @param response
   * @param model
   * @return
   * @throws CmrException
   * @throws IOException
   * @throws JsonMappingException
   * @throws JsonGenerationException
   */
  @RequestMapping(value = WINDOW_URL + "/showcmrList")
  public ModelAndView showCmrList(@RequestParam("reqId") long reqId, @RequestParam("reqType") String reqType, HttpServletRequest request,
      HttpServletResponse response, ModelMap model) throws CmrException, JsonGenerationException, JsonMappingException, IOException {
    ParamContainer params = new ParamContainer();
    // params.addParam("reqId", reqId);
    CMRListModel cmrListModel = service.process(request, params);
    if (cmrListModel == null) {
      cmrListModel = new CMRListModel();
    }
    if (reqType != null && "R".equals(reqType)) {
      return getWindow(new ModelAndView("showcmrList", "cmrListModel", cmrListModel), "CMR List: Reactivate Request (" + reqId + ")");
    } else {
      return getWindow(new ModelAndView("showcmrList", "cmrListModel", cmrListModel), "CMR List: Delete Request (" + reqId + ")");
    }
  }

  @RequestMapping(value = "/requestentry/showCMRList", method = { RequestMethod.POST, RequestMethod.GET })
  public ModelMap getCMRList(HttpServletRequest request, HttpServletResponse response, @RequestParam("reqId") long reqId) throws CmrException {

    List<MassUpdateModel> massUpdList = new ArrayList<>();

    MassUpdateModel massUpdtModel = new MassUpdateModel();
    massUpdtModel.setParReqId(reqId);
    massUpdList = service.getCMRList(massUpdtModel, request);
    return wrapAsPlainSearchResult(massUpdList);

  }
}
