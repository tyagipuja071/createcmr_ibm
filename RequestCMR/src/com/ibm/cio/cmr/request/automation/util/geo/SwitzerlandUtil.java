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
import com.ibm.cio.cmr.request.automation.util.DACHFieldContainer;
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
import com.ibm.cmr.services.client.matching.gbg.GBGFinderRequest;

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
      CmrConstants.RDC_INSTALL_AT, CmrConstants.RDC_SHIP_TO, CmrConstants.RDC_PAYGO_BILLING);

  private static final List<String> NON_RELEVANT_ADDRESS_FIELDS = Arrays.asList("Building", "Floor", "Department", "PostBox",
      "Attention to/Building/Floor/Office", "Att. Person", "Phone #", "FAX", "Customer Name 4");

  private static final List<String> RELEVANT_ADDRESS_FIELDS_ZS01_ZP01 = Arrays.asList("Street name and number", "Customer legal name");
  private static final List<String> NON_RELEVANT_ADDRESS_FIELDS_ZS01_ZP01 = Arrays.asList("Attention to/Building/Floor/Office",
      "Division/Department");

  // private static List<ChMubotyMapping> mubotyMappings = new
  // ArrayList<ChMubotyMapping>();
  //
  // @SuppressWarnings("unchecked")
  // public SwitzerlandUtil() {
  // if (SwitzerlandUtil.mubotyMappings.isEmpty()) {
  // Digester digester = new Digester();
  // digester.setValidating(false);
  // digester.addObjectCreate("mappings", ArrayList.class);
  //
  // digester.addObjectCreate("mappings/mapping", ChMubotyMapping.class);
  //
  // digester.addBeanPropertySetter("mappings/mapping/ims", "ims");
  // digester.addBeanPropertySetter("mappings/mapping/postalCdMin",
  // "postalCdMin");
  // digester.addBeanPropertySetter("mappings/mapping/postalCdMax",
  // "postalCdMax");
  // digester.addBeanPropertySetter("mappings/mapping/isu", "isu");
  // digester.addBeanPropertySetter("mappings/mapping/ctc", "ctc");
  // digester.addBeanPropertySetter("mappings/mapping/muboty", "muboty");
  // digester.addSetNext("mappings/mapping", "add");
  // try {
  // InputStream is = ConfigUtil.getResourceStream("ch-muboty-mapping.xml");
  // SwitzerlandUtil.mubotyMappings = (ArrayList<ChMubotyMapping>)
  // digester.parse(is);
  // // test
  // ChMubotyMapping mapping = getMubotyFromMapping("C", "1000", "32", "S");
  // LOG.debug(new ObjectMapper().writeValueAsString(mapping));
  // } catch (Exception e) {
  // LOG.error("Error occured while digesting xml.", e);
  // }
  // }
  // }

  @Override
  public boolean performScenarioValidation(EntityManager entityManager, RequestData requestData, AutomationEngineData engineData,
      AutomationResult<ValidationOutput> result, StringBuilder details, ValidationOutput output) {
    Data data = requestData.getData();
    Addr soldTo = requestData.getAddress("ZS01");
    String scenario = data.getCustSubGrp();
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
    LOG.info("Starting scenario validations for Request ID " + data.getId().getReqId());

    if (StringUtils.isBlank(scenario) || scenario.length() != 5) {
      details.append("Scenario not correctly specified on the request");
      engineData.addNegativeCheckStatus("_chNoScenario", "Scenario not correctly specified on the request");
      return true;
    }

    if ("C".equals(requestData.getAdmin().getReqType())) {
      // remove duplicates
      removeDuplicateAddresses(entityManager, requestData, details);
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
        engineData.addPositiveCheckStatus(AutomationEngineData.SKIP_GBG);
      case SCENARIO_IBM_EMPLOYEE:
        return doPrivatePersonChecks(engineData, SystemLocation.SWITZERLAND, soldTo.getLandCntry(), customerName, details,
            SCENARIO_IBM_EMPLOYEE.equals(actualScenario), requestData);
      case SCENARIO_INTERNAL:
        break;
      case SCENARIO_GOVERNMENT:
        break;
      case SCENARIO_THIRD_PARTY:
        engineData.addNegativeCheckStatus("_chThirdParty", "Third Party/Data Center request needs further validation.");
        details.append("Third Party/Data Center request needs further validation.").append("\n");
        break;
      case SCENARIO_BUSINESS_PARTNER:
        engineData.addPositiveCheckStatus(AutomationEngineData.SKIP_GBG);
        engineData.addPositiveCheckStatus(AutomationEngineData.SKIP_COVERAGE);
        return doBusinessPartnerChecks(engineData, data.getPpsceid(), details);
      }
    }

    return true;
  }

  @Override
  protected List<String> getCountryLegalEndings() {
    return Arrays.asList("GMBH", "KLG", "AG", "Sàrl", "SARL", "SA", "S.A.", "SAGL");
  }

  @Override
  public void tweakDnBMatchingRequest(GBGFinderRequest request, RequestData requestData, AutomationEngineData engineData) {
    Data data = requestData.getData();
    if (StringUtils.isNotBlank(data.getVat()) && SystemLocation.SWITZERLAND.equalsIgnoreCase(data.getCmrIssuingCntry())) {
      request.setOrgId(data.getVat().split("\\s")[0]);
    }
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
            } else if ((payGoAddredited && addrTypesChanged.contains(CmrConstants.RDC_PAYGO_BILLING.toString())) || isZS01WithAufsdPG) {
              if ("N".equals(addr.getImportInd())) {
                LOG.debug("Checking duplicates for " + addrType + "(" + addr.getId().getAddrSeq() + ")");
                boolean duplicate = addressExists(entityManager, addr, requestData);
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
      engineData.addNegativeCheckStatus("_chCheckFailed", "Updated elements cannot be checked automatically.");
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
              engineData.addNegativeCheckStatus("_chVATCheckFailed", "VAT # on the request did not match D&B");
              details.append("VAT # on the request did not match D&B\n");
            } else {
              details.append("VAT # on the request matches D&B\n");
            }
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
      case "SORTL":
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
    Data data = requestData.getData();
    Admin admin = requestData.getAdmin();
    if ("C".equals(admin.getReqType())) {
      String scenario = data.getCustSubGrp();
      LOG.info("Starting Field Computations for Request ID " + data.getId().getReqId());
      String actualScenario = scenario.substring(2);
      if (StringUtils.isNotBlank(actualScenario)
          && !(SCENARIO_BUSINESS_PARTNER.equals(actualScenario) || SCENARIO_IBM_EMPLOYEE.equals(actualScenario)
              || SCENARIO_PRIVATE_CUSTOMER.equals(actualScenario) || SCENARIO_INTERNAL.equals(actualScenario))
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
      return true;
    }
    Data data = requestData.getData();
    Addr soldTo = requestData.getAddress("ZS01");
    String scenario = data.getCustSubGrp();
    LOG.info("Starting coverage calculations for Request ID " + requestData.getData().getId().getReqId());
    String actualScenario = scenario.substring(2);
    String coverage = data.getSearchTerm();
    LOG.debug("coverageId--------------------" + container.getFinalCoverage());
    LOG.debug("sortl--------------------" + coverage);
    String isuCd = null;
    String clientTier = null;
    if (StringUtils.isNotBlank(container.getIsuCd())) {
      isuCd = container.getIsuCd();
      clientTier = container.getClientTierCd();
    } else {
      isuCd = data.getIsuCd();
      clientTier = data.getClientTier();
    }

    List<String> covList = Arrays.asList("A0004520", "A0004515", "A0004541", "A0004580");

    if (data.getBgId() != null && !"BGNONE".equals(data.getBgId().trim())) {
      List<DACHFieldContainer> queryResults = AutomationUtil.computeDACHCoverageElements(entityManager, "AUTO.COV.CALCULATE_COV_ELEMENTS_DACH",
          data.getBgId(), data.getCmrIssuingCntry());
      if (queryResults != null && !queryResults.isEmpty()) {
        for (DACHFieldContainer result : queryResults) {
          DACHFieldContainer queryResult = (DACHFieldContainer) result;
          String containerCtc = StringUtils.isBlank(container.getClientTierCd()) ? "" : container.getClientTierCd();
          String containerIsu = StringUtils.isBlank(container.getIsuCd()) ? "" : container.getIsuCd();
          String queryIsu = queryResult.getIsuCd();
          String queryCtc = queryResult.getClientTier();
          if (containerIsu.equals(queryIsu) && containerCtc.equals(queryCtc)) {
            overrides.addOverride(AutomationElementRegistry.GBL_CALC_COV, "DATA", "SEARCH_TERM", data.getSearchTerm(), queryResult.getSearchTerm());
            overrides.addOverride(AutomationElementRegistry.GBL_CALC_COV, "DATA", "INAC_CD", data.getInacCd(), queryResult.getInac());
            overrides.addOverride(AutomationElementRegistry.GBL_CALC_COV, "DATA", "ENTERPRISE", data.getEnterprise(), queryResult.getEnterprise());
            details.append("Data calculated based on CMR Data:").append("\n");
            details.append(" - SORTL = " + queryResult.getSearchTerm()).append("\n");
            details.append(" - INAC Code = " + queryResult.getInac()).append("\n");
            details.append(" - Enterprise = " + queryResult.getEnterprise()).append("\n");
            engineData.addPositiveCheckStatus(AutomationEngineData.COVERAGE_CALCULATED);
            results.setResults("Calculated");
            break;
          }
        }
      }
    } else {

      // ChMubotyMapping muboty = null;
      String sortl = null;
      switch (actualScenario) {
      case SCENARIO_COMMERCIAL:
        sortl = getSortlForISUCTC(entityManager, data.getSubIndustryCd(), soldTo.getPostCd(), isuCd, clientTier);
        break;
      case SCENARIO_PRIVATE_CUSTOMER:
      case SCENARIO_IBM_EMPLOYEE:
        sortl = getSortlForISUCTC(entityManager, data.getSubIndustryCd(), soldTo.getPostCd(), isuCd, clientTier);
        break;
      default:
        if ("34".equals(data.getIsuCd()) && ("Q".equals(data.getClientTier())) && !isCoverageCalculated) {
          if (SCENARIO_CROSS_BORDER.equals(scenario)) {
            // verified all logic is based on 3000-9999 condition for
            // crossborders
            sortl = getSortlForISUCTC(entityManager, data.getSubIndustryCd(), "3000", data.getIsuCd(), data.getClientTier());

          } else {
            sortl = getSortlForISUCTC(entityManager, data.getSubIndustryCd(), soldTo.getPostCd(), data.getIsuCd(), data.getClientTier());

          }
        }
      }

      LOG.debug("----CovId() + Coverage--" + data.getCovId());
      if (sortl != null) {
        details.append("Setting SORTL to " + sortl + " based on Postal Code rules.");
        overrides.addOverride(covElement.getProcessCode(), "DATA", "SEARCH_TERM", data.getSearchTerm(), sortl);
        engineData.addPositiveCheckStatus(AutomationEngineData.COVERAGE_CALCULATED);
        results.setResults("Calculated");
      } else if (!isCoverageCalculated) {
        String searchTerm = data.getSearchTerm();
        if (!StringUtils.isBlank(searchTerm)) {
          String msg = "No valid SORTL mapping from request data. Using SORTL " + searchTerm + " from request.";
          details.append(msg);
          results.setResults("Calculated");
          results.setDetails(details.toString());
        } else {
          String msg = "Coverage cannot be calculated. No valid SORTL mapping from request data.";
          details.append(msg);
          results.setResults("Cannot Calculate");
          results.setDetails(details.toString());
          engineData.addNegativeCheckStatus("_chMuboty", msg);
        }
      }

    }

    LOG.debug("Before Setting isu ctc to 28-7 for matched coverage from list");
    System.out.println("sortl--------------------" + data.getSearchTerm() + "coverage---" + coverage);
    if ((SCENARIO_COMMERCIAL.equals(actualScenario) || SCENARIO_GOVERNMENT.equals(actualScenario)) && StringUtils.isNotBlank(coverage)
        && covList.contains(coverage)) {
      LOG.debug("Setting isu ctc to 28-7 based on coverage mapping.");
      details.append("Setting isu ctc to 287 based on coverage mapping.");
      overrides.addOverride(covElement.getProcessCode(), "DATA", "ISU_CD", data.getIsuCd(), "28");
      overrides.addOverride(covElement.getProcessCode(), "DATA", "CLIENT_TIER", data.getClientTier(), "7");
    }
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

  // public ChMubotyMapping getMubotyFromMapping(String subIndustryCd, String
  // postCd, String isuCd, String clientTier) {
  // if (!mubotyMappings.isEmpty()) {
  // int postalCd = Integer.parseInt(postCd);
  // if (StringUtils.isNotBlank(subIndustryCd) && subIndustryCd.length() > 1) {
  // subIndustryCd = subIndustryCd.substring(0, 1);
  // }
  // for (ChMubotyMapping mapping : mubotyMappings) {
  // List<String> subIndustryCds = Arrays.asList(mapping.getIms().split(","));
  // if (StringUtils.isNotBlank(subIndustryCd) &&
  // subIndustryCds.contains(subIndustryCd) && isuCd.equals(mapping.getIsu())
  // && clientTier.equals(mapping.getCtc())) {
  // if (StringUtils.isNotBlank(mapping.getPostalCdMin()) &&
  // StringUtils.isNotBlank(mapping.getPostalCdMax())) {
  // int start = Integer.parseInt(mapping.getPostalCdMin());
  // int end = Integer.parseInt(mapping.getPostalCdMax());
  // if (postalCd >= start && postalCd <= end) {
  // return mapping;
  // }
  // }
  // }
  // }
  // }
  // return null;
  // }

  public String getSortlForISUCTC(EntityManager entityManager, String subIndustryCd, String postCd, String isuCd, String clientTier) {
    if (StringUtils.isNotBlank(isuCd)) {

      if (StringUtils.isNotBlank(subIndustryCd) && subIndustryCd.length() > 1) {
        subIndustryCd = subIndustryCd.substring(0, 1);
      } else {
        subIndustryCd = "";
      }

      if (StringUtils.isNotBlank(postCd) && "34".equals(isuCd) && StringUtils.isNumeric(postCd)) {
        int postalCode = Integer.parseInt(postCd);
        if (postalCode >= 3000) {
          // postCd=2 represents the 2nd range which is 3000 to 9999
          postCd = "2";
        } else {
          // postCd=1 represents the 1st range which is 1000 to 2999
          postCd = "1";
          subIndustryCd = "";
        }
      } else {
        postCd = "";
      }

      if (!"34".equals("isuCd") && !"Q".equals(clientTier)) {
        subIndustryCd = "";
        postCd = "";
      }
      clientTier = StringUtils.isNotBlank(clientTier) ? clientTier : "";

      String sql = ExternalizedQuery.getSql("QUERY.SWISS.GET.SORTL_BY_ISUCTCIMS");
      PreparedQuery query = new PreparedQuery(entityManager, sql);
      query.setParameter("ISU_CD", "%" + isuCd + "%");
      query.setParameter("CLIENT_TIER", "%" + clientTier + "%");
      query.setParameter("IMS", "%" + subIndustryCd + "%");
      query.setParameter("POST_CD_RANGE", postCd);
      query.setForReadOnly(true);
      List<Object[]> result = query.getResults();
      if (result != null && result.size() == 1) {
        String sortl = (String) result.get(0)[0];
        return sortl;
      }
    }
    return null;
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
    LOG.debug("perform coverage based on GBG");
    LOG.debug("result--------" + result);
    if (result != null || bgId.equals("DB500JRX")) {
      LOG.debug("Setting isu-ctc to 34Y and sortl based on gbg matching.");
      details.append("Setting isu-ctc to 34Y and sortl based on gbg matching.");
      overrides.addOverride(covElement.getProcessCode(), "DATA", "ISU_CD", data.getIsuCd(), "34");
      overrides.addOverride(covElement.getProcessCode(), "DATA", "CLIENT_TIER", data.getClientTier(), "Y");
      overrides.addOverride(covElement.getProcessCode(), "DATA", "SEARCH_TERM", data.getSearchTerm(), "T0007971");
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
