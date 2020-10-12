package com.ibm.cio.cmr.request.automation.impl.us;

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
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.automation.AutomationElement;
import com.ibm.cio.cmr.request.automation.AutomationElementRegistry;
import com.ibm.cio.cmr.request.automation.AutomationEngineData;
import com.ibm.cio.cmr.request.automation.RequestData;
import com.ibm.cio.cmr.request.automation.impl.OverridingElement;
import com.ibm.cio.cmr.request.automation.impl.ProcessWaitingElement;
import com.ibm.cio.cmr.request.automation.out.AutomationResult;
import com.ibm.cio.cmr.request.automation.out.OverrideOutput;
import com.ibm.cio.cmr.request.automation.util.AutomationConst;
import com.ibm.cio.cmr.request.automation.util.DummyServletRequest;
import com.ibm.cio.cmr.request.automation.util.geo.USUtil;
import com.ibm.cio.cmr.request.automation.util.geo.us.USCeIdMapping;
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
import com.ibm.cmr.services.client.dnb.DnBCompany;
import com.ibm.cmr.services.client.dnb.DnbData;
import com.ibm.cmr.services.client.matching.MatchingResponse;
import com.ibm.cmr.services.client.matching.cmr.DuplicateCMRCheckRequest;
import com.ibm.cmr.services.client.matching.cmr.DuplicateCMRCheckResponse;
import com.ibm.cmr.services.client.matching.dnb.DnBMatchingResponse;
import com.ibm.cmr.services.client.matching.gbg.GBGResponse;
import com.ibm.cmr.services.client.pps.PPSProfile;
import com.ibm.cmr.services.client.pps.PPSRequest;
import com.ibm.cmr.services.client.pps.PPSResponse;
import com.ibm.cmr.services.client.query.QueryRequest;
import com.ibm.cmr.services.client.query.QueryResponse;
import com.ibm.json.java.JSONObject;

/**
 * {@link AutomationElement} handling the US - Business Partner End User
 * Scenario
 * 
 * @author JeffZAMORA
 *
 */
public class USBusinessPartnerElement extends OverridingElement implements ProcessWaitingElement {

  private static final Logger LOG = Logger.getLogger(USBusinessPartnerElement.class);
  public static final String RESTRICT_TO_END_USER = "BPQS";
  public static final String RESTRICT_TO_MAINTENANCE = "IRCSO";

  public static final String BP_MANAGING_IR = "MIR";
  public static final String BP_INDIRECT_REMARKETER = "IRMR";

  public static final String TYPE_STATE_AND_LOCAL = "7";
  public static final String TYPE_FEDERAL = "9";
  public static final String TYPE_COMMERCIAL = "1";
  public static final String TYPE_BUSINESS_PARTNER = "5";

  public static final String SUB_TYPE_STATE_AND_LOCAL_STATE = "STATE";
  public static final String SUB_TYPE_STATE_AND_LOCAL_DISTRICT = "SPEC DIST";
  public static final String SUB_TYPE_STATE_AND_LOCAL_COUNTY = "COUNTY";
  public static final String SUB_TYPE_STATE_AND_LOCAL_CITY = "CITY";
  public static final String SUB_TYPE_FEDERAL_POA = "POA";
  public static final String SUB_TYPE_FEDERAL_REGULAR_GOVT = "FEDERAL";
  public static final String SUB_TYPE_COMMERCIAL_REGULAR = "REGULAR";
  public static final String SUB_TYPE_BUSINESS_PARTNER_END_USER = "END USER";
  private static final List<String> SPECIAL_TAX_STATES = Arrays.asList("AK", "DE", "MT", "NH", "OR");

  public static final String AFFILIATE_FEDERAL = "9200000";
  private boolean waiting;

  public USBusinessPartnerElement(String requestTypes, String actionOnError, boolean overrideData, boolean stopOnError) {
    super(requestTypes, actionOnError, overrideData, stopOnError);
  }

