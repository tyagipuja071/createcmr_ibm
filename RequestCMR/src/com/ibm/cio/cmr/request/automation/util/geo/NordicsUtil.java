package com.ibm.cio.cmr.request.automation.util.geo;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;

import org.apache.commons.digester.Digester;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

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
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.model.window.UpdatedDataModel;
import com.ibm.cio.cmr.request.model.window.UpdatedNameAddrModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.util.ConfigUtil;
import com.ibm.cio.cmr.request.util.SystemLocation;
import com.ibm.cmr.services.client.AutomationServiceClient;
import com.ibm.cmr.services.client.CmrServicesFactory;
import com.ibm.cmr.services.client.ServiceClient.Method;
import com.ibm.cmr.services.client.automation.AutomationResponse;
import com.ibm.cmr.services.client.automation.nordics.NorwayVatRequest;
import com.ibm.cmr.services.client.automation.nordics.NorwayVatResponse;
import com.ibm.cmr.services.client.matching.MatchingResponse;
import com.ibm.cmr.services.client.matching.cmr.DuplicateCMRCheckResponse;
import com.ibm.cmr.services.client.matching.dnb.DnBMatchingResponse;

/**
 * 
 * @author Shivangi
 *
 */

public class NordicsUtil extends AutomationUtil {
  private static final Logger LOG = Logger.getLogger(NordicsUtil.class);

  // Denmark
  public static final String DK_COMME_LOCAL = "DKCOM";
  public static final String DK_INTER_LOCAL = "DKINT";
  public static final String DK_GOV_LOCAL = "DKGOV";
  public static final String DK_BUSPR_LOCAL = "DKBUS";
  public static final String DK_INTSO_LOCAL = "DKISO";
  public static final String DK_PRIPE_LOCAL = "DKPRI";
  public static final String DK_IBMEM_LOCAL = "DKIBM";
  public static final String DK_THIRD_PARTY = "DK3PA";

  // Sub Regions Iceland, Greenland , Faroe islands
  public static final String FO_COMME_LOCAL = "FOCOM";
  public static final String FO_GOV_LOCAL = "FOGOV";
  public static final String FO_INTSO_LOCAL = "FOISO";
  public static final String FO_IBME_LOCAL = "FOIBM";
  public static final String FO_PRIPE_LOCAL = "FOPRI";
  public static final String FO_BP_LOCAL = "FOBUS";
  public static final String FO_INT_LOCAL = "FOINT";

  public static final String IS_COMME_LOCAL = "ISCOM";
  public static final String IS_GOV_LOCAL = "ISGOV";
  public static final String IS_INTSO_LOCAL = "ISISO";
  public static final String IS_IBME_LOCAL = "ISIBM";
  public static final String IS_PRIPE_LOCAL = "ISPRI";
  public static final String IS_INT_LOCAL = "ISINT";
  public static final String IS_BP_LOCAL = "ISBUS";

  public static final String GL_COMME_LOCAL = "GLCOM";
  public static final String GL_GOV_LOCAL = "GLGOV";
  public static final String GL_INTSO_LOCAL = "GLISO";
  public static final String GL_IBME_LOCAL = "GLIBM";
  public static final String GL_PRIPE_LOCAL = "GLPRI";
  public static final String GL_INT_LOCAL = "GLINT";
  public static final String GL_BP_LOCAL = "GLBUS";

  // Finland
  public static final String FI_COMME_LOCAL = "FICOM";
  public static final String FI_INTER_LOCAL = "FIINT";
  public static final String FI_GOV_LOCAL = "FIGOV";
  public static final String FI_BUSPR_LOCAL = "FIBUS";
  public static final String FI_INTSO_LOCAL = "FIISO";
  public static final String FI_PRIPE_LOCAL = "FIPRI";
  public static final String FI_IBMEM_LOCAL = "FIIBM";
  public static final String FI_THIRD_PARTY = "FI3PA";

