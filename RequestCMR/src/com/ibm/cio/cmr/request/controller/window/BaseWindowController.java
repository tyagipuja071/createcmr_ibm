/**
 * 
 */
package com.ibm.cio.cmr.request.controller.window;

import org.springframework.web.servlet.ModelAndView;

import com.ibm.cio.cmr.request.controller.BaseController;

/**
 * @author Jeffrey Zamora
 * 
 */
public class BaseWindowController extends BaseController {

  protected static final String WINDOW_URL = "/window";

  protected ModelAndView getWindow(ModelAndView mv, String title) {
    mv.addObject("windowtitle", title);
    return mv;
  }
}
