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
import com.ibm.cio.cmr.request.util.SystemLocation;
import com.ibm.cmr.services.client.matching.MatchingResponse;
import com.ibm.cmr.services.client.matching.cmr.DuplicateCMRCheckResponse;
import com.ibm.cmr.services.client.matching.dnb.DnBMatchingResponse;

/**
 * {@link AutomationUtil} for Spain specific validations
 * 
 *
 */
public class SpainUtil extends AutomationUtil {

  private static final Logger LOG = Logger.getLogger(SpainUtil.class);
  public static final String SCENARIO_BUSINESS_PARTNER = "BUSPR";
  public static final String SCENARIO_BUSINESS_PARTNER_CROSS = "XBP";
  public static final String SCENARIO_PRIVATE_CUSTOMER = "PRICU";
  public static final String SCENARIO_THIRD_PARTY = "THDPT";
  public static final String SCENARIO_THIRD_PARTY_IG = "THDIG";
  public static final String SCENARIO_INTERNAL = "INTER";
  public static final String SCENARIO_INTERNAL_SO = "INTSO";

  private static final List<String> RELEVANT_ADDRESSES = Arrays.asList(CmrConstants.RDC_SOLD_TO, CmrConstants.RDC_BILL_TO,
      CmrConstants.RDC_INSTALL_AT, CmrConstants.RDC_SHIP_TO, CmrConstants.RDC_SECONDARY_SOLD_TO);

  @Override
  public boolean performScenarioValidation(EntityManager entityManager, RequestData requestData, AutomationEngineData engineData,
      AutomationResult<ValidationOutput> result, StringBuilder details, ValidationOutput output) {
    Data data = requestData.getData();
    String scenario = data.getCustSubGrp();
    Addr soldTo = requestData.getAddress("ZS01");
    String customerName = soldTo.getCustNm1() + (!StringUtils.isBlank(soldTo.getCustNm2()) ? " " + soldTo.getCustNm2() : "");

    LOG.info("Starting scenario validations for Request ID " + data.getId().getReqId());

    LOG.debug("Scenario to check: " + scenario);

    switch (scenario) {
    case SCENARIO_PRIVATE_CUSTOMER:
      return doPrivatePersonChecks(engineData, SystemLocation.SPAIN, soldTo.getLandCntry(), customerName, details, true);
    case SCENARIO_BUSINESS_PARTNER:
    case SCENARIO_BUSINESS_PARTNER_CROSS:
      return doBusinessPartnerChecks(engineData, data.getPpsceid(), details);
    }
    return true;
  }

  @Override
  public AutomationResult<OverrideOutput> doCountryFieldComputations(EntityManager entityManager, AutomationResult<OverrideOutput> results,
      StringBuilder details, OverrideOutput overrides, RequestData requestData, AutomationEngineData engineData) throws Exception {
    // TODO Auto-generated method stub
    return null;
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
            engineData.addNegativeCheckStatus("_atVATCheckFailed", "VAT # on the request did not match D&B");
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
      engineData.addRejectionComment("_atVATUpd", "VAT # on the request has characters updated other than the first character", "", "");
      validation.setSuccess(false);
      validation.setMessage("VAT Updated");
    } else if (cmdeReview) {
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
              checkDetails.append("Addition of new ZD01 and ZD02(" + addr.getId().getAddrSeq() + ") address skipped in the checks.\n");
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
      engineData.addNegativeCheckStatus("_atCheckFailed", "Updated elements cannot be checked automatically.");
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
    String address = "";

    LOG.debug("Address for the scenario to check: " + scenario);
    if (SCENARIO_THIRD_PARTY.equals(scenario) || SCENARIO_THIRD_PARTY_IG.equals(scenario)) {
      address = "ZI01";
    }
    return address;
  }

}
