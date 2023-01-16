package com.ibm.cio.cmr.request.automation.util.geo.us;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.automation.AutomationElementRegistry;
import com.ibm.cio.cmr.request.automation.AutomationEngineData;
import com.ibm.cio.cmr.request.automation.RequestData;
import com.ibm.cio.cmr.request.automation.out.AutomationResult;
import com.ibm.cio.cmr.request.automation.out.OverrideOutput;
import com.ibm.cio.cmr.request.automation.util.AutomationConst;
import com.ibm.cio.cmr.request.automation.util.DummyServletRequest;
import com.ibm.cio.cmr.request.automation.util.geo.USUtil;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.AddrPK;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.Attachment;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.entity.ReqCmtLog;
import com.ibm.cio.cmr.request.entity.Scorecard;
import com.ibm.cio.cmr.request.entity.WfHist;
import com.ibm.cio.cmr.request.model.requestentry.FindCMRRecordModel;
import com.ibm.cio.cmr.request.model.requestentry.FindCMRResultModel;
import com.ibm.cio.cmr.request.model.requestentry.RequestEntryModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.requestentry.AttachmentService;
import com.ibm.cio.cmr.request.service.requestentry.RequestEntryService;
import com.ibm.cio.cmr.request.ui.template.Template;
import com.ibm.cio.cmr.request.ui.template.TemplateManager;
import com.ibm.cio.cmr.request.ui.template.TemplatedField;
import com.ibm.cio.cmr.request.user.AppUser;
import com.ibm.cio.cmr.request.util.CompanyFinder;
import com.ibm.cio.cmr.request.util.JpaManager;
import com.ibm.cio.cmr.request.util.RequestUtils;
import com.ibm.cio.cmr.request.util.SystemLocation;
import com.ibm.cio.cmr.request.util.SystemUtil;
import com.ibm.cio.cmr.request.util.geo.GEOHandler;
import com.ibm.cio.cmr.request.util.geo.impl.USHandler;
import com.ibm.cmr.services.client.CmrServicesFactory;
import com.ibm.cmr.services.client.MatchingServiceClient;
import com.ibm.cmr.services.client.PPSServiceClient;
import com.ibm.cmr.services.client.QueryClient;
import com.ibm.cmr.services.client.ServiceClient.Method;
import com.ibm.cmr.services.client.automation.us.SosResponse;
import com.ibm.cmr.services.client.dnb.DnBCompany;
import com.ibm.cmr.services.client.dnb.DnbData;
import com.ibm.cmr.services.client.matching.MatchingResponse;
import com.ibm.cmr.services.client.matching.cmr.DuplicateCMRCheckRequest;
import com.ibm.cmr.services.client.matching.cmr.DuplicateCMRCheckResponse;
import com.ibm.cmr.services.client.matching.dnb.DnBMatchingResponse;
import com.ibm.cmr.services.client.pps.PPSProfile;
import com.ibm.cmr.services.client.pps.PPSRequest;
import com.ibm.cmr.services.client.pps.PPSResponse;
import com.ibm.cmr.services.client.query.QueryRequest;
import com.ibm.cmr.services.client.query.QueryResponse;
import com.ibm.json.java.JSONObject;

/**
 * Generic Handler Class to handle BP requests via automation engine for US
 * 
 * @author RoopakChugh
 *
 */
public abstract class USBPHandler {

  private static final Logger LOG = Logger.getLogger(USBPHandler.class);
  public static final String RESTRICT_TO_END_USER = "BPQS";
  public static final String RESTRICT_TO_MAINTENANCE = "IRCSO";
  public static final String RESTRICT_ICC = "ICC";

  public static final String BP_MANAGING_IR = "MIR";
  public static final String BP_INDIRECT_REMARKETER = "IRMR";

  public static final String TYPE_STATE_AND_LOCAL = "7";
  public static final String TYPE_FEDERAL = "9";
  public static final String TYPE_COMMERCIAL = "1";
  public static final String TYPE_BUSINESS_PARTNER = "5";
  public static final String TYPE_LEASING = "6";

  public static final String SUB_TYPE_STATE_AND_LOCAL_STATE = "STATE";
  public static final String SUB_TYPE_STATE_AND_LOCAL_DISTRICT = "SPEC DIST";
  public static final String SUB_TYPE_STATE_AND_LOCAL_COUNTY = "COUNTY";
  public static final String SUB_TYPE_STATE_AND_LOCAL_CITY = "CITY";
  public static final String SUB_TYPE_FEDERAL_POA = "POA";
  public static final String SUB_TYPE_FEDERAL_POAN = "POAN";
  public static final String SUB_TYPE_FEDERAL_REGULAR_GOVT = "FEDERAL";
  public static final String SUB_TYPE_COMMERCIAL_REGULAR = "REGULAR";
  public static final String SUB_TYPE_BUSINESS_PARTNER_END_USER = "END USER";
  public static final String SUB_TYPE_FSP_END_USER = "FSP END USER";
  public static final String AFFILIATE_FEDERAL = "9200000";
  public static final String SUB_TYPE_LEASE_NO_RESTRICT = "NO RESTRICT";
  public static final String SUB_TYPE_LEASE_3CC = "3CC";
  public static final String SUB_TYPE_LEASE_IPMA = "IPMA";
  public static final String SUB_TYPE_LEASE_LPMA = "LPMA";
  public static final String SUB_TYPE_LEASE_SVR_CONT = "SVR CONT";

  public static final List<String> DEPARTMENT_IDENTIFIERS = Arrays.asList("E-HOST", "EHOST", "DEMO DEVELOPMENT", "DEMO DEV", "DEVELOPMENT",
      "ICC LEASE DEV", "IDL LEASE DEV", "POOL");

  private static final List<String> RELEVANT_ADDRESSES = Arrays.asList(CmrConstants.RDC_SOLD_TO, CmrConstants.RDC_INSTALL_AT);

  private boolean waiting;
  private FindCMRRecordModel ibmCmr;

  public static USBPHandler getBPHandler(EntityManager entityManager, RequestData requestData, AutomationEngineData engineData) {
    Data data = requestData.getData();
    Admin admin = requestData.getAdmin();
    Addr addr = requestData.getAddress("ZS01");
    String custGrp = data.getCustGrp();
    String custSubGrp = data.getCustSubGrp();

    if (USUtil.CG_THIRD_P_BUSINESS_PARTNER.equals(custGrp) || USUtil.CG_THIRD_P_FSP.equals(custGrp)) {
      switch (custSubGrp) {
      case USUtil.SC_FSP_END_USER:
      case USUtil.SC_BP_END_USER:
        return new USBPEndUserHandler();
      case USUtil.SC_FSP_POOL:
      case USUtil.SC_BP_POOL:
        return new USBPPoolHandler();
      case USUtil.SC_BP_DEVELOP:
        return new USBPDevelopHandler();
      case USUtil.SC_BP_E_HOST:
        return new USBPEhostHandler();
      }
    } else if (USUtil.CG_BY_MODEL.equals(custGrp)) {
      String type = admin.getCustType();
      String deptAttn = addr.getDept() != null ? addr.getDept().toLowerCase() : "";
      if (USUtil.BUSINESS_PARTNER.equals(type)) {
        if ("E".equals(data.getBpAcctTyp())
            && (RESTRICT_TO_END_USER.equals(data.getRestrictTo()) || RESTRICT_TO_MAINTENANCE.equals(data.getRestrictTo()))) {
          if (!(deptAttn.contains("ehost") || deptAttn.contains("e-host") || deptAttn.contains("e host"))) {
            return new USBPEndUserHandler();
          } else {
            return new USBPEhostHandler();
          }
        } else if (RESTRICT_TO_END_USER.equals(data.getRestrictTo()) && deptAttn.contains("pool")
            && ("P".equals(data.getBpAcctTyp()) || "TT2".equals(data.getCsoSite()))) {
          return new USBPPoolHandler();
        } else if (RESTRICT_TO_END_USER.equals(data.getRestrictTo())
            && (deptAttn.contains("demo dev") || deptAttn.contains("lease dev") || deptAttn.contains("development"))) {
          return new USBPDevelopHandler();
        }
      } else if (USUtil.LEASING.equals(custGrp)) {
        return new USLeasingHandler();
      }
    } else if (USUtil.CG_THIRD_P_LEASING.equals(custGrp)) {
      return new USLeasingHandler();
    }

    return null;
  }

  /**
   * performs initial validations on request data to determine if a request
   * should be processed by BP logic or not
   * 
   * @param admin
   * @param data
   * @param addr
   * @param output
   * @param engineData
   * @return
   */
  public abstract boolean doInitialValidations(Admin admin, Data data, Addr addr, AutomationResult<OverrideOutput> output,
      AutomationEngineData engineData);

