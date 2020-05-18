package com.ibm.cio.cmr.request.automation.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import com.ibm.cio.cmr.request.automation.AutomationEngineData;
import com.ibm.cio.cmr.request.automation.RequestData;
import com.ibm.cio.cmr.request.automation.impl.gbl.CalculateCoverageElement;
import com.ibm.cio.cmr.request.automation.out.AutomationResult;
import com.ibm.cio.cmr.request.automation.out.OverrideOutput;
import com.ibm.cio.cmr.request.automation.out.ValidationOutput;
import com.ibm.cio.cmr.request.automation.util.geo.AustraliaUtil;
import com.ibm.cio.cmr.request.automation.util.geo.BrazilUtil;
import com.ibm.cio.cmr.request.automation.util.geo.FranceUtil;
import com.ibm.cio.cmr.request.automation.util.geo.GermanyUtil;
import com.ibm.cio.cmr.request.automation.util.geo.SingaporeUtil;
import com.ibm.cio.cmr.request.automation.util.geo.SwitzerlandUtil;
import com.ibm.cio.cmr.request.automation.util.geo.USUtil;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.entity.DataPK;
import com.ibm.cio.cmr.request.entity.DataRdc;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.util.BluePagesHelper;
import com.ibm.cio.cmr.request.util.Person;
import com.ibm.cio.cmr.request.util.SystemLocation;
import com.ibm.cio.cmr.request.util.dnb.DnBUtil;
import com.ibm.cmr.services.client.CmrServicesFactory;
import com.ibm.cmr.services.client.MatchingServiceClient;
import com.ibm.cmr.services.client.PPSServiceClient;
import com.ibm.cmr.services.client.ServiceClient.Method;
import com.ibm.cmr.services.client.matching.MatchingResponse;
import com.ibm.cmr.services.client.matching.cmr.DuplicateCMRCheckRequest;
import com.ibm.cmr.services.client.matching.cmr.DuplicateCMRCheckResponse;
import com.ibm.cmr.services.client.matching.dnb.DnBMatchingResponse;
import com.ibm.cmr.services.client.matching.gbg.GBGFinderRequest;
import com.ibm.cmr.services.client.pps.PPSRequest;
import com.ibm.cmr.services.client.pps.PPSResponse;

/**
 *
 * Interface to handle country specific utility methods
 *
 * @author RoopakChugh
 *
 */

public abstract class AutomationUtil {

  private static final Logger LOG = Logger.getLogger(AutomationUtil.class);

  protected static final Map<String, Class<? extends AutomationUtil>> GEO_UTILS = new HashMap<String, Class<? extends AutomationUtil>>() {
    private static final long serialVersionUID = 1L;
    {
      put(SystemLocation.BRAZIL, BrazilUtil.class);
      put(SystemLocation.SINGAPORE, SingaporeUtil.class);
      put(SystemLocation.AUSTRALIA, AustraliaUtil.class);
      put(SystemLocation.GERMANY, GermanyUtil.class);
      put(SystemLocation.UNITED_STATES, USUtil.class);

      // FRANCE Sub Regions
      put(SystemLocation.FRANCE, FranceUtil.class);
      put(SystemLocation.ANDORRA, FranceUtil.class);
      put(SystemLocation.VANUATU, FranceUtil.class);
      put(SystemLocation.FRENCH_GUIANA, FranceUtil.class);
      put(SystemLocation.FRENCH_POLYNESIA_TAHITI, FranceUtil.class);
      put(SystemLocation.GUATEMALA, FranceUtil.class);
      put(SystemLocation.MAYOTTE, FranceUtil.class);
      put(SystemLocation.NEW_CALEDONIA, FranceUtil.class);
      put(SystemLocation.WALLIS_FUTUNA, FranceUtil.class);
      put(SystemLocation.COMOROS, FranceUtil.class);
      put(SystemLocation.SAINT_PIERRE_MIQUELON, FranceUtil.class);

      put(SystemLocation.SWITZERLAND, SwitzerlandUtil.class);
      put(SystemLocation.LIECHTENSTEIN, SwitzerlandUtil.class);

    }
  };

