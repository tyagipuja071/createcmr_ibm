/**
 * 
 */
package com.ibm.cio.cmr.request.automation.impl.gbl;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.automation.AutomationElementRegistry;
import com.ibm.cio.cmr.request.automation.AutomationEngineData;
import com.ibm.cio.cmr.request.automation.RequestData;
import com.ibm.cio.cmr.request.automation.impl.OverridingElement;
import com.ibm.cio.cmr.request.automation.out.AutomationResult;
import com.ibm.cio.cmr.request.automation.out.OverrideOutput;
import com.ibm.cio.cmr.request.automation.util.CoverageContainer;
import com.ibm.cio.cmr.request.automation.util.CoverageRulesFieldMap;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.util.RequestUtils;
import com.ibm.cio.cmr.request.util.SystemParameters;
import com.ibm.cio.cmr.request.util.SystemUtil;
import com.ibm.cio.cmr.request.util.geo.GEOHandler;
import com.ibm.cio.cmr.utils.coverage.CoverageRules;
import com.ibm.cio.cmr.utils.coverage.JarProperties;
import com.ibm.cio.cmr.utils.coverage.objects.CoverageInput;
import com.ibm.cio.cmr.utils.coverage.rules.Condition;
import com.ibm.cio.cmr.utils.coverage.rules.Coverage;
import com.ibm.cio.cmr.utils.coverage.rules.Rule;
import com.ibm.cmr.services.client.matching.gbg.GBGResponse;

/**
 * @author JeffZAMORA
 *
 */
public class CalculateCoverageElement extends OverridingElement {

  private static final Logger LOG = Logger.getLogger(CalculateCoverageElement.class);
  private static CoverageRules coverageRules;
  private Map<String, String> covDescriptions = new HashMap<String, String>();
  private boolean noInit = false;

  /**
   * @param requestTypes
   * @param actionOnError
   * @param overrideData
   * @param stopOnError
   * @throws Exception
   */
  public CalculateCoverageElement(String requestTypes, String actionOnError, boolean overrideData, boolean stopOnError) {
    super(requestTypes, actionOnError, overrideData, stopOnError);
  }

  /**
   * Initializes the local files for coverage computation
   * 
   * @throws Exception
   */
  private static synchronized void initCoverageRules() throws Exception {
    // determine ruleset
    String ruleSetId = SystemParameters.getString("COV_RULES_ID");
    if (StringUtils.isBlank(ruleSetId)) {
      Date dt = new Date();
      GregorianCalendar cal = new GregorianCalendar();
      cal.setTime(dt);
      int month = cal.get(Calendar.MONTH);
      SimpleDateFormat yrFormatter = new SimpleDateFormat("yyyy");
      ruleSetId = (month < 7 ? "1" : "2") + "H" + yrFormatter.format(dt);
    }
    LOG.debug("Initializing Coverage Rules for " + ruleSetId);

    coverageRules = new CoverageRules(ruleSetId);
    String zipFile = JarProperties.getProperty("jar.zip.dir.root");
    zipFile = zipFile + File.separator + ruleSetId + ".zip";
    File zip = new File(zipFile);
    if (!zip.exists()) {
      zipFile = JarProperties.getProperty("jar.zip.dir.root");
      zipFile = zipFile + File.separator + ruleSetId + ".jar";
    }
    coverageRules.initializeFrom(zipFile);
  }

