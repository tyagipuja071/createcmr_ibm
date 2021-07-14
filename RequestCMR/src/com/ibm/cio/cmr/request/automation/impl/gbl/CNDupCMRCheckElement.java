package com.ibm.cio.cmr.request.automation.impl.gbl;

import java.util.ArrayList;
import java.util.Arrays;
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
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.AutomationMatching;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.entity.IntlAddr;
import com.ibm.cio.cmr.request.model.CompanyRecordModel;
import com.ibm.cio.cmr.request.model.requestentry.FindCMRRecordModel;
import com.ibm.cio.cmr.request.model.requestentry.FindCMRResultModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.util.CompanyFinder;
import com.ibm.cio.cmr.request.util.RequestUtils;
import com.ibm.cio.cmr.request.util.SystemUtil;
import com.ibm.cio.cmr.request.util.geo.GEOHandler;
import com.ibm.cmr.services.client.CmrServicesFactory;
import com.ibm.cmr.services.client.MatchingServiceClient;
import com.ibm.cmr.services.client.QueryClient;
import com.ibm.cmr.services.client.automation.AutomationResponse;
import com.ibm.cmr.services.client.automation.cn.CNResponse;
import com.ibm.cmr.services.client.matching.MatchingResponse;
import com.ibm.cmr.services.client.matching.cmr.DuplicateCMRCheckRequest;
import com.ibm.cmr.services.client.matching.cmr.DuplicateCMRCheckResponse;
import com.ibm.cmr.services.client.query.QueryRequest;
import com.ibm.cmr.services.client.query.QueryResponse;

public class CNDupCMRCheckElement extends DuplicateCheckElement {

  private static final Logger log = Logger.getLogger(CNDupCMRCheckElement.class);

  public CNDupCMRCheckElement(String requestTypes, String actionOnError, boolean overrideData, boolean stopOnError) {
    super(requestTypes, actionOnError, overrideData, stopOnError);
  }

  @Override
  public boolean importMatch(EntityManager entityManager, RequestData requestData, AutomationMatching match) {

    return false;
  }

