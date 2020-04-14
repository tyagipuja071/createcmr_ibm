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
import com.ibm.cio.cmr.request.automation.out.FieldResult;
import com.ibm.cio.cmr.request.automation.out.FieldResultKey;
import com.ibm.cio.cmr.request.automation.out.OverrideOutput;
import com.ibm.cio.cmr.request.automation.util.AutomationUtil;
import com.ibm.cio.cmr.request.automation.util.CoverageContainer;
import com.ibm.cio.cmr.request.automation.util.CoverageRulesFieldMap;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.ui.PageManager;
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
 * @author RoopakChugh
 *
 */
public class CalculateCoverageElement extends OverridingElement {

  private static final Logger LOG = Logger.getLogger(CalculateCoverageElement.class);
  private static CoverageRules coverageRules;
  private Map<String, String> covDescriptions = new HashMap<String, String>();
  private boolean noInit = false;
  public static final String BG_CALC = "BG_CALC";
  public static final String BG_ODM = "BG_ODM";
  public static final String COV_REQ = "COV_REQ";
  public static final String COV_VAT = "COV_VAT";
  public static final String COV_ODM = "COV_ODM";
  public static final String NONE = "NONE";

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

    // init
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
      CoverageContainer calculatedCoverageContainer = new CoverageContainer();
      boolean coverageNotFound = false;
      boolean isCoverageCalculated = false;
      String negativeCheck = "";
      List<CoverageContainer> coverages = null;
      boolean withCmrData = false;
      StringBuilder details = new StringBuilder();

      // check if coverage rules are initialized or not
      if (this.noInit) {
        result.setProcessOutput(output);
        result.setResults("Cannot Start");
        result.setOnError(true);
        result.setDetails("Coverage calculation cannot be done because of a system configuration issue.");
        return result;
      }

      GBGResponse computedGbg = (GBGResponse) engineData.get(AutomationEngineData.GBG_MATCH);
      String gbgId = null;
      String bgId = null;
      String covFrom = "XXX";
      if (computedGbg != null) {
        bgId = computedGbg.getBgId();
        gbgId = computedGbg.getGbgId();
        covFrom = BG_CALC;
      } else if (!StringUtils.isBlank(data.getBgId())) {
        bgId = data.getBgId();
        gbgId = data.getGbgId();
        covFrom = BG_ODM;
      }
      if (bgId != null) {
        coverages = computeCoverageFromRDCQuery(entityManager, "AUTO.COV.GET_COV_FROM_BG", bgId, data.getCmrIssuingCntry());
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
        coverages = computeCoverageFromRDCQuery(entityManager, "AUTO.COV.GET_COV_FROM_VAT", data.getVat(), data.getCmrIssuingCntry());
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
        covFrom = COV_VAT;
      } else if (!StringUtils.isBlank(data.getCovId())) {
        try {
          coverages = getCoverageRuleForID(data.getCovId());
        } catch (Exception e) {
          coverages = new ArrayList<CoverageContainer>();
          coverageNotFound = true;
        }
        covFrom = COV_ODM;
      }
      // get default coverage
      String defaultCoverage = getDefaultCoverage(data.getCmrIssuingCntry());
      boolean showCurrentCoverage = false;
      CoverageInput input = null;
      List<Coverage> currCoverage = new ArrayList<>();

      // for now don't show, CMDE doesn't want it shown
      if (showCurrentCoverage) {
        // get coverage based on current request data
        input = extractCoverageInput(entityManager, requestData, data, requestData.getAddress("ZS01"), data.getGbgId(), data.getBgId());
        currCoverage = coverageRules.findCoverage(input);
        if (!currCoverage.isEmpty()) {
          details.append("\nCoverage IDs computed based on current data:").append("\n");
          for (Coverage cov : currCoverage) {
            String desc = getCoverageDescription(entityManager, cov.getType() + cov.getId());
            if (StringUtils.isBlank(desc)) {
              desc = "-no description available-";
            }
            details.append(" - " + cov.getLevel() + ": " + cov.getType() + cov.getId() + " (" + desc + ")\n");
            if (StringUtils.isBlank(calculatedCoverageContainer.getFinalCoverage())) {
              calculatedCoverageContainer.setFinalCoverage(cov.getType() + cov.getId());
            } else if (StringUtils.isBlank(calculatedCoverageContainer.getBaseCoverage())) {
              calculatedCoverageContainer.setBaseCoverage(cov.getType() + cov.getId());
            }
          }
        }

      }

