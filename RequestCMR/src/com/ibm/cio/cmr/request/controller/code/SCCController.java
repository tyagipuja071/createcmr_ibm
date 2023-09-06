/**
 * 
 */
package com.ibm.cio.cmr.request.controller.code;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.controller.BaseController;
import com.ibm.cio.cmr.request.model.BaseModel;
import com.ibm.cio.cmr.request.model.code.SCCModel;
import com.ibm.cio.cmr.request.service.code.SCCMaintainService;
import com.ibm.cio.cmr.request.service.code.SCCService;
import com.ibm.cio.cmr.request.user.AppUser;
import com.ibm.cio.cmr.request.util.MessageUtil;
import com.ibm.cio.cmr.request.util.SystemParameters;
import com.ibm.cio.cmr.request.util.mail.Email;
import com.ibm.cio.cmr.request.util.mail.MessageType;

/**
 * @author Sonali Jain
 * 
 */
@Controller
public class SCCController extends BaseController {
  @Autowired
  private SCCService service;

  @Autowired
  private SCCMaintainService maintService;

  private static final Logger LOG = Logger.getLogger(SCCController.class);

  @RequestMapping(value = "/code/scclist", method = RequestMethod.GET)
  public @ResponseBody ModelAndView showSCC(HttpServletRequest request, ModelMap model) {
    String taxTeamFlag = request.getParameter("taxTeamFlag");

    if ("Y".equals(taxTeamFlag)) {
      // AppUser user = AppUser.getUser(request);
      // if (!user.isAdmin() && !user.isCmde()) {
      // LOG.warn("User " + user.getIntranetId() + " (" +
      // user.getBluePagesName() + ") tried accessing the Fields system
      // function.");
      // ModelAndView mv = new ModelAndView("noaccess", "scc", new SCCModel());
      // return mv;
      // }

      // access granted
      ModelAndView mv = new ModelAndView("scclist", "scc", new SCCModel());
      setPageKeys("SEARCH_HOME", "USSCC", mv);
      return mv;
    } else {
      AppUser user = AppUser.getUser(request);
      if (!user.isAdmin() && !user.isCmde()) {
        LOG.warn("User " + user.getIntranetId() + " (" + user.getBluePagesName() + ") tried accessing the Fields system function.");
        ModelAndView mv = new ModelAndView("noaccess", "scc", new SCCModel());
        return mv;
      }

      // access granted
      ModelAndView mv = new ModelAndView("scclist", "scc", new SCCModel());
      setPageKeys("ADMIN", "CODE_ADMIN", mv);
      return mv;

    }
  }

  @RequestMapping(value = "/code/delete", method = { RequestMethod.POST, RequestMethod.GET })
  public ModelAndView deleteSCC(HttpServletRequest request, HttpServletResponse response, SCCModel model) throws CmrException {
    model.setAction(BaseModel.ACT_DELETE);
    model.setState(BaseModel.STATE_EXISTING);

    AppUser user = AppUser.getUser(request);
    if (!user.isAdmin() && !user.isCmde()) {
      LOG.warn("User " + user.getIntranetId() + " (" + user.getBluePagesName() + ") tried accessing the field Maintenance system function.");
      ModelAndView mv = new ModelAndView("noaccessfieldinfo", "fields", new SCCModel());
      return mv;
    }

    ModelAndView mv = null;
    if (model.allKeysAssigned()) {
      if (shouldProcess(model)) {
        try {
          // CREATCMR-5627
          SCCModel currentModel = new SCCModel();
          List<SCCModel> current = maintService.search(model, request);
          if (current != null && current.size() > 0) {
            currentModel = current.get(0);
          }
          sendMail(model.getAction(), currentModel);
          // CREATCMR-5627

          maintService.save(model, request);

          mv = new ModelAndView("scclist", "scc", new SCCModel());
          MessageUtil.setInfoMessage(mv, MessageUtil.INFO_RECORD_DELETED, model.getRecordDescription());
        } catch (Exception e) {
          mv = new ModelAndView("scclist", "scc", new SCCModel());
          setError(e, mv);
        }
      }
    }
    setPageKeys("ADMIN", "CODE_ADMIN", mv);
    return mv;
  }

