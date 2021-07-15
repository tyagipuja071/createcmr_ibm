package com.ibm.cmr.create.batch.entry;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cmr.create.batch.service.BaseBatchService;
import com.ibm.cmr.create.batch.service.FRMassProcessMultiService;
import com.ibm.cmr.create.batch.service.FRService;

public class FRMassProcessServiceEntryPoint extends BatchEntryPoint {

  /**
   * @param args
   */
  public static void main(String[] args) throws CmrException {
    BatchEntryPoint.initContext("FRMassProcess");

    BaseBatchService service = null;
    if (args != null && args.length > 0 && "MULTI".equalsIgnoreCase(args[0])) {
      service = new FRMassProcessMultiService();
    } else {
      FRService frService = new FRService();
      frService.setMassServiceMode(true);
      service = frService;
    }
    service.execute();
  }
}
