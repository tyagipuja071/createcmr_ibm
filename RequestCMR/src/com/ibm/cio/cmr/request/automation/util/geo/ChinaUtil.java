package com.ibm.cio.cmr.request.automation.util.geo;

import java.util.Arrays;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.automation.AutomationEngineData;
import com.ibm.cio.cmr.request.automation.RequestData;
import com.ibm.cio.cmr.request.automation.out.AutomationResult;
import com.ibm.cio.cmr.request.automation.out.OverrideOutput;
import com.ibm.cio.cmr.request.automation.out.ValidationOutput;
import com.ibm.cio.cmr.request.automation.util.AutomationUtil;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cmr.services.client.matching.MatchingResponse;
import com.ibm.cmr.services.client.matching.cmr.DuplicateCMRCheckResponse;

public class ChinaUtil extends AutomationUtil {

  private static final Logger LOG = Logger.getLogger(ChinaUtil.class);
  public static final String SCENARIO_LOCAL_NRML = "NRML";
  public static final String SCENARIO_LOCAL_EMBSA = "EMBSA";
  public static final String SCENARIO_CROSS_CROSS = "CROSS";
  public static final String SCENARIO_LOCAL_AQSTN = "AQSTN";
  public static final String SCENARIO_LOCAL_BLUMX = "BLUMX";
  public static final String SCENARIO_LOCAL_MRKT = "MRKT";
  public static final String SCENARIO_LOCAL_BUSPR = "BUSPR";
  public static final String SCENARIO_LOCAL_INTER = "INTER";
  public static final String SCENARIO_LOCAL_PRIV = "PRIV";

