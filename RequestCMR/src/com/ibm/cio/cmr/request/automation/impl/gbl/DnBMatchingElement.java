/**
 * 
 */
package com.ibm.cio.cmr.request.automation.impl.gbl;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.xmlbeans.impl.common.Levenshtein;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.springframework.ui.ModelMap;

import com.ibm.cio.cmr.request.automation.AutomationElement;
import com.ibm.cio.cmr.request.automation.AutomationElementRegistry;
import com.ibm.cio.cmr.request.automation.AutomationEngineData;
import com.ibm.cio.cmr.request.automation.CompanyVerifier;
import com.ibm.cio.cmr.request.automation.ProcessType;
import com.ibm.cio.cmr.request.automation.RequestData;
import com.ibm.cio.cmr.request.automation.impl.MatchingElement;
import com.ibm.cio.cmr.request.automation.out.AutomationResult;
import com.ibm.cio.cmr.request.automation.out.MatchingOutput;
import com.ibm.cio.cmr.request.automation.util.ScenarioExceptionsUtil;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.AutomationMatching;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.service.CmrClientService;
import com.ibm.cio.cmr.request.service.requestentry.ImportDnBService;
import com.ibm.cio.cmr.request.user.AppUser;
import com.ibm.cio.cmr.request.util.RequestUtils;
import com.ibm.cio.cmr.request.util.SystemLocation;
import com.ibm.cio.cmr.request.util.dnb.DnBUtil;
import com.ibm.cio.cmr.request.util.geo.GEOHandler;
import com.ibm.cmr.services.client.CmrServicesFactory;
import com.ibm.cmr.services.client.MatchingServiceClient;
import com.ibm.cmr.services.client.dnb.DnBCompany;
import com.ibm.cmr.services.client.dnb.DnbData;
import com.ibm.cmr.services.client.dnb.DnbOrganizationId;
import com.ibm.cmr.services.client.matching.MatchingResponse;
import com.ibm.cmr.services.client.matching.dnb.DnBMatchingResponse;
import com.ibm.cmr.services.client.matching.gbg.GBGFinderRequest;

/**
 * {@link AutomationElement} implementation for the advanced D&B matching
 * 
 * @author RoopakChugh
 * 
 */
public class DnBMatchingElement extends MatchingElement implements CompanyVerifier {

  private static final Logger LOG = Logger.getLogger(DnBMatchingElement.class);

  public DnBMatchingElement(String requestTypes, String actionOnError, boolean overrideData, boolean stopOnError) {
    super(requestTypes, actionOnError, overrideData, stopOnError);
  }

