package com.ibm.cio.cmr.request.automation.impl.us;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.automation.AutomationElement;
import com.ibm.cio.cmr.request.automation.AutomationElementRegistry;
import com.ibm.cio.cmr.request.automation.AutomationEngineData;
import com.ibm.cio.cmr.request.automation.RequestData;
import com.ibm.cio.cmr.request.automation.impl.DuplicateCheckElement;
import com.ibm.cio.cmr.request.automation.impl.gbl.DupCMRCheckElement;
import com.ibm.cio.cmr.request.automation.impl.gbl.DupReqCheckElement;
import com.ibm.cio.cmr.request.automation.out.AutomationResult;
import com.ibm.cio.cmr.request.automation.out.MatchingOutput;
import com.ibm.cio.cmr.request.automation.util.AutomationUtil;
import com.ibm.cio.cmr.request.automation.util.ScenarioExceptionsUtil;
import com.ibm.cio.cmr.request.automation.util.geo.USUtil;
import com.ibm.cio.cmr.request.automation.util.geo.us.USDetailsContainer;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.AutomationMatching;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.util.SystemLocation;
import com.ibm.cmr.services.client.CmrServicesFactory;
import com.ibm.cmr.services.client.QueryClient;
import com.ibm.cmr.services.client.matching.MatchingResponse;
import com.ibm.cmr.services.client.matching.cmr.DuplicateCMRCheckResponse;
import com.ibm.cmr.services.client.matching.request.ReqCheckResponse;
import com.ibm.cmr.services.client.query.QueryRequest;
import com.ibm.cmr.services.client.query.QueryResponse;

/**
 * 
 * {@link AutomationElement} handling the US - Duplicate Checks
 * 
 * @author RoopakChugh
 *
 */
public class USDuplicateCheckElement extends DuplicateCheckElement {

  private static final Logger LOG = Logger.getLogger(USDuplicateCheckElement.class);
  private static DupReqCheckElement dupReqCheckElement = new DupReqCheckElement(null, null, false, false);
  private static DupCMRCheckElement dupCMRCheckElement = new DupCMRCheckElement(null, null, false, false);

  private static final List<String> negativeCheckScenarioList = Arrays.asList(USUtil.SC_IGS, USUtil.SC_IGSF, USUtil.SC_LEASE_NO_RESTRICT,
      USUtil.SC_LEASE_IPMA, USUtil.SC_LEASE_LPMA, USUtil.SC_FED_REGULAR, USUtil.SC_FED_INDIAN_TRIBE, USUtil.SC_FED_TRIBAL_BUS,
      USUtil.SC_FED_HEALTHCARE, USUtil.SC_FED_HOSPITAL, USUtil.SC_FED_CLINIC, USUtil.SC_FED_NATIVE_CORP, USUtil.SC_FED_CAMOUFLAGED,
      USUtil.SC_STATE_STATE, USUtil.SC_STATE_COUNTY, USUtil.SC_STATE_CITY, USUtil.SC_STATE_HOSPITALS, USUtil.SC_SCHOOL_PUBLIC,
      USUtil.SC_SCHOOL_CHARTER, USUtil.SC_STATE_DIST, USUtil.SC_SCHOOL_PRIV, USUtil.SC_BYMODEL);
  private static final List<String> skipScenariosForBYMODEL = Arrays.asList(USUtil.SC_INTERNAL, USUtil.SC_FED_CAMOUFLAGED, USUtil.SC_FED_CLINIC,
      USUtil.SC_FED_FEDSTATE, USUtil.SC_FED_HEALTHCARE, USUtil.SC_FED_HOSPITAL, USUtil.SC_FED_INDIAN_TRIBE, USUtil.SC_FED_NATIVE_CORP,
      USUtil.SC_FED_REGULAR, USUtil.SC_FED_TRIBAL_BUS, USUtil.SC_STATE_CITY, USUtil.SC_STATE_COUNTY, USUtil.SC_STATE_DIST, USUtil.SC_STATE_HOSPITALS,
      USUtil.SC_STATE_STATE);
  private static final List<String> END_USER_SCENARIOS = Arrays.asList(USUtil.SC_BP_END_USER, USUtil.SC_BP_DEVELOP, USUtil.SC_BP_E_HOST,
      USUtil.SC_FSP_END_USER);

  public USDuplicateCheckElement(String requestTypes, String actionOnError, boolean overrideData, boolean stopOnError) {
    super(requestTypes, actionOnError, overrideData, stopOnError);
  }

