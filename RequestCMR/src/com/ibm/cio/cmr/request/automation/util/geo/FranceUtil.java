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
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.automation.AutomationElementRegistry;
import com.ibm.cio.cmr.request.automation.AutomationEngineData;
import com.ibm.cio.cmr.request.automation.RequestData;
import com.ibm.cio.cmr.request.automation.impl.gbl.CalculateCoverageElement;
import com.ibm.cio.cmr.request.automation.out.AutomationResult;
import com.ibm.cio.cmr.request.automation.out.FieldResultKey;
import com.ibm.cio.cmr.request.automation.out.OverrideOutput;
import com.ibm.cio.cmr.request.automation.out.ValidationOutput;
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
import com.ibm.cio.cmr.request.util.ConfigUtil;
import com.ibm.cio.cmr.request.util.RequestUtils;
import com.ibm.cio.cmr.request.util.SystemLocation;
import com.ibm.cio.cmr.request.util.dnb.DnBUtil;
import com.ibm.cmr.services.client.dnb.DnBCompany;
import com.ibm.cmr.services.client.matching.MatchingResponse;
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
      CmrConstants.RDC_INSTALL_AT, CmrConstants.RDC_SHIP_TO);
  private static final Logger LOG = Logger.getLogger(FranceUtil.class);
  private static List<FrSboMapping> sortlMappings = new ArrayList<FrSboMapping>();
  private static final String MATCHING = "matching";
  private static final String POSTAL_CD_STARTS = "postalCdStarts";
  private static final String SBO = "sbo";
  private static final List<String> NON_RELEVANT_ADDRESS_FIELDS = Arrays.asList("Att. Person", "Phone #", "Division/Department",
      "Attention to/Building/Floor/Office");
  public static final List<String> FRANCE_SUBREGIONS = Arrays.asList("MC", "GP", "GF", "MQ", "RE", "PM", "KM", "VU", "PF", "YT", "NC", "WF", "AD",
      "DZ");

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
    Admin admin = requestData.getAdmin();
    Addr zs01 = requestData.getAddress("ZS01");
    String customerName = getCustomerFullName(zs01);
    Addr zd01 = requestData.getAddress("ZD01");
    Addr zi01 = requestData.getAddress("ZI01");
    String customerNameZI01 = "";
    if (zi01 != null) {
      customerNameZI01 = getCustomerFullName(zi01);
    }
    Addr zp01 = requestData.getAddress("ZP01");

    String scenario = data.getCustSubGrp();
    if (StringUtils.isNotBlank(scenario)) {
      String scenarioDesc = getScenarioDescription(entityManager, data);
      if (StringUtils.isNotBlank(data.getCountryUse()) && data.getCountryUse().length() > 3 && !SCENARIO_COMMERCIAL.equals(scenario)
          && !SCENARIO_CROSSBORDER_COMMERCIAL.equals(scenario)) {
        engineData.addNegativeCheckStatus("DISABLEDAUTOPROC",
            "Requests for " + scenarioDesc + " cannot be processed automatically. Manual processing would be required.");
      } else {
        switch (scenario) {
        case SCENARIO_CROSSBORDER_PRIVATE_PERSON:
        case SCENARIO_PRIVATE_PERSON:
        case SCENARIO_CROSSBORDER_IBM_EMPLOYEE:
        case SCENARIO_IBM_EMPLOYEE:
          return doPrivatePersonChecks(engineData, SystemLocation.FRANCE, zs01.getLandCntry(), customerName, details,
              (SCENARIO_IBM_EMPLOYEE.equals(scenario) || SCENARIO_IBM_EMPLOYEE.equals(scenario)), requestData);

        case SCENARIO_INTERNAL:
        case SCENARIO_CROSSBORDER_INTERNAL:
          for (String addrType : RELEVANT_ADDRESSES) {
            List<Addr> addresses = requestData.getAddresses(addrType);
            for (Addr addr : addresses) {
              String custNmTrimmed = getCustomerFullName(addr);
              if (!(custNmTrimmed.toUpperCase().contains("IBM") || custNmTrimmed.toUpperCase().contains("International Business Machines"))) {
                details.append("Wrong Customer Name on the main address. IBM should be part of the name.").append("\n");
                engineData.addRejectionComment("OTH", "Wrong Customer Name on the main address. IBM should be part of the name.", "", "");
                return false;
              }
            }
          }
          break;
        case SCENARIO_CROSSBORDER_COMMERCIAL:
        case SCENARIO_COMMERCIAL:
          if ((!customerName.toUpperCase().equals(customerNameZI01.toUpperCase())) && StringUtils.isNotBlank(customerName)
              && StringUtils.isNotBlank(customerNameZI01)) {
            details.append("Sold-to and Installing name are not same. Request will require CMDE review before proceeding.").append("\n");
            engineData.addNegativeCheckStatus("SOLDTO_INSTALL_DIFF", "Sold-to and Installing addresses are not same.");
            return false;
          }
          if (zs01 != null) {
            // remove duplicate address
            removeDuplicateAddresses(entityManager, requestData, details);
          }
          break;
        case SCENARIO_HOSTING:
        case SCENARIO_CROSSBORDER_HOSTING:
          if (StringUtils.isNotBlank(customerNameZI01) && StringUtils.isNotBlank(customerName)
              && customerName.toUpperCase().equals(customerNameZI01.toUpperCase())) {
            details.append("Customer Names on Sold-to and Install-at address should be different for Hosting Scenario").append("\n");
            engineData.addRejectionComment("OTH", "Customer Names on Sold-to and Install-at address should be different for Hosting Scenario", "",
                "");
            return false;
          }
          break;
        case SCENARIO_BUSINESS_PARTNER:
        case SCENARIO_CROSSBORDER_BUSINESS_PARTNER:
          return doBusinessPartnerChecks(engineData, data.getPpsceid(), details);
        case SCENARIO_CROSSBORDER_THIRD_PARTY:
        case SCENARIO_THIRD_PARTY:
          if (customerName.toUpperCase().equals(customerNameZI01.toUpperCase()) && StringUtils.isNotBlank(customerName)
              && StringUtils.isNotBlank(customerNameZI01)) {
            details.append("Customer Names on Sold-to and Install-at address should be different for Third Party Scenario").append("\n");
            engineData.addRejectionComment("OTH", "Customer Names on Sold-to and Install-at address should be different for Third Party Scenario", "",
                "");
            return false;
          }
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
    details.append("\n");
    if (isCoverageCalculated && StringUtils.isNotBlank(coverageId) && CalculateCoverageElement.COV_BG.equals(covFrom)) {
      engineData.addPositiveCheckStatus(AutomationEngineData.COVERAGE_CALCULATED);
    } else if (!FRANCE_SUBREGIONS.contains(addr.getLandCntry())) {
      isCoverageCalculated = false;
      // if not calculated using bg/gbg try calculation using SIREN
      details.setLength(0);// clear string builder
      overrides.clearOverrides(); // clear existing overrides
      details.append("Calculating Coverage using SIREN.").append("\n\n");
      String siren = StringUtils.isNotBlank(data.getTaxCd1()) ? (data.getTaxCd1().length() > 9 ? data.getTaxCd1().substring(0, 9) : data.getTaxCd1())
          : "";
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
            overrides.addOverride(AutomationElementRegistry.GBL_CALC_COV, "DATA", "INSTALL_BRANCH_OFF", data.getInstallBranchOff(), sboValue);
          } else {
            sboValue = getSBOfromCoverage(entityManager, coverage.getFinalCoverage());
            if (StringUtils.isNotBlank(sboValue)) {
              details.append("SORTL calculated on basis of Existing CMR Data: " + sboValue);
              overrides.addOverride(AutomationElementRegistry.GBL_CALC_COV, "DATA", "INSTALL_BRANCH_OFF", data.getInstallBranchOff(), sboValue);
              overrides.addOverride(AutomationElementRegistry.GBL_CALC_COV, "DATA", "SALES_BO_CD", data.getSalesBusOffCd(), sboValue);
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

      if (!isCoverageCalculated) {
        // if not calculated using siren as well
        if ("32".equals(data.getIsuCd()) && "S".equals(data.getClientTier())) {
          details.setLength(0);
          details.append("Calculating coverage using 32S-PostalCode logic.").append("\n");
          if (SCENARIO_INTERNAL_SO.equals(scenario) || SCENARIO_THIRD_PARTY.equals(scenario) || SCENARIO_CROSSBORDER_INTERNAL_SO.equals(scenario)
              || SCENARIO_CROSSBORDER_THIRD_PARTY.equals(scenario)) {
            addr = requestData.getAddress("ZI01");
          }
          HashMap<String, String> response = getSBOFromPostalCodeMapping(data.getCountryUse(), data.getIsicCd(), addr.getPostCd(), data.getIsuCd(),
              data.getClientTier());
          LOG.debug("Calculated SBO: " + response.get(SBO));
          if (StringUtils.isNotBlank(response.get(MATCHING))) {
            switch (response.get(MATCHING)) {
            case "Exact Match":
              overrides.addOverride(AutomationElementRegistry.GBL_CALC_COV, "DATA", "SALES_BO_CD", data.getSalesBusOffCd(), response.get(SBO));
              details.append("Coverage calculation Successful.").append("\n");
              details.append("Computed SBO = " + response.get(SBO)).append("\n\n");
              details.append("Matched Rule:").append("\n");
              details.append("ISIC = " + data.getIsicCd()).append("\n");
              details.append("ISU = " + data.getIsuCd()).append("\n");
              details.append("CTC = " + data.getClientTier()).append("\n");
              details.append("Postal Code Starts = " + response.get(POSTAL_CD_STARTS)).append("\n\n");
              details.append("Matching: " + response.get(MATCHING));
              results.setResults("Coverage Calculated");
              engineData.addPositiveCheckStatus(AutomationEngineData.COVERAGE_CALCULATED);
              break;
            case "No Match Found":
              details.append("Coverage cannot be computed using 32S-PostalCode logic.").append("\n");
              results.setResults("Coverage not calculated.");
              break;
            }
          } else {
            details.append("Coverage cannot be computed using 32S-PostalCode logic.").append("\n");
            results.setResults("Coverage not calculated.");
          }
        } else {
          // if isu ctc is not 32S and coverage is not calculated (needs
          // review... whether to set on error true or set skip results here)
          details.setLength(0);
          overrides.clearOverrides();
          details.append("Coverage could not be calculated through Buying Group or SIREN.\n Skipping coverage calculation.").append("\n");
          results.setResults("Skipped");
        }
      }
    }
    return true;
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

  private HashMap<String, String> getSBOFromPostalCodeMapping(String countryUse, String isicCd, String postCd, String isuCd, String clientTier) {
    HashMap<String, String> response = new HashMap<String, String>();
    response.put(MATCHING, "");
    response.put(POSTAL_CD_STARTS, "");
    response.put(SBO, "");
    if (!sortlMappings.isEmpty()) {
      for (FrSboMapping mapping : sortlMappings) {
        List<String> isicCds = new ArrayList<String>();
        if (mapping.getIsicCds() != null && !mapping.getIsicCds().isEmpty()) {
          isicCds = Arrays.asList(mapping.getIsicCds().replaceAll("\n", "").replaceAll(" ", "").split(","));
        }
        if (countryUse.equals(mapping.getCountryUse()) && (isicCds.isEmpty() || (!isicCds.isEmpty() && isicCds.contains(isicCd)))
            && isuCd.equals(mapping.getIsu()) && clientTier.equals(mapping.getCtc())) {
          if (StringUtils.isNotBlank(mapping.getPostalCdStarts())) {
            String[] postalCodeRanges = mapping.getPostalCdStarts().replaceAll("\n", "").replaceAll(" ", "").split(",");
            for (String postalCdRange : postalCodeRanges) {
              if (postCd.startsWith(postalCdRange)) {
                response.put(MATCHING, "Exact Match");
                response.put(SBO, mapping.getSbo());
                response.put(POSTAL_CD_STARTS, postalCdRange);
                return response;
              }
            }
          }
        } else {
          response.put(MATCHING, "Exact Match");
          response.put(SBO, mapping.getSbo());
          response.put(POSTAL_CD_STARTS, "- No Postal Code Range Defined -");
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
    String[] scenariosToBeChecked = { "COMME", "CBMME", "GOVRN", "CBVRN", "BPIEU", "CBIEU" };
    String scenario = requestData.getData().getCustSubGrp();
    String[] kuklaComme = { "11" };
    String[] kuklaGovrn = { "13", "14", "17" };
    String[] kuklaBuspr = { "42", "43", "45", "46", "47", "48" };

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
          } else if (Arrays.asList(kuklaBuspr).contains(kukla) && ("BPIEU".equals(scenario) || "CBIEU".equals(scenario))) {
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
      case "VAT #":
        if ((StringUtils.isBlank(change.getOldData()) && !StringUtils.isBlank(change.getNewData())) || (!StringUtils.isBlank(change.getOldData())
            && !StringUtils.isBlank(change.getNewData()) && !(change.getOldData().equals(change.getNewData())))) {
          // ADD and Update
          List<DnBMatchingResponse> matches = getMatches(requestData, engineData, soldTo, true);
          boolean matchesDnb = false;
          if (matches != null) {
            // check against D&B
            matchesDnb = ifaddressCloselyMatchesDnb(matches, soldTo, admin, data.getCmrIssuingCntry());
          }
          if (!matchesDnb) {
            cmdeReview = true;
            engineData.addNegativeCheckStatus("_esVATCheckFailed", "VAT # on the request did not match D&B");
            details.append("VAT # on the request did not match D&B\n");
          } else {
            details.append("VAT # on the request matches D&B\n");
          }
        }
        if (!StringUtils.isBlank(change.getOldData()) && (StringUtils.isBlank(change.getNewData()))) {
          // noop, for switch handling only
        }
        break;
      case "ISU Code":
      case "Client Tier":
      case "Search Term/Sales Branch Office":
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
            if (CmrConstants.RDC_SHIP_TO.equals(addrType)) {
              if (addressExists(entityManager, addr)) {
                LOG.debug(" - Duplicates found for " + addrType + "(" + addr.getId().getAddrSeq() + ")");
                checkDetails.append("Address " + addrType + "(" + addr.getId().getAddrSeq() + ") provided matches an existing address.\n");
                resultCodes.add("R");
              } else {
                LOG.debug("Addition of " + addrType + "(" + addr.getId().getAddrSeq() + ")");
                checkDetails.append("Addition of new address (" + addr.getId().getAddrSeq() + ") address skipped in the checks.\n");
              }
            }
            if (CmrConstants.RDC_INSTALL_AT.equals(addrType)) {
              String addrName = addr.getCustNm1() + (StringUtils.isNotBlank(addr.getCustNm2()) ? addr.getCustNm2() : "");
              String soldToName = "";
              Addr zs01 = requestData.getAddress("ZS01");
              if (zs01 != null) {
                soldToName = zs01.getCustNm1() + (StringUtils.isNotBlank(zs01.getCustNm2()) ? zs01.getCustNm2() : "");
              }

              if (addrName.equals(soldToName)) {
                if (addressExists(entityManager, addr)) {
                  LOG.debug(" - Duplicates found for " + addrType + "(" + addr.getId().getAddrSeq() + ")");
                  checkDetails.append("Address " + addrType + "(" + addr.getId().getAddrSeq() + ") provided matches an existing address.\n");
                  resultCodes.add("R");
                } else {
                  LOG.debug("Addition of " + addrType + "(" + addr.getId().getAddrSeq() + ")");
                  checkDetails.append("Addition of new address (" + addr.getId().getAddrSeq() + ") validated.\n");
                }
              } else {
                LOG.debug("New address " + addrType + "(" + addr.getId().getAddrSeq() + ") needs to be verified");
                checkDetails.append("New address " + addrType + "(" + addr.getId().getAddrSeq() + ") has different customer name than sold-to.\n");
                resultCodes.add("D");
              }
            }
            if (CmrConstants.RDC_BILL_TO.equals(addrType)) {

              LOG.debug("New address " + addrType + "(" + addr.getId().getAddrSeq() + ") needs to be verified");
              checkDetails.append("New address " + addrType + "(" + addr.getId().getAddrSeq() + ") needs to be verified \n");
              resultCodes.add("D");

            }
          } else if ("Y".equals(addr.getChangedIndc())) {
            // update address
            if (isRelevantAddressFieldUpdated(changes, addr)) {
              if (CmrConstants.RDC_INSTALL_AT.equals(addrType)) {
                if (null == changes.getAddressChange(addrType, "Customer legal name")
                    && null == changes.getAddressChange(addrType, "Legal name continued")) {
                  LOG.debug("Update to InstallAt " + addrType + "(" + addr.getId().getAddrSeq() + ")");
                  checkDetails.append("Update to InstallAt (" + addr.getId().getAddrSeq() + ") skipped in the checks.\n");
                } else {
                  resultCodes.add("D");
                  checkDetails.append("Update to InstallAt (" + addr.getId().getAddrSeq() + ") has different customer name than sold-to .\n");
                }
              } else if (CmrConstants.RDC_SOLD_TO.equals(addrType) || CmrConstants.RDC_BILL_TO.equals(addrType)) {
                LOG.debug("Update to Address " + addrType + "(" + addr.getId().getAddrSeq() + ") needs to be verified");
                checkDetails.append("Update to address " + addrType + "(" + addr.getId().getAddrSeq() + ") needs to be verified \n");
                resultCodes.add("D");
              } else {
                // proceed
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
      engineData.addRejectionComment("_atRejectAddr", "Add or update on the address is rejected", "", "");
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

}