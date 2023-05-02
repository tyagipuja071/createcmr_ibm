package com.ibm.cio.cmr.request.automation.util.geo;

import java.util.ArrayList;
import java.util.Arrays;
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
import com.ibm.cio.cmr.request.automation.util.RequestChangeContainer;
import com.ibm.cio.cmr.request.automation.util.ScenarioExceptionsUtil;
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
import com.ibm.cio.cmr.request.util.Person;
import com.ibm.cio.cmr.request.util.RequestUtils;
import com.ibm.cio.cmr.request.util.SystemLocation;
import com.ibm.cmr.services.client.matching.dnb.DnBMatchingResponse;

public class NetherlandsUtil extends AutomationUtil {

  private static final Logger LOG = Logger.getLogger(NetherlandsUtil.class);
  public static final String SCENARIO_LOCAL_COMMERCIAL = "COMME";
  public static final String SCENARIO_CROSS_COMMERCIAL = "CBCOM";
  public static final String SCENARIO_LOCAL_PUBLIC = "PUBCU";
  public static final String SCENARIO_BP_LOCAL = "BUSPR";
  public static final String SCENARIO_BP_CROSS = "CBBUS";
  public static final String SCENARIO_PRIVATE_CUSTOMER = "PRICU";
  public static final String SCENARIO_INTERNAL = "INTER";
  public static final String SCENARIO_IBM_EMPLOYEE = "IBMEM";
  private static final String QUERY_BG_SBO_BENELUX = "AUTO.COV.GET_COV_FROM_BG_ES_UK";

  private static final List<String> RELEVANT_ADDRESSES = Arrays.asList(CmrConstants.RDC_SOLD_TO, CmrConstants.RDC_BILL_TO,
      CmrConstants.RDC_INSTALL_AT, CmrConstants.RDC_SHIP_TO, CmrConstants.RDC_PAYGO_BILLING);
  private static final List<String> NON_RELEVANT_ADDRESS_FIELDS = Arrays.asList("Attention Person", "Phone #", "Collection Code");

