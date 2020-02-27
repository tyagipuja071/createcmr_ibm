package com.ibm.cio.cmr.request.automation;

/**
 * Contains the different process types for an {@link AutomationElement}
 * 
 * @author JeffZAMORA
 * 
 */
public enum ProcessType {

  /**
   * Processes that validates data on the request and returns a basic true or
   * false with corresponding result details
   */
  Validation,
  /**
   * Processes that perform a specific set of matching based on request
   * characteristics
   */
  Matching,
  /**
   * Processes that perform some computations and propose some data overrides on
   * the current requests
   */
  DataOverride,
  /**
   * Processes the end up creating approvals tagged under the request
   */
  Approvals,
  /**
   * Elements that execute CreateCMR standard processes that automatically
   * import, validate, or override data
   */
  StandardProcess;

  /**
   * Gets the 1-character code for the
   * 
   * @return
   */
  public String toCode() {
    switch (this) {
    case Approvals:
      return "A";
    case DataOverride:
      return "D";
    case Validation:
      return "V";
    case Matching:
      return "M";
    case StandardProcess:
      return "S";
    }
    return "";
  }

}
