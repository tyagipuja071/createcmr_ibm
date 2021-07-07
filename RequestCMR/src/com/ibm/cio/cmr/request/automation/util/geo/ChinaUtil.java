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
import com.ibm.cio.cmr.request.automation.util.RequestChangeContainer;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.util.BluePagesHelper;
import com.ibm.cio.cmr.request.util.SystemParameters;
import com.ibm.cio.cmr.request.util.dnb.DnBUtil;
import com.ibm.cmr.services.client.dnb.DnBCompany;

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
  // private static final List<String> RELEVANT_ADDRESSES =
  // Arrays.asList(CmrConstants.RDC_SOLD_TO, CmrConstants.RDC_BILL_TO,
  // CmrConstants.RDC_INSTALL_AT, CmrConstants.RDC_SHIP_TO);
  // private static final List<String> NON_RELEVANT_ADDRESS_FIELDS =
  // Arrays.asList("Attention Person", "Phone #", "Collection Code");

  @Override
  public boolean performScenarioValidation(EntityManager entityManager, RequestData requestData, AutomationEngineData engineData,
      AutomationResult<ValidationOutput> result, StringBuilder details, ValidationOutput output) {

    // ScenarioExceptionsUtil scenarioExceptions = (ScenarioExceptionsUtil)
    // engineData.get("SCENARIO_EXCEPTIONS");
    Data data = requestData.getData();
    String scenario = data.getCustSubGrp();
    Admin admin = requestData.getAdmin();
    if ("U".equals(admin.getReqType())) {

    }
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
      engineData.addPositiveCheckStatus(AutomationEngineData.SKIP_RETRIEVE_VALUES);
      doBusinessPartnerChecks(engineData, data.getPpsceid(), details);
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
      engineData.addPositiveCheckStatus(AutomationEngineData.SKIP_RETRIEVE_VALUES);
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
      engineData.addPositiveCheckStatus(AutomationEngineData.SKIP_RETRIEVE_VALUES);
      break;
    }

    return true;
  }

  @Override
  public AutomationResult<OverrideOutput> doCountryFieldComputations(EntityManager entityManager, AutomationResult<OverrideOutput> results,
      StringBuilder details, OverrideOutput overrides, RequestData requestData, AutomationEngineData engineData) throws Exception {
    Data data = requestData.getData();
    Admin admin = requestData.getAdmin();
    if ("C".equals(admin.getReqType())) {
      String scenario = data.getCustSubGrp();
      LOG.info("Starting Field Computations for Request ID " + data.getId().getReqId());
      if (StringUtils.isNotBlank(scenario) && SCENARIO_LOCAL_PRIV.equals(scenario)) {
        details.append("Skip processing of element.\n");
        results.setResults("Skipped");
        results.setDetails("Skip processing of element.");
        results.setOnError(false);
      } else if (StringUtils.isNotBlank(scenario) && SCENARIO_LOCAL_BUSPR.equals(scenario)) {
        List<String> managerID = SystemParameters.getList("AUTO_CN_MGR_BP_LIST");
        boolean managerCheck = BluePagesHelper.isBluePagesHeirarchyManager(admin.getRequesterId(), managerID);
        if (!managerCheck) {
          details.append("request must be from BPSO team.");
          engineData.addRejectionComment("OTH", "please sure requester must be from BPSO team.", "", "");
          results.setOnError(true);
          results.setResults("Requester check fail");
        } else {
          details.append("Skipping validation for requester must be from BPSO team for requester - " + admin.getRequesterId() + ".\n");
          results.setOnError(false);
          results.setResults("Computed");
        }
      } else {
        String error = performISICCheck(entityManager, requestData);

        if (StringUtils.isNotBlank(error)) {
          details.append(error);
          engineData.addRejectionComment("OTH",
              "If you insist to use different ISIC from D&B,please raise D&B ticket to do investigation and raise jira ticket to CMDE team within 30 days after D&B refresh.",
              "", "");
          results.setOnError(true);
        } else {
          details.append("ISIC is valid" + "\n");
          details.append("Successful Execution.\n");
          results.setOnError(false);
          results.setResults("Computed");
        }

      }

    } else {
      details.append("No specific fields to compute.\n");
      results.setResults("Skipped");
      results.setOnError(false);
    }
    results.setDetails(details.toString());

    return results;
  }

  /**
   * Checks to perform if ISIC updated
   * 
   * @param cedpManager
   * @param entityManager
   * @param requestData
   * @param updatedDataModel
   * @return a string with error message if some issues encountered during
   *         checks, null if validated.
   * @throws Exception
   */
  private String performISICCheck(EntityManager entityManager, RequestData requestData) throws Exception {
    Data data = requestData.getData();
    String isic = data.getIsicCd() == null ? "" : data.getIsicCd();
    String dunsNo = "";
    if (StringUtils.isNotBlank(data.getDunsNo())) {
      dunsNo = data.getDunsNo();

      if (StringUtils.isNotBlank(dunsNo)) {
        DnBCompany dnbData = DnBUtil.getDnBDetails(dunsNo);
        if (dnbData != null && StringUtils.isNotBlank(dnbData.getIbmIsic())) {
          if (!dnbData.getIbmIsic().equals(isic)) {
            isic = dnbData.getIbmIsic();
            return "ISIC validation failed by D&B,import and override the ISIC from D&B directly";
          } else {

            return null;

          }
        }
      } else {
        return "\n- Duns No. is blank";
      }
    } else {
      return "\n- Duns No. is blank";
    }

    return null;
  }

  @Override
  public boolean runUpdateChecksForData(EntityManager entityManager, AutomationEngineData engineData, RequestData requestData,
      RequestChangeContainer changes, AutomationResult<ValidationOutput> output, ValidationOutput validation) throws Exception {
    Admin admin = requestData.getAdmin();
    Data data = requestData.getData();

    StringBuilder details = new StringBuilder();
    String scenario = data.getCustSubGrp();
    if (StringUtils.isNotBlank(scenario) && SCENARIO_LOCAL_BUSPR.equals(scenario) || StringUtils.isNotBlank(data.getPpsceid())) {
      List<String> managerID = SystemParameters.getList("AUTO_CN_MGR_BP_LIST");
      boolean managerCheck = BluePagesHelper.isBluePagesHeirarchyManager(admin.getRequesterId(), managerID);
      if (!managerCheck) {
        details.append("requester must be from BPSO team.");
        engineData.addRejectionComment("OTH", "please sure requester must be from BPSO team.", "", "");
        output.setOnError(true);
        validation.setSuccess(false);
        validation.setMessage("Rejected");
      } else {
        details.append("Skipping validation for request must be from BPSO team for requester - " + admin.getRequesterId() + ".\n");
        validation.setSuccess(true);
        validation.setMessage("Successful");
      }

    } else {
      details.append("Skipping validation for this scenario. scenario = " + scenario + ".\n");
      validation.setSuccess(true);
      validation.setMessage("Successful");
    }

    output.setDetails(details.toString());
    output.setProcessOutput(validation);
    return true;
  }

  // @Override
  // public boolean runUpdateChecksForAddress(EntityManager entityManager,
  // AutomationEngineData engineData, RequestData requestData,
  // RequestChangeContainer changes, AutomationResult<ValidationOutput> output,
  // ValidationOutput validation) throws Exception {
  //
  // Admin admin = requestData.getAdmin();
  // Data data = requestData.getData();
  // StringBuilder nonRelAddrFdsDetails = new StringBuilder();
  // List<Addr> addresses = null;
  // // int ignoredAddr = 0;
  // StringBuilder checkDetails = new StringBuilder();
  // Set<String> resultCodes = new HashSet<String>();// R - review
  // Addr zs01 = requestData.getAddress("ZS01");
  //
  // for (String addrType : RELEVANT_ADDRESSES) {
  // addresses = requestData.getAddresses(addrType);
  // if (changes.isAddressChanged(addrType)) {
  // for (Addr addr : addresses) {
  // if (isRelevantAddressFieldUpdated(changes, addr, nonRelAddrFdsDetails)) {
  //
  // if ((addrType.equalsIgnoreCase(CmrConstants.RDC_SOLD_TO) &&
  // "Y".equals(addr.getImportInd()))) {
  // List<DnBMatchingResponse> matches = getMatches(requestData, engineData,
  // addr, false);
  // boolean matchesDnb = false;
  // if (matches != null) {
  // // check against D&B
  // matchesDnb = ifaddressCloselyMatchesDnb(matches, addr, admin,
  // data.getCmrIssuingCntry());
  // }
  // if (!matchesDnb) {
  // LOG.debug("Address " + addrType + "(" + addr.getId().getAddrSeq() + ") does
  // not match D&B");
  // resultCodes.add("R");
  // checkDetails.append("Address " + addrType + "(" + addr.getId().getAddrSeq()
  // + ") did not match D&B records.\n");
  // } else {
  // checkDetails.append("Address " + addrType + "(" + addr.getId().getAddrSeq()
  // + ") matches D&B records. Matches:\n");
  // for (DnBMatchingResponse dnb : matches) {
  // checkDetails.append(" - DUNS No.: " + dnb.getDunsNo() + " \n");
  // checkDetails.append(" - Name.: " + dnb.getDnbName() + " \n");
  // checkDetails.append(" - Address: " + dnb.getDnbStreetLine1() + " " +
  // dnb.getDnbCity() + " " + dnb.getDnbPostalCode() + " "
  // + dnb.getDnbCountry() + "\n\n");
  // }
  // }
  // }
  //
  // if (!addrType.equalsIgnoreCase(CmrConstants.RDC_SOLD_TO)) {
  // if (!compareCustomerNames(zs01, addr)) {
  // LOG.debug("Address " + addrType + "(" + addr.getId().getAddrSeq() + ")
  // needs to be verified");
  // checkDetails.append("Address " + addrType + "(" + addr.getId().getAddrSeq()
  // + ") has different customer name than sold-to.\n");
  // resultCodes.add("D");
  // } else {
  // List<DnBMatchingResponse> matches = getMatches(requestData, engineData,
  // addr, false);
  // boolean matchesDnb = false;
  // if (matches != null) {
  // // check against D&B
  // matchesDnb = ifaddressCloselyMatchesDnb(matches, addr, admin,
  // data.getCmrIssuingCntry());
  // }
  // if (!matchesDnb) {
  // LOG.debug("Address " + addrType + "(" + addr.getId().getAddrSeq() + ") does
  // not match D&B");
  // resultCodes.add("R");
  // checkDetails.append("Address " + addrType + "(" + addr.getId().getAddrSeq()
  // + ") did not match D&B records.\n");
  // } else {
  // checkDetails.append("Address " + addrType + "(" + addr.getId().getAddrSeq()
  // + ") matches D&B records. Matches:\n");
  // for (DnBMatchingResponse dnb : matches) {
  // checkDetails.append(" - DUNS No.: " + dnb.getDunsNo() + " \n");
  // checkDetails.append(" - Name.: " + dnb.getDnbName() + " \n");
  // checkDetails.append(" - Address: " + dnb.getDnbStreetLine1() + " " +
  // dnb.getDnbCity() + " " + dnb.getDnbPostalCode() + " "
  // + dnb.getDnbCountry() + "\n\n");
  // }
  // }
  //
  // }
  // }
  //
  // // if (CmrConstants.RDC_INSTALL_AT.equals(addrType)) {
  // // String installAtName = getCustomerFullName(addr);
  // // String soldToName = getCustomerFullName(zs01);
  // // if (installAtName.equals(soldToName)) {
  // // if (addressExists(entityManager, addr)) {
  // // LOG.debug(" - Duplicates found for " + addrType + "(" +
  // // addr.getId().getAddrSeq() + ")");
  // // checkDetails.append("Address " + addrType + "(" +
  // // addr.getId().getAddrSeq() + ") provided matches an existing
  // // address.\n");
  // // resultCodes.add("R");
  // // } else {
  // // LOG.debug("Addition/Updation of " + addrType + "(" +
  // // addr.getId().getAddrSeq() + ")");
  // // checkDetails.append("Address (" + addr.getId().getAddrSeq() + ")
  // // is validated.\n");
  // // }
  // // } else {
  // // LOG.debug("Address " + addrType + "(" + addr.getId().getAddrSeq()
  // // + ") needs to be verified");
  // // checkDetails.append("Address " + addrType + "(" +
  // // addr.getId().getAddrSeq() + ") has different customer name than
  // // sold-to.\n");
  // // resultCodes.add("D");
  // // }
  // // }
  // //
  // // if (addrType.equalsIgnoreCase(CmrConstants.RDC_SHIP_TO) &&
  // // "N".equals(addr.getImportInd())) {
  // // LOG.debug("Checking duplicates for " + addrType + "(" +
  // // addr.getId().getAddrSeq() + ")");
  // // boolean duplicate = addressExists(entityManager, addr);
  // // if (duplicate) {
  // // LOG.debug(" - Duplicates found for " + addrType + "(" +
  // // addr.getId().getAddrSeq() + ")");
  // // checkDetails.append("Addition of " + addrType + "(" +
  // // addr.getId().getAddrSeq() + ") provided matches an existing
  // // address.\n");
  // // resultCodes.add("R");
  // // }
  // // }
  // //
  // // if (addrType.equalsIgnoreCase(CmrConstants.RDC_SHIP_TO) &&
  // // "Y".equals(addr.getImportInd())) {
  // // ignoredAddr++;
  // // }
  // }
  // }
  // }
  // }
  // if (resultCodes.contains("R")) {
  // output.setOnError(true);
  // engineData.addRejectionComment("_atRejectAddr", "Addition or updation on
  // the address is rejected", "", "");
  // validation.setSuccess(false);
  // validation.setMessage("Rejected");
  // } else if (resultCodes.contains("D")) {
  // validation.setSuccess(false);
  // validation.setMessage("Not Validated");
  // engineData.addNegativeCheckStatus("_atCheckFailed", "Updates to addresses
  // cannot be checked automatically.");
  // } else {
  // validation.setSuccess(true);
  // validation.setMessage("Successful");
  // }
  //
  // // if (ignoredAddr > 0) {
  // // checkDetails.append("Updates to imported Address Ship-To(ZD01) is
  // // skipped. ");
  // // }
  // String details = (output.getDetails() != null &&
  // output.getDetails().length() > 0) ? output.getDetails() : "";
  // details += checkDetails.length() > 0 ? "\n" + checkDetails.toString() : "";
  // details += nonRelAddrFdsDetails.length() > 0 ? "Following updates ignored -
  // \n" + nonRelAddrFdsDetails.toString() : "";
  // output.setDetails(details);
  // output.setProcessOutput(validation);
  // return true;
  //
  // }
  //
  // private boolean isRelevantAddressFieldUpdated(RequestChangeContainer
  // changes, Addr addr, StringBuilder details) {
  // List<UpdatedNameAddrModel> addrChanges =
  // changes.getAddressChanges(addr.getId().getAddrType(),
  // addr.getId().getAddrSeq());
  // if (addrChanges == null) {
  // return false;
  // }
  // for (UpdatedNameAddrModel change : addrChanges) {
  // if (!NON_RELEVANT_ADDRESS_FIELDS.contains(change.getDataField())) {
  // return true;
  // } else {
  // details.append(change.getDataField() + " of address " +
  // addr.getId().getAddrType() + " \n");
  // }
  // }
  // return false;
  // }

  @Override
  public List<String> getSkipChecksRequestTypesforCMDE() {
    return Arrays.asList("C", "U", "M", "D", "R");
  }
}
