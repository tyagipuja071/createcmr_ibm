/**
 * 
 */
package com.ibm.cio.cmr.request.controller.ws;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.controller.BaseController;
import com.ibm.cio.cmr.request.model.ParamContainer;
import com.ibm.cio.cmr.request.model.requestentry.AddressModel;
import com.ibm.cio.cmr.request.model.ws.TgmeAddrStdModel;
import com.ibm.cio.cmr.request.service.ws.TgmeAddrStdService;

/**
 * @author Jeffrey Zamora
 * 
 */
@Controller
public class TgmeAddrStdController extends BaseController {

  @Autowired
  private TgmeAddrStdService service;

  @RequestMapping("/tgme")
  public ModelMap performAddrStd(HttpServletRequest request, HttpServletResponse response, AddressModel model) {
    ModelMap result = new ModelMap();

    ParamContainer params = new ParamContainer();
    params.addParam("address", model);
    TgmeAddrStdModel tgmeResult = null;
    try {
      tgmeResult = service.process(request, params);
    } catch (CmrException e) {
      tgmeResult = new TgmeAddrStdModel();
      tgmeResult.setStdResultCode("X");
    }

    result.addAttribute("result", tgmeResult);
    return result;
  }
}
