package com.ibm.cio.cmr.request.automation.util.geo;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.commons.digester.Digester;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import com.ibm.cio.cmr.request.automation.AutomationElementRegistry;
import com.ibm.cio.cmr.request.automation.AutomationEngineData;
import com.ibm.cio.cmr.request.automation.RequestData;
import com.ibm.cio.cmr.request.automation.impl.gbl.CalculateCoverageElement;
import com.ibm.cio.cmr.request.automation.impl.gbl.DupCMRCheckElement;
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
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.util.BluePagesHelper;
import com.ibm.cio.cmr.request.util.Person;
import com.ibm.cio.cmr.request.util.SystemParameters;
import com.ibm.cmr.services.client.CmrServicesFactory;
import com.ibm.cmr.services.client.MatchingServiceClient;
import com.ibm.cmr.services.client.PPSServiceClient;
import com.ibm.cmr.services.client.ServiceClient.Method;
import com.ibm.cmr.services.client.matching.MatchingResponse;
import com.ibm.cmr.services.client.matching.cmr.DuplicateCMRCheckRequest;
import com.ibm.cmr.services.client.matching.cmr.DuplicateCMRCheckResponse;
import com.ibm.cmr.services.client.matching.dnb.DnBMatchingResponse;
import com.ibm.cmr.services.client.matching.gbg.GBGFinderRequest;
import com.ibm.cmr.services.client.pps.PPSRequest;
import com.ibm.cmr.services.client.pps.PPSResponse;

public class FranceUtil extends AutomationUtil {

