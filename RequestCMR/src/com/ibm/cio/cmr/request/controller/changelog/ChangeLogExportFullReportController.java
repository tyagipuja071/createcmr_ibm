package com.ibm.cio.cmr.request.controller.changelog;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.controller.BaseController;
import com.ibm.cio.cmr.request.controller.system.MetricsController;
import com.ibm.cio.cmr.request.model.ParamContainer;
import com.ibm.cio.cmr.request.service.changelog.ChangeLogExportFullReportLegacyService;
import com.ibm.cio.cmr.request.service.changelog.ChangeLogExportFullReportRDcService;
import com.ibm.cio.cmr.request.service.changelog.ChangeLogExportFullReportService;

@Controller
public class ChangeLogExportFullReportController extends BaseController {

  private static final Logger LOG = Logger.getLogger(MetricsController.class);

  @Autowired
  private ChangeLogExportFullReportService service;

  @Autowired
  private ChangeLogExportFullReportRDcService rdcService;

  @Autowired
  private ChangeLogExportFullReportLegacyService legacyService;

  @RequestMapping(value = "/changelog/exportFullReport", method = { RequestMethod.POST, RequestMethod.GET })
  public void generateRequesterStatistics(HttpServletRequest request, HttpServletResponse response) throws CmrException {

    ParamContainer params = new ParamContainer();
    params.addParam("KATR6", request.getParameter("katr6"));
    params.addParam("ZZKV_CUSNO", request.getParameter("zzkvCusNo"));

    List<Object[]> rdcList = (List<Object[]>) rdcService.process(request, params);

    List<Object[]> legacyList = (List<Object[]>) legacyService.process(request, params);

    try {
      service.exportToExcel(rdcList, legacyList, response);
    } catch (Exception e) {
      LOG.debug("Cannot export Requester statistics", e);
    }
  }

}