  // Sub regions Estonia , Latvia , Lithuania
  public static final String EE_COMME_LOCAL = "EECOM";
  public static final String EE_GOV_LOCAL = "EEGOV";
  public static final String EE_INTSO_LOCAL = "EEISO";
  public static final String EE_IBME_LOCAL = "EEIBM";
  public static final String EE_PRIPE_LOCAL = "EEPRI";

  public static final String LT_COMME_LOCAL = "LTCOM";
  public static final String LT_GOV_LOCAL = "LTGOV";
  public static final String LT_INTSO_LOCAL = "LTISO";
  public static final String LT_IBME_LOCAL = "LTIBM";
  public static final String LT_PRIPE_LOCAL = "LTPRI";

  public static final String LV_COMME_LOCAL = "LVCOM";
  public static final String LV_GOV_LOCAL = "LVGOV";
  public static final String LV_INTSO_LOCAL = "LVISO";
  public static final String LV_IBME_LOCAL = "LVIBM";
  public static final String LV_PRIPE_LOCAL = "LVPRI";

  // Norway and Sweden
  public static final String COMME_LOCAL = "COMME";
  public static final String INTER_LOCAL = "INTER";
  public static final String GOVRN_LOCAL = "GOVRN";
  public static final String BUSPR_LOCAL = "BUSPR";
  public static final String INTSO_LOCAL = "INTSO";
  public static final String PRIPE_LOCAL = "PRIPE";
  public static final String IBMEM_LOCAL = "IBMEM";
  public static final String THIRD_PARTY = "THDPT";
  // Cross
  public static final String CROSS_COMME = "CBCOM";
  public static final String CROSS_INTER = "CBINT";
  public static final String CROSS_BUSPR = "CBBUS";
  public static final String CROSS_INTSO = "CBISO";
  public static final String CROSS_IBMEM = "CBIBM";
  public static final String CROSS_PRIPE = "CBPRI";

  private static final List<String> SCENARIOS_COVERAGE = Arrays.asList(DK_COMME_LOCAL, FO_COMME_LOCAL, GL_COMME_LOCAL, IS_COMME_LOCAL, FI_COMME_LOCAL,
      LT_COMME_LOCAL, LV_COMME_LOCAL, EE_COMME_LOCAL, COMME_LOCAL, CROSS_COMME, DK_INTSO_LOCAL, FO_INTSO_LOCAL, GL_INTSO_LOCAL, IS_INTSO_LOCAL,
      FI_INTSO_LOCAL, LT_INTSO_LOCAL, LV_INTSO_LOCAL, EE_INTSO_LOCAL, INTSO_LOCAL, CROSS_INTSO, DK_GOV_LOCAL, FO_GOV_LOCAL, GL_GOV_LOCAL,
      IS_GOV_LOCAL, FI_GOV_LOCAL, LT_GOV_LOCAL, LV_GOV_LOCAL, EE_GOV_LOCAL);

  private static final List<String> RELEVANT_ADDRESSES = Arrays.asList(CmrConstants.RDC_SOLD_TO, CmrConstants.RDC_BILL_TO,
      CmrConstants.RDC_INSTALL_AT, CmrConstants.RDC_SHIP_TO, CmrConstants.RDC_SECONDARY_SOLD_TO);
  private static final List<String> NON_RELEVANT_ADDRESS_FIELDS = Arrays.asList("Att. Person", "Phone #");

  private static List<NordicsCovMapping> coverageMapping = new ArrayList<NordicsCovMapping>();

