package com.ibm.cio.cmr.request.automation.util.geo;

import java.util.Arrays;

import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.automation.AutomationEngineData;
import com.ibm.cio.cmr.request.automation.RequestData;
import com.ibm.cio.cmr.request.automation.out.AutomationResult;
import com.ibm.cio.cmr.request.automation.out.OverrideOutput;
import com.ibm.cio.cmr.request.automation.out.ValidationOutput;
import com.ibm.cio.cmr.request.automation.util.AutomationUtil;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.Data;

public class USUtil extends AutomationUtil {

  private static final Logger LOG = Logger.getLogger(USUtil.class);

  @Override
  public AutomationResult<OverrideOutput> doCountryFieldComputations(EntityManager entityManager, AutomationResult<OverrideOutput> results,
      StringBuilder details, OverrideOutput overrides, RequestData requestData, AutomationEngineData engineData) throws Exception {
    return null;
  }

  @Override
  public boolean performScenarioValidation(EntityManager entityManager, RequestData requestData, AutomationEngineData engineData,
      AutomationResult<ValidationOutput> result, StringBuilder details, ValidationOutput output) {
    // get request admin and data
    Admin admin = requestData.getAdmin();
    Data data = requestData.getData();
    String[] scnarioList_schools = { "REGULAR", "SCHOOL PUBLIC", "SCHOOL CHARTER", "SCHOOL PRIV", "SCHOOL PAROCHL", "SCHOOL COLLEGE" };
    String[] scnarioList_state = { "REGULAR", "STATE", "SPEC DIST", "COUNTY", "CITY" };
    String customerName = admin.getMainCustNm1() + (StringUtils.isBlank(admin.getMainCustNm2()) ? "" : " " + admin.getMainCustNm2());
    String scenarioSubType = "";
    if ("C".equals(admin.getReqType()) && data != null) {
      scenarioSubType = StringUtils.isBlank(data.getCustSubGrp()) ? "" : data.getCustSubGrp();
    }
    if ((StringUtils.containsIgnoreCase(customerName, "SCHOOL") || StringUtils.containsIgnoreCase(customerName, "COLLEGE")
        || StringUtils.containsIgnoreCase(customerName, "UNIVERSITY") || StringUtils.containsIgnoreCase(customerName, "CHARTER"))) {
      if (Arrays.asList(scnarioList_schools).contains(scenarioSubType)) {
        engineData.addNegativeCheckStatus("US_SCHOOL", "Verification for Public/Private needs to be performed");
      } else {
        details.append("The chosen scenario is incorrect, should be Commercial or State & Local");
        return false;
      }
    }

    if ((StringUtils.containsIgnoreCase(customerName, "STATE") || StringUtils.containsIgnoreCase(customerName, "COUNTY")
        || StringUtils.containsIgnoreCase(customerName, "CITY") || StringUtils.containsIgnoreCase(customerName, "COMMONWEALTH OF")
        || StringUtils.containsIgnoreCase(customerName, "DISTRICT"))) {
      if (Arrays.asList(scnarioList_state).contains(scenarioSubType)) {
        engineData.addNegativeCheckStatus("US_STATE", "Verification for Public/Private needs to be performed");
      } else {
        details.append("The chosen scenario is incorrect, should be Commercial or State & Local");
        return false;
      }
    }
    return true;
  }
}
