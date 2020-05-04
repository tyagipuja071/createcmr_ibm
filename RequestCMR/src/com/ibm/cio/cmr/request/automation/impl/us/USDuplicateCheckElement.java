package com.ibm.cio.cmr.request.automation.impl.us;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import com.ibm.cio.cmr.request.automation.AutomationElementRegistry;
import com.ibm.cio.cmr.request.automation.AutomationEngineData;
import com.ibm.cio.cmr.request.automation.RequestData;
import com.ibm.cio.cmr.request.automation.impl.DuplicateCheckElement;
import com.ibm.cio.cmr.request.automation.out.AutomationResult;
import com.ibm.cio.cmr.request.automation.out.MatchingOutput;
import com.ibm.cio.cmr.request.automation.util.AutomationUtil;
import com.ibm.cio.cmr.request.automation.util.DuplicateChecksUtil;
import com.ibm.cio.cmr.request.automation.util.ScenarioExceptionsUtil;
import com.ibm.cio.cmr.request.automation.util.geo.USUtil;
import com.ibm.cio.cmr.request.automation.util.geo.us.USDetailsContainer;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.AutomationMatching;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.util.SystemLocation;
import com.ibm.cmr.services.client.CmrServicesFactory;
import com.ibm.cmr.services.client.MatchingServiceClient;
import com.ibm.cmr.services.client.matching.MatchingResponse;
import com.ibm.cmr.services.client.matching.cmr.DuplicateCMRCheckRequest;
import com.ibm.cmr.services.client.matching.cmr.DuplicateCMRCheckResponse;
import com.ibm.cmr.services.client.matching.request.ReqCheckRequest;
import com.ibm.cmr.services.client.matching.request.ReqCheckResponse;

/**
 * 
 * @author RangoliSaxena
 *
 */
public class USDuplicateCheckElement extends DuplicateCheckElement {

  private static final Logger log = Logger.getLogger(USDuplicateCheckElement.class);

  private static boolean reqChkSrvError = false;
  private static boolean cmrChkSrvError = false;
  private static boolean reqNegStatFlag = false;
  private static boolean dupReqFound = false;
  private static boolean dupCMRFound = false;

  public USDuplicateCheckElement(String requestTypes, String actionOnError, boolean overrideData, boolean stopOnError) {
    super(requestTypes, actionOnError, overrideData, stopOnError);
  }

  @Override
  public boolean importMatch(EntityManager entityManager, RequestData requestData, AutomationMatching match) {
    return false;
  }

