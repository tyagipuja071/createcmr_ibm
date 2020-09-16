package com.ibm.cio.cmr.request.automation.util.geo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.automation.AutomationElementRegistry;
import com.ibm.cio.cmr.request.automation.AutomationEngineData;
import com.ibm.cio.cmr.request.automation.RequestData;
import com.ibm.cio.cmr.request.automation.out.AutomationResult;
import com.ibm.cio.cmr.request.automation.out.OverrideOutput;
import com.ibm.cio.cmr.request.automation.out.ValidationOutput;
import com.ibm.cio.cmr.request.automation.util.AutomationUtil;
import com.ibm.cio.cmr.request.automation.util.RequestChangeContainer;
import com.ibm.cio.cmr.request.automation.util.ScenarioExceptionsUtil;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.entity.Kna1;
import com.ibm.cio.cmr.request.model.CompanyRecordModel;
import com.ibm.cio.cmr.request.model.window.UpdatedDataModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.util.CompanyFinder;
import com.ibm.cio.cmr.request.util.SystemLocation;
import com.ibm.cmr.services.client.matching.dnb.DnBMatchingResponse;
import com.ibm.cmr.services.client.matching.gbg.GBGResponse;

public class SingaporeUtil extends AutomationUtil {

  private static final Logger LOG = Logger.getLogger(SingaporeUtil.class);

  private static final List<String> ALLOW_DEFAULT_SCENARIOS = Arrays.asList("PRIV", "XPRIV", "BLUMX", "MKTPC", "XBLUM", "XMKTP");

  private static final List<String> RELEVANT_ADDRESSES = Arrays.asList(CmrConstants.RDC_SOLD_TO, CmrConstants.RDC_BILL_TO,
      CmrConstants.RDC_INSTALL_AT, "ZH01");

  public static final String SCENARIO_BLUEMIX = "BLUMX";
  public static final String SCENARIO_MARKETPLACE = "MKTPC";
  public static final String SCENARIO_CROSS_BLUEMIX = "XBLUM";
  public static final String SCENARIO_CROSS_MARKETPLACE = "XMKTP";

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