  @Override
  public AutomationResult<MatchingOutput> executeElement(EntityManager entityManager, RequestData requestData, AutomationEngineData engineData)
      throws Exception {
    LOG.debug("USDupcCheckElement");
    boolean dupReqFound = false;
    boolean reqChkSrvError = false;
    boolean dupCMRFound = false;
    boolean cmrChkSrvError = false;

    String matchType = "NA";
    Admin admin = requestData.getAdmin();
    String isProspectCmr = admin.getProspLegalInd();
    Data data = requestData.getData();
    String issuingCntry = data.getCmrIssuingCntry();
    ScenarioExceptionsUtil scenarioExceptions = getScenarioExceptions(entityManager, requestData, engineData);
    AutomationResult<MatchingOutput> result = buildResult(admin.getId().getReqId());
    MatchingOutput output = new MatchingOutput();
    StringBuilder details = new StringBuilder();
    Addr soldTo = requestData.getAddress("ZS01");
    if (soldTo != null && !scenarioExceptions.isSkipDuplicateChecks()) {
      List<String> duplicateList = new ArrayList<String>();
      List<String> soldToKunnrsList = new ArrayList<String>();

      List<DuplicateCMRCheckResponse> cmrCheckMatches = new ArrayList<DuplicateCMRCheckResponse>();
      List<ReqCheckResponse> reqCheckMatches = new ArrayList<ReqCheckResponse>();
      MatchingResponse<ReqCheckResponse> responseREQ = new MatchingResponse<>();
      MatchingResponse<DuplicateCMRCheckResponse> responseCMR = new MatchingResponse<>();

      if (USUtil.SC_BYMODEL.equals(data.getCustSubGrp())) {
        // SKIP duplicate checks for BYMODEL internal, federal except POA, and
        // STATE/LOCAL scenarios
        String subScenario = USUtil.determineCustSubScenario(entityManager, admin.getModelCmrNo(), engineData, requestData);
        if (subScenario != null && skipScenariosForBYMODEL.contains(subScenario)) {
          String scenarioDesc = USUtil.getScenarioDesc(entityManager, subScenario);
          details.append("Skipping Duplicate checks for Create By Model request"
              + (StringUtils.isNotBlank(scenarioDesc) ? " with '" + scenarioDesc + "' CMR imported." : ".")).append("\n");
          result.setDetails(details.toString());
          result.setResults("Skipped");
          result.setOnError(false);
          return result;
        }
      }

      int itemNo = 1;
      // perform duplicate request check
      // check if eligible for vat matching
      if (AutomationUtil.isCheckVatForDuplicates(data.getCmrIssuingCntry())) {
        matchType = "V";
      }
      // get duplicate req Matches
      responseREQ = getRequestMatches(entityManager, requestData, engineData);
      if (responseREQ != null && responseREQ.getSuccess()) {
        if (responseREQ.getMatched() && !responseREQ.getMatches().isEmpty()) {
          reqCheckMatches = responseREQ.getMatches();
          details.append(reqCheckMatches.size() + " record(s) found.");
          if (reqCheckMatches.size() > 5) {
            details.append("Showing top 5 matches only.");
            reqCheckMatches = reqCheckMatches.subList(0, 5);
          }

          for (ReqCheckResponse reqCheckRecord : reqCheckMatches) {
            details.append("\n");
            LOG.debug("Duplicate Requests Found, Req Id: " + reqCheckRecord.getReqId());
            duplicateList.add("" + reqCheckRecord.getReqId());
            output.addMatch(getProcessCode(), "REQ_ID", reqCheckRecord.getReqId() + "", matchType, reqCheckRecord.getMatchGrade() + "", "REQ",
                itemNo++);
            dupReqCheckElement.logDuplicateRequest(details, reqCheckRecord, matchType);
            if (!StringUtils.isBlank(reqCheckRecord.getUsRestrictTo())) {
              details.append("US Restrict To =  " + reqCheckRecord.getUsRestrictTo()).append("\n");
            } else {
              details.append("US Restrict To = -blank- ").append("\n");
            }
          }
          engineData.put("reqCheckMatches", reqCheckMatches);
          dupReqFound = true;
        }
      } else {
        reqChkSrvError = true;
      }

      if (!dupReqFound) {
        // perform duplicate cmr check if no duplicate requests found
        if (StringUtils.isNotBlank(admin.getDupCmrReason())) {
          // skip if only duplicate cmr and override reason is provided
          details.setLength(0);
          details.append("User requested to proceed with Duplicate CMR Creation.").append("\n");
          details.append("Reason provided - ").append("\n");
          details.append(admin.getDupCmrReason()).append("\n");
          result.setDetails(details.toString());
          result.setResults("Overridden");
          result.setOnError(false);
          return result;
        } else {
          responseCMR = getCMRMatches(entityManager, requestData, engineData);
          cmrCheckMatches = responseCMR.getMatches();
          if (responseCMR != null && responseCMR.getSuccess()) {
            result = DupCMRCheckElement.checkDupcProspectCmr(cmrCheckMatches, soldTo, isProspectCmr, engineData, result, issuingCntry);
            if (result.isOnError()) {
              return result;
            }
            if (responseCMR.getMatched() && !responseCMR.getMatches().isEmpty() && cmrCheckMatches.size() != 0) {
              details.append(cmrCheckMatches.size() + " record(s) found.");
              if (cmrCheckMatches.size() > 5) {
                cmrCheckMatches = cmrCheckMatches.subList(0, 5);
                details.append("Showing top 5 matches only.");
              }

              itemNo = 1;
              for (DuplicateCMRCheckResponse cmrCheckRecord : cmrCheckMatches) {
                details.append("\n");
                LOG.debug("Duplicate CMRs Found..");
                output.addMatch(getProcessCode(), "CMR_NO", cmrCheckRecord.getCmrNo(), "Matching Logic", cmrCheckRecord.getMatchGrade() + "", "CMR",
                    itemNo++);
                duplicateList.add(cmrCheckRecord.getCmrNo());
                soldToKunnrsList.add(getZS01Kunnr(cmrCheckRecord.getCmrNo(), SystemLocation.UNITED_STATES));

                DupCMRCheckElement.logDuplicateCMR(details, cmrCheckRecord);
                if (!StringUtils.isBlank(cmrCheckRecord.getUsRestrictTo())) {
                  details.append("US Restrict To =  " + cmrCheckRecord.getUsRestrictTo()).append("\n");
                } else {
                  details.append("US Restrict To = -blank- ").append("\n");
                }
                engineData.put("cmrCheckMatches", cmrCheckRecord);
              }
              dupCMRFound = true;
            }
          } else {
            cmrChkSrvError = true;
          }
        }
      }
      if (dupReqFound || dupCMRFound) {
        result.setResults("Duplicates found.");
        if (engineData.hasPositiveCheckStatus("allowDuplicates") || shouldSetNegativeCheck(entityManager, requestData, data.getCustSubGrp())) {
          engineData.addNegativeCheckStatus("dupAllowed",
              "There were possible duplicate CMRs/Requests found with the same data but allowed for the scenario.");
        } else {
          if (duplicateList.size() > 3) {
            duplicateList = duplicateList.subList(0, 3);
          }
          if (soldToKunnrsList.size() > 3) {
            soldToKunnrsList = soldToKunnrsList.subList(0, 3);
          }

          if (dupReqFound) {
            engineData.addRejectionComment("DUPR", "There were possible duplicate requests found with the same data.",
                StringUtils.join(duplicateList, ", "), "");
          } else if (dupCMRFound) {
            List<String> dupFiltered = removeDupEntriesFrmList(duplicateList);
            List<String> kunnrsFiltered = removeDupEntriesFrmList(soldToKunnrsList);
            String supplInfo1 = StringUtils.isNotBlank(dupFiltered.get(0)) ? dupFiltered.get(0) : "";
            String supplInfo2 = StringUtils.isNotBlank(kunnrsFiltered.get(0)) ? kunnrsFiltered.get(0) : "";
            engineData.addRejectionComment("DUPC", "There were possible duplicate CMRs found with the same data.", supplInfo1, supplInfo2);
            admin.setMatchIndc("C");
          }
          result.setOnError(true);
        }
        result.setProcessOutput(output);
        result.setDetails(details.toString().trim());
      } else if (!reqChkSrvError && !cmrChkSrvError) {
        result.setDetails("No Duplicate CMRs/Requests were found.");
        result.setResults("No Matches");
        result.setOnError(false);
      } else if (reqChkSrvError) {
        if (responseREQ != null && StringUtils.isNotBlank(responseREQ.getMessage())) {
          result.setDetails(responseREQ.getMessage());
          engineData.addRejectionComment("OTH", responseREQ.getMessage(), "", "");
          result.setOnError(true);
          result.setResults("Duplicate Request Check Encountered an error.");
        } else {
          result.setDetails("Duplicate Request Check Encountered an error.");
          engineData.addRejectionComment("OTH", "Duplicate Request Check Encountered an error.", "", "");
          result.setOnError(true);
          result.setResults("Duplicate Request Check Encountered an error.");
        }
      } else if (cmrChkSrvError) {
        if (responseCMR != null && StringUtils.isNotBlank(responseCMR.getMessage())) {
          result.setDetails(responseCMR.getMessage());
          engineData.addRejectionComment("OTH", responseCMR.getMessage(), "", "");
          result.setOnError(true);
          result.setResults("Duplicate CMR Check encountered an error.");
        } else {
          result.setDetails("Duplicate CMR Check Encountered an error.");
          engineData.addRejectionComment("OTH", "Duplicate CMR Check Encountered an error.", "", "");
          result.setOnError(true);
          result.setResults("Error on Duplicate CMR Check");
        }
      }
    } else if (scenarioExceptions.isSkipDuplicateChecks()) {
      result.setDetails("The request's scenario is configured to skip duplicate checks.");
      result.setResults("Skipped Duplicate Check");
      result.setOnError(false);
    } else if (soldTo == null) {
      result.setDetails("Missing main address on the request.");
      engineData.addRejectionComment("OTH", "Missing main address on the request.", "", "");
      result.setResults("No Matches");
      result.setOnError(true);
    } else {
      result.setDetails("Duplicate Check Encountered an error.");
      engineData.addRejectionComment("OTH", "Duplicate Check Encountered an error.", "", "");
      result.setOnError(true);
      result.setResults("Duplicate Check Encountered an error.");
    }
    return result;
  }

