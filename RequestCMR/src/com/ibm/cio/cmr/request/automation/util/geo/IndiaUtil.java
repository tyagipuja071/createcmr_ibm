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
import com.ibm.cio.cmr.request.model.window.UpdatedNameAddrModel;
import com.ibm.cmr.services.client.matching.MatchingResponse;
import com.ibm.cmr.services.client.matching.cmr.DuplicateCMRCheckResponse;
import com.ibm.cmr.services.client.matching.dnb.DnBMatchingResponse;

public class IndiaUtil extends AutomationUtil {

  public static final String SCENARIO_ESOSW = "ESOSW";
  public static final String SCENARIO_NORMAL = "NRML";
  public static final String SCENARIO_INTERNAL = "INTER";
  public static final String SCENARIO_ACQUISITION = "AQSTN";
  public static final String SCENARIO_DUMMY = "DUMMY";
  public static final String SCENARIO_IGF = "IGF";
  public static final String SCENARIO_PRIVATE_CUSTOMER = "PRIV";
  public static final String SCENARIO_FOREIGN = "CROSS";
  private static final List<String> RELEVANT_ADDRESSES = Arrays.asList(CmrConstants.RDC_SOLD_TO, CmrConstants.RDC_BILL_TO,
      CmrConstants.RDC_INSTALL_AT, CmrConstants.RDC_SECONDARY_SHIPPING);
  private static final List<String> NON_RELEVANT_ADDRESS_FIELDS = Arrays.asList("Attn", "Phone #");

  private static final Logger LOG = Logger.getLogger(IndiaUtil.class);

  @Override
  public AutomationResult<OverrideOutput> doCountryFieldComputations(EntityManager entityManager, AutomationResult<OverrideOutput> results,
      StringBuilder details, OverrideOutput overrides, RequestData requestData, AutomationEngineData engineData) throws Exception {
    // TODO Auto-generated method stub
    Admin admin = requestData.getAdmin();
    Data data = requestData.getData();
    String scenario = data.getCustSubGrp();
    if (!"C".equals(admin.getReqType())) {
      details.append("Field Computation skipped for Updates.");
      results.setResults("Skipped");
      results.setDetails(details.toString());
      return results;
    }
    details.append("No specific fields to calculate.");
    results.setResults("Skipped.");
    results.setProcessOutput(overrides);
    results.setDetails(details.toString());
    LOG.debug(results.getDetails());
    return results;
  }

  @Override
  public boolean performScenarioValidation(EntityManager entityManager, RequestData requestData, AutomationEngineData engineData,
      AutomationResult<ValidationOutput> result, StringBuilder details, ValidationOutput output) {
    Data data = requestData.getData();
    String scenario = data.getCustSubGrp();
    switch (scenario) {
    case SCENARIO_ACQUISITION:
    case SCENARIO_NORMAL:
    case SCENARIO_ESOSW:
    case SCENARIO_FOREIGN:
      if (data.getApCustClusterId().contains("012D999")) {
        details.append("Cluster cannot be default as 012D999.").append("\n");
        engineData.addRejectionComment("OTH", "Cluster cannot be default as 012D999.", "", "");
        return false;
      }
      break;
    case SCENARIO_INTERNAL:
      for (String addrType : RELEVANT_ADDRESSES) {
        List<Addr> addresses = requestData.getAddresses(addrType);
        for (Addr addr : addresses) {
          String custNmTrimmed = getCustomerFullName(addr);
          if (!(custNmTrimmed.toUpperCase().contains("IBM INDIA") || custNmTrimmed.toUpperCase().contains("INTERNATIONAL BUSINESS MACHINES INDIA"))) {
            details.append("Customer Name in all addresses should contain IBM India for Internal Scenario.").append("\n");
            engineData.addRejectionComment("OTH", "Customer Name in all addresses should contain IBM India for Internal Scenario.", "", "");
            return false;
          }
        }
      }
      break;
    case SCENARIO_PRIVATE_CUSTOMER:
      for (String addrType : RELEVANT_ADDRESSES) {
        List<Addr> addresses = requestData.getAddresses(addrType);
        for (Addr addr : addresses) {
          String custNmTrimmed = getCustomerFullName(addr);
          if (custNmTrimmed.toUpperCase().contains("PRIVATE LIMITED") || custNmTrimmed.toUpperCase().contains("COMPANY")
              || custNmTrimmed.toUpperCase().contains("CORPORATION") || custNmTrimmed.toUpperCase().contains("INCORPORATE")
              || custNmTrimmed.toUpperCase().contains("ORGANIZATION") || custNmTrimmed.toUpperCase().contains("PVT LTD")) {
            details.append("Customer name should not contain 'Private Limited', 'Company', 'Corporation', 'Incorporate', 'Organization', 'Pvt Ltd' .")
                .append("\n");
            engineData.addRejectionComment("OTH",
                "Customer name should not contain 'Private Limited', 'Company', 'Corporation', 'Incorporate', 'Organization', 'Pvt Ltd' .", "", "");
            return false;
          }
        }
      }
      break;
    case SCENARIO_DUMMY:
    case SCENARIO_IGF:
      engineData.addPositiveCheckStatus(AutomationEngineData.SKIP_GBG);
      break;

    }
    return true;
  }