  private static final List<String> GLOBAL_LEGAL_ENDINGS = Arrays.asList("COMPANY", " CO", "CORP", "CORPORATION", "LTD", "LIMITED", "LLC", "INC",
      "INCORPORATED");

  /**
   * Holds the possible return values of
   * {@link SwitzerlandUtil#checkPrivatePersonRecord(Addr, boolean)}
   * 
   * @author JeffZAMORA
   *
   */
  protected static enum PrivatePersonCheckStatus {
    Passed, PassedBoth, DuplicateCMR, NoIBMRecord, DuplicateCheckError, BluepagesError;
  }

  private static final List<String> VAT_CHECK_COUNTRIES = Arrays.asList(SystemLocation.BRAZIL);

  /**
   * returns an instance of
   *
   * @param cmrIssuingCntry
   * @return
   * @throws InstantiationException
   * @throws IllegalAccessException
   */
  public static AutomationUtil getNewCountryUtil(String cmrIssuingCntry) throws IllegalAccessException, InstantiationException {
    if (!GEO_UTILS.containsKey(cmrIssuingCntry)) {
      return null;
    }
    try {
      Class<? extends AutomationUtil> utilClass = GEO_UTILS.get(cmrIssuingCntry);
      return utilClass.newInstance();
    } catch (Throwable t) {
      LOG.warn("Automation Util for " + cmrIssuingCntry + " found but cannot be initialized.", t);
      return null;
    }
  }

  /**
   *
   * Computes IBM field values specific to countries and scenarios.
   *
   * @param results
   * @param details
   * @param overrides
   * @param requestData
   * @param engineData
   * @return
   * @throws Exception
   */
  public abstract AutomationResult<OverrideOutput> doCountryFieldComputations(EntityManager entityManager, AutomationResult<OverrideOutput> results,
      StringBuilder details, OverrideOutput overrides, RequestData requestData, AutomationEngineData engineData) throws Exception;

  /**
   * validates if the scenario on request is correct or not
   *
   * @param entityManager
   * @param requestData
   * @param engineData
   * @param result
   * @param details
   * @param putput
   * @param rejectionComment
   * @return
   */
  public abstract boolean performScenarioValidation(EntityManager entityManager, RequestData requestData, AutomationEngineData engineData,
      AutomationResult<ValidationOutput> result, StringBuilder details, ValidationOutput output);

  /**
   * This method should be overridden by implementing classes and
   * <strong>always</strong> return true if there are country specific logic
   * 
   * @param covElement
   * @param entityManager
   * @param results
   * @param details
   * @param overrides
   * @param requestData
   * @param engineData
   * @param covFrom
   * @param container
   * @param isCoverageCalculated
   * @return
   * @throws Exception
   */
  public boolean performCountrySpecificCoverageCalculations(CalculateCoverageElement covElement, EntityManager entityManager,
      AutomationResult<OverrideOutput> results, StringBuilder details, OverrideOutput overrides, RequestData requestData,
      AutomationEngineData engineData, String covFrom, CoverageContainer container, boolean isCoverageCalculated) throws Exception {
    return false;
  }

  /**
   *
   * Gets the default cluster code for country.
   *
   * @param results
   * @param details
   * @param overrides
   * @param requestData
   * @param engineData
   * @return
   */
  public String getDefaultCluster(EntityManager entityManager, RequestData requestData, AutomationEngineData engineData) {
    String defaultClusterCd = "";
    Data data = requestData.getData();
    String sql = ExternalizedQuery.getSql("AUTO.GET.DEFAULT_CLUSTER");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("ISSUING_CNTRY", data.getCmrIssuingCntry());
    List<String> defaultClusters = query.getResults(String.class);
    if (defaultClusters != null && defaultClusters.size() > 0) {
      defaultClusterCd = defaultClusters.get(0);
    }
    return defaultClusterCd;
  }