      // coverage id calculation from BG/VAT
      input = extractCoverageInput(entityManager, requestData, data, requestData.getAddress("ZS01"), gbgId, bgId);
      if (coverages != null && !coverages.isEmpty()) {
        switch (covFrom) {
        case BG_CALC:
          details.append("\nCoverages from Calculated Buying Group ID " + bgId + " (" + gbgId + ")" + (withCmrData ? "[from current CMRs]" : ""))
              .append("\n");
          break;
        case BG_ODM:
          details.append("\nCoverages from ODM Projected Buying Group ID " + bgId + " (" + gbgId + ")" + (withCmrData ? "[from current CMRs]" : ""))
              .append("\n");
          break;
        case COV_VAT:
          details.append("\nCoverages from CMR VAT data for VAT Number " + data.getVat() + (withCmrData ? " [from current CMRs]" : "")).append("\n");
          break;
        case COV_ODM:
          details.append("\nCoverage from ODM Projected/User Specified Coverage ID " + data.getCovId()).append("\n");
          break;
        }

        List<String> coverageIds = new ArrayList<String>();
        for (CoverageContainer container : coverages) {
          LOG.debug("Logging Final Coverage ID: " + container.getFinalCoverage());
          logCoverage(entityManager, engineData, requestData, coverageIds, details, output, input, container.getFinalCoverage(), "Final",
              container.getFinalCoverageRules(), data.getCmrIssuingCntry(), container);

          boolean logBaseCoverage = false;

          if (logBaseCoverage) {
            // don't log base for now
            if (container.getBaseCoverage() != null) {
              LOG.debug("Logging Base Coverage ID: " + container.getBaseCoverage());
              logCoverage(entityManager, engineData, requestData, coverageIds, details, output, input, container.getBaseCoverage(), "Base",
                  container.getBaseCoverageRules(), data.getCmrIssuingCntry(), container);
            }
          }

          // Save the first calculated coverage ID in the engine data to allow
          // next elements to use it
          if (!isCoverageCalculated
              && (StringUtils.isNotBlank(container.getFinalCoverage()) || StringUtils.isNotBlank(container.getBaseCoverage()))) {
            String finalCoverage = container.getFinalCoverage();
            if (StringUtils.isNotBlank(finalCoverage)) {
              if (!finalCoverage.equals(calculatedCoverageContainer.getFinalCoverage()) && !finalCoverage.equals(defaultCoverage)) {
                result.setResults("Calculated");
                isCoverageCalculated = true;
              } else if (finalCoverage.equals(calculatedCoverageContainer.getFinalCoverage())) {
                result.setResults("Review Needed");
                negativeCheck = "Calculated Coverage is same as the coverage calculated using Request Data.";
                details.append("\nCalculated Coverage is same as the coverage calculated using Request Data.").append("\n");
              } else if (finalCoverage.equals(defaultCoverage)) {
                result.setResults("Default Coverage");
                negativeCheck = "Calculated Coverage is same as the Default Coverage.";
                details.append("\nCalculated Coverage is same as the Default Coverage.").append("\n");
              }
            }
            calculatedCoverageContainer = container;

          }
        }
      } else if (!currCoverage.isEmpty()) {
        if (calculatedCoverageContainer.getFinalCoverage().equals(defaultCoverage)) {
          result.setResults("Default Coverage");
          details.append("\nCoverage calculated based on request data is same as the Default Coverage. No projected Buying Group found.")
              .append("\n");
        } else {
          result.setResults("Calculated");
          details.append("\nCoverage calculated based only on request data. No projected Buying Group found.").append("\n");
          isCoverageCalculated = true;
          covFrom = COV_REQ;
        }
      } else {
        result.setResults("Cannot Calculate");
        covFrom = NONE;
      }

      // hook to perform country specific operations