  @Override
  public AutomationResult<MatchingOutput> executeElement(EntityManager entityManager, RequestData requestData, AutomationEngineData engineData)
      throws Exception {
    Addr soldTo = requestData.getAddress("ZS01");
    Admin admin = requestData.getAdmin();
    Data data = requestData.getData();

    AutomationResult<MatchingOutput> result = buildResult(admin.getId().getReqId());

    MatchingOutput output = new MatchingOutput();

    if (StringUtils.isNotBlank(admin.getDupCmrReason())) {
      StringBuilder details = new StringBuilder();
      details.append("User requested to proceed with Duplicate CMR Creation.").append("\n\n");
      details.append("Reason provided - ").append("\n");
      details.append(admin.getDupCmrReason()).append("\n");
      result.setDetails(details.toString());
      result.setResults("Overridden");
      result.setOnError(false);
    } else if (soldTo != null) {

      String cnName = null;
      String cnHistoryName = null;
      String cnAddr = null;
      String cnCreditCd = data.getBusnType();
      String cnCeid = data.getPpsceid();

      IntlAddr iAddr = new IntlAddr();
      CompanyRecordModel searchModelCNAPI = new CompanyRecordModel();

      AutomationResponse<CNResponse> resultCNApi = null;
      List<CompanyRecordModel> resultFindCmrCN = null;
      FindCMRResultModel findCMRResult = null;
      GEOHandler handler = RequestUtils.getGEOHandler(data.getCmrIssuingCntry());
      CompanyRecordModel cmrData = new CompanyRecordModel(); // nonLatin result
      MatchingResponse<DuplicateCMRCheckResponse> response = null; // Latin
                                                                   // result
      boolean nameMatched = false;
      boolean nameIsBP = false;
      boolean historyNmMatched = false;
      boolean historyNmIsBP = false;
      boolean ceidMatched = false;
      String nameFindCmrCnResult = null;
      String addrFindCmrCnResult = null;
      String ceidFindCmrCnResult = null;
      List<String> matchedCMRs = new ArrayList<>();
      StringBuilder details = new StringBuilder();

      if (data.getCustSubGrp() != null) {
        switch (data.getCustSubGrp()) {
        case "NRML": // SCENARIO_LOCAL_NRML

          // logic:
          // a) if the return result has the same Chinese name, then it is a
          // duplicate request
          // b) if the findCMR result has ceid, then it is not a duplicate
          // request

          nameIsBP = true;
          historyNmIsBP = true;

          iAddr = handler.getIntlAddrById(soldTo, entityManager);
          if (iAddr != null) {

            try {

              // 1, Check CN API
              if (cnCreditCd != null && cnCreditCd.length() > 0) {
                searchModelCNAPI.setIssuingCntry(data.getCmrIssuingCntry());
                searchModelCNAPI.setCountryCd(soldTo.getLandCntry());
                searchModelCNAPI.setTaxCd1(cnCreditCd);
                resultCNApi = CompanyFinder.getCNApiInfo(searchModelCNAPI, "TAXCD");
              } else {
                cnName = iAddr.getIntlCustNm1() + (iAddr.getIntlCustNm2() != null ? " " + iAddr.getIntlCustNm2() : "");
                searchModelCNAPI.setIssuingCntry(data.getCmrIssuingCntry());
                searchModelCNAPI.setCountryCd(soldTo.getLandCntry());
                searchModelCNAPI.setAltName(cnName);
                resultCNApi = CompanyFinder.getCNApiInfo(searchModelCNAPI, "ALTNAME");
              }

              if (resultCNApi != null && resultCNApi.isSuccess()) {
                cnName = resultCNApi.getRecord().getName();
                cnHistoryName = resultCNApi.getRecord().getHistoryNames();
                cnAddr = resultCNApi.getRecord().getRegLocation();

                try {
                  // 2, Check FindCMR NON Latin with Chinese name
                  CompanyRecordModel searchModelFindCmrCN = new CompanyRecordModel();
                  searchModelFindCmrCN.setIssuingCntry(data.getCmrIssuingCntry());
                  searchModelFindCmrCN.setCountryCd(soldTo.getLandCntry());
                  searchModelFindCmrCN.setName(cnName);
                  resultFindCmrCN = CompanyFinder.findCompanies(searchModelFindCmrCN);

                  if (!resultFindCmrCN.isEmpty() && resultFindCmrCN.size() > 0) {
                    for (int i = 0; i < resultFindCmrCN.size(); i++) {
                      nameFindCmrCnResult = resultFindCmrCN.get(i).getAltName() != null ? resultFindCmrCN.get(i).getAltName() : "";
                      if (nameFindCmrCnResult != null && cnName.equals(nameFindCmrCnResult)) {

                        nameMatched = true;

                        if (resultFindCmrCN.get(i).getCied() == null || StringUtils.isBlank(resultFindCmrCN.get(i).getCied())) {
                          nameIsBP = false;
                        }

                        if (nameMatched && !nameIsBP) {
                          matchedCMRs.add(resultFindCmrCN.get(i).getCmrNo());
                          cmrData = resultFindCmrCN.get(i);
                          details.append("\n");
                          logDuplicateCMR(details, cmrData);
                        }
                      }
                    }
                  }
                } catch (Exception e) {
                  e.printStackTrace();
                  result.setDetails("Error on checking findCMR on Duplicate CMR Check of Chinese, name: " + cnName);
                  engineData.addRejectionComment("OTH", "Error on checking findCMR on Duplicate CMR Check of Chinese, name: " + cnName, "", "");
                  result.setOnError(true);
                  result.setResults("Error on checking findCMR on Duplicate CMR Check of Chinese, name: " + cnName);
                }

                // 3, Check FindCMR Non Latin with historical Chinese name
                if (cnHistoryName != null && !"".equals(cnHistoryName)) {
                  List<String> cnHistoryNameList = Arrays.asList(cnHistoryName.split(";"));
                  CompanyRecordModel searchModelFindCmrCN = new CompanyRecordModel();
                  searchModelFindCmrCN.setIssuingCntry(data.getCmrIssuingCntry());
                  try {
                    for (String historyName : cnHistoryNameList) {
                      searchModelFindCmrCN.setName(historyName);
                      resultFindCmrCN = CompanyFinder.findCompanies(searchModelFindCmrCN);
                      if (!resultFindCmrCN.isEmpty() && resultFindCmrCN.size() > 0) {
                        for (int i = 0; i < resultFindCmrCN.size(); i++) {
                          nameFindCmrCnResult = resultFindCmrCN.get(i).getAltName() != null ? resultFindCmrCN.get(i).getAltName() : "";
                          if (nameFindCmrCnResult != null && nameFindCmrCnResult.equals(historyName)) {

                            historyNmMatched = true;

                            if (resultFindCmrCN.get(i).getCied() == null || StringUtils.isBlank(resultFindCmrCN.get(i).getCied())) {
                              historyNmIsBP = false;
                            }

                            if (historyNmMatched && !historyNmIsBP) {
                              matchedCMRs.add(resultFindCmrCN.get(i).getCmrNo());
                              cmrData = resultFindCmrCN.get(i);
                              details.append("\n");
                              logDuplicateCMR(details, cmrData);
                            }
                          }
                        }
                      }
                    }

                  } catch (Exception e) {
                    e.printStackTrace();
                    result.setDetails("Error on getting findCMR data when Duplicate CMR Check of historical Chinese, name: " + cnHistoryName);
                    engineData.addRejectionComment("OTH",
                        "Error on getting findCMR data when Duplicate CMR Check of historical Chinese, name: " + cnHistoryName, "", "");
                    result.setOnError(true);
                    result.setResults("Error on getting findCMR data when Duplicate CMR Check of historical Chinese, name: " + cnHistoryName);
                  }

                }

                // output
                if ((nameMatched && !nameIsBP) || (historyNmMatched && !historyNmIsBP)) {
                  result.setResults("Matches Found");
                  result.setResults("Found Duplicate CMRs.");
                  engineData.addRejectionComment("DUPC", "Customer already exists / duplicate CMR", StringUtils.join(matchedCMRs, ", "), "");
                  // to allow overides later
                  requestData.getAdmin().setMatchIndc("C");
                  result.setOnError(true);
                  result.setProcessOutput(output);
                  result.setDetails(details.toString().trim());
                } else if ((nameMatched && nameIsBP) || (historyNmMatched && historyNmIsBP)) {
                  result.setDetails("Duplicate CMR Check returned BP record for Normal Scenario." + StringUtils.join(matchedCMRs, ", "));
                  result.setResults("No Matches");
                  result.setOnError(false);
                } else if (!nameMatched && !historyNmMatched) {
                  result.setDetails("No Duplicate CMRs were found.");
                  result.setResults("No Matches");
                  result.setOnError(false);
                }
              } else if (resultCNApi != null && !resultCNApi.isSuccess()) {
                result.setDetails("Error on getting China API Data when Duplicate CMR Check of Chinese - " + resultCNApi.getMessage());
                engineData.addRejectionComment("OTH",
                    "Error on getting China API Data when Duplicate CMR Check of Chinese - " + resultCNApi.getMessage(), "", "");
                result.setOnError(true);
                result.setResults("Error on getting China API Data when Duplicate CMR Check of Chinese - " + resultCNApi.getMessage());
              }

            } catch (Exception e) {
              e.printStackTrace();
              result.setDetails("Error on getting China API Data when Duplicate CMR Check of Chinese.");
              engineData.addRejectionComment("OTH", "Error on getting China API Data when Duplicate CMR Check of Chinese.", "", "");
              result.setOnError(true);
              result.setResults("Error on getting China API Data when Duplicate CMR Check of Chinese.");
            }
          } else {
            result.setDetails("Error on getting Chinese name when Duplicate CMR Check.");
            engineData.addRejectionComment("OTH", "Error on getting Chinese name when Duplicate CMR Check.", "", "");
            result.setOnError(true);
            result.setResults("Error on getting Chinese name when Duplicate CMR Check.");
          }

          break;

        case "BUSPR": // SCENARIO_LOCAL_BUSPR

          // logic:
          // a) if the return result has the same Chinese name, or CEID, then it
          // is a duplicate request
          // b) if the findCMR result has not ceid, then it is not a duplicate
          // request

          nameIsBP = false;
          historyNmIsBP = false;

          iAddr = handler.getIntlAddrById(soldTo, entityManager);
          if (iAddr != null) {

            try {

              // 1, Check CN API
              if (cnCreditCd != null && cnCreditCd.length() > 0) {
                searchModelCNAPI.setIssuingCntry(data.getCmrIssuingCntry());
                searchModelCNAPI.setCountryCd(soldTo.getLandCntry());
                searchModelCNAPI.setTaxCd1(cnCreditCd);
                resultCNApi = CompanyFinder.getCNApiInfo(searchModelCNAPI, "TAXCD");
              } else {
                cnName = iAddr.getIntlCustNm1() + (iAddr.getIntlCustNm2() != null ? " " + iAddr.getIntlCustNm2() : "");
                searchModelCNAPI.setIssuingCntry(data.getCmrIssuingCntry());
                searchModelCNAPI.setCountryCd(soldTo.getLandCntry());
                searchModelCNAPI.setAltName(cnName);
                resultCNApi = CompanyFinder.getCNApiInfo(searchModelCNAPI, "ALTNAME");
              }

              if (resultCNApi != null && resultCNApi.isSuccess()) {
                cnName = resultCNApi.getRecord().getName();
                cnHistoryName = resultCNApi.getRecord().getHistoryNames();
                cnAddr = resultCNApi.getRecord().getRegLocation();

                try {
                  // 2, Check FindCMR NON Latin with Chinese name
                  CompanyRecordModel searchModelFindCmrCN = new CompanyRecordModel();
                  searchModelFindCmrCN.setIssuingCntry(data.getCmrIssuingCntry());
                  searchModelFindCmrCN.setCountryCd(soldTo.getLandCntry());
                  searchModelFindCmrCN.setName(cnName);
                  resultFindCmrCN = CompanyFinder.findCompanies(searchModelFindCmrCN);
                  if (!resultFindCmrCN.isEmpty() && resultFindCmrCN.size() > 0) {
                    for (int i = 0; i < resultFindCmrCN.size(); i++) {
                      nameFindCmrCnResult = resultFindCmrCN.get(i).getAltName() != null ? resultFindCmrCN.get(i).getAltName() : "";
                      if (nameFindCmrCnResult != null && cnName.equals(nameFindCmrCnResult)) {

                        nameMatched = true;

                        if (resultFindCmrCN.get(i).getCied() != null && resultFindCmrCN.get(i).getCied().length() > 0) {
                          nameIsBP = true;
                        }

                        if (nameMatched && nameIsBP) {
                          matchedCMRs.add(resultFindCmrCN.get(i).getCmrNo());
                          cmrData = resultFindCmrCN.get(i);
                          details.append("\n");
                          logDuplicateCMR(details, cmrData);
                        }
                      }
                    }
                  }
                } catch (Exception e) {
                  e.printStackTrace();
                  result.setDetails("Error on checking findCMR on Duplicate CMR Check of Chinese, name: " + cnName);
                  engineData.addRejectionComment("OTH", "Error on checking findCMR on Duplicate CMR Check of Chinese, name: " + cnName, "", "");
                  result.setOnError(true);
                  result.setResults("Error on checking findCMR on Duplicate CMR Check of Chinese, name: " + cnName);
                }

                // 3, Check FindCMR Non Latin with historical Chinese name
                if (cnHistoryName != null && !"".equals(cnHistoryName)) {
                  List<String> cnHistoryNameList = Arrays.asList(cnHistoryName.split(";"));
                  try {
                    CompanyRecordModel searchModelFindCmrCN = new CompanyRecordModel();
                    searchModelFindCmrCN.setIssuingCntry(data.getCmrIssuingCntry());
                    for (String historyName : cnHistoryNameList) {
                      searchModelFindCmrCN.setName(historyName);
                      resultFindCmrCN = CompanyFinder.findCompanies(searchModelFindCmrCN);
                      if (!resultFindCmrCN.isEmpty() && resultFindCmrCN.size() > 0) {
                        for (int i = 0; i < resultFindCmrCN.size(); i++) {
                          nameFindCmrCnResult = resultFindCmrCN.get(i).getAltName() != null ? resultFindCmrCN.get(i).getAltName() : "";
                          if (nameFindCmrCnResult != null && nameFindCmrCnResult.equals(historyName)) {

                            historyNmMatched = true;

                            if (resultFindCmrCN.get(i).getCied() != null && resultFindCmrCN.get(i).getCied().length() > 0) {
                              historyNmIsBP = true;
                            }

                            if (historyNmMatched && historyNmIsBP) {
                              matchedCMRs.add(resultFindCmrCN.get(i).getCmrNo());
                              cmrData = resultFindCmrCN.get(i);
                              details.append("\n");
                              logDuplicateCMR(details, cmrData);
                            }
                          }
                        }
                      }
                    }

                  } catch (Exception e) {
                    e.printStackTrace();
                    result.setDetails("Error on getting findCMR data when Duplicate CMR Check of historical Chinese, name: " + cnHistoryName);
                    engineData.addRejectionComment("OTH",
                        "Error on getting findCMR data when Duplicate CMR Check of historical Chinese, name: " + cnHistoryName, "", "");
                    result.setOnError(true);
                    result.setResults("Error on getting findCMR data when Duplicate CMR Check of historical Chinese, name: " + cnHistoryName);
                  }

                }

                if (cnCeid != null && !"".equals(cnCeid)) {
                  try {
                    // 4, Check FindCMR with CEID
                    CompanyRecordModel searchModelFindCmrCN = new CompanyRecordModel();
                    searchModelFindCmrCN.setIssuingCntry(data.getCmrIssuingCntry());
                    searchModelFindCmrCN.setCied(cnCeid);
                    // resultFindCmrCN =
                    // CompanyFinder.findCompanies(searchModelFindCmrCN);
                    findCMRResult = searchFindCMR(searchModelFindCmrCN);
                    if (findCMRResult != null && findCMRResult.getItems() != null && !findCMRResult.getItems().isEmpty()) {
                      ceidMatched = true;
                      List<FindCMRRecordModel> cmrs = findCMRResult.getItems();
                      for (FindCMRRecordModel cmrsMods : cmrs) {
                        cmrData.setCmrNo(cmrsMods.getCmrNum());
                        cmrData.setName(cmrsMods.getCmrName1Plain() + (cmrsMods.getCmrName2Plain() != null ? cmrsMods.getCmrName2Plain() : ""));
                        cmrData.setIssuingCntry(cmrsMods.getCmrIssuedBy());
                        cmrData.setCied(cmrsMods.getCmrPpsceid());
                        cmrData.setCountryCd(cmrsMods.getCmrCountryLanded());
                        cmrData.setStreetAddress1(cmrsMods.getCmrStreetAddress());
                        cmrData.setStreetAddress2(cmrsMods.getCmrStreetAddressCont());
                        cmrData.setPostCd(cmrsMods.getCmrPostalCode());
                        cmrData.setCity(cmrsMods.getCmrCity());

                        matchedCMRs.add(cmrsMods.getCmrNum());
                        details.append("\n");
                        logDuplicateCMR(details, cmrData);
                      }

                    }
                  } catch (Exception e) {
                    e.printStackTrace();
                    result.setDetails("Error on checking findCMR on Duplicate CMR Check of Chinese, ceid: " + cnCeid);
                    engineData.addRejectionComment("OTH", "Error on checking findCMR on Duplicate CMR Check of Chinese, ceid: " + cnCeid, "", "");
                    result.setOnError(true);
                    result.setResults("Error on checking findCMR on Duplicate CMR Check of Chinese, ceid: " + cnCeid);
                  }
                }

                // output
                if ((nameMatched && nameIsBP) || (historyNmMatched && historyNmIsBP) || ceidMatched) {
                  result.setResults("Matches Found");
                  result.setResults("Found Duplicate CMRs.");
                  engineData.addRejectionComment("DUPC", "Customer already exists / duplicate CMR", StringUtils.join(matchedCMRs, ", "), "");
                  // to allow overides later
                  requestData.getAdmin().setMatchIndc("C");
                  result.setOnError(true);
                  result.setProcessOutput(output);
                  result.setDetails(details.toString().trim());
                } else if ((nameMatched && !nameIsBP) || (historyNmMatched && !historyNmIsBP) && !ceidMatched) {
                  result.setDetails("Duplicate CMR Check returned record without ceid for BP Scenario." + StringUtils.join(matchedCMRs, ", "));
                  result.setResults("No Matches");
                  result.setOnError(false);
                } else if (!nameMatched && !historyNmMatched && !ceidMatched) {
                  result.setDetails("No Duplicate CMRs were found.");
                  result.setResults("No Matches");
                  result.setOnError(false);
                }
              } else if (resultCNApi != null && !resultCNApi.isSuccess()) {
                result.setDetails("Error on getting China API Data when Duplicate CMR Check of Chinese - " + resultCNApi.getMessage());
                engineData.addRejectionComment("OTH",
                    "Error on getting China API Data when Duplicate CMR Check of Chinese - " + resultCNApi.getMessage(), "", "");
                result.setOnError(true);
                result.setResults("Error on getting China API Data when Duplicate CMR Check of Chinese - " + resultCNApi.getMessage());
              }

            } catch (Exception e) {
              e.printStackTrace();
              result.setDetails("Error on getting China API Data when Duplicate CMR Check of Chinese.");
              engineData.addRejectionComment("OTH", "Error on getting China API Data when Duplicate CMR Check of Chinese.", "", "");
              result.setOnError(true);
              result.setResults("Error on getting China API Data when Duplicate CMR Check of Chinese.");
            }
          } else {
            result.setDetails("Error on getting Chinese name when Duplicate CMR Check.");
            engineData.addRejectionComment("OTH", "Error on getting Chinese name when Duplicate CMR Check.", "", "");
            result.setOnError(true);
            result.setResults("Error on getting Chinese name when Duplicate CMR Check.");
          }
          break;

        case "EMBSA": // SCENARIO_LOCAL_EMBSA

          // logic:
          // a) if the return result has same Chinese name, then it is a
          // duplicate request
          // b) fastpass - TBD

          iAddr = handler.getIntlAddrById(soldTo, entityManager);
          // searchModelFindCMR.setCmrNo(cmrNo);
          if (iAddr != null) {

            try {

              // 1, Check CN API
              if (cnCreditCd != null && cnCreditCd.length() > 0) {
                searchModelCNAPI.setIssuingCntry(data.getCmrIssuingCntry());
                searchModelCNAPI.setCountryCd(soldTo.getLandCntry());
                searchModelCNAPI.setTaxCd1(cnCreditCd);
                resultCNApi = CompanyFinder.getCNApiInfo(searchModelCNAPI, "TAXCD");
              } else {
                cnName = iAddr.getIntlCustNm1() + (iAddr.getIntlCustNm2() != null ? " " + iAddr.getIntlCustNm2() : "");
                searchModelCNAPI.setIssuingCntry(data.getCmrIssuingCntry());
                searchModelCNAPI.setCountryCd(soldTo.getLandCntry());
                searchModelCNAPI.setAltName(cnName);
                resultCNApi = CompanyFinder.getCNApiInfo(searchModelCNAPI, "ALTNAME");
              }

              if (resultCNApi != null && resultCNApi.isSuccess()) {
                cnName = resultCNApi.getRecord().getName();
                cnHistoryName = resultCNApi.getRecord().getHistoryNames();
                cnAddr = resultCNApi.getRecord().getRegLocation();

                try {
                  // 2, Check FindCMR NON Latin with Chinese name
                  CompanyRecordModel searchModelFindCmrCN = new CompanyRecordModel();
                  searchModelFindCmrCN.setIssuingCntry(data.getCmrIssuingCntry());
                  searchModelFindCmrCN.setName(cnName);
                  resultFindCmrCN = CompanyFinder.findCompanies(searchModelFindCmrCN);
                  if (!resultFindCmrCN.isEmpty() && resultFindCmrCN.size() > 0) {
                    for (int i = 0; i < resultFindCmrCN.size(); i++) {
                      nameFindCmrCnResult = resultFindCmrCN.get(i).getAltName() != null ? resultFindCmrCN.get(i).getAltName() : "";
                      if (nameFindCmrCnResult != null && cnName.equals(nameFindCmrCnResult)) {

                        nameMatched = true;
                        matchedCMRs.add(resultFindCmrCN.get(i).getCmrNo());

                        if (nameMatched) {
                          cmrData = resultFindCmrCN.get(i);
                          details.append("\n");
                          logDuplicateCMR(details, cmrData);
                        }
                      }
                    }
                  }
                } catch (Exception e) {
                  e.printStackTrace();
                  result.setDetails("Error on checking findCMR on Duplicate CMR Check of Chinese, name: " + cnName);
                  engineData.addRejectionComment("OTH", "Error on checking findCMR on Duplicate CMR Check of Chinese, name: " + cnName, "", "");
                  result.setOnError(true);
                  result.setResults("Error on checking findCMR on Duplicate CMR Check of Chinese, name: " + cnName);
                }

                // 3, Check FindCMR Non Latin with historical Chinese name
                if (cnHistoryName != null && !"".equals(cnHistoryName)) {
                  List<String> cnHistoryNameList = Arrays.asList(cnHistoryName.split(";"));
                  try {
                    CompanyRecordModel searchModelFindCmrCN = new CompanyRecordModel();
                    searchModelFindCmrCN.setIssuingCntry(data.getCmrIssuingCntry());
                    for (String historyName : cnHistoryNameList) {
                      searchModelFindCmrCN.setName(historyName);
                      resultFindCmrCN = CompanyFinder.findCompanies(searchModelFindCmrCN);
                      if (!resultFindCmrCN.isEmpty() && resultFindCmrCN.size() > 0) {
                        for (int i = 0; i < resultFindCmrCN.size(); i++) {
                          nameFindCmrCnResult = resultFindCmrCN.get(i).getAltName() != null ? resultFindCmrCN.get(i).getAltName() : "";
                          if (nameFindCmrCnResult != null && nameFindCmrCnResult.equals(historyName)) {

                            historyNmMatched = true;
                            matchedCMRs.add(resultFindCmrCN.get(i).getCmrNo());

                            if (historyNmMatched) {
                              cmrData = resultFindCmrCN.get(i);
                              details.append("\n");
                              logDuplicateCMR(details, cmrData);
                            }
                          }
                        }
                      }
                    }

                  } catch (Exception e) {
                    e.printStackTrace();
                    result.setDetails("Error on getting findCMR data when Duplicate CMR Check of historical Chinese, name: " + cnHistoryName);
                    engineData.addRejectionComment("OTH",
                        "Error on getting findCMR data when Duplicate CMR Check of historical Chinese, name: " + cnHistoryName, "", "");
                    result.setOnError(true);
                    result.setResults("Error on getting findCMR data when Duplicate CMR Check of historical Chinese, name: " + cnHistoryName);
                  }
                }
                // output
                if (nameMatched || historyNmMatched) {
                  result.setResults("Matches Found");
                  result.setResults("Found Duplicate CMRs.");
                  engineData.addRejectionComment("DUPC", "Customer already exists / duplicate CMR", StringUtils.join(matchedCMRs, ", "), "");
                  // to allow overides later
                  requestData.getAdmin().setMatchIndc("C");
                  result.setOnError(true);
                  result.setProcessOutput(output);
                  result.setDetails(details.toString().trim());
                } else if (!nameMatched && !historyNmMatched) {
                  result.setDetails("No Duplicate CMRs were found.");
                  result.setResults("No Matches");
                  result.setOnError(false);
                }
              } else if (resultCNApi != null && !resultCNApi.isSuccess()) {
                result.setDetails("Error on getting China API Data when Duplicate CMR Check of Chinese - " + resultCNApi.getMessage());
                engineData.addRejectionComment("OTH",
                    "Error on getting China API Data when Duplicate CMR Check of Chinese - " + resultCNApi.getMessage(), "", "");
                result.setOnError(true);
                result.setResults("Error on getting China API Data when Duplicate CMR Check of Chinese - " + resultCNApi.getMessage());
              }

            } catch (Exception e) {
              e.printStackTrace();
              result.setDetails("Error on getting China API Data when Duplicate CMR Check of Chinese.");
              engineData.addRejectionComment("OTH", "Error on getting China API Data when Duplicate CMR Check of Chinese.", "", "");
              result.setOnError(true);
              result.setResults("Error on getting China API Data when Duplicate CMR Check of Chinese.");
            }
          } else {
            result.setDetails("Error on getting Chinese name when Duplicate CMR Check.");
            engineData.addRejectionComment("OTH", "Error on getting Chinese name when Duplicate CMR Check.", "", "");
            result.setOnError(true);
            result.setResults("Error on getting Chinese name when Duplicate CMR Check.");
          }
          break;

        case "AQSTN": // SCENARIO_LOCAL_AQSTN

          // logic:
          // a) if the return result has same Chinese name, then it is a
          // duplicate request.
          // b) if no Chinese information, then rely on English Name

          boolean ifAQSTNHasCN = true;
          iAddr = handler.getIntlAddrById(soldTo, entityManager);
          if (iAddr != null) {
            // cnName = iAddr.getIntlCustNm1() + (iAddr.getIntlCustNm2() != null
            // ? iAddr.getIntlCustNm2() : "");
            if (iAddr.getIntlCustNm2() != null) {
              cnName = iAddr.getIntlCustNm1() + iAddr.getIntlCustNm2();
            } else {
              cnName = iAddr.getIntlCustNm1();
            }
          }
          if (!StringUtils.isBlank(cnName)) {

            try {

              // 1, Check CN API
              if (cnCreditCd != null && cnCreditCd.length() > 0) {
                searchModelCNAPI.setIssuingCntry(data.getCmrIssuingCntry());
                searchModelCNAPI.setCountryCd(soldTo.getLandCntry());
                searchModelCNAPI.setTaxCd1(cnCreditCd);
                resultCNApi = CompanyFinder.getCNApiInfo(searchModelCNAPI, "TAXCD");
              } else {
                cnName = iAddr.getIntlCustNm1() + (iAddr.getIntlCustNm2() != null ? " " + iAddr.getIntlCustNm2() : "");
                searchModelCNAPI.setIssuingCntry(data.getCmrIssuingCntry());
                searchModelCNAPI.setCountryCd(soldTo.getLandCntry());
                searchModelCNAPI.setAltName(cnName);
                resultCNApi = CompanyFinder.getCNApiInfo(searchModelCNAPI, "ALTNAME");
              }

              if (resultCNApi != null && resultCNApi.isSuccess()) {
                cnName = resultCNApi.getRecord().getName();
                cnHistoryName = resultCNApi.getRecord().getHistoryNames();
                cnAddr = resultCNApi.getRecord().getRegLocation();

                try {
                  // 2, Check FindCMR NON Latin with Chinese name
                  CompanyRecordModel searchModelFindCmrCN = new CompanyRecordModel();
                  searchModelFindCmrCN.setIssuingCntry(data.getCmrIssuingCntry());
                  searchModelFindCmrCN.setName(cnName);
                  resultFindCmrCN = CompanyFinder.findCompanies(searchModelFindCmrCN);
                  if (!resultFindCmrCN.isEmpty() && resultFindCmrCN.size() > 0) {
                    for (int i = 0; i < resultFindCmrCN.size(); i++) {
                      nameFindCmrCnResult = resultFindCmrCN.get(i).getAltName() != null ? resultFindCmrCN.get(i).getAltName() : "";
                      if (nameFindCmrCnResult != null && cnName.equals(nameFindCmrCnResult)) {

                        nameMatched = true;
                        matchedCMRs.add(resultFindCmrCN.get(i).getCmrNo());

                        if (nameMatched) {
                          cmrData = resultFindCmrCN.get(i);
                          details.append("\n");
                          logDuplicateCMR(details, cmrData);
                        }
                      }
                    }
                  }
                } catch (Exception e) {
                  e.printStackTrace();
                  result.setDetails("Error on checking findCMR on Duplicate CMR Check of Chinese, name: " + cnName);
                  engineData.addRejectionComment("OTH", "Error on checking findCMR on Duplicate CMR Check of Chinese, name: " + cnName, "", "");
                  result.setOnError(true);
                  result.setResults("Error on checking findCMR on Duplicate CMR Check of Chinese, name: " + cnName);
                }

                // 3, Check FindCMR Non Latin with historical Chinese name
                if (cnHistoryName != null && !"".equals(cnHistoryName)) {
                  List<String> cnHistoryNameList = Arrays.asList(cnHistoryName.split(";"));
                  try {
                    CompanyRecordModel searchModelFindCmrCN = new CompanyRecordModel();
                    searchModelFindCmrCN.setIssuingCntry(data.getCmrIssuingCntry());
                    for (String historyName : cnHistoryNameList) {
                      searchModelFindCmrCN.setName(historyName);
                      resultFindCmrCN = CompanyFinder.findCompanies(searchModelFindCmrCN);
                      if (!resultFindCmrCN.isEmpty() && resultFindCmrCN.size() > 0) {
                        for (int i = 0; i < resultFindCmrCN.size(); i++) {
                          nameFindCmrCnResult = resultFindCmrCN.get(i).getAltName() != null ? resultFindCmrCN.get(i).getAltName() : "";
                          if (nameFindCmrCnResult != null && nameFindCmrCnResult.equals(historyName)) {

                            historyNmMatched = true;
                            matchedCMRs.add(resultFindCmrCN.get(i).getCmrNo());

                            if (historyNmMatched) {
                              cmrData = resultFindCmrCN.get(i);
                              details.append("\n");
                              logDuplicateCMR(details, cmrData);
                            }
                          }
                        }
                      }
                    }

                  } catch (Exception e) {
                    e.printStackTrace();
                    result.setDetails("Error on getting findCMR data when Duplicate CMR Check of historical Chinese, name: " + cnHistoryName);
                    engineData.addRejectionComment("OTH",
                        "Error on getting findCMR data when Duplicate CMR Check of historical Chinese, name: " + cnHistoryName, "", "");
                    result.setOnError(true);
                    result.setResults("Error on getting findCMR data when Duplicate CMR Check of historical Chinese, name: " + cnHistoryName);
                  }
                }
                // output
                if (nameMatched || historyNmMatched) {
                  result.setResults("Matches Found");
                  result.setResults("Found Duplicate CMRs.");
                  engineData.addRejectionComment("DUPC", "Customer already exists / duplicate CMR", StringUtils.join(matchedCMRs, ", "), "");
                  // to allow overides later
                  requestData.getAdmin().setMatchIndc("C");
                  result.setOnError(true);
                  result.setProcessOutput(output);
                  result.setDetails(details.toString().trim());
                } else if (!nameMatched && !historyNmMatched) {
                  result.setDetails("No Duplicate CMRs were found.");
                  result.setResults("No Matches");
                  result.setOnError(false);
                }
              } else if (resultCNApi != null && !resultCNApi.isSuccess()) {
                result.setDetails("Error on getting China API Data when Duplicate CMR Check of Chinese - " + resultCNApi.getMessage());
                engineData.addRejectionComment("OTH",
                    "Error on getting China API Data when Duplicate CMR Check of Chinese - " + resultCNApi.getMessage(), "", "");
                result.setOnError(true);
                result.setResults("Error on getting China API Data when Duplicate CMR Check of Chinese - " + resultCNApi.getMessage());
              }

            } catch (Exception e) {
              e.printStackTrace();
              result.setDetails("Error on getting China API Data when Duplicate CMR Check of Chinese.");
              engineData.addRejectionComment("OTH", "Error on getting China API Data when Duplicate CMR Check of Chinese.", "", "");
              result.setOnError(true);
              result.setResults("Error on getting China API Data when Duplicate CMR Check of Chinese.");
            }

          } else {

            // check with English name and get matched result, then it is dup
            // req
            ifAQSTNHasCN = false;

            CompanyRecordModel searchModelFindCmr = new CompanyRecordModel();
            searchModelFindCmr.setIssuingCntry(data.getCmrIssuingCntry());
            searchModelFindCmr.setName(soldTo.getCustNm1() + (soldTo.getCustNm2() != null ? soldTo.getCustNm2() : ""));
            findCMRResult = searchFindCMR(searchModelFindCmr);
            if (findCMRResult != null && findCMRResult.getItems() != null && !findCMRResult.getItems().isEmpty()) {
              ceidMatched = true;
              List<FindCMRRecordModel> cmrs = findCMRResult.getItems();
              for (FindCMRRecordModel cmrsMods : cmrs) {
                cmrData.setCmrNo(cmrsMods.getCmrNum());
                cmrData.setName(cmrsMods.getCmrName1Plain() + (cmrsMods.getCmrName2Plain() != null ? cmrsMods.getCmrName2Plain() : ""));
                cmrData.setIssuingCntry(cmrsMods.getCmrIssuedBy());
                cmrData.setCied(cmrsMods.getCmrPpsceid());
                cmrData.setCountryCd(cmrsMods.getCmrCountryLanded());
                cmrData.setStreetAddress1(cmrsMods.getCmrStreetAddress());
                cmrData.setStreetAddress2(cmrsMods.getCmrStreetAddressCont());
                cmrData.setPostCd(cmrsMods.getCmrPostalCode());
                cmrData.setCity(cmrsMods.getCmrCity());

                matchedCMRs.add(cmrsMods.getCmrNum());
                details.append("\n");
                logDuplicateCMR(details, cmrData);
              }

              result.setResults("Matches Found");
              result.setResults("Found Duplicate CMRs.");
              engineData.addRejectionComment("DUPC", "Customer already exists / duplicate CMR", StringUtils.join(matchedCMRs, ", "), "");
              // to allow overides later
              requestData.getAdmin().setMatchIndc("C");
              result.setOnError(true);
              result.setProcessOutput(output);
              result.setDetails(details.toString().trim());
            } else {
              result.setDetails("No Duplicate CMRs were found.");
              result.setResults("No Matches");
              result.setOnError(false);
            }

          }
          break;
        case "BLUMX": // SCENARIO_LOCAL_BLUMX
        case "MRKT": // SCENARIO_LOCAL_MRKT

          // logic:
          // a) if the return result has same Chinese name, then it is a
          // duplicate request

          iAddr = handler.getIntlAddrById(soldTo, entityManager);
          if (iAddr != null) {

            try {

              // 1, Check CN API
              if (cnCreditCd != null && cnCreditCd.length() > 0) {
                searchModelCNAPI.setIssuingCntry(data.getCmrIssuingCntry());
                searchModelCNAPI.setCountryCd(soldTo.getLandCntry());
                searchModelCNAPI.setTaxCd1(cnCreditCd);
                resultCNApi = CompanyFinder.getCNApiInfo(searchModelCNAPI, "TAXCD");
              } else {
                cnName = iAddr.getIntlCustNm1() + (iAddr.getIntlCustNm2() != null ? " " + iAddr.getIntlCustNm2() : "");
                searchModelCNAPI.setIssuingCntry(data.getCmrIssuingCntry());
                searchModelCNAPI.setCountryCd(soldTo.getLandCntry());
                searchModelCNAPI.setAltName(cnName);
                resultCNApi = CompanyFinder.getCNApiInfo(searchModelCNAPI, "ALTNAME");
              }

              if (resultCNApi != null && resultCNApi.isSuccess()) {
                cnName = resultCNApi.getRecord().getName();
                cnHistoryName = resultCNApi.getRecord().getHistoryNames();
                cnAddr = resultCNApi.getRecord().getRegLocation();

                try {
                  // 2, Check FindCMR NON Latin with Chinese name
                  CompanyRecordModel searchModelFindCmrCN = new CompanyRecordModel();
                  searchModelFindCmrCN.setIssuingCntry(data.getCmrIssuingCntry());
                  searchModelFindCmrCN.setName(cnName);
                  resultFindCmrCN = CompanyFinder.findCompanies(searchModelFindCmrCN);
                  if (!resultFindCmrCN.isEmpty() && resultFindCmrCN.size() > 0) {
                    for (int i = 0; i < resultFindCmrCN.size(); i++) {
                      nameFindCmrCnResult = resultFindCmrCN.get(i).getAltName() != null ? resultFindCmrCN.get(i).getAltName() : "";
                      if (nameFindCmrCnResult != null && cnName.equals(nameFindCmrCnResult)) {

                        nameMatched = true;
                        matchedCMRs.add(resultFindCmrCN.get(i).getCmrNo());

                        if (nameMatched) {
                          cmrData = resultFindCmrCN.get(i);
                          details.append("\n");
                          logDuplicateCMR(details, cmrData);
                        }
                      }
                    }
                  }
                } catch (Exception e) {
                  e.printStackTrace();
                  result.setDetails("Error on checking findCMR on Duplicate CMR Check of Chinese, name: " + cnName);
                  engineData.addRejectionComment("OTH", "Error on checking findCMR on Duplicate CMR Check of Chinese, name: " + cnName, "", "");
                  result.setOnError(true);
                  result.setResults("Error on checking findCMR on Duplicate CMR Check of Chinese, name: " + cnName);
                }

                // 3, Check FindCMR Non Latin with historical Chinese name
                if (cnHistoryName != null && !"".equals(cnHistoryName)) {
                  List<String> cnHistoryNameList = Arrays.asList(cnHistoryName.split(";"));
                  try {
                    CompanyRecordModel searchModelFindCmrCN = new CompanyRecordModel();
                    searchModelFindCmrCN.setIssuingCntry(data.getCmrIssuingCntry());
                    for (String historyName : cnHistoryNameList) {
                      searchModelFindCmrCN.setName(historyName);
                      resultFindCmrCN = CompanyFinder.findCompanies(searchModelFindCmrCN);
                      if (!resultFindCmrCN.isEmpty() && resultFindCmrCN.size() > 0) {
                        for (int i = 0; i < resultFindCmrCN.size(); i++) {
                          nameFindCmrCnResult = resultFindCmrCN.get(i).getAltName() != null ? resultFindCmrCN.get(i).getAltName() : "";
                          if (nameFindCmrCnResult != null && nameFindCmrCnResult.equals(historyName)) {

                            historyNmMatched = true;
                            matchedCMRs.add(resultFindCmrCN.get(i).getCmrNo());

                            if (historyNmMatched) {
                              cmrData = resultFindCmrCN.get(i);
                              details.append("\n");
                              logDuplicateCMR(details, cmrData);
                            }
                          }
                        }
                      }
                    }

                  } catch (Exception e) {
                    e.printStackTrace();
                    result.setDetails("Error on getting findCMR data when Duplicate CMR Check of historical Chinese, name: " + cnHistoryName);
                    engineData.addRejectionComment("OTH",
                        "Error on getting findCMR data when Duplicate CMR Check of historical Chinese, name: " + cnHistoryName, "", "");
                    result.setOnError(true);
                    result.setResults("Error on getting findCMR data when Duplicate CMR Check of historical Chinese, name: " + cnHistoryName);
                  }
                }
                // output
                if (nameMatched || historyNmMatched) {
                  result.setResults("Matches Found");
                  result.setResults("Found Duplicate CMRs.");
                  engineData.addRejectionComment("DUPC", "Customer already exists / duplicate CMR", StringUtils.join(matchedCMRs, ", "), "");
                  // to allow overides later
                  requestData.getAdmin().setMatchIndc("C");
                  result.setOnError(true);
                  result.setProcessOutput(output);
                  result.setDetails(details.toString().trim());
                } else if (!nameMatched && !historyNmMatched) {
                  result.setDetails("No Duplicate CMRs were found.");
                  result.setResults("No Matches");
                  result.setOnError(false);
                }
              } else if (resultCNApi != null && !resultCNApi.isSuccess()) {
                result.setDetails("Error on getting China API Data when Duplicate CMR Check of Chinese - " + resultCNApi.getMessage());
                engineData.addRejectionComment("OTH",
                    "Error on getting China API Data when Duplicate CMR Check of Chinese - " + resultCNApi.getMessage(), "", "");
                result.setOnError(true);
                result.setResults("Error on getting China API Data when Duplicate CMR Check of Chinese - " + resultCNApi.getMessage());
              }

            } catch (Exception e) {
              e.printStackTrace();
              result.setDetails("Error on getting China API Data when Duplicate CMR Check of Chinese.");
              engineData.addRejectionComment("OTH", "Error on getting China API Data when Duplicate CMR Check of Chinese.", "", "");
              result.setOnError(true);
              result.setResults("Error on getting China API Data when Duplicate CMR Check of Chinese.");
            }
          } else {
            result.setDetails("Error on getting Chinese name when Duplicate CMR Check.");
            engineData.addRejectionComment("OTH", "Error on getting Chinese name when Duplicate CMR Check.", "", "");
            result.setOnError(true);
            result.setResults("Error on getting Chinese name when Duplicate CMR Check.");
          }
          break;

        case "INTER": // SCENARIO_LOCAL_INTER

          // logic:
          // a) if the return result has same Chinese name and address, then it
          // is a duplicate request
          // b) if there is no Chinese name, then check English name

          iAddr = handler.getIntlAddrById(soldTo, entityManager);
          if (iAddr != null) {
            // cnName = iAddr.getIntlCustNm1() + (iAddr.getIntlCustNm2() != null
            // ? iAddr.getIntlCustNm2() : "");
            if (iAddr.getIntlCustNm2() != null) {
              cnName = iAddr.getIntlCustNm1() + iAddr.getIntlCustNm2();
            } else {
              cnName = iAddr.getIntlCustNm1();
            }
          }
          if (!StringUtils.isBlank(cnName)) {

            try {

              // 1, Check CN API
              if (cnCreditCd != null && cnCreditCd.length() > 0) {
                searchModelCNAPI.setIssuingCntry(data.getCmrIssuingCntry());
                searchModelCNAPI.setCountryCd(soldTo.getLandCntry());
                searchModelCNAPI.setTaxCd1(cnCreditCd);
                resultCNApi = CompanyFinder.getCNApiInfo(searchModelCNAPI, "TAXCD");
              } else {
                cnName = iAddr.getIntlCustNm1() + (iAddr.getIntlCustNm2() != null ? " " + iAddr.getIntlCustNm2() : "");
                searchModelCNAPI.setIssuingCntry(data.getCmrIssuingCntry());
                searchModelCNAPI.setCountryCd(soldTo.getLandCntry());
                searchModelCNAPI.setAltName(cnName);
                resultCNApi = CompanyFinder.getCNApiInfo(searchModelCNAPI, "ALTNAME");
              }

              if (resultCNApi != null && resultCNApi.isSuccess()) {
                cnName = resultCNApi.getRecord().getName();
                cnHistoryName = resultCNApi.getRecord().getHistoryNames();
                cnAddr = resultCNApi.getRecord().getRegLocation();

                try {
                  // 2, Check FindCMR NON Latin with Chinese name
                  CompanyRecordModel searchModelFindCmrCN = new CompanyRecordModel();
                  searchModelFindCmrCN.setIssuingCntry(data.getCmrIssuingCntry());
                  searchModelFindCmrCN.setName(cnName);
                  resultFindCmrCN = CompanyFinder.findCompanies(searchModelFindCmrCN);
                  if (!resultFindCmrCN.isEmpty() && resultFindCmrCN.size() > 0) {
                    for (int i = 0; i < resultFindCmrCN.size(); i++) {
                      nameFindCmrCnResult = resultFindCmrCN.get(i).getAltName() != null ? resultFindCmrCN.get(i).getAltName() : "";
                      if (nameFindCmrCnResult != null && cnName.equals(nameFindCmrCnResult)) {

                        nameMatched = true;

                        // check Chinese address
                        addrFindCmrCnResult = resultFindCmrCN.get(i).getAltStreet() != null ? resultFindCmrCN.get(i).getAltStreet() : "";
                        if (addrFindCmrCnResult.equals(cnAddr)) {
                          nameMatched = true;
                        } else {
                          nameMatched = false;
                        }

                        if (nameMatched) {
                          matchedCMRs.add(resultFindCmrCN.get(i).getCmrNo());
                          cmrData = resultFindCmrCN.get(i);
                          details.append("\n");
                          logDuplicateCMR(details, cmrData);
                        }
                      }
                    }
                  }
                } catch (Exception e) {
                  e.printStackTrace();
                  result.setDetails("Error on checking findCMR on Duplicate CMR Check of Chinese, name: " + cnName);
                  engineData.addRejectionComment("OTH", "Error on checking findCMR on Duplicate CMR Check of Chinese, name: " + cnName, "", "");
                  result.setOnError(true);
                  result.setResults("Error on checking findCMR on Duplicate CMR Check of Chinese, name: " + cnName);
                }

                // 3, Check FindCMR Non Latin with historical Chinese name
                if (cnHistoryName != null && !"".equals(cnHistoryName)) {
                  List<String> cnHistoryNameList = Arrays.asList(cnHistoryName.split(";"));
                  try {
                    CompanyRecordModel searchModelFindCmrCN = new CompanyRecordModel();
                    searchModelFindCmrCN.setIssuingCntry(data.getCmrIssuingCntry());
                    for (String historyName : cnHistoryNameList) {
                      searchModelFindCmrCN.setName(historyName);
                      resultFindCmrCN = CompanyFinder.findCompanies(searchModelFindCmrCN);
                      if (!resultFindCmrCN.isEmpty() && resultFindCmrCN.size() > 0) {
                        for (int i = 0; i < resultFindCmrCN.size(); i++) {
                          nameFindCmrCnResult = resultFindCmrCN.get(i).getAltName() != null ? resultFindCmrCN.get(i).getAltName() : "";
                          if (nameFindCmrCnResult != null && nameFindCmrCnResult.equals(historyName)) {

                            historyNmMatched = true;

                            // check Chinese address
                            addrFindCmrCnResult = resultFindCmrCN.get(i).getAltStreet() != null ? resultFindCmrCN.get(i).getAltStreet() : "";
                            if (addrFindCmrCnResult.equals(cnAddr)) {
                              historyNmMatched = true;
                            } else {
                              historyNmMatched = false;
                            }

                            if (historyNmMatched) {
                              cmrData = resultFindCmrCN.get(i);
                              details.append("\n");
                              logDuplicateCMR(details, cmrData);
                            }
                          }
                        }
                      }
                    }

                  } catch (Exception e) {
                    e.printStackTrace();
                    result.setDetails("Error on getting findCMR data when Duplicate CMR Check of historical Chinese, name: " + cnHistoryName);
                    engineData.addRejectionComment("OTH",
                        "Error on getting findCMR data when Duplicate CMR Check of historical Chinese, name: " + cnHistoryName, "", "");
                    result.setOnError(true);
                    result.setResults("Error on getting findCMR data when Duplicate CMR Check of historical Chinese, name: " + cnHistoryName);
                  }

                }
                // output
                if (nameMatched || historyNmMatched) {
                  result.setResults("Matches Found");
                  result.setResults("Found Duplicate CMRs.");
                  engineData.addRejectionComment("DUPC", "Customer already exists / duplicate CMR", StringUtils.join(matchedCMRs, ", "), "");
                  // to allow overides later
                  requestData.getAdmin().setMatchIndc("C");
                  result.setOnError(true);
                  result.setProcessOutput(output);
                  result.setDetails(details.toString().trim());
                } else if (!nameMatched && !historyNmMatched) {
                  result.setDetails("No Duplicate CMRs were found.");
                  result.setResults("No Matches");
                  result.setOnError(false);
                }
              } else if (resultCNApi != null && !resultCNApi.isSuccess()) {
                result.setDetails("Error on getting China API Data when Duplicate CMR Check of Chinese - " + resultCNApi.getMessage());
                engineData.addRejectionComment("OTH",
                    "Error on getting China API Data when Duplicate CMR Check of Chinese - " + resultCNApi.getMessage(), "", "");
                result.setOnError(true);
                result.setResults("Error on getting China API Data when Duplicate CMR Check of Chinese - " + resultCNApi.getMessage());
              }

            } catch (Exception e) {
              e.printStackTrace();
              result.setDetails("Error on getting China API Data when Duplicate CMR Check of Chinese.");
              engineData.addRejectionComment("OTH", "Error on getting China API Data when Duplicate CMR Check of Chinese.", "", "");
              result.setOnError(true);
              result.setResults("Error on getting China API Data when Duplicate CMR Check of Chinese.");
            }

          } else {

            // check with English name and get matched result, then it is dup
            // req
            CompanyRecordModel searchModelFindCmr = new CompanyRecordModel();
            searchModelFindCmr.setIssuingCntry(data.getCmrIssuingCntry());
            searchModelFindCmr.setName(soldTo.getCustNm1() + (soldTo.getCustNm2() != null ? soldTo.getCustNm2() : ""));
            findCMRResult = searchFindCMR(searchModelFindCmr);
            if (findCMRResult != null && findCMRResult.getItems() != null && !findCMRResult.getItems().isEmpty()) {
              ceidMatched = true;
              List<FindCMRRecordModel> cmrs = findCMRResult.getItems();
              for (FindCMRRecordModel cmrsMods : cmrs) {
                cmrData.setCmrNo(cmrsMods.getCmrNum());
                cmrData.setName(cmrsMods.getCmrName1Plain() + (cmrsMods.getCmrName2Plain() != null ? cmrsMods.getCmrName2Plain() : ""));
                cmrData.setIssuingCntry(cmrsMods.getCmrIssuedBy());
                cmrData.setCied(cmrsMods.getCmrPpsceid());
                cmrData.setCountryCd(cmrsMods.getCmrCountryLanded());
                cmrData.setStreetAddress1(cmrsMods.getCmrStreetAddress());
                cmrData.setStreetAddress2(cmrsMods.getCmrStreetAddressCont());
                cmrData.setPostCd(cmrsMods.getCmrPostalCode());
                cmrData.setCity(cmrsMods.getCmrCity());

                matchedCMRs.add(cmrsMods.getCmrNum());
                details.append("\n");
                logDuplicateCMR(details, cmrData);
              }

              result.setResults("Matches Found");
              result.setResults("Found Duplicate CMRs.");
              engineData.addRejectionComment("DUPC", "Customer already exists / duplicate CMR", StringUtils.join(matchedCMRs, ", "), "");
              // to allow overides later
              requestData.getAdmin().setMatchIndc("C");
              result.setOnError(true);
              result.setProcessOutput(output);
              result.setDetails(details.toString().trim());
            } else {
              result.setDetails("No Duplicate CMRs were found.");
              result.setResults("No Matches");
              result.setOnError(false);
            }

          }
          break;

        case "PRIV": // SCENARIO_LOCAL_PRIV

          // logic:
          // a) check with English name and address, if get matched result, then
          // it is dup req

          CompanyRecordModel searchModelFindCmr = new CompanyRecordModel();
          searchModelFindCmr.setIssuingCntry(data.getCmrIssuingCntry());
          searchModelFindCmr.setName(soldTo.getCustNm1() + (soldTo.getCustNm2() != null ? soldTo.getCustNm2() : ""));
          searchModelFindCmr.setStreetAddress1(soldTo.getAddrTxt());
          searchModelFindCmr.setStreetAddress2(soldTo.getAddrTxt2());
          findCMRResult = searchFindCMR(searchModelFindCmr);
          if (findCMRResult != null && findCMRResult.getItems() != null && !findCMRResult.getItems().isEmpty()) {
            ceidMatched = true;
            List<FindCMRRecordModel> cmrs = findCMRResult.getItems();
            for (FindCMRRecordModel cmrsMods : cmrs) {
              cmrData.setCmrNo(cmrsMods.getCmrNum());
              cmrData.setName(cmrsMods.getCmrName1Plain() + (cmrsMods.getCmrName2Plain() != null ? cmrsMods.getCmrName2Plain() : ""));
              cmrData.setIssuingCntry(cmrsMods.getCmrIssuedBy());
              cmrData.setCied(cmrsMods.getCmrPpsceid());
              cmrData.setCountryCd(cmrsMods.getCmrCountryLanded());
              cmrData.setStreetAddress1(cmrsMods.getCmrStreetAddress());
              cmrData.setStreetAddress2(cmrsMods.getCmrStreetAddressCont());
              cmrData.setPostCd(cmrsMods.getCmrPostalCode());
              cmrData.setCity(cmrsMods.getCmrCity());

              matchedCMRs.add(cmrsMods.getCmrNum());
              details.append("\n");
              logDuplicateCMR(details, cmrData);
            }

            result.setResults("Matches Found");
            result.setResults("Found Duplicate CMRs.");
            engineData.addRejectionComment("DUPC", "Customer already exists / duplicate CMR", StringUtils.join(matchedCMRs, ", "), "");
            // to allow overides later
            requestData.getAdmin().setMatchIndc("C");
            result.setOnError(true);
            result.setProcessOutput(output);
            result.setDetails(details.toString().trim());
          } else {
            result.setDetails("No Duplicate CMRs were found.");
            result.setResults("No Matches");
            result.setOnError(false);
          }

          break;
        case "CROSS": // SCENARIO_CROSS_CROSS

          // logic:
          // a) check with English name and get matched result, then it is dup
          // req

          CompanyRecordModel searchModel = new CompanyRecordModel();
          searchModel.setIssuingCntry(data.getCmrIssuingCntry());
          searchModel.setName(soldTo.getCustNm1() + (soldTo.getCustNm2() != null ? soldTo.getCustNm2() : ""));
          findCMRResult = searchFindCMR(searchModel);
          if (findCMRResult != null && findCMRResult.getItems() != null && !findCMRResult.getItems().isEmpty()) {
            ceidMatched = true;
            List<FindCMRRecordModel> cmrs = findCMRResult.getItems();
            for (FindCMRRecordModel cmrsMods : cmrs) {
              cmrData.setCmrNo(cmrsMods.getCmrNum());
              cmrData.setName(cmrsMods.getCmrName1Plain() + (cmrsMods.getCmrName2Plain() != null ? cmrsMods.getCmrName2Plain() : ""));
              cmrData.setIssuingCntry(cmrsMods.getCmrIssuedBy());
              cmrData.setCied(cmrsMods.getCmrPpsceid());
              cmrData.setCountryCd(cmrsMods.getCmrCountryLanded());
              cmrData.setStreetAddress1(cmrsMods.getCmrStreetAddress());
              cmrData.setStreetAddress2(cmrsMods.getCmrStreetAddressCont());
              cmrData.setPostCd(cmrsMods.getCmrPostalCode());
              cmrData.setCity(cmrsMods.getCmrCity());

              matchedCMRs.add(cmrsMods.getCmrNum());
              details.append("\n");
              logDuplicateCMR(details, cmrData);
            }

            result.setResults("Matches Found");
            result.setResults("Found Duplicate CMRs.");
            engineData.addRejectionComment("DUPC", "Customer already exists / duplicate CMR", StringUtils.join(matchedCMRs, ", "), "");
            // to allow overides later
            requestData.getAdmin().setMatchIndc("C");
            result.setOnError(true);
            result.setProcessOutput(output);
            result.setDetails(details.toString().trim());
          } else {
            result.setDetails("No Duplicate CMRs were found.");
            result.setResults("No Matches");
            result.setOnError(false);
          }
          break;
        default:
          result.setDetails("Invalid Scenario Sub-type on Duplicate CMR Check." + data.getCustSubGrp());
          engineData.addRejectionComment("OTH", "Invalid Scenario Sub-type is missing on Duplicate CMR Check." + data.getCustSubGrp(), "", "");
          result.setOnError(true);
          result.setResults("Invalid Scenario Sub-type is missing on Duplicate CMR Check");
          break;
        }
      } else {
        result.setDetails("Scenario Sub-type is missing on Duplicate CMR Check.");
        engineData.addRejectionComment("OTH", "Scenario Sub-type is missing on Duplicate CMR Check.", "", "");
        result.setOnError(true);
        result.setResults("Scenario Sub-type is missing on Duplicate CMR Check");
      }

    } else {
      result.setDetails("Missing main address on the request.");
      engineData.addRejectionComment("OTH", "Missing main address on the request.", "", "");
      result.setResults("No Matches");
      result.setOnError(true);
    }
    return result;
  }

