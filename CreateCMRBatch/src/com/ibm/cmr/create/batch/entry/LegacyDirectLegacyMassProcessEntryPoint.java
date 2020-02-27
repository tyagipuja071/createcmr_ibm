/**
 * 
 */
package com.ibm.cmr.create.batch.entry;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cmr.create.batch.service.LegacyDirectLegacyMassProcessService;

/**
 * Entry point for the {@link LegacyDirectMassProcessService} process
 * 
 * @author Dennis NATAD
 * 
 */
public class LegacyDirectLegacyMassProcessEntryPoint extends BatchEntryPoint {

  /**
   * @param args
   */
  public static void main(String[] args) throws CmrException {
    BatchEntryPoint.initContext("LegacyDirectLegacyMassProcessEntryPoint");

    LegacyDirectLegacyMassProcessService service = new LegacyDirectLegacyMassProcessService();
    service.setDevMode(args != null && args.length > 0 && "DEV".equals(args[0]));
    service.execute();
  }

}