  @Override
  public AutomationResult<MatchingOutput> executeElement(EntityManager entityManager, RequestData requestData, AutomationEngineData engineData)
      throws Exception {
    String matchType = "NA";
    Admin admin = requestData.getAdmin();
    Data data = requestData.getData();
    ScenarioExceptionsUtil scenarioExceptions = getScenarioExceptions(entityManager, requestData, engineData);
    AutomationResult<MatchingOutput> result = buildResult(admin.getId().getReqId());
    MatchingOutput output = new MatchingOutput();
    StringBuilder details = new StringBuilder();
    boolean matchDepartment = false;
    if (engineData.get(AutomationEngineData.MATCH_DEPARTMENT) != null) {
      matchDepartment = (boolean) engineData.get(AutomationEngineData.MATCH_DEPARTMENT);
    }

    Addr soldTo = requestData.getAddress("ZS01");
    if (soldTo != null && !scenarioExceptions.isSkipDuplicateChecks()) {
      int itemNo = 1;
      // perform duplicate request check
      // check if eligible for vat matching
      if (AutomationUtil.isCheckVatForDuplicates(data.getCmrIssuingCntry())) {
        matchType = "V";
      }

      List<ReqCheckResponse> reqCheckMatches = new ArrayList<ReqCheckResponse>();
      // get duplicate req Matches
      MatchingResponse<ReqCheckResponse> response = getReqMatches(entityManager, requestData, engineData);
      if (response != null) {
        if (response.getSuccess()) {
          if (response.getMatched()) {
            reqCheckMatches = response.getMatches();
            reqCheckMatches = filterDupReqs(entityManager, requestData, engineData, reqCheckMatches);
            if (reqCheckMatches.size() != 0) {
              details.append(reqCheckMatches.size() + " record(s) found.");
              if (reqCheckMatches.size() > 5) {
                details.append("Showing top 5 matches only.");
                reqCheckMatches = reqCheckMatches.subList(0, 5);
              }

              for (ReqCheckResponse reqCheckRecord : reqCheckMatches) {
                details.append("\n");
                log.debug("Duplicate Requests Found, Req Id: " + reqCheckRecord.getReqId());
                output.addMatch(getProcessCode(), "REQ_ID", reqCheckRecord.getReqId() + "", matchType, reqCheckRecord.getMatchGrade() + "", "REQ",
                    itemNo++);
                details.append("Request ID = " + reqCheckRecord.getReqId()).append("\n");
                details.append("Match Type = " + matchType).append("\n");
                details.append("Match Score = " + reqCheckRecord.getMatchGrade()).append("\n");
                details.append("Issuing Country =  " + reqCheckRecord.getIssuingCntry()).append("\n");
                details.append("Customer Name =  " + reqCheckRecord.getCustomerName()).append("\n");
                details.append("Address =  " + reqCheckRecord.getStreetLine1()).append("\n");
                if (!StringUtils.isBlank(reqCheckRecord.getStreetLine2())) {
                  details.append("Address (cont)=  " + reqCheckRecord.getStreetLine2()).append("\n");
                }
                if (!StringUtils.isBlank(reqCheckRecord.getCity())) {
                  details.append("City =  " + reqCheckRecord.getCity()).append("\n");
                }
                if (!StringUtils.isBlank(reqCheckRecord.getStateProv())) {
                  details.append("State =  " + reqCheckRecord.getStateProv()).append("\n");
                }
                if (!StringUtils.isBlank(reqCheckRecord.getPostalCode())) {
                  details.append("Postal Code =  " + reqCheckRecord.getPostalCode()).append("\n");
                }
                if (!StringUtils.isBlank(reqCheckRecord.getLandedCountry())) {
                  details.append("Landed Country =  " + reqCheckRecord.getLandedCountry()).append("\n");
                }
                engineData.put("reqCheckMatches", reqCheckRecord);
              }
              dupReqFound = true;
            } else {
              dupReqFound = false;
            }
          } else {
            dupReqFound = false;
          }
        } else {
          reqChkSrvError = true;
        }
      } else {
        reqChkSrvError = true;
      }

      // perform duplicate request check
      String department = StringUtils.isBlank(soldTo.getDept()) ? "" : soldTo.getDept();
      List<DuplicateCMRCheckResponse> cmrCheckMatches = new ArrayList<DuplicateCMRCheckResponse>();
      // get CMR Matches
      MatchingResponse<DuplicateCMRCheckResponse> responseCMR = getCMRMatches(entityManager, requestData, engineData);
      if (responseCMR != null) {
        if (responseCMR.getSuccess()) {
          if (responseCMR.getMatched()) {
            cmrCheckMatches = responseCMR.getMatches();
            cmrCheckMatches = filterDupCmrs(entityManager, requestData, engineData, cmrCheckMatches);
            // cmr-2067 fix start
            if (matchDepartment) {
              List<DuplicateCMRCheckResponse> cmrCheckMatchesDept = new ArrayList<DuplicateCMRCheckResponse>();
              for (DuplicateCMRCheckResponse cmrCheckRecord : cmrCheckMatches) {
                if (department.equalsIgnoreCase(cmrCheckRecord.getDept()) || department.equalsIgnoreCase(cmrCheckRecord.getCust_name3())
                    || department.equalsIgnoreCase(cmrCheckRecord.getCust_name4())) {
                  cmrCheckMatchesDept.add(cmrCheckRecord);
                }
              }
              Collections.copy(cmrCheckMatches, cmrCheckMatchesDept);
            }
            if (cmrCheckMatches.size() != 0) {
              // result.setResults("Matches Found");
              details.append(cmrCheckMatches.size() + " record(s) found.");
              if (cmrCheckMatches.size() > 5) {
                cmrCheckMatches = cmrCheckMatches.subList(0, 5);
                details.append("Showing top 5 matches only.");
              }

              // int itemNo = 1;
              for (DuplicateCMRCheckResponse cmrCheckRecord : cmrCheckMatches) {
                details.append("\n");

                log.debug("Duplicate CMRs Found..");
                output.addMatch(getProcessCode(), "CMR_NO", cmrCheckRecord.getCmrNo(), "Matching Logic", cmrCheckRecord.getMatchGrade() + "", "CMR",
                    itemNo++);
                if (!StringUtils.isBlank(cmrCheckRecord.getCmrNo())) {
                  details.append("CMR Number = " + cmrCheckRecord.getCmrNo()).append("\n");
                }
                if (!StringUtils.isBlank(cmrCheckRecord.getMatchGrade())) {
                  details.append("Match Grade = " + cmrCheckRecord.getMatchGrade()).append("\n");
                }
                if (!StringUtils.isBlank(cmrCheckRecord.getIssuingCntry())) {
                  details.append("Issuing Country =  " + cmrCheckRecord.getIssuingCntry()).append("\n");
                }
                if (!StringUtils.isBlank(cmrCheckRecord.getCustomerName())) {
                  details.append("Customer Name =  " + cmrCheckRecord.getCustomerName()).append("\n");
                }
                if (!StringUtils.isBlank(cmrCheckRecord.getLandedCountry())) {
                  details.append("Landed Country =  " + cmrCheckRecord.getLandedCountry()).append("\n");
                }
                if (!StringUtils.isBlank(cmrCheckRecord.getStreetLine1())) {
                  details.append("Address =  " + cmrCheckRecord.getStreetLine1()).append("\n");
                }
                if (!StringUtils.isBlank(cmrCheckRecord.getStreetLine2())) {
                  details.append("Address (cont)=  " + cmrCheckRecord.getStreetLine2()).append("\n");
                }
                if (!StringUtils.isBlank(cmrCheckRecord.getCity())) {
                  details.append("City =  " + cmrCheckRecord.getCity()).append("\n");
                }
                if (!StringUtils.isBlank(cmrCheckRecord.getStateProv())) {
                  details.append("State =  " + cmrCheckRecord.getStateProv()).append("\n");
                }
                if (!StringUtils.isBlank(cmrCheckRecord.getPostalCode())) {
                  details.append("Postal Code =  " + cmrCheckRecord.getPostalCode()).append("\n");
                }
                if (!StringUtils.isBlank(cmrCheckRecord.getVat())) {
                  details.append("VAT =  " + cmrCheckRecord.getVat()).append("\n");
                }
                if (!StringUtils.isBlank(cmrCheckRecord.getDunsNo())) {
                  details.append("Duns No =  " + cmrCheckRecord.getDunsNo()).append("\n");
                }
                if (!StringUtils.isBlank(cmrCheckRecord.getParentDunsNo())) {
                  details.append("Parent Duns No =  " + cmrCheckRecord.getParentDunsNo()).append("\n");
                }
                if (!StringUtils.isBlank(cmrCheckRecord.getGuDunsNo())) {
                  details.append("Global Duns No =  " + cmrCheckRecord.getGuDunsNo()).append("\n");
                }
                if (!StringUtils.isBlank(cmrCheckRecord.getCoverageId())) {
                  details.append("Coverage Id=  " + cmrCheckRecord.getCoverageId()).append("\n");
                }
                if (!StringUtils.isBlank(cmrCheckRecord.getIbmClientId())) {
                  details.append("IBM Client Id =  " + cmrCheckRecord.getIbmClientId()).append("\n");
                }
                if (!StringUtils.isBlank(cmrCheckRecord.getCapInd())) {
                  details.append("Cap Indicator =  " + cmrCheckRecord.getCapInd()).append("\n");
                }
                engineData.put("cmrCheckMatches", cmrCheckRecord);
              }
              dupCMRFound = true;
            } else {
              dupCMRFound = false;
            }
          } else {
            dupCMRFound = false;
          }
        } else {
          cmrChkSrvError = true;
        }
      } else {
        cmrChkSrvError = true;
      }

      if (dupReqFound || dupCMRFound) {
        result.setResults("Duplicates found.");
        if (engineData.hasPositiveCheckStatus("allowDuplicates") || reqNegStatFlag) {
          engineData.addNegativeCheckStatus("dupAllowed",
              "There were possible duplicate CMRs/Requests found with the same data but allowed for the scenario.");
        } else {
          engineData.addRejectionComment("DUPC/R", "There were possible duplicate CMRs/Requests found with the same data.", "", "");
          result.setOnError(true);
        }
        result.setProcessOutput(output);
        result.setDetails(details.toString().trim());
      } else if (!reqChkSrvError && !cmrChkSrvError) {
        result.setDetails("No Duplicate CMRs/Requests were found.");
        result.setResults("No Matches");
        result.setOnError(false);
      } else if (reqChkSrvError) {
        if (response != null && StringUtils.isNotBlank(response.getMessage())) {
          result.setDetails(response.getMessage());
          engineData.addRejectionComment("OTH", response.getMessage(), "", "");
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

  public List<ReqCheckResponse> filterDupReqs(EntityManager entityManager, RequestData requestData, AutomationEngineData engineData,
      List<ReqCheckResponse> reqCheckMatches) throws Exception {
    // get request admin and data
    Admin admin = requestData.getAdmin();
    Data data = requestData.getData();
    String subIndCode = StringUtils.isBlank(data.getSubIndustryCd()) ? "" : data.getSubIndustryCd();
    String resToCode = StringUtils.isBlank(data.getRestrictTo()) ? "" : data.getRestrictTo();
    String[] scnarioList = { "POA", "OIO", "OEMHW", "OEM-SW", "TPD", "RFBPO", "SSI", "ICC", "SVMP", "END USER", "POOL", "DEVELOP", "E-HOST", "IGS",
        "IGSF", "NO RESTRICT", "32C", "TPPS", "3CC", "IPMA", "LPMA", "INDIAN TRIBE", "TRIBAL BUS", "HEALTHCARE", "HOSPITAL", "CLINIC", "NATIVE CORP",
        "BYMODEL", "CAMOUFLAGED", "STATE", "SPEC DIST", "COUNTY", "CITY", "HOSPITALS", "SCHOOL PUBLIC", "SCHOOL CHARTER", "SCHOOL PRIV" };
    String scenarioSubType = "";
    if ("C".equals(admin.getReqType()) && data != null) {
      scenarioSubType = StringUtils.isBlank(data.getCustSubGrp()) ? "" : data.getCustSubGrp();
    }

    if (Arrays.asList(scnarioList).contains(scenarioSubType)) {
      List<ReqCheckResponse> reqCheckMatchesTmp = new ArrayList<ReqCheckResponse>();
      if ("POA".equals(scenarioSubType) && subIndCode.startsWith("Y")) {
        for (ReqCheckResponse reqCheckRecord : reqCheckMatches) {
          if (reqCheckRecord.getSubIndustryCd().startsWith("Y")) {
            reqCheckMatchesTmp.add(reqCheckRecord);
          }
        }
        Collections.copy(reqCheckMatches, reqCheckMatchesTmp);
      } else if ("OIO".equals(scenarioSubType)) {
        for (ReqCheckResponse reqCheckRecord : reqCheckMatches) {
          if ("OIO".equalsIgnoreCase(reqCheckRecord.getUsRestrictTo())) {
            reqCheckMatchesTmp.add(reqCheckRecord);
          }
        }
        Collections.copy(reqCheckMatches, reqCheckMatchesTmp);
      } else if ("OEMHW".equals(scenarioSubType) || "OEM-SW".equals(scenarioSubType)) {
        for (ReqCheckResponse reqCheckRecord : reqCheckMatches) {
          if ("OEMHQ".equalsIgnoreCase(reqCheckRecord.getUsRestrictTo())) {
            reqCheckMatchesTmp.add(reqCheckRecord);
          }
        }
        Collections.copy(reqCheckMatches, reqCheckMatchesTmp);
      } else if ("TPD".equals(scenarioSubType)) {
        for (ReqCheckResponse reqCheckRecord : reqCheckMatches) {
          if ("TPD".equalsIgnoreCase(reqCheckRecord.getUsRestrictTo())) {
            reqCheckMatchesTmp.add(reqCheckRecord);
          }
        }
        Collections.copy(reqCheckMatches, reqCheckMatchesTmp);
      } else if ("RFBPO".equals(scenarioSubType)) {
        for (ReqCheckResponse reqCheckRecord : reqCheckMatches) {
          if ("RFBPO".equalsIgnoreCase(reqCheckRecord.getUsRestrictTo())) {
            reqCheckMatchesTmp.add(reqCheckRecord);
          }
        }
        Collections.copy(reqCheckMatches, reqCheckMatchesTmp);
      } else if ("SSI".equals(scenarioSubType)) {
        for (ReqCheckResponse reqCheckRecord : reqCheckMatches) {
          if ("SSI".equalsIgnoreCase(reqCheckRecord.getUsRestrictTo())) {
            reqCheckMatchesTmp.add(reqCheckRecord);
          }
        }
        Collections.copy(reqCheckMatches, reqCheckMatchesTmp);
      } else if ("ICC".equals(scenarioSubType)) {
        for (ReqCheckResponse reqCheckRecord : reqCheckMatches) {
          if ("ICC".equalsIgnoreCase(reqCheckRecord.getUsRestrictTo())) {
            reqCheckMatchesTmp.add(reqCheckRecord);
          }
        }
        Collections.copy(reqCheckMatches, reqCheckMatchesTmp);
      } else if ("SVMP".equals(scenarioSubType)) {
        for (ReqCheckResponse reqCheckRecord : reqCheckMatches) {
          if ("SVMP".equalsIgnoreCase(reqCheckRecord.getUsRestrictTo())) {
            reqCheckMatchesTmp.add(reqCheckRecord);
          }
        }
        Collections.copy(reqCheckMatches, reqCheckMatchesTmp);
      } else if ("END USER".equals(scenarioSubType)) {
        if ("BPQS".equals(resToCode)) {
          for (ReqCheckResponse reqCheckRecord : reqCheckMatches) {
            if ("BPQS".equalsIgnoreCase(reqCheckRecord.getUsRestrictTo())) {
              reqCheckMatchesTmp.add(reqCheckRecord);
            }
          }
        } else if ("IRCSO".equals(resToCode)) {
          for (ReqCheckResponse reqCheckRecord : reqCheckMatches) {
            if ("IRCSO".equalsIgnoreCase(reqCheckRecord.getUsRestrictTo())) {
              reqCheckMatchesTmp.add(reqCheckRecord);
            }
          }
        }
        Collections.copy(reqCheckMatches, reqCheckMatchesTmp);
      } else if ("POOL".equals(scenarioSubType) || "DEVELOP".equals(scenarioSubType) || "E-HOST".equals(scenarioSubType)) {
        for (ReqCheckResponse reqCheckRecord : reqCheckMatches) {
          if ("BPQS".equalsIgnoreCase(reqCheckRecord.getUsRestrictTo())) {
            reqCheckMatchesTmp.add(reqCheckRecord);
          }
        }
        Collections.copy(reqCheckMatches, reqCheckMatchesTmp);
      } else if ("IGS".equals(scenarioSubType)) {
        for (ReqCheckResponse reqCheckRecord : reqCheckMatches) {
          if ("11505720".equals(reqCheckRecord.getCompany())) {
            reqCheckMatchesTmp.add(reqCheckRecord);
            reqNegStatFlag = true;
          }
        }
        Collections.copy(reqCheckMatches, reqCheckMatchesTmp);
      } else if ("IGSF".equals(scenarioSubType)) {
        for (ReqCheckResponse reqCheckRecord : reqCheckMatches) {
          if ("11774312".equals(reqCheckRecord.getCompany())) {
            reqCheckMatchesTmp.add(reqCheckRecord);
            reqNegStatFlag = true;
          }
        }
        Collections.copy(reqCheckMatches, reqCheckMatchesTmp);
      } else if ("NO RESTRICT".equals(scenarioSubType) || "32C".equals(scenarioSubType)) {
        for (ReqCheckResponse reqCheckRecord : reqCheckMatches) {
          if (StringUtils.isBlank(reqCheckRecord.getUsRestrictTo())) {
            reqCheckMatchesTmp.add(reqCheckRecord);
            reqNegStatFlag = true;
          }
        }
        Collections.copy(reqCheckMatches, reqCheckMatchesTmp);
      } else if ("TPPS".equals(scenarioSubType)) {
        for (ReqCheckResponse reqCheckRecord : reqCheckMatches) {
          if ("TPPS".equalsIgnoreCase(reqCheckRecord.getUsRestrictTo())) {
            reqCheckMatchesTmp.add(reqCheckRecord);
          }
        }
        Collections.copy(reqCheckMatches, reqCheckMatchesTmp);
      } else if ("3CC".equals(scenarioSubType)) {
        for (ReqCheckResponse reqCheckRecord : reqCheckMatches) {
          if ("ICC".equalsIgnoreCase(reqCheckRecord.getUsRestrictTo()) && "12003567".equals(reqCheckRecord.getCompany())) {
            reqCheckMatchesTmp.add(reqCheckRecord);
          }
        }
        Collections.copy(reqCheckMatches, reqCheckMatchesTmp);
      } else if ("IPMA".equals(scenarioSubType) || "LPMA".equals(scenarioSubType)) {
        for (ReqCheckResponse reqCheckRecord : reqCheckMatches) {
          if ("ICC".equalsIgnoreCase(reqCheckRecord.getUsRestrictTo()) && !"12003567".equals(reqCheckRecord.getCompany())) {
            reqCheckMatchesTmp.add(reqCheckRecord);
            reqNegStatFlag = true;
          }
        }
        Collections.copy(reqCheckMatches, reqCheckMatchesTmp);
      } else if ("INDIAN TRIBE".equals(scenarioSubType) || "TRIBAL BUS".equals(scenarioSubType) || "HEALTHCARE".equals(scenarioSubType)
          || "CLINIC".equals(scenarioSubType) || "NATIVE CORP".equals(scenarioSubType) || "CAMOUFLAGED".equals(scenarioSubType)
          || "STATE".equals(scenarioSubType) || "SPEC DIST".equals(scenarioSubType) || "COUNTY".equals(scenarioSubType)
          || "CITY".equals(scenarioSubType) || "HOSPITALS".equals(scenarioSubType) || "SCHOOL PUBLIC".equals(scenarioSubType)
          || "SCHOOL CHARTER".equals(scenarioSubType) || "SCHOOL PRIV".equals(scenarioSubType)) {
        Collections.copy(reqCheckMatches, reqCheckMatches);
        reqNegStatFlag = true;
      } else if ("BYMODEL".equals(scenarioSubType)) {
        USDetailsContainer usDetails = USUtil.determineUSCMRDetails(entityManager, requestData.getAdmin().getModelCmrNo(), engineData);

        if ("6".equals(usDetails.getCustTypCd())) {
          for (ReqCheckResponse reqCheckRecord : reqCheckMatches) {
            if (StringUtils.isNotBlank(reqCheckRecord.getCompany()) && StringUtils.isNotBlank(usDetails.getCompanyNo())
                && reqCheckRecord.getCompany().equalsIgnoreCase(usDetails.getCompanyNo())) {
              reqCheckMatchesTmp.add(reqCheckRecord);
              reqNegStatFlag = true;
            }
          }
          Collections.copy(reqCheckMatches, reqCheckMatchesTmp);
        } else if ("4".equals(usDetails.getCustTypCd())) {
          for (ReqCheckResponse reqCheckRecord : reqCheckMatches) {
            if (StringUtils.isNotBlank(reqCheckRecord.getCompany()) && StringUtils.isNotBlank(usDetails.getCompanyNo())
                && reqCheckRecord.getCompany().equalsIgnoreCase(usDetails.getCompanyNo())) {
              reqCheckMatchesTmp.add(reqCheckRecord);
              reqNegStatFlag = true;
            }
          }
          Collections.copy(reqCheckMatches, reqCheckMatchesTmp);
        } else if ("3".equals(usDetails.getCustTypCd())) {
          for (ReqCheckResponse reqCheckRecord : reqCheckMatches) {
            if (StringUtils.isNotBlank(reqCheckRecord.getCompany()) && StringUtils.isNotBlank(usDetails.getCompanyNo())
                && reqCheckRecord.getCompany().equalsIgnoreCase(usDetails.getCompanyNo()) && StringUtils.isNotBlank(reqCheckRecord.getUsRestrictTo())
                && StringUtils.isNotBlank(usDetails.getUsRestrictTo())
                && reqCheckRecord.getUsRestrictTo().equalsIgnoreCase(usDetails.getUsRestrictTo())) {
              reqCheckMatchesTmp.add(reqCheckRecord);
              reqNegStatFlag = true;
            }
          }
          Collections.copy(reqCheckMatches, reqCheckMatchesTmp);
        } else if ("7".equals(usDetails.getCustTypCd())) {
          for (ReqCheckResponse reqCheckRecord : reqCheckMatches) {
            if (StringUtils.isNotBlank(reqCheckRecord.getCompany()) && StringUtils.isNotBlank(usDetails.getCompanyNo())
                && reqCheckRecord.getCompany().equalsIgnoreCase(usDetails.getCompanyNo()) && StringUtils.isNotBlank(reqCheckRecord.getUsRestrictTo())
                && StringUtils.isNotBlank(usDetails.getUsRestrictTo())
                && reqCheckRecord.getUsRestrictTo().equalsIgnoreCase(usDetails.getUsRestrictTo())) {
              reqCheckMatchesTmp.add(reqCheckRecord);
              reqNegStatFlag = true;
            }
          }
          Collections.copy(reqCheckMatches, reqCheckMatchesTmp);
        } else if ("2".equals(usDetails.getCustTypCd())) {
          for (ReqCheckResponse reqCheckRecord : reqCheckMatches) {
            if (StringUtils.isNotBlank(reqCheckRecord.getCompany()) && StringUtils.isNotBlank(usDetails.getCompanyNo())
                && reqCheckRecord.getCompany().equalsIgnoreCase(usDetails.getCompanyNo())) {
              reqCheckMatchesTmp.add(reqCheckRecord);
              reqNegStatFlag = true;
            }
          }
          Collections.copy(reqCheckMatches, reqCheckMatchesTmp);
        } else if ("1".equals(usDetails.getCustTypCd())) {
          for (ReqCheckResponse reqCheckRecord : reqCheckMatches) {
            if (StringUtils.isNotBlank(reqCheckRecord.getCompany()) && StringUtils.isNotBlank(usDetails.getCompanyNo())
                && reqCheckRecord.getCompany().equalsIgnoreCase(usDetails.getCompanyNo())
                && ((StringUtils.isNotBlank(reqCheckRecord.getUsRestrictTo()) && StringUtils.isNotBlank(usDetails.getUsRestrictTo())
                    && reqCheckRecord.getUsRestrictTo().equalsIgnoreCase(usDetails.getUsRestrictTo()))
                    || (StringUtils.isBlank(usDetails.getUsRestrictTo()) && StringUtils.isBlank(reqCheckRecord.getUsRestrictTo())))) {
              reqCheckMatchesTmp.add(reqCheckRecord);
            }
          }
          Collections.copy(reqCheckMatches, reqCheckMatchesTmp);
        } else {
          Collections.copy(reqCheckMatches, reqCheckMatches);
          reqNegStatFlag = true;
        }
      } else {
        Collections.copy(reqCheckMatches, reqCheckMatches);
      }
      reqCheckMatchesTmp.clear();
    }
    return reqCheckMatches;

  }

  public List<DuplicateCMRCheckResponse> filterDupCmrs(EntityManager entityManager, RequestData requestData, AutomationEngineData engineData,
      List<DuplicateCMRCheckResponse> cmrCheckMatches) throws Exception {
    // get request admin and data
    Admin admin = requestData.getAdmin();
    Data data = requestData.getData();
    String subIndCode = StringUtils.isBlank(data.getSubIndustryCd()) ? "" : data.getSubIndustryCd();
    String resToCode = StringUtils.isBlank(data.getRestrictTo()) ? "" : data.getRestrictTo();
    String[] scnarioList = { "POA", "OIO", "OEMHW", "OEM-SW", "TPD", "RFBPO", "SSI", "ICC", "SVMP", "END USER", "POOL", "DEVELOP", "E-HOST", "IGS",
        "IGSF", "NO RESTRICT", "32C", "TPPS", "3CC", "IPMA", "LPMA", "INDIAN TRIBE", "TRIBAL BUS", "HEALTHCARE", "HOSPITAL", "CLINIC", "NATIVE CORP",
        "BYMODEL", "CAMOUFLAGED", "STATE", "SPEC DIST", "COUNTY", "CITY", "HOSPITALS", "SCHOOL PUBLIC", "SCHOOL CHARTER", "SCHOOL PRIV" };
    String scenarioSubType = "";
    if ("C".equals(admin.getReqType()) && data != null) {
      scenarioSubType = StringUtils.isBlank(data.getCustSubGrp()) ? "" : data.getCustSubGrp();
    }

    if (Arrays.asList(scnarioList).contains(scenarioSubType)) {
      List<DuplicateCMRCheckResponse> cmrCheckMatchesTmp = new ArrayList<DuplicateCMRCheckResponse>();
      if ("POA".equals(scenarioSubType) && subIndCode.startsWith("Y")) {
        for (DuplicateCMRCheckResponse cmrCheckRecord : cmrCheckMatches) {
          if (cmrCheckRecord.getSubIndustryCd().startsWith("Y")) {
            cmrCheckMatchesTmp.add(cmrCheckRecord);
          }
        }
        Collections.copy(cmrCheckMatches, cmrCheckMatchesTmp);
      } else if ("OIO".equals(scenarioSubType)) {
        for (DuplicateCMRCheckResponse cmrCheckRecord : cmrCheckMatches) {
          if ("OIO".equalsIgnoreCase(cmrCheckRecord.getUsRestrictTo())) {
            cmrCheckMatchesTmp.add(cmrCheckRecord);
          }
        }
        Collections.copy(cmrCheckMatches, cmrCheckMatchesTmp);
      } else if ("OEMHW".equals(scenarioSubType) || "OEM-SW".equals(scenarioSubType)) {
        for (DuplicateCMRCheckResponse cmrCheckRecord : cmrCheckMatches) {
          if ("OEMHQ".equalsIgnoreCase(cmrCheckRecord.getUsRestrictTo())) {
            cmrCheckMatchesTmp.add(cmrCheckRecord);
          }
        }
        Collections.copy(cmrCheckMatches, cmrCheckMatchesTmp);
      } else if ("TPD".equals(scenarioSubType)) {
        for (DuplicateCMRCheckResponse cmrCheckRecord : cmrCheckMatches) {
          if ("TPD".equalsIgnoreCase(cmrCheckRecord.getUsRestrictTo())) {
            cmrCheckMatchesTmp.add(cmrCheckRecord);
          }
        }
        Collections.copy(cmrCheckMatches, cmrCheckMatchesTmp);
      } else if ("RFBPO".equals(scenarioSubType)) {
        for (DuplicateCMRCheckResponse cmrCheckRecord : cmrCheckMatches) {
          if ("RFBPO".equalsIgnoreCase(cmrCheckRecord.getUsRestrictTo())) {
            cmrCheckMatchesTmp.add(cmrCheckRecord);
          }
        }
        Collections.copy(cmrCheckMatches, cmrCheckMatchesTmp);
      } else if ("SSI".equals(scenarioSubType)) {
        for (DuplicateCMRCheckResponse cmrCheckRecord : cmrCheckMatches) {
          if ("SSI".equalsIgnoreCase(cmrCheckRecord.getUsRestrictTo())) {
            cmrCheckMatchesTmp.add(cmrCheckRecord);
          }
        }
        Collections.copy(cmrCheckMatches, cmrCheckMatchesTmp);
      } else if ("ICC".equals(scenarioSubType)) {
        for (DuplicateCMRCheckResponse cmrCheckRecord : cmrCheckMatches) {
          if ("ICC".equalsIgnoreCase(cmrCheckRecord.getUsRestrictTo())) {
            cmrCheckMatchesTmp.add(cmrCheckRecord);
          }
        }
        Collections.copy(cmrCheckMatches, cmrCheckMatchesTmp);
      } else if ("SVMP".equals(scenarioSubType)) {
        for (DuplicateCMRCheckResponse cmrCheckRecord : cmrCheckMatches) {
          if ("SVMP".equalsIgnoreCase(cmrCheckRecord.getUsRestrictTo())) {
            cmrCheckMatchesTmp.add(cmrCheckRecord);
          }
        }
        Collections.copy(cmrCheckMatches, cmrCheckMatchesTmp);
      } else if ("END USER".equals(scenarioSubType)) {
        if ("BPQS".equals(resToCode)) {
          for (DuplicateCMRCheckResponse cmrCheckRecord : cmrCheckMatches) {
            if ("BPQS".equalsIgnoreCase(cmrCheckRecord.getUsRestrictTo())) {
              cmrCheckMatchesTmp.add(cmrCheckRecord);
            }
          }
        } else if ("IRCSO".equals(resToCode)) {
          for (DuplicateCMRCheckResponse cmrCheckRecord : cmrCheckMatches) {
            if ("IRCSO".equalsIgnoreCase(cmrCheckRecord.getUsRestrictTo())) {
              cmrCheckMatchesTmp.add(cmrCheckRecord);
            }
          }
        }
        Collections.copy(cmrCheckMatches, cmrCheckMatchesTmp);
      } else if ("POOL".equals(scenarioSubType) || "DEVELOP".equals(scenarioSubType) || "E-HOST".equals(scenarioSubType)) {
        for (DuplicateCMRCheckResponse cmrCheckRecord : cmrCheckMatches) {
          if ("BPQS".equalsIgnoreCase(cmrCheckRecord.getUsRestrictTo())) {
            cmrCheckMatchesTmp.add(cmrCheckRecord);
          }
        }
        Collections.copy(cmrCheckMatches, cmrCheckMatchesTmp);
      } else if ("IGS".equals(scenarioSubType)) {
        for (DuplicateCMRCheckResponse cmrCheckRecord : cmrCheckMatches) {
          if ("11505720".equals(cmrCheckRecord.getCompany())) {
            cmrCheckMatchesTmp.add(cmrCheckRecord);
            reqNegStatFlag = true;
          }
        }
        Collections.copy(cmrCheckMatches, cmrCheckMatchesTmp);
      } else if ("IGSF".equals(scenarioSubType)) {
        for (DuplicateCMRCheckResponse cmrCheckRecord : cmrCheckMatches) {
          if ("11774312".equals(cmrCheckRecord.getCompany())) {
            cmrCheckMatchesTmp.add(cmrCheckRecord);
            reqNegStatFlag = true;
          }
        }
        Collections.copy(cmrCheckMatches, cmrCheckMatchesTmp);
      } else if ("NO RESTRICT".equals(scenarioSubType) || "32C".equals(scenarioSubType)) {
        for (DuplicateCMRCheckResponse cmrCheckRecord : cmrCheckMatches) {
          if (StringUtils.isBlank(cmrCheckRecord.getUsRestrictTo())) {
            cmrCheckMatchesTmp.add(cmrCheckRecord);
            reqNegStatFlag = true;
          }
        }
        Collections.copy(cmrCheckMatches, cmrCheckMatchesTmp);
      } else if ("TPPS".equals(scenarioSubType)) {
        for (DuplicateCMRCheckResponse cmrCheckRecord : cmrCheckMatches) {
          if ("TPPS".equalsIgnoreCase(cmrCheckRecord.getUsRestrictTo())) {
            cmrCheckMatchesTmp.add(cmrCheckRecord);
          }
        }
        Collections.copy(cmrCheckMatches, cmrCheckMatchesTmp);
      } else if ("3CC".equals(scenarioSubType)) {
        for (DuplicateCMRCheckResponse cmrCheckRecord : cmrCheckMatches) {
          if ("ICC".equalsIgnoreCase(cmrCheckRecord.getUsRestrictTo()) && "12003567".equals(cmrCheckRecord.getCompany())) {
            cmrCheckMatchesTmp.add(cmrCheckRecord);
          }
        }
        Collections.copy(cmrCheckMatches, cmrCheckMatchesTmp);
      } else if ("IPMA".equals(scenarioSubType) || "LPMA".equals(scenarioSubType)) {
        for (DuplicateCMRCheckResponse cmrCheckRecord : cmrCheckMatches) {
          if ("ICC".equalsIgnoreCase(cmrCheckRecord.getUsRestrictTo()) && !"12003567".equals(cmrCheckRecord.getCompany())) {
            cmrCheckMatchesTmp.add(cmrCheckRecord);
            reqNegStatFlag = true;
          }
        }
        Collections.copy(cmrCheckMatches, cmrCheckMatchesTmp);
      } else if ("INDIAN TRIBE".equals(scenarioSubType) || "TRIBAL BUS".equals(scenarioSubType) || "HEALTHCARE".equals(scenarioSubType)
          || "CLINIC".equals(scenarioSubType) || "NATIVE CORP".equals(scenarioSubType) || "CAMOUFLAGED".equals(scenarioSubType)
          || "STATE".equals(scenarioSubType) || "SPEC DIST".equals(scenarioSubType) || "COUNTY".equals(scenarioSubType)
          || "CITY".equals(scenarioSubType) || "HOSPITALS".equals(scenarioSubType) || "SCHOOL PUBLIC".equals(scenarioSubType)
          || "SCHOOL CHARTER".equals(scenarioSubType) || "SCHOOL PRIV".equals(scenarioSubType)) {
        Collections.copy(cmrCheckMatches, cmrCheckMatches);
        reqNegStatFlag = true;
      } else if ("BYMODEL".equals(scenarioSubType)) {
        USDetailsContainer usDetails = USUtil.determineUSCMRDetails(entityManager, requestData.getAdmin().getModelCmrNo(), engineData);

        if ("6".equals(usDetails.getCustTypCd())) {
          for (DuplicateCMRCheckResponse cmrCheckRecord : cmrCheckMatches) {
            if (StringUtils.isNotBlank(cmrCheckRecord.getCompany()) && StringUtils.isNotBlank(usDetails.getCompanyNo())
                && cmrCheckRecord.getCompany().equalsIgnoreCase(usDetails.getCompanyNo())) {
              cmrCheckMatchesTmp.add(cmrCheckRecord);
              reqNegStatFlag = true;
            }
          }
          Collections.copy(cmrCheckMatches, cmrCheckMatchesTmp);
        } else if ("4".equals(usDetails.getCustTypCd())) {
          for (DuplicateCMRCheckResponse cmrCheckRecord : cmrCheckMatches) {
            if (StringUtils.isNotBlank(cmrCheckRecord.getCompany()) && StringUtils.isNotBlank(usDetails.getCompanyNo())
                && cmrCheckRecord.getCompany().equalsIgnoreCase(usDetails.getCompanyNo())) {
              cmrCheckMatchesTmp.add(cmrCheckRecord);
              reqNegStatFlag = true;
            }
          }
          Collections.copy(cmrCheckMatches, cmrCheckMatchesTmp);
        } else if ("3".equals(usDetails.getCustTypCd())) {
          for (DuplicateCMRCheckResponse cmrCheckRecord : cmrCheckMatches) {
            if (StringUtils.isNotBlank(cmrCheckRecord.getCompany()) && StringUtils.isNotBlank(usDetails.getCompanyNo())
                && cmrCheckRecord.getCompany().equalsIgnoreCase(usDetails.getCompanyNo()) && StringUtils.isNotBlank(cmrCheckRecord.getUsRestrictTo())
                && StringUtils.isNotBlank(usDetails.getUsRestrictTo())
                && cmrCheckRecord.getUsRestrictTo().equalsIgnoreCase(usDetails.getUsRestrictTo())) {
              cmrCheckMatchesTmp.add(cmrCheckRecord);
              reqNegStatFlag = true;
            }
          }
          Collections.copy(cmrCheckMatches, cmrCheckMatchesTmp);
        } else if ("7".equals(usDetails.getCustTypCd())) {
          Collections.copy(cmrCheckMatches, cmrCheckMatches);
          reqNegStatFlag = true;
        } else if ("2".equals(usDetails.getCustTypCd())) {
          for (DuplicateCMRCheckResponse cmrCheckRecord : cmrCheckMatches) {
            if (StringUtils.isNotBlank(cmrCheckRecord.getCompany()) && StringUtils.isNotBlank(usDetails.getCompanyNo())
                && cmrCheckRecord.getCompany().equalsIgnoreCase(usDetails.getCompanyNo())) {
              cmrCheckMatchesTmp.add(cmrCheckRecord);
              reqNegStatFlag = true;
            }
          }
          Collections.copy(cmrCheckMatches, cmrCheckMatchesTmp);
        } else if ("1".equals(usDetails.getCustTypCd())) {
          for (DuplicateCMRCheckResponse cmrCheckRecord : cmrCheckMatches) {
            if (StringUtils.isNotBlank(cmrCheckRecord.getCompany()) && StringUtils.isNotBlank(usDetails.getCompanyNo())
                && cmrCheckRecord.getCompany().equalsIgnoreCase(usDetails.getCompanyNo())
                && ((StringUtils.isNotBlank(cmrCheckRecord.getUsRestrictTo()) && StringUtils.isNotBlank(usDetails.getUsRestrictTo())
                    && cmrCheckRecord.getUsRestrictTo().equalsIgnoreCase(usDetails.getUsRestrictTo()))
                    || (StringUtils.isBlank(usDetails.getUsRestrictTo()) && StringUtils.isBlank(cmrCheckRecord.getUsRestrictTo())))) {
              cmrCheckMatchesTmp.add(cmrCheckRecord);
            }
          }
          Collections.copy(cmrCheckMatches, cmrCheckMatchesTmp);
        } else {
          Collections.copy(cmrCheckMatches, cmrCheckMatches);
          reqNegStatFlag = true;
        }
      } else {
        Collections.copy(cmrCheckMatches, cmrCheckMatches);
      }
      cmrCheckMatchesTmp.clear();
    }
    return cmrCheckMatches;
  }

  public MatchingResponse<ReqCheckResponse> getReqMatches(EntityManager entityManager, RequestData requestData, AutomationEngineData engineData)
      throws Exception {
    Admin admin = requestData.getAdmin();
    Data data = requestData.getData();
    ScenarioExceptionsUtil scenarioExceptions = getScenarioExceptions(entityManager, requestData, engineData);
    MatchingResponse<ReqCheckResponse> response = new MatchingResponse<ReqCheckResponse>();

    // check if End user and has divn value
    if ("END USER".equals(data.getCustSubGrp()) && "C".equals(admin.getReqType())) {
      Addr zs01 = requestData.getAddress("ZS01");
      if (zs01 != null && StringUtils.isBlank(zs01.getDivn())) {
        response.setSuccess(false);
        response.setMatched(false);
        response.setMessage("Division Line Empty for End User request.");
        return response;
      }
    }

    MatchingServiceClient client = CmrServicesFactory.getInstance().createClient(SystemConfiguration.getValue("BATCH_SERVICES_URL"),
        MatchingServiceClient.class);
    client.setReadTimeout(1000 * 60 * 5);
    boolean first = true;
    for (String addrType : scenarioExceptions.getAddressTypesForDuplicateRequestCheck()) {
      Addr addr = requestData.getAddress(addrType);
      if (addr != null) {
        ReqCheckRequest request = getRequestForReqChk(entityManager, data, admin, addr, scenarioExceptions);
        log.debug("Executing Duplicate Request Check "
            + (admin.getId().getReqId() > 0 ? " for Request ID: " + admin.getId().getReqId() : " through UI") + " for AddrType: " + addrType);
        MatchingResponse<?> rawResponse = client.executeAndWrap(MatchingServiceClient.REQ_SERVICE_ID, request, MatchingResponse.class);
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(rawResponse);
        TypeReference<MatchingResponse<ReqCheckResponse>> ref = new TypeReference<MatchingResponse<ReqCheckResponse>>() {
        };
        MatchingResponse<ReqCheckResponse> res = mapper.readValue(json, ref);

        if (first) {
          if (res.getSuccess()) {
            response = res;
            first = false;
          } else {
            return res;
          }
        } else if (response.getMatches().size() > 0 && res.getSuccess()) {
          updateReqMatches(response, res);
        }

      } else {
        log.debug("No '" + addrType + "' address on the request. Skipping duplicate check.");
        continue;
      }
    }

    // reverify
    if (response.getMatches().size() == 0) {
      response.setMatched(false);
      response.setMessage("No matches found for the given search criteria.");
    }

    return response;
  }

  private void updateReqMatches(MatchingResponse<ReqCheckResponse> global, MatchingResponse<ReqCheckResponse> iteration) {
    List<ReqCheckResponse> updated = new ArrayList<ReqCheckResponse>();
    for (ReqCheckResponse i : global.getMatches()) {
      for (ReqCheckResponse j : iteration.getMatches()) {
        if (i.getReqId() == j.getReqId()) {
          updated.add(i);
          break;
        }
      }
    }

    global.setSuccess(true);
    global.setMatches(updated);
    global.setMatched(updated.size() > 0);
  }

  private ReqCheckRequest getRequestForReqChk(EntityManager entityManager, Data data, Admin admin, Addr addr,
      ScenarioExceptionsUtil scenarioExceptions) {
    ReqCheckRequest request = new ReqCheckRequest();
    request.setReqId(admin.getId().getReqId());
    String scenarioSubType = "";
    if ("C".equals(admin.getReqType()) && data != null) {
      scenarioSubType = StringUtils.isBlank(data.getCustSubGrp()) ? "" : data.getCustSubGrp();
    }
    if (addr != null) {
      request.setAddrType(addr.getId().getAddrType());
      request.setCity(addr.getCity1());

      if ("END USER".equals(scenarioSubType)) {
        if ("ZS01".equals(addr.getId().getAddrType()) && addr.getDivn() != null) {
          request.setCustomerName(StringUtils.isBlank(addr.getDivn()) ? "" : addr.getDivn());
        } else if ("ZI01".equals(addr.getId().getAddrType()) && admin.getMainCustNm1() != null) {
          request.setCustomerName(admin.getMainCustNm1() + (StringUtils.isBlank(admin.getMainCustNm2()) ? "" : " " + admin.getMainCustNm2()));
        } else {
          if (admin.getMainCustNm1() != null) {
            request.setCustomerName(admin.getMainCustNm1() + (StringUtils.isBlank(admin.getMainCustNm2()) ? "" : " " + admin.getMainCustNm2()));
          } else {
            request.setCustomerName(addr.getCustNm1() + (StringUtils.isBlank(addr.getCustNm2()) ? "" : " " + addr.getCustNm2()));
          }
        }
      } else {
        if (admin.getMainCustNm1() != null) {
          request.setCustomerName(admin.getMainCustNm1() + (StringUtils.isBlank(admin.getMainCustNm2()) ? "" : " " + admin.getMainCustNm2()));
        } else {
          request.setCustomerName(addr.getCustNm1() + (StringUtils.isBlank(addr.getCustNm2()) ? "" : " " + addr.getCustNm2()));
        }
      }

      request.setStreetLine1(addr.getAddrTxt());
      request.setStreetLine2(addr.getAddrTxt2());
      request.setLandedCountry(addr.getLandCntry());
      request.setIssuingCountry(data.getCmrIssuingCntry());
      request.setPostalCode(addr.getPostCd());
      request.setStateProv(addr.getStateProv());
      if (StringUtils.isNotBlank(data.getVat())) {
        request.setVat(data.getVat());
      } else if (StringUtils.isNotBlank(addr.getVat())) {
        request.setVat(addr.getVat());
      }

      if (StringUtils.isNotBlank(request.getIssuingCountry())) {
        if (StringUtils.isNotBlank(data.getCustSubGrp()) && SystemLocation.BRAZIL.equals(request.getIssuingCountry())) {
          request.setScenario(data.getCustSubGrp());
        }

        if (AutomationUtil.isCheckVatForDuplicates(data.getCmrIssuingCntry())) {
          request.setMatchType("V");
        }
      }

      DuplicateChecksUtil.setCountrySpecificsForRequestChecks(entityManager, admin, data, addr, request);
    }
    return request;
  }

  public MatchingResponse<DuplicateCMRCheckResponse> getCMRMatches(EntityManager entityManager, RequestData requestData,
      AutomationEngineData engineData) throws Exception {
    Admin admin = requestData.getAdmin();
    Data data = requestData.getData();
    ScenarioExceptionsUtil scenarioExceptions = getScenarioExceptions(entityManager, requestData, engineData);
    MatchingResponse<DuplicateCMRCheckResponse> response = new MatchingResponse<DuplicateCMRCheckResponse>();

    // check if End user and has divn value
    if ("END USER".equals(data.getCustSubGrp()) && "C".equals(admin.getReqType())) {
      Addr zs01 = requestData.getAddress("ZS01");
      if (zs01 != null && StringUtils.isBlank(zs01.getDivn())) {
        response.setSuccess(false);
        response.setMatched(false);
        response.setMessage("Division Line Empty for End User request.");
        return response;
      }
    }

    MatchingServiceClient client = CmrServicesFactory.getInstance().createClient(SystemConfiguration.getValue("BATCH_SERVICES_URL"),
        MatchingServiceClient.class);
    client.setReadTimeout(1000 * 60 * 5);
    Map<String, List<String>> addrTypes = scenarioExceptions.getAddressTypesForDuplicateCMRCheck();
    boolean vatMatchRequired = AutomationUtil.isCheckVatForDuplicates(data.getCmrIssuingCntry());
    if (addrTypes.isEmpty()) {
      addrTypes.put("ZS01", Arrays.asList("ZS01"));
    }
    boolean first = true;
    List<String> rdcAddrTypes = null;
    for (String cmrAddrType : addrTypes.keySet()) {
      log.debug("CmrAddrType= " + cmrAddrType);
      rdcAddrTypes = addrTypes.get(cmrAddrType);
      Addr addr = requestData.getAddress(cmrAddrType);
      for (String rdcAddrType : rdcAddrTypes) {
        if (addr != null) {
          DuplicateCMRCheckRequest request = getRequestForCmrChk(entityManager, data, admin, addr, rdcAddrType, vatMatchRequired);
          log.debug("Executing Duplicate CMR Check " + (admin.getId().getReqId() > 0 ? " for Request ID: " + admin.getId().getReqId() : " through UI")
              + " for AddrType: " + cmrAddrType + "-" + rdcAddrType);
          MatchingResponse<?> rawResponse = client.executeAndWrap(MatchingServiceClient.CMR_SERVICE_ID, request, MatchingResponse.class);
          ObjectMapper mapper = new ObjectMapper();
          String json = mapper.writeValueAsString(rawResponse);
          TypeReference<MatchingResponse<DuplicateCMRCheckResponse>> ref = new TypeReference<MatchingResponse<DuplicateCMRCheckResponse>>() {
          };
          MatchingResponse<DuplicateCMRCheckResponse> res = mapper.readValue(json, ref);

          if (first) {
            if (res.getSuccess()) {
              response = res;
              first = false;
            } else {
              return res;
            }
          } else if (response.getMatches().size() > 0 && res.getSuccess()) {
            updateCmrMatches(response, res);
          }

        } else {
          log.debug("No '" + cmrAddrType + "' address on the request. Skipping duplicate check.");
          continue;
        }
      }
    }

    // reverify
    if (response.getMatches().size() == 0) {
      response.setMatched(false);
      response.setMessage("No matches found for the given search criteria.");
    }

    return response;
  }

  private DuplicateCMRCheckRequest getRequestForCmrChk(EntityManager entityManager, Data data, Admin admin, Addr addr, String rdcAddrType,
      boolean vatMatchRequired) {
    DuplicateCMRCheckRequest request = new DuplicateCMRCheckRequest();
    String scenarioSubType = "";
    if ("C".equals(admin.getReqType()) && data != null) {
      scenarioSubType = StringUtils.isBlank(data.getCustSubGrp()) ? "" : data.getCustSubGrp();
    }
    if (addr != null) {
      request.setIssuingCountry(data.getCmrIssuingCntry());
      request.setLandedCountry(addr.getLandCntry());

      if ("END USER".equals(scenarioSubType)) {
        if ("ZS01".equals(addr.getId().getAddrType())) {
          request.setCustomerName(StringUtils.isBlank(addr.getDivn()) ? "" : addr.getDivn());
        } else if ("ZI01".equals(addr.getId().getAddrType()) && admin.getMainCustNm1() != null) {
          request.setCustomerName(admin.getMainCustNm1() + (StringUtils.isBlank(admin.getMainCustNm2()) ? "" : " " + admin.getMainCustNm2()));
        }
      } else {
        if (admin.getMainCustNm1() != null) {
          request.setCustomerName(admin.getMainCustNm1() + (StringUtils.isBlank(admin.getMainCustNm2()) ? "" : " " + admin.getMainCustNm2()));
        } else {
          request.setCustomerName(addr.getCustNm1() + (StringUtils.isBlank(addr.getCustNm2()) ? "" : " " + addr.getCustNm2()));
        }
      }

      request.setStreetLine1(addr.getAddrTxt());
      request.setStreetLine2(StringUtils.isEmpty(addr.getAddrTxt2()) ? "" : addr.getAddrTxt2());
      request.setCity(addr.getCity1());

      request.setStateProv(addr.getStateProv());
      request.setPostalCode(addr.getPostCd());

      if (vatMatchRequired) {
        if (StringUtils.isNotBlank(data.getVat())) {
          request.setVat(data.getVat());
        } else if (StringUtils.isNotBlank(addr.getVat())) {
          request.setVat(addr.getVat());
        }
      }
      request.setAddrType(rdcAddrType);

      DuplicateChecksUtil.setCountrySpecificsForCMRChecks(entityManager, admin, data, addr, request);
    }
    return request;
  }

  private void updateCmrMatches(MatchingResponse<DuplicateCMRCheckResponse> global, MatchingResponse<DuplicateCMRCheckResponse> iteration) {
    List<DuplicateCMRCheckResponse> updated = new ArrayList<DuplicateCMRCheckResponse>();
    for (DuplicateCMRCheckResponse resp1 : global.getMatches()) {
      for (DuplicateCMRCheckResponse resp2 : iteration.getMatches()) {
        if (resp1.getCmrNo().equals(resp2.getCmrNo())) {
          updated.add(resp1);
          break;
        }
      }
    }

    global.setSuccess(true);
    global.setMatches(updated);
    global.setMatched(updated.size() > 0);
  }

  @Override
  public String getProcessCode() {
    // TODO Auto-generated method stub
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

}
