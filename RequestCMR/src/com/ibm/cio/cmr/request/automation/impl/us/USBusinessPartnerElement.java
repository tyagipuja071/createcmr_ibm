package com.ibm.cio.cmr.request.automation.impl.us;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
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
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.entity.Scorecard;
import com.ibm.cio.cmr.request.model.requestentry.FindCMRRecordModel;
import com.ibm.cio.cmr.request.model.requestentry.FindCMRResultModel;
import com.ibm.cio.cmr.request.model.requestentry.RequestEntryModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
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
import com.ibm.cmr.services.client.pps.PPSProfile;
import com.ibm.cmr.services.client.pps.PPSRequest;
import com.ibm.cmr.services.client.pps.PPSResponse;
import com.ibm.cmr.services.client.query.QueryRequest;
import com.ibm.cmr.services.client.query.QueryResponse;
import com.ibm.json.java.JSONObject;

import edu.emory.mathcs.backport.java.util.Collections;

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

    // check the scenario
    if ("U".equals(admin.getReqType())) {
      output.setResults("Skipped");
      output.setDetails("Update types not supported.");
      return output;
    }

    String custGrp = data.getCustGrp();
    String custSubGrp = data.getCustSubGrp();
    if ("BYMODEL".equals(custSubGrp)) {
      String type = admin.getCustType();
      if (!USUtil.BUSINESS_PARTNER.equals(type) || !"E".equals(data.getBpAcctTyp())
          || (!RESTRICT_TO_END_USER.equals(data.getRestrictTo()) && !RESTRICT_TO_MAINTENANCE.equals(data.getRestrictTo()))) {
        output.setResults("Skipped");
        output.setDetails("Non BP End User create by model scenario not supported.");
        return output;
      }
    } else if (!TYPE_BUSINESS_PARTNER.equals(custGrp) || !SUB_TYPE_BUSINESS_PARTNER_END_USER.equals(custSubGrp)) {
      output.setResults("Skipped");
      output.setDetails("Non BP End User scenario not supported.");
      return output;
    }

    if (StringUtils.isBlank(data.getPpsceid())) {
      String msg = "PPS CEID is required for Business Partner requests.";
      engineData.addRejectionComment("OTH", msg, "", "");
      output.setOnError(true);
      output.setDetails(msg);
      output.setResults("CEID Missing");
      return output;
    }

    USHandler handler = new USHandler();
    OverrideOutput overrides = new OverrideOutput(false);
    StringBuilder details = new StringBuilder();
    Addr addr = requestData.getAddress("ZS01");

    FindCMRRecordModel ibmDirectCmr = null;
    long childReqId = admin.getChildReqId();
    String childCmrNo = null;
    if (childReqId > 0) {
      // check child reqId processing first
      LOG.debug("Getting child request data for Request " + childReqId);
      RequestData childRequest = new RequestData(entityManager, childReqId);
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
        // try to check once if an ibm direct cmr is available
        LOG.debug("Trying to check once for a Direct CMR for the record");
        ibmDirectCmr = findIBMDirectCMR(entityManager, handler, requestData, addr, engineData, null);
        if (ibmDirectCmr != null) {
          setError = false;
        }

        if (setError) {
          LOG.debug("Child request processing has been stopped (Rejected or Sent back to requesters). Sending to processors.");
          String msg = "IBM Direct Request " + childReqId
              + " has been sent back to requesters and no IBM Direct CMRs were found. The request needs to be manually processed.";
          details.append(msg);
          details.append("\n");
          details.append("No field value has been computed for this record.");
          // engineData.addNegativeCheckStatus("_usBpRejected", msg);
          engineData.addRejectionComment("OTH", msg, "", "");
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
      }
    }

    // check IBM Direct CMR
    if (ibmDirectCmr == null) {
      // if a rejected child caused the retrieval of a child cmr
      ibmDirectCmr = findIBMDirectCMR(entityManager, handler, requestData, addr, engineData, childCmrNo);
    }
    if (ibmDirectCmr != null) {
      LOG.debug("IBM Direct CMR Found: " + ibmDirectCmr.getCmrNum() + " - " + ibmDirectCmr.getCmrName());
    }
    // match against D&B
    DnBMatchingResponse dnbMatch = matchAgainstDnB(handler, requestData, addr, engineData, details, ibmDirectCmr != null);

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
          // engineData.addNegativeCheckStatus("_usBpRejected", childErrorMsg);
          engineData.addRejectionComment("OTH", childErrorMsg, "", "");
          output.setOnError(true);
          output.setResults("Issues Encountered");
          output.setDetails(details.toString());
          return output;
        } else {
          details.append("Child Request " + childReqId + " created for the IBM Direct CMR record of " + addr.getDivn()
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

    // copy from IBM Direct if found, and fill the rest of BO codes
    copyAndFillIBMData(handler, ibmDirectCmr, requestData, engineData, details, overrides);

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

    USCeIdMapping mapping = USCeIdMapping.getByCeid(data.getPpsceid());
    String enterpriseNo = data.getEnterprise();
    if (mapping != null) {
      if (!mapping.getEnterpriseNo().equals(enterpriseNo)) {
        details.append("\nEnterprise No. updated to mapped value for the CEID (" + mapping.getEnterpriseNo() + ").");
        overrides.addOverride(getProcessCode(), "DATA", "ENTERPRISE", enterpriseNo, mapping.getEnterpriseNo());
        enterpriseNo = mapping.getEnterpriseNo();
      }
      if (!mapping.getCompanyNo().equals(data.getCompany())) {
        details.append("\nCompany No. updated to mapped value for the CEID (" + mapping.getCompanyNo() + ").");
        overrides.addOverride(getProcessCode(), "DATA", "COMPANY", data.getCompany(), mapping.getCompanyNo());
      }
    } else {
      details.append("\nEnterprise No. and CEID combination cannot be validated automatically.");
      engineData.addNegativeCheckStatus("_usBpEnt", "Enterprise No. and CEID combination cannot be validated automatically.");
    }
    if (StringUtils.isBlank(enterpriseNo)) {
      details.append("\nEnterprise No. cannot be computed automatically.\n");
      engineData.addNegativeCheckStatus("_usBpEnt", "Enterprise No. cannot be computed automatically");
    }

    output.setProcessOutput(overrides);
    output.setDetails(details.toString());
    output.setResults("Success");
    return output;
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
      StringBuilder details, boolean hasExistingCmr) throws Exception {
    List<DnBMatchingResponse> dnbMatches = USUtil.getMatchesForBPEndUser(handler, requestData, engineData);
    if (dnbMatches.isEmpty()) {
      LOG.debug("No D&B matches found for the End User " + addr.getDivn());
      String msg = "No high quality D&B matches for the End User " + addr.getDivn();
      details.append(msg + "\n");
      if (hasExistingCmr) {
        details.append("- A current active CMR exists for the company.");
      } else {
        engineData.addNegativeCheckStatus("_usBpNoMatch", msg);
      }
      details.append("\n");
      return null;
    } else {
      DnBMatchingResponse dnbMatch = dnbMatches.get(0);
      LOG.debug("D&B match found for " + addr.getDivn() + " with DUNS " + dnbMatch.getDunsNo());
      details.append("D&B match/es found for the End User " + addr.getDivn() + ". Highest Match:\n");
      details.append("\n");
      details.append(dnbMatch.getDnbName() + "\n");
      details.append(dnbMatch.getDnbStreetLine1() + "\n");
      if (!StringUtils.isBlank(dnbMatch.getDnbStreetLine2())) {
        details.append(dnbMatch.getDnbStreetLine2() + "\n");
      }
      details.append(dnbMatch.getDnbCity() + ", " + dnbMatch.getDnbStateProv() + "\n");
      details.append(dnbMatch.getDnbCountry() + " " + dnbMatch.getDnbPostalCode() + "\n\n");
      return dnbMatch;
    }

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
      DuplicateCMRCheckRequest request = new DuplicateCMRCheckRequest();
      request.setIssuingCountry(SystemLocation.UNITED_STATES);
      request.setLandedCountry(addr.getLandCntry());
      request.setCustomerName(addr.getDivn());
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
    model.setCmrIssuingCntry(SystemLocation.UNITED_STATES);
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
    String[] names = handler.doSplitName(cleanName(bpAddr.getDivn()), "", 28, 22);
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
    String isic = data.getIsicCd();
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
  private void copyAndFillIBMData(GEOHandler handler, FindCMRRecordModel ibmDirectCmr, RequestData requestData, AutomationEngineData engineData,
      StringBuilder details, OverrideOutput overrides) {
    Data data = requestData.getData();
    if (ibmDirectCmr != null) {
      details.append("\nCopying IBM Codes from IBM Direct CMR " + ibmDirectCmr.getCmrNum() + " - " + ibmDirectCmr.getCmrName() + " ("
          + ibmDirectCmr.getCmrSapNumber() + "): \n");

      if (!StringUtils.isBlank(ibmDirectCmr.getCmrAffiliate())) {
        details.append(" - Affiliate: " + ibmDirectCmr.getCmrAffiliate() + "\n");
        overrides.addOverride(getProcessCode(), "DATA", "AFFILIATE", data.getAffiliate(), ibmDirectCmr.getCmrAffiliate());
      }

      if (!StringUtils.isBlank(ibmDirectCmr.getCmrIsu())) {
        details.append(" - ISU: " + ibmDirectCmr.getCmrIsu() + "\n");
        overrides.addOverride(getProcessCode(), "DATA", "ISU_CD", data.getIsuCd(), ibmDirectCmr.getCmrIsu());
      }

      if (!StringUtils.isBlank(ibmDirectCmr.getCmrTier())) {
        details.append(" - Client Tier: " + ibmDirectCmr.getCmrTier() + "\n");
        overrides.addOverride(getProcessCode(), "DATA", "CLIENT_TIER", data.getClientTier(), ibmDirectCmr.getCmrTier());
      }

      if (!StringUtils.isBlank(ibmDirectCmr.getCmrInac())) {
        details.append(
            " - NAC/INAC: " + ("I".equals(ibmDirectCmr.getCmrInacType()) ? "INAC" : ("N".equals(ibmDirectCmr.getCmrInacType()) ? "NAC" : "-")));
        overrides.addOverride(getProcessCode(), "DATA", "INAC_TYPE", data.getInacType(), ibmDirectCmr.getCmrInacType());
        overrides.addOverride(getProcessCode(), "DATA", "INAC_CD", data.getInacCd(), ibmDirectCmr.getCmrInac());
      }

      details.append((StringUtils.isBlank(ibmDirectCmr.getCmrInac()) ? " - " + ibmDirectCmr.getCmrInac() : "") + "\n");
      details.append(" - ISIC: " + ibmDirectCmr.getCmrIsic() + "\n");
      details.append(" - Subindustry: " + ibmDirectCmr.getCmrSubIndustry() + "\n");

      overrides.addOverride(getProcessCode(), "DATA", "ISIC_CD", data.getIsicCd(), ibmDirectCmr.getCmrIsic());
      overrides.addOverride(getProcessCode(), "DATA", "SUB_INDUSTRY_CD", data.getSubIndustryCd(), ibmDirectCmr.getCmrSubIndustry());
    }

    // do final checks on request data
    overrides.addOverride(getProcessCode(), "DATA", "RESTRICT_IND", data.getRestrictInd(), "Y");
    overrides.addOverride(getProcessCode(), "DATA", "MISC_BILL_CD", data.getMiscBillCd(), "I");
    overrides.addOverride(getProcessCode(), "DATA", "TAX_CD1", data.getTaxCd1(), "J666");
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
        LOG.warn("Not successful executiong of US CMR query");
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
   * 
   * Cleans the given name into US-CMR standards
   * 
   * @param name
   * @return
   */
  private String cleanName(String name) {
    if (name == null) {
      return "";
    }
    name = name.replaceAll("'", "");
    name = name.replaceAll("[^A-Za-z0-9&\\-]", " ");
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
