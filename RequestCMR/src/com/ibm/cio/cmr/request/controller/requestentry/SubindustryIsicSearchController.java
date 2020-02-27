/**
 * 
 */
package com.ibm.cio.cmr.request.controller.requestentry;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.controller.BaseController;
import com.ibm.cio.cmr.request.model.ParamContainer;
import com.ibm.cio.cmr.request.model.requestentry.SubindustryIsicSearchModel;
import com.ibm.cio.cmr.request.service.requestentry.SubindustryIsicSearchService;

/**
 * @author Jeffrey Zamora
 * 
 */
@Controller
public class SubindustryIsicSearchController extends BaseController {

  @Autowired
  private SubindustryIsicSearchService service;

  @RequestMapping(value = "/subindisic/search", method = { RequestMethod.POST, RequestMethod.GET })
  public ModelMap getSubindIsicSearchResults(HttpServletRequest request, HttpServletResponse response, SubindustryIsicSearchModel model)
      throws CmrException {

    ParamContainer params = new ParamContainer();
    params.addParam("model", model);
    List<SubindustryIsicSearchModel> results = service.process(request, params);

    return wrapAsSearchResult(results);
  }

}
