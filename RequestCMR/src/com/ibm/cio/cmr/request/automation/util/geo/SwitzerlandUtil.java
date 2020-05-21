/**
 * 
 */
package com.ibm.cio.cmr.request.automation.util.geo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.automation.AutomationEngineData;
import com.ibm.cio.cmr.request.automation.RequestData;
import com.ibm.cio.cmr.request.automation.out.AutomationResult;
import com.ibm.cio.cmr.request.automation.out.OverrideOutput;
import com.ibm.cio.cmr.request.automation.out.ValidationOutput;
import com.ibm.cio.cmr.request.automation.util.AutomationUtil;
import com.ibm.cio.cmr.request.automation.util.RequestChangeContainer;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.model.window.UpdatedDataModel;
import com.ibm.cio.cmr.request.model.window.UpdatedNameAddrModel;
import com.ibm.cio.cmr.request.util.SystemLocation;
import com.ibm.cmr.services.client.matching.dnb.DnBMatchingResponse;

import edu.emory.mathcs.backport.java.util.Collections;

/**
 * {@link AutomationUtil} for Switzwerland/LI country specific validations
 * 
 * @author JeffZAMORA
 *
 */
public class SwitzerlandUtil extends AutomationUtil {

  private static final Logger LOG = Logger.getLogger(SwitzerlandUtil.class);
  private static final String SCENARIO_COMMERCIAL = "COM";
  private static final String SCENARIO_GOVERNMENT = "GOV";
  private static final String SCENARIO_INTERNAL = "INT";
  private static final String SCENARIO_THIRD_PARTY = "3PA";
  private static final String SCENARIO_PRIVATE_CUSTOMER = "PRI";
  private static final String SCENARIO_IBM_EMPLOYEE = "IBM";
  private static final String SCENARIO_BUSINESS_PARTNER = "BUS";
  private static final String SCENARIO_CROSS_BORDER = "XCHCM";

  private static final List<String> RELEVANT_ADDRESSES = Arrays.asList(CmrConstants.RDC_SOLD_TO, CmrConstants.RDC_BILL_TO,
      CmrConstants.RDC_INSTALL_AT, CmrConstants.RDC_SHIP_TO);

  private static final List<String> NON_RELEVANT_ADDRESS_FIELDS = Arrays.asList("Phone #", "FAX", "Customer Name 4");

  @Override
  public boolean performScenarioValidation(EntityManager entityManager, RequestData requestData, AutomationEngineData engineData,
      AutomationResult<ValidationOutput> result, StringBuilder details, ValidationOutput output) {
    Data data = requestData.getData();
    Addr soldTo = requestData.getAddress("ZS01");
    String scenario = data.getCustSubGrp();
    LOG.info("Starting scenario validations for Request ID " + data.getId().getReqId());

    if (StringUtils.isBlank(scenario) || scenario.length() != 5) {
      details.append("Scenario not correctly specified on the request");
      engineData.addNegativeCheckStatus("_chNoScenario", "Scenario not correctly specified on the request");
      return true;
    }

    String actualScenario = scenario.substring(2);
    String customerName = soldTo.getCustNm1() + (!StringUtils.isBlank(soldTo.getCustNm2()) ? " " + soldTo.getCustNm2() : "");

    if (!SCENARIO_THIRD_PARTY.equals(actualScenario) && (customerName.toUpperCase().contains("C/O") || customerName.toUpperCase().contains("CAREOF")
        || customerName.toUpperCase().contains("CARE OF"))) {
      details.append("Scenario should be Third Party/Data Center based on custmer name.").append("\n");
      engineData.addNegativeCheckStatus("PPSCEID", "Scenario should be Third Party/Data Center based on custmer name.");
      return true;
    }
    LOG.debug("Scenario to check: " + actualScenario);

    if (SCENARIO_CROSS_BORDER.equals(scenario)) {
      // noop
    } else {
      switch (actualScenario) {
      case SCENARIO_COMMERCIAL:
        break;
      case SCENARIO_PRIVATE_CUSTOMER:
      case SCENARIO_IBM_EMPLOYEE:
        return doPrivatePersonChecks(engineData, SystemLocation.SWITZERLAND, soldTo.getLandCntry(), customerName, details,
            SCENARIO_IBM_EMPLOYEE.equals(actualScenario));
      case SCENARIO_INTERNAL:
        break;
      case SCENARIO_GOVERNMENT:
        break;
      case SCENARIO_THIRD_PARTY:
        engineData.addNegativeCheckStatus("_chThirdParty", "Third Party/Data Center request needs further validation.");
        details.append("Third Party/Data Center request needs further validation.").append("\n");
        break;
      case SCENARIO_BUSINESS_PARTNER:
        return doBusinessPartnerChecks(engineData, data.getPpsceid(), details);
      }
    }

    return true;
  }

