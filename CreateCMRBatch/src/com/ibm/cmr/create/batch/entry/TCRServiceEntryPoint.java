package com.ibm.cmr.create.batch.entry;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cmr.create.batch.service.TCRService;
import com.ibm.cmr.create.batch.service.TCRService.Mode;

public class TCRServiceEntryPoint extends BatchEntryPoint {

  /**
   * @param args
   */
  public static void main(String[] args) throws CmrException {
    BatchEntryPoint.initContext("TCRProcess");
    // gather records first
    TCRService service = new TCRService();
    service.setSkipExit(true);
    service.setMode(Mode.Extract);
    service.execute();

    service.setSkipExit(false);
    service.setMode(Mode.Update);
    service.execute();
  }
}