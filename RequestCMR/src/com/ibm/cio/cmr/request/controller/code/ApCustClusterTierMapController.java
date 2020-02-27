/**
 * 
 */
package com.ibm.cio.cmr.request.controller.code;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
import com.ibm.cio.cmr.request.model.BaseModel;
import com.ibm.cio.cmr.request.model.code.ApCustClusterTierMapModel;
import com.ibm.cio.cmr.request.service.code.ApCustClusterTierMapMaintService;
import com.ibm.cio.cmr.request.service.code.ApCustClusterTierMapService;
import com.ibm.cio.cmr.request.user.AppUser;
import com.ibm.cio.cmr.request.util.MessageUtil;

/**
 * @author Anuja Srivastava
 * 
 */
@Controller
public class ApCustClusterTierMapController extends BaseController {

  @Autowired
  private ApCustClusterTierMapMaintService maintService;
  @Autowired
  private ApCustClusterTierMapService service;

  private static final Logger LOG = Logger.getLogger(ApCustClusterTierMapController.class);

  @RequestMapping(value = "/code/apClusterMap", method = RequestMethod.GET)
  public @ResponseBody ModelAndView showClusterList(HttpServletRequest request, ModelMap model) {
    AppUser user = AppUser.getUser(request);
    if (!user.isAdmin() && !user.isCmde()) {
      LOG.warn("User " + user.getIntranetId() + " (" + user.getBluePagesName()
          + ") tried accessing the maintain AP_CUST_CLUSTER_TIER_MAP Table function.");
      ModelAndView mv = new ModelAndView("noaccess", "user", new ApCustClusterTierMapModel());
      return mv;
    }

    // access granted
    ModelAndView mv = new ModelAndView("apClusterMap", "apClusterMap", new ApCustClusterTierMapModel());
    setPageKeys("ADMIN", "CODE_ADMIN", mv);
    return mv;
  }

  @RequestMapping(value = "/code/apClusterMapdetails")
  public @ResponseBody ModelAndView maintainClusterMapping(HttpServletRequest request, HttpServletResponse response, ApCustClusterTierMapModel model)
      throws CmrException {
    AppUser user = AppUser.getUser(request);
    if (!user.isAdmin() && !user.isCmde()) {
      LOG.warn("User " + user.getIntranetId() + " (" + user.getBluePagesName() + ") tried accessing the AP_CUST_CLUSTER_TIER_MAP Table function.");
      ModelAndView mv = new ModelAndView("noaccess", "apClusterMapDetails", new ApCustClusterTierMapModel());
      return mv;
    }

    ModelAndView mv = null;

    if (model.allKeysAssigned()) {

      if (shouldProcess(model)) {
        try {

          ApCustClusterTierMapModel newModel = maintService.save(model, request);
          mv = new ModelAndView("redirect:/code/apClusterMap", "apClusterMap", newModel);
          // mv = new ModelAndView("test");
          MessageUtil.setInfoMessage(mv, MessageUtil.INFO_RECORD_SAVED, model.getRecordDescription());

          if (BaseModel.ACT_DELETE.equals(model.getAction())) {
            MessageUtil.setInfoMessage(mv, MessageUtil.INFO_RECORD_DELETED, model.getRecordDescription());
          } else {
            MessageUtil.setInfoMessage(mv, MessageUtil.INFO_RECORD_SAVED, model.getRecordDescription());
          }
        } catch (Exception e) {
          mv = new ModelAndView("apClusterMapdetails", "apClusterMapDetails", model);
          // mv = new ModelAndView("test");
          setError(e, mv);
        }
      } else {
        ApCustClusterTierMapModel currentModel = new ApCustClusterTierMapModel();
        List<ApCustClusterTierMapModel> current = maintService.search(model, request);
        if (current != null && current.size() > 0) {
          currentModel = current.get(0);
        }
        mv = new ModelAndView("apClusterMapdetails", "apClusterMapDetails", currentModel);
      }
    }
    if (mv == null) {
      mv = new ModelAndView("apClusterMapdetails", "apClusterMapDetails", new ApCustClusterTierMapModel());
    }

    setPageKeys("ADMIN", "CODE_ADMIN", mv);
    return mv;
  }

  @RequestMapping(value = "/code/aplist", method = { RequestMethod.POST, RequestMethod.GET })
  public ModelMap getapList(HttpServletRequest request, HttpServletResponse response, ApCustClusterTierMapModel model) throws CmrException {

    List<ApCustClusterTierMapModel> results = service.search(model, request);
    return wrapAsSearchResult(results);
  }

  @RequestMapping(value = "/code/apClusters/process", method = { RequestMethod.POST, RequestMethod.GET })
  public ModelMap processApClusters(HttpServletRequest request, HttpServletResponse response, ApCustClusterTierMapModel model) throws CmrException {
    ModelMap map = new ModelMap();
    try {
      service.processTransaction(model, request);
      if (model.getAction().equals("REMOVE_CLUSTERS")) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "Record(s) removed successfully");
        map.put("result", result);
      } else {
        map.put("success", true);
      }
    } catch (Exception e) {
      if (model.getAction().equals("REMOVE_CLUSTERS")) {
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

}
