/**
 * 
 */
package com.ibm.cio.cmr.request.automation.impl;

import com.ibm.cio.cmr.request.automation.AutomationElement;
import com.ibm.cio.cmr.request.automation.ProcessType;
import com.ibm.cio.cmr.request.automation.out.EmptyOutput;

/**
 * {@link AutomationElement} implementations that perform a standard CreateCMR
 * process that can automatically import, validate, or override data
 * 
 * @author JeffZAMORA
 *
 */
public abstract class StandardProcessElement extends AutomationElement<EmptyOutput> {

  public StandardProcessElement(String requestTypes, String actionOnError, boolean overrideData, boolean stopOnError) {
    super(requestTypes, actionOnError, overrideData, stopOnError);

  }

  @Override
  public ProcessType getProcessType() {
    return ProcessType.StandardProcess;
  }

}