  @Override
  public AutomationResult<MatchingOutput> executeElement(EntityManager entityManager, RequestData requestData, AutomationEngineData engineData)
      throws Exception {

    Addr soldTo = requestData.getAddress("ZS01");
    Admin admin = requestData.getAdmin();
    Data data = requestData.getData();
    GEOHandler handler = RequestUtils.getGEOHandler(data.getCmrIssuingCntry());
    ScenarioExceptionsUtil scenarioExceptions = getScenarioExceptions(entityManager, requestData, engineData);
    AutomationResult<MatchingOutput> result = buildResult(admin.getId().getReqId());
    MatchingOutput output = new MatchingOutput();
    // COMMENTEDD BECAUSE OF CONFLICTING REQUIREMENT
    // if (scorecard != null &&
    // StringUtils.isNotBlank(scorecard.getFindDnbResult())
    // && ("Accepted".equals(scorecard.getFindDnbResult()) ||
    // "Rejected".equals(scorecard.getFindDnbResult()))) {
    // String message = "Accepted".equals(scorecard.getFindDnbResult()) ? "D&B
    // record has been imported into the request already."
    // : "D&B record has been rejected already.";
    // result.setDetails(message);
    // result.setResults("D&B Imported");
    // } else
    if (soldTo != null) {
      boolean shouldThrowError = !"Y".equals(admin.getCompVerifiedIndc());
      boolean hasValidMatches = false;
      MatchingResponse<DnBMatchingResponse> response = getMatches(handler, requestData, engineData);
      if (engineData.hasPositiveCheckStatus("hasValidMatches")) {
        hasValidMatches = true;
      }
      if (response != null && response.getMatched()) {
        StringBuilder details = new StringBuilder();
        List<DnBMatchingResponse> dnbMatches = response.getMatches();
        if (!hasValidMatches) {
          details.append("No match with confidence level 8 and above.").append("\n\n");
        }

        if (dnbMatches.size() > 5) {
          dnbMatches = dnbMatches.subList(0, 4);
        }
        boolean isOrgIdMatched = false;
        int itemNo = 0;
        for (DnBMatchingResponse dnbRecord : dnbMatches) {
          // Create DnB Fields Records Regardless
          itemNo++;
          processDnBFields(entityManager, data, dnbRecord, output, details, itemNo);
          if (!isOrgIdMatched) {
            isOrgIdMatched = "Y".equals(dnbRecord.getOrgIdMatch());
          }
          // Check if element is configured to import from DNB
          if (scenarioExceptions.isImportDnbInfo()) {
            // Create Address Records only if Levenshtein Distance
            List<String> eligibleAddresses = getAddrSatisfyingLevenshteinDistance(handler, admin, requestData.getAddresses(), dnbRecord);
            if (eligibleAddresses.size() > 0) {
              processAddressFields(dnbRecord, output, itemNo, scenarioExceptions, handler, eligibleAddresses);
            }
          }
          engineData.put("dnbMatching", dnbRecord);
          LOG.debug(new ObjectMapper().writeValueAsString(dnbRecord));
        }

        if (!hasValidMatches) {
          result.setOnError(shouldThrowError);
          result.setResults("No Matches");
          result.setDetails("No high quality matches with D&B records. Please import from D&B search.");
          engineData.addNegativeCheckStatus("DnBMatch", "No high quality matches with D&B records. Please import from D&B search.");
        } else {
          result.setResults("Matches Found");
          if (scenarioExceptions.isCheckVATForDnB() && !isOrgIdMatched) {
            details.insert(0, itemNo + " High Quality matches found.\nVAT value did not match any of the high confidence D&B matches.\n");
            engineData.addNegativeCheckStatus("DNB_VAT_MATCH_FAIL", "VAT value did not match any of the high confidence D&B matches.");
          } else {
            details.insert(0, itemNo + " valid matches found.\n");
          }
          // save the highest matched record
          if (dnbMatches != null && !dnbMatches.isEmpty()) {
            ImportDnBService dnbService = new ImportDnBService();
            DnBMatchingResponse highestMatch = dnbMatches.get(0);
            AppUser user = (AppUser) engineData.get("appUser");
            LOG.debug("Saving DnB Highest match attachment for DUNS " + highestMatch.getDunsNo() + "..");
            try {
              dnbService.saveDnBAttachment(entityManager, user, admin.getId().getReqId(), highestMatch.getDunsNo(), "DnBHighestMatch",
                  highestMatch.getConfidenceCode());
            } catch (Exception e) {
              // ignore attachment issues
              LOG.warn("Error in saving D&B attachment", e);
            }
          }
          result.setDetails(details.toString().trim());
        }
        result.setProcessOutput(output);
      } else {
        result.setDetails("No D&B record was found using advanced matching.");
        engineData.addRejectionComment("No matches with D&B records. Please import from D&B search.");
        result.setResults("No Matches");
        result.setOnError(true);
      }

    } else {
      result.setDetails("Missing main address on the request.");
      engineData.addRejectionComment("Missing main address on the request");
      result.setResults("No Matches");
      result.setOnError(true);
    }
    return result;
  }

