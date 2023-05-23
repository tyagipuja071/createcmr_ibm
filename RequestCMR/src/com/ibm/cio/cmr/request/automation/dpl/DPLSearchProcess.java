/**
 * 
 */
package com.ibm.cio.cmr.request.automation.dpl;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.util.RequestUtils;
import com.ibm.cio.cmr.request.util.SystemUtil;
import com.ibm.cmr.services.client.CmrServicesFactory;
import com.ibm.cmr.services.client.DPLCheckClient;
import com.ibm.cmr.services.client.dpl.DPLCheckRequest;
import com.ibm.cmr.services.client.dpl.DPLSearchResponse;
import com.ibm.cmr.services.client.dpl.DPLSearchResults;
import com.ibm.cmr.services.client.dpl.KycScreeningResponse;

/**
 * Connects to the DPL Check service via a user's encrypted LTPA token and
 * retrieves the results
 * 
 * @author JeffZAMORA
 * 
 */
public class DPLSearchProcess {

  private static final Logger LOG = Logger.getLogger(DPLSearchProcess.class);

  private DPLSearchResults currentResult;

  /**
   * Connects to the defined DPL Check Web URL and performs a search.
   * 
   * @param companyName
   * @throws Exception
   */
  public void performDplSearch(String companyName, boolean privatePerson) throws Exception {

    DPLCheckClient client = CmrServicesFactory.getInstance().createClient(SystemConfiguration.getValue("BATCH_SERVICES_URL"), DPLCheckClient.class);
    DPLCheckRequest request = new DPLCheckRequest();
    request.setId(new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
    request.setCompanyName(companyName);
    request.setIncludeScreening(true);
    LOG.debug("Performing DPL Search on " + companyName);
    DPLSearchResponse response = null;
    if (SystemUtil.useKYCForDPLChecks()) {
      KycScreeningResponse kycResponse = client.executeAndWrap(DPLCheckClient.KYC_APP_ID, request, KycScreeningResponse.class);
      response = RequestUtils.convertToLegacySearchResults("CreateCMR", kycResponse);
    } else {
      response = client.executeAndWrap(DPLCheckClient.DPL_SEARCH_APP_ID, request, DPLSearchResponse.class);
    }

    if (response == null || !response.isSuccess()) {
      LOG.warn("DPL Search failed with message: " + response.getMsg());
    } else {
      this.currentResult = response.getResults();
    }
  }

  /**
   * Returns the results. This result will be blank unless
   * {@link #performDplCheck(String)} has been done
   * 
   * @return
   */
  public DPLSearchResults getResult() {
    return this.currentResult;
  }

}
