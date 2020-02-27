package com.ibm.cmr.create.batch.entry;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cmr.create.batch.service.SWISSService;

public class SWISSServiceEntryPoint extends BatchEntryPoint {

  /**
   * @param args
   */
  public static void main(String[] args) throws CmrException {
    if (args != null && args.length > 0 && "MASS".equals(args[0])) {
      BatchEntryPoint.initContext("SwissMassProcess");
    } else {
      BatchEntryPoint.initContext("SWISSProcess");
    }
    SWISSService service = new SWISSService();
    service.setMassServiceMode(args != null && args.length > 0 && "MASS".equals(args[0]));
    service.execute();
  }
}