  public MatchingResponse<DnBMatchingResponse> getMatches(GEOHandler handler, RequestData requestData, AutomationEngineData engineData)
      throws Exception {
    Admin admin = requestData.getAdmin();
    Data data = requestData.getData();
    Addr soldTo = requestData.getAddress("ZS01");
    GBGFinderRequest request = createRequest(handler, admin, data, soldTo);
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

    List<DnBMatchingResponse> dnbMatches = response.getMatches();

    for (DnBMatchingResponse dnbRecord : dnbMatches) {
      if (dnbRecord.getConfidenceCode() > 7) {
        // use 8 as threshold
        engineData.addPositiveCheckStatus("hasValidMatches");
        break;
      }
    }

    return response;

  }

  /**
   * returns list of eligible addresses for which matching records can be
   * created
   * 
   * @param handler
   * @param admin
   * @param addresses
   * @param dnbRecord
   * @return
   */
  private List<String> getAddrSatisfyingLevenshteinDistance(GEOHandler handler, Admin admin, List<Addr> addresses, DnBMatchingResponse dnbRecord) {
    List<String> eligibleAddresses = new ArrayList<String>();
    String dnbAddress = dnbRecord.getDnbStreetLine1()
        + (StringUtils.isNotBlank(dnbRecord.getDnbStreetLine2()) ? " " + dnbRecord.getDnbStreetLine2() : "");
    for (Addr addr : addresses) {
      boolean result = true;

      if (StringUtils.isNotBlank(getCustomerName(handler, admin, addr)) && StringUtils.isNotBlank(dnbRecord.getDnbName())
          && Levenshtein.distance(getCustomerName(handler, admin, addr).toUpperCase(), dnbRecord.getDnbName().toUpperCase()) > 16) {
        result = false;
      }
      String address1 = addr.getAddrTxt();
      String address2 = addr.getAddrTxt();
      if (StringUtils.isNotBlank(addr.getAddrTxt2())) {
        address1 = address1 + " " + addr.getAddrTxt2();
        address2 = addr.getAddrTxt2() + " " + address2;
      }

      if (StringUtils.isNotBlank(addr.getAddrTxt()) && StringUtils.isNotBlank(dnbRecord.getDnbStreetLine1())
          && Levenshtein.distance(address1.toUpperCase(), dnbAddress.toUpperCase()) > 16
          && Levenshtein.distance(address2.toUpperCase(), dnbAddress.toUpperCase()) > 16) {
        result = false;
      }
      if (StringUtils.isNotBlank(addr.getPostCd()) && StringUtils.isNotBlank(dnbRecord.getDnbPostalCode())
          && Levenshtein.distance(addr.getPostCd().toUpperCase(), dnbRecord.getDnbPostalCode().toUpperCase()) > 16) {
        result = false;
      }
      if (StringUtils.isNotBlank(addr.getCity1()) && StringUtils.isNotBlank(dnbRecord.getDnbCity())
          && Levenshtein.distance(addr.getCity1().toUpperCase(), dnbRecord.getDnbCity().toUpperCase()) > 16) {
        result = false;
      }
      if (result) {
        eligibleAddresses.add(addr.getId().getAddrType());
      }
    }
    return eligibleAddresses;
  }

  /**
   * prepares and returns a dnb request based on requestData
   * 
   * @param handler
   * @param admin
   * @param data
   * @param soldTo
   * @return
   */
  private GBGFinderRequest createRequest(GEOHandler handler, Admin admin, Data data, Addr soldTo) {
    GBGFinderRequest request = new GBGFinderRequest();
    request.setMandt(SystemConfiguration.getValue("MANDT"));
    if (StringUtils.isNotBlank(data.getVat())) {
      request.setOrgId(data.getVat());
    } else if (StringUtils.isNotBlank(soldTo.getVat())) {
      if (SystemLocation.SWITZERLAND.equalsIgnoreCase(data.getCmrIssuingCntry())) {
        request.setOrgId(soldTo.getVat().split("\\s")[0]);
      } else {
        request.setOrgId(soldTo.getVat());
      }
    }
    if (soldTo != null) {
      request.setCity(soldTo.getCity1());
      if (StringUtils.isBlank(request.getCity()) && SystemLocation.SINGAPORE.equals(data.getCmrIssuingCntry())) {
        // here for now, find a way to move to common class
        request.setCity("SINGAPORE");
      }
      request.setCustomerName(getCustomerName(handler, admin, soldTo));
      request.setStreetLine1(soldTo.getAddrTxt());
      request.setStreetLine2(soldTo.getAddrTxt2());
      request.setLandedCountry(soldTo.getLandCntry());
      request.setPostalCode(soldTo.getPostCd());
      request.setStateProv(soldTo.getStateProv());
      request.setMinConfidence("4");
    }

    return request;
  }