  @RequestMapping(value = "/scclist", method = { RequestMethod.POST, RequestMethod.GET })
  public ModelMap getSCCList(HttpServletRequest request, HttpServletResponse response, SCCModel model) throws CmrException {

    List<SCCModel> results = service.search(model, request);
    return wrapAsSearchResult(results);
  }

  @RequestMapping(value = "/code/sccdetails")
  public @ResponseBody ModelAndView sccMaintenance(HttpServletRequest request, HttpServletResponse response, SCCModel model) throws CmrException {
    AppUser user = AppUser.getUser(request);
    if (!user.isAdmin() && !user.isCmde()) {
      LOG.warn("User " + user.getIntranetId() + " (" + user.getBluePagesName() + ") tried accessing the field Maintenance system function.");
      ModelAndView mv = new ModelAndView("noaccessfieldinfo", "fields", new SCCModel());
      return mv;
    }

    ModelAndView mv = null;
    if (model.allKeysAssigned()) {
      if (shouldProcess(model)) {
        try {
          SCCModel newModel = maintService.save(model, request);
          String url = "";
          if (BaseModel.ACT_INSERT.equals(model.getAction())) {
            url = "/code/sccdetails";
          } else {
            url = "/code/sccdetails?sccId=" + String.valueOf(newModel.getSccId());
          }

          // CREATCMR-5627
          sendMail(model.getAction(), model);
          // CREATCMR-5627

          mv = new ModelAndView("redirect:" + url, "scc", newModel);

          MessageUtil.setInfoMessage(mv, MessageUtil.INFO_RECORD_SAVED, model.getRecordDescription());
        } catch (Exception e) {
          mv = new ModelAndView("sccdetails", "scc", model);
          setError(e, mv);
        }
      } else {
        SCCModel currentModel = new SCCModel();
        List<SCCModel> current = maintService.search(model, request);
        if (current != null && current.size() > 0) {
          currentModel = current.get(0);
        }
        mv = new ModelAndView("sccdetails", "scc", currentModel);
      }
    }
    if (mv == null) {
      mv = new ModelAndView("sccdetails", "scc", new SCCModel());
    }

    setPageKeys("ADMIN", "CODE_ADMIN", mv);
    return mv;
  }

  @RequestMapping(value = "/code/scc/process")
  public @ResponseBody ModelAndView processMassAction(HttpServletRequest request, HttpServletResponse response, SCCModel model) throws CmrException {
    AppUser user = AppUser.getUser(request);
    if (!user.isAdmin() && !user.isCmde()) {
      LOG.warn("User " + user.getIntranetId() + " (" + user.getBluePagesName() + ") tried accessing the SCC Maintenance system function.");
      ModelAndView mv = new ModelAndView("noaccessfieldinfo", "fields", new SCCModel());
      return mv;
    }

    ModelAndView mv = null;
    if (shouldProcess(model) && isMassProcess(model)) {
      try {
        service.processTransaction(model, request);
        mv = new ModelAndView("redirect:/code/sccdetails", "scc", model);
        if ("MASS_DELETE".equals(model.getMassAction())) {
          MessageUtil.setInfoMessage(mv, "Record(s) deleted successfully.");
        } else {
          MessageUtil.setInfoMessage(mv, "No action to perform.");
        }
      } catch (Exception e) {
        mv = new ModelAndView("redirect:/code/field_info", "fieldInfo", model);
        MessageUtil.setErrorMessage(mv, (e instanceof CmrException) ? ((CmrException) e).getCode() : MessageUtil.ERROR_GENERAL);
      }
    } else {
      mv = new ModelAndView("redirect:/code/field_info", "fieldInfo", model);
    }
    setPageKeys("ADMIN", "CODE_ADMIN", mv);
    return mv;
  }