  private boolean shouldSetNegativeCheck(EntityManager entityManager, RequestData requestData, String custSubGrp) {
    if (USUtil.SC_BYMODEL.equals(custSubGrp)) {
      try {
        USDetailsContainer usDetails = USUtil.determineUSCMRDetails(entityManager, requestData.getAdmin().getModelCmrNo(), requestData);
        if (!USUtil.COMMERCIAL.equals(usDetails.getCustTypCd())) {
          return true;
        }
      } catch (Exception e) {
        LOG.error("Unable to determine CMR details for create by model scenario", e);
        return true;
      }
    } else if (negativeCheckScenarioList.contains(custSubGrp)
        || (requestData.getAdmin().getSourceSystId() != null && requestData.getAdmin().getSourceSystId().contains("CreateCMR"))) {
      return true;
    }
    return false;
  }

  /**
   * Filters duplicate requests based on scenario criteria
   * 
   * @param entityManager
   * @param requestData
   * @param engineData
   * @param response
   * @throws Exception
   */
  public void filterDuplicateRequests(EntityManager entityManager, RequestData requestData, AutomationEngineData engineData,
      MatchingResponse<ReqCheckResponse> response) throws Exception {
    List<ReqCheckResponse> reqCheckMatches = response.getMatches();
    List<ReqCheckResponse> reqCheckMatchesTmp = new ArrayList<ReqCheckResponse>();

    Admin admin = requestData.getAdmin();
    Data data = requestData.getData();
    String subIndCode = StringUtils.isBlank(data.getSubIndustryCd()) ? "" : data.getSubIndustryCd();
    String scenarioSubType = "";
    if ("C".equals(admin.getReqType()) && data != null) {
      scenarioSubType = StringUtils.isBlank(data.getCustSubGrp()) ? "" : data.getCustSubGrp();
    }

    if (StringUtils.isNotBlank(data.getRestrictTo()) && !USUtil.SC_BYMODEL.equals(scenarioSubType)) {
      for (ReqCheckResponse reqCheckRecord : reqCheckMatches) {
        if (data.getRestrictTo().equals(reqCheckRecord.getUsRestrictTo())) {
          reqCheckMatchesTmp.add(reqCheckRecord);
        }
      }
      reqCheckMatches = reqCheckMatchesTmp;
      reqCheckMatchesTmp = new ArrayList<ReqCheckResponse>();
    }

    switch (scenarioSubType) {
    case USUtil.SC_FED_POA:
      if (subIndCode.startsWith("Y")) {
        for (ReqCheckResponse reqCheckRecord : reqCheckMatches) {
          if (reqCheckRecord.getSubIndustryCd().startsWith("Y")) {
            reqCheckMatchesTmp.add(reqCheckRecord);
          }
        }
        response.setMatches(reqCheckMatchesTmp);
      }
      break;
    case USUtil.SC_IGSF:
      for (ReqCheckResponse reqCheckRecord : reqCheckMatches) {
        if ("11774312".equals(reqCheckRecord.getCompany())) {
          reqCheckMatchesTmp.add(reqCheckRecord);
        }
      }
      response.setMatches(reqCheckMatchesTmp);
      break;
    case USUtil.SC_LEASE_3CC:
      for (ReqCheckResponse reqCheckRecord : reqCheckMatches) {
        if ("ICC".equalsIgnoreCase(reqCheckRecord.getUsRestrictTo()) && "12003567".equals(reqCheckRecord.getCompany())) {
          reqCheckMatchesTmp.add(reqCheckRecord);
        }
      }
      response.setMatches(reqCheckMatchesTmp);
      break;
    case USUtil.SC_LEASE_IPMA:
    case USUtil.SC_LEASE_LPMA:
      for (ReqCheckResponse reqCheckRecord : reqCheckMatches) {
        if ("ICC".equalsIgnoreCase(reqCheckRecord.getUsRestrictTo()) && !"12003567".equals(reqCheckRecord.getCompany())) {
          reqCheckMatchesTmp.add(reqCheckRecord);
        }
      }
      response.setMatches(reqCheckMatchesTmp);
      break;
    case USUtil.SC_BYMODEL:
      USDetailsContainer usDetails = USUtil.determineUSCMRDetails(entityManager, requestData.getAdmin().getModelCmrNo(), requestData);
      switch (usDetails.getCustTypCd()) {
      case USUtil.POWER_OF_ATTORNEY:
      case USUtil.FEDERAL:
      case USUtil.STATE_LOCAL:
        response.setMatches(reqCheckMatches);
        break;
      case USUtil.LEASING:
        for (ReqCheckResponse reqCheckRecord : reqCheckMatches) {
          if (StringUtils.isNotBlank(reqCheckRecord.getUsRestrictTo()) && StringUtils.isNotBlank(usDetails.getUsRestrictTo())
              && reqCheckRecord.getUsRestrictTo().equalsIgnoreCase(usDetails.getUsRestrictTo())) {
            reqCheckMatchesTmp.add(reqCheckRecord);
          }
        }
        response.setMatches(reqCheckMatchesTmp);
        break;
      case USUtil.BUSINESS_PARTNER:
        for (ReqCheckResponse reqCheckRecord : reqCheckMatches) {
          if (StringUtils.isNotBlank(reqCheckRecord.getUsRestrictTo()) && StringUtils.isNotBlank(usDetails.getUsRestrictTo())
              && reqCheckRecord.getUsRestrictTo().equalsIgnoreCase(usDetails.getUsRestrictTo())) {
            String sql = ExternalizedQuery.getSql("AUTO.US.BP_ACCOUNT_TYPE");
            PreparedQuery query = new PreparedQuery(entityManager, sql);
            query.setParameter("REQ_ID", reqCheckRecord.getReqId());
            query.setForReadOnly(true);
            String bpAccType = query.getSingleResult(String.class);
            if (StringUtils.isNotBlank(bpAccType) && bpAccType.equals(data.getBpAcctTyp())) {
              reqCheckMatchesTmp.add(reqCheckRecord);
            }
          }
        }
        response.setMatches(reqCheckMatchesTmp);
        break;
      case USUtil.COMMERCIAL:
        for (ReqCheckResponse reqCheckRecord : reqCheckMatches) {
          if ((StringUtils.isNotBlank(reqCheckRecord.getUsRestrictTo()) && StringUtils.isNotBlank(usDetails.getUsRestrictTo())
              && reqCheckRecord.getUsRestrictTo().equalsIgnoreCase(usDetails.getUsRestrictTo()))
              || (StringUtils.isBlank(usDetails.getUsRestrictTo()) && StringUtils.isBlank(reqCheckRecord.getUsRestrictTo()))) {
            reqCheckMatchesTmp.add(reqCheckRecord);
          }
        }
        response.setMatches(reqCheckMatchesTmp);
        break;
      }
      break;
    default:
      for (ReqCheckResponse reqCheckRecord : reqCheckMatches) {
        if ((StringUtils.isBlank(data.getRestrictTo()) && StringUtils.isBlank(reqCheckRecord.getUsRestrictTo()))
            || (StringUtils.isNotBlank(data.getRestrictTo()) && StringUtils.isNotBlank(reqCheckRecord.getUsRestrictTo()))
                && data.getRestrictTo().equals(reqCheckRecord.getUsRestrictTo())) {
          reqCheckMatchesTmp.add(reqCheckRecord);
        }
      }
      response.setMatches(reqCheckMatchesTmp);
      break;
    }
  }

