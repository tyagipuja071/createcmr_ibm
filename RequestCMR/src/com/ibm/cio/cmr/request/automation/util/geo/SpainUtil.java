/**
 * 
 */
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
import com.ibm.cio.cmr.request.util.RequestUtils;
import com.ibm.cio.cmr.request.util.SystemLocation;
import com.ibm.cio.cmr.request.util.dnb.DnBUtil;
import com.ibm.cmr.services.client.dnb.DnBCompany;
import com.ibm.cmr.services.client.matching.MatchingResponse;
import com.ibm.cmr.services.client.matching.cmr.DuplicateCMRCheckResponse;
import com.ibm.cmr.services.client.matching.dnb.DnBMatchingResponse;
import com.ibm.cmr.services.client.matching.gbg.GBGFinderRequest;

/**
 * {@link AutomationUtil} for Spain specific validations
 * 
 *
 */
public class SpainUtil extends AutomationUtil {

  private static final Logger LOG = Logger.getLogger(SpainUtil.class);
  public static final String SCENARIO_COMMERCIAL = "COMME";
  public static final String SCENARIO_IGS_GSE = "IGSGS";
  public static final String SCENARIO_BUSINESS_PARTNER = "BUSPR";
  public static final String SCENARIO_PRIVATE_CUSTOMER = "PRICU";
  public static final String SCENARIO_THIRD_PARTY = "THDPT";
  public static final String SCENARIO_THIRD_PARTY_IG = "THDIG";
  public static final String SCENARIO_INTERNAL = "INTER";
  public static final String SCENARIO_INTERNAL_SO = "INTSO";
  public static final String SCENARIO_CROSSBORDER = "XCRO";
  public static final String SCENARIO_CROSSBORDER_BP = "XBP";

  private static final List<String> RELEVANT_ADDRESSES = Arrays.asList(CmrConstants.RDC_SOLD_TO, CmrConstants.RDC_BILL_TO,
      CmrConstants.RDC_INSTALL_AT, CmrConstants.RDC_SHIP_TO, CmrConstants.RDC_SECONDARY_SOLD_TO);
  private static final List<String> SCENARIOS_TO_SKIP_COVERAGE = Arrays.asList(SCENARIO_INTERNAL, SCENARIO_INTERNAL_SO, SCENARIO_BUSINESS_PARTNER,
      SCENARIO_CROSSBORDER_BP);

