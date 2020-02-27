package com.ibm.cio.cmr.request.automation.impl;

import javax.persistence.EntityManager;

import com.ibm.cio.cmr.request.automation.ProcessType;
import com.ibm.cio.cmr.request.automation.RequestData;
import com.ibm.cio.cmr.request.entity.AutomationMatching;

public abstract class DuplicateCheckElement extends MatchingElement {

  public DuplicateCheckElement(String requestTypes, String actionOnError, boolean overrideData, boolean stopOnError) {
    super(requestTypes, actionOnError, overrideData, stopOnError);

  }

  /**
   * Based on the {@link AutomationMatching} record, import the relevant data
   * into the request
   * 
   * @param entityManager
   * @param requestData
   * @param match
   */
  public abstract boolean importMatch(EntityManager entityManager, RequestData requestData, AutomationMatching match);

  @Override
  public ProcessType getProcessType() {
    return ProcessType.Matching;
  }

}
