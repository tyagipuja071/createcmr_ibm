/**
 * 
 */
package com.ibm.cmr.create.batch.entry;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cmr.create.batch.service.BaseBatchService;
import com.ibm.cmr.create.batch.service.LDMassProcessMultiRdcService;
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
    BatchEntryPoint.initContext("LegacyDirectRdcMassProcess");

    BaseBatchService service = null;
    if (args != null && args.length > 0 && "MULTI".equalsIgnoreCase(args[0])) {
      service = new LDMassProcessMultiRdcService();
    } else {
      service = new LegacyDirectRdcMassProcessService();
    }
    service.execute();
  }

}
