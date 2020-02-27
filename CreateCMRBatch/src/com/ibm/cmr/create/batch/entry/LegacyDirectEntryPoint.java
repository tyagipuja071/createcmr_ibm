/**
 * 
 */
package com.ibm.cmr.create.batch.entry;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cmr.create.batch.service.LegacyDirectService;

/**
 * Entry point for the {@link LegacyDirectService} process
 * 
 * @author JeffZAMORA
 * 
 */
public class LegacyDirectEntryPoint extends BatchEntryPoint {

  /**
   * @param args
   */
  public static void main(String[] args) throws CmrException {
    BatchEntryPoint.initContext("LegacyDirect");

    LegacyDirectService service = new LegacyDirectService();
    service.setDevMode(args != null && args.length > 0 && "DEV".equals(args[0]));
    service.execute();
  }

}
