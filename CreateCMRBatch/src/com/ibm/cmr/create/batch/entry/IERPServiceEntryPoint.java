package com.ibm.cmr.create.batch.entry;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cmr.create.batch.service.BaseBatchService;
import com.ibm.cmr.create.batch.service.IERPProcessMultiService;
import com.ibm.cmr.create.batch.service.IERPProcessService;

public class IERPServiceEntryPoint extends BatchEntryPoint {

  /**
   * @param args
   */
  public static void main(String[] args) throws CmrException {
    BatchEntryPoint.initContext("IERPProcess");

    BaseBatchService service = null;
    if (args != null && args.length > 0 && "MULTI".equalsIgnoreCase(args[0])) {
      service = new IERPProcessMultiService();
    } else {
      service = new IERPProcessService();
    }

    service.execute();
  }

}
