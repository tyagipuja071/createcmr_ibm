package com.ibm.cmr.create.batch.entry;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cmr.create.batch.service.IERPProcessService;

public class IERPServiceEntryPoint extends BatchEntryPoint {

  /**
   * @param args
   */
  public static void main(String[] args) throws CmrException {
    BatchEntryPoint.initContext("IERPProcess");

    IERPProcessService service = new IERPProcessService();
    service.execute();
  }

}
