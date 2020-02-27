/**
 * 
 */
package com.ibm.cio.cmr.request.automation.out;

import javax.persistence.EntityManager;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.automation.AutomationElement;
import com.ibm.cio.cmr.request.automation.RequestData;
import com.ibm.cio.cmr.request.user.AppUser;

/**
 * A placeholder for {@link AutomationElement} executions that contain no
 * relevant output
 * 
 * @author JeffZAMORA
 * 
 */
public class EmptyOutput implements AutomationOutput {

  @Override
  public void apply(EntityManager entityManager, AppUser user, RequestData requestData, long resultId, long itemNo, String processCd,
      boolean activeEngine) throws CmrException {
    // noop
  }

  @Override
  public void recordData(EntityManager entityManager, long resultId, AppUser user, RequestData requestData) throws Exception {
    // noop
  }

}