  @Override
  public boolean performScenarioValidation(EntityManager entityManager, RequestData requestData, AutomationEngineData engineData,
      AutomationResult<ValidationOutput> result, StringBuilder details, ValidationOutput output) {

    // ScenarioExceptionsUtil scenarioExceptions = (ScenarioExceptionsUtil)
    // engineData.get("SCENARIO_EXCEPTIONS");
    Data data = requestData.getData();
    String scenario = data.getCustSubGrp();
    Addr soldTo = requestData.getAddress("ZS01");
    String custNm1 = soldTo.getCustNm1();
    String custNm2 = StringUtils.isNotBlank(soldTo.getCustNm2()) ? " " + soldTo.getCustNm2() : "";
    String customerName = custNm1 + custNm2;

    switch (scenario) {

    case SCENARIO_LOCAL_NRML:
      if (!("00000".equals(data.getSearchTerm()) || "04182".equals(data.getSearchTerm()))) {
        LOG.debug("Cluster allowed: Cluster=" + data.getSearchTerm() + " Scenario=" + data.getCustSubGrp());
        result.setOnError(false);
        details.append("Cluster allowed:Cluster=" + data.getSearchTerm() + " Scenario=" + data.getCustSubGrp() + " for the request.\n");
      } else {
        details.append("Cluster=" + data.getSearchTerm() + " should not be allowed for Scenario=" + data.getCustSubGrp() + " for the request.\n");
        engineData.addRejectionComment("OTH", "Cluster=" + data.getSearchTerm() + " should not be allowed for this scenario", "", "");
        result.setOnError(true);
      }
      break;
    case SCENARIO_LOCAL_EMBSA:
      if (!("00000".equals(data.getSearchTerm()) || "04182".equals(data.getSearchTerm()))) {
        LOG.debug("Cluster allowed: Cluster=" + data.getSearchTerm() + " Scenario=" + data.getCustSubGrp());
        result.setOnError(false);
        details.append("Cluster allowed:Cluster=" + data.getSearchTerm() + " Scenario=" + data.getCustSubGrp() + " for the request.\n");
      } else {
        details.append("Cluster=" + data.getSearchTerm() + " should not be allowed for Scenario=" + data.getCustSubGrp() + " for the request.\n");
        engineData.addRejectionComment("OTH", "Cluster=" + data.getSearchTerm() + " should not be allowed for this scenario", "", "");
        result.setOnError(true);
      }
      break;
    case SCENARIO_CROSS_CROSS:
      if (!("00000".equals(data.getSearchTerm()) || "04182".equals(data.getSearchTerm()))) {
        LOG.debug("Cluster allowed: Cluster=" + data.getSearchTerm() + " Scenario=" + data.getCustSubGrp());
        result.setOnError(false);
        details.append("Cluster allowed:Cluster=" + data.getSearchTerm() + " Scenario=" + data.getCustSubGrp() + " for the request.\n");
      } else {
        details.append("Cluster=" + data.getSearchTerm() + " should not be allowed for Scenario=" + data.getCustSubGrp() + " for the request.\n");
        engineData.addRejectionComment("OTH", "Cluster=" + data.getSearchTerm() + " should not be allowed for this scenario", "", "");
        result.setOnError(true);
      }
      break;
    case SCENARIO_LOCAL_AQSTN:
      if (!("00000".equals(data.getSearchTerm()) || "04182".equals(data.getSearchTerm()))) {
        LOG.debug("Cluster allowed: Cluster=" + data.getSearchTerm() + " Scenario=" + data.getCustSubGrp());
        result.setOnError(false);
        details.append("Cluster allowed:Cluster=" + data.getSearchTerm() + " Scenario=" + data.getCustSubGrp() + " for the request.\n");
      } else {
        details.append("Cluster=" + data.getSearchTerm() + " should not be allowed for Scenario=" + data.getCustSubGrp() + " for the request.\n");
        engineData.addRejectionComment("OTH", "Cluster=" + data.getSearchTerm() + " should not be allowed for this scenario", "", "");
        result.setOnError(true);
      }
      break;
    case SCENARIO_LOCAL_BLUMX:
      engineData.addPositiveCheckStatus(AutomationEngineData.SKIP_COVERAGE);
      break;
    case SCENARIO_LOCAL_MRKT:
      engineData.addPositiveCheckStatus(AutomationEngineData.SKIP_COVERAGE);
      break;
    case SCENARIO_LOCAL_BUSPR:
      if ("04182".equals(data.getSearchTerm())) {
        LOG.debug("Cluster allowed: Cluster=" + data.getSearchTerm() + " Scenario=" + data.getCustSubGrp());
        result.setOnError(false);
        details.append("Cluster allowed:Cluster=" + data.getSearchTerm() + " Scenario=" + data.getCustSubGrp() + " for the request.\n");
      } else {
        details
            .append("Cluster=" + data.getSearchTerm() + " should be default (04182)  for Scenario=" + data.getCustSubGrp() + " for the request.\n");
        engineData.addRejectionComment("OTH", "Cluster=" + data.getSearchTerm() + " should default (04182)  for this scenario", "", "");
        result.setOnError(true);
      }
      if (StringUtils.isNotBlank(data.getPpsceid())) {
        LOG.debug("CEID=" + data.getPpsceid() + " for Scenario=" + data.getCustSubGrp());
        result.setOnError(false);
        details.append("CEID=" + data.getPpsceid() + " for Scenario=" + data.getCustSubGrp() + " for the request.\n");
      } else {
        details.append("CEID cannot be blank for Scenario=" + data.getCustSubGrp() + " for the request.\n");
        engineData.addRejectionComment("OTH", "CEID cannot be blank for this scenario", "", "");
        result.setOnError(true);
      }
      if ("7230".equals(data.getIsicCd())) {
        LOG.debug("ISIC allowed: ISIC=" + data.getIsicCd() + " Scenario=" + data.getCustSubGrp());
        result.setOnError(false);
        details.append("ISIC ISIC=" + data.getIsicCd() + " Scenario=" + data.getCustSubGrp() + " for the request.\n");
      } else {
        details.append("ISIC=" + data.getIsicCd() + " should be default (7230)  for Scenario=" + data.getCustSubGrp() + " for the request.\n");
        engineData.addRejectionComment("OTH", "ISIC=" + data.getIsicCd() + " should default (7230)  for this scenario", "", "");
        result.setOnError(true);
      }
      engineData.addPositiveCheckStatus(AutomationEngineData.SKIP_GBG);
      engineData.addPositiveCheckStatus(AutomationEngineData.SKIP_COVERAGE);
      break;
    case SCENARIO_LOCAL_INTER:
      if (StringUtils.isNotBlank(customerName) && customerName.indexOf("IBM China") >= 0) {
        LOG.debug("English name=" + customerName + " for Scenario=" + data.getCustSubGrp());
        result.setOnError(false);
        details.append("English name=" + customerName + " for Scenario=" + data.getCustSubGrp() + " for the request.\n");
      } else {
        details.append("English name should include 'IBM China' for Scenario=" + data.getCustSubGrp() + " for the request.\n");
        engineData.addRejectionComment("OTH", "English name should include 'IBM China' for this scenario", "", "");
        result.setOnError(true);
      }
      engineData.addPositiveCheckStatus(AutomationEngineData.SKIP_GBG);
      engineData.addPositiveCheckStatus(AutomationEngineData.SKIP_COVERAGE);
      break;
    case SCENARIO_LOCAL_PRIV:
      if ("00000".equals(data.getSearchTerm())) {
        LOG.debug("Cluster allowed: Cluster=" + data.getSearchTerm() + " Scenario=" + data.getCustSubGrp());
        result.setOnError(false);
        details.append("Cluster allowed:Cluster=" + data.getSearchTerm() + " Scenario=" + data.getCustSubGrp() + " for the request.\n");
      } else {
        details
            .append("Cluster=" + data.getSearchTerm() + " should be default (00000)  for Scenario=" + data.getCustSubGrp() + " for the request.\n");
        engineData.addRejectionComment("OTH", "Cluster=" + data.getSearchTerm() + " should default (00000)  for this scenario", "", "");
        result.setOnError(true);
      }
      if ("9500".equals(data.getIsicCd())) {
        LOG.debug("ISIC allowed: ISIC=" + data.getIsicCd() + " Scenario=" + data.getCustSubGrp());
        result.setOnError(false);
        details.append("ISIC ISIC=" + data.getIsicCd() + " Scenario=" + data.getCustSubGrp() + " for the request.\n");
      } else {
        details.append("ISIC=" + data.getIsicCd() + " should be default (9500)  for Scenario=" + data.getCustSubGrp() + " for the request.\n");
        engineData.addRejectionComment("OTH", "ISIC=" + data.getIsicCd() + " should default (9500)  for this scenario", "", "");
        result.setOnError(true);
      }
      engineData.addPositiveCheckStatus(AutomationEngineData.SKIP_GBG);
      engineData.addPositiveCheckStatus(AutomationEngineData.SKIP_COVERAGE);
      break;
    }

    return true;
  }