  /**
   *
   * Validates the Cluster and SalesMan Combination.
   *
   * @return a) validCode = "NO_RESULTS" if no rows found on CREQCMR.REP_TEAM b)
   *         validCode = "INVALID" if cluster exist but with different SalesMan
   *         on CREQCMR.REP_TEAM c) validCode = "VALID" if cluster exist with
   *         request's SalesMan on CREQCMR.REP_TEAM
   */
  public String checkIfClusterSalesmanIsValid(EntityManager entityManager, RequestData requestData) {
    String validCode = null;
    Data data = requestData.getData();
    String sql = ExternalizedQuery.getSql("AUTO.VALIDATE_CLUSTER_SALESMAN");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("ISSUING_CNTRY", data.getCmrIssuingCntry());
    query.setParameter("REP_TEAM_CD", data.getApCustClusterId());
    query.setParameter("REP_TEAM_MEMBER_NO", data.getRepTeamMemberNo());
    List<String> results = query.getResults(String.class);
    if (results != null && results.size() > 0) {
      for (String res : results) {
        if (res.equals(data.getRepTeamMemberNo())) {
          validCode = "VALID";
        }
      }
      validCode = ((validCode == null) ? "INVALID" : validCode);
    } else {
      validCode = "NO_RESULTS";
    }
    return validCode;
  }

  /**
   * Returns true if issuing country is configured to perform VAT match
   *
   * @param cmrIssuingCntry
   * @return
   */
  public static boolean isCheckVatForDuplicates(String cmrIssuingCntry) {
    if (StringUtils.isNotBlank(cmrIssuingCntry)) {
      return VAT_CHECK_COUNTRIES.contains(cmrIssuingCntry);
    }
    return false;
  }

  /**
   * Checks if ISIC and Subindustry is valid for specified scenario
   *
   * @param scenarioList
   * @return inValid
   */

  public static boolean isISICValidForScenario(RequestData requestData, List<String> scenarioList) {
    boolean isInvalid = false;
    // get request admin and data
    Admin admin = requestData.getAdmin();
    Data data = requestData.getData();
    String scenarioSubType = "";
    String isicCode = "";
    String subindustry = "";
    boolean isIsicValid = true;
    boolean isSubIndValid = true;
    if ("C".equals(admin.getReqType()) && data != null) {
      scenarioSubType = data.getCustSubGrp();
    }

    if (data != null) {
      isicCode = data.getIsicCd();
      subindustry = data.getSubIndustryCd();
    }

    if (scenarioSubType != null && StringUtils.isNotBlank(scenarioSubType) && scenarioList != null && scenarioList.contains(scenarioSubType)) {
      // check if ISIC does not contains letters
      if (isicCode != null && StringUtils.isNotBlank(isicCode) && isicCode.matches("[0-9]*[a-zA-Z]")) {
        isIsicValid = false;
      }
      // Check that Subindustry is not under Z* (any starting with Z).
      if (subindustry != null && StringUtils.isNotBlank(subindustry) && subindustry.startsWith("Z")) {
        isSubIndValid = false;
      }

      if (!isIsicValid || !isSubIndValid) {
        isInvalid = true;
      }
    }

    return isInvalid;
  }

  /**
   * This method should be overridden by implementing classes and
   * <strong>always</strong> return true if there are country specific logic
   * 
   * @param entityManager
   * @param engineData
   * @param requestData
   * @param changes
   * @param output
   * @param validation
   * @return
   * @throws Exception
   */
  public boolean runUpdateChecksForData(EntityManager entityManager, AutomationEngineData engineData, RequestData requestData,
      RequestChangeContainer changes, AutomationResult<ValidationOutput> output, ValidationOutput validation) throws Exception {
    return false; // false denotes no country specific runs
  }

  /**
   * This method should be overridden by implementing classes and
   * <strong>always</strong> return true if there are country specific logic
   * 
   * @param entityManager
   * @param engineData
   * @param requestData
   * @param changes
   * @param output
   * @param validation
   * @return
   * @throws Exception
   */
  public boolean runUpdateChecksForAddress(EntityManager entityManager, AutomationEngineData engineData, RequestData requestData,
      RequestChangeContainer changes, AutomationResult<ValidationOutput> output, ValidationOutput validation) throws Exception {
    return false; // false denotes no country specific runs
  }

