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
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.model.window.UpdatedNameAddrModel;
import com.ibm.cio.cmr.request.util.SystemLocation;

/**
 * {@link AutomationUtil} for Austria specific validations
 * 
 *
 */
public class AustriaUtil extends AutomationUtil {

  private static final Logger LOG = Logger.getLogger(AustriaUtil.class);
  private static final String SCENARIO_COMMERCIAL = "COMME";
  private static final String SCENARIO_GOVERNMENT = "GOVRN";
  private static final String SCENARIO_INTERNAL = "INTER";
  private static final String SCENARIO_INTERNAL_SO = "INTSO";
  private static final String SCENARIO_PRIVATE_CUSTOMER = "PRICU";
  private static final String SCENARIO_IBM_EMPLOYEE = "IBMEM";
  private static final String SCENARIO_BUSINESS_PARTNER = "BUSPR";
  private static final String SCENARIO_CROSS_COMMERICAL = "XCOM";
  private static final String SCENARIO_CROSS_GOVERNMENT = "XGOV";
  private static final String SCENARIO_CROSS_BUSINESS_PARTNER = "XBUS";
  private static final String SCENARIO_CROSS_INTERNAL = "XINT";
  private static final String SCENARIO_CROSS_INTERNAL_SO = "XISO";

  private static final List<String> RELEVANT_ADDRESSES = Arrays.asList(CmrConstants.RDC_SOLD_TO, CmrConstants.RDC_BILL_TO,
      CmrConstants.RDC_INSTALL_AT, CmrConstants.RDC_SHIP_TO);

  private static final List<String> NON_RELEVANT_ADDRESS_FIELDS = Arrays.asList("Att. Person", "Phone #", "FAX", "Customer Name 4");

  @SuppressWarnings("unchecked")
  public AustriaUtil() {
  }

  @Override
  public boolean performScenarioValidation(EntityManager entityManager, RequestData requestData, AutomationEngineData engineData,
      AutomationResult<ValidationOutput> result, StringBuilder details, ValidationOutput output) {
    Data data = requestData.getData();
    Addr soldTo = requestData.getAddress("ZS01");
    String scenario = data.getCustSubGrp();
    LOG.info("Starting scenario validations for Request ID " + data.getId().getReqId());
    String customerName = soldTo.getCustNm1() + (!StringUtils.isBlank(soldTo.getCustNm2()) ? " " + soldTo.getCustNm2() : "");

    if (StringUtils.isBlank(scenario) || scenario.length() != 5) {
      details.append("Scenario not correctly specified on the request");
      engineData.addNegativeCheckStatus("_atNoScenario", "Scenario not correctly specified on the request");
      return true;
    }

    LOG.debug("Scenario to check: " + scenario);

    switch (scenario) {
    case SCENARIO_PRIVATE_CUSTOMER:
    case SCENARIO_IBM_EMPLOYEE:
      return doPrivatePersonChecks(engineData, SystemLocation.AUSTRIA, soldTo.getLandCntry(), customerName, details,
          SCENARIO_IBM_EMPLOYEE.equals(scenario));
    case SCENARIO_BUSINESS_PARTNER:
      return doBusinessPartnerChecks(engineData, data.getPpsceid(), details);
    }

    return true;
  }

  @Override
  protected List<String> getCountryLegalEndings() {
    return Arrays.asList("AG", "GmbH", "e.U.", "GesmbH & Co KG", "GmbH & Co KG");
  }

  /**
   * Checks if relevant fields were updated
   * 
   * @param changes
   * @param addr
   * @return
   */
  private boolean isRelevantAddressFieldUpdated(RequestChangeContainer changes, Addr addr) {
    List<UpdatedNameAddrModel> addrChanges = changes.getAddressChanges(addr.getId().getAddrType(), addr.getId().getAddrSeq());
    if (addrChanges == null) {
      return false;
    }
    for (UpdatedNameAddrModel change : addrChanges) {
      if (!NON_RELEVANT_ADDRESS_FIELDS.contains(change.getDataField())) {
        return true;
      }
    }
    return false;
  }

  @Override
  public AutomationResult<OverrideOutput> doCountryFieldComputations(EntityManager entityManager, AutomationResult<OverrideOutput> results,
      StringBuilder details, OverrideOutput overrides, RequestData requestData, AutomationEngineData engineData) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }
}
