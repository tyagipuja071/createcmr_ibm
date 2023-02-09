/**
 * 
 */
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
import com.ibm.cio.cmr.request.automation.AutomationElementRegistry;
import com.ibm.cio.cmr.request.automation.AutomationEngineData;
import com.ibm.cio.cmr.request.automation.RequestData;
import com.ibm.cio.cmr.request.automation.impl.gbl.CalculateCoverageElement;
import com.ibm.cio.cmr.request.automation.out.AutomationResult;
import com.ibm.cio.cmr.request.automation.out.FieldResultKey;
import com.ibm.cio.cmr.request.automation.out.OverrideOutput;
import com.ibm.cio.cmr.request.automation.out.ValidationOutput;
import com.ibm.cio.cmr.request.automation.util.AutomationUtil;
import com.ibm.cio.cmr.request.automation.util.CoverageContainer;
import com.ibm.cio.cmr.request.automation.util.RequestChangeContainer;
import com.ibm.cio.cmr.request.automation.util.ScenarioExceptionsUtil;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.model.window.UpdatedDataModel;
import com.ibm.cio.cmr.request.model.window.UpdatedNameAddrModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.util.SystemParameters;
import com.ibm.cio.cmr.request.util.dnb.DnBUtil;
import com.ibm.cmr.services.client.AutomationServiceClient;
import com.ibm.cmr.services.client.CmrServicesFactory;
import com.ibm.cmr.services.client.ServiceClient.Method;
import com.ibm.cmr.services.client.automation.AutomationResponse;
import com.ibm.cmr.services.client.automation.in.GstLayerRequest;
import com.ibm.cmr.services.client.automation.in.GstLayerResponse;
import com.ibm.cmr.services.client.matching.MatchingResponse;
import com.ibm.cmr.services.client.matching.cmr.DuplicateCMRCheckResponse;
import com.ibm.cmr.services.client.matching.dnb.DnBMatchingResponse;

public class IndiaUtil extends AutomationUtil {

  public static final String SCENARIO_ESOSW = "ESOSW";
  public static final String SCENARIO_NORMAL = "NRML";
  public static final String SCENARIO_INTERNAL = "INTER";
  public static final String SCENARIO_ACQUISITION = "AQSTN";
  public static final String SCENARIO_DUMMY = "DUMMY";
  public static final String SCENARIO_IGF = "IGF";
  public static final String SCENARIO_PRIVATE_CUSTOMER = "PRIV";
  public static final String SCENARIO_FOREIGN = "CROSS";
  public static final String SCENARIO_BLUEMIX = "BLUMX";
  public static final String SCENARIO_MARKETPLACE = "MKTPC";
  private static final List<String> India_LEGAL_ENDINGS = Arrays.asList("PRIVATE LIMITED", "ORGANIZATION", "ORGANISATION", "INC.", "ORG.",
      "INCORPORATE", "COMPANY", "CORP.", "CORPORATION", "LIMITED", "LTD", "PVT", "PRIVATE", "PVT LTD");
  private static final List<String> RELEVANT_ADDRESSES = Arrays.asList(CmrConstants.RDC_SOLD_TO, CmrConstants.RDC_BILL_TO,
      CmrConstants.RDC_INSTALL_AT, CmrConstants.RDC_SECONDARY_SHIPPING);
  private static final List<String> NON_RELEVANT_ADDRESS_FIELDS = Arrays.asList("Attn", "Phone #");

  private static final Logger LOG = Logger.getLogger(IndiaUtil.class);

