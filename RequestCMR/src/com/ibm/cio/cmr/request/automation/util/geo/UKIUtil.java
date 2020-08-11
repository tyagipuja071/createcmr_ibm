package com.ibm.cio.cmr.request.automation.util.geo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;
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
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.model.window.UpdatedDataModel;
import com.ibm.cio.cmr.request.model.window.UpdatedNameAddrModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.util.BluePagesHelper;
import com.ibm.cio.cmr.request.util.RequestUtils;
import com.ibm.cio.cmr.request.util.SystemLocation;
import com.ibm.cio.cmr.request.util.SystemParameters;
import com.ibm.cio.cmr.request.util.dnb.DnBUtil;
import com.ibm.cmr.services.client.dnb.DnBCompany;
import com.ibm.cmr.services.client.matching.MatchingResponse;
import com.ibm.cmr.services.client.matching.cmr.DuplicateCMRCheckResponse;
import com.ibm.cmr.services.client.matching.dnb.DnBMatchingResponse;
import com.ibm.cmr.services.client.matching.gbg.GBGFinderRequest;

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
  private static final List<String> RELEVANT_ADDRESSES = Arrays.asList(CmrConstants.RDC_SOLD_TO, CmrConstants.RDC_BILL_TO,
      CmrConstants.RDC_INSTALL_AT, CmrConstants.RDC_SHIP_TO, CmrConstants.RDC_SECONDARY_SOLD_TO);
  private static final List<String> NON_RELEVANT_ADDRESS_FIELDS = Arrays.asList("Attn", "Phone #", "Hardware Master");

  @Override
  public boolean performScenarioValidation(EntityManager entityManager, RequestData requestData, AutomationEngineData engineData,
      AutomationResult<ValidationOutput> result, StringBuilder details, ValidationOutput output) {
    Data data = requestData.getData();
    Admin admin = requestData.getAdmin();
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
    case SCENARIO_IGF:
    case SCENARIO_CROSS_IGF:
      boolean requesterFromTeam = BluePagesHelper.isBluePagesHeirarchyManager(admin.getRequesterId(), SystemParameters.getList("UKI.SKIP_SCENARIO"));
      if (!requesterFromTeam) {
        details.append("Requester is not allowed to submit the request for IGF Scenario.").append("\n");
        engineData.addRejectionComment("OTH", "Requester is not allowed to submit the request for IGF Scenario.", "", "");
        return false;
      }
    }
    return true;
  }

  @Override
  protected List<String> getCountryLegalEndings() {
    return Arrays.asList("LLP", "LTD", "Ltd.", "CIC", "CIO", "Cyf", "CCC", "Unltd.", "Ultd.");
  }

  @Override
  public boolean runUpdateChecksForData(EntityManager entityManager, AutomationEngineData engineData, RequestData requestData,
      RequestChangeContainer changes, AutomationResult<ValidationOutput> output, ValidationOutput validation) throws Exception {
    Admin admin = requestData.getAdmin();
    Data data = requestData.getData();
    if (handlePrivatePersonRecord(entityManager, admin, output, validation, engineData)) {
      return true;
    }
    StringBuilder details = new StringBuilder();
    boolean cmdeReview = false;
    Set<String> resultCodes = new HashSet<String>();// D for Reject
    List<String> ignoredUpdates = new ArrayList<String>();
    for (UpdatedDataModel change : changes.getDataUpdates()) {
      switch (change.getDataField()) {
      case "VAT #":
        if (!StringUtils.isBlank(change.getNewData()) && !(change.getOldData().equals(change.getNewData()))) {
          // ADD OR UPDATE
          Addr soldTo = requestData.getAddress(CmrConstants.RDC_SOLD_TO);
          List<DnBMatchingResponse> matches = getMatches(requestData, engineData, soldTo, true);
          boolean matchesDnb = false;
          if (matches != null) {
            // check against D&B
            matchesDnb = ifaddressCloselyMatchesDnb(matches, soldTo, admin, data.getCmrIssuingCntry());
          }
          if (!matchesDnb) {
            resultCodes.add("R");// Reject
            details.append("VAT # on the request did not match D&B\n");
          } else {
            details.append("VAT # on the request matches D&B\n");
          }
        }
        break;
      case "INAC/NAC Code":
      case "Company Number":
        cmdeReview = true;
        break;
      case "Tax Code":
        // noop, for switch handling only
        break;
      case "ISU Code":
        // noop, for switch handling only
        break;
      case "Client Tier":
        // noop, for switch handling only
        break;
      case "Sales Rep No":
        // noop, for switch handling only
        break;
      case "Order Block Code":
        if ("94".equals(change.getOldData()) || "94".equals(change.getNewData()) || "88".equals(change.getOldData())
            || "88".equals(change.getNewData())) {
          // noop, for switch handling only
        }
        break;
      default:
        ignoredUpdates.add(change.getDataField());
        break;
      }
    }
    if (resultCodes.contains("R")) {
      output.setOnError(true);
      validation.setSuccess(false);
      validation.setMessage("Rejected");
    } else if (cmdeReview) {
      engineData.addNegativeCheckStatus("_esDataCheckFailed", "Updates to one or more fields cannot be validated.");
      details.append("Updates to one or more fields cannot be validated.\n");
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
    if (handlePrivatePersonRecord(entityManager, admin, output, validation, engineData)) {
      return true;
    }
    List<Addr> addresses = null;
    StringBuilder checkDetails = new StringBuilder();
    Set<String> resultCodes = new HashSet<String>();// R - review
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
            if (CmrConstants.RDC_SHIP_TO.equals(addrType) || CmrConstants.RDC_SECONDARY_SOLD_TO.equals(addrType)) {
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
              if (null == changes.getAddressChange("ZS01", "Customer Name") && null == changes.getAddressChange("ZS01", "Customer Name Con't")) {
                if (addressExists(entityManager, addr)) {
                  LOG.debug(" - Duplicates found for " + addrType + "(" + addr.getId().getAddrSeq() + ")");
                  checkDetails.append("Address " + addrType + "(" + addr.getId().getAddrSeq() + ") provided matches an existing address.\n");
                  resultCodes.add("R");
                } else {
                  LOG.debug("Addition of " + addrType + "(" + addr.getId().getAddrSeq() + ")");
                  checkDetails.append("Addition of new address (" + addr.getId().getAddrSeq() + ") address skipped in the checks.\n");
                }
              } else {
                LOG.debug("New address " + addrType + "(" + addr.getId().getAddrSeq() + ") needs to be verified");
                checkDetails.append("New address " + addrType + "(" + addr.getId().getAddrSeq() + ") has Sold-To name changed \n");
                resultCodes.add("R");
              }
            }
          } else if ("Y".equals(addr.getChangedIndc())) {
            // update address
            if (CmrConstants.RDC_INSTALL_AT.equals(addrType)) {
              if (isRelevantAddressFieldUpdated(changes, addr)) {
                // CMDE Review
                checkDetails.append("Updates to address fields for " + addrType + "(" + addr.getId().getAddrSeq() + ") need to be verified.")
                    .append("\n");
                resultCodes.add("D");
              } else {
                checkDetails.append("Updates to non-address fields for " + addrType + "(" + addr.getId().getAddrSeq() + ") skipped in the checks.")
                    .append("\n");
              }

            } else if (CmrConstants.RDC_SOLD_TO.equals(addrType)) {
              if (isRelevantAddressFieldUpdated(changes, addr)) {
                Addr soldTo = requestData.getAddress(CmrConstants.RDC_SOLD_TO);
                List<DnBMatchingResponse> matches = getMatches(requestData, engineData, soldTo, true);
                boolean matchesDnb = false;
                if (matches != null) {
                  // check against D&B
                  matchesDnb = ifaddressCloselyMatchesDnb(matches, addr, admin, data.getCmrIssuingCntry());
                }
                if (!matchesDnb) {
                  LOG.debug("Update address for " + addrType + "(" + addr.getId().getAddrSeq() + ") does not match D&B");
                  resultCodes.add("R");
                  checkDetails.append("Update address " + addrType + "(" + addr.getId().getAddrSeq() + ") did not match D&B records.\n");
                } else {
                  checkDetails.append("Update address " + addrType + "(" + addr.getId().getAddrSeq() + ") matches D&B records. Matches:\n");
                  for (DnBMatchingResponse dnb : matches) {
                    checkDetails.append(" - DUNS No.:  " + dnb.getDunsNo() + " \n");
                    checkDetails.append(" - Name.:  " + dnb.getDnbName() + " \n");
                    checkDetails.append(" - Address:  " + dnb.getDnbStreetLine1() + " " + dnb.getDnbCity() + " " + dnb.getDnbPostalCode() + " "
                        + dnb.getDnbCountry() + "\n\n");
                  }
                }
              } else {
                checkDetails.append("Updates to non-address fields for " + addrType + "(" + addr.getId().getAddrSeq() + ") skipped in the checks.")
                    .append("\n");
              }
            } else {
              // proceed
              LOG.debug("Update to Address " + addrType + "(" + addr.getId().getAddrSeq() + ") skipped in the checks.\\n");
              checkDetails.append("Updates to Address (" + addr.getId().getAddrSeq() + ") skipped in the checks.\n");
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
      engineData.addNegativeCheckStatus("_atCheckFailed", "Updated to Installing Address cannot be checked automatically.");
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

  /**
   * Checks if relevant fields were updated
   * 
   * @param changes
   * @param addr
   * @return
   */
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
      boolean highQualityMatchExists = false;
      List<DnBMatchingResponse> response = getMatches(requestData, engineData, zi01, false);
      if (response != null && response.size() > 0) {
        // actions to be performed only when matches with high confidence are
        // found
        for (DnBMatchingResponse dnbRecord : response) {
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
    } else if (SCENARIO_INTERNAL.equalsIgnoreCase(scenario)) {
      // check value of Department under '##UKIInternalDepartment' LOV
      String sql = ExternalizedQuery.getSql("UKI.CHECK_DEPARTMENT");
      PreparedQuery query = new PreparedQuery(entityManager, sql);
      String dept = data.getIbmDeptCostCenter();
      if (StringUtils.isNotBlank(dept)) {
        query.setParameter("CD", dept);
        query.setParameter("CNTRY", data.getCmrIssuingCntry());
        String result = query.getSingleResult(String.class);
        if (result == null) {
          engineData.addRejectionComment("OTH", "IBM Department/Cost Center on the request is invalid.", "", "");
          details.append("IBM Department/Cost Center on the request is invalid.").append("\n");
        } else {
          details.append("IBM Department/Cost Center " + dept + " validated successfully.").append("\n");
          results.setResults("IBM Department/Cost Center " + dept + " validated successfully.");
        }
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
      response.setMatches(filteredMatches);
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

  @Override
  public String getAddressTypeForGbgCovCalcs(EntityManager entityManager, RequestData requestData, AutomationEngineData engineData) throws Exception {
    Data data = requestData.getData();
    String scenario = data.getCustSubGrp();
    String address = "ZS01";

    LOG.debug("Address for the scenario to check: " + scenario);
    if (SCENARIO_THIRD_PARTY.equals(scenario) || SCENARIO_INTERNAL_FSL.equals(scenario)) {
      address = "ZI01";
    }
    return address;
  }

  @Override
  public void tweakGBGFinderRequest(EntityManager entityManager, GBGFinderRequest request, RequestData requestData, AutomationEngineData engineData) {
    Admin admin = requestData.getAdmin();
    Data data = requestData.getData();
    String scenario = data.getCustSubGrp();
    if ("C".equals(admin.getReqType()) && (SCENARIO_THIRD_PARTY.equals(scenario) || SCENARIO_INTERNAL_FSL.equals(scenario))) {
      DnBMatchingResponse dnbRecord = (DnBMatchingResponse) engineData.get("ZI01_DNB_MATCH");
      if (dnbRecord != null) {
        request.setDunsNo(dnbRecord.getDunsNo());
      }
    }

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

  @Override
  public GBGFinderRequest createRequest(Admin admin, Data data, Addr addr, Boolean isOrgIdMatchOnly) {
    GBGFinderRequest request = super.createRequest(admin, data, addr, isOrgIdMatchOnly);
    if (SCENARIO_THIRD_PARTY.equals(data.getCustSubGrp()) || SCENARIO_INTERNAL_FSL.equals(data.getCustSubGrp())) {
      String custNmTrimmed = getCustomerFullName(addr);
      if (custNmTrimmed.toUpperCase().matches("^VR[0-9]{3}\\.+$") || custNmTrimmed.toUpperCase().matches("^VR[0-9]{3}/.+$")) {
        custNmTrimmed = custNmTrimmed.substring(6);
      } else if (custNmTrimmed.toUpperCase().matches("^VR[0-9]{3}.+$")) {
        custNmTrimmed = custNmTrimmed.substring(5);
      }
      request.setCustomerName(custNmTrimmed);
    }
    return request;
  }

}
