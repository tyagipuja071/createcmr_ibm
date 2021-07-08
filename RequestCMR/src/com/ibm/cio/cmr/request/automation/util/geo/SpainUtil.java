/**
 * 
 */
package com.ibm.cio.cmr.request.automation.util.geo;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;

import org.apache.commons.digester.Digester;
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
import com.ibm.cio.cmr.request.util.BluePagesHelper;
import com.ibm.cio.cmr.request.util.ConfigUtil;
import com.ibm.cio.cmr.request.util.RequestUtils;
import com.ibm.cio.cmr.request.util.SystemLocation;
import com.ibm.cio.cmr.request.util.SystemParameters;
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
  public static final String SCENARIO_CROSSBORDER_IGS = "XIGS";
  public static final String SCENARIO_GOVERNMENT = "GOVRN";
  public static final String SCENARIO_GOVERNMENT_IGS = "GOVIG";

  private static SpainISICPostalMapping esIsicPostalMapping = new SpainISICPostalMapping();
  private static List<ESPostalMapping> postalMappings = new ArrayList<ESPostalMapping>();
  private static final String SALES_REP = "salesRep";
  private static final String ENTP = "enterprise";
  private static final String MATCHING = "matching";

  private static final List<String> RELEVANT_ADDRESSES = Arrays.asList(CmrConstants.RDC_SOLD_TO, CmrConstants.RDC_BILL_TO,
      CmrConstants.RDC_INSTALL_AT, CmrConstants.RDC_SHIP_TO, CmrConstants.RDC_SECONDARY_SOLD_TO);
  private static final List<String> NON_RELEVANT_ADDRESS_FIELDS = Arrays.asList("Att. Person", "Phone #");
  private static final List<String> SCENARIOS_TO_SKIP_COVERAGE = Arrays.asList(SCENARIO_INTERNAL, SCENARIO_INTERNAL_SO, SCENARIO_BUSINESS_PARTNER,
      SCENARIO_CROSSBORDER_BP);

  @SuppressWarnings("unchecked")
  public SpainUtil() {
    if (SpainUtil.esIsicPostalMapping == null || SpainUtil.postalMappings.isEmpty()) {
      Digester digester = new Digester();
      Digester digester_ = new Digester();

      digester.setValidating(false);
      digester_.setValidating(false);

      digester.addObjectCreate("mappings", ArrayList.class);
      digester.addObjectCreate("mappings/postalMapping", ESPostalMapping.class);

      digester_.addObjectCreate("mappping-subInd", SpainISICPostalMapping.class);

      digester.addBeanPropertySetter("mappings/postalMapping/isuCTC", "isuCTC");
      digester.addBeanPropertySetter("mappings/postalMapping/postalCdStarts", "postalCdStarts");
      digester.addBeanPropertySetter("mappings/postalMapping/enterprise", "enterprise");
      digester.addBeanPropertySetter("mappings/postalMapping/salesRep", "salesRep");
      digester.addBeanPropertySetter("mappings/postalMapping/scenario", "scenario");
      digester.addBeanPropertySetter("mappings/postalMapping/isicBelongs", "isicBelongs");

      digester.addSetNext("mappings/postalMapping", "add");

      digester_.addBeanPropertySetter("mappping-subInd/subIndCds", "subIndCds");

      try {
        InputStream is = ConfigUtil.getResourceStream("spain-sr-entp-mapping.xml");
        SpainUtil.postalMappings = (ArrayList<ESPostalMapping>) digester.parse(is);

        InputStream is_ = ConfigUtil.getResourceStream("spain-subInd-mapping.xml");
        SpainUtil.esIsicPostalMapping = (SpainISICPostalMapping) digester_.parse(is_);

      } catch (Exception e) {
        LOG.error("Error occured while digesting xml.", e);
      }
    }
  }

  @Override
  public boolean performScenarioValidation(EntityManager entityManager, RequestData requestData, AutomationEngineData engineData,
      AutomationResult<ValidationOutput> result, StringBuilder details, ValidationOutput output) {
    Data data = requestData.getData();
    String scenario = data.getCustSubGrp();
    Addr soldTo = requestData.getAddress("ZS01");
    String customerName = getCustomerFullName(soldTo);
    Addr installAt = requestData.getAddress("ZI01");
    String customerNameZI01 = getCustomerFullName(installAt);
    if (StringUtils.isBlank(scenario)) {
      details.append("Scenario not correctly specified on the request");
      engineData.addNegativeCheckStatus("_atNoScenario", "Scenario not correctly specified on the request");
      return true;
    }
    LOG.info("Starting scenario validations for Request ID " + data.getId().getReqId());

    LOG.debug("Scenario to check: " + scenario);

    if ("C".equals(requestData.getAdmin().getReqType())) {
      if ((SCENARIO_COMMERCIAL.equals(scenario) || SCENARIO_IGS_GSE.equals(scenario) || SCENARIO_CROSSBORDER.equals(scenario)
          || SCENARIO_CROSSBORDER_IGS.equals(scenario) || SCENARIO_GOVERNMENT.equals(scenario) || SCENARIO_GOVERNMENT_IGS.equals(scenario))
          && !compareCustomerNames(soldTo, installAt)) {
        engineData.addRejectionComment("OTH", "Customer names on billing and installing address are not identical, 3rd Party should be selected.", "",
            "");
        details.append("Customer names on billing and installing address are not identical, 3rd Party should be selected.");
        return false;
      } else if ((SCENARIO_THIRD_PARTY.equals(scenario) || SCENARIO_THIRD_PARTY_IG.equals(scenario)) && addressEquals(soldTo, installAt)) {
        engineData.addRejectionComment("OTH", "Billing and installing address should be different.", "", "");
        details.append("For 3rd Party Billing and installing address should be different");
        return false;
      }
    }

    switch (scenario) {
    case SCENARIO_PRIVATE_CUSTOMER:
      engineData.addPositiveCheckStatus(AutomationEngineData.SKIP_GBG);
      return doPrivatePersonChecks(engineData, SystemLocation.SPAIN, soldTo.getLandCntry(), customerName, details, false, requestData);
    case SCENARIO_BUSINESS_PARTNER:
    case SCENARIO_CROSSBORDER_BP:
      engineData.addPositiveCheckStatus(AutomationEngineData.SKIP_GBG);
      engineData.hasPositiveCheckStatus(AutomationEngineData.SKIP_COVERAGE);
      return doBusinessPartnerChecks(engineData, data.getPpsceid(), details);
    case SCENARIO_INTERNAL:
    case SCENARIO_INTERNAL_SO:
      engineData.addPositiveCheckStatus(AutomationEngineData.SKIP_GBG);
      engineData.addPositiveCheckStatus(AutomationEngineData.SKIP_COVERAGE);
      break;
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
      MatchingResponse<DnBMatchingResponse> response = DnBUtil.getMatches(requestData, engineData, "ZI01");
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
    } else if (SCENARIO_INTERNAL.equals(scenario) || SCENARIO_INTERNAL_SO.equals(scenario)) {
      details.append("Ensuring ISIC is set to '0000' for " + (SCENARIO_INTERNAL.equals(scenario) ? "Internal" : "Internal SO") + "scenario.");
      overrides.addOverride(AutomationElementRegistry.GBL_FIELD_COMPUTE, "DATA", "ISIC_CD", data.getIsicCd(), "0000");
      String subInd = RequestUtils.getSubIndustryCd(entityManager, "0000", data.getCmrIssuingCntry());
      if (subInd != null) {
        overrides.addOverride(AutomationElementRegistry.GBL_FIELD_COMPUTE, "DATA", "SUB_INDUSTRY_CD", data.getSubIndustryCd(), subInd);
      }
      results.setResults("Calculated.");
      results.setProcessOutput(overrides);
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
          if (Arrays.asList(sboValuesToCheck).contains(sortl)) {
            filteredMatches.add(match);
          }
        }

      }
      // set filtered matches in response
      response.setMatches(filteredMatches);
    }

  }

  @Override
  public boolean runUpdateChecksForData(EntityManager entityManager, AutomationEngineData engineData, RequestData requestData,
      RequestChangeContainer changes, AutomationResult<ValidationOutput> output, ValidationOutput validation) throws Exception {
    Admin admin = requestData.getAdmin();
    Data data = requestData.getData();
    int coverageFieldUpdtd = 0;
    if (handlePrivatePersonRecord(entityManager, admin, output, validation, engineData)) {
      return true;
    }
    StringBuilder details = new StringBuilder();
    boolean cmdeReview = false;
    Set<String> resultCodes = new HashSet<String>();// D for Reject
    List<String> ignoredUpdates = new ArrayList<String>();
    for (UpdatedDataModel change : changes.getDataUpdates()) {
      boolean requesterFromTeam = false;
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
          String oldData = change.getOldData().substring(3, 11);
          String newData = change.getNewData().substring(3, 11);
          if (!(oldData.equals(newData))) {
            resultCodes.add("D");// Reject
            details.append("VAT # on the request has characters updated other than the first character. Create New CMR. \n");
          } else {
            details.append("VAT # on the request differs only in the first Character\n");
          }
        }
        break;
      case "SBO":
        if (!StringUtils.isBlank(change.getOldData()) && !StringUtils.isBlank(change.getNewData())
            && !(change.getOldData().equals(change.getNewData()))) {
          requesterFromTeam = BluePagesHelper.isBluePagesHeirarchyManager(admin.getRequesterId(), SystemParameters.getList("ES.SKIP_UPDATE_CHECK"));
          if ("9".equals(change.getNewData().substring(1, 2)) && !requesterFromTeam) {
            resultCodes.add("D");// Reject
            details.append("Requester is not allowed to submit updates to " + change.getDataField() + " field. \n");
          }
          if (!"9".equals(change.getNewData().substring(1, 2))) {
            // cmdeReview = true;
            coverageFieldUpdtd++;
          }
        }
        break;
      case "ISIC":
      case "Currency Code":
        cmdeReview = true;
        break;
      case "Mode Of Payment":
      case "Mailing Condition":
        requesterFromTeam = BluePagesHelper.isBluePagesHeirarchyManager(admin.getRequesterId(), SystemParameters.getList("ES.SKIP_UPDATE_CHECK"));
        if (!requesterFromTeam) {
          resultCodes.add("D");// Reject
          details.append("Requester is not allowed to submit updates to " + change.getDataField() + " field. \n");
        }
        break;
      case "Tax Code":
        // noop, for switch handling only
        break;
      case "ISU Code":
      case "Client Tier":
      case "Enterprise Number":
      case "INAC/NAC Code":
      case "Sales Rep":
        coverageFieldUpdtd++;
        break;
      case "Order Block Code":
        if ("E".equals(change.getOldData()) || "E".equals(change.getNewData())) {
          // noop, for switch handling only
        }
        break;
      default:
        ignoredUpdates.add(change.getDataField());
        break;
      }
    }
    if (coverageFieldUpdtd > 0) {
      List<String> managerID = SystemParameters.getList("ES_UKI_MGR_COV_UPDT");
      boolean managerCheck = BluePagesHelper.isBluePagesHeirarchyManager(admin.getRequesterId(), managerID);
      if (!managerCheck) {
        if (changes.isDataChanged("INAC/NAC Code")) {
          cmdeReview = true;
          admin.setScenarioVerifiedIndc("Y");
        } else {
          details.append("Updates to coverage fields cannot be validated. An approval will be required.\n");
          admin.setScenarioVerifiedIndc("N");
        }
      } else {
        details.append("Skipping validation for coverage fields update for requester - " + admin.getRequesterId() + ".\n");
        admin.setScenarioVerifiedIndc("Y");
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
              if (isRelevantAddressFieldUpdated(changes, addr)) {
                Addr soldTo = requestData.getAddress(CmrConstants.RDC_SOLD_TO);
                List<DnBMatchingResponse> matches = getMatches(requestData, engineData, soldTo, false);
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
              if (CmrConstants.RDC_SHIP_TO.equals(addrType) || CmrConstants.RDC_SECONDARY_SOLD_TO.equals(addrType)) {
                // proceed
                LOG.debug("Update to Shipping and EPL " + addrType + "(" + addr.getId().getAddrSeq() + ")");
                checkDetails.append("Updates to Shipping and EPL (" + addr.getId().getAddrSeq() + ") skipped in the checks.\n");
              } else {
                checkDetails.append("Updates to Updated Addresses for " + addrType + "(" + addr.getId().getAddrSeq() + ") needs to be verified")
                    .append("\n");
                resultCodes.add("R");
              }
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
    Addr addr = requestData.getAddress("ZS01");

    if ((!isCoverageCalculated)) {
      details.setLength(0);
      overrides.clearOverrides();

      HashMap<String, String> response = getEntpSalRepFromPostalCodeMapping(data.getSubIndustryCd(), addr, data.getIsuCd(), data.getClientTier(),
          data.getCustSubGrp());

      if (response.get(MATCHING).equalsIgnoreCase("Match Found.")) {
        LOG.debug("Calculated Enterprise: " + response.get(ENTP));
        LOG.debug("Calculated Sales Rep: " + response.get(SALES_REP));
        details.append("Coverage calculated successfully using 34Q logic mapping.").append("\n");
        details.append("Sales Rep : " + response.get(SALES_REP)).append("\n");
        details.append("Enterprise : " + response.get(ENTP)).append("\n");
        overrides.addOverride(AutomationElementRegistry.GBL_CALC_COV, "DATA", "ENTERPRISE", data.getEnterprise(), response.get(ENTP));
        overrides.addOverride(AutomationElementRegistry.GBL_CALC_COV, "DATA", "REP_TEAM_MEMBER_NO", data.getRepTeamMemberNo(),
            response.get(SALES_REP));
        results.setResults("Calculated");
        results.setDetails(details.toString());
      } else if (StringUtils.isNotBlank(data.getRepTeamMemberNo()) && StringUtils.isNotBlank(data.getSalesBusOffCd())
          && StringUtils.isNotBlank(data.getEnterprise())) {
        details.append("Coverage could not be calculated using 34Q logic. Using values from request").append("\n");
        details.append("Sales Rep : " + data.getRepTeamMemberNo()).append("\n");
        details.append("Enterprise : " + data.getEnterprise()).append("\n");
        details.append("SBO : " + data.getSalesBusOffCd()).append("\n");
        results.setResults("Calculated");
        results.setDetails(details.toString());
      } else {
        String msg = "Coverage cannot be calculated. No valid 34Q mapping found from request data.";
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

  private HashMap<String, String> getEntpSalRepFromPostalCodeMapping(String subIndCd, Addr addr, String isuCd, String clientTier, String scenario) {
    HashMap<String, String> response = new HashMap<String, String>();
    response.put(ENTP, "");
    response.put(SALES_REP, "");
    response.put(MATCHING, "");
    if (addr != null) {
      List<String> isicCds = new ArrayList<String>();
      List<String> postalCodes = new ArrayList<String>();
      List<String> scenariosList = new ArrayList<String>();
      String postCdtStrt = addr.getPostCd().substring(0, 2);

      if (esIsicPostalMapping != null) {
        String[] postalCodeRanges = null;
        for (ESPostalMapping postalMapping : postalMappings) {
          if (esIsicPostalMapping.getSubIndCds() != null && !esIsicPostalMapping.getSubIndCds().isEmpty()) {
            isicCds = Arrays.asList(esIsicPostalMapping.getSubIndCds().replaceAll("\n", "").replaceAll(" ", "").split(","));
          }
          if (!StringUtils.isEmpty(postalMapping.getPostalCdStarts())) {
            postalCodeRanges = postalMapping.getPostalCdStarts().replaceAll("\n", "").replaceAll(" ", "").split(",");
            postalCodes = Arrays.asList(postalCodeRanges);
          }

          String[] scenarios = postalMapping.getScenarios().replaceAll("\n", "").replaceAll(" ", "").split(",");
          scenariosList = Arrays.asList(scenarios);

          if (isuCd.concat(clientTier).equalsIgnoreCase(postalMapping.getIsuCTC()) && scenariosList.contains(scenario)
              && "None".equalsIgnoreCase(postalMapping.getIsicBelongs())
              || (!postalCodes.isEmpty() && postalCodes.contains(postCdtStrt))
                  && (("Yes".equalsIgnoreCase(postalMapping.getIsicBelongs()) && isicCds.contains(subIndCd))
                      || ("No".equalsIgnoreCase(postalMapping.getIsicBelongs()) && !isicCds.contains(subIndCd)))) {
            response.put(ENTP, postalMapping.getEnterprise());
            response.put(SALES_REP, postalMapping.getSaleRep());
            response.put(MATCHING, "Match Found.");
            break;
          }
        }
      } else {
        response.put(MATCHING, "No Match Found");
      }
    }
    return response;
  }

  @Override
  public List<String> getSkipChecksRequestTypesforCMDE() {
    return Arrays.asList("C", "U", "M", "D", "R");
  }

}
