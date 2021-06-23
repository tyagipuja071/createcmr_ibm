package com.ibm.cio.cmr.request.automation.util.geo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

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
    Addr soldTo = requestData.getAddress("ZS01");
    String details = StringUtils.isNotBlank(output.getDetails()) ? output.getDetails() : "";
    StringBuilder detail = new StringBuilder(details);
    String duns = null;
    boolean isNegativeCheckNeedeed = false;
    if (changes != null && changes.hasDataChanges()) {
      if (changes.isDataChanged("VAT #")) {
        UpdatedDataModel vatChange = changes.getDataChange("VAT #");
        if (vatChange != null) {
          if ((StringUtils.isBlank(vatChange.getOldData()) && StringUtils.isNotBlank(vatChange.getNewData()))
              || (StringUtils.isNotBlank(vatChange.getOldData()) && StringUtils.isNotBlank(vatChange.getNewData()))) {
            // check if the name + VAT exists in D&B
            List<DnBMatchingResponse> matches = getMatches(requestData, engineData, soldTo, true);
            if (matches.isEmpty()) {
              // get DnB matches based on all address details
              matches = getMatches(requestData, engineData, soldTo, false);
            }
            String custName = soldTo.getCustNm1() + (StringUtils.isBlank(soldTo.getCustNm2()) ? "" : " " + soldTo.getCustNm2());
            if (!matches.isEmpty()) {
              for (DnBMatchingResponse dnbRecord : matches) {
                if ("Y".equals(dnbRecord.getOrgIdMatch()) && (StringUtils.isNotEmpty(custName) && StringUtils.isNotEmpty((dnbRecord.getDnbName()))
                    && StringUtils.getLevenshteinDistance(custName.toUpperCase(), dnbRecord.getDnbName().toUpperCase()) <= 5)) {
                  duns = dnbRecord.getDunsNo();
                  isNegativeCheckNeedeed = false;
                  break;
                }
                isNegativeCheckNeedeed = true;
              }
            }
            if (isNegativeCheckNeedeed) {
              detail.append("Updates to VAT need verification as VAT and legal name doesn't matches DnB.\n");
              LOG.debug("Updates to VAT need verification as VAT and legal name doesn't matches DnB.");
            } else {
              detail.append("Updates to VAT matches DnB.\n DUNS No :" + duns + "\n");
              LOG.debug("Updates to VAT matches DnB.\n DUNS No :" + duns + "\n");
            }

          } else if (StringUtils.isNotBlank(vatChange.getOldData()) && StringUtils.isBlank(vatChange.getNewData())) {
            admin.setScenarioVerifiedIndc("N");
            entityManager.merge(admin);
            detail.append("Setting scenario verified indc= N as VAT is blank.\n");
            LOG.debug("Setting scenario verified indc= N as VAT is blank.");
          } else if (StringUtils.isNotBlank(vatChange.getOldData()) && StringUtils.isNotBlank(vatChange.getNewData())) {
            isNegativeCheckNeedeed = true;
            detail.append("Updates to VAT need verification.\n");
            LOG.debug("Updates to VAT need verification.");
          }

        }
      } else {
        boolean otherFieldsChanged = false;
        for (UpdatedDataModel dataChange : changes.getDataUpdates()) {
          if (dataChange != null && !"CAP Record".equals(dataChange.getDataField())) {
            otherFieldsChanged = true;
            break;
          }
        }
        if (otherFieldsChanged) {
          isNegativeCheckNeedeed = true;
          detail.append("Updates to data were found, review is required.\n");
          LOG.debug("Updates to data were found, review is required.");
        }
      }

    }
    if (isNegativeCheckNeedeed) {
      validation.setSuccess(false);
      validation.setMessage("Not validated");
      engineData.addNegativeCheckStatus("UPDT_REVIEW_NEEDED", "Updated elements cannot be checked automatically.");

    } else {
      validation.setSuccess(true);
      validation.setMessage("Validated");
      if (detail.toString().isEmpty()) {
        detail.append("No data updates made on the request");
      }
    }
    output.setDetails(detail.toString());
    output.setProcessOutput(validation);
    return true;
  }

  @Override
  public boolean runUpdateChecksForAddress(EntityManager entityManager, AutomationEngineData engineData, RequestData requestData,
      RequestChangeContainer changes, AutomationResult<ValidationOutput> output, ValidationOutput validation) throws Exception {
    Data data = requestData.getData();
    Admin admin = requestData.getAdmin();
    List<Addr> addressList = requestData.getAddresses();
    boolean isInstallAtMatchesDnb = true;
    boolean isBillToMatchesDnb = true;
    boolean isNegativeCheckNeedeed = false;
    boolean isInstallAtExistOnReq = false;
    boolean isBillToExistOnReq = false;
    boolean isShipToExistOnReq = false;
    Addr installAt = requestData.getAddress("ZI01");
    Addr billTo = requestData.getAddress("ZP01");
    Addr shipTo = requestData.getAddress("ZD01");
    String details = StringUtils.isNotBlank(output.getDetails()) ? output.getDetails() : "";
    StringBuilder detail = new StringBuilder(details);
    long reqId = requestData.getAdmin().getId().getReqId();
    if (changes != null && changes.hasAddressChanges()) {
      if (StringUtils.isNotEmpty(data.getCustClass()) && ("81".equals(data.getCustClass()) || "85".equals(data.getCustClass()))
          && !changes.hasDataChanges()) {
        LOG.debug("Skipping checks as customer class is " + data.getCustClass() + " and only address changes.");
        output.setDetails("Skipping checks as customer class is " + data.getCustClass() + " and only address changes.");
        validation.setSuccess(true);
        validation.setMessage("No Validations");
        output.setProcessOutput(validation);
        return true;
      }

      if (shipTo != null && (changes.isAddressChanged("ZD01") || isAddressAdded(shipTo))) {
        // Check If Address already exists on request
        isShipToExistOnReq = addressExists(entityManager, shipTo);
        if (isShipToExistOnReq) {
          detail.append("Ship To details provided matches an existing address.");
          validation.setMessage("ShipTo already exists");
          engineData.addRejectionComment("OTH", "Ship To details provided matches an existing address.", "", "");
          output.setOnError(true);
          validation.setSuccess(false);
          validation.setMessage("Not validated");
          output.setDetails(detail.toString());
          output.setProcessOutput(validation);
          LOG.debug("Ship To details provided matches already existing address.");
          return true;
        }
      }

      if (installAt != null && (changes.isAddressChanged("ZI01") || isAddressAdded(installAt))) {
        // Check If Address already exists on request
        isInstallAtExistOnReq = addressExists(entityManager, installAt);
        if (isInstallAtExistOnReq) {
          detail.append("Install At details provided matches an existing address.");
          engineData.addRejectionComment("OTH", "Install At details provided matches an existing address.", "", "");
          LOG.debug("Install At details provided matches an existing address.");
          output.setOnError(true);
          validation.setSuccess(false);
          validation.setMessage("Not validated");
          output.setDetails(detail.toString());
          output.setProcessOutput(validation);
          return true;
        }
        if ((changes.isAddressChanged("ZI01") && isOnlyDnBRelevantFieldUpdated(changes, "ZI01")) || isAddressAdded(installAt)) {
          // Check if address closely matches DnB
          List<DnBMatchingResponse> matches = getMatches(requestData, engineData, installAt, false);
          if (matches != null) {
            isInstallAtMatchesDnb = ifaddressCloselyMatchesDnb(matches, installAt, admin, data.getCmrIssuingCntry());
          }
          if (!isInstallAtMatchesDnb) {
            isNegativeCheckNeedeed = true;
            detail.append("Updates to Install At address need verification as it does not matches D&B");
            LOG.debug("Updates to Install At address need verification as it does not matches D&B");
          }
        }
      }

      if (billTo != null && (changes.isAddressChanged("ZP01") || isAddressAdded(billTo))) {
        // Check If Address already exists on request
        isBillToExistOnReq = addressExists(entityManager, billTo);
        if (isBillToExistOnReq) {
          detail.append("Bill To details provided matches an existing address.");
          engineData.addRejectionComment("OTH", "Bill To details provided matches an existing address.", "", "");
          LOG.debug("Bill To details provided matches an existing address.");
          output.setOnError(true);
          validation.setSuccess(false);
          validation.setMessage("Not validated");
          output.setDetails(detail.toString());
          output.setProcessOutput(validation);
          return true;
        }

        if ((changes.isAddressChanged("ZP01") && isOnlyDnBRelevantFieldUpdated(changes, "ZP01")) || isAddressAdded(billTo)) {
          // Check if address closely matches DnB
          List<DnBMatchingResponse> matches = getMatches(requestData, engineData, billTo, false);
          if (matches != null) {
            isBillToMatchesDnb = ifaddressCloselyMatchesDnb(matches, billTo, admin, data.getCmrIssuingCntry());
            if (!isBillToMatchesDnb) {
              isNegativeCheckNeedeed = true;
              detail.append("Updates to Bill To address need verification as it does not matches D&B");
              LOG.debug("Updates to Bill To address need verification as it does not matches D&B");
            }
          }
        }
      }

      if (isNegativeCheckNeedeed || isShipToExistOnReq || isInstallAtExistOnReq || isBillToExistOnReq) {
        validation.setSuccess(false);
        validation.setMessage("Not validated");
        engineData.addNegativeCheckStatus("UPDT_REVIEW_NEEDED", "Updated elements cannot be checked automatically.");
      } else {
        for (Addr addr : addressList) {
          if ("Y".equals(addr.getImportInd())) {
            if (!isRelevantAddressFieldUpdated(changes, addr)) {
              validation.setSuccess(true);
              LOG.debug("Updates to relevant addresses fields is found.Updates verified.");
              detail.append("Updates to relevant addresses found but have been marked as Verified.");
              validation.setMessage("Validated");
              isNegativeCheckNeedeed = false;
              break;
            } else if (isRelevantAddressFieldUpdated(changes, addr)) {
              isNegativeCheckNeedeed = true;
            }
          }
        }

        if (isNegativeCheckNeedeed) {
          detail.append("Updates to addresses found which cannot be checked automatically.");
          LOG.debug("Updates to addresses found which cannot be checked automatically.");
          validation.setSuccess(false);
          validation.setMessage("Not validated");
          engineData.addNegativeCheckStatus("UPDT_REVIEW_NEEDED", "Updated elements cannot be checked automatically.");
        } else {
          LOG.debug("Address changes don't need review");
          if (changes.hasAddressChanges()) {
            validation.setMessage("Address changes were found. No further review required.");
            detail.append("Address changes were found. No further review required.");
          } else {
            validation.setMessage("No Address changes found on the request.");
            detail.append("No Address changes found on the request.");
          }
          if (StringUtils.isBlank(output.getResults())) {
            output.setResults("Validated");
          }
          validation.setSuccess(true);
        }

      }

    }
    output.setDetails(detail.toString());
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

}