  @SuppressWarnings("unchecked")
  public NordicsUtil() {
    if (NordicsUtil.coverageMapping.isEmpty()) {
      Digester digester = new Digester();

      digester.setValidating(false);

      digester.addObjectCreate("mappings", ArrayList.class);
      digester.addObjectCreate("mappings/mapping", NordicsCovMapping.class);

      digester.addBeanPropertySetter("mappings/mapping/country", "country");
      digester.addBeanPropertySetter("mappings/mapping/isuCTC", "isuCTC");
      digester.addBeanPropertySetter("mappings/mapping/subIndustry", "subIndustry");
      digester.addBeanPropertySetter("mappings/mapping/sortl", "sortl");
      digester.addBeanPropertySetter("mappings/mapping/salesRep", "salesRep");
      digester.addBeanPropertySetter("mappings/mapping/exclude", "exclude");

      digester.addSetNext("mappings/mapping", "add");

      try {
        InputStream is = ConfigUtil.getResourceStream("nordics-mapping.xml");
        NordicsUtil.coverageMapping = (ArrayList<NordicsCovMapping>) digester.parse(is);

      } catch (Exception e) {
        LOG.error("Error occured while digesting xml.", e);
      }
    }
  }

  @Override
  public AutomationResult<OverrideOutput> doCountryFieldComputations(EntityManager entityManager, AutomationResult<OverrideOutput> results,
      StringBuilder details, OverrideOutput overrides, RequestData requestData, AutomationEngineData engineData) throws Exception {

    Data data = requestData.getData();
    Admin admin = requestData.getAdmin();
    if ("C".equals(admin.getReqType()) && StringUtils.isNotEmpty(data.getVat()) && SystemLocation.NORWAY.equals(data.getCmrIssuingCntry())) {
      LOG.info("Starting Field Computations for Request ID " + data.getId().getReqId());
      // register vat service of Norway
      AutomationResponse<NorwayVatResponse> resp = getMVAVatInfo(admin, data);
      if (resp.isSuccess() && resp.getRecord().isMva()) {
        String newVAT = data.getVat() + "MVA";
        details.append("Appending VAT with suffix MVA.").append("\n");
        overrides.addOverride(AutomationElementRegistry.GBL_FIELD_COMPUTE, "DATA", "VAT", data.getVat(), newVAT);
        results.setResults("Calculated.");
        results.setProcessOutput(overrides);
      } else if (!resp.isSuccess()) {
        details.append("MVA will not be appeneded to VAT and request to be sent for review.").append("\n");
        engineData.addNegativeCheckStatus("MVA_NOT_APPENDED", "MVA will not be appeneded to VAT and request to be sent for review.");
      }
    } else {
      details.append("No specific fields to compute.\n");
      results.setResults("Skipped");
    }
    results.setDetails(details.toString());

    return results;
  }