  /**
   * Allows duplicate cmrs for scenario
   *
   * @param scenarioList
   * @return
   */
  public void allowDuplicatesForScenario(AutomationEngineData engineData, RequestData requestData, List<String> scenarioList) {
    Admin admin = requestData.getAdmin();
    Data data = requestData.getData();

    String scenarioSubType = "";
    if ("C".equals(admin.getReqType()) && data != null) {
      scenarioSubType = data.getCustSubGrp();
    }
    if (!StringUtils.isBlank(scenarioSubType) && !scenarioList.isEmpty() && scenarioList.contains(scenarioSubType)) {
      engineData.addPositiveCheckStatus("allowDuplicates");
    }
    LOG.debug("Allowing Duplicate CMR for reqid = " + admin.getId().getReqId());
  }

  /**
   * Hooks to be able to manipulate the data to be sent to GBG finder services
   * 
   * @param entityManager
   * @param request
   * @param requestData
   */
  public void tweakGBGFinderRequest(EntityManager entityManager, GBGFinderRequest request, RequestData requestData) {
    // NOOP
  }

  /**
   * prepares and returns a dnb request based on requestData
   *
   * @param admin
   * @param data
   * @param addr
   * @return
   */
  public GBGFinderRequest createRequest(Admin admin, Data data, Addr addr, Boolean isOrgIdMatchOnly) {
    GBGFinderRequest request = new GBGFinderRequest();
    request.setMandt(SystemConfiguration.getValue("MANDT"));
    if (StringUtils.isNotBlank(data.getVat())) {
      request.setOrgId(data.getVat());
    }

    if (addr != null) {
      if (isOrgIdMatchOnly) {
        request.setLandedCountry(addr.getLandCntry());
      } else {
        request.setCity(addr.getCity1());
        request.setCustomerName(addr.getCustNm1() + (StringUtils.isBlank(addr.getCustNm2()) ? "" : " " + addr.getCustNm2()));
        request.setStreetLine1(addr.getAddrTxt());
        request.setStreetLine2(addr.getAddrTxt2());
        request.setLandedCountry(addr.getLandCntry());
        request.setPostalCode(addr.getPostCd());
        request.setStateProv(addr.getStateProv());
        // request.setMinConfidence("8");
      }
    }

    return request;
  }

  /**
   * Returns the DnB matches based on requestData & address
   *
   * @param requestData
   * @param engineData
   * @param addr
   * @return
   */
  public List<DnBMatchingResponse> getMatches(RequestData requestData, AutomationEngineData engineData, Addr addr, boolean isOrgIdMatchOnly)
      throws Exception {
    Admin admin = requestData.getAdmin();
    Data data = requestData.getData();
    List<DnBMatchingResponse> dnbMatches = new ArrayList<DnBMatchingResponse>();
    if (addr == null) {
      addr = requestData.getAddress("ZS01");
    }
    GBGFinderRequest request = createRequest(admin, data, addr, isOrgIdMatchOnly);
    MatchingServiceClient client = CmrServicesFactory.getInstance().createClient(SystemConfiguration.getValue("BATCH_SERVICES_URL"),
        MatchingServiceClient.class);
    client.setReadTimeout(1000 * 60 * 5);
    LOG.debug("Connecting to the Advanced D&B Matching Service at " + SystemConfiguration.getValue("BATCH_SERVICES_URL"));
    MatchingResponse<?> rawResponse = client.executeAndWrap(MatchingServiceClient.DNB_SERVICE_ID, request, MatchingResponse.class);
    ObjectMapper mapper = new ObjectMapper();
    String json = mapper.writeValueAsString(rawResponse);

    TypeReference<MatchingResponse<DnBMatchingResponse>> ref = new TypeReference<MatchingResponse<DnBMatchingResponse>>() {
    };

    MatchingResponse<DnBMatchingResponse> response = mapper.readValue(json, ref);
    if (response != null) {
      dnbMatches = response.getMatches();
    }

    return dnbMatches;
  }

