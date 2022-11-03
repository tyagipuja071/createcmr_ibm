package com.ibm.cmr.create.batch.entry;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cmr.create.batch.service.GCARSService;
import com.ibm.cmr.create.batch.service.GCARSService.Mode;

public class GCARSServiceEntryPoint extends BatchEntryPoint {

  /**
   * @param args
   */
  public static void main(String[] args) throws CmrException {

    String param = args != null && args.length > 0 ? args[0] : "";
    boolean runExtract = "".equals(param) || "EXT".equals(param);
    boolean runUpdate = "".equals(param) || "UPD".equals(param);
    String contextName = "GCARSProcess";
    if (runExtract && !runUpdate) {
      contextName = "GCARSExtract";
    } else if (!runExtract && runUpdate) {
      contextName = "GCARSUpdate";
    }
    BatchEntryPoint.initContext(contextName);

    // gather records first
    GCARSService service = new GCARSService();
    if (runExtract) {
      service.setSkipExit(runUpdate);
      service.setMode(Mode.Extract);
      service.execute();
    }

    if (runUpdate) {
      service.setSkipExit(false);
      service.setMode(Mode.Update);
      service.execute();
    }
  }
}