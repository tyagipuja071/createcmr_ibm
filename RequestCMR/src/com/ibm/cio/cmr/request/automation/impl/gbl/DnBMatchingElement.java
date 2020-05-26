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
import org.codehaus.jackson.map.ObjectMapper;

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
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.AutomationMatching;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.service.requestentry.ImportDnBService;
import com.ibm.cio.cmr.request.user.AppUser;
import com.ibm.cio.cmr.request.util.RequestUtils;
import com.ibm.cio.cmr.request.util.dnb.DnBUtil;
import com.ibm.cio.cmr.request.util.geo.GEOHandler;
import com.ibm.cmr.services.client.dnb.DnBCompany;
import com.ibm.cmr.services.client.dnb.DnbOrganizationId;
import com.ibm.cmr.services.client.matching.MatchingResponse;
import com.ibm.cmr.services.client.matching.dnb.DnBMatchingResponse;

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
    if (soldTo != null) {
      boolean shouldThrowError = !"Y".equals(admin.getCompVerifiedIndc());
      boolean hasValidMatches = false;
      MatchingResponse<DnBMatchingResponse> response = DnBUtil.getMatches(requestData, "ZS01");
      hasValidMatches = DnBUtil.hasValidMatches(response);
      if (response != null && response.getMatched()) {
        StringBuilder details = new StringBuilder();
        List<DnBMatchingResponse> dnbMatches = response.getMatches();
        engineData.put(AutomationEngineData.DNB_ALL_MATCHES, dnbMatches);
        if (!hasValidMatches) {
          // if no valid matches - do not process records
          result.setOnError(shouldThrowError);
          result.setResults("No Matches");
          result.setDetails("No high quality matches with D&B records. Please import from D&B search.");
          engineData.addNegativeCheckStatus("DnBMatch", "No high quality matches with D&B records. Please import from D&B search.");
        } else {
          // actions to be performed only when matches with high confidence are
          // found
          boolean isOrgIdMatched = false;
          boolean vatFound = false;

          // process records and overrides
          DnBMatchingResponse highestCloseMatch = null;
          DnBMatchingResponse perfectMatch = null;

          for (DnBMatchingResponse dnbRecord : dnbMatches) {

            // check if the record closely matches D&B. This really validates
            // input against record
            boolean closelyMatches = DnBUtil.closelyMatchesDnb(data.getCmrIssuingCntry(), soldTo, admin, dnbRecord);
            if (closelyMatches) {
              LOG.debug("DUNS " + dnbRecord.getDunsNo() + " matches the request data.");
              if (highestCloseMatch == null) {
                highestCloseMatch = dnbRecord;
                if (!scenarioExceptions.isCheckVATForDnB() || StringUtils.isBlank(data.getVat()) || "Y".equals(data.getVatExempt())) {
                  // no D&B VAT check or no VAT on record or VAT exempt, this
                  // match is the one we
                  // need
                  perfectMatch = dnbRecord;
                  break;
                }
              }

              isOrgIdMatched = "Y".equals(dnbRecord.getOrgIdMatch());
              vatFound = StringUtils.isNotBlank(DnBUtil.getVAT(dnbRecord.getDnbCountry(), dnbRecord.getOrgIdDetails()));

              if (scenarioExceptions.isCheckVATForDnB() && !StringUtils.isBlank(data.getVat())
                  && ((!vatFound && engineData.hasPositiveCheckStatus(AutomationEngineData.VAT_VERIFIED)) || (vatFound && isOrgIdMatched))) {
                // found the perfect match here
                perfectMatch = dnbRecord;
                break;
              }

            } else {
              LOG.debug("DUNS " + dnbRecord.getDunsNo() + " does NOT match the request data.");
            }

          }

          // assess the matches here
          if (perfectMatch != null) {
            LOG.debug("Perfect match was found with DUNS " + perfectMatch.getDunsNo());
            result.setResults("Matched");
            details.append("High Quality match D&B record matched the request name/address information.\n");
            if (!"Y".equals(perfectMatch.getOrgIdMatch()) && !scenarioExceptions.isCheckVATForDnB()) {
              details.append("VAT on the D&B record does not match VAT on the request but country is not configured to match VAT.\n");
            }
            processDnBFields(entityManager, data, perfectMatch, output, details, 1);
            if (scenarioExceptions.isImportDnbInfo()) {
              // Create Address Records only if Levenshtein Distance
              List<String> eligibleAddresses = getAddrSatisfyingLevenshteinDistance(data.getCmrIssuingCntry(), admin, requestData.getAddresses(),
                  perfectMatch);
              if (eligibleAddresses.size() > 0) {
                processAddressFields(perfectMatch, output, 1, scenarioExceptions, handler, eligibleAddresses);
              }
            }
            engineData.put("dnbMatching", perfectMatch);
            LOG.trace(new ObjectMapper().writeValueAsString(perfectMatch));
          } else if (highestCloseMatch != null && scenarioExceptions.isCheckVATForDnB() && !StringUtils.isBlank(data.getVat())
              && !"Y".equals(data.getVatExempt())) {
            LOG.debug("High quality match was found with DUNS " + highestCloseMatch.getDunsNo() + " but incorrect VAT");
            result.setResults("VAT not matched");
            details.append(
                "High Quality match D&B record matched the request name/address information but the VAT on record did not match request data.\n");
            processDnBFields(entityManager, data, highestCloseMatch, output, details, 1);
            if (scenarioExceptions.isImportDnbInfo()) {
              // Create Address Records only if Levenshtein Distance
              List<String> eligibleAddresses = getAddrSatisfyingLevenshteinDistance(data.getCmrIssuingCntry(), admin, requestData.getAddresses(),
                  highestCloseMatch);
              if (eligibleAddresses.size() > 0) {
                processAddressFields(highestCloseMatch, output, 1, scenarioExceptions, handler, eligibleAddresses);
              }
            }
            engineData.addNegativeCheckStatus("DNB_VAT_MATCH_CHECK_FAIL", "VAT value did not match with the highest confidence D&B match.");
            LOG.trace(new ObjectMapper().writeValueAsString(highestCloseMatch));
          } else {

            // by now this is sure to be on error with no data match
            details.append("Matches against D&B were found but no record matched the request data.\n");
            details.append("Showing D&B matches:\n");
            int itemNo = 1;
            for (DnBMatchingResponse dnbRecord : dnbMatches) {
              processDnBFields(entityManager, data, dnbRecord, output, details, itemNo);
              itemNo++;
            }
            engineData.addRejectionComment("OTH", "Invalid / incomplete name and/or address", "", "");
            result.setResults("Name/Address not matched");
            result.setOnError(true);
            engineData.put("dnbMatching", dnbMatches.get(0));
          }

          // save the highest matched record
          DnBMatchingResponse attachRecord = null;
          if (perfectMatch != null || highestCloseMatch != null) {
            attachRecord = perfectMatch != null ? perfectMatch : highestCloseMatch;
          } else {
            attachRecord = dnbMatches.get(0);
          }
          ImportDnBService dnbService = new ImportDnBService();
          AppUser user = (AppUser) engineData.get("appUser");
          LOG.debug("Saving DnB Highest match attachment for DUNS " + attachRecord.getDunsNo() + "..");
          try {
            dnbService.saveDnBAttachment(entityManager, user, admin.getId().getReqId(), attachRecord.getDunsNo(), "DnBHighestMatch",
                attachRecord.getConfidenceCode());
          } catch (Exception e) {
            // ignore attachment issues
            LOG.warn("Error in saving D&B attachment", e);
          }
          result.setDetails(details.toString().trim());
          result.setProcessOutput(output);
        }
      } else {
        result.setDetails("No D&B record was found using advanced matching.");
        engineData.addRejectionComment("OTH", "No matches with D&B records. Please import from D&B search.", "", "");
        result.setResults("No Matches");
        result.setOnError(true);
      }

    } else {
      result.setDetails("Missing main address on the request.");
      engineData.addRejectionComment("OTH", "Invalid / incomplete name and/or address. Missing main address on the request.", "", "");
      result.setResults("No Matches");
      result.setOnError(true);
    }
    return result;
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
  private List<String> getAddrSatisfyingLevenshteinDistance(String country, Admin admin, List<Addr> addresses, DnBMatchingResponse dnbRecord) {
    List<String> eligibleAddresses = new ArrayList<String>();
    for (Addr addr : addresses) {
      if (DnBUtil.closelyMatchesDnb(country, addr, admin, dnbRecord)) {
        eligibleAddresses.add(addr.getId().getAddrType());
      }
    }
    return eligibleAddresses;
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

    details.append("Organization IDs:");
    boolean relevantOrgId = false;
    for (int i = 0; i < orgIDDetails.size(); i++) {
      DnbOrganizationId orgId = orgIDDetails.get(i);
      if (DnBUtil.isRelevant(dnbRecord.getDnbCountry(), orgId)) {
        details.append("\n - " + orgId.getOrganizationIdType() + " = " + orgId.getOrganizationIdCode());
        relevantOrgId = true;
      }
    }

    if (!relevantOrgId) {
      details.append("(No relevant Org Id found)\n");
    } else {
      details.append("\n");
    }

    LOG.debug("Connecting to D&B details service..");
    DnBCompany dnbData = DnBUtil.getDnBDetails(dnbRecord.getDunsNo());
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

}
