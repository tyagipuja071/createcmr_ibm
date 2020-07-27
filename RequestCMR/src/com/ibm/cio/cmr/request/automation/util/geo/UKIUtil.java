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
import com.ibm.cio.cmr.request.entity.Data;

public class UKIUtil extends AutomationUtil {

  private static final Logger LOG = Logger.getLogger(UKIUtil.class);
  public static final String SCENARIO_BUSINESS_PARTNER = "BUSPR";

  @Override
  public boolean performScenarioValidation(EntityManager entityManager, RequestData requestData, AutomationEngineData engineData,
      AutomationResult<ValidationOutput> result, StringBuilder details, ValidationOutput output) {
    Data data = requestData.getData();
    String scenario = data.getCustSubGrp();
    if (StringUtils.isBlank(scenario)) {
      details.append("Scenario not correctly specified on the request");
      engineData.addNegativeCheckStatus("_atNoScenario", "Scenario not correctly specified on the request");
      return true;
    }
    LOG.info("Starting scenario validations for Request ID " + data.getId().getReqId());
    LOG.debug("Scenario to check: " + scenario);

    switch (scenario) {
    case SCENARIO_BUSINESS_PARTNER:
      return doBusinessPartnerChecks(engineData, data.getPpsceid(), details);

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

}
