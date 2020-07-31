package com.ibm.cio.cmr.request.automation.util.geo;

import java.util.ArrayList;
import java.util.Arrays;
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
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.util.RequestUtils;
import com.ibm.cio.cmr.request.util.SystemLocation;
import com.ibm.cio.cmr.request.util.dnb.DnBUtil;
import com.ibm.cmr.services.client.dnb.DnBCompany;
import com.ibm.cmr.services.client.matching.MatchingResponse;
import com.ibm.cmr.services.client.matching.cmr.DuplicateCMRCheckResponse;
import com.ibm.cmr.services.client.matching.dnb.DnBMatchingResponse;

public class UKIUtil extends AutomationUtil {

  private static final Logger LOG = Logger.getLogger(UKIUtil.class);
  public static final String SCENARIO_BUSINESS_PARTNER = "BUSPR";
  public static final String SCENARIO_COMMERCIAL = "COMME";
  public static final String SCENARIO_GOVERNMENT = "GOVRN";
  public static final String SCENARIO_DATACENTER = "DC";
  public static final String SCENARIO_IGF = "IGF";
  public static final String SCENARIO_INTERNAL_FSL = "INFSL";
  public static final String SCENARIO_INTERNAL = "INTER";
  public static final String SCENARIO_PRIVATE_PERSON = "PRICU";
  public static final String SCENARIO_THIRD_PARTY = "THDPT";
  public static final String SCENARIO_CROSSBORDER = "CROSS";
  public static final String SCENARIO_CROSS_GOVERNMENT = "XGOVR";
  public static final String SCENARIO_CROSS_IGF = "XIGF";
  private static final List<String> SCENARIOS_TO_SKIP_COVERAGE = Arrays.asList(SCENARIO_INTERNAL, SCENARIO_PRIVATE_PERSON, SCENARIO_BUSINESS_PARTNER);

