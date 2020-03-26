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
    String[] scnarioList = { "HOSPITALS", "SCHOOL PUBLIC", "SCHOOL CHARTER", "SCHOOL PRIV", "SCHOOL PAROCHL", "SCHOOL COLLEGE", "STATE", "SPEC DIST",
        "COUNTY", "CITY", "32C", "TPPS", "3CC", "SVR CONT" };
    String scenarioSubType = "";
    if ("C".equals(admin.getReqType()) && data != null) {
      scenarioSubType = StringUtils.isBlank(data.getCustSubGrp()) ? "" : data.getCustSubGrp();
    }

    if (Arrays.asList(scnarioList).contains(scenarioSubType)) {
      engineData.addNegativeCheckStatus("US_SCENARIO_CHK", "Automated checks cannot be performed for this scenario.");
    }
    return true;
  }
}
