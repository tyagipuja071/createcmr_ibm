package com.ibm.cmr.create.batch.entry;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cmr.create.batch.service.FRService;

public class FRServiceEntryPoint extends BatchEntryPoint {

  /**
   * @param args
   */
  public static void main(String[] args) throws CmrException {
    if (args != null && args.length > 0 && "MASS".equals(args[0])) {
      BatchEntryPoint.initContext("FRMassProcess");
    } else {
      BatchEntryPoint.initContext("FRProcess");
    }
    FRService service = new FRService();
    service.setMassServiceMode(args != null && args.length > 0 && "MASS".equals(args[0]));
    service.execute();
  }
}
