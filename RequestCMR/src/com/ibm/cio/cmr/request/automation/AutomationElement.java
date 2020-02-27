/**
 * 
 */
package com.ibm.cio.cmr.request.automation;

import javax.persistence.EntityManager;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.automation.out.AutomationOutput;
import com.ibm.cio.cmr.request.automation.out.AutomationResult;
import com.ibm.cio.cmr.request.automation.util.ScenarioExceptionsUtil;
import com.ibm.cio.cmr.request.entity.BaseEntity;
import com.ibm.cio.cmr.request.entity.Data;

/**
 * An object that can be configured and ran via the {@link AutomationEngine}
 * framework. Each implementation is expected not to depend on other
 * {@link AutomationElement} objects running, but instead purely base the
 * execution on the supplied <br>
 * <br>
 * To preserve the independence of automation elements among other automation
 * elements, implementing classes <strong>should not carry fields that maintain
 * state</strong> so that they can be reused as many times without affecting the
 * outcome of the process. If the class needs to have state-dependent fields,
 * then each field's value should be cleared on every call to
 * {@link #executeElement(EntityManager, RequestData)}
 * 
 * @author JeffZAMORA
 * 
 */
public abstract class AutomationElement<R extends AutomationOutput> {

  private String requestTypes;
  private ActionOnError actionOnError = ActionOnError.Proceed;
  private boolean overrideData = false;
  private boolean stopOnError = false;

  /**
   * Creates a new instance of the {@link AutomationElement} with the given
   * external configuration settings
   * 
   * @param requestTypes
   * @param actionOnError
   * @param overrideData
   * @param stopOnError
   */
  public AutomationElement(String requestTypes, String actionOnError, boolean overrideData, boolean stopOnError) {
    this.requestTypes = requestTypes;
    this.actionOnError = ActionOnError.fromCode(actionOnError);
    this.overrideData = overrideData;
    this.stopOnError = stopOnError;
  }

  /**
   * Executes the {@link AutomationElement} and returns an
   * {@link AutomationResult} containing the results of the process<br>
   * <br>
   * Each {@link AutomationElement} can put or get data on the current
   * {@link AutomationEngineData} passed on, to be able to share data.
   * <strong>The processing of each element though is expected to work even
   * without the needed data to be fully independent.</strong> Having the needed
   * data will only avoid duplicate executions or database queries or service
   * calls, whenever applicable
   * 
   * @param entityManager
   * @param requestData
   * @return
   * @throws CmrException
   */
  public abstract AutomationResult<R> executeElement(EntityManager entityManager, RequestData requestData, AutomationEngineData engineData)
      throws Exception;

  /**
   * Builds the base results for {@link AutomationEngine}.
   * 
   * @param reqId
   * @param result
   * @param details
   * @return
   */
  protected AutomationResult<R> buildResult(long reqId) {
    AutomationResult<R> results = new AutomationResult<R>();
    results.setReqId(reqId);
    results.setProcessType(getProcessType());
    results.setProcessCode(getProcessCode());
    results.setProcessDesc(getProcessDesc());
    return results;
  }

  /**
   * Returns the process code assigned for this automation element.
   * 
   * @return
   */
  public abstract String getProcessCode();

  /**
   * Returns the process description assigned for this automation element.
   * 
   * @return
   */
  public abstract String getProcessDesc();

  /**
   * Returns the {@link ProcessType} expected to be performed by this automation
   * element
   * 
   * @return
   */
  public abstract ProcessType getProcessType();

  /**
   * Calls the JPA method to update the entity
   * 
   * @param entity
   * @param entityManager
   */
  public void updateEntity(BaseEntity<?> entity, EntityManager entityManager) {
    entityManager.merge(entity);
    entityManager.flush();
  }

  /**
   * Fixes an element's importable status to NO. Used for elements that can be
   * matching or overrides but can only display the results
   * 
   * @return
   */
  public boolean isNonImportable() {
    return false;
  }

  /**
   * Calls the JPA method to create the entity
   * 
   * @param entity
   * @param entityManager
   */
  public void createEntity(BaseEntity<?> entity, EntityManager entityManager) {
    entityManager.persist(entity);
    entityManager.flush();
  }

  public String getRequestTypes() {
    return requestTypes;
  }

  public void setRequestTypes(String requestTypes) {
    this.requestTypes = requestTypes;
  }

  public ActionOnError getActionOnError() {
    return actionOnError;
  }

  public void setActionOnError(ActionOnError actionOnError) {
    this.actionOnError = actionOnError;
  }

  public boolean isOverrideData() {
    return overrideData;
  }

  public void setOverrideData(boolean overrideData) {
    this.overrideData = overrideData;
  }

  public boolean isStopOnError() {
    return stopOnError;
  }

  public void setStopOnError(boolean stopOnError) {
    this.stopOnError = stopOnError;
  }

  /**
   * Creates a result holder for skipped processes, so that they can be viewed.
   * 
   * @param reqId
   * @return
   */
  public AutomationResult<R> createSkippedResult(long reqId) {
    AutomationResult<R> result = new AutomationResult<>();
    result.setDetails("Checks skipped because of scenario exceptions and/or previous element results.");
    result.setProcessCode(getProcessCode());
    result.setOnError(false);
    result.setProcessDesc(getProcessDesc());
    result.setProcessType(getProcessType());
    result.setReqId(reqId);
    result.setResults("Skipped");
    return result;
  }

  public ScenarioExceptionsUtil getScenarioExceptions(EntityManager entityManager, RequestData requestData, AutomationEngineData engineData) {
    ScenarioExceptionsUtil scenarioExceptions = null;
    if (engineData != null && engineData.containsKey("SCENARIO_EXCEPTIONS")) {
      scenarioExceptions = (ScenarioExceptionsUtil) engineData.get("SCENARIO_EXCEPTIONS");
      return scenarioExceptions;
    } else {
      Data data = requestData.getData();
      scenarioExceptions = new ScenarioExceptionsUtil(entityManager, data.getCmrIssuingCntry(), data.getCountryUse(), data.getCustGrp(),
          data.getCustSubGrp());
      if (engineData != null) {
        engineData.put("SCENARIO_EXCEPTIONS", scenarioExceptions);
      }
      return scenarioExceptions;
    }

  }
}
