/**
 * 
 */
package com.ibm.cmr.create.batch.entry;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cmr.create.batch.service.LegacyDirectRdcMassProcessService;

/**
 * Entry point for the {@link LegacyDirectMassProcessService} process
 * 
 * @author Dennis NATAD
 * 
 */
public class LegacyDirectRdcMassProcessEntryPoint extends BatchEntryPoint {

  /**
   * @param args
   */
  public static void main(String[] args) throws CmrException {
    BatchEntryPoint.initContext("LegacyDirectRdcMassProcessEntryPoint");

    LegacyDirectRdcMassProcessService service = new LegacyDirectRdcMassProcessService();
    service.setDevMode(args != null && args.length > 0 && "DEV".equals(args[0]));
    service.execute();
  }

}
