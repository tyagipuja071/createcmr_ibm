/**
 * 
 */
package com.ibm.cio.cmr.request.automation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.ibm.cio.cmr.request.automation.util.RejectionContainer;

/**
 * The container for {@link AutomationEngine} data. When an
 * {@link AutomationEngine} executes, each element can contribute to the
 * engine's current execution data.
 * 
 * @author JeffZAMORA
 * 
 */
public class AutomationEngineData extends HashMap<String, Object> {

  public static final String APP_USER = "appUser";
  public static final String REJECTIONS = "rejections";
  public static final String NEGATIVE_CHECKS = "negative_checks";
  public static final String POSITIVE_CHECKS = "positive_checks";
  public static final String GBG_MATCH = "gbgMatch";
  public static final String COVERAGE_CALCULATED = "coverageCalculated";
  public static final String SOS_MATCH = "sosMatching";
  public static final String DNB_MATCH = "dnbMatching";
  public static final String DNB_ALL_MATCHES = "dnbAllMatches";
  public static final String VAT_VERIFIED = "vatVerified";
  public static final String SKIP_VAT_CHECKS = "skipVatChecks";
  public static final String SKIP_CHECKS = "skipChecks";
  public static final String COMPANY_INFO_SOURCE = "compInfoSrc";
  public static final String SCENARIO_VERIFIED_INDC = "scenarioVerifiedIndc";
  public static final String MATCH_DEPARTMENT = "matchDepartment";
  public static final String SKIP_APPROVALS = "defaultApproval";
  public static final String SKIP_DPL_CHECK = "skipDplChecks";
  public static final String SKIP_UPDATE_SWITCH = "skipUpdateSwitch";
  public static final String BO_COMPUTATION = "_usBOComputation";

  public static final String SKIP_GBG = "_gblSkipGbg";
  public static final String SKIP_COVERAGE = "_gblSkipCoverage";
  public static final String SKIP_FIELD_COMPUTATION = "_skipFieldComputatiom";
  public static final String REQ_MATCH_SCENARIO = "REQ_MATCH_SCENARIO";
  public static final String SKIP_DNB_ORGID_VAL = "_skipDnBOrgIdVald";
  public static final String SKIP_RETRIEVE_VALUES = "_skipRetrieveValues";

  private int trackedNegativeCheckCount;
  private boolean trackNegativeChecks;
  private boolean flagNZBNAPI;
  /**
   * 
   */
  private static final long serialVersionUID = 3524182254051963720L;

  /**
   * Adds a rejection comment to the data
   * 
   * @param comment
   */
  @SuppressWarnings("unchecked")
  public void addRejectionComment(String code, String comment, String supplInf1, String supplInf2) {
    List<RejectionContainer> rejComments = (List<RejectionContainer>) get(REJECTIONS);
    if (rejComments == null) {
      rejComments = new ArrayList<RejectionContainer>();
      put(REJECTIONS, rejComments);
    }
    rejComments = (List<RejectionContainer>) get(REJECTIONS);
    RejectionContainer rejCon = new RejectionContainer();
    rejCon.setRejCode(code);
    rejCon.setRejComment(comment);
    rejCon.setSupplInfo1(supplInf1);
    rejCon.setSupplInfo2(supplInf2);
    rejComments.add(rejCon);
  }

  /**
   * Adds a new negative check status to the engine data wit the corresponding
   * user-friendly check message to be displayed to the users
   * 
   * @param checkKey
   * @param userFriendlyCheckMessage
   */
  @SuppressWarnings("unchecked")
  public void addNegativeCheckStatus(String checkKey, String userFriendlyCheckMessage) {
    Map<String, String> checks = (Map<String, String>) get(NEGATIVE_CHECKS);
    if (checks == null) {
      checks = new HashMap<String, String>();
      put(NEGATIVE_CHECKS, checks);
    }
    checks = (Map<String, String>) get(NEGATIVE_CHECKS);
    checks.put(checkKey, userFriendlyCheckMessage);
    if (this.trackNegativeChecks) {
      this.trackedNegativeCheckCount++;
    }
  }

  /**
   * Checks negative check status
   * 
   * @param userFriendlyCheckMessage
   */
  @SuppressWarnings("unchecked")
  public boolean hasNegativeCheckStatus() {
    Map<String, String> checks = (Map<String, String>) get(NEGATIVE_CHECKS);
    if (checks != null) {
      return true;
    } else {
      return false;
    }
  }

  /**
   * Checks the current negative check status for the given key. If the status
   * is still set, the user-friendly message associated with the status is
   * returned. Null is returned if the status is not set
   * 
   * @param checkKey
   * @return
   */
  @SuppressWarnings("unchecked")
  public String getNegativeCheckStatus(String checkKey) {
    Map<String, String> checks = (Map<String, String>) get(NEGATIVE_CHECKS);
    if (checks == null) {
      return null;
    }
    checks = (Map<String, String>) get(NEGATIVE_CHECKS);
    return checks.get(checkKey);
  }

  /**
   * Returns the map of negative check statuses
   * 
   * @return
   */
  public HashMap<String, String> getNegativeChecks() {
    @SuppressWarnings("unchecked")
    HashMap<String, String> checks = (HashMap<String, String>) get(NEGATIVE_CHECKS);
    if (checks == null) {
      return new HashMap<String, String>();
    } else {
      return checks;
    }
  }