  public static void logDuplicateCMR(StringBuilder details, CompanyRecordModel companyRecordModel) {
    if (!StringUtils.isBlank(companyRecordModel.getCmrNo())) {
      details.append("CMR Number = " + companyRecordModel.getCmrNo()).append("\n");
    }
    if (!StringUtils.isBlank(companyRecordModel.getMatchGrade())) {
      details.append("Match Grade = " + companyRecordModel.getMatchGrade()).append("\n");
    }
    if (!StringUtils.isBlank(companyRecordModel.getIssuingCntry())) {
      details.append("Issuing Country =  " + companyRecordModel.getIssuingCntry()).append("\n");
    }
    if (!StringUtils.isBlank(companyRecordModel.getAltName())) {
      details.append("Chinese Customer Name =  " + companyRecordModel.getAltName()).append("\n");
    }
    if (!StringUtils.isBlank(companyRecordModel.getCity())) {
      details.append("Chinese City =  " + companyRecordModel.getAltCity()).append("\n");
    }
    if (!StringUtils.isBlank(companyRecordModel.getAltStreet())) {
      details.append("Chinese Address =  " + companyRecordModel.getAltStreet()).append("\n");
    }
    if (!StringUtils.isBlank(companyRecordModel.getName())) {
      details.append("Customer Name=  " + companyRecordModel.getName()).append("\n");
    }
    if (!StringUtils.isBlank(companyRecordModel.getCity())) {
      details.append("City =  " + companyRecordModel.getCity()).append("\n");
    }
    if (!StringUtils.isBlank(companyRecordModel.getStateProv())) {
      details.append("State =  " + companyRecordModel.getStateProv()).append("\n");
    }
    if (!StringUtils.isBlank(companyRecordModel.getPostCd())) {
      details.append("Postal Code =  " + companyRecordModel.getPostCd()).append("\n");
    }
    if (!StringUtils.isBlank(companyRecordModel.getVat())) {
      details.append("VAT =  " + companyRecordModel.getVat()).append("\n");
    }
    if (!StringUtils.isBlank(companyRecordModel.getDunsNo())) {
      details.append("Duns No =  " + companyRecordModel.getDunsNo()).append("\n");
    }
    // if (!StringUtils.isBlank(cmrCheckRecord.getParentDunsNo())) {
    // details.append("Parent Duns No = " +
    // cmrCheckRecord.getParentDunsNo()).append("\n");
    // }
    // if (!StringUtils.isBlank(cmrCheckRecord.getGuDunsNo())) {
    // details.append("Global Duns No = " +
    // cmrCheckRecord.getGuDunsNo()).append("\n");
    // }
    // if (!StringUtils.isBlank(cmrCheckRecord.getCoverageId())) {
    // details.append("Coverage Id= " +
    // cmrCheckRecord.getCoverageId()).append("\n");
    // }
    // if (!StringUtils.isBlank(cmrCheckRecord.getIbmClientId())) {
    // details.append("IBM Client Id = " +
    // cmrCheckRecord.getIbmClientId()).append("\n");
    // }
    // if (!StringUtils.isBlank(cmrCheckRecord.getCapInd())) {
    // details.append("Cap Indicator = " +
    // cmrCheckRecord.getCapInd()).append("\n");
    // }
  }

