package com.ibm.cio.cmr.request.automation.util.geo;

import java.util.List;

import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.automation.AutomationElementRegistry;
import com.ibm.cio.cmr.request.automation.AutomationEngineData;
import com.ibm.cio.cmr.request.automation.RequestData;
import com.ibm.cio.cmr.request.automation.out.AutomationResult;
import com.ibm.cio.cmr.request.automation.out.OverrideOutput;
import com.ibm.cio.cmr.request.automation.out.ValidationOutput;
import com.ibm.cio.cmr.request.automation.util.AutomationUtil;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.util.RequestUtils;
import com.ibm.cio.cmr.request.util.dnb.DnBUtil;
import com.ibm.cmr.services.client.dnb.DnBCompany;
import com.ibm.cmr.services.client.matching.dnb.DnBMatchingResponse;
import com.ibm.cmr.services.client.matching.gbg.GBGFinderRequest;

public class BeLuxUtil extends AutomationUtil {

  private static final Logger LOG = Logger.getLogger(BeLuxUtil.class);
  public static final String SCENARIO_LOCAL_COMMERCIAL = "BECOM";
  public static final String SCENARIO_CROSS_COMMERCIAL = "CBCOM";
  public static final String SCENARIO_LOCAL_PUBLIC = "BEPUB";
  public static final String SCENARIO_BP_LOCAL = "BEBUS";
  public static final String SCENARIO_BP_CROSS = "CBBUS";
  public static final String SCENARIO_PRIVATE_CUSTOMER = "BEPRI";
  public static final String SCENARIO_THIRD_PARTY = "BE3PA";
  public static final String SCENARIO_INTERNAL = "BEINT";
  public static final String SCENARIO_INTERNAL_SO = "BEISO";
  // Lux
  public static final String SCENARIO_CROSS_LU = "LUCRO";
  public static final String SCENARIO_LOCAL_PUBLIC_LU = "LUPUB";
  public static final String SCENARIO_PRIVATE_CUSTOMER_LU = "LUPRI";
  public static final String SCENARIO_LOCAL_COMMERCIAL_LU = "LUCOM";
  public static final String SCENARIO_INTERNAL_LU = "LUINT";
  public static final String SCENARIO_INTERNAL_SO_LU = "LUISO";
  public static final String SCENARIO_THIRD_PARTY_LU = "LU3PA";
  public static final String SCENARIO_BP_LOCAL_LU = "LUBUS";

  @Override
  public AutomationResult<OverrideOutput> doCountryFieldComputations(EntityManager entityManager, AutomationResult<OverrideOutput> results,
      StringBuilder details, OverrideOutput overrides, RequestData requestData, AutomationEngineData engineData) throws Exception {
	  Admin admin = requestData.getAdmin();
	    Data data = requestData.getData();
	    String scenario = data.getCustSubGrp();
	    if (!"C".equals(admin.getReqType())) {
	      details.append("Field Computation skipped for Updates.");
	      results.setResults("Skipped");
	      results.setDetails(details.toString());
	      return results;
	    }
	    if (SCENARIO_INTERNAL_SO.equals(scenario) || SCENARIO_INTERNAL_SO_LU.equals(scenario)) {
	      Addr zi01 = requestData.getAddress("ZI01");
	      boolean highQualityMatchExists = false;
	      List<DnBMatchingResponse> response = getMatches(requestData, engineData, zi01, false);
	      if (response != null && response.size() > 0) {
	        for (DnBMatchingResponse dnbRecord : response) {
	          boolean closelyMatches = DnBUtil.closelyMatchesDnb(data.getCmrIssuingCntry(), zi01, admin, dnbRecord);
	          if (closelyMatches) {
	            engineData.put("ZI01_DNB_MATCH", dnbRecord);
	            highQualityMatchExists = true;
	            details.append("High Quality DnB Match found for Installing address.\n");
	            details.append(" - Confidence Code:  " + dnbRecord.getConfidenceCode() + " \n");
	            details.append(" - DUNS No.:  " + dnbRecord.getDunsNo() + " \n");
	            details.append(" - Name:  " + dnbRecord.getDnbName() + " \n");
	            details.append(" - Address:  " + dnbRecord.getDnbStreetLine1() + " " + dnbRecord.getDnbCity() + " " + dnbRecord.getDnbPostalCode() + " "
	                + dnbRecord.getDnbCountry() + "\n\n");
	            details.append("Overriding ISIC and Sub Industry Code using DnB Match retrieved.\n");
	            LOG.debug("Connecting to D&B details service..");
	            DnBCompany dnbData = DnBUtil.getDnBDetails(dnbRecord.getDunsNo());
	            if (dnbData != null) {
	              overrides.addOverride(AutomationElementRegistry.GBL_FIELD_COMPUTE, "DATA", "ISIC_CD", data.getIsicCd(), dnbData.getIbmIsic());
	              details.append("ISIC =  " + dnbData.getIbmIsic() + " (" + dnbData.getIbmIsicDesc() + ")").append("\n");
	              String subInd = RequestUtils.getSubIndustryCd(entityManager, dnbData.getIbmIsic(), data.getCmrIssuingCntry());
	              if (subInd != null) {
	                overrides.addOverride(AutomationElementRegistry.GBL_FIELD_COMPUTE, "DATA", "SUB_INDUSTRY_CD", data.getSubIndustryCd(), subInd);
	                details.append("Subindustry Code  =  " + subInd).append("\n");
	              }
	            }
	            results.setResults("Calculated.");
	            results.setProcessOutput(overrides);
	            break;
	          }
	        }
	      }
	      if (!highQualityMatchExists && "C".equals(admin.getReqType())) {
	        LOG.debug("No High Quality DnB Match found for Installing address.");
	        details.append("No High Quality DnB Match found for Installing address. Request will require CMDE review before proceeding.").append("\n");
	        engineData.addNegativeCheckStatus("NOMATCHFOUND",
	            "No High Quality DnB Match found for Installing address. Request cannot be processed automatically.");
	      }
	    } else {
	      details.append("No specific fields to calculate.");
	      results.setResults("Skipped.");
	      results.setProcessOutput(overrides);
	    }
	    results.setDetails(details.toString());
	    LOG.debug(results.getDetails());
	    return results;
	  }

	 