  /**
   * Clears the given negative check status key from the current data
   * 
   * @param checkKey
   */
  public void clearNegativeCheckStatus(String checkKey) {
    Map<String, String> checks = getPendingChecks();
    if (checks == null) {
      return;
    }
    checks.remove(checkKey);
  }

  /**
   * Returns the map of positive check statuses
   * 
   * @return
   */
  public List<String> getPositiveChecks() {
    @SuppressWarnings("unchecked")
    List<String> checks = (List<String>) get(POSITIVE_CHECKS);
    if (checks == null) {
      return new ArrayList<String>();
    } else {
      return checks;
    }
  }

  /**
   * Clears the positive check status for a particular key
   * 
   * @param key
   */
  public void clearPositiveCheck(String key) {
    List<String> checks = getPositiveChecks();
    if (checks == null) {
      return;
    }
    checks.remove(key);
  }

  /**
   * Adds a new positive check status to the engine
   * 
   * @param checkKey
   * @param checkMessage
   */
  @SuppressWarnings("unchecked")
  public void addPositiveCheckStatus(String checkKey) {
    List<String> checks = (List<String>) get(POSITIVE_CHECKS);
    if (checks == null) {
      checks = new ArrayList<>();
      put(POSITIVE_CHECKS, checks);
    }
    checks = (List<String>) get(POSITIVE_CHECKS);
    if (!checks.contains(checkKey)) {
      checks.add(checkKey);
    }
  }

  /**
   * Checks all positive statuses whether the current key has been set
   * 
   * @param checkKey
   * @return
   */
  @SuppressWarnings("unchecked")
  public boolean hasPositiveCheckStatus(String checkKey) {
    List<String> checks = (List<String>) get(POSITIVE_CHECKS);
    if (checks == null) {
      return false;
    }
    checks = (List<String>) get(POSITIVE_CHECKS);
    return checks.contains(checkKey);
  }

  /**
   * Sets a vat verified status on the engine data
   * 
   * @param vatVerified
   */
  public void setSkipChecks() {
    if (!hasPositiveCheckStatus(SKIP_CHECKS)) {
      addPositiveCheckStatus(SKIP_CHECKS);
    }
  }

  /**
   * Returns a boolean indicating vat verified status for a request
   * 
   * @return
   */
  public boolean isSkipChecks() {
    return hasPositiveCheckStatus(SKIP_CHECKS);
  }

  public void setCompanySource(String source) {
    if (get(COMPANY_INFO_SOURCE) == null) {
      // only track the first element that verified the source
      put(COMPANY_INFO_SOURCE, source);
    }
  }

  public void setScenarioVerifiedIndc(String indc) {
    if (get(SCENARIO_VERIFIED_INDC) == null) {
      // only track the first element that verified the source
      put(SCENARIO_VERIFIED_INDC, indc);
    }
  }

  public void setMatchDepartment(boolean indc) {
    if (get(MATCH_DEPARTMENT) == null) {
      // only track the first element that verified the source
      put(MATCH_DEPARTMENT, indc);
    }
  }

  @SuppressWarnings("unchecked")
  public List<RejectionContainer> getRejectionReasons() {
    List<RejectionContainer> container = (List<RejectionContainer>) get(REJECTIONS);
    if (container != null) {
      return container;
    } else {
      container = new ArrayList<RejectionContainer>();
      put(REJECTIONS, container);
      return container;
    }
  }

  public HashMap<String, String> getPendingChecks() {
    return getNegativeChecks();
  }

  public boolean isTrackNegativeChecks() {
    return trackNegativeChecks;
  }

  public void setTrackNegativeChecks(boolean trackNegativeChecks) {
    this.trackNegativeChecks = trackNegativeChecks;
  }

  public int getTrackedNegativeCheckCount() {
    return trackedNegativeCheckCount;
  }

  /**
   * Returns true if VAT has been verified for a request
   * 
   * @return
   */
  public boolean isVatVerified() {
    return hasPositiveCheckStatus(VAT_VERIFIED);
  }

  /**
   * Returns true if Dnb has been verified for a request
   * 
   * @return
   */
  public boolean isDnbVerified() {
    return hasPositiveCheckStatus(DNB_MATCH);
  }

  /**
   * Returns true if SOS-RPA has been verified for a request
   * 
   * @return
   */
  public boolean isSosVerified() {
    return hasPositiveCheckStatus(SOS_MATCH);
  }

  /**
   * Set VAT verified for the request. If false sets the message as a pending
   * check
   * 
   * @param vatVerified
   * @param message
   */
  public void setVatVerified(boolean vatVerified, String message) {
    if (vatVerified) {
      addPositiveCheckStatus(VAT_VERIFIED);
      clearNegativeCheckStatus(VAT_VERIFIED);
    } else {
      if (hasPositiveCheckStatus(VAT_VERIFIED)) {
        clearPositiveCheck(VAT_VERIFIED);
      }
      addNegativeCheckStatus(VAT_VERIFIED, StringUtils.isNotBlank(message) ? message : "");
    }
  }

  public boolean isNZBNAPICheck() {
    return flagNZBNAPI;
  }

  public void setNZBNAPICheck(boolean flagNZBNAPI) {
    this.flagNZBNAPI = flagNZBNAPI;
  }
}
