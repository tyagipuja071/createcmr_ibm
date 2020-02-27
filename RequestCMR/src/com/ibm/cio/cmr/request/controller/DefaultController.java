/**
 * 
 */
package com.ibm.cio.cmr.request.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;

/**
 * Default controller for all unhandled pages, redirects to error controller
 * 
 * @author Jeffrey Zamora
 * 
 */
@Controller
public class DefaultController {

  // @RequestMapping
  public String forwardRequest(final HttpServletRequest request) {
    String uri = request.getRequestURI();
    if (uri.toLowerCase().endsWith("css") || uri.toLowerCase().endsWith("js")) {
      return "redirect:/" + uri;
    }
    return "redirect:/error";
  }
}