  // CREATCMR-5627
  private void sendMail(String IUD, SCCModel sccModel) {
    try {

      String toStr = "";
      String cSt = StringUtils.leftPad("" + (int) sccModel.getcSt(), 2, "0");
      cSt = "00".equals(cSt) ? "99" : cSt;
      List<String> taxTeamList = SystemParameters.getList("US.TAX_TEAM_HEAD");

      for (String taxTeam : taxTeamList) {
        toStr += taxTeam.trim().toLowerCase().toString() + ", ";
      }
      toStr = toStr.trim().substring(0, toStr.trim().length() - 1);

      String host = SystemConfiguration.getValue("MAIL_HOST");

      Email mail = new Email();
      String from = SystemConfiguration.getValue("MAIL_FROM");

      mail.setSubject("Notification: SCC table in CreateCMR has been maintained.");
      mail.setTo(toStr);
      mail.setFrom(from);
      mail.setType(MessageType.HTML);

      StringBuffer sb = new StringBuffer();

      if (BaseModel.ACT_INSERT.equals(IUD) || BaseModel.ACT_UPDATE.equals(IUD)) {
        sb.append("INSERT OR UPDATE CREQCMR.US_CMR_SCC infomation.<br/><br/>");
      } else if (BaseModel.ACT_DELETE.equals(IUD)) {
        sb.append("DELETE CREQCMR.US_CMR_SCC infomation.<br/><br/>");
      }

      sb.append("<table border='1' cellpadding='0' cellspacing='0' style='border-collapse:collapse;'>");
      sb.append("<tr>");
      sb.append("<th align='left' width='100'>CNTRY</th>");
      sb.append("<th align='left' width='100'>C_ST</th>");
      sb.append("<th align='left' width='100'>C_CNTY</th>");
      sb.append("<th align='left' width='100'>C_CITY</th>");
      sb.append("<th align='left' width='100'>N_ST</th>");
      sb.append("<th align='left' width='100'>N_CNTY</th>");
      sb.append("<th align='left' width='100'>N_CITY</th>");
      sb.append("<th align='left' width='100'>ZIP</th>");
      sb.append("</tr>");
      sb.append("<tr>");
      sb.append("<td>" + sccModel.getnLand() + "</td>");
      sb.append("<td>" + cSt + "</td>");
      sb.append("<td>" + StringUtils.leftPad("" + (int) sccModel.getcCnty(), 3, "0") + "</td>");
      sb.append("<td>" + StringUtils.leftPad("" + (int) sccModel.getcCity(), 4, "0") + "</td>");
      sb.append("<td>" + sccModel.getnSt() + "</td>");
      sb.append("<td>" + sccModel.getnCnty() + "</td>");
      sb.append("<td>" + sccModel.getnCity() + "</td>");

      String newZipCode = "";
      int newZipCodeLength = (sccModel.getcZip() + "").length();

      if (newZipCodeLength < 5) {
        newZipCode = StringUtils.leftPad("" + sccModel.getcZip(), 5, "0").substring(0, 5);
      }

      if (newZipCodeLength == 5) {
        newZipCode = sccModel.getcZip() + "";
      }

      if (newZipCodeLength > 5 && newZipCodeLength <= 9) {
        newZipCode = StringUtils.leftPad("" + sccModel.getcZip(), 9, "0").substring(0, 5);
      }

      sb.append("<td>" + newZipCode + "</td>");
      sb.append("</tr>");
      sb.append("</table>");

      mail.setMessage(sb.toString());
      mail.send(host);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  // CREATCMR-5627

  // CREATCMR-9311: export the scc list to excel file
  @RequestMapping(value = "/code/exportToExcel", method = { RequestMethod.POST, RequestMethod.GET })
  public void exportSCCList(HttpServletRequest request, HttpServletResponse response, SCCModel model) throws CmrException {

    List<SCCModel> results = service.search(model, request);
    try {
      service.exportToExcel(results, response);
    } catch (Exception e) {
      LOG.debug("Cannot export Requester statistics", e);
    }
  }
  // CREATCMR-9311 end
}
