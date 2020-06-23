/**
 * 
 */
package com.ibm.cio.cmr.request.automation.util.geo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.automation.AutomationElementRegistry;
import com.ibm.cio.cmr.request.automation.AutomationEngineData;
import com.ibm.cio.cmr.request.automation.RequestData;
import com.ibm.cio.cmr.request.automation.impl.gbl.CalculateCoverageElement;
import com.ibm.cio.cmr.request.automation.out.AutomationResult;
import com.ibm.cio.cmr.request.automation.out.OverrideOutput;
import com.ibm.cio.cmr.request.automation.out.ValidationOutput;
import com.ibm.cio.cmr.request.automation.util.AutomationUtil;
import com.ibm.cio.cmr.request.automation.util.CoverageContainer;
import com.ibm.cio.cmr.request.automation.util.RequestChangeContainer;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.model.window.UpdatedNameAddrModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
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
  private static final String SCENARIO_BUSINESS_PARTNER_CROSS = "XBP";
  private static final String SCENARIO_CROSS_COMMERICAL = "XCOM";

  private static final List<String> RELEVANT_ADDRESSES = Arrays.asList(CmrConstants.RDC_SOLD_TO, CmrConstants.RDC_BILL_TO,
      CmrConstants.RDC_INSTALL_AT, CmrConstants.RDC_SHIP_TO);

  private static final List<String> NON_RELEVANT_ADDRESS_FIELDS = Arrays.asList("Att. Person", "Phone #", "FAX", "Customer Name 4");

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
    case SCENARIO_BUSINESS_PARTNER_CROSS:
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

    Data data = requestData.getData();
    Admin admin = requestData.getAdmin();
    if ("C".equals(admin.getReqType())) {
      String scenario = data.getCustSubGrp();
      LOG.info("Starting Field Computations for Request ID " + data.getId().getReqId());
      if (StringUtils.isNotBlank(scenario)
          && !(SCENARIO_BUSINESS_PARTNER.equals(scenario) || SCENARIO_IBM_EMPLOYEE.equals(scenario) || SCENARIO_PRIVATE_CUSTOMER.equals(scenario)
              || SCENARIO_INTERNAL.equals(scenario) || SCENARIO_BUSINESS_PARTNER_CROSS.equals(scenario))
          && data.getSubIndustryCd() != null && data.getSubIndustryCd().startsWith("B") && "32".equals(data.getIsuCd())
          && "S".equals(data.getClientTier())) {
        details.append("Found IMS value 'B' on the request, setting ISU-CTC as 32-N").append("\n");
        overrides.addOverride(AutomationElementRegistry.GBL_FIELD_COMPUTE, "DATA", "ISU_CD", data.getIsuCd(), "32");
        overrides.addOverride(AutomationElementRegistry.GBL_FIELD_COMPUTE, "DATA", "CLIENT_TIER", data.getClientTier(), "N");
        results.setResults("Computed");
      } else {
        details.append("No specific fields to compute.\n");
        results.setResults("Skipped");
      }
    } else {
      details.append("No specific fields to compute.\n");
      results.setResults("Skipped");
    }
    results.setDetails(details.toString());

    return results;
  }

  @Override
  public boolean performCountrySpecificCoverageCalculations(CalculateCoverageElement covElement, EntityManager entityManager,
      AutomationResult<OverrideOutput> results, StringBuilder details, OverrideOutput overrides, RequestData requestData,
      AutomationEngineData engineData, String covFrom, CoverageContainer container, boolean isCoverageCalculated) throws Exception {
    if (!"C".equals(requestData.getAdmin().getReqType())) {
      details.append("Coverage Calculation skipped for Updates.");
      results.setResults("Skipped");
      results.setDetails(details.toString());
      overrides.clearOverrides();
      return true;
    }
    Data data = requestData.getData();
    String scenario = data.getCustSubGrp();
    String sbo = "";
    LOG.info("Starting coverage calculations for Request ID " + requestData.getData().getId().getReqId());
    switch (scenario) {
    case SCENARIO_COMMERCIAL:
    case SCENARIO_CROSS_COMMERICAL:
      if (!isCoverageCalculated) {
        sbo = getSBOFromIMS(entityManager, data.getSubIndustryCd(), data.getIsuCd(), data.getClientTier());
      }
      break;
    case SCENARIO_PRIVATE_CUSTOMER:
      sbo = getSBOFromIMS(entityManager, data.getSubIndustryCd(), data.getIsuCd(), data.getClientTier());
      break;
    }

    if (sbo != null) {
      details.append("Setting SBO to " + sbo + " based on IMS mapping rules.");
      overrides.addOverride(covElement.getProcessCode(), "DATA", "SALES_BO_CD", data.getSalesBusOffCd(), sbo);
      engineData.addPositiveCheckStatus(AutomationEngineData.COVERAGE_CALCULATED);
      results.setResults("Calculated");
    } else if (!isCoverageCalculated) {
      String sboReq = data.getSalesBusOffCd();
      if (!StringUtils.isBlank(sboReq)) {
        String msg = "No valid SBO mapping from request data. Using SBO " + sboReq + " from request.";
        details.append(msg);
        results.setResults("Calculated");
        results.setDetails(details.toString());
      } else {
        String msg = "Coverage cannot be calculated. No valid SBO mapping from request data.";
        details.append(msg);
        results.setResults("Cannot Calculate");
        results.setDetails(details.toString());
        engineData.addNegativeCheckStatus("_atSbo", msg);
      }
    }

    return true;

  }

  private String getSBOFromIMS(EntityManager entityManager, String subIndustryCd, String isuCd, String clientTier) {
    List<String> sboValues = new ArrayList<>();
    String isuCtc = (StringUtils.isNotBlank(isuCd) ? isuCd : "") + (StringUtils.isNotBlank(clientTier) ? clientTier : "");
    if (StringUtils.isNotBlank(subIndustryCd) && ("32S".equals(isuCtc) || "32N".equals(isuCtc) || "32T".equals(isuCtc))) {
      String ims = subIndustryCd.substring(0, 1);
      String sql = ExternalizedQuery.getSql("AUTO.AT.GET_SBOLIST_FROM_ISUCTC");
      PreparedQuery query = new PreparedQuery(entityManager, sql);
      query.setParameter("ISU", "%" + isuCtc + "%");
      query.setParameter("ISSUING_CNTRY", SystemLocation.AUSTRIA);
      query.setParameter("CLIENT_TIER", "%" + ims + "%");
      query.setForReadOnly(true);
      sboValues = query.getResults(String.class);
    } else {
      String sql = ExternalizedQuery.getSql("AUTO.AT.GET_SBOLIST_FROM_ISU");
      PreparedQuery query = new PreparedQuery(entityManager, sql);
      query.setParameter("ISU", "%" + isuCtc + "%");
      query.setParameter("ISSUING_CNTRY", SystemLocation.AUSTRIA);
      sboValues = query.getResults(String.class);
    }
    if (sboValues != null && sboValues.size() == 1) {
      return sboValues.get(0);
    } else {
      return "";
    }
  }

}
