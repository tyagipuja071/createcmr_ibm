package com.ibm.cio.cmr.request.controller.requestentry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.ibm.cio.cmr.request.model.requestentry.CmtModel;
import com.ibm.cio.cmr.request.service.requestentry.CommentLogService;

@Controller
public class CommentLogController {

  @Autowired
  private CommentLogService service;

  @RequestMapping(value = "/addcomment", method = { RequestMethod.POST, RequestMethod.GET })
  public ModelMap checkTokenStatus(HttpServletRequest request, HttpServletResponse response) {
    ModelMap map = new ModelMap();
    String comment = request.getParameter("comment");
    String reqId = request.getParameter("reqId");

    if (StringUtils.isEmpty(comment) || StringUtils.isEmpty(reqId)) {
      map.addAttribute("success", false);
      map.addAttribute("msg", "One or more parameters is missing.");
    } else {
      CmtModel model = new CmtModel();
      model.setReqId(Long.parseLong(reqId));
      model.setCmt(comment);
      try {
        service.processTransaction(model, request);
        map.addAttribute("success", true);
        map.addAttribute("msg", "");
      } catch (Exception e) {
        map.addAttribute("success", false);
        map.addAttribute("msg", "An error occurred while adding the comment to the request. Please contact your system administrator.");
      }
    }
    return map;
  }
}
