package com.ibm.cio.cmr.request.automation.util.geo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.automation.AutomationEngineData;
import com.ibm.cio.cmr.request.automation.RequestData;
import com.ibm.cio.cmr.request.automation.out.AutomationResult;
import com.ibm.cio.cmr.request.automation.out.OverrideOutput;
import com.ibm.cio.cmr.request.automation.out.ValidationOutput;
import com.ibm.cio.cmr.request.automation.util.AutomationUtil;
import com.ibm.cio.cmr.request.automation.util.RequestChangeContainer;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.model.window.UpdatedDataModel;
import com.ibm.cio.cmr.request.model.window.UpdatedNameAddrModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.util.RequestUtils;
import com.ibm.cio.cmr.request.util.dnb.DnBUtil;
import com.ibm.cio.cmr.request.util.geo.GEOHandler;
import com.ibm.cmr.services.client.CmrServicesFactory;
import com.ibm.cmr.services.client.QueryClient;
import com.ibm.cmr.services.client.matching.MatchingResponse;
import com.ibm.cmr.services.client.matching.dnb.DnBMatchingResponse;
import com.ibm.cmr.services.client.query.QueryRequest;
import com.ibm.cmr.services.client.query.QueryResponse;

/**
 * 
 * @author RoopakChugh
 *
 */

public class USUtil extends AutomationUtil {

  private static final Logger LOG = Logger.getLogger(USUtil.class);
  public static final String COMMERCIAL = "1";
  public static final String STATE_LOCAL = "2";
  public static final String LEASING = "3";
  public static final String FEDERAL = "4";
  public static final String INTERNAL = "5";
  public static final String POWER_OF_ATTORNEY = "6";
  public static final String BUSINESS_PARTNER = "7";

  @Override
  public AutomationResult<OverrideOutput> doCountryFieldComputations(EntityManager entityManager, AutomationResult<OverrideOutput> results,
      StringBuilder details, OverrideOutput overrides, RequestData requestData, AutomationEngineData engineData) throws Exception {
    return null;
  }

  @Override
  public boolean performScenarioValidation(EntityManager entityManager, RequestData requestData, AutomationEngineData engineData,
      AutomationResult<ValidationOutput> result, StringBuilder details, ValidationOutput output) {
    // get request admin and data
    Admin admin = requestData.getAdmin();
    Data data = requestData.getData();
    String[] scnarioList = { "HOSPITALS", "SCHOOL PUBLIC", "SCHOOL CHARTER", "SCHOOL PRIV", "SCHOOL PAROCHL", "SCHOOL COLLEGE", "STATE", "SPEC DIST",
        "COUNTY", "CITY", "32C", "TPPS", "3CC", "SVR CONT" };
    String scenarioSubType = "";
    if ("C".equals(admin.getReqType()) && data != null) {
      scenarioSubType = StringUtils.isBlank(data.getCustSubGrp()) ? "" : data.getCustSubGrp();
    }

    if (Arrays.asList(scnarioList).contains(scenarioSubType)) {
      engineData.addNegativeCheckStatus("US_SCENARIO_CHK", "Automated checks cannot be performed for this scenario.");
    }
    return true;
  }

