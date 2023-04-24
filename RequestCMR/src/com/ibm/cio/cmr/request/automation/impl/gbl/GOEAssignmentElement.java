/**
 * 
 */
package com.ibm.cio.cmr.request.automation.impl.gbl;

import javax.persistence.EntityManager;

import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.automation.AutomationElementRegistry;
import com.ibm.cio.cmr.request.automation.AutomationEngineData;
import com.ibm.cio.cmr.request.automation.RequestData;
import com.ibm.cio.cmr.request.automation.impl.OverridingElement;
import com.ibm.cio.cmr.request.automation.out.AutomationResult;
import com.ibm.cio.cmr.request.automation.out.OverrideOutput;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cmr.services.client.CmrServicesFactory;
import com.ibm.cmr.services.client.GOEClient;
import com.ibm.cmr.services.client.goe.GOERequest;
import com.ibm.cmr.services.client.goe.GOEResponse;
import com.ibm.cmr.services.client.matching.dnb.DnBMatchingResponse;

/**
 * @author 136786PH1
 *
 */
public class GOEAssignmentElement extends OverridingElement {

  private static final Logger LOG = Logger.getLogger(GOEAssignmentElement.class);

  public GOEAssignmentElement(String requestTypes, String actionOnError, boolean overrideData, boolean stopOnError) {
    super(requestTypes, actionOnError, overrideData, stopOnError);
  }

  @Override
  public AutomationResult<OverrideOutput> executeElement(EntityManager entityManager, RequestData requestData, AutomationEngineData engineData)
      throws Exception {

    long reqId = requestData.getAdmin().getId().getReqId();
    Data data = requestData.getData();
    DnBMatchingResponse dnbMatching = (DnBMatchingResponse) engineData.get(AutomationEngineData.DNB_MATCH);
    AutomationResult<OverrideOutput> result = buildResult(reqId);
    OverrideOutput output = new OverrideOutput(false);
    result.setProcessOutput(output);
    StringBuilder details = new StringBuilder();
    boolean computed = false;
    if (dnbMatching == null) {
      details.append("No D&B matches found.\nGOE Indicator will be set to U - Unknown");
      output.addOverride(AutomationElementRegistry.GBL_GOE, null, "GOE_IND", data.getGoeInd(), "U");
      output.addOverride(AutomationElementRegistry.GBL_GOE, null, "GOE_SRC_CD", data.getGoeSrcCd(), "NA");
      output.addOverride(AutomationElementRegistry.GBL_GOE, null, "GOE_RSN_CD", data.getGoeRsnCd(), "C");
    } else {
      try {
        String duns = dnbMatching.getDunsNo();
        String country = dnbMatching.getDnbCountry();
        GOEResponse goeResponse = findGOE(duns, country);
        if (!goeResponse.isSuccess()) {
          details.append("Error from FindGOE service. : " + goeResponse.getMessage() + "\nGOE Indicator will be set to U - Unknown");
          output.addOverride(AutomationElementRegistry.GBL_GOE, null, "GOE_IND", data.getGoeInd(), "U");
          output.addOverride(AutomationElementRegistry.GBL_GOE, null, "GOE_SRC_CD", data.getGoeSrcCd(), "NA");
          output.addOverride(AutomationElementRegistry.GBL_GOE, null, "GOE_RSN_CD", data.getGoeRsnCd(), "C");
        } else {
          LOG.debug("GOE Response: " + goeResponse.getGoeStatusCd() + " / " + goeResponse.getGoeSrcCd() + " / " + goeResponse.getGoeReasonCd());
          details.append("FindGOE Results:\n");
          details.append("Overall Status: " + goeResponse.getGoeStatusCd() + " - " + goeResponse.getGoeStatusDesc() + "\n");
          details.append("Source: " + goeResponse.getGoeSrcCd() + " - " + goeResponse.getGoeSrcDesc() + "\n");
          details.append("Reason: " + goeResponse.getGoeReasonCd() + " - " + goeResponse.getGoeReasonDesc());
          output.addOverride(AutomationElementRegistry.GBL_GOE, null, "GOE_IND", data.getGoeInd(), goeResponse.getGoeStatusCd());
          if ("U".equals(goeResponse.getGoeStatusCd())) {
            output.addOverride(AutomationElementRegistry.GBL_GOE, null, "GOE_SRC_CD", data.getGoeSrcCd(), "NA");
            output.addOverride(AutomationElementRegistry.GBL_GOE, null, "GOE_RSN_CD", data.getGoeRsnCd(), "C");
          } else {
            output.addOverride(AutomationElementRegistry.GBL_GOE, null, "GOE_SRC_CD", data.getGoeSrcCd(), goeResponse.getGoeSrcCd());
            output.addOverride(AutomationElementRegistry.GBL_GOE, null, "GOE_RSN_CD", data.getGoeRsnCd(), goeResponse.getGoeReasonCd());
          }
          computed = true;
        }
      } catch (Exception e) {
        LOG.error("Error in connecting to FindGOE service", e);
        details.append("Cannot connect to FindGOE service.\nGOE Indicator will be set to U - Unknown");
        output.addOverride(AutomationElementRegistry.GBL_GOE, null, "GOE_IND", data.getGoeInd(), "U");
        output.addOverride(AutomationElementRegistry.GBL_GOE, null, "GOE_SRC_CD", data.getGoeSrcCd(), "NA");
        output.addOverride(AutomationElementRegistry.GBL_GOE, null, "GOE_RSN_CD", data.getGoeRsnCd(), "C");
      }
    }

    result.setProcessOutput(output);
    result.setDetails(details.toString());
    result.setResults(computed ? "Computed" : "Defaulted");
    result.setOnError(false);
    return result;
  }

  /**
   * Calls GOE service on CMR Services passing the DUNS and Country
   * 
   * @param duns
   * @param country
   * @return
   * @throws Exception
   */
  private GOEResponse findGOE(String duns, String country) throws Exception {
    LOG.debug("Connecting to FindGOE service using DUNS " + duns + " Country: " + country);
    String url = SystemConfiguration.getValue("CMR_SERVICES_URL");
    GOEClient goe = CmrServicesFactory.getInstance().createClient(url, GOEClient.class);
    GOERequest request = new GOERequest();
    request.setRecId(duns);
    request.setRecType(GOERequest.TYPE_DUNS);
    request.setLandCountry(country);

    GOEResponse response = goe.execute(null, request, GOEResponse.class);
    LOG.debug("Response received. Status: " + response.getGoeStatusCd());
    return response;

  }

  @Override
  public String getProcessCode() {
    return AutomationElementRegistry.GBL_GOE;
  }

  @Override
  public String getProcessDesc() {
    return "GOE Determination";
  }

}
