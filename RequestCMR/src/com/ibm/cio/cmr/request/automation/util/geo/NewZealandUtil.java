package com.ibm.cio.cmr.request.automation.util.geo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import com.ibm.cio.cmr.request.CmrConstants;
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
import com.ibm.cio.cmr.request.entity.listeners.ChangeLogListener;
import com.ibm.cio.cmr.request.model.window.UpdatedNameAddrModel;
import com.ibm.cio.cmr.request.util.SystemLocation;
import com.ibm.cio.cmr.request.util.dnb.DnBUtil;
import com.ibm.cmr.services.client.AutomationServiceClient;
import com.ibm.cmr.services.client.CmrServicesFactory;
import com.ibm.cmr.services.client.ServiceClient.Method;
import com.ibm.cmr.services.client.automation.AutomationResponse;
import com.ibm.cmr.services.client.automation.ap.nz.NZBNValidationRequest;
import com.ibm.cmr.services.client.automation.ap.nz.NZBNValidationResponse;
import com.ibm.cmr.services.client.matching.MatchingResponse;
import com.ibm.cmr.services.client.matching.dnb.DnBMatchingResponse;

public class NewZealandUtil extends AutomationUtil {

  private static final Logger LOG = Logger.getLogger(NewZealandUtil.class);

  // CREATCMR-8430: do DNB check for all address types for NZ update (ignore mailing address)
  private static final List<String> RELEVANT_ADDRESSES = Arrays.asList(CmrConstants.RDC_SOLD_TO, "MAIL", "ZP01", "ZI01", "ZF01", "CTYG", "CTYH");
  private static final List<String> NON_RELEVANT_ADDRESS_FIELDS = Arrays.asList("Attn", "Phone #", "Customer Name", "Customer Name Con't", "State");

  private static final String SCENARIO_PRIVATE_CUSTOMER = "PRIV";
  private static final String SCENARIO_BLUEMIX = "BLUMX";
  private static final String SCENARIO_MARKETPLACE = "MKTPC";
  private static final String SCENARIO_INTERNAL = "INTER";
  private static final String SCENARIO_DUMMY = "DUMMY";
  private static final String SCENARIO_ACQUISITION = "AQSTN";
  private static final String SCENARIO_NORMAL = "NRML";
  private static final String SCENARIO_ESOSW = "ESOSW";
  private static final String SCENARIO_ECOSYS = "ECSYS";
  private static final String SCENARIO_CROSS_FOREIGN = "CROSS";
  private static final List<String> RELEVANT_SCENARIO = Arrays.asList(SCENARIO_PRIVATE_CUSTOMER, SCENARIO_DUMMY, SCENARIO_INTERNAL);

  @Override
  public boolean performScenarioValidation(EntityManager entityManager, RequestData requestData, AutomationEngineData engineData,
      AutomationResult<ValidationOutput> result, StringBuilder details, ValidationOutput output) {
    Admin admin = requestData.getAdmin();
    Data data = requestData.getData();
    String scenario = data.getCustSubGrp();

    processSkipCompanyChecks(engineData, requestData, details);
    switch (scenario) {

    case SCENARIO_PRIVATE_CUSTOMER:
      engineData.addPositiveCheckStatus(AutomationEngineData.SKIP_GBG);
      break;
    // case SCENARIO_BLUEMIX:
    // case SCENARIO_MARKETPLACE:
    case SCENARIO_INTERNAL:
      engineData.addPositiveCheckStatus(AutomationEngineData.SKIP_GBG);
      break;
    case SCENARIO_DUMMY:
      engineData.addPositiveCheckStatus(AutomationEngineData.SKIP_GBG);
      break;
    // case SCENARIO_ACQUISITION:
    // case SCENARIO_NORMAL:
    // case SCENARIO_ESOSW:
    // case SCENARIO_ECOSYS:
    // case SCENARIO_CROSS_FOREIGN:
    // addToNotifyListANZ(entityManager, data.getId().getReqId());
    }
    boolean companyProofProvided = DnBUtil.isDnbOverrideAttachmentProvided(entityManager, admin.getId().getReqId());
    if (companyProofProvided) {
      details.append("Supporting documentation(Company Proof) is provided by the requester as attachment").append("\n");
      details.append("This Request will be routed to CMDE.\n");
      engineData.addRejectionComment("OTH", "This Request will be routed to CMDE.", "", "");
      admin.setCompVerifiedIndc("Y");
      entityManager.merge(admin);
      entityManager.flush();
    }
    return true;
  }