  @Override
  public void filterDuplicateCMRMatches(EntityManager entityManager, RequestData requestData, AutomationEngineData engineData,
      MatchingResponse<DuplicateCMRCheckResponse> response) {
    String[] scenariosToBeChecked = { "ESOSW" };
    String scenario = requestData.getData().getCustSubGrp();
    if (Arrays.asList(scenariosToBeChecked).contains(scenario)) {
      List<DuplicateCMRCheckResponse> matches = response.getMatches();
      List<DuplicateCMRCheckResponse> filteredMatches = new ArrayList<DuplicateCMRCheckResponse>();
      for (DuplicateCMRCheckResponse match : matches) {
        if (StringUtils.isNotBlank(match.getCmrNo())) {
          String cmrFound = match.getCmrNo().substring(0, 3);
          if ("800".equals(cmrFound)) {
            filteredMatches.add(match);
          }
        }
      }
      // set filtered matches in response
      response.setMatches(filteredMatches);
    }
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
          if ("Y".equals(addr.getChangedIndc())) {
            // update address
            if (RELEVANT_ADDRESSES.contains(addrType)) {
              if (isRelevantAddressFieldUpdated(changes, addr)) {
                Addr addressToChk = requestData.getAddress(addrType);
                List<DnBMatchingResponse> matches = getMatches(requestData, engineData, addressToChk, true);
                boolean matchesDnb = false;
                if (matches != null) {
                  // check against D&B
                  matchesDnb = ifaddressCloselyMatchesDnb(matches, addr, admin, data.getCmrIssuingCntry());
                }
                if (!matchesDnb) {
                  LOG.debug("Update address for " + addrType + "(" + addr.getId().getAddrSeq() + ") does not match D&B");
                  resultCodes.add("D");
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
    if (resultCodes.contains("D")) {
      validation.setSuccess(false);
      validation.setMessage("Not Validated");
      engineData.addNegativeCheckStatus("_atCheckFailed", "Updates to addresses cannot be checked automatically.");
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
  public boolean runUpdateChecksForData(EntityManager entityManager, AutomationEngineData engineData, RequestData requestData,
      RequestChangeContainer changes, AutomationResult<ValidationOutput> output, ValidationOutput validation) throws Exception {
    Admin admin = requestData.getAdmin();
    Data data = requestData.getData();
    Addr soldTo = requestData.getAddress(CmrConstants.RDC_SOLD_TO);
    if (handlePrivatePersonRecord(entityManager, admin, output, validation, engineData)) {
      return true;
    }
    StringBuilder details = new StringBuilder();
    Set<String> resultCodes = new HashSet<String>();// D for Reject
    List<String> ignoredUpdates = new ArrayList<String>();
    for (UpdatedDataModel change : changes.getDataUpdates()) {
      switch (change.getDataField()) {

      case "GST #":
        // For GST update, Match with DnB and API
        if (!StringUtils.isBlank(change.getNewData()) && !(change.getNewData().equals(change.getOldData()))) {
          // UPDATE
          List<DnBMatchingResponse> matches = getMatches(requestData, engineData, soldTo, true);
          boolean matchesDnb = false;
          if (matches != null) {
            // check against D&B
            matchesDnb = ifaddressCloselyMatchesDnb(matches, soldTo, admin, data.getCmrIssuingCntry());
          }
          if (!matchesDnb) {
            resultCodes.add("R");
            details.append("GST# update on the request did not match D&B\n");
          } else {
            details.append("GST# update on the request matches D&B\n");
          }
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
