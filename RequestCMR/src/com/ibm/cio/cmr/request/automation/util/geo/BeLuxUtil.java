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
import com.ibm.cio.cmr.request.automation.util.ScenarioExceptionsUtil;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.model.window.UpdatedDataModel;
import com.ibm.cio.cmr.request.model.window.UpdatedNameAddrModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.util.SystemParameters;
import com.ibm.cmr.services.client.matching.dnb.DnBMatchingResponse;

public class BeLuxUtil extends AutomationUtil {

  private static final Logger LOG = Logger.getLogger(BeLuxUtil.class);
  public static final String SCENARIO_LOCAL_COMMERCIAL = "BECOM";
  public static final String SCENARIO_CROSS_COMMERCIAL = "CBCOM";
  public static final String SCENARIO_LOCAL_PUBLIC = "BEPUB";
  public static final String SCENARIO_BP_LOCAL = "BEBUS";
  public static final String SCENARIO_BP_CROSS = "CBBUS";
  public static final String SCENARIO_PRIVATE_CUSTOMER = "BEPRI";
  public static final String SCENARIO_THIRD_PARTY = "BE3PA";
  public static final String SCENARIO_INTERNAL = "BEINT";
  public static final String SCENARIO_INTERNAL_SO = "BEISO";
  public static final String SCENARIO_DATA_CENTER = "BEDAT";

  // Lux
  public static final String SCENARIO_CROSS_LU = "LUCRO";
  public static final String SCENARIO_LOCAL_PUBLIC_LU = "LUPUB";
  public static final String SCENARIO_PRIVATE_CUSTOMER_LU = "LUPRI";
  public static final String SCENARIO_LOCAL_COMMERCIAL_LU = "LUCOM";
  public static final String SCENARIO_INTERNAL_LU = "LUINT";
  public static final String SCENARIO_INTERNAL_SO_LU = "LUISO";
  public static final String SCENARIO_THIRD_PARTY_LU = "LU3PA";
  public static final String SCENARIO_BP_LOCAL_LU = "LUBUS";
  public static final String SCENARIO_DATA_CENTER_LU = "LUDAT";

  private static final List<String> RELEVANT_ADDRESSES = Arrays.asList(CmrConstants.RDC_SOLD_TO, CmrConstants.RDC_BILL_TO,
      CmrConstants.RDC_INSTALL_AT, CmrConstants.RDC_SHIP_TO, CmrConstants.RDC_SECONDARY_SOLD_TO);
  private static final List<String> NON_RELEVANT_ADDRESS_FIELDS = Arrays.asList("Attention Person", "Phone #");