  public static void logDuplicateCMR(StringBuilder details, DuplicateCMRCheckResponse cmrCheckRecord) {
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
  }

  @Override
  public String getProcessCode() {
    return AutomationElementRegistry.CN_DUP_CMR_CHECK;
  }

  @Override
  public String getProcessDesc() {
    return "China Duplicate CMR Check";
  }

  public MatchingResponse<DuplicateCMRCheckResponse> getMatches(EntityManager entityManager, RequestData requestData, AutomationEngineData engineData)
      throws Exception {
    Admin admin = requestData.getAdmin();
    Data data = requestData.getData();
    ScenarioExceptionsUtil scenarioExceptions = getScenarioExceptions(entityManager, requestData, engineData);
    MatchingResponse<DuplicateCMRCheckResponse> response = new MatchingResponse<DuplicateCMRCheckResponse>();
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
      if (addr != null) {
        for (String rdcAddrType : rdcAddrTypes) {

          DuplicateCMRCheckRequest request = getRequest(entityManager, data, admin, addr, engineData, rdcAddrType, vatMatchRequired);
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
          } else if (res.getSuccess()) {
            updateMatches(response, res);
          }
        }
      } else {
        log.debug("No '" + cmrAddrType + "' address on the request. Skipping duplicate check.");
        continue;
      }
    }

    if (response.getSuccess() && response.getMatches().size() > 0) {
      AutomationUtil countryUtil = AutomationUtil.getNewCountryUtil(data.getCmrIssuingCntry());
      if (countryUtil != null) {
        countryUtil.filterDuplicateCMRMatches(entityManager, requestData, engineData, response);
      }
    }

    // reverify
    if (response.getSuccess() && response.getMatches().size() == 0) {
      response.setMatched(false);
      response.setMessage("No matches found for the given search criteria.");
    }

    return response;
  }

  private DuplicateCMRCheckRequest getRequest(EntityManager entityManager, Data data, Admin admin, Addr addr, AutomationEngineData engineData,
      String rdcAddrType, boolean vatMatchRequired) {
    DuplicateCMRCheckRequest request = new DuplicateCMRCheckRequest();
    if (addr != null) {
      request.setIssuingCountry(data.getCmrIssuingCntry());
      request.setLandedCountry(addr.getLandCntry());
      GEOHandler handler = RequestUtils.getGEOHandler(data.getCmrIssuingCntry());
      if (handler != null && !handler.customerNamesOnAddress()) {
        request.setCustomerName(admin.getMainCustNm1() + (StringUtils.isBlank(admin.getMainCustNm2()) ? "" : " " + admin.getMainCustNm2()));
      } else {
        request.setCustomerName(addr.getCustNm1() + (StringUtils.isBlank(addr.getCustNm2()) ? "" : " " + addr.getCustNm2()));
      }

      if (data.getCustSubGrp() != null) {
        if ("PRIV".equals(data.getCustSubGrp()) || "INTER".equals(data.getCustSubGrp())) {
          request.setStreetLine1(addr.getAddrTxt());
          request.setStreetLine2(StringUtils.isEmpty(addr.getAddrTxt2()) ? "" : addr.getAddrTxt2());
        } else if ("CROSS".equals(data.getCustSubGrp()) || "AQSTN".equals(data.getCustSubGrp())) {
          request.setNameMatch("Y");
        }

      }

      if (vatMatchRequired) {
        if (StringUtils.isNotBlank(data.getVat())) {
          request.setVat(data.getVat());
        } else if (StringUtils.isNotBlank(addr.getVat())) {
          request.setVat(addr.getVat());
        }
      }
      request.setAddrType(rdcAddrType);

      DuplicateChecksUtil.setCountrySpecificsForCMRChecks(entityManager, admin, data, addr, request, engineData);
    }
    return request;
  }

  private void updateMatches(MatchingResponse<DuplicateCMRCheckResponse> global, MatchingResponse<DuplicateCMRCheckResponse> iteration) {
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

  protected String getZS01Kunnr(String cmrNo, String cntry) throws Exception {
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

  protected static FindCMRResultModel searchFindCMR(CompanyRecordModel searchModel) throws Exception {
    String cmrNo = searchModel.getCmrNo();
    String issuingCntry = searchModel.getIssuingCntry();
    String params = null;
    if (!StringUtils.isBlank(searchModel.getCied())) {
      params = "&ppsCeId=" + searchModel.getCied();
    }
    if (!StringUtils.isBlank(searchModel.getName())) {
      String name = searchModel.getName();
      name = StringUtils.replace(name, " ", "%20");
      params = "&customerName=" + name;
    }
    if (!StringUtils.isBlank(searchModel.getStreetAddress1())) {
      String street = searchModel.getStreetAddress1();
      street = StringUtils.replace(street, " ", "%20");
      if (!StringUtils.isBlank(searchModel.getStreetAddress2())) {
        String street2 = searchModel.getStreetAddress2();
        street2 = StringUtils.replace(street2, " ", "%20");
        street += street2;
      }
      params += "&streetAddress=" + street;
    }
    FindCMRResultModel results = SystemUtil.findCMRs(cmrNo, issuingCntry, 10, null, params);
    return results;
  }

}
