/**
 * 
 */
package com.ibm.cio.cmr.request.automation.util.geo;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;

import org.apache.commons.digester.Digester;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;

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
import com.ibm.cio.cmr.request.automation.util.geo.mappings.ChMubotyMapping;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.model.window.UpdatedDataModel;
import com.ibm.cio.cmr.request.model.window.UpdatedNameAddrModel;
import com.ibm.cio.cmr.request.util.SystemLocation;
import com.ibm.cmr.services.client.matching.dnb.DnBMatchingResponse;

import edu.emory.mathcs.backport.java.util.Collections;

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
      CmrConstants.RDC_INSTALL_AT, CmrConstants.RDC_SHIP_TO);

  private static final List<String> NON_RELEVANT_ADDRESS_FIELDS = Arrays.asList("Att. Person", "Phone #", "FAX", "Customer Name 4");

  private static List<ChMubotyMapping> mubotyMappings = new ArrayList<ChMubotyMapping>();

  @SuppressWarnings("unchecked")
  public SwitzerlandUtil() {
    if (SwitzerlandUtil.mubotyMappings.isEmpty()) {
      Digester digester = new Digester();
      digester.setValidating(false);
      digester.addObjectCreate("mappings", ArrayList.class);

      digester.addObjectCreate("mappings/mapping", ChMubotyMapping.class);

      digester.addBeanPropertySetter("mappings/mapping/ims", "ims");
      digester.addBeanPropertySetter("mappings/mapping/postalCdMin", "postalCdMin");
      digester.addBeanPropertySetter("mappings/mapping/postalCdMax", "postalCdMax");
      digester.addBeanPropertySetter("mappings/mapping/isu", "isu");
      digester.addBeanPropertySetter("mappings/mapping/ctc", "ctc");
      digester.addBeanPropertySetter("mappings/mapping/muboty", "muboty");
      digester.addSetNext("mappings/mapping", "add");
      try {
        ClassLoader loader = GermanyUtil.class.getClassLoader();
        InputStream is = loader.getResourceAsStream("ch-muboty-mapping.xml");
        SwitzerlandUtil.mubotyMappings = (ArrayList<ChMubotyMapping>) digester.parse(is);
        // test
        ChMubotyMapping mapping = getMubotyFromMapping("C", "1000", "32", "S");
        LOG.debug(new ObjectMapper().writeValueAsString(mapping));
      } catch (Exception e) {
        LOG.error("Error occured while digesting xml.", e);
      }
    }
  }

  @Override
  public boolean performScenarioValidation(EntityManager entityManager, RequestData requestData, AutomationEngineData engineData,
      AutomationResult<ValidationOutput> result, StringBuilder details, ValidationOutput output) {
    Data data = requestData.getData();
    Addr soldTo = requestData.getAddress("ZS01");
    String scenario = data.getCustSubGrp();
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
      case SCENARIO_IBM_EMPLOYEE:
        return doPrivatePersonChecks(engineData, SystemLocation.SWITZERLAND, soldTo.getLandCntry(), customerName, details,
            SCENARIO_IBM_EMPLOYEE.equals(actualScenario));
      case SCENARIO_INTERNAL:
        break;
      case SCENARIO_GOVERNMENT:
        break;
      case SCENARIO_THIRD_PARTY:
        engineData.addNegativeCheckStatus("_chThirdParty", "Third Party/Data Center request needs further validation.");
        details.append("Third Party/Data Center request needs further validation.").append("\n");
        break;
      case SCENARIO_BUSINESS_PARTNER:
        return doBusinessPartnerChecks(engineData, data.getPpsceid(), details);
      }
    }

    return true;
  }

  @Override
  protected List<String> getCountryLegalEndings() {
    return Arrays.asList("GMBH", "KLG", "AG", "Sàrl", "SARL", "SA", "S.A.", "SAGL");
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
              if (CmrConstants.RDC_SHIP_TO.equals(addrType)) {
                LOG.debug("Addition of " + addrType + "(" + addr.getId().getAddrSeq() + ")");
                checkDetails.append("Addition of new ZD01 (" + addr.getId().getAddrSeq() + ") address skipped in the checks.\n");
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
      engineData.addRejectionComment("_chDupAddr", "One or more new addresses matches existing addresses on record.", "", "");
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
      case "MUBOTY(SORTL)":
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

    ChMubotyMapping muboty = null;
    switch (actualScenario) {
    case SCENARIO_COMMERCIAL:
      if (!isCoverageCalculated) {
        muboty = getMubotyFromMapping(data.getSubIndustryCd(), soldTo.getPostCd(), data.getIsuCd(), data.getClientTier());
      }
      break;
    case SCENARIO_PRIVATE_CUSTOMER:
    case SCENARIO_IBM_EMPLOYEE:
      muboty = getMubotyFromMapping(data.getSubIndustryCd(), soldTo.getPostCd(), data.getIsuCd(), data.getClientTier());
      break;
    default:
      if ("32".equals(data.getIsuCd()) && ("S".equals(data.getClientTier()) || "N".equals(data.getClientTier())) && !isCoverageCalculated) {
        if (SCENARIO_CROSS_BORDER.equals(scenario)) {
          // verified all logic is based on 3000-9999 condition for crossborders
          muboty = getMubotyFromMapping(data.getSubIndustryCd(), "3000", data.getIsuCd(), data.getClientTier());
        } else {
          muboty = getMubotyFromMapping(data.getSubIndustryCd(), soldTo.getPostCd(), data.getIsuCd(), data.getClientTier());
        }
      }
    }

    if (muboty != null) {
      details.append("Setting MUBOTY to " + muboty.getMuboty() + " based on Postal Code rules.");
      overrides.addOverride(covElement.getProcessCode(), "DATA", "SEARCH_TERM", data.getSearchTerm(), muboty.getMuboty());
      engineData.addPositiveCheckStatus(AutomationEngineData.COVERAGE_CALCULATED);
      results.setResults("Calculated");
    } else if (!isCoverageCalculated) {
      String sortl = data.getSearchTerm();
      if (!StringUtils.isBlank(sortl)) {
        String msg = "No valid MUBOTY mapping from request data. Using MUBOTY " + sortl + " from request.";
        details.append(msg);
        results.setResults("Calculated");
        results.setDetails(details.toString());
      } else {
        String msg = "Coverage cannot be calculated. No valid MUBOTY mapping from request data.";
        details.append(msg);
        results.setResults("Cannot Calculate");
        results.setDetails(details.toString());
        engineData.addNegativeCheckStatus("_chMuboty", msg);
      }
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

  public ChMubotyMapping getMubotyFromMapping(String subIndustryCd, String postCd, String isuCd, String clientTier) {
    if (!mubotyMappings.isEmpty()) {
      int postalCd = Integer.parseInt(postCd);
      if (StringUtils.isNotBlank(subIndustryCd) && subIndustryCd.length() > 1) {
        subIndustryCd = subIndustryCd.substring(0, 1);
      }
      for (ChMubotyMapping mapping : mubotyMappings) {
        List<String> subIndustryCds = Arrays.asList(mapping.getIms().split(","));
        if (StringUtils.isNotBlank(subIndustryCd) && subIndustryCds.contains(subIndustryCd) && isuCd.equals(mapping.getIsu())
            && clientTier.equals(mapping.getCtc())) {
          if (StringUtils.isNotBlank(mapping.getPostalCdMin()) && StringUtils.isNotBlank(mapping.getPostalCdMax())) {
            int start = Integer.parseInt(mapping.getPostalCdMin());
            int end = Integer.parseInt(mapping.getPostalCdMax());
            if (postalCd >= start && postalCd <= end) {
              return mapping;
            }
          }
        }
      }
    }
    return null;
  }

}