  @Override
  public AutomationResult<OverrideOutput> executeElement(EntityManager entityManager, RequestData requestData, AutomationEngineData engineData)
      throws Exception {

    try {
      if (coverageRules == null) {
        try {
          initCoverageRules();
        } catch (Exception e) {
          LOG.error("Error in initialization", e);
          noInit = true;
        }
      }

      Admin admin = requestData.getAdmin();
      Data data = requestData.getData();
      long reqId = admin.getId().getReqId();
      AutomationResult<OverrideOutput> result = buildResult(reqId);
      OverrideOutput output = new OverrideOutput(false);
      if (this.noInit) {
        result.setProcessOutput(output);
        result.setResults("Cannot Start");
        result.setOnError(true);
        result.setDetails("Coverage calculation cannot be done because of a system configuration issue.");
      }
      StringBuilder details = new StringBuilder();

      GBGResponse computedGbg = (GBGResponse) engineData.get(AutomationEngineData.GBG_MATCH);
      String gbgId = null;
      String bgId = null;
      String covFrom = "XXX";
      if (computedGbg != null) {
        bgId = computedGbg.getBgId();
        gbgId = computedGbg.getGbgId();
        covFrom = "BG_CALC";
      } else if (!StringUtils.isBlank(data.getBgId())) {
        bgId = data.getBgId();
        gbgId = data.getGbgId();
        covFrom = "BG_ODM";
      }

      boolean coverageNotFound = false;
      List<CoverageContainer> coverages = null;
      boolean withCmrData = false;
      if (bgId != null) {
        coverages = computeCoverageFromBuyingGroup(entityManager, bgId, data.getCmrIssuingCntry());
        if (coverages == null || coverages.isEmpty()) {
          CoverageInput inputBG = extractCoverageInput(entityManager, requestData, data, requestData.getAddress("ZS01"), gbgId, bgId);
          List<Coverage> currCoverage = coverageRules.findCoverage(inputBG);
          if (currCoverage != null && !currCoverage.isEmpty()) {
            CoverageContainer cont = new CoverageContainer();
            cont.setFinalCoverage(currCoverage.get(0).getType() + currCoverage.get(0).getId());
            cont.setFinalCoverageRules(coverageRules.findRule(currCoverage.get(0).getType() + currCoverage.get(0).getId()));
            if (currCoverage.size() > 1) {
              cont.setBaseCoverage(currCoverage.get(1).getType() + currCoverage.get(1).getId());
              cont.setBaseCoverageRules(coverageRules.findRule(currCoverage.get(1).getType() + currCoverage.get(1).getId()));
            }
            coverages.add(cont);
          }
        } else {
          withCmrData = true;
        }
      } else if (StringUtils.isNotEmpty(data.getVat())) {
        coverages = computeCoverageFromVAT(entityManager, data.getVat(), data.getCmrIssuingCntry());
        if (coverages == null || coverages.isEmpty()) {
          CoverageInput inputBG = extractCoverageInput(entityManager, requestData, data, requestData.getAddress("ZS01"), gbgId, bgId);
          List<Coverage> currCoverage = coverageRules.findCoverage(inputBG);
          if (currCoverage != null && !currCoverage.isEmpty()) {
            CoverageContainer cont = new CoverageContainer();
            cont.setFinalCoverage(currCoverage.get(0).getType() + currCoverage.get(0).getId());
            cont.setFinalCoverageRules(coverageRules.findRule(currCoverage.get(0).getType() + currCoverage.get(0).getId()));
            if (currCoverage.size() > 1) {
              cont.setBaseCoverage(currCoverage.get(1).getType() + currCoverage.get(1).getId());
              cont.setBaseCoverageRules(coverageRules.findRule(currCoverage.get(1).getType() + currCoverage.get(1).getId()));
            }
            coverages.add(cont);
          }
        } else {
          withCmrData = true;
        }
        covFrom = "COV_VAT";
      } else if (!StringUtils.isBlank(data.getCovId())) {
        try {
          coverages = getCoverageRuleForID(data.getCovId());
        } catch (Exception e) {
          coverages = new ArrayList<CoverageContainer>();
          coverageNotFound = true;
        }
        covFrom = "COV_ODM";
      }

      CoverageInput input = extractCoverageInput(entityManager, requestData, data, requestData.getAddress("ZS01"), data.getGbgId(), data.getBgId());
      List<Coverage> currCoverage = coverageRules.findCoverage(input);
      if (!currCoverage.isEmpty()) {
        details.append("\nCoverage IDs computed based on current data:").append("\n");
        for (Coverage cov : currCoverage) {
          String desc = getCoverageDescription(entityManager, cov.getType() + cov.getId());
          if (StringUtils.isBlank(desc)) {
            desc = "-no description available-";
          }
          details.append(" - " + cov.getLevel() + ": " + cov.getType() + cov.getId() + " (" + desc + ")\n");
        }
      }

      input = extractCoverageInput(entityManager, requestData, data, requestData.getAddress("ZS01"), gbgId, bgId);
      if (coverages != null && !coverages.isEmpty()) {
        result.setResults("Calculated");
        switch (covFrom) {
        case "BG_CALC":
          details.append("\nCoverages from Calculated Buying Group ID " + bgId + " (" + gbgId + ")" + (withCmrData ? "[from current CMRs]" : ""))
              .append("\n");
          break;
        case "BG_ODM":
          details.append("\nCoverages from ODM Projected Buying Group ID " + bgId + " (" + gbgId + ")" + (withCmrData ? "[from current CMRs]" : ""))
              .append("\n");
          break;
        case "COV_VAT":
          details.append("\nCoverages from CMR VAT data for VAT Number " + data.getVat() + (withCmrData ? " [from current CMRs]" : "")).append("\n");
          break;
        case "COV_ODM":
          details.append("\nCoverage from ODM Projected/User Specified Coverage ID " + data.getCovId()).append("\n");
          break;
        }

        List<String> coverageIds = new ArrayList<String>();
        for (CoverageContainer container : coverages) {

          LOG.debug("Logging Final Coverage ID: " + container.getFinalCoverage());
          logCoverage(entityManager, engineData, coverageIds, details, output, input, container.getFinalCoverage(), "Final",
              container.getFinalCoverageRules(), data.getCmrIssuingCntry());
          // Save the first calculated coverage ID in the engine data to allow
          // next elements to use it
          if (engineData.get(AutomationEngineData.COVERAGE_ID) == null) {
            engineData.put(AutomationEngineData.COVERAGE_ID, container.getFinalCoverage());
          }
          if (container.getBaseCoverage() != null) {
            LOG.debug("Logging Base Coverage ID: " + container.getBaseCoverage());
            logCoverage(entityManager, engineData, coverageIds, details, output, input, container.getBaseCoverage(), "Base",
                container.getBaseCoverageRules(), data.getCmrIssuingCntry());
          }
        }
        engineData.addPositiveCheckStatus(AutomationEngineData.COVERAGE_CALCULATED);
      } else if (!currCoverage.isEmpty()) {
        result.setResults("Calculated");
        details.append("Coverage calculated based only on request data. No projected Buying Group found.");
      } else {
        result.setResults("Cannot Calculate");
        result.setOnError(true);
        if (coverageNotFound) {
          details.append("Coverage ID " + data.getCovId() + " not found in the coverage rules.");
          engineData.addRejectionComment("Coverage ID " + data.getCovId() + " not found in the coverage rules.");
        } else {
          details.append("Coverage cannot be calculated. Missing calculated or projected Buying Group, and ODM determined Coverage");
          engineData.addRejectionComment("Coverage cannot be calculated. Missing calculated or projected Buying Group, and ODM determined Coverage");
        }

      }
      result.setDetails(details.toString());
      result.setProcessOutput(output);
      return result;
    } catch (Throwable t) {
      LOG.error("Error in coverage element", t);
      Admin admin = requestData.getAdmin();
      long reqId = admin.getId().getReqId();
      AutomationResult<OverrideOutput> result = buildResult(reqId);
      result.setDetails("Coverage Missing or Invalid.");
      result.setResults("Error");
      result.setProcessOutput(new OverrideOutput(false));

      return result;
    }
  }