  @Override
  public boolean performScenarioValidation(EntityManager entityManager, RequestData requestData, AutomationEngineData engineData,
      AutomationResult<ValidationOutput> result, StringBuilder details, ValidationOutput output) {

    ScenarioExceptionsUtil scenarioExceptions = (ScenarioExceptionsUtil) engineData.get("SCENARIO_EXCEPTIONS");
    Data data = requestData.getData();
    String scenario = data.getCustSubGrp();
    Addr zs01 = requestData.getAddress("ZS01");
    Addr zi01 = requestData.getAddress("ZI01");
    String customerName = zs01.getCustNm1();
    Addr zp01 = requestData.getAddress("ZP01");
    String customerNameZP01 = "";
    String landedCountryZP01 = "";
    if (zp01 != null) {
      customerNameZP01 = StringUtils.isBlank(zp01.getCustNm1()) ? "" : zp01.getCustNm1();
      landedCountryZP01 = StringUtils.isBlank(zp01.getLandCntry()) ? "" : zp01.getLandCntry();
    }
    if (!StringUtils.equals(zs01.getLandCntry(), zp01.getLandCntry())) {
      scenarioExceptions.setCheckVATForDnB(false);
    }

    if ((SCENARIO_BP_LOCAL.equals(scenario) || SCENARIO_BP_CROSS.equals(scenario) || SCENARIO_BP_LOCAL_LU.equals(scenario)) && zp01 != null
        && (!StringUtils.equals(getCleanString(customerName), getCleanString(customerNameZP01))
            || !StringUtils.equals(zs01.getLandCntry(), landedCountryZP01))) {
      details.append("Customer Name and Landed Country on Sold-to and Bill-to address should be same for BP Scenario.").append("\n");
      engineData.addNegativeCheckStatus("SOLDTO_BILLTO_DIFF",
          "Customer Name and Landed Country on Sold-to and Bill-to address should be same for BP Scenario.");
    }

    switch (scenario) {

    case SCENARIO_LOCAL_COMMERCIAL:
    case SCENARIO_CROSS_COMMERCIAL:
    case SCENARIO_LOCAL_COMMERCIAL_LU:
    case SCENARIO_LOCAL_PUBLIC:
    case SCENARIO_LOCAL_PUBLIC_LU:
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
    case SCENARIO_BP_LOCAL_LU:
      return doBusinessPartnerChecks(engineData, data.getPpsceid(), details);

    case SCENARIO_INTERNAL_SO:
    case SCENARIO_INTERNAL_SO_LU:
      if (zi01 == null) {
        details.append("Install-at address should be present for Interna SO Scenario.").append("\n");
        engineData.addRejectionComment("OTH", "Install-at address should be present for Internal SO Scenario.", "", "");
        return false;
      }
      break;
    case SCENARIO_PRIVATE_CUSTOMER:
    case SCENARIO_PRIVATE_CUSTOMER_LU:
      return doPrivatePersonChecks(engineData, data.getCmrIssuingCntry(), zs01.getLandCntry(), customerName, details, false, requestData);

    case SCENARIO_THIRD_PARTY:
    case SCENARIO_THIRD_PARTY_LU:
    case SCENARIO_DATA_CENTER:
    case SCENARIO_DATA_CENTER_LU:
      details.append("Processor Review will be required for Third Party Scenario/Data Center.\n");
      engineData.addNegativeCheckStatus("Scenario_Validation", "3rd Party/Data Center request will require CMDE review before proceeding.\n");
      break;
    }

    return true;
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
    String coverageId = container.getFinalCoverage();

    if (isCoverageCalculated && StringUtils.isNotBlank(coverageId) && CalculateCoverageElement.COV_BG.equals(covFrom)) {
      engineData.addPositiveCheckStatus(AutomationEngineData.COVERAGE_CALCULATED);
    } else if (!isCoverageCalculated) {
      // if not calculated using bg/gbg try calculation using 32/S logic
      details.setLength(0);// clear string builder
      overrides.clearOverrides(); // clear existing overrides

      BeLuxFieldsContainer fields = calculate32SValuesFromIMSBeLux(entityManager, data.getCmrIssuingCntry(), data.getCountryUse(),
          data.getSubIndustryCd(), data.getIsuCd(), data.getClientTier());
      if (fields != null) {
        details.append("Coverage calculated successfully using 32S logic.").append("\n");
        details.append("Sales Rep : " + fields.getSalesRep()).append("\n");
        overrides.addOverride(AutomationElementRegistry.GBL_CALC_COV, "DATA", "SEARCH_TERM", data.getSearchTerm(), fields.getSalesRep());
        if (StringUtils.isNotBlank(fields.getSalesRep())) {
          String sbo = "";
          if (data.getCountryUse().equalsIgnoreCase("624")) {
            sbo = "0" + fields.getSalesRep().substring(0, 2) + "0000";
          } else if (data.getCountryUse().equalsIgnoreCase("624LU")) {
            sbo = "0" + fields.getSalesRep().substring(0, 2) + "0001";
          }
          details.append("SBO : " + sbo).append("\n");
          overrides.addOverride(AutomationElementRegistry.GBL_CALC_COV, "DATA", "SALES_BO_CD", data.getSalesBusOffCd(), sbo);
        }
        results.setResults("Calculated");
        results.setDetails(details.toString());
      } else if (StringUtils.isNotBlank(data.getSearchTerm()) && StringUtils.isNotBlank(data.getSalesBusOffCd())) {
        details.append("Coverage could not be calculated using 32S logic. Using values from request").append("\n");
        details.append("Sales Rep : " + data.getRepTeamMemberNo()).append("\n");
        details.append("SBO : " + data.getSalesBusOffCd()).append("\n");
        results.setResults("Calculated");
        results.setDetails(details.toString());
      } else {
        String msg = "Coverage cannot be calculated. No valid 32S mapping found from request data.";
        details.append(msg);
        results.setResults("Cannot Calculate");
        results.setDetails(details.toString());
        engineData.addNegativeCheckStatus("_beluxCoverage", msg);
      }
    }
    return true;
  }

  private BeLuxFieldsContainer calculate32SValuesFromIMSBeLux(EntityManager entityManager, String cmrIssuingctry, String countryUse,
      String subIndustryCd, String isuCd, String clientTier) {
    BeLuxFieldsContainer container = new BeLuxFieldsContainer();
    List<Object[]> salesRepRes = new ArrayList<>();
    String isuCtc = (StringUtils.isNotBlank(isuCd) ? isuCd : "") + (StringUtils.isNotBlank(clientTier) ? clientTier : "");
    String geoCd = (StringUtils.isNotBlank(countryUse.substring(3, 5)) ? countryUse.substring(3, 5) : "");
    String cmrCntry = cmrIssuingctry + geoCd;
    if (StringUtils.isNotBlank(subIndustryCd) && ("32S".equals(isuCtc))) {
      String ims = subIndustryCd.substring(0, 1);
      String sql = ExternalizedQuery.getSql("QUERY.GET.SRLIST.BYISUCTC");
      PreparedQuery query = new PreparedQuery(entityManager, sql);
      query.setParameter("ISU", "%" + isuCtc + "%");
      query.setParameter("ISSUING_CNTRY", cmrCntry);
      query.setParameter("CLIENT_TIER", "%" + ims + "%");
      query.setForReadOnly(true);
      salesRepRes = query.getResults();
    } else {
      String sql = ExternalizedQuery.getSql("QUERY.GET.SRLIST.BYISU");
      PreparedQuery query = new PreparedQuery(entityManager, sql);
      query.setParameter("ISU", "%" + isuCtc + "%");
      query.setParameter("ISSUING_CNTRY", cmrCntry);
      salesRepRes = query.getResults();
    }
    if (salesRepRes != null && salesRepRes.size() == 1) {
      String salesRep = (String) salesRepRes.get(0)[0];
      container.setSalesRep(salesRep);
      return container;
    } else {
      return null;
    }

  }