  @Override
  public boolean performScenarioValidation(EntityManager entityManager, RequestData requestData, AutomationEngineData engineData,
      AutomationResult<ValidationOutput> result, StringBuilder details, ValidationOutput output) {
    // TODO Auto-generated method stub
    Data data = requestData.getData();
    String scenario = data.getCustSubGrp();
    Addr zs01 = requestData.getAddress("ZS01");
    Addr zp01 = requestData.getAddress("ZP01");
    String customerName = getCustomerFullName(zs01);

    if (zp01 != null && !compareCustomerNames(zs01, zp01)) {
      details.append("Sold-to and Bill-to name are not identical. Request will require CMDE review before proceeding.").append("\n");
      engineData.addNegativeCheckStatus("SOLDTO_BILLTO_DIFF", "Sold-to and Bill-to name are not identical.");
    }

    // if ((DK_BUSPR_LOCAL.equals(scenario) || FI_BUSPR_LOCAL.equals(scenario)
    // || BUSPR_LOCAL.equals(scenario) || CROSS_BUSPR.equals(scenario))
    // && zp01 != null && !compareCustomerNames(zs01, zp01)) {
    // details.append("Sold-to and Bill-to name are not identical for BP
    // Scenario. Request will require CMDE review before
    // proceeding.").append("\n");
    // engineData.addNegativeCheckStatus("SOLDTO_BILLTO_DIFF", "Sold-to Bill-to
    // name are not identical for BP Scenario.");
    // }

    switch (scenario) {

    case COMME_LOCAL:
    case CROSS_COMME:
    case GOVRN_LOCAL:
    case DK_COMME_LOCAL:
    case DK_GOV_LOCAL:
    case FI_COMME_LOCAL:
    case FI_GOV_LOCAL:
      if (zp01 != null && !compareCustomerNames(zs01, zp01)) {
        details.append("Sold-to and Bill-to name are not identical. Request will require CMDE review before proceeding.").append("\n");
        engineData.addNegativeCheckStatus("SOLDTO_BILLTO_DIFF", "Sold-to and Bill-to name are not identical.");
      }
      break;

    case DK_THIRD_PARTY:
    case FI_THIRD_PARTY:
    case THIRD_PARTY:
      engineData.addNegativeCheckStatus("_atTHI", "Third Party request need to be send to CMDE queue for review. ");
      details.append("Third Party request need to be send to CMDE queue for review. ").append("\n");
      return true;

    case DK_BUSPR_LOCAL:
    case FI_BUSPR_LOCAL:
    case BUSPR_LOCAL:
    case CROSS_BUSPR:
      return doBusinessPartnerChecks(engineData, data.getPpsceid(), details);
    case DK_PRIPE_LOCAL:
    case DK_IBMEM_LOCAL:
    case FI_PRIPE_LOCAL:
    case FI_IBMEM_LOCAL:
    case PRIPE_LOCAL:
    case IBMEM_LOCAL:
    case FO_PRIPE_LOCAL:
    case FO_IBME_LOCAL:
    case GL_PRIPE_LOCAL:
    case GL_IBME_LOCAL:
    case IS_PRIPE_LOCAL:
    case IS_IBME_LOCAL:
    case EE_PRIPE_LOCAL:
    case EE_IBME_LOCAL:
    case LT_PRIPE_LOCAL:
    case LT_IBME_LOCAL:
    case LV_PRIPE_LOCAL:
    case LV_IBME_LOCAL:
    case CROSS_PRIPE:
      return doPrivatePersonChecks(engineData, data.getCmrIssuingCntry(), zs01.getLandCntry(), customerName, details, false, requestData);

    case FO_GOV_LOCAL:
    case FO_INTSO_LOCAL:
    case FO_BP_LOCAL:
    case FO_INT_LOCAL:
    case IS_GOV_LOCAL:
    case IS_INTSO_LOCAL:
    case IS_INT_LOCAL:
    case IS_BP_LOCAL:
    case GL_COMME_LOCAL:
    case GL_GOV_LOCAL:
    case GL_INTSO_LOCAL:
    case GL_INT_LOCAL:
    case GL_BP_LOCAL:
      engineData.addPositiveCheckStatus(AutomationEngineData.SKIP_GBG);
      engineData.addPositiveCheckStatus(AutomationEngineData.SKIP_COVERAGE);
      engineData.addPositiveCheckStatus(AutomationEngineData.SKIP_FIELD_COMPUTATION);
      engineData.addPositiveCheckStatus(AutomationEngineData.SKIP_RETRIEVE_VALUES);
    }

    return true;

  }