  @Override
  public AutomationResult<OverrideOutput> doCountryFieldComputations(EntityManager entityManager, AutomationResult<OverrideOutput> results,
      StringBuilder details, OverrideOutput overrides, RequestData requestData, AutomationEngineData engineData) throws Exception {
    // TODO Auto-generated method stub
    Admin admin = requestData.getAdmin();
    Data data = requestData.getData();
    String scenario = data.getCustSubGrp();
    if (!"C".equals(admin.getReqType())) {
      details.append("Field Computation skipped for Updates.");
      results.setResults("Skipped");
      results.setDetails(details.toString());
      return results;
    }
    if ("C".equals(admin.getReqType()) && SCENARIO_IGF.equals(scenario)) {
      overrides.addOverride(AutomationElementRegistry.GBL_FIELD_COMPUTE, "DATA", "ISIC_CD", data.getIsicCd(), "8888");
    }
    details.append("No specific fields to calculate.");
    results.setResults("Skipped.");
    results.setProcessOutput(overrides);
    results.setDetails(details.toString());
    LOG.debug(results.getDetails());
    return results;
  }

  @Override
  public boolean performScenarioValidation(EntityManager entityManager, RequestData requestData, AutomationEngineData engineData,
      AutomationResult<ValidationOutput> result, StringBuilder details, ValidationOutput output) {
    Data data = requestData.getData();
    String scenario = data.getCustSubGrp();
    ScenarioExceptionsUtil scenarioExceptions = (ScenarioExceptionsUtil) engineData.get("SCENARIO_EXCEPTIONS");
    scenarioExceptions.setCheckVATForDnB(false);
    switch (scenario) {
    case SCENARIO_ACQUISITION:
    case SCENARIO_NORMAL:
    case SCENARIO_FOREIGN:
      if (data.getApCustClusterId().contains("08033")) {
        details.append("Cluster is set to 08033 Ecosystem Partners.").append("\n");
        details.append("Cmde review is required.").append("\n");
        engineData.addNegativeCheckStatus("OTH", "Cmde review required as cluster is set to 08033.");
        return true;
      }
      // if (data.getApCustClusterId().contains("012D999")) {
      // details.append("Cluster cannot be default as 012D999.").append("\n");
      // engineData.addRejectionComment("OTH", "Cluster cannot be default as
      // 012D999.", "", "");
      // return false;
      // }
      break;
    case SCENARIO_ESOSW:
      engineData.addNegativeCheckStatus("_atESO", "ESOSW request need to be send to CMDE queue for review. ");
      details.append("ESOSW request need to be send to CMDE queue for review. ").append("\n");
      return true;
    case SCENARIO_INTERNAL:
      for (String addrType : RELEVANT_ADDRESSES) {
        List<Addr> addresses = requestData.getAddresses(addrType);
        for (Addr addr : addresses) {
          String custNmTrimmed = getCustomerFullName(addr);
          if (!(custNmTrimmed.toUpperCase().contains("IBM INDIA") || custNmTrimmed.toUpperCase().contains("INTERNATIONAL BUSINESS MACHINES INDIA"))) {
            details.append("Customer Name in all addresses should contain IBM India for Internal Scenario.").append("\n");
            engineData.addRejectionComment("OTH", "Customer Name in all addresses should contain IBM India for Internal Scenario.", "", "");
            return false;
          }
        }
      }
      engineData.addPositiveCheckStatus(AutomationEngineData.SKIP_COVERAGE);
      break;
    case SCENARIO_PRIVATE_CUSTOMER:
      for (String addrType : RELEVANT_ADDRESSES) {
        List<Addr> addresses = requestData.getAddresses(addrType);
        for (Addr addr : addresses) {
          String custNmTrimmed = getCustomerFullName(addr).toUpperCase();
          if (hasINLegalEndings(custNmTrimmed)) {
            details.append(
                "Customer name should not contain 'Private Limited', 'Company', 'Corporation', 'Incorporate', 'Organization', 'Organisation', 'Pvt Ltd', 'Private', 'Limited', 'Pvt', 'Ltd', 'Inc.', 'Org.', 'Corp.' .")
                .append("\n");
            engineData.addRejectionComment("OTH",
                "Customer name should not contain 'Private Limited', 'Company', 'Corporation', 'Incorporate', 'Organization', 'Organisation', 'Pvt Ltd', 'Private', 'Limited', 'Pvt', 'Ltd', 'Inc.', 'Org.', 'Corp.' .",
                "", "");
            return false;
          }
        }
      }
      break;
    case SCENARIO_BLUEMIX:
    case SCENARIO_MARKETPLACE:
      engineData.addPositiveCheckStatus(AutomationEngineData.SKIP_COVERAGE);
      break;
    case SCENARIO_DUMMY:
    case SCENARIO_IGF:
      engineData.addPositiveCheckStatus(AutomationEngineData.SKIP_GBG);
      engineData.addPositiveCheckStatus(AutomationEngineData.SKIP_COVERAGE);
      break;

    }
    return true;
  }