  // @Override
  // public boolean
  // performCountrySpecificCoverageCalculations(CalculateCoverageElement
  // covElement, EntityManager entityManager,
  // AutomationResult<OverrideOutput> results, StringBuilder details,
  // OverrideOutput overrides, RequestData requestData,
  // AutomationEngineData engineData, String covFrom, CoverageContainer
  // container, boolean isCoverageCalculated) throws Exception {
  //
  // if (!"C".equals(requestData.getAdmin().getReqType())) {
  // details.append(" Coverage Calculation skipped for Updates.");
  // results.setResults("Skipped");
  // results.setDetails(details.toString());
  // return true;
  // }
  //
  // Data data = requestData.getData();
  // String scenario = data.getCustSubGrp();
  // if (StringUtils.isNotEmpty(scenario) &&
  // !((SCENARIO_LOCAL_BLUMX.equals(scenario) ||
  // SCENARIO_LOCAL_MRKT.equals(scenario)
  // || SCENARIO_LOCAL_BUSPR.equals(scenario) ||
  // SCENARIO_LOCAL_INTER.equals(scenario) ||
  // SCENARIO_LOCAL_PRIV.equals(scenario)))) {
  //
  // }
  // return true;
  // }

  @Override
  public AutomationResult<OverrideOutput> doCountryFieldComputations(EntityManager entityManager, AutomationResult<OverrideOutput> results,
      StringBuilder details, OverrideOutput overrides, RequestData requestData, AutomationEngineData engineData) throws Exception {
    Data data = requestData.getData();
    Admin admin = requestData.getAdmin();
    if ("C".equals(admin.getReqType())) {
      String scenario = data.getCustSubGrp();
      LOG.info("Starting Field Computations for Request ID " + data.getId().getReqId());
      if (StringUtils.isNotBlank(scenario) && (SCENARIO_LOCAL_BLUMX.equals(scenario) || SCENARIO_LOCAL_MRKT.equals(scenario)
          || SCENARIO_LOCAL_BUSPR.equals(scenario) || SCENARIO_LOCAL_INTER.equals(scenario) || SCENARIO_LOCAL_PRIV.equals(scenario))) {
        details.append("No specific fields to compute.\n");
        results.setResults("Skipped");
      } else {
        details.append("Successful Execution.\n");
        results.setResults("Computed");
      }

    } else {
      details.append("No specific fields to compute.\n");
      results.setResults("Skipped");
    }
    results.setDetails(details.toString());

    return results;
  }
  /*
   * @Override public boolean runUpdateChecksForData(EntityManager
   * entityManager, AutomationEngineData engineData, RequestData requestData,
   * RequestChangeContainer changes, AutomationResult<ValidationOutput> output,
   * ValidationOutput validation) throws Exception { Admin admin =
   * requestData.getAdmin(); Data data = requestData.getData(); StringBuilder
   * details = new StringBuilder(); boolean cmdeReview = false; Set<String>
   * resultCodes = new HashSet<String>(); List<String> ignoredUpdates = new
   * ArrayList<String>(); for (UpdatedDataModel change :
   * changes.getDataUpdates()) { switch (change.getDataField()) { case "VAT #":
   * if (StringUtils.isBlank(change.getOldData()) &&
   * !StringUtils.isBlank(change.getNewData())) { // ADD Addr soldTo =
   * requestData.getAddress(CmrConstants.RDC_SOLD_TO); List<DnBMatchingResponse>
   * matches = getMatches(requestData, engineData, soldTo, true); boolean
   * matchesDnb = false; if (matches != null) { // check against D&B matchesDnb
   * = ifaddressCloselyMatchesDnb(matches, soldTo, admin,
   * data.getCmrIssuingCntry()); } if (!matchesDnb) { cmdeReview = true;
   * engineData.addNegativeCheckStatus("_beluxVATCheckFailed",
   * "VAT # on the request did not match D&B");
   * details.append("VAT # on the request did not match D&B\n"); } else {
   * details.append("VAT # on the request matches D&B\n"); } }
   * 
   * if (!StringUtils.isBlank(change.getOldData()) &&
   * !StringUtils.isBlank(change.getNewData()) &&
   * !(change.getOldData().equals(change.getNewData()))) { // UPDATE Addr soldTo
   * = requestData.getAddress(CmrConstants.RDC_SOLD_TO);
   * List<DnBMatchingResponse> matches = getMatches(requestData, engineData,
   * soldTo, true); boolean matchesDnb = false; if (matches != null) { // check
   * against D&B matchesDnb = ifaddressCloselyMatchesDnb(matches, soldTo, admin,
   * data.getCmrIssuingCntry()); } if (!matchesDnb) { cmdeReview = true;
   * engineData.addNegativeCheckStatus("_beluxVATCheckFailed",
   * "VAT # on the request did not match D&B");
   * details.append("VAT # on the request did not match D&B\n"); } else {
   * details.append("VAT # on the request matches D&B\n"); } }
   * 
   * break; case "ISIC": case "INAC/NAC Code": cmdeReview = true; break; case
   * "Economic Code": String newEcoCd = change.getNewData(); String oldEcoCd =
   * change.getOldData(); List<String> ls = Arrays.asList("A", "F", "C", "R");
   * if (StringUtils.isNotBlank(newEcoCd) && StringUtils.isNotBlank(oldEcoCd) &&
   * newEcoCd.length() == 3 && oldEcoCd.length() == 3) { if
   * (!newEcoCd.substring(0, 1).equals(oldEcoCd.substring(0, 1)) &&
   * newEcoCd.substring(1, newEcoCd.length()).equals(oldEcoCd.substring(1,
   * oldEcoCd.length()))) { if (ls.contains(newEcoCd.substring(0, 1)) &&
   * oldEcoCd.substring(0, 1).equals("K") || ls.contains(oldEcoCd.substring(0,
   * 1)) && newEcoCd.substring(0, 1).equals("K")) {
   * admin.setScenarioVerifiedIndc("Y");
   * details.append("Economic Code has been updated by registered user to " +
   * newEcoCd + "\n"); } else { cmdeReview = true; } } else if
   * (newEcoCd.substring(0, 1).equals(oldEcoCd.substring(0, 1)) &&
   * (newEcoCd.substring(1, 3).equals("11") || newEcoCd.substring(1,
   * 3).equals("13") || newEcoCd.substring(1, 3).equals("49"))) { cmdeReview =
   * true; engineData.addNegativeCheckStatus("_beluxEconomicCdUpdt",
   * "Economic code is updated from " + oldEcoCd + " to " + newEcoCd);
   * details.append("Economic code is updated to " + newEcoCd + "\n"); } else {
   * cmdeReview = true;
   * engineData.addNegativeCheckStatus("_beluxEconomicCdUpdt",
   * "Economic code was updated incorrectly or by non registered user.");
   * details.
   * append("Economic code was updated incorrectly or by non registered user.\n"
   * ); } } break; case "PPS CEID": String newppsceid = change.getNewData();
   * String oldppsceid = change.getOldData(); String Kukla =
   * data.getCustClass(); // ADD if (StringUtils.isBlank(oldppsceid) &&
   * !StringUtils.isBlank(newppsceid)) { if ("49".equalsIgnoreCase(Kukla) &&
   * checkPPSCEID(data.getPpsceid())) { details.
   * append("PPS CE ID validated successfully with PartnerWorld Profile Systems."
   * ).append("\n"); } else { resultCodes.add("D"); if
   * (!"49".equalsIgnoreCase(Kukla)) {
   * details.append("PS Ceid added for CMR with Kukla other than 49").append(
   * "\n"); } else {
   * details.append("PPS ceid on the request is invalid").append("\n"); } } } //
   * DELETE if (!StringUtils.isBlank(oldppsceid) &&
   * StringUtils.isBlank(newppsceid)) { cmdeReview = true;
   * engineData.addNegativeCheckStatus("_beluxPpsCeidUpdt",
   * " Deletion of ppsceid needs cmde review.\n");
   * details.append(" Deletion of ppsceid needs cmde review.\n"); } // UPDATE if
   * (!StringUtils.isBlank(oldppsceid) && !StringUtils.isBlank(newppsceid) &&
   * !oldppsceid.equalsIgnoreCase(newppsceid)) { cmdeReview = true;
   * engineData.addNegativeCheckStatus("_beluxPpsCeidUpdt",
   * " Update of ppsceid needs cmde review.\n");
   * details.append(" Update of ppsceid needs cmde review.\n"); } break; case
   * "Order Block Code": String newOrdBlk = change.getNewData(); String
   * oldOrdBlk = change.getOldData(); // ADD if (StringUtils.isBlank(oldOrdBlk)
   * && !StringUtils.isBlank(newOrdBlk) && "P".equalsIgnoreCase(newOrdBlk)) {
   * cmdeReview = true; engineData.addNegativeCheckStatus("_beluxOrdBlkUpdt",
   * "Embargo Code P was added.");
   * details.append("Embargo Code P was added.\n"); }
   * 
   * // DELETE if (!StringUtils.isBlank(oldOrdBlk) &&
   * StringUtils.isBlank(newOrdBlk) && "P".equalsIgnoreCase(oldOrdBlk)) {
   * cmdeReview = true; engineData.addNegativeCheckStatus("_beluxOrdBlkUpdt",
   * "Embargo Code P was deleted.");
   * details.append("Embargo Code P was deleted.\n"); } break; case
   * "Preferred Language":
   * details.append("Preferred Language data field was updated.\n"); break;
   * default: ignoredUpdates.add(change.getDataField()); break; } } if
   * (resultCodes.contains("D")) { output.setOnError(true);
   * validation.setSuccess(false); validation.setMessage("Rejected"); } else if
   * (cmdeReview) { engineData.addNegativeCheckStatus("_esDataCheckFailed",
   * "Updates to one or more fields requires CMDE review.");
   * details.append("Updates to one or more fields requires CMDE review.\n");
   * validation.setSuccess(false); validation.setMessage("Not Validated"); }
   * else { validation.setSuccess(true); validation.setMessage("Successful"); }
   * if (!ignoredUpdates.isEmpty()) {
   * details.append("Updates to the following fields skipped validation:\n");
   * for (String field : ignoredUpdates) { details.append(" - " + field + "\n");
   * } } output.setDetails(details.toString());
   * output.setProcessOutput(validation); return true; }
   * 
   * @Override public boolean runUpdateChecksForAddress(EntityManager
   * entityManager, AutomationEngineData engineData, RequestData requestData,
   * RequestChangeContainer changes, AutomationResult<ValidationOutput> output,
   * ValidationOutput validation) throws Exception { Admin admin =
   * requestData.getAdmin(); Data data = requestData.getData(); StringBuilder
   * duplicateDetails = new StringBuilder(); StringBuilder nonRelAddrFdsDetails
   * = new StringBuilder(); List<Addr> addresses = null; int ignoredAddr = 0;
   * StringBuilder checkDetails = new StringBuilder(); Set<String> resultCodes =
   * new HashSet<String>();// R - review for (String addrType :
   * RELEVANT_ADDRESSES) { addresses = requestData.getAddresses(addrType); if
   * (changes.isAddressChanged(addrType)) { for (Addr addr : addresses) { if
   * (isRelevantAddressFieldUpdated(changes, addr, nonRelAddrFdsDetails)) {
   * 
   * if ((addrType.equalsIgnoreCase(CmrConstants.RDC_SOLD_TO) &&
   * "Y".equals(addr.getImportInd())) ||
   * addrType.equalsIgnoreCase(CmrConstants.RDC_BILL_TO) ||
   * addrType.equalsIgnoreCase(CmrConstants.RDC_SECONDARY_SOLD_TO)) {
   * 
   * List<DnBMatchingResponse> matches = getMatches(requestData, engineData,
   * addr, false); boolean matchesDnb = false; if (matches != null) { // check
   * against D&B matchesDnb = ifaddressCloselyMatchesDnb(matches, addr, admin,
   * data.getCmrIssuingCntry()); } if (!matchesDnb) { LOG.debug("Address " +
   * addrType + "(" + addr.getId().getAddrSeq() + ") does not match D&B");
   * resultCodes.add("X"); checkDetails.append("Address " + addrType + "(" +
   * addr.getId().getAddrSeq() + ") did not match D&B records.\n"); } else {
   * checkDetails.append("Address " + addrType + "(" + addr.getId().getAddrSeq()
   * + ") matches D&B records. Matches:\n"); for (DnBMatchingResponse dnb :
   * matches) { checkDetails.append(" - DUNS No.:  " + dnb.getDunsNo() + " \n");
   * checkDetails.append(" - Name.:  " + dnb.getDnbName() + " \n");
   * checkDetails.append(" - Address:  " + dnb.getDnbStreetLine1() + " " +
   * dnb.getDnbCity() + " " + dnb.getDnbPostalCode() + " " + dnb.getDnbCountry()
   * + "\n\n"); } }
   * 
   * }
   * 
   * String soldToCustNm1 = requestData.getAddress("ZS01").getCustNm1(); String
   * installAtCustNm = requestData.getAddress("ZI01") != null ?
   * requestData.getAddress("ZI01").getCustNm1() : ""; if
   * ((addrType.equalsIgnoreCase(CmrConstants.RDC_INSTALL_AT) &&
   * soldToCustNm1.equalsIgnoreCase(installAtCustNm)) ||
   * (addrType.equalsIgnoreCase(CmrConstants.RDC_SHIP_TO) &&
   * "N".equals(addr.getImportInd()))) { LOG.debug("Checking duplicates for " +
   * addrType + "(" + addr.getId().getAddrSeq() + ")"); boolean duplicate =
   * addressExists(entityManager, addr); if (duplicate) {
   * LOG.debug(" - Duplicates found for " + addrType + "(" +
   * addr.getId().getAddrSeq() + ")"); duplicateDetails.append("Address " +
   * addrType + "(" + addr.getId().getAddrSeq() +
   * ") provided matches an existing address.\n"); resultCodes.add("R"); } }
   * 
   * if (addrType.equalsIgnoreCase(CmrConstants.RDC_SHIP_TO) &&
   * "Y".equals(addr.getImportInd())) { ignoredAddr++; } } } } } if
   * (resultCodes.contains("X")) { validation.setSuccess(false);
   * validation.setMessage("Review Required.");
   * engineData.addNegativeCheckStatus("_esCheckFailed",
   * "Updated elements cannot be checked automatically."); } else if
   * (resultCodes.contains("R")) { output.setOnError(true);
   * engineData.addRejectionComment("_atRejectAddr",
   * "Addition or updation on the address is rejected", "", "");
   * validation.setSuccess(false); validation.setMessage("Rejected."); } else {
   * validation.setSuccess(true); validation.setMessage("Successful"); }
   * 
   * if (ignoredAddr > 0) {
   * checkDetails.append("Updates to imported Address Ship-To(ZD01) ignored. ");
   * } String details = (output.getDetails() != null &&
   * output.getDetails().length() > 0) ? output.getDetails() : ""; details +=
   * duplicateDetails.length() > 0 ? duplicateDetails.toString() : ""; details
   * += checkDetails.length() > 0 ? "\n" + checkDetails.toString() : ""; details
   * += nonRelAddrFdsDetails.length() > 0 ? "Following updates ignored - \n" +
   * nonRelAddrFdsDetails.toString() : ""; output.setDetails(details);
   * output.setProcessOutput(validation); return true; }
   * 
   * private boolean isRelevantAddressFieldUpdated(RequestChangeContainer
   * changes, Addr addr, StringBuilder details) { List<UpdatedNameAddrModel>
   * addrChanges = changes.getAddressChanges(addr.getId().getAddrType(),
   * addr.getId().getAddrSeq()); if (addrChanges == null) { return false; } for
   * (UpdatedNameAddrModel change : addrChanges) { if
   * (!NON_RELEVANT_ADDRESS_FIELDS.contains(change.getDataField())) { return
   * true; } else { details.append(change.getDataField() + " of address " +
   * addr.getId().getAddrType() + " \n"); } } return false; }
   */

