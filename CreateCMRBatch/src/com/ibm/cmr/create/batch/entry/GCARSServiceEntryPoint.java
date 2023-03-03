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
    boolean runDownload = "".equals(param) || "DOW".equals(param);

    String contextName = "GCARSProcess";
    if (runExtract) {
      contextName = "GCARSExtract";
    } else if (runUpdate) {
      contextName = "GCARSUpdate";
    } else if (runDownload) {
      contextName = "GCARSDownload";
    }
    BatchEntryPoint.initContext(contextName);

    // gather records first
    GCARSService service = new GCARSService();

    // 1. download files from external FTP first and move to local dir
    if (runDownload) {
      service.setSkipExit(false);
      service.setMode(Mode.Download);
      service.execute();
    }

    // 2. extract the records from the file and move to DB queue
    if (runExtract) {
      service.setSkipExit(runUpdate);
      service.setMode(Mode.Extract);
      service.execute();
    }

    // 3. Process the DB queue
    if (runUpdate) {
      service.setSkipExit(false);
      service.setMode(Mode.Update);
      service.execute();
    }

  }
}