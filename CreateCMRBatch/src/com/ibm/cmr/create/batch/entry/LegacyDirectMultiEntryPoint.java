/**
 * 
 */
package com.ibm.cmr.create.batch.entry;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cmr.create.batch.service.LegacyDirectMultiService;
import com.ibm.cmr.create.batch.service.LegacyDirectMultiService.Mode;

/**
 * Entry point for the {@link LegacyDirectMultiService} process
 * 
 * @author clint
 * 
 */
public class LegacyDirectMultiEntryPoint extends BatchEntryPoint {

  /**
   * @param args
   */
  public static void main(String[] args) throws CmrException {
    BatchEntryPoint.initContext("LegacyDirectMulti");

    LegacyDirectMultiService service = new LegacyDirectMultiService();
    // service.setDevMode(args != null && args.length > 0 &&
    // "DEV".equals(args[0]));

    service.setSkipExit(true);
    service.setMode(Mode.PendingLegacy);
    service.execute();

    service.setSkipExit(false);
    service.setMode(Mode.PendingRDC);
    service.execute();

  }

}
