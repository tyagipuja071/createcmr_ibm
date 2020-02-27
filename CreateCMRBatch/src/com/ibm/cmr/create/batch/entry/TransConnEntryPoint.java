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
    BatchEntryPoint.initContext("TransConn");

    TransConnService service = new TransConnService();
    if (args != null && args.length > 0 && "DELETE".equalsIgnoreCase(args[0].trim())) {
      service.setDeleteRDcTargets(true);
    }
    service.execute();
  }
}
