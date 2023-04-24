/**
 * 
 */
package com.ibm.cio.cmr.request.controller.window;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
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

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.model.ParamContainer;
import com.ibm.cio.cmr.request.model.requestentry.GeoContactInfoModel;
import com.ibm.cio.cmr.request.model.requestentry.GeoTaxInfoModel;
import com.ibm.cio.cmr.request.model.requestentry.LicenseModel;
import com.ibm.cio.cmr.request.model.window.MassDataSummaryModel;
import com.ibm.cio.cmr.request.model.window.RequestSummaryModel;
import com.ibm.cio.cmr.request.model.window.UpdatedDataModel;
import com.ibm.cio.cmr.request.model.window.UpdatedNameAddrModel;
import com.ibm.cio.cmr.request.service.window.RequestSummaryService;

/**
 * @author Jeffrey Zamora
 * 
 */
@Controller
public class RequestSummaryController extends BaseWindowController {

  @Autowired
  private RequestSummaryService service;

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
  @RequestMapping(
      value = WINDOW_URL + "/summary")
  public ModelAndView showSummary(@RequestParam("reqId") long reqId, HttpServletRequest request, HttpServletResponse response, ModelMap model)
      throws CmrException, JsonGenerationException, JsonMappingException, IOException {
    ParamContainer params = new ParamContainer();
    params.addParam("reqId", reqId);
    RequestSummaryModel summary = service.process(request, params);
    if (summary == null) {
      summary = new RequestSummaryModel();
    }
    if (summary != null && "C".equals(summary.getAdmin().getReqType())) {
      summary = service.getSoldToDetails(summary);
      boolean othraddrexist = service.checkForOtherAddr(summary.getAdmin().getId().getReqId());
      summary.setOthraddrexist(othraddrexist);
      return getWindow(new ModelAndView("summarycrt", "summary", summary), "Request Summary: Create Request (" + reqId + ")");
    } else if (summary != null && CmrConstants.REQ_TYPE_UPDATE.equals(summary.getAdmin().getReqType())) {
      return getWindow(new ModelAndView("summaryupd", "summary", summary), "Request Summary: Update Request (" + reqId + ")");
    } else if (summary != null && CmrConstants.REQ_TYPE_MASS_UPDATE.equals(summary.getAdmin().getReqType())) {
      return getWindow(new ModelAndView("summarymass", "summary", summary), "Request Summary: Mass Update Request (" + reqId + ")");
    } else if (summary != null && CmrConstants.REQ_TYPE_REACTIVATE.equals(summary.getAdmin().getReqType())) {
      return getWindow(new ModelAndView("summarymass", "summary", summary), "Request Summary: Reactivate Request (" + reqId + ")");
    } else if (summary != null && CmrConstants.REQ_TYPE_MASS_CREATE.equals(summary.getAdmin().getReqType())) {
      return getWindow(new ModelAndView("summarymass", "summary", summary), "Request Summary: Mass Create Request (" + reqId + ")");
    } else if (summary != null && CmrConstants.REQ_TYPE_DELETE.equals(summary.getAdmin().getReqType())) {
      return getWindow(new ModelAndView("summarymass", "summary", summary), "Request Summary: Delete Request (" + reqId + ")");
    } else if (summary != null && CmrConstants.REQ_TYPE_UPDT_BY_ENT.equals(summary.getAdmin().getReqType())) {
      return getWindow(new ModelAndView("summarymass", "summary", summary), "Request Summary: Update by  Enterprise# Request (" + reqId + ")");
    } else {
      return getWindow(new ModelAndView("summarymass", "summary", summary), "Request Summary: Unspecified Request Type (" + reqId + ")");
    }

  }

  @RequestMapping(
      value = WINDOW_URL + "/dplsummary")
  public ModelAndView showDPLSummary(@RequestParam("reqId") long reqId, HttpServletRequest request, HttpServletResponse response, ModelMap model)
      throws CmrException, JsonGenerationException, JsonMappingException, IOException {
    ParamContainer params = new ParamContainer();
    params.addParam("reqId", reqId);
    RequestSummaryModel summary = service.process(request, params);
    if (summary == null) {
      summary = new RequestSummaryModel();
    }
    return getWindow(new ModelAndView("summarydplmass", "summary", summary), "Request Summary: Mass Update DPL Summary (" + reqId + ")");
  }

  @RequestMapping(
      value = "/updated/nameaddr",
      method = { RequestMethod.POST, RequestMethod.GET })
  public ModelMap getUpdateNameAddressList(HttpServletRequest request, HttpServletResponse response, @RequestParam("reqId") long reqId)
      throws CmrException {

    List<UpdatedNameAddrModel> updatedList = service.getUpdatedNameAddr(reqId);

    return wrapAsPlainSearchResult(updatedList);
  }

