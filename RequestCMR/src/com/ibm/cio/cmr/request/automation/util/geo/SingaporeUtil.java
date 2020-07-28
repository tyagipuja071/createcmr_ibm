package com.ibm.cio.cmr.request.automation.util.geo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
import com.ibm.cio.cmr.request.automation.util.ScenarioExceptionsUtil;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.model.window.UpdatedDataModel;
import com.ibm.cmr.services.client.matching.dnb.DnBMatchingResponse;
import com.ibm.cmr.services.client.matching.gbg.GBGResponse;

public class SingaporeUtil extends AutomationUtil {

  private static final Logger LOG = Logger.getLogger(SingaporeUtil.class);

  private static final List<String> ALLOW_DEFAULT_SCENARIOS = Arrays.asList("PRIV", "XPRIV", "BLUMX", "MKTPC", "XBLUM", "XMKTP");

  private static final List<String> RELEVANT_ADDRESSES = Arrays.asList(CmrConstants.RDC_SOLD_TO, CmrConstants.RDC_BILL_TO,
      CmrConstants.RDC_INSTALL_AT, CmrConstants.RDC_SHIPPING);

  @Override
  public AutomationResult<OverrideOutput> doCountryFieldComputations(EntityManager entityManager, AutomationResult<OverrideOutput> results,
      StringBuilder details, OverrideOutput overrides, RequestData requestData, AutomationEngineData engineData) throws Exception {
    long reqId = requestData.getAdmin().getId().getReqId();
    LOG.debug("Executing doCountryFieldComputations() for reqId=" + reqId);
    Data data = requestData.getData();
    boolean ifDefaultCluster = false;
    String cluster = data.getApCustClusterId();
    String govType = data.getGovType();
    String duns = data.getDunsNo();
    StringBuilder eleResults = new StringBuilder();
    String defaultCluster = getDefaultCluster(entityManager, requestData, engineData);
    String[] isicScenarioList = { "NRML", "ASLOM", "BUSPR", "SPOFF", "CROSS", "AQSTN", "XAQST", "SOFT" };
    boolean isIsicInvalid = false;

    if (StringUtils.isNotBlank(defaultCluster)) {
      ifDefaultCluster = cluster.equals(defaultCluster);
    }

    if (ifDefaultCluster) {
      if ("9500".equals(data.getIsicCd()) || ALLOW_DEFAULT_SCENARIOS.contains(data.getCustSubGrp())) {
        LOG.debug("Default Cluster used but allowed: ISIC=" + data.getIsicCd() + " Scenario=" + data.getCustSubGrp());
        results.setOnError(false);
        // eleResults.append("Default Cluster used.\n");
        details.append("Default Cluster used but allowed for the request (Private Person/BlueMix/Marketplace).\n");
      } else {
        details.append("Cluster should not be the default for the scenario.\n");
        engineData.addRejectionComment("OTH", "Cluster should not be the default for the scenario.", "", "");
        // eleResults.append("Default cluster found.\n");
        results.setOnError(true);
      }
    } else {
      details.append("Default cluster not used.\n");
    }

    String validRes = checkIfClusterSalesmanIsValid(entityManager, requestData);
    LOG.debug("IfClusterSalesmanIsValid" + validRes);

    if ("9500".equals(data.getIsicCd()) || ALLOW_DEFAULT_SCENARIOS.contains(data.getCustSubGrp())) {
      LOG.debug("Salesman check skipped for private and allowed scenarios.");
      details.append("\nSalesman check skipped for this scenario (Private/Marketplace/Bluemix).\n");
    } else {

      if (validRes != null && validRes.equals("INVALID")) {
        details.append("The combination of Salesman No. and Cluster is not valid.\n");
        engineData.addRejectionComment("OTH", "The combination of Salesman No. and Cluster is not valid.", "", "");
        // eleResults.append("Invalid Salesman No.\n");
        results.setOnError(true);
      } else if (validRes != null && validRes.equals("VALID")) {
        details.append("The combination of Salesman No. and Cluster is valid.\n");
      } else if (validRes != null && validRes.equals("NO_RESULTS")) {
        details.append("The combination of Salesman No. and Cluster doesn't exist for country.\n");
      }

    }

    if (govType != null && govType.equals("Y")) {
      // eleResults.append("Government Organization" + "\n");
      details.append("Processor review is needed as customer is a Government Organization" + "\n");
      engineData.addNegativeCheckStatus("ISGOV", "Customer is a Government Organization");
    } else {
      // eleResults.append("Non Government Customer" + "\n");
      details.append("Customer is a non government organization" + "\n");
    }

    // CMR-2034 fix
    if ("34".equals(data.getIsuCd()) || "04".equals(data.getIsuCd())) {
      GBGResponse computedGbg = (GBGResponse) engineData.get(AutomationEngineData.GBG_MATCH);
      if (computedGbg == null) {
        engineData.setScenarioVerifiedIndc("N");
      }
    }

    isIsicInvalid = isISICValidForScenario(requestData, Arrays.asList(isicScenarioList));

    if (isIsicInvalid) {
      details.append("Invalid ISIC code, please choose another one based on industry.\n");
      // eleResults.append("Invalid ISIC code.\n");
      engineData.addRejectionComment("OTH", "Invalid ISIC code, please choose another one based on industry.", "", "");
      results.setOnError(true);
    } else {
      details.append("ISIC is valid" + "\n");
    }

    if (!results.isOnError()) {
      // eleResults.append("Successful Exceution");
      details.append("Field Computation completed successfully." + "\n");
    } else {
      eleResults.append("Error On Field Calculation.");
    }

    results.setDetails(details.toString());
    results.setResults(eleResults.toString());
    results.setProcessOutput(overrides);
    LOG.debug(details.toString());
    return results;
  }