  /**
   * holds the execution logic for the request
   * 
   * returns true if successully processed false if there are any issues/wait
   * flow
   * 
   * @param entityManager
   * @param requestData
   * @param engineData
   * @param output
   * @param details
   * @param childCompleted
   * @param childRequest
   * @param handler
   * @param ibmCmr
   * @param overrides
   * @return
   * @throws Exception
   */
  public abstract boolean processRequest(EntityManager entityManager, RequestData requestData, AutomationEngineData engineData,
      AutomationResult<OverrideOutput> output, StringBuilder details, boolean childCompleted, RequestData childRequest, GEOHandler handler,
      FindCMRRecordModel ibmCmr, OverrideOutput overrides) throws Exception;

  /**
   * Copies the IBM codes from the CMRs and fills the BO codes with the given
   * rules
   * 
   * @param entityManager
   * @param handler
   * @param requestData
   * @param engineData
   * @param details
   * @param overrides
   * @param childRequest
   * @param ibmCmr
   */
  public abstract void copyAndFillIBMData(EntityManager entityManager, GEOHandler handler, RequestData requestData, AutomationEngineData engineData,
      StringBuilder details, OverrideOutput overrides, RequestData childRequest, FindCMRRecordModel ibmCmr);

  /**
   * Performs final Validations on the request before allowing for completion
   * 
   * @param engineData
   * @param requestData
   * @param details
   * @param overrides
   * @param ibmCmr
   * @param result
   */
  public abstract void doFinalValidations(AutomationEngineData engineData, RequestData requestData, StringBuilder details, OverrideOutput overrides,
      FindCMRRecordModel ibmCmr, AutomationResult<OverrideOutput> result);

  /**
   * Filters through the IBM CMR matches found to find the best match
   * 
   * @param engineData
   * @param requestData
   * @param matches
   * @return
   * @throws CmrException
   */
  protected abstract FindCMRRecordModel getIBMCMRBestMatch(AutomationEngineData engineData, RequestData requestData,
      List<DuplicateCMRCheckResponse> matches) throws CmrException;