    if (!"SPOFF".equalsIgnoreCase(data.getCustSubGrp())) {
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

    // CMR - 4507
    if ("SPOFF".equalsIgnoreCase(data.getCustSubGrp()) && StringUtils.isNotBlank(data.getCmrNo())) {
      Addr addr = requestData.getAddress(CmrConstants.RDC_SOLD_TO);
      String zs01LandCntry = addr.getLandCntry();
      String landCntryCd = getLandCntryCode(entityManager, zs01LandCntry);
      Map<String, Boolean> checkResults = checkifCMRNumExistsNDetailsMatch(data.getCmrNo(), landCntryCd, entityManager, data,
          requestData.getAddress(CmrConstants.RDC_SOLD_TO));
      if (!checkResults.get("cmrExists")) {
        details.append("CMR# " + data.getCmrNo() + " cleared as it doesn't exist in FIND CMR.");
        overrides.addOverride(AutomationElementRegistry.GBL_FIELD_COMPUTE, "DATA", "CMR_NO", data.getCmrNo(), "");
      } else if (checkResults.get("cmrExistsFrSG")) {
        details.append("CMR# " + data.getCmrNo() + " cleared as  it is not available for Singapore.");
        overrides.addOverride(AutomationElementRegistry.GBL_FIELD_COMPUTE, "DATA", "CMR_NO", data.getCmrNo(), "");

      }
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
    Data data = requestData.getData();
    String scenario = data.getCustSubGrp();
    String[] scnarioList = { "ASLOM", "NRML" };

    allowDuplicatesForScenario(engineData, requestData, Arrays.asList(scnarioList));

    processSkipCompanyChecks(engineData, requestData, details);

    // CMR - 4507
    if ("SPOFF".equalsIgnoreCase(data.getCustSubGrp())) {
      Addr addr = requestData.getAddress(CmrConstants.RDC_SOLD_TO);
      String zs01LandCntry = addr.getLandCntry();
      String issuinglandCntryCd = getLandCntryCode(entityManager, zs01LandCntry);
      try {
        if (StringUtils.isNotBlank(data.getCmrIssuingCntry()) && StringUtils.isNotBlank(issuinglandCntryCd)) {
          Map<String, Boolean> checkresult = checkifCMRNumExistsNDetailsMatch(data.getCmrNo(), issuinglandCntryCd, entityManager, data,
              requestData.getAddress(CmrConstants.RDC_SOLD_TO));
          boolean cmrExists = checkresult.get("cmrExists");
          boolean cmrExistsFrSG = checkresult.get("cmrExistsFrSG");
          boolean detailsMatch = checkresult.get("detailsMatch");

          if (cmrExists) {
            LOG.debug("CMR#" + data.getCmrNo() + " exists in FIND CMR for -> " + issuinglandCntryCd);
            if (cmrExistsFrSG) {
              LOG.debug("Requested CMR No. " + data.getCmrNo() + " is not available, CMR No. will automatically be generated by the system.");
              details.append("CMR#. " + data.getCmrNo() + " unavailable & will be generated by system.");
            } else {
              if (detailsMatch) {
                LOG.debug("CMR exists and Details Match , Hence proceeding with automating the request.");
                details.append("CMR exists & Details Match,proceeding with automating the request.");
              } else {
                LOG.debug("CMR# " + data.getCmrNo() + " Exists but request details do not match the details of CMR in FIND CMR.");
                details.append("CMR# " + data.getCmrNo() + " exists but request details do not match.");
                engineData.addNegativeCheckStatus("OTH",
                    "CMR# " + data.getCmrNo() + " exists but request details do not match the details of CMR in RDc.");
                result.setOnError(true);
              }
            }
          } else {
            LOG.debug("CMR# " + data.getCmrNo() + " does not exist in FIND CMR.");
            details.append("CMR# " + data.getCmrNo() + " does not exist in FIND CMR.");
            engineData.addNegativeCheckStatus("OTH", "CMR# " + data.getCmrNo() + " does not exist in FIND CMR.");
            result.setOnError(true);
          }

          /*
           * if (!cmrExists) { details.append("CMR# " + data.getCmrNo() +
           * " does not exist in RDc.");
           * engineData.addNegativeCheckStatus("OTH", "CMR# " + data.getCmrNo()
           * + " does not exist in RDc."); result.setOnError(true); } else if
           * (cmrExistsFrSG) { details.append("Requested CMR No. " +
           * data.getCmrNo() +
           * " is not available, CMR No. will automatically be generated by the system."
           * ); } else if (cmrExists && !detailsMatch) { details.append("CMR# "
           * + data.getCmrNo() +
           * " Exists but request details do not match the details of CMR in RDc."
           * ); engineData.addNegativeCheckStatus("OTH", "CMR# " +
           * data.getCmrNo() +
           * " Exists but request details do not match the details of CMR in RDc."
           * ); result.setOnError(true); } else if (cmrExists && detailsMatch) {
           * details.
           * append("Details Match , Hence proceeding with automating the request."
           * ); }
           */
        }
      } catch (Exception e) {
        LOG.debug("Error on searching for CMR in FIND CMR." + e.getMessage());
      }
    }
    switch (scenario) {
    case SCENARIO_BLUEMIX:
    case SCENARIO_MARKETPLACE:
    case SCENARIO_CROSS_BLUEMIX:
    case SCENARIO_CROSS_MARKETPLACE:
      engineData.addPositiveCheckStatus(AutomationEngineData.SKIP_GBG);
      break;
    }
    result.setDetails(details.toString());
    return true;
  }

  private Map<String, Boolean> checkifCMRNumExistsNDetailsMatch(String cmrNo, String issuinglandCntryCd, EntityManager entityManager, Data data,
      Addr addr) {
    Map<String, Boolean> cmrdetails = new HashMap<String, Boolean>();
    CompanyRecordModel searchModel = new CompanyRecordModel();
    searchModel.setCmrNo(cmrNo);
    searchModel.setIssuingCntry(issuinglandCntryCd);
    CompanyRecordModel cmrData = null;
    try {
      List<CompanyRecordModel> cmrsData = CompanyFinder.findCompanies(searchModel);
      if (!cmrsData.isEmpty())
        cmrData = cmrsData.get(0);
    } catch (Exception e) {
      e.printStackTrace();
    }
    if (cmrData != null) {
      LOG.debug("CMR No. exists in FIND CMR > " + cmrNo);
      cmrdetails.put("cmrExists", true);

      // if cmr exists,check if available for processing under 834
      boolean cmrExistsFrSG = checkifCMRNumExistsSG(cmrNo, entityManager);

      // if cmr does not exist in 834 , check for name , address and isic
      if (!cmrExistsFrSG) {
        LOG.debug("CMR No. does not exist in RDC Db for SG and hence can be used for processing > " + cmrNo);
        cmrdetails.put("cmrExistsFrSG", false);

        // collect Req Data
        String reqNme1 = StringUtils.isNotBlank(addr.getCustNm1()) ? addr.getCustNm1().replaceAll("\\s", "") : "";
        String reqNme2 = StringUtils.isNotBlank(addr.getCustNm2()) ? addr.getCustNm2().replaceAll("\\s", "") : "";
        String reqFullNme = reqNme1.concat(reqNme2);

        String reqAddr1 = StringUtils.isNotBlank(addr.getAddrTxt()) ? addr.getAddrTxt().replaceAll("\\s", "") : "";
        String reqAddr2 = StringUtils.isNotBlank(addr.getAddrTxt2()) ? addr.getAddrTxt2().replaceAll("\\s", "") : "";
        String reqCity = StringUtils.isNotBlank(addr.getCity1()) ? addr.getCity1().replaceAll("\\s", "") : "";
        String reqDept = StringUtils.isNotBlank(addr.getDept()) ? addr.getDept().replaceAll("\\s", "") : "";

        String reqAddrFullTxt = reqAddr1 + reqAddr2 + reqCity + reqDept;
        String isicCd = data.getIsicCd();

        // collect RDC data
        Kna1 kna1 = getDataAddrDetailsFrmRDC(cmrNo, entityManager, issuinglandCntryCd);
        String rdcName1 = StringUtils.isNotBlank(kna1.getName1()) ? kna1.getName1().replace("@", "").replaceAll("\\s", "") : "";
        String rdcName2 = StringUtils.isNotBlank(kna1.getName2()) ? kna1.getName2().replaceAll("\\s", "") : "";
        String rdcFullName = rdcName1 + rdcName2;
        String rdcIsicCd = kna1.getZzkvSic();

        String rdcAddr1 = StringUtils.isNotBlank(kna1.getName3()) ? kna1.getName3().replaceAll("\\s", "") : "";
        String rdcAddr2 = StringUtils.isNotBlank(kna1.getName4()) ? kna1.getName4().replaceAll("\\s", "") : "";
        String rdcAddr3 = StringUtils.isNotBlank(kna1.getStras()) ? kna1.getStras().replaceAll("\\s", "") : "";
        String rdcAddr4 = StringUtils.isNotBlank(kna1.getOrt01()) ? kna1.getOrt01().replaceAll("\\s", "") : "";
        String rdcAddr5 = StringUtils.isNotBlank(kna1.getOrt02()) ? kna1.getOrt02().replaceAll("\\s", "") : "";

        // city and dept are interchanged in RDC sometimes
        String rdcFullAddrTxt1 = rdcAddr1 + rdcAddr2 + rdcAddr3 + rdcAddr5 + rdcAddr4;
        String rdcFullAddrTxt2 = rdcAddr1 + rdcAddr2 + rdcAddr3 + rdcAddr4 + rdcAddr5;

        // compare the two data , to see if they match
        if (StringUtils.isNotBlank(rdcFullName) && StringUtils.isNotBlank(rdcFullAddrTxt1) && StringUtils.isNotBlank(rdcFullAddrTxt2)
            && rdcFullName.equalsIgnoreCase(reqFullNme)
            && (rdcFullAddrTxt1.equalsIgnoreCase(reqAddrFullTxt) || rdcFullAddrTxt2.equalsIgnoreCase(reqAddrFullTxt))) {
          if (StringUtils.isNotBlank(rdcIsicCd) && isicCd.equals(rdcIsicCd)) {
            cmrdetails.put("detailsMatch", true);
          } else {
            cmrdetails.put("detailsMatch", false);
          }
        } else {
          cmrdetails.put("detailsMatch", false);
        }
      } else {
        cmrdetails.put("cmrExistsFrSG", true);
        LOG.debug("CMR No. already exists in RDC Db for Singapore and hence cannot be used for processing > " + cmrNo);
      }
    } else {
      cmrdetails.put("cmrExists", false);
      LOG.debug("CMR No. details mentioned in the request do not exist in FIND CMR for the landed cntry > " + issuinglandCntryCd);
    }

    return cmrdetails;
  }

  private Kna1 getDataAddrDetailsFrmRDC(String cmrNo, EntityManager entityManager, String issuinglandCntry) {
    String sqlRDC = ExternalizedQuery.getSql("KNA1.CHECK_IF_CMR_EXISTS");
    PreparedQuery queryRDC = new PreparedQuery(entityManager, sqlRDC);
    queryRDC.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
    queryRDC.setParameter("ZZKV_CUSNO", cmrNo);
    queryRDC.setParameter("KATR6", issuinglandCntry);

    Kna1 kna1 = queryRDC.getSingleResult(Kna1.class);
    return kna1;

  }

  private boolean checkifCMRNumExistsSG(String cmrNo, EntityManager entityManager) {
    LOG.debug("Checking if CMR exists or not in Singapore and available for procesing...");
    String sqlRDC = ExternalizedQuery.getSql("KNA1.CHECK_IF_CMR_EXISTS");
    PreparedQuery queryRDC = new PreparedQuery(entityManager, sqlRDC);
    queryRDC.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
    queryRDC.setParameter("ZZKV_CUSNO", cmrNo);
    queryRDC.setParameter("KATR6", SystemLocation.SINGAPORE);

    Kna1 kna1 = queryRDC.getSingleResult(Kna1.class);
    if (kna1 != null) {
      return true;
    } else {
      return false;
    }
  }

  private String getLandCntryCode(EntityManager entityManager, String landCntryCd) {
    String issuingCntryCd = null;
    String sql = ExternalizedQuery.getSql("GET.CNTRYCD_FRM_LANDCNTRY");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("LAND_CNTRY", landCntryCd);
    List<String> results = query.getResults(String.class);
    if (results != null && !results.isEmpty()) {
      issuingCntryCd = results.get(0);
    }
    entityManager.flush();
    return issuingCntryCd;
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
    List<DnBMatchingResponse> matches = new ArrayList<DnBMatchingResponse>();
    boolean matchesDnb = false;
    boolean cmdeReview = false;
    for (String addrType : RELEVANT_ADDRESSES) {
      if (CmrConstants.RDC_SOLD_TO.equals(addrType)) {
        Addr soldTo = requestData.getAddress(CmrConstants.RDC_SOLD_TO);
        matches = getMatches(requestData, engineData, soldTo, false);
        if (matches != null) {
          // check against D&B
          matchesDnb = ifaddressCloselyMatchesDnb(matches, soldTo, admin, data.getCmrIssuingCntry());
        }
      }
      if (changes.isAddressChanged(addrType)) {
        if (CmrConstants.RDC_SOLD_TO.equals(addrType)) {
          addresses = Collections.singletonList(requestData.getAddress(CmrConstants.RDC_SOLD_TO));
        } else {
          addresses = requestData.getAddresses(addrType);
        }
        for (Addr addr : addresses) {
          if (CmrConstants.RDC_SOLD_TO.equals(addrType) && ("Y".equals(addr.getChangedIndc()) || "N".equals(addr.getImportInd()))) {
            if (null == changes.getAddressChange(addrType, "Customer Name") && null == changes.getAddressChange(addrType, "Customer Name Con't")) {
              if (!matchesDnb) {
                // CMDE Review
                checkDetails.append("Updates to Sold To " + addrType + "(" + addr.getId().getAddrSeq() + ") did not match D&B records.").append("\n");
                cmdeReview = true;
                break;
              } else {
                // proceed
                checkDetails.append("Updates to Sold To " + addrType + "(" + addr.getId().getAddrSeq() + ") matches D&B records.").append("\n");
                for (DnBMatchingResponse dnb : matches) {
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