  @Override
  public boolean runUpdateChecksForData(EntityManager entityManager, AutomationEngineData engineData, RequestData requestData,
      RequestChangeContainer changes, AutomationResult<ValidationOutput> output, ValidationOutput validation) throws Exception {
    String custTypeCd = determineUSCMRDetails(entityManager, requestData, engineData).get("custTypCd");
    List<String> restrictedCodesAddition = Arrays.asList("F", "G", "C", "D", "V", "W", "X");
    List<String> restrictedCodesRemoval = Arrays.asList("F", "G", "C", "D", "A", "B", "H", "M", "N");
    boolean hasNegativeCheck = false;
    for (UpdatedDataModel updatedDataModel : changes.getDataUpdates()) {
      if (updatedDataModel != null && !hasNegativeCheck) {
        LOG.debug("Checking updates for : " + new ObjectMapper().writeValueAsString(updatedDataModel));
        switch (updatedDataModel.getDataField()) {
        case "Tax Class / Code 1":
        case "Tax Class / Code 2":
        case "Tax Class / Code 3":
        case "Tax Exempt Status":
        case "ICC Tax Class":
        case "ICC Tax Exempt Status":
        case "Out of City Limits":
          boolean requesterFromTaxTeam = false;
          // TODO check if requester is from TaxTeam
          if (!requesterFromTaxTeam) {
            hasNegativeCheck = true;
          }
          break;
        case "CSO Site":
        case "Marketing Department":
        case "Marketing A/R Department":
          // set negative check status for FEDERAL Power of Attorney and BP
          if ((FEDERAL.equals(custTypeCd) || POWER_OF_ATTORNEY.equals(custTypeCd) || BUSINESS_PARTNER.equals(custTypeCd))) {
            hasNegativeCheck = true;
          }
          break;
        case "Miscellaneous Bill Code":
          for (String s : updatedDataModel.getOldData().split("")) {
            List<String> newCodes = Arrays.asList(updatedDataModel.getNewData().split(""));
            if (!newCodes.contains(s) && !restrictedCodesRemoval.contains(s) && !hasNegativeCheck) {
              hasNegativeCheck = true;
              break;
            }
          }
          for (String s : updatedDataModel.getNewData().split("")) {
            List<String> oldCodes = Arrays.asList(updatedDataModel.getOldData().split(""));
            if (!oldCodes.contains(s) && !restrictedCodesAddition.contains(s) && !hasNegativeCheck) {
              hasNegativeCheck = true;
              break;
            }
          }
          break;

        case "Abbreviated Name (TELX1)":
        case "PCC A/R Department":
        case "SVC A/R Office":
        case "Size Code":
        case "CAP Record":
          // SKIP THESE FIELDS
          break;
        default:
          // Set Negative check status for any other fields updated.
          hasNegativeCheck = true;
          break;
        }
      } else if (hasNegativeCheck) {
        break;
      }
    }

    if (hasNegativeCheck) {
      engineData.addNegativeCheckStatus("RESTRICED_DATA_UPDATED", "Updated elements cannot be checked automatically.");
      LOG.debug("Updated elements cannot be checked automatically.");
      output.setDetails("Updated elements cannot be checked automatically.\n");
      validation.setMessage("Review needed");
      validation.setSuccess(false);
    }

    return true;
  }

