/**
 * 
 */
package com.ibm.cmr.create.batch.entry;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cmr.create.batch.service.BaseBatchService;
import com.ibm.cmr.create.batch.service.MassCreateValidatorMultiService;
import com.ibm.cmr.create.batch.service.MassCreateValidatorService;

/**
 * Entry Point for the Mass Create validator batch program
 * 
 * @author Jeffrey Zamora
 * 
 */
public class MassCreateValidatorEntryPoint extends BatchEntryPoint {

  public static void main(String[] args) throws CmrException {

    BatchEntryPoint.initContext("MassCreateValidator");

    BaseBatchService service = null;
    if (args != null && args.length > 0 && "MULTI".equalsIgnoreCase(args[0])) {
      service = new MassCreateValidatorMultiService();
    } else {
      service = new MassCreateValidatorService();
    }
    service.execute();

  }
}