  /**
   * 
   * Processes DnB fields for a particular Dnb Record, creates details for
   * results and matching records for importing Dnb data to the request.
   * 
   * @param entityManager
   * @param data
   * @param dnbRecord
   * @param output
   * @param details
   * @param itemNo
   * @throws Exception
   */
  private void processDnBFields(EntityManager entityManager, Data data, DnBMatchingResponse dnbRecord, MatchingOutput output, StringBuilder details,
      int itemNo) throws Exception {
    details.append("\n");

    LOG.debug("Matches found via D&B matching..");

    output.addMatch(getProcessCode(), "DUNS_NO", dnbRecord.getDunsNo(), "Confidence Code", dnbRecord.getConfidenceCode() + "", "D&B", itemNo);
    details.append("DUNS No. = " + dnbRecord.getDunsNo()).append("\n");
    details.append("Confidence Code = " + dnbRecord.getConfidenceCode()).append("\n");
    details.append("Company Name =  " + dnbRecord.getDnbName()).append("\n");
    details.append("Address =  " + dnbRecord.getDnbStreetLine1()).append("\n");
    if (!StringUtils.isBlank(dnbRecord.getDnbStreetLine2())) {
      details.append("Address (cont)=  " + dnbRecord.getDnbStreetLine2()).append("\n");
    }
    if (!StringUtils.isBlank(dnbRecord.getDnbCity())) {
      details.append("City =  " + dnbRecord.getDnbCity()).append("\n");
    }
    if (!StringUtils.isBlank(dnbRecord.getDnbStateProv())) {
      details.append("State =  " + dnbRecord.getDnbStateProv()).append("\n");
    }
    if (!StringUtils.isBlank(dnbRecord.getDnbPostalCode())) {
      details.append("Postal Code =  " + dnbRecord.getDnbPostalCode()).append("\n");
    }
    if (!StringUtils.isBlank(dnbRecord.getDnbCountry())) {
      details.append("Country =  " + dnbRecord.getDnbCountry()).append("\n");
    }
    String orgIdMatch = "Y".equals(dnbRecord.getOrgIdMatch()) ? "Matched" : ("N".equals(dnbRecord.getOrgIdMatch()) ? "Not Matched" : "Not Done");
    details.append("Org ID Matching =  " + orgIdMatch).append("\n");

    List<DnbOrganizationId> orgIDDetails = dnbRecord.getOrgIdDetails();

    details.append("Organization IDs:\n");
    for (int i = 0; i < orgIDDetails.size(); i++) {
      DnbOrganizationId orgId = orgIDDetails.get(i);
      if (DnBUtil.isRelevant(dnbRecord.getDnbCountry(), orgId)) {
        details.append(orgId.getOrganizationIdType() + " - " + orgId.getOrganizationIdCode() + "\n");
      }
    }

    LOG.debug("Connecting to D&B details service..");
    DnBCompany dnbData = getDnBDetails(dnbRecord.getDunsNo());
    if (dnbData != null) {

      if (!StringUtils.isBlank(dnbData.getPrimaryCounty())) {
        details.append("County =  " + dnbData.getPrimaryCounty()).append("\n");
      }

      output.addMatch(getProcessCode(), "ISIC_CD", dnbData.getIbmIsic(), "Derived", "Derived", "ISIC", itemNo);
      details.append("ISIC =  " + dnbData.getIbmIsic() + " (" + dnbData.getIbmIsicDesc() + ")").append("\n");
      String subInd = RequestUtils.getSubIndustryCd(entityManager, dnbData.getIbmIsic(), data.getCmrIssuingCntry());
      if (subInd != null) {
        output.addMatch(getProcessCode(), "SUB_INDUSTRY_CD", subInd, "Derived", "Derived", "SUB_INDUSTRY_CD", itemNo);
        details.append("Subindustry Code  =  " + subInd).append("\n");
      }
    }
  }

