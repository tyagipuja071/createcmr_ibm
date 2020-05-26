/**
 * 
 */
package com.ibm.cio.cmr.request.automation.impl.gbl;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import com.ibm.cio.cmr.request.automation.AutomationElement;
import com.ibm.cio.cmr.request.automation.AutomationElementRegistry;
import com.ibm.cio.cmr.request.automation.AutomationEngineData;
import com.ibm.cio.cmr.request.automation.RequestData;
import com.ibm.cio.cmr.request.automation.impl.MatchingElement;
import com.ibm.cio.cmr.request.automation.out.AutomationResult;
import com.ibm.cio.cmr.request.automation.out.MatchingOutput;
import com.ibm.cio.cmr.request.automation.util.AutomationUtil;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.AutomationMatching;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.util.RequestUtils;
import com.ibm.cio.cmr.request.util.geo.GEOHandler;
import com.ibm.cmr.services.client.CmrServicesFactory;
import com.ibm.cmr.services.client.MatchingServiceClient;
import com.ibm.cmr.services.client.ServiceClient.Method;
import com.ibm.cmr.services.client.matching.MatchingResponse;
import com.ibm.cmr.services.client.matching.dnb.DnBMatchingResponse;
import com.ibm.cmr.services.client.matching.gbg.GBGFinderRequest;
import com.ibm.cmr.services.client.matching.gbg.GBGResponse;

/**
 * {@link AutomationElement} for Global Buying Group assignment
 * 
 * @author JeffZAMORA
 * 
 */
public class GBGMatchingElement extends MatchingElement {

  private static final Logger LOG = Logger.getLogger(GBGMatchingElement.class);

  public GBGMatchingElement(String requestTypes, String actionOnError, boolean overrideData, boolean stopOnError) {
    super(requestTypes, actionOnError, overrideData, stopOnError);

  }

