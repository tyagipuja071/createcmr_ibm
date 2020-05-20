package com.ibm.cio.cmr.request.automation.impl.us;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;
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
      USUtil.SC_LEASE_32C, USUtil.SC_LEASE_IPMA, USUtil.SC_LEASE_LPMA, USUtil.SC_FED_INDIAN_TRIBE, USUtil.SC_FED_TRIBAL_BUS, USUtil.SC_FED_HEALTHCARE,
      USUtil.SC_FED_HOSPITAL, USUtil.SC_FED_CLINIC, USUtil.SC_FED_NATIVE_CORP, USUtil.SC_FED_CAMOUFLAGED, USUtil.SC_STATE_STATE,
      USUtil.SC_STATE_COUNTY, USUtil.SC_STATE_CITY, USUtil.SC_STATE_HOSPITALS, USUtil.SC_SCHOOL_PUBLIC, USUtil.SC_SCHOOL_CHARTER,
      USUtil.SC_STATE_DIST, USUtil.SC_SCHOOL_PRIV, USUtil.SC_BYMODEL);

  public USDuplicateCheckElement(String requestTypes, String actionOnError, boolean overrideData, boolean stopOnError) {
    super(requestTypes, actionOnError, overrideData, stopOnError);
  }

  @Override
  public AutomationResult<MatchingOutput> executeElement(EntityManager entityManager, RequestData requestData, AutomationEngineData engineData)
      throws Exception {
    boolean dupReqFound = false;
    boolean reqChkSrvError = false;
    boolean dupCMRFound = false;
    boolean cmrChkSrvError = false;

    String matchType = "NA";
    Admin admin = requestData.getAdmin();
    Data data = requestData.getData();
    ScenarioExceptionsUtil scenarioExceptions = getScenarioExceptions(entityManager, requestData, engineData);
    AutomationResult<MatchingOutput> result = buildResult(admin.getId().getReqId());
    MatchingOutput output = new MatchingOutput();
    StringBuilder details = new StringBuilder();
    Addr soldTo = requestData.getAddress("ZS01");
    Addr invoiceTo = requestData.getAddress("ZI01");
    if (soldTo != null && !scenarioExceptions.isSkipDuplicateChecks()) {
      List<String> duplicateList = new ArrayList<String>();
      List<String> soldToKunnrsList = new ArrayList<String>();

      List<DuplicateCMRCheckResponse> cmrCheckMatches = new ArrayList<DuplicateCMRCheckResponse>();
      List<ReqCheckResponse> reqCheckMatches = new ArrayList<ReqCheckResponse>();
      MatchingResponse<ReqCheckResponse> responseREQ = new MatchingResponse<>();
      MatchingResponse<DuplicateCMRCheckResponse> responseCMR = new MatchingResponse<>();

      // if (scenarioExceptions != null && invoiceTo==null) {
      // scenarioExceptions.getAddressTypesForDuplicateCMRCheck().get("ZS01").add("ZP01");
      // }

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
          if (responseCMR != null && responseCMR.getSuccess()) {
            if (responseCMR.getMatched() && !responseCMR.getMatches().isEmpty()) {
              cmrCheckMatches = responseCMR.getMatches();
              details.append(cmrCheckMatches.size() + " record(s) found.");
              if (cmrCheckMatches.size() > 5) {
                cmrCheckMatches = cmrCheckMatches.subList(0, 5);
                details.append("Showing top 5 matches only.");
              }

              // int itemNo = 1;
              for (DuplicateCMRCheckResponse cmrCheckRecord : cmrCheckMatches) {
                details.append("\n");
                LOG.debug("Duplicate CMRs Found..");
                output.addMatch(getProcessCode(), "CMR_NO", cmrCheckRecord.getCmrNo(), "Matching Logic", cmrCheckRecord.getMatchGrade() + "", "CMR",
                    itemNo++);
                duplicateList.add(cmrCheckRecord.getCmrNo());
                soldToKunnrsList.add(getZS01Kunnr(cmrCheckRecord.getCmrNo()));

                dupCMRCheckElement.logDuplicateCMR(details, cmrCheckRecord);
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
                "Duplicate Requests : " + StringUtils.join(duplicateList, ", "), "");
          } else if (dupCMRFound) {
            engineData.addRejectionComment("DUPC", "There were possible duplicate CMRs found with the same data.",
                "Duplicate CMRs : " + StringUtils.join(duplicateList, ", "), "SOLD TO KUNNR : " + StringUtils.join(soldToKunnrsList, ", "));
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
      engineData.addRejectionComment("ADDR", "Missing main address on the request.", "", "");
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
        USDetailsContainer usDetails = USUtil.determineUSCMRDetails(entityManager, requestData.getAdmin().getModelCmrNo());
        if (!USUtil.COMMERCIAL.equals(usDetails.getCustTypCd())) {
          return true;
        }
      } catch (Exception e) {
        LOG.error("Unable to determine CMR details for create by model scenario", e);
        return true;
      }
    } else if (negativeCheckScenarioList.contains(custSubGrp)) {
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
      USDetailsContainer usDetails = USUtil.determineUSCMRDetails(entityManager, requestData.getAdmin().getModelCmrNo());
      switch (usDetails.getCustTypCd()) {
      case USUtil.POWER_OF_ATTORNEY:
      case USUtil.FEDERAL:
      case USUtil.STATE_LOCAL:
        for (ReqCheckResponse reqCheckRecord : reqCheckMatches) {
          if (StringUtils.isNotBlank(reqCheckRecord.getCompany()) && StringUtils.isNotBlank(usDetails.getCompanyNo())
              && reqCheckRecord.getCompany().equalsIgnoreCase(usDetails.getCompanyNo())) {
            reqCheckMatchesTmp.add(reqCheckRecord);
          }
        }
        response.setMatches(reqCheckMatchesTmp);
        break;
      case USUtil.LEASING:
      case USUtil.BUSINESS_PARTNER:
        for (ReqCheckResponse reqCheckRecord : reqCheckMatches) {
          if (StringUtils.isNotBlank(reqCheckRecord.getCompany()) && StringUtils.isNotBlank(usDetails.getCompanyNo())
              && reqCheckRecord.getCompany().equalsIgnoreCase(usDetails.getCompanyNo()) && StringUtils.isNotBlank(reqCheckRecord.getUsRestrictTo())
              && StringUtils.isNotBlank(usDetails.getUsRestrictTo())
              && reqCheckRecord.getUsRestrictTo().equalsIgnoreCase(usDetails.getUsRestrictTo())) {
            reqCheckMatchesTmp.add(reqCheckRecord);
          }
        }
        response.setMatches(reqCheckMatchesTmp);
        break;
      case USUtil.COMMERCIAL:
        for (ReqCheckResponse reqCheckRecord : reqCheckMatches) {
          if (StringUtils.isNotBlank(reqCheckRecord.getCompany()) && StringUtils.isNotBlank(usDetails.getCompanyNo())
              && reqCheckRecord.getCompany().equalsIgnoreCase(usDetails.getCompanyNo())
              && ((StringUtils.isNotBlank(reqCheckRecord.getUsRestrictTo()) && StringUtils.isNotBlank(usDetails.getUsRestrictTo())
                  && reqCheckRecord.getUsRestrictTo().equalsIgnoreCase(usDetails.getUsRestrictTo()))
                  || (StringUtils.isBlank(usDetails.getUsRestrictTo()) && StringUtils.isBlank(reqCheckRecord.getUsRestrictTo())))) {
            reqCheckMatchesTmp.add(reqCheckRecord);
          }
        }
        response.setMatches(reqCheckMatchesTmp);
      }
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
    case USUtil.SC_BYMODEL:
      USDetailsContainer usDetails = USUtil.determineUSCMRDetails(entityManager, requestData.getAdmin().getModelCmrNo());
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
                  || (StringUtils.isBlank(usDetails.getUsRestrictTo()) && StringUtils.isBlank(cmrCheckRecord.getUsRestrictTo())))) {
            cmrCheckMatchesTmp.add(cmrCheckRecord);
          }
        }
        response.setMatches(cmrCheckMatchesTmp);
      }
    default:
      for (DuplicateCMRCheckResponse cmrCheckRecord : cmrCheckMatches) {
        if ((StringUtils.isBlank(data.getRestrictTo()) && StringUtils.isBlank(cmrCheckRecord.getUsRestrictTo()))
            || (StringUtils.isNotBlank(data.getRestrictTo()) && StringUtils.isNotBlank(cmrCheckRecord.getUsRestrictTo()))
                && data.getRestrictTo().equals(cmrCheckRecord.getUsRestrictTo())) {
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

    // check if End user and has divn value
    if (USUtil.SC_BP_END_USER.equals(data.getCustSubGrp()) && "C".equals(admin.getReqType())) {
      Addr zs01 = requestData.getAddress("ZS01");
      if (zs01 != null && StringUtils.isBlank(zs01.getDivn())) {
        response.setSuccess(false);
        response.setMatched(false);
        response.setMessage("Division Line Empty for End User request.");
        return response;
      }
    }

    // get duplicates
    response = dupReqCheckElement.getMatches(entityManager, requestData, engineData);

    // filter dup requests
    if (response.getSuccess() && response.getMatched() && !response.getMatches().isEmpty()) {
      filterDuplicateRequests(entityManager, requestData, engineData, response);
    }

    // reverify
    if (response.getMatches().size() == 0) {
      response.setMatched(false);
      response.setMessage("No matches found for the given search criteria.");
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
  private MatchingResponse<DuplicateCMRCheckResponse> getCMRMatches(EntityManager entityManager, RequestData requestData,
      AutomationEngineData engineData) throws Exception {
    Admin admin = requestData.getAdmin();
    Data data = requestData.getData();
    MatchingResponse<DuplicateCMRCheckResponse> response = new MatchingResponse<DuplicateCMRCheckResponse>();

    // check if End user and has divn value
    if (USUtil.SC_BP_END_USER.equals(data.getCustSubGrp()) && "C".equals(admin.getReqType())) {
      Addr zs01 = requestData.getAddress("ZS01");
      if (zs01 != null && StringUtils.isBlank(zs01.getDivn())) {
        response.setSuccess(false);
        response.setMatched(false);
        response.setMessage("Division Line Empty for End User request.");
        return response;
      }
    }

    // get duplicates
    response = dupCMRCheckElement.getMatches(entityManager, requestData, engineData);

    // filter dup requests
    if (response.getSuccess() && response.getMatched() && !response.getMatches().isEmpty()) {
      filterDuplicateCmrRecords(entityManager, requestData, engineData, response);
    }

    // reverify
    if (response.getMatches().size() == 0) {
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

  private String getZS01Kunnr(String cmrNo) throws Exception {
    String kunnr = "";

    String url = SystemConfiguration.getValue("CMR_SERVICES_URL");
    String mandt = SystemConfiguration.getValue("MANDT");
    String sql = ExternalizedQuery.getSql("GET.ZS01.KUNNR");
    sql = StringUtils.replace(sql, ":MANDT", "'" + mandt + "'");
    sql = StringUtils.replace(sql, ":ZZKV_CUSNO", "'" + cmrNo + "'");

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

}
