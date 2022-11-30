package com.ibm.cio.cmr.request.automation.util.geo;

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

  private static final List<String> RELEVANT_ADDRESSES = Arrays.asList(CmrConstants.RDC_SOLD_TO, CmrConstants.RDC_BILL_TO,
      CmrConstants.RDC_INSTALL_AT, "STAT", "MAIL", "ZF01", "PUBS", "PUBB", "EDUC", "CTYG", "CTYH");
  private static final List<String> NON_RELEVANT_ADDRESS_FIELDS = Arrays.asList("Attn", "Phone #", "Customer Name", "Customer Name Con't");

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

  @Override
  public boolean performScenarioValidation(EntityManager entityManager, RequestData requestData, AutomationEngineData engineData,
      AutomationResult<ValidationOutput> result, StringBuilder details, ValidationOutput output) {

    Data data = requestData.getData();
    String scenario = data.getCustSubGrp();

    processSkipCompanyChecks(engineData, requestData, details);
    switch (scenario) {

    case SCENARIO_PRIVATE_CUSTOMER:
      engineData.addPositiveCheckStatus(AutomationEngineData.SKIP_GBG);
      break;
    case SCENARIO_BLUEMIX:
    case SCENARIO_MARKETPLACE:
    case SCENARIO_INTERNAL:
      engineData.addPositiveCheckStatus(AutomationEngineData.SKIP_GBG);
      break;
    case SCENARIO_DUMMY:
      engineData.addPositiveCheckStatus(AutomationEngineData.SKIP_GBG);
      break;
    case SCENARIO_ACQUISITION:
    case SCENARIO_NORMAL:
    case SCENARIO_ESOSW:
    case SCENARIO_ECOSYS:
    case SCENARIO_CROSS_FOREIGN:
      // addToNotifyListANZ(entityManager, data.getId().getReqId());
    }
    return true;
  }

  @Override
  public AutomationResult<OverrideOutput> doCountryFieldComputations(EntityManager entityManager, AutomationResult<OverrideOutput> results,
      StringBuilder details, OverrideOutput overrides, RequestData requestData, AutomationEngineData engineData) throws Exception {
    // get request admin and data
    StringBuilder eleResults = new StringBuilder();

    if (results != null && !results.isOnError()) {

    } else {
      eleResults.append("Error On Field Calculation.");
    }
    results.setResults(eleResults.toString());
    results.setDetails(details.toString());
    results.setProcessOutput(overrides);

    return results;
  }

  private AutomationResponse<NZBNValidationResponse> getVatLayerInfo(Admin admin, Data data) throws Exception {
    AutomationServiceClient client = CmrServicesFactory.getInstance().createClient(SystemConfiguration.getValue("BATCH_SERVICES_URL"),
        AutomationServiceClient.class);
    client.setReadTimeout(1000 * 60 * 5);
    client.setRequestMethod(Method.Get);

    NZBNValidationRequest request = new NZBNValidationRequest();
    request.setBusinessNumber(data.getVat());
    System.out.println(request + request.getBusinessNumber());
    AutomationResponse<?> rawResponse = client.executeAndWrap(AutomationServiceClient.NZ_BN_VALIDATION_SERVICE_ID, request, AutomationResponse.class);
    ObjectMapper mapper = new ObjectMapper();
    String json = mapper.writeValueAsString(rawResponse);
    TypeReference<AutomationResponse<NZBNValidationRequest>> ref = new TypeReference<AutomationResponse<NZBNValidationRequest>>() {
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
    // CustNmChanged = false;
    if (CustNmChanged) {
      AutomationResponse<NZBNValidationResponse> response = null;
      Addr zs01 = requestData.getAddress("ZS01");
      String regex = "\\s+$";
      String customerName = zs01.getCustNm1() + (StringUtils.isBlank(zs01.getCustNm2()) ? "" : " " + zs01.getCustNm2());
      String formerCustName = !StringUtils.isBlank(admin.getOldCustNm1()) ? admin.getOldCustNm1().toUpperCase() : "";
      formerCustName += !StringUtils.isBlank(admin.getOldCustNm2()) ? " " + admin.getOldCustNm2().toUpperCase() : "";
      // List<String> dnbTradestyleNames = new ArrayList<String>();
      boolean custNmMatch = false;
      // boolean formerCustNmMatch = false;
      try {

        if (!(custNmMatch)) {
          LOG.debug("CustNm match failed with API. Now Checking with DNB to vrify CustNm update");
          MatchingResponse<DnBMatchingResponse> Dnbresponse = DnBUtil.getMatches(requestData, null, "ZS01");
          List<DnBMatchingResponse> matches = Dnbresponse.getMatches();
          if (!matches.isEmpty()) {
            for (DnBMatchingResponse dnbRecord : matches) {
              // checks for CustNm match
              String dnbCustNm = StringUtils.isBlank(dnbRecord.getDnbName()) ? "" : dnbRecord.getDnbName().replaceAll("\\s+$", "");
              if (customerName.equalsIgnoreCase(dnbCustNm)) {
                custNmMatch = true;
              }
            }
          }
        }
        if (!(custNmMatch)) {
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
              if (StringUtils.isNotEmpty(response.getRecord().getName())) {
                String responseCustNm = StringUtils.isBlank(response.getRecord().getName()) ? ""
                    : response.getRecord().getName().replaceAll(regex, "");

                if (!(custNmMatch)) {
                  if (customerName.equalsIgnoreCase(responseCustNm)) {
                    custNmMatch = true;

                  }
                }
              }
            }
          }

        }
        // }
        // customerNm Validation
        details.append("\nUpdates to the non Relevant dataFields fields skipped validation \n\n");
        if (custNmMatch) {
          validation.setSuccess(true);
          validation.setMessage("Successful");
          output.setProcessOutput(validation);
          output.setDetails("Updates to the CustomerNm field Verified");

        } else {
          validation.setMessage("Not Validated");
          details.append("The Customer Name doesn't match from API & DNB");
          // company proof
          if (DnBUtil.isDnbOverrideAttachmentProvided(entityManager, admin.getId().getReqId())) {
            details.append("\nSupporting documentation is provided by the requester as attachment for " + customerName).append("\n");
          } else {
            details.append("\nNo supporting documentation is provided by the requester for customer name update from " + formerCustName + " to "
                + customerName + " update. Please provide Supporting documentation(Company Proof) as attachment.");
          }
          output.setDetails(details.toString());
          engineData.addNegativeCheckStatus("ABNLegalName", "The Customer Name doesn't match from API & DNB");
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
          if (CmrConstants.RDC_SOLD_TO.equals(addressToChk.getId().getAddrType())) {
            String addressStr = addressToChk.getCustNm1() + addressToChk.getCustNm2() == null ? ""
                : addressToChk.getCustNm2() + addressToChk.getAddrTxt() + addressToChk.getAddrTxt2() == null ? "" : addressToChk.getAddrTxt2();
            if (addressStr != null && addressStr.contains("PO BOX")) {
              poboxReview = true;
            }
          }
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
                    checkDetails.append("\nNo supporting documentation is provided by the requester for " + addrType
                        + " address. Please provide Supporting documentation(Company Proof) as attachment.");
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
                checkDetails.append("\nNo supporting documentation is provided by the requester for " + addrType
                    + " address. Please provide Supporting documentation(Company Proof) as attachment.");
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
}
