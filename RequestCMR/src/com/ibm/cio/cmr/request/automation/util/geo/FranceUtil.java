package com.ibm.cio.cmr.request.automation.util.geo;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.apache.commons.digester.Digester;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import com.ibm.cio.cmr.request.CmrConstants;
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
import com.ibm.cio.cmr.request.entity.DataRdc;
import com.ibm.cio.cmr.request.model.window.UpdatedDataModel;
import com.ibm.cio.cmr.request.model.window.UpdatedNameAddrModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.util.BluePagesHelper;
import com.ibm.cio.cmr.request.util.ConfigUtil;
import com.ibm.cio.cmr.request.util.Person;
import com.ibm.cio.cmr.request.util.SystemLocation;
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
    Data data = requestData.getData();
    String coverage = (String) engineData.get(AutomationEngineData.COVERAGE_CALCULATED);
    if (!StringUtils.isBlank(coverage) || engineData.hasPositiveCheckStatus(AutomationEngineData.COVERAGE_CALCULATED)) {
      // if calculated data would hold value for sbo calculated using coverage
      // calculation
      details.append("SBO value calculated via Coverage Calculation Element- " + data.getSalesBusOffCd()).append("\n");
    } else {
      if (StringUtils.isNotBlank(data.getSalesBusOffCd()) && StringUtils.isNotBlank(data.getInstallBranchOff())) {
        details.append("SBO value already provided on the request - " + data.getSalesBusOffCd()).append("\n");
        results.setResults("Skipped");
      } else {
        engineData.addRejectionComment("OTH", "SBO cannot be computed automatically.", "", "");
        details.append("SBO cannot be computed automatically.").append("\n");
        results.setResults("SBO not calculated.");
        results.setOnError(true);
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
    Addr zs01 = requestData.getAddress("ZS01");
    Admin admin = requestData.getAdmin();
    boolean valid = true;
    String scenario = data.getCustSubGrp();
    if (StringUtils.isNotBlank(scenario)) {
      String scenarioDesc = getScenarioDescription(entityManager, data);
      if (StringUtils.isNotBlank(data.getCountryUse()) && data.getCountryUse().length() > 3 && !"CBMME".equals(scenario)
          && !"COMME".equals(scenario)) {
        engineData.addNegativeCheckStatus("DISABLEDAUTOPROC",
            "Requests for " + scenarioDesc + " cannot be processed automatically. Manual processing would be required.");
      } else {
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
                String zs01Kunnr = getZS01Kunnr(duplicateCMRNo, SystemLocation.FRANCE);
                details.append("The " + ((scenario.equals("PRICU") || scenario.equals("CBICU")) ? "Private Customer" : "IBM Employee")
                    + " already has a record with CMR No. " + duplicateCMRNo);
                engineData.addRejectionComment("DUPC",
                    "Customer already exists / duplicate CMR.The "
                        + ((scenario.equals("PRICU") || scenario.equals("CBICU")) ? "Private Customer" : "IBM Employee")
                        + " already has a record with CMR No. " + duplicateCMRNo,
                    duplicateCMRNo, zs01Kunnr);
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
                    engineData.addRejectionComment("OTH", "Employee details not found in IBM BluePages.", "", "");
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
              engineData.addRejectionComment("OTH", "Multiple registered CMRs already found for this customer.", "", "");
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
                engineData.addRejectionComment("OTH", "PPS CE ID on the request is invalid.", "", "");
                details.append("PPS CE ID on the request is invalid.");
                valid = false;
              } else {
                details.append("PPS CE ID validated successfully with PartnerWorld Profile Systems.");
              }
              engineData.addNegativeCheckStatus("DISABLEDAUTOPROC",
                  "Requests for " + scenarioDesc + " cannot be processed automatically. Manual processing would be required.");
            } catch (Exception e) {
              LOG.error("Not able to validate PPS CE ID using PPS Service.", e);
              details.append("Not able to validate PPS CE ID using PPS Service.");
              engineData.addNegativeCheckStatus("PPSCEID", "Not able to validate PPS CE ID using PPS Service.");
            }
          } else {
            details.append("PPS CE ID not available on the request.");
            engineData.addNegativeCheckStatus("PPSCEID", "PPS CE ID not available on the request.");
          }
          break;
        case "INTER":
        case "CBTER":
        case "LCIFF":
        case "LCIFL":
        case "CBIFF":
        case "CBIFL":
          String mainCustNm = zs01.getCustNm1();
          if (StringUtils.isNotBlank(mainCustNm) && !mainCustNm.toUpperCase().contains("IBM") && !(data.getCountryUse().length() > 3)) {
            engineData.addRejectionComment("OTH", "Wrong Customer Name on the main address. IBM should be part of the name.", "", "");
            details.append("Wrong Customer Name on the main address. IBM should be part of the name.").append("\n");
            valid = false;
          }
          break;
        case "HOSTC":
        case "CBSTC":
          String custNm1 = zs01.getCustNm1();
          String custNm2 = zs01.getCustNm2();
          String custNm = custNm1 + (StringUtils.isNotBlank(custNm2) ? " " + custNm2 : "");
          if (!(data.getCountryUse().length() > 3)) {
            if (StringUtils.isNotBlank(custNm) && custNm.toUpperCase().contains("CHEZ")) {
              valid = true;
            } else {
              engineData.addRejectionComment("OTH", "Wrong Customer Name on Host address. CHEZ should be part of the name.", "", "");
              details.append("Wrong Customer Name on Host address. CHEZ should be part of the name.").append("\n");
              valid = false;
            }
          }
          break;
        case "INTSO":
        case "CBTSO":
          engineData.addNegativeCheckStatus("DISABLEDAUTOPROC",
              "Requests for " + scenarioDesc + " cannot be processed automatically. Manual processing would be required.");
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
              engineData.addRejectionComment("OTH", "Invalid Billing/Mailing address found on the request. The addresses should be the same.", "",
                  "");
              details.append("Invalid Billing/Mailing address found on the request. The addresses should be the same.").append("\n");
              valid = false;
            }
          }
        }
        // if (admin.getSourceSystId() != null) {
        // // if ("MARKETPLACE".equalsIgnoreCase(admin.getSourceSystId())) {
        // // engineData.addNegativeCheckStatus("MARKETPLACE", "Processor review
        // // is required for MARKETPLACE requests.");
        // // } else
        // if ("CreateCMR-BP".equalsIgnoreCase(admin.getSourceSystId())) {
        // engineData.addNegativeCheckStatus("BP_PORTAL", "Processor review is
        // required for BP Portal requests.");
        // }
        // }
      }
    } else {
      if (StringUtils.isBlank(scenario)) {
        valid = false;
        engineData.addRejectionComment("OTH", "No Scenario found on the request", "", "");
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
    if (isCoverageCalculated && StringUtils.isNotBlank(coverageId)
        && (CalculateCoverageElement.BG_CALC.equals(covFrom) || CalculateCoverageElement.BG_ODM.equals(covFrom))) {
      // If calculated using buying group then skip any other calculation
      FieldResultKey sboKey = new FieldResultKey("DATA", "SALES_BO_CD");
      String sboValue = "";
      if (overrides.getData().containsKey(sboKey)) {
        sboValue = overrides.getData().get(sboKey).getNewValue();
        if (StringUtils.isNotBlank(data.getCustGrp()) && !"CROSS".equals(data.getCustGrp())) {
          overrides.addOverride(AutomationElementRegistry.GBL_CALC_COV, "DATA", "INSTALL_BRANCH_OFF", data.getInstallBranchOff(), sboValue);
        } else if (StringUtils.isNotBlank(data.getCustGrp()) && "CROSS".equals(data.getCustGrp())
            && (StringUtils.isBlank(data.getIsuCd()) || (StringUtils.isNotBlank(data.getIsuCd()) && !"200".equals(data.getIsuCd())))) {
          overrides.addOverride(AutomationElementRegistry.GBL_CALC_COV, "DATA", "INSTALL_BRANCH_OFF", data.getInstallBranchOff(), "200");
        }
      } else {
        sboValue = getSBOfromCoverage(entityManager, container.getFinalCoverage());
        if (StringUtils.isNotBlank(sboValue)) {
          details.append("SORTL calculated on basis of Existing CMR Data: " + sboValue);
          if (StringUtils.isNotBlank(data.getCustGrp()) && !"CROSS".equals(data.getCustGrp())) {
            overrides.addOverride(AutomationElementRegistry.GBL_CALC_COV, "DATA", "INSTALL_BRANCH_OFF", data.getInstallBranchOff(), sboValue);
          } else if (StringUtils.isNotBlank(data.getCustGrp()) && "CROSS".equals(data.getCustGrp())
              && (StringUtils.isBlank(data.getIsuCd()) || (StringUtils.isNotBlank(data.getIsuCd()) && !"200".equals(data.getIsuCd())))) {
            overrides.addOverride(AutomationElementRegistry.GBL_CALC_COV, "DATA", "INSTALL_BRANCH_OFF", data.getInstallBranchOff(), "200");
          }
          overrides.addOverride(AutomationElementRegistry.GBL_CALC_COV, "DATA", "SALES_BO_CD", data.getSalesBusOffCd(), sboValue);
        }
      }
      engineData.addPositiveCheckStatus(AutomationEngineData.COVERAGE_CALCULATED);
    } else {
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
          covElement.logCoverage(entityManager, engineData, requestData, null, details, overrides, container, CalculateCoverageElement.FINAL, null,
              null, true);
          FieldResultKey sboKey = new FieldResultKey("DATA", "SALES_BO_CD");
          String sboValue = "";
          if (overrides.getData().containsKey(sboKey)) {
            sboValue = overrides.getData().get(sboKey).getNewValue();
            if (StringUtils.isNotBlank(data.getCustGrp()) && !"CROSS".equals(data.getCustGrp())) {
              overrides.addOverride(AutomationElementRegistry.GBL_CALC_COV, "DATA", "INSTALL_BRANCH_OFF", data.getInstallBranchOff(), sboValue);
            } else if (StringUtils.isNotBlank(data.getCustGrp()) && "CROSS".equals(data.getCustGrp())
                && (StringUtils.isBlank(data.getIsuCd()) || (StringUtils.isNotBlank(data.getIsuCd()) && !"200".equals(data.getIsuCd())))) {
              overrides.addOverride(AutomationElementRegistry.GBL_CALC_COV, "DATA", "INSTALL_BRANCH_OFF", data.getInstallBranchOff(), "200");
            }
          } else {
            sboValue = getSBOfromCoverage(entityManager, coverage.getFinalCoverage());
            if (StringUtils.isNotBlank(sboValue)) {
              details.append("SORTL calculated on basis of Existing CMR Data: " + sboValue);
              if (StringUtils.isNotBlank(data.getCustGrp()) && !"CROSS".equals(data.getCustGrp())) {
                overrides.addOverride(AutomationElementRegistry.GBL_CALC_COV, "DATA", "INSTALL_BRANCH_OFF", data.getInstallBranchOff(), sboValue);
              } else if (StringUtils.isNotBlank(data.getCustGrp()) && "CROSS".equals(data.getCustGrp())
                  && (StringUtils.isBlank(data.getIsuCd()) || (StringUtils.isNotBlank(data.getIsuCd()) && !"200".equals(data.getIsuCd())))) {
                overrides.addOverride(AutomationElementRegistry.GBL_CALC_COV, "DATA", "INSTALL_BRANCH_OFF", data.getInstallBranchOff(), "200");
              }
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
          HashMap<String, String> response = getSBOFromPostalCodeMapping(data.getCountryUse(), data.getIsicCd(), zs01.getPostCd(), data.getIsuCd(),
              data.getClientTier());
          LOG.debug("Calculated SBO: " + response.get(SBO));
          if (StringUtils.isNotBlank(response.get(MATCHING))) {
            switch (response.get(MATCHING)) {
            case "Exact Match":
              overrides.addOverride(AutomationElementRegistry.GBL_CALC_COV, "DATA", "SALES_BO_CD", data.getSalesBusOffCd(), response.get(SBO));
              if (StringUtils.isNotBlank(data.getCustGrp()) && !"CROSS".equals(data.getCustGrp())) {
                overrides.addOverride(AutomationElementRegistry.GBL_CALC_COV, "DATA", "INSTALL_BRANCH_OFF", data.getInstallBranchOff(),
                    response.get(SBO));
              } else if (StringUtils.isNotBlank(data.getCustGrp()) && "CROSS".equals(data.getCustGrp())
                  && (StringUtils.isBlank(data.getIsuCd()) || (StringUtils.isNotBlank(data.getIsuCd()) && !"200".equals(data.getIsuCd())))) {
                overrides.addOverride(AutomationElementRegistry.GBL_CALC_COV, "DATA", "INSTALL_BRANCH_OFF", data.getInstallBranchOff(), "200");
              }
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
  public boolean runUpdateChecksForData(EntityManager entityManager, AutomationEngineData engineData, RequestData requestData,
      RequestChangeContainer changes, AutomationResult<ValidationOutput> output, ValidationOutput validation) throws Exception {
    Admin admin = requestData.getAdmin();
    Addr soldTo = requestData.getAddress("ZS01");
    boolean hasNegativeCheck = false;
    Map<String, String> failedChecks = new HashMap<String, String>();
    LOG.debug("Changes are -> " + changes);

    DataRdc rdc = getDataRdc(entityManager, admin);
    if ("9500".equals(rdc.getIsicCd())) {
      LOG.debug("Private customer record. Skipping validations.");
      validation.setSuccess(true);
      validation.setMessage("Skipped");
      engineData.addPositiveCheckStatus(AutomationEngineData.SKIP_VAT_CHECKS);
      output.setDetails("Update checks skipped for Private Customer record.");
      return true;
    }
    for (UpdatedDataModel updatedDataModel : changes.getDataUpdates()) {
      if (updatedDataModel != null) {
        LOG.debug("Checking updates for : " + new ObjectMapper().writeValueAsString(updatedDataModel));
        String field = updatedDataModel.getDataField();
        switch (field) {
        case "VAT #":
          if (StringUtils.isBlank(updatedDataModel.getOldData()) && StringUtils.isNotBlank(updatedDataModel.getNewData())) {
            // check if the name + VAT exists in D&B
            List<DnBMatchingResponse> matches = getMatches(requestData, engineData, soldTo, true);
            if (matches.isEmpty()) {
              // get DnB matches based on all address details
              matches = getMatches(requestData, engineData, soldTo, false);
            }
            if (!matches.isEmpty()) {
              for (DnBMatchingResponse dnbRecord : matches) {
                if ("Y".equals(dnbRecord.getOrgIdMatch())) {
                  hasNegativeCheck = false;
                  break;
                }
                hasNegativeCheck = true;
                failedChecks.put(field, field + " updated. Updates to " + field + " needs verification as it does not match DnB");
                LOG.debug("Updates to VAT need verification as it does not match DnB");
              }
            }
          }
          break;
        case "Collection Code":
          if ((StringUtils.isBlank(updatedDataModel.getOldData()) && StringUtils.isNotBlank(updatedDataModel.getNewData()))
              || (StringUtils.isNotBlank(updatedDataModel.getOldData()) && StringUtils.isNotBlank(updatedDataModel.getNewData()))) {
            if (!"AR".equalsIgnoreCase(admin.getRequestingLob())) {
              hasNegativeCheck = true;
              failedChecks.put(field, field + " updated. Updates to " + field + " needs verification.");
              LOG.debug("Updates to Collection Code need verification.");
            }
          }
          break;
        case "Top List Speciale":
          String designatedUser = SystemParameters.getString("TOP_LST_SPECI_USER");
          if (!admin.getRequesterId().equalsIgnoreCase(designatedUser)) {
            hasNegativeCheck = true;
            failedChecks.put(field, field + " updated. Updates to " + field + " needs verification.");
            LOG.debug("Updates to Top List Speciale need verification.");
          }
          break;
        case "ISU Code":
        case "Client Tier":
        case "Search Term/Sales Branch Office":
        case "Installing BO":
          String designatedISUCTCUser = SystemParameters.getString("ISU_CTC_SBO_USER");
          if (!admin.getRequesterId().equalsIgnoreCase(designatedISUCTCUser)) {
            hasNegativeCheck = true;
            failedChecks.put(field, field + " updated. Updates to " + field + " needs verification.");
            LOG.debug("Updates to ISU/CTC/SBO/IBO need verification.");
          }
          break;
        case "iERP Site Party ID":
          // SKIP THESE FIELDS
          break;
        default:
          // Set Negative check status for any other fields updated.
          failedChecks.put(field, field + " updated.");
          hasNegativeCheck = true;
          break;
        }
      }
    }

    if (hasNegativeCheck) {
      engineData.addNegativeCheckStatus("RESTRICED_DATA_UPDATED", "Updated elements cannot be checked automatically.");
      output.setDetails("Updated elements cannot be checked automatically.\n");
      if (failedChecks != null && failedChecks.size() > 0) {
        StringBuilder details = new StringBuilder();
        details.append("Updated elements cannot be checked automatically.\nDetails:").append("\n");
        for (String failedCheck : failedChecks.values()) {
          details.append(" - " + failedCheck).append("\n");
        }
        output.setDetails(details.toString());
      }
      validation.setMessage("Review needed.");
      validation.setSuccess(false);
    } else {
      output.setDetails("Updated DATA elements were validated successfully.\n");
      validation.setMessage("Validated");
      validation.setSuccess(true);
    }
    return true;
  }

  @Override
  public boolean runUpdateChecksForAddress(EntityManager entityManager, AutomationEngineData engineData, RequestData requestData,
      RequestChangeContainer changes, AutomationResult<ValidationOutput> output, ValidationOutput validation) throws Exception {
    Data data = requestData.getData();
    Admin admin = requestData.getAdmin();
    boolean doesBillingMatchDnb = true;
    boolean hasNegativeCheck = false;
    Addr billing = requestData.getAddress("ZP01");
    Addr installing = requestData.getAddress("ZS01");
    Addr payment = requestData.getAddress("ZP02");
    StringBuilder detail = new StringBuilder();
    Map<String, String> failedChecks = new HashMap<String, String>();
    DataRdc rdc = getDataRdc(entityManager, admin);
    if ("9500".equals(rdc.getIsicCd())) {
      LOG.debug("Private customer record. Skipping validations.");
      validation.setSuccess(true);
      validation.setMessage("Skipped");
      output.setDetails("Update checks skipped for Private Customer record.");
      engineData.addPositiveCheckStatus(AutomationEngineData.SKIP_VAT_CHECKS);
      return true;
    }
    String newAbbNm = changes.getDataChange("Abbreviated Name") != null ? changes.getDataChange("Abbreviated Name").getNewData() : "";
    String oldAbbNm = changes.getDataChange("Abbreviated Name") != null ? changes.getDataChange("Abbreviated Name").getOldData() : "";

    List<String> addrTypesChanged = new ArrayList<String>();
    for (UpdatedNameAddrModel addrModel : changes.getAddressUpdates()) {
      if (!addrTypesChanged.contains(addrModel.getAddrTypeCode())) {
        addrTypesChanged.add(addrModel.getAddrTypeCode());
      }
    }

    if (addrTypesChanged.contains(CmrConstants.ADDR_TYPE.ZP01.toString())) {

      LOG.debug("Billing changed -> " + changes.isAddressChanged("ZP01"));

      // Check if address closely matches DnB
      List<DnBMatchingResponse> matches = getMatches(requestData, engineData, billing, false);
      if (matches != null) {
        doesBillingMatchDnb = ifaddressCloselyMatchesDnb(matches, billing, admin, data.getCmrIssuingCntry());
      }
      if (!doesBillingMatchDnb) {
        hasNegativeCheck = true;
        failedChecks.put("BILLING_UPDTD", "Updates to Billing address need verification as it does not match D&B.");
        LOG.debug("Updates to Billing address need verification as it does not match D&B");
      }
    }

    if (addrTypesChanged.contains(CmrConstants.ADDR_TYPE.ZD02.toString())) {
      if (!"IGF".equalsIgnoreCase(admin.getRequestingLob()) && !(newAbbNm.contains("DF") && oldAbbNm.contains("D3"))) {
        hasNegativeCheck = true;
        failedChecks.put("H_ADDR_UPDTD", "Address H updated, Requesting LOB should be IGF.");
      }
    }

    if (addrTypesChanged.contains(CmrConstants.ADDR_TYPE.ZS01.toString()) || addrTypesChanged.contains(CmrConstants.ADDR_TYPE.ZP02.toString())) {
      List<Addr> addrsToChk = new ArrayList<Addr>();
      if (payment != null) {
        addrsToChk.add(payment);
      }
      if (installing != null) {
        addrsToChk.add(installing);
      }

      for (Addr addr : addrsToChk) {
        if ("Y".equals(addr.getImportInd())) {
          if ((changes.isAddressFieldChanged(addr.getId().getAddrType(), "Contact Person")
              || changes.isAddressFieldChanged(addr.getId().getAddrType(), "Phone #")) && isOnlyFieldUpdated(changes)
              && engineData.getNegativeCheckStatus("RESTRICED_DATA_UPDATED") == null && failedChecks.isEmpty()) {
            validation.setSuccess(true);
            LOG.debug("Contact Person/Phone# is found to be updated.Updates verified.");
            detail.append("Updates to relevant addresses found but have been marked as Verified.");
            validation.setMessage("Validated");
            hasNegativeCheck = false;
            break;
          }

          else if (!isOnlyFieldUpdated(changes)) {
            hasNegativeCheck = true;
            failedChecks.put("ADDR_FIELDS_UPDTD", "Installing / Payment addresses cannot be modified.");
          }

        }
      }
    }

    if (hasNegativeCheck) {
      engineData.addNegativeCheckStatus("RESTRICED_ADDR_UPDATED", "Updated elements cannot be checked automatically.");
      output.setDetails("Updated elements cannot be checked automatically.\n");
      StringBuilder details = new StringBuilder();
      if (failedChecks != null && failedChecks.size() > 0) {
        details.append("Updated elements cannot be checked automatically.\nDetails:").append("\n");
        for (String failedCheck : failedChecks.values()) {
          details.append(" - " + failedCheck).append("\n");
        }
      }
      validation.setMessage("Review needed.");
      validation.setSuccess(false);
      output.setDetails(details.toString());
    } else {
      validation.setSuccess(true);
      detail.append("Updates to relevant addresses found but have been marked as Verified.");
      validation.setMessage("Validated");
      output.setDetails(detail.toString());
    }
    return true;
  }

  private boolean isOnlyFieldUpdated(RequestChangeContainer changes) {
    boolean isOnlyFieldUpdated = true;
    List<UpdatedNameAddrModel> updatedAddrList = changes.getAddressUpdates();
    String[] addressFields = { "Customer Name", "Customer Name Continuation", "Customer Name/ Additional Address Information", "Country (Landed)",
        "Street", "Street Continuation", "Postal Code", "City", "PostBox" };
    List<String> relevantFieldNames = Arrays.asList(addressFields);
    for (UpdatedNameAddrModel updatedAddrModel : updatedAddrList) {
      String fieldId = updatedAddrModel.getDataField();
      if (StringUtils.isNotEmpty(fieldId) && relevantFieldNames.contains(fieldId)) {
        isOnlyFieldUpdated = false;
        break;
      }
    }

    return isOnlyFieldUpdated;
  }
}