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
import com.ibm.cio.cmr.request.automation.impl.gbl.CalculateCoverageElement;
import com.ibm.cio.cmr.request.automation.out.AutomationResult;
import com.ibm.cio.cmr.request.automation.out.OverrideOutput;
import com.ibm.cio.cmr.request.automation.out.ValidationOutput;
import com.ibm.cio.cmr.request.automation.util.AutomationUtil;
import com.ibm.cio.cmr.request.automation.util.CoverageContainer;
import com.ibm.cio.cmr.request.automation.util.RequestChangeContainer;
import com.ibm.cio.cmr.request.automation.util.ScenarioExceptionsUtil;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.model.window.UpdatedDataModel;
import com.ibm.cio.cmr.request.model.window.UpdatedNameAddrModel;
import com.ibm.cmr.services.client.matching.dnb.DnBMatchingResponse;

public class NetherlandsUtil extends AutomationUtil {

  private static final Logger LOG = Logger.getLogger(NetherlandsUtil.class);
  public static final String SCENARIO_LOCAL_COMMERCIAL = "COMME";
  public static final String SCENARIO_CROSS_COMMERCIAL = "CBCOM";
  public static final String SCENARIO_LOCAL_PUBLIC = "PUBCU";
  public static final String SCENARIO_BP_LOCAL = "BUSPR";
  public static final String SCENARIO_BP_CROSS = "CBBUS";
  public static final String SCENARIO_PRIVATE_CUSTOMER = "PRICU";

  private static final List<String> RELEVANT_ADDRESSES = Arrays.asList(CmrConstants.RDC_SOLD_TO, CmrConstants.RDC_BILL_TO,
      CmrConstants.RDC_INSTALL_AT, CmrConstants.RDC_SHIP_TO);
  private static final List<String> NON_RELEVANT_ADDRESS_FIELDS = Arrays.asList("Attention Person", "Phone #", "Collection Code");

  @Override
  public boolean performScenarioValidation(EntityManager entityManager, RequestData requestData, AutomationEngineData engineData,
      AutomationResult<ValidationOutput> result, StringBuilder details, ValidationOutput output) {

    ScenarioExceptionsUtil scenarioExceptions = (ScenarioExceptionsUtil) engineData.get("SCENARIO_EXCEPTIONS");
    Data data = requestData.getData();
    String scenario = data.getCustSubGrp();
    Addr zs01 = requestData.getAddress("ZS01");
    Addr zp01 = requestData.getAddress("ZP01");
    String customerName = zs01.getCustNm1();
    String customerNameZP01 = "";
    String landedCountryZP01 = "";
    if (zp01 != null) {
      customerNameZP01 = StringUtils.isBlank(zp01.getCustNm1()) ? "" : zp01.getCustNm1();
      landedCountryZP01 = StringUtils.isBlank(zp01.getLandCntry()) ? "" : zp01.getLandCntry();
    }
    if (!StringUtils.equals(zs01.getLandCntry(), zp01.getLandCntry())) {
      scenarioExceptions.setCheckVATForDnB(false);
    }
    if ((SCENARIO_BP_LOCAL.equals(scenario) || SCENARIO_BP_CROSS.equals(scenario)) && zp01 != null
        && (!StringUtils.equals(getCleanString(customerName), getCleanString(customerNameZP01))
            || !StringUtils.equals(zs01.getLandCntry(), landedCountryZP01))) {
      details.append("Customer Name and Landed Country on Sold-to and Bill-to address should be same for Business Partner Scenario.").append("\n");
      engineData.addNegativeCheckStatus("SOLDTO_BILLTO_DIFF",
          "Customer Name and Landed Country on Sold-to and Bill-to address should be same for Business Partner Scenario.");
    }

    switch (scenario) {

    case SCENARIO_LOCAL_COMMERCIAL:
    case SCENARIO_CROSS_COMMERCIAL:
    case SCENARIO_LOCAL_PUBLIC:
      if (zp01 != null && (!StringUtils.equals(getCleanString(customerName), getCleanString(customerNameZP01))
          || !StringUtils.equals(zs01.getLandCntry(), landedCountryZP01))) {
        details.append("Customer Name and Landed Country on Sold-to and Bill-to address should be same for Commercial and Public Customer Scenario.")
            .append("\n");
        engineData.addNegativeCheckStatus("SOLDTO_BILLTO_DIFF",
            "Customer Name and Landed Country on Sold-to and Bill-to address should be same for Commercial and Public Customer Scenario.");
      }
      break;

    case SCENARIO_BP_LOCAL:
    case SCENARIO_BP_CROSS:
      return doBusinessPartnerChecks(engineData, data.getPpsceid(), details);

    case SCENARIO_PRIVATE_CUSTOMER:
      return doPrivatePersonChecks(engineData, data.getCmrIssuingCntry(), zs01.getLandCntry(), customerName, details, false, requestData);
    }
    return true;
  }

