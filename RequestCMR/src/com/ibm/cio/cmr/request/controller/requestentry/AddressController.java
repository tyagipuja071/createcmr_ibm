/**
 * 
 */
package com.ibm.cio.cmr.request.controller.requestentry;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.controller.BaseController;
import com.ibm.cio.cmr.request.model.ParamContainer;
import com.ibm.cio.cmr.request.model.ProcessResultModel;
import com.ibm.cio.cmr.request.model.requestentry.AddressModel;
import com.ibm.cio.cmr.request.model.requestentry.CopyAddressModel;
import com.ibm.cio.cmr.request.model.requestentry.MachineModel;
import com.ibm.cio.cmr.request.service.requestentry.AddressService;
import com.ibm.cio.cmr.request.service.requestentry.CopyAddressService;
import com.ibm.cio.cmr.request.service.requestentry.MachineService;
import com.ibm.cio.cmr.request.user.AppUser;
import com.ibm.cio.cmr.request.util.MessageUtil;

/**
 * Controller for handling request entry page functions
 * 
 * @author Sonali Jain
 * 
 */
@Controller
public class AddressController extends BaseController {

  @Autowired
  AddressService service;

  @Autowired
  private CopyAddressService copyService;

  @Autowired
  MachineService machineService;

  @RequestMapping(
      value = "/request/address/list")
  public ModelMap doSearch(HttpServletRequest request, HttpServletResponse response, AddressModel model) throws CmrException {

    List<AddressModel> results = service.search(model, request);
    ModelMap map = new ModelMap();
    map.addAttribute("items", results);
    return map;
  }

  @RequestMapping(
      value = "/request/address/process",
      method = { RequestMethod.POST, RequestMethod.GET })
  public ModelMap processAddressModal(HttpServletRequest request, HttpServletResponse response, AddressModel model) throws CmrException {

    ProcessResultModel result = new ProcessResultModel();
    try {
      SimpleDateFormat dateFormat = CmrConstants.DATE_FORMAT();
      if (model.getAddrStdTsString() != null && !"".equalsIgnoreCase(model.getAddrStdTsString())) {
        Date date = dateFormat.parse(model.getAddrStdTsString());
        model.setAddrStdTs(new Timestamp(date.getTime()));
      }
      service.processTransaction(model, request);

      String action = model.getAction();

      // set correct information message
      if ("ADD_ADDRESS".equals(action)) {
        result.setMessage(MessageUtil.getMessage(MessageUtil.INFO_ADDRESS_ADD_LIST));
      } else if ("UPDATE_ADDRESS".equals(action)) {
        result.setMessage(MessageUtil.getMessage(MessageUtil.INFO_ADDRESS_UPDATE_LIST));
      } else if ("REMOVE_ADDRESS".equals(action)) {
        result.setMessage(MessageUtil.getMessage(MessageUtil.INFO_ADDRESS_REMOVE_LIST));
      } else if ("REMOVE_ADDRESSES".equals(action)) {
        result.setMessage(MessageUtil.getMessage(MessageUtil.INFO_ADDRESSES_REMOVED));
      } /*
         * else if ("ADD_MACHINE".equals(action)) {
         * result.setMessage(MessageUtil
         * .getMessage(MessageUtil.INFO_MACHINE_ADD_LIST)); } else if
         * ("REMOVE_MACHINE".equals(action)) {
         * result.setMessage(MessageUtil.getMessage
         * (MessageUtil.INFO_MACHINE_REMOVE_LIST)); }
         */
      result.setSuccess(true);
    } catch (Exception e) {
      result.setSuccess(false);
      result.setMessage(e.getMessage());
    }

    return wrapAsProcessResult(result);
  }

  @RequestMapping(
      value = "/request/address/copydata",
      method = { RequestMethod.POST, RequestMethod.GET })
  public ModelMap copyAddressData(HttpServletRequest request, HttpServletResponse response, CopyAddressModel model) throws CmrException {

    ProcessResultModel result = new ProcessResultModel();
    try {
      ParamContainer params = new ParamContainer();
      params.addParam("model", model);
      copyService.processTransaction(model, request);
      result.setMessage(MessageUtil.getMessage(MessageUtil.INFO_ADDRESS_COPIED));
      result.setSuccess(true);
    } catch (Exception e) {
      result.setSuccess(false);
      result.setMessage(e.getMessage());
      result.setDisplayMsg(true);
    }

    return wrapAsProcessResult(result);
  }

  @RequestMapping(
      value = "/dpl/{reqId}")
  public ModelMap performDPLCheck(HttpServletRequest request, HttpServletResponse response, @PathVariable long reqId) throws Exception {
    ProcessResultModel result = new ProcessResultModel();
    try {
      AppUser user = AppUser.getUser(request);
      // service.performDPLCheck(user, reqId);
      service.performDPLCheckPerAddress(user, reqId);
      result.setSuccess(true);
      result.setMessage(MessageUtil.getMessage(MessageUtil.INFO_DPL_SUCCESS));
    } catch (Exception e) {
      result.setSuccess(false);
      result.setMessage(e.getMessage());
    }

    return wrapAsProcessResult(result);
  }

  @RequestMapping(
      value = "/request/address/filter")
  public ModelMap doSearchWithFilter(HttpServletRequest request, HttpServletResponse response, AddressModel model) throws CmrException {
    model.setFilterInd("Y");
    List<AddressModel> orgResults = service.search(model, request);
    ModelMap map = new ModelMap();
    map.addAttribute("items", orgResults);
    return map;
  }

  @RequestMapping(
      value = "/request/address/machines/list")
  public ModelMap doMachineSearch(HttpServletRequest request, HttpServletResponse response, AddressModel model) throws CmrException {

    MachineModel machineModel = new MachineModel();
    machineModel.setReqId(model.getReqId());
    machineModel.setAddrType(model.getAddrType());
    machineModel.setAddrSeq(model.getAddrSeq());
    List<MachineModel> results = machineService.search(machineModel, request);
    ModelMap map = new ModelMap();
    map.addAttribute("items", results);
    return map;
  }
}