      // if it is the first calculated coverage, use it to do country
      // specific coverage calculations
      boolean hasCountryCheck = false;
      AutomationUtil countryUtil = AutomationUtil.getNewCountryUtil(data.getCmrIssuingCntry());
      if (countryUtil != null) {
        // hook to perform calculations and update results
        hasCountryCheck = countryUtil.performCountrySpecificCoverageCalculations(this, entityManager, result, details, output, requestData,
            engineData, covFrom, calculatedCoverageContainer, isCoverageCalculated);
      }

      if (!hasCountryCheck) {
        if ("NONE".equals(covFrom)) {
          result.setOnError(true);
          if (coverageNotFound) {
            details.append("Coverage ID " + data.getCovId() + " not found in the coverage rules.");
            engineData.addRejectionComment("Coverage ID " + data.getCovId() + " not found in the coverage rules.");
          } else {
            details.append("Coverage cannot be calculated. Missing calculated or projected Buying Group, and ODM determined Coverage");
            engineData
                .addRejectionComment("Coverage cannot be calculated. Missing calculated or projected Buying Group, and ODM determined Coverage");
          }
        } else if (!isCoverageCalculated) {
          engineData.addNegativeCheckStatus("COVERAGE_ERROR",
              StringUtils.isNotBlank(negativeCheck) ? negativeCheck : "Coverage calculation requires processor review.");
        } else if (isCoverageCalculated) {
          engineData.addPositiveCheckStatus(AutomationEngineData.COVERAGE_CALCULATED);
          if (calculatedCoverageContainer != null) {
            engineData.put(AutomationEngineData.COVERAGE_CALCULATED, calculatedCoverageContainer.getFinalCoverage());
          }
        }
      }

