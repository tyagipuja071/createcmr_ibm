package com.ibm.cio.cmr.request.automation.util.geo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

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
  private static final String COMMERCIAL = "1";
  private static final String POWER_OF_ATTORNEY = "6";
  private static final String FEDERAL = "4";
  private static final String INTERNAL = "5";
  private static final String LEASING = "3";
  private static final String BUSINESS_PARTNER = "7";
  private static final String STATE_LOCAL = "2";

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
    return false;
  }

  @Override
  public boolean runUpdateChecksForAddress(EntityManager entityManager, AutomationEngineData engineData, RequestData requestData,
      RequestChangeContainer changes, AutomationResult<ValidationOutput> output, ValidationOutput validation) throws Exception {
    // init
    StringBuilder details = new StringBuilder();
    Data data = requestData.getData();
    Admin admin = requestData.getAdmin();
    String custTypCd = determineUSCMRDetails(entityManager, requestData, engineData).get("custTypeCd");
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
          boolean immutableAddrFound = false;
          // TODO check if street address belongs to immutable address list
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
    String addrDesc = "ZS01".equals(addrType) ? "Install-At" : "Invoice-To";
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

    String cmrNo = StringUtils.isBlank(admin.getModelCmrNo()) ? "" : admin.getModelCmrNo();
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
        custTypCd = "6";
      } else if ("F".equals(entType)) {
        custTypCd = "4";
      } else if ("I".equals(entType)) {
        custTypCd = "5";
      } else if ("C".equals(entType) || StringUtils.isNotBlank(leasingCo)) {
        custTypCd = "3";
      } else if (StringUtils.isNotBlank(bpAccTyp)) {
        custTypCd = "7";
      } else if ("2".equals(cGem)) {
        custTypCd = "2";
      } else {
        custTypCd = "1";
      }
    }
    mapUSCMR.put("custTypCd", custTypCd);
    mapUSCMR.put("entType", entType);
    mapUSCMR.put("leasingCo", leasingCo);
    mapUSCMR.put("bpAccTyp", bpAccTyp);
    mapUSCMR.put("cGem", cGem);
    mapUSCMR.put("usRestricTo", usRestricTo);
    mapUSCMR.put("companyNo", companyNo);
    System.out.println(mapUSCMR);
    return mapUSCMR;
  }

}
