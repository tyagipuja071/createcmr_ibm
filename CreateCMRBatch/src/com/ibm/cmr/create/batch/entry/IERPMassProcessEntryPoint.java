/**
 * 
 */
package com.ibm.cmr.create.batch.entry;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cmr.create.batch.service.BaseBatchService;
import com.ibm.cmr.create.batch.service.IERPMassProcessMultiService;
import com.ibm.cmr.create.batch.service.IERPMassProcessService;

/**
 * Entry point for the {@link IERPMassProcessService} process
 * 
 * @author Dennis NATAD
 * 
 */
public class IERPMassProcessEntryPoint extends BatchEntryPoint {

  /**
   * @param args
   */
  public static void main(String[] args) throws CmrException {
    BatchEntryPoint.initContext("IERPMassProcess");

    BaseBatchService service = null;
    if (args != null && args.length > 0 && "MULTI".equalsIgnoreCase(args[0])) {
      service = new IERPMassProcessMultiService();
    } else {
      service = new IERPMassProcessService();
    }
    service.execute();
  }

}
