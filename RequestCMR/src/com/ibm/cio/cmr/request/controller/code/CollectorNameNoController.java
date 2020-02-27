/**
 * 
 */
package com.ibm.cio.cmr.request.controller.code;

import java.net.URLEncoder;
import java.util.List;

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
import com.ibm.cio.cmr.request.model.code.CollectorNameNoModel;
import com.ibm.cio.cmr.request.service.code.CollectorNameNoService;
import com.ibm.cio.cmr.request.user.AppUser;
import com.ibm.cio.cmr.request.util.MessageUtil;

/**
 * @author DennisNATAD
 * 
 */
@Controller
public class CollectorNameNoController extends BaseController {

  @Autowired
  private CollectorNameNoService cnnService;

  private static final Logger LOG = Logger.getLogger(CollectorNameNoController.class);

  @RequestMapping(value = "/code/collectornodef", method = RequestMethod.GET)
  public @ResponseBody
  ModelAndView showCollectorNoList(HttpServletRequest request, ModelMap model) {
    AppUser user = AppUser.getUser(request);
    if (!user.isAdmin()) {
      LOG.warn("User " + user.getIntranetId() + " (" + user.getBluePagesName() + ") tried accessing the maintain LA Collector Number function.");
      ModelAndView mv = new ModelAndView("noaccess", "collector", new CollectorNameNoModel());
      return mv;
    }

    // access granted
    ModelAndView mv = new ModelAndView("collectornodeflist", "collector", new CollectorNameNoModel());
    setPageKeys("ADMIN", "CODE_ADMIN", mv);
    return mv;
  }

  @RequestMapping(value = "/code/collectornamenomain")
  public @ResponseBody
  ModelAndView maintainCollectorNo(HttpServletRequest request, CollectorNameNoModel model) {
    AppUser user = AppUser.getUser(request);
    if (!user.isAdmin()) {
      LOG.warn("User " + user.getIntranetId() + " (" + user.getBluePagesName() + ") tried accessing the maintain LA Collector Number function.");
      ModelAndView mv = new ModelAndView("noaccess", "collector", new CollectorNameNoModel());
      return mv;
    }

    // access granted
    ModelAndView mv = null;

    if (StringUtils.isEmpty(model.getCollectorNo())) {
      model.setCollectorNo(request.getParameter("collectorno"));
    }

    if (StringUtils.isEmpty(model.getCmrIssuingCntry())) {
      model.setCmrIssuingCntry(request.getParameter("cmrIssuingCntry"));
    }

    if (model.allKeysAssigned()) {
      if (shouldProcess(model)) {
        try {

          CollectorNameNoModel newModel = cnnService.save(model, request);
          String collNo = newModel.getCollectorNo();
          String encodedFieldID = URLEncoder.encode(collNo, "UTF-8");
          String url = "/code/collectornamenomain?collNo=" + encodedFieldID + "&cmrIssuingCntry=" + newModel.getCmrIssuingCntry();
          mv = new ModelAndView("redirect:" + url, "collectornamenomain", newModel);
          MessageUtil.setInfoMessage(mv, MessageUtil.INFO_RECORD_SAVED, model.getRecordDescription());
        } catch (Exception e) {
          mv = new ModelAndView("collectornamenomain", "collNoMain", model);
          setError(e, mv);
        }
      } else {
        CollectorNameNoModel currentModel = new CollectorNameNoModel();
        List<CollectorNameNoModel> current;
        try {
          current = cnnService.search(model, request);
          if (current != null && current.size() > 0) {
            currentModel = current.get(0);
          }
          mv = new ModelAndView("collectornamenomain", "collNoMain", currentModel);
        } catch (CmrException e) {
          e.printStackTrace();
        }
      }
    }

    if (mv == null) {
      mv = new ModelAndView("collectornamenomain", "collNoMain", new CollectorNameNoModel());
    }

    setPageKeys("ADMIN", "CODE_ADMIN", mv);
    return mv;
  }

  @RequestMapping(value = "/code/collectornolist", method = { RequestMethod.POST, RequestMethod.GET })
  public ModelMap getCntryGeoDefList(HttpServletRequest request, HttpServletResponse response, CollectorNameNoModel model) throws CmrException {
    List<CollectorNameNoModel> results = cnnService.search(model, request);
    return wrapAsSearchResult(results);
  }

}
