/**
 * 
 */
package com.ibm.cio.cmr.request.automation.util;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.ibm.cio.cmr.request.util.SystemLocation;
import com.ibm.cio.cmr.utils.coverage.rules.Condition;

/**
 * Translation of coverage rules {@link Condition} fields to logical names
 * 
 * @author JeffZAMORA
 *
 */
public class CoverageRulesFieldMap {

  private static final String DEFAULT = "DEFAULT";
  private static Map<String, HashMap<String, String>> logicalFieldMap = new HashMap<String, HashMap<String, String>>();
  private static Map<String, HashMap<String, String>> physicalFieldMap = new HashMap<String, HashMap<String, String>>();

  static {
    addLogicalFieldMapping(DEFAULT, "db2ID", "Internal ID");
    addLogicalFieldMapping(DEFAULT, "sitePartyID", "iERP Site ID");
    addLogicalFieldMapping(DEFAULT, "countryCode", "Country");
    addLogicalFieldMapping(DEFAULT, "statePrefectureCode", "State");
    addLogicalFieldMapping(DEFAULT, "postalCode", "Postal Code");
    addLogicalFieldMapping(DEFAULT, "geoLocationCode", "GEO Location Code");
    addLogicalFieldMapping(DEFAULT, "gbQuadSectorTier", "Client Tier");
    addLogicalFieldMapping(DEFAULT, "industryClass", "Subindustry Code");
    addLogicalFieldMapping(DEFAULT, "industrySolutionUnit", "ISU");
    addLogicalFieldMapping(DEFAULT, "unISIC", "ISIC");
    addLogicalFieldMapping(DEFAULT, "requestType", "Request Type");
    addLogicalFieldMapping(DEFAULT, "industryCode", "Industry Code");
    addLogicalFieldMapping(DEFAULT, "county", "County");
    addLogicalFieldMapping(DEFAULT, "localSIC", "Local SIC");
    addLogicalFieldMapping(DEFAULT, "physicalAddressCountry", "Country (Address)");
    addLogicalFieldMapping(DEFAULT, "city", "City");
    addLogicalFieldMapping(DEFAULT, "nationalTaxID", "National Tax ID");
    addLogicalFieldMapping(DEFAULT, "classification", "Customer Class");
    addLogicalFieldMapping(DEFAULT, "segmentation", "CMR Owner");
    addLogicalFieldMapping(DEFAULT, "inac", "INAC");
    addLogicalFieldMapping(DEFAULT, "enterprise", "Enterprise");
    addLogicalFieldMapping(DEFAULT, "affiliateGroup", "Affiliate");
    addLogicalFieldMapping(DEFAULT, "companyNumber", "Company");
    addLogicalFieldMapping(DEFAULT, "globalUltimateClientID", "Global Ultimate Client ID");
    addLogicalFieldMapping(DEFAULT, "globalClientID", "Global Client ID");
    addLogicalFieldMapping(DEFAULT, "domesticClientID", "Domestic Client ID");
    addLogicalFieldMapping(DEFAULT, "subClientID", "Subclient ID");
    addLogicalFieldMapping(DEFAULT, "globalBuyingGroupID", "Global Buying Group");
    addLogicalFieldMapping(DEFAULT, "domesticBuyingGroupID", "Buying Group");
    addLogicalFieldMapping(DEFAULT, "SORTL", "Search Term (SORTL)");
    addLogicalFieldMapping(DEFAULT, "COVERAGE", "Coverage ID");
    addLogicalFieldMapping(DEFAULT, "coverageID", "Coverage ID");

    addPhysicalFieldMapping(DEFAULT, "statePrefectureCode", "ADDR.STATE_PROV");
    addPhysicalFieldMapping(DEFAULT, "postalCode", "ADDR.POST_CD");
    addPhysicalFieldMapping(DEFAULT, "geoLocationCode", "GEO_LOCATION_CD");
    addPhysicalFieldMapping(DEFAULT, "gbQuadSectorTier", "CLIENT_TIER");
    addPhysicalFieldMapping(DEFAULT, "industryClass", "SUB_INDUSTRY_CD");
    addPhysicalFieldMapping(DEFAULT, "industrySolutionUnit", "ISU_CD");
    addPhysicalFieldMapping(DEFAULT, "unISIC", "ISIC_CD");
    addPhysicalFieldMapping(DEFAULT, "county", "ADDR.COUNTY");
    // addPhysicalFieldMapping(DEFAULT, "physicalAddressCountry",
    // "ADDR.LANDED_CNTRY");
    addPhysicalFieldMapping(DEFAULT, "city", "ADDR.CITY1");
    addPhysicalFieldMapping(DEFAULT, "nationalTaxID", "VAT");
    addPhysicalFieldMapping(DEFAULT, "classification", "CUST_CLASS");
    addPhysicalFieldMapping(DEFAULT, "segmentation", "CMR_OWNER");
    addPhysicalFieldMapping(DEFAULT, "inac", "INAC");
    addPhysicalFieldMapping(DEFAULT, "enterprise", "ENTERPRISE");
    addPhysicalFieldMapping(DEFAULT, "affiliateGroup", "AFFILIATE");
    addPhysicalFieldMapping(DEFAULT, "companyNumber", "COMPANY");
    addPhysicalFieldMapping(DEFAULT, "globalBuyingGroupID", "GBG_ID");
    addPhysicalFieldMapping(DEFAULT, "domesticBuyingGroupID", "BG_ID");
    addPhysicalFieldMapping(DEFAULT, "SORTL", "SEARCH_TERM");

    // Country specific mappings
    addLogicalFieldMapping(SystemLocation.INDIA, "SORTL", "Cluster");
    addPhysicalFieldMapping(SystemLocation.INDIA, "SORTL", "AP_CUST_CLUSTER_ID");

    addLogicalFieldMapping(SystemLocation.FRANCE, "SORTL", "Sales BO Code");
    addPhysicalFieldMapping(SystemLocation.FRANCE, "SORTL", "SALES_BO_CD");

    addLogicalFieldMapping(SystemLocation.AUSTRIA, "SORTL", "Sales BO Code");
    addPhysicalFieldMapping(SystemLocation.AUSTRIA, "SORTL", "SALES_BO_CD");

  };