  @Override
  public AutomationResult<OverrideOutput> doCountryFieldComputations(EntityManager entityManager, AutomationResult<OverrideOutput> results,
      StringBuilder details, OverrideOutput overrides, RequestData requestData, AutomationEngineData engineData) throws Exception {
    // get request admin and data

    Data data = requestData.getData();
    Admin admin = requestData.getAdmin();
    Addr zs01 = requestData.getAddress("ZS01");
    String custType = data.getCustGrp();
    String scenario = data.getCustSubGrp();
    // boolean isSourceSysIDBlank = StringUtils.isBlank(admin.getSourceSystId())
    // ? true : false;// && !isSourceSysIDBlank

    if ("C".equals(admin.getReqType()) && !RELEVANT_SCENARIO.contains(scenario) && SystemLocation.NEW_ZEALAND.equals(data.getCmrIssuingCntry())
        && "LOCAL".equalsIgnoreCase(custType) && engineData.getPendingChecks() != null
        && (engineData.getPendingChecks().containsKey("DnBMatch") || engineData.getPendingChecks().containsKey("DNBCheck"))) {
      LOG.info("Starting Field Computations for Request ID " + data.getId().getReqId());
      // register vat service of Norway
      AutomationResponse<NZBNValidationResponse> response = null;
      String regex = "\\s+$";
      String customerName = zs01.getCustNm1() + (StringUtils.isBlank(zs01.getCustNm2()) ? "" : " " + zs01.getCustNm2());

      boolean custNmMatch = false;
      boolean matchesAddAPI = false;

      LOG.debug("Checking CustNm with NZBN API to vrify CustNm update");
      try {
        response = getNZBNService(admin, data, zs01);
      } catch (Exception e) {
        if (response == null || !response.isSuccess()) {
          LOG.debug("\nFailed to Connect to NZBN Service.");
        }
      }
      if (response != null && response.isSuccess() && response.getRecord() != null) {
        // custNm Validation
        LOG.debug("\nSuccess to Connect to NZBN Service.");
        if (StringUtils.isNotEmpty(response.getRecord().getName())) {
          String responseCustNm = StringUtils.isBlank(response.getRecord().getName()) ? "" : response.getRecord().getName().replaceAll(regex, "");

          if (customerName.equalsIgnoreCase(responseCustNm)) {
            LOG.debug("\ncustNmMatch  = true to Connect to NZBN Service.");
            custNmMatch = true;
          }
        }
        String regexForAddr = "\\s+|$";
        String addressAll = zs01.getCustNm1() + (zs01.getCustNm2() == null ? "" : zs01.getCustNm2()) + zs01.getAddrTxt()
            + (zs01.getAddrTxt2() == null ? "" : zs01.getAddrTxt2()) + (zs01.getStateProv() == null ? "" : zs01.getStateProv())
            + (zs01.getCity1() == null ? "" : zs01.getCity1()) + (zs01.getPostCd() == null ? "" : zs01.getPostCd());
        addressAll = addressAll.toUpperCase();
        if (StringUtils.isNotEmpty(response.getRecord().getAddress())
            && addressAll.replaceAll(regexForAddr, "").contains(response.getRecord().getAddress().replaceAll(regexForAddr, "").toUpperCase())
            && StringUtils.isNotEmpty(response.getRecord().getCity())
            && addressAll.replaceAll(regexForAddr, "").contains(response.getRecord().getCity().replaceAll(regexForAddr, "").toUpperCase())
            && StringUtils.isNotEmpty(response.getRecord().getPostal())
            && addressAll.replaceAll(regexForAddr, "").contains(response.getRecord().getPostal().replaceAll(regexForAddr, "").toUpperCase())) {
          matchesAddAPI = true;
          LOG.debug("\nSuccess to Connect to NZBN Service matchesAddAPI:true.");
        }

        // CREATCMR-8430: checking if the address matches with
        // service type address from API
        LOG.debug("REGISTERED Address matched ?  " + matchesAddAPI);
        String serviceAddr = response.getRecord().getServiceAddressDetail();
        if (!matchesAddAPI && StringUtils.isNotEmpty(serviceAddr)) {
          LOG.debug("****** addressOfRequest: " + addressAll);
          LOG.debug("****** serviceAddr: " + serviceAddr);
          String[] serviceAddrArr = serviceAddr.split("\\^");
          boolean serviceFlag = false;
          for (String partAddr : serviceAddrArr) {
            serviceFlag = (addressAll.replaceAll(regexForAddr, "").contains(partAddr.replaceAll(regexForAddr, "").toUpperCase()));
            if (!serviceFlag) {
              break;
            }
          }
          matchesAddAPI = serviceFlag;
          LOG.debug("SERVICE Address matched ?  " + matchesAddAPI);
        }
      }
      if (custNmMatch && matchesAddAPI) {
        details.append("The Customer Name and address matched NZBN API.").append("\n");
        results.setResults("Calculated.");
        engineData.setNZBNAPICheck(true);

      } else {
        results.setResults("Request check failed");
        if (!"PayGo-Test".equals(admin.getSourceSystId()) && !"BSS".equals(admin.getSourceSystId())) {
          results.setOnError(true);
        }
        admin.setCompVerifiedIndc("Y");
        entityManager.merge(admin);
        entityManager.flush();
        if (!custNmMatch) {
          details.append("The Customer Name doesn't match NZBN API");
          if (response != null && response.isSuccess() && response.getRecord() != null) {
            details.append(" Call NZBN API result - NZBN:  " + response.getRecord().getBusinessNumber() + " \n");
            details.append(" - Name.:  " + response.getRecord().getName() + " \n");
          }
        } else if (!matchesAddAPI) {
          details.append("The address doesn't match NZBN API");
          // company proof
          if (DnBUtil.isDnbOverrideAttachmentProvided(entityManager, admin.getId().getReqId())) {
            details.append("\nSupporting documentation is provided by the requester as attachment for " + customerName).append("\n");
          } else {
            details.append("\nNo supporting documentation is provided by the requester for customer name " + " :" + customerName
                + " update. Please provide Supporting documentation(Company Proof) as attachment.");
          }
          engineData.addNegativeCheckStatus("NZName", "The Customer Name doesn't match from NZBN API");
        }
      }
    }
    results.setDetails(details.toString());

    if (("PayGo-Test".equals(admin.getSourceSystId()) || "BSS".equals(admin.getSourceSystId()))
        && ("LOCAL".equals(data.getCustGrp()) && "PRIV".equals(data.getCustSubGrp()))) {
      data.setApCustClusterId("00002");
      entityManager.merge(data);
      entityManager.flush();
    }
    return results;
  }