  @Override
  public boolean performCountrySpecificCoverageCalculations(CalculateCoverageElement covElement, EntityManager entityManager,
      AutomationResult<OverrideOutput> results, StringBuilder details, OverrideOutput overrides, RequestData requestData,
      AutomationEngineData engineData, String covFrom, CoverageContainer container, boolean isCoverageCalculated) throws Exception {
    return true;
  }

  @Override
  public AutomationResult<OverrideOutput> doCountryFieldComputations(EntityManager entityManager, AutomationResult<OverrideOutput> results,
      StringBuilder details, OverrideOutput overrides, RequestData requestData, AutomationEngineData engineData) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean runUpdateChecksForData(EntityManager entityManager, AutomationEngineData engineData, RequestData requestData,
      RequestChangeContainer changes, AutomationResult<ValidationOutput> output, ValidationOutput validation) throws Exception {
    Admin admin = requestData.getAdmin();
    Data data = requestData.getData();
    StringBuilder details = new StringBuilder();
    boolean cmdeReview = false;
    Set<String> resultCodes = new HashSet<String>();
    List<String> ignoredUpdates = new ArrayList<String>();
    for (UpdatedDataModel change : changes.getDataUpdates()) {
      switch (change.getDataField()) {
      case "VAT #":
        if (StringUtils.isBlank(change.getOldData()) && !StringUtils.isBlank(change.getNewData())) {
          // ADD
          Addr soldTo = requestData.getAddress(CmrConstants.RDC_SOLD_TO);
          Addr billTo = requestData.getAddress(CmrConstants.RDC_BILL_TO);
          if (soldTo.getLandCntry().equals(billTo.getLandCntry())) {
            List<DnBMatchingResponse> matches = getMatches(requestData, engineData, soldTo, true);
            boolean matchesDnb = false;
            if (matches != null) {
              // check against D&B
              matchesDnb = ifaddressCloselyMatchesDnb(matches, soldTo, admin, data.getCmrIssuingCntry());
            }
            if (!matchesDnb) {
              cmdeReview = true;
              engineData.addNegativeCheckStatus("_beluxVATCheckFailed", "VAT # on the request did not match D&B");
              details.append("VAT # on the request did not match D&B\n");
            } else {
              details.append("VAT # on the request matches D&B\n");
            }
          } else {
            cmdeReview = true;
            engineData.addNegativeCheckStatus("_beluxVATCheckFailed", "Sold to and Bill to have different landed country.");
          }
        }

        if (!StringUtils.isBlank(change.getOldData()) && !StringUtils.isBlank(change.getNewData())
            && !(change.getOldData().equals(change.getNewData()))) {
          // UPDATE
          cmdeReview = true;
          engineData.addNegativeCheckStatus("_beluxVATCheckFailed", "VAT updation requires cmde review.");
        }
        if (StringUtils.isBlank(change.getNewData()) && !StringUtils.isBlank(change.getOldData())) {
          // noop
        }

        break;
      case "ISIC":
      case "INAC/NAC Code":
        cmdeReview = true;
        break;
      case "Economic Code":
        String newEcoCd = change.getNewData();
        String oldEcoCd = change.getOldData();
        List<String> ls = Arrays.asList("A", "F", "C", "R");
        if (StringUtils.isNotBlank(newEcoCd) && StringUtils.isNotBlank(oldEcoCd) && newEcoCd.length() == 3 && oldEcoCd.length() == 3) {
          if (!newEcoCd.substring(0, 1).equals(oldEcoCd.substring(0, 1))
              && newEcoCd.substring(1, newEcoCd.length()).equals(oldEcoCd.substring(1, oldEcoCd.length()))) {
            if (ls.contains(newEcoCd.substring(0, 1)) && oldEcoCd.substring(0, 1).equals("K")
                || ls.contains(oldEcoCd.substring(0, 1)) && newEcoCd.substring(0, 1).equals("K")) {
              admin.setScenarioVerifiedIndc("Y");
              details.append("Economic Code has been updated by registered user to " + newEcoCd + "\n");
            } else {
              cmdeReview = true;
            }
          } else if (newEcoCd.substring(0, 1).equals(oldEcoCd.substring(0, 1))
              && (newEcoCd.substring(1, 3).equals("11") || newEcoCd.substring(1, 3).equals("13") || newEcoCd.substring(1, 3).equals("49"))) {
            cmdeReview = true;
            engineData.addNegativeCheckStatus("_beluxEconomicCdUpdt", "Economic code is updated from " + oldEcoCd + " to " + newEcoCd);
            details.append("Economic code is updated to " + newEcoCd + "\n");
          } else {
            cmdeReview = true;
            engineData.addNegativeCheckStatus("_beluxEconomicCdUpdt", "Economic code was updated incorrectly or by non registered user.");
            details.append("Economic code was updated incorrectly or by non registered user.\n");
          }
        }
        break;
      case "KVK":
        cmdeReview = true;
        details.append("KVK is updated to " + change.getNewData()).append("\n");
        break;
      case "PPS CEID":
        String newppsceid = change.getNewData();
        String oldppsceid = change.getOldData();
        String Kukla = data.getCustClass();
        // ADD
        if (StringUtils.isBlank(oldppsceid) && !StringUtils.isBlank(newppsceid)) {
          if ("49".equalsIgnoreCase(Kukla) && checkPPSCEID(data.getPpsceid())) {
            details.append("PPS CE ID validated successfully with PartnerWorld Profile Systems.").append("\n");
          } else {
            resultCodes.add("D");
            if (!"49".equalsIgnoreCase(Kukla)) {
              details.append("PS Ceid added for CMR with Kukla other than 49").append("\n");
            } else {
              details.append("PPS ceid on the request is invalid").append("\n");
            }
          }
        }
        // DELETE
        if (!StringUtils.isBlank(oldppsceid) && StringUtils.isBlank(newppsceid)) {
          cmdeReview = true;
          engineData.addNegativeCheckStatus("_beluxPpsCeidUpdt", "Deletion of ppsceid needs cmde review.\n");
          details.append("Deletion of ppsceid needs cmde review.\n");
        }
        // UPDATE
        if (!StringUtils.isBlank(oldppsceid) && !StringUtils.isBlank(newppsceid) && !oldppsceid.equalsIgnoreCase(newppsceid)) {
          cmdeReview = true;
          engineData.addNegativeCheckStatus("_beluxPpsCeidUpdt", "Update of ppsceid needs cmde review.\n");
          details.append("Update of ppsceid needs cmde review.\n");
        }
        break;
      case "Order Block Code":
        String newOrdBlk = change.getNewData();
        String oldOrdBlk = change.getOldData();
        // ADD
        if (StringUtils.isBlank(oldOrdBlk) && !StringUtils.isBlank(newOrdBlk) && "P".equalsIgnoreCase(newOrdBlk)) {
          cmdeReview = true;
          engineData.addNegativeCheckStatus("_beluxOrdBlkUpdt", "Embargo Code P was added.");
          details.append("Embargo Code P was added.\n");
        }
        // UPDATE
        if (!StringUtils.isBlank(oldOrdBlk) && !StringUtils.isBlank(newOrdBlk) && newOrdBlk.equalsIgnoreCase(oldOrdBlk)
            && "P".equalsIgnoreCase(newOrdBlk)) {
          cmdeReview = true;
          engineData.addNegativeCheckStatus("_beluxOrdBlkUpdt", "Embargo Code was updated to P.");
          details.append("Embargo Code P was added.\n");
        }

        // DELETE
        if (!StringUtils.isBlank(oldOrdBlk) && StringUtils.isBlank(newOrdBlk) && "P".equalsIgnoreCase(oldOrdBlk)) {
          cmdeReview = true;
          engineData.addNegativeCheckStatus("_beluxOrdBlkUpdt", "Embargo Code P was deleted.");
          details.append("Embargo Code P was deleted.\n");
        }
        break;
      default:
        ignoredUpdates.add(change.getDataField());
        break;
      }
    }
    if (resultCodes.contains("D")) {
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
    StringBuilder nonRelAddrFdsDetails = new StringBuilder();
    List<Addr> addresses = null;
    int ignoredAddr = 0;
    StringBuilder checkDetails = new StringBuilder();
    Set<String> resultCodes = new HashSet<String>();// R - review
    Addr zs01 = requestData.getAddress("ZS01");
    String soldToCustNm = zs01.getCustNm1() + (!StringUtils.isBlank(zs01.getCustNm2()) ? " " + zs01.getCustNm2() : "");

    for (String addrType : RELEVANT_ADDRESSES) {
      addresses = requestData.getAddresses(addrType);
      if (changes.isAddressChanged(addrType)) {
        for (Addr addr : addresses) {
          if (isRelevantAddressFieldUpdated(changes, addr, nonRelAddrFdsDetails)) {

            if (addrType.equalsIgnoreCase(CmrConstants.RDC_BILL_TO)) {
              String billToCustNm = addr.getCustNm1() + (!StringUtils.isBlank(addr.getCustNm2()) ? " " + addr.getCustNm2() : "");
              if (!soldToCustNm.equals(billToCustNm)) {
                LOG.debug("Address " + addrType + "(" + addr.getId().getAddrSeq() + ") needs to be verified");
                checkDetails.append("Address " + addrType + "(" + addr.getId().getAddrSeq() + ") has different customer name than sold-to.\n");
                resultCodes.add("D");
              }
            }

            if ((addrType.equalsIgnoreCase(CmrConstants.RDC_SOLD_TO) && "Y".equals(addr.getImportInd()))
                || addrType.equalsIgnoreCase(CmrConstants.RDC_BILL_TO)) {
              List<DnBMatchingResponse> matches = getMatches(requestData, engineData, addr, false);
              boolean matchesDnb = false;
              if (matches != null) {
                // check against D&B
                matchesDnb = ifaddressCloselyMatchesDnb(matches, addr, admin, data.getCmrIssuingCntry());
              }
              if (!matchesDnb) {
                LOG.debug("Address " + addrType + "(" + addr.getId().getAddrSeq() + ") does not match D&B");
                resultCodes.add("R");
                checkDetails.append("Address " + addrType + "(" + addr.getId().getAddrSeq() + ") did not match D&B records.\n");
              } else {
                checkDetails.append("Address " + addrType + "(" + addr.getId().getAddrSeq() + ") matches D&B records. Matches:\n");
                for (DnBMatchingResponse dnb : matches) {
                  checkDetails.append(" - DUNS No.:  " + dnb.getDunsNo() + " \n");
                  checkDetails.append(" - Name.:  " + dnb.getDnbName() + " \n");
                  checkDetails.append(" - Address:  " + dnb.getDnbStreetLine1() + " " + dnb.getDnbCity() + " " + dnb.getDnbPostalCode() + " "
                      + dnb.getDnbCountry() + "\n\n");
                }
              }

            }

            if (CmrConstants.RDC_INSTALL_AT.equals(addrType)) {
              String installAtName = getCustomerFullName(addr);
              String soldToName = getCustomerFullName(zs01);
              if (installAtName.equals(soldToName)) {
                if (addressExists(entityManager, addr)) {
                  LOG.debug(" - Duplicates found for " + addrType + "(" + addr.getId().getAddrSeq() + ")");
                  checkDetails.append("Address " + addrType + "(" + addr.getId().getAddrSeq() + ") provided matches an existing address.\n");
                  resultCodes.add("R");
                } else {
                  LOG.debug("Addition/Updation of " + addrType + "(" + addr.getId().getAddrSeq() + ")");
                  checkDetails.append("Address (" + addr.getId().getAddrSeq() + ") is validated.\n");
                }
              } else {
                LOG.debug("Address " + addrType + "(" + addr.getId().getAddrSeq() + ") needs to be verified");
                checkDetails.append("Address " + addrType + "(" + addr.getId().getAddrSeq() + ") has different customer name than sold-to.\n");
                resultCodes.add("D");
              }
            }

            if (addrType.equalsIgnoreCase(CmrConstants.RDC_SHIP_TO) && "N".equals(addr.getImportInd())) {
              LOG.debug("Checking duplicates for " + addrType + "(" + addr.getId().getAddrSeq() + ")");
              boolean duplicate = addressExists(entityManager, addr);
              if (duplicate) {
                LOG.debug(" - Duplicates found for " + addrType + "(" + addr.getId().getAddrSeq() + ")");
                checkDetails.append("Addition of " + addrType + "(" + addr.getId().getAddrSeq() + ") provided matches an existing address.\n");
                resultCodes.add("R");
              }
            }

            if (addrType.equalsIgnoreCase(CmrConstants.RDC_SHIP_TO) && "Y".equals(addr.getImportInd())) {
              ignoredAddr++;
            }
          }
        }
      }
    }
    if (resultCodes.contains("R")) {
      output.setOnError(true);
      engineData.addRejectionComment("_atRejectAddr", "Addition or updation on the address is rejected", "", "");
      validation.setSuccess(false);
      validation.setMessage("Rejected");
    } else if (resultCodes.contains("D")) {
      validation.setSuccess(false);
      validation.setMessage("Not Validated");
      engineData.addNegativeCheckStatus("_atCheckFailed", "Updates to addresses cannot be checked automatically.");
    } else {
      validation.setSuccess(true);
      validation.setMessage("Successful");
    }

    if (ignoredAddr > 0) {
      checkDetails.append("Updates to imported Address Ship-To(ZD01) ignored. ");
    }
    String details = (output.getDetails() != null && output.getDetails().length() > 0) ? output.getDetails() : "";
    details += checkDetails.length() > 0 ? "\n" + checkDetails.toString() : "";
    details += nonRelAddrFdsDetails.length() > 0 ? "Following updates ignored - \n" + nonRelAddrFdsDetails.toString() : "";
    output.setDetails(details);
    output.setProcessOutput(validation);
    return true;

  }

  private boolean isRelevantAddressFieldUpdated(RequestChangeContainer changes, Addr addr, StringBuilder details) {
    List<UpdatedNameAddrModel> addrChanges = changes.getAddressChanges(addr.getId().getAddrType(), addr.getId().getAddrSeq());
    if (addrChanges == null) {
      return false;
    }
    for (UpdatedNameAddrModel change : addrChanges) {
      if (!NON_RELEVANT_ADDRESS_FIELDS.contains(change.getDataField())) {
        return true;
      } else {
        details.append(change.getDataField() + " of address " + addr.getId().getAddrType() + " \n");
      }
    }
    return false;
  }

}
