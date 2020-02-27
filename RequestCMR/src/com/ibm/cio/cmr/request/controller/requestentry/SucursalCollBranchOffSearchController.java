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
import com.ibm.cio.cmr.request.model.requestentry.SucursalCollBranchOffModel;
import com.ibm.cio.cmr.request.service.requestentry.SucursalCollBranchOffSearchService;

/**
 * @author Mukesh
 *
 */
@Controller
public class SucursalCollBranchOffSearchController extends BaseController{
  
  @Autowired
  private SucursalCollBranchOffSearchService service;
  
  @RequestMapping(value = "/sucursalcollbranchoff/search", method = { RequestMethod.POST, RequestMethod.GET })
  public ModelMap getCollBranchOfficeSearchResults(HttpServletRequest request, HttpServletResponse response, SucursalCollBranchOffModel model)
      throws CmrException {

    ParamContainer params = new ParamContainer();
    params.addParam("model", model);
    List<SucursalCollBranchOffModel> results = service.process(request, params);

    return wrapAsSearchResult(results);
  }
}
