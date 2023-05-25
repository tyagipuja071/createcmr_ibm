package com.ibm.cio.cmr.request.automation.util.geo;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;

import org.apache.commons.digester.Digester;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.automation.AutomationElementRegistry;
import com.ibm.cio.cmr.request.automation.AutomationEngineData;
import com.ibm.cio.cmr.request.automation.RequestData;
import com.ibm.cio.cmr.request.automation.impl.gbl.CalculateCoverageElement;
import com.ibm.cio.cmr.request.automation.out.AutomationResult;
import com.ibm.cio.cmr.request.automation.out.FieldResultKey;
import com.ibm.cio.cmr.request.automation.out.OverrideOutput;
import com.ibm.cio.cmr.request.automation.out.ValidationOutput;
import com.ibm.cio.cmr.request.automation.util.AutomationConst;
import com.ibm.cio.cmr.request.automation.util.AutomationUtil;
import com.ibm.cio.cmr.request.automation.util.CoverageContainer;
import com.ibm.cio.cmr.request.automation.util.RequestChangeContainer;
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
import com.ibm.cio.cmr.request.util.ConfigUtil;
import com.ibm.cio.cmr.request.util.JpaManager;
import com.ibm.cio.cmr.request.util.Person;
import com.ibm.cio.cmr.request.util.RequestUtils;
import com.ibm.cio.cmr.request.util.SystemLocation;
import com.ibm.cio.cmr.request.util.dnb.DnBUtil;
import com.ibm.cmr.services.client.CmrServicesFactory;
import com.ibm.cmr.services.client.MatchingServiceClient;
import com.ibm.cmr.services.client.dnb.DnBCompany;
import com.ibm.cmr.services.client.matching.MatchingResponse;
import com.ibm.cmr.services.client.matching.cmr.DuplicateCMRCheckRequest;
import com.ibm.cmr.services.client.matching.cmr.DuplicateCMRCheckResponse;
import com.ibm.cmr.services.client.matching.dnb.DnBMatchingResponse;
import com.ibm.cmr.services.client.matching.gbg.GBGFinderRequest;

public class FranceUtil extends AutomationUtil {

  public static final String SCENARIO_INTERNAL_SO = "INTSO";
  public static final String SCENARIO_THIRD_PARTY = "THDPT";
  public static final String SCENARIO_HOSTING = "HOSTC";
  public static final String SCENARIO_BUSINESS_PARTNER = "BUSPR";
  public static final String SCENARIO_PRIVATE_PERSON = "PRICU";
  public static final String SCENARIO_IBM_EMPLOYEE = "IBMEM";
  public static final String SCENARIO_INTERNAL = "INTER";
  public static final String SCENARIO_COMMERCIAL = "COMME";
  public static final String SCENARIO_CROSSBORDER_INTERNAL_SO = "CBTSO";
  public static final String SCENARIO_CROSSBORDER_THIRD_PARTY = "CBDPT";
  public static final String SCENARIO_CROSSBORDER_HOSTING = "CBSTC";
  public static final String SCENARIO_CROSSBORDER_BUSINESS_PARTNER = "XBUSP";
  public static final String SCENARIO_CROSSBORDER_PRIVATE_PERSON = "XBLUM";
  public static final String SCENARIO_CROSSBORDER_IBM_EMPLOYEE = "CBIEM";
  public static final String SCENARIO_CROSSBORDER_INTERNAL = "CBTER";
  public static final String SCENARIO_CROSSBORDER_COMMERCIAL = "CBMME";
  private static final List<String> RELEVANT_ADDRESSES = Arrays.asList(CmrConstants.RDC_SOLD_TO, CmrConstants.RDC_BILL_TO,
      CmrConstants.RDC_INSTALL_AT, CmrConstants.RDC_SHIP_TO, CmrConstants.RDC_PAYGO_BILLING);
  private static final Logger LOG = Logger.getLogger(FranceUtil.class);
  private static List<FrSboMapping> sortlMappings = new ArrayList<FrSboMapping>();
  private static final String MATCHING = "matching";
  private static final String QUERY_BG_SBO_FR = "AUTO.COV.GET_SBO_FROM_BG_FR";
  private static final String ISIC_CD = "IsicCd";
  private static final String POSTAL_CD_STARTS = "postalCdStarts";
  private static final String CITY = "city";
  private static final String SBO = "sbo";
  private static final List<String> NON_RELEVANT_ADDRESS_FIELDS = Arrays.asList("Att. Person", "Phone #", "Division/Department",
      "Attention to/Building/Floor/Office");
  public static final List<String> FRANCE_SUBREGIONS = Arrays.asList("MC", "GP", "GF", "MQ", "RE", "PM", "KM", "VU", "PF", "YT", "NC", "WF", "AD",
      "DZ");

  public static final List<String> COVERAGE_34Q = Arrays.asList("TF", "RE", "MQ", "GP", "GF", "PM", "YT", "NC", "VU", "WF", "PF", "AD", "MC", "DZ");

  @SuppressWarnings("unchecked")
  public FranceUtil() {
    if (FranceUtil.sortlMappings.isEmpty()) {
      Digester digester = new Digester();
      digester.setValidating(false);
      digester.addObjectCreate("mappings", ArrayList.class);

      digester.addObjectCreate("mappings/mapping", FrSboMapping.class);

      digester.addBeanPropertySetter("mappings/mapping/countryUse", "countryUse");
      digester.addBeanPropertySetter("mappings/mapping/postalCdStarts", "postalCdStarts");
      digester.addBeanPropertySetter("mappings/mapping/isu", "isu");
      digester.addBeanPropertySetter("mappings/mapping/ctc", "ctc");
      digester.addBeanPropertySetter("mappings/mapping/sbo", "sbo");
      digester.addBeanPropertySetter("mappings/mapping/countryLanded", "countryLanded");
      digester.addSetNext("mappings/mapping", "add");
      try {
        InputStream is = ConfigUtil.getResourceStream("fr-sbo-mapping.xml");
        FranceUtil.sortlMappings = (ArrayList<FrSboMapping>) digester.parse(is);
      } catch (Exception e) {
        LOG.error("Error occured while digesting xml.", e);
      }
    }
  }