  private class BeLuxFieldsContainer {
    private String salesRep;

    public String getSalesRep() {
      return salesRep;
    }

    public void setSalesRep(String salesRep) {
      this.salesRep = salesRep;
    }

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
            engineData.addNegativeCheckStatus("_beluxVATCheckFailed", "VAT # on the request did not match D&B");
            details.append("VAT # on the request did not match D&B\n");
          } else {
            details.append("VAT # on the request matches D&B\n");
          }
        }

        if (!StringUtils.isBlank(change.getOldData()) && !StringUtils.isBlank(change.getNewData())
            && !(change.getOldData().equals(change.getNewData()))) {
          // UPDATE
          Addr soldTo = requestData.getAddress(CmrConstants.RDC_SOLD_TO);
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
        }

        break;
      case "ISIC":
      case "INAC/NAC Code":
        cmdeReview = true;
        break;
      case "Economic Code":
        String newEcoCd = change.getNewData();
        String oldEcoCd = change.getOldData();
        String regUser = SystemParameters.getString("BELUX_ECO_CD_UPDT");
        String currentUser = admin.getRequesterId();
        if (StringUtils.isNotBlank(newEcoCd) && StringUtils.isNotBlank(oldEcoCd) && newEcoCd.length() == 3 && oldEcoCd.length() == 3) {
          if (!newEcoCd.substring(0, 1).equals(oldEcoCd.substring(0, 1))
              && newEcoCd.substring(1, newEcoCd.length()).equals(oldEcoCd.substring(1, oldEcoCd.length())) && currentUser.equalsIgnoreCase(regUser)) {
            details.append("Economic Code has been updated by registered user.\n");
          } else {
            cmdeReview = true;
            engineData.addNegativeCheckStatus("_beluxEconomicCdUpdt", "Economic code was updated incorrectly or by non registered user.");
            details.append("Economic code was updated incorrectly or by non registered user.\n");
          }
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
    if (cmdeReview) {
      engineData.addNegativeCheckStatus("_beluxDataCheckFailed", "Updates to one or more data fields cannot be validated.");
      details.append("Updates to one or more data fields cannot be validated.\n");
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
    StringBuilder duplicateDetails = new StringBuilder();
    StringBuilder nonRelAddrFdsDetails = new StringBuilder();
    List<Addr> addresses = null;
    int ignoredAddr = 0;
    StringBuilder checkDetails = new StringBuilder();
    Set<String> resultCodes = new HashSet<String>();// R - review
    for (String addrType : RELEVANT_ADDRESSES) {
      addresses = requestData.getAddresses(addrType);
      if (changes.isAddressChanged(addrType)) {
        for (Addr addr : addresses) {
          if (isRelevantAddressFieldUpdated(changes, addr, nonRelAddrFdsDetails)) {

            if ((addrType.equalsIgnoreCase(CmrConstants.RDC_SOLD_TO) && "Y".equals(addr.getImportInd()))
                || addrType.equalsIgnoreCase(CmrConstants.RDC_BILL_TO) || addrType.equalsIgnoreCase(CmrConstants.RDC_SECONDARY_SOLD_TO)) {

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

            String soldToCustNm1 = requestData.getAddress("ZS01").getCustNm1();
            String installAtCustNm = requestData.getAddress("ZI01") != null ? requestData.getAddress("ZI01").getCustNm1() : "";
            if ((addrType.equalsIgnoreCase(CmrConstants.RDC_INSTALL_AT) && soldToCustNm1.equalsIgnoreCase(installAtCustNm))
                || (addrType.equalsIgnoreCase(CmrConstants.RDC_SHIP_TO) && "N".equals(addr.getImportInd()))) {
              LOG.debug("Checking duplicates for " + addrType + "(" + addr.getId().getAddrSeq() + ")");
              boolean duplicate = addressExists(entityManager, addr);
              if (duplicate) {
                LOG.debug(" - Duplicates found for " + addrType + "(" + addr.getId().getAddrSeq() + ")");
                duplicateDetails.append("Address " + addrType + "(" + addr.getId().getAddrSeq() + ") provided matches an existing address.\n");
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
      validation.setSuccess(false);
      validation.setMessage("Rejected.");
      engineData.addNegativeCheckStatus("_esCheckFailed", "Updated elements cannot be checked automatically.");
    } else {
      validation.setSuccess(true);
      validation.setMessage("Successful");
    }

    if (ignoredAddr > 0) {
      checkDetails.append("Updates to imported Address Ship-To(ZD01) ignored. ");
    }
    String details = (output.getDetails() != null && output.getDetails().length() > 0) ? output.getDetails() : "";
    details += duplicateDetails.length() > 0 ? duplicateDetails.toString() : "";
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
