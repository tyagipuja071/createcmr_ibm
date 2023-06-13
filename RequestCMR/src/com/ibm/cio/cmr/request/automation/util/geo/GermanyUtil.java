package com.ibm.cio.cmr.request.automation.util.geo;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

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
import com.ibm.cio.cmr.request.automation.impl.gbl.DupCMRCheckElement;
import com.ibm.cio.cmr.request.automation.out.AutomationResult;
import com.ibm.cio.cmr.request.automation.out.OverrideOutput;
import com.ibm.cio.cmr.request.automation.out.ValidationOutput;
import com.ibm.cio.cmr.request.automation.util.AutomationUtil;
import com.ibm.cio.cmr.request.automation.util.CoverageContainer;
import com.ibm.cio.cmr.request.automation.util.DACHFieldContainer;
import com.ibm.cio.cmr.request.automation.util.RequestChangeContainer;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.model.window.UpdatedDataModel;
import com.ibm.cio.cmr.request.model.window.UpdatedNameAddrModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.util.BluePagesHelper;
import com.ibm.cio.cmr.request.util.ConfigUtil;
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

  private static final List<String> RELEVANT_ADDRESSES = Arrays.asList(CmrConstants.RDC_SOLD_TO, CmrConstants.RDC_BILL_TO,
      CmrConstants.RDC_INSTALL_AT, CmrConstants.RDC_SHIP_TO, CmrConstants.RDC_PAYGO_BILLING);
  private static final List<String> NON_RELEVANT_ADDRESS_FIELDS = Arrays.asList("Building", "Floor", "Office", "Department", "Customer Name 2",
      "Phone #", "PostBox", "State/Province");
  private static final List<String> RELEVANT_ADDRESS_FIELDS_ZS01_ZP01 = Arrays.asList("Street name and number", "Customer legal name");
  private static final List<String> NON_RELEVANT_ADDRESS_FIELDS_ZS01_ZP01 = Arrays.asList("Attention To/Building/Floor/Office",
      "Division/Department");

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
        InputStream is = ConfigUtil.getResourceStream("de-sortl-mapping.xml");
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
    Addr zi01 = requestData.getAddress("ZI01");
    Admin admin = requestData.getAdmin();
    boolean valid = true;
    String scenario = data.getCustSubGrp();
    // cmr-2067 fix
    engineData.setMatchDepartment(true);
    // if (admin.getSourceSystId() != null &&
    // "CreateCMR-BP".equalsIgnoreCase(admin.getSourceSystId())) {
    // // BP skip checks - remove after BP is enabled
    // details.append("Processor review is required for BP Portal
    // requests.").append("\n");
    // engineData.addNegativeCheckStatus("BP_PORTAL", "Processor review is
    // required for BP Portal requests.");
    // skipAllChecks(engineData); // remove after BP is enabled
    // } else
    if ("C".equals(requestData.getAdmin().getReqType())) {
      // remove duplicates
      removeDuplicateAddresses(entityManager, requestData, details);
    }

    if (StringUtils.isNotBlank(scenario)) {
      switch (scenario) {
      case "PRIPE":
      case "IBMEM":
        for (Addr addr : requestData.getAddresses()) {
          String custNm = addr.getCustNm1() + (StringUtils.isNotBlank(addr.getCustNm2()) ? " " + addr.getCustNm2() : "");
          if (StringUtils.isNotBlank(custNm) && (custNm.contains("GmbH") || custNm.contains("AG") || custNm.contains("e.V.") || custNm.contains("OHG")
              || custNm.contains("Co.KG") || custNm.contains("Co.OHG") || custNm.contains("KGaA") || custNm.contains("mbH") || custNm.contains("UG")
              || custNm.contains("e.G") || custNm.contains("mit beschränkter Haftung") || custNm.contains("Aktiengesellschaft"))) {
            engineData.addRejectionComment("OTH", "Scenario chosen is incorrect, should be Commercial.", "", "");
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
            request.setIsicCd("9500");
            request.setPostalCode(zs01.getPostCd());
            request.setStateProv(zs01.getStateProv());
            request.setStreetLine1(zs01.getAddrTxt());
            request.setStreetLine2(zs01.getAddrTxt2());
            request.setCity(zs01.getCity1());
            request.setCustClass("71");
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
                String zs01Kunnr = getZS01Kunnr(duplicateCMRNo, SystemLocation.GERMANY);
                details.append(
                    "The " + (scenario.equals("PRIPE") ? "Private Person" : "IBM Employee") + " already has a record with CMR No. " + duplicateCMRNo)
                    .append("\n");
                engineData.addRejectionComment("DUPC",
                    "The " + (scenario.equals("PRIPE") ? "Private Person" : "IBM Employee") + " already has a record with CMR No. " + duplicateCMRNo,
                    duplicateCMRNo, zs01Kunnr);
                valid = false;
              } else {
                details.append("No Duplicate CMRs were found.").append("\n");
              }
            }
          } catch (Exception e) {
            details.append("Duplicate CMR check using customer name match failed to execute.").append("\n");
            engineData.addNegativeCheckStatus("DUPLICATE_CHECK_ERROR", "Duplicate CMR check using customer name match failed to execute.");
          }

          if (scenario.equals("IBMEM")) {
            Person person = null;
            if (StringUtils.isNotBlank(zs01.getCustNm1())) {
              try {
                String mainCustName = zs01.getCustNm1() + (StringUtils.isNotBlank(zs01.getCustNm2()) ? " " + zs01.getCustNm2() : "");
                person = BluePagesHelper.getPersonByName(insertGermanCharacters(mainCustName), data.getCmrIssuingCntry());
                person = (person == null) ? BluePagesHelper.getPersonByName(mainCustName, data.getCmrIssuingCntry()) : person;
                if (person == null) {
                  engineData.addRejectionComment("OTH", "Employee details not found in IBM People.", "", "");
                  details.append("Employee details not found in IBM People.").append("\n");
                  valid = false;
                } else {
                  details.append("Employee details validated with IBM BluePages for " + person.getName() + "(" + person.getEmail() + ").")
                      .append("\n");
                }
              } catch (Exception e) {
                LOG.error("Not able to check name against bluepages", e);
                engineData.addNegativeCheckStatus("BLUEPAGES_NOT_VALIDATED", "Not able to check name against bluepages for scenario IBM Employee.");
                valid = false;
              }
            } else {
              LOG.warn("Not able to check name against bluepages, Customer Name 1 not found on the main address");
              engineData.addNegativeCheckStatus("BLUEPAGES_NOT_VALIDATED", "Customer Name 1 not found on the main address");
              valid = false;
            }
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
            engineData.addRejectionComment("OTH", "Multiple registered CMRs already found for this customer.", "", "");
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
        engineData.addPositiveCheckStatus(AutomationEngineData.SKIP_GBG);
        engineData.addPositiveCheckStatus(AutomationEngineData.SKIP_COVERAGE);
        if (StringUtils.isNotBlank(data.getPpsceid())) {
          try {
            if (!checkPPSCEID(data.getPpsceid())) {
              engineData.addRejectionComment("OTH", "PPS CE ID on the request is invalid.", "", "");
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
            engineData.addRejectionComment("OTH", "IBM Department/Cost Center on the request is invalid.", "", "");
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
      case "3PA":
      case "X3PA":
      case "DC":
      case "XDC":
        // Duplicate CMR checks Based on Name and Addresses
        valid = duplicateCmrCheck3PADC(requestData, engineData, details, data, zs01, zi01, valid, scenario);
        break;
      }

      List<String> scenario3PA = Arrays.asList("3PA", "X3PA");
      List<String> scenarioDC = Arrays.asList("DC", "XDC");
      if (!scenario3PA.contains(scenario)) {
        for (Addr addr : requestData.getAddresses()) {
          String custNm = addr.getCustNm1() + (StringUtils.isNotBlank(addr.getCustNm2()) ? addr.getCustNm2() : "");
          if (StringUtils.isNotBlank(custNm)
              && (custNm.toUpperCase().contains("C/O") || custNm.toUpperCase().contains("C / O") || custNm.toUpperCase().contains("CARE OF"))) {
            engineData.addNegativeCheckStatus("SCENARIO_CHECK", "The scenario should be for 3rd Party");
            break;
          }
        }
      }
      // String[] skipCompanyCheckList = { "PRIPE", "IBMEM" };
      // skipCompanyCheckForScenario(requestData, engineData,
      // Arrays.asList(skipCompanyCheckList), false);

    } else {
      valid = false;
      engineData.addRejectionComment("TYPR", "Wrong type of request.", "No Scenario found on the request", "");
      details.append("No Scenario found on the request").append("\n");
    }
    return valid;
  }

  private boolean duplicateCmrCheck3PADC(RequestData requestData, AutomationEngineData engineData, StringBuilder details, Data data, Addr zs01,
      Addr zi01, boolean valid, String scenario) {
    LOG.debug("duplicateCmrCheck3PADC");
    try {
      String zs01Kunnr = null;
      String zi01Kunnr = null;
      String dupCMRzS01 = null;
      String dupCMRzI01 = null;
      for (Addr addr : requestData.getAddresses()) {
        String addrTyp = addr.getId().getAddrType();
        if (StringUtils.isNotBlank(addrTyp) && (addrTyp.equalsIgnoreCase("ZS01") || addrTyp.equalsIgnoreCase("ZI01"))) {
          String custNm = addr.getCustNm1().trim() + (StringUtils.isNotBlank(addr.getCustNm2()) ? " " + addr.getCustNm2() : "");
          String duplicateCMRNo = null;
          // getting fuzzy matches on basis of name
          MatchingServiceClient client = CmrServicesFactory.getInstance().createClient(SystemConfiguration.getValue("BATCH_SERVICES_URL"),
              MatchingServiceClient.class);
          DuplicateCMRCheckRequest request = new DuplicateCMRCheckRequest();
          request.setCustomerName(custNm);
          request.setIssuingCountry(data.getCmrIssuingCntry());
          request.setLandedCountry(addr.getLandCntry());
          request.setPostalCode(addr.getPostCd());
          request.setStateProv(addr.getStateProv());
          request.setStreetLine1(addr.getAddrTxt());
          request.setStreetLine2(addr.getAddrTxt2());
          request.setCity(addr.getCity1());
          request.setAddrType(addrTyp);
          client.setReadTimeout(1000 * 60 * 5);
          LOG.debug("Connecting to the Duplicate CMR Check Service at " + SystemConfiguration.getValue("BATCH_SERVICES_URL"));
          MatchingResponse<?> rawResponse = client.executeAndWrap(MatchingServiceClient.CMR_SERVICE_ID, request, MatchingResponse.class);
          ObjectMapper mapper = new ObjectMapper();
          String json = mapper.writeValueAsString(rawResponse);

          TypeReference<MatchingResponse<DuplicateCMRCheckResponse>> ref = new TypeReference<MatchingResponse<DuplicateCMRCheckResponse>>() {
          };

          MatchingResponse<DuplicateCMRCheckResponse> resp = mapper.readValue(json, ref);

          if (resp.getSuccess()) {
            if (resp.getMatched() && resp.getMatches().size() > 0) {
              duplicateCMRNo = resp.getMatches().get(0).getCmrNo();
              if (addrTyp.equalsIgnoreCase("ZS01")) {
                dupCMRzS01 = duplicateCMRNo;
                zs01Kunnr = getZS01Kunnr(duplicateCMRNo, SystemLocation.GERMANY);
              } else if (addrTyp.equalsIgnoreCase("ZI01")) {
                dupCMRzI01 = duplicateCMRNo;
                zi01Kunnr = getZI01Kunnr(duplicateCMRNo, SystemLocation.GERMANY);
              }
            }
          }
        }
      }
      if (zs01Kunnr != null && zi01Kunnr != null && StringUtils.isNotBlank(dupCMRzS01) && StringUtils.isNotBlank(dupCMRzI01)
          && dupCMRzS01.equals(dupCMRzI01)) {
        details.append("The " + ((scenario.equals("3PA") || scenario.equals("X3PA")) ? "Third Party Person" : "Data Center")
            + " already has a record with CMR No. for Sold-To " + dupCMRzS01 + " and for Install-At " + dupCMRzI01).append("\n");
        engineData.addRejectionComment("DUPC", "The " + ((scenario.equals("3PA") || scenario.equals("X3PA")) ? "Third Party Person" : "Data Center")
            + " already has a record with CMR No. for Sold-To " + dupCMRzS01, dupCMRzS01, zs01Kunnr);
        engineData.addRejectionComment("DUPC", "The " + ((scenario.equals("3PA") || scenario.equals("X3PA")) ? "Third Party Person" : "Data Center")
            + " already has a record with CMR No. for Install-At " + dupCMRzI01, dupCMRzI01, zi01Kunnr);
        valid = false;
      } else {
        details.append("No Duplicate CMRs were found.").append("\n");
      }
    } catch (Exception e) {
      details.append("Duplicate CMR check using customer name match failed to execute.").append("\n");
      engineData.addNegativeCheckStatus("DUPLICATE_CHECK_ERROR", "Duplicate CMR check using customer name match failed to execute.");
    }
    // Sold-To and Install-At cannot be same for these scenarios
    if (zs01 != null && zi01 != null && addressEquals(zs01, zi01)) {
      engineData.addRejectionComment("OTH", "For this scenario, Sold-to and Install-at need to be different", "", "");
      details.append("For 3rd Party and Data Center Sold-To and Install-At address should be different");
      return false;
    }
    return valid;
  }
  
    @Override
  public void filterDuplicateCMRMatches(EntityManager entityManager, RequestData requestData, AutomationEngineData engineData,
      MatchingResponse<DuplicateCMRCheckResponse> response) {

    String[] scenariosToBeChecked = { "PRIPE", "IBMEM" };
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
          if (Arrays.asList(kuklaPriv).contains(kukla) && ("PRIPE".equals(scenario))) {
            filteredMatches.add(match);
          } else if (Arrays.asList(kuklaIBMEM).contains(kukla) && ("IBMEM".equals(scenario))) {
            filteredMatches.add(match);
          }
        }

      }
      // set filtered matches in response
      response.setMatches(filteredMatches);
    }
  }

  @Override
  public String getAddressTypeForGbgCovCalcs(EntityManager entityManager, RequestData requestData, AutomationEngineData engineData) throws Exception {
    Data data = requestData.getData();
    String scenario = data.getCustSubGrp();
    String address = "ZS01";

    LOG.debug("Address for the scenario to check: " + scenario);
    if ("3PA".equals(scenario) || "X3PA".equals(scenario)) {
      address = "ZI01";
    }
    return address;
  }

  @Override
  public AutomationResult<OverrideOutput> doCountryFieldComputations(EntityManager entityManager, AutomationResult<OverrideOutput> results,
      StringBuilder details, OverrideOutput overrides, RequestData requestData, AutomationEngineData engineData) throws Exception {
    Data data = requestData.getData();
    Admin admin = requestData.getAdmin();

    if ("3PA".contains(data.getCustSubGrp()) || "X3PA".contains(data.getCustSubGrp())) {
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
    }

    /*
     * Addr zs01 = requestData.getAddress("ZS01"); Admin admin =
     * requestData.getAdmin(); boolean payGoAddredited =
     * RequestUtils.isPayGoAccredited(entityManager, admin.getSourceSystId());
     * String custNm1 = StringUtils.isNotBlank(zs01.getCustNm1()) ?
     * zs01.getCustNm1().trim() : ""; String custNm2 =
     * StringUtils.isNotBlank(zs01.getCustNm2()) ? zs01.getCustNm2().trim() :
     * ""; String mainCustNm = (custNm1 + (StringUtils.isNotBlank(custNm2) ? " "
     * + custNm2 : "")).toUpperCase(); String mainStreetAddress1 =
     * (StringUtils.isNotBlank(zs01.getAddrTxt()) ? zs01.getAddrTxt() :
     * "").trim().toUpperCase(); String mainCity =
     * (StringUtils.isNotBlank(zs01.getCity1()) ? zs01.getCity1() :
     * "").trim().toUpperCase(); String mainPostalCd =
     * (StringUtils.isNotBlank(zs01.getPostCd()) ? zs01.getPostCd() :
     * "").trim(); String mainExtWalletId =
     * (StringUtils.isNotBlank(zs01.getExtWalletId()) ? zs01.getExtWalletId() :
     * "").trim(); Iterator<Addr> it = requestData.getAddresses().iterator();
     * boolean removed = false;
     * details.append("Checking for duplicate address records - ").append("\n");
     * while (it.hasNext()) { Addr addr = it.next(); if (!payGoAddredited) { if
     * (!"ZS01".equals(addr.getId().getAddrType())) { removed = true; String
     * custNm = (addr.getCustNm1().trim() +
     * (StringUtils.isNotBlank(addr.getCustNm2()) ? " " +
     * addr.getCustNm2().trim() : "")) .toUpperCase(); String streetaddress =
     * ((StringUtils.isNotBlank(addr.getAddrTxt()) ? addr.getAddrTxt().trim() :
     * "")).toUpperCase(); if (custNm.equals(mainCustNm) &&
     * streetaddress.equals(mainStreetAddress1) &&
     * addr.getCity1().trim().toUpperCase().equals(mainCity) &&
     * addr.getPostCd().trim().equals(mainPostalCd)) {
     * details.append("Removing duplicate address record: " +
     * addr.getId().getAddrType() + " from the request.").append("\n"); Addr
     * merged = entityManager.merge(addr); if (merged != null) {
     * entityManager.remove(merged); } it.remove(); } } } else { if
     * (!"ZS01".equals(addr.getId().getAddrType())) { removed = true; String
     * custNm = (addr.getCustNm1().trim() +
     * (StringUtils.isNotBlank(addr.getCustNm2()) ? " " +
     * addr.getCustNm2().trim() : "")) .toUpperCase(); if
     * (custNm.equals(mainCustNm) &&
     * addr.getAddrTxt().trim().toUpperCase().equals(mainStreetAddress1) &&
     * addr.getCity1().trim().toUpperCase().equals(mainCity) &&
     * addr.getPostCd().trim().equals(mainPostalCd) &&
     * zs01.getExtWalletId().trim().toUpperCase().equals(mainExtWalletId)) {
     * details.append("Removing duplicate address record: " +
     * addr.getId().getAddrType() + " from the request.").append("\n"); Addr
     * merged = entityManager.merge(addr); if (merged != null) {
     * entityManager.remove(merged); } it.remove(); } } } }
     * 
     * if (!removed) {
     * details.append("No duplicate address records found on the request.").
     * append("\n"); }
     */

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
    String scenario = data.getCustSubGrp();
    String coverageId = container.getFinalCoverage();
    String coverage = data.getSearchTerm();
    String bgId = data.getBgId();
    List<String> covList = Arrays.asList("A0004520", "A0004515", "A0004541", "A0004580");
    LOG.debug("CovereageID -> " + coverageId);

    details.append("\n");

    String sbo = "";
    String isuCd = null;
    String clientTier = null;
    if (StringUtils.isNotBlank(container.getIsuCd())) {
      isuCd = container.getIsuCd();
      clientTier = container.getClientTierCd();
    } else {
      isuCd = data.getIsuCd();
      clientTier = data.getClientTier();
    }

    if (bgId != null && !"BGNONE".equals(bgId.trim())) {
      List<DACHFieldContainer> queryResults = AutomationUtil.computeDACHCoverageElements(entityManager, "AUTO.COV.CALCULATE_COV_ELEMENTS_DACH", bgId,
          data.getCmrIssuingCntry());
      if (queryResults != null && !queryResults.isEmpty()) {
        for (DACHFieldContainer result : queryResults) {
          DACHFieldContainer queryResult = (DACHFieldContainer) result;
          String containerCtc = StringUtils.isBlank(container.getClientTierCd()) ? "" : container.getClientTierCd();
          String containerIsu = StringUtils.isBlank(container.getIsuCd()) ? "" : container.getIsuCd();
          String queryIsu = queryResult.getIsuCd();
          String queryCtc = queryResult.getClientTier();
          if (containerIsu.equals(queryIsu) && containerCtc.equals(queryCtc)) {
            overrides.addOverride(AutomationElementRegistry.GBL_CALC_COV, "DATA", "SEARCH_TERM", data.getSearchTerm(), queryResult.getSearchTerm());
            overrides.addOverride(AutomationElementRegistry.GBL_CALC_COV, "DATA", "INAC_CD", data.getInacCd(), queryResult.getInac());
            overrides.addOverride(AutomationElementRegistry.GBL_CALC_COV, "DATA", "ENTERPRISE", data.getEnterprise(), queryResult.getEnterprise());
            details.append("Data calculated based on CMR Data:").append("\n");
            details.append(" - SORTL = " + queryResult.getSearchTerm()).append("\n");
            details.append(" - INAC Code = " + queryResult.getInac()).append("\n");
            details.append(" - Enterprise = " + queryResult.getEnterprise()).append("\n");
            engineData.addPositiveCheckStatus(AutomationEngineData.COVERAGE_CALCULATED);
            results.setResults("Calculated");
            break;
          }
        }
      }
    } else {
      sbo = getSBOFromIMS(entityManager, data.getSubIndustryCd(), isuCd, clientTier);
      if (StringUtils.isNotBlank(sbo)) {
        details.append("Setting SBO to " + sbo + " based on IMS mapping rules.");
        overrides.addOverride(AutomationElementRegistry.GBL_CALC_COV, "DATA", "SEARCH_TERM", data.getSearchTerm(), sbo);
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
      } else {
        details.setLength(0);
        overrides.clearOverrides();
        details.append("Coverage could not be calculated IMS Mapping rule.\n Skipping coverage calculation.").append("\n");
        results.setResults("Skipped");
      }
    }

    /*
     * HashMap<String, String> response =
     * getSORTLFromPostalCodeMapping(data.getSubIndustryCd(), zs01.getPostCd(),
     * data.getIsuCd(), data.getClientTier()); LOG.debug("Calculated SORTL: " +
     * response.get(SORTL)); if (StringUtils.isNotBlank(response.get(MATCHING)))
     * { switch (response.get(MATCHING)) { case "Exact Match": case
     * "Nearest Match":
     * overrides.addOverride(AutomationElementRegistry.GBL_CALC_COV, "DATA",
     * "SEARCH_TERM", data.getSearchTerm(), response.get(SORTL));
     * details.append("Coverage calculation Successful.").append("\n");
     * details.append("Computed SORTL = " + response.get(SORTL)).append("\n\n");
     * details.append("Matched Rule:").append("\n");
     * details.append("Sub Industry = " + data.getSubIndustryCd()).append("\n");
     * details.append("ISU = " + data.getIsuCd()).append("\n");
     * details.append("CTC = " + data.getClientTier()).append("\n");
     * details.append("Postal Code Range = " +
     * response.get(POSTAL_CD_RANGE)).append("\n\n");
     * details.append("Matching: " + response.get(MATCHING));
     * results.setResults("Coverage Calculated");
     * engineData.addPositiveCheckStatus(AutomationEngineData.
     * COVERAGE_CALCULATED); break; case "No Match Found":
     * engineData.addRejectionComment("OTH",
     * "Coverage cannot be computed using 34Q-PostalCode logic.", "", "");
     * details.
     * append("Coverage cannot be computed using 34Q-PostalCode logic.").
     * append("\n"); results.setResults("Coverage not calculated.");
     * results.setOnError(true); break; } } else {
     * engineData.addRejectionComment("OTH",
     * "Coverage cannot be computed using 34Q-PostalCode logic.", "", "");
     * details.
     * append("Coverage cannot be computed using 34Q-PostalCode logic.").
     * append("\n"); results.setResults("Coverage not calculated.");
     * results.setOnError(true); }
     */
    // } else {
    // }
    LOG.debug("---data.getSearchTerm---" + data.getSearchTerm());
    LOG.debug("---coverageId---" + coverageId);
    LOG.debug("---coverage---" + coverage);
    LOG.debug("Setting isu ctc to 28-7 for matched coverage from list");
    if (("COMME".equals(scenario) || "GOVMT".equals(scenario)) && StringUtils.isNotBlank(coverageId) && covList.contains(coverageId)) {
      LOG.debug("Setting isu ctc to 28-7 based on coverage mapping.");
      details.append("Setting isu ctc to 287 based on coverage mapping.");
      overrides.addOverride(covElement.getProcessCode(), "DATA", "ISU_CD", data.getIsuCd(), "28");
      overrides.addOverride(covElement.getProcessCode(), "DATA", "CLIENT_TIER", data.getClientTier(), "7");
    }
    return true;
  }

  private String getSBOFromIMS(EntityManager entityManager, String subIndustryCd, String isuCd, String clientTier) {
    List<String> sboValues = new ArrayList<>();
    String isu = StringUtils.isNotBlank(isuCd) ? isuCd : "";
    String ctc = StringUtils.isNotBlank(clientTier) ? clientTier : "";
    String ims = "";
    if (StringUtils.isNotBlank(subIndustryCd)) {
      ims = subIndustryCd.substring(0, 1);
    }
    String isuCtc = (StringUtils.isNotBlank(isuCd) ? isuCd : "") + (StringUtils.isNotBlank(clientTier) ? clientTier : "");
    if (!"34Q".equals(isuCtc)) {
      ims = "";
    }

    if (StringUtils.isNotBlank(subIndustryCd)) {
      String sql = ExternalizedQuery.getSql("AUTO.DE.GET_SBOLIST_FROM_ISUCTC");
      PreparedQuery query = new PreparedQuery(entityManager, sql);
      query.setParameter("ISU", isu);
      query.setParameter("CLIENT_TIER", ctc);
      query.setParameter("ISSUING_CNTRY", SystemLocation.GERMANY);
      query.setParameter("UPDATE_BY_ID", "%" + ims + "%");
      query.setForReadOnly(true);
      sboValues = query.getResults(String.class);
    }
    if (sboValues != null) {
      return sboValues.get(0);
    } else {
      return "";
    }
  }

  @Override
  public boolean runUpdateChecksForData(EntityManager entityManager, AutomationEngineData engineData, RequestData requestData,
      RequestChangeContainer changes, AutomationResult<ValidationOutput> output, ValidationOutput validation) throws Exception {
    Admin admin = requestData.getAdmin();
    Data data = requestData.getData();
    Addr soldTo = requestData.getAddress("ZS01");
    String details = StringUtils.isNotBlank(output.getDetails()) ? output.getDetails() : "";
    StringBuilder detail = new StringBuilder(details);
    String duns = null;
    boolean isZS01WithAufsdPG = (CmrConstants.RDC_SOLD_TO.equals(soldTo.getId().getAddrSeq()) && "PG".equals(data.getOrdBlk()));
    boolean payGoAddredited = RequestUtils.isPayGoAccredited(entityManager, admin.getSourceSystId());
    boolean isNegativeCheckNeedeed = false;
    if (changes != null && changes.hasDataChanges()) {
      if (changes.isDataChanged("VAT #")
          || (CmrConstants.RDC_SOLD_TO.equals("ZS01") && soldTo != null && isRelevantAddressFieldUpdatedZS01ZP01(changes, soldTo))) {
        UpdatedDataModel vatChange = changes.getDataChange("VAT #");
        if (isRelevantAddressFieldUpdatedZS01ZP01(changes, soldTo)
            || (vatChange != null && (StringUtils.isBlank(vatChange.getOldData()) && StringUtils.isNotBlank(vatChange.getNewData()))
                || (StringUtils.isNotBlank(vatChange.getOldData()) && StringUtils.isNotBlank(vatChange.getNewData())))) {
          // check if the name + VAT exists in D&B
          List<DnBMatchingResponse> matches = getMatches(requestData, engineData, soldTo, true);
          if (matches.isEmpty()) {
            // get DnB matches based on all address details
            matches = getMatches(requestData, engineData, soldTo, false);
          }
          String custName = soldTo.getCustNm1() + (StringUtils.isBlank(soldTo.getCustNm2()) ? "" : " " + soldTo.getCustNm2());
          if (!matches.isEmpty()) {
            for (DnBMatchingResponse dnbRecord : matches) {
              if ("Y".equals(dnbRecord.getOrgIdMatch()) && (StringUtils.isNotEmpty(custName) && StringUtils.isNotEmpty((dnbRecord.getDnbName()))
                  && StringUtils.getLevenshteinDistance(custName.toUpperCase(), dnbRecord.getDnbName().toUpperCase()) <= 5)) {
                duns = dnbRecord.getDunsNo();
                isNegativeCheckNeedeed = false;
                break;
              }
              isNegativeCheckNeedeed = true;
            }
          }
          if (isNegativeCheckNeedeed) {
            detail.append("Updates to VAT need verification as VAT and legal name doesn't matches DnB.\n");
            LOG.debug("Updates to VAT need verification as VAT and legal name doesn't matches DnB.");
          } else {
            detail.append("Updates to VAT matches DnB.\n DUNS No :" + duns + "\n");
            LOG.debug("Updates to VAT matches DnB.\n DUNS No :" + duns + "\n");
          }

        } else if (StringUtils.isNotBlank(vatChange.getOldData()) && StringUtils.isBlank(vatChange.getNewData())) {
          admin.setScenarioVerifiedIndc("N");
          entityManager.merge(admin);
          detail.append("Setting scenario verified indc= N as VAT is blank.\n");
          LOG.debug("Setting scenario verified indc= N as VAT is blank.");
        } else if (StringUtils.isNotBlank(vatChange.getOldData()) && StringUtils.isNotBlank(vatChange.getNewData())) {
          isNegativeCheckNeedeed = true;
          detail.append("Updates to VAT need verification.\n");
          LOG.debug("Updates to VAT need verification.");
        }
      } else if (changes.isDataChanged("Order Block")) {
        UpdatedDataModel OBChange = changes.getDataChange("Order Block");
        if (OBChange != null) {
          if ("88".equals(OBChange.getOldData()) || "88".equals(OBChange.getNewData()) || "94".equals(OBChange.getOldData())
              || "94".equals(OBChange.getOldData())) {
            // noop
            detail.append("Updates to the Order Block field are validated.\n");
            LOG.debug("Updates to the Order Block field are validated.");
          }
        }
      } else if (changes.isDataChanged("Abbreviated Name (TELX1)")) {
        UpdatedDataModel AbbrevNameChange = changes.getDataChange("Abbreviated Name (TELX1)");
        String oldAbbrName = insertGermanCharacters(AbbrevNameChange.getOldData());
        if (oldAbbrName.equals(AbbrevNameChange.getNewData())) {
          detail.append("Updates to the Abbreviated Name field are validated.\n");
          LOG.debug("Updates to the Abbreviated Name field are validated.");
        } else if (payGoAddredited || isZS01WithAufsdPG) {
          detail.append("Updates to the Abbreviated Name field are skipped for PayGo cmr\n");
          LOG.debug("Updates to the Abbreviated Name field are skipped for Paygo cmr ");
        } else {
          detail.append("Updates to the Abbreviated Name field  are skipped.\n");
          LOG.debug("Updates to the Abbreviated Name field  are skipped.");
        }
      } else {
        boolean otherFieldsChanged = false;
        for (UpdatedDataModel dataChange : changes.getDataUpdates()) {
          if (dataChange != null && !"CAP Record".equals(dataChange.getDataField())) {
            otherFieldsChanged = true;
            break;
          }
        }
        if (otherFieldsChanged) {
          isNegativeCheckNeedeed = true;
          detail.append("Updates to data were found, review is required.\n");
          LOG.debug("Updates to data were found, review is required.");
        }
      }

    }
    if (isNegativeCheckNeedeed) {
      validation.setSuccess(false);
      validation.setMessage("Not validated");
      engineData.addNegativeCheckStatus("UPDT_REVIEW_NEEDED", "Updated elements cannot be checked automatically.");

    } else {
      validation.setSuccess(true);
      validation.setMessage("Validated");
      if (detail.toString().isEmpty()) {
        detail.append("No data updates made on the request");
      }
    }
    output.setDetails(detail.toString());
    output.setProcessOutput(validation);
    return true;
  }

  @Override
  public boolean runUpdateChecksForAddress(EntityManager entityManager, AutomationEngineData engineData, RequestData requestData,
      RequestChangeContainer changes, AutomationResult<ValidationOutput> output, ValidationOutput validation) throws Exception {
    Data data = requestData.getData();
    Admin admin = requestData.getAdmin();
    List<Addr> addressList = requestData.getAddresses();
    boolean isInstallAtMatchesDnb = true;
    boolean isBillToMatchesDnb = true;
    boolean isSoldToMatchesDnb = true;
    boolean isNegativeCheckNeedeed = false;
    boolean isInstallAtExistOnReq = false;
    boolean isInstallAtSameAsSoldTo = false;
    boolean isBillToExistOnReq = false;
    boolean isPayGoBillToExistOnReq = false;
    boolean isShipToExistOnReq = false;
    Addr installAt = requestData.getAddress("ZI01");
    Addr billTo = requestData.getAddress("ZP01");
    Addr shipTo = requestData.getAddress("ZD01");
    Addr payGoBillTo = requestData.getAddress("PG01");
    Addr soldTo = requestData.getAddress("ZS01");
    String details = StringUtils.isNotBlank(output.getDetails()) ? output.getDetails() : "";
    StringBuilder detail = new StringBuilder(details);
    long reqId = requestData.getAdmin().getId().getReqId();
    LOG.debug("Verifying PayGo Accreditation for " + admin.getSourceSystId());
    boolean payGoAddredited = RequestUtils.isPayGoAccredited(entityManager, admin.getSourceSystId());
    if (changes != null && changes.hasAddressChanges()) {
      if (StringUtils.isNotEmpty(data.getCustClass()) && ("81".equals(data.getCustClass()) || "85".equals(data.getCustClass()))
          && !changes.hasDataChanges()) {
        LOG.debug("Skipping checks as customer class is " + data.getCustClass() + " and only address changes.");
        output.setDetails("Skipping checks as customer class is " + data.getCustClass() + " and only address changes.");
        validation.setSuccess(true);
        validation.setMessage("No Validations");
        output.setProcessOutput(validation);
        return true;
      }

      if (shipTo != null && (changes.isAddressChanged("ZD01") || isAddressAdded(shipTo))) {
        // Check If Address already exists on request
        isShipToExistOnReq = addressExists(entityManager, shipTo, requestData);
        if (isShipToExistOnReq) {
          detail.append("Ship To details provided matches an existing address.");
          validation.setMessage("ShipTo already exists");
          engineData.addRejectionComment("OTH", "Ship To details provided matches an existing address.", "", "");
          output.setOnError(true);
          validation.setSuccess(false);
          validation.setMessage("Not validated");
          output.setDetails(detail.toString());
          output.setProcessOutput(validation);
          LOG.debug("Ship To details provided matches already existing address.");
          return true;
        }
      }

      if (installAt != null && (changes.isAddressChanged("ZI01") || isAddressAdded(installAt))) {
        // Check If Address same as Sold To address
        isInstallAtSameAsSoldTo = addressExists(entityManager, installAt, requestData);
        if (isInstallAtSameAsSoldTo) {
          detail.append("Install At details provided matches an existing address.");
          engineData.addRejectionComment("OTH", "Install At details provided matches an existing address.", "", "");
          LOG.debug("Install At details provided matches an existing address.");
          output.setOnError(true);
          validation.setSuccess(false);
          validation.setMessage("Not validated");
          output.setDetails(detail.toString());
          output.setProcessOutput(validation);
          return true;
        }
      }

      if (billTo != null && (changes.isAddressChanged("ZP01") || isAddressAdded(billTo))) {
        // Check If Address already exists on request
        isBillToExistOnReq = addressExists(entityManager, billTo, requestData);
        if (isBillToExistOnReq) {
          detail.append("Bill To details provided matches an existing address.");
          engineData.addRejectionComment("OTH", "Bill To details provided matches an existing address.", "", "");
          LOG.debug("Bill To details provided matches an existing address.");
          output.setOnError(true);
          validation.setSuccess(false);
          validation.setMessage("Not validated");
          output.setDetails(detail.toString());
          output.setProcessOutput(validation);
          return true;
        }

        if (isRelevantAddressFieldUpdatedZS01ZP01(changes, billTo) || isAddressAdded(billTo)) {
          // Check if address closely matches DnB
          List<DnBMatchingResponse> matches = getMatches(requestData, engineData, billTo, false);
          if (matches != null) {
            isBillToMatchesDnb = ifaddressCloselyMatchesDnb(matches, billTo, admin, data.getCmrIssuingCntry());
            if (!isBillToMatchesDnb) {
              isNegativeCheckNeedeed = true;
              detail.append("Updates to Bill To address need verification as it does not matches D&B");
              LOG.debug("Updates to Bill To address need verification as it does not matches D&B");
            } else {
              detail.append("Updated address ZP01 (" + billTo.getId().getAddrSeq() + ") matches D&B records. Matches:\n");
              for (DnBMatchingResponse dnb : matches) {
                detail.append(" - DUNS No.:  " + dnb.getDunsNo() + " \n");
                detail.append(" - Name.:  " + dnb.getDnbName() + " \n");
                detail.append(" - Address:  " + dnb.getDnbStreetLine1() + " " + dnb.getDnbCity() + " " + dnb.getDnbPostalCode() + " "
                    + dnb.getDnbCountry() + "\n\n");
              }
            }
          }
        }
      }

      if (payGoBillTo != null && (changes.isAddressChanged("PG01") || isAddressAdded(payGoBillTo))) {
        // Check If Address already exists on request
        isPayGoBillToExistOnReq = addressExists(entityManager, payGoBillTo, requestData);
        if (isPayGoBillToExistOnReq) {
          detail.append(" PayGo Billing details provided matches an existing address.");
          engineData.addRejectionComment("OTH", "PayGo Billing details provided matches an existing address.", "", "");
          LOG.debug("PayGo Billing details provided matches an existing address.");
          output.setOnError(true);
          validation.setSuccess(false);
          validation.setMessage("Not validated");
          output.setDetails(detail.toString());
          output.setProcessOutput(validation);
          return true;
        }
      }

      if (CmrConstants.RDC_SOLD_TO.equals("ZS01") && soldTo != null && isRelevantAddressFieldUpdatedZS01ZP01(changes, soldTo)) {
        // Check if address closely matches DnB
        List<DnBMatchingResponse> matches = getMatches(requestData, engineData, soldTo, false);
        if (matches != null) {
          isSoldToMatchesDnb = ifaddressCloselyMatchesDnb(matches, soldTo, admin, data.getCmrIssuingCntry());
          if (!isSoldToMatchesDnb) {
            isNegativeCheckNeedeed = true;
            detail.append("Updates to Sold To address need verification as it does not matches D&B");
            LOG.debug("Updates to Sold To address need verification as it does not matches D&B");
          } else {
            detail.append("Updated address ZS01 (" + soldTo.getId().getAddrSeq() + ") matches D&B records. Matches:\n");
            for (DnBMatchingResponse dnb : matches) {
              detail.append(" - DUNS No.:  " + dnb.getDunsNo() + " \n");
              detail.append(" - Name.:  " + dnb.getDnbName() + " \n");
              detail.append(" - Address:  " + dnb.getDnbStreetLine1() + " " + dnb.getDnbCity() + " " + dnb.getDnbPostalCode() + " "
                  + dnb.getDnbCountry() + "\n\n");
            }
          }
        }
      }
      if (isNegativeCheckNeedeed || isShipToExistOnReq || isInstallAtExistOnReq || isBillToExistOnReq || isPayGoBillToExistOnReq) {
        validation.setSuccess(false);
        validation.setMessage("Not validated");
        engineData.addNegativeCheckStatus("UPDT_REVIEW_NEEDED", "Updated elements cannot be checked automatically.");
      } else {
        for (String addrType : RELEVANT_ADDRESSES) {
          if (changes.isAddressChanged(addrType)) {
            if (CmrConstants.RDC_SOLD_TO.equals(addrType)) {
              addressList = Collections.singletonList(requestData.getAddress(CmrConstants.RDC_SOLD_TO));
            } else {
              addressList = requestData.getAddresses(addrType);
            }

            for (Addr addr : addressList) {
              List<String> addrTypesChanged = new ArrayList<String>();
              for (UpdatedNameAddrModel addrModel : changes.getAddressUpdates()) {
                if (!addrTypesChanged.contains(addrModel.getAddrTypeCode())) {
                  addrTypesChanged.add(addrModel.getAddrTypeCode());
                }
              }
              boolean isZS01WithAufsdPG = (CmrConstants.RDC_SOLD_TO.equals(addrType) && "PG".equals(data.getOrdBlk()));
              if ("Y".equals(addr.getImportInd())) {
                if ((payGoAddredited && addrTypesChanged.contains(CmrConstants.RDC_PAYGO_BILLING.toString())) || isZS01WithAufsdPG) {
                  validation.setSuccess(true);
                  LOG.debug("Updates to PG01 addresses fields is found.Updates verified.");
                  detail.append("Updates to PG01 addresses found but have been marked as Verified.");
                  isNegativeCheckNeedeed = false;
                } else if (CmrConstants.RDC_SOLD_TO.equals(addrType) && soldTo != null
                    && (isNonRelevantAddressFieldUpdatedForZS01ZP01(changes, soldTo) || isRelevantAddressFieldUpdatedZS01ZP01(changes, soldTo))) {
                  validation.setSuccess(true);
                  LOG.debug("Updates to relevant addresses is found.Updates verified.");
                  detail.append("Updates to relevant addresses found but have been marked as Verified.");
                  validation.setMessage("Validated");
                  isNegativeCheckNeedeed = false;
                  break;

                } else if (CmrConstants.RDC_BILL_TO.equals(addrType) && billTo != null
                    && (isNonRelevantAddressFieldUpdatedForZS01ZP01(changes, billTo) || isRelevantAddressFieldUpdatedZS01ZP01(changes, billTo))) {
                  validation.setSuccess(true);
                  LOG.debug("Updates to relevant addresses is found.Updates verified.");
                  detail.append("Updates to relevant addresses found but have been marked as Verified.");
                  validation.setMessage("Validated");
                  isNegativeCheckNeedeed = false;
                  break;

                } else if (!isRelevantAddressFieldUpdated(changes, addr)) {
                  validation.setSuccess(true);
                  LOG.debug("Updates to relevant addresses fields is found.Updates verified.");
                  detail.append("Updates to relevant addresses found but have been marked as Verified.");
                  validation.setMessage("Validated");
                  isNegativeCheckNeedeed = false;
                  break;
                } else if (isRelevantAddressFieldUpdated(changes, addr)) {
                  isNegativeCheckNeedeed = true;
                }
              }
            }
          }
        }

        if (isNegativeCheckNeedeed) {
          detail.append("Updates to addresses found which cannot be checked automatically.");
          LOG.debug("Updates to addresses found which cannot be checked automatically.");
          validation.setSuccess(false);
          validation.setMessage("Not validated");
          engineData.addNegativeCheckStatus("UPDT_REVIEW_NEEDED", "Updated elements cannot be checked automatically.");
        } else {
          LOG.debug("Address changes don't need review");
          if (changes.hasAddressChanges()) {
            validation.setMessage("Validated");
            detail.append("Address changes were found. No further review required.");
          } else {
            validation.setMessage("Validated");
            detail.append("No Address changes found on the request.");
          }
          if (StringUtils.isBlank(output.getResults())) {
            output.setResults("Validated");
          }
          validation.setSuccess(true);
        }

      }

    }
    output.setDetails(detail.toString());
    output.setProcessOutput(validation);
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

  private boolean isOnlyDnBRelevantFieldUpdated(RequestChangeContainer changes, String addrTypeCode) {
    boolean isDnBRelevantFieldUpdated = false;
    String[] addressFields = { "Customer Name 1", "Country (Landed)", "Street Address", "Postal Code", "City" };
    List<String> relevantFieldNames = Arrays.asList(addressFields);
    for (String fieldId : relevantFieldNames) {
      UpdatedNameAddrModel addressChange = changes.getAddressChange(addrTypeCode, fieldId);
      if (addressChange != null) {
        isDnBRelevantFieldUpdated = true;
        break;
      }
    }
    return isDnBRelevantFieldUpdated;
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
    if (result != null || bgId.equals("DB500JRX")) {
      LOG.debug("Setting isu-ctc to 34Y and sortl based on gbg matching.");
      details.append("Setting isu-ctc to 34Y and sortl based on gbg matching.");
      overrides.addOverride(covElement.getProcessCode(), "DATA", "ISU_CD", data.getIsuCd(), "34");
      overrides.addOverride(covElement.getProcessCode(), "DATA", "CLIENT_TIER", data.getClientTier(), "Y");
      overrides.addOverride(covElement.getProcessCode(), "DATA", "SEARCH_TERM", data.getSearchTerm(), "T0007970");
    }
    if (result != null || bgId.equals("DB502GQG")) {
      LOG.debug("Setting isu ctc to 5K based on gbg matching.");
      details.append("Setting isu ctc to 5K based on gbg matching.");
      overrides.addOverride(covElement.getProcessCode(), "DATA", "ISU_CD", data.getIsuCd(), "5K");
      overrides.addOverride(covElement.getProcessCode(), "DATA", "CLIENT_TIER", data.getClientTier(), "");
    }
    LOG.debug("isu" + data.getIsuCd());
    LOG.debug("client tier" + data.getClientTier());
    LOG.debug("sortl" + data.getSearchTerm());
  }

  @Override
  public List<String> getSkipChecksRequestTypesforCMDE() {
    return Arrays.asList("C", "U", "M");
  }

  private boolean isRelevantAddressFieldUpdatedZS01ZP01(RequestChangeContainer changes, Addr addr) {
    List<UpdatedNameAddrModel> addrChanges = changes.getAddressChanges(addr.getId().getAddrType(), addr.getId().getAddrSeq());
    if (addrChanges == null) {
      return false;
    }
    for (UpdatedNameAddrModel change : addrChanges) {
      if (RELEVANT_ADDRESS_FIELDS_ZS01_ZP01.contains(change.getDataField())) {
        return true;
      }
    }
    return false;
  }

  private boolean isNonRelevantAddressFieldUpdatedForZS01ZP01(RequestChangeContainer changes, Addr addr) {
    List<UpdatedNameAddrModel> addrChanges = changes.getAddressChanges(addr.getId().getAddrType(), addr.getId().getAddrSeq());
    if (addrChanges == null) {
      return false;
    }
    for (UpdatedNameAddrModel change : addrChanges) {
      if (NON_RELEVANT_ADDRESS_FIELDS_ZS01_ZP01.contains(change.getDataField())) {
        return true;
      }
    }
    return false;
  }

}