  /**
   * Filters for Duplicate CMR Records
   * 
   * @param entityManager
   * @param requestData
   * @param engineData
   * @param response
   * @throws Exception
   */
  public void filterDuplicateCmrRecords(EntityManager entityManager, RequestData requestData, AutomationEngineData engineData,
      MatchingResponse<DuplicateCMRCheckResponse> response) throws Exception {

    List<DuplicateCMRCheckResponse> cmrCheckMatches = response.getMatches();
    List<DuplicateCMRCheckResponse> cmrCheckMatchesTmp = new ArrayList<DuplicateCMRCheckResponse>();

    Admin admin = requestData.getAdmin();
    Data data = requestData.getData();
    String restrictTo = StringUtils.isNotBlank(data.getRestrictTo()) ? data.getRestrictTo() : "";
    String subIndCode = StringUtils.isBlank(data.getSubIndustryCd()) ? "" : data.getSubIndustryCd();
    String scenarioSubType = "";
    if ("C".equals(admin.getReqType()) && data != null) {
      scenarioSubType = StringUtils.isBlank(data.getCustSubGrp()) ? "" : data.getCustSubGrp();
    }
    switch (scenarioSubType) {
    case USUtil.SC_FED_POA:
      if (subIndCode.startsWith("Y")) {
        for (DuplicateCMRCheckResponse cmrCheckRecord : cmrCheckMatches) {
          if (cmrCheckRecord.getSubIndustryCd().startsWith("Y")) {
            cmrCheckMatchesTmp.add(cmrCheckRecord);
          }
        }
        response.setMatches(cmrCheckMatchesTmp);
      }
      break;
    case USUtil.SC_IGSF:
      for (DuplicateCMRCheckResponse cmrCheckRecord : cmrCheckMatches) {
        if ("11774312".equals(cmrCheckRecord.getCompany())) {
          cmrCheckMatchesTmp.add(cmrCheckRecord);
        }
      }
      response.setMatches(cmrCheckMatchesTmp);
      break;
    case USUtil.SC_LEASE_3CC:
      for (DuplicateCMRCheckResponse cmrCheckRecord : cmrCheckMatches) {
        if ("ICC".equalsIgnoreCase(cmrCheckRecord.getUsRestrictTo()) && "12003567".equals(cmrCheckRecord.getCompany())) {
          cmrCheckMatchesTmp.add(cmrCheckRecord);
        }
      }
      response.setMatches(cmrCheckMatchesTmp);
      break;
    case USUtil.SC_LEASE_IPMA:
    case USUtil.SC_LEASE_LPMA:
      for (DuplicateCMRCheckResponse cmrCheckRecord : cmrCheckMatches) {
        if ("ICC".equalsIgnoreCase(cmrCheckRecord.getUsRestrictTo()) && !"12003567".equals(cmrCheckRecord.getCompany())) {
          cmrCheckMatchesTmp.add(cmrCheckRecord);
        }
      }
      response.setMatches(cmrCheckMatchesTmp);
      break;
    case USUtil.SC_COMM_REGULAR:
      for (DuplicateCMRCheckResponse cmrCheckRecord : cmrCheckMatches) {
        String usRestrictTo = StringUtils.isNotBlank(cmrCheckRecord.getUsRestrictTo()) ? cmrCheckRecord.getUsRestrictTo() : "";
        String subIndustryCd = StringUtils.isNotBlank(cmrCheckRecord.getSubIndustryCd()) ? cmrCheckRecord.getSubIndustryCd() : "";
        if (restrictTo != null && subIndustryCd != null && restrictTo.equals(usRestrictTo) && !subIndustryCd.startsWith("Y")) {
          cmrCheckMatchesTmp.add(cmrCheckRecord);
        }
      }
      response.setMatches(cmrCheckMatchesTmp);
      break;
    case USUtil.SC_BYMODEL:
      USDetailsContainer usDetails = USUtil.determineUSCMRDetails(entityManager, requestData.getAdmin().getModelCmrNo(), requestData);
      switch (usDetails.getCustTypCd()) {
      case USUtil.POWER_OF_ATTORNEY:
      case USUtil.FEDERAL:
      case USUtil.STATE_LOCAL:
        for (DuplicateCMRCheckResponse cmrCheckRecord : cmrCheckMatches) {
          if (StringUtils.isNotBlank(cmrCheckRecord.getCompany()) && StringUtils.isNotBlank(usDetails.getCompanyNo())
              && cmrCheckRecord.getCompany().equalsIgnoreCase(usDetails.getCompanyNo())) {
            cmrCheckMatchesTmp.add(cmrCheckRecord);
          }
        }
        response.setMatches(cmrCheckMatchesTmp);
        break;
      case USUtil.LEASING:
      case USUtil.BUSINESS_PARTNER:
        for (DuplicateCMRCheckResponse cmrCheckRecord : cmrCheckMatches) {
          if (StringUtils.isNotBlank(cmrCheckRecord.getCompany()) && StringUtils.isNotBlank(usDetails.getCompanyNo())
              && cmrCheckRecord.getCompany().equalsIgnoreCase(usDetails.getCompanyNo()) && StringUtils.isNotBlank(cmrCheckRecord.getUsRestrictTo())
              && StringUtils.isNotBlank(usDetails.getUsRestrictTo())
              && cmrCheckRecord.getUsRestrictTo().equalsIgnoreCase(usDetails.getUsRestrictTo())) {
            cmrCheckMatchesTmp.add(cmrCheckRecord);
          }
        }
        response.setMatches(cmrCheckMatchesTmp);
        break;
      case USUtil.COMMERCIAL:
        for (DuplicateCMRCheckResponse cmrCheckRecord : cmrCheckMatches) {
          if (StringUtils.isNotBlank(cmrCheckRecord.getCompany()) && StringUtils.isNotBlank(usDetails.getCompanyNo())
              && cmrCheckRecord.getCompany().equalsIgnoreCase(usDetails.getCompanyNo())
              && ((StringUtils.isNotBlank(cmrCheckRecord.getUsRestrictTo()) && StringUtils.isNotBlank(usDetails.getUsRestrictTo())
                  && cmrCheckRecord.getUsRestrictTo().equalsIgnoreCase(usDetails.getUsRestrictTo()))
                  || (StringUtils.isBlank(usDetails.getUsRestrictTo()) && StringUtils.isBlank(cmrCheckRecord.getUsRestrictTo())))
              && !cmrCheckRecord.getSubIndustryCd().startsWith("Y")) {
            cmrCheckMatchesTmp.add(cmrCheckRecord);
          }
        }
        response.setMatches(cmrCheckMatchesTmp);
      }
    case USUtil.SC_BP_E_HOST:
      for (DuplicateCMRCheckResponse cmrCheckRecord : cmrCheckMatches) {
        if (StringUtils.isNotBlank(data.getRestrictTo()) && data.getRestrictTo().equals(cmrCheckRecord.getUsRestrictTo())
            && StringUtils.isNotBlank(cmrCheckRecord.getDept())
            && (cmrCheckRecord.getDept().toUpperCase().contains("E-HOST") || cmrCheckRecord.getDept().toUpperCase().contains("EHOST"))) {
          cmrCheckMatchesTmp.add(cmrCheckRecord);
        }
      }
      response.setMatches(cmrCheckMatchesTmp);
      break;
    default:
      for (DuplicateCMRCheckResponse cmrCheckRecord : cmrCheckMatches) {
        if ((StringUtils.isBlank(data.getRestrictTo()) && StringUtils.isBlank(cmrCheckRecord.getUsRestrictTo()))
            || ((StringUtils.isNotBlank(data.getRestrictTo()) && StringUtils.isNotBlank(cmrCheckRecord.getUsRestrictTo()))
                && data.getRestrictTo().equals(cmrCheckRecord.getUsRestrictTo()))) {
          cmrCheckMatchesTmp.add(cmrCheckRecord);
        }
      }
      response.setMatches(cmrCheckMatchesTmp);
      break;
    }
  }

