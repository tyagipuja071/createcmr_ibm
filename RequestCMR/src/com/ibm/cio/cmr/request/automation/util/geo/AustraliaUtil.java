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
import com.ibm.cio.cmr.request.entity.NotifList;
import com.ibm.cio.cmr.request.entity.NotifListPK;
import com.ibm.cio.cmr.request.entity.listeners.ChangeLogListener;
import com.ibm.cio.cmr.request.model.window.UpdatedNameAddrModel;
import com.ibm.cio.cmr.request.util.SystemLocation;
import com.ibm.cio.cmr.request.util.SystemParameters;
import com.ibm.cio.cmr.request.util.dnb.DnBUtil;
import com.ibm.cmr.services.client.AutomationServiceClient;
import com.ibm.cmr.services.client.CmrServicesFactory;
import com.ibm.cmr.services.client.ServiceClient.Method;
import com.ibm.cmr.services.client.automation.AutomationResponse;
import com.ibm.cmr.services.client.automation.ap.anz.BNValidationRequest;
import com.ibm.cmr.services.client.automation.ap.anz.BNValidationResponse;
import com.ibm.cmr.services.client.automation.ap.anz.NMValidationRequest;
import com.ibm.cmr.services.client.matching.MatchingResponse;
import com.ibm.cmr.services.client.matching.dnb.DnBMatchingResponse;

public class AustraliaUtil extends AutomationUtil {

  private static final Logger LOG = Logger.getLogger(AustraliaUtil.class);

  private static final List<String> ALLOW_DEFAULT_SCENARIOS = Arrays.asList("PRIV", "XPRIV", "BLUMX", "MKTPC", "XBLUM", "XMKTP");
  private static final List<String> RELEVANT_ADDRESSES = Arrays.asList(CmrConstants.RDC_SOLD_TO, CmrConstants.RDC_BILL_TO,
      CmrConstants.RDC_INSTALL_AT, "STAT", "MAIL", "ZF01", "PUBS", "PUBB", "EDUC", "CTYG", "CTYH");
  private static final List<String> NON_RELEVANT_ADDRESS_FIELDS = Arrays.asList("Attn", "Phone #", "Customer Name", "Customer Name Con't");

  public static final String SCENARIO_BLUEMIX = "BLUMX";
  public static final String SCENARIO_MARKETPLACE = "MKTPC";
  private static final String SCENARIO_PRIVATE_CUSTOMER = "PRIV";
  private static final String SCENARIO_DUMMY = "DUMMY";
  private static final String SCENARIO_INTERNAL = "INTER";
  private static final String SCENARIO_ECOSYS = "ECSYS";
  private static final String SCENARIO_CROSS_ECOSYS = "XECO";

