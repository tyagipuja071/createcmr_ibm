package com.ibm.cio.cmr.request.automation.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import com.ibm.cio.cmr.request.automation.AutomationEngineData;
import com.ibm.cio.cmr.request.automation.RequestData;
import com.ibm.cio.cmr.request.automation.out.AutomationResult;
import com.ibm.cio.cmr.request.automation.out.OverrideOutput;
import com.ibm.cio.cmr.request.automation.util.geo.AustraliaUtil;
import com.ibm.cio.cmr.request.automation.util.geo.BrazilUtil;
import com.ibm.cio.cmr.request.automation.util.geo.SingaporeUtil;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.util.SystemLocation;

/**
 * 
 * Interface to handle country specific utility methods
 * 
 * @author GarimaNarang, Roopak Chugh
 * 
 */

public abstract class AutomationUtil {

  protected static final Map<String, Class<? extends AutomationUtil>> GEO_UTILS = new HashMap<String, Class<? extends AutomationUtil>>() {
    private static final long serialVersionUID = 1L;
    {
      put(SystemLocation.BRAZIL, BrazilUtil.class);
      put(SystemLocation.SINGAPORE, SingaporeUtil.class);
      put(SystemLocation.AUSTRALIA, AustraliaUtil.class);
    }
  };

  /**
   * returns an instance of
   * 
   * @param cmrIssuingCntry
   * @return
   * @throws InstantiationException
   * @throws IllegalAccessException
   */
  public static Class<? extends AutomationUtil> getCountrySpecificUtil(String cmrIssuingCntry) throws IllegalAccessException, InstantiationException {
    if (GEO_UTILS.containsKey(cmrIssuingCntry)) {
      return GEO_UTILS.get(cmrIssuingCntry);
    } else {
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
   * @return
   * 
   */
  public abstract boolean performScenarioValidation(EntityManager entityManager, RequestData requestData, AutomationEngineData engineData);

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
}