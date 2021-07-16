package com.ibm.cmr.create.batch.entry;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cmr.create.batch.service.BaseBatchService;
import com.ibm.cmr.create.batch.service.SWISSMassProcessMultiService;
import com.ibm.cmr.create.batch.service.SWISSService;

public class SWISSMassProcessEntryPoint extends BatchEntryPoint {

  /**
   * @param args
   */
  public static void main(String[] args) throws CmrException {
    BatchEntryPoint.initContext("SwissMassProcess");

    BaseBatchService service = null;
    if (args != null && args.length > 0 && "MULTI".equalsIgnoreCase(args[0])) {
      service = new SWISSMassProcessMultiService();
    } else {
      SWISSService swissService = new SWISSService();
      swissService.setMassServiceMode(true);
      service = swissService;
    }
    service.execute();
  }

}
