/**
 * 
 */
package com.ibm.cio.cmr.request.automation.impl.gbl;

import java.io.File;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.EntityManager;

import org.apache.commons.lang3.StringUtils;
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
import com.ibm.cio.cmr.request.util.SystemLocation;
import com.ibm.cio.cmr.request.util.SystemParameters;
import com.ibm.cio.cmr.request.util.SystemUtil;
import com.ibm.cio.cmr.request.util.geo.GEOHandler;
import com.ibm.cio.cmr.utils.coverage.CoverageRules;
import com.ibm.cio.cmr.utils.coverage.JarProperties;
import com.ibm.cio.cmr.utils.coverage.objects.CoverageInput;
import com.ibm.cio.cmr.utils.coverage.rules.Condition;
import com.ibm.cio.cmr.utils.coverage.rules.Coverage;
import com.ibm.cio.cmr.utils.coverage.rules.Rule;

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
  public static final String COV_BG = "COV_BG";
  public static final String BG_NONE = "BG_NONE";
  public static final String COV_REQ = "COV_REQ";
  public static final String COV_VAT = "COV_VAT";
  public static final String COV_ODM = "COV_ODM";
  public static final String NONE = "NONE";
  public static final String FINAL = "Final";
  public static final String BASE = "Base";
  private static final String QUERY_BG = "AUTO.COV.GET_COV_FROM_BG";
  private static final String QUERY_VAT = "AUTO.COV.GET_COV_FROM_VAT";
  public String addrToUse;

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
  public static synchronized void initCoverageRules() throws Exception {
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
      AutomationUtil countryUtil = AutomationUtil.getNewCountryUtil(data.getCmrIssuingCntry());
      if (countryUtil != null) {
        addrToUse = countryUtil.getAddressTypeForGbgCovCalcs(entityManager, requestData, engineData);
      } else {
        addrToUse = "ZS01";
      }
      long reqId = admin.getId().getReqId();
      Addr addr = requestData.getAddress(addrToUse);

      AutomationResult<OverrideOutput> result = buildResult(reqId);
      OverrideOutput output = new OverrideOutput(false);
      CoverageContainer calculatedCoverageContainer = new CoverageContainer();
      boolean coverageNotFound = false;
      boolean isCoverageCalculated = false;
      List<CoverageContainer> coverages = null;
      boolean withCmrData = false;
      StringBuilder details = new StringBuilder();

      if (SystemLocation.UNITED_STATES.equals(data.getCmrIssuingCntry()) && data.getBgId() != null && "BYMODEL".equals(data.getCustSubGrp())) {
        engineData.addPositiveCheckStatus(AutomationEngineData.SKIP_COVERAGE);
        LOG.debug("Skip Coverage for Create By Model.");
      }

      // added flow to skip gbg matching
      LOG.debug("Before -Skip Coverage for Create By Model...");
      if (engineData.hasPositiveCheckStatus(AutomationEngineData.SKIP_COVERAGE)) {
        // ensure a GBG is set
        LOG.debug("Skip Coverage for Create By Model contains Positive Check");
        String covId = (String) engineData.get(AutomationEngineData.COVERAGE_CALCULATED);
        if (covId != null) {
          details.append("Coverage already computed by external process: ");
          details.append("\n").append("Coverage ID: " + covId);
          result.setDetails(details.toString());
          result.setResults("Skipped");
          result.setProcessOutput(output);
        } else {
          result.setDetails("Coverage calculation skipped due to previous element execution results.");
          result.setResults("Skipped");
          result.setProcessOutput(output);
          LOG.debug("After Skip Coverage for Create By Model...");
        }
        return result;
      }

      // check if coverage rules are initialized or not
      if (this.noInit) {
        result.setProcessOutput(output);
        result.setResults("Cannot Start");
        result.setOnError(true);
        result.setDetails("Coverage calculation cannot be done because of a system configuration issue.");
        return result;
      }

      String bgId = data.getBgId();
      String gbgId = data.getGbgId();
      String country = data.getCmrIssuingCntry();
      String covFrom = "XXX";
      if (bgId != null && !"BGNONE".equals(bgId.trim())) {
        coverages = computeCoverageFromRDCQuery(entityManager, QUERY_BG, bgId, data.getCmrIssuingCntry(), false);
        if (coverages != null && !coverages.isEmpty()) {
          CoverageContainer preferredCoverage = coverages.get(0);
          if (StringUtils.isNotBlank(preferredCoverage.getFinalCoverage())) {
            setCoverageEsUKI(data, preferredCoverage.getFinalCoverage());
          }
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
          if (countryUtil != null) {
            LOG.debug("Performing Gbg based on coverage");
            countryUtil.performCoverageBasedOnGBG(this, entityManager, result, details, output, requestData, engineData, covFrom,
                calculatedCoverageContainer, isCoverageCalculated);
          }
          CoverageInput inputBG = extractCoverageInput(entityManager, requestData, data, addr, gbgId, bgId);
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
        covFrom = COV_BG;
      } else if (StringUtils.isNotEmpty(data.getVat()) && !SystemLocation.CANADA.equals(data.getCmrIssuingCntry())) {
        coverages = computeCoverageFromRDCQuery(entityManager, QUERY_VAT, data.getVat(), data.getCmrIssuingCntry());
        if (coverages == null || coverages.isEmpty()) {
          CoverageInput inputBG = extractCoverageInput(entityManager, requestData, data, addr, gbgId, bgId);
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
      } else if (bgId != null && "BGNONE".equals(bgId.trim())) {
        details.append("Projected Buying Group on the request is 'BGNONE'. Skipping Coverage Calculation from Buying Group.\n");
        covFrom = BG_NONE;
      }
      // get default coverage
      String defaultCoverage = getDefaultCoverage(data.getCmrIssuingCntry());
      CoverageInput input = null;
      // coverage id calculation from BG/VAT
      input = extractCoverageInput(entityManager, requestData, data, addr, gbgId, bgId);
      if (coverages != null && !coverages.isEmpty()) {
        switch (covFrom) {
        case COV_BG:
          details.append("\nCoverages from Buying Group ID " + bgId + " (" + gbgId + ")" + (withCmrData ? "[from current CMRs]" : "")).append("\n");
          break;
        case COV_VAT:
          details.append("\nCoverages from CMR VAT data for VAT Number " + data.getVat() + (withCmrData ? " [from current CMRs]" : "")).append("\n");
          break;
        case COV_ODM:
          details.append("\nCoverage from ODM Projected/User Specified Coverage ID " + data.getCovId()).append("\n");
          break;
        }

        List<String> coverageIds = new ArrayList<String>();
        boolean logNegativeCheck = !COV_ODM.equals(covFrom);
        for (CoverageContainer container : coverages) {
          if (coverages.size() > 3 && coverages.indexOf(container) > 3) {
            break;
          }
          LOG.debug("Logging Final Coverage ID: " + container.getFinalCoverage());
          logCoverage(entityManager, engineData, requestData, coverageIds, details, output, input, container, FINAL, covFrom, logNegativeCheck);
          logNegativeCheck = false;
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
                details.append("\nCalculated Coverage is same as the coverage calculated using Request Data.").append("\n");
              } else if (finalCoverage.equals(defaultCoverage)) {
                result.setResults("Default Coverage");
                details.append("\nCalculated Coverage is same as the Default Coverage.").append("\n");
              }
            }
            calculatedCoverageContainer = container;
          }
          if (RequestUtils.EMEA_CNTRY_DACH.contains(data.getCmrIssuingCntry())) {
            calculatedCoverageContainer = coverages.get(0);
          }
        }
      } else if (BG_NONE.equals(covFrom)) {
        LOG.debug("No Calculated BG Found. Projected BG='BGNONE'. Skipping Regular Coverage Calculation.");
        result.setResults("Skipped");
      } else {
        result.setResults("Cannot Calculate");
        covFrom = NONE;
      }

      // hook to perform country specific operations

      // if it is the first calculated coverage, use it to do country
      // specific coverage calculations
      boolean hasCountryCheck = false;
      if (countryUtil != null) {
        // hook to perform calculations and update results
        hasCountryCheck = countryUtil.performCountrySpecificCoverageCalculations(this, entityManager, result, details, output, requestData,
            engineData, covFrom, calculatedCoverageContainer, isCoverageCalculated);
      }

      if (!hasCountryCheck) {
        if ("NONE".equals(covFrom)) {
          if (coverageNotFound) {
            details.append("Coverage ID " + data.getCovId() + " not found in the coverage rules.");
          } else {
            result.setResults("Skipped");
            details.append("No projected Buying Group or ODM Coverage found, coverage adjustments will be skipped.");
          }
        } else if (!isCoverageCalculated && !BG_NONE.equals(covFrom)) {
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

  private void setCoverageEsUKI(Data data, String finalCoverage) {
    if (SystemLocation.SPAIN.equals(data.getCmrIssuingCntry()) || SystemLocation.UNITED_KINGDOM.equals(data.getCmrIssuingCntry())
        || SystemLocation.IRELAND.equals(data.getCmrIssuingCntry())) {
      data.setCovId(finalCoverage);
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
   * @param input
   * @param coverageContainer
   * @param currCovLevel
   * @param covFrom
   * @param logNegativeCheck
   */
  public void logCoverage(EntityManager entityManager, AutomationEngineData engineData, RequestData requestData, List<String> coverageIds,
      StringBuilder details, OverrideOutput output, CoverageInput input, CoverageContainer coverageContainer, String currCovLevel, String covFrom,
      boolean logNegativeCheck) {
    coverageIds = coverageIds != null ? coverageIds : new ArrayList<String>();
    covFrom = covFrom != null ? covFrom : "";
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

    if (!coverageIds.contains(currCovId) && currCovRules != null && !currCovRules.isEmpty()) {
      // create overrides for first logged/manually logged coverage only
      Collections.sort(currCovRules);
      Rule rule = currCovRules.get(0);
      boolean createOverrides = coverageIds.isEmpty() && !COV_ODM.equals(covFrom);
      List<Rule> matchedRules = coverageRules.matchWithCoverage(input, currCovId);
      if (matchedRules != null && !matchedRules.isEmpty()) {
        Collections.sort(matchedRules);
        rule = matchedRules.get(0);
      }
      details.append("\nCoverage ID = " + currCovId).append(" (" + currCovLevel + ")").append("\n");
      details.append("Coverage Name = " + getCoverageDescription(entityManager, currCovId)).append("\n");
      details.append("Overrides for " + currCovId + ":").append("\n");
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
              fieldValue = getColumnValueFromAddr(requestData.getAddress(addrToUse), dbField);
            } else {
              fieldValue = getColumnValueFromData(requestData.getData(), dbField);
            }
            if (val != null && !val.startsWith("!")) {
              val = val.trim();
              String operation = condition.getOperation().toString();
              switch (operation) {
              case "StartsOrEndsWith":
              case "Equals":
                details.append(" - " + (addr ? "[Addr] " : "") + field + " = " + val + "\n");
                if (createOverrides && (StringUtils.isBlank(fieldValue) || !fieldValue.trim().equals(val))) {
                  output.addOverride(getProcessCode(), addr ? addrToUse : "DATA", dbField, "", val);
                }
                break;
              case "StartsWith":
                if (StringUtils.isNotBlank(fieldValue) && fieldValue.startsWith(val)) {
                  details.append(" - " + (addr ? "[Addr] " : "") + field + " = " + val + "\n");
                } else {
                  notDeterminedFields.put(field, val);
                }
                break;
              case "In":
                if (containsValue(condition, fieldValue)) {
                  // no override using first value, the list already
                  // contains current value
                  details.append(" - " + (addr ? "[Addr] " : "") + field + " = " + fieldValue + "\n");
                  break;
                } else {
                  // TODO review this logic
                  if (condition.getValues() != null) {
                    details.append(" - " + (addr ? "[Addr] " : "") + field + " = " + condition.getValues() + " [" + fieldValue + "]\n");
                  } else {
                    details.append(" - " + (addr ? "[Addr] " : "") + field + " = " + val + "\n");
                  }

                  if (createOverrides) {
                    output.addOverride(getProcessCode(), addr ? addrToUse : "DATA", dbField, "", val);
                  }
                }
                break;
              case "LengthGreaterThan":
                break;
                
              case "NotIn":
                break;

              default:
                notDeterminedFields.put(field, val);
                break;
              }
            } else if (val != null && val.startsWith("!")) {
              String value = val.substring(1);
              if (StringUtils.isNotBlank(fieldValue) && !fieldValue.startsWith(value)) {
                details.append(" - " + (addr ? "[Addr] " : "") + field + " = " + val + "\n");
              } else {
                notDeterminedFields.put(field, val);
              }
            } else {
              notDeterminedFields.put(field, val);
            }
          }
        }
      }
      if (!notDeterminedFields.isEmpty() && !(COV_BG.equals(covFrom) && SystemLocation.UNITED_STATES.equals(data.getCmrIssuingCntry()))) {
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
        // CREATCMR-7173
        boolean isKynScenarioSubType = false;
        if ("C".equals(requestData.getAdmin().getReqType()) && SystemLocation.UNITED_STATES.equals(data.getCmrIssuingCntry())
            && ("KYN".equals(data.getCustSubGrp())
                || "BYMODEL".equalsIgnoreCase(data.getCustSubGrp()) && "KYN".equalsIgnoreCase(data.getRestrictTo()))) {
          isKynScenarioSubType = true;
        }
        if (!isKynScenarioSubType) {
          details.append("\nOverrides based on CMR data:").append("\n");
          details.append(" - ISU Code = " + coverageContainer.getIsuCd()).append("\n");
          details.append(" - Client Tier = " + coverageContainer.getClientTierCd()).append("\n");
          if (createOverrides) {
            output.addOverride(getProcessCode(), "DATA", "ISU_CD", requestData.getData().getIsuCd(), coverageContainer.getIsuCd());
            output.addOverride(getProcessCode(), "DATA", "CLIENT_TIER", requestData.getData().getClientTier(),
                StringUtils.isNotBlank(coverageContainer.getClientTierCd()) ? coverageContainer.getClientTierCd() : "");
          }
        }
      }

      // Check if BG ID calculated
      if (createOverrides && output.getData() != null && !output.getData().isEmpty()) {
        FieldResultKey bgKey = new FieldResultKey("DATA", "BG_ID");
        FieldResult bgResult = output.getData().get(bgKey);
        if (bgResult != null && StringUtils.isNotBlank(data.getBgId()) && !"BGNONE".equals(data.getBgId().trim())
            && !data.getBgId().equals(bgResult.getNewValue())) {
          // calculated buying group is different from coverage buying group.
          details.append("Buying Group ID under coverage overrides is different from the one on request.\n");
          // engineData.addNegativeCheckStatus("BG_DIFFERENT", "Buying Group ID
          // under coverage overrides is different from the one on request.");
          output.getData().remove(bgKey);
        }
        FieldResultKey gbgKey = new FieldResultKey("DATA", "GBG_ID");
        FieldResult gbgResult = output.getData().get(gbgKey);
        if (gbgResult != null && StringUtils.isNotBlank(data.getGbgId()) && !"BGNONE".equals(data.getGbgId().trim())
            && !data.getGbgId().equals(gbgResult.getNewValue())) {
          // calculated global buying group is different from coverage global
          // buying group.
          details.append("Global Buying Group ID under coverage overrides is different from the one on request.\n");
          // engineData.addNegativeCheckStatus("GBG_DIFFERENT", "Buying Group ID
          // under coverage overrides is different from the one on request.");
          output.getData().remove(gbgKey);
        }
      }

      if (COV_ODM.equals(covFrom)) {
        // no overrides if COV_ODM
        details.append("Current request data already fall into the projected ODM coverage.\n");
      }

      // Add to list if rules found.
      coverageIds.add(currCovId);
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
