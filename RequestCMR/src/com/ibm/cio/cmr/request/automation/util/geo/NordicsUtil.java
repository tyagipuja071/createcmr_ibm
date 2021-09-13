package com.ibm.cio.cmr.request.automation.util.geo;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.commons.digester.Digester;
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
import com.ibm.cio.cmr.request.util.ConfigUtil;
import com.ibm.cmr.services.client.matching.MatchingResponse;
import com.ibm.cmr.services.client.matching.cmr.DuplicateCMRCheckResponse;

/**
 * 
 * @author Shivangi
 *
 */

public class NordicsUtil extends AutomationUtil {
  private static final Logger LOG = Logger.getLogger(NordicsUtil.class);

  // Denmark
  public static final String DK_COMME_LOCAL = "DKCOM";
  public static final String DK_INTER_LOCAL = "DKINT";
  public static final String DK_GOV_LOCAL = "DKGOV";
  public static final String DK_BUSPR_LOCAL = "DKBUS";
  public static final String DK_INTSO_LOCAL = "DKISO";
  public static final String DK_PRIPE_LOCAL = "DKPRI";
  public static final String DK_IBMEM_LOCAL = "DKIBM";
  // Finland
  public static final String FI_COMME_LOCAL = "FICOM";
  public static final String FI_INTER_LOCAL = "FIINT";
  public static final String FI_GOV_LOCAL = "FIGOV";
  public static final String FI_BUSPR_LOCAL = "FIBUS";
  public static final String FI_INTSO_LOCAL = "FIISO";
  public static final String FI_PRIPE_LOCAL = "FIPRI";
  public static final String FI_IBMEM_LOCAL = "FIIBM";
  // Norway and Sweden
  public static final String COMME_LOCAL = "COMME";
  public static final String INTER_LOCAL = "INTER";
  public static final String GOVRN_LOCAL = "GOVRN";
  public static final String BUSPR_LOCAL = "BUSPR";
  public static final String INTSO_LOCAL = "INTSO";
  public static final String PRIPE_LOCAL = "PRIPE";
  public static final String IBMEM_LOCAL = "IBMEM";
  // Cross
  public static final String CROSS_COMME = "CBCOM";
  public static final String CROSS_INTER = "CBINT";
  public static final String CROSS_BUSPR = "CBBUS";
  public static final String CROSS_INTSO = "CBISO";
  public static final String CROSS_IBMEM = "CBIBM";
  public static final String CROSS_PRIPE = "CBPRI";

  private static final List<String> SCENARIOS_COVERAGE = Arrays.asList(DK_COMME_LOCAL, FI_COMME_LOCAL, COMME_LOCAL, CROSS_COMME, DK_INTSO_LOCAL,
      FI_INTSO_LOCAL, INTSO_LOCAL, CROSS_INTSO, DK_GOV_LOCAL, FI_GOV_LOCAL);

  private static List<NordicsCovMapping> coverageMapping = new ArrayList<NordicsCovMapping>();

  @SuppressWarnings("unchecked")
  public NordicsUtil() {
    if (NordicsUtil.coverageMapping.isEmpty()) {
      Digester digester = new Digester();

      digester.setValidating(false);

      digester.addObjectCreate("mappings", ArrayList.class);
      digester.addObjectCreate("mappings/mapping", NordicsCovMapping.class);

      digester.addBeanPropertySetter("mappings/mapping/country", "country");
      digester.addBeanPropertySetter("mappings/mapping/isuCTC", "isuCTC");
      digester.addBeanPropertySetter("mappings/mapping/subIndustry", "subIndustry");
      digester.addBeanPropertySetter("mappings/mapping/sortl", "sortl");
      digester.addBeanPropertySetter("mappings/mapping/salesRep", "salesRep");
      digester.addBeanPropertySetter("mappings/mapping/exclude", "exclude");

      digester.addSetNext("mappings/mapping", "add");

      try {
        InputStream is = ConfigUtil.getResourceStream("nordics-mapping.xml");
        NordicsUtil.coverageMapping = (ArrayList<NordicsCovMapping>) digester.parse(is);

      } catch (Exception e) {
        LOG.error("Error occured while digesting xml.", e);
      }
    }
  }