  private boolean hasINLegalEndings(String custNmTrimmed) {
    if (custNmTrimmed == null) {
      return false;
    }

    for (String lglEnding : India_LEGAL_ENDINGS) {

      if (custNmTrimmed.contains(lglEnding)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public void filterDuplicateCMRMatches(EntityManager entityManager, RequestData requestData, AutomationEngineData engineData,
      MatchingResponse<DuplicateCMRCheckResponse> response) {
    String[] scenariosToBeChecked = { "ESOSW" };
    String scenario = requestData.getData().getCustSubGrp();
    if (Arrays.asList(scenariosToBeChecked).contains(scenario)) {
      List<DuplicateCMRCheckResponse> matches = response.getMatches();
      List<DuplicateCMRCheckResponse> filteredMatches = new ArrayList<DuplicateCMRCheckResponse>();
      for (DuplicateCMRCheckResponse match : matches) {
        if (match.getCmrNo() != null && match.getCmrNo().startsWith("P") && "75".equals(match.getOrderBlk())) {
          filteredMatches.add(match);
        }
        if (StringUtils.isNotBlank(match.getCmrNo())) {
          String cmrFound = match.getCmrNo().substring(0, 3);
          if ("800".equals(cmrFound)) {
            filteredMatches.add(match);
          }
        }
      }
      // set filtered matches in response
      response.setMatches(filteredMatches);
    }
  }

  @Override
  public boolean runUpdateChecksForAddress(EntityManager entityManager, AutomationEngineData engineData, RequestData requestData,
      RequestChangeContainer changes, AutomationResult<ValidationOutput> output, ValidationOutput validation) throws Exception {
    Admin admin = requestData.getAdmin();
    Data data = requestData.getData();
    boolean cmdeReview = false;
    boolean cmdeReviewCustNme = false;
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
                // if customer name has been updated on any address , simply
                // send to CMDE

                List<UpdatedNameAddrModel> addrChanges = changes.getAddressChanges(addr.getId().getAddrType(), addr.getId().getAddrSeq());
                for (UpdatedNameAddrModel change : addrChanges) {
                  if ("Customer Name".equals(change.getDataField()) && CmrConstants.RDC_SOLD_TO.equalsIgnoreCase(addr.getId().getAddrType())) {
                    cmdeReviewCustNme = true;
                    checkDetails.append("Update of Customer Name for " + addrType + "(" + addr.getId().getAddrSeq() + ") needs review.\n");
                  }
                }

                List<DnBMatchingResponse> matches = getMatches(requestData, engineData, addressToChk, false);
                boolean matchesDnb = false;
                if (matches != null) {
                  // check against D&B
                  matchesDnb = ifaddressCloselyMatchesDnb(matches, addr, admin, data.getCmrIssuingCntry());
                }
                if (!matchesDnb) {
                  LOG.debug("Update address for " + addrType + "(" + addr.getId().getAddrSeq() + ") does not match D&B");
                  cmdeReview = true;
                  checkDetails.append("Update address " + addrType + "(" + addr.getId().getAddrSeq() + ") did not match D&B records.\n");
                  // company proof
                  if (DnBUtil.isDnbOverrideAttachmentProvided(entityManager, admin.getId().getReqId())) {
                    checkDetails.append("Supporting documentation is provided by the requester as attachment for " + addrType).append("\n");
                  } else {
                    checkDetails.append("\nNo supporting documentation is provided by the requester for " + addrType + " address.");
                  }

                } else {
                  checkDetails.append("Update address " + addrType + "(" + addr.getId().getAddrSeq() + ") matches D&B records. Matches:\n");
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
              checkDetails.append("New address " + addrType + "(" + addr.getId().getAddrSeq() + ") did not match D&B.\n");
              // company proof
              if (DnBUtil.isDnbOverrideAttachmentProvided(entityManager, admin.getId().getReqId())) {
                checkDetails.append("Supporting documentation is provided by the requester as attachment for " + addrType).append("\n");
              } else {
                checkDetails.append("\nNo supporting documentation is provided by the requester for " + addrType + " address.");
              }
            } else {
              checkDetails.append("New address " + addrType + "(" + addr.getId().getAddrSeq() + ") matches D&B records. Matches:\n");
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
      engineData.addNegativeCheckStatus("_indCheckFailed", "Updates to addresses cannot be checked automatically.");
    } else if (cmdeReview) {
      engineData.addNegativeCheckStatus("_indCheckFailed", "Updates to addresses cannot be checked automatically.");
      validation.setSuccess(false);
      validation.setMessage("Review Required.");
    } else if (cmdeReviewCustNme) {
      engineData.addNegativeCheckStatus("_indCheckFailed", "Updates to addresses(Customer Name) cannot be checked automatically.");
      validation.setSuccess(false);
      validation.setMessage("Not Validated");
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
  public boolean runUpdateChecksForData(EntityManager entityManager, AutomationEngineData engineData, RequestData requestData,
      RequestChangeContainer changes, AutomationResult<ValidationOutput> output, ValidationOutput validation) throws Exception {
    Admin admin = requestData.getAdmin();
    Data data = requestData.getData();
    Addr soldTo = requestData.getAddress(CmrConstants.RDC_SOLD_TO);
    if (handlePrivatePersonRecord(entityManager, admin, output, validation, engineData)) {
      return true;
    }
    StringBuilder details = new StringBuilder();
    Set<String> resultCodes = new HashSet<String>();// D for Reject
    List<String> ignoredUpdates = new ArrayList<String>();
    for (UpdatedDataModel change : changes.getDataUpdates()) {
      switch (change.getDataField()) {

      case "GST#":
        // For GST update, Match with gst api
        boolean matchesgGST = false;

        if (StringUtils.isBlank(change.getOldData()) && !(change.getNewData().equals(change.getOldData()))) {
          admin.setScenarioVerifiedIndc("Y");
        } else {
          admin.setScenarioVerifiedIndc("N");
        }

        if (!StringUtils.isBlank(change.getNewData()) && !(change.getNewData().equals(change.getOldData()))) {
          // check against gST
          matchesgGST = getGstMatches(data.getId().getReqId(), soldTo, data.getVat());

          if (!matchesgGST) {
            resultCodes.add("R"); // R- review to CMDE
            details.append("GST# update on the request did not match GST Layer Service.\n");
          } else {
            details.append("GST# update on the request matches GST Layer Service.\n");
          }
        }
        break;
      default:
        ignoredUpdates.add(change.getDataField());
        break;
      }
    }

    if (resultCodes.contains("R")) {
      engineData.addNegativeCheckStatus("_indCheckFailed", "Updates to fields cannot be checked automatically.");
      validation.setSuccess(false);
      validation.setMessage("Review Required.");
    } else {
      validation.setSuccess(true);
      validation.setMessage("Successful");
    }
    if (!ignoredUpdates.isEmpty()) {
      details.append("Updates to the following fields skipped validation:\n");
      for (String field : ignoredUpdates) {
        details.append(" - " + field + "\n");
      }
    }
    output.setDetails(details.toString());
    output.setProcessOutput(validation);
    return true;
  }

  /**
   * Checks if relevant fields were updated
   * 
   * @param changes
   * @param addr
   * @return
   */
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

  private boolean getGstMatches(long reqId, Addr addr, String vat) throws Exception {

    LOG.debug("Validating GST# " + vat + " for country India");

    String baseUrl = SystemConfiguration.getValue("CMR_SERVICES_URL");
    AutomationServiceClient autoClient = CmrServicesFactory.getInstance().createClient(baseUrl, AutomationServiceClient.class);
    autoClient.setReadTimeout(1000 * 60 * 5);
    autoClient.setRequestMethod(Method.Get);

    GstLayerRequest gstLayerRequest = new GstLayerRequest();
    gstLayerRequest.setGst(vat);
    gstLayerRequest.setCountry("IN");
    gstLayerRequest.setName((StringUtils.isNotBlank(addr.getCustNm1()) ? addr.getCustNm1() : ""));
    gstLayerRequest.setAddress((StringUtils.isNotBlank(addr.getAddrTxt()) ? addr.getAddrTxt() : "")
        + (StringUtils.isNotBlank(addr.getAddrTxt2()) ? " " + addr.getAddrTxt2() : ""));
    gstLayerRequest.setCity(addr.getCity1());
    gstLayerRequest.setPostal(addr.getPostCd());

    LOG.debug("Connecting to the GST Layer Service at " + baseUrl);
    AutomationResponse<?> rawResponse = autoClient.executeAndWrap(AutomationServiceClient.IN_GST_SERVICE_ID, gstLayerRequest,
        AutomationResponse.class);
    ObjectMapper mapper = new ObjectMapper();
    String json = mapper.writeValueAsString(rawResponse);
    TypeReference<AutomationResponse<GstLayerResponse>> ref = new TypeReference<AutomationResponse<GstLayerResponse>>() {
    };
    AutomationResponse<GstLayerResponse> gstResponse = mapper.readValue(json, ref);
    if (gstResponse != null && gstResponse.isSuccess()) {
      if (gstResponse.getMessage().equals("Valid GST and Company Name entered on the Request")) {
        return true;
      } else {
        return false;
      }
    } else {
      return false;
    }
  }

  @Override
  public void emptyINAC(EntityManager entityManager, RequestData requestData, AutomationEngineData engineData) throws Exception {
    Data data = requestData.getData();
    LOG.debug("INAC code value " + data.getInacCd());
    data.setInacCd("");
    LOG.debug("INAC type value " + data.getInacType());
    data.setInacType("");
  }

  @Override
  public boolean performCountrySpecificCoverageCalculations(CalculateCoverageElement covElement, EntityManager entityManager,
      AutomationResult<OverrideOutput> results, StringBuilder details, OverrideOutput overrides, RequestData requestData,
      AutomationEngineData engineData, String covFrom, CoverageContainer container, boolean isCoverageCalculated) throws Exception {
    Admin admin = requestData.getAdmin();
    String ecoSystemCluster = SystemParameters.getString("IN_ECO_SYSTEM");
    FieldResultKey cluster = new FieldResultKey("DATA", "AP_CUST_CLUSTER_ID");
    String clusterVal = ""; // overriden cluster value is 08033
    if (overrides.getData().containsKey(cluster)) {
      clusterVal = overrides.getData().get(cluster).getNewValue();
    }
    if (isCoverageCalculated && StringUtils.isNotBlank(clusterVal) && clusterVal.equals(ecoSystemCluster)) {
      if (!isEcoSystemAttachmentProvided(entityManager, admin.getId().getReqId())) {
        details.append("\nEcoSystem cluster computed on coverage requires CMDE review.");
        engineData.addNegativeCheckStatus("_esCoverage", "EcoSystem cluster computed on coverage requires CMDE review.");
      }
    }
    return true;
  }

  private boolean isEcoSystemAttachmentProvided(EntityManager entityManager, long reqId) {
    // TODO Auto-generated method stub
    String sql = ExternalizedQuery.getSql("QUERY.CHECK_ECSYS_ATTACHMENT");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("ID", reqId);
    return query.exists();
  }

}