  @RequestMapping(
      value = "/updated/cust",
      method = { RequestMethod.POST, RequestMethod.GET })
  public ModelMap getUpdateCustomerDataList(HttpServletRequest request, HttpServletResponse response, @RequestParam("reqId") long reqId)
      throws CmrException {

    ParamContainer params = new ParamContainer();
    params.addParam("reqId", reqId);
    RequestSummaryModel summary = service.process(request, params);

    Data newData = summary.getData();

    List<UpdatedDataModel> updatedList = service.getUpdatedData(newData, reqId, "C");

    return wrapAsPlainSearchResult(updatedList);
  }

  @RequestMapping(
      value = "/updated/ibm",
      method = { RequestMethod.POST, RequestMethod.GET })
  public ModelMap getUpdateIBMDataList(HttpServletRequest request, HttpServletResponse response, @RequestParam("reqId") long reqId)
      throws CmrException {

    ParamContainer params = new ParamContainer();
    params.addParam("reqId", reqId);
    RequestSummaryModel summary = service.process(request, params);

    Data newData = summary.getData();

    List<UpdatedDataModel> updatedList = service.getUpdatedData(newData, reqId, "IBM");

    return wrapAsPlainSearchResult(updatedList);
  }

  @RequestMapping(
      value = "/summary/massprocess",
      method = { RequestMethod.POST, RequestMethod.GET })
  public ModelMap showMassRecord(HttpServletRequest request, HttpServletResponse response, @RequestParam("reqId") long reqId,
      @RequestParam("reqType") String reqType) throws CmrException {

    List<MassDataSummaryModel> massDataList;
    if (CmrConstants.REQ_TYPE_MASS_CREATE.equals(reqType)) {
      massDataList = service.getMassData(request, reqId, true, false);
    } else if (CmrConstants.REQ_TYPE_REACTIVATE.equals(reqType) || CmrConstants.REQ_TYPE_DELETE.equals(reqType)) {
      massDataList = service.getMassData(request, reqId, false, true);
    } else {
      massDataList = service.getMassData(request, reqId, false, false);
    }

    Collections.sort(massDataList);

    return wrapAsPlainSearchResult(massDataList);
  }

  @RequestMapping(
      value = "/summary/dplsummarry",
      method = { RequestMethod.POST, RequestMethod.GET })
  public ModelMap showDplSummaryRecord(HttpServletRequest request, HttpServletResponse response, @RequestParam("reqId") long reqId,
      @RequestParam("reqType") String reqType) throws CmrException {

    List<MassDataSummaryModel> massDataList;
    massDataList = service.getDplSummarryData(request, reqId);
    Collections.sort(massDataList);
    return wrapAsPlainSearchResult(massDataList);
  }

  @RequestMapping(
      value = "/summary/addlcontacts",
      method = { RequestMethod.GET, RequestMethod.POST })
  public ModelMap showAddtlConactDetails(HttpServletRequest request, HttpServletResponse response, @RequestParam("reqId") long reqId,
      @RequestParam("issuingCntry") String issuingCntry) throws CmrException {
    List<GeoContactInfoModel> contacts = new ArrayList<>();
    contacts = service.getAddlContactDetails(request, reqId, issuingCntry);
    return wrapAsPlainSearchResult(contacts);
  }

  @RequestMapping(
      value = "/summary/origAddlcontacts",
      method = { RequestMethod.GET, RequestMethod.POST })
  public ModelMap showOrigContactDetails(HttpServletRequest request, HttpServletResponse response, @RequestParam("cmr") String cmr,
      @RequestParam("issuingCntry") String issuingCntry, @RequestParam("reqId") long reqId) throws CmrException {
    List<GeoContactInfoModel> contacts = new ArrayList<>();
    contacts = service.getCurrentContactInfoDetails(String.valueOf(reqId), issuingCntry, cmr);
    return wrapAsPlainSearchResult(contacts);
  }

  @RequestMapping(
      value = "/summary/currtaxinfo",
      method = { RequestMethod.GET, RequestMethod.POST })
  public ModelMap showCurrTaxInfo(HttpServletRequest request, HttpServletResponse response, @RequestParam("reqId") long reqId,
      @RequestParam("issuingCntry") String issuingCntry) throws CmrException {
    List<GeoTaxInfoModel> currTax = new ArrayList<>();
    currTax = service.getCurrentTaxInfoDetails(reqId, issuingCntry);
    return wrapAsPlainSearchResult(currTax);
  }

  @RequestMapping(
      value = "/summary/newlicenses",
      method = { RequestMethod.GET, RequestMethod.POST })
  public ModelMap showNewLicenses(HttpServletRequest request, HttpServletResponse response, @RequestParam("reqId") long reqId) throws CmrException {
    List<LicenseModel> newLicenses = new ArrayList<>();
    newLicenses = service.getNewLicenses(request, reqId);
    return wrapAsPlainSearchResult(newLicenses);
  }
}
