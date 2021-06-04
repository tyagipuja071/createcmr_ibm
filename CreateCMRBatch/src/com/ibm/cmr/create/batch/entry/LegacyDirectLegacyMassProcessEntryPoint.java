/**
 * 
 */
package com.ibm.cmr.create.batch.entry;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cmr.create.batch.service.BaseBatchService;
import com.ibm.cmr.create.batch.service.LDMassProcessMultiLegacyService;
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
    BatchEntryPoint.initContext("LegacyDirectLegacyMassProcess");

    BaseBatchService service = null;
    if (args != null && args.length > 0 && "MULTI".equalsIgnoreCase(args[0])) {
      service = new LDMassProcessMultiLegacyService();
    } else {
      service = new LegacyDirectLegacyMassProcessService();
    }
    service.execute();
  }

}