  /**
   * Matches against SOS-RPA and gets only closely matching records.
   * 
   * @param handler
   * @param requestData
   * @param addr
   * @param engineData
   * @param details
   * @throws Exception
   */
  public void matchAgainstSosRpa(GEOHandler handler, RequestData requestData, Addr addr, AutomationEngineData engineData, StringBuilder details,
      OverrideOutput overrides, boolean hasExistingCmr) throws Exception {
    Scorecard scorecard = requestData.getScorecard();
    scorecard.setRpaMatchingResult("");
    int itr = 0;
    for (String addrType : RELEVANT_ADDRESSES) {
      details.append("SOS-RPA Matching Results for " + addrType + "\n");
      List<SosResponse> sosRpaMatches = USUtil.getSosRpaMatchesForBPEndUser(handler, requestData, engineData, addrType);
      if (!sosRpaMatches.isEmpty()) {
        scorecard.setRpaMatchingResult("Y");
        LOG.debug("Scorecard Updated for SOS-RPA to" + scorecard.getRpaMatchingResult());
        String msg = "Record found in SOS-RPA Service.";
        details.append(msg + "\n");
        SosResponse response = sosRpaMatches.get(0);
        details.append("Record found in SOS.");
        details.append("\nCompany Id = " + (StringUtils.isBlank(response.getCompanyId()) ? "" : response.getCompanyId()));
        details.append("\nCustomer Name = " + (StringUtils.isBlank(response.getLegalName()) ? "" : response.getLegalName()));
        details.append("\nAddress = " + (StringUtils.isBlank(response.getAddress1()) ? "" : response.getAddress1()));
        details.append("\nState = " + (StringUtils.isBlank(response.getState()) ? "" : response.getState()));
        details.append("\nZip = " + (StringUtils.isBlank(response.getZip()) ? "" : response.getZip()) + "\n\n");
        overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "ADMN", "COMP_VERIFIED_INDC", requestData.getAdmin().getCompVerifiedIndc(),
            "Y");
      } else {
        if (itr == 1 && ("N".equals(scorecard.getRpaMatchingResult()) || "".equals(scorecard.getRpaMatchingResult()))) {
          scorecard.setRpaMatchingResult("N");
        }
        LOG.debug("Scorecard Updated for SOS-RPA to" + scorecard.getRpaMatchingResult());
        String msg = "No records found in SOS-RPA Service.";
        details.append(msg + "\n");
        if (hasExistingCmr) {
          overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "ADMN", "COMP_VERIFIED_INDC", requestData.getAdmin().getCompVerifiedIndc(),
              "Y");
          details.append("- A current active CMR exists for the company.");
        }
      }
      itr = 1;
    }
  }

  /**
   * Matches against D&B and gets only closely matching records.
   * 
   * @param handler
   * @param requestData
   * @param addr
   * @param engineData
   * @param details
   * @throws Exception
   */
  public DnBMatchingResponse matchAgainstDnB(GEOHandler handler, RequestData requestData, Addr addr, AutomationEngineData engineData,
      StringBuilder details, OverrideOutput overrides, boolean hasExistingCmr) throws Exception {
    Scorecard scorecard = requestData.getScorecard();
    scorecard.setDnbMatchingResult("");
    List<DnBMatchingResponse> dnbMatches = USUtil.getMatchesForBPEndUser(handler, requestData, engineData);
    if (dnbMatches.isEmpty()) {
      scorecard.setDnbMatchingResult("N");
      LOG.debug("No D&B matches found for the End User " + addr.getDivn()
          + ((!StringUtils.isBlank(addr.getDept()) && useDeptForMatching(requestData)) ? " " + addr.getDept() : ""));
      String msg = "No high quality D&B matches for the End User " + addr.getDivn()
          + ((!StringUtils.isBlank(addr.getDept()) && useDeptForMatching(requestData)) ? " " + addr.getDept() : "");
      details.append(msg + "\n");
      if (hasExistingCmr) {
        overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "ADMN", "COMP_VERIFIED_INDC", requestData.getAdmin().getCompVerifiedIndc(),
            "Y");
        details.append("- A current active CMR exists for the company.");
      } else {
        performAction(engineData, msg);
      }
      details.append("\n");
      return null;
    } else {
      scorecard.setDnbMatchingResult("Y");
      DnBMatchingResponse dnbMatch = dnbMatches.get(0);
      LOG.debug("D&B match found for " + addr.getDivn()
          + ((!StringUtils.isBlank(addr.getDept()) && useDeptForMatching(requestData)) ? " " + addr.getDept() : "") + " with DUNS "
          + dnbMatch.getDunsNo());
      details.append("D&B match/es found for the End User " + addr.getDivn()
          + ((!StringUtils.isBlank(addr.getDept()) && useDeptForMatching(requestData)) ? " " + addr.getDept() : "") + ". Highest Match:\n");
      details.append("\n");
      details.append(dnbMatch.getDnbName() + "\n");
      details.append(dnbMatch.getDnbStreetLine1() + "\n");
      if (!StringUtils.isBlank(dnbMatch.getDnbStreetLine2())) {
        details.append(dnbMatch.getDnbStreetLine2() + "\n");
      }
      overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "ADMN", "COMP_VERIFIED_INDC", requestData.getAdmin().getCompVerifiedIndc(), "Y");
      details.append(dnbMatch.getDnbCity() + ", " + dnbMatch.getDnbStateProv() + "\n");
      details.append(dnbMatch.getDnbCountry() + " " + dnbMatch.getDnbPostalCode() + "\n\n");
      return dnbMatch;
    }

  }

  protected void performAction(AutomationEngineData engineData, String msg) {
    engineData.addNegativeCheckStatus("_usBpNoMatch", msg);
  }

  /**
   * 
   * overrides address info from child request/ CMR to the BP request
   * 
   * @param entityManager
   * @param handler
   * @param requestData
   * @param engineData
   * @param details
   * @param overrides
   * @param childRequest
   */
  public void createAddressOverrides(EntityManager entityManager, GEOHandler handler, RequestData requestData, AutomationEngineData engineData,
      StringBuilder details, OverrideOutput overrides, RequestData childRequest, FindCMRRecordModel ibmCmr) {
    Addr installAt = requestData.getAddress("ZS01");
    boolean createAddressOverrides = false;
    String mainCustNm1 = "";
    String mainCustNm2 = "";
    String streetAddress1 = "";
    String streetAddress2 = "";
    String city1 = "";
    String postalCd = "";
    String stateProv = "";
    String county = "";
    String countyNm = "";
    String phone = "";
    String fax = "";
    String transportZone = "";
    String district = "";
    String building = "";
    String floor = "";

    if (childRequest != null && "COM".equals(childRequest.getAdmin().getReqStatus())) {
      Addr childInstallAt = childRequest.getAddress("ZS01");
      Admin childAdmin = childRequest.getAdmin();
      mainCustNm1 = childAdmin.getMainCustNm1();
      mainCustNm2 = childAdmin.getMainCustNm2();
      streetAddress1 = childInstallAt.getAddrTxt();
      streetAddress2 = childInstallAt.getAddrTxt2();
      city1 = childInstallAt.getCity1();
      postalCd = childInstallAt.getPostCd();
      stateProv = childInstallAt.getStateProv();
      county = childInstallAt.getCounty();
      countyNm = childInstallAt.getCountyName();
      phone = childInstallAt.getCustPhone();
      fax = childInstallAt.getCustFax();
      transportZone = childInstallAt.getTransportZone();
      district = childInstallAt.getCity2();
      building = childInstallAt.getBldg();
      floor = childInstallAt.getFloor();
      createAddressOverrides = true;
    } else if (ibmCmr != null) {
      // CMR-4033
      String name = (StringUtils.isNotBlank(ibmCmr.getCmrName1Plain()) ? ibmCmr.getCmrName1Plain() : "")
          + (StringUtils.isNotBlank(ibmCmr.getCmrName2Plain()) ? " " + ibmCmr.getCmrName2Plain() : "");
      String parts[] = handler.doSplitName(name, "", 24, 24);
      mainCustNm1 = parts[0];
      mainCustNm2 = parts[1];
      parts = handler.doSplitAddress(ibmCmr.getCmrStreetAddress(), ibmCmr.getCmrStreetAddressCont(), 24, 24);
      streetAddress1 = parts[0];
      streetAddress2 = parts[1];
      city1 = ibmCmr.getCmrCity();
      postalCd = ibmCmr.getCmrPostalCode();
      stateProv = ibmCmr.getCmrState();
      county = ibmCmr.getCmrCountyCode();
      countyNm = ibmCmr.getCmrCounty();
      phone = ibmCmr.getCmrCustPhone();
      fax = ibmCmr.getCmrCustFax();
      transportZone = ibmCmr.getCmrTransportZone();
      district = ibmCmr.getCmrCity2();
      building = ibmCmr.getCmrBldg();
      floor = ibmCmr.getCmrFloor();
      createAddressOverrides = true;
    }

    if (createAddressOverrides) {

      if (!StringUtils.equals(installAt.getDivn(), mainCustNm1) || !StringUtils.equals(installAt.getDept(), mainCustNm2)) {

        String customerName = installAt.getDivn() + (!StringUtils.isBlank(installAt.getDept()) ? " " + installAt.getDept() : "");
        customerName = customerName.toUpperCase();
        String customerNameSuffix = "";
        if (customerName.contains("CHN SVCS")) {
          customerNameSuffix = customerName.substring(customerName.lastIndexOf("CHN SVCS")).trim();
          customerName = customerName.substring(0, customerName.lastIndexOf("CHN SVCS")).trim();
        } else if (customerName.contains("CHANNEL SVCS")) {
          customerNameSuffix = customerName.substring(customerName.lastIndexOf("CHANNEL SVCS")).trim();
          customerName = customerName.substring(0, customerName.lastIndexOf("CHANNEL SVCS")).trim();
        } else if (customerName.contains("CHANNEL SVCS USE ONLY")) {
          customerNameSuffix = customerName.substring(customerName.lastIndexOf("CHANNEL SVCS USE ONLY")).trim();
          customerName = customerName.substring(0, customerName.lastIndexOf("CHANNEL SVCS USE ONLY")).trim();
        } else if (customerName.contains("C/O")) {
          customerNameSuffix = customerName.substring(customerName.lastIndexOf("C/O")).trim();
          customerName = customerName.substring(0, customerName.lastIndexOf("C/O")).trim();
        } else if (customerName.contains("UCW")) {
          customerNameSuffix = customerName.substring(customerName.lastIndexOf("UCW")).trim();
          customerName = customerName.substring(0, customerName.lastIndexOf("UCW")).trim();
        }

        String fullName = mainCustNm1;
        if (!StringUtils.isBlank(mainCustNm2)) {
          fullName += " " + mainCustNm2;
        }
        String nameParts[] = handler.doSplitName(fullName, "", 24, 24);
        // CMR-4002 always put suffix on dept line
        nameParts[1] = (nameParts[1] != null ? nameParts[1] : "") + (StringUtils.isBlank(customerNameSuffix) ? "" : " " + customerNameSuffix);
        nameParts[1] = nameParts[1].trim();

        if (nameParts[1].length() > 24) {
          details.append("Value for Department Line exceeds 24 characters. Final value needs to be reviewed.\n");
          engineData.addNegativeCheckStatus("_deptLengthExceeded",
              "Value for Department Line exceeds 24 characters. Final value needs to be reviewed.");
        }

        overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "ZS01", "DIVN", installAt.getDivn(),
            StringUtils.isNotBlank(nameParts[0]) ? nameParts[0] : "");
        details.append("Updated Division to " + (StringUtils.isNotBlank(nameParts[0]) ? nameParts[0] : "-blank-")).append("\n");

        if (useDeptForMatching(requestData)) {
          overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "ZS01", "DEPT", installAt.getDept(),
              StringUtils.isNotBlank(nameParts[1]) ? nameParts[1] : "");
          details.append("Updated Department to " + (StringUtils.isNotBlank(nameParts[1]) ? nameParts[1] : "-blank-")).append("\n");
        }
      }

      // if (StringUtils.isBlank(installAt.getDept()) &&
      // StringUtils.isNotBlank(dept)) {
      // overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS,
      // "ZS01",
      // "DEPT", installAt.getDept(), dept);
      // details.append("Updated Department to " + dept).append("\n");
      // }

      if (!StringUtils.equals(installAt.getAddrTxt(), streetAddress1)) {
        overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "ZS01", "ADDR_TXT", installAt.getAddrTxt(),
            StringUtils.isNotBlank(streetAddress1) ? streetAddress1.trim() : "");
        details.append("Updated Street Address1 to " + (StringUtils.isNotBlank(streetAddress1) ? streetAddress1.trim() : "-blank-")).append("\n");
      }
      if (!StringUtils.equals(installAt.getAddrTxt2(), streetAddress2)) {
        overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "ZS01", "ADDR_TXT_2", installAt.getAddrTxt2(),
            StringUtils.isNotBlank(streetAddress2) ? streetAddress2.trim() : "");
        details.append("Updated Street Address2 to " + (StringUtils.isNotBlank(streetAddress2) ? streetAddress2.trim() : "-blank-")).append("\n");
      }
      if (!StringUtils.equals(installAt.getCity1(), city1)) {
        overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "ZS01", "CITY1", installAt.getCity1(),
            StringUtils.isNotBlank(city1) ? city1.trim() : "");
        details.append("Updated City to " + (StringUtils.isNotBlank(city1) ? city1.trim() : "-blank-")).append("\n");
      }
      if (!StringUtils.equals(installAt.getPostCd(), postalCd)) {
        overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "ZS01", "POST_CD", installAt.getPostCd(),
            StringUtils.isNotBlank(postalCd) ? postalCd.trim() : "");
        details.append("Updated Postal Code to " + (StringUtils.isNotBlank(postalCd) ? postalCd.trim() : "-blank-")).append("\n");
      }
      if (!StringUtils.equals(installAt.getStateProv(), stateProv)) {
        overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "ZS01", "STATE_PROV", installAt.getStateProv(),
            StringUtils.isNotBlank(stateProv) ? stateProv.trim() : "");
        details.append("Updated State/Prov to " + (StringUtils.isNotBlank(stateProv) ? stateProv.trim() : "-blank-")).append("\n");
      }
      if (!StringUtils.equals(installAt.getCounty(), county)) {
        overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "ZS01", "COUNTY", installAt.getCounty(),
            StringUtils.isNotBlank(county) ? county.trim() : "");
      }

      if (!StringUtils.equals(installAt.getCounty(), county)) {
        overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "ZS01", "COUNTY_NAME", installAt.getCountyName(),
            StringUtils.isNotBlank(countyNm) ? countyNm.trim() : "");
        details.append("Updated County to " + (StringUtils.isNotBlank(countyNm) ? countyNm.trim() : "-blank-")).append("\n");
      }

      if (!StringUtils.equals(installAt.getCustPhone(), phone)) {
        overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "ZS01", "CUST_PHONE", installAt.getCustPhone(),
            StringUtils.isNotBlank(phone) ? phone.trim() : "");
        // details.append("Updated Phone to " +
        // (StringUtils.isNotBlank(phone) ?
        // phone.trim() : "-blank-")).append("\n");
      }

      if (!StringUtils.equals(installAt.getCustFax(), fax)) {
        overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "ZS01", "CUST_FAX", installAt.getCustFax(),
            StringUtils.isNotBlank(fax) ? fax.trim() : "");
        // details.append("Updated Fax to " +
        // (StringUtils.isNotBlank(fax) ?
        // fax.trim() : "-blank-")).append("\n");
      }
      if (!StringUtils.equals(installAt.getCity2(), district)) {
        overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "ZS01", "CITY2", installAt.getCity2(),
            StringUtils.isNotBlank(district) ? district.trim() : "");
        // details.append("Updated District to " +
        // (StringUtils.isNotBlank(district) ? district.trim() :
        // "-blank-")).append("\n");
      }
      if (!StringUtils.equals(installAt.getBldg(), building)) {
        overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "ZS01", "BLDG", installAt.getBldg(),
            StringUtils.isNotBlank(building) ? building.trim() : "");
        // details.append("Updated Building to " +
        // (StringUtils.isNotBlank(building) ? building.trim() :
        // "-blank-")).append("\n");
      }
      if (!StringUtils.equals(installAt.getFloor(), floor)) {
        overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "ZS01", "FLOOR", installAt.getFloor(),
            StringUtils.isNotBlank(floor) ? floor.trim() : "");
        // details.append("Updated Floor to " +
        // (StringUtils.isNotBlank(floor) ?
        // floor.trim() : "-blank-")).append("\n");
      }
      if (!StringUtils.equals(installAt.getTransportZone(), transportZone)) {
        overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "ZS01", "TRANSPORT_ZONE", installAt.getTransportZone(),
            StringUtils.isNotBlank(transportZone) ? transportZone.trim() : "");
        // details.append("Updated Transport Zone to " +
        // (StringUtils.isNotBlank(transportZone) ? transportZone.trim()
        // :
        // "-blank-")).append("\n");
      }
    }
    invoiceToOverrides(requestData, overrides, entityManager);
  }

  public void invoiceToOverrides(RequestData requestData, OverrideOutput overrides, EntityManager entityManager) {
    // NOOP
  }

  /**
   * Creates a {@link FindCMRRecordModel} copy from the Child Request
   * 
   * @param childRequest
   * @return
   */
  public FindCMRRecordModel createIBMCMRFromChild(RequestData childRequest, EntityManager entityManager) {
    if (childRequest == null || !"COM".equals(childRequest.getAdmin().getReqStatus())) {
      return null;
    }
    FindCMRRecordModel ibmCmrModel = new FindCMRRecordModel();
    Data completedChildData = childRequest.getData();
    // make sure we switch the codes to use directly from child
    ibmCmrModel.setCmrNum(completedChildData.getCmrNo());
    String affiliate = completedChildData.getAffiliate();

    String processingType = getProcessingTypeForUS(entityManager, "897");

    if (StringUtils.isBlank(affiliate) && "TC".equals(processingType)) {
      affiliate = getUSCMRAffiliate(completedChildData.getCmrNo());
    }
    ibmCmrModel.setCmrAffiliate(affiliate);

    String childIsic = completedChildData.getIsicCd();
    boolean federalPoa = childIsic != null && (childIsic.startsWith("90") || childIsic.startsWith("91") || childIsic.startsWith("92"));
    if (federalPoa) {
      String enterprise = completedChildData.getEnterprise();
      if (StringUtils.isBlank(enterprise) && "TC".equals(processingType)) {
        enterprise = getUSCMREnterprise(completedChildData.getCmrNo());
      }
      ibmCmrModel.setCmrEnterpriseNumber(enterprise);
    }

    ibmCmrModel.setCmrIsu(completedChildData.getIsuCd());
    ibmCmrModel.setCmrTier(completedChildData.getClientTier());
    ibmCmrModel.setCmrInac(completedChildData.getInacCd());
    ibmCmrModel.setCmrInacType(completedChildData.getInacType());
    ibmCmrModel.setCmrSubIndustry(completedChildData.getSubIndustryCd());
    ibmCmrModel.setCmrIsic(completedChildData.getUsSicmen());
    Addr childInstallAt = childRequest.getAddress("ZS01");
    ibmCmrModel.setCmrCountyCode(childInstallAt.getCounty());
    ibmCmrModel.setCmrCounty(childInstallAt.getCountyName());
    ibmCmrModel.setCmrName4(childInstallAt.getCustNm4());
    ibmCmrModel.setCmrStreetAddress(
        childInstallAt.getAddrTxt() + (StringUtils.isNotBlank(childInstallAt.getAddrTxt2()) ? childInstallAt.getAddrTxt2() : ""));
    ibmCmrModel.setCmrCity(childInstallAt.getCity1());
    ibmCmrModel.setCmrPostalCode(childInstallAt.getPostCd());
    ibmCmrModel.setCmrState(childInstallAt.getStateProv());
    ibmCmrModel.setCmrCountryLanded(childInstallAt.getLandCntry());
    ibmCmrModel.setCmrDept(childInstallAt.getDept());
    ibmCmrModel.setCmrCustPhone(childInstallAt.getCustPhone());
    ibmCmrModel.setCmrCustFax(childInstallAt.getCustFax());
    ibmCmrModel.setCmrTransportZone(childInstallAt.getTransportZone());
    ibmCmrModel.setCmrCity2(childInstallAt.getCity2());
    ibmCmrModel.setCmrBldg(childInstallAt.getBldg());
    ibmCmrModel.setCmrFloor(childInstallAt.getFloor());
    ibmCmrModel.setCmrBuyingGroup(completedChildData.getBgId());
    ibmCmrModel.setCmrBuyingGroupDesc(completedChildData.getBgDesc());
    ibmCmrModel.setCmrGlobalBuyingGroup(completedChildData.getGbgId());
    ibmCmrModel.setCmrGlobalBuyingGroupDesc(completedChildData.getGbgDesc());
    ibmCmrModel.setCmrLde(completedChildData.getBgRuleId());
    ibmCmrModel.setCmrCoverage(completedChildData.getCovId());
    ibmCmrModel.setCmrCoverageName(completedChildData.getCovDesc());
    ibmCmrModel.setCmrName1Plain(childRequest.getAdmin().getMainCustNm1());
    ibmCmrModel.setCmrName2Plain(childRequest.getAdmin().getMainCustNm2());
    ibmCmrModel.setUsCmrCsoSite(completedChildData.getCsoSite());
    ibmCmrModel.setUsCmrMktgArDept(completedChildData.getMtkgArDept());

    return ibmCmrModel;
  }

  /**
   * Finds an IBM direct CMR for the end user
   * 
   * @param handler
   * @param requestData
   * @param addr
   * @param engineData
   * @return
   * @throws InvocationTargetException
   * @throws IllegalArgumentException
   * @throws IllegalAccessException
   * @throws InstantiationException
   * @throws SecurityException
   * @throws NoSuchMethodException
   */
  public FindCMRRecordModel findIBMCMR(EntityManager entityManager, GEOHandler handler, RequestData requestData, Addr addr,
      AutomationEngineData engineData, String directCmrNo) throws Exception {

    if (!StringUtils.isBlank(directCmrNo)) {
      LOG.debug("Checking details of CMR No. " + directCmrNo);
      FindCMRResultModel result = SystemUtil.findCMRs(directCmrNo, SystemLocation.UNITED_STATES, 5);
      if (result != null && result.getItems() != null && !result.getItems().isEmpty()) {
        Collections.sort(result.getItems());
        LOG.debug(" - record found via FindCMR");
        return result.getItems().get(0);
      }
    } else {
      LOG.debug("Checking IBM direct CMR for " + addr.getDivn());

      String customerName = addr.getDivn() + ((!StringUtils.isBlank(addr.getDept()) && useDeptForMatching(requestData)) ? " " + addr.getDept() : "");
      customerName = customerName.toUpperCase();
      if (customerName.contains("CHN SVCS")) {
        customerName = customerName.substring(0, customerName.lastIndexOf("CHN SVCS")).trim();
      } else if (customerName.contains("CHANNEL SVCS")) {
        customerName = customerName.substring(0, customerName.lastIndexOf("CHANNEL SVCS")).trim();
      } else if (customerName.contains("CHANNEL SVCS USE ONLY")) {
        customerName = customerName.substring(0, customerName.lastIndexOf("CHANNEL SVCS USE ONLY")).trim();
      } else if (customerName.contains("C/O")) {
        customerName = customerName.substring(0, customerName.lastIndexOf("C/O")).trim();
      } else if (customerName.contains("UCW")) {
        customerName = customerName.substring(0, customerName.lastIndexOf("UCW")).trim();
      }

      DuplicateCMRCheckRequest request = new DuplicateCMRCheckRequest();
      request.setIssuingCountry(SystemLocation.UNITED_STATES);
      request.setLandedCountry(addr.getLandCntry());
      request.setCustomerName(customerName);
      request.setStreetLine1(addr.getAddrTxt());
      request.setStreetLine2(addr.getAddrTxt2());
      request.setCity(addr.getCity1());
      request.setStateProv(addr.getStateProv());
      request.setPostalCode(addr.getPostCd());
      request.setAddrType("ZS01");

      tweakFindCMRRequest(entityManager, handler, requestData, request);

      MatchingServiceClient client = CmrServicesFactory.getInstance().createClient(SystemConfiguration.getValue("BATCH_SERVICES_URL"),
          MatchingServiceClient.class);
      client.setReadTimeout(1000 * 60 * 5);

      JSONObject retObj = client.execute(MatchingServiceClient.CMR_SERVICE_ID, request);
      ObjectMapper mapper = new ObjectMapper();
      String rawJson = mapper.writeValueAsString(retObj);
      TypeReference<MatchingResponse<DuplicateCMRCheckResponse>> typeRef = new TypeReference<MatchingResponse<DuplicateCMRCheckResponse>>() {
      };
      MatchingResponse<DuplicateCMRCheckResponse> res = mapper.readValue(rawJson, typeRef);
      if (res != null && res.getSuccess() && res.getMatched()) {
        LOG.debug("Matches found for request " + requestData.getAdmin().getId().getReqId() + " with customer Name " + customerName);
        LOG.debug("Checking best Cmr matches");
        List<DuplicateCMRCheckResponse> cmrCheckMatches = res.getMatches();
        if (cmrCheckMatches.size() != 0) {
          LOG.debug(res.getMessage());
          for (DuplicateCMRCheckResponse cmrCheckRecord : cmrCheckMatches) {
            if (!StringUtils.isBlank(cmrCheckRecord.getCmrNo())) {
              LOG.debug("CMR Number = " + cmrCheckRecord.getCmrNo());
            }
          }
        }
        return getIBMCMRBestMatch(engineData, requestData, res.getMatches());
      } else {
        LOG.debug("Response matches" + res.getMatched());
        LOG.debug("Response success" + res.getSuccess());
        LOG.debug("No matches found for request " + requestData.getAdmin().getId().getReqId() + " with customer Name " + customerName);
      }
    }

    // for now use findcmr directly for matching
    boolean doSecondaryChecks = false;

    if (doSecondaryChecks) {
      // do a secondary check directly from RDC
      LOG.debug("No CMR found using duplicate checks, querying database directly..");
      LOG.debug("No IBM Direct CMRs found in FindCMR. Checking directly on the database..");
      String sql = ExternalizedQuery.getSql("AUTO.US.GET_IBM_DIRECT");

      if (!StringUtils.isBlank(directCmrNo)) {
        sql = ExternalizedQuery.getSql("AUTO.US.GET_IBM_DIRECT");
      }
      PreparedQuery query = new PreparedQuery(entityManager, sql);
      query.setForReadOnly(true);
      query.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
      query.setParameter("NAME", addr.getDivn().toUpperCase() + "%");
      query.setParameter("CMR_NO", directCmrNo);

      List<Object[]> results = query.getResults(1);
      if (results != null && !results.isEmpty()) {
        Object[] cmr = results.get(0);
        // 0 - affiliate
        // 1 - client tier
        // 2 - ISU
        // 3 - INAC type
        // 4 - INAC code
        // 5 - KUNNR
        // 6 - CMR No.
        // 7 - ISIC
        // 8 - sub industry
        FindCMRRecordModel record = new FindCMRRecordModel();
        record.setCmrAffiliate((String) cmr[0]);
        record.setCmrTier((String) cmr[1]);
        record.setCmrIsu((String) cmr[2]);
        record.setCmrInacType((String) cmr[3]);
        record.setCmrInac((String) cmr[4]);
        record.setCmrSapNumber((String) cmr[5]);
        record.setCmrNum((String) cmr[6]);
        record.setCmrIsic((String) cmr[7]);
        record.setCmrSubIndustry((String) cmr[8]);
        LOG.debug(" - found CMR from DB: " + record.getCmrNum());
        return record;
      }
    }
    LOG.debug("No IBM Direct CMRs found.");
    return null;
  }

  protected void tweakFindCMRRequest(EntityManager entityManager, GEOHandler handler, RequestData requestData, DuplicateCMRCheckRequest request) {
    // NOOP
  }

  /**
   * Checks if the cmrNo has a blank restriction code in US CMR DB
   * 
   * @param cmrNo
   * @return
   */
  protected boolean hasBlankRestrictionCodeInUSCMR(String cmrNo) {

    EntityManager emgp = JpaManager.getEntityManager();
    try {
      String processingType = getProcessingTypeForUS(emgp, "897");

      if ("TC".equals(processingType)) {
        String usSchema = SystemConfiguration.getValue("US_CMR_SCHEMA");
        String sql = ExternalizedQuery.getSql("AUTO.USBP.CHECK_RESTRICTION", usSchema);
        sql = StringUtils.replace(sql, ":CMR_NO", cmrNo);
        System.err.println(sql);
        QueryRequest query = new QueryRequest();
        query.setSql(sql);
        query.setRows(1);
        query.addField("C_COM_RESTRCT_CODE");
        query.addField("I_CUST_ENTITY");

        try {
          String url = SystemConfiguration.getValue("CMR_SERVICES_URL");
          QueryClient client = CmrServicesFactory.getInstance().createClient(url, QueryClient.class);
          QueryResponse response = client.executeAndWrap(QueryClient.USCMR_APP_ID, query, QueryResponse.class);

          if (!response.isSuccess()) {
            LOG.warn("Not successful executiong of US CMR query: " + response.getMsg());
            return true;
          } else if (response.getRecords() == null || response.getRecords().size() == 0) {
            LOG.warn("No records in US CMR DB");
            return true;
          } else {
            Map<String, Object> record = response.getRecords().get(0);
            return StringUtils.isBlank((String) record.get("C_COM_RESTRCT_CODE"));
          }
        } catch (Exception e) {
          LOG.warn("Error in executing US CMR query", e);
          return true;
        }
      } else {
        return true;
      }
    } catch (Exception e) {
      LOG.warn("Error in get processing type", e);
      return true;
    }

  }

  /**
   * Gets the Affiliate from US CMR
   * 
   * @param cmrNo
   * @return
   */
  protected String getUSCMRAffiliate(String cmrNo) {
    String usSchema = SystemConfiguration.getValue("US_CMR_SCHEMA");
    String sql = ExternalizedQuery.getSql("AUTO.US.USCMR.AFFILIATE", usSchema);
    sql = StringUtils.replace(sql, ":CMR_NO", cmrNo);
    QueryRequest query = new QueryRequest();
    query.setSql(sql);
    query.setRows(1);
    query.addField("I_MKT_AFFLTN");
    query.addField("I_CUST_ENTITY");

    try {
      String url = SystemConfiguration.getValue("CMR_SERVICES_URL");
      QueryClient client = CmrServicesFactory.getInstance().createClient(url, QueryClient.class);
      QueryResponse response = client.executeAndWrap(QueryClient.USCMR_APP_ID, query, QueryResponse.class);

      if (!response.isSuccess()) {
        LOG.warn("Not successful executiong of US CMR query");
        return null;
      } else if (response.getRecords() == null || response.getRecords().size() == 0) {
        LOG.warn("No records in US CMR DB");
        return null;
      } else {
        Map<String, Object> record = response.getRecords().get(0);
        String affiliate = (String) record.get("I_MKT_AFFLTN");
        if (!StringUtils.isBlank(affiliate)) {
          return StringUtils.leftPad(affiliate, 7, '0');
        }
        return null;
      }
    } catch (Exception e) {
      LOG.warn("Error in executing US CMR query", e);
      return null;
    }

  }

  /**
   * Gets the Enterprise from US CMR
   * 
   * @param cmrNo
   * @return
   */
  protected String getUSCMREnterprise(String cmrNo) {
    String usSchema = SystemConfiguration.getValue("US_CMR_SCHEMA");
    String sql = ExternalizedQuery.getSql("AUTO.US.USCMR.ENTERPRISE", usSchema);
    sql = StringUtils.replace(sql, ":CMR_NO", cmrNo);
    QueryRequest query = new QueryRequest();
    query.setSql(sql);
    query.setRows(1);
    query.addField("I_ENT");
    query.addField("I_CUST_ENTITY");

    try {
      String url = SystemConfiguration.getValue("CMR_SERVICES_URL");
      QueryClient client = CmrServicesFactory.getInstance().createClient(url, QueryClient.class);
      QueryResponse response = client.executeAndWrap(QueryClient.USCMR_APP_ID, query, QueryResponse.class);

      if (!response.isSuccess()) {
        LOG.warn("Not successful executiong of US CMR query");
        return null;
      } else if (response.getRecords() == null || response.getRecords().size() == 0) {
        LOG.warn("No records in US CMR DB");
        return null;
      } else {
        Map<String, Object> record = response.getRecords().get(0);
        String enterprise = (String) record.get("I_ENT");
        if (!StringUtils.isBlank(enterprise)) {
          return StringUtils.leftPad(enterprise, 7, '0');
        }
        return null;
      }
    } catch (Exception e) {
      LOG.warn("Error in executing US CMR query", e);
      return null;
    }

  }

  /**
   * Processes cancellation of the child request and setting parent to correct
   * statuses
   * 
   * @param entityManager
   * @param details
   * @param requestData
   * @param childRequestData
   * @throws SQLException
   * @throws CmrException
   */
  public void processChildCancellation(EntityManager entityManager, StringBuilder details, RequestData requestData, RequestData childRequestData,
      AutomationEngineData engineData, WfHist rejectionHist) throws CmrException, SQLException {
    // set child to CANCELLED
    long childReqId = childRequestData.getAdmin().getId().getReqId();
    LOG.debug("Cancelling child request " + childReqId);
    childRequestData.getAdmin().setReqStatus("CAN");
    RequestUtils.createWorkflowHistoryFromBatch(entityManager, "AutomationEngine", childRequestData.getAdmin(),
        "Automatically cancelled by CreateCMR", "Cancel", null, null, true, false, null);

    if (rejectionHist != null) {

      String rejectCmt = StringUtils.isBlank(rejectionHist.getCmt()) ? "Request rejected by processor. Please check comments."
          : rejectionHist.getCmt();
      if ("DUPR".equals(rejectionHist.getRejReasonCd())) {
        rejectCmt = "An ongoing request for the CMR is being processed at the moment. Please try to resubmit the request after 15 mins.";
      }
      engineData.addRejectionComment(rejectionHist.getRejReasonCd(), rejectCmt, rejectionHist.getRejSupplInfo1(), rejectionHist.getRejSupplInfo2());
      requestData.getAdmin().setChildReqId(0);

      // copy comments
      LOG.debug("Copying comments from Child Request " + childReqId);
      String sql = ExternalizedQuery.getSql("AUTO.US.GET_COMMENTS");
      PreparedQuery query = new PreparedQuery(entityManager, sql);
      query.setParameter("REQ_ID", childReqId);
      query.setForReadOnly(true);
      List<ReqCmtLog> cmts = query.getResults(ReqCmtLog.class);
      if (cmts != null) {
        for (ReqCmtLog cmt : cmts) {
          RequestUtils.createCommentLogFromBatch(entityManager, cmt.getCreateById(), requestData.getAdmin().getId().getReqId(), cmt.getCmt());
        }
      }

      // copy attachments
      LOG.debug("Copying attachments from Child Request " + childReqId);
      try {
        sql = ExternalizedQuery.getSql("AUTO.US.GET_ATTACHMENTS");
        query = new PreparedQuery(entityManager, sql);
        query.setParameter("REQ_ID", childReqId);
        query.setForReadOnly(true);
        List<Attachment> attachments = query.getResults(Attachment.class);
        if (attachments != null) {
          AttachmentService attachService = new AttachmentService();
          long parentReqId = requestData.getAdmin().getId().getReqId();
          AppUser user = null;
          for (Attachment attachment : attachments) {
            File file = new File(attachment.getId().getDocLink());
            if (file.exists()) {
              try (FileInputStream fis = new FileInputStream(file)) {
                user = new AppUser();
                user.setIntranetId(attachment.getDocAttachById());
                user.setBluePagesName(attachment.getDocAttachByNm());
                LOG.debug("Attaching " + file.getName() + " to Request " + parentReqId);
                attachService.addExternalAttachment(entityManager, user, parentReqId, attachment.getDocContent(), file.getName(), attachment.getCmt(),
                    fis);
              }
            }
          }
        }
      } catch (Exception e) {
        LOG.warn("Attachments cannot be copied.");
        details.append("Attachments cannot be copied to the request due to an error.\n");
      }

    }
  }

  /**
   * Gets the CMR no from the child rejection. If null, it means the rejection
   * is not due to duplicate CMR
   * 
   * @param reqId
   * @return
   */
  public WfHist getWfHistForRejection(EntityManager entityManager, long reqId) {
    String sql = ExternalizedQuery.getSql("AUTO.US.GET_REJECTION");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", reqId);
    query.setForReadOnly(true);
    return query.getSingleResult(WfHist.class);
  }

  /**
   * Checks if the current BP is a T1 from PPS Service
   * 
   * @param data
   * @return
   */
  public boolean isTier1BP(Data data) {

    USCeIdMapping mapping = null;
    String enterpriseNo = data.getEnterprise();
    String ceId = data.getPpsceid();

    if (!StringUtils.isBlank(enterpriseNo)) {
      mapping = USCeIdMapping.getByEnterprise(enterpriseNo);
    }
    if (mapping != null) {
      // registered BPs are T1s
      return true;
    }

    if (!StringUtils.isBlank(ceId)) {
      mapping = USCeIdMapping.getByCeid(ceId);
      if (mapping != null) {
        // registered BPs are T1s
        return true;
      }
      String url = SystemConfiguration.getValue("CMR_SERVICES_URL");
      LOG.debug("CE ID or Enterprise not mapped to a registered BP. Checking");
      try {
        PPSServiceClient client = CmrServicesFactory.getInstance().createClient(url, PPSServiceClient.class);
        client.setRequestMethod(Method.Get);
        client.setReadTimeout(1000 * 60 * 5);
        PPSRequest request = new PPSRequest();
        request.setCeid(ceId);
        PPSResponse response = client.executeAndWrap(request, PPSResponse.class);
        if (response == null || !response.isSuccess()) {
          LOG.warn("PPS error encountered.");
          return false;
        }
        for (PPSProfile profile : response.getProfiles()) {
          if ("1".equals(profile.getTierCode())) {
            LOG.debug("BP " + response.getCompanyName() + " is a T1.");
            return true;
          }
        }
      } catch (Exception e) {
        LOG.warn("PPS error encountered.", e);
        return false;
      }
    }
    return false;
  }

  /**
   * Checks if the current BP is a T2 from PPS Service
   * 
   * @param data
   * @return
   */
  public boolean isTier2BP(Data data) {
    String ceId = data.getPpsceid();

    if (!StringUtils.isBlank(ceId)) {
      String url = SystemConfiguration.getValue("CMR_SERVICES_URL");
      LOG.debug("CE ID or Enterprise not mapped to a registered BP. Checking");
      try {
        PPSServiceClient client = CmrServicesFactory.getInstance().createClient(url, PPSServiceClient.class);
        client.setRequestMethod(Method.Get);
        client.setReadTimeout(1000 * 60 * 5);
        PPSRequest request = new PPSRequest();
        request.setCeid(ceId);
        PPSResponse response = client.executeAndWrap(request, PPSResponse.class);
        if (response == null || !response.isSuccess()) {
          LOG.warn("PPS error encountered.");
          return false;
        }
        for (PPSProfile profile : response.getProfiles()) {
          if ("2".equals(profile.getTierCode())) {
            LOG.debug("BP " + response.getCompanyName() + " is a T2.");
            return true;
          }
        }
      } catch (Exception e) {
        LOG.warn("PPS error encountered.", e);
        return false;
      }
    }
    return false;
  }

  /**
   * Checks if the child request is completed or not
   * 
   * @param entityManager
   * @param engineData
   * @param requestData
   * @param childRequest
   * @param details
   * @param output
   * @param ibmCmr
   * @param handler
   * @return
   * @throws Exception
   */
  public boolean checkIfChildRequestCompleted(EntityManager entityManager, AutomationEngineData engineData, RequestData requestData,
      RequestData childRequest, StringBuilder details, AutomationResult<OverrideOutput> output, GEOHandler handler) throws Exception {
    long childReqId = childRequest.getAdmin().getId().getReqId();
    Addr addr = requestData.getAddress("ZS01");

    // check child reqId processing first
    LOG.debug("Getting child request data for Request " + childReqId);

    if (childRequest == null || childRequest.getAdmin() == null) {
      String msg = "Child request " + childReqId + " missing. The request needs to be manually processed.";
      details.append(msg);
      details.append("\n");
      details.append("No field value has been computed for this record.");
      engineData.addRejectionComment("OTH", msg, "", "");
      output.setOnError(true);
      output.setDetails(details.toString());
      output.setResults("Issues Encountered");
      return false;
    }
    String childReqStatus = childRequest.getAdmin().getReqStatus();
    if ("PRJ".equals(childReqStatus) || "DRA".equals(childReqStatus) || "CAN".endsWith(childReqStatus)) {
      boolean setError = true;
      String rejectionCmrNo = null;
      WfHist rejectHist = null;
      if ("PRJ".equals(childReqStatus)) {
        rejectHist = getWfHistForRejection(entityManager, childReqId);
        if (rejectHist != null) {
          // Duplicate CMR
          if ("DUPC".equals(rejectHist.getRejReasonCd())) {
            rejectionCmrNo = rejectHist.getRejSupplInfo1();
            if (!StringUtils.isBlank(rejectionCmrNo)) {
              details.append("Child CMR Rejected and specified IBM CMR No. :" + rejectionCmrNo).append("\n");
            }
          }
        }
      }
      if (rejectHist == null) {
        rejectHist = new WfHist();
        rejectHist.setRejReasonCd("OTH");
      }

      // try to check once if an ibm direct cmr is available
      LOG.debug("Trying to check once for a CMR for the record");
      this.ibmCmr = findIBMCMR(entityManager, handler, requestData, addr, engineData, rejectionCmrNo);
      if (this.ibmCmr != null) {
        setError = false;
      }

      processChildCancellation(entityManager, details, requestData, childRequest, engineData, setError ? rejectHist : null);

      if (setError) {
        LOG.debug("Child request processing has been stopped (Rejected or Sent back to requesters). Returning error.");
        String msg = "Child Request " + childReqId + " has been rejected and no IBM CMRs were found.";
        details.append(msg);
        details.append("\n");
        if (rejectHist != null) {
          details.append("Rejection: ").append(rejectHist.getRejReason()).append("\n");
          if (!StringUtils.isBlank(rejectHist.getCmt())) {
            details.append("Comments: ").append(rejectHist.getCmt()).append("\n");
          }
        }
        output.setOnError(true);
        output.setDetails(details.toString());
        output.setResults("Issues Encountered");
        return false;
      } else {
        LOG.debug("Child Request was not completed, but CMR " + this.ibmCmr.getCmrNum() + "(" + this.ibmCmr.getCmrName() + ") was found.");
        details.append("Child Request was not completed, but CMR " + this.ibmCmr.getCmrNum() + "(" + this.ibmCmr.getCmrName() + ") was found.\n");
        return true;
      }
    } else if (!"COM".equals(childReqStatus)) {
      LOG.debug("Child request not yet completed. Checking waiting time..");
      setWaiting(true);
      output.setDetails(details.toString());
      output.setOnError(false);
      output.setResults("Waiting on Child Request");
      return false;
    } else {
      LOG.debug("Child request " + childReqId + " completed.");
      details.append("Child Request " + childReqId + " completed. Proceeding with automated checks.\n");
      return true;
    }

  }

  /**
   * Creates a child request for this particular BP request
   * 
   * @param entityManager
   * @param requestData
   * @param engineData
   * @return
   * @throws CmrException
   */
  protected long createChildRequest(EntityManager entityManager, RequestData requestData, AutomationEngineData engineData) throws CmrException {

    RequestEntryModel model = new RequestEntryModel();
    try {
      PropertyUtils.copyProperties(model, requestData.getAdmin());
      // PropertyUtils.copyProperties(model, requestData.getData());
      PropertyUtils.copyProperties(model, requestData.getScorecard());
    } catch (Exception e) {
      LOG.warn("Cannot copy properties.", e);
    }
    model.setSourceSystId("CREATECMR");
    model.setModelCmrNo(null);
    model.setCmrIssuingCntry(SystemLocation.UNITED_STATES);
    // CMR-3880 - ensure scenarios are editable
    model.setDelInd(null);
    // model.setEnterprise(requestData.getData().getEnterprise());
    RequestEntryService service = new RequestEntryService();
    AppUser user = new AppUser();
    user.setIntranetId(requestData.getAdmin().getRequesterId());
    user.setBluePagesName(requestData.getAdmin().getRequesterNm());
    DummyServletRequest dummyReq = new DummyServletRequest();
    if (dummyReq.getSession() != null) {
      LOG.trace("Session found for dummy req");
      dummyReq.getSession().setAttribute(CmrConstants.SESSION_APPUSER_KEY, user);
    } else {
      LOG.warn("Session not found for dummy req");
    }
    model.setReqId(0);
    model.setReqStatus("DRA");
    model.setAction("SAV");
    try {
      service.processTransaction(model, dummyReq);
    } catch (Exception e) {
      LOG.debug("Error in creating request..", e);
      return -1;
    }

    return model.getReqId();

  }

  /**
   * Adjusts the child request data using the logic for BP creation. Creates an
   * internal 'details' on the information that needs to be added on the
   * processing
   * 
   * @param entityManager
   * @param requestData
   * @param engineData
   * @param childReqId
   * @param dnbMatch
   * @return
   * @throws Exception
   */
  protected String completeChildRequestDataAndAddress(EntityManager entityManager, RequestData requestData, AutomationEngineData engineData,
      long childReqId, DnBMatchingResponse dnbMatch) throws Exception {
    StringBuilder details = new StringBuilder();

    Data data = requestData.getData();
    Addr bpAddr = requestData.getAddress("ZS01");
    Addr childAddr = new Addr();
    AddrPK childAddrPk = new AddrPK();
    childAddrPk.setReqId(childReqId);
    childAddrPk.setAddrType("ZS01");
    childAddrPk.setAddrSeq(bpAddr.getId().getAddrSeq());
    try {
      PropertyUtils.copyProperties(childAddr, bpAddr);
    } catch (Exception e) {
      LOG.warn("Address data cannot be created.", e);
      return null;
    }
    childAddr.setId(childAddrPk);
    childAddr.setDivn(null);
    childAddr.setDept(null);
    LOG.debug("Creating Address for Child Request " + childReqId);
    entityManager.persist(childAddr);

    USHandler handler = new USHandler();
    String procCenter = RequestUtils.getProcessingCenter(entityManager, SystemLocation.UNITED_STATES);
    RequestData childReqData = new RequestData(entityManager, childReqId);
    Admin childAdmin = childReqData.getAdmin();
    childAdmin.setReqStatus(AutomationConst.STATUS_AUTOMATED_PROCESSING);
    childAdmin.setSourceSystId("CreateCMR");
    String customerName = bpAddr.getDivn()
        + ((!StringUtils.isBlank(bpAddr.getDept()) && useDeptForMatching(requestData)) ? " " + bpAddr.getDept() : "");
    customerName = customerName.toUpperCase();
    if (customerName.contains("CHN SVCS")) {
      customerName = customerName.substring(0, customerName.lastIndexOf("CHN SVCS")).trim();
    } else if (customerName.contains("CHANNEL SVCS")) {
      customerName = customerName.substring(0, customerName.lastIndexOf("CHANNEL SVCS")).trim();
    } else if (customerName.contains("CHANNEL SVCS USE ONLY")) {
      customerName = customerName.substring(0, customerName.lastIndexOf("CHANNEL SVCS USE ONLY")).trim();
    } else if (customerName.contains("C/O")) {
      customerName = customerName.substring(0, customerName.lastIndexOf("C/O")).trim();
    } else if (customerName.contains("UCW")) {
      customerName = customerName.substring(0, customerName.lastIndexOf("UCW")).trim();
    }

    String[] names = handler.doSplitName(cleanName(customerName), "", 28, 22);
    childAdmin.setMainCustNm1(names[0]);
    childAdmin.setMainCustNm2(names[1]);
    childAdmin.setLockBy(null);
    childAdmin.setLockByNm(null);
    childAdmin.setLockInd("N");
    childAdmin.setLockTs(null);
    childAdmin.setLastProcCenterNm(procCenter);
    LOG.debug("Updating Child Admin data..");
    entityManager.merge(childAdmin);

    // set the correct scenario
    setChildRequestScenario(data, childReqData.getData(), childAdmin, details);
    Data childData = childReqData.getData();

    LOG.debug("Updating Child data..");
    loadTemplateDefaults(childData, childData.getCustSubGrp());
    if (StringUtils.isBlank(childData.getIsicCd())) {
      childData.setIsicCd(data.getIsicCd());
      childData.setSubIndustryCd(data.getSubIndustryCd());
    }

    // CREATCMR-4569
    if (StringUtils.isEmpty(childData.getAbbrevNm())) {
      if (!StringUtils.isEmpty(childAdmin.getMainCustNm1())) {
        if (childAdmin.getMainCustNm1().length() >= 15) {
          childData.setAbbrevNm(childAdmin.getMainCustNm1().substring(0, 15));
        } else {
          childData.setAbbrevNm(childAdmin.getMainCustNm1().substring(0, childAdmin.getMainCustNm1().length()));
        }
      }
    }

    if (StringUtils.isEmpty(childData.getSearchTerm())) {
      if (!StringUtils.isEmpty(childData.getAbbrevNm())) {
        if (childData.getAbbrevNm().length() >= 10) {
          childData.setSearchTerm(childData.getAbbrevNm().substring(0, 10));
        } else {
          childData.setSearchTerm(childData.getAbbrevNm().substring(0, childData.getAbbrevNm().length()));
        }
      }
    }

    // CREATCMR-6081
    if (StringUtils.isEmpty(childData.getIccTaxClass())) {
      childData.setIccTaxClass("000");
    }
    // CREATCMR-6081

    // set ppsceid if pool scenario request
    if (USUtil.CG_THIRD_P_BUSINESS_PARTNER.equals(childData.getCustGrp()) && USUtil.SC_BP_POOL.equals(childData.getCustSubGrp())) {
      childData.setPpsceid(data.getPpsceid());
    }
    childData.setUsSicmen(childData.getIsicCd());
    Scorecard childScoreCard = childReqData.getScorecard();
    childScoreCard.setFindCmrResult(CmrConstants.RESULT_NO_RESULT);
    childScoreCard.setFindCmrTs(SystemUtil.getCurrentTimestamp());
    childScoreCard.setFindCmrUsrId("CreateCMR");
    childScoreCard.setFindCmrUsrNm("CreateCMR");

    if (dnbMatch != null) {
      DnbData dnbData = CompanyFinder.getDnBDetails(dnbMatch.getDunsNo());
      DnBCompany dnbCompany = dnbData != null && dnbData.getResults() != null && !dnbData.getResults().isEmpty() ? dnbData.getResults().get(0) : null;
      if (dnbCompany != null) {
        if (dnbCompany.getIbmIsic() != null) {
          childData.setIsicCd(dnbCompany.getIbmIsic());
          childData.setUsSicmen(dnbCompany.getIbmIsic());
          String subInd = RequestUtils.getSubIndustryCd(entityManager, dnbCompany.getIbmIsic(), SystemLocation.UNITED_STATES);
          childData.setSubIndustryCd(subInd);
        }
      }
      childData.setDunsNo(dnbMatch.getDunsNo());
      childScoreCard.setFindDnbResult(CmrConstants.RESULT_ACCEPTED);
    } else {
      childScoreCard.setFindDnbResult(CmrConstants.RESULT_NO_RESULT);
    }
    childScoreCard.setFindDnbTs(SystemUtil.getCurrentTimestamp());
    childScoreCard.setFindDnbUsrId("CreateCMR");
    childScoreCard.setFindDnbUsrNm("CreateCMR");

    modifyChildDataValues(entityManager, requestData, childReqData, details);

    entityManager.merge(childData);
    entityManager.merge(childScoreCard);

    LOG.debug("Adding comment log");
    RequestUtils.createCommentLogFromBatch(entityManager, "AutomationEngine", childReqId,
        "IBM CMR request for parent Business Partner request " + requestData.getAdmin().getId().getReqId());

    LOG.debug("Creating workflow history");
    RequestUtils.createWorkflowHistoryFromBatch(entityManager, "AutomationEngine", childAdmin, "Sending for processing via Automation Engine",
        "Send for Processing", procCenter, procCenter, false, false, null);

    Admin admin = requestData.getAdmin();
    admin.setChildReqId(childReqId);
    entityManager.merge(admin);

    return details.toString();
  }

  /**
   * Allows to modify child request data values after all template values are
   * loaded
   * 
   * @param requestData
   * @param childReqData
   * @param details
   */
  protected void modifyChildDataValues(EntityManager entityManager, RequestData requestData, RequestData childReqData, StringBuilder details) {
    // NOOP
  }

  /**
   * Sets the correct scenario on the child record based on request information
   * 
   * @param data
   * @param childData
   * @param childAdmin
   * @return
   */
  protected abstract void setChildRequestScenario(Data data, Data childData, Admin childAdmin, StringBuilder details);

  /**
   * Loads the scenario values for the given subtype
   * 
   * @param data
   * @param custSubType
   * @throws Exception
   */
  private void loadTemplateDefaults(Data data, String custSubType) throws Exception {
    DummyServletRequest dummyReq = new DummyServletRequest();
    LOG.debug("Getting template for " + custSubType);
    dummyReq.addParam("custSubGrp", custSubType);

    LOG.debug("Loading template support for US");
    Template template = TemplateManager.getTemplate(SystemLocation.UNITED_STATES, dummyReq);
    Map<String, String> valueMap = new HashMap<String, String>();
    for (TemplatedField field : template.getFields()) {
      if (field.getValues() != null && !field.getValues().isEmpty()) {
        String firstVal = field.getValues().get(0);
        if (!StringUtils.isBlank(firstVal)) {
          if (!Arrays.asList("*", "%", "$", "~", "@").contains(firstVal)) {
            LOG.trace(" - template: " + field.getFieldName() + " = " + firstVal);
            valueMap.put(field.getFieldName(), firstVal);
          }
        }
      }
    }
    PropertyUtils.copyProperties(data, valueMap);

  }

  /**
   * returns true if the End user scenario type scenario
   *
   * @return
   */
  public boolean isEndUserSupported() {
    return true;
  }

  /**
   * returns true if the request should go into a waiting state
   * 
   * @return
   */
  public boolean isWaiting() {
    return waiting;
  }

  /**
   * set request to waiting state
   * 
   * @param waiting
   */
  public void setWaiting(boolean waiting) {
    this.waiting = waiting;
  }

  /**
   * 
   * Cleans the given name into US-CMR standards
   * 
   * @param name
   * @return
   */
  protected static String cleanName(String name) {
    if (name == null) {
      return "";
    }
    name = name.replaceAll("'", "");
    name = name.replaceAll("[^A-Za-z0-9&\\-/]", " ");
    name = name.replaceAll("  ", " ").toUpperCase();
    return name;
  }

  public static boolean useDeptForMatching(RequestData requestData) {
    Addr addr = requestData.getAddress("ZS01");
    if (addr != null && StringUtils.isNotBlank(addr.getDept())) {
      String dept = addr.getDept().toUpperCase();

      for (String identifier : DEPARTMENT_IDENTIFIERS) {
        if (dept.contains(identifier)) {
          return false;
        }
      }
    }

    return true;
  }

  public FindCMRRecordModel getIbmCmr(EntityManager entityManager, GEOHandler handler, RequestData requestData, StringBuilder details, Addr zs01,
      AutomationEngineData engineData, RequestData childRequest, boolean childCompleted) throws Exception {
    if (this.ibmCmr == null) {
      String childCmrNo = null;

      if (childRequest != null) {
        childCmrNo = childRequest.getData().getCmrNo();
      }
      // prioritize child request here
      if (childCompleted) {
        details.append("Copying CMR values direct CMR " + childCmrNo + " from Child Request " + childRequest.getAdmin().getId().getReqId() + ".\n");
        this.ibmCmr = createIBMCMRFromChild(childRequest, entityManager);
      } else {
        // check IBM Direct CMR
        if (this.ibmCmr == null) {
          // if a rejected child caused the retrieval of a child cmr
          this.ibmCmr = findIBMCMR(entityManager, handler, requestData, zs01, engineData, childCmrNo);
        }
        if (this.ibmCmr != null) {
          details.append("Copying CMR values CMR " + ibmCmr.getCmrNum() + " from FindCMR.\n");
          LOG.debug("IBM Direct CMR Found: " + ibmCmr.getCmrNum() + " - " + ibmCmr.getCmrName());
          Admin theAdmin = requestData.getAdmin();
          if (theAdmin != null && "CreateCMR-BP".equals(theAdmin.getSourceSystId())) {
            Data theData = requestData.getData();
            if (theData != null && SystemLocation.UNITED_STATES.equals(theData.getCmrIssuingCntry()) && "E".equals(theData.getEmail3())) {
              theData.setCmrNo2(ibmCmr.getCmrNum());
            }
          }
        }
      }
    }
    return this.ibmCmr;
  }

  public FindCMRRecordModel getIbmCmr() {
    return this.ibmCmr;
  }

  public void setIbmCmr(FindCMRRecordModel ibmCmr) {
    this.ibmCmr = ibmCmr;
  }

  public static String getProcessingTypeForUS(EntityManager entityManager, String country) {
    String sql = ExternalizedQuery.getSql("AUTO.GET_PROCESSING_TYPE");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("CNTRY", country);
    query.setForReadOnly(true);
    return query.getSingleResult(String.class);
  }

}
