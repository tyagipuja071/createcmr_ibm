/**
 * 
 */
package com.ibm.cio.cmr.request.automation.util.geo;

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
import com.ibm.cio.cmr.request.automation.util.RequestChangeContainer;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.util.SystemLocation;

/**
 * {@link AutomationUtil} for Switzwerland/LI country specific validations
 * 
 * @author JeffZAMORA
 *
 */
public class SwitzerlandUtil extends AutomationUtil {

  private static final Logger LOG = Logger.getLogger(SwitzerlandUtil.class);
  private static final String SCENARIO_COMMERCIAL = "COM";
  private static final String SCENARIO_GOVERNMENT = "GOV";
  private static final String SCENARIO_INTERNAL = "INT";
  private static final String SCENARIO_THIRD_PARTY = "3PA";
  private static final String SCENARIO_PRIVATE_CUSTOMER = "PRIV";
  private static final String SCENARIO_IBM_EMPLOYEE = "IBM";
  private static final String SCENARIO_BUSINESS_PARTNER = "BUS";
  private static final String SCENARIO_CROSS_BORDER = "XCHCM";

  @Override
  public boolean performScenarioValidation(EntityManager entityManager, RequestData requestData, AutomationEngineData engineData,
      AutomationResult<ValidationOutput> result, StringBuilder details, ValidationOutput output) {
    Data data = requestData.getData();
    Addr soldTo = requestData.getAddress("ZS01");
    String scenario = data.getCustSubGrp();
    LOG.info("Starting scenario validations for Request ID " + data.getId().getReqId());

    if (StringUtils.isBlank(scenario) || scenario.length() != 5) {
      details.append("Scenario not correctly specified on the request");
      engineData.addNegativeCheckStatus("_chNoScenario", "Scenario not correctly specified on the request");
      return true;
    }

    String actualScenario = scenario.substring(2);
    String customerName = soldTo.getCustNm1() + (!StringUtils.isBlank(soldTo.getCustNm2()) ? " " + soldTo.getCustNm2() : "");

    if (!SCENARIO_THIRD_PARTY.equals(actualScenario) && (customerName.toUpperCase().contains("C/O") || customerName.toUpperCase().contains("CAREOF")
        || customerName.toUpperCase().contains("CARE OF"))) {
      details.append("Scenario should be Third Party/Data Center based on custmer name.").append("\n");
      engineData.addNegativeCheckStatus("PPSCEID", "Scenario should be Third Party/Data Center based on custmer name.");
      return true;
    }

    if (SCENARIO_CROSS_BORDER.equals(scenario)) {

    } else {
      switch (actualScenario) {
      case SCENARIO_COMMERCIAL:
        break;
      case SCENARIO_PRIVATE_CUSTOMER:
      case SCENARIO_IBM_EMPLOYEE:
        return doPrivatePersonChecks(engineData, SystemLocation.SWITZERLAND, soldTo.getLandCntry(), customerName, details,
            SCENARIO_IBM_EMPLOYEE.equals(actualScenario));
      case SCENARIO_INTERNAL:
        break;
      case SCENARIO_GOVERNMENT:
        break;
      case SCENARIO_THIRD_PARTY:
        engineData.addNegativeCheckStatus("_chThirdParty", "Third Party/Data Center request needs further validation.");
        break;
      case SCENARIO_BUSINESS_PARTNER:
        return doBusinessPartnerChecks(engineData, data.getPpsceid(), details);
      }
    }

    return true;
  }

  @Override
  protected List<String> getCountryLegalEndings() {
    return Arrays.asList("GMBH", "KLG", " AG", "Sàrl", "SARL", " SA", "S.A.", "SAGL");
  }

  @Override
  public AutomationResult<OverrideOutput> doCountryFieldComputations(EntityManager entityManager, AutomationResult<OverrideOutput> results,
      StringBuilder details, OverrideOutput overrides, RequestData requestData, AutomationEngineData engineData) throws Exception {
    return null;
  }

  @Override
  public boolean runUpdateChecksForAddress(EntityManager entityManager, AutomationEngineData engineData, RequestData requestData,
      RequestChangeContainer changes, AutomationResult<ValidationOutput> output, ValidationOutput validation) throws Exception {
    return super.runUpdateChecksForAddress(entityManager, engineData, requestData, changes, output, validation);
  }

  @Override
  public boolean runUpdateChecksForData(EntityManager entityManager, AutomationEngineData engineData, RequestData requestData,
      RequestChangeContainer changes, AutomationResult<ValidationOutput> output, ValidationOutput validation) throws Exception {
    return super.runUpdateChecksForData(entityManager, engineData, requestData, changes, output, validation);
  }

}