  /**
   * Logs the coverage information to the request and results
   * 
   * @param entityManager
   * @param coverageIds
   * @param details
   * @param output
   * @param input
   * @param currCovId
   * @param currCovLevel
   * @param currCovRules
   */
  private void logCoverage(EntityManager entityManager, AutomationEngineData engineData, List<String> coverageIds, StringBuilder details,
      OverrideOutput output, CoverageInput input, String currCovId, String currCovLevel, List<Rule> currCovRules, String cmrIssuingCntry) {
    // boolean tempVarForPhase2 = true;
    if (!coverageIds.contains(currCovId)) {
      GEOHandler handler = RequestUtils.getGEOHandler(cmrIssuingCntry);
      details.append("\nCoverage ID = " + currCovId).append(" (" + currCovLevel + ")").append("\n");
      details.append("Coverage Name = " + getCoverageDescription(entityManager, currCovId)).append("\n");
      List<Rule> rules = coverageRules.matchWithCoverage(input, currCovId);
      boolean matched = false;
      if (rules != null && !rules.isEmpty()) {
        matched = true;
      } else {
        details.append("No matched coverage rules from request data. The request may need to be tweaked to align based on conditions below.")
            .append("\n");
        rules = currCovRules;
      }
      if (rules != null && !rules.isEmpty()) {
        if (matched) {
          details.append("Matched rules for Coverage ID " + currCovId + ":").append("\n");
        } else {
          details.append("Proposed rules for Coverage ID " + currCovId + ":").append("\n");
        }
        int index = 1;
        String dtName = null;
        for (Rule rule : rules) {
          dtName = rule.getDecisionTable();
          if (dtName.contains(".")) {
            dtName = dtName.substring(dtName.lastIndexOf(".") + 1);
          }
          details.append("\nRule " + index + " = " + rule.getName()).append("\n");
          details.append("Decision Table = " + dtName).append("\n");
          details.append("Conditions:").append("\n");
          for (Condition condition : rule.getRuleConditions()) {
            if (!"COVERAGE".equals(condition.getField()) && !"coverageID".equals(condition.getField())) {
              String field = CoverageRulesFieldMap.getLabel(cmrIssuingCntry, condition.getField());
              details.append(" - " + field + " " + condition.getOperation().toString() + " " + condition.getValues()
                  + (condition.getValues2() != null && !condition.getValues2().isEmpty() ? ", " + condition.getValues2() : "")).append("\n");

              if (!matched && index == 1) {
                // for now do not execute this at all, just show details
                String dbField = CoverageRulesFieldMap.getDBField(cmrIssuingCntry, condition.getField());
                if (!StringUtils.isBlank(dbField)) {
                  boolean addr = dbField.startsWith("ADDR");
                  if (addr) {
                    dbField = dbField.substring(4);
                  }
                  String val = condition.getFirstUsableValue();
                  if (val != null && !val.startsWith("!")) {
                    String operation = condition.getOperation().toString();
                    switch (operation) {
                    case "In":
                    case "StartsOrEndsWith":
                    case "Equals":
                      output.addOverride(getProcessCode(), addr ? "ZS01" : "DATA", dbField, "", val);
                      break;
                    case "StartsWith":
                      String uiField = null;
                      if (handler != null) {
                        Map<String, String> uiFieldMap = handler.getUIFieldIdMap();
                        if (uiFieldMap != null && !uiFieldMap.isEmpty()) {
                          String tempDbField = WordUtils.capitalizeFully(dbField, new char[] { '_' }).replaceAll("_", "");
                          tempDbField = tempDbField.substring(0, 1).toLowerCase() + (tempDbField.length() > 1 ? tempDbField.substring(1) : "");
                          for (String key : uiFieldMap.keySet()) {
                            if (tempDbField.equals(uiFieldMap.get(key))) {
                              uiField = key;
                              break;
                            }
                          }
                        }
                      }

                      if (StringUtils.isNotEmpty(uiField)) {
                        String sql = ExternalizedQuery.getSql("AUTOMATION.GET_LOV_STARTSWITH");
                        PreparedQuery query = new PreparedQuery(entityManager, sql);
                        query.setParameter("FIELD_ID", uiField);
                        query.setParameter("CMR_ISSUING_CNTRY", cmrIssuingCntry);
                        query.setParameter("CD", val + "%");
                        query.setForReadOnly(true);
                        String value = query.getSingleResult(String.class);
                        if (StringUtils.isNotBlank(value)) {
                          output.addOverride(getProcessCode(), addr ? "ZS01" : "DATA", dbField, "", val);
                        } else {
                          engineData.addNegativeCheckStatus(dbField,
                              "The override value could not be determined for the field - " + field + " during coverage calculation.");
                        }
                      } else {
                        engineData.addNegativeCheckStatus(dbField,
                            "The override value could not be determined for the field - " + field + " during coverage calculation.");
                      }
                      break;
                    default:
                      engineData.addNegativeCheckStatus(dbField,
                          "The override value could not be determined for the field - " + field + " during coverage calculation.");
                      break;
                    }
                  } else {
                    engineData.addNegativeCheckStatus(dbField,
                        "The override value could not be determined for the field - " + field + " during coverage calculation.");
                  }
                }
              }
            }
          }
          index++;
        }
      }
      coverageIds.add(currCovId);
    }
  }