  @Override
  public boolean performScenarioValidation(EntityManager entityManager, RequestData requestData, AutomationEngineData engineData,
      AutomationResult<ValidationOutput> result, StringBuilder details, ValidationOutput output) {
    /*
     * String[] scnarioList = { "XBLUM", "BLUMX", "MKTPC", "XMKTP", "DUMMY",
     * "XDUMM", "INTER", "XINT", "AQSTN", "XAQST", "SOFT" };
     * skipCompanyCheckForScenario(requestData, engineData,
     * Arrays.asList(scnarioList), false);
     */

    String[] scnarioList = { "ASLOM", "NRML" };

    allowDuplicatesForScenario(engineData, requestData, Arrays.asList(scnarioList));

    processSkipCompanyChecks(engineData, requestData, details);
    return true;
  }

  /**
   * CHecks if the record is a private customer / bluemix / marketplace
   * 
   * @param engineData
   * @param requestData
   * @param details
   * @return
   */
  private void processSkipCompanyChecks(AutomationEngineData engineData, RequestData requestData, StringBuilder details) {
    Data data = requestData.getData();

    boolean skipCompanyChecks = "9500".equals(data.getIsicCd()) || (data.getCustSubGrp() != null && data.getCustSubGrp().contains("PRIV"));
    if (skipCompanyChecks) {
      details.append("Private Person request - company checks will be skipped.\n");
      ScenarioExceptionsUtil exc = (ScenarioExceptionsUtil) engineData.get("SCENARIO_EXCEPTIONS");
      if (exc != null) {
        exc.setSkipCompanyVerification(true);
        engineData.put("SCENARIO_EXCEPTIONS", exc);
      }
    }
  }

