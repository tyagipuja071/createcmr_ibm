/**
 * 
 */
package com.ibm.cio.cmr.request.automation.dpl;

import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cmr.services.client.CmrServicesFactory;
import com.ibm.cmr.services.client.DPLCheckClient;
import com.ibm.cmr.services.client.dpl.DPLSearchRequest;
import com.ibm.cmr.services.client.dpl.DPLSearchResponse;
import com.ibm.cmr.services.client.dpl.DPLSearchResults;

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
  public void performDplSearch(String companyName) throws Exception {

    DPLCheckClient client = CmrServicesFactory.getInstance().createClient(SystemConfiguration.getValue("BATCH_SERVICES_URL"), DPLCheckClient.class);
    DPLSearchRequest request = new DPLSearchRequest();
    request.setCompanyName(companyName);

    DPLSearchResponse response = client.executeAndWrap(DPLCheckClient.DPL_SEARCH_APP_ID, request, DPLSearchResponse.class);
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
