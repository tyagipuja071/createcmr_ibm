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
import com.ibm.cio.cmr.request.automation.util.SpainFieldsContainer;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.model.window.UpdatedDataModel;
import com.ibm.cio.cmr.request.model.window.UpdatedNameAddrModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.ui.PageManager;
import com.ibm.cio.cmr.request.util.BluePagesHelper;
import com.ibm.cio.cmr.request.util.ConfigUtil;
import com.ibm.cio.cmr.request.util.Person;
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
  public static final String SCENARIO_IBM_EMPLOYEE = "IBMEM";

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

  private static final String QUERY_BG_ES = "AUTO.COV.GET_COV_FROM_BG_ES";

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
    String custGrp = data.getCustGrp();
    // CREATCMR-6244 LandCntry UK(GB)
    if (soldTo != null) {
      String landCntry = soldTo.getLandCntry();
      if (data.getVat() != null && !data.getVat().isEmpty() && landCntry.equals("GB") && !data.getCmrIssuingCntry().equals("866") && custGrp != null
          && StringUtils.isNotEmpty(custGrp) && ("CROSS".equals(custGrp))) {
        engineData.addNegativeCheckStatus("_vatUK", " request need to be send to CMDE queue for further review. ");
        details.append("Landed Country UK. The request need to be send to CMDE queue for further review.\n");
      }
    }
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
    case SCENARIO_IBM_EMPLOYEE:
      Person person = null;
      if (StringUtils.isNotBlank(soldTo.getCustNm1())) {
        try {
          String mainCustName = soldTo.getCustNm1() + (StringUtils.isNotBlank(soldTo.getCustNm2()) ? " " + soldTo.getCustNm2() : "");
          person = BluePagesHelper.getPersonByName(mainCustName, data.getCmrIssuingCntry());
          if (person == null) {
            engineData.addRejectionComment("OTH", "Employee details not found in IBM BluePages.", "", "");
            details.append("Employee details not found in IBM BluePages.").append("\n");
            return false;
          } else {
            details.append("Employee details validated with IBM BluePages for " + person.getName() + "(" + person.getEmail() + ").").append("\n");
          }
        } catch (Exception e) {
          LOG.error("Not able to check name against bluepages", e);
          engineData.addNegativeCheckStatus("BLUEPAGES_NOT_VALIDATED", "Not able to check name against bluepages for scenario IBM Employee.");
          return false;
        }
      } else {
        LOG.warn("Not able to check name against bluepages, Customer Name 1 not found on the main address");
        engineData.addNegativeCheckStatus("BLUEPAGES_NOT_VALIDATED", "Customer Name 1 not found on the main address");
        return false;
      }
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
          // actions to be performed only when matches with high
          // confidence are
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

    // for P2L Conversions - checking of mandatory fields
    if ("Y".equalsIgnoreCase(admin.getProspLegalInd()) && StringUtils.isNotBlank(admin.getSourceSystId())) {
      if ("COMME".equalsIgnoreCase(data.getCustSubGrp())) {
        Addr soldtoAddr = requestData.getAddress(CmrConstants.RDC_SOLD_TO);
        if (StringUtils.isBlank(soldtoAddr.getStateProv())) {
          details.append("\nState Prov is a mandatory field. Processor Review will be required.");
          engineData.addNegativeCheckStatus("_stateMissing", "State Prov is a mandatory field.");
        }
        if (StringUtils.isBlank(data.getEnterprise())) {
          details.append("\nEnterprise is a mandatory field. Processor Review will be required.");
          engineData.addNegativeCheckStatus("_enterpriseMissing", "Enterprise is a mandatory field.");
        }
      }
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
    String[] scenariosToBeChecked = { "THDIG", "GOVIG", "XIGS", "IGSGS", "PRICU", "IBMEM" };
    String scenario = requestData.getData().getCustSubGrp();
    String[] kuklaPriv = { "60" };
    String[] kuklaIBMEM = { "71" };
    String[] sboValuesToCheck = { "109", "209", "309" };
    if (Arrays.asList(scenariosToBeChecked).contains(scenario)) {
      List<DuplicateCMRCheckResponse> matches = response.getMatches();
      List<DuplicateCMRCheckResponse> filteredMatches = new ArrayList<DuplicateCMRCheckResponse>();
      for (DuplicateCMRCheckResponse match : matches) {
        if (match.getCmrNo() != null && match.getCmrNo().startsWith("P") && "75".equals(match.getOrderBlk())) {
          filteredMatches.add(match);
        }
        if (StringUtils.isNotBlank(match.getSortl()) && !("PRICU".equals(scenario)) && !("IBMEM".equals(scenario))) {
          String sortl = match.getSortl().length() > 3 ? match.getSortl().substring(0, 3) : match.getSortl();
          if (Arrays.asList(sboValuesToCheck).contains(sortl)) {
            filteredMatches.add(match);
          }
        }
        if (StringUtils.isNotBlank(match.getCustClass())) {
          String kukla = match.getCustClass() != null ? match.getCustClass() : "";
          if (Arrays.asList(kuklaPriv).contains(kukla) && ("PRICU".equals(scenario))) {
            filteredMatches.add(match);
          } else if (Arrays.asList(kuklaIBMEM).contains(kukla) && ("IBMEM".equals(scenario))) {
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
        if (requestData.getAddress("ZS01").getLandCntry().equals("GB")) {
          if (!AutomationUtil.isTaxManagerEmeaUpdateCheck(entityManager, engineData, requestData)) {
            engineData.addNegativeCheckStatus("_vatUK", " request need to be send to CMDE queue for further review. ");
            details.append("Landed Country UK. The request need to be send to CMDE queue for further review.\n");
          }
        } else {
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
        requesterFromTeam = BluePagesHelper.isBluePagesHeirarchyManager(admin.getRequesterId(), SystemParameters.getList("ES_MOPA_AUTO_UPDATE"));
        if (requesterFromTeam) {
          details.append("Skipping validation for MOPA update for requester - " + admin.getRequesterId() + ".\n");
          engineData.addPositiveCheckStatus("SKIP_APPROVALS");
          admin.setScenarioVerifiedIndc("Y");
        }
        break;
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
      case "PPS CEID":
  		cmdeReview = validatePpsCeidForUpdateRequest(engineData, data, details, resultCodes, change, "D");
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
    int zi01count = 0;
    int zp01count = 0;
    int zd01count = 0;
    List<Integer> addrCount = getAddressCount(entityManager, SystemLocation.SPAIN, data.getCmrIssuingCntry(), data.getCmrNo());
    zi01count = addrCount.get(0);
    zp01count = addrCount.get(1);
    zd01count = addrCount.get(2);
    StringBuilder checkDetails = new StringBuilder();
    Set<String> resultCodes = new HashSet<String>();// R - review, D-Reject
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
            // CREATCMR-6586
            LOG.debug("Checking duplicates for " + addrType + "(" + addr.getId().getAddrSeq() + ")");
            if (CmrConstants.RDC_BILL_TO.equals(addrType) || CmrConstants.RDC_SECONDARY_SOLD_TO.equals(addrType)) {
              LOG.debug("Addition of " + addrType + "(" + addr.getId().getAddrSeq() + ")");
              checkDetails.append("Addition of new Mailing and EPL (" + addr.getId().getAddrSeq() + ") address skipped in the checks.\n");
            } else if (((zi01count == 0 && CmrConstants.RDC_INSTALL_AT.equals(addrType))
                || (zd01count == 0 && CmrConstants.RDC_SHIP_TO.equals(addrType))) && null == changes.getAddressChange(addrType, "Customer Name")
                && null == changes.getAddressChange(addrType, "Customer Name Con't")) {
              LOG.debug("Addition of " + addrType + "(" + addr.getId().getAddrSeq() + ")");
              checkDetails.append("Addition of new " + (addrType.equalsIgnoreCase("ZI01") ? "Installing " : "Shipping ") + "("
                  + addr.getId().getAddrSeq() + ") address skipped in the checks.\n");
            } else if (addressExists(entityManager, addr, requestData)) {
              LOG.debug(" - Duplicates found for " + addrType + "(" + addr.getId().getAddrSeq() + ")");
              checkDetails.append("Address " + addrType + "(" + addr.getId().getAddrSeq() + ") provided matches an existing address.\n");
              resultCodes.add("D");
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
    if (resultCodes.contains("D")) {
      output.setOnError(true);
      engineData.addRejectionComment("DUPADDR", "Add or update on the address is rejected", "", "");
      validation.setSuccess(false);
      validation.setMessage("Rejected");
    } else if (resultCodes.contains("R")) {
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
    String bgId = data.getBgId();
    String enterprise = null;
    String salesRep = null;

    if (!isCoverageCalculated) {
      details.setLength(0);
      overrides.clearOverrides();
    }

    if (bgId != null && !"BGNONE".equals(bgId.trim())) {
      List<SpainFieldsContainer> enterpriseResults = computeSREnterpriseES(entityManager, QUERY_BG_ES, bgId, data.getCmrIssuingCntry(), false);
      if (enterpriseResults != null && !enterpriseResults.isEmpty()) {
        for (SpainFieldsContainer field : enterpriseResults) {
          String containerCtc = StringUtils.isBlank(container.getClientTierCd()) ? "" : container.getClientTierCd();
          String calculatedCtc = StringUtils.isBlank(field.getClientTier()) ? "" : field.getClientTier();
          if (field.getIsuCd().equals(container.getIsuCd()) && containerCtc.equals(calculatedCtc) && !StringUtils.isBlank(field.getEnterprise())) {
            enterprise = field.getEnterprise();
            salesRep = !StringUtils.isBlank(field.getSalesRep()) ? field.getSalesRep().substring(4) : "";
            break;
          }
        }
      }
    }

    // HashMap<String, String> response =
    // getEntpSalRepFromPostalCodeMapping(data.getSubIndustryCd(), addr,
    // container.getIsuCd(),
    // container.getClientTierCd(), data.getCustSubGrp());

    if (!StringUtils.isBlank(enterprise) && !StringUtils.isBlank(salesRep)) {
      LOG.debug("Calculated Enterprise: " + enterprise);
      LOG.debug("Calculated Sales Rep: " + salesRep);
      details.append("Coverage calculated successfully from found CMRs.").append("\n");
      details.append("Sales Rep : " + salesRep).append("\n");
      details.append("Enterprise : " + (StringUtils.isBlank(enterprise) ? data.getEnterprise() : enterprise)).append("\n");
      overrides.addOverride(AutomationElementRegistry.GBL_CALC_COV, "DATA", "ENTERPRISE", data.getEnterprise(), enterprise);
      overrides.addOverride(AutomationElementRegistry.GBL_CALC_COV, "DATA", "REP_TEAM_MEMBER_NO", data.getRepTeamMemberNo(), salesRep);
      results.setResults("Calculated");
      results.setDetails(details.toString());
    } else if (StringUtils.isNotBlank(data.getRepTeamMemberNo()) && StringUtils.isNotBlank(data.getSalesBusOffCd())
        && StringUtils.isNotBlank(data.getEnterprise())) {
      details.append("Coverage could not be calculated from the found CMRs. Using values from request").append("\n");
      details.append("Sales Rep : " + data.getRepTeamMemberNo()).append("\n");
      details.append("Enterprise : " + data.getEnterprise()).append("\n");
      details.append("SBO : " + data.getSalesBusOffCd()).append("\n");
      results.setResults("Calculated");
      results.setDetails(details.toString());
    } else {
      String msg = "Coverage cannot be calculated. No valid CMRs found from request data.";
      details.append(msg);
      results.setResults("Cannot Calculate");
      results.setDetails(details.toString());
      engineData.addNegativeCheckStatus("_esCoverage", msg);
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
              && ("None".equalsIgnoreCase(postalMapping.getIsicBelongs()) || ((!postalCodes.isEmpty() && postalCodes.contains(postCdtStrt))
                  && (("Yes".equalsIgnoreCase(postalMapping.getIsicBelongs()) && isicCds.contains(subIndCd))
                      || ("No".equalsIgnoreCase(postalMapping.getIsicBelongs()) && !isicCds.contains(subIndCd)))))) {
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
    if (result != null || bgId.equals("DB502GQG")) {
      LOG.debug("Setting isu ctc to 5K based on gbg matching.");
      details.append("Setting isu ctc to 5K based on gbg matching.");
      overrides.addOverride(covElement.getProcessCode(), "DATA", "ISU_CD", data.getIsuCd(), "5K");
      overrides.addOverride(covElement.getProcessCode(), "DATA", "CLIENT_TIER", data.getClientTier(), "");
    }
    LOG.debug("isu" + data.getIsuCd());
    LOG.debug("client tier" + data.getClientTier());
  }

  private List<SpainFieldsContainer> computeSREnterpriseES(EntityManager entityManager, String queryBgEs, String bgId, String cmrIssuingCntry,
      boolean b) {
    List<SpainFieldsContainer> calculatedFields = new ArrayList<>();
    String sql = ExternalizedQuery.getSql(queryBgEs);
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("KEY", bgId);
    query.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
    query.setParameter("COUNTRY", cmrIssuingCntry);
    String isoCntry = PageManager.getDefaultLandedCountry(cmrIssuingCntry);
    System.err.println("ISO: " + isoCntry);
    query.setParameter("ISO_CNTRY", isoCntry);
    query.setForReadOnly(true);

    LOG.debug("Calculating Fields using Spain query " + queryBgEs + " for key: " + bgId);
    List<Object[]> results = query.getResults(5);
    if (results != null && !results.isEmpty()) {
      for (Object[] result : results) {
        SpainFieldsContainer fieldValues = new SpainFieldsContainer();

        fieldValues.setIsuCd((String) result[0]);
        fieldValues.setClientTier((String) result[1]);
        fieldValues.setEnterprise((String) result[2]);
        fieldValues.setSalesRep("1FICTI"); // Default Sales Rep for
        // Spain
        if (!StringUtils.isBlank((String) result[3])
            && AutomationUtil.validateLOVVal(entityManager, cmrIssuingCntry, "##SalRepNameNo", ((String) result[3]).substring(4))) {
          fieldValues.setSalesRep((String) result[3]);
        }
        calculatedFields.add(fieldValues);
      }
    }

    return calculatedFields;
  }

  public List<Integer> getAddressCount(EntityManager entityManager, String cmrIssuingCntry, String realCntry, String cmrNo) {
    int zi01count = 0;
    int zp01count = 0;
    int zd01count = 0;
    String sql = ExternalizedQuery.getSql("QUERY.GET.COUNT.ADDRTYP");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REALCTY", realCntry);
    query.setParameter("RCYAA", cmrIssuingCntry);
    query.setParameter("RCUXA", cmrNo);
    List<Object[]> results = query.getResults();

    if (results != null && !results.isEmpty()) {
      Object[] sResult = results.get(0);
      zi01count = Integer.parseInt(sResult[0].toString());
      zp01count = Integer.parseInt(sResult[1].toString());
      zd01count = Integer.parseInt(sResult[2].toString());
    }

    return Arrays.asList(zi01count, zp01count, zd01count);
  }
}
