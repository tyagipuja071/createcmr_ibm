/**
 * 
 */
package com.ibm.cio.cmr.request.automation.util.geo;

import com.ibm.cio.cmr.request.automation.util.AutomationUtil;

import java.util.Arrays;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.automation.AutomationEngineData;
import com.ibm.cio.cmr.request.automation.RequestData;
import com.ibm.cio.cmr.request.automation.out.AutomationResult;
import com.ibm.cio.cmr.request.automation.out.OverrideOutput;
import com.ibm.cio.cmr.request.automation.out.ValidationOutput;
import com.ibm.cio.cmr.request.automation.util.AutomationUtil;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.Data;

public class IndiaUtil extends AutomationUtil {

  public static final String SCENARIO_ESOSW = "ESOSW";
  public static final String SCENARIO_NORMAL = "NRML";
  public static final String SCENARIO_INTERNAL = "INTER";
  public static final String SCENARIO_ACQUISITION = "AQSTN";
  public static final String SCENARIO_DUMMY = "DUMMY";
  public static final String SCENARIO_IGF = "IGF";
  public static final String SCENARIO_PRIVATE_CUSTOMER = "PRIV";
  public static final String SCENARIO_FOREIGN = "CROSS";
  private static final List<String> RELEVANT_ADDRESSES = Arrays.asList(CmrConstants.RDC_SOLD_TO, CmrConstants.RDC_BILL_TO,
      CmrConstants.RDC_INSTALL_AT, CmrConstants.RDC_SECONDARY_SHIPPING);
  private static final Logger LOG = Logger.getLogger(IndiaUtil.class);

  @Override
  public AutomationResult<OverrideOutput> doCountryFieldComputations(EntityManager entityManager, AutomationResult<OverrideOutput> results,
      StringBuilder details, OverrideOutput overrides, RequestData requestData, AutomationEngineData engineData) throws Exception {
    // TODO Auto-generated method stub
    Admin admin = requestData.getAdmin();
    Data data = requestData.getData();
    String scenario = data.getCustSubGrp();
    if (!"C".equals(admin.getReqType())) {
      details.append("Field Computation skipped for Updates.");
      results.setResults("Skipped");
      results.setDetails(details.toString());
      return results;
    }
    details.append("No specific fields to calculate.");
    results.setResults("Skipped.");
    results.setProcessOutput(overrides);
    results.setDetails(details.toString());
    LOG.debug(results.getDetails());
    return results;
  }

  @Override
  public boolean performScenarioValidation(EntityManager entityManager, RequestData requestData, AutomationEngineData engineData,
      AutomationResult<ValidationOutput> result, StringBuilder details, ValidationOutput output) {
    Data data = requestData.getData();
    String scenario = data.getCustSubGrp();
    switch (scenario) {
    case SCENARIO_ACQUISITION:
    case SCENARIO_NORMAL:
    case SCENARIO_ESOSW:
    case SCENARIO_FOREIGN:
      if (data.getApCustClusterId().contains("012D999")) {
        details.append("Cluster cannot be default as 012D999.").append("\n");
        engineData.addRejectionComment("OTH", "Cluster cannot be default as 012D999.", "", "");
        return false;
      }
      break;
    case SCENARIO_INTERNAL:
      for (String addrType : RELEVANT_ADDRESSES) {
        List<Addr> addresses = requestData.getAddresses(addrType);
        for (Addr addr : addresses) {
          String custNmTrimmed = getCustomerFullName(addr);
          if (!(custNmTrimmed.toUpperCase().contains("IBM INDIA") || custNmTrimmed.toUpperCase().contains("INTERNATIONAL BUSINESS MACHINES INDIA"))) {
            details.append("Customer Name in all addresses should contain IBM India for Internal Scenario.").append("\n");
            engineData.addRejectionComment("OTH", "Customer Name in all addresses should contain IBM India for Internal Scenario.", "", "");
            return false;
          }
        }
      }
      break;
    case SCENARIO_PRIVATE_CUSTOMER:
      for (String addrType : RELEVANT_ADDRESSES) {
        List<Addr> addresses = requestData.getAddresses(addrType);
        for (Addr addr : addresses) {
          String custNmTrimmed = getCustomerFullName(addr);
          if (custNmTrimmed.toUpperCase().contains("PRIVATE LIMITED") || custNmTrimmed.toUpperCase().contains("COMPANY")
              || custNmTrimmed.toUpperCase().contains("CORPORATION") || custNmTrimmed.toUpperCase().contains("INCORPORATE")
              || custNmTrimmed.toUpperCase().contains("ORGANIZATION") || custNmTrimmed.toUpperCase().contains("PVT LTD")) {
            details.append("Customer name should not contain 'Private Limited', 'Company', 'Corporation', 'Incorporate', 'Organization', 'Pvt Ltd' .")
                .append("\n");
            engineData.addRejectionComment("OTH",
                "Customer name should not contain 'Private Limited', 'Company', 'Corporation', 'Incorporate', 'Organization', 'Pvt Ltd' .", "", "");
            return false;
          }
        }
      }
      break;
    case SCENARIO_DUMMY:
    case SCENARIO_IGF:
      engineData.addPositiveCheckStatus(AutomationEngineData.SKIP_GBG);
      break;

    }
    return true;
  }

}
