/**
 * 
 */
package com.ibm.cio.cmr.request.automation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
  public static final String DNB_MATCH = "dnbMatching";
  public static final String VAT_VERIFIED = "vatVerified";
  public static final String SKIP_CHECKS = "skipChecks";
  public static final String COMPANY_INFO_SOURCE = "compInfoSrc";
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
  public void addRejectionComment(String comment) {
    List<String> rejComments = (List<String>) get(REJECTIONS);
    if (rejComments == null) {
      rejComments = new ArrayList<String>();
      put(REJECTIONS, rejComments);
    }
    rejComments = (List<String>) get(REJECTIONS);
    rejComments.add(comment);
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
   * Clears the given negative check status key from the current data
   * 
   * @param checkKey
   */
  @SuppressWarnings("unchecked")
  public void clearNegativeCheckStatus(String checkKey) {
    Map<String, String> checks = (Map<String, String>) get(NEGATIVE_CHECKS);
    if (checks == null) {
      return;
    }
    checks = (Map<String, String>) get(NEGATIVE_CHECKS);
    checks.remove(checkKey);
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
    checks.add(checkKey);
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
    if (get(COMPANY_INFO_SOURCE) != null) {
      // only track the first element that verified the source
      put(COMPANY_INFO_SOURCE, source);
    }
  }
}
