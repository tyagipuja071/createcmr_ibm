package com.ibm.cio.cmr.request.automation.util.geo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.automation.AutomationEngineData;
import com.ibm.cio.cmr.request.automation.RequestData;
import com.ibm.cio.cmr.request.automation.out.AutomationResult;
import com.ibm.cio.cmr.request.automation.out.OverrideOutput;
import com.ibm.cio.cmr.request.automation.out.ValidationOutput;
import com.ibm.cio.cmr.request.automation.util.AutomationUtil;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cmr.services.client.matching.MatchingResponse;
import com.ibm.cmr.services.client.matching.cmr.DuplicateCMRCheckResponse;

public class UKIUtil extends AutomationUtil {

  private static final Logger LOG = Logger.getLogger(UKIUtil.class);
  public static final String SCENARIO_BUSINESS_PARTNER = "BUSPR";
  public static final String SCENARIO_COMMERCIAL = "COMME";
  public static final String SCENARIO_GOVERNMENT = "GOVRN";
  public static final String SCENARIO_DATACENTER = "DC";
  public static final String SCENARIO_IGF = "IGF";
  public static final String SCENARIO_INTERNAL_FSL = "INFSL";
  public static final String SCENARIO_INTERNAL = "INTER";
  public static final String SCENARIO_PRIVATE_PERSON = "PRICU";
  public static final String SCENARIO_THIRD_PARTY = "THDPT";
  public static final String SCENARIO_CROSSBORDER = "CROSS";
  public static final String SCENARIO_CROSS_GOVERNMENT = "XGOVR";
  public static final String SCENARIO_CROSS_IGF = "XIGF";

  @Override
  public boolean performScenarioValidation(EntityManager entityManager, RequestData requestData, AutomationEngineData engineData,
      AutomationResult<ValidationOutput> result, StringBuilder details, ValidationOutput output) {
    Data data = requestData.getData();
    Addr zs01 = requestData.getAddress("ZS01");
    String custNm1 = zs01.getCustNm1();
    String custNm2 = !StringUtils.isBlank(zs01.getCustNm2()) ? " " + zs01.getCustNm2() : "";
    String customerName = custNm1 + custNm2;
    String scenario = data.getCustSubGrp();

    Addr zi01 = requestData.getAddress("ZI01");
    custNm1 = zi01.getCustNm1();
    custNm2 = !StringUtils.isBlank(zi01.getCustNm2()) ? " " + zi01.getCustNm2() : "";
    String customerNameZI01 = custNm1 + custNm2;
    if (StringUtils.isBlank(scenario)) {
      details.append("Scenario not correctly specified on the request");
      engineData.addNegativeCheckStatus("_atNoScenario", "Scenario not correctly specified on the request");
      return true;
    }
    LOG.info("Starting scenario validations for Request ID " + data.getId().getReqId());
    LOG.debug("Scenario to check: " + scenario);
    if (!SCENARIO_THIRD_PARTY.equals(scenario)
        && ((customerNameZI01.toUpperCase().contains("C/O") || customerNameZI01.toUpperCase().contains("CAREOF")
            || customerNameZI01.toUpperCase().contains("CARE OF")) || customerNameZI01.toUpperCase().matches("^VR[0-9]{3}.+$"))) {
      details.append("Third Party Scenario should be selected.").append("\n");
      engineData.addRejectionComment("OTH", "Third Party Scenario should be selected.", "", "");
      return false;
    } else if ((SCENARIO_COMMERCIAL.equals(scenario) || SCENARIO_GOVERNMENT.equals(scenario) || SCENARIO_CROSSBORDER.equals(scenario)
        || SCENARIO_CROSS_GOVERNMENT.equals(scenario)) && !addressEquals(zs01, zi01)) {
      details.append("Billing and Installing addresses are not same. Request will require CMDE review before proceeding.").append("\n");
      engineData.addNegativeCheckStatus("BILL_INSTALL_DIFF", "Billing and Installing addresses are not same.");
    }

    switch (scenario) {
    case SCENARIO_BUSINESS_PARTNER:
      return doBusinessPartnerChecks(engineData, data.getPpsceid(), details);
    case SCENARIO_PRIVATE_PERSON:
      return doPrivatePersonChecks(engineData, data.getCmrIssuingCntry(), zs01.getLandCntry(), customerName, details, false);
    case SCENARIO_INTERNAL:
      if (!customerName.contains("IBM") && !customerNameZI01.contains("IBM")) {
        details.append("Mailing and Billing addresses should have IBM in them.");
        engineData.addRejectionComment("OTH", "Mailing and Billing addresses should have IBM in them.", "", "");
        return false;
      }
      break;
    case SCENARIO_THIRD_PARTY:
      if (customerName.toUpperCase().equals(customerNameZI01.toUpperCase())) {
        details.append("Customer Names on installing and billing address should be different for Third Party Scenario").append("\n");
        engineData.addRejectionComment("OTH", "Customer Names on installing and billing address should be different for Third Party Scenario", "",
            "");
        return false;
      } else if (!(customerNameZI01.toUpperCase().contains("C/O") || customerNameZI01.toUpperCase().contains("CAREOF")
          || customerNameZI01.toUpperCase().contains("CARE OF")) && !customerNameZI01.toUpperCase().matches("^VR[0-9]{3}.+$")) {
        details.append("The request does not meet the criteria for Third Party Scenario.").append("\n");
        engineData.addRejectionComment("OTH", "The request does not meet the criteria for Third Party Scenario.", "", "");
        return false;
      }
      break;
    case SCENARIO_DATACENTER:
      if (!customerNameZI01.toUpperCase().contains("DATACENTER") && !customerNameZI01.toUpperCase().contains("DATA CENTER")) {
        details.append("The request does not meet the criteria for Data Center Scenario.").append("\n");
        engineData.addRejectionComment("OTH", "The request does not meet the criteria for Data Center Scenario.", "", "");
        return false;
      }
      break;
    }
    return true;
  }

  @Override
  protected List<String> getCountryLegalEndings() {
    return Arrays.asList("LLP", "LTD", "Ltd.", "CIC", "CIO", "Cyf", "CCC", "Unltd.", "Ultd.");
  }

  @Override
  public AutomationResult<OverrideOutput> doCountryFieldComputations(EntityManager entityManager, AutomationResult<OverrideOutput> results,
      StringBuilder details, OverrideOutput overrides, RequestData requestData, AutomationEngineData engineData) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void filterDuplicateCMRMatches(EntityManager entityManager, RequestData requestData, AutomationEngineData engineData,
      MatchingResponse<DuplicateCMRCheckResponse> response) {
    String scenario = requestData.getData().getCustSubGrp();
    String[] custClassValuesToCheck = { "43", "45", "46" };
    if (UKIUtil.SCENARIO_BUSINESS_PARTNER.equals(scenario)) {
      List<DuplicateCMRCheckResponse> matches = response.getMatches();
      List<DuplicateCMRCheckResponse> filteredMatches = new ArrayList<DuplicateCMRCheckResponse>();
      for (DuplicateCMRCheckResponse match : matches) {
        if (StringUtils.isNotBlank(match.getCustClass())) {
          String custClass = match.getCustClass();
          if (Arrays.asList(custClassValuesToCheck).contains(custClass)) {
            filteredMatches.add(match);
          }
        }

      }
      // set filtered matches in response
      if (!filteredMatches.isEmpty()) {
        response.setMatches(filteredMatches);
      }
    }

  }

}