  /**
   * Gets filtered duplicate requests
   * 
   * @param entityManager
   * @param requestData
   * @param engineData
   * @return
   * @throws Exception
   */
  public MatchingResponse<ReqCheckResponse> getRequestMatches(EntityManager entityManager, RequestData requestData, AutomationEngineData engineData)
      throws Exception {
    Admin admin = requestData.getAdmin();
    Data data = requestData.getData();
    MatchingResponse<ReqCheckResponse> response = new MatchingResponse<ReqCheckResponse>();
    Addr zs01 = requestData.getAddress("ZS01");
    Addr zi01 = requestData.getAddress("ZI01");
    ScenarioExceptionsUtil scenarioExceptions = getScenarioExceptions(entityManager, requestData, engineData);
    boolean requiresZI01Match = false;

    // check if End user and has divn value
    if (END_USER_SCENARIOS.contains(data.getCustSubGrp()) && "C".equals(admin.getReqType())) {
      if (zs01 != null && StringUtils.isBlank(zs01.getDivn())) {
        response.setSuccess(false);
        response.setMatched(false);
        response.setMessage("Division Line Empty for End User request.");
        return response;
      }
    }

    // check if ZI01 is to be matched and is it unique
    if (scenarioExceptions.getAddressTypesForDuplicateRequestCheck().contains("ZI01") && (zi01 == null || isAddressDuplicate(zs01, zi01))) {
      scenarioExceptions.getAddressTypesForDuplicateRequestCheck().remove("ZI01");
      requiresZI01Match = true;
    }

    response = getUSRequestMatches(entityManager, requestData, engineData);

    if (response != null && response.getSuccess() && response.getMatched() && requiresZI01Match) {
      List<ReqCheckResponse> dirtyMatches = new ArrayList<>();
      List<ReqCheckResponse> pureMatches = new ArrayList<>();
      for (ReqCheckResponse record : response.getMatches()) {
        String sql = ExternalizedQuery.getSql("AUTO.US.CHECK_ZI01_ON_REQ");
        PreparedQuery query = new PreparedQuery(entityManager, sql);
        query.setParameter("REQ_ID", record.getReqId());
        query.setForReadOnly(true);
        if (query.exists()) {
          // has ZI01 add to dirtyMatches
          dirtyMatches.add(record);
        } else {
          // has only zs01 add to pure matches
          pureMatches.add(record);
        }
      }
      if (!dirtyMatches.isEmpty()) {
        // if we found dirty matches we need to filter them again
        engineData.addPositiveCheckStatus("US_ZI01_REQ_MATCH");
        // adding this to scenario exceptions will check only ZS01-ZP01 matches
        response = getUSRequestMatches(entityManager, requestData, engineData);
        if (response != null && response.getSuccess() && response.getMatched()) {
          dirtyMatches = updateREQMatches(dirtyMatches, response.getMatches());
          if (dirtyMatches.size() > 0) {
            // if matches found for ZI01 we add them to pure matches
            pureMatches.addAll(dirtyMatches);
          }
        }
        if (!pureMatches.isEmpty()) {
          response.setSuccess(true);
          response.setMatched(true);
          response.setMatches(pureMatches);
        } else {
          response.setSuccess(true);
          response.setMatched(false);
          response.setMessage("No matches found for the given search criteria.");
        }
      }

    }

    // filter dup requests
    if (response.getSuccess() && response.getMatched() && !response.getMatches().isEmpty()) {
      filterDuplicateRequests(entityManager, requestData, engineData, response);
    }

    // reverify
    if (response.getMatches().size() == 0) {
      response.setSuccess(true);
      response.setMatched(false);
      response.setMessage("No matches found for the given search criteria.");
    }

    return response;
  }