  @Override
  public AutomationResult<OverrideOutput> executeElement(EntityManager entityManager, RequestData requestData, AutomationEngineData engineData)
      throws Exception {

    Admin admin = requestData.getAdmin();
    long reqId = admin.getId().getReqId();
    Data data = requestData.getData();
    AutomationResult<OverrideOutput> output = buildResult(reqId);
    Addr addr = requestData.getAddress("ZS01");

    // validate first
    if (doInitialValidations(admin, data, addr, output, engineData)) {
      return output;
    }

    USHandler handler = new USHandler();
    OverrideOutput overrides = new OverrideOutput(false);
    StringBuilder details = new StringBuilder();

    FindCMRRecordModel ibmDirectCmr = null;
    long childReqId = admin.getChildReqId();
    String childCmrNo = null;
    RequestData childRequest = null;
    boolean childCompleted = false;
    if (childReqId > 0) {
      // check child reqId processing first
      LOG.debug("Getting child request data for Request " + childReqId);
      childRequest = new RequestData(entityManager, childReqId);
      if (childRequest == null || childRequest.getAdmin() == null) {
        String msg = "Child request " + childReqId + " missing. The request needs to be manually processed.";
        details.append(msg);
        details.append("\n");
        details.append("No field value has been computed for this record.");
        engineData.addRejectionComment("OTH", msg, "", "");
        output.setOnError(true);
        output.setDetails(details.toString());
        output.setResults("Issues Encountered");
        return output;
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
                details.append("Child CMR Rejected and specified IBM Direct CMR No. :" + rejectionCmrNo).append("\n");
              }
            }
          }
        }
        if (rejectHist == null) {
          rejectHist = new WfHist();
          rejectHist.setRejReasonCd("OTH");
        }

        // try to check once if an ibm direct cmr is available
        LOG.debug("Trying to check once for a Direct CMR for the record");
        ibmDirectCmr = findIBMDirectCMR(entityManager, handler, requestData, addr, engineData, rejectionCmrNo);
        if (ibmDirectCmr != null) {
          setError = false;
        }

        processChildCancellation(entityManager, details, requestData, childRequest, engineData, setError ? rejectHist : null);

        if (setError) {
          LOG.debug("Child request processing has been stopped (Rejected or Sent back to requesters). Returning error.");
          String msg = "IBM Direct Request " + childReqId + " has been rejected and no IBM Direct CMRs were found.";
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
          return output;
        } else {
          LOG.debug("Child Request was not completed, but IBM Direct CMR " + ibmDirectCmr.getCmrNum() + "(" + ibmDirectCmr.getCmrName() + ") found.");
          details.append(
              "Child Request was not completed, but IBM Direct CMR " + ibmDirectCmr.getCmrNum() + "(" + ibmDirectCmr.getCmrName() + ") found.\n");
        }

      } else if (!"COM".equals(childReqStatus)) {
        LOG.debug("Child request not yet completed. Checking waiting time..");
        this.waiting = true;
        output.setDetails(details.toString());
        output.setOnError(false);
        output.setResults("Waiting on Child Request");
        return output;
      } else {
        LOG.debug("Child request " + childReqId + " completed.");
        details.append("Child Request " + childReqId + " completed. Proceeding with automated checks.\n");
        childCmrNo = childRequest.getData().getCmrNo();
        childCompleted = true;
      }
    }

    // prioritize child request here
    if (childCompleted) {
      details.append("Copying CMR values direct CMR " + childCmrNo + " from Child Request " + childReqId + ".\n");
      ibmDirectCmr = createDirectCMRFromChild(childRequest);
    } else {
      // check IBM Direct CMR
      if (ibmDirectCmr == null) {
        // if a rejected child caused the retrieval of a child cmr
        ibmDirectCmr = findIBMDirectCMR(entityManager, handler, requestData, addr, engineData, childCmrNo);
      }
      if (ibmDirectCmr != null) {
        details.append("Copying CMR values CMR " + ibmDirectCmr.getCmrNum() + " from FindCMR.\n");
        LOG.debug("IBM Direct CMR Found: " + ibmDirectCmr.getCmrNum() + " - " + ibmDirectCmr.getCmrName());
      }
    }

    // match against D&B
    DnBMatchingResponse dnbMatch = matchAgainstDnB(handler, requestData, addr, engineData, details, overrides, ibmDirectCmr != null);

    // check CEID
    boolean t1 = isTier1BP(data);
    if (!t1) {
      details.append("BP is NOT a Tier 1.\n");
      engineData.addNegativeCheckStatus("_usBpT1", "BP is not a T1 or status cannot be determined via PPS profile.");
    } else {
      details.append("BP is a Tier 1.\n");
    }

    if (ibmDirectCmr == null) {

      LOG.debug("No IBM Direct CMR for the end user.");
      childReqId = createChildRequest(entityManager, requestData, engineData);
      details.append("No IBM Direct CMR for the end user.\n");

      if (childCompleted) {
        String childError = "Child Request " + childReqId + " was completed but Direct CMR cannot be determined. Manula validation needed.";
        details.append(childError + "\n");
        engineData.addNegativeCheckStatus("_usChildError", childError);
        output.setDetails(details.toString());
        output.setResults("Issues Encountered");
        return output;
      } else {
        String childErrorMsg = "- IBM Direct CMR request creation cannot be done, errors were encountered -";
        if (childReqId <= 0) {
          details.append(childErrorMsg + "\n");
          // engineData.addNegativeCheckStatus("_usBpRejected", childErrorMsg);
          engineData.addRejectionComment("OTH", childErrorMsg, "", "");
          output.setDetails(details.toString());
          output.setOnError(true);
          output.setResults("Issues Encountered");
          return output;
        } else {
          String childDetails = completeChildRequestDataAndAddress(entityManager, requestData, engineData, childReqId, dnbMatch);
          if (childDetails == null) {
            details.append(childErrorMsg + "\n");
            // engineData.addNegativeCheckStatus("_usBpRejected",
            // childErrorMsg);
            engineData.addRejectionComment("OTH", childErrorMsg, "", "");
            output.setOnError(true);
            output.setResults("Issues Encountered");
            output.setDetails(details.toString());
            return output;
          } else {
            details.append("Child Request " + childReqId + " created for the IBM Direct CMR record of " + addr.getDivn()
                + (!StringUtils.isBlank(addr.getDept()) ? " " + addr.getDept() : "")
                + ".\nThe system will wait for completion of the child record bfore processing the request.\n");
            details.append(childDetails + "\n");
            this.waiting = true;
            output.setDetails(details.toString());
            output.setResults("Waiting on Child Request");
            output.setOnError(false);
            return output;
          }

        }
      }
    }

    // copy from IBM Direct if found, and fill the rest of BO codes
    copyAndFillIBMData(entityManager, handler, ibmDirectCmr, requestData, engineData, details, overrides, childRequest);

    // CMR-3334 - do some last checks on Enterprise/Affiliate/Company
    details.append("\n");
    String affiliate = data.getAffiliate();
    if (ibmDirectCmr != null && !StringUtils.isBlank(ibmDirectCmr.getCmrAffiliate())) {
      affiliate = ibmDirectCmr.getCmrAffiliate();
    }
    if (StringUtils.isBlank(affiliate)) {
      details.append("\nAffiliate cannot be computed automatically.");
      engineData.addNegativeCheckStatus("_usBpAff", "Affiliate cannot be computed automatically");
    }

    // USCeIdMapping mapping = USCeIdMapping.getByCeid(data.getPpsceid());
    USCeIdMapping mapping = USCeIdMapping.getByEnterprise(data.getEnterprise());
    // String enterpriseNo = data.getEnterprise();
    if (mapping != null) {
      // if (!mapping.getEnterpriseNo().equals(enterpriseNo)) {
      // details.append("\nEnterprise No. updated to mapped value for the CEID
      // (" + mapping.getEnterpriseNo() + ").");
      // overrides.addOverride(getProcessCode(), "DATA", "ENTERPRISE",
      // enterpriseNo, mapping.getEnterpriseNo());
      // enterpriseNo = mapping.getEnterpriseNo();
      // }
      if (!mapping.getCompanyNo().equals(data.getCompany())) {
        details.append("\nCompany No. updated to mapped value for the CEID (" + mapping.getCompanyNo() + ").");
        overrides.addOverride(getProcessCode(), "DATA", "COMPANY", data.getCompany(), mapping.getCompanyNo());
      }
    } else {
      details.append("\nEnterprise No. cannot be validated automatically.");
      engineData.addNegativeCheckStatus("_usBpEnt", "Enterprise No. cannot be validated automatically.");
    }
    // if (StringUtils.isBlank(enterpriseNo)) {
    // details.append("\nEnterprise No. cannot be computed automatically.\n");
    // engineData.addNegativeCheckStatus("_usBpEnt", "Enterprise No. cannot be
    // computed automatically");
    // }

    output.setProcessOutput(overrides);
    output.setDetails(details.toString());
    output.setResults("Success");
    return output;
  }

  /**
   * Perform initial validations on the request whether it fits the BP@EU
   * scenario
   * 
   * @param admin
   * @param data
   * @param addr
   * @param output
   * @param engineData
   * @return true if the processing needs to halt
   */
  private boolean doInitialValidations(Admin admin, Data data, Addr addr, AutomationResult<OverrideOutput> output, AutomationEngineData engineData) {
    // check the scenario
    if ("U".equals(admin.getReqType())) {
      output.setResults("Skipped");
      output.setDetails("Update types not supported.");
      return true;
    }

    String custGrp = data.getCustGrp();
    String custSubGrp = data.getCustSubGrp();
    if ("BYMODEL".equals(custSubGrp)) {
      String type = admin.getCustType();
      if (!USUtil.BUSINESS_PARTNER.equals(type) || !"E".equals(data.getBpAcctTyp())
          || (!RESTRICT_TO_END_USER.equals(data.getRestrictTo()) && !RESTRICT_TO_MAINTENANCE.equals(data.getRestrictTo()))) {
        output.setResults("Skipped");
        output.setDetails("Non BP End User create by model scenario not supported.");
        return true;
      }
      // CMR-3856 add check for ehosting
      String deptAttn = addr.getDept() != null ? addr.getDept().toLowerCase() : "";
      if (deptAttn.contains("ehost") || deptAttn.contains("e-host") || deptAttn.contains("e host")) {
        output.setResults("Skipped");
        output.setDetails("Non BP End User create by model scenario not supported.");
        return true;
      }
    } else if (!TYPE_BUSINESS_PARTNER.equals(custGrp) || !SUB_TYPE_BUSINESS_PARTNER_END_USER.equals(custSubGrp)) {
      output.setResults("Skipped");
      output.setDetails("Non BP End User scenario not supported.");
      return true;
    }

    // if (StringUtils.isBlank(data.getPpsceid())) {
    // String msg = "PPS CEID is required for Business Partner requests.";
    // engineData.addRejectionComment("OTH", msg, "", "");
    // output.setOnError(true);
    // output.setDetails(msg);
    // output.setResults("CEID Missing");
    // return true;
    // }

    return false;
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
  private DnBMatchingResponse matchAgainstDnB(GEOHandler handler, RequestData requestData, Addr addr, AutomationEngineData engineData,
      StringBuilder details, OverrideOutput overrides, boolean hasExistingCmr) throws Exception {
    List<DnBMatchingResponse> dnbMatches = USUtil.getMatchesForBPEndUser(handler, requestData, engineData);
    if (dnbMatches.isEmpty()) {
      LOG.debug("No D&B matches found for the End User " + addr.getDivn() + (!StringUtils.isBlank(addr.getDept()) ? " " + addr.getDept() : ""));
      String msg = "No high quality D&B matches for the End User " + addr.getDivn()
          + (!StringUtils.isBlank(addr.getDept()) ? " " + addr.getDept() : "");
      details.append(msg + "\n");
      if (hasExistingCmr) {
        overrides.addOverride(getProcessCode(), "ADMN", "COMP_VERIFIED_INDC", requestData.getAdmin().getCompVerifiedIndc(), "Y");
        details.append("- A current active CMR exists for the company.");
      } else {
        engineData.addNegativeCheckStatus("_usBpNoMatch", msg);
      }
      details.append("\n");
      return null;
    } else {
      DnBMatchingResponse dnbMatch = dnbMatches.get(0);
      LOG.debug("D&B match found for " + addr.getDivn() + (!StringUtils.isBlank(addr.getDept()) ? " " + addr.getDept() : "") + " with DUNS "
          + dnbMatch.getDunsNo());
      details.append("D&B match/es found for the End User " + addr.getDivn() + (!StringUtils.isBlank(addr.getDept()) ? " " + addr.getDept() : "")
          + ". Highest Match:\n");
      details.append("\n");
      details.append(dnbMatch.getDnbName() + "\n");
      details.append(dnbMatch.getDnbStreetLine1() + "\n");
      if (!StringUtils.isBlank(dnbMatch.getDnbStreetLine2())) {
        details.append(dnbMatch.getDnbStreetLine2() + "\n");
      }
      overrides.addOverride(getProcessCode(), "ADMN", "COMP_VERIFIED_INDC", requestData.getAdmin().getCompVerifiedIndc(), "Y");
      details.append(dnbMatch.getDnbCity() + ", " + dnbMatch.getDnbStateProv() + "\n");
      details.append(dnbMatch.getDnbCountry() + " " + dnbMatch.getDnbPostalCode() + "\n\n");
      return dnbMatch;
    }

  }

  /**
   * Creates a {@link FindCMRRecordModel} copy from the Child Request
   * 
   * @param childRequest
   * @return
   */
  private FindCMRRecordModel createDirectCMRFromChild(RequestData childRequest) {
    if (childRequest == null || !"COM".equals(childRequest.getAdmin().getReqStatus())) {
      return null;
    }
    FindCMRRecordModel ibmDirectCmr = new FindCMRRecordModel();
    Data completedChildData = childRequest.getData();
    // make sure we switch the codes to use directly from child
    ibmDirectCmr.setCmrNum(completedChildData.getCmrNo());
    String affiliate = completedChildData.getAffiliate();
    if (StringUtils.isBlank(affiliate)) {
      affiliate = getUSCMRAffiliate(completedChildData.getCmrNo());
    }
    ibmDirectCmr.setCmrAffiliate(affiliate);

    String childIsic = completedChildData.getIsicCd();
    boolean federalPoa = childIsic != null && (childIsic.startsWith("90") || childIsic.startsWith("91") || childIsic.startsWith("92"));
    if (federalPoa) {
      String enterprise = completedChildData.getEnterprise();
      if (StringUtils.isBlank(enterprise)) {
        enterprise = getUSCMREnterprise(completedChildData.getCmrNo());
      }
      ibmDirectCmr.setCmrEnterpriseNumber(enterprise);
    }

    ibmDirectCmr.setCmrIsu(completedChildData.getIsuCd());
    ibmDirectCmr.setCmrTier(completedChildData.getClientTier());
    ibmDirectCmr.setCmrInac(completedChildData.getInacCd());
    ibmDirectCmr.setCmrInacType(completedChildData.getInacType());
    ibmDirectCmr.setCmrSubIndustry(completedChildData.getSubIndustryCd());
    ibmDirectCmr.setCmrIsic(completedChildData.getUsSicmen());
    Addr childInstallAt = childRequest.getAddress("ZS01");
    ibmDirectCmr.setCmrCountyCode(childInstallAt.getCounty());
    ibmDirectCmr.setCmrCounty(childInstallAt.getCountyName());
    ibmDirectCmr.setCmrName4(childInstallAt.getCustNm4());
    ibmDirectCmr.setCmrStreetAddress(
        childInstallAt.getAddrTxt() + (StringUtils.isNotBlank(childInstallAt.getAddrTxt2()) ? childInstallAt.getAddrTxt2() : ""));
    ibmDirectCmr.setCmrCity(childInstallAt.getCity1());
    ibmDirectCmr.setCmrPostalCode(childInstallAt.getPostCd());
    ibmDirectCmr.setCmrState(childInstallAt.getStateProv());
    ibmDirectCmr.setCmrCountryLanded(childInstallAt.getLandCntry());
    ibmDirectCmr.setCmrDept(childInstallAt.getDept());
    ibmDirectCmr.setCmrBuyingGroup(completedChildData.getBgId());
    ibmDirectCmr.setCmrBuyingGroupDesc(completedChildData.getBgDesc());
    ibmDirectCmr.setCmrGlobalBuyingGroup(completedChildData.getGbgId());
    ibmDirectCmr.setCmrGlobalBuyingGroupDesc(completedChildData.getGbgDesc());
    ibmDirectCmr.setCmrLde(completedChildData.getBgRuleId());
    ibmDirectCmr.setCmrCoverage(completedChildData.getCovId());
    ibmDirectCmr.setCmrCoverageName(completedChildData.getCovDesc());
    ibmDirectCmr.setCmrName(childRequest.getAdmin().getMainCustNm1());

    return ibmDirectCmr;
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
  private FindCMRRecordModel findIBMDirectCMR(EntityManager entityManager, GEOHandler handler, RequestData requestData, Addr addr,
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

      String customerName = addr.getDivn() + (!StringUtils.isBlank(addr.getDept()) ? " " + addr.getDept() : "");
      customerName = customerName.toUpperCase();
      if (customerName.contains("C/O")) {
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
        for (DuplicateCMRCheckResponse record : res.getMatches()) {
          LOG.debug(" - Duplicate: (Restrict To: " + record.getUsRestrictTo() + ", Grade: " + record.getMatchGrade() + ")" + record.getCompany()
              + " - " + record.getCmrNo() + " - " + record.getAddrType() + " - " + record.getStreetLine1());
          if (StringUtils.isBlank(record.getUsRestrictTo())) {
            // check US CMR DB first to confirm no restriction
            if (hasBlankRestrictionCodeInUSCMR(record.getCmrNo())) {

              // IBM Direct CMRs have blank restrict to
              LOG.debug("CMR No. " + record.getCmrNo() + " has BLANK restriction code in US CMR. Getting CMR Details..");
              String overrides = "addressType=ZS01&cmrOwner=IBM&showCmrType=R&customerNumber=" + record.getCmrNo();
              FindCMRResultModel result = CompanyFinder.getCMRDetails(SystemLocation.UNITED_STATES, record.getCmrNo(), 5, null, overrides);
              if (result != null && result.getItems() != null && !result.getItems().isEmpty()) {
                return result.getItems().get(0);
              }
            } else {
              LOG.debug("CMR No. " + record.getCmrNo() + " has non-blank restriction code in US CMR");
            }
          }
        }
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

  /**
   * Creates a child request for this particular BP request
   * 
   * @param entityManager
   * @param requestData
   * @param engineData
   * @return
   * @throws CmrException
   */
  private long createChildRequest(EntityManager entityManager, RequestData requestData, AutomationEngineData engineData) throws CmrException {

    RequestEntryModel model = new RequestEntryModel();
    try {
      PropertyUtils.copyProperties(model, requestData.getAdmin());
      // PropertyUtils.copyProperties(model, requestData.getData());
      PropertyUtils.copyProperties(model, requestData.getScorecard());
    } catch (Exception e) {
      LOG.warn("Cannot copy properties.", e);
    }
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
  private String completeChildRequestDataAndAddress(EntityManager entityManager, RequestData requestData, AutomationEngineData engineData,
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
    String customerName = bpAddr.getDivn() + (!StringUtils.isBlank(bpAddr.getDept()) ? " " + bpAddr.getDept() : "");
    customerName = customerName.toUpperCase();
    if (customerName.contains("C/O")) {
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

    entityManager.merge(childData);
    entityManager.merge(childScoreCard);

    LOG.debug("Adding comment log");
    RequestUtils.createCommentLogFromBatch(entityManager, "AutomationEngine", childReqId,
        "IBM Direct CMR request for parent Business Partner request " + requestData.getAdmin().getId().getReqId());

    LOG.debug("Creating workflow history");
    RequestUtils.createWorkflowHistoryFromBatch(entityManager, "AutomationEngine", childAdmin, "Sending for processing via Automation Engine",
        "Send for Processing", procCenter, procCenter, false, false, null);

    Admin admin = requestData.getAdmin();
    admin.setChildReqId(childReqId);
    entityManager.merge(admin);

    return details.toString();
  }

  /**
   * Sets the correct scenario on the child record based on request information
   * 
   * @param data
   * @param childData
   * @param childAdmin
   */
  private void setChildRequestScenario(Data data, Data childData, Admin childAdmin, StringBuilder details) {
    String isic = data.getUsSicmen();
    if (StringUtils.isBlank(isic)) {
      isic = data.getIsicCd();
    }
    if (StringUtils.isBlank(isic)) {
      isic = "";
    }

    String affiliate = data.getAffiliate();

    String typeDesc = null;
    String subTypeDesc = null;
    String type = null;
    String subType = null;
    String custType = null;

    int isicNumeric = 0;
    if (StringUtils.isNumeric(isic.substring(0, 2))) {
      isicNumeric = Integer.parseInt(isic.substring(0, 2));
    }

    if (isicNumeric >= 94 && isicNumeric <= 97) {
      typeDesc = "State and Local";
      type = TYPE_STATE_AND_LOCAL;
      custType = USUtil.STATE_LOCAL;
      switch (isicNumeric) {
      case 94:
        subTypeDesc = "State and Local - State";
        subType = SUB_TYPE_STATE_AND_LOCAL_STATE;
        break;
      case 95:
        subTypeDesc = "State and Local - County";
        subType = SUB_TYPE_STATE_AND_LOCAL_COUNTY;
        break;
      case 96:
        subTypeDesc = "State and Local - City";
        subType = SUB_TYPE_STATE_AND_LOCAL_CITY;
        break;
      case 97:
        subTypeDesc = "State and Local - District";
        subType = SUB_TYPE_STATE_AND_LOCAL_DISTRICT;
        break;
      }
    } else if (isicNumeric >= 90 && isicNumeric <= 92) {
      typeDesc = "Federal";
      type = TYPE_FEDERAL;
      if (AFFILIATE_FEDERAL.equals(affiliate)) {
        subTypeDesc = "Federal Gov't Regular";
        subType = SUB_TYPE_FEDERAL_REGULAR_GOVT;
        custType = USUtil.FEDERAL;
      } else {
        subTypeDesc = "Power of Attorney";
        subType = SUB_TYPE_FEDERAL_POA;
        custType = USUtil.POWER_OF_ATTORNEY;
      }
    } else {
      typeDesc = "Commercial";
      type = TYPE_COMMERCIAL;
      subTypeDesc = "Regular Commercial CMR";
      subType = SUB_TYPE_COMMERCIAL_REGULAR;
      custType = USUtil.COMMERCIAL;
    }

    details.append(" - Type: " + typeDesc + "\n");
    details.append(" - Sub-type: " + subTypeDesc + "\n");
    childData.setCustGrp(type);
    childData.setCustSubGrp(subType);
    childAdmin.setCustType(custType);

  }

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
   * Checks if the current BP is a T1 from PPS Service
   * 
   * @param data
   * @return
   */
  private boolean isTier1BP(Data data) {

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
   * Copies the IBM codes from the IBM Direct CMR and fills the BO codes with
   * the given rules
   * 
   * @param handler
   * @param requestData
   * @param addr
   * @param engineData
   * @param details
   */
  private void copyAndFillIBMData(EntityManager entityManager, GEOHandler handler, FindCMRRecordModel ibmDirectCmr, RequestData requestData,
      AutomationEngineData engineData, StringBuilder details, OverrideOutput overrides, RequestData childRequest) {
    Data data = requestData.getData();
    if (ibmDirectCmr != null) {
      if (!StringUtils.isBlank(ibmDirectCmr.getCmrSapNumber())) {
        details.append("\nCopying IBM Codes from IBM Direct CMR " + ibmDirectCmr.getCmrNum() + " - " + ibmDirectCmr.getCmrName() + " ("
            + ibmDirectCmr.getCmrSapNumber() + "): \n");
      } else {
        details.append("\nCopying IBM Codes from IBM Direct CMR " + ibmDirectCmr.getCmrNum() + " - " + ibmDirectCmr.getCmrName() + ": \n");
      }

      String affiliate = ibmDirectCmr.getCmrAffiliate();
      String isic = ibmDirectCmr.getCmrIsic();
      boolean federalPoa = isic != null && (isic.startsWith("90") || isic.startsWith("91") || isic.startsWith("92"));
      if (federalPoa) {
        affiliate = ibmDirectCmr.getCmrEnterpriseNumber();
      }
      if (!StringUtils.isBlank(ibmDirectCmr.getCmrAffiliate())) {
        details.append(" - Affiliate: " + ibmDirectCmr.getCmrAffiliate() + (federalPoa ? " (Enterprise from Federal/POA)" : "") + "\n");
        overrides.addOverride(getProcessCode(), "DATA", "AFFILIATE", data.getAffiliate(), affiliate);
      }

      if (!StringUtils.isBlank(ibmDirectCmr.getCmrIsu())) {
        details.append(" - ISU: " + ibmDirectCmr.getCmrIsu() + "\n");
        overrides.addOverride(getProcessCode(), "DATA", "ISU_CD", data.getIsuCd(), ibmDirectCmr.getCmrIsu());
        details.append(" - Client Tier: " + ibmDirectCmr.getCmrTier() + "\n");
        overrides.addOverride(getProcessCode(), "DATA", "CLIENT_TIER", data.getClientTier(), ibmDirectCmr.getCmrTier());
      }

      if (!StringUtils.isBlank(ibmDirectCmr.getCmrInac())) {
        details
            .append(" - NAC/INAC: " + ("I".equals(ibmDirectCmr.getCmrInacType()) ? "INAC" : ("N".equals(ibmDirectCmr.getCmrInacType()) ? "NAC" : "-"))
                + " " + ibmDirectCmr.getCmrInac() + (ibmDirectCmr.getCmrInacDesc() != null ? "( " + ibmDirectCmr.getCmrInacDesc() + ")" : ""));
        overrides.addOverride(getProcessCode(), "DATA", "INAC_TYPE", data.getInacType(), ibmDirectCmr.getCmrInacType());
        overrides.addOverride(getProcessCode(), "DATA", "INAC_CD", data.getInacCd(), ibmDirectCmr.getCmrInac());
      }

      // add here gbg and cov
      LOG.debug("Getting Buying Group/Coverage values");
      String bgId = ibmDirectCmr.getCmrBuyingGroup();
      GBGResponse calcGbg = new GBGResponse();
      if (!StringUtils.isBlank(bgId)) {
        calcGbg.setBgId(ibmDirectCmr.getCmrBuyingGroup());
        calcGbg.setBgName(ibmDirectCmr.getCmrBuyingGroupDesc());
        calcGbg.setCmrCount(1);
        calcGbg.setGbgId(ibmDirectCmr.getCmrGlobalBuyingGroup());
        calcGbg.setGbgName(ibmDirectCmr.getCmrGlobalBuyingGroupDesc());
        calcGbg.setLdeRule(ibmDirectCmr.getCmrLde());
      } else {
        calcGbg.setBgId("BGNONE");
        calcGbg.setBgName("None");
        calcGbg.setCmrCount(1);
        calcGbg.setGbgId("GBGNONE");
        calcGbg.setGbgName("None");
        calcGbg.setLdeRule("BG_DEFAULT");
      }
      if (!StringUtils.isBlank(calcGbg.getGbgId())) {
        details.append(" - GBG: " + calcGbg.getGbgId() + "(" + (StringUtils.isBlank(calcGbg.getGbgName()) ? "not specified" : calcGbg.getGbgName())
            + ")" + "\n");
      } else {
        details.append(" - GBG: none\n");
      }
      if (!StringUtils.isBlank(calcGbg.getBgId())) {
        details.append(
            " - BG: " + calcGbg.getBgId() + "(" + (StringUtils.isBlank(calcGbg.getBgName()) ? "not specified" : calcGbg.getBgName()) + ")" + "\n");
      } else {
        details.append(" - BG: none\n");
      }
      if (!StringUtils.isBlank(calcGbg.getBgId())) {
        details.append(" - LDE Rule: " + calcGbg.getLdeRule() + "\n");
      } else {
        details.append(" - LDE Rule: none\n");
      }
      engineData.addPositiveCheckStatus(AutomationEngineData.SKIP_GBG);
      engineData.put(AutomationEngineData.GBG_MATCH, calcGbg);

      LOG.debug("BG ID: " + calcGbg.getBgId());
      String calcCovId = ibmDirectCmr.getCmrCoverage();
      if (StringUtils.isBlank(calcCovId)) {
        calcCovId = RequestUtils.getDefaultCoverage(entityManager, "US");
      }
      details.append(
          " - Coverage: " + calcCovId + (ibmDirectCmr.getCmrCoverageName() != null ? " (" + ibmDirectCmr.getCmrCoverageName() + ")" : "") + "\n");
      LOG.debug("Coverage: " + calcCovId);
      engineData.addPositiveCheckStatus(AutomationEngineData.SKIP_COVERAGE);
      engineData.put(AutomationEngineData.COVERAGE_CALCULATED, calcCovId);

      details.append(" - SICMEN: " + ibmDirectCmr.getCmrIsic() + "\n");
      details.append(" - ISIC: " + ibmDirectCmr.getCmrIsic() + "\n");
      details.append(" - Subindustry: " + ibmDirectCmr.getCmrSubIndustry() + "\n");
      overrides.addOverride(getProcessCode(), "DATA", "ISIC_CD", data.getIsicCd(), ibmDirectCmr.getCmrIsic());
      overrides.addOverride(getProcessCode(), "DATA", "US_SICMEN", data.getUsSicmen(), ibmDirectCmr.getCmrIsic());
      overrides.addOverride(getProcessCode(), "DATA", "SUB_INDUSTRY_CD", data.getSubIndustryCd(), ibmDirectCmr.getCmrSubIndustry());
    }

    // do final checks on request data
    overrides.addOverride(getProcessCode(), "DATA", "RESTRICT_IND", data.getRestrictInd(), "Y");
    overrides.addOverride(getProcessCode(), "DATA", "MISC_BILL_CD", data.getMiscBillCd(), "I");

    Addr installAt = requestData.getAddress("ZS01");
    if (installAt != null && SPECIAL_TAX_STATES.contains(installAt.getStateProv())) {
      details.append("Tax Code set to J000 based on state.\n");
      overrides.addOverride(getProcessCode(), "DATA", "TAX_CD1", data.getTaxCd1(), "J000");
    } else {
      details.append("Tax Code set to J666 based on state.\n");
      overrides.addOverride(getProcessCode(), "DATA", "TAX_CD1", data.getTaxCd1(), "J666");
    }
    overrides.addOverride(getProcessCode(), "DATA", "MKTG_DEPT", data.getMktgDept(), "EI3");
    overrides.addOverride(getProcessCode(), "DATA", "SVC_AR_OFFICE", data.getSvcArOffice(), "IKE");

    boolean hasFieldError = false;
    if (!RESTRICT_TO_END_USER.equals(data.getRestrictTo()) && !RESTRICT_TO_MAINTENANCE.equals(data.getRestrictTo())) {
      String msg = "Restrict To value is incorrect for BP End User request.";
      engineData.addNegativeCheckStatus("_usBpData", msg);
      details.append(msg + "\n");
      hasFieldError = true;
    } else {
      if (RESTRICT_TO_END_USER.equals(data.getRestrictTo())) {
        overrides.addOverride(getProcessCode(), "DATA", "MTKG_AR_DEPT", data.getMtkgArDept(), "DI3");
        overrides.addOverride(getProcessCode(), "DATA", "PCC_AR_DEPT", data.getPccArDept(), "G8G");
      } else if (RESTRICT_TO_MAINTENANCE.equals(data.getRestrictTo())) {
        overrides.addOverride(getProcessCode(), "DATA", "MTKG_AR_DEPT", data.getMtkgArDept(), "2NS");
        overrides.addOverride(getProcessCode(), "DATA", "PCC_AR_DEPT", data.getPccArDept(), "G8M");
      }
    }

    USCeIdMapping mapping = null;
    String mappingRule = null;
    if (!StringUtils.isBlank(data.getEnterprise())) {
      mapping = USCeIdMapping.getByEnterprise(data.getEnterprise());
      mappingRule = "E";
    }
    if (mapping == null && !StringUtils.isBlank(data.getPpsceid())) {
      mapping = USCeIdMapping.getByCeid(data.getPpsceid());
      mappingRule = "C";
    }
    details.append("\n");
    if (mapping == null) {
      String msg = "Cannot determine distributor status based on request data.";
      engineData.addNegativeCheckStatus("_usBpData", msg);
      details.append(msg + "\n");
      hasFieldError = true;
    } else {
      if (StringUtils.isBlank(data.getCompany())) {
        overrides.addOverride(getProcessCode(), "DATA", "COMPANY", data.getCompany(), mapping.getCompanyNo());
      }
      boolean distributor = mapping.isDistributor();
      details.append("BP is a distributor based on (" + ("E".equals(mappingRule) ? "Enterprise No." : "CE ID") + ").\n");
      if (distributor) {
        overrides.addOverride(getProcessCode(), "DATA", "CSO_SITE", data.getCsoSite(), "YBV");
        overrides.addOverride(getProcessCode(), "DATA", "BP_NAME", data.getBpName(), BP_MANAGING_IR);
      } else {
        overrides.addOverride(getProcessCode(), "DATA", "CSO_SITE", data.getCsoSite(), "DV4");
        overrides.addOverride(getProcessCode(), "DATA", "BP_NAME", data.getBpName(), BP_INDIRECT_REMARKETER);
      }
    }

    boolean createAddressOverrides = false;
    String mainCustNm1 = "";
    String mainCustNm2 = "";
    String streetAddress1 = "";
    String streetAddress2 = "";
    String city1 = "";
    String postalCd = "";
    String stateProv = "";
    String county = "";
    String dept = "";

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
      dept = childInstallAt.getDept();
      createAddressOverrides = true;
    } else if (ibmDirectCmr != null) {
      // CMR-4033
      String parts[] = handler.doSplitName(ibmDirectCmr.getCmrName(), "", 24, 24);
      mainCustNm1 = parts[0];
      mainCustNm2 = parts[1];
      parts = handler.doSplitAddress(ibmDirectCmr.getCmrStreetAddress(), ibmDirectCmr.getCmrName4(), 24, 24);
      streetAddress1 = parts[0];
      streetAddress2 = parts[1];
      city1 = ibmDirectCmr.getCmrCity();
      postalCd = ibmDirectCmr.getCmrPostalCode();
      stateProv = ibmDirectCmr.getCmrState();
      county = ibmDirectCmr.getCmrCounty();
      dept = ibmDirectCmr.getCmrDept();
      createAddressOverrides = true;
    }

    if (createAddressOverrides) {

      if (!StringUtils.equals(installAt.getDivn(), mainCustNm1) || !StringUtils.equals(installAt.getDept(), mainCustNm2)) {

        String customerName = installAt.getDivn() + (!StringUtils.isBlank(installAt.getDept()) ? " " + installAt.getDept() : "");
        customerName = customerName.toUpperCase();
        String customerNameSuffix = "";
        if (customerName.contains("C/O")) {
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

        overrides.addOverride(getProcessCode(), "ZS01", "DIVN", installAt.getDivn(), StringUtils.isNotBlank(nameParts[0]) ? nameParts[0] : "");
        overrides.addOverride(getProcessCode(), "ZS01", "DEPT", installAt.getDept(), StringUtils.isNotBlank(nameParts[1]) ? nameParts[1] : "");
        details.append("Updated Division to " + (StringUtils.isNotBlank(nameParts[0]) ? nameParts[0] : "-blank-")).append("\n");
        details.append("Updated Department to " + (StringUtils.isNotBlank(nameParts[1]) ? nameParts[1] : "-blank-")).append("\n");
      }

      if (StringUtils.isBlank(installAt.getDept()) && StringUtils.isNotBlank(dept)) {
        overrides.addOverride(getProcessCode(), "ZS01", "DEPT", installAt.getDept(), dept);
        details.append("Updated Department to " + dept).append("\n");
      }

      if (!StringUtils.equals(installAt.getAddrTxt(), streetAddress1)) {
        overrides.addOverride(getProcessCode(), "ZS01", "ADDR_TXT", installAt.getAddrTxt(),
            StringUtils.isNotBlank(streetAddress1) ? streetAddress1.trim() : "");
        details.append("Updated Street Address1 to " + (StringUtils.isNotBlank(streetAddress1) ? streetAddress1.trim() : "-blank-")).append("\n");
      }
      if (!StringUtils.equals(installAt.getAddrTxt2(), streetAddress2)) {
        overrides.addOverride(getProcessCode(), "ZS01", "ADDR_TXT_2", installAt.getAddrTxt2(),
            StringUtils.isNotBlank(streetAddress2) ? streetAddress2.trim() : "");
        details.append("Updated Street Address2 to " + (StringUtils.isNotBlank(streetAddress2) ? streetAddress2.trim() : "-blank-")).append("\n");
      }
      if (!StringUtils.equals(installAt.getCity1(), city1)) {
        overrides.addOverride(getProcessCode(), "ZS01", "CITY1", installAt.getCity1(), StringUtils.isNotBlank(city1) ? city1.trim() : "");
        details.append("Updated City to " + (StringUtils.isNotBlank(city1) ? city1.trim() : "-blank-")).append("\n");
      }
      if (!StringUtils.equals(installAt.getPostCd(), postalCd)) {
        overrides.addOverride(getProcessCode(), "ZS01", "POST_CD", installAt.getPostCd(), StringUtils.isNotBlank(postalCd) ? postalCd.trim() : "");
        details.append("Updated Postal Code to " + (StringUtils.isNotBlank(postalCd) ? postalCd.trim() : "-blank-")).append("\n");
      }
      if (!StringUtils.equals(installAt.getStateProv(), stateProv)) {
        overrides.addOverride(getProcessCode(), "ZS01", "STATE_PROV", installAt.getStateProv(),
            StringUtils.isNotBlank(stateProv) ? stateProv.trim() : "");
        details.append("Updated State/Prov to " + (StringUtils.isNotBlank(stateProv) ? stateProv.trim() : "-blank-")).append("\n");
      }
      if (!StringUtils.equals(installAt.getCounty(), county)) {
        overrides.addOverride(getProcessCode(), "ZS01", "COUNTY", installAt.getCounty(), StringUtils.isNotBlank(county) ? county.trim() : "");
        overrides.addOverride(getProcessCode(), "ZS01", "COUNTY_NAME", installAt.getCountyName(),
            StringUtils.isNotBlank(county) ? county.trim() : "");
        details.append("Updated County to " + (StringUtils.isNotBlank(county) ? county.trim() : "-blank-")).append("\n");
      }
    }

    if (!hasFieldError) {
      details.append("Branch Office codes computed successfully.");
      engineData.addPositiveCheckStatus(AutomationEngineData.BO_COMPUTATION);
    }

  }

  /**
   * Checks if the cmrNo has a blank restriction code in US CMR DB
   * 
   * @param cmrNo
   * @return
   */
  private boolean hasBlankRestrictionCodeInUSCMR(String cmrNo) {
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

  }

  /**
   * Gets the Affiliate from US CMR
   * 
   * @param cmrNo
   * @return
   */
  private String getUSCMRAffiliate(String cmrNo) {
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
  private String getUSCMREnterprise(String cmrNo) {
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
   * Gets the CMR no from the child rejection. If null, it means the rejection
   * is not due to duplicate CMR
   * 
   * @param reqId
   * @return
   */
  private WfHist getWfHistForRejection(EntityManager entityManager, long reqId) {
    String sql = ExternalizedQuery.getSql("AUTO.US.GET_REJECTION");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", reqId);
    query.setForReadOnly(true);
    return query.getSingleResult(WfHist.class);
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
  private void processChildCancellation(EntityManager entityManager, StringBuilder details, RequestData requestData, RequestData childRequestData,
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
        rejectCmt = "An ongoing request for the Direct CMR is being processed at the moment. Please try to resubmit the request after 15 mins.";
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
   * 
   * Cleans the given name into US-CMR standards
   * 
   * @param name
   * @return
   */
  private static String cleanName(String name) {
    if (name == null) {
      return "";
    }
    name = name.replaceAll("'", "");
    name = name.replaceAll("[^A-Za-z0-9&\\-/]", " ");
    name = name.replaceAll("  ", " ").toUpperCase();
    return name;
  }

  @Override
  public boolean isWaiting() {
    return this.waiting;
  }

  @Override
  public String getProcessCode() {
    return AutomationElementRegistry.US_BP_PROCESS;
  }

  @Override
  public String getProcessDesc() {
    return "US - Business Partner Process";
  }

}