  /**
   * Checks if the address updated closely matches D&B
   *
   * @param cntry
   * @param addr
   * @param matches
   * @return
   */
  public boolean ifaddressCloselyMatchesDnb(List<DnBMatchingResponse> matches, Addr addr, Admin admin, String cntry) {
    boolean result = false;
    for (DnBMatchingResponse dnbRecord : matches) {
      result = DnBUtil.closelyMatchesDnb(cntry, addr, admin, dnbRecord);
      if (result) {
        break;
      }
    }

    return result;
  }

  /**
   * Does the private person and IBM employee checks
   * 
   * @param entityManager
   * @param requestData
   * @param engineData
   * @param result
   * @param details
   * @param output
   * @return
   */
  protected boolean doPrivatePersonChecks(AutomationEngineData engineData, String country, String landCntry, String name, StringBuilder details,
      boolean checkBluepages) {

    if (hasLegalEndings(name)) {
      engineData.addRejectionComment("OTH", "Scenario chosen is incorrect, should be Commercial.", "", "");
      details.append("Scenario chosen is incorrect, should be Commercial.").append("\n");
      return false;
    }

    PrivatePersonCheckResult checkResult = checkPrivatePersonRecord(country, landCntry, name, checkBluepages);
    PrivatePersonCheckStatus checkStatus = checkResult.getStatus();

    switch (checkStatus) {
    case BluepagesError:
      engineData.addNegativeCheckStatus("BLUEPAGES_NOT_VALIDATED", "Not able to check the name against bluepages.");
      break;
    case DuplicateCMR:
      details.append("The name already matches a current record with CMR No. " + checkResult.getCmrNo()).append("\n");
      engineData.addRejectionComment("DUPC", "The name already has matches a current record with CMR No. " + checkResult.getCmrNo(),
          " Duplicate CMR No : " + checkResult.getCmrNo() + ". ", null);
      return false;
    case DuplicateCheckError:
      details.append("Duplicate CMR check using customer name match failed to execute.").append("\n");
      engineData.addNegativeCheckStatus("DUPLICATE_CHECK_ERROR", "Duplicate CMR check using customer name match failed to execute.");
      break;
    case NoIBMRecord:
      engineData.addRejectionComment("OTH", "Employee details not found in IBM BluePages.", "", "");
      details.append("Employee details not found in IBM BluePages.").append("\n");
      break;
    case Passed:
      details.append("No Duplicate CMRs were found.").append("\n");
      break;
    case PassedBoth:
      details.append("No Duplicate CMRs were found.").append("\n");
      details.append("Name validated against IBM BluePages successfully.").append("\n");
      break;
    }
    return true;
  }

  /**
   * Checks the private person record against current CMR records and optionally
   * against BluePages
   * 
   * @param soldTo
   * @param checkBluePages
   * @return
   */
  protected PrivatePersonCheckResult checkPrivatePersonRecord(String country, String landCntry, String name, boolean checkBluePages) {
    LOG.debug("Validating Private Person record for " + name);
    try {
      DuplicateCMRCheckResponse checkResponse = checkDuplicatePrivatePersonRecord(name, country, landCntry);
      String cmrNo = checkResponse.getCmrNo();
      // TODO find kunnr String kunnr = checkResponse.get
      if (!StringUtils.isBlank(cmrNo)) {
        LOG.debug("Duplicate CMR No. found: " + checkResponse.getCmrNo());
        return new PrivatePersonCheckResult(PrivatePersonCheckStatus.DuplicateCMR, cmrNo, null);
      }
    } catch (Exception e) {
      LOG.warn("Duplicate CMR check error.", e);
      return new PrivatePersonCheckResult(PrivatePersonCheckStatus.DuplicateCheckError);
    }
    if (checkBluePages) {
      LOG.debug("Checking against BluePages..");
      Person person = null;
      try {
        person = BluePagesHelper.getPersonByName(name);
        if (person == null) {
          LOG.debug("NO BluePages record found");
          return new PrivatePersonCheckResult(PrivatePersonCheckStatus.NoIBMRecord);
        } else {
          LOG.debug("BluePages record found: " + person.getName() + "/" + person.getEmail());
          return new PrivatePersonCheckResult(PrivatePersonCheckStatus.PassedBoth);
        }
      } catch (Exception e) {
        LOG.warn("BluePages check error.", e);
        return new PrivatePersonCheckResult(PrivatePersonCheckStatus.BluepagesError);
      }
    } else {
      LOG.debug("No duplicate CMR found.");
      return new PrivatePersonCheckResult(PrivatePersonCheckStatus.Passed);
    }

  }

