package com.ibm.cmr.create.batch.entry;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cmr.create.batch.service.TCRService;
import com.ibm.cmr.create.batch.service.TCRService.Mode;

public class TCRServiceEntryPoint extends BatchEntryPoint {

  /**
   * @param args
   */
  public static void main(String[] args) throws CmrException {

    String param = args != null && args.length > 0 ? args[0] : "";
    boolean runExtract = "".equals(param) || "EXT".equals(param);
    boolean runUpdate = "".equals(param) || "UPD".equals(param);
    String contextName = "TCRProcess";
    if (runExtract && !runUpdate) {
      contextName = "TCRExtract";
    } else if (!runExtract && runUpdate) {
      contextName = "TCRUpdate";
    }
    BatchEntryPoint.initContext(contextName);

    // gather records first
    TCRService service = new TCRService();
    if (runExtract) {
      service.setSkipExit(runUpdate);
      service.setMode(Mode.Extract);
      service.execute();
    }

    if (runUpdate) {
      service.setSkipExit(true);
      service.setMode(Mode.Clean);
      service.execute();

      service.setSkipExit(false);
      service.setMode(Mode.Update);
      service.execute();
    }
  }
}