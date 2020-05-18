/**
 * 
 */
package com.ibm.cio.cmr.request.automation.util.geo;

import java.util.Arrays;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.automation.AutomationEngineData;
import com.ibm.cio.cmr.request.automation.RequestData;
import com.ibm.cio.cmr.request.automation.out.AutomationResult;
import com.ibm.cio.cmr.request.automation.out.OverrideOutput;
import com.ibm.cio.cmr.request.automation.out.ValidationOutput;
import com.ibm.cio.cmr.request.automation.util.AutomationUtil;
import com.ibm.cio.cmr.request.automation.util.RequestChangeContainer;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.model.window.UpdatedDataModel;
import com.ibm.cio.cmr.request.util.SystemLocation;

import edu.emory.mathcs.backport.java.util.Collections;

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

  private static final List<String> RELEVANT_ADDRESSES = Arrays.asList(CmrConstants.RDC_SOLD_TO, CmrConstants.RDC_BILL_TO,
      CmrConstants.RDC_INSTALL_AT);

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
      // noop
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

  @SuppressWarnings("unchecked")
  @Override
  public boolean runUpdateChecksForAddress(EntityManager entityManager, AutomationEngineData engineData, RequestData requestData,
      RequestChangeContainer changes, AutomationResult<ValidationOutput> output, ValidationOutput validation) throws Exception {

    Admin admin = requestData.getAdmin();

    if (handlePrivatePersonRecord(entityManager, admin, output, validation, engineData)) {
      return true;
    }

    List<Addr> addresses = null;
    for (String addrType : RELEVANT_ADDRESSES) {
      if (changes.isAddressChanged(addrType)) {
        if (CmrConstants.RDC_SOLD_TO.equals(addrType)) {
          addresses = Collections.singletonList(requestData.getAddress(CmrConstants.RDC_SOLD_TO));
        } else {
          addresses = requestData.getAddresses(addrType);
        }
        boolean hasAddition = false;
        boolean hasUpdate = false;
        for (Addr addr : addresses) {
          // new address
          if ("N".equals(addr.getImportInd())) {
            boolean duplicate = addressExists(entityManager, addr);
          } else {
            engineData.addNegativeCheckStatus("_chAddrUpdated", "Updates to ");
          }
        }
      }
    }

    return true;
  }

  @Override
  public boolean runUpdateChecksForData(EntityManager entityManager, AutomationEngineData engineData, RequestData requestData,
      RequestChangeContainer changes, AutomationResult<ValidationOutput> output, ValidationOutput validation) throws Exception {
    Admin admin = requestData.getAdmin();
    Data data = requestData.getData();

    if (handlePrivatePersonRecord(entityManager, admin, output, validation, engineData)) {
      return true;
    }

    for (UpdatedDataModel change : changes.getDataUpdates()) {

    }

    return true;
  }

  @Override
  public AutomationResult<OverrideOutput> doCountryFieldComputations(EntityManager entityManager, AutomationResult<OverrideOutput> results,
      StringBuilder details, OverrideOutput overrides, RequestData requestData, AutomationEngineData engineData) throws Exception {
    return null;
  }

}
