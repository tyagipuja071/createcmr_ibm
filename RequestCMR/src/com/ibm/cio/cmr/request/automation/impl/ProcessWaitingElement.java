/**
 * 
 */
package com.ibm.cio.cmr.request.automation.impl;

import com.ibm.cio.cmr.request.automation.AutomationElement;

/**
 * Interface denoting that a target {@link AutomationElement} implements process
 * waiting
 * 
 * @author JeffZAMORA
 *
 */
public interface ProcessWaitingElement {

  /**
   * Denotes that this element is waiting on an external process
   * 
   * @return
   */
  public boolean isWaiting();

  /**
   * Resets the waiting status of the element. Called once the waiting check is
   * done.
   */
  public void resetWaitingStatus();
}
