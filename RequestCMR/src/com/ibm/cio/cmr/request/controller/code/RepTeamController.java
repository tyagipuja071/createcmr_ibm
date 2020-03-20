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
import com.ibm.cio.cmr.request.model.code.RepTeamMaintModel;
import com.ibm.cio.cmr.request.model.code.RepTeamModel;
import com.ibm.cio.cmr.request.service.code.RepTeamMaintService;
import com.ibm.cio.cmr.request.service.code.RepTeamService;
import com.ibm.cio.cmr.request.user.AppUser;
import com.ibm.cio.cmr.request.util.MessageUtil;

/**
 * 
 * @author Anuja Srivastava
 *
 */

@Controller
public class RepTeamController extends BaseController {

  @Autowired
  private RepTeamService repTeamService;

  @Autowired
  private RepTeamMaintService repTeamMaintService;

  private static final Logger LOG = Logger.getLogger(RepTeamController.class);

  @RequestMapping(
      value = "/code/repTeam",
      method = RequestMethod.GET)
  public @ResponseBody ModelAndView showRepTeamMaint(HttpServletRequest request, ModelMap model) {
    AppUser user = AppUser.getUser(request);
    if (!user.isAdmin()) {
      LOG.warn("User " + user.getIntranetId() + " (" + user.getBluePagesName() + ") tried accessing the maintain Rep Team Table function.");
      ModelAndView mv = new ModelAndView("noaccess", "repTeamModel", new RepTeamModel());
      return mv;
    }

    // access granted
    ModelAndView mv = new ModelAndView("repTeamModel", "repTeamModel", new RepTeamModel());
    setPageKeys("ADMIN", "CODE_ADMIN", mv);
    return mv;
  }

  @RequestMapping(
      value = "/code/repTeamMaint")
  public @ResponseBody ModelAndView maintainRepTeam(HttpServletRequest request, HttpServletResponse response, RepTeamModel model)
      throws CmrException {
    AppUser user = AppUser.getUser(request);
    if (!user.isAdmin()) {
      LOG.warn("User " + user.getIntranetId() + " (" + user.getBluePagesName() + ") tried accessing the maintain Rep Team Table function.");
      ModelAndView mv = new ModelAndView("noaccess", "repTeamModel", new RepTeamModel());
      return mv;
    }

    ModelAndView mv = null;

    if (StringUtils.isNotEmpty(model.getIssuingCntry())) {

      if (shouldProcess(model)) {
        try {
          RepTeamModel newModel = repTeamMaintService.save(model, request);
          String url = "/code/repTeamMaint?issuingCntry=" + newModel.getIssuingCntry();
          mv = new ModelAndView("redirect:" + url, "repTeamMaintModel", newModel);
          MessageUtil.setInfoMessage(mv, MessageUtil.INFO_RECORD_SAVED, model.getRecordDescription());
        } catch (Exception e) {
          mv = new ModelAndView("repTeamMaintModel", "repTeamMaintModel", model);
          setError(e, mv);
        }
      } else {
        RepTeamModel currentModel = new RepTeamModel();
        List<RepTeamModel> current = repTeamMaintService.search(model, request);
        if (current != null && current.size() > 0) {
          currentModel = current.get(0);
        }
        if (StringUtils.isNotBlank(currentModel.getIssuingCntry())) {
          mv = new ModelAndView("repTeamMaintModel", "repTeamMaintModel", currentModel);
        }
      }
    }
    if (mv == null) {
      mv = new ModelAndView("repTeamMaintModel", "repTeamMaintModel", new RepTeamModel());
    }
    setPageKeys("ADMIN", "CODE_ADMIN", mv);
    return mv;
  }

  @RequestMapping(
      value = "/code/repTeamList",
      method = { RequestMethod.POST, RequestMethod.GET })
  public ModelMap getRepTeamList(HttpServletRequest request, HttpServletResponse response, RepTeamModel model) throws CmrException {
    List<RepTeamModel> results = repTeamService.search(model, request);
    return wrapAsSearchResult(results);
  }

  @RequestMapping(
      value = "/code/repTeam/process",
      method = { RequestMethod.POST, RequestMethod.GET })
  public ModelMap processSalesBo(HttpServletRequest request, HttpServletResponse response, RepTeamModel model) throws CmrException {
    ModelMap map = new ModelMap();
    // salesBoService.setValuesModel(model);
    try {
      if (model.getAction().equals("REMOVE_MAPPINGS")) {
        repTeamService.processTransaction(model, request);
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
      value = "/code/repTeamMaint/process",
      method = { RequestMethod.POST, RequestMethod.GET })
  public ModelMap processSalesBoMaint(HttpServletRequest request, HttpServletResponse response, RepTeamMaintModel model) throws CmrException {
    ModelMap map = new ModelMap();
    repTeamMaintService.setMaintModel(model);
    try {
      if (StringUtils.isNotBlank(model.getMassAction())) {
        repTeamMaintService.processTransaction(new RepTeamModel(), request);
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