  @Override
  public boolean runUpdateChecksForData(EntityManager entityManager, AutomationEngineData engineData, RequestData requestData,
      RequestChangeContainer changes, AutomationResult<ValidationOutput> output, ValidationOutput validation) throws Exception {
    Admin admin = requestData.getAdmin();
    if (handlePrivatePersonRecord(entityManager, admin, output, validation, engineData)) {
      return true;
    }
    StringBuilder details = new StringBuilder();
    boolean cmdeReview = false;
    List<String> ignoredUpdates = new ArrayList<String>();
    for (UpdatedDataModel change : changes.getDataUpdates()) {
      switch (change.getDataField()) {
      case "ISIC":
      case "Market Responsibility Code (MRC)":
      case "Cluster":
      case "GB segment":
      case "ISU Code":
      case "INAC/NAC Code":
      case "Sales Rep No":
      case "AR Code":
        cmdeReview = true;
        break;
      default:
        ignoredUpdates.add(change.getDataField());
        break;
      }
    }
    if (cmdeReview) {
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
    List<Addr> addresses = null;
    Admin admin = requestData.getAdmin();
    Data data = requestData.getData();
    StringBuilder checkDetails = new StringBuilder();
    boolean cmdeReview = false;
    for (String addrType : RELEVANT_ADDRESSES) {
      if (changes.isAddressChanged(addrType)) {
        if (CmrConstants.RDC_SOLD_TO.equals(addrType)) {
          addresses = Collections.singletonList(requestData.getAddress(CmrConstants.RDC_SOLD_TO));
        } else {
          addresses = requestData.getAddresses(addrType);
        }
        for (Addr addr : addresses) {
          if (CmrConstants.RDC_SOLD_TO.equals(addrType) && ("Y".equals(addr.getChangedIndc()) || "N".equals(addr.getImportInd()))) {
            if (null == changes.getAddressChange(addrType, "Customer Name") && null == changes.getAddressChange(addrType, "Customer Name Con't")) {
              Addr soldTo = requestData.getAddress(CmrConstants.RDC_SOLD_TO);
              List<DnBMatchingResponse> matchesSoldTo = getMatches(requestData, engineData, soldTo, false);
              boolean matchesDnbSoldTo = false;
              if (matchesSoldTo != null) {
                // check against D&B
                matchesDnbSoldTo = ifaddressCloselyMatchesDnb(matchesSoldTo, addr, admin, data.getCmrIssuingCntry());
              }
              if (!matchesDnbSoldTo) {
                // CMDE Review
                checkDetails.append("Updates to Sold To " + addrType + "(" + addr.getId().getAddrSeq() + ") did not match D&B records.").append("\n");
                cmdeReview = true;
                break;
              } else {
                // proceed
                checkDetails.append("Updates to Sold To " + addrType + "(" + addr.getId().getAddrSeq() + ") matches D&B records.").append("\n");
                for (DnBMatchingResponse dnb : matchesSoldTo) {
                  checkDetails.append(" - DUNS No.:  " + dnb.getDunsNo() + " \n");
                  checkDetails.append(" - Name.:  " + dnb.getDnbName() + " \n");
                  checkDetails.append(" - Address:  " + dnb.getDnbStreetLine1() + " " + dnb.getDnbCity() + " " + dnb.getDnbPostalCode() + " "
                      + dnb.getDnbCountry() + "\n\n");
                }
              }
            } else {
              // CMDE Review
              checkDetails.append("Customer Name Updates to Sold To " + addrType + "(" + addr.getId().getAddrSeq() + ") needs to be verified")
                  .append("\n");
              cmdeReview = true;
              break;
            }
          }

          if (!CmrConstants.RDC_SOLD_TO.equals(addrType) && ("Y".equals(addr.getChangedIndc()) || "N".equals(addr.getImportInd()))) {
            if (null == changes.getAddressChange(addrType, "Customer Name") && null == changes.getAddressChange(addrType, "Customer Name Con't")) {
              if (addressEquals(requestData.getAddress("ZS01"), requestData.getAddress(addrType))) {
                // proceed
                Addr address = requestData.getAddress(CmrConstants.RDC_SOLD_TO);
                List<DnBMatchingResponse> matches = getMatches(requestData, engineData, address, false);
                boolean matchesDnb = false;
                if (matches != null) {
                  // check against D&B
                  matchesDnb = ifaddressCloselyMatchesDnb(matches, addr, admin, data.getCmrIssuingCntry());
                }
                if (!matchesDnb) {
                  // CMDE Review
                  checkDetails.append("Updates to Address " + addrType + "(" + addr.getId().getAddrSeq() + ") did not match D&B records.")
                      .append("\n");
                  cmdeReview = true;
                  break;
                } else {
                  // proceed
                  checkDetails.append("Updates to Address " + addrType + "(" + addr.getId().getAddrSeq() + ") matches D&B records.").append("\n");
                  for (DnBMatchingResponse dnb : matches) {
                    checkDetails.append(" - DUNS No.:  " + dnb.getDunsNo() + " \n");
                    checkDetails.append(" - Name.:  " + dnb.getDnbName() + " \n");
                    checkDetails.append(" - Address:  " + dnb.getDnbStreetLine1() + " " + dnb.getDnbCity() + " " + dnb.getDnbPostalCode() + " "
                        + dnb.getDnbCountry() + "\n\n");
                  }
                }
              } else {
                // CMDE Review
                checkDetails.append("Updates Addresses for " + addrType + "(" + addr.getId().getAddrSeq() + ") does not match Sold To Address. ")
                    .append("\n");
                cmdeReview = true;
                break;
              }
            } else {
              // CMDE Review
              checkDetails.append("Customer name Updates Addresses for " + addrType + "(" + addr.getId().getAddrSeq() + ") needs to be verified. ")
                  .append("\n");
              cmdeReview = true;
              break;
            }
          }
        }
      }
    }
    if (cmdeReview) {
      engineData.addNegativeCheckStatus("_esCheckFailed", "Updated elements cannot be checked automatically.");
      validation.setSuccess(false);
      validation.setMessage("Not Validated");
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
}
