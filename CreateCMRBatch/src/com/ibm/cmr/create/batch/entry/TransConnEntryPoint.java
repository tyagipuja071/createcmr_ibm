/**
 * 
 */
package com.ibm.cmr.create.batch.entry;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cmr.create.batch.service.TransConnService;

/**
 * @author Jeffrey Zamora
 * 
 */
public class TransConnEntryPoint extends BatchEntryPoint {

  public static void main(String[] args) throws CmrException {
    BatchEntryPoint.initContext("TransConn", true);

    TransConnService service = new TransConnService();
    if (args != null && args.length > 0 && "DELETE".equalsIgnoreCase(args[0].trim())) {
      service.setDeleteRDcTargets(true);
    }
    if (args != null && args.length > 0 && "MULTI".equalsIgnoreCase(args[0].trim())) {
      service.setMultiMode(true);
    }
    if (args != null && args.length > 1 && "MULTI".equalsIgnoreCase(args[1].trim())) {
      service.setMultiMode(true);
    }
    service.execute();
  }
}