  private AutomationResponse<NZBNValidationResponse> getNZBNService(Admin admin, Data data, Addr addr) throws Exception {
    AutomationServiceClient client = CmrServicesFactory.getInstance().createClient(SystemConfiguration.getValue("BATCH_SERVICES_URL"),
        AutomationServiceClient.class);
    client.setReadTimeout(1000 * 60 * 5);
    client.setRequestMethod(Method.Get);

    NZBNValidationRequest request = new NZBNValidationRequest();
    String customerName = addr.getCustNm1() + (StringUtils.isBlank(addr.getCustNm2()) ? "" : " " + addr.getCustNm2());
    if (StringUtils.isNotBlank(data.getVat())) {
      request.setBusinessNumber(data.getVat());
      LOG.debug("request-businessNumber:" + request.getBusinessNumber());
    }
    request.setName(customerName);
    LOG.debug("request-name:" + customerName);

    AutomationResponse<?> rawResponse = client.executeAndWrap(AutomationServiceClient.NZ_BN_VALIDATION_SERVICE_ID, request, AutomationResponse.class);
    ObjectMapper mapper = new ObjectMapper();
    String json = mapper.writeValueAsString(rawResponse);
    TypeReference<AutomationResponse<NZBNValidationResponse>> ref = new TypeReference<AutomationResponse<NZBNValidationResponse>>() {
    };
    AutomationResponse<NZBNValidationResponse> nzbnResponse = mapper.readValue(json, ref);
    return nzbnResponse;
  }

