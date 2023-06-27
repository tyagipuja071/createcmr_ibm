package com.ibm.cio.cmr.request.automation.util.geo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;

import org.apache.commons.lang3.StringUtils;
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
import com.ibm.cio.cmr.request.automation.util.ScenarioExceptionsUtil;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.model.window.UpdatedDataModel;
import com.ibm.cio.cmr.request.model.window.UpdatedNameAddrModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.ui.PageManager;
import com.ibm.cio.cmr.request.util.BluePagesHelper;
import com.ibm.cio.cmr.request.util.Person;
import com.ibm.cio.cmr.request.util.RequestUtils;
import com.ibm.cio.cmr.request.util.SystemLocation;
import com.ibm.cmr.services.client.matching.MatchingResponse;
import com.ibm.cmr.services.client.matching.cmr.DuplicateCMRCheckResponse;
import com.ibm.cmr.services.client.matching.dnb.DnBMatchingResponse;
import com.ibm.cmr.services.client.matching.gbg.GBGResponse;

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
  public static final String SCENARIO_IBMEM_BE = "IBMEM";

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
  public static final String SCENARIO_IBMEM_LU = "LUIBM";
  private static final String QUERY_BG_SBO_BENELUX = "AUTO.COV.GET_COV_FROM_BG_ES_UK";
  
  private static final List<String> RELEVANT_ADDRESSES = Arrays.asList(CmrConstants.RDC_SOLD_TO, CmrConstants.RDC_BILL_TO,
      CmrConstants.RDC_INSTALL_AT, CmrConstants.RDC_SHIP_TO, CmrConstants.RDC_SECONDARY_SOLD_TO, CmrConstants.RDC_PAYGO_BILLING);
  private static final List<String> NON_RELEVANT_ADDRESS_FIELDS = Arrays.asList("Attention Person", "Phone #");

  @Override
  public boolean performScenarioValidation(EntityManager entityManager, RequestData requestData, AutomationEngineData engineData,
      AutomationResult<ValidationOutput> result, StringBuilder details, ValidationOutput output) {

    ScenarioExceptionsUtil scenarioExceptions = (ScenarioExceptionsUtil) engineData.get("SCENARIO_EXCEPTIONS");
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

      if (!StringUtils.equals(zs01.getLandCntry(), zp01.getLandCntry())) {
        scenarioExceptions.setCheckVATForDnB(false);
      }
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
      String customerNameCombined = zs01.getCustNm1() + (StringUtils.isNotBlank(zs01.getCustNm2()) ? " " + zs01.getCustNm2() : "");
      if (customerNameCombined.contains("IBM")) {
        details.append("Scenario sub-type should be Internal or Internal SO").append("\n");
        engineData.addRejectionComment("OTH", "Change Scenario sub-type to Internal or Internal SO.", "", "");
        return false;
      }
      break;
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
      String customerNameFull = zs01.getCustNm1() + (StringUtils.isNotBlank(zs01.getCustNm2()) ? " " + zs01.getCustNm2() : "");
      return doPrivatePersonChecks(engineData, data.getCmrIssuingCntry(), zs01.getLandCntry(), customerNameFull, details, false, requestData);

    case SCENARIO_THIRD_PARTY:
    case SCENARIO_THIRD_PARTY_LU:
    case SCENARIO_DATA_CENTER:
    case SCENARIO_DATA_CENTER_LU:
      details.append("Processor Review will be required for Third Party Scenario/Data Center.\n");
      engineData.addNegativeCheckStatus("Scenario_Validation", "3rd Party/Data Center request will require CMDE review before proceeding.\n");
      break;
    case SCENARIO_IBMEM_LU:
    case SCENARIO_IBMEM_BE:
      Person person = null;
      if (StringUtils.isNotBlank(zs01.getCustNm1())) {
        try {
          String mainCustName = zs01.getCustNm1() + (StringUtils.isNotBlank(zs01.getCustNm2()) ? " " + zs01.getCustNm2() : "");
          person = BluePagesHelper.getPersonByName(mainCustName, data.getCmrIssuingCntry());
          if (person == null) {
            engineData.addRejectionComment("OTH", "Employee details not found in IBM People.", "", "");
            details.append("Employee details not found in IBM People.").append("\n");
            return false;
          } else {
            details.append("Employee details validated with IBM BluePages for " + person.getName() + "(" + person.getEmail() + ").").append("\n");
          }
        } catch (Exception e) {
          LOG.error("Not able to check name against bluepages", e);
          engineData.addNegativeCheckStatus("BLUEPAGES_NOT_VALIDATED", "Not able to check name against bluepages for scenario IBM Employee.");
          return false;
        }
      } else {
        LOG.warn("Not able to check name against bluepages, Customer Name 1 not found on the main address");
        engineData.addNegativeCheckStatus("BLUEPAGES_NOT_VALIDATED", "Customer Name 1 not found on the main address");
        return false;
      }
      break;

    }

    return true;
  }

  @Override
  public boolean performCountrySpecificCoverageCalculations(CalculateCoverageElement covElement, EntityManager entityManager,
      AutomationResult<OverrideOutput> results, StringBuilder details, OverrideOutput overrides, RequestData requestData,
      AutomationEngineData engineData, String covFrom, CoverageContainer container, boolean isCoverageCalculated) throws Exception {

    if (!"C".equals(requestData.getAdmin().getReqType())) {
      details.append(" Coverage Calculation skipped for Updates.");
      results.setResults("Skipped");
      results.setDetails(details.toString());
      return true;
    }

    Data data = requestData.getData();
    String gbgCntry = chkFrAffiliateCntry(engineData, requestData, entityManager);
    String cmrCntry = data.getCountryUse().length() == 3 ? "Belgium" : "Luxembourg";
    String bgId = data.getBgId();
    String commercialFin = "";
    String coverageId = container.getFinalCoverage();
    
    if (StringUtils.isNotBlank(gbgCntry) && cmrCntry.equalsIgnoreCase(gbgCntry)) {
      details.append("Coverage calculated for: " + gbgCntry).append("\n");
    } else if (StringUtils.isNotBlank(gbgCntry) && !cmrCntry.equalsIgnoreCase(gbgCntry)) {
      results.setOnError(true);
      details.append("Coverage calculated for: " + gbgCntry + " hence skipping overrides.").append("\n");
      overrides.clearOverrides(); // clear existing overrides
      return true;
    }

    if (StringUtils.isNotBlank(cmrCntry)) {
      String sortl = getSORTLfromCoverage(entityManager, container.getFinalCoverage(), cmrCntry);
      if (StringUtils.isNotBlank(sortl)) {
        details.append("SORTL calculated on basis of Existing CMR Data: " + sortl).append("\n");
        overrides.addOverride(AutomationElementRegistry.GBL_CALC_COV, "DATA", "SEARCH_TERM", data.getSearchTerm(), sortl);
        String sbo = "";
        if (data.getCountryUse().length() == 3) {
          sbo = "0" + sortl.substring(0, 2) + "0000";
        } else if (data.getCountryUse().length() > 3) {
          sbo = "0" + sortl.substring(0, 2) + "0001";
        }
        details.append("SBO calculated from Account Team: " + sbo);
        overrides.addOverride(AutomationElementRegistry.GBL_CALC_COV, "DATA", "SALES_BO_CD", data.getSalesBusOffCd(), sbo);
      }
      details.append("\n");
      if (isCoverageCalculated && StringUtils.isNotBlank(coverageId)) {
        if (covFrom != null && !"BGNONE".equals(bgId.trim())) {
          commercialFin = computeSBOForCovBelux(entityManager, QUERY_BG_SBO_BENELUX, bgId, data.getCmrIssuingCntry(), false);
        }
        if (commercialFin != null && !commercialFin.isEmpty()) {
          overrides.addOverride(AutomationElementRegistry.GBL_CALC_COV, "DATA", "COMMERCIAL_FINANCED", data.getSalesBusOffCd(), commercialFin);
          details.append("SORTL: " + commercialFin);
        }
        engineData.addPositiveCheckStatus(AutomationEngineData.COVERAGE_CALCULATED);
      }
    }

    if (!isCoverageCalculated) {
      // if not calculated using bg/gbg try calculation using 32/S logic
      details.setLength(0);// clear string builder
      overrides.clearOverrides(); // clear existing overrides

      BeLuxFieldsContainer fields = calculate32SValuesFromIMSBeLux(entityManager, data.getCmrIssuingCntry(), data.getCountryUse(),
          data.getSubIndustryCd(), data.getIsuCd(), data.getClientTier());

      String commercialFinanced = calculateSortlByRepTeamCd(entityManager, data.getCmrIssuingCntry(), data.getCountryUse(), data.getIsuCd(),
          data.getClientTier());
      if (StringUtils.isNotBlank(commercialFinanced)) {
        details.append("Coverage calculated successfully using Sortl logic.").append("\n");
        details.append("SORTL : " + data.getCommercialFinanced()).append("\n");
        overrides.addOverride(AutomationElementRegistry.GBL_CALC_COV, "DATA", "SORTL", data.getCommercialFinanced(), commercialFinanced);
        results.setResults("Calculated");
        results.setDetails(details.toString());
      } else if (StringUtils.isNotBlank(data.getCommercialFinanced())) {
        details.append("Coverage could not be calculated using Sortl logic. Using values from request").append("\n");
        details.append("SORTL : " + data.getCommercialFinanced()).append("\n");
        results.setResults("Calculated");
        results.setDetails(details.toString());
      } else {
        String msg = "Coverage cannot be calculated. No valid Sortl mapping found from request data.";
        details.append(msg);
        results.setResults("Cannot Calculate");
        results.setDetails(details.toString());
      }

      if (fields != null) {
        if (StringUtils.isNotBlank(fields.getSearchterm())) {
          details.append("Coverage calculated successfully using 32S logic.").append("\n");
          details.append("Account Team Number : " + fields.getSearchterm()).append("\n");
          overrides.addOverride(AutomationElementRegistry.GBL_CALC_COV, "DATA", "SEARCH_TERM", data.getSearchTerm(), fields.getSearchterm());
          String sbo = "";
          if (data.getCountryUse().equalsIgnoreCase("624")) {
            sbo = "0" + fields.getSearchterm().substring(0, 2) + "0000";
          } else if (data.getCountryUse().equalsIgnoreCase("624LU")) {
            sbo = "0" + fields.getSearchterm().substring(0, 2) + "0001";
          }
          details.append("SBO : " + sbo).append("\n");
          overrides.addOverride(AutomationElementRegistry.GBL_CALC_COV, "DATA", "SALES_BO_CD", data.getSalesBusOffCd(), sbo);

          details.append(" INAC/NAC Code : " + fields.getInacCd()).append("\n");
          overrides.addOverride(AutomationElementRegistry.GBL_CALC_COV, "DATA", "INAC_CD", data.getInacCd(), fields.getInacCd());
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
  
  private String computeSBOForCovBelux(EntityManager entityManager, String queryBgFR, String bgId, String cmrIssuingCntry, boolean b) {
    String sortl = "";
    String sql = ExternalizedQuery.getSql(queryBgFR);
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("KEY", bgId);
    query.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
    query.setParameter("COUNTRY", cmrIssuingCntry);
    String isoCntry = PageManager.getDefaultLandedCountry(cmrIssuingCntry);
    System.err.println("ISO: " + isoCntry);
    query.setParameter("ISO_CNTRY", isoCntry);
    query.setForReadOnly(true);

    LOG.debug("Calculating SORTL using Belgium query " + queryBgFR + " for key: " + bgId);
    List<Object[]> results = query.getResults(5);
    List<String> sortlList = new ArrayList<String>();
    if (results != null && !results.isEmpty()) {
      for (Object[] result : results) {
        sortl = (String) result[3];
        sortlList.add(sortl);
        // SpainFieldsContainer fieldValues = new SpainFieldsContainer();
      }
    }
    sortl = sortlList.get(0);
    return sortl;
  }
  
  private String chkFrAffiliateCntry(AutomationEngineData engineData, RequestData reqData, EntityManager entityManager) {
    GBGResponse gbg = (GBGResponse) engineData.get(AutomationEngineData.GBG_MATCH);
    String gbgCntry = "";
    if (gbg != null && gbg.isDomesticGBG()) {
      // check konsz

      Data data = reqData.getData();
      int count = 0;
      String affiliate = data.getCountryUse().length() > 3 ? "0502" : "0016";
      String sql = ExternalizedQuery.getSql("AUTO.GBG.COV.KNA1.KONSZ");
      String bgId = gbg.getBgId();
      PreparedQuery query = new PreparedQuery(entityManager, sql);
      query.setParameter("CNTRY", data.getCmrIssuingCntry());
      query.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
      query.setParameter("BG_ID", bgId);
      query.setParameter("AFFILIATE", affiliate);

      count = query.getSingleResult(Integer.class);
      if (count > 0 && data.getCountryUse().length() == 3) {
        gbgCntry = "Belgium";
      } else if (count > 0 && data.getCountryUse().length() > 3) {
        gbgCntry = "Luxembourg";
      }
    }
    return gbgCntry;
  }

  private BeLuxFieldsContainer calculate32SValuesFromIMSBeLux(EntityManager entityManager, String cmrIssuingctry, String countryUse,
      String subIndustryCd, String isuCd, String clientTier) {
    BeLuxFieldsContainer container = new BeLuxFieldsContainer();
    List<Object[]> searchTerms = new ArrayList<>();
    String isuCtc = (StringUtils.isNotBlank(isuCd) ? isuCd : "") + (StringUtils.isNotBlank(clientTier) ? clientTier : "");
    String geoCd = "";
    if (countryUse.length() > 3) {
      geoCd = countryUse.substring(3, 5);
    }

    String cmrCntry = cmrIssuingctry + geoCd;
    if (StringUtils.isNotBlank(subIndustryCd) && ("32S".equals(isuCtc))) {
      String ims = subIndustryCd.substring(0, 1);
      String sql = ExternalizedQuery.getSql("QUERY.GET.SRLIST.BYISUCTC");
      PreparedQuery query = new PreparedQuery(entityManager, sql);
      query.setParameter("ISU", "%" + isuCtc + "%");
      query.setParameter("ISSUING_CNTRY", cmrCntry);
      query.setParameter("CLIENT_TIER", "%" + ims + "%");
      query.setForReadOnly(true);
      searchTerms = query.getResults();
    } else {
      String sql = ExternalizedQuery.getSql("QUERY.GET.SRLIST.BYISU");
      PreparedQuery query = new PreparedQuery(entityManager, sql);
      query.setParameter("ISU", "%" + isuCtc + "%");
      query.setParameter("ISSUING_CNTRY", cmrCntry);
      searchTerms = query.getResults();
    }
    if (searchTerms != null) {
      String searchTerm = (String) searchTerms.get(0)[0];
      container.setSearchterm(searchTerm);
    }

    // inac
    List<Object[]> inacs = new ArrayList<>();

    if (StringUtils.isNotBlank(container.getSearchterm())) {
      String sql = ExternalizedQuery.getSql("QUERY.GET.INACLIST.BYST");
      PreparedQuery query = new PreparedQuery(entityManager, sql);
      query.setParameter("SEARCHTERM", "%" + container.getSearchterm() + "%");
      query.setParameter("ISSUING_CNTRY", SystemLocation.BELGIUM);
      query.setForReadOnly(true);
      inacs = query.getResults();
    }

    if (inacs != null && inacs.size() == 1) {
      String inacCd = (String) inacs.get(0)[0];
      container.setInacCd(inacCd);
    }

    return container;

  }

  private class BeLuxFieldsContainer {
    private String searchTerm;
    private String inacCd;

    public String getSearchterm() {
      return searchTerm;
    }

    public void setSearchterm(String searchTerm) {
      this.searchTerm = searchTerm;
    }

    public String getInacCd() {
      return inacCd;
    }

    public void setInacCd(String inacCd) {
      this.inacCd = inacCd;
    }

  }

  @Override
  public AutomationResult<OverrideOutput> doCountryFieldComputations(EntityManager entityManager, AutomationResult<OverrideOutput> results,
      StringBuilder details, OverrideOutput overrides, RequestData requestData, AutomationEngineData engineData) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean runUpdateChecksForData(EntityManager entityManager, AutomationEngineData engineData, RequestData requestData,
      RequestChangeContainer changes, AutomationResult<ValidationOutput> output, ValidationOutput validation) throws Exception {
    Admin admin = requestData.getAdmin();
    Data data = requestData.getData();
    StringBuilder details = new StringBuilder();
    boolean cmdeReview = false;
    Set<String> resultCodes = new HashSet<String>();
    List<String> ignoredUpdates = new ArrayList<String>();
    for (UpdatedDataModel change : changes.getDataUpdates()) {
      switch (change.getDataField()) {
      case "VAT #":
        if (StringUtils.isBlank(change.getOldData()) && !StringUtils.isBlank(change.getNewData())) {
          // ADD
          Addr soldTo = requestData.getAddress(CmrConstants.RDC_SOLD_TO);
          List<DnBMatchingResponse> matches = getMatches(requestData, engineData, soldTo, true);
          boolean matchesDnb = false;
          if (matches != null) {
            // check against D&B
            matchesDnb = ifaddressCloselyMatchesDnb(matches, soldTo, admin, data.getCmrIssuingCntry());
          }
          if (!matchesDnb) {
            cmdeReview = true;
            engineData.addNegativeCheckStatus("_beluxVATCheckFailed", "VAT # on the request did not match D&B");
            details.append("VAT # on the request did not match D&B\n");
          } else {
            details.append("VAT # on the request matches D&B\n");
          }
        }

        if (!StringUtils.isBlank(change.getOldData()) && !StringUtils.isBlank(change.getNewData())
            && !(change.getOldData().equals(change.getNewData()))) {
          // UPDATE
          Addr soldTo = requestData.getAddress(CmrConstants.RDC_SOLD_TO);
          List<DnBMatchingResponse> matches = getMatches(requestData, engineData, soldTo, true);
          boolean matchesDnb = false;
          if (matches != null) {
            // check against D&B
            matchesDnb = ifaddressCloselyMatchesDnb(matches, soldTo, admin, data.getCmrIssuingCntry());
          }
          if (!matchesDnb) {
            cmdeReview = true;
            engineData.addNegativeCheckStatus("_beluxVATCheckFailed", "VAT # on the request did not match D&B");
            details.append("VAT # on the request did not match D&B\n");
          } else {
            details.append("VAT # on the request matches D&B\n");
          }
        }

        break;
      case "ISIC":
      case "INAC/NAC Code":
        cmdeReview = true;
        break;
      case "Economic Code":
        String newEcoCd = change.getNewData();
        String oldEcoCd = change.getOldData();
        List<String> ls = Arrays.asList("A", "F", "C", "R");
        if (StringUtils.isNotBlank(newEcoCd) && StringUtils.isNotBlank(oldEcoCd) && newEcoCd.length() == 3 && oldEcoCd.length() == 3) {
          if (!newEcoCd.substring(0, 1).equals(oldEcoCd.substring(0, 1))
              && newEcoCd.substring(1, newEcoCd.length()).equals(oldEcoCd.substring(1, oldEcoCd.length()))) {
            if (ls.contains(newEcoCd.substring(0, 1)) && oldEcoCd.substring(0, 1).equals("K")
                || ls.contains(oldEcoCd.substring(0, 1)) && newEcoCd.substring(0, 1).equals("K")) {
              admin.setScenarioVerifiedIndc("Y");
              details.append("Economic Code has been updated by registered user to " + newEcoCd + "\n");
            } else {
              cmdeReview = true;
            }
          } else if (newEcoCd.substring(0, 1).equals(oldEcoCd.substring(0, 1))
              && (newEcoCd.substring(1, 3).equals("11") || newEcoCd.substring(1, 3).equals("13") || newEcoCd.substring(1, 3).equals("49"))) {
            cmdeReview = true;
            engineData.addNegativeCheckStatus("_beluxEconomicCdUpdt", "Economic code is updated from " + oldEcoCd + " to " + newEcoCd);
            details.append("Economic code is updated to " + newEcoCd + "\n");
          } else {
            cmdeReview = true;
            engineData.addNegativeCheckStatus("_beluxEconomicCdUpdt", "Economic code was updated incorrectly or by non registered user.");
            details.append("Economic code was updated incorrectly or by non registered user.\n");
          }
        }
        break;
      case "PPS CEID":
        String newppsceid = change.getNewData();
        String oldppsceid = change.getOldData();
        String Kukla = data.getCustClass();
        // ADD
        if (StringUtils.isBlank(oldppsceid) && !StringUtils.isBlank(newppsceid)) {
          if ("49".equalsIgnoreCase(Kukla) && checkPPSCEID(data.getPpsceid())) {
            details.append("PPS CE ID validated successfully with PartnerWorld Profile Systems.").append("\n");
          } else {
            resultCodes.add("D");
            if (!"49".equalsIgnoreCase(Kukla)) {
              details.append("PS Ceid added for CMR with Kukla other than 49").append("\n");
            } else {
              details.append("PPS ceid on the request is invalid").append("\n");
            }
          }
        }
        // DELETE
        if (!StringUtils.isBlank(oldppsceid) && StringUtils.isBlank(newppsceid)) {
          cmdeReview = true;
          engineData.addNegativeCheckStatus("_beluxPpsCeidUpdt", " Deletion of ppsceid needs cmde review.\n");
          details.append(" Deletion of ppsceid needs cmde review.\n");
        }
        // UPDATE
        if (!StringUtils.isBlank(oldppsceid) && !StringUtils.isBlank(newppsceid) && !oldppsceid.equalsIgnoreCase(newppsceid)) {
          cmdeReview = true;
          engineData.addNegativeCheckStatus("_beluxPpsCeidUpdt", " Update of ppsceid needs cmde review.\n");
          details.append(" Update of ppsceid needs cmde review.\n");
        }
        break;
      case "Order Block Code":
        String newOrdBlk = change.getNewData();
        String oldOrdBlk = change.getOldData();
        // ADD
        if (StringUtils.isBlank(oldOrdBlk) && !StringUtils.isBlank(newOrdBlk) && "P".equalsIgnoreCase(newOrdBlk)) {
          cmdeReview = true;
          engineData.addNegativeCheckStatus("_beluxOrdBlkUpdt", "Embargo Code P was added.");
          details.append("Embargo Code P was added.\n");
        }

        // DELETE
        if (!StringUtils.isBlank(oldOrdBlk) && StringUtils.isBlank(newOrdBlk) && "P".equalsIgnoreCase(oldOrdBlk)) {
          cmdeReview = true;
          engineData.addNegativeCheckStatus("_beluxOrdBlkUpdt", "Embargo Code P was deleted.");
          details.append("Embargo Code P was deleted.\n");
        }
        break;
      case "Preferred Language":
        details.append("Preferred Language data field was updated.\n");
        break;
      default:
        ignoredUpdates.add(change.getDataField());
        break;
      }
    }
    if (resultCodes.contains("D")) {
      output.setOnError(true);
      validation.setSuccess(false);
      validation.setMessage("Rejected");
    } else if (cmdeReview) {
      engineData.addNegativeCheckStatus("_esDataCheckFailed", "Updates to one or more fields requires CMDE review.");
      details.append("Updates to one or more fields requires CMDE review.\n");
      validation.setSuccess(false);
      validation.setMessage("Not Validated");
    } else {
      validation.setSuccess(true);
      validation.setMessage("Successful");
    }
    if (!ignoredUpdates.isEmpty()) {
      details.append("Updates to the following fields skipped validation:\n");
      for (String field : ignoredUpdates) {
        details.append(" - " + field + "\n");
      }
    }
    output.setDetails(details.toString());
    output.setProcessOutput(validation);
    return true;
  }

  @Override
  public boolean runUpdateChecksForAddress(EntityManager entityManager, AutomationEngineData engineData, RequestData requestData,
      RequestChangeContainer changes, AutomationResult<ValidationOutput> output, ValidationOutput validation) throws Exception {
    Admin admin = requestData.getAdmin();
    Data data = requestData.getData();
    StringBuilder duplicateDetails = new StringBuilder();
    StringBuilder nonRelAddrFdsDetails = new StringBuilder();
    List<Addr> addresses = null;
    LOG.debug("Verifying PayGo Accreditation for " + admin.getSourceSystId());
    boolean payGoAddredited = RequestUtils.isPayGoAccredited(entityManager, admin.getSourceSystId());
    boolean isOnlyPayGoUpdated = changes != null && changes.isAddressChanged("PG01") && !changes.isAddressChanged("ZS01")
        && !changes.isAddressChanged("ZI01");
    int ignoredAddr = 0;
    StringBuilder checkDetails = new StringBuilder();
    Set<String> resultCodes = new HashSet<String>();// R - review
    for (String addrType : RELEVANT_ADDRESSES) {
      addresses = requestData.getAddresses(addrType);
      if (changes.isAddressChanged(addrType)) {
        for (Addr addr : addresses) {
          List<String> addrTypesChanged = new ArrayList<String>();
          for (UpdatedNameAddrModel addrModel : changes.getAddressUpdates()) {
            if (!addrTypesChanged.contains(addrModel.getAddrTypeCode())) {
              addrTypesChanged.add(addrModel.getAddrTypeCode());
            }
          }
          boolean isZS01WithAufsdPG = (CmrConstants.RDC_SOLD_TO.equals(addrType) && "PG".equals(data.getOrdBlk()));

          if (isRelevantAddressFieldUpdated(changes, addr, nonRelAddrFdsDetails)) {

            if ((addrType.equalsIgnoreCase(CmrConstants.RDC_SOLD_TO) && "Y".equals(addr.getImportInd()))
                || addrType.equalsIgnoreCase(CmrConstants.RDC_BILL_TO) || addrType.equalsIgnoreCase(CmrConstants.RDC_SECONDARY_SOLD_TO)) {

              List<DnBMatchingResponse> matches = getMatches(requestData, engineData, addr, false);
              boolean matchesDnb = false;
              if (matches != null) {
                // check against D&B
                matchesDnb = ifaddressCloselyMatchesDnb(matches, addr, admin, data.getCmrIssuingCntry());
              }
              if ("U".equals(admin.getReqType()) && payGoAddredited) {
                LOG.debug("No D&B record was found using advanced matching. Skipping checks for PayGo Addredited Customers.");
                checkDetails.append("No D&B record was found using advanced matching. Skipping checks for PayGo Addredited Customers.");
            //    admin.setPaygoProcessIndc("Y");
              } else if (!matchesDnb) {
                LOG.debug("Address " + addrType + "(" + addr.getId().getAddrSeq() + ") does not match D&B");
                resultCodes.add("X");
                checkDetails.append("Address " + addrType + "(" + addr.getId().getAddrSeq() + ") did not match D&B records.\n");
              } else {
                checkDetails.append("Address " + addrType + "(" + addr.getId().getAddrSeq() + ") matches D&B records. Matches:\n");
                for (DnBMatchingResponse dnb : matches) {
                  checkDetails.append(" - DUNS No.:  " + dnb.getDunsNo() + " \n");
                  checkDetails.append(" - Name.:  " + dnb.getDnbName() + " \n");
                  checkDetails.append(" - Address:  " + dnb.getDnbStreetLine1() + " " + dnb.getDnbCity() + " " + dnb.getDnbPostalCode() + " "
                      + dnb.getDnbCountry() + "\n\n");
                }
              }
            }

            String soldToCustNm1 = requestData.getAddress("ZS01").getCustNm1();
            String installAtCustNm = requestData.getAddress("ZI01") != null ? requestData.getAddress("ZI01").getCustNm1() : "";
            if ((addrType.equalsIgnoreCase(CmrConstants.RDC_INSTALL_AT) && soldToCustNm1.equalsIgnoreCase(installAtCustNm))
                || ((addrType.equalsIgnoreCase(CmrConstants.RDC_SHIP_TO) || addrType.equalsIgnoreCase(CmrConstants.RDC_PAYGO_BILLING))
                    && "N".equals(addr.getImportInd()))) {
              LOG.debug("Checking duplicates for " + addrType + "(" + addr.getId().getAddrSeq() + ")");
              boolean duplicate = addressExists(entityManager, addr, requestData);
              if (duplicate) {
                LOG.debug(" - Duplicates found for " + addrType + "(" + addr.getId().getAddrSeq() + ")");
                duplicateDetails.append("Address " + addrType + "(" + addr.getId().getAddrSeq() + ") provided matches an existing address.\n");
                resultCodes.add("R");
              } else {
                LOG.debug("Addition/Updation of " + addrType + "(" + addr.getId().getAddrSeq() + ")");
                checkDetails.append("Address (" + addr.getId().getAddrSeq() + ") is validated.\n");
              }
            } else if ((payGoAddredited && addrTypesChanged.contains(CmrConstants.RDC_PAYGO_BILLING.toString())) || isZS01WithAufsdPG) {
              if ("N".equals(addr.getImportInd())) {
                LOG.debug("Checking duplicates for " + addrType + "(" + addr.getId().getAddrSeq() + ")");
                boolean duplicate = addressExists(entityManager, addr, requestData);
                if (duplicate) {
                  LOG.debug(" - Duplicates found for " + addrType + "(" + addr.getId().getAddrSeq() + ")");
                  checkDetails.append("Address " + addrType + "(" + addr.getId().getAddrSeq() + ") provided matches an existing Bill-To address.\n");
                  resultCodes.add("D");
                } else {
                  LOG.debug(" - NO duplicates found for " + addrType + "(" + addr.getId().getAddrSeq() + ")");
                  checkDetails.append(" - NO duplicates found for " + addrType + "(" + addr.getId().getAddrSeq() + ")" + "with same attentionTo");
                  checkDetails.append("Updates to address fields for" + addrType + "(" + addr.getId().getAddrSeq() + ")  validated in the checks.\n");
                }
              } else {
                checkDetails.append("Updates to address fields for" + addrType + "(" + addr.getId().getAddrSeq() + ")  validated in the checks.\n");
              }
              if (addrType.equalsIgnoreCase(CmrConstants.RDC_SHIP_TO) && "Y".equals(addr.getImportInd())) {
                ignoredAddr++;
              }

            }
          }
        }
      }
      if (resultCodes.contains("X")) {
        validation.setSuccess(false);
        validation.setMessage("Review Required.");
        engineData.addNegativeCheckStatus("_esCheckFailed", "Updated elements cannot be checked automatically.");
      } else if (resultCodes.contains("R")) {
        output.setOnError(true);
        engineData.addRejectionComment("_atRejectAddr", "Addition or updation on the address is rejected", "", "");
        validation.setSuccess(false);
        validation.setMessage("Rejected.");
      } else {
        validation.setSuccess(true);
        validation.setMessage("Successful");
      }
    }
    if (ignoredAddr > 0) {
      checkDetails.append("Updates to imported Address Ship-To(ZD01) ignored. ");
    }
    String details = (output.getDetails() != null && output.getDetails().length() > 0) ? output.getDetails() : "";
    details += duplicateDetails.length() > 0 ? duplicateDetails.toString() : "";
    details += checkDetails.length() > 0 ? "\n" + checkDetails.toString() : "";
    details += nonRelAddrFdsDetails.length() > 0 ? "Following updates ignored - \n" + nonRelAddrFdsDetails.toString() : "";
    output.setDetails(details);
    output.setProcessOutput(validation);
    return true;
  }

  private boolean isRelevantAddressFieldUpdated(RequestChangeContainer changes, Addr addr, StringBuilder details) {
    List<UpdatedNameAddrModel> addrChanges = changes.getAddressChanges(addr.getId().getAddrType(), addr.getId().getAddrSeq());
    if (addrChanges == null) {
      return false;
    }
    for (UpdatedNameAddrModel change : addrChanges) {
      if (!NON_RELEVANT_ADDRESS_FIELDS.contains(change.getDataField())) {
        return true;
      } else {
        details.append(change.getDataField() + " of address " + addr.getId().getAddrType() + " \n");
      }
    }
    return false;
  }

  /**
   * Computes the SBO by getting the SORTL most used by the COverage ID
   * 
   * @param entityManager
   * @param coverage
   * @return
   */
  private String getSORTLfromCoverage(EntityManager entityManager, String coverage, String gbgCntry) {
    List<String> sortlList = null;
    try {
      LOG.debug("Computing SORTL for Coverage " + coverage);
      String gbgCntryCd = gbgCntry.equalsIgnoreCase("Belgium") ? "BE" : "LU";
      String konzs = "BE".equalsIgnoreCase(gbgCntryCd) ? "0016" : "0502";
      String sql = ExternalizedQuery.getSql("AUTO.BELUX.COV.SORTL");
      PreparedQuery query = new PreparedQuery(entityManager, sql);
      query.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
      query.setParameter("COVID", coverage);
      query.setParameter("LAND1", gbgCntryCd);
      query.setParameter("KONZS", konzs);
      query.setForReadOnly(true);
      sortlList = query.getResults(20, String.class);
    } catch (Exception e) {
      LOG.debug("Error while computing SORTL for Coverage " + coverage + " from RDc query.");
    }
    if (sortlList != null) {
      for (String sortl : sortlList) {
        if (StringUtils.isNotBlank(sortl) && sortl.length() == 6) {
          return sortl;
        }
      }
    }
    return null;
  }

  @Override
  public List<String> getSkipChecksRequestTypesforCMDE() {
    return Arrays.asList("C", "U", "M", "D", "R");
  }

  @Override
  public void performCoverageBasedOnGBG(CalculateCoverageElement covElement, EntityManager entityManager, AutomationResult<OverrideOutput> results,
      StringBuilder details, OverrideOutput overrides, RequestData requestData, AutomationEngineData engineData, String covFrom,
      CoverageContainer container, boolean isCoverageCalculated) throws Exception {
    Data data = requestData.getData();
    String bgId = data.getBgId();
    String gbgId = data.getGbgId();
    String country = data.getCmrIssuingCntry();
    String sql = ExternalizedQuery.getSql("QUERY.GET_GBG_FROM_LOV");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("CD", gbgId);
    query.setParameter("COUNTRY", country);
    query.setForReadOnly(true);
    String result = query.getSingleResult(String.class);
    LOG.debug("perform coverage based on GBG-------------");
    LOG.debug("result--------" + result);
    if (result != null || bgId.equals("DB502GQG")) {
      LOG.debug("Setting isu ctc to 5K based on gbg matching.");
      details.append("Setting isu ctc to 5K based on gbg matching.");
      overrides.addOverride(covElement.getProcessCode(), "DATA", "ISU_CD", data.getIsuCd(), "5K");
      overrides.addOverride(covElement.getProcessCode(), "DATA", "CLIENT_TIER", data.getClientTier(), "");
    }
    LOG.debug("isu" + data.getIsuCd());
    LOG.debug("client tier" + data.getClientTier());
  }

  public String calculateSortlByRepTeamCd(EntityManager entityManager, String cmrIssuingctry, String countryUse, String isuCd, String clientTier) {
    List<Object[]> commercialFinancedResults = new ArrayList<>();
    String commercialFinanced = "";
    String salesRepTeam = "";
    String isuCtc = (StringUtils.isNotBlank(isuCd) ? isuCd : "") + (StringUtils.isNotBlank(clientTier) ? clientTier : "");
    String geoCd = "";
    if (StringUtils.isNotBlank(countryUse) && countryUse.length() > 3) {
      geoCd = countryUse.substring(3, 5);
    }
    String cmrCntry = cmrIssuingctry + geoCd;
    if (StringUtils.isNotBlank(countryUse) && countryUse.length() == 3) {
      salesRepTeam = isuCtc + "BG";
    } else {
      salesRepTeam = isuCtc + "LU";
    }
    String sql = ExternalizedQuery.getSql("QUERY.GET.DSCLIST.BYSR");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("ISSUING_CNTRY", cmrCntry);
    query.setParameter("ISU", "%" + isuCtc + "%");
    query.setParameter("REP_TEAM_CD", salesRepTeam);
    query.setForReadOnly(true);
    commercialFinancedResults = query.getResults();
    if (commercialFinancedResults != null && commercialFinancedResults.size() == 1) {
      commercialFinanced = (String) commercialFinancedResults.get(0)[0];
      return commercialFinanced;
    } else {
      return null;
    }
  }

  @Override
  public void filterDuplicateCMRMatches(EntityManager entityManager, RequestData requestData, AutomationEngineData engineData,
      MatchingResponse<DuplicateCMRCheckResponse> response) {

    String[] scenariosToBeChecked = { "BEPRI", "IBMEM", "LUIBM", "LUPRI" };
    String scenario = requestData.getData().getCustSubGrp();
    String[] kuklaPriv = { "60" };
    String[] kuklaIBMEM = { "71" };

    if (Arrays.asList(scenariosToBeChecked).contains(scenario)) {
      List<DuplicateCMRCheckResponse> matches = response.getMatches();
      List<DuplicateCMRCheckResponse> filteredMatches = new ArrayList<DuplicateCMRCheckResponse>();
      for (DuplicateCMRCheckResponse match : matches) {
        if (match.getCmrNo() != null && match.getCmrNo().startsWith("P") && "75".equals(match.getOrderBlk())) {
          filteredMatches.add(match);
        }
        if (StringUtils.isNotBlank(match.getCustClass())) {
          String kukla = match.getCustClass() != null ? match.getCustClass() : "";
          if (Arrays.asList(kuklaPriv).contains(kukla) && ("BEPRI".equals(scenario) || "LUPRI".equals(scenario))) {
            filteredMatches.add(match);
          } else if (Arrays.asList(kuklaIBMEM).contains(kukla) && ("IBMEM".equals(scenario) || "LUIBM".equals(scenario))) {
            filteredMatches.add(match);
          }
        }

      }
      // set filtered matches in response
      response.setMatches(filteredMatches);
    }
  }
}