  @Override
  public void filterDuplicateCMRMatches(EntityManager entityManager, RequestData requestData, AutomationEngineData engineData,
      MatchingResponse<DuplicateCMRCheckResponse> response) {
    String[] bpScenariosToBeChecked = { "BUSPR", "FIBUS", "DKBUS", "CBBUS" };
    String[] isoScenariosToBeChecked = { "INTSO", "DKISO", "FIISO", "CBISO" };
    String[] intScenariosToBeChecked = { "INTER", "DKINT", "FIINT", "CBINT" };

    String scenario = requestData.getData().getCustSubGrp();
    String[] kuklaBUSPR = { "43", "44", "45" };
    String[] kuklaISO = { "81", "85" };
    String cntry = requestData.getData().getCmrIssuingCntry();

    List<DuplicateCMRCheckResponse> matches = response.getMatches();
    List<DuplicateCMRCheckResponse> filteredMatches = new ArrayList<DuplicateCMRCheckResponse>();

    if (Arrays.asList(bpScenariosToBeChecked).contains(scenario)) {
      for (DuplicateCMRCheckResponse match : matches) {
        if (StringUtils.isNotBlank(match.getCustClass())) {
          String kukla = match.getCustClass() != null ? match.getCustClass() : "";
          if (Arrays.asList(kuklaBUSPR).contains(kukla) || isuCTCMatch(match.getCmrNo(), entityManager, cntry)) {
            filteredMatches.add(match);
          }
        }
      }
      // set filtered matches in response
      response.setMatches(filteredMatches);
    }

    else if (Arrays.asList(isoScenariosToBeChecked).contains(scenario)) {
      for (DuplicateCMRCheckResponse match : matches) {
        if (StringUtils.isNotBlank(match.getCustClass())) {
          String kukla = match.getCustClass() != null ? match.getCustClass() : "";
          if (Arrays.asList(kuklaISO).contains(kukla) && (match.getCmrNo().startsWith("997"))) {
            filteredMatches.add(match);
          }
        }
      }
      // set filtered matches in response
      response.setMatches(filteredMatches);
    }

    else if (Arrays.asList(intScenariosToBeChecked).contains(scenario)) {
      for (DuplicateCMRCheckResponse match : matches) {
        if (StringUtils.isNotBlank(match.getCustClass())) {
          if ((match.getCmrNo().startsWith("99") && !match.getCmrNo().startsWith("997"))) {
            filteredMatches.add(match);
          }
        }
      }
      // set filtered matches in response
      response.setMatches(filteredMatches);
    }
  }

  private boolean isuCTCMatch(String cmr, EntityManager entityManager, String cntry) {
    boolean def = false;
    String isuCTC = "";
    String sql = ExternalizedQuery.getSql("KNA1.NORDICS.BP.ISUCTC");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
    query.setParameter("CNTRY", cntry);
    query.setParameter("CMR", cmr);
    query.setForReadOnly(true);
    isuCTC = query.getSingleResult(String.class);
    if ("8B7".equalsIgnoreCase(isuCTC)) {
      def = true;
    }

    return def;
  }

