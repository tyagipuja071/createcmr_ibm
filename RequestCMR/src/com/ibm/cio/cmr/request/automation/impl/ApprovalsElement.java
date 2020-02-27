/**
 * 
 */
package com.ibm.cio.cmr.request.automation.impl;

import com.ibm.cio.cmr.request.automation.AutomationElement;
import com.ibm.cio.cmr.request.automation.ProcessType;
import com.ibm.cio.cmr.request.automation.out.EmptyOutput;

/**
 * {@link AutomationElement} for approvals
 * 
 * @author JeffZAMORA
 * 
 */
public abstract class ApprovalsElement extends AutomationElement<EmptyOutput> {

  public ApprovalsElement(String requestTypes, String actionOnError, boolean overrideData, boolean stopOnError) {
    super(requestTypes, actionOnError, overrideData, stopOnError);

  }

  @Override
  public ProcessType getProcessType() {
    return ProcessType.Approvals;
  }

}