  @Override
  public boolean performScenarioValidation(EntityManager entityManager, RequestData requestData, AutomationEngineData engineData,
      AutomationResult<ValidationOutput> result, StringBuilder details, ValidationOutput output) {

    ScenarioExceptionsUtil scenarioExceptions = (ScenarioExceptionsUtil) engineData.get("SCENARIO_EXCEPTIONS");
    Data data = requestData.getData();
    String scenario = data.getCustSubGrp();
    Addr zs01 = requestData.getAddress("ZS01");
    Addr zp01 = requestData.getAddress("ZP01");
    String customerName = zs01.getCustNm1();
    String customerNameZP01 = "";
    String landedCountryZP01 = "";
    String custGrp = data.getCustGrp();
    // CREATCMR-6244 LandCntry UK(GB)
    if (zs01 != null) {
      String landCntry = zs01.getLandCntry();
      if (data.getVat() != null && !data.getVat().isEmpty() && landCntry.equals("GB") && !data.getCmrIssuingCntry().equals("866") && custGrp != null
          && StringUtils.isNotEmpty(custGrp) && ("CROSS".equals(custGrp))) {
        engineData.addNegativeCheckStatus("_vatUK", " request need to be send to CMDE queue for further review. ");
        details.append("Landed Country UK. The request need to be send to CMDE queue for further review.\n");
      }
    }
    if (zp01 != null) {
      customerNameZP01 = StringUtils.isBlank(zp01.getCustNm1()) ? "" : zp01.getCustNm1();
      landedCountryZP01 = StringUtils.isBlank(zp01.getLandCntry()) ? "" : zp01.getLandCntry();
    }

    if (zp01 != null && !StringUtils.equals(zs01.getLandCntry(), zp01.getLandCntry())) {
      scenarioExceptions.setCheckVATForDnB(false);
    }
    if ((SCENARIO_BP_LOCAL.equals(scenario) || SCENARIO_BP_CROSS.equals(scenario)) && zp01 != null
        && (!StringUtils.equals(getCleanString(customerName), getCleanString(customerNameZP01))
            || !StringUtils.equals(zs01.getLandCntry(), landedCountryZP01))) {
      details.append("Customer Name and Landed Country on Sold-to and Bill-to address should be same for Business Partner Scenario.").append("\n");
      engineData.addNegativeCheckStatus("SOLDTO_BILLTO_DIFF",
          "Customer Name and Landed Country on Sold-to and Bill-to address should be same for Business Partner Scenario.");
    }
    if (!SCENARIO_BP_LOCAL.equals(scenario) && !SCENARIO_LOCAL_COMMERCIAL.equals(scenario) && !SCENARIO_INTERNAL.equals(scenario)
        && !SCENARIO_LOCAL_PUBLIC.equals(scenario)) {
      engineData.addPositiveCheckStatus(AutomationEngineData.SKIP_DNB_ORGID_VAL);
    }
    switch (scenario) {

    case SCENARIO_LOCAL_COMMERCIAL:
    case SCENARIO_CROSS_COMMERCIAL:
    case SCENARIO_LOCAL_PUBLIC:
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
      return doBusinessPartnerChecks(engineData, data.getPpsceid(), details);

    case SCENARIO_PRIVATE_CUSTOMER:
      String customerNameFull = zs01.getCustNm1() + (StringUtils.isNotBlank(zs01.getCustNm2()) ? " " + zs01.getCustNm2() : "");
      return doPrivatePersonChecks(engineData, data.getCmrIssuingCntry(), zs01.getLandCntry(), customerNameFull, details, false, requestData);

    case SCENARIO_IBM_EMPLOYEE:
      Person person = null;
      if (StringUtils.isNotBlank(zs01.getCustNm1())) {
        try {
          String mainCustName = zs01.getCustNm1() + (StringUtils.isNotBlank(zs01.getCustNm2()) ? " " + zs01.getCustNm2() : "");
          person = BluePagesHelper.getPersonByName(mainCustName, data.getCmrIssuingCntry());
          if (person == null) {
            engineData.addRejectionComment("OTH", "Employee details not found in IBM People.", "", "");
            details.append("Employee details not found in IBM People.").append("\n");
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
    String bgId = data.getBgId();
    String commercialFin = "";

    /*
     * if (StringUtils.isNotBlank(coverageId)) { String sortl =
     * getSORTLfromCoverage(entityManager, container.getFinalCoverage());
     * overrides.addOverride(AutomationElementRegistry.GBL_CALC_COV, "DATA",
     * "ENGINEERING_BO", data.getEngineeringBo(), sortl);
     * details.append("-BO Team: " + sortl); }
     */

    if (covFrom != null && !"BGNONE".equals(bgId.trim())) {
      commercialFin = computeSBOForCovBelux(entityManager, QUERY_BG_SBO_BENELUX, bgId, data.getCmrIssuingCntry(), false);
    }
    if (commercialFin != null && !commercialFin.isEmpty()) {
      overrides.addOverride(AutomationElementRegistry.GBL_CALC_COV, "DATA", "COMMERCIAL_FINANCED", data.getSalesBusOffCd(), commercialFin);
      details.append("SORTL: " + commercialFin);
    }
    engineData.addPositiveCheckStatus(AutomationEngineData.COVERAGE_CALCULATED);

    if (!isCoverageCalculated) {
      // if not calculated using bg/gbg try calculation using 32/S logic
      details.setLength(0);// clear string builder
      overrides.clearOverrides(); // clear existing overrides

      NLFieldsContainer fields = calculate32SValuesFromIMSNL(entityManager, requestData.getData());

      String commercialFinanced = calculateSortlByRepTeamCd(entityManager, data.getCmrIssuingCntry(), data.getCountryUse(), data.getIsuCd(),
          data.getClientTier());
      if (StringUtils.isNotBlank(commercialFinanced)) {
        details.append("Coverage calculated successfully using Sortl logic.").append("\n");
        details.append("SORTL : " + data.getCommercialFinanced()).append("\n");
        overrides.addOverride(AutomationElementRegistry.GBL_CALC_COV, "DATA", "SORTL", data.getCommercialFinanced(), commercialFinanced);
        results.setResults("Calculated");
        results.setDetails(details.toString());
      } else if (StringUtils.isNotBlank(data.getCommercialFinanced())) {
        details.append("Coverage could not be calculated using Sortl logic. Using values from request").append("\n");
        details.append("SORTL : " + data.getCommercialFinanced()).append("\n");
        results.setResults("Calculated");
        results.setDetails(details.toString());
      } else {
        String msg = "Coverage cannot be calculated. No valid Sortl mapping found from request data.";
        details.append(msg);
        results.setResults("Cannot Calculate");
        results.setDetails(details.toString());
      }

      if (fields != null) {
        details.append("Coverage calculated successfully using 32S logic.").append("\n");
        /*
         * if (StringUtils.isNotBlank(fields.getEngineeringBO())) {
         * details.append("BO Team : " +
         * fields.getEngineeringBO()).append("\n");
         * overrides.addOverride(AutomationElementRegistry.GBL_CALC_COV, "DATA",
         * "ENGINEERING_BO", data.getEngineeringBo(),
         * fields.getEngineeringBO()); }
         */
        if (StringUtils.isNotBlank(fields.getInac())) {
          details.append(" INAC/NAC Code : " + fields.getInac()).append("\n");
          overrides.addOverride(AutomationElementRegistry.GBL_CALC_COV, "DATA", "INAC_CD", data.getInacCd(), fields.getInac());
        }
        if (StringUtils.isNotBlank(fields.getEcoCd())) {
          details.append(" Economic Code : " + fields.getEcoCd()).append("\n");
          overrides.addOverride(AutomationElementRegistry.GBL_CALC_COV, "DATA", "ECONOMIC_CD", data.getEconomicCd(), fields.getEcoCd());
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

  private String computeSBOForCovBelux(EntityManager entityManager, String queryBgFR, String bgId, String cmrIssuingCntry, boolean b) {
    String sortl = "";
    String sql = ExternalizedQuery.getSql(queryBgFR);
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("KEY", bgId);
    query.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
    query.setParameter("COUNTRY", cmrIssuingCntry);
    String isoCntry = PageManager.getDefaultLandedCountry(cmrIssuingCntry);
    System.err.println("ISO: " + isoCntry);
    query.setParameter("ISO_CNTRY", isoCntry);
    query.setForReadOnly(true);

    LOG.debug("Calculating SORTL using Netherlands query " + queryBgFR + " for key: " + bgId);
    List<Object[]> results = query.getResults(5);
    List<String> sortlList = new ArrayList<String>();
    if (results != null && !results.isEmpty()) {
      for (Object[] result : results) {
        sortl = (String) result[3];
        sortlList.add(sortl);
        // SpainFieldsContainer fieldValues = new SpainFieldsContainer();
      }
    }
    sortl = sortlList.get(0);
    return sortl;
  }

  private NLFieldsContainer calculate32SValuesFromIMSNL(EntityManager entityManager, Data data) {
    NLFieldsContainer container = new NLFieldsContainer();

    // setEngineeringBO
    List<Object[]> engineeringBOs = new ArrayList<>();
    String isuCtc = (StringUtils.isNotBlank(data.getIsuCd()) ? data.getIsuCd() : "")
        + (StringUtils.isNotBlank(data.getClientTier()) ? data.getClientTier() : "");

    if (StringUtils.isNotBlank(data.getSubIndustryCd()) && data.getSubIndustryCd().length() > 1 && ("32S".equals(isuCtc))) {
      String ims = data.getSubIndustryCd().substring(0, 1);
      String sql = ExternalizedQuery.getSql("QUERY.GET.BOTEAMLIST.BYISUCTC");
      PreparedQuery query = new PreparedQuery(entityManager, sql);
      query.setParameter("ISU", "%" + isuCtc + "%");
      query.setParameter("ISSUING_CNTRY", SystemLocation.NETHERLANDS);
      query.setParameter("CLIENT_TIER", "%" + ims + "%");
      query.setForReadOnly(true);
      engineeringBOs = query.getResults();
    } else {
      String sql = ExternalizedQuery.getSql("QUERY.GET.SRLIST.BYISU");
      PreparedQuery query = new PreparedQuery(entityManager, sql);
      query.setParameter("ISU", "%" + isuCtc + "%");
      query.setParameter("ISSUING_CNTRY", SystemLocation.NETHERLANDS);
      engineeringBOs = query.getResults();
    }
    /*
     * if (engineeringBOs != null && engineeringBOs.size() == 1) { String
     * engineeringBO = (String) engineeringBOs.get(0)[0];
     * container.setEngineeringBO(engineeringBO); }
     */

    // setINACValues
    List<Object[]> inacValues = new ArrayList<>();
    if (StringUtils.isNotBlank(container.getEngineeringBO())) {
      String sql = ExternalizedQuery.getSql("QUERY.GET.INACLIST.BYBO");
      PreparedQuery query = new PreparedQuery(entityManager, sql);
      query.setParameter("ISSUING_CNTRY", SystemLocation.NETHERLANDS);
      query.setParameter("EngineeringBo", "%" + container.getEngineeringBO() + "%");
      query.setForReadOnly(true);
      inacValues = query.getResults();
    }
    if (inacValues != null && inacValues.size() == 1) {
      String inacValue = (String) inacValues.get(0)[0];
      container.setInac(inacValue);
    }

    // setEcoCode
    List<Object[]> ecoCodeValues = new ArrayList<>();
    if (StringUtils.isNotBlank(container.getEngineeringBO())) {
      String sql = ExternalizedQuery.getSql("QUERY.GET.ECONOMICLIST.BYST.NL");
      PreparedQuery query = new PreparedQuery(entityManager, sql);
      query.setParameter("ISSUING_CNTRY", SystemLocation.NETHERLANDS);
      query.setParameter("REP_TEAM_MEMBER_NO", "%" + container.getEngineeringBO() + "%");
      query.setForReadOnly(true);
      inacValues = query.getResults();
    }
    if (ecoCodeValues != null && ecoCodeValues.size() == 1) {
      String ecoCd = (String) ecoCodeValues.get(0)[0];
      container.setEcoCd(ecoCd);
    }

    return container;
  }

  private class NLFieldsContainer {
    private String engineeringBO;
    private String inac;
    private String ecoCd;

    public String getEngineeringBO() {
      return engineeringBO;
    }

    public void setEngineeringBO(String engineeringBO) {
      this.engineeringBO = engineeringBO;
    }

    public String getInac() {
      return inac;
    }

    public void setInac(String inac) {
      this.inac = inac;
    }

    public String getEcoCd() {
      return ecoCd;
    }

    public void setEcoCd(String ecoCd) {
      this.ecoCd = ecoCd;
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
    Set<String> resultCodes = new HashSet<String>();
    List<String> ignoredUpdates = new ArrayList<String>();
    if (admin.getReqType().equals("U")) {
      engineData.addPositiveCheckStatus(AutomationEngineData.SKIP_DNB_ORGID_VAL);
    }
    for (UpdatedDataModel change : changes.getDataUpdates()) {
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
            String billToCntry = getBillToLandCntry(entityManager, requestData);
            if (soldTo.getLandCntry().equals(billToCntry)) {
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
            } else {
              cmdeReview = true;
              engineData.addNegativeCheckStatus("_beluxVATCheckFailed", "Sold to and Bill to have different landed country.");
            }
          }

          if (!StringUtils.isBlank(change.getOldData()) && !StringUtils.isBlank(change.getNewData())
              && !(change.getOldData().equals(change.getNewData()))) {
            // UPDATE
            cmdeReview = true;
            engineData.addNegativeCheckStatus("_beluxVATCheckFailed", "VAT updation requires cmde review.");
          }
          if (StringUtils.isBlank(change.getNewData()) && !StringUtils.isBlank(change.getOldData())) {
            // noop
          }
        }
        break;
      case "ISIC":
      case "INAC/NAC Code":
        cmdeReview = true;
        details.append(change.getDataField() + " is updated.").append("\n");
        break;
      case "Economic Code":
        cmdeReview = true;
        details.append("Eco is updated to " + change.getNewData()).append("\n");
        break;
      case "KVK":
        cmdeReview = true;
        details.append("KVK is updated to " + change.getNewData()).append("\n");
        break;
      case "PPS CEID":
        String newppsceid = change.getNewData();
        String oldppsceid = change.getOldData();
        String Kukla = data.getCustClass();
        // ADD
        if (StringUtils.isBlank(oldppsceid) && !StringUtils.isBlank(newppsceid)) {
          if ("49".equalsIgnoreCase(Kukla) && checkPPSCEID(data.getPpsceid())) {
            details.append("PPS CE ID validated successfully with PartnerWorld Profile Systems.").append("\n");
          } else {
            resultCodes.add("D");
            if (!"49".equalsIgnoreCase(Kukla)) {
              details.append("PS Ceid added for CMR with Kukla other than 49").append("\n");
            } else {
              details.append("PPS ceid on the request is invalid").append("\n");
            }
          }
        }
        // DELETE
        if (!StringUtils.isBlank(oldppsceid) && StringUtils.isBlank(newppsceid)) {
          cmdeReview = true;
          engineData.addNegativeCheckStatus("_beluxPpsCeidUpdt", "Deletion of ppsceid needs cmde review.\n");
          details.append("Deletion of ppsceid needs cmde review.\n");
        }
        // UPDATE
        if (!StringUtils.isBlank(oldppsceid) && !StringUtils.isBlank(newppsceid) && !oldppsceid.equalsIgnoreCase(newppsceid)) {
          cmdeReview = true;
          engineData.addNegativeCheckStatus("_beluxPpsCeidUpdt", "Update of ppsceid needs cmde review.\n");
          details.append("Update of ppsceid needs cmde review.\n");
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
        // UPDATE
        if (!StringUtils.isBlank(oldOrdBlk) && !StringUtils.isBlank(newOrdBlk) && newOrdBlk.equalsIgnoreCase(oldOrdBlk)
            && "P".equalsIgnoreCase(newOrdBlk)) {
          cmdeReview = true;
          engineData.addNegativeCheckStatus("_beluxOrdBlkUpdt", "Embargo Code was updated to P.");
          details.append("Embargo Code P was added.\n");
        }

        // DELETE
        if (!StringUtils.isBlank(oldOrdBlk) && StringUtils.isBlank(newOrdBlk) && "P".equalsIgnoreCase(oldOrdBlk)) {
          cmdeReview = true;
          engineData.addNegativeCheckStatus("_beluxOrdBlkUpdt", "Embargo Code P was deleted.");
          details.append("Embargo Code P was deleted.\n");
        }
        break;
      case "Preferred Language":
        details.append("Preferred Language data field was updated.\n");
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
      engineData.addNegativeCheckStatus("_esDataCheckFailed", "Updates to one or more fields need review.");
      details.append("Updates to one or more fields need review.\n");
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
    StringBuilder nonRelAddrFdsDetails = new StringBuilder();
    List<Addr> addresses = null;
    int ignoredAddr = 0;
    StringBuilder checkDetails = new StringBuilder();
    Set<String> resultCodes = new HashSet<String>();// R - review
    Addr zs01 = requestData.getAddress("ZS01");
    // boolean payGoAddredited = RequestUtils.isPayGoAccredited(entityManager,
    // requestData.getAdmin().getSourceSystId());
    LOG.debug("Verifying PayGo Accreditation for " + admin.getSourceSystId());
    boolean payGoAddredited = RequestUtils.isPayGoAccredited(entityManager, admin.getSourceSystId());
    boolean isOnlyPayGoUpdated = changes != null && changes.isAddressChanged("PG01") && !changes.isAddressChanged("ZS01")
        && !changes.isAddressChanged("ZI01");

    if (admin.getReqType().equals("U")) {
      engineData.addPositiveCheckStatus(AutomationEngineData.SKIP_DNB_ORGID_VAL);
    }

    for (String addrType : RELEVANT_ADDRESSES) {
      addresses = requestData.getAddresses(addrType);
      if (changes.isAddressChanged(addrType)) {
        for (Addr addr : addresses) {
          List<String> addrTypesChanged = new ArrayList<String>();
          for (UpdatedNameAddrModel addrModel : changes.getAddressUpdates()) {
            if (!addrTypesChanged.contains(addrModel.getAddrTypeCode())) {
              addrTypesChanged.add(addrModel.getAddrTypeCode());
            }
          }

          boolean isZS01WithAufsdPG = (CmrConstants.RDC_SOLD_TO.equals(addrType) && "PG".equals(data.getOrdBlk()));

          if (isRelevantAddressFieldUpdated(changes, addr, nonRelAddrFdsDetails)) {

            if ((addrType.equalsIgnoreCase(CmrConstants.RDC_SOLD_TO) && "Y".equals(addr.getImportInd()))) {
              List<DnBMatchingResponse> matches = getMatches(requestData, engineData, addr, false);
              boolean matchesDnb = false;
              if (matches != null) {
                // check against D&B
                matchesDnb = ifaddressCloselyMatchesDnb(matches, addr, admin, data.getCmrIssuingCntry());
              }

              if (!matchesDnb) {
                LOG.debug("Address " + addrType + "(" + addr.getId().getAddrSeq() + ") does not match D&B");
                resultCodes.add("D");
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

            if (addrType.equalsIgnoreCase(CmrConstants.RDC_BILL_TO)) {
              if (!compareCustomerNames(zs01, addr)) {
                LOG.debug("Address " + addrType + "(" + addr.getId().getAddrSeq() + ") needs to be verified");
                checkDetails.append("Address " + addrType + "(" + addr.getId().getAddrSeq() + ") has different customer name than sold-to.\n");
                resultCodes.add("D");
              } else {
                List<DnBMatchingResponse> matches = getMatches(requestData, engineData, addr, false);
                boolean matchesDnb = false;
                if (matches != null) {
                  // check against D&B
                  matchesDnb = ifaddressCloselyMatchesDnb(matches, addr, admin, data.getCmrIssuingCntry());
                }
                if (!matchesDnb) {
                  LOG.debug("Address " + addrType + "(" + addr.getId().getAddrSeq() + ") does not match D&B");
                  resultCodes.add("D");
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
            }

            if (CmrConstants.RDC_INSTALL_AT.equals(addrType)) {
              String installAtName = getCustomerFullName(addr);
              String soldToName = getCustomerFullName(zs01);
              if (installAtName.equals(soldToName)) {
                if (addressExists(entityManager, addr, requestData)) {

                  LOG.debug(" - Duplicates found for " + addrType + "(" + addr.getId().getAddrSeq() + ")");
                  checkDetails.append("Address " + addrType + "(" + addr.getId().getAddrSeq() + ") provided matches an existing address.\n");
                  resultCodes.add("D");
                } else if ((payGoAddredited && addrTypesChanged.contains(CmrConstants.RDC_PAYGO_BILLING.toString())) || isZS01WithAufsdPG) {
                  if ("N".equals(addr.getImportInd())) {
                    LOG.debug("Checking duplicates for " + addrType + "(" + addr.getId().getAddrSeq() + ")");
                    boolean duplicate = addressExists(entityManager, addr, requestData);
                    if (duplicate) {
                      LOG.debug(" - Duplicates found for " + addrType + "(" + addr.getId().getAddrSeq() + ")");
                      checkDetails
                          .append("Address " + addrType + "(" + addr.getId().getAddrSeq() + ") provided matches an existing Bill-To address.\n");
                      resultCodes.add("D");
                    } else {
                      LOG.debug(" - NO duplicates found for " + addrType + "(" + addr.getId().getAddrSeq() + ")");
                      checkDetails.append(" - NO duplicates found for " + addrType + "(" + addr.getId().getAddrSeq() + ")" + "with same attentionTo");
                      checkDetails
                          .append("Updates to address fields for" + addrType + "(" + addr.getId().getAddrSeq() + ") validated in the checks.\n");

                    }
                  } else {
                    checkDetails
                        .append("Updates to address fields for" + addrType + "(" + addr.getId().getAddrSeq() + ") validated in the checks.\n");
                  }
                } else {
                  LOG.debug("Addition/Updation of " + addrType + "(" + addr.getId().getAddrSeq() + ")");
                  checkDetails.append("Address (" + addr.getId().getAddrSeq() + ") is validated.\n");
                }
              } else {
                LOG.debug("Address " + addrType + "(" + addr.getId().getAddrSeq() + ") needs to be verified");
                checkDetails.append("Address " + addrType + "(" + addr.getId().getAddrSeq() + ") has different customer name than sold-to.\n");
                resultCodes.add("D");
              }
            }

            if ((addrType.equalsIgnoreCase(CmrConstants.RDC_SHIP_TO) || addrType.equalsIgnoreCase(CmrConstants.RDC_PAYGO_BILLING))
                && "N".equals(addr.getImportInd())) {
              LOG.debug("Checking duplicates for " + addrType + "(" + addr.getId().getAddrSeq() + ")");

              boolean duplicate = addressExists(entityManager, addr, requestData);

              if (duplicate) {
                LOG.debug(" - Duplicates found for " + addrType + "(" + addr.getId().getAddrSeq() + ")");
                checkDetails.append("Addition of " + addrType + "(" + addr.getId().getAddrSeq() + ") provided matches an existing address.\n");
                resultCodes.add("D");
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
      engineData.addRejectionComment("_atRejectAddr", "Addition or updation on the address is rejected", "", "");
      validation.setSuccess(false);
      validation.setMessage("Rejected");
    } else if (resultCodes.contains("D")) {
      validation.setSuccess(false);
      validation.setMessage("Not Validated");
      engineData.addNegativeCheckStatus("_atCheckFailed", "Updates to addresses cannot be checked automatically.");
    } else {
      validation.setSuccess(true);
      validation.setMessage("Successful");
    }

    if (ignoredAddr > 0) {
      checkDetails.append("Updates to imported Address Ship-To(ZD01) is skipped. ");
    }
    String details = (output.getDetails() != null && output.getDetails().length() > 0) ? output.getDetails() : "";
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

  private String getBillToLandCntry(EntityManager entityManager, RequestData requestData) {
    String zp01 = "";
    String sql = ExternalizedQuery.getSql("AUTO.GET_LAND_CNTRY");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", requestData.getAdmin().getId().getReqId());
    query.setForReadOnly(true);
    List<String> landedCntry = query.getResults(String.class);
    if (landedCntry != null && !landedCntry.isEmpty()) {
      zp01 = landedCntry.get(0);
    }
    return zp01;
  }

  /**
   * Computes the SBO by getting the SORTL most used by the COverage ID
   * 
   * @param entityManager
   * @param coverage
   * @return
   */
  private String getSORTLfromCoverage(EntityManager entityManager, String coverage) {
    List<String> sortlList = null;
    try {
      LOG.debug("Computing SORTL for Coverage " + coverage);
      String sql = ExternalizedQuery.getSql("AUTO.NL.COV.SORTL");
      PreparedQuery query = new PreparedQuery(entityManager, sql);
      query.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
      query.setParameter("COVID", coverage);
      query.setParameter("KATR6", SystemLocation.NETHERLANDS);
      query.setForReadOnly(true);
      sortlList = query.getResults(20, String.class);
    } catch (Exception e) {
      LOG.debug("Error while computing SORTL for Coverage " + coverage + " from RDc query.");
    }
    if (sortlList != null) {
      for (String sortl : sortlList) {
        if (StringUtils.isNotBlank(sortl) && sortl.length() == 6) {
          return sortl;
        }
      }
    }
    return "333D3";
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

  @Override
  public List<String> getSkipChecksRequestTypesforCMDE() {
    return Arrays.asList("C", "U", "M", "D", "R");
  }

  public String calculateSortlByRepTeamCd(EntityManager entityManager, String cmrIssuingctry, String countryUse, String isuCd, String clientTier) {
    List<Object[]> commercialFinancedResults = new ArrayList<>();
    String commercialFinanced = "";
    String salesRepTeam = "";
    String isuCtc = (StringUtils.isNotBlank(isuCd) ? isuCd : "") + (StringUtils.isNotBlank(clientTier) ? clientTier : "");
    String geoCd = "";
    if (StringUtils.isNotBlank(countryUse) && countryUse.length() > 3) {
      geoCd = countryUse.substring(3, 5);
    }
    String cmrCntry = cmrIssuingctry + geoCd;
    salesRepTeam = isuCtc + "2P0";
    String sql = ExternalizedQuery.getSql("QUERY.GET.DSCLIST.BYSR");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("ISSUING_CNTRY", cmrCntry);
    query.setParameter("ISU", "%" + isuCtc + "%");
    query.setParameter("REP_TEAM_CD", salesRepTeam);
    query.setForReadOnly(true);
    commercialFinancedResults = query.getResults();
    if (commercialFinancedResults != null && commercialFinancedResults.size() == 1) {
      commercialFinanced = (String) commercialFinancedResults.get(0)[0];
      return commercialFinanced;
    } else {
      return null;
    }
  }
}
