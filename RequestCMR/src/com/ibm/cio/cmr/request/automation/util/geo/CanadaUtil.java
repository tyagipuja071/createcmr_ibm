package com.ibm.cio.cmr.request.automation.util.geo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.ui.ModelMap;

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
import com.ibm.cio.cmr.request.model.requestentry.RequestEntryModel;
import com.ibm.cio.cmr.request.model.window.UpdatedDataModel;
import com.ibm.cio.cmr.request.model.window.UpdatedNameAddrModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.CmrClientService;
import com.ibm.cio.cmr.request.util.JpaManager;
import com.ibm.cmr.services.client.matching.dnb.DnBMatchingResponse;

/**
 * 
 * @author clint
 *
 */

public class CanadaUtil extends AutomationUtil {

  private static final Logger LOG = Logger.getLogger(CanadaUtil.class);
  private static List<DeSortlMapping> sortlMappings = new ArrayList<DeSortlMapping>();
  private static final String MATCHING = "matching";
  private static final String POSTAL_CD_RANGE = "postalCdRange";
  private static final String SORTL = "SORTL";

  private static final String SCENARIO_COMMERCIAL = "COMME";
  private static final String SCENARIO_BUSINESS_PARTNER = "BUSP";
  private static final String SCENARIO_PRIVATE_HOUSEHOLD = "PRIV";
  private static final String SCENARIO_INTERNAL = "INTER";
  private static final String SCENARIO_OEM = "OEM";
  private static final String SCENARIO_STRATEGIC_OUTSOURCING = "SOCUS";
  private static final String SCENARIO_GOVERNMENT = "GOVT";
  private static final String SCENARIO_CROSS_BORDER_USA = "USA";
  private static final String SCENARIO_CROSS_BORDER_CARIB = "CND";

  private static final List<String> RELEVANT_ADDRESSES = Arrays.asList(CmrConstants.RDC_SOLD_TO, CmrConstants.RDC_BILL_TO,
      CmrConstants.RDC_INSTALL_AT, CmrConstants.RDC_SHIP_TO, "ZP02", "ZP03", "ZP04", "ZP05", "ZP06", "ZP07", "ZP08", "ZP09");

  private static final List<String> NON_RELEVANT_ADDRESS_FIELDS = Arrays.asList("Building", "Floor", "Office", "Department", "Customer Name 2",
      "Phone #", "PostBox", "State/Province");

  @Override
  public boolean performScenarioValidation(EntityManager entityManager, RequestData requestData, AutomationEngineData engineData,
      AutomationResult<ValidationOutput> results, StringBuilder details, ValidationOutput output) {
    Data data = requestData.getData();
    Addr zs01 = requestData.getAddress("ZS01");
    boolean valid = true;
    String scenario = data.getCustSubGrp();

    if (StringUtils.isBlank(scenario)) {
      details.append("Scenario not correctly specified on the request");
      engineData.addNegativeCheckStatus("_atNoScenario", "Scenario not correctly specified on the request");
      return true;
    }
    LOG.info("Starting scenario validations for Request ID " + data.getId().getReqId());
    LOG.debug("Scenario to check: " + scenario);

    if ("C".equals(requestData.getAdmin().getReqType())) {
      // remove duplicates
      removeDuplicateAddresses(entityManager, requestData, details);
    }
    // engineData.setMatchDepartment(true);

    if (StringUtils.isNotBlank(scenario)) {
      switch (scenario) {
      case SCENARIO_COMMERCIAL:
      case SCENARIO_PRIVATE_HOUSEHOLD:
      case SCENARIO_INTERNAL:
      case SCENARIO_OEM:
      case SCENARIO_STRATEGIC_OUTSOURCING:
      case SCENARIO_GOVERNMENT:
      case SCENARIO_CROSS_BORDER_USA:
      case SCENARIO_CROSS_BORDER_CARIB:
        break;
      case SCENARIO_BUSINESS_PARTNER:
        return doBusinessPartnerChecks(engineData, data.getPpsceid(), details);
      }
    } else {
      valid = false;
      engineData.addRejectionComment("TYPR", "Wrong type of request.", "No Scenario found on the request", "");
      details.append("No Scenario found on the request").append("\n");
    }
    return valid;
  }

