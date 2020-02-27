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
 * An object representing specific process output from an
 * {@link AutomationElement} execution
 * 
 * @author JeffZAMORA
 * 
 */
public interface AutomationOutput {

  /**
   * Applies the contents of this output to the given {@link RequestData}
   * 
   * @param entityManager
   * @param requestData
   * @throws CmrException
   */
  public void apply(EntityManager entityManager, AppUser user, RequestData requestData, long resultId, long itemNo, String processCd,
      boolean activeEngine) throws Exception;

  /**
   * Records the specific data under the corresponding tables, from which the
   * {@link #apply(EntityManager, RequestData)} can be called again to import
   * the data to the request
   * 
   * @param entityManager
   * @param requestData
   * @throws Exception
   */
  public void recordData(EntityManager entityManager, long resultId, AppUser user, RequestData requestData) throws Exception;

}