  @Override
  public AutomationResult<MatchingOutput> executeElement(EntityManager entityManager, RequestData requestData, AutomationEngineData engineData)
      throws Exception {

    DnBMatchingResponse dnbMatching = (DnBMatchingResponse) engineData.get(AutomationEngineData.DNB_MATCH);
    GBGFinderRequest request = new GBGFinderRequest();
    request.setMandt(SystemConfiguration.getValue("MANDT"));

    Addr currentAddress = requestData.getAddress("ZS01");
    Admin admin = requestData.getAdmin();
    Data data = requestData.getData();
    GEOHandler geoHandler = RequestUtils.getGEOHandler(data.getCmrIssuingCntry());
    AutomationUtil automationUtil = AutomationUtil.getNewCountryUtil(data.getCmrIssuingCntry());

    AutomationResult<MatchingOutput> result = buildResult(admin.getId().getReqId());
    MatchingOutput output = new MatchingOutput();

    // boolean continueCheck = true;
    // List<String> usedNames = new ArrayList<String>();
    // while (continueCheck) {
    if (currentAddress != null) {
      // if ("ZS01".equals(currentAddress.getId().getAddrType())) {
      // continueCheck = false;
      // }
      request.setCity(currentAddress.getCity1());

      if (geoHandler != null && !geoHandler.customerNamesOnAddress()) {
        request.setCustomerName(admin.getMainCustNm1() + (StringUtils.isBlank(admin.getMainCustNm2()) ? "" : " " + admin.getMainCustNm2()));
      } else {
        request.setCustomerName(
            currentAddress.getCustNm1() + (StringUtils.isBlank(currentAddress.getCustNm2()) ? "" : " " + currentAddress.getCustNm2()));
      }

      String nameUsed = request.getCustomerName();
      LOG.debug("Checking GBG for " + nameUsed);
      // usedNames.add(nameUsed.toUpperCase());
      request.setStreetLine1(currentAddress.getAddrTxt());
      request.setStreetLine2(currentAddress.getAddrTxt2());
      request.setLandedCountry(currentAddress.getLandCntry());
      request.setPostalCode(currentAddress.getPostCd());
      request.setStateProv(currentAddress.getStateProv());
      if (!StringUtils.isBlank(data.getVat())) {
        request.setOrgId(data.getVat());
      }
      request.setMinConfidence("6");

      if (StringUtils.isBlank(data.getDunsNo())) {
        // duns has not been computed yet, check if any matching has been
        // performed
        if (dnbMatching != null && dnbMatching.getConfidenceCode() > 7) {
          request.setDunsNo(dnbMatching.getDunsNo());
        }
      }

      if (automationUtil != null) {
        automationUtil.tweakGBGFinderRequest(entityManager, request, requestData);
      }

      MatchingServiceClient client = CmrServicesFactory.getInstance().createClient(SystemConfiguration.getValue("BATCH_SERVICES_URL"),
          MatchingServiceClient.class);
      client.setRequestMethod(Method.Get);
      client.setReadTimeout(1000 * 60 * 5);

      LOG.debug("Connecting to the GBG Finder Service at " + SystemConfiguration.getValue("BATCH_SERVICES_URL"));
      MatchingResponse<?> rawResponse = client.executeAndWrap(MatchingServiceClient.GBG_SERVICE_ID, request, MatchingResponse.class);
      ObjectMapper mapper = new ObjectMapper();
      String json = mapper.writeValueAsString(rawResponse);

      TypeReference<MatchingResponse<GBGResponse>> ref = new TypeReference<MatchingResponse<GBGResponse>>() {
      };
      MatchingResponse<GBGResponse> response = mapper.readValue(json, ref);

      if (response != null && response.getMatched()) {
        StringBuilder details = new StringBuilder();
        List<GBGResponse> gbgMatches = response.getMatches();
        Collections.sort(gbgMatches, new GBGComparator(request.getLandedCountry()));

        result.setResults("Matches Found");
        details.append(gbgMatches.size() + " record(s) found.");
        if (gbgMatches.size() > 5) {
          gbgMatches = gbgMatches.subList(0, 4);
          details.append("Showing top 5 matches only.");
        }
        int itemNo = 1;
        for (GBGResponse gbg : gbgMatches) {
          if (!currentAddress.getLandCntry().equals(gbg.getCountry())) {
            LOG.debug("Non-Local gbg found as highest match..");
            details.append("\n")
                .append("Matches for Global Buying Groups retrieved but no domestic Global Buying Group was found during the matching.");
            break;
          } else {
            details.append("\n");
            if (gbg.isDnbMatch()) {
              LOG.debug("Matches found via D&B matching..");
              details.append("\n").append("Found via DUNS matching:");
              output.addMatch(getProcessCode(), "LDE", gbg.getLdeRule(), "DUNS-Ctry/CMR Count", gbg.getCountry() + "/" + gbg.getCmrCount(), "GBG",
                  itemNo);
            } else if (gbg.isVatMatch()) {
              LOG.debug("Matches found via ORG ID matching..");
              details.append("\n").append("Found via ORG ID matching:");
              output.addMatch(getProcessCode(), "LDE", gbg.getLdeRule(), "VAT-Ctry/CMR Count", gbg.getCountry() + "/" + gbg.getCmrCount(), "GBG",
                  itemNo);
            }
            // else {
            // LOG.debug("Matches found via Name matching..");
            // details.append("\n").append("Found via Name matching [" +
            // CommonWordsUtil.minimize(nameUsed) + "]):");
            // output.addMatch(getProcessCode(), "LDE", gbg.getLdeRule(),
            // "Name-Ctry/CMR Count", gbg.getCountry() + "/" +
            // gbg.getCmrCount(),
            // "GBG",
            // itemNo);
            // }
            details.append("\n").append("GBG: " + gbg.getGbgId() + " (" + gbg.getGbgName() + ")");
            details.append("\n").append("BG: " + gbg.getBgId() + " (" + gbg.getBgName() + ")");
            details.append("\n").append("Country: " + gbg.getCountry());
            details.append("\n").append("CMR Count: " + gbg.getCmrCount());
            details.append("\n").append("LDE Rule: " + gbg.getLdeRule());
            details.append("\n").append("IA Account: " + (gbg.getIntAcctType() != null ? gbg.getIntAcctType() : "-"));
            if (gbg.isDnbMatch()) {
              details.append("\n").append("GU DUNS: " + gbg.getGuDunsNo() + "\nDUNS: " + gbg.getDunsNo());
            }

            if (itemNo == 1) {
              engineData.put(AutomationEngineData.GBG_MATCH, gbg);
            }

            itemNo++;
          }
        }
        result.setProcessOutput(output);
        result.setDetails(details.toString());
        // continueCheck = false;
      } else {
        // boolean nextFound = false;
        // if (geoHandler != null && geoHandler.customerNamesOnAddress()) {
        // for (Addr addr : requestData.getAddresses()) {
        // String name = addr.getCustNm1() +
        // (StringUtils.isBlank(addr.getCustNm2()) ? "" : " " +
        // addr.getCustNm2());
        // name = name.toUpperCase();
        // if (!usedNames.contains(name)) {
        // currentAddress = addr;
        // nextFound = true;
        // // make sure to clear duns on non-main address to trigger re
        // // check of DUNS for next call
        // dnbMatching = null;
        // break;
        // }
        // }
        // }

        // if (!nextFound) {
        // continueCheck = false;
        // result.setDetails("No GBG was found using DUNS hierarchy matching
        // and Name matching.");
        result.setDetails("No GBG was found using DUNS hierarchy matching.");
        // engineData.addRejectionComment("No GBG was found using DUNS
        // hierarchy matching and Name matching.");
        result.setResults("No Matches");
        result.setOnError(false);
        // }
      }
    } else {
      result.setDetails("Missing main address on the request.");
      engineData.addRejectionComment("OTH", "Missing main address on the request.", "", "");
      result.setResults("Missing Address");
      result.setOnError(true);
    }
    // }
    return result;
  }