  private static final Logger LOG = Logger.getLogger(FranceUtil.class);
  private static List<FrSboMapping> sortlMappings = new ArrayList<FrSboMapping>();
  private static final String MATCHING = "matching";
  private static final String POSTAL_CD_STARTS = "postalCdStarts";
  private static final String SBO = "sbo";

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
        ClassLoader loader = FranceUtil.class.getClassLoader();
        InputStream is = loader.getResourceAsStream("fr-sbo-mapping.xml");
        FranceUtil.sortlMappings = (ArrayList<FrSboMapping>) digester.parse(is);
      } catch (Exception e) {
        LOG.error("Error occured while digesting xml.", e);
      }
    }
  }

  @Override
  public AutomationResult<OverrideOutput> doCountryFieldComputations(EntityManager entityManager, AutomationResult<OverrideOutput> results,
      StringBuilder details, OverrideOutput overrides, RequestData requestData, AutomationEngineData engineData) throws Exception {
    Data data = requestData.getData();
    String subRegion = data.getCountryUse();
    Addr zs01 = requestData.getAddress("ZS01");

    String sBo = getSBOFromMapping(data.getClientTier(), zs01.getPostCd(), data.getIsuCd(), data.getClientTier(), subRegion);
    LOG.debug("Calculated SBO: " + sBo);
    if (StringUtils.isNotBlank(sBo)) {
      overrides.addOverride(AutomationElementRegistry.GBL_FIELD_COMPUTE, "DATA", "SALES_BO_CD", data.getSalesBusOffCd(), sBo);
      overrides.addOverride(AutomationElementRegistry.GBL_FIELD_COMPUTE, "DATA", "INSTALL_BRANCH_OFF", data.getInstallBranchOff(), sBo);
      details.append("Calculated value for SBO: " + sBo).append("\n");
    } else {
      boolean computed = false;
      // check if we need to get SBO from Coverage
      String coverage = (String) engineData.get(AutomationEngineData.COVERAGE_CALCULATED);
      if (!StringUtils.isBlank(coverage)) {
        String sortlSbo = getSBOfromCoverage(entityManager, coverage);
        if (sortlSbo != null) {
          overrides.addOverride(AutomationElementRegistry.GBL_FIELD_COMPUTE, "DATA", "SALES_BO_CD", data.getSalesBusOffCd(), sortlSbo);
          overrides.addOverride(AutomationElementRegistry.GBL_FIELD_COMPUTE, "DATA", "INSTALL_BRANCH_OFF", data.getInstallBranchOff(), sortlSbo);
          computed = true;
          details.append("Calculated value for SBO via Coverage " + coverage + ": " + sBo).append("\n");
        }
      }

      if (!computed) {
        if (StringUtils.isNotBlank(data.getSalesBusOffCd()) && StringUtils.isNotBlank(data.getInstallBranchOff())) {
          details.append("SBO value already provided on the request - " + data.getSalesBusOffCd()).append("\n");
          results.setResults("Skipped");
        } else {
          engineData.addRejectionComment("SBO cannot be computed automatically.");
          details.append("SBO cannot be computed automatically.").append("\n");
          results.setResults("SBO not calculated.");
          results.setOnError(true);
        }
      }
    }
    results.setProcessOutput(overrides);
    return results;
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
    String countryUse = data.getCountryUse();
    Addr zs01 = requestData.getAddress("ZS01");
    boolean valid = true;
    String scenario = data.getCustSubGrp();
    String scenarioDesc = getScenarioDescription(entityManager, data);

    if (StringUtils.isNotBlank(scenario)) {
      switch (scenario) {
      case "PRICU":
      case "CBICU":
      case "IBMEM":
      case "CBIEM":

        String name = zs01.getCustNm1() + (StringUtils.isNotBlank(zs01.getCustNm2()) ? " " + zs01.getCustNm2() : "");
        String duplicateCMRNo = null;
        // getting fuzzy matches on basis of name
        try {
          MatchingServiceClient client = CmrServicesFactory.getInstance().createClient(SystemConfiguration.getValue("BATCH_SERVICES_URL"),
              MatchingServiceClient.class);
          DuplicateCMRCheckRequest request = new DuplicateCMRCheckRequest();
          request.setCustomerName(name);
          request.setIssuingCountry(data.getCmrIssuingCntry());
          request.setLandedCountry(zs01.getLandCntry());
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
              duplicateCMRNo = response.getMatches().get(0).getCmrNo();
              details.append("The " + ((scenario.equals("PRICU") || scenario.equals("CBICU")) ? "Private Customer" : "IBM Employee")
                  + " already has a record with CMR No. " + duplicateCMRNo);
              engineData.addRejectionComment("The " + ((scenario.equals("PRICU") || scenario.equals("CBICU")) ? "Private Customer" : "IBM Employee")
                  + " already has a record with CMR No. " + duplicateCMRNo);
              valid = false;
            } else {
              details.append("No Duplicate CMRs were found with Name: " + name);
            }
            if (StringUtils.isBlank(duplicateCMRNo) && (scenario.equals("IBMEM") || scenario.equals("CBIEM"))) {
              Person person = null;
              try {
                person = BluePagesHelper
                    .getPersonByName(zs01.getCustNm1() + (StringUtils.isNotBlank(zs01.getCustNm2()) ? " " + zs01.getCustNm2() : ""));
                if (person == null) {
                  engineData.addRejectionComment("Employee details not found in IBM BluePages.");
                  details.append("Employee details not found in IBM BluePages.").append("\n");
                } else {
                  details.append("Employee details validated with IBM BluePages for " + person.getName() + "(" + person.getEmail() + ").")
                      .append("\n");
                }
              } catch (Exception e) {
                LOG.error("Not able to check name against bluepages", e);
                engineData.addNegativeCheckStatus("BLUEPAGES_NOT_VALIDATED", "Not able to check name against bluepages for scenario IBM Employee.");
              }
            }
          }
        } catch (Exception e) {
          details.append("Duplicate CMR check using customer name match failed to execute.");
          engineData.addNegativeCheckStatus("DUPLICATE_CHECK_ERROR", "Duplicate CMR check using customer name match failed to execute.");
        }

        // For sub_regions of France for this scenario, requests should go the
        // CMDE
        if (countryUse.length() > 3) {
          engineData.addNegativeCheckStatus("DISABLEDAUTOPROC",
              "For scenario " + scenarioDesc + " the automated processing should be off - so at all times, the request  goes to CMDE queue.");
        }

        break;
      case "CBOEM":
      case "LCOEM":
        // check duplicate CMR's manually
        MatchingResponse<DuplicateCMRCheckResponse> response = null;
        try {
          DupCMRCheckElement cmrCheckElement = new DupCMRCheckElement(null, null, false, false);
          response = cmrCheckElement.getMatches(entityManager, requestData, engineData);
          // get a count of matches with match grade E1,E2, F1 or F2
          int count = 0;
          if (response != null && response.getSuccess()) {
            if (response.getMatched()) {
              LOG.debug("Duplicate CMR's found for request: " + data.getId().getReqId());
              for (DuplicateCMRCheckResponse cmrResponse : response.getMatches()) {
                if (cmrResponse.getMatchGrade().equals("E1") || cmrResponse.getMatchGrade().equals("E2") || cmrResponse.getMatchGrade().equals("F1")
                    || cmrResponse.getMatchGrade().equals("F2")) {
                  count++;
                }
              }
            }
          } else {
            LOG.error("Unable to perform Duplicate CMR Check for BROKR scenario.");
            details.append("Unable to perform Duplicate CMR Check for Broker scenario.");
            engineData.addNegativeCheckStatus("CMR_CHECK_FAILED", "Unable to perform Duplicate CMR Check for Broker scenario.");
          }
          if (count > 1) {
            engineData.addRejectionComment("Multiple registered CMRs already found for this customer.");
            details.append("Multiple registered CMRs already found for this customer.");
            valid = false;
          } else if (count == 1) {
            details.append("Single registered CMR found for this customer.");
          } else {
            details.append("No registered CMRs found for this customer.");
          }
        } catch (Exception e) {
          LOG.error("Unable to perform Duplicate CMR Check for BROKR scenario.", e);
          details.append("Unable to perform Duplicate CMR Check for Broker scenario.");
          engineData.addNegativeCheckStatus("CMR_CHECK_FAILED", "Unable to perform Duplicate CMR Check for Broker scenario.");
        }

        // For sub_regions of France for this scenario, requests should go the
        // CMDE
        if (countryUse.length() > 3) {
          engineData.addNegativeCheckStatus("DISABLEDAUTOPROC",
              "For scenario " + scenarioDesc + " the automated processing should be off - so at all times, the request  goes to CMDE queue.");
        }

        break;
      case "CBIEU":
      case "CBUEU":
      case "BPIEU":
      case "BPUEU":
        if (StringUtils.isNotBlank(data.getPpsceid())) {
          try {
            PPSServiceClient client = CmrServicesFactory.getInstance().createClient(SystemConfiguration.getValue("BATCH_SERVICES_URL"),
                PPSServiceClient.class);
            client.setRequestMethod(Method.Get);
            client.setReadTimeout(1000 * 60 * 5);
            PPSRequest request = new PPSRequest();
            request.setCeid(data.getPpsceid());
            PPSResponse ppsResponse = client.executeAndWrap(request, PPSResponse.class);
            if (!ppsResponse.isSuccess() || ppsResponse.getProfiles().size() == 0) {
              engineData.addRejectionComment("PPS CE ID on the request is invalid.");
              details.append("PPS CE ID on the request is invalid.");
              valid = false;
            } else {
              details.append("PPS CE ID validated successfully with PartnerWorld Profile Systems.");
            }
          } catch (Exception e) {
            LOG.error("Not able to validate PPS CE ID using PPS Service.", e);
            details.append("Not able to validate PPS CE ID using PPS Service.");
            engineData.addNegativeCheckStatus("PPSCEID", "Not able to validate PPS CE ID using PPS Service.");
          }
        } else {
          details.append("PPS CE ID not available on the request.");
          engineData.addNegativeCheckStatus("PPSCEID", "PPS CE ID not available on the request.");
        }

        // For France as well as sub-regions for this scenario, requests should
        // go the CMDE
        engineData.addNegativeCheckStatus("DISABLEDAUTOPROC",
            "For scenario " + scenarioDesc + " the automated processing should be off - so at all times, the request  goes to CMDE queue.");

        break;
      case "INTER":
      case "CBTER":
      case "LCIFF":
      case "LCIFL":
      case "CBIFF":
      case "CBIFL":
        String mainCustNm = zs01.getCustNm1();
        if (StringUtils.isNotBlank(mainCustNm) && !mainCustNm.toUpperCase().contains("IBM")) {
          engineData.addRejectionComment("Wrong Customer Name on the main address. IBM should be part of the name.");
          details.append("Wrong Customer Name on the main address. IBM should be part of the name.").append("\n");
          valid = false;
        }

        // For sub_regions of France for this scenario, requests should go the
        // CMDE
        if (countryUse.length() > 3) {
          engineData.addNegativeCheckStatus("DISABLEDAUTOPROC",
              "For scenario " + scenarioDesc + " the automated processing should be off - so at all times, the request  goes to CMDE queue.");
        }

        break;

      case "HOSTC":
      case "CBSTC":
        String custNm1 = zs01.getCustNm1();
        String custNm2 = zs01.getCustNm2();
        String custNm = custNm1 + (StringUtils.isNotBlank(custNm2) ? " " + custNm2 : "");
        if (StringUtils.isNotBlank(custNm) && custNm.toUpperCase().contains("CHEZ")) {
          valid = true;
        } else {
          engineData.addRejectionComment("Wrong Customer Name on Host address. CHEZ should be part of the name.");
          details.append("Wrong Customer Name on Host address. CHEZ should be part of the name.").append("\n");
          valid = false;
        }

        // For sub_regions of France for this scenario, requests should go the
        // CMDE
        if (countryUse.length() > 3) {
          engineData.addNegativeCheckStatus("DISABLEDAUTOPROC",
              "For scenario " + scenarioDesc + " the automated processing should be off - so at all times, the request  goes to CMDE queue.");
        }

        break;

      case "CHDPT":
      case "THDPT":
        // zs02 -> mailing zp01 -> billing
        Addr mailing = null;
        Addr billing = null;
        for (Addr addr : requestData.getAddresses()) {
          if ("ZS02".equalsIgnoreCase(addr.getId().getAddrType())) {
            mailing = addr;
          }
          if ("ZP01".equalsIgnoreCase(addr.getId().getAddrType())) {
            billing = addr;
          }
        }
        if (mailing != null && billing != null) {
          String mailDetails = (StringUtils.isNotBlank(mailing.getCustNm1()) ? mailing.getCustNm1() : "")
              + (StringUtils.isNotBlank(mailing.getAddrTxt()) ? mailing.getAddrTxt() : "")
              + (StringUtils.isNotBlank(mailing.getCity1()) ? mailing.getCity1() : "");
          String billDetails = (StringUtils.isNotBlank(billing.getCustNm1()) ? billing.getCustNm1() : "")
              + (StringUtils.isNotBlank(billing.getAddrTxt()) ? billing.getAddrTxt() : "")
              + (StringUtils.isNotBlank(billing.getCity1()) ? billing.getCity1() : "");

          if (mailDetails.equalsIgnoreCase(billDetails)) {
            valid = true;
          } else {
            engineData.addRejectionComment("Invalid Billing/Mailing address found on the request. The addresses should be the same.");
            details.append("Invalid Billing/Mailing address found on the request. The addresses should be the same.").append("\n");
            valid = false;
          }
        }

        // For sub_regions of France for this scenario, requests should go the
        // CMDE
        if (countryUse.length() > 3) {
          engineData.addNegativeCheckStatus("DISABLEDAUTOPROC",
              "For scenario " + scenarioDesc + " the automated processing should be off - so at all times, the request  goes to CMDE queue.");
        }

        break;
      case "INTSO":
      case "CBTSO":
        engineData.addNegativeCheckStatus("DISABLEDAUTOPROC",
            "For scenario " + scenarioDesc + " the automated processing should be off - so at all times, the request  goes to CMDE queue.");

      }
    } else {
      if (StringUtils.isBlank(scenario)) {
        valid = false;
        engineData.addRejectionComment("No Scenario found on the request");
        details.append("No Scenario found on the request");
      }
    }
    return valid;
  }

  private String getScenarioDescription(EntityManager entityManager, Data data) {

    String sql = ExternalizedQuery.getSql("GET_SCENARIO_DESC_FR");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("CUST_SUB_TYP_VAL", data.getCustSubGrp());
    query.setParameter("CUST_TYP_VAL", data.getCustGrp());
    query.setForReadOnly(true);
    return query.getSingleResult(String.class);
  }

  private String getSBOFromMapping(String isicCd, String postCd, String isuCd, String clientTier, String subRegion) {
    if (!sortlMappings.isEmpty()) {
      for (FrSboMapping mapping : sortlMappings) {
        LOG.debug("Mapping -> " + mapping);
        if (StringUtils.isNotBlank(mapping.getIsicCds())) {
          List<String> isicCds = Arrays.asList(mapping.getIsicCds().replaceAll("\n", "").replaceAll(" ", "").split(","));
          LOG.debug("isicCds -> " + isicCds);
          if (isicCds.contains(isicCd) && isuCd.equals(mapping.getIsu()) && clientTier.equals(mapping.getCtc())
              && subRegion.equalsIgnoreCase(mapping.getCountryUse())) {
            if (StringUtils.isNotBlank(mapping.getPostalCdStarts())) {
              String[] postalCodeStarts = mapping.getPostalCdStarts().replaceAll("\n", "").replaceAll(" ", "").split(",");
              for (String p : postalCodeStarts) {
                if (postCd.startsWith(p, 0)) {
                  return mapping.getSbo();
                }
              }
            } else {
              return mapping.getSbo();
            }
          }
        } else {

          if (isuCd.equals(mapping.getIsu()) && clientTier.equals(mapping.getCtc()) && subRegion.equalsIgnoreCase(mapping.getCountryUse())) {
            if (StringUtils.isNotBlank(mapping.getPostalCdStarts())) {
              String[] postalCodeStarts = mapping.getPostalCdStarts().replaceAll("\n", "").replaceAll(" ", "").split(",");
              for (String p : postalCodeStarts) {
                if (postCd.startsWith(p, 0)) {
                  return mapping.getSbo();
                }
              }
            } else {
              return mapping.getSbo();
            }
          }

        }

      }
      return null;
    } else {
      return null;
    }
  }

  @Override
  public void tweakGBGFinderRequest(EntityManager entityManager, GBGFinderRequest request, RequestData requestData) {
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
    Data data = requestData.getData();
    String coverageId = container.getFinalCoverage();
    Addr zs01 = requestData.getAddress("ZS01");
    details.append("\n");
    if (!isCoverageCalculated
        || (isCoverageCalculated && !(CalculateCoverageElement.BG_CALC.equals(covFrom) || CalculateCoverageElement.BG_ODM.equals(covFrom)))) {
      details.setLength(0);// clear string builder
      details.append("\nCalculating Coverage using SIREN.").append("\n\n");
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
          covElement.logCoverage(entityManager, engineData, requestData, null, details, overrides, null, coverage.getFinalCoverage(), "Final",
              coverage.getFinalCoverageRules(), data.getCmrIssuingCntry(), container);
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
          results.setResults("Coverage Calculated");
          engineData.addPositiveCheckStatus(AutomationEngineData.COVERAGE_CALCULATED);
          engineData.put(AutomationEngineData.COVERAGE_CALCULATED, coverage.getFinalCoverage());
        } else {
          details.append("Coverage could not be calculated on the basis of SIREN").append("\n");
          results.setResults("Review needed");
          engineData.addNegativeCheckStatus("COVERAGE_ERROR", "Coverage could not be calculated on the basis of SIREN");
        }
      } else {
        details.append("SIREN/SIRET not found on the request.").append("\n");
        results.setResults("SIREN not found");
        engineData.addNegativeCheckStatus("SIREN_NOT_FOUND", "SIREN/SIRET not found on the request.");
      }
    } else if (isCoverageCalculated && StringUtils.isNotBlank(coverageId) && covFrom != null
        && (CalculateCoverageElement.BG_CALC.equals(covFrom) || CalculateCoverageElement.BG_ODM.equals(engineData.get(covFrom)))) {
      details.append("\nISU Code supplied on request = " + data.getIsuCd()).append("\n");
      details.append("Client Tier supplied on request = " + data.getClientTier()).append("\n");
      String isuCd = container.getIsuCd();
      String clientTier = container.getClientTierCd();
      if (StringUtils.isNotBlank(isuCd) && StringUtils.isNotBlank(clientTier)) {
      }

      engineData.addPositiveCheckStatus(AutomationEngineData.COVERAGE_CALCULATED);
    } else if ("32".equals(data.getIsuCd()) && "S".equals(data.getClientTier())) {
      details.append("Calculating coverage using 32S-PostalCode logic.").append("\n");
      HashMap<String, String> response = getSBOFromPostalCodeMapping(data.getCountryUse(), data.getIsicCd(), zs01.getPostCd(), data.getIsuCd(),
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
          engineData.addPositiveCheckStatus(AutomationEngineData.COVERAGE_CALCULATED);
          break;
        case "No Match Found":
          engineData.addRejectionComment("Coverage cannot be computed automatically.");
          details.append("Coverage cannot be computed automatically.").append("\n");
          results.setResults("Coverage not calculated.");
          results.setOnError(true);
          break;
        }
      } else {
        engineData.addRejectionComment("Coverage cannot be computed automatically.");
        details.append("Coverage cannot be computed automatically.").append("\n");
        results.setResults("Coverage not calculated.");
        results.setOnError(true);
      }
    } else {
      details.append("Skipped coverage calculation from 32S-PostalCode logic.").append("\n");
      results.setResults("Coverage calculation skipped.");
    }

    return true;
  }

  private HashMap<String, String> getSBOFromPostalCodeMapping(String countryUse, String isicCd, String postCd, String isuCd, String clientTier) {
    HashMap<String, String> response = new HashMap<String, String>();
    response.put(MATCHING, "");
    response.put(POSTAL_CD_STARTS, "");
    response.put(SBO, "");
    if (!sortlMappings.isEmpty()) {
      for (FrSboMapping mapping : sortlMappings) {
        List<String> isicCds = Arrays.asList(mapping.getIsicCds().replaceAll("\n", "").replaceAll(" ", "").split(","));
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

  @Override
  public boolean runUpdateChecksForData(EntityManager entityManager, AutomationEngineData engineData, RequestData requestData,
      RequestChangeContainer changes, AutomationResult<ValidationOutput> output, ValidationOutput validation) throws Exception {
    Admin admin = requestData.getAdmin();

    Addr soldTo = requestData.getAddress("ZS01");
    StringBuilder detail = new StringBuilder();
    boolean isNegativeCheckNeedeed = false;
    LOG.debug("Changes are -> " + changes);

    if (changes != null && changes.hasDataChanges()) {
      LOG.debug("Changes has data changes -> " + changes.hasDataChanges());
      if (changes.isDataChanged("VAT")) {
        LOG.debug("Changes has VAT changes -> " + changes.isDataChanged("VAT"));
        UpdatedDataModel vatChange = changes.getDataChange("VAT");
        if (vatChange != null) {
          if (StringUtils.isBlank(vatChange.getOldData()) && StringUtils.isNotBlank(vatChange.getNewData())) {
            // check if the name + VAT exists in D&B
            List<DnBMatchingResponse> matches = getMatches(requestData, engineData, soldTo);
            if (!matches.isEmpty()) {
              for (DnBMatchingResponse dnbRecord : matches) {
                if ("Y".equals(dnbRecord.getOrgIdMatch())) {
                  isNegativeCheckNeedeed = false;
                  break;
                }
                isNegativeCheckNeedeed = true;
              }
            }
            if (isNegativeCheckNeedeed) {
              validation.setSuccess(false);
              validation.setMessage("Not validated");
              detail.append("Updates to VAT need verification as it does'nt match DnB");
              engineData.addNegativeCheckStatus("UPDT_REVIEW_NEEDED", "Updated elements cannot be checked automatically.");
              LOG.debug("Updates to VAT need verification as it does not match DnB");
            }

          }
        }
      }

      if (changes.isDataChanged("CollectionCd")) {
        UpdatedDataModel collCdChange = changes.getDataChange("CollectionCd");
        if (collCdChange != null) {
          if (!"AR".equalsIgnoreCase(admin.getRequestingLob())) {
            isNegativeCheckNeedeed = true;
          }

          if (isNegativeCheckNeedeed) {
            validation.setSuccess(false);
            validation.setMessage("Not validated");
            detail.append("Updates to Collection Code need verification.");
            engineData.addNegativeCheckStatus("UPDT_REVIEW_NEEDED", "Updated elements cannot be checked automatically.");
            LOG.debug("Updates to Collection Code need verification.");
          }

        }
      }

      if (changes.isDataChanged("CommercialFinanced")) {
        UpdatedDataModel commFinanceChange = changes.getDataChange("CommercialFinanced");
        if (commFinanceChange != null) {
          String designatedUser = SystemParameters.getString("TOP_LST_SPECI_USER");
          isNegativeCheckNeedeed = admin.getRequesterId().equalsIgnoreCase(designatedUser) ? false : true;
          if (isNegativeCheckNeedeed) {
            validation.setSuccess(false);
            validation.setMessage("Not validated");
            detail.append("Updates to Top List Speciale need verification.");
            engineData.addNegativeCheckStatus("UPDT_REVIEW_NEEDED", "Updated elements cannot be checked automatically.");
            LOG.debug("Updates to Top List Speciale need verification.");
          }

        }
      }

      if (changes.isDataChanged("ISU") || changes.isDataChanged("ClientTier") || changes.isDataChanged("SearchTerm")
          || changes.isDataChanged("InstallBranchOff")) {
        UpdatedDataModel isuCdChange = changes.getDataChange("ISU");
        UpdatedDataModel clientTierChange = changes.getDataChange("ClientTier");
        UpdatedDataModel sboChange = changes.getDataChange("SearchTerm");
        UpdatedDataModel iboChange = changes.getDataChange("InstallBranchOff");

        if (isuCdChange != null || clientTierChange != null || sboChange != null || iboChange != null) {
          String designatedUser = SystemParameters.getString("ISU_CTC_SBO_USER");
          isNegativeCheckNeedeed = admin.getRequesterId().equalsIgnoreCase(designatedUser) ? false : true;
          if (isNegativeCheckNeedeed) {
            validation.setSuccess(false);
            validation.setMessage("Not validated");
            detail.append("Updates to ISU/CTC/SBO/IBO need verification.");
            engineData.addNegativeCheckStatus("UPDT_REVIEW_NEEDED", "Updated elements cannot be checked automatically.");
            LOG.debug("Updates to ISU/CTC/SBO/IBO need verification.");
          }

        }
      }

    }

    if (!isNegativeCheckNeedeed) {
      validation.setSuccess(true);
      validation.setMessage("Validated");
    }
    output.setDetails(detail.toString());
    return true;
  }

  @Override
  public boolean runUpdateChecksForAddress(EntityManager entityManager, AutomationEngineData engineData, RequestData requestData,
      RequestChangeContainer changes, AutomationResult<ValidationOutput> output, ValidationOutput validation) throws Exception {
    Data data = requestData.getData();
    Admin admin = requestData.getAdmin();
    boolean doesBillingMatchDnb = true;
    boolean isNegativeCheckNeedeed = false;
    Addr addressH = requestData.getAddress("ZD02");
    Addr billing = requestData.getAddress("ZP01");
    StringBuilder detail = new StringBuilder();

    LOG.debug("Address changes are -> " + changes);
    if (changes != null && changes.hasAddressChanges()) {
      if (billing != null && (changes.isAddressChanged("Billing"))) {
        LOG.debug("Billing changed -> " + changes.isAddressChanged("Billing"));

        // Check if address closely matches DnB
        List<DnBMatchingResponse> matches = getMatches(requestData, engineData, billing);
        if (matches != null) {
          doesBillingMatchDnb = ifaddressCloselyMatchesDnb(matches, billing, admin, data.getCmrIssuingCntry());
        }
        if (!doesBillingMatchDnb) {
          isNegativeCheckNeedeed = true;
          detail.append("Updates to Billing address need verification as it does not match D&B");
          LOG.debug("Updates to Billing address need verification as it does not match D&B");
        }
      }

      if (addressH != null && (changes.isAddressChanged("(H Address (IGF))"))) {
        if (!"IGF".equalsIgnoreCase(admin.getRequestingLob())) {
          isNegativeCheckNeedeed = true;
        }
      }
    }

    if (isNegativeCheckNeedeed) {
      validation.setSuccess(false);
      validation.setMessage("Not validated");
      engineData.addNegativeCheckStatus("UPDT_REVIEW_NEEDED", "Updated elements cannot be checked automatically.");
    } else {
      validation.setSuccess(true);
      detail.append("Updates to relevant addresses found but have been marked as Verified.");
      validation.setMessage("Validated");
    }
    output.setDetails(detail.toString());
    return true;
  }

}