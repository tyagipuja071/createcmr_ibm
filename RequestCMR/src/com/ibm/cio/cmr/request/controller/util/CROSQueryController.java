/**
 * 
 */
package com.ibm.cio.cmr.request.controller.util;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cmr.services.client.CROSServiceClient;
import com.ibm.cmr.services.client.CmrServicesFactory;
import com.ibm.cmr.services.client.cros.CROSQueryRequest;
import com.ibm.cmr.services.client.cros.CROSQueryResponse;

/**
 * @author RoopakChugh
 * 
 */
@Controller
public class CROSQueryController {

  private static final Logger LOG = Logger.getLogger(CROSQueryController.class);

  @RequestMapping(
      value = "/cros")
  public ModelMap queryCROS(HttpServletRequest request, HttpServletResponse response) throws Exception {
    ModelMap map = new ModelMap();
    CROSQueryResponse crosResponse = null;
    String issuingCntry = request.getParameter("issuingCntry");
    String cmrNo = request.getParameter("cmrNo");
    if (StringUtils.isEmpty(issuingCntry)) {
      crosResponse = new CROSQueryResponse();
      crosResponse.setSuccess(false);
      crosResponse.setMsg("Param 'issuingCntry' is required.");
    }
    if (StringUtils.isEmpty(cmrNo)) {
      crosResponse = new CROSQueryResponse();
      crosResponse.setSuccess(false);
      crosResponse.setMsg("Param 'cmrNo' is required.");
    }
    if (crosResponse == null || crosResponse.isSuccess()) {

      LOG.debug("Querying CROS for CMR No. " + cmrNo + " for country " + issuingCntry);

      String baseUrl = SystemConfiguration.getValue("CMR_SERVICES_URL");
      // String baseUrl = "https://localhost:9443/CMRServicesV2";
      // String baseUrl =
      // "https://ibmcmrr0.portsmouth.uk.ibm.com/CMRServicesV2";
      CROSServiceClient client = CmrServicesFactory.getInstance().createClient(baseUrl, CROSServiceClient.class);
      CROSQueryRequest crosRequest = new CROSQueryRequest();
      crosRequest.setCmrNo(cmrNo);
      crosRequest.setIssuingCntry(issuingCntry);
      crosResponse = client.executeAndWrap(CROSServiceClient.QUERY_APP_ID, crosRequest, CROSQueryResponse.class);
    }
    map.addAttribute("result", crosResponse);
    return map;
  }
}