  @Override
  public AutomationResult<OverrideOutput> doCountryFieldComputations(EntityManager entityManager, AutomationResult<OverrideOutput> results,
      StringBuilder details, OverrideOutput overrides, RequestData requestData, AutomationEngineData engineData) throws Exception {
    Addr zs01 = requestData.getAddress("ZS01");
    String custNm1 = StringUtils.isNotBlank(zs01.getCustNm1()) ? zs01.getCustNm1().trim() : "";
    String custNm2 = StringUtils.isNotBlank(zs01.getCustNm2()) ? zs01.getCustNm2().trim() : "";
    String mainCustNm = (custNm1 + (StringUtils.isNotBlank(custNm2) ? " " + custNm2 : "")).toUpperCase();
    String mainStreetAddress1 = (StringUtils.isNotBlank(zs01.getAddrTxt()) ? zs01.getAddrTxt() : "").trim().toUpperCase();
    String mainCity = (StringUtils.isNotBlank(zs01.getCity1()) ? zs01.getCity1() : "").trim().toUpperCase();
    String mainPostalCd = (StringUtils.isNotBlank(zs01.getPostCd()) ? zs01.getPostCd() : "").trim();
    Iterator<Addr> it = requestData.getAddresses().iterator();
    boolean removed = false;
    details.append("Checking for duplicate address records - ").append("\n");
    while (it.hasNext()) {
      Addr addr = it.next();
      if (!"ZS01".equals(addr.getId().getAddrType())) {
        removed = true;
        String custNm = (addr.getCustNm1().trim() + (StringUtils.isNotBlank(addr.getCustNm2()) ? " " + addr.getCustNm2().trim() : "")).toUpperCase();
        if (custNm.equals(mainCustNm) && addr.getAddrTxt().trim().toUpperCase().equals(mainStreetAddress1)
            && addr.getCity1().trim().toUpperCase().equals(mainCity) && addr.getPostCd().trim().equals(mainPostalCd)) {
          details.append("Removing duplicate address record: " + addr.getId().getAddrType() + " from the request.").append("\n");
          Addr merged = entityManager.merge(addr);
          if (merged != null) {
            entityManager.remove(merged);
          }
          it.remove();
        }
      }
    }

    if (!removed) {
      details.append("No duplicate address records found on the request.").append("\n");
    }

    return results;
  }

  private HashMap<String, String> getSORTLFromPostalCodeMapping(String subIndustryCd, String postCd, String isuCd, String clientTier) {
    HashMap<String, String> response = new HashMap<String, String>();
    response.put(MATCHING, "");
    response.put(POSTAL_CD_RANGE, "");
    response.put(SORTL, "");
    if (!sortlMappings.isEmpty()) {
      int postalCd = Integer.parseInt(postCd);
      int distance = 1000;
      String nearbySortl = null;
      String nearbyPostalCdRange = null;
      for (DeSortlMapping mapping : sortlMappings) {
        List<String> subIndustryCds = Arrays.asList(mapping.getSubIndustryCds().replaceAll("\n", "").replaceAll(" ", "").split(","));
        if (subIndustryCds.contains(subIndustryCd) && isuCd.equals(mapping.getIsu()) && clientTier.equals(mapping.getCtc())) {
          if (StringUtils.isNotBlank(mapping.getPostalCdRanges())) {
            String[] postalCodeRanges = mapping.getPostalCdRanges().replaceAll("\n", "").replaceAll(" ", "").split(",");
            for (String postalCdRange : postalCodeRanges) {
              String[] range = postalCdRange.split("to");
              int start = 0;
              int end = 0;
              if (range.length == 2) {
                start = Integer.parseInt(range[0]);
                end = Integer.parseInt(range[1]);
              } else if (range.length == 1) {
                start = Integer.parseInt(range[0].replaceAll("x", "0"));
                end = Integer.parseInt(range[0].replaceAll("x", "9"));
              }
              String postalCodeRange = start + " to " + end;
              if (postalCd >= start && postalCd <= end) {
                response.put(MATCHING, "Exact Match");
                response.put(SORTL, mapping.getSortl());
                response.put(POSTAL_CD_RANGE, postalCodeRange);
                return response;
              } else if (postalCd > end) {
                int diff = postalCd - end;
                if (diff > 0 && diff < distance) {
                  distance = diff;
                  nearbySortl = mapping.getSortl();
                  nearbyPostalCdRange = postalCodeRange;
                }
              } else if (postalCd < start) {
                int diff = start - postalCd;
                if (diff > 0 && diff < distance) {
                  distance = diff;
                  nearbySortl = mapping.getSortl();
                  nearbyPostalCdRange = postalCodeRange;
                }
              }
            }
          } else {
            response.put(MATCHING, "Exact Match");
            response.put(SORTL, mapping.getSortl());
            response.put(POSTAL_CD_RANGE, "- No Postal Code Range Defined -");
            return response;
          }
        }
      }
      if (StringUtils.isNotBlank(nearbySortl)) {
        response.put(MATCHING, "Nearest Match");
        LOG.debug("SORTL Calculated by near by postal code range logic: " + nearbySortl);
      } else {
        response.put(MATCHING, "No Match Found");
      }
      response.put(SORTL, nearbySortl);
      response.put(POSTAL_CD_RANGE, nearbyPostalCdRange);
      return response;
    } else {
      response.put(MATCHING, "No Match Found");
      return response;
    }
  }