  @Override
  public boolean performScenarioValidation(EntityManager entityManager, RequestData requestData, AutomationEngineData engineData,
      AutomationResult<ValidationOutput> result, StringBuilder details, ValidationOutput output) {
    Data data = requestData.getData();
    String scenario = data.getCustSubGrp();
    Addr soldTo = requestData.getAddress("ZS01");
    String customerName = soldTo.getCustNm1() + (!StringUtils.isBlank(soldTo.getCustNm2()) ? " " + soldTo.getCustNm2() : "");
    if (StringUtils.isBlank(scenario)) {
      details.append("Scenario not correctly specified on the request");
      engineData.addNegativeCheckStatus("_atNoScenario", "Scenario not correctly specified on the request");
      return true;
    }
    LOG.info("Starting scenario validations for Request ID " + data.getId().getReqId());

    LOG.debug("Scenario to check: " + scenario);

    if ("C".equals(requestData.getAdmin().getReqType())) {
      if (!SCENARIO_THIRD_PARTY.equals(scenario) && !SCENARIO_THIRD_PARTY_IG.equals(scenario)) {
        if (!addressEquals(requestData.getAddress("ZS01"), requestData.getAddress("ZI01"))) {
          engineData.addRejectionComment("SCENARIO_CHECK", "3rd Party should be selected.", "", "");
          details.append("Scenario should be 3rd Party");
          return false;

        }
      } else {
        if (addressEquals(requestData.getAddress("ZS01"), requestData.getAddress("ZI01"))) {
          engineData.addRejectionComment("SCENARIO_CHECK", "Billing and installing address should be different.", "", "");
          details.append("For 3rd Party Billing and installing address should be different");
          return false;

        }
      }
    }

    switch (scenario) {
    case SCENARIO_PRIVATE_CUSTOMER:
      return doPrivatePersonChecks(engineData, SystemLocation.SPAIN, soldTo.getLandCntry(), customerName, details, false, requestData);
    case SCENARIO_BUSINESS_PARTNER:
    case SCENARIO_CROSSBORDER_BP:
      return doBusinessPartnerChecks(engineData, data.getPpsceid(), details);
    case SCENARIO_INTERNAL:
    case SCENARIO_INTERNAL_SO:
      Addr mailTo = requestData.getAddress("ZP01");
      String customerName2 = (StringUtils.isNotBlank(mailTo.getCustNm1()) ? mailTo.getCustNm1().trim() : "")
          + (!StringUtils.isBlank(mailTo.getCustNm2()) ? " " + mailTo.getCustNm2() : "");
      if (!customerName2.toUpperCase().contains("IBM") && !customerName.toUpperCase().contains("IBM")) {
        details.append("Mailing and Billing addresses should have IBM in them.");
        engineData.addRejectionComment("SCENARIO_CHECK", "Mailing and Billing addresses should have IBM in them.", "", "");
        return false;
      }

    }
    return true;
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

    if (SCENARIO_THIRD_PARTY.equals(scenario) || SCENARIO_THIRD_PARTY_IG.equals(scenario)) {
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
  protected List<String> getCountryLegalEndings() {
    return Arrays.asList("SL", "S.L.", "S.A.", "SLL", "SA", "LTD", "SOCIEDAD LIMITADA", "SLP", "S.C.C.L.", "SLU", "SAU", "S.A.U", "C.B.", "S.E.E.");
  }

  @Override
  public void filterDuplicateCMRMatches(EntityManager entityManager, RequestData requestData, AutomationEngineData engineData,
      MatchingResponse<DuplicateCMRCheckResponse> response) {
    String[] scenariosToBeChecked = { "THDIG", "GOVIG", "XIGS", "IGSGS" };
    String scenario = requestData.getData().getCustSubGrp();
    String[] sboValuesToCheck = { "109", "209", "309" };
    if (Arrays.asList(scenariosToBeChecked).contains(scenario)) {
      List<DuplicateCMRCheckResponse> matches = response.getMatches();
      List<DuplicateCMRCheckResponse> filteredMatches = new ArrayList<DuplicateCMRCheckResponse>();
      for (DuplicateCMRCheckResponse match : matches) {
        if (StringUtils.isNotBlank(match.getSortl())) {
          String sortl = match.getSortl().length() > 3 ? match.getSortl().substring(0, 3) : match.getSortl();
          if (!Arrays.asList(sboValuesToCheck).contains(sortl)) {
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
            engineData.addNegativeCheckStatus("_esVATCheckFailed", "VAT # on the request did not match D&B");
            details.append("VAT # on the request did not match D&B\n");
          } else {
            details.append("VAT # on the request matches D&B\n");
          }
        }
        if (!StringUtils.isBlank(change.getOldData()) && !StringUtils.isBlank(change.getNewData())
            && !(change.getOldData().equals(change.getNewData()))) {
          // UPDATE
          String oldData = change.getOldData().substring(1);
          String newData = change.getNewData().substring(1);
          if (!(oldData.equals(newData))) {
            resultCodes.add("D");// Reject
            details.append("VAT # on the request has characters updated other than the first character\n");
          } else {
            details.append("VAT # on the request differs only in the first Character\n");
          }
        }
        break;
      case "Order Block Code":
        if ("94".equals(change.getOldData()) || "94".equals(change.getNewData())) {
          cmdeReview = true;
        }
        break;
      case "SBO":
        if (!StringUtils.isBlank(change.getOldData()) && !StringUtils.isBlank(change.getNewData())
            && !(change.getOldData().equals(change.getNewData()))) {
          cmdeReview = true;
        }
        break;
      case "INAC/NAC Code":
      case "Mode Of Payment":
      case "Mailing Condition":
        cmdeReview = true;
        break;
      case "Tax Code":
        // noop, for switch handling only
        break;
      case "ISU Code":
        // noop, for switch handling only
        break;
      case "Client Tier Code":
        // noop, for switch handling only
        break;
      case "Enterprise Number":
        // noop, for switch handling only
        break;
      case "Sales Rep":
        // noop, for switch handling only
        break;
      default:
        ignoredUpdates.add(change.getDataField());
        break;
      }
    }
    if (resultCodes.contains("D")) {
      output.setOnError(true);
      engineData.addRejectionComment("_esVATUpd", "VAT # on the request has characters updated other than the first character", "", "");
      validation.setSuccess(false);
      validation.setMessage("VAT Updated");
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
              LOG.debug("Addition of " + addrType + "(" + addr.getId().getAddrSeq() + ")");
              checkDetails.append("Addition of new ZD01 and ZS02(" + addr.getId().getAddrSeq() + ") address skipped in the checks.\n");
            } else if (CmrConstants.RDC_INSTALL_AT.equals(addrType) && null == changes.getAddressChange(addrType, "Customer Name")
                && null == changes.getAddressChange(addrType, "Customer Name Con't")) {
              LOG.debug("Addition of " + addrType + "(" + addr.getId().getAddrSeq() + ")");
              checkDetails.append("Addition of new ZI01 (" + addr.getId().getAddrSeq() + ") address skipped in the checks.\n");
            } else {
              LOG.debug("New address " + addrType + "(" + addr.getId().getAddrSeq() + ") needs to be verified");
              resultCodes.add("R");
              checkDetails.append("New address " + addrType + "(" + addr.getId().getAddrSeq() + ") needs to be verified \n");
            }
          } else if ("Y".equals(addr.getChangedIndc())) {
            // update address
            if ((CmrConstants.RDC_INSTALL_AT.equals(addrType) && null == changes.getAddressChange(addrType, "Customer Name")
                && null == changes.getAddressChange(addrType, "Customer Name Con't"))
                || (CmrConstants.RDC_BILL_TO.equals(addrType) && null == changes.getAddressChange(addrType, "Customer Name")
                    && null == changes.getAddressChange(addrType, "Customer Name Con't"))) {
              // just proceed for installAT and Mailing updates
              LOG.debug("Update to InstallAt and Mailing " + addrType + "(" + addr.getId().getAddrSeq() + ")");
              checkDetails.append("Updates to InstallAt and Mailing (" + addr.getId().getAddrSeq() + ") skipped in the checks.\n");
            } else if (CmrConstants.RDC_SOLD_TO.equals(addrType) && null == changes.getDataChange("VAT #")) {
              checkDetails.append("Updates to Sold To " + addrType + "(" + addr.getId().getAddrSeq() + ") skipped in the checks").append("\n");
            } else {
              checkDetails.append("Updates to Updated Addresses for " + addrType + "(" + addr.getId().getAddrSeq() + ") needs to be verified")
                  .append("\n");
              resultCodes.add("R");
            }
          }
        }
      }
    }
    if (resultCodes.contains("R")) {
      validation.setSuccess(false);
      validation.setMessage("Not Validated");
      engineData.addNegativeCheckStatus("_esCheckFailed", "Updated elements cannot be checked automatically.");
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

  @Override
  public String getAddressTypeForGbgCovCalcs(EntityManager entityManager, RequestData requestData, AutomationEngineData engineData) throws Exception {
    Data data = requestData.getData();
    String scenario = data.getCustSubGrp();
    String address = "ZS01";

    LOG.debug("Address for the scenario to check: " + scenario);
    if (SCENARIO_THIRD_PARTY.equals(scenario) || SCENARIO_THIRD_PARTY_IG.equals(scenario)) {
      address = "ZI01";
    }
    return address;
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

    if ((!isCoverageCalculated || SCENARIO_PRIVATE_CUSTOMER.equals(scenario)
        || ((SCENARIO_THIRD_PARTY.equals(scenario) || SCENARIO_THIRD_PARTY_IG.equals(scenario)) && engineData.get("ZI01_DNB_MATCH") != null))
        && !SCENARIOS_TO_SKIP_COVERAGE.contains(scenario)) {
      details.setLength(0);
      overrides.clearOverrides();
      SpainFieldsCompContainer fields = new SpainFieldsCompContainer(entityManager, data, data.getIsuCd(), data.getClientTier());
      if (fields != null && fields.allFieldsCalculated()) {
        details.append("Coverage calculated successfully using 32S logic.").append("\n");
        details.append("Sales Rep : " + fields.getSalesRep()).append("\n");
        details.append("Enterprise : " + fields.getEnterprise()).append("\n");
        details.append("SBO : " + fields.getSbo()).append("\n");
        overrides.addOverride(AutomationElementRegistry.GBL_CALC_COV, "DATA", "SALES_BO_CD", data.getSalesBusOffCd(), fields.getSbo());
        overrides.addOverride(AutomationElementRegistry.GBL_CALC_COV, "DATA", "ENTERPRISE", data.getEnterprise(), fields.getEnterprise());
        overrides.addOverride(AutomationElementRegistry.GBL_CALC_COV, "DATA", "REP_TEAM_MEMBER_NO", data.getRepTeamMemberNo(), fields.getSalesRep());
        results.setResults("Calculated");
        results.setDetails(details.toString());
      } else if (StringUtils.isNotBlank(data.getRepTeamMemberNo()) && StringUtils.isNotBlank(data.getSalesBusOffCd())
          && StringUtils.isNotBlank(data.getEnterprise())) {
        details.append("Coverage could not be calculated using 32S logic. Using values from request").append("\n");
        details.append("Sales Rep : " + fields.getSalesRep()).append("\n");
        details.append("Enterprise : " + fields.getEnterprise()).append("\n");
        details.append("SBO : " + fields.getSbo()).append("\n");
        results.setResults("Calculated");
        results.setDetails(details.toString());
      } else {
        String msg = "Coverage cannot be calculated. No valid 32S mapping found from request data.";
        details.append(msg);
        results.setResults("Cannot Calculate");
        results.setDetails(details.toString());
        engineData.addNegativeCheckStatus("_esCoverage", msg);
      }
    }
    return true;
  }

  @Override
  public void tweakGBGFinderRequest(EntityManager entityManager, GBGFinderRequest request, RequestData requestData, AutomationEngineData engineData) {
    Admin admin = requestData.getAdmin();
    Data data = requestData.getData();
    String scenario = data.getCustSubGrp();
    if ("C".equals(admin.getReqType()) && (SCENARIO_THIRD_PARTY.equals(scenario) || SCENARIO_THIRD_PARTY_IG.equals(scenario))) {
      DnBMatchingResponse dnbRecord = (DnBMatchingResponse) engineData.get("ZI01_DNB_MATCH");
      if (dnbRecord != null) {
        request.setDunsNo(dnbRecord.getDunsNo());
      }
    }

  }

}
