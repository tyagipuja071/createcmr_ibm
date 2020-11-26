package com.ibm.cmr.create.batch.entry;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cmr.create.batch.service.MTService;

/**
 * @author MukeshKumar
 *
 */
public class MTServiceEntryPoint extends BatchEntryPoint {

  /**
   * @param args
   */
  public static void main(String[] args) throws CmrException {
    if (args != null && args.length > 0 && "MASS".equals(args[0])) {
      BatchEntryPoint.initContext("MTMassProcess");
    } else {
      BatchEntryPoint.initContext("MTProcess");
    }
    MTService service = new MTService();
    service.setMassServiceMode(args != null && args.length > 0 && "MASS".equals(args[0]));
    service.execute();
  }
}