  @Override
  protected List<String> getCountryLegalEndings() {
    return Arrays.asList("GMBH", "KLG", " AG", "Sàrl", "SARL", " SA", "S.A.", "SAGL");
  }

  @SuppressWarnings("unchecked")
  @Override
  public boolean runUpdateChecksForAddress(EntityManager entityManager, AutomationEngineData engineData, RequestData requestData,
      RequestChangeContainer changes, AutomationResult<ValidationOutput> output, ValidationOutput validation) throws Exception {

    Admin admin = requestData.getAdmin();
    Data data = requestData.getData();
    if (handlePrivatePersonRecord(entityManager, admin, output, validation, engineData)) {
      return true;
    }

    List<Addr> addresses = null;

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
          if ("N".equals(addr.getImportInd())) {
            // new address
            LOG.debug("Checking duplicates for " + addrType + "(" + addr.getId().getAddrSeq() + ")");
            boolean duplicate = addressExists(entityManager, addr);
            if (duplicate) {
              LOG.debug(" - Duplicates found for " + addrType + "(" + addr.getId().getAddrSeq() + ")");
              duplicateDetails.append("Address " + addrType + "(" + addr.getId().getAddrSeq() + ") provided matches an existing address.\n");
              resultCodes.add("D");
            } else {
              LOG.debug(" - NO duplicates found for " + addrType + "(" + addr.getId().getAddrSeq() + ")");
              if (CmrConstants.RDC_SHIP_TO.equals(addrType)) {
                LOG.debug("Addition of " + addrType + "(" + addr.getId().getAddrSeq() + ")");
                checkDetails.append("Addition of new ZD01 (" + addr.getId().getAddrSeq() + ") address skipped in the checks.\n");
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
      engineData.addRejectionComment("_chDupAddr", "One or more new addresses matches existing addresses on record.", "", "");
      validation.setSuccess(false);
      validation.setMessage("Duplicate Address");
    } else if (resultCodes.contains("R")) {
      validation.setSuccess(false);
      validation.setMessage("Not Validated");
      engineData.addNegativeCheckStatus("_chCheckFailed", "Updated elements cannot be checked automatically.");
    } else {
      validation.setSuccess(true);
      validation.setMessage("Successful");
    }

    String details = duplicateDetails.length() > 0 ? duplicateDetails.toString() : "";
    details += checkDetails.length() > 0 ? "\n" + checkDetails.toString() : "";
    output.setDetails(details);
    output.setProcessOutput(validation);

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
        if (!StringUtils.isBlank(change.getOldData()) && !StringUtils.isBlank(change.getNewData())) {
          Addr soldTo = requestData.getAddress(CmrConstants.RDC_SOLD_TO);
          List<DnBMatchingResponse> matches = getMatches(requestData, engineData, soldTo, true);
          boolean matchesDnb = false;
          if (matches != null) {
            // check against D&B
            matchesDnb = ifaddressCloselyMatchesDnb(matches, soldTo, admin, data.getCmrIssuingCntry());
          }
          if (!matchesDnb) {
            cmdeReview = true;
            engineData.addNegativeCheckStatus("_chVATCheckFailed", "VAT # on the request did not match D&B");
            details.append("VAT # on the request did not match D&B\n");
          } else {
            details.append("VAT # on the request matches D&B\n");
          }

        }
        break;
      case "Order Block Code":
        if ("94".equals(change.getOldData()) || "94".equals(change.getNewData())) {
          cmdeReview = true;
        }
        break;
      case "ISIC":
      case "Subindustry":
      case "INAC/NAC Code":
        cmdeReview = true;
        break;
      case "Tax Code":
        // noop, for switch handling only
        break;
      case "Client Tier Code":
        // noop, for switch handling only
        break;
      case "ISU Code":
        // noop, for switch handling only
        break;
      case "MUBOTY(SORTL)":
        // noop, for switch handling only
        break;
      default:
        ignoredUpdates.add(change.getDataField());
        break;
      }
    }

    if (cmdeReview) {
      engineData.addNegativeCheckStatus("_chDataCheckFailed", "Updates to one or more fields cannot be validated.");
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
  public AutomationResult<OverrideOutput> doCountryFieldComputations(EntityManager entityManager, AutomationResult<OverrideOutput> results,
      StringBuilder details, OverrideOutput overrides, RequestData requestData, AutomationEngineData engineData) throws Exception {
    details.append("Fields Computation Element development is in progress...\n");
    results.setResults("Fields Computation Element development is in progress...");
    results.setDetails(details.toString());

    return results;
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
}