  /**
   * Generic matching logic to check NAME against private person records
   * 
   * @param name
   * @param issuingCountry
   * @param landedCountry
   * @return CMR No. of the duplicate record
   */
  protected DuplicateCMRCheckResponse checkDuplicatePrivatePersonRecord(String name, String issuingCountry, String landedCountry) throws Exception {
    MatchingServiceClient client = CmrServicesFactory.getInstance().createClient(SystemConfiguration.getValue("BATCH_SERVICES_URL"),
        MatchingServiceClient.class);
    DuplicateCMRCheckRequest request = new DuplicateCMRCheckRequest();
    request.setCustomerName(name);
    request.setIssuingCountry(issuingCountry);
    request.setLandedCountry(landedCountry);
    request.setIsicCd(AutomationConst.ISIC_PRIVATE_PERSON);
    request.setNameMatch("Y");
    client.setReadTimeout(1000 * 60 * 5);
    LOG.debug("Connecting to the Duplicate CMR Check Service at " + SystemConfiguration.getValue("BATCH_SERVICES_URL"));
    MatchingResponse<?> rawResponse = client.executeAndWrap(MatchingServiceClient.CMR_SERVICE_ID, request, MatchingResponse.class);
    ObjectMapper mapper = new ObjectMapper();
    String json = mapper.writeValueAsString(rawResponse);

    TypeReference<MatchingResponse<DuplicateCMRCheckResponse>> ref = new TypeReference<MatchingResponse<DuplicateCMRCheckResponse>>() {
    };

    MatchingResponse<DuplicateCMRCheckResponse> response = mapper.readValue(json, ref);

    if (response.getSuccess()) {
      if (response.getMatched() && response.getMatches().size() > 0) {
        return response.getMatches().get(0);
      }
    }
    return null;

  }

  /**
   * Connects to PPS and checks the validity of the supplied CE ID
   * 
   * @param engineData
   * @param ceId
   * @param details
   * @return
   */
  protected boolean doBusinessPartnerChecks(AutomationEngineData engineData, String ceId, StringBuilder details) {
    if (StringUtils.isBlank(ceId)) {
      engineData.addRejectionComment("OTH", "PPS CEID is required for Business Partner scenarios.", "", "");
      details.append("PPS CEID is required for Business Partner scenarios.").append("\n");
      return false;
    }
    try {
      if (!checkPPSCEID(ceId)) {
        engineData.addRejectionComment("OTH", "CEID " + ceId + "  is not valid as checked against the PartnerWorld Profile System.", "", "");
        details.append("CEID " + ceId + " is not valid as checked against the PartnerWorld Profile System.").append("\n");
        return false;
      }
    } catch (Exception e) {
      LOG.error("Not able to validate PPS CE ID using PPS Service.", e);
      details.append("Not able to validate PPS CEID " + ceId + " against PPS. An issue occurred during the validation.").append("\n");
      engineData.addNegativeCheckStatus("PPSCEID",
          "Not able to validate PPS CEID " + ceId + " against PPS. An issue occurred during the validation.");
    }
    return true;
  }

