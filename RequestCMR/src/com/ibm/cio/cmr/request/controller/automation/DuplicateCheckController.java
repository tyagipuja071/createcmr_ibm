package com.ibm.cio.cmr.request.controller.automation;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.controller.window.BaseWindowController;
import com.ibm.cio.cmr.request.model.ParamContainer;
import com.ibm.cio.cmr.request.model.auto.BaseV2RequestModel;
import com.ibm.cio.cmr.request.model.automation.DuplicateCheckModel;
import com.ibm.cio.cmr.request.service.automation.DuplicateCheckService;

/**
 * Controller for the automation results page
 * 
 * @author PoojaTyagi
 * 
 */
@Controller
public class DuplicateCheckController extends BaseWindowController {

  private static final Logger LOG = Logger.getLogger(DuplicateCheckController.class);

  @Autowired
  private DuplicateCheckService service;

  @RequestMapping(
      value = "/auto/duplicate/reqcheck")
  public ModelMap duplicateReqCheck(HttpServletRequest request, HttpServletResponse response, DuplicateCheckModel model) throws CmrException {
    ParamContainer params = new ParamContainer();
    ModelMap map = new ModelMap();
    DuplicateCheckModel result = new DuplicateCheckModel();
    try {
      model.setAction("CHECK_FR_DUP_REQS");
      params.addParam("model", model);
      result = this.service.process(request, params);
      map.addAttribute("success", model.isSuccess());
      map.addAttribute("records", params.getParam("records"));
    } catch (Exception e) {
      result.setSuccess(false);
      map.addAttribute("success", false);
      map.addAttribute("error", e.getMessage());
      result.setMessage("A general error occurred. " + e.getMessage());
    }
    map.addAttribute("result", result);
    return map;
  }

  @SuppressWarnings("unchecked")
  @RequestMapping(
      value = "/auto/duplicate/reqslist")
  public ModelMap getDuplicateReqsList(HttpServletRequest request, HttpServletResponse response, DuplicateCheckModel model) throws CmrException {
    ParamContainer params = new ParamContainer();
    List<DuplicateCheckModel> resultList = new ArrayList<>();
    ModelMap map = new ModelMap();
    model.setAction("GET_DUP_REQS_LISTS");
    params.addParam("model", model);
    try {
      this.service.process(request, params);
      resultList = (List<DuplicateCheckModel>) params.getParam("resultList");
      map.addAttribute("resultList", resultList);
      map.addAttribute("success", true);
    } catch (Exception e) {
      map.addAttribute("success", false);
      map.addAttribute("error", e.getMessage());
    }
    return wrapAsSearchResult(resultList);
  }

  @RequestMapping(
      value = "/auto/duplicate/cmrcheck")
  public ModelMap duplicateCMRCheck(HttpServletRequest request, HttpServletResponse response, DuplicateCheckModel model) throws CmrException {
    ParamContainer params = new ParamContainer();
    ModelMap map = new ModelMap();
    DuplicateCheckModel result = new DuplicateCheckModel();
    try {
      model.setAction("CHECK_FR_DUP_CMRS");
      params.addParam("model", model);
      result = this.service.process(request, params);
      map.addAttribute("success", model.isSuccess());
      map.addAttribute("records", params.getParam("records"));
    } catch (Exception e) {
      result.setSuccess(false);
      map.addAttribute("success", false);
      map.addAttribute("error", e.getMessage());
      result.setMessage("A general error occurred. " + e.getMessage());
    }
    map.addAttribute("result", result);
    return map;
  }

  @SuppressWarnings("unchecked")
  @RequestMapping(
      value = "/auto/duplicate/cmrslist")
  public ModelMap getDuplicateCMRsList(HttpServletRequest request, HttpServletResponse response, DuplicateCheckModel model) throws CmrException {
    ParamContainer params = new ParamContainer();
    List<DuplicateCheckModel> cmrResultList = new ArrayList<>();
    ModelMap map = new ModelMap();
    model.setAction("GET_DUP_CMRS_LISTS");
    params.addParam("model", model);
    try {
      this.service.process(request, params);
      cmrResultList = (List<DuplicateCheckModel>) params.getParam("cmrResultList");
      map.addAttribute("cmrResultList", cmrResultList);
      map.addAttribute("success", true);
    } catch (Exception e) {
      map.addAttribute("success", false);
      map.addAttribute("error", e.getMessage());
    }
    return wrapAsSearchResult(cmrResultList);
  }

  @RequestMapping(
      value = "/auto/duplicate/dupcmrreason")
  public ModelMap duplicateCMRCreateReason(HttpServletRequest request, HttpServletResponse response, BaseV2RequestModel model) throws CmrException {
    ParamContainer params = new ParamContainer();
    ModelMap map = new ModelMap();
    try {
      model.setAction("DUP_CMR_RSN");
      request.setAttribute("duplicacyReason", "Y");
      params.addParam("model", model);
      this.service.process(request, params);
      map.addAttribute("success", true);
    } catch (Exception e) {
      map.addAttribute("success", false);
      map.addAttribute("error", e.getMessage());
    }
    return map;
  }

  @RequestMapping(
      value = "/auto/br_bp/bluegroups/check")
  public ModelMap bluegroupsCheck(HttpServletRequest request, HttpServletResponse response, DuplicateCheckModel model) throws CmrException {
    ParamContainer params = new ParamContainer();
    ModelMap map = new ModelMap();
    try {
      model.setAction("CHECK_FR_BLGRP");
      params.addParam("model", model);
      this.service.process(request, params);
      map.addAttribute("success", model.isSuccess());
    } catch (Exception e) {
      map.addAttribute("success", false);
      map.addAttribute("error", e.getMessage());
    }
    return map;
  }
}