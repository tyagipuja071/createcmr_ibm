/**
 * 
 */
package com.ibm.cio.cmr.request.automation.impl.gbl;

import java.io.File;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;
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
  public static final String FINAL = "Final";
  public static final String BASE = "Base";

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
      // String negativeCheck = "";
      List<CoverageContainer> coverages = null;
      boolean withCmrData = false;
      StringBuilder details = new StringBuilder();

      // added flow to skip gbg matching
      if (engineData.hasPositiveCheckStatus(AutomationEngineData.SKIP_COVERAGE)) {
        // ensure a GBG is set
        String covId = (String) engineData.get(AutomationEngineData.COVERAGE_CALCULATED);
        if (covId != null) {
          details.append("Coverage already computed by external process: ");
          details.append("\n").append("Coverage ID: " + covId);
          result.setDetails(details.toString());
          result.setResults("Skipped");
          result.setProcessOutput(output);
          return result;
        }
      }

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
        coverages = computeCoverageFromRDCQuery(entityManager, "AUTO.COV.GET_COV_FROM_BG", bgId, data.getCmrIssuingCntry(), false);
        if (coverages != null && !coverages.isEmpty()) {
          CoverageContainer preferredCoverage = coverages.get(0);
          if (preferredCoverage.getFinalCoverageRules() == null) {
            details.append("The preferred coverage '" + preferredCoverage.getFinalCoverage() + "' determined using Buying Group '" + bgId
                + "' was not found in coverage rules.").append("\n");
            details.append("Proceeding with other calculated coverages - ").append("\n");
            engineData.addNegativeCheckStatus("PREFERRED_COVERAGE_ERROR", "The preferred coverage '" + preferredCoverage.getFinalCoverage()
                + "' determined using Buying Group '" + bgId + "' was not found in coverage rules.");
            coverages = getValidCoverages(coverages);
          }
        }

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
      } else if (!StringUtils.isBlank(data.getCovId()) && data.getCovId().matches("[AITP]{1}[0-9]{7}")) {
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
        boolean logNegativeCheck = true;
        for (CoverageContainer container : coverages) {
          LOG.debug("Logging Final Coverage ID: " + container.getFinalCoverage());
          logCoverage(entityManager, engineData, requestData, coverageIds, details, output, container, FINAL, computedGbg, logNegativeCheck);
          logNegativeCheck = false;

          boolean logBaseCoverage = false;

          if (logBaseCoverage && StringUtils.isBlank(container.getFinalCoverage())) {
            // don't log base for now
            if (container.getBaseCoverage() != null) {
              LOG.debug("Logging Base Coverage ID: " + container.getBaseCoverage());
              logCoverage(entityManager, engineData, requestData, coverageIds, details, output, container, BASE, computedGbg, false);
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
                // negativeCheck = "Calculated Coverage is same as the coverage
                // calculated using Request Data.";
                details.append("\nCalculated Coverage is same as the coverage calculated using Request Data.").append("\n");
              } else if (finalCoverage.equals(defaultCoverage)) {
                result.setResults("Default Coverage");
                // negativeCheck = "Calculated Coverage is same as the Default
                // Coverage.";
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
          // result.setOnError(true);
          if (coverageNotFound) {
            details.append("Coverage ID " + data.getCovId() + " not found in the coverage rules.");
            // engineData.addRejectionComment("Coverage ID " + data.getCovId() +
            // " not found in the coverage rules.");
          } else {
            result.setResults("Skipped");
            details.append("No projected Buying Group or ODM Coverage found, coverage adjustments will be skipped.");
            // engineData
            // .addRejectionComment("Coverage cannot be calculated. Missing
            // calculated or projected Buying Group, and ODM determined
            // Coverage");
          }
        } else if (!isCoverageCalculated) {
          details.setLength(0);
          details.append("Coverage could not be calculated.");
        } else if (isCoverageCalculated) {
          engineData.addPositiveCheckStatus(AutomationEngineData.COVERAGE_CALCULATED);
          if (calculatedCoverageContainer != null) {
            engineData.put(AutomationEngineData.COVERAGE_CALCULATED, calculatedCoverageContainer.getFinalCoverage());
          }
        }
      }

      result.setDetails(details.toString());
      result.setProcessOutput(output);
      // Map<FieldResultKey, FieldResult> keys = output.getData();
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
   * @param engineData
   * @param requestData
   * @param coverageIds
   * @param details
   * @param output
   * @param coverageContainer
   * @param currCovLevel
   * @param logNegativeCheck
   */
  public void logCoverage(EntityManager entityManager, AutomationEngineData engineData, RequestData requestData, List<String> coverageIds,
      StringBuilder details, OverrideOutput output, CoverageContainer coverageContainer, String currCovLevel, GBGResponse gbg,
      boolean logNegativeCheck) {
    if (coverageIds == null) {
      // check for null
      coverageIds = new ArrayList<>();
    }

    Data data = requestData.getData();
    String cmrIssuingCntry = data.getCmrIssuingCntry();
    String currCovId = "";
    List<Rule> currCovRules = null;
    if (FINAL.equals(currCovLevel)) {
      currCovId = coverageContainer.getFinalCoverage();
      currCovRules = coverageContainer.getFinalCoverageRules();
    } else if (BASE.equals(currCovLevel)) {
      currCovId = coverageContainer.getBaseCoverage();
      currCovRules = coverageContainer.getBaseCoverageRules();
    }

    if (!coverageIds.contains(currCovId)) {
      // GEOHandler handler = RequestUtils.getGEOHandler(cmrIssuingCntry);
      // create overrides for first logged/manually logged coverage only
      boolean createOverrides = coverageIds.isEmpty();
      if (currCovRules != null && !currCovRules.isEmpty()) {
        details.append("\nCoverage ID = " + currCovId).append(" (" + currCovLevel + ")").append("\n");
        details.append("Coverage Name = " + getCoverageDescription(entityManager, currCovId)).append("\n");
        details.append("Overrides for " + currCovId + ":").append("\n");
        Rule rule = currCovRules.get(0);
        Map<String, String> notDeterminedFields = new HashMap<String, String>();
        for (Condition condition : rule.getRuleConditions()) {
          if (!"COVERAGE".equals(condition.getField()) && !"coverageID".equals(condition.getField())) {
            String field = CoverageRulesFieldMap.getLabel(cmrIssuingCntry, condition.getField());
            String dbField = CoverageRulesFieldMap.getDBField(cmrIssuingCntry, condition.getField());
            if (!StringUtils.isBlank(dbField)) {
              boolean addr = dbField.startsWith("ADDR");
              if (addr) {
                dbField = dbField.substring(4);
              }
              String val = condition.getFirstUsableValue();
              String fieldValue = "";
              if (addr) {
                fieldValue = getColumnValueFromAddr(requestData.getAddress("ZS01"), dbField);
              } else {
                fieldValue = getColumnValueFromData(requestData.getData(), dbField);
              }
              if (val != null && !val.startsWith("!")) {
                String operation = condition.getOperation().toString();
                switch (operation) {
                case "StartsOrEndsWith":
                case "Equals":
                  details.append(" - " + (addr ? "[Main Addr] " : "") + field + " = " + val + "\n");
                  if (createOverrides) {
                    output.addOverride(getProcessCode(), addr ? "ZS01" : "DATA", dbField, "", val);
                  }
                  break;
                case "StartsWith":
                  if (StringUtils.isNotBlank(fieldValue) && fieldValue.startsWith(val)) {
                    details.append(" - " + (addr ? "[Main Addr] " : "") + field + " = " + val + "\n");
                  } else {
                    notDeterminedFields.put(field, val);
                    // if we need to create an override check in lov for the
                    // values
                    // String uiField = null;
                    // if (handler != null) {
                    // Map<String, String> uiFieldMap =
                    // handler.getUIFieldIdMap();
                    // if (uiFieldMap != null && !uiFieldMap.isEmpty()) {
                    // String tempDbField = WordUtils.capitalizeFully(dbField,
                    // new char[] { '_' }).replaceAll("_", "");
                    // tempDbField = tempDbField.substring(0, 1).toLowerCase()
                    // + (tempDbField.length() > 1 ? tempDbField.substring(1)
                    // : "");
                    // for (String key : uiFieldMap.keySet()) {
                    // if (tempDbField.equals(uiFieldMap.get(key))) {
                    // uiField = key;
                    // break;
                    // }
                    // }
                    // }
                    // }
                    //
                    // if (StringUtils.isNotEmpty(uiField)) {
                    // String sql =
                    // ExternalizedQuery.getSql("AUTOMATION.GET_LOV_STARTSWITH");
                    // PreparedQuery query = new PreparedQuery(entityManager,
                    // sql);
                    // query.setParameter("FIELD_ID", uiField);
                    // query.setParameter("CMR_ISSUING_CNTRY",
                    // cmrIssuingCntry);
                    // query.setParameter("CD", val + "%");
                    // query.setForReadOnly(true);
                    // String value = query.getSingleResult(String.class);
                    // if (StringUtils.isNotBlank(value)) {
                    // details.append(" - " + (addr ? "[Main Addr] " : "") +
                    // field + " = " + val + "\n");
                    // if (createOverrides) {
                    // output.addOverride(getProcessCode(), addr ? "ZS01" :
                    // "DATA", dbField, "", val);
                    // }
                    // } else {
                    // notDeterminedFields.put(field, val);
                    // }
                    // } else {
                    // notDeterminedFields.put(field, val);
                    // }
                  }
                  break;
                case "In":
                  if (containsValue(condition, fieldValue)) {
                    // no override using first value, the list already
                    // contains current value
                    details.append(" - " + (addr ? "[Main Addr] " : "") + field + " = " + fieldValue + "\n");
                    break;
                  } else {
                    if (condition.getValues() != null) {
                      details.append(" - " + (addr ? "[Main Addr] " : "") + field + " = " + condition.getValues() + " [" + fieldValue + "]\n");
                    } else {
                      details.append(" - " + (addr ? "[Main Addr] " : "") + field + " = " + val + "\n");
                    }

                    if (createOverrides) {
                      output.addOverride(getProcessCode(), addr ? "ZS01" : "DATA", dbField, "", val);
                    }
                  }
                  break;
                default:
                  notDeterminedFields.put(field, val);
                  break;
                }
              } else if (val != null && val.startsWith("!")) {
                String value = val.substring(1);
                if (StringUtils.isNotBlank(fieldValue) && !fieldValue.startsWith(value)) {
                  details.append(" - " + (addr ? "[Main Addr] " : "") + field + " = " + val + "\n");
                } else {
                  notDeterminedFields.put(field, val);
                }
              } else {
                notDeterminedFields.put(field, val);
              }
            }
          }
        }
        if (!notDeterminedFields.isEmpty()) {
          details.append("\nOverrides for following fields could not be determined:").append("\n");
          for (String key : notDeterminedFields.keySet()) {
            String val = notDeterminedFields.get(key);
            details.append(" - " + key + " = " + (StringUtils.isNotBlank(val) ? val : "- no value defined -") + "\n");
            if (logNegativeCheck) {
              engineData.addNegativeCheckStatus(key, "The override value could not be determined for '" + key + "' during coverage calculation.");
            }
          }
        }

        if (StringUtils.isNotBlank(coverageContainer.getIsuCd())) {
          details.append("\nOverrides based on CMR data:").append("\n");
          details.append(" - ISU Code = " + coverageContainer.getIsuCd()).append("\n");
          details.append(" - Client Tier = " + coverageContainer.getClientTierCd()).append("\n");
          if (createOverrides) {
            output.addOverride(getProcessCode(), "DATA", "ISU_CD", requestData.getData().getIsuCd(), coverageContainer.getIsuCd());
            output.addOverride(getProcessCode(), "DATA", "CLIENT_TIER", requestData.getData().getClientTier(),
                StringUtils.isNotBlank(coverageContainer.getClientTierCd()) ? coverageContainer.getClientTierCd() : "");
          }
        }

        // Check if BG ID calculated
        if (gbg != null && createOverrides && output.getData() != null && !output.getData().isEmpty()) {
          FieldResult bgResult = output.getData().get(new FieldResultKey("DATA", "BG_ID"));
          if (bgResult != null && StringUtils.isNotBlank(data.getBgId()) && !"BGNONE".equals(data.getBgId())
              && !data.getBgId().equals(bgResult.getNewValue())) {
            // calculated buying group is different from coverage buying group.
            details.append("\nBuying Group ID under coverage overrides is different from the one on request.\n");
            engineData.addNegativeCheckStatus("BG_DIFFERENT", "Buying Group ID under coverage overrides is different from the one on request.");
          }
          FieldResult gbgResult = output.getData().get(new FieldResultKey("DATA", "GBG_ID"));
          if (gbgResult != null && StringUtils.isNotBlank(data.getGbgId()) && !"BGNONE".equals(data.getGbgId())
              && !data.getGbgId().equals(gbgResult.getNewValue())) {
            // calculated global buying group is different from coverage global
            // buying group.
            details.append("\nGlobal Buying Group ID under coverage overrides is different from the one on request.\n");
            engineData.addNegativeCheckStatus("GBG_DIFFERENT", "Buying Group ID under coverage overrides is different from the one on request.");
          }
        }

        // Add to list if rules found.
        coverageIds.add(currCovId);
      }
    }

  }

  /**
   * Checks whether a condition holds the field value
   * 
   * @param condition
   * @param fieldValue
   * @return
   */
  private boolean containsValue(Condition condition, String fieldValue) {
    if (fieldValue != null && condition.getValues() != null && !condition.getValues().isEmpty()) {
      for (String value : condition.getValues()) {
        if (value != null && value.trim().equals(fieldValue.trim())) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Gets column value from Address
   * 
   * @param addr
   * @param dbField
   * @return
   */
  private String getColumnValueFromAddr(Addr addr, String dbField) {
    try {
      for (Field field : Addr.class.getDeclaredFields()) {
        Column column = field.getAnnotation(Column.class);
        if (column != null && dbField.equals(column.name())) {
          field.setAccessible(true);
          Object objValue = field.get(addr);
          if (objValue != null) {
            return (String) objValue;
          } else {
            return null;
          }
        }
      }
    } catch (Exception e) {
      LOG.error("Unable to determine value from entity Data for field " + dbField, e);
    }
    return null;
  }

  /**
   * Gets column value from data
   * 
   * @param data
   * @param dbField
   * @return
   */
  private String getColumnValueFromData(Data data, String dbField) {
    try {
      for (Field field : Data.class.getDeclaredFields()) {
        Column column = field.getAnnotation(Column.class);
        if (column != null && dbField.equals(column.name())) {
          field.setAccessible(true);
          Object objValue = field.get(data);
          if (objValue != null) {
            return (String) objValue;
          } else {
            return null;
          }
        }
      }
    } catch (Exception e) {
      LOG.error("Unable to determine value from entity Data for field " + dbField, e);
    }
    return null;
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
   * of the specified <strong>sqlKey</strong> RDC query, gets only valid
   * coverages
   * 
   * 
   * @param entityManager
   * @param sqlKey
   * @param key
   * @param cmrIssuingCountry
   * @return
   */
  public List<CoverageContainer> computeCoverageFromRDCQuery(EntityManager entityManager, String sqlKey, String key, String cmrIssuingCountry) {
    return computeCoverageFromRDCQuery(entityManager, sqlKey, key, cmrIssuingCountry, true);
  }

  /**
   * * Using the <strong>key</strong> provided calculate the Coverage on the
   * basis of the specified <strong>sqlKey</strong> RDC query coverages
   * 
   * if valid only=false, returns all retrieved coverages
   * 
   * @param entityManager
   * @param sqlKey
   * @param key
   * @param cmrIssuingCountry
   * @param validOnly
   * @return
   */
  public List<CoverageContainer> computeCoverageFromRDCQuery(EntityManager entityManager, String sqlKey, String key, String cmrIssuingCountry,
      boolean validOnly) {
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

        if (validOnly && container.getFinalCoverageRules() != null) {
          coverages.add(container);
        } else {
          coverages.add(container);
        }
      }
    }

    return coverages;

  }

  /**
   * Returns a list with coverages which hold coverage rules
   * 
   * @param coverages
   * @return
   */
  public List<CoverageContainer> getValidCoverages(List<CoverageContainer> coverages) {
    List<CoverageContainer> validCoverages = new ArrayList<CoverageContainer>();
    if (coverages != null && !coverages.isEmpty()) {
      for (CoverageContainer container : coverages) {
        if (container.getFinalCoverageRules() != null) {
          validCoverages.add(container);
        }
      }
    }
    return validCoverages;
  }

  /**
   * Extract coverage input from requestData
   * 
   * @param entityManager
   * @param requestData
   * @param data
   * @param addr
   * @param gbgId
   * @param bgId
   * @return
   * @throws Exception
   */
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
    container.setFinalCoverage(coverageId);
    container.setFinalCoverageRules(rule);
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