  @Override
  public AutomationResult<OverrideOutput> doCountryFieldComputations(EntityManager entityManager, AutomationResult<OverrideOutput> results,
      StringBuilder details, OverrideOutput overrides, RequestData requestData, AutomationEngineData engineData) throws Exception {
    // get request admin and data
    long reqId = requestData.getAdmin().getId().getReqId();
    Admin admin = requestData.getAdmin();
    Data data = requestData.getData();
    Addr soldTo = requestData.getAddress("ZS01");
    StringBuilder eleResults = new StringBuilder();
    String[] isicScenarioList = { "NRML", "ESOSW", "IGF", "XIGF", "CROSS", "AQSTN", "XAQST", "SOFT" };
    boolean isIsicInvalid = false;
    LOG.debug("Australia : Performing field computations for req_id : " + reqId);
    String defaultClusterCd = getDefaultCluster(entityManager, requestData, engineData);
    String validCode = checkIfClusterSalesmanIsValid(entityManager, requestData);

    // a. check cluster
    if (defaultClusterCd.equalsIgnoreCase(data.getApCustClusterId())) {

      if ("9500".equals(data.getIsicCd()) || ALLOW_DEFAULT_SCENARIOS.contains(data.getCustSubGrp())) {
        LOG.debug("Default Cluster used but allowed: ISIC=" + data.getIsicCd() + " Scenario=" + data.getCustSubGrp());
        results.setOnError(false);
        // eleResults.append("Default Cluster used.\n");
        details.append("Default Cluster used but allowed for the request (Private Person/BlueMix/Marketplace).\n");
      } else {
        LOG.debug("Default Cluster used.");
        engineData.addRejectionComment("OTH", "Cluster should not be the default cluster for the scenario.", "", "");
        results.setOnError(true);
        // eleResults.append("Default Cluster used.\n");
        details.append("Cluster should not be the default cluster for the scenario.\n");
      }
    } else {
      LOG.debug("Default Cluster NOT used.");
      // eleResults.append("Default Cluster not used.\n");
      details.append("Cluster used is not default.\n");
    }

    if ("9500".equals(data.getIsicCd()) || ALLOW_DEFAULT_SCENARIOS.contains(data.getCustSubGrp())) {
      LOG.debug("Salesman check skipped for private and allowed scenarios.");
      // eleResults.append("\nValid cluster and Salesman No. used.\n");
      details.append("\nSalesman check skipped for this scenario (Private/Marketplace/Bluemix).\n");
    } // else {

    // CREATCMR-6825
    // b. check cluster and salesman no. combination
    // if ("VALID".equalsIgnoreCase(validCode)) {
    // LOG.debug("The combination of Salesman No. and Cluster is valid.");
    // // eleResults.append("\nValid cluster and Salesman No. used.\n");
    // details.append("\nThe combination of Salesman No. and Cluster is
    // valid.\n");
    // } else if ("INVALID".equalsIgnoreCase(validCode)) {
    // LOG.debug("The combination of Salesman No. and Cluster is INVALID.");
    // engineData.addRejectionComment("OTH", "The combination of Salesman No.
    // and Cluster is invalid.", "", "");
    // results.setOnError(true);
    // // eleResults.append("Invalid Cluster and Salesman No.
    // combination.\n");
    // details.append("\nThe combination of Salesman No. and Cluster is
    // invalid.\n");
    // } else {
    // LOG.debug("Salesman No.-Cluster combination not present.");
    // /*
    // * engineData.addRejectionComment("OTH",
    // * "No combination of Salesman No. and Cluster is present.", "", "");
    // * results.setOnError(true);
    // */
    // // eleResults.append("No combination of Salesman No. and Cluster
    // // present.\n");
    // details.append("\nNo combination of Salesman No. and Cluster is
    // present.\n");
    // }
    // }
    isIsicInvalid = isISICValidForScenario(requestData, Arrays.asList(isicScenarioList));

    if (isIsicInvalid) {
      details.append("Invalid ISIC code, please choose another one based on industry.\n");
      engineData.addRejectionComment("OTH", "Invalid ISIC code, please choose another one based on industry.", "", "");
      results.setOnError(true);
    } else {
      details.append("ISIC is valid" + "\n");
    }

    if (results != null && !results.isOnError()) {

    } else {
      eleResults.append("Error On Field Calculation.");
    }
    results.setResults(eleResults.toString());
    results.setDetails(details.toString());
    results.setProcessOutput(overrides);

    return results;
  }

  private AutomationResponse<BNValidationResponse> getVatLayerInfo(Admin admin, Data data) throws Exception {
    AutomationServiceClient client = CmrServicesFactory.getInstance().createClient(SystemConfiguration.getValue("BATCH_SERVICES_URL"),
        AutomationServiceClient.class);
    client.setReadTimeout(1000 * 60 * 5);
    client.setRequestMethod(Method.Get);

    BNValidationRequest request = new BNValidationRequest();
    request.setBusinessNumber(data.getVat());
    System.out.println(request + request.getBusinessNumber());
    AutomationResponse<?> rawResponse = client.executeAndWrap(AutomationServiceClient.AU_ABN_VALIDATION_SERVICE_ID, request,
        AutomationResponse.class);
    ObjectMapper mapper = new ObjectMapper();
    String json = mapper.writeValueAsString(rawResponse);
    TypeReference<AutomationResponse<BNValidationResponse>> ref = new TypeReference<AutomationResponse<BNValidationResponse>>() {
    };
    return mapper.readValue(json, ref);

  }