  @Override
  public AutomationResult<OverrideOutput> doCountryFieldComputations(EntityManager entityManager, AutomationResult<OverrideOutput> results,
      StringBuilder details, OverrideOutput overrides, RequestData requestData, AutomationEngineData engineData) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean performScenarioValidation(EntityManager entityManager, RequestData requestData, AutomationEngineData engineData,
      AutomationResult<ValidationOutput> result, StringBuilder details, ValidationOutput output) {
    // TODO Auto-generated method stub
    Data data = requestData.getData();
    String scenario = data.getCustSubGrp();
    Addr zs01 = requestData.getAddress("ZS01");
    Addr zp01 = requestData.getAddress("ZP01");
    String customerName = getCustomerFullName(zs01);

    if ((DK_BUSPR_LOCAL.equals(scenario) || FI_BUSPR_LOCAL.equals(scenario) || BUSPR_LOCAL.equals(scenario) || CROSS_BUSPR.equals(scenario))
        && zp01 != null && !compareCustomerNames(zs01, zp01)) {
      details.append("Sold-to and Bill-to name are not identical for BP Scenario. Request will require CMDE review before proceeding.").append("\n");
      engineData.addNegativeCheckStatus("SOLDTO_BILLTO_DIFF", "Sold-to Bill-to name are not identical for BP Scenario.");
    }

    switch (scenario) {

    case COMME_LOCAL:
    case CROSS_COMME:
    case GOVRN_LOCAL:
    case DK_COMME_LOCAL:
    case DK_GOV_LOCAL:
    case FI_COMME_LOCAL:
    case FI_GOV_LOCAL:
      if (zp01 != null && !compareCustomerNames(zs01, zp01)) {
        details.append("Sold-to and Bill-to name are not identical. Request will require CMDE review before proceeding.").append("\n");
        engineData.addNegativeCheckStatus("SOLDTO_BILLTO_DIFF", "Sold-to and Bill-to name are not identical.");
      }
      break;

    case DK_BUSPR_LOCAL:
    case FI_BUSPR_LOCAL:
    case BUSPR_LOCAL:
    case CROSS_BUSPR:
      return doBusinessPartnerChecks(engineData, data.getPpsceid(), details);
    case DK_PRIPE_LOCAL:
    case FI_PRIPE_LOCAL:
    case PRIPE_LOCAL:
    case CROSS_PRIPE:
      return doPrivatePersonChecks(engineData, data.getCmrIssuingCntry(), zs01.getLandCntry(), customerName, details, false, requestData);
    }

    return true;

  }

  @Override
  public void filterDuplicateCMRMatches(EntityManager entityManager, RequestData requestData, AutomationEngineData engineData,
      MatchingResponse<DuplicateCMRCheckResponse> response) {
    String[] scenariosToBeChecked = { "BUSPR", "FIBUS", "DKBUS", "CBBUS" };
    String scenario = requestData.getData().getCustSubGrp();
    String[] kuklaBuspr = { "43", "44", "45" };

    if (Arrays.asList(scenariosToBeChecked).contains(scenario)) {
      List<DuplicateCMRCheckResponse> matches = response.getMatches();
      List<DuplicateCMRCheckResponse> filteredMatches = new ArrayList<DuplicateCMRCheckResponse>();
      for (DuplicateCMRCheckResponse match : matches) {
        if (StringUtils.isNotBlank(match.getCustClass())) {
          String kukla = match.getCustClass() != null ? match.getCustClass() : "";
          if (Arrays.asList(kuklaBuspr).contains(kukla) && Arrays.asList(scenariosToBeChecked).contains(scenario)) {
            filteredMatches.add(match);
          }
        }
      }
      // set filtered matches in response
      response.setMatches(filteredMatches);
    }
  }

  @Override
  public boolean performCountrySpecificCoverageCalculations(CalculateCoverageElement covElement, EntityManager entityManager,
      AutomationResult<OverrideOutput> results, StringBuilder details, OverrideOutput overrides, RequestData requestData,
      AutomationEngineData engineData, String covFrom, CoverageContainer container, boolean isCoverageCalculated) throws Exception {
    Data data = requestData.getData();
    String scenario = data.getCustSubGrp();
    String reqSubInd = data.getSubIndustryCd().substring(0, 1);
    String cntry = data.getCmrIssuingCntry();
    details.append("\n");
    if (!isCoverageCalculated && "34".equals(data.getIsuCd()) && "Q".equals(data.getClientTier()) && SCENARIOS_COVERAGE.contains(scenario)) {
      details.setLength(0); // clearing details
      overrides.clearOverrides();
      List<String> subIndList = new ArrayList<String>();
      if (!coverageMapping.isEmpty()) {
        String[] subInd = null;
        for (NordicsCovMapping mapping : coverageMapping) {
          if (!StringUtils.isEmpty(mapping.getSubIndustry())) {
            subInd = mapping.getSubIndustry().replaceAll("\n", "").replaceAll(" ", "").split(",");
            subIndList = Arrays.asList(subInd);
          }
          if (mapping.getCountry().equals(cntry) && !subIndList.isEmpty()
              && ((mapping.isExclude() && !subIndList.contains(reqSubInd)) || (!mapping.isExclude() && subIndList.contains(reqSubInd)))) {
            details.append("Calculating coverage using 34Q logic.").append("\n");

            details.append("SORTL : " + mapping.getSortl() + " calculated using 34-Q mapping.").append("\n");
            overrides.addOverride(AutomationElementRegistry.GBL_CALC_COV, "DATA", "SEARCH_TERM", data.getSearchTerm(), mapping.getSortl());

            details.append("Sales Rep : " + mapping.getSalesRep() + " calculated using 34-Q mapping.");
            overrides.addOverride(AutomationElementRegistry.GBL_CALC_COV, "DATA", "REP_TEAM_MEMBER_NO", data.getRepTeamMemberNo(),
                mapping.getSalesRep());

          }
        }

      } else {
        details.setLength(0);
        overrides.clearOverrides();
        details.append("Coverage could not be calculated through Buying group or 34Q logic.\n Skipping coverage calculation.").append("\n");
        results.setResults("Skipped");
      }

    }
    return true;
  }

}