  /**
   * 
   * Processes address data and creates matching records for importing Address
   * data to the request
   * 
   * @param dnbRecord
   * @param output
   * @param itemNo
   * @param scenarioExceptions
   * @param handler
   */
  private void processAddressFields(DnBMatchingResponse dnbRecord, MatchingOutput output, int itemNo, ScenarioExceptionsUtil scenarioExceptions,
      GEOHandler handler, List<String> eligibleAddressTypes) {
    boolean mainCustNameAdded = false;
    for (String addrType : eligibleAddressTypes) {

      String companyNm = dnbRecord.getDnbName();
      String address = dnbRecord.getDnbStreetLine1()
          + (StringUtils.isNotBlank(dnbRecord.getDnbStreetLine2()) ? " " + dnbRecord.getDnbStreetLine2() : "");
      String name1, name2, addrTxt1, addrTxt2;

      // customer name 1 and name 2
      if (!StringUtils.isEmpty(companyNm)) {
        if (companyNm.trim().length() > 30) {
          name1 = companyNm.substring(0, 30);
          name2 = ((companyNm.length() - 30 > 30) ? companyNm.substring(30, 60) : companyNm.substring(30));
        } else {
          name1 = companyNm;
          name2 = "";
        }
        if (!handler.customerNamesOnAddress() && !mainCustNameAdded) {
          output.addMatch(getProcessCode(), "MAIN_CUST_NM1", name1, "Derived", "Derived", "D&B", itemNo);
          if (name2 != null) {
            output.addMatch(getProcessCode(), "MAIN_CUST_NM2", name2, "Derived", "Derived", "D&B", itemNo);
          }
          mainCustNameAdded = true;
        } else {
          output.addMatch(getProcessCode(), addrType + "::CUST_NM1", name1, "Derived", "Derived", "D&B", itemNo);
          if (name2 != null) {
            output.addMatch(getProcessCode(), addrType + "::CUST_NM2", name2, "Derived", "Derived", "D&B", itemNo);
          }
        }
      }
      if (StringUtils.isNotBlank(address)) {
        if (address.length() > 30) {
          addrTxt1 = address.substring(0, 30);
          addrTxt2 = ((address.length() - 30 > 30) ? address.substring(30, 60) : address.substring(30));
        } else {
          addrTxt1 = address;
          addrTxt2 = "";
        }
        // street line1
        output.addMatch(getProcessCode(), addrType + "::ADDR_TXT", addrTxt1, "Derived", "Derived", "D&B", itemNo);
        if (StringUtils.isNotBlank(addrTxt2)) {
          output.addMatch(getProcessCode(), addrType + "::ADDR_TXT_2", addrTxt2, "Derived", "Derived", "D&B", itemNo);
        }
      }
      if (!StringUtils.isBlank(dnbRecord.getDnbCity())) {
        output.addMatch(getProcessCode(), addrType + "::CITY1", dnbRecord.getDnbCity(), "Derived", "Derived", "D&B", itemNo);
      }
      if (!StringUtils.isBlank(dnbRecord.getDnbStateProv())) {
        output.addMatch(getProcessCode(), addrType + "::STATE_PROV", dnbRecord.getDnbStateProv(), "Derived", "Derived", "D&B", itemNo);
      }
      if (!StringUtils.isBlank(dnbRecord.getDnbPostalCode())) {
        output.addMatch(getProcessCode(), addrType + "::POST_CD", dnbRecord.getDnbPostalCode(), "Derived", "Derived", "D&B", itemNo);
      }
      if (!StringUtils.isBlank(dnbRecord.getDnbCountry())) {
        output.addMatch(getProcessCode(), addrType + "::LAND_CNTRY", dnbRecord.getDnbCountry(), "Derived", "Derived", "D&B", itemNo);
      }
    }
  }

