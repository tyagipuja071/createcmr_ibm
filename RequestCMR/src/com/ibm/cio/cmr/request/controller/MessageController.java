/**
 * 
 */
package com.ibm.cio.cmr.request.controller;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.ibm.cio.cmr.request.Message;
import com.ibm.cio.cmr.request.util.MessageUtil;

/**
 * Controller for handling messages.
 * 
 * @author Jeffrey Zamora
 * 
 */
@Controller
public class MessageController extends BaseController {

  /**
   * Gets the specific message mapped to the msgCode found in the URL
   * 
   * @param msgCode
   * @param request
   * @param response
   * @throws IOException
   */
  @RequestMapping(value = "/messages/{msgCode}")
  public void getMessage(@PathVariable int msgCode, HttpServletRequest request, HttpServletResponse response) throws IOException {
    String params = request.getParameter("params");
    String msg = MessageUtil.getMessage(msgCode);
    if (params != null) {
      msg = MessageFormat.format(msg, (Object[]) params.split(","));
    }
    response.setHeader("Content-Type", "text/plain");
    response.getOutputStream().write(msg.getBytes());
  }

  /**
   * Gets the client messages mapped to the configuration
   * 
   * @param request
   * @param response
   * @return
   * @throws IOException
   */
  @RequestMapping(value = "/messages/client")
  public ModelMap getClientMessages(HttpServletRequest request, HttpServletResponse response) throws IOException {
    ModelMap model = new ModelMap();
    List<Message> msgs = MessageUtil.getClientMessages();
    for (Message msg : msgs) {
      model.addAttribute("m" + msg.getCode(), msg.getMessage());
    }
    return model;
  }

}
