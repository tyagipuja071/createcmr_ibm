package com.ibm.cio.cmr.request.automation.util.geo;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
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
import com.ibm.cmr.services.client.CmrServicesFactory;
import com.ibm.cmr.services.client.MatchingServiceClient;
import com.ibm.cmr.services.client.PPSServiceClient;
import com.ibm.cmr.services.client.ServiceClient.Method;
import com.ibm.cmr.services.client.matching.MatchingResponse;
import com.ibm.cmr.services.client.matching.cmr.DuplicateCMRCheckRequest;
import com.ibm.cmr.services.client.matching.cmr.DuplicateCMRCheckResponse;
import com.ibm.cmr.services.client.matching.dnb.DnBMatchingResponse;
import com.ibm.cmr.services.client.pps.PPSRequest;
import com.ibm.cmr.services.client.pps.PPSResponse;

/**
 * 
 * @author RoopakChugh
 *
 */

public class GermanyUtil extends AutomationUtil {

  private static final Logger LOG = Logger.getLogger(GermanyUtil.class);
  private static List<DeSortlMapping> sortlMappings = new ArrayList<DeSortlMapping>();
  private static final String MATCHING = "matching";
  private static final String POSTAL_CD_RANGE = "postalCdRange";
  private static final String SORTL = "SORTL";

  @SuppressWarnings("unchecked")
  public GermanyUtil() {
    if (GermanyUtil.sortlMappings.isEmpty()) {
      Digester digester = new Digester();
      digester.setValidating(false);
      digester.addObjectCreate("mappings", ArrayList.class);

      digester.addObjectCreate("mappings/mapping", DeSortlMapping.class);

      digester.addBeanPropertySetter("mappings/mapping/subIndustryCds", "subIndustryCds");
      digester.addBeanPropertySetter("mappings/mapping/postalCdRanges", "postalCdRanges");
      digester.addBeanPropertySetter("mappings/mapping/isu", "isu");
      digester.addBeanPropertySetter("mappings/mapping/ctc", "ctc");
      digester.addBeanPropertySetter("mappings/mapping/sortl", "sortl");
      digester.addSetNext("mappings/mapping", "add");
      try {
        ClassLoader loader = GermanyUtil.class.getClassLoader();
        InputStream is = loader.getResourceAsStream("de-sortl-mapping.xml");
        GermanyUtil.sortlMappings = (ArrayList<DeSortlMapping>) digester.parse(is);
      } catch (Exception e) {
        LOG.error("Error occured while digesting xml.", e);
      }
    }
  }