  @Override
  public boolean runUpdateChecksForAddress(EntityManager entityManager, AutomationEngineData engineData, RequestData requestData,
      RequestChangeContainer changes, AutomationResult<ValidationOutput> output, ValidationOutput validation) throws Exception {
    // init
    StringBuilder details = new StringBuilder();
    Data data = requestData.getData();
    Admin admin = requestData.getAdmin();
    String custTypCd = determineUSCMRDetails(entityManager, requestData, engineData).get("custTypCd");
    GEOHandler handler = RequestUtils.getGEOHandler(data.getCmrIssuingCntry());

    // check addresses
    if (StringUtils.isNotBlank(custTypCd) && !"NA".equals(custTypCd)) {
      if (LEASING.equals(custTypCd) || BUSINESS_PARTNER.equals(custTypCd)) {
        engineData.addNegativeCheckStatus("UPD_REVIEW_NEEDED",
            "Address updates for " + (custTypCd.equals(LEASING) ? "Leasing" : "Business Partner") + " scenario found.");
        details.append("Address updates for " + (custTypCd.equals(LEASING) ? "Leasing" : "Business Partner")
            + " scenario found. Processor review will be required.").append("\n");
        validation.setMessage("Review needed");
        validation.setSuccess(false);
      } else {
        List<String> addrTypesChanged = new ArrayList<String>();
        for (UpdatedNameAddrModel addrModel : changes.getAddressUpdates()) {
          addrTypesChanged.add(addrModel.getAddrType());
        }
        if (addrTypesChanged.contains(CmrConstants.ADDR_TYPE.ZS01)) {
          closelyMatchAddressWithDnbRecords(handler, requestData, engineData, "ZS01", details, validation);
        }

        if (addrTypesChanged.contains(CmrConstants.ADDR_TYPE.ZP01)) {
          Addr zp01 = requestData.getAddress("ZP01");
          boolean immutableAddrFound = false;
          List<String> immutableAddrList = Arrays.asList("150 KETTLETOWN RD", "6303 BARFIELD RD", "PO BOX 12195 BLDG 061", "1 N CASTLE DR",
              "7100 HIGHLANDS PKWY", "294 ROUTE 100", "6710 ROCKLEDGE DR");
          String addrTxt = zp01.getAddrTxt() + (StringUtils.isNotBlank(zp01.getAddrTxt2()) ? " " + zp01.getAddrTxt2() : "");
          for (String streetAddr : immutableAddrList) {
            if (addrTxt.contains(streetAddr)) {
              immutableAddrFound = true;
              break;
            }
          }
          if (immutableAddrFound) {
            engineData.addNegativeCheckStatus("IMMUTABLE_ADDR_FOUND", "Invoice-to address cannot be modified.");
            details.append("Invoice-to address cannot be modified.").append("\n");
            validation.setMessage("Review needed");
            validation.setSuccess(false);
          } else {
            closelyMatchAddressWithDnbRecords(handler, requestData, engineData, "ZP01", details, validation);
          }
        }
      }
    } else

    {
      validation.setSuccess(false);
      validation.setMessage("Unknown CustType");
      details.append("Customer Type could not be determined. Update checks for address could not be run.").append("\n");
      output.setOnError(true);
    }
    output.setDetails(details.toString());
    return true;
  }

  private void closelyMatchAddressWithDnbRecords(GEOHandler handler, RequestData requestData, AutomationEngineData engineData, String addrType,
      StringBuilder details, ValidationOutput validation) throws Exception {
    String addrDesc = "ZS01".equals(addrType) ? "Install-at" : "Invoice-at";
    Addr addr = requestData.getAddress(addrType);
    Data data = requestData.getData();
    Admin admin = requestData.getAdmin();
    MatchingResponse<DnBMatchingResponse> response = DnBUtil.getMatches(handler, requestData, engineData, addrType);
    if (response.getSuccess()) {
      if (response.getMatched() && !response.getMatches().isEmpty()) {
        if (DnBUtil.hasValidMatches(response)) {
          boolean isAddressMatched = false;
          for (DnBMatchingResponse record : response.getMatches()) {
            if (record.getConfidenceCode() > 7 && DnBUtil.closelyMatchesDnb(data.getCmrIssuingCntry(), addr, admin, record)) {
              isAddressMatched = true;
              break;
            }
          }
          if (isAddressMatched) {
            details.append(addrDesc + " address details matched successfully with High Quality D&B Matches.").append("\n");
            validation.setMessage("Validated.");
            validation.setSuccess(true);
          } else {
            engineData.addNegativeCheckStatus("DNB_MATCH_FAIL_" + addrType,
                "High confidence D&B matches did not match the " + addrDesc + " address data. ");
            details.append("High confidence D&B matches did not match the " + addrDesc + " address data.").append("\n");
            validation.setMessage("Review needed");
            validation.setSuccess(false);
          }
        } else {
          engineData.addNegativeCheckStatus("DNB_MATCH_FAIL_" + addrType, "No High Quality D&B Matches were found for " + addrDesc + " address.");
          details.append("No High Quality D&B Matches were found for " + addrDesc + " address.").append("\n");
          validation.setMessage("Review needed");
          validation.setSuccess(false);
        }
      } else {
        engineData.addNegativeCheckStatus("DNB_MATCH_FAIL_" + addrType, "No D&B Matches were found for " + addrDesc + " address.");
        details.append("No D&B Matches were found for " + addrDesc + " address.").append("\n");
        validation.setMessage("Review needed");
        validation.setSuccess(false);
      }
    } else {
      engineData.addNegativeCheckStatus("DNB_MATCH_FAIL_" + "ZS01", "D&B Matching couldn't be performed for " + addrDesc + " address.");
      details.append("D&B Matching couldn't be performed for " + addrDesc + " address.").append("\n");
      validation.setMessage("Review needed");
      validation.setSuccess(false);
    }
  }

