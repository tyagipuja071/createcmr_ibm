/**
 * 
 */
package com.ibm.cmr.create.batch.entry;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cmr.create.batch.service.BaseBatchService;
import com.ibm.cmr.create.batch.service.CloningProcessService;
import com.ibm.cmr.create.batch.service.ProspectCloningService;

/**
 * Entry point for the {@link CloningProcessEntryPoint} process
 * 
 * @author PriyRanjan
 * 
 */
public class CloningProcessEntryPoint extends BatchEntryPoint {

  /**
   * @param args
   */
  public static void main(String[] args) throws CmrException {

    BaseBatchService service = null;
    if (args != null && args.length > 0 && args[0] != null && "PROSPECT".equals(args[0])) {
      BatchEntryPoint.initContext("ProspectCloningProcess");
      service = new ProspectCloningService();
    } else {
      BatchEntryPoint.initContext("CloningProcess");
      service = new CloningProcessService();
    }
    service.execute();

  }

}