  @Override
  public boolean performScenarioValidation(EntityManager entityManager, RequestData requestData, AutomationEngineData engineData,
      AutomationResult<ValidationOutput> results, StringBuilder details, ValidationOutput output) {
    Data data = requestData.getData();
    Addr zs01 = requestData.getAddress("ZS01");
    Admin admin = requestData.getAdmin();
    boolean valid = true;
    String scenario = data.getCustSubGrp();
    // cmr-2067 fix
    engineData.setMatchDepartment(true);
    if (StringUtils.isNotBlank(scenario)) {
      switch (scenario) {
      case "PRIPE":
      case "IBMEM":
        for (Addr addr : requestData.getAddresses()) {
          String custNm = addr.getCustNm1() + (StringUtils.isNotBlank(addr.getCustNm2()) ? " " + addr.getCustNm2() : "");
          if (StringUtils.isNotBlank(custNm) && (custNm.contains("GmbH") || custNm.contains("AG") || custNm.contains("e.V.") || custNm.contains("OHG")
              || custNm.contains("Co.KG") || custNm.contains("Co.OHG") || custNm.contains("KGaA") || custNm.contains("mbH") || custNm.contains("UG")
              || custNm.contains("e.G") || custNm.contains("mit beschränkter Haftung") || custNm.contains("Aktiengesellschaft"))) {
            engineData.addRejectionComment("Scenario chosen is incorrect, should be Commercial.");
            details.append("Scenario chosen is incorrect, should be Commercial.").append("\n");
            valid = false;
            break;
          }
        }
        if (valid) {
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
            request.setNameMatch("Y");
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
                details.append(
                    "The " + (scenario.equals("PRIPE") ? "Private Person" : "IBM Employee") + " already has a record with CMR No. " + duplicateCMRNo)
                    .append("\n");
                engineData.addRejectionComment(
                    "The " + (scenario.equals("PRIPE") ? "Private Person" : "IBM Employee") + " already has a record with CMR No. " + duplicateCMRNo);
                valid = false;
              } else {
                details.append("No Duplicate CMRs were found.").append("\n");
              }
              if (StringUtils.isBlank(duplicateCMRNo) && scenario.equals("IBMEM")) {
                Person person = null;
                if (StringUtils.isNotBlank(zs01.getCustNm1())) {
                  try {
                    String mainCustName = zs01.getCustNm1() + (StringUtils.isNotBlank(zs01.getCustNm2()) ? " " + zs01.getCustNm2() : "");
                    person = BluePagesHelper.getPersonByName(insertGermanCharacters(mainCustName));
                    if (person == null) {
                      engineData.addRejectionComment("Employee details not found in IBM BluePages.");
                      details.append("Employee details not found in IBM BluePages.").append("\n");
                    } else {
                      details.append("Employee details validated with IBM BluePages for " + person.getName() + "(" + person.getEmail() + ").")
                          .append("\n");
                    }
                  } catch (Exception e) {
                    LOG.error("Not able to check name against bluepages", e);
                    engineData.addNegativeCheckStatus("BLUEPAGES_NOT_VALIDATED",
                        "Not able to check name against bluepages for scenario IBM Employee.");
                  }
                } else {
                  LOG.warn("Not able to check name against bluepages, Customer Name 1 not found on the main address");
                  engineData.addNegativeCheckStatus("BLUEPAGES_NOT_VALIDATED", "Customer Name 1 not found on the main address");
                }
              }
            }
          } catch (Exception e) {
            details.append("Duplicate CMR check using customer name match failed to execute.").append("\n");
            engineData.addNegativeCheckStatus("DUPLICATE_CHECK_ERROR", "Duplicate CMR check using customer name match failed to execute.");
          }
        }
        break;
      case "BROKR":
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
            details.append("Unable to perform Duplicate CMR Check for Broker scenario.").append("\n");
            engineData.addNegativeCheckStatus("CMR_CHECK_FAILED", "Unable to perform Duplicate CMR Check for Broker scenario.");
          }
          if (count > 1) {
            engineData.addRejectionComment("Multiple registered CMRs already found for this customer.");
            details.append("Multiple registered CMRs already found for this customer.").append("\n");
            valid = false;
          } else if (count == 1) {
            details.append("Single registered CMR found for this customer.").append("\n");
          } else {
            details.append("No registered CMRs found for this customer.").append("\n");
          }
        } catch (Exception e) {
          LOG.error("Unable to perform Duplicate CMR Check for BROKR scenario.", e);
          details.append("Unable to perform Duplicate CMR Check for Broker scenario.");
          engineData.addNegativeCheckStatus("CMR_CHECK_FAILED", "Unable to perform Duplicate CMR Check for Broker scenario.");
        }
        break;
      case "BUSPR":
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
              details.append("PPS CE ID on the request is invalid.").append("\n");
              valid = false;
            } else {
              details.append("PPS CE ID validated successfully with PartnerWorld Profile Systems.").append("\n");
            }
          } catch (Exception e) {
            LOG.error("Not able to validate PPS CE ID using PPS Service.", e);
            details.append("Not able to validate PPS CE ID using PPS Service.").append("\n");
            engineData.addNegativeCheckStatus("PPSCEID", "Not able to validate PPS CE ID using PPS Service.");
          }
        } else {
          details.append("PPS CE ID not available on the request.").append("\n");
          engineData.addNegativeCheckStatus("PPSCEID", "PPS CE ID not available on the request.");
        }
        break;
      case "INTIN":
      case "INTSO":
      case "INTAM":
        // check value of Department under '##GermanyInternalDepartment' LOV
        String sql = ExternalizedQuery.getSql("DE.CHECK_DEPARTMENT");
        PreparedQuery query = new PreparedQuery(entityManager, sql);
        String dept = data.getIbmDeptCostCenter();
        if (StringUtils.isNotBlank(dept)) {
          query.setParameter("CD", dept);
          String result = query.getSingleResult(String.class);
          if (result == null) {
            engineData.addRejectionComment("IBM Department/Cost Center on the request is invalid.");
            details.append("IBM Department/Cost Center on the request is invalid.").append("\n");
            valid = false;
          } else {
            details.append("IBM Department/Cost Center " + dept + " validated successfully.").append("\n");
          }
        } else {
          details.append("IBM Department/Cost Center not provided on the request. Default Department Number(00X306) will be set.");
          data.setIbmDeptCostCenter("00X306");
        }
        break;
      }

      if (!scenario.equals("3PADC")) {
        for (Addr addr : requestData.getAddresses()) {
          String custNm = addr.getCustNm1() + (StringUtils.isNotBlank(addr.getCustNm2()) ? addr.getCustNm2() : "");
          if (StringUtils.isNotBlank(custNm)
              && (custNm.toUpperCase().contains("C/O") || custNm.toUpperCase().contains("C / O") || custNm.toUpperCase().contains("CARE OF"))) {
            engineData.addNegativeCheckStatus("SCENARIO_CHECK", "The scenario should be for 3rd Party / Data Center.");
            break;
          }
        }
      }
      if (admin.getSourceSystId() != null && "MARKETPLACE".equalsIgnoreCase(admin.getSourceSystId())) {
        engineData.addNegativeCheckStatus("MARKETPLACE", "Processor review is required for MARKETPLACE requests.");
      }

      // String[] skipCompanyCheckList = { "PRIPE", "IBMEM" };
      // skipCompanyCheckForScenario(requestData, engineData,
      // Arrays.asList(skipCompanyCheckList), false);

    } else {
      valid = false;
      engineData.addRejectionComment("No Scenario found on the request");
      details.append("No Scenario found on the request").append("\n");
    }
    return valid;
  }

  @Override
  public AutomationResult<OverrideOutput> doCountryFieldComputations(EntityManager entityManager, AutomationResult<OverrideOutput> results,
      StringBuilder details, OverrideOutput overrides, RequestData requestData, AutomationEngineData engineData) throws Exception {
    Data data = requestData.getData();
    Addr zs01 = requestData.getAddress("ZS01");
    String custNm1 = StringUtils.isNotBlank(zs01.getCustNm1()) ? zs01.getCustNm1().trim() : "";
    String custNm2 = StringUtils.isNotBlank(zs01.getCustNm2()) ? zs01.getCustNm2().trim() : "";
    String mainCustNm = (custNm1 + (StringUtils.isNotBlank(custNm2) ? " " + custNm2 : "")).toUpperCase();
    String mainStreetAddress1 = (StringUtils.isNotBlank(zs01.getAddrTxt()) ? zs01.getAddrTxt() : "").trim().toUpperCase();
    String mainCity = (StringUtils.isNotBlank(zs01.getCity1()) ? zs01.getCity1() : "").trim().toUpperCase();
    String mainPostalCd = (StringUtils.isNotBlank(zs01.getPostCd()) ? zs01.getPostCd() : "").trim();
    Iterator<Addr> it = requestData.getAddresses().iterator();
    boolean removed = false;
    details.append("Checking for duplicate address records - ").append("\n");
    while (it.hasNext()) {
      Addr addr = it.next();
      if (!"ZS01".equals(addr.getId().getAddrType())) {
        removed = true;
        String custNm = (addr.getCustNm1().trim() + (StringUtils.isNotBlank(addr.getCustNm2()) ? " " + addr.getCustNm2().trim() : "")).toUpperCase();
        if (custNm.equals(mainCustNm) && addr.getAddrTxt().trim().toUpperCase().equals(mainStreetAddress1)
            && addr.getCity1().trim().toUpperCase().equals(mainCity) && addr.getPostCd().trim().equals(mainPostalCd)) {
          details.append("Removing duplicate address record: " + addr.getId().getAddrType() + " from the request.").append("\n");
          entityManager.remove(addr);
          it.remove();
        }
      }
    }

    if (!removed) {
      details.append("No duplicate address records found on the request.").append("\n");
    }

    // replace special characters from addresses.
    details.append("Replacing German language characters from address fields if any.").append("\n");
    for (Addr addr : requestData.getAddresses()) {
      if (addr != null) {
        addr.setCustNm1(replaceGermanCharacters(addr.getCustNm1()));
        addr.setCustNm2(replaceGermanCharacters(addr.getCustNm2()));
        addr.setAddrTxt(replaceGermanCharacters(addr.getAddrTxt()));
        addr.setAddrTxt2(replaceGermanCharacters(addr.getAddrTxt2()));
        addr.setCity1(replaceGermanCharacters(addr.getCity1()));
        addr.setDept(replaceGermanCharacters(addr.getDept()));
        addr.setOffice(replaceGermanCharacters(addr.getOffice()));
        addr.setBldg(replaceGermanCharacters(addr.getBldg()));
        addr.setFloor(replaceGermanCharacters(addr.getFloor()));
        data.setAbbrevNm(replaceGermanCharacters(data.getAbbrevNm()));
      }
    }

    return results;
  }

  private HashMap<String, String> getSORTLFromPostalCodeMapping(String subIndustryCd, String postCd, String isuCd, String clientTier) {
    HashMap<String, String> response = new HashMap<String, String>();
    response.put(MATCHING, "");
    response.put(POSTAL_CD_RANGE, "");
    response.put(SORTL, "");
    if (!sortlMappings.isEmpty()) {
      int postalCd = Integer.parseInt(postCd);
      int distance = 1000;
      String nearbySortl = null;
      String nearbyPostalCdRange = null;
      for (DeSortlMapping mapping : sortlMappings) {
        List<String> subIndustryCds = Arrays.asList(mapping.getSubIndustryCds().replaceAll("\n", "").replaceAll(" ", "").split(","));
        if (subIndustryCds.contains(subIndustryCd) && isuCd.equals(mapping.getIsu()) && clientTier.equals(mapping.getCtc())) {
          if (StringUtils.isNotBlank(mapping.getPostalCdRanges())) {
            String[] postalCodeRanges = mapping.getPostalCdRanges().replaceAll("\n", "").replaceAll(" ", "").split(",");
            for (String postalCdRange : postalCodeRanges) {
              String[] range = postalCdRange.split("to");
              int start = 0;
              int end = 0;
              if (range.length == 2) {
                start = Integer.parseInt(range[0]);
                end = Integer.parseInt(range[1]);
              } else if (range.length == 1) {
                start = Integer.parseInt(range[0].replaceAll("x", "0"));
                end = Integer.parseInt(range[0].replaceAll("x", "9"));
              }
              String postalCodeRange = start + " to " + end;
              if (postalCd >= start && postalCd <= end) {
                response.put(MATCHING, "Exact Match");
                response.put(SORTL, mapping.getSortl());
                response.put(POSTAL_CD_RANGE, postalCodeRange);
                return response;
              } else if (postalCd > end) {
                int diff = postalCd - end;
                if (diff > 0 && diff < distance) {
                  distance = diff;
                  nearbySortl = mapping.getSortl();
                  nearbyPostalCdRange = postalCodeRange;
                }
              } else if (postalCd < start) {
                int diff = start - postalCd;
                if (diff > 0 && diff < distance) {
                  distance = diff;
                  nearbySortl = mapping.getSortl();
                  nearbyPostalCdRange = postalCodeRange;
                }
              }
            }
          } else {
            response.put(MATCHING, "Exact Match");
            response.put(SORTL, mapping.getSortl());
            response.put(POSTAL_CD_RANGE, "- No Postal Code Range Defined -");
            return response;
          }
        }
      }
      if (StringUtils.isNotBlank(nearbySortl)) {
        response.put(MATCHING, "Nearest Match");
        LOG.debug("SORTL Calculated by near by postal code range logic: " + nearbySortl);
      } else {
        response.put(MATCHING, "No Match Found");
      }
      response.put(SORTL, nearbySortl);
      response.put(POSTAL_CD_RANGE, nearbyPostalCdRange);
      return response;
    } else {
      response.put(MATCHING, "No Match Found");
      return response;
    }
  }

  private String replaceGermanCharacters(String input) {
    if (StringUtils.isNotBlank(input)) {
      String str = input.replaceAll("Ä", "AE").replaceAll("ä", "ae").replaceAll("Ö", "OE").replaceAll("ö", "oe").replaceAll("Ü", "UE")
          .replace("ü", "ue").replaceAll("ß", "SS");
      return str;

    }
    return null;
  }

  private String insertGermanCharacters(String input) {
    if (StringUtils.isNotBlank(input)) {
      String str = input.replaceAll("Ae", "Ä").replaceAll("ae", "ä").replace("AE", "Ä").replaceAll("Oe", "Ö").replaceAll("oe", "ö").replace("OE", "Ö")
          .replaceAll("Ue", "Ü").replace("ue", "ü").replace("UE", "Ü").replaceAll("ss", "ß").replaceAll("SS", "ß").replace("Ss", "ß");
      return str;
    }
    return null;
  }

  @Override
  public boolean performCountrySpecificCoverageCalculations(CalculateCoverageElement covElement, EntityManager entityManager,
      AutomationResult<OverrideOutput> results, StringBuilder details, OverrideOutput overrides, RequestData requestData,
      AutomationEngineData engineData, String covFrom, CoverageContainer container, boolean isCoverageCalculated) throws Exception {
    Data data = requestData.getData();
    Addr zs01 = requestData.getAddress("ZS01");
    String coverageId = container.getFinalCoverage();
    details.append("\n");
    if (isCoverageCalculated && StringUtils.isNotBlank(coverageId) && covFrom != null
        && (CalculateCoverageElement.BG_CALC.equals(covFrom) || CalculateCoverageElement.BG_ODM.equals(engineData.get(covFrom)))) {
      overrides.addOverride(AutomationElementRegistry.GBL_CALC_COV, "DATA", "SEARCH_TERM", data.getSearchTerm(), coverageId);
      details.append("Computed SORTL = " + coverageId).append("\n");
      results.setResults("Coverage Calculated");
      engineData.addPositiveCheckStatus(AutomationEngineData.COVERAGE_CALCULATED);
    } else if ("32".equals(data.getIsuCd()) && "S".equals(data.getClientTier())) {
      details.setLength(0); // clearing details
      overrides.clearOverrides();
      details.append("Calculating coverage using 32S-PostalCode logic.").append("\n");
      HashMap<String, String> response = getSORTLFromPostalCodeMapping(data.getSubIndustryCd(), zs01.getPostCd(), data.getIsuCd(),
          data.getClientTier());
      LOG.debug("Calculated SORTL: " + response.get(SORTL));
      if (StringUtils.isNotBlank(response.get(MATCHING))) {
        switch (response.get(MATCHING)) {
        case "Exact Match":
        case "Nearest Match":
          overrides.addOverride(AutomationElementRegistry.GBL_CALC_COV, "DATA", "SEARCH_TERM", data.getSearchTerm(), response.get(SORTL));
          details.append("Coverage calculation Successful.").append("\n");
          details.append("Computed SORTL = " + response.get(SORTL)).append("\n\n");
          details.append("Matched Rule:").append("\n");
          details.append("Sub Industry = " + data.getSubIndustryCd()).append("\n");
          details.append("ISU = " + data.getIsuCd()).append("\n");
          details.append("CTC = " + data.getClientTier()).append("\n");
          details.append("Postal Code Range = " + response.get(POSTAL_CD_RANGE)).append("\n\n");
          details.append("Matching: " + response.get(MATCHING));
          results.setResults("Coverage Calculated");
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
      details.setLength(0);
      overrides.clearOverrides();
      details.append("Skipped coverage calculation from 32S-PostalCode logic.").append("\n");
      results.setResults("Skipped");
    }
    return true;
  }

  @Override
  public boolean runUpdateChecksForData(EntityManager entityManager, AutomationEngineData engineData, RequestData requestData,
      RequestChangeContainer changes, AutomationResult<ValidationOutput> output, ValidationOutput validation) throws Exception {
    Admin admin = requestData.getAdmin();
    Addr soldTo = requestData.getAddress("ZS01");
    StringBuilder detail = new StringBuilder();
    boolean isNegativeCheckNeedeed = false;
    if (changes != null && changes.hasDataChanges()) {
      if (changes.isDataChanged("VAT")) {
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
              detail.append("Updates to VAT need verification as it does'nt matches DnB");
              engineData.addNegativeCheckStatus("UPDT_REVIEW_NEEDED", "Updated elements cannot be checked automatically.");
              LOG.debug("Updates to VAT need verification as it does not matches DnB");
            }

          } else if (StringUtils.isNotBlank(vatChange.getOldData()) && StringUtils.isBlank(vatChange.getNewData())) {
            admin.setScenarioVerifiedIndc("N");
            detail.append("Setting scenario verified indc= N as VAT is blank");
            LOG.debug("Setting scenario verified indc= N as VAT is blank");
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
    boolean isInstallAtMatchesDnb = true;
    boolean isBillToMatchesDnb = true;
    boolean isNegativeCheckNeedeed = false;
    boolean isInstallAtExistOnReq = false;
    boolean isBillToExistOnReq = false;
    boolean isShipToExistOnReq = false;
    Addr installAt = requestData.getAddress("ZI01");
    Addr billTo = requestData.getAddress("ZP01");
    Addr shipTo = requestData.getAddress("ZD01");
    StringBuilder detail = new StringBuilder();
    long reqId = requestData.getAdmin().getId().getReqId();
    if (changes != null && changes.hasAddressChanges()) {
      if (StringUtils.isNotEmpty(data.getCustClass()) && ("81".equals(data.getCustClass()) || "85".equals(data.getCustClass()))
          && !changes.hasDataChanges()) {
        LOG.debug("Skipping checks as customer class is " + data.getCustClass() + " and only address changes.");
        output.setDetails("Skipping checks as customer class is " + data.getCustClass() + " and only address changes.");
        validation.setSuccess(true);
        validation.setMessage("No Validations");
        return true;
      }

      if (shipTo != null && (changes.isAddressChanged("Ship To") || isAddressAdded(shipTo))) {
        // Check If Address already exists on request
        isShipToExistOnReq = isAddressAleardyExists(entityManager, shipTo, reqId);
        if (isShipToExistOnReq) {
          detail.append("Ship To already exists on the request with same details.");
          validation.setMessage("ShipTo already exists");
          engineData.addRejectionComment("Ship To already exists on the request with same details.");
          LOG.debug("Ship To already exists on the request with same details.");
        }
      }

      if (installAt != null && (changes.isAddressChanged("Install At (1)") || isAddressAdded(installAt))) {
        // Check If Address already exists on request
        isInstallAtExistOnReq = isAddressAleardyExists(entityManager, installAt, reqId);
        if (isInstallAtExistOnReq) {
          detail.append("Install At already exists on the request with same details.");
          engineData.addRejectionComment("Install At already exists on the request with same details.");
          LOG.debug("Install At already exists on the request with same details.");
        }
        // Check if address closely matches DnB
        List<DnBMatchingResponse> matches = getMatches(requestData, engineData, installAt);
        if (matches != null) {
          isInstallAtMatchesDnb = ifaddressCloselyMatchesDnb(matches, installAt, admin, data.getCmrIssuingCntry());
        }
        if (!isInstallAtMatchesDnb) {
          isNegativeCheckNeedeed = true;
          detail.append("Updates to Install At address need verification as it does not matches D&B");
          LOG.debug("Updates to Install At address need verification as it does not matches D&B");
        }
      }

      if (billTo != null && (changes.isAddressChanged("Bill To") || isAddressAdded(billTo))) {
        // Check If Address already exists on request
        isBillToExistOnReq = isAddressAleardyExists(entityManager, billTo, reqId);
        if (isBillToExistOnReq) {
          detail.append("Bill To already exists on the request with same details.");
          engineData.addRejectionComment("Bill To already exists on the request with same details.");
          LOG.debug("Bill To already exists on the request with same details.");
        }

        // Check if address closely matches DnB
        List<DnBMatchingResponse> matches = getMatches(requestData, engineData, billTo);
        if (matches != null) {
          isBillToMatchesDnb = ifaddressCloselyMatchesDnb(matches, billTo, admin, data.getCmrIssuingCntry());
          if (!isBillToMatchesDnb) {
            isNegativeCheckNeedeed = true;
            detail.append("Updates to Bill To address need verification as it does not matches D&B");
            LOG.debug("Updates to Bill To address need verification as it does not matches D&B");
          }

        }
      }

      if (isNegativeCheckNeedeed || isShipToExistOnReq || isInstallAtExistOnReq || isBillToExistOnReq) {
        validation.setSuccess(false);
        validation.setMessage("Not validated");
        engineData.addNegativeCheckStatus("UPDT_REVIEW_NEEDED", "Updated elements cannot be checked automatically.");
      } else {
        validation.setSuccess(true);
        detail.append("Updates to relevant addresses found but have been marked as Verified.");
        validation.setMessage("Validated");
      }

    }
    output.setDetails(detail.toString());
    return true;
  }

  /**
   * Checks if the address is added on the Update Request
   *
   * @param addr
   * @return
   */
  private boolean isAddressAdded(Addr addr) {
    if (StringUtils.isNotEmpty(addr.getImportInd()) && "N".equals(addr.getImportInd())) {
      return true;
    }
    return false;
  }

  /**
   * Checks if the address already exists on the Request
   *
   * @param addrToBeCHecked
   * @param reqId
   * @return
   */
  private boolean isAddressAleardyExists(EntityManager entityManager, Addr addrToBeCHecked, long reqId) {
    boolean addrExists = false;
    String sql = ExternalizedQuery.getSql("AUTO.DE.CHECK_IF_ADDRESS_EXIST");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", reqId);
    query.setParameter("NAME1", addrToBeCHecked.getCustNm1());
    query.setParameter("NAME2", addrToBeCHecked.getCustNm2());
    query.setParameter("DEPT", addrToBeCHecked.getDept());
    query.setParameter("FLOOR", addrToBeCHecked.getFloor());
    query.setParameter("BLDG", addrToBeCHecked.getBldg());
    query.setParameter("OFFICE", addrToBeCHecked.getOffice());
    query.setParameter("LAND_CNTRY", addrToBeCHecked.getLandCntry());
    query.setParameter("STATE", addrToBeCHecked.getStateProv());
    query.setParameter("ADDR_TXT", addrToBeCHecked.getAddrTxt());
    query.setParameter("PO_BOX", addrToBeCHecked.getPoBox());
    query.setParameter("POST_CD", addrToBeCHecked.getPostCd());
    query.setParameter("CITY", addrToBeCHecked.getCity1());
    query.setParameter("PHONE", addrToBeCHecked.getCustNm1());
    query.setParameter("COUNTY", addrToBeCHecked.getCounty());
    query.setParameter("TRANSPORT_ZONE", addrToBeCHecked.getTransportZone());
    // query.setParameter("ADDR_TYPE", addrToBeCHecked.getId().getAddrType());
    query.setParameter("ADDR_SEQ", addrToBeCHecked.getId().getAddrSeq());
    String res = query.getSingleResult(String.class);
    if (res != null) {
      if (Integer.parseInt(res) == 1) {
        addrExists = true;
      }
    }
    return addrExists;
  }

}
