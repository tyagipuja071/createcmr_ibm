package com.ibm.cio.cmr.request.controller.code;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.controller.BaseController;
import com.ibm.cio.cmr.request.model.code.SalesBoMaintModel;
import com.ibm.cio.cmr.request.model.code.SalesBoModel;
import com.ibm.cio.cmr.request.service.code.SalesBoMaintService;
import com.ibm.cio.cmr.request.service.code.SalesBoService;
import com.ibm.cio.cmr.request.user.AppUser;
import com.ibm.cio.cmr.request.util.MessageUtil;

/**
 * 
 * @author RoopakChugh
 *
 */

@Controller
public class SalesBoController extends BaseController {

  @Autowired
  private SalesBoService salesBoService;

  @Autowired
  private SalesBoMaintService salesBoMaintService;

  private static final Logger LOG = Logger.getLogger(SalesBoController.class);

  @RequestMapping(
      value = "/code/salesBo",
      method = RequestMethod.GET)
  public @ResponseBody ModelAndView showSboMaint(HttpServletRequest request, ModelMap model) {
    AppUser user = AppUser.getUser(request);
    if (!user.isAdmin()) {
      LOG.warn("User " + user.getIntranetId() + " (" + user.getBluePagesName() + ") tried accessing the Sales BO Maintenance function.");
      ModelAndView mv = new ModelAndView("noaccess", "salesBoModel", new SalesBoModel());
      return mv;
    }

    // access granted
    ModelAndView mv = new ModelAndView("salesBoModel", "salesBoModel", new SalesBoModel());
    setPageKeys("ADMIN", "CODE_ADMIN", mv);
    return mv;
  }

  @RequestMapping(
      value = "/code/salesBoMaint")
  public @ResponseBody ModelAndView maintainSalesBo(HttpServletRequest request, HttpServletResponse response, SalesBoModel model)
      throws CmrException {
    AppUser user = AppUser.getUser(request);
    if (!user.isAdmin()) {
      LOG.warn("User " + user.getIntranetId() + " (" + user.getBluePagesName() + ") tried accessing the Sales BO maintenance function.");
      ModelAndView mv = new ModelAndView("noaccess", "salesBoModel", new SalesBoModel());
      return mv;
    }

    ModelAndView mv = null;

    if (StringUtils.isNotEmpty(model.getIssuingCntry())) {
      if (shouldProcess(model)) {
        try {

          SalesBoModel newModel = salesBoMaintService.save(model, request);
          String url = "/code/salesBoMaint?issuingCntry=" + newModel.getIssuingCntry();
          mv = new ModelAndView("redirect:" + url, "salesBoMaintModel", newModel);
          MessageUtil.setInfoMessage(mv, MessageUtil.INFO_RECORD_SAVED, model.getRecordDescription());
        } catch (Exception e) {
          mv = new ModelAndView("salesBoMaintModel", "salesBoMaintModel", model);
          setError(e, mv);
        }
      } else {
        SalesBoModel currentModel = new SalesBoModel();
        List<SalesBoModel> current = salesBoMaintService.search(model, request);
        if (current != null && current.size() > 0) {
          currentModel = current.get(0);
        }
        if (StringUtils.isNotBlank(currentModel.getIssuingCntry())) {
          mv = new ModelAndView("salesBoMaintModel", "salesBoMaintModel", currentModel);
        }
      }
    }
    if (mv == null) {
      mv = new ModelAndView("salesBoMaintModel", "salesBoMaintModel", new SalesBoModel());
    }
    setPageKeys("ADMIN", "CODE_ADMIN", mv);
    return mv;
  }

  @RequestMapping(
      value = "/code/salesBoList",
      method = { RequestMethod.POST, RequestMethod.GET })
  public ModelMap getSalesBoList(HttpServletRequest request, HttpServletResponse response, SalesBoModel model) throws CmrException {
    List<SalesBoModel> results = salesBoService.search(model, request);
    return wrapAsSearchResult(results);
  }

  @RequestMapping(
      value = "/code/salesBo/process",
      method = { RequestMethod.POST, RequestMethod.GET })
  public ModelMap processSalesBo(HttpServletRequest request, HttpServletResponse response, SalesBoModel model) throws CmrException {
    ModelMap map = new ModelMap();
    // salesBoService.setValuesModel(model);
    try {
      if (model.getAction().equals("REMOVE_MAPPINGS")) {
        salesBoService.processTransaction(model, request);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "Record(s) removed successfully");
        map.put("result", result);
      }
    } catch (Exception e) {
      if (model.getAction().equals("REMOVE_MAPPINGS")) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("error", "Failed to remove Mapping(s).");
        map.put("result", result);
      } else {
        map.put("success", false);
        map.put("error", e.getMessage());
      }
    }
    return map;
  }

  @RequestMapping(
      value = "/code/salesBoMaint/process",
      method = { RequestMethod.POST, RequestMethod.GET })
  public ModelMap processSalesBoMaint(HttpServletRequest request, HttpServletResponse response, SalesBoMaintModel model) throws CmrException {
    ModelMap map = new ModelMap();
    salesBoMaintService.setMaintModel(model);
    try {
      if (StringUtils.isNotBlank(model.getMassAction())) {
        salesBoMaintService.processTransaction(new SalesBoModel(), request);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "Record(s) saved successfully");
        map.put("result", result);
      }
    } catch (Exception e) {
      Map<String, Object> result = new HashMap<>();
      result.put("success", true);
      result.put("error", "Failed to save record(s).");
      map.put("result", result);
    }
    return map;
  }

}
