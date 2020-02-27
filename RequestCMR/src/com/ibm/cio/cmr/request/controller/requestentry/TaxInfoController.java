package com.ibm.cio.cmr.request.controller.requestentry;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.controller.BaseController;
import com.ibm.cio.cmr.request.model.ProcessResultModel;
import com.ibm.cio.cmr.request.model.requestentry.GeoTaxInfoModel;
import com.ibm.cio.cmr.request.service.requestentry.TaxInfoService;
import com.ibm.cio.cmr.request.util.MessageUtil;

/**
 * Controller for handling request entry page functions
 * 
 * @author Garima Narang
 * 
 */
@Controller
public class TaxInfoController extends BaseController {

  @Autowired
  TaxInfoService service;

  @RequestMapping(value = "/request/taxinfo/list")
  public ModelMap doSearch(HttpServletRequest request, HttpServletResponse response, GeoTaxInfoModel model) throws CmrException {

    List<GeoTaxInfoModel> results = service.search(model, request);
    ModelMap map = new ModelMap();
    map.addAttribute("items", results);
    return map;
  }

  @RequestMapping(value = "/request/taxinfo/process", method = { RequestMethod.POST, RequestMethod.GET })
  public ModelMap processTaxInfoModal(HttpServletRequest request, HttpServletResponse response, GeoTaxInfoModel model) throws CmrException {

    ProcessResultModel result = new ProcessResultModel();

    service.processTransaction(model, request);

    String action = model.getAction();

    // set correct information message
    if ("ADD_TAXINFO".equals(action)) {
      result.setMessage(MessageUtil.getMessage(MessageUtil.INFO_TAXINFO_ADD_LIST));
    } else if ("UPDATE_TAXINFO".equals(action)) {
      result.setMessage(MessageUtil.getMessage(MessageUtil.INFO_TAXINFO_UPDATE_LIST));
    } else if ("REMOVE_TAXINFO".equals(action)) {
      result.setMessage(MessageUtil.getMessage(MessageUtil.INFO_TAXINFO_REMOVE_LIST));
    }
    result.setSuccess(true);
    return wrapAsProcessResult(result);
  }
}