  private MatchingResponse<ReqCheckResponse> getUSRequestMatches(EntityManager entityManager, RequestData requestData,
      AutomationEngineData engineData) throws Exception {
    MatchingResponse<ReqCheckResponse> response = new MatchingResponse<ReqCheckResponse>();
    MatchingResponse<ReqCheckResponse> byModelResponse = new MatchingResponse<>();
    Data data = requestData.getData();
    // get duplicates
    if (!USUtil.SC_BYMODEL.equals(data.getCustSubGrp())) {
      engineData.put(AutomationEngineData.REQ_MATCH_SCENARIO, data.getCustSubGrp());
      response = dupReqCheckElement.getMatches(entityManager, requestData, engineData);
      engineData.put(AutomationEngineData.REQ_MATCH_SCENARIO, USUtil.SC_BYMODEL);
      byModelResponse = dupReqCheckElement.getMatches(entityManager, requestData, engineData);
    } else {
      engineData.put(AutomationEngineData.REQ_MATCH_SCENARIO, "");
      response = dupReqCheckElement.getMatches(entityManager, requestData, engineData);
    }

    if ((response == null || !response.getSuccess() || !response.getMatched())
        && (byModelResponse.getSuccess() && byModelResponse.getMatched() && !byModelResponse.getMatches().isEmpty())) {
      response = byModelResponse;
    } else if ((response.getSuccess() && response.getMatched() && !response.getMatches().isEmpty())
        && (byModelResponse.getSuccess() && byModelResponse.getMatched() && !byModelResponse.getMatches().isEmpty())) {
      for (ReqCheckResponse record1 : byModelResponse.getMatches()) {
        boolean found = false;
        for (ReqCheckResponse record2 : response.getMatches()) {
          if (record2.getReqId() == record1.getReqId()) {
            found = true;
            break;
          }
        }
        if (!found) {
          response.addMatch(record1);
        }
      }
    }
    return response;
  }

