/**
 * 
 */
package com.ibm.cmr.create.batch.entry;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cmr.create.batch.service.AutomationService;

/**
 * Entry point for the {@link AutomationService} batch job
 * 
 * @author JeffZAMORA
 * 
 */
public class AutomationServiceEntryPoint extends BatchEntryPoint {

  public static void main(String[] args) throws CmrException {
    BatchEntryPoint.initContext("Automation", true);
    AutomationService service = new AutomationService();
    service.execute();
  }

}
