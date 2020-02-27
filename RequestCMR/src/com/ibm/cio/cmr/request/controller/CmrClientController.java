/**
 * 
 */
package com.ibm.cio.cmr.request.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.model.ParamContainer;
import com.ibm.cio.cmr.request.model.requestentry.AddressModel;
import com.ibm.cio.cmr.request.model.requestentry.RequestEntryModel;
import com.ibm.cio.cmr.request.service.CmrClientService;
import com.ibm.cio.cmr.request.util.MessageUtil;

/**
 * @author Jeffrey Zamora
 * 
 */
@Controller
public class CmrClientController extends BaseController {

  private static final Logger LOG = Logger.getLogger(CmrClientController.class);
  @Autowired
  private CmrClientService service;

  @RequestMapping(value = "/cmrservices/{serviceType}")
  public ModelMap retrieveServiceValues(@PathVariable("serviceType") String serviceType, HttpServletRequest request, HttpServletResponse response,
      @RequestParam("reqId") long requestId, RequestEntryModel data, AddressModel addr) throws CmrException {

    ModelMap map = new ModelMap();
    ParamContainer params = new ParamContainer();
    params.addParam("reqId", requestId);
    params.addParam("serviceType", serviceType);
    params.addParam("model", map);
    params.addParam("data", data);
    params.addParam("addr", addr);
    try {
      service.process(request, params);
    } catch (Exception e) {
      if (e instanceof CmrException) {
        throw e;
      }
      LOG.error("General error occurred. ", e);
      throw new CmrException(MessageUtil.ERROR_GENERAL);
    }
    return map;
  }

  @RequestMapping(value = "/dnb")
  public ModelMap checkDnB(HttpServletRequest request, HttpServletResponse response, @RequestParam("dunsNo") String dunsNo, RequestEntryModel data,
      AddressModel addr) throws CmrException {

    ModelMap map = new ModelMap();
    ParamContainer params = new ParamContainer();
    params.addParam("serviceType", "DNB");
    params.addParam("dunsNo", dunsNo);
    params.addParam("model", map);
    try {
      service.process(request, params);
    } catch (Exception e) {
      if (e instanceof CmrException) {
        throw e;
      }
      LOG.error("General error occurred. ", e);
      throw new CmrException(MessageUtil.ERROR_GENERAL);
    }
    return map;
  }

  @RequestMapping(value = "/wtaas")
  public ModelMap checkWTAAS(HttpServletRequest request, HttpServletResponse response, @RequestParam("cmrNo") String cmrNo,
      @RequestParam("country") String country, RequestEntryModel data, AddressModel addr) throws CmrException {

    ModelMap map = new ModelMap();
    ParamContainer params = new ParamContainer();
    params.addParam("serviceType", "WTAAS");
    params.addParam("cmrNo", cmrNo);
    params.addParam("country", country);
    params.addParam("model", map);
    try {
      service.process(request, params);
    } catch (Exception e) {
      if (e instanceof CmrException) {
        throw e;
      }
      LOG.error("General error occurred. ", e);
      throw new CmrException(MessageUtil.ERROR_GENERAL);
    }
    return map;
  }

}