  /**
   * Gets the coverage descriptions
   * 
   * @param entityManager
   * @param covId
   * @return
   */
  private String getCoverageDescription(EntityManager entityManager, String covId) {
    if (StringUtils.isBlank(covId)) {
      return "";
    }
    if (this.covDescriptions.containsKey(covId)) {
      return this.covDescriptions.get(covId);
    }
    String field = null;
    String table = null;

    String type = covId.substring(0, 1);
    String id = covId.substring(1);
    switch (type) {
    case "T":
      field = "TERRITORYID";
      table = "SAPR3.TERRITORY";
      break;
    case "P":
      field = "POOLID";
      table = "SAPR3.POOL";
      break;
    case "A":
      field = "CLUSTERID";
      table = "SAPR3.CLUSTER";
      break;
    case "I":
      field = "INACID";
      table = "SAPR3.INT_ACCOUNT";
      break;
    }
    LOG.debug("Getting description for coverage " + covId);
    String sql = "select NAME from " + table + " where MANDT = :MANDT and " + field + " = :ID";
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
    query.setParameter("ID", id.trim());
    query.setForReadOnly(true);
    List<String> names = query.getResults(1, String.class);
    if (names != null && !names.isEmpty()) {
      this.covDescriptions.put(covId, names.get(0));
      return names.get(0);
    }
    this.covDescriptions.put(covId, "");
    return "";
  }