  @Override
  public boolean importMatch(EntityManager entityManager, RequestData requestData, AutomationMatching match) {
    String keyType = match.getId().getMatchKeyName();
    switch (keyType) {
    case "LDE":
      return importLDE(entityManager, requestData, match);
    }
    return false;
  }

  /**
   * Imports the needed values to satisfy the LDE condition. BG LDE rules
   * revolve around 5 fields:<br>
   * <ul>
   * <li>Company</li>
   * <li>Enterprise</li>
   * <li>Affiliate</li>
   * <li>ISIC</li>
   * <li>INAC</li>
   * </ul>
   * 
   * @param entityManager
   * @param requestData
   * @param match
   * @return
   */
  private boolean importLDE(EntityManager entityManager, RequestData requestData, AutomationMatching match) {
    String ldeRule = match.getId().getMatchKeyValue();
    if (StringUtils.isBlank(ldeRule)) {
      LOG.warn("LDE rule for import is missing.");
      return false;
    }
    LOG.debug("Importing data for LDE Rule " + ldeRule);

    // format of LDE is BG_CNTRY_<CNTRY VALUE>_FIELD_VALUE for country BGs
    // and BG_FIELD_VALUE for global BGs
    String[] ldeSplit = ldeRule.split("[_]");
    List<String> ldeParts = Arrays.asList(ldeSplit);
    Collections.reverse(ldeParts);

    // first part now is the value, second is the type
    if (ldeParts.size() >= 2) {

      // import the matches
      Data data = requestData.getData();

      GEOHandler handler = RequestUtils.getGEOHandler(data.getCmrIssuingCntry());

      String value = ldeParts.get(0);
      String ruleField = ldeParts.get(1).toUpperCase();
      if (ruleField.contains("ENT")) {
        data.setEnterprise(value);
        handler.setGBGValues(entityManager, requestData, "ENT", value);
      } else if (ruleField.contains("CMPY")) {
        data.setCompany(value);
        handler.setGBGValues(entityManager, requestData, "CMPY", value);
      } else if (ruleField.contains("AFF")) {
        data.setAffiliate(value);
        handler.setGBGValues(entityManager, requestData, "AFFNO", value);
      } else if (ruleField.contains("SIC")) {
        data.setIsicCd(value);
        String subInd = RequestUtils.getSubIndustryCd(entityManager, data.getIsicCd(), data.getCmrIssuingCntry());
        if (subInd != null) {
          data.setSubIndustryCd(subInd);
        }
        handler.setGBGValues(entityManager, requestData, "SIC", value);
      } else if (ruleField.contains("NAC")) {
        data.setInacCd(value);
        if (StringUtils.isNumeric(value)) {
          data.setInacType("I");
        } else {
          data.setInacType("N");
        }
        handler.setGBGValues(entityManager, requestData, "INAC", value);
      }
      return true;
    } else {
      LOG.warn("LDE rule for import is missing.");
      return false;
    }

  }

  /**
   * Comparator for {@link GBGResponse}
   * 
   * @author JeffZAMORA
   * 
   */
  private class GBGComparator implements Comparator<GBGResponse> {

    private String landedCountry;

    public GBGComparator(String landedCountry) {
      this.landedCountry = landedCountry;
    }

    @Override
    public int compare(GBGResponse o1, GBGResponse o2) {
      // matched cmrs on the country
      if (this.landedCountry.equals(o1.getCountry()) && !this.landedCountry.equals(o2.getCountry())) {
        return -1;
      }
      if (!this.landedCountry.equals(o1.getCountry()) && this.landedCountry.equals(o2.getCountry())) {
        return 1;
      }
      // cmr count
      if (o1.getCmrCount() > o2.getCmrCount()) {
        return -1;
      }
      if (o1.getCmrCount() < o2.getCmrCount()) {
        return 1;
      }

      // Null pointer exception encountered. when comparing using this.
      if (StringUtils.isNotBlank(o1.getLdeRule()) && StringUtils.isNotBlank(o2.getLdeRule())) {
        // rule is country specific
        if (o1.getLdeRule().contains(this.landedCountry) && !o2.getLdeRule().contains(this.landedCountry)) {
          return -1;
        }
        if (!o1.getLdeRule().contains(this.landedCountry) && o2.getLdeRule().contains(this.landedCountry)) {
          return 1;
        }
      }

      return o1.getBgId().compareTo(o2.getBgId());
    }

  }

  @Override
  public String getProcessCode() {
    return AutomationElementRegistry.GBL_MATCH_GBG;
  }

  @Override
  public String getProcessDesc() {
    return "Find GBG";
  }

}
