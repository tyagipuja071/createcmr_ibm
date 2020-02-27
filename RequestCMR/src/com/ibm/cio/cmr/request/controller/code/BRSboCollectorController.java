/**
 * 
 */
package com.ibm.cio.cmr.request.controller.code;

import java.util.List;

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
import com.ibm.cio.cmr.request.model.code.BRSboCollectorModel;
import com.ibm.cio.cmr.request.service.code.BRSboCollectorMaintService;
import com.ibm.cio.cmr.request.service.code.BRSboCollectorService;
import com.ibm.cio.cmr.request.user.AppUser;
import com.ibm.cio.cmr.request.util.MessageUtil;

/**
 * @author Rangoli Saxena
 * 
 */
@Controller
public class BRSboCollectorController extends BaseController {

  @Autowired
  private BRSboCollectorMaintService maintService;
  @Autowired
  private BRSboCollectorService service;

  private static final Logger LOG = Logger.getLogger(BRSboCollectorController.class);

  @RequestMapping(value = "/code/brSboCollector", method = RequestMethod.GET)
  public @ResponseBody
  ModelAndView showSboList(HttpServletRequest request, ModelMap model) {
    AppUser user = AppUser.getUser(request);
    if (!user.isAdmin() && !user.isCmde()) {
      LOG.warn("User " + user.getIntranetId() + " (" + user.getBluePagesName() + ") tried accessing the maintain BR SBO/Collector Table function.");
      ModelAndView mv = new ModelAndView("noaccess", "user", new BRSboCollectorModel());
      return mv;
    }

    // access granted
    ModelAndView mv = new ModelAndView("brSboCollector", "brSboCollectors", new BRSboCollectorModel());
    setPageKeys("ADMIN", "CODE_ADMIN", mv);
    return mv;
  }

  @RequestMapping(value = "/code/brSboCollectordetails")
  public @ResponseBody
  ModelAndView maintainSboMapping(HttpServletRequest request, HttpServletResponse response, BRSboCollectorModel model) throws CmrException {
    AppUser user = AppUser.getUser(request);
    if (!user.isAdmin() && !user.isCmde()) {
      LOG.warn("User " + user.getIntranetId() + " (" + user.getBluePagesName() + ") tried accessing the maintain BR SBO/Collector Table function.");
      ModelAndView mv = new ModelAndView("noaccess", "brSboCollecDetails", new BRSboCollectorModel());
      return mv;
    }

    ModelAndView mv = null;

    if (model.allKeysAssigned()) {

      if (shouldProcess(model)) {
        try {

          BRSboCollectorModel newModel = maintService.save(model, request);
          mv = new ModelAndView("redirect:/code/brSboCollector", "brSboCollectors", newModel);
          if (BaseModel.ACT_DELETE.equals(model.getAction())) {
            MessageUtil.setInfoMessage(mv, MessageUtil.INFO_RECORD_DELETED, model.getRecordDescription());
          } else {
            MessageUtil.setInfoMessage(mv, MessageUtil.INFO_RECORD_SAVED, model.getRecordDescription());
          }
        } catch (Exception e) {
          mv = new ModelAndView("brSboCollectordetails", "brSboCollecDetails", model);
          setError(e, mv);
        }
      } else {
        BRSboCollectorModel currentModel = new BRSboCollectorModel();
        List<BRSboCollectorModel> current = maintService.search(model, request);
        if (current != null && current.size() > 0) {
          currentModel = current.get(0);
        }
        mv = new ModelAndView("brSboCollectordetails", "brSboCollecDetails", currentModel);
      }
    }
    if (mv == null) {
      mv = new ModelAndView("brSboCollectordetails", "brSboCollecDetails", new BRSboCollectorModel());
    }

    setPageKeys("ADMIN", "CODE_ADMIN", mv);
    return mv;
  }

  @RequestMapping(value = "/code/sbolist", method = { RequestMethod.POST, RequestMethod.GET })
  public ModelMap getsboList(HttpServletRequest request, HttpServletResponse response, BRSboCollectorModel model) throws CmrException {

    List<BRSboCollectorModel> results = service.search(model, request);
    return wrapAsSearchResult(results);
  }
}