      result.setDetails(details.toString());
      result.setProcessOutput(output);
      Map<FieldResultKey, FieldResult> keys = output.getData();
      return result;
    } catch (

    Throwable t) {
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

  private String getDefaultCoverage(String cmrIssuingCntry) throws Exception {
    CoverageInput input = new CoverageInput();
    String isoCntryCd = SystemUtil.getISOCountryCode(cmrIssuingCntry);
    LOG.debug("System Location: " + cmrIssuingCntry + " ISO Code: " + isoCntryCd);
    input.setCountryCode(isoCntryCd != null ? isoCntryCd : cmrIssuingCntry);
    List<Coverage> coverages = coverageRules.findCoverage(input);
    if (coverages != null && !coverages.isEmpty()) {
      Coverage coverage = coverages.get(0);
      return (coverage.getType() + coverage.getId());
    }
    return null;
  }

  /**
   * Logs the coverage information to the request and results
   * 
   * @param entityManager
   * @param requestData
   * @param coverageIds
   * @param details
   * @param output
   * @param input
   * @param currCovId
   * @param currCovLevel
   * @param currCovRules
   * @param container
   */
  public void logCoverage(EntityManager entityManager, AutomationEngineData engineData, RequestData requestData, List<String> coverageIds,
      StringBuilder details, OverrideOutput output, CoverageInput input, String currCovId, String currCovLevel, List<Rule> currCovRules,
      String cmrIssuingCntry, CoverageContainer container) {
    // boolean tempVarForPhase2 = true;
    if (coverageIds == null) {
      // check for null
      coverageIds = new ArrayList<>();
    }
    if (!coverageIds.contains(currCovId)) {
      GEOHandler handler = RequestUtils.getGEOHandler(cmrIssuingCntry);
      details.append("\nCoverage ID = " + currCovId).append(" (" + currCovLevel + ")").append("\n");
      details.append("Coverage Name = " + getCoverageDescription(entityManager, currCovId)).append("\n");
      List<Rule> rules = currCovRules;
      // List<Rule> rules = coverageRules.matchWithCoverage(input, currCovId);
      // boolean matched = false;
      // if (rules != null && !rules.isEmpty()) {
      // matched = true;
      // } else {
      // details.append("No matched coverage rules from request data. The
      // request may need to be tweaked to align based on conditions below.")
      // .append("\n");
      // rules = currCovRules;
      // }
      if (rules != null && !rules.isEmpty()) {
        // if (matched) {
        // details.append("Matched rules for Coverage ID " + currCovId +
        // ":").append("\n");
        // } else {
        // details.append("Proposed rules for Coverage ID " + currCovId +
        // ":").append("\n");
        // }
        details.append("Overrides for " + currCovId + ":").append("\n");
        int index = 1;
        String dtName = null;
        for (Rule rule : rules) {
          dtName = rule.getDecisionTable();
          if (dtName.contains(".")) {
            dtName = dtName.substring(dtName.lastIndexOf(".") + 1);
          }
          // details.append("\nRule " + index + " = " +
          // rule.getName()).append("\n");
          // details.append("Decision Table = " + dtName).append("\n");
          // details.append("Conditions:").append("\n");
          for (Condition condition : rule.getRuleConditions()) {
            if (!"COVERAGE".equals(condition.getField()) && !"coverageID".equals(condition.getField())) {
              String field = CoverageRulesFieldMap.getLabel(cmrIssuingCntry, condition.getField());
              // details.append(" - " + field + " " +
              // condition.getOperation().toString() + " " +
              // condition.getValues()
              // + (condition.getValues2() != null &&
              // !condition.getValues2().isEmpty() ? ", " +
              // condition.getValues2() : "")).append("\n");

              // Create overrides if not matched for the first rule found
              // 1. Creates overrides for Final Coverage if it is there
              // 2. Creates overrides for base Coverage if Final Coverage is not
              // present

              if (index == 1 && (("Final".equals(currCovLevel) && StringUtils.isNotBlank(currCovId))
                  || ("Base".equals(currCovLevel) && StringUtils.isBlank(container.getFinalCoverage())))) {
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
                      details.append(" - " + (addr ? "[Main Addr] " : "") + field + " = " + val + "\n");
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
                          details.append(" - " + (addr ? "[Main Addr] " : "") + field + " = " + val + "\n");
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
          if (StringUtils.isNotBlank(container.getIsuCd()) && StringUtils.isNotBlank(container.getClientTierCd())) {
            details.append(" - ISU Code = " + container.getIsuCd()).append("\n");
            details.append(" - Client Tier = " + container.getClientTierCd()).append("\n");
            output.addOverride(getProcessCode(), "DATA", "ISU_CD", requestData.getData().getIsuCd(), container.getIsuCd());
            output.addOverride(getProcessCode(), "DATA", "CLIENT_TIER", requestData.getData().getClientTier(), container.getClientTierCd());
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
   * Using the <strong>key</strong> provided calculate the Coverage on the basis
   * of the specified <strong>sqlKey</strong> RDC query
   * 
   * 
   * @param entityManager
   * @param sqlKey
   * @param key
   * @param cmrIssuingCountry
   * @return
   */
  public List<CoverageContainer> computeCoverageFromRDCQuery(EntityManager entityManager, String sqlKey, String key, String cmrIssuingCountry) {
    List<CoverageContainer> coverages = new ArrayList<>();
    String sql = ExternalizedQuery.getSql(sqlKey);
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("KEY", key);
    query.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
    query.setParameter("COUNTRY", cmrIssuingCountry);
    String isoCntry = PageManager.getDefaultLandedCountry(cmrIssuingCountry);
    System.err.println("ISO: " + isoCntry);
    query.setParameter("ISO_CNTRY", isoCntry);
    query.setForReadOnly(true);

    LOG.debug("Getting coverages using query " + sqlKey + " under Country " + cmrIssuingCountry + " for key: " + key);
    List<Object[]> results = query.getResults(5);
    if (results != null && !results.isEmpty()) {
      for (Object[] coverage : results) {
        // 0 - base coverage
        // 1 - final coverage
        // 2 - ISU
        // 3 - CTC

        CoverageContainer container = new CoverageContainer();
        container.setFinalCoverage((String) coverage[0]);
        container.setBaseCoverage((String) coverage[0]);
        if (!StringUtils.isBlank((String) coverage[1])) {
          container.setFinalCoverage((String) coverage[1]);
        }

        List<Rule> rule = coverageRules.findRule(container.getFinalCoverage());
        container.setFinalCoverageRules(rule);
        rule = coverageRules.findRule(container.getBaseCoverage());
        container.setBaseCoverageRules(rule);

        container.setIsuCd((String) coverage[2]);
        container.setClientTierCd((String) coverage[3]);

        if (container.getFinalCoverageRules() != null) {
          coverages.add(container);
        }

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

}