  private AutomationResponse<BNValidationResponse> getAuCustNmLayerInfo(String customerName) throws Exception {
    AutomationServiceClient client = CmrServicesFactory.getInstance().createClient(SystemConfiguration.getValue("BATCH_SERVICES_URL"),
        AutomationServiceClient.class);
    client.setReadTimeout(1000 * 60 * 5);
    client.setRequestMethod(Method.Get);

    NMValidationRequest request = new NMValidationRequest();
    request.setCustNm(customerName);
    System.out.println(request + request.getCustNm());
    AutomationResponse<?> rawResponse = client.executeAndWrap(AutomationServiceClient.AU_NM_VALIDATION_SERVICE_ID, request, AutomationResponse.class);
    ObjectMapper mapper = new ObjectMapper();
    String json = mapper.writeValueAsString(rawResponse);
    TypeReference<AutomationResponse<BNValidationResponse>> ref = new TypeReference<AutomationResponse<BNValidationResponse>>() {
    };
    return mapper.readValue(json, ref);
  }

  @Override
  public boolean runUpdateChecksForData(EntityManager entityManager, AutomationEngineData engineData, RequestData requestData,
      RequestChangeContainer changes, AutomationResult<ValidationOutput> output, ValidationOutput validation) throws Exception {
    Admin admin = requestData.getAdmin();
    Data data = requestData.getData();
    StringBuilder details = new StringBuilder();
    boolean CustNmChanged = changes.isLegalNameChanged();
    ChangeLogListener.setManager(entityManager);

    if (CustNmChanged) {
      AutomationResponse<BNValidationResponse> response = null;
      Addr zs01 = requestData.getAddress("ZS01");
      String regex = "\\s+$";
      String customerName = zs01.getCustNm1() + (StringUtils.isBlank(zs01.getCustNm2()) ? "" : " " + zs01.getCustNm2());
      String formerCustName = !StringUtils.isBlank(admin.getOldCustNm1()) ? admin.getOldCustNm1().toUpperCase() : "";
      formerCustName += !StringUtils.isBlank(admin.getOldCustNm2()) ? " " + admin.getOldCustNm2().toUpperCase() : "";
      List<String> dnbTradestyleNames = new ArrayList<String>();
      boolean custNmMatch = false;
      boolean formerCustNmMatch = false;
      try {
        try {
          response = getVatLayerInfo(admin, data);
        } catch (Exception e) {
          if (response == null || !response.isSuccess()
              && "Parameter 'businessNumber' is required by the service to verify CustNm change.".equalsIgnoreCase(response.getMessage())) {
            LOG.debug("\nFailed to Connect to ABN Service.Now Checking with DNB to vrify CustNm update");
          }
        }

        if (!StringUtils.isBlank(data.getVat())) {
          if (response != null && response.isSuccess()) {
            // custNm Validation
            if (response.getRecord().isValid()) {
              String responseCustNm = StringUtils.isBlank(response.getRecord().getCompanyName()) ? ""
                  : response.getRecord().getCompanyName().replaceAll(regex, "");
              String responseTradingNm = StringUtils.isBlank(response.getRecord().getTradingName()) ? ""
                  : response.getRecord().getTradingName().replaceAll(regex, "");
              String responseOthTradingNm = StringUtils.isBlank(response.getRecord().getOtherTradingName()) ? ""
                  : response.getRecord().getOtherTradingName().replaceAll(regex, "");
              String responseBusinessNm = StringUtils.isBlank(response.getRecord().getBusinessName()) ? ""
                  : response.getRecord().getBusinessName().replaceAll(regex, "");
              List<String> historicalNameList = new ArrayList<String>();
              historicalNameList = response.getRecord().getHistoricalNameList();
              for (String historicalNm : historicalNameList) {
                historicalNm = historicalNm.replaceAll(regex, "");
                if (response.getRecord().isValid() && customerName.equalsIgnoreCase(responseCustNm)
                    && formerCustName.equalsIgnoreCase(historicalNm)) {
                  custNmMatch = true;
                  formerCustNmMatch = true;
                }
              }
              if (!(custNmMatch && formerCustNmMatch)) {
                if (response.getRecord().isValid() && (customerName.equalsIgnoreCase(responseCustNm))
                    && ((formerCustName.equalsIgnoreCase(responseTradingNm)) || (formerCustName.equalsIgnoreCase(responseOthTradingNm))
                        || (formerCustName.equalsIgnoreCase(responseBusinessNm)))) {
                  custNmMatch = true;
                  formerCustNmMatch = true;
                } else if (response.getRecord().isValid() && (customerName.equalsIgnoreCase(responseCustNm))
                    && !((formerCustName.equalsIgnoreCase(responseTradingNm)) || (formerCustName.equalsIgnoreCase(responseOthTradingNm))
                        || (formerCustName.equalsIgnoreCase(responseBusinessNm)))) {
                  custNmMatch = true;
                  formerCustNmMatch = false;
                } else if (response.getRecord().isValid() && !(customerName.equalsIgnoreCase(responseCustNm))
                    && ((formerCustName.equalsIgnoreCase(responseTradingNm)) || (formerCustName.equalsIgnoreCase(responseOthTradingNm))
                        || (formerCustName.equalsIgnoreCase(responseBusinessNm)))) {
                  custNmMatch = false;
                  formerCustNmMatch = true;
                } else {
                  custNmMatch = false;
                  formerCustNmMatch = false;
                }
              }
            }
          }
        }
        if (!(custNmMatch && formerCustNmMatch)) {
          try {
            response = getAuCustNmLayerInfo(customerName);
          } catch (Exception e) {
            if (response == null || !response.isSuccess()
                && "Parameter 'CustomerName' is required by the service to verify CustNm change.".equalsIgnoreCase(response.getMessage())) {
              LOG.debug("\nFailed to Connect to AU Customer Name Service.Now Checking with DNB to vrify CustNm update");
            }
          }

          if (response != null && response.isSuccess()) {
            // custNm Validation
            if (response.getRecord().isValid()) {
              String responseCustNm = StringUtils.isBlank(response.getRecord().getCompanyName()) ? ""
                  : response.getRecord().getCompanyName().replaceAll(regex, "");
              String responseTradingNm = StringUtils.isBlank(response.getRecord().getTradingName()) ? ""
                  : response.getRecord().getTradingName().replaceAll(regex, "");
              String responseOthTradingNm = StringUtils.isBlank(response.getRecord().getOtherTradingName()) ? ""
                  : response.getRecord().getOtherTradingName().replaceAll(regex, "");
              String responseBusinessNm = StringUtils.isBlank(response.getRecord().getBusinessName()) ? ""
                  : response.getRecord().getBusinessName().replaceAll(regex, "");
              if (response.getRecord().isValid() && (customerName.equalsIgnoreCase(responseCustNm))
                  && ((formerCustName.equalsIgnoreCase(responseTradingNm)) || (formerCustName.equalsIgnoreCase(responseOthTradingNm))
                      || (formerCustName.equalsIgnoreCase(responseBusinessNm)))) {
                custNmMatch = true;
                formerCustNmMatch = true;
              } else if (response.getRecord().isValid() && (customerName.equalsIgnoreCase(responseCustNm))
                  && !((formerCustName.equalsIgnoreCase(responseTradingNm)) || (formerCustName.equalsIgnoreCase(responseOthTradingNm))
                      || (formerCustName.equalsIgnoreCase(responseBusinessNm)))) {
                custNmMatch = true;
                formerCustNmMatch = false;
              } else if (response.getRecord().isValid() && !(customerName.equalsIgnoreCase(responseCustNm))
                  && ((formerCustName.equalsIgnoreCase(responseTradingNm)) || (formerCustName.equalsIgnoreCase(responseOthTradingNm))
                      || (formerCustName.equalsIgnoreCase(responseBusinessNm)))) {
                custNmMatch = false;
                formerCustNmMatch = true;
              } else {
                custNmMatch = false;
                formerCustNmMatch = false;
              }
            }
          }
          if (!(custNmMatch && formerCustNmMatch)) {
            LOG.debug("CustNm and formerCustNm match failed with API.Now Checking with DNB to vrify CustNm update");
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
        }
        // customerNm Validation
        details.append("\nUpdates to the non Relevant dataFields fields skipped validation \n\n");
        if (custNmMatch && formerCustNmMatch) {
          validation.setSuccess(true);
          validation.setMessage("Successful");
          output.setProcessOutput(validation);
          output.setDetails("Updates to the CustomerNm field Verified");
        } else if (custNmMatch && !formerCustNmMatch) {
          validation.setMessage("Not Validated");
          details.append("The Customer Name on the request verified but former Customer Name does not match from API & DNB service.");
          // company proof
          if (DnBUtil.isDnbOverrideAttachmentProvided(entityManager, admin.getId().getReqId())) {
            details.append("\nSupporting documentation is provided by the requester as attachment for " + customerName).append("\n");
          } else {
            details.append("\nNo supporting documentation is provided by the requester for customer name update from " + formerCustName + " to "
                + customerName + " update.");
          }
          output.setDetails(details.toString());
          engineData.addNegativeCheckStatus("ABNLegalName", "Former Customer name doesn't matches from API & DNB match");
        } else if (!custNmMatch && formerCustNmMatch) {
          validation.setMessage("Not Validated");
          details.append("The Former Customer Name on the request matches but Customer Name does not match match from API & DNB.");
          // company proof
          if (DnBUtil.isDnbOverrideAttachmentProvided(entityManager, admin.getId().getReqId())) {
            details.append("\nSupporting documentation is provided by the requester as attachment for " + customerName).append("\n");
          } else {
            details.append("\nNo supporting documentation is provided by the requester for customer name update from " + formerCustName + " to "
                + customerName + " update.");
          }
          output.setDetails(details.toString());
          engineData.addNegativeCheckStatus("ABNLegalName", "Customer name doesn't matches from API & DNB match");
        } else {
          validation.setMessage("Not Validated");
          details.append("The Customer Name and Former Customer Name doesn't match from API & DNB");
          // company proof
          if (DnBUtil.isDnbOverrideAttachmentProvided(entityManager, admin.getId().getReqId())) {
            details.append("\nSupporting documentation is provided by the requester as attachment for " + customerName).append("\n");
          } else {
            details.append("\nNo supporting documentation is provided by the requester for customer name update from " + formerCustName + " to "
                + customerName + " update.");
          }
          output.setDetails(details.toString());
          engineData.addNegativeCheckStatus("ABNLegalName", "The Customer Name and Former Customer Name doesn't match from API & DNB");
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

    boolean skipCompanyChecks = "9500".equals(data.getIsicCd()) || (data.getCustSubGrp() != null && data.getCustSubGrp().contains("PRIV"));
    if (skipCompanyChecks) {
      details.append("Private Person request - company checks will be skipped.\n");
      ScenarioExceptionsUtil exc = (ScenarioExceptionsUtil) engineData.get("SCENARIO_EXCEPTIONS");
      if (exc != null) {
        exc.setSkipCompanyVerification(true);
        engineData.put("SCENARIO_EXCEPTIONS", exc);
      }
    }
  }

  @Override
  public boolean performScenarioValidation(EntityManager entityManager, RequestData requestData, AutomationEngineData engineData,
      AutomationResult<ValidationOutput> result, StringBuilder details, ValidationOutput output) {
    // String[] scnarioList = { "XIGF", "CROSS", "MKTPC", "BLUMX", "DUMMY",
    // "XDUMM", "INTER", "XINT", "AQSTN", "XAQST", "SOFT" };
    // skipCompanyCheckForScenario(requestData, engineData,
    // Arrays.asList(scnarioList), true);
    // scenario check for normal and ESOSW
    Data data = requestData.getData();
    String scenario = data.getCustSubGrp();
    String scenarioList[] = { "NRML", "ESOSW" };
    Addr soldTo = requestData.getAddress("ZS01");
    String custNm1 = soldTo.getCustNm1();
    String custNm2 = StringUtils.isNotBlank(soldTo.getCustNm2()) ? " " + soldTo.getCustNm2() : "";
    String customerName = custNm1 + custNm2;

    // allowDuplicatesForScenario(engineData, requestData,
    // Arrays.asList(scenarioList));
    processSkipCompanyChecks(engineData, requestData, details);
    switch (scenario) {
    // CREATCMR - 2031
    // case SCENARIO_BLUEMIX:
    // case SCENARIO_MARKETPLACE:
    // engineData.addPositiveCheckStatus(AutomationEngineData.SKIP_GBG);
    // break;
    // CREATCMR - 5772
    case SCENARIO_BLUEMIX:
    case SCENARIO_MARKETPLACE:
    case SCENARIO_DUMMY:
    case SCENARIO_INTERNAL:
      engineData.addPositiveCheckStatus(AutomationEngineData.SKIP_COVERAGE);
      break;
    case SCENARIO_PRIVATE_CUSTOMER:
      engineData.addPositiveCheckStatus(AutomationEngineData.SKIP_COVERAGE);
      return doPrivatePersonChecks(engineData, SystemLocation.AUSTRALIA, soldTo.getLandCntry(), customerName, details, false, requestData);
    case SCENARIO_ECOSYS:
    case SCENARIO_CROSS_ECOSYS:
      addToNotifyListANZ(entityManager, data.getId().getReqId());
    }
    return true;
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

  private AutomationResponse<BNValidationResponse> getBNInfo(Admin admin, Data data) throws Exception {
    AutomationServiceClient client = CmrServicesFactory.getInstance().createClient(SystemConfiguration.getValue("BATCH_SERVICES_URL"),
        AutomationServiceClient.class);
    client.setReadTimeout(1000 * 60 * 5);
    client.setRequestMethod(Method.Get);

    BNValidationRequest request = new BNValidationRequest();
    request.setBusinessNumber(data.getVat());
    System.out.println(request + request.getBusinessNumber());

    LOG.debug("Connecting to the BNValidation service at " + SystemConfiguration.getValue("BATCH_SERVICES_URL"));
    AutomationResponse<?> rawResponse = client.executeAndWrap(AutomationServiceClient.AU_ABN_VALIDATION_SERVICE_ID, request,
        AutomationResponse.class);
    ObjectMapper mapper = new ObjectMapper();
    String json = mapper.writeValueAsString(rawResponse);
    TypeReference<AutomationResponse<BNValidationResponse>> ref = new TypeReference<AutomationResponse<BNValidationResponse>>() {
    };
    return mapper.readValue(json, ref);
  }

  public static void addToNotifyListANZ(EntityManager entityManager, long reqId) {
    StringBuilder anzEcoNotifyList = getANZEcoNotifyList();
    List<String> users = Arrays.asList(anzEcoNotifyList.toString().split("\\s*,\\s*"));
    for (String user : users) {
      NotifList notif = new NotifList();
      NotifListPK pk = new NotifListPK();
      pk.setReqId(reqId);
      pk.setNotifId(user);
      notif.setId(pk);
      notif.setNotifNm(user);
      entityManager.merge(notif);
    }
  }

  @Override
  public boolean runUpdateChecksForAddress(EntityManager entityManager, AutomationEngineData engineData, RequestData requestData,
      RequestChangeContainer changes, AutomationResult<ValidationOutput> output, ValidationOutput validation) throws Exception {
    Admin admin = requestData.getAdmin();
    Data data = requestData.getData();
    boolean cmdeReview = false;
    if (handlePrivatePersonRecord(entityManager, admin, output, validation, engineData)) {
      return true;
    }
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
          if ("Y".equals(addr.getChangedIndc())) {
            // update address
            if (RELEVANT_ADDRESSES.contains(addrType)) {
              if (isRelevantAddressFieldUpdated(changes, addr)) {
                List<DnBMatchingResponse> matches = getMatches(requestData, engineData, addressToChk, false);
                boolean matchesDnb = false;
                if (matches != null) {
                  // check against D&B
                  matchesDnb = ifaddressCloselyMatchesDnb(matches, addr, admin, data.getCmrIssuingCntry());
                }
                if (!matchesDnb) {
                  LOG.debug("Update address for " + addrType + "(" + addr.getId().getAddrSeq() + ") does not match D&B");
                  cmdeReview = true;
                  checkDetails.append("\nUpdate address " + addrType + "(" + addr.getId().getAddrSeq() + ") did not match D&B records.\n");
                  // company proof
                  if (DnBUtil.isDnbOverrideAttachmentProvided(entityManager, admin.getId().getReqId())) {
                    checkDetails.append("\nSupporting documentation is provided by the requester as attachment for " + addrType).append("\n");
                  } else {
                    checkDetails.append("\nNo supporting documentation is provided by the requester for " + addrType + " address.");
                  }

                } else {
                  checkDetails.append("\nUpdate address " + addrType + "(" + addr.getId().getAddrSeq() + ") matches D&B records. Matches:\n");
                  for (DnBMatchingResponse dnb : matches) {
                    checkDetails.append(" - DUNS No.:  " + dnb.getDunsNo() + " \n");
                    checkDetails.append(" - Name.:  " + dnb.getDnbName() + " \n");
                    checkDetails.append(" - Address:  " + dnb.getDnbStreetLine1() + " " + dnb.getDnbCity() + " " + dnb.getDnbPostalCode() + " "
                        + dnb.getDnbCountry() + "\n\n");
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

            List<DnBMatchingResponse> matches = getMatches(requestData, engineData, addressToChk, false);
            boolean matchesDnb = false;
            if (matches != null) {
              // check against D&B
              matchesDnb = ifaddressCloselyMatchesDnb(matches, addr, admin, data.getCmrIssuingCntry());
            }

            if (!matchesDnb) {
              LOG.debug("New address for " + addrType + "(" + addr.getId().getAddrSeq() + ") does not match D&B");
              cmdeReview = true;
              checkDetails.append("\nNew address " + addrType + "(" + addr.getId().getAddrSeq() + ") did not match D&B.\n");
              // company proof
              if (DnBUtil.isDnbOverrideAttachmentProvided(entityManager, admin.getId().getReqId())) {
                checkDetails.append("Supporting documentation is provided by the requester as attachment for " + addrType).append("\n");
              } else {
                checkDetails.append("\nNo supporting documentation is provided by the requester for " + addrType + " address.");
              }
            } else {
              checkDetails.append("\nNew address " + addrType + "(" + addr.getId().getAddrSeq() + ") matches D&B records. Matches:\n");
              for (DnBMatchingResponse dnb : matches) {
                checkDetails.append(" - DUNS No.:  " + dnb.getDunsNo() + " \n");
                checkDetails.append(" - Name.:  " + dnb.getDnbName() + " \n");
                checkDetails.append(" - Address:  " + dnb.getDnbStreetLine1() + " " + dnb.getDnbCity() + " " + dnb.getDnbPostalCode() + " "
                    + dnb.getDnbCountry() + "\n\n");
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

  private static StringBuilder getANZEcoNotifyList() {
    StringBuilder anzEcoNotifyList = new StringBuilder();
    anzEcoNotifyList.append(SystemParameters.getString("ANZ_ECSYS_NOTIFY"));
    return anzEcoNotifyList;
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
