package com.ibm.cio.cmr.request.automation.util.geo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
import com.ibm.cio.cmr.request.model.window.UpdatedDataModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cmr.services.client.CmrServicesFactory;
import com.ibm.cmr.services.client.QueryClient;
import com.ibm.cmr.services.client.matching.dnb.DnBMatchingResponse;
import com.ibm.cmr.services.client.matching.gbg.GBGResponse;
import com.ibm.cmr.services.client.query.QueryRequest;
import com.ibm.cmr.services.client.query.QueryResponse;

public class SingaporeUtil extends AutomationUtil {

  private static final Logger LOG = Logger.getLogger(SingaporeUtil.class);

  private static final List<String> ALLOW_DEFAULT_SCENARIOS = Arrays.asList("PRIV", "XPRIV", "BLUMX", "MKTPC", "XBLUM", "XMKTP");

  private static final List<String> RELEVANT_ADDRESSES = Arrays.asList(CmrConstants.RDC_SOLD_TO, CmrConstants.RDC_BILL_TO,
      CmrConstants.RDC_INSTALL_AT, CmrConstants.RDC_SHIPPING);

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
      if (checkifCMRNumExists(data.getCmrNo(), addr.getLandCntry())) {
        details.append("CMR Number field cleared as requested CMR does not exist.");
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
    List<Addr> addresses = requestData.getAddresses();
    String[] scnarioList = { "ASLOM", "NRML" };

    allowDuplicatesForScenario(engineData, requestData, Arrays.asList(scnarioList));

    processSkipCompanyChecks(engineData, requestData, details);

    // CMR - 4507
    // get zs01 land cntry
    if ("SPOFF".equalsIgnoreCase(data.getCustSubGrp())) {
      Addr addr = requestData.getAddress(CmrConstants.RDC_SOLD_TO);
      String zs01LandCntry = addr.getLandCntry();

      try {
        if (StringUtils.isNotBlank(data.getCmrIssuingCntry()) && StringUtils.isNotBlank(zs01LandCntry)) {
          boolean cmrExists = checkifCMRNumExists(data.getCmrNo(), zs01LandCntry);
          boolean detailsmatch = checkifDetailsOnReqMatch(data, zs01LandCntry, requestData.getAddress(CmrConstants.RDC_SOLD_TO));

          if (!cmrExists || !detailsmatch) {
            details.append("Details do not match.");
            engineData.addNegativeCheckStatus("OTH", "");
            result.setOnError(true);
          } else if (cmrExists && detailsmatch) {
            details.append("Details Match ! Hence proceeding with automating the request.");
          } else if (!cmrExists) {
            details.append("Requested CMR No. is not available, CMR No. will automatically be generated by the system.");
          }

        }
      } catch (Exception e) {
        LOG.debug("");
      }
      result.setDetails(details.toString());
    }
    return true;
  }

  private boolean checkifCMRNumExists(String cmrNo, String zs01LandCntry) {
    try {
      String url = SystemConfiguration.getValue("CMR_SERVICES_URL");
      String mandt = SystemConfiguration.getValue("MANDT");
      String sql = ExternalizedQuery.getSql("KNA1.CHECK_IF_CMR_EXISTS");
      sql = StringUtils.replace(sql, ":MANDT", "'" + mandt + "'");
      sql = StringUtils.replace(sql, ":ZZKV_CUSNO", "'" + cmrNo + "'");
      sql = StringUtils.replace(sql, ":BEGRU", "'" + zs01LandCntry + "'");

      String dbId = QueryClient.RDC_APP_ID;

      QueryRequest query = new QueryRequest();
      query.setSql(sql);
      QueryClient client = CmrServicesFactory.getInstance().createClient(url, QueryClient.class);
      QueryResponse response = client.executeAndWrap(dbId, query, QueryResponse.class);
      if (response.isSuccess() && response.getRecords() != null && response.getRecords().size() != 0) {
        LOG.debug("CMR No. exists in RDC Db > " + cmrNo);
        // if cmr exists , check for name , address and isic
        return true;
      } else {
        return false;
      }
    } catch (Exception e) {
      LOG.debug("Error occured while querying RDC database");
    }
    return false;
  }

  private boolean checkifDetailsOnReqMatch(Data data, String zs01LandCntry, Addr addr) {
    try {
      String name1 = addr.getCustNm1();
      String name2 = StringUtils.isNotBlank(addr.getCustNm2()) ? addr.getCustNm2() : "";
      String name3 = addr.getAddrTxt();
      String name4 = StringUtils.isNotBlank(addr.getAddrTxt2()) ? addr.getAddrTxt2() : "";
      String isic = data.getIsicCd();
      String cmrNo = data.getCmrNo();
      String url = SystemConfiguration.getValue("CMR_SERVICES_URL");
      String mandt = SystemConfiguration.getValue("MANDT");
      String sql = ExternalizedQuery.getSql("KNA1.CHECK_IF_DATA_MATCHES");
      sql = StringUtils.replace(sql, ":MANDT", "'" + mandt + "'");
      sql = StringUtils.replace(sql, ":ZZKV_CUSNO", "'" + cmrNo + "'");
      sql = StringUtils.replace(sql, ":BEGRU", "'" + zs01LandCntry + "'");
      sql = StringUtils.replace(sql, ":NAME1", "'" + name1 + "'");
      sql = StringUtils.replace(sql, ":NAME2", "'" + name2 + "'");
      sql = StringUtils.replace(sql, ":NAME3", "'" + name3 + "'");
      sql = StringUtils.replace(sql, ":NAME4", "'" + name4 + "'");
      sql = StringUtils.replace(sql, ":ZZKV_SIC", "'" + isic + "'");

      String dbId = QueryClient.RDC_APP_ID;

      QueryRequest query = new QueryRequest();
      query.setSql(sql);
      QueryClient client = CmrServicesFactory.getInstance().createClient(url, QueryClient.class);
      QueryResponse response = client.executeAndWrap(dbId, query, QueryResponse.class);
      if (response.isSuccess() && response.getRecords() != null && response.getRecords().size() != 0) {
        return true;
      } else {
        return false;
      }
    } catch (Exception e) {
      LOG.debug("Error occurred while querying RDC database.");
    }
    return false;
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
