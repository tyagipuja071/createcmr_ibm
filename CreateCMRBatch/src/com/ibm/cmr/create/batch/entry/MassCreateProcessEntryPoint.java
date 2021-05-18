/**
 * 
 */
package com.ibm.cmr.create.batch.entry;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cmr.create.batch.service.BaseBatchService;
import com.ibm.cmr.create.batch.service.MassCreateProcessMultiService;
import com.ibm.cmr.create.batch.service.MassCreateProcessService;

/**
 * @author Rochelle Salazar
 * 
 */
public class MassCreateProcessEntryPoint extends BatchEntryPoint {

  public static void main(String[] args) throws CmrException {
    BatchEntryPoint.initContext("MassCreateProcess");

    BaseBatchService service = null;
    if (args != null && args.length > 0 && "MULTI".equalsIgnoreCase(args[0])) {
      service = new MassCreateProcessMultiService();
    } else {
      service = new MassCreateProcessService();
    }
    service.execute();
  }
}