  @Override
  public boolean performCountrySpecificCoverageCalculations(CalculateCoverageElement covElement, EntityManager entityManager,
      AutomationResult<OverrideOutput> results, StringBuilder details, OverrideOutput overrides, RequestData requestData,
      AutomationEngineData engineData, String covFrom, CoverageContainer container, boolean isCoverageCalculated) throws Exception {
    Data data = requestData.getData();
    String scenario = data.getCustSubGrp();
    String reqSubInd = data.getSubIndustryCd().substring(0, 1);
    String cntry = data.getCountryUse() != null ? data.getCountryUse() : data.getCmrIssuingCntry();
    String coverageId = container.getFinalCoverage();
    HashMap<String, String> response = new HashMap<String, String>();
    response.put("MATCHING", "No Match Found.");

    details.append("\n");
    if (!isCoverageCalculated || (isCoverageCalculated && StringUtils.isNotBlank(coverageId) && !CalculateCoverageElement.COV_BG.equals(covFrom))
        && "34".equals(data.getIsuCd()) && "Q".equals(data.getClientTier()) && SCENARIOS_COVERAGE.contains(scenario)) {
      if (!isCoverageCalculated) {
        overrides.clearOverrides();
        details.setLength(0); // clearing details
      }
      List<String> subIndList = new ArrayList<String>();
      if (!coverageMapping.isEmpty()) {
        String[] subInd = null;
        for (NordicsCovMapping mapping : coverageMapping) {
          if (!StringUtils.isEmpty(mapping.getSubIndustry())) {
            subInd = mapping.getSubIndustry().replaceAll("\n", "").replaceAll(" ", "").split(",");
            subIndList = Arrays.asList(subInd);
          }
          if (mapping.getCountry().equals(cntry) && ((!subIndList.isEmpty()
              && ((mapping.isExclude() && !subIndList.contains(reqSubInd)) || (!mapping.isExclude() && subIndList.contains(reqSubInd))))
              || (mapping.getSubIndustry() == null && StringUtils.isNotBlank(mapping.getSortl())))) {
            details.append("Calculating coverage using 34Q logic.").append("\n");

            details.append("SORTL : " + mapping.getSortl()).append("\n");
            overrides.addOverride(AutomationElementRegistry.GBL_CALC_COV, "DATA", "SEARCH_TERM", data.getSearchTerm(), mapping.getSortl());

            details.append("Sales Rep : " + mapping.getSalesRep());
            overrides.addOverride(AutomationElementRegistry.GBL_CALC_COV, "DATA", "REP_TEAM_MEMBER_NO", data.getRepTeamMemberNo(),
                mapping.getSalesRep());
            response.put("MATCHING", "Match Found.");
            break;
          }
        }

        if ("No Match Found.".equalsIgnoreCase(response.get("MATCHING"))) {
          details.append("Coverage couldn't be calculated through 34Q logic as no match found.\n").append("\n");
        }

      } else {
        details.setLength(0);
        overrides.clearOverrides();
        details.append("Coverage could not be calculated through Buying group or 34Q logic.\n Skipping coverage calculation.").append("\n");
        results.setResults("Skipped");
      }

    }
    return true;
  }

