package com.ibm.cio.cmr.request.automation.util.geo;

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

public class BeLuxUtil extends AutomationUtil {

  private static final Logger LOG = Logger.getLogger(BeLuxUtil.class);
  public static final String SCENARIO_LOCAL_COMMERCIAL = "BECOM";
  public static final String SCENARIO_CROSS_COMMERCIAL = "CBCOM";
  public static final String SCENARIO_LOCAL_PUBLIC = "BEPUB";
  public static final String SCENARIO_BP_LOCAL = "BEBUS";
  public static final String SCENARIO_BP_CROSS = "CBBUS";
  public static final String SCENARIO_PRIVATE_CUSTOMER = "BEPRI";
  public static final String SCENARIO_THIRD_PARTY = "BE3PA";
  public static final String SCENARIO_INTERNAL = "BEINT";
  public static final String SCENARIO_INTERNAL_SO = "BEISO";
  // Lux
  public static final String SCENARIO_CROSS_LU = "LUCRO";
  public static final String SCENARIO_LOCAL_PUBLIC_LU = "LUPUB";
  public static final String SCENARIO_PRIVATE_CUSTOMER_LU = "LUPRI";
  public static final String SCENARIO_LOCAL_COMMERCIAL_LU = "LUCOM";
  public static final String SCENARIO_INTERNAL_LU = "LUINT";
  public static final String SCENARIO_INTERNAL_SO_LU = "LUISO";
  public static final String SCENARIO_THIRD_PARTY_LU = "LUBUS";
  public static final String SCENARIO_BP_LOCAL_LU = "LU3PA";

  @Override
  public AutomationResult<OverrideOutput> doCountryFieldComputations(EntityManager entityManager, AutomationResult<OverrideOutput> results,
      StringBuilder details, OverrideOutput overrides, RequestData requestData, AutomationEngineData engineData) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean performScenarioValidation(EntityManager entityManager, RequestData requestData, AutomationEngineData engineData,
      AutomationResult<ValidationOutput> result, StringBuilder details, ValidationOutput output) {

    Data data = requestData.getData();
    String scenario = data.getCustSubGrp();
    Addr zs01 = requestData.getAddress("ZS01");
    String customerName = zs01.getCustNm1();
    Addr zp01 = requestData.getAddress("ZP01");
    String customerNameZP01 = "";
    String landedCountryZP01 = "";
    if (zp01 != null) {
      customerNameZP01 = StringUtils.isBlank(zp01.getCustNm1()) ? "" : zp01.getCustNm1();
      landedCountryZP01 = StringUtils.isBlank(zp01.getLandCntry()) ? "" : zp01.getLandCntry();
    }
    if ("C".equals(requestData.getAdmin().getReqType())) {
      // remove duplicates
      removeDuplicateAddresses(entityManager, requestData, details);
    }

    if ((SCENARIO_BP_LOCAL.equals(scenario) || SCENARIO_BP_CROSS.equals(scenario) || SCENARIO_BP_LOCAL_LU.equals(scenario)) && zp01 != null
        && (!StringUtils.equals(getCleanString(customerName), getCleanString(customerNameZP01))
            || !StringUtils.equals(zs01.getLandCntry(), landedCountryZP01))) {
      details.append("Customer Name and Landed Country on Sold-to and Bill-to address should be same for BP Scenario.").append("\n");
      engineData.addNegativeCheckStatus("SOLDTO_BILLTO_DIFF",
          "Customer Name and Landed Country on Sold-to and Bill-to address should be same for BP Scenario.");
    }

    switch (scenario) {

    case SCENARIO_LOCAL_COMMERCIAL:
    case SCENARIO_CROSS_COMMERCIAL:
    case SCENARIO_LOCAL_COMMERCIAL_LU:
    case SCENARIO_LOCAL_PUBLIC:
    case SCENARIO_LOCAL_PUBLIC_LU:
      if (zp01 != null && (!StringUtils.equals(getCleanString(customerName), getCleanString(customerNameZP01))
          || !StringUtils.equals(zs01.getLandCntry(), landedCountryZP01))) {
        details.append("Customer Name and Landed Country on Sold-to and Bill-to address should be same for Commercial and Public Customer Scenario.")
            .append("\n");
        engineData.addNegativeCheckStatus("SOLDTO_BILLTO_DIFF",
            "Customer Name and Landed Country on Sold-to and Bill-to address should be same for Commercial and Public Customer Scenario.");
      }
      break;

    case SCENARIO_BP_LOCAL:
    case SCENARIO_BP_CROSS:
    case SCENARIO_BP_LOCAL_LU:
      return doBusinessPartnerChecks(engineData, data.getPpsceid(), details);

    case SCENARIO_PRIVATE_CUSTOMER:
    case SCENARIO_PRIVATE_CUSTOMER_LU:
      return doPrivatePersonChecks(engineData, data.getCmrIssuingCntry(), zs01.getLandCntry(), customerName, details, false, requestData);

    case SCENARIO_THIRD_PARTY:
    case SCENARIO_THIRD_PARTY_LU:
      details.append("Processor Review will be required for Third Party Scenario/Data Center.\n");
      engineData.addNegativeCheckStatus("Scenario_Validation", "3rd Party/Data Center request will require CMDE review before proceeding.\n");
      break;
    }

    return true;
  }
}