  private static void addLogicalFieldMapping(String country, String key, String label) {
    if (StringUtils.isNotBlank(country) && StringUtils.isNotBlank(key) && StringUtils.isNotBlank(label)) {
      if (logicalFieldMap.containsKey(country)) {
        logicalFieldMap.get(country).put(key, label);
      } else {
        HashMap<String, String> countryMap = new HashMap<String, String>();
        countryMap.put(key, label);
        logicalFieldMap.put(country, countryMap);
      }
    }
  }

  private static void addPhysicalFieldMapping(String country, String key, String label) {
    if (StringUtils.isNotBlank(country) && StringUtils.isNotBlank(key) && StringUtils.isNotBlank(label)) {
      if (physicalFieldMap.containsKey(country)) {
        physicalFieldMap.get(country).put(key, label);
      } else {
        HashMap<String, String> countryMap = new HashMap<String, String>();
        countryMap.put(key, label);
        physicalFieldMap.put(country, countryMap);
      }
    }
  }

  public static String getLabel(String country, String key) {
    if (logicalFieldMap.containsKey(country)) {
      if (logicalFieldMap.get(country).containsKey(key)) {
        return logicalFieldMap.get(country).get(key);
      }
    }
    return logicalFieldMap.get(DEFAULT).get(key);
  }

  public static String getDBField(String country, String key) {
    if (physicalFieldMap.containsKey(country)) {
      if (physicalFieldMap.get(country).containsKey(key)) {
        return physicalFieldMap.get(country).get(key);
      }
    }
    return physicalFieldMap.get(DEFAULT).get(key);
  }

}
