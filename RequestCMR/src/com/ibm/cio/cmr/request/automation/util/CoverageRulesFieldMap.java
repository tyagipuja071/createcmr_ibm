/**
 * 
 */
package com.ibm.cio.cmr.request.automation.util;

import java.util.HashMap;
import java.util.Map;

import com.ibm.cio.cmr.utils.coverage.rules.Condition;

/**
 * Translation of coverage rules {@link Condition} fields to logical names
 * 
 * @author JeffZAMORA
 *
 */
public class CoverageRulesFieldMap {

  private static Map<String, String> logicalFieldMap = new HashMap<String, String>();
  private static Map<String, String> physicalFieldMap = new HashMap<String, String>();

  static {
    logicalFieldMap.put("db2ID", "Internal ID");
    logicalFieldMap.put("sitePartyID", "iERP Site ID");
    logicalFieldMap.put("countryCode", "Country");
    logicalFieldMap.put("statePrefectureCode", "State");
    logicalFieldMap.put("postalCode", "Postal Code");
    logicalFieldMap.put("geoLocationCode", "GEO Location Code");
    logicalFieldMap.put("gbQuadSectorTier", "Client Tier");
    logicalFieldMap.put("industryClass", "Subindustry Code");
    logicalFieldMap.put("industrySolutionUnit", "ISU");
    logicalFieldMap.put("unISIC", "ISIC");
    logicalFieldMap.put("requestType", "Request Type");
    logicalFieldMap.put("industryCode", "Industry Code");
    logicalFieldMap.put("county", "County");
    logicalFieldMap.put("localSIC", "Local SIC");
    logicalFieldMap.put("physicalAddressCountry", "Country (Address)");
    logicalFieldMap.put("city", "City");
    logicalFieldMap.put("nationalTaxID", "National Tax ID");
    logicalFieldMap.put("classification", "Customer Class");
    logicalFieldMap.put("segmentation", "CMR Owner");
    logicalFieldMap.put("inac", "INAC");
    logicalFieldMap.put("enterprise", "Enterprise");
    logicalFieldMap.put("affiliateGroup", "Affiliate");
    logicalFieldMap.put("companyNumber", "Company");
    logicalFieldMap.put("globalUltimateClientID", "Global Ultimate Client ID");
    logicalFieldMap.put("globalClientID", "Global Client ID");
    logicalFieldMap.put("domesticClientID", "Domestic Client ID");
    logicalFieldMap.put("subClientID", "Subclient ID");
    logicalFieldMap.put("globalBuyingGroupID", "Global Buying Group");
    logicalFieldMap.put("domesticBuyingGroupID", "Buying Group");
    logicalFieldMap.put("SORTL", "Search Term (SORTL)");
    logicalFieldMap.put("COVERAGE", "Coverage ID");
    logicalFieldMap.put("coverageID", "Coverage ID");

    physicalFieldMap.put("statePrefectureCode", "ADDR.STATE_PROV");
    physicalFieldMap.put("postalCode", "ADDR.POST_CD");
    physicalFieldMap.put("geoLocationCode", "GEO_LOCATION_CD");
    physicalFieldMap.put("gbQuadSectorTier", "ORDER_BLOCK");
    physicalFieldMap.put("industryClass", "SUB_INDUSTRY_CD");
    physicalFieldMap.put("industrySolutionUnit", "ISU_CD");
    physicalFieldMap.put("unISIC", "ISIC_CD");
    physicalFieldMap.put("county", "ADDR.COUNTY");
    physicalFieldMap.put("physicalAddressCountry", "ADDR.LANDED_CNTRY");
    physicalFieldMap.put("city", "ADDR.CITY1");
    physicalFieldMap.put("nationalTaxID", "VAT");
    physicalFieldMap.put("classification", "CUST_CLASS");
    physicalFieldMap.put("segmentation", "CMR_OWNER");
    physicalFieldMap.put("inac", "INAC");
    physicalFieldMap.put("enterprise", "ENTERPRISE");
    physicalFieldMap.put("affiliateGroup", "AFFILIATE");
    physicalFieldMap.put("companyNumber", "COMPANY");
    physicalFieldMap.put("globalBuyingGroupID", "GBG_ID");
    physicalFieldMap.put("domesticBuyingGroupID", "BG_ID");
    physicalFieldMap.put("SORTL", "SEARCH_TERM");

  };

  public static String getLabel(String key) {
    return logicalFieldMap.get(key);
  }

  public static String getDBField(String key) {
    return physicalFieldMap.get(key);
  }

}
