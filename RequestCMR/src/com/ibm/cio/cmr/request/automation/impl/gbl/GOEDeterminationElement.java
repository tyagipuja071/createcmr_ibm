/**
 * 
 */
package com.ibm.cio.cmr.request.automation.impl.gbl;

import java.io.IOException;

import javax.persistence.EntityManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.wink.client.ClientConfig;
import org.apache.wink.client.ClientResponse;
import org.apache.wink.client.Resource;
import org.apache.wink.client.RestClient;

import com.ibm.cio.cmr.request.automation.AutomationElement;
import com.ibm.cio.cmr.request.automation.AutomationElementRegistry;
import com.ibm.cio.cmr.request.automation.AutomationEngineData;
import com.ibm.cio.cmr.request.automation.RequestData;
import com.ibm.cio.cmr.request.automation.impl.MatchingElement;
import com.ibm.cio.cmr.request.automation.out.AutomationResult;
import com.ibm.cio.cmr.request.automation.out.MatchingOutput;
import com.ibm.cio.cmr.request.automation.util.CommonWordsUtil;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.AutomationMatching;
import com.ibm.cmr.services.client.matching.dnb.DnBMatchingResponse;
import com.ibm.json.java.JSONArray;
import com.ibm.json.java.JSONObject;

/**
 * {@link AutomationElement} that connects to FindGOE and determines whether the
 * customer is a government owned entity or not
 * 
 * @author JeffZAMORA
 * @deprecated -Use {@link GOEAssignmentElement}
 */
@Deprecated
public class GOEDeterminationElement extends MatchingElement {

  private static final Logger LOG = Logger.getLogger(GOEDeterminationElement.class);

  /**
   * @param requestTypes
   * @param actionOnError
   * @param overrideData
   * @param stopOnError
   */
  public GOEDeterminationElement(String requestTypes, String actionOnError, boolean overrideData, boolean stopOnError) {
    super(requestTypes, actionOnError, overrideData, stopOnError);

  }

  @Override
  public boolean importMatch(EntityManager entityManager, RequestData requestData, AutomationMatching match) {
    return false;
  }

  @Override
  public AutomationResult<MatchingOutput> executeElement(EntityManager entityManager, RequestData requestData, AutomationEngineData engineData)
      throws Exception {
    long reqId = requestData.getAdmin().getId().getReqId();
    DnBMatchingResponse dnbMatching = (DnBMatchingResponse) engineData.get(AutomationEngineData.DNB_MATCH);
    AutomationResult<MatchingOutput> result = buildResult(reqId);
    MatchingOutput output = new MatchingOutput();
    result.setProcessOutput(output);
    StringBuilder details = new StringBuilder();

    LOG.debug("Connecting to FindGOE to determine GOE values for Request " + reqId);

    String dunsNo = requestData.getData().getDunsNo();
    if (StringUtils.isBlank(dunsNo) && dnbMatching != null) {
      dunsNo = dnbMatching.getDunsNo();
    }

    // duns matching first, then name and country
    JSONArray matches = new JSONArray();
    if (!StringUtils.isBlank(dunsNo)) {
      matches = findGOE(dunsNo, null, null, null);
    }
    if (matches.isEmpty()) {
      Addr soldTo = requestData.getAddress("ZS01");
      String name = requestData.getAdmin().getMainCustNm1();
      if (!StringUtils.isBlank(requestData.getAdmin().getMainCustNm2())) {
        name += " " + requestData.getAdmin().getMainCustNm2();
      }
      matches = findGOE(null, null, name, soldTo.getLandCntry());
    }
    if (!matches.isEmpty()) {
      boolean singleMatch = matches.size() == 1;
      if (!singleMatch) {
        result.setResults("Multiple Matches");
        details.append(matches.size() + " matches found.\n");
      }
      for (Object dunsMatch : matches) {
        JSONObject match = (JSONObject) dunsMatch;
        String overallIndicator = (String) match.get("OVERALL_GOE_CD");
        String drivingFactor = (String) match.get("GOE_DRIVING_FACTOR");
        String busName = (String) match.get("BUSINESS_NAME");
        details.append("\nDUNS No. = " + dunsNo + "\n");
        details.append("Business Name = " + busName + "\n");
        switch (overallIndicator) {
        case "Y":
          if (singleMatch) {
            result.setResults("Government Owned");
          }
          details.append("GOE Determination = Government Owned Entity\n");
          break;
        case "S":
          if (singleMatch) {
            result.setResults("State Owned");
          }
          details.append("GOE Determination = State Owned Entity\n");
          break;
        default:
          if (singleMatch) {
            result.setResults("NOT a GOE");
          }
          details.append("GOE Determination = NOT a GOE\n");
        }
        details.append("Driving Factor = " + drivingFactor + "\n");
      }
    } else {
      result.setResults("Cannot determine");
      details.append("GOE status cannot be determined via DUNS No., KUNNR, or Name and Address matching");
    }

    result.setDetails(details.toString());

    return result;
  }

  /**
   * Connects to the FindGOE service and matches the KUNNR, DUNS No. or Name +
   * Address
   */
  public JSONArray findGOE(String dunsNo, String kunnr, String name, String landedCountry) throws IOException {
    ClientConfig config = new ClientConfig();
    config.readTimeout(1000 * 60 * 2);
    String ciServicesUrl = SystemConfiguration.getValue("BATCH_CI_SERVICES_URL");

    RestClient client = new RestClient(config);
    Resource resource = client.resource(ciServicesUrl + "/service/search/cgoe");

    if (!StringUtils.isBlank(kunnr)) {
      LOG.debug("Querying FindGOE using KUNNR: " + kunnr);
      resource.queryParam("sapNumber", kunnr);
      resource.queryParam("cgoeResultRows", 1);
    } else if (!StringUtils.isBlank(dunsNo)) {
      LOG.debug("Querying FindGOE using DUNS No.: " + dunsNo);
      resource.queryParam("dunsNumber", dunsNo);
      resource.queryParam("cgoeResultRows", 1);
    } else if (!StringUtils.isBlank(name) && !StringUtils.isBlank(landedCountry)) {
      LOG.debug("Querying FindGOE using Name: " + CommonWordsUtil.minimize(name) + " Country: " + landedCountry);
      resource.queryParam("cmrLandedCountry", landedCountry);
      resource.queryParam("customerLegalName", CommonWordsUtil.minimize(name));
    } else {
      return new JSONArray();
    }

    ClientResponse response = resource.contentType("application/json").accept("*/*").get();

    String jsonArr = response.getEntity(String.class);
    JSONArray array = JSONArray.parse(jsonArr);
    return array;

  }

  @Override
  public String getProcessCode() {
    return AutomationElementRegistry.GBL_GOE;
  }

  @Override
  public String getProcessDesc() {
    return "GOE Determination";
  }

  @Override
  public boolean isNonImportable() {
    return true;
  }
}
