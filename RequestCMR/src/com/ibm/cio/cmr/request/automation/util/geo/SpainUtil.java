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
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.util.SystemLocation;

/**
 * {@link AutomationUtil} for Spain specific validations
 * 
 *
 */
public class SpainUtil extends AutomationUtil {

  private static final Logger LOG = Logger.getLogger(SpainUtil.class);
  private static final String SCENARIO_BUSINESS_PARTNER = "BUSPR";
  private static final String SCENARIO_BUSINESS_PARTNER_CROSS = "XBP";
  private static final String SCENARIO_PRIVATE_CUSTOMER = "PRICU";


  @Override
  public boolean performScenarioValidation(EntityManager entityManager, RequestData requestData, AutomationEngineData engineData,
      AutomationResult<ValidationOutput> result, StringBuilder details, ValidationOutput output) {
    Data data = requestData.getData();
    String scenario = data.getCustSubGrp();
    Addr soldTo = requestData.getAddress("ZS01");
    String customerName = soldTo.getCustNm1() + (!StringUtils.isBlank(soldTo.getCustNm2()) ? " " + soldTo.getCustNm2() : "");

    LOG.info("Starting scenario validations for Request ID " + data.getId().getReqId());
    
      LOG.debug("Scenario to check: " + scenario);

      switch (scenario) {
      case SCENARIO_PRIVATE_CUSTOMER:
        return doPrivatePersonChecks(engineData, SystemLocation.SPAIN, soldTo.getLandCntry(), customerName, details,
            true);
      case SCENARIO_BUSINESS_PARTNER:
      case SCENARIO_BUSINESS_PARTNER_CROSS:
        return doBusinessPartnerChecks(engineData, data.getPpsceid(), details);
      }
    return true;
  }

  @Override
  public AutomationResult<OverrideOutput> doCountryFieldComputations(EntityManager entityManager,
			AutomationResult<OverrideOutput> results, StringBuilder details, OverrideOutput overrides,
			RequestData requestData, AutomationEngineData engineData) throws Exception {
	// TODO Auto-generated method stub
	return null;
  }

  @Override
  protected List<String> getCountryLegalEndings() {
    return Arrays.asList("SL","S.L.","S.A.","SLL","SA","LTD","SOCIEDAD LIMITADA","SLP","S.C.C.L.","SLU","SAU","S.A.U","C.B.","S.E.E.");
  }
}