  @Override
  public boolean performScenarioValidation(EntityManager entityManager, RequestData requestData, AutomationEngineData engineData,
      AutomationResult<ValidationOutput> result, StringBuilder details, ValidationOutput output) {
    Data data = requestData.getData();
    Addr zs01 = requestData.getAddress("ZS01");
    String custNm1 = zs01.getCustNm1();
    String custNm2 = !StringUtils.isBlank(zs01.getCustNm2()) ? " " + zs01.getCustNm2() : "";
    String customerName = custNm1 + custNm2;
    String scenario = data.getCustSubGrp();

    Addr zi01 = requestData.getAddress("ZI01");
    custNm1 = zi01.getCustNm1();
    custNm2 = !StringUtils.isBlank(zi01.getCustNm2()) ? " " + zi01.getCustNm2() : "";
    String customerNameZI01 = custNm1 + custNm2;
    if (StringUtils.isBlank(scenario)) {
      details.append("Scenario not correctly specified on the request");
      engineData.addNegativeCheckStatus("_atNoScenario", "Scenario not correctly specified on the request");
      return true;
    }
    LOG.info("Starting scenario validations for Request ID " + data.getId().getReqId());
    LOG.debug("Scenario to check: " + scenario);
    if (!SCENARIO_THIRD_PARTY.equals(scenario)
        && ((customerNameZI01.toUpperCase().contains("C/O") || customerNameZI01.toUpperCase().contains("CAREOF")
            || customerNameZI01.toUpperCase().contains("CARE OF")) || customerNameZI01.toUpperCase().matches("^VR[0-9]{3}.+$"))) {
      details.append("Third Party Scenario should be selected.").append("\n");
      engineData.addRejectionComment("OTH", "Third Party Scenario should be selected.", "", "");
      return false;
    } else if ((SCENARIO_COMMERCIAL.equals(scenario) || SCENARIO_GOVERNMENT.equals(scenario) || SCENARIO_CROSSBORDER.equals(scenario)
        || SCENARIO_CROSS_GOVERNMENT.equals(scenario)) && !addressEquals(zs01, zi01)) {
      details.append("Billing and Installing addresses are not same. Request will require CMDE review before proceeding.").append("\n");
      engineData.addNegativeCheckStatus("BILL_INSTALL_DIFF", "Billing and Installing addresses are not same.");
    }

    switch (scenario) {
    case SCENARIO_BUSINESS_PARTNER:
      return doBusinessPartnerChecks(engineData, data.getPpsceid(), details);
    case SCENARIO_PRIVATE_PERSON:
      return doPrivatePersonChecks(engineData, data.getCmrIssuingCntry(), zs01.getLandCntry(), customerName, details, false);
    case SCENARIO_INTERNAL:
      if (!customerName.contains("IBM") && !customerNameZI01.contains("IBM")) {
        details.append("Mailing and Billing addresses should have IBM in them.");
        engineData.addRejectionComment("OTH", "Mailing and Billing addresses should have IBM in them.", "", "");
        return false;
      }
      break;
    case SCENARIO_THIRD_PARTY:
      if (customerName.toUpperCase().equals(customerNameZI01.toUpperCase())) {
        details.append("Customer Names on installing and billing address should be different for Third Party Scenario").append("\n");
        engineData.addRejectionComment("OTH", "Customer Names on installing and billing address should be different for Third Party Scenario", "",
            "");
        return false;
      } else if (!(customerNameZI01.toUpperCase().contains("C/O") || customerNameZI01.toUpperCase().contains("CAREOF")
          || customerNameZI01.toUpperCase().contains("CARE OF")) && !customerNameZI01.toUpperCase().matches("^VR[0-9]{3}.+$")) {
        details.append("The request does not meet the criteria for Third Party Scenario.").append("\n");
        engineData.addRejectionComment("OTH", "The request does not meet the criteria for Third Party Scenario.", "", "");
        return false;
      }
      break;
    case SCENARIO_DATACENTER:
      if (!customerNameZI01.toUpperCase().contains("DATACENTER") && !customerNameZI01.toUpperCase().contains("DATA CENTER")) {
        details.append("The request does not meet the criteria for Data Center Scenario.").append("\n");
        engineData.addRejectionComment("OTH", "The request does not meet the criteria for Data Center Scenario.", "", "");
        return false;
      }
      break;
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

    Admin admin = requestData.getAdmin();
    Data data = requestData.getData();
    String scenario = data.getCustSubGrp();
    if (!"C".equals(admin.getReqType())) {
      details.append("Field Computation skipped for Updates.");
      results.setResults("Skipped");
      results.setDetails(details.toString());
      return results;
    }

    if (SCENARIO_THIRD_PARTY.equals(scenario) || SCENARIO_INTERNAL_FSL.equals(scenario)) {
      Addr zi01 = requestData.getAddress("ZI01");
      boolean hasValidMatches = false;
      boolean highQualityMatchExists = false;
      MatchingResponse<DnBMatchingResponse> response = DnBUtil.getMatches(requestData, "ZI01");
      hasValidMatches = DnBUtil.hasValidMatches(response);
      if (response != null && response.getMatched()) {
        List<DnBMatchingResponse> dnbMatches = response.getMatches();
        if (hasValidMatches) {
          // actions to be performed only when matches with high confidence are
          // found
          for (DnBMatchingResponse dnbRecord : dnbMatches) {
            boolean closelyMatches = DnBUtil.closelyMatchesDnb(data.getCmrIssuingCntry(), zi01, admin, dnbRecord);
            if (closelyMatches) {
              engineData.put("ZI01_DNB_MATCH", dnbRecord);
              highQualityMatchExists = true;
              details.append("High Quality DnB Match found for Installing address.\n");
              details.append("Overriding ISIC and Sub Industry Code using DnB Match retrieved.\n");
              LOG.debug("Connecting to D&B details service..");
              DnBCompany dnbData = DnBUtil.getDnBDetails(dnbRecord.getDunsNo());
              if (dnbData != null) {
                overrides.addOverride(AutomationElementRegistry.GBL_FIELD_COMPUTE, "DATA", "ISIC_CD", data.getIsicCd(), dnbData.getIbmIsic());
                details.append("ISIC =  " + dnbData.getIbmIsic() + " (" + dnbData.getIbmIsicDesc() + ")").append("\n");
                String subInd = RequestUtils.getSubIndustryCd(entityManager, dnbData.getIbmIsic(), data.getCmrIssuingCntry());
                if (subInd != null) {
                  overrides.addOverride(AutomationElementRegistry.GBL_FIELD_COMPUTE, "DATA", "SUB_INDUSTRY_CD", data.getSubIndustryCd(), subInd);
                  details.append("Subindustry Code  =  " + subInd).append("\n");
                }
              }
              results.setResults("Calculated.");
              results.setProcessOutput(overrides);
              break;
            }
          }
        }
      }
      if (!highQualityMatchExists && "C".equals(admin.getReqType())) {
        details.append("No high quality matches found for Installing Address, setting ISIC to 7499.");
        overrides.addOverride(AutomationElementRegistry.GBL_FIELD_COMPUTE, "DATA", "ISIC_CD", data.getIsicCd(), "7499");
        String subInd = RequestUtils.getSubIndustryCd(entityManager, "7499", data.getCmrIssuingCntry());
        if (subInd != null) {
          overrides.addOverride(AutomationElementRegistry.GBL_FIELD_COMPUTE, "DATA", "SUB_INDUSTRY_CD", data.getSubIndustryCd(), subInd);
        }
        engineData.addPositiveCheckStatus(AutomationEngineData.SKIP_GBG);
        results.setResults("Calculated.");
        results.setProcessOutput(overrides);
      }
    } else {
      details.append("No specific fields to calculate.");
      results.setResults("Skipped.");
      results.setProcessOutput(overrides);
    }
    results.setDetails(details.toString());
    LOG.debug(results.getDetails());
    return results;
  }

  @Override
  public void filterDuplicateCMRMatches(EntityManager entityManager, RequestData requestData, AutomationEngineData engineData,
      MatchingResponse<DuplicateCMRCheckResponse> response) {
    String scenario = requestData.getData().getCustSubGrp();
    String[] custClassValuesToCheck = { "43", "45", "46" };
    if (UKIUtil.SCENARIO_BUSINESS_PARTNER.equals(scenario)) {
      List<DuplicateCMRCheckResponse> matches = response.getMatches();
      List<DuplicateCMRCheckResponse> filteredMatches = new ArrayList<DuplicateCMRCheckResponse>();
      for (DuplicateCMRCheckResponse match : matches) {
        if (StringUtils.isNotBlank(match.getCustClass())) {
          String custClass = match.getCustClass();
          if (Arrays.asList(custClassValuesToCheck).contains(custClass)) {
            filteredMatches.add(match);
          }
        }

      }
      // set filtered matches in response
      if (!filteredMatches.isEmpty()) {
        response.setMatches(filteredMatches);
      }
    }

  }

  @Override
  public boolean performCountrySpecificCoverageCalculations(CalculateCoverageElement covElement, EntityManager entityManager,
      AutomationResult<OverrideOutput> results, StringBuilder details, OverrideOutput overrides, RequestData requestData,
      AutomationEngineData engineData, String covFrom, CoverageContainer container, boolean isCoverageCalculated) throws Exception {

    // override 32S logic
    if (!"C".equals(requestData.getAdmin().getReqType())) {
      details.append("Coverage Calculation skipped for Updates.");
      results.setResults("Skipped");
      results.setDetails(details.toString());
      return true;
    }

    Data data = requestData.getData();
    String scenario = data.getCustSubGrp();

    if ((!isCoverageCalculated
        || ((SCENARIO_THIRD_PARTY.equals(scenario) || SCENARIO_INTERNAL_FSL.equals(scenario)) && engineData.get("ZI01_DNB_MATCH") != null))
        && !SCENARIOS_TO_SKIP_COVERAGE.contains(scenario)) {
      details.setLength(0);
      overrides.clearOverrides();
      UkiFieldsContainer fields = null;
      if (SystemLocation.UNITED_KINGDOM.equals(data.getCmrIssuingCntry())) {
        fields = calculate32SValuesForUK(entityManager, data.getIsuCd(), data.getClientTier(), data.getIsicCd());
      }
      if (fields != null) {
        details.append("Coverage calculated successfully using 32S logic.").append("\n");
        details.append("Sales Rep : " + fields.getSalesRep()).append("\n");
        details.append("SBO : " + fields.getSbo()).append("\n");
        overrides.addOverride(AutomationElementRegistry.GBL_CALC_COV, "DATA", "SALES_BO_CD", data.getSalesBusOffCd(), fields.getSbo());
        overrides.addOverride(AutomationElementRegistry.GBL_CALC_COV, "DATA", "REP_TEAM_MEMBER_NO", data.getRepTeamMemberNo(), fields.getSalesRep());
        results.setResults("Calculated");
        results.setDetails(details.toString());
      } else if (StringUtils.isNotBlank(data.getRepTeamMemberNo()) && StringUtils.isNotBlank(data.getSalesBusOffCd())) {
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
        engineData.addNegativeCheckStatus("_ukiCoverage", msg);
      }
    }
    return true;

  }

  private UkiFieldsContainer calculate32SValuesForUK(EntityManager entityManager, String isuCd, String clientTier, String isicCd) {
    if ("32".equals(isuCd) && StringUtils.isNotBlank(clientTier) && StringUtils.isNotBlank(isicCd)) {
      UkiFieldsContainer container = new UkiFieldsContainer();
      String sql = ExternalizedQuery.getSql("QUERY.UK.GET.SBOSR_FOR_ISIC");
      PreparedQuery query = new PreparedQuery(entityManager, sql);
      query.setParameter("ISU_CD", isuCd);
      query.setParameter("ISIC_CD", isicCd);
      query.setParameter("CLIENT_TIER", clientTier);
      query.setForReadOnly(true);
      List<Object[]> results = query.getResults();
      if (results != null && results.size() == 1) {
        String sbo = (String) results.get(0)[0];
        String salesRep = (String) results.get(0)[1];
        container.setSbo(sbo);
        container.setSalesRep(salesRep);
        return container;
      }
    }
    return null;

  }

  private class UkiFieldsContainer {
    private String sbo;
    private String salesRep;

    public String getSbo() {
      return sbo;
    }

    public void setSbo(String sbo) {
      this.sbo = sbo;
    }

    public String getSalesRep() {
      return salesRep;
    }

    public void setSalesRep(String salesRep) {
      this.salesRep = salesRep;
    }

  }
}
