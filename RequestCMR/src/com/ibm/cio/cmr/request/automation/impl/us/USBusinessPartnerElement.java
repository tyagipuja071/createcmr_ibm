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
    if ("BYMODEL".equals(data.getCustGrp())) {
      // TODO insert logic here to determine scenario by model
    }
    if (!"5".equals(custGrp) || !"END USER".equals(custSubGrp)) {
      output.setResults("Skipped");
      output.setDetails("Non BP End User scenario not supported.");
      return output;
    }

    USHandler handler = new USHandler();
    OverrideOutput overrides = new OverrideOutput(false);
    StringBuilder details = new StringBuilder();
    Addr addr = requestData.getAddress("ZS01");

    long childReqId = admin.getChildReqId();
    if (childReqId > 0) {
      // 1 - check child reqId processing first
      LOG.debug("Getting child request data for Request " + childReqId);
      RequestData childRequest = new RequestData(entityManager, childReqId);
      if (childRequest == null || childRequest.getAdmin() == null) {
        String msg = "Child request " + childReqId + " missing. The request needs to be manually processed.";
        details.append(msg);
        details.append("\n");
        details.append("No field value has been computed for this record.");
        engineData.addRejectionComment(msg);
        output.setOnError(true);
        output.setDetails(details.toString());
        output.setResults("Issues Encountered");
        return output;
      }
      String childReqStatus = childRequest.getAdmin().getReqStatus();
      if ("PRJ".equals(childReqStatus) || "DRA".equals(childReqStatus) || "CAN".endsWith(childReqStatus)) {
        LOG.debug("Child request processing has been stopped (Rejected or Sent back to requesters). Sending to processors.");
        String msg = "IBM Direct Request " + childReqId + " has been sent back to requesters. The request needs to be manually processed.";
        details.append(msg);
        details.append("\n");
        details.append("No field value has been computed for this record.");
        // engineData.addNegativeCheckStatus("_usBpRejected", msg);
        engineData.addRejectionComment(msg);
        output.setOnError(true);
        output.setDetails(details.toString());
        output.setResults("Issues Encountered");
        return output;
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
      }
    }
    // 2 - match against D&B
    DnBMatchingResponse dnbMatch = matchAgainstDnB(handler, requestData, addr, engineData, details);

    // check CEID
    boolean t1 = isTier1BP(data);
    if (!t1) {
      engineData.addNegativeCheckStatus("_usBpT1", "BP is not a T1 or status cannot be determined via PPS profile.");
    }

    // 4 - check IBM Direct CMR
    FindCMRRecordModel ibmDirectCmr = findIBMDirectCMR(entityManager, handler, requestData, addr, engineData);
    if (ibmDirectCmr == null) {

      LOG.debug("No IBM Direct CMR for the end user.");
      childReqId = createChildRequest(entityManager, requestData, engineData);
      details.append("No IBM Direct CMR for the end user.\n");

      String childErrorMsg = "- IBM Direct CMR request creation cannot be done, errors were encountered -";
      if (childReqId <= 0) {
        details.append(childErrorMsg + "\n");
        // engineData.addNegativeCheckStatus("_usBpRejected", childErrorMsg);
        engineData.addRejectionComment(childErrorMsg);
        output.setDetails(details.toString());
        output.setOnError(true);
        output.setResults("Issues Encountered");
        return output;
      } else {
        String childDetails = completeChildRequestDataAndAddress(entityManager, requestData, engineData, childReqId, dnbMatch);
        if (childDetails == null) {
          details.append(childErrorMsg + "\n");
          // engineData.addNegativeCheckStatus("_usBpRejected", childErrorMsg);
          engineData.addRejectionComment(childErrorMsg);
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
    } else {
      // copy IBM Direct CMR details
      details.append("\nCopying IBM Codes from IBM Direct CMR " + ibmDirectCmr.getCmrNum() + " (" + ibmDirectCmr.getCmrSapNumber() + "): \n");
      details.append(" - Affiliate: " + ibmDirectCmr.getCmrAffiliate() + "\n");
      details.append(" - ISU: " + ibmDirectCmr.getCmrIsu() + "\n");
      details.append(" - Client Tier: " + ibmDirectCmr.getCmrTier() + "\n");
      details
          .append(" - NAC/INAC: " + ("I".equals(ibmDirectCmr.getCmrInacType()) ? "INAC" : ("N".equals(ibmDirectCmr.getCmrInacType()) ? "NAC" : "-")));
      details.append((StringUtils.isBlank(ibmDirectCmr.getCmrInac()) ? " - " + ibmDirectCmr.getCmrInac() : "") + "\n");
      details.append(" - ISIC: " + ibmDirectCmr.getCmrIsic() + "\n");
      details.append(" - Subindustry: " + ibmDirectCmr.getCmrSubIndustry() + "\n");

      overrides.addOverride(getProcessCode(), "DATA", "AFFILIATE", data.getAffiliate(), ibmDirectCmr.getCmrAffiliate());
      overrides.addOverride(getProcessCode(), "DATA", "ISU_CD", data.getIsuCd(), ibmDirectCmr.getCmrIsu());
      overrides.addOverride(getProcessCode(), "DATA", "CLIENT_TIER", data.getClientTier(), ibmDirectCmr.getCmrTier());
      overrides.addOverride(getProcessCode(), "DATA", "INAC_TYPE", data.getInacType(), ibmDirectCmr.getCmrInacType());
      overrides.addOverride(getProcessCode(), "DATA", "INAC_CD", data.getInacCd(), ibmDirectCmr.getCmrInac());
      overrides.addOverride(getProcessCode(), "DATA", "ISIC_CD", data.getIsicCd(), ibmDirectCmr.getCmrIsic());
      overrides.addOverride(getProcessCode(), "DATA", "SUB_INDUSTRY_CD", data.getSubIndustryCd(), ibmDirectCmr.getCmrSubIndustry());
    }

    // do final checks on request data
    overrides.addOverride(getProcessCode(), "DATA", "RESTRICT_IND", data.getRestrictInd(), "Y");
    overrides.addOverride(getProcessCode(), "DATA", "MISC_BILL_CD", data.getMiscBillCd(), "I");
    overrides.addOverride(getProcessCode(), "DATA", "TAX_CD1", data.getTaxCd1(), "J666");
    overrides.addOverride(getProcessCode(), "DATA", "MKTG_DEPT", data.getMktgDept(), "EI3");
    overrides.addOverride(getProcessCode(), "DATA", "PCC_AR_DEPT", data.getPccArDept(), "G8M");
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
      } else if (RESTRICT_TO_MAINTENANCE.equals(data.getRestrictTo())) {
        if (!"7NZ".equals(data.getMtkgArDept()) && !"2NS".equals(data.getMtkgArDept())) {
          String msg = "Marketing A/R Department for End User - Maintenance request cannot be validated.";
          engineData.addNegativeCheckStatus("_usBpData", msg);
          details.append(msg + "\n");
          hasFieldError = true;
        }
      }
    }

    USCeIdMapping mapping = null;
    if (!StringUtils.isBlank(data.getEnterprise())) {
      mapping = USCeIdMapping.getByEnterprise(data.getEnterprise());
    }
    if (mapping == null && !StringUtils.isBlank(data.getPpsceid())) {
      mapping = USCeIdMapping.getByCeid(data.getPpsceid());
    }
    details.append("\n");
    if (mapping == null) {
      String msg = "Cannot determine distributor status based on request data.";
      engineData.addNegativeCheckStatus("_usBpData", msg);
      details.append(msg + "\n");
      hasFieldError = true;
    } else {
      boolean distributor = mapping.isDistributor();
      if (distributor) {
        overrides.addOverride(getProcessCode(), "DATA", "CSO_SITE", data.getCsoSite(), "YBV");
        overrides.addOverride(getProcessCode(), "DATA", "BP_NAME", data.getBpName(), BP_MANAGING_IR);
      } else {
        overrides.addOverride(getProcessCode(), "DATA", "CSO_SITE", data.getCsoSite(), "DV4");
        overrides.addOverride(getProcessCode(), "DATA", "BP_NAME", data.getBpName(), BP_INDIRECT_REMARKETER);
      }
    }
    if (!hasFieldError) {
      details.append("\n");
      details.append("Branch Office codes computed successfully.");
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
      StringBuilder details) throws Exception {
    List<DnBMatchingResponse> dnbMatches = USUtil.getMatchesForBPEndUser(handler, requestData, engineData);
    if (dnbMatches.isEmpty()) {
      LOG.debug("No D&B matches found for the End User " + addr.getDivn());
      String msg = "No high quality D&B matches for the End User " + addr.getDivn();
      details.append(msg + "\n\n");
      engineData.addNegativeCheckStatus("_usBpNoMatch", msg);
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
      AutomationEngineData engineData) throws Exception {
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
        if (StringUtils.isBlank(record.getUsRestrictTo())) {
          // IBM Direct CMRs have blank restrict to
          FindCMRResultModel result = CompanyFinder.getCMRDetails(SystemLocation.UNITED_STATES, record.getCmrNo(), 1, null, "addressType=ZS01");
          if (result != null && result.getItems() != null && !result.getItems().isEmpty()) {
            return result.getItems().get(0);
          }
        }
      }
    }

    // do a secondary check directly from RDC
    LOG.debug("No IBM Direct CMRs found in FindCMR. Checking directly on the database..");
    String sql = ExternalizedQuery.getSql("AUTO.US.GET_IBM_DIRECT");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setForReadOnly(true);
    query.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
    query.setParameter("NAME", addr.getDivn().toUpperCase() + "%");
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
      return record;
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
    // if (model.getReqId() <= 0) {
    // LOG.debug("Request ID was not generated");
    // return -1;
    // }

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
    LOG.debug("Creating Address for Child Request " + childReqId);
    entityManager.persist(childAddr);

    USHandler handler = new USHandler();
    String procCenter = RequestUtils.getProcessingCenter(entityManager, SystemLocation.UNITED_STATES);
    RequestData childReqData = new RequestData(entityManager, childReqId);
    Admin childAdmin = childReqData.getAdmin();
    childAdmin.setReqStatus(AutomationConst.STATUS_AUTOMATED_PROCESSING);
    childAdmin.setSourceSystId("CreateCMR");
    String[] names = handler.doSplitName(bpAddr.getDivn(), "", 28, 22);
    childAdmin.setMainCustNm1(names[0]);
    childAdmin.setMainCustNm2(names[1]);
    childAdmin.setLockBy(null);
    childAdmin.setLockByNm(null);
    childAdmin.setLockInd("N");
    childAdmin.setLockTs(null);
    childAdmin.setLastProcCenterNm(procCenter);
    LOG.debug("Updating Child Admin data..");
    entityManager.merge(childAdmin);

    // TODO commercial for now, fix soon
    Data childData = childReqData.getData();
    details.append(" - Type: Commercial\n");
    details.append(" - Sub-type: Regular Commercial CMR\n");
    childData.setCustGrp("1");
    childData.setCustSubGrp("REGULAR");
    childData.setCustAcctType(USUtil.COMMERCIAL);
    LOG.debug("Updating Child data..");
    loadTemplateDefaults(childData, childData.getCustSubGrp());
    if (StringUtils.isBlank(childData.getIsicCd())) {
      childData.setIsicCd(data.getIsicCd());
      childData.setSubIndustryCd(data.getSubIndustryCd());
    }
    Scorecard childScoreCard = childReqData.getScorecard();
    childScoreCard.setFindCmrResult(CmrConstants.RESULT_NO_RESULT);
    childScoreCard.setFindDnbTs(SystemUtil.getCurrentTimestamp());
    childScoreCard.setFindDnbUsrId("CreateCMR");
    childScoreCard.setFindDnbUsrNm("CreateCMR");

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
      childScoreCard.setFindDnbTs(SystemUtil.getCurrentTimestamp());
      childScoreCard.setFindDnbUsrId("CreateCMR");
      childScoreCard.setFindDnbUsrNm("CreateCMR");
    }
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
          if (!Arrays.asList("*", "%", "$", "~").contains(firstVal)) {
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
          if ("T1".equals(profile.getTierCode())) {
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