  @Override
  public void filterDuplicateCMRMatches(EntityManager entityManager, RequestData requestData, AutomationEngineData engineData,
      MatchingResponse<DuplicateCMRCheckResponse> response) {
    // String[] scenariosToBeChecked = { "COMME", "CBMME", "GOVRN", "CBVRN",
    // "BPIEU", "CBIEU", "CBIEM", "XBLUM" };
    // String scenario = requestData.getData().getCustSubGrp();
    // String[] kuklaComme = { "11" };
    // String[] kuklaGovrn = { "13", "14", "17" };
    // String[] kuklaBuspr = { "42", "43", "45", "46", "47", "48" };
    // String[] kuklaCBIEM = { "71" };
    // String[] kuklaXBLUM = { "60" };
    //
    // if (Arrays.asList(scenariosToBeChecked).contains(scenario)) {
    // List<DuplicateCMRCheckResponse> matches = response.getMatches();
    // List<DuplicateCMRCheckResponse> filteredMatches = new
    // ArrayList<DuplicateCMRCheckResponse>();
    // for (DuplicateCMRCheckResponse match : matches) {
    // if (StringUtils.isNotBlank(match.getCustClass())) {
    // String kukla = match.getCustClass() != null ? match.getCustClass() : "";
    // if (Arrays.asList(kuklaComme).contains(kukla) &&
    // ("COMME".equals(scenario) || "CBMME".equals(scenario))) {
    // filteredMatches.add(match);
    // } else if (Arrays.asList(kuklaGovrn).contains(kukla) &&
    // ("GOVRN".equals(scenario) || "CBVRN".equals(scenario))) {
    // filteredMatches.add(match);
    // } else if (Arrays.asList(kuklaBuspr).contains(kukla) &&
    // ("BPIEU".equals(scenario) || "CBIEU".equals(scenario))) {
    // filteredMatches.add(match);
    // } else if (Arrays.asList(kuklaCBIEM).contains(kukla) &&
    // ("CBIEM".equals(scenario))) {
    // filteredMatches.add(match);
    // } else if (Arrays.asList(kuklaXBLUM).contains(kukla) &&
    // ("XBLUM".equals(scenario))) {
    // filteredMatches.add(match);
    // }
    // }
    //
    // }
    // // set filtered matches in response
    // response.setMatches(filteredMatches);
    // }

  }

  @Override
  public List<String> getSkipChecksRequestTypesforCMDE() {
    return Arrays.asList("C", "U", "M", "D", "R");
  }
}
