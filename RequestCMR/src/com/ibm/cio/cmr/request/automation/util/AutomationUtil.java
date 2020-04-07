package com.ibm.cio.cmr.request.automation.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.automation.AutomationEngineData;
import com.ibm.cio.cmr.request.automation.RequestData;
import com.ibm.cio.cmr.request.automation.impl.gbl.CalculateCoverageElement;
import com.ibm.cio.cmr.request.automation.out.AutomationResult;
import com.ibm.cio.cmr.request.automation.out.OverrideOutput;
import com.ibm.cio.cmr.request.automation.out.ValidationOutput;
import com.ibm.cio.cmr.request.automation.util.geo.AustraliaUtil;
import com.ibm.cio.cmr.request.automation.util.geo.BrazilUtil;
import com.ibm.cio.cmr.request.automation.util.geo.FranceUtil;
import com.ibm.cio.cmr.request.automation.util.geo.GermanyUtil;
import com.ibm.cio.cmr.request.automation.util.geo.SingaporeUtil;
import com.ibm.cio.cmr.request.automation.util.geo.USUtil;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.util.SystemLocation;
import com.ibm.cmr.services.client.matching.gbg.GBGFinderRequest;

/**
 *
 * Interface to handle country specific utility methods
 *
 * @author RoopakChugh
 *
 */

public abstract class AutomationUtil {

  private static final Logger LOG = Logger.getLogger(AutomationUtil.class);

  protected static final Map<String, Class<? extends AutomationUtil>> GEO_UTILS = new HashMap<String, Class<? extends AutomationUtil>>() {
    private static final long serialVersionUID = 1L;
    {
      put(SystemLocation.BRAZIL, BrazilUtil.class);
      put(SystemLocation.SINGAPORE, SingaporeUtil.class);
      put(SystemLocation.AUSTRALIA, AustraliaUtil.class);
      put(SystemLocation.GERMANY, GermanyUtil.class);
      put(SystemLocation.UNITED_STATES, USUtil.class);

      // FRANCE Sub Regions
      put(SystemLocation.FRANCE, FranceUtil.class);
      put(SystemLocation.ANDORRA, FranceUtil.class);
      put(SystemLocation.VANUATU, FranceUtil.class);
      put(SystemLocation.FRENCH_GUIANA, FranceUtil.class);
      put(SystemLocation.FRENCH_POLYNESIA_TAHITI, FranceUtil.class);
      put(SystemLocation.GUATEMALA, FranceUtil.class);
      put(SystemLocation.MAYOTTE, FranceUtil.class);
      put(SystemLocation.NEW_CALEDONIA, FranceUtil.class);
      put(SystemLocation.WALLIS_FUTUNA, FranceUtil.class);
      put(SystemLocation.COMOROS, FranceUtil.class);
      put(SystemLocation.SAINT_PIERRE_MIQUELON, FranceUtil.class);

    }
  };

  private static final List<String> VAT_CHECK_COUNTRIES = Arrays.asList(SystemLocation.BRAZIL);

  /**
   * returns an instance of
   *
   * @param cmrIssuingCntry
   * @return
   * @throws InstantiationException
   * @throws IllegalAccessException
   */
  public static AutomationUtil getNewCountryUtil(String cmrIssuingCntry) throws IllegalAccessException, InstantiationException {
    if (!GEO_UTILS.containsKey(cmrIssuingCntry)) {
      return null;
    }
    try {
      Class<? extends AutomationUtil> utilClass = GEO_UTILS.get(cmrIssuingCntry);
      return utilClass.newInstance();
    } catch (Throwable t) {
      LOG.warn("Automation Util for " + cmrIssuingCntry + " found but cannot be initialized.", t);
      return null;
    }
  }

  /**
   *
   * Computes IBM field values specific to countries and scenarios.
   *
   * @param results
   * @param details
   * @param overrides
   * @param requestData
   * @param engineData
   * @return
   * @throws Exception
   */
  public abstract AutomationResult<OverrideOutput> doCountryFieldComputations(EntityManager entityManager, AutomationResult<OverrideOutput> results,
      StringBuilder details, OverrideOutput overrides, RequestData requestData, AutomationEngineData engineData) throws Exception;

  /**
   * validates if the scenario on request is correct or not
   *
   * @param entityManager
   * @param requestData
   * @param engineData
   * @param result
   * @param details
   * @param putput
   * @param rejectionComment
   * @return
   */
  public abstract boolean performScenarioValidation(EntityManager entityManager, RequestData requestData, AutomationEngineData engineData,
      AutomationResult<ValidationOutput> result, StringBuilder details, ValidationOutput output);

  /**
   * This method should be overridden by implementing classes and
   * <strong>always</strong> return true if there are country specific logic
   * 
   * @param covElement
   * @param entityManager
   * @param results
   * @param details
   * @param overrides
   * @param requestData
   * @param engineData
   * @param covFrom
   * @param container
   * @param isCoverageCalculated
   * @return
   * @throws Exception
   */
  public boolean performCountrySpecificCoverageCalculations(CalculateCoverageElement covElement, EntityManager entityManager,
      AutomationResult<OverrideOutput> results, StringBuilder details, OverrideOutput overrides, RequestData requestData,
      AutomationEngineData engineData, String covFrom, CoverageContainer container, boolean isCoverageCalculated) throws Exception {
    return false;
  }

  /**
   *
   * Gets the default cluster code for country.
   *
   * @param results
   * @param details
   * @param overrides
   * @param requestData
   * @param engineData
   * @return
   */
  public String getDefaultCluster(EntityManager entityManager, RequestData requestData, AutomationEngineData engineData) {
    String defaultClusterCd = "";
    Data data = requestData.getData();
    String sql = ExternalizedQuery.getSql("AUTO.GET.DEFAULT_CLUSTER");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("ISSUING_CNTRY", data.getCmrIssuingCntry());
    List<String> defaultClusters = query.getResults(String.class);
    if (defaultClusters != null && defaultClusters.size() > 0) {
      defaultClusterCd = defaultClusters.get(0);
    }
    return defaultClusterCd;
  }

