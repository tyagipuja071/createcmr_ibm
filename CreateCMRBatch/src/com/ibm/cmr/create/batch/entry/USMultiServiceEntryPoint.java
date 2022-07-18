/**
 * 
 */
package com.ibm.cmr.create.batch.entry;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cmr.create.batch.service.USMultiService;
import com.ibm.cmr.create.batch.service.USMultiService.Mode;

/**
 * @author Paul
 *
 */
public class USMultiServiceEntryPoint extends BatchEntryPoint {

  public static void main(String[] args) throws CmrException {
    USMultiService service = null;
    if (args != null && args.length > 0 && "MASS".equals(args[0])) {
      BatchEntryPoint.initContext("USMultiMass");
      service = new USMultiService();
      service.setMode(Mode.MassUpdt);
    } else {
      BatchEntryPoint.initContext("USMulti");
      service = new USMultiService();
      service.setMode(Mode.Single);
    }
    service.execute();
  }
}