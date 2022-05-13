/**
 * 
 */
package com.ibm.cmr.create.batch.entry;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cmr.create.batch.service.SwissMultiService;
import com.ibm.cmr.create.batch.service.SwissMultiService.Mode;

/**
 * @author 136786PH1
 *
 */
public class SwissMultiServiceEntryPoint extends BatchEntryPoint {

  public static void main(String[] args) throws CmrException {
    SwissMultiService service = null;
    if (args != null && args.length > 0 && "MASS".equals(args[0])) {
      BatchEntryPoint.initContext("SwissMultiMass");
      service = new SwissMultiService();
      service.setMode(Mode.MassUpdt);
    } else {
      BatchEntryPoint.initContext("SwissMulti");
      service = new SwissMultiService();
      service.setMode(Mode.Single);
    }
    service.execute();
  }
}