  /**
   * Using the buying group ID, checks existing coverages from records from the
   * same country
   * 
   * @param entityManager
   * @param bgId
   * @param cmrIssuingCountry
   * @return
   */
  private List<CoverageContainer> computeCoverageFromBuyingGroup(EntityManager entityManager, String bgId, String cmrIssuingCountry) {
    List<CoverageContainer> coverages = new ArrayList<>();
    String sql = ExternalizedQuery.getSql("AUTO.COV.GET_COV_FROM_BG");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("BG_ID", bgId);
    query.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
    query.setParameter("COUNTRY", cmrIssuingCountry);
    query.setForReadOnly(true);

    LOG.debug("Getting coverages for Buying Group " + bgId + " under Country " + cmrIssuingCountry);
    List<Object[]> results = query.getResults(5);
    if (results != null && !results.isEmpty()) {
      for (Object[] coverage : results) {
        // 0 - final coverage
        // 1 - base coverage
        // 3 - territory id

        CoverageContainer container = new CoverageContainer();
        container.setFinalCoverage((String) coverage[0]);
        List<Rule> rule = coverageRules.findRule(container.getFinalCoverage());
        container.setFinalCoverageRules(rule);

        if (coverage[1] != null && !StringUtils.isBlank((String) coverage[1])) {
          container.setBaseCoverage((String) coverage[1]);
          rule = coverageRules.findRule(container.getBaseCoverage());
          container.setBaseCoverageRules(rule);
        }
        container.setTerritoryId((String) coverage[2]);

        coverages.add(container);
      }
    }

    return coverages;

  }

