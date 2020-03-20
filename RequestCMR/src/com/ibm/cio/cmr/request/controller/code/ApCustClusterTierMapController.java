/**
 * 
 */
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
import com.ibm.cio.cmr.request.model.code.ApClusterMaintModel;
import com.ibm.cio.cmr.request.model.code.ApClusterModel;
import com.ibm.cio.cmr.request.service.code.ApClusterMaintService;
import com.ibm.cio.cmr.request.service.code.ApClusterService;
import com.ibm.cio.cmr.request.user.AppUser;
import com.ibm.cio.cmr.request.util.MessageUtil;

/**
 * 
 * @author PoojaTyagi
 *
 */

@Controller
public class ApCustClusterTierMapController extends BaseController {

  @Autowired
  private ApClusterService apClusterService;

  @Autowired
  private ApClusterMaintService apClusterMaintService;

  private static final Logger LOG = Logger.getLogger(SalesBoController.class);

  @RequestMapping(value = "/code/apCluster", method = RequestMethod.GET)
  public @ResponseBody ModelAndView showSboMaint(HttpServletRequest request, ModelMap model) {
    AppUser user = AppUser.getUser(request);
    if (!user.isAdmin()) {
      LOG.warn("User " + user.getIntranetId() + " (" + user.getBluePagesName() + ") tried accessing the Sales BO Maintenance function.");
      ModelAndView mv = new ModelAndView("noaccess", "apClusterModel", new ApClusterModel());
      return mv;
    }

    // access granted
    ModelAndView mv = new ModelAndView("apClusterModel", "apClusterModel", new ApClusterModel());
    setPageKeys("ADMIN", "CODE_ADMIN", mv);
    return mv;
  }

  @RequestMapping(value = "/code/apClusterMaint")
  public @ResponseBody ModelAndView maintainSalesBo(HttpServletRequest request, HttpServletResponse response, ApClusterModel model)
      throws CmrException {
    AppUser user = AppUser.getUser(request);
    if (!user.isAdmin()) {
      LOG.warn("User " + user.getIntranetId() + " (" + user.getBluePagesName() + ") tried accessing the Sales BO maintenance function.");
      ModelAndView mv = new ModelAndView("noaccess", "apClusterModel", new ApClusterModel());
      return mv;
    }

    ModelAndView mv = null;

    if (StringUtils.isNotEmpty(model.getIssuingCntry())) {
      if (shouldProcess(model)) {
        try {

          ApClusterModel newModel = apClusterMaintService.save(model, request);
          String url = "/code/apClusterMaint?issuingCntry=" + newModel.getIssuingCntry();
          mv = new ModelAndView("redirect:" + url, "apClusterMaintModel", newModel);
          MessageUtil.setInfoMessage(mv, MessageUtil.INFO_RECORD_SAVED, model.getRecordDescription());
        } catch (Exception e) {
          mv = new ModelAndView("apClusterMaintModel", "apClusterMaintModel", model);
          setError(e, mv);
        }
      } else {
        ApClusterModel currentModel = new ApClusterModel();
        List<ApClusterModel> current = apClusterMaintService.search(model, request);
        if (current != null && current.size() > 0) {
          currentModel = current.get(0);
        }
        if (StringUtils.isNotBlank(currentModel.getIssuingCntry())) {
          mv = new ModelAndView("apClusterMaintModel", "apClusterMaintModel", currentModel);
        }
      }
    }
    if (mv == null) {
      mv = new ModelAndView("apClusterMaintModel", "apClusterMaintModel", new ApClusterModel());
    }
    setPageKeys("ADMIN", "CODE_ADMIN", mv);
    return mv;
  }

  @RequestMapping(value = "/code/apClusterList", method = { RequestMethod.POST, RequestMethod.GET })
  public ModelMap getAPClusterList(HttpServletRequest request, HttpServletResponse response, ApClusterModel model) throws CmrException {
    List<ApClusterModel> results = apClusterService.search(model, request);
    return wrapAsSearchResult(results);
  }

  @RequestMapping(value = "/code/apCluster/process", method = { RequestMethod.POST, RequestMethod.GET })
  public ModelMap processAPCluster(HttpServletRequest request, HttpServletResponse response, ApClusterModel model) throws CmrException {
    ModelMap map = new ModelMap();
    try {
      if (model.getAction().equals("REMOVE_MAPPINGS")) {
        apClusterService.processTransaction(model, request);
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

  @RequestMapping(value = "/code/apClusterMaint/process", method = { RequestMethod.POST, RequestMethod.GET })
  public ModelMap processSalesBoMaint(HttpServletRequest request, HttpServletResponse response, ApClusterMaintModel model) throws CmrException {
    ModelMap map = new ModelMap();
    apClusterMaintService.setMaintModel(model);
    try {
      if (StringUtils.isNotBlank(model.getMassAction())) {
        apClusterMaintService.processTransaction(new ApClusterModel(), request);
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
