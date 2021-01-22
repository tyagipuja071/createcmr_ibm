package com.ibm.cio.cmr.request.automation.util.geo;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.automation.AutomationElementRegistry;
import com.ibm.cio.cmr.request.automation.AutomationEngineData;
import com.ibm.cio.cmr.request.automation.RequestData;
import com.ibm.cio.cmr.request.automation.impl.gbl.CalculateCoverageElement;
import com.ibm.cio.cmr.request.automation.out.AutomationResult;
import com.ibm.cio.cmr.request.automation.out.OverrideOutput;
import com.ibm.cio.cmr.request.automation.out.ValidationOutput;
import com.ibm.cio.cmr.request.automation.util.AutomationUtil;
import com.ibm.cio.cmr.request.automation.util.CoverageContainer;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;

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
  public static final String SCENARIO_DATA_CENTER = "BEDAT";

  // Lux
  public static final String SCENARIO_CROSS_LU = "LUCRO";
  public static final String SCENARIO_LOCAL_PUBLIC_LU = "LUPUB";
  public static final String SCENARIO_PRIVATE_CUSTOMER_LU = "LUPRI";
  public static final String SCENARIO_LOCAL_COMMERCIAL_LU = "LUCOM";
  public static final String SCENARIO_INTERNAL_LU = "LUINT";
  public static final String SCENARIO_INTERNAL_SO_LU = "LUISO";
  public static final String SCENARIO_THIRD_PARTY_LU = "LU3PA";
  public static final String SCENARIO_BP_LOCAL_LU = "LUBUS";
  public static final String SCENARIO_DATA_CENTER_LU = "LUDAT";

  @Override
  public boolean performScenarioValidation(EntityManager entityManager, RequestData requestData, AutomationEngineData engineData,
      AutomationResult<ValidationOutput> result, StringBuilder details, ValidationOutput output) {

    Data data = requestData.getData();
    String scenario = data.getCustSubGrp();
    Addr zs01 = requestData.getAddress("ZS01");
    Addr zi01 = requestData.getAddress("ZI01");
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

    case SCENARIO_INTERNAL_SO:
    case SCENARIO_INTERNAL_SO_LU:
      if (zi01 == null) {
        details.append("Install-at address should be present for Interna SO Scenario.").append("\n");
        engineData.addRejectionComment("OTH", "Install-at address should be present for Internal SO Scenario.", "", "");
        return false;
      }
      break;
    case SCENARIO_PRIVATE_CUSTOMER:
    case SCENARIO_PRIVATE_CUSTOMER_LU:
      return doPrivatePersonChecks(engineData, data.getCmrIssuingCntry(), zs01.getLandCntry(), customerName, details, false, requestData);

    case SCENARIO_THIRD_PARTY:
    case SCENARIO_THIRD_PARTY_LU:
    case SCENARIO_DATA_CENTER:
    case SCENARIO_DATA_CENTER_LU:
      details.append("Processor Review will be required for Third Party Scenario/Data Center.\n");
      engineData.addNegativeCheckStatus("Scenario_Validation", "3rd Party/Data Center request will require CMDE review before proceeding.\n");
      break;
    }

    return true;
  }

  @Override
  public boolean performCountrySpecificCoverageCalculations(CalculateCoverageElement covElement, EntityManager entityManager,
      AutomationResult<OverrideOutput> results, StringBuilder details, OverrideOutput overrides, RequestData requestData,
      AutomationEngineData engineData, String covFrom, CoverageContainer container, boolean isCoverageCalculated) throws Exception {

    if (!"C".equals(requestData.getAdmin().getReqType())) {
      details.append("Coverage Calculation skipped for Updates.");
      results.setResults("Skipped");
      results.setDetails(details.toString());
      return true;
    }

    Data data = requestData.getData();
    String coverageId = container.getFinalCoverage();

    if (isCoverageCalculated && StringUtils.isNotBlank(coverageId) && CalculateCoverageElement.COV_BG.equals(covFrom)) {
      engineData.addPositiveCheckStatus(AutomationEngineData.COVERAGE_CALCULATED);
    } else if (!isCoverageCalculated) {
      // if not calculated using bg/gbg try calculation using 32/S logic
      details.setLength(0);// clear string builder
      overrides.clearOverrides(); // clear existing overrides

      BeLuxFieldsContainer fields = calculate32SValuesFromIMSBeLux(entityManager, data.getCmrIssuingCntry(), data.getCountryUse(),
          data.getSubIndustryCd(), data.getIsuCd(), data.getClientTier());
      if (fields != null) {
        details.append("Coverage calculated successfully using 32S logic.").append("\n");
        details.append("Sales Rep : " + fields.getSalesRep()).append("\n");
        overrides.addOverride(AutomationElementRegistry.GBL_CALC_COV, "DATA", "SEARCH_TERM", data.getSearchTerm(), fields.getSalesRep());
        if (StringUtils.isNotBlank(fields.getSalesRep())) {
          String sbo = "";
          if (data.getCountryUse().equalsIgnoreCase("624")) {
            sbo = "0" + fields.getSalesRep().substring(0, 2) + "0000";
          } else if (data.getCountryUse().equalsIgnoreCase("624LU")) {
            sbo = "0" + fields.getSalesRep().substring(0, 2) + "0001";
          }
          details.append("SBO : " + sbo).append("\n");
          overrides.addOverride(AutomationElementRegistry.GBL_CALC_COV, "DATA", "SALES_BO_CD", data.getSalesBusOffCd(), sbo);
        }
        results.setResults("Calculated");
        results.setDetails(details.toString());
      } else if (StringUtils.isNotBlank(data.getSearchTerm()) && StringUtils.isNotBlank(data.getSalesBusOffCd())) {
        details.append("Coverage could not be calculated using 32S logic. Using values from request").append("\n");
        details.append("Sales Rep : " + data.getRepTeamMemberNo()).append("\n");
        details.append("SBO : " + data.getSalesBusOffCd()).append("\n");
        results.setResults("Calculated");
        results.setDetails(details.toString());
      } else {
        String msg = "Coverage cannot be calculated. No valid 32S mapping found from request data.";
        details.append(msg);
        results.setResults("Cannot Calculate");
        results.setDetails(details.toString());
        engineData.addNegativeCheckStatus("_beluxCoverage", msg);
      }
    }
    return true;
  }

  private BeLuxFieldsContainer calculate32SValuesFromIMSBeLux(EntityManager entityManager, String cmrIssuingctry, String countryUse,
      String subIndustryCd, String isuCd, String clientTier) {
    BeLuxFieldsContainer container = new BeLuxFieldsContainer();
    List<Object[]> salesRepRes = new ArrayList<>();
    String isuCtc = (StringUtils.isNotBlank(isuCd) ? isuCd : "") + (StringUtils.isNotBlank(clientTier) ? clientTier : "");
    String geoCd = countryUse.substring(3, 5);
    String cmrCntry = cmrIssuingctry + geoCd;
    if (StringUtils.isNotBlank(subIndustryCd) && ("32S".equals(isuCtc))) {
      String ims = subIndustryCd.substring(0, 1);
      String sql = ExternalizedQuery.getSql("QUERY.GET.SRLIST.BYISUCTC");
      PreparedQuery query = new PreparedQuery(entityManager, sql);
      query.setParameter("ISU", "%" + isuCtc + "%");
      query.setParameter("ISSUING_CNTRY", cmrCntry);
      query.setParameter("CLIENT_TIER", "%" + ims + "%");
      query.setForReadOnly(true);
      salesRepRes = query.getResults();
    } else {
      String sql = ExternalizedQuery.getSql("QUERY.GET.SRLIST.BYISU");
      PreparedQuery query = new PreparedQuery(entityManager, sql);
      query.setParameter("ISU", "%" + isuCtc + "%");
      query.setParameter("ISSUING_CNTRY", cmrCntry);
      salesRepRes = query.getResults();
    }
    if (salesRepRes != null && salesRepRes.size() == 1) {
      String salesRep = (String) salesRepRes.get(0)[1];
      container.setSalesRep(salesRep);
      return container;
    } else {
      return null;
    }

  }

  private class BeLuxFieldsContainer {
    private String salesRep;

    public String getSalesRep() {
      return salesRep;
    }

    public void setSalesRep(String salesRep) {
      this.salesRep = salesRep;
    }

  }

  @Override
  public AutomationResult<OverrideOutput> doCountryFieldComputations(EntityManager entityManager, AutomationResult<OverrideOutput> results,
      StringBuilder details, OverrideOutput overrides, RequestData requestData, AutomationEngineData engineData) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

}
