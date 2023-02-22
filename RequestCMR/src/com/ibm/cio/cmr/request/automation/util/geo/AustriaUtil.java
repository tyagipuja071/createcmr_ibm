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
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.model.window.UpdatedDataModel;
import com.ibm.cio.cmr.request.model.window.UpdatedNameAddrModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.util.RequestUtils;
import com.ibm.cio.cmr.request.util.SystemLocation;
import com.ibm.cmr.services.client.matching.dnb.DnBMatchingResponse;

/**
 * {@link AutomationUtil} for Austria specific validations
 * 
 *
 */
public class AustriaUtil extends AutomationUtil {

  private static final Logger LOG = Logger.getLogger(AustriaUtil.class);
  private static final String SCENARIO_COMMERCIAL = "COMME";
  private static final String SCENARIO_PRIVATE_CUSTOMER = "PRICU";
  private static final String SCENARIO_IBM_EMPLOYEE = "IBMEM";
  private static final String SCENARIO_BUSINESS_PARTNER = "BUSPR";
  private static final String SCENARIO_THIRD_PARTY_DC = "THDPT";
  private static final String SCENARIO_BUSINESS_PARTNER_CROSS = "XBP";
  private static final String SCENARIO_CROSS_COMMERICAL = "XCOM";
  private static final String SCENARIO_INTERNAL = "INTER";
  private static final String SCENARIO_INTERNAL_SO = "INTSO";
  private static final String SCENARIO_GOVERNMENT = "GOVRN";

  private static final List<String> RELEVANT_ADDRESSES = Arrays.asList(CmrConstants.RDC_SOLD_TO, CmrConstants.RDC_BILL_TO,
      CmrConstants.RDC_INSTALL_AT, CmrConstants.RDC_SHIP_TO, CmrConstants.RDC_SECONDARY_SOLD_TO, CmrConstants.RDC_PAYGO_BILLING);

  private static final List<String> NON_RELEVANT_ADDRESS_FIELDS = Arrays.asList("Attention Person", "Phone number", "FAX", "Customer Name 4");
  private static final List<String> RELEVANT_ADDRESS_FIELDS_ZS01_ZP01 = Arrays.asList("Street Name And Number", "Customer Legal name");
  private static final List<String> NON_RELEVANT_ADDRESS_FIELDS_ZS01_ZP01 = Arrays.asList("Attention To/Building/Floor/Office",
      "Division/Department");

