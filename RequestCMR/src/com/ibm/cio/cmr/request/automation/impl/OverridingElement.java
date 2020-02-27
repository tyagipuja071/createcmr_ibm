/**
 * 
 */
package com.ibm.cio.cmr.request.automation.impl;

import com.ibm.cio.cmr.request.automation.AutomationElement;
import com.ibm.cio.cmr.request.automation.ProcessType;
import com.ibm.cio.cmr.request.automation.out.OverrideOutput;

/**
 * {@link AutomationElement} that performs data overrides
 * 
 * @author JeffZAMORA
 * 
 */
public abstract class OverridingElement extends AutomationElement<OverrideOutput> {

  public OverridingElement(String requestTypes, String actionOnError, boolean overrideData, boolean stopOnError) {
    super(requestTypes, actionOnError, overrideData, stopOnError);

  }

  @Override
  public ProcessType getProcessType() {
    return ProcessType.DataOverride;
  }

}