  @Override
  public boolean runUpdateChecksForData(EntityManager entityManager, AutomationEngineData engineData, RequestData requestData,
      RequestChangeContainer changes, AutomationResult<ValidationOutput> output, ValidationOutput validation) throws Exception {
    Admin admin = requestData.getAdmin();
    Data data = requestData.getData();
    StringBuilder details = new StringBuilder();
    boolean CustNmChanged = changes.isLegalNameChanged();
    ChangeLogListener.setManager(entityManager);
    // CustNmChanged = false;
    if (CustNmChanged) {
      AutomationResponse<NZBNValidationResponse> response = null;
      Addr zs01 = requestData.getAddress("ZS01");
      String regex = "\\s+$";
      String customerName = zs01.getCustNm1() + (StringUtils.isBlank(zs01.getCustNm2()) ? "" : " " + zs01.getCustNm2());
      String formerCustName = !StringUtils.isBlank(admin.getOldCustNm1()) ? admin.getOldCustNm1().toUpperCase() : "";
      formerCustName += !StringUtils.isBlank(admin.getOldCustNm2()) ? " " + admin.getOldCustNm2().toUpperCase() : "";
      List<String> dnbTradestyleNames = new ArrayList<String>();
      boolean custNmMatch = false;
      boolean formerCustNmMatch = false;
      try {

        if (!(custNmMatch && formerCustNmMatch)) {
          LOG.debug("Now Checking with DNB to vrify CustNm update");
          MatchingResponse<DnBMatchingResponse> Dnbresponse = DnBUtil.getMatches(requestData, null, "ZS01");
          List<DnBMatchingResponse> matches = Dnbresponse.getMatches();
          if (!matches.isEmpty()) {
            for (DnBMatchingResponse dnbRecord : matches) {
              // checks for CustNm match
              String dnbCustNm = StringUtils.isBlank(dnbRecord.getDnbName()) ? "" : dnbRecord.getDnbName().replaceAll("\\s+$", "");
              if (customerName.equalsIgnoreCase(dnbCustNm)) {
                custNmMatch = true;
                dnbTradestyleNames = dnbRecord.getTradeStyleNames();
                if (dnbTradestyleNames != null) {
                  for (String tradestyleNm : dnbTradestyleNames) {
                    tradestyleNm = tradestyleNm.replaceAll("\\s+$", "");
                    if (tradestyleNm.equalsIgnoreCase(formerCustName)) {
                      formerCustNmMatch = true;
                      break;
                    }
                  }
                }
              }
            }
          }
        }

        if (!(custNmMatch && formerCustNmMatch)) {
          LOG.debug("DNB Checking CustNm match failed. Now Checking CustNm with NZBN API with  to vrify CustNm update");
          try {
            response = getNZBNService(admin, data, zs01);
          } catch (Exception e) {
            if (response == null || !response.isSuccess()) {
              LOG.debug("\nFailed to Connect to NZBN Service.");
            }
          }
          if (response != null && response.isSuccess()) {
            // custNm Validation
            LOG.debug("\nSuccess to Connect to NZBN Service.");
            if (StringUtils.isNotEmpty(response.getRecord().getName())) {
              String responseCustNm = StringUtils.isBlank(response.getRecord().getName()) ? "" : response.getRecord().getName().replaceAll(regex, "");

              if (!(custNmMatch)) {
                if (customerName.equalsIgnoreCase(responseCustNm)) {
                  custNmMatch = true;
                  LOG.debug("\ncustNmMatch  = true to Connect to NZBN Service.");
                }
              }
            }
            if (response.getRecord() != null && response.getRecord().getPreviousEntityNames() != null
                && response.getRecord().getPreviousEntityNames().length > 0) {
              String[] historicalNameList = new String[response.getRecord().getPreviousEntityNames().length];
              historicalNameList = response.getRecord().getPreviousEntityNames();
              for (String historicalNm : historicalNameList) {
                historicalNm = historicalNm.replaceAll(regex, "");
                if (formerCustName.equalsIgnoreCase(historicalNm)) {
                  formerCustNmMatch = true;
                  LOG.debug("\nformerCustNmMatch = true to Connect to NZBN Service.");
                }
              }
            }
          }
        }

        if (custNmMatch && formerCustNmMatch) {
          validation.setSuccess(true);
          validation.setMessage("Successful");
          output.setProcessOutput(validation);
          output.setDetails("Updates to the CustomerNm field Verified");

        } else {
          validation.setMessage("Not Validated");
          details.append("The Customer Name and Former Customer Name doesn't match from DNB & NZBN API");
          if (response != null && response.isSuccess() && response.getRecord() != null) {
            details.append(" Call NZBN API result - NZBN:  " + response.getRecord().getBusinessNumber() + " \n");
            details.append(" - Name.:  " + response.getRecord().getName() + " \n");
            if (response.getRecord().getPreviousEntityNames() != null) {
              String[] historicalNameList = new String[response.getRecord().getPreviousEntityNames().length];
              historicalNameList = response.getRecord().getPreviousEntityNames();
              for (String historicalNm : historicalNameList) {
                historicalNm = historicalNm.replaceAll(regex, "");
                details.append(" - historicalName:  " + historicalNm + " \n");
              }
            }
          }
          // company proof
          if (DnBUtil.isDnbOverrideAttachmentProvided(entityManager, admin.getId().getReqId())) {
            details.append("\nSupporting documentation is provided by the requester as attachment for " + customerName).append("\n");
          } else {
            details.append("\nNo supporting documentation is provided by the requester for customer name update from " + formerCustName + " to "
                + customerName + " update. Please provide Supporting documentation(Company Proof) as attachment.");
          }
          output.setDetails(details.toString());
          engineData.addNegativeCheckStatus("ABNLegalName", "The Customer Name and Former Customer Name doesn't match from DNB & NZBN API");
        }

      } finally {
        ChangeLogListener.clearManager();
      }
    } else {
      validation.setSuccess(true);
      validation.setMessage("Successful");
      output.setProcessOutput(validation);
      output.setDetails("Updates to the dataFields fields skipped validation");
    }
    return true;
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

    boolean skipCompanyChecks = data.getCustSubGrp() != null && (data.getCustSubGrp().contains(SCENARIO_PRIVATE_CUSTOMER)
        || data.getCustSubGrp().contains(SCENARIO_DUMMY) || data.getCustSubGrp().contains(SCENARIO_INTERNAL));
    if (skipCompanyChecks) {
      details.append("This checks will be skipped.\n");
      ScenarioExceptionsUtil exc = (ScenarioExceptionsUtil) engineData.get("SCENARIO_EXCEPTIONS");
      if (exc != null) {
        exc.setSkipCompanyVerification(true);
        engineData.put("SCENARIO_EXCEPTIONS", exc);
      }
    }
  }