  @Override
  public boolean performCountrySpecificCoverageCalculations(CalculateCoverageElement covElement, EntityManager entityManager,
      AutomationResult<OverrideOutput> results, StringBuilder details, OverrideOutput overrides, RequestData requestData,
      AutomationEngineData engineData, String covFrom, CoverageContainer container, boolean isCoverageCalculated) throws Exception {
    Data data = requestData.getData();
    Addr zs01 = requestData.getAddress("ZS01");
    String coverageId = container.getFinalCoverage();
    details.append("\n");
    if (isCoverageCalculated && StringUtils.isNotBlank(coverageId) && covFrom != null && CalculateCoverageElement.COV_BG.equals(covFrom)) {
      overrides.addOverride(AutomationElementRegistry.GBL_CALC_COV, "DATA", "SEARCH_TERM", data.getSearchTerm(), coverageId);
      details.append("Computed SORTL = " + coverageId).append("\n");
      results.setResults("Coverage Calculated");
      engineData.addPositiveCheckStatus(AutomationEngineData.COVERAGE_CALCULATED);
    } else if ("34".equals(data.getIsuCd()) && "Q".equals(data.getClientTier())) {
      details.setLength(0); // clearing details
      overrides.clearOverrides();
      details.append("Calculating coverage using 34Q-PostalCode logic.").append("\n");
      HashMap<String, String> response = getSORTLFromPostalCodeMapping(data.getSubIndustryCd(), zs01.getPostCd(), data.getIsuCd(),
          data.getClientTier());
      LOG.debug("Calculated SORTL: " + response.get(SORTL));
      if (StringUtils.isNotBlank(response.get(MATCHING))) {
        switch (response.get(MATCHING)) {
        case "Exact Match":
        case "Nearest Match":
          overrides.addOverride(AutomationElementRegistry.GBL_CALC_COV, "DATA", "SEARCH_TERM", data.getSearchTerm(), response.get(SORTL));
          details.append("Coverage calculation Successful.").append("\n");
          details.append("Computed SORTL = " + response.get(SORTL)).append("\n\n");
          details.append("Matched Rule:").append("\n");
          details.append("Sub Industry = " + data.getSubIndustryCd()).append("\n");
          details.append("ISU = " + data.getIsuCd()).append("\n");
          details.append("CTC = " + data.getClientTier()).append("\n");
          details.append("Postal Code Range = " + response.get(POSTAL_CD_RANGE)).append("\n\n");
          details.append("Matching: " + response.get(MATCHING));
          results.setResults("Coverage Calculated");
          engineData.addPositiveCheckStatus(AutomationEngineData.COVERAGE_CALCULATED);
          break;
        case "No Match Found":
          engineData.addRejectionComment("OTH", "Coverage cannot be computed using 32S-PostalCode logic.", "", "");
          details.append("Coverage cannot be computed using 32S-PostalCode logic.").append("\n");
          results.setResults("Coverage not calculated.");
          results.setOnError(true);
          break;
        }
      } else {
        engineData.addRejectionComment("OTH", "Coverage cannot be computed using 32S-PostalCode logic.", "", "");
        details.append("Coverage cannot be computed using 32S-PostalCode logic.").append("\n");
        results.setResults("Coverage not calculated.");
        results.setOnError(true);
      }
    } else {
      details.setLength(0);
      overrides.clearOverrides();
      details.append("Coverage could not be calculated through Buying group or 32S-PostalCode logic.\n Skipping coverage calculation.").append("\n");
      results.setResults("Skipped");
    }
    return true;
  }