  /**
   * Using the buying group ID, checks existing coverages from records from the
   * same country
   * 
   * @param entityManager
   * @param bgId
   * @param cmrIssuingCountry
   * @return
   */
  private List<CoverageContainer> computeCoverageFromVAT(EntityManager entityManager, String vat, String cmrIssuingCountry) {
    List<CoverageContainer> coverages = new ArrayList<>();
    String sql = ExternalizedQuery.getSql("AUTO.COV.GET_COV_FROM_VAT");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("VAT", vat);
    query.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
    query.setParameter("COUNTRY", cmrIssuingCountry);
    query.setForReadOnly(true);

    LOG.debug("Getting coverages for VAT " + vat + " under Country " + cmrIssuingCountry);
    List<Object[]> results = query.getResults(5);
    if (results != null && !results.isEmpty()) {
      for (Object[] coverage : results) {
        // 0 - final coverage
        // 1 - base coverage
        // 3 - territory id

        CoverageContainer container = new CoverageContainer();
        container.setFinalCoverage((String) coverage[0]);
        List<Rule> rule = coverageRules.findRule(container.getFinalCoverage());
        container.setFinalCoverageRules(rule);

        if (coverage[1] != null && !StringUtils.isBlank((String) coverage[1])) {
          container.setBaseCoverage((String) coverage[1]);
          rule = coverageRules.findRule(container.getBaseCoverage());
          container.setBaseCoverageRules(rule);
        }
        container.setTerritoryId((String) coverage[2]);

        coverages.add(container);
      }
    }

    return coverages;

  }

  private CoverageInput extractCoverageInput(EntityManager entityManager, RequestData requestData, Data data, Addr addr, String gbgId, String bgId)
      throws Exception {
    CoverageInput coverage = new CoverageInput();

    coverage.setAffiliateGroup(data.getAffiliate());
    coverage.setClassification(data.getCustClass());
    coverage.setCompanyNumber(data.getCompany());
    coverage.setSegmentation("IBM".equals(data.getCmrOwner()) ? null : data.getCmrOwner());
    String isoCntryCd = SystemUtil.getISOCountryCode(data.getCmrIssuingCntry());
    LOG.debug("System Location: " + data.getCmrIssuingCntry() + " ISO Code: " + isoCntryCd);
    coverage.setCountryCode(isoCntryCd != null ? isoCntryCd : data.getCmrIssuingCntry());
    coverage.setDb2ID(data.getCmrNo());
    coverage.setEnterprise(data.getEnterprise());
    coverage.setGbQuadSectorTier(data.getClientTier());
    coverage.setInac(data.getInacCd());
    coverage.setIndustryClass(data.getSubIndustryCd());
    coverage
        .setIndustryCode(data.getSubIndustryCd() != null && data.getSubIndustryCd().length() > 0 ? data.getSubIndustryCd().substring(0, 1) : null);
    coverage.setIndustrySolutionUnit(data.getIsuCd());
    coverage.setNationalTaxID(data.getTaxCd1());
    coverage.setSORTL(data.getSearchTerm());
    coverage.setUnISIC(data.getIsicCd());
    coverage.setSitePartyID(data.getSitePartyId());
    coverage.setCity(addr.getCity1());
    coverage.setCounty(addr.getCounty());
    coverage.setPostalCode(addr.getPostCd());
    if (!"''".equals(addr.getStateProv())) {
      coverage.setStatePrefectureCode(addr.getStateProv());
    }
    coverage.setPhysicalAddressCountry(addr.getLandCntry());

    coverage.setGlobalBuyingGroupID(gbgId);
    coverage.setDomesticBuyingGroupID(bgId);
    coverage.setGeoLocationCode(data.getGeoLocationCd());

    GEOHandler geoHandler = RequestUtils.getGEOHandler(data.getCmrIssuingCntry());
    if (geoHandler != null) {
      geoHandler.convertCoverageRulesInput(entityManager, coverage, addr, requestData.createModelFromRequest());
    }

    return coverage;
  }

  /**
   * Extracts the rules which were satisfied by the coverage id
   * 
   * @param coverageId
   * @return
   */
  private List<CoverageContainer> getCoverageRuleForID(String coverageId) {
    List<CoverageContainer> coverages = new ArrayList<>();
    List<Rule> rule = coverageRules.findRule(coverageId);
    CoverageContainer container = new CoverageContainer();
    container.setBaseCoverage(coverageId);
    container.setBaseCoverageRules(rule);
    coverages.add(container);
    return coverages;
  }

  @Override
  public String getProcessCode() {
    return AutomationElementRegistry.GBL_CALC_COV;
  }

  @Override
  public String getProcessDesc() {
    return "Calculate Coverage";
  }

  @Override
  public boolean isNonImportable() {
    return true;
  }

}
