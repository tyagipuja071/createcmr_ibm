/**
 * 
 */
package com.ibm.cio.cmr.request.controller.requestentry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.cio.cmr.request.model.ParamContainer;
import com.ibm.cio.cmr.request.service.requestentry.ConnectService;

/**
 * @author JeffZAMORA
 * 
 */
@Controller
public class ConnectController {

  @Autowired
  private ConnectService service;

  @RequestMapping(
      value = "/connect")
  public ModelAndView showConnectPage(HttpServletRequest request, HttpServletResponse response) throws Exception {

    ParamContainer params = new ParamContainer();
    String reqId = request.getParameter("reqId");
    if (reqId != null && StringUtils.isNumeric(reqId)) {
      params.addParam("reqId", Long.parseLong(reqId));
    }
    String detailString = this.service.process(request, params);
    ModelAndView mv = new ModelAndView("connect");
    mv.addObject("request", detailString);
    return mv;
  }

}