  private boolean isRelevantAddressFieldUpdated(RequestChangeContainer changes, Addr addr) {
    List<UpdatedNameAddrModel> addrChanges = changes.getAddressChanges(addr.getId().getAddrType(), addr.getId().getAddrSeq());
    if (addrChanges == null) {
      return false;
    }
    for (UpdatedNameAddrModel change : addrChanges) {
      if (!NON_RELEVANT_ADDRESS_FIELDS.contains(change.getDataField())) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean runUpdateChecksForAddress(EntityManager entityManager, AutomationEngineData engineData, RequestData requestData,
      RequestChangeContainer changes, AutomationResult<ValidationOutput> output, ValidationOutput validation) throws Exception {
    Admin admin = requestData.getAdmin();
    Data data = requestData.getData();
    boolean cmdeReview = false;
    boolean poboxReview = false;

    List<Addr> addresses = null;
    StringBuilder checkDetails = new StringBuilder();
    Set<String> resultCodes = new HashSet<String>();// R - review
    for (String addrType : RELEVANT_ADDRESSES) {
      if (changes.isAddressChanged(addrType)) {
        if (CmrConstants.RDC_SOLD_TO.equals(addrType)) {
          addresses = Collections.singletonList(requestData.getAddress(CmrConstants.RDC_SOLD_TO));
        } else {
          addresses = requestData.getAddresses(addrType);
        }
        for (Addr addr : addresses) {
          Addr addressToChk = requestData.getAddress(addrType);
          if (CmrConstants.RDC_SOLD_TO.equals(addressToChk.getId().getAddrType())) {
            String addressStr = addressToChk.getCustNm1() + (addressToChk.getCustNm2() == null ? "" : addressToChk.getCustNm2())
                + addressToChk.getAddrTxt() + (addressToChk.getAddrTxt2() == null ? "" : addressToChk.getAddrTxt2());
            if (addressStr != null && addressStr.contains("PO BOX")) {
              poboxReview = true;
            }
          }
          if ("Y".equals(addr.getChangedIndc())) {
            // update address
            if (RELEVANT_ADDRESSES.contains(addrType)) {
              if (isRelevantAddressFieldUpdated(changes, addr)) {
                List<DnBMatchingResponse> matches = getMatches(requestData, engineData, addressToChk, false);
                AutomationResponse<NZBNValidationResponse> nZBNAPIresponse = null;
                boolean matchesDnb = false;
                boolean matchesAddAPI = false;
                if (matches != null) {
                  // check against D&B
                  matchesDnb = ifaddressCloselyMatchesDnb(matches, addr, admin, data.getCmrIssuingCntry());
                }

                if (!matchesDnb) {
                  LOG.debug("DNB Checking Addr match failed. Now Checking Addr with NZBN API with  to vrify Addr update");
                  String regexForAddr = "\\s+|$";
                  try {
                    nZBNAPIresponse = getNZBNService(admin, data, addr);
                  } catch (Exception e) {
                    if (nZBNAPIresponse == null || !nZBNAPIresponse.isSuccess()) {
                      LOG.debug("\nFailed to Connect to NZBN Service.");
                    }
                  }

                  if (nZBNAPIresponse != null && nZBNAPIresponse.isSuccess() && nZBNAPIresponse.getRecord() != null) {
                    // addr Validation
                    LOG.debug("\nSuccess to Connect to NZBN Service.");
                    String addressAll = addressToChk.getCustNm1() + (addressToChk.getCustNm2() == null ? "" : addressToChk.getCustNm2())
                        + addressToChk.getAddrTxt() + (addressToChk.getAddrTxt2() == null ? "" : addressToChk.getAddrTxt2())
                        + (addressToChk.getStateProv() == null ? "" : addressToChk.getStateProv())
                        + (addressToChk.getCity1() == null ? "" : addressToChk.getCity1())
                        + (addressToChk.getPostCd() == null ? "" : addressToChk.getPostCd());
                    addressAll = addressAll.toUpperCase();
                    if (StringUtils.isNotEmpty(nZBNAPIresponse.getRecord().getAddress())
                        && addressAll.replaceAll(regexForAddr, "")
                            .contains(nZBNAPIresponse.getRecord().getAddress().replaceAll(regexForAddr, "").toUpperCase())
                        && StringUtils.isNotEmpty(nZBNAPIresponse.getRecord().getCity())
                        && addressAll.replaceAll(regexForAddr, "")
                            .contains(nZBNAPIresponse.getRecord().getCity().replaceAll(regexForAddr, "").toUpperCase())
                        && StringUtils.isNotEmpty(nZBNAPIresponse.getRecord().getPostal())
                        && addressAll.replaceAll(regexForAddr, "")
                            .contains(nZBNAPIresponse.getRecord().getPostal().replaceAll(regexForAddr, "").toUpperCase())) {
                      matchesAddAPI = true;
                      LOG.debug("\nSuccess to Connect to NZBN Service matchesAddAPI:true.");
                    }

                    // CREATCMR-8430: checking if the address matches with
                    // service type address from API
                    LOG.debug("REGISTERED Address matched ?  " + matchesAddAPI);
                    String serviceAddr = nZBNAPIresponse.getRecord().getServiceAddressDetail();
                    if (!matchesAddAPI && StringUtils.isNotEmpty(serviceAddr)) {
                      LOG.debug("****** addressOfRequest: " + addressAll);
                      LOG.debug("****** serviceAddr: " + serviceAddr);
                      String[] serviceAddrArr = serviceAddr.split("\\^");
                      boolean serviceFlag = false;
                      for (String partAddr : serviceAddrArr) {
                        serviceFlag = (addressAll.replaceAll(regexForAddr, "").contains(partAddr.replaceAll(regexForAddr, "").toUpperCase()));
                        if (!serviceFlag) {
                          break;
                        }
                      }
                      matchesAddAPI = serviceFlag;
                      LOG.debug("SERVICE Address matched ?  " + matchesAddAPI);
                    }
                  }
                }
                if (matchesDnb || matchesAddAPI) {
                  if (matchesDnb) {
                    checkDetails.append("\nUpdate address " + addrType + "(" + addr.getId().getAddrSeq() + ") matches D&B records.");
                  } else {
                    checkDetails.append("\nUpdate address " + addrType + "(" + addr.getId().getAddrSeq() + ") matches NZBN API records.\n");
                  }
                } else {

                  LOG.debug("Update address for " + addrType + "(" + addr.getId().getAddrSeq() + ") does not match D&B & NZBN API.");
                  cmdeReview = true;
                  checkDetails.append("\nUpdate address " + addrType + "(" + addr.getId().getAddrSeq() + ") did not match D&B"
                      + "  & NZBN API records.\n");
                  // company proof
                  if (DnBUtil.isDnbOverrideAttachmentProvided(entityManager, admin.getId().getReqId())) {
                    checkDetails.append("\nSupporting documentation is provided by the requester as attachment for " + addrType).append("\n");
                  } else {
                    checkDetails.append("\nNo supporting documentation is provided by the requester for " + addrType
                        + " address. Please provide Supporting documentation(Company Proof) as attachment.");
                  }

                }

              } else {
                checkDetails.append("Updates to non-address fields for " + addrType + "(" + addr.getId().getAddrSeq() + ") skipped in the checks.")
                    .append("\n");
              }
            } else {
              // proceed
              LOG.debug("Update to Address " + addrType + "(" + addr.getId().getAddrSeq() + ") skipped in the checks.\\n");
              checkDetails.append("Updates to Address (" + addr.getId().getAddrSeq() + ") skipped in the checks.\n");
            }
          } else if ("N".equals(addr.getImportInd())) {
            // new address addition
            // CREATCMR-8430: add NZBN API check for new addresses
            AutomationResponse<NZBNValidationResponse> nZBNAPIresponse = null;
            boolean matchesAddAPI = false;

            List<DnBMatchingResponse> matches = getMatches(requestData, engineData, addressToChk, false);

            boolean matchesDnb = false;
            if (matches != null) {
              // check against D&B
              matchesDnb = ifaddressCloselyMatchesDnb(matches, addr, admin, data.getCmrIssuingCntry());
            }

            if (matchesDnb) {
              checkDetails.append("\nNew address " + addrType + "(" + addr.getId().getAddrSeq() + ") matches D&B records.");
            } else {
              // CREATCMR-8430: add NZBN API check for new addresses
              LOG.debug(
                  "New address for " + addrType + "(" + addr.getId().getAddrSeq() + ") does not match D&B. Now Checking Addr with NZBN API... ");

              String regexForAddr = "\\s+|$";
              try {
                nZBNAPIresponse = getNZBNService(admin, data, addr);
              } catch (Exception e) {
                if (nZBNAPIresponse == null || !nZBNAPIresponse.isSuccess()) {
                  LOG.debug("\nFailed to Connect to NZBN Service.");
                }
              }

              if (nZBNAPIresponse != null && nZBNAPIresponse.isSuccess() && nZBNAPIresponse.getRecord() != null) {
                // addr Validation
                LOG.debug("\nSuccess to Connect to NZBN Service.");
                String addressAll = addressToChk.getCustNm1() + (addressToChk.getCustNm2() == null ? "" : addressToChk.getCustNm2())
                    + addressToChk.getAddrTxt() + (addressToChk.getAddrTxt2() == null ? "" : addressToChk.getAddrTxt2())
                    + (addressToChk.getStateProv() == null ? "" : addressToChk.getStateProv())
                    + (addressToChk.getCity1() == null ? "" : addressToChk.getCity1())
                    + (addressToChk.getPostCd() == null ? "" : addressToChk.getPostCd());
                addressAll = addressAll.toUpperCase();
                if (StringUtils.isNotEmpty(nZBNAPIresponse.getRecord().getAddress())
                    && addressAll.replaceAll(regexForAddr, "")
                        .contains(nZBNAPIresponse.getRecord().getAddress().replaceAll(regexForAddr, "").toUpperCase())
                    && StringUtils.isNotEmpty(nZBNAPIresponse.getRecord().getCity())
                    && addressAll.replaceAll(regexForAddr, "")
                        .contains(nZBNAPIresponse.getRecord().getCity().replaceAll(regexForAddr, "").toUpperCase())
                    && StringUtils.isNotEmpty(nZBNAPIresponse.getRecord().getPostal())
                    && addressAll.replaceAll(regexForAddr, "")
                        .contains(nZBNAPIresponse.getRecord().getPostal().replaceAll(regexForAddr, "").toUpperCase())) {
                  matchesAddAPI = true;
                  LOG.debug("\nSuccess to Connect to NZBN Service matchesAddAPI:true.");
                }

                // CREATCMR-8430: checking if the address matches with service
                // type address from API
                LOG.debug("REGISTERED Address matched ?  " + matchesAddAPI);
                String serviceAddr = nZBNAPIresponse.getRecord().getServiceAddressDetail();
                if (!matchesAddAPI && StringUtils.isNotEmpty(serviceAddr)) {
                  LOG.debug("****** addressOfRequest: " + addressAll);
                  LOG.debug("****** serviceAddr: " + serviceAddr);
                  String[] serviceAddrArr = serviceAddr.split("\\^");
                  boolean serviceFlag = false;
                  for (String partAddr : serviceAddrArr) {
                    serviceFlag = (addressAll.replaceAll(regexForAddr, "").contains(partAddr.replaceAll(regexForAddr, "").toUpperCase()));
                    if (!serviceFlag) {
                      break;
                    }
                  }
                  matchesAddAPI = serviceFlag;
                  LOG.debug("SERVICE Address matched ?  " + matchesAddAPI);
                }
              }

              if (matchesAddAPI) {
                checkDetails.append("\nNew address " + addrType + "(" + addr.getId().getAddrSeq() + ") matches NZBN API records.\n");
              } else {
                cmdeReview = true;
                LOG.debug("\nNew address " + addrType + "(" + addr.getId().getAddrSeq() + ") matches NZBN API records.\n");

                checkDetails.append("\nNew address " + addrType + "(" + addr.getId().getAddrSeq() + ") did not match NZBN API records.\n");
                 // company proof
                if (DnBUtil.isDnbOverrideAttachmentProvided(entityManager, admin.getId().getReqId())) {
                  checkDetails.append("Supporting documentation is provided by the requester as attachment for " + addrType).append("\n");
                } else {
                  checkDetails.append("\nNo supporting documentation is provided by the requester for " + addrType
                      + " address. Please provide Supporting documentation(Company Proof) as attachment.");
                }
              }
            }
          }
        }
      }
    }
    if (resultCodes.contains("D")) {
      validation.setSuccess(false);
      validation.setMessage("Not Validated");
      engineData.addNegativeCheckStatus("_auCheckFailed", "Updates to addresses cannot be checked automatically.");
    } else if (cmdeReview) {
      engineData.addNegativeCheckStatus("DNB_MATCH_FAIL_", "Updates to addresses cannot be checked automatically.");
      validation.setSuccess(false);
      validation.setMessage("Review Required.");
    } else if (poboxReview) {
      engineData.addNegativeCheckStatus("_POBOXCheckFailed", "NZ Installing address can't contain 'PO BOX'.");
      validation.setSuccess(false);
      validation.setMessage("Review Required.");
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

  @Override
  protected List<String> getCountryLegalEndings() {
    return Arrays.asList("PTY LTD", "LTD", "company", "limited", "PT", "SDN BHD", "berhad", "CO. LTD", "company limited", "JSC", "JOINT STOCK",
        "INC.", "PTE LTD", "PVT LTD", "private limited", "CORPORATION", "hospital", "university");
  }

  @Override
  public void emptyINAC(EntityManager entityManager, RequestData requestData, AutomationEngineData engineData) throws Exception {
    Data data = requestData.getData();
    LOG.debug("INAC code value " + data.getInacCd());
    data.setInacCd("");
    LOG.debug("INAC type value " + data.getInacType());
    data.setInacType("");
  }

}