  @Override
  public boolean runUpdateChecksForData(EntityManager entityManager, AutomationEngineData engineData, RequestData requestData,
      RequestChangeContainer changes, AutomationResult<ValidationOutput> output, ValidationOutput validation) throws Exception {
    Admin admin = requestData.getAdmin();
    Data data = requestData.getData();

    // if (handlePrivatePersonRecord(entityManager, admin, output, validation,
    // engineData)) {
    // return true;
    // }
    String sqlKey = ExternalizedQuery.getSql("AUTO.US.CHECK_CMDE");
    PreparedQuery query = new PreparedQuery(entityManager, sqlKey);
    query.setParameter("EMAIL", admin.getRequesterId());
    query.setForReadOnly(true);
    if (query.exists()) {
      // skip checks if requester is from Canada CMDE team
      admin.setScenarioVerifiedIndc("Y");
      LOG.debug("Requester is from CA CMDE team, skipping update checks.");
      output.setDetails("Requester is from CA CMDE team, skipping update checks.\n");
      validation.setMessage("Skipped");
      validation.setSuccess(true);
    } else {
      StringBuilder details = new StringBuilder();
      boolean cmdeReview = false;
      EntityManager cedpManager = JpaManager.getEntityManager("CEDP");
      List<String> ignoredUpdates = new ArrayList<String>();
      for (UpdatedDataModel change : changes.getDataUpdates()) {
        switch (change.getDataField()) {
        case "VAT #":
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
          break;
        case "Order Block Code":
          if ("94".equals(change.getOldData()) || "94".equals(change.getNewData())) {
            cmdeReview = true;
          }
          break;
        case "ISIC":
        case "Subindustry":
        case "INAC/NAC Code":
          String error = performInacCheck(cedpManager, entityManager, requestData);
          if (StringUtils.isNotBlank(error)) {
            if ("BG_ERROR".equals(error)) {
              cmdeReview = true;
              engineData.addNegativeCheckStatus("_chINACCheckFailed",
                  "The projected global buying group during INAC checks did not match the one on the request.");
              details.append("The projected global buying group during INAC checks did not match the one on the request.\n");
            } else {
              return true;
            }
          }
          String ageError = performCMRNewCheck(cedpManager, entityManager, requestData);
          if (StringUtils.isNotBlank(ageError)) {
            engineData.addNegativeCheckStatus("_chINACCheckFailed", ageError);
            details.append(ageError);
          }
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
    }
    return true;
  }

  @Override
  public boolean runUpdateChecksForAddress(EntityManager entityManager, AutomationEngineData engineData, RequestData requestData,
      RequestChangeContainer changes, AutomationResult<ValidationOutput> output, ValidationOutput validation) throws Exception {
    Admin admin = requestData.getAdmin();
    Data data = requestData.getData();
    // if (handlePrivatePersonRecord(entityManager, admin, output, validation,
    // engineData)) {
    // return true;
    // }

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
              if (addrType.startsWith("ZP")) {
                LOG.debug("Addition of " + addrType + "(" + addr.getId().getAddrSeq() + ")");
                checkDetails.append("Addition of new " + addrType + "(" + addr.getId().getAddrSeq() + ") address skipped in the checks.\n");
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
            if (CmrConstants.RDC_SHIP_TO.equals(addrType) || addrType.startsWith("ZP")) {
              // just proceed for billing and shipping updates
              LOG.debug("Update to " + addrType + "(" + addr.getId().getAddrSeq() + ")");
              checkDetails.append("Updates to (" + addr.getId().getAddrSeq() + ") skipped in the checks.\n");
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

  /**
   * Checks if the address is added on the Update Request
   *
   * @param addr
   * @return
   */
  private boolean isAddressAdded(Addr addr) {
    if (StringUtils.isNotEmpty(addr.getImportInd()) && "N".equals(addr.getImportInd())) {
      return true;
    }
    return false;
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

  private boolean isOnlyDnBRelevantFieldUpdated(RequestChangeContainer changes, String addrTypeCode) {
    boolean isDnBRelevantFieldUpdated = false;
    String[] addressFields = { "Customer Name 1", "Country (Landed)", "Street Address", "Postal Code", "City" };
    List<String> relevantFieldNames = Arrays.asList(addressFields);
    for (String fieldId : relevantFieldNames) {
      UpdatedNameAddrModel addressChange = changes.getAddressChange(addrTypeCode, fieldId);
      if (addressChange != null) {
        isDnBRelevantFieldUpdated = true;
        break;
      }
    }
    return isDnBRelevantFieldUpdated;
  }

  @Override
  public List<String> getSkipChecksRequestTypesforCMDE() {
    return Arrays.asList("C", "U", "M");
  }

  /**
   * Checks to perform if INAC field updated.
   * 
   * @param cedpManager
   * @param entityManager
   * @param requestData
   * @return An error message if validation failed, null if validated.
   * @throws Exception
   */
  private String performInacCheck(EntityManager cedpManager, EntityManager entityManager, RequestData requestData) throws Exception {
    Data data = requestData.getData();
    String error = "The CMR does not fulfill the criteria to be updated in execution cycle, please contact CMDE via Jira to verify possibility of update in Preview cycle.\nLink:- https://jira.data.zc2.ibm.com/servicedesk/customer/portal/14";
    String sql = ExternalizedQuery.getSql("AUTO.CA.GET_CMR_REVENUE");
    PreparedQuery query = new PreparedQuery(cedpManager, sql);
    query.setParameter("CMR_NO", data.getCmrNo());
    query.setForReadOnly(true);
    List<Object[]> results = query.getResults(1);
    if (results != null && results.size() > 0) {
      BigDecimal revenue = new BigDecimal(0);
      if (results.get(0)[1] != null) {
        revenue = (BigDecimal) results.get(0)[1];
      }
      if (revenue.floatValue() > 0) {
        return error + "\n- CMR with revenue";
      } else if (revenue.floatValue() == 0) {
        sql = ExternalizedQuery.getSql("AUTO.CA.INAC_DUNS_CHECK");
        query = new PreparedQuery(entityManager, sql);
        query.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
        query.setParameter("CMR_NO", data.getCmrNo());
        query.setParameter("INAC", data.getInacCd());
        query.setForReadOnly(true);
        results = query.getResults(1);
        if (results != null && !results.isEmpty()) {
          // String guDunsNo = (String) results.get(0)[0];
          String gbgIdDb = (String) results.get(0)[1];

          CmrClientService odmService = new CmrClientService();
          RequestEntryModel model = requestData.createModelFromRequest();
          Addr soldTo = requestData.getAddress("ZS01");
          ModelMap response = new ModelMap();

          odmService.getBuyingGroup(entityManager, soldTo, model, response);
          String gbgId = (String) response.get("globalBuyingGroupID");
          if (StringUtils.isBlank(gbgId)) {
            gbgId = gbgIdDb;
          }

          if (StringUtils.isBlank(gbgId) || (StringUtils.isNotBlank(gbgId) && !gbgId.equals(data.getGbgId()))) {
            return "BG_ERROR";
          }

        } else {
          return error + "\n- Target INAC is not under the same GU DUNs/parent";
        }
      }
    }
    return null;
  }

  /**
   * Checks to perform if CMR is new (age within 30 days)
   * 
   * @param cedpManager
   * @param entityManager
   * @param requestData
   * @return An error message if validation failed, null if validated.
   * @throws Exception
   */
  private String performCMRNewCheck(EntityManager cedpManager, EntityManager entityManager, RequestData requestData) throws Exception {
    Data data = requestData.getData();
    String error = "The CMR does not fulfill the criteria to be updated in execution cycle, please contact CMDE via Jira to verify possibility of update in Preview cycle.\nLink:- https://jira.data.zc2.ibm.com/servicedesk/customer/portal/14";
    String sql = ExternalizedQuery.getSql("AUTO.CA.CHECK_CMR_NEW");
    PreparedQuery query = new PreparedQuery(cedpManager, sql);
    query.setParameter("CMR_NO", data.getCmrNo());
    query.setForReadOnly(true);
    List<Object[]> results = query.getResults(1);
    if (results != null && results.size() > 0) {
      String creationCapChanged = (String) results.get(0)[2];
      if (!"Ok".equals(creationCapChanged)) {
        if (!"Ok".equals(creationCapChanged)) {
          error += "\n- Not new CMR (for CMR pass the 30 days period)";
        }
        return error;
      }
    }
    return null;
  }

}