  @Override
  public AutomationResult<OverrideOutput> doCountryFieldComputations(EntityManager entityManager, AutomationResult<OverrideOutput> results,
      StringBuilder details, OverrideOutput overrides, RequestData requestData, AutomationEngineData engineData) throws Exception {
    Admin admin = requestData.getAdmin();
    // Addr addr = requestData.getAddress();
    Data data = requestData.getData();
    String scenario = data.getCustSubGrp();
    if (!"C".equals(admin.getReqType())) {
      details.append("Field Computation skipped for Updates.");
      results.setResults("Skipped");
      results.setDetails(details.toString());
      return results;
    }
    if (SCENARIO_INTERNAL_SO.equals(scenario) || SCENARIO_THIRD_PARTY.equals(scenario) || SCENARIO_CROSSBORDER_INTERNAL_SO.equals(scenario)
        || SCENARIO_CROSSBORDER_THIRD_PARTY.equals(scenario)) {
      Addr zi01 = requestData.getAddress("ZI01");
      boolean highQualityMatchExists = false;
      List<DnBMatchingResponse> response = getMatches(requestData, engineData, zi01, false);
      if (response != null && response.size() > 0) {
        for (DnBMatchingResponse dnbRecord : response) {
          boolean closelyMatches = DnBUtil.closelyMatchesDnb(data.getCmrIssuingCntry(), zi01, admin, dnbRecord);
          if (closelyMatches) {
            engineData.put("ZI01_DNB_MATCH", dnbRecord);
            highQualityMatchExists = true;
            details.append("High Quality DnB Match found for Installing address.\n");
            details.append(" - Confidence Code:  " + dnbRecord.getConfidenceCode() + " \n");
            details.append(" - DUNS No.:  " + dnbRecord.getDunsNo() + " \n");
            details.append(" - Name:  " + dnbRecord.getDnbName() + " \n");
            details.append(" - Address:  " + dnbRecord.getDnbStreetLine1() + " " + dnbRecord.getDnbCity() + " " + dnbRecord.getDnbPostalCode() + " "
                + dnbRecord.getDnbCountry() + "\n\n");
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
      if (!highQualityMatchExists && "C".equals(admin.getReqType())) {
        LOG.debug("No High Quality DnB Match found for Installing address.");
        details.append("No High Quality DnB Match found for Installing address. Request will require CMDE review before proceeding.").append("\n");
        engineData.addNegativeCheckStatus("NOMATCHFOUND",
            "No High Quality DnB Match found for Installing address. Request cannot be processed automatically.");
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
  public GBGFinderRequest createRequest(Admin admin, Data data, Addr addr, Boolean isOrgIdMatchOnly) {
    GBGFinderRequest request = super.createRequest(admin, data, addr, isOrgIdMatchOnly);
    String scenarioType = data.getCustSubGrp();
    if (("C".equals(admin.getReqType())) && (SCENARIO_INTERNAL_SO.equals(scenarioType) || SCENARIO_THIRD_PARTY.equals(scenarioType)
        || SCENARIO_CROSSBORDER_INTERNAL_SO.equals(scenarioType) || SCENARIO_CROSSBORDER_THIRD_PARTY.equals(scenarioType))) {
      request.setOrgId("");
    }
    return request;
  }

  /**
   * Computes the SBO by getting the SORTL most used by the COverage ID
   * 
   * @param entityManager
   * @param coverage
   * @return
   */
  private String getSBOfromCoverage(EntityManager entityManager, String coverage) {
    LOG.debug("Computing SBO for Coverage " + coverage);
    String sql = ExternalizedQuery.getSql("AUTO.FR.COV.SORTL");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
    query.setParameter("COVID", coverage);
    query.setForReadOnly(true);
    String sortl = query.getSingleResult(String.class);
    if (sortl != null && !StringUtils.isBlank(sortl)) {
      sortl = StringUtils.rightPad(sortl, 6, '0');
      return sortl.substring(0, 3);
    }
    return null;
  }

  @Override
  public boolean performScenarioValidation(EntityManager entityManager, RequestData requestData, AutomationEngineData engineData,
      AutomationResult<ValidationOutput> result, StringBuilder details, ValidationOutput output) {
    Data data = requestData.getData();
    Addr zs01 = requestData.getAddress("ZS01");
    String customerName = getCustomerFullName(zs01);
    Addr zi01 = requestData.getAddress("ZI01");

    String scenario = data.getCustSubGrp();
    if (StringUtils.isNotBlank(scenario)) {
      String scenarioDesc = getScenarioDescription(entityManager, data);
      if (StringUtils.isNotBlank(data.getCountryUse()) && data.getCountryUse().length() > 3 && !SCENARIO_COMMERCIAL.equals(scenario)
          && !SCENARIO_CROSSBORDER_COMMERCIAL.equals(scenario)) {
        engineData.addNegativeCheckStatus("DISABLEDAUTOPROC",
            "Requests for " + scenarioDesc + " cannot be processed automatically. Manual processing would be required.");
      }
      if ((SCENARIO_HOSTING.equals(scenario) || SCENARIO_CROSSBORDER_HOSTING.equals(scenario) || SCENARIO_THIRD_PARTY.equals(scenario)
          || SCENARIO_CROSSBORDER_THIRD_PARTY.equals(scenario)) && (zi01 != null) && compareCustomerNames(zs01, zi01)) {
        details.append("Customer Names on Sold-to and Install-at address should be different for Third Party and Hosting Scenario").append("\n");
        engineData.addRejectionComment("OTH",
            "Customer Names on Sold-to and Install-at address should be different for Third Party and Hosting Scenario", "", "");
        return false;
      } else if ((SCENARIO_COMMERCIAL.equals(scenario) || SCENARIO_CROSSBORDER_COMMERCIAL.equals(scenario)) && (zi01 != null)
          && !compareCustomerNames(zs01, zi01)) {
        details.append("Sold-to and Installing name are not identical. Request will require CMDE review before proceeding.").append("\n");
        engineData.addNegativeCheckStatus("SOLDTO_INSTALL_DIFF", "Sold-to and Installing addresses are not identical.");
      }
      if (zs01 != null) {
        // remove duplicate address
        removeDuplicateAddresses(entityManager, requestData, details);
      }
      switch (scenario) {
      case SCENARIO_CROSSBORDER_PRIVATE_PERSON:
      case SCENARIO_PRIVATE_PERSON:
        engineData.addPositiveCheckStatus(AutomationEngineData.SKIP_GBG);
        return doPrivatePersonChecks(engineData, SystemLocation.FRANCE, zs01.getLandCntry(), customerName, details, false, requestData);
      case SCENARIO_CROSSBORDER_IBM_EMPLOYEE:
      case SCENARIO_IBM_EMPLOYEE:
        engineData.addPositiveCheckStatus(AutomationEngineData.SKIP_GBG);
        return doPrivatePersonChecks(engineData, SystemLocation.FRANCE, zs01.getLandCntry(), customerName, details, true, requestData);

      case SCENARIO_INTERNAL:
      case SCENARIO_CROSSBORDER_INTERNAL:
        engineData.addPositiveCheckStatus(AutomationEngineData.SKIP_GBG);
        engineData.addPositiveCheckStatus(AutomationEngineData.SKIP_COVERAGE);
        for (String addrType : RELEVANT_ADDRESSES) {
          List<Addr> addresses = requestData.getAddresses(addrType);
          for (Addr addr : addresses) {
            String custNmTrimmed = getCustomerFullName(addr);
            if (!(custNmTrimmed.toUpperCase().contains("IBM") || custNmTrimmed.toUpperCase().contains("INTERNATIONAL BUSINESS MACHINES"))) {
              details.append("Wrong Customer Name on the main address. IBM should be part of the name.").append("\n");
              engineData.addRejectionComment("OTH", "Wrong Customer Name on the main address. IBM should be part of the name.", "", "");
              return false;
            }
          }
        }
        break;
      case SCENARIO_BUSINESS_PARTNER:
      case SCENARIO_CROSSBORDER_BUSINESS_PARTNER:
        engineData.addPositiveCheckStatus(AutomationEngineData.SKIP_GBG);
        engineData.addPositiveCheckStatus(AutomationEngineData.SKIP_COVERAGE);
        return doBusinessPartnerChecks(engineData, data.getPpsceid(), details);
      case SCENARIO_INTERNAL_SO:
      case SCENARIO_CROSSBORDER_INTERNAL_SO:
        if (zi01 == null) {
          details.append("Install-at address should be present for Interna SO Scenario.").append("\n");
          engineData.addRejectionComment("OTH", "Install-at address should be present for Internal SO Scenario.", "", "");
          return false;
        }

        List<Addr> addresses = requestData.getAddresses(CmrConstants.RDC_BILL_TO);
        for (Addr addr : addresses) {
          String custNmTrimmed = getCustomerFullName(addr);
          if (!(custNmTrimmed.toUpperCase().contains("IBM") || custNmTrimmed.toUpperCase().contains("INTERNATIONAL BUSINESS MACHINES"))) {
            details.append("Wrong Customer Name on the main address. IBM should be part of the name.").append("\n");
            engineData.addRejectionComment("OTH", "Wrong Customer Name on the main address. IBM should be part of the name.", "", "");
            return false;
          }
        }

      case SCENARIO_THIRD_PARTY:
      case SCENARIO_CROSSBORDER_THIRD_PARTY:
        if (zi01 == null) {
          details.append("Install-at address should be present for Third party Scenario.").append("\n");
          engineData.addRejectionComment("OTH", "Install-at address should be present for Third Party Scenario.", "", "");
          return false;
        }
      }
    } else {
      if (StringUtils.isBlank(scenario)) {
        engineData.addRejectionComment("OTH", "No Scenario found on the request", "", "");
        details.append("No Scenario found on the request");
        return false;
      }
    }
    return true;
  }

  private String getScenarioDescription(EntityManager entityManager, Data data) {

    String sql = ExternalizedQuery.getSql("GET_SCENARIO_DESC_FR");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("CUST_SUB_TYP_VAL", data.getCustSubGrp());
    query.setParameter("CUST_TYP_VAL", data.getCustGrp());
    query.setForReadOnly(true);
    return query.getSingleResult(String.class);
  }

  @Override
  public void tweakGBGFinderRequest(EntityManager entityManager, GBGFinderRequest request, RequestData requestData, AutomationEngineData engineData) {
    String siret = requestData.getData().getTaxCd1();
    if (!StringUtils.isBlank(siret) && siret.length() > 9) {
      request.setOrgId(siret.substring(0, 9)); // SIREN
      LOG.debug("Passing SIREN as " + request.getOrgId() + " with GBG finder priority.");
      request.setOrgIdFirst("Y");
    }
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
    String scenario = data.getCustSubGrp();
    String coverageId = container.getFinalCoverage();
    Addr addr = requestData.getAddress("ZS01");
    String bgId = data.getBgId();
    String sbo = "";
    details.append("\n");
    if (isCoverageCalculated && StringUtils.isNotBlank(coverageId) && CalculateCoverageElement.COV_BG.equals(covFrom)) {
      if (covFrom != null && !"BGNONE".equals(bgId.trim())) {
        sbo = computeSBOForCovFR(entityManager, QUERY_BG_SBO_FR, bgId, data.getCmrIssuingCntry(), false);
      }

      FieldResultKey sboKeyVal = new FieldResultKey("DATA", "SALES_BO_CD");
      String sboVal = "";
      if (overrides.getData().containsKey(sboKeyVal)) {
        sboVal = overrides.getData().get(sboKeyVal).getNewValue();
        sboVal = sboVal.substring(0, 3);
        overrides.addOverride(AutomationElementRegistry.GBL_CALC_COV, "DATA", "SALES_BO_CD", data.getSalesBusOffCd(), sboVal + sboVal);
        details.append("SORTL: " + sboVal + sboVal);
      } else if (sbo != null && !sbo.isEmpty()) {
        sbo = sbo.substring(0, 3);
        overrides.addOverride(AutomationElementRegistry.GBL_CALC_COV, "DATA", "SALES_BO_CD", data.getSalesBusOffCd(), sbo + sbo);
        details.append("SORTL: " + sbo + sbo);
      }
      engineData.addPositiveCheckStatus(AutomationEngineData.COVERAGE_CALCULATED);
    } else {
      isCoverageCalculated = false;
      // if not calculated using bg/gbg try calculation using SIREN
      details.setLength(0);// clear string builder
      overrides.clearOverrides(); // clear existing overrides
      if (!FRANCE_SUBREGIONS.contains(addr.getLandCntry())) {
        details.append("Calculating Coverage using SIREN.").append("\n\n");
        String siren = StringUtils.isNotBlank(data.getTaxCd1())
            ? (data.getTaxCd1().length() > 9 ? data.getTaxCd1().substring(0, 9) : data.getTaxCd1()) : "";
        if (StringUtils.isNotBlank(siren)) {
          details.append("SIREN: " + siren).append("\n");
          List<CoverageContainer> coverages = covElement.computeCoverageFromRDCQuery(entityManager, "AUTO.COV.GET_COV_FROM_TAX_CD1", siren + "%",
              data.getCmrIssuingCntry());
          if (coverages != null && !coverages.isEmpty()) {
            CoverageContainer coverage = coverages.get(0);
            LOG.debug("Calculated Coverage using SIREN- Final Cov:" + coverage.getFinalCoverage() + ", Base Cov:" + coverage.getBaseCoverage()
                + ", ISU:" + coverage.getIsuCd() + ", CTC:" + coverage.getClientTierCd());
            covElement.logCoverage(entityManager, engineData, requestData, null, details, overrides, null, coverage, CalculateCoverageElement.FINAL,
                CalculateCoverageElement.COV_REQ, true);
            FieldResultKey sboKey = new FieldResultKey("DATA", "SALES_BO_CD");
            String sboValue = "";
            if (overrides.getData().containsKey(sboKey)) {
              sboValue = overrides.getData().get(sboKey).getNewValue();
              overrides.addOverride(AutomationElementRegistry.GBL_CALC_COV, "DATA", "INSTALL_BRANCH_OFF", data.getInstallBranchOff(),
                  sboValue + sboValue);
            } else {
              sboValue = getSBOfromCoverage(entityManager, coverage.getFinalCoverage());
              if (StringUtils.isNotBlank(sboValue)) {
                details.append("SORTL calculated on basis of Existing CMR Data: " + sboValue + sboValue);
                overrides.addOverride(AutomationElementRegistry.GBL_CALC_COV, "DATA", "INSTALL_BRANCH_OFF", data.getInstallBranchOff(),
                    sboValue + sboValue);
                overrides.addOverride(AutomationElementRegistry.GBL_CALC_COV, "DATA", "SALES_BO_CD", data.getSalesBusOffCd(), sboValue + sboValue);
              }
            }
            isCoverageCalculated = true;
            results.setResults("Coverage Calculated");
            engineData.addPositiveCheckStatus(AutomationEngineData.COVERAGE_CALCULATED);
            engineData.put(AutomationEngineData.COVERAGE_CALCULATED, coverage.getFinalCoverage());
          } else {
            details.append("Coverage could not be calculated on the basis of SIREN").append("\n");
          }
        } else {
          details.append("Coverage could not be calculated on the basis of SIREN. SIREN/SIRET not found on the request.").append("\n");
        }
      }

      if (!isCoverageCalculated) {
        // if not calculated using siren as well
        if (("34".equals(data.getIsuCd()) && "Q".equals(data.getClientTier())) || ("36".equals(data.getIsuCd()) && "Y".equals(data.getClientTier()))
            || ("32".equals(data.getIsuCd()) && "T".equals(data.getClientTier()))
            || ("5K".equals(data.getIsuCd()) || "14".equals(data.getIsuCd()) || "18".equals(data.getIsuCd()) || "19".equals(data.getIsuCd())
                || "1R".equals(data.getIsuCd()) || "31".equals(data.getIsuCd()) || "3T".equals(data.getIsuCd()) || "4A".equals(data.getIsuCd()))) {
          details.setLength(0);
          String custGrp = data.getCustGrp();

          if ((custGrp.equals("CROSS") && !SCENARIO_CROSSBORDER_BUSINESS_PARTNER.equals(scenario) && !SCENARIO_CROSSBORDER_INTERNAL.equals(scenario))
              || (custGrp.equals("LOCAL") && !SCENARIO_BUSINESS_PARTNER.equals(scenario) && !SCENARIO_INTERNAL.equals(scenario))) {
            addr = requestData.getAddress("ZS01");
          }

          String logicMsg = "";
          if (("36".equals(data.getIsuCd()) && "Y".equals(data.getClientTier()))) {
            details.append("Calculating coverage using 36Y-new logic.").append("\n");
            logicMsg = "36Y logic.";
          } else if ("5K".equals(data.getIsuCd()) || "14".equals(data.getIsuCd()) || "18".equals(data.getIsuCd()) || "19".equals(data.getIsuCd())
              || "1R".equals(data.getIsuCd()) || "31".equals(data.getIsuCd()) || "3T".equals(data.getIsuCd()) || "4A".equals(data.getIsuCd())) {
            details.append("Calculating coverage using 5K,14,18,19,1R,31,3T,4A-new logic.").append("\n");
            logicMsg = "5K,14,18,19,1R,31,3T,4A logic.";
          } else {
            details.append("Calculating coverage using 34Q-new logic.").append("\n");
            logicMsg = "34Q logic.";
          }

          if ((COVERAGE_34Q.contains(addr.getLandCntry()) && custGrp.equals("CROSS") && !SCENARIO_CROSSBORDER_BUSINESS_PARTNER.equals(scenario)
              && !SCENARIO_CROSSBORDER_INTERNAL.equals(scenario)) || ("36".equals(data.getIsuCd()) && "Y".equals(data.getClientTier()))
              || ("5K".equals(data.getIsuCd()) || "14".equals(data.getIsuCd()) || "18".equals(data.getIsuCd()) || "19".equals(data.getIsuCd())
                  || "1R".equals(data.getIsuCd()) || "31".equals(data.getIsuCd()) || "3T".equals(data.getIsuCd()) || "4A".equals(data.getIsuCd()))) {

            HashMap<String, String> response = getSBOFromMapping(addr.getLandCntry(), data.getIsuCd(), data.getClientTier());
            LOG.debug("Calculated SBO: " + response.get(SBO) + response.get(SBO));
            if (StringUtils.isNotBlank(response.get(MATCHING))) {
              switch (response.get(MATCHING)) {
              case "Exact Match":
                overrides.addOverride(AutomationElementRegistry.GBL_CALC_COV, "DATA", "SALES_BO_CD", data.getSalesBusOffCd(),
                    response.get(SBO) + response.get(SBO));
                overrides.addOverride(AutomationElementRegistry.GBL_CALC_COV, "DATA", "SEARCH_TERM", data.getSearchTerm(), response.get(SBO));
                details.append("Coverage calculation Successful.").append("\n");
                details.append("Computed SORTL = " + response.get(SBO) + response.get(SBO)).append("\n\n");
                details.append("Matched Rule:").append("\n");
                details.append("LANDED = " + addr.getLandCntry()).append("\n");
                details.append("ISU = " + data.getIsuCd()).append("\n");
                details.append("CTC = " + data.getClientTier()).append("\n\n");
                details.append("Matching: " + response.get(MATCHING));
                results.setResults("Coverage Calculated");
                engineData.addPositiveCheckStatus(AutomationEngineData.COVERAGE_CALCULATED);
                break;
              case "No Match Found":
                details.append("Coverage cannot be computed using ").append(logicMsg).append("\n");
                results.setResults("Coverage not calculated.");
                break;
              }
            } else {
              details.append("Coverage cannot be computed using ").append(logicMsg).append("\n");
              results.setResults("Coverage not calculated.");
            }
          } else if (("FR".equals(addr.getLandCntry()) && custGrp.equals("LOCAL") && !SCENARIO_BUSINESS_PARTNER.equals(scenario)
              && !SCENARIO_IBM_EMPLOYEE.equals(scenario) && !SCENARIO_INTERNAL.equals(scenario)) && "34".equals(data.getIsuCd())
              && "Q".equals(data.getClientTier())) {
            // POSTAL CODE LOGIC
            HashMap<String, String> response = getSBOFromPostalCodeMapping(data.getCountryUse(), data.getIsicCd(), addr.getPostCd(), addr.getCity1(),
                data.getIsuCd(), data.getClientTier());
            LOG.debug("Calculated SBO: " + response.get(SBO) + response.get(SBO));
            if (StringUtils.isNotBlank(response.get(MATCHING))) {
              switch (response.get(MATCHING)) {
              case "Exact Match":
                overrides.addOverride(AutomationElementRegistry.GBL_CALC_COV, "DATA", "SALES_BO_CD", data.getSalesBusOffCd(),
                    response.get(SBO) + response.get(SBO));
                details.append("Coverage calculation Successful.").append("\n");
                details.append("Computed SORTL = " + response.get(SBO) + response.get(SBO)).append("\n\n");
                details.append("Matched Rule:").append("\n");
                details.append("ISIC = " + data.getIsicCd()).append("\n");
                details.append("ISU = " + data.getIsuCd()).append("\n");
                details.append("CTC = " + data.getClientTier()).append("\n");
                details.append("Postal Code Starts = " + response.get(POSTAL_CD_STARTS)).append("\n\n");
                details.append("City= " + response.get(CITY)).append("\n\n");
                details.append("Matching: " + response.get(MATCHING));
                results.setResults("Coverage Calculated");
                engineData.addPositiveCheckStatus(AutomationEngineData.COVERAGE_CALCULATED);
                break;
              case "No Match Found":
                details.append("Coverage cannot be computed using PostalCode logic.").append("\n");
                details.append("Starting IsicCd logic to set SORTl.").append("\n");
                // results.setResults("Coverage not calculated.");
                HashMap<String, String> response2 = getSBOFromIsicCdMapping(data.getCountryUse(), data.getIsicCd(), addr.getPostCd(), data.getIsuCd(),
                    data.getClientTier());
                LOG.debug("Calculated SBO: " + response2.get(SBO) + response2.get(SBO));
                if (StringUtils.isNotBlank(response2.get(MATCHING))) {
                  switch (response2.get(MATCHING)) {
                  case "Exact Match":
                    overrides.addOverride(AutomationElementRegistry.GBL_CALC_COV, "DATA", "SALES_BO_CD", data.getSalesBusOffCd(),
                        response2.get(SBO) + response2.get(SBO));
                    details.append("Coverage calculation Successful.").append("\n");
                    details.append("Computed SORTL = " + response2.get(SBO) + response2.get(SBO)).append("\n\n");
                    details.append("Matched Rule:").append("\n");
                    details.append("ISIC = " + data.getIsicCd()).append("\n");
                    details.append("ISU = " + data.getIsuCd()).append("\n");
                    details.append("CTC = " + data.getClientTier()).append("\n");
                    details.append("Postal Code Starts = " + response2.get(ISIC_CD)).append("\n\n");
                    details.append("Matching: " + response2.get(MATCHING));
                    results.setResults("Coverage Calculated");
                    engineData.addPositiveCheckStatus(AutomationEngineData.COVERAGE_CALCULATED);
                    break;
                  case "No Match Found":
                    details.append("Coverage cannot be computed using ").append(logicMsg).append("\n");
                    results.setResults("Coverage not calculated.");
                    break;
                  }
                  break;
                } else {
                  details.append("Coverage cannot be computed using ").append(logicMsg).append("\n");
                  results.setResults("Coverage not calculated.");
                }
              }
            } else {
              // if isu ctc is not 34Q and coverage is not calculated (needs
              // review... whether to set on error true or set skip results
              // here)
              details.setLength(0);
              overrides.clearOverrides();
              details.append("Coverage could not be calculated through Buying Group or SIREN.\n Skipping coverage calculation.").append("\n");
              results.setResults("Skipped");
            }

            /*
             * if ("32".equals(data.getIsuCd()) &&
             * "S".equals(data.getClientTier())) { details.setLength(0);
             * details.append("Calculating coverage using 32S-PostalCode logic."
             * ). append("\n"); if (SCENARIO_INTERNAL_SO.equals(scenario) ||
             * SCENARIO_THIRD_PARTY.equals(scenario) ||
             * SCENARIO_CROSSBORDER_INTERNAL_SO.equals(scenario) ||
             * SCENARIO_CROSSBORDER_THIRD_PARTY.equals(scenario)) { addr =
             * requestData.getAddress("ZI01"); } HashMap<String, String>
             * response = getSBOFromPostalCodeMapping(data.getCountryUse(),
             * data.getIsicCd(), addr.getPostCd(), data.getIsuCd(),
             * data.getClientTier()); LOG.debug("Calculated SBO: " +
             * response.get(SBO) + response.get(SBO)); if
             * (StringUtils.isNotBlank(response.get(MATCHING))) { switch
             * (response.get(MATCHING)) { case "Exact Match":
             * overrides.addOverride(AutomationElementRegistry.GBL_CALC_COV,
             * "DATA", "SALES_BO_CD", data.getSalesBusOffCd(), response.get(SBO)
             * + response.get(SBO));
             * details.append("Coverage calculation Successful.").append("\n");
             * details.append("Computed SORTL = " + response.get(SBO) +
             * response.get(SBO)).append("\n\n");
             * details.append("Matched Rule:").append("\n");
             * details.append("ISIC = " + data.getIsicCd()).append("\n");
             * details.append("ISU = " + data.getIsuCd()).append("\n");
             * details.append("CTC = " + data.getClientTier()).append("\n");
             * details.append("Postal Code Starts = " +
             * response.get(POSTAL_CD_STARTS)).append("\n\n");
             * details.append("Matching: " + response.get(MATCHING));
             * results.setResults("Coverage Calculated");
             * engineData.addPositiveCheckStatus(AutomationEngineData.
             * COVERAGE_CALCULATED); break; case "No Match Found": details.
             * append("Coverage cannot be computed using 32S-PostalCode logic."
             * ). append("\n"); results.setResults("Coverage not calculated.");
             * break; } } else { details.
             * append("Coverage cannot be computed using 32S-PostalCode logic."
             * ). append("\n"); results.setResults("Coverage not calculated.");
             * } } else { // if isu ctc is not 32S and coverage is not
             * calculated (needs // review... whether to set on error true or
             * set skip results here) details.setLength(0);
             * overrides.clearOverrides(); details.
             * append("Coverage could not be calculated through Buying Group or SIREN.\n Skipping coverage calculation."
             * ).append("\n"); results.setResults("Skipped"); }
             */

          } else {
            // if isu ctc is not 34Q and coverage is not calculated (needs
            // review... whether to set on error true or set skip results
            // here)
            details.setLength(0);
            overrides.clearOverrides();
            details.append("Coverage could not be calculated through Buying Group or SIREN.\n Skipping coverage calculation.").append("\n");
            results.setResults("Skipped");
          }
        }
      }
    }
    return true;
  }

  private String computeSBOForCovFR(EntityManager entityManager, String queryBgFR, String bgId, String cmrIssuingCntry, boolean b) {
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

    LOG.debug("Calculating SORTL using France query " + queryBgFR + " for key: " + bgId);
    List<Object[]> results = query.getResults(5);
    List<String> sortlList = new ArrayList<String>();
    if (results != null && !results.isEmpty()) {
      for (Object[] result : results) {
        sortl = (String) result[0];
        sortlList.add(sortl);
      }
    }
    sortl = sortlList.get(0);
    return sortl;
  }

  private HashMap<String, String> getSBOFromPostalCodeMapping(String countryUse, String isicCd, String postCd, String city, String isuCd,
      String clientTier) {
    HashMap<String, String> response = new HashMap<String, String>();
    String regex = "\\s+$";
    city = (city.toUpperCase()).replaceAll(regex, "");
    response.put(MATCHING, "");
    response.put(POSTAL_CD_STARTS, "");
    response.put(CITY, "");
    response.put(SBO, "");
    if (!sortlMappings.isEmpty()) {
      for (FrSboMapping mapping : sortlMappings) {
        if (countryUse.equals(mapping.getCountryUse()) && isuCd.equals(mapping.getIsu()) && clientTier.equals(mapping.getCtc())) {
          if (StringUtils.isNotBlank(mapping.getPostalCdStarts()) && StringUtils.isNotBlank(mapping.getCity())) {
            String[] postalCodeRanges = mapping.getPostalCdStarts().replaceAll("\n", "").replaceAll(" ", "").split(",");
            String[] cityRanges = mapping.getCity().replaceAll("\n", "").replaceAll(" ", "").split(",");
            for (String postalCdRange : postalCodeRanges) {
              for (String cityRange : cityRanges) {
                if (postCd.startsWith(postalCdRange) && city.equals(cityRange)) {
                  response.put(MATCHING, "Exact Match");
                  response.put(SBO, mapping.getSbo());
                  response.put(CITY, cityRange);
                  response.put(POSTAL_CD_STARTS, postalCdRange);
                  return response;
                } else if ("NIORT".equals(city)) {
                  response.put(MATCHING, "Exact Match");
                  response.put(SBO, "8CB");
                  response.put(CITY, cityRange);
                  response.put(POSTAL_CD_STARTS, "any postal code");
                  return response;
                }
              }
            }
          }
        } else {
          response.put(MATCHING, "No Match Found");
          return response;
        }
      }
      response.put(MATCHING, "No Match Found");
      return response;
    } else {
      response.put(MATCHING, "No Match Found");
      return response;
    }
  }

  private HashMap<String, String> getSBOFromIsicCdMapping(String countryUse, String isicCd, String postCd, String isuCd, String clientTier) {
    HashMap<String, String> response = new HashMap<String, String>();
    response.put(MATCHING, "");
    response.put(ISIC_CD, "");
    response.put(SBO, "");
    if (!sortlMappings.isEmpty()) {
      for (FrSboMapping mapping : sortlMappings) {
        List<String> isicCds = new ArrayList<String>();
        if (mapping.getIsicCds() != null && !mapping.getIsicCds().isEmpty()) {
          isicCds = Arrays.asList(mapping.getIsicCds().replaceAll("\n", "").replaceAll(" ", "").split(","));
        }
        if (countryUse.equals(mapping.getCountryUse()) && (isicCds.isEmpty() || (!isicCds.isEmpty() && isicCds.contains(isicCd)))
            && isuCd.equals(mapping.getIsu()) && clientTier.equals(mapping.getCtc())) {
          for (String isicCD : isicCds) {
            if (isicCds.contains(isicCD)) {
              response.put(MATCHING, "Exact Match");
              response.put(SBO, mapping.getSbo());
              response.put(ISIC_CD, isicCD);
              return response;
            }
          }
        }
        // else {
        // response.put(MATCHING, "Exact Match");
        // response.put(SBO, mapping.getSbo());
        // response.put(POSTAL_CD_STARTS, "- No Postal Code Range and isicCd
        // Defined -");
        // return response;
        // }
      }
      response.put(MATCHING, "No Match Found");
      return response;
    } else {
      response.put(MATCHING, "No Match Found");
      return response;
    }
  }

  @Override
  public String getAddressTypeForGbgCovCalcs(EntityManager entityManager, RequestData requestData, AutomationEngineData engineData) throws Exception {
    Data data = requestData.getData();
    String scenario = data.getCustSubGrp();
    String address = "ZS01";

    LOG.debug("Address for the scenario to check: " + scenario);
    if (SCENARIO_INTERNAL_SO.equals(scenario) || SCENARIO_THIRD_PARTY.equals(scenario) || SCENARIO_CROSSBORDER_INTERNAL_SO.equals(scenario)
        || SCENARIO_CROSSBORDER_THIRD_PARTY.equals(scenario)) {
      address = "ZI01";
    }
    return address;
  }

  private HashMap<String, String> getSBOFromMapping(String landedCountry, String isuCd, String clientTier) {
    HashMap<String, String> response = new HashMap<String, String>();
    response.put(MATCHING, "");
    response.put(SBO, "");
    if (!sortlMappings.isEmpty()) {
      for (FrSboMapping mapping : sortlMappings) {
        if (landedCountry.equals(mapping.getCountryLanded()) && isuCd.equals(mapping.getIsu()) && clientTier.equals(mapping.getCtc())) {
          response.put(MATCHING, "Exact Match");
          response.put(SBO, mapping.getSbo());
          return response;
        }
      }

      if (isuCd.equals("34") && clientTier.equals("Y")) {
        response.put(MATCHING, "Exact Match");
        response.put(SBO, "09A");
        return response;
      }

      response.put(MATCHING, "No Match Found");
      return response;
    } else {
      response.put(MATCHING, "No Match Found");
      return response;
    }
  }

  // @Override
  // public boolean runUpdateChecksForData(EntityManager entityManager,
  // AutomationEngineData engineData, RequestData requestData,
  // RequestChangeContainer changes, AutomationResult<ValidationOutput> output,
  // ValidationOutput validation) throws Exception {
  // Admin admin = requestData.getAdmin();
  // Data data = requestData.getData();
  //
  // Addr soldTo = requestData.getAddress("ZS01");
  // StringBuilder detail = new StringBuilder();
  // boolean isNegativeCheckNeedeed = false;
  // LOG.debug("Changes are -> " + changes);
  //
  // DataRdc rdc = getDataRdc(entityManager, admin);
  // if ("9500".equals(rdc.getIsicCd())) {
  // LOG.debug("Private customer record. Skipping validations.");
  // validation.setSuccess(true);
  // validation.setMessage("Skipped");
  // engineData.addPositiveCheckStatus(AutomationEngineData.SKIP_VAT_CHECKS);
  // output.setDetails("Update checks skipped for Private Customer record.");
  // return true;
  // }
  // if (changes != null && changes.hasDataChanges()) {
  // LOG.debug("Changes has data changes -> " + changes.hasDataChanges());
  // boolean vatChngd = changes.isDataChanged("VAT #");
  // boolean collCdChngd = changes.isDataChanged("Collection Code");
  // boolean topLstChngd = changes.isDataChanged("Top List Speciale");
  // boolean sboChngd = changes.isDataChanged("Search Term/Sales Branch
  // Office");
  // boolean iboChngd = changes.isDataChanged("Installing BO");
  // boolean isuCdChngd = changes.isDataChanged("ISU Code");
  // boolean ctcChngd = changes.isDataChanged("Client Tier");
  //
  // if (!"9500".equals(data.getIsicCd()) && (vatChngd || collCdChngd ||
  // topLstChngd || sboChngd || iboChngd || isuCdChngd || ctcChngd)) {
  // if (vatChngd) {
  // LOG.debug("Changes has VAT changes -> " + changes.isDataChanged("VAT #"));
  // UpdatedDataModel vatChange = changes.getDataChange("VAT #");
  // if (vatChange != null) {
  // if (StringUtils.isBlank(vatChange.getOldData()) &&
  // StringUtils.isNotBlank(vatChange.getNewData())) {
  // // check if the name + VAT exists in D&B
  // List<DnBMatchingResponse> matches = getMatches(requestData, engineData,
  // soldTo, true);
  // if (matches.isEmpty()) {
  // // get DnB matches based on all address details
  // matches = getMatches(requestData, engineData, soldTo, false);
  // }
  // if (!matches.isEmpty()) {
  // for (DnBMatchingResponse dnbRecord : matches) {
  // if ("Y".equals(dnbRecord.getOrgIdMatch())) {
  // isNegativeCheckNeedeed = false;
  // break;
  // }
  // isNegativeCheckNeedeed = true;
  // }
  // }
  // if (isNegativeCheckNeedeed) {
  // validation.setSuccess(false);
  // validation.setMessage("Not validated");
  // detail.append("Updates to VAT need verification as it does'nt match DnB");
  // engineData.addNegativeCheckStatus("UPDT_REVIEW_NEEDED", "Updated elements
  // cannot be checked automatically.");
  // LOG.debug("Updates to VAT need verification as it does not match DnB");
  // }
  //
  // }
  // }
  // }
  //
  // if (collCdChngd) {
  // UpdatedDataModel collCdChange = changes.getDataChange("Collection Code");
  // if (collCdChange != null) {
  // if ((StringUtils.isBlank(collCdChange.getOldData()) &&
  // StringUtils.isNotBlank(collCdChange.getNewData()))
  // || (StringUtils.isNotBlank(collCdChange.getOldData()) &&
  // StringUtils.isNotBlank(collCdChange.getNewData()))) {
  // if (!"AR".equalsIgnoreCase(admin.getRequestingLob())) {
  // isNegativeCheckNeedeed = true;
  // }
  // }
  //
  // if (isNegativeCheckNeedeed) {
  // validation.setSuccess(false);
  // validation.setMessage("Not validated");
  // detail.append("Updates to Collection Code need verification.");
  // engineData.addNegativeCheckStatus("UPDT_REVIEW_NEEDED", "Updated elements
  // cannot be checked automatically.");
  // LOG.debug("Updates to Collection Code need verification.");
  // }
  //
  // }
  // }
  //
  // if (topLstChngd) {
  // UpdatedDataModel commFinanceChange = changes.getDataChange("Top List
  // Speciale");
  // if (commFinanceChange != null) {
  // String designatedUser = SystemParameters.getString("TOP_LST_SPECI_USER");
  // isNegativeCheckNeedeed =
  // admin.getRequesterId().equalsIgnoreCase(designatedUser) ? false : true;
  // if (isNegativeCheckNeedeed) {
  // validation.setSuccess(false);
  // validation.setMessage("Not validated");
  // detail.append("Updates to Top List Speciale need verification.");
  // engineData.addNegativeCheckStatus("UPDT_REVIEW_NEEDED", "Updated elements
  // cannot be checked automatically.");
  // LOG.debug("Updates to Top List Speciale need verification.");
  // }
  //
  // }
  // }
  //
  // if (isuCdChngd || ctcChngd || sboChngd || iboChngd) {
  // UpdatedDataModel isuCdChange = changes.getDataChange("ISU Code");
  // UpdatedDataModel clientTierChange = changes.getDataChange("Client Tier");
  // UpdatedDataModel sboChange = changes.getDataChange("Search Term/Sales
  // Branch Office");
  // UpdatedDataModel iboChange = changes.getDataChange("Installing BO");
  //
  // if (isuCdChange != null || clientTierChange != null || sboChange != null ||
  // iboChange != null) {
  // String designatedUser = SystemParameters.getString("ISU_CTC_SBO_USER");
  // isNegativeCheckNeedeed =
  // admin.getRequesterId().equalsIgnoreCase(designatedUser) ? false : true;
  // if (isNegativeCheckNeedeed) {
  // validation.setSuccess(false);
  // validation.setMessage("Not validated");
  // detail.append("Updates to ISU/CTC/SBO/IBO need verification.");
  // engineData.addNegativeCheckStatus("UPDT_REVIEW_NEEDED", "Updated elements
  // cannot be checked automatically.");
  // LOG.debug("Updates to ISU/CTC/SBO/IBO need verification.");
  // }
  //
  // }
  // }
  //
  // } else if (!vatChngd && !collCdChngd && !isuCdChngd && !topLstChngd &&
  // !sboChngd && !iboChngd && !ctcChngd) {
  // isNegativeCheckNeedeed = true;
  // validation.setSuccess(false);
  // validation.setMessage("Not validated");
  // detail.append("Updates to fields need verification.");
  // engineData.addNegativeCheckStatus("UPDT_REVIEW_NEEDED", "Updated elements
  // cannot be checked automatically.");
  // LOG.debug("Updates to fields need verification.");
  // }
  // }
  //
  // if (!isNegativeCheckNeedeed) {
  // validation.setSuccess(true);
  // validation.setMessage("Validated");
  // }
  // output.setDetails(detail.toString());
  // return true;
  // }

  @Override
  public void filterDuplicateCMRMatches(EntityManager entityManager, RequestData requestData, AutomationEngineData engineData,
      MatchingResponse<DuplicateCMRCheckResponse> response) {
    String[] scenariosToBeChecked = { "COMME", "CBMME", "GOVRN", "CBVRN", "BPIEU", "CBIEU", "CBIEM", "XBLUM" };
    String scenario = requestData.getData().getCustSubGrp();
    String[] kuklaComme = { "11" };
    String[] kuklaGovrn = { "13", "14", "17" };
    String[] kuklaBuspr = { "42", "43", "45", "46", "47", "48" };
    String[] kuklaCBIEM = { "71" };
    String[] kuklaXBLUM = { "60" };

    if (Arrays.asList(scenariosToBeChecked).contains(scenario)) {
      List<DuplicateCMRCheckResponse> matches = response.getMatches();
      List<DuplicateCMRCheckResponse> filteredMatches = new ArrayList<DuplicateCMRCheckResponse>();
      for (DuplicateCMRCheckResponse match : matches) {
        if (StringUtils.isNotBlank(match.getCustClass())) {
          String kukla = match.getCustClass() != null ? match.getCustClass() : "";
          if (Arrays.asList(kuklaComme).contains(kukla) && ("COMME".equals(scenario) || "CBMME".equals(scenario))) {
            filteredMatches.add(match);
          } else if (Arrays.asList(kuklaGovrn).contains(kukla) && ("GOVRN".equals(scenario) || "CBVRN".equals(scenario))) {
            filteredMatches.add(match);
          } else if (Arrays.asList(kuklaBuspr).contains(kukla)
              && ("BPIEU".equals(scenario) || "CBIEU".equals(scenario) || "BUSPR".equals(scenario) || "XBUSP".equals(scenario))) {
            filteredMatches.add(match);
          } else if (Arrays.asList(kuklaCBIEM).contains(kukla) && ("CBIEM".equals(scenario))) {
            filteredMatches.add(match);
          } else if (Arrays.asList(kuklaXBLUM).contains(kukla) && ("XBLUM".equals(scenario))) {
            filteredMatches.add(match);
          }
        }

      }
      // set filtered matches in response
      response.setMatches(filteredMatches);
    }

  }

  @Override
  public boolean runUpdateChecksForData(EntityManager entityManager, AutomationEngineData engineData, RequestData requestData,
      RequestChangeContainer changes, AutomationResult<ValidationOutput> output, ValidationOutput validation) throws Exception {

    Admin admin = requestData.getAdmin();
    Data data = requestData.getData();
    Addr soldTo = requestData.getAddress("ZS01");

    StringBuilder details = new StringBuilder();
    boolean cmdeReview = false;
    List<String> ignoredUpdates = new ArrayList<String>();
    for (UpdatedDataModel change : changes.getDataUpdates()) {
      switch (change.getDataField()) {
      // case "VAT #":
      // if ((StringUtils.isBlank(change.getOldData()) &&
      // !StringUtils.isBlank(change.getNewData())) ||
      // (!StringUtils.isBlank(change.getOldData())
      // && !StringUtils.isBlank(change.getNewData()) &&
      // !(change.getOldData().equals(change.getNewData())))) {
      // // ADD and Update
      // List<DnBMatchingResponse> matches = getMatches(requestData,
      // engineData, soldTo, true);
      // boolean matchesDnb = false;
      // if (matches != null) {
      // // check against D&B
      // matchesDnb = ifaddressCloselyMatchesDnb(matches, soldTo, admin,
      // data.getCmrIssuingCntry());
      // }
      // if (!matchesDnb) {
      // cmdeReview = true;
      // engineData.addNegativeCheckStatus("_esVATCheckFailed", "VAT # on the
      // request did not match D&B");
      // details.append("VAT # on the request did not match D&B\n");
      // } else {
      // details.append("VAT # on the request matches D&B\n");
      // }
      // }
      // if (!StringUtils.isBlank(change.getOldData()) &&
      // (StringUtils.isBlank(change.getNewData()))) {
      // // noop, for switch handling only
      // }
      // break;
      case "ISU Code":
      case "Client Tier":
      case "Search Term (SORTL)":
      case "Tax Code": {
        // noop for switch handling
      }
        break;
      case "Order Block Code":
        if ("88".equals(change.getOldData()) || "88".equals(change.getNewData()) || "94".equals(change.getOldData())
            || "94".equals(change.getOldData())) {
          // noop, for switch handling only
        }
        break;
      case "ISIC":
      case "INAC/NAC Code":
      case "SIRET":
        cmdeReview = true;
        details.append("Updates to one or more fields cannot be validated.\n");
        details.append("-" + change.getDataField() + " needs to be verified.\n");
        break;
      default:
        ignoredUpdates.add(change.getDataField());
        break;
      }
    }

    if (cmdeReview) {
      engineData.addNegativeCheckStatus("_esDataCheckFailed", "Updates to one or more fields cannot be validated.");
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
    List<Addr> addresses = null;
    StringBuilder checkDetails = new StringBuilder();
    Set<String> resultCodes = new HashSet<String>();// R - review
    if (StringUtils.isNotBlank(data.getCustClass())
        && ("60".equals(data.getCustClass()) || "71".equals(data.getCustClass()) || "81".equals(data.getCustClass()))) {
      LOG.debug("Skipping validations.");
      validation.setSuccess(true);
      validation.setMessage("Skipped Address Update");
      output.setDetails("Skipped Address update on CMR with Kukla " + data.getCustClass());
      engineData.addPositiveCheckStatus(AutomationEngineData.SKIP_CHECKS);
      return true;
    }

    for (String addrType : RELEVANT_ADDRESSES) {
      if (changes.isAddressChanged(addrType)) {
        if (CmrConstants.RDC_SOLD_TO.equals(addrType)) {
          addresses = Collections.singletonList(requestData.getAddress(CmrConstants.RDC_SOLD_TO));
        } else {
          addresses = requestData.getAddresses(addrType);
        }
        for (Addr addr : addresses) {
          if ("N".equals(addr.getImportInd())) {
            // new address
            if (!CmrConstants.RDC_SOLD_TO.equals(addrType) && !CmrConstants.RDC_BILL_TO.equals(addrType)) {
              engineData.addPositiveCheckStatus(AutomationEngineData.SKIP_DNB_ORGID_VAL);
            }

            if (addressExists(entityManager, addr, requestData)) {
              LOG.debug(" - Duplicates found for " + addrType + "(" + addr.getId().getAddrSeq() + ")");
              checkDetails.append("Address " + addrType + "(" + addr.getId().getAddrSeq() + ") provided matches an existing address.\n");
              resultCodes.add("R");
            } else if (CmrConstants.RDC_BILL_TO.equals(addrType)) {

              // CMR - 1606
              // if bill-to has been updated , validate Bill-To with
              // DnB
              Addr addrToChk = requestData.getAddress(addrType);
              boolean matchesDnbBillTo = false;
              List<DnBMatchingResponse> matches = getMatches(requestData, engineData, addrToChk, false);
              matchesDnbBillTo = ifaddressCloselyMatchesDnb(matches, addrToChk, admin, data.getCmrIssuingCntry());

              if (!matches.isEmpty() && matches != null && matches.size() > 0 && matchesDnbBillTo) {
                // validate sold to with siret
                Addr soldTo = requestData.getAddress(CmrConstants.RDC_SOLD_TO);
                boolean matchesDnbSoldTo = false;
                List<DnBMatchingResponse> matchesSoldTo = getMatches(requestData, engineData, soldTo, false);
                matchesDnbSoldTo = ifaddressCloselyMatchesDnb(matchesSoldTo, soldTo, admin, data.getCmrIssuingCntry());

                if (!matchesDnbSoldTo) {
                  resultCodes.add("D"); // send to cmde for review
                  engineData.addNegativeCheckStatus("_frSIRETCheckFailed",
                      "Bill-To address is validated in DnB but Sold-To with SIRET couldn't be validated.");
                  checkDetails.append("Bill-To address is validated in DnB but Sold-To with SIRET couldn't be validated.\n");
                } else {
                  // check against D&B
                  boolean siretMatch = false;
                  for (DnBMatchingResponse dnb : matches) {
                    String siret = DnBUtil.getTaxCode1(dnb.getDnbCountry(), dnb.getOrgIdDetails());
                    if (StringUtils.isNotBlank(siret) && siret.equalsIgnoreCase(data.getTaxCd1()) && !"O".equalsIgnoreCase(dnb.getOperStatusCode())) {
                      siretMatch = true;
                      checkDetails.append("Bill-To address is validated in DnB and Sold-To with SIRET is validated and is active.\n");
                      break;
                    }
                  }
                  if (!siretMatch) {
                    resultCodes.add("D"); // send to cmde for review
                    engineData.addNegativeCheckStatus("_frSIRETCheckFailed",
                        "Bill-To is validated but SIRET with Sold-To couldn't be validated in DnB.");
                    checkDetails.append("Bill-To is validated but SIRET with Sold-To couldn't be validated in DnB.\n");
                  }
                }
              } else {
                resultCodes.add("D"); // CMDE review
                engineData.addNegativeCheckStatus("_frSIRETCheckFailed", "Updated Bill-To address could not be validated in DnB.");
                checkDetails.append("Updated Bill-To address could not be validated in DnB.\n");
              }
            }

          } else if ("Y".equals(addr.getChangedIndc())) {
            // update address
            if (isRelevantAddressFieldUpdated(changes, addr)) {
              if (CmrConstants.RDC_INSTALL_AT.equals(addrType)) {
                engineData.addPositiveCheckStatus(AutomationEngineData.SKIP_DNB_ORGID_VAL);
                if (null == changes.getAddressChange(addrType, "Customer legal name")
                    && null == changes.getAddressChange(addrType, "Legal name continued")) {
                  LOG.debug("Update to InstallAt " + addrType + "(" + addr.getId().getAddrSeq() + ")");
                  checkDetails.append("Update to InstallAt (" + addr.getId().getAddrSeq() + ") skipped in the checks.\n");
                } else {
                  resultCodes.add("D");
                  checkDetails.append("Update to InstallAt (" + addr.getId().getAddrSeq() + ") has different customer name than sold-to .\n");
                }
              } else if (CmrConstants.RDC_SOLD_TO.equals(addrType)) {
                // LOG.debug("Update to Address " + addrType + "(" +
                // addr.getId().getAddrSeq() + ") needs to be verified");
                // checkDetails.append("Update to address " + addrType + "(" +
                // addr.getId().getAddrSeq() + ") needs to be verified \n");
                // resultCodes.add("D");

                // CMR - 1218
                UpdatedDataModel siretChange = changes.getDataChange("SIRET");
                if (siretChange != null) {
                  // means address and siret both have bene updated
                  resultCodes.add("D"); // send to cmde for review
                  // engineData.addPositiveCheckStatus(AutomationEngineData.SKIP_DNB_ORGID_VAL);
                  LOG.debug("Updates to Address " + addrType + "(" + addr.getId().getAddrSeq()
                      + ") could not be verified because of SIRET update. CMDE review required.\\n");
                  checkDetails.append("Updates to Address " + addrType + "(" + addr.getId().getAddrSeq()
                      + ") could not be verified because of SIRET update. CMDE review required.\n");
                } else {
                  Addr addrToChk = requestData.getAddress(addrType);
                  List<DnBMatchingResponse> matches = getMatches(requestData, engineData, addrToChk, false);
                  boolean matchesDnbSoldTo = false;
                  matchesDnbSoldTo = ifaddressCloselyMatchesDnb(matches, addrToChk, admin, data.getCmrIssuingCntry());
                  if (!matchesDnbSoldTo) {
                    resultCodes.add("D"); // send to cmde for review
                    engineData.addNegativeCheckStatus("_frSIRETCheckFailed",
                        "Sold-To address is validated in DnB but Sold-To with SIRET couldn't be validated.");
                    checkDetails.append("Sold-To address is validated in DnB but Sold-To with SIRET couldn't be validated.\n");
                  } else {
                    // siert check against D&B
                    boolean siretMatch = false;
                    for (DnBMatchingResponse dnb : matches) {
                      String siret = DnBUtil.getTaxCode1(dnb.getDnbCountry(), dnb.getOrgIdDetails());
                      if (StringUtils.isNotBlank(siret) && siret.equalsIgnoreCase(data.getTaxCd1())
                          && !"O".equalsIgnoreCase(dnb.getOperStatusCode())) {
                        siretMatch = true;
                        checkDetails.append("Sold-To address is validated in DnB and Sold-To with SIRET is validated and is active.\n");
                        break;
                      }
                    }
                    if (!siretMatch) {
                      resultCodes.add("R"); // reject
                      engineData.addNegativeCheckStatus("_frSIRETCheckFailed",
                          "Sold-To is validated but SIRET with Sold-To couldn't be validated in DnB.");
                      checkDetails.append("Sold-To is validated but SIRET with Sold-To couldn't be validated in DnB.\n");
                    }
                  }
                }
                // else {
                // // if only address has been updated , validate vat with DnB
                // Addr addrToChk = requestData.getAddress(addrType);
                // List<DnBMatchingResponse> matches = getMatches(requestData,
                // engineData, addrToChk, true);
                // boolean matchesDnb = false;
                // if (matches != null) {
                // // check against D&B
                // matchesDnb = ifaddressCloselyMatchesDnb(matches, addrToChk,
                // admin, data.getCmrIssuingCntry());
                // }
                // if (!matchesDnb) {
                // //
                // engineData.addPositiveCheckStatus(AutomationEngineData.SKIP_DNB_ORGID_VAL);
                // resultCodes.add("R"); // reject
                // engineData.addNegativeCheckStatus("_frVATCheckFailed", "VAT #
                // on the request did not match D&B");
                // checkDetails.append("VAT # on the request did not match
                // D&B\n");
                // } else {
                // checkDetails.append("VAT # on the request matches D&B\n");
                // }
                //
                // }
              } else if (CmrConstants.RDC_BILL_TO.equals(addrType)) {
                // CMR - 1606
                // if bill-to has been updated , validate Bill-To with
                // DnB
                Addr addrToChk = requestData.getAddress(addrType);
                boolean matchesDnbBillTo = false;
                List<DnBMatchingResponse> matches = getMatches(requestData, engineData, addrToChk, false);
                matchesDnbBillTo = ifaddressCloselyMatchesDnb(matches, addrToChk, admin, data.getCmrIssuingCntry());

                if (!matches.isEmpty() && matches != null && matches.size() > 0 && matchesDnbBillTo) {
                  // validate sold to with siret
                  Addr soldTo = requestData.getAddress(CmrConstants.RDC_SOLD_TO);
                  boolean matchesDnbSoldTo = false;
                  List<DnBMatchingResponse> matchesSoldTo = getMatches(requestData, engineData, soldTo, false);
                  matchesDnbSoldTo = ifaddressCloselyMatchesDnb(matchesSoldTo, soldTo, admin, data.getCmrIssuingCntry());

                  if (!matchesDnbSoldTo) {
                    resultCodes.add("D"); // send to cmde for review
                    engineData.addNegativeCheckStatus("_frSIRETCheckFailed",
                        "Bill-To address is validated in DnB but Sold-To with SIRET couldn't be validated.");
                    checkDetails.append("Bill-To address is validated in DnB but Sold-To with SIRET couldn't be validated.\n");
                  } else {
                    // check against D&B
                    boolean siretMatch = false;
                    for (DnBMatchingResponse dnb : matches) {
                      String siret = DnBUtil.getTaxCode1(dnb.getDnbCountry(), dnb.getOrgIdDetails());
                      if (StringUtils.isNotBlank(siret) && siret.equalsIgnoreCase(data.getTaxCd1())
                          && !"O".equalsIgnoreCase(dnb.getOperStatusCode())) {
                        siretMatch = true;
                        checkDetails.append("Bill-To address is validated in DnB and Sold-To with SIRET is validated and is active.\n");
                        break;
                      }
                    }
                    if (!siretMatch) {
                      resultCodes.add("R"); // reject
                      engineData.addNegativeCheckStatus("_frSIRETCheckFailed",
                          "Bill-To is validated but SIRET with Sold-To couldn't be validated in DnB.");
                      checkDetails.append("Bill-To is validated but SIRET with Sold-To couldn't be validated in DnB.\n");
                    }
                  }
                } else {
                  resultCodes.add("D"); // CMDE review
                  engineData.addNegativeCheckStatus("_frSIRETCheckFailed", "Updated Bill-To address could not be validated in DnB.");
                  checkDetails.append("Updated Bill-To address could not be validated in DnB.\n");
                }
              } else {
                // proceed
                engineData.addPositiveCheckStatus(AutomationEngineData.SKIP_DNB_ORGID_VAL);
                LOG.debug("Update to Address " + addrType + "(" + addr.getId().getAddrSeq() + ") skipped in the checks.\\n");
                checkDetails.append("Updates to Address (" + addr.getId().getAddrSeq() + ") skipped in the checks.\n");
              }
            } else {
              checkDetails.append("Updates to non-address fields for " + addrType + "(" + addr.getId().getAddrSeq() + ") skipped in the checks.")
                  .append("\n");
            }
          }
        }
      }
    }
    if (resultCodes.contains("R")) {
      output.setOnError(true);
      engineData.addRejectionComment("DUPADDR", "Add or update on the address is rejected", "", "");
      validation.setSuccess(false);
      validation.setMessage("Rejected");
    } else if (resultCodes.contains("D")) {
      validation.setSuccess(false);
      validation.setMessage("Not Validated");
      engineData.addNegativeCheckStatus("_atCheckFailed", "Updates to addresses cannot be checked automatically.");
    } else {
      validation.setSuccess(true);
      validation.setMessage("Successful");
    }
    String details = (output.getDetails() != null && output.getDetails().length() > 0) ? output.getDetails() : "";
    details += checkDetails.length() > 0 ? "\n" + checkDetails.toString() : "";
    output.setDetails(details);
    output.setProcessOutput(validation);
    return true;

  }

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

  /*
   * private boolean isRelevantFieldUpdated(RequestChangeContainer changes) {
   * boolean isRelevantFieldUpdated = false; List<UpdatedNameAddrModel>
   * updatedAddrList = changes.getAddressUpdates(); String[] addressFields = {
   * "Customer Name", "Customer Name Continuation",
   * "Customer Name/ Additional Address Information", "Country (Landed)",
   * "Street", "Street Continuation", "Postal Code", "City", "PostBox" };
   * List<String> relevantFieldNames = Arrays.asList(addressFields); for
   * (UpdatedNameAddrModel updatedAddrModel : updatedAddrList) { String fieldId
   * = updatedAddrModel.getDataField(); if (StringUtils.isNotEmpty(fieldId) &&
   * relevantFieldNames.contains(fieldId)) { isRelevantFieldUpdated = true;
   * break; } }
   * 
   * return isRelevantFieldUpdated; }
   */

  /**
   * @param entityManager
   * @param requestData
   * @param engineData
   * @param result
   * @param details
   * @param output
   * @return
   */
  @Override
  protected boolean doPrivatePersonChecks(AutomationEngineData engineData, String country, String landCntry, String name, StringBuilder details,
      boolean checkBluepages, RequestData reqData) {
    EntityManager entityManager = JpaManager.getEntityManager();
    boolean legalEndingExists = false;
    Data data = reqData.getData();
    for (Addr addr : reqData.getAddresses()) {
      String customerName = getCustomerFullName(addr);
      if (hasLegalEndings(customerName)) {
        legalEndingExists = true;
        break;
      }
    }
    if (legalEndingExists) {
      String commentSpecific = "Commercial.";
      String commentGeneric = "Changed.";
      String[] arrCntries = { "834", "616" };
      List<String> genericCmtCntries = Arrays.asList(arrCntries);
      if (genericCmtCntries.contains(country)) {
        engineData.addRejectionComment("OTH", "Scenario chosen is incorrect, should be " + commentGeneric, "", "");
        details.append("Scenario chosen is incorrect, should be " + commentGeneric).append("\n");
      } else {
        engineData.addRejectionComment("OTH", "Scenario chosen is incorrect, should be " + commentSpecific, "", "");
        details.append("Scenario chosen is incorrect, should be " + commentSpecific).append("\n");
      }
      return false;
    }

    // Duplicate Request check with customer name

    List<String> dupReqIds = checkDuplicateRequest(entityManager, reqData);
    if (!dupReqIds.isEmpty()) {
      details.append("Duplicate request found with matching customer name.\nMatch found with Req id :").append("\n");
      details.append(StringUtils.join(dupReqIds, "\n"));
      engineData.addRejectionComment("OTH", "Duplicate request found with matching customer name.", "", "");
      return false;
    } else {
      details.append("No duplicate requests found").append("\n");
    }

    PrivatePersonCheckResult checkResult = chkPrivatePersonRecordFR(country, landCntry, name, checkBluepages, reqData.getData());
    PrivatePersonCheckStatus checkStatus = checkResult.getStatus();
    String scenario = data.getCustSubGrp();
    switch (checkStatus) {
    case BluepagesError:
      engineData.addNegativeCheckStatus("BLUEPAGES_NOT_VALIDATED", "Not able to check the name against bluepages.");
      break;
    case DuplicateCMR:
      details.append("The name already matches a current record with CMR No. " + checkResult.getCmrNo()).append("\n");
      engineData.addRejectionComment("DUPC", "The name already has matches a current record with CMR No. " + checkResult.getCmrNo(),
          checkResult.getCmrNo(), checkResult.getKunnr());
      return false;
    case DuplicateCheckError:
      details.append("Duplicate CMR check using customer name match failed to execute.").append("\n");
      engineData.addNegativeCheckStatus("DUPLICATE_CHECK_ERROR", "Duplicate CMR check using customer name match failed to execute.");
      break;
    case NoIBMRecord:
      if (SCENARIO_IBM_EMPLOYEE.equalsIgnoreCase(scenario)) {
        engineData.addRejectionComment("OTH", "Employee details not found in IBM People.", "", "");
        details.append("Employee details not found in IBM People.").append("\n");
        return false;
      }
    case Passed:
      details.append("No Duplicate CMRs were found.").append("\n");
      break;
    case PassedBoth:
      details.append("No Duplicate CMRs were found.").append("\n");
      details.append("Name validated against IBM BluePages successfully.").append("\n");
      break;
    }
    return true;
  }

  private PrivatePersonCheckResult chkPrivatePersonRecordFR(String country, String landCntry, String name, boolean checkBluePages, Data data) {
    LOG.debug("Validating Private Person record for " + name);
    try {
      DuplicateCMRCheckResponse checkResponse = chkDupPrivatePersonRecordFR(name, country, landCntry, data);
      String cmrNo = "";
      if (checkResponse != null) {
        cmrNo = checkResponse.getCmrNo();
      }
      // TODO find kunnr String kunnr = checkResponse.get
      if (!StringUtils.isBlank(cmrNo)) {
        LOG.debug("Duplicate CMR No. found: " + checkResponse.getCmrNo());
        return new PrivatePersonCheckResult(PrivatePersonCheckStatus.DuplicateCMR, cmrNo, null);
      }
    } catch (Exception e) {
      LOG.warn("Duplicate CMR check error.", e);
      return new PrivatePersonCheckResult(PrivatePersonCheckStatus.DuplicateCheckError);
    }
    if (checkBluePages) {
      LOG.debug("Checking against BluePages..");
      Person person = null;
      try {
        person = BluePagesHelper.getPersonByName(name, data.getCmrIssuingCntry());
        if (person == null) {
          LOG.debug("NO BluePages record found");
          return new PrivatePersonCheckResult(PrivatePersonCheckStatus.NoIBMRecord);
        } else {
          LOG.debug("BluePages record found: " + person.getName() + "/" + person.getEmail());
          return new PrivatePersonCheckResult(PrivatePersonCheckStatus.PassedBoth);
        }
      } catch (Exception e) {
        LOG.warn("BluePages check error.", e);
        return new PrivatePersonCheckResult(PrivatePersonCheckStatus.BluepagesError);
      }
    } else {
      LOG.debug("No duplicate CMR found.");
      return new PrivatePersonCheckResult(PrivatePersonCheckStatus.Passed);
    }

  }

  /**
   * Generic matching logic to check NAME against private person records
   * 
   * @param name
   * @param issuingCountry
   * @param landedCountry
   * @return CMR No. of the duplicate record
   */

  private DuplicateCMRCheckResponse chkDupPrivatePersonRecordFR(String name, String issuingCountry, String landedCountry, Data data)
      throws Exception {
    MatchingServiceClient client = CmrServicesFactory.getInstance().createClient(SystemConfiguration.getValue("BATCH_SERVICES_URL"),
        MatchingServiceClient.class);
    DuplicateCMRCheckRequest request = new DuplicateCMRCheckRequest();
    request.setCustomerName(name);
    request.setIssuingCountry(issuingCountry);
    request.setLandedCountry(landedCountry);
    request.setIsicCd(AutomationConst.ISIC_PRIVATE_PERSON);
    request.setNameMatch("Y");
    if (SystemLocation.FRANCE.equals(issuingCountry)) {
      switch (data.getCustSubGrp()) {
      case "PRICU":
      case "XBLUM":
        request.setCustClass("60");
        break;
      case "CBIEM":
      case "IBMEM":
        request.setCustClass("71");
        break;
      }
    }
    client.setReadTimeout(1000 * 60 * 5);
    LOG.debug("Connecting to the Duplicate CMR Check Service at " + SystemConfiguration.getValue("BATCH_SERVICES_URL"));
    MatchingResponse<?> rawResponse = client.executeAndWrap(MatchingServiceClient.CMR_SERVICE_ID, request, MatchingResponse.class);
    ObjectMapper mapper = new ObjectMapper();
    String json = mapper.writeValueAsString(rawResponse);

    TypeReference<MatchingResponse<DuplicateCMRCheckResponse>> ref = new TypeReference<MatchingResponse<DuplicateCMRCheckResponse>>() {
    };

    MatchingResponse<DuplicateCMRCheckResponse> response = mapper.readValue(json, ref);

    if (response.getSuccess()) {
      if (response.getMatched() && response.getMatches().size() > 0) {
        return response.getMatches().get(0);
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

  public static boolean isCountryFREnabled(EntityManager entityManager, String cntry) {

    boolean isFR = false;

    String sql = ExternalizedQuery.getSql("FRANCE.GET_SUPP_CNTRY_BY_ID");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("CNTRY", cntry);
    query.setForReadOnly(true);
    List<Integer> records = query.getResults(Integer.class);
    Integer singleObject = null;

    if (records != null && records.size() > 0) {
      singleObject = records.get(0);
      Integer val = singleObject != null ? singleObject : null;

      if (val != null) {
        isFR = true;
      } else {
        isFR = false;
      }
    } else {
      isFR = false;
    }
    return isFR;
  }
}