  /**
   * Connects to the details service and gets the details of the DUNS NO from
   * D&B
   * 
   * @param dunsNo
   * @return
   * @throws Exception
   */
  private DnBCompany getDnBDetails(String dunsNo) throws Exception {
    CmrClientService service = new CmrClientService();
    ModelMap map = new ModelMap();
    service.getDnBDetails(map, dunsNo);
    DnbData data = (DnbData) map.get("data");
    if (data != null && data.getResults() != null && !data.getResults().isEmpty()) {
      return data.getResults().get(0);
    }
    return null;
  }

  @Override
  public boolean importMatch(EntityManager entityManager, RequestData requestData, AutomationMatching match) {
    String field = match.getId().getMatchKeyName();
    Data data = requestData.getData();
    Admin admin = requestData.getAdmin();
    if (Arrays.asList("DUNS_NO", "ISIC_CD", "SUB_INDUSTRY_CD").contains(field)) {
      setEntityValue(data, field, match.getId().getMatchKeyValue());
    } else if (field.contains("::")) {
      String[] addressInfoField = field.split("::");
      if (addressInfoField.length == 2 && addressInfoField[0].length() == 4) {
        Addr addr = requestData.getAddress(addressInfoField[0]);
        if (Arrays.asList("CUST_NM1", "CUST_NM2", "ADDR_TXT", "ADDR_TXT_2", "CITY1", "STATE_PROV", "LAND_CNTRY", "POST_CD")
            .contains(addressInfoField[1])) {
          setEntityValue(addr, addressInfoField[1], match.getId().getMatchKeyValue());
        }
      }
    } else if (Arrays.asList("MAIN_CUST_NM1", "MAIN_CUST_NM2").contains(field)) {
      setEntityValue(admin, field, match.getId().getMatchKeyValue());
    }
    return true;
  }

  /**
   * Sets the data value by finding the relevant column having the given field
   * name
   * 
   * @param entity
   * @param fieldName
   * @param value
   */
  protected void setEntityValue(Object entity, String fieldName, Object value) {
    boolean fieldMatched = false;
    for (Field field : entity.getClass().getDeclaredFields()) {
      // match the entity name to field name
      fieldMatched = false;
      Column col = field.getAnnotation(Column.class);
      if (col != null && fieldName.toUpperCase().equals(col.name().toUpperCase())) {
        fieldMatched = true;
      } else if (field.getName().toUpperCase().equals(fieldName.toUpperCase())) {
        fieldMatched = true;
      }
      if (fieldMatched) {
        try {
          field.setAccessible(true);
          try {
            Method set = entity.getClass().getDeclaredMethod("set" + (field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1)),
                value != null ? value.getClass() : String.class);
            if (set != null) {
              set.invoke(entity, value);
            }
          } catch (Exception e) {
            field.set(entity, value);
          }
        } catch (Exception e) {
          LOG.trace("Field " + fieldName + " cannot be assigned. Error: " + e.getMessage());
        }
      }
    }
  }

  @Override
  public String getProcessCode() {
    return AutomationElementRegistry.GBL_MATCH_DNB;
  }

  @Override
  public String getProcessDesc() {
    return "D&B Matching";
  }

  @Override
  public ProcessType getProcessType() {
    return ProcessType.Matching;
  }

  /**
   * returns concatenated customerName from admin or address as per country
   * settings
   * 
   * @param handler
   * @param admin
   * @param soldTo
   * @return
   */
  private String getCustomerName(GEOHandler handler, Admin admin, Addr soldTo) {
    String customerName = null;
    if (!handler.customerNamesOnAddress()) {
      customerName = admin.getMainCustNm1() + (StringUtils.isBlank(admin.getMainCustNm2()) ? "" : " " + admin.getMainCustNm2());
    } else {
      customerName = soldTo.getCustNm1() + (StringUtils.isBlank(soldTo.getCustNm2()) ? "" : " " + soldTo.getCustNm2());
    }
    return customerName;
  }

}
