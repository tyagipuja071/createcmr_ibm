package com.ibm.cmr.create.batch.entry;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cmr.create.batch.service.ATService;

public class ATServiceEntryPoint extends BatchEntryPoint {

  /**
   * @param args
   */
  public static void main(String[] args) throws CmrException {
    if (args != null && args.length > 0 && "MASS".equals(args[0])) {
      BatchEntryPoint.initContext("ATMassProcess");
    } else {
      BatchEntryPoint.initContext("ATProcess");
    }
    ATService service = new ATService();
    service.setMassServiceMode(args != null && args.length > 0 && "MASS".equals(args[0]));
    service.execute();
  }
}