  @Override
  public boolean performScenarioValidation(EntityManager entityManager, RequestData requestData, AutomationEngineData engineData,
      AutomationResult<ValidationOutput> result, StringBuilder details, ValidationOutput output) {
    Data data = requestData.getData();
    Addr soldTo = requestData.getAddress("ZS01");
    String scenario = data.getCustSubGrp();
    LOG.info("Starting scenario validations for Request ID " + data.getId().getReqId());
    String custNm1 = soldTo.getCustNm1();
    String custNm2 = !StringUtils.isBlank(soldTo.getCustNm2()) ? " " + soldTo.getCustNm2() : "";
    String custNm3 = !StringUtils.isBlank(soldTo.getCustNm3()) ? " " + soldTo.getCustNm3() : "";
    String customerName = custNm1 + custNm2 + custNm3;
    String custGrp = data.getCustGrp();
    // CREATCMR-6244 LandCntry UK(GB)
    if(soldTo != null){
    	String landCntry = soldTo.getLandCntry();
    	if(data.getVat()!=null && !data.getVat().isEmpty() && landCntry.equals("GB") && !data.getCmrIssuingCntry().equals("866") && custGrp != null && StringUtils.isNotEmpty(custGrp)
                && ("CROSS".equals(custGrp))){
        	engineData.addNegativeCheckStatus("_vatUK", " request need to be send to CMDE queue for further review. ");
        	details.append("Landed Country UK. The request need to be send to CMDE queue for further review.\n");
        }
    }
    if (StringUtils.isBlank(scenario)) {
      details.append("Scenario not correctly specified on the request");
      engineData.addNegativeCheckStatus("_atNoScenario", "Scenario not correctly specified on the request");
      return true;
    }

    if ("C".equals(requestData.getAdmin().getReqType())) {
      // remove duplicates
      removeDuplicateAddresses(entityManager, requestData, details);
    }

    if (!SCENARIO_THIRD_PARTY_DC.equals(scenario) && (customerName.toUpperCase().contains("C/O") || customerName.toUpperCase().contains("CAREOF")
        || customerName.toUpperCase().contains("CARE OF"))) {
      details.append("Scenario should be Third Party/Data Center based on custmer name.").append("\n");
      // engineData.addNegativeCheckStatus("SCENARIO_CHECK", "The scenario
      // should be for 3rd Party / Data Center.");
      engineData.addRejectionComment("OTH", "The scenario should be for 3rd Party / Data Center.", "", "");
      return false;
    }

    LOG.debug("Scenario to check: " + scenario);

    switch (scenario) {
    case SCENARIO_PRIVATE_CUSTOMER:
    case SCENARIO_IBM_EMPLOYEE:
      return doPrivatePersonChecks(engineData, SystemLocation.AUSTRIA, soldTo.getLandCntry(), customerName, details,
          SCENARIO_IBM_EMPLOYEE.equals(scenario), requestData);
    case SCENARIO_BUSINESS_PARTNER:
      engineData.addPositiveCheckStatus(AutomationEngineData.SKIP_GBG);
      engineData.addPositiveCheckStatus(AutomationEngineData.SKIP_COVERAGE);
      break;
    case SCENARIO_BUSINESS_PARTNER_CROSS:
      engineData.addPositiveCheckStatus(AutomationEngineData.SKIP_GBG);
      engineData.addPositiveCheckStatus(AutomationEngineData.SKIP_COVERAGE);
      return doBusinessPartnerChecks(engineData, data.getPpsceid(), details);
    case SCENARIO_THIRD_PARTY_DC:
      engineData.addNegativeCheckStatus("_atThirdParty", "Third Party/Data Center request needs further validation.");
      details.append("Third Party/Data Center request needs further validation.").append("\n");
    }

    return true;
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
    List<String> ignoredUpdates = new ArrayList<String>();
    for (UpdatedDataModel change : changes.getDataUpdates()) {
      switch (change.getDataField()) {
      case "VAT #":
        if (requestData.getAddress("ZS01").getLandCntry().equals("GB")) {
          if (!AutomationUtil.isTaxManagerEmeaUpdateCheck(entityManager, engineData, requestData)) {
            engineData.addNegativeCheckStatus("_vatUK", " request need to be send to CMDE queue for further review. ");
            details.append("Landed Country UK. The request need to be send to CMDE queue for further review.\n");
          }
        } else {
          if (!StringUtils.isBlank(change.getNewData())) {
            Addr soldTo = requestData.getAddress(CmrConstants.RDC_SOLD_TO);
            List<DnBMatchingResponse> matches = getMatches(requestData, engineData, soldTo, true);
            boolean matchesDnb = false;
            if (matches != null) {
              // check against D&B
              matchesDnb = ifaddressCloselyMatchesDnb(matches, soldTo, admin, data.getCmrIssuingCntry());
            }
            if (!matchesDnb) {
              cmdeReview = true;
              engineData.addNegativeCheckStatus("_atVATCheckFailed", "VAT # on the request did not match D&B");
              details.append("VAT # on the request did not match D&B\n");
            } else {
              details.append("VAT # on the request matches D&B\n");
            }
          }
        }
        break;
      case "Central order block code":
        if ("94".equals(change.getOldData()) || "94".equals(change.getNewData())) {
          cmdeReview = true;
        }
        break;
      case "Sensitive Flag":
      case "ISIC":
      case "Subindustry":
      case "INAC/NAC Code":
      case "Company Number":
        cmdeReview = true;
        break;
      case "Client Tier Code":
        // noop, for switch handling only
        break;
      case "ISU Code":
        // noop, for switch handling only
        break;
      case "SBO":
        // noop, for switch handling only
        break;
      default:
        ignoredUpdates.add(change.getDataField());
        break;
      }
    }

    if (cmdeReview) {
      engineData.addNegativeCheckStatus("_atDataCheckFailed", "Updates to one or more fields cannot be validated.");
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
    LOG.debug("Verifying PayGo Accreditation for " + admin.getSourceSystId());
    boolean payGoAddredited = RequestUtils.isPayGoAccredited(entityManager, admin.getSourceSystId());
    boolean isOnlyPayGoUpdated = changes != null && changes.isAddressChanged("PG01") && !changes.isAddressChanged("ZS01")
        && !changes.isAddressChanged("ZI01");

    StringBuilder duplicateDetails = new StringBuilder();
    StringBuilder checkDetails = new StringBuilder();

    // D - duplicates, R - review
    Set<String> resultCodes = new HashSet<String>();

    for (String addrType : RELEVANT_ADDRESSES) {
      if (changes.isAddressChanged(addrType)) {
        if (CmrConstants.RDC_SOLD_TO.equals(addrType)) {
          addresses = Collections.singletonList(requestData.getAddress(CmrConstants.RDC_SOLD_TO));
        } else {
          addresses = requestData.getAddresses(addrType);
        }
        for (Addr addr : addresses) {
          List<String> addrTypesChanged = new ArrayList<String>();
          for (UpdatedNameAddrModel addrModel : changes.getAddressUpdates()) {
            if (!addrTypesChanged.contains(addrModel.getAddrTypeCode())) {
              addrTypesChanged.add(addrModel.getAddrTypeCode());
            }
          }
          boolean isZS01WithAufsdPG = (CmrConstants.RDC_SOLD_TO.equals(addrType) && "PG".equals(data.getOrdBlk()));

          if ("N".equals(addr.getImportInd())) {
            // new address

            LOG.debug("Checking duplicates for " + addrType + "(" + addr.getId().getAddrSeq() + ")");
            boolean duplicate = addressExists(entityManager, addr, requestData);
            if (duplicate) {
              LOG.debug(" - Duplicates found for " + addrType + "(" + addr.getId().getAddrSeq() + ")");
              duplicateDetails.append("Address " + addrType + "(" + addr.getId().getAddrSeq() + ") provided matches an existing address.\n");
              resultCodes.add("D");
            } else if ((payGoAddredited && addrTypesChanged.contains(CmrConstants.RDC_PAYGO_BILLING.toString())) || isZS01WithAufsdPG) {
              if ("N".equals(addr.getImportInd())) {
                LOG.debug("Checking duplicates for " + addrType + "(" + addr.getId().getAddrSeq() + ")");
                duplicate = addressExists(entityManager, addr, requestData);
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

            } else {
              LOG.debug(" - NO duplicates found for " + addrType + "(" + addr.getId().getAddrSeq() + ")");
              if (CmrConstants.RDC_SHIP_TO.equals(addrType)) {
                LOG.debug("Addition of " + addrType + "(" + addr.getId().getAddrSeq() + ")");
                checkDetails.append("Addition of new ZD01 (" + addr.getId().getAddrSeq() + ") address skipped in the checks.\n");
              } else if (CmrConstants.RDC_PAYGO_BILLING.equals(addrType)) {
                LOG.debug("Addition of " + addrType + "(" + addr.getId().getAddrSeq() + ")");
                checkDetails.append("Addition of new PG01 (" + addr.getId().getAddrSeq() + ") address validated in the checks.\n");
              } else if (CmrConstants.RDC_INSTALL_AT.equals(addrType)) {
                LOG.debug("Addition of " + addrType + "(" + addr.getId().getAddrSeq() + ")");
                checkDetails.append("Addition of new ZI01 (" + addr.getId().getAddrSeq() + ") address validated in the checks.\n");
              } else {
                List<DnBMatchingResponse> matches = getMatches(requestData, engineData, addr, false);
                boolean matchesDnb = false;
                if (matches != null) {
                  // check against D&B
                  matchesDnb = ifaddressCloselyMatchesDnb(matches, addr, admin, data.getCmrIssuingCntry());
                }
                if (!matchesDnb) {
                  LOG.debug("New address " + addrType + "(" + addr.getId().getAddrSeq() + ") does not match D&B");
                  resultCodes.add("R");
                  checkDetails.append("New address " + addrType + "(" + addr.getId().getAddrSeq() + ") did not match D&B records.\n");
                } else {
                  checkDetails.append("New address " + addrType + "(" + addr.getId().getAddrSeq() + ") matches D&B records. Matches:\n");
                  for (DnBMatchingResponse dnb : matches) {
                    checkDetails.append(" - DUNS No.:  " + dnb.getDunsNo() + " \n");
                    checkDetails.append(" - Name.:  " + dnb.getDnbName() + " \n");
                    checkDetails.append(" - Address:  " + dnb.getDnbStreetLine1() + " " + dnb.getDnbCity() + " " + dnb.getDnbPostalCode() + " "
                        + dnb.getDnbCountry() + "\n\n");
                  }
                }
              }
            }
          } else if ("Y".equals(addr.getChangedIndc())) {
            // updated addresses
            if (CmrConstants.RDC_SHIP_TO.equals(addrType)) {
              // just proceed for shipping updates
              LOG.debug("Update to ZD01 " + addrType + "(" + addr.getId().getAddrSeq() + ")");
              checkDetails.append("Updates to ZD01 (" + addr.getId().getAddrSeq() + ") skipped in the checks.\n");
            } else if (isZS01WithAufsdPG || "PG".equals(data.getOrdBlk())) {
              LOG.debug("Update to " + addrType + "(" + addr.getId().getAddrSeq() + ") with Aufsd = " + data.getOrdBlk());
              checkDetails.append(
                  "Updates to " + addrType + "(" + addr.getId().getAddrSeq() + ") with Aufsd = " + data.getOrdBlk() + " skipped in the checks.\n");
            } else if (CmrConstants.RDC_SOLD_TO.equals(addrType)) {
              if (isRelevantAddressFieldUpdatedZS01ZP01(changes, addr)) {
                List<DnBMatchingResponse> matches = getMatches(requestData, engineData, addr, false);
                boolean matchesDnb = false;
                if (matches != null) {
                  // check against D&B
                  matchesDnb = ifaddressCloselyMatchesDnb(matches, addr, admin, data.getCmrIssuingCntry());
                }
                if (!matchesDnb) {
                  resultCodes.add("R");
                  checkDetails.append("Updates to Sold To address need verification as it does not matches D&B");
                  LOG.debug("Updates to Sold To address need verification as it does not matches D&B");
                } else {
                  checkDetails.append("Updated address " + addrType + "(" + addr.getId().getAddrSeq() + ") matches D&B records. Matches:\n");
                  for (DnBMatchingResponse dnb : matches) {
                    checkDetails.append(" - DUNS No.:  " + dnb.getDunsNo() + " \n");
                    checkDetails.append(" - Name.:  " + dnb.getDnbName() + " \n");
                    checkDetails.append(" - Address:  " + dnb.getDnbStreetLine1() + " " + dnb.getDnbCity() + " " + dnb.getDnbPostalCode() + " "
                        + dnb.getDnbCountry() + "\n\n");
                  }
                }
              } else if (isNonRelevantAddressFieldUpdatedForZS01ZP01(changes, addr)) {
                checkDetails.append("Updates to non-address fields for " + addrType + "(" + addr.getId().getAddrSeq() + ") skipped in the checks.")
                    .append("\n");
              }
            } else if (CmrConstants.RDC_BILL_TO.equals(addrType)) {
              if (isRelevantAddressFieldUpdatedZS01ZP01(changes, addr)) {
                List<DnBMatchingResponse> matches = getMatches(requestData, engineData, addr, false);
                boolean matchesDnb = false;
                if (matches != null) {
                  // check against D&B
                  matchesDnb = ifaddressCloselyMatchesDnb(matches, addr, admin, data.getCmrIssuingCntry());
                }
                if (!matchesDnb) {
                  resultCodes.add("R");
                  checkDetails.append("Updates to Bill To address need verification as it does not matches D&B");
                  LOG.debug("Updates to Bill To address need verification as it does not matches D&B");
                } else {
                  checkDetails.append("Updated address " + addrType + "(" + addr.getId().getAddrSeq() + ") matches D&B records. Matches:\n");
                  for (DnBMatchingResponse dnb : matches) {
                    checkDetails.append(" - DUNS No.:  " + dnb.getDunsNo() + " \n");
                    checkDetails.append(" - Name.:  " + dnb.getDnbName() + " \n");
                    checkDetails.append(" - Address:  " + dnb.getDnbStreetLine1() + " " + dnb.getDnbCity() + " " + dnb.getDnbPostalCode() + " "
                        + dnb.getDnbCountry() + "\n\n");
                  }
                }
              } else if (isNonRelevantAddressFieldUpdatedForZS01ZP01(changes, addr)) {
                checkDetails.append("Updates to non-address fields for " + addrType + "(" + addr.getId().getAddrSeq() + ") skipped in the checks.")
                    .append("\n");
              }
            } else {
              // update to other relevant addresses
              if (isRelevantAddressFieldUpdated(changes, addr)) {
                checkDetails.append("Updates to address fields for " + addrType + "(" + addr.getId().getAddrSeq() + ") need to be verified.")
                    .append("\n");
                resultCodes.add("R");
              } else {
                checkDetails.append("Updates to non-address fields for " + addrType + "(" + addr.getId().getAddrSeq() + ") skipped in the checks.")
                    .append("\n");
              }
            }
          }
        }
      }
    }

    if (resultCodes.contains("D")) {
      // prioritize duplicates, set error
      output.setOnError(true);
      engineData.addRejectionComment("DUPADDR", "One or more new addresses matches existing addresses on record.", "", "");
      validation.setSuccess(false);
      validation.setMessage("Duplicate Address");
    } else if (resultCodes.contains("R")) {
      validation.setSuccess(false);
      validation.setMessage("Not Validated");
      engineData.addNegativeCheckStatus("_atCheckFailed", "Updated elements cannot be checked automatically.");
    } else {
      validation.setSuccess(true);
      validation.setMessage("Successful");
    }

    String details = (output.getDetails() != null && output.getDetails().length() > 0) ? output.getDetails() : "";
    details += duplicateDetails.length() > 0 ? duplicateDetails.toString() : "";
    details += checkDetails.length() > 0 ? "\n" + checkDetails.toString() : "";
    output.setDetails(details);
    output.setProcessOutput(validation);

    return true;
  }

  @Override
  protected List<String> getCountryLegalEndings() {
    return Arrays.asList("AG", "GmbH", "e.U.", "GesmbH & Co KG", "GmbH & Co KG");
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

    Data data = requestData.getData();
    Admin admin = requestData.getAdmin();
    if ("C".equals(admin.getReqType())) {
      String scenario = data.getCustSubGrp();
      LOG.info("Starting Field Computations for Request ID " + data.getId().getReqId());
      if (StringUtils.isNotBlank(scenario)
          && !(SCENARIO_BUSINESS_PARTNER.equals(scenario) || SCENARIO_IBM_EMPLOYEE.equals(scenario) || SCENARIO_PRIVATE_CUSTOMER.equals(scenario)
              || SCENARIO_INTERNAL.equals(scenario) || SCENARIO_INTERNAL_SO.equals(scenario) || SCENARIO_BUSINESS_PARTNER_CROSS.equals(scenario))
          && data.getSubIndustryCd() != null && data.getSubIndustryCd().startsWith("B") && "32".equals(data.getIsuCd())
          && "S".equals(data.getClientTier())) {
        details.append("Found IMS value 'B' on the request, setting ISU-CTC as 32-N").append("\n");
        overrides.addOverride(AutomationElementRegistry.GBL_FIELD_COMPUTE, "DATA", "ISU_CD", data.getIsuCd(), "32");
        overrides.addOverride(AutomationElementRegistry.GBL_FIELD_COMPUTE, "DATA", "CLIENT_TIER", data.getClientTier(), "N");
        results.setResults("Computed");
      } else {
        details.append("No specific fields to compute.\n");
        results.setResults("Skipped");
      }
    } else {
      details.append("No specific fields to compute.\n");
      results.setResults("Skipped");
    }
    results.setDetails(details.toString());

    return results;
  }

  @Override
  public boolean performCountrySpecificCoverageCalculations(CalculateCoverageElement covElement, EntityManager entityManager,
      AutomationResult<OverrideOutput> results, StringBuilder details, OverrideOutput overrides, RequestData requestData,
      AutomationEngineData engineData, String covFrom, CoverageContainer container, boolean isCoverageCalculated) throws Exception {
    if (!"C".equals(requestData.getAdmin().getReqType())) {
      details.append("Coverage Calculation skipped for Updates.");
      results.setResults("Skipped");
      results.setDetails(details.toString());
      overrides.clearOverrides();
      return true;
    }
    Data data = requestData.getData();
    String scenario = data.getCustSubGrp();
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
    String coverageId = container.getFinalCoverage();
    String coverage = data.getSearchTerm();
    System.out.println("sortl----------" + coverage);
    System.out.println("coverageId----------" + coverageId);
    List<String> covList = Arrays.asList("A0004520", "A0004515", "A0004541", "A0004580");
    LOG.info("Starting coverage calculations for Request ID " + requestData.getData().getId().getReqId());
    switch (scenario) {
    case SCENARIO_COMMERCIAL:
    case SCENARIO_CROSS_COMMERICAL:
    case SCENARIO_THIRD_PARTY_DC:
    case SCENARIO_PRIVATE_CUSTOMER:
      sbo = getSBOFromIMS(entityManager, data.getSubIndustryCd(), isuCd, clientTier);
      break;
    }

    if ((SCENARIO_COMMERCIAL.equals(scenario) || SCENARIO_GOVERNMENT.equals(scenario)) && StringUtils.isNotBlank(coverage)
        && covList.contains(coverage)) {
      details.append("Setting Isu ctc to 28-7 based on coverage.");
      overrides.addOverride(covElement.getProcessCode(), "DATA", "ISU_CD", data.getIsuCd(), "28");
      overrides.addOverride(covElement.getProcessCode(), "DATA", "CLIENT_TIER", data.getClientTier(), "7");
    }
    if (StringUtils.isNotBlank(sbo)) {
      details.append("Setting SBO to " + sbo + " based on IMS mapping rules.");
      overrides.addOverride(covElement.getProcessCode(), "DATA", "SALES_BO_CD", data.getSalesBusOffCd(), sbo);
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
    }

    return true;

  }

  private String getSBOFromIMS(EntityManager entityManager, String subIndustryCd, String isuCd, String clientTier) {
    List<String> sboValues = new ArrayList<>();
    String isuCtc = (StringUtils.isNotBlank(isuCd) ? isuCd : "") + (StringUtils.isNotBlank(clientTier) ? clientTier : "");
    if (StringUtils.isNotBlank(subIndustryCd) && ("32S".equals(isuCtc) || "32N".equals(isuCtc) || "32T".equals(isuCtc) || "34Q".equals(isuCtc))) {
      String ims = subIndustryCd.substring(0, 1);
      String sql = ExternalizedQuery.getSql("AUTO.AT.GET_SBOLIST_FROM_ISUCTC");
      PreparedQuery query = new PreparedQuery(entityManager, sql);
      query.setParameter("ISU", "%" + isuCtc + "%");
      query.setParameter("ISSUING_CNTRY", SystemLocation.AUSTRIA);
      query.setParameter("UPDATE_BY_ID", "%" + ims + "%");
      query.setForReadOnly(true);
      sboValues = query.getResults(String.class);
    } else {
      String sql = ExternalizedQuery.getSql("AUTO.AT.GET_SBOLIST_FROM_ISU");
      PreparedQuery query = new PreparedQuery(entityManager, sql);
      query.setParameter("ISU", "%" + isuCtc + "%");
      query.setParameter("ISSUING_CNTRY", SystemLocation.AUSTRIA);
      sboValues = query.getResults(String.class);
    }
    if (sboValues != null) {
      return sboValues.get(0);
    } else {
      return "";
    }
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
      LOG.debug("Setting isu ctc to 34Y based on gbg matching.");
      details.append("Setting isu ctc to 34Y based on gbg matching.");
      overrides.addOverride(covElement.getProcessCode(), "DATA", "ISU_CD", data.getIsuCd(), "34");
      overrides.addOverride(covElement.getProcessCode(), "DATA", "CLIENT_TIER", data.getClientTier(), "Y");
      overrides.addOverride(covElement.getProcessCode(), "DATA", "SEARCH_TERM", data.getSearchTerm(), "T0007972");
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
    return Arrays.asList("C", "U", "M", "D", "R");
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
