/**
 * 
 */
package com.ibm.cio.cmr.request.automation.out;

import javax.persistence.EntityManager;

import com.ibm.cio.cmr.request.automation.AutomationElement;
import com.ibm.cio.cmr.request.automation.RequestData;
import com.ibm.cio.cmr.request.user.AppUser;

/**
 * Represents output from validating {@link AutomationElement} processes
 * 
 * @author JeffZAMORA
 * 
 */
public class ValidationOutput implements AutomationOutput {

  private boolean success;
  private String message;

  @Override
  public void apply(EntityManager entityManager, AppUser user, RequestData requestData, long resultId, long itemNo, String processCd,
      boolean activeEngine) throws Exception {
    // noop
  }

  @Override
  public void recordData(EntityManager entityManager, long resultId, AppUser user, RequestData requestData) throws Exception {
    // noop
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public boolean isSuccess() {
    return success;
  }

  public void setSuccess(boolean success) {
    this.success = success;
  }

}