  /**
   * Connects to the PPS Service and checks whether the PPS CEID is valid
   * 
   * @param ppsCeId
   * @return
   * @throws Exception
   */
  protected boolean checkPPSCEID(String ppsCeId) throws Exception {
    if (StringUtils.isBlank(ppsCeId)) {
      return false;
    }
    PPSServiceClient client = CmrServicesFactory.getInstance().createClient(SystemConfiguration.getValue("BATCH_SERVICES_URL"),
        PPSServiceClient.class);
    client.setRequestMethod(Method.Get);
    client.setReadTimeout(1000 * 60 * 5);
    PPSRequest request = new PPSRequest();
    request.setCeid(ppsCeId);
    PPSResponse ppsResponse = client.executeAndWrap(request, PPSResponse.class);
    if (!ppsResponse.isSuccess() || ppsResponse.getProfiles().size() == 0) {
      return false;
    } else {
      return true;
    }
  }

  /**
   * Generic method for ignoring Private Customer record updates
   * 
   * @param entityManager
   * @param admin
   * @param output
   * @param validation
   * @param engineData
   * @return
   */
  protected boolean handlePrivatePersonRecord(EntityManager entityManager, Admin admin, AutomationResult<ValidationOutput> output,
      ValidationOutput validation, AutomationEngineData engineData) {
    DataRdc rdc = getDataRdc(entityManager, admin);
    if ("9500".equals(rdc.getIsicCd())) {
      LOG.debug("Private customer record. Skipping validations.");
      validation.setSuccess(true);
      validation.setMessage("Skipped");
      engineData.addPositiveCheckStatus(AutomationEngineData.SKIP_VAT_CHECKS);
      output.setDetails("Update checks skipped for Private Customer record.");
      return true;
    } else {
      return false;
    }
  }

  /**
   * Checks if the given name has legal endings. This checks the globally
   * defined legal identifiers and has support for country-specific legal
   * endings
   * 
   * @param name
   * @return
   */
  protected boolean hasLegalEndings(String name) {
    if (name == null) {
      return false;
    }

    for (String gblEnding : GLOBAL_LEGAL_ENDINGS) {
      if (name.toUpperCase().contains(gblEnding)) {
        return true;
      }
    }
    List<String> extendedEndings = getCountryLegalEndings();
    if (extendedEndings != null) {
      for (String cntryEnding : extendedEndings) {
        if (name.toUpperCase().contains(cntryEnding)) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * 
   * Extended legal endings apart from the global values. Override in
   * country-specific utilities to extend the {@link #hasLegalEndings(String)}
   * method
   * 
   * @return
   */
  protected List<String> getCountryLegalEndings() {
    return Collections.emptyList();
  }

  /**
   * Gets the {@link DataRdc} record for the given request
   * 
   * @param entityManager
   * @param admin
   * @return
   */
  protected DataRdc getDataRdc(EntityManager entityManager, Admin admin) {
    DataPK rdcPk = new DataPK();
    rdcPk.setReqId(admin.getId().getReqId());
    DataRdc rdc = entityManager.find(DataRdc.class, rdcPk);
    return rdc;
  }

  /**
   * Container class, extended to hold extra info for the matched CMR No.
   * 
   * @author JeffZAMORA
   *
   */
  protected class PrivatePersonCheckResult {
    private PrivatePersonCheckStatus status;
    private String cmrNo;
    private String kunnr;

    public PrivatePersonCheckResult(PrivatePersonCheckStatus status) {
      this(status, null, null);
    }

    public PrivatePersonCheckResult(PrivatePersonCheckStatus status, String cmrNo, String kunnr) {
      this.status = status;
      this.cmrNo = cmrNo;
    }

    public PrivatePersonCheckStatus getStatus() {
      return status;
    }

    public void setStatus(PrivatePersonCheckStatus status) {
      this.status = status;
    }

    public String getCmrNo() {
      return cmrNo;
    }

    public void setCmrNo(String cmrNo) {
      this.cmrNo = cmrNo;
    }

    public String getKunnr() {
      return kunnr;
    }

    public void setKunnr(String kunnr) {
      this.kunnr = kunnr;
    }
  }

}