  @Override
  public boolean performScenarioValidation(EntityManager entityManager, RequestData requestData, AutomationEngineData engineData,
      AutomationResult<ValidationOutput> result, StringBuilder details, ValidationOutput output) {

    Data data = requestData.getData();
    String scenario = data.getCustSubGrp();
    Addr zs01 = requestData.getAddress("ZS01");
    Addr zi01 = requestData.getAddress("ZI01");
    String customerName = zs01.getCustNm1();
    Addr zp01 = requestData.getAddress("ZP01");
    String customerNameZP01 = "";
    String landedCountryZP01 = "";
    if (zp01 != null) {
      customerNameZP01 = StringUtils.isBlank(zp01.getCustNm1()) ? "" : zp01.getCustNm1();
      landedCountryZP01 = StringUtils.isBlank(zp01.getLandCntry()) ? "" : zp01.getLandCntry();
    }
    if ("C".equals(requestData.getAdmin().getReqType())) {
      // remove duplicates
      removeDuplicateAddresses(entityManager, requestData, details);
    }

    if ((SCENARIO_BP_LOCAL.equals(scenario) || SCENARIO_BP_CROSS.equals(scenario) || SCENARIO_BP_LOCAL_LU.equals(scenario)) && zp01 != null
        && (!StringUtils.equals(getCleanString(customerName), getCleanString(customerNameZP01))
            || !StringUtils.equals(zs01.getLandCntry(), landedCountryZP01))) {
      details.append("Customer Name and Landed Country on Sold-to and Bill-to address should be same for BP Scenario.").append("\n");
      engineData.addNegativeCheckStatus("SOLDTO_BILLTO_DIFF",
          "Customer Name and Landed Country on Sold-to and Bill-to address should be same for BP Scenario.");
    }

    switch (scenario) {

    case SCENARIO_LOCAL_COMMERCIAL:
    case SCENARIO_CROSS_COMMERCIAL:
    case SCENARIO_LOCAL_COMMERCIAL_LU:
    case SCENARIO_LOCAL_PUBLIC:
    case SCENARIO_LOCAL_PUBLIC_LU:
      if (zp01 != null && (!StringUtils.equals(getCleanString(customerName), getCleanString(customerNameZP01))
          || !StringUtils.equals(zs01.getLandCntry(), landedCountryZP01))) {
        details.append("Customer Name and Landed Country on Sold-to and Bill-to address should be same for Commercial and Public Customer Scenario.")
            .append("\n");
        engineData.addNegativeCheckStatus("SOLDTO_BILLTO_DIFF",
            "Customer Name and Landed Country on Sold-to and Bill-to address should be same for Commercial and Public Customer Scenario.");
      }
      break;

    case SCENARIO_BP_LOCAL:
    case SCENARIO_BP_CROSS:
    case SCENARIO_BP_LOCAL_LU:
      return doBusinessPartnerChecks(engineData, data.getPpsceid(), details);
    
    case SCENARIO_INTERNAL_SO:
    case SCENARIO_INTERNAL_SO_LU:
      if (zi01 == null) {
        details.append("Install-at address should be present for Interna SO Scenario.").append("\n");
        engineData.addRejectionComment("OTH", "Install-at address should be present for Internal SO Scenario.", "", "");
        return false;
      }
      
    case SCENARIO_PRIVATE_CUSTOMER:
    case SCENARIO_PRIVATE_CUSTOMER_LU:
      return doPrivatePersonChecks(engineData, data.getCmrIssuingCntry(), zs01.getLandCntry(), customerName, details, false, requestData);

    case SCENARIO_THIRD_PARTY:
    case SCENARIO_THIRD_PARTY_LU:
      details.append("Processor Review will be required for Third Party Scenario/Data Center.\n");
      engineData.addNegativeCheckStatus("Scenario_Validation", "3rd Party/Data Center request will require CMDE review before proceeding.\n");
      break;
    }

    return true;
  }
}
