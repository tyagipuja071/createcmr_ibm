/**
 * 
 */
package com.ibm.cio.cmr.request.automation.impl;

import com.ibm.cio.cmr.request.automation.AutomationElement;
import com.ibm.cio.cmr.request.automation.ProcessType;
import com.ibm.cio.cmr.request.automation.out.ValidationOutput;

/**
 * {@link AutomationElement} that perform validations
 * 
 * @author JeffZAMORA
 * 
 */
public abstract class ValidatingElement extends AutomationElement<ValidationOutput> {

  public ValidatingElement(String requestTypes, String actionOnError, boolean overrideData, boolean stopOnError) {
    super(requestTypes, actionOnError, overrideData, stopOnError);

  }

  @Override
  public ProcessType getProcessType() {
    return ProcessType.Validation;
  }

}