  /**
   * Get filtered CMR Matches
   * 
   * @param entityManager
   * @param requestData
   * @param engineData
   * @return
   * @throws Exception
   */
  public MatchingResponse<DuplicateCMRCheckResponse> getCMRMatches(EntityManager entityManager, RequestData requestData,
      AutomationEngineData engineData) throws Exception {
    Admin admin = requestData.getAdmin();
    Data data = requestData.getData();
    Addr zs01 = requestData.getAddress("ZS01");
    Addr zi01 = requestData.getAddress("ZI01");
    MatchingResponse<DuplicateCMRCheckResponse> response = new MatchingResponse<DuplicateCMRCheckResponse>();
    ScenarioExceptionsUtil scenarioExceptions = getScenarioExceptions(entityManager, requestData, engineData);
    String subScenario = data.getCustSubGrp();
    if (USUtil.SC_BYMODEL.equals(subScenario)) {
      subScenario = USUtil.determineCustSubScenario(entityManager, admin.getModelCmrNo(), engineData, requestData);
    }

    // check if End user
    if (END_USER_SCENARIOS.contains(subScenario) && "C".equals(admin.getReqType())) {

      ScenarioExceptionsUtil bpScenarioExceptions = new ScenarioExceptionsUtil(entityManager, data.getCmrIssuingCntry(), data.getCountryUse(),
          USUtil.CG_THIRD_P_BUSINESS_PARTNER, subScenario);
      engineData.addPositiveCheckStatus("BP_EU_REQ");
      if (bpScenarioExceptions != null) {
        // set cmr check mappings to bp
        scenarioExceptions.setAddressTypesForDuplicateCMRCheck(bpScenarioExceptions.getAddressTypesForDuplicateCMRCheck());
      }

      if (zs01 != null && StringUtils.isBlank(zs01.getDivn())) {
        response.setSuccess(false);
        response.setMatched(false);
        response.setMessage("Division Line Empty for End User request.");
        return response;
      }
    }

    // get duplicates
    response = dupCMRCheckElement.getMatches(entityManager, requestData, engineData);
    LOG.debug("Response match - " + response.getMatched());
    LOG.debug("Response success - " + response.getSuccess());

    if (response != null && response.getSuccess() && response.getMatched() && zi01 == null) {
      List<DuplicateCMRCheckResponse> dirtyMatches = new ArrayList<DuplicateCMRCheckResponse>();
      List<DuplicateCMRCheckResponse> pureMatches = new ArrayList<DuplicateCMRCheckResponse>();
      // this means that we only received ZS01 matches.
      // check if matches have ZP01 addresses, if not consider pure match and
      // include always
      // if matches have ZP01 address, then remove mapping of ZS01-ZS01 and add
      // ZS01-ZP01 in scenario exceptions to get invoice to address duplicates
      // only
      for (DuplicateCMRCheckResponse record : response.getMatches()) {
        String sql = ExternalizedQuery.getSql("AUTO.US.CHECK_ZP01_ADDRESS");
        PreparedQuery query = new PreparedQuery(entityManager, sql);
        query.setParameter("CMR_NO", record.getCmrNo());
        query.setForReadOnly(true);
        if (query.exists()) {
          // has ZP01 add to dirtyMatches
          dirtyMatches.add(record);
        } else {
          // has only zs01 add to pure matches
          pureMatches.add(record);
        }
      }
      if (!dirtyMatches.isEmpty()) {
        // if we found dirty matches we need to filter them again
        scenarioExceptions.getAddressTypesForDuplicateCMRCheck().clear();
        scenarioExceptions.getAddressTypesForDuplicateCMRCheck().put("ZS01", Arrays.asList("ZP01"));
        // adding this to scenario exceptions will check only ZS01-ZP01 matches
        response = dupCMRCheckElement.getMatches(entityManager, requestData, engineData);
        if (response != null && response.getSuccess() && response.getMatched()) {
          dirtyMatches = updateCMRMatches(dirtyMatches, response.getMatches());
          if (dirtyMatches.size() > 0) {
            // if matches found for ZP01 we add them to pure matches
            pureMatches.addAll(dirtyMatches);
          }
        } else {
          LOG.debug("No matches found for record found in dirty matches ");
        }
        if (!pureMatches.isEmpty()) {
          response.setSuccess(true);
          response.setMatched(true);
          response.setMatches(pureMatches);
          LOG.debug("Pure matches found for the given search criteria");
        } else {
          response.setSuccess(true);
          response.setMatched(false);
          response.setMessage("No matches found for the given search criteria.");
          LOG.debug("No matches found for the given search criteria.");
        }
      }

    }

    // filter dup requests
    if (response.getSuccess() && response.getMatched() && !response.getMatches().isEmpty()) {
      filterDuplicateCmrRecords(entityManager, requestData, engineData, response);
    }

    // reverify
    if (response.getMatches().size() == 0) {
      response.setSuccess(true);
      response.setMatched(false);
      response.setMessage("No matches found for the given search criteria.");
    }

    return response;
  }