  public static HashMap<String, String> determineUSCMRDetails(EntityManager entityManager, RequestData requestData, AutomationEngineData engineData)
      throws Exception {
    // get request admin and data
    HashMap<String, String> mapUSCMR = new HashMap<>();
    Admin admin = requestData.getAdmin();
    Data data = requestData.getData();
    String custTypCd = "NA";
    String entType = "";
    String leasingCo = "";
    String bpAccTyp = "";
    String cGem = "";
    String usRestricTo = "";
    String companyNo = "";

    String cmrNo = "";
    if ("C".equals(admin.getReqType())) {
      cmrNo = StringUtils.isBlank(admin.getModelCmrNo()) ? "" : admin.getModelCmrNo();
    } else if ("U".equals(admin.getReqType())) {
      cmrNo = StringUtils.isNotBlank(data.getCmrNo()) ? data.getCmrNo() : "";
    }
    String url = SystemConfiguration.getValue("CMR_SERVICES_URL");
    String usSchema = SystemConfiguration.getValue("US_CMR_SCHEMA");
    String sql = ExternalizedQuery.getSql("AUTO.GET_CODES_USCMR", usSchema);
    sql = StringUtils.replace(sql, ":CMR_NO", "'" + cmrNo + "'");
    String dbId = QueryClient.USCMR_APP_ID;

    QueryRequest query = new QueryRequest();
    query.setSql(sql);
    query.setRows(1);
    query.addField("I_ENT_TYPE");
    query.addField("I_BP_ACCOUNT_TYPE");
    query.addField("C_LEASING_CO");
    query.addField("C_GEM");
    query.addField("C_COM_RESTRCT_CODE");
    query.addField("I_CO");

    QueryClient client = CmrServicesFactory.getInstance().createClient(url, QueryClient.class);
    QueryResponse response = client.executeAndWrap(dbId, query, QueryResponse.class);

    if (!response.isSuccess()) {
      custTypCd = "NA";

    } else if (response.getRecords() == null || response.getRecords().size() == 0) {
      custTypCd = "NA";
    } else {
      Map<String, Object> record = response.getRecords().get(0);
      entType = (String) record.get("I_ENT_TYPE");
      leasingCo = (String) record.get("C_LEASING_CO");
      bpAccTyp = (String) record.get("I_BP_ACCOUNT_TYPE");
      cGem = (String) record.get("C_GEM");
      usRestricTo = (String) record.get("C_COM_RESTRCT_CODE");
      companyNo = String.valueOf(record.get("I_CO"));
      if ("P".equals(entType)) {
        custTypCd = POWER_OF_ATTORNEY;
      } else if ("F".equals(entType)) {
        custTypCd = FEDERAL;
      } else if ("I".equals(entType)) {
        custTypCd = INTERNAL;
      } else if ("C".equals(entType) || StringUtils.isNotBlank(leasingCo)) {
        custTypCd = LEASING;
      } else if (StringUtils.isNotBlank(bpAccTyp)) {
        custTypCd = BUSINESS_PARTNER;
      } else if ("2".equals(cGem)) {
        custTypCd = STATE_LOCAL;
      } else {
        custTypCd = COMMERCIAL;
      }
    }
    mapUSCMR.put("custTypCd", custTypCd);
    mapUSCMR.put("entType", entType);
    mapUSCMR.put("leasingCo", leasingCo);
    mapUSCMR.put("bpAccTyp", bpAccTyp);
    mapUSCMR.put("cGem", cGem);
    mapUSCMR.put("usRestricTo", usRestricTo);
    mapUSCMR.put("companyNo", companyNo);
    // System.out.println(mapUSCMR);
    return mapUSCMR;
  }

}