  /**
   *
   * Validates the Cluster and SalesMan Combination.
   *
   * @return a) validCode = "NO_RESULTS" if no rows found on CREQCMR.REP_TEAM b)
   *         validCode = "INVALID" if cluster exist but with different SalesMan
   *         on CREQCMR.REP_TEAM c) validCode = "VALID" if cluster exist with
   *         request's SalesMan on CREQCMR.REP_TEAM
   */
  public String checkIfClusterSalesmanIsValid(EntityManager entityManager, RequestData requestData) {
    String validCode = null;
    Data data = requestData.getData();
    String sql = ExternalizedQuery.getSql("AUTO.VALIDATE_CLUSTER_SALESMAN");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("ISSUING_CNTRY", data.getCmrIssuingCntry());
    query.setParameter("REP_TEAM_CD", data.getApCustClusterId());
    query.setParameter("REP_TEAM_MEMBER_NO", data.getRepTeamMemberNo());
    List<String> results = query.getResults(String.class);
    if (results != null && results.size() > 0) {
      for (String res : results) {
        if (res.equals(data.getRepTeamMemberNo())) {
          validCode = "VALID";
        }
      }
      validCode = ((validCode == null) ? "INVALID" : validCode);
    } else {
      validCode = "NO_RESULTS";
    }
    return validCode;
  }

  /**
   * Returns true if issuing country is configured to perform VAT match
   *
   * @param cmrIssuingCntry
   * @return
   */
  public static boolean isCheckVatForDuplicates(String cmrIssuingCntry) {
    if (StringUtils.isNotBlank(cmrIssuingCntry)) {
      return VAT_CHECK_COUNTRIES.contains(cmrIssuingCntry);
    }
    return false;
  }

  /**
   * Checks if ISIC and Subindustry is valid for specified scenario
   *
   * @param scenarioList
   * @return inValid
   */

  public static boolean isISICValidForScenario(RequestData requestData, List<String> scenarioList) {
    boolean isInvalid = false;
    // get request admin and data
    Admin admin = requestData.getAdmin();
    Data data = requestData.getData();
    String scenarioSubType = "";
    String isicCode = "";
    String subindustry = "";
    boolean isIsicValid = true;
    boolean isSubIndValid = true;
    if ("C".equals(admin.getReqType()) && data != null) {
      scenarioSubType = data.getCustSubGrp();
    }

    if (data != null) {
      isicCode = data.getIsicCd();
      subindustry = data.getSubIndustryCd();
    }

    if (scenarioSubType != null && StringUtils.isNotBlank(scenarioSubType) && scenarioList != null && scenarioList.contains(scenarioSubType)) {
      // check if ISIC does not contains letters
      if (isicCode != null && StringUtils.isNotBlank(isicCode) && isicCode.matches("[0-9]*[a-zA-Z]")) {
        isIsicValid = false;
      }
      // Check that Subindustry is not under Z* (any starting with Z).
      if (subindustry != null && StringUtils.isNotBlank(subindustry) && subindustry.startsWith("Z")) {
        isSubIndValid = false;
      }

      if (!isIsicValid || !isSubIndValid) {
        isInvalid = true;
      }
    }

    return isInvalid;
  }

  /**
   * This method should be overridden by implementing classes and
   * <strong>always</strong> return true if there are country specific logic
   * 
   * @param entityManager
   * @param engineData
   * @param requestData
   * @param changes
   * @param output
   * @param validation
   * @return
   * @throws Exception
   */
  public boolean runUpdateChecksForData(EntityManager entityManager, AutomationEngineData engineData, RequestData requestData,
      RequestChangeContainer changes, AutomationResult<ValidationOutput> output, ValidationOutput validation) throws Exception {
    return false; // false denotes no country specific runs
  }

  /**
   * This method should be overridden by implementing classes and
   * <strong>always</strong> return true if there are country specific logic
   * 
   * @param entityManager
   * @param engineData
   * @param requestData
   * @param changes
   * @param output
   * @param validation
   * @return
   * @throws Exception
   */
  public boolean runUpdateChecksForAddress(EntityManager entityManager, AutomationEngineData engineData, RequestData requestData,
      RequestChangeContainer changes, AutomationResult<ValidationOutput> output, ValidationOutput validation) throws Exception {
    return false; // false denotes no country specific runs
  }

  /**
   * Allows duplicate cmrs for scenario
   *
   * @param scenarioList
   * @return
   */
  public void allowDuplicatesForScenario(AutomationEngineData engineData, RequestData requestData, List<String> scenarioList) {
    Admin admin = requestData.getAdmin();
    Data data = requestData.getData();

    String scenarioSubType = "";
    if ("C".equals(admin.getReqType()) && data != null) {
      scenarioSubType = data.getCustSubGrp();
    }
    if (!StringUtils.isBlank(scenarioSubType) && !scenarioList.isEmpty() && scenarioList.contains(scenarioSubType)) {
      engineData.addPositiveCheckStatus("allowDuplicates");
    }
    LOG.debug("Allowing Duplicate CMR for reqid = " + admin.getId().getReqId());
  }

  /**
   * Hooks to be able to manipulate the data to be sent to GBG finder services
   * 
   * @param entityManager
   * @param request
   * @param requestData
   */
  public void tweakGBGFinderRequest(EntityManager entityManager, GBGFinderRequest request, RequestData requestData) {
    // NOOP
  }

}