  @Override
  public boolean importMatch(EntityManager entityManager, RequestData requestData, AutomationMatching match) {
    return false;
  }

  @Override
  public String getProcessCode() {
    return AutomationElementRegistry.US_DUP_CHK;
  }

  @Override
  public String getProcessDesc() {
    return "US Duplicate Check";
  }

  @Override
  public boolean isNonImportable() {
    return true;
  }

  public String getZS01Kunnr(String cmrNo, String cntry) throws Exception {
    String kunnr = "";

    String url = SystemConfiguration.getValue("CMR_SERVICES_URL");
    String mandt = SystemConfiguration.getValue("MANDT");
    String sql = ExternalizedQuery.getSql("GET.ZS01.KUNNR");
    sql = StringUtils.replace(sql, ":MANDT", "'" + mandt + "'");
    sql = StringUtils.replace(sql, ":ZZKV_CUSNO", "'" + cmrNo + "'");
    sql = StringUtils.replace(sql, ":KATR6", "'" + cntry + "'");

    String dbId = QueryClient.RDC_APP_ID;

    QueryRequest query = new QueryRequest();
    query.setSql(sql);
    query.addField("KUNNR");
    query.addField("ZZKV_CUSNO");

    QueryClient client = CmrServicesFactory.getInstance().createClient(url, QueryClient.class);
    QueryResponse response = client.executeAndWrap(dbId, query, QueryResponse.class);

    if (response.isSuccess() && response.getRecords() != null && response.getRecords().size() != 0) {
      List<Map<String, Object>> records = response.getRecords();
      Map<String, Object> record = records.get(0);
      kunnr = record.get("KUNNR") != null ? record.get("KUNNR").toString() : "";
    }
    return kunnr;
  }

  private List<DuplicateCMRCheckResponse> updateCMRMatches(List<DuplicateCMRCheckResponse> global, List<DuplicateCMRCheckResponse> iteration) {
    List<DuplicateCMRCheckResponse> updated = new ArrayList<DuplicateCMRCheckResponse>();
    for (DuplicateCMRCheckResponse resp1 : global) {
      for (DuplicateCMRCheckResponse resp2 : iteration) {
        if (resp1.getCmrNo().equals(resp2.getCmrNo())) {
          updated.add(resp1);
          break;
        }
      }
    }
    return updated;
  }

  private List<ReqCheckResponse> updateREQMatches(List<ReqCheckResponse> global, List<ReqCheckResponse> iteration) {
    List<ReqCheckResponse> updated = new ArrayList<>();
    for (ReqCheckResponse resp1 : global) {
      for (ReqCheckResponse resp2 : iteration) {
        if (resp1.getReqId() == resp2.getReqId()) {
          updated.add(resp1);
          break;
        }
      }
    }
    return updated;

  }

  public List<String> removeDupEntriesFrmList(List<String> duplList) {
    Set<String> s = new LinkedHashSet<String>();
    s.addAll(duplList);
    duplList.clear();
    duplList.addAll(s);
    return duplList;
  }

  private boolean isAddressDuplicate(Addr first, Addr second) {
    String firstStreetAddress1 = (StringUtils.isNotBlank(first.getAddrTxt()) ? first.getAddrTxt() : "").trim().toUpperCase();
    String firstStreetAddress2 = (StringUtils.isNotBlank(first.getAddrTxt2()) ? first.getAddrTxt2() : "").trim().toUpperCase();
    String firstStreetAddress = firstStreetAddress1;
    if (StringUtils.isBlank(firstStreetAddress)) {
      firstStreetAddress = firstStreetAddress2;
    } else if (StringUtils.isNotBlank(firstStreetAddress2)) {
      firstStreetAddress += " " + firstStreetAddress2;
    }
    firstStreetAddress = StringUtils.isNotBlank(firstStreetAddress) ? firstStreetAddress : "";
    String firstCity = (StringUtils.isNotBlank(first.getCity1()) ? first.getCity1() : "").trim().toUpperCase();
    String firstPostalCd = (StringUtils.isNotBlank(first.getPostCd()) ? first.getPostCd() : "").trim();

    String secondStreetAddress1 = (StringUtils.isNotBlank(second.getAddrTxt()) ? second.getAddrTxt() : "").trim().toUpperCase();
    String secondStreetAddress2 = (StringUtils.isNotBlank(second.getAddrTxt2()) ? second.getAddrTxt2() : "").trim().toUpperCase();
    String secondStreetAddress = secondStreetAddress1;
    if (StringUtils.isBlank(secondStreetAddress)) {
      secondStreetAddress = secondStreetAddress2;
    } else if (StringUtils.isNotBlank(secondStreetAddress2)) {
      secondStreetAddress += " " + secondStreetAddress2;
    }
    secondStreetAddress = StringUtils.isNotBlank(secondStreetAddress) ? secondStreetAddress : "";
    String secondCity = (StringUtils.isNotBlank(second.getCity1()) ? second.getCity1() : "").trim().toUpperCase();
    String secondPostalCd = (StringUtils.isNotBlank(second.getPostCd()) ? second.getPostCd() : "").trim();

    if (secondStreetAddress.equals(firstStreetAddress) && secondCity.equals(firstCity) && secondPostalCd.equals(firstPostalCd)) {
      return true;
    }
    return false;

  }
}
