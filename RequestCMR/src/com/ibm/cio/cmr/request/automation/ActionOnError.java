/**
 * 
 */
package com.ibm.cio.cmr.request.automation;

/**
 * @author JeffZAMORA
 * 
 */
public enum ActionOnError {

  /**
   * Process that sends the request to the processor even when errors are
   * encounterd
   */
  Proceed,
  /**
   * Process that does not change the request's status when errors occur
   */
  Wait,
  /**
   * Process that rejects the request and sends it back to the requester when
   * errors occur
   */
  Reject,
  /**
   * Process that ignores any processing errors encountered and proceeds with
   * the next element or step in the workflow. This treats the element as having
   * been successfully executed even with errors.
   */
  Ignore;

  public static ActionOnError fromCode(String code) {
    if (code != null) {
      if ("W".equalsIgnoreCase(code)) {
        return Wait;
      }
      if ("R".equalsIgnoreCase(code)) {
        return Reject;
      }
      if ("I".equalsIgnoreCase(code)) {
        return Ignore;
      }
    }
    return Proceed;

  }
}