  @Override
  public boolean runUpdateChecksForData(EntityManager entityManager, AutomationEngineData engineData, RequestData requestData,
      RequestChangeContainer changes, AutomationResult<ValidationOutput> output, ValidationOutput validation) throws Exception {
    Admin admin = requestData.getAdmin();
    Data data = requestData.getData();
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
            engineData.addNegativeCheckStatus("_esVATCheckFailed", "VAT # on the request did not match D&B");
            details.append("VAT # on the request did not match D&B\n");
          } else {
            details.append("VAT # on the request matches D&B\n");
            engineData.setVatVerified(true, "VAT Verified");
          }
        }
        break;
      case "ISU":
      case "Client Tier":
      case "Tax Code":
      case "SORTL":
      case "Sales Rep":
        // noop, for switch handling only
        break;
      case "Order Block Code":
        if ("K".equals(change.getOldData()) || "K".equals(change.getNewData()) || "D".equals(change.getNewData())
            || "D".equals(change.getNewData())) {
          // noop, for switch handling only
        }
        break;
      case "ISIC":
      case "INAC/NAC Code":
      case "KUKLA":
      case "Leading Account Number":
        resultCodes.add("D");// Reject
        details.append("In case of INAC update/ ISIC update/ KUKLA update/ Leading Account Number update please raise a JIRA ticket:").append("\n");
        details.append("*https://jira.data.zc2.ibm.com/servicedesk/customer/portal/14*").append("\n");
        details.append("Thank you.");
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
    List<Addr> addresses = null;
    StringBuilder checkDetails = new StringBuilder();
    Set<String> resultCodes = new HashSet<String>();// R - review
    for (String addrType : RELEVANT_ADDRESSES) {
      if (changes.isAddressChanged(addrType)) {
        addresses = requestData.getAddresses(addrType);
        for (Addr addr : addresses) {
          if ("N".equals(addr.getImportInd())) {
            // new address
            if (CmrConstants.RDC_SHIP_TO.equals(addrType) || CmrConstants.RDC_INSTALL_AT.equals(addrType)) {
              LOG.debug("Addition of " + addrType + "(" + addr.getId().getAddrSeq() + ")");
              LOG.debug("Checking duplicates for " + addrType + "(" + addr.getId().getAddrSeq() + ")");
              boolean duplicate = addressExists(entityManager, addr, requestData);
              if (duplicate) {
                LOG.debug(" - Duplicates found for " + addrType + "(" + addr.getId().getAddrSeq() + ")");
                checkDetails.append("Address " + addrType + "(" + addr.getId().getAddrSeq() + ") provided matches an existing address.\n");
                resultCodes.add("R");
              } else {
                LOG.debug("Addition/Updation of " + addrType + "(" + addr.getId().getAddrSeq() + ")");
                checkDetails.append("Address (" + addr.getId().getAddrSeq() + ") is validated.\n");
              }
            }

          } else if ("Y".equals(addr.getChangedIndc())) {
            // update address
            if (CmrConstants.RDC_SOLD_TO.equals(addrType) || CmrConstants.RDC_BILL_TO.equals(addrType)) {
              if (isRelevantAddressFieldUpdated(changes, addr)) {
                List<DnBMatchingResponse> matches = getMatches(requestData, engineData, addr, false);
                boolean matchesDnb = false;
                if (matches != null) {
                  // check against D&B
                  matchesDnb = ifaddressCloselyMatchesDnb(matches, addr, admin, data.getCmrIssuingCntry());
                }
                if (!matchesDnb) {
                  LOG.debug("Update address for " + addrType + "(" + addr.getId().getAddrSeq() + ") does not match D&B");
                  resultCodes.add("X");
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
              if (CmrConstants.RDC_SHIP_TO.equals(addrType) || CmrConstants.RDC_INSTALL_AT.equals(addrType)) {
                // proceed
                LOG.debug("Update to " + addrType + "(" + addr.getId().getAddrSeq() + ")");
                checkDetails.append("Updates to (" + addr.getId().getAddrSeq() + ") ignored in the checks.\n");
              } else {
                checkDetails.append("Updates to Updated Addresses for " + addrType + "(" + addr.getId().getAddrSeq() + ") needs to be verified")
                    .append("\n");
                resultCodes.add("X");
              }
            }
          }
        }
      }
    }
    if (resultCodes.contains("X")) {
      validation.setSuccess(false);
      validation.setMessage("Review Required.");
      engineData.addNegativeCheckStatus("_esCheckFailed", "Updated elements cannot be checked automatically.");
    } else if (resultCodes.contains("R")) {
      output.setOnError(true);
      engineData.addRejectionComment("_atRejectAddr", "Addition or updation on the address is rejected", "", "");
      validation.setSuccess(false);
      validation.setMessage("Rejected.");
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

  private AutomationResponse<NorwayVatResponse> getMVAVatInfo(Admin admin, Data data) throws Exception {
    AutomationServiceClient client = CmrServicesFactory.getInstance().createClient(SystemConfiguration.getValue("BATCH_SERVICES_URL"),
        AutomationServiceClient.class);
    client.setReadTimeout(1000 * 60 * 5);
    client.setRequestMethod(Method.Get);

    NorwayVatRequest request = new NorwayVatRequest();
    String vatReq = StringUtils.isNumeric(data.getVat()) ? data.getVat() : data.getVat().substring(2);
    request.setVat(vatReq);
    System.out.println(request + request.getVat());

    LOG.debug("Connecting to the Norway VAT service at " + SystemConfiguration.getValue("BATCH_SERVICES_URL"));
    AutomationResponse<?> rawResponse = client.executeAndWrap(AutomationServiceClient.NORWAY_VAT_SERVICE_ID, request, AutomationResponse.class);
    ObjectMapper mapper = new ObjectMapper();
    String json = mapper.writeValueAsString(rawResponse);
    TypeReference<AutomationResponse<NorwayVatResponse>> ref = new TypeReference<AutomationResponse<NorwayVatResponse>>() {
    };
    return mapper.readValue(json, ref);
  }